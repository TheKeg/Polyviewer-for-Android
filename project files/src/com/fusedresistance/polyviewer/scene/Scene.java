package com.fusedresistance.polyviewer.scene;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Hashtable;
import java.util.Random;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.fusedresistance.polyviewer.pro.R;
import com.fusedresistance.polyviewer.pro.renderer.GLSLShader;
import com.fusedresistance.polyviewer.pro.renderer.RenderContainer;
import com.fusedresistance.polyviewer.settings.SettingsContainer;
import com.fusedresistance.polyviewer.settings.Preferences;
import com.fusedresistance.polyviewer.utility.FileLoader;
import com.fusedresistance.polyviewer.utility.Utilities;

public class Scene extends Thread
{
	public static final int DEFINE_DIFFUSE = 2;
	public static final int DEFINE_NORMAL = 4;
	public static final int DEFINE_SPECULAR = 8;
	public static final int DEFINE_EMISSIVE = 16;
	public static final int DEFINE_FULLBRIGHT = 32;
	public static final int DEFINE_WIREFRAME = 64;
	
	public static int MODEL_SIZE = 6;
	public static int NUM_TEXTURES = MODEL_SIZE - 1;

   public static final Object renderLock = 0;

   public ObjModel[] models;
   public SettingsContainer settingsContainer;
   public CameraPresets camPresets;
   public LightPresets lightPresets;
   public Camera camera;
   public Light[] lights;

	// Scene variables
	private ObjModel fsQuad;
	private ObjModel lightPointer;
   private Hashtable<String, GLSLShader> shaders = new Hashtable<String, GLSLShader>();
   private RenderContainer renderData = new RenderContainer();
	private Context context;
	private float[] identityMatrix = new float[16];
	private int prevShaderFlag = 0;
	
	// Shader handles
	private int useOffsetHandle;
	private int offsetHandle;
//	private int bgBrightnessHandle;
//	private int[] cubemapTexture;
	
	// Emissive Shader variables
	private int[] frameBuffer = new int[2];
	private int[] renderBuffer = new int[2];
	private int[] renderTexture = new int[2];
//	private IntBuffer textureBuffer;
//	private IntBuffer blurBuffer;
//	private CameraPresets camPresets;
//	private LightPresets lightPresets;
	private int SCREEN_WIDTH = 0;
	private int SCREEN_HEIGHT = 0;
	private int blurTextureHandle;
	private int blurStrengthHandle;
//	private int blurGlowHandle;
	
	// Background color and background texture adjustment vectors
	public float[] screenOffsets = new float[2];
	private float[] texAdjust = new float[2];
	private float[] uvWidths = new float[2];
	private float[] textureArea = new float[2];
	private float[] textureRes = new float[2];
	
	// Count values
	private double count = 0.0;
	private long prevTime = 0;
	private long prevFPSTime = 0;
	private int frameCount = 0;
	private float prevCamRotAmount = 0.0f;
	private Handler fpsHandler;
	
	public Scene(Context newContext, String folderName)
	{	
		models = new ObjModel[0];
		settingsContainer = new SettingsContainer();
		context = newContext;
		camera = new Camera(720, 1280, settingsContainer.fieldOfView);
		lights = new Light[3];
		
		lights[0] = new Light();
		lights[0].setEnabled(true);
		
		lights[1] = new Light();
		lights[1].setEnabled(false);
		
		lights[2] = new Light();
		lights[2].setEnabled(false);

      settingsContainer.meshFolder = folderName;
      settingsContainer.backgroundColour = new float[] { 0.3f, 0.3f, 0.3f};

      Random randGen = new Random();
      randGen.setSeed(SystemClock.elapsedRealtime());

      Matrix.setIdentityM(identityMatrix, 0);

		this.setupPresets();
	}
	
