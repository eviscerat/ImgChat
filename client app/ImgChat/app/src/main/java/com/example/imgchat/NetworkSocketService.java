package com.example.imgchat;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;


public class NetworkSocketService extends IntentService {
    // Address ov the server
    public String ADDRESS;

    // Creates a server object
    private Socket server;
    boolean isConnected = false;

    Callbacks chatActivit;
    private Context appContext;

    /******************* Binding code between service and ChatActivity ****************************/
    // Binder given to clients of service
    private final IBinder binder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */

    public class LocalBinder extends Binder {
        NetworkSocketService getService() {
            // Return this instance of LocalService so clients can call public methods
            return NetworkSocketService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity){
        this.chatActivit = (Callbacks)activity;
    }

    //callbacks interface for communication with service clients!
    public interface Callbacks{
         void updateChatUI(byte [] data, byte [] nickname, byte [] geolocationData);
         void returnToMainNoConnection();
    }

    /************************* Binding code end ***************************************************/



    @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
         ADDRESS = intent.getStringExtra("HOST_ADDRESS");
        appContext = getApplicationContext();
        return super.onStartCommand(intent,flags,startId);


    }
    /**
     * A constructor is required, and must call the super
     * constructor with a name for the worker thread.
     */
    public NetworkSocketService() {
        super("SocketWorkerThread");
    }

    void showToast(final String msg){
        if(null !=appContext){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run()
                {
                    Toast.makeText(appContext, msg, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

                try
                {
                    InetAddress serverAddr = InetAddress.getByName(ADDRESS);
                    server = new Socket(serverAddr, SocketHandler.instance.PORT);
                    showToast("Successfully connected to server");
                    isConnected=true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    isConnected=false;
                    Log.d("Error", "on Socket creation");
                    showToast("Could not connect to server");
                    chatActivit.returnToMainNoConnection();
                    return;
                }

                DataInputStream in =null;
        try
        {
            in = new DataInputStream(server.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Error", "on recieving socket stream!");
            showToast("Could not connect to server");
            chatActivit.returnToMainNoConnection();
            return;
        }
        while(isConnected)
                {
                    try
                    {
                        //read image
                        byte[] size = new byte[4];
                        in.read(size);
                        ByteBuffer wrapped = ByteBuffer.wrap(size);
                        int sz = wrapped.getInt();
                        byte[] buffer = new byte[sz];
                        in.readFully(buffer, 0, sz);
                        //read nickname
                        byte[] nameSize = new byte[4];
                        in.read(nameSize);
                        int nSize = ByteBuffer.wrap(nameSize).getInt();
                        byte[] nickname = new byte[nSize];
                        in.read(nickname);
                        //read gps data
                        byte[] gpsSizeData = new byte[4];
                        in.read(gpsSizeData);
                        int gpsDataSize = ByteBuffer.wrap(gpsSizeData).getInt();
                        byte[] gpsData = new byte[gpsDataSize];
                        in.read(gpsData);
                        //read ending byte
                        byte[] bline =new byte[1];
                        in.read(bline);

                        chatActivit.updateChatUI(buffer, nickname, gpsData);

                    }
                    catch ( Exception e)
                    {
                        isConnected= false;
                        Log.d("Error", "Reading data from server error");
                    }
                }

    }

    public void sendOverSocket(final ByteArrayOutputStream stream, final String nickname ,final String gpsData)
    {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                OutputStream out = null;
                try
                {
                    out = server.getOutputStream();
                }
                catch (Exception e)
                {
                    Log.d("Error", "Creating socket stream");
                }
                try
                {
                    DataOutputStream output = new DataOutputStream(out);
                    byte [] name  = nickname.getBytes();
                    byte [] nameSize = ByteBuffer.allocate(4).putInt(name.length).array();
                    int size = stream.toByteArray().length;
                    byte[] byteLength = ByteBuffer.allocate(4).putInt(size).array();

                    byte [] gpsDt = gpsData.getBytes();
                    byte [] gpsDataSize = ByteBuffer.allocate(4).putInt(gpsDt.length).array();

                    //sending data
                    //send image
                    output.write(byteLength);
                    output.flush();
                    output.write(stream.toByteArray(),0,size);
                    output.flush();
                    // send the nickname
                    output.write(nameSize);
                    output.flush();
                    output.write(name);
                    output.flush();
                    // send the gpsData
                    output.write(gpsDataSize);
                    output.flush();
                    output.write(gpsDt);
                    output.flush();
                    //send finishing byte
                    output.writeByte('\n');
                    output.flush();
                }
                catch (Exception e)
                {
                    isConnected= false;
                    Log.d("Error", "Sending data to server");
                }

            }
        });
        t1.start();

    }
    // close the connection to the server
    public  void closeSocketConnection()
    {
        try {
            isConnected = false;
            if (server.isConnected())
            {
                server.close();
            }
        } catch (IOException e) {
            Log.d("Error", "Could not close socket connection");
        }
    }
}
