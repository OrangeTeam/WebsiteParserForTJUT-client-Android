package org.orange.querysystem.content;

import org.orange.querysystem.CourseAndUser;
import org.orange.querysystem.R;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.BitOperate.BitOperateException;
import util.webpage.Course;
import util.webpage.Course.TimeAndAddress;
import util.webpage.Course.TimeAndAddress.TimeAndAddressException;
import util.webpage.SchoolWebpageParser;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AddCourseInfoActivity extends Activity{
	private TextView course_name;
	private TextView course_time_and_adress;
	private TextView course_code;
	private TextView course_class_number;
	private TextView course_teacher;
	private TextView course_credit;
	private TextView course_kind;
	private TextView course_test_score;
	private TextView course_total_score;
	private TextView course_grade_point;
	private EditText course_code_input;
	private EditText course_class_number_input;
	private EditText course_teacher_input;
	private EditText course_credit_input;
	private EditText course_kind_input;
	private EditText course_test_score_input;
	private EditText course_total_score_input;
	private EditText course_grade_point_input;
	private EditText course_name_input;
	private EditText course_time_and_adress_input;
	private TextView week;
	private TextView day_of_week;
	private TextView period;
	private TextView classroom;
	private EditText week_input;
	private EditText day_of_week_input;
	private EditText period_input;
	private EditText classroom_input;
	private Button add;
	private String[] week_get = new String[10];
	private String[] day_of_week_get = new String[10];
	private String[] period_get = new String[10];
	private String[] classroom_get = new String[10];
	private int add_num = 1;
	
	private static final int DIALOG_TEXT_ENTRY = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_course_info);
        
        course_name = (TextView)findViewById(R.id.course_name);
        course_time_and_adress = (TextView)findViewById(R.id.course_time_and_adress);
        course_name_input = (EditText)findViewById(R.id.course_name_input);
        course_time_and_adress_input = (EditText)findViewById(R.id.course_time_and_adress_input);
        
        course_name.setText("课程名称：");
        course_time_and_adress.setText("时间地点1：");
        course_code = (TextView)findViewById(R.id.course_code);
        course_class_number = (TextView)findViewById(R.id.course_class_number);
        course_teacher = (TextView)findViewById(R.id.course_teacher);
        course_credit = (TextView)findViewById(R.id.course_credit);
        course_kind = (TextView)findViewById(R.id.course_kind);
        course_test_score = (TextView)findViewById(R.id.course_test_score);
        course_total_score = (TextView)findViewById(R.id.course_total_score);
        course_grade_point = (TextView)findViewById(R.id.course_grade_point);
        
        course_code_input = (EditText)findViewById(R.id.course_code_input);
        course_class_number_input = (EditText)findViewById(R.id.course_class_number_input);
        course_teacher_input = (EditText)findViewById(R.id.course_teacher_input);
        course_credit_input = (EditText)findViewById(R.id.course_credit_input);
        course_kind_input = (EditText)findViewById(R.id.course_kind_input);
        course_test_score_input = (EditText)findViewById(R.id.course_test_score_input);
        course_total_score_input = (EditText)findViewById(R.id.course_total_score_input);
        course_grade_point_input = (EditText)findViewById(R.id.course_grade_point_input);
        
        add = (Button)findViewById(R.id.add);
