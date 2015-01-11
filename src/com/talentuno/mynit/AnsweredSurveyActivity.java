package com.talentuno.mynit;

import java.util.ArrayList;
import java.util.Arrays;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AnsweredSurveyActivity extends Activity {

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
		new Thread( new NetworkAccess(latch, "http://54.148.201.55:7474/db/data/transaction/commit","{\"statements\" :[{\"statement\" : \"MATCH (survey:Survey{sid:{sid}}) return survey\",\"parameters\" : {\"sid\":\""+value+"\"}},{\"statement\" : \"MATCH (survey:Survey{sid:{sid}}),(survey)-[r:A_YES]->(yes:Yes) return count(yes)\",\"parameters\" : {\"sid\":\""+value+"\"}},{\"statement\" : \"MATCH (survey:Survey{sid:{sid}}),(survey)-[r:A_NO]->(no:No) return count(no)\",\"parameters\" : {\"sid\":\""+value+"\"}},{\"statement\" : \"MATCH (survey:Survey{sid:{sid}}),(survey)-[r:A_MAYBE]->(maybe:Maybe) return count(maybe)\",\"parameters\" : {\"sid\":\""+value+"\"}},{\"statement\" : \"MATCH (survey:Survey{sid:{sid}}) ,(survey)-[:A_COMMENT]->(comment:Comment) return comment\",\"parameters\" : {\"sid\":\""+value+"\"}}]}" )).start();
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
		sid = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("sid").getAsString();
		//String surveyMedia = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsJsonObject().get("media").getAsString();
		String yes = json.getAsJsonArray("results").get(1).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsString();
		String no = json.getAsJsonArray("results").get(2).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsString();
		String mayBe = json.getAsJsonArray("results").get(3).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsString();
		
		JsonArray comments = json.getAsJsonArray("results").get(4).getAsJsonObject().getAsJsonArray( "data" );
	
		Log.d( "com.talentuno.mynit" , "text:" + surveyText );
		Log.d( "com.talentuno.mynit" , "sid:" + sid );
		Log.d( "com.talentuno.mynit" , "yes:" + yes );
		Log.d( "com.talentuno.mynit" , "no:" + no );
		Log.d( "com.talentuno.mynit" , "maybe:" + mayBe );

		Log.d( "com.talentuno.mynit" , "comments:" + comments );
		
		ArrayList<Comment> commentsList = new ArrayList<Comment>();
		
		for( int i = 0 ; i < comments.size() ; i++ )
			commentsList.add( getComment( comments.get(i).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject() ) );
		
//		Log.d( "com.talentuno.mynit" , "comments:" + getComment( comments.get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject() ).toString() );
//		Log.d( "com.talentuno.mynit" , "comments:" + getComment( comments.get(1).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject() ).toString() );
//		Log.d( "com.talentuno.mynit" , "comments:" + getComment( comments.get(2).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject() ).toString() );
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_answered_survey);
		Button yesBtn = (Button) findViewById(R.id.yes_count);
		Button noBtn = (Button) findViewById(R.id.no_count);
		Button maybeBtn = (Button) findViewById(R.id.maybe_count);
		TextView surveyTextView = (TextView) findViewById(R.id.ans_survey_text);
		TextView numResponsesView = (TextView) findViewById(R.id.numResponses);
		
		long yesCount = Long.parseLong(yes);
		long noCount = Long.parseLong(no);
		long maybeCount = Long.parseLong(mayBe);
		
		surveyTextView.setText(surveyText);
		numResponsesView.setText(yesCount + noCount + maybeCount + " Responses");
		
		yesBtn.setText( Math.abs( yesCount*100/(yesCount + noCount + maybeCount) ) + "%" );
		noBtn.setText(Math.abs( noCount*100/(yesCount + noCount + maybeCount) ) + "%");
		maybeBtn.setText(Math.abs( maybeCount*100/(yesCount + noCount + maybeCount) ) + "%");
		
