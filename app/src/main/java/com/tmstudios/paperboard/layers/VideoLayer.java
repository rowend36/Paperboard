package com.tmstudios.paperboard.layers;
import android.graphics.*;
import android.view.*;
import com.android.grafika.gles.*;
import android.opengl.*;
import com.tmstudios.paperboard.resources.*;
import com.tmstudios.paperboard.*;
import com.tmstudios.paperboard.util.*;
import android.util.*;
//import com.tmstudios.paperboard.util.math.*;

public class VideoLayer extends TextureLayer
{

	protected SurfaceTexture mVideoSurfaceTexture;
	private Surface mVideoSurface;
	private OnSurfacePrepareListener mOnSurfacePrepareListener;
	private float mAspectRatio=1;
	private VideoResource mVideoResource;

	private boolean mSelected;

	public void setAspectRatio(float videoAspectRatio)
	{
		float currentAspectRatio = (float) width / height;
		float w=500,h=500;
		this.mAspectRatio = videoAspectRatio;		
		if (currentAspectRatio > mAspectRatio)
		{
			this.width = (int) (height * mAspectRatio);
			w=500*mAspectRatio;
		}
		else
		{
			this.height = (int) (width / mAspectRatio);
			h=500/mAspectRatio;
		}
		rectF.set(0,0,w,h);
		rect_points = new float[]{0,0,w,h,0,h,w,0};
		if (mVideoSurfaceTexture != null)
			mVideoSurfaceTexture.setDefaultBufferSize(width, height);
		super.setSize(this.width, this.height);
		//Log.e(String.format("width, %f,height,%f",width,height,)
	}

	@Override
	public void softDraw(Canvas canvas, android.graphics.Matrix root)
	{
		// TODO: Implement this method
		super.softDraw(canvas, root);
		if (mVideoSurfaceTexture == null)
		{
			setup();
		}
		mVideoSurfaceTexture.updateTexImage();
	}

	public void setOnSurfacePrepareListener(OnSurfacePrepareListener surfacePrepared)
	{
		mOnSurfacePrepareListener = surfacePrepared;
		if (mVideoSurface != null)
			mOnSurfacePrepareListener.onSurfacePrepared(mVideoSurface);
	}


	public void setVideoResource(VideoResource resource)
	{
		mVideoResource = resource;
		mVideoResource.setVideoLayer(this);
	}
	@Override
	public void setup()
	{
		super.setup();
		if (mHardTexture < 1)
		{
			//this way we can specify textures in videolayers
			//probably for debug purposes
			GlUtil.checkGlError("prepareFramebuffer start");
			int[] values = new int[1];
			// Create a texture object and bind it.  This will be the color buffer.
			GLES20.glGenTextures(1, values, 0);

			//GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOffscreenTexture);
			GlUtil.checkGlError("glGenTextures");
			mHardTexture = values[0];   // expected > 0	
		}
		mVideoSurfaceTexture  = new SurfaceTexture(mHardTexture, false);
		mVideoSurfaceTexture.setDefaultBufferSize(width, height);
		mVideoSurface = new Surface(mVideoSurfaceTexture);
		mOnSurfacePrepareListener.onSurfacePrepared(mVideoSurface);
		setHardTexture(mHardTexture);
	}


	@Override
	public void disposeGl()
	{
		// TODO: Implement this method
		super.disposeGl();
		mVideoResource.stop();
		if (mHardTexture > 0)
		{
			//mSurfaceTexture.detachFromGLContext();
			mVideoSurfaceTexture.release();
			mHardTexture = -1;
			mVideoSurfaceTexture = null;
		}
	}

	@Override
	public void onDisplaySizeChanged(int width, int height)
	{
		// TODO: Implement this method
		super.onDisplaySizeChanged(width, height);


	}

	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	public interface OnSurfacePrepareListener
	{
		public void onSurfacePrepared(Surface surface);
	}

	@Override
	public void onPause()
	{
		// TODO: Implement this method
		super.onPause();
		mVideoResource.pause();
		//mVideoResource.
	}


	@Override
	public void onResume()
	{
		// TODO: Implement this method
		super.onResume();
		mVideoResource.start();
	}
	public VideoLayer(LayerManager b){
		super(b,400,400,new AbstractHardwareLayer(b));
	}
}

