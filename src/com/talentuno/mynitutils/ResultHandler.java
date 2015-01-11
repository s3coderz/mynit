package com.talentuno.mynitutils;

public interface ResultHandler {

	public void onSuccess( Object object );
	public void onFailure( String errMsg );
	
}