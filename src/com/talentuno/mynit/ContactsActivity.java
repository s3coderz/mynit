package com.talentuno.mynit;

import java.util.Arrays;
import java.util.HashMap;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class ContactsActivity extends ListActivity {
	
	public static HashMap<String, String> SELECTED_NUMBERS;
	public static HashMap<String, String> contacts;
	static final String GROUP_NAME = "com.talentuno.mynit.group.name";
	public static final String DATA_STORE = "com.talentuno.mynit";
 
	@Override
	public void onCreate(Bundle savedInstanceState) {
	 
		contacts = new HashMap<String,String>();
		ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,null, null);

        while (cur.moveToNext()) {

            String name =cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));    
            String phoneNumber = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if(! contacts.containsKey(name)) contacts.put(name, phoneNumber);
        }

        SELECTED_NUMBERS = new HashMap<String, String>();
        
        final String[] CONTACTS = contacts.keySet().toArray( new String[]{} );
        Arrays.sort(CONTACTS);
        
		super.onCreate(savedInstanceState);
 		setListAdapter(new ContactArrayAdapterNew(this, CONTACTS));
 		
 		ListView list = (ListView)findViewById(android.R.id.list);
 		View footerView = getLayoutInflater().inflate(R.layout.footer, null);
        list.addFooterView(footerView);
 
	}
	
	public void createGroup( View view ) {

		String groupName = null;
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    groupName = extras.getString(GROUP_NAME);
		}
		
		if( groupName != null ) {
			
			SharedPreferences dataStore = getSharedPreferences(DATA_STORE, MODE_PRIVATE);
		    SharedPreferences.Editor editor = dataStore.edit();
		    String groups = dataStore.getString( "groups" , null );
		    if( groups == null )
		    	editor.putString("groups", groupName );
		    else
		    	editor.putString("groups", groups + "," + groupName );
		    String groupMembers = "";
		    for( String s : SELECTED_NUMBERS.keySet() )
		    	groupMembers += s + ",";
		    groupMembers = groupMembers.substring(0, groupMembers.length() - 1 ); // remove trailing comma
		    editor.putString( "group." + groupName + ".members" , groupMembers );
		    editor.commit();			
			
		}
		windUp();
		
	}
	
	public void exitGroup( View view ) {
		
		windUp();
		
	}
	
	private void windUp() {
		
		SELECTED_NUMBERS.clear();
		finish();
		
	}
 
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
 
		CheckBox checkbox = (CheckBox) v.findViewById(R.id.check);
		checkbox.setChecked(!checkbox.isChecked());
		System.out.println(position);
		System.out.println(id);
		
	}
	
	class ContactArrayAdapterNew extends ArrayAdapter<String> {
		private final Context context;
		private final String[] values;
	 
		public ContactArrayAdapterNew(Context context, String[] values) {
			super(context, R.layout.contact_list, values);
			this.context = context;
			this.values = values;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 
			View rowView = inflater.inflate(R.layout.contact_list, parent, false);
			TextView textView = (TextView) rowView.findViewById(R.id.label);
			textView.setText(values[position]);
	 
			CheckBox cb = (CheckBox) rowView.findViewById(R.id.check);
			cb.setTag(values[position]);
			
			cb.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {

					CheckBox ck = (CheckBox)v;
					String contactName = ck.getTag().toString();
					if(ck.isChecked())
						SELECTED_NUMBERS.put(contactName,contacts.get(contactName));
					if( ! ck.isChecked())
						SELECTED_NUMBERS.remove(contactName);
				}
			});
			
			return rowView;
		}
		
	}
 
}