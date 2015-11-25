package com.fusedresistance.polyviewer.scene;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.fusedresistance.polyviewer.utility.BoundingBox;
import com.fusedresistance.polyviewer.utility.Utilities;

public class ObjModel implements SceneObject, Serializable
{
	private static final long serialVersionUID = 1L;
	private static final int FLOAT_BYTES = 4;
	private static final int VERTEX_STRIDE = 8 * FLOAT_BYTES;
	private static final int VERTICES_POS_OFFSET = 0;
	private static final int VERTICES_NORMAL_OFFSET = 3;// * 4;
	private static final int VERTICES_UV_OFFSET = 6;// * 4;
	
	private transient FloatBuffer vertexBuffer = null;
	private transient ShortBuffer indexBuffer = null;
	private int triCount = 0;
	private int indexCount = 0;
	private int[] textureArray = new int[Scene.NUM_TEXTURES];
	
	public float[] translationMatrix = new float[16];
	public float[] scaleMatrix = new float[16];
	public float[] rotationMatrix = new float[16];
	public float[] modelMatrix = new float[16];
	
	private boolean enabled = true;
	private boolean hasEmissive = false;
//	private boolean hasReflection = false;
	private float[] target = new float[3];
	private float[] position = new float[3];
	private float[] direction = new float[3];
	
	private float midPoint;
	private float height;
	private String meshName;
	private BoundingBox boundingBox = null;
	
	public ObjModel()
	{
		this.reset();
	}
	
	public void deleteTextures()
	{
		GLES20.glDeleteTextures(textureArray.length, textureArray, 0);
	}
	
	//@Override
	public void translate(float xAmount, float yAmount, float zAmount)
	{
		float[] translation = new float[] { xAmount, yAmount, zAmount };
		
		position = Utilities.addVectors(position, translation);
		target = Utilities.addVectors(target, translation);
		
		Matrix.setIdentityM(translationMatrix, 0);
		Matrix.translateM(translationMatrix, 0, position[0], position[1], position[2]);
		
		this.updateMatrix();
	}

	//@Override
	public void rotate(float xAmount, float yAmount, float zAmount)
	{
		float xRotation[] = new float[16];
		float yRotation[] = new float[16];
		float zRotation[] = new float[16];
		
		Matrix.setIdentityM(xRotation, 0);
		Matrix.setIdentityM(yRotation, 0);
		Matrix.setIdentityM(zRotation, 0);
		
		Matrix.rotateM(xRotation, 0, xAmount, 1, 0, 0);
		Matrix.rotateM(yRotation, 0, yAmount, 0, 1, 0);
		Matrix.rotateM(zRotation, 0, zAmount, 0, 0, 1);
		
		Matrix.multiplyMM(rotationMatrix, 0, xRotation, 0, yRotation, 0);
		Matrix.multiplyMM(rotationMatrix, 0, zRotation, 0, rotationMatrix, 0);
		
		this.updateMatrix();
	}

	//@Override
	public void scale(float xAmount, float yAmount, float zAmount)
	{
		Matrix.setIdentityM(scaleMatrix, 0);
		Matrix.scaleM(scaleMatrix, 0, xAmount, yAmount, zAmount);
		
		this.updateMatrix();
	}

	//@Override
	public void orbit(float horizAmount, float vertAmount)
	{
		// Not used by this class
	}

	//@Override
	public void updateMatrix()
	{
		Matrix.multiplyMM(modelMatrix, 0, scaleMatrix, 0, rotationMatrix, 0);
		Matrix.multiplyMM(modelMatrix, 0, translationMatrix, 0, modelMatrix, 0);
	}

	//@Override
	public void reset()
	{
		position = new float[] { 0.0f, 0.0f, 0.0f };
		target = new float[] { 0.0f, 0.0f, 1.0f };
		direction = Utilities.subtractVectors(target, position);
		
		Matrix.setIdentityM(rotationMatrix, 0);
		Matrix.setIdentityM(scaleMatrix, 0);
		Matrix.setIdentityM(translationMatrix, 0);
		
		this.updateMatrix();
	}

	//@Override
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	//@Override
	public void setTarget(float xValue, float yValue, float zValue)
	{
		target = new float[] { xValue, yValue, zValue };
		direction = Utilities.subtractVectors(target, position);
	}

	//@Override
	public void setPosition(float xValue, float yValue, float zValue)
	{
		position = new float[] { xValue, yValue, zValue };
		direction = Utilities.subtractVectors(target, position);
		
		Matrix.setIdentityM(translationMatrix, 0);
		Matrix.translateM(translationMatrix, 0, position[0], position[1], position[2]);
		
		this.updateMatrix();
	}
	
	public void setName(String name)
	{
		meshName = name;
	}
	
	public void setHeight(final float value)
	{
		height = value;
	}
	
	public void setMidPoint(final float value)
	{
		midPoint = value;
	}
	
	public void setTexture(int[] value)
	{
		textureArray = value;
	}
	
	public void setIndexBuffer(final ArrayList<Short> newIB)
	{
		if(indexBuffer != null)
			indexBuffer.clear();
		
		newIB.trimToSize();
		
		ByteBuffer byteBuff = ByteBuffer.allocateDirect(newIB.size() * 2);
		byteBuff.order(ByteOrder.nativeOrder());
		
		indexBuffer = byteBuff.asShortBuffer();
		indexCount = newIB.size();
		
		for(int i = 0; i < newIB.size(); i++)
			indexBuffer.put(newIB.get(i));
		
		indexBuffer.rewind();
	}
	
