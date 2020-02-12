package com.tmstudios.paperboard;
import java.util.*;
import android.view.*;
import com.tmstudios.paperboard.layers.*;
import com.tmstudios.paperboard.resources.*;
import android.content.*;
import android.opengl.GLES20;
import android.util.*;
import com.tmstudios.paperboard.util.*;
import com.tmstudios.paperboard.components.*;
import android.opengl.Matrix;
import com.android.grafika.gles.*;
import android.media.*;

public class LayerManager
{
	public static boolean logging;
	public int displayW,displayH;
	public float displayRatio;
	public Layer currentLayer;
	

	private MainActivity context;
	
	//view matrix for software
	public android.graphics.Matrix sMatrix=new android.graphics.Matrix();
	
	private Comparator<Layer> comparator;

	private Object mDrawFence = new Object();      // guards layers from concurrent modification
	//projection matrixi
	public float[] uiMat;

	private int centerX;

	private int centerY;

	public int displayMin;

	private Layer backGround;

	public float displayScale=1;

	private float lastX;

	private float lastY;

	private boolean mDrawing;

	public void rotateBy(int velocity)
	{
		sMatrix.postRotate(velocity,centerX,centerY);
	}


	public boolean onDoubleTap(float x, float y)
	{
		// TODO: Implement this method
		return false;
	}

	


	public void rotateBy(float velX, float velY, float x, float y)
	{
		Vector2 start = new Vector2(x-velX,y-velY);
		//Vector2 end = new Vector2(x,y);
		Vector2 slope = new Vector2(velX,velY);
		Vector2 centre = new Vector2(centerX,centerY);
		Vector2 normal = start.minus(centre);
		float velocity = slope.cross(normal.norm()) ;// displayMin;
		Log.e("velocity",""+velocity);
		//LogOnce.lo(944994,"velocity", new Float(velocity).toString(),100,10);
		sMatrix.postRotate(velocity,centerX,centerY);
	}
	
	public void scale(float scaleFactor, float scaleFactorY, float focusX, float focusY)
	{
		sMatrix.postScale(scaleFactor,scaleFactorY,focusX,focusY);
	}
	
	private String stringify(float[] hMatrix)
	{
		// TODO: Implement this method
		String bee="";
		for (int i =0;i < 4;i++)
		{
		 bee += hMatrix[i] + ", " + hMatrix[i + 4] + ", " + hMatrix[i + 8] + ", " +hMatrix[i + 12] + ", ";
			
		}
		return bee;
	}

	//public float scale=1.0f;

	public boolean fling(float p3, float p4)
	{
		return currentLayer.onFling(p3, p4);
	}

	public void onDoubleClick(float x, float y)
	{
		ListIterator<Layer> iterator;
		
		if(currentLayer==backGround)
			iterator=layers.listIterator(layers.size());
		else
			iterator=layers.listIterator(layers.indexOf(currentLayer));
		while(iterator.hasPrevious())
		{
			Layer layer = iterator.previous();
			if(layer.intersects(x,y))
			{
				currentLayer.mCurrent=false;
				currentLayer=layer;
				currentLayer.mCurrent=true;
				break;
			}
		}
		return;
	}
	public void scrollBy(float p3, float p4)
	{//LogOnce.lo(1, "Lm scrollling", p3 + "," + p4 + " ", 1000, 10);
		sMatrix.postTranslate(- p3, - p4);
	}

	public boolean onClick(float x, float y)
	{
		// TODO: Implement this method

		LogOnce.lo(35, "click called", "done", 3, 1);
		if(currentLayer.onClick(x,y))return true;
		ListIterator<Layer> iterator;
		if(currentLayer==backGround)
			iterator=layers.listIterator(layers.size());
		else
			iterator=layers.listIterator(layers.indexOf(currentLayer));
		while(iterator.hasPrevious())
		{
			Layer layer = iterator.previous();
			if(layer.intersects(x,y))
			{
				setCurrent(layer);
				break;
			}
		}
		return true;
	}

	private void setCurrent(Layer layer)
	{
		if(currentLayer!=null)
			currentLayer.mCurrent = false;
		currentLayer = layer;
		currentLayer.mCurrent = true;
		context.onCurrentLayerChanged(layer);
	}
	
	public void onUp(float x, float y)
	{

		LogOnce.lo(18, "up called", "done", 3, 1);
		currentLayer.onUp(x, y);
	}

	public void onMove(float x, float y)
	{

		LogOnce.lo(24, "move called", "done", 3, 1);
		currentLayer.onMove(x,y,x-lastX, y-lastY);
		lastX=x;
		lastY=y;
	}

	public void onDown(float x, float y)
	{
		lastX=x;
		lastY=y;
		currentLayer.onDown(x, y);
	}
	
	public boolean onLongPress(float x, float y)
	{
		return currentLayer.onLongPress(x,y);
	}
	
	public void add(Layer layer)
	{
		synchronized(mDrawFence){
			while(mDrawing){
				
			}
		layers.add(layer);
		}
		setCurrent(layer);
	}

	public void remove(Layer layer)
	{
		int v = layers.indexOf(layer);
		layers.remove(layer);
		if (v > 0)
			setCurrent(layers.get(v - 1));
		else if (layers.size() > 0)
			setCurrent(layers.get(0));
		else currentLayer = null;
	}

	//Ideally, LayerManager goes into the RenderManager
	//But RenderManager also doubles as a RecordManager
	//So I delegate drawing and audio etc which are needed
	//by rendermanager to the layermanager and audiomanager
	//respectively

