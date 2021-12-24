package cn.kcrxorg.chengweiepms.businessmodule.cmdinfo;

public class SuperScanCMD extends BaseCommand {

    public superScanBusiInfo[] getBusiInfoList() {
        return busiInfoList;
    }

    public void setBusiInfoList(superScanBusiInfo[] busiInfoList) {
        this.busiInfoList = busiInfoList;
    }

    public stackInfo[] getStackInfoList() {
        return stackInfoList;
    }

    public void setStackInfoList(stackInfo[] stackInfoList) {
        this.stackInfoList = stackInfoList;
    }

    private  superScanBusiInfo[] busiInfoList;
    private stackInfo[] stackInfoList;
}
