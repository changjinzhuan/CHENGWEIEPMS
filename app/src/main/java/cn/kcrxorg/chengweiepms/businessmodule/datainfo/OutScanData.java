package cn.kcrxorg.chengweiepms.businessmodule.datainfo;

public class OutScanData extends BaseData {
    public transferData[] getTransferDataList() {
        return transferDataList;
    }

    public void setTransferDataList(transferData[] transferDataList) {
        this.transferDataList = transferDataList;
    }

    private transferData[] transferDataList;
}
