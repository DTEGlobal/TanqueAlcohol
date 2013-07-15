package tanque;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
    public boolean updateM(int AnalogInputAI,double newVal){
        //Get Configuration from DB
        SQLiteDatabase myDataBase = SQLiteDatabase.openDatabase("/data/data/com.dte.tanque/databases/AI", null,
                SQLiteDatabase.OPEN_READWRITE);
        Cursor c = myDataBase.rawQuery(
                    "UPDATE AI SET m = " +String.valueOf(newVal)+
                    " WHERE _id = "+String.valueOf(AnalogInputAI)
                    ,null);
        c.moveToFirst();
        c.close();
        myDataBase.close();
        double temp = getM(1);
        if(temp == newVal){
            Log.i("G4MB - DB Update", "True - getM = "+temp+" | newVal = "+newVal);
            return true;
        }
        else{
            Log.i("G4MB - DB Update", "False - getM = "+temp+" | newVal = "+newVal);
            return false;
        }

    }
    public boolean updateB(int AnalogInputAI,double newVal){
        SQLiteDatabase myDataBase = SQLiteDatabase.openDatabase("/data/data/com.dte.tanque/databases/AI", null,
                SQLiteDatabase.OPEN_READWRITE);
        Cursor c = myDataBase.rawQuery(
                "UPDATE AI SET b = " +String.valueOf(newVal)+
                        " WHERE _id = "+String.valueOf(AnalogInputAI)
                ,null);
        c.moveToFirst();
        c.close();
        myDataBase.close();
        double temp = getM(1);
        if(temp == newVal){
            Log.i("G4MB - DB Update", "True - getM = "+temp+" | newVal = "+newVal);
            return true;
        }
        else{
            Log.i("G4MB - DB Update", "False - getM = "+temp+" | newVal = "+newVal);
            return false;
        }

    }
    public boolean updateUnits(int AnalogInputAI,String newVal){
        SQLiteDatabase myDataBase = SQLiteDatabase.openDatabase("/data/data/com.dte.tanque/databases/AI", null,
                SQLiteDatabase.OPEN_READWRITE);
        Cursor c = myDataBase.rawQuery(
                "UPDATE AI SET units = " + "\""+newVal+"\"" +
                        " WHERE _id = " + String.valueOf(AnalogInputAI)
                ,null);
        c.moveToFirst();
        c.close();
        myDataBase.close();
        String temp = getUnits(1);
        if(temp.equals( newVal)){
            Log.i("G4MB - DB Update", "True - getM = "+temp+" | newVal = "+newVal);
            return true;
        }
        else{
            Log.i("G4MB - DB Update", "False - getM = "+temp+" | newVal = "+newVal);
            return false;
        }

    }

}
