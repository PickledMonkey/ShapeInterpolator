package com.monkey.pickled.src;

import java.util.ArrayList;
import java.util.HashMap;

public class SplineReferenceSetData 
{
	
	ArrayList<ArrayList<Double>> referenceShapePointsX = new ArrayList<ArrayList<Double>>();
	ArrayList<ArrayList<Double>> referenceShapePointsY = new ArrayList<ArrayList<Double>>();
	ArrayList<Double> referenceTranslationsX = new ArrayList<Double>();
	ArrayList<Double> referenceTranslationsY = new ArrayList<Double>();
	ArrayList<Double> referenceScalingsX = new ArrayList<Double>();
	ArrayList<Double> referenceScalingsY = new ArrayList<Double>();
	ArrayList<Double> referenceShears = new ArrayList<Double>();
	ArrayList<Double> referenceRotationAngles = new ArrayList<Double>();

	
	public SplineReferenceSetData()
	{
		
		
	}
	
	
	public boolean addPoints(ArrayList<Double> pointsX, ArrayList<Double> pointsY)
	{
		if(pointsX == null || pointsY == null)
		{
			return false;
		}
		
		if(pointsX.size() != pointsY.size())
		{
			return false;
		}
		
		referenceShapePointsX.add(pointsX);
		referenceShapePointsY.add(pointsY);
		
		return true;
	}
	
	public boolean addTranslation(Double X, Double Y)
	{
		if(X == null || Y == null)
		{
			return false;
		}
		
		referenceTranslationsX.add(X);
		referenceTranslationsY.add(Y);
	
		return true;
	}
	
	public boolean addScaling(Double X, Double Y)
	{
		if(X == null || Y == null)
		{
			return false;
		}
		
		referenceScalingsX.add(X);
		referenceScalingsY.add(Y);
	
		return true;
	}
	
	public boolean addShear(Double shear)
	{
		if(shear == null)
		{
			return false;
		}
		
		referenceShears.add(shear);
	
		return true;
	}
	
	public boolean addRotation(Double rotationAngle)
	{
		if(rotationAngle == null)
		{
			return false;
		}
		
		referenceRotationAngles.add(rotationAngle);
	
		return true;
	}
	
}
