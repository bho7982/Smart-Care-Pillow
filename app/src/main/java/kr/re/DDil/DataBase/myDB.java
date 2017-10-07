package kr.re.DDil.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by inthetech on 2017-01-06.
 */

public class myDB extends SQLiteOpenHelper {
    public myDB(Context context){
        super(context, "UserDB_01", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table member (_id INTEGER PRIMARY KEY AUTOINCREMENT, name char(10), age char(10), gender char(10), weight char(10))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS member");
        onCreate(db);
    }
}