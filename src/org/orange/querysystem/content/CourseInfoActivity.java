package org.orange.querysystem.content;

import java.util.ArrayList;

import org.orange.querysystem.CourseAndUser;
import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.BitOperate.BitOperateException;
import util.webpage.Course;
import util.webpage.Course.CourseException;
import util.webpage.Course.TimeAndAddress;
import util.webpage.Course.TimeAndAddress.TimeAndAddressException;
import util.webpage.SchoolWebpageParser;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CourseInfoActivity extends FragmentActivity{
	private TextView course_name;
	private TextView course_code;
	private TextView course_class_number;
	private TextView course_teacher;
	private TextView course_credit;
	private TextView course_kind;
	private TextView course_test_score;
	private TextView course_total_score;
	private TextView course_grade_point;
	private EditText course_name_input;
	private EditText course_code_input;
	private EditText course_class_number_input;
	private EditText course_teacher_input;
	private EditText course_credit_input;
	private EditText course_kind_input;
	private EditText course_test_score_input;
	private EditText course_total_score_input;
	private EditText course_grade_point_input;
	private TextView week;
	private TextView day_of_week;
	private TextView period;
	private TextView classroom;
	private static EditText week_input;
	private static EditText day_of_week_input;
	private static EditText period_input;
	private static EditText classroom_input;
	private int course_id;
	private int time_and_adress_counter;
	private int time_and_adress_sign = 0;
	private String week_get = "";
	private String day_of_week_get = "";
	private String period_get = "";
	private String classroom_get = "";
	private static String choice_result = "";
	private int add_num = 1;
	private static int address_choice = 0;
	private static int choice_input_address = 0;
	private static int choice_num = 0;
	private static String address_choice_title = "";
	private static ArrayList mSelectedItems;
	
	private static final int ITEM1 = Menu.FIRST;
	private static final int ITEM2 = Menu.FIRST+1;
	private static final int ITEM3 = Menu.FIRST+2;
	private static final int ITEM4 = Menu.FIRST+3;
	
	private static final int DIALOG_TEXT_ENTRY = 1;
	
	ArrayList<TimeAndAddress> timeAndAddresses = new ArrayList<TimeAndAddress>();
    TimeAndAddress timeAndAddress = new TimeAndAddress();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_info);
        
        course_name = (TextView)findViewById(R.id.course_name);
        course_code = (TextView)findViewById(R.id.course_code);
        course_class_number = (TextView)findViewById(R.id.course_class_number);
        course_teacher = (TextView)findViewById(R.id.course_teacher);
        course_credit = (TextView)findViewById(R.id.course_credit);
        course_kind = (TextView)findViewById(R.id.course_kind);
        course_test_score = (TextView)findViewById(R.id.course_test_score);
        course_total_score = (TextView)findViewById(R.id.course_total_score);
        course_grade_point = (TextView)findViewById(R.id.course_grade_point);
        
        course_name_input = (EditText)findViewById(R.id.course_name_input);
        course_code_input = (EditText)findViewById(R.id.course_code_input);
        course_class_number_input = (EditText)findViewById(R.id.course_class_number_input);
        course_teacher_input = (EditText)findViewById(R.id.course_teacher_input);
        course_credit_input = (EditText)findViewById(R.id.course_credit_input);
        course_kind_input = (EditText)findViewById(R.id.course_kind_input);
        course_test_score_input = (EditText)findViewById(R.id.course_test_score_input);
        course_total_score_input = (EditText)findViewById(R.id.course_total_score_input);
        course_grade_point_input = (EditText)findViewById(R.id.course_grade_point_input);
        
    	new ReadCourseInfo().execute(SettingsActivity.getAccountStudentID(this));
        
        Intent intent = getIntent();
        course_id = intent.getIntExtra("course_info", 0);
	}
	
	public void showCourse(Course course){
		course_id = course.getId();
		course_name.setText("课程名称：");
		course_code.setText("课程代码：");
		course_class_number.setText("教学班号：");
		course_teacher.setText("任课老师：");
		course_credit.setText( "学        分：");
		course_kind.setText("课程性质：");
		course_test_score.setText("结课成绩：");
		course_total_score.setText("期末总评：");
		course_grade_point.setText("绩        点：");
		course_name_input.setText(course.getName());
		course_code_input.setText(course.getCode());
		course_class_number_input.setText(course.getClassNumber());
		course_teacher_input.setText(course.getTeacherString());
		course_credit_input.setText(String.valueOf(course.getCredit()));
		course_kind_input.setText("抱歉！暂时无数据");
		if(course.getTestScore() == -1 && course.getTotalScore() == -1){
			course_test_score_input.setText("抱歉！暂时无数据");
			course_total_score_input.setText("抱歉！暂时无数据");
		}else{
			course_test_score_input.setText(String.valueOf(course.getTestScore()));
			course_total_score_input.setText(String.valueOf(course.getTotalScore()));
		}
		try {
			course_grade_point_input.setText(String.valueOf(course.getGradePoint()));
		} catch (CourseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			course_grade_point_input.setText("抱歉！暂时无数据");
		}
		for(time_and_adress_counter=0; time_and_adress_counter<course.getTimeAndAddress().size(); time_and_adress_counter++){
			TextView textView = new TextView(this);
			textView.setText("时间地点" + (time_and_adress_counter+1) + ": ");
			textView.setId(time_and_adress_counter*2 + 1);
			textView.setTextSize(18);
			textView.setTextColor(Color.rgb(0, 0, 0));
			EditText editText = new EditText(this);
			editText.setText(course.getTimeAndAddress().get(time_and_adress_counter).toString());

			timeAndAddresses.add(new TimeAndAddress(course.getTimeAndAddress().get(time_and_adress_counter)));
			editText.setId(time_and_adress_counter*2 + 2);
			editText.setEnabled(false);
			editText.setCursorVisible(false);
			editText.setLongClickable(false);
			editText.setFocusable(false);

			RelativeLayout.LayoutParams tvlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			RelativeLayout.LayoutParams etlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			if(time_and_adress_counter == 0){
				tvlp.addRule(RelativeLayout.BELOW, R.id.course_grade_point_input);
				etlp.addRule(RelativeLayout.BELOW, R.id.course_grade_point_input);
			}
			else{
				tvlp.addRule(RelativeLayout.BELOW, time_and_adress_counter*2 - 1);
				etlp.addRule(RelativeLayout.BELOW, time_and_adress_counter*2);
			}
			tvlp.addRule(RelativeLayout.ALIGN_BASELINE, time_and_adress_counter*2 + 2);
			etlp.addRule(RelativeLayout.RIGHT_OF, time_and_adress_counter*2 + 1);
			((RelativeLayout)findViewById(R.id.relativeLayout)).addView(textView, tvlp);
			((RelativeLayout)findViewById(R.id.relativeLayout)).addView(editText, etlp);
		}
		//为了与原代码一致，让timeAndAddress成为timeAndAddresses的最后一个
		if(!course.getTimeAndAddress().isEmpty())
			timeAndAddress = new TimeAndAddress(timeAndAddresses.get(timeAndAddresses.size()-1));
	}
	
	public class ReadCourseInfo extends AsyncTask<String,Void,Course>{
		StudentInfDBAdapter studentInfDBAdapter = null;
		public static final String TAG = "org.orange.querysystem";

		@Override
		protected Course doInBackground(String... args) {
			Course result = null;
			studentInfDBAdapter = new StudentInfDBAdapter(CourseInfoActivity.this);
			try {
				studentInfDBAdapter.open();
				result = studentInfDBAdapter.getCourseFromDB(StudentInfDBAdapter.KEY_ID + "=" + course_id, args[0]);
			} catch(SQLException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally{
				studentInfDBAdapter.close();
			}
			
			return result;
		}

		@Override
		protected void onCancelled() {
			studentInfDBAdapter.close();
		}

		@Override
		protected void onPostExecute(Course course){
			if(course != null)
				showCourse(course);
			else{
				Toast.makeText(CourseInfoActivity.this, "no data in the database!", Toast.LENGTH_LONG).show();			
			}
		}
		
		class MyParserListener extends SchoolWebpageParser.ParserListenerAdapter{

			/* (non-Javadoc)
			 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onError(int, java.lang.String)
			 */
			@Override
			public void onError(int code, String message) {
				Log.e(TAG, message);
			}

			/* (non-Javadoc)
			 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onWarn(int, java.lang.String)
			 */
			@Override
			public void onWarn(int code, String message) {
				Log.w(TAG, message);
			}

			/* (non-Javadoc)
			 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onInformation(int, java.lang.String)
			 */
			@Override
			public void onInformation(int code, String message) {
				Log.i(TAG, message);
			}
		}	
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.course_info_change);
        menu.add(0, 2, 2, R.string.course_info_submit);
        
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// TODO Auto-generated method stub\
    	if(item.getItemId() == 1){
    		course_name_input.setEnabled(true);
    		course_code_input.setEnabled(true);
    		course_class_number_input.setEnabled(true);
    		course_teacher_input.setEnabled(true);
    		course_credit_input.setEnabled(true);
    		course_kind_input.setEnabled(true);
    		course_test_score_input.setEnabled(true);
    		course_total_score_input.setEnabled(true);
    		course_grade_point_input.setEnabled(true);
    		for(time_and_adress_sign=0; time_and_adress_sign < time_and_adress_counter; time_and_adress_sign++){
    			findViewById(time_and_adress_sign*2 + 2).setEnabled(true);
    	        findViewById(time_and_adress_sign*2 + 2).setOnClickListener(new EditText.OnClickListener(){
    	        	public void onClick(View v) {
    	        		showDialog(v.getId()/2);
    	        		}
    	        	});
    		}
    	}
    	else if(item.getItemId() == 2){
    		updateCoursesListToDatabase();
    		finish();
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
    protected Dialog onCreateDialog(final int id) {
    	LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.time_and_adress_entry, null);
        
        week = (TextView)textEntryView.findViewById(R.id.week);
        day_of_week = (TextView)textEntryView.findViewById(R.id.day_of_week);
        period = (TextView)textEntryView.findViewById(R.id.period);
        classroom = (TextView)textEntryView.findViewById(R.id.classroom);
        week_input = (EditText)textEntryView.findViewById(R.id.week_input);
        day_of_week_input = (EditText)textEntryView.findViewById(R.id.day_of_week_input);
        period_input = (EditText)textEntryView.findViewById(R.id.period_input);
        classroom_input = (EditText)textEntryView.findViewById(R.id.classroom_input);

        week_input.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				address_choice = R.array.week;
				address_choice_title = "周数";
				choice_num = 0;
				choice_result = "";
				choice_input_address = R.id.week_input;
				showDialog();
			}
		 });
        day_of_week_input.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				address_choice = R.array.days_of_week;
				address_choice_title = "星期数";
				choice_num = 0;
				choice_result = "";
				choice_input_address = R.id.day_of_week_input;
				showDialog();
			}
		 });
        period_input.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				address_choice_title = "节数";
				address_choice = R.array.period;
				choice_num = 0;
				choice_result = "";
				choice_input_address = R.id.period_input;
				showDialog();
			}
		 });
        return new AlertDialog.Builder(this)
        .setView(textEntryView)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
           	 
                /* User clicked OK so do some stuff */
           	 	week = (TextView)textEntryView.findViewById(R.id.week);
                day_of_week = (TextView)textEntryView.findViewById(R.id.day_of_week);
                period = (TextView)textEntryView.findViewById(R.id.period);
                classroom = (TextView)textEntryView.findViewById(R.id.classroom);
                
                week_get = week_input.getText().toString();
                day_of_week_get = day_of_week_input.getText().toString();
                period_get = period_input.getText().toString();
                classroom_get = classroom_input.getText().toString();
                ((EditText) findViewById((id-1) * 2 + 2)).setText(week_get + "周" + " " + day_of_week_get + " " + period_get + "节" + " " + classroom_get);
                try {
					timeAndAddresses.get(id-1).setWeek(0).addWeeks(week_get);
					timeAndAddresses.get(id-1).setDay(0).addDays(day_of_week_get);
					timeAndAddresses.get(id-1).setPeriod(0).addPeriods(period_get);
					timeAndAddresses.get(id-1).setAddress(classroom_get);
				} catch (BitOperateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TimeAndAddressException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                /* User clicked cancel so do some stuff */
            }
        })
        .create();    
    }
    
    public void showDialog(){
    	DialogFragment newFragment = MyAlertDialogFragment.newInstance(
                android.R.string.ok);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }
    
    public void doPositiveClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Positive click!");
    }
    
    public void doNegativeClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Negative click!");
    }

    

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int title) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }
        
        @Override
    	public Dialog onCreateDialog(Bundle savedInstanceState) {
    	    mSelectedItems = new ArrayList();  // Where we track the selected items
    	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	    // Set the dialog title
    	    builder.setTitle(address_choice_title)
    	    // Specify the list array, the items to be selected by default (null for none),
    	    // and the listener through which to receive callbacks when items are selected
    	           .setMultiChoiceItems(address_choice, null,
    	                      new DialogInterface.OnMultiChoiceClickListener() {
    	               @Override
    	               public void onClick(DialogInterface dialog, int which,
    	                       boolean isChecked) {
    	                   if (isChecked) {
    	                       // If the user checked the item, add it to the selected items
    	                       mSelectedItems.add(which);
    	                       choice_num++;
    	                   } else if (mSelectedItems.contains(which)) {
    	                       // Else, if the item is already in the array, remove it 
    	                       mSelectedItems.remove(Integer.valueOf(which));
    	                       choice_num--;
    	                   }
    	               }
    	           })
    	    // Set the action buttons
    	           .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    	               @Override
    	               public void onClick(DialogInterface dialog, int id) {
    	                   // User clicked OK, so save the mSelectedItems results somewhere
    	                   // or return them to the component that opened the dialog
//    	                   ...
    	            	   Resources res = getResources();
    	            	   String[] content = res.getStringArray(address_choice);
    	            	   System.out.println(choice_result);
    	            	   if(choice_input_address == R.id.week_input){
    	            		   for(int i=0; i<choice_num; i++){
        	            		   if(i == choice_num-1){
        	            			   choice_result = choice_result + content[(Integer) mSelectedItems.get(i)];
        	            			   break;
        	            		   }
        	                	   choice_result = choice_result + content[(Integer) mSelectedItems.get(i)] + ",";
        	                   }
    	            		   week_input.setText(choice_result); 
    	            	   }
    	            	   if(choice_input_address == R.id.day_of_week_input){
    	            		   for(int i=0; i<choice_num; i++){
        	            		   if(i == choice_num-1){
        	            			   choice_result = choice_result + content[(Integer) mSelectedItems.get(i)];
        	            			   break;
        	            		   }
        	                	   choice_result = choice_result + content[(Integer) mSelectedItems.get(i)] + ",";
        	                   } 
    	            		   day_of_week_input.setText(choice_result);  
    	            	   }
    	            	   if(choice_input_address == R.id.period_input){
    	            		   for(int i=0; i<choice_num; i++){
        	            		   if(i == choice_num-1){
        	            			   choice_result = choice_result + content[(Integer) mSelectedItems.get(i)];
        	            			   break;
        	            		   }
        	                	   choice_result = choice_result + content[(Integer) mSelectedItems.get(i)] + ",";
        	                   } 
    	            		   period_input.setText(choice_result); 
    	            	   }
    	            	   
    	               }
    	           })
    	           .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
    	               @Override
    	               public void onClick(DialogInterface dialog, int id) {
//    	                   ...
    	               }
    	           });

    	    return builder.create();
    	}
    }
    
    public void updateCoursesListToDatabase(){
    	
    	Course course = new Course();
    	course.setId(course_id);
        course.setName(course_name_input.getText().toString());
        course.setCode(course_code_input.getText().toString());
        course.setClassNumber(course_class_number_input.getText().toString());
        course.setTeachers(course_teacher_input.getText().toString());
        try {
        	course.setTimeAndAddresse(timeAndAddresses);
			course.setCredit(Integer.parseInt(course_credit_input.getText().toString()));
			course.setTestScore(Integer.parseInt(course_test_score_input.getText().toString()));
			course.setTotalScore(Integer.parseInt(course_total_score_input.getText().toString()));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CourseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        course.setKind(course_kind_input.getText().toString());
        String userName = SettingsActivity.getAccountStudentID(this);
        String password = SettingsActivity.getAccountPassword(this);
        CourseAndUser courseAndUser = new CourseAndUser(course, userName, password);
        new UpdateCoursesListToDatabase().execute(courseAndUser);
    }
    
    class UpdateCoursesListToDatabase extends AsyncTask<CourseAndUser,Void,Void>{
		
		public static final String TAG = "org.orange.querysystem";
		public static final int PARSE_COURSE = 1;
		public static final int PARSE_SCORE = 2;

		@Override
		protected Void doInBackground(CourseAndUser... args) {
			
			SchoolWebpageParser parser = null;
			StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(CourseInfoActivity.this);
			try {
				parser = new SchoolWebpageParser(new MyParserListener(), args[0].getUserName(), args[0].getPassword());
				studentInfDBAdapter.open();
				System.out.println(args[0].getCourse());
				studentInfDBAdapter.updateCourseInf(args[0].getCourse());
				studentInfDBAdapter.updateScoreInf(args[0].getCourse());
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(SQLiteException e){
				e.printStackTrace();
			} 
			studentInfDBAdapter.close();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void course){
			
		}
		
		class MyParserListener extends SchoolWebpageParser.ParserListenerAdapter{

			/* (non-Javadoc)
			 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onError(int, java.lang.String)
			 */
			@Override
			public void onError(int code, String message) {
				Log.e(TAG, message);
			}

			/* (non-Javadoc)
			 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onWarn(int, java.lang.String)
			 */
			@Override
			public void onWarn(int code, String message) {
				Log.w(TAG, message);
			}

			/* (non-Javadoc)
			 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onInformation(int, java.lang.String)
			 */
			@Override
			public void onInformation(int code, String message) {
				Log.i(TAG, message);
			}
		}	
	}
}
