package com.example.imgchat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import static com.example.imgchat.MainActivity.recyclerView;

public class ChatActivity extends AppCompatActivity implements NetworkSocketService.Callbacks {
    private Button button_cam;


    NetworkSocketService nService;
    public ItemsAdapter mAdapter;
    byte [] imgArray;
    private ArrayList <ImageObject> mImgData= new ArrayList <>();
    private String host_address;
    private String nickname;
    boolean mBound = false;
    static final int REQUEST_TAKE_PHOTO  = 1;
    String currentPhotoPath;
    private String gpsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // This callback adds functionality to the back button so it closes connection when
        // returning to the login main activity
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                returnToLoginActivity();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        //get recycler view id
        recyclerView = findViewById(R.id.reyclerview_message_list);
        // use his setting is to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adapter creation
        mAdapter = new ItemsAdapter(mImgData,this);
        recyclerView.setAdapter(mAdapter);

        host_address = getIntent().getStringExtra("HOST_ADDRESS");
        nickname = getIntent().getStringExtra("USER_NICKNAME");

        button_cam = (Button) findViewById(R.id.camera_button);
        button_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    // Method that creates the three dots menu on this activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater findMenuItems = getMenuInflater();
        findMenuItems.inflate(R.menu.menu_scrolling, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                break;
            case R.id.action_about:
                Toast.makeText(this, "Developer: Lucian Panaite", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_exit:
                // Stops the Service intent we use for sockets connection and exit
                closeApp();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void returnToLoginActivity()
    {
        // Stops the Service intent we use for sockets connection
        Intent intent = new Intent(getApplicationContext(), NetworkSocketService.class);
        nService.closeSocketConnection();
        stopService(intent);
        mImgData.clear();
        Toast.makeText(this, "Connection to server was closed", Toast.LENGTH_SHORT).show();
        finish();
    }
    @Override
    public void returnToMainNoConnection()
    {
        // Stops the Service intent we use for sockets connection
        Intent intent = new Intent(getApplicationContext(), NetworkSocketService.class);
        stopService(intent);
        mImgData.clear();
        finish();
    }
    /************************ Start Bind service to activity code *********************************/
    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, NetworkSocketService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound)
        {
            unbindService(connection);
            mBound = false;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            NetworkSocketService.LocalBinder binder = (NetworkSocketService.LocalBinder) service;
            nService = binder.getService();
            mBound = true;
            nService.registerClient(ChatActivity.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }


    };

    //@Override
    public void closeApp()
    {
        Intent intent = new Intent(getApplicationContext(), NetworkSocketService.class);
        nService.closeSocketConnection();
        stopService(intent);
        mImgData.clear();
        Intent quitIntent = new Intent(this, MainActivity.class);
        quitIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        quitIntent.putExtra(MainActivity.SHOULD_FINISH, true);
        startActivity(quitIntent);
    }
    @Override
    public void updateChatUI(final byte [] byteArray, final byte [] nckname, final byte [] geolocationData)
    {
        // we use run on uiThread so it can run on updateChatUi activity, or else it will show error
        // since it is invoked by another thread other than the UI holding one
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                //String gpsData = "Coords:";
                String gpsDataString = new String(geolocationData);
                String clientNickname = new String(nckname);
                // compare coming picture with the one sent to check if are the same,
                // this way we detect if it s sent by this client or another
                if (Arrays.equals(imgArray, byteArray))
                {
                    mImgData.add(new ImageObject(bitmap, clientNickname, 0, gpsDataString));
                }
                else
                {
                    mImgData.add(new ImageObject(bitmap, clientNickname, 1,gpsDataString));
                }
                mAdapter.updateList(mImgData);
                recyclerView.smoothScrollToPosition(mImgData.size()-1);
            }
        });
    }

 /*************************   END Bind service to activity code ***********************************/

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
               //
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.imgchat.fileprovider", photoFile);
               //mPhotoFile = photoFile;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }


        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            switch (requestCode) {
                case 1: {
                    if (resultCode == RESULT_OK) {
                        File file = new File(currentPhotoPath);
                        ExifInterface exifInterface = new ExifInterface(currentPhotoPath);
                        gpsData = getExifGeoData(exifInterface);
                        Bitmap bitmap = MediaStore.Images.Media
                                .getBitmap(getContentResolver(), Uri.fromFile(file));
                        bitmap = checkPhotoOrientation(bitmap, exifInterface);
                        bitmap = resize(bitmap, 512, 512);
                        if (bitmap != null) {
                            // convert bitmap to png and put it in stream
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            nService.sendOverSocket(stream, nickname, gpsData);
                            imgArray = stream.toByteArray();
                            stream.flush();
                        }
                    }
                    break;
                }
            }

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    protected String getExifGeoData(ExifInterface exifInterface)
    {
        String gpsData = "LAT: "+exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE)+"\n";
        gpsData+= "LONG :"+exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)+"\n";
        gpsData+= "ALT: "+ exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE)+"\n";
        return gpsData;
    }
    /*
        Checks photo orientation and rotates it to portrait mode if it's in wrong position
     */
    private Bitmap checkPhotoOrientation(Bitmap bitmap, ExifInterface ei)
    {
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    // Function to resize bitmap to fixed size while keeping aspect ratio
    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }
}

