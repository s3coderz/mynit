package com.talentuno.fragments;

import android.app.Fragment;
import android.widget.Toast;

public class HomeFragment extends Fragment {
	@Override
	public void onResume() {
		super.onResume();
		Toast.makeText(getActivity(), "Home", Toast.LENGTH_SHORT).show();
	}
}
