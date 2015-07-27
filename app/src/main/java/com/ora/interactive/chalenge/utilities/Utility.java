package com.ora.interactive.chalenge.utilities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ora.interactive.chalenge.beans.RegistrationResponse;
import com.ora.interactive.chalenge.controller.Config;
import com.ora.interactive.chalenge.controller.OraInteractiveApp;
import com.ora.interactive.chalenge.greendao.ChatMessage;
import com.ora.interactive.chalenge.greendao.ChatMessageDao;
import com.ora.interactive.chalenge.greendao.ChatRoom;
import com.ora.interactive.chalenge.greendao.ChatRoomDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.dao.query.WhereCondition;

public class Utility {
    private static final String LOG = Utility.class.getName();

    /**
     * @param drawableId : resource id of the given drawable
     */
    public static Drawable getDrawableById(int drawableId) {

        Drawable myDrawable;
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
            myDrawable = OraInteractiveApp.getApp().getAppResources().getDrawable(drawableId,
                    OraInteractiveApp.getApp().getTheme());
        } else {
            myDrawable = OraInteractiveApp.getApp().getAppResources().getDrawable(drawableId);
        }

        return myDrawable;
    }

    /**
     * @param dp : Number of DP to convert
     */
    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                OraInteractiveApp.getApp().getAppResources().getDisplayMetrics());
    }

    /**
     * Get Chat Rooms available
     */
    public static List<ChatRoom> getChatRooms() {
        return OraInteractiveApp.getApp().getDAOSession().getChatRoomDao().loadAll();
    }

    /**
     * Store the new Chat Rooms
     * @param chatRoom Chat Room
     */
    public static void storeChatRoom(ChatRoom chatRoom) {
        ChatRoomDao dao = OraInteractiveApp.getApp().getDAOSession().getChatRoomDao();
        dao.insertInTx(chatRoom);
    }

    /**
     * Store the new Chat Rooms
     * @param chatRooms Chat Rooms to Store
     */
    public static void storeChatRooms(List<ChatRoom> chatRooms) {
        ChatRoomDao dao = OraInteractiveApp.getApp().getDAOSession().getChatRoomDao();
        dao.deleteAll();
        dao.insertInTx(chatRooms);
    }

    /**
     * Get Chat Room By Id
     * @param id Id of the Chat Room
     */
    public static ChatRoom getChatRoomById(int id) {
        ChatRoomDao dao = OraInteractiveApp.getApp().getDAOSession().getChatRoomDao();

        WhereCondition where = ChatRoomDao.Properties.KChatRoomId.eq(id);
        List<ChatRoom> rooms = dao.queryBuilder().where(where).list();
        if (rooms.size() > 0)
            return rooms.get(0);
        return null;
    }


    /**
     * Get all messages per session
     * @param chatId : Session Id of the chat
     */
    public static List<ChatMessage> getChatMessagesByChatId(int chatId) {

        ChatMessageDao dao = OraInteractiveApp.getApp().getDAOSession().getChatMessageDao();

        WhereCondition where = ChatMessageDao.Properties.KChatId.eq(chatId);

        List<ChatMessage> list = dao.queryBuilder().where(where)
                .orderAsc(ChatMessageDao.Properties.KMessageId)
                .list();

        return list;
    }

    /**
     * Get all messages per session
     * @param messages Messages to store
     * @param chatId : Chat Id
     */
    public static void fillChatMessagesByChatId(List<ChatMessage> messages, int chatId) {
        ChatMessageDao dao = OraInteractiveApp.getApp().getDAOSession().getChatMessageDao();
        WhereCondition where = ChatMessageDao.Properties.KChatId.eq(chatId);
        List<ChatMessage> list = dao.queryBuilder().where(where).list();
        dao.deleteInTx(list);
        dao.insertInTx(messages);
    }

    public static void fillChatMessageByChatId(ChatMessage message) {
        ChatMessageDao dao = OraInteractiveApp.getApp().getDAOSession().getChatMessageDao();
        dao.insert(message);
    }


    public static void writeStringToProfile(String key, String value) {
        SharedPreferences.Editor editor = OraInteractiveApp.getApp()
                .getOIProfiles().edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void writeIntToProfile(String key, int value) {
        SharedPreferences.Editor editor = OraInteractiveApp.getApp()
                .getOIProfiles().edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void writeBooleanToProfile(String key, boolean value) {
        SharedPreferences.Editor editor = OraInteractiveApp.getApp()
                .getOIProfiles().edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static String readStringToProfile(String key, String defvalue) {
        return OraInteractiveApp.getApp().getOIProfiles().getString(key, defvalue);
    }

    public static int readIntToProfile(String key, int defvalue) {
        return OraInteractiveApp.getApp().getOIProfiles().getInt(key, defvalue);
    }

    public static boolean readBooleanToProfile(String key, boolean defvalue) {
        return OraInteractiveApp.getApp().getOIProfiles().getBoolean(key, defvalue);
    }


    /**
     * Parses JSON String and transforms it to the desired type
     * @param json : Json String
     * @param type : Type of Object to generate
     */
    public static Object parseJSON(String json, Class<?> type) {
        Object r = null;
        try {
            r = new Gson().fromJson(json, type);
        } catch (Exception e) {
            Log.i(LOG, "error while parsing :: " + e);
        }
        return r;
    }

    /**
     * Stores user profile
     * @param userProfile : USer Profile to save
     */
    public static void storeUserProfile(RegistrationResponse userProfile) {
        writeStringToProfile(Config.USER_EMAIL,
                userProfile.getData().getEmail());
        writeStringToProfile(Config.FIRST_NAME,
                userProfile.getData().getName());
        writeIntToProfile(Config.USER_ID,
                userProfile.getData().getId());
        writeStringToProfile(Config.USER_TOKEN,
                userProfile.getData().getToken());
        writeBooleanToProfile(Config.IS_USER_LOGGED, true);
    }

    /**
     * Deletes user profile
     */
    public static void deleteUserProfile() {
        SharedPreferences.Editor editor = OraInteractiveApp.getApp().getOIProfiles().edit();
        editor.remove(Config.USER_EMAIL);
        editor.remove(Config.FIRST_NAME);
        editor.remove(Config.USER_ID);
        editor.remove(Config.USER_TOKEN);
        editor.remove(Config.IS_USER_LOGGED);
        editor.commit();
    }

    public static Map<String, String> getJsonAccess() {
        Map<String, String> map = new HashMap<>();
        map.put("Accept", "application/json");
        return map;
    }

    public static Map<String, String> getJsonAccessAndAuthorization(String auth) {
        Map<String, String> map = new HashMap<>();
        map.put("Accept", "application/json");
        map.put("Authorization", auth);
        return map;
    }

    public static void sendUserInitiatedSession() {
        Intent sessionStarted = new Intent();
        sessionStarted.setAction(Config.SESSION_APP);
        OraInteractiveApp.getApp().sendBroadcast(sessionStarted);
    }

    public static void sendUserLogout() {
        Intent sessionStarted = new Intent();
        sessionStarted.setAction(Config.NO_SESSION_APP);
        OraInteractiveApp.getApp().sendBroadcast(sessionStarted);
    }

    public static void sendChatRoomsUpdated() {
        Intent sessionStarted = new Intent();
        sessionStarted.setAction(Config.CHAT_ROOMS_UPDATED);
        OraInteractiveApp.getApp().sendBroadcast(sessionStarted);
    }

    public static void showToast(String message) {
        Toast toast = Toast.makeText(OraInteractiveApp.getApp(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
