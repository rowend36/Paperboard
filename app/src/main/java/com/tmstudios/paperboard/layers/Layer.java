package com.tmstudios.paperboard.layers;
import com.android.grafika.gles.*;
import android.opengl.*;
import com.tmstudios.paperboard.*;
import java.util.*;
import com.tmstudios.paperboard.util.*;

public abstract class Layer
{
	public float _z=0;
	private static float lastZ=0;
	public boolean hidden;
	public boolean mCurrent;
	protected int width,height;
	//analogue of android view
	protected static Texture2dProgram mTexProgram;

	private LayerManager mLayerManager;

	public Layer(LayerManager m){
		mLayerManager=m;
		_z=lastZ;
		lastZ++;
	}

	public void onMove(float x, float y, float velX, float velY)
	{
		onMoveTo(x,y);
	}
	
	public boolean onClick(float x, float y)
	{
		return intersects(x,y);
	}

	public boolean intersects(float x, float y)
	{
		// TODO: Implement this method
		return false;
	}

	public boolean onLongPress(float x, float y)
	{
		//return true if you want to stop further events
		
		return true;
	}


	public boolean onFling(float p3, float p4)
	{
		// TODO: Implement this method
		return false;
	}
	public void onUp(float x, float y)
	{
		// TODO: Implement this method
	}

	public void onMoveTo(float x, float y)
	{
		// TODO: Implement this method
	}

	public void onDown(float x, float y)
	{
		// TODO: Implement this method
	}
	

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getWidth()
	{
		return width;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public int getHeight()
	{
		return height;
	}

	public void setSize(int oldWidth, int oldHeight)
	{
		// TODO: Implement this method
	}

	public void onResume()
	{
		// TODO: Implement this method
	}

	public void onPause()
	{
		// TODO: Implement this method
	}
	public abstract void draw(float[] ortho);
		//Log.e("layer drawn","i");
		//GLES20.glClearColor(1,1,1,1);
		//GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		
		//this.setScale(this.getScaleX()*0.98f,this.getScaleY()*0.98f);
	
	public void setup(){
		if(mTexProgram==null)
			Layer.mTexProgram = new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT);
	}
		//called from the render thread
		//the thread with glcontext
	//public Layer(){	
		//this.setPosition(100,0)
		//this.setScale(this.getScaleX()*101f,this.getScaleY()*101f);
	//}
	public void disposeGl(){
		if(mTexProgram!=null){
			Layer.mTexProgram.release();
			Layer.mTexProgram=null;
		}
	};
	public void onDisplaySizeChanged(int width,int height){
		
	}
	final LayerManager getManager(){
		return mLayerManager;
	}
	public boolean isSoftwareLayer(){
		return false;
	}
	public HashMap<String,Config> getProperties(){
		HashMap<String, Config> v = new HashMap<String,Config>();
		v.put("z-ordering",new FloatConfig().setValue(_z));
		return v;
	}
	
}
