package cn.kcrxorg.chengweiepms.businessmodule.cmdinfo;

import java.math.BigDecimal;

public class outDetail {
    public String getTransNo() {
        return transNo;
    }

    public void setTransNo(String transNo) {
        this.transNo = transNo;
    }

    public String getOrganID() {
        return organID;
    }

    public void setOrganID(String organID) {
        this.organID = organID;
    }

    public String getOrganName() {
        return organName;
    }

    public void setOrganName(String organName) {
        this.organName = organName;
    }

    public String getVoucherTypeID() {
        return voucherTypeID;
    }

    public void setVoucherTypeID(String voucherTypeID) {
        this.voucherTypeID = voucherTypeID;
    }

    public String getVoucherTypeName() {
        return voucherTypeName;
    }

    public void setVoucherTypeName(String voucherTypeName) {
        this.voucherTypeName = voucherTypeName;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getPaperTypeID() {
        return paperTypeID;
    }

    public void setPaperTypeID(String paperTypeID) {
        this.paperTypeID = paperTypeID;
    }

    public String getPaperTypeName() {
        return paperTypeName;
    }

    public void setPaperTypeName(String paperTypeName) {
        this.paperTypeName = paperTypeName;
    }

    public String getOprDT() {
        return oprDT;
    }

    public void setOprDT(String oprDT) {
        this.oprDT = oprDT;
    }

    public BigDecimal getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(BigDecimal totalMoney) {
        this.totalMoney = totalMoney;
    }

    private String transNo;


    private String organID;

    private String organName;


    private String voucherTypeID;

    private String voucherTypeName;

    private String val;

    private String paperTypeID;

    private String paperTypeName;

    private String oprDT;

    private BigDecimal totalMoney;

}
