package cn.kcrxorg.chengweiepms.communicationmodule;

public class Replay8001 extends BaseCmd{
   String atr="";

   public String getError() {
      return error;
   }

   public void setError(String error) {
      this.error = error;
   }

   String error="00000000";

   public Replay8001()
   {
       setTransport("8001");

   }
   public String getAtr() {
      return atr;
   }

   public void setAtr(String atr) {
      this.atr = atr;
   }

}
