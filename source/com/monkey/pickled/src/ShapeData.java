package com.monkey.pickled.src;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import com.monkey.pickled.helper.PageHelper;
import com.monkey.pickled.helper.ShapeHelper;
import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.drawing.HomogenMatrix3;
import com.sun.star.drawing.HomogenMatrixLine3;
import com.sun.star.drawing.PolyPolygonBezierCoords;
import com.sun.star.drawing.PolygonFlags;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;


public class ShapeData 
{
	public static final int x = 0;
	public static final int y = 1;
	
	
	private static final String OpenBezierShape = "com.sun.star.drawing.OpenBezierShape";
	private static final String ClosedBezierShape = "com.sun.star.drawing.ClosedBezierShape";
	private static final String PolyPolygonBezierShape = "com.sun.star.drawing.PolyPolygonBezierShape";
	private static final String PolyPolygonShape = "com.sun.star.drawing.PolyPolygonShape";
	private static final String PolyLineShape = "com.sun.star.drawing.PolyLineShape";
	private static final String GroupShape = "com.sun.star.drawing.GroupShape";
	private static final String CustomShape = "com.sun.star.drawing.CustomShape";
	private static final String LineShape = "com.sun.star.drawing.LineShape";
	private static final String GraphicObjectShape = "com.sun.star.drawing.GraphicObjectShape";
	private static final String RectangleShape = "com.sun.star.drawing.RectangleShape";
	private static final String TextShape = "com.sun.star.drawing.TextShape";
	private static final String EllipseShape = "com.sun.star.drawing.EllipseShape";	
	
	public enum PointType
	{
		Position(0),
		Bezier(1),
		Unknown(2);
		
		private int id;
		private PointType(int i) {id = i;}
		public int getVal() {return id;}
		public boolean equals(PointType other) {return id == other.id;}
	}
	
	public enum ShapeType
	{
		ClosedBezier(0),
		OpenBezier(1),
		PolyBezier(2),
		PolyPolygon(3),
		PolyLine(4),
		Line(5);
		
		
		private int id;
		private ShapeType(int i) {id = i;}
		public int getVal() {return id;}
		public boolean equals(ShapeType other) {return id == other.id;}
	}
	
	
	public class ShapePointData
	{		
		PointType type;
		
		int X;
		int Y;
		
		ShapePointData(PointType type, int X, int Y)
		{
			this.X = X;
			this.Y = Y;
			this.type = type;
		}
		
		ShapePointData(Point coord)
		{
			this.X = coord.X;
			this.Y = coord.Y;

			this.type = PointType.Unknown;
		}
		
		ShapePointData(Point coord, PolygonFlags flags)
		{
			this.X = coord.X;
			this.Y = coord.Y;
			
			switch(flags.getValue())
			{
			case PolygonFlags.CONTROL_value:
				this.type = PointType.Bezier;
				break;
			case PolygonFlags.NORMAL_value:
				this.type = PointType.Position;
				break;
			case PolygonFlags.SMOOTH_value:
				this.type = PointType.Unknown;
				break;
			case PolygonFlags.SYMMETRIC_value:
				this.type = PointType.Unknown;
				break;
			default:
				this.type = PointType.Unknown;
				break;
			}
		}
		
		ShapePointData(ShapePointData other)
		{
			type = other.type;
			X = other.X;
			Y = other.Y;
		}
		
		public boolean equals(ShapePointData other) 
		{
			return type.equals(other.type) && X == other.X && Y == other.Y;
		}
		
		public PolygonFlags getFlag()
		{
			switch(type)
			{
			case Bezier:
				return PolygonFlags.CONTROL;
			case Position:
				return PolygonFlags.NORMAL;
			case Unknown:
				return null;
			default:
				return null;
			}
		}
	}
	
	
	ShapeType type;
	public ArrayList<ShapePointData> points = new ArrayList<ShapePointData>();
	
	public double[] translationVector = new double[2];
	public double[] scalingVector = new double[2];
	public double shear = 0.0;
	public double rotationAngle = 0.0;
	public String shapeName = null;
	
	public XShape referenceShape = null;
	public XPropertySet referenceShapeProperties = null;
	
	private boolean initialized = false;
	
	public ShapeData()
	{
		
	}
	
	public ShapeData(XShape shape)
	{
		//UnoRuntime.queryInterface(PolyPolygonBezierShape.class, xPolyPolygonBezier);
	
		String shapeType = shape.getShapeType();
		
		System.out.println("Received Shape Type=" + shapeType);
		
		referenceShape = shape;
		
		XPropertySet xShapeProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, shape);
		referenceShapeProperties = xShapeProperties;
		
