package com.fusedresistance.polyviewer.scene;

import android.graphics.Color;
import android.opengl.Matrix;
import android.util.Log;

import com.fusedresistance.polyviewer.utility.Utilities;

public class Light implements SceneObject
{
	private boolean enabled = true;
	private float[] target = new float[3];
	private float[] position = new float[3];
	private float[] crossProd = new float[4];
	private float[] direction = new float[3];
	private float[] viewMatrix = new float[16];
	private float[] rotationMatrix = new float[16];
	
	private float[] diffuse = new float[3];
	private float[] specular = new float[3];
	
	private float xRotation = 0.0f;
	private float yRotation = 0.0f;
	
	public Light()
	{
		this.reset();
	}
	
	//@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	//@Override
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled; 
	}

	//@Override
	public void translate(float xAmount, float yAmount, float zAmount)
	{
		float[] translation = new float[] { xAmount, yAmount, zAmount };
		
		position = Utilities.addVectors(position, translation);
		target = Utilities.addVectors(target, translation);
		
		this.updateMatrix();
	}

	//@Override
	public void rotate(float xAmount, float yAmount, float zAmount)
	{
		// Unused by the light class currently.
	}

	//@Override
	public void orbit(float horizAmount, float vertAmount)
	{
		xRotation += horizAmount;
		yRotation += vertAmount;
		
		Log.d("XROT", "X rotation: " + xRotation);
		Log.d("YROT", "Y rotation: " + yRotation);
		
		float[] newPos = Utilities.subtractVectors(position, target);
		float[] rotMatrix = new float[16];
		float[] rotatedPos = new float[4];
		
		if(newPos.length >= 4)
			newPos[3] = 1.0f;
		
		Matrix.setRotateM(rotMatrix, 0, horizAmount, 0.0f, 1.0f, 0.0f);
		Matrix.multiplyMV(rotatedPos, 0, rotMatrix, 0, newPos, 0);
		Matrix.multiplyMV(crossProd, 0, rotMatrix, 0, crossProd, 0);
		Matrix.multiplyMM(rotationMatrix, 0, rotMatrix, 0, rotationMatrix, 0);
		
		Matrix.setRotateM(rotMatrix, 0, vertAmount, crossProd[0], crossProd[1], crossProd[2]);
		Matrix.multiplyMV(rotatedPos, 0, rotMatrix, 0, rotatedPos, 0);
		Matrix.multiplyMM(rotationMatrix, 0, rotMatrix, 0, rotationMatrix, 0);
		
		position = Utilities.addVectors(rotatedPos, target);
		direction = Utilities.subtractVectors(target, position);
	}

	//@Override
	public void setTarget(float xValue, float yValue, float zValue)
	{
		target[0] = xValue;
		target[1] = yValue;
		target[2] = zValue;
	}

	//@Override
	public void setPosition(float xValue, float yValue, float zValue)
	{
		position[0] = xValue;
		position[1] = yValue;
		position[2] = zValue;
	}

	//@Override
	public void updateMatrix()
	{
		Matrix.setLookAtM(viewMatrix, 0, position[0], position[1], position[2], 
				  position[0], position[1], position[2], 0, 1, 0);
	}

	//@Override
	public void reset()
	{
		xRotation = 0.0f;
		yRotation = 0.0f;
		
		target = new float[] { 0.0f, 0.0f, 0.0f };
		position = new float[] { 0.0f, 0.0f, 10.0f };
		crossProd = new float[] { -1.0f, 0.0f, 0.0f, 0.0f };
		enabled = true;
		
		direction = Utilities.subtractVectors(target, position);
		
		diffuse = new float[] { 1.0f, 1.0f, 1.0f };
		specular = new float[] { 0.7f, 0.7f, 0.7f };
		
		Matrix.setIdentityM(rotationMatrix, 0);
	}

	//@Override
	public void scale(float xAmount, float yAmount, float zAmount)
	{
		// Not used by the current class.
	}

	//@Override
	public float[] getPosition()
	{
		return position;
	}

	//@Override
	public float[] getTarget()
	{
		return target;
	}

	//@Override
	public float[] getDirection()
	{
		return direction;
	}
	
	public int getDiffuse()
	{
		return Color.rgb((int)(diffuse[0] * 255), 
						 (int)(diffuse[1] * 255), 
						 (int)(diffuse[2] * 255));
	}
	
	public float[] getDiffuseFloat()
	{
		return diffuse;
	}
	
	public int getSpecular()
	{
		return Color.rgb((int)(specular[0] * 255), 
				 		 (int)(specular[1] * 255), 
				 		 (int)(specular[2] * 255));
	}
	
	public float[] getSpecularFloat()
	{
		return specular;
	}
	
	public void setDiffuse(int colour)
	{
		diffuse[0] = Color.red(colour) / 255.0f;
		diffuse[1] = Color.green(colour) / 255.0f;
		diffuse[2] = Color.blue(colour) / 255.0f;
	}
	
	public void setSpecular(int colour)
	{
		specular[0] = Color.red(colour) / 255.0f;
		specular[1] = Color.green(colour) / 255.0f;
		specular[2] = Color.blue(colour) / 255.0f;
	}

	//@Override
	public float[] getMatrix()
	{
		return rotationMatrix;//viewMatrix;
	}
	
	public float[] getViewMatrix()
	{
		return viewMatrix;
	}

	public float getXRotation()
	{
		return xRotation;
	}
	
	public float getYRotation()
	{
		return yRotation;
	}
}
