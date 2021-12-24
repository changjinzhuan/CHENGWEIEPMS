package cn.kcrxorg.chengweiepms.businessmodule.cmdinfo;

public class EnterCheckPackInfo extends packInfo {

    private String paperTypeID;
    private String paperTypeName;
    private String bundles;
    private String tie;
    private String sackMoney;
    private String oprDT;
    private String sstackCode;
    private String sstackName;

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

    public String getSackMoney() {
        return sackMoney;
    }

    public void setSackMoney(String sackMoney) {
        this.sackMoney = sackMoney;
    }

    public String getOprDT() {
        return oprDT;
    }

    public void setOprDT(String oprDT) {
        this.oprDT = oprDT;
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
}