//        add.setText("添加");
        add.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				add_num++;
				addTimeAndAddress();
			}
		});
        
        course_code.setText("课程代码：");
		course_class_number.setText("教学班号：");
		course_teacher.setText("任课老师：");
		course_credit.setText( "学        分：");
		course_kind.setText("课程性质：");
		course_test_score.setText("结课成绩：");
		course_total_score.setText("期末总评：");
		course_grade_point.setText("绩        点：");
		course_time_and_adress_input.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog(DIALOG_TEXT_ENTRY);
			}
		});
	}
	
	public void addTimeAndAddress(){
		TextView textView = new TextView(this);
		textView.setText("时间地点" + add_num + "：");
		textView.setId((add_num-2)*2 + 1);
		textView.setTextSize(18);
		EditText editText = new EditText(this);
		editText.setId((add_num-2)*2 + 2);
		editText.setCursorVisible(false);
		editText.setLongClickable(false);
//		editText.setEditableFactory(null);
		editText.setFocusable(false);
		
		RelativeLayout.LayoutParams tvlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams etlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		if((add_num-2) == 0){
			tvlp.addRule(RelativeLayout.BELOW, R.id.course_time_and_adress_input);
			etlp.addRule(RelativeLayout.BELOW, R.id.course_time_and_adress_input);
		}
		else{
			tvlp.addRule(RelativeLayout.BELOW, (add_num-2)*2 - 1);
			etlp.addRule(RelativeLayout.BELOW, (add_num-2)*2);
		}
		tvlp.addRule(RelativeLayout.ALIGN_BASELINE, (add_num-2)*2 + 2);
		etlp.addRule(RelativeLayout.RIGHT_OF, (add_num-2)*2 + 1);
		((RelativeLayout)findViewById(R.id.relativeLayout)).addView(textView, tvlp);
		((RelativeLayout)findViewById(R.id.relativeLayout)).addView(editText, etlp);
		findViewById((add_num-2)*2 + 2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog(DIALOG_TEXT_ENTRY);
			}
		});
		
	}

    @Override
    protected Dialog onCreateDialog(int id) {
    	switch(id){
    	 case DIALOG_TEXT_ENTRY:
             // This example shows how to add a custom layout to an AlertDialog
             LayoutInflater factory = LayoutInflater.from(this);
             final View textEntryView = factory.inflate(R.layout.time_and_adress_entry, null);
             return new AlertDialog.Builder(this)
                 //.setIconAttribute(android.R.attr.alertDialogIcon)
//                 .setTitle(R.string.alert_dialog_text_entry)
                 .setView(textEntryView)
                 .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                    	 
                         /* User clicked OK so do some stuff */
                    	 week = (TextView)textEntryView.findViewById(R.id.week);
                         day_of_week = (TextView)textEntryView.findViewById(R.id.day_of_week);
                         period = (TextView)textEntryView.findViewById(R.id.period);
                         classroom = (TextView)textEntryView.findViewById(R.id.classroom);
                         week_input = (EditText)textEntryView.findViewById(R.id.week_input);
                         day_of_week_input = (EditText)textEntryView.findViewById(R.id.day_of_week_input);
                         period_input = (EditText)textEntryView.findViewById(R.id.period_input);
                         classroom_input = (EditText)textEntryView.findViewById(R.id.classroom_input);
                         week_get[add_num-1] = week_input.getText().toString();
                         day_of_week_get[add_num-1] = day_of_week_input.getText().toString();
                         period_get[add_num-1] = period_input.getText().toString();
                         classroom_get[add_num-1] = classroom_input.getText().toString();
                         if(add_num == 1){
                    		 course_time_and_adress_input.setText(week_get[add_num-1] + "周" + " " + day_of_week_get[add_num-1] + " " + period_get[add_num-1] + "节" + " " + classroom_get[add_num-1]); 
                    	 }
                    	 else
                    	 {
                    		 ((EditText)findViewById((add_num-2)*2 + 2)).setText(week_get[add_num-1] + "周" + " " + day_of_week_get[add_num-1] + " " + period_get[add_num-1] + "节" + " " + classroom_get[add_num-1]);
                    	 }	 
                     }
                 })
                 .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {

                         /* User clicked cancel so do some stuff */
                     }
                 })
                 .create();
    	}
    	return null;
    }
    
    public void updateCoursesListToDatabase(){
    	
        Course course = new Course();
        course.setName(course_name_input.getText().toString());
        course.setCode(course_code_input.getText().toString());
        course.setClassNumber(course_class_number_input.getText().toString());
        course.setTeachers(course_teacher_input.getText().toString());
        try {
//			course.setCredit(Integer.parseInt(course_credit_input.getText().toString()));
//			course.setTestScore(Integer.parseInt(course_test_score_input.getText().toString()));
//	        course.setTotalScore(Integer.parseInt(course_total_score_input.getText().toString()));
        	for(int j=0; j<add_num; j++){
        		TimeAndAddress timeAndAddress = new TimeAndAddress();
        		timeAndAddress.addDays(day_of_week_get[j]);
        		timeAndAddress.addPeriods(period_get[j]);
        		timeAndAddress.addWeeks(week_get[j]);
        		timeAndAddress.setAddress(classroom_get[j]);
        		course.addTimeAndAddress(timeAndAddress);
        	}
	        
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
//		} catch (CourseException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
		} catch (TimeAndAddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 } catch (BitOperateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 } catch (NullPointerException e){
			 e.printStackTrace();
		 }
        course.setKind(course_kind_input.getText().toString());
    	SharedPreferences shareData = getSharedPreferences("data", 0);
        String userName = shareData.getString("userName", null);
        String password = shareData.getString("password", null);
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
			StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(AddCourseInfoActivity.this);
			try {
				parser = new SchoolWebpageParser(new MyParserListener(), args[0].getUserName(), args[0].getPassword());
				studentInfDBAdapter.open();
				studentInfDBAdapter.autoInsertCourseInf(args[0].getCourse(), args[0].getUserName());
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.course_info_submit);
        
        return super.onCreateOptionsMenu(menu); 
    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// TODO Auto-generated method stub\
    	if(item.getItemId() == 1){
    		updateCoursesListToDatabase();
    		finish();
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
}
