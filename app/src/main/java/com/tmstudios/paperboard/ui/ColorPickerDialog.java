package com.tmstudios.paperboard.ui;
import android.support.v4.app.*;
import android.view.*;
import android.os.*;
//import android.support.v7.appcompat.;
import android.content.*;
import com.tmstudios.paperboard.*;
import android.widget.*;
import android.graphics.*;
import com.rarepebble.colorpicker.ColorPickerView;
import com.rarepebble.colorpicker.ColorObserver;
import com.rarepebble.colorpicker.ObservableColor;
import android.graphics.drawable.*;
import android.support.v7.app.*;
import android.util.*;
import android.graphics.drawable.shapes.*;

public class ColorPickerDialog extends AlertDialog implements 
ColorObserver,AlertDialog.OnDismissListener,
AdapterView.OnItemClickListener
{

	@Override
	public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
	{

		//int m =1/0;
		int itemAtPosition = p1.getItemAtPosition(p3);
		mColorPicker.setCurrentColor(itemAtPosition);
		//Log.e("item selected","6");
	}
	

	

	private ColorPickerView mColorPicker;

	private GridView recentsGrid;

	
	public ColorObserver mObserver;

	public int mColor;

	public ColorPickerDialog(Context ctx){
		super(ctx);
	}
	@Override
	public void updateColor(ObservableColor observableColor)
	{
		if(mObserver!=null)
			mObserver.updateColor(observableColor);
		//int val =  (int) (255*observableColor.getLightness());
		Log.e("update_color_called","ui");
		
		//recentsGrid.getSelector().setTint(Color.rgb(val,val,val));
		//recentsGrid.getSelector().setTintMode(PorterDuff.Mode.MULTIPLY);
	}
	

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		FrameLayout dialog = new FrameLayout(getContext());
		setContentView(dialog);
		getLayoutInflater().inflate(R.layout.color_picker,dialog,true);
		TabHandler holder = TabHandler.createHolder(dialog, R.id.color_pickerGridTitle, R.id.color_picker_grid);
		new TabHandler(dialog, R.id.color_pickerTitle, R.id.color_picker_widget, holder);
		new TabHandler(dialog, R.id.color_pickerRGBTitle, R.id.color_picker_rgb, holder);
		//new TabHandler(dialog, R.id.color_pickerHSLTitle, R.id.color_picker_hsl, holder);
		new TabHandler(dialog, R.id.color_pickerGridTitle, R.id.color_picker_grid, holder);
		recentsGrid = dialog.findViewById(R.id.color_gridRecent);
		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(getContext(), 0){
			@Override
			public View getView(int position, View convertView , ViewGroup parent)
			{
				if (convertView == null)convertView = new View(getContext());
				Drawable b = getContext().getResources().getDrawable(R.drawable.curved_rect);
				b.setTint(getItem(position));
				convertView.setBackground(b);
				int pixels = getContext().getResources().getDimensionPixelSize(R.dimen.color_grid_size);
				convertView.setLayoutParams(new GridView.LayoutParams(pixels,pixels));
				return convertView;
			}
		};
		adapter.addAll(new Integer[]{Color.RED,Color.BLACK,Color.BLUE,Color.YELLOW,Color.RED,Color.BLACK,Color.BLUE,Color.YELLOW,Color.RED,Color.BLACK,Color.BLUE,Color.YELLOW});
		recentsGrid.setAdapter(adapter);
		//recentsGrid.setOnItemSelectedListener(this);
		recentsGrid.setOnItemClickListener(this);
		recentsGrid.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
		mColorPicker = dialog.findViewById(R.id.colorPicker);
		//Log.e("colorpicker"," "+b);
		mColorPicker.addColorObserver(this);
		mColorPicker.setColor(mColor);
		setOnDismissListener(this);
	}
    @Override
	public void onDismiss(DialogInterface p1)
	{
		removeObserver();
	}


	private void removeObserver()
	{
		mObserver=null;
	}
	
}
