package org.orange.querysystem.content;

import org.orange.parser.connection.SSFWWebsiteConnectionAgent;
import org.orange.parser.parser.ParseAdapter;
import org.orange.parser.parser.ParseListener;
import org.orange.parser.parser.SelectedCourseParser;
import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import android.app.Activity;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.IOException;

public class InsertDBFragmentActivity extends Activity {

    public static final int RESULT_NO_STUDENT_ID_OR_PASSWORD = RESULT_FIRST_USER + 1;

    public static final int RESULT_CANNOT_LOGIN = RESULT_FIRST_USER + 2;

    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refresh_fragment);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        loadCourses();
    }

    public void loadCourses() {
        String userName = SettingsActivity.getAccountStudentID(this);
        String password = SettingsActivity.getAccountPassword(this);
        if (userName != null && password != null) {
            new UpdateCoursesListToDatabase().execute(userName, password);
        } else {
            setResult(RESULT_NO_STUDENT_ID_OR_PASSWORD);
            finish();
        }
    }

    private class UpdateCoursesListToDatabase extends AsyncTask<String, Void, Void> {

        public static final String TAG = "org.orange.querysystem";

        @Override
        protected Void doInBackground(String... args) {
            final String username = args[0], password = args[1];
            SelectedCourseParser parser = new SelectedCourseParser();
            parser.setParseListener(new MyParserListener());
            parser.setConnectionAgent(
                    new SSFWWebsiteConnectionAgent().setAccount(username, password));
            StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(
                    InsertDBFragmentActivity.this);
            try {
                studentInfDBAdapter.open();
                studentInfDBAdapter.autoInsertArrayCoursesInf(parser.parse(), args[0]);
                setResult(RESULT_OK);
            } catch (SQLiteException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            studentInfDBAdapter.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void course) {
            progressBar.setVisibility(ProgressBar.GONE);
            finish();
        }

        class MyParserListener extends ParseAdapter {

            /* (non-Javadoc)
             * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onError(int, java.lang.String)
             */
            @Override
            public void onError(int code, String message) {
                Log.e(TAG, message);
                switch (code) {
                    case ParseListener.ERROR_CANNOT_LOGIN:
                        setResult(RESULT_CANNOT_LOGIN);
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
