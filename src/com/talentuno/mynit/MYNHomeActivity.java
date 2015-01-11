package com.talentuno.mynit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.talentuno.mynit.ContactsActivity.ContactArrayAdapterNew;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MYNHomeActivity extends ListActivity {

    static final String SID = "com.talentuno.mynit";
    static ArrayList<Survey> SURVEYS = new ArrayList<Survey>();

    @Override
	public void onResume() {
		
		super.onResume();
		CountDownLatch latch = new CountDownLatch(1);
		NetworkAccess.result = null;
		new Thread( new NetworkAccess(latch, "http://54.148.201.55:7474/db/data/transaction/commit","{\"statements\":[{\"statement\":\"MATCH (user:User{uid:{uid}}) MATCH (user)-[r:A_SURVEY_REQUEST]->(survey:Survey) return survey.sid\",\"parameters\":{\"uid\":\"911234567890\"}}]}" )).start();
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
		
		ArrayList<String> sidList = new ArrayList<String>();
		
		JsonArray sids = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" );
		Log.d( "com.talentuno.mynit" , "comments:" + sids );
		
		for( int i = 0 ; i < sids.size() ; i++ )
			sidList.add( sids.get(i).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsString() );
		
		SURVEYS.clear();
		sidList.clear();sidList.add("date_time");
		
		for( String sid : sidList ) {
			
			latch = new CountDownLatch(1);
			NetworkAccess.result = null;
			new Thread( new NetworkAccess(latch, "http://54.148.201.55:7474/db/data/transaction/commit","{\"statements\" :[{\"statement\" : \"MATCH (survey:Survey{sid:{sid}}) return survey\",\"parameters\" : {\"sid\":\""+sid+"\"}},{\"statement\" : \"MATCH (survey:Survey{sid:{sid}}),(survey)-[r:A_YES]->(yes:Yes) return count(yes)\",\"parameters\" : {\"sid\":\""+sid+"\"}},{\"statement\" : \"MATCH (survey:Survey{sid:{sid}}),(survey)-[r:A_NO]->(no:No) return count(no)\",\"parameters\" : {\"sid\":\""+sid+"\"}},{\"statement\" : \"MATCH (survey:Survey{sid:{sid}}),(survey)-[r:A_MAYBE]->(maybe:Maybe) return count(maybe)\",\"parameters\" : {\"sid\":\""+sid+"\"}},{\"statement\" : \"MATCH (survey:Survey{sid:{sid}}) ,(survey)-[:A_COMMENT]->(comment:Comment) return comment\",\"parameters\" : {\"sid\":\""+sid+"\"}}]}" )).start();
			try {
				latch.await();
			} catch (InterruptedException e) {
				Log.d( "com.talentuno.mynit" , e.getMessage() );
			}
			
			if( NetworkAccess.result == null ) {
				
				Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
				finish();
				
			}
			
			json = new JsonParser().parse( NetworkAccess.result ).getAsJsonObject();
			errors = json.getAsJsonArray( "errors" );
			if( errors.size() != 0 ) {
				
				Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
				Log.d( "com.talentuno.mynit" , "errors:" + errors );
				finish();
				
			}
			
			String surveyText = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("text").getAsString();
			String requestedBy = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("uid").getAsString();
			String enabled = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("enabled").getAsString();
			//String surveyMedia = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("media").getAsString();
			String yes = json.getAsJsonArray("results").get(1).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsString();
			String no = json.getAsJsonArray("results").get(2).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsString();
			String maybe = json.getAsJsonArray("results").get(3).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsString();
			
			JsonArray comments = json.getAsJsonArray("results").get(4).getAsJsonObject().getAsJsonArray( "data" );
		
			Log.d( "com.talentuno.mynit" , "text:" + surveyText );
			Log.d( "com.talentuno.mynit" , "sid:" + sid );
			Log.d( "com.talentuno.mynit" , "yes:" + yes );
			Log.d( "com.talentuno.mynit" , "no:" + no );
			Log.d( "com.talentuno.mynit" , "maybe:" + maybe );

			Log.d( "com.talentuno.mynit" , "comments:" + comments );
			
			SharedPreferences dataStore = getSharedPreferences(SID, MODE_PRIVATE);
			ArrayList<String> surveys = new ArrayList<String>(Arrays.asList(dataStore.getString( "surveys", "NA" ).split(",")));
			
			boolean surveyTaken = false;
			
			if( surveys.contains( sid ) ) {
				
				surveyTaken = true;
				
			}
			
			SURVEYS.add( new Survey(surveyText, Boolean.parseBoolean(enabled), surveyTaken, requestedBy, "" + comments.size() , sid, Long.parseLong(yes), Long.parseLong(no), Long.parseLong(maybe)));
			
		}
		
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Survey[] surveys = SURVEYS.toArray( new Survey[]{} );;        
        
		super.onCreate(savedInstanceState);
 		setListAdapter(new SurveyListAdapter(this, surveys));
 		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mynhome, menu);
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
	
	class Survey {
		
		String surveyText;
		boolean enabled;
		boolean surveyTaken;
		String requestedBy;
		String comments;
		String sid;
		long yes;
		long no;
		long maybe;
		
		public Survey(String surveyText, boolean enabled, boolean surveyTaken, String requestedBy, String comments, String sid, long yes, long no, long maybe) {
			this.surveyText = surveyText.replaceAll( "<br/>" , "\n");
			this.enabled = enabled;
			this.surveyTaken = surveyTaken;
			this.requestedBy = requestedBy;
			this.comments = comments;
			this.sid = sid;
			this.yes = yes;
			this.no = no;
			this.maybe = maybe;
		}
		
	}
	
//	public void yes( View view ) {
//		
//		Button b = (Button) view;
//		System.out.println( b.getTag() );
//		SharedPreferences dataStore = getSharedPreferences(SID, MODE_PRIVATE);
//		String uid = dataStore.getString("uid", null);
//		if( uid == null ) {
//			
//			Intent intent = new Intent(this, LoginActivity.class);
//			startActivity(intent);
//			finish();
//			
//		}
//		
//		Long time = new Date().getTime();
//		
//		CountDownLatch latch = new CountDownLatch(1);
//		NetworkAccess.result = null;
//		new Thread( new NetworkAccess(latch, "http://54.148.201.55:7474/db/data/transaction/commit","{\"statements\" : [ {\"statement\":\"MATCH (survey:Survey{sid:{sid}}),(user:User{uid:{uid}}),(survey)-[r1:A_YES|A_NO|A_MAYBE]->(n)<-[r2:A_YES|A_NO|A_MAYBE]-(user) DELETE r1, r2, n\",\"parameters\":{\"sid\":\""+sid+"\",\"uid\":\""+uid+"\"}} , {\"statement\" : \"CREATE (yes:Yes{props}) return yes\",\"parameters\":{\"props\":{\"uid\":\""+uid+"\",\"time\":\""+time+"\"}}},{\"statement\" : \"MATCH (yes:Yes{time:{time},uid:{uid}}),(survey:Survey{sid:{sid}}) CREATE UNIQUE (survey)-[r:A_YES]->(yes) return r\",\"parameters\" : {\"sid\":\""+sid+"\",\"time\":\""+time+"\",\"uid\":\""+uid+"\"}},{\"statement\" : \"MATCH (yes:Yes{uid:{uid},time:{time}}),(user:User{uid:{uid}}) CREATE UNIQUE (user)-[r1:A_YES]->(yes) return r1\",\"parameters\" : {\"uid\":\""+uid+"\",\"time\":\""+time+"\"}} ]}" )).start();
//		try {
//			latch.await();
//		} catch (InterruptedException e) {
//			Log.d( "com.talentuno.mynit" , e.getMessage() );
//		}
//		
//		if( NetworkAccess.result == null ) {
//			
//			Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
//			finish();
//			
//		}
//		
//		JsonObject json = new JsonParser().parse( NetworkAccess.result ).getAsJsonObject();
//		JsonArray errors = json.getAsJsonArray( "errors" );
//		if( errors.size() != 0 ) {
//			
//			Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
//			Log.d( "com.talentuno.mynit" , "errors:" + errors );
//			finish();
//			
//		}
//		
//	    Intent intent = new Intent(this, HomeActivity.class);
//		startActivity(intent);
//		finish();
//		
//	}
	
//	public void no( View view ) {
//		
//		SharedPreferences dataStore = getSharedPreferences(SID, MODE_PRIVATE);
//		String uid = dataStore.getString("uid", null);
//		if( uid == null ) {
//			
//			Intent intent = new Intent(this, LoginActivity.class);
//			startActivity(intent);
//			finish();
//			
//		}
//		
//		Long time = new Date().getTime();
//		
//		CountDownLatch latch = new CountDownLatch(1);
//		NetworkAccess.result = null;
//		new Thread( new NetworkAccess(latch, "http://54.148.201.55:7474/db/data/transaction/commit","{\"statements\" : [ {\"statement\":\"MATCH (survey:Survey{sid:{sid}}),(user:User{uid:{uid}}),(survey)-[r1:A_YES|A_NO|A_MAYBE]->(n)<-[r2:A_YES|A_NO|A_MAYBE]-(user) DELETE r1, r2, n\",\"parameters\":{\"sid\":\""+sid+"\",\"uid\":\""+uid+"\"}} , {\"statement\" : \"CREATE (no:No{props}) return no\",\"parameters\":{\"props\":{\"uid\":\""+uid+"\",\"time\":\""+time+"\"}}},{\"statement\" : \"MATCH (no:No{time:{time},uid:{uid}}),(survey:Survey{sid:{sid}}) CREATE UNIQUE (survey)-[r:A_NO]->(no) return r\",\"parameters\" : {\"sid\":\""+sid+"\",\"time\":\""+time+"\",\"uid\":\""+uid+"\"}},{\"statement\" : \"MATCH (no:No{uid:{uid},time:{time}}),(user:User{uid:{uid}}) CREATE UNIQUE (user)-[r1:A_NO]->(no) return r1\",\"parameters\" : {\"uid\":\""+uid+"\",\"time\":\""+time+"\"}} ]}" )).start();
//		try {
//			latch.await();
//		} catch (InterruptedException e) {
//			Log.d( "com.talentuno.mynit" , e.getMessage() );
//		}
//		
//		if( NetworkAccess.result == null ) {
//			
//			Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
//			finish();
//			
//		}
//		
//		JsonObject json = new JsonParser().parse( NetworkAccess.result ).getAsJsonObject();
//		JsonArray errors = json.getAsJsonArray( "errors" );
//		if( errors.size() != 0 ) {
//			
//			Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
//			Log.d( "com.talentuno.mynit" , "errors:" + errors );
//			finish();
//			
//		}
//		
//	    Intent intent = new Intent(this, LoginActivity.class);
//		startActivity(intent);
//		finish();
//		
//	}
//	
//	public void maybe( View view ) {
//		
//		SharedPreferences dataStore = getSharedPreferences(SID, MODE_PRIVATE);
//		String uid = dataStore.getString("uid", null);
//		if( uid == null ) {
//			
//			Intent intent = new Intent(this, LoginActivity.class);
//			startActivity(intent);
//			finish();
//			
//		}
//		
//		Long time = new Date().getTime();
//		
//		CountDownLatch latch = new CountDownLatch(1);
//		NetworkAccess.result = null;
//		new Thread( new NetworkAccess(latch, "http://54.148.201.55:7474/db/data/transaction/commit","{\"statements\" : [ {\"statement\":\"MATCH (survey:Survey{sid:{sid}}),(user:User{uid:{uid}}),(survey)-[r1:A_YES|A_NO|A_MAYBE]->(n)<-[r2:A_YES|A_NO|A_MAYBE]-(user) DELETE r1, r2, n\",\"parameters\":{\"sid\":\""+sid+"\",\"uid\":\""+uid+"\"}} , {\"statement\" : \"CREATE (maybe:Maybe{props}) return maybe\",\"parameters\":{\"props\":{\"uid\":\""+uid+"\",\"time\":\""+time+"\"}}},{\"statement\" : \"MATCH (maybe:Maybe{time:{time},uid:{uid}}),(survey:Survey{sid:{sid}}) CREATE UNIQUE (survey)-[r:A_MAYBE]->(maybe) return r\",\"parameters\" : {\"sid\":\""+sid+"\",\"time\":\""+time+"\",\"uid\":\""+uid+"\"}},{\"statement\" : \"MATCH (maybe:Maybe{uid:{uid},time:{time}}),(user:User{uid:{uid}}) CREATE UNIQUE (user)-[r1:A_MAYBE]->(maybe) return r1\",\"parameters\" : {\"uid\":\""+uid+"\",\"time\":\""+time+"\"}} ]}" )).start();
//		try {
//			latch.await();
//		} catch (InterruptedException e) {
//			Log.d( "com.talentuno.mynit" , e.getMessage() );
//		}
//		
//		if( NetworkAccess.result == null ) {
//			
//			Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
//			finish();
//			
//		}
//		
//		JsonObject json = new JsonParser().parse( NetworkAccess.result ).getAsJsonObject();
//		JsonArray errors = json.getAsJsonArray( "errors" );
//		if( errors.size() != 0 ) {
//			
//			Toast.makeText(getApplicationContext(), "No network connectivity...", Toast.LENGTH_LONG).show();
//			Log.d( "com.talentuno.mynit" , "errors:" + errors );
//			finish();
//			
//		}
//		
//	    Intent intent = new Intent(this, LoginActivity.class);
//		startActivity(intent);
//		finish();
//		
//	}
	
	class SurveyListAdapter extends ArrayAdapter<Survey> {
		private final Context context;
		private final Survey[] values;
	 
		public SurveyListAdapter(Context context, Survey[] values) {
			super(context, R.layout.survey_list, values);
			this.context = context;
			this.values = values;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 
			View rowView = inflater.inflate(R.layout.survey_list, parent, false);
			TextView surveyText = (TextView) rowView.findViewById(R.id.survey_text);
			ImageView enabledSurvey = (ImageView) rowView.findViewById(R.id.enabled_survey);
			ImageView disabledSurvey = (ImageView) rowView.findViewById(R.id.disabled_survey);
			ImageView surveyTaken = (ImageView) rowView.findViewById(R.id.survey_taken);
			ImageView visitSurvey = (ImageView) rowView.findViewById(R.id.visit_survey);
			ImageView profilePic = (ImageView) rowView.findViewById(R.id.profile_pic);
			TextView requestedBy = (TextView) rowView.findViewById(R.id.requested_by);
			TextView numResponses = (TextView) rowView.findViewById(R.id.numResponses);
			Button comments = (Button) rowView.findViewById(R.id.comments);
			Button yesButton = (Button) rowView.findViewById(R.id.yes_count);
			Button noButton = (Button) rowView.findViewById(R.id.no_count);
			Button maybeButton = (Button) rowView.findViewById(R.id.maybe_count);
			
			surveyText.setText(values[position].surveyText);
			if(values[position].enabled) enabledSurvey.setVisibility(View.VISIBLE);
			else disabledSurvey.setVisibility(View.VISIBLE);
			if(values[position].surveyTaken) surveyTaken.setVisibility(View.VISIBLE);
			else surveyTaken.setVisibility(View.GONE);
			requestedBy.setText(values[position].requestedBy);
			comments.setText(values[position].comments);

			comments.setTag(values[position].sid);
			yesButton.setTag(values[position].sid);
			noButton.setTag(values[position].sid);
			maybeButton.setTag(values[position].sid);
			visitSurvey.setTag(values[position].sid);
			
			SharedPreferences dataStore = getSharedPreferences(SID, MODE_PRIVATE);
			ArrayList<String> surveys = new ArrayList<String>(Arrays.asList(dataStore.getString( "surveys", "NA" ).split(",")));
			
			if( ! surveys.contains( values[position].sid ) ) {
				
				yesButton.setVisibility(View.GONE);
				noButton.setVisibility(View.GONE);
				maybeButton.setVisibility(View.GONE);
				numResponses.setText( "WHAT DO YOU THINK" );
				
			}
			
			else {
				
				yesButton.setText( Math.abs( values[position].yes*100/(values[position].yes + values[position].no + values[position].maybe) ) + "%" );
				noButton.setText( Math.abs( values[position].no*100/(values[position].yes + values[position].no + values[position].maybe) ) + "%" );
				maybeButton.setText( Math.abs( values[position].maybe*100/(values[position].yes + values[position].no + values[position].maybe) ) + "%" );
				numResponses.setText((values[position].yes + values[position].no + values[position].maybe) + " Responses");
				
			}

			comments.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {

					Button b = (Button)v;
					String sid = b.getTag().toString();
					Intent intent = new Intent(getApplicationContext(), AnsweredSurveyActivity.class);
					intent.putExtra(SID, sid);
					startActivity(intent);
					finish();
					
				}
			});

			visitSurvey.setOnClickListener(new View.OnClickListener() {
	
				@Override
				public void onClick(View v) {

					ImageView i = (ImageView)v;
					String sid = i.getTag().toString();
					Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
					intent.putExtra(SID, sid);
					startActivity(intent);
					finish();
					
				}
			});

			maybeButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {

					Button b = (Button)v;
					String sid = b.getTag().toString();
					SharedPreferences dataStore = getSharedPreferences(SID, MODE_PRIVATE);
					String uid = dataStore.getString("uid", null);
					if( uid == null ) {
						
						Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
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
					
				    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
					intent.putExtra(SID, sid);
					startActivity(intent);
					finish();
					
				}
			});

			noButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {

					Button b = (Button)v;
					String sid = b.getTag().toString();
					SharedPreferences dataStore = getSharedPreferences(SID, MODE_PRIVATE);
					String uid = dataStore.getString("uid", null);
					if( uid == null ) {
						
						Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
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
					
				    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
					intent.putExtra(SID, sid);
					startActivity(intent);
					finish();
					
				}
			});
			
			yesButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {

					Button b = (Button)v;
					String sid = b.getTag().toString();
					SharedPreferences dataStore = getSharedPreferences(SID, MODE_PRIVATE);
					String uid = dataStore.getString("uid", null);
					if( uid == null ) {
						
						Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
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
					
				    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
					intent.putExtra(SID, sid);
					startActivity(intent);
					finish();
					
				}
			});
			
			return rowView;
		}
		
	}

}
