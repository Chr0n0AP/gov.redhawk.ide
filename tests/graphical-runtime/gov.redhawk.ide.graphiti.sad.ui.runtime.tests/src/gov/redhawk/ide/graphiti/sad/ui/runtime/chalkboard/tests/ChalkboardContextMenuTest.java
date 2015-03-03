/**
 * This file is protected by Copyright. 
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 * 
 * This file is part of REDHAWK IDE.
 * 
 * All rights reserved.  This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 *
 */
package gov.redhawk.ide.graphiti.sad.ui.runtime.chalkboard.tests;

import gov.redhawk.ide.swtbot.ViewUtils;
import gov.redhawk.ide.swtbot.diagram.DiagramTestUtils;
import gov.redhawk.ide.swtbot.scaExplorer.ScaExplorerTestUtils;
import gov.redhawk.logging.ui.LogLevels;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.Assert;
import org.junit.Test;

public class ChalkboardContextMenuTest extends AbstractGraphitiChalkboardTest {

	private SWTBotGefEditor editor;
	private static final String SIGGEN_1 = SIGGEN + "_1";

	/**
	 * IDE-661, IDE-662, IDE-663, IDE-664, IDE-665, IDE-666, IDE-667, IDE-1038, IDE-1065
	 * Test that context menu options appear in Graphiti during runtime,
	 * ensures that the proper views appear based on selection and that views are interactive
	 */
	@Test
	public void runtimeContextMenuTest() {
		editor = openChalkboardDiagram(gefBot);

		// Prepare Graphiti diagram
		DiagramTestUtils.addFromPaletteToDiagram(editor, SIGGEN, 0, 0);
		
		//wait for SIGGEN_1 to show up in Sca Explorer
		ScaExplorerTestUtils.waitUntilComponentDisplaysInScaExplorer(bot, CHALKBOARD_PARENT_PATH, CHALKBOARD, SIGGEN_1);

		// Start the component
		DiagramTestUtils.startComponentFromDiagram(editor, SIGGEN);
		ScaExplorerTestUtils.waitUntilComponentAppearsStartedInScaExplorer(bot, CHALKBOARD_PARENT_PATH, CHALKBOARD, SIGGEN_1);

		// Check that we can't undo certain actions
		Assert.assertFalse("IDE-1038 No Undo Start Command context menu item", DiagramTestUtils.hasContentMenuItem(editor, SIGGEN, "Undo Start Command"));
		Assert.assertFalse("IDE-1065 No Undo Do Command context menu item", DiagramTestUtils.hasContentMenuItem(editor, SIGGEN, "Undo Do Command"));

		// Test Log Levels
		DiagramTestUtils.changeLogLevelFromDiagram(editor, SIGGEN, LogLevels.TRACE);
		DiagramTestUtils.confirmLogLevelFromDiagram(editor, SIGGEN, LogLevels.TRACE);

		DiagramTestUtils.changeLogLevelFromDiagram(editor, SIGGEN, LogLevels.FATAL);
		DiagramTestUtils.confirmLogLevelFromDiagram(editor, SIGGEN, LogLevels.FATAL);

		//plot port data for SIGGEN
		editor.setFocus();
		DiagramTestUtils.plotPortDataOnComponentPort(editor, SIGGEN, null);
		//close plot view
		SWTBotView plotView = ViewUtils.getPlotView(bot);
		plotView.close();

		// SRI view test
		DiagramTestUtils.displaySRIDataOnComponentPort(editor, SIGGEN, null);
		//verify sriView displayed
		ViewUtils.waitUntilSRIViewPopulates(bot);
		SWTBotView sriView = ViewUtils.getSRIView(bot);
		Assert.assertEquals("streamID property is missing for column 1", "streamID: ", sriView.bot().tree().cell(0, "Property: "));
		Assert.assertEquals("streamID property is wrong", SIGGEN + " Stream", sriView.bot().tree().cell(0, "Value: "));
		sriView.close();

		// Audio/Play port view test
		DiagramTestUtils.playPortDataOnComponentPort(editor, SIGGEN, null);
		//wait until audio view populates
		ViewUtils.waitUntilAudioViewPopulates(bot);
		//get audio view
		SWTBotView audioView = ViewUtils.getAudioView(bot);
		String item = audioView.bot().list().getItems()[0];
		Assert.assertTrue("SigGen not found in Audio Port Playback", item.matches(SIGGEN + ".*"));
		audioView.close();

		//open data list view
		DiagramTestUtils.displayDataListViewOnComponentPort(editor, SIGGEN, null);
		//verify data list view opens
		ViewUtils.waitUntilDataListViewDisplays(bot);
		//start acquire
		ViewUtils.startAquireOnDataListView(bot);
		//wait until view populates
		ViewUtils.waitUntilDataListViewPopulates(bot);
		//close data list view
		SWTBotView dataListView = ViewUtils.getDataListView(bot);
		dataListView.close();

		// Snapshot view test
		DiagramTestUtils.displaySnapshotDialogOnComponentPort(editor, SIGGEN, null);
		//wait until Snapshot dialog appears
		ViewUtils.waitUntilSnapshotDialogDisplays(bot);
		//get snapshot dialog
		SWTBotShell snapshotDialog = ViewUtils.getSnapshotDialog(bot);
		Assert.assertNotNull(snapshotDialog);
		snapshotDialog.close();

		// Monitor ports test
		DiagramTestUtils.displayPortMonitorViewOnComponentPort(editor, SIGGEN, null);
		//wait until port monitor view appears
		ViewUtils.waitUntilPortMonitorViewPopulates(bot, SIGGEN);
		//close PortMonitor View
		SWTBotView monitorView = ViewUtils.getPortMonitorView(bot);
		monitorView.close();

		//stop component
		DiagramTestUtils.stopComponentFromDiagram(editor, SIGGEN);
		ScaExplorerTestUtils.waitUntilComponentAppearsStoppedInScaExplorer(bot, CHALKBOARD_PARENT_PATH, CHALKBOARD, SIGGEN_1);
		
		Assert.assertFalse("IDE-1038 No Undo Stop Command context menu item", DiagramTestUtils.hasContentMenuItem(editor, SIGGEN, "Undo Stop Command"));
		Assert.assertFalse("IDE-1065 No Undo Do Command context menu item", DiagramTestUtils.hasContentMenuItem(editor, SIGGEN, "Undo Do Command"));
	}

	/**
	 * IDE-326
	 * Test that certain context menu option don't appear in Graphiti during runtime,
	 */
	@Test
	public void removeDevelopmentContextOptionsTest() {
		editor = openChalkboardDiagram(gefBot);

		// Prepare Graphiti diagram
		DiagramTestUtils.addFromPaletteToDiagram(editor, SIGGEN, 0, 0);
		ScaExplorerTestUtils.waitUntilComponentDisplaysInScaExplorer(bot, CHALKBOARD_PARENT_PATH, CHALKBOARD, SIGGEN + "_1");

		// Make sure start order and assembly controller context options don't exist
		editor.getEditPart(SIGGEN).select();
		String[] removedContextOptions = { "Set As Assembly Controller", "Move Start Order Earlier", "Move Start Order Later" };
		for (String contextOption : removedContextOptions) {
			Assert.assertFalse("IDE-326 No context menu item " + contextOption, DiagramTestUtils.hasContentMenuItem(editor, SIGGEN, contextOption));
		}
	}
	
}