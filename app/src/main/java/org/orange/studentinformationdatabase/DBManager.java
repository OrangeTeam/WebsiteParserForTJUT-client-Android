package org.orange.studentinformationdatabase;

import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DBManager {

    public static void openHelper(Context context) throws IOException {
        String dbDirPath = "/data/data/org.orange.querysystem/databases";
        File dbDir = new File(dbDirPath);
        if (!dbDir.exists()) {
            dbDir.mkdir();
        }
        InputStream is = context.getResources().openRawResource(R.raw.studentinf);
        FileOutputStream os = new FileOutputStream(dbDirPath + "/studentInf.db");
        byte[] buffer = new byte[1024];
        int count = 0;
        while ((count = is.read(buffer)) > 0) {
            os.write(buffer, 0, count);
        }
        is.close();
        os.close();
    }

    /**
     * 当{@code SettingsActivity.getAccountStudentID(context) == nul}时，导入静态数据库
     */
    public static void importInitialDB(Context context) {
        if (SettingsActivity.getAccountStudentID(context) == null) {
            try {
                openHelper(context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
