package charters;

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
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.ArrayList;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUnits;

/**
 * Class for a fixed-width rectangle that wraps text to fit inside it
 * @author Benjamin
 */
public class Textangle 
{
	public static void main(String... args)
	{
		final String PATH = "D:\\Code\\Charters Card Generator\\Java Card Visualizer\\Card Visualizer\\test.svg";
		SVGGraphics2D g = new SVGGraphics2D(1000, 1000, SVGUnits.PX);
		g.setFont(new Font("Consolas", Font.PLAIN, 20));
		g.setStroke(new BasicStroke(2));
		AttributedString t = new AttributedString
		(
			"The quick brown fox jumped over the lazy dog." 
			+ " Sphinx of black quartz, judge my vow."
		);
		t.addAttribute(TextAttribute.FONT, g.getFont());
		//Ellipse2D.Double el = new Ellipse2D.Double(-50, -50, 500, 500);
		Textangle box = new Textangle(g, t);
		//box.clip = new Area(el);
		box.leftJustified = true;
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
	private double lPad;
	private double tPad;
	private double rPad;
	private double bPad;
	private int width;
	/** If set, the height of the box will always be this. */
	private Integer overrideHeight;
	private AttributedString text;
	/** Used to store the generated layouts */
//-----------------------------------------------PUBLIC INTERFACE-----------------------------------------------//
	public Color textColor, strokeColor, fillColor;
	public Area clip;
	/** Justification of text */
	public boolean leftJustified;
	public Textangle(Graphics2D pen, AttributedString text)
	{
		this.pen = pen;
		this.leftJustified = true;
		if (text == null)
		{
			this.text = new AttributedString("");
		}
		else
		{
			this.text = text;
		}
		textColor = Color.BLACK;
		strokeColor = Color.BLACK;
		fillColor = Color.WHITE;
	}
	public void draw(int x, int y)
	{
		System.out.println("Drawing Textangle...");
		//Translate the pen to the corner to make things easier
		pen.translate(x, y);
		System.out.println("Textangle Height: " + height());
		System.out.println("Textangle Width: " + width);
		Area rect = new Area(new Rectangle2D.Double
		(
			0,
			0,
			width,
			height()
		));
		if (clip != null) {rect.intersect(clip);};
		pen.setPaint(fillColor);
		pen.fill(rect);
		pen.setPaint(textColor);
		ArrayList<TextLayout> layouts = generateLayouts(pen);
		for (int i = 0; i < layouts.size(); i++)
		{
			TextLayout lay = layouts.get(i);
			pen.setClip(clip);
			int dy = (int)(py(tPad) + lay.getAscent() + i * (lay.getAscent() + lay.getDescent() + lay.getLeading()));
			if (leftJustified)
			{
				System.out.println("Rendering a leftJust layout.");
				lay.draw(pen, px(lPad), dy);
			}
			else
			{
				System.out.println("Rendering a rightJust layout.");
				lay.draw(pen, px(1 - rPad) - (int)lay.getBounds().getWidth(), dy);
			}
		}
		pen.setPaint(strokeColor);
		pen.draw(rect);
		//Translate the pen back
		pen.translate(-x, -y);
	}
	public double fx(int opx) {return (double)opx/(double)width;}
	public double fy(int opy) {return (double)opy/(double)height();}
	public void setLeftPad(double percent) {lPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setRightPad(double percent) {rPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setTopPad(double percent) {tPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setBottomPad(double percent) {bPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setWidth(int units) {width = Math.abs(units);};
	public void setOverrideHeight(int units) {overrideHeight = Math.abs(units);};
	/** Returns the actual total height of the box */
	public int height() 
	{
		if (overrideHeight != null)
		{
			return overrideHeight;
		}
		else
		{
			int textHeight = 0;
			ArrayList<TextLayout> layouts = generateLayouts(pen);
			for (TextLayout t : layouts)
			{
				textHeight += t.getAscent() + t.getDescent() + t.getLeading();
			}
			return (int)((textHeight)/(1 - tPad - bPad));
		}
	};
//-----------------------------------------------PRIVATE METHODS-----------------------------------------------//
	private int px(double percent) {return (int)Math.abs(width * percent);}
	private int py(double percent) {return (int)Math.abs(height() * percent);}
	/** Used to update the layout lists */
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
}
