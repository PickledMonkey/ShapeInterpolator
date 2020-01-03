package com.monkey.pickled.src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class ShapeInterpolator 
{
	
	
	private ArrayList<ShapeDataSet> referenceShapeSets = new ArrayList<ShapeDataSet>();
	private ShapeDataSet interpolatedShapeSet = null;
	
	private HashMap<String, SplineReferenceSetData> referenceDataMap = null;
	private HashMap<String, ShapeSplineData> shapeSplineMap = null;

	public ShapeInterpolator()
	{
		
	}
	
	public boolean reset()
	{
		referenceShapeSets = new ArrayList<ShapeDataSet>();
		interpolatedShapeSet = null;
		
		referenceDataMap = null;
		shapeSplineMap = null;
		return true;
	}
	
	public boolean setAsInterpolationSet(ShapeDataSet newInterpolationSet)
	{
		if(referenceShapeSets == null)
		{
			return false;
		}
		if(referenceShapeSets.size() < 1)
		{
			return false;
		}
		
		if(!canAddSet(newInterpolationSet))
		{
			return false;
		}
		
		interpolatedShapeSet = newInterpolationSet;
		return true;
	}
	
	public boolean addReferenceSet(ShapeDataSet newRefSet)
	{
		if(!canAddSet(newRefSet))
		{
			return false;
		}
		
		if(referenceShapeSets.isEmpty())
		{
			interpolatedShapeSet = new ShapeDataSet(newRefSet);
		}
		
		referenceShapeSets.add(newRefSet);
		return true;
	}
	
	public boolean canAddSet(ShapeDataSet dataSet)
	{
		if(referenceShapeSets.isEmpty())
		{
			return true;
		}
		
		if(dataSet == null)
		{
			return false;
		}
		
		if(dataSet.isEquivalent(interpolatedShapeSet))
		{
			return true;
		}
		
		return false;
	}
	

	public boolean gatherReferenceData()
	{
		if(referenceShapeSets.size() < 2)
		{
			return false;
		}
		
		referenceDataMap = new HashMap<String, SplineReferenceSetData>();
		
		for(int i = 0; i < referenceShapeSets.size(); i++)
		{
			if(!referenceShapeSets.get(i).addShapeDataToReferences(referenceDataMap))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public boolean generateSplines()
	{
		if(referenceDataMap == null)
		{
			return false;
		}
		
		shapeSplineMap = new HashMap<String, ShapeSplineData>();
		
		Set<String> keys = referenceDataMap.keySet();
		for(Iterator<String> it = keys.iterator(); it.hasNext();)
		{
			String key = it.next();
			
			ShapeSplineData spline = new ShapeSplineData();
			spline.generateSplines(referenceDataMap.get(key));
			
			shapeSplineMap.put(key, spline);
		}
		
		return true;
	}
	
	public boolean interpolateSpline(double t)
	{
		if(interpolatedShapeSet == null || shapeSplineMap == null)
		{
			return false;
		}
		
		if(!interpolatedShapeSet.interpolateFromSplines(shapeSplineMap, t))
		{
			return false;
		}
		
		return true;
	}
	
	public boolean createNewInterpolatedShapeSet()
	{
		if(referenceShapeSets == null)
		{
			return false;
		}
		
		if(referenceShapeSets.size() < 1)
		{
			return false;
		}
		
		interpolatedShapeSet = new ShapeDataSet(referenceShapeSets.get(0));
		
		return true;
	}
	
	public boolean generateInterpolatedShapes()
	{
		if(interpolatedShapeSet == null)
		{
			return false;
		}
		
		if(!interpolatedShapeSet.updateShapeTransformations())
		{
			return false;
		}
		
		return true;
	}
	
}
