package com.example.rxb.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.example.rxb.message.*;
import com.example.rxb.server.ServerListener;

public class ChatManager extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	JButton jbChat = new JButton("聊天");
	JButton jbOffline = new JButton("离线");
	JButton jbRefresh = new JButton("刷新");

	JPanel jp = new JPanel();
	TextArea ta = new TextArea(5, 39);

	NetClient nc = new NetClient(this);//连接实体
	
	public List<Guest> guestList = new ArrayList<Guest>();//用户列表
	public boolean online = false;
	

	int id;
	String name;
	public static final int UDPPort = 3332;

	public ChatManager() {
		this.setBackground(Color.BLUE);
		this.setVisible(true);
		this.setLayout(new FlowLayout());
		this.setLocation(200, 300);

		String name = JOptionPane.showInputDialog("名字");

		this.name = name;

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (ChatManager.this.isOnline() == true) {
					jbOffline.doClick();
				}
				System.exit(0);
			}
		});
		this.add(ta);

		this.add(jp, BorderLayout.SOUTH);
		jp.add(jbChat);
		jp.add(jbOffline);
		jp.add(jbRefresh);
		jbChat.addActionListener(this);
		jbOffline.addActionListener(this);
		jbRefresh.addActionListener(this);

		ta.setEditable(false);
		this.setSize(300, 400);
		this.setResizable(false);

		//通过TCP连接到服务器端来登陆
		this.connect(nc.serverIP, ServerListener.TCP_PORT);
		
		//发送客户端上线的信息到服务器端
		//name是用户名
		//id是客户端连接服务器时服务器分配的一个用id
		ClientOnMsg msg = new ClientOnMsg(this.name, this.id);
		this.nc.send(msg);
		
		online = true;
		this.setTitle(name + "上线");

		//发送用户上线的id过去，并从服务器获取在线人数等相关信息
		//id是服务器端分配给客户端的已个id
		AskClientsMsg aMsg = new AskClientsMsg(this.id);
		this.nc.send(aMsg);

	}
	
	///程序入口方法
	public static void main(String[] args) {
		new ChatManager();
	}
	
	public void connect(String IP, int port) {
		if (IP == null || IP.equals("")) {
			System.exit(0);
		}

		try {
			
			Socket s = new Socket(IP, port);
			//登陆的时候是通过TCP连接的，所有服务器端的接收也写在了TCP的连接中
			//创建了一个发送到服务器端的输出流，里面存放了登陆要告诉服务器的消息内容：port,用户名。
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			dos.writeUTF(name);
			
			DataInputStream dis = new DataInputStream(s.getInputStream());
			id = dis.readInt();
			System.out.println("用户id=" + id);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	//按钮事件
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "聊天") {
			String sId = JOptionPane.showInputDialog("请输入要聊天的对方的编号：");
			int guestId;
			if (sId != null && !sId.equals("")) {
				guestId = Integer.parseInt(sId);
				if (guestId == this.id) {
					JOptionPane.showMessageDialog(this, "没搞错吧，跟自己聊天？");
					return;
				}
				
				Guest guest = findClient(guestId);
				if(guest!=null&&guest.getCf().isVisible() == false){
					/**
					 * 建立到客户机的地址映射
					 * 也就是俗称的UDP打洞（客户端到客户端的打洞）
					 * p2p模式
					 * 
					 * */
					System.out.println("准备打洞");
					MsgRequest msg = new MsgRequest(this.id,guestId);
					nc.send(msg);
					
					
					/**
					 * 打开聊天窗口
					 * 去掉上面的p2p模式就是服务器中转模式
					 * */
					guest.getCf().setVisible(true);				
				}
			} else{
				return;
			}
		}
		
		if (e.getActionCommand() == "离线") {
			if (this.online == true) {
				ClientOffMsg msg = new ClientOffMsg(this.id, this.name);
				this.nc.send(msg);
				this.online = false;
				this.setTitle(this.name + "离线");

				JOptionPane.showMessageDialog(this, "您已经离线");

				this.jbOffline.setEnabled(false);
				this.jbChat.setEnabled(false);
				this.jbRefresh.setEnabled(false);
			}

		}
		
		if (e.getActionCommand() == "刷新") {
			AskClientsMsg msg = new AskClientsMsg(this.id);
			nc.send(msg);
		}

	}


	public JButton getJbOffline() {
		return jbOffline;
	}

	public void setJbOffline(JButton jbOffline) {
		this.jbOffline = jbOffline;
	}

	public JPanel getJp() {
		return jp;
	}

	public void setJp(JPanel jp) {
		this.jp = jp;
	}

	public TextArea getTa() {
		return ta;
	}

	public void setTa(TextArea ta) {
		this.ta = ta;
	}

	public NetClient getNc() {
		return nc;
	}

	public void setNc(NetClient nc) {
		this.nc = nc;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static int getUDPPort() {
		return UDPPort;
	}
	
	/**
	 * 移除本地的一个客户端
	 * */
	public void removeClient(int id, String name) {
		this.ta.append(name + "离线" + "\r\n");
		for (int i = 0; i < guestList.size(); i++) {
			Guest g = guestList.get(i);
			if (g.getGuestId() == id && g.getGuestName().equals(name)) {
				guestList.remove(g);
				break;
			}
		}
		AskClientsMsg msg = new AskClientsMsg(this.id);
		nc.send(msg);
	}

	/**
	 * 在线总人数
	 * */
	public void countClient(int count) {
		this.ta.setText("");
		this.ta.append("在线人数：" + count + "\r\n");
	}

	/**
	 * 向客户端的在线人数列表中添加新登陆的用户
	 * */
	public void addGuest(int guestId, String guestName,String guestUDP_IP,int guestUDP_Port) {
		Guest g = new Guest(guestId, guestName,guestUDP_IP,guestUDP_Port);
		if (!this.guestList.contains(g)) {
			ChatFrame cf = new ChatFrame(this);
			
			//设置聊天对象的id,用户名,ip，port
			cf.setGuestId(guestId);
			cf.setGuestName(g.getGuestName());
			cf.setGuestIP(guestUDP_IP);
			cf.setGuestPort(guestUDP_Port);
			
			cf.setTitle("与" + cf.getGuestName() + "聊天中");
			g.setCf(cf);
			cf.setVisible(false);
			guestList.add(g);
		}
	}

	/**
	 * 展现在线用户信息
	 * */
	public void showClient(int id, String name) {
		this.ta.append("ID：" + id + "--名字：" + name + "\r\n");
	}

	/**
	 * 向指定id的用户发送消息
	 * 该消息通过服务器中转
	 * */
	public void sendMsgByServer(int id, String msg) {
		this.nc.sendMsgByServer(id, msg);
	}
	
	/**
	 * 向指定id的用户发送消息
	 * 该消息通过p2p发送
	 * */
	public void sendMsgByP2p(String ip,int port, String msg) {
		this.nc.sendMsgByP2P(ip, port, msg);
	}

	/**
	 * 聊天界面展现消息
	 * */
	public void showChatMsg(int id, String name, String msg) {
		for (int i = 0; i < this.guestList.size(); i++) {
			Guest g = guestList.get(i);
			if (g.getGuestId() == id) {
				g.getCf().setVisible(true);
				g.getCf().showChatMsg(name, msg);
				break;
			}
		}
	}

	/**
	 * 当前客户端向指定的客户端打洞
	 * 
	 * sendid:发送探测包的用户id
	 * recid:要接收探测包的用户id
	 * ip:要接收探测包的用户ip
	 * port:要接收探测包的用户端口
	 * */
	public void handleRequest(int sendid,int recid,String ip,int port) {		
		
		//在自己这边打洞
		burrow(ip,port); 
	       
        //告诉服务器通知对方打洞
        MsgResponse msg = new MsgResponse(recid,sendid);
		nc.send(msg);
		
		System.out.println("告诉服务器让对方打洞");
		
		//刷新在线人数
		AskClientsMsg aMsg = new AskClientsMsg(this.id);
		nc.send(aMsg);
	}

	/**
	 * 当前客户端回复其他客户端打洞
	 * 
	 * sendid:发送探测包的用户id
	 * recid:要接收探测包的用户id
	 * ip:要接收探测包的用户ip
	 * port:要接收探测包的用户端口
	 * */
	public void handleResponse(String ip,int port) {
		//在自己这边打洞
		burrow(ip,port); 
			       
		System.out.println("对方打洞已完毕，我方开始打洞");
	}
	
	/**
	 * 查找指定id的客户端
	 * */
	private Guest findClient(int clientId) {
		Guest g = null;
		for (int i = 0; i < this.guestList.size(); i++) {
			g = this.guestList.get(i);
			if(g.getGuestId() != clientId){
				g = null;
				continue;
			}
			else {
				break;
			}
		}
		return g;
	}

	/**
	 * 客户端打洞方法
	 * */
	private void burrow(String ip,int port){
		SocketAddress target = new InetSocketAddress(ip, port);  
        
        String message = "send probe packet ";  
        byte[] sendbuf = message.getBytes(); 
        
        //开始在自己这边打洞
        DatagramPacket pack;
        try {
        	
        	pack = new DatagramPacket(sendbuf, sendbuf.length, target);
			nc.ds.send(pack);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}

}
