package com.pmntm.nhom4.facemeshdetection.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class FaceHandler extends SQLiteOpenHelper {
  public static class FaceEntry implements BaseColumns {
    public static final String TABLE_NAME = "FaceTbl";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EDW_RATIO = "eyes_dist_width_ratio";
    public static final String COLUMN_EDW_RATIO_VAR = "eyes_dist_width_ratio_variance";
    public static final String COLUMN_NFH_RATIO = "nose_face_height_ratio";
    public static final String COLUMN_NFH_RATIO_VAR = "nose_face_height_ratio_variance";
    public static final String COLUMN_NFW_RATIO = "nose_face_width_ratio";
    public static final String COLUMN_NFW_RATIO_VAR = "nose_face_width_ratio_variacne";
    public static final String COLUMN_LFH_RATIO = "lip_face_height_ratio";
    public static final String COLUMN_LFH_RATIO_VAR = "lip_face_height_ratio_variance";
  }

  public static final int DATABASE_VERSION = 1;
  public static final String DATABASE_NAME = "FaceRecognize.db";

  private static final String SQL_CREATE_ENTRIES =
          "CREATE TABLE " + FaceEntry.TABLE_NAME + " (" +
                  FaceEntry._ID + " INTEGER PRIMARY KEY," +
                  FaceEntry.COLUMN_NAME + " TEXT," +
                  FaceEntry.COLUMN_EDW_RATIO + " REAL," +
                  FaceEntry.COLUMN_EDW_RATIO_VAR + " REAL," +
                  FaceEntry.COLUMN_NFH_RATIO + " REAL," +
                  FaceEntry.COLUMN_NFH_RATIO_VAR + " REAL," +
                  FaceEntry.COLUMN_NFW_RATIO + " REAL," +
                  FaceEntry.COLUMN_NFW_RATIO_VAR + " REAL," +
                  FaceEntry.COLUMN_LFH_RATIO + " REAL," +
                  FaceEntry.COLUMN_LFH_RATIO_VAR + " REAL)";

  private static final String SQL_DELETE_ENTRIES =
          "DROP TABLE IF EXISTS " + FaceEntry.TABLE_NAME;

  public FaceHandler(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_ENTRIES);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL(SQL_DELETE_ENTRIES);
    onCreate(db);
  }

  public long addFace(Face face) {
    SQLiteDatabase db = this.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(FaceEntry.COLUMN_NAME, face.getName());
    values.put(FaceEntry.COLUMN_EDW_RATIO, face.getEdwRatio());
    values.put(FaceEntry.COLUMN_EDW_RATIO_VAR, face.getEdwRatioVariance());
    values.put(FaceEntry.COLUMN_NFH_RATIO, face.getNfhRatio());
    values.put(FaceEntry.COLUMN_NFH_RATIO_VAR, face.getNfhRatioVariance());
    values.put(FaceEntry.COLUMN_NFW_RATIO, face.getNfwRatio());
    values.put(FaceEntry.COLUMN_NFW_RATIO_VAR, face.getNfwRatioVariance());
    values.put(FaceEntry.COLUMN_LFH_RATIO, face.getNfwRatio());
    values.put(FaceEntry.COLUMN_LFH_RATIO_VAR, face.getNfwRatioVariance());

    return db.insert(FaceEntry.TABLE_NAME, null, values);
  }

  public List<Face> getFaces(Face face) {
    SQLiteDatabase db = this.getReadableDatabase();

    String[] columns = {"*"};  // Select all column
    String selection = FaceEntry.COLUMN_EDW_RATIO + " - " + FaceEntry.COLUMN_EDW_RATIO_VAR + " < ? AND " +
                        FaceEntry.COLUMN_EDW_RATIO + " + " + FaceEntry.COLUMN_EDW_RATIO_VAR + " > ?";
    String[] selectionArgs = {String.valueOf(face.getEdwRatio()),
                              String.valueOf(face.getEdwRatio())};

    Cursor cursor = db.query(
            FaceEntry.TABLE_NAME,     // The table to query
            columns,                  // The array of columns to return (pass null to get all)
            selection,                // The columns for the WHERE clause
            selectionArgs,            // The values for the WHERE clause
            null,                     // don't group the rows
            null,                     // don't filter by row groups
            null                      // The sort order
    );

    List<Face> faces = new ArrayList<>();
    while(cursor.moveToNext()) {
      faces.add(new Face(cursor.getString(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NAME)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_EDW_RATIO)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_EDW_RATIO_VAR)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NFH_RATIO)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NFH_RATIO_VAR)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NFW_RATIO)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NFW_RATIO_VAR)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_LFH_RATIO)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_LFH_RATIO_VAR))));
    }

    cursor.close();
    return faces;
  }

  public List<Face> getAllFaces() {
    SQLiteDatabase db = this.getReadableDatabase();
    String[] columns = {"*"};  // Select all column

    Cursor cursor = db.query(
            FaceEntry.TABLE_NAME,     // The table to query
            columns,                  // The array of columns to return (pass null to get all)
            null,                     // The columns for the WHERE clause
            null,                     // The values for the WHERE clause
            null,                     // don't group the rows
            null,                     // don't filter by row groups
            null                      // The sort order
    );

    List<Face> faces = new ArrayList<>();
    while(cursor.moveToNext()) {
      faces.add(new Face(cursor.getString(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NAME)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_EDW_RATIO)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_EDW_RATIO_VAR)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NFH_RATIO)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NFH_RATIO_VAR)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NFW_RATIO)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NFW_RATIO_VAR)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_LFH_RATIO)),
              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_LFH_RATIO_VAR))));
    }

    cursor.close();
    return faces;
  }
}
