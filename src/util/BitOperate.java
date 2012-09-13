/**
 * 
 */
package util;


/**
 * @author Bai Jie
 *
 */
public class BitOperate {
	/** 把original的第location位设为1.location从低位到高位,从0起 
	 * @throws BitOperateException 
	 */
    public static int add1onCertainBit(int original,int location) throws BitOperateException{
    	if(location<0)
    		throw new BitOperateException("negative bit position.", 
    				BitOperateException.NEGATIVE_POSITION);
    	if(location>31)
    		throw new BitOperateException("too large bit position.(>31)", 
    				BitOperateException.TOO_LARGE_POSITON);
    	return original | (1<<location); 
    }
    public static int add1onCertainBit(int original,int[] locations) throws BitOperateException{
    	int result = original;
    	for(int location:locations)
    		result = add1onCertainBit(result, location);
    	return result;
    }
    /** 把original的第location位设为0.location从低位到高位,从0起 
     * @throws BitOperateException */
    public static int add0onCertainBit(int original, int location) throws BitOperateException{
    	if(location<0)
    		throw new BitOperateException("negative bit position.", 
    				BitOperateException.NEGATIVE_POSITION);
    	if(location>31)
    		throw new BitOperateException("too large bit position.(>31)", 
    				BitOperateException.TOO_LARGE_POSITON);
    	int mask = ~(1<<location);
    	return original & mask;
    }
    public static int add0onCertainBit(int original, int[] locations) throws BitOperateException{
    	int result = original;
    	for(int location:locations)
    		result = add0onCertainBit(result, location);
    	return result;
    }
    /**
     * 把original的start到end位设置为1，根据oddOrEven，只设置奇或偶数位
     * @param original 基数
     * @param start 起始位置（第start位也设为1）
     * @param end 结束位置（第end位也设为1）
     * @param oddOrEven null表示连续（不分奇偶位），true表仅设奇数位，false只设偶数位
     * @throws BitOperateException 
     */
    public static int add1onRange(int original, int start, int end, Boolean oddOrEven) throws BitOperateException{
    	if(start<0 || end<0)
    		throw new BitOperateException("negative bit position.", 
    				BitOperateException.NEGATIVE_POSITION);
    	if(start>31 || end>31)
    		throw new BitOperateException("too large bit position.(>31)", 
    				BitOperateException.TOO_LARGE_POSITON);
    	if(start>end)
    		throw new BitOperateException("start shouldn't have been greater than end.", 
    				BitOperateException.START_GREATER_THAN_END);
    	int mask = (~(~0<<(end-start+1)))<<start;	//①全1左移end-start+1位 ②取反 ③左移start位
    	if(oddOrEven != null){
    		if(oddOrEven.booleanValue())
    			mask &= 0xAAAAAAAA;	//1010 1010 1010 1010 1010 1010 1010 1010
    		else
    			mask &= 0x55555555;	//0101 0101 0101 0101 0101 0101 0101 0101
    	}
    	return original | mask;
    }
    /**
     * 把original的start到end位设置为1
     * @param original 基数
     * @param start 起始位置（第start位也设为1）
     * @param end 结束为止（第end位也设为1）
     * @throws BitOperateException 
     */
    public static int add1onRange(int original, int start, int end) throws BitOperateException{
    	return add1onRange(original, start, end, null);
    }
    /**
     * 在指定位置设置1，识别类似2,4,6,8,10,12,14,18 01-21的字符串<br />
     * <strong>注意：</strong>2,4,6,8,10,12,14,18 01- 21（注意最后一个空格）无效
     * @param original 基数
     * @param positionStr 指定位置，类似2,4,6,8,10,12,14,18 01-21的字符串
     * @param oddOrEven null表示连续（不分奇偶位），true表仅设奇数位，false只设偶数位
     * @throws BitOperateException 遇到非法参数抛出异常
     * @precondition positionStr 不包含[\\d\\s\u00a0\u3000;；,，\\-－\u2013\u2014\u2015]以外的任何内容
     */
    public static int add1onCertainBit(int original, String positionStr, Boolean oddOrEven) throws BitOperateException{
		int result = original;
		if(positionStr.matches(".*[^\\d\\s\u00a0\u3000;；,，\\-－\u2013\u2014\u2015].*"))
			throw new BitOperateException("Unknown character in parameter.", 
					BitOperateException.UNKNOWN_NOTATION);
		String[] positions = positionStr.split("[\\s\u00a0\u3000;；,，]");//根据这些分割
		for(String position:positions){
			if(position.length()==0)
				continue;
			if(position.matches(".*[\\-－\u2013\u2014\u2015].*")){
				String[] pair = position.split("[\\-－\u2013\u2014\u2015]+");
				if(pair.length!=2)
					throw new BitOperateException("Don't match token like \"3-6\".", 
							BitOperateException.UNKNOWN_TOKEN);
				else{
					if(pair[0].length() == 0)	//negative number 负数都到此分支
						throw new BitOperateException("Encounter negative number.", 
								BitOperateException.NEGATIVE_POSITION);
					int start = Integer.parseInt(pair[0]);
					int end = Integer.parseInt(pair[1]);
					result = add1onRange(result, start, end);
				}
			}
			else{
				int weekNumber = Integer.parseInt(position);
				result = add1onCertainBit(result, weekNumber);
			}
		}
		if(oddOrEven != null){
    		if(oddOrEven.booleanValue())
    			result &= 0xAAAAAAAA;	//1010 1010 1010 1010 1010 1010 1010 1010
    		else
    			result &= 0x55555555;	//0101 0101 0101 0101 0101 0101 0101 0101
    	}
		return result;
	}
    /**
     * 在指定位置设置1，识别类似2,4,6,8,10,12,14,18 01-21的字符串<br />
     * <strong>注意：</strong>2,4,6,8,10,12,14,18 01- 21（注意最后一个空格）无效
     * @param original 基数
     * @param positionStr 指定位置，类似2,4,6,8,10,12,14,18 01-21的字符串
     * @throws BitOperateException 遇到非法参数抛出异常
     * @precondition positionStr 不包含[\\d\\s\u00a0\u3000;；,，\\-－\u2013\u2014\u2015]以外的任何内容
     */
    public static int add1onCertainBit(int original, String positionStr) throws BitOperateException{
    	return add1onCertainBit(original, positionStr, null);
    }
    /**
     * 显示converted中各个值为1的bit的位置，与add1onCertainBit(int original, String positionStr)相反
     * @param converted 要被检测（转换）的数
     * @return 类似2,4,6,8,10,12,14,18-21的字符串
     */
    public static String convertIntToString(int converted){
    	StringBuilder sb = new StringBuilder();
    	int start = -1, index;
    	for(index=0;index<Integer.SIZE;index++){
    		try {
				if(is1onCertainBit(converted, index)){
					if(start<0)
						start = index;
				}
				else if(start>=0){
					if(start == index-1)
						sb.append(start+",");
					else
						sb.append(start+"-"+(index-1)+",");
					start = -1;
				}
			} catch (BitOperateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	if(start>0){
			if(start == index-1)
				sb.append(start+",");
			else
				sb.append(start+"-"+(index-1)+",");
		}
    	if(sb.length() == 0)
    		return "(None)";
    	else
    		return sb.substring(0, sb.length()-1);
    }
    /**
     * 检测某一bit是否是1，是返回true
     * @param tested 被检测数
     * @param position 被检测位置
     * @return 是1返回true，是0返回false
     * @throws BitOperateException when position<0 || position>31
     */
    public static boolean is1onCertainBit(int tested, int position) throws BitOperateException{
    	if(position<0)
    		throw new BitOperateException("negative bit position.", 
    				BitOperateException.NEGATIVE_POSITION);
    	if(position>31)
    		throw new BitOperateException("too large bit position.(>31)", 
    				BitOperateException.TOO_LARGE_POSITON);
    	return ( tested & (1<<position) ) != 0; 
    }
    /**
     * 检测tested的position及以上位(bit)是否有1
     * @param tested 被检数
     * @param position 高于position位，包括第position位，例如5表示5、6、7...位
     * @throws BitOperateException 参数超出0-31
     */
    public static boolean has1onBitsHigherThan(int tested, int position) throws BitOperateException{
    	if(position<0)
    		throw new BitOperateException("negative bit position.", 
    				BitOperateException.NEGATIVE_POSITION);
    	if(position>31)
    		throw new BitOperateException("too large bit position.(>31)", 
    				BitOperateException.TOO_LARGE_POSITON);
    	return ( tested & (~0<<position) ) != 0;
    }

    public static class BitOperateException extends Exception{
		private static final long serialVersionUID = 6979403183991540981L;

		public static final int NOT_SET					=-1;
		public static final int ILLEGAL_POSITION 		= 1;
		public static final int NEGATIVE_POSITION 		= 2;
		public static final int TOO_LARGE_POSITON 		= 3;
		public static final int START_GREATER_THAN_END 	= 4;
		public static final int UNKNOWN_NOTATION 		= 5;
		public static final int UNKNOWN_TOKEN 			= 6;

		private int code = NOT_SET;
		public BitOperateException(String message, int exceptionCode){
			super(message);
			code = exceptionCode;
		}
		public BitOperateException(String message){
			this(message, NOT_SET);
		}
		public BitOperateException(){
			this("Cannot do bit operate normally.");
		}
		public BitOperateException(String message, int exceptionCode, Throwable cause){
			super(message, cause);
			code = exceptionCode;
		}
		public BitOperateException(String message, Throwable cause) {
			this(message, NOT_SET, cause);
		}
		public BitOperateException(Throwable cause) {
			this("Cannot do bit operate normally.", cause);
		}

		
		/**
		 * @param code 错误代码
		 */
		public void setCode(int exceptionCode) {
			this.code = exceptionCode;
		}
		/**
		 * @return 错误代码
		 */
		public int getCode(){
			return code;
		}
    }
}
