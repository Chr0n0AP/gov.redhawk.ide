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
package gov.redhawk.ide.debug.internal.ui;

import gov.redhawk.ide.debug.LocalScaWaveform;
import gov.redhawk.ide.debug.ScaDebugPlugin;
import gov.redhawk.model.sca.ScaWaveform;
import gov.redhawk.sca.ui.ScaFileStoreEditorInput;
import gov.redhawk.sca.ui.editors.IScaContentDescriber;

import java.io.IOException;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.ui.IEditorInput;

public class LocalScaChalkboardContentDescriber implements IScaContentDescriber {

	public static IFileStore getFileStore(ScaWaveform waveform) throws CoreException {
		org.eclipse.emf.common.util.URI uri = waveform.getProfileURI();
		final URI resolvedURI;
		if (uri.isPlatform() || uri.isFile()) {
			uri = uri.trimQuery();
		}
		if (uri.isPlatform()) {
			resolvedURI = URI.create(CommonPlugin.resolve(uri).toString());
		} else {
			resolvedURI = URI.create(uri.toString());
		}
		return EFS.getStore(resolvedURI);
	}

	@Override
	public int describe(final Object contents) throws IOException {
		if (contents instanceof LocalScaWaveform) {
			return IScaContentDescriber.VALID;
		}
		return IScaContentDescriber.INVALID;
	}

	@Override
	public IEditorInput getEditorInput(final Object contents) {
		if (contents == ScaDebugPlugin.getInstance().getLocalSca().getSandboxWaveform()) {
			return LocalScaElementFactory.getLocalScaInput();
		} else if (contents instanceof LocalScaWaveform) {
			try {
				return new ScaFileStoreEditorInput((LocalScaWaveform) contents, LocalScaChalkboardContentDescriber.getFileStore((LocalScaWaveform) contents));
			} catch (CoreException e) {
				return null;
			}
		}

		return null;
	}
}
