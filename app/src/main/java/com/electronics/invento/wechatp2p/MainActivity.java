package com.electronics.invento.wechatp2p;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageView_profile;
    private TextView textView_username, textView_phone, textView_bio;
    private Button button_show_friends;
    private ImageButton imageButton_edit;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        if (getIntent().hasExtra("type")) {
            String username = getIntent().getStringExtra("username");
            String phone = getIntent().getStringExtra("phone");
            String bio = getIntent().getStringExtra("bio");
            byte[] profile_image_byte = getIntent().getByteArrayExtra("byte_image");

            button_show_friends.setVisibility(View.GONE);
            imageButton_edit.setVisibility(View.GONE);

            showUsersDetail(username, phone, bio, profile_image_byte);
        } else {
            showDetail();
            button_show_friends.setOnClickListener(this);
            imageButton_edit.setOnClickListener(this);
        }
    }

    private void initialize() {
        imageView_profile = findViewById(R.id.imageView_main_profile);
        textView_username = findViewById(R.id.textView_main_username);
        textView_phone = findViewById(R.id.textView_main_phone);
        textView_bio = findViewById(R.id.textView_main_bio);

        button_show_friends = findViewById(R.id.button_main_show_friends);
        imageButton_edit = findViewById(R.id.imageButton_main_goto_edit);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_main_show_friends:
                //recyclerview showing friends list
                break;
            case R.id.imageButton_main_goto_edit:
                Intent editIntent = new Intent(MainActivity.this, EditProfileActivity.class);
                startActivity(editIntent);
                break;
        }
    }
    private void showUsersDetail(String username, String phone, String bio, byte[] profile_image_byte) {
        textView_username.setText(username);
        textView_phone.setText(phone);
        textView_bio.setText(bio);
        if (profile_image_byte != null) {
            convertToImage(profile_image_byte);
        }
    }

    private void showDetail() {
        Users userdetail = new Users();
        byte[] image;
        try {
            ProcessExternalDBHelper testProcess = new ProcessExternalDBHelper(this);
            testProcess.openRead();
            userdetail = testProcess.userDetail();
            testProcess.close();

            textView_username.setText("@" + userdetail.getUsername());
            textView_phone.setText("Phone: " + userdetail.getPhone());
            textView_bio.setText(userdetail.getBio());
            image = userdetail.getProfilebyte();
            if (image != null) {
                convertToImage(image);
            } else {
                Toast.makeText(this, "NO IMAGE!!!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading user detail \n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void convertToImage(byte[] image) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        imageView_profile.setImageBitmap(bitmap);
    }
}
