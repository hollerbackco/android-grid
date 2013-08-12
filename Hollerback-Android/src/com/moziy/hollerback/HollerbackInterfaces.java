package com.moziy.hollerback;

import android.view.View;

public class HollerbackInterfaces {
	
	public interface OnCustomItemClickListener{
		public void onItemClicked(int position, View convertView);
	}

	public interface OnFilterVideoListener{
		public void onFilterSelected(int position);
	}
}
