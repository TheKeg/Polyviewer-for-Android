package com.fusedresistance.polyviewer.pro;

import com.fusedresistance.polyviewer.colorpicker.ColourPicker;
import com.fusedresistance.polyviewer.pro.renderer.OGLView;
import com.fusedresistance.polyviewer.settings.SettingsContainer;
import com.fusedresistance.polyviewer.settings.Preferences;
import com.fusedresistance.polyviewer.utility.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Typeface;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * @author Keg
 * The class that is used to handle all events that occur with the overlay layer of the model viewer.
 */
public class OverlayHandler
   implements android.view.View.OnClickListener, ColourPicker.OnColorChangedListener, OnClickListener
{
   public int diffuseColour;
   public int specularColour;
   public ImageButton selectionButton;
   public CheckBox lightEnabled;
   public CheckBox camLightEnabled;
   public Button diffuseButton;
   public Button specularButton;
   public TextView fpsView;
   public TextView camLightText;
   public String dialogTitle;
   public OGLView oglView;
   
   private Activity parentActivity;
   private String[] selectionList;
   private int viewID;
   private int selection = 0;
   private int[] selectionValues = new int[] { 3, 0, 1, 2};
   private View propertyView;
   
   public OverlayHandler()
   {
   }   
   
   /** Function to initialize the overlay event handlers and set a font if desired
    * @param parent The parent activity
    * @param overlayView The view that contains the expected buttons and values.
    * @param selList The list of available selection objects for the camera/light selection
    * @return true on success
    */
   public boolean initialize(Activity parent, View overlayView, String[] selList)
   {
      if(overlayView == null || parent == null) 
         return false;
      
      parentActivity = parent;
      selectionList = selList;
      
      Typeface overlayFont  = Typeface.createFromAsset(parentActivity.getAssets(), "fonts/SourceSansPro-Regular.ttf");
      
      dialogTitle = "Selection";
      
      // Obtain references to the view overlay buttons
      selectionButton = (ImageButton)overlayView.findViewById(R.id.selectionButton);
      lightEnabled    = (CheckBox)overlayView.findViewById(R.id.enabledCheckbox);
      diffuseButton   = (Button)overlayView.findViewById(R.id.diffuseButton);
      specularButton  = (Button)overlayView.findViewById(R.id.specularButton);
      fpsView         = (TextView)overlayView.findViewById(R.id.fpsCounter);
      camLightEnabled = (CheckBox)overlayView.findViewById(R.id.enabledCameraLight);
      camLightText    = (TextView)overlayView.findViewById(R.id.cameraLightText);
      
      // Set the on click listener for each button of the overlay
      if(selectionButton != null)
         selectionButton.setOnClickListener(this);
      
      if(lightEnabled != null)
      {
         lightEnabled.setOnClickListener(this);
         
         if(overlayFont != null)
            lightEnabled.setTypeface(overlayFont);
      }
      
      if(diffuseButton != null)
      {
         diffuseButton.setOnClickListener(this);
         
         if(overlayFont != null)
            diffuseButton.setTypeface(overlayFont);
      }
      
      if(specularButton != null)
      {
         specularButton.setOnClickListener(this);
         
         if(overlayFont != null)
            specularButton.setTypeface(overlayFont);
      }
      
      if(camLightEnabled != null)
      {
         camLightEnabled.setOnClickListener(this);
         
         if(overlayFont != null)
            camLightEnabled.setTypeface(overlayFont);
      }
      
      if(camLightText != null)
      {
         camLightText.setOnClickListener(this);
         
         if(overlayFont != null)
            camLightText.setTypeface(overlayFont);
      }
      
      if(fpsView != null)
      {
         if(overlayFont != null)
            fpsView.setTypeface(overlayFont);
      }
      
      onClick(null, 0);
      
      return true;
   }
   
   //@Override
   public void onClick(View v)
   {
      if(oglView == null)
         return;
      
      int colour;
      ColourPicker colourPicker;

      viewID = v.getId();
      AlertDialog.Builder alertBuilder;
      
      switch(viewID)
      {
         case R.id.selectionButton:
            
            alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(parentActivity, Preferences.THEME));
            alertBuilder.setTitle("Select Object");
            alertBuilder.setSingleChoiceItems(selectionList, selection, this);
            AlertDialog selectionAlert = alertBuilder.create();

            selectionAlert.show();
            
            dialogTitle = "Selection";
            
            break;
            
         case R.id.diffuseButton:
            colour = oglView.getDiffuse(selectionValues[selection]);

            colourPicker = new ColourPicker(parentActivity, colour);
            colourPicker.setOnColorChangedListener(this);

            propertyView = colourPicker.findViewById(R.layout.color_picker_dialog);

            colourPicker.show();

            break;
            
         case R.id.specularButton:
            colour = oglView.getSpecular(selectionValues[selection]);

            colourPicker = new ColourPicker(parentActivity, colour);
            colourPicker.setOnColorChangedListener(this);

            propertyView = colourPicker.findViewById(R.layout.color_picker_dialog);

            colourPicker.show();
            
            break;
            
         case R.id.enabledCheckbox:
            oglView.enableLight(selectionValues[selection], lightEnabled.isChecked());
            oglView.updateLights();
            break;
            
         case R.id.cameraLightText:
            boolean value = camLightEnabled.isChecked();
            camLightEnabled.setChecked(!value);
            
         case R.id.enabledCameraLight:
            
            SettingsContainer settings = oglView.getSettings();
            settings.useCameraLight = camLightEnabled.isChecked();
            
            oglView.setSettings(settings);
            oglView.getRenderer().getScene().updateLights();
            
            break;
      }
   }
   
   //@Override
   public void onColorChanged(int color)
   {
      switch(viewID)
      {
         case R.id.diffuseView:
            if(propertyView != null)
               propertyView.findViewById(R.id.diffuseView).setBackgroundColor(color);
            
         case R.id.diffuseButton:
            diffuseColour = color;
            diffuseButton.setBackgroundColor(color);
            diffuseButton.setTextColor(Utilities.invertColour(color));
            oglView.setDiffuse(selectionValues[selection], color);
            
            break;
            
         case R.id.specularView:
            if(propertyView != null)
               propertyView.findViewById(R.id.specularView).setBackgroundColor(color);
            
         case R.id.specularButton:
            specularColour = color;
            specularButton.setBackgroundColor(color);
            specularButton.setTextColor(Utilities.invertColour(color));
            oglView.setSpecular(selectionValues[selection], color);
            
            break;
      }
      
      oglView.updateLights();
   }

   @Override
   public void onClick(DialogInterface dialog, int buttonID)
   {
      if(buttonID >= 0 && dialogTitle.equals("Selection"))
      {
         selection = buttonID;
         
         if(oglView != null)
            oglView.setSelection(selection);
         
         if(dialog != null)
            dialog.dismiss();
         
         if(selectionButton == null || lightEnabled == null || diffuseButton == null || specularButton == null || camLightEnabled == null || camLightText == null)
            return;
         
         if(selection == 0)
         {           
            selectionButton.setImageResource(R.drawable.camera);
            
            lightEnabled.setVisibility(View.INVISIBLE);
            diffuseButton.setVisibility(View.INVISIBLE);
            specularButton.setVisibility(View.INVISIBLE);
            
            camLightEnabled.setVisibility(View.VISIBLE);
            camLightText.setVisibility(View.VISIBLE);
            
            return;
         }
         
         switch(selection)
         {
            case 1:
               selectionButton.setImageResource(R.drawable.light1);
               break;
               
            case 2:
               selectionButton.setImageResource(R.drawable.light2);
               break;
               
            case 3:
               selectionButton.setImageResource(R.drawable.light3);
               break;
         }
         
         int diffColour  = oglView.getDiffuse(selectionValues[selection]);
         int specColour  = oglView.getSpecular(selectionValues[selection]);
         boolean enabled = oglView.lightEnabled(selectionValues[selection]);
         
         diffuseButton.setBackgroundColor(diffColour);//oglView.getDiffuse(selectionValues[selection]));
         specularButton.setBackgroundColor(specColour);//oglView.getSpecular(selectionValues[selection]));
         lightEnabled.setChecked(enabled);//oglView.lightEnabled(selectionValues[selection]));
         
         diffuseButton.setTextColor(Utilities.invertColour(oglView.getDiffuse(selectionValues[selection])));
         specularButton.setTextColor(Utilities.invertColour(oglView.getSpecular(selectionValues[selection])));
         
         lightEnabled.setVisibility(View.VISIBLE);
         diffuseButton.setVisibility(View.VISIBLE);
         specularButton.setVisibility(View.VISIBLE);
         camLightEnabled.setVisibility(View.INVISIBLE);
         camLightText.setVisibility(View.INVISIBLE);
         
         lightEnabled.invalidate();
         diffuseButton.invalidate();
         specularButton.invalidate();
         
//         if(overlayView != null)
//            overlayView.invalidate();//.bringToFront();

         lightEnabled.bringToFront();
         diffuseButton.bringToFront();
         specularButton.bringToFront();

//         invalidateOptionsMenu();
         dialogTitle = "";
      }
//      else if(buttonID == AlertDialog.BUTTON_POSITIVE)
//      {
//         if(isLight)
//         {
//            if(propertyView != null)
//               oglView.enableLight(selectionValues[selection], ((CheckBox)propertyView.findViewById(R.id.light_enabled)).isChecked());
//            
//            oglView.setDiffuse(selectionValues[selection], diffuseColour);
//              oglView.setSpecular(selectionValues[selection], specularColour);
//              
//              lightEnabled.setChecked(oglView.lightEnabled(selectionValues[selection]));
//              
//              oglView.updateLights();
//         }
//      }      
   }
}