	Stack<SoftwareLayer> v = new Stack<SoftwareLayer>();
	public void draw()
	{
		Trace.beginSection("draw");

		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		synchronized (mDrawFence) {	
			mDrawing = true;
		}
		for (Layer i:layers)
		{
			if (i.isSoftwareLayer())
			{
				v.push((SoftwareLayer)i);
			}
			else if (v.size() > 0)
			{
				Trace.beginSection("draw  Batch");
				SoftwareLayer.drawBatch(v, uiMat, sMatrix);
				v.clear();
				i.draw(uiMat);
				Trace.endSection();
			}
			else i.draw(uiMat);
		}

		mDrawing=false;//no longer trasversing the list
		if (v.size() > 0)
		{
			SoftwareLayer.drawBatch(v, uiMat, sMatrix);
			v.clear();
		}

		GLES20.glDisable(GLES20.GL_BLEND);
		Trace.endSection();
	}

	
	static int count=0;
	public static void logarray4(String tag, float[] ortho)
	{
		count ++;
		if (count > 60)count = 0;
		if (count > 30)return;
		for (int i =0;i < 4;i++)
		{
			String bee = ortho[i] + ", " + ortho[i + 4] + ", " + ortho[i + 8] + ", " + ortho[i + 12] + ", ";
			Log.e(tag, bee);
		}
	}
	public static void logarray3(String tag, float[] ortho)
	{
		count ++;
		if (count > 50)count = 0;
		if (count > 5)return;
		for (int i =0;i < 9;i += 3)
		{
			String bee = ortho[i] + ", " + ortho[i + 1] + ", " + ortho[i + 2]  + ", ";
			Log.e(tag, bee);
		}
	}

	public void swap(Layer layer1, Layer layer2)
	{
		float k=layer1._z;
		layer1._z = layer2._z;
		layer2._z = k;
		layers.sort(comparator);
	}
	public void disposeGl()
	{
		for (Layer i:layers)
		{
			i.disposeGl();
		}
	}
	public void dispatchDisplayChange(int width, int height)
	{
		displayRatio = ((float)width) / height;
		displayH = height;
		displayW = width;
		displayMin = Math.min(displayW, displayH);
		
		this.uiMat = new float[16];
		Matrix.setIdentityM(uiMat,0);
		if(displayRatio<1){
			uiMat[5]*=displayRatio;
			uiMat[13]=(1-displayRatio);
		}
		else{
			uiMat[0]/=displayRatio;
			uiMat[12]=(1/displayRatio-1);
		}

		centerX=displayW/2;centerY=displayH/2;
		//Matrix.setIdentityM(hMatrix,0);
		//sMatrix.reset();
		//sMatrix.preScale(scale,displayScale/scale);
		//Log.e("scale",""+scale);
		//Log.e("displayScale",""+displayScale);
		//displayScale=scale;
		//Matrix.orthoM(mat,0,0,1,0,1/displayRatio,-1,1);
		for (Layer i:layers)
		{
			i.onDisplaySizeChanged(width, height);
		}
		//logarray4("matrix",mat);
		//logarray4("matrix",uiMat);
	}
	public void dispatchPause()
	{
		for (Layer i:layers)
		{
			i.onPause();
		}
	}
	public void dispatchResume()
	{
		for (Layer i:layers)
		{
			i.onResume();
		}
	}
	ArrayList<Layer> layers;
	public LayerManager(MainActivity context)
	{
		this.context = context;
		layers = new ArrayList<Layer>();
		comparator = new Comparator<Layer>(){

			@Override
			public int compare(Layer p1, Layer p2)
			{
				// TODO: Implement this method
				if (p1._z > p2._z)return 1;
				else if (p1._z < p2._z)return -1;
				else return 0;
			}
		};
		centerX=displayW/2;centerY=displayH/2;
		SoftwareLayer.reset();
		backGround= new BackgroundLayer(this);
		add(backGround);
		add(new DoodleLayer(this));
		/*Trace.beginSection("createVideo");
		add(new VideoLayer(this));
		//AbstractHardwareLayer b =((TextureLayer)currentLayer).mHardLayer;
		//b.setPosition(0,0);
		VideoResource videoResource = new VideoResource(context);
		videoResource.setOnErrorListener(new MediaPlayer.OnErrorListener(){

				@Override
				public boolean onError(MediaPlayer p1, int p2, int p3)
				{
					// TODO: Implement this method
					Log.e("error"," "+p1+" "+p2);
					return false;
				}
			});
		videoResource.setVideoFromAssets("blap.mp4");
		((VideoLayer)currentLayer).setVideoResource(videoResource);
		Trace.endSection();
*/
		Trace.beginSection("Chromavideo");
		add(new ChromaVideoLayer(this));
		//AbstractHardwareLayer b =((TextureLayer)currentLayer).mHardLayer;
		//b.setPosition(0,0);
	 	VideoResource videoResource = new VideoResource(context);
		videoResource.setOnErrorListener(new MediaPlayer.OnErrorListener(){

				@Override
				public boolean onError(MediaPlayer p1, int p2, int p3)
				{
					// TODO: Implement this method
					Log.e("error"," "+p1+" "+p2);
					return false;
				}
			});
		videoResource.setVideoFromAssets("ball.mp4");
		((VideoLayer)currentLayer).setVideoResource(videoResource);	
		Trace.endSection();
		layers.sort(comparator);
	}
	public Context getContext()
	{
		return context;
	}

}
