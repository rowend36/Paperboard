package com.tmstudios.paperboard.storage;
import com.tmstudios.paperboard.*;

public abstract class Action
{
	protected static LayerManager mLayerManager;
	public static void setup(LayerManager v){
		mLayerManager=v;
	}
	public abstract void undo();
	public abstract void redo();
	public void execute(){
		redo();
	}
}
