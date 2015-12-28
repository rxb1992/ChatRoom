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

public class MsgRequest implements Msg {
	private int msgType = Msg.MsgRequest;
	private int userId;
	private int guestId;
	private ChatManager mc;
	
	public MsgRequest(int userid, int guestId) {
		this.userId = userid;
		this.guestId = guestId;
	}
	public MsgRequest(ChatManager mc) {
		this.mc = mc;
	}

	public void parse(DataInputStream dis) {
		try {
			
			int recid = dis.readInt();//接收探测包的用户id
			int sendid = dis.readInt();//发送探测包的用户id
			String clientIP = dis.readUTF();//接收探测包的用户ip
			int clientPort = dis.readInt();//接收探测包的用户port

			if(sendid != mc.getId()) {
				return;
			}
			
			System.out.println("收到服务器给请求机打洞的命令");
			mc.handleRequest(sendid,recid,clientIP,clientPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(DatagramSocket ds, String IP, int port) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		try {
			dos.writeInt(msgType);//消息类型
			dos.writeInt(userId);//本人id
			dos.writeInt(guestId);//对方id
			
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
