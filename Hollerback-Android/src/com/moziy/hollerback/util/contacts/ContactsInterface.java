package com.moziy.hollerback.util.contacts;

import java.util.List;

import com.moziy.hollerback.model.Contact;

public interface ContactsInterface {

    public enum LOADING_STATE {
        IDLE, LOADING, DONE, FAILED
    };

    public List<Contact> getDeviceContacts();

    public List<Contact> getHollerbackContacts();

    public LOADING_STATE getDeviceContactsLoadState();

    public LOADING_STATE getHbContactsLoadState();
}
