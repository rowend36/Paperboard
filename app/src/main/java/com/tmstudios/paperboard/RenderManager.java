package com.tmstudios.paperboard;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Choreographer;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.app.Activity;
import android.graphics.Rect;

import com.android.grafika.gles.Drawable2d;
import com.android.grafika.gles.EglCore;
import com.android.grafika.gles.FlatShadedProgram;
import com.android.grafika.gles.FullFrameRect;
import com.android.grafika.gles.GlUtil;
import com.android.grafika.gles.Texture2dProgram;
import com.android.grafika.gles.WindowSurface;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import com.tmstudios.paperboard.*;
//import com.android.grafika.*;
import com.tmstudios.paperboard.layers.*;
import com.tmstudios.paperboard.resources.*;
import android.opengl.*;
import com.android.grafika.MiscUtils;
import com.tmstudios.paperboard.audio.*;

/**
 * Demonstrates efficient display + recording of OpenGL rendering using an FBO.  This
 * records only the GL surface (i.e. not the app UI, nav bar, status bar, or alert dialog).
 * <p>
 * This uses a plain SurfaceView, rather than GLSurfaceView, so we have full control
 * over the EGL config and rendering.  When available, we use GLES 3, which allows us
 * to do recording with one extra copy instead of two.
 * <p>
 * We use Choreographer so our animation matches vsync, and a separate rendering
 * thread to keep the heavy lifting off of the UI thread.  Ideally we'd let the render
 * thread receive the Choreographer events directly, but that appears to be creating
 * a permanent JNI global reference to the render thread object, preventing it from
 * being garbage collected (which, in turn, causes the Activity to be retained).  So
 * instead we receive the vsync on the UI thread and forward it.
 * <p>
 * If the rendering is fairly simple, it may be more efficient to just render the scene
 * twice (i.e. configure for display, call draw(), configure for video, call draw()).  If
 * the video being created is at a lower resolution than the display, rendering at the lower
 * resolution may produce better-looking results than a downscaling blit.
 * <p>
 * To reduce the impact of recording on rendering (which is probably a fancy-looking game),
 * we want to perform the recording tasks on a separate thread.  The actual video encoding
 * is performed in a separate process by the hardware H.264 encoder, so feeding input into
 * the encoder requires little effort.  The MediaMuxer step runs on the CPU and performs
 * disk I/O, so we really want to drain the encoder on a separate thread.
 * <p>
 * Some other examples use a pair of EGL contexts, configured to share state.  We don't want
 * to do that here, because GLES3 allows us to improve performance by using glBlitFramebuffer(),
 * and framebuffer objects aren't shared.  So we use a single EGL context for rendering to
 * both the display and the video encoder.
 * <p>
 * It might appear that shifting the rendering for the encoder input to a different thread
 * would be advantageous, but in practice all of the work is done by the GPU, and submitting
 * the requests from different CPU cores isn't going to matter.
 * <p>
 * As always, we have to be careful about sharing state across threads.  By fully configuring
 * the encoder before starting the encoder thread, we ensure that the new thread sees a
 * fully-constructed object.  The encoder object then "lives" in the encoder thread.  The main
 * thread doesn't need to talk to it directly, because all of the input goes through Surface.
 */
