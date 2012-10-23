package util;

import java.util.Date;
import java.util.List;

import util.webpage.Post;

public interface GetterInterface {
	/**
	 * 测试用：返回输入参数。
	 * @param in 传给远程的测试字符串
	 * @return 返回"echo:"+in
	 */
	public String echo(String in);
	/**
	 * 返回[start, end)内的Posts，包括start，<strong>不</strong>包括end
	 * @param start 起始时间，包括start
	 * @param end 终止时间，不包括end
	 * @param max 返回结果的最大条数
	 * @return 符合条件的posts
	 */
	public List<Post> getPosts(Date start, Date end, int max);
}
