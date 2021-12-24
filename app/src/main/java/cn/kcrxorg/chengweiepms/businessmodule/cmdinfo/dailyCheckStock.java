package cn.kcrxorg.chengweiepms.businessmodule.cmdinfo;

import java.math.BigDecimal;

public class dailyCheckStock {
    private String sackNo;
    private String voucherTypeID;

    private String voucherTypeName;

    private String val;

    private String paperTypeID;

    private String paperTypeName;

    private BigDecimal sackMoney;

    private String bundles;

    private String tie;

    private String oprDT;

    private String isUnPacket;

    private String isCheck;

    private String sstackCode;

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

    public BigDecimal getSackMoney() {
        return sackMoney;
    }

    public void setSackMoney(BigDecimal sackMoney) {
        this.sackMoney = sackMoney;
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

    public String getOprDT() {
        return oprDT;
    }

    public void setOprDT(String oprDT) {
        this.oprDT = oprDT;
    }

    public String getIsUnPacket() {
        return isUnPacket;
    }

    public void setIsUnPacket(String isUnPacket) {
        this.isUnPacket = isUnPacket;
    }

    public String getIsCheck() {
        return isCheck;
    }

    public void setIsCheck(String isCheck) {
        this.isCheck = isCheck;
    }

    public String getSstackCode() {
        return sstackCode;
    }

    public void setSstackCode(String sstackCode) {
        this.sstackCode = sstackCode;
    }

    public String getSstackName() {
        return sstackName;
    }

    public void setSstackName(String sstackName) {
        this.sstackName = sstackName;
    }

    private String sstackName;


}
