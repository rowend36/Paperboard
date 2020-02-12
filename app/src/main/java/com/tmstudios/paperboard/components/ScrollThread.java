package com.tmstudios.paperboard.components;
import android.widget.*;
import android.content.*;
import com.tmstudios.paperboard.*;
import android.app.*;
import android.util.*;

public class ScrollThread extends Thread
{

	private Scroller scroll;

	private LayerManager lm;

	private int lastY;

	private int lastX;
	public ScrollThread(Scroller scroll, LayerManager lm)
	{
		this.scroll = scroll;
		this.lm = lm;
	}

	@Override
	public void run()
	{
		while (!scroll.isFinished())
		{
			scroll.computeScrollOffset();
			//Log.e("gog", scroll.getCurrX() + " " + scroll.getCurrY());
			lm.scrollBy(lastX-scroll.getCurrX(),scroll.getCurrY()-lastY);
			lastX=scroll.getCurrX();
			lastY=scroll.getCurrY();
		}
	}
}
