package com.monkey.pickled.dialog;

import java.util.HashMap;

import com.monkey.pickled.helper.DialogHelper;
import com.monkey.pickled.src.InterpolateShapesImpl;
import com.monkey.pickled.src.ShapeDataSet;
import com.sun.star.awt.TextEvent;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogEventHandler;
import com.sun.star.awt.XNumericField;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XUnoControlDialog;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;


public class RenameShapeDialog implements XDialogEventHandler
{
	//Private Constants
	
	//method names called from dialog
	private static final String closeAction = "onCloseButtonPressed";
	private static final String setShapeNameAction = "onSetNameButtonPressed";
	private static final String nameChangeAction = "onNameFieldChange";
	
	private ShapeDataSet selectedShapes = null;
	private XTextComponent dialogNameField = null;
	
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
	
	private String[] supportedActions = new String[] { closeAction, setShapeNameAction, nameChangeAction };
	
	private HashMap<String, Runnable> actionCallbackMap = new HashMap<String, Runnable>();
	private HashMap<String, HandlerArgs> callbackArgsMap = new HashMap<String, HandlerArgs>();

	
	//Public Methods
	public RenameShapeDialog(XComponentContext xContext) 
	{
		this.dialog = DialogHelper.createDialog("RenameShapeDialog.xdl", xContext, this);
		
		constructCallMap();
		
		if(!initNameField())
		{
			System.out.println("Name field failed to initialize");
		}
	}

	public void show() 
	{
		dialog.execute();
	}
	
	public void setShapes(ShapeDataSet newSelectedShapes)
	{
		selectedShapes = newSelectedShapes;
		setNameFieldFromShapeSet();
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
		actionCallbackMap.put(setShapeNameAction, this::onSetShapeName);
		//actionCallbackMap.put(nameChangeAction, this::onChangeNameFieldValue);
		
		for(int i = 0; i < supportedActions.length; i++)
		{
			callbackArgsMap.put(supportedActions[i], new HandlerArgs());
		}
	}
	
	private boolean initNameField()
	{
		XUnoControlDialog controls = (XUnoControlDialog)UnoRuntime.queryInterface(XUnoControlDialog.class, dialog);
		if(controls == null)
		{
			return false;
		}
		
		XControl nameEntryControl = controls.getControl("NameEntryField");
		if(nameEntryControl == null)
		{
			return false;
		}
		
		XTextComponent nameField = (XTextComponent)UnoRuntime.queryInterface(XTextComponent.class, nameEntryControl);
		if (nameField == null)
		{
			return false;
		}
		
		dialogNameField = nameField;
		
		return true;
	}
	
	private void onClose()
	{
		dialog.endExecute();
	}
	
	private void onSetShapeName()
	{
		if(!setShapeSetNameFromField())
		{
			System.out.println("Failed to set shape names");
		}
	}
	
//	private void onChangeNameFieldValue()
//	{
//		
//	}
	
	private void setNameFieldVal(String val)
	{
		if(val != null)
		{
			dialogNameField.setText(val);
		}
	}
	
	private String getNameFieldVal()
	{
		if(dialogNameField == null)
		{
			return null;
		}
		
		return dialogNameField.getText();
	}
	
	private boolean setShapeSetNameFromField()
	{
		if(selectedShapes == null)
		{
			return false;
		}
		
		String newName = getNameFieldVal();
		if(newName == null)
		{
			return false;
		}
		
		selectedShapes.renameShapes(newName);
		
		return true;
	}
	
	private boolean setNameFieldFromShapeSet()
	{
		if(selectedShapes == null)
		{
			return false;
		}
		
		String name = selectedShapes.getShapeName();
		if(name != null)
		{
			setNameFieldVal(name);
		}
		
		return true;
	}
	
}

