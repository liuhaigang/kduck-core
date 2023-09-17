package cn.kduck.core.utils;

import java.util.Arrays;

public final class StringUtils {

	private StringUtils() {}

	/**
	 * 对字符串根据指定的字符进行分隔成数组，返回结果会忽略空白值，被分隔的内容不包含完全由空格、控制符组成的值。
	 * 返回的值不会将被分隔部分的前后空格忽略。例如，假设分隔符为'/'：
	 * <pre>
	 *     "/a/b/c"			-->	{"a","b","c"}
	 *     "a/b/c"			-->	{"a","b","c"}
	 *     "a/b/c/"			-->	{"a","b","c"}
	 *     "/a/b/c/"		-->	{"a","b","c"}
	 *     "  /a/b/c/  "		-->	{"a","b","c"}
	 *     "  a/b  /c/"		-->	{"  a","b  ","c"}
	 *     " /  / /  /"		-->	{}
	 *     "/"			-->	{}
	 *     ""			-->	{}
	 *     null			-->	{}
	 * </pre>
	 * @param toSplit 被分隔的的字符串
	 * @param delimiter 分隔字符
	 * @return 如果toSplit为null或者其中没有值可返回，返回空数组，永远不会返回null
	 */
	public static String[] split(String toSplit,char delimiter) {
		if (toSplit == null || toSplit.isEmpty()) {
			return new String[0];
		}

		String[] splitInfo = toSplit.split("[" + delimiter + "]");
		String[] resultArray = new String[splitInfo.length];
		int count = 0;
		for (String info : splitInfo) {
			if(!info.trim().isEmpty()){
				resultArray[count++] = info;
			}
		}

		if(count == 0){
			return new String[0];
		}

		if(splitInfo.length == count) {
			return resultArray;
		}
		return Arrays.copyOf(resultArray,count);
	}
	public static String upperFirstChar(String str) {
		if(str == null || str.length() == 0) return "";
		char[] charArray = str.toCharArray();
		charArray[0] = Character.toUpperCase(charArray[0]);
		return new String(charArray);
	}
	
	public static String lowerFirstChar(String str) {
		if(str == null || str.length() == 0) return "";
		char[] charArray = str.toCharArray();
		charArray[0] = Character.toLowerCase(charArray[0]);
		return new String(charArray);
	}
	
	public static String getWordFirstChar(String str) {
		return getWordFirstChar(str,'_');
	}
	
	public static String getWordFirstChar(String str,char separator) {
		String[] split = str.split("[" + separator + "]");
		StringBuilder sb = new StringBuilder();
		for (String word : split) {
			sb.append(word.charAt(0));
		}
		return sb.toString().toLowerCase();
	}
	
	public static String clearSeparator(String str,char separator) {
		String[] split = str.split("[" + separator + "]");
		StringBuilder sb = new StringBuilder(split[0].toLowerCase());
		for (int i = 1; i < split.length; i++) {
			sb.append(upperFirstChar(split[i].toLowerCase()));
		}
		return sb.toString();
	}
	
	public static String lowerCaseWithSeparator(String str,char separator){
		return caseWithSeparator(str,separator,false);
	}
	
	public static String upperCaseWithSeparator(String str,char separator){
		return caseWithSeparator(str,separator,true);
	}

	private static String caseWithSeparator(String str,char separator,boolean firstUpper){
		if(str == null){
			return "";
		}

		char[] charArray = str.toCharArray();
		StringBuilder resultBuilder = new StringBuilder();
		for (int i = 0; i < charArray.length; i++) {
			if(i != 0 && charArray[i] == separator){
				firstUpper = !firstUpper;
				continue;
			}
			if(firstUpper){
				resultBuilder.append(Character.toUpperCase(charArray[i]));
				firstUpper = !firstUpper;
			}else{
				resultBuilder.append(Character.toLowerCase(charArray[i]));
			}
		}
		return resultBuilder.toString();
	}

	public static boolean hasLowerCaseChar(String text){
		char[] chars = text.toCharArray();
		for (char c : chars) {
			if(Character.isLowerCase(c)){
				return true;
			}
		}
		return false;
	}

	public static boolean contain(String[] array,String str){
		if(array == null){
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if(array[i].equals(str)){
				return true;
			}
		}
		return false;
	}
}
