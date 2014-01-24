package com.moziy.hollerback.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.moziy.hollerback.R;
import com.moziy.hollerback.fragment.delegates.ConvoHistoryDelegate.GetRemoteHistoryTask;
import com.moziy.hollerback.fragment.workers.FragmentTaskWorker;
import com.moziy.hollerback.fragment.workers.FragmentTaskWorker.TaskClient;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.task.Task;
import com.squareup.picasso.Picasso;

public class ConvoHistoryFragment extends BaseFragment implements TaskClient {
    public static final String CONVO_ID_BUNDLE_ARG_KEY = "CONVO_ID";

    public static ConvoHistoryFragment newInstance(long convoId) {
        ConvoHistoryFragment f = new ConvoHistoryFragment();

        Bundle args = new Bundle();
        args.putLong(CONVO_ID_BUNDLE_ARG_KEY, convoId);
        f.setArguments(args);

        return f;
    }

    private long mConvoId;
    public static final String WORKER = "convo_history_worker";
    private ConvoHistoryAdapter mAdapter;
    private ListView mHistoryList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConvoId = getArguments().getLong(CONVO_ID_BUNDLE_ARG_KEY);

        Fragment f = getFragmentManager().findFragmentByTag(WORKER);
        if (f == null) {
            f = new FragmentTaskWorker();
            f.setTargetFragment(this, 0);
            getFragmentManager().beginTransaction().add(f, WORKER).commit();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.convo_history_layout, container, false);
        mHistoryList = (ListView) v.findViewById(R.id.lv_convo_history);
        return v;
    }

    @Override
    protected String getScreenName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onPause() {

        if (isRemoving()) {
            Fragment f = getFragmentManager().findFragmentByTag(WORKER);
            if (f != null)
                getFragmentManager().beginTransaction().remove(f).commit();
        }

        super.onPause();
    }

    @Override
    public void onTaskComplete(Task t) {
        mAdapter = new ConvoHistoryAdapter(getActivity(), R.layout.convo_history_list_item, R.id.tv_date);
        mAdapter.addAll(((GetRemoteHistoryTask) t).getRemoteVideos());
        mHistoryList.setAdapter(mAdapter);

    }

    @Override
    public void onTaskError(Task t) {

    }

    @Override
    public Task getTask() {
        return new GetRemoteHistoryTask(mConvoId, -1, false);
    }

    public static class ConvoHistoryAdapter extends ArrayAdapter<VideoModel> {

        private LayoutInflater mInflater;

        public ConvoHistoryAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.convo_history_list_item, parent, false);
                holder = new ViewHolder();
                holder.mSquareImageView = (ImageView) convertView.findViewById(R.id.iv_square);
                holder.mDateTextView = (TextView) convertView.findViewById(R.id.tv_date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            VideoModel v = getItem(position);
            holder.mDateTextView.setText(v.getCreateDate());
            Picasso.with(getContext()).load(v.getThumbUrl()).into(holder.mSquareImageView);

            return convertView;
        }

        private class ViewHolder {
            public ImageView mSquareImageView;
            public TextView mDateTextView;
        }

    }

}
