package com.talentuno.mynit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class DatabaseQuery extends AsyncTask<String, String, String> {

	public static String result;
	private ProgressDialog dialog;
	private Context callerContext;
	testinterface testInterface;
	public DatabaseQuery(testinterface testInterface) {
	this.testInterface = testInterface;
	}
	
	

	@Override
	protected void onPreExecute() {

		dialog = ProgressDialog.show(callerContext, "", "Please Wait", false);

	}

	public DatabaseQuery(Context callerContext) {

		this.callerContext = callerContext;

	}

	private String doGet(String uri) {

		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		String responseString = null;
		try {
			response = httpclient.execute(new HttpGet(uri));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
			} else {
				// Closes the connection.
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (ClientProtocolException e) {
			Log.d("com.talentuno.mynit", e.getMessage());
			return null;
		} catch (IOException e) {
			Log.d("com.talentuno.mynit", e.getMessage());
			return null;
		}
		return responseString;

	}

	private String doPost(String uri, String params) {

		HttpPost httpPost = new HttpPost(uri);

		if (params != null) {

			try {
				httpPost.setEntity(new StringEntity(params));
			} catch (UnsupportedEncodingException e) {
				Log.d("com.talentuno.mynit", e.getMessage());
				return null;
			}

		}

		HttpResponse response;
		String responseString = null;
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
				// Closes the connection.
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (ClientProtocolException e) {
			Log.d("com.talentuno.mynit", e.getMessage());
			return null;
		} catch (IOException e) {
			Log.d("com.talentuno.mynit", e.getMessage());
			return null;
		}
		return responseString;

	}

	@Override
	protected String doInBackground(String... uri) {

		String result = null;

		if (uri[1].equals("GET"))
			result = doGet(uri[0]);
		else if (uri[1].equals("POST"))
			if (uri.length > 2)
				result = doPost(uri[0], uri[2]);
			else
				result = doPost(uri[0], null);

		DatabaseQuery.result = result;
		return result;

	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		Log.d("com.talentuno.mynit", "Received:" + result);
		dialog.dismiss();
		testInterface.test(result);
	}
}