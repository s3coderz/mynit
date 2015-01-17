package com.talentuno.mynitutils;

public class User {

	String name;
	String uid;
	String dpId;
	String phNumber;
	String email;
	String imei;

	@Override
	public String toString() {
		return "User [name=" + name + ", uid=" + uid + ", dpId=" + dpId
				+ ", phNumber=" + phNumber + ", email=" + email + ", imei=" + imei + "]";
	}

	public User(String name, String uid, String dpId, String phNumber, String email, String imei) {
		this.name = name;
		this.uid = uid;
		this.dpId = dpId;
		this.phNumber = phNumber;
		this.email = email;
		this.imei = imei;
	}

	public static String createUser( String name, String uid, String dpId, String phNumber, String email, String imei ) {
		
		if( uid == null ) return "error:uid cannot be null";
		return "{\"statements\":[{\"statement\":\"CREATE (user:User{props}) RETURN user.uid\",\"parameters\":{\"props\":{\"name\":\""+name+"\",\"uid\":\""+uid+"\",\"phNumber\":\""+phNumber+"\",\"dpId\":\""+dpId+"\",\"email\":\""+email+"\",\"imei\":\""+imei+"\"}}}]}";
		
	}

	public static String editUser( String name, String uid, String dpId, String phNumber, String email, String imei ) {
		
		if( uid == null ) return "error:uid cannot be null";
		return "{\"statements\":[{\"statement\":\"MATCH (user:User{uid:{uid}}) SET user.name={name},user.dpId={dpId},user.phNumber={phNumber},user.email={email} RETURN user.uid\",\"parameters\":{\"name\":\""+name+"\",\"uid\":\""+uid+"\",\"phNumber\":\""+phNumber+"\",\"dpId\":\""+dpId+"\",\"email\":\""+email+"\",\"imei\":\""+imei+"\"}}]}";
		
	}

	public static  String getUser( String uid ) {
		
		if( uid == null ) return "error:uid cannot be null";
		return "{\"statements\":[{\"statement\":\"MATCH (user:User{uid:{uid}}) RETURN user\",\"parameters\":{\"uid\":\""+uid+"\"}}]}";
		
	}

	public static  String requestOTP( String countryCode , String phNumber, String imei ) {
		
		String number = countryCode + phNumber;
		number = number.replaceAll( "\\+" , "" );
		number = number.replaceAll( "\\-" , "" );
		number = number.replaceAll( "\\s+" , "" );
		
		return "{\"statements\":[{\"statement\":\"MATCH (user:User{phNumber:{phNumber}}) return count(user)\",\"parameters\":{\"phNumber\":\""+number+"\"}},{\"statement\":\"MATCH (user:User{phNumber:{phNumber},imei:{imei}}) return count(user)\",\"parameters\":{\"phNumber\":\""+number+"\",\"imei\":\""+imei+"\"}},{\"statement\":\"MATCH (user:User{phNumber:{phNumber}}) SET user.otpSkip =  CASE user.imei WHEN {imei} THEN 'yes' ELSE 'no' END return user.otpSkip\",\"parameters\":{\"phNumber\":\""+number+"\",\"imei\":\""+imei+"\"}},{\"statement\":\"CREATE (otp:PendingOTP{props}) RETURN id(otp)\",\"parameters\":{\"props\":{\"number\":\""+number+"\"}}}]}";
		
	}

	public static  String verifyOTP( String countryCode , String phNumber , String OTP ) {
		
		String number = countryCode + phNumber;
		number = number.replaceAll( "\\+" , "" );
		number = number.replaceAll( "\\-" , "" );
		number = number.replaceAll( "\\s+" , "" );
		
		return "{\"statements\":[{\"statement\":\"MATCH (o:VerifyOTP{otp:{otp},number:{number}}) RETURN id(o)\",\"parameters\":{\"number\":\""+number+"\",\"otp\":\""+OTP+"\"}}]}";
		
	}
	
}