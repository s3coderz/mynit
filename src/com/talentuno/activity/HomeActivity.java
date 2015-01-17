package com.talentuno.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.talentuno.fragments.ContactsFragment;
import com.talentuno.fragments.HomeFragment;
import com.talentuno.fragments.LicenceAgreementFragment;
import com.talentuno.fragments.ProfileFragment;
import com.talentuno.fragments.SettingsFragment;
import com.talentuno.fragments.TrendingFragment;
import com.talentuno.mynit.R;

public class HomeActivity extends Activity implements OnItemClickListener {

	Fragment homeFragment, trendingFragment, contactsFragment, profileFragment,
			settingsFragment, licenceAgreementFragment, selectedFragment;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private int selectedPos = -1;
	private ActionBarDrawerToggle mDrawerToggle;
	private String[] mTitles = { "Home", "Profile", "Settings", "Trending",
			"Licence Agreement", "Contacts" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_activity_home);
		init();
		initDrawer();
		loadFagment(0);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				// getActionBar().setTitle(mTitle);
				// invalidateOptionsMenu(); // creates call to
				// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				// getActionBar().setTitle(mDrawerTitle);
				// invalidateOptionsMenu(); // creates call to
				// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		// if (savedInstanceState == null) {
		// // selectItem(0);
		// }

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		return super.onOptionsItemSelected(item);
	}

	private void initDrawer() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mTitles));
		mDrawerList.setOnItemClickListener(this);
	}

	private void init() {
		homeFragment = new HomeFragment();
		trendingFragment = new TrendingFragment();
		contactsFragment = new ContactsFragment();
		profileFragment = new ProfileFragment();
		settingsFragment = new SettingsFragment();
		licenceAgreementFragment = new LicenceAgreementFragment();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		if (selectedPos == position)
			return;
		mDrawerList.setItemChecked(position, true);
		// setTitle(mPlanetTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
		loadFagment(position);

	}

	private void loadFagment(int position) {
		Fragment oldFrag = selectedFragment;
		switch (position) {
		case 0:
			selectedFragment = homeFragment;
			break;
		case 1:
			selectedFragment = profileFragment;
			break;
		case 2:
			selectedFragment = settingsFragment;
			break;
		case 3:
			selectedFragment = trendingFragment;
			break;
		case 4:
			selectedFragment = licenceAgreementFragment;
			break;
		case 5:
			selectedFragment = contactsFragment;
			break;
		}
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		if (oldFrag != null) {
			ft.remove(oldFrag);
		}
		ft.add(R.id.content_frame, selectedFragment);
		ft.commit();
	}
}
