package cn.kcrxorg.chengweiepms.communicationmodule;

import com.BRMicro.Tools;

import java.io.UnsupportedEncodingException;

public class Cmd1002 extends BaseCmd {


    public  Cmd1002(BaseCmd baseCmd) throws UnsupportedEncodingException {
        setLevel(baseCmd.getPayloads().substring(0,2));
        byte[] messagesb= Tools.HexString2Bytes(baseCmd.getPayloads().substring(10,baseCmd.getPayloads().length()-16));
        String messagestr=new String(messagesb,"utf-8");
        setMessage(messagestr);
    }
    private String level;
    private String message;
    private String branch;
    private String details;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
