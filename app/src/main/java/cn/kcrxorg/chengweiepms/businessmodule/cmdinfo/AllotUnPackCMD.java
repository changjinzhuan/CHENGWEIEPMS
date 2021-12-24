package cn.kcrxorg.chengweiepms.businessmodule.cmdinfo;

public class AllotUnPackCMD extends BaseCommand {
    private int operatorID;


    private int reviewerID;


    private stock[] stockList;


    public int getOperatorID() {
        return operatorID;
    }

    public void setOperatorID(int operatorID) {
        this.operatorID = operatorID;
    }

    public int getReviewerID() {
        return reviewerID;
    }

    public void setReviewerID(int reviewerID) {
        this.reviewerID = reviewerID;
    }

    public stock[] getStockList() {
        return stockList;
    }

    public void setStockList(stock[] stockList) {
        this.stockList = stockList;
    }

    public team[] getTeamList() {
        return teamList;
    }

    public void setTeamList(team[] teamList) {
        this.teamList = teamList;
    }

    private team[] teamList;

}
