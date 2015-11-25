package com.fusedresistance.polyviewer.pro.renderer;

import com.fusedresistance.polyviewer.scene.ObjModel;

import android.opengl.GLES20;

public class RenderContainer
{
	public ObjModel[] modelArray = new ObjModel[1];
	public float[] viewMatrix = new float[16];
	public float[] projMatrix = new float[16]; 
	public float[] lightDiffuse = new float[9];
	public float[] lightDir = new float[9];
	public float[] lightSpec = new float[9];
	public float[] cameraDir = new float[3];
	public int drawMode = GLES20.GL_TRIANGLES;
}
