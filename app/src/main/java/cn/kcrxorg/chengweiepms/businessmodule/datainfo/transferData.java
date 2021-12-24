package cn.kcrxorg.chengweiepms.businessmodule.datainfo;

import java.math.BigDecimal;

public class transferData {
    private String sackNo;

    private String voucherTypeID;

    private String voucherTypeName;

    private String val;

    private String paperTypeID;


    private String paperTypeName;


    private String bundles;

    private String tie;

    private BigDecimal sackMoney;

    private String mustOutFlag;


    private String oprDT;

    public String getSackNo() {
        return sackNo;
    }

    public void setSackNo(String sackNo) {
        this.sackNo = sackNo;
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

    public String getBundles() {
        return bundles;
    }

    public void setBundles(String bundles) {
        this.bundles = bundles;
    }

    public String getTie() {
        return tie;
    }

    public void setTie(String tie) {
        this.tie = tie;
    }

    public BigDecimal getSackMoney() {
        return sackMoney;
    }

    public void setSackMoney(BigDecimal sackMoney) {
        this.sackMoney = sackMoney;
    }

    public String getMustOutFlag() {
        return mustOutFlag;
    }

    public void setMustOutFlag(String mustOutFlag) {
        this.mustOutFlag = mustOutFlag;
    }

    public String getOprDT() {
        return oprDT;
    }

    public void setOprDT(String oprDT) {
        this.oprDT = oprDT;
    }

    public String getStackCode() {
        return stackCode;
    }

    public void setStackCode(String stackCode) {
        this.stackCode = stackCode;
    }

    private String stackCode;

}
