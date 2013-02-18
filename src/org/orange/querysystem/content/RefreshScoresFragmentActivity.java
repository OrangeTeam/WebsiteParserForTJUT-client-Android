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
		new UpdateCoursesListToDatabase().execute(userName, password);
	}
	
	private class UpdateCoursesListToDatabase extends AsyncTask<String, Void, Void>{
		public static final String TAG = "org.orange.querysystem";
		
		@Override
		protected Void doInBackground(String... args){
			SchoolWebpageParser parser = null;
			StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(RefreshScoresFragmentActivity.this);
 			try {
 				List<Course> result1 = parser.parseScores(Constant.url.个人全部成绩);
 				List<Course> result2 = parser.parseScores(Constant.url.期末最新成绩);
				parser = new SchoolWebpageParser(new MyParserListener(), args[0], args[1]);
				studentInfDBAdapter.open();
				studentInfDBAdapter.autoInsertArrayCoursesInf(result1,args[0]);
				studentInfDBAdapter.updateScoreInf(result1);
				studentInfDBAdapter.updateScoreInf(result2);
				studentInfDBAdapter.autoInsertArrayCoursesInf(result2,args[0]);
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
	

