package com.fusedresistance.polyviewer.utility;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.opengl.Matrix;

public class Utilities
{
	public static float[] addVectors(final float[] vectorA, final float[] vectorB)
	{
		float[] output = new float[4];
		
		output[0] = vectorA[0] + vectorB[0];
		output[1] = vectorA[1] + vectorB[1];
		output[2] = vectorA[2] + vectorB[2];
		
		if(vectorA.length == 4 && vectorB.length == 4)
			output[3] = vectorA[3] + vectorB[3];
		
		return output;
	}
	
	public static int invertColour(int colour)
	{
		int output = colour;
		
		int red = 255 - Color.red(colour);
		int green = 255 - Color.green(colour);
		int blue = 255 - Color.blue(colour);
		
		output = Color.rgb(red, green, blue);
		
		return output;
	}
	
	public static float[] subtractVectors(final float[] vectorA, final float[] vectorB)
	{
		float[] output = new float[4];
		
		output[0] = vectorA[0] - vectorB[0];
		output[1] = vectorA[1] - vectorB[1];
		output[2] = vectorA[2] - vectorB[2];
		
		if(vectorA.length == 4 && vectorB.length == 4)
			output[3] = vectorA[3] - vectorB[3];
		
		return output;
	}
	
	public static float vectorLength(final float[] vector)
	{
		return (float)Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]); 
	}
	
	public static float interpolate(final float minVal, final float maxVal, final float pos)
	{
		if(pos >= maxVal)
			return maxVal;
		
		if(pos <= minVal)
			return minVal;
		
		float length = maxVal - minVal; 
		float output = (pos - minVal) / length;
		
		return minVal + (length * output);
	}
	
//	public static float interpolate(final float[] minVal, final float[] maxVal, final float pos)
//	{
//		if(pos >= maxVal)
//			return maxVal;
//		
//		if(pos <= minVal)
//			return minVal;
//		
//		float length = maxVal - minVal; 
//		float output = (pos - minVal) / length;
//		
//		return minVal + (length * output);
//	}
	
	public static boolean isPowerOfTwo(final int value)
	{
		boolean output = false;
		
		if(Integer.bitCount(value) == 1 && value != 0)
			output = true;
		
		return output;
	}
	
	public static int nextPOT(final int x)
	{
		int output = x;
		
		output = output - 1;
		output = output | (output >> 1);
		output = output | (output >> 2);
		output = output | (output >> 4);
		output = output | (output >> 8);
		output = output | (output >>16);
	    return output + 1;
	}
	
	public static Bitmap padImage(final Bitmap source, final int xPadding, final int yPadding)
	{
		Bitmap output = Bitmap.createBitmap(source.getWidth() + xPadding, source.getHeight() + yPadding, source.getConfig());//Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawBitmap(source, 0, 0, null);
		
		return output;
	}


	public static float[] normalizeVector(float[] vector)
	{
		float[] output = new float[3];
		float length = Matrix.length(vector[0], vector[1], vector[2]);
		
		output[0] = vector[0] / length;
		output[1] = vector[1] / length;
		output[2] = vector[2] / length;
		
		return output;
	}


	public static float[] multiplyVector(float[] vector, float multiplier)
	{
		float[] output = new float[3];
		
		output[0] = vector[0] * multiplier;
		output[1] = vector[1] * multiplier;
		output[2] = vector[2] * multiplier;
		
		return output;
	}


	public static float degToRad(float value)
	{
		return value * (float)(Math.PI / 180.0);
	}
	
	public static float[] perspectiveMatrix(float fov, float aspect, float nearClip, float farClip)
	{
		float[] perspectiveM = new float[16];
		float yScale = 1.0f / (float)Math.tan(Utilities.degToRad(fov) / 2.0f);
		float xScale = yScale / aspect;
		float farNear = nearClip - farClip;
		
		Matrix.setIdentityM(perspectiveM, 0);
		
		perspectiveM[0] = xScale;
		perspectiveM[5] = yScale;
		perspectiveM[10] = (farClip + nearClip) / farNear;
		perspectiveM[11] = -1.0f;
		perspectiveM[14] = 2.0f * farClip * nearClip / farNear;
		perspectiveM[15] = 1.0f;
		
		return perspectiveM;
	}
}
