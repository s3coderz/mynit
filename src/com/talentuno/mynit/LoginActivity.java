package com.talentuno.mynit;

import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.talentuno.mynit.R;

public class LoginActivity extends Activity {
	
	private String countryCode;
	private String phoneNumber;
	private String OTP;
	public static final String DATA_STORE = "com.talentuno.mynit";
	private Context loginActivityCtx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Intent intentnew = new Intent(this,MYNHomeActivity.class);
		startActivity(intentnew);
		finish();
		
		loginActivityCtx = LoginActivity.this;
		
		SharedPreferences dataStore = getSharedPreferences(DATA_STORE, MODE_PRIVATE);
		if( dataStore.getBoolean( "enabled" , false ) ) {
			
			Intent intent = new Intent(this,HomeActivity.class);
			startActivity(intent);
			finish();
			
		}
		
//		String phNumber = ((TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
		
		super.onCreate(savedInstanceState);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar));
		setContentView(R.layout.activity_login);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
	}
	
	public void confirmNumber( View view ) {
		
		EditText countryCodeEditText = (EditText) findViewById(R.id.countryCode);
		EditText phoneNumberEditText = (EditText) findViewById(R.id.phoneNumber);
		
		String countryCode = countryCodeEditText.getText().toString();
		String phoneNumber = phoneNumberEditText.getText().toString();
		
		countryCode = countryCode.split( " " )[0];
		if( countryCode.isEmpty() )
			countryCode = "+91";
		if( ! countryCode.startsWith( "+" ))
			countryCode = "+" + countryCode;
		if( ! countryCode.matches( "[0-9\\+]+" )) {
			
			new AlertDialog.Builder(this)
		    .setTitle("Error!")
		    .setMessage(R.string.invalid_country_code)
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {}
		     })
		    .setIcon(android.R.drawable.ic_dialog_alert)
		     .show();
			return;
		}
		
		if( phoneNumber.isEmpty() || ! phoneNumber.matches( "[0-9\\+]+" ) || phoneNumber.length() != 10 ) {
			
			new AlertDialog.Builder(this)
		    .setTitle("Error!")
		    .setMessage(R.string.invalid_phone_number)
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {}
		     })
		    .setIcon(android.R.drawable.ic_dialog_alert)
		     .show();
			return;
		}
		
		this.countryCode = countryCode;
		this.phoneNumber = phoneNumber;
		new AlertDialog.Builder(this)
		    .setTitle(R.string.confirm_your_number)
		    .setMessage( countryCode + " " + phoneNumber )
		    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            requestOTP();
		        }
		     })
		    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {}
		     })
		    .setIcon(android.R.drawable.ic_dialog_alert)
		     .show();	
		
	}
	
	public void requestOTP() {
		
		int cc = Integer.parseInt(countryCode.substring(1));
		long pn = Long.parseLong(phoneNumber);
		int otp = 0;
		
		Random random = new Random( cc+pn );
		otp = random.nextInt(10000);
		if( otp < 1000 )
			OTP = "0" + otp;
		else
			OTP = otp + "";
		
		SharedPreferences dataStore = getSharedPreferences(DATA_STORE, MODE_PRIVATE);
	    SharedPreferences.Editor editor = dataStore.edit();
	    editor.putString("OTP", OTP);
	    editor.commit();
	    
	    //new DatabaseQuery().execute("http://54.148.201.55:7474/db/data","GET");
	    new DatabaseQuery( loginActivityCtx ).execute("http://54.148.201.55:7474/db/data/transaction/commit","POST","{\"statements\" : [ {\"statement\" : \"CREATE (user:User{props})\",\"parameters\" : {\"props\" : {\"uid\" : \""+cc+pn+"\",\"otp\" : \""+OTP+"\"}}},{\"statement\" : \"MATCH(root:ROOT) MATCH(u:User{uid:{uid}}) CREATE UNIQUE (root)-[r:A_USER]->(u) RETURN id(r),id(u)\",\"parameters\" : {\"uid\" : \""+cc+pn+"\"}}]}");
	    Toast.makeText(getApplicationContext(), "Generating OTP...", Toast.LENGTH_LONG).show();
	    
	    AlertDialog.Builder alert = new AlertDialog.Builder(this);

	    alert.setTitle("Validation!");
	    alert.setMessage(R.string.enter_OTP);
	    final EditText input = new EditText(this);
	    alert.setView(input);

	    alert.setPositiveButton( R.string.confirm, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		String value = input.getText().toString();
	    		validateOTP(value);
	    	}
	    });

	    alert.show();
	    
	}
	
	public void validateOTP( String otp ) {
		
		SharedPreferences dataStore = getSharedPreferences(DATA_STORE, MODE_PRIVATE);
		if( dataStore.getString( "OTP" , "NA" ).equals(otp) ) {
			
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

		    alert.setTitle("Enter Your Name");
		    final EditText input = new EditText(this);
		    alert.setView(input);

		    alert.setPositiveButton( R.string.confirm, new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int whichButton) {
		    		String value = input.getText().toString();
		    		storeUsername(value);
		    	}
		    });

		    alert.show();
		    
			SharedPreferences.Editor editor = dataStore.edit();
			editor.putBoolean("enabled", true);
			editor.putString("uid", countryCode.substring(1)+phoneNumber);
		    editor.commit();
			
		}
		else {
			
			new AlertDialog.Builder(this)
		    .setTitle("Error!")
		    .setMessage(R.string.invalid_OTP)
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {}
		     })
		    .setIcon(android.R.drawable.ic_dialog_alert)
		     .show();

		    String uid = countryCode.substring(1);
		    uid+=phoneNumber;
			new DatabaseQuery( loginActivityCtx ).execute("http://54.148.201.55:7474/db/data/transaction/commit","POST","{\"statements\" : [ {\"statement\" : \"MATCH (user:User{uid:{uid}}) MATCH (root:ROOT) MATCH (root)-[r:A_USER]->(user) DELETE r\",\"parameters\" : {\"uid\":\""+uid+"\"}},{\"statement\" : \"MATCH (user:User{uid:{uid}}) DELETE user\",\"parameters\" : {\"uid\":\""+uid+"\"}} ]}");
			
			Intent intent = new Intent(this,LoginActivity.class);
			startActivity(intent);
			finish();
			
		}
		
	}
	
	public void storeUsername( String name ) {
		
		SharedPreferences dataStore = getSharedPreferences(DATA_STORE, MODE_PRIVATE);
		SharedPreferences.Editor editor = dataStore.edit();
		editor.putString("name", name );
	    editor.commit();
	    String uid = countryCode.substring(1);
	    uid+=phoneNumber;
	    new DatabaseQuery( loginActivityCtx ).execute("http://54.148.201.55:7474/db/data/transaction/commit","POST","{\"statements\" : [ {\"statement\" : \"MATCH (user:User{uid:{uid}}) SET user.name = {name}\",\"parameters\" : {\"uid\":\""+uid+"\",\"name\":\""+name+"\"}} ]}");
	    
	    Intent intent = new Intent(this,HomeActivity.class);
		startActivity(intent);
		finish();
		
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_login,
					container, false);
			return rootView;
		}
	}

}
