package com.fusedresistance.polyviewer.pro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.LayoutParams;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.fusedresistance.polyviewer.pro.renderer.OGLView;
import com.fusedresistance.polyviewer.scene.CameraPresets;
import com.fusedresistance.polyviewer.scene.LightPresets;
import com.fusedresistance.polyviewer.scene.Scene;
import com.fusedresistance.polyviewer.settings.PolyviewerOptions;
import com.fusedresistance.polyviewer.settings.Preferences;
import com.fusedresistance.polyviewer.slidingmenu.MenuHandler;
import com.fusedresistance.polyviewer.utility.FileLoader;
import com.fusedresistance.polyviewer.utility.PathDatabase;
import com.fusedresistance.polyviewer.utility.Utilities;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class PolyviewerActivity extends SlidingFragmentActivity
	implements OnMultiChoiceClickListener, OnClickListener, android.view.View.OnClickListener, 
			     ActionBar.OnNavigationListener
{
	public static final String MODEL = "modelName";
//	public static final String THEME = "activityTheme";
		
	public View menuView;
   public View overlayView;
   public View settingsView;
	
	private ActionBar      actionBar;
	private OGLView        oglView = null;
	private View           propertyView = null;
	private boolean        isTextureAlert = false;
	private boolean        isLight = false;
	private boolean        isFullscreen = false;
	private boolean[]      enabledTextures;
   private int            diffuseColour;
   private int            specularColour;
	private int            selection = 0;
	private int[]          selectionValues = new int[] { 3, 0, 1, 2};
	private Handler        handler;
	private Handler        lightHandler;
	private String         meshTitle = "";
   private String         dialogTitle = "";
   private String[]       selectionList;
   private String[]       textureList;
	private String[]       availableModels = null;
	private String[]       modelPaths = null;
	private MenuHandler    menuHandler;
	private OverlayHandler overlayHandler;
	
	protected SherlockListFragment mFrag;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Intent            intent       = this.getIntent();
		LayoutInflater    inflater     = this.getLayoutInflater();
		SlidingMenu       slidingMenu  = this.getSlidingMenu();
		SharedPreferences sharedPrefs  = this.getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0);
		Editor            editor       = sharedPrefs.edit();

		this.setTitle(meshTitle);
      this.setTheme(Preferences.THEME);

	   actionBar       = this.getSupportActionBar();
      dialogTitle     = "Selection";
      menuView        = inflater.inflate(R.layout.menu_frame, null);
      overlayView     = inflater.inflate(R.layout.overlay, null);
      settingsView    = inflater.inflate(R.layout.settings_layout, null);
      selectionList   = this.getResources().getStringArray(R.array.selectionList);
      textureList     = this.getResources().getStringArray(R.array.textureModes);
      enabledTextures = new boolean[textureList.length];
      menuHandler     = new MenuHandler();
      overlayHandler  = new OverlayHandler();

		// set the Behind View
		setBehindContentView(R.layout.menu_frame);

		if (savedInstanceState == null)
		{
			FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
			mFrag = new SherlockListFragment();
			t.replace(R.id.menu_frame, mFrag);
			t.commit();
		}
		else
			mFrag = (SherlockListFragment)this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);

		// Set the Sliding menu settings
		slidingMenu.setMode(SlidingMenu.LEFT);
      slidingMenu.setShadowWidth(25);
		slidingMenu.setShadowDrawable(R.drawable.shadow);
		slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setMenu(menuView);
      slidingMenu.setAnimationCacheEnabled(true);

		if(intent != null)
			meshTitle = intent.getStringExtra(MODEL);

		for(int i = 2; i < enabledTextures.length - 1; i++)
			enabledTextures[i] = true;

      int optionsMask = sharedPrefs.getInt(Preferences.PREF_OPTIONS, 0);

		if((optionsMask & Preferences.PREF_SCREEN_STAY) == Preferences.PREF_SCREEN_STAY)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if(actionBar != null)
		{
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setIcon(R.drawable.menu_icon);

			try
			{
			   String[]     polyviewerDirs;
			   long         currTime       = System.currentTimeMillis();
				long         dbTime         = sharedPrefs.getLong(Preferences.PREF_DATABASE_UPDATE, Preferences.DB_UPDATE);
			   PathDatabase pathDB         = new PathDatabase(this, Preferences.PATH_DATABASE);

				pathDB.open();

	        	if(dbTime <= Preferences.DB_UPDATE)
	        	{
	        		polyviewerDirs = FileLoader.retrieveDirectories("polyviewer");
	        		editor.putLong(Preferences.PREF_DATABASE_UPDATE, currTime);

	        		pathDB.clear();

               for(String value : polyviewerDirs)
	        		   pathDB.addPreset(value);
	        	}
	        	else
	        		polyviewerDirs = pathDB.getList();//

	        	pathDB.close();

	        	modelPaths = FileLoader.retrieveMeshDirectories(polyviewerDirs);
	    		availableModels = FileLoader.parseString(modelPaths);

				if(availableModels != null)
				{
               SpinnerAdapter spinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, availableModels);

					actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
					actionBar.setListNavigationCallbacks(spinner, this);

					// Set a blank OpenGL view to prevent the fragment from crashing. gets replaced with
					// a new view from the spinner adapter.
					oglView = new OGLView(this, "");// modelPaths[index]);
					oglView.getRenderer().setPreferences(sharedPrefs);
					oglView.getRenderer().getScene().setHandler(handler);
					oglView.setLightHandler(lightHandler);

					this.setContentView(oglView);

					overlayHandler.oglView  = oglView;
					menuHandler.oglView     = oglView;
					menuHandler.data[0]     = availableModels;

					overlayHandler.initialize(this, overlayView, selectionList);
					menuHandler.initialize(this, menuView);

					initializeHandlers();
				}
			}
			catch(Exception e)
			{
				if(e.getLocalizedMessage() != null)
				{
					Log.d("MODEL VIEW ERROR", e.getLocalizedMessage());

					Toast toast = Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT);
					toast.show();
				}
				else
				{
					Toast toast = Toast.makeText(this, "Null Exception Error", Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		}


		editor.putBoolean(Preferences.PREF_MOTION, false);
      editor.putString(Preferences.PREF_MESH_FOLDER, meshTitle);
      editor.putString(Preferences.PREF_WALLPAPER_FOLDER, "");
      editor.putString(Preferences.PREF_SHADER_NAME, sharedPrefs.getString(Preferences.PREF_DEFAULT_SHADER, "Material"));
      editor.putBoolean(Preferences.PREF_ENABLE_ROTATION, false);
		editor.commit();

		this.toggle();
	}
	
	private void initializeHandlers()
	{
	   handler = new Handler()
      {
         public void handleMessage(Message msg)
         {
            int fps = msg.arg1;
            String input = "";

            if(oglView.getSettings().displayFPS)
               input =  "FPS: " + fps;

            if(overlayHandler.fpsView != null)
               overlayHandler.fpsView.setText(input);//"FPS: " + fps);
         }
      };
      
      lightHandler = new Handler()
      {
         public void handleMessage(Message msg)
         {
            overlayHandler.lightEnabled.setChecked(oglView.lightEnabled(selectionValues[selection]));
            
            overlayHandler.diffuseColour = oglView.getDiffuse(selectionValues[selection]);
            overlayHandler.diffuseButton.setBackgroundColor(diffuseColour);
            overlayHandler.diffuseButton.setTextColor(Utilities.invertColour(diffuseColour));
            
            overlayHandler.specularColour = oglView.getSpecular(selectionValues[selection]);
            overlayHandler.specularButton.setBackgroundColor(specularColour);
            overlayHandler.specularButton.setTextColor(Utilities.invertColour(specularColour));
         }
      };
	}
	
	
	//@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.clear();
		menu.add("Textures").setIcon(R.drawable.content_picture).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	//@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.clear();
		menu.add("Textures").setIcon(R.drawable.content_picture).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	//@Override
    public void onPause()
    {
    	super.onPause();
    	
    	if(oglView != null)
    		oglView.onPause();
    }
    
    //@Override
    public void onResume()
    {
    	super.onResume();
    	
    	if(oglView != null)
    		oglView.onResume();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
       if ( keyCode == KeyEvent.KEYCODE_MENU ) 
       {
          this.toggle();
          return true;
       }
        
       return super.onKeyDown(keyCode, event);
    }
    
    //@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
		String title = (String) item.getTitle();
		
		if(item.getItemId() == android.R.id.home)
		{
         oglView.onPause();
		   this.toggle();
		   
    		return true;
		}
		else if(title.equals("Textures") || title.equals("Texture Display"))
		{
         AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, Preferences.THEME));
         alertBuilder.setTitle("Select Textures");
         alertBuilder.setMultiChoiceItems(textureList, enabledTextures, this);
         alertBuilder.setPositiveButton("OK", this);
         alertBuilder.setNegativeButton("Cancel", this);
         AlertDialog textureAlert = alertBuilder.create();

         isTextureAlert = true;
         setupTextureList(textureAlert.getListView());
         textureAlert.show();

         return true;
		}
		else if(title.equals("Options") || title.equals("Settings"))
		{
			Intent intent = new Intent(this, PolyviewerOptions.class);
			startActivity(intent);
			
			return true;
		}
		else if(title.equals("Camera/Light Selection"))
		{
         AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, Preferences.THEME));
         alertBuilder.setTitle("Select Object");
         alertBuilder.setSingleChoiceItems(selectionList, selection, this);
         AlertDialog selectionAlert = alertBuilder.create();

         isTextureAlert = false;
         selectionAlert.show();

         dialogTitle = "Selection";

			return true;
		}
		else if(title.equals("Light Presets"))
		{
			LightPresets lightPresets = oglView.getLightPresets();//.getRenderer().getScene().getLightPresets();
			
			String[] lightPresetList = lightPresets.getList();
			
			if(lightPresetList == null)
				return false;
			
			String selected = getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0).getString(Preferences.PREF_LIGHT_PRESET, "Direct Downward");
			int index;
			
			for(index = 0; index < lightPresetList.length; ++index)
			{
				if(lightPresetList[index].equals(selected))
					break;
			}
			
         AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, Preferences.THEME));
         alertBuilder.setTitle("Select Light Preset");
         alertBuilder.setSingleChoiceItems(lightPresetList, index, this);
         AlertDialog selectionAlert = alertBuilder.create();

         selectionAlert.show();
         dialogTitle = "Lights";

         return true;
		}
		else if(title.equals("Camera Presets"))
		{
			CameraPresets camPresets = oglView.getCameraPresets();//.getRenderer().getScene().getCameraPresets();
			
			String[] camPresetList = camPresets.getList();
			
			if(camPresetList == null)
				return false;
			
			String selected = getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0).getString(Preferences.PREF_CAMERA_PRESET, "");
			int index;
			
			for(index = 0; index < camPresetList.length; ++index)
			{
				if(camPresetList[index].equals(selected))
					break;
			}
			
			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, Preferences.THEME));
         alertBuilder.setTitle("Select Camera Preset");
         alertBuilder.setSingleChoiceItems(camPresetList, index, this);
         AlertDialog selectionAlert = alertBuilder.create();
	        
         selectionAlert.show();
	      dialogTitle = "Camera";
	        
	      return true;
		}
		else if(title.equals("Light Property"))
		{
         propertyView = this.getLayoutInflater().inflate(R.layout.light_property_dialog, (ViewGroup) getCurrentFocus());
			diffuseColour = oglView.getDiffuse(selectionValues[selection]);
			specularColour = oglView.getSpecular(selectionValues[selection]);

         if(propertyView == null)
            return false;

         View tempView = propertyView.findViewById(R.id.diffuseView);

         if(tempView != null)
         {
            tempView.setBackgroundColor(diffuseColour);
            tempView.setOnClickListener(this);
         }

         tempView = propertyView.findViewById(R.id.specularView);

         if(tempView != null)
         {
            tempView.setBackgroundColor(specularColour);
            tempView.setOnClickListener(this);
         }

			CheckBox checkbox = (CheckBox)propertyView.findViewById(R.id.light_enabled);

         if(checkbox == null)
            return false;

			checkbox.setChecked(oglView.lightEnabled(selectionValues[selection]));

         AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, Preferences.THEME));
         alertBuilder.setTitle("Light #" + selection + " Properties");
         alertBuilder.setPositiveButton("OK", this);
         alertBuilder.setNegativeButton("Cancel", this);
         alertBuilder.setView(propertyView);
         AlertDialog propertyAlert = alertBuilder.create();
	        
         propertyAlert.show();
         isLight  = true;

			return true;
		}
		else if(title.equals("Save Light Preset"))
		{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Save Light Preset");

			// Set an EditText view to get user input 
			final EditText input = new EditText(this);
			alert.setView(input);

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					Editable value = input.getText();
               if(value != null)
					   oglView.getRenderer().getScene().saveLightPreset(value.toString());
				}
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					// Canceled.
				}
			});

			alert.show();
		}
		else if(title.equals("Save Camera Preset"))
		{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Save Camera Preset");

			// Set an EditText view to get user input 
			final EditText input = new EditText(this);
			alert.setView(input);

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					Editable value = input.getText();
               if(value != null)
					   oglView.getRenderer().getScene().saveCameraPreset(value.toString());
				}
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					// Canceled.
				}
			});

			alert.show();
		}
		else if(title.contains("Fullscreen"))
		{
			if(!isFullscreen)
			{
				isFullscreen = true;
				invalidateOptionsMenu();
				
				actionBar.hide();
				overlayView.setVisibility(View.INVISIBLE);

				getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			   getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			}
			else
			{
				isFullscreen = false;
				invalidateOptionsMenu();
				
				actionBar.show();
				overlayView.setVisibility(View.VISIBLE);
				
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		}
    		
		return true;//super.onOptionsItemSelected(item);
    }

	//@Override
	public void onClick(DialogInterface dialog, int index, boolean isChecked)
	{
		ListView listView = ((AlertDialog) dialog).getListView();

		switch(index)
		{
			case 0:
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

	//@Override
	public void onClick(DialogInterface dialog, int buttonID)
	{
//		if(buttonID >= 0 && dialogTitle.equals("Selection"))
//		{
//			selection = buttonID;
//			
//			if(oglView != null)
//				oglView.setSelection(selection);
//			
//			if(dialog != null)
//				dialog.dismiss();
//			
//			if(selectionButton == null || lightEnabled == null || diffuseButton == null || specularButton == null || camLightEnabled == null || camLightText == null)
//				return;
//			
//			if(selection == 0)
//			{				
//				selectionButton.setImageResource(R.drawable.camera);
//				
//				lightEnabled.setVisibility(View.INVISIBLE);
//				diffuseButton.setVisibility(View.INVISIBLE);
//				specularButton.setVisibility(View.INVISIBLE);
//				
//				camLightEnabled.setVisibility(View.VISIBLE);
//				camLightText.setVisibility(View.VISIBLE);
//				
//				return;
//			}
//			
//			switch(selection)
//			{
//				case 1:
//					selectionButton.setImageResource(R.drawable.light1);
//					break;
//					
//				case 2:
//					selectionButton.setImageResource(R.drawable.light2);
//					break;
//					
//				case 3:
//					selectionButton.setImageResource(R.drawable.light3);
//					break;
//			}
//			
//			diffuseButton.setBackgroundColor(oglView.getDiffuse(selectionValues[selection]));
//			specularButton.setBackgroundColor(oglView.getSpecular(selectionValues[selection]));
//			lightEnabled.setChecked(oglView.lightEnabled(selectionValues[selection]));
//			
//			diffuseButton.setTextColor(Utilities.invertColour(oglView.getDiffuse(selectionValues[selection])));
//			specularButton.setTextColor(Utilities.invertColour(oglView.getSpecular(selectionValues[selection])));
//			
//			lightEnabled.setVisibility(View.VISIBLE);
//			diffuseButton.setVisibility(View.VISIBLE);
//			specularButton.setVisibility(View.VISIBLE);
//			camLightEnabled.setVisibility(View.INVISIBLE);
//			camLightText.setVisibility(View.INVISIBLE);
//			
//			if(overlayView != null)
//				overlayView.invalidate();//.bringToFront();
//
//			lightEnabled.bringToFront();
//			diffuseButton.bringToFront();
//			specularButton.bringToFront();
//
//			invalidateOptionsMenu();
//			dialogTitle = "";
//		}
//		else if(dialogTitle.equals("Camera"))
//		{
//			if(dialog != null)
//				dialog.dismiss();
//			
//			dialogTitle = "";
//			
//			if(camPresetList != null && buttonID >= 0)
//			{
//				oglView.getRenderer().getScene().setCameraPreset(camPresetList[buttonID]);
//			}
//		}
//		else if(dialogTitle.equals("Lights"))
//		{
//			if(dialog != null)
//				dialog.dismiss();
//			
//			dialogTitle = "";
//			
//			if(lightPresetList != null && buttonID >= 0)
//			{
//				oglView.getRenderer().getScene().setLightPreset(lightPresetList[buttonID]);
//				
//				lightEnabled.setChecked(oglView.lightEnabled(selectionValues[selection]));
//				
//				diffuseColour = oglView.getDiffuse(selectionValues[selection]);
//				diffuseButton.setBackgroundColor(diffuseColour);
//				diffuseButton.setTextColor(Utilities.invertColour(diffuseColour));
//				
//				specularColour = oglView.getSpecular(selectionValues[selection]);
//				specularButton.setBackgroundColor(specularColour);
//				specularButton.setTextColor(Utilities.invertColour(specularColour));
//			}
//		}
		
		if(dialogTitle.equals("Mesh"))
		{
			if(actionBar == null)
				return;
			
			actionBar.setSelectedNavigationItem(buttonID);
		}
		else if(buttonID == AlertDialog.BUTTON_POSITIVE)
		{
			if(isTextureAlert)
			{
				String shaderName = "Material";
				int shaderFlag = 0;
				
				if(enabledTextures[3])
					shaderFlag |= Scene.DEFINE_NORMAL;
				else if(!enabledTextures[1] && shaderName.length() == 0 )
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
					
					if(overlayHandler.camLightEnabled != null)
						editor.putBoolean(Preferences.PREF_CAMERA_LIGHT, overlayHandler.camLightEnabled.isChecked());
					
					editor.commit();
				}
			}
			else if(isLight)
			{
            if(propertyView != null)
               oglView.enableLight(selectionValues[selection], ((CheckBox)propertyView.findViewById(R.id.light_enabled)).isChecked());

            oglView.setDiffuse(selectionValues[selection], diffuseColour);
            oglView.setSpecular(selectionValues[selection], specularColour);
            oglView.updateLights();
            
            overlayHandler.lightEnabled.setChecked(oglView.lightEnabled(selectionValues[selection]));
			}
		}
	}

	//@Override
	public void onClick(View v)
	{
		if(oglView == null)
			return;
				
		int viewID = v.getId();
		AlertDialog.Builder alertBuilder;
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
		
		switch(viewID)
		{
			case R.id.view_model_linear:
				if(menuView != null)
				{
					menuView.setVisibility(View.GONE);
					menuView.setAnimation(anim);
				}
			case R.id.view_model:
				
				try
				{
					if(availableModels == null)
					{
						Toast toast = Toast.makeText(this, "Model List is empty", Toast.LENGTH_SHORT);
						toast.show();
						
						return;
					}
					
					alertBuilder = new AlertDialog.Builder(this);
			      alertBuilder.setTitle("Select Model");
			      alertBuilder.setItems(availableModels, this);
			      
			      AlertDialog modelAlert = alertBuilder.create();

               modelAlert.show();
					dialogTitle = "Mesh";
				}
				catch(Exception e)
				{
					if(e.getLocalizedMessage() != null)
					{
						Log.d("MODEL MENU ERROR", e.getLocalizedMessage());
						
						Toast toast = Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT);
						toast.show();
					}
					else
					{
						Toast toast = Toast.makeText(this, "Null Exception Error", Toast.LENGTH_SHORT);
						toast.show();
					}
				}
		        
				break;

			case R.id.options_linear:
				if(menuView != null)
				{
					menuView.setVisibility(View.GONE);
					menuView.setAnimation(anim);
				}
			case R.id.options_button:

            oglView.onPause();
			   this.toggle();
			   
				Intent optionsIntent = new Intent(this, PolyviewerOptions.class);
				startActivity(optionsIntent);

//            FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
//            SettingsFragment settingsFragmemt = new SettingsFragment();
//            t.add(R.id.fragm)
//            t.commit();

				
				break;
			
			case R.id.help_linear:
				if(menuView != null)
				{
					menuView.setVisibility(View.GONE);
					menuView.setAnimation(anim);
				}
			case R.id.help_activity:
				
				Intent helpIntent = new Intent(this, HelpActivity.class);
				startActivity(helpIntent);

				break;
		}
	}

	//@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId)
	{
		if(availableModels == null)
			return false;
		
		if(oglView != null)
		{
		   menuHandler.oglView     = null;
		   overlayHandler.oglView  = null;
		   
			oglView.release();
			oglView = null;
		}
		
		oglView = new OGLView(this, modelPaths[itemPosition]);
		
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		SharedPreferences sharedPrefs = getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0);
		
		oglView.getRenderer().setPreferences(sharedPrefs);
		oglView.getRenderer().getScene().setHandler(handler);
		oglView.setLightHandler(lightHandler);
		
		this.setContentView(oglView);
		this.addContentView(overlayView, layoutParams);
		
		overlayHandler.dialogTitle = "Selection";
		overlayHandler.oglView     = oglView;
		menuHandler.oglView        = oglView;
		
		overlayHandler.onClick(null, 0);
		
		return true;
	}
	
	private void setupTextureList(ListView listView)
	{
		String[] nameTags     = new String[] { "WireFrame", "FullBright", "Diffuse", "Normal", "Specular", "Emissive" };
		int[]    defineValues = new int[]{ Scene.DEFINE_WIREFRAME, Scene.DEFINE_FULLBRIGHT, Scene.DEFINE_DIFFUSE, Scene.DEFINE_NORMAL, Scene.DEFINE_SPECULAR, Scene.DEFINE_EMISSIVE };
		String   shaderName   = getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0).getString(Preferences.PREF_SHADER_NAME, "Material");
		int      flags        = getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0).getInt(Preferences.PREF_SHADER_FLAGS, defineValues[2] | defineValues[3] | defineValues[4]);
		
		// Sort through the list and make all items un-checked
		for(int i = 0; i < enabledTextures.length; ++i)
		{
			listView.setItemChecked(i, false);
			enabledTextures[i] = false;
		}
		
		// Loop through the list and check the appropriate textures according to the texture flag.
		for(int i = 0; i < nameTags.length; ++i)
		{
			if(shaderName.contains(nameTags[i]) || (flags & defineValues[i]) == defineValues[i])
			{
				listView.setItemChecked(i, true);
				enabledTextures[i] = true;
			}
		}
	}

   private void setTheme(boolean lightTheme)
   {

   }
}
