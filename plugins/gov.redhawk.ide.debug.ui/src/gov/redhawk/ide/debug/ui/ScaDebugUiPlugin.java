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

package gov.redhawk.ide.debug.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import gov.redhawk.ide.debug.internal.ui.console.ConsoleExitStatus;

/**
 * The activator class controls the plug-in life cycle
 */
public class ScaDebugUiPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "gov.redhawk.ide.debug.ui"; //$NON-NLS-1$

	/**
	 * @since 2.1
	 */
	public static final String CHALKBOARD_EDITOR_URI_PATH = "/" + ScaDebugUiPlugin.PLUGIN_ID + "/data/LocalSca.sad.xml";

	// The shared instance
	private static ScaDebugUiPlugin plugin;

	private ConsoleExitStatus consoleWriter;

	/**
	 * The constructor
	 */
	public ScaDebugUiPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		ScaDebugUiPlugin.plugin = this;

		consoleWriter = new ConsoleExitStatus();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(consoleWriter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		ScaDebugUiPlugin.plugin = null;
		super.stop(context);

		if (consoleWriter != null) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(consoleWriter);
			consoleWriter = null;
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ScaDebugUiPlugin getDefault() {
		return ScaDebugUiPlugin.plugin;
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status
	 * status to log
	 */
	public static void log(final IStatus status) {
		ScaDebugUiPlugin.getDefault().getLog().log(status);
	}

	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message
	 * the error message to log
	 */
	public static void logErrorMessage(final String message) {
		ScaDebugUiPlugin.log(new Status(IStatus.ERROR, ScaDebugUiPlugin.getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e
	 * the exception to be logged
	 */
	public static void log(final Throwable e) {
		ScaDebugUiPlugin.log(new Status(IStatus.ERROR, ScaDebugUiPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (ScaDebugUiPlugin.getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return ScaDebugUiPlugin.PLUGIN_ID;
		}
		return ScaDebugUiPlugin.getDefault().getBundle().getSymbolicName();
	}

}
