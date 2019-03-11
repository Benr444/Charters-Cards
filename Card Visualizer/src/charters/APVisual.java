package charters;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.AttributedString;
import java.util.ArrayList;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUnits;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class APVisual 
{
	public static void main(String... args) {APVisual.initialize(Paths.get(INPUT_FOLDER), Paths.get(OUTPUT_FOLDER));}
	
	//Constants
		//Mathematical
			public static final double PHI = 1.6180339887;
		//Meta
			public static final String OUTPUT_FOLDER = "D:\\Code\\Charters Card Generator\\Charters Card Outputs";
			public static final String INPUT_FOLDER = "D:\\Code\\Charters Card Generator\\Charters Card Inputs";
			public static final String IMAGE_EXTENSION = ".svg";
			public static final String CARDS_EXTENSION = "_cards.json";
		//Image-Specfic
			//General
			public static final int MAX_AP_HEIGHT = 400;
			public static final int MAX_AP_WIDTH = (int)(PHI * MAX_AP_HEIGHT);
			public static final double SHADOW_DX = 0.01;
			public static final double SHADOW_DY = SHADOW_DX;
			public static final double CORNER_RADIUS = 0.1;
			public static final double STROKE_WIDTH = 0.005;
			public static final double SIDE_PAD = 0.1; //On either side of the AP, nothing can render here
			//Critical Row
			public static final double CRITICAL_HEIGHT = 0.15;
			public static final double CRITICAL_GAP = 0.15;
			public static final double CRITICAL_LIFT = 0.03; //The height from the bottom of the critical row to the text line
			public static final int CRITICAL_FONT_SIZE = 25;
			//Modifier Row
			public static final double MODIFIER_HEIGHT = 0.10;
			public static final double MODIFIER_GAP = 0.15;
			public static final double MODIFIER_LIFT = 0.03; //The height from the bottom of the modifier row to the text line
			public static final int MODIFIER_FONT_SIZE = 20;
			//Contents Row
			
	//Variables/Fields
		//Public
		//Private
			private String fileName;
			private CardObject cardObj;
			private SVGGraphics2D pen;
			/* 
			 * apWidth, apHeight: Smaller versions of abilities/passives dont render at full size.
			 * This is the size they render at.
			 */
			private final int apWidth;
			private final int apHeight;
			private final boolean minMode;
	//Functions/Methods
		//Public
			public int px(double percent)
			{
				return (int)Math.abs(percent * apWidth);
			};
			
			public int py(double percent)
			{
				return (int)Math.abs(percent * apHeight);
			};
			
			public static void initialize(Path inputFolder, Path outputFolder)
			{	
				//Initialization
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				
				//Search the input folder for .json design files
				try 
				{
					DirectoryStream<Path> files = Files.newDirectoryStream(inputFolder);
					for (Path p : files)
					{
						if (p.getFileName().toString().endsWith(CARDS_EXTENSION))
						{
							System.out.println("Found design file: " + p.getFileName());
							String contents = new String(Files.readAllBytes(p));
							
							//For each of those files, create a CardObject
							CardDesign cd = gson.fromJson(contents, CardDesign.class);
							
							//For each CardObject, create a CardVisual
							for (CardObject co : cd.cards)
							{
								co.clean();
								System.out.println(co);
								
								APVisual cv = new APVisual(co, outputFolder, false);
								cv.generate(Paths.get(OUTPUT_FOLDER));
								//Export each CardVisual to the output folder
							}
						}
					}
					files.close();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
			
			public APVisual(CardObject co, Path outputFolder, boolean minMode)
			{
				this.cardObj = co;
				this.fileName = co.name.trim().toLowerCase().replace(" ", "_");
				this.minMode = minMode;
				
				/*
				 * General graphics setup
				 * Nothing will be drawn in this part of the class
				 */
				Font CRITICAL_FONT = new Font("Consolas", Font.PLAIN, CRITICAL_FONT_SIZE);
				Font MODIFIER_FONT = new Font("Consolas", Font.PLAIN, MODIFIER_FONT_SIZE);
				this.pen = new SVGGraphics2D(1000, 1000, SVGUnits.PX);
				FontMetrics metrics = pen.getFontMetrics();
				this.apWidth = MAX_AP_WIDTH;
				this.apHeight = MAX_AP_HEIGHT;
				BasicStroke bs = new BasicStroke(px(STROKE_WIDTH));
				pen.setStroke(bs);
				
				Shape mainRect; //Used to bound the entire card (Not shadow)
				
				if (cardObj.isAbility) //Draw these if it is an ability
				{
					//Shadow box
					RoundRectangle2D.Double cardShadow = new RoundRectangle2D.Double
					(
						px(SHADOW_DX), 
						py(SHADOW_DY), 
						px(1), 
						py(1), 
						px(CORNER_RADIUS), 
						px(CORNER_RADIUS)
					);
					pen.setPaint(Color.BLACK);
					pen.fill(cardShadow);
					
					//Main rect fill
					mainRect = new RoundRectangle2D.Double
					(
						0, 
						0, 
						px(1), 
						py(1), 
						px(CORNER_RADIUS), 
						px(CORNER_RADIUS)
					);
				}
				else //Is passive
				{
					//Shadow box
					Rectangle2D.Double cardShadow = new Rectangle2D.Double
					(
						0, 
						0, 
						px(SHADOW_DX + 1), 
						py(SHADOW_DY + 1)
					);
					pen.setPaint(Color.BLACK);
					pen.fill(cardShadow);
					
					//Main rect
					pen.translate(px(SHADOW_DX), py(SHADOW_DY));
					mainRect = new Rectangle2D.Double
					(
						0, 
						0, 
						px(1), 
						py(1)
					);
				}
				
				Area mainArea = new Area(mainRect);
				pen.setFont(CRITICAL_FONT);
				APRow criticalRow = new APRow();
				criticalRow.setWidth(MAX_AP_WIDTH);
				criticalRow.clip = (mainArea);
				criticalRow.setLeftPad(SIDE_PAD);
				criticalRow.setTopPad(0.1);
				criticalRow.setRightPad(SIDE_PAD);
				criticalRow.setBottomPad(0.05);
				criticalRow.setGap(CRITICAL_GAP);
				criticalRow.setDivisor(1/PHI);
				String aspectString = "";
				for (String a : cardObj.aspects)
				{
					aspectString = aspectString + "[" + a + "]";
				}
				if (cardObj.cost != null || aspectString != "")
				{
					criticalRow.rightText = new AttributedString(aspectString + cardObj.cost);
					criticalRow.rightText.addAttribute(TextAttribute.FOREGROUND, Color.BLUE);
					criticalRow.rightText.addAttribute(TextAttribute.BACKGROUND, Color.WHITE);
					criticalRow.rightText.addAttribute(TextAttribute.FONT, CRITICAL_FONT);
				}
				if (cardObj.name != null) //Sorta redundant since the card will always have a name
				{
					criticalRow.leftText = new AttributedString(cardObj.name);
					criticalRow.leftText.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
					criticalRow.leftText.addAttribute(TextAttribute.BACKGROUND, Color.WHITE);
					criticalRow.leftText.addAttribute(TextAttribute.FONT, CRITICAL_FONT);
				}
				int critHeight = criticalRow.draw(pen, 0, 0);
				
				pen.setFont(MODIFIER_FONT);
				APRow modifierRow = new APRow();
				modifierRow.setWidth(MAX_AP_WIDTH);
				modifierRow.setLeftPad(SIDE_PAD);
				modifierRow.setTopPad(0.05);
				modifierRow.setRightPad(SIDE_PAD);
				modifierRow.setBottomPad(0.05);
				modifierRow.setGap(MODIFIER_GAP);
				modifierRow.setDivisor(1/PHI);
				
				String modifierString = "";
				for (String s : cardObj.modifiers)
				{
					modifierString = modifierString + s;
				}
				if (modifierString != "")
				{
					modifierRow.rightText = new AttributedString(modifierString);
					modifierRow.rightText.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
					modifierRow.rightText.addAttribute(TextAttribute.BACKGROUND, Color.WHITE);
					modifierRow.rightText.addAttribute(TextAttribute.FONT, MODIFIER_FONT);
					modifierRow.rightText.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
					
				}
				if (cardObj.types != null) //Sorta redundant since the card will always have a name
				{
					modifierRow.leftText = new AttributedString(cardObj.types);
					modifierRow.leftText.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
					modifierRow.leftText.addAttribute(TextAttribute.BACKGROUND, Color.WHITE);
					modifierRow.leftText.addAttribute(TextAttribute.FONT, MODIFIER_FONT);
				}
				modifierRow.draw(pen, 0, critHeight);
				
				
				/*
				//Grab modifier information
				pen.setFont(MODIFIER_FONT);
				String completeModifier = "";
				for (String s : cardObj.modifiers)
				{
					completeModifier = completeModifier + s;
				}
				int modifiersWidth = pen.getFontMetrics(CRITICAL_FONT).stringWidth(completeModifier);
				
				//Modifier row
				Area modifierRow = new Area(new Rectangle2D.Double
				(
						0,
						py(CRITICAL_ROW_HEIGHT),
						px(1), 
						py(MODIFIER_ROW_HEIGHT)
				));
				modifierRow.intersect(mainArea);
				pen.setPaint(Color.WHITE);
				pen.fill(modifierRow);
				pen.setPaint(Color.BLACK);
				pen.draw(modifierRow);
				
				//Content row
				Area contentRow = new Area(new Rectangle2D.Double
				(
						0,
						py(CRITICAL_ROW_HEIGHT + MODIFIER_ROW_HEIGHT),
						px(1), 
						py(1 - MODIFIER_ROW_HEIGHT - CRITICAL_ROW_HEIGHT)
				));
				contentRow.intersect(mainArea);
				pen.setPaint(Color.WHITE);
				pen.fill(contentRow);
				pen.setPaint(Color.BLACK);
				pen.draw(contentRow);
				
				//Get font height
				int nameHeight = pen.getFontMetrics(CRITICAL_FONT).getAscent() - pen.getFontMetrics(CRITICAL_FONT).getLeading();
				System.out.println("nameHeight: " + nameHeight);
				
				//Draw name
				pen.setFont(CRITICAL_FONT);
				pen.drawString
				(
						cardObj.name,
						0 + px(0 + CRITICAL_PADDING_X),
						0 + py(CRITICAL_ROW_HEIGHT - CRITICAL_PADDING_Y)
				);
				
				//Draw cost
				if (cardObj.cost != null)
				{
					pen.setFont(CRITICAL_FONT);
					int costWidth = pen.getFontMetrics(CRITICAL_FONT).stringWidth(cardObj.cost);
					pen.drawString
					(
							cardObj.cost,
							0 + (MAX_AP_WIDTH - costWidth - px(CRITICAL_PADDING_X)),
							0 + py(CRITICAL_ROW_HEIGHT - CRITICAL_PADDING_Y)
					);
				}
				
				//Draw modifier text
				pen.drawString
				(
						completeModifier,
						0 + (MAX_AP_WIDTH - modifiersWidth - px(0 + MODIFIER_PADDING_X)),
						0 + py(CRITICAL_ROW_HEIGHT + MODIFIER_ROW_HEIGHT - MODIFIER_PADDING_Y)
				);
				
				//Draw subtype text
				pen.setFont(MODIFIER_FONT);
				pen.drawString
				(
						cardObj.types,
						0 + px(0 + MODIFIER_PADDING_X),
						0 + py(CRITICAL_ROW_HEIGHT + MODIFIER_ROW_HEIGHT - MODIFIER_PADDING_Y)
				);
				*/
			}
			
			/**
			 * @param outputFolder - folder to save to
			 */
			public void generate(Path outputFolder)
			{
				//Save
				this.save(outputFolder);
			}
		
		//Private
			/** Saves this visual */
			private void save(Path outputFolder)
			{
				//Write the string into an svg file
				Path fullOutputPath = Paths.get(outputFolder.toString(), fileName + IMAGE_EXTENSION);
				try 
				{
					FileWriter fw = new FileWriter(fullOutputPath.toString());
					fw.write(pen.getSVGElement());
					System.out.println("> CardVisual saved to " + fullOutputPath);
					fw.close();
				} 
				catch (IOException e) {e.printStackTrace();}
			}
}
