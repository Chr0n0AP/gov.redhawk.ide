package gov.redhawk.ide.sad.graphiti.ui.diagram.util;

import java.util.Collection;

import org.eclipse.graphiti.mm.StyleContainer;
import org.eclipse.graphiti.mm.algorithms.styles.Style;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;
import org.eclipse.graphiti.util.PredefinedColoredAreas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class StyleUtil {

	public static final IColorConstant TEXT_FOREGROUND = IColorConstant.BLACK;
	public static final IColorConstant WHITE = new ColorConstant(255, 255, 255);
	public static final IColorConstant BLACK = new ColorConstant(0, 0, 0);
	public static final IColorConstant COMPONENT_FOREGROUND = new ColorConstant(98, 131, 167);
	public static final IColorConstant COMPONENT_BACKGROUND = new ColorConstant(187, 218, 247);
	
	//COMPONENT
	public static final int DEFAULT_LINE_WIDTH = 2;
	public static final int ASSEMBLY_CONTROLLER_LINE_WIDTH = 3;
	public static final Color FOREGROUND_COLOR = new Color(null, 116, 130, 141); // TODO shouldn't we be disposing of these correctly?
	public static final Color COMPONENT_IDLE_COLOR = new Color(null, 219, 233, 246); // TODO shouldn't we be disposing of these correctly?
	public static final Color COMPONENT_STARTED_COLOR = new Color(null, 186, 234, 173); // TODO shouldn't we be disposing of these correctly?
	public static final Color DEFAULT_COMPONENT_COLOR = new Color(null, 176, 176, 176); // TODO shouldn't we be disposing of these correctly?
	public static final Color ASSEMBLY_CONTROLLER_COLOR = new Color(null, 255, 218, 105); // TODO shouldn't we be disposing of these correctly?
	public static final Font START_ORDER_FONT = new Font(null, "Arial", 12, SWT.BOLD); // TODO shouldn't we be disposing of these correctly?
	
	
	
	
	private static final IColorConstant EXTERNAL_PORT_BACKGROUND = new ColorConstant(0, 210, 255);
	
	private final static String DEFAULT_FONT = "Arial";
	
	//returns component outer rectangle style
	public static Style getStyleForComponentOuter(Diagram diagram){
		final String styleId = "ComponentOuter";
		Style style = findStyle(diagram, styleId);
		
		if(style == null){
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setTransparency(.99d);
			style.setBackground(gaService.manageColor(diagram, WHITE));
			style.setFont(gaService.manageFont(diagram, DEFAULT_FONT, 8, false, false));
			style.setLineWidth(0);
			style.setLineVisible(false);
		}
		return style;
	}
	
	//returns component inner rectangle style
	public static Style getStyleForComponentInner(Diagram diagram){
		final String styleId = "ComponentInner";
		Style parentStyle = getStyleForComponentOuter(diagram);
		Style style = findStyle(diagram, styleId);
		if(style == null){
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(parentStyle, styleId);
			style.setBackground(gaService.manageColor(diagram, new ColorConstant(255, 0, 0)));
			gaService.setRenderingStyle(style, PredefinedColoredAreas.getBlueWhiteAdaptions());
			style.setLineWidth(2);
		}
		return style;
	}
	
	//returns component text style
	public static Style getStyleForComponentText(Diagram diagram){
		final String styleId = "ComponentText";
		Style parentStyle = getStyleForComponentInner(diagram);
		Style style = findStyle(diagram, styleId);
		if(style == null){
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(parentStyle, styleId);
			style.setLineWidth(2);
		}
		return style;
	}
	
	//returns style for provides port
	public static Style getStyleForProvidesPort(Diagram diagram){
		final String styleId = "ProvidesPort";
		Style style = findStyle(diagram, styleId);
		
		if(style == null){
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, WHITE));
			style.setFont(gaService.manageFont(diagram, DEFAULT_FONT, 8, false, false));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}
	
	//returns style for uses port
	public static Style getStyleForUsesPort(Diagram diagram){
		final String styleId = "UsesPort";
		Style style = findStyle(diagram, styleId);
		
		if(style == null){
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, BLACK));
			style.setFont(gaService.manageFont(diagram, DEFAULT_FONT, 8, false, false));
			style.setLineWidth(2);
			style.setLineVisible(true);
		}
		return style;
	}
	
	//returns component text style
	public static Style getStyleForPortText(Diagram diagram){
		final String styleId = "ComponentText";
Style style = findStyle(diagram, styleId);
		
		if(style == null){
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, BLACK));
			style.setBackground(gaService.manageColor(diagram, WHITE));
			style.setFont(gaService.manageFont(diagram, DEFAULT_FONT, 8, false, false));
		}
		return style;
	}
	
	//find the style with given id in style-container
	private static Style findStyle(StyleContainer styleContainer, String id){
		//find and return style
		Collection<Style> styles = styleContainer.getStyles();
		if(styles != null){
			for(Style style: styles){
				if(id.equals(style.getId())){
					return style;
				}
			}
		}
		return null;
	}
}
