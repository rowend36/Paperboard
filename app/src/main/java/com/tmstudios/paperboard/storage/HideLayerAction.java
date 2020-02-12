package com.tmstudios.paperboard.storage;
import com.tmstudios.paperboard.layers.*;

public class HideLayerAction extends Action
{

	private Layer layer;

	public HideLayerAction(Layer layer){
		this.layer=layer;
	}
	@Override
	public void undo()
	{
		layer.hidden=false;
	}

	@Override
	public void redo()
	{
		layer.hidden=true;
	}
	
}
