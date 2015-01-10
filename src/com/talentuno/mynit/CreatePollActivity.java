package com.talentuno.mynit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class CreatePollActivity extends Activity {
	
	static final String SURVEY_QUESTION = "com.talentuno.mynit.survey.question";
	static final String ALLOW_INVITE = "com.talentuno.mynit.allow.invite";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_poll);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_poll, menu);
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

	public void onToggleClicked( View view ) {
		
		ToggleButton tb = (ToggleButton)findViewById(R.id.togglebutton);
		System.out.println( tb.isChecked() );
		
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    
		TextView tv = (TextView)findViewById(R.id.poll_question);
		EditText et = (EditText)findViewById(R.id.poll_text);
	    
		tv.setText( et.getText().toString() );
		
		return super.onKeyUp(keyCode, event);
		
	}
	
	public void createGroup( View view ) {
		
		ToggleButton tb = (ToggleButton)findViewById(R.id.togglebutton);
		EditText et = (EditText)findViewById(R.id.poll_text);
		TextView tv = (TextView)findViewById(R.id.poll_question);
		
		tv.setText( et.getText().toString() );
		
		if( et.getText().toString().trim().equals( "" ) || tv.getText().toString().trim().equals( "" ) ) {
			
			Toast.makeText(getApplicationContext(), "Please enter survey text", Toast.LENGTH_LONG).show();
			return;
			
		}
		
		Intent intent = new Intent(this, GroupActivity.class);
		intent.putExtra(SURVEY_QUESTION, et.getText().toString().trim() );
		if( tb.isChecked() )
			intent.putExtra(ALLOW_INVITE, "true" );
		if( ! tb.isChecked() )
			intent.putExtra(ALLOW_INVITE, "false" );
		startActivity(intent);
		finish();
		
	}
	
}
