package com.tmstudios.paperboard.layers;
import com.tmstudios.paperboard.*;
import android.graphics.*;

public class ImageLayer extends SoftwareLayer
{

	private Bitmap mBitmap;

	@Override
	public void softDraw(Canvas canvas, Matrix root)
	{
		canvas.setMatrix(root);
		canvas.drawBitmap(mBitmap,0,0,new Paint());
	}
	
	public ImageLayer(LayerManager lm,Bitmap b){
		super(lm);
		this.mBitmap = b;
	}
}
