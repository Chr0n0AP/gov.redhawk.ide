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
package gov.redhawk.ide.graphiti.ui.diagram.util;

import java.util.Collection;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.graphiti.mm.StyleContainer;
import org.eclipse.graphiti.mm.algorithms.styles.Font;
import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
import org.eclipse.graphiti.mm.algorithms.styles.Style;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;
import org.eclipse.graphiti.util.PredefinedColoredAreas;
import org.eclipse.swt.graphics.Color;

import gov.redhawk.ide.graphiti.internal.ui.resource.StyleResourceFactory;
import gov.redhawk.sca.util.PluginUtil;

public class StyleUtil { // SUPPRESS CHECKSTYLE INLINE

	private static final String COMPONENT_TEXT = "gov.redhawk.style.ComponentText";
	private static final String START_ORDER_TEXT = "gov.redhawk.style.StartOrderText";
	private static final String START_ORDER_ELLIPSE = "gov.redhawk.style.StartOrderEllipse";
	private static final String START_ORDER_ASSEMBLY_CONTROLLER_ELLIPSE = "gov.redhawk.style.StartOrderAssemblyControllerEllipse";
	private static final String LOLLIPOP_LINE = "gov.redhawk.style.LollipopLine";
	private static final String LOLLIPOP_ELLIPSE = "gov.redhawk.style.LollipopEllipse";
	private static final String USES_PORT_ANCHOR = "gov.redhawk.style.UsesPortAnchor";
	private static final String USES_EXTERNAL_PORT = "gov.redhawk.style.UsesExternalPort";
	private static final String SUPER_USES_PORT = "gov.redhawk.style.SuperUsesPort";
	private static final String USES_PORT = "gov.redhawk.style.UsesPort";
	private static final String EXTERNAL_PROVIDES_PORT = "gov.redhawk.style.ExternalProvidesPort";
	private static final String PROVIDES_PORT = "gov.redhawk.style.ProvidesPort";
	private static final String SUPER_PROVIDES_PORT = "gov.redhawk.style.SuperProvidesPort";
	private static final String ERROR_TEXT_CONNECTIONS = "gov.redhawk.style.ErrorTextConnections";
	private static final String INNER_TEXT = "gov.redhawk.style.InnerText";
	private static final String OUTER_TEXT = "gov.redhawk.style.OuterText";
	private static final String FIND_BY_INNER = "gov.redhawk.style.FindByInner";
	private static final String USES_DEVICE_INNER = "gov.redhawk.style.UsesDeviceInner";
	private static final String HOST_COLLOCATION = "gov.redhawk.style.HostCollocation";
	private static final String FIND_BY_OUTER = "gov.redhawk.style.FindByOuter";
	private static final String USES_DEVICE_OUTER = "gov.redhawk.style.UsesDeviceOuter";
	private static final String COMPONENT_INNER = "gov.redhawk.style.ComponentInner";
	private static final String COMPONENT_INNER_STARTED = "gov.redhawk.style.ComponentInnerStarted";
	private static final String COMPONENT_INNER_ERROR = "gov.redhawk.style.ComponentInnerError";
	private static final String COMPONENT_INNER_DISABLED = "gov.redhawk.style.ComponentInnerDisabled";
	private static final String COMPONENT_OUTER = "gov.redhawk.style.ComponentOuter";
	public static final IColorConstant TEXT_FOREGROUND = IColorConstant.BLACK;
	public static final IColorConstant WHITE = IColorConstant.WHITE;
	public static final IColorConstant BLACK = IColorConstant.BLACK;
	public static final IColorConstant RED = IColorConstant.RED;
	public static final IColorConstant YELLOW = IColorConstant.YELLOW;
	public static final IColorConstant GREEN = IColorConstant.GREEN;
	public static final IColorConstant BLUE = new ColorConstant(0, 0, 194);
	public static final IColorConstant GOLD = new ColorConstant(255, 218, 105);
	public static final IColorConstant COMPONENT_FOREGROUND = new ColorConstant(98, 131, 167);
	public static final IColorConstant COMPONENT_BACKGROUND = new ColorConstant(187, 218, 247);
	public static final IColorConstant OUTER_CONTAINER_BACKGROUND = new ColorConstant(250, 250, 250);

