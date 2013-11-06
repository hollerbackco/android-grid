package com.moziy.hollerback.model;

public class SMSContact {
    private String _name;
    private String _sms;

    public SMSContact(String name, String sms) {
        _name = name;
        _sms = sms;
    }

    public String getName() {
        return _name;
    }

    public String getSMS() {
        return _sms;
    }
}
