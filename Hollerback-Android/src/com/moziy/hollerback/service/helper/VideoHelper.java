package com.moziy.hollerback.service.helper;

import java.util.List;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.VideoModel;

public class VideoHelper {

    /**
     * Retrieves a list of videos so that a transaction can be performed on them
     * @return  A list of videos if prior to this call had their state as not transacting.
     * The transacting flag will automatically be set
     */
    public static synchronized List<VideoModel> getVideosForTransaction(String where) {

        List<VideoModel> videos = new Select().from(VideoModel.class).where(where + " AND " + ActiveRecordFields.C_VID_TRANSACTING + "=0").execute();
        ActiveAndroid.beginTransaction();
        try {
            for (VideoModel video : videos) {
                video.setTransacting();
                video.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }

        return videos;
    }

}
