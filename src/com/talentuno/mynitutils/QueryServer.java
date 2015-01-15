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

public class QueryServer extends AsyncTask<String, Void, String> {
	
	public enum Action {
		
		CREATE_USER, // action to create a user in the Database
		GET_USER, // action to get all profile information of a user from Database 
		REQUEST_OTP, // action to request an OTP from server
		VERIFY_OTP, // action to verify OTP entered by user matches one on server
		EDIT_USER,
		CREATE_COMMENT,
		GET_COMMENT,
		GET_COMMENTS,
		UPVOTE_COMMENT,
		DOWNVOTE_COMMENT,
		TEST_ACTION
		
	}

	String cypherQuery = "";
	ResultHandler caller;
	Action action;
	int requestId;
	int responseId;

	public QueryServer(ResultHandler caller, Action action , int requestId , int responseId ) {
		this.caller = caller;
		this.action = action;
		this.requestId = requestId;
		this.responseId = responseId;
	}

	@Override
	protected void onPostExecute(String result) {
		
		if( cypherQuery.startsWith( "error:" ) ) {
			
			Log.d( "com.talentuno.mynit", cypherQuery );
			caller.onFailure(cypherQuery, requestId, responseId);
			return;
			
		}
		
		JsonObject json = new JsonParser().parse( result ).getAsJsonObject();
		JsonArray errors = json.getAsJsonArray( "errors" );
		if( errors.size() != 0 ) {
			
			String errMsg = "error:" + errors.get(0).getAsJsonObject().get("message").getAsString();
			Log.d( "com.talentuno.mynit", errMsg );
			caller.onFailure(errMsg, requestId, responseId);
			return;
			
		}
		
		if( json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).size() == 0 ) {
			
			String errMsg = "error:No data to show";
			Log.d( "com.talentuno.mynit", errMsg );
			caller.onFailure(errMsg, requestId, responseId);
			return;
			
		}
		
		switch( action ) {
		// on request OTP send false for OTP not required and true for OTP required
		case CREATE_USER:
		case REQUEST_OTP:
		case VERIFY_OTP:
		case EDIT_USER:
		case CREATE_COMMENT:
		case UPVOTE_COMMENT:
		case DOWNVOTE_COMMENT:
			caller.onSuccess("", requestId, responseId);
			break;
			
		case GET_USER:
			String name = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("name").getAsString();
			String uid = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("uid").getAsString();
			String phNumber = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("phNumber").getAsString();
			String email = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("email").getAsString();
			String dpId = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("dpId").getAsString();
			String imei = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("imei").getAsString();
			caller.onSuccess( new User(name, uid, dpId, phNumber, email, imei), requestId, responseId);
			break;
			
		case GET_COMMENT:
			name = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("name").getAsString();
			uid = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("uid").getAsString();
			String text = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("text").getAsString();
			boolean enabled = Boolean.parseBoolean( json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("enabled").getAsString() );
			long date = Long.parseLong(json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("date").getAsString());
			String commentId = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("commentId").getAsString();
			long upvote = Long.parseLong(json.getAsJsonArray("results").get(1).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsString());
			long downvote = Long.parseLong(json.getAsJsonArray("results").get(2).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsString());
			caller.onSuccess( new Comment(text, name, uid, enabled, date, upvote, downvote, commentId), requestId, responseId);
			break;
			
		case GET_COMMENTS:
			caller.onSuccess(json, requestId, responseId);
			break;
			
		default:
			cypherQuery = "error:Action " + action + " is undefined";
			caller.onFailure(cypherQuery, requestId, responseId);
			break;
			
		}
		
	}

	@Override
	protected String doInBackground( String... params ) {
		
		switch( action ) {
		
		case CREATE_USER:
			cypherQuery = User.createUser(params[0],params[1],params[2],params[3],params[4],params[5]);
			break;
			
		case GET_USER:
			cypherQuery = User.getUser(params[0]);
			break;
			
		case REQUEST_OTP:
			cypherQuery = User.requestOTP(params[0],params[1],params[2]);
			break;
			
		case VERIFY_OTP:
			cypherQuery = User.verifyOTP(params[0],params[1],params[2]);
			break;
			
		case EDIT_USER:
			cypherQuery = User.editUser(params[0],params[1],params[2],params[3],params[4],params[5]);
			break;
			
		case CREATE_COMMENT:
			cypherQuery = Comment.createComment(params[0],params[1],params[2],params[3]);
			break;
			
		case GET_COMMENT:
			cypherQuery = Comment.getComment(params[0]);
			break;
			
		case GET_COMMENTS:
			cypherQuery = Comment.getComments(params);
			break;
			
		case UPVOTE_COMMENT:
			cypherQuery = Comment.upVote(params[0],params[1]);
			break;
			
		case DOWNVOTE_COMMENT:
			cypherQuery = Comment.downVote(params[0],params[1]);
			break;	
			
		default:
			cypherQuery = "error:Action " + action + " is undefined";
			break;
			
		}
		

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