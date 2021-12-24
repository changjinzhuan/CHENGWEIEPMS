package cn.kcrxorg.chengweiepms.rfidtool;

public class PervalueHelper {

    public static String getVal(int pervalueid)
    {

         switch (pervalueid)
         {
             case 1:
                 return "100.0";
             case 2:
                 return "50.0";
             case 3:
                 return "20.0";
             case 4:
                 return "10.0";
             case 5:
                 return "5.0";
             case 6:
                 return "1.0";
             default:
                 return "0";
         }
    }
}
