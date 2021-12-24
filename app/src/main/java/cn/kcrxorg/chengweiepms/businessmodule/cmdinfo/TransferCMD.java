package cn.kcrxorg.chengweiepms.businessmodule.cmdinfo;

public class TransferCMD extends BaseCommand {
    public scanSort[] getScanSortList() {
        return scanSortList;
    }

    public void setScanSortList(scanSort[] scanSortList) {
        this.scanSortList = scanSortList;
    }

    public outDetail[] getOutDetailList() {
        return outDetailList;
    }

    public void setOutDetailList(outDetail[] outDetailList) {
        this.outDetailList = outDetailList;
    }

    private scanSort[] scanSortList;
    private outDetail[] outDetailList;
}
