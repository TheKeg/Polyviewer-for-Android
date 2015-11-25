package com.fusedresistance.polyviewer.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeMap;

import com.fusedresistance.polyviewer.scene.ObjModel;
import com.fusedresistance.polyviewer.scene.Scene;
import com.fusedresistance.polyviewer.utility.OBJLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Environment;
import android.util.Log;

class SortIgnoreCase implements Comparator<String>
{
	public int compare(String s1, String s2)
	{
		return s1.toLowerCase().compareTo(s2.toLowerCase());
	}
}

/**
 * @author Craig Young
 * 
 * This class handles the loading of obj 3d mesh files, the loading of shader files
 * and the loading of image files. All of the available functions used are static in
 * nature so an instance of the FileLoaded class is unnecessary for every day usage.
 */
public class FileLoader
{
	/**
	 * Returns a string containing the shader code specified by the passed in resource
	 * ID. The context argument pertains to the current activity that contains the 
	 * intended shader resource to be loaded. The resource ID must point to a valid
	 * resource or else the method will return an empty string.
	 *   
	 * @param context		the context from the currently active Activity.
	 * @param resourceID	the resource ID associated with the intended file to load.
	 * @return				a string containing the shader code or an empty string if it fails.
	 */
	public static String readShader(Context context, int resourceID)
	{
		InputStream inputStream = context.getResources().openRawResource(resourceID);
		InputStreamReader iStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(iStreamReader);
		String inputLine;
		StringBuilder strBuilder = new StringBuilder();
		
		try
		{
			// Loop through the shader text file and append each line to the string builder class
			while((inputLine = bufferedReader.readLine()) != null)
			{
				strBuilder.append(inputLine);
				strBuilder.append("\n");
			}
			
			bufferedReader.close();
			iStreamReader.close();
			inputStream.close();
		}
		catch(Exception e)
		{
			Log.e("READ SHADER", e.toString());

			return "";
		}
		
		return strBuilder.toString();
	}
	
	/**
	 * Creates a list of available model folders stored in mnt/sdcard/polyviewer, mnt/sdcard/data/polviewer, or
	 * mnt/sdcard/external_sd/polyviewer.
	 * @return Array of strings containing the names of the available models.
	 */
	public static String[] availableModels()
    {
    	String[] output = new String[0];
    	  	
    	boolean mExternalStorageAvailable = false;
		String state = Environment.getExternalStorageState();
		
		// Check if the external media is readable
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) 
		{
		    // We can read and write the media
		    mExternalStorageAvailable = true;
		}
		
		// Search for the available models if the external storage can be read from
		if(mExternalStorageAvailable)
		{
			File externalDir = Environment.getExternalStorageDirectory();//new File("mnt/sdcard");//Environment.getExternalStorageDirectory();
			String[] localModels;
			ArrayList<String> models = new ArrayList<String>();
			
			if(!externalDir.isDirectory())
				return new String[] { "External Directory returned is not a directory at all." };
				
			File[] polyviewTest = FileLoader.findDirectory(externalDir, "polyviewer");
			
			if(polyviewTest == null)
				return new String[] { "Unable to find polyviewer directory." };
			
			for(File fileDir : polyviewTest)
			{
				localModels = FileLoader.listDirectories(fileDir);
				
				if(localModels == null)
					continue;

            models.addAll(Arrays.asList(localModels));
//				for(String path : localModels)
//					models.add(path);
			}
			
			output = new String[models.size()];
			
			models.toArray(output);
			// Sort the array of names.
			if(output.length > 0)
				Arrays.sort(output);
			else
				output = new String[] { "No model subfolders were found." };
		}
		
		return output;
    }

