package org.orange.querysystem;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.orange.querysystem.AllCoursesActivity.CourseToSimpleCourse;
import org.orange.querysystem.content.ListCoursesFragment;
import org.orange.querysystem.content.ListCoursesFragment.SimpleCourse;
import org.orange.querysystem.util.ReadDB;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Course;
import util.webpage.Student;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class AllCoursesInNextSemesterActivity extends CoursesInThisWeekActivity {

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new RefreshCurrentSemester().execute();
	}
	@Override
	@TargetApi(11)
	protected void setTitle() {
		super.setTitle();
		String title = getString(R.string.curriculum_schedule_in_next_semester);
		//3.0以上版本，使用ActionBar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getActionBar().setTitle(title);
		else
			((TextView)findViewById(R.id.currentTime))
				.setText(title + "\t\t" + DateFormat.getDateInstance().format(new Date()));
	}

	@Override
	public void readDB(){
    	new ReadDB(this, this).execute(SettingsActivity.getAccountStudentID(this), "next");
    }
    
    @Override
	public void onPostReadFromDB(ArrayList<Course> courses) {
		if(courses != null)
			showCoursesInfo(courses, AllCoursesActivity.mCourseToSimpleCourse);
		else
			Toast.makeText(this, "无下学期课程", Toast.LENGTH_SHORT).show();
	}

	@Override
    public void showCoursesInfo(List<Course> courses, CourseToSimpleCourse converter){
		mTabsAdapter.clear();

		Bundle[] args = new Bundle[8];
		List<SimpleCourse>[][] lesson = AllCoursesActivity.getTimeTable(courses, converter);

		//把每天的课程放到传到ListCoursesFragment的参数容器中
		for(int dayOfWeek = 0; dayOfWeek<=7; dayOfWeek++){
    		ArrayList<SimpleCourse> coursesInADay = new ArrayList<SimpleCourse>();
			for(int period = 1; period < lesson[dayOfWeek].length; period++){
    			for(SimpleCourse course:lesson[dayOfWeek][period])
    				coursesInADay.add(course);
    		}
    		Bundle argForFragment = new Bundle();
    		argForFragment.putParcelableArrayList(ListCoursesFragment.COURSES_KEY, coursesInADay);
    		args[dayOfWeek] = argForFragment;
    	}
		//交换周日args[0]和时间未定args[7]，把周日显示在最后
		Bundle temp = args[0];
		args[0] = args[7];
		args[7] = temp;

		String[] daysOfWeek = getResources().getStringArray(R.array.days_of_week);
		
		for(int day = 0;day<=7;day++){
			TabSpec tabSpec = mTabHost.newTabSpec(daysOfWeek[day]);
			mTabsAdapter.addTab(tabSpec.setIndicator(daysOfWeek[day]),
					ListCoursesFragment.class, args[day]);
		}

		int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		mTabHost.setCurrentTab(dayOfWeek!=Calendar.SUNDAY ? dayOfWeek-Calendar.SUNDAY : 7);
	}

    private class RefreshCurrentSemester extends AsyncTask<Object,Void,Student>{
    	protected Student doInBackground(Object... args){
    		int currentWeek = 0;
    		StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(AllCoursesInNextSemesterActivity.this);
    		studentInfDBAdapter.open();
    		currentWeek = SettingsActivity.getCurrentWeekNumber(AllCoursesInNextSemesterActivity.this);
    		if(currentWeek < 5){
	        	studentInfDBAdapter.updateCurrentSemester();
	        }
    		studentInfDBAdapter.close();
    		return null;
    	}
    }
}
