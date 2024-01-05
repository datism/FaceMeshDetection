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
  }

  public static class TriangleEntry implements BaseColumns {
    public static final String TABLE_NAME = "TriangleTbl";
    public static final String COLUMN_PERIMETER = "perimeter";
    public static final String COLUMN_PERIMETER_VAR = "perimeter_variance";
    public static final String COLUMN_FACE_ID = "face_id_ref";
  }

  public static final int DATABASE_VERSION = 1;
  public static final String DATABASE_NAME = "FaceRecognize.db";

  private static final String SQL_CREATE_FACE_ENTRIES =
          "CREATE TABLE " + FaceEntry.TABLE_NAME + " (" +
                  FaceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                  FaceEntry.COLUMN_NAME + " TEXT)";

  private static final String SQL_CREATE_TRIANGLE_ENTRIES =
          "CREATE TABLE " + TriangleEntry.TABLE_NAME + " (" +
                  TriangleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                  TriangleEntry.COLUMN_PERIMETER + " REAL," +
                  TriangleEntry.COLUMN_PERIMETER_VAR + " REAL," +
                  TriangleEntry.COLUMN_FACE_ID + " INTEGER," +
                  " FOREIGN KEY (" + TriangleEntry.COLUMN_FACE_ID + ")" +
                  " REFERENCES " + FaceEntry.TABLE_NAME + "(" + FaceEntry._ID + "))";

  private static final String SQL_DELETE_FACE_ENTRIES =
          "DROP TABLE IF EXISTS " + FaceEntry.TABLE_NAME;

  private static final String SQL_DELETE_TRIANGLE_ENTRIES =
          "DROP TABLE IF EXISTS " + TriangleEntry.TABLE_NAME;

  public FaceHandler(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
//    db.execSQL(SQL_DELETE_TRIANGLE_ENTRIES);
//    db.execSQL(SQL_DELETE_FACE_ENTRIES);
    db.execSQL(SQL_CREATE_FACE_ENTRIES);
    db.execSQL(SQL_CREATE_TRIANGLE_ENTRIES);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL(SQL_DELETE_TRIANGLE_ENTRIES);
    db.execSQL(SQL_DELETE_FACE_ENTRIES);
    onCreate(db);
  }

  public void addFace(Face face) {
    SQLiteDatabase db = this.getWritableDatabase();

    ContentValues faceValues = new ContentValues();
    faceValues.put(FaceEntry.COLUMN_NAME, face.getName());
    long faceID = db.insert(FaceEntry.TABLE_NAME, null, faceValues);

    for (Triangle triangle: face.getTriangles()) {
      ContentValues triangleValue = new ContentValues();
      triangleValue.put(TriangleEntry.COLUMN_PERIMETER, triangle.getPerimeter());
      triangleValue.put(TriangleEntry.COLUMN_PERIMETER_VAR, triangle.getPerimeterVariance());
      triangleValue.put(TriangleEntry.COLUMN_FACE_ID, faceID);
      db.insert(TriangleEntry.TABLE_NAME, null, triangleValue);
    }
  }

//  public List<Face> getFaces(Face face) {
//    SQLiteDatabase db = this.getReadableDatabase();
//
//    String[] columns = {"*"};  // Select all column
//    String selection = FaceEntry.COLUMN_EDW_RATIO + " - " + FaceEntry.COLUMN_EDW_RATIO_VAR + " < ? AND " +
//                        FaceEntry.COLUMN_EDW_RATIO + " + " + FaceEntry.COLUMN_EDW_RATIO_VAR + " > ?";
//    String[] selectionArgs = {String.valueOf(face.getEdwRatio()),
//                              String.valueOf(face.getEdwRatio())};
//
//    Cursor cursor = db.query(
//            FaceEntry.TABLE_NAME,     // The table to query
//            columns,                  // The array of columns to return (pass null to get all)
//            selection,                // The columns for the WHERE clause
//            selectionArgs,            // The values for the WHERE clause
//            null,                     // don't group the rows
//            null,                     // don't filter by row groups
//            null                      // The sort order
//    );
//
//    List<Face> faces = new ArrayList<>();
//    while(cursor.moveToNext()) {
//      faces.add(new Face(cursor.getString(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NAME)),
//              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_EDW_RATIO)),
//              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_EDW_RATIO_VAR)),
//              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NFH_RATIO)),
//              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NFH_RATIO_VAR)),
//              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NFW_RATIO)),
//              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NFW_RATIO_VAR)),
//              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_LFH_RATIO)),
//              cursor.getDouble(cursor.getColumnIndexOrThrow(FaceEntry.COLUMN_LFH_RATIO_VAR))));
//    }
//
//    cursor.close();
//    return faces;
//  }

  public List<Long> getAllFacesID() {
    SQLiteDatabase db = this.getReadableDatabase();
    String[] columns = {FaceEntry._ID};  // Select all column

    Cursor faceCursor = db.query(
            FaceEntry.TABLE_NAME,     // The table to query
            columns,                  // The array of columns to return (pass null to get all)
            null,                     // The columns for the WHERE clause
            null,                     // The values for the WHERE clause
            null,                     // don't group the rows
            null,                     // don't filter by row groups
            null                      // The sort order
    );

    List<Long> ids = new ArrayList<>();

    while(faceCursor.moveToNext()) {
      long faceID = faceCursor.getLong(faceCursor.getColumnIndexOrThrow(FaceEntry._ID));
      ids.add(faceID);
    }

    faceCursor.close();
    return ids;
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
      long faceID = faceCursor.getLong(faceCursor.getColumnIndexOrThrow(FaceEntry._ID));
      String faceName = faceCursor.getString(faceCursor.getColumnIndexOrThrow(FaceEntry.COLUMN_NAME));

      Cursor triangleCursor = db.query(
              TriangleEntry.TABLE_NAME,
              columns,
              TriangleEntry.COLUMN_FACE_ID + "=?",
              new String[]{Long.toString(faceID)},
              null,
              null,
              null
      );

      List<Triangle> triangles = new ArrayList<>();
      while (triangleCursor.moveToNext()) {
        triangles.add(new Triangle(
                triangleCursor.getDouble(triangleCursor.getColumnIndexOrThrow(TriangleEntry.COLUMN_PERIMETER)),
                triangleCursor.getDouble(triangleCursor.getColumnIndexOrThrow(TriangleEntry.COLUMN_PERIMETER_VAR))));
      }

      triangleCursor.close();

      faces.add(new Face(faceName, triangles));
    }

    faceCursor.close();
    return faces;
  }
}