	public void renderScene()
	{
		try
		{
			long currTime = System.currentTimeMillis();
			
			int delay = (int) (currTime - prevTime);
			
			if(delay < settingsContainer.frameLimit)
				Thread.sleep(settingsContainer.frameLimit - delay);

			currTime = System.currentTimeMillis();

			frameCount++;
			delay = (int) (currTime - prevFPSTime);
			
			if(delay >= 1000)
			{
				double fps = (((double)frameCount) / delay) * 1000;
				
				frameCount = 0;
				prevFPSTime = currTime;
				
				if(fpsHandler != null)
				{
					Message msg = fpsHandler.obtainMessage();
					msg.arg1 = (int)fps;

               Log.d("FPS", "" + fps);

					fpsHandler.sendMessage(msg);
				}
			}
			
			float amt = ((currTime - prevTime) / 1000.0f) * 2.0f; 
			
			prevTime = currTime;
			count += amt;
			
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
			GLES20.glViewport(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
			
			renderData.cameraDir = camera.getDirection();
			
			synchronized(renderLock)//models)
			{
            synchronized(shaders)
            {
            if(settingsContainer.useBackground)
            {
               GLES20.glDisable(GLES20.GL_DEPTH_TEST);

               if(fsQuad != null)
               {
                  if(shaders.containsKey("Background"))
                     GLES20.glUseProgram(shaders.get("Background").getShaderHandle());

//                  GLES20.glUniform1f(bgBrightnessHandle, settingsContainer.brightness);
                  GLES20.glUniform1i(useOffsetHandle, settingsContainer.useMotion ? 1 : 0);
                  GLES20.glUniform2fv(offsetHandle, 1, texAdjust, 0);

                  renderData.modelArray = new ObjModel[] { fsQuad };
                  renderData.viewMatrix = identityMatrix;
                  renderData.projMatrix = identityMatrix;

                  if(shaders.containsKey("Background"))
                     shaders.get("Background").renderModel(renderData);
               }

               GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            }

            if(settingsContainer.useLightAssist)
            {
               lightPointer.setRotationMatrix(lights[settingsContainer.lightIndex].getMatrix());

               renderData.modelArray = new ObjModel[] { lightPointer };
               renderData.viewMatrix = camera.getViewMatrix();
               renderData.projMatrix = camera.getProjectionMatrix();
               renderData.drawMode = GLES20.GL_TRIANGLES;

               if(shaders.containsKey("FullBright"))
               {
                  shaders.get("FullBright").buildShader(DEFINE_DIFFUSE);
                  shaders.get("FullBright").renderModel(renderData);
               }
            }

            if(!settingsContainer.useMotion && settingsContainer.useRotation)
            {
               if(settingsContainer.rotateCamera)
               {
                  camera.orbit(settingsContainer.rotationSpeed / 20.0f * settingsContainer.rotationDirection, 0.0f);
               }
               else
               {
                  float yaw = (float)(count * settingsContainer.rotationSpeed * settingsContainer.rotationDirection);

                  synchronized(renderLock)
                  {
                     if(models != null)
                     {
                        for(ObjModel objModel : models)//(int i = 0; i < models.length; i++)
                        {
                           if(objModel == null)
                              continue;

                           objModel.rotate(0.0f, yaw, 0.0f);
                        }
                     }
                  }
               }
            }

            String shaderName = settingsContainer.shaderName;
            boolean useEmissive = false;
            boolean useWireframe = false;

            if(shaderName.contains("Wireframe"))
            {
               shaderName = shaderName.substring(0, shaderName.lastIndexOf("Wireframe"));
               useWireframe = true;
            }

            if(shaderName.contains("Emissive")  && !shaderName.contains("FullBright"))
            {
               shaderName = shaderName.substring(0, shaderName.lastIndexOf("Emissive"));
               useEmissive = true;
            }

            if(shaders.containsKey(shaderName) && models != null)
            {
               if(prevShaderFlag != settingsContainer.shaderFlags)
               {
                  prevShaderFlag = settingsContainer.shaderFlags;
                  shaders.get(shaderName).buildShader(settingsContainer.shaderFlags);
               }

               renderData.modelArray = models;
               renderData.viewMatrix = camera.getViewMatrix();
               renderData.projMatrix = camera.getProjectionMatrix();
               renderData.drawMode = GLES20.GL_TRIANGLES;

               if(useWireframe)
                  GLES20.glPolygonOffset(5.0f, 5.0f);

               shaders.get(shaderName).renderModel(renderData);

               if(useWireframe)
               {
                  GLES20.glPolygonOffset(0.0f, 0.0f);

                  renderData.drawMode = GLES20.GL_LINE_LOOP;
                  shaders.get("Wireframe").renderModel(renderData);

                  renderData.drawMode = GLES20.GL_TRIANGLES;
               }

               if(useEmissive)
                  this.renderEmissive();
            }
            else
               Log.d("RENDER", "Could not find: " + settingsContainer.shaderName);
            }
			}
		}
		catch(Exception e)
		{
			Log.d("OGL Render", e.getLocalizedMessage());
		}
	}
	
	private void renderEmissive()
	{
		if(frameBuffer[0] == 0)
			return;
		
		GLSLShader horizShader = shaders.get("GaussianBlurHorizontalPass");
		GLSLShader vertShader = shaders.get("GaussianBlurVerticalPass");
		GLES20.glViewport(0, 0, SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2);
		
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, 
									  GLES20.GL_TEXTURE_2D, renderTexture[0], 0);
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, 
										 GLES20.GL_RENDERBUFFER, renderBuffer[0]);
		
//		int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		
		if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE)
		{
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,  GLES20.GL_TEXTURE_2D, 0, 0);
			GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, 0);
			return;
		}
		
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		shaders.get("EmissivePass").renderModel(renderData);

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glUseProgram(horizShader.getShaderHandle());
		
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, 
				  GLES20.GL_TEXTURE_2D, renderTexture[1], 0);
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, 
				 GLES20.GL_RENDERBUFFER, renderBuffer[0]);
		
