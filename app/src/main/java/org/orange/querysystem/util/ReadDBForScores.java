package org.orange.querysystem.util;

import java.util.List;
import java.util.Map;

import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Course;
import android.content.Context;
import android.database.SQLException;
import android.os.AsyncTask;

public class ReadDBForScores extends AsyncTask<String, Void, Map<Integer, Map<Integer, List<Course>>>> {
	public interface OnPostExcuteListerner{
		public void onPostReadFromDBForScores(Map<Integer, Map<Integer, List<Course>>> courses);
	}
	private Context context;
	private OnPostExcuteListerner listener;
	StudentInfDBAdapter studentInfDBAdapter = null;
	public static final String TAG = "org.orange.querysystem";

	public ReadDBForScores(Context context, OnPostExcuteListerner listener) {
		super();
		this.context = context;
		this.listener = listener;
	}

	@Override
	protected Map<Integer, Map<Integer, List<Course>>> doInBackground(String... args) {
		Map<Integer, Map<Integer, List<Course>>> result = null;
		studentInfDBAdapter = new StudentInfDBAdapter(context);
		try {
			studentInfDBAdapter.open();
			result = studentInfDBAdapter.getAllCoursesFromDB(args[0]);
		} catch(SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			studentInfDBAdapter.close();
		}
		
		return result;
	}

	@Override
	protected void onCancelled() {
		studentInfDBAdapter.close();
	}

	@Override
	protected void onPostExecute(Map<Integer, Map<Integer, List<Course>>> courses) {
		if(listener != null)
			listener.onPostReadFromDBForScores(courses);
	}
}
