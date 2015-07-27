package com.ora.interactive.chalenge.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ora.interactive.chalenge.R;
import com.ora.interactive.chalenge.activities.ChatActivity;
import com.ora.interactive.chalenge.adapters.ChatRoomAdapter;
import com.ora.interactive.chalenge.beans.AddNewChatRoom;
import com.ora.interactive.chalenge.beans.RoomChat;
import com.ora.interactive.chalenge.beans.RoomChatsResponse;
import com.ora.interactive.chalenge.controller.Config;
import com.ora.interactive.chalenge.controller.OraInteractiveApp;
import com.ora.interactive.chalenge.dialogs.MyDialogFragment;
import com.ora.interactive.chalenge.greendao.ChatRoom;
import com.ora.interactive.chalenge.network.LoadServices;
import com.ora.interactive.chalenge.network.Service;
import com.ora.interactive.chalenge.utilities.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends FragmentBase implements AdapterView.OnItemClickListener,
        MyDialogFragment.MyDialogFragmentInterface {

    private static final String LOG = HomeFragment.class.getName();
    private ArrayList<ChatRoom> mChatRooms =  new ArrayList<>();
    private ChatRoomAdapter mChatRoomsAdapter;
    private ChatsRoomReceiver mReceiver;
    private IntentFilter mFilter;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_chat, container, false);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        receiver();
        ListView mLV = (ListView)getView();
        fillChatRooms();
        mChatRoomsAdapter = new ChatRoomAdapter(getActivity(), R.layout.chat_room_item,
                mChatRooms);
        mLV.setAdapter(mChatRoomsAdapter);
        mLV.setOnItemClickListener(this);
    }

    protected void receiver() {
        mReceiver = new ChatsRoomReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction(Config.CHAT_ROOMS_UPDATED);
    }

    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, mFilter);
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }

    private void fillChatRooms() {
        mChatRooms.clear();
        mChatRooms.addAll(Utility.getChatRooms());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mChatRooms =  null;
        mChatRoomsAdapter = null;
        mReceiver = null;
        mFilter = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int chatId = mChatRooms.get(position).getKChatRoomId();
        Intent activity = new Intent(getActivity(), ChatActivity.class);
        activity.putExtra(Config.CHAT_ID, chatId);
        startActivity(activity);
    }

    @Override
    public void ok(String name) {
        Log.i(LOG, "Chat Name :: " + name);
        addNewChatRoom(name);
    }

    private class ChatsRoomReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Config.CHAT_ROOMS_UPDATED)) {
                fillChatRooms();
                mChatRoomsAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home, menu);
        showGlobalContextActionBar(OraInteractiveApp.getApp().getResources()
                .getString(R.string.app_name));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_new_chat_room:
                MyDialogFragment dialog = MyDialogFragment.newDialog(this);
                dialog.show(getFragmentManager(), "");
                break;
            case R.id.action_exit:
                getActivity().finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addNewChatRoom(String name) {
        if (isProcessing) return;
        Map map = new HashMap();
        map.put("name", name);
        addNewChatRoom(map);
    }

    /**
     * Create a new Chat
     */
    public void addNewChatRoom(Map map) {
        Service service = new Service();
        service.setServiceCode(Config.POST_USER_CHATS_CODE);
        service.setServiceName(Config.GAP_USER_CHATS);
        service.setServiceType(Config.POST);
        service.setHeaders(Utility.getJsonAccessAndAuthorization(
                Utility.readStringToProfile(Config.USER_TOKEN, "")));
        service.setServiceInput(map);
        service.setNotificationTask(this);
        new LoadServices().loadOnExecutor(service);
    }

    @Override
    public void completed(Service response) {
        switch (response.getServiceCode()) {
            case Config.GET_USER_CHATS_CODE: {
                Object convert;
                RoomChatsResponse rr;

                try {
                    convert = Utility.parseJSON(response.getOutput(),
                            RoomChatsResponse.class);
                    if (convert != null) {
                        Utility.showToast("Successful call");

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
                        Utility.showToast("server not responded");
                    }
                } catch (Exception e) {
                    Utility.showToast("server not responded");
                }
            }
            break;
            case Config.POST_USER_CHATS_CODE: {
                Object convert;
                AddNewChatRoom rr;
                ChatRoom chat;

                try {
                    convert = Utility.parseJSON(response.getOutput(),
                            AddNewChatRoom.class);

                    if (convert != null) {
                        Utility.showToast("Successful call");

                        rr = (AddNewChatRoom)convert;

                        if (rr.isSuccess()) {
                            // Chat
                            chat = new ChatRoom();
                            chat.setKChatRoomId(rr.getData().getId());
                            chat.setKUserId(rr.getData().getUser_id());
                            chat.setKChatRoomName(rr.getData().getName());

                            // Store
                            Utility.storeChatRoom(chat);

                            // Update
                            fillChatRooms();
                        }
                    } else {
                        Utility.showToast("server not responded");
                    }
                } catch (Exception e) {
                    Utility.showToast("server not responded");
                }
            }
            break;
        }
    }
}
