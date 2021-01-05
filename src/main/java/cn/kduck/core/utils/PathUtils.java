package cn.kduck.core.utils;

import org.springframework.util.StringUtils;

/**
 * LiuHG
 */
public class PathUtils {

    public static final char PATH_SEPARATOR = '/';

    private PathUtils() {}

    /**
     * 拼装数据路径，返回的路径以“/”开头，但不以“/”结尾的字符串，如果传入的参数均为空字符串，则返回“/”
     * <pre>
     * 例如：
     * appendPath("/a/b/c/d", "/e")	-&gt; /a/b/c/d/e
     * appendPath("a/b/c/d/", "/e")	-&gt; /a/b/c/d/e
     * appendPath("/a/b/c/d/", "e")	-&gt; /a/b/c/d/e
     * appendPath("a/b/c/d", "e")		-&gt; /a/b/c/d/e
     * appendPath("a/b/c/d", "e/f")	-&gt; /a/b/c/d/e/f
     * appendPath("a/b/c/d", "/e/f")	-&gt; /a/b/c/d/e/f
     * appendPath("a/b/c/d/", "/e/f")	-&gt; /a/b/c/d/e/f
     * appendPath("", "e")		-&gt; /e
     * appendPath("a/b/c/d", "")		-&gt; /a/b/c/d
     * appendPath("", ""))		-&gt; /
     * </pre>
     * @param dataPath
     * @param data
     * @return 拼接后的完整路径
     */
    public static String appendPath(String dataPath,String data){
        return appendPath(dataPath,data,false);
    }

    public static String appendPath(String dataPath,String data,boolean endSeparator){
        if(dataPath == null){
            return appendPath("",data,endSeparator);
        }
        if(data == null){
            return appendPath(dataPath,"",endSeparator);
        }
        if(dataPath.length() == 0 && data.length() == 0){
            return "" + PATH_SEPARATOR;
        }

        String[] dataPathSplit = dataPath.split("["+PATH_SEPARATOR+"]");
        String[] resultArray = new String[dataPathSplit.length + 1];
        System.arraycopy(dataPathSplit, 0,resultArray , 0, dataPathSplit.length);

        if(data.length() > 0 && data.charAt(0) == PATH_SEPARATOR){
            data = data.substring(1);
        }

        resultArray[resultArray.length - 1] = data;

        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < resultArray.length; i++) {
            if(StringUtils.hasText(resultArray[i])){
                strBuilder.append(PATH_SEPARATOR);
                strBuilder.append(resultArray[i]);
            }
        }

        String str = strBuilder.toString();

        if(endSeparator && !str.endsWith("["+PATH_SEPARATOR+"]")){
            str += PATH_SEPARATOR;
        }

        return str;
    }

//	public static void main(String[] args) {
//		System.out.println(DataPathUtils.appendPath("/a/b/c/d", "/e"));
//		System.out.println(DataPathUtils.appendPath("a/b/c/d/", "/e"));
//		System.out.println(DataPathUtils.appendPath("/a/b/c/d/", "e"));
//		System.out.println(DataPathUtils.appendPath("a/b/c/d", "e"));
//		System.out.println(DataPathUtils.appendPath("a/b/c/d", "e/f"));
//		System.out.println(DataPathUtils.appendPath("a/b/c/d", "/e/f"));
//		System.out.println(DataPathUtils.appendPath("a/b/c/d/", "/e/f"));
//		System.out.println(DataPathUtils.appendPath("", "e"));
//		System.out.println(DataPathUtils.appendPath("a/b/c/d", ""));
//		System.out.println(DataPathUtils.appendPath("", ""));
//	}
}
