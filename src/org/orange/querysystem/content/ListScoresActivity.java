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
import org.orange.querysystem.SettingsActivity;
import org.orange.querysystem.content.ListScoresFragment.SimpleScore;
import org.orange.querysystem.content.ReadDBForScores.OnPostExcuteListerner;

import util.webpage.Course;
import util.webpage.Course.CourseException;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

public class ListScoresActivity extends FragmentActivity implements OnPostExcuteListerner{
	private int start_resume = 0;

	TabHost mTabHost;
	ViewPager  mViewPager;
	TabsAdapter mTabsAdapter;
	private TextView currentTime;
	public static final int PASSWORD_PROMPT = 1;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@TargetApi(11)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_tabs_pager);
		
		showDialog(PASSWORD_PROMPT);
		
		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager)findViewById(R.id.pager);

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		
		//3.0以上版本，使用ActionBar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar mActionBar = getActionBar();
			mActionBar.setTitle("成绩单");
			//横屏时，为节省空间隐藏ActionBar
			if(getResources().getConfiguration().orientation == 
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				mActionBar.hide();
		}else{
			TabWidget tabWidget = mTabHost.getTabWidget();
			for (int i = 0; i < tabWidget.getChildCount(); i++) {  
				View child = tabWidget.getChildAt(i);  

				final TextView tv = (TextView)child.findViewById(android.R.id.title);  
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();  
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0); //取消文字底边对齐  
				params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE); //设置文字居中对齐  

				//child.getLayoutParams().height = tv.getHeight();
			}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if(getResources().getConfiguration().orientation ==
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				getActionBar().hide();
		}
		readDB();
		}
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch(id){
		case PASSWORD_PROMPT:
			final TextView textView = new TextView(this);
			textView.setText("请输入登陆密码：");
			textView.setTextSize(14);
			textView.setId(1);
			final EditText editText = new EditText(this);
			editText.setId(2);
			editText.setEnabled(true);
			editText.setCursorVisible(true);
			editText.setLongClickable(true);
			editText.setFocusable(true);
       	 	
			RelativeLayout.LayoutParams tvlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			RelativeLayout.LayoutParams etlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			RelativeLayout relativeLayout = new RelativeLayout(this);
			tvlp.addRule(RelativeLayout.ALIGN_BASELINE, 2);
			etlp.addRule(RelativeLayout.RIGHT_OF, 1);
			relativeLayout.addView(textView, tvlp);
			relativeLayout.addView(editText, etlp);
			return new AlertDialog.Builder(this)
            .setView(relativeLayout)
            .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int whichButton) {
                  	 
            		/* User clicked OK so do some stuff */
            		if(editText.getText().toString().equals(SettingsActivity.getAccountPassword(ListScoresActivity.this))){
            			
            		}else if(!editText.getText().toString().equals(SettingsActivity.getAccountPassword(ListScoresActivity.this))){
            			editText.setText("");
            			Toast.makeText(ListScoresActivity.this, "密码输入错误，请重试！！", Toast.LENGTH_LONG).show();
            			finish();
            		}
                      
                }
            })
            .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int whichButton) {

            		/* User clicked cancel so do some stuff */
            		finish();
                }
            }).create();
		}
		return null;
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
    	mTabsAdapter.clear();
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
		for(Bundle arg:args){
			TabSpec tabSpec = mTabHost.newTabSpec(counter+"学期");
			mTabsAdapter.addTab(tabSpec.setIndicator((counter++)+"学期"),
					ListScoresFragment.class, arg);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				currentTime.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
			}else{
				mTabHost.getTabWidget().getChildAt(counter-2).setBackgroundResource(R.drawable.tab);
			}		
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
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.refresh);
        menu.add(0, 2, 2, R.string.settings);
        
        return super.onCreateOptionsMenu(menu); 
    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// TODO Auto-generated method stub\
    	if(item.getItemId() == 1){
    		if(isNetworkConnected()){
    			start_resume = 1;
        		startActivity(new Intent(this, InsertDBFragmentActivity.class));
        		//TODO startActivity后不会继续运行
            }
            else{
            	Toast.makeText(this, "网络异常！请检查网络设置！", Toast.LENGTH_LONG).show();
            }
    	}
    	else if(item.getItemId() == 2){
    		startActivity(new Intent(this, SettingsActivity.class));
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

