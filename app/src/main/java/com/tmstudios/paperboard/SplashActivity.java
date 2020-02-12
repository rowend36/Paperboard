package com.tmstudios.paperboard;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.content.*;

public class SplashActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		TextView v = new TextView(this);
		v.setText("Welcome to PaperBoard");
		setContentView(v);
		v.postDelayed(new Runnable(){

				@Override
				public void run()
				{
					finish();
				}
				
			
		},1000);
	}
	
};
