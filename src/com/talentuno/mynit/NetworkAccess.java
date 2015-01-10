package com.talentuno.mynit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class NetworkAccess implements Runnable {

	private final CountDownLatch latch;
	private final String uri;
	private final String params;
	public static String result;
	
	@Override
	public void run() {
		
		String responseString = null ;
		try {
        	HttpPost httpPost = new HttpPost(this.uri);
        	
    		if( params != null ) {
    			
    			try {
    				httpPost.setEntity(new StringEntity(params));
    			} catch (UnsupportedEncodingException e) {
    				Log.d( "com.talentuno.mynit", e.getMessage() );
    			}
    			
    		}
            
            HttpResponse response;
            try {
            	
                httpPost.setHeader("Accept", "application/json; charset=UTF-8");
                httpPost.setHeader("Content-type", "application/json");
                response = new DefaultHttpClient().execute(httpPost);
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK || statusLine.getStatusCode() == HttpStatus.SC_CREATED ){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                Log.d( "com.talentuno.mynit", e.getMessage() );
            } catch (IOException e) {
            	Log.d( "com.talentuno.mynit", e.getMessage() );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
		
		NetworkAccess.result = responseString;
		latch.countDown();
		
	}

	public NetworkAccess( CountDownLatch latch , String uri, String params ) {
		
		this.latch = latch;
		this.uri = uri;
		this.params = params;
		
	}
	
}