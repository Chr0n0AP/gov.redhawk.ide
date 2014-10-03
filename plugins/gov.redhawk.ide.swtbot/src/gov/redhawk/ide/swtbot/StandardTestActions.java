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
package gov.redhawk.ide.swtbot;

import gov.redhawk.ide.sdr.SdrRoot;
import gov.redhawk.ide.sdr.ui.SdrUiPlugin;
import gov.redhawk.ide.sdr.ui.preferences.SdrUiPreferenceConstants;
import gov.redhawk.ide.swtbot.internal.ProjectRecord;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.keyboard.Keyboard;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.junit.Assert;
import org.osgi.framework.Bundle;
import org.python.pydev.ui.pythonpathconf.AutoConfigMaker;
import org.python.pydev.ui.pythonpathconf.IInterpreterProviderFactory.InterpreterType;

public final class StandardTestActions {

	private static boolean pydevSetup = false;

	private StandardTestActions() {

	}

	public static void configurePyDev() {
		if (pydevSetup) {
			return;
		}
		boolean advanced = false;
		InterpreterType interpreterType = InterpreterType.PYTHON;
		final AutoConfigMaker a = new AutoConfigMaker(interpreterType, advanced, null, null);
		final Object block = new Object();
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				a.autoConfigSingleApply(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						super.done(event);
						synchronized (block) {
							pydevSetup = true;
							block.notifyAll();
						}
					}
				});
			}

		});

		synchronized (block) {
			while (!pydevSetup) {
				try {
					block.wait();
				} catch (InterruptedException e) {
					// PASS
				}
			}
		}
	}

	public static void closeIntro() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				final IIntroManager introManager = PlatformUI.getWorkbench().getIntroManager();
				if (introManager != null) {
					IIntroPart part = introManager.getIntro();
					if (part != null) {
						introManager.closeIntro(part);
					}
				}

				Display current = Display.getCurrent();
				if (current != null) {
					Shell shell = current.getActiveShell();
					shell.setMaximized(true);
				}
			}
		});
	}

	public static void switchToScaPerspective(SWTWorkbenchBot bot) {
		SWTBotPerspective perspective = bot.perspectiveById("gov.redhawk.ide.ui.perspectives.sca");
		perspective.activate();
		bot.resetActivePerspective();
		bot.sleep(100);
	}

	public static void assertNoOpenDialogs() {
		final boolean[] badDialogs = { false };
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				Shell s = Display.getCurrent().getActiveShell();
				if (s == PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()) {
					badDialogs[0] = false;
				} else {
					if (s != null) {
						badDialogs[0] = true;
					}
				}
			}

		});

		if (badDialogs[0]) {
			Assert.fail("Invalid dialogs left open at end of test.");
		}
	}

	public static void clearWorkspace() throws CoreException {
		for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			p.delete(true, true, null);
		}
	}

	/**
	 * <b>NOTE</b>: It is recommended you override the environment variables in the pom.xml for tests instead of
	 * changing the SDR root.
	 * @param pluginId Plugin that contains the SDR
	 * @param path Relative path within the plugin for the SDR, usually 'sdr'
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void setTargetSdr(String pluginId, String path) throws IOException, URISyntaxException {
		// XXX NOTE: SDR is loaded from the environment variables in the pom.xml not need to change it here.
		final URL url = FileLocator.find(Platform.getBundle(pluginId), new Path(path), null);
		final SdrRoot root = SdrUiPlugin.getDefault().getTargetSdrRoot();
		root.load(null);
		final URL fileURL = FileLocator.toFileURL(url);
		SdrUiPlugin.getDefault().getPreferenceStore().setValue(SdrUiPreferenceConstants.SCA_LOCAL_SDR_PATH_PREFERENCE,
			new File(fileURL.toURI()).getAbsolutePath());
		root.reload(null);
		Assert.assertTrue("SDR failed to load: " + root.getLoadStatus(), root.getLoadStatus().isOK());
	}

	public static void importProject(Bundle bundle, IPath path, Map< ? , ? > override) throws CoreException {
		if (!path.lastSegment().equals(".project")) {
			path = path.append(".project");
		}
		URL bundleUrl = FileLocator.find(bundle, path, override);

		URL fileUrl;
		try {
			fileUrl = FileLocator.toFileURL(bundleUrl);
		} catch (IOException e) {
			throw new CoreException(new Status(Status.ERROR, SwtBotActivator.PLUGIN_ID, "Failed to covert project path to file system path: " + path, e));
		}
		File bundleFile = new File(fileUrl.getPath());
		ProjectRecord record = new ProjectRecord(bundleFile);
		try {
			createExistingProject(record);
		} catch (InvocationTargetException | InterruptedException e) {
			throw new CoreException(new Status(Status.ERROR, SwtBotActivator.PLUGIN_ID, "Failed to import existing project: " + path, e));
		}
	}

	/**
	 * Create the project described in record. If it is successful return true.
	 * 
	 * @param record
	 * @return boolean <code>true</code> if successful
	 * @throws InterruptedException
	 */
	private static boolean createExistingProject(final ProjectRecord record) throws InvocationTargetException, InterruptedException {
		String projectName = record.getProjectName();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);
		if (record.description == null) {
			// error case
			record.description = workspace.newProjectDescription(projectName);
			IPath locationPath = new Path(record.projectSystemFile.getAbsolutePath());

			// If it is under the root use the default location
			if (Platform.getLocation().isPrefixOf(locationPath)) {
				record.description.setLocation(null);
			} else {
				record.description.setLocation(locationPath);
			}
		} else {
			record.description.setName(projectName);
		}

		// import from file system
		File importSource = null;
		// import project from location copying files - use default project
		// location for this workspace
		URI locationURI = record.description.getLocationURI();
		// if location is null, project already exists in this location or
		// some error condition occured.
		if (locationURI != null) {
			// validate the location of the project being copied
			IStatus result = ResourcesPlugin.getWorkspace().validateProjectLocationURI(project, locationURI);
			if (!result.isOK()) {
				throw new InvocationTargetException(new CoreException(result));
			}

			importSource = new File(locationURI);
			IProjectDescription desc = workspace.newProjectDescription(projectName);
			desc.setBuildSpec(record.description.getBuildSpec());
			desc.setComment(record.description.getComment());
			desc.setDynamicReferences(record.description.getDynamicReferences());
			desc.setNatureIds(record.description.getNatureIds());
			desc.setReferencedProjects(record.description.getReferencedProjects());
			record.description = desc;
		}

		try {
			project.create(record.description, new NullProgressMonitor());
			project.open(IResource.BACKGROUND_REFRESH, new NullProgressMonitor());
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}

		// import operation to import project files if copy checkbox is selected
		if (importSource != null) {
			List< ? > filesToImport = FileSystemStructureProvider.INSTANCE.getChildren(importSource);
			IOverwriteQuery overwrite = new IOverwriteQuery() {

				@Override
				public String queryOverwrite(String pathString) {
					return IOverwriteQuery.ALL;
				}
			};
			ImportOperation operation = new ImportOperation(project.getFullPath(), importSource, FileSystemStructureProvider.INSTANCE, overwrite, filesToImport);
			operation.setContext(null);
			operation.setOverwriteResources(true); // need to overwrite
			// .project, .classpath
			// files
			operation.setCreateContainerStructure(false);
			operation.run(new NullProgressMonitor());
			IStatus status = operation.getStatus();
			if (!status.isOK()) {
				throw new InvocationTargetException(new CoreException(status));
			}
		}

		return true;
	}
	
	public static void writeToCell(SWTBot bot, final SWTBotTreeItem item, final int column, final String text) {
		item.click(column);
		
		// Wait for cell editor to appear
		bot.sleep(500);
		
		Keyboard keyboard = KeyboardFactory.getSWTKeyboard();
		keyboard.typeText(text);
		keyboard.pressShortcut(Keystrokes.CR);
		
		// Wait for cell editor to close
		bot.sleep(100);
	}
	
	public static void cleanup(SWTWorkbenchBot bot) throws CoreException {
		if (bot == null) {
			bot = new SWTWorkbenchBot();
		}

		final boolean[] dialogsClosed = { false };
		while (!dialogsClosed[0]) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					Shell s = Display.getCurrent().getActiveShell();
					if (s == PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()) {
						dialogsClosed[0] = true;
					} else {
						if (s != null) {
							s.dispose();
						}
					}
				}

			});
		}

		bot.closeAllShells();

		bot.closeAllEditors();

		StandardTestActions.clearWorkspace();
	}
	
	public static void writeToCell(SWTBot bot, SWTBotTable table, final int row, final int column, final String text) {
		table.click(row, column);
		
		// Wait for cell editor to appear
		bot.sleep(500);
		
		Keyboard keyboard = KeyboardFactory.getSWTKeyboard();
		keyboard.typeText(text);
		keyboard.pressShortcut(Keystrokes.CR);
		
		// Wait for cell editor to close
		bot.sleep(100);
	}
	
	public static void assertFormValid(SWTBot bot, final FormPage page) {
		try {
			waitForValidationState(bot, page, IMessageProvider.NONE, IMessageProvider.INFORMATION, IMessageProvider.WARNING);
		} catch (TimeoutException e) {
			Assert.fail("Form should be valid");
		}
	}

	public static int getValidationState(FormPage page) {
		int messageType = page.getManagedForm().getForm().getMessageType();
		return messageType;
	}
	
	public static void waitForValidationState(SWTBot bot, final FormPage page, final int ... states) {
		bot.waitUntil(new ICondition() {

			@Override
			public boolean test() throws Exception {
				int current = getValidationState(page);
				for (int i : states) {
					if (i == current) {
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
				return "Failed waiting for validation state to change to: " + Arrays.toString(states);
			}
			
		}, 5000, 200);
	}
	
	public static void assertFormInvalid(SWTBot bot, final FormPage page) {
		try {
			waitForValidationState(bot, page, IMessageProvider.ERROR);
		} catch (TimeoutException e) {
			Assert.fail("Form should be valid");
		}
	}
}
