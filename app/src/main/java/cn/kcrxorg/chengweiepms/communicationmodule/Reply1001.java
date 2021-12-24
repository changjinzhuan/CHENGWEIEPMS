package cn.kcrxorg.chengweiepms.communicationmodule;

import com.BRMicro.Tools;

import cn.kcrxorg.chengweiepms.mbutil.MyTools;


public class Reply1001 extends BaseCmd {

    String  token="8000001F";//8000 001F  id 是由用户授予 有电子签封拆封能力 有电子签封识别能力 便携 可移动 即时联机
   // String  token="0000001F";//0000 001F  测试

    L4vtype id;//Psam卡号
     L4vtype prefix;//牌照信息
     L4vtype provider;//供应商信息
     L4vtype version;//版本信息
     L4vtype label;//硬件显示信息
     L4vtype model;//硬件型号信息
     String heartbeat="0000";//无心跳
    public Reply1001(String idstr,String versionstr)
   {
         setTransport("1001");
         setId(new L4vtype(idstr));
         setPrefix(new L4vtype(Tools.Bytes2HexString("kcrx".getBytes(),"kcrx".getBytes().length)));
         setProvider(new L4vtype(Tools.Bytes2HexString("kcrx".getBytes(),"kcrx".getBytes().length)));
         setVersion(new L4vtype(versionstr));
         setLabel(new L4vtype(Tools.Bytes2HexString("KC-UHF".getBytes(),"KC-UHF".getBytes().length)));
         setModel(new L4vtype(Tools.Bytes2HexString("P288".getBytes(),"P288".getBytes().length)));
         setAllrs();
   }
    public void setAllrs()
    {
        setPayloads(toString());
        int len=getPayloads().length()/2;
        String hex= Integer.toHexString(len).toUpperCase();
        setLength("8"+ MyTools.addZeroForNum(hex,7));
        setSequence("0000000000000000");
    }
   public String getReplydata()
   {
       return getLength()+getSequence()+getTransport()+getPayloads();
   }
   public String toString()
   {
       return token+id.toString()+prefix.toString()+provider.toString()+version.toString()+label.toString()+model.toString()+heartbeat;
   }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public L4vtype getId() {
        return id;
    }

    public void setId(L4vtype id) {
        this.id = id;
    }

    public L4vtype getPrefix() {
        return prefix;
    }

    public void setPrefix(L4vtype prefix) {
        this.prefix = prefix;
    }

    public L4vtype getProvider() {
        return provider;
    }

    public void setProvider(L4vtype provider) {
        this.provider = provider;
    }

    public L4vtype getVersion() {
        return version;
    }

    public void setVersion(L4vtype version) {
        this.version = version;
    }

    public L4vtype getLabel() {
        return label;
    }

    public void setLabel(L4vtype label) {
        this.label = label;
    }

    public L4vtype getModel() {
        return model;
    }

    public void setModel(L4vtype model) {
        this.model = model;
    }




}
