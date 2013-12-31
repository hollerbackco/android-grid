package com.moziy.hollerback.service.task;

import com.activeandroid.query.Select;
import com.activeandroid.util.Log;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.util.HBFileUtil;

public class VideoDownloadTask extends HttpDownloadTask {
    private static final String TAG = VideoDownloadTask.class.getSimpleName();

    public enum POST_TXN_OPS { // this decides whether to clear a transaction or not after this task has completed
        CLEAR, CLEAR_ON_SUCCESS, CLEAR_ON_FAILURE, DONT_CLEAR
    };

    private String mVideoId;
    private POST_TXN_OPS mPostTransactionOp = POST_TXN_OPS.CLEAR; // default operation is to clear the transacting flag

    public VideoDownloadTask(VideoModel model) { // default clears the transacting model after complete
        super(model.getFileUrl(), HBFileUtil.getOutputVideoFile(model));
        mVideoId = model.getVideoId(); // don't hold on to the model
        // mark video as downloading
        model.setState(VideoModel.ResourceState.DOWNLOADING);
        model.save(); // the transaction flag should be set prior in our case, it's in the VideoHelper class; in case of history it's in the convohistorydelegate
    }

    @Override
    public void run() {

        super.run(); // download it

        // depending on the status, now, mark the resource state correctly
        VideoModel model = new Select().from(VideoModel.class).where(ActiveRecordFields.C_VID_GUID + " = ?", mVideoId).executeSingle();
        if (isSuccess()) {
            Log.d(TAG, "marking video as ondisk");
            model.setState(VideoModel.ResourceState.ON_DISK);
        } else {
            Log.d(TAG, "marking video as pending download");
            model.setState(VideoModel.ResourceState.PENDING_DOWNLOAD);
        }

        handlePostTxnOp(model);

    }

    public void setPostTxnOps(POST_TXN_OPS transactionOp) {
        mPostTransactionOp = transactionOp;
    }

    private void handlePostTxnOp(VideoModel v) {
        switch (mPostTransactionOp) {
            case CLEAR:
                v.clearTransacting();
                break;
            case CLEAR_ON_SUCCESS:
                if (isSuccess())
                    v.clearTransacting();
                break;
            case CLEAR_ON_FAILURE:
                if (!isSuccess())
                    v.clearTransacting();
                break;
            case DONT_CLEAR:
                break;
            default:
                break;
        }

        v.save();
    }

    public String getVideoId() {
        return mVideoId;
    }
}
