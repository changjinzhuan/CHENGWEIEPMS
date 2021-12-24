package cn.kcrxorg.chengweiepms.communicationmodule;

public class Replay4003 extends BaseCmd {
    String aid;
    String count;
    String users;
    public Replay4003()
    {
        setTransport("4003");
    }
    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }
}
