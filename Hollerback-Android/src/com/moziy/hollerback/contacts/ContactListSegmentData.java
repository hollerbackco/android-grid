package com.moziy.hollerback.contacts;

import java.util.List;

import com.moziy.hollerback.model.Contact;

/**
 * A segment of a list
 * @author sajjad
 *
 */
public class ContactListSegmentData {
    public String mSegmentTitle;
    public List<Contact> mContacts;
    public List<ContactListSegmentData> mChildSegments;
}
