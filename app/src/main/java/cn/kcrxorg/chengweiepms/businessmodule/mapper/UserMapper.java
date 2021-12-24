package cn.kcrxorg.chengweiepms.businessmodule.mapper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.User;


public class UserMapper {
    DBHelper helper;
    SQLiteDatabase db;
    Context mcontext;

    public UserMapper(Context context) {
        this.helper = new DBHelper(context, "KCRXEPMS.db", null, 1);
        this.db = helper.getWritableDatabase();
        mcontext=context;
    }

    public void delete()
    {
        int i=db.delete("T_USER",null,null);
        if(i>0)
        {
            Log.e("kcrx","T_USER表清除完成"+i);
        }
    }
    public long insert(User user)
    {
        ContentValues values=new ContentValues();
        values.put("UID",user.getUid());
        values.put("CID",user.getCid());
        values.put("NAME",user.getName());
        values.put("privileges",  Arrays.toString(user.getPrivileges()));
        long insertrs= db.insert("T_USER",null,values);
        return insertrs;
    }
    public boolean checkUser(String cid)
    {
        boolean flag=false;
        String[] args = {cid+""};
        Cursor queryCursor= db.query("T_USER",new String[]{"CID"},"CID=?",args,null,null,null,null);
        if(queryCursor.getCount()>0)
        {
            flag=true;
        }

        return  flag;
    }
    public User getUseruid(String cid)
    {
        String[] args = {cid+""};
        Cursor queryCursor= db.query("T_USER",new String[]{"*"},"CID=?",args,null,null,null,null);
        while(queryCursor.moveToNext())
        {
            User user=new User();
            user.setUid(queryCursor.getString(queryCursor.getColumnIndex("UID")));
            user.setCid(queryCursor.getString(queryCursor.getColumnIndex("CID")));
            user.setName(queryCursor.getString(queryCursor.getColumnIndex("NAME")));
            String p=queryCursor.getString(queryCursor.getColumnIndex("privileges"));
            String[] ps=p.split(",");
            user.setPrivileges(ps);
            return  user;
        }

          return  null;
    }
    public List<User> getUsers()
    {
        Cursor queryCursor= db.query("T_USER",new String[]{"*"},null,null,null,null,null,null);
        List userlist=new ArrayList();
        while(queryCursor.moveToNext())
        {
            User user=new User();
            user.setUid(queryCursor.getString(queryCursor.getColumnIndex("UID")));
            user.setCid(queryCursor.getString(queryCursor.getColumnIndex("CID")));
            user.setName(queryCursor.getString(queryCursor.getColumnIndex("NAME")));
            String p=queryCursor.getString(queryCursor.getColumnIndex("privileges"));
            String[] ps=p.split(",");
            user.setPrivileges(ps);
            userlist.add(user);
           // return  user;
        }
        return userlist;
    }
}
