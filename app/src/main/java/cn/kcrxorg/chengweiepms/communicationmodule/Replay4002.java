package cn.kcrxorg.chengweiepms.communicationmodule;

public class Replay4002 extends BaseCmd {
    String id;
    String fragment;//4字节随机数，最高两位保持0 1,073,741,823最大
    String timestamp;
    String data;
    String operator;
    String auditor;
    public Replay4002()
    {
        setTransport("4002");
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getAuditor() {
        return auditor;
    }

    public void setAuditor(String auditor) {
        this.auditor = auditor;
    }




}
