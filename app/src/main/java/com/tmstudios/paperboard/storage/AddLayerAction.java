package com.tmstudios.paperboard.storage;
import com.tmstudios.paperboard.layers.*;

public class AddLayerAction extends Action
{
	Layer layer;
	public AddLayerAction(Layer layer){
		this.layer=layer;
	}
	@Override
	public void undo()
	{
		mLayerManager.remove(layer);
	}

	@Override
	public void redo()
	{
		mLayerManager.add(layer);
	}

}
