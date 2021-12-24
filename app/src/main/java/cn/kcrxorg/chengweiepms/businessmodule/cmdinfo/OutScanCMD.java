package cn.kcrxorg.chengweiepms.businessmodule.cmdinfo;

public class OutScanCMD extends BaseCommand {
    public outScanBusiInfo[] getBusiInfoList() {
        return busiInfoList;
    }

    public void setBusiInfoList(outScanBusiInfo[] busiInfoList) {
        this.busiInfoList = busiInfoList;
    }

    public outScanStockPackInfo[] getStockPackInfoList() {
        return stockPackInfoList;
    }

    public void setStockPackInfoList(outScanStockPackInfo[] stockPackInfoList) {
        this.stockPackInfoList = stockPackInfoList;
    }

    outScanBusiInfo[] busiInfoList;


    outScanStockPackInfo[] stockPackInfoList;

}