	// Colors for port statistics feature
	private static final IColorConstant PORT_OK = GREEN;
	private static final IColorConstant PORT_WARNING_1 = YELLOW;
	private static final IColorConstant PORT_WARNING_2 = new ColorConstant(255, 170, 0);
	private static final IColorConstant PORT_WARNING_3 = new ColorConstant(255, 85, 0);
	private static final IColorConstant PORT_WARNING_4 = IColorConstant.RED;

	// Colors for port connection helpers
	private static final IColorConstant COMPATIBLE_PORT = GREEN;

	// COMPONENT
	public static final int DEFAULT_LINE_WIDTH = 2;
	public static final int ASSEMBLY_CONTROLLER_LINE_WIDTH = 3;

	// COLORS
	// TODO shouldn't we be disposing of these Colors correctly?
	public static final Color FOREGROUND_COLOR = new Color(null, 116, 130, 141); // TODO dispose?
	public static final Color COMPONENT_IDLE_COLOR = new Color(null, 219, 233, 246); // TODO dispose?
	public static final Color COMPONENT_STARTED_COLOR = new Color(null, 186, 234, 173); // TODO dispose?
	public static final Color DEFAULT_COMPONENT_COLOR = new Color(null, 176, 176, 176); // TODO dispose?
	public static final Color ASSEMBLY_CONTROLLER_COLOR = new Color(null, 255, 218, 105); // TODO dispose?

	// FONTS
	private static final String SANS_FONT = "Sans";
	private static final String DEFAULT_FONT = SANS_FONT;

	/**
	 * Globally shared instance for styles
	 */
	private static Resource styleResource = null;

	public static final Font getOuterTitleFont(Diagram diagram) {
		return Graphiti.getGaService().manageFont(diagram, DEFAULT_FONT, 8, false, true);
	}

	public static final Font getInnerTitleFont(Diagram diagram) {
		return Graphiti.getGaService().manageFont(diagram, DEFAULT_FONT, 11, false, false);
	}

	public static final Font getErrorConnectionFont(Diagram diagram) {
		return Graphiti.getGaService().manageFont(diagram, DEFAULT_FONT, 8, false, false);
	}

	public static final Font getStartOrderFont(Diagram diagram) {
		return Graphiti.getGaService().manageFont(diagram, DEFAULT_FONT, 8, false, false);
	}

	public static final Font getPortFont(Diagram diagram) {
		return Graphiti.getGaService().manageFont(diagram, DEFAULT_FONT, 8, false, false);
	}

	private static Diagram getStyleDiagram() {
		if (styleResource == null) {
			styleResource = StyleResourceFactory.createResource();
		}
		return (Diagram) styleResource.getContents().get(0);
	}

	public static void createAllStyles(Diagram diagram) {
		createStyleForComponentOuter(diagram);
		createStyleForComponentInner(diagram);
		createStyleForComponentInnerStarted(diagram);
		createStyleForComponentInnerError(diagram);
		createStyleForComponentInnerDisabled(diagram);
	}

	public static Style getStyle(String styleId) {
		Diagram diagram = getStyleDiagram();
		return findStyle(diagram, styleId);
	}

	// returns component outer rectangle style
	public static Style getStyleForComponentOuter() {
		return getStyle(COMPONENT_OUTER);
	}

	private static Style createStyleForComponentOuter(Diagram diagram) {
		IGaService gaService = Graphiti.getGaService();
		Style style = gaService.createStyle(diagram, COMPONENT_OUTER);
		style.setForeground(gaService.manageColor(diagram, BLACK));
		style.setBackground(gaService.manageColor(diagram, OUTER_CONTAINER_BACKGROUND));
		style.setFont(gaService.manageFont(diagram, DEFAULT_FONT, 8, false, false));
		style.setLineWidth(0);
		style.setLineVisible(false);
		return style;
	}

	// returns component inner rectangle style
	public static Style getStyleForComponentInner() {
		return getStyle(COMPONENT_INNER);
	}

