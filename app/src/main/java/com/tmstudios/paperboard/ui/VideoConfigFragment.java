package com.tmstudios.paperboard.ui;
import android.app.*;
import com.tmstudios.paperboard.layers.*;
import android.view.*;
import android.os.*;
import com.tmstudios.paperboard.*;

public class VideoConfigFragment extends ConfigFragment<VideoLayer>
{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// TODO: Implement this method
		return inflater.inflate(R.layout.video_config,container,false);//super.onCreateView(inflater, container, savedInstanceState);
	}
	
	


	public void updateProperties()
	{
		if(mLayer==null||getView()==null){
			return;
		}
	}
}
