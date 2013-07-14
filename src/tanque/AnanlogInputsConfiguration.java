package tanque;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Cesar on 7/13/13.
 */
public class AnanlogInputsConfiguration {

    public double getM(int AnalogInputAI){
        //Get Configuration from DB
        SQLiteDatabase myDataBase = SQLiteDatabase.openDatabase("/data/data/com.dte.tanque/databases/AI", null,
                SQLiteDatabase.OPEN_READONLY);
        Cursor c =myDataBase.rawQuery("SELECT * FROM AI",null);
        c.move(AnalogInputAI);
        double temp = c.getFloat(c.getColumnIndex("m"));
        c.close();
        myDataBase.close();
        return temp;
    }
    public double getB(int AnalogInputAI){
        //Get Configuration from DB
        SQLiteDatabase myDataBase = SQLiteDatabase.openDatabase("/data/data/com.dte.tanque/databases/AI", null,
                SQLiteDatabase.OPEN_READONLY);
        Cursor c =myDataBase.rawQuery("SELECT * FROM AI",null);

        c.move(AnalogInputAI);
        double temp = c.getFloat(c.getColumnIndex("b"));
        c.close();
        myDataBase.close();
        return temp;
    }
    public String getUnits(int AnalogInputAI){
        //Get Configuration from DB
        SQLiteDatabase myDataBase = SQLiteDatabase.openDatabase("/data/data/com.dte.tanque/databases/AI", null,
                SQLiteDatabase.OPEN_READONLY);
        Cursor c =myDataBase.rawQuery("SELECT * FROM AI",null);
        c.move(AnalogInputAI);
        String temp = c.getString(c.getColumnIndex("units"));
        c.close();
        myDataBase.close();

        return temp;
    }
}
