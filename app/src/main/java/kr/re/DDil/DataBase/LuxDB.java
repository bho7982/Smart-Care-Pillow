package kr.re.DDil.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by inthetech on 2017-01-09.
 */

public class LuxDB extends SQLiteOpenHelper {
    public LuxDB(Context context){
        super(context, "LuxDB_01", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table member (_id INTEGER PRIMARY KEY AUTOINCREMENT, lux_val char(20), breath_val char(20), snore_val char(20), apnea_val char(20), user_id char(10), name char(10), Input_date datetime(10))");
        //일주일 + 한달
        db.execSQL("create table member2 (_id INTEGER PRIMARY KEY AUTOINCREMENT, lux_val char(20), breath_val char(20), snore_val char(20), apnea_val char(20), Input_date datetime(10))");
        //하루
        db.execSQL("create table member3 (_id INTEGER PRIMARY KEY AUTOINCREMENT, lux_val char(20), breath_val char(20), snore_val char(20), apnea_val char(20), Input_date datetime(10))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS member");
        db.execSQL("DROP TABLE IF EXISTS member2");
        db.execSQL("DROP TABLE IF EXISTS member3");
        onCreate(db);
    }
}
