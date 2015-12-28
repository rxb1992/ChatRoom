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

public class ClientOnMsg implements Msg {
	
	ChatManager mc = null;
	int msgType = Msg.ClientOnMsg;
	String name;
	int id;
	
	public ClientOnMsg(String name, int id) {
		this.name = name;
		this.id = id;
	}
	
	public ClientOnMsg(ChatManager mc) {
		this.mc = mc;
	}

	
	public void parse(DataInputStream dis) {
		try {
			int id = dis.readInt();
			if(id == mc.getId()) {
				return;
			} else {
//				String name = dis.readUTF();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(DatagramSocket ds,String IP,int port) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		try {
			dos.writeInt(msgType);//消息类型
			dos.writeInt(id);//用户id
			dos.writeUTF(name);//用户名
			
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
