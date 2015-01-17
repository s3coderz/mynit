package com.talentuno.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.talentuno.mynit.R;
import com.talentuno.mynitutils.QueryServer;
import com.talentuno.mynitutils.ResultHandler;
import com.talentuno.mynitutils.Utils;
import com.talentuno.mynitutils.QueryServer.Action;

public class CreateUserActivity extends Activity implements OnClickListener,
		ResultHandler {
	Button otpContinueBtn, createUserContinueBtn;
	TextView otpError, nameError;
	LinearLayout otpLl;
	EditText otpEt, nameEt;
	QueryServer query_OtpVerify, query_registerUser;

	private final int REQUEST_ID_OTP = 1;
	private final int REQUEST_ID_REGISTER_USER = 2;
	String countryCode, phoneNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_activity_usercreate);
		init();

		countryCode = getIntent().getExtras().getString(
				OTPActivity.INTENT_EXTRA_COUTNRY_CODE);
		phoneNumber = getIntent().getExtras().getString(
				OTPActivity.INTENT_EXTRA_PHONE_NUMBER);
	}

	private void init() {
		otpContinueBtn = (Button) findViewById(R.id.register_otp_continueBtn);
		otpLl = (LinearLayout) findViewById(R.id.register_otp_ll);
		otpError = (TextView) findViewById(R.id.register_otp_errorText);
		nameError = (TextView) findViewById(R.id.register_name_errorText);
		otpEt = (EditText) findViewById(R.id.register_OTPText);
		nameEt = (EditText) findViewById(R.id.register_UserName);
		createUserContinueBtn = (Button) findViewById(R.id.register_createuser_continueBtn);
		createUserContinueBtn.setOnClickListener(this);
		otpContinueBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.register_otp_continueBtn:
			verifyOTP();
			break;
		case R.id.register_createuser_continueBtn:
			registerUser();
			break;
		}
	}

	private void registerUser() {
		if (nameEt.getText() == null) {
			return;
		}
		String name = nameEt.getText().toString();
		if (name.length() < 1) {
			nameError.setVisibility(View.VISIBLE);
			nameError.setText(getResources().getString(
					R.string.register_User_emptyTextError));
			return;
		}

		showUserCreateProgress(true);
		query_registerUser = new QueryServer(this, Action.CREATE_USER,
				REQUEST_ID_REGISTER_USER, 0);
		query_registerUser.execute(name, phoneNumber, null, phoneNumber, null,
				Utils.getImeiNumber(this));
	}

	private void showOTPProgress(boolean show) {
		otpEt.setEnabled(!show);
		otpContinueBtn.setEnabled(!show);
	}

	private void showUserCreateProgress(boolean show) {
		nameEt.setEnabled(!show);
		createUserContinueBtn.setEnabled(!show);
	}

	private void verifyOTP() {
		if (otpEt.getText() == null) {
			return;
		}
		String otp = otpEt.getText().toString();
		if (otp.length() < 1) {
			otpError.setVisibility(View.VISIBLE);
			otpError.setText(CreateUserActivity.this.getResources().getString(
					R.string.verify_OTP_emptyTextError));
			return;
		}
		showOTPProgress(true);
		query_OtpVerify = new QueryServer(this, Action.VERIFY_OTP,
				REQUEST_ID_OTP, 0);
		query_OtpVerify.execute(countryCode, phoneNumber, otp);
		Log.i("", "verifying OTP : otp : " + otp + " countryCode : "
				+ countryCode + " phoneNumber : " + phoneNumber);
	}

	@Override
	public void onSuccess(Object object, int requestId, int responseId) {
		if (requestId == REQUEST_ID_OTP) {
			showOTPProgress(false);
			otpLl.setVisibility(View.GONE);
			nameEt.setEnabled(true);
			createUserContinueBtn.setEnabled(true);
			nameError.setText(CreateUserActivity.this.getResources().getString(
					R.string.register_OTP_success));
			nameError.setVisibility(View.VISIBLE);
			Log.i("", "verifying OTP : success");
		} else if (requestId == REQUEST_ID_REGISTER_USER) {
			Log.i("", "register user OTP : success");
			showUserCreateProgress(false);
			Intent homeIntent = new Intent(this, HomeActivity.class);
			startActivity(homeIntent);
			finish();
		}
	}

	@Override
	public void onFailure(String errMsg, int requestId, int responseId) {
		if (requestId == REQUEST_ID_OTP) {
			showOTPProgress(false);
			if (errMsg != null && errMsg.length() > 0)
				otpError.setText(errMsg);
			Log.i("", "verifying OTP : failure");
			onSuccess(null, REQUEST_ID_OTP, 0);
		} else if (requestId == REQUEST_ID_REGISTER_USER) {
			Log.i("", "register User : failure");
			showUserCreateProgress(false);

		}

	}
}
