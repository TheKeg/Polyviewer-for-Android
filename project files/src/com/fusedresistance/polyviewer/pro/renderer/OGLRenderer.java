package com.fusedresistance.polyviewer.pro.renderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.fusedresistance.polyviewer.scene.Scene;
import com.fusedresistance.polyviewer.settings.Preferences;
import com.fusedresistance.polyviewer.settings.SettingsContainer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
//import android.widget.TextView;

public class OGLRenderer implements GLSurfaceView.Renderer, SharedPreferences.OnSharedPreferenceChangeListener
{
	private boolean paused = false;
	private Scene scene;
	
	
	public OGLRenderer(Context context, String folderName)
	{
		scene = new Scene(context, folderName);
	}
	
	public void onPause()
	{
		paused = true;
	}
	
	public void onResume()
	{
		paused = false;
	}
	
	@Override
	public void onDrawFrame(GL10 arg0)
	{
		if(paused)
			return;
		
		try
		{
			scene.renderScene();
		}
		catch(Exception e)
		{
			Log.e("DRAW FRAME", e.getLocalizedMessage());
		}
	}	
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		GLES20.glViewport(0, 0, width, height);
		
		scene.updateCameraProjection(width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		// Setup the render target clear values
		SettingsContainer settings = scene.settingsContainer;//.getSettings();
		
		GLES20.glClearColor(settings.backgroundColour[0], settings.backgroundColour[1], settings.backgroundColour[2], 1.0f);
		GLES20.glClearDepthf(1.0f);
		
		// Set the depth buffer
		GLES20.glDepthMask(true);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
		// Enable alpha blending (toggle option in future iterations)
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		GLES20.glLineWidth(1.0f);
		GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL);
		
		// Enable backface culling
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glCullFace(GLES20.GL_BACK);
		
