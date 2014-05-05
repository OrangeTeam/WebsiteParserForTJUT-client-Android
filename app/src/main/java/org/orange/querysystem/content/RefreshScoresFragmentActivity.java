package org.orange.querysystem.content;

import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.querysystem.util.PersonalInformationUpdater;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import util.webpage.Course;
import util.webpage.SchoolWebpageParser;
import util.webpage.SchoolWebpageParser.ParserException;
import util.webpage.SchoolWebpageParser.ParserListener;

public class RefreshScoresFragmentActivity extends Activity {

    public static final int RESULT_NO_STUDENT_ID_OR_PASSWORD = RESULT_FIRST_USER + 1;

    public static final int RESULT_CANNOT_LOGIN = RESULT_FIRST_USER + 2;

    private static final int NUMBER_OF_ATTEMPTS = 3;

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

    private class UpdateCoursesListToDatabase extends AsyncTask<String, Void, Boolean> {

        public static final String TAG = "org.orange.querysystem";

        private static final String START_YEAR_SQL =
                "SELECT " + StudentInfDBAdapter.KEY_VALUE +
                        " FROM " + StudentInfDBAdapter.DATABASE_EAV_TABLE +
                        " WHERE " + StudentInfDBAdapter.KEY_ATTRIBUTE + " = '入学年级';";

        @Override
        protected Boolean doInBackground(String... args) {
            String userName = args[0], password = args[1];
            SchoolWebpageParser parser = null;
            StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(
                    RefreshScoresFragmentActivity.this);
            try {
                parser = new SchoolWebpageParser(new MyParserListener(), userName, password);
                List<Course> result = new LinkedList<Course>();
                studentInfDBAdapter.open();
                String yearString = getStartAcademicYear(studentInfDBAdapter.getDatabase(), parser);
                if (yearString == null) {
                    return false;
                }
                int startYear = Integer.parseInt(yearString);
                int currentYear = SettingsActivity
                        .getCurrentAcademicYear(RefreshScoresFragmentActivity.this);
                byte currentSemester = SettingsActivity
                        .getCurrentSemester(RefreshScoresFragmentActivity.this);
                int[][] semesters = new int[1 + currentYear - startYear][];
                for (int i = 0; i < semesters.length - 1; i++) {
                    semesters[i] = new int[3];
                    semesters[i][0] = startYear + i;
                    semesters[i][1] = 1;
                    semesters[i][2] = 2;
                }
                // current year
                semesters[semesters.length - 1] = new int[currentSemester + 1];
                semesters[semesters.length - 1][0] = currentYear;
                for (int i = 1; i <= currentSemester; i++) {
                    semesters[semesters.length - 1][i] = i;
                }
                for (Map<Integer, List<Course>> year : parser.parseScores(semesters).values()) {
                    for (List<Course> semester : year.values()) {
                        result.addAll(semester);
                    }
                }
                // save
                studentInfDBAdapter.autoInsertArrayCoursesInf(result, userName);
                studentInfDBAdapter.updateScoreInf(result);
                return true;
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            } catch (SQLiteException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                Log.e(TAG, "Cannot get start academic year from personal information", e);
            } catch (ParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                studentInfDBAdapter.close();
            }
            return false;
        }

        private String getStartAcademicYear(SQLiteDatabase database, SchoolWebpageParser parser)
                throws NumberFormatException {
            String result = getStartAcademicYear(database);
            if (result != null) {
                return result;
            }
            // 尝试刷新个人信息
            PersonalInformationUpdater updater = new PersonalInformationUpdater(
                    RefreshScoresFragmentActivity.this, parser);
            int attemptsCounter = NUMBER_OF_ATTEMPTS;
            while (result == null) {
                if (attemptsCounter-- == 0) {
                    break;
                }
                updater.update();
                result = getStartAcademicYear(database);
            }
            return result;
        }

        private String getStartAcademicYear(SQLiteDatabase database) {
            String queryResult = null;
            Cursor cursor = database.rawQuery(START_YEAR_SQL, null);
            if (cursor.moveToFirst()) {
                queryResult = cursor.getString(0);
            }
            cursor.close();
            return queryResult;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressBar.setVisibility(ProgressBar.GONE);
            if (result) {
                setResult(RESULT_OK);
            }
            finish();
        }

        class MyParserListener extends SchoolWebpageParser.ParserListenerAdapter {

            /* (non-Javadoc)
             * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onError(int, java.lang.String)
             */
            @Override
            public void onError(int code, String message) {
                Log.e(TAG, message);
                switch (code) {
                    case ParserListener.ERROR_CANNOT_LOGIN:
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
