package ir.markazandroid.advertiser.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.network.JSONParser.Parser;
import ir.markazandroid.advertiser.object.Record;

/**
 * Coded by Ali on 26/05/2017.
 */

public class DataBase extends SQLiteOpenHelper {

    private Parser parser;

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "databse.db";
    private static final String COMMA_SEP ="," ;

    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        parser=((AdvertiserApplication) context.getApplicationContext()).getParser();
    }

    private static abstract class RecordTable implements BaseColumns {
        public static final String TABLE_NAME = "records";
        public static final String DATA = "data";
    }

    public static abstract class LinkTable {
        public static final String TABLE_NAME = "links";
        public static final String LINK = "link";
        public static final String ETAG = "ETag";
    }

    private static final String CREATE_RECORD="CREATE TABLE " + RecordTable.TABLE_NAME+ " (" +
            RecordTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + COMMA_SEP+
            RecordTable.DATA +" TEXT "+
            " )";
    private static final String CREATE_LINK="CREATE TABLE " + LinkTable.TABLE_NAME+ " (" +
            LinkTable.LINK + " TEXT PRIMARY KEY " + COMMA_SEP+
            LinkTable.ETAG +" TEXT "+
            " )";


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_RECORD);

        db.execSQL(CREATE_LINK);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 2){
            if (oldVersion==1)
                db.execSQL(CREATE_LINK);
        }
    }

    @Nullable
    public Record getRecord(){
        Cursor cursor = getReadableDatabase()
                .query(RecordTable.TABLE_NAME,null,null,null,null,null,null);
        try {
            if (cursor.moveToFirst()) {
                String object = cursor.getString(cursor.getColumnIndex(RecordTable.DATA));
                try {
                    return parser.get(Record.class, new JSONObject(object));
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            } else return null;
        }
        finally {
            cursor.close();
        }

    }

    public void setRecord(Record record){
        getWritableDatabase().delete(RecordTable.TABLE_NAME,null,null);
        if(record != null) {
            ContentValues values = new ContentValues();
            values.put(RecordTable.DATA, parser.get(record).toString());
            getWritableDatabase().insert(RecordTable.TABLE_NAME, null, values);
        }
    }
}
