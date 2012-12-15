package org.orange.querysystem.content;

import java.util.ArrayList;

import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Course;
import util.webpage.Course.CourseException;
import util.webpage.SchoolWebpageParser;
import android.content.Context;
import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class ReadDBForScores extends AsyncTask<String,Void,ArrayList<ArrayList<Course>>>{
	protected interface OnPostExcuteListerner{
		public void onPostReadFromDBForScores(ArrayList<ArrayList<Course>> courses);
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
	protected ArrayList<ArrayList<Course>> doInBackground(String... args) {
		ArrayList<ArrayList<Course>> result = null;
		studentInfDBAdapter = new StudentInfDBAdapter(context);
		try {
			studentInfDBAdapter.open();
//			for(int i=0; i<studentInfDBAdapter.getAllCoursesFromDB(args[0]).size(); i++){
//				result.add(studentInfDBAdapter.getAllCoursesFromDB(args[0]).get(0));
//			}
			result = studentInfDBAdapter.getAllCoursesFromDB(args[0]);
		} catch(SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CourseException e) {
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
	protected void onPostExecute(ArrayList<ArrayList<Course>> courses){
		if(courses != null)
			listener.onPostReadFromDBForScores(courses);	
		else{
			Toast.makeText(context, "数据库无数据，请刷新！", Toast.LENGTH_LONG).show();			
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
	
	

