package charters;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUnits;

/**
 * Class for a fixed-width rectangle that wraps text to fit inside it
 * @author Benjamin
 */
public class TextangleOld 
{
	public static void main(String... args)
	{
		final String PATH = "D:\\Code\\Charters Card Generator\\Java Card Visualizer\\Card Visualizer\\test.svg";
		SVGGraphics2D g = new SVGGraphics2D(1000, 1000, SVGUnits.PX);
		g.setFont(new Font("Consolas", Font.PLAIN, 30));
		g.setStroke(new BasicStroke(2));
		Ellipse2D.Double el = new Ellipse2D.Double(-50, -50, 200, 200);
		TextangleOld box = new TextangleOld();
		box.clip = new Area(el);
		box.setWidth(500);
		box.text = "Reeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee";
		box.setLeftPad(0.2);
		box.setRightPad(0.2);
		box.setTopPad(0.2);
		box.setBottomPad(0.2);
		box.draw(g, 50, 50);
		try 
		{
			FileWriter fw = new FileWriter(PATH);
			fw.write(g.getSVGElement());
			fw.close();
		} 
		catch (IOException e) {e.printStackTrace();}
	}
//-----------------------------------------------FIELDS-----------------------------------------------//
	private double lPad;
	private double tPad;
	private double rPad;
	private double bPad;
	private int height;
	private int width;
//-----------------------------------------------PUBLIC INTERFACE-----------------------------------------------//
	public String text;
	public Color textColor, strokeColor, fillColor;
	public Area clip;
	public TextangleOld()
	{
		height = 100;
		textColor = Color.BLACK;
		strokeColor = Color.BLACK;
		fillColor = Color.WHITE;
	}
	public void draw(Graphics2D pen, int x, int y)
	{
		//Translate the pen to the corner to make things easier
		pen.translate(x, y);
		//Text width is the available space minus the pads
		int textWidth = (int)(width * (1 - lPad - rPad));
		//Wrap the text string to fit inside the width
		String[] rows = completelyWrapString(text, pen.getFontMetrics(), textWidth);
		int strHeight = rows.length * pen.getFontMetrics().getHeight();
		height = (int)(strHeight/(1 - tPad - bPad));
		Area rect = new Area(new Rectangle2D.Double
		(
			0,
			0,
			width,
			height
		));
		if (clip != null) {rect.intersect(clip);};
		pen.setPaint(fillColor);
		pen.fill(rect);
		pen.setPaint(strokeColor);
		pen.draw(rect);
		for (int r = 0; r < rows.length; r++)
		{
			pen.drawString(rows[r], (int)(width * lPad), (int)(height * tPad) + (1 + r) * pen.getFontMetrics().getHeight());
		}
		//Translate the pen back
		pen.translate(-x, -y);
	}
	public void setLeftPad(double percent) {lPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setRightPad(double percent) {lPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setTopPad(double percent) {lPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setBottomPad(double percent) {lPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setWidth(int units) {width = Math.abs(units);};
	/** Wraps strings once to fit inside a width */
	private String[] wrapString(String in, FontMetrics fm, double width)
	{
		int cumulativeWidth = 0;
		String[] out = {in}; //Only remains the base string if no wrapping occurs
		
		for (int j = 0; j < in.length(); j++)
		{
			System.out.println("Cumulative Width: " + cumulativeWidth);
			if (cumulativeWidth >= width && j != 0)
			{
				//Split the string into rows
				out = new String[2];
				out[0] = in.substring(0, j - 1);
				out[1] = in.substring(j, in.length() - 1);
				return out;
			}
			cumulativeWidth += fm.stringWidth("" + in.charAt(j));
		}
		return out;
	}

	/** Wraps strings to fit inside a width */
	private String[] completelyWrapString(String in, FontMetrics fm, double width)
	{
		ArrayList<String> lines = new ArrayList<String>();
		String[] oneWrap = wrapString(in, fm, width);
		lines.add(oneWrap[0]);
		if (oneWrap.length > 1)
		{
			//Continue wrapping
			lines.addAll(Arrays.asList(completelyWrapString(oneWrap[1], fm, width)));
		}
		return lines.toArray(new String[lines.size()]);
	}
//-----------------------------------------------PRIVATE METHODS-----------------------------------------------//
}
