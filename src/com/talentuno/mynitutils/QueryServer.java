package com.talentuno.mynitutils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

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
		GET_USER, // action to get all profile information of a user from
					// Database
		REQUEST_OTP, // action to request an OTP from server
		VERIFY_OTP, // action to verify OTP entered by user matches one on
					// server
		EDIT_USER, CREATE_COMMENT, GET_COMMENT, GET_COMMENTS, UPVOTE_COMMENT, DOWNVOTE_COMMENT, TEST_ACTION, CREATE_SURVEY,
		GET_SURVEY, SAY_YES, SAY_NO, SAY_MAYBE, SET_GROUP

	}

	String cypherQuery = "";
	ResultHandler caller;
	Action action;
	int requestId;
	int responseId;

	public boolean isNetworkAvailable() {
	
		try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://54.148.201.55:7474/db/data/").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500); 
            urlc.connect();
            return (urlc.getResponseCode() == 200);
        } catch (IOException e) {
            Log.d("com.talentuno.mynit", "error:Failed to connect to server");
    		return false;
        }
		
	}

	public QueryServer(ResultHandler caller, Action action, int requestId,
			int responseId) {
		this.caller = caller;
		this.action = action;
		this.requestId = requestId;
		this.responseId = responseId;
	}

	@Override
	protected void onPostExecute(String result) {

		if( action == Action.VERIFY_OTP ) {
			
			Log.d("com.talentuno.mynit", "fake verification" );
			caller.onSuccess("", requestId, responseId);
			return;
			
		}
		
		if (cypherQuery.startsWith("error:")) {

			Log.d("com.talentuno.mynit", cypherQuery);
			caller.onFailure(cypherQuery, requestId, responseId);
			return;

		}

		JsonObject json = new JsonParser().parse(result).getAsJsonObject();
		JsonArray errors = json.getAsJsonArray("errors");
		if (errors.size() != 0) {

			String errMsg = "error:"
					+ errors.get(0).getAsJsonObject().get("message")
							.getAsString();
			Log.d("com.talentuno.mynit", errMsg);
			caller.onFailure(errMsg, requestId, responseId);
			return;

		}

		if (json.getAsJsonArray("results").get(0).getAsJsonObject()
				.getAsJsonArray("data").size() == 0) {

			String errMsg = "error:No data to show";
			Log.d("com.talentuno.mynit", errMsg);
			caller.onFailure(errMsg, requestId, responseId);
			return;

		}

		switch (action) {
		// on request OTP send false for OTP not required and true for OTP
		// required
		case CREATE_USER:
		case VERIFY_OTP:
		case EDIT_USER:
		case CREATE_COMMENT:
		case UPVOTE_COMMENT:
		case DOWNVOTE_COMMENT:
		case CREATE_SURVEY:
		case SAY_YES:
		case SAY_NO:
		case SAY_MAYBE:
		case SET_GROUP:
			caller.onSuccess("", requestId, responseId);
			break;

		case REQUEST_OTP:

			// 0 - new user, otp required
			// 1 - exiting user with same phnumber but different imei, otp
			// required
			// 2 - existing user with same phnumber and imei, no otp required

			long count1 = json.getAsJsonArray("results").get(0)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0).getAsLong();
			long count2 = json.getAsJsonArray("results").get(1)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0).getAsLong();

			if (count1 == 0)
				caller.onSuccess(0, requestId, responseId);
			if (count1 != 0 && count2 == 0)
				caller.onSuccess(1, requestId, responseId);
			if (count1 != 0 && count2 != 0)
				caller.onSuccess(2, requestId, responseId);

			break;

		case GET_USER:
			String name = json.getAsJsonArray("results").get(0)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsJsonObject().get("name").getAsString();
			String uid = json.getAsJsonArray("results").get(0)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsJsonObject().get("uid").getAsString();
			String phNumber = json.getAsJsonArray("results").get(0)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsJsonObject().get("phNumber").getAsString();
			String email = json.getAsJsonArray("results").get(0)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsJsonObject().get("email").getAsString();
			String dpId = json.getAsJsonArray("results").get(0)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsJsonObject().get("dpId").getAsString();
			String imei = json.getAsJsonArray("results").get(0)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsJsonObject().get("imei").getAsString();
			caller.onSuccess(new User(name, uid, dpId, phNumber, email, imei),
					requestId, responseId);
			break;

		case GET_COMMENT:
			name = json.getAsJsonArray("results").get(0).getAsJsonObject()
					.getAsJsonArray("data").get(0).getAsJsonObject()
					.getAsJsonArray("row").get(0).getAsJsonObject().get("name")
					.getAsString();
			uid = json.getAsJsonArray("results").get(0).getAsJsonObject()
					.getAsJsonArray("data").get(0).getAsJsonObject()
					.getAsJsonArray("row").get(0).getAsJsonObject().get("uid")
					.getAsString();
			String text = json.getAsJsonArray("results").get(0)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsJsonObject().get("text").getAsString();
			boolean enabled = Boolean.parseBoolean(json
					.getAsJsonArray("results").get(0).getAsJsonObject()
					.getAsJsonArray("data").get(0).getAsJsonObject()
					.getAsJsonArray("row").get(0).getAsJsonObject()
					.get("enabled").getAsString());
			long date = Long.parseLong(json.getAsJsonArray("results").get(0)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsJsonObject().get("date").getAsString());
			String commentId = json.getAsJsonArray("results").get(0)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsJsonObject().get("commentId").getAsString();
			long upvote = Long.parseLong(json.getAsJsonArray("results").get(1)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsString());
			long downvote = Long.parseLong(json.getAsJsonArray("results")
					.get(2).getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsString());
			caller.onSuccess(new Comment(text, name, uid, enabled, date,
					upvote, downvote, commentId), requestId, responseId);
			break;

		case GET_SURVEY:
			/*
	long yes;
	long no;
	long maybe;*/
			name = json.getAsJsonArray("results").get(0).getAsJsonObject()
					.getAsJsonArray("data").get(0).getAsJsonObject()
					.getAsJsonArray("row").get(0).getAsJsonObject().get("name")
					.getAsString();
			uid = json.getAsJsonArray("results").get(0).getAsJsonObject()
					.getAsJsonArray("data").get(0).getAsJsonObject()
					.getAsJsonArray("row").get(0).getAsJsonObject().get("uid")
					.getAsString();
			text = json.getAsJsonArray("results").get(0)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsJsonObject().get("text").getAsString();
			enabled = Boolean.parseBoolean(json
					.getAsJsonArray("results").get(0).getAsJsonObject()
					.getAsJsonArray("data").get(0).getAsJsonObject()
					.getAsJsonArray("row").get(0).getAsJsonObject()
					.get("enabled").getAsString());
			boolean allowInvite = Boolean.parseBoolean(json
					.getAsJsonArray("results").get(0).getAsJsonObject()
					.getAsJsonArray("data").get(0).getAsJsonObject()
					.getAsJsonArray("row").get(0).getAsJsonObject()
					.get("allowInvite").getAsString());
			date = Long.parseLong(json.getAsJsonArray("results").get(0)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsJsonObject().get("date").getAsString());
			String surveyId = json.getAsJsonArray("results").get(0)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsJsonObject().get("surveyId").getAsString();
			long yes = Long.parseLong(json.getAsJsonArray("results").get(1)
					.getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsString());
			long no = Long.parseLong(json.getAsJsonArray("results")
					.get(2).getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsString());
			long maybe = Long.parseLong(json.getAsJsonArray("results")
					.get(3).getAsJsonObject().getAsJsonArray("data").get(0)
					.getAsJsonObject().getAsJsonArray("row").get(0)
					.getAsString());
			caller.onSuccess(new Survey(surveyId, text, uid, name, enabled, allowInvite, date, yes, no, maybe), requestId, responseId);
			break;

		case GET_COMMENTS:
			ArrayList<Comment> comments = new ArrayList<Comment>();
			JsonArray jsonArray = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray("data");
			for( int i = 0 ; i < jsonArray.size() ; i++ ){
				name = jsonArray.get(i).getAsJsonObject()
						.getAsJsonArray("row").get(0).getAsJsonObject().get("name")
						.getAsString();
				uid = jsonArray.get(i).getAsJsonObject()
						.getAsJsonArray("row").get(0).getAsJsonObject().get("uid")
						.getAsString();
				text = jsonArray.get(i)
						.getAsJsonObject().getAsJsonArray("row").get(0)
						.getAsJsonObject().get("text").getAsString();
				enabled = Boolean.parseBoolean(jsonArray.get(i).getAsJsonObject()
						.getAsJsonArray("row").get(0).getAsJsonObject()
						.get("enabled").getAsString());
				date = Long.parseLong(jsonArray.get(i)
						.getAsJsonObject().getAsJsonArray("row").get(0)
						.getAsJsonObject().get("date").getAsString());
				commentId = jsonArray.get(i)
						.getAsJsonObject().getAsJsonArray("row").get(0)
						.getAsJsonObject().get("commentId").getAsString();
				upvote = Long.parseLong(jsonArray.get(i)
						.getAsJsonObject().getAsJsonArray("row").get(0)
						.getAsString());
				downvote = Long.parseLong(jsonArray.get(i)
						.getAsJsonObject().getAsJsonArray("row").get(0)
						.getAsString());
				comments.add(new Comment(text, name, uid, enabled, date, upvote, downvote, commentId));
			}
			caller.onSuccess(comments, requestId, responseId);
			break;

		default:
			cypherQuery = "error:Action " + action + " is undefined";
			caller.onFailure(cypherQuery, requestId, responseId);
			break;

		}

	}

	@Override
	protected String doInBackground(String... params) {
		
		if( ! isNetworkAvailable() )
			return ("error:Failed to connect to server");

		switch (action) {

		case CREATE_USER:
			cypherQuery = User.createUser(params[0], params[1], params[2],
					params[3], params[4], params[5]);
			break;

		case GET_USER:
			cypherQuery = User.getUser(params[0]);
			break;

		case REQUEST_OTP:
			cypherQuery = User.requestOTP(params[0], params[1], params[2]);
			break;

		case VERIFY_OTP:
			cypherQuery = User.verifyOTP(params[0], params[1], params[2]);
			break;

		case EDIT_USER:
			cypherQuery = User.editUser(params[0], params[1], params[2],
					params[3], params[4], params[5]);
			break;

		case CREATE_COMMENT:
			cypherQuery = Comment.createComment(params[0], params[1],
					params[2], params[3]);
			break;

		case GET_COMMENT:
			cypherQuery = Comment.getComment(params[0]);
			break;

		case GET_COMMENTS:
			cypherQuery = Survey.getComments(params[0]);
			break;

		case UPVOTE_COMMENT:
			cypherQuery = Comment.upVote(params[0], params[1]);
			break;

		case DOWNVOTE_COMMENT:
			cypherQuery = Comment.downVote(params[0], params[1]);
			break;
			
		case CREATE_SURVEY:
			cypherQuery = Survey.createSurvey(params[0], params[1],params[2], Boolean.parseBoolean(params[3]));
			break;

		case GET_SURVEY:
			cypherQuery = Survey.getSurvey(params[0]);
			break;
			
		case SAY_YES:
			cypherQuery = Survey.sayYes(params[0], params[1]);
			break;
			
		case SAY_NO:
			cypherQuery = Survey.sayNo(params[0], params[1]);
			break;
			
		case SAY_MAYBE:
			cypherQuery = Survey.sayMaybe(params[0], params[1]);
			break;
			
		case SET_GROUP:
			cypherQuery = User.setGroup(params[0],params[1],params[2]);
			break;
			
			
		default:
			cypherQuery = "error:Action " + action + " is undefined";
			break;

		}

		if (cypherQuery.startsWith("error:"))
			return cypherQuery;

		String responseString = null;

		HttpPost httpPost = new HttpPost(
				"http://54.148.201.55:7474/db/data/transaction/commit");

		try {
			httpPost.setEntity(new StringEntity(cypherQuery));
		} catch (UnsupportedEncodingException e) {
			Log.d("com.talentuno.mynit", "error:" + e.getMessage());
			return "error:" + e.getMessage();
		}

		HttpResponse response;
		try {

			httpPost.setHeader("Accept", "application/json; charset=UTF-8");
			httpPost.setHeader("Content-type", "application/json");
			response = new DefaultHttpClient().execute(httpPost);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK
					|| statusLine.getStatusCode() == HttpStatus.SC_CREATED) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
			} else {
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (ClientProtocolException e) {
			Log.d("com.talentuno.mynit", "error:" + e.getMessage());
			return "error:" + e.getMessage();
		} catch (IOException e) {
			Log.d("com.talentuno.mynit", "error:" + e.getMessage());
			return "error:" + e.getMessage();
		} catch (Exception e) {
			Log.d("com.talentuno.mynit", "error:" + e.getMessage());
			return "error:" + e.getMessage();
		}

		return responseString;
	}

}