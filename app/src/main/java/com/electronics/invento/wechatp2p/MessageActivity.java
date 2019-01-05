package com.electronics.invento.wechatp2p;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageActivity extends AppCompatActivity {
    TextView textView_message;
    private EditText editText_message;
    private Button button_send;

    private TextView textView_status;

    static final int MESSAGE_READ = 1;
    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private WifiChatBroadcastReceiver mWifiChatBroadcastReceiver;

    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        initialize();

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);
        //mWifiChatBroadcastReceiver = new WifiChatBroadcastReceiver(mWifiP2pManager, mChannel, this);

        initializeIntentFilter();

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first check for empty message
                if (!(editText_message.getText().toString().trim().isEmpty())) {
                    String msg = editText_message.getText().toString();
                    if (sendReceive != null) {      //below here i was getting null pointer exception
                        sendReceive.write(msg.getBytes());
                        Toast.makeText(MessageActivity.this, "Message Sent!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (mWifiP2pManager != null) {
                            mWifiP2pManager.requestConnectionInfo(mChannel, connectionInfoListener);
                        }
                        Toast.makeText(MessageActivity.this, "Message not sent. Try again!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(v, "Message cannot be empty", Snackbar.LENGTH_SHORT).show();
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

    private void initialize() {
        textView_message = findViewById(R.id.textView_message_message);
        textView_status = findViewById(R.id.textView_message_status);
        editText_message = findViewById(R.id.editText_message_message);
        button_send = findViewById(R.id.button_message_send);
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
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        private void write(byte[] bytes) {
            try {
                if (bytes != null) {
                    outputStream.write(bytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
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
                    textView_message.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    public class ClientClass extends Thread {
        Socket socket;
        String hostAddress;

        private ClientClass(InetAddress hostaddress) {
            hostAddress = hostaddress.getHostAddress();
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
                textView_status.setText("Host");
                serverClass = new ServerClass();
                serverClass.start();    //start thread
            } else if (info.groupFormed) {
                textView_status.setText("Client");
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();    //start thread
            }
        }
    };
}
