package com.tmstudios.paperboard.layers;
import com.tmstudios.paperboard.*;
import java.util.*;

public class StaticBackupLayer extends Layer
{
	Vector<SoftwareLayer> layers;
	public StaticBackupLayer(LayerManager m){
		super(m);
		layers=new Vector<SoftwareLayer>();
	}
	public void addLayer(SoftwareLayer layer){
		layers.add(layer);
	}
	@Override
	public void draw(float[] ortho)
	{
		// TODO: Implement this method
	}
	
}
