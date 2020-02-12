package com.tmstudios.paperboard.ui;
import android.app.*;
import android.view.*;
import android.os.*;
import com.tmstudios.paperboard.R;
import com.tmstudios.paperboard.layers.*;
import android.util.*;
import android.widget.*;
import android.graphics.drawable.*;
import android.graphics.*;
import android.support.v4.content.*;
import android.graphics.drawable.shapes.*;
import com.tmstudios.paperboard.*;
import com.rarepebble.colorpicker.*;

public class DoodleConfigFragment extends ConfigFragment<DoodleLayer> implements ColorObserver
{

	@Override
	public void updateColor(ObservableColor observableColor)
	{
		mLayer.paint.setColor( observableColor.getColor());
	}
	boolean viewCreated;
	boolean propertiesUpdate;
	Drawable colorDrawable;
	private static final String TAG="DoodleConfigFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// TODO: Implement this method
		return inflater.inflate(R.layout.doodle_config,container,false);//super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void updateProperties()
	{
		propertiesUpdate=true;
		final Paint paint = new Paint();
		View configStrokeColor = getView().findViewById(R.id.doodle_configStrokeColor);
		colorDrawable = new Drawable(){

			@Override
			public void draw(Canvas p1)
			{
				paint.setColor(mLayer.paint.getColor());
		 	 	p1.drawCircle(p1.getWidth()/2.0f,p1.getHeight()/2.0f,Math.min(p1.getHeight(),p1.getWidth())/2.1f,paint);
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
				return 0;
			}
			
			
		};
		configStrokeColor.setBackgroundDrawable(colorDrawable);
		configStrokeColor.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					((MainActivity)
						getActivity()).openColorDialog(DoodleConfigFragment.this,mLayer.paint.getColor());
				}
				
			
		});
		((TextView)getView().findViewById(R.id.doodle_configPencilType)).setText(mLayer.type);

		((TextView)getView().findViewById(R.id.doodle_configStrokeWidth)).setText(String.format("%.1f",mLayer.paint.getStrokeWidth()));
		getView().findViewById(R.id.doodle_configStrokePreview).setBackground(new Drawable(){

				@Override
				public void draw(Canvas p1)
				{
					Paint paint = new Paint();
					paint.setColor(mLayer.paint.getColor());
					paint.setAlpha(mLayer.paint.getAlpha());
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setMaskFilter(mLayer.paint.getMaskFilter());
					p1.drawCircle(p1.getWidth()/2.0f,p1.getHeight()/2.0f,Math.min(p1.getHeight(),p1.getWidth())/2.1f,paint);
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
	};
	
}
