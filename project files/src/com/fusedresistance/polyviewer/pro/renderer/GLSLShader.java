package com.fusedresistance.polyviewer.pro.renderer;

import com.fusedresistance.polyviewer.scene.Scene;
import com.fusedresistance.polyviewer.utility.FileLoader;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class GLSLShader
{
	private static final int ATTRIBUTE = 0;
	private static final int UNIFORM = 1;
	
	private int shaderHandle;
	private int mvpMatrixHandle;
	private int modelMatrixHandle;
	private int ambientHandle;
	private int lightHandle;
	private int eyeHandle;
	private int brightnessHandle;
	private int lightDiffuseVec;
	private int lightSpecularVec;
	private int lightDirVec;
	
	private final int[] attributeHandles = new int[3];
	private final int[] textureHandles = new int[Scene.NUM_TEXTURES];
	
	public float ambientAmount;
	public float brightness;
	
	private String[] defines = new String[]{ "#define DIFFUSEMAP\n", "#define NORMALMAP\n", "#define SPECULARMAP\n", "#define EMISSIVEMAP\n" };
	private int[] defineValues = new int[]{ Scene.DEFINE_DIFFUSE, Scene.DEFINE_NORMAL, Scene.DEFINE_SPECULAR, Scene.DEFINE_EMISSIVE };
	private String vertexShader;
	private String fragmentShader;
	private int shaderDefines = 0;
	
	public GLSLShader()
	{
		shaderHandle = 0;	
	}
	
	public GLSLShader(Context context, int vertexID, int fragmentID, int flags)
	{
		String vertex = FileLoader.readShader(context, vertexID);
		String fragment = FileLoader.readShader(context, fragmentID);
		
		loadShader(vertex, fragment, flags);
	}

	public int loadShader(final String vertex, final String fragment, final int flags)
	{
		vertexShader = vertex;
		fragmentShader = fragment;
		shaderDefines = flags;
		
		return buildShader(shaderDefines);
	}
	
	public int buildShader(final int flags)
	{
		if(vertexShader.isEmpty() || fragmentShader.isEmpty())
			return -1;
		
		String vertex = vertexShader;
		String fragment = fragmentShader;
		
		for(int i = 0; i < defineValues.length; ++i)
		{
			if((flags & defineValues[i]) == defineValues[i])
			{
				vertex = defines[i] + vertex;
				fragment = defines[i] + fragment;
			}
		}
		
		try
		{
			GLES20.glDeleteShader(shaderHandle);
						
			int vertexHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
			int fragmentHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
			
			GLES20.glShaderSource(vertexHandle, vertex);
			GLES20.glCompileShader(vertexHandle);
			Log.d("Vertex Shader", GLES20.glGetShaderInfoLog(vertexHandle));
			
			GLES20.glShaderSource(fragmentHandle, fragment);			
			GLES20.glCompileShader(fragmentHandle);
			Log.d("Fragment Shader", GLES20.glGetShaderInfoLog(fragmentHandle));
			
			shaderHandle = GLES20.glCreateProgram();
			
			GLES20.glAttachShader(shaderHandle, vertexHandle);			
			GLES20.glAttachShader(shaderHandle, fragmentHandle);
			
			GLES20.glLinkProgram(shaderHandle);
			
			Log.d("Progam Link Info", GLES20.glGetProgramInfoLog(shaderHandle));
			
			initShaderHandles();
		}
		catch(Exception e)
		{
			Log.d("Shader Setup", e.getLocalizedMessage());
		}
		
		return shaderHandle;
	}
	
	public int getAttributeHandle(final String varName, final int type)
	{
		int output = -1;
		
		switch(type)
		{
			case ATTRIBUTE:
				output = GLES20.glGetAttribLocation(shaderHandle, varName);
				break;
				
			case UNIFORM:
				output = GLES20.glGetUniformLocation(shaderHandle, varName);
				break;
		}
		
		return output;
	}
	
	public int getShaderHandle()
	{
		return shaderHandle;
	}
	
	private void initShaderHandles()
	{
		attributeHandles[0] = GLES20.glGetAttribLocation(shaderHandle, "aPosition");
		attributeHandles[1] = GLES20.glGetAttribLocation(shaderHandle, "aNormal");
		attributeHandles[2] = GLES20.glGetAttribLocation(shaderHandle, "aTextureCoord");
		
		textureHandles[0] = GLES20.glGetUniformLocation(shaderHandle, "diffuseSampler");
		textureHandles[1] = GLES20.glGetUniformLocation(shaderHandle, "normalSampler");
		textureHandles[2] = GLES20.glGetUniformLocation(shaderHandle, "specularSampler");
		textureHandles[3] = GLES20.glGetUniformLocation(shaderHandle, "emissiveSampler");
		textureHandles[4] = GLES20.glGetUniformLocation(shaderHandle, "reflectiveSampler");
		
		ambientHandle = GLES20.glGetUniformLocation(shaderHandle, "ambient");
		brightnessHandle = GLES20.glGetUniformLocation(shaderHandle, "brightness");
		mvpMatrixHandle = GLES20.glGetUniformLocation(shaderHandle, "mvpMatrix");
		modelMatrixHandle = GLES20.glGetUniformLocation(shaderHandle, "modelMatrix");
		ambientHandle = GLES20.glGetUniformLocation(shaderHandle, "ambient");
		brightnessHandle = GLES20.glGetUniformLocation(shaderHandle, "brightness");
		lightHandle = GLES20.glGetUniformLocation(shaderHandle, "uLightDir");
		eyeHandle = GLES20.glGetUniformLocation(shaderHandle, "uEyeDir");
		lightDiffuseVec = GLES20.glGetUniformLocation(shaderHandle, "lightDiffuse");
		lightSpecularVec = GLES20.glGetUniformLocation(shaderHandle, "lightSpec");
		lightDirVec = GLES20.glGetUniformLocation(shaderHandle, "lightDir");
	}
	
	public void renderModel(RenderContainer renderData)
	{
		float[] mvpMatrix = new float[16]; 
				
		GLES20.glUseProgram(shaderHandle);
		
		GLES20.glUniform1f(ambientHandle, ambientAmount);
		GLES20.glUniform1f(brightnessHandle, brightness);
		GLES20.glUniform3fv(lightHandle, 1, renderData.lightDir, 0);
		GLES20.glUniform3fv(eyeHandle, 1, renderData.cameraDir, 0);
		GLES20.glUniformMatrix3fv(lightDiffuseVec, 1, false, renderData.lightDiffuse, 0);
		GLES20.glUniformMatrix3fv(lightSpecularVec, 1, false, renderData.lightSpec, 0);
		GLES20.glUniformMatrix3fv(lightDirVec, 1, false, renderData.lightDir, 0);
		
		for(int i = 0; i < renderData.modelArray.length; i++)
		{
			Matrix.multiplyMM(mvpMatrix, 0, renderData.viewMatrix, 0, renderData.modelArray[i].modelMatrix, 0);
			Matrix.multiplyMM(mvpMatrix, 0, renderData.projMatrix, 0, mvpMatrix, 0);
			
			GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
			GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, renderData.modelArray[i].modelMatrix, 0);
			
			renderData.modelArray[i].render(attributeHandles, textureHandles, renderData.drawMode);
		}
	}

	public void release()
	{
		GLES20.glDeleteShader(shaderHandle);		
	}	
}
