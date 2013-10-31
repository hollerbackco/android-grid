package com.moziy.hollerback.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListAdapter;
import android.widget.VideoView;
import android.widget.WrapperListAdapter;

import com.activeandroid.util.Log;
import com.origamilabs.library.views.StaggeredGridView;

public class CustomStaggeredGridView extends StaggeredGridView{
	private SparseIntArray mScrollingSiblings = new SparseIntArray();

	public CustomStaggeredGridView(Context context) {
		super(context);
	}
	
    public CustomStaggeredGridView(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.gridViewStyle);
    }

    public CustomStaggeredGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
		getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener()
				{
					@Override
					public void onGlobalLayout()
					{
						updateSiblingPositions();
					}
				}
		);
    }
    
    
	
	public void addScrollingSibling(int position, int viewId)
	{
		mScrollingSiblings.append(position, viewId);
	}
	
	public boolean removeScrollingSibling(int position)
	{
		int index = mScrollingSiblings.indexOfKey(position);
		if(index < 0){
			return false;
		}
		mScrollingSiblings.removeAt(index);
		return true;
	}
	
	public void clearScrollingSiblings()
	{
		mScrollingSiblings.clear();
	}
	
    
	@Override
	public void setAdapter(ListAdapter adapter)
	{
		if(adapter != null){
			adapter = new ScrollingSyncAdapter(adapter);
		}
		super.setAdapter(adapter);
	}
	
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		super.onScrollChanged(l, t, oldl, oldt);
		updateSiblingPositions();
	}
	
	private void updateSiblingPositions()
	{
		ViewGroup parent = (ViewGroup)getParent();
		final int firstPos = getFirstPosition();
		final int lastPos = firstPos + getChildCount();
		final int size = mScrollingSiblings.size();
		for(int i = 0; i < size; i++){
			View view = parent.findViewById(mScrollingSiblings.valueAt(i));
			if(view == null){
				continue;
			}
			int pos = mScrollingSiblings.keyAt(i);
			if(pos >= firstPos && pos <= lastPos){
				View child = getChildAt(pos - firstPos);
				int left   = child.getLeft();
				int top    = child.getTop();
				int width  = view.getWidth();
				int height = view.getHeight();
				if(child instanceof PlaceHolderView){
					((PlaceHolderView)child).setFixedDimensions(width, height);
				}
				view.layout(left, top, left + width, top + height);
				view.setVisibility(View.VISIBLE);
			}
			else{
				view.setVisibility(View.INVISIBLE);
			}
		}
	}
    
	private class ScrollingSyncAdapter implements WrapperListAdapter
	{
		private ListAdapter mWrapped;
		private SparseArray<PlaceHolderView> mPlaceHolders = new SparseArray<PlaceHolderView>();
		
		
		public ScrollingSyncAdapter(ListAdapter wrapped)
		{
			mWrapped = wrapped;
		}
		
		@Override
		public ListAdapter getWrappedAdapter()
		{
			return mWrapped;
		}
		
		@Override
		public void registerDataSetObserver(DataSetObserver observer)
		{
			mWrapped.registerDataSetObserver(observer);
		}
		
		@Override
		public void unregisterDataSetObserver(DataSetObserver observer)
		{
			mWrapped.unregisterDataSetObserver(observer);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View sibling;
			int viewId = mScrollingSiblings.get(position);
			if(viewId == 0 || (sibling = ((ViewGroup)parent.getParent()).findViewById(viewId)) == null){
				return mWrapped.getView(position, convertView, parent);
			}
			PlaceHolderView view = mPlaceHolders.get(position);
			if(view == null){
				view = new PlaceHolderView(parent.getContext());
			}
			view.setFixedDimensions(sibling.getWidth(), sibling.getHeight());
			return view;
		}
		
		@Override
		public Object getItem(int position)
		{
			return mWrapped.getItem(position);
		}
		
		@Override
		public long getItemId(int position)
		{
			return mWrapped.getItemId(position);
		}
		
		@Override
		public boolean hasStableIds()
		{
			return mWrapped.hasStableIds();
		}
		
		@Override
		public boolean areAllItemsEnabled()
		{
			return mWrapped.areAllItemsEnabled();
		}
		
		@Override
		public boolean isEnabled(int position)
		{
			return mWrapped.isEnabled(position);
		}
		
		@Override
		public int getItemViewType(int position)
		{
			int viewId = mScrollingSiblings.get(position);
			if(viewId != 0){
				return IGNORE_ITEM_VIEW_TYPE;
			}
			return mWrapped.getItemViewType(position);
		}
		
		@Override
		public int getViewTypeCount()
		{
			return mWrapped.getViewTypeCount();
		}
		
		@Override
		public int getCount()
		{
			return mWrapped.getCount();
		}
		
		@Override
		public boolean isEmpty()
		{
			return mWrapped.isEmpty();
		}
	}
    
	private static class PlaceHolderView extends View
	{
		private int mWidth;
		private int mHeight;
		
		
		public PlaceHolderView(Context context)
		{
			super(context);
			setWillNotDraw(true);
		}
		
		public void setFixedDimensions(int width, int height)
		{
			mWidth  = width;
			mHeight = height;
		}
		
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			setMeasuredDimension(getDefaultSize(mWidth, widthMeasureSpec), getDefaultSize(mHeight, heightMeasureSpec));
		}
	}
}
