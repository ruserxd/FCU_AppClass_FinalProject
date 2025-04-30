package fcu.app.appclassfinalproject.dataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SqlDataBaseHelper extends SQLiteOpenHelper {
    private static final String DataBaseName = "FCU_FinalProjectDataBase";
    private static final int DataBaseVersion = 1;

    private static final String TABLENAME = "Users";

    public SqlDataBaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version,String TableName) {
        super(context, DataBaseName, null, DataBaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SqlTable = "CREATE TABLE IF NOT EXISTS " +  TABLENAME +" (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "account TEXT not null," +
                "password TEXT not null," +
                "email TEXT not null" +
                ")";
        sqLiteDatabase.execSQL(SqlTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        final String SQL = "DROP TABLE Users";
        sqLiteDatabase.execSQL(SQL);
    }
}