package com.moziy.hollerback.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.moziy.hollerback.R;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.fragment.ConversationListFragment;
import com.moziy.hollerback.fragment.RecordVideoFragment;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.network.VolleySingleton;
import com.moziy.hollerback.util.ConversionUtil;
import com.moziy.hollerback.view.RoundImageView;

public class ConversationListAdapter extends BaseAdapter implements Filterable {

    protected List<ConversationModel> mConversations;
    protected List<ConversationModel> mFilteredConversations;

    LayoutInflater inflater;
    ConversationFilter mFilter;
    private ColorPicker mColorPicker = new ColorPicker();

    private SherlockFragmentActivity mActivity;

    // protected ImageLoader imageLoader = ImageLoader.getInstance();

    public ConversationListAdapter(SherlockFragmentActivity activity) {
        mActivity = activity;
        inflater = LayoutInflater.from(activity);
        mConversations = new ArrayList<ConversationModel>();
        mFilteredConversations = new ArrayList<ConversationModel>();

        // options = new DisplayImageOptions.Builder().showStubImage(R.drawable.background_opaque).showImageForEmptyUri(R.drawable.background_opaque).showImageOnFail(R.drawable.background_opaque)
        // .cacheInMemory(true).cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY).build();
    }

    public void setConversations(List<ConversationModel> conversations) {
        mConversations = conversations;
        mFilteredConversations = new ArrayList<ConversationModel>();
        mFilteredConversations.addAll(mConversations);
        this.notifyDataSetChanged();
    }

    public List<ConversationModel> getConversations() {
        return mFilteredConversations;
    }

    public void clearConversations() {
        if (mConversations != null) {
            mConversations = new ArrayList<ConversationModel>();
            mFilteredConversations = new ArrayList<ConversationModel>();
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFilteredConversations.size();
    }

    @Override
    public ConversationModel getItem(int position) {
        // TODO Auto-generated method stub
        return mFilteredConversations.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.message_list_item, parent, false);
            viewHolder.conversationName = (TextView) convertView.findViewById(R.id.tv_convoname);
            viewHolder.newMessagesIndicator = (ImageView) convertView.findViewById(R.id.iv_green_dot);
            viewHolder.conversationTime = (TextView) convertView.findViewById(R.id.tv_time);
            viewHolder.thumb = (RoundImageView) convertView.findViewById(R.id.iv_thumb);
            // viewHolder.imgBackground = (ImageView) convertView.findViewById(R.id.imgBackground);
            viewHolder.btnRecord = (ImageView) convertView.findViewById(R.id.btnRecord);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (mFilteredConversations.get(position).getUnreadCount() > 0) {
            viewHolder.newMessagesIndicator.setVisibility(View.VISIBLE);
        } else {
            viewHolder.newMessagesIndicator.setVisibility(View.INVISIBLE);
        }

        viewHolder.conversationName.setText(mFilteredConversations.get(position).getConversationName().toUpperCase());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ", Locale.US);
        try {
            Date date = df.parse(mFilteredConversations.get(position).getLastMessageAt());
            viewHolder.conversationTime.setText(ConversionUtil.timeAgo(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (mFilteredConversations.get(position).getMostRecentThumbUrl() != null) {
            // imageLoader.displayImage(mFilteredConversations.get(position).getUrl(), viewHolder.imgBackground, options);
            viewHolder.thumb.setImageUrl(mFilteredConversations.get(position).getMostRecentThumbUrl(), VolleySingleton.getInstance(mActivity).getImageLoader());

            int[] colors = mColorPicker.getConvoColors();
            // Test, lets set a color
            viewHolder.thumb.setHaloBorderColor(colors[0]);

            convertView.setBackgroundColor(colors[1]);

        }

        viewHolder.btnRecord.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Peter: no idea why it's int here and string everywhere else, this was written prior:
                // I changed it to be "long" everywhere :)
                mActivity.getActionBar().hide();
                // XXX: Make sure that the watched ids are sent here
                RecordVideoFragment fragment = RecordVideoFragment.newInstance(mFilteredConversations.get(position).getConversationId(), true, mFilteredConversations.get(position)
                        .getConversationName(), new ArrayList<String>());
                mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(ConversationListFragment.FRAGMENT_TAG).commitAllowingStateLoss();
            }
        });

        LogUtil.i("Conv " + mFilteredConversations.get(position).getConversationName());

        return convertView;
    }

    static class ViewHolder {
        TextView conversationName;
        ImageView newMessagesIndicator;
        TextView conversationTime;
        RoundImageView thumb;
        ImageView btnRecord;
    }

    /**
     * A conversation color picker
     * @author sajjad
     *
     */
    public class ColorPicker {

        private int mColorIndex = 0;

        private int[] mBackgroundColors = {
                R.color.convo_blue, R.color.convo_green, R.color.convo_red, R.color.convo_purple, R.color.convo_yellow, R.color.convo_orange
        };
        private int[] mForegroundColors = {
                R.color.convo_red, R.color.convo_yellow, R.color.convo_green, R.color.convo_orange, R.color.convo_purple, R.color.convo_blue_2
        };

        public int[] getConvoColors() {

            int[] colors = {
                    mActivity.getResources().getColor(mForegroundColors[mColorIndex]), mActivity.getResources().getColor(mBackgroundColors[mColorIndex])
            };

            ++mColorIndex;

            if (mColorIndex >= mBackgroundColors.length) // reset if needed
                mColorIndex = 0;

            return colors;
        }

    }

    class ConversationFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
                results.values = mConversations;
                results.count = mConversations.size();
            } else {
                // We perform filtering operation
                ArrayList<ConversationModel> nConversations = new ArrayList<ConversationModel>();

                for (ConversationModel c : mConversations) {
                    if (c.getConversationName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        nConversations.add(c);
                    }
                }

                results.values = nConversations;
                results.count = nConversations.size();

            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Now we have to inform the adapter about the new list filtered
            mFilteredConversations = (ArrayList<ConversationModel>) results.values;
            notifyDataSetChanged();
        }

    }

    @Override
    public Filter getFilter() {
        if (mFilter == null)
            mFilter = new ConversationFilter();

        return mFilter;
    }
}