//		status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		
		if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE)
		{
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, 0, 0);
			GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, 0);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			return;
		}
			
		blurStrengthHandle = horizShader.getAttributeHandle("blurStrength", 1);
		blurTextureHandle  = horizShader.getAttributeHandle("blurSampler", 1);

		GLES20.glUniform1f(blurStrengthHandle, settingsContainer.emissiveStr);
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE6);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTexture[0]);
		GLES20.glUniform1i(blurTextureHandle, 6);
		
		float[] identityMatrix = new float[16];
		Matrix.setIdentityM(identityMatrix, 0);
		
		renderData.modelArray = new ObjModel[] { fsQuad };
		renderData.viewMatrix = identityMatrix;
		renderData.projMatrix = identityMatrix;
		
		horizShader.renderModel(renderData);
		
		GLES20.glViewport(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
		GLES20.glUseProgram(vertShader.getShaderHandle());
		
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, 
		GLES20.GL_TEXTURE_2D, 0, 0);
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, 
										 GLES20.GL_RENDERBUFFER, 0);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

		if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE)
		{
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, 0, 0);
			GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, 0);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			return;
		}
		
		blurStrengthHandle = vertShader.getAttributeHandle("blurStrength", 1);
		blurTextureHandle  = vertShader.getAttributeHandle("blurSampler", 1);
		int blurGlowHandle = vertShader.getAttributeHandle("glowSampler", 1);
		
		GLES20.glUniform1f(blurStrengthHandle, 1.75f);
	
		GLES20.glActiveTexture(GLES20.GL_TEXTURE7);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTexture[1]);
		GLES20.glUniform1i(blurTextureHandle, 7);
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE6);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTexture[0]);
		GLES20.glUniform1i(blurGlowHandle, 6);
		
		vertShader.renderModel(renderData);
		
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		GLES20.glClearColor(settingsContainer.backgroundColour[0], settingsContainer.backgroundColour[1], settingsContainer.backgroundColour[2], 1.0f);
	}
		
	public void updateCameraProjection(int width, int height)
	{
		SCREEN_WIDTH = width;
		SCREEN_HEIGHT = height;
		
		if(camera != null)
		{
			camera.adjustProjection(width, height, settingsContainer.fieldOfView);
		}
		
		if(fsQuad != null)
		{
			float[] uvOffsets = new float[2];
			float ratio = (float) width / height;
			float halfRatio = ratio / 2.0f;
			
			if(width < height)
			{
				if(textureRes[0] != textureRes[1])
					halfRatio = ((width * (textureRes[1] / height)) / textureRes[0]) / 2.0f;
				
				uvOffsets[0] = 0.5f - halfRatio;
				uvOffsets[1] = 0.0f;
				uvWidths[0] = halfRatio;
			}
			else
			{
				if(textureRes[0] != textureRes[1])
					halfRatio = ((height * (textureRes[0] / width)) / textureRes[1]) / 2.0f;
				
				uvOffsets[0] = 0.0f;
				uvOffsets[1] = 0.5f - halfRatio;
				uvWidths[1] = halfRatio;
			}
			
			float[] texValues = new float[] { textureArea[0], textureArea[1] };
			
			if(settingsContainer.useBackground)
				FileLoader.adjustFullscreenPlane(fsQuad, uvOffsets, texValues);
		}
		
		this.buildFrameBuffer();
	}
		
	public Camera getCamera()
	{
		return camera;
	}
		
	public Light getLight(int index)
	{
		if(index >= lights.length || index < 0)
			return null;
		
		return lights[index];
	}
		