	public void setIndexBuffer(final short[] input)
	{
		if(indexBuffer != null)
			indexBuffer.clear();
		
		ByteBuffer byteBuff = ByteBuffer.allocateDirect(input.length * 2);
		byteBuff.order(ByteOrder.nativeOrder());
		
		indexBuffer = byteBuff.asShortBuffer();
		indexBuffer.put(input);
		indexBuffer.rewind();
	}

	public void setVertexBuffer(final ArrayList<Float> newVB)
	{
		if(vertexBuffer != null)
			vertexBuffer.clear();
		
		newVB.trimToSize();
		
		ByteBuffer byteBuff = ByteBuffer.allocateDirect(newVB.size() * 4);
		byteBuff.order(ByteOrder.nativeOrder());
		
		vertexBuffer = byteBuff.asFloatBuffer();
				
		triCount = newVB.size() / 3;
		
		for(int i = 0; i < newVB.size(); i++)
			vertexBuffer.put(newVB.get(i));
		
		vertexBuffer.rewind();
	}
	
	public void setVertexBuffer(final float[] input)
	{
		if(vertexBuffer != null)
			vertexBuffer.clear();
		
		ByteBuffer byteBuff = ByteBuffer.allocateDirect(input.length * 4);
		byteBuff.order(ByteOrder.nativeOrder());
		
		triCount = input.length / 3;
		
		vertexBuffer = byteBuff.asFloatBuffer();
		vertexBuffer.put(input);
		vertexBuffer.rewind();
	}
	
	public void setEmissive(boolean emissive)
	{
		hasEmissive = emissive;
	}
	
//	public void setReflection(boolean reflection)
//	{
//		hasReflection = reflection;
//	}
	
	//@Override
	public boolean isEnabled()
	{
		return enabled;
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
		return modelMatrix;
	}
	
	public String getName()
	{
		return meshName;
	}
	
	public int[] getTexture()
	{
		return textureArray;
	}
	
	public int getTriCount()
	{
		return triCount;
	}
	
	public int getIndexCount()
	{
		return indexCount;
	}
	
	public float getHeight()
	{
		return height;
	}
	
	public float getMidPoint()
	{
		return midPoint;
	}
	
	public ShortBuffer getIndexBuffer()
	{
		return indexBuffer;
	}
	
	public FloatBuffer getVertexBuffer()
	{
		return vertexBuffer;
	}
	
	public boolean hasEmissive()
	{
		return hasEmissive;
	}
	
	public boolean hasReflection()
	{
		return hasReflection();
	}
	
	public void render(final int[] attributeHandles, final int[] textureHandles, final int displayMode)
	{
		for(int i = 0; i < textureArray.length; i++)
		{
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureArray[i]);
			GLES20.glUniform1i(textureHandles[i], i);
		}
		
		// Setup the position details.
		this.vertexBuffer.position(VERTICES_POS_OFFSET);
		GLES20.glVertexAttribPointer(attributeHandles[0], 3, GLES20.GL_FLOAT, false, VERTEX_STRIDE, this.vertexBuffer);
		GLES20.glEnableVertexAttribArray(attributeHandles[0]);
		
		// Setup the normal details.
		this.vertexBuffer.position(VERTICES_NORMAL_OFFSET);
		GLES20.glVertexAttribPointer(attributeHandles[1], 3, GLES20.GL_FLOAT, false, VERTEX_STRIDE, this.vertexBuffer);
		GLES20.glEnableVertexAttribArray(attributeHandles[1]);
		
		// Setup the texture details.
		this.vertexBuffer.position(VERTICES_UV_OFFSET);
		GLES20.glVertexAttribPointer(attributeHandles[2], 2, GLES20.GL_FLOAT, false, VERTEX_STRIDE, this.vertexBuffer);
		GLES20.glEnableVertexAttribArray(attributeHandles[2]);

		// Draw the model.
		if(displayMode == GLES20.GL_TRIANGLES)
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, this.indexCount, GLES20.GL_UNSIGNED_SHORT, this.indexBuffer);
		else
		{
			for(int i = 0; i < indexCount; i += 3)
				GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, i, 3);
		}
	}

	public void setRotationMatrix(float[] matrix)
	{
		rotationMatrix = matrix;
		
		this.updateMatrix();
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		
		float[] vertBuff = new float[vertexBuffer.capacity()];//vertexBuffer.array();
		short[] indexBuff = new short[indexBuffer.capacity()];//indexBuffer.array();
		
		out.writeInt(vertBuff.length);
		out.writeInt(indexBuff.length);
		
		vertexBuffer.get(vertBuff);
		indexBuffer.get(indexBuff);
		
		for(int i = 0; i < vertBuff.length; ++i)
			out.writeFloat(vertBuff[i]);
		
		for(int i = 0; i < indexBuff.length; ++i)
			out.writeShort(indexBuff[i]);
		
		vertexBuffer.rewind();
		indexBuffer.rewind();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException 
	{
		in.defaultReadObject();
		
		int vertLength = in.readInt();
		int indexLength = in.readInt();

		vertexBuffer = ByteBuffer.allocateDirect(vertLength * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		indexBuffer = ByteBuffer.allocateDirect(indexLength * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
		
		for(int i = 0; i < vertLength; ++i)
			vertexBuffer.put(in.readFloat());
		
		for(int i = 0; i < indexLength; ++i)
			indexBuffer.put(in.readShort());
		
		vertexBuffer.rewind();
		indexBuffer.rewind();
	}

	public void release()
	{
		GLES20.glDeleteTextures(textureArray.length, textureArray, 0);
	}
	
	public boolean setBoundingBox(BoundingBox box)
	{
		if(box == null)
			return false;
		
		boundingBox = box;
		
		return true;
	}

	public BoundingBox getBoundingBox()
	{
		return boundingBox;
	}
}
