package com.imgchat.ro;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class EchoThread extends Thread {
    protected Socket socket;
    protected boolean isConnected = false;
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	int clientId;
	Iterator<Integer> itr = Server.clientsMap.keySet().iterator();
	
    public EchoThread(Socket clientSocket, int id) {
        this.socket = clientSocket;
        this.isConnected = false;
        this.clientId = id;
    }

    public void run() {
        printlnSafe(dateFormat.format(date)
        		+ "| Device "+ socket.getInetAddress() +" port:" + socket.getPort() +" just connected.");
        DataInputStream in ;
        isConnected = true;
       
        while (isConnected) {
            try {
            	printlnSafe(dateFormat.format(date)+"| Ready to read data!");
            	in = new DataInputStream(socket.getInputStream());   
            	// read image
            	byte[] size = new byte[4];
            	in.read(size);
            	ByteBuffer wrapped = ByteBuffer.wrap(size);
            	int sz = wrapped.getInt();
            	if(sz == 0)
            	{
            		break;
            	}
            	byte[] buffer = new byte[sz];
            	in.readFully(buffer, 0, sz);
            	// in read name
            	byte[] nameSize = new byte[4];
            	in.read(nameSize);
            	int nSize = ByteBuffer.wrap(nameSize).getInt();
            	byte[] nickname = new byte[nSize];
            	in.read(nickname);
            	
            	//read gps data
            	byte[] gpsDataSize = new byte[4];
            	in.read(gpsDataSize);
            	int gSize = ByteBuffer.wrap(gpsDataSize).getInt();
            	byte[] gpsData = new byte[gSize];
            	in.read(gpsData);
            	
            	// in read finish byte
            	byte[] bline =new byte[1];
            	in.read(bline);
            	printlnSafe(dateFormat.format(date)+"| Finished reading = "+ sz);
                broadcastToClients(buffer, nickname, gpsData);
            } catch (IOException e) {
                isConnected = false;
                printlnSafe(Server.clientsMap.get(this.clientId).toString()+ " closed !!");
                synchronized (Server.clientsMap) {
   				Server.clientsMap.keySet().removeIf(key -> key == this.clientId);}
                for (Integer id: Server.clientsMap.keySet()){
		            String key = id.toString();
		            String value = Server.clientsMap.get(id).toString();  
		            printlnSafe(key + " " + value);  }
                return;
            }
            catch(Exception e)
            {
            	printlnSafe("Error when trying to read data from client!");
            }
        }
        printlnSafe(Server.clientsMap.get(this.clientId).toString()+ " closed !!");
		 synchronized (Server.clientsMap) {
		 Server.clientsMap.keySet().removeIf(key -> key == this.clientId);} 
        for (Integer id: Server.clientsMap.keySet()){
            String key = id.toString();
            String value = Server.clientsMap.get(id).toString();  
            printlnSafe(key + " " + value);  }
        return;
        
    }
    
    public void broadcastToClients(byte[] image, byte [] nickname, byte[] gpsData)
    {
    	synchronized (Server.clientsMap) 
        {
            Server.clientsMap.forEach((key, client) -> sendToClient (client, image, nickname, gpsData));
        }
    }
    public void sendToClient(Socket client, byte[] image, byte[] nickname, byte[] gpsData) 
    {
    	OutputStream out = null;
		try {
			out = client.getOutputStream();
		} catch (IOException e1) {
			printlnSafe("Error when trying to get stream out of client socket!");
		} 
		try {
			DataOutputStream output = new DataOutputStream(out);
			byte[] byteLength = new byte[4];
			byte[] nameLength = new byte[4];
            int size = image.length;
            int nSize = nickname.length;
            
            byteLength = ByteBuffer.allocate(4).putInt(size).array();
            nameLength = ByteBuffer.allocate(4).putInt(nSize).array();   
            
            int  gpsSize = gpsData.length;
            byte [] gpsDataSize = ByteBuffer.allocate(4).putInt(gpsSize).array();
            
            output.write(byteLength);
            output.flush();
            output.write(image,0,size);
            output.flush();
            // send nickname
            output.write(nameLength);
            output.flush();
            output.write(nickname,0,nSize);
            output.flush();
            // send the gpsData
            output.write(gpsDataSize);
            output.flush();
            output.write(gpsData, 0, gpsSize);
            output.flush();
            // send ending byte
            output.writeByte('\n');
            output.flush();
            printlnSafe(dateFormat.format(date)+"| Finished sending data back to client!");
		} catch (IOException e) {
			 isConnected = false;
			 printlnSafe(Server.clientsMap.get(this.clientId).toString()+ " closed !!");
			 synchronized (Server.clientsMap) {
			 Server.clientsMap.keySet().removeIf(key -> key == this.clientId);} 
			 for (Integer id: Server.clientsMap.keySet()){
		            String key = id.toString();
		            String value = Server.clientsMap.get(id).toString();  
		            printlnSafe(key + " " + value);  }
             return;
		}
    }
    
    // safe console output
    public void printlnSafe(String s) {
    	  synchronized (System.out) {
    	    System.out.println(s);
    	  }
    	}
}