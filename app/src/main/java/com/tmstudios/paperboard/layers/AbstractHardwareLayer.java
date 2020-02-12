package com.tmstudios.paperboard.layers;
import android.opengl.Matrix;
import android.util.Log;
import com.tmstudios.paperboard.*;
import com.tmstudios.paperboard.util.*;
import com.android.grafika.gles.*;
//import com.tmstudios.paperboard.util.math.*;
import android.graphics.RectF;

public class AbstractHardwareLayer extends Layer
{
	private static final String TAG = GlUtil.TAG;
	public float[] mapMatrix;
	protected Drawable2d mDrawable;
	protected float mColor[];
	public int mTextureId;
	public CustomTexture2dProgram program;
	public AbstractHardwareLayer(LayerManager m)
	{
		super(m);
		this.mapMatrix = new float[16];
	}

	public void setColor(float red, float green, float blue)
	{
		mColor[0] = red;
		mColor[1] = green;
		mColor[2] = blue;
	}

	@Override
	public void draw(float[] ortho)
	{
		Trace.beginSection("abstract hardware layer");
		if(program==null)
			draw(mTexProgram, mapMatrix);
		else
			draw(program,mapMatrix,GlUtil.IDENTITY_MATRIX);
		Trace.endSection();
	}


	/**
	 * Sets texture to use for textured rendering.  Has no effect on flat-shaded rendering.
	 */
	public void setTexture(int textureId)
	{
		mTextureId = textureId;
	}

	/**
	 * Returns the color.
	 * <p>
	 * To avoid allocations, this returns internal state.  The caller must not modify it.
	 */
	public float[] getColor()
	{
		return mColor;
	}

	/**
	 * Draws the rectangle with the supplied program and projection matrix.
	 */
	public void draw(FlatShadedProgram program, float[] mScratchMatrix)
	{
		// Compute model/view/projection matrix.
		program.draw(mScratchMatrix, mColor, mDrawable.getVertexArray(), 0,
					 mDrawable.getVertexCount(), mDrawable.getCoordsPerVertex(),
					 mDrawable.getVertexStride());
	}



	/**
	 * Draws the rectangle with the supplied program and projection matrix.
	 */
	public void draw(Texture2dProgram program, float[] mScratchMatrix)
	{
		program.draw(mScratchMatrix, mDrawable.getVertexArray(), 0,
					 mDrawable.getVertexCount(), mDrawable.getCoordsPerVertex(),
					 mDrawable.getVertexStride(), GlUtil.IDENTITY_MATRIX, mDrawable.getTexCoordArray(),
					 mTextureId, mDrawable.getTexCoordStride());
	}
	public void draw(CustomTexture2dProgram program, float[] mScratchMatrix, float[] texMatrix)
	{
		program.draw(mScratchMatrix, mDrawable.getVertexArray(), 0,
					 mDrawable.getVertexCount(), mDrawable.getCoordsPerVertex(),
					 mDrawable.getVertexStride(), texMatrix, mDrawable.getTexCoordArray(),
					 mTextureId, mDrawable.getTexCoordStride());
	}


}
