package com.electronics.invento.wechatp2p;

import android.provider.BaseColumns;

public class ChatColumns {
    public ChatColumns() {}

    public static final class ChatColumnsNames implements BaseColumns {
        public static final String TABLE_NAME = "we_chat_tbl_4441";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_MESSAGE_ORDER = "message_order";  //don't use COLUMN_ORDER
        public static final String COLUMN_MESSAGE_TYPE = "message_type";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_MESSAGE_USERID = "user_id";
    }
}