package com.tmstudios.paperboard.components;
import android.view.*;
import android.content.*;
import com.tmstudios.paperboard.*;
import com.tmstudios.paperboard.util.*;
import android.widget.*;
import android.util.*;
import android.graphics.*;

public class Controller extends GestureDetector.SimpleOnGestureListener
implements ScaleGestureDetector.OnScaleGestureListener
{

	public boolean enableEvents;

	public void onUp(MotionEvent p2)
	{
		enableEvents=false;
		switch(touchMode){
			case SELECT:
				transformMotion(p2);
				ctx.getLayerManager().onUp(p2.getX(), p2.getY());
		}
		
	}

	private enum Mode
	{SELECT,ROTATE,SCROLL};
	Mode touchMode;

	public void enterRotateMode()
	{
		touchMode = Mode.ROTATE;
	}

	public void enterSelectMode()
	{
		touchMode = Mode.SELECT;
	}

	public void enterScrollMode()
	{
		touchMode = Mode.SCROLL;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e)
	{
		return true;
	}


	@Override
	public boolean onScale(ScaleGestureDetector p1)
	{
		switch (touchMode)
		{
			case SCROLL:
			case ROTATE:
				ctx.getLayerManager().scale(p1.getScaleFactor(), p1.getScaleFactor(), p1.getFocusX(), p1.getFocusY());
		}
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector p1)
	{
		return true;

	}

	@Override
	public void onScaleEnd(ScaleGestureDetector p1)
	{}


	private MainActivity ctx;
	private ScrollThread scrollThread;

	@Override
	public boolean onDown(MotionEvent p1)
	{
		if (scrollThread != null && scrollThread.isAlive())
			flingHandler.forceFinished(true);
		transformMotion(p1);
		ctx.getLayerManager().onDown(p1.getX(), p1.getY());
		return true;
	}


	@Override
	public boolean onSingleTapConfirmed(MotionEvent p1)
	{
		switch(touchMode){
			case SELECT:
				transformMotion(p1);
				return ctx.getLayerManager().onClick(p1.getX(), p1.getY());
		}
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent p1, MotionEvent p2, float p3, float p4)
	{
		switch (touchMode)
		{
			case SCROLL:
				ctx.getLayerManager().scrollBy(p3, p4);
				break;
			case SELECT:
				transformMotion(p2);
				ctx.getLayerManager().onMove(p2.getX(), p2.getY());
				break;
			case ROTATE:
				ctx.getLayerManager().rotateBy(p3,p4,p2.getX(),p2.getY());
		}
		return true;
	}

	private void transformMotion(MotionEvent p2)
	{
		Matrix m = new Matrix();
		ctx.getLayerManager().sMatrix.invert(m);
		p2.transform(m);
	}

	@Override
	public boolean onDoubleTap(MotionEvent e)
	{
		switch (touchMode)
		{
			case SELECT:
				transformMotion(e);
				ctx.getLayerManager().onDoubleClick(e.getX(), e.getY());
		}
		return true;
	}
	Scroller flingHandler;

	@Override
	public boolean onFling(MotionEvent p1, MotionEvent p2, float p3, float p4)
	{

		switch (touchMode)
		{
			case SELECT:
				transformMotion(p1);
				return ctx.getLayerManager().fling(p3, p4);

			case SCROLL:
				
//			try
//			{
//				if (flingHandler != null)flingHandler.forceFinished(true);
//				if (scrollThread != null)scrollThread.join();
//				scrollThread = null;
//			}
//			catch (Exception e)
//			{
//				Log.e("Error", "err", e);
//			}
				if (scrollThread == null || !scrollThread.isAlive() || flingHandler.isFinished())
				{

					flingHandler = new Scroller(ctx);
					flingHandler.fling(0, 0,
									   (int)(p3), (int)(-p4), -300000, 300000, -300000, 300000);
					scrollThread = new ScrollThread(flingHandler, ctx.getLayerManager());
					flingHandler.setFriction(0.1f);
					scrollThread.start();
				}
				else
				{
					flingHandler.fling(0, 0,
									   (int)(p3), (int)(-p4), -300000, 300000, -300000, 300000);	
				}
				return true;
		}
		return true;
	}
	@Override
	public void onShowPress(MotionEvent p1)
	{
		// TODO: Implement this method
	}
	@Override
	public void onLongPress(MotionEvent p1)
	{

		switch (touchMode)
		{
			case SELECT:
				transformMotion(p1);
				enableEvents = !ctx.getLayerManager().onLongPress(p1.getX(), p1.getY());
		}
		
		return;
	}
	public void onLongPressScroll(MotionEvent p1,float p3,float p4){

		switch (touchMode)
		{
			case SCROLL:
				ctx.getLayerManager().scrollBy(p3, p4);
				break;
			case SELECT:
				transformMotion(p1);
				ctx.getLayerManager().onMove(p1.getX(), p1.getY());
				break;
			case ROTATE:
				ctx.getLayerManager().rotateBy(p3,p4,p1.getX(),p1.getY());
		}
		
	}
	public Controller(MainActivity ctx)
	{
		this.ctx = ctx;
	}
}
