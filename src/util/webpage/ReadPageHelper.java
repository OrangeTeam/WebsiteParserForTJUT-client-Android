package util.webpage;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ReadPageHelper implements Cloneable{
	private OnReadPageListener listener = null;
	
	/**网络连接的超时时间，单位milliseconds*/
	private int timeout;
	private String userName, password, charset, charsetForParsePostsFromSCCE;
	private boolean isNewUser;
	private Cookie teachingAffairsSession, SCCESession;
	/**保留的session的过期时间，单位milliseconds*/
	private int expire;

	public ReadPageHelper(){
		this.timeout = 12000;
		this.userName = this.password = "";
		this.charset = null;
		this.charsetForParsePostsFromSCCE = null;
		this.isNewUser = false;
		this.teachingAffairsSession = this.SCCESession = null;
		this.expire = 60 * 60 *1000;//	1 hour 
	}
	public ReadPageHelper(String userName, String password){
		this();
		setUser(userName, password);
	}
	public ReadPageHelper(String userName, String password, String pageCharset){
		this(userName, password);
		this.charset = pageCharset;
	}
	public ReadPageHelper(String userName, String password, String pageCharset, int timeout){
		this(userName, password, pageCharset);
		setTimeout(timeout);
	}
	
	/**
	 * @return the listener
	 */
	public OnReadPageListener getListener() {
		return listener;
	}
	/**
	 * @param listener the listener to set
	 */
	public void setListener(OnReadPageListener listener) {
		this.listener = listener;
	}
	/**
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}
	/**
	 * 设置 网络连接的超时时间，单位milliseconds
	 * @param timeout 网络连接的超时时间，单位milliseconds
	 * @return 参数大于0返回真；参数小于等于0忽略本次调用，直接返回假
	 */
	public boolean setTimeout(int timeout) {
		if(timeout<=0)
			return false;
		this.timeout = timeout;
		return true;
	}
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * 设置用户名、密码<br />
	 * <strong>注意：</strong>参数不能设为null，否则抛出NullPointerException
	 * @param userName the user name to set
	 * @param password the password to set
	 */
	public void setUser(String userName, String password) {
		if(userName==null || password == null)
			throw new NullPointerException("Encounter null when set user.");
		this.userName = userName;
		this.password = password;
		this.isNewUser = true;
	}
	/**
	 * @return the teachingAffairsSession
	 */
	public Cookie getTeachingAffairsSession() {
		return teachingAffairsSession;
	}
	/**
	 * @param teachingAffairsSession the teachingAffairsSession to set
	 */
	public void setTeachingAffairsSession(String sessionCookieKey, String sessionCookieValue) {
		this.teachingAffairsSession = new Cookie(sessionCookieKey, sessionCookieValue);
	}
	/**
	 * @return the charset
	 */
	public String getCharset() {
		return charset;
	}
	/**
	 * @param charset the charset to set
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}
	/**
	 * @return the charsetForParsePostsFromSCCE
	 */
	public String getCharsetForParsePostsFromSCCE() {
		return charsetForParsePostsFromSCCE;
	}
	/**
	 * @param charsetForParsePostsFromSCCE the charsetForParsePostsFromSCCE to set
	 */
	public void setCharsetForParsePostsFromSCCE(String charsetForParsePostsFromSCCE) {
		this.charsetForParsePostsFromSCCE = charsetForParsePostsFromSCCE;
	}
	/**
	 * do login<br />登录
	 * @param loginPageURL send login request to this page 向此网址发送登录请求 
	 * @return true for success, false for failure 成功登录返回真，失败返回假
	 * @throws IOException encounter error when make post request 进行post请求时遇到error
	 */
	public boolean doLogin(String loginPageURL) throws IOException{
		if(!isNewUser && teachingAffairsSession!=null 
				&& teachingAffairsSession.isModifiedWithIn(expire))
			return true;
		Response res = Jsoup
				.connect(loginPageURL).timeout(timeout).followRedirects(false)
				.data("name",userName,"pswd", password)
				.method(Method.POST).execute();
		if(listener != null)
			listener.onRequest(loginPageURL, res.statusCode(), res.statusMessage(), res.bodyAsBytes().length);
		if(res.statusCode() != 302)
			return false;
		this.teachingAffairsSession = getCookie1FromMap(res.cookies());
		if(this.teachingAffairsSession == null)
			return false;
		return true;
	}
	/**
	 * do login with default login page<br />登录,使用默认登录页
	 * @return true for success, false for failure
	 * @throws IOException
	 */
	public boolean doLogin() throws IOException{
		return doLogin(Constant.url.LOGIN_PAGE);
	}
	/**
	 * 准备
	 * @param preparePageURL
	 * @param charset
	 * @return
	 * @throws IOException
	 * @see {@link #request(String, Method, String)}
	 */
	public boolean prepareToParsePostsFromSCCE(String preparePageURL) throws IOException{
		if(SCCESession!=null && SCCESession.isModifiedWithIn(expire))
			return true;
		this.SCCESession = getCookie1FromMap(request(preparePageURL, Method.GET, null).cookies());
		if(this.SCCESession == null)
			return false;
		return true;
	}
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean prepareToParsePostsFromSCCE() throws IOException{
		return prepareToParsePostsFromSCCE(Constant.url.PREPARE_PAGE_FOR_GET_POSTS_FROM_SCCE);
	}
	/**
	 * 取出cookies中的第一个Cookie
	 * @return 成功返回Cookie；失败返回null
	 */
	private Cookie getCookie1FromMap(Map<String, String> cookies){
		if(cookies == null)
			return null;
		String key = (String)cookies.keySet().toArray()[0];
		if(key == null || key.length() == 0)
			return null;
		String value = cookies.get(key);
		if(value == null || value.length() == 0)
			return null;
		return new Cookie(key, value);
	}
	/**
	 * read page by GET method
	 * @param url page's URL you want to read from
	 * @return web page's content in String
	 * @throws IOException
	 */
	public String get(String url) throws IOException{
		return getWithDocument(url).outerHtml();
	}
	/**
	 * @see {@link #request(String, Method, String)}
	 */
	public Document getWithDocument(String url) throws IOException{
		return getWithDocument(url, this.charset);
	}
	/**
	 * @see {@link #request(String, Method, String)}
	 */
	public Document getWithDocumentForParsePostsFromSCCE(String url) throws IOException{
		return getWithDocument(url, this.charsetForParsePostsFromSCCE);
	}
	/**
	 * @see {@link #request(String, Method, String)}
	 */
	public Document getWithDocument(String url, String charset) throws IOException{
		return request(url, Method.GET, charset).parse();
	}
	/**
	 * read page by POST method
	 * @param url page's URL you want to read from
	 * @return web page's content in String
	 * @throws IOException
	 * @see {@link #request(String, Method, String)}
	 */
	public String post(String url) throws IOException{
		return request(url, Method.POST, null).parse().outerHtml();
	}
	/**
	 * Execute the request.
	 * @param url URL to connect to
	 * @param method {@link Method}
	 * @param charset set character set to be used
	 * @return a response object
	 * @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
	 * @throws org.jsoup.HttpStatusException if the response is not OK and HTTP response errors are not ignored
	 * @throws org.jsoup.UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
	 * @throws java.net.SocketTimeoutException if the connection times out
	 * @throws IOException on error
	 */
	private Response request(String url, Method method, String charset) throws IOException{
		Connection conn = Jsoup.connect(url).timeout(timeout).followRedirects(false);
		if(teachingAffairsSession != null && !teachingAffairsSession.isEmpty())
			conn.cookie(teachingAffairsSession.cookieKey, teachingAffairsSession.cookieValue);
		if(SCCESession != null && !SCCESession.isEmpty())
			conn.cookie(SCCESession.cookieKey, SCCESession.cookieValue);
		
		Response response = conn.method(method).execute();
		if(charset != null)
			response.charset(charset);
		if(listener != null){
			listener.onRequest(url, response.statusCode(), response.statusMessage(), response.bodyAsBytes().length);
		}
		return response;
	}
	/** 修剪头尾不可见符，包括\s\u00a0\u3000 */
	public static String trim(String src){
		if(src!=null)
			return src.replaceAll("(^[\\s\u00a0\u3000])|([\\s\u00a0\u3000]$)", "");
		else
			return null;
	}
	/** 删除所有不可见字符，包括\s\u00a0\u3000 */
	public static String deleteSpace(String src){
    	if(src!=null)
    		return src.replaceAll("[\\s\u00a0\u3000]", "");
    		//	ideographic space	0x3000	&#12288(HTML);
    	else
    		return null;
    }
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ReadPageHelper clone() throws CloneNotSupportedException {
		ReadPageHelper clone = (ReadPageHelper) super.clone();
		if(this.SCCESession != null)
			clone.SCCESession = this.SCCESession.clone();
		else
			clone.SCCESession = null;
		if(this.teachingAffairsSession != null)
			clone.teachingAffairsSession = this.teachingAffairsSession.clone();
		else
			clone.teachingAffairsSession = null;
		return clone;
	}

	public static class Cookie implements Cloneable {
		private String cookieKey;
		private String cookieValue;
		private Date modifiedTime;
		
		public Cookie(){
			cookieKey = cookieValue = "";
			modifiedTime = new Date(0);
		}
		public Cookie(String cookieKey, String cookieValue) {
			this();
			setCookie(cookieKey, cookieValue);
		}
		/**
		 * @return the cookieKey
		 */
		public String getCookieKey() {
			return cookieKey;
		}
		/**
		 * @return the cookieValue
		 */
		public String getCookieValue() {
			return cookieValue;
		}
		/**
		 * @param cookieKey the cookie's key to set
		 * @param cookieValue the cookie's value to set
		 */
		public void setCookie(String cookieKey, String cookieValue) {
			this.cookieKey = cookieKey;
			this.cookieValue = cookieValue;
			modifiedTime.setTime(System.currentTimeMillis());
		}
		/**
		 * @return the modifiedTime
		 */
		public Date getModifiedTime() {
			return (Date)modifiedTime.clone();
		}
		public String getModifiedTimeString(){
			return DateFormat.getInstance().format(modifiedTime);
		}
		/**
		 * 上次修改时间是否在距现在指定时间（毫秒）内
		 * @param milliseconds 测试标准。单位：毫秒
		 * @return 在milliseconds毫秒内，返回true；在milliseconds毫秒外，返回false
		 */
		public boolean isModifiedWithIn(long milliseconds){
			return (System.currentTimeMillis()-modifiedTime.getTime() <= milliseconds);
		}
		public boolean isEmpty(){
			return (cookieKey.length() == 0&&cookieValue.length() == 0);
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#clone()
		 */
		@Override
		public Cookie clone() throws CloneNotSupportedException {
			Cookie clone = (Cookie) super.clone();
			clone.modifiedTime = (Date) this.modifiedTime.clone();
			return clone;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Cookie [cookieKey=" + cookieKey + ", cookieValue="
					+ cookieValue +"modifiedTime="+getModifiedTimeString()+ "]";
		}
	}
	
	public static interface OnReadPageListener{
		/**
		 * 当（用Get或Post等方法）读取一个页面后
		 * @param url
		 * @param statusCode
		 * @param statusMessage
		 * @param pageSize 单位：字节
		 */
		public void onRequest(String url, int statusCode, String statusMessage, int pageSize);
	}
}
