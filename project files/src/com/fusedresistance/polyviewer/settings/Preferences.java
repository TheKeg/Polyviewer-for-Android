package com.fusedresistance.polyviewer.settings;

import android.content.DialogInterface;
import android.widget.SeekBar;

import com.fusedresistance.polyviewer.pro.R;
import com.fusedresistance.polyviewer.pro.R.style;

public class Preferences
{
   public static final long   DB_UPDATE = 1366416000;
	public static final String SHARED_PREFS_NAME = "polyviewerPrefs";
	public static final String LIVE_WALLPAPER_PREFS = "polyviewerWallpaperPrefs";
	public static final String PREF_SHOW_BG = "pref_showBackground";
	public static final String PREF_SELECT_BG = "pref_selectBackground";
	public static final String PREF_COLOUR_PICKER = "pref_backgroundColour";
	public static final String PREF_BG_COLOUR = "bgColour";
	public static final String PREF_HIGH_QUALITY = "pref_perfBoost";
	public static final String PREF_BG_IMG_LOC = "galleryLocation";
	public static final String PREF_MOTION = "pref_enableMotion";
	public static final String PREF_BRIGHTNESS = "pref_brightness";
	public static final String PREF_SPIN_SPEED = "pref_spinSpeed";
	public static final String PREF_MOVEMENT_AMOUNT = "pref_movementAmount";
	public static final String PREF_ORBIT_CAMERA = "pref_enableCamera";
	public static final String PREF_ROTATION_DIR = "pref_reverseDirection";
	public static final String PREF_MESH_FOLDER = "pref_meshFolder";
   public static final String PREF_WALLPAPER_FOLDER = "pref_wallpaperFolder";
	public static final String PREF_RESET = "pref_reset";
	public static final String PREF_MESH_SELECTION = "pref_selectModel";
	public static final String PREF_SHADER_NAME = "pref_shaderName";
	public static final String PREF_TEXTURE = "pref_selectTexture";
	public static final String PREF_FPS = "pref_fpsLimit";
	public static final String PREF_EMISSIVE = "pref_emissive";
	public static final String PREF_DISPLAY_MODES = "pref_selectDisplayMode";
	public static final String PREF_ENABLE_ROTATION = "pref_enableRotation";
	public static final String PREF_ENABLE_FPS_DISPLAY = "pref_enabledFPS";
	public static final String PREF_CAMERA_FOV = "pref_cameraFOV";
	public static final String PREF_SCREEN_ON = "pref_forceSceenOn";
	public static final String PREF_CAMERA_X_DEFAULT = "pref_defaultX";
	public static final String PREF_CAMERA_Y_DEFAULT = "pref_defaultY";
	public static final String PREF_LIGHT_PRESET = "pref_lightPreset";
	public static final String PREF_CAMERA_PRESET = "pref_cameraPreset";
	public static final String PREF_SHADER_FLAGS = "pref_shaderFlag";
	public static final String PREF_ROTATION_DIRECTION = "pref_rotationDirection";
	public static final String PREF_CAMERA_ZOOM = "pref_cameraZoom";
	public static final String PREF_DISPLAY_SLIDER = "pref_angleSlider";
	public static final String PREF_CAM_MODEL_TOGGLE = "pref_viewControl";
	public static final String PREF_DEFAULT_SHADER = "pref_defaultShader";
	public static final String PREF_DELETE_CACHE = "pref_rebuild";
	public static final String PREF_DELETE_PATHS = "pref_clearPaths";
	public static final String PREF_DELETE_LIGHTS = "pref_clearLightPresets";
	public static final String PREF_DELETE_CAMERAS = "pref_clearCameraPresets";
	public static final String PREF_CAMERA_LIGHT = "pref_useCameraLight";
	public static final String PREF_DATABASE_UPDATE = "pref_dbUpdate";
	
	
	/// NEW "MORE OPTIMAL" SETTINGS VALUES
	
	// Option masks
	public static final int PREF_SHOWBG       = 0x1;
	public static final int PREF_FPS_TOGGLE   = 0x2;
	public static final int PREF_SCREEN_STAY  = 0x4;
	public static final int PREF_QUALITY_SPEC = 0x8;

	// Light and Dark shades
	public static final int LIGHT_COLOUR = 0xFFCCCCCC;
	public static final int DARK_COLOUR  = 0xFF333333;
	
	public static final String PREF_OPTIONS = "pref_options";
	
	public static final int THEME              = R.style.PolyDark;//R.style.Theme_Sherlock;
   public static final int LIGHT_THEME        = R.style.PolyLight;
//   public static final String POLYVIEWER_SHARED_PREFS = "polyviewerSharedPrefs";
   public static final String CAMERA_DATABASE = "CameraPresetsPro.db";
   public static final String LIGHT_DATABASE  = "LightPresetsPro.db";
   public static final String PATH_DATABASE   = "PolyviewerPathsPro.db";


   public static final int BRIGHTNESS_SEEKBAR = 0;
   public static final int BRIGHTNESS_DEF_VAL = 100;
   public static final int BRIGHTNESS_MIN_VAL = 50;
   public static final int BRIGHTNESS_MAX_VAL = 150;

   public static final int EMISSIVE_SEEKBAR = 1;
   public static final int EMISSIVE_DEF_VAL = 100;
   public static final int EMISSIVE_MIN_VAL = 0;
   public static final int EMISSIVE_MAX_VAL = 100;

   public static final int CAMERA_FOV_SEEKBAR = 2;
   public static final int CAMERA_FOV_DEF_VAL = 90;
   public static final int CAMERA_FOV_MIN_VAL = 60;
   public static final int CAMERA_FOV_MAX_VAL = 160;


   public static final int TEXTURE_DIALOG = 0;
   public static final int BRIGHTNESS_DIALOG = 1;
   public static final int EMISSIVE_DIALOG = 2;
   public static final int CAMERA_FOV_DIALOG = 3;
   public static final int CAMERA_PRESET_DIALOG = 4;
   public static final int LIGHT_PRESET_DIALOG = 5;


//   public Preferences()
//   {
//
//   }
//
//   public void initialize()
//   {
//
//   }
//
//   @Override
//   public void onClick(DialogInterface dialog, int which)
//   {
//
//   }
//
//   @Override
//   public void onClick(DialogInterface dialog, int which, boolean isChecked)
//   {
//
//   }
//
//   @Override
//   public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
//   {
//
//   }
//
//   @Override
//   public void onStartTrackingTouch(SeekBar seekBar)
//   {
//
//   }
//
//   @Override
//   public void onStopTrackingTouch(SeekBar seekBar)
//   {
//
//   }
}
