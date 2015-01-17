package com.talentuno.activity;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
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
import com.talentuno.mynitutils.Utils;
import com.talentuno.mynitutils.QueryServer.Action;
import com.talentuno.mynitutils.ResultHandler;

public class OTPActivity extends Activity implements OnClickListener,
		ResultHandler {
	EditText mobNumber;
	TextView cc_code, cc_name, errorText;

	// ProgressBar pb;

	ListPopupWindow listPopupWindow;
	LinearLayout ll_cc;
	List<HashMap<String, String>> countryData;

	Button continueBtn;

	public final int REQUEST_ID = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_activity_otp);
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
		continueBtn = (Button) findViewById(R.id.otp_continueBtn);
		mobNumber = (EditText) findViewById(R.id.otp_mobNumber);
		cc_code = (TextView) findViewById(R.id.otp_cc_number);
		cc_name = (TextView) findViewById(R.id.otp_cc_name);
		ll_cc = (LinearLayout) findViewById(R.id.otp_cc_ll);
		// pb = (ProgressBar) findViewById(R.id.otp_pb);
		errorText = (TextView) findViewById(R.id.otp_errorText);

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
		case R.id.otp_continueBtn:
			requestOTP();
			break;
		case R.id.otp_cc_ll:
			listPopupWindow.show();
			break;
		}
	}

	private void requestOTP() {
		if (mobNumber.getText() == null)
			return;
		String mobNo = mobNumber.getText().toString();
		if (mobNo.length() < 1) {
			errorText.setVisibility(View.VISIBLE);
			errorText.setText(getResources().getString(
					R.string.OTP_emptyTextError));
			return;
		}
		showProgress(true);
		String countryCode = cc_code.getText().toString();
		QueryServer requestTask = new QueryServer(this, Action.REQUEST_OTP,
				REQUEST_ID, 0);
		requestTask.execute(countryCode, mobNo, Utils.getImeiNumber(this));
		Log.i("", "requesting OTP for cc : " + countryCode + " number : "
				+ mobNo);
	}

	private void showProgress(boolean show) {
		if (show) {
			// pb.setVisibility(View.VISIBLE);
			errorText.setVisibility(View.INVISIBLE);
			errorText.setText("");
		} else {
			// pb.setVisibility(View.INVISIBLE);
		}
		ll_cc.setEnabled(!show);
		continueBtn.setEnabled(!show);
		mobNumber.setEnabled(!show);
	}

	public static final String INTENT_EXTRA_COUTNRY_CODE = "countryCode";
	public static final String INTENT_EXTRA_PHONE_NUMBER = "phoneNumber";

	@Override
	public void onSuccess(Object object, int requestId, int responseId) {
		Log.i("", "requestOTP onSuccess");
		showProgress(false);
		Intent createUserIntent = new Intent(this, CreateUserActivity.class);
		createUserIntent.putExtra(INTENT_EXTRA_COUTNRY_CODE, cc_code.getText()
				.toString());
		createUserIntent.putExtra(INTENT_EXTRA_PHONE_NUMBER, mobNumber
				.getText().toString());
		startActivity(createUserIntent);
		this.finish();
	}

	@Override
	public void onFailure(String errMsg, int requestId, int responseId) {
		Log.i("", "requestOTP onError : " + errMsg);
		errorText.setVisibility(View.VISIBLE);
		errorText.setText(errMsg);
		showProgress(false);
	}

}
