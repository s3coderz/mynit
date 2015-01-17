package com.talentuno.fragments;

import android.app.Fragment;
import android.widget.Toast;

public class SettingsFragment extends Fragment {
	@Override
	public void onResume() {
		super.onResume();
		Toast.makeText(getActivity(), "Settings", Toast.LENGTH_SHORT).show();
	}
}
