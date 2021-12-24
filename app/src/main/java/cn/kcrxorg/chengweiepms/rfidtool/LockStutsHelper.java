package cn.kcrxorg.chengweiepms.rfidtool;

public class LockStutsHelper {

    public static String getStutsName(String stuts)
    {
          if(stuts=="Lock")
          {
              return "关锁";
          }else if(stuts=="unLock")
          {
              return "开锁";
          }else if(stuts=="unlawful")
          {
              return "非法";
          }else
          {
              return "未知";
          }
    }
}
