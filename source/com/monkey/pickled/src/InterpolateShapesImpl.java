package com.monkey.pickled.src;

import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.lib.uno.helper.Factory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.monkey.pickled.dialog.RenameShapeDialog;
import com.monkey.pickled.dialog.SplineDialog;
import com.monkey.pickled.helper.DialogHelper;
import com.monkey.pickled.helper.PageHelper;
import com.monkey.pickled.helper.ShapeHelper;
import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.comp.beans.OfficeDocument;
import com.sun.star.frame.Desktop;
import com.sun.star.frame.FrameSearchFlag;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.drawing.PolyPolygonBezierCoords;
import com.sun.star.drawing.PolygonFlags;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;


public final class InterpolateShapesImpl extends WeakBase
   implements com.monkey.pickled.XInterpolateShapes,
              com.sun.star.lang.XServiceInfo,
              com.sun.star.task.XJobExecutor
{
	
	//Private Static Variables
	private static final String m_implementationName = InterpolateShapesImpl.class.getName();
    private static final String[] m_serviceNames = { "com.monkey.pickled.InterpolateShapes" };

    private static final String resetSplineGenSetAction = "resetSplineGenerationSet";
    private static final String addShapesToSplineGenAction = "addShapeSetToSplineGen";
    private static final String createInterpolatedShapesAction = "generateSplines";
    private static final String renameShapeAction  = "renameShape";
    private static final String setInterpolationSetAction = "setAsInterpolationSet";
	
	//Private Member Variables
    private final XComponentContext m_xContext;
    private XComponent m_xDrawDoc;
    
    private static ShapeInterpolator interpolator = new ShapeInterpolator();
    private static ShapeDataSet selectedShapeSet = null;
    
    //Public Methods
    public InterpolateShapesImpl( XComponentContext context )
    {
        m_xContext = context;
    };

    public static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
        XSingleComponentFactory xFactory = null;

        if ( sImplementationName.equals( m_implementationName ) )
            xFactory = Factory.createComponentFactory(InterpolateShapesImpl.class, m_serviceNames);
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
        return Factory.writeRegistryServiceInfo(m_implementationName,
                                                m_serviceNames,
                                                xRegistryKey);
    }
    
    public static void createInterpolatedShapes()
    {
    	interpolator.createNewInterpolatedShapeSet();
    	interpolateShapes(0.0);
    }
    
    public static void interpolateShapes(double t)
    {		
		if(!interpolator.interpolateSpline(t))
		{
			System.out.println("Unable interpolate splines");
			return;
		}
		
		if(!interpolator.generateInterpolatedShapes())
		{
			System.out.println("Unable create shapes");
			return;
		}
    }

    // com.sun.star.lang.XServiceInfo:
    public String getImplementationName() {
         return m_implementationName;
    }

    public boolean supportsService( String sService ) {
        int len = m_serviceNames.length;

        for( int i=0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }

	@Override
	public void trigger(String action) 
	{		
		switch (action)
		{
		case resetSplineGenSetAction:
			resetSplineGenerationSet();
			break;
		case addShapesToSplineGenAction:
			addShapesToSplineGenerationSet();
			break;
		case createInterpolatedShapesAction:
			generateSplines();
			break;
		case renameShapeAction:
			renameShapes();
			break;
		case setInterpolationSetAction:
			setSelectedShapesAsInterpolationSet();
			break;
		default:
			//Do Nothing
			break;
		}
	}
	
	//Private Methods	
	private boolean getDrawDocument()
	{
		XMultiComponentFactory mcf = m_xContext.getServiceManager();
		
		
		Object desktop = null;
		try {
			 desktop = mcf.createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);
		} catch (Exception e) {
			//e.printStackTrace();
			return false;
		}
		
		XDesktop docDesktop = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, desktop);
		
		if(docDesktop == null)
		{
			return false;
		}
		
		XComponent xDrawComp = docDesktop.getCurrentComponent();
		
