package com.tmstudios.traceviewer;

import android.app.*;
import android.os.*;
import android.view.*;
import android.graphics.drawable.*;
import android.graphics.*;
import java.io.*;
import android.widget.*;
import java.util.*;
import android.text.*;
import android.util.*;

public class TraceActivity extends Activity 
{
	
	HashMap<String,Stat> map;
	private View mSurface;
	float max=1;
	float avg;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		
		setContentView(R.layout.library); 
		mSurface=findViewById(R.id.libraryView);
		map = new HashMap<String,Stat>();
		mSurface.setBackground(new Drawable(){

				@Override
				public void draw(Canvas canvas)
				{
					Toast.makeText(TraceActivity.this,"hell",Toast.LENGTH_SHORT).show();
					int i = 1;
					int j = map.size();
					canvas.drawColor(Color.WHITE);
					//mSurface.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 2000));
					Paint p = new Paint();
					p.setColor(Color.RED);
					p.setStrokeWidth(15);
					TextPaint d = new TextPaint();
					d.setTextSize(35);
					d.setColor(Color.BLACK);

					for (HashMap.Entry<String,Stat> m:map.entrySet())
					{
						avg =  m.getValue().time/ m.getValue().count;
						canvas.drawText(m.getKey() + " : " + avg + " : " + m.getValue().count
										, 5, i * 100, d);
						if(avg/max>0.8)p.setColor(Color.RED);
						else if(avg/max>0.5)p.setColor(Color.rgb(255,100,0));
						else if(avg/max>0.2)p.setColor(Color.YELLOW);
						else p.setColor(Color.BLUE);
						canvas.drawLine(0, i * 100 + 50, (float)Math.sqrt(avg / max) * 600 + 10, i * 100 + 50, p);
						i += 1;
					}
				}

				@Override
				public void setAlpha(int p1)
				{
					// TODO: Implement this method
				}

				@Override
				public void setColorFilter(ColorFilter p1)
				{
					// TODO: Implement this method
				}

				@Override
				public int getOpacity()
				{
					// TODO: Implement this method
					return 1;
				}
				
			
		});
	    }
	public static class Stat
	{
		int count;
		float time;
		public Stat(int count, float time)
		{
			this.count = count;
			this.time = time;
		}
	}
	@Override
	protected void onResume()
	{
		// TODO: Implement this method
		super.onResume();
		map.clear();
		File trace = new File("/sdcard/trace_dumps");

		try
		{
			DataInputStream b = new DataInputStream(new FileInputStream(trace));

			String s;
			float get;
			while (b.available()>0)
			{
				try
				{
					s = b.readUTF();
					get = b.readFloat();
				}
				catch(EOFException f){
					Log.e("error", "j", f);
					Toast.makeText(this, "Error reading trace file", Toast.LENGTH_SHORT).show();
					break;
				}
				catch (IOException e)
				{
					Log.e("error", "j", e);
					Toast.makeText(this, "Error reading trace file", Toast.LENGTH_SHORT).show();
					continue;
				}
				int count;
				if (map.containsKey(s))
				{
					get = map.get(s).time + get;
					count = map.get(s).count + 1;
					try{
					if (get / ((map.get(s).time) / (map.get(s).count)) > 1000)continue;
					}catch(ArithmeticException e){}
					map.replace(s, new Stat(count, get));
					avg = (get) / count;
				}
				else
				{
					map.put(s, new Stat(1, get));
					avg = get;
				}
				if (avg > max)max = avg;
			}
		}
		catch (IOException e)
		{

			Log.e("error", "j", e);
			Toast.makeText(this, "Error reading trace file", Toast.LENGTH_SHORT).show();
		}

		mSurface.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,map.size()*100+100));
		mSurface.getParent().requestLayout();
		mSurface.invalidate();
	}

}
