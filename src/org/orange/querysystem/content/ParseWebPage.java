package org.orange.querysystem.content;

import java.io.IOException;
import java.util.List;

import util.webpage.Course;
import util.webpage.SchoolWebpageParser;
import util.webpage.SchoolWebpageParser.ParserException;
import android.os.AsyncTask;
import android.util.Log;
/**Please use ParseWebPage.execute(Interger targetContent, String url, CoursesInfo classHasCoursesInfo)*/
public class ParseWebPage extends AsyncTask<Object,Void,List<Course>>{
	interface CoursesInfo{
		void coursesInfo(List<Course> coursers);
	}
	
	public static final String TAG = "org.orange.querysystem";
	
	public static final int PARSE_COURSE = 1;
	public static final int PARSE_SCORE = 2;
	CoursesInfo coursesInfo = null;

	@Override
	protected List<Course> doInBackground(Object... args) {
		coursesInfo = (CoursesInfo) args[2];
		List<Course> result = null;
		SchoolWebpageParser parser = null;
		try {
			parser = new SchoolWebpageParser(
					new MyParserListener(), "20106135","20106135");
			switch((Integer)args[0]){
			case PARSE_COURSE: result = parser.parseCourse((String)args[1]);break;
			case PARSE_SCORE: result = parser.parseScores((String)args[1]);break;
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		} catch (ParserException e) {
			Log.e(TAG, "不能正确读取课程表表头时");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG,"IO异常：可能是网络连接出现异常");
			e.printStackTrace();
		}
		return result;
	}
	@Override
	protected void onPostExecute(List<Course> courses){
		coursesInfo.coursesInfo(courses);
	}
	
	private class MyParserListener extends SchoolWebpageParser.ParserListenerAdapter{

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