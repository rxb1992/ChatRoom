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

public class MsgResponse implements Msg {
	private int msgType = Msg.MsgResponse;
	private int userId;//要发送探测包的用户id
	private int guestId;//要接收探测包的用户id
	private ChatManager mc;
	
	public MsgResponse(int userid,int guestId) {
		this.userId = userid;
		this.guestId = guestId;

	} 
	public MsgResponse(ChatManager mc) {
		this.mc = mc;
	}

	public void parse(DataInputStream dis) {
		try {
			int recid = dis.readInt();//接收探测包的用户id
			int sendid = dis.readInt();//发送探测包的用户id
			String clientIP = dis.readUTF();//接收探测包的用户ip
			int clientPort = dis.readInt();//接收探测包的用户port
			
			if(sendid != this.mc.getId()) {
				return;
			} 
			
			System.out.println("收到服务器给请求机打洞的命令");	
			mc.handleResponse(clientIP,clientPort);
			
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
