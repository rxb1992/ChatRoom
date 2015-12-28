package com.example.rxb.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.example.rxb.message.*;

public class ServerListener {
	
	public static int ID = 100;
	private Vector<Client> clients = new Vector<Client>();//客户端的集合
	public ServerSocket ss = null;
	public static final int TCP_PORT = 8888;//服务器端TCP连接端口号
	public static final int UDP_PORT = 6666;//服务器端UDP连接端口号
	
	private DatagramPacket dp = null;
	
	//发给客户端的输出流
	private ByteArrayOutputStream baos = null;
	private DataOutputStream dos = null;  
	
	//接收到客户端输入的流
	private ByteArrayInputStream bais = null;
	private DataInputStream dis = null;
				
	private int msgType = 0;//获取消息类型
	private int userid = 0;//请求方的id
	private String userIP;//连接用户的ip
	private int userUDP_Port = 0;//连接用户的port
	
	private byte[] buffer = new byte[1024];//消息缓冲区
	
	public ServerListener() {

		try {
			//开启一个TCP连接，用于处理登陆
			ss = new ServerSocket(TCP_PORT);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 建立连接
	 * 该方法创建两个链接
	 * 一个TCP连接：用来登陆
	 * 一个UDP连接：用来接收消息
	 * 
	 * */
	public void connect() {

		//每连接一个新的客户端都开启一个UDP的线程用接收聊天消息
		new Thread(new UDPThread()).start();

		//下面的都是TCP的连接，只是用来连接用，获取IP，port,用户id,用户名
		Socket s = null;
		while (true) {
			if (ss == null || ss.isClosed()) {
				System.exit(0);
				return;
			}
			try {
				
				//接收一个来自客户端的TCP请求（登陆的请求）
				s = ss.accept();
				
				userIP = s.getInetAddress().getHostAddress();//获取客户机ip
				int clientTCP_Port = s.getPort();//获取客户端的TCP端口号
				DataInputStream dis = new DataInputStream(s.getInputStream());

				/*
				 * 这里读取的消息跟客户端TCP连接时发送的消息顺序是一致的
				 * 客户端连接时发送的第一个int类型数据时UDP端口号,第一个string类型参数是用户名
				 * */					
				String name = dis.readUTF();//读取客户端用户名
							
				if (clientTCP_Port == 0) {
					this.disConnect();
					System.exit(0);
					return;
				}	
				
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				dos.writeInt(ID++);
				System.out.println("一个用户上线IP="+ s.getInetAddress().getHostAddress() + "port="+ clientTCP_Port);

			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}

	/**
	 * 断开连接
	 * */
	public void disConnect() {
		if (ss != null) {
			try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ss = null;
		}
	}
	
	/*
	 * 服务器端用来保持记录的客户端信息的实体类
	 * 客户端IP
	 * 客户端UDP端口
	 * 客户端ID
	 * 客户端用户名
	 * */
	private class Client {
		String userIP;
		int UDPPort;
		int userId;
		String userName;

		public Client(String IP, int UDPPort,int id, String name) {
			this.userIP = IP;
			this.UDPPort = UDPPort;
			this.userId = id;
			this.userName = name;
		}
		public Client(String IP, int UDPPort) {
			this.userIP = IP;
			this.UDPPort = UDPPort;
		}
	}

	/*
	 * 开启了一个UDP的连接的线程
	 * */
	private class UDPThread implements Runnable {
		DatagramSocket ds = null;
		
		public void run() {
			try {
				//用于接收和发送UDP的Socket实例
				ds = new DatagramSocket(UDP_PORT);
				
			} catch (SocketException e1) {
				e1.printStackTrace();
			}

			while (ds != null) {
				try {
				
					if(ds.isClosed()) {
						System.exit(0);
					}
					
					//DatagramPacket用于处理报文，它将Byte数组、目标地址、目标端口等数据包装成报文或者将报文拆卸成Byte数组
					//发送给客户端的报文包
					//将数据包中buffer.length长的数据装进buffer数组，一般用来接收客户端发送的数据。
					dp = new DatagramPacket(buffer, buffer.length);
					
					//发给客户端的输出流
					baos = new ByteArrayOutputStream();
					dos = new DataOutputStream(baos);
									
					//接收数据报文放到dp中。
					//receive方法产生一个“阻塞”。“阻塞”是一个专业名词，它会产生一个内部循环，使程序暂停在这个地方，直到一个条件触发。
					//该方法会阻塞用来接收各自客户端传来的消息
					ds.receive(dp);
					System.out.println("userid:"+userid);
					userUDP_Port = dp.getPort();
					
					//接收到客户端输入的流
					bais = new ByteArrayInputStream(dp.getData());
					dis = new DataInputStream(bais);
					
					
					msgType = dis.readInt();//获取消息类型
					userid = dis.readInt();//请求方的id
					System.out.println("userid:"+userid);
					
					switch (msgType) {		
					case Msg.ClientOnMsg://用户上线
					
						String cName = dis.readUTF();
						System.out.println(clients.size());
						System.out.println(userid);
						if(findClient(userid)==null){
							//每连接一个客户端就维护一个客户端实例，并将其添加到客户端列表中
							Client cOn = new Client(userIP,userUDP_Port,ID-1,cName);
							clients.add(cOn);
						}
						System.out.println("一个用户上线");			
						break;
						
					case Msg.ClientOffMsg://用户下线
						
						Client cOff = findClient(userid);
						if(cOff!=null){
							ServerListener.this.clients.remove(cOff);
						}
						System.out.println("一个用户下线");
						break;
						
						
					case Msg.AskClientsMsg://返回在线人数信息				
						
						//返回在线人数信息
						askCientsCount(dp,ds);
						break;
						
					case Msg.ChatMsg://聊天消息
						
						//消息的接收方id
						int msgGuestId = dis.readInt();
						//向指定的一个客户端发送消息
						sendMsg2One(dp, ds, msgGuestId);
			
						break;
						
					case Msg.MsgRequest://私聊请求（p2p模式用来发送探测包）
						
						System.out.println("聊天请求，目的机先准备向请求机发送探测包");
						
						//被请求方id，即要发送探测包的客户端id
						int reqGguestId = dis.readInt();
						
						System.out.println("请求机id"+userid);
						System.out.println("目标机id"+reqGguestId);
						//服务器告诉目标机发送探测包给请求机（打洞）				
						//参数3：接收探测包id
						//参数4：发送探测包id
						sendProbePacket(dp,ds,userid,reqGguestId);

						break;
						
					case Msg.MsgResponse://私聊请求回复
						
						System.out.println("回复聊天请求，请求机准备向目的机发送探测包");
						
						//被回复方的id,即要接收探测包的客户端id	
						int repGuestId = dis.readInt();				
						//向指定的客户端发送消息
						System.out.println("请求机id"+userid);
						System.out.println("目标机id"+repGuestId);
						
						//服务器告诉请求机发送探测包给目标机
						//参数3：接收探测包id
						//参数4：发送探测包id
						sendProbePacket(dp,ds,repGuestId,userid);
			
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
	
	/**
	 * 
	 * 返回在线人数消息
	 * 
	 * */
	private void askCientsCount(DatagramPacket dp,DatagramSocket ds){
		
		//消息类型写入输出流
		try {
			
			dos.writeInt(msgType);//消息类型
			dos.writeInt(userid);//用户ID写入到输出流
			dos.writeUTF(userIP);//用户的udp地址
			dos.writeInt(userUDP_Port);//用户的udp端口

			//在线人数写入的输出流
			int clientCount = ServerListener.this.clients.size();		
			dos.writeInt(clientCount);
			
			//把每个在线用户的id和name都写入到输出流
			for(int i=0; i<clientCount; i++) {
				int clientId = ServerListener.this.clients.get(i).userId;
				String name = ServerListener.this.clients.get(i).userName;
				dos.writeInt(clientId);
				dos.writeUTF(name);
			}
			
			//把在线人数的消息放入到一个btye的缓冲区数组中
			buffer = baos.toByteArray();
			
			//释放DatagramPacket，把从客户端读取的消息清空，并把在线人数消息放入到DatagramPacket中
			dp = null;
			dp = new DatagramPacket(buffer, buffer.length);
			baos.reset();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		System.out.println("将所有在线信息发送到所有客户端");				
		if (dp != null) {
			
			System.out.println("当前服务器端记录的客户端总数:"+ServerListener.this.clients.size());
			//循环的将消息发送到每个在线的客户端上
			for (int i = 0; i < ServerListener.this.clients.size(); i++) {
				Client c = ServerListener.this.clients.get(i);
				//设置要将此数据报发往的远程主机的 SocketAddress（通常为 IP 地址 + 端口号）
				dp.setSocketAddress(new InetSocketAddress(c.userIP,c.UDPPort));
				
				try {
					ds.send(dp);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * 将消息发送到指定客户端
	 * 
	 * */
	private void sendMsg2One(DatagramPacket dp,DatagramSocket ds,int guestId){
		System.out.println("将消息发送到指定的客户端");				
		if (dp != null && ServerListener.this.clients.size()>0) {
			
			Client c = findClient(guestId);
			if(c!=null)
			{		
				dp.setSocketAddress(new InetSocketAddress(c.userIP,c.UDPPort));
				
				try {
					
					ds.send(dp);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		}
	}
	
	/**
	 * 
	 * 将消息发送到所有客户端
	 * 
	 * */
	private void sendMsg2All(DatagramPacket dp,DatagramSocket ds){
		System.out.println("将信息发送到所有客户端");				
		if (dp != null) {
			
			System.out.println("当前服务器端记录的客户端总数:"+ServerListener.this.clients.size());
			//循环的将消息发送到每个在线的客户端上
			for (int i = 0; i < ServerListener.this.clients.size(); i++) {
				Client c = ServerListener.this.clients.get(i);
				//设置要将此数据报发往的远程主机的 SocketAddress（通常为 IP 地址 + 端口号）
				dp.setSocketAddress(new InetSocketAddress(c.userIP,c.UDPPort));
				
				try {
					ds.send(dp);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * p2p的发送探测包
	 * recId:接收探测包的用户id
	 * sendId:发送探测包的用户id
	 * */
	private void sendProbePacket(DatagramPacket dp,DatagramSocket ds,int recId,int sendId){
		//消息类型写入输出流
		try {
			
			dos.writeInt(msgType);//消息类型写入输出流
			dos.writeInt(recId);//要接受探测包的id
			dos.writeInt(sendId);//要发送探测包的用户id写入输出流
			
			String ip = null;
			int port = 0;
			
			if (dp != null && ServerListener.this.clients.size()>0) {
				
				//需要接收探测包的客户端信息
				Client recClient = findClient(recId);
				//发送探测包的客户端信息
				Client sendClient = findClient(sendId);
				
				if(recClient!=null&&sendClient!=null){
					ip = sendClient.userIP;
					port = sendClient.UDPPort;
					
					dos.writeUTF(recClient.userIP);//接收探测包的ip写入输出流
					dos.writeInt(recClient.UDPPort);//接收探测包的UDP端口写入输出流
				}
				else{//目标机不在线
					dos.writeUTF("0");//目标机的ip写入输出流
					dos.writeInt(0);//目标机的UDP端口写入输出流
				}
			}
		
			//把在线人数的消息放入到一个btye的缓冲区数组中
			buffer = baos.toByteArray();			
			//释放DatagramPacket，把从客户端读取的消息清空，并把在线人数消息放入到DatagramPacket中
			dp = null;
			dp = new DatagramPacket(buffer, buffer.length);
			baos.reset();
			
			//探测包发送到目标机
			dp.setSocketAddress(new InetSocketAddress(ip,port));			
			ds.send(dp);
			System.out.println("已告知目标机发送探测包");
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
	} 
	
	/**
	 * 查找需要的在线客户端
	 * */
	private Client findClient(int clientId) {
		Client c = null;
		for (int i = 0; i < clients.size(); i++) {
			c = clients.get(i);
			if(c.userId != clientId){
				c = null;
				continue;
			}
			else {
				break;
			}
		}
		return c;
	}
}
