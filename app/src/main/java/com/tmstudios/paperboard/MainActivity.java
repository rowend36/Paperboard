package com.tmstudios.paperboard;

//import android.app.*;
import android.os.*;
import android.view.*;
import java.io.*;
import com.tmstudios.paperboard.components.*;
import android.widget.*;
import android.content.*;
import com.tmstudios.paperboard.layers.*;
import com.tmstudios.paperboard.ui.*;
import android.support.v7.app.*;
import android.content.res.*;
import android.util.Log;
import android.support.v4.content.pm.*;
import android.content.pm.*;
import android.app.Dialog;
import android.graphics.*;
import com.rarepebble.colorpicker.*;
import android.support.v4.app.*;

public class MainActivity extends AppCompatActivity
{


	private ColorPickerDialog colorFragment;

	public void onCurrentLayerChanged(Layer layer)
	{
		Class layerType = layer.getClass();
		if (layerType == (DoodleLayer.class))
		{
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.config, doodleFragment);
			ft.commit();
			doodleFragment.setLayer((DoodleLayer)layer);
		}
		else if(layerType == (VideoLayer.class)){
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.config, videoFragment);
			ft.commit();
			videoFragment.setLayer((VideoLayer)layer);
		}
		else if(layerType == (ImageLayer.class)){
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.config, imageFragment);
			ft.commit();
			imageFragment.setLayer((ImageLayer)layer);
		}
	}
	public void openColorDialog(ColorObserver p1,int color)
	{
		colorFragment.mObserver = p1;
		colorFragment.mColor = color;
		colorFragment.show();
	}

	public static String TAG;
	public ImageButton pauseBtn;


	private LayerManager lm;

	private StateManager sm;

	private String outputFileName="/sdcard/ty.mp4";

	private static final int DIALOG_PICK_LAYER = 99;
	private SurfaceFagment mSurfaceFragment;
	private DoodleConfigFragment doodleFragment;
	private VideoConfigFragment videoFragment;
	private ImageConfigFragment imageFragment;

	private static final int RESULT_PICK_IMAGE = 33;
	public static final int RESULT_PICK_VIDEO = 34;
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		Trace.beginSection("create");
		if (mSurfaceFragment == null)
			mSurfaceFragment = new SurfaceFagment();
		if (doodleFragment == null)
			doodleFragment = new DoodleConfigFragment();
		if( videoFragment == null)
			videoFragment=new VideoConfigFragment();
		if(imageFragment == null)
			imageFragment = new ImageConfigFragment();
		if(colorFragment == null)
			colorFragment = new ColorPickerDialog(this);
        setContentView(R.layout.main);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.my_surface_background, mSurfaceFragment);
		ft.commit();
		if (lm == null)
			lm = new LayerManager(this);
		sm = new StateManager(this);
		//doodleFragment.setLayer((DoodleLayer)getLayerManager().currentLayer);

		oldConfig = getResources().getConfiguration();

		pauseBtn = findViewById(R.id.pauseRecord);
		pauseBtn.setEnabled(false);
		ImageButton startRecord = findViewById(R.id.startRecord);
		startRecord.setSelected(false);
		startRecord.setOnLongClickListener(new View.OnLongClickListener(){

				private static final int DIALOG_PICK_OUTPUT = 0;

				@Override
				public boolean onLongClick(View p1)
				{
					showDialog(DIALOG_PICK_OUTPUT);
					return true;
				}
			});
		Intent i = new Intent();
		i.setClass(this, SplashActivity.class);
		startActivity(i);
		Trace.endSection();
    }

	@Override{}
	protected void onResume()
	{
		// TODO: Implement this method
		super.onResume();

	}
	public void openVideoView(View v){
		View d = findViewById(R.id.mainVideoView);
		if(d.getVisibility()==View.INVISIBLE)d.setVisibility(View.VISIBLE);
		else{
			d.setVisibility(View.INVISIBLE);
		}
	}
	public void debug(String d)
	{
		//TextView c = findViewById(R.id.debugTextView);
		//c.setText(d);
	}
	Configuration oldConfig;
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		//int changes = newConfig.diff(oldConfig);
//		Log.e("changes",""+changes);
//		Log.e("orient",""+(changes&ActivityInfo.CONFIG_ORIENTATION));
//		Log.e("screen",""+(changes&ActivityInfo.CONFIG_SCREEN_SIZE));
//		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.remove(mSurfaceFragment);
		try{
		ft.commitAllowingStateLoss();
		}catch(IllegalStateException e){
			Log.w(TAG,"saving fragment failed");
		}
			
		onDestroy();
		onCreate(new Bundle());
		Log.e("onconfiguration changed", "called");
		//newConfig.orientation=Configuration.ORIENTATION_PORTRAIT;
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		//rm.setRecording(true,new File("/sdcard/test.mp4"));

		return super.onTouchEvent(event);
	}

	public void startRecording(View v)
	{
		if (mSurfaceFragment == null)
			return;
		RenderManager rm = mSurfaceFragment.rm;
		// TODO: Implement this method
		if (rm.isRecording())
		{
			rm.setRecording(false, null);
			pauseBtn.setEnabled(false);
			pauseBtn.setSelected(false);
			v.setSelected(false);
		}
		else
		{
			rm.setRecording(true, new File(outputFileName));
			pauseBtn.setEnabled(true);
			pauseBtn.setSelected(true);
			v.setSelected(true);
		}
	}
	public void pauseRecording(View v)
	{

		if (mSurfaceFragment == null)
			return;
		RenderManager rm = mSurfaceFragment.rm;
		if (rm.isRecording())
		{
			if (rm.isRecordPaused())
			{
				rm.resumeRecording();
				pauseBtn.setSelected(true);
				Toast.makeText(this, "Resumed Video", Toast.LENGTH_SHORT).show();
			}
			else
			{
				rm.pauseRecording();
				pauseBtn.setSelected(false);

				Toast.makeText(this, "Paused Video", Toast.LENGTH_SHORT).show();
			}
		}
	}
	@Override
	protected void onPause()
	{
		Trace.reset();
		// TODO: Implement this method
		super.onPause();

	}

	public LayerManager getLayerManager()
	{
		return lm;
	}
	public void addLayer(View v)
	{
		showDialog(DIALOG_PICK_LAYER);
	}

	@Override
	protected AlertDialog onCreateDialog(int id)
	{
		Trace.beginSection("create Dialog");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id)
		{
			case DIALOG_PICK_LAYER:
				enterSelectMode(null);
				builder.setItems(new CharSequence[]{
						"Doodle",
						"Video",
						"Picture",
						"Pdf",
						"Webpage",
						"Shape"
					}, new DialogInterface.OnClickListener(){



						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							switch (p2)
							{
								case 0:
									lm.add(new DoodleLayer(lm));
									//updateConfigView();
									break;
								case 1:
									pickVideo();
									break;
								case 2:
									pickPicture();
									break;

							}
						}



						private void pickPicture()
						{
							Intent i = new Intent();
							//i.setClass(getApplicationContext(), GalleryActivity.class);
							i.setType("image/*");
							i.setAction(Intent.ACTION_PICK);
							startActivityForResult(i, RESULT_PICK_IMAGE);
						}

						public void pickVideo()
						{

						}
					});

				return builder.create();
			}
		Trace.endSection();
		throw new RuntimeException("Wrong Dialog id");
	}
	
	@Override
	protected void onPrepareDialog(int id, final Dialog dialog)
	{
		// TODO: Implement this method
		switch (id)
		{
				
		}
	}
	
	private void onCurrentLayerChanged()
	{
		onCurrentLayerChanged(lm.currentLayer);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO: Implement this method
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode!=RESULT_OK)return;
		switch(requestCode){
			case RESULT_PICK_IMAGE:
				//lm.add(new ImageLayer(lm));
				try
				{
					Bitmap b = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
					lm.add(new ImageLayer(lm,b));
					onCurrentLayerChanged();
				}
				catch (FileNotFoundException e)
				{
					
				}
		}
	}
	
	public void enterRotateMode(View  v)
	{
		mSurfaceFragment.controller.enterRotateMode();
	}
	public void enterSelectMode(View  v)
	{
		mSurfaceFragment.controller.enterSelectMode();
	}
	public void enterScrollMode(View  v)
	{
		mSurfaceFragment.controller.enterScrollMode();
	}


}

