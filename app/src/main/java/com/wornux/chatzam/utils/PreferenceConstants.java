package com.wornux.chatzam.utils;

public final class PreferenceConstants {
    
    public static final String PREFERENCE_FILE_NAME = "chatzam_preferences";
    
    public static final String KEY_PUSH_NOTIFICATIONS = "push_notifications";
    public static final String KEY_MESSAGE_SOUNDS = "message_sounds";
    public static final String KEY_SHOW_ONLINE_STATUS = "show_online_status";
    public static final String KEY_READ_RECEIPTS = "read_receipts";
    
    public static final boolean DEFAULT_PUSH_NOTIFICATIONS = true;
    public static final boolean DEFAULT_MESSAGE_SOUNDS = true;
    public static final boolean DEFAULT_SHOW_ONLINE_STATUS = true;
    public static final boolean DEFAULT_READ_RECEIPTS = true;
    
    private PreferenceConstants() {
        //not required
    }
}