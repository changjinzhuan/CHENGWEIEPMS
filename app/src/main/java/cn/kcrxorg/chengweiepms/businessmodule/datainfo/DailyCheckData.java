package cn.kcrxorg.chengweiepms.businessmodule.datainfo;

public class DailyCheckData extends BaseData {
    public DailyCheckPackInfo[] getPackInfoList() {
        return packInfoList;
    }

    public void setPackInfoList(DailyCheckPackInfo[] packInfoList) {
        this.packInfoList = packInfoList;
    }

    private DailyCheckPackInfo[] packInfoList;
}
