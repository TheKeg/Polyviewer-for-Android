package com.fusedresistance.polyviewer.pro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.fusedresistance.polyviewer.colorpicker.ColourPicker;
import com.fusedresistance.polyviewer.colorpicker.ColourPickerPreference;
import com.fusedresistance.polyviewer.scene.CameraPresets;
import com.fusedresistance.polyviewer.scene.LightPresets;
import com.fusedresistance.polyviewer.scene.Scene;
import com.fusedresistance.polyviewer.settings.Preferences;
import com.fusedresistance.polyviewer.utility.FileLoader;
import com.fusedresistance.polyviewer.utility.PathDatabase;

import de.ankri.views.Switch;

public class SettingsActivity extends SherlockActivity
   implements android.view.View.OnClickListener, OnClickListener, OnMultiChoiceClickListener, SeekBar.OnSeekBarChangeListener, ColourPicker.OnColorChangedListener

{
   private SharedPreferences sharedPrefs;
   private Typeface          ubuntuCondensed;
   private Typeface          sourceSansPro;
   private View              settingsView;
   private TextView          currentValueTextView;
   private CameraPresets     camPresets;
   private String[]          cameraPresetList;
   private String[]          lightPresetList;    
   private String[]          textureList;
   private int               dialogID;
   private int               optionsMask;
   private int               seekBarMinValue;
   private int               seekBarRange;
   private boolean           isLightTheme = false;
   private boolean[]         enabledTextures;
      
   public SettingsActivity()
   {
   }
   
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      
      setTitle("Polyviewer Options");
      setTheme(Preferences.THEME);
      
      LayoutInflater inflator = getLayoutInflater();  
            
      ubuntuCondensed = Typeface.createFromAsset(this.getAssets(), "fonts/UbuntuCondensed-Regular.ttf");
      sourceSansPro   = Typeface.createFromAsset(this.getAssets(), "fonts/SourceSansPro-Regular.ttf");
      settingsView    = inflator.inflate(R.layout.settings_layout, null);
      sharedPrefs     = getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0);

      this.initialize(false);      
      this.setContentView(settingsView);
   }
   
   private void initialize(boolean lightTheme)
   {
      int styleID    = R.style.PolyText; 
      int titleBarID = R.style.PolyText_Light;      
      int background = Preferences.DARK_COLOUR;
      int titleColor = Preferences.LIGHT_COLOUR;
      
      if(lightTheme)
      {
         styleID = R.style.PolyText_Light;
         titleBarID = R.style.PolyText;
         background = Preferences.LIGHT_COLOUR;
         titleColor = Preferences.DARK_COLOUR;
      }
      
      optionsMask = sharedPrefs.getInt(Preferences.PREF_OPTIONS, 0);
      boolean enabledOption;

            // Obtain the string array for the texture display option list.
      textureList = getResources().getStringArray(R.array.textureModes);
      enabledTextures = new boolean[textureList.length];

      // Set the appropriate values to true for the list
      for(int i = 2; i < enabledTextures.length - 1; i++)
         enabledTextures[i] = true;
      
      // Retrieve the light preset database
      LightPresets lightPresets = new LightPresets(this, Preferences.LIGHT_DATABASE);//"LightPresets.db");
      lightPresets.open();
      
      // Retrieve the camera preset database
      camPresets = new CameraPresets(this, Preferences.CAMERA_DATABASE);//"CameraPresets.db");
      camPresets.open();
      
      // Obtain the light and camera preset name lists
      lightPresetList = lightPresets.getList();
      cameraPresetList = camPresets.getList();
      
      // Set the listener for the fullscreen button to this activity
      ImageView themeIcon = (ImageView)settingsView.findViewById(R.id.themeButton);
      if(themeIcon != null)
         themeIcon.setOnClickListener(this);
      
      // Get the title bar view and set the background colour
      View titleView  = settingsView.findViewById(R.id.options_title_bar);
      if(titleView != null)
         titleView.setBackgroundColor(titleColor);
      
      // Set the background colour for the entire view
      settingsView.setBackgroundColor(background);
      
      // Set the listener for the fps toggle checkbox
      Switch tempSwitch = (Switch)settingsView.findViewById(R.id.options_fps_switch);
      if(tempSwitch != null)
      {
         tempSwitch.setOnClickListener(this);
         enabledOption = (optionsMask & Preferences.PREF_FPS_TOGGLE) == Preferences.PREF_FPS_TOGGLE;

         tempSwitch.setChecked(enabledOption);
      }
      
      // Set the listener for the background toggle checkbox
      tempSwitch = (Switch)settingsView.findViewById(R.id.options_background_switch);
      if(tempSwitch != null)
      {
         tempSwitch.setOnClickListener(this);
         enabledOption = (optionsMask & Preferences.PREF_SHOWBG) == Preferences.PREF_SHOWBG;

         View bgImageView     = settingsView.findViewById(R.id.options_background_select);
         View bgColourView    = settingsView.findViewById(R.id.options_background_colour_layout);
         View bgColourPreview = settingsView.findViewById(R.id.options_background_colour_preview);

         bgColourPreview.setBackgroundColor(Color.parseColor(sharedPrefs.getString(Preferences.PREF_BG_COLOUR, "#FF333333")));

         if(enabledOption)
         {
            bgImageView.setVisibility(View.VISIBLE);
            bgColourView.setVisibility(View.GONE);
         }
         else
         {
            bgImageView.setVisibility(View.GONE);
            bgColourView.setVisibility(View.VISIBLE);
         }

         tempSwitch.setChecked(enabledOption);
      }
      
      // Set the listener for the screen stay toggle checkbox
      tempSwitch = (Switch)settingsView.findViewById(R.id.options_screen_switch);
      if(tempSwitch != null)
      {
         tempSwitch.setOnClickListener(this);
         enabledOption = (optionsMask & Preferences.PREF_SCREEN_STAY) == Preferences.PREF_SCREEN_STAY;

         tempSwitch.setChecked(enabledOption);
      }

      // Set the listener for the specular quality toggle checkbox
      tempSwitch = (Switch)settingsView.findViewById(R.id.options_quality_switch);
      if(tempSwitch != null)
      {
         tempSwitch.setOnClickListener(this);
         enabledOption = (optionsMask & Preferences.PREF_QUALITY_SPEC) == Preferences.PREF_QUALITY_SPEC;

         tempSwitch.setChecked(enabledOption);
      }

      // Setup the title font and style
      setupTitle(R.id.options_title_text, styleID);
      setupTitle(R.id.options_general_title, styleID);
      setupTitle(R.id.options_visuals_title, styleID);
      setupTitle(R.id.options_misc_title, styleID);
      setupTitle(R.id.options_about_title, styleID);
      setTextStyle(R.id.options_title_bar, titleBarID);
      
      View aboutText = this.findViewById(R.id.options_about_title);
      if(aboutText != null)
         aboutText.setOnClickListener(this);
      
      // Setup the general option buttons font, style and listeners
      setupButton(R.id.options_default_textures, styleID);
      setupButton(R.id.options_brightness, styleID);
      setupButton(R.id.options_fps_limit, styleID);
      setupButton(R.id.options_fps_toggle, styleID);
     
      // Setup the visual option buttons font, style and listeners
      setupButton(R.id.options_camera_fov, styleID);
      setupButton(R.id.options_camera_preset, styleID);
      setupButton(R.id.options_light_preset, styleID);
      setupButton(R.id.options_emissive_strength, styleID);
      setupButton(R.id.options_quality_toggle, styleID);
      setupButton(R.id.options_background_toggle, styleID);
      setupButton(R.id.options_background_select, styleID);
      setupButton(R.id.options_background_colour, styleID);
      
      // Setup the misc option buttons font, style and listeners
      setupButton(R.id.options_screen_toggle, styleID);
      setupButton(R.id.options_delete_meshes, styleID);
      setupButton(R.id.options_reset_model_paths, styleID);
      setupButton(R.id.options_reset_defaults, styleID);
      setupButton(R.id.options_camera_reset, styleID);
      setupButton(R.id.options_light_reset, styleID);      
   }
   
   @Override
   public void onClick(DialogInterface dialog, int buttonID)
   {
      Editor editor   = sharedPrefs.edit();
      SeekBar seekBar = null;

      if(dialog != null)
      {
         dialog.dismiss();
         seekBar = (SeekBar)((AlertDialog)dialog).findViewById(R.id.seek_bar);
      }

      if(buttonID == AlertDialog.BUTTON_POSITIVE)
      {
         switch(dialogID)
         {
            case Preferences.BRIGHTNESS_DIALOG:
               if(seekBar != null)
                  setSliderPreference(Preferences.PREF_BRIGHTNESS, seekBar.getProgress());

               break;

            case Preferences.CAMERA_FOV_DIALOG:
               if(seekBar != null)
                  setSliderPreference(Preferences.PREF_CAMERA_FOV, seekBar.getProgress());

               break;

            case Preferences.EMISSIVE_DIALOG:
               if(seekBar != null)
                  setSliderPreference(Preferences.PREF_EMISSIVE, seekBar.getProgress());

               break;

            case Preferences.TEXTURE_DIALOG:
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
                  editor.putString(Preferences.PREF_SHADER_NAME, shaderName);
                  editor.putInt(Preferences.PREF_SHADER_FLAGS, shaderFlag);
               }

               break;
         }
      }
      else if(dialogID == Preferences.LIGHT_PRESET_DIALOG)//(dialogTitle.equals("LightPreset"))
      {
         if(lightPresetList == null)
            return;
         
         editor.putString(Preferences.PREF_LIGHT_PRESET, lightPresetList[buttonID]);
      }
      else if(dialogID == Preferences.CAMERA_PRESET_DIALOG)//(dialogTitle.equals("CameraPreset"))
      {
         if(cameraPresetList == null)
            return;
         
         float[] camValues = camPresets.getPreset(cameraPresetList[buttonID]);  
         
         editor.putString(Preferences.PREF_CAMERA_PRESET, cameraPresetList[buttonID]);
         editor.putInt(Preferences.PREF_CAMERA_X_DEFAULT, (int)camValues[0]);
         editor.putInt(Preferences.PREF_CAMERA_Y_DEFAULT, (int)camValues[1]);
      }
      
      editor.commit();
      dialogID = -1;
   }

   @Override
   public void onClick(View v)
   {
      if(v == null)
         return;

      AlertDialog.Builder alertBuilder;
      AlertDialog         selectionAlert;
      String              selectedPreset;
      int                 index;
      int                 viewID = v.getId();
      Editor              editor = this.getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0).edit();

      switch(viewID)
      {
         case R.id.options_theme_text:
         case R.id.themeButton:
            
            isLightTheme = !isLightTheme;
            this.setTheme(isLightTheme);
            
            break;

         case R.id.options_about_title:
            // About Polyviewer
            break;

         case R.id.options_background_toggle:
         case R.id.options_background_switch:
            
            Switch bgSwitch     = (Switch)settingsView.findViewById(R.id.options_background_switch);
            View   bgImageView  = settingsView.findViewById(R.id.options_background_select);
            View   bgColourView = settingsView.findViewById(R.id.options_background_colour_layout);
            
            if(bgSwitch == null || bgImageView == null || bgColourView == null)
               return;
            
            // Toggle the switch in the case that the text area is touched instead of the switch itself
            if(viewID == R.id.options_background_toggle)
               bgSwitch.setChecked(!bgSwitch.isChecked());
            
            if(bgSwitch.isChecked())
            {
               bgImageView.setVisibility(View.VISIBLE);
               bgColourView.setVisibility(View.GONE);
                              
               optionsMask += Preferences.PREF_SHOWBG;
            }
            else
            {
               optionsMask -= Preferences.PREF_SHOWBG;
               
               bgImageView.setVisibility(View.GONE);
               bgColourView.setVisibility(View.VISIBLE);
               
               editor.putString(Preferences.PREF_BG_IMG_LOC, "");
            }
            
            break;
            
         case R.id.options_background_select:
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 0);
            
            break;
            
         case R.id.options_background_colour:

            int colour = Color.parseColor(sharedPrefs.getString(Preferences.PREF_BG_COLOUR, "#FF333333"));

            ColourPicker colourPicker = new ColourPicker(this, colour);
            colourPicker.setOnColorChangedListener(this);
