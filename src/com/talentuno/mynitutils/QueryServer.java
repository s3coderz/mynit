package com.talentuno.mynitutils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.os.AsyncTask;
import android.util.Log;

public class QueryServer extends AsyncTask<Void, Void, String> {

	String cypherQuery = "";
	ResultHandler caller;
	Object[] params;
	String action = "";

	public QueryServer(ResultHandler caller, String action, String... params) {
		this.caller = caller;
		this.action = action;
		this.params = params;
		
		switch( action ) {
		
		case "createUser":
			cypherQuery = User.createUser(params[0],params[1],params[2],params[3],params[4]);
			break;
			
		case "getUser":
			cypherQuery = User.getUser(params[0]);
			break;
			
		case "requestOTP":
			cypherQuery = User.requestOTP(params[0],params[1]);
			break;
			
		default:
			cypherQuery = "error:Action " + action + " is undefined";
			break;
			
		}
		
	}

	@Override
	protected void onPostExecute(String result) {
		
		if( cypherQuery.startsWith( "error:" ) ) {
			
			Log.d( "com.talentuno.mynit", cypherQuery );
			caller.onFailure(cypherQuery);
			return;
			
		}
		
		JsonObject json = new JsonParser().parse( result ).getAsJsonObject();
		JsonArray errors = json.getAsJsonArray( "errors" );
		if( errors.size() != 0 ) {
			
			String errMsg = "error:" + errors.get(0).getAsJsonObject().get("message").getAsString();
			Log.d( "com.talentuno.mynit", errMsg );
			caller.onFailure(errMsg);
			return;
			
		}
		
		if( json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).size() == 0 ) {
			
			String errMsg = "error:No data to show";
			Log.d( "com.talentuno.mynit", errMsg );
			caller.onFailure(errMsg);
			return;
			
		}
		
		switch( action ) {
		
		case "createUser":
			caller.onSuccess("");
			break;
			
		case "getUser":
			String name = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("name").getAsString();
			String uid = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("uid").getAsString();
			String phNumber = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("phNumber").getAsString();
			String email = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("email").getAsString();
			String dpId = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("dpId").getAsString();
			caller.onSuccess( new User(name, uid, dpId, phNumber, email));
			break;
			
		case "requestOTP":
			caller.onSuccess("");
			break;
			
		default:
			cypherQuery = "error:Action " + action + " is undefined";
			caller.onFailure(cypherQuery);
			break;
			
		}
		
	}

	@Override
	protected String doInBackground(Void... params) {

		if( cypherQuery.startsWith( "error:" ) )
			return cypherQuery;
		
		String responseString = null;
		
		HttpPost httpPost = new HttpPost("http://54.148.201.55:7474/db/data/transaction/commit");

		try {
			httpPost.setEntity(new StringEntity(cypherQuery));
		} catch (UnsupportedEncodingException e) {
			Log.d( "com.talentuno.mynit", "error:" + e.getMessage() );
			return "error:" + e.getMessage();
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
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (ClientProtocolException e) {
			Log.d( "com.talentuno.mynit", "error:" + e.getMessage() );
			return "error:" + e.getMessage();
		} catch (IOException e) {
			Log.d( "com.talentuno.mynit", "error:" + e.getMessage() );
			return "error:" + e.getMessage();
		} catch (Exception e) {
			Log.d( "com.talentuno.mynit", "error:" + e.getMessage() );
			return "error:" + e.getMessage();
        }
		
		return responseString;
	}

}