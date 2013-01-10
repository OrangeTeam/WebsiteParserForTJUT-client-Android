package org.orange.querysystem.content;

import java.io.IOException;

import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Constant;
import util.webpage.SchoolWebpageParser;
import util.webpage.SchoolWebpageParser.ParserException;
import android.app.Activity;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

public class InsertDBFragmentActivity extends Activity{
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
			StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(InsertDBFragmentActivity.this);
			try {
				parser = new SchoolWebpageParser(new MyParserListener(), args[0], args[1]);
				studentInfDBAdapter.open();
				studentInfDBAdapter.autoInsertArrayCoursesInf(parser.parseScores(Constant.url.个人全部成绩),args[0]);
				studentInfDBAdapter.updateScoreInf(parser.parseScores(Constant.url.个人全部成绩));
				studentInfDBAdapter.updateScoreInf(parser.parseScores(Constant.url.期末最新成绩));
				studentInfDBAdapter.autoInsertArrayCoursesInf(parser.parseScores(Constant.url.本学期修读课程),args[0]);
				studentInfDBAdapter.autoInsertArrayCoursesInf(parser.parseScores(Constant.url.已选下学期课程),args[0]);
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
	

