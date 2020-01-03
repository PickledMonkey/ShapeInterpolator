package com.monkey.pickled.src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ShapeSplineData 
{
	public ArrayList<SplineInterpolator> pointSplinesX = null;
	public ArrayList<SplineInterpolator> pointSplinesY = null;
	
	public SplineInterpolator translationSplineX = null;
	public SplineInterpolator translationSplineY = null;
	
	public SplineInterpolator scalingSplineX = null;
	public SplineInterpolator scalingSplineY = null;
	
	public SplineInterpolator shearSpline = null;
	
	public SplineInterpolator rotationSpline = null;
	
	ShapeSplineData()
	{
		
	}
	
	
	public boolean generateSplines(SplineReferenceSetData refData)
	{
		if(refData == null)
		{
			return false;
		}
		
		generatePointSplines(refData.referenceShapePointsX, refData.referenceShapePointsY);
		
		if(!generateTranslationSpline(refData.referenceTranslationsX, refData.referenceTranslationsY))
		{
			return false;
		}
		
		if(!generateScalingSpline(refData.referenceScalingsX, refData.referenceScalingsY))
		{
			return false;
		}
		
		if(!generateShearSpline(refData.referenceShears))
		{
			return false;
		}
		
		if(!generateRotationSpline(refData.referenceRotationAngles))
		{
			return false;
		}
		
		return true;
	}
	
	public boolean interpolateShape(ShapeData shape, double t)
	{
		if(pointSplinesX == null || pointSplinesY == null)
		{
			return false;
		}
		
		if(pointSplinesX.size() != pointSplinesY.size() || pointSplinesX.size() == 0)
		{
			return false;
		}
		
		int numPointSplines = pointSplinesX.size();
		double[] newPointsX = new double[numPointSplines];
		double[] newPointsY = new double[numPointSplines];
		for(int i = 0; i < numPointSplines; i++)
		{
			newPointsX[i] = pointSplinesX.get(i).interpolate(t);;
			newPointsY[i] = pointSplinesY.get(i).interpolate(t);
		}
		shape.setPointData(newPointsX, newPointsY);
		
		double newTranslationX = translationSplineX.interpolate(t);
		double newTranslationY = translationSplineY.interpolate(t);
		shape.setTranslation(newTranslationX, newTranslationY);
		
		double newScalingX = scalingSplineX.interpolate(t);
		double newScalingY = scalingSplineY.interpolate(t);
		shape.setScaling(newScalingX, newScalingY);
		
		double newShear = shearSpline.interpolate(t);
		shape.setShear(newShear);
		
		double newRotationAngle = rotationSpline.interpolate(t);
		shape.setRotation(newRotationAngle);
		
		return true;
	}
	
	private boolean generatePointSplines(ArrayList<ArrayList<Double>> shapeSetPointsX, ArrayList<ArrayList<Double>> shapeSetPointsY)
	{			
		if(shapeSetPointsX == null || shapeSetPointsY == null)
		{
			return false;
		}
		
		if(shapeSetPointsX.size() != shapeSetPointsY.size())
		{
			return false;
		}
		
		pointSplinesX = new ArrayList<SplineInterpolator>();
		pointSplinesY = new ArrayList<SplineInterpolator>();
		
		int numRefSets = shapeSetPointsX.size();
		int numPoints = shapeSetPointsX.get(0).size();
		
		for(int i = 0; i < numPoints; i++)
		{
			ArrayList<Double> xPoints = new ArrayList<Double>();
			ArrayList<Double> yPoints = new ArrayList<Double>();
			
			for(int j = 0; j < numRefSets; j++)
			{
				xPoints.add(shapeSetPointsX.get(j).get(i));
				yPoints.add(shapeSetPointsY.get(j).get(i));
			}			
			
			pointSplinesX.add(generateSpline(xPoints));
			pointSplinesY.add(generateSpline(yPoints));
		}
					
		return true;
	}
	
	private boolean generateTranslationSpline(ArrayList<Double> xVals, ArrayList<Double> yVals)
	{		
		translationSplineX = generateSpline(xVals);
		translationSplineY = generateSpline(yVals);
		
		if(translationSplineX == null || translationSplineY == null)
		{
			return false;
		}
		
		return true;
	}
	
	private boolean generateScalingSpline(ArrayList<Double> xVals, ArrayList<Double> yVals)
	{
		scalingSplineX = generateSpline(xVals);
		scalingSplineY = generateSpline(yVals);
		
		if(scalingSplineX == null || scalingSplineY == null)
		{
			return false;
		}
		
		return true;
	}
	
	private boolean generateShearSpline(ArrayList<Double> vals)
	{
		shearSpline = generateSpline(vals);
		
		if(shearSpline == null)
		{
			return false;
		}
		
		return true;
	}
	
	private boolean generateRotationSpline(ArrayList<Double> vals)
	{
		rotationSpline = generateSpline(vals);
		
		if(rotationSpline == null)
		{
			return false;
		}
		
		return true;
	}
	
	private SplineInterpolator generateSpline(ArrayList<Double> vals)
	{
		if(vals == null)
		{
			return null;
		}
		
		int numVals = vals.size();
		
		ArrayList<Double>xArr = new ArrayList<Double>();
		
		double xItVal = 1.0/((double)(numVals-1));
		double xVal = 0.0;
		for(int i = 0; i < numVals; i++)
		{
			xArr.add(xVal);
			xVal += xItVal;
		}
		
		return SplineInterpolator.createMonotoneCubicSpline(xArr, vals);
	}
	
//	private PolynomialSplineFunction generateSpline(SplineInterpolator interpolator, double[] vals)
//	{
//		if(interpolator == null || vals == null)
//		{
//			return null;
//		}
//		
//		int numVals = vals.length;
//		
//		double[] xArr = new double[numVals];
//		
//		double xItVal = 1.0/((double)numVals);
//		double xVal = 0.0;
//		for(int i = 0; i < numVals; i++)
//		{
//			xArr[i] = xVal;
//			xVal += xItVal;
//		}
//		
//		return interpolator.interpolate(xArr, vals);
//	}
	
}
