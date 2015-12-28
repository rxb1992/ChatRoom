package com.example.rxb.message;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import com.example.rxb.client.*;


public class ClientOffMsg implements Msg {
	int msgType = Msg.ClientOffMsg;
	int id;
	String name;
	ChatManager mc;
	
	public ClientOffMsg(int id,String name) {
		this.id = id;
		this.name = name;
	}
	public ClientOffMsg(ChatManager mc) {
		this.mc = mc;
	}

	public void parse(DataInputStream dis) {
		try {
			int id = dis.readInt();
			if(id == mc.getId()) {
				return;
			} else {
				String name = dis.readUTF();
				mc.removeClient(id,name);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(DatagramSocket ds, String IP, int port) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		try {
			dos.writeInt(msgType);
			dos.writeInt(id);
			dos.writeUTF(name);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		byte[] buffer = baos.toByteArray();
		
		try {
			DatagramPacket dp = new DatagramPacket(buffer,buffer.length,new InetSocketAddress(IP,port));
			ds.send(dp);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
