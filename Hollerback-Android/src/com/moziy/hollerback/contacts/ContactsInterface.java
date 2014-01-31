package com.moziy.hollerback.contacts;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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

    public LOADING_STATE getFriendsLoadState();

    public LOADING_STATE getUnaddedFriendsLoadState();

    public List<Contact> getHBContactsExcludingFriends();

    public List<Contact> getRecentContacts();

    public List<Contact> getFriends();

    public List<Contact> getUnaddedFriends();

    public Set<Contact> getInviteList();

    public Contact getFriendByUsername(String username);

    public boolean hasFriend(String username);

    public Transaction beginTransaction();

    public boolean removeContactFrom(Contact contact, Collection<Contact> list);
}
