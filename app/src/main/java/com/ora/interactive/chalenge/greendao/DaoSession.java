package com.ora.interactive.chalenge.greendao;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.ora.interactive.chalenge.greendao.ChatRoom;
import com.ora.interactive.chalenge.greendao.ChatMessage;

import com.ora.interactive.chalenge.greendao.ChatRoomDao;
import com.ora.interactive.chalenge.greendao.ChatMessageDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig chatRoomDaoConfig;
    private final DaoConfig chatMessageDaoConfig;

    private final ChatRoomDao chatRoomDao;
    private final ChatMessageDao chatMessageDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        chatRoomDaoConfig = daoConfigMap.get(ChatRoomDao.class).clone();
        chatRoomDaoConfig.initIdentityScope(type);

        chatMessageDaoConfig = daoConfigMap.get(ChatMessageDao.class).clone();
        chatMessageDaoConfig.initIdentityScope(type);

        chatRoomDao = new ChatRoomDao(chatRoomDaoConfig, this);
        chatMessageDao = new ChatMessageDao(chatMessageDaoConfig, this);

        registerDao(ChatRoom.class, chatRoomDao);
        registerDao(ChatMessage.class, chatMessageDao);
    }
    
    public void clear() {
        chatRoomDaoConfig.getIdentityScope().clear();
        chatMessageDaoConfig.getIdentityScope().clear();
    }

    public ChatRoomDao getChatRoomDao() {
        return chatRoomDao;
    }

    public ChatMessageDao getChatMessageDao() {
        return chatMessageDao;
    }

}