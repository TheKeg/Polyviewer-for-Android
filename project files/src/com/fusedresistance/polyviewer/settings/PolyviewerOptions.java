package com.fusedresistance.polyviewer.settings;

//import java.util.Arrays;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.MediaStore;
import android.view.ContextThemeWrapper;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.fusedresistance.polyviewer.colorpicker.ColourPickerPreference;
import com.fusedresistance.polyviewer.pro.PolyviewerActivity;
import com.fusedresistance.polyviewer.pro.R;
import com.fusedresistance.polyviewer.pro.R.array;
import com.fusedresistance.polyviewer.pro.R.xml;
import com.fusedresistance.polyviewer.scene.CameraPresets;
import com.fusedresistance.polyviewer.scene.LightPresets;
import com.fusedresistance.polyviewer.scene.Scene;
import com.fusedresistance.polyviewer.settings.Preferences;
import com.fusedresistance.polyviewer.utility.FileLoader;
import com.fusedresistance.polyviewer.utility.PathDatabase;

public class PolyviewerOptions extends SherlockPreferenceActivity 
	implements OnSharedPreferenceChangeListener, OnPreferenceClickListener, OnPreferenceChangeListener, OnClickListener, OnMultiChoiceClickListener
{
	private ActionBar actionBar;
	private String[] lightPresetList = null;
	private String[] cameraPresetList = null;
	private String[] textureList;
	private boolean[] enabledTextures;
	private String dialogTitle = "";
	private LightPresets lightPresets;
	private CameraPresets camPresets;
	private SharedPreferences sharedPrefs;
	
	@SuppressWarnings("deprecation")
   public void onCreate(Bundle savedInstance)
	{
		setTitle("Settings");
		//setTheme(PolyviewerActivity.THEME);
		
		actionBar = getSupportActionBar();
		
		if(actionBar != null)
			actionBar.setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstance);
		
		Preference pref = null;
		boolean bgEnabled = false;
		
//		if(context != null)
//		{
//		   sharedPrefs = this.getPreferences(0);
//		}
		
		getPreferenceManager().setSharedPreferencesName(Preferences.SHARED_PREFS_NAME);
		addPreferencesFromResource(R.xml.preferences);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
		pref = findPreference(Preferences.PREF_TEXTURE);
		pref.setOnPreferenceClickListener(this);
		
		pref = findPreference(Preferences.PREF_SHOW_BG);
		pref.setOnPreferenceClickListener(this);
		bgEnabled = getPreferenceManager().getSharedPreferences().getBoolean(Preferences.PREF_SHOW_BG, true);
		
		pref = findPreference(Preferences.PREF_RESET);
		pref.setOnPreferenceClickListener(this);
		
		pref = findPreference(Preferences.PREF_LIGHT_PRESET);
		pref.setOnPreferenceClickListener(this);
		
		pref = findPreference(Preferences.PREF_CAMERA_PRESET);
		pref.setOnPreferenceClickListener(this);
		
		pref = findPreference(Preferences.PREF_DELETE_CACHE);
		pref.setOnPreferenceClickListener(this);
		
		pref = findPreference(Preferences.PREF_DELETE_PATHS);
		pref.setOnPreferenceClickListener(this);
		
		pref = findPreference(Preferences.PREF_DELETE_LIGHTS);
		pref.setOnPreferenceClickListener(this);
		
		pref = findPreference(Preferences.PREF_DELETE_CAMERAS);
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
		((ColourPickerPreference)pref).setDefaultValue(this.getPreferenceManager().getSharedPreferences().getString(Preferences.PREF_BG_COLOUR, "0xFF333333"));
		
		textureList = getResources().getStringArray(R.array.textureModes);
		enabledTextures = new boolean[textureList.length];
		
		for(int i = 2; i < enabledTextures.length - 1; i++)
			enabledTextures[i] = true;
		
		lightPresets = new LightPresets(this, Preferences.LIGHT_DATABASE);//"LightPresets.db");
		lightPresets.open();
		
		lightPresetList = lightPresets.getList();
		
		camPresets = new CameraPresets(this, Preferences.CAMERA_DATABASE);//"CameraPresets.db");
		camPresets.open();
		
		cameraPresetList = camPresets.getList();
		
//		if(lightPresetList != null)
//		{
//			String[] lightValues = new String[lightPresetList.length];
//			
//			for(int i = 0; i < lightValues.length; ++i)
//				lightValues[i] = String.valueOf(i);
//			
//			ListPreference listPref = (ListPreference)findPreference(Preferences.PREF_LIGHT_PRESET);
//			listPref.setEntries(lightPresetList);
//			listPref.setEntryValues(lightValues);
//			listPref.setOnPreferenceChangeListener(this);
//		}
		
//		if(PolyviewerActivity.LAYOUT == R.layout.main_limited)
//		{
			pref = findPreference(Preferences.PREF_SPIN_SPEED);
			pref.setEnabled(true);
			
			pref = findPreference(Preferences.PREF_ROTATION_DIR);
			pref.setEnabled(true);
			
			pref = findPreference(Preferences.PREF_ORBIT_CAMERA);
			pref.setEnabled(true);
//		}
		
		CheckBoxPreference fpsCheckBox = (CheckBoxPreference)findPreference(Preferences.PREF_ENABLE_FPS_DISPLAY);
		boolean fpsEnabled = getPreferenceManager().getSharedPreferences().getBoolean(Preferences.PREF_ENABLE_FPS_DISPLAY, true);
		fpsCheckBox.setChecked(fpsEnabled);
	}
	
	//@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
	}

	@SuppressWarnings("deprecation")
	//@Override
	public boolean onPreferenceClick(Preference preference)
	{
		String prefKey = preference.getKey();
		
		if(prefKey.equals(Preferences.PREF_SELECT_BG))
		{
			Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			
			startActivityForResult(intent, 0);
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
						
			checkPref = (CheckBoxPreference)findPreference(Preferences.PREF_ORBIT_CAMERA);
			checkPref.setChecked(false);
			
			checkPref = (CheckBoxPreference)findPreference(Preferences.PREF_ROTATION_DIR);
			checkPref.setChecked(false);
			
			checkPref = (CheckBoxPreference)findPreference(Preferences.PREF_ENABLE_FPS_DISPLAY);
			checkPref.setChecked(true);
			
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
			editor.putInt(Preferences.PREF_CAMERA_FOV, 90);
			editor.putInt(Preferences.PREF_CAMERA_X_DEFAULT, 0);
			editor.putInt(Preferences.PREF_CAMERA_Y_DEFAULT, 0);
			editor.putString(Preferences.PREF_DEFAULT_SHADER, "Material");
			editor.putString(Preferences.PREF_SHADER_NAME, "Material");
			editor.putInt(Preferences.PREF_SHADER_FLAGS, Scene.DEFINE_DIFFUSE | Scene.DEFINE_NORMAL | Scene.DEFINE_SPECULAR);
			editor.putInt(Preferences.PREF_EMISSIVE, 100);
			editor.putBoolean(Preferences.PREF_ENABLE_ROTATION, false);
			
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
	        
	        if(textureAlert != null)
	        {
	        	setupTextureList(textureAlert.getListView());
				textureAlert.show();
	        }
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
		else if(prefKey.equals(Preferences.PREF_CAMERA_PRESET))
		{
			if(cameraPresetList == null)
				return false;
			
			String selected = getPreferenceManager().getSharedPreferences().getString(Preferences.PREF_CAMERA_PRESET, "");
			int index = 0;
			
			for(index = 0; index < cameraPresetList.length; ++index)
			{
				if(cameraPresetList[index].equals(selected))
					break;
			}
			
			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, Preferences.THEME));
	        alertBuilder.setTitle("Select Camera Preset");
	        alertBuilder.setSingleChoiceItems(cameraPresetList, index, this);
	        AlertDialog selectionAlert = alertBuilder.create();
	        
	        if(selectionAlert != null)
	        	selectionAlert.show();
	        
	        dialogTitle  = "CameraPreset";
	        
	        return true;
		}
		else if(prefKey.equals(Preferences.PREF_DELETE_CACHE))
		{
			FileLoader.DeleteCachedMeshes(this);
			Toast toast = Toast.makeText(this, "Cache Deleted", Toast.LENGTH_SHORT);
			
			if(toast != null)
				toast.show();
		}
		else if(prefKey.equals(Preferences.PREF_DELETE_PATHS))
		{
			this.deleteDatabase(Preferences.PATH_DATABASE);
			
			PathDatabase pathDB = new PathDatabase(this, Preferences.PATH_DATABASE);
			pathDB.open();
		}
		else if(prefKey.equals(Preferences.PREF_DELETE_LIGHTS))
		{
			this.deleteDatabase(Preferences.LIGHT_DATABASE);
			
			LightPresets lightDB = new LightPresets(this, Preferences.LIGHT_DATABASE);
			lightDB.open();
		}
		else if(prefKey.equals(Preferences.PREF_DELETE_CAMERAS))
		{
			this.deleteDatabase(Preferences.CAMERA_DATABASE);
			
			CameraPresets cameraDB = new CameraPresets(this, Preferences.CAMERA_DATABASE);
			cameraDB.open();
		}
		
		return true;
	}

	@SuppressWarnings("deprecation")
	//@Override
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
	//@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		String prefKey = preference.getKey();
		
		if(prefKey.equals(Preferences.PREF_COLOUR_PICKER))
		{
			SharedPreferences sharedPrefs = this.getPreferenceManager().getSharedPreferences();
			Editor editor = sharedPrefs.edit();
			
			String colour = ColourPickerPreference.convertToARGB(Integer.valueOf((String.valueOf(newValue))));
			
			editor.putString(Preferences.PREF_BG_COLOUR, colour);
			editor.commit();
			
			return true;
		}
		
		return false;
	}
	
	//@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    		case android.R.id.home:
    			Intent intent = new Intent(this, PolyviewerActivity.class);
    			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			
    			startActivity(intent);
    			
    			return true;
    			
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }

	//@Override
	public void onClick(DialogInterface dialog, int buttonID)
	{
		if(buttonID == AlertDialog.BUTTON_POSITIVE)
		{
			String shaderName = "Material";
			int shaderFlag = 0;
			
			if(enabledTextures[3])
				shaderFlag |= Scene.DEFINE_NORMAL;
			else if(enabledTextures[1] == false && shaderName.length() == 0 )
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
				SharedPreferences sharedPrefs = getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0);
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
			
			SharedPreferences sharedPrefs = getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0);
			Editor editor = sharedPrefs.edit();
			editor.putString(Preferences.PREF_LIGHT_PRESET, lightPresetList[buttonID]);
			editor.commit();
		}
		else if(dialogTitle.equals("CameraPreset"))
		{
			dialogTitle = "";
			
			if(dialog != null)
				dialog.dismiss();
			
			if(cameraPresetList == null)
				return;
			
			float[] camValues = camPresets.getPreset(cameraPresetList[buttonID]);  
			
			SharedPreferences sharedPrefs = getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0);
			Editor editor = sharedPrefs.edit();
			editor.putString(Preferences.PREF_CAMERA_PRESET, cameraPresetList[buttonID]);
			editor.putInt(Preferences.PREF_CAMERA_X_DEFAULT, (int)camValues[0]);
			editor.putInt(Preferences.PREF_CAMERA_Y_DEFAULT, (int)camValues[1]);
			editor.commit();
		}
	}

	//@Override
	public void onClick(DialogInterface dialog, int index, boolean isChecked)
	{
		ListView listView = ((AlertDialog) dialog).getListView();
		
		switch(index)
		{
			case 0:
				
				for(int i = 1; i < enabledTextures.length; i++)
				{
					enabledTextures[i] = false;
					listView.setItemChecked(i, false);
				}
				
				break;
				
			case 1:
				
				enabledTextures[0] = false;
				listView.setItemChecked(0, false);
				
				enabledTextures[2] = true;
				listView.setItemChecked(2, true);
				
				for(int i = 3; i < enabledTextures.length; i++)
				{
					enabledTextures[i] = false;
					listView.setItemChecked(i, false);
				}
				
				break;

			default:
				enabledTextures[0] = false;
				listView.setItemChecked(0, false);
				
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
	
	private void setupTextureList(ListView listView)
	{		
		String[] nameTags = new String[] { "WireFrame", "FullBright", "Diffuse", "Normal", "Specular", "Emissive" };
		int[] defineValues = new int[]{ Scene.DEFINE_WIREFRAME, Scene.DEFINE_FULLBRIGHT, Scene.DEFINE_DIFFUSE, Scene.DEFINE_NORMAL, Scene.DEFINE_SPECULAR, Scene.DEFINE_EMISSIVE };
		
		String shaderName = getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0).getString(Preferences.PREF_SHADER_NAME, "Material");
		int flags = getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0).getInt(Preferences.PREF_SHADER_FLAGS, defineValues[2] | defineValues[3] | defineValues[4]);
		
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
