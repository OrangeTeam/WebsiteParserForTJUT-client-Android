package org.orange.querysystem.content;

import java.io.IOException;

import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Constant;
import util.webpage.SchoolWebpageParser;
import util.webpage.SchoolWebpageParser.ParserException;
import util.webpage.SchoolWebpageParser.ParserListener;
import android.app.Activity;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

public class InsertDBFragmentActivity extends Activity{
	public static final int RESULT_NO_STUDENT_ID_OR_PASSWORD = RESULT_FIRST_USER + 1;
	public static final int RESULT_CANNOT_LOGIN = RESULT_FIRST_USER + 2;

	private String userName = null;
	private String password = null;
	private ProgressBar progressBar;
	public static final int LOG_IN_ERROR_DIALOG_ID = 2;
	public static boolean logIn_error = false;
//	private org.orange.querysystem.content.InsertDBFragmentActivity.UpdateCoursesListToDatabase.MyParserListener myParserListener;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refresh_fragment);
        
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
        loadCourses();
    }
	
	public void loadCourses(){
        userName = SettingsActivity.getAccountStudentID(this);
        password = SettingsActivity.getAccountPassword(this);
		if(userName != null && password != null)
			new UpdateCoursesListToDatabase().execute(userName, password);
		else{
			setResult(RESULT_NO_STUDENT_ID_OR_PASSWORD);
			logIn_error = true;
			finish();
		}
	}

	private class UpdateCoursesListToDatabase extends AsyncTask<String, Void, Void>{
		public static final String TAG = "org.orange.querysystem";
		MyParserListener myParserListener;
		SchoolWebpageParser parser = null;
		
		@Override
		protected Void doInBackground(String... args){
			StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(InsertDBFragmentActivity.this);
			try {
				parser.setUser(args[0], args[1]);
				studentInfDBAdapter.open();
				studentInfDBAdapter.autoInsertArrayCoursesInf(parser.parseCourse(Constant.url.本学期修读课程),args[0]);
				studentInfDBAdapter.autoInsertArrayCoursesInf(parser.parseCourse(Constant.url.已选下学期课程),args[0]);
				setResult(RESULT_OK);
			} catch(SQLiteException e){
				e.printStackTrace();
			} catch (ParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			studentInfDBAdapter.close();
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			myParserListener = new MyParserListener();
			try {
				parser = new SchoolWebpageParser(myParserListener);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}



		@Override
		protected void onPostExecute(Void course){
			progressBar.setVisibility( ProgressBar.GONE);
			finish();
		}
		
		class MyParserListener extends SchoolWebpageParser.ParserListenerAdapter{

			/* (non-Javadoc)
			 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onError(int, java.lang.String)
			 */
			@Override
			public void onError(int code, String message) {
				Log.e(TAG, message);
				switch (code) {
				case ParserListener.ERROR_CANNOT_LOGIN:
					setResult(RESULT_CANNOT_LOGIN);
					logIn_error = true;
					break;
				default:
					break;
				}
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
	

