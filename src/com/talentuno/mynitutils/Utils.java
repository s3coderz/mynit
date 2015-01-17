package com.talentuno.mynitutils;

import android.content.Context;
import android.telephony.TelephonyManager;

public class Utils {

	public static String getImeiNumber(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}
}
