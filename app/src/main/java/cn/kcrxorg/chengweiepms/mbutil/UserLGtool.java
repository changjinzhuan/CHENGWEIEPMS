package cn.kcrxorg.chengweiepms.mbutil;


import android.content.Context;
import android.util.Log;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserLGtool {
	
	Context mContext;
    private FtpServer mFtpServer;
	private String mFtpServerAddr;
	String myPath;
   
  public void stop()
  {
	  mFtpServer.stop();
  }

   public void startFtpServer(Context context) throws FtpException {
//		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
//		File files=new File("/mnt/sdcard/users.properties");
//		userManagerFactory.setFile(files);
	   mContext=context;
       FtpServerFactory serverFactory = new FtpServerFactory();
       ListenerFactory factory = new ListenerFactory();
       
//       serverFactory.setUserManager(userManagerFactory.createUserManager());
       // set the port of the listener
       File dataRoot = mContext.getFilesDir();
	   myPath = dataRoot.getPath();
       BaseUser user = new BaseUser();
       user.setName("kcrx");
       user.setPassword("kcrx8888");
       user.setHomeDirectory(myPath);
       List<Authority> authorities = new ArrayList<Authority>();
       authorities.add(new WritePermission());
       user.setAuthorities(authorities);
	   serverFactory.getUserManager().save(user);
       	 int port = 2121;
            factory.setPort(port);
            factory.setServerAddress(mFtpServerAddr);
          
            // replace the default listener
            Listener lisener=factory.createListener();
            serverFactory.addListener("default", lisener);
            // start the server
            FtpServer server = serverFactory.createServer();
         
            this.mFtpServer = server;
       	    mFtpServer.start();  	
            Log.d("ftpserver", "用户FTP服务器已启动"+factory.getServerAddress()+":"+factory.getPort()+"\r\n主目录:"+user.getHomeDirectory());

	}
	public void setFtpAddr(String FTPAddr)
	{
		this.mFtpServerAddr=FTPAddr;
	}
}
