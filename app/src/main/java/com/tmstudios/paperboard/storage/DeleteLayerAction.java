package com.tmstudios.paperboard.storage;
import com.tmstudios.paperboard.layers.*;

public class DeleteLayerAction extends Action
{
	Layer layer;
	public DeleteLayerAction(Layer layer){
		this.layer=layer;
	}
	@Override
	public void undo()
	{
		mLayerManager.add(layer);
	}

	@Override
	public void redo()
	{
		mLayerManager.remove(layer);
	}

	
}