public class RenderManager implements SurfaceHolder.Callback,
Choreographer.FrameCallback
{

	public EncoderThread mVideoEncoder;
	private static final String TAG = "MainActivity.TAG";

	private static final int RECMETHOD_DRAW_TWICE = 0;
	private static final int RECMETHOD_FBO = 1;
	private static final int RECMETHOD_BLIT_FRAMEBUFFER = 2;
	private boolean mBlitFramebufferAllowed = false;    // requires GLES3
	private int mSelectedRecordMethod;                  // current radio button

	private RenderThread mRenderThread;
	private MainActivity mActivity;

	private SurfaceView view;

	@Override
	public RenderManager(MainActivity act, SurfaceView v)
	{
		this.mActivity = act;
		this.view = v;
		mSelectedRecordMethod = RECMETHOD_BLIT_FRAMEBUFFER;
		v.getHolder().addCallback(this);
		Log.d(TAG, "RenderManager: constructed");
	}
	

	public boolean isRecording(){
		return mRenderThread.mRecordingEnabled;
	}
	public boolean isRecordPaused(){
		return mRenderThread.mVideoPaused;
	}
	protected void pause()
	{
		Log.d(TAG, "onPause unhooking choreographer");
		Choreographer.getInstance().removeFrameCallback(this);
		//we actually just want the recording to be flushed
		pauseRecording();
	}
	public void pauseRecording(){
		if(mRenderThread!=null){
			mRenderThread.getHandler().sendPauseRecording();
		}
	}
	public void resumeRecording(){
		if(mRenderThread!=null){
			mRenderThread.getHandler().sendResumeRecording();
		}
	}
	
	protected void resume()
	{
		// If we already have a Surface, we just need to resume the frame notifications.
		if (mRenderThread != null)
		{
			Log.d(TAG, "onResume re-hooking choreographer");
			Choreographer.getInstance().postFrameCallback(this);
			mRenderThread.getHandler().sendResumeRecording();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		Log.d(TAG, "surfaceCreated holder=" + holder);
		//There is space to actually create a render thread with a premade
		//encoder probably one that does something different
		//or continues a recording
		mRenderThread = new RenderThread(view.getHolder(), mActivity.getLayerManager(), new ActivityHandler(this),
										 mVideoEncoder
										 , MiscUtils.getDisplayRefreshNsec(mActivity));
		mRenderThread.setName("RecordFBO GL render");
		mRenderThread.start();
		mRenderThread.waitUntilReady();
		mRenderThread.setRecordMethod(mSelectedRecordMethod);

		RenderHandler rh = mRenderThread.getHandler();
		if (rh != null)
		{
			rh.sendSurfaceCreated();
		}

		// start the draw events
		Choreographer.getInstance().postFrameCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		Log.d(TAG, "surfaceChanged fmt=" + format + " size=" + width + "x" + height +
			  " holder=" + holder);
		RenderHandler rh = mRenderThread.getHandler();
		if (rh != null)
		{
			rh.sendSurfaceChanged(format, width, height);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Log.d(TAG, "surfaceDestroyed holder=" + holder);

		// We need to wait for the render thread to shut down before continuing because we
		// don't want the Surface to disappear out from under it mid-render.  The frame
		// notifications will have been stopped back in onPause(), but there might have
		// been one in progress.
		//
		// TODO: the RenderThread doesn't currently wait for the encoder / muxer to stop,
		//       so we can't use this as an indication that the .mp4 file is complete.

		RenderHandler rh = mRenderThread.getHandler();
		if (rh != null)
		{
			rh.sendShutdown();
			try
			{
				mRenderThread.join();
			}
			catch (InterruptedException ie)
			{
				// not expected
				throw new RuntimeException("join was interrupted", ie);
			}
		}
		//keep a reference to the recorder???
		//mVideoEncoder=mRenderThread.mVideoEncoder;

		mRenderThread = null;


		// If the callback was posted, remove it.  Without this, we could get one more
		// call on doFrame().
		Choreographer.getInstance().removeFrameCallback(this);
		Log.d(TAG, "surfaceDestroyed complete");
	}


	/*
	 * Choreographer callback, called near vsync.
	 *
	 * @see android.view.Choreographer.FrameCallback#doFrame(long)
	 */
	@Override
	public void doFrame(long frameTimeNanos)
	{
		RenderHandler rh = mRenderThread.getHandler();
		if (rh != null)
		{
			Choreographer.getInstance().postFrameCallback(this);
			rh.sendDoFrame(frameTimeNanos);
		}
	}

	/**
	 * Updates the GLES version string.
	 * <p>
	 * Called from the render thread (via ActivityHandler) after the EGL context is created.
	 */
	void handleShowGlesVersion(int version)
	{
		if (version >= 3)
		{
			mBlitFramebufferAllowed = true;
		}
	}



	public void setRecording(boolean on, File output)
	{
		Log.d(TAG, "clickToggleRecording");
		RenderHandler rh = mRenderThread.getHandler();
		if (rh != null)
		{
			rh.setRecordingEnabled(on, output);
		}
	}

	/**
	 * onClick handler for radio buttons.
	 */
	public void setRecordMode(int mode)
	{
		mSelectedRecordMethod = mode;
		Log.d(TAG, "Selected rec mode " + mSelectedRecordMethod);
		RenderHandler rh = mRenderThread.getHandler();
		if (rh != null)
		{
			rh.setRecordMethod(mSelectedRecordMethod);
		}
	}

	/**
	 * Updates the on-screen controls to reflect the current state of the app.
	 */

	/**
	 * Handles messages sent from the render thread to the UI thread.
	 * <p>
	 * The object is created on the UI thread, and all handlers run there.
	 */
	static class ActivityHandler extends Handler
	{
		private static final int MSG_GLES_VERSION = 0;
		private static final int MSG_UPDATE_FPS = 1;

		// Weak reference to the Activity; only access this from the UI thread.
		private WeakReference<RenderManager> mWeakManager;

		public ActivityHandler(RenderManager activity)
		{
			mWeakManager = new WeakReference<RenderManager>(activity);
		}

		/**
		 * Send the GLES version.
		 * <p>
		 * Call from non-UI thread.
		 */
		public void sendGlesVersion(int version)
		{
			sendMessage(obtainMessage(MSG_GLES_VERSION, version, 0));
		}

		/**
		 * Send an FPS update.  "fps" should be in thousands of frames per second
		 * (i.e. fps * 1000), so we can get fractional fps even though the Handler only
		 * supports passing integers.
		 * <p>
		 * Call from non-UI thread.
		 */
		public void sendFpsUpdate(int tfps, int dropped)
		{
			sendMessage(obtainMessage(MSG_UPDATE_FPS, tfps, dropped));
		}

		@Override  // runs on UI thread
		public void handleMessage(Message msg)
		{
			int what = msg.what;
			//Log.d(TAG, "ActivityHandler [" + this + "]: what=" + what);

			RenderManager activity = mWeakManager.get();
			if (activity == null)
			{
				Log.w(TAG, "ActivityHandler.handleMessage: activity is null");
				return;
			}

			switch (what)
			{
				case MSG_GLES_VERSION:
					activity.handleShowGlesVersion(msg.arg1);
					break;
				case MSG_UPDATE_FPS:
					//activity.handleUpdateFps(msg.arg1, msg.arg2);
					break;
				default:
					throw new RuntimeException("unknown msg " + what);
			}
		}
	}

	//start after surface has been prepared
	private static class RenderThread extends Thread
	{
		private volatile RenderHandler mHandler;

		// Handler we can send messages to if we want to update the app UI.
		private ActivityHandler mActivityHandler;

		// Used to wait for the thread to start.
		private Object mStartLock = new Object();
		private boolean mReady = false;

		private volatile SurfaceHolder mSurfaceHolder;
		// may be updated by UI thread

		private EglCore mEglCore;
		private WindowSurface mWindowSurface;

		// Orthographic projection matrix.
		//private float[] mDisplayProjectionMatrix = new float[16];

		private final float[] mIdentityMatrix;
		
		// Previous frame time.
		private long mPrevTimeNanos=0;
		private long mVideoTimeStamp=0;

		// FPS / drop counter.
		private long mRefreshPeriodNanos;
		private long mFpsCountStartNanos;
		private int mFpsCountFrame;
		private int mDroppedFrames;
		private boolean mPreviousWasDropped;

		// Used for off-screen rendering.
		private int mOffscreenTexture;
		private int mFramebuffer;
		private int mDepthBuffer;
		private FullFrameRect mFullScreen;

		// Used for recording.
		private boolean mRecordingEnabled;
		public  EncoderThread mVideoEncoder;
		public volatile File mOutputFile;
		private WindowSurface mInputWindowSurface;
		private int mRecordMethod;
		private boolean mRecordedPrevious;
		private Rect mVideoRect;
		
		private LayerManager mLayerManager;


		private boolean mVideoPaused;

		final int BIT_RATE = 4000000;   // 4Mbps
		final int VIDEO_WIDTH = 1280;
		final int VIDEO_HEIGHT = 720;

		private boolean mPreviewPaused;

		private MicrophoneEncoder mAudioThread;



		/**
		 * Pass in the SurfaceView's SurfaceHolder.  Note the Surface may not yet exist.
		 */
		public RenderThread(SurfaceHolder holder, LayerManager lm, ActivityHandler ahandler,
							EncoderThread encoder,
							long refreshPeriodNs)
		{
			mSurfaceHolder = holder;
			mActivityHandler = ahandler;
			mRefreshPeriodNanos = refreshPeriodNs;
			mLayerManager = lm;
			mVideoRect = new Rect();

			mIdentityMatrix = new float[16];
			Matrix.setIdentityM(mIdentityMatrix, 0);
			mVideoPaused = false;
			mVideoEncoder = encoder;
			if (encoder != null){
				mVideoPaused = true;
				mRecordingEnabled = true;
				}
		}

		//There could be a way to record kn background
		//But for now, since we need the egl context
		//Recording is done on ui
		//However, we can pause the rendering to screen to
		//speed things up, by using mPreviewPaused
		//Using this we can avoid destroying the renderthread on surface destroyed
		//and leave that to on pause
		//To be implemented
		private void pauseRecording()
		{
			if (mRecordingEnabled)
				this.mVideoEncoder.frameAvailableSoon();
			mVideoPaused = true;
		}
		private void resumeRecording()
		{
			mVideoPaused = false;
		}
		private void pauseRendering()
		{
			mPreviewPaused = true;
		}
		private void resumeRendering()
		{
			mPreviewPaused = false;
		}
		public boolean isPreviewPaused(){
			return mPreviewPaused;
		}
		public void setRecordingEnabled(boolean arg1)
		{
			if (mRecordingEnabled == arg1)
			{
				return;
			}
			if (arg1)
			{
				if (mOutputFile != null)
				{
					startEncoder();
					mRecordingEnabled = true;
					mVideoPaused = false;}
				else
				{
					throw new IllegalStateException("OutputFile cannot be null");
				}}
			else
			{
				stopEncoder();
			}
		}

		/**
		 * Thread entry point.
		 * <p>
		 * The thread should not be started until the Surface associated with the SurfaceHolder
		 * has been created.  That way we don't have to wait for a separate "surface created"
		 * message to arrive.
		 */
		@Override
		public void run()
		{
			Looper.prepare();
			mHandler = new RenderHandler(this);
			mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE | EglCore.FLAG_TRY_GLES3);
			synchronized (mStartLock)
			{
				mReady = true;
				mStartLock.notify();    // signal waitUntilReady()
			}

			Looper.loop();

			Log.d(TAG, "looper quit");
			releaseGl();
			mEglCore.release();

			synchronized (mStartLock)
			{
				mReady = false;
			}
		}

		/**
		 * Waits until the render thread is ready to receive messages.
		 * <p>
		 * Call from the UI thread.
		 */
		public void waitUntilReady()
		{
			synchronized (mStartLock)
			{
				while (!mReady)
				{
					try
					{
						mStartLock.wait();
					}
					catch (InterruptedException ie)
					{ /* not expected */ }
				}
			}
		}

		/**
		 * Shuts everything down.
		 */
		private void shutdown()
		{
			Log.d(TAG, "shutdown");
			stopEncoder();
			Looper.myLooper().quit();
		}

		/**
		 * Returns the render thread's Handler.  This may be called from any thread.
		 */
		public RenderHandler getHandler()
		{
			return mHandler;
		}

		/**
		 * Prepares the surface.
		 */
		private void surfaceCreated()
		{
			Surface surface = mSurfaceHolder.getSurface();
			prepareGl(surface);
		}

		/**
		 * Prepares window surface and GL state.
		 */
		private void prepareGl(Surface surface)
		{

			mWindowSurface = new WindowSurface(mEglCore, surface, false);
			mWindowSurface.makeCurrent();

			// Used for blitting texture to FBO.
			mFullScreen = new FullFrameRect(
				new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_2D));

			// Set the background color.
			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

			// Disable depth testing -- we're 2D only.
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);

			// Don't need backface culling.  (If you're feeling pedantic, you can turn it on to
			// make sure we're defining our shapes correctly.)
			GLES20.glDisable(GLES20.GL_CULL_FACE);

			mActivityHandler.sendGlesVersion(mEglCore.getGlVersion());
		}

		/**
		 * Handles changes to the size of the underlying surface.  Adjusts viewport as needed.
		 * Must be called before we start drawing.
		 * (Called from RenderHandler.)
		 */
		private void surfaceChanged(int width, int height)
		{
			//todo allow recording at higher resolution than screen
			Log.d(TAG, "surfaceChanged " + width + "x" + height);
			prepareFramebuffer(width, height);
			// Use full window.
			GLES20.glViewport(0, 0, width, height);
			int windowWidth = width;
			int windowHeight=height;
			float windowAspect = (float) windowHeight / (float) windowWidth;
			int outWidth, outHeight;
			if (VIDEO_HEIGHT > VIDEO_WIDTH * windowAspect)
			{
				// limited by narrow width; reduce height
				outWidth = VIDEO_WIDTH;
				outHeight = (int) (VIDEO_WIDTH * windowAspect);
			}
			else
			{
				// limited by short height; restrict width
				outHeight = VIDEO_HEIGHT;
				outWidth = (int) (VIDEO_HEIGHT / windowAspect);
			}
			int offX = (VIDEO_WIDTH - outWidth) / 2;
			int offY = (VIDEO_HEIGHT - outHeight) / 2;
			mVideoRect.set(offX, offY, offX + outWidth, offY + outHeight);
			Log.d(TAG, "Adjusting window " + windowWidth + "x" + windowHeight +
				  " to +" + offX + ",+" + offY + " " +
				  mVideoRect.width() + "x" + mVideoRect.height());
			//if(windowAspect>1)
			//	Matrix.orthoM(mDisplayProjectionMatrix,0,0,1,0,windowAspect,-1,1);
			//else

			//	Matrix.orthoM(mDisplayProjectionMatrix,0,0,1f/windowAspect,0,1,-1,1);
			mLayerManager.dispatchDisplayChange(width,height);
		}

		/**
		 * Prepares the off-screen framebuffer.
		 */
		private void prepareFramebuffer(int width, int height)
		{
			GlUtil.checkGlError("prepareFramebuffer start");
			Trace.beginSection("prepare Framebuffer");
			int[] values = new int[1];
			// Create a texture object and bind it.  This will be the color buffer.
			GLES20.glGenTextures(1, values, 0);
			GlUtil.checkGlError("glGenTextures");
			//Log.e("pp2",values[0]+"");
			mOffscreenTexture = values[0];   // expected > 0
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOffscreenTexture);
			GlUtil.checkGlError("glBindTexture " + mOffscreenTexture);

			// Create texture storage.
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
								GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

			// Set parameters.  We're probably using non-power-of-two dimensions, so
			// some values may not be available for use.
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
								   GLES20.GL_NEAREST);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
								   GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
								   GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
								   GLES20.GL_CLAMP_TO_EDGE);
			GlUtil.checkGlError("glTexParameter");

			// Create framebuffer object and bind it.
			GLES20.glGenFramebuffers(1, values, 0);
			GlUtil.checkGlError("glGenFramebuffers");
			mFramebuffer = values[0];    // expected > 0
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
			GlUtil.checkGlError("glBindFramebuffer " + mFramebuffer);

			// Create a depth buffer and bind it.
			GLES20.glGenRenderbuffers(1, values, 0);
			GlUtil.checkGlError("glGenRenderbuffers");
			mDepthBuffer = values[0];    // expected > 0
			GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mDepthBuffer);
			GlUtil.checkGlError("glBindRenderbuffer " + mDepthBuffer);

			// Allocate storage for the depth buffer.
			GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
										 width, height);
			GlUtil.checkGlError("glRenderbufferStorage");

			// Attach the depth buffer and the texture (color buffer) to the framebuffer object.
			GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
											 GLES20.GL_RENDERBUFFER, mDepthBuffer);
			GlUtil.checkGlError("glFramebufferRenderbuffer");
			GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
										  GLES20.GL_TEXTURE_2D, mOffscreenTexture, 0);
			GlUtil.checkGlError("glFramebufferTexture2D");

			// See if GLES is happy with all this.
			int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
			if (status != GLES20.GL_FRAMEBUFFER_COMPLETE)
			{
				throw new RuntimeException("Framebuffer not complete, status=" + status);
			}

			// Switch back to the default framebuffer.
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			Trace.endSection();
			GlUtil.checkGlError("prepareFramebuffer done");
		}

		/**
		 * Releases most of the GL resources we currently hold.
		 * <p>
		 * Does not release EglCore.
		 */
		private void releaseGl()
		{
			GlUtil.checkGlError("releaseGl start");

			int[] values = new int[1];
			
			if (mWindowSurface != null)
			{
				mWindowSurface.release();
				mWindowSurface = null;
			}
			/*if (mProgram != null)
			 {
			 mProgram.release();
			 mProgram = null;
			 }*/
			if (mOffscreenTexture > 0)
			{
				values[0] = mOffscreenTexture;
				GLES20.glDeleteTextures(1, values, 0);
				mOffscreenTexture = -1;
			}
			if (mFramebuffer > 0)
			{
				values[0] = mFramebuffer;
				GLES20.glDeleteFramebuffers(1, values, 0);
				mFramebuffer = -1;
			}
			if (mDepthBuffer > 0)
			{
				values[0] = mDepthBuffer;
				GLES20.glDeleteRenderbuffers(1, values, 0);
				mDepthBuffer = -1;
			}
			if (mFullScreen != null)
			{
				mFullScreen.release(false); // TODO: should be "true"; must ensure mEglCore current
				mFullScreen = null;
			}
			mLayerManager.disposeGl();
			GlUtil.checkGlError("releaseGl done");

			mEglCore.makeNothingCurrent();
		}


		/**
		 * Changes the method we use to render frames to the encoder.
		 */
		private void setRecordMethod(int recordMethod)
		{
			Log.d(TAG, "RT: setRecordMethod " + recordMethod);
			mRecordMethod = recordMethod;
		}

		/**
		 * Creates the video encoder object and starts the encoder thread.  Creates an EGL
		 * surface for encoder input.
		 */
		private void startEncoder()
		{
			Log.d(TAG, "starting to record");
			// Record at 1280x720, regardless of the window dimensions.  The encoder may
			// explode if given "strange" dimensions, e.g. a width that is not a multiple
			// of 16.  We can box it as needed to preserve dimensions.


			VideoEncoderCore videoCore;
			AudioEncoderCore audioCore;
			AndroidMuxer muxer;
			try
			{
				muxer = AndroidMuxer.create(mOutputFile.getAbsolutePath(),AndroidMuxer.FORMAT.MPEG4);
				SessionConfig config = new SessionConfig.Builder(muxer).build();
				videoCore = new VideoEncoderCore(config.getVideoWidth(), config.getVideoHeight(),
												 config.getVideoBitrate(), muxer);
				mAudioThread = new MicrophoneEncoder(config);
				audioCore=mAudioThread.mEncoderCore;
			}
			catch (IOException ioe)
			{
				throw new RuntimeException(ioe);
			}
			mInputWindowSurface = new WindowSurface(mEglCore, videoCore.getInputSurface(), true);
			mVideoEncoder = new EncoderThread(videoCore,audioCore,muxer);
			
			Log.e("hello","hello");
			mAudioThread.setThread(mVideoEncoder);
			Log.e("error",""+5);
			
			mAudioThread.startRecording();
			Log.e("error",""+6);
			
			mVideoTimeStamp = System.nanoTime();
			Log.e("error",""+7);
		}

		/**
		 * Stops the video encoder if it's running.
		 */
		private void stopEncoder()
		{
			mRecordingEnabled=false;

			if(mAudioThread != null){
				mAudioThread.stopRecording();
				mAudioThread=null;
			}
			if (mVideoEncoder != null)
			{
				Log.d(TAG, "stopping recorder, mVideoEncoder=" + mVideoEncoder);
				
				mVideoEncoder.stopRecording();
				// TODO: wait (briefly) until it finishes shutting down so we know file is
				//       complete, or have a callback that updates the UI
				mVideoEncoder = null;
			}
			if (mInputWindowSurface != null)
			{
				mInputWindowSurface.release();
				mInputWindowSurface = null;
			}
		}

		/**
		 * Advance state and draw frame in response to a vsync event.
		 */
		private void doFrame(long timeStampNanos)
		{
			if(!mVideoPaused)
				if(mPrevTimeNanos!=0)
					//skips first frame
					mVideoTimeStamp += timeStampNanos - mPrevTimeNanos;
			mPrevTimeNanos = timeStampNanos;
			
			long diff = System.nanoTime() - timeStampNanos;
			long max = mRefreshPeriodNanos - 2000000;   // if we're within 2ms, don't bother
			if (diff > max)
			{
				// too much, drop a frame
				//Log.d(TAG, "diff is " + (diff / 1000000.0) + " ms, max " + (max / 1000000.0) +
					  //", skipping render");
				mRecordedPrevious = false;
				mPreviousWasDropped = true;
				mDroppedFrames++;
				return;
			}

			boolean swapResult;

			if (!mRecordingEnabled || mRecordedPrevious || mVideoPaused)
			{
				mRecordedPrevious = false;

				// Render the scene, swap back to front.
				draw();
				swapResult = mWindowSurface.swapBuffers();
			}
			else
			{
				mRecordedPrevious = true;

				// recording
				if (mRecordMethod == RECMETHOD_DRAW_TWICE)
				{
					//if (true)
					//throw(new RuntimeException("alls good"));
					//Log.d(TAG, "MODE: draw 2x");

					// Draw for display, swap.
					draw();
					swapResult = mWindowSurface.swapBuffers();

					// Draw for recording, swap.
					mVideoEncoder.frameAvailableSoon();
					mInputWindowSurface.makeCurrent();
					GLES20.glClearColor(0,0,0,1);
					GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
					GLES20.glViewport(mVideoRect.left, mVideoRect.top,
									  mVideoRect.width(), mVideoRect.height());
					GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
					GLES20.glScissor(mVideoRect.left, mVideoRect.top,
									 mVideoRect.width(), mVideoRect.height());
					draw();
					GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
					mInputWindowSurface.setPresentationTime(mVideoTimeStamp);
					mInputWindowSurface.swapBuffers();

					GLES20.glViewport(0, 0, mWindowSurface.getWidth(), mWindowSurface.getHeight());
					//GLES20.glViewport((int)(-mVideoRect.left*mDrawTwiceScalingX), (int)(-mVideoRect.top*mDrawTwiceScalingY), (int)(VIDEO_WIDTH*mDrawTwiceScalingX), (int)(VIDEO_HEIGHT*mDrawTwiceScalingY));

					mWindowSurface.makeCurrent();

				}
				else if (mEglCore.getGlVersion() >= 3 &&
						 mRecordMethod == RECMETHOD_BLIT_FRAMEBUFFER)
				{

					//Log.d(TAG, "MODE: blitFramebuffer");
					// Draw the frame, but don't swap it yet.
					draw();

					mVideoEncoder.frameAvailableSoon();
					mInputWindowSurface.makeCurrentReadFrom(mWindowSurface);
					// Clear the pixels we're not going to overwrite with the blit.  Once again,
					// this is excessive -- we don't need to clear the entire screen.
					GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
					GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
					GlUtil.checkGlError("before glBlitFramebuffer");
					Log.v(TAG, "glBlitFramebuffer: 0,0," + mWindowSurface.getWidth() + "," +
						  mWindowSurface.getHeight() + "  " + mVideoRect.left + "," +
						  mVideoRect.top + "," + mVideoRect.right + "," + mVideoRect.bottom +
						  "  COLOR_BUFFER GL_NEAREST");
					GLES30.glBlitFramebuffer(
						0, 0, mWindowSurface.getWidth(), mWindowSurface.getHeight(),
						mVideoRect.left, mVideoRect.top, mVideoRect.right, mVideoRect.bottom,
						GLES30.GL_COLOR_BUFFER_BIT, GLES30.GL_NEAREST);
					int err;
					if ((err = GLES30.glGetError()) != GLES30.GL_NO_ERROR)
					{
						Log.w(TAG, "ERROR: glBlitFramebuffer failed: 0x" +
							  Integer.toHexString(err));
					}
					mInputWindowSurface.setPresentationTime(mVideoTimeStamp);
					mInputWindowSurface.swapBuffers();

					// Now swap the display buffer.
					mWindowSurface.makeCurrent();
					swapResult = mWindowSurface.swapBuffers();

				}
				else
				{


					//Log.d(TAG, "MODE: offscreen + blit 2x");
					// Render offscreen.
					GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
					GlUtil.checkGlError("glBindFramebuffer");
					draw();

					// Blit to display.
					GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
					GlUtil.checkGlError("glBindFramebuffer");
					mFullScreen.drawFrame(mOffscreenTexture, mIdentityMatrix);
					swapResult = mWindowSurface.swapBuffers();

					// Blit to encoder.
					mVideoEncoder.frameAvailableSoon();
					mInputWindowSurface.makeCurrent();
					GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);    // again, only really need to
					GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);     //  clear pixels outside rect
					GLES20.glViewport(mVideoRect.left, mVideoRect.top,
									  mVideoRect.width(), mVideoRect.height());
					mFullScreen.drawFrame(mOffscreenTexture, mIdentityMatrix);
					mInputWindowSurface.setPresentationTime(mVideoTimeStamp);
					mInputWindowSurface.swapBuffers();

					// Restore previous values.
					GLES20.glViewport(0, 0, mWindowSurface.getWidth(), mWindowSurface.getHeight());
					mWindowSurface.makeCurrent();
				}
			}

			mPreviousWasDropped = false;

			if (!swapResult)
			{
				// This can happen if the Activity stops without waiting for us to halt.
				Log.w(TAG, "swapBuffers failed, killing renderer thread");
				shutdown();
				return;
			}

			// Update the FPS counter.
			//
			// Ideally we'd generate something approximate quickly to make the UI look
			// reasonable, then ease into longer sampling periods.
			final int NUM_FRAMES = 120;
			final long ONE_TRILLION = 1000000000000L;
			if (mFpsCountStartNanos == 0)
			{
				mFpsCountStartNanos = timeStampNanos;
				mFpsCountFrame = 0;
			}
			else
			{
				mFpsCountFrame++;
				if (mFpsCountFrame == NUM_FRAMES)
				{
					// compute thousands of frames per second
					long elapsed = timeStampNanos - mFpsCountStartNanos;
					mActivityHandler.sendFpsUpdate((int)(NUM_FRAMES * ONE_TRILLION / elapsed),
												   mDroppedFrames);

					// reset
					mFpsCountStartNanos = timeStampNanos;
					mFpsCountFrame = 0;
				}
			}
		}



		/**
		 * Draws the scene.
		 */
		private void draw()
		{
			GlUtil.checkGlError("draw start");

			// Clear to a non-black color to make the content easily differentiable from
			// the pillar-/letter-boxing.
			//mRecordRect.draw(mProgram, mDisplayProjectionMatrix);
			//mRecordRect.setRotation(0.8f);
			mLayerManager.draw();
			GlUtil.checkGlError("draw done");
		}
	}

	/**
	 * Handler for RenderThread.  Used for messages sent from the UI thread to the render thread.
	 * <p>
	 * The object is created on the render thread, and the various "send" methods are called
	 * from the UI thread.
	 */
	private static class RenderHandler extends Handler
	{
		private static final int MSG_SURFACE_CREATED = 0;
		private static final int MSG_SURFACE_CHANGED = 1;
		private static final int MSG_DO_FRAME = 2;
		private static final int MSG_RECORDING_ENABLED = 3;
		private static final int MSG_RECORD_METHOD = 4;
		private static final int MSG_SHUTDOWN = 5;

		// This shouldn't need to be a weak ref, since we'll go away when the Looper quits,
		// but no real harm in it.
		private WeakReference<RenderThread> mWeakRenderThread;

		private static final int MSG_PAUSE = 6;

		private static final int MSG_RESUME = 7;

		/**
		 * Call from render thread.
		 */
		public RenderHandler(RenderThread rt)
		{
			mWeakRenderThread = new WeakReference<RenderThread>(rt);
		}

		public void sendResumeRecording()
		{
			sendMessage(obtainMessage(RenderHandler.MSG_RESUME));
		}

		/**
		 * Sends the "surface created" message.
		 * <p>
		 * Call from UI thread.
		 */
		public void sendSurfaceCreated()
		{
			sendMessage(obtainMessage(RenderHandler.MSG_SURFACE_CREATED));
		}

		/**
		 * Sends the "surface changed" message, forwarding what we got from the SurfaceHolder.
		 * <p>
		 * Call from UI thread.
		 */
		public void sendSurfaceChanged(@SuppressWarnings("unused") int format,
									   int width, int height)
		{
			// ignore format
			sendMessage(obtainMessage(RenderHandler.MSG_SURFACE_CHANGED, width, height));
		}
		/**
		 * Sends the "do frame" message, forwarding the Choreographer event.
		 * <p>
		 * Call from UI thread.
		 */
		public void sendDoFrame(long frameTimeNanos)
		{
			sendMessage(obtainMessage(RenderHandler.MSG_DO_FRAME,
									  (int) (frameTimeNanos >> 32), (int) frameTimeNanos));
		}

		/**
		 * Enable or disable recording.
		 * <p>
		 * Call from non-UI thread.
		 */
		public void setRecordingEnabled(boolean enabled, File output)
		{
			sendMessage(obtainMessage(MSG_RECORDING_ENABLED, enabled ? 1 : 0, 0, output));
		}

		/**
		 * Set the method used to render a frame for the encoder.
		 * <p>
		 * Call from non-UI thread.
		 */
		public void setRecordMethod(int recordMethod)
		{
			sendMessage(obtainMessage(MSG_RECORD_METHOD, recordMethod, 0));
		}
		public void sendPauseRecording()
		{
			sendMessage(obtainMessage(RenderHandler.MSG_PAUSE));
		}
		/**
		 * Sends the "shutdown" message, which tells the render thread to halt.
		 * <p>
		 * Call from UI thread.
		 */
		public void sendShutdown()
		{
			sendMessage(obtainMessage(RenderHandler.MSG_SHUTDOWN));
		}

		@Override  // runs on RenderThread
		public void handleMessage(Message msg)
		{
			int what = msg.what;
			//Log.d(TAG, "RenderHandler [" + this + "]: what=" + what);

			RenderThread renderThread = mWeakRenderThread.get();
			if (renderThread == null)
			{
				Log.w(TAG, "RenderHandler.handleMessage: weak ref is null");
				return;
			}

			switch (what)
			{
				case MSG_SURFACE_CREATED:
					renderThread.surfaceCreated();
					break;
				case MSG_SURFACE_CHANGED:
					renderThread.surfaceChanged(msg.arg1, msg.arg2);
					break;
				case MSG_DO_FRAME:
					long timestamp = (((long) msg.arg1) << 32) |
						(((long) msg.arg2) & 0xffffffffL);
					renderThread.doFrame(timestamp);
					break;
				case MSG_RECORDING_ENABLED:
					if (msg.arg1 != 0)
						renderThread.mOutputFile = (File) msg.obj;
					renderThread.setRecordingEnabled(msg.arg1 != 0);
					break;
				case MSG_RECORD_METHOD:
					renderThread.setRecordMethod(msg.arg1);
					break;
				case MSG_SHUTDOWN:
					renderThread.shutdown();
					break;
				case MSG_PAUSE:
					renderThread.pauseRecording();
					break;
				case MSG_RESUME:
					renderThread.resumeRecording();
					break;
				default:
					throw new RuntimeException("unknown message " + what);
			}
		}
	}

}
