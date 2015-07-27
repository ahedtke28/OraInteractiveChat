package com.ora.interactive.chalenge.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;

import com.ora.interactive.chalenge.R;
import com.ora.interactive.chalenge.adapters.ChatAdapter;
import com.ora.interactive.chalenge.beans.ChatMessageResponse;
import com.ora.interactive.chalenge.beans.ChatMessagesResponse;
import com.ora.interactive.chalenge.beans.Message;
import com.ora.interactive.chalenge.controller.Config;
import com.ora.interactive.chalenge.greendao.ChatMessage;
import com.ora.interactive.chalenge.greendao.ChatRoom;
import com.ora.interactive.chalenge.interfaces.NotificationTask;
import com.ora.interactive.chalenge.network.LoadServices;
import com.ora.interactive.chalenge.network.Service;
import com.ora.interactive.chalenge.utilities.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity implements NotificationTask {
    ChatRoom room;
    @Bind(R.id.chats)
    ListView chats;
    @Bind(R.id.chatMsg)
    EditText chatMsg;

    ChatAdapter mAdapter;
    ArrayList<ChatMessage> list = new ArrayList<>();
    boolean isProcessing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        loadUI1();
        if (room == null) {
            finish();
            return;
        }
        showGlobalContextActionBar(room.getKChatRoomName());
        loadUI2();
        fillBuffer();
        getMessagesByChatId();
    }

    protected void loadUI1() {
        loadToolBar();
        setUpActionBar();
        loadChatRoom();
    }

    protected void loadUI2() {
        mAdapter = new ChatAdapter(this, R.layout.chat_local_item, list);
        chats.setAdapter(mAdapter);
    }

    protected void loadToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    public void loadChatRoom() {
        int id = getIntent().getIntExtra(Config.CHAT_ID, -1);
        room = Utility.getChatRoomById(id);
    }

    protected void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @OnClick({ R.id.sendMsg })
    public void onClickView(View view) {
        switch(view.getId()) {
            case R.id.sendMsg:
                String msg = chatMsg.getText().toString().trim();
                if (msg.equals("")) return;
                Map map = new HashMap();
                map.put("message", msg);
                addMessage(map);
                break;
        }
    }

    protected void addMessage(Map message) {
        if (isProcessing) return;

        Service mService = new Service();
        mService.setServiceCode(Config.POST_USER_CHAT_BY_ID);
        mService.setServiceName(Config.GAP_USER_CHATS + "/" + room.getKChatRoomId()
                + Config.GAP_USER_CHAT_BY_ID);
        mService.setServiceType(Config.POST);
        mService.setHeaders(Utility.getJsonAccessAndAuthorization(Utility.
                readStringToProfile(Config.USER_TOKEN, "")));
        mService.setServiceInput(message);
        mService.setNotificationTask(this);
        new LoadServices().loadOnExecutor(mService);

        isProcessing = true;
        openIndeterminateBar(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showGlobalContextActionBar(String title) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }

    protected void getMessagesByChatId() {
        if (isProcessing) return;

        Service mService = new Service();
        mService.setServiceCode(Config.GET_USER_CHAT_BY_ID);
        mService.setServiceName(Config.GAP_USER_CHATS + "/" + room.getKChatRoomId()
                + Config.GAP_USER_CHAT_BY_ID);
        mService.setServiceType(Config.GET);
        mService.setHeaders(Utility.getJsonAccessAndAuthorization(Utility.
                readStringToProfile(Config.USER_TOKEN, "")));
        mService.setNotificationTask(this);
        new LoadServices().loadOnExecutor(mService);

        isProcessing = true;
        openIndeterminateBar(true);
    }


    @Override
    public void completed(Service response) {
        isProcessing = false;
        openIndeterminateBar(false);

        switch(response.getServiceCode()) {

            case Config.GET_USER_CHAT_BY_ID: {
                Object convert;
                ChatMessagesResponse rr;

                try {
                    convert = Utility.parseJSON(response.getOutput(),
                            ChatMessagesResponse.class);
                    if (convert == null) {
                        Utility.showToast("server not responded");
                    } else {
                        rr = (ChatMessagesResponse)convert;
                        if (rr.isSuccess()) {
                            fillBuffer(rr.getData());
                        }
                    }
                } catch (Exception e) {
                    Utility.showToast("server not responded");
                }
            }
            break;
            case Config.POST_USER_CHAT_BY_ID:
                Object convert;
                ChatMessageResponse rr;

                try {
                    convert = Utility.parseJSON(response.getOutput(),
                            ChatMessageResponse.class);
                    if (convert == null) {
                        Utility.showToast("server not responded");
                    } else {
                        rr = (ChatMessageResponse)convert;
                        if (rr.isSuccess()) {
                            fillMessage(rr.getData());
                            chatMsg.setText("");
                        }
                    }
                } catch (Exception e) {
                    Utility.showToast("server not responded");
                }
                break;
            default:
                break;
        }
    }

    protected void fillBuffer(ArrayList<Message> messages) {
        List<ChatMessage> bufferMessages = null;
        ChatMessage dbMessage;

        if (messages != null && messages.size() > 0) {
            bufferMessages = new ArrayList<>();

            for (Message message : messages) {
                dbMessage = new ChatMessage();
                dbMessage.setKChatId(room.getKChatRoomId());
                dbMessage.setKMessage(message.getMessage());
                dbMessage.setKMessageId(message.getId());
                dbMessage.setKUserName(message.getUser().getName());
                dbMessage.setKSenderId(message.getUser_id());
                bufferMessages.add(dbMessage);
            }

            Utility.fillChatMessagesByChatId(bufferMessages,
                    bufferMessages.get(0).getKChatId());

            fillBuffer();
        }
    }

    protected void fillMessage(Message message) {
        ChatMessage dbMessage;

        dbMessage = new ChatMessage();
        dbMessage.setKChatId(room.getKChatRoomId());
        dbMessage.setKMessage(message.getMessage());
        dbMessage.setKMessageId(message.getId());
        dbMessage.setKUserName(message.getUser().getName());
        dbMessage.setKSenderId(message.getUser_id());

        Utility.fillChatMessageByChatId(dbMessage);

        fillBuffer();
    }

    protected void fillBuffer() {
        mAdapter.clear();
        mAdapter.addAll(Utility.getChatMessagesByChatId(
                room.getKChatRoomId()));
    }

    public void openIndeterminateBar(boolean open) {
        setProgressBarIndeterminateVisibility(open);
    }
}
