package cn.kcrxorg.chengweiepms.businessmodule.cmdinfo;

public class UnPackageCMD extends BaseCommand {
    public stockPackInfo[] getStockPackInfoList() {
        return stockPackInfoList;
    }

    public void setStockPackInfoList(stockPackInfo[] stockPackInfoList) {
        this.stockPackInfoList = stockPackInfoList;
    }

    stockPackInfo[] stockPackInfoList;

}
