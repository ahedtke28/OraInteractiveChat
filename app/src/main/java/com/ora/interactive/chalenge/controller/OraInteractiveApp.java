package com.ora.interactive.chalenge.controller;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ora.interactive.chalenge.beans.RegistrationErrorWrapper;
import com.ora.interactive.chalenge.beans.RegistrationResponse;
import com.ora.interactive.chalenge.beans.RoomChat;
import com.ora.interactive.chalenge.beans.RoomChatsResponse;
import com.ora.interactive.chalenge.greendao.ChatRoom;
import com.ora.interactive.chalenge.greendao.DaoMaster;
import com.ora.interactive.chalenge.greendao.DaoSession;
import com.ora.interactive.chalenge.interfaces.NotificationTask;
import com.ora.interactive.chalenge.network.LoadServices;
import com.ora.interactive.chalenge.network.Service;
import com.ora.interactive.chalenge.utilities.Utility;

import java.util.ArrayList;
import java.util.List;

public class OraInteractiveApp extends Application implements NotificationTask {
    private static String LOG = OraInteractiveApp.class.getName();

    private static OraInteractiveApp thiz;

    private SQLiteDatabase database;
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    private SharedPreferences mOIProfiles;

    public void onCreate() {
        super.onCreate();

        // Save this context
        thiz = this;

        // Load Data Base
        openSQLiteDatabase();

        // Load App Profiles
        mOIProfiles = PreferenceManager.getDefaultSharedPreferences(this);

        // Load initial services
        loadServices();
    }

    public Resources getAppResources() {
        return thiz.getResources();
    }

    public static OraInteractiveApp getApp() {
        return thiz;
    }


    public SQLiteDatabase openSQLiteDatabase() throws SQLiteException {
        if (database == null) {
            database = new DaoMaster.DevOpenHelper(this, Config.DATABASE_NAME,
                    null).getWritableDatabase();
        } else if (!database.isOpen()) {
            database = new DaoMaster.DevOpenHelper(this, Config.DATABASE_NAME,
                    null).getWritableDatabase();
        }
        return database;
    }

    public DaoSession getDAOSession() {
        DaoMaster dm = getDAOMaster();
        if (daoSession == null)
            daoSession = dm.newSession();
        return daoSession;
    }

    public DaoMaster getDAOMaster() {
        SQLiteDatabase d = openSQLiteDatabase();
        if (daoMaster == null)
            daoMaster = new DaoMaster(d);
        else if (daoMaster.getDatabase() != d)
            daoMaster = new DaoMaster(d);
        return daoMaster;
    }

    public SQLiteDatabase getDB() {
        return openSQLiteDatabase();
    }

    public void closeDB() {
        try {
            database.close();
        } catch (Exception e) {
            Log.i(LOG, "error when try to close the db(" + e + ")");
        } finally {
            database = null;
            daoMaster = null;
            daoSession = null;
        }
    }

    public SharedPreferences getOIProfiles() {
        return mOIProfiles;
    }

    public void loadServices() {
        Service[] services;

        if (Utility.readBooleanToProfile(Config.IS_USER_LOGGED, false)) {
            services = new Service[2];
            services[0] = getProfile();
            services[1] = getChatRooms();
            new LoadServices().loadOnExecutor(services);
        }
    }

    /**
     * Current User Profile
     */
    public Service getProfile() {
        Service mService = new Service();
        mService.setServiceCode(Config.GET_USER_CURRENT_CODE);
        mService.setServiceName(Config.PAG_USER_CURRENT);
        mService.setServiceType(Config.GET);
        mService.setHeaders(Utility.getJsonAccessAndAuthorization(Utility.
                readStringToProfile(Config.USER_TOKEN, "")));
        mService.setNotificationTask(this);
        return mService;
    }

    /**
     * Get Available Rooms
     */
    public Service getChatRooms() {
        Service mService = new Service();
        mService.setServiceCode(Config.GET_USER_CHATS_CODE);
        mService.setServiceName(Config.GAP_USER_CHATS);
        mService.setServiceType(Config.GET);
        mService.setHeaders(Utility.getJsonAccessAndAuthorization(Utility.
                readStringToProfile(Config.USER_TOKEN, "")));
        mService.setNotificationTask(this);
        return mService;
    }

    @Override
    public void completed(Service response) {
        switch(response.getServiceCode()) {
            case Config.GET_USER_CURRENT_CODE: {
                Object convert;
                RegistrationResponse rr;
                RegistrationErrorWrapper errorWrapper;

                try {
                    convert = Utility.parseJSON(response.getOutput(),
                            RegistrationResponse.class);
                    if (convert == null) {
                        convert = Utility.parseJSON(response.getOutput(),
                                RegistrationErrorWrapper.class);

                        // Error Query
                        if (convert != null) {
                            errorWrapper = (RegistrationErrorWrapper) convert;
                            Log.i(LOG, "error :: " + errorWrapper.getError().getMessage());
                        } else {
                            Log.i(LOG, "server not responded");
                        }
                    } else {
                        Log.i(LOG, "Successful call");

                        // Successful query
                        Utility.storeUserProfile((RegistrationResponse) convert);
                    }
                } catch (Exception e) {
                    Log.i(LOG, "error while parsing JSON :: " + e);
                }
            }
                break;
            case Config.GET_USER_CHATS_CODE: {
                Object convert;
                RoomChatsResponse rr;

                try {
                    convert = Utility.parseJSON(response.getOutput(),
                            RoomChatsResponse.class);
                    if (convert != null) {
                        Log.i(LOG, "Successful call");

                        // Successful query
                        rr = (RoomChatsResponse) convert;

                        // Update DataBase
                        ChatRoom rc;
                        List<ChatRoom> rooms = new ArrayList<>();
                        for (RoomChat chat : rr.getData()) {
                            rc = new ChatRoom();
                            rc.setKChatRoomId(chat.getId());
                            rc.setKUserId(chat.getUser_id());
                            rc.setKChatRoomName(chat.getName());
                            rooms.add(rc);
                        }

                        // Publish Changes to UI
                        if (rooms.size() > 0) {
                            Utility.storeChatRooms(rooms);
                            Utility.sendChatRoomsUpdated();
                        }
                    } else {
                        Log.i(LOG, "server not responded");
                    }
                } catch (Exception e) {
                    Log.i(LOG, "error while parsing JSON :: " + e);
                }
            }
                break;
        }
    }
}
