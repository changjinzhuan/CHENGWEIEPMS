package cn.kcrxorg.chengweiepms.communicationmodule;

public class Replay8002 extends BaseCmd {
    String error="00000000";
    String rAPDU;
    public Replay8002()
    {
         setTransport("8002");

    }


    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getrAPDU() {
        return rAPDU;
    }

    public void setrAPDU(String rAPDU) {
        this.rAPDU = rAPDU;
    }
}
