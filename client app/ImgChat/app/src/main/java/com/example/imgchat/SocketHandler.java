package com.example.imgchat;

import java.net.Socket;

public class SocketHandler {
    public static  final SocketHandler instance = new SocketHandler();
    /*
    Static Socket object so it can be passed and used in various activities.
    It can give you statically access to socket throughout the app
     */
    private static Socket socket;
    // hardcoded port number
    public static final int PORT = 5500 ;
    public static boolean isConnectable = false;
    /*
    Private constructor
     */
    private  SocketHandler()
    {}
    /*
    Method to get a Socket object
     */
    public static synchronized Socket getSocket() {
        return socket;
    }

    /*
    Method to set a Socket object
     */
    public static synchronized void setSocket(Socket socket) {
        SocketHandler.socket = socket;
    }
}

