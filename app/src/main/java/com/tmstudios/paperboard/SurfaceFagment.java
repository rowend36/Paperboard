package com.tmstudios.paperboard;
import android.view.*;
import android.os.*;
import com.tmstudios.paperboard.components.*;
import android.support.v4.app.*;

public class SurfaceFagment extends Fragment
{

	private View mSurface;
	public RenderManager rm;

	public Controller controller;

	private GestureDetector detector;
	public SurfaceFagment(){
		
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// TODO: Implement this method
		return new SurfaceView(getContext());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onViewCreated(view, savedInstanceState);
		this.mSurface=view;
		controller = new Controller((MainActivity)getActivity());
		detector = new GestureDetector(getActivity(), controller);
		detector.setOnDoubleTapListener(controller);

		detector.setContextClickListener(controller);
		final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(getActivity(), controller);
		
		mSurface.setOnTouchListener(new View.OnTouchListener(){
				public float lastX,lastY;
				@Override
				public boolean onTouch(View p1, MotionEvent p2)
				{
					scaleDetector.onTouchEvent(p2);
					detector.onTouchEvent(p2);

					switch (p2.getAction())
					{
						case p2.ACTION_UP:
							controller.onUp(p2);
			  				lastX = p2.getX();lastY = p2.getY();
							break;
						case p2.ACTION_MOVE:
							if (controller.enableEvents)
							{
								controller.onLongPressScroll(p2, p2.getX() - lastX, p2.getY() - lastY);
							}
							lastX = p2.getX();lastY = p2.getY();
							break;
						case p2.ACTION_DOWN:
			  				lastX = p2.getX();lastY = p2.getY();
							break;
					}
					return true;
				}


			});

		
		//surface = findViewById(R.id.my_surface);
		rm = new RenderManager((MainActivity)getActivity(), (SurfaceView)mSurface);
	}

	@Override
	public void onResume()
	{
		// TODO: Implement this method
		super.onResume();
		rm.resume();
	}

	@Override
	public void onPause()
	{
		// TODO: Implement this method
		super.onPause();
		rm.pause();
	}
	
}
