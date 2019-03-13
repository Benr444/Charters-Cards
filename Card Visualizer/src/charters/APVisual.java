package charters;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
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
			public static final String INPUT_FOLDER = "in";
			public static final String OUTPUT_FOLDER = "out";
			public static final String IMAGE_EXTENSION = ".svg";
			public static final String CARDS_EXTENSION = "_cards.json";
		//Image-Specfic
			//General
			public static final Color BG_COLOR = new Color(255, 255, 230);
			public static final int MAX_AP_HEIGHT = 400;
			public static final int MAX_AP_WIDTH = (int)(PHI * MAX_AP_HEIGHT);
			public static final double SHADOW_DX = 0.01;
			public static final double SHADOW_DY = SHADOW_DX;
			public static final double CORNER_RADIUS = 0.1;
			public static final double STROKE_WIDTH = 0.005;
			public static final double SIDE_PAD = 0.1; //On either side of the AP, nothing can render here
			//Critical Row
			public static final double CRITICAL_GAP = 0.15;
			public static final double CRITICAL_LIFT = 0.03; //The height from the bottom of the critical row to the text line
			public static final int CRITICAL_FONT_SIZE = 20;
			//Modifier Row
			public static final double MODIFIER_GAP = 0.15;
			public static final double MODIFIER_LIFT = 0.03; //The height from the bottom of the modifier row to the text line
			public static final int MODIFIER_FONT_SIZE = 20;
			//Contents Row
			public static final int CONTENTS_FONT_SIZE = 20;
			public static final double ART_DIVISOR = 1 - 1/PHI;
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
						print("File: " + p);
						if (p.getFileName().toString().endsWith(CARDS_EXTENSION))
						{
							print("Found design file: " + p.getFileName());
							String contents = new String(Files.readAllBytes(p));
							
							//For each of those files, create a CardObject
							CardDesign cd = gson.fromJson(contents, CardDesign.class);
							
							//For each CardObject, create a CardVisual
							for (CardObject co : cd.cards)
							{
								co.clean();
								print(co);
								
								APVisual cv = new APVisual(co, false, 1);
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

			public APVisual(CardObject co, boolean minMode, double scaleFactor)
			{
				this(null, co, minMode, scaleFactor, 0, 0);
			}
			
			public APVisual(SVGGraphics2D inputPen, CardObject co, boolean minMode, double scaleFactor, int bx, int by)
			{
				this.cardObj = co;
				this.fileName = co.name.trim().toLowerCase().replace(" ", "_");
				this.apWidth = (int)(MAX_AP_WIDTH * scaleFactor);
				this.apHeight = (int)(MAX_AP_HEIGHT * scaleFactor);
				
				if (inputPen == null)
				{
					this.pen = new SVGGraphics2D(px(1 + SHADOW_DX), py(1 + SHADOW_DY), SVGUnits.PX);
				}
				else
				{
					this.pen = inputPen;
				}
				
				/*
				 * General graphics setup
				 * Nothing will be drawn in this part of the class
				 */
				Font CRITICAL_FONT = new Font("Consolas", Font.PLAIN, (int)(CRITICAL_FONT_SIZE * scaleFactor));
				Font MODIFIER_FONT = new Font("Consolas", Font.PLAIN, (int)(MODIFIER_FONT_SIZE * scaleFactor));
				Font CONTENTS_FONT = new Font("Consolas", Font.PLAIN, (int)(CONTENTS_FONT_SIZE * scaleFactor));
				BasicStroke bs = new BasicStroke(px(STROKE_WIDTH));
				pen.setStroke(bs);
				
				Shape mainRect; //Used to bound the entire card (Not shadow)
				
				if (cardObj.isAbility) //Draw these if it is an ability
				{
					//Shadow box
					RoundRectangle2D.Double cardShadow = new RoundRectangle2D.Double
					(
						bx + px(SHADOW_DX), 
						by + py(SHADOW_DY), 
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
						bx, 
						by, 
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
						bx, 
						by, 
						px(SHADOW_DX + 1), 
						py(SHADOW_DY + 1)
					);
					pen.setPaint(Color.BLACK);
					pen.fill(cardShadow);
					
					//Main rect
					pen.translate(px(SHADOW_DX), py(SHADOW_DY));
					mainRect = new Rectangle2D.Double
					(
						bx, 
						by, 
						px(1), 
						py(1)
					);
				}
				
				Area mainArea = new Area(mainRect);
				//pen.setFont(CRITICAL_FONT);
				APRow criticalRow = new APRow();
				criticalRow.fillColor = BG_COLOR;
				criticalRow.setWidth(px(1));
				criticalRow.setClip(mainArea);
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
					String tText = aspectString + ((cardObj.cost == null) ? "" : cardObj.cost);
					criticalRow.rightText = new AttributedString(tText);
					criticalRow.rightText.addAttribute(TextAttribute.FOREGROUND, Color.BLUE);
					criticalRow.rightText.addAttribute(TextAttribute.BACKGROUND, BG_COLOR);
					criticalRow.rightText.addAttribute(TextAttribute.FONT, CRITICAL_FONT);
				}
				if (cardObj.name != null) //Sorta redundant since the card will always have a name
				{
					criticalRow.leftText = new AttributedString(cardObj.name);
					criticalRow.leftText.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
					criticalRow.leftText.addAttribute(TextAttribute.BACKGROUND, BG_COLOR);
					criticalRow.leftText.addAttribute(TextAttribute.FONT, CRITICAL_FONT);
				}
				int critHeight = criticalRow.draw(pen, bx, by);
				
				//pen.setFont(MODIFIER_FONT);
				APRow modifierRow = new APRow();
				modifierRow.fillColor = BG_COLOR;
				modifierRow.setClip(mainArea);
				modifierRow.setWidth(px(1));
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
					modifierRow.rightText.addAttribute(TextAttribute.BACKGROUND, BG_COLOR);
					modifierRow.rightText.addAttribute(TextAttribute.FONT, MODIFIER_FONT);
					modifierRow.rightText.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
					
				}
				if (cardObj.types != null) //Sorta redundant since the card will always have a name
				{
					modifierRow.leftText = new AttributedString(cardObj.types);
					modifierRow.leftText.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
					modifierRow.leftText.addAttribute(TextAttribute.BACKGROUND, BG_COLOR);
					modifierRow.leftText.addAttribute(TextAttribute.FONT, MODIFIER_FONT);
				}
				int modHeight = modifierRow.draw(pen, bx, by + critHeight);
				
				APRow contentsRow = new APRow();
				contentsRow.fillColor = BG_COLOR;
				contentsRow.setClip(mainArea);
				contentsRow.setWidth(px(1));
				contentsRow.setLeftPad(minMode ? SIDE_PAD : ART_DIVISOR);
				contentsRow.setTopPad(0.05);
				contentsRow.setRightPad(SIDE_PAD);
				contentsRow.setBottomPad(0.05);
				contentsRow.setGap(0);
				contentsRow.setOverrideHeight(py(1) - critHeight - modHeight);
				String totalContents = "";
				for (String c : cardObj.contentsText)
				{
					totalContents = totalContents + c;
				}
				contentsRow.rightText = new AttributedString(totalContents);
				if (totalContents != "")
				{
					contentsRow.rightText.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
					contentsRow.rightText.addAttribute(TextAttribute.BACKGROUND, BG_COLOR);
					contentsRow.rightText.addAttribute(TextAttribute.FONT, CONTENTS_FONT);
				}
				contentsRow.draw(pen, bx, by + critHeight + modHeight);

				ArrayList<APVisual> subAPList= new ArrayList<APVisual>();
				for (CardObject c : cardObj.contentsSubs)
				{
					c.clean();
					subAPList.add(new APVisual(pen, c, true, 1 - ART_DIVISOR - SIDE_PAD, px(ART_DIVISOR), 50));
				}
				
				for (APVisual a : subAPList)
				{
					//a.
				}
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
					print("> CardVisual saved to " + fullOutputPath);
					fw.close();
				} 
				catch (IOException e) {e.printStackTrace();}
			}
			
			/** Helper function for better printouts */
			private static void print(Object o) {System.out.println("[APVisual] " + o);}
}
