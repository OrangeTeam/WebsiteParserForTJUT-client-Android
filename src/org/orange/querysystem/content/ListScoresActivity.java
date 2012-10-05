/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orange.querysystem.content;

import java.util.ArrayList;

import org.orange.querysystem.R;
import org.orange.querysystem.content.ListScoresFragment.SimpleScore;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.TabHost;

public class ListScoresActivity extends FragmentActivity{

	TabHost mTabHost;
	ViewPager  mViewPager;
	TabsAdapter mTabsAdapter;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@TargetApi(11)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_tabs_pager);
		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager)findViewById(R.id.pager);

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);

		ArrayList<Bundle> args = new ArrayList<Bundle>(7);
		for(int semester = 1;semester<=6;semester++){
			ArrayList<SimpleScore> scores = new ArrayList<SimpleScore>();
			for(int counter = 0;counter<=semester;counter++)
				scores.add(new SimpleScore(semester+counter, "课程名称加长加长加长加长加长加长再加长"+semester+" "+counter, (short)(counter*semester), (short)(counter*semester), (float)counter, (byte)counter, "课程性质"+counter));
			Bundle arg = new Bundle();
			arg.putParcelableArrayList(ListScoresFragment.SCORES_KEY, scores);
			args.add(arg);
		}
		int counter = 0;
		for(Bundle arg:args)
			mTabsAdapter.addTab(mTabHost.newTabSpec(counter+"学期").setIndicator((counter++)+"学期"),
					ListScoresFragment.class, arg);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if(getResources().getConfiguration().orientation ==
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				getActionBar().hide();
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);

		mTabsAdapter.adjustSelectedTabToCenter();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("tab", mTabHost.getCurrentTabTag());
	}

}
