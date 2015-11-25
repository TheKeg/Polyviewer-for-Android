package com.fusedresistance.polyviewer.scene;

import android.opengl.Matrix;
//import android.util.Log;

import com.fusedresistance.polyviewer.utility.Utilities;

public class Camera implements SceneObject
{
	private static final float nearClip = 1.0f;
	private static final float farClip = 2000.0f;
	private boolean enabled = true;
	private float[] target = new float[3];
	private float[] position = new float[3];
	private float[] crossProd = new float[4];
	private float[] direction = new float[3];
	private float[] viewMatrix = new float[16];
	private float[] projMatrix = new float[16];
	private float xRotation = 0.0f;
	private float yRotation = 0.0f;
	
	private float camDistance = 25.0f;
	private float camHeight = 0.0f;
	private float zoom = 0.0f;
	private float fieldOfView = 90.0f;
	private float ratio = 1.0f;
	
	public Camera(int width, int height, float sceneFOV)
	{
		fieldOfView = sceneFOV;
		this.adjustProjection(width, height, sceneFOV);
		this.reset(0.0f, 30.0f);
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
		// Unused by the camera class currently.
	}
	
	//@Override
	public void orbit(float horizAmount, float vertAmount)
	{
		xRotation += horizAmount;
		yRotation += vertAmount;
		
		float[] newPos = Utilities.subtractVectors(position, target);
		float[] rotMatrix = new float[16];
		float[] rotatedPos = new float[4];
		
		if(newPos.length >= 4)
			newPos[3] = 1.0f;
		
		Matrix.setRotateM(rotMatrix, 0, horizAmount, 0.0f, 1.0f, 0.0f);
		Matrix.multiplyMV(rotatedPos, 0, rotMatrix, 0, newPos, 0);
		Matrix.multiplyMV(crossProd, 0, rotMatrix, 0, crossProd, 0);
		
		Matrix.setRotateM(rotMatrix, 0, vertAmount, crossProd[0], crossProd[1], crossProd[2]);
		Matrix.multiplyMV(rotatedPos, 0, rotMatrix, 0, rotatedPos, 0);
		
		position = Utilities.addVectors(rotatedPos, target);
		direction = Utilities.subtractVectors(target, position);
		
		this.updateMatrix();
	}
	
	public void orbitTarget(float horizAmount, float vertAmount)
	{
		float[] newTar = Utilities.subtractVectors(target, position);
		float[] rotMatrix = new float[16];
		float[] rotatedTar = new float[4];
		
		if(newTar.length >= 4)
			newTar[3] = 1.0f;
		
		Matrix.setRotateM(rotMatrix, 0, horizAmount, 0.0f, 1.0f, 0.0f);
		Matrix.multiplyMV(rotatedTar, 0, rotMatrix, 0, newTar, 0);
		Matrix.multiplyMV(crossProd, 0, rotMatrix, 0, crossProd, 0);
		
		Matrix.setRotateM(rotMatrix, 0, vertAmount, crossProd[0], crossProd[1], crossProd[2]);
		Matrix.multiplyMV(rotatedTar, 0, rotMatrix, 0, rotatedTar, 0);
		
		target = Utilities.addVectors(rotatedTar, position);
		direction = Utilities.subtractVectors(target, position);
		
		this.updateMatrix();
	}
	
	public void pan(float horizAmount, float vertAmount)
	{
		float[] movementVector = Utilities.normalizeVector(crossProd);
		movementVector = Utilities.multiplyVector(movementVector, horizAmount);
		
		this.translate(movementVector[0], vertAmount, movementVector[2]);
	}
	
	public void zoom(final float amount)
	{
		direction = Utilities.subtractVectors(position, target);
		float[] dir = Utilities.normalizeVector(direction);
		zoom = zoom * amount;
		
		float[] movementAmount = new float[] { dir[0] * zoom, 
				dir[1] * zoom,
				dir[2] * zoom };
		
		position = Utilities.addVectors(movementAmount, target);
		direction = Utilities.subtractVectors(target,  position);
		this.updateMatrix();
	}
	
	//@Override
	public void setTarget(float xValue, float yValue, float zValue)
	{
		target[0] = xValue;
		target[1] = yValue;
		target[2] = zValue;
		
		this.updateMatrix();
	}

	//@Override
	public void setPosition(float xValue, float yValue, float zValue)
	{
		position[0] = xValue;
		position[1] = yValue;
		position[2] = zValue;
		
		this.updateMatrix();
	}

	//@Override
	public void updateMatrix()
	{
		Matrix.setLookAtM(viewMatrix, 0, position[0], position[1], position[2], 
						  target[0], target[1], target[2], 0, 1, 0);
	}
	
	public void adjustProjection(float width, float height, float fov)
	{
		ratio = width / height;
		fieldOfView = fov;
		
		projMatrix = Utilities.perspectiveMatrix(fov, ratio, nearClip, farClip);
	}

	public void reset(float height, float distance)
	{
		camHeight = height;
		
		if(distance > 0.0f)
		{	
			camDistance = distance;
			zoom = distance;
		}
		else
		{
			camDistance = zoom = 30.0f;
		}
		
		reset();
	}
	
	//@Override
	public void reset()
	{
		xRotation = 0.0f;
		yRotation = 0.0f;
		
		crossProd = new float[] { 1.0f, 0.0f, 0.0f, 0.0f };
		position = new float[] { 0.0f, camHeight, camDistance };
		target = new float[] { 0.0f, camHeight, 0.0f };
		direction = Utilities.subtractVectors(target, position);
		zoom = camDistance;
		
		this.updateMatrix();	
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
	
	//@Override
	public float[] getMatrix()
	{
		return null;
	}
	
	public float[] getProjectionMatrix()
	{
		return projMatrix;
	}

	public float[] getViewMatrix()
	{
		return viewMatrix;
	}
	
	public float getFieldOfView()
	{
		return fieldOfView;
	}
	
	public float getScreenRatio()
	{
		return ratio ;
	}

	public float getDistance()
	{
		return Utilities.vectorLength(Utilities.subtractVectors(position, target));
	}
	
	public void setFieldOfView(float newFOV)
	{
		fieldOfView = newFOV;
		
		reset();
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
