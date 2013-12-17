package com.moziy.hollerback.util;

public class HBPreferences {

    public static final String ACCESS_TOKEN = "Access Token";
    public static final String LAST_LOGIN = "Last Login";

    public static final String USERNAME = "User Name";
    public static final String PHONE = "Phone";
    public static final String REGION_CODE = "RegionCode";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String ID = "id";
    public static final String IS_VERIFIED = "is_verified";
    public static final String LAST_REGISTRATION_TIME = "last_registration_time";

    public static final String SAVED_EMAIL = "SavedEmail";

    public static final String GCM_ID = "GCM_ID";

    // type: string : last sync time
    public static final String LAST_SERVICE_SYNC_TIME = "last_service_sync_time";

    // type: long wallclock time for recovery alarm in millis
    public static final String RECOVERY_ALARM_TIME = "recovery_alarm_time";

    // type: long: millis in current alarm schedule
    public static final String RESOURCE_RECOVERY_BACKOFF_TIME = "resource_recovery_backoff_time";

    // type: boolean pending alarm for recovery
    public static final String PENDING_RECOVERY_ALARM = "pending_recovery_alarm";

    // type: boolean - to display start recording dialog or not
    public static final String SHOWN_START_RECORDING_DIALOG = "start_recording_dialog_shown";

    // type: Set<String> - Set of recovery client class names
    public static final String RECOVERY_CLIENTS = "recovery_client_set";

}
