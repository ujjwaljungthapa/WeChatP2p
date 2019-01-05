package com.electronics.invento.wechatp2p;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editText_username, editText_phone, editText_bio;
    private Button button_save;
    private ImageView imageView_profile;
    private TextView textView_empty_username;

    private static final String usertype = "sender";
    final int REQUEST_CODE_GALLERY = 345;
    private byte[] mUploadBytes;
    private Uri resultUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initialize();

        imageView_profile.setOnClickListener(this);
        button_save.setOnClickListener(this);
    }

    private void initialize() {
        imageView_profile = findViewById(R.id.imageView_edit_profileImage);
        editText_username = findViewById(R.id.edittext_edit_username);
        editText_phone = findViewById(R.id.edittext_edit_phone);
        editText_bio = findViewById(R.id.edittext_edit_bio);
        textView_empty_username = findViewById(R.id.tv_empty_username);

        button_save = findViewById(R.id.button_edit_save);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView_edit_profileImage:
                //runtime permission for devices android 6.0 and above
                ActivityCompat.requestPermissions(
                        EditProfileActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
                break;
            case R.id.button_edit_save:
                if (mUploadBytes == null) {
                    Toast.makeText(this, "Adding image failed. Please choose image once again! ", Toast.LENGTH_SHORT).show();
                } else if (editText_phone.getText().toString().trim().isEmpty()) {
                    textView_empty_username.setVisibility(View.VISIBLE);
                } else {
                    textView_empty_username.setVisibility(View.GONE);
                    InsertProfileData();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //gallery intent
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY);
            } else {
                Toast.makeText(this, "Permission Denied. Cannot Access photos.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON) //enable image guideline
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                //set image choosen from gallery into imageView
                BackGroundImageResize resize = new BackGroundImageResize(null);
                resize.execute(resultUri);
                imageView_profile.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public class BackGroundImageResize extends AsyncTask<Uri, Integer, byte[]> {
        Bitmap mBitmap;

        private BackGroundImageResize(Bitmap bitmap) {
            if (bitmap != null) {
                this.mBitmap = bitmap;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected byte[] doInBackground(Uri... uris) {
            if (mBitmap == null) {
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uris[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            byte[] bytes = null;
            bytes = getBytesFromBitmap(mBitmap, 80);
            return bytes;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);

            mUploadBytes = bytes;
            Toast.makeText(EditProfileActivity.this, "Processing Image complete!", Toast.LENGTH_SHORT).show();
            //finally add image to database
            //InsertProfileData(mUploadBytes);
        }
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private void InsertProfileData() {
        String username, phone, bio, userid;

        userid = "useridxyz123";

        username = editText_username.getText().toString();
        phone = editText_phone.getText().toString();
        bio = editText_bio.getText().toString();

        try {
            ProcessExternalDBHelper insertProcess = new ProcessExternalDBHelper(this);
            insertProcess.openWrite();
            insertProcess.insertProfileData(username, phone, bio, userid, mUploadBytes, usertype);
            insertProcess.close();

            Toast.makeText(this, "Inserting data success", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            String error = e.toString();
            Toast.makeText(this, "InsertProfileData() error : \n" + error, Toast.LENGTH_LONG).show();
        }
    }

}
