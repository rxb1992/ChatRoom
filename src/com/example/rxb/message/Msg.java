package com.example.rxb.message;

import java.io.DataInputStream;
import java.net.DatagramSocket;

//消息接口，定义的消息的类型，消息的发送，消息格式的解析
public interface Msg {
	public static final int ClientOnMsg = 1;
	public static final int ClientOffMsg = 2;
	public static final int AskClientsMsg = 3;
	public static final int ChatMsg = 4;
	public static final int MsgRequest = 5;
	public static final int MsgResponse = 6;
	
	public void send(DatagramSocket ds,String IP,int port);
	
	public void parse(DataInputStream dis);
}
