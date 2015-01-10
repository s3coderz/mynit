package com.talentuno.mynit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.MultipleCategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {
	
	public static final String DATA_STORE = "com.talentuno.mynit";
	GraphicalView gv;
    RelativeLayout rl;
    String sid;
    static final String SID = "com.talentuno.mynit";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		String value = null;
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    value = extras.getString(SID);
		}
		
		if( value == null ) {
			
			value = "date_time";
			
		}
		CountDownLatch latch = new CountDownLatch(1);
		NetworkAccess.result = null;
		new Thread( new NetworkAccess(latch, "http://54.148.201.55:7474/db/data/transaction/commit","{\"statements\" : [ {\"statement\" : \"MATCH (survey:Survey{sid:{sid}}) return survey\",\"parameters\" : {\"sid\":\""+value+"\"}} ]}" )).start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			Log.d( "com.talentuno.mynit" , e.getMessage() );
		}
		
		SharedPreferences dataStore = getSharedPreferences(DATA_STORE, MODE_PRIVATE);
		Log.d("com.talentuno.mynit", "Welcome!, "+dataStore.getString( "name" , "Guest" ));
		
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
		
		latch = new CountDownLatch(1);
		NetworkAccess.result = null;
		new Thread( new NetworkAccess(latch, "http://54.148.201.55:7474/db/data/transaction/commit","{\"statements\" :[{\"statement\" : \"MATCH (survey:Survey{sid:{sid}}),(survey)-[r:A_YES]->(yes:Yes) return count(yes)\",\"parameters\" : {\"sid\":\"date_time\"}},{\"statement\" : \"MATCH (survey:Survey{sid:{sid}}),(survey)-[r:A_NO]->(no:No) return count(no)\",\"parameters\" : {\"sid\":\"date_time\"}},{\"statement\" : \"MATCH (survey:Survey{sid:{sid}}),(survey)-[r:A_MAYBE]->(maybe:Maybe) return count(maybe)\",\"parameters\" : {\"sid\":\"date_time\"}}]}" )).start();
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
		
		String yes = json.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsString();
		String no = json.getAsJsonArray("results").get(1).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsString();
		String mayBe = json.getAsJsonArray("results").get(2).getAsJsonObject().getAsJsonArray( "data" ).get(0).getAsJsonObject().getAsJsonArray( "row" ).get(0).getAsString();
		
		Log.d( "com.talentuno.mynit" , "yes:" + yes );
		Log.d( "com.talentuno.mynit" , "no:" + no );
		Log.d( "com.talentuno.mynit" , "maybe:" + mayBe );
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		//View view = (View) LayoutInflater.from(getApplicationContext() ).inflate(R.layout.fragment_home, null);
		TextView textView = (TextView) findViewById(R.id.survey_text);
		textView.setText( surveyText );
		
		if( surveyEnabled.equals( "true" ) )
			findViewById(R.id.survey_enabled).setVisibility(View.VISIBLE);
		else
			findViewById(R.id.survey_disabled).setVisibility(View.VISIBLE);
		
		List<double[]> values1 = new ArrayList<double[]>();

        values1.add(new double[] { Double.parseDouble( yes ), Double.parseDouble( no ), Double.parseDouble( mayBe ) });

        gv = createIntent(values1);

        rl = (RelativeLayout) findViewById(R.id.rel);
        rl.addView(gv); 
		
	}
	
	public GraphicalView createIntent(List<double[]> values1) {

        List<String[]> titles = new ArrayList<String[]>();

        titles.add(new String[] { "YES", "N0", "MAY BE" });
        int[] colors = new int[] { Color.GREEN, Color.RED , Color.YELLOW };

        DefaultRenderer renderer = buildCategoryRenderer(colors);
//        renderer.setApplyBackgroundColor(true);
        renderer.setShowLegend(false);

        renderer.setShowLabels(true);
        renderer.setStartAngle(135);
//        renderer.setBackgroundColor(Color.rgb(222, 222, 200));
        renderer.setLabelsColor(Color.BLACK);

        double totalResponses = 0;
        for( double[] d : values1 )
        	for( int i = 0; i < d.length; i++ )
        		totalResponses += d[i];
        
        Log.d( "com.talentuno.mynit" , "total responses:" + totalResponses );

        return ChartFactory.getDoughnutChartView(HomeActivity.this, buildMultipleCategoryDataset( totalResponses + "Responses", titles, values1), renderer);
    }

    protected MultipleCategorySeries buildMultipleCategoryDataset(String title, List<String[]> titles, List<double[]> values) {
        MultipleCategorySeries series = new MultipleCategorySeries(title);
        int k = 0;
        String[] categories = {"YES","NO","MAY BE"};
        for (double[] value : values) {
            series.add( categories[k], titles.get(k), value);
            k++;
        }
        return series;
    }

    protected DefaultRenderer buildCategoryRenderer(int[] colors) {
        DefaultRenderer renderer = new DefaultRenderer();
        renderer.setLabelsTextSize(10);
        renderer.setLegendTextSize(10);
        renderer.setScale((float)1.35);
        renderer.setMargins(new int[] { 0, 0, 0, 0 });
        for (int color : colors) {
            SimpleSeriesRenderer r = new SimpleSeriesRenderer();
            r.setColor(color);
            renderer.addSeriesRenderer(r);
        }
        return renderer;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
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

	public void answerSurvey( View view ) {
		
		SharedPreferences dataStore = getSharedPreferences(DATA_STORE, MODE_PRIVATE);
		ArrayList<String> surveys = new ArrayList<String>(Arrays.asList(dataStore.getString( "surveys", "NA" ).split(",")));
		
		for( String s : surveys )
			Log.d( "com.talentuno.mynit" , "in list:" + s );
		
		Log.d( "com.talentuno.mynit" , "sid:" + sid );
		
		if( ! surveys.contains( sid ) ) {
			
			Intent intent = new Intent(this, UnansweredSurveyActivity.class);
			intent.putExtra(SID, sid);
			startActivity(intent);
			finish();
			
		}
		
		else {
			
			Intent intent = new Intent(this, AnsweredSurveyActivity.class);
			intent.putExtra(SID, sid);
			startActivity(intent);
			finish();
			
		}
		
		finish();
		
	}
	
}