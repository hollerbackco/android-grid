package com.moziy.hollerback.util.contacts;

import java.util.List;

import com.moziy.hollerback.model.Contact;

public interface ContactsInterface {

    public List<Contact> getDeviceContacts();

    public List<Contact> getHollerbackContacts();

    public boolean deviceContactsLoaded();

    public boolean hbContactsLoaded();
}
