package cn.kcrxorg.chengweiepms.mbutil;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

public class Config {
	
	Properties prop;
	String ipath="config.properties";
	private Context mContext;
	private static final String ConfigDir = "/config/";
	private static final String ConfigFile = "kcrxepms.properties";
	
	private static final String DEFULTbank = "192.168.1.10";


	public Config(Context context)
	{
		mContext=context;
	}
	public void load()
	
	{
		File LocalRoot = mContext.getFilesDir();
		File LocalConfigDir = new File(LocalRoot.getPath()+ConfigDir);
		
		if(!LocalConfigDir.exists())
		{
			LocalConfigDir.mkdirs();	//创建目录
		}
		File LocalConfigFile = new File(LocalRoot.getPath()+ConfigDir+ConfigFile);
		if(!LocalConfigFile.exists())
		{			
			//如果内部配置文件不存在
			try
			{
				LocalConfigFile.createNewFile();//创建文件
				writeStart(LocalConfigFile);
				//setProperty("serverIp","192.168.1.10");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		prop = new Properties();
		try {
		InputStreamReader isr=new InputStreamReader(new FileInputStream(LocalConfigFile),"UTF-8");
		prop.load(isr);
			
			//System.out.println(  prop.values());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String getValue(String key)
	{
		String Value=prop.getProperty(key);
		return Value;
		
	}
	public void setProperty(String key,String value)
	{
		try {
			OutputStream out = new FileOutputStream(ipath);
			prop.setProperty(key, value);

			prop.store(out, "Update " + key + " name");
		//	System.out.println(prop.values());  
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void writeStart(File file)
	{
		TXTWriter tw=new TXTWriter();
		tw.writeBinFile(file.getPath(), ("serverIp="+DEFULTbank+"\r\n").getBytes());
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	

}
