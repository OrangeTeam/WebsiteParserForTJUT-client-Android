package org.orange.querysystem.content;

import java.util.ArrayList;

import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.BitOperate.BitOperateException;
import util.webpage.Course;
import util.webpage.Course.CourseException;
import util.webpage.Course.TimeAndAddress.TimeAndAddressException;
import util.webpage.SchoolWebpageParser;
import android.content.Context;
import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class ReadDB extends AsyncTask<String,Void,ArrayList<Course>>{
	protected interface OnPostExcuteListerner{
		public void onPostReadFromDB(ArrayList<Course> courses);
	}
	private Context context;
	private OnPostExcuteListerner listener;
	StudentInfDBAdapter studentInfDBAdapter = null;
	public static final String TAG = "org.orange.querysystem";

	public ReadDB(Context context, OnPostExcuteListerner listener) {
		super();
		this.context = context;
		this.listener = listener;
	}

	@Override
	protected ArrayList<Course> doInBackground(String... args) {
		ArrayList<Course> result = null;
		studentInfDBAdapter = new StudentInfDBAdapter(context);
		try {
			studentInfDBAdapter.open();
//			System.out.println(args[0]);
			result = studentInfDBAdapter.getCoursesFromDB(StudentInfDBAdapter.KEY_YEAR + "=" + 0, null, args[0]);
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
	protected void onPostExecute(ArrayList<Course> courses){
		if(courses != null)
			listener.onPostReadFromDB(courses);	
		else{
			Toast.makeText(context, "no data in the database!", Toast.LENGTH_LONG).show();			
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
	
	