//
//            propertyView = colourPicker.findViewById(R.layout.color_picker_dialog);

            colourPicker.show();
            break;

         case R.id.options_brightness:
            dialogID = Preferences.BRIGHTNESS_DIALOG;
            showSliderDialog("Brightness", Preferences.BRIGHTNESS_MIN_VAL, Preferences.BRIGHTNESS_MAX_VAL, Preferences.BRIGHTNESS_DEF_VAL);
            break;

         case R.id.options_camera_fov:
            dialogID = Preferences.CAMERA_FOV_DIALOG;
            showSliderDialog("Camera FOV", Preferences.CAMERA_FOV_MIN_VAL, Preferences.CAMERA_FOV_MAX_VAL, Preferences.CAMERA_FOV_DEF_VAL);
            break;

         // Bring up the dialog for selecting the default camera position
         case R.id.options_camera_preset:

            if(cameraPresetList == null)
               return;

            selectedPreset = sharedPrefs.getString(Preferences.PREF_CAMERA_PRESET, "");

            for(index = 0; index < cameraPresetList.length; ++index)
            {
               if(cameraPresetList[index].equals(selectedPreset))
                  break;
            }

            alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, Preferences.THEME));
            alertBuilder.setTitle("Select Camera Preset");
            alertBuilder.setSingleChoiceItems(cameraPresetList, index, this);
            selectionAlert = alertBuilder.create();

            dialogID = Preferences.CAMERA_PRESET_DIALOG;
            selectionAlert.show();

            break;

         case R.id.options_default_textures:
            dialogID = Preferences.TEXTURE_DIALOG;
            showTextureDialog();

            break;

         case R.id.options_fps_limit:
            showFPSDialog();

            break;

         case R.id.options_fps_toggle:
         case R.id.options_fps_switch:

            Switch fpsSwitch = (Switch)settingsView.findViewById(R.id.options_fps_switch);

            if(fpsSwitch == null)
               return;

            // Toggle the switch in the case that the text area is touched instead of the switch itself
            if(viewID == R.id.options_fps_toggle)
               fpsSwitch.setChecked(!fpsSwitch.isChecked());

            if(fpsSwitch.isChecked())
               optionsMask += Preferences.PREF_FPS_TOGGLE;
            else
               optionsMask -= Preferences.PREF_FPS_TOGGLE;

            break;

         // Bring up the dialog for selecting the default lighting setup
         case R.id.options_light_preset:

            if(lightPresetList == null)
               return;

            selectedPreset = sharedPrefs.getString(Preferences.PREF_LIGHT_PRESET, "Direct Downward");

            for(index = 0; index < lightPresetList.length; ++index)
            {
               if(lightPresetList[index].equals(selectedPreset))
                  break;
            }

            alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, Preferences.THEME));
            alertBuilder.setTitle("Select Light Preset");
            alertBuilder.setSingleChoiceItems(lightPresetList, index, this);
            selectionAlert = alertBuilder.create();

            dialogID = Preferences.LIGHT_PRESET_DIALOG;
            selectionAlert.show();


            break;

         case R.id.options_emissive_strength:
            dialogID = Preferences.EMISSIVE_DIALOG;
            showSliderDialog("Emissive Strength", Preferences.EMISSIVE_MIN_VAL, Preferences.EMISSIVE_MAX_VAL, Preferences.EMISSIVE_DEF_VAL);
            break;

         case R.id.options_quality_toggle:
         case R.id.options_quality_switch:

            Switch qualitySwitch = (Switch)settingsView.findViewById(R.id.options_quality_switch);

            if(qualitySwitch == null)
               return;

            // Toggle the switch in the case that the text area is touched instead of the switch itself
            if(viewID == R.id.options_quality_toggle)
               qualitySwitch.setChecked(!qualitySwitch.isChecked());

            if(qualitySwitch.isChecked())
               optionsMask += Preferences.PREF_QUALITY_SPEC;
            else
               optionsMask -= Preferences.PREF_QUALITY_SPEC;

            break;

         case R.id.options_screen_toggle:
         case R.id.options_screen_switch:

            Switch screenSwitch = (Switch)settingsView.findViewById(R.id.options_screen_switch);

            if(screenSwitch == null)
               return;

            // Toggle the switch in the case that the text area is touched instead of the switch itself
            if(viewID == R.id.options_screen_toggle)
               screenSwitch.setChecked(!screenSwitch.isChecked());

            if(screenSwitch.isChecked())
            {
               optionsMask += Preferences.PREF_SCREEN_STAY;

               if(getParent() != null)
                  getParent().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            else
            {
               optionsMask -= Preferences.PREF_SCREEN_STAY;

               if(getParent() != null)
                  getParent().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }

            break;
         
         // Deletes the saved serialized versions of the meshes.
         case R.id.options_delete_meshes:
            
            FileLoader.DeleteCachedMeshes(this);
            Toast toast = Toast.makeText(this, "Cache Deleted", Toast.LENGTH_SHORT);
            toast.show();
            
            break;
         
         // Delete the database containing the models database and rebuild the database
         case R.id.options_reset_model_paths:
            
            this.deleteDatabase(Preferences.PATH_DATABASE);
            
            String[]     polyviewerDirs;
            long         currTime       = System.currentTimeMillis();
            PathDatabase pathDB         = new PathDatabase(this, Preferences.PATH_DATABASE);
            
            pathDB.open();
            
            polyviewerDirs = FileLoader.retrieveDirectories("polyviewer");
            editor.putLong(Preferences.PREF_DATABASE_UPDATE, currTime);

            pathDB.clear();
               
            for(String value : polyviewerDirs)
               pathDB.addPreset(value);
            
            pathDB.close();
                                    
            break;
            
         case R.id.options_reset_defaults:
            
            reset();
            
            break;
            
         // Delete the camera preset database and re-populate it with the default values.
         case R.id.options_camera_reset:
            
            this.deleteDatabase(Preferences.CAMERA_DATABASE);
            
            CameraPresets cameraDB = new CameraPresets(this, Preferences.CAMERA_DATABASE);
            cameraDB.open();
            cameraDB.close();
            
            break;
         
         // Delete the light preset database and re-populate it with the default values.
         case R.id.options_light_reset:
            
            this.deleteDatabase(Preferences.LIGHT_DATABASE);
            
            LightPresets lightDB = new LightPresets(this, Preferences.LIGHT_DATABASE);
            lightDB.open();
            lightDB.close();

            break;
      }
      
      editor.putInt(Preferences.PREF_OPTIONS, optionsMask);
      editor.commit();
   }
   
   @Override
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
   
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      super.onActivityResult(requestCode, resultCode, data);
      
      if(resultCode == RESULT_OK)
      {
         Uri uri = data.getData();
         String[] projection = {MediaStore.Images.Media.DATA};  

         if(uri == null)
            return;

         Cursor cursor = getContentResolver().query(uri, projection, null, null, null);  

         if(cursor == null)
            return;

         cursor.moveToFirst();

         int columnIndex = cursor.getColumnIndex(projection[0]);  
         String imgLocation = cursor.getString(columnIndex);  
         cursor.close();  
         
         Editor editor = sharedPrefs.edit();
         
         editor.putString(Preferences.PREF_BG_IMG_LOC, imgLocation);
         editor.commit();
      }
   }
   
   private void setupTitle(int id, int styleID)
   {
      TextView tempView = (TextView)settingsView.findViewById(id);
      
      if(tempView != null)
      {
         tempView.setTypeface(ubuntuCondensed);
         tempView.setTextAppearance(this, styleID);
      }      
   }
   
   private void setupButton(int id, int styleID)
   {
      TextView tempView = (TextView)settingsView.findViewById(id);
      
      if(tempView != null)
      {
         tempView.setTypeface(sourceSansPro);
         tempView.setTextAppearance(this, styleID);
         tempView.setOnClickListener(this);
      }
   }
   
   private void setTextStyle(int id, int styleID)
   {
      TextView tempView = (TextView)settingsView.findViewById(id);
      
      if(tempView != null)
         tempView.setTextAppearance(this, styleID);
   }
   
   private void setTheme(boolean lightTheme)
   {
      TextView  themeText   = (TextView)settingsView.findViewById(R.id.options_theme_text);
      ImageView themeIcon   = (ImageView)settingsView.findViewById(R.id.themeButton);
      ImageView titleIcon   = (ImageView)settingsView.findViewById(R.id.options_title_icon);
      View      titleView   = settingsView.findViewById(R.id.options_title_bar);
      String    themeTitle  = "Dark"; 
      int       themeIconID = R.drawable.dark_theme;
      int       titleIconID = R.drawable.settings_icon;
      int       background  = Preferences.DARK_COLOUR;
      int       titleColor  = Preferences.LIGHT_COLOUR; 
      int       styleID     = R.style.PolyText; 
      
      if(lightTheme)
      {
         themeIconID = R.drawable.light_theme;
         titleIconID = R.drawable.settings_icon_light;
         styleID     = R.style.PolyText_Light;
         background  = Preferences.LIGHT_COLOUR;
         titleColor  = Preferences.DARK_COLOUR;
         themeTitle  = "Light";         
      }
            
      if(themeText != null && themeIcon != null && titleIcon != null)
      {
         themeText.setText(themeTitle);
         themeText.setTextAppearance(this, styleID);
         themeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
         themeIcon.setImageResource(themeIconID);
         titleIcon.setImageResource(titleIconID);
      }
      
      settingsView.setBackgroundColor(background);
      titleView.setBackgroundColor(titleColor);
      
      // Setup the title font and style
      setTextStyle(R.id.options_title_text, styleID);
      setTextStyle(R.id.options_general_title, styleID);
      setTextStyle(R.id.options_visuals_title, styleID);
      setTextStyle(R.id.options_misc_title, styleID);
      setTextStyle(R.id.options_about_title, styleID);

      // Setup the general option buttons font, style and listeners
      setTextStyle(R.id.options_default_textures, styleID);
      setTextStyle(R.id.options_brightness, styleID);
      setTextStyle(R.id.options_fps_limit, styleID);
      setTextStyle(R.id.options_fps_toggle, styleID);
     
      // Setup the visual option buttons font, style and listeners
      setTextStyle(R.id.options_camera_fov, styleID);
      setTextStyle(R.id.options_camera_preset, styleID);
      setTextStyle(R.id.options_light_preset, styleID);
      setTextStyle(R.id.options_emissive_strength, styleID);
      setTextStyle(R.id.options_background_toggle, styleID);
      setTextStyle(R.id.options_background_select, styleID);
      setTextStyle(R.id.options_background_colour, styleID);
      
      // Setup the misc option buttons font, style and listeners
      setTextStyle(R.id.options_screen_toggle, styleID);
      setTextStyle(R.id.options_delete_meshes, styleID);
      setTextStyle(R.id.options_reset_model_paths, styleID);
      setTextStyle(R.id.options_reset_defaults, styleID);
      setTextStyle(R.id.options_camera_reset, styleID);
      setTextStyle(R.id.options_light_reset, styleID);
   }
   
   private void reset()
   {
      
   }   

   public void showTextureDialog()
   {
      String[] nameTags     = getResources().getStringArray(R.array.textureModes); //new String[] { "WireFrame", "FullBright", "Diffuse", "Normal", "Specular", "Emissive" };
      int[]    defineValues = getResources().getIntArray(R.array.textureValues); //new int[]{ Scene.DEFINE_WIREFRAME, Scene.DEFINE_FULLBRIGHT, Scene.DEFINE_DIFFUSE, Scene.DEFINE_NORMAL, Scene.DEFINE_SPECULAR, Scene.DEFINE_EMISSIVE };

      String shaderName = sharedPrefs.getString(Preferences.PREF_SHADER_NAME, "Material");
      int    flags      = sharedPrefs.getInt(Preferences.PREF_SHADER_FLAGS, defineValues[2] | defineValues[3] | defineValues[4]);

      AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, Preferences.THEME));
      alertBuilder.setTitle("Select Textures");
      alertBuilder.setMultiChoiceItems(textureList, enabledTextures, this);
      alertBuilder.setPositiveButton("OK", this);
      alertBuilder.setNegativeButton("Cancel", this);
      AlertDialog textureAlert = alertBuilder.create();

      ListView listView = textureAlert.getListView();

      if(listView != null)
      {
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

      textureAlert.show();
   }

   public void showFPSDialog()
   {
      AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, Preferences.THEME));
      alertBuilder.setTitle("Select FPS Limit");
      alertBuilder.setSingleChoiceItems(R.array.fpsNames,  R.array.fpsValues, this);
      alertBuilder.setPositiveButton("OK", this);
      alertBuilder.setNegativeButton("Cancel", this);

      AlertDialog fpsDialog = alertBuilder.create();
      fpsDialog.show();
   }


   public void showSliderDialog(String title, int minValue, int maxValue, int startingValue)
   {
      LayoutInflater inflator = getLayoutInflater();
      View seekbarView = inflator.inflate(R.layout.dialog_slider, null);

      if(seekbarView == null)
         return;

      AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, Preferences.THEME));
      alertBuilder.setTitle(title);
      alertBuilder.setView(seekbarView);
      alertBuilder.setPositiveButton("OK", this);
      alertBuilder.setNegativeButton("Cancel", this);

      seekBarMinValue = minValue;
      seekBarRange = maxValue - minValue;

      currentValueTextView          = (TextView)seekbarView.findViewById(R.id.current_value);
      TextView minValueTextView     = (TextView)seekbarView.findViewById(R.id.min_value);
      TextView maxValueTextView     = (TextView)seekbarView.findViewById(R.id.max_value);
      SeekBar seekbar               = (SeekBar)seekbarView.findViewById(R.id.seek_bar);

      seekbar.setOnSeekBarChangeListener(this);
      seekbar.setProgress((int)(((startingValue - minValue) / (float) seekBarRange) * 100));
      currentValueTextView.setText(String.valueOf(startingValue));
      minValueTextView.setText(String.valueOf(minValue));
      maxValueTextView.setText(String.valueOf(maxValue));

      AlertDialog sliderAlert = alertBuilder.create();
      sliderAlert.show();
   }

   public void setSliderPreference(String preferenceName, float value)
   {
      if(sharedPrefs == null)
         return;

      int finalValue = seekBarMinValue + (seekBarRange * (int)value);
      Editor editor  = sharedPrefs.edit();

      editor.putInt(preferenceName, finalValue);
      editor.commit();
   }

   @Override
   public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
   {
      int currentValue = seekBarMinValue + (int)(((float)progress / 100.0f) * seekBarRange);

      currentValueTextView.setText(String.valueOf(currentValue));
   }

   @Override
   public void onStartTrackingTouch(SeekBar seekBar)
   {

   }

   @Override
   public void onStopTrackingTouch(SeekBar seekBar)
   {

   }

   public void onColorChanged(int colour)
   {
      Editor editor          = sharedPrefs.edit();
      View   bgColourPreview = settingsView.findViewById(R.id.options_background_colour_preview);

      bgColourPreview.setBackgroundColor(colour);
      editor.putString(Preferences.PREF_BG_COLOUR, ColourPickerPreference.convertToARGB(colour));
      editor.commit();
   }
}
