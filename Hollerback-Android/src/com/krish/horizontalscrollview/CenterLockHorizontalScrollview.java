package com.krish.horizontalscrollview;

import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.util.ViewUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class CenterLockHorizontalScrollview extends HorizontalScrollView {
	Context context;
	int prevIndex = 0;

	public CenterLockHorizontalScrollview(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		this.setSmoothScrollingEnabled(true);

	}

	public void setAdapter(Context context, CustomListBaseAdapter mAdapter) {
		try {
			fillViewWithAdapter(mAdapter);
		} catch (ZeroChildException e) {

			e.printStackTrace();
		}
	}

	private void fillViewWithAdapter(CustomListBaseAdapter mAdapter)
			throws ZeroChildException {
		if (getChildCount() == 0) {
			throw new ZeroChildException(
					"CenterLockHorizontalScrollView must have one child");
		}
		if (getChildCount() == 0 || mAdapter == null)
			return;

		ViewGroup parent = (ViewGroup) getChildAt(0);

		parent.removeAllViews();

		for (int i = 0; i < mAdapter.getCount(); i++) {
			parent.addView(mAdapter.getView(i, null, parent));
		}
	}

	public void setCenter(int index) {
		ViewGroup parent = (ViewGroup) getChildAt(0);

		View preView;
		preView = parent.getChildAt(index);

		preView.findViewById(R.id.iv_selected_video)
				.setVisibility(View.VISIBLE);

		LogUtil.i("Child count: " + getChildCount());

		for (int i = 0; i < parent.getChildCount(); i++) {
			preView = parent.getChildAt(i);

			if (i != index) {

				preView.findViewById(R.id.iv_selected_video).setVisibility(
						View.GONE);
			}
		}

		View view = parent.getChildAt(index);
		parent.invalidate();

		int screenWidth = ((Activity) context).getWindowManager()
				.getDefaultDisplay().getWidth();

		int scrollX = (view.getLeft() - (screenWidth / 2))
				+ (view.getWidth() / 2);
		this.smoothScrollTo(scrollX, 0);
		prevIndex = index;
	}

	public void snapCenter(int index) {
		int imageWidth = (int) ViewUtil.convertDpToPixel(80,
				HollerbackApplication.getInstance().getApplicationContext());

		ViewGroup parent = (ViewGroup) getChildAt(0);

		View preView;

		// for (int i = 0; i < this.getChildCount(); i++) {
		// preView = parent.getChildAt(i);
		//
		// if (i == index) {
		// preView.findViewById(R.id.iv_selected_video).setVisibility(
		// View.VISIBLE);
		// LogUtil.i("setting visible: " + i);
		// } else {
		// preView.findViewById(R.id.iv_selected_video).setVisibility(
		// View.GONE);
		// }
		// }
		preView = parent.getChildAt(index);

		if (preView != null) {
			preView.findViewById(R.id.iv_selected_video).setVisibility(
					View.VISIBLE);
		}
		LogUtil.i("Child count: " + getChildCount());

		for (int i = 0; i < parent.getChildCount(); i++) {

			preView = parent.getChildAt(i);

			if (preView != null) {
				if (i != index) {

					preView.findViewById(R.id.iv_selected_video).setVisibility(
							View.GONE);
				}
			}
		}

		View view = parent.getChildAt(index);
		parent.invalidate();

		int screenWidth = ((Activity) context).getWindowManager()
				.getDefaultDisplay().getWidth();

		if (view != null) {
			LogUtil.i("View Left: " + view.getLeft() + " view right: "
					+ view.getRight());
			// int scrollX = (view.getLeft() - (screenWidth / 2))
			// + (view.getWidth() / 2);
			// // this.smoothScrollTo(scrollX, 0);
			// this.setScrollX(scrollX);
			// prevIndex = index;
			LogUtil.i("View is not null!!!");
			int scrollX = ((imageWidth * index) - (screenWidth / 2))
					+ ((imageWidth * (index + 1)) / 2);
			// this.smoothScrollTo(scrollX, 0);
			LogUtil.i("View set scrollx " + scrollX);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				this.setScrollX(scrollX);
			} else {
				this.scrollTo(scrollX, 0);
			}

			prevIndex = index;
		} else {

			LogUtil.i("View is null!!!");
			int scrollX = ((imageWidth * index) - (screenWidth / 2))
					+ ((imageWidth * (index + 1)) / 2);
			// this.smoothScrollTo(scrollX, 0);
			this.setScrollX(scrollX);
			prevIndex = index;
		}
	}

}
