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

import org.orange.querysystem.AboutActivity;
import org.orange.querysystem.LoginActivity;
import org.orange.querysystem.R;
import org.orange.querysystem.content.ListScoresFragment.SimpleScore;
import org.orange.querysystem.content.ReadDBForScores.OnPostExcuteListerner;

import util.webpage.Course;
import util.webpage.Course.CourseException;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class ListScoresActivity extends FragmentActivity implements OnPostExcuteListerner{
	private int start_resume = 0;

	TabHost mTabHost;
	ViewPager  mViewPager;
	TabsAdapter mTabsAdapter;
	private TextView currentTime;

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
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if(getResources().getConfiguration().orientation ==
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				getActionBar().hide();
		}
//		SharedPreferences shareData = getSharedPreferences("data", 0);
//        if(shareData.getBoolean("changeUser", false)){
//			Editor editor = getSharedPreferences("data", 0).edit();
//            editor.putBoolean("changeUser", false);
//            editor.commit();
//            start_resume = 1;
//            startActivity(new Intent(this, InsertDBFragmentActivity.class));
//		}else{
			readDB();
//		}
		
	}
	
	public void readDB(){
		SharedPreferences shareData = getSharedPreferences("data", 0);
    	new ReadDBForScores(this, this).execute(shareData.getString("userName", null));
    }
    
    @Override
	public void onPostReadFromDBForScores(ArrayList<ArrayList<Course>> courses) {
			showScoresInfo(courses);
	}
    
    public void showScoresInfo(ArrayList<ArrayList<Course>> courses){
//    	mTabHost.clearAllTabs();
    	currentTime = (TextView)findViewById(R.id.currentTime);
    	currentTime.setText("成绩单");
    	SharedPreferences shareData = getSharedPreferences("data", 0);
    	
    	ArrayList<Bundle> args = new ArrayList<Bundle>(7);
    	System.out.println("Activity" + courses.size());
		for(int semester = 1;semester<courses.size();semester++){
			float totalGradePoint = 0;
			byte totalCredit = 0;
			ArrayList<SimpleScore> scores = new ArrayList<SimpleScore>();
			for(int counter = 0;counter<courses.get(semester).size();counter++)
			try {
					totalGradePoint = totalGradePoint + (float)courses.get(semester).get(counter).getGradePoint();
//					totalCredit = totalCredit + (byte)courses.get(semester).get(counter).getCredit();
					scores.add(new SimpleScore(semester+1, courses.get(semester).get(counter).getName(), (short)(courses.get(semester).get(counter).getTestScore()), (short)(courses.get(semester).get(counter).getTotalScore()), (float)courses.get(semester).get(counter).getGradePoint(), (byte)courses.get(semester).get(counter).getCredit(), courses.get(semester).get(counter).getKind()));
			} catch (CourseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			currentTime.setText("成绩单");
			Bundle arg = new Bundle();
			arg.putParcelableArrayList(ListScoresFragment.SCORES_KEY, scores);
			args.add(arg);
		}
		int counter = 1;
		for(Bundle arg:args)
			mTabsAdapter.addTab(mTabHost.newTabSpec(counter+"学期").setIndicator((counter++)+"学期"),
					ListScoresFragment.class, arg);
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
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.main_menu);
        menu.add(0, 2, 2, R.string.change_number);
        menu.add(0, 3, 3, R.string.settings);
        menu.add(0, 4, 4, R.string.refresh);
        menu.add(0, 5, 5, R.string.about);
        
        return super.onCreateOptionsMenu(menu); 
    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// TODO Auto-generated method stub\
    	if(item.getItemId() == 1){
//    		Editor editor = getSharedPreferences("data", 0).edit();
//			editor.putString("passMainMenu", "true");
//            editor.commit();
//    		startActivity(new Intent(this, MainMenuActivity.class));
    		finish();
    	}
    	else if(item.getItemId() == 2){
    		Editor editor = getSharedPreferences("data", 0).edit();
    		editor.putBoolean("logIn_auto", false);
    		editor.putBoolean("changeUser", false);
    		editor.commit();
    		start_resume = 1;
    		startActivity(new Intent(this, LoginActivity.class));
    	}
    	else if(item.getItemId() == 3){
//    		startActivity(new Intent(this, AllListCoursesActivity.class));
    	}
    	else if(item.getItemId() == 4){
    		if(isNetworkConnected()){
    			start_resume = 1;
        		startActivity(new Intent(this, InsertDBFragmentActivity.class));
        		//TODO startActivity后不会继续运行
//        		readDB();
            }
            else{
            	Toast.makeText(this, "网络异常！请检查网络设置！", Toast.LENGTH_LONG).show();
            }
    	}
    	else if(item.getItemId() == 5){
    		startActivity(new Intent(this, AboutActivity.class));
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    public boolean isNetworkConnected(){
    	ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if(networkInfo !=null && networkInfo.isConnected()){
			return true;
		}
		else{
		    return false;
		}
    }

//    @Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if(keyCode == KeyEvent.KEYCODE_BACK){
//			Editor editor = getSharedPreferences("data", 0).edit();
//			editor.putString("passMainMenu", "true");
//            editor.commit();
//		}
//		return super.onKeyDown(keyCode, event);
//	}
    
    @Override
	protected void onResume(){
		super.onResume();
		if(start_resume == 0){
			
		}
		else if(start_resume == 1){
			readDB();
		}
    }
}