		scene.loadData();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key)
	{
      SettingsContainer settings;

      if(scene != null)
         settings = scene.settingsContainer;
      else
         return;

		if(settings == null)
      {
         settings = new SettingsContainer();
         scene.settingsContainer = settings;
      }

		// Data Values
		settings.wallpaperFolder = sharedPrefs.getString(Preferences.PREF_WALLPAPER_FOLDER, "");
		settings.shaderName 	    = sharedPrefs.getString(Preferences.PREF_SHADER_NAME, "Material");

      Log.d("MESH FOLDER", settings.wallpaperFolder);

		// Visual Values
		settings.backgroundImage = sharedPrefs.getString(Preferences.PREF_BG_IMG_LOC, "");
		settings.brightness 	    = (float)(sharedPrefs.getInt(Preferences.PREF_BRIGHTNESS, 100)) / 100.0f;
		settings.displayFPS 	    = sharedPrefs.getBoolean(Preferences.PREF_ENABLE_FPS_DISPLAY, true);
		settings.emissiveStr	    = (float)(sharedPrefs.getInt(Preferences.PREF_EMISSIVE, 100)) / 100.0f;
		settings.frameLimit 	    = Integer.parseInt(sharedPrefs.getString(Preferences.PREF_FPS, "16"));
		settings.useAlpha 		 = sharedPrefs.getBoolean(Preferences.PREF_HIGH_QUALITY, true);
		settings.useBackground   = sharedPrefs.getBoolean(Preferences.PREF_SHOW_BG, false);
		settings.shaderFlags	    = sharedPrefs.getInt(Preferences.PREF_SHADER_FLAGS, Scene.DEFINE_DIFFUSE | Scene.DEFINE_SPECULAR | Scene.DEFINE_NORMAL);
		
		// Movement Values
		settings.maxAngle 		 = sharedPrefs.getInt(Preferences.PREF_MOVEMENT_AMOUNT, 45);
		settings.useMotion 		 = sharedPrefs.getBoolean(Preferences.PREF_MOTION, false);
		settings.useRotation 	 = sharedPrefs.getBoolean(Preferences.PREF_ENABLE_ROTATION, false);
		settings.rotationSpeed 	 = sharedPrefs.getInt(Preferences.PREF_SPIN_SPEED, 2) * 10;
		
		// Camera & Light Values
		settings.fieldOfView 	 = sharedPrefs.getInt(Preferences.PREF_CAMERA_FOV, 90);
		settings.rotateCamera 	 = sharedPrefs.getBoolean(Preferences.PREF_ORBIT_CAMERA, false);
		settings.useCameraLight  = sharedPrefs.getBoolean(Preferences.PREF_CAMERA_LIGHT, false);
		
		String lightPreset = sharedPrefs.getString(Preferences.PREF_LIGHT_PRESET, "Direct Downward");
		String bgStr       = sharedPrefs.getString(Preferences.PREF_BG_COLOUR, "#FF333333");
		int xRot           = sharedPrefs.getInt(Preferences.PREF_CAMERA_X_DEFAULT, 0);
		int yRot           = sharedPrefs.getInt(Preferences.PREF_CAMERA_Y_DEFAULT, 0);
      int colour         = Color.parseColor(bgStr);
		int index          = Integer.parseInt(sharedPrefs.getString(Preferences.PREF_DISPLAY_MODES, "-1"));

      int settingsFlag       = sharedPrefs.getInt(Preferences.PREF_OPTIONS, 0);
      settings.displayFPS 	  = (settingsFlag & Preferences.PREF_FPS_TOGGLE) == Preferences.PREF_FPS_TOGGLE;//sharedPrefs.getBoolean(Preferences.PREF_ENABLE_FPS_DISPLAY, true);
      settings.useBackground = (settingsFlag & Preferences.PREF_SHOWBG) == Preferences.PREF_SHOWBG;//sharedPrefs.getBoolean(Preferences.PREF_SHOW_BG, false);
      settings.useAlpha 	  = (settingsFlag & Preferences.PREF_QUALITY_SPEC) == Preferences.PREF_QUALITY_SPEC;//sharedPrefs.getBoolean(Preferences.PREF_HIGH_QUALITY, false);

//      Log.d("DISPLAY FPS", "" + settings.displayFPS);
//      Log.d("HQ SPEC", "" + settings.useAlpha);

		settings.cameraPreset = new float[] { xRot, yRot };
		settings.backgroundColour = new float[] { Color.red(colour) / 255.0f, Color.green(colour) / 255.0f, Color.blue(colour) / 255.0f };
		settings.rotationDirection = Integer.parseInt(sharedPrefs.getString(Preferences.PREF_ROTATION_DIRECTION, "0")) > 0 ? 1.0f : -1.0f;

		if(index >= 0)
		{
			settings.maxAngle = sharedPrefs.getInt(Preferences.PREF_DISPLAY_SLIDER, 45);
			settings.isWallpaper = true;			
			settings.useMotion = false;
			settings.rotateCamera = false;
			settings.useRotation = false;
			settings.useCameraTarget = false;
			settings.useCameraPan = false;
			
			if(index == 1 || index == 2)
			{
				boolean value = index == 1;
				settings.rotationSpeed = sharedPrefs.getInt(Preferences.PREF_DISPLAY_SLIDER, 2) * 10;
				settings.useMotion = false;
				settings.useCameraTarget = false;
				settings.useCameraPan = false;
				
				settings.rotateCamera = value;
				settings.useRotation = !value;
			}
			
			if(index > 2)
				settings.useMotion = true;
			
			if(index > 3 && index < 6)
				settings.rotateCamera = true;
			
			if(index == 5)
				settings.useCameraTarget = true;
			
			if(index == 6)
				settings.useCameraPan = true;
		}
	
		//scene.settingsContainer = settings;//.setSettings(settings);

      synchronized(scene.renderLock)
      {
         if(scene.models == null || settings.isWallpaper)
         {
            scene.resetScene();
            scene.setLightPreset(lightPreset);
            scene.offsetsChanged(scene.screenOffsets[0], scene.screenOffsets[1]);
         }
      }
	}

	public void setPreferences(SharedPreferences sharedPreferences)
	{
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		onSharedPreferenceChanged(sharedPreferences, null);
	}
	
	public void offsetsChanged(float xAmount, float yAmount)
	{
		if(scene == null)
			return;
		
		scene.offsetsChanged(xAmount, yAmount);
	}
	
	public Scene getScene()
	{
		return scene;
	}

	public void release()
	{
		scene.release();
	}

	public SettingsContainer getSettings() 
	{
		return scene.settingsContainer;//.getSettings();
	}

	public void setSettings(SettingsContainer settings)
	{
		scene.settingsContainer = settings;//.setSettings(settings);
	}
}
