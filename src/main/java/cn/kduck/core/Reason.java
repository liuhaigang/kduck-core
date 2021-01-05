package cn.kduck.core;

import java.util.Date;

/**
 * 用于标记使用扩展接口的原因的说明对象
 * @author LiuHG
 */
public class Reason {

    private final String userName;
    private final String approver;
    private final ReasonType type;
    private final String remark;
    private Date fixDate;


    public String getUserName() {
        return userName;
    }

    public String getApprover() {
        return approver;
    }

    public ReasonType getType() {
        return type;
    }

    public String getRemark() {
        return remark;
    }

    public Date getFixDate() {
        return fixDate;
    }

    public void setFixDate(Date fixDate) {
        this.fixDate = fixDate;
    }

    /**
     *
     * @param type 原因类型
     * @param userName 调用者姓名（中文）
     * @param approver 批准人姓名（中文）
     * @param remark 使用该方法原因
     */
    public Reason(ReasonType type,String userName, String approver, String remark){
        this.type = type;
        this.userName = userName;
        this.approver = approver;
        this.remark = remark;
    }

    public enum ReasonType{
        FIX_LATER,//未来修复
        LOGIC_COMPLEX,//逻辑复杂
        TEST_ONLY,//仅为测试
        DEPRECATED,//折旧、不使用、旧代码
    }
}
