package com.moziy.hollerback.model;

public class Sender {

    private String mSender;
    private long mConvoId;

    public Sender(VideoModel v) {
        mSender = v.getSenderName();
        mConvoId = v.getConversationId();
    }

    public String getSenderName() {
        return mSender;
    }

    public long getConversationId() {
        return mConvoId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (mConvoId ^ (mConvoId >>> 32));
        result = prime * result + ((mSender == null) ? 0 : mSender.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Sender other = (Sender) obj;
        if (mConvoId != other.mConvoId)
            return false;
        if (mSender == null) {
            if (other.mSender != null)
                return false;
        } else if (!mSender.equals(other.mSender))
            return false;
        return true;
    }

}