//		//CREATES A NEW DOCUMENT RATHER THAN SELECTED ONE
//		XComponentLoader componentLdr = (XComponentLoader)UnoRuntime.queryInterface(XComponentLoader.class, desktop);
//		
//		PropertyValue[] propValues = new PropertyValue[0];
//		
//		XComponent xDrawComp = null;
//		
//		try {
//			xDrawComp = componentLdr.loadComponentFromURL("private:factory/sdraw", "_default", 0, propValues);
//		} catch (IllegalArgumentException e) {
//			//e.printStackTrace();
//			return false;
//		} catch (IOException e) {
//			//e.printStackTrace();
//			return false;
//		}
		
		if (xDrawComp == null)
		{
			//Document not found
			return false;
		}
		
		m_xDrawDoc = xDrawComp;
		
		return true;
	}
	
	private XShapes getSelectedShapes()
	{
		if(m_xDrawDoc == null)
		{
			return null;
		}
		
		XModel docModel = (XModel)UnoRuntime.queryInterface(XModel.class, m_xDrawDoc);
		if(docModel == null)
		{
			return null;
		}
		
		XInterface selection = (XInterface) docModel.getCurrentSelection();
		
		if(selection == null)
		{
			return null;
		}		
		
		XShapes selectedShapes = (XShapes)UnoRuntime.queryInterface(XShapes.class, selection);
		
		if(selectedShapes == null)
		{
			return null;
		}		
		
		return selectedShapes;
	}
	
	private void setShapeSet()
	{
		if(m_xDrawDoc == null)
		{
			if(!getDrawDocument())
			{
				System.out.println("No draw document found");
				return;
			}
		}
		
		XShapes selectedShapes = getSelectedShapes();
		
		if(selectedShapes == null)
		{
			System.out.println("Unable to find any selected shapes");
			return;
		}
		
		ShapeDataSet shapeSet = new ShapeDataSet();
		
		if(!shapeSet.parseShapeSelection(selectedShapes))
		{
			System.out.println("Unable to parse shapes");
			return;
		}
		
		selectedShapeSet = shapeSet;
	}
	
	private void resetSplineGenerationSet()
	{
		interpolator.reset();
	}
	
	private void addShapesToSplineGenerationSet()
	{
		setShapeSet();
		
		if(selectedShapeSet == null)
		{
			System.out.println("No shapes selected");
			return;
		}
		
		if(!interpolator.addReferenceSet(selectedShapeSet))
		{
			System.out.println("Could not add reference set. Was probably not equivalent to the sets already contained");
		}
	}
	
	private void generateSplines()
	{
		if(!interpolator.gatherReferenceData())
		{
			System.out.println("Could not gather reference data for spline interpolation");
			return;
		}
		
		if(!interpolator.generateSplines())
		{
			System.out.println("Unable to generate splines");
			return;
		}
		
    	getDrawDocument();
		ShapeHelper.setDrawDoc(m_xDrawDoc);
		
		SplineDialog splineDialog = new SplineDialog(m_xContext);
		splineDialog.show();
	}
	
	private void renameShapes()
	{
		setShapeSet();
		if(selectedShapeSet == null)
		{
			System.out.println("No Shapes Selected");
		}
		
		RenameShapeDialog renameDialog = new RenameShapeDialog(m_xContext);
		renameDialog.setShapes(selectedShapeSet);
		
		renameDialog.show();
	}
	
	private void setSelectedShapesAsInterpolationSet()
	{
		setShapeSet();
		if(selectedShapeSet == null)
		{
			System.out.println("No Shapes Selected");
		}
		
		if(!interpolator.setAsInterpolationSet(selectedShapeSet))
		{
			System.out.println("Unable to set selected shapes as the interpolation set");
		}
	}
	
	private boolean drawAShapeOnPage()
	{		
		if(m_xDrawDoc == null)
		{
			if(!getDrawDocument())
			{
				return false;
			}
		}
		
		XShape xPolyPolygonBezier = null;
		
		try {
			ShapeHelper.setDrawDoc(m_xDrawDoc);
			xPolyPolygonBezier = ShapeHelper.createShape(new Point( 0, 0 ), new Size( 0, 0 ), "com.sun.star.drawing.ClosedBezierShape" );
		} catch (java.lang.Exception e) {
			//e.printStackTrace();
			return false;
		}
		
		XDrawPage xDrawPage = null;
		
		try {
			xDrawPage = PageHelper.getDrawPageByIndex(m_xDrawDoc, 0);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		} catch (WrappedTargetException e) {
			e.printStackTrace();
			return false;
		}
		
		XShapes xShapes = UnoRuntime.queryInterface( XShapes.class, xDrawPage );
        xShapes.add( xPolyPolygonBezier );
		
        XPropertySet xShapeProperties = UnoRuntime.queryInterface( XPropertySet.class, xPolyPolygonBezier );
        
        XPropertySet xPageProperties = UnoRuntime.queryInterface( XPropertySet.class, xDrawPage );
        int nPageWidth;
        int nPageHeight;
		try {
			nPageWidth = ((Integer)xPageProperties.getPropertyValue( "Width" )).intValue() / 2;
	        nPageHeight = ((Integer)xPageProperties.getPropertyValue( "Height" )).intValue() / 2;
		} catch (UnknownPropertyException | WrappedTargetException e) {
			//e.printStackTrace();
			return false;
		}

        
        PolyPolygonBezierCoords aCoords = new PolyPolygonBezierCoords();
        // allocating the outer sequence
        int nPolygonCount = 50;
        aCoords.Coordinates = new Point[ nPolygonCount ][ ];
        aCoords.Flags = new PolygonFlags[ nPolygonCount ][ ];
        int i, n, nY;
        // fill the inner point sequence now
        for ( nY = 0, i = 0; i < nPolygonCount; i++, nY += nPageHeight / nPolygonCount )
        {
            // create a polygon using two normal and two control points
            // allocating the inner sequence
            int nPointCount = 8;
            Point[]         pPolyPoints = new Point[ nPointCount ];
            PolygonFlags[]  pPolyFlags  = new PolygonFlags[ nPointCount ];

            for ( n = 0; n < nPointCount; n++ )
                pPolyPoints[ n ] = new Point();

            pPolyPoints[ 0 ].X = 0;
            pPolyPoints[ 0 ].Y = nY;
            pPolyFlags[ 0 ] = PolygonFlags.NORMAL;
            pPolyPoints[ 1 ].X = nPageWidth / 2;
            pPolyPoints[ 1 ].Y = nPageHeight;
            pPolyFlags[ 1 ] = PolygonFlags.CONTROL;
            pPolyPoints[ 2 ].X = nPageWidth / 2;
            pPolyPoints[ 2 ].Y = nPageHeight;
            pPolyFlags[ 2 ] = PolygonFlags.CONTROL;
            pPolyPoints[ 3 ].X = nPageWidth;
            pPolyPoints[ 3 ].Y = nY;
            pPolyFlags[ 3 ] = PolygonFlags.NORMAL;

            pPolyPoints[ 4 ].X = nPageWidth - 1000;
            pPolyPoints[ 4 ].Y = nY;
            pPolyFlags[ 4 ] = PolygonFlags.NORMAL;
            pPolyPoints[ 5 ].X = nPageWidth / 2;
            pPolyPoints[ 5 ].Y = nPageHeight / 2;
            pPolyFlags[ 5 ] = PolygonFlags.CONTROL;
            pPolyPoints[ 6 ].X = nPageWidth / 2;
            pPolyPoints[ 6 ].Y = nPageHeight / 2;
            pPolyFlags[ 6 ] = PolygonFlags.CONTROL;
            pPolyPoints[ 7 ].X = 1000;
            pPolyPoints[ 7 ].Y = nY;
            pPolyFlags[ 7 ] = PolygonFlags.NORMAL;

            aCoords.Coordinates[ i ]= pPolyPoints;
            aCoords.Flags[ i ]      = pPolyFlags;
        }
        
        try {
			xShapeProperties.setPropertyValue( "PolyPolygonBezier", aCoords );
			// move the shape to the back by changing the ZOrder
			xShapeProperties.setPropertyValue( "ZOrder", Integer.valueOf( 1 ) );
        } catch (IllegalArgumentException e) {
        	//e.printStackTrace();
			return false;
		} catch (UnknownPropertyException e) {
			//e.printStackTrace();
			return false;
		} catch (PropertyVetoException e) {
			//e.printStackTrace();
			return false;
		} catch (WrappedTargetException e) {
			//e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
}
