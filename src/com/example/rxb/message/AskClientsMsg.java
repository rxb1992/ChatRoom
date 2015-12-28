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

public class AskClientsMsg implements Msg {
	int msgType = Msg.AskClientsMsg;
	int id;
	ChatManager mc;
	
	public AskClientsMsg(int id) {
		this.id = id;
	}
	
	public AskClientsMsg(ChatManager mc) {
		this.mc = mc;
	}

	public void parse(DataInputStream dis) {
		try {
			
			int id = dis.readInt();//用户id
			String udpIP = dis.readUTF();//用户的udp的ip
			int udpPort = dis.readInt();//用户的udp的port
			int clientsCount = dis.readInt();//在线总人数
			
			this.mc.countClient(clientsCount);
			System.out.println("刷新在现人数："+clientsCount);
			for(int i=0; i<clientsCount; i++) {
				int clientId = dis.readInt();
				String clientName = dis.readUTF();
				
				this.mc.showClient(clientId, clientName);
				this.mc.addGuest(clientId, clientName,udpIP,udpPort);
				
			}
			System.out.println("客户端记录的总在线人数222："+this.mc.guestList.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(DatagramSocket ds, String IP, int port) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		try {
			dos.writeInt(msgType);//消息类型：这里是Msg.AskClientsMsg;
			dos.writeInt(id);//用户id
			
			
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
