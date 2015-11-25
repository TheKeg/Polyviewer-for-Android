package com.fusedresistance.polyviewer.scene;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;

public class LightPresets extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 3;
	public static final String COLUMN_ID = "PresetName";
	public static final String COLUMN_LIGHT_COUNT = "LightCount";
	public static final String COLUMN_1_Y_AXIS = "OneYAxis";
	public static final String COLUMN_1_Z_AXIS = "OneZAxis";
	public static final String COLUMN_1_DIFFUSE_COLOUR = "OneDiffuse";
	public static final String COLUMN_1_SPECULAR_COLOUR = "OneSpecular";
	
	public static final String COLUMN_2_Y_AXIS = "TwoYAxis";
	public static final String COLUMN_2_Z_AXIS = "TwoZAxis";
	public static final String COLUMN_2_DIFFUSE_COLOUR = "TwoDiffuse";
	public static final String COLUMN_2_SPECULAR_COLOUR = "TwoSpecular";
	
	public static final String COLUMN_3_Y_AXIS = "ThreeYAxis";
	public static final String COLUMN_3_Z_AXIS = "ThreeZAxis";
	public static final String COLUMN_3_DIFFUSE_COLOUR = "ThreeDiffuse";
	public static final String COLUMN_3_SPECULAR_COLOUR = "ThreeSpecular";
	
	private static String[] selectColumns = new String[] { COLUMN_LIGHT_COUNT, COLUMN_1_Y_AXIS, COLUMN_1_Z_AXIS, 
														   COLUMN_1_DIFFUSE_COLOUR, COLUMN_1_SPECULAR_COLOUR,
														   COLUMN_2_Y_AXIS, COLUMN_2_Z_AXIS, 
														   COLUMN_2_DIFFUSE_COLOUR, COLUMN_2_SPECULAR_COLOUR, 
														   COLUMN_3_Y_AXIS, COLUMN_3_Z_AXIS, 
														   COLUMN_3_DIFFUSE_COLOUR, COLUMN_3_SPECULAR_COLOUR };
	
