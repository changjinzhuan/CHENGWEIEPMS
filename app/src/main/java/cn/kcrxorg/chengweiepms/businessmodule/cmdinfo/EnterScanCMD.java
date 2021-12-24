package cn.kcrxorg.chengweiepms.businessmodule.cmdinfo;

public class EnterScanCMD extends BaseCommand {
    public paymentSack[] getPaymentSackList() {
        return paymentSackList;
    }

    public void setPaymentSackList(paymentSack[] paymentSackList) {
        this.paymentSackList = paymentSackList;
    }

    public stackInfo[] getStackInfoList() {
        return stackInfoList;
    }

    public void setStackInfoList(stackInfo[] stackInfoList) {
        this.stackInfoList = stackInfoList;
    }

    private paymentSack[] paymentSackList;
    private stackInfo[] stackInfoList;
}