//	public ObjModel getModel(int index)
//	{
//		if(index >= models.length || index < 0)
//			return null;
//		else
//			return models[index];
//	}
		
	public void updateLights()
	{
		float[] diffuse 	= new float[9];
		float[] spec 		= new float[9];
		float[] dir 		= new float[9];
		float[] lightDir;// 	= new float[3];
		float[] lightDiff;//	= new float[3];
		float[] lightSpec;//	= new float[3];
		int index = 0;
		
		if(settingsContainer.useCameraLight)
		{
			lightDir = camera.getDirection();
			lightDiff = lights[0].getDiffuseFloat();
			lightSpec = lights[0].getSpecularFloat();
			
			for(int i = 0; i < 3; ++i)
			{
				diffuse[i] = lightDiff[i];
				spec[i] = lightSpec[i];
				dir[i] = lightDir[i];
			}
		}
		else
		{
			for(Light light : lights)//(int i = 0; i < lights.length; ++i)
			{
				lightDir = light.getDirection();
				lightDiff = light.getDiffuseFloat();
				lightSpec = light.getSpecularFloat();
				
				for(int j = 0; j < 3; ++j)
				{
					if(light.isEnabled())
					{
						diffuse[index] = lightDiff[j];
						spec[index] = lightSpec[j];
					}
					else
					{
						diffuse[index] = 0.0f;
						spec[index] = 0.0f;
					}
					
					dir[index++] = lightDir[j];
				}
			}
		}
		
		renderData.lightDiffuse = diffuse;
		renderData.lightSpec = spec;
		renderData.lightDir = dir;
	}
	
	public void loadData()
	{
		this.loadShaders();
		this.loadModels();
		
		prevTime = System.currentTimeMillis();
		updateLights();
	}
	
	private void loadModels()
	{
		boolean noMesh = false;
		float[] textureInfo;// = new float[NUM_TEXTURES];
		int[] meshTextureArray;// = new int[NUM_TEXTURES];
		String[] fileData = new String[] { "" };
		String[] imgFileLocations = new String[] { "", "", "", "", "" };
		
		if(!settingsContainer.meshFolder.isEmpty())
         fileData = FileLoader.parseFolder(settingsContainer.meshFolder);
      else if(!settingsContainer.wallpaperFolder.isEmpty())
         fileData = FileLoader.parseFolder(settingsContainer.wallpaperFolder);
	   else if(!settingsContainer.galleryFolder.isEmpty())
         fileData = FileLoader.parseFolder(settingsContainer.galleryFolder);

		int numModels = 0;

		if(fileData != null)
			numModels = fileData.length / MODEL_SIZE;

      synchronized(renderLock)
      {
         if(models == null || settingsContainer.isWallpaper)
            models = new ObjModel[numModels];
         else if(models.length < numModels)
            models = new ObjModel[numModels];


         for(int i = 0; i < models.length; i++)
         {
            if(fileData.length >= NUM_TEXTURES)
            {
               for(int j = 0; j < NUM_TEXTURES; j++)
                  imgFileLocations[j] = fileData[i * MODEL_SIZE + 1 + j];
            }

            // Load the model and texture
            if(models[i] == null || settingsContainer.isWallpaper)
            {
               models[i] = FileLoader.loadObj(context, R.raw.cube, fileData[i * MODEL_SIZE]);

               if(models[i] == null)
                  continue;

               models[i].setName(settingsContainer.meshFolder);

               settingsContainer.sceneBounds = FileLoader.MaxBounds(null, models[i].getBoundingBox());

               noMesh = true;
            }
            else if(!models[i].getName().contains(settingsContainer.meshFolder))
            {
               models[i].deleteTextures();
               models[i] = FileLoader.loadObj(context, R.raw.cube, fileData[i * MODEL_SIZE]);

               if(models[i] == null)
                  continue;

               models[i].setName(settingsContainer.meshFolder);

               settingsContainer.sceneBounds = FileLoader.MaxBounds(null, models[i].getBoundingBox());

               noMesh = true;
            }
            else
            {
               models[i].deleteTextures();
               settingsContainer.sceneBounds = FileLoader.MaxBounds(null, models[i].getBoundingBox());
            }

            int[] resourceID = new int[] { R.raw.diffuse, R.raw.normal, R.raw.specular,
                                    R.raw.emissive, R.raw.emissive };

            meshTextureArray = FileLoader.loadTexture(context, resourceID, imgFileLocations);
            models[i].setTexture(meshTextureArray);
            models[i].setPosition(0.0f, 0.0f, 0.0f);

            if(imgFileLocations[3].length() > 0)
            {
               //Log.d("EMISSIVE", "Model " + MESH_FOLDER + " has an emissive texture.");
               models[i].setEmissive(true);
            }
         }
      }

		if(settingsContainer.sceneBounds != null)
		{	
			float midPoint = settingsContainer.sceneBounds.getMidPoint();
			float distance = settingsContainer.sceneBounds.calculateCameraDistance(camera);
			
			if(noMesh)
			{
				Log.d("VALUES", "Mid point: " + midPoint + ", Distance: " + distance);
				
				camera.reset(midPoint, distance);
				camera.orbit(settingsContainer.cameraPreset[0], settingsContainer.cameraPreset[1]);
			}
			
			if(settingsContainer.useMotion && (settingsContainer.rotateCamera || settingsContainer.useCameraPan))
			{
				if(!noMesh)
					camera.reset(midPoint, distance);
				
				prevCamRotAmount = 0.0f;
				offsetsChanged(screenOffsets[0], screenOffsets[1]);
			}
			
		}

      if(shaders.containsKey("Background"))
      {
         offsetHandle = shaders.get("Background").getAttributeHandle("uvOffset", 1);
         useOffsetHandle = shaders.get("Background").getAttributeHandle("useOffset", 1);
      }

		// Create and set the background texture quad
		float[] adjust = new float[] { 0.0f, 0.0f };
		
		if(fsQuad == null)
			fsQuad = FileLoader.createFullscreenPlane(adjust);
	
		textureInfo = FileLoader.loadTexture(context, R.raw.specular, settingsContainer.backgroundImage); 
		
		fsQuad.setTexture(new int[] { (int)textureInfo[0] });
		textureArea = new float[] { textureInfo[1], textureInfo[2] };
		textureRes = new float[] { textureInfo[3], textureInfo[4] };
		
		int[] resourceID = new int[] { R.raw.diffuse, R.raw.normal, R.raw.specular, 
								 R.raw.emissive, R.raw.emissive };
		String[] imgLocations = new String[] { "", "", "", "", "" };
		
		lightPointer = FileLoader.loadObj(context, R.raw.pointer, "");
		lightPointer.setTexture(FileLoader.loadTexture(context, resourceID, imgLocations));
	}
	
	
	private void loadShaders()
	{
		GLSLShader shader;

      // Create the shader program for the background shader.
      shader = new GLSLShader(context, R.raw.background_vertex, R.raw.background_fragment, 0);
      shader.brightness = 1.0f;
      shader.ambientAmount = 1.0f;
      shaders.put("Background", shader);

      // Create the shader program for full bright shader.
      shader = new GLSLShader(context, R.raw.background_vertex, R.raw.fullbright_fragment, settingsContainer.shaderFlags);
      shader.brightness = 1.0f;
      shaders.put("FullBright", shader);

      // Create the shader program for wireframe display.
      shader = new GLSLShader(context, R.raw.wireframe_vertex, R.raw.wireframe_fragment, 0);
      shaders.put("Wireframe", shader);

      // Create the shader program for material display.
      shader = new GLSLShader(context, R.raw.material_vertex, R.raw.material_fragment, settingsContainer.shaderFlags);//DEFINE_DIFFUSE | DEFINE_SPECULAR | DEFINE_NORMAL);
      shader.brightness = 1.0f;
      shaders.put("Material", shader);

      // Create the shader program for a horizontal blur.
      shader = new GLSLShader(context, R.raw.blur_vertex, R.raw.blur_horizontal_fragment, 0);
      shaders.put("GaussianBlurHorizontalPass", shader);

      // Create the shader program for vertical blur.
      shader = new GLSLShader(context, R.raw.blur_vertex, R.raw.blur_vertical_fragment, 0);
      shaders.put("GaussianBlurVerticalPass", shader);

      // Create the shader program for drawing the emissive textures.
      shader = new GLSLShader(context, R.raw.background_vertex, R.raw.fullbright_fragment, DEFINE_EMISSIVE);
      shader.brightness = 1.0f;
      shaders.put("EmissivePass", shader);
	}
	
	private void buildFrameBuffer()
	{
		// Generate the buffers.
		GLES20.glGenFramebuffers(1, frameBuffer, 0);
		GLES20.glGenRenderbuffers(1, renderBuffer, 0);
		GLES20.glGenTextures(2, renderTexture, 0);
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTexture[0]);
		
		// Setup texture parameters
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		
		// Create the texture buffers.
