package com.moziy.hollerback;

import android.view.View;

public class HollerbackInterfaces {
	
	public interface OnCustomItemClickListener{
		public void onItemClicked(int position, View convertView);
	}

	public interface OnTriggerNextVideo{
		public void onPositionTriggered(int position);
	}
}
