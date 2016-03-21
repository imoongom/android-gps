package com.datacomm.gpstrack;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by moongom on 3/10/2016.
 */
public class Network {
    static final int DgramSize = 1024;
    String ServerName;
    int ServerPort;
    String ClientString;
    byte[] PacketData;
    InetAddress Addr;
    DatagramSocket ClientSocket;
    DatagramPacket dgram;


    //network constructor. initialize server ip and port number
    public Network(String server, int port)  {
        this.ServerName = server;
        this.ServerPort = port;
    }

    //setup connection and prepare to send.
    public void connect ()
    {

        // Get the IP address of the Server
        //** get server information
        Log.d("CHECK SERVER IP", ServerName);
        try {
            Addr = InetAddress.getByName(ServerName);
            ClientSocket = new DatagramSocket();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        PacketData = new byte[DgramSize];

    }

    //Packetize and Send;
    public void send(String lat, String lng, String name) {
    //    ClientString = null;
        ClientString = new String("<marker name = \""+ name + "\" lat = \"" + lat + "\" lng =\""+ lng + "\"/>");
        ClientString = createPacket(lat, lng, name);
        System.arraycopy(ClientString.getBytes(), 0, PacketData, 0, ClientString.length());
        Log.e("SEND CHECK", ClientString);
        // Create the complete datagram
        dgram = new DatagramPacket(PacketData, PacketData.length, Addr, ServerPort);

        try {
            // Send the Datagram to the server
            String temp = new String("Sending Datagram to: " + ServerName + " on port " + ServerPort);
            Log.w("will be sendto", temp);

            ClientSocket.send(dgram);
        } catch (IOException ie) {
            System.out.println("Send Failure: " + ie.getMessage());
            System.exit(0);
        }
    }

    //create packet and call
    public String createPacket(String lat, String lng, String name){
       return new String("INSERT INTO `markers`(`name`, `lat`, `lng`) VALUES ('"+name+"',"+lat+","+lng+");");
    }

}
