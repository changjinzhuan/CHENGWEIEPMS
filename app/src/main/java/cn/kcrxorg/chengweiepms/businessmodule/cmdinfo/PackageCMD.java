package cn.kcrxorg.chengweiepms.businessmodule.cmdinfo;

public class PackageCMD extends BaseCommand {
    private busiInfo[] busiinfoList;
    private packInfo[] packinfoList;
    private stackInfo[] stackInfoList;

    public busiInfo[] getBusiinfoList() {
        return busiinfoList;
    }

    public void setBusiinfoList(busiInfo[] busiinfoList) {
        this.busiinfoList = busiinfoList;
    }

    public packInfo[] getPackinfoList() {
        return packinfoList;
    }

    public void setPackinfoList(packInfo[] packinfoList) {
        this.packinfoList = packinfoList;
    }

    public stackInfo[] getStackInfoList() {
        return stackInfoList;
    }

    public void setStackInfoList(stackInfo[] stackInfoList) {
        this.stackInfoList = stackInfoList;
    }



}
