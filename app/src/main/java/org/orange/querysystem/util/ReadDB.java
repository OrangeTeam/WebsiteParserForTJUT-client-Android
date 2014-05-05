package org.orange.querysystem.util;

import org.orange.parser.entity.Course;
import org.orange.querysystem.SettingsActivity;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import android.content.Context;
import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;


public class ReadDB extends AsyncTask<String, Void, List<Course>> {

    public interface OnPostExcuteListerner {

        public void onPostReadFromDB(List<Course> courses);
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
    protected List<Course> doInBackground(String... args) {
        int year = SettingsActivity.getCurrentAcademicYear(context);
        byte semester = SettingsActivity.getCurrentSemester(context);
        if (!args[1].equals("this")) {
            // 推断下学期的学年和学期号
            if (semester == 1) {
                semester++;
            } else if (semester == 2) {
                year++;
                semester = 1;
            } //TODO 其他情况怎么办？
        }
        List<Course> result = null;
        studentInfDBAdapter = new StudentInfDBAdapter(context);
        try {
            // 查询
            studentInfDBAdapter.open();
            result = studentInfDBAdapter.getCoursesFromDB(
                    StudentInfDBAdapter.KEY_YEAR + "= ?  AND " + StudentInfDBAdapter.KEY_SEMESTER
                            + " = ?",
                    new String[]{String.valueOf(year), String.valueOf(semester)}, null, args[0]
            );
        } catch (SQLException e) {
            Log.e("TAG", "Cannot read courses from database", e);
        } finally {
            studentInfDBAdapter.close();
        }

        return result;
    }

    @Override
    protected void onCancelled() {
        studentInfDBAdapter.close();
    }

    @Override
    protected void onPostExecute(List<Course> courses) {
        if (listener != null) {
            listener.onPostReadFromDB(courses);
        }
    }
}
