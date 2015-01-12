package com.talentuno.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.talentuno.mynit.R;
import com.talentuno.mynitutils.CountryUtil;
import com.talentuno.mynitutils.QueryServer;
import com.talentuno.mynitutils.QueryServer.Action;
import com.talentuno.mynitutils.ResultHandler;

public class LoginActivity extends Activity implements OnClickListener,
		ResultHandler {
	EditText mobNumber;
	TextView cc_code, cc_name;

	ListPopupWindow listPopupWindow;
	LinearLayout ll_cc;
	List<HashMap<String, String>> countryData;

	public final int REQUEST_ID = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_activity_login);
		init();

		countryData = CountryUtil.prepareList(this);
		SimpleAdapter adapter = new SimpleAdapter(this, countryData,
				R.layout.new_popup_country_list_row, new String[] { "code",
						"country" }, new int[] { R.id.popup_cc_number,
						R.id.popup_cc_name });
		listPopupWindow.setAdapter(adapter);

		listPopupWindow.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				HashMap<String, String> map = countryData.get(arg2);
				String country_name = map.get("country");
				String country_code = map.get("code");
				cc_name.setText(country_name);
				cc_code.setText("+" + country_code);
				listPopupWindow.dismiss();
			}
		});
		ll_cc.setOnClickListener(this);

		updateDefaultCountryISO();

	}

	private void updateDefaultCountryISO() {

		// TelephonyManager manager = (TelephonyManager)
		// this.getSystemService(Context.TELEPHONY_SERVICE);
		// //getNetworkCountryIso
		// String countryISO= manager.getSimCountryIso().toUpperCase();

		String countryName = CountryUtil.getUserCountryName(this);
		String countryCode = CountryUtil.getUserCountryCode(this);
		Log.i("", "countryName : " + countryName + " countryCode : "
				+ countryCode);
		cc_name.setText(countryName);
		cc_code.setText("+" + countryCode);
	}

	private void init() {
		Button continueBtn = (Button) findViewById(R.id.login_continueBtn);
		mobNumber = (EditText) findViewById(R.id.login_mobNumber);
		cc_code = (TextView) findViewById(R.id.login_cc_number);
		cc_name = (TextView) findViewById(R.id.login_cc_name);
		ll_cc = (LinearLayout) findViewById(R.id.login_cc_ll);

		ll_cc.getViewTreeObserver().addOnPreDrawListener(
				new OnPreDrawListener() {

					@Override
					public boolean onPreDraw() {
						ll_cc.getViewTreeObserver().removeOnPreDrawListener(
								this);
						listPopupWindow.setWidth(ll_cc.getWidth());
						return true;
					}
				});

		continueBtn.setOnClickListener(this);

		listPopupWindow = new ListPopupWindow(this);

		listPopupWindow.setAnchorView(ll_cc);
		// listPopupWindow.setWidth(100);

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;

		listPopupWindow.setHeight(height / 3);

		listPopupWindow.setModal(true);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_continueBtn:
			login();
			break;
		case R.id.login_cc_ll:
			listPopupWindow.show();
			break;
		}
	}

	private void login() {
		if (mobNumber.getText() == null)
			return;
		String mobNo = mobNumber.getText().toString();
		String countryCode = cc_code.getText().toString();
		QueryServer requestTask = new QueryServer(this, Action.REQUEST_OTP,
				REQUEST_ID);
		requestTask.execute(0, countryCode, mobNo);
		Log.i("", "requesting OTP for cc : " + countryCode + " number : "
				+ mobNo);
	}

	@Override
	public void onSuccess(Object object, int requestId, int responseId) {
		Log.i("", "requestOTP onSuccess");
	}

	@Override
	public void onFailure(String errMsg, int requestId, int responseId) {
		Log.i("", "requestOTP onError : " + errMsg);
	}

}
