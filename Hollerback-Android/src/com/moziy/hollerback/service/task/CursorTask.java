package com.moziy.hollerback.service.task;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class CursorTask extends AbsTask {

    private Cursor mCursor;
    private ContentResolver mContentResolver;
    private Uri mUri;
    private String[] mProjection;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mSortOrder;

    public CursorTask(ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        mContentResolver = resolver;
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;

    }

    @Override
    public void run() {
        mCursor = mContentResolver.query(mUri, mProjection, mSelection, mSelectionArgs, mSortOrder);

        mIsFinished = true;
        mIsSuccess = true;
    }

    public Cursor getCursor() {
        return mCursor;
    }

}
