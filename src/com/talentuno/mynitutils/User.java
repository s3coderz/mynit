package com.talentuno.mynitutils;

public class User {

	String name;
	String uid;
	String dpId;
	String phNumber;
	String email;

	@Override
	public String toString() {
		return "User [name=" + name + ", uid=" + uid + ", dpId=" + dpId
				+ ", phNumber=" + phNumber + ", email=" + email + "]";
	}

	public User(String name, String uid, String dpId, String phNumber, String email) {
		super();
		this.name = name;
		this.uid = uid;
		this.dpId = dpId;
		this.phNumber = phNumber;
		this.email = email;
	}

	public static String createUser( String name, String uid, String dpId, String phNumber, String email ) {
		
		return "{\"statements\":[{\"statement\":\"CREATE (user:User{props}) RETURN user.uid\",\"parameters\":{\"props\":{\"name\":\""+name+"\",\"uid\":\""+uid+"\",\"phNumber\":\""+phNumber+"\",\"dpId\":\""+dpId+"\",\"email\":\""+email+"\"}}}]}";
		
	}

	public static  String getUser( String uid ) {
		
		return "{\"statements\":[{\"statement\":\"MATCH (user:User{uid:{uid}}) RETURN user\",\"parameters\":{\"uid\":\""+uid+"\"}}]}";
		
	}

	public static  String requestOTP( String countryCode , String phNumber ) {
		
		String number = countryCode + phNumber;
		number = number.replaceAll( "\\+" , "" );
		number = number.replaceAll( "\\-" , "" );
		number = number.replaceAll( "\\s+" , "" );
		
		return "{\"statements\":[{\"statement\":\"CREATE (otp:PendingOTP{props}) RETURN id(otp)\",\"parameters\":{\"props\":{\"number\":\""+number+"\"}}}]}";
		
	}
	
}