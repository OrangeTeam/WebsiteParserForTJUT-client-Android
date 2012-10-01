package org.orange.querysystem.content;

import java.io.IOException;

import org.orange.querysystem.R;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Constant;
import util.webpage.SchoolWebpageParser;
import util.webpage.SchoolWebpageParser.ParserException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class InsertDBFragment extends Fragment{
	private TextView refresh;
	private ProgressBar progressBar;
	private String userName = null;
	private String password = null;
	
	public InsertDBFragment(String userName, String password){
		this.userName = userName;
		this.password = password;
	}
	
	private static final int Course_Dialog = 1;
	
	
 	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancedState){
		View view = inflater.inflate(R.layout.refresh_fragment, container, false);
		refresh = (TextView) view.findViewById(R.id.refresh);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		
		return view;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
//		Student student = new Student();
		(new InsertDB()).execute(this.userName, this.password);
		
	}
	
	class InsertDB extends AsyncTask<String,String,Void>{
		
		public static final String TAG = "org.orange.querysystem";
		public static final int PARSE_COURSE = 1;
		public static final int PARSE_SCORE = 2;

		@Override
		protected Void doInBackground(String... args) {
			
			SchoolWebpageParser parser = null;
			StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(getActivity());
			try {
				parser = new SchoolWebpageParser(new MyParserListener(), args[0], args[1]);
				studentInfDBAdapter.open();
				studentInfDBAdapter.autoInsertArrayCoursesInf(parser.parseCourse(Constant.url.本学期修读课程));
				studentInfDBAdapter.autoInsertArrayCoursesInf(parser.parseScores(Constant.url.个人全部成绩));
//				studentInfDBAdapter.insertScoreInf(parser.parseScores(Constant.url.个人全部成绩));
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
			System.out.println("刷新成功");
			progressBar.setVisibility( ProgressBar.GONE);
			refresh.setText("数据库刷新成功！");
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
