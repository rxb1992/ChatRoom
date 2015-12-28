package com.example.rxb.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ChatFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = 3L;

	ChatManager mc;

	Container cont = this.getContentPane();
	JPanel jp1 = new JPanel(new GridLayout(1, 1));
	JPanel jp2 = new JPanel(new GridLayout(2, 1));
	JPanel jp3 = new JPanel();

	JTextArea jta1 = new JTextArea();
	JScrollPane scrollPane1 = new JScrollPane(jta1);
	JTextArea jta2 = new JTextArea();
	JScrollPane scrollPane2 = new JScrollPane(jta2);

	JButton sendButton = null;
	JButton updButton = null;
	
	private int guestId;
	private String guestName;
	private String guestIP;
	private int guestPort;

	public ChatFrame(ChatManager mc) {
		this.mc = mc;
		
		this.setSize(400, 500);
		this.setLocation(800, 100);
		this.setLayout(new GridLayout(2, 1));

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (ChatFrame.this.jta1.getText().equals("") || !ChatFrame.this.mc.isOnline()) {
					ChatFrame.this.setVisible(false);
					return;
				}
				int result = JOptionPane.showConfirmDialog(ChatFrame.this,
						"是否保存聊天记录？", "提示", JOptionPane.YES_NO_CANCEL_OPTION);
				switch (result) {
				case JOptionPane.YES_OPTION:
					ChatFrame.this.saveChatRecord();
					break;
				case JOptionPane.NO_OPTION:
					break;
				case JOptionPane.CANCEL_OPTION:
					break;
				}
				ChatFrame.this.jta1.setText("");
				ChatFrame.this.setVisible(false);
			}
		});
//		this.setVisible(true);
		jta1.setEditable(false);
		jta2.setEditable(true);

		jp1.add(scrollPane1);
		jp2.add(scrollPane2);
		jp2.add(jp3, BorderLayout.SOUTH);

		Icon sendIcon = new ImageIcon("icons/send.png");
		Icon updIcon = new ImageIcon("icons/upd_info.png");
		sendButton = new JButton(sendIcon);
		updButton = new JButton(updIcon);
		sendButton.setSize(100, 30);
		updButton.setSize(100, 30);
		sendButton.setPreferredSize(new Dimension(100, 30));
		updButton.setPreferredSize(new Dimension(100, 30));
		sendButton.addActionListener(this);
		updButton.addActionListener(this);
		jp3.add(sendButton);
		jp3.add(updButton);
		jp3.setSize(200, 60);
		this.add(jp1, BorderLayout.SOUTH);

		cont.add(jp1);
		cont.add(jp2);
	}

	protected void saveChatRecord() {

		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		String date = year + "-" + month + "-" + day + "  " + hour + ":"
				+ minute + ":" + second;

		FileWriter fw = null;
		try {
			fw = new FileWriter("D:\\chatReord.txt");
			String record = this.jta1.getText();
			fw.append("与"+this.guestName+"聊天记录   ");
			fw.append(date + "\r\n");
			fw.append(record + "\r\n");
			fw.flush();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.sendButton) {
			if (mc.isOnline() == false) {
				JOptionPane.showConfirmDialog(this, "你已经离线！！！");
				return;
			}
			String msg = jta2.getText();
			jta2.setText("");
			this.showChatMsg(this.mc.getName(), msg);
			
			//通过服务器中转方式发送消息
			//this.mc.sendMsgByServer(this.getGuestId(),msg);
			
			//通过p2p的方式发送消息
			this.mc.sendMsgByP2p(guestIP,guestPort , msg);
		}
		if (e.getSource() == this.updButton) {
			jta2.setText("");
		}
	}

	public void showChatMsg(String name, String msg) {
		this.jta1.append(name + "说：" + msg + "\r\n");
	}

	public int getGuestId() {
		return guestId;
	}

	public void setGuestId(int guestId) {
		this.guestId = guestId;
	}

	public String getGuestName() {
		return guestName;
	}

	public void setGuestName(String guestName) {
		this.guestName = guestName;
	}

	public void setGuestIP(String guestIP) {
		this.guestIP = guestIP;
	}
	
	public String getGuestIP() {
		return guestIP;
	}
	
	public void setGuestPort(int guestPort) {
		this.guestPort = guestPort;
	}
	
	public int getGuestPort() {
		return guestPort;
	}
	
}