//		int[] buffer = new int[(SCREEN_WIDTH / 2) * (SCREEN_HEIGHT / 2)];
      IntBuffer textureBuffer = ByteBuffer.allocateDirect((SCREEN_WIDTH / 2) * (SCREEN_HEIGHT / 2) * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		
		// Generate the texture
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0, 
							GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, textureBuffer);
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTexture[1]);
		
		// Setup texture parameters
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		
		// Create the texture buffers.
      IntBuffer blurBuffer = ByteBuffer.allocateDirect((SCREEN_WIDTH / 2) * (SCREEN_HEIGHT / 2) * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		
		// Generate the texture
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0, 
							GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, blurBuffer);
		
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBuffer[0]);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2);

      if(shaders.containsKey("GaussianBlurPass"))
      {
         GLSLShader shader = shaders.get("GaussianBlurPass");

         blurTextureHandle = shader.getAttributeHandle("blurSampler", 1);
         blurStrengthHandle = shader.getAttributeHandle("blurStrength", 1);
      }
	}
	
	private void setupPresets()
	{
		camPresets = new CameraPresets(context, Preferences.CAMERA_DATABASE);
		lightPresets = new LightPresets(context, Preferences.LIGHT_DATABASE);
		
		camPresets.open();
		lightPresets.open();
	}
	
	public void resetScene()
	{
		camera.setFieldOfView(settingsContainer.fieldOfView);

      synchronized(renderLock)
      {
         if(models != null)
         {
            for(int i = 0; i < models.length; i++)
            {
               if(models[i] != null)
               {
                  models[i].reset();

                  if(settingsContainer.isWallpaper && !settingsContainer.useMotion && !settingsContainer.rotateCamera && !settingsContainer.useRotation)
                     models[i].rotate(0.0f, settingsContainer.maxAngle, 0.0f);

                  if(i == 0)
                     camera.reset(models[i].getMidPoint(), models[i].getHeight());
               }
            }
         }
         else if(settingsContainer.galleryFolder.length() == 0)
            camera.reset(0.0f, 0.0f);

         camera.orbit(settingsContainer.cameraPreset[0], settingsContainer.cameraPreset[1]);
      }
	}
	
	
	/**
	 * Method to transform the camera or objects based upon the homescreen for the live wallpaper app.
	 * @param xAmount Amount of change along the horizontal plane. 
	 * @param yAmount Amount of change along the vertical plane.
	 */
	public void offsetsChanged(float xAmount, float yAmount)
	{
		// Set the offset values.
		screenOffsets = new float[] { xAmount, yAmount };
		
		// Convert the xAmount value from 0 - 1 to a -1 to 1 range. 
		float rotAmount = xAmount * 2.0f - 1.0f;
		
		// Change the background image texture offset along the x axis
		float ratio = uvWidths[0];
		float length = (this.textureArea[0] - ratio) - ratio;
		float position = ratio + (length * xAmount);
		
		// Store the interpolated value to be passed into the shader.
		texAdjust[0] = Utilities.interpolate(ratio, this.textureArea[0] - ratio, position) - 0.5f;
		
		// Rotate the models in the scene.
      synchronized(renderLock)
      {
         if(models != null && !settingsContainer.rotateCamera && !settingsContainer.useCameraPan)
         {
            for(ObjModel objModel : models)
            {
               if(objModel == null)
                  continue;

               if(settingsContainer.useMotion)
                  objModel.rotate(0.0f, rotAmount * -settingsContainer.maxAngle * settingsContainer.rotationDirection, 0.0f);
            }
         }
      }
		
		// Rotate the camera
		if(settingsContainer.rotateCamera)
		{
			if(settingsContainer.useCameraTarget)
				camera.orbitTarget((rotAmount - prevCamRotAmount) * -settingsContainer.maxAngle * settingsContainer.rotationDirection, 0.0f);
			else
				camera.orbit((rotAmount - prevCamRotAmount) * settingsContainer.maxAngle * settingsContainer.rotationDirection, 0.0f);
			
			prevCamRotAmount = rotAmount;
		}
		// Pan the camera
		else if(settingsContainer.useCameraPan)
		{
			camera.translate((rotAmount - prevCamRotAmount) * settingsContainer.maxAngle, 0.0f, 0.0f);
			prevCamRotAmount = rotAmount;
		}
		
	}
	
	/**
	 * Set the handler to update the fps display in the overlay view.
	 * @param handler Reference to the handler that handles setting the fps view. 
	 */
	public void setHandler(Handler handler)
	{
		fpsHandler = handler;
	}
	
