package com.moziy.hollerback.contacts;

import java.util.List;

import com.moziy.hollerback.contacts.ContactsDelegate.Transaction;
import com.moziy.hollerback.model.Contact;

public interface ContactsInterface {

    public enum LOADING_STATE {
        IDLE, LOADING, DONE, FAILED
    };

    public List<Contact> getDeviceContacts();

    public List<Contact> getContactsExcludingHBContacts();

    public List<Contact> getHollerbackContacts();

    public LOADING_STATE getDeviceContactsLoadState();

    public LOADING_STATE getHbContactsLoadState();

    public List<Contact> getRecentContacts();

    public List<Contact> getFriends();

    public Transaction beginTransaction();

}