package com.moziy.hollerback.service.helper;

import java.util.ArrayList;
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

        List<VideoModel> videos = new Select().from(VideoModel.class).where("(" + where + ")" + " AND " + ActiveRecordFields.C_VID_TRANSACTING + "=0").execute();
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

    public static synchronized VideoModel getVideoForTransaction(String where) {
        VideoModel v = new Select().from(VideoModel.class).where("(" + where + ")" + " AND " + ActiveRecordFields.C_VID_TRANSACTING + "=0").executeSingle();
        if (v != null) {
            v.setTransacting();
            v.save();
        }
        return v;
    }

    public static synchronized void clearVideoTransacting(VideoModel video) {
        video.clearTransacting();
        video.save();
    }

    public static synchronized void clearVideoTransacting(List<VideoModel> videos) {
        if (videos.isEmpty())
            return;

        ActiveAndroid.beginTransaction();

        try {
            for (VideoModel video : videos) {
                video.clearTransacting();
                video.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    public static ArrayList<String> getWatchedIds(List<VideoModel> watchedVideos) {

        final ArrayList<String> watchedIds = new ArrayList<String>();// (ArrayList<String>) intent.getStringArrayListExtra(INTENT_ARG_WATCHED_IDS); // TODO: store this in another table?

        for (VideoModel watchedVideo : watchedVideos) {
            watchedIds.add(watchedVideo.getGuid());
        }
        // lets just query the watched ids

        return watchedIds;
    }

    public static void markVideosAsWatched(List<VideoModel> watchedVideos) {
        if (watchedVideos.isEmpty()) {
            return;
        }

        // udpate all the watched videos state to watched
        ActiveAndroid.beginTransaction();
        try {
            for (VideoModel v : watchedVideos) {
                v.setWatchedState(VideoModel.ResourceState.WATCHED_AND_POSTED);
                v.save();
            }

            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

}
