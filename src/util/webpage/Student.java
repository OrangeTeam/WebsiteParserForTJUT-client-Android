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
 * @author Bai Jie
 *
 */
public class Student implements Cloneable {
	/**学号*/
	String number;
	/**姓名*/
	String name;
	/**性别 。true表示男，false表示女，null表示未知*/
	Boolean isMale;
	/**生日*/
	Date birthday;
	/**入学时间*/
	Date admissionTime;
	/**学制，单位；年。如4（年）*/
	byte academicPeriod;
	/**学院名称*/
	String schoolName;
	/**专业名称*/
	String majorName;
	/**班级名称*/
	String className;

	public Student(){
		super();
		number = name = schoolName = majorName = className = null;
		isMale = null;
		academicPeriod = -1;
		birthday = new Date(0);
		admissionTime = new Date(0);
	}
	public Student(String number, String name){
		this();
		this.number = number;
		this.name = name;
	}
	public Student(String number, String name, String schoolName, String majorName, String className){
		this(number, name);
		this.schoolName = schoolName;
		this.majorName = majorName;
		this.className = className;
	}
	public Student(String number, String name, Boolean isMale, Date birthday, Date admissionTime, 
			int academicPeriod, String schoolName, String majorName, String className){
		this(number, name, schoolName, majorName, className);
		this.isMale = isMale;
		setBirthday(birthday).setAdmissionTime(admissionTime);
		try {
			setAcademicPeriod(academicPeriod);
		} catch (StudentException e) {
			// skip setAdmissionTime if encounter exception
			e.printStackTrace();
		}
	}
	public Student(Student src){
		this(src.number, src.name, src.schoolName, src.majorName, src.className);
		this.isMale = src.isMale;
		setBirthday(src.birthday).setAdmissionTime(src.admissionTime);
		this.academicPeriod = src.academicPeriod;
	}
	/**
	 * @return 学号
	 */
	public String getNumber() {
		return number;
	}
	/**
	 * @param number 学号
	 */
	public Student setNumber(String number) {
		this.number = number;
		return this;
	}
	/**
	 * @return 姓名
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name 姓名
	 */
	public Student setName(String name) {
		this.name = name;
		return this;
	}
	/**
	 * @return 性别 。true表示男，false表示女，null表示未知
	 */
	public Boolean isMale() {
		return isMale;
	}
	/**
	 * 取得性别
	 * @return 男 or 女 or 不明
	 */
	public String getGender(){
		if(this.isMale == null)
			return "不明";
		else if(this.isMale)
			return "男";
		else
			return "女";
	}
	/**
	 * @param isMale 性别 。true表示男，false表示女，null表示未知
	 */
	public Student setIsMale(Boolean isMale) {
		this.isMale = isMale;
		return this;
	}
	/**
	 * @return 生日
	 */
	public Date getBirthday() {
		return (Date) birthday.clone();
	}
	/**
	 * 取得生日字符串
	 * @return 生日。类似2012-07-16的字符串 
	 * @see #getDateString(Date date)
	 * @see #getDateString(Date date, String delimiter)
	 */
	public String getBirthdayString(){
		return getDateString(this.birthday);
	}
	/**
	 * 设置生日
	 * @param birthday 生日
	 */
	public Student setBirthday(Date birthday) {
		this.birthday = (Date) birthday.clone();
		return this;
	}
	/**
	 * 设置生日。（参数依据中华人民共和国地区设置）
	 * @param birthday 生日。类似2012-07-16或2012-07-16的字符串
	 * @throws ParseException if the beginning of the specified string cannot be parsed.
	 * @see #convertToDate(String date)
	 */
	public Student setBirthday(String birthday) throws ParseException{
		this.birthday = convertToDate(birthday);
		return this;
	}
	/**
	 * 设置生日。（参数依据东8时区及中华人民共和国区域设置）
	 * @param year 年
	 * @param month 月
	 * @param date 日
	 * @see #convertToDate(int year, int month, int date)
	 */
	public Student setBirthday(int year, int month, int date){
		this.birthday = convertToDate(year, month, date);
		return this;
	}
	/**
	 * 设置入学时间
	 * @return 入学时间
	 */
	public Date getAdmissionTime() {
		return (Date) admissionTime.clone();
	}
	/**
	 * 取得入学时间。
	 * @return 入学时间。类似2012-07-16的字符串
	 * @see #getDateString(Date date)
	 * @see #getDateString(Date date, String delimiter)
	 */
	public String getAdmissionTimeString(){
		return getDateString(this.admissionTime);
	}
	/**
	 * 设置入学时间。
	 * @param admissionTime 入学时间
	 */
	public Student setAdmissionTime(Date admissionTime) {
		this.admissionTime = (Date) admissionTime.clone();
		return this;
	}
	/**
	 * 设置入学时间。（参数依据中华人民共和国地区设置）
	 * @param admissionTime 入学时间。类似2012-07-16或2012-07-16的字符串
	 * @throws ParseException if the beginning of the specified string cannot be parsed.
	 * @see #convertToDate(String date)
	 */
	public Student setAdmissionTime(String admissionTime) throws ParseException{
		this.admissionTime = convertToDate(admissionTime);
		return this;
	}
	/**
	 * 设置入学时间。（参数依据东8时区及中华人民共和国区域设置）
	 * @param year 年
	 * @param month 月
	 * @param date 日
	 * @see #convertToDate(int year, int month, int date)
	 */
	public Student setAdmissionTime(int year, int month, int date){
		this.admissionTime = convertToDate(year, month, date);
		return this;
	}
	/**
	 * @return 学制，如4（单位：年）
	 */
	public byte getAcademicPeriod() {
		return academicPeriod;
	}
	/**
	 * @param academicPeriod 学制，如4（单位：年）
	 * @throws StudentException academicPeriod<=0 || academicPeriod>Byte.MAX_VALUE
	 */
	public Student setAcademicPeriod(int academicPeriod) throws StudentException {
		if(academicPeriod<=0 || academicPeriod>Byte.MAX_VALUE)
			throw new StudentException("Beyond a reasonable range: academicPeriod "+academicPeriod);
		this.academicPeriod = (byte) academicPeriod;
		return this;
	}
	/**
	 * @return 学院名称
	 */
	public String getSchoolName() {
		return schoolName;
	}
	/**
	 * @param schoolName 学院名称
	 */
	public Student setSchoolName(String schoolName) {
		this.schoolName = schoolName;
		return this;
	}
	/**
	 * @return 专业名称
	 */
	public String getMajorName() {
		return majorName;
	}
	/**
	 * @param majorName 专业名称
	 */
	public Student setMajorName(String majorName) {
		this.majorName = majorName;
		return this;
	}
	/**
	 * @return 班级名称，如“10计算机(合作)-6班”
	 */
	public String getClassName() {
		return className;
	}
	/**
	 * @param className 班级名称，如“10计算机(合作)-6班”
	 */
	public Student setClassName(String className) {
		this.className = className;
		return this;
	}
	/**
	 * @return 免冠头像照片的URL。如果没有设置{@link #number}，返回null
	 */
	public String getUrlOfFacedPhoto(){
		if(this.number == null)
			return null;
		else
			return "http://59.67.148.66/cet46/photo/apply/"+number+".jpg";
	}
	/**
	 * 把类似2012-07-16或2012-7-16的字符串，依据中华人民共和国地区设置，解析为Date
	 * @param date 要被解析的日期。类似2012-07-16或2012-07-16的字符串
	 * @return 日期解析后的Date对象
	 * @throws ParseException if the beginning of the specified string cannot be parsed.
	 * @see SimpleDateFormat
	 * @see Date
	 */
	public static Date convertToDate(String date) throws ParseException{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.PRC);
		return dateFormat.parse(date);
	}
	/**
	 * 依据指定年、月、日，以东8时区及中华人民共和国区域设置，生成相应Date对象
	 * @param year 年
	 * @param month 月
	 * @param date 日
	 * @return 对应日期的Date对象
	 * @see Calendar
	 * @see Date
	 */
	public static Date convertToDate(int year, int month, int date){
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+08"), Locale.PRC);
		calendar.clear();
		calendar.set(year, month-1, date);
		return calendar.getTime();
	}
	/**
	 * 以字符串显示日期，用指定的分隔符（YYYY(delimiter)MM(delimiter)DD）
	 * @param date 日期
	 * @param delimiter 日期分隔符
	 * @return 类似2012(delimiter)07(delimiter)16的字符串
	 */
	public String getDateString(Date date, String delimiter){
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("yyyy'"+delimiter+"'MM'"+delimiter+"'dd", Locale.PRC);
		return dateFormat.format(date);
	}
	/**
	 * 以字符串显示日期(YYYY-MM-DD)
	 * @return 类似2012-07-16的字符串
	 */
	public String getDateString(Date date){
		return getDateString(date, "-");
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return number+"\t"+name+"\t"+getGender()+"\t"+getBirthdayString()+"\t"+getAdmissionTimeString()
				+"\t"+academicPeriod+"\t"+schoolName+"\t"+majorName+"\t"+className;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Student clone() throws CloneNotSupportedException {
		Student clone = (Student) super.clone();
		clone.setBirthday(this.birthday);
		clone.setAdmissionTime(this.admissionTime);
		return clone;
	}



	public static class StudentException extends Exception{
		private static final long serialVersionUID = 1988292879191685182L;
		public StudentException(){
			super("Encounter exception in Student.");
		}
		public StudentException(String m){
			super(m);
		}
		public StudentException(String message, Throwable cause) {
			super(message, cause);
		}
		public StudentException(Throwable cause){
			super("Encounter exception in Student.", cause);
		}
	}
}
