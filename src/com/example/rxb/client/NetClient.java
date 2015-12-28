package com.example.rxb.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

import javax.swing.JOptionPane;

import com.example.rxb.message.*;
import com.example.rxb.server.ServerListener;

public class NetClient {

	DatagramSocket ds = null;//一个UDP的socket
	ChatManager mc = null;
	String serverIP;//服务器ip
	int guestId;//用户id
	String guestName;//用户名

	public NetClient(ChatManager mc) {
		serverIP = JOptionPane.showInputDialog("请输入服务端IP：");
		this.mc = mc;
		try {
			
			//用客户端的端口创建了一个客户端的DatagramSocket
			ds = new DatagramSocket(ChatManager.UDPPort);
			
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		
		//一个用于UDP的新线程用来接收服务器发送来的消息
		new Thread(new UDPThread()).start();
	}

	public void send(Msg msg) {
		msg.send(ds, serverIP, ServerListener.UDP_PORT);
	}

	//一个客户端的UDP线程，用来接收服务器端的消息
	private class UDPThread implements Runnable {

		public void run() {
			while (ds != null) {
				
				byte[] buffer = new byte[1024];
				DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
				
				try {
					//接收服务器端传来的信息
					ds.receive(dp);
					System.out.println("从服务器端接收到一个DP");
					NetClient.this.parse(dp);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 通过服务器的中转的发送消息
	 * */
	public void sendMsgByServer(int id, String msg) {
		ChatMsg sMsg = new ChatMsg(mc.getId(), id, msg);
		sMsg.setName(mc.getName());
		send(sMsg);
	}
	
	/**
	 * 通过p2p打洞的方式发送消息
	 * */
	public void sendMsgByP2P(String ip,int port,String msg){
		
		SocketAddress target = new InetSocketAddress(ip, port);  
        
        String message = msg;  
        byte[] sendbuf = message.getBytes(); 
        
        DatagramPacket pack;
        try {
        	
        	pack = new DatagramPacket(sendbuf, sendbuf.length, target);
			ds.send(pack);
			
		} catch (IOException e) {

			e.printStackTrace();
		}  
	}
	
	/**
	 * 解析服务器传递过来的信息的格式
	 * */
	public void parse(DatagramPacket dp) {
		if (dp.getLength() != 0) {
			ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
			DataInputStream dis = new DataInputStream(bais);
			Msg msg = null;
			try {
				int msgType = dis.readInt();
				switch (msgType) {
				case Msg.ClientOnMsg:
					System.out.println("ClientOnMsg received");
					msg = new ClientOnMsg(this.mc);
					msg.parse(dis);
					break;
				case Msg.ClientOffMsg:
					System.out.println("A client offline");
					msg = new ClientOffMsg(this.mc);
					msg.parse(dis);
					break;
				case Msg.AskClientsMsg:
					System.out.println("Ask client counts");
					msg = new AskClientsMsg(this.mc);
					msg.parse(dis);
					break;
				case Msg.ChatMsg:
					msg = new ChatMsg(this.mc);
					msg.parse(dis);
					break;
				case Msg.MsgRequest:
					msg = new MsgRequest(this.mc);
					msg.parse(dis);
					break;
				case Msg.MsgResponse:
					msg = new MsgResponse(this.mc);
					msg.parse(dis);
					break;
				default:
					break;

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


}
