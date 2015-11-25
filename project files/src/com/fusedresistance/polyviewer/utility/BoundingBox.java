package com.fusedresistance.polyviewer.utility;

import java.io.Serializable;

//import android.util.Log;

import com.fusedresistance.polyviewer.scene.Camera;

public class BoundingBox implements Serializable
{
	private static final long serialVersionUID = 1L;
	private float[] xBounds = new float[2];
	private float[] yBounds = new float[2];
	private float[] zBounds = new float[2];
	
	public BoundingBox(float[] bounds)
	{
		if(bounds.length < 6)
			return;
		
		xBounds[0] = bounds[0];
		xBounds[1] = bounds[1];
		
		yBounds[0] = bounds[2];
		yBounds[1] = bounds[3];
		
		zBounds[0] = bounds[4];
		zBounds[1] = bounds[5];
	}
	
	public float[] getXBounds()
	{
		return xBounds;
	}
	
	public float[] getYBounds()
	{
		return yBounds;
	}
	
	public float[] getZBounds()
	{
		return zBounds;
	}
	
	public boolean setXBounds(float[] newBounds)
	{
		if(newBounds.length < 2)
			return false;
		
		xBounds = new float[] { newBounds[0], newBounds[1] };
		
		return true;
	}
	
	public boolean setYBounds(float[] newBounds)
	{
		if(newBounds.length < 2)
			return false;
		
		yBounds = new float[] { newBounds[0], newBounds[1] };
		
		return true;
	}
	
	public boolean setZBounds(float[] newBounds)
	{
		if(newBounds.length < 2)
			return false;
		
		zBounds = new float[] { newBounds[0], newBounds[1] };
		
		return true;
	}
	
	public float[] getBounds()
	{
		return new float[] { xBounds[0], xBounds[1], yBounds[0], yBounds[1], zBounds[0], zBounds[1] }; 
	}
	
	public float calculateCameraDistance(Camera camera)
	{
		float distance = 0.0f;
		
		if(camera == null)
			return distance;
		
		float fov = camera.getFieldOfView() / 2.0f;
		float width = xBounds[1] - xBounds[0];
		float height = yBounds[1] - yBounds[0];
		float zLength = zBounds[1] - zBounds[0];
		float ratio = camera.getScreenRatio();
		float xDistance = 0.0f;
		float yDistance = 0.0f;
		
//		Log.d("FOV", "Field of View: " + fov);
		
		xDistance = (float)((width) / Math.tan(Utilities.degToRad(fov * ratio)));// + zBounds[1];
		yDistance = (float)((height) / Math.tan(Utilities.degToRad(fov)));// + zBounds[1];

		distance = Math.min(xDistance, yDistance);
//		distance = (float)((maxSize) / Math.tan(Utilities.degToRad(fov))) + zBounds[1];
		
		if(distance < zLength)
			distance += zBounds[1];
		
//		Log.d("DISTANCE", "Distance: " + distance + ", X Distance: " + xDistance + ", Y Distance: " + yDistance);
		
		return distance;
	}
	
	public float getMidPoint()
	{
		float midPoint = 0.0f;
		
		if(yBounds != null)
		{
			midPoint = yBounds[1] - yBounds[0];
			midPoint /= 2.0f;
			midPoint += yBounds[0];
		}
			
		return midPoint;		
	}
	
	public float getRadius()
	{
		float diameter = 0.0f;
		float xLength = xBounds[1] - xBounds[0];
		float yLength = yBounds[1] - yBounds[0];
		float zLength = zBounds[1] - zBounds[0];
		
		diameter = Math.max(xLength, Math.max(yLength, zLength));
		
		return diameter / 2.0f;
	}
}
