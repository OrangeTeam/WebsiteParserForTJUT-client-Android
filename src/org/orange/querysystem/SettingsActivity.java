package org.orange.querysystem;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	public static final String KEY_PREF_SCHOOL_STARTING_DATE = "pref_startingDate";

	@TargetApi(11)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar mActionBar = getActionBar();
			mActionBar.setTitle(R.string.settings);
			//横屏时，为节省空间隐藏ActionBar
			if(getResources().getConfiguration().orientation ==
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				mActionBar.hide();
		}
	}

}
