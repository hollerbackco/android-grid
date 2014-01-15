package com.moziy.hollerback.contacts.data;

import java.util.List;

import com.moziy.hollerback.model.Contact;

/**
 * A segment of a list
 * @author sajjad
 *
 */
public class ContactListSegmentData {
    public String mTextPlaceHolderMsg;
    public String mSegmentTitle;
    public List<Contact> mContacts;
    public List<ContactListSegmentData> mChildSegments;
}