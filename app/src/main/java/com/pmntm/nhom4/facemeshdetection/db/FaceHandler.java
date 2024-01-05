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
    public static final String COLUMN_AVG_DIST = "average_distance";
  }

  public static final int DATABASE_VERSION = 1;
  public static final String DATABASE_NAME = "FaceRecognize.db";


  private static String getSqlCreate() {
    StringBuilder sql_create = new StringBuilder("CREATE TABLE " + FaceEntry.TABLE_NAME + " (" +
            FaceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            FaceEntry.COLUMN_NAME + " TEXT," +
            FaceEntry.COLUMN_AVG_DIST + " REAL");

    for(int i = 0; i < 898; i++) {
      sql_create.append(",").append('`').append(i).append('`').append(" REAL");
    }

    sql_create.append(")");
    return sql_create.toString();
  }

  private static final String SQL_DELETE_FACE_ENTRIES =
          "DROP TABLE IF EXISTS " + FaceEntry.TABLE_NAME;


  public FaceHandler(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
//    db.execSQL(SQL_DELETE_TRIANGLE_ENTRIES);
//    db.execSQL(SQL_DELETE_FACE_ENTRIES);
    String sql_crate = getSqlCreate();
    db.execSQL(sql_crate);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL(SQL_DELETE_FACE_ENTRIES);
    onCreate(db);
  }

  public void addFace(Face face) {
    SQLiteDatabase db = this.getWritableDatabase();

    ContentValues values = new ContentValues();

    values.put(FaceEntry.COLUMN_NAME, face.getName());
    values.put(FaceEntry.COLUMN_AVG_DIST, face.getAverageDistance());

    List<Double> perimeterRatios = face.getPerimeterRatio();
    for (int i = 0; i < perimeterRatios.size(); i++) {
      values.put('`' + Integer.toString(i) + '`', perimeterRatios.get(i));
    }

    db.insert(FaceEntry.TABLE_NAME, null, values);
  }

  public List<Face> getAllFaces() {
    SQLiteDatabase db = this.getReadableDatabase();
    String[] columns = {"*"};  // Select all column

    Cursor faceCursor = db.query(
            FaceEntry.TABLE_NAME,     // The table to query
            columns,                  // The array of columns to return (pass null to get all)
            null,                     // The columns for the WHERE clause
            null,                     // The values for the WHERE clause
            null,                     // don't group the rows
            null,                     // don't filter by row groups
            null                      // The sort order
    );

    List<Face> faces = new ArrayList<>();
    while(faceCursor.moveToNext()) {
      String faceName = faceCursor.getString(faceCursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NAME));
      double averageDist = faceCursor.getDouble(faceCursor.getColumnIndexOrThrow(FaceEntry.COLUMN_AVG_DIST));

      List<Double> perimeterRatios = new ArrayList<>();
      for(int i = 0; i < 898; i++) {
        perimeterRatios.add(faceCursor.getDouble(
                faceCursor.getColumnIndexOrThrow(Integer.toString(i))
        ));
      }

      faces.add(new Face(faceName, averageDist, perimeterRatios));
    }

    faceCursor.close();
    return faces;
  }
}
