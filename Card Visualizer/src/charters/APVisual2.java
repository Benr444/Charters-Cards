package charters;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;
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
import java.util.ArrayList;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUnits;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class APVisual2 
{
	public static void main(String... args) {APVisual2.initialize(Paths.get(INPUT_FOLDER), Paths.get(OUTPUT_FOLDER));}
	
	//Constants
		//Mathematical
			public static final double PHI = 1.6180339887;
		//Meta
			public static final String OUTPUT_FOLDER = "D:\\Code\\Charters Card Generator\\Charters Card Outputs";
			public static final String INPUT_FOLDER = "D:\\Code\\Charters Card Generator\\Charters Card Inputs";
			public static final String IMAGE_EXTENSION = ".svg";
			public static final String CARDS_EXTENSION = "_cards.json";
		//Image-Specfic
			public static final int CARD_HEIGHT = 400;
			public static final int CARD_WIDTH = (int)(PHI * 400);
			public static final double SHADOW_DX = 0.01;
			public static final double SHADOW_DY = SHADOW_DX;
			public static final double CORNER_RADIUS = 0.1;
			public static final double LINE_WIDTH = 0.005;
			public static final double MODIFIER_ROW_HEIGHT = 0.1;
			public static final double CRITICAL_ROW_HEIGHT = 1.25 * MODIFIER_ROW_HEIGHT;
			public static final double CRITICAL_PADDING_X = 0.025;
			public static final double CRITICAL_PADDING_Y = 0.01;
			public static final double CRITICAL_GAP = 0.15;
			public static final double MODIFIER_PADDING_X = CRITICAL_PADDING_X * (CRITICAL_ROW_HEIGHT/MODIFIER_ROW_HEIGHT);
			public static final double MODIFIER_PADDING_Y = CRITICAL_PADDING_Y * (CRITICAL_ROW_HEIGHT/MODIFIER_ROW_HEIGHT);
			public static final double MODIFIER_GAP = 0.15;
			public static final double CRITICAL_FONT_SIZE = CRITICAL_ROW_HEIGHT - (2 * CRITICAL_PADDING_Y);
			public static final double MODIFIER_FONT_SIZE = MODIFIER_ROW_HEIGHT - (2 * MODIFIER_PADDING_Y);
			public final Font CRITICAL_FONT;
			public final Font MODIFIER_FONT;
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
								System.out.println(co);
								
								APVisual2 cv = new APVisual2(co, outputFolder, false);
								cv.generate(Paths.get(OUTPUT_FOLDER), false);
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
			
			public APVisual2(CardObject co, Path outputFolder, boolean minMode)
			{
				this.cardObj = co;
				this.fileName = co.name.trim().toLowerCase().replace(" ", "_");
				this.apWidth = this.determineAPWidth(minMode);
				this.apHeight = this.determineAPHeight(minMode);
				CRITICAL_FONT = new Font("Consolas", Font.PLAIN, py(CRITICAL_FONT_SIZE));
				MODIFIER_FONT = new Font("Consolas", Font.PLAIN, py(MODIFIER_FONT_SIZE));
				this.pen = new SVGGraphics2D(this.width(), this.height(), SVGUnits.PX);
			}
			
			public int px(double percent)
			{
				return (int)Math.abs(percent * apWidth);
			};
			
			public int py(double percent)
			{
				return (int)Math.abs(percent * apHeight);
			};
			
			/***/
			public int height()
			{
				return px(1 + SHADOW_DY);
			}
			
			/***/
			public int width()
			{
				return px(1 + SHADOW_DX);
			}
			
			/**
			 * @param outputFolder - folder to save to
			 * @param minMode - TODO: if true, frame renders only large enough to fit contents
			 */
			public void generate(Path outputFolder, boolean minMode)
			{
				//DRAWING
				
				//Set general pen settings
				BasicStroke bs = new BasicStroke(px(LINE_WIDTH));
				pen.setStroke(bs);
				pen.setPaint(Color.BLACK);
				Shape mainRect; //Used to bound the entire card (Not shadow)
				
				if (cardObj.isAbility) //Draw these if it is an ability
				{
					//Shadow box
					pen.setPaint(Color.BLACK);
					RoundRectangle2D.Double cardShadow = new RoundRectangle2D.Double
					(
							px(SHADOW_DX), 
							py(SHADOW_DY), 
							px(1), 
							py(1), 
							px(CORNER_RADIUS), 
							px(CORNER_RADIUS)
					);
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
					pen.setPaint(Color.BLACK);
					Rectangle2D.Double cardShadow = new Rectangle2D.Double
					(
							0, 
							0, 
							px(SHADOW_DX + 1), 
							py(SHADOW_DY + 1)
					);
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
				
				//Critical row
				Area criticalRow = new Area(new Rectangle2D.Double
				(
						0, 
						0,
						px(1),
						py(CRITICAL_ROW_HEIGHT)
				));
				criticalRow.intersect(mainArea);
				pen.setPaint(Color.WHITE);
				pen.fill(criticalRow);
				pen.setPaint(Color.BLACK);
				pen.draw(criticalRow);

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
							0 + (CARD_WIDTH - costWidth - px(CRITICAL_PADDING_X)),
							0 + py(CRITICAL_ROW_HEIGHT - CRITICAL_PADDING_Y)
					);
				}
				
				//Draw modifier text
				pen.drawString
				(
						completeModifier,
						0 + (CARD_WIDTH - modifiersWidth - px(0 + MODIFIER_PADDING_X)),
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
				//Save
				this.save(pen.getSVGElement(), outputFolder);
			}
		
		//Private
		/** Wraps strings once to fit inside a width */
		private String[] wrapString( String in, int width)
		{
			pen.getFontMetrics().stringWidth(in);
			int cumulativeWidth = 0;
			String[] out = {in}; //Only remains the base string if no wrapping occurs
			
			for (int j = 0; j < in.length(); j++)
			{
				if (cumulativeWidth >= width && j != 0)
				{
					//Split the string into rows
					out = new String[2];
					out[0] = in.substring(0, j);
					out[1] = in.substring(j, in.length() - 1);
				}
				cumulativeWidth += pen.getFontMetrics().stringWidth("" + in.charAt(j));
			}
			
			return out;
		}

		/** Wraps strings to fit inside a width */
		private String[] completelyWrapString(String in, int width)
		{
			ArrayList<String> lines = new ArrayList<String>();
			String[] oneWrap = wrapString(in, width);
			lines.add(oneWrap[0]);
			if (oneWrap.length > 1)
			{
				//Continue wrapping
				String[] additionalWraps = completelyWrapString(oneWrap[1], width);
				for (int l = 0; l < additionalWraps.length; l++)
				{
					lines.add(additionalWraps[l]);
				}
			}
			return (String[])lines.toArray();
		}
		
		/** Breaks strings at the \n mark */
		private String[] breakString()
		{
			return null;
		}
			
		/** Helper function that determines the appropriate rendering width for this ap */
		private int determineAPWidth(boolean minMode)
		{
			if (minMode)
			{
				/*
				 * There are two main parts: determining the width of the critical row and modifier row.
				 * The larger of the two becomes the width of the ap, with a maximum of CARD_WIDTH
				 */
				FontMetrics metrics = pen.getFontMetrics();
				int costWidth = metrics.stringWidth(cardObj.cost);
				int costAndGapWidth = 0;
				if (costWidth > 0)
				{
					costAndGapWidth = costWidth + px(CRITICAL_GAP);
				}
				int usedWidth = px((2 * CRITICAL_PADDING_X)) + costAndGapWidth;
				int nameWidth = metrics.stringWidth(cardObj.name);
				if (nameWidth + usedWidth < CARD_WIDTH)
				{
					return nameWidth + usedWidth;
				}
				else
				{
					return CARD_WIDTH;
				}
			}
			else
			{
				return CARD_WIDTH;
			}
		}

		/** Helper function that determines the appropriate rendering height for this ap */
		private int determineAPHeight(boolean minMode)
		{
			if (minMode)
			{
				return 0;
			}
			else
			{
				return CARD_HEIGHT;
			}
		}
			
		/** Saves this visual */
		private void save(String svg, Path outputFolder)
		{
			//Write the string into an svg file
			Path fullOutputPath = Paths.get(outputFolder.toString(), fileName + IMAGE_EXTENSION);
			try 
			{
				FileWriter fw = new FileWriter(fullOutputPath.toString());
				fw.write(svg);
				System.out.println("> CardVisual saved to " + fullOutputPath);
				fw.close();
			} 
			catch (IOException e) {e.printStackTrace();}
		}
}
