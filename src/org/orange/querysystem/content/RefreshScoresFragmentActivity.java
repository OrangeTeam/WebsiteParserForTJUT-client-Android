package org.orange.querysystem.content;

import java.io.IOException;
import java.util.List;

import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Constant;
import util.webpage.Course;
import util.webpage.SchoolWebpageParser;
import util.webpage.SchoolWebpageParser.ParserException;
import util.webpage.SchoolWebpageParser.ParserListener;
import android.app.Activity;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RefreshScoresFragmentActivity extends Activity{
	public static final int RESULT_NO_STUDENT_ID_OR_PASSWORD = RESULT_FIRST_USER + 1;
	public static final int RESULT_CANNOT_LOGIN = RESULT_FIRST_USER + 2;

	private String userName = null;
	private String password = null;
	private TextView refresh;
	private ProgressBar progressBar;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refresh_fragment);
        refresh = (TextView)findViewById(R.id.refresh);
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
			InsertDBFragmentActivity.logIn_error = true;
			finish();
		}
	}
	
	private class UpdateCoursesListToDatabase extends AsyncTask<String, Void, Void>{
		public static final String TAG = "org.orange.querysystem";
		
		@Override
		protected Void doInBackground(String... args){
			SchoolWebpageParser parser = null;
			StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(RefreshScoresFragmentActivity.this);
			try {
				parser = new SchoolWebpageParser(new MyParserListener(), args[0], args[1]);
				studentInfDBAdapter.open();
				List<Course> result = null;
				result = parser.parseScores(Constant.url.个人全部成绩);
				studentInfDBAdapter.autoInsertArrayCoursesInf(result, args[0]);
				studentInfDBAdapter.updateScoreInf(result);
				result = parser.parseScores(Constant.url.期末最新成绩);
				studentInfDBAdapter.autoInsertArrayCoursesInf(result, args[0]);
				studentInfDBAdapter.updateScoreInf(result);
				setResult(RESULT_OK);
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(SQLiteException e){
				e.printStackTrace();
			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			studentInfDBAdapter.close();
			return null;
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
					InsertDBFragmentActivity.logIn_error = true;
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
	

