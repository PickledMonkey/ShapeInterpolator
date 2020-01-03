package com.monkey.pickled.dialog;

import java.util.HashMap;

import com.monkey.pickled.helper.DialogHelper;
import com.monkey.pickled.helper.PageHelper;
import com.monkey.pickled.helper.ShapeHelper;
import com.monkey.pickled.src.InterpolateShapesImpl;
import com.sun.star.awt.TextEvent;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogEventHandler;
import com.sun.star.awt.XNumericField;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;

public class SplineDialog implements XDialogEventHandler
{
	//Private Constants
	
	//method names called from dialog
	private static final String closeAction = "onCloseButtonPressed";
	private static final String createShapesAction = "onCreateShapesButtonPressed";
	private static final String changeSplineValueAction = "onSplineValFieldChange";
	private static final String saveShapesAction = "onSaveShapesButtonPressed";
	
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
	
	private String[] supportedActions = new String[] { closeAction, createShapesAction, changeSplineValueAction, saveShapesAction };
	
	private HashMap<String, Runnable> actionCallbackMap = new HashMap<String, Runnable>();
	private HashMap<String, HandlerArgs> callbackArgsMap = new HashMap<String, HandlerArgs>();

	
	//Public Methods
	public SplineDialog(XComponentContext xContext) 
	{
		this.dialog = DialogHelper.createDialog("SplineDialog.xdl", xContext, this);
		
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
		actionCallbackMap.put(createShapesAction, this::onCreateShapes);
		actionCallbackMap.put(changeSplineValueAction, this::onChangeSplineValue);
		actionCallbackMap.put(saveShapesAction, this::onSaveShapes);
		
		for(int i = 0; i < supportedActions.length; i++)
		{
			callbackArgsMap.put(supportedActions[i], new HandlerArgs());
		}
	}
	
	private void onClose()
	{
		dialog.endExecute();
	}
	
	private void onCreateShapes()
	{
		InterpolateShapesImpl.createInterpolatedShapes();
	}
	
	private void onChangeSplineValue()
	{
		HandlerArgs args = callbackArgsMap.get(changeSplineValueAction);
		
		TextEvent eventData = (TextEvent) args.eventObject;
		XInterface eventSource = (XInterface) eventData.Source;
		
		XNumericField numField = UnoRuntime.queryInterface(XNumericField.class, eventSource);
		if(numField == null)
		{
			System.out.println("Not a numeric field");
			return;
		}
		
		double splineValue = numField.getValue();
		InterpolateShapesImpl.interpolateShapes(splineValue);
	}
	
	private void onSaveShapes()
	{
    	try {
			PageHelper.duplicatePageByIndex(ShapeHelper.xDrawDoc, 0);
		} catch (java.lang.Exception e) {
			System.out.println("Unable to duplicate the draw page");
			e.printStackTrace();
		}
	}
}
