package com.fusedresistance.polyviewer.utility;

import java.util.Hashtable;

public class Preset
{
	private Hashtable<String, float[]> presets = new Hashtable<String, float[]>();
	
	public boolean addPreset(String presetName, float[] presetValue)
	{
		if(!presets.containsKey(presetName))
		{
			presets.put(presetName, presetValue);
			return true;
		}
		
		return false;			
	}
	
	public float[] getPreset(String presetName)
	{
		if(presets.containsKey(presetName))
		{
			return presets.get(presetName);
		}
		
		return null;
	}
}
