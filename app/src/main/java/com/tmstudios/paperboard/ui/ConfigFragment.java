package com.tmstudios.paperboard.ui;
import com.tmstudios.paperboard.layers.*;
import android.view.*;
import android.os.*;
import com.tmstudios.paperboard.*;
import android.util.*;
import android.support.v4.app.*;

public class ConfigFragment<T extends Layer> extends Fragment
{

	protected T mLayer;

	private boolean viewCreated;

	private String TAG = "ConfigFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// TODO: Implement this method
		return inflater.inflate(R.layout.video_config,container,false);//super.onCreateView(inflater, container, savedInstanceState);
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onViewCreated(view, savedInstanceState);
		viewCreated=true;
		Log.e(TAG,"view created");
		tryUpdateProperties();
	}


	public void setLayer(T currentLayer)
	{
		this.mLayer=currentLayer;
		tryUpdateProperties();
	}

	public final void tryUpdateProperties()
	{
		if(mLayer==null||getView()==null){
			return;
		}
		updateProperties();
	}
	protected void updateProperties(){
		
	}
}
