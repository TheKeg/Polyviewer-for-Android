package com.fusedresistance.polyviewer.pro.renderer;

import com.fusedresistance.polyviewer.scene.CameraPresets;
import com.fusedresistance.polyviewer.scene.LightPresets;
import com.fusedresistance.polyviewer.scene.Scene;
import com.fusedresistance.polyviewer.settings.SettingsContainer;

import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
//import android.widget.TextView;
import android.content.Context;
import android.graphics.PixelFormat;

public class OGLView extends GLSurfaceView
{
	private OGLRenderer oglRenderer;
	private float xPrev;
	private float yPrev;
	private GestureDetector gestures;
	private ScaleGestureDetector scaleGestures;
	private boolean scaling = false;
	private boolean gesture = false;
	private int selection = 0;
	private boolean isGallery = false;
	private Handler handler = null;
   private CameraPresets cameraPresets;

   public OGLView(Context context, String meshFolder)
	{
		super(context);
		
		if(oglRenderer == null)
		{
			setEGLContextClientVersion(2);
//			setEGLConfigChooser(8, 8, 8, 8, 16, 0);

			try
			{
				this.getHolder().setFormat(PixelFormat.RGBX_8888);
			}
			catch(Exception e)
			{
				setEGLConfigChooser(false);
			}
			
			
			oglRenderer = new OGLRenderer(context, meshFolder);
			
			setRenderer(oglRenderer);
		}
		
		gestures = new GestureDetector(context, new GestureListener());
		scaleGestures = new ScaleGestureDetector(context, new ScaleListener());
	}
	
	public OGLView(Context context)
	{
		super(context);
		
		gestures = new GestureDetector(context, new GestureListener());
		scaleGestures = new ScaleGestureDetector(context, new ScaleListener());
	}
	
	public OGLRenderer getRenderer()
	{
		return oglRenderer;
	}
	
	@Override
	public void setRenderer(Renderer renderer)
	{
		super.setRenderer(renderer);
		
		oglRenderer = (OGLRenderer)renderer;
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		if(oglRenderer != null)
			oglRenderer.onPause();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		if(oglRenderer != null)
			oglRenderer.onResume();
	}
		
	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		try
		{
			if(e.getPointerCount() < 3)
			{
				gestures.onTouchEvent(e);
				scaleGestures.onTouchEvent(e);
			}
			
			switch(e.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					
	//				Log.d("ACTION DOWN", "Pointer Count: " + e.getPointerCount());
					
					xPrev = e.getX();
					yPrev = e.getY();
					
					break;
					
				case MotionEvent.ACTION_UP:
					
	//				Log.d("ACTION UP", "Pointer Count: " + e.getPointerCount());
					
					scaling = false;
					gesture = false;
					
					break;
					
				case MotionEvent.ACTION_CANCEL:
					
					gesture = false;
					scaling = false;
					
					break;
					
				case MotionEvent.ACTION_MOVE:
					
					if(scaling || gesture || isGallery)
						break;
					
					float xVal = e.getX();
					float yVal = e.getY();
					float dx = xVal - xPrev;
					float dy = yVal - yPrev;
					Scene scene = oglRenderer.getScene();
					SettingsContainer settings = scene.settingsContainer;//.getSettings();
					
					if(e.getPointerCount() > 2)
						scene.getCamera().pan(dx / 10.0f, dy / 10.0f);
					else
					{
						if(selection == 0)
						{
							scene.getCamera().orbit(-dx / 2.0f, -dy / 2.0f);
							
							if(settings.useCameraLight)
								scene.updateLights();
						}
						else
						{
							scene.getLight(selection - 1).orbit(dx / 2.0f, dy / -2.0f);
							scene.updateLights();
						}
					}
					
					xPrev = xVal;
					yPrev = yVal;
					
					break;
			}
		}
		catch(IllegalArgumentException iae)
		{
			Log.d("TOUCH ERROR", iae.toString());
			return false;
		}
		
