package org.orange.querysystem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import org.orange.querysystem.content.ListCoursesFragment.SimpleCourse;
import org.orange.querysystem.content.MainMenuActivity;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.BitOperate.BitOperateException;
import util.webpage.Course;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class MyAppWidgetProvider extends AppWidgetProvider {
	private int mMinute = 0;
	private int mHour = 0;
	private int mDayOfWeek = 0;
	private int period = 0;
	StudentInfDBAdapter studentInfDBAdapter = null;
	
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){        
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		long triggerAtTime = SystemClock.elapsedRealtime() + 15 * 60 * 1000;
		int interval = 15 * 60 * 1000;
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime, interval, pendingIntent);
		updateCourse(context, appWidgetManager, appWidgetIds);
	}
	
	public void onReceive(Context context, Intent intent){
		String action = intent.getAction();
		if(AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)){
			Bundle extras = intent.getExtras();
			if(extras != null){
				int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
				if(appWidgetIds != null && appWidgetIds.length > 0){
					this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
				}
			}
		}
	}
	
	public void updateCourse(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
		SharedPreferences shareData = PreferenceManager.getDefaultSharedPreferences(context);
		if(shareData == null)
		{
			String str = "你还没登入";
			showResult(context, appWidgetManager, appWidgetIds, str);
			return;
		}
		if(shareData.getLong(SettingsActivity.KEY_PREF_SCHOOL_STARTING_DATE, -1) == -1)
		{
			String str = "你还没设置开学时间";
			showResult(context, appWidgetManager, appWidgetIds, str);
			return;
		}
		ArrayList<Course> courses = null;
		studentInfDBAdapter = new StudentInfDBAdapter(context);
		try{
			studentInfDBAdapter.open();
			courses = studentInfDBAdapter.getCoursesFromDB(StudentInfDBAdapter.KEY_YEAR + "=" + 0, null, shareData.getString("userName", null));
		}catch(SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			studentInfDBAdapter.close();
		}
		if(courses == null)
		{
			String str = "你还没刷新数据库";
			showResult(context, appWidgetManager, appWidgetIds, str);
			return;
		}
		int thePeriod = getTime();
		if(thePeriod == 0)
		{
			String str = "此时无课";
			showResult(context, appWidgetManager, appWidgetIds, str);
			return;
		}
		Calendar calendar = Calendar.getInstance();
		mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)-Calendar.MONDAY+1;
		LinkedList<SimpleCourse>[][] lesson = new LinkedList[7][14];
    	for(int day=0;day<=6;day++)
    		for(int period=1;period<=13;period++)
    			lesson[day][period] = new LinkedList<SimpleCourse>();
    	for(Course course:courses)
			for(Course.TimeAndAddress time:course.getTimeAndAddress())
				try{
					if(time.hasSetDay(mDayOfWeek)&&time.hasSetPeriod(thePeriod)&&time.hasSetWeek(SettingsActivity.getCurrentWeekNumber(context) + 1)){
						lesson[mDayOfWeek][thePeriod].addLast(new SimpleCourse(course.getId(),course.getName(),String.valueOf(thePeriod),time.getAddress()));
					}
				} catch(BitOperateException e){
					e.printStackTrace();
				}
    	ArrayList<SimpleCourse> coursesInADay = new ArrayList<SimpleCourse>();
		if(lesson[mDayOfWeek][thePeriod]!=null)
			for(SimpleCourse course:lesson[mDayOfWeek][thePeriod])
				coursesInADay.add(course);
		
		String str = "此时无课程！";
		if(coursesInADay.size() != 0){
			str = coursesInADay.get(0).getName();
			str = str + "  " + coursesInADay.get(0).getOtherInfo();
		}
		showResult(context, appWidgetManager, appWidgetIds, str);
	}
	
	private void showResult(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, String str){
		final int N = appWidgetIds.length;
		for(int i = 0; i < N; i++){
			int appWidgetId = appWidgetIds[i];
			Intent intent;
			if(str == "你还没登入")
			{
				intent = new Intent(context, LoginActivity.class);
			}else{
				intent = new Intent(context, MainMenuActivity.class);
			}
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.myappwidget);
			views.setOnClickPendingIntent(R.id.theWidget, pendingIntent);
			if((str == "你还没登入") || (str == "你还没刷新数据库")){
				views.setTextViewText(R.id.appWidgetTextView, str);
				views.setTextViewText(R.id.widgetPeriod, "");
			}else{
				views.setTextViewText(R.id.appWidgetTextView, str);
				if(period != 0)
				{
					views.setTextViewText(R.id.widgetPeriod, "第" + String.valueOf(period) + "节");
				}
			}
			
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
	
	private int getTime(){
		period = 0;
		Calendar calendar = Calendar.getInstance();
		mMinute = calendar.get(Calendar.MINUTE);
		mHour = calendar.get(Calendar.HOUR_OF_DAY);
		if((mHour <= 8) || ((mHour == 8)&&(mMinute <= 30))){
			period = 1;
		}else{
			if(((mHour == 8)&&(mMinute > 30)) || ((mHour ==9)&&(mMinute <= 25))){
				period = 2;
			}else{
				if(((mHour == 9)&&(mMinute > 25)) || ((mHour == 10)&&(mMinute <= 40))){
					period = 3;
				}else{
					if(((mHour == 10)&&(mMinute > 40)) || ((mHour == 11)&&(mMinute <= 35))){
						period = 4;
					}else{
						if(((mHour == 11)&&(mMinute > 35)) || ((mHour == 12)&&(mMinute <= 45))){
							period = 5;
						}else{
							if(((mHour == 12)&&(mMinute > 45)) || (mHour == 13) || ((mHour == 14)&&(mMinute <= 30))){
								period = 7;
							}else{
								if(((mHour == 14)&&(mMinute > 30)) || ((mHour == 15)&&(mMinute <= 25))){
									period = 8;
								}else{
									if(((mHour == 15)&&(mMinute > 25)) || ((mHour == 16)&&(mMinute <= 40))){
										period = 9;
									}else{
										if(((mHour == 16)&&(mMinute > 40)) || ((mHour == 17)&&(mMinute <= 35))){
											period = 10;
										}else{
											if(((mHour == 17)&&(mMinute > 35)) || (mHour == 18)){
												period = 11;
											}else{
												if((mHour == 19)&&(mMinute <= 55)){
													period = 12;
												}else{
													if(((mHour == 19)&&(mMinute > 55)) || ((mHour == 21)&&(mMinute <= 5))){
														period = 13;
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return period;
	}
}
