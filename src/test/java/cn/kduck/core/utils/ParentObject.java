package cn.kduck.core.utils;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ParentObject {

    private String text;
    private Integer num;
    private Date date;
    private Boolean bool;
    private Double decimalDouble;
    private Float decimalFloat;

    private String[] textArray;
    private List<SubObject> subList;
    private Map valueMap;

    private SubObject subObject;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getBool() {
        return bool;
    }

    public void setBool(Boolean bool) {
        this.bool = bool;
    }

    public Double getDecimalDouble() {
        return decimalDouble;
    }

    public void setDecimalDouble(Double decimalDouble) {
        this.decimalDouble = decimalDouble;
    }

    public Float getDecimalFloat() {
        return decimalFloat;
    }

    public void setDecimalFloat(Float decimalFloat) {
        this.decimalFloat = decimalFloat;
    }

    public SubObject getSubObject() {
        return subObject;
    }

    public void setSubObject(SubObject subObject) {
        this.subObject = subObject;
    }

    public Map getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map valueMap) {
        this.valueMap = valueMap;
    }

    public String[] getTextArray() {
        return textArray;
    }

    public void setTextArray(String[] textArray) {
        this.textArray = textArray;
    }

    public List<SubObject> getSubList() {
        return subList;
    }

    public void setSubList(List<SubObject> subList) {
        this.subList = subList;
    }
}
