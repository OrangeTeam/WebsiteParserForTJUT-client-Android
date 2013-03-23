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
package org.orange.querysystem;

import java.util.ArrayList;

import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.querysystem.content.InsertDBFragmentActivity;
import org.orange.querysystem.content.ListScoresFragment;
import org.orange.querysystem.content.ListScoresFragment.SimpleScore;
import org.orange.querysystem.content.RefreshScoresFragmentActivity;
import org.orange.querysystem.content.TabsAdapter;
import org.orange.querysystem.util.Network;
import org.orange.querysystem.util.ReadDBForScores;
import org.orange.querysystem.util.ReadDBForScores.OnPostExcuteListerner;

import util.webpage.Course;
import util.webpage.Course.CourseException;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

public class ScoresActivity extends FragmentActivity implements OnPostExcuteListerner{
	private int start_resume = 0;

	TabHost mTabHost;
	ViewPager  mViewPager;
	TabsAdapter mTabsAdapter;
	public static final int PASSWORD_PROMPT = 1;

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

		//3.0以上版本，使用ActionBar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			//去掉低版本使用的Title
			findViewById(R.id.currentTime).setLayoutParams(new LinearLayout.LayoutParams(0, 0));
			ActionBar mActionBar = getActionBar();
			mActionBar.setTitle(R.string.score_query);
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
		}

		showDialog(PASSWORD_PROMPT);
	}

	@TargetApi(11)
	public void enterActivity(){
		readDB();
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
			editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
       	 	
			RelativeLayout.LayoutParams tvlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			RelativeLayout.LayoutParams etlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			RelativeLayout relativeLayout = new RelativeLayout(this);
			tvlp.addRule(RelativeLayout.ALIGN_BASELINE, 2);
			etlp.addRule(RelativeLayout.RIGHT_OF, 1);
			relativeLayout.addView(textView, tvlp);
			relativeLayout.addView(editText, etlp);
			return new AlertDialog.Builder(this)
            .setView(relativeLayout)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int whichButton) {
                  	 
            		/* User clicked OK so do some stuff */
            		if(editText.getText().toString().equals(SettingsActivity.getAccountPassword(ScoresActivity.this))){
            			enterActivity();
            		}else if(!editText.getText().toString().equals(SettingsActivity.getAccountPassword(ScoresActivity.this))){
            			editText.setText("");
            			Toast.makeText(ScoresActivity.this, "密码输入错误，请重试！！", Toast.LENGTH_LONG).show();
            			finish();
            		}
                      
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int whichButton) {

            		/* User clicked cancel so do some stuff */
            		finish();
                }
            })
            .setOnKeyListener(new OnKeyListener(){

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
						finish();
					}
					return false;
					
				}
				
            	
            })
            .create();
		case InsertDBFragmentActivity.LOG_IN_ERROR_DIALOG_ID:
			final TextView textView2 = new TextView(this);
			textView2.setText("用户名或密码错误，请重新设置！");
			textView2.setTextSize(14);
			RelativeLayout.LayoutParams tvlp2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			RelativeLayout relativeLayout2 = new RelativeLayout(this);
			
			tvlp2.addRule(RelativeLayout.CENTER_IN_PARENT);
			relativeLayout2.addView(textView2, tvlp2);
			return new AlertDialog.Builder(this)
            .setView(relativeLayout2)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int whichButton) {
                  	 
            		/* User clicked OK so do some stuff */
            		InsertDBFragmentActivity.logIn_error = false;
            		startActivity(new Intent(ScoresActivity.this, SettingsActivity.class));
                      
                }
            })
            .setOnKeyListener(new OnKeyListener(){

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent even) {
					// TODO Auto-generated method stub
					finish();
					return false;
				}
				
            	
            }).create();
		}
		return null;
	}
	
	public void readDB(){
    	new ReadDBForScores(this, this).execute(SettingsActivity.getAccountStudentID(this));
    }
    
    @Override
	public void onPostReadFromDBForScores(ArrayList<ArrayList<Course>> courses) {
			showScoresInfo(courses);
	}
    
    public void showScoresInfo(ArrayList<ArrayList<Course>> courses){
    	mTabsAdapter.clear();

    	ArrayList<Bundle> args = new ArrayList<Bundle>(7);
		for(ArrayList<Course> coursesInASemester:courses){
			ArrayList<SimpleScore> scores = new ArrayList<SimpleScore>();
			for(Course course:coursesInASemester)
				try {
					scores.add(new SimpleScore(course.getId(), course.getName(),
							course.getTestScore(), course.getTotalScore(),
							course.getGradePoint(), course.getCredit(), course.getKind()));
				} catch (CourseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			Bundle arg = new Bundle();
			arg.putParcelableArrayList(ListScoresFragment.SCORES_KEY, scores);
			args.add(arg);
		}
		int counter = 1;
		for(Bundle arg:args){
			TabSpec tabSpec = mTabHost.newTabSpec(counter+"学期");
			mTabsAdapter.addTab(tabSpec.setIndicator((counter++)+"学期"),
					ListScoresFragment.class, arg);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				mTabHost.getTabWidget().getChildAt(counter-2).setBackgroundResource(R.drawable.tab);
			}		
		}
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
    		if(Network.getInstance(this).isConnected()){
    			start_resume = 1;
        		startActivity(new Intent(this, RefreshScoresFragmentActivity.class));
        		//TODO startActivity后不会继续运行
            }
            else{
            	Network.openNoConnectionDialog(this);
            }
    	}
    	else if(item.getItemId() == 2){
    		startActivity(new Intent(this, SettingsActivity.class));
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
	protected void onResume(){
		super.onResume();
		if(InsertDBFragmentActivity.logIn_error == true){
			showDialog(InsertDBFragmentActivity.LOG_IN_ERROR_DIALOG_ID);
		}
		if(start_resume == 0){
			
		}
		else if(start_resume == 1){
			readDB();
		}
    }
}

