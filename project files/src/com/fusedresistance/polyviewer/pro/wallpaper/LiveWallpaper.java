package com.fusedresistance.polyviewer.pro.wallpaper;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;

import com.fusedresistance.polyviewer.pro.renderer.OGLRenderer;
import com.fusedresistance.polyviewer.settings.Preferences;

import android.content.Context;
import android.util.Log;

public class LiveWallpaper extends GLWallpaperService
{
	public static WeakReference<LiveWallpaper> wallpaperService = null;
	private static LinkedList<WeakReference<PolyviewerGLEngine>> engines = new LinkedList<WeakReference<PolyviewerGLEngine>>();
	
	public LiveWallpaper()
	{
		super();
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
	}
	
	@Override
	public void onDestroy()
	{
	   super.onDestroy();
	   
		if(wallpaperService != null)
		{
		   wallpaperService.clear();
			wallpaperService = null;
		}
		
		synchronized(engines)
		{
			for(WeakReference<PolyviewerGLEngine> engineRef: engines)
			{
				if(engineRef != null)
					engineRef.clear();
			}
		}		
	}
		
	@Override
	public Engine onCreateEngine()
	{
		PolyviewerGLEngine engine = new PolyviewerGLEngine(getApplicationContext());
		
		synchronized(engines)
		{
			engines.add(new WeakReference<PolyviewerGLEngine>(engine));
		}
		
		engine.renderer.setPreferences(getSharedPreferences(Preferences.LIVE_WALLPAPER_PREFS, 0));
		
		return engine;
	}
	
	class PolyviewerGLEngine extends GLEngine
	{
		public OGLRenderer renderer = null;
		private float xOffset = 0.0f;
		private float yOffset = 0.0f;
		
		public PolyviewerGLEngine(Context context)
		{
			super();
			
			if(context == null)
			{
				Log.e("ENGINE ERROR", "No context passed in");
				return;
			}
			
			this.setEGLContextClientVersion(2);
			this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			
			int renderMode = RENDERMODE_CONTINUOUSLY;
			
			renderer = new OGLRenderer(context, "");
			
			this.setRenderer(renderer);
			this.setRenderMode(renderMode);
		}
		
		@Override
		public void onDestroy()
		{
			synchronized(engines)
			{
				super.onDestroy();
				
				if(renderer != null)
				{
					Log.d("RELEASE", "Release method being called");
					renderer.release();
					renderer = null;
				}
				
				Iterator<WeakReference<PolyviewerGLEngine>> iter = engines.iterator();
				
				while(iter.hasNext())
				{
					PolyviewerGLEngine engine = iter.next().get();
					
					if(engine == this)
					{
						iter.remove();
						return;
					}
				}
			}
		}
		
		@Override 
		public void onVisibilityChanged(boolean visible)
		{
			super.onVisibilityChanged(visible);
			
			if(renderer == null)
			   return;
			
			if(visible)
			{
				renderer.onResume();
				renderer.offsetsChanged(xOffset, yOffset);
			}
			else
				renderer.onPause();
		}
		
		@Override
      public void onOffsetsChanged(float xOffset, float yOffset, 
        							        float xOffsetStep, float yOffsetStep, 
        							        int xPixelOffset, int yPixelOffset)
      {
			this.xOffset = xOffset;
			this.yOffset = yOffset;

        	if(renderer != null)
        		renderer.offsetsChanged(xOffset, yOffset);
      }
	}
}
