/**
 * 
 */
package util.webpage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <strong>Note:</strong>大部分用于设置的方法返回this，做Builder
 * @author Bai Jie
 *
 */
public class Post {
	public static final class SOURCES{
		public static final int WEBSITE_OF_TEACHING_AFFAIRS = 1;
		public static final int WEBSITE_OF_SCCE = 2;
		public static final int STUDENT_WEBSITE_OF_SCCE = 3;
		public static final String NOTICES_IN_SCCE_URL = "http://59.67.152.3/wnoticemore.aspx";
		public static final String NEWS_IN_SCCE_URL = "http://59.67.152.3/wnewmore.aspx";
	}
	public static final class CATEGORYS{
		public static final String TEACHING_AFFAIRS_NOTICES = "重要通知";
		public static final String TEACHING_AFFAIRS_COURSE_SELECTION = "选课相关通知";
		public static final String TEACHING_AFFAIRS_NEWS = "教务快讯";
		public static final String TEACHING_AFFAIRS_NOTICES_ON_TEST = "考试相关通知";
		public static final String TEACHING_AFFAIRS_CET = "大学英语四六级考试";
		public static final String[] CATEGORYS_IN_TEACHING_AFFAIRS_WEBSITE = new String[]{
			TEACHING_AFFAIRS_NOTICES, TEACHING_AFFAIRS_COURSE_SELECTION, TEACHING_AFFAIRS_NEWS, 
			TEACHING_AFFAIRS_NOTICES_ON_TEST, TEACHING_AFFAIRS_CET,"考试相关规定","选课相关规定","成绩学籍相关通知",
			"成绩相关规定","学籍相关规定","教学研究与评价相关通知","专业建设","培养计划","课程建设","教材建设","教学评价","教学研究",
			"辅修专业","学科竞赛","实践教学相关通知","实验室建设","实验教学相关规定","实习教学相关规定","第二校园","毕业设计相关规定",
			"课程设计专业设计","仪器设备","投资规划相关规定","基本教学管理文件","学籍与考试管理文件","教学建设文件","实践教学管理文件",
			"教学质量监控文件","表格下载"
		};
		
		public static final String SCCE_NOTICE_TEACHING = "教学通知";
		public static final String SCCE_NOTICE_RESEARCH = "科研通知";
		public static final String SCCE_NOTICE_STUDENT = "学生通知";
		public static final String SCCE_NOTICE_OFFICE = "办公通知";
		public static final String SCCE_NOTICE_UNION = "工会通知";
		public static final String SCCE_NEW_SCHOOL = "学校新闻";
		public static final String SCCE_NEW_COLLEGE = "学院新闻";
		public static final String SCCE_NEW_DEPARTMENT = "系内新闻";
		
		public static final String SCCE_STUDENT_NEWS = "新闻中心";
		public static final String SCCE_STUDENT_NOTICES = "通知公告";
		public static final String SCCE_STUDENT_UNION = "院学生会";
		public static final String SCCE_STUDENT_EMPLOYMENT = "招聘快讯";
		public static final String SCCE_STUDENT_YOUTH_LEAGUE = "分团时讯";
		public static final String SCCE_STUDENT_DOWNLOADS = "下载中心";
		public static final String SCCE_STUDENT_JOBS = "岗位信息";
		public static final String[] IN_STUDENT_WEBSITE_OF_SCCE = new String[]{
			SCCE_STUDENT_NOTICES, SCCE_STUDENT_NEWS, SCCE_STUDENT_UNION, SCCE_STUDENT_EMPLOYMENT
			, SCCE_STUDENT_DOWNLOADS, SCCE_STUDENT_YOUTH_LEAGUE, SCCE_STUDENT_JOBS
		};
	}
	

	int source;
	String category;
	String title;
	String url;
	String author;
	Date date;

	public Post() {
		super();
		source = -1; 
		title = url = category = null;
		date = null;
	}
	public Post(int source, String category, String title, String url, String author, String date) {
		this();
		this.source = source;
		this.category = category;
		this.title = title;
		this.url = url;
		this.author = author;
		try {
			setDate(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			System.out.println("Can't parse date normally. "+e.getMessage());
			e.printStackTrace();
		}
	}
	/**
	 * @return the source
	 */
	public int getSource() {
		return source;
	}
	/**
	 * 以字符串形式返回通知源
	 * @return 类似“教务处”的字符串
	 */
	public String getSourceString(){
		switch(source){
		case SOURCES.WEBSITE_OF_TEACHING_AFFAIRS:return "教务处";
		case SOURCES.WEBSITE_OF_SCCE:return "计算机与通信工程学院网站";
		case SOURCES.STUDENT_WEBSITE_OF_SCCE:return "计算机与通信工程学院学生网站";
		default:return "未知";
		}
	}
	/**
	 * @param source the source to set
	 */
	public Post setSource(int source) {
		this.source = source;
		return this;
	}
	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}
	/**
	 * @param category the category to set
	 */
	public Post setCategory(String category) {
		this.category = category;
		return this;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public Post setTitle(String title) {
		this.title = title;
		return this;
	}
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public Post setUrl(String url) {
		this.url = url;
		return this;
	}
	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}
	/**
	 * @param author the author to set
	 */
	public Post setAuthor(String author) {
		this.author = author;
		return this;
	}
	/**
	 * 对返回时间的修改不会影响本对象（只读）
	 * @return the date
	 */
	public Date getDate() {
		return (Date)date.clone();
	}
	/**
	 * @param date the date to set
	 */
	public Post setDate(Date date) {
		this.date = date;
		return this;
	}
	/**
	 * 以字符串显示日期，用指定的分隔符（YYYY(delimiter)MM(delimiter)DD）
	 * @param delimiter 日期分隔符
	 * @return 类似2012(delimiter)07(delimiter)16的字符串
	 */
	public String getDateString(String delimiter){
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("yyyy'"+delimiter+"'MM'"+delimiter+"'dd", Locale.PRC);
		return dateFormat.format(this.date);
	}
	/**
	 * 以字符串显示日期(YYYY-MM-DD)
	 * @return 类似2012-07-16的字符串
	 */
	public String getDateString(){
		return getDateString("-");
	}
	/**
	 * 设置日期
	 * @param year 年
	 * @param month 月
	 * @param date 日
	 * @return 返回this（Builder）
	 */
	public Post setDate(int year, int month, int date){
		this.date = convertToDate(year, month, date);
		return this;
	}
	public static Date convertToDate(int year, int month, int date){
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+08"), Locale.PRC);
		calendar.clear();
		calendar.set(year, month-1, date);
		return calendar.getTime();
	}
	/**
	 * 以字符串(YYYY-MM-DD格式)设置日期
	 * @param date YYYY-MM-DD格式的字符串，例如2012-07-16
	 * @return 返回this（Builder）
	 * @throws ParseException if the beginning of the specified string cannot be parsed.
	 */
	public Post setDate(String date) throws ParseException{
		this.date = convertToDate(date);
		return this;
	}
	public static Date convertToDate(String date) throws ParseException{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.PRC);
		return dateFormat.parse(date);
	}
	
	public String toString(){
		return getSourceString()+"\t"+getCategory()+"\t"+getTitle()+"\t"+getUrl()+"\t"
				+getAuthor()+"\t"+getDateString();
	}
}
