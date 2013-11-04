package com.krish.horizontalscrollview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moziy.hollerback.HollerbackInterfaces.OnCustomItemClickListener;
import com.moziy.hollerback.R;
import com.moziy.hollerback.bitmap.ImageFetcher;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.helper.ProgressHelper;
import com.moziy.hollerback.helper.S3RequestHelper;
import com.moziy.hollerback.model.UploadingModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.ConversionUtil;
import com.moziy.hollerback.util.DipUtil;
import com.moziy.hollerback.view.CustomVideoView;
import com.moziy.hollerbacky.connection.HBRequestManager;

public class CustomListBaseAdapter extends BaseAdapter {
    Context context;
    public View view;
    public ArrayList<UploadingModel> mUploadingHelper = new ArrayList<UploadingModel>();
    public int currPosition = 0;
    LayoutInflater mInflater;

    private ArrayList<VideoModel> mVideoModels;
    private ArrayList<CustomVideoView> mVideoViews = new ArrayList<CustomVideoView>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, View> mViews = new HashMap<Integer, View>();

    ImageFetcher mImageFetcher;
    S3RequestHelper mS3RequestHelper;
    private View mWrapperInformation;

    private OnCustomItemClickListener mCustomClickListener;
    int mVideoWidth;

    public CustomListBaseAdapter(Activity activity, ImageFetcher imageFetcher, S3RequestHelper helper, View wrapperInformation) {
        mWrapperInformation = wrapperInformation;
        mImageFetcher = imageFetcher;
        this.context = activity.getApplicationContext();
        mInflater = LayoutInflater.from(activity);
        this.mS3RequestHelper = helper;
        mVideoModels = new ArrayList<VideoModel>();
    }

    public void setOnCustomItemClickListener(OnCustomItemClickListener listener) {
        mCustomClickListener = listener;
    }

    @Override
    public int getCount() {
        return mVideoModels.size();
    }

    public void setListItems(ArrayList<VideoModel> videos) {
        mVideoModels = videos;
    }

    @Override
    public VideoModel getItem(int position) {
        return mVideoModels.get(position);

    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        view = convertView;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.video_gallery_item, parent, false);

            viewHolder.videoThumbnail = (ImageView) convertView.findViewById(R.id.iv_video_thumbnail);
            viewHolder.videoThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            viewHolder.unreadCircle = (ImageView) convertView.findViewById(R.id.iv_unread_circle);

            viewHolder.videoPlayer = (CustomVideoView) convertView.findViewById(R.id.videoPlayer);
            viewHolder.txtTime = (TextView) convertView.findViewById(R.id.txtTime);
            viewHolder.txtSent = (TextView) convertView.findViewById(R.id.txtSent);

            viewHolder.progresshelper = new ProgressHelper(convertView.findViewById(R.id.rl_progress));
            viewHolder.progresshelper.setSiblingTextView(viewHolder.txtTime);

            viewHolder.progressupload = new ProgressHelper(convertView.findViewById(R.id.rl_upload));
            viewHolder.progressupload.setSiblingTextView(viewHolder.txtTime);

