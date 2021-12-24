package cn.kcrxorg.chengweiepms.mbutil;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class MyLog {
	
	//private static final int MAXLINE = 8;
	//private static final String ConfigDir = "/Config/";
	
	private static final String LogDir = "/Log/";
	
	private String mLogPath = null;
	private int mLogWriteLevel;
	

	private String [] mStrDisplay;
	
	private int mMaxLine;
	private int mCurLine;	//0---mMaxLine
	private int mPreLine;	//0---mMaxLine-1
	private Context mContext;
	
	public MyLog(Context context, int maxline, int LogWriteLevel)
	{
		mContext = context;
		mLogWriteLevel = LogWriteLevel;
		mMaxLine = maxline;
		mCurLine = mPreLine = 0;;
		 
		mLogWriteLevel = 1;
//		String SDState = Environment.getExternalStorageState();
//		if (!Environment.MEDIA_MOUNTED.equals(SDState))
//		//	if (false)
//		{
//			//没有SD卡
//			mLogWriteLevel = 0;	//不写日志文件
//		}
		
	
		if(mLogWriteLevel > 0)
		{
			//File logRoot = mContext.getExternalFilesDir(null);
			File logRoot = mContext.getFilesDir();
			File logDir = new File(logRoot.getPath() + LogDir);

			if (!logDir.exists())
			{ 
				logDir.mkdirs();
			}
			
			//创建日志文件
    		SimpleDateFormat mytime = new SimpleDateFormat("yyyy_MM_dd");
    	    Date curDate = new Date(System.currentTimeMillis());
    	    mLogPath = logDir.getPath() + "/" + mytime.format(curDate)+".log";
    	    
    	    File logFile = new File(mLogPath);
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		mStrDisplay = new String[maxline];
		DisplayClear();
	
	}
	
	/*public boolean Open()
	{
		boolean Result = true;
	       
		SimpleDateFormat mytime = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
	    Date curDate = new Date(System.currentTimeMillis());
	    String logFileName = mLogPath + "/" + mytime.format(curDate)+".log";
	    
	    File logFile = new File(logFileName);
	    try
	    {
	    	mLogStream = new FileOutputStream(logFile);
	    	Result = true;
	    }
	    catch(IOException e)
	    {
	    	e.printStackTrace();
	    	Result = false;
	    }
	    
		
		return Result;
	
	}*/
	
/*	public void Close()
	}
*/	
	//Pos : 0---mMaxLine-1
	public String DisplayInPos(String msg, int Pos)
	{
		String Result = "";
		
		if(Pos < mMaxLine )
		{
			mStrDisplay[Pos] = msg;
			if(Pos > mCurLine)
			{
				mCurLine = Pos;
				mPreLine = Pos -1;
			}
		}
		
		int Count;
		if(mCurLine < mMaxLine)
		{
			Count = mCurLine +1;
		}
		else
		{
			Count = mCurLine;
		}
		
		for (int i = 0; i< Count; i++)
		{
			Result += mStrDisplay[i];
			Result += "\n";
		}
		
		return Result;
	}
	
	public void ClearInpos(int Pos)
	{
		if(Pos <= mCurLine )
		{
			mStrDisplay[Pos] = "";
			if(Pos == mCurLine)
			{
				mCurLine--;
				mPreLine = mCurLine -1;
			}
		}	
	}
	public String Display(String msg)
	{
		String Result = "";
		int count;
		
		if (mCurLine < mMaxLine)
		{
			mStrDisplay[mCurLine]= msg;
			mPreLine = mCurLine;
			mCurLine++;
			count = mCurLine;
		}
		else
		{
			for (int i = 0; i < mMaxLine-1; i++)
			{
				mStrDisplay[i] = mStrDisplay[i+1];
			}
			mStrDisplay[mMaxLine-1]= msg;
			count = mMaxLine;
			mPreLine = mMaxLine -1;
		}
		
		for (int i = 0; i< count; i++)
		{
			Result += mStrDisplay[i];
			Result += "\n";
		}
		
		return Result;
	}

	public String DisplayModify(String msg)
	{
		String Result = "";
		mStrDisplay[mPreLine]= msg;
		
		for (int i = 0; i< mPreLine + 1; i++)
		{
			Result += mStrDisplay[i];
			Result += "\n";
		}
		return Result;
	}
	
	public void DisplayClear()
	{
		mCurLine = mPreLine = 0;;
		for (int i = 0; i< mMaxLine; i++)
		{
			mStrDisplay[i] = "";
		}
	}
	public  void Write(String msg)
	{
		SimpleDateFormat mytime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());
		msg="["+mytime.format(curDate)+"]["+mContext.getClass().getSimpleName()+"]:"+msg+"\r\n";
	//	msg += "\r\n";

		if(mLogWriteLevel >0)
		{
			//写日志
			try
			{
				FileOutputStream fs = new FileOutputStream(mLogPath,true);
				MyLogger.show("kcrx["+mContext.getClass().getName()+"]",msg);
				fs.write(msg.getBytes());
				fs.close();
			}
		    catch(IOException e)
		    {
		    	e.printStackTrace();
		    }
		}
		 

	}
	public String getmLogPath()
	{
		return mLogPath;
	}
	public String DandW(String msg)
	{
		Write(msg);
		return Display(msg);
	
	}
	

}
