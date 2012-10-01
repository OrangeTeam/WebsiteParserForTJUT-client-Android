package org.orange.querysystem;

import java.util.ArrayList;

import org.orange.querysystem.content.ListCoursesActivity;
import org.orange.querysystem.content.ListCoursesFragment;
import org.orange.querysystem.content.ListCoursesFragment.SimpleCourse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class TestListCoursesActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<Bundle> args = new ArrayList<Bundle>(7);
        for(int day = 0;day<=6;day++){
        	ArrayList<SimpleCourse> courses = new ArrayList<SimpleCourse>();
        	for(int counter = 0;counter<=day;counter++)
        		courses.add(new SimpleCourse(day+counter, "课程名称"+day+" "+counter, 
        				counter*2-1+"、"+counter*2, day+" "+counter));
        	Bundle arg = new Bundle();
        	arg.putParcelableArrayList(ListCoursesFragment.COURSES_KEY, courses);
        	args.add(arg);
        }
        Intent intent = new Intent(this, ListCoursesActivity.class);
        intent.putExtra(ListCoursesActivity.ARRAYLIST_OF_COURSES_KEY, args);
        startActivityForResult(intent,0);
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		finish();
	}

    
}
