package com.roran.dangerous.makemyschedule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by dangerous on 07/03/17.
 */

public class DBAdapter {

    /////////////////////////////////////////////////////////////////////
    //	Constants & Data
    /////////////////////////////////////////////////////////////////////
    // For logging:
    private static final String TAG = "DBAdapter";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final int COL_ROWID = 0;

    // [TO_DO_A2]
    // TODO: Change the field names (column names) of your table

    public static final String KEY_NAME = "name";
    public static final String KEY_STUDENTNUM = "studentnum";
    public static final String KEY_FAVCOLOUR = "favcolour";

    // [TO_DO_A3]
    // Update the field numbers here (0 = KEY_ROWID, 1=...)
    public static final int COL_NAME = 1;
    public static final int COL_STUDENTNUM = 2;
    public static final int COL_FAVCOLOUR = 3;

    // [TO_DO_A4]
    // Update the ALL-KEYS string array
    public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_NAME, KEY_STUDENTNUM, KEY_FAVCOLOUR};

    // [TO_DO_A5]
    // DB info: db name and table name.
    public static final String DATABASE_NAME = "MyDb";
    public static final String DATABASE_TABLE = "TermTable";

    // [TO_DO_A6]
    // Track DB version
    public static final int DATABASE_VERSION = 1;


    // [TO_DO_A7]
    // DATABASE_CREATE SQL command
    private static final String DATABASE_CREATE_TERMS_SQL =
            "create table terms"
                    + " (termid integer primary key, "
                    + KEY_NAME         + " text not null, "
                    + ");";
    private static final String DATABASE_CREATE_SUBJECTS_SQL =
            "create table subjects"
                    + " (subjectid integer primary key autoincrement, "
                    + KEY_NAME         + " text not null, "
                    +"time text,"
                    +"day text,"
                    + "termfk integer," +
                    "foreign key(termfk) references (terms.termid)"
                    + ");";
    private static final String DATABASE_CREATE_PROGRAMS_SQL=
            "create table programs" +
                    "(programid integer primary key autoincrement," +
                    "name text not null," +
                    "shortcut text not null" +
                    "for";
    private static final String DATABASE_CREATE_LABS_SQL =
            "create table labs"
                    + " (labid integer primary key autoincrement, "
                    + KEY_NAME         + " text not null, "
                    + "subjfk integer,"
                    +"time text,"
                    +"day text,"
                    +"foreign key (subjfk) references (subjects.subjectid));";

    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    // ==================
    //	Public methods:
    // ==================

    public DBAdapter(Context ctx) {
        myDBHelper = new DatabaseHelper(ctx);
    }

    // Open the database connection.
    public DBAdapter open() {
        db = myDBHelper.getWritableDatabase();
        db.execSQL("PRAGMA foreign_keys=ON;");
        return this;
    }

    // Close the database connection.
    public void close() {
        myDBHelper.close();
    }

    public void insertTerm(String name, int id){
        ContentValues tempVal = new ContentValues();
        tempVal.put("termid",id);
        tempVal.put("name", name);
        db.insert("terms", null,tempVal);
    }

    public void insertProgram(String name, int id){
        ContentValues tempVal = new ContentValues();
        tempVal.put("termid",id);
        tempVal.put("name",name);
        db.insert("programs",null,tempVal);
    }

    public void insertSubject(String name, int id,String time, String day){
        ContentValues tempVal = new ContentValues();
        tempVal.put("termfk",id);
        tempVal.put("name", name);
        tempVal.put("time",time);
        tempVal.put("day",day);
        db.insert("subjects", null,tempVal);
    }

    public void insertLab(String name, int id, String time, String day){
        ContentValues tempVal = new ContentValues();
        tempVal.put("subjfk", id);
        tempVal.put("name",name);
        tempVal.put("time",time);
        tempVal.put("day",day);
        db.insert("labs",null,tempVal);
    }

    // Delete a row from the database, by rowId (primary key)
    private boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(DATABASE_TABLE, where, null) != 0;
    }

    // Delete all records
    public void deleteAll() {
        Cursor c = getAllRows();
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    public Cursor getTerms(){
        String[]  t = {"name"};
        return db.query(true, "terms", t,null,null,null,null,null,null);
    }

    public Cursor getSubjects(int id){
        String[] t = {"name"};
        return db.query(true,"subjects",t,null,null,null,null,null,null);
    }

    // Return all rows in the database.
    private Cursor getAllRows() {
        String where = null;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Get a specific row (by rowId)
    public Cursor getRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRow(long rowId, String name, int studentNum, String favColour) {
        String where = KEY_ROWID + "=" + rowId;

        // [TO_DO_A8]
        // Update data in the row with new fields.
        // Also change the function's arguments to be what you need!
        // Create row's data:
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_NAME, name);
        newValues.put(KEY_STUDENTNUM, studentNum);
        newValues.put(KEY_FAVCOLOUR, favColour);

        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }



    // ==================
    //	Private Helper Classes:
    // ==================

    /**
     * Private class which handles database creation and upgrading.
     * Used to handle low-level database access.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE_TERMS_SQL);
            _db.execSQL(DATABASE_CREATE_SUBJECTS_SQL);
            _db.execSQL(DATABASE_CREATE_LABS_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS terms");
            _db.execSQL("DROP TABLE IF EXISTS subjects");
            _db.execSQL("DROP TABLE IF EXISTS labs");

            // Recreate new database:
            onCreate(_db);
        }
    }
}
