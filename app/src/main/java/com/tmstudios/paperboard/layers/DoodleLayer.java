package com.tmstudios.paperboard.layers;
import android.graphics.*;
import com.tmstudios.paperboard.*;
import com.tmstudios.paperboard.util.*;
import java.util.*;

public class DoodleLayer extends SoftwareLayer
{

	private Path path;
	private boolean down;

	public String type = "default";
	public Paint paint;
	private float hardness=0.3f;
	
	public DoodleLayer(LayerManager m){
		super(m);
		this.path=new Path();
		this.paint=new Paint();
		paint.setStrokeWidth(12);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.RED);
		
	}

	@Override
	public boolean intersects(float x, float y)
	{
		// TODO: Implement this method
		return true;
	}
	
	public void addPoint(float x,float y){
		if(!down){path.moveTo(x,y);down=true;}
		else this.path.lineTo(x,y);
	}
	public void up(){
		down=false;
	}
	@Override
	public void softDraw(Canvas canvas,Matrix mat)
	{
		canvas.setMatrix(mat);
		canvas.drawCircle(60,60,60,paint);
		canvas.drawRect(240,240,760,760,paint);
		canvas.drawPath(path,paint);
	}

	@Override
	public void onUp(float x, float y)
	{
		// TODO: Implement this method
		super.onUp(x, y);
		addPoint(x,y);
		up();
	}

	@Override
	public void onDown(float x, float y)
	{
		// TODO: Implement this method
		super.onDown(x, y);
		MainActivity h = (MainActivity) this.getManager().getContext();
		down=false;
		addPoint(x,y);
	}

	@Override
	public void onMoveTo(float x, float y)
	{
		// TODO: Implement this method
		super.onMoveTo(x, y);
		addPoint(x,y);
	}

	@Override
	public HashMap<String, Config> getProperties()
	{
		// TODO: Implement this method
		HashMap<String,Config> b =  super.getProperties();
		b.put("brushColor",new IntConfig().setValue(paint.getColor()));
		b.put("brushSize",new FloatConfig().setValue(paint.getStrokeWidth()));
		b.put("brushStyle",new StringConfig().setValue(type));
		b.put("brushHardness",new FloatConfig().setValue(hardness));
		return b;
	}
	
	
	
}