            viewHolder.gridWrapper = (RelativeLayout) convertView.findViewById(R.id.gridWrapper);
            convertView.setTag(viewHolder);
        } else {
            convertView.setPadding(0, 0, 0, 0);
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.videoPlayer.setVisibility(View.GONE);
        }

        viewHolder.videoPlayer.setProgressHelper(viewHolder.progresshelper);
        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (viewHolder.videoPlayer.isPlaying()) {
                    viewHolder.videoPlayer.pause();
                    viewHolder.videoPlayer.setTag(viewHolder.videoPlayer.getCurrentPosition());
                } else if (viewHolder.videoPlayer.getTag() != null) {
                    viewHolder.videoPlayer.seekTo((Integer) viewHolder.videoPlayer.getTag());
                    viewHolder.videoPlayer.start();
                    viewHolder.videoPlayer.setTag(null);
                } else if (mCustomClickListener != null) {
                    LogUtil.i("Clicking on position: " + position);

                    mCustomClickListener.onItemClicked(position, v);

                    // Clean up
                    for (int i = 0; i < mVideoViews.size(); i++) {
                        if (mVideoViews.get(i).hasProgressHelper()) {
                            mVideoViews.get(i).stopProgressHelper();
                        }
                        if (mVideoViews.get(i).hasProgressHelper()) {
                            mVideoViews.get(i).stopProgressHelper();
                        }
                        if (mVideoViews.get(i).isPlaying()) {
                            mVideoViews.get(i).pause();
                            mVideoViews.get(i).stopPlayback();
                        }
                        mVideoViews.get(i).setVisibility(View.GONE);
                    }

                    viewHolder.progresshelper.startIndeterminateSpinner();
                    viewHolder.videoPlayer.changeVideoSize(viewHolder.gridWrapper.getWidth(), viewHolder.gridWrapper.getHeight());

                    if (position < CustomListBaseAdapter.this.getCount() - 1) {
                        viewHolder.videoPlayer.setNextView(mViews.get(position + 1));
                    }

                    mVideoViews.add(viewHolder.videoPlayer);
                    VideoModel model = mVideoModels.get(position);
                    mS3RequestHelper.downloadS3(AppEnvironment.getInstance().PICTURE_BUCKET, model.getLocalFileName(), viewHolder.progresshelper, viewHolder.videoPlayer, mWrapperInformation,
                            mVideoViews);

                    if (!model.isRead()) {
                        model.setRead(true);
                    }
                    if (model.isSent()) {
                        model.setSent(true);
                    }
                    viewHolder.txtSent.setVisibility(View.GONE);

                    HBRequestManager.postVideoRead(Integer.toString(model.getVideoId()));
                }
                viewHolder.unreadCircle.setVisibility(View.GONE);
            }
        });

        if (mVideoWidth == 0) {
            mVideoWidth = viewHolder.videoThumbnail.getWidth();
        }

        VideoModel model = mVideoModels.get(position);

        viewHolder.progressupload.hideLoader();
        viewHolder.txtSent.setVisibility(View.GONE);
        if (model != null) {
            if (model.isUploading()) {
                UploadingModel tmp = new UploadingModel();
                tmp.setProgressHelper(viewHolder.progressupload);
                tmp.setVideoModel(model);
                tmp.setTxtSent(viewHolder.txtSent);
                mUploadingHelper.add(tmp);
                viewHolder.progressupload.startIndeterminateSpinner(true);
            }

            if (model.isSent()) {
                viewHolder.txtSent.setVisibility(View.VISIBLE);
            }

            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ", Locale.US);
                Date date = df.parse(model.getCreateDate());
                LogUtil.e(model.getCreateDate() + " realtime" + date.toGMTString());
                viewHolder.txtTime.setText(ConversionUtil.timeAgo(date));

            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // only on first one, to offset the it
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        viewHolder.gridWrapper.setLayoutParams(params);
        if (position == 1) {
            params.topMargin = (int) DipUtil.dipToPixels(context, 80);
            viewHolder.gridWrapper.setLayoutParams(params);
        }

        if (getItem(position) != null) {
            LogUtil.i("Loading Thumb: " + getItem(position).getThumbUrl());

            if (getItem(position).isRead()) {
                viewHolder.unreadCircle.setVisibility(View.GONE);
            } else {
                viewHolder.unreadCircle.setVisibility(View.VISIBLE);
            }

            if (getItem(position).getThumbUrl() != null && mImageFetcher != null) {
                Uri uri = Uri.parse(getItem(position).getThumbUrl());
                if (uri.getScheme() != null && uri.getScheme().equalsIgnoreCase("file")) {
                    viewHolder.videoThumbnail.setImageURI(uri);
                } else {
                    mImageFetcher.loadImage(getItem(position).getThumbUrl(), viewHolder.videoThumbnail);
                }
                // This is the part parsing out local files
            }
        }

        mViews.put(position, convertView);

        return convertView;
    }

    static class ViewHolder {
        ImageView videoThumbnail;
        ImageView unreadCircle;
        CustomVideoView videoPlayer;
        TextView txtTime;
        TextView txtSent;
        ProgressHelper progresshelper;
        ProgressHelper progressupload;
        RelativeLayout gridWrapper;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
}
