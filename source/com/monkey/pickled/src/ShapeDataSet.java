package com.monkey.pickled.src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.UnoRuntime;

public class ShapeDataSet 
{
	public static final int x = 0;
	public static final int y = 1;
	public static final int numAxis = 2;

	private HashMap<String, ShapeData> shapes = new HashMap<String, ShapeData>();
	
	
	public ShapeDataSet()
	{
		
	}
	
	public ShapeDataSet(XShapes selectedShapes)
	{
		parseShapeSelection(selectedShapes);
	}
	
	public ShapeDataSet(ShapeDataSet other)
	{
		deepCopy(other);
	}
	
	private void deepCopy(ShapeDataSet other)
	{
		shapes = new HashMap<String, ShapeData>();
		
		Set<String> keys = other.shapes.keySet();
		for(Iterator<String> it = keys.iterator(); it.hasNext();)
		{
			String key = it.next();
		
			shapes.put(key, new ShapeData(other.shapes.get(key)));
		}
		
	}
	
	public boolean interpolateFromSplines(HashMap<String, ShapeSplineData> splineMap, double t)
	{
		if(splineMap == null)
		{
			return false;
		}
		
		Set<String> keys = shapes.keySet();
		for(Iterator<String> it = keys.iterator(); it.hasNext();)
		{
			String key = it.next();
			ShapeSplineData spline = splineMap.get(key);
			ShapeData shape = shapes.get(key);
			
			spline.interpolateShape(shape, t);
		}
		
		
		return true;
	}
	
	public boolean updateShapeTransformations()
	{
		Set<String> keys = shapes.keySet();
		for(Iterator<String> it = keys.iterator(); it.hasNext();)
		{
			String key = it.next();
			ShapeData shape = shapes.get(key);
			
			if(!shape.updateReferenceShape())
			{
				return false;
			}
		}
		
		return true;
	}
	
	public boolean parseShapeSelection(XShapes selectedShapes)
	{
		if(selectedShapes == null)
		{
			return false;
		}
		
		for(int i = 0; i < selectedShapes.getCount(); i++)
		{
			Object shape;
			try {
				shape = selectedShapes.getByIndex(i);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
				return false;
			} catch (WrappedTargetException e) {
				e.printStackTrace();
				return false;
			}
			
			XShape xShape = UnoRuntime.queryInterface(XShape.class, shape);
			if(xShape != null)
			{
				addShape(xShape);
			}
		}
		
		return true;
	}
	
	public int addShape(XShape shape)
	{
		ArrayList<ShapeData> shapeDataArr = ShapeData.CreateShapeData(shape);
		if(shapeDataArr == null)
		{
			return -1;
		}
		
		int shapesAdded = 0;
		for(int i = 0; i < shapeDataArr.size(); i++)
		{
			ShapeData shapeData = shapeDataArr.get(i);
			if(shapeData.isInitialized())
			{
				if(addShapeToMap(shapeData))
				{
					shapesAdded++;
				}
			}
		}
		
		return shapesAdded;
	}
	
	public String getShapeName()
	{
		//Only works for sets with only 1 shape
		if(shapes.size() != 1)
		{
			return null;
		}
		
		String name = null;
		Set<String> keys = shapes.keySet();
		for(Iterator<String> it = keys.iterator(); it.hasNext();)
		{
			String key = it.next();
			name = shapes.get(key).getName();
		}
		
		return name;
	}
	
	public boolean renameShapes(String newName)
	{
		if(newName == null || newName == "")
		{
			return false;
		}
		
		HashMap<String, ShapeData> remappedShapes = new HashMap<String, ShapeData>();
		
		Set<String> keys = shapes.keySet();
		for(Iterator<String> it = keys.iterator(); it.hasNext();)
		{
			String key = it.next();
			
			ShapeData shape = shapes.get(key);
			
			if(shapes.size() <= 1)
			{
				shape.setName(newName);
			}
			else
			{
				shape.setNameWithZOrder(newName);
			}
			
			String changedName = shape.getName();
			
			remappedShapes.put(changedName, shape);
		}
		
		shapes = remappedShapes;
		
		return true;
	}
	
	
	public boolean addShapeDataToReferences(HashMap<String, SplineReferenceSetData> referenceDataMap)
	{
		if(referenceDataMap == null || shapes.size() < 1)
		{
			return false;
		}
		
		Set<String> keys = shapes.keySet();
		for(Iterator<String> it = keys.iterator(); it.hasNext();)
		{
			String key = it.next();
			
			if(!referenceDataMap.containsKey(key))
			{
				referenceDataMap.put(key, new SplineReferenceSetData());
			}
			
			SplineReferenceSetData ref = referenceDataMap.get(key);
			ShapeData shape = shapes.get(key);
			
			if(!ref.addPoints(shape.getPointData(ShapeData.x), shape.getPointData(ShapeData.y)))
			{
				System.out.println("Unable to add points to reference for Shape="+shape.getName());
				return false;
			}
			
			if(!ref.addScaling(shape.getScaling(ShapeData.x), shape.getScaling(ShapeData.y)))
			{
				System.out.println("Unable to add scaling to reference for Shape="+shape.getName());
				return false;
			}
			
			if(!ref.addTranslation(shape.getTranslation(ShapeData.x), shape.getTranslation(ShapeData.y)))
			{
				System.out.println("Unable to add translation to reference for Shape="+shape.getName());
				return false;
			}
			
			if(!ref.addRotation(shape.getRotation()))
			{
				System.out.println("Unable to add rotation to reference for Shape="+shape.getName());
				return false;
			}
			
			if(!ref.addShear(shape.getShear()))
			{
				System.out.println("Unable to add shear to reference for Shape="+shape.getName());
				return false;
			}
		}
		
		return true;
	}
	
	public boolean isEquivalent(ShapeDataSet other)
	{
		if(other == null)
		{
			return false;
		}
		
		if(shapes.size() != other.shapes.size())
		{
			return false;
		}
		
		Set<String> keys = shapes.keySet();
		for(Iterator<String> it = keys.iterator(); it.hasNext();)
		{
			String key = it.next();
			if(!other.shapes.containsKey(key))
			{
				return false;
			}
			if(!shapes.get(key).isEquivalent(other.shapes.get(key)))
			{
				return false;
			}
		}
		
		return true;
	}
	
	private boolean addShapeToMap(ShapeData shape)
	{
		if(shapes.containsKey(shape.shapeName))
		{
			String newName = generateUniqueName(shape.shapeName);
			shape.setShapeName(newName);
		}
		
		shapes.put(shape.shapeName, shape);
		
		return true;
	}
	
	private String generateUniqueName(String baseName)
	{
		String newName = baseName;
		//This is a terrible solution, but hey, it should work.
		while(shapes.containsKey(newName))
		{
			int tagNum = (int)(Math.random() * 5000.0);
			newName = baseName + "_" + tagNum;
		}

		return newName;
	}
	
	
}
