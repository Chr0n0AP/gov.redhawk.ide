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
package gov.redhawk.ide.codegen.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import mil.jpeojtrs.sca.spd.Implementation;

/**
 * @since 2.1
 */
public class DefaultGeneratorDisplayFactory implements ICodegenLanguageDisplayFactory {

	@Override
	public ICodegenComposite createComposite(final Composite parent, final int style, final FormToolkit toolkit) {
		return new DefaultGeneratorPropertiesComposite(parent, style, toolkit);
	}

	@Override
	public ICodegenComposite createComposite(Implementation impl, String codegenId, Composite parent, int style, FormToolkit toolkit) {
		return new DefaultGeneratorPropertiesComposite(impl, codegenId, parent, style, toolkit);
	}

	@Override
	public ICodegenWizardPage createPage() {
		return new DefaultGeneratorPropertiesWizardPage();
	}

}