	private static Style createStyleForComponentInner(Diagram diagram) {
		IGaService gaService = Graphiti.getGaService();
		Style style = gaService.createStyle(diagram, COMPONENT_INNER);
		gaService.setRenderingStyle(style, PredefinedColoredAreas.getBlueWhiteAdaptions());
		style.setLineWidth(2);
		return style;
	}

	// updates component inner rectangle style
	public static Style getStyleForComponentInnerStarted() {
		return getStyle(COMPONENT_INNER_STARTED);
	}

	private static Style createStyleForComponentInnerStarted(Diagram diagram) {
		IGaService gaService = Graphiti.getGaService();
		Style style = gaService.createStyle(diagram, COMPONENT_INNER_STARTED);
		gaService.setRenderingStyle(style, RHContainerColoredAreas.getGreenWhiteAdaptions());
		style.setLineWidth(2);
		return style;
	}

	// updates component inner rectangle style when it is in an error state
	public static Style getStyleForComponentInnerError() {
		return getStyle(COMPONENT_INNER_ERROR);
	}

	private static Style createStyleForComponentInnerError(Diagram diagram) {
		IGaService gaService = Graphiti.getGaService();
		Style style = gaService.createStyle(diagram, COMPONENT_INNER_ERROR);
		gaService.setRenderingStyle(style, RHContainerColoredAreas.getYellowWhiteAdaptions());
		style.setLineWidth(2);
		return style;
	}

	// updates component inner rectangle style when it is in a disabled state
	public static Style getStyleForComponentInnerDisabled() {
		return getStyle(COMPONENT_INNER_DISABLED);
	}

	private static Style createStyleForComponentInnerDisabled(Diagram diagram) {
		IGaService gaService = Graphiti.getGaService();
		Style style = gaService.createStyle(diagram, COMPONENT_INNER_DISABLED);
		gaService.setRenderingStyle(style, PredefinedColoredAreas.getLightGrayAdaptions());
		style.setLineWidth(2);
		return style;
	}

