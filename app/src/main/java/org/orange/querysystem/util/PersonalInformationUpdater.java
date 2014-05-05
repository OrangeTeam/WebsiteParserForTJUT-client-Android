package org.orange.querysystem.util;

import java.io.IOException;
import java.util.Map;

import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.SchoolWebpageParser;
import util.webpage.SchoolWebpageParser.ParserException;
import android.content.Context;

/**
 * 个人信息更新器。
 */
public class PersonalInformationUpdater {
	protected final Context mContext;
	private final SchoolWebpageParser mParser;

	public PersonalInformationUpdater(Context context,SchoolWebpageParser parser ) {
		mContext = context;
		mParser = parser;
	}

	/**
	 * <strong>Note</strong>: 此方法涉及网络和数据库操作，不应在UI线程中调用
	 */
	public long update() {
		Map<String, Map<String, String>> student = null;
		try {
			student = mParser.parsePersonalInformation();
		} catch (ParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(student == null || student.isEmpty())
			return 0L;
		StudentInfDBAdapter dbAdapter = new StudentInfDBAdapter(mContext);
		dbAdapter.open();
		try {
			return dbAdapter.saveTwodimensionalMap(student, StudentInfDBAdapter.ENTITY_PERSONAL_INFORMATION);
		} finally {
			dbAdapter.close();
		}
	}

}
