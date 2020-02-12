package com.tmstudios.paperboard.layers;

import android.graphics.*;
import android.opengl.*;
import android.view.*;
import com.android.grafika.gles.*;
import com.tmstudios.paperboard.*;

public class ChromaVideoLayer extends VideoLayer
{
	private final String alphaShader = "#extension GL_OES_EGL_image_external : require\n"
	+ "precision mediump float;\n"
	+ "varying vec2 vTextureCoord;\n"
	+ "uniform samplerExternalOES sTexture;\n"
	+ "varying mediump float text_alpha_out;\n"
	+ "void main() {\n"
	+ "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
	//+ "  float red = %f;\n"
	//+ "  float green = %f;\n"
	//+ "  float blue = %f;\n"
	//+ "  float accuracy = %f;\n"
	//+ "  if (abs(color.r - red) <= accuracy && abs(color.g - green) <= accuracy && abs(color.b - blue) <= accuracy) {\n"
	//+ "      gl_FragColor = vec4(color.r, color.g, color.b, 0.0);\n"
	//+ "  } else {\n"
	+ "   float alpha = color.r+color.b - color.g;\n"
	+ "      gl_FragColor = vec4(color.r, min(color.g,max(color.r,color.b)), color.b,clamp(alpha*100.0,0.0,1.0));\n"
	//+ "  }\n"
	+ "}\n";
	public ChromaVideoLayer(LayerManager m)
	{
		super(m);
	}

	@Override
	public void setup()
	{
		// TODO: Implement this method
		super.setup();
		this.mHardLayer.program=new CustomTexture2dProgram(CustomTexture2dProgram.VERTEX_SHADER,alphaShader,Texture2dProgram.ProgramType.TEXTURE_EXT);
		
	}
	
	
}

