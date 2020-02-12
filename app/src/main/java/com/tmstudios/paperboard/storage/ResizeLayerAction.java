package com.tmstudios.paperboard.storage;
import com.tmstudios.paperboard.layers.*;

public class ResizeLayerAction extends Action
{
	Layer layer;
	int oldWidth,newWidth,oldHeight,newHeight;
	public ResizeLayerAction(Layer layer,int newWidth,int newHeight,int oldWidth,int oldHeight){
		this.layer=layer;
		this.oldWidth=oldWidth;
		this.oldHeight=oldHeight;
		this.newWidth=newWidth;
		this.newHeight=newHeight;
	}
	public ResizeLayerAction(Layer layer,int newWidth,int newHeight){
		this(layer,newWidth,newHeight,layer.getHeight(),layer.getWidth());
	}
	@Override
	public void undo()
	{
		this.layer.setSize(oldWidth,oldHeight);
	}

	@Override
	public void redo()
	{
		this.layer.setSize(newWidth,newHeight);
	}
	
	
}
