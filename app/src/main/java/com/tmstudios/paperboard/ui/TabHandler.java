package com.tmstudios.paperboard.ui;
import android.view.*;
import android.app.*;

public class TabHandler implements View.OnClickListener
{
	View pane;
	View selector;
	TabHandler current;
	private TabHandler()
	{

	}
	public TabHandler(View selector, View pane, TabHandler holder)
	{
		this.pane = pane;
		this.selector=selector;
		this.selector.setOnClickListener(this);
		this.current = holder;
		if(holder.pane!=pane){
			this.pane.setVisibility(View.GONE);
			selector.setSelected(false);
			}
	}
	public static TabHandler createHolder(View selector, View pane)
	{
		TabHandler b = new TabHandler();
		b.pane = pane;
		b.selector = selector;
		return b;
	}
	public static TabHandler createHolder(View dialog, int selector, int pane)
	{
		return createHolder(dialog.findViewById(selector), dialog.findViewById(pane));
	}
	public TabHandler(View dialog, int selector, int pane, TabHandler holder)
	{
		this(dialog.findViewById(selector),dialog.findViewById(pane),holder);
	}
	@Override
	public void onClick(View p1)
	{
		current.selector.setSelected(false);
		current.pane.setVisibility(View.GONE);
		current.pane = pane;
		current.selector = selector;
		selector.setSelected(true);
		pane.setVisibility(View.VISIBLE);
	}

	}
