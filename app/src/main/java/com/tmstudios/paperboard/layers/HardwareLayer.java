package com.tmstudios.paperboard.layers;
import com.tmstudios.paperboard.*;
import com.android.grafika.gles.*;
import android.opengl.*;

public class HardwareLayer extends AbstractHardwareLayer
{
	protected float mAngle;
	protected float mScaleX, mScaleY;
	protected float mPosX, mPosY;

	private float[] mModelMatrix;
	private boolean mMatrixReady;

	
	protected float[] mModelViewMatrix;
	public HardwareLayer(LayerManager b)
	{
		super(b);
		mModelMatrix = new float[16];
		mModelViewMatrix = new float[16];
		mDrawable = new Drawable2d(Drawable2d.Prefab.RECTANGLE);
		setScale(1, 1);
		setPosition(0, 0);
	}
	public void draw(float[] viewMatrix)
	{
		getModelMatrix();
		Matrix.multiplyMM(mModelViewMatrix, 0, viewMatrix, 0, mModelMatrix, 0);
		Matrix.multiplyMM(mapMatrix, 0, getManager().uiMat, 0, mModelViewMatrix, 0);
		draw(mTexProgram, mapMatrix);
	}
	/**
	 * Re-computes mModelViewMatrix, based on the current values for rotation, scale, and
	 * translation.
	 */

	public void recomputeMatrix()
	{
		float[] modelView = mModelMatrix;

		Matrix.setIdentityM(modelView, 0);
		Matrix.translateM(modelView, 0, mPosX, mPosY, 0.0f);
		if (mAngle != 0.0f)
		{
			Matrix.rotateM(modelView, 0, mAngle, 0.0f, 0.0f, 1.0f);
		}
		Matrix.scaleM(modelView, 0, mScaleX, mScaleY, 1.0f);
		mMatrixReady = true;
	}

	/**
	 * Returns the sprite scale along the X axis.
	 */
	public float getScaleX()
	{
		return mScaleX;
	}

	/**
	 * Returns the sprite scale along the Y axis.
	 */
	public float getScaleY()
	{
		return mScaleY;
	}

	/**
	 * Sets the sprite scale (size).
	 */
	public void setScale(float scaleX, float scaleY)
	{
		mScaleX = scaleX;
		mScaleY = scaleY;
		mMatrixReady = false;
	}

	/**
	 * Gets the sprite rotation angle, in degrees.
	 */
	public float getRotation()
	{
		return mAngle;
	}

	/**
	 * Sets the sprite rotation angle, in degrees.  Sprite will rotate counter-clockwise.
	 */
	public void setRotation(float angle)
	{
		// Normalize.  We're not expecting it to be way off, so just iterate.
		while (angle >= 360.0f)
		{
			angle -= 360.0f;
		}
		while (angle <= -360.0f)
		{
			angle += 360.0f;
		}
		mAngle = angle;
		mMatrixReady = false;
	}

	/**
	 * Returns the position on the X axis.
	 */
	public float getPositionX()
	{
		return mPosX;
	}

	/**
	 * Returns the position on the Y axis.
	 */
	public float getPositionY()
	{
		return mPosY;
	}

	/**
	 * Sets the sprite position.
	 */
	public void setPosition(float posX, float posY)
	{
		mPosX = posX;
		mPosY = posY;
		mMatrixReady = false;
	}

	/**
	 * Returns the model-view matrix.
	 * <p>
	 * To avoid allocations, this returns internal state.  The caller must not modify it.
	 */
	public float[] getModelMatrix()
	{
		if (!mMatrixReady)
		{
			recomputeMatrix();
		}
		return mModelMatrix;
	}

	@Override
	public String toString()
	{
		return "[Sprite2d pos=" + mPosX + "," + mPosY +
			" scale=" + mScaleX + "," + mScaleY + " angle=" + mAngle +
			"} drawable=" + mDrawable + "]";
	}
	/**
	 * Sets color to use for flat-shaded rendering.  Has no effect on textured rendering.
	 */

}
