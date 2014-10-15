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
package gov.redhawk.ide.swtbot;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class ComponentUtils {

	protected ComponentUtils() {
	}
	
	/**
	 * Launches the selected component in the SCA Explorer chalkboard
	 * @returns the SWTBotTreeItem for the component
	 */
	public static SWTBotTreeItem launchLocalComponent(final SWTWorkbenchBot bot, final String componentName, final String implementation) {
		SWTBotView explorerView = bot.viewById("gov.redhawk.ui.sca_explorer");
		explorerView.show();
		explorerView.setFocus();
		SWTBot viewBot = explorerView.bot();

		SWTBotTreeItem waveformNode = viewBot.tree().expandNode("Target SDR", "Components", componentName);
		waveformNode.contextMenu("Launch in Sandbox").menu(implementation.toLowerCase()).click();

		// Wait for the launched waveform to appear in the sandbox
		final SWTBotTreeItem chalkboard = viewBot.tree().expandNode("Sandbox", "Chalkboard");
		bot.waitUntil(new ICondition() {

			@Override
			public boolean test() throws Exception {
				for (SWTBotTreeItem item : chalkboard.getItems()) {
					if (item.getText().matches(componentName + ".*")) {
						return true;
					}
				}
				return false;
			}

			@Override
			public void init(SWTBot bot) {
			}

			@Override
			public String getFailureMessage() {
				return "Component: " + componentName + " [" + implementation + "] did not launch";
			}
		});

		// Return the treeitem for the running waveform
		for (SWTBotTreeItem item : chalkboard.getItems()) {
			if (item.getText().matches(componentName + ".*")) {
				return item;
			}
		}

		return null;
	}
	
}
