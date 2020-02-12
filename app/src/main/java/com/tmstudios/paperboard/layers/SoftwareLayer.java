package com.tmstudios.paperboard.layers;
import com.tmstudios.paperboard.*;
import android.graphics.*;
import com.android.grafika.gles.*;
//import android.opengl.Matrix;
import android.opengl.GLES20;
import android.view.*;
import android.util.*;
import java.util.*;

public abstract class SoftwareLayer extends Layer
{

	private static int height;
	private static int width;

	private static Drawable2d mDrawable;
	private static int mOffscreenTexture;
	private static SurfaceTexture mSurfaceTexture;
	private static Surface mSurface;
	private static float[] values;
	private static Matrix v;
	private static int mTextureId;
	private static float[] mFlipMatrix;
	public Matrix modelMatrix;
	public static void reset()
	{
		mOffscreenTexture = 0;
		mSurfaceTexture = null;
		mSurface = null;
		v = new Matrix();
		values = new float[9];
	}


	@Override
	public void draw(float[] ortho)
	{
		if (mSurfaceTexture == null)
		{
			setup();
		}
		setTexture(mOffscreenTexture);
		Canvas e = mSurface.lockCanvas(new Rect(0, 0, width, height));
		softDraw(e, new Matrix());
		mSurface.unlockCanvasAndPost(e);
		mSurfaceTexture.updateTexImage();
		draw(mTexProgram, getManager().uiMat);
		mSurfaceTexture.releaseTexImage();
	}

	private static void setTexture(int mOffscreenTexture)
	{
		mTextureId = mOffscreenTexture;
	}
	public static void drawBatch(Stack<SoftwareLayer> layers, float[] ortho, Matrix matrix)
	{
		SoftwareLayer first = layers.firstElement();

		first.setup();

		setTexture(mOffscreenTexture);
		Canvas e = mSurface.lockCanvas(new Rect(0, 0, width, height));
		//e.setMatrix(getMatrix(ortho));
		e.drawColor(Color.argb(0, 0, 0, 0), PorterDuff.Mode.SRC);
		
		for (SoftwareLayer i:layers){
			int b = e.save();
			i.softDraw(e, matrix);
			e.restoreToCount(b);
			}
		mSurface.unlockCanvasAndPost(e);
		mSurfaceTexture.updateTexImage();
		draw(mTexProgram, mFlipMatrix);
		mSurfaceTexture.releaseTexImage();
	}

	public SoftwareLayer(LayerManager m)
	{
		//requires a width and height to start but it is actually
		//not required until setup is called in draw
		super(m);
		mDrawable =  new Drawable2d(Drawable2d.Prefab.FULL_RECTANGLE);
		this.width = getManager().displayW;
		this.height = getManager().displayH;
		modelMatrix=new Matrix();
		//translate -0.5,0.5 to 0,1
	}

	public abstract void softDraw(Canvas canvas, Matrix root)
	@Override
	public void setup()
	{
		// TODO: Implement this method
		super.setup();
		if (mSurfaceTexture == null)
		{
			if (mFlipMatrix == null)
			{
				//static lazy init
				mFlipMatrix = new float[16];
				android.opengl.Matrix.setIdentityM(mFlipMatrix, 0);
				android.opengl.Matrix.scaleM(mFlipMatrix, 0, 1, -1, 1);
				//mFullFrameRect = new FullFrameRect(new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
			}
			GlUtil.checkGlError("prepareFramebuffer start");
			if (mOffscreenTexture < 1)
			{
				int[] values = new int[1];
				// Create a texture object and bind it.  This will be the color buffer.
				GLES20.glGenTextures(1, values, 0);
				GlUtil.checkGlError("glGenTextures");
				mOffscreenTexture = values[0];   // expected > 0	
			}
			mSurfaceTexture  = new SurfaceTexture(mOffscreenTexture, false);
			mSurfaceTexture.setDefaultBufferSize(width, height);
			mSurface = new Surface(mSurfaceTexture);
		}
	}

	@Override
	public void disposeGl()
	{
		// TODO: Implement this method
		super.disposeGl();
		if (mSurface == null)return;
		mSurface.release();
		mSurface = null;
		if (mOffscreenTexture > 0)
		{
			//mSurfaceTexture.detachFromGLContext();
			mSurfaceTexture.release();
			mOffscreenTexture = -1;
			mSurfaceTexture = null;
		}
	}
	public void setSize(int width, int height)
	{
		if (width == this.width && height == this.height)return;
		if (mSurfaceTexture != null)
		    mSurfaceTexture.setDefaultBufferSize(width, height);
		this.width = width;
		this.height = height;
	}

	@Override
	public void onDisplaySizeChanged(int width, int height)
	{
		setSize(width, height);
	}

	@Override
	public boolean isSoftwareLayer()
	{
		// TODO: Implement this method
		return true;
	}

	/**
	 * Draws the rectangle with the supplied program and projection matrix.
	 */
	public static void draw(Texture2dProgram program, float[] projectionMatrix)
	{
		// Compute model/view/projection matrix.

		program.draw(projectionMatrix, mDrawable.getVertexArray(), 0,
					 mDrawable.getVertexCount(), mDrawable.getCoordsPerVertex(),
					 mDrawable.getVertexStride(), GlUtil.IDENTITY_MATRIX, mDrawable.getTexCoordArray(),
					 mTextureId, mDrawable.getTexCoordStride());
	}
	public static void draw(CustomTexture2dProgram program, float[] projectionMatrix, float[] texMatrix)
	{
		program.draw(projectionMatrix, mDrawable.getVertexArray(), 0,
					 mDrawable.getVertexCount(), mDrawable.getCoordsPerVertex(),
					 mDrawable.getVertexStride(), texMatrix, mDrawable.getTexCoordArray(),
					 mTextureId, mDrawable.getTexCoordStride());
	}

}
