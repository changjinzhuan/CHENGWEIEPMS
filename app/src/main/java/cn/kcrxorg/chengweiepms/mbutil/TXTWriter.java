package cn.kcrxorg.chengweiepms.mbutil;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TXTWriter {

    //任务数据文件目录
    private static final String CmdDir = "/Cmd/";
    private static final String DataDir = "/Data/";
	
	public static void writeBinFile(String filename, byte[] b)
    {

        OutputStream out=null;
        try
        {

            File f=new File(filename);
            if(!f.exists())
            {
                f.createNewFile();
            }
            out=new FileOutputStream(f,false);
            Log.e("main","*************写入文件："+new String(b));
            out.write(b);
            out.flush();
            out.close();
            out=null;
        } catch(FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public  void writeDataFile(Context context, String filename, byte[] b)
    {
        File logRoot = context.getFilesDir();
        File CmdDirF =new File(logRoot.getPath()+DataDir);
        OutputStream out=null;
        try
        {

            File f=new File(logRoot.getPath()+DataDir+filename);
            if(!f.exists())
            {
                f.createNewFile();
            }
            out=new FileOutputStream(f,false);
            Log.e("main","*************写入文件："+new String(b));
            out.write(b);
            out.flush();
            out.close();
            out=null;
        } catch(FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
