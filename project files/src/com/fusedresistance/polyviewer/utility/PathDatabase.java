package com.fusedresistance.polyviewer.utility;

import com.fusedresistance.polyviewer.utility.FileLoader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PathDatabase extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 3;
	public static final String COLUMN_PATH = "DirectoryPath";
	
	private String tableName = "";
	private SQLiteDatabase db = null;
	
	public PathDatabase(Context context, String name)
	{
		super(context, name, null, DATABASE_VERSION);
		
		tableName = name.substring(0, name.lastIndexOf("."));
	}

	@Override
	public void onCreate(SQLiteDatabase database)
	{
		db = database;
		String createString = "CREATE TABLE " + tableName + "(" + COLUMN_PATH + " TEXT NOT NULL);";
		
		db.execSQL(createString);
		
		String[] pathList = new String[0];
		
		try
		{
			pathList = FileLoader.retrieveDirectories("polyviewer");
		}
		catch(Exception e)
		{
			Log.e("DIRECTORY ERROR", e.getMessage());
		}
		
		if(pathList != null)
		{
			for(int i = 0; i < pathList.length; ++i)
				addPreset(pathList[i]);
		}
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}
	
	public boolean addPreset(final String pathName)
	{
		if(pathName.length() == 2)
			return false;
		
		ContentValues cv = new ContentValues();
		boolean newAdd = false;
		
		try
		{
			String[] selectColumns = new String[] { COLUMN_PATH };
			
			if(db == null)
			{
				db = getWritableDatabase();
				newAdd = true;
			}
			
			cv.put(COLUMN_PATH, pathName);
			
			Cursor cursor = db.query(tableName, selectColumns, COLUMN_PATH + "=?", new String[] { pathName }, null, null, null);
			
			if(cursor.getCount() == 0)
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
			Log.e("PATH ADD ITEM", e.getLocalizedMessage());
			
			return false;
		}
		
		return true;
	}

	public String[] getList()
	{
		String[] output = null;//new String[1];
		SQLiteDatabase database = getWritableDatabase();
		
		try
		{
			String[] selectColumns = new String[] { COLUMN_PATH };
			
			database = this.getReadableDatabase();
			Cursor cursor = database.query(tableName, selectColumns, null, null, null, null, null);
			
			if(cursor.getCount() > 0)
				output = new String[cursor.getCount()];
			
			int index = 0;
			
			if(cursor.moveToFirst() && output != null)
			{
				do
				{
					int columnIndex = cursor.getColumnIndex(COLUMN_PATH);
					output[index++] = cursor.getString(columnIndex);
				}
				while(cursor.moveToNext());
			}
			
			cursor.close();
			database.close();
		}
		catch(Exception e)
		{
			database.close();
			Log.e("PATH GET LIST", e.getLocalizedMessage());
		}
		
		return output;
	}

	public void open()
	{
		db = this.getWritableDatabase();
		db.close();
		db = null;
	}

   public void clear()
   {
      if(db == null)
         db = getWritableDatabase();
         
      db.delete(tableName, null, null);
      db.close();
      db = null;
   }
}
