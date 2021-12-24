package cn.kcrxorg.chengweiepms.businessmodule.datainfo;

import java.math.BigDecimal;

public class elePackInfo {
    private String sackNo;


    private String voucherTypeID;

    private String voucherTypeName;


    private String val;


    private String paperTypeID;


    private String paperTypeName;



    private String bundles;


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

    public BigDecimal getSackMoney() {
        return sackMoney;
    }

    public void setSackMoney(BigDecimal sackMoney) {
        this.sackMoney = sackMoney;
    }

    public String getOprDT() {
        return oprDT;
    }

    public void setOprDT(String oprDT) {
        this.oprDT = oprDT;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getScanUser() {
        return scanUser;
    }

    public void setScanUser(String scanUser) {
        this.scanUser = scanUser;
    }

    public String getHavaBoxInfo() {
        return havaBoxInfo;
    }

    public void setHavaBoxInfo(String havaBoxInfo) {
        this.havaBoxInfo = havaBoxInfo;
    }

    public String getEditionCode() {
        return editionCode;
    }

    public void setEditionCode(String editionCode) {
        this.editionCode = editionCode;
    }

    public String getEditionName() {
        return editionName;
    }

    public void setEditionName(String editionName) {
        this.editionName = editionName;
    }

    public String getSeriesCode() {
        return seriesCode;
    }

    public void setSeriesCode(String seriesCode) {
        this.seriesCode = seriesCode;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    private BigDecimal sackMoney;

    private String oprDT;


    private String teamId;
    private String teamName;
    private String scanUser;


    private String havaBoxInfo;


    //质疑
    private String editionCode;

    private String editionName;

    private String seriesCode;

    private String seriesName;


}

