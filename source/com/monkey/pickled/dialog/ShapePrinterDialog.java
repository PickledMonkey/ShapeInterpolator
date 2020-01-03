package com.monkey.pickled.dialog;

import java.util.HashMap;

import com.monkey.pickled.helper.DialogHelper;

import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogEventHandler;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.XComponentContext;

public class ShapePrinterDialog 
	implements XDialogEventHandler
{
	
	//Private Constants
	
	//method names called from dialog
	private static final String closeAction = "closeShapePrinter";
	private static final String printAction = "printShape";

	private static final String shapesSelectedAction = "shapeSelectionChanged";
	private static final String pointsSelectedAction = "pointSelectionChanged";
	private static final String constantsSelectedAction = "splineConstantsChanged";
	
	private static final String setConstantExactAction = "setConstantModExact";
	private static final String setConstantMultAction = "setConstantModMult";
	private static final String setConstantAddAction = "setConstantModAdd";
	
	private static final String timeChangedAction = "timeModifierChanged";
	private static final String constantChangedAction = "constantModifierChanged";	
	
	private static final String loadSplineGenAction = "loadSplineGenSet";
	private static final String generateSplinesAction = "generateSplines";
	
	private static final String loadSplinesAction = "loadSplines";
	private static final String saveSplinesActions = "saveSplines";
	
	private static final String loadShapesAction = "loadShapeSet";	
	private static final String saveShapeSetAction = "saveShapeSet";
	
	
	//Private Member Variables
	private class HandlerArgs
	{
		public XDialog dialog = null;
		public Object eventObject = null;
		public String methodName = null;
		
		HandlerArgs()
		{
			
		}
		
		void setArgs(XDialog dialog, Object eventObject, String methodName)
		{
			this.dialog = dialog;
			this.eventObject = eventObject;
			this.methodName = methodName;
		}
	}
	
	private XDialog dialog;
	
	private String[] supportedActions = new String[] { closeAction, printAction, shapesSelectedAction, 
			pointsSelectedAction, constantsSelectedAction, setConstantExactAction, setConstantMultAction,
			setConstantAddAction, timeChangedAction, constantChangedAction, loadSplineGenAction,
			generateSplinesAction, loadSplinesAction, saveShapeSetAction, loadShapesAction, saveSplinesActions};
	
	private HashMap<String, Runnable> actionCallbackMap = new HashMap<String, Runnable>();
	private HashMap<String, HandlerArgs> callbackArgsMap = new HashMap<String, HandlerArgs>();

	
	//Public Methods
	public ShapePrinterDialog(XComponentContext xContext) 
	{
		this.dialog = DialogHelper.createDialog("ShapePrinterDialog.xdl", xContext, this);
		
		constructCallMap();
	}

	public void show() 
	{
		dialog.execute();
	}
	
	@Override
	public String[] getSupportedMethodNames() 
	{
		return supportedActions;
	}
	
	@Override
	public boolean callHandlerMethod(XDialog dialog, Object eventObject, String methodName) throws WrappedTargetException 
	{	
		if(actionCallbackMap.containsKey(methodName))
		{
			callbackArgsMap.get(methodName).setArgs(dialog, eventObject, methodName);
			actionCallbackMap.get(methodName).run();
			return true;
		}
		return false;
	}
	
	//Private methods
	
	private void constructCallMap()
	{
		actionCallbackMap.put(closeAction, this::onClose);
		actionCallbackMap.put(printAction, this::onPrintShape);
		actionCallbackMap.put(shapesSelectedAction, this::onShapeSelectionChanged);
		actionCallbackMap.put(pointsSelectedAction, this::onPointSelectionChanged);
		actionCallbackMap.put(constantsSelectedAction, this::onConstantsSelectionChanged);
		actionCallbackMap.put(setConstantExactAction, this::onConstantModExact);
		actionCallbackMap.put(setConstantMultAction, this::onConstantModMult);
		actionCallbackMap.put(setConstantAddAction, this::onConstantModAdd);
		actionCallbackMap.put(timeChangedAction, this::onLoadSplineGenerationSet);
		actionCallbackMap.put(constantChangedAction, this::onGenerateSplines);
		actionCallbackMap.put(loadSplineGenAction, this::onLoadSplineGenerationSet);
		actionCallbackMap.put(generateSplinesAction, this::onGenerateSplines);
		actionCallbackMap.put(loadSplinesAction, this::onLoadSplines);
		actionCallbackMap.put(saveSplinesActions, this::onSaveSplines);
		actionCallbackMap.put(loadShapesAction, this::onLoadShapes);
		actionCallbackMap.put(saveShapeSetAction, this::onSaveShapes);
		
		for(int i = 0; i < supportedActions.length; i++)
		{
			callbackArgsMap.put(supportedActions[i], new HandlerArgs());
		}
	}
	
	private void onClose() 
	{
		dialog.endExecute();
	}
	
	private void onPrintShape()
	{
		//TODO print shape to page following spline and shape settings
	}
	
	private void onShapeSelectionChanged()
	{
		//TODO adjust points, constants, and spline selection to match the shape selection
	}
	
	private void onPointSelectionChanged()
	{
		//TODO adjust constants and spline selection to match the points selected
	}
	
	private void onConstantsSelectionChanged()
	{
		//TODO adjust spline selection to match the constants selected
	}
	
	private void onConstantModExact()
	{
		//TODO adjust logic to modify splines with exact amount
	}
	
	private void onConstantModMult()
	{
		//TODO adjust logic to modify splines with a multiplicative amount
	}
	
	private void onConstantModAdd()
	{
		//TODO adjust logic to modify splines with an additive amount
	}
	
	private void onLoadSplineGenerationSet()
	{
		//TODO load in a set a shapes that can be used to generate splines
	}
	
	private void onGenerateSplines()
	{
		//TODO generate splines from a set of shapes
	}
	
	private void onLoadSplines()
	{
		//TODO load in a set of splines
	}
	
	private void onSaveSplines()
	{
		//TODO Save the current splines
	}
	
	private void onLoadShapes()
	{
		//TODO load in a set of shapes that will be adjusted by the splines
	}
	
	private void onSaveShapes()
	{
		//TODO save the current shape selection
	}
	
}
