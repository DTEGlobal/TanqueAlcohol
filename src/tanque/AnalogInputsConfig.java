package tanque;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class AnalogInputsConfig extends ContentProvider {

    private static class DatabaseHelper extends SQLiteOpenHelper{
        DatabaseHelper(Context context) {
            super(context, ANALOGINPUTS, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            //tabla mensajes
            db.execSQL(CREAR_BASEDATOS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS mensajes ");
            onCreate(db);

        }
    }


    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        int count=0;

        switch (uriMatcher.match(arg0)){
            case ANALOG:
                count = TankDB.delete(
                        ANALOGINPUTS,
                        arg1,
                        arg2);
                break;

            default: throw new IllegalArgumentException("Unknown URI " + arg0);
        }

        getContext().getContentResolver().notifyChange(arg0, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            //mensajes toda la tabla
            case ANALOG:
                return "vnd.android.cursor.dir/com.nebula.labinal";

            default:
                throw new IllegalArgumentException("URI no admitida: " + uri);
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = TankDB.insert(ANALOGINPUTS,"",values);
        Uri _uri=null;
        switch (uriMatcher.match(uri)){
            //---obtener todos los mensajes
            case ANALOG:
                if (rowID>0)
                {
                    _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(_uri, null);

                }
                break;
            default:
                throw new IllegalArgumentException("URI no admitida: " + uri);
        }

        return _uri;


    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        TankDB = dbHelper.getWritableDatabase();
        TankDB = dbHelper.getReadableDatabase();
        return (TankDB == null)? false:true;

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        // Query para las tablas
        switch (uriMatcher.match(uri)){
            case ANALOG:
                sqlBuilder.setTables(ANALOGINPUTS);
                if (sortOrder==null || sortOrder=="")
                    sortOrder = _ID;
                break;
            default: throw new NullPointerException("mal query"+uri);
        }



        Cursor c = sqlBuilder.query(
                TankDB,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;


    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case ANALOG:
                count = TankDB.update(ANALOGINPUTS,values,selection,selectionArgs);
                break;

            default: throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }







    // Con esto URIS entramos a cada tabla que nesecitamos
    public static final String PROVIDER_NAME 			="com.dteglobal.tanque";
    public static final Uri CONTENT_URI 				=Uri.parse("content://"+ PROVIDER_NAME + "/AI");


    //Analog Inputs Table
    public static final String _ID 			= "_id";
    public static final String M 			= "m";
    public static final String B 			= "b";
    public static final String UNITS		= "units";


    // para poder sacar toda la consulta o solamente una especifica
    private static final int ANALOG 				= 1;


    // este tiene que hacer el match con la tabla mensajes
    private static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "analog", ANALOG);
    }


    //---para uso base datos---
    private SQLiteDatabase TankDB;
    private static final String DB_NAME = "AnalogInputsConfig";
    private static final String ANALOGINPUTS  = "AI";
    private static final int DB_VERSION   = 1;




    // Tables needed by DB
    private static final String CREAR_BASEDATOS =
            "create table " + ANALOGINPUTS+
                    " (_id integer primary key autoincrement," +
                    "    m integer not null, " +
                    "    b integer not null, " +
                    "    units text not null," +
                    " );";


}