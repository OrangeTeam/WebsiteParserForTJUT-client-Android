package org.orange.querysystem.content;

import org.orange.querysystem.R;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Course;
import util.webpage.Course.CourseException;
import util.webpage.SchoolWebpageParser;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CourseInfoActivity extends Activity{
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
//	private Button change_info;
//	private Button submit;
	private int course_id;
	private int time_and_adress_counter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_info);
        
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
        
//        change_info = (Button)findViewById(R.id.change_info);
//        submit = (Button)findViewById(R.id.submit);
//        change_info.setText("    修改    ");
//        submit.setText("    提交    ");
        
        SharedPreferences shareData = getSharedPreferences("data", 0);
    	new ReadCourseInfo().execute(shareData.getString("userName", null));
        
        Intent intent = getIntent();
        course_id = intent.getIntExtra("course_info", 0);
	}
	
	public void showCourse(Course course){
		course_code.setText("课程代码：");
		course_class_number.setText("教学班号：");
		course_teacher.setText("任课老师：");
		course_credit.setText( "学        分：");
		course_kind.setText("课程性质：");
		course_test_score.setText("结课成绩：");
		course_total_score.setText("期末总评：");
		course_grade_point.setText("绩        点：");
		course_code_input.setText(course.getCode());
		course_class_number_input.setText(course.getClassNumber());
		course_teacher_input.setText(course.getTeacherString());
		course_credit_input.setText(String.valueOf(course.getCredit()));
		course_kind_input.setText(course.getKind());
		course_test_score_input.setText(String.valueOf(course.getTestScore()));
		course_total_score_input.setText(String.valueOf(course.getTotalScore()));
		for(time_and_adress_counter=0; time_and_adress_counter<course.getTimeAndAddress().size(); time_and_adress_counter++){
			TextView textView = new TextView(this);
			textView.setText("时间地点" + (time_and_adress_counter+1) + ": ");
			textView.setId(time_and_adress_counter*2 + 1);
			textView.setTextSize(18);
			EditText editText = new EditText(this);
			editText.setText(course.getTimeAndAddress().get(time_and_adress_counter).toString());
			editText.setId(time_and_adress_counter*2 + 2);
			editText.setEnabled(false);
			
			RelativeLayout.LayoutParams tvlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			RelativeLayout.LayoutParams etlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
		
		try {
			course_grade_point_input.setText(String.valueOf(course.getGradePoint()));
		} catch (CourseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			course_grade_point_input.setText("-1");
		}
		
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
    		course_code_input.setEnabled(true);
    		course_class_number_input.setEnabled(true);
    		course_teacher_input.setEnabled(true);
    		course_credit_input.setEnabled(true);
    		course_kind_input.setEnabled(true);
    		course_test_score_input.setEnabled(true);
    		course_total_score_input.setEnabled(true);
    		course_grade_point_input.setEnabled(true);
//    		for(int i=0; i < time_and_adress_counter; i++){
//    			findViewById(i*2 + 2).setEnabled(true);
//    		}
    		for(int i=0; i < time_and_adress_counter; i++){
//    			findViewById(i*2 + 2).setEnabled(true);
    			findViewById(i*2 + 2).setOnClickListener(new EditText.OnClickListener(){
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						System.out.println("我能行！");
						
					}
    			});
    		}
    	}
    	else if(item.getItemId() == 2){
    		
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
}
