package com.tmstudios.paperboard.layers;
import android.graphics.*;
import android.text.*;
import com.tmstudios.paperboard.*;

public class TextLayer extends SoftwareLayer
{
	public Typeface typeface=Typeface.SERIF;
	private TextPaint textPaint=new TextPaint();

	private DynamicLayout mLayout;
	public TextLayer(LayerManager m,String text){
		
		super(m);
		float defaultWidth = DynamicLayout.getDesiredWidth(text,textPaint);
	    mLayout = new DynamicLayout(
			text,textPaint,(int)Math.min(defaultWidth,500),DynamicLayout.Alignment.ALIGN_NORMAL,
			1,1,false);
		textPaint.setTextSize(50);
	}
	
	float count=0;
	@Override
	public void softDraw(Canvas canvas,Matrix c)
	{
		// TODO: Implement this method
		canvas.setMatrix(c);
		canvas.drawColor(Color.BLUE);
		//Path p = new Path();
		//p.lineTo(40,40);
		//p.quadTo(60,70,80,90);
		//p.close();
		//canvas.drawPath(p,new Paint());
		textPaint.setColor(Color.BLACK);
		canvas.drawRect(5,5,245,245,textPaint);

		textPaint.setTypeface(typeface);
		canvas.save();

		textPaint.setColor(Color.CYAN);
		canvas.translate(0,mLayout.getHeight()*2);
		mLayout.draw(canvas);
		canvas.restore();
		canvas.save();

		textPaint.setColor(Color.CYAN);
		canvas.translate(450,50+mLayout.getHeight()*2);
		mLayout.draw(canvas);
		canvas.restore();
		
		count+=0.01;	
		
	}

	@Override
	public void onDisplaySizeChanged(int width, int height)
	{
		// TODO: Implement this method
		super.onDisplaySizeChanged(width, height);
		mLayout.increaseWidthTo(width);
	}
	
	
	
}