//	private String databaseName = "";
	private String tableName = "";
	private SQLiteDatabase lightPresets;
	
	public LightPresets(Context context, String name)
	{
		super(context, name, null, DATABASE_VERSION);
		
		tableName = name.substring(0, name.lastIndexOf("."));
//		databaseName = name;
		
//		context.deleteDatabase(name);
	}

	@Override
	public void onCreate(SQLiteDatabase database)
	{
		lightPresets = database;
//		String deleteString = "DROP TABLE IF EXISTS " +  tableName;
		String createString = "CREATE TABLE " + tableName + "(" + COLUMN_ID + " TEXT NOT NULL, " + 
																  COLUMN_LIGHT_COUNT + " INTEGER, " +
																  COLUMN_1_Y_AXIS + " FLOAT, " + 
																  COLUMN_1_Z_AXIS + " FLOAT, " +
																  COLUMN_1_DIFFUSE_COLOUR + " FLOAT, " +
																  COLUMN_1_SPECULAR_COLOUR + " FLOAT, " +
																  COLUMN_2_Y_AXIS + " FLOAT, " + 
																  COLUMN_2_Z_AXIS + " FLOAT, " +
																  COLUMN_2_DIFFUSE_COLOUR + " FLOAT, " +
																  COLUMN_2_SPECULAR_COLOUR + " FLOAT, " +
																  COLUMN_3_Y_AXIS + " FLOAT, " + 
																  COLUMN_3_Z_AXIS + " FLOAT, " +
																  COLUMN_3_DIFFUSE_COLOUR + " FLOAT, " +
																  COLUMN_3_SPECULAR_COLOUR + " FLOAT );";
		
//		db.execSQL(deleteString);
		database.execSQL(createString);
		
		float[] lightVals = new float[13];
		
		lightVals[0] = 3;
		lightVals[1] = -45;
		lightVals[2] = 30;
		lightVals[3] = Color.rgb(255, 255, 255);
		lightVals[4] = Color.rgb(200, 200, 200);
		lightVals[5] = 30;
		lightVals[6] = 15;
		lightVals[7] = Color.rgb(180, 180, 180);
		lightVals[8] = Color.rgb(140, 140, 140);
		lightVals[9] = 150;
		lightVals[10] = 45;
		lightVals[11] = Color.rgb(220, 220, 220);
		lightVals[12] = Color.rgb(220, 220, 220);
		
		addPreset("Three Point Lighting", lightVals);
		
		lightVals[0] = 1;
		lightVals[1] = 0.0f;
		lightVals[2] = 0.0f;
		lightVals[3] = Color.rgb(255, 255, 255);
		lightVals[4] = Color.rgb(200, 200, 200);
		addPreset("Direct", lightVals);
		
		lightVals[2] = 45.0f;
		addPreset("Direct Downward", lightVals);
		
		lightVals[2] = -45.0f;
		addPreset("Direct Upward", lightVals);
		
		lightVals[1] = -45.0f;
		lightVals[2] = 0.0f;
		addPreset("Left", lightVals);
		
		lightVals[2] = 45.0f;
		addPreset("Left Downward", lightVals);
		
		lightVals[2] = -45.0f;
		addPreset("Left Upward", lightVals);
		
		lightVals[1] = 45.0f;
		lightVals[2] = 0.0f;
		addPreset("Right", lightVals);
		
		lightVals[2] = 45.0f;
		addPreset("Right Downward", lightVals);
		
		lightVals[2] = -45.0f;
		addPreset("Right Upward", lightVals);		
		
//		if(database.isOpen())
//			database.close();
//		
//		db = null;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}
	
	public float[] getPreset(final String presetName)
	{
		float[] output = new float[0];
		SQLiteDatabase db = null;
		
		try
		{
			db = this.getReadableDatabase();
			
			Cursor cursor = db.query(tableName, selectColumns, COLUMN_ID + "=?", new String[] { presetName }, null, null, null);
			
			int index = 0;
			
			if(cursor.moveToFirst())
			{
				int cursorIndex = 0;
				int lightCount = cursor.getInt(cursorIndex++);//cursor.getColumnIndex(COLUMN_LIGHT_COUNT));
				
				output = new float[lightCount * 4];
				
				do
				{
					output[index++] = cursor.getFloat(cursorIndex++);
				}
				while(index < output.length);//cursor.moveToNext());
			}
			
			cursor.close();
			db.close();
		}
		catch(Exception e)
		{
			if(db != null)
				db.close();
			
			Log.e("LIGHT DB", e.getLocalizedMessage());
		}
		
		return output;
	}

	public boolean addPreset(final String presetName, final float[] values)
	{
		if(values.length < 13)
			return false;
		
		ContentValues cv = new ContentValues();
		String[] selectColumns = new String[] { COLUMN_ID, COLUMN_LIGHT_COUNT };
		boolean newAdd = false;
		
		try
		{
			if(lightPresets == null)
			{
				lightPresets = this.getWritableDatabase();
				newAdd = true;
			}
			else if(!lightPresets.isOpen())
			{
				lightPresets = this.getWritableDatabase();
				newAdd = true;
			}
			
			cv.put(COLUMN_ID, presetName);
			cv.put(COLUMN_LIGHT_COUNT, values[0]);
			cv.put(COLUMN_1_Y_AXIS, values[1]);
			cv.put(COLUMN_1_Z_AXIS, values[2]);
			cv.put(COLUMN_1_DIFFUSE_COLOUR, values[3]);
			cv.put(COLUMN_1_SPECULAR_COLOUR, values[4]);
			cv.put(COLUMN_2_Y_AXIS, values[5]);
			cv.put(COLUMN_2_Z_AXIS, values[6]);
			cv.put(COLUMN_2_DIFFUSE_COLOUR, values[7]);
			cv.put(COLUMN_2_SPECULAR_COLOUR, values[8]);
			cv.put(COLUMN_3_Y_AXIS, values[9]);
			cv.put(COLUMN_3_Z_AXIS, values[10]);
			cv.put(COLUMN_3_DIFFUSE_COLOUR, values[11]);
			cv.put(COLUMN_3_SPECULAR_COLOUR, values[12]);
			
			Cursor cursor = lightPresets.query(tableName, selectColumns, COLUMN_ID + "=?", new String[] { presetName }, null, null, null);
			
			if(cursor.getCount() > 0 && cursor.moveToFirst())
				lightPresets.update(tableName, cv, COLUMN_ID + "=?", new String[] { presetName });
			else
				lightPresets.insert(tableName, null, cv);
			
			cursor.close();
			
			if(newAdd)
			{
				lightPresets.close();
				lightPresets = null;
			}
		}
		catch(Exception e)
		{
			Log.d("LIGHT PRESET ADD", e.getMessage());
			
			return false;
		}
		
		return true;
	}
	
	public String[] getList()
	{
		String[] output = null;
		SQLiteDatabase db = null;
		
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
			if(db != null)
				db.close();
			
			Log.e("LIGHT GET LIST", e.getLocalizedMessage());
		}
		
		return output;
	}
	
	public void open()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.close();
		lightPresets = null;
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
         
         Log.e("LIGHT DB", e.getLocalizedMessage());
      }      
   }
}
