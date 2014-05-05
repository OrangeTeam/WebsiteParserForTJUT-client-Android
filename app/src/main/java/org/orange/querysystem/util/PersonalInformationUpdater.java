package org.orange.querysystem.util;

import org.orange.parser.parser.PersonalInformationParser;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import android.content.Context;

import java.io.IOException;
import java.util.Map;


/**
 * 个人信息更新器。
 */
public class PersonalInformationUpdater {

    protected final Context mContext;

    private final PersonalInformationParser mParser;

    public PersonalInformationUpdater(Context context, PersonalInformationParser parser) {
        mContext = context;
        mParser = parser;
    }

    /**
     * <strong>Note</strong>: 此方法涉及网络和数据库操作，不应在UI线程中调用
     */
    public long update() {
        Map<String, Map<String, String>> student = null;
        try {
            student = mParser.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (student == null || student.isEmpty()) {
            return 0L;
        }
        StudentInfDBAdapter dbAdapter = new StudentInfDBAdapter(mContext);
        dbAdapter.open();
        try {
            return dbAdapter.saveTwodimensionalMap(student,
                    StudentInfDBAdapter.ENTITY_PERSONAL_INFORMATION);
        } finally {
            dbAdapter.close();
        }
    }

}
