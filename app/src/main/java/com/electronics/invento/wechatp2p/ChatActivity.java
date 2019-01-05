package com.electronics.invento.wechatp2p;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private TextView textView_receiverfullname;
    TextView textView_status;       //since accessed by broadcastreceiver class
    private EditText editText_message;
    ImageButton imageButton_send;
    private int mChatOrder = 0;

    private RecyclerView recyclerView_chatLists;
    private List<Chats> mChatsArrayList;
    private ChatAdapter mChatsAdapter;

    private String receiverId, senderId;

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private WifiChatBroadcastReceiver mWifiChatBroadcastReceiver;
    private IntentFilter mIntentFilter;

    static final int MESSAGE_READ = 1;
    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    //private String receiverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //receiverName = getIntent().getStringExtra("finalfriendname");

        initializeP2p();
        initialize();

        recyclerView_chatLists.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView_chatLists.setLayoutManager(linearLayoutManager);

        mChatsAdapter = new ChatAdapter(this, mChatsArrayList);
        recyclerView_chatLists.setAdapter(mChatsAdapter);

        ShowChatMessages();

        imageButton_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageButton_send.getTag().equals("offline")) {
                    Snackbar.make(v, "You cannot send message. User is offline.", Snackbar.LENGTH_SHORT).show();
                } else {
                    //first check for empty message
                    if (!(editText_message.getText().toString().trim().isEmpty())) {
                        String msg = editText_message.getText().toString();
                        if (sendReceive != null) {      //below here i was getting null pointer exception
                            sendReceive.sendMessage(msg.getBytes());
                            Toast.makeText(ChatActivity.this, "Message Sent!", Toast.LENGTH_SHORT).show();
                            AddSent(msg, "sender");
                        } else {
                            if (mWifiP2pManager != null) {
                                mWifiP2pManager.requestConnectionInfo(mChannel, connectionInfoListener);
                            }
                            Toast.makeText(ChatActivity.this, "Message not sent. Try again!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make(v, "Message cannot be empty", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mWifiChatBroadcastReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mWifiChatBroadcastReceiver);
    }

    private void initializeP2p() {
        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);
        mWifiChatBroadcastReceiver = new WifiChatBroadcastReceiver(mWifiP2pManager, mChannel, this);
        initializeIntentFilter();
    }

    private void initialize() {
        textView_receiverfullname = findViewById(R.id.textView_chat_username);
        textView_status = findViewById(R.id.textView_chat_status);

        editText_message = findViewById(R.id.editText_chat_messages);
        imageButton_send = findViewById(R.id.imageButton_chat_send);

        recyclerView_chatLists = findViewById(R.id.recyclerView_chat_list);
        mChatsArrayList = new ArrayList<>();
        senderId = "efgh";      //####
        receiverId = "abcd";    //###

        //textView_receiverfullname.setText(receiverName);
    }

    private void initializeIntentFilter() {
        mIntentFilter = new IntentFilter();
        // Indicates a change in the Wi-Fi P2P status.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void ShowChatMessages() {
        try {
            mChatsArrayList.clear();
            ProcessExternalDBHelper autoProcess = new ProcessExternalDBHelper(this);
            autoProcess.openRead();
            mChatsArrayList.addAll(autoProcess.findallChats(senderId, receiverId));
            autoProcess.close();

            mChatsAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            String error = e.toString();
            Toast.makeText(this, "ShowChatMessages() error : \n" + error, Toast.LENGTH_LONG).show();
        }
    }

    private void AddReceived(String message, String type) {
        try {
            ProcessExternalDBHelper addProcess = new ProcessExternalDBHelper(this);
            addProcess.openWrite();
            addProcess.addMessage(message, type, mChatOrder, "userid");
            mChatsArrayList.clear();
            mChatsArrayList.addAll(addProcess.findallChats(senderId, receiverId));
            addProcess.close();

            mChatsAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            String error = e.getMessage();
            Toast.makeText(this, "Add Received Chat Error : \n" + error, Toast.LENGTH_SHORT).show();
        }
    }

    private void AddSent(String message, String type) {
        try {
            ProcessExternalDBHelper addProcess = new ProcessExternalDBHelper(this);
            addProcess.openWrite();
            addProcess.addMessage(message, type, mChatOrder, "userid");
            mChatsArrayList.clear();
            mChatsArrayList.addAll(addProcess.findallChats(senderId, receiverId));
            addProcess.close();

            editText_message.getText().clear();
            mChatsAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            String error = e.getMessage();
            Toast.makeText(this, "Add Sent save Chat Error : /n" + error, Toast.LENGTH_LONG).show();
        }
    }

    public class ServerClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8686);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SendReceive extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        private SendReceive(Socket skt) {
            socket = skt;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        //###Received message
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //###Send message
        private void sendMessage(byte[] bytes) {
            if (bytes != null) {
                try {
                    outputStream.write(bytes);
                    Log.d("CHAT", "WRITE(): MESSAGE WRITTEN TO OUTPUT STREAM");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    Log.d("CHAT", "READ(): MESSAGE READ TO OUTPUT STREAM");
                    AddReceived(tempMsg, "receiver");
                    break;
            }
            return true;
        }
    });

    public class ClientClass extends Thread {
        Socket socket;
        String hostAddress;

        private ClientClass(InetAddress host_address) {
            hostAddress = host_address.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAddress, 8686), 500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if (info.groupFormed && info.isGroupOwner) {
                textView_status.setText("(Host)");
                serverClass = new ServerClass();
                serverClass.start();    //start thread
            } else if (info.groupFormed) {
                textView_status.setText("(Client)");
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();    //start thread
            }
        }
    };
}