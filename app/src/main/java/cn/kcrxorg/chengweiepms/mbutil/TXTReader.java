package cn.kcrxorg.chengweiepms.mbutil;

import android.content.Context;
import android.util.Log;

import com.BRMicro.Tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TXTReader {
	//任务数据文件目录
	private static final String CmdDir = "/Cmd/";
	private static final String DataDir = "/Data/";

	public String read(File f)
	{
		StringBuilder sb=new StringBuilder();
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(f),"UTF-8");
			BufferedReader br = new BufferedReader(read);
			String s;
			while ((s = br.readLine()) != null) {
		//	Log.d("read",s);
				sb.append(s);
			}		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}public List<String> readLog(File f)
	{
		List<String> stringList=new ArrayList<>();
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(f),"UTF-8");
			BufferedReader br = new BufferedReader(read);
			String s;
			while ((s = br.readLine()) != null) {
				//	Log.d("read",s);
				Log.e("kcrx","read line:"+s);
				stringList.add(s);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return stringList;
	}

	public String findLastFile(Context context,String dir) throws UnsupportedEncodingException {
		String lastfiledata="";
		File logRoot = context.getFilesDir();
		File CmdDirF =new File(logRoot.getPath()+dir);
        Long lasttime=0L;
        File lastFile=null;
        if(CmdDirF.listFiles().length>0)//如果存在文件
		{
			for(File f:CmdDirF.listFiles())
			{
                if(f.lastModified()>lasttime)
				{
					lasttime=f.lastModified();
					lastFile=f;
				}
			}
		}else
		{
			return null;
		}
		byte[] lastfiledatestr=(read(lastFile)).getBytes("UTF-8");
		lastfiledata=lastFile.getName().split("_")[1]+lastFile.getName().split("_")[2].replace(".json","")+byteArrToHex(lastfiledatestr);

		return lastfiledata;
	}
	public String getCmdById(Context context,String id)
	{
		File logRoot = context.getFilesDir();
		File CmdDirF =new File(logRoot.getPath()+CmdDir);
		File DataDirF =new File(logRoot.getPath()+DataDir);

		for(File f:CmdDirF.listFiles())
		{
			if(f.getName().split("_")[1].toUpperCase().equals(id.toUpperCase()))
			{
				return read(f);
			}
		}
		return null;
	}
	public List<String> clearCmdFile(Context context)
	{
		List<String> oldfilenames=new ArrayList<String>();
		File logRoot = context.getFilesDir();

		File CmdDirF =new File(logRoot.getPath()+CmdDir);
		Date nowDate = new Date();
		Long nowtime=nowDate.getTime();
		for(File f:CmdDirF.listFiles())
		{
			if((nowtime-f.lastModified())>(60 * 60 * 24*7*1000l))//7天过期
			//if((nowtime-f.lastModified())>(60*1000l))//1分钟就过期测试
			{
				oldfilenames.add(f.getName());
				f.delete();
			}
		}
		return oldfilenames;
	}
    public boolean delCmdDataFile(Context context,String id)
	{
		boolean flags=false;
		File logRoot = context.getFilesDir();
		File CmdDirF =new File(logRoot.getPath()+CmdDir);
		File DataDirF =new File(logRoot.getPath()+DataDir);


		for(File f:CmdDirF.listFiles())
		{
			if(f.getName().split("_")[1].equals(id))
			{
				flags=f.delete();
			}
		}
		for(File f:DataDirF.listFiles())
		{
			if(f.getName().split("_")[1].equals(id))
			{
				flags=f.delete();
			}
		}
		return  flags;
	}
	public boolean delDataFile(Context context)
	{
		boolean flags=false;
		File logRoot = context.getFilesDir();

		File DataDirF =new File(logRoot.getPath()+DataDir);
        if(DataDirF.listFiles().length==0)
		{
			return true;
		}
		for(File f:DataDirF.listFiles())
		{
			flags=f.delete();
		}
		return  flags;
	}
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String byteArrToHex(byte... bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
