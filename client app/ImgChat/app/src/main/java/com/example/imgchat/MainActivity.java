package com.example.imgchat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;



public class MainActivity extends FragmentActivity {

    private static  String host_address;
    public static RecyclerView recyclerView;
    String nickname;
    private Button button;
    public static final String SHOULD_FINISH = "should_finish";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getIntent().getBooleanExtra(SHOULD_FINISH, false)) {
            finish();
        }
        statusCheck();
        final EditText tAddress   = findViewById(R.id.tAddress);
        final EditText tNickname   = findViewById(R.id.tNickname);
        final TextView tWarning = findViewById(R.id.tWarning);
        button = findViewById(R.id.connect_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                //takes the values from the login page text fields
                if(!tNickname.getText().toString().isEmpty())
                {
                    nickname = tNickname.getText().toString();
                }
                else {
                    tNickname.setText("");
                    tWarning.setText("Enter a valid nickname");
                    tWarning.setVisibility(View.VISIBLE);
                    return;
                }
                if (validIP(tAddress.getText().toString())) {
                    host_address = tAddress.getText().toString();
                }
                else {
                    tAddress.setText("");
                    tWarning.setText("Wrong IP address !");
                    tWarning.setVisibility(View.VISIBLE);
                    return;
                }

                //Creates the intent towards the ServiceIntent created by me
                Intent intent = new Intent(getApplicationContext(), NetworkSocketService.class);
                // Pass host to the Service intent
                intent.putExtra("HOST_ADDRESS", host_address);
                //starts the service by calling the onStartCommand()
                startService(intent);
                openChatActivity();

            }
        });
    }

    private void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isGpsEnabledDialog();

        }
    }
    /* Function to create a dialog for the user to activate GPS on app start */
    private void isGpsEnabledDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS is disabled, in order for this app to work properly you need to enable it.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void openChatActivity ()
    {
            Intent intent = new Intent (this, ChatActivity.class);
            intent.putExtra("HOST_ADDRESS", host_address);
            intent.putExtra("USER_NICKNAME", nickname);
            startActivity(intent);

    }

    public static boolean validIP (String ip) {
        try {
            if ( ip == null || ip.isEmpty() ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
