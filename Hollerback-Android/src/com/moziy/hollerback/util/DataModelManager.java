package com.moziy.hollerback.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Intent;
import android.os.AsyncTask;

import com.moziy.hollerback.cache.memory.TempMemoryStore;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.helper.ActiveRecordHelper;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.util.security.HashUtil;

/**
 * Abstract usage of database, memory store, api calls
 * 
 * @author jianchen
 * 
 */
public class DataModelManager {

    private static HashMap<String, Object> mObjectHash;
    private static TempMemoryStore mTempMemoryStore;

    public DataModelManager() {
        mObjectHash = new HashMap<String, Object>();
    }

    private class GetConversationsAsyncTask extends AsyncTask<Void, Void, ArrayList<ConversationModel>> {

        @Override
        protected ArrayList<ConversationModel> doInBackground(Void... params) {

            ArrayList<ConversationModel> conversationModel = (ArrayList<ConversationModel>) ActiveRecordHelper.getAllConversations();

            return conversationModel;
        }

        @Override
        protected void onPostExecute(ArrayList<ConversationModel> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            String hash = HashUtil.getConvHash();

            mObjectHash.put(hash, result);
            /*
             * Intent intent = new Intent(IABIntent.INTENT_GET_CONVERSATIONS); intent.putExtra(IABIntent.PARAM_INTENT_DATA, hash); IABroadcastManager.sendLocalBroadcast(intent);
             */
        }

    }

    private class GetVideoAsyncTask extends AsyncTask<Long, Void, HashMap<Long, ArrayList<VideoModel>>> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(HashMap<Long, ArrayList<VideoModel>> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            Iterator<Map.Entry<Long, ArrayList<VideoModel>>> it = result.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, ArrayList<VideoModel>> pairs = (Map.Entry<Long, ArrayList<VideoModel>>) it.next();
                System.out.println(pairs.getKey() + " = " + pairs.getValue());
                Intent intent = new Intent(IABIntent.GET_CONVERSATION_VIDEOS);

                String hash = HashUtil.generateHashFor(IABIntent.ASYNC_REQ_VIDEOS, String.valueOf(pairs.getKey()));

                mObjectHash.put(hash, pairs.getValue());

                intent.putExtra(IABIntent.PARAM_INTENT_DATA, hash);
                // IABroadcastManager.sendLocalBroadcast(intent);
                it.remove(); // avoids a ConcurrentModificationException
            }

        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
        }

        @Override
        protected HashMap<Long, ArrayList<VideoModel>> doInBackground(Long... params) {

            if (params.length != 1) {
                return null;
            }

            HashMap<Long, ArrayList<VideoModel>> h = new HashMap<Long, ArrayList<VideoModel>>();

            ArrayList<VideoModel> videos = (ArrayList<VideoModel>) ActiveRecordHelper.getVideosForConversation(params[0]);

            if (videos != null) {
                Collections.reverse(videos);
                h.put(params[0], videos);
            }

            return h;

        }
    }

    public Object getObjectForToken(String token) {
        if (mObjectHash.containsKey(token)) {
            return mObjectHash.get(token);
        }
        return null;
    }

    public void putIntoHash(String key, Object value) {
        mObjectHash.put(key, value);
    }

}
