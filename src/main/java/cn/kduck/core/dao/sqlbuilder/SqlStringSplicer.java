package cn.kduck.core.dao.sqlbuilder;

import cn.kduck.core.dao.sqlbuilder.SelectConditionBuilder.OrderBuilder.OrderType;

public class SqlStringSplicer {

    private final char wrappedChar;

    private final StringBuilder stringBuilder;

    private boolean wrapped = false;

    public SqlStringSplicer(){
        wrappedChar = '`';
        stringBuilder = new StringBuilder();
    }

    public SqlStringSplicer(String sql){
        wrappedChar = '`';
        stringBuilder = new StringBuilder(sql);
    }

    public SqlStringSplicer(char wrappedChar){
        this.wrappedChar = wrappedChar;
        stringBuilder = new StringBuilder();
    }

    public SqlStringSplicer(String sql,char wrappedChar){
        this.wrappedChar = wrappedChar;
        stringBuilder = new StringBuilder(sql);
    }

    public SqlStringSplicer append(char c) {
        stringBuilder.append(c);
        return this;
    }

    public SqlStringSplicer append(String str) {
        stringBuilder.append(str);
        return this;
    }

    public SqlStringSplicer appendSpace() {
        stringBuilder.append(' ');
        return this;
    }

    public SqlStringSplicer appendGroup(String str) {
        stringBuilder.append('(');
        stringBuilder.append(str);
        stringBuilder.append(')');
        return this;
    }

    public SqlStringSplicer appendPlaceholder(String attrName) {
        stringBuilder.append("#{");
        stringBuilder.append(attrName);
        stringBuilder.append("}");
        return this;
    }


    public SqlStringSplicer appendWrapped(String str) {
        int delimiterIndex = str.indexOf('.');
        if(delimiterIndex < 0){
            appendWrappedIfEnabled(str);
        }else{
            appendWrapped(str.substring(0,delimiterIndex),str.substring(delimiterIndex+1));
        }

        return this;
    }

    public static String textWrapped(String str){
        return new SqlStringSplicer().appendWrapped(str).toString();
    }

    public SqlStringSplicer appendWrapped(String alias,String str) {
        appendWrappedIfEnabled(alias);
        stringBuilder.append('.');
        appendWrappedIfEnabled(str);
        return this;
    }

    public SqlStringSplicer append(OrderType type) {
        stringBuilder.append(type);
        return this;
    }

    public boolean hasText(){
        return stringBuilder.length() > 0;
    }

    private void appendWrappedIfEnabled(String str){
        if(wrapped){
            stringBuilder.append(wrappedChar);
            stringBuilder.append(str);
            stringBuilder.append(wrappedChar);
        }else{
            stringBuilder.append(str);
        }
    }

//    /**
//     * 去除前后的包装字符，会破坏程序的完整性，不应该被使用，暂弃
//     * @return
//     */
//    public String toUnwrappedString() {
//        String sql = stringBuilder.toString();
//        if(sql.startsWith(""+wrappedChar) && sql.endsWith(""+wrappedChar)){
//            return sql.substring(1,sql.length()-1);
//        }
//        return sql;
//    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
