package com.talentuno.mynitutils;

public interface ResultHandler {

	public void onSuccess( Object object, int requestId, int responseId );
	public void onFailure( String errMsg, int requestId, int responseId );
	
}