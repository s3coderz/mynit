package com.talentuno.mynit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GroupActivity extends Activity {
	
	public static final String DATA_STORE = "com.talentuno.mynit";
	public HashMap<String, String> groups;
	static final String GROUP_NAME = "com.talentuno.mynit.group.name";
	public static HashMap<String, String> SELECTED_GROUPS;
	static final String SURVEY_QUESTION = "com.talentuno.mynit.survey.question";
	static final String ALLOW_INVITE = "com.talentuno.mynit.allow.invite";
	
	String surveyText = null;
	String allowInvite = null;
	
	private void setUpDatas() {
		
		SharedPreferences dataStore = getSharedPreferences(DATA_STORE, MODE_PRIVATE);
		String groupList = dataStore.getString( "groups" , null );
		groups = new HashMap<String,String>();
		if( groupList != null ) {
			
			for( String s : groupList.split( "," ) ) {
				
				String groupName = s.trim();
				if( groupName.equals("")) continue;
				String groupMembers = dataStore.getString( "group." + groupName + ".members" , null );
				if( groupMembers == null ) continue;
				groups.put( groupName , groupMembers );
				
			}
			
		}
		
//		ListView lv = (ListView)findViewById(android.R.id.list);
//		String[] items = new String[] {"Item 1", "Item 2", "Item 3"};
//	    ArrayAdapter<String> adapter =
//	      new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
//		lv.setAdapter(adapter);
//		lv.setOnItemClickListener(new OnItemClickListener()
//		{
//		     @Override
//		     public void onItemClick(AdapterView<?> a, View v,int position, long id) 
//		     {
//		          Toast.makeText(getBaseContext(), "Click", Toast.LENGTH_LONG).show();
//		      }
//		});
		
		SELECTED_GROUPS = new HashMap<String,String>();
        ArrayList<String> temp = new ArrayList<String>();
        for( String s : groups.keySet() )
        	temp.add( s + ":" + groups.get(s) );
        final String[] GROUPS = temp.toArray( new String[]{} );
        ListView lv = (ListView)findViewById(android.R.id.list);
	    lv.setAdapter(new GroupArrayAdapter(this, GROUPS));
		
	}
	
	@Override
	public void onResume() {
		
		super.onResume();
		setUpDatas();
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group);
		setUpDatas();
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			surveyText = extras.getString(SURVEY_QUESTION);
			allowInvite = extras.getString(ALLOW_INVITE);
		}
		
		if( surveyText == null || allowInvite == null ) {
			
			Intent intent = new Intent(this, HomeActivity.class);
			startActivity(intent);
			finish();
			
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group, menu);
		return true;
	}
	
	public void addFriends( View view ) {
		
		String groupName = null;
		EditText et = (EditText) findViewById(R.id.group_name);
		groupName = et.getText().toString();
		if( groupName == null || groupName.trim().equals("") ) {
			
			Toast.makeText(getApplicationContext(), "Please enter group name", Toast.LENGTH_LONG).show();
			return;
			
		}
		if( groups.containsKey( groupName.trim() ) ) {
			
			Toast.makeText(getApplicationContext(), "Group name " + groupName.trim() + " already exists", Toast.LENGTH_LONG).show();
			return;
			
		}
		
		Intent intent = new Intent(this, ContactsActivity.class);
		intent.putExtra(GROUP_NAME, groupName.trim() );
		startActivity(intent);
		
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
	
	public void createSurvey( View view ) {
		
		surveyText = surveyText.replaceAll("\n", "<br/>");
		
		SharedPreferences dataStore = getSharedPreferences(DATA_STORE, MODE_PRIVATE);
		String uid = dataStore.getString( "uid" , null );
		
		if( uid == null ) {
			
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
			
		}
		
		String sid = uid + "_" + new Date().getTime();
		String cc = uid.substring(0, uid.length() - 10 );
		String statements = "";
		
		HashMap<String, String> contacts  = new HashMap<String,String>();
		ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,null, null);

        while (cur.moveToNext()) {

            String name =cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));    
            String phoneNumber = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if(! contacts.containsKey(name)) contacts.put(name, phoneNumber);
        
        }
		
        for( String group : SELECTED_GROUPS.keySet() ) {
        	
        	String members = SELECTED_GROUPS.get(group);
        	for( String user : members.split( "," ) ) {
        		
        		String phNumber = contacts.get(user);
        		phNumber = phNumber.replaceAll( "\\+" , "" );
        		phNumber = phNumber.replaceAll( "\\-" , "" );
        		phNumber = phNumber.replaceAll( "\\s+" , "" );
        		if( phNumber.length() < 10 ) continue;
        		if( phNumber.length() == 10 ) phNumber = cc + phNumber;
        		statements += ",{\"statement\":\"MATCH (survey:Survey{sid:{sid}}) MATCH (user:User{uid:{uid}}) CREATE UNIQUE (user)-[r:A_SURVEY_REQUEST]->(survey) RETURN id(r)\",\"parameters\" : {\"uid\":\""+phNumber+"\",\"sid\":\""+sid+"\"}}";
        		
        	}
        	
        }
        
        new BackgroundTask().execute("http://54.148.201.55:7474/db/data/transaction/commit","{\"statements\":[{\"statement\":\"MATCH (root:ROOT) CREATE (survey:Survey{props}) CREATE UNIQUE (root)-[r:A_SURVEY]->(survey) return id(survey),id(r)\",\"parameters\":{\"props\":{\"sid\":\""+sid+"\",\"text\":\""+surveyText+"\",\"uid\":\""+uid+"\",\"enabled\":\"true\",\"allowInvite\":\""+allowInvite+"\"}}},{\"statement\":\"MATCH (survey:Survey{sid:{sid}}) MATCH (user:User{uid:{uid}}) CREATE UNIQUE (user)-[r:A_SURVEY]->(survey) RETURN id(r)\",\"parameters\":{\"uid\":\""+uid+"\",\"sid\":\""+sid+"\"}}"+statements+"]}");
        
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		finish();
		
	}
	
	public void cancelSurvey( View view ) {
		
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		finish();
		
	}
	
	class BackgroundTask extends AsyncTask<String, String, String> {

		@Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	        Log.d( "com.talentuno.mynit" , "Received:" + result );
	    }
		
		@Override
		protected String doInBackground(String... uri) {
			
			System.out.println( "Running query:" + uri[1]  );
			HttpPost httpPost = new HttpPost(uri[0]);
			String params = uri[1];
	    	
			if( params != null ) {
				
				try {
					httpPost.setEntity(new StringEntity(params));
				} catch (UnsupportedEncodingException e) {
					Log.d( "com.talentuno.mynit", e.getMessage() );
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
	        	return null;
	        } catch (IOException e) {
	        	Log.d( "com.talentuno.mynit", e.getMessage() );
	        	return null;
	        }
	        
	        return responseString;
	        
		}
		
	}
	
	class GroupArrayAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final String[] values;
	 
		public GroupArrayAdapter(Context context, String[] values) {
			super(context, R.layout.group_list, values);
			this.context = context;
			this.values = values;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 
			View rowView = inflater.inflate(R.layout.group_list, parent, false);
			TextView groupNameTv = (TextView) rowView.findViewById(R.id.group_name);
			TextView groupMembersTv = (TextView) rowView.findViewById(R.id.group_members);
			
			String groupName = (values[position].split( ":" ))[0];
			String groupMembers = (values[position].split( ":" ))[1];
			
			groupNameTv.setText(groupName);
			groupMembersTv.setText(groupMembers);
			
			CheckBox cb = (CheckBox) rowView.findViewById(R.id.check_group);
			cb.setTag(groupName);
			
			cb.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {

					CheckBox ck = (CheckBox)v;
					String groupName = ck.getTag().toString();
					if(ck.isChecked()) {
						SELECTED_GROUPS.put(groupName,groups.get(groupName));
						System.out.println( groupName + " was selected with members:" + groups.get(groupName));
					}
					if( ! ck.isChecked()) {
						SELECTED_GROUPS.remove(groupName);
						System.out.println(groupName + " was removed with members:" + groups.get(groupName));
					}
				}
			});
			
			return rowView;
		}
		
	}

}