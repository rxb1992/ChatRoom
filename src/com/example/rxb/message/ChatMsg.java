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

public class ChatMsg implements Msg {
	private int msgType = Msg.ChatMsg;
	private ChatManager mc;
	private String name;
	private String cMsg;
	private int userId;
	private int guestId;
	
	public ChatMsg(int userId,int guestId, String cMsg) {
		this.userId = userId;
		this.guestId = guestId;
		this.cMsg = cMsg;
	}
	public ChatMsg(ChatManager mc) {
		this.mc = mc;
	}
	

	public void parse(DataInputStream dis) {
		try {
			int userIdid = dis.readInt();
			int guestId = dis.readInt();
			
			if(guestId != mc.getId()) {
				return;
			} else {
				String name = dis.readUTF();
				String msg = dis.readUTF();
				mc.showChatMsg(userIdid,name,msg);
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
			dos.writeInt(userId);
			dos.writeInt(guestId);
			dos.writeUTF(name);
			dos.writeUTF(cMsg);
			
			
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
