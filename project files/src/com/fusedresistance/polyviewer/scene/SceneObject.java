package com.fusedresistance.polyviewer.scene;

public abstract interface SceneObject
{
	void translate(float xAmount, float yAmount, float zAmount);
	
	void rotate(float xAmount, float yAmount, float zAmount);
	
	void scale(float xAmount, float yAmount, float zAmount);
	
	void orbit(float horizAmount, float vertAmount);
	
	void updateMatrix();
	
	void reset();
	
	void setEnabled(boolean enabled);
	
	void setTarget(float xValue, float yValue, float zValue);
	
	void setPosition(float xValue, float yValue, float zValue);
	
	boolean isEnabled();
		
	float[] getPosition();
	
	float[] getTarget();
	
	float[] getDirection();

	float[] getMatrix();
}
