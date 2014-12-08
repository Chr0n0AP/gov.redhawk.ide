/*******************************************************************************
 * This file is protected by Copyright. 
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 *
 * This file is part of REDHAWK IDE.
 *
 * All rights reserved.  This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package gov.redhawk.ide.swtbot.diagram;

import gov.redhawk.ide.graphiti.ext.impl.RHContainerShapeImpl;
import gov.redhawk.ide.graphiti.ui.diagram.util.DUtil;
import gov.redhawk.ide.graphiti.ui.diagram.util.StyleUtil;
import gov.redhawk.ide.graphiti.sad.ext.impl.ComponentShapeImpl;
import gov.redhawk.ide.graphiti.sad.ui.diagram.util.SadStyleUtil;

import java.lang.reflect.Field;
import java.util.List;

import mil.jpeojtrs.sca.partitioning.FindByStub;
import mil.jpeojtrs.sca.partitioning.ProvidesPortStub;
import mil.jpeojtrs.sca.partitioning.UsesPortStub;
import mil.jpeojtrs.sca.sad.HostCollocation;
import mil.jpeojtrs.sca.sad.Port;
import mil.jpeojtrs.sca.sad.SadComponentInstantiation;

import org.eclipse.graphiti.mm.PropertyContainer;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefConnectionEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefFigureCanvas;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefViewer;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.junit.Assert;

@SuppressWarnings("restriction")
public class DiagramTestUtils { // SUPPRESS CHECKSTYLE INLINE - this utility method is intended to be public

	protected DiagramTestUtils() {

	}

	public static final String OVERVIEW_TAB = "Overview", PROPERTIES_TAB = "Properties", DIAGRAM_TAB = "Diagram", XML_TAB = ".sad.xml";

	/**
	 * Deletes the provided part from the diagram. Part must have a context menu option for "Delete"
	 * @param editor - SWTBotGefEditor
	 * @param part - part to be delete from diagram
	 */
	public static void deleteFromDiagram(SWTBotGefEditor editor, SWTBotGefEditPart part) {
		part.select();
		editor.clickContextMenu("Delete");
	}

	public static void releaseFromChalkboard(SWTBotGefEditor editor, SWTBotGefEditPart part) {
		part.select();
		editor.clickContextMenu("Release");
	}

	public static void terminateFromChalkboard(SWTBotGefEditor editor, SWTBotGefEditPart part) {
		part.select();
		editor.clickContextMenu("Terminate");
	}

	/**
	 * Drag a component onto the SAD diagram editor from the Palette.
	 * Position is determined relative to the last item dropped on the diagram.
	 * @param editor - SWTBotGefEditor
	 * @param componentName - Component to grab from palette
	 * @param xTargetPosition - x coordinate for drop location
	 * @param yTargetPosition - y coordinate for drop location
	 */
	public static void dragFromPaletteToDiagram(SWTBotGefEditor editor, String componentName, int xTargetPosition, int yTargetPosition) {
		String[] impls = { " (python)", " (cpp)", " (java)", "" };
		for (int i = 0; i < impls.length; i++) {
			try {
				editor.activateTool(componentName + impls[i]);
				break;
			} catch (WidgetNotFoundException e) {
				if (i == impls.length - 1) {
					throw e;
				} else {
					continue;
				}
			}
		}
		editor.drag(xTargetPosition, yTargetPosition, xTargetPosition, yTargetPosition);
	}

	/**
	 * Drag a component onto the SAD diagram editor from the Target SDR.
	 * @param gefBot
	 * @param editor
	 * @param componentName
	 */
	public static void dragFromTargetSDRToDiagram(SWTGefBot gefBot, SWTBotGefEditor editor, String componentName) {
		SWTBotView scaExplorerView = gefBot.viewByTitle("SCA Explorer");
		SWTBotTree scaTree = scaExplorerView.bot().tree();
		SWTBotTreeItem hardLimitItem = scaTree.expandNode("Target SDR", "Components", componentName);

		SWTBotGefViewer viewer = editor.getSWTBotGefViewer();
		SWTBotGefFigureCanvas canvas = null;

		for (Field f : viewer.getClass().getDeclaredFields()) {
			if ("canvas".equals(f.getName())) {
				f.setAccessible(true);
				try {
					canvas = (SWTBotGefFigureCanvas) f.get(viewer);
				} catch (IllegalArgumentException e) {
					throw new IllegalStateException(e);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			}
		}

		Assert.assertNotNull(canvas);
		hardLimitItem.dragAndDrop(canvas);
	}

	/**
	 * Drag a HostCollocation onto the SAD diagram editor
	 */
	public static void dragHostCollocationToDiagram(SWTGefBot gefBot, SWTBotGefEditor editor, String hostCoName) {
		dragFromPaletteToDiagram(editor, "Host Collocation", 0, 0);
		SWTBotShell hostCoShell = gefBot.shell("New Host Collocation");
		hostCoShell.setFocus();
		SWTBotText textField = gefBot.textWithLabel("Host Collocation:");
		textField.setFocus();
		textField.typeText(hostCoName);
		gefBot.button("OK").click();
		editor.setFocus();
	}

	/**
	 * 
	 * @param editor - SWTBotGefEditor
	 * @param usesEditPart - SWTBotGefEditPart for the uses/source port
	 * @param providesEditPart - SWTBotGefEditPart for the provides/target port
	 */
	public static void drawConnectionBetweenPorts(SWTBotGefEditor editor, SWTBotGefEditPart usesEditPart, SWTBotGefEditPart providesEditPart) {
		editor.activateTool("Connection");
		getDiagramPortAnchor(usesEditPart).click();
		getDiagramPortAnchor(providesEditPart).click();
	}

	/**
	 * Utility method to extract business object from a component in the Graphiti diagram.
	 * Returns null if object not found
	 * @param editor
	 * @param componentName
	 * @return
	 */
	public static SadComponentInstantiation getComponentObject(SWTBotGefEditor editor, String componentName) {

		ComponentShapeImpl componentShape = getComponentShape(editor, componentName);
		if (componentShape == null) {
			return null;
		}
		SadComponentInstantiation businessObject = (SadComponentInstantiation) DUtil.getBusinessObject(componentShape);
		return businessObject;
	}

	/**
	 * Utility method to extract ComponentShape from the Graphiti diagram with the provided componentName.
	 * Returns null if object not found
	 * @param editor
	 * @param componentName
	 * @return
	 */
	public static ComponentShapeImpl getComponentShape(SWTBotGefEditor editor, String componentName) {

		SWTBotGefEditPart swtBotGefEditPart = editor.getEditPart(componentName);
		if (swtBotGefEditPart == null) {
			return null;
		}
		return (ComponentShapeImpl) swtBotGefEditPart.part().getModel();
	}

	/**
	 * Utility method to extract business object from a findby in the Graphiti diagram.
	 * Returns null if object not found
	 * @param editor
	 * @param findByName
	 * @return
	 */
	public static FindByStub getFindByObject(SWTBotGefEditor editor, String findByName) {

		RHContainerShapeImpl findByShape = getFindByShape(editor, findByName);
		if (findByShape == null) {
			return null;
		}
		FindByStub businessObject = (FindByStub) DUtil.getBusinessObject(findByShape);
		return businessObject;
	}

	/**
	 * Utility method to extract RHContainerShapeImpl from the Graphiti diagram with the provided findByName.
	 * Returns null if object not found
	 * @param editor
	 * @param findByName
	 * @return
	 */
	public static RHContainerShapeImpl getFindByShape(SWTBotGefEditor editor, String findByName) {

		SWTBotGefEditPart swtBotGefEditPart = editor.getEditPart(findByName);
		if (swtBotGefEditPart == null) {
			return null;
		}
		return (RHContainerShapeImpl) swtBotGefEditPart.part().getModel();
	}

	/**
	 * Utility method to extract business object from a host collocation in the Graphiti diagram.
	 * Returns null if object not found
	 * @param editor
	 * @param name
	 * @return
	 */
	public static HostCollocation getHostCollocationObject(SWTBotGefEditor editor, String name) {

		ContainerShape containerShape = getHostCollocationShape(editor, name);
		if (containerShape == null) {
			return null;
		}
		HostCollocation businessObject = (HostCollocation) DUtil.getBusinessObject(containerShape);
		return businessObject;
	}

	/**
	 * Utility method to extract HostCollocationShape from the Graphiti diagram with the provided name.
	 * Returns null if object not found
	 * @param editor
	 * @param name
	 * @return
	 */
	public static ContainerShape getHostCollocationShape(SWTBotGefEditor editor, String name) {

		SWTBotGefEditPart swtBotGefEditPart = editor.getEditPart(name);
		if (swtBotGefEditPart == null) {
			return null;
		}
		return (ContainerShape) swtBotGefEditPart.part().getModel();
	}

	/**
	 * Return true if child shape exists within ContainerShape. Grandchildren, Great grandchildren included..
	 * @param containerShape
	 * @param childShape
	 * @return
	 */
	public static boolean childShapeExists(ContainerShape containerShape, Shape childShape) {
		List<Shape> shapes = DUtil.collectShapeChildren(containerShape);
		for (Shape s : shapes) {
			if (childShape.equals(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Drills down into the ports GEF children to return the anchor point
	 * Primarily uses for making connections. Makes assumptions as to how deep the anchor is.
	 * TODO: Figure out how to grab the anchor without assuming depth, maybe a recursive loop and type check?
	 * @param portEditPart - The SWTBotGefEditPart of the port you are trying to get the anchor for
	 * @return
	 */
	public static SWTBotGefEditPart getDiagramPortAnchor(SWTBotGefEditPart portEditPart) {
		return portEditPart.children().get(0).children().get(0);
	}

	/**
	 * Plot port data on provided Port Anchor
	 * If there is only one port you leave portName null
	 * @param portEditPart - The SWTBotGefEditPart of the port you are trying to get the anchor for
	 * @return
	 */
	public static void plotPortDataOnComponentPort(SWTBotGefEditor editor, String componentName, String portName) {
		final SWTBotGefEditPart usesPort = getDiagramUsesPort(editor, componentName, portName);
		final SWTBotGefEditPart usesPortAnchor = getDiagramPortAnchor(usesPort);
		usesPortAnchor.select();
		editor.clickContextMenu("Plot Port Data");
	}

	/**
	 * Play port data on provided Port Anchor
	 * If there is only one port you leave portName null
	 * @param portEditPart - The SWTBotGefEditPart of the port you are trying to get the anchor for
	 * @return
	 */
	public static void playPortDataOnComponentPort(SWTBotGefEditor editor, String componentName, String portName) {
		final SWTBotGefEditPart usesPort = getDiagramUsesPort(editor, componentName, portName);
		final SWTBotGefEditPart usesPortAnchor = getDiagramPortAnchor(usesPort);
		usesPortAnchor.select();
		editor.clickContextMenu("Play Port");
	}

	/**
	 * Display SRI data on provided Port Anchor
	 * If there is only one port you leave portName null
	 * @param portEditPart - The SWTBotGefEditPart of the port you are trying to get the anchor for
	 * @return
	 */
	public static void displaySRIDataOnComponentPort(SWTBotGefEditor editor, String componentName, String portName) {
		final SWTBotGefEditPart usesPort = getDiagramUsesPort(editor, componentName, portName);
		final SWTBotGefEditPart usesPortAnchor = getDiagramPortAnchor(usesPort);
		usesPortAnchor.select();
		editor.clickContextMenu("Display SRI");
	}

	/**
	 * Display DataList View on provided Port Anchor
	 * If there is only one port you leave portName null
	 * @param portEditPart - The SWTBotGefEditPart of the port you are trying to get the anchor for
	 * @return
	 */
	public static void displayDataListViewOnComponentPort(SWTBotGefEditor editor, String componentName, String portName) {
		final SWTBotGefEditPart usesPort = getDiagramUsesPort(editor, componentName, portName);
		final SWTBotGefEditPart usesPortAnchor = getDiagramPortAnchor(usesPort);
		usesPortAnchor.select();
		editor.clickContextMenu("Data List");
	}

	/**
	 * Display Snapshot View on provided Port Anchor
	 * If there is only one port you leave portName null
	 * @param portEditPart - The SWTBotGefEditPart of the port you are trying to get the anchor for
	 * @return
	 */
	public static void displaySnapshotDialogOnComponentPort(SWTBotGefEditor editor, String componentName, String portName) {
		final SWTBotGefEditPart usesPort = getDiagramUsesPort(editor, componentName, portName);
		final SWTBotGefEditPart usesPortAnchor = getDiagramPortAnchor(usesPort);
		usesPortAnchor.select();
		editor.clickContextMenu("Snapshot");
	}

	/**
	 * Display Port Monitor View on provided Port Anchor
	 * If there is only one port you leave portName null
	 * @param portEditPart - The SWTBotGefEditPart of the port you are trying to get the anchor for
	 * @return
	 */
	public static void displayPortMonitorViewOnComponentPort(SWTBotGefEditor editor, String componentName, String portName) {
		final SWTBotGefEditPart usesPort = getDiagramUsesPort(editor, componentName, portName);
		final SWTBotGefEditPart usesPortAnchor = getDiagramPortAnchor(usesPort);
		usesPortAnchor.select();
		editor.clickContextMenu("Monitor Ports");
	}

	/**
	 * 
	 * @param editor - SWTBotGefEditor
	 * @param componentName - Component being searched
	 * @return Returns SWTBotGefEditPart for the first provides port found in the component, or null if none found
	 */
	public static SWTBotGefEditPart getDiagramProvidesPort(SWTBotGefEditor editor, String componentName) {
		return getDiagramProvidesPort(editor, componentName, null);
	}

	/**
	 * 
	 * @param editor - SWTBotGefEditor
	 * @param componentName - Component being searched
	 * @param portName - Name of port being searched for
	 * @return Returns SWTBotGefEditPart for the specified provides port, or null if none found
	 */
	public static SWTBotGefEditPart getDiagramProvidesPort(SWTBotGefEditor editor, String componentName, String portName) {
		SWTBotGefEditPart componentEditPart = editor.getEditPart(componentName);

		for (SWTBotGefEditPart child : componentEditPart.children()) {
			ContainerShape containerShape = (ContainerShape) child.part().getModel();
			Object bo = DUtil.getBusinessObject(containerShape);

			// Only return objects of type ProvidesPortStub
			if (bo == null || !(bo instanceof ProvidesPortStub)) {
				continue;
			}

			List<SWTBotGefEditPart> providesPortsEditParts = child.children();
			for (SWTBotGefEditPart portEditPart : providesPortsEditParts) {
				// If no port name was supplied, return edit part for first provides port
				if (portName == null) {
					return portEditPart;
				}

				// Other wise, check for a matching port name
				ProvidesPortStub portStub = (ProvidesPortStub) DUtil.getBusinessObject((ContainerShape) portEditPart.part().getModel());
				if (portName != null && portName.equals(portStub.getName())) {
					return portEditPart;
				}
			}
		}
		// If you get here, no matching provides port was found
		return null;
	}

	/**
	 * 
	 * @param editor - SWTBotGefEditor
	 * @param componentName - Component being searched
	 * @return Returns SWTBotGefEditPart for the first uses port found in the component, or null if none found
	 */
	public static SWTBotGefEditPart getDiagramUsesPort(SWTBotGefEditor editor, String componentName) {
		return getDiagramUsesPort(editor, componentName, null);
	}

	/**
	 * 
	 * @param editor - SWTBotGefEditor
	 * @param componentName - Component being searched
	 * @param portName - Name of port being searched for
	 * @return Returns SWTBotGefEditPart for the specified uses port, or null if none found
	 */
	public static SWTBotGefEditPart getDiagramUsesPort(SWTBotGefEditor editor, String componentName, String portName) {
		SWTBotGefEditPart componentEditPart = editor.getEditPart(componentName);
		Assert.assertNotNull(componentEditPart);
		for (SWTBotGefEditPart child : componentEditPart.children()) {
			ContainerShape containerShape = (ContainerShape) child.part().getModel();
			Object bo = DUtil.getBusinessObject(containerShape);

			// Only return objects of type UsesPortStub
			if (bo == null || !(bo instanceof UsesPortStub)) {
				continue;
			}

			List<SWTBotGefEditPart> usesPortsEditParts = child.children();
			for (SWTBotGefEditPart portEditPart : usesPortsEditParts) {
				// If no port name was supplied, return edit part for first provides port
				if (portName == null) {
					return portEditPart;
				}

				// Other wise, check for a matching port name
				UsesPortStub portStub = (UsesPortStub) DUtil.getBusinessObject((ContainerShape) portEditPart.part().getModel());
				if (portName != null && portName.equals(portStub.getName())) {
					return portEditPart;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param editor - SWTBotGefEditor
	 * @param portEditPart - source port edit part
	 * @return - Returns of list of SWTBotGefConnectionEditParts that includes any connection where the provided port is
	 * the source
	 */
	public static List<SWTBotGefConnectionEditPart> getSourceConnectionsFromPort(SWTBotGefEditor editor, SWTBotGefEditPart portEditPart) {
		SWTBotGefEditPart anchor = getDiagramPortAnchor(portEditPart);
		return anchor.sourceConnections();
	}

	/**
	 * 
	 * @param editor - SWTBotGefEditor
	 * @param portEditPart - target port edit part
	 * @return - Returns of list of SWTBotGefConnectionEditParts that includes any connection where the provided port is
	 * the target
	 */
	public static List<SWTBotGefConnectionEditPart> getTargetConnectionsFromPort(SWTBotGefEditor editor, SWTBotGefEditPart portEditPart) {
		SWTBotGefEditPart anchor = getDiagramPortAnchor(portEditPart);
		return anchor.targetConnections();
	}

	/**
	 * Opens the given tab with the given name within the waveform editor.
	 * @param editor - the editor within which to open the tab
	 * @param tabName - name of the tab to be opened
	 */
	public static void openTabInEditor(SWTBotGefEditor editor, String tabName) {
		editor.bot().cTabItem(tabName).activate();
	}

	/**
	 * Checks sad.xml for component instantiation code
	 * @param componentShape
	 * @return
	 */
	public static String regexStringForSadComponent(ComponentShapeImpl componentShape) {
		Object bo = DUtil.getBusinessObject(componentShape);
		SadComponentInstantiation ci = (SadComponentInstantiation) bo;
		String componentinstantiation = "<componentinstantiation id=\"" + ci.getUsageName() + "\""
			+ ((ci.getStartOrder() != null) ? " startorder=\"" + ci.getStartOrder() + "\">" : ">");
		String usagename = "<usagename>" + ci.getUsageName() + "</usagename>";
		String namingservice = "<namingservice name=\"" + ci.getUsageName() + "\"/>";

		return "(?s).*" + componentinstantiation + ".*" + usagename + ".*" + namingservice + ".*";

	}

	/**
	 * Checks sad.xml for component property code
	 * @param componentShape
	 * @param propertyname
	 * @param value
	 * @return
	 */
	public static String regexStringForSadProperty(ComponentShapeImpl componentShape, String propertyname, String value) {
		return "(?s).*<componentproperties>.*<simpleref refid=\"" + propertyname + "\" value=\"" + value + "\"/>.*</componentproperties>.*";
	}

	/**
	 * Maximizes active window
	 */
	public static void maximizeActiveWindow(SWTGefBot gefBot) {
		gefBot.menu(IDEWorkbenchMessages.Workbench_window).menu("Maximize Active View or Editor").click();
	}

	/**
	 * Return true if Shape's x/y coordinates match arguments
	 * @param shape
	 * @param x
	 * @param y
	 * @return
	 */
	public static boolean verifyShapeLocation(Shape shape, int x, int y) {
		if (shape.getGraphicsAlgorithm().getX() == x && shape.getGraphicsAlgorithm().getY() == y) {
			return true;
		}
		return false;
	}

	/**
	 * Waits until Connection displays in Chalkboard Diagram
	 * @param componentName
	 */
	public static void waitUntilConnectionDisplaysInDiagram(SWTWorkbenchBot bot, SWTBotGefEditor editor, final String targetComponentName) {
		SWTBotGefEditPart targetComponentEditPart = editor.getEditPart(targetComponentName);
		final ContainerShape targetContainerShape = (ContainerShape) targetComponentEditPart.part().getModel();

		bot.waitUntil(new DefaultCondition() {
			@Override
			public String getFailureMessage() {
				return targetComponentName + " Target Component's connection did not load into Chalkboard Diagram";
			}

			@Override
			public boolean test() throws Exception {
				if (DUtil.getIncomingConnectionsContainedInContainerShape(targetContainerShape).size() > 0) {
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * Waits until Connection disappears in Chalkboard Diagram
	 * @param componentName
	 */
	public static void waitUntilConnectionDisappearsInDiagram(SWTWorkbenchBot bot, SWTBotGefEditor editor, final String targetComponentName) {

		SWTBotGefEditPart targetComponentEditPart = editor.getEditPart(targetComponentName);
		final ContainerShape targetContainerShape = (ContainerShape) targetComponentEditPart.part().getModel();

		bot.waitUntil(new DefaultCondition() {
			@Override
			public String getFailureMessage() {
				return targetComponentName + " Target Component's connection did not disappear diagram";
			}

			@Override
			public boolean test() throws Exception {
				if (DUtil.getIncomingConnectionsContainedInContainerShape(targetContainerShape).size() < 1) {
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * Start component from Chalkboard Diagram
	 * @param componentName
	 */
	public static void startComponentFromDiagram(SWTBotGefEditor editor, String componentName) {
		editor.setFocus();
		SWTBotGefEditPart componentPart = editor.getEditPart(componentName);
		componentPart.select();
		editor.clickContextMenu("Start");
	}

	/**
	 * Stop component from Chalkboard Diagram
	 * @param componentName
	 */
	public static void stopComponentFromDiagram(SWTBotGefEditor editor, String componentName) {
		editor.setFocus();
		SWTBotGefEditPart componentPart = editor.getEditPart(componentName);
		componentPart.select();
		editor.clickContextMenu("Stop");
	}

	/**
	 * Waits until Component displays in Chalkboard Diagram
	 * @param componentName
	 */
	public static void waitUntilComponentDisappearsInChalkboardDiagram(SWTWorkbenchBot bot, final SWTBotGefEditor editor, final String componentName) {

		bot.waitUntil(new DefaultCondition() {
			@Override
			public String getFailureMessage() {
				return componentName + " Component did not disappear from Chalkboard Diagram";
			}

			@Override
			public boolean test() throws Exception {
				if (editor.getEditPart(componentName) == null) {
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * Waits until Component appears started in ChalkboardDiagram
	 * @param componentName
	 */
	public static void waitUntilComponentAppearsStartedInDiagram(SWTWorkbenchBot bot, final SWTBotGefEditor editor, final String componentName) {

		bot.waitUntil(new DefaultCondition() {
			@Override
			public String getFailureMessage() {
				return componentName + " Component did not appear started in Diagram";
			}

			@Override
			public boolean test() throws Exception {
				ComponentShapeImpl componentShape = (ComponentShapeImpl) editor.getEditPart(componentName).part().getModel();
				RoundedRectangle innerRoundedRectangle = (RoundedRectangle) componentShape.getInnerContainerShape().getGraphicsAlgorithm();
				Diagram diagram = DUtil.findDiagram(componentShape);
				return innerRoundedRectangle.getStyle().equals(SadStyleUtil.createStyleForComponentInnerStarted(diagram));
			}
		}, 10000);
	}

	/**
	 * Waits until Component appears stopped in ChalkboardDiagram
	 * @param componentName
	 */
	public static void waitUntilComponentAppearsStoppedInDiagram(SWTWorkbenchBot bot, final SWTBotGefEditor editor, final String componentName) {

		bot.waitUntil(new DefaultCondition() {
			@Override
			public String getFailureMessage() {
				return componentName + " Component did not appear stopped in Diagram";
			}

			@Override
			public boolean test() throws Exception {
				ComponentShapeImpl componentShape = (ComponentShapeImpl) editor.getEditPart(componentName).part().getModel();
				RoundedRectangle innerRoundedRectangle = (RoundedRectangle) componentShape.getInnerContainerShape().getGraphicsAlgorithm();
				Diagram diagram = DUtil.findDiagram(componentShape);
				return innerRoundedRectangle.getStyle().equals(StyleUtil.createStyleForComponentInner(diagram));
			}
		}, 10000);
	}

	/**
	 * Waits until Component displays in Chalkboard Diagram
	 * @param componentName
	 */
	public static void waitUntilComponentDisplaysInDiagram(SWTWorkbenchBot bot, final SWTBotGefEditor editor, final String componentName) {

		bot.waitUntil(new DefaultCondition() {
			@Override
			public String getFailureMessage() {
				return componentName + " Component did not appear in Diagram";
			}

			@Override
			public boolean test() throws Exception {
				if (editor.getEditPart(componentName) != null) {
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * Asserts external port
	 * @param external
	 * @return
	 */
	public static void assertExternalPort(SWTBotGefEditPart portEditPart, boolean external) {
		ContainerShape portContainerShape = (ContainerShape) portEditPart.part().getModel();
		for (PropertyContainer portChild : DUtil.collectPropertyContainerChildren(portContainerShape)) {
			if (DUtil.isPropertyElementType(portChild, RHContainerShapeImpl.GA_FIX_POINT_ANCHOR_RECTANGLE)) {
				Object obj = DUtil.getBusinessObject((PictogramElement) portChild.eContainer(), Port.class);
				if (external) {
					Assert.assertTrue("Not an external port", obj != null);
				} else {
					Assert.assertTrue("Port is external", obj == null);
				}
				break;
			}
		}
	}
}
