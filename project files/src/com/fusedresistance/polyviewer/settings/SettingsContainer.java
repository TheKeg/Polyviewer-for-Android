package com.fusedresistance.polyviewer.settings;

import com.fusedresistance.polyviewer.utility.BoundingBox;

public class SettingsContainer 
{
	// Data Variables
	public String meshFolder = "";
	public String galleryFolder = "";
   public String wallpaperFolder = "";
	public String shaderName = "NormalDiffuseSpecular";
	
	// Visual Variables
	public boolean useAlpha = false;
	public boolean useBackground = false;
	public boolean isWallpaper = false;
	public boolean displayFPS = false;
	public float brightness = 1.0f;
	public float emissiveStr = 1.0f;
	public float[] backgroundColour = new float[4];
	public int frameLimit = 0;
	public String backgroundImage = "";
	public BoundingBox sceneBounds = null;
	public int shaderFlags = 0;
	
	// Movement Variables
	public boolean useMotion = false;
	public boolean useRotation = false;
	public boolean rotateCamera = false;
	public float rotationDirection = 1.0f;
	public int rotationSpeed = 20;
	public int maxAngle = 45;
	
	// Light & Camera Variables
	public boolean useLightAssist = false;
	public boolean useCameraPan = false;
	public boolean useCameraTarget = false;
	public boolean useCameraLight = false;
	public float[] cameraPreset = new float[] { 0.0f, 0.0f }; 
	public float fieldOfView = 90.0f;
	public int lightIndex = 0;
}
