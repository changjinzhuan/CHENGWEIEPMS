package cn.kcrxorg.chengweiepms.businessmodule.mapper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

     //建表
        String creaTT_USER="CREATE TABLE [T_USER] (\n" +
                "  [UID] VARCHAR(20) NOT NULL, \n" +
                "  [CID] VARCHAR(20) NOT NULL, \n" +
                "  [NAME] VARCHAR(20) NOT NULL, \n" +
                "  [privileges] VARCHAR(50) NOT NULL);\n" ;

        sqLiteDatabase.execSQL(creaTT_USER);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


}
