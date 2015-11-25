package com.fusedresistance.polyviewer.scene;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CameraPresets extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 3;
	public static final String COLUMN_ID = "PresetName";
	public static final String COLUMN_Y_AXIS = "YAxis";
	public static final String COLUMN_Z_AXIS = "ZAxis";
	
//	private String databaseName = "";
	private String tableName = "";
	private SQLiteDatabase db = null;
	
	public CameraPresets(Context context, String name)
	{
		super(context, name, null, DATABASE_VERSION);
		
		tableName = name.substring(0, name.lastIndexOf("."));
//		databaseName = name;
		
//		context.deleteDatabase(name);
	}

	@Override
	public void onCreate(SQLiteDatabase database)
	{
		db = database;
//		String deleteString = "DROP TABLE IF EXISTS " +  tableName;
		String createString = "CREATE TABLE " + tableName + "(" + COLUMN_ID + " TEXT NOT NULL, " + 
																  COLUMN_Y_AXIS + " FLOAT, " + 
																  COLUMN_Z_AXIS + " FLOAT);";
		
//		db.execSQL(deleteString);
		db.execSQL(createString);
//		db.close();
		
		addPreset("Direct", new float[] { 0.0f, 0.0f } );
		addPreset("Direct Downward", new float[] { 0.0f, -45.0f } );
		addPreset("Direct Upward", new float[] { 0.0f, 45.0f } );
		
		addPreset("Left", new float[] { -45.0f, 0.0f } );
		addPreset("Left Downward", new float[] { -45.0f, -45.0f } );
		addPreset("Left Upward", new float[] { -45.0f, 45.0f } );
		
		addPreset("Right", new float[] { 45.0f, 0.0f } );
		addPreset("Right Downward", new float[] { 45.0f, -45.0f } );
		addPreset("Right Upward", new float[] { 45.0f, 45.0f } );
		
//		if(db.isOpen())
//			db.close();
//		
//		db = null;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}
	
	public float[] getPreset(final String presetName)
	{
		float[] output = new float[2];

		try
		{
			String[] selectColumns = new String[] { COLUMN_Y_AXIS, COLUMN_Z_AXIS };
			
			db = this.getReadableDatabase();
			Cursor cursor = db.query(tableName, selectColumns, COLUMN_ID + "=?", new String[] { presetName }, null, null, null);
			
			if(cursor.getCount() > 0)
				output = new float[2];
			
			if(cursor.moveToFirst())
			{
				output[0] = cursor.getFloat(cursor.getColumnIndex(COLUMN_Y_AXIS));
				output[1] = cursor.getFloat(cursor.getColumnIndex(COLUMN_Z_AXIS));
			}
			
			cursor.close();
			db.close();
		}
		catch(Exception e)
		{
			db.close();
			
			Log.e("CAMERA PRESET", e.getLocalizedMessage());
		}
		
		return output;
	}
	
	public boolean addPreset(final String presetName, final float[] values)
	{
		if(values.length < 2)
			return false;
		
		ContentValues cv = new ContentValues();
		boolean newAdd = false;
		
		try
		{
			String[] selectColumns = new String[] { COLUMN_Y_AXIS, COLUMN_Z_AXIS };
			
			if(db == null)
			{
				db = this.getWritableDatabase();
				newAdd = true;
			}
			else if(!db.isOpen())
			{
				db = this.getWritableDatabase();
				newAdd = true;
			}
			
			cv.put(COLUMN_ID, presetName);
			cv.put(COLUMN_Y_AXIS, values[0]);
			cv.put(COLUMN_Z_AXIS, values[1]);
			
			Cursor cursor = db.query(tableName, selectColumns, COLUMN_ID + "=?", new String[] { presetName }, null, null, null);
			
			if(cursor.getCount() > 0)
				db.update(tableName, cv, COLUMN_ID + "=?", new String[] { presetName });
			else
				db.insert(tableName, null, cv);
			
			cursor.close();
			
			if(newAdd)
			{
				db.close();
				db = null;
			}
		}
		catch(Exception e)
		{
//			db.close();
			
			Log.e("CAMERA ADD ITEM", e.getLocalizedMessage());
			
			return false;
		}
		
		return true;
	}

	public String[] getList()
	{
		String[] output = null;//new String[1];
		
		try
		{
			String[] selectColumns = new String[] { COLUMN_ID };
			
			db = this.getReadableDatabase();
			Cursor cursor = db.query(tableName, selectColumns, null, null, null, null, null);
			
			if(cursor.getCount() > 0)
				output = new String[cursor.getCount()];
			
			int index = 0;
			
			if(cursor.moveToFirst() && output != null)
			{
				do
				{
					int columnIndex = cursor.getColumnIndex(COLUMN_ID);
					output[index++] = cursor.getString(columnIndex);
				}
				while(cursor.moveToNext());
			}
			
			cursor.close();
			db.close();
		}
		catch(Exception e)
		{
			db.close();
			Log.e("CAMERA GET LIST", e.getLocalizedMessage());
		}
		
		return output;
	}

	public void open()
	{
		db = this.getWritableDatabase();
		db.close();
		db = null;
	}

   public void removePreset(String presetName)
   {
SQLiteDatabase db = null;
      
      try
      {
         db = this.getReadableDatabase();
         
         db.delete(tableName, COLUMN_ID + "=?", new String[] { presetName });
         db.close();
      }
      catch(Exception e)
      {
         if(db != null)
            db.close();
         
         Log.e("CAMERA DB", e.getLocalizedMessage());
      }
   }
}
