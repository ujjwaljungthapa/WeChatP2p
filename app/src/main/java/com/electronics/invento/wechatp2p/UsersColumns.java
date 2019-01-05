package com.electronics.invento.wechatp2p;

import android.provider.BaseColumns;

public class UsersColumns {
    public UsersColumns() {}

    public static final class UsersColumnsNames implements BaseColumns {
        public static final String USERS_TABLE_NAME = "we_chat_users_tbl_4442";
        public static final String COLUMN_USERS_NAME = "user_username";
        public static final String COLUMN_USERS_PHONE = "user_phone";
        public static final String COLUMN_USERS_BIO = "user_bio";
        public static final String COLUMN_USERS_PROFILE = "user_profile";
        public static final String COLUMN_USERS_USERID = "user_id";
        public static final String COLUMN_USERS_USERTYPE = "user_usertype";
    }
}