//        View linearLayout =  findViewById(R.id.chatWindow);
//		for( int j = 0 ; j < 5 ; j ++ )
//		for( int i = comments.size() -1 ; i >= 0 && i >= comments.size() -10 ; i-- ) {
//			
//			RelativeLayout r = new RelativeLayout(this);
//			r.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
//			r.setBackgroundColor(Color.YELLOW);
//			r.setPadding( 2,1,2,1 );
//			r.setId(i);
//			
//			TextView name = new TextView(this);
//			name.setText( commentsList.get(i).name );
//			name.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
//			name.setGravity(Gravity.TOP);
//			r.addView(name);
//			
////			View view = new View(this);
////			view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,1));
////			view.setBackgroundColor(Color.BLACK);
////			l.addView(view);
//			
//			TextView newText = new TextView(this);
//			newText.setText( commentsList.get(i).text );
//			newText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
//			newText.setGravity(Gravity.BOTTOM);
//			r.addView(newText);
//			
//	        ((LinearLayout) linearLayout).addView(r);
//			
//		}

		int commentIndex = commentsList.size() - 1;
		
		if( commentsList.size() >= 1 ) {
			
			findViewById(R.id.comLay1).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.name1)).setText(commentsList.get(commentIndex).name);
			((TextView) findViewById(R.id.comment1)).setText(commentsList.get(commentIndex).text);
			((TextView) findViewById(R.id.date1)).setText(commentsList.get(commentIndex--).date);
			
		}
		
		if( commentsList.size() >= 2 ) {
			
			findViewById(R.id.comLay2).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.name2)).setText(commentsList.get(commentIndex).name);
			((TextView) findViewById(R.id.comment2)).setText(commentsList.get(commentIndex).text);
			((TextView) findViewById(R.id.date2)).setText(commentsList.get(commentIndex--).date);
			
		}
		
		if( commentsList.size() >= 3 ) {
			
			findViewById(R.id.comLay3).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.name3)).setText(commentsList.get(commentIndex).name);
			((TextView) findViewById(R.id.comment3)).setText(commentsList.get(commentIndex).text);
			((TextView) findViewById(R.id.date3)).setText(commentsList.get(commentIndex--).date);
			
		}
		
		if( commentsList.size() >= 4 ) {
			
			findViewById(R.id.comLay4).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.name4)).setText(commentsList.get(commentIndex).name);
			((TextView) findViewById(R.id.comment4)).setText(commentsList.get(commentIndex).text);
			((TextView) findViewById(R.id.date4)).setText(commentsList.get(commentIndex--).date);
			
		}
		
		if( commentsList.size() >= 5 ) {
			
			findViewById(R.id.comLay5).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.name5)).setText(commentsList.get(commentIndex).name);
			((TextView) findViewById(R.id.comment5)).setText(commentsList.get(commentIndex).text);
			((TextView) findViewById(R.id.date5)).setText(commentsList.get(commentIndex--).date);
			
		}
		
		if( commentsList.size() >= 6 ) {
			
			findViewById(R.id.comLay6).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.name6)).setText(commentsList.get(commentIndex).name);
			((TextView) findViewById(R.id.comment6)).setText(commentsList.get(commentIndex).text);
			((TextView) findViewById(R.id.date6)).setText(commentsList.get(commentIndex--).date);
			
		}
		
		if( commentsList.size() >= 7 ) {
			
			findViewById(R.id.comLay7).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.name7)).setText(commentsList.get(commentIndex).name);
			((TextView) findViewById(R.id.comment7)).setText(commentsList.get(commentIndex).text);
			((TextView) findViewById(R.id.date7)).setText(commentsList.get(commentIndex--).date);
			
		}
		
		if( commentsList.size() >= 8 ) {
			
			findViewById(R.id.comLay8).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.name8)).setText(commentsList.get(commentIndex).name);
			((TextView) findViewById(R.id.comment8)).setText(commentsList.get(commentIndex).text);
			((TextView) findViewById(R.id.date8)).setText(commentsList.get(commentIndex--).date);
			
		}
		
		if( commentsList.size() >= 9 ) {
			
			findViewById(R.id.comLay9).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.name9)).setText(commentsList.get(commentIndex).name);
			((TextView) findViewById(R.id.comment9)).setText(commentsList.get(commentIndex).text);
			((TextView) findViewById(R.id.date9)).setText(commentsList.get(commentIndex--).date);
			
		}
		
		if( commentsList.size() >= 10 ) {
			
			findViewById(R.id.comLay10).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.name10)).setText(commentsList.get(commentIndex).name);
			((TextView) findViewById(R.id.comment10)).setText(commentsList.get(commentIndex).text);
			((TextView) findViewById(R.id.date10)).setText(commentsList.get(commentIndex--).date);
			
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.answered_survey, menu);
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
		if( id == R.id.create_poll) {
			createPoll();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void createPoll() {
		
		Intent intent = new Intent(this,CreatePollActivity.class);
		startActivity(intent);
		finish();
		
	}

	@SuppressWarnings("deprecation")
	public Comment getComment( JsonObject e ) {
		
		Comment c = new Comment();
		c.name = e.get("name").getAsString();
		c.uid = e.get("uid").getAsString();
		c.text = e.get("text").getAsString();
		c.text = c.text.replaceAll("<br/>","\n");
		c.enabled = e.get("enabled").getAsString();
		c.cid = e.get("cid").getAsString();
		c.date = e.get("date").getAsString();
		try {
		Date d = new Date( Long.parseLong(c.date) );
		ArrayList<String> months = new ArrayList<String>(Arrays.asList(new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"}));
		c.date = d.getDate() + " " + months.get(d.getMonth()) + " " + (1900 + d.getYear());
		}
		catch( NumberFormatException ex ) {}
		return c;
		
	}
	
	public void postComment( View view ) {
		
		EditText commentText = (EditText)findViewById(R.id.commentText);
		String comment = commentText.getText().toString();
		commentText.setText( "" );
		comment = comment.trim();
		
		if( comment.equals("") ) return;
		comment = comment.replaceAll("\n", "<br/>");
		
		SharedPreferences dataStore = getSharedPreferences(SID, MODE_PRIVATE);
		String uid = dataStore.getString( "uid" , null );
		String name = dataStore.getString( "name" , null );
		
		if( uid == null || name == null ) {
			
			Intent intent = new Intent(this,LoginActivity.class);
			startActivity(intent);
			finish();

		}
		
		long time = new Date().getTime();
		
		CountDownLatch latch = new CountDownLatch(1);
		NetworkAccess.result = null;
		System.out.println("{\"statements\" : [ {\"statement\" : \"CREATE (comment:Comment{props})\",\"parameters\" : {\"props\" : {\"uid\" : \""+uid+"\",\"name\" : \""+name+"\",\"enabled\":\"true\",\"date\":\""+time+"\",\"text\":\""+comment+"\",\"cid\":\""+uid+"_"+time+"\"}}},{\"statement\" : \"MATCH(comment:Comment{cid:{cid}}) MATCH(user:User{uid:{uid}}) CREATE UNIQUE (user)-[r:A_COMMENT]->(comment) RETURN id(r),id(comment)\",\"parameters\" : {\"uid\" : \""+uid+"\",\"cid\" : \""+uid+"_"+time+"\"}},{\"statement\" : \"MATCH(comment:Comment{cid:{cid}}) MATCH(survey:Survey{sid:{sid}}) CREATE UNIQUE (survey)-[r:A_COMMENT]->(comment) RETURN id(r)\",\"parameters\" : {\"sid\" : \""+sid+"\",\"cid\" : \""+uid+"_"+time+"\"}}]}");
		new Thread( new NetworkAccess(latch, "http://54.148.201.55:7474/db/data/transaction/commit","{\"statements\" : [ {\"statement\" : \"CREATE (comment:Comment{props})\",\"parameters\" : {\"props\" : {\"uid\" : \""+uid+"\",\"name\" : \""+name+"\",\"enabled\":\"true\",\"date\":\""+time+"\",\"text\":\""+comment+"\",\"cid\":\""+uid+"_"+time+"\"}}},{\"statement\" : \"MATCH(comment:Comment{cid:{cid}}) MATCH(user:User{uid:{uid}}) CREATE UNIQUE (user)-[r:A_COMMENT]->(comment) RETURN id(r),id(comment)\",\"parameters\" : {\"uid\" : \""+uid+"\",\"cid\" : \""+uid+"_"+time+"\"}},{\"statement\" : \"MATCH(comment:Comment{cid:{cid}}) MATCH(survey:Survey{sid:{sid}}) CREATE UNIQUE (survey)-[r:A_COMMENT]->(comment) RETURN id(r)\",\"parameters\" : {\"sid\" : \""+sid+"\",\"cid\" : \""+uid+"_"+time+"\"}}]}" )).start();
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
		
		Intent intent = new Intent(this, AnsweredSurveyActivity.class);
		intent.putExtra(SID, sid);
		startActivity(intent);
//		overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
		finish();
		
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
		
	    Intent intent = new Intent(this, HomeActivity.class);
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
		
	    Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		finish();
		
	}
	
	class Comment{
		
		public String text;
		public String cid;
		public String name;
		public String uid;
		public String enabled;
		public String date;
		
		@Override
		public String toString() {
			return "Comment [text=" + text + ", cid=" + cid + ", name=" + name
					+ ", uid=" + uid + ", enabled=" + enabled + ", date="
					+ date + "]";
		}
		
	}
	
}