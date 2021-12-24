package cn.kcrxorg.chengweiepms.communicationmodule;

public class BaseCmd {

       String length;
       String sequence;
       String transport;
       String payloads;

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getTransport() {
        return transport;
    }


    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getPayloads() {
        return payloads;
    }

    public void setPayloads(String payloads) {
        this.payloads = payloads;
    }
    public boolean checkLenth()
    {
      //  byte[] lenB= Tools.HexString2Bytes("0"+getLength().substring(1));
        int lenth=Integer.parseInt(getLength().substring(1),16);
        if(lenth==(getPayloads().length()/2))
        {
            return true;
        }else
        {
            return false;
        }
    }
    public String toDataString()
    {
        return getLength()+getSequence()+getTransport()+getPayloads();
    }


}