	// returns findby outer rectangle style
	public static Style createStyleForFindByOuter(Diagram diagram) {
		final String styleId = FIND_BY_OUTER;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setTransparency(.99d);
			style.setBackground(gaService.manageColor(diagram, OUTER_CONTAINER_BACKGROUND));
			style.setFont(gaService.manageFont(diagram, DEFAULT_FONT, 8, false, false));
			style.setLineWidth(0);
			style.setLineVisible(false);
		}
		return style;
	}

	// returns uses device outer rectangle style
	public static Style createStyleForUsesDeviceOuter(Diagram diagram) {
		final String styleId = USES_DEVICE_OUTER;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setTransparency(.99d);
			style.setBackground(gaService.manageColor(diagram, OUTER_CONTAINER_BACKGROUND));
			style.setFont(gaService.manageFont(diagram, DEFAULT_FONT, 8, false, false));
			style.setLineWidth(0);
			style.setLineVisible(false);
		}
		return style;
	}

	// returns host collocation rectangle style
	public static Style createStyleForHostCollocation(Diagram diagram) {
		final String styleId = HOST_COLLOCATION;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setTransparency(.99d);
			style.setBackground(gaService.manageColor(diagram, OUTER_CONTAINER_BACKGROUND));
			style.setFont(gaService.manageFont(diagram, DEFAULT_FONT, 8, false, false));
			style.setLineWidth(1);
			style.setLineVisible(true);
		}
		return style;
	}

	// returns find by inner rectangle style
	public static Style createStyleForFindByInner(Diagram diagram) {
		final String styleId = FIND_BY_INNER;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setBackground(gaService.manageColor(diagram, new ColorConstant(255, 0, 0)));
			style.setLineStyle(LineStyle.DASH);
			gaService.setRenderingStyle(style, FindByColoredAreas.getCopperWhiteAdaptions());
			style.setLineWidth(2);
		}
		return style;
	}

	// returns uses device inner rectangle style
	public static Style createStyleForUsesDeviceInner(Diagram diagram) {
		final String styleId = USES_DEVICE_INNER;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setBackground(gaService.manageColor(diagram, new ColorConstant(255, 0, 0)));
			style.setLineStyle(LineStyle.DASH);
			gaService.setRenderingStyle(style, FindByColoredAreas.getLightGrayAdaptions());
			style.setLineWidth(2);
		}
		return style;
	}

	// returns outer text style
	public static Style createStyleForOuterText(Diagram diagram) {
		final String styleId = OUTER_TEXT;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setFont(getOuterTitleFont(diagram));
			style.setLineWidth(2);
		}
		return style;
	}

	// returns inner text style
	public static Style createStyleForInnerText(Diagram diagram) {
		final String styleId = INNER_TEXT;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setFont(getInnerTitleFont(diagram));
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setLineWidth(2);
		}
		return style;
	}

	// returns error message font for improper connections
	public static Style createStyleForErrorTextConnections(Diagram diagram) {
		final String styleId = ERROR_TEXT_CONNECTIONS;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setFont(getErrorConnectionFont(diagram));
			style.setBackground(gaService.manageColor(diagram, OUTER_CONTAINER_BACKGROUND));
			style.setForeground(gaService.manageColor(diagram, RED));
			style.setLineWidth(2);
		}
		return style;
	}

	public static boolean needsUpdateForProvidesPort(Diagram diagram, Style style) {
		if (style == null) {
			return true;
		}
		boolean result = PluginUtil.equals(PROVIDES_PORT, style.getId());
		return !result;
	}

	// returns style for provides port
	public static Style createStyleForProvidesPort(Diagram diagram) {
		final String styleId = PROVIDES_PORT;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, WHITE));
			style.setFont(getPortFont(diagram));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}

	/**
	 * Style for a port which is compatible as the other end of a connection.
	 * @param diagram
	 * @return
	 */
	public static Style createStyleForCompatiblePort(Diagram diagram) {
		final String styleId = "gov.redhawk.style.PortCompatible";
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, COMPATIBLE_PORT));
			style.setFont(getPortFont(diagram));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}

	// returns style for port statistics - no errors
	public static Style createStyleForPortOK(Diagram diagram) {
		final String styleId = "gov.redhawk.style.PortOK";
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, PORT_OK));
			style.setFont(getPortFont(diagram));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}

	// returns style for port statistics - error level 1
	public static Style createStyleForPortWarning1(Diagram diagram) {
		final String styleId = "gov.redhawk.style.PortWarning1";
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, PORT_WARNING_1));
			style.setFont(getPortFont(diagram));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}

	// returns style for port statistics - error level 2
	public static Style createStyleForPortWarning2(Diagram diagram) {
		final String styleId = "gov.redhawk.style.PortWarning2";
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, PORT_WARNING_2));
			style.setFont(getPortFont(diagram));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}

	// returns style for port statistics - error level 3
	public static Style createStyleForPortWarning3(Diagram diagram) {
		final String styleId = "gov.redhawk.style.PortWarning3";
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, PORT_WARNING_3));
			style.setFont(getPortFont(diagram));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}

	// returns style for port statistics - error level 4
	public static Style createStyleForPortWarning4(Diagram diagram) {
		final String styleId = "gov.redhawk.style.PortWarning4";
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, PORT_WARNING_4));
			style.setFont(getPortFont(diagram));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}

	public static boolean needsUpdateForExternalProvidesPort(Diagram diagram, Style style) {
		if (style == null) {
			return true;
		}
		boolean result = PluginUtil.equals(EXTERNAL_PROVIDES_PORT, style.getId());
		return !result;
	}

	// returns style for provides port
	public static Style createStyleForExternalProvidesPort(Diagram diagram) {
		final String styleId = EXTERNAL_PROVIDES_PORT;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, BLUE));
			style.setFont(getPortFont(diagram));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}

	public static boolean needsUpdateForUsesPort(Diagram diagram, Style style) {
		if (style == null) {
			return true;
		}
		boolean result = PluginUtil.equals(USES_PORT, style.getId());
		return !result;
	}

	// returns style for uses port
	public static Style createStyleForUsesPort(Diagram diagram) {
		final String styleId = USES_PORT;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, BLACK));
			style.setFont(getPortFont(diagram));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}

	// returns style for super uses port
	public static Style createStyleForSuperUsesPort(Diagram diagram) {
		final String styleId = SUPER_USES_PORT;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, BLACK));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}

	// returns style for super uses port
	public static Style createStyleForSuperProvidesPort(Diagram diagram) {
		final String styleId = SUPER_PROVIDES_PORT;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, WHITE));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}

	public static boolean needsUpdateForExternalUsesPort(Diagram diagram, Style style) {
		if (style == null) {
			return true;
		}
		boolean result = PluginUtil.equals(USES_EXTERNAL_PORT, style.getId());
		return !result;
	}

	// returns style for uses external port
	public static Style createStyleForExternalUsesPort(Diagram diagram) {
		final String styleId = USES_EXTERNAL_PORT;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, BLUE));
			style.setFont(getPortFont(diagram));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}

	// returns style for uses port
	public static Style createStyleForUsesPortAnchor(Diagram diagram) {
		final String styleId = USES_PORT_ANCHOR;
		Style style = findStyle(diagram, styleId);
		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setTransparency(100d);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, BLACK));
			style.setFont(getPortFont(diagram));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}

	// returns style for lollipop ellipse
	public static Style createStyleForLollipopEllipse(Diagram diagram) {
		final String styleId = LOLLIPOP_ELLIPSE;
		Style style = findStyle(diagram, styleId);

		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setLineWidth(1);
			style.setBackground(Graphiti.getGaService().manageColor(diagram, WHITE));
			style.setTransparency(.99d);
		}
		return style;
	}

	// returns style for lollipop line
	public static Style createStyleForLollipopLine(Diagram diagram) {
		final String styleId = LOLLIPOP_LINE;
		Style style = findStyle(diagram, styleId);

		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setBackground(Graphiti.getGaService().manageColor(diagram, BLACK));
		}
		return style;
	}

	public static boolean needsUpdateForStartOrderAssemblyControllerEllipse(Diagram diagram, Style style) {
		if (style == null) {
			return true;
		}
		boolean result = PluginUtil.equals(style.getId(), START_ORDER_ASSEMBLY_CONTROLLER_ELLIPSE);
		return !result;
	}

	public static Style createStyleForStartOrderAssemblyControllerEllipse(Diagram diagram) {
		final String styleId = START_ORDER_ASSEMBLY_CONTROLLER_ELLIPSE;
		Style style = findStyle(diagram, styleId);

		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setLineWidth(1);
			style.setBackground(Graphiti.getGaService().manageColor(diagram, GOLD));
			style.setTransparency(.99d);
		}
		return style;
	}

	public static boolean needsUpdateForStartOrderEllipse(Diagram diagram, Style style) {
		if (style == null) {
			return true;
		}
		boolean result = PluginUtil.equals(style.getId(), START_ORDER_ELLIPSE);
		return !result;
	}

	public static Style createStyleForStartOrderEllipse(Diagram diagram) {
		final String styleId = START_ORDER_ELLIPSE;
		Style style = findStyle(diagram, styleId);

		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setLineWidth(1);
			style.setBackground(Graphiti.getGaService().manageColor(diagram, WHITE));
			style.setTransparency(.99d);
		}
		return style;
	}

	public static Style createStyleForStartOrderText(Diagram diagram) {
		final String styleId = START_ORDER_TEXT;
		Style style = findStyle(diagram, styleId);

		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, WHITE));
			style.setFont(getStartOrderFont(diagram));
		}
		return style;
	}

	// returns component text style
	public static Style createStyleForPortText(Diagram diagram) {
		final String styleId = COMPONENT_TEXT;
		Style style = findStyle(diagram, styleId);

		if (style == null) {
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, WHITE));
			style.setFont(getOuterTitleFont(diagram));
		}
		return style;
	}

	// find the style with given id in style-container
	public static Style findStyle(StyleContainer styleContainer, String id) {
		// find and return style
		Collection<Style> styles = styleContainer.getStyles();
		if (styles != null) {
			for (Style style : styles) {
				if (id.equals(style.getId())) {
					return style;
				}
			}
		}
		return null;
	}

}
