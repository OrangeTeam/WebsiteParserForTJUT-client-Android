package org.orange.querysystem.content;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.querysystem.content.ListCoursesFragment.SimpleCourse;
import org.orange.querysystem.content.ReadDB.OnPostExcuteListerner;

import util.BitOperate.BitOperateException;
import util.webpage.Course;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

public class NextAllListCoursesActivity extends FragmentActivity implements OnPostExcuteListerner{
	private int mYear = 0;
	private int mMonth = 0;
	private int mDay = 0;
	private int mWeek = 0;
	private int mDayOfWeek = 0;
	private int start_resume = 0;
	
	private TextView currentTime;
	
	protected static final int COURSE_NUMBER = 6;
	public static final String ARRAYLIST_OF_COURSES_KEY
		= ListCoursesActivity.class.getName()+"ARRAYLIST_OF_COURSES_KEY";
	
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
		
		//3.0以上版本，使用ActionBar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar mActionBar = getActionBar();
			mActionBar.setTitle("下学期课程表");
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
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if(getResources().getConfiguration().orientation == 
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				getActionBar().hide();
		}
		
		Calendar calendar = Calendar.getInstance();
		mYear = calendar.get(Calendar.YEAR);
		mMonth = calendar.get(Calendar.MONTH);//比正常少一个月
		mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mWeek = calendar.get(Calendar.WEEK_OF_YEAR);//正常
        mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);//比正常的多一天
        readDB();
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
		
	public void readDB(){
    	new ReadDB(this, this).execute(SettingsActivity.getAccountStudentID(this), "next");
    }
    
    @Override
	public void onPostReadFromDB(ArrayList<Course> courses) {
			showCoursesInfo(courses);
	}
    
    public void showCoursesInfo(ArrayList<Course> courses){
		mTabsAdapter.clear();
        currentTime = (TextView)findViewById(R.id.currentTime);
        currentTime.setText("下学期总课程表" + "        " + mYear + "-" + (mMonth+1) + "-" + mDay);
        
    	Bundle[] args = new Bundle[7]; 
    	
    	/*String[星期几][第几大节]*/
    	LinkedList<SimpleCourse>[][] lesson = new LinkedList[7][14];
    	for(int day=0;day<=6;day++)
    		for(int period=1;period<=13;period++)
    			lesson[day][period] = new LinkedList<SimpleCourse>();
    	for(Course course:courses)
			for(Course.TimeAndAddress time:course.getTimeAndAddress())
				for(int dayOfWeek = 0; dayOfWeek<=6; dayOfWeek++)			
					for(int period = 1; period<=13; period++)
						try{
							if(time.hasSetDay(dayOfWeek)&&time.hasSetPeriod(period)){
								lesson[dayOfWeek][period].addLast(new SimpleCourse(course.getId(),course.getName(),String.valueOf(period), time.getWeekString()+ "          " + time.getAddress()));
							}
						} catch(BitOperateException e){
							e.printStackTrace();
						}
    	
    	for(int dayOfWeek = 0; dayOfWeek<=6; dayOfWeek++){
    		ArrayList<SimpleCourse> coursesInADay = new ArrayList<SimpleCourse>();
    		for(int period = 1; period<=13; period++){
    			for(SimpleCourse course:lesson[dayOfWeek][period])
    				coursesInADay.add(course);
    		}
    		Bundle argForFragment = new Bundle();
    		argForFragment.putParcelableArrayList(ListCoursesFragment.COURSES_KEY, coursesInADay);
    		args[dayOfWeek] = argForFragment;
    	}
    	
		String[] daysOfWeek = getResources().getStringArray(R.array.days_of_week);
		
		for(int day = 0;day<=6;day++){
			TabSpec tabSpec = mTabHost.newTabSpec(daysOfWeek[day]);
			mTabsAdapter.addTab(tabSpec.setIndicator(daysOfWeek[day]),
					ListCoursesFragment.class, args[day]);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				currentTime.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
			}else{
				mTabHost.getTabWidget().getChildAt(day).setBackgroundResource(R.drawable.tab);
			}	
		}
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			TabWidget tabWidget = mTabHost.getTabWidget();
			for (int i = 0; i < tabWidget.getChildCount(); i++) {  
				View child = tabWidget.getChildAt(i);  

				final TextView tv = (TextView)child.findViewById(android.R.id.title);  
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();  
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0); //取消文字底边对齐  
				params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE); //设置文字居中对齐  

				child.getLayoutParams().height = 80;
			}
		}
		mTabHost.setCurrentTab(mDayOfWeek-1);
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
}
