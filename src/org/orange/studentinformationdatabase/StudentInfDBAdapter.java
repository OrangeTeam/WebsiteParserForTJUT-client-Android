package org.orange.studentinformationdatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import util.BitOperate.BitOperateException;
import util.webpage.Course;
import util.webpage.Course.CourseException;
import util.webpage.Course.TimeAndAddress;
import util.webpage.Post;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 数据库名为：studentInf.db，有三张表，其中courseInf1和courseInf2是相关连的表，存储课程信息及成绩信息。表post存储通知信息。
 * @author Zhou Peican
 *
 */
public class StudentInfDBAdapter {
	private static final String DATABASE_NAME = "studentInf.db";
	private static final String DATABASE_COURSE_TABLE1 = "courseInf1";
	private static final String DATABASE_COURSE_TABLE2 = "courseInf2";
	static final String DATABASE_POST_TABLE = "post";
	private static final int DATABASE_VERSION = 1;
	
	private SQLiteDatabase db;
	private final Context context;
	private StudentInfDBOpenHelper dbHelper;
	
	/**
	 * 构造方法 建立数据库
	 * @param theContext 系统参数 “上下文”
	 */
	public StudentInfDBAdapter(Context theContext){
		context = theContext;
		dbHelper = new StudentInfDBOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public static final String KEY_ID = "_id";
	public static final String KEY_CODE = "code";
	public static final String KEY_NAME = "name";
	public static final String KEY_TEACHERS = "teacrhers";
	public static final String KEY_CREDIT = "credit";
	public static final String KEY_CLASS_NUMBER = "class_number";
	public static final String KEY_TEACHING_MATERIAL = "teachingmaterial";
	public static final String KEY_YEAR = "year";
	public static final String KEY_ISFIRSTSEMESTER = "is_first_semester";
	public static final String KEY_TEST_SCORE = "test_score";
	public static final String KEY_TOTAL_SCORE = "total_score";
	public static final String KEY_KIND = "kind";
	public static final String KEY_NOTE = "note";
	public static final String KEY_CURRENT_SEMESTER = "current_semester";
	public static final String KEY_USER_NAME = "user_name";
	
	
	public static final String KEY_LINK = "link";
	public static final String KEY_VICEID = "viceid";
	public static final String KEY_WEEK ="week";
	public static final String KEY_DAY = "day";
	public static final String KEY_PERIOD = "period";
	public static final String KEY_ADDRESS ="address";
	
	
	public static final String KEY_POST_ID = Contract.Posts._ID;
	public static final String KEY_SOURCE = "source";
	public static final String KEY_CATEGORY = "category";
	public static final String KEY_TITLE = "title";
	public static final String KEY_URL = "url";
	public static final String KEY_AUTHOR = "author";
	public static final String KEY_DATE = "date";
	public static final String KEY_MAINBODY = "mainbody";
	
	
	/*
	 *  内部类，构建数据库用的，生成courseInf1、courseInf2和post表。
	 */
	static class StudentInfDBOpenHelper extends SQLiteOpenHelper{
		public StudentInfDBOpenHelper(Context context, String name, CursorFactory factory, int version){
			super(context, name, factory,version);
		}
		public StudentInfDBOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
	
	    private static final String COURSE_TABLE1_CREATE = "create table " + DATABASE_COURSE_TABLE1 + "(" + KEY_ID + " integer primary key,"
	    + KEY_CODE + " character(7) unique," + KEY_NAME + " varchar(25)," + KEY_TEACHERS + " varchar(25)," + KEY_CREDIT + " tinyint," + KEY_CLASS_NUMBER + " varchar(5),"
	    + KEY_TEACHING_MATERIAL + " varchar(15)," + KEY_YEAR + " integer," + KEY_ISFIRSTSEMESTER + " varchar(1)," + KEY_TEST_SCORE + " float,"
	    + KEY_TOTAL_SCORE + " float," + KEY_KIND + " varchar(5)," + KEY_NOTE + " varchar(30)," + KEY_CURRENT_SEMESTER + " integer," + KEY_USER_NAME + " varchar(8));";
	    
	    private static final String COURSE_TABLE2_CREATE = "create table " + DATABASE_COURSE_TABLE2 + "(" + KEY_LINK + " integer," + KEY_VICEID + " varchar(5) unique,"
	    + KEY_WEEK + " integer," + KEY_DAY + " integer," + KEY_PERIOD + " integer," + KEY_ADDRESS + " varchar(5));";
	    
	    private static final String POST_TABLE_CREATE = "create table " + DATABASE_POST_TABLE + "(" + KEY_POST_ID + " integer primary key," 
	    + KEY_SOURCE + " integer," + KEY_CATEGORY + " varchar(35)," + KEY_TITLE + " varchar(35)," + KEY_URL + " varchar(60)," + KEY_AUTHOR + " varchar(15),"
	    + KEY_DATE + " integer," + KEY_MAINBODY + " text);"; 
	
	    public void onCreate(SQLiteDatabase theDB){
		    theDB.execSQL(COURSE_TABLE1_CREATE);
		    theDB.execSQL(COURSE_TABLE2_CREATE);
		    theDB.execSQL(POST_TABLE_CREATE);
		    theDB.execSQL("CREATE INDEX post_index ON " + DATABASE_POST_TABLE + "(" + KEY_DATE + ");");
	    }
	    
	    public void onUpgrade(SQLiteDatabase theDB, int theOldVersion, int theNewVersion){
	    	Log.w("StudentInfDBAdapter", "Upgrading from version " + theOldVersion + " to " + theNewVersion + ", which will destroy all data");
	    	
	    	theDB.execSQL("DROP TABLE IF EXISTS " + DATABASE_COURSE_TABLE1);
	    	theDB.execSQL("DROP TABLE IF EXISTS " + DATABASE_COURSE_TABLE2);
	    	theDB.execSQL("DROP TABLE IF EXISTS " + DATABASE_POST_TABLE);
	    	
	    	onCreate(theDB);
	    }
	}
	
	/**
	 * getWritableDatabase()创建一个可读写的数据库。
	 * @throws SQLiteException
	 */
	public void open() throws SQLiteException{
		try{
			db = dbHelper.getWritableDatabase();
		}catch(SQLiteException ex){
			db = dbHelper.getReadableDatabase();
		}
	}
	/**
	 * Returns true if the database is currently open.
	 * @return True if the database is currently open (has not been closed).
	 */
	public boolean isOpen(){
		return (db!=null && db.isOpen());
	}
	public void close(){
		if(isOpen())
			db.close();
	}
	
	/*在数据库中isFirstSemester存储形式为字符串，而Course类中的isFirstSemester存储形式为Boolean对象。
	 * 所以isFirstSemester存储到数据库中要进行转换，convert方法是把布尔对象转换为字符串。
	 * 当isFirstSemester为false时转换为字符“f”，true时为字符“t”。
	 */
	private String convert(Boolean temp){
		if((temp == null)||temp)
		{
			return "t";
		}else{
			return "f";
		}
	}
	
	//当然isFirstSemester从数据库中提取也要进行转换，reconvert方法是把字符串转换为布尔对象。
	private Boolean reconvert(String temp){
		if(temp == "t")
		{
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 一次性插入多门课程及每门课程成绩的初始化（成绩与课程在同一张表中），之所以成绩要初始化是因为读取课程时并没有读取成绩，
	 * @param theCourseInf 类型为 List<Course>
	 */
	private void insertArrayCoursesToCourseInf1(List<Course> theCourseInf, String theUserName){
		Cursor cursor, cursor1;
		ContentValues newCourseInfValues = new ContentValues();
		
		cursor = db.query(DATABASE_COURSE_TABLE1, null, KEY_YEAR + "=" + 0, null, null, null, null);
		
		for(Course aCourse:theCourseInf){
			newCourseInfValues.put(KEY_CODE, aCourse.getCode());
			newCourseInfValues.put(KEY_NAME, aCourse.getName());
			newCourseInfValues.put(KEY_TEACHERS, aCourse.getTeacherString());
			newCourseInfValues.put(KEY_CREDIT, aCourse.getCredit());
			newCourseInfValues.put(KEY_CLASS_NUMBER, aCourse.getClassNumber());
			newCourseInfValues.put(KEY_TEACHING_MATERIAL, aCourse.getTeachingMaterial());
			newCourseInfValues.put(KEY_YEAR, aCourse.getYear());
			newCourseInfValues.put(KEY_ISFIRSTSEMESTER, convert(aCourse.isFirstSemester()));
			newCourseInfValues.put(KEY_TEST_SCORE, aCourse.getTestScore());
			newCourseInfValues.put(KEY_TOTAL_SCORE, aCourse.getTotalScore());
			newCourseInfValues.put(KEY_KIND, aCourse.getKind());
			newCourseInfValues.put(KEY_NOTE, aCourse.getNote());
			if(cursor.getCount() == 0){
				newCourseInfValues.put(KEY_CURRENT_SEMESTER, 1);
			}else{
				newCourseInfValues.put(KEY_CURRENT_SEMESTER, 0);
			}
			newCourseInfValues.put(KEY_USER_NAME, theUserName);
			//theCourseInf为ArrayList对象，get(i)顺序找到其中的一门课程。getCode()等方法得到相应实例变量的值。
			
			cursor1 = db.query(DATABASE_COURSE_TABLE1, null, KEY_CODE + "= '" + aCourse.getCode() + "'", null, null, null, null);
			if(cursor1.getCount() == 0){
				db.insert(DATABASE_COURSE_TABLE1,null, newCourseInfValues);
			}else{
				newCourseInfValues.clear();
			}
		}
		
	}
	
	/**
	 * 课程的时间地点是另一张表，这是对时间地点的插入操作。courseInf2为时间地点的存储与courseInf1相关联。
	 * @param theCourseInf  List<Course>类型
	 */
	private void insertArrayCoursesToCourseInf2(List<Course> theCourseInf){
		ContentValues newCourseInfTAValues = new ContentValues();
		int counter = 0;
		for(Course aCourse:theCourseInf){
			Cursor cursor = db.query(DATABASE_COURSE_TABLE1, null, KEY_CODE + "= '" + aCourse.getCode() + "'", null, null, null, null);
			cursor.moveToFirst();
			//这里对courseInf1表的查询是为了获得cuorseInf1表的id字段值，用来存储到courseInf2中的link字段。
			counter = 0;
			for(TimeAndAddress aTimeAndAddress:aCourse.getTimeAndAddress()){
				newCourseInfTAValues.put(KEY_LINK, cursor.getInt(0));
				//link列是courseInf1和courseInf2相‘连接’的字段以执行相应的操作，所以link列须和_id列值相等。cursor.getInt(0)是得到id字段值。
				newCourseInfTAValues.put(KEY_VICEID, Integer.toString(cursor.getInt(0)) + Integer.toString(counter++));
				//一门课有多个TimeAndAddress与之相对应，viceid列是为了对其中一个TimeAndAddress进行操作。例如当link列为1时viceid就为11、12、、、等
				//当然link的1是整数型的而viceid的11、12为字符串类型的。
				newCourseInfTAValues.put(KEY_WEEK, aTimeAndAddress.getWeek());
				newCourseInfTAValues.put(KEY_DAY, aTimeAndAddress.getDay());
				newCourseInfTAValues.put(KEY_PERIOD, aTimeAndAddress.getPeriod());
				newCourseInfTAValues.put(KEY_ADDRESS, aTimeAndAddress.getAddress());
				
				db.insert(DATABASE_COURSE_TABLE2,null,newCourseInfTAValues);
			}
		}
	}
	
	/**
	 * 先判断数据库课程的记录是否为当前用户的，如果不是就清空课程的记录，建立当前用户的课程信息记录。是当前用户的就继续操作。
	 *  判断数据库课程表中是否已经有要查入的课程，如果已经有就不会再次插入，当然没有时就会调用insertArrayCoursesToCourseInf1方法和insertArrayCoursesToCourseInf2。
	 * @param theCourseInf List<Course>类型
	 * @param theUserName String类型
	 */
	public void autoInsertArrayCoursesInf(List<Course> theCourseInf, String theUserName){
		if(theCourseInf==null || theCourseInf.isEmpty()) return;
		Cursor cursor1 = db.query(DATABASE_COURSE_TABLE1, null, KEY_USER_NAME + "= '" + theUserName + "'", null, null, null, null);
		if(cursor1.getCount() != 0)
		{
			//String code = theCourseInf.get(0).getCode();
			//Cursor cursor2 = db.query(DATABASE_COURSE_TABLE1, null, KEY_CODE + "= '" + code + "'", null, null, null, null);
			//if(cursor2.getCount() == 0)
			//{
				insertArrayCoursesToCourseInf1(theCourseInf, theUserName);
				insertArrayCoursesToCourseInf2(theCourseInf);
			//}
		}else{
			db.delete(DATABASE_COURSE_TABLE1, null, null);
			db.delete(DATABASE_COURSE_TABLE2, null, null);
			
			String code = theCourseInf.get(0).getCode();
			Cursor cursor2 = db.query(DATABASE_COURSE_TABLE1, null, KEY_CODE + "= '" + code + "'", null, null, null, null);
			if(cursor2.getCount() == 0)
			{
				insertArrayCoursesToCourseInf1(theCourseInf, theUserName);
				insertArrayCoursesToCourseInf2(theCourseInf);
			}
		}
	}
	
	/**
	 * 这个课程插入的方法是一次插入一门课程及相关成绩的初始化，
	 * @param theCourseInf 类型为Course类型
	 */
	private void insertCourseToCourseInf1(Course theCourseInf, String theUserName){
		ContentValues newCourseInfValues = new ContentValues();
		
		newCourseInfValues.put(KEY_CODE, theCourseInf.getCode());
		newCourseInfValues.put(KEY_NAME, theCourseInf.getName());
		newCourseInfValues.put(KEY_TEACHERS, theCourseInf.getTeacherString());
		newCourseInfValues.put(KEY_CREDIT, theCourseInf.getCredit());
		newCourseInfValues.put(KEY_CLASS_NUMBER, theCourseInf.getClassNumber());
		newCourseInfValues.put(KEY_TEACHING_MATERIAL, theCourseInf.getTeachingMaterial());
		newCourseInfValues.put(KEY_YEAR, theCourseInf.getYear());
		newCourseInfValues.put(KEY_ISFIRSTSEMESTER, convert(theCourseInf.isFirstSemester()));
		newCourseInfValues.put(KEY_TEST_SCORE, theCourseInf.getTestScore());
		newCourseInfValues.put(KEY_TOTAL_SCORE, theCourseInf.getTotalScore());
		newCourseInfValues.put(KEY_KIND, theCourseInf.getKind());
		newCourseInfValues.put(KEY_NOTE, theCourseInf.getNote());
		newCourseInfValues.put(KEY_CURRENT_SEMESTER, 0);
		newCourseInfValues.put(KEY_USER_NAME, theUserName);
		
		theCourseInf.setId((int)db.insert(DATABASE_COURSE_TABLE1, null, newCourseInfValues));
	}
	
	/**
	 * 对一门课程的时间和地点的插入操作。
	 * @param theCourseInf
	 */
	private void insertCourseToCourseInf2(Course theCourseInf){
		ContentValues newCourseInfTAValues = new ContentValues();
		Cursor cursor = db.query(DATABASE_COURSE_TABLE1, null, KEY_ID + "=" + theCourseInf.getId(), null, null, null, null);
		
		System.err.println("count: "+cursor.getCount()+"\nmovetofirst"+cursor.moveToFirst());
		
		
		for(int i=0; i < theCourseInf.getTimeAndAddress().size(); i++){
			newCourseInfTAValues.put(KEY_LINK, cursor.getInt(0));
			newCourseInfTAValues.put(KEY_VICEID, Integer.toString(cursor.getInt(0)) + Integer.toString(i));
			newCourseInfTAValues.put(KEY_WEEK, theCourseInf.getTimeAndAddress().get(i).getWeek());
			newCourseInfTAValues.put(KEY_DAY, theCourseInf.getTimeAndAddress().get(i).getDay());
			newCourseInfTAValues.put(KEY_PERIOD, theCourseInf.getTimeAndAddress().get(i).getPeriod());
			newCourseInfTAValues.put(KEY_ADDRESS, theCourseInf.getTimeAndAddress().get(i).getAddress());
			
			db.insert(DATABASE_COURSE_TABLE2,null,newCourseInfTAValues);
		}
	}
	
	/**
	 *  判断数据库课程表中是否已经有要查入的课程，如果已经有就不会再次插入，当然如果 没有就会掉用insertCourseToCourseInf1方法和insertCourseToCourseInf2。
	 *  insertCourseInf:这个课程插入的方法是一次插入一门课程及相关成绩的初始化，
	 * @param theCourseInf Course类型
	 * @param theUserName  String类型
	 */
	public void autoInsertCourseInf(Course theCourseInf, String theUserName){
		if(theCourseInf == null) return;
		String code = theCourseInf.getCode();
		Cursor cursor = db.query(DATABASE_COURSE_TABLE1, null, KEY_CODE + "= '" + code + "'", null, null, null, null);
		if(cursor.getCount() == 0)
		{
			insertCourseToCourseInf1(theCourseInf, theUserName);
			insertCourseToCourseInf2(theCourseInf);
		}
	}
	
	/**
	 * 解析出的通知通过此插入操作方法存入到数据库的post表中。
	 * @param thePostInf 类型为List<Post>类型
	 */
	private int insertArrayPostsInf(List<Post> thePostInf){
		int count = 0;
		long insertResult;
		ContentValues newPostInfValues = new ContentValues();
		
		for(Post aPost:thePostInf){
			if(aPost.getId() != null)
				newPostInfValues.put(KEY_POST_ID, aPost.getId());
			newPostInfValues.put(KEY_SOURCE, aPost.getSource());
			newPostInfValues.put(KEY_CATEGORY, aPost.getCategory());
			newPostInfValues.put(KEY_TITLE, aPost.getTitle());
			newPostInfValues.put(KEY_URL, aPost.getUrl());
			newPostInfValues.put(KEY_AUTHOR, aPost.getAuthor());
			newPostInfValues.put(KEY_DATE, aPost.getDate().getTime());
			newPostInfValues.put(KEY_MAINBODY, aPost.getMainBody());
			//aPost.getDate().getTime()获取date字段并进行转换为长整型数据。
			
			insertResult = db.insert(DATABASE_POST_TABLE,null, newPostInfValues);
			if(insertResult > -1){
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 判断数据库通知表中是否已经有要插入的通知，如果有就不进行插入操作，如果没有就调用insertArrayPostsInf
	 * insertArrayPostsInf:解析出的通知通过此插入操作方法存入到数据库的post表中。
	 * @param thePostInf
	 */
	public int autoInsertArrayPostsInf(List<Post> thePostInf){
		int count = 0;
		if(thePostInf == null || thePostInf.isEmpty()) return 0;
		count = insertArrayPostsInf(thePostInf);
		return count;
	}
	
	/**
	 * 删除一门课程的所有信息，也就是一条记录。这调记录为courseInf1和courseInf2的相关的一门课程的信息，
	 * 如courseInf1的_id为1的行进行删除时courseInf2的link为1的行也要进行删除。
	 * @param theCourseInf 类型为Course类型
	 * @return boolean,表示删除是否成功。<br />另，若theCourseInf为空，返回false。
	 */
	public boolean deleteCourseInf(Course theCourseInf){
		if(theCourseInf == null) return false;
		int rowIndex = theCourseInf.getId();
		return (db.delete(DATABASE_COURSE_TABLE1, KEY_ID + "=" + rowIndex, null) > 0)
				&&(db.delete(DATABASE_COURSE_TABLE2, KEY_LINK + "=" + rowIndex, null) > 0);
	}
	
	
	/**
	 * 删除表post中的一条记录，此删除方法是根据参数传递进来的一个Post对象，获得这条消息的标题，从而根据标题进行删除。
	 * @param thePostInf 类型为Post类型
	 * @return boolean，表示删除是否已经成功。<br />另，若thePostInf为空，返回false。
	 */
	public boolean deletePostInf(Post thePostInf){
		if(thePostInf == null) return false;
		String theTitle = thePostInf.getTitle();
		return db.delete(DATABASE_POST_TABLE, KEY_TITLE + "= '" + theTitle + "'", null) > 0;
	}
	
	
	/**
	 * 对课程的更新操作，从参数传递进来一门课程，再从数据库中找到这门课的记录，然后进行比较，比较结果不一样就对数据库中的这条记录相应的字段进行更改。
	 * @param theCourseInf 类型为Course类型
	 */
	public void updateCourseInf(Course theCourseInf){
		if(theCourseInf == null) return;
		ContentValues newCourseInfValues1 = new ContentValues();
		ContentValues newCourseInfValues2 = new ContentValues();
		ContentValues newCourseInfValues3 = new ContentValues();
		
		//从传递进来的参数获得这门课程的记录的_id，进行查询，获得courseInf1和courseInf2中的这门课的记录。
		int rowIndex = theCourseInf.getId();
		Cursor cursor1 = db.query(DATABASE_COURSE_TABLE1, null, KEY_ID + "=" + rowIndex, null, null, null, null);
		Cursor cursor2 = db.query(DATABASE_COURSE_TABLE2, null, KEY_LINK + "=" + rowIndex, null, null, null, null);
		cursor1.moveToFirst();
		cursor2.moveToFirst();
		
		if(cursor1.getString(1) != null)//数据库里的字符串初始化值一般都为空，所以从数据库中获得字符串时要进行判断是否为空。为空是直接进行更新操作，不为空时进行数据库相应课程对应的字段的值与传递进来的课程相比较
		{
			if(!(cursor1.getString(1).equals(theCourseInf.getCode())))//判断数据库中与这门课传递进来的相应字段值是否相等
			{
				newCourseInfValues1.put(KEY_CODE, theCourseInf.getCode());
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}
		}else{
			newCourseInfValues1.put(KEY_CODE, theCourseInf.getCode());
			db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
		}
		
		if(cursor1.getString(2) != null)
		{
			if(!(cursor1.getString(2).equals(theCourseInf.getName())))
			{
				newCourseInfValues1.put(KEY_NAME, theCourseInf.getName());
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}
		}else{
			newCourseInfValues1.put(KEY_NAME, theCourseInf.getName());
			db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
		}
		
		if(cursor1.getString(3) != null)
		{
			if(!(cursor1.getString(3).equals(theCourseInf.getTeacherString())))
			{
				newCourseInfValues1.put(KEY_TEACHERS, theCourseInf.getTeacherString());
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}
		}else{
			newCourseInfValues1.put(KEY_TEACHERS, theCourseInf.getTeacherString());
			db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
		}
		
		if(cursor1.getInt(4) != theCourseInf.getCredit())
			if(theCourseInf.getCredit() < 0)
			{
				newCourseInfValues1.put(KEY_CREDIT, 0);
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}else{
				newCourseInfValues1.put(KEY_CREDIT, theCourseInf.getCredit());
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}
		
		if(cursor1.getString(5) != null)
		{
			if(!(cursor1.getString(5).equals(theCourseInf.getClassNumber())))
			{
				newCourseInfValues1.put(KEY_CLASS_NUMBER, theCourseInf.getClassNumber());
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}
		}else{
			newCourseInfValues1.put(KEY_CLASS_NUMBER, theCourseInf.getClassNumber());
			db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
		}
		
		if(cursor1.getString(6) != null)
		{
			if(!(cursor1.getString(6).equals(theCourseInf.getTeachingMaterial())))
			{
				newCourseInfValues1.put(KEY_TEACHING_MATERIAL, theCourseInf.getTeachingMaterial());
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}
		}else{
			newCourseInfValues1.put(KEY_TEACHING_MATERIAL, theCourseInf.getTeachingMaterial());
			db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
		}
		
		if(cursor2.getCount() != 0)//判断这门课的TimeAndAddress是否没有。如外教的时间和地点就没此时ArrayList对象的TimeAndAddress的size就为0，数据库中的courseInf2中就没了这条记录，
		{                          //当没这天记录时就进行插入操作，当有时就要进行比较是否一样，不一样就要进行更改。
			for(int i = 0; i < theCourseInf.getTimeAndAddress().size(); i++){
				cursor2.moveToPosition(i);
				if(cursor2.getInt(2) != theCourseInf.getTimeAndAddress().get(i).getWeek())
				{
					newCourseInfValues2.put(KEY_WEEK, theCourseInf.getTimeAndAddress().get(i).getWeek());
					db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
				}
				
				if(cursor2.getInt(3) != theCourseInf.getTimeAndAddress().get(i).getDay())
				{
					newCourseInfValues2.put(KEY_DAY, theCourseInf.getTimeAndAddress().get(i).getDay());
					db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
				}
				
				if(cursor2.getInt(4) != theCourseInf.getTimeAndAddress().get(i).getPeriod())
				{
					newCourseInfValues2.put(KEY_PERIOD, theCourseInf.getTimeAndAddress().get(i).getPeriod());
					db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
				}
					
				if(cursor2.getString(5) != null)
				{
					if(!(cursor2.getString(5).equals(theCourseInf.getTimeAndAddress().get(i).getAddress())))
					{
						newCourseInfValues2.put(KEY_ADDRESS, theCourseInf.getTimeAndAddress().get(i).getAddress());
						db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
					}
				}else{
					newCourseInfValues2.put(KEY_ADDRESS, theCourseInf.getTimeAndAddress().get(i).getAddress());
					db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
				}
			}
		}else{
			for(int j=0; j < theCourseInf.getTimeAndAddress().size(); j++){
					newCourseInfValues3.put(KEY_LINK, rowIndex);
					newCourseInfValues3.put(KEY_VICEID, (Integer.toString(rowIndex) + (j + 1)));
					newCourseInfValues3.put(KEY_WEEK, theCourseInf.getTimeAndAddress().get(j).getWeek());
					newCourseInfValues3.put(KEY_DAY, theCourseInf.getTimeAndAddress().get(j).getDay());
					newCourseInfValues3.put(KEY_PERIOD, theCourseInf.getTimeAndAddress().get(j).getPeriod());
					newCourseInfValues3.put(KEY_ADDRESS, theCourseInf.getTimeAndAddress().get(j).getAddress());
					
					db.insert(DATABASE_COURSE_TABLE2,null,newCourseInfValues3);
			}
		}
	}
	
	/**
	 * 成绩只有更新，因为在课程插入时就已经对成绩进行了初始化。
	 * @param theScoreInf 类型为List<Course>,当出一门课程成绩时生成一个成员的ArrrayList类型，就可以调用此方法进行更新。
	 */
	public void updateScoreInf(List<Course> theScoreInf){
		if(theScoreInf == null || theScoreInf.isEmpty()) return;
		ContentValues newCourseInfValues1 = new ContentValues();
		
		for(Course aScore:theScoreInf){
			Cursor cursor1 = db.query(DATABASE_COURSE_TABLE1, null, KEY_CODE + " = '" + aScore.getCode() + "'", null, null, null, null);
			//getCode()得到课程代码实现成绩插入到相应的课程中。getCode()是字符串所以两边要加单引号。
			if(cursor1.getCount() == 0)
			{
				continue;
			}
			cursor1.moveToFirst();
			if(cursor1.getInt(7) != aScore.getYear())//判断数据库的内容和ArrayList的成绩对象的相关字段是否想等
				if(aScore.getYear() <= 1900)
				{
					newCourseInfValues1.put(KEY_YEAR, 0);
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + aScore.getCode() + "'", null);
				}else{
					newCourseInfValues1.put(KEY_YEAR, aScore.getYear());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + aScore.getCode() + "'", null);
				}
			
			if(cursor1.getString(8) != convert(aScore.isFirstSemester()))
			{
				newCourseInfValues1.put(KEY_ISFIRSTSEMESTER, convert(aScore.isFirstSemester()));
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + aScore.getCode() + "'", null);
			}
			
			if(cursor1.getFloat(9) != aScore.getTestScore())
				if(aScore.getTestScore() < 0)
				{
					newCourseInfValues1.put(KEY_TEST_SCORE, Float.NaN);
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + aScore.getCode() + "'", null);
				}else{
					newCourseInfValues1.put(KEY_TEST_SCORE, aScore.getTestScore());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + aScore.getCode() + "'", null);
				}
			
			if(cursor1.getFloat(10) != aScore.getTotalScore())
				if(aScore.getTotalScore() < 0)
				{
					newCourseInfValues1.put(KEY_TOTAL_SCORE, Float.NaN);
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + aScore.getCode() + "'", null);
				}else{
					newCourseInfValues1.put(KEY_TOTAL_SCORE, aScore.getTotalScore());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + aScore.getCode() + "'", null);
				}
			
			if(cursor1.getString(11) != null)//成绩这一块已经在课程的表courseInf1建立时就已经初始化了，初始化时字符串是空的，所以这里要判断是否为空
			{
				if(!(cursor1.getString(11).equals(aScore.getKind())))
				{
					newCourseInfValues1.put(KEY_KIND, aScore.getKind());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + aScore.getCode() + "'", null);
				}
			}else{
				newCourseInfValues1.put(KEY_KIND, aScore.getKind());
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + aScore.getCode() + "'", null);
			}
			
			if(cursor1.getString(12) != null)
			{
				if(!(cursor1.getString(12).equals(aScore.getNote())))
				{
					newCourseInfValues1.put(KEY_NOTE, aScore.getNote());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + aScore.getCode() + "'", null);
				}
			}else{
				newCourseInfValues1.put(KEY_NOTE, aScore.getNote());
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + aScore.getCode() + "'", null);
			}
		}
	}
	
	/**
	 *  返回本学期课程 
	 * @param order
	 * @param theUserName
	 * @return  ArrayList<Course>
	 * @throws SQLException
	 */
	public ArrayList<Course> getThisTermCoursesFromDB(String order, String theUserName) throws SQLException {
		ArrayList<Course> courses = new ArrayList<Course>();
		courses = getCoursesFromDB(KEY_YEAR + "=" + 0 + " AND " + KEY_CURRENT_SEMESTER + "=" + 1, order, theUserName);
		return courses;
	}
	
	/**
	 * 返回下学期课程
	 * @param order
	 * @param theUserName
	 * @return ArrayList<Course>
	 * @throws SQLException
	 */
	public ArrayList<Course> getNextTermCoursesFromDB(String order, String theUserName) throws SQLException{
		ArrayList<Course> courses = new ArrayList<Course>();
		courses = getCoursesFromDB(KEY_YEAR + "=" + 0 + " AND " + KEY_CURRENT_SEMESTER + "=" + 0, order, theUserName);
		return courses;
	}
	
    /**
     * 从数据库中一次获得courseInf1和courseInf2中的在where条件下的所有记录，也就是所有课程信息包括每门课程的成绩。  
     * @param where相当于mysql的where。 调用时参数的用法如：StudentInfDBAdapter.KEY_YEAR + "=" + 2011, StudentInfDBAdapter.KEY_CODE + " DESC"。
     * @param theUsername 
     * @return ArrayList<Course>
     * @throws CourseException
     */
	public ArrayList<Course> getCoursesFromDB(String where,String order, String theUserName) throws SQLException{
		 Cursor cursor = db.query(DATABASE_COURSE_TABLE1, null, KEY_USER_NAME + "= '" + theUserName + "'", null, null, null, null);
		 if(cursor.getCount() == 0)
		 {
			 throw new SQLException("no this user data");
		 }
		 ArrayList<Course> courses = new ArrayList<Course>();
		 Course course = new Course();
		 Cursor cursor1 = db.query(DATABASE_COURSE_TABLE1, null, where, null, null, null, order);
		 if((cursor1.getCount() == 0) || !cursor1.moveToFirst()){
			 throw new SQLException("No course found from database");
		 }
		 else{
			 for(int i = 0; i <cursor1.getCount(); i++){
				 cursor1.moveToPosition(i);
				 int newId = cursor1.getInt(0);
				 String newCode = cursor1.getString(1);
				 String newName = cursor1.getString(2);
				 String newTeachers = cursor1.getString(3);
				 int newCredit = cursor1.getInt(4);
				 String newClassNumber = cursor1.getString(5);
				 String newTeachingMaterial = cursor1.getString(6);
				 int newYear = cursor1.getInt(7);
				 String newIsFirstSemester = cursor1.getString(8);
				 float newTestScore = cursor1.getFloat(9);
				 float newTotalScore = cursor1.getFloat(10);
				 String newKind = cursor1.getString(11);
				 String newNote = cursor1.getString(12);
				 
				 ArrayList<TimeAndAddress> timeAndAddresses = new ArrayList<TimeAndAddress>();
				 TimeAndAddress timeAndAddress = new TimeAndAddress();
				 Cursor cursor2 = db.query(DATABASE_COURSE_TABLE2, null, KEY_LINK + "=" + newId, null, null, null, null);
				 if((cursor2.getCount() == 0) || !cursor2.moveToFirst()){
					 
				 }
				 else{
					 for(int j = 0; j < cursor2.getCount(); j++){
						 cursor2.moveToPosition(j);
						 int newWeek = cursor2.getInt(2);
						 int newDay = cursor2.getInt(3);
						 int newPeriod = cursor2.getInt(4);
						 String newAddress = cursor2.getString(5);
						 try{
							 timeAndAddress.setWeek(newWeek);
							 timeAndAddress.setDay(newDay);
							 timeAndAddress.setPeriod(newPeriod);
							 timeAndAddress.setAddress(newAddress);
						 }catch(BitOperateException e){
							 e.printStackTrace();
						 }catch(NullPointerException e){
							 e.printStackTrace();
						 }
						 timeAndAddresses.add(new TimeAndAddress(timeAndAddress));
						 //这里使用了TimeAndAddress的拷贝构造方法进行深拷贝。
					 }
				 }
				 course.setId(newId);
				 course.setCode(newCode);
				 course.setName(newName);
				 course.setClassNumber(newClassNumber);
				 course.setTeachingMaterial(newTeachingMaterial);
				 course.setIsFirstSemester(reconvert(newIsFirstSemester));
				 course.setKind(newKind);
				 course.setNote(newNote);
				 course.setTimeAndAddresse(timeAndAddresses);
				 course.setTeachers(newTeachers);
				 try{
					 course.setCredit(newCredit);
					 if(newYear != 0)course.setYear(newYear);
					 if(newTestScore != Float.NaN)course.setTestScore(newTestScore);
					 if(newTotalScore != Float.NaN)course.setTotalScore(newTotalScore);
				 }catch(NullPointerException e){
					 e.printStackTrace();
				 }catch(CourseException e){
					 e.printStackTrace();
				 }
				 courses.add(new Course(course));
				 //这里使用了Course的拷贝构造方法进行了深拷贝。
			 }
		 }
		 return courses;
	 }
	
	/**
	 * 从数据库中的courseInf1和courseInf2返回所有课程也包括成绩。
	 * @param theUserName
	 * @return Array<ArrayList<Course>>
	 * @throws SQLException
	 */
	public ArrayList<ArrayList<Course>> getAllCoursesFromDB(String theUserName) throws SQLException, CourseException{
		Cursor cursor = db.query(DATABASE_COURSE_TABLE1, null, KEY_USER_NAME + "= '" + theUserName + "'", null, null, null, null);
		 if(cursor.getCount() == 0)
		 {
			 throw new SQLException("no this user data");
		 }
		 
		 String year[] = {"year"};
		 Cursor cursor3 = db.query(true, DATABASE_COURSE_TABLE1, year, null, null, null, null, null, null);
		 int sum[] = new int[cursor3.getCount()];
		 if((cursor3.getCount() == 0) || !cursor3.moveToFirst()){
			 throw new SQLException("No course found from database");
		 }
		 else{
			 for(int i = 0; i < cursor3.getCount(); i++){
				 cursor3.moveToPosition(i);
				 sum[i] = cursor3.getInt(0);
			 }
		 }
		 
		 ArrayList<ArrayList<Course>> all = new ArrayList<ArrayList<Course>>();
		 for (int m = 0; m < sum.length; m++){ 
			 for(int n = 0; n < 2; n++){
				 String temp;
				 if(n == 0)
					 temp = "t";
				 else
					 temp = "f";
				 
				ArrayList<Course> courses = new ArrayList<Course>();
				Course course = new Course();
				Cursor cursor1 = db.query(DATABASE_COURSE_TABLE1, null, KEY_YEAR + "=" + sum[m] + " AND " + KEY_ISFIRSTSEMESTER + "= '" + temp + "'", null, null, null, null);
				if((cursor1.getCount() == 0) || !cursor1.moveToFirst()){
					continue;
				}
				else{
					for(int i = 0; i <cursor1.getCount(); i++){
						cursor1.moveToPosition(i);
						int newId = cursor1.getInt(0);
						String newCode = cursor1.getString(1);
						String newName = cursor1.getString(2);
						String newTeachers = cursor1.getString(3);
						int newCredit = cursor1.getInt(4);
						String newClassNumber = cursor1.getString(5);
						String newTeachingMaterial = cursor1.getString(6);
						int newYear = cursor1.getInt(7);
						String newIsFirstSemester = cursor1.getString(8);
						float newTestScore = cursor1.getFloat(9);
						float newTotalScore = cursor1.getFloat(10);
						String newKind = cursor1.getString(11);
						String newNote = cursor1.getString(12);
							 
						ArrayList<TimeAndAddress> timeAndAddresses = new ArrayList<TimeAndAddress>();
						TimeAndAddress timeAndAddress = new TimeAndAddress();
						Cursor cursor2 = db.query(DATABASE_COURSE_TABLE2, null, KEY_LINK + "=" + newId, null, null, null, null);
						if((cursor2.getCount() == 0) || !cursor2.moveToFirst()){
								 
						}
						else{
							for(int j = 0; j < cursor2.getCount(); j++){
								cursor2.moveToPosition(j);
								int newWeek = cursor2.getInt(2);
								int newDay = cursor2.getInt(3);
								int newPeriod = cursor2.getInt(4);
								String newAddress = cursor2.getString(5);
								try{
									timeAndAddress.setWeek(newWeek);
									timeAndAddress.setDay(newDay);
									timeAndAddress.setPeriod(newPeriod);
									timeAndAddress.setAddress(newAddress); 
								}catch(BitOperateException e){
									e.printStackTrace();
								}catch(NullPointerException e){
									e.printStackTrace();
								}
								timeAndAddresses.add(new TimeAndAddress(timeAndAddress));
								//这里使用了TimeAndAddress的拷贝构造方法进行深拷贝。
						    }
						}
						course.setId(newId);
						course.setCode(newCode);
						course.setName(newName);
						course.setClassNumber(newClassNumber);
						course.setTeachingMaterial(newTeachingMaterial);
						course.setIsFirstSemester(reconvert(newIsFirstSemester));
						course.setKind(newKind);
						course.setNote(newNote);
						try{
							course.setTimeAndAddresse(timeAndAddresses);
							course.setTeachers(newTeachers);
							course.setCredit(newCredit);
							if(newYear != 0)course.setYear(newYear);
							if(newTestScore != Float.NaN)course.setTestScore(newTestScore);
							if(newTotalScore != Float.NaN)course.setTotalScore(newTotalScore);
						}catch(NullPointerException e){
							e.printStackTrace();
						}catch(CourseException e){
							e.printStackTrace();
						}
						courses.add(new Course(course));
							 //这里使用了Course的拷贝构造方法进行了深拷贝。
						}
				   }
			 all.add(courses);
			 }
		 }
		 return all;
	 }
	
	/**
	 * 从数据库中的courseInf1和courseInf2返回一门课程和成绩。
	 * @param where相当于mysql的where。 调用时参数的用法如：StudentInfDBAdapter.KEY_NAME + "=" + 营销学.
	 * @param theUserName
	 * @return Course.
	 * @throws CourseException
	 */
	public Course getCourseFromDB(String where, String theUserName) throws SQLException{
		Cursor cursor = db.query(DATABASE_COURSE_TABLE1, null, KEY_USER_NAME + "= '" + theUserName + "'", null, null, null, null);
		 if(cursor.getCount() == 0)
		 {
			 throw new SQLException("no this user data");
		 }
		 Course course = new Course();
		 Cursor cursor1 = db.query(DATABASE_COURSE_TABLE1, null, where, null, null, null, null);
		 if((cursor1.getCount() == 0) || !cursor1.moveToFirst()){
			 throw new SQLException("No course found from database");
		 }
		 else{
			 int newId = cursor1.getInt(0);
			 String newCode = cursor1.getString(1);
			 String newName = cursor1.getString(2);
			 String newTeachers = cursor1.getString(3);
			 int newCredit = cursor1.getInt(4);
			 String newClassNumber = cursor1.getString(5);
			 String newTeachingMaterial = cursor1.getString(6);
			 int newYear = cursor1.getInt(7);
			 String newIsFirstSemester = cursor1.getString(8);
			 float newTestScore = cursor1.getFloat(9);
			 float newTotalScore = cursor1.getFloat(10);
			 String newKind = cursor1.getString(11);
			 String newNote = cursor1.getString(12);
			 
			 ArrayList<TimeAndAddress> timeAndAddresses = new ArrayList<TimeAndAddress>();
			 TimeAndAddress timeAndAddress = new TimeAndAddress();
			 Cursor cursor2 = db.query(DATABASE_COURSE_TABLE2, null, KEY_LINK + "=" + newId, null, null, null, null);
			 if((cursor2.getCount() == 0) || !cursor2.moveToFirst()){
				 
			 }
			 else{
				 for(int j = 0; j < cursor2.getCount(); j++){
					 cursor2.moveToPosition(j);
					 int newWeek = cursor2.getInt(2);
					 int newDay = cursor2.getInt(3);
					 int newPeriod = cursor2.getInt(4);
					 String newAddress = cursor2.getString(5);
					 try{
						 timeAndAddress.setWeek(newWeek);
						 timeAndAddress.setDay(newDay);
						 timeAndAddress.setPeriod(newPeriod);
						 timeAndAddress.setAddress(newAddress);
					 }catch(BitOperateException e){
						 e.printStackTrace();
					 }catch(NullPointerException e){
						 e.printStackTrace();
					 }
					 timeAndAddresses.add(new TimeAndAddress(timeAndAddress));
				 }
			 }
			 course.setId(newId);
			 course.setCode(newCode);
			 course.setName(newName);
			 course.setClassNumber(newClassNumber);
			 course.setTeachingMaterial(newTeachingMaterial);
			 course.setIsFirstSemester(reconvert(newIsFirstSemester));
			 course.setKind(newKind);
			 course.setNote(newNote);
			 course.setTimeAndAddresse(timeAndAddresses);
			 course.setTeachers(newTeachers);
			 try{
				 course.setCredit(newCredit);
				 if(newYear != 0)course.setYear(newYear);
				 if(newTestScore != Float.NaN)course.setTestScore(newTestScore);
				 if(newTotalScore != Float.NaN)course.setTotalScore(newTotalScore);
				 course.setTimeAndAddresse(timeAndAddresses);
			 }catch(NullPointerException e){
				 e.printStackTrace();
			 }catch(CourseException e){
				 e.printStackTrace();
			 }
		 }
		 return new Course(course);
	}
	
	/**
	 * 在where条件下，从post表中读取所有的通知信息。
	 * @param where。order.limit这三个参数和mysql的用法一样，调用时的参数用法如：StudentInfDBAdapter.KEY_TITLE + "=" + xxxx, StudentInfDBAdapter.KEY_DATE + " DESC", "2".
	 * @return ArrayList<Post>
	 * @throws SQLException
	 */
	public ArrayList<Post> getPostsFromDB(String where, String order, String limit)throws SQLException{
		ArrayList<Post> posts = new ArrayList<Post>();
		Post post = new Post();
		Cursor cursor = db.query(DATABASE_POST_TABLE, null, where, null, null, null, order, limit);
		
		if((cursor.getCount() == 0) || !cursor.moveToFirst())
		{
			throw new SQLException("No post found from database");
		}else{
			for(int i = 0; i < cursor.getCount(); i++){
				cursor.moveToPosition(i);
				Long newId = cursor.getLong(0);
				int newSource = cursor.getInt(1);
				String newCategory = cursor.getString(2);
				String newTitle = cursor.getString(3);
				String newUrl = cursor.getString(4);
				String newAuthor = cursor.getString(5);
				long newDate = cursor.getLong(6);
				//date在数据库中的存储类型为integer（integer会根据数据的量级自动改变位数），date是长整型存储的所以要用getLong()，否则会丢失位数。
				String newMainBody = cursor.getString(7);
				
				post.setId(newId);
				post.setSource((byte)newSource);
				post.setCategory(newCategory);
				post.setTitle(newTitle);
				post.setUrl(newUrl);
				post.setAuthor(newAuthor);
				post.setDate(new Date(newDate));//new Date(newDate))是把整型的date数据转换成Date类型
				post.setMainBody(newMainBody);
				posts.add(new Post(post));
				//这里用了Post的拷贝构造方法进行深拷贝。
			}
		}
		return posts;
	}
	
	/**
	 * 更新MainBody字段。
	 * @param thePost。 Post类型
	 */
	public void updatePostInf(Post thePost){
		if(thePost == null) return;
		ContentValues newPostValue = new ContentValues();
		Long rowIndex = thePost.getId();
		Cursor cursor = db.query(DATABASE_POST_TABLE, null, KEY_POST_ID + "=" + rowIndex, null, null, null, null);
		cursor.moveToFirst();
		if(cursor.getString(7) != null)
		{
			if(!(cursor.getString(7).equals(thePost.getMainBody())))
			{
				newPostValue.put(KEY_MAINBODY, thePost.getMainBody());
				db.update(DATABASE_POST_TABLE, newPostValue, KEY_POST_ID + "=" + rowIndex, null);
			}
		}else{
			newPostValue.put(KEY_MAINBODY, thePost.getMainBody());
			db.update(DATABASE_POST_TABLE, newPostValue, KEY_POST_ID + "=" + rowIndex, null);
		}
	}
	 	
}