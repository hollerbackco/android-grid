package com.moziy.hollerback.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.R.color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.activeandroid.util.Log;
import com.moziy.hollerback.R;
import com.moziy.hollerback.fragment.ConversationListFragment;
import com.moziy.hollerback.fragment.RecordVideoFragment;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.network.VolleySingleton;
import com.moziy.hollerback.util.ConversionUtil;
import com.moziy.hollerback.view.RoundImageView;
import com.moziy.hollerback.view.RoundNetworkImageView;
import com.moziy.hollerback.widget.CustomButton;

public class ConversationListAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = ConversationListAdapter.class.getSimpleName();
    protected List<ConversationModel> mConversations;
    protected List<ConversationModel> mFilteredConversations;

    private LruCache<String, Bitmap> mFileCache = new LruCache<String, Bitmap>(10); // up to 10 new convos

    LayoutInflater inflater;
    ConversationFilter mFilter;
    private ColorPicker mColorPicker = new ColorPicker();
    private int mHBTextColor;
    private Map<ConversationModel, int[]> mConvoColorMap;

    private SherlockFragmentActivity mActivity;

    // protected ImageLoader imageLoader = ImageLoader.getInstance();

    public ConversationListAdapter(SherlockFragmentActivity activity) {
        mActivity = activity;
        inflater = LayoutInflater.from(activity);
        mConversations = new ArrayList<ConversationModel>();
        mFilteredConversations = new ArrayList<ConversationModel>();

        mHBTextColor = mActivity.getResources().getColor(R.color.hb_blue);
        // options = new DisplayImageOptions.Builder().showStubImage(R.drawable.background_opaque).showImageForEmptyUri(R.drawable.background_opaque).showImageOnFail(R.drawable.background_opaque)
        // .cacheInMemory(true).cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY).build();
    }

    public void setConversations(List<ConversationModel> conversations) {
        mConversations = conversations;
        mFilteredConversations = new ArrayList<ConversationModel>();
        mFilteredConversations.addAll(mConversations);
        mConvoColorMap = new HashMap<ConversationModel, int[]>();
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
            convertView = inflater.inflate(R.layout.conversation_list_item, parent, false);
            viewHolder.topLayer = (ViewGroup) convertView.findViewById(R.id.top_layer);
            viewHolder.conversationName = (TextView) convertView.findViewById(R.id.tv_convoname);
            viewHolder.conversationTime = (TextView) convertView.findViewById(R.id.tv_time);
            viewHolder.conversationSubTitle = (TextView) convertView.findViewById(R.id.tv_ttyl);
            viewHolder.thumb = (RoundNetworkImageView) convertView.findViewById(R.id.iv_thumb);
            viewHolder.localThumb = (RoundImageView) convertView.findViewById(R.id.iv_local_thumb);
            viewHolder.btnRecord = (CustomButton) convertView.findViewById(R.id.btnRecord);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ConversationModel conversationModel = mFilteredConversations.get(position);
        viewHolder.conversationSubTitle.setText(""); // clear the text
        // Log.d(TAG, "unread count: " + conversationModel.getUnreadCount());
        if (conversationModel.getUnreadCount() > 0) {
            int[] colors;
            if (mConvoColorMap.containsKey(conversationModel)) {
                colors = mConvoColorMap.get(conversationModel);
            } else {
                colors = mColorPicker.getConvoColors();
                mConvoColorMap.put(conversationModel, colors);
            }
            viewHolder.thumb.setHaloBorderColor(colors[0]);
            viewHolder.topLayer.setBackgroundColor(colors[1]);
            viewHolder.conversationName.setTextColor(Color.WHITE);
            viewHolder.conversationTime.setTextColor(Color.WHITE);
            viewHolder.btnRecord.setEmphasized(true);
            viewHolder.btnRecord.setVisibility(View.GONE);

        } else {
            convertView.setBackgroundColor(color.white);
            viewHolder.topLayer.setBackgroundColor(Color.WHITE);
            viewHolder.conversationName.setTextColor(mHBTextColor);
            viewHolder.conversationTime.setTextColor(mHBTextColor);
            viewHolder.thumb.setHaloBorderColor(-1); // clear any border
            viewHolder.localThumb.setHaloBorderColor(-1);
            viewHolder.btnRecord.setEmphasized(false);
            viewHolder.btnRecord.setVisibility(View.VISIBLE);

            if (conversationModel.getSubTitle() != null)
                viewHolder.conversationSubTitle.setText(conversationModel.getSubTitle());

        }

        viewHolder.conversationName.setText(conversationModel.getConversationName());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ", Locale.US);
        try {
            Date date = df.parse(conversationModel.getLastMessageAt());
            viewHolder.conversationTime.setText(ConversionUtil.timeAgo(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (conversationModel.getMostRecentThumbUrl() != null) {

            if (conversationModel.getMostRecentThumbUrl().contains("file:///")) {
                // Log.d(TAG, "from file");
                Bitmap b = mFileCache.get(conversationModel.getMostRecentThumbUrl());
                if (b == null) {
                    b = BitmapFactory.decodeFile(Uri.parse(conversationModel.getMostRecentThumbUrl()).getPath());
                    if (b != null)
                        mFileCache.put(conversationModel.getMostRecentThumbUrl(), b);
                }
                viewHolder.localThumb.setVisibility(View.VISIBLE);
                viewHolder.thumb.setVisibility(View.INVISIBLE);
                viewHolder.localThumb.setImageBitmap(b);
                // viewHolder.thumb.setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), BitmapFactory.decodeFile(conversationModel.getMostRecentThumbUrl().substring(9)))); //
                // setImageBitmap(BitmapFactory.decodeFile(conversationModel.getMostRecentThumbUrl().substring(9)));
            } else {
                viewHolder.thumb.setVisibility(View.VISIBLE);
                viewHolder.localThumb.setVisibility(View.GONE);
                viewHolder.thumb.setImageUrl(conversationModel.getMostRecentThumbUrl(), VolleySingleton.getInstance(mActivity).getImageLoader());
            }
        }

        viewHolder.btnRecord.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mActivity.getActionBar().hide();
                // TODO: no need to pass in watched ids
                RecordVideoFragment fragment = RecordVideoFragment.newInstance(conversationModel.getConversationId(), true, conversationModel.getConversationName());
                mActivity.getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in_scale_up, R.anim.fade_out, R.anim.slide_in_from_top, R.anim.slide_out_to_bottom)
                        .replace(R.id.fragment_holder, fragment).addToBackStack(ConversationListFragment.FRAGMENT_TAG).commitAllowingStateLoss();
            }
        });

        final GestureDetector detector = new GestureDetector(mActivity, new SimpleOnGestureListener() {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {

                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                    } else {
                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                onSwipeBottom();
                            } else {
                                onSwipeTop();
                            }
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }

        });

        convertView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(final View v, MotionEvent event) {

                if (event.getAction() != MotionEvent.ACTION_DOWN)
                    return detector.onTouchEvent(event);

                return false;

            }
        });

        return convertView;
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
        Log.d("sw", "swipe left");
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }

    public static class ViewHolder {
        public ViewGroup topLayer;
        TextView conversationName;
        TextView conversationTime;
        TextView conversationSubTitle;
        RoundNetworkImageView thumb;
        RoundImageView localThumb;
        CustomButton btnRecord;
        int foregroundColor = -1;
        int backgroundColor = -1;
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

    class ConversationItem {
        public ConversationModel conversation;
        public int foregroundColor = -1;
        public int backgroundColor = -1;
    }

    List<ConversationItem> getConversationItemListFor(List<ConversationModel> model) {
        List<ConversationItem> items = new ArrayList<ConversationListAdapter.ConversationItem>();
        for (ConversationModel m : model) {
            ConversationItem item = new ConversationItem();
            item.conversation = m;
            items.add(item);
        }
        return items;
    }

}