//	/**
//	 * Returns the camera preset class.
//	 * @return CameraPreset class.
//	 */
//	public CameraPresets getCameraPresets()
//	{
//		return camPresets;
//	}
//
//	/**
//	 * Returns the light presets class.
//	 * @return LightPresets class.
//	 */
//	public LightPresets getLightPresets()
//	{
//		return lightPresets;
//	}
	
	/**
	 * Sets the lighting values based on the name of the preset passed in.
	 * @param presetName Name of the preset.
	 */
	public void setLightPreset(String presetName)
	{
		if(lightPresets == null)
			return;

		// Retrieve the values based upon the preset name.
		float[] presetValues = lightPresets.getPreset(presetName);
		
		int index = 0;
		
		// Update the light values if there are any based upon the preset name.
		if(presetValues != null)
		{	
			// Set the values for light 1 if the array is long enough.
			if(presetValues.length >= 4)
			{
				lights[0].reset();
				lights[0].setEnabled(true);
				lights[0].orbit(presetValues[index++], presetValues[index++]);
				lights[0].setDiffuse((int)presetValues[index++]);
				lights[0].setSpecular((int)presetValues[index++]);
				
				lights[1].setEnabled(false);
				lights[2].setEnabled(false);
			}
			
			// Set the values for light 2 if the array is long enough.
			if(presetValues.length >= 8)
			{
				lights[1].reset();
				lights[1].setEnabled(true);
				lights[1].orbit(presetValues[index++], presetValues[index++]);
				lights[1].setDiffuse((int)presetValues[index++]);
				lights[1].setSpecular((int)presetValues[index++]);
			}
			
			// Set the values for light 3 if the array is long enough.
			if(presetValues.length >= 12)
			{
				lights[2].reset();
				lights[2].setEnabled(true);
				lights[2].orbit(presetValues[index++], presetValues[index++]);
				lights[2].setDiffuse((int)presetValues[index++]);
				lights[2].setSpecular((int)presetValues[index]);
			}
			
			updateLights();
		}
	}
	
	public void setCameraPreset(String presetName)
	{
		float[] values = camPresets.getPreset(presetName);
		
		if(values != null)
		{
			camera.reset();
			camera.orbit(values[0], values[1]);
		}
	}

	public void release()
	{
      synchronized(renderLock)
      {
         if(models != null)
         {
            for(ObjModel objModel : models)
            {
               if(objModel != null)
                  objModel.release();
            }

            models = null;
         }
      }

      if(shaders != null)
      {
         //Iterator<GLSLShader> shaderIter = shaders.values().iterator();

         for(GLSLShader shader : shaders.values())//(Iterator<GLSLShader> shaderIter = shaders.values().iterator(); shaderIter.hasNext();)
         {
            shader.release();
//				shaderIter.next().release();
         }

         shaders.clear();
      }


      if(frameBuffer != null && renderBuffer != null && renderTexture != null)
		{
			GLES20.glDeleteFramebuffers(1, frameBuffer, 0);
			GLES20.glDeleteRenderbuffers(1, renderBuffer, 0);
			GLES20.glDeleteTextures(2, renderTexture, 0);
		}
	}
	
	public void setSelection(final int selection)
	{
		if(selection >= 0  && selection < lights.length)
		{
			settingsContainer.lightIndex = selection;
			settingsContainer.useLightAssist = true;
			
			float camDistance = camera.getDistance();
			float zoom = camDistance / 50.0f;
			
			lightPointer.reset();
			lightPointer.scale(zoom, zoom, zoom);
			
			float xRot = lights[settingsContainer.lightIndex].getXRotation();
			float yRot = lights[settingsContainer.lightIndex].getYRotation();
			int diffuse = lights[settingsContainer.lightIndex].getDiffuse();
			int specular = lights[settingsContainer.lightIndex].getSpecular();
			boolean enabled = lights[settingsContainer.lightIndex].isEnabled();
			
			lights[settingsContainer.lightIndex].reset();
			
			lights[settingsContainer.lightIndex].setEnabled(enabled);
			lights[settingsContainer.lightIndex].orbit(xRot, yRot);
			lights[settingsContainer.lightIndex].setDiffuse(diffuse);
			lights[settingsContainer.lightIndex].setSpecular(specular);
		}
		else
			settingsContainer.useLightAssist = false;
	}

	public void saveLightPreset(String presetName)
	{
		if(lightPresets == null)
			return;
		
		float[] lightVals = new float[13];
		float lightCount = 0;
		
		for(int i = 0; i < lights.length; ++i)
		{
			if(lights[i].isEnabled())
			{
				int index = (i * 4) + 1;
				lightCount++;
				lightVals[index++] = lights[i].getXRotation();
				lightVals[index++] = lights[i].getYRotation();
				lightVals[index++] = lights[i].getDiffuse();
				lightVals[index] = lights[i].getSpecular();
				
				Log.d("XROT", "X rotation: " + lights[i].getXRotation());
				Log.d("YROT", "Y rotation: " + lights[i].getYRotation());
			}
		}
		
		lightVals[0] = lightCount;
		
		lightPresets.addPreset(presetName, lightVals);
	}

	public void saveCameraPreset(String presetName)
	{
		if(camPresets == null)
			return;
		
		float[] camVals = new float[2];

		camVals[0] = camera.getXRotation();
		camVals[1] = camera.getYRotation();
		
		camPresets.addPreset(presetName, camVals);
	}
	
//	public void setSettings(SettingsContainer settings)
//	{
//		settings.galleryFolder = settingsContainer.galleryFolder;
//		settingsContainer = settings;
//	}
//
//	public SettingsContainer getSettings()
//	{
//		return settingsContainer;
//	}
//
//   public void removeCameraPreset(String presetName)
//   {
//      camPresets.removePreset(presetName);
//   }
//
//   public void removeLightPreset(String presetName)
//   {
//      lightPresets.removePreset(presetName);
//   }
}
