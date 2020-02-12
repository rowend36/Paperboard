package com.tmstudios.paperboard.layers;
import com.tmstudios.paperboard.*;
import java.util.*;
import com.tmstudios.paperboard.util.*;
import android.opengl.*;

public class BackgroundLayer extends Layer
{

	private IntConfig backgroundColor;
	public BackgroundLayer(LayerManager m){
		super(m);
	}
	@Override
	public void draw(float[] ortho)
	{
		//pass
		if(mCurrent)
			GLES20.glClearColor(0.0f, 0.2f, 0.3f, 1.0f);
		else
			GLES20.glClearColor(1,1,1,1);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public boolean intersects(float x, float y)
	{
		// TODO: Implement this method
		return true;
	}
	
	@Override
	public HashMap<String, Config> getProperties()
	{
		// TODO: Implement this method
		HashMap<String, Config> properties = super.getProperties();
		properties.put("color",backgroundColor);
		return properties;
	}
	
}
