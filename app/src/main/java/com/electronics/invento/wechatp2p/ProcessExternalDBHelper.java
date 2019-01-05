package com.electronics.invento.wechatp2p;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.electronics.invento.wechatp2p.ChatColumns.*;
import com.electronics.invento.wechatp2p.UsersColumns.*;

public class ProcessExternalDBHelper {
    private static final String DATABASE_NAME = "we_chat_db_444.db";
    private static final int DATABASE_VERSION = 1;

    private ExternalDbHelper ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDatabase;

    private static class ExternalDbHelper extends SQLiteOpenHelper {

        public ExternalDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            final String SQL_CREATE_CHAT_MESSAGELIST_TABLE = "CREATE TABLE "
                    + ChatColumnsNames.TABLE_NAME
                    + " ("
                    + ChatColumnsNames._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ChatColumnsNames.COLUMN_MESSAGE + " TEXT NOT NULL, "
                    + ChatColumnsNames.COLUMN_MESSAGE_ORDER + " INTEGER NOT NULL, "
                    + ChatColumnsNames.COLUMN_MESSAGE_TYPE + " TEXT NOT NULL, "
                    + ChatColumnsNames.COLUMN_MESSAGE_USERID + " TEXT NOT NULL, "
                    + ChatColumnsNames.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");";

            final String SQL_CREATE_USER_TABLE = "CREATE TABLE "
                    + UsersColumnsNames.USERS_TABLE_NAME
                    + " ("
                    + UsersColumnsNames._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + UsersColumnsNames.COLUMN_USERS_NAME + " TEXT NOT NULL, "
                    + UsersColumnsNames.COLUMN_USERS_PHONE + " TEXT, "
                    + UsersColumnsNames.COLUMN_USERS_BIO + " TEXT, "
                    + UsersColumnsNames.COLUMN_USERS_USERID + " TEXT NOT NULL, "
                    + UsersColumnsNames.COLUMN_USERS_USERTYPE + " TEXT NOT NULL, "
                    + UsersColumnsNames.COLUMN_USERS_PROFILE + " BLOB"
                    + ");";

            db.execSQL(SQL_CREATE_CHAT_MESSAGELIST_TABLE);
            db.execSQL(SQL_CREATE_USER_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + ChatColumnsNames.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + UsersColumnsNames.USERS_TABLE_NAME);
            onCreate(db);
        }
    }

    public ProcessExternalDBHelper(Context context) {
        ourContext = context;
    }

    public ProcessExternalDBHelper openRead() throws SQLException {
        ourHelper = new ExternalDbHelper(ourContext);
        ourDatabase = ourHelper.getReadableDatabase();
        return this;
    }

    public ProcessExternalDBHelper openWrite() throws SQLException {
        ourHelper = new ExternalDbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (ourHelper != null) {
            ourHelper.close();
        }
    }

    public List<Chats> findallChats(String senderId, String receiverId) {
        List<Chats> mChatsArrayLists = new ArrayList<>();

        //String[] columns = new String[]{ChatColumnsNames.COLUMN_MESSAGE};
        Cursor cursor = ourDatabase.query(ChatColumnsNames.TABLE_NAME,
                null, null, null, null, null, ChatColumnsNames.COLUMN_TIMESTAMP + " ASC");
        int iChat_id = cursor.getColumnIndex(ChatColumnsNames._ID);
        int iChat_Message = cursor.getColumnIndex(ChatColumnsNames.COLUMN_MESSAGE);
        int iChat_Type = cursor.getColumnIndex(ChatColumnsNames.COLUMN_MESSAGE_TYPE);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Chats chat = new Chats();
            chat.setMessage(cursor.getString(iChat_Message));
            chat.setId(cursor.getLong(iChat_id));
            chat.setType(cursor.getString(iChat_Type));

            mChatsArrayLists.add(chat);
        }

        cursor.close();
        return mChatsArrayLists;
    }

    public Users userDetail() {
        Users userdetail = null;

        String[] columns = new String[]{UsersColumnsNames.COLUMN_USERS_NAME, UsersColumnsNames.COLUMN_USERS_PHONE,
                UsersColumnsNames.COLUMN_USERS_BIO, UsersColumnsNames.COLUMN_USERS_USERID, UsersColumnsNames.COLUMN_USERS_PROFILE};
        Cursor cursor = ourDatabase.query(UsersColumnsNames.USERS_TABLE_NAME,
                columns, null, null, null, null, null);
        int iUser_username = cursor.getColumnIndex(UsersColumnsNames.COLUMN_USERS_NAME);
        int iUser_phone = cursor.getColumnIndex(UsersColumnsNames.COLUMN_USERS_PHONE);
        int iUser_bio = cursor.getColumnIndex(UsersColumnsNames.COLUMN_USERS_BIO);
        int iUser_userid = cursor.getColumnIndex(UsersColumnsNames.COLUMN_USERS_USERID);
        int iUser_profile = cursor.getColumnIndex(UsersColumnsNames.COLUMN_USERS_PROFILE);

        if (cursor.moveToFirst()) {
            userdetail = new Users();       //object of the class must be initialized first ok
            userdetail.setUsername(cursor.getString(iUser_username));
            userdetail.setPhone(cursor.getString(iUser_phone));
            userdetail.setBio(cursor.getString(iUser_bio));
            userdetail.setUserid(cursor.getString(iUser_userid));
            userdetail.setProfilebyte(cursor.getBlob(iUser_profile));
        }

        cursor.close();
        return userdetail;
    }

    public void addMessage(String message, String type, int mChatOrder, String userid) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatColumnsNames.COLUMN_MESSAGE, message);
        contentValues.put(ChatColumnsNames.COLUMN_MESSAGE_ORDER, mChatOrder);
        contentValues.put(ChatColumnsNames.COLUMN_MESSAGE_TYPE, type);
        contentValues.put(ChatColumnsNames.COLUMN_MESSAGE_USERID, userid);

        ourDatabase.insert(ChatColumnsNames.TABLE_NAME, null, contentValues);
        Log.d("CHAT", "addMessage(): MESSAGE ADDED" + message + "order" + mChatOrder + "type" + type);
        //ourDatabase.close();//#### not needed
    }

    public void insertProfileData(String username, String phone, String bio, String userid, byte[] mUploadBytes, String usertype) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(UsersColumnsNames.COLUMN_USERS_NAME, username);
        contentValues.put(UsersColumnsNames.COLUMN_USERS_PHONE, phone);
        contentValues.put(UsersColumnsNames.COLUMN_USERS_BIO, bio);
        contentValues.put(UsersColumnsNames.COLUMN_USERS_USERID, userid);
        contentValues.put(UsersColumnsNames.COLUMN_USERS_PROFILE, mUploadBytes);
        contentValues.put(UsersColumnsNames.COLUMN_USERS_USERTYPE, usertype);

        ourDatabase.insert(UsersColumnsNames.USERS_TABLE_NAME, null, contentValues);
    }

    public void deleteChatRow(List<Chats> mChatsArrayLists, int adapterPosition) {
        Chats chat = mChatsArrayLists.get(adapterPosition);
        Long id = chat.getId();

        ourDatabase.delete(ChatColumnsNames.TABLE_NAME,
                ChatColumnsNames._ID + "=" + id, null);
    }
}