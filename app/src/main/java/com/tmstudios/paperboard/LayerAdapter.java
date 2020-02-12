package com.tmstudios.paperboard;
import android.widget.*;
import android.view.*;
import android.database.*;

public class LayerAdapter implements ListAdapter
{
	LayerManager mLayerManager;
	public LayerAdapter(LayerManager b){
		mLayerManager=b;
	}
	@Override
	public void registerDataSetObserver(DataSetObserver p1)
	{
		// TODO: Implement this method
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver p1)
	{
		// TODO: Implement this method
	}

	@Override
	public int getCount()
	{
		// TODO: Implement this method
		return mLayerManager.layers.size();
	}

	@Override
	public Object getItem(int p1)
	{
		// TODO: Implement this method
		return mLayerManager.layers.get(p1);
	}

	@Override
	public long getItemId(int p1)
	{
		// TODO: Implement this method
		return 0;
	}

	@Override
	public boolean hasStableIds()
	{
		// TODO: Implement this method
		return false;
	}

	@Override
	public View getView(int p1, View p2, ViewGroup p3)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public int getItemViewType(int p1)
	{
		// TODO: Implement this method
		return 0;
	}

	@Override
	public int getViewTypeCount()
	{
		// TODO: Implement this method
		return 0;
	}

	@Override
	public boolean isEmpty()
	{
		// TODO: Implement this method
		return false;
	}

	@Override
	public boolean areAllItemsEnabled()
	{
		// TODO: Implement this method
		return false;
	}

	@Override
	public boolean isEnabled(int p1)
	{
		// TODO: Implement this method
		return false;
	}
	
}