		return true;
	}

   public CameraPresets getCameraPresets()
   {
      if(oglRenderer == null)
         return null;

      return oglRenderer.getScene().camPresets;
   }

   public LightPresets getLightPresets()
   {
      if(oglRenderer == null)
         return null;

      return oglRenderer.getScene().lightPresets;
   }

   public void setCameraPreset(String presetName)
   {
      if(oglRenderer == null)
         return;

      oglRenderer.getScene().setCameraPreset(presetName);
   }

   public void setLightPreset(String presetName)
   {
      if(oglRenderer == null)
         return;

      oglRenderer.getScene().setLightPreset(presetName);
   }

   public void removeCameraPreset(String presetName)
   {
      CameraPresets cameraPresets = this.getCameraPresets();

      if(cameraPresets == null)
         return;

      cameraPresets.removePreset(presetName);
   }

   public void removeLightPreset(String presetName)
   {
      LightPresets lightPresets = this.getLightPresets();

      if(lightPresets == null)
         return;

      lightPresets.removePreset(presetName);
   }

   public void saveCameraPreset(String presetName)
   {
      if(oglRenderer == null)
         return;

      oglRenderer.getScene().saveCameraPreset(presetName);
   }

   public void saveLightPreset(String presetName)
   {
      if(oglRenderer == null)
         return;

      oglRenderer.getScene().saveLightPreset(presetName);
   }

   private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
	{
		@Override
		public boolean onScale(ScaleGestureDetector detector)
		{
			if(oglRenderer != null)
			{
				float scale = detector.getScaleFactor();
				
				if(scale > 1.000f)
					scale = 1.0f - (scale - 1.0f);
				else
					scale = Math.abs(scale - 1.0f) + 1.0f;
				
				oglRenderer.getScene().getCamera().zoom(scale);
				scaling = true;
			}
			
			return true;
		}
	}
	
	private class GestureListener extends GestureDetector.SimpleOnGestureListener
	{		
		@Override
		public boolean onDown(MotionEvent e)
		{
			return super.onDown(e);
		}
		
		
		@Override
		public boolean onDoubleTap(MotionEvent e)
		{			
			if(oglRenderer != null)
			{
				if(selection == 0)
					oglRenderer.getScene().getCamera().reset();
				else
				{
					oglRenderer.getScene().getLight(selection - 1).reset();
					oglRenderer.getScene().updateLights();
					
					if(handler != null)
					{
						Message msg = handler.obtainMessage();
						handler.sendMessage(msg);
					}
				}
				
				gesture = true;
				
				return true;
			}
			else
				return super.onDoubleTap(e);
		}
	}

	public void setGalleryMode(final boolean enableGallery)
	{
		isGallery = enableGallery;
	}

	public void setSelection(int index)
	{
		selection = index;

      oglRenderer.getScene().setSelection(index - 1);
	}

	public int getDiffuse(int index)
	{
		if(oglRenderer == null || index >= 3)
			return 0;
		else
			return oglRenderer.getScene().getLight(index).getDiffuse();
	}
	
	public int getSpecular(int index)
	{
		if(oglRenderer == null || index >= 3)
			return 0;
		else
			return oglRenderer.getScene().getLight(index).getSpecular();
	}

	public void setDiffuse(final int index, final int colour)
	{
		if(oglRenderer != null)
			oglRenderer.getScene().getLight(index).setDiffuse(colour);
	}

	public void setSpecular(final int index, final int colour)
	{
		if(oglRenderer != null)
			oglRenderer.getScene().getLight(index).setSpecular(colour);
	}
	
	public void enableLight(final int index, final boolean enabled)
	{
		if(oglRenderer != null)
			oglRenderer.getScene().getLight(index).setEnabled(enabled);
	}
	
	public boolean lightEnabled(final int index)
	{
		if(oglRenderer != null)
			if(index <= 0 && index < 3)
				return oglRenderer.getScene().getLight(index).isEnabled();
			else
				return false;
		else
			return false;
	}

	public void updateLights()
	{
		if(oglRenderer != null)
			oglRenderer.getScene().updateLights();
	}

	public void release()
	{
		if(oglRenderer != null)
		{
			oglRenderer.release();
			oglRenderer = null;
		}		
	}

	public void setLightHandler(Handler lightHandler)
	{
		handler = lightHandler;
	}

	public SettingsContainer getSettings() 
	{
		if(oglRenderer != null)
			return oglRenderer.getSettings();
		
		return null;
	}

	public void setSettings(SettingsContainer settings)
	{
		if(oglRenderer != null)
			oglRenderer.setSettings(settings);
		
	}
}
