package org.orange.studentinformationdatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import util.BitOperate.BitOperateException;
import util.webpage.Course;
import util.webpage.Course.CourseException;
import util.webpage.Course.TimeAndAddress;
import util.webpage.Course.TimeAndAddress.TimeAndAddressException;
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
	private static final String DATABASE_POST_TABLE = "post";
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
	
	private static final String KEY_ID = "_id";
	private static final String KEY_CODE = "code";
	private static final String KEY_NAME = "name";
	private static final String KEY_TEACHERS = "teacrhers";
	private static final String KEY_CREDIT = "credit";
	private static final String KEY_CLASS_NUMBER = "class_number";
	private static final String KEY_TEACHING_MATERIAL = "teachingmaterial";
	private static final String KEY_YEAR = "year";
	private static final String KEY_ISFIRSTSEMESTER = "is_first_semester";
	private static final String KEY_TEST_SCORE = "test_score";
	private static final String KEY_TOTAL_SCORE = "total_score";
	private static final String KEY_KIND = "kind";
	private static final String KEY_NOTE = "note";
	
	
	private static final String KEY_LINK = "link";
	private static final String KEY_VICEID = "viceid";
	private static final String KEY_WEEK ="week";
	private static final String KEY_DAY = "day";
	private static final String KEY_PERIOD = "period";
	private static final String KEY_ADDRESS ="address";
	
	
	private static final String KEY_POST_ID = "post_id";
	private static final String KEY_SOURCE = "source";
	private static final String KEY_CATEGORY = "category";
	private static final String KEY_TITLE = "title";
	private static final String KEY_URL = "url";
	private static final String KEY_AUTHOR = "author";
	private static final String KEY_DATE = "date";
	
	
	/*
	 *  内部类，构建数据库用的，生成courseInf1、courseInf2和post表。
	 */
	private static class StudentInfDBOpenHelper extends SQLiteOpenHelper{
		public StudentInfDBOpenHelper(Context context, String name, CursorFactory factory, int version){
			super(context, name, factory,version);
		}
	
	    private static final String COURSE_TABLE1_CREATE = "create table " + DATABASE_COURSE_TABLE1 + "(" + KEY_ID + " integer primary key autoincrement,"
	    + KEY_CODE + " character(7)," + KEY_NAME + " varchar(15)," + KEY_TEACHERS + " varchar(15)," + 
	    KEY_CREDIT + " tinyint," + KEY_CLASS_NUMBER + " varchar(5)," + KEY_TEACHING_MATERIAL + " varchar(15)," + KEY_YEAR + " integer," + KEY_ISFIRSTSEMESTER
	    + " varchar(1)," + KEY_TEST_SCORE + " integer," + KEY_TOTAL_SCORE + " integer," + KEY_KIND + " varchar(5)," + KEY_NOTE + " varchar(30));";
	    
	    private static final String COURSE_TABLE2_CREATE = "create table " + DATABASE_COURSE_TABLE2 + "(" + KEY_LINK + " integer," + KEY_VICEID + " varchar(5),"
	    + KEY_WEEK + " integer," + KEY_DAY + " integer," + KEY_PERIOD + " integer," + KEY_ADDRESS + " varchar(5));";
	    
	    private static final String POST_TABLE_CREATE = "create table " + DATABASE_POST_TABLE + "(" + KEY_POST_ID + " integer primary key autoincrement," 
	    + KEY_SOURCE + " integer," + KEY_CATEGORY + " varchar(35)," + KEY_TITLE + " varchar(35)," + KEY_URL + " varchar(60)," + KEY_AUTHOR + " varchar(15),"
	    + KEY_DATE + " integer);"; 
	
	    public void onCreate(SQLiteDatabase theDB){
		    theDB.execSQL(COURSE_TABLE1_CREATE);
		    theDB.execSQL(COURSE_TABLE2_CREATE);
		    theDB.execSQL(POST_TABLE_CREATE);
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
	
	public void close(){
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
	 * 而当做成绩'插入'时已经为更新操作而不是真正的插入
	 * @param theCourseInf 类型为 ArrayList<Course>
	 */
	public void insertArrayCoursesInf(ArrayList<Course> theCourseInf){
		ContentValues newCourseInfValues = new ContentValues();
		
		for(int i = 0; i < theCourseInf.size(); i++){
			newCourseInfValues.put(KEY_CODE, theCourseInf.get(i).getCode());
			newCourseInfValues.put(KEY_NAME, theCourseInf.get(i).getName());
			newCourseInfValues.put(KEY_TEACHERS, theCourseInf.get(i).getTeacherString());
			newCourseInfValues.put(KEY_CREDIT, theCourseInf.get(i).getCredit());
			newCourseInfValues.put(KEY_CLASS_NUMBER, theCourseInf.get(i).getClassNumber());
			newCourseInfValues.put(KEY_TEACHING_MATERIAL, theCourseInf.get(i).getTeachingMaterial());
			newCourseInfValues.put(KEY_YEAR, theCourseInf.get(i).getYear());
			newCourseInfValues.put(KEY_ISFIRSTSEMESTER, convert(theCourseInf.get(i).isFirstSemester()));
			newCourseInfValues.put(KEY_TEST_SCORE, theCourseInf.get(i).getTestScore());
			newCourseInfValues.put(KEY_TOTAL_SCORE, theCourseInf.get(i).getTotalScore());
			newCourseInfValues.put(KEY_KIND, theCourseInf.get(i).getKind());
			newCourseInfValues.put(KEY_NOTE, theCourseInf.get(i).getNote());
			//theCourseInf为ArrayList对象，get(i)顺序找到其中的一门课程。getCode()等方法得到相应实例变量的值。
			
			ContentValues newCourseInfTAValues = new ContentValues();
			
			//此for循环为对表courseInf2的插入操作，courseInf2为时间地址的存储与courseInf1相关联，为for循环是因为TimeAndAddress也是ArrayList对象。
			for(int m=0; m < theCourseInf.get(i).getTimeAndAddress().size(); m++){
				newCourseInfTAValues.put(KEY_LINK, db.query(DATABASE_COURSE_TABLE1, null, null, null, null, null, null).getCount()+1);
				//link列是courseInf1和courseInf2相‘连接’的字段以执行相应的操作，所以link列须和_id列值相等。先对courseInf1进行query操作再进行getCount()
				//得到courseInf1的行数，加1是因为外层的for循环还没插入。这样courseInf2的link值就和courseInf1的_id值相等了。
				newCourseInfTAValues.put(KEY_VICEID, Integer.toString(db.query(DATABASE_COURSE_TABLE1, null, null, null, null, null, null)
						.getCount()+1) + Integer.toString(m+1));
				//一门课有多个TimeAndAddress与之相对应，viceid列是为了对其中一个TimeAndAddress进行操作。例如当link列为1时viceid就为11、12、、、等
				//当然link的1是整数型的而viceid的11、12为字符串类型的。
				newCourseInfTAValues.put(KEY_WEEK, theCourseInf.get(i).getTimeAndAddress().get(m).getWeek());
				newCourseInfTAValues.put(KEY_DAY, theCourseInf.get(i).getTimeAndAddress().get(m).getDay());
				newCourseInfTAValues.put(KEY_PERIOD, theCourseInf.get(i).getTimeAndAddress().get(m).getPeriod());
				newCourseInfTAValues.put(KEY_ADDRESS, theCourseInf.get(i).getTimeAndAddress().get(m).getAddress());
				
				db.insert(DATABASE_COURSE_TABLE2,null,newCourseInfTAValues);
			}
			
			db.insert(DATABASE_COURSE_TABLE1,null, newCourseInfValues);
		}
		
	}
	
	/**
	 * 此插入方法实则为更新（上面也已经说到了），之所以叫插入是因为当每门课的成绩出来时就可以放到相应的课程上。
	 * @param theScoreInf 类型为ArrayList,当出一门课程成绩时生成一个成员的ArrrayList类型，就可以调用此方法进行‘插入’。
	 */
	public void insertScoreInf(ArrayList<Course> theScoreInf){
		ContentValues newCourseInfValues1 = new ContentValues();
		
		for(int i = 0; i < theScoreInf.size(); i ++){
			Cursor cursor1 = db.query(DATABASE_COURSE_TABLE1, null, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null, null, null, null);
			//getCode()得到课程代码实现成绩插入到相应的课程中。getCode()是字符串所以两边要加单引号。
			cursor1.moveToFirst();
			if(cursor1.getInt(7) != theScoreInf.get(i).getYear())//判断数据库的内容和ArrayList的成绩对象的相关字段是否想等
				if(theScoreInf.get(i).getYear() < 0)
				{
					newCourseInfValues1.put(KEY_YEAR, "");
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
				}else{
					newCourseInfValues1.put(KEY_YEAR, theScoreInf.get(i).getYear());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
				}
			
			if(cursor1.getString(8) != convert(theScoreInf.get(i).isFirstSemester()))
			{
				newCourseInfValues1.put(KEY_ISFIRSTSEMESTER, convert(theScoreInf.get(i).isFirstSemester()));
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
			}
			
			if(cursor1.getInt(9) != theScoreInf.get(i).getTestScore())
				if(theScoreInf.get(i).getTestScore() < 0)
				{
					newCourseInfValues1.put(KEY_TEST_SCORE, "");
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
				}else{
					newCourseInfValues1.put(KEY_TEST_SCORE, theScoreInf.get(i).getTestScore());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
				}
			
			if(cursor1.getInt(10) != theScoreInf.get(i).getTotalScore())
				if(theScoreInf.get(i).getTotalScore() < 0)
				{
					newCourseInfValues1.put(KEY_TOTAL_SCORE, "");
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
				}else{
					newCourseInfValues1.put(KEY_TOTAL_SCORE, theScoreInf.get(i).getTotalScore());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
				}
			
			if(cursor1.getString(11) != null)//成绩这一块已经在课程的表courseInf1建立时就已经初始化了，初始化时字符串是空的，所以这里要判断是否为空
			{
				if(!(cursor1.getString(11).equals(theScoreInf.get(i).getKind())))
					if(theScoreInf.get(i).getKind() == null)
					{
						newCourseInfValues1.put(KEY_KIND, "" );
						db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
					}else{
						newCourseInfValues1.put(KEY_KIND, theScoreInf.get(i).getKind());
						db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
					}
			}else{
				if(theScoreInf.get(i).getKind() == null)
				{
					newCourseInfValues1.put(KEY_KIND, "" );
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
				}else{
					newCourseInfValues1.put(KEY_KIND, theScoreInf.get(i).getKind());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
				}
			}
			
			if(cursor1.getString(12) != null)
			{
				if(!(cursor1.getString(12).equals(theScoreInf.get(i).getNote())))
					if(theScoreInf.get(i).getNote() == null)
					{
						newCourseInfValues1.put(KEY_NOTE, "");
						db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
					}else{
						newCourseInfValues1.put(KEY_NOTE, theScoreInf.get(i).getNote());
						db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
					}
			}else{
				if(theScoreInf.get(i).getNote() == null)
				{
					newCourseInfValues1.put(KEY_NOTE, "" );
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
				}else{
					newCourseInfValues1.put(KEY_NOTE, theScoreInf.get(i).getNote());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_CODE + " = '" + theScoreInf.get(i).getCode() + "'", null);
				}
			}
		}
	}
	
	/**
	 * 这个课程插入的方法是一次插入一门课程及相关成绩的初始化，
	 * @param theCourseInf 类型为Course类型
	 */
	public void insertCourseInf(Course theCourseInf){
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
		
		ContentValues newCourseInfTAValues = new ContentValues();
		
		for(int j=0; j < theCourseInf.getTimeAndAddress().size(); j++){
			newCourseInfTAValues.put(KEY_LINK, db.query(DATABASE_COURSE_TABLE1, null, null, null, null, null, null).getCount()+1);
			newCourseInfTAValues.put(KEY_VICEID, Integer.toString(db.query(DATABASE_COURSE_TABLE1, null, null, null, null, null, null)
					.getCount()+1) + Integer.toString(j+1));
			newCourseInfTAValues.put(KEY_WEEK, theCourseInf.getTimeAndAddress().get(j).getWeek());
			newCourseInfTAValues.put(KEY_DAY, theCourseInf.getTimeAndAddress().get(j).getDay());
			newCourseInfTAValues.put(KEY_PERIOD, theCourseInf.getTimeAndAddress().get(j).getPeriod());
			newCourseInfTAValues.put(KEY_ADDRESS, theCourseInf.getTimeAndAddress().get(j).getAddress());
			
			db.insert(DATABASE_COURSE_TABLE2,null,newCourseInfTAValues);
		}
		
		db.insert(DATABASE_COURSE_TABLE1, null, newCourseInfValues);
	}
	
	
	/**
	 * 解析出的通知通过此插入操作方法存入到数据库的post表中。
	 * @param thePostInf 类型为ArrayList<Post>类型
	 */
	public void insertArrayPostsInf(ArrayList<Post> thePostInf){
		ContentValues newPostInfValues = new ContentValues();
		
		for(int i = 0; i < thePostInf.size(); i++){
			newPostInfValues.put(KEY_SOURCE, thePostInf.get(i).getSource());
			newPostInfValues.put(KEY_CATEGORY, thePostInf.get(i).getCategory());
			newPostInfValues.put(KEY_TITLE, thePostInf.get(i).getTitle());
			newPostInfValues.put(KEY_URL, thePostInf.get(i).getUrl());
			newPostInfValues.put(KEY_AUTHOR, thePostInf.get(i).getAuthor());
			newPostInfValues.put(KEY_DATE, thePostInf.get(i).getDate().getTime());
			//thePostInf.get(i).getDate().getTime()获取date字段并进行转换为长整型数据。
			
			db.insert(DATABASE_POST_TABLE,null, newPostInfValues);
		}
		
	}
	
	
	/**
	 * 删除一门课程的所有信息，也就是一条记录。这调记录为courseInf1和courseInf2的相关的一门课程的信息，
	 * 如courseInf1的_id为1的行进行删除时courseInf2的link为1的行也要进行删除。
	 * @param theCourseInf 类型为Course类型
	 * @return boolean,表示删除是否成功。
	 */
	public boolean deleteCourseInf(Course theCourseInf){
		int rowIndex = theCourseInf.getId();
		return (db.delete(DATABASE_COURSE_TABLE1, KEY_ID + "=" + rowIndex, null) > 0)
				&&(db.delete(DATABASE_COURSE_TABLE2, KEY_LINK + "=" + rowIndex, null) > 0);
	}
	
	
	/**
	 * 删除表post中的一条记录，此删除方法是根据参数传递进来的一个Post对象，获得这条消息的标题，从而根据标题进行删除。
	 * @param thePostInf 类型为Post类型
	 * @return boolean，表示删除是否已经成功
	 */
	public boolean deletePostInf(Post thePostInf){
		String theTitle = thePostInf.getTitle();
		return db.delete(DATABASE_POST_TABLE, KEY_TITLE + "= '" + theTitle + "'", null) > 0;
	}
	
	
	/**
	 * 对课程的更新操作，从参数传递进来一门课程，再从数据库中找到这门课的记录，然后进行比较，比较结果不一样就对数据库中的这条记录相应的字段进行更改。
	 * @param theCourseInf 类型为Course类型
	 */
	public void updateCourseInf(Course theCourseInf){
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
				if(theCourseInf.getCode() == null)
				{
					newCourseInfValues1.put(KEY_CODE, "");
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
				}else{
					newCourseInfValues1.put(KEY_CODE, theCourseInf.getCode());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
				}
		}else{
			if(theCourseInf.getCode() == null)
			{
				newCourseInfValues1.put(KEY_CODE, "");
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}else{
				newCourseInfValues1.put(KEY_CODE, theCourseInf.getCode());
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}
		}
		
		if(cursor1.getString(2) != null)
		{
			if(!(cursor1.getString(2).equals(theCourseInf.getName())))
				if(theCourseInf.getName() == null)
				{
					newCourseInfValues1.put(KEY_NAME, "");
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
				}else{
					newCourseInfValues1.put(KEY_NAME, theCourseInf.getName());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
				}
		}else{
			if(theCourseInf.getName() == null)
			{
				newCourseInfValues1.put(KEY_NAME, "");
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}else{
				newCourseInfValues1.put(KEY_NAME, theCourseInf.getName());
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}
		}
		
		if(cursor1.getString(3) != null)
		{
			if(!(cursor1.getString(3).equals(theCourseInf.getTeacherString())))
				if(theCourseInf.getTeacherString() == null)
				{
					newCourseInfValues1.put(KEY_TEACHERS, "");
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
				}else{
					newCourseInfValues1.put(KEY_TEACHERS, theCourseInf.getTeacherString());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
				}
		}else{
			if(theCourseInf.getTeacherString() == null)
			{
				newCourseInfValues1.put(KEY_TEACHERS, "");
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}else{
				newCourseInfValues1.put(KEY_TEACHERS, theCourseInf.getTeacherString());
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}
		}
		
		if(cursor1.getInt(4) != theCourseInf.getCredit())
			if(theCourseInf.getCredit() < 0)
			{
				newCourseInfValues1.put(KEY_CREDIT, "");
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}else{
				newCourseInfValues1.put(KEY_CREDIT, theCourseInf.getCredit());
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}
		
		if(cursor1.getString(5) != null)
		{
			if(!(cursor1.getString(5).equals(theCourseInf.getClassNumber())))
				if(theCourseInf.getClassNumber() == null)
				{
					newCourseInfValues1.put(KEY_CLASS_NUMBER, "");
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
				}else{
					newCourseInfValues1.put(KEY_CLASS_NUMBER, theCourseInf.getClassNumber());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
				}
		}else{
			if(theCourseInf.getClassNumber() == null)
			{
				newCourseInfValues1.put(KEY_CLASS_NUMBER, "");
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}else{
				newCourseInfValues1.put(KEY_CLASS_NUMBER, theCourseInf.getClassNumber());
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}
		}
		
		if(cursor1.getString(6) != null)
		{
			if(!(cursor1.getString(6).equals(theCourseInf.getTeachingMaterial())))
				if(theCourseInf.getTeachingMaterial() == null)
				{
					newCourseInfValues1.put(KEY_TEACHING_MATERIAL, "" );
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
				}else{
					newCourseInfValues1.put(KEY_TEACHING_MATERIAL, theCourseInf.getTeachingMaterial());
					db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
				}
		}else{
			if(theCourseInf.getTeachingMaterial() == null)
			{
				newCourseInfValues1.put(KEY_TEACHING_MATERIAL, "" );
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}else{
				newCourseInfValues1.put(KEY_TEACHING_MATERIAL, theCourseInf.getTeachingMaterial());
				db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
			}
		}
		
		if(cursor2.getCount() != 0)//判断这门课的TimeAndAddress是否没有。如外教的时间和地点就没此时ArrayList对象的TimeAndAddress的size就为0，数据库中的courseInf2中就没了这条记录，
		{                          //当没这天记录时就进行插入操作，当有时就要进行比较是否一样，不一样就要进行更改。
			for(int i = 0; i < theCourseInf.getTimeAndAddress().size(); i++){
				cursor2.moveToPosition(i);
				if(cursor2.getInt(2) != theCourseInf.getTimeAndAddress().get(i).getWeek())
					if(theCourseInf.getTimeAndAddress().get(i).getWeek() == 0)
					{
						newCourseInfValues2.put(KEY_WEEK, "");
						db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
					}else{
						newCourseInfValues2.put(KEY_WEEK, theCourseInf.getTimeAndAddress().get(i).getWeek());
						db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
					}
				
				if(cursor2.getInt(3) != theCourseInf.getTimeAndAddress().get(i).getDay())
					if(theCourseInf.getTimeAndAddress().get(i).getDay() == 0)
					{
						newCourseInfValues2.put(KEY_DAY, "");
						db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
					}else{
						newCourseInfValues2.put(KEY_DAY, theCourseInf.getTimeAndAddress().get(i).getDay());
						db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
					}
				
				if(cursor2.getInt(4) != theCourseInf.getTimeAndAddress().get(i).getPeriod())
					if(theCourseInf.getTimeAndAddress().get(i).getPeriod() == 0)
					{
						newCourseInfValues2.put(KEY_PERIOD, "");
						db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
					}else{
						newCourseInfValues2.put(KEY_PERIOD, theCourseInf.getTimeAndAddress().get(i).getPeriod());
						db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
					}
					
				if(cursor2.getString(5) != null)
				{
					if(!(cursor2.getString(5).equals(theCourseInf.getTimeAndAddress().get(i).getAddress())))
						if(theCourseInf.getTimeAndAddress().get(i).getAddress() == null)
						{
							newCourseInfValues2.put(KEY_ADDRESS, "");
							db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
						}else{
							newCourseInfValues2.put(KEY_ADDRESS, theCourseInf.getTimeAndAddress().get(i).getAddress());
							db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
						}
				}else{
					if(theCourseInf.getTimeAndAddress().get(i).getAddress() == null)
					{
						newCourseInfValues2.put(KEY_ADDRESS, "");
						db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
					}else{
						newCourseInfValues2.put(KEY_ADDRESS, theCourseInf.getTimeAndAddress().get(i).getAddress());
						db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2, KEY_VICEID + "=" + (Integer.toString(rowIndex) + (i + 1)), null);
					}
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
     * 从数据库中一次获得courseInf1和courseInf2中的所有记录，也就是所有课程信息包括每门课程的成绩。  
     * @return ArrayList<Course>
     * @throws BitOperateException
     * @throws TimeAndAddressException
     * @throws CourseException
     */
	public ArrayList<Course> getCoursesFromDB() throws BitOperateException, TimeAndAddressException, CourseException{
		 ArrayList<Course> courses = new ArrayList<Course>();
		 Course course = new Course();
		 Cursor cursor1 = db.query(DATABASE_COURSE_TABLE1, null, null, null, null, null, null);
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
				 int newTestScore = cursor1.getInt(9);
				 int newTotalScore = cursor1.getInt(10);
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
						 timeAndAddress.setWeek(newWeek);
						 timeAndAddress.setDay(newDay);
						 timeAndAddress.setPeriod(newPeriod);
						 timeAndAddress.setAddress(newAddress);
						 timeAndAddresses.add(new TimeAndAddress(timeAndAddress));
						 //这里使用了TimeAndAddress的拷贝构造方法进行深拷贝。
					 }
				 }
				 course.setId(newId);
				 course.setCode(newCode);
				 course.setName(newName);
				 course.setTeachers(newTeachers);
				 course.setCredit(newCredit);
				 course.setClassNumber(newClassNumber);
				 course.setTeachingMaterial(newTeachingMaterial);
				 course.setYear(newYear);
				 course.isFirstSemester(reconvert(newIsFirstSemester));
				 course.setTestScore(newTestScore);
				 course.setTotalScore(newTotalScore);
				 course.setKind(newKind);
				 course.setNote(newNote);
				 course.setTimeAndAddresse(timeAndAddresses);
				 courses.add(new Course(course));
				 //这里使用了Course的拷贝构造方法进行了深拷贝。
			 }
		 }
		 return courses;
	 }
	
	/**
	 * 通过参数传递进来课程名称进行一门课程的查询。
	 * @param courseName，string类型
	 * @return Course.
	 * @throws BitOperateException
	 * @throws TimeAndAddressException
	 * @throws CourseException
	 */
	public Course getCourseFromDB(String courseName) throws BitOperateException, TimeAndAddressException, CourseException{
		 Course course = new Course();
		 Cursor cursor1 = db.query(DATABASE_COURSE_TABLE1, null, KEY_NAME + " = '" + courseName + "'", null, null, null, null);
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
			 int newTestScore = cursor1.getInt(9);
			 int newTotalScore = cursor1.getInt(10);
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
					 timeAndAddress.setWeek(newWeek);
					 timeAndAddress.setDay(newDay);
					 timeAndAddress.setPeriod(newPeriod);
					 timeAndAddress.setAddress(newAddress);
					 timeAndAddresses.add(new TimeAndAddress(timeAndAddress));
				 }
			 }
			 course.setId(newId);
			 course.setCode(newCode);
			 course.setName(newName);
			 course.setTeachers(newTeachers);
			 course.setCredit(newCredit);
			 course.setClassNumber(newClassNumber);
			 course.setTeachingMaterial(newTeachingMaterial);
			 course.setYear(newYear);
			 course.isFirstSemester(reconvert(newIsFirstSemester));
			 course.setTestScore(newTestScore);
			 course.setTotalScore(newTotalScore);
			 course.setKind(newKind);
			 course.setNote(newNote);
			 course.setTimeAndAddresse(timeAndAddresses);
		 }
		 return new Course(course);
	}
	
	/**
	 * 一次性从post表中读取所有的通知信息。
	 * @return ArrayList<Post>
	 */
	public ArrayList<Post> getPostsFromDB(){
		ArrayList<Post> posts = new ArrayList<Post>();
		Post post = new Post();
		Cursor cursor = db.query(DATABASE_POST_TABLE, null, null, null, null, null,null);
		
		if((cursor.getCount() == 0) || !cursor.moveToFirst())
		{
			throw new SQLException("No post found from database");
		}else{
			for(int i = 0; i < cursor.getCount(); i++){
				cursor.moveToPosition(i);
				int newSource = cursor.getInt(1);
				String newCategory = cursor.getString(2);
				String newTitle = cursor.getString(3);
				String newUrl = cursor.getString(4);
				String newAuthor = cursor.getString(5);
				long newDate = cursor.getLong(6);
				//date在数据库中的存储类型为integer（integer会根据数据的量级自动改变位数），date是长整型存储的所以要用getLong()，否则会丢失位数。
				
				post.setSource((byte)newSource);
				post.setCategory(newCategory);
				post.setTitle(newTitle);
				post.setUrl(newUrl);
				post.setAuthor(newAuthor);
				post.setDate(new Date(newDate));//new Date(newDate))是把整型的date数据转换成Date类型
				posts.add(new Post(post));
				//这里用了Post的拷贝构造方法进行深拷贝。
			}
		}
		return posts;
	}
	
	/**
	 * 根据source字段来获取post表中的通知
	 * @param theSource，int类型
	 * @return ArrayList<Post>
	 */
	public ArrayList<Post> getPostsFromDB(int theSource){
		ArrayList<Post> posts = new ArrayList<Post>();
		Post post = new Post();
		Cursor cursor = db.query(DATABASE_POST_TABLE, null, KEY_SOURCE + "= " + theSource, null, null, null,null);
		
		if((cursor.getCount() == 0) || !cursor.moveToFirst())
		{
			throw new SQLException("No post found from database");
		}else{
			for(int i = 0; i < cursor.getCount(); i++){
				cursor.moveToPosition(i);
				int newSource = cursor.getInt(1);
				String newCategory = cursor.getString(2);
				String newTitle = cursor.getString(3);
				String newUrl = cursor.getString(4);
				String newAuthor = cursor.getString(5);
				long newDate = cursor.getLong(6);
				
				post.setSource((byte)newSource);
				post.setCategory(newCategory);
				post.setTitle(newTitle);
				post.setUrl(newUrl);
				post.setAuthor(newAuthor);
				post.setDate(new Date(newDate));
				posts.add(new Post(post));
			}
		}
		return posts;
	}
	
	
	/*
	 * 这个方法来自柏杰编写的Post类，这里要用到所以直接搬过来用。作用是吧字符串"yyyy-MM-dd"转换为date类型。
	 */
	private static Date convertToDate(String date) throws ParseException{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.PRC);
		return dateFormat.parse(date);
	}
	
	/**
	 * 根据日期获取post表中的指定日期的通知。
	 * @param date，String类型"yyyy-MM-dd"
	 * @return ArrayList<Post>
	 * @throws ParseException
	 */
	public ArrayList<Post> getPostsFromDB(String date) throws ParseException{
		ArrayList<Post> posts = new ArrayList<Post>();
		Post post = new Post();
		Cursor cursor = db.query(DATABASE_POST_TABLE, null, KEY_DATE + "=" + convertToDate(date).getTime(), null, null, null,null);
		//convertToDate(date).getTime()先转化为date类型再转化为长整型。
		
		if((cursor.getCount() == 0) || !cursor.moveToFirst())
		{
			throw new SQLException("No post found from database");
		}else{
			for(int i = 0; i < cursor.getCount(); i++){
				cursor.moveToPosition(i);
				int newSource = cursor.getInt(1);
				String newCategory = cursor.getString(2);
				String newTitle = cursor.getString(3);
				String newUrl = cursor.getString(4);
				String newAuthor = cursor.getString(5);
				long newDate = cursor.getLong(6);
				
				post.setSource((byte)newSource);
				post.setCategory(newCategory);
				post.setTitle(newTitle);
				post.setUrl(newUrl);
				post.setAuthor(newAuthor);
				post.setDate(new Date(newDate));
				posts.add(new Post(post));
			}
		}
		return posts;
	}
	 	
}
