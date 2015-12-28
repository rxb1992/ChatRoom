package com.example.rxb.client;

import javax.swing.JOptionPane;

public class Guest {
	private int guestId;
	private String guestName;
	private String guestIP;
	private int guestPort;
	private ChatFrame cf;
	
	public Guest(int guestId,String guestName,String guestIP,int guestPort) {
		this.guestId = guestId;
		this.guestName = guestName;
		this.guestIP = guestIP;
		this.guestPort = guestPort;
		
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

	public boolean equals(Object obj) {
		
		if(obj instanceof Guest) {
			Guest g = (Guest)obj;
			if(this.getGuestId() == g.getGuestId() && this.getGuestName().equals(g.getGuestName())) {
				return true;
			}
		}
		return false;
	}

	public ChatFrame getCf() {
		return cf;
	}

	public void setCf(ChatFrame cf) {
		this.cf = cf;
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
