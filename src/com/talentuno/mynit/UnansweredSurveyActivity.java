package com.talentuno.mynit;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class UnansweredSurveyActivity extends Activity {

    static final String SID = "com.talentuno.mynit";
    private String sid;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		String value = null;
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    value = extras.getString(SID);
		}
		
		if( value == null ) {
			
			Intent intent = new Intent(this, HomeActivity.class);
			startActivity(intent);
			finish();
			
		}
		
		CountDownLatch latch = new CountDownLatch(1);
		NetworkAccess.result = null;
		new Thread( new NetworkAccess(latch, "http://54.148.201.55:7474/db/data/transaction/commit","{\"statements\" : [ {\"statement\" : \"MATCH (survey:Survey{sid:{sid}}) return survey\",\"parameters\" : {\"sid\":\""+value+"\"}} ]}" )).start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			Log.d( "com.talentuno.mynit" , e.getMessage() );
		}
		
		if( NetworkAccess.result == null ) {
			
			Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
			finish();
			
		}
		
		JsonObject json = new JsonParser().parse( NetworkAccess.result ).getAsJsonObject();
		JsonArray errors = json.getAsJsonArray( "errors" );
		if( errors.size() != 0 ) {
			
			Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
			Log.d( "com.talentuno.mynit" , "errors:" + errors );
			finish();
			
		}
		
		String surveyText = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("text").getAsString();
		String surveyEnabled = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("enabled").getAsString();
		sid = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("sid").getAsString();
		
		Log.d( "com.talentuno.mynit" , "text:" + surveyText );
		Log.d( "com.talentuno.mynit" , "enabled:" + surveyEnabled );
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unanswered_survey);
		
		TextView textView = (TextView) findViewById(R.id.survey_text_unanswered);
		textView.setText( surveyText );

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.unanswered_survey, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void yes( View view ) {
		
		SharedPreferences dataStore = getSharedPreferences(SID, MODE_PRIVATE);
		String uid = dataStore.getString("uid", null);
		if( uid == null ) {
			
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
			
		}
		
		Long time = new Date().getTime();
		
		CountDownLatch latch = new CountDownLatch(1);
		NetworkAccess.result = null;
		new Thread( new NetworkAccess(latch, "http://54.148.201.55:7474/db/data/transaction/commit","{\"statements\" : [ {\"statement\":\"MATCH (survey:Survey{sid:{sid}}),(user:User{uid:{uid}}),(survey)-[r1:A_YES|A_NO|A_MAYBE]->(n)<-[r2:A_YES|A_NO|A_MAYBE]-(user) DELETE r1, r2, n\",\"parameters\":{\"sid\":\""+sid+"\",\"uid\":\""+uid+"\"}} , {\"statement\" : \"CREATE (yes:Yes{props}) return yes\",\"parameters\":{\"props\":{\"uid\":\""+uid+"\",\"time\":\""+time+"\"}}},{\"statement\" : \"MATCH (yes:Yes{time:{time},uid:{uid}}),(survey:Survey{sid:{sid}}) CREATE UNIQUE (survey)-[r:A_YES]->(yes) return r\",\"parameters\" : {\"sid\":\""+sid+"\",\"time\":\""+time+"\",\"uid\":\""+uid+"\"}},{\"statement\" : \"MATCH (yes:Yes{uid:{uid},time:{time}}),(user:User{uid:{uid}}) CREATE UNIQUE (user)-[r1:A_YES]->(yes) return r1\",\"parameters\" : {\"uid\":\""+uid+"\",\"time\":\""+time+"\"}} ]}" )).start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			Log.d( "com.talentuno.mynit" , e.getMessage() );
		}
		
		if( NetworkAccess.result == null ) {
			
			Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
			finish();
			
		}
		
		JsonObject json = new JsonParser().parse( NetworkAccess.result ).getAsJsonObject();
		JsonArray errors = json.getAsJsonArray( "errors" );
		if( errors.size() != 0 ) {
			
			Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
			Log.d( "com.talentuno.mynit" , "errors:" + errors );
			finish();
			
		}
		
		String surveys = dataStore.getString("surveys", null );
		SharedPreferences.Editor editor = dataStore.edit();
		if( surveys != null )
			editor.putString("surveys", surveys + "," + sid );
		else
			editor.putString("surveys", sid );
	    editor.commit();
	    
	    Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		finish();
		
	}
	
	public void no( View view ) {
		
		SharedPreferences dataStore = getSharedPreferences(SID, MODE_PRIVATE);
		String uid = dataStore.getString("uid", null);
		if( uid == null ) {
			
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
			
		}
		
		Long time = new Date().getTime();
		
		CountDownLatch latch = new CountDownLatch(1);
		NetworkAccess.result = null;
		new Thread( new NetworkAccess(latch, "http://54.148.201.55:7474/db/data/transaction/commit","{\"statements\" : [ {\"statement\":\"MATCH (survey:Survey{sid:{sid}}),(user:User{uid:{uid}}),(survey)-[r1:A_YES|A_NO|A_MAYBE]->(n)<-[r2:A_YES|A_NO|A_MAYBE]-(user) DELETE r1, r2, n\",\"parameters\":{\"sid\":\""+sid+"\",\"uid\":\""+uid+"\"}} , {\"statement\" : \"CREATE (no:No{props}) return no\",\"parameters\":{\"props\":{\"uid\":\""+uid+"\",\"time\":\""+time+"\"}}},{\"statement\" : \"MATCH (no:No{time:{time},uid:{uid}}),(survey:Survey{sid:{sid}}) CREATE UNIQUE (survey)-[r:A_NO]->(no) return r\",\"parameters\" : {\"sid\":\""+sid+"\",\"time\":\""+time+"\",\"uid\":\""+uid+"\"}},{\"statement\" : \"MATCH (no:No{uid:{uid},time:{time}}),(user:User{uid:{uid}}) CREATE UNIQUE (user)-[r1:A_NO]->(no) return r1\",\"parameters\" : {\"uid\":\""+uid+"\",\"time\":\""+time+"\"}} ]}" )).start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			Log.d( "com.talentuno.mynit" , e.getMessage() );
		}
		
		if( NetworkAccess.result == null ) {
			
			Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
			finish();
			
		}
		
		JsonObject json = new JsonParser().parse( NetworkAccess.result ).getAsJsonObject();
		JsonArray errors = json.getAsJsonArray( "errors" );
		if( errors.size() != 0 ) {
			
			Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
			Log.d( "com.talentuno.mynit" , "errors:" + errors );
			finish();
			
		}
		
		String surveys = dataStore.getString("surveys", null );
		SharedPreferences.Editor editor = dataStore.edit();
		if( surveys != null )
			editor.putString("surveys", surveys + "," + sid );
		else
			editor.putString("surveys", sid );
	    editor.commit();
	    
	    Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
		
	}
	
	public void maybe( View view ) {
		
		SharedPreferences dataStore = getSharedPreferences(SID, MODE_PRIVATE);
		String uid = dataStore.getString("uid", null);
		if( uid == null ) {
			
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
			
		}
		
		Long time = new Date().getTime();
		
		CountDownLatch latch = new CountDownLatch(1);
		NetworkAccess.result = null;
		new Thread( new NetworkAccess(latch, "http://54.148.201.55:7474/db/data/transaction/commit","{\"statements\" : [ {\"statement\":\"MATCH (survey:Survey{sid:{sid}}),(user:User{uid:{uid}}),(survey)-[r1:A_YES|A_NO|A_MAYBE]->(n)<-[r2:A_YES|A_NO|A_MAYBE]-(user) DELETE r1, r2, n\",\"parameters\":{\"sid\":\""+sid+"\",\"uid\":\""+uid+"\"}} , {\"statement\" : \"CREATE (maybe:Maybe{props}) return maybe\",\"parameters\":{\"props\":{\"uid\":\""+uid+"\",\"time\":\""+time+"\"}}},{\"statement\" : \"MATCH (maybe:Maybe{time:{time},uid:{uid}}),(survey:Survey{sid:{sid}}) CREATE UNIQUE (survey)-[r:A_MAYBE]->(maybe) return r\",\"parameters\" : {\"sid\":\""+sid+"\",\"time\":\""+time+"\",\"uid\":\""+uid+"\"}},{\"statement\" : \"MATCH (maybe:Maybe{uid:{uid},time:{time}}),(user:User{uid:{uid}}) CREATE UNIQUE (user)-[r1:A_MAYBE]->(maybe) return r1\",\"parameters\" : {\"uid\":\""+uid+"\",\"time\":\""+time+"\"}} ]}" )).start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			Log.d( "com.talentuno.mynit" , e.getMessage() );
		}
		
		if( NetworkAccess.result == null ) {
			
			Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
			finish();
			
		}
		
		JsonObject json = new JsonParser().parse( NetworkAccess.result ).getAsJsonObject();
		JsonArray errors = json.getAsJsonArray( "errors" );
		if( errors.size() != 0 ) {
			
			Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
			Log.d( "com.talentuno.mynit" , "errors:" + errors );
			finish();
			
		}
		
		String surveys = dataStore.getString("surveys", null );
		SharedPreferences.Editor editor = dataStore.edit();
		if( surveys != null )
			editor.putString("surveys", surveys + "," + sid );
		else
			editor.putString("surveys", sid );
	    editor.commit();
	    
	    Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
		
	}

}
