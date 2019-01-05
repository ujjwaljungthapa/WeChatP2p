package com.electronics.invento.wechatp2p;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.StringTokenizer;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    TextView textView_connectionStatus;
    private ListView listView_peers;
    private Button button_onOff, button_Discover;
    private CardView cv_goToChats, cv_goToAboutus, cv_goToEdit;

    private WifiManager wifiManager;

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private WifiBroadCastReceiver mWifiBroadcastReceiver;

    private IntentFilter mIntentFilter;

    private List<WifiP2pDevice> peersArrayList;
    private String[] deviceNameArray;
    private WifiP2pDevice[] deviceArray;

    /*private static final int MESSAGE_READ = 1;
    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    private String username, phone, bio, userid;
    private byte[] profile_image_byte;
    private String friendname, friendphone, friendbio, frienduserid;
    private byte[] friend_profile_image_byte;

    private boolean informationFound = false;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initialize();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        checkwifistate(); //check wifi and set wifi value in button through wifiManager

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);
        mWifiBroadcastReceiver = new WifiBroadCastReceiver(mWifiP2pManager, mChannel, this);

        initializeIntentFilter();

        button_onOff.setOnClickListener(this);
        button_Discover.setOnClickListener(this);

        cv_goToChats.setOnClickListener(this);
        cv_goToEdit.setOnClickListener(this);
        cv_goToAboutus.setOnClickListener(this);

        listView_peers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WifiP2pDevice device = deviceArray[position];
                WifiP2pConfig config = new WifiP2pConfig();

                config.deviceAddress = device.deviceAddress;

                mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(HomeActivity.this, "Success! Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                        //###
                        //SendUserInformation();//dont use here since peer connection not started
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(HomeActivity.this, "Not Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /*register the broadcast receiver with the intent values to be matched*/
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mWifiBroadcastReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mWifiBroadcastReceiver);
    }

    private void initialize() {
        button_onOff = findViewById(R.id.button_home_onOff);
        button_Discover = findViewById(R.id.button_home_discover);
        textView_connectionStatus = findViewById(R.id.textView_home_conStatus);
        listView_peers = findViewById(R.id.listView_home_peers);

        cv_goToChats = findViewById(R.id.cv_home_goToChat);
        cv_goToEdit = findViewById(R.id.cv_home_goToEdit);
        cv_goToAboutus = findViewById(R.id.cv_home_goToAboutUs);

        peersArrayList = new ArrayList<>();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_home_onOff:
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    button_onOff.setText("Wifi On");
                } else {
                    wifiManager.setWifiEnabled(true);
                    button_onOff.setText("Wifi Off");
                }
                break;
            case R.id.button_home_discover:
                listView_peers.setVisibility(View.VISIBLE);
                mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        textView_connectionStatus.setText("Discovery Started!");
                    }

                    @Override
                    public void onFailure(int reason) {
                        textView_connectionStatus.setText("Discovery Starting Failed!");
                        listView_peers.setVisibility(View.GONE);
                    }
                });
                break;
            case R.id.cv_home_goToChat:
                Intent chatIntent = new Intent(HomeActivity.this, ChatActivity.class);
                //chatIntent.putExtra("finalfriendname", friendname);
                startActivity(chatIntent);
                break;
            case R.id.cv_home_goToEdit:
                Intent editIntent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(editIntent);
                break;
            case R.id.cv_home_goToAboutUs:
                Intent aboutIntent = new Intent(HomeActivity.this, AboutUsActivity.class);
                startActivity(aboutIntent);
                break;
        }
    }

    private void checkwifistate() {
        if (wifiManager.isWifiEnabled()) {
            button_onOff.setText("Wifi Off");
        } else {
            button_onOff.setText("Wifi On");
        }
    }

    public void resetData() {
        if (peersArrayList != null) {
            peersArrayList.clear();
        }
    }

    /*public class ServerClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8080);
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

        public SendReceive(Socket skt) {
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

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String message = new String(readBuff, 0, msg.arg1);
                    //#####read_msg_box.setText(tempMsg);
                    if (!informationFound) {
                        if (message.equals("OHKKK")) {
                            String temp = "RECEIVE";
                            sendReceive.write(temp.getBytes());
                        } else {
                            if (message.equals("RECEIVE")) {
                                SendUserInformation();
                                informationFound = true;
                            } else {
                                String temp = "OHKKK";
                                sendReceive.write(temp.getBytes());
                                AddFriendInfo(message);
                            }
                        }
                    }
                    break;
            }
            return true;
        }
    });

    public class ClientClass extends Thread {
        Socket socket;
        String hostAdd;

        public ClientClass(InetAddress hostAddress) {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8080), 500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerLists) {
            if (!peerLists.getDeviceList().equals(peersArrayList)) {
                peersArrayList.clear();
                peersArrayList.addAll(peerLists.getDeviceList());

                deviceNameArray = new String[peerLists.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerLists.getDeviceList().size()];

                int i = 0;
                for (WifiP2pDevice device : peerLists.getDeviceList()) {
                    deviceNameArray[i] = device.deviceName;
                    deviceArray[i] = device;
                    i++;
                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView_peers.setAdapter(arrayAdapter);
            }

            if (peersArrayList.size() == 0) {
                //hide list and show rocket
                Toast.makeText(HomeActivity.this, "No Device Found!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /*WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if (info.groupFormed && info.isGroupOwner) {
                //("Host")
                serverClass = new ServerClass();
                serverClass.start();            //start thread
                SendUserInformation();
            } else if (info.groupFormed) {
                //("Client")
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();        //start thread
            }
        }
    };

    // Send user information //###DO ON NEW THREAD??
    private void SendUserInformation() {
        //GetUserInformation();
        *//*String msg = username + "#" + phone + "#" + bio + "#" + userid;
        sendReceive.write(msg.getBytes());*//*

        GetUserInfoBackground getUserInfoBackground = new GetUserInfoBackground();
        getUserInfoBackground.execute();
    }

    public class GetUserInfoBackground extends AsyncTask<Void, Void, Boolean> {

        private GetUserInfoBackground() {
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            GetUserInformation();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            String msg = username + "#" + phone + "#" + bio + "#" + userid;
            //String msg = "johnny";
            sendReceive.write(msg.getBytes());
        }
    }

    private void GetUserInformation() {
        Users userdetail = new Users();
        try {
            ProcessExternalDBHelper testProcess = new ProcessExternalDBHelper(this);
            testProcess.openRead();
            userdetail = testProcess.userDetail();
            testProcess.close();

            username = userdetail.getUsername();
            phone = userdetail.getPhone();
            bio = userdetail.getBio();
            userid = userdetail.getUserid();
            if (userdetail.getProfilebyte() != null) {
                profile_image_byte = userdetail.getProfilebyte();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading user detail \n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void SendProfileBytes() {
        //insert friends data into database
    }

    private void AddFriendInfo(String message) {

        StringTokenizer tokens = new StringTokenizer(message, "#");
        friendname = tokens.nextToken();
        friendphone = tokens.nextToken();
        friendbio = tokens.nextToken();
        frienduserid = tokens.nextToken();

        AddFriendtoDatabase();
    }

    private void AddFriendtoDatabase() {
        try {
            ProcessExternalDBHelper insertProcess = new ProcessExternalDBHelper(this);
            insertProcess.openWrite();
            insertProcess.insertProfileData(friendname, friendphone, friendbio, frienduserid, null, "receiver");
            insertProcess.close();

            Toast.makeText(this, "Inserting data success", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            String error = e.toString();
            Toast.makeText(this, "InsertProfileData() error : \n" + error, Toast.LENGTH_LONG).show();
        }
    }*/
}