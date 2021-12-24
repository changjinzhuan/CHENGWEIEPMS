package cn.kcrxorg.chengweiepms.communicationmodule;

import com.BRMicro.Tools;

public class Cmd1001 extends BaseCmd {

    public  Cmd1001(BaseCmd baseCmd)
    {
        setVersion(new String(Tools.HexString2Bytes(baseCmd.getPayloads().substring(8))));
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    String version;
}
