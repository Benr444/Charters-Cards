package visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.FileWriter;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.ArrayList;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUnits;

/**
 * Class for a fixed-width rectangle that wraps text to fit inside it
 * The class supports one AttributedString that it renders inside a rectangle
 * @author Benjamin
 */
public class Textangle 
{
//-----------------------------------------------TEST ZONE-----------------------------------------------//
	public static void main(String... args)
	{
		final String PATH = "test.svg";
		SVGGraphics2D g = new SVGGraphics2D(1000, 1000, SVGUnits.PX);
		g.setFont(new Font("Consolas", Font.PLAIN, 20));
		g.setStroke(new BasicStroke(2));
		AttributedString t = new AttributedString
		(
			"The quick brown fox jumped over the lazy dog." 
			+ " Sphinx of black quartz, judge my vow."
		);
		t.addAttribute(TextAttribute.FONT, g.getFont());
		t.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
		Ellipse2D.Double el = new Ellipse2D.Double(-50, -50, 500, 500);
		g.draw(el);
		Textangle box = new Textangle(g, t);
		box.clip = new Area(el);
		box.leftJustified = true;
		box.setOverrideHeight(100);
		box.setWidth(500);
		box.setLeftPad(0.25);
		box.setRightPad(0.25);
		box.setTopPad(0.25);
		box.setBottomPad(0.25);
		box.draw(50, 50);
		try 
		{
			FileWriter fw = new FileWriter(PATH);
			fw.write(g.getSVGElement());
			fw.close();
		} 
		catch (IOException e) {e.printStackTrace();}
	}
//-----------------------------------------------FIELDS-----------------------------------------------//
	/** Go ahead and save the pen because the height of the textangle depends so heavily on it */
	private Graphics2D pen;
	/** % of width on the left of the box where text cannot be */
	private double lPad;
	/** % of height on the top of the box where text cannot be */
	private double tPad;
	/** % of width on the right of the box where text cannot be */
	private double rPad;
	/** % of height on the bottom of the box where text cannot be */
	private double bPad;
	/** Must be set by the user in this version. Total width of the box */
	private int width;
	/** If set, the height of the box will always be this. */
	private int maxHeight = -1;
	/** If set > 0, the height cannot be less than this. */
	private int minHeight = -1;
	/** The string that is rendered inside this box */
	private AttributedString text;
	/** The shape used to to clip the box and text before drawing it. 
	 * Nothing will render outside this area */
	private Area clip = null;
//-----------------------------------------------PUBLIC INTERFACE-----------------------------------------------//
	/** The colors that are used to stroke and fill this box */
	public Color strokeColor = Color.BLACK, fillColor = Color.WHITE;
	/** Justification of text. true = left, false = right */
	public boolean leftJustified = true;
	/** Constructor */
	public Textangle(Graphics2D pen, AttributedString text)
	{
		this.pen = pen;
		this.leftJustified = true;
		if (text != null)
		{
			AttributedCharacterIterator aci = text.getIterator();
			String textContents = "";
			while (aci.getIndex() < aci.getEndIndex())
			{
				textContents = textContents + aci.next();
			}
			if (textContents.equals(""))
			{
				this.text = null;
			}
		}
		this.text = text;
	}
	/** Draw the textangle at the specified coordinate */
	public void draw(int bx, int by)
	{
		print("Drawing...");
		Area rect = new Area(new Rectangle2D.Double
		(
			bx,
			by,
			width,
			height()
		));
		if (clip != null) {rect.intersect(clip);};
		if (maxHeight != -1)
		{
			//If there is a maxHeight specified, clip everything beyond it
			Area maxBounding = new Area(new Rectangle2D.Double
			(
				bx, by, width, maxHeight
			));
			if (clip == null) {clip = maxBounding;}
			else {clip.intersect(maxBounding);}
			pen.setClip(clip);
		}
		print("Clip.x:" + clip.getBounds2D().getX());
		print("Clip.y:" + clip.getBounds2D().getY());
		print("Clip.w:" + clip.getBounds2D().getWidth());
		print("Clip.h:" + clip.getBounds2D().getHeight());
		pen.setPaint(fillColor);
		pen.fill(rect);
		if (text != null)
		{
			ArrayList<TextLayout> layouts = generateLayouts(pen);
			for (int i = 0; i < layouts.size(); i++)
			{
				TextLayout lay = layouts.get(i);
				pen.setClip(clip);
				int dy = (int)(py(tPad) + lay.getAscent() + i * (lay.getAscent() + lay.getDescent() + lay.getLeading()));
				if (leftJustified)
				{
					lay.draw(pen, bx + px(lPad), by + dy);
				}
				else
				{
					lay.draw(pen, bx + px(1 - rPad) - (int)lay.getBounds().getWidth(), by + dy);
				}
			}
		}
		else
		{
			print("No text/null text was provided. Drawing bounding only.");
		}
		pen.setPaint(strokeColor);
		pen.draw(rect);
		pen.setClip(null);
	}
	/** @return The fraction (%) of this width
	 *  @param opx = the units to calculate the fraction for */
	public double fx(int opx) {return (double)opx/(double)width;}
	