//	public static String[] readModel(final Context context, final String fileName)
//	{
//		boolean mExternalStorageAvailable = false;
//		String state = Environment.getExternalStorageState();
//		String[] fileList = null;
//
//		// Check if the external media is readable
//		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
//		{
//		    // We can read and write the media
//		    mExternalStorageAvailable = true;
//		}
//
//		// Read in the
//		if(mExternalStorageAvailable)
//		{
//			File externalDir = Environment.getExternalStorageDirectory();
//			File[] polyviewFolders = FileLoader.findDirectory(externalDir, "polyviewer");
//			File meshFolder = null;
//
//			if(polyviewFolders != null)
//			{
//				for(int i = 0; i < polyviewFolders.length; ++i)
//				{
//					meshFolder = FileLoader.getDirectory(polyviewFolders[i], fileName);
//
//					if(meshFolder != null)
//						break;
//				}
//
//				if(meshFolder != null)
//					fileList = FileLoader.readFolder(meshFolder);
//			}
//		}
//
//		return fileList;
//	}
	
	public static String[] parseFolder(final String pathName)
	{
		boolean mExternalStorageAvailable = false;
		String state = Environment.getExternalStorageState();
		String[] fileList = null;
		
		// Check if the external media is readable
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) 
		{
		    // We can read and write the media
		    mExternalStorageAvailable = true;
		}
		
		// Read in the
		if(mExternalStorageAvailable)
		{
			File folder = new File(pathName);
			
			if(folder.exists() && folder.isDirectory())
			{
				fileList = FileLoader.readFolder(folder);
			}
		}
		
		return fileList;
	}
	
	private static String[] readFolder(File inputFolder)
	{
		String[] sortOrder = new String[] { "obj", "diffuse", "normal", "specular", "emissive", "reflection" };
		String[] sortAlts = new String[] { "obj", ".D", ".N", ".S", ".E", ".R" };
		File[] filteredFiles = inputFolder.listFiles(getFilter("pvb"));
		
		if(filteredFiles.length == 0)
			filteredFiles = inputFolder.listFiles(getFilter(sortOrder[0]));
		else
			sortOrder[0] = "pvb";
		
		String[] objNames = new String[filteredFiles.length];

		for(int i = 0; i < filteredFiles.length; ++i)
		{
			if(filteredFiles.length >= 1)
			{
				objNames[i] = filteredFiles[i].getName();
				objNames[i] = objNames[i].substring(0, objNames[i].lastIndexOf("."));
			}
			else
				objNames[i] = "";
		}
		
		String[] output = new String[objNames.length * Scene.MODEL_SIZE];
		
		for(int i = 0; i < objNames.length; ++i)
		{
			for(int j = 0; j < sortOrder.length; ++j)
			{
				if(j >= sortOrder.length)
					break;
				
				filteredFiles = inputFolder.listFiles(getFilter(sortOrder[j]));
				
				if(filteredFiles.length == 0)
					filteredFiles = inputFolder.listFiles(getFilter(sortAlts[j])); 
				
				if(filteredFiles != null)
				{
					output[(i * Scene.MODEL_SIZE) + j] = "";
					
					for(File filteredFile : filteredFiles)//(int k = 0; k < filteredFiles.length; ++k)
					{
						if(filteredFile.getName().contains(objNames[i]))
						{
							output[(i * Scene.MODEL_SIZE) + j] = filteredFile.getAbsolutePath();
							break;
						}
					}
				}
			}
		}
		
		return output;
	}
	
	public static FilenameFilter getFilter(final String filterString)
    {
    	return new FilenameFilter()
         {
            //@Override
            public boolean accept(File dir, String name)
            {
               return name.contains(filterString);
            }
         };
    }
	
	/**
	 * Returns a loaded 3D obj model. The context argument pertains to the current 
	 * activity that contains the intended model resource to be loaded. The 
	 * resource ID must point to a valid resource or else the method will return null. 
	 * 
	 * @param context		the context from the currently active Activity.
	 * @param resourceID	the resource ID associated with the intended file to load.
	 * @return				an OGLModel file that contains the loaded models information.
	 */
 	public synchronized static ObjModel loadObj(final Context context, final int resourceID, final String fileData)
	{	
		ObjModel outputModel = null;

      if(context == null)
         return null;

		if(fileData.length() == 0)
      	outputModel = OBJLoader.loadModel(context, resourceID, "");//context.getResources().openRawResource(resourceID);
      else if(fileData.endsWith(".pvb"))
			return loadSerialized(fileData);
		else if(fileData.endsWith(".obj"))
         outputModel = OBJLoader.loadModel(context, resourceID, fileData);
      else if(fileData.endsWith(".3ds"))
         outputModel = ThreeDSLoader.loadModel(context, resourceID, fileData);
      else if(fileData.endsWith(".fbx"))
         outputModel = FBXLoader.loadModel(context, resourceID, fileData);
      else if(fileData.endsWith(".md3"))
         outputModel = Q3Loader.loadModel(context, resourceID, fileData);
      else if(fileData.endsWith(".smd"))
         outputModel = SMDLoader.loadModel(context, resourceID, fileData);

      if(fileData.length() > 0 && outputModel != null)
         saveSerialized(outputModel, fileData);

      return outputModel;
	}
	
 	private static ObjModel loadSerialized(final String filePath)
 	{
 		ObjModel output = null;
 		
 		FileInputStream fis;
 		ObjectInputStream ois;
 		
 		boolean mExternalStorageAvailable = false;
// 		boolean mExternalStorageWritable = false;
		String state = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) 
		{
		    // We can read and write the media
		    mExternalStorageAvailable = true;
		} 
 		
		if(!mExternalStorageAvailable)
			return output;
		
 		try
 		{
 			fis = new FileInputStream(filePath);
 			ois = new ObjectInputStream(fis);
 			
 			output = (ObjModel)ois.readObject();
 			ois.close();
 		}
 		catch(Exception e)
 		{
 			Log.e("LOAD SERIALIZED", e.toString());//getMessage());//e.toString());
 		}
		
 		return output;
 	}
	
 	private static boolean saveSerialized(final ObjModel input, final String filePath)
 	{
 		FileOutputStream fos;
 		ObjectOutputStream oos;
 		
// 		boolean mExternalStorageAvailable = false;
 		boolean mExternalStorageWritable = false;
		String state = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED.equals(state)) 
		{
		    // We can read and write the media
		    mExternalStorageWritable = true;
		}
 		
		if(!mExternalStorageWritable)
			return false;
 		
 		try
 		{
 			String outPath = filePath.substring(0, filePath.lastIndexOf("."))  + ".pvb";
 			fos = new FileOutputStream(outPath);
 			oos = new ObjectOutputStream(fos);
 			
 			oos.writeObject(input);
 			oos.close();
 		}
 		catch(Exception e)
 		{
 			Log.e("SAVE SERIALIZED", e.getLocalizedMessage());
 			return false;
 		}
 		
 		return true;
 	}
 	
	/**
	 * A private method that reads in from a buffered reader and attempts to populate an
	 * array list with the available vertices with each 3 array values in a sequence 
	 * representing the x, y, and z values for the vertex.
	 * 
	 * @param bufferedReader	a buffered reader of the currently opened file.
	 * @param vertexBuffer		the arraylist to populate with the vertex values.
	 */
	private static float[] readVertices(final BufferedReader bufferedReader, String input, ArrayList<Float> vertexBuffer)
	{
		String inputLine = input;
		String[] splitString;
		float minVal = 0.0f;
		float maxVal = 0.0f;
		float[] xMinMax = new float[] { 0.0f, 0,0f };
		float[] yMinMax = new float[] { 0.0f, 0,0f };
		float[] zMinMax = new float[] { 0.0f, 0,0f };
		float[] output = new float[2];
		
		try
		{
			if(input.contains("vertices"))
				inputLine = bufferedReader.readLine();
			
			while(inputLine.startsWith("v"))
			{
				splitString = inputLine.split("\\s+");
				
				if(splitString.length >= 4)
				{
					Float xVal = Float.parseFloat(splitString[1]);
					Float yVal = Float.parseFloat(splitString[2]);
					Float zVal = Float.parseFloat(splitString[3]);
					
					vertexBuffer.add(xVal);
					vertexBuffer.add(yVal);
					vertexBuffer.add(zVal);
					
					xMinMax[0] = Math.min(xMinMax[0], xVal);
					xMinMax[1] = Math.max(xMinMax[1], xVal);
					
					yMinMax[0] = Math.min(yMinMax[0], yVal);
					yMinMax[1] = Math.max(yMinMax[1], yVal);
					
					zMinMax[0] = Math.min(zMinMax[0], zVal);
					zMinMax[1] = Math.max(zMinMax[1], zVal);
					
					if(yVal < minVal)
						minVal = yVal;
					
					if(yVal > maxVal)
						maxVal = yVal;
				}
				
				inputLine = bufferedReader.readLine();
			}
			
			float xDistance = xMinMax[1] - xMinMax[0];
			float yDistance = yMinMax[1] - yMinMax[0];
			float zDistance = zMinMax[1] - zMinMax[0];
			
			float height = (maxVal - minVal) / 2.0f + minVal;
			float distance = Math.max(xDistance, Math.max(yDistance, zDistance));
			
			output = new float[] { height, distance, xMinMax[0], xMinMax[1], yMinMax[0], yMinMax[1], zMinMax[0], zMinMax[1] };
		}
		catch(IOException e)
		{
			return output;
		}
		
		return output;
	}
	
	/**
	 * A private method that reads in from a buffered reader and attempts to populate an
	 * array list with the available normals with each 3 array values in a sequence 
	 * representing the x, y, and z values for the normal.
	 * @param bufferedReader	a buffered reader of the currently opened file.
	 * @param normalBuffer		the arraylist to populate with the normal values.
	 */
	private static void readNormals(final BufferedReader bufferedReader, String input, ArrayList<Float> normalBuffer)
	{
		String inputLine = input;
		String[] splitString;

		try
		{
			if(input.contains("normals"))
				inputLine = bufferedReader.readLine();
			
			while(inputLine.startsWith("vn"))
			{
				splitString = inputLine.split("\\s+");
				
				if(splitString.length >= 4)
				{
					normalBuffer.add(Float.parseFloat(splitString[1]));
					normalBuffer.add(Float.parseFloat(splitString[2]));
					normalBuffer.add(Float.parseFloat(splitString[3]));
				}
				
				inputLine = bufferedReader.readLine();
			}
		}
		catch(IOException e)
		{
			return;
		}
	}
	
	/**
	 * A private method that reads in from a buffered reader and attempts to populate an
	 * array list with the available texture coordinates with each 2 array values in a 
	 * sequence representing the x and y values for the texture coordinate.
	 * @param bufferedReader	a buffered reader of the currently opened file.
	 * @param uvBuffer			the arraylist to populate with the texture coordinate values.
	 */
	private static void readUVs(final BufferedReader bufferedReader, String input, ArrayList<Float> uvBuffer)
	{
		String inputLine = input;
		String[] splitString;
		
		try
		{
			if(input.contains("texture"))
				inputLine = bufferedReader.readLine();
			
			while(inputLine.startsWith("vt"))
			{
				splitString = inputLine.split("\\s+");
				
				if(splitString.length >= 4)
				{
					uvBuffer.add(Float.parseFloat(splitString[1]));
					uvBuffer.add(Float.parseFloat(splitString[2]));
				}
				
				inputLine = bufferedReader.readLine();
			}
		}
		catch(IOException e)
		{
			return;
		}
	}
	
	/**
	 * A private method that takes in a buffered reader of the obj file and array lists for
	 * the vertex, normal and texture coordinate buffers. The method reads in the face data
	 * and obtains the values from each buffer and places them into a new array where the values
	 * are stored for each vertex where every 8 array indices apply to each vertex. The ordering
	 * of the values are the x, y, z values of the vertex followed by the x, y, and z values of 
	 * the normal followed by the x and y values of the texture coordinate.
	 * 
	 * @param bufferedReader	a buffered reader of the currently opened file.
	 * @param vertexBuffer		the arraylist to populate with the vertex values.
	 * @param normalBuffer		the arraylist to populate with the normal values.
	 * @param uvBuffer			the arraylist to populate with the texture coordinate values.
	 * @return					returns an OGLModel that is empty if the method fails.							
	 */
	private static ObjModel buildObjBuffer(final BufferedReader bufferedReader, final String input, final ArrayList<Float> vertexBuffer, 
										   final ArrayList<Float> normalBuffer, final ArrayList<Float> uvBuffer)
	{
		ObjModel outputModel = new ObjModel();
		String inputLine = input;
		String[] splitString;
		String[] vertexIndices;
		int vertexIndex = 0;
		int normalIndex = 0;
		int uvIndex = 0;
		short index = 0;
		
		ArrayList<Float> vertexList = new ArrayList<Float>();
		ArrayList<Short> indexList = new ArrayList<Short>();
		
		vertexBuffer.trimToSize();
		normalBuffer.trimToSize();
		uvBuffer.trimToSize();
				
		try
		{
			if(input.contains("faces"))
				inputLine = bufferedReader.readLine();
			
			while(inputLine.startsWith("f"))
			{
				splitString = inputLine.split("\\s+");
				
				if(splitString.length < 4)
					continue;
				
				for(int i = 1; i < 4; i++)
				{
					vertexIndices = splitString[i].split("/");
					
					vertexIndex = Integer.parseInt(vertexIndices[0]);// - 1;
					uvIndex = Integer.parseInt(vertexIndices[1]);// - 1;
					normalIndex = Integer.parseInt(vertexIndices[2]);// - 1;
					
					if(vertexIndex < 0)
					{
						vertexIndex = vertexBuffer.size() / 3 + vertexIndex;
						uvIndex = uvBuffer.size() / 2 + uvIndex;
						normalIndex = normalBuffer.size() / 3 + normalIndex;
					}
					else
					{
						vertexIndex--;
						uvIndex--;
						normalIndex--;
					}	
					
					try
					{
						vertexList.add((Float)vertexBuffer.get(vertexIndex * 3));
						vertexList.add((Float)vertexBuffer.get(vertexIndex * 3 + 1));
						vertexList.add((Float)vertexBuffer.get(vertexIndex * 3 + 2));
						
						vertexList.add((Float)normalBuffer.get(normalIndex * 3));
						vertexList.add((Float)normalBuffer.get(normalIndex * 3 + 1));
						vertexList.add((Float)normalBuffer.get(normalIndex * 3 + 2));
						
						vertexList.add((Float)uvBuffer.get(uvIndex * 2));
						vertexList.add((Float)1.0f - uvBuffer.get(uvIndex * 2 + 1));
						
						indexList.add(index++);
					}
					catch(Exception e)
					{
						break;
					}
				}
				
				if(splitString.length >= 5)
				{
					int[] indices = new int[] { 1, 3, 4 };
					
					for(int i = 0; i < indices.length; i++)
					{
						vertexIndices = splitString[indices[i]].split("/");
						
						vertexIndex = Integer.parseInt(vertexIndices[0]);// - 1;
						uvIndex = Integer.parseInt(vertexIndices[1]);// - 1;
						normalIndex = Integer.parseInt(vertexIndices[2]);// - 1;
						
						if(vertexIndex < 0)
						{
							vertexIndex = vertexBuffer.size() / 3 + vertexIndex;
							uvIndex = uvBuffer.size() / 2 + uvIndex;
							normalIndex = normalBuffer.size() / 3 + normalIndex;
						}
						else
						{
							vertexIndex--;
							uvIndex--;
							normalIndex--;
						}	
						
						try
						{
							vertexList.add((Float)vertexBuffer.get(vertexIndex * 3));
							vertexList.add((Float)vertexBuffer.get(vertexIndex * 3 + 1));
							vertexList.add((Float)vertexBuffer.get(vertexIndex * 3 + 2));
							
							vertexList.add((Float)normalBuffer.get(normalIndex * 3));
							vertexList.add((Float)normalBuffer.get(normalIndex * 3 + 1));
							vertexList.add((Float)normalBuffer.get(normalIndex * 3 + 2));
							
							vertexList.add((Float)uvBuffer.get(uvIndex * 2));
							vertexList.add((Float)1.0f - uvBuffer.get(uvIndex * 2 + 1));
							
							indexList.add(index++);
						}
						catch(Exception e)
						{
							break;
						}
					}
				}
				
				inputLine = bufferedReader.readLine();
				
				if(inputLine.startsWith("usemtl") || inputLine.startsWith("s"))
					inputLine = bufferedReader.readLine();
			}
			
			vertexList.trimToSize();
			
			outputModel.setIndexBuffer(indexList);
			outputModel.setVertexBuffer(vertexList);
			
			vertexList.clear();
		}
		catch(IOException e)
		{
			return null;
		}
		
		return outputModel;
	}
	
	/**
	 * Returns an OGLModel containing a fullscreen quad with the exterior coordinates
	 * spanning from -1 to 1 in screen space values. The method takes in a texAdjustment
	 * array of 2 float values that adjust the offset from the edges to compensate for
	 * screen and texture resolutions.
	 * 
	 * @param texAdjustment	array of 2 floats to adjust the texture coordinates.
	 * @return				OGLModel containing the fullscreen quad with screenspace coordinates.
	 */
	public synchronized static ObjModel createFullscreenPlane(final float[] texAdjustment)
	{
		ObjModel output = new ObjModel();
		
		ArrayList<Float> vertexList = new ArrayList<Float>();
		ArrayList<Short> indexList = new ArrayList<Short>();
		
		// Vertex 1 data.
		vertexList.add(-1.0f);
		vertexList.add(1.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(-1.0f);
		vertexList.add(0.0f + texAdjustment[0]);
		vertexList.add(0.0f + texAdjustment[1]);
		
		// Vertex 2 data.
		vertexList.add(1.0f);
		vertexList.add(1.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(-1.0f);
		vertexList.add(1.0f - texAdjustment[1]);
		vertexList.add(0.0f + texAdjustment[1]);
	
		// Vertex 3 data.
		vertexList.add(-1.0f);
		vertexList.add(-1.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(-1.0f);
		vertexList.add(0.0f + texAdjustment[1]);
		vertexList.add(1.0f - texAdjustment[1]);
		
		// Vertex 4 data.
		vertexList.add(1.0f);
		vertexList.add(-1.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(-1.0f);
		vertexList.add(1.0f - texAdjustment[1]);
		vertexList.add(1.0f - texAdjustment[1]);
		
		indexList.add((short) 0);
		indexList.add((short) 3);
		indexList.add((short) 1);
		
		indexList.add((short) 0);
		indexList.add((short) 2);
		indexList.add((short) 3);
		
		//vertexList = calculateTangents(vertexList, 8, 6, indexList);
		
		output.setVertexBuffer(vertexList);
		output.setIndexBuffer(indexList);
		
		return output;
	}

	public synchronized static void adjustFullscreenPlane(ObjModel input, final float[] texAdjustment, final float[] texValues)
	{
		if(input == null)
			return;
		
		ArrayList<Float> vertexList = new ArrayList<Float>();
		
		// Vertex 1 data.
		vertexList.add(-1.0f);
		vertexList.add(1.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(-1.0f);
		vertexList.add(0.0f + texAdjustment[0]);
		
		if(texAdjustment[1] > 0.0f)
			vertexList.add(texValues[1] - texAdjustment[1]);
		else
			vertexList.add(0.0f);
		
		// Vertex 2 data.
		vertexList.add(1.0f);
		vertexList.add(1.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(-1.0f);
		vertexList.add(texValues[0] - texAdjustment[0]);
		
		if(texAdjustment[1] > 0.0f)
			vertexList.add(texValues[1] - texAdjustment[1]);
		else
			vertexList.add(0.0f);
		
		// Vertex 3 data.
		vertexList.add(-1.0f);
		vertexList.add(-1.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(-1.0f);
		vertexList.add(0.0f + texAdjustment[0]);
		
		if(texAdjustment[1] > 0.0f)
			vertexList.add(0.0f + texAdjustment[1]);
		else
			vertexList.add(texValues[1]);
		
		// Vertex 4 data.
		vertexList.add(1.0f);
		vertexList.add(-1.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(0.0f);
		vertexList.add(-1.0f);
		vertexList.add(texValues[0] - texAdjustment[0]);
		
		if(texAdjustment[1] > 0.0f)
			vertexList.add(0.0f + texAdjustment[1]);
		else
			vertexList.add(texValues[1]);
		
		input.setVertexBuffer(vertexList);
	}

	public static float[] loadTexture(final Context context, final int resourceID, final String location)
	{
		int[] texIDs = new int[1];
		float[] output = new float[5];
		
		GLES20.glGenTextures(1, texIDs, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIDs[0]);
		
		output[0] = texIDs[0];
		output[1] = 1.0f;
		output[2] = 1.0f;
		
		Bitmap bitmap = null;
		
		try
		{
			if(location.length() != 0)
			{
				String path = location;
				
				if(path.startsWith("/data/data/"))
				{
					path = path.substring(path.lastIndexOf("/") + 1);
					FileInputStream imgStream = null;
					
					try
					{
						imgStream = context.openFileInput(path);
					} catch (FileNotFoundException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(imgStream != null)
						bitmap = BitmapFactory.decodeStream(imgStream);
				}
				else	
					bitmap = BitmapFactory.decodeFile(path);
			}
			
			if(bitmap == null)
			{
				InputStream is = context.getResources().openRawResource(resourceID);
				
				try
				{
					bitmap = BitmapFactory.decodeStream(is);
				} 
				finally
				{
					try
					{
						is.close();
					} 
					catch(IOException e)
					{
						Log.e("Texture Error", e.getLocalizedMessage());
					}
				}
			}
		}
		catch(Exception e)
		{
			Log.e("TEXTURE ERROR", e.getMessage());
		}
		
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
		
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		
		output[3] = width;
		output[4] = height;
		
		boolean widthP2 = Utilities.isPowerOfTwo(width);
		boolean heightP2 = Utilities.isPowerOfTwo(height);
		
		if(!widthP2 || !heightP2)
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			int newWidth = Utilities.nextPOT(width);
			int newHeight = Utilities.nextPOT(height);

			if(newWidth > 2048)
			{
				newWidth = 2048;
				width = 2048;
			}
			
			if(newHeight > 2048)
			{
				newHeight = 2048;
				height = 2048;
			}
			
			options.outWidth = newWidth;
			options.outHeight = newHeight;
			
			bitmap = Utilities.padImage(bitmap, newWidth - width, newHeight - height);
			
			output[1] = (float)width / (float)newWidth;
			output[2] = (float)height / (float)newHeight;
			output[3] = width;
			output[4] = height;
		}
		
//		GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		bitmap.recycle();
		
		return output;//texIDs;
	}
	
	public static int[] loadCubemap(final Context context, final int[] resourceID, final String[] imgLocations)
	{
		int numTextures = resourceID.length;
		
		if(imgLocations.length > 0)
			numTextures = imgLocations.length;
		
		int[] texIDs = new int[1];
		
		GLES20.glGenTextures(texIDs.length, texIDs, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, texIDs[0]);

		for(int i = 0; i < numTextures; i++)
		{
			Bitmap bitmap = null;
			
//			if(imgLocations[i].length() != 0)
//				bitmap = BitmapFactory.decodeFile(imgLocations[i]);
			
			if(imgLocations[i].length() != 0)
			{
				String path = imgLocations[i];
				
				if(path.startsWith("/data/data/"))
				{
					path = path.substring(path.lastIndexOf("/") + 1);
					FileInputStream imgStream = null;
					
					try
					{
						imgStream = context.openFileInput(path);
					} catch (FileNotFoundException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(imgStream != null)
						bitmap = BitmapFactory.decodeStream(imgStream);
				}
				else
					bitmap = BitmapFactory.decodeFile(imgLocations[i]);
			}
			
			if(bitmap == null)
			{
				InputStream is = context.getResources().openRawResource(resourceID[i]);
				
				try
				{
					bitmap = BitmapFactory.decodeStream(is);
				} 
				finally
				{
					try
					{
						is.close();
					} 
					catch(IOException e)
					{
						Log.e("Texture Error", e.getLocalizedMessage());
					}
				}
			}
			
//			GLUtils.texSubImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, 0, 0, bitmap);
			GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, bitmap, 0);
			bitmap.recycle();
		}
		
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		//GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);
		//GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, 0);

		return texIDs;
	}

	public static int[] loadTexture(final Context context, final int[] resourceID, final String[] imgLocations)
	{
		int numTextures = resourceID.length;
		
		if(imgLocations.length > 0)
			numTextures = imgLocations.length;
		
		int[] texIDs = new int[numTextures];
		
		GLES20.glGenTextures(texIDs.length, texIDs, 0);
		
		for(int i = 0; i < numTextures; i++)
		{
			if(resourceID[i] == -1)
				continue;
			
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIDs[i]);

			Bitmap bitmap = null;
			
			if(imgLocations[i].length() != 0)
			{
				String path = imgLocations[i];
				
				if(path.startsWith("/data/data/"))
				{
					path = path.substring(path.lastIndexOf("/") + 1);
					FileInputStream imgStream = null;
					
					try
					{
						imgStream = context.openFileInput(path);
					} catch (FileNotFoundException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(imgStream != null)
						bitmap = BitmapFactory.decodeStream(imgStream);
				}
				else
					bitmap = BitmapFactory.decodeFile(imgLocations[i]);
			}
			
			if(bitmap == null)
			{
				InputStream is = context.getResources().openRawResource(resourceID[i]);
				
				try
				{
					bitmap = BitmapFactory.decodeStream(is);
				} 
				finally
				{
					try
					{
						is.close();
					} 
					catch(IOException e)
					{
						Log.e("Texture Error", e.getLocalizedMessage());
					}
				}
			}
			
			if(bitmap == null)
				continue;
			
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

			if(Utilities.isPowerOfTwo(bitmap.getWidth()) && Utilities.isPowerOfTwo(bitmap.getHeight()))
			{
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
			}		
			else
			{
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
			}
			
//			GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
			bitmap.recycle();
		}
		
		return texIDs;
	}
	
	public static BoundingBox MaxBounds(BoundingBox boxA, BoundingBox boxB)
	{
		if(boxA == null && boxB == null)
			return null;
		else if(boxB == null)
			return boxA;
		else if(boxA == null)
			return boxB;
		
		float[] values = new float[6];
		float[] bounds1 = boxA.getBounds();
		float[] bounds2 = boxB.getBounds();
		
		values[0] = Math.min(bounds1[0], bounds2[0]);
		values[1] = Math.max(bounds1[1], bounds2[1]);
		values[2] = Math.min(bounds1[2], bounds2[2]);
		values[3] = Math.max(bounds1[3], bounds2[3]);
		values[4] = Math.min(bounds1[4], bounds2[4]);
		values[5] = Math.max(bounds1[5], bounds2[5]);
		
		return new BoundingBox(values);
	}
	
	public static void DeleteCachedMeshes(Context context)
	{
		boolean mExternalStorageAvailable;
 		boolean mExternalStorageWritable;
		String state = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED.equals(state)) 
		{
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWritable = true;
		} 
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) 
		{
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWritable = false;
		} 
		else 
		{
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWritable = false;
		}
 		
		if(!mExternalStorageAvailable || !mExternalStorageWritable)
			return;
		
		String[] fileList = FileLoader.availableModels();//context);
		
		if(fileList == null)
			return;

		String[] filePaths = new String[] { "mnt/sdcard", "mnt/extSdCard" };
		File externalDir;// = new File("mnt/sdcard");//Environment.getExternalStorageDirectory();
		File[] folders;// = FileLoader.findDirectory(externalDir, "polyviewer");
		File[] subDirs;

      for(int index = 0; index < filePaths.length; index++)
      {
         externalDir = new File(filePaths[index]);
         folders = FileLoader.findDirectory(externalDir, "polyviewer");

         if(folders == null)
            return;

         for(int i = 0; i < folders.length; ++i)
         {
            subDirs = folders[i].listFiles();

            if(subDirs == null)
               continue;

            for(int j = 0; j < subDirs.length; ++j)
            {
               if(!subDirs[j].isDirectory())
                  continue;

               File[] pvbFiles = subDirs[j].listFiles(FileLoader.getFilter(".pvb"));

               if(pvbFiles == null)
                  continue;

               for(int k = 0; k < pvbFiles.length; ++k)
                  pvbFiles[k].delete();
            }
         }
      }
	}
	
	private static File getDirectory(final File rootDir, final String dirName)
	{
		File directory = null;
		
		if(rootDir == null)
			return null;
			
		if(rootDir.isDirectory())
		{
			File[] listings = rootDir.listFiles(FileLoader.getFilter(dirName));
			
			if(listings == null)
				return null;
			
			for(int i = 0; i < listings.length; ++i)
			{
				if(listings[i].isDirectory())
					return listings[i];
			}
			
			if(directory == null)
			{
				listings = rootDir.listFiles();
				
				for(int i = 0; i < listings.length; ++i)
				{
					directory = FileLoader.getDirectory(listings[i], dirName);
					
					if(directory != null)
						return directory;
				}
			}
		}
		
		return directory;
	}
	
	public static File[] findDirectory(File directory, String dirName)
	{
		File[] dirs = null;
		ArrayList<File> folders = new ArrayList<File>();
		
		if(directory.isDirectory())
		{
			File[] listings = directory.listFiles(FileLoader.getFilter(dirName));

         if(listings != null)
         {
            for(File file : listings)//int i = 0; i < listings.length; ++i)
            {
               if(file.isDirectory())
                  folders.add(file);
            }
         }
			
			// Recursive section to search all sub folders of the supplied directory.
			listings = directory.listFiles();
			File dir;

         if(listings != null)
         {
            for(File file : listings)//int i = 0; i < listings.length; ++i)
            {
               dir = FileLoader.getDirectory(file, dirName);

               if(dir != null)
                  folders.add(dir);
            }
         }
			
			dirs = new File[folders.size()];
			
			folders.toArray(dirs);
		}
		
		return dirs; 
	}
	
	public static String[] listDirectories(final File directory)
	{
		String[] subDirs = null;
		ArrayList<String> dirArray = new ArrayList<String>();
		
		if(directory.isDirectory())
		{
			File[] files = directory.listFiles();
			
			for(int i = 0; i < files.length; ++i)
			{
				if(files[i].isDirectory())
					dirArray.add(files[i].getPath());//.getAbsolutePath());//.getName());
			}
			
			subDirs = new String[dirArray.size()];
			dirArray.toArray(subDirs);
		}
				
		return subDirs;
	}
	
	public static String[] parseString(final String[] paths)
	{
		String[] output = new String[paths.length];
		
		for(int i = 0; i < paths.length; ++i)
		{
			if(paths[i] == null)
				break;
			
			output[i] = paths[i].substring(paths[i].lastIndexOf("/") + 1);
		}
		
		return output;
	}
	
	public static String[] sortPaths(final String[] paths)
	{
		SortIgnoreCase sort = new SortIgnoreCase();
		
		String[] sortedPaths = new String[paths.length];
		String[] directoryNames = FileLoader.parseString(paths);
		TreeMap<String, String> pathMap = new TreeMap<String, String>(sort);
		
		for(int i = 0; i < paths.length; ++i)
		{
			if(paths[i] == null)
				break;
			
			pathMap.put(directoryNames[i], paths[i]);
		}
		
		pathMap.values().toArray(sortedPaths);
		
		return sortedPaths;
	}
	
	public static String[] retrieveDirectories(final String filter)
	{
		String[] directories = new String[0];
		
		boolean mExternalStorageAvailable = false;
		String state = Environment.getExternalStorageState();
		
		// Check if the external media is readable
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) 
		{
		    // We can read and write the media
		    mExternalStorageAvailable = true;
		}
		
		if(!mExternalStorageAvailable)
			return directories;
		
		try
		{
         String[] filePaths = new String[] { "mnt/sdcard", "mnt/extSdCard" };
			File externalDir;
			File[] rootFolder;
			ArrayList<String> directoryList = new ArrayList<String>();

         for(String path : filePaths)
         {
            externalDir = new File(path);
            rootFolder = externalDir.listFiles(FileLoader.getFilter(filter));

//            Log.e("RETRIEVE FILES", path);

            if(rootFolder != null)
            {
               if(rootFolder.length > 0)
               {
                  directoryList.add(rootFolder[0].getAbsolutePath());
//                  Log.d("ROOT FOLDER", rootFolder[0].getAbsolutePath());
//                  Log.d("ROOT SIZE", "" + rootFolder.length);
               }
            }

            File[] searchList = FileLoader.findDirectory(externalDir, filter);

            if(searchList != null && rootFolder != null)
            {
               String absPath;

               for(File file : searchList)//int i = 0; i < searchList.length; ++i)
               {
                  absPath = file.getAbsolutePath();

                  if(!absPath.equals(rootFolder[0].getAbsolutePath()))
                     directoryList.add(absPath);
               }
            }
         }

//         Log.e("DIR SIZE", "" + directoryList.size());

			directories = new String[directoryList.size()];
			directoryList.toArray(directories);
		}
		catch(Exception e)
		{
			Log.e("DIRECTORIES", e.getMessage());
			
			return directories;
		}
		
		return directories;
	}
	
	public static String[] retrieveMeshDirectories(String[] baseFolders)
	{
		boolean mExternalStorageAvailable = false;
		String state = Environment.getExternalStorageState();
		String[] meshDirs = new String[0];
		ArrayList<String> directories = new ArrayList<String>();
		
		
		// Check if the external media is readable
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) 
		{
		    // We can read and write the media
		    mExternalStorageAvailable = true;
		}
		
		if(!mExternalStorageAvailable)
			return meshDirs;
		
		try
		{
			for(int i = 0; i < baseFolders.length; ++i)
			{
				File rootPath = new File(baseFolders[i]);
				
				if(!rootPath.exists() || !rootPath.isDirectory())
					continue;
				
				meshDirs = FileLoader.listDirectories(rootPath);
				
				if(meshDirs != null)
				{
					for(int j = 0; j < meshDirs.length; ++j)
						directories.add(meshDirs[j]);
				}
			}
			
			meshDirs = new String[directories.size()];
			directories.toArray(meshDirs);
		}
		catch(Exception e)
		{
			Log.d("MESH DIR", e.getMessage());
			return null;
		}
		
		return FileLoader.sortPaths(meshDirs);
	}
	
	public static boolean copyFolderToInternal(Context context, File directory)
	{
		File dataDir = context.getDir("wallpaper", Context.MODE_PRIVATE);
		File[] modelFiles = directory.listFiles();

      if(modelFiles == null)
         return false;
		else if(modelFiles.length == 0)
			return false;

		// Clear the directory of any files or create the directory if it does not exist
//		if(dataDir.exists())
			FileLoader.clearInternalDirectory(context);
//		else
//			dataDir.mkdir();

		// Try and copy over the file content of the supplied folder.
		try
		{
			InputStream is;
			OutputStream os;
			
			for(File file : modelFiles)//int i = 0; i < modelFiles.length; ++i)
			{
//				if(file.getName().contains(".pvb"))
//					continue;

				String path = dataDir + "/" + file.getName();
				File tmpFile = new File(path);
				
				tmpFile.createNewFile();
				
				is = new FileInputStream(file);//context.openFileInput(modelFiles[i].getAbsolutePath());
				os = context.openFileOutput(file.getName(), Context.MODE_PRIVATE);

				FileLoader.copyFile(is, os);

            os.flush();
				os.close();
            is.close();
			}
		}
		catch(Exception e)
		{
			Log.e("INTERNAL COPY ERROR", e.getMessage());
			return false;
		}
		
		
		return true;
	}
	
	private static void clearInternalDirectory(Context context)
	{
		File dataDir = context.getDir("wallpaper", Context.MODE_PRIVATE);
		File[] modelFiles = dataDir.listFiles();
		
		// Check for a null value
		if(modelFiles != null)
		{
			// Loop through and delete all files.
			for(File tmpFile : modelFiles)
				tmpFile.delete();
		}
	}
	
	private static void copyFile(InputStream in, OutputStream out)
	{
		byte[] buffer = new byte[1024];
		int length;// = in.read(buffer);
		
		try
		{
         length = in.read(buffer);

			while(length > 0)
         {
				out.write(buffer, 0, length);
            length = in.read(buffer);
         }
		}
		catch(Exception e)
		{
			Log.e("COPY FILE", e.getMessage());
		}
	}
}
