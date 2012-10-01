package org.orange.querysystem.content;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.orange.querysystem.ApplicationExit;
import org.orange.querysystem.LoginActivity;
import org.orange.querysystem.MenuActivity;
import org.orange.querysystem.R;
import org.orange.querysystem.content.ReadDB.OnPostExcuteListerner;

import util.BitOperate.BitOperateException;
import util.webpage.Constant;
import util.webpage.Course;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.korovyansk.android.slideout.SlideoutActivity;

public class WeekCourseListActivity extends ListActivity implements OnPostExcuteListerner{
	
	private TextView courseListTitle;
	private Button refresh;
	private Button refresh_db;
	private Spinner spinner = null;
	private int year = 0;
	private int month = 0;
	private int day = 0;
	private int hour = 0;
	private int minute = 0;
	private int second = 0;
	private int week = 0;
	private int dayOfWeek = 0;
	private TextView dialog_title;
	private TextView input_year;
	private TextView input_month;
	private TextView input_date;
	private EditText input_year_box;
	private EditText input_month_box;
	private EditText input_date_box;
	private int input_year_get = 0;
	private int input_month_get = 0;
	private int input_day_get = 0;
	private int calculate_week = 0;
	private int real_week = 0;
	
	
	static final int DATE_DIALOG_ID = 1;
	
	@Override
	protected void onStart(){
		   super.onStart();
		   ApplicationExit appExit = (ApplicationExit)getApplication();
		   if(appExit.isExit()){
			   finish();
		   }
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week_course_list_main);
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);//比正常少一个月
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);
        week = calendar.get(Calendar.WEEK_OF_YEAR);//正常
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);//比正常的多一天
        
        refresh = (Button)findViewById(R.id.refresh);
        refresh.setBackgroundResource(R.drawable.ic_action_refresh);
        refresh_db = (Button)findViewById(R.id.refresh_db);
		ArrayAdapter<CharSequence> menu_adapter = ArrayAdapter.createFromResource(this, R.array.week_course_menu_array, android.R.layout.simple_spinner_item); 
		menu_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner = (Spinner)findViewById(R.id.course_spinner_menu);
		spinner.setAdapter(menu_adapter);
		spinner.setPrompt("课程表");
		spinner.setOnItemSelectedListener(new SpinnerOnSelectedListener());
		showDialog(DATE_DIALOG_ID);
        
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                            mDateSetListener,
                            year, month, day);
        }
        return null;
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(year, month, day);
                break;
        }
    }    

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year_choice, int month_choice,
                        int day_choice) {
                    input_year_get = year_choice;
                    input_month_get = month_choice;
                    input_day_get = day_choice;
                    readDB();
                }
            };
    
    class SpinnerOnSelectedListener implements OnItemSelectedListener{
    	@Override
    	public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id){
    		if(adapterView.getItemIdAtPosition(position) == 0){
    			
    		}
    		if(adapterView.getItemIdAtPosition(position) == 1){
    			startActivity(new Intent(WeekCourseListActivity.this, DayCourseListActivity.class));
    		}
    		if(adapterView.getItemIdAtPosition(position) == 2){
    			startActivity(new Intent(WeekCourseListActivity.this, AllCourseListActivity.class));
    		}
    			
    	}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
			// TODO Auto-generated method stub
			
		}
    }
