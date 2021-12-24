package cn.kcrxorg.chengweiepms.businessmodule.cmdinfo;

public class EmptyEnterScanCMD extends BaseCommand {

    private cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.EmptyEnterScanPackInfo[] packInfoList;

    private stackInfo[] stackInfoList;

    public cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.EmptyEnterScanPackInfo[] getPackInfoList() {
        return packInfoList;
    }

    public void setPackInfoList(cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.EmptyEnterScanPackInfo[] packInfoList) {
        this.packInfoList = packInfoList;
    }

    public stackInfo[] getStackInfoList() {
        return stackInfoList;
    }

    public void setStackInfoList(stackInfo[] stackInfoList) {
        this.stackInfoList = stackInfoList;
    }
}
