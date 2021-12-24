package cn.kcrxorg.chengweiepms.communicationmodule;

import androidx.annotation.Nullable;

public class CmdSelector {

    //000000090000000000000000100100000005312E302E33
    @Nullable
    public static BaseCmd makeCmd(String cmddata)
    {
        BaseCmd baseCmd=new BaseCmd();
        if(cmddata.length()>36)//命令小于36就不合法
        {
            baseCmd.setLength(cmddata.substring(0,8));
            baseCmd.setSequence(cmddata.substring(8,24));
            baseCmd.setTransport(cmddata.substring(24,28));
            baseCmd.setPayloads(cmddata.substring(28));
            return baseCmd;
        }else
        {
         return null;
        }
    }
}
