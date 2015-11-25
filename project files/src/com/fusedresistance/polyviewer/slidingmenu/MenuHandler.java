package com.fusedresistance.polyviewer.slidingmenu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Typeface;
import android.text.Editable;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fusedresistance.polyviewer.pro.PolyviewerActivity;
import com.fusedresistance.polyviewer.pro.R;
import com.fusedresistance.polyviewer.pro.SettingsActivity;
import com.fusedresistance.polyviewer.pro.renderer.OGLView;
import com.fusedresistance.polyviewer.scene.CameraPresets;
import com.fusedresistance.polyviewer.scene.LightPresets;
import com.fusedresistance.polyviewer.settings.Preferences;
import com.fusedresistance.polyviewer.utility.FileLoader;
import com.fusedresistance.polyviewer.utility.PathDatabase;

public class MenuHandler
   implements android.view.View.OnClickListener, OnClickListener, OnGroupExpandListener, OnChildClickListener
{
   public String data[][] = {{"Test 1", "Test 2"}, 
                             {"Load Preset", "Save Preset", "Remove Preset"}, 
                             {"Load Preset", "Save Preset", "Remove Preset"}};
   public OGLView oglView;
   private PolyviewerActivity parentActivity;
   private ExpandableListView listView;
   private ImageView fsView;
   private int prevGroup = 0;
   private boolean isFullscreen = false;
   private boolean removePreset;
   private boolean isCamPreset;
   private String[] camPresetList;
   private String[] lightPresetList;
   
   public MenuHandler()
   {
   }   
   
   public boolean initialize(PolyviewerActivity parent, View menuView)
   {
      if(menuView == null)
         return false;
      
      parentActivity = parent;
      menuView.setOnClickListener(this);
      
      Typeface titleFont = Typeface.createFromAsset(parentActivity.getAssets(), "fonts/UbuntuCondensed-Regular.ttf");
//      listFont  = Typeface.createFromAsset(parentActivity.getAssets(), "fonts/SourceSansPro-Regular.ttf");
      
      // Set the listener for the fullscreen button to this activity
      fsView = (ImageView)menuView.findViewById(R.id.fullscreenButton);
      if(fsView != null)
         fsView.setOnClickListener(this);
      
      // Set the listener for the load model menu option to this activity
      View tempView = menuView.findViewById(R.id.fileload_menu_button);
      if(tempView != null)
         tempView.setOnClickListener(this);
      
      // Set the listener for the load model menu option to this activity
      tempView = menuView.findViewById(R.id.camera_menu_button);
      if(tempView != null)
         tempView.setOnClickListener(this);
      
      // Set the listener for the load model menu option to this activity
      tempView = menuView.findViewById(R.id.light_menu_button);
      if(tempView != null)
         tempView.setOnClickListener(this);
      
      // Set the listener for the load model menu option to this activity
      tempView = menuView.findViewById(R.id.settings_menu_button);
      if(tempView != null)
         tempView.setOnClickListener(this);

      // Set the listener for the refresh directories menu option.
      tempView = menuView.findViewById(R.id.model_refresh_menu_button);
      if(tempView != null)
         tempView.setOnClickListener(this);

      // Set the font for the title text
      tempView = menuView.findViewById(R.id.menu_title_text);
      if(tempView != null)
         ((TextView)tempView).setTypeface(titleFont);

      // Set the font for the settings text
      tempView = menuView.findViewById(R.id.menu_settings_text);
      if(tempView != null)
         ((TextView)tempView).setTypeface(titleFont);

      // Set the font for the refresh text
      tempView = menuView.findViewById(R.id.menu_refresh_text);
      if(tempView != null)
         ((TextView)tempView).setTypeface(titleFont);
      
      listView = (ExpandableListView) menuView.findViewById(R.id.file_expandable_list);
      
      if(listView != null)
      {
         PolyviewerExpandableListAdapter listAdapter = new PolyviewerExpandableListAdapter(parent.getApplicationContext(), parent, data);
         listView.setAdapter(listAdapter);
         listView.setDivider(null);
         listView.setGroupIndicator(null);
         listView.setOnGroupExpandListener(this);
         listView.setOnChildClickListener(this);
      }
      
      return true;
   }

   @Override
   public void onClick(DialogInterface dialog, int buttonID)
   {
      dialog.dismiss();
      parentActivity.toggle();
      
      try
      {
         if(isCamPreset)
         {
            if(removePreset)
            {
               AlertDialog.Builder alert = new AlertDialog.Builder(parentActivity);

               alert.setTitle("Save Camera Preset");

               // Set an EditText view to get user input 
               final TextView input = new TextView(parentActivity);
               final int id = buttonID;
               
               input.setText("Are you sure you wish to remove the camera preset: " + camPresetList[id] + "?");
               
               alert.setView(input);
               alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
               {
                  public void onClick(DialogInterface dialog, int whichButton) 
                  {
                     oglView.removeCameraPreset(camPresetList[id]);
                  }
               });

               alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
               {
                  public void onClick(DialogInterface dialog, int whichButton){}
               });

               alert.show();

               removePreset = false;
            }
            else
               oglView.setCameraPreset(camPresetList[buttonID]);
            
            
         }
         else
         {
            if(removePreset)
            {
               AlertDialog.Builder alert = new AlertDialog.Builder(parentActivity);

               alert.setTitle("Save Camera Preset");

               // Set an EditText view to get user input 
               final TextView input = new TextView(parentActivity);
               final int id = buttonID;
               
               input.setText("Are you sure you wish to remove the light preset: " + lightPresetList[id] + "?");
               
               alert.setView(input);
               alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
               {
                  public void onClick(DialogInterface dialog, int whichButton) 
                  {
                     oglView.removeLightPreset(lightPresetList[id]);
                  }
               });

               alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
               {
                  public void onClick(DialogInterface dialog, int whichButton){}
               });

               alert.show();

               removePreset = false;
            }
            else
               oglView.setLightPreset(lightPresetList[buttonID]);
         }
      }
      catch(Exception e)
      {
         Toast msg = Toast.makeText(parentActivity, e.toString(), Toast.LENGTH_LONG);
         msg.show();
      }
   }

   @Override
   public void onClick(View v)
   {
      if(v == null || parentActivity == null)
         return;
      
      switch(v.getId())
      {
         case R.id.fullscreenButton:
            
            if(!isFullscreen )
            {
               isFullscreen = true;
               
               parentActivity.getSupportActionBar().hide();
               parentActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
               parentActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
               
               fsView.setImageResource(R.drawable.fullscreen_back);
            }
            else
            {
               isFullscreen = false;
               
               parentActivity.getSupportActionBar().show();
               parentActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
               parentActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
               
               fsView.setImageResource(R.drawable.fullscreen_icon);
            }
            
            break;

         case R.id.model_refresh_menu_button:
            Toast toast = Toast.makeText(parentActivity, "Refreshing Model List", Toast.LENGTH_LONG);
            PathDatabase pathDB = new PathDatabase(parentActivity, Preferences.PATH_DATABASE);
            String[] polyviewerDirs;

            // Open the database and show the toast message
            toast.show();
            pathDB.open();

            polyviewerDirs = FileLoader.retrieveDirectories("polyviewer");

            // Add all the new values.
            for(String value : polyviewerDirs)
               pathDB.addPreset(value);

            String[] modelPaths      = FileLoader.retrieveMeshDirectories(polyviewerDirs);
            String[] availableModels = FileLoader.parseString(modelPaths);

            data[0] = availableModels;
            PolyviewerExpandableListAdapter listAdapter = new PolyviewerExpandableListAdapter(parentActivity, parentActivity, data);
            listView.setAdapter(listAdapter);

            break;

         case R.id.settings_menu_button:
            
            Intent optionsIntent = new Intent(parentActivity, SettingsActivity.class);//PolyviewerOptions.class);//

            parentActivity.toggle();
            parentActivity.startActivity(optionsIntent);


            break;
      }
      
   }

   @Override
   public void onGroupExpand(int groupPosition)
   {
      if(groupPosition != prevGroup)
      {
         listView.collapseGroup(prevGroup);
         prevGroup = groupPosition;
      }      
   }

   @Override
   public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
   {  
      if(oglView == null || parentActivity == null)
         return false;
      
      switch(groupPosition)
      {
         // Handle loading a new model from the available list
         case 0:
            parentActivity.getSupportActionBar().setSelectedNavigationItem(childPosition);
            parentActivity.toggle();
            break;
            
         // Handle camera presets
         case 1:
            
            switch(childPosition)
            {
               // Save a camera preset
               case 1:
                  
                  AlertDialog.Builder alert = new AlertDialog.Builder(parentActivity);

                  alert.setTitle("Save Camera Preset");

                  // Set an EditText view to get user input 
                  final EditText input = new EditText(parentActivity);
                  alert.setView(input);

                  alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                  {
                     public void onClick(DialogInterface dialog, int whichButton) 
                     {
                        Editable value = input.getText();

                        if(value != null)
                           oglView.saveCameraPreset(value.toString());
                     }
                  });

                  alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
                  {
                     public void onClick(DialogInterface dialog, int whichButton){}
                  });

                  alert.show();
                  
                  break;
                
               // Load or Remove a camera preset from the saved database
               case 0:
               case 2:
                  CameraPresets camPresets = oglView.getCameraPresets();
                  camPresetList = camPresets.getList();
                  
                  if(camPresetList == null)
                     return false;
                  
                  String selected = parentActivity.getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0).getString(Preferences.PREF_CAMERA_PRESET, "");
                  int index;
                  
                  for(index = 0; index < camPresetList.length; ++index)
                  {
                     if(camPresetList[index].equals(selected))
                        break;
                  }
                  
                  AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(parentActivity, Preferences.THEME));
                  alertBuilder.setTitle("Select Camera Preset");
                  alertBuilder.setSingleChoiceItems(camPresetList, index, this);
                  AlertDialog selectionAlert = alertBuilder.create();
                  selectionAlert.show();

                  isCamPreset = true;

                  if(childPosition == 2)
                     removePreset = true;
                  
                  break;
            }
            
            break;
            
         // Handle light presets
         case 2:
            
            switch(childPosition)
            {
               // Save a Light Preset
               case 1:
                  
                  AlertDialog.Builder alert = new AlertDialog.Builder(parentActivity);

                  alert.setTitle("Save Light Preset");

                  // Set an EditText view to get user input 
                  final EditText input = new EditText(parentActivity);
                  alert.setView(input);

                  alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                  {
                     public void onClick(DialogInterface dialog, int whichButton) 
                     {
                        Editable value = input.getText();

                        if(value != null)
                           oglView.saveLightPreset(value.toString());
                     }
                  });

                  alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
                  {
                     public void onClick(DialogInterface dialog, int whichButton){}
                  });

                  alert.show();
                  
                  break;
                  
               // Select or Remove a light preset
               case 0:
               case 2:
                  LightPresets lightPresets = oglView.getLightPresets();
                  lightPresetList = lightPresets.getList();
                  
                  if(lightPresetList == null)
                     return false;
                  
                  String selected = parentActivity.getSharedPreferences(Preferences.SHARED_PREFS_NAME, 0).getString(Preferences.PREF_LIGHT_PRESET, "Direct Downward");
                  int index;
                  
                  for(index = 0; index < lightPresetList.length; ++index)
                  {
                     if(lightPresetList[index].equals(selected))
                        break;
                  }
                  
                  AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(parentActivity, Preferences.THEME));
                  alertBuilder.setTitle("Select Light Preset");
                  alertBuilder.setSingleChoiceItems(lightPresetList, index, this);
                  AlertDialog selectionAlert = alertBuilder.create();
                  selectionAlert.show();

                  isCamPreset = false;

                  if(childPosition == 2)
                     removePreset = true;
                    
                  break;
            }
            
            break;
      }      
      
      listView.collapseGroup(groupPosition);
      
      return true;
   }

   private void setTextStyle(int id, int styleID)
   {
      //TextView tempView = (TextView)settingsView.findViewById(id);

//      if(tempView != null)
//         tempView.setTextAppearance(this, styleID);
   }

   private void setTheme(boolean lightTheme)
   {

   }

}
