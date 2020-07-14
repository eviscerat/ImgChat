package com.imgchat.ro;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
	
	static final int PORT = 5500;
	public static ConcurrentHashMap<Integer, Socket> clientsMap = new ConcurrentHashMap<>();
	public static void main(String[] args) {
		
		int counter = 0;
		ServerSocket serverSocket = null;
        Socket socket = null;
        InetAddress addr;
		try 
		{
			addr = InetAddress.getByName("0.0.0.0");
            serverSocket = new ServerSocket(PORT, 50, addr);
        } catch (IOException e) 
		{
            e.printStackTrace();
            System.exit(0);

        }
		
		System.out.println("Server started successfully!");
		System.out.println("Waiting for connections:");
        while (true) 
        {
            try 
            {
                socket = serverSocket.accept();
                clientsMap.put(counter, socket);
                for (Integer id: Server.clientsMap.keySet()){
		            String key = id.toString();
		            String value = Server.clientsMap.get(id).toString();  
		            System.out.println(key + " " + value);  }
            } 
            catch (IOException e) 
            {
                System.out.println("I/O error: " + e);
            }
            // new thread for a client set id and increment the counter
            new EchoThread(socket, counter++).start();      
        }
	}

}
