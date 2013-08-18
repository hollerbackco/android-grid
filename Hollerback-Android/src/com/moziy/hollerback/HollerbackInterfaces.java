package com.moziy.hollerback;

import android.view.View;

public class HollerbackInterfaces {
	
	public interface OnCustomItemClickListener{
		public void onItemClicked(int position, View convertView);
	}
	
	public interface OnContactSelectedListener{
		public void onItemClicked(int position);
	}
}
