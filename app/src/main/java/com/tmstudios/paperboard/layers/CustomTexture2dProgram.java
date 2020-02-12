package com.tmstudios.paperboard.layers;

import android.opengl.*;
import android.util.*;
import com.android.grafika.gles.*;
import java.nio.*;

public class CustomTexture2dProgram
{

	/**
	 * GL program and supporting functions for textured 2D shapes.
	 */
	private static final String TAG = GlUtil.TAG;
	
	// Simple vertex shader, used for all programs.
	public static final String VERTEX_SHADER =
	"uniform mat4 uMVPMatrix;\n" +
	"uniform mat4 uTexMatrix;\n" +
	"attribute vec4 aPosition;\n" +
	"attribute vec4 aTextureCoord;\n" +
	"varying vec2 vTextureCoord;\n" +
	"void main() {\n" +
	"    gl_Position = uMVPMatrix * aPosition;\n" +
	"    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
	"}\n";

	// Simple fragment shader for use with "normal" 2D textures.
	public static final String FRAGMENT_SHADER_2D =
	"precision mediump float;\n" +
	"varying vec2 vTextureCoord;\n" +
	"uniform sampler2D sTexture;\n" +
	"void main() {\n" +
	"    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
	"}\n";

	public static final String FRAGMENT_SHADER_EXT =
	"#extension GL_OES_EGL_image_external : require\n" +
	"precision mediump float;\n" +
	"varying vec2 vTextureCoord;\n" +
	"uniform samplerExternalOES sTexture;\n" +
	"void main() {\n" +
	"    gl_FragColor = vec4(texture2D(sTexture, vTextureCoord).r);\n" +
	"}\n";
	// Handles to the GL program and various components of it.
	private int mProgramHandle;
	private int muMVPMatrixLoc;
	private int muTexMatrixLoc;

	private int maPositionLoc;
	private int maTextureCoordLoc;

	private int mTextureTarget;

	private float[] mTexOffset;


	/**
	 * Prepares the program in the current EGL context.
	 */
	public CustomTexture2dProgram(String vShader,String fShader,Texture2dProgram.ProgramType type)
	{
		if(type==Texture2dProgram.ProgramType.TEXTURE_2D)
				mTextureTarget = GLES20.GL_TEXTURE_2D;
		else 
				mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
				
				mProgramHandle = GlUtil.createProgram(vShader, fShader);
			
		if (mProgramHandle == 0)
		{
			throw new RuntimeException("Unable to create program");
		}
		Log.d(TAG, "Created program " + mProgramHandle + " (" + type + ")");

		// get locations of attributes and uniforms

		maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
		GlUtil.checkLocation(maPositionLoc, "aPosition");
		maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
		GlUtil.checkLocation(maTextureCoordLoc, "aTextureCoord");
		muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
		GlUtil.checkLocation(muMVPMatrixLoc, "uMVPMatrix");
		muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
		GlUtil.checkLocation(muTexMatrixLoc, "uTexMatrix");
		
	}

	/**
	 * Releases the program.
	 * <p>
	 * The appropriate EGL context must be current (i.e. the one that was used to create
	 * the program).
	 */
	public void release()
	{
		Log.d(TAG, "deleting program " + mProgramHandle);
		GLES20.glDeleteProgram(mProgramHandle);
		mProgramHandle = -1;
	}


	/**
	 * Creates a texture object suitable for use with this program.
	 * <p>
	 * On exit, the texture will be bound.
	 */
	public int createTextureObject()
	{
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		GlUtil.checkGlError("glGenTextures");

		int texId = textures[0];
		GLES20.glBindTexture(mTextureTarget, texId);
		GlUtil.checkGlError("glBindTexture " + texId);

		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
							   GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
							   GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
							   GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
							   GLES20.GL_CLAMP_TO_EDGE);
		GlUtil.checkGlError("glTexParameter");

		return texId;
	}

	/**
	 * Configures the convolution filter values.
	 *
	 * @param values Normalized filter values; must be KERNEL_SIZE elements.
	 */
	/**
	 * Sets the size of the texture.  This is used to find adjacent texels when filtering.
	 */
	public void setTexSize(int width, int height)
	{
		float rw = 1.0f / width;
		float rh = 1.0f / height;

		// Don't need to create a new array here, but it's syntactically convenient.
		mTexOffset = new float[] {
			-rw, -rh,   0f, -rh,    rw, -rh,
			-rw, 0f,    0f, 0f,     rw, 0f,
			-rw, rh,    0f, rh,     rw, rh
		};
		//Log.d(TAG, "filt size: " + width + "x" + height + ": " + Arrays.toString(mTexOffset));
	}

	/**
	 * Issues the draw call.  Does the full setup on every call.
	 *
	 * @param mvpMatrix The 4x4 projection matrix.
	 * @param vertexBuffer Buffer with vertex position data.
	 * @param firstVertex Index of first vertex to use in vertexBuffer.
	 * @param vertexCount Number of vertices in vertexBuffer.
	 * @param coordsPerVertex The number of coordinates per vertex (e.g. x,y is 2).
	 * @param vertexStride Width, in bytes, of the position data for each vertex (often
	 *        vertexCount * sizeof(float)).
	 * @param texMatrix A 4x4 transformation matrix for texture coords.  (Primarily intended
	 *        for use with SurfaceTexture.)
	 * @param texBuffer Buffer with vertex texture data.
	 * @param texStride Width, in bytes, of the texture data for each vertex.
	 */
	public void draw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
					 int vertexCount, int coordsPerVertex, int vertexStride,
					 float[] texMatrix, FloatBuffer texBuffer, int textureId, int texStride)
	{
		GlUtil.checkGlError("draw start");

		// Select the program.
		GLES20.glUseProgram(mProgramHandle);
		GlUtil.checkGlError("glUseProgram");

		// Set the texture.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(mTextureTarget, textureId);

		// Copy the model / view / projection matrix over.
		GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0);
		GlUtil.checkGlError("glUniformMatrix4fv");

		// Copy the texture transformation matrix over.
		GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
		GlUtil.checkGlError("glUniformMatrix4fv");

		// Enable the "aPosition" vertex attribute.
		GLES20.glEnableVertexAttribArray(maPositionLoc);
		GlUtil.checkGlError("glEnableVertexAttribArray");

		// Connect vertexBuffer to "aPosition".
		GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex,
									 GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
		GlUtil.checkGlError("glVertexAttribPointer");

		// Enable the "aTextureCoord" vertex attribute.
		GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
		GlUtil.checkGlError("glEnableVertexAttribArray");

		// Connect texBuffer to "aTextureCoord".
		GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
									 GLES20.GL_FLOAT, false, texStride, texBuffer);
		GlUtil.checkGlError("glVertexAttribPointer");

		// Draw the rect.
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);
		GlUtil.checkGlError("glDrawArrays");

		// Done -- disable vertex array, texture, and program.
		GLES20.glDisableVertexAttribArray(maPositionLoc);
		GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
		GLES20.glBindTexture(mTextureTarget, 0);
		GLES20.glUseProgram(0);
	}
	
}
