package org.orange.querysystem.content;

import java.util.ArrayList;

import org.orange.querysystem.R;
import org.orange.querysystem.content.ReadDB.OnPostExcuteListerner;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Course;
import util.webpage.Course.CourseException;
import util.webpage.SchoolWebpageParser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class CourseInfoActivity extends Activity{
	private TextView course_info;
	private int course_id;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_info);
        
        course_info = (TextView)findViewById(R.id.course_info);
        SharedPreferences shareData = getSharedPreferences("data", 0);
    	new ReadCourseInfo().execute(shareData.getString("userName", null));
        
        Intent intent = getIntent();
        course_id = intent.getIntExtra("course_info", 0);
	}
	
	public void showCourse(Course course){
		try {
			course_info.setText("课程代码：    " + course.getCode() + "\n" +
								"教学班号：    " + course.getClassNumber() + "\n" + 
								"任课老师：    " + course.getTeacherString() + "\n" +
								"学         分：    " + course.getCredit() + "\n" +
								"课程性质：    " + course.getKind() + "\n" +
								"结课考核成绩：" + course.getTestScore() + "\n" +
								"总评成绩：     " + course.getTotalScore() + "\n" + 
								"绩         点：    " + course.getGradePoint());
		} catch (CourseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			course_info.setText("课程代码：    " + course.getCode() + "\n" +
					"教学班号：    " + course.getClassNumber() + "\n" + 
					"任课老师：    " + course.getTeacherString() + "\n" +
					"学         分：    " + course.getCredit() + "\n" +
					"课程性质：    " + course.getKind() + "\n" +
					"结课考核成绩：" + course.getTestScore() + "\n" +
					"总评成绩：     " + course.getTotalScore() + "\n" + 
					"绩         点：    " + "-1");
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
				/*新增 此捕获、两个close和
				 * 79行捕获*/
			} finally{
				closeDatabase();
			}
			
			return result;
		}

		@Override
		protected void onCancelled() {
			closeDatabase();
		}

		@Override
		protected void onPostExecute(Course course){
			if(course != null)
				showCourse(course);
			else{
				Toast.makeText(CourseInfoActivity.this, "no data in the database!", Toast.LENGTH_LONG).show();			
			}
		}
		
		private void closeDatabase(){
			if(studentInfDBAdapter != null)
				try{
					studentInfDBAdapter.close();
				}catch(NullPointerException e){
					e.printStackTrace();
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
}
