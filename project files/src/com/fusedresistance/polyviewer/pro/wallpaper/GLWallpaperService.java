package com.fusedresistance.polyviewer.pro.wallpaper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.fusedresistance.polyviewer.pro.renderer.OGLView;

import android.opengl.GLSurfaceView;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public abstract class GLWallpaperService extends WallpaperService
{
	public class GLEngine extends WallpaperService.Engine
	{
		public final static int RENDERMODE_WHEN_DIRTY = 0;
		public final static int RENDERMODE_CONTINUOUSLY = 1;
		
		private Object lock = new Object();
		private OGLView glSurfaceView = null;
		
		private int debugFlags;
		private int renderMode;
		private List<Runnable> pendingOps = new ArrayList<Runnable>();
		
		public GLEngine()
		{
		}
		
		public void setGLWrapper(final GLSurfaceView.GLWrapper wrapper)
		{
			synchronized(lock)
			{
				if(glSurfaceView != null)
					glSurfaceView.setGLWrapper(wrapper);
				else
					pendingOps.add(new Runnable() { public void run() { setGLWrapper(wrapper); } } );
			}
		}
		
		public void setDebugFlags(final int flags)
		{
			synchronized(lock)
			{
				if(glSurfaceView != null)
					glSurfaceView.setDebugFlags(flags);
				else
				{
					this.debugFlags = flags;
					pendingOps.add(new Runnable() { public void run() { setDebugFlags(flags); } });
				}
			}
		}
		
		public int getDebugFlags()
		{
			synchronized(lock)
			{
				if(glSurfaceView != null)
					return glSurfaceView.getDebugFlags();
				else 
					return this.debugFlags; 
			}
		}
		
		public void setRenderer(final GLSurfaceView.Renderer renderer)
		{
			synchronized(lock)
			{
				if(glSurfaceView != null)
					glSurfaceView.setRenderer(renderer);
				else
					pendingOps.add(new Runnable() { public void run() { setRenderer(renderer); } } );
			}
		}
		
		public void queueEvent(final Runnable runnable)
		{
			synchronized(lock)
			{
				if(glSurfaceView != null)
					glSurfaceView.queueEvent(runnable);
				else
					pendingOps.add(new Runnable() { public void run() { queueEvent(runnable); } } );
			}
		}
		
		public void setEGLContextFactory(final GLSurfaceView.EGLContextFactory factory)
		{
			synchronized(lock)
			{
				if(glSurfaceView != null)
					glSurfaceView.setEGLContextFactory(factory);
				else
					pendingOps.add(new Runnable() { public void run() { setEGLContextFactory(factory); } } );
			}
		}
		
		public void setEGLWindowSurfaceFactory(final GLSurfaceView.EGLWindowSurfaceFactory factory)
		{
			synchronized(lock)
			{
				if(glSurfaceView != null)
					glSurfaceView.setEGLWindowSurfaceFactory(factory);
				else
					pendingOps.add(new Runnable() { public void run() { setEGLWindowSurfaceFactory(factory); } } );
			}
		}
		
		public void setEGLConfigChooser(final GLSurfaceView.EGLConfigChooser configChooser)
		{
			synchronized(lock)
			{
				if(glSurfaceView != null)
					glSurfaceView.setEGLConfigChooser(configChooser);
				else
					pendingOps.add(new Runnable() { public void run() { setEGLConfigChooser(configChooser); } } );
			}
		}
		
		public void setEGLConfigChooser(final boolean needDepth)
		{
			synchronized(lock)
			{
				if(glSurfaceView != null)
					glSurfaceView.setEGLConfigChooser(needDepth);
				else
					pendingOps.add(new Runnable() { public void run() { setEGLConfigChooser(needDepth); } } );
			}
		}
		
		public void setEGLConfigChooser(final int redSize, final int greenSize, final int blueSize, 
										final int alphaSize, final int depthSize, final int stencilSize)
		{
			synchronized(lock)
			{
				if(glSurfaceView != null)
					glSurfaceView.setEGLConfigChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize);
				else
					pendingOps.add(new Runnable() { public void run() { setEGLConfigChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize); } } );
			}
		}
		
		public void setEGLContextClientVersion(final int version)
		{
			synchronized(lock)
			{
				Method method = null;
				try
				{
					method = GLSurfaceView.class.getMethod("setEGLContextClientVersion", int.class);
				}
				catch(NoSuchMethodException e)
				{
					return;
				}
				
				if(glSurfaceView != null)
				{
					try
					{
						method.invoke(glSurfaceView, version);
					}
					catch(Exception e)
					{
						return;
					}
				}
				else
					pendingOps.add(new Runnable() { public void run() { setEGLContextClientVersion(version); } } );
			}
		}
		
		public void setRenderMode(final int renderMode)
		{
			synchronized(lock)
			{
				if(glSurfaceView != null)
					glSurfaceView.setRenderMode(renderMode);
				else
				{
					this.renderMode = renderMode;
					pendingOps.add(new Runnable() { public void run() { setRenderMode(renderMode); } } );
				}
			}
		}
		
		public int getRenderMode()
		{
			synchronized(lock)
			{
				if(glSurfaceView != null)
					return glSurfaceView.getRenderMode();
				else
					return this.renderMode;
			}
		}
		
		public void requestRender()
		{
			if(glSurfaceView != null)
				glSurfaceView.requestRender();
		}
		
		@Override
      public void onVisibilityChanged(final boolean visible) 
		{
         synchronized(lock)
        	{
            super.onVisibilityChanged(visible);
            	
        		if(glSurfaceView != null)
        		{
        			if(visible)
        				glSurfaceView.onResume();
        			else
        				glSurfaceView.onPause();
        		}
        		else
        			pendingOps.add(new Runnable() { public void run() { if(visible) { glSurfaceView.onResume(); } else { glSurfaceView.onPause(); } } } );
        	}
      }

      @Override
      public void onSurfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) 
      {
        	synchronized(lock)
        	{
        	   super.onSurfaceChanged(holder, format, width, height);
        	   
        		if(glSurfaceView != null)
        			glSurfaceView.surfaceChanged(holder, format, width, height);
        		else
        			pendingOps.add(new Runnable() { public void run() { onSurfaceChanged(holder, format, width, height); } } );
        	}
      }

      @Override
      public void onSurfaceCreated(SurfaceHolder holder) 
      {
        	synchronized(lock)
        	{
        	   super.onSurfaceCreated(holder);
        	   
        		if(glSurfaceView == null)
        		{
        			glSurfaceView = new OGLView(GLWallpaperService.this)
        			{
        				@Override
        				public SurfaceHolder getHolder()
        				{
        					return GLEngine.this.getSurfaceHolder();
        				}
        			};
        			
        			for(Runnable pendingOperation: pendingOps)
        				pendingOperation.run();
        			
        			pendingOps.clear();
        		}
        		
        		glSurfaceView.surfaceCreated(holder);
        	}
      }

      @Override
      public void onSurfaceDestroyed(SurfaceHolder holder) 
      {
        	synchronized(lock)
        	{
        	   super.onSurfaceDestroyed(holder);
        	   
        		if(glSurfaceView != null)
        			glSurfaceView.surfaceDestroyed(holder);
        	}
      }
	}
}