//    }
    
    public void readDB(){
    	new ReadDB(this, this).execute();
    }
    
    @Override
   	public void onPostReadFromDB(ArrayList<Course> courses) {
   			showCoursesInfo(courses);
   	}
    
    public void showCoursesInfo(ArrayList<Course> courses){
    	courseListTitle = (TextView)findViewById(R.id.course_list_title);
        
        Calendar calendar_2 = Calendar.getInstance();
        calendar_2.set(input_year_get, input_month_get, input_day_get);
        calculate_week =  week - calendar_2.get(Calendar.WEEK_OF_YEAR);
        real_week = calculate_week + 1;
        courseListTitle.setText("第" + real_week + "周课程表");
        
        String[] lessonOne = new String[]{" " ,"" ,"" ,"" ,"" ,"" ,""};
    	String[] lessonTwo = new String[]{" " ,"" ,"" ,"" ,"" ,"" ,""};
    	String[] lessonThree = new String[]{" " ,"" ,"" ,"" ,"" ,"" ,""};
    	String[] lessonFour = new String[]{" " ,"" ,"" ,"" ,"" ,"" ,""};
    	String[] lessonFive = new String[]{" " ,"" ,"" ,"" ,"" ,"" ,""};
    	String[] lessonSix = new String[]{" " ,"" ,"" ,"" ,"" ,"" ,""};
        
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map1 = new HashMap<String, String>();
		HashMap<String, String> map2 = new HashMap<String, String>();
		HashMap<String, String> map3 = new HashMap<String, String>();
		HashMap<String, String> map4 = new HashMap<String, String>();
		HashMap<String, String> map5 = new HashMap<String, String>();
		HashMap<String, String> map6 = new HashMap<String, String>();
		HashMap<String, String> map7 = new HashMap<String, String>();
		
		map1.put("blank", "");
		map1.put("Monday", "周一");
		map1.put("Tuesday", "周二");
		map1.put("Wednesday", "周三");
		map1.put("Thursday", "周四");
		map1.put("Friday", "周五");
		map1.put("Saturday", "周六");
		map1.put("Sunday", "周日");
		map2.put("blank", "1-2节");
		map3.put("blank", "3-4节");
		map4.put("blank", "5-6节");
		map5.put("blank", "7-8节");
		map6.put("blank", "9-10节");
		map7.put("blank", "11-13节");
		try{
		for(int i=0; i<courses.size(); i++){
			for(int j=0; j<courses.get(i).getTimeAndAddress().size(); j++ ){
				if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(1) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(2) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(1) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonOne[0] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map2.put("Monday", lessonOne[0]);
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(1) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(2) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(2) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonOne[1] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map2.put("Tuesday", lessonOne[1]);
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(1) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(2) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(3) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonOne[2] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map2.put("Wednesday", lessonOne[2]);
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(1) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(2) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(4) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonOne[3] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map2.put("Thursday", lessonOne[3]);
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(1) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(2) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(5) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonOne[4] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map2.put("Friday", lessonOne[4]);
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(1) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(2) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(6) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonOne[5] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map2.put("Saturday", lessonOne[5]);
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(1) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(2) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(7) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonOne[6] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map2.put("Sunday", lessonOne[6]);
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(3) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(4) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(1) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonTwo[0] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map3.put("Monday", lessonTwo[0]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(3) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(4) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(2) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonTwo[1] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map3.put("Tuesday", lessonTwo[1]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(3) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(4) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(3) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonTwo[2] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map3.put("Wednesday", lessonTwo[2]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(3) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(4) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(4) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonTwo[3] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map3.put("Thursday", lessonTwo[3]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(3) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(4) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(5) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonTwo[4] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map3.put("Friday", lessonTwo[4]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(3) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(4) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(6) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonTwo[5] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map3.put("Saturday", lessonTwo[5]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(3) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(4) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(7) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonTwo[6] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map3.put("Sunday", lessonTwo[6]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(5) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(6) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(1) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonThree[0] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map4.put("Monday", lessonThree[0]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(5) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(6) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(2) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonThree[1] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map4.put("Tuesday", lessonThree[1]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(5) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(6) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(3) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonThree[2] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map4.put("Wednesday", lessonThree[2]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(5) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(6) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(4) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonThree[3] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map4.put("Thursday", lessonThree[3]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(5) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(6) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(5) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonThree[4] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map4.put("Friday", lessonThree[4]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(5) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(6) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(6) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonThree[5] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map4.put("Saturday", lessonThree[5]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(5) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(6) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(7) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonThree[6] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map4.put("Sunday", lessonThree[6]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(7) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(8) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(1) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonFour[0] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map5.put("Monday", lessonFour[0]);
				
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(7) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(8) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(2) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonFour[1] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map5.put("Tuesday", lessonFour[1]);
				
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(7) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(8) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(3) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonFour[2] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map5.put("Wednesday", lessonFour[2]);
				
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(7) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(8) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(4) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonFour[3] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map5.put("Thursday", lessonFour[3]);
				
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(7) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(8) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(5) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonFour[4] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map5.put("Friday", lessonFour[4]);
				
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(7) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(8) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(6) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonFour[5] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map5.put("Saturday", lessonFour[5]);
				
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(7) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(8) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(7) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonFour[6] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map5.put("Sunday", lessonFour[6]);
				
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(9) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(10) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(1) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonFive[0] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map6.put("Monday", lessonFive[0]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(9) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(10) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(2) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonFive[1] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map6.put("Tuesday", lessonFive[1]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(9) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(10) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(3) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonFive[2] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map6.put("Wednesday", lessonFive[2]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(9) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(10) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(4) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonFive[3] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map6.put("Thursday", lessonFive[3]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(9) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(10) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(5) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonFive[4] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map6.put("Friday", lessonFive[4]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(9) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(10) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(6) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonFive[5] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map6.put("Saturday", lessonFive[5]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(9) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(10) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(7) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonFive[6] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map6.put("Sunday", lessonFive[6]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(11) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(12) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(13) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(1) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonSix[0] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map7.put("Monday", lessonSix[0]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(11) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(12) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(13) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(2) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonSix[1] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map7.put("Tuesday", lessonSix[1]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(11) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(12) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(13) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(3) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonSix[2] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map7.put("Wednesday", lessonSix[2]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(11) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(12) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(13) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(4) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonSix[3] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map7.put("Thursday", lessonSix[3]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(11) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(12) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(13) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(5) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonSix[4] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map7.put("Friday", lessonSix[4]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(11) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(12) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(13) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(6) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonSix[5] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map7.put("Saturday", lessonSix[5]);
					
				}
				else if(courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(11) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(12) && courses.get(i).getTimeAndAddress().get(j).hasSetPeriod(13) && courses.get(i).getTimeAndAddress().get(j).hasSetDay(7) && courses.get(i).getTimeAndAddress().get(j).hasSetWeek(real_week)){
					lessonSix[6] += courses.get(i).getName() + "\n" +courses.get(i).getTimeAndAddress().get(j).getAddress();
					map7.put("Sunday", lessonSix[6]);
					
				}
			}
				
		}
		}catch(BitOperateException e){
			e.printStackTrace();
		}
		
		list.add(map1);
		list.add(map2);
		list.add(map3);
		list.add(map4);
		list.add(map5);
		list.add(map6);
		list.add(map7);
		SimpleAdapter listAdapter = new MySimpleAdapter(this, list, R.layout.week_course_list_listview, new String[]{"blank", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}, new int[] {R.id.blank, R.id.Monday, R.id.Tuesday, R.id.Wednesday, R.id.Thursday, R.id.Friday, R.id.Saturday, R.id.Sunday});
		setListAdapter(listAdapter);
		
		
		
//		ListView listView = (ListView)findViewById(android.R.id.list);
//		Drawable drawable = getResources().getDrawable(R.drawable.pic);
//		listView.setSelector(drawable);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.exit);
        menu.add(0, 2, 2, R.string.change_number);
        menu.add(0, 3, 3, R.string.about);
        menu.add(0, 4, 4, R.string.course_query);
        menu.add(0, 5, 5, R.string.score_query);
        menu.add(0, 6, 6, R.string.post_query);
        return super.onCreateOptionsMenu(menu); 
    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// TODO Auto-generated method stub\
    	if(item.getItemId() == 1){
    		finish();
    	}
    	else if(item.getItemId() == 2){
    		Editor editor = getSharedPreferences("data", 0).edit();
    		editor.remove("logIn_auto");
    		editor.putBoolean("logIn_auto", false);
    		editor.commit();
    		startActivity(new Intent(this, LoginActivity.class));
    	}
    	else if(item.getItemId() == 4){
    		readDB();
    	}
    	else if(item.getItemId() == 5){
    		startActivity(new Intent(this, TerminalScoreActivity.class));
    	}
    	else{
    		startActivity(new Intent(this, StudentInfoActivity.class));
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {    
        super.onConfigurationChanged(newConfig);
        // 检测屏幕的方向：纵向或横向
        if (this.getResources().getConfiguration().orientation 
                == Configuration.ORIENTATION_LANDSCAPE) {
            //当前为横屏， 在此处添加额外的处理代码
        }
        else if (this.getResources().getConfiguration().orientation 
                == Configuration.ORIENTATION_PORTRAIT) {
            //当前为竖屏， 在此处添加额外的处理代码
        }
        //检测实体键盘的状态：推出或者合上    
        if (newConfig.hardKeyboardHidden 
                == Configuration.HARDKEYBOARDHIDDEN_NO){ 
            //实体键盘处于推出状态，在此处添加额外的处理代码
        } 
        else if (newConfig.hardKeyboardHidden
                == Configuration.HARDKEYBOARDHIDDEN_YES){ 
            //实体键盘处于合上状态，在此处添加额外的处理代码
        }
    }
    
    @Override
    public void onBackPressed() {
    	//实现Home键效果
    	//super.onBackPressed();这句话一定要注掉,不然又去调用默认的back处理方式了
    	Intent i= new Intent(Intent.ACTION_MAIN);
    	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	i.addCategory(Intent.CATEGORY_HOME);
    	startActivity(i);
    }

    public void refreshdb(View view){
      	startActivity(new Intent(this, InsertDBFragmentActivity.class));   	
    }
    public void refreshActivity(View view){
    	showDialog(DATE_DIALOG_ID);
    }
}