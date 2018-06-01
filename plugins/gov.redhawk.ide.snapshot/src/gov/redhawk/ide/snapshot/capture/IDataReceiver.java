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
package gov.redhawk.ide.snapshot.capture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import gov.redhawk.ide.snapshot.writer.IDataWriter;

/**
 * 
 */
public interface IDataReceiver {

	void setDataWriter(IDataWriter writer);
	IDataWriter getDataWriter();
	
	IStatus run(IProgressMonitor monitor); 
}
