package demonworld.model;

import java.io.Serializable;

public class ServerTransferPackage implements Serializable{
	public int sourceUserId;
	public String username;
	/**
	 * Object to transfer
	 */
	public Object obj; //
	
	public ServerTransferPackage( int userId, String username, Object obj ) {
		this.sourceUserId = userId;
		this.username = username;
		this.obj = obj;
	}
	
	public String toString() {
		String s = "";
		s += "SenderID:"+sourceUserId;
		s += "SenderUN:"+username;
		s += "obj:"+obj;
		return s;
	}
}