	/** @return The fraction (%) of this height
	 *  @param opy = the units to calculate the fraction for */
	public double fy(int opy) {return (double)opy/(double)height();}
	
	/** Sets the clip. Uses the copy constructor to be safe */
	public void setClip(Area a) {this.clip = (a != null) ? (Area)a.clone() : null;}
	
	/** @param percent = the % of this card that will serve as the left pad. Will be converted to be between 0 and +1. */
	public void setLeftPad(double percent) {lPad = Math.abs(percent) - (int)Math.abs(percent);}
	
	/** @param percent = the % of this card that will serve as the right pad. Will be converted to be between 0 and +1. */
	public void setRightPad(double percent) {rPad = Math.abs(percent) - (int)Math.abs(percent);}
	
	/** @param percent = the % of this card that will serve as the top pad. Will be converted to be between 0 and +1. */
	public void setTopPad(double percent) {tPad = Math.abs(percent) - (int)Math.abs(percent);}
	
	/** @param percent = the % of this card that will serve as the bottom pad. Will be converted to be between 0 and +1. */
	public void setBottomPad(double percent) {bPad = Math.abs(percent) - (int)Math.abs(percent);}
	
	/** @param units = the new total width of this textangle */
	public void setWidth(int units) {width = Math.abs(units);};

	/** The height must be this value. Same as setting min = max = units */
	public void setOverrideHeight(int units) {setMaxHeight(units); setMinHeight(units);}
	
	/** @param units = If set to less than 0, disables the maximum. If set to above 0, enables the maximum to that value */
	public void setMaxHeight(int units) {maxHeight = units < 0 ? -1 : units;}
	
	/** @param units = If set to less than 0, disables the minimum. If set to above 0, enables the minimum to that value */
	public void setMinHeight(int units) {minHeight = units < 0 ? -1 : units;}
	
	/** @return The actual total height of the box, in units */
	public int height() 
	{
		if (this.text == null)
		{
			return minHeight < 0 ? 0 : minHeight;
		}
		else
		{
			if (minHeight == maxHeight && minHeight != -1)
			{
				//Utter override scenario
				return minHeight;
			}
			else
			{
				int textHeight = 0;
				ArrayList<TextLayout> layouts = generateLayouts(pen);
				for (TextLayout t : layouts)
				{
					textHeight += t.getAscent() + t.getDescent() + t.getLeading();
				}
				int calculatedHeight = (int)((textHeight)/(1 - tPad - bPad));
				if (maxHeight != -1) //max specified
				{
					if (minHeight != -1) //min specified as well
					{
						print("min/max specified");
						return Math.max(minHeight, Math.min(calculatedHeight, maxHeight));
					}
					else //only max specified
					{
						print("max specified");
						return Math.min(calculatedHeight, maxHeight);
					}
				}
				else 
				{
					if (minHeight != -1) //only min specified
					{
						print("min specified");
						return Math.max(calculatedHeight, minHeight);
					}
					else //Neither specified
					{
						print("Neither min/max specified");
						return calculatedHeight;
					}
				}
			}
		}
	};
//-----------------------------------------------PRIVATE METHODS-----------------------------------------------//
	/** @return The pixels covered by the percentage of this box in the x-direction */
	private int px(double percent) {return (int)Math.abs(width * percent);}
	
	/** @return The pixels covered by the percentage of this box in the y-direction */
	private int py(double percent) {return (int)Math.abs(height() * percent);}
	
	/** Subdivides the text into rows based on current width, etc parameters. 
	 * @return The rows of text */
	private ArrayList<TextLayout> generateLayouts(Graphics2D pen)
	{
		ArrayList<TextLayout> ret = new ArrayList<TextLayout>();
		//Wrap the text string to fit inside the width
		LineBreakMeasurer measurer = new LineBreakMeasurer
		(
			text.getIterator(),
			BreakIterator.getLineInstance(),
			pen.getFontRenderContext()
		);
		while (true)
		{
			TextLayout tempLay = measurer.nextLayout(textWidth());
			if (tempLay == null)
			{
				break;
			}
			else
			{
				ret.add(tempLay);
			}
		}
		return ret;
	}
	
	/** Returns the width of the box available for text */
	public int textWidth()
	{
		//Text width is the available space minus the pads
		return (int)(width * (1 - lPad - rPad));
	}
	
	/** Helper method to print in a better format */
	public static void print(Object o) {System.out.println("[Textangle] " + o);}
}
