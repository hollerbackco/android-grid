package com.moziy.hollerback.util;

import java.util.ArrayList;

import com.moziy.hollerback.model.VideoModel;

public class ArrayModifierUtil {

	public boolean modifyVideosArray(ArrayList<VideoModel> originalVideos,
			ArrayList<VideoModel> newVideos) {
		if (originalVideos.isEmpty()) {
			originalVideos.addAll(newVideos);
			return true;
		} else {

			// modify
				
			
			if (originalVideos.size() == newVideos.size()) {
				return false;
			} else {
				return true;
			}
		}
	}

}
