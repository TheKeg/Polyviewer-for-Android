package com.fusedresistance.polyviewer.pro.wallpaper;

import java.io.File;

import com.fusedresistance.polyviewer.colorpicker.ColourPickerPreference;
import com.fusedresistance.polyviewer.pro.R;
import com.fusedresistance.polyviewer.pro.PolyviewerActivity;
import com.fusedresistance.polyviewer.scene.LightPresets;
import com.fusedresistance.polyviewer.scene.Scene;
import com.fusedresistance.polyviewer.settings.Preferences;
import com.fusedresistance.polyviewer.settings.SeekBarPreference;
import com.fusedresistance.polyviewer.utility.FileLoader;
import com.fusedresistance.polyviewer.utility.PathDatabase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.ListView;

public class WallpaperSettings extends PreferenceActivity 
	implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener, 
			   Preference.OnPreferenceChangeListener, OnMultiChoiceClickListener, OnClickListener
{
   private boolean[] enabledTextures;
	private LightPresets lightPresets;
	private String dialogTitle;
	private String[] textureList;
   private String[] lightPresetList;
	private String[] modelPaths;
	private String[] availableModels;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstance)
	{
	   super.onCreate(savedInstance);
	   
		SharedPreferences sharedPrefs = getSharedPreferences(Preferences.LIVE_WALLPAPER_PREFS, 0);
		Editor            editor      = sharedPrefs.edit();
		Preference        pref;//        = null;
		boolean           bgEnabled;//   = false;
		
//		setTheme(R.style.Theme_Sherlock_NoActionBar);

		getPreferenceManager().setSharedPreferencesName(Preferences.LIVE_WALLPAPER_PREFS);
		addPreferencesFromResource(R.xml.wallpaper_preferences);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		pref = findPreference(Preferences.PREF_SHOW_BG);
		pref.setOnPreferenceClickListener(this);
		bgEnabled = getPreferenceManager().getSharedPreferences().getBoolean(Preferences.PREF_SHOW_BG, false);

		pref = findPreference(Preferences.PREF_TEXTURE);
		pref.setOnPreferenceClickListener(this);

		pref = findPreference(Preferences.PREF_RESET);
		pref.setOnPreferenceClickListener(this);

		pref = findPreference(Preferences.PREF_LIGHT_PRESET);
		pref.setOnPreferenceClickListener(this);

		pref = findPreference(Preferences.PREF_SELECT_BG);
		pref.setOnPreferenceClickListener(this);
		pref.setEnabled(bgEnabled);

		String imgLocation = this.getPreferenceManager().getSharedPreferences().getString(Preferences.PREF_BG_IMG_LOC, "");

		if(imgLocation.length() == 0)
			pref.setSummary("Choose an image from the gallery");
		else
			pref.setSummary(imgLocation);

		pref = findPreference(Preferences.PREF_COLOUR_PICKER);
		pref.setOnPreferenceChangeListener(this);
		pref.setEnabled(!bgEnabled);

		lightPresets = new LightPresets(this, Preferences.LIGHT_DATABASE);//"LightPresets.db");
		lightPresets.open();

		lightPresetList = lightPresets.getList();

		try
		{
			String[]     polyviewerDirs;// = new String[0];
        	long         currTime       = System.currentTimeMillis();
         long         dbTime         = sharedPrefs.getLong(Preferences.PREF_DATABASE_UPDATE, Preferences.DB_UPDATE);
         PathDatabase pathDB         = new PathDatabase(this, Preferences.PATH_DATABASE);

         pathDB.open();

         if(dbTime <= Preferences.DB_UPDATE)
         {
            polyviewerDirs = FileLoader.retrieveDirectories("polyviewer");
            editor.putLong(Preferences.PREF_DATABASE_UPDATE, currTime);

            pathDB.clear();

            for(String directory : polyviewerDirs)//int i = 0; i < polyviewerDirs.length; ++i)
            {
               pathDB.addPreset(directory);
               Log.e("DIRECTORY", directory);
            }
         }
         else
            polyviewerDirs = pathDB.getList();//

         pathDB.close();

        	modelPaths = FileLoader.retrieveMeshDirectories(polyviewerDirs);
    		availableModels = FileLoader.parseString(modelPaths);
		}
		catch(Exception e)
		{
			if(e.getLocalizedMessage() != null)
				Log.d("MODEL ERROR", e.getLocalizedMessage());
        	else if(e.getMessage() != null)
        		Log.e("MODEL ERROR", e.getMessage());
        	else
        		Log.e("MODEL ERROR", e.toString());
		}

		ListPreference listPref = (ListPreference)findPreference(Preferences.PREF_MESH_SELECTION);

		if(availableModels != null)
		{
			listPref.setEntries(availableModels);
			listPref.setEntryValues(modelPaths);
			listPref.setOnPreferenceChangeListener(this);
			listPref.setDefaultValue(sharedPrefs.getString(Preferences.PREF_MESH_SELECTION, modelPaths[0]));
		}

		listPref = (ListPreference)findPreference(Preferences.PREF_DISPLAY_MODES);
		listPref.setEntries(R.array.displayModes);
		listPref.setEntryValues(R.array.displayModeValues);
		listPref.setOnPreferenceChangeListener(this);
		this.onPreferenceChange(listPref, sharedPrefs.getString(Preferences.PREF_DISPLAY_MODES, "0"));

		listPref = (ListPreference)findPreference(Preferences.PREF_ROTATION_DIRECTION);
		listPref.setEntries(R.array.rotationDirection);
		listPref.setEntryValues(R.array.rotationValues);
		listPref.setOnPreferenceChangeListener(this);
		this.onPreferenceChange(listPref, sharedPrefs.getString(Preferences.PREF_ROTATION_DIRECTION, "0"));

		textureList = getResources().getStringArray(R.array.textureModes);
		enabledTextures = new boolean[textureList.length];

		for(int i = 2; i < enabledTextures.length - 1; i++)
			enabledTextures[i] = true;

		editor.putBoolean(Preferences.PREF_ENABLE_ROTATION, true);
		editor.commit();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onDestroy()
	{
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		
		super.onDestroy();
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		String prefKey = preference.getKey();
		
		if(prefKey.equals(Preferences.PREF_SELECT_BG))
		{
			Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			
			try
			{
				startActivityForResult(intent, 1);
			}
			catch(Exception e)
			{
				Log.e("INTENT ERROR", e.toString());
			}
		}
		else if(prefKey.equals(Preferences.PREF_SHOW_BG))
		{
			CheckBoxPreference checkPref = (CheckBoxPreference) preference;
			
			if(checkPref.isChecked())
			{
				findPreference(Preferences.PREF_SELECT_BG).setEnabled(true);
				findPreference(Preferences.PREF_COLOUR_PICKER).setEnabled(false);
			}
			else
			{
				findPreference(Preferences.PREF_COLOUR_PICKER).setEnabled(true);
				
				Preference pref = findPreference(Preferences.PREF_SELECT_BG);//
				pref.setEnabled(false);
				pref.setSummary("Choose an image from the gallery");
				
				SharedPreferences sharedPrefs = this.getPreferenceManager().getSharedPreferences();
				Editor editor = sharedPrefs.edit();
				
				editor.putString(Preferences.PREF_BG_IMG_LOC, "");
				editor.commit();
			}
		}
		else if(prefKey.equals(Preferences.PREF_RESET))
		{
			CheckBoxPreference checkPref = (CheckBoxPreference)findPreference(Preferences.PREF_SHOW_BG);
			checkPref.setChecked(false);
						
//			checkPref = (CheckBoxPreference)findPreference(Preferences.PREF_ORBIT_CAMERA);
//			checkPref.setChecked(false);
//			
//			checkPref = (CheckBoxPreference)findPreference(Preferences.PREF_ROTATION_DIR);
//			checkPref.setChecked(false);
//			
//			checkPref = (CheckBoxPreference)findPreference(Preferences.PREF_ENABLE_FPS_DISPLAY);
//			checkPref.setChecked(true);
			
			ColourPickerPreference colourPref = (ColourPickerPreference)findPreference(Preferences.PREF_COLOUR_PICKER);
			colourPref.onColorChanged(ColourPickerPreference.colourStrToInt("#FF333333"));
			
			ListPreference listPref = (ListPreference)findPreference(Preferences.PREF_FPS);
			listPref.setValueIndex(0);
			
			listPref = (ListPreference)findPreference(Preferences.PREF_ROTATION_DIRECTION);
			listPref.setValueIndex(0);
			
			SharedPreferences sharedPrefs = this.getPreferenceManager().getSharedPreferences();
			Editor editor = sharedPrefs.edit();
			
			editor.putBoolean(Preferences.PREF_MOTION, false);
			editor.putString(Preferences.PREF_FPS, "16");
			editor.putString(Preferences.PREF_BG_IMG_LOC, "");
			editor.putInt(Preferences.PREF_BRIGHTNESS, 100);
			editor.putInt(Preferences.PREF_SPIN_SPEED, 2);
			editor.putInt(Preferences.PREF_MOVEMENT_AMOUNT, 45);
			editor.putString(Preferences.PREF_SHADER_NAME, "Material");
			editor.putInt(Preferences.PREF_EMISSIVE, 100);
			editor.putInt(Preferences.PREF_CAMERA_FOV, 90);
			editor.putBoolean(Preferences.PREF_ENABLE_ROTATION, false);
			editor.putInt(Preferences.PREF_SHADER_FLAGS, Scene.DEFINE_DIFFUSE | Scene.DEFINE_NORMAL | Scene.DEFINE_SPECULAR);
			editor.commit();
		}
		else if(prefKey.equals(Preferences.PREF_TEXTURE))
		{
			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, Preferences.THEME));
	      alertBuilder.setTitle("Select Textures");
	      alertBuilder.setMultiChoiceItems(textureList, enabledTextures, this);
	      alertBuilder.setPositiveButton("OK", this);
	      alertBuilder.setNegativeButton("Cancel", this);
	      AlertDialog textureAlert = alertBuilder.create();

	      setupTextureList(textureAlert.getListView());
			textureAlert.show();

		}
		else if(prefKey.equals(Preferences.PREF_LIGHT_PRESET))
		{
			if(lightPresetList == null)
				return false;
			
			String selected = getPreferenceManager().getSharedPreferences().getString(Preferences.PREF_LIGHT_PRESET, "Direct Downward");
			int index = 0;
			
			for(index = 0; index < lightPresetList.length; ++index)
			{
				if(lightPresetList[index].equals(selected))
					break;
			}
			
			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, Preferences.THEME));
	      alertBuilder.setTitle("Select Light Preset");
	      alertBuilder.setSingleChoiceItems(lightPresetList, index, this);
	      AlertDialog selectionAlert = alertBuilder.create();
	        
	      if(selectionAlert != null)
	        	selectionAlert.show();
	        
	      dialogTitle  = "LightPreset";
	        
	      return true;
		}
		
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == RESULT_OK)
		{
			Uri uri = data.getData();
			String[] projection = {MediaStore.Images.Media.DATA};  
			  
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);  
            cursor.moveToFirst();  

            int columnIndex = cursor.getColumnIndex(projection[0]);  
            String imgLocation = cursor.getString(columnIndex);  
            cursor.close();  
			
			SharedPreferences sharedPrefs = this.getPreferenceManager().getSharedPreferences();
			Editor editor = sharedPrefs.edit();
			
			editor.putString(Preferences.PREF_BG_IMG_LOC, imgLocation);
			editor.commit();
			
			Preference pref = findPreference(Preferences.PREF_SELECT_BG);
			pref.setSummary(imgLocation);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		String prefKey = preference.getKey();
		SharedPreferences sharedPrefs = this.getPreferenceManager().getSharedPreferences();
		Editor editor = sharedPrefs.edit();
		
		if(prefKey.equals(Preferences.PREF_COLOUR_PICKER))
		{
			String colour = ColourPickerPreference.convertToARGB(Integer.valueOf((String.valueOf(newValue))));
			
			editor.putString(Preferences.PREF_BG_COLOUR, colour);
			editor.commit();
		}
		else if(prefKey.equals(Preferences.PREF_MESH_SELECTION))
		{
			String value = String.valueOf(newValue);
			File dataDir = getDir("wallpaper", Context.MODE_PRIVATE);

			//FileLoader.copyFolderToInternal(this, new File(value));

         editor.putString(Preferences.PREF_MESH_FOLDER, "");
			editor.putString(Preferences.PREF_WALLPAPER_FOLDER, value);//dataDir.getAbsolutePath());//.getName());
			editor.commit();
		}
		else if(prefKey.equals(Preferences.PREF_DISPLAY_MODES))
		{
			ListPreference listPref = (ListPreference)preference;
			SeekBarPreference slider = (SeekBarPreference)findPreference(Preferences.PREF_DISPLAY_SLIDER);
			ListPreference rotation = (ListPreference)findPreference(Preferences.PREF_ROTATION_DIRECTION);
			
			int index = Integer.parseInt((String)newValue);//listPref.getValue());

			switch(index)
			{
				case 0:
					listPref.setSummary("Static View");
					
					slider.setTitle("Object Rotation");
					slider.setMinMaxValues(-180, 180);
					slider.setDefaultValue(0);
					
					editor.putInt(Preferences.PREF_DISPLAY_SLIDER, 0);
					editor.commit();
					
					rotation.setEnabled(false);
					
					break;
					
				case 1:
				case 2:
					
					if(index == 1)
						listPref.setSummary("Rotate Scene");
					else
						listPref.setSummary("Orbit Camera");
					
					slider.setTitle("Rotation Speed");
					slider.setMinMaxValues(1, 20);
					slider.setDefaultValue(2);
					
					editor.putInt(Preferences.PREF_DISPLAY_SLIDER, 2);
					editor.commit();
					
					rotation.setEnabled(true);
					
					break;
					
				case 3:
				case 4:
				case 5:
					
					if(index == 3)
						listPref.setSummary("Homescreen Scene Rotation");
					else if(index == 4)
						listPref.setSummary("Homescreen Camera Rotation");
					else if(index == 5)
						listPref.setSummary("Homescreen Camera Target Rotation");
					
					slider.setTitle("Rotation Amount");
					slider.setMinMaxValues(0, 180);
					slider.setDefaultValue(45);
					
					editor.putInt(Preferences.PREF_DISPLAY_SLIDER, 45);
					editor.commit();
					
					rotation.setEnabled(true);
					
					break;
					
				case 6:
					
					listPref.setSummary("Homescreen Camera Pan");
					
					slider.setTitle("Pan Amount");
					slider.setMinMaxValues(0, 300);
					slider.setDefaultValue(50);
					
					editor.putInt(Preferences.PREF_DISPLAY_SLIDER, 50);
					editor.commit();
					
					rotation.setEnabled(true);
					
					break;
			}
		}
		else if(prefKey.equals(Preferences.PREF_ROTATION_DIRECTION))
		{
			ListPreference listPref = (ListPreference)preference;
			int index = Integer.parseInt((String)newValue);
			
			switch(index)
			{
				case 0:
					listPref.setSummary("Clockwise");
					
					break;
					
				case 1:
					listPref.setSummary("Counter-Clockwise");
					
					break;
			}
		}
		
		return true;
	}
	
	@Override
	public void onClick(DialogInterface dialog, int index, boolean isChecked)
	{
		ListView listView = ((AlertDialog) dialog).getListView();
		
		switch(index)
		{
			case 0:
				
//				for(int i = 1; i < enabledTextures.length; i++)
//				{
//					enabledTextures[i] = false;
//					listView.setItemChecked(i, false);
//				}
				
				break;
				
			case 1:

				enabledTextures[2] = true;
				listView.setItemChecked(2, true);
				
				for(int i = 3; i < enabledTextures.length; i++)
				{
					enabledTextures[i] = false;
					listView.setItemChecked(i, false);
				}
				
				break;

			default:

				if(enabledTextures[1])
				{
					for(int i = 2; i < enabledTextures.length; i++)
					{
						if(i == index)
							continue;
						
						enabledTextures[i] = false;
						listView.setItemChecked(i, false);
					}
				}
				
				break;					
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int buttonID)
	{
		if(buttonID == AlertDialog.BUTTON_POSITIVE)
		{
			String shaderName = "Material";
			int shaderFlag = 0;
			
			if(enabledTextures[3])
				shaderFlag |= Scene.DEFINE_NORMAL;
			else if(!enabledTextures[1] && shaderName.length() == 0)
				shaderName = "Smooth";
			
			if(enabledTextures[2])
				shaderFlag |= Scene.DEFINE_DIFFUSE;
			
			if(enabledTextures[4])
				shaderFlag |= Scene.DEFINE_SPECULAR;
			
			if(enabledTextures[5])
			{
				shaderName += "Emissive";
				shaderFlag |= Scene.DEFINE_EMISSIVE;
			}
			
			if(enabledTextures[0])
			{
				shaderName += "Wireframe";
				shaderFlag |= Scene.DEFINE_WIREFRAME;
			}
			
			if(enabledTextures[1])
			{
				shaderName = "FullBright";
				shaderFlag |= Scene.DEFINE_FULLBRIGHT;
			}
	
			if(shaderName.length() != 0)
			{
				SharedPreferences sharedPrefs = getSharedPreferences(Preferences.LIVE_WALLPAPER_PREFS, 0);
				Editor editor = sharedPrefs.edit();
				editor.putString(Preferences.PREF_SHADER_NAME, shaderName);
				editor.putInt(Preferences.PREF_SHADER_FLAGS, shaderFlag);
				editor.commit();
			}
		}
		else if(dialogTitle.equals("LightPreset"))
		{
			dialogTitle = "";
			
			if(dialog != null)
				dialog.dismiss();
			
			if(lightPresetList == null)
				return;
			
			SharedPreferences sharedPrefs = getSharedPreferences(Preferences.LIVE_WALLPAPER_PREFS, 0);
			Editor editor = sharedPrefs.edit();
			editor.putString(Preferences.PREF_LIGHT_PRESET, lightPresetList[buttonID]);
			editor.commit();
		}
	}
	
	private void setupTextureList(ListView listView)
	{
		String[] nameTags = new String[] { "WireFrame", "FullBright", "Diffuse", "Normal", "Specular", "Emissive" };
		int[] defineValues = new int[]{ Scene.DEFINE_WIREFRAME, Scene.DEFINE_FULLBRIGHT, Scene.DEFINE_DIFFUSE, Scene.DEFINE_NORMAL, Scene.DEFINE_SPECULAR, Scene.DEFINE_EMISSIVE };
		
		String shaderName = getSharedPreferences(Preferences.LIVE_WALLPAPER_PREFS, 0).getString(Preferences.PREF_SHADER_NAME, "Material");
		int flags = getSharedPreferences(Preferences.LIVE_WALLPAPER_PREFS, 0).getInt(Preferences.PREF_SHADER_FLAGS, defineValues[2] | defineValues[3] | defineValues[4]);
		
		for(int i = 0; i < enabledTextures.length; ++i)
		{
			listView.setItemChecked(i, false);
			enabledTextures[i] = false;
		}
		
		for(int i = 0; i < nameTags.length; ++i)
		{
			if(shaderName.contains(nameTags[i]) || (flags & defineValues[i]) == defineValues[i])
			{
				listView.setItemChecked(i, true);
				enabledTextures[i] = true;
			}
		}
	}
	
	
}