		parseShape(shape, shapeType);
	}
	
	public ShapeData(ShapeData other)
	{
		deepCopy(other);
	}	
	
	public static ArrayList<ShapeData> CreateShapeData(XShape shape)
	{
		if(shape == null)
		{
			return null;
		}
		
		ArrayList<ShapeData> shapeList = new ArrayList<ShapeData>();
		
		String shapeType = shape.getShapeType();
		if(shapeType.equals(GroupShape))
		{
			XShapes shapesInGroup = (XShapes)UnoRuntime.queryInterface(XShapes.class, shape);
			if(shapesInGroup != null)
			{
				ArrayList<ShapeData> groupShapeData = ParseShapeGroup(shapesInGroup);
				if(groupShapeData != null)
				{
					shapeList.addAll(groupShapeData);
				}
			}
		}
		else
		{
			ShapeData parsedShapeData = new ShapeData(shape);
			if(parsedShapeData.isInitialized())
			{
				shapeList.add(parsedShapeData);
			}
		}
		
		return shapeList;
	}
	
	private static ArrayList<ShapeData> ParseShapeGroup(XShapes shapeGroup)
	{
		if(shapeGroup == null)
		{
			return null;
		}
		
		ArrayList<ShapeData> shapeList = new ArrayList<ShapeData>();
		
		for(int i = 0; i < shapeGroup.getCount(); i++)
		{
			Object subShapeObj = null;
			try {
				subShapeObj = shapeGroup.getByIndex(i);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
				return null;
			} catch (WrappedTargetException e) {
				e.printStackTrace();
				return null;
			}
			
			XShape subShape = UnoRuntime.queryInterface(XShape.class, subShapeObj);
			if(subShape != null)
			{
				ArrayList<ShapeData> groupShapeData = CreateShapeData(subShape);
				if(groupShapeData != null)
				{
					shapeList.addAll(groupShapeData);
				}
			}
		}
		
		return shapeList;
	}
	
	public boolean isInitialized()
	{
		return initialized;
	}
	
	public boolean setPointData(double[] xPoints, double[] yPoints)
	{
		if(!isInitialized())
		{
			return false;
		}
		
		if(xPoints == null || yPoints == null)
		{
			return false;
		}
		
		if(xPoints.length != yPoints.length)
		{
			return false;
		}
		
		if(xPoints.length != points.size())
		{
			return false;
		}
		
		for(int i = 0; i < points.size(); i++)
		{
			points.get(i).X = (int) xPoints[i];
			points.get(i).Y = (int) yPoints[i];
		}
		
		return true;
	}
	
	public boolean setScaling(double xScale, double yScale)
	{
		if(!isInitialized())
		{
			return false;
		}
		
		scalingVector[x] = xScale;
		scalingVector[y] = yScale;
		
		return true;
	}
	
	public boolean setTranslation(double xTranslation, double yTranslation)
	{
		if(!isInitialized())
		{
			return false;
		}
		
		translationVector[x] = xTranslation;
		translationVector[y] = yTranslation;
		
		return true;
	}
	
	public boolean setShear(double newShear)
	{
		if(!isInitialized())
		{
			return false;
		}
		
		shear = newShear;
		
		return true;
	}
	
	public boolean setRotation(double newRotationAngle)
	{
		if(!isInitialized())
		{
			return false;
		}
		
		rotationAngle = newRotationAngle;
		
		return true;
	}
	
	public ArrayList<Double> getPointData(int axis)
	{
		if(axis < x || axis > y)
		{
			return null;
		}
		
		ArrayList<Double> pointList = new ArrayList<Double>();
		
		for(int i = 0; i < points.size(); i++)
		{
			if(axis == x)
			{
				pointList.add((double)points.get(i).X);
			}
			else if(axis == y)
			{
				pointList.add((double)points.get(i).Y);
			}
		}
		
		return pointList;
	}
	
	public boolean setName(String newName)
	{
		if(newName == null || newName == "")
		{
			return false;
		}
		
		shapeName = newName;
		
		applyName();
		
		return true;
	}
	
	public boolean setNameWithZOrder(String newName)
	{
		int zOrder = getZOrder();
		
		if(zOrder < 0)
		{
			return false;
		}
		
		return setName(newName + "_" + zOrder);
	}
	
	public String getName()
	{
		return shapeName;
	}
	
	public Double getShear()
	{
		if(!isInitialized())
		{
			return null;
		}
		return shear;
	}
	
	public Double getRotation()
	{
		if(!isInitialized())
		{
			return null;
		}
		return rotationAngle;
	}
	
	public Double getScaling(int axis)
	{
		if(!isInitialized() || axis < x || axis > y)
		{
			return null;
		}
		return scalingVector[axis];
	}
	
	public Double getTranslation(int axis)
	{
		if(!isInitialized() || axis < x || axis > y)
		{
			return null;
		}
		return translationVector[axis];
	}
	
	public boolean isEquivalent(ShapeData other)
	{
		if(other == null)
		{
			return false;
		}
		
		if(!other.isInitialized())
		{
			return false;
		}
		
		if(!type.equals(other.type))
		{
			return false;
		}

		if(points.size() != other.points.size())
		{
			return false;
		}
		
		for(int i = 0; i < points.size(); i++)
		{
			ShapePointData thisPoint = points.get(i);
			ShapePointData otherPoint = other.points.get(i);
			
			if(!thisPoint.type.equals(otherPoint.type))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public boolean updateReferenceShape()
	{
		if(!isInitialized())
		{
			return false;
		}
		
		if(referenceShape == null || referenceShapeProperties == null)
		{
			if(!createReferenceShape())
			{
				return false;
			}
		}
		
		if(!applyPoints())
		{
			return false;
		}
	
		if(!applyTransformation())
		{
			return false;
		}
		
		return true;
	}
	
	public boolean parseShape(XShape shape, String shapeType)
	{
		initialized = false;
		switch (shapeType)
		{
		case OpenBezierShape:
			type = ShapeType.OpenBezier;
			initialized = parseBezierShape();
			break;
		case ClosedBezierShape:
			type = ShapeType.ClosedBezier;
			initialized = parseBezierShape();
			break;
		case PolyPolygonBezierShape:
			type = ShapeType.PolyBezier;
			initialized = parseBezierShape();
			break;
		case PolyPolygonShape:
			type = ShapeType.PolyPolygon;
			initialized = parsePolygonShape();
			break;
		case PolyLineShape:
			type = ShapeType.PolyLine;
			initialized = parsePolygonShape();
			break;
		case GroupShape:
			break;
		case CustomShape:
			break;
		case LineShape:
			type = ShapeType.Line;
			initialized = parsePolygonShape();
			break;
		case GraphicObjectShape:
			break;
		case RectangleShape:
			break;
		case TextShape:
			break;
		case EllipseShape:
			break;
		default:
			break;
		}
		
		return initialized;
	}
	
	public boolean setShapeName(String newName)
	{
		if(!isInitialized())
		{
			return false;
		}
		
		shapeName = newName;
		try {
			referenceShapeProperties.setPropertyValue("Name", newName);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			return false;
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			return false;
		} catch (WrappedTargetException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean createReferenceShape()
	{
		String shapeTypeName = getShapeTypeName();
		try {
			referenceShape = ShapeHelper.createShape(new Point( 0, 0 ), new Size( 0, 0 ), shapeTypeName );
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		if(referenceShape == null)
		{
			return false;
		}
		
		XPropertySet xShapeProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, referenceShape);
		if(xShapeProperties == null)
		{
			return false;
		}
		referenceShapeProperties = xShapeProperties;
		
		if(!applyName())
		{
			return false;
		}
		
		XDrawPage xDrawPage = null;
		
		try {
			xDrawPage = PageHelper.getDrawPageByIndex(ShapeHelper.xDrawDoc, 0);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		} catch (WrappedTargetException e) {
			e.printStackTrace();
			return false;
		}
		
		XShapes xShapes = UnoRuntime.queryInterface( XShapes.class, xDrawPage );
		if(xDrawPage == null)
		{
			return false;
		}
        xShapes.add(referenceShape);
		
		return true;
	}
	
	private String getShapeTypeName()
	{
		switch (type)
		{
			case ClosedBezier:
				return ClosedBezierShape;
			case OpenBezier:
				return OpenBezierShape;
			case PolyBezier:
				return PolyPolygonBezierShape;
			case PolyPolygon:
				return PolyPolygonShape;
			case Line:
				return LineShape;
			case PolyLine:
				return PolyLineShape;
		default:
			break;
		}
		
		return null;
	}
	
	private boolean applyPoints()
	{
		switch (type)
		{
			case ClosedBezier:
			case OpenBezier:
			case PolyBezier:
				return applyBezierPoints();
			case PolyPolygon:
			case Line:
			case PolyLine:
				return applyPolygonPoints();
		default:
			break;
		}
		return false;
	}
	
	private boolean applyBezierPoints()
	{
		PolyPolygonBezierCoords bezCoords = new PolyPolygonBezierCoords();
		bezCoords.Coordinates = new Point[1][points.size()];
		bezCoords.Flags = new PolygonFlags[1][points.size()];
		for(int i = 0; i < bezCoords.Coordinates.length; i++)
		{
			for(int j = 0; j < bezCoords.Coordinates[i].length; j++)
			{
				ShapePointData point = points.get(j);
				
				bezCoords.Flags[i][j] = point.getFlag();				
				bezCoords.Coordinates[i][j] = new Point(point.X, point.Y);
			}
		}

		try {
			referenceShapeProperties.setPropertyValue("Geometry", bezCoords);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			return false;
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			return false;
		} catch (WrappedTargetException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean applyPolygonPoints()
	{
		Point[][] polyCoords = new Point[1][points.size()];
		for(int i = 0; i < polyCoords.length; i++)
		{
			for(int j = 0; j < polyCoords[i].length; j++)
			{
				ShapePointData point = points.get(j);	
				polyCoords[i][j] = new Point(point.X, point.Y);
			}
		}

		try {
			referenceShapeProperties.setPropertyValue("Geometry", polyCoords);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			return false;
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			return false;
		} catch (WrappedTargetException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean applyTransformation()
	{
		AffineTransform affineMatrix = constructTransformationMatrix();
		HomogenMatrix3 transformation = new HomogenMatrix3();
		copyMatrixToTransformation(affineMatrix, transformation);		
		
		try {
			referenceShapeProperties.setPropertyValue("Transformation", transformation);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			return false;
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			return false;
		} catch (WrappedTargetException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean applyName()
	{
		try {
			referenceShapeProperties.setPropertyValue("Name", shapeName);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			return false;
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			return false;
		} catch (WrappedTargetException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean parseBezierShape()
	{
		if(!initBezierGeometry())
		{
			return false;
		}
		
		if(!initTransformationData())
		{
			return false;
		}
		
		if(!initShapeName())
		{
			return false;
		}
		
		return true;
	}
	
	private boolean parsePolygonShape()
	{
		if(!initPolygonGeometry())
		{
			return false;
		}
		
		if(!initTransformationData())
		{
			return false;
		}
		
		if(!initShapeName())
		{
			return false;
		}
		
		return true;
	}
	
	private int getZOrder()
	{
		int zOrder = -1;
		
		try {
			zOrder = ((Integer)referenceShapeProperties.getPropertyValue("ZOrder")).intValue();
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			return -1;
		} catch (WrappedTargetException e) {
			e.printStackTrace();
			return -2;
		}
		
		return zOrder;
	}
	
	private boolean initShapeName()
	{
		String name = null;
		try {
			name = (String)referenceShapeProperties.getPropertyValue("Name");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			return false;
		} catch (WrappedTargetException e) {
			e.printStackTrace();
			return false;
		}
		
		int zOrder = getZOrder();
		
		if(name.isEmpty())
		{
			name = "Shape_"+zOrder;
			try {
				referenceShapeProperties.setPropertyValue("Name", name);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			} catch (UnknownPropertyException e) {
				e.printStackTrace();
				return false;
			} catch (PropertyVetoException e) {
				e.printStackTrace();
				return false;
			} catch (WrappedTargetException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		shapeName = name;
		
		System.out.println("Name="+name+" ZOrder="+zOrder);
		
		return true;
	}

	private boolean initBezierGeometry()
	{
		PolyPolygonBezierCoords bezCoords = null;
		try {
			bezCoords = (PolyPolygonBezierCoords) referenceShapeProperties.getPropertyValue("Geometry");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			return false;
		} catch (WrappedTargetException e) {
			e.printStackTrace();
			return false;
		}
		
		points.clear();
		for(int i = 0; i < bezCoords.Coordinates.length; i++)
		{
			for(int j = 0; j < bezCoords.Coordinates[i].length; j++)
			{
				
				PolygonFlags flags = bezCoords.Flags[i][j];
				Point coord = bezCoords.Coordinates[i][j];
				
				points.add(new ShapePointData(coord, flags));
			}
		}
		return true;
	}
	
	private boolean initPolygonGeometry()
	{
//		XPropertySetInfo info = referenceShapeProperties.getPropertySetInfo();
//		Property[] props = info.getProperties();
		
		Point[][] polyCoords = null;
		try {
			polyCoords = (Point[][]) referenceShapeProperties.getPropertyValue("Geometry");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			return false;
		} catch (WrappedTargetException e) {
			e.printStackTrace();
			return false;
		}
		
		points.clear();
		for(int i = 0; i < polyCoords.length; i++)
		{
			for(int j = 0; j < polyCoords[i].length; j++)
			{
				Point coord = polyCoords[i][j];
				
				points.add(new ShapePointData(coord));
			}
		}
		return true;
	}
	
	private boolean initTransformationData()
	{
		HomogenMatrix3 transformation = null;
		try {
			transformation = (HomogenMatrix3) referenceShapeProperties.getPropertyValue("Transformation");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			return false;
		} catch (WrappedTargetException e) {
			e.printStackTrace();
			return false;
		}
		

		decomposeTransformation(transformation);	
		
		return true;
	}
	
	private void decomposeTransformation(HomogenMatrix3 transformation)
	{
		/* Implemented two possible methods. Not sure which one is correct*/
		HomogenMatrixLine3 rowX = transformation.Line1;
		HomogenMatrixLine3 rowY = transformation.Line2;
		HomogenMatrixLine3 rowZ = transformation.Line3;
		
		//Works for Translation and rotation. Shearing is not working yet.
		getTranslationVector(rowX, rowY, rowZ);
		getRotationAngle(rowX, rowY, rowZ);
		getScalingVector(rowX, rowY, rowZ);
		getShearVector(rowX, rowY, rowZ);
	}
	
	//M = Translation * Rotation * Shear * Scale
	private AffineTransform constructTransformationMatrix()
	{
		AffineTransform mat = new AffineTransform();
		
		mat.setToIdentity();
		mat.concatenate(AffineTransform.getTranslateInstance(translationVector[x], translationVector[y]));
		mat.concatenate(AffineTransform.getRotateInstance(rotationAngle));
		mat.concatenate(AffineTransform.getShearInstance(shear, 0));
		mat.concatenate(AffineTransform.getScaleInstance(scalingVector[x], scalingVector[y]));
		
		return mat;
	}
	
	private void copyMatrixToTransformation(AffineTransform matrix, HomogenMatrix3 transformation)
	{
		double[] mat = new double[6];
		matrix.getMatrix(mat); 
		
		transformation.Line1.Column1 = mat[0];
		transformation.Line2.Column1 = mat[1];
		transformation.Line3.Column1 = 0.0;
		
		transformation.Line1.Column2 = mat[2];
		transformation.Line2.Column2 = mat[3];
		transformation.Line3.Column2 = 0.0;
		
		transformation.Line1.Column3 = mat[4];
		transformation.Line2.Column3 = mat[5];
		transformation.Line3.Column3 = 1.0;
	}
	
	
	private void getTranslationVector(HomogenMatrixLine3 rowX, HomogenMatrixLine3 rowY, HomogenMatrixLine3 rowZ)
	{		
		translationVector[x] = rowX.Column3;
		translationVector[y] = rowY.Column3;
	}
	
	
	private void getScalingVector(HomogenMatrixLine3 rowX, HomogenMatrixLine3 rowY, HomogenMatrixLine3 rowZ)
	{
		scalingVector[x] = vectorMagnitude(rowX.Column1, rowY.Column1);
		scalingVector[y] = (rowY.Column2*Math.cos(rotationAngle)) - (rowX.Column2*Math.sin(rotationAngle));
	}
	
	private void getRotationAngle(HomogenMatrixLine3 rowX, HomogenMatrixLine3 rowY, HomogenMatrixLine3 rowZ)
	{
		rotationAngle = Math.atan2(rowY.Column1, rowX.Column1);
	}
	
	private void getShearVector(HomogenMatrixLine3 rowX, HomogenMatrixLine3 rowY, HomogenMatrixLine3 rowZ)
	{
		
		double a = (rowX.Column2*Math.cos(rotationAngle)) + (rowY.Column2*Math.sin(rotationAngle));
		double b = (rowY.Column2*Math.cos(rotationAngle)) - (rowX.Column2*Math.sin(rotationAngle));
		
		shear = a/b;
	}
	
	private double vectorMagnitude(double xVal, double yVal)
	{
		double magnitude = xVal*xVal + yVal*yVal;
		magnitude = Math.sqrt(magnitude);
		return magnitude;
	}
	
	//Does not copy XShape or XProperties
	private void deepCopy(ShapeData other)
	{
		type = other.type;
		if(other.points != null)
		{
			points = new ArrayList<ShapePointData>();
			for(int i = 0; i < other.points.size(); i++)
			{
				points.add(new ShapePointData(other.points.get(i)));
			}
		}
		
		for(int i = 0; i < 2; i++)
		{
			translationVector[i] = other.translationVector[i];
			scalingVector[i] = other.scalingVector[i];
		}
		
		shear = other.shear;
		rotationAngle = other.rotationAngle;
		
		shapeName = other.shapeName;
		
		initialized = other.initialized;
	}
}







