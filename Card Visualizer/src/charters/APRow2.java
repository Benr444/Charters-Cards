package charters;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * A class that manages the drawing of a row of a charters ap.
 * The right-side of the row has priority over the left side: 
 * the left side will wrap and the right-side will wrap and the right will not
 */
public class APRow2
{
	/** The font for the contents of this row to be written in */
	private FontMetrics metrics;
	/** The row cannot expand past this limit. -1 means no limit */
	private double maxH;
	/** The row cannot expand past this limit. -1 means no limit */
	private double maxW;
	/* The pad variables prevent anything from being drawn inside
	 * Pads are used relative to the the respective dimension of the row
	 */
	private double lPad;
	private double tPad;
	private double rPad;
	private double bPad;
	/** The relative gap between the left and right side. Only enforced if both sides are not empty */
	private double potentialGap;
	/** The left-side contents of this row. Each element is a string line */
	private ArrayList<String> leftContents;
	/** The right-side contents of this row. Each element is a string line */
	private ArrayList<String> rightContents;
	/** The drawn row will clip inside this shape */
	private Area clip;
	private Color leftColor, rightColor, fillColor, strokeColor;
	
//-----------------------------------------PUBLIC FUNCTIONS-------------------------------------//
	
	public APRow2(FontMetrics metrics, double maxW, double maxH)
	{
		this.metrics = metrics;
		this.maxW = maxW;
		this.maxH = maxH;
		lPad = tPad = rPad = bPad = 0;
		potentialGap = 0;
		leftContents = new ArrayList<String>();
		rightContents = new ArrayList<String>();
		clip = null;
		this.leftColor = Color.BLACK;
		this.rightColor = Color.BLACK;
		this.fillColor = Color.WHITE;
		this.strokeColor = Color.BLACK;
	}

	public APRow2(FontMetrics metrics)
	{
		this(metrics, -1, -1);
	}
	
	/**
	 * The main function of this class.
	 * Renders the row onto the provided graphics
	 * @param pen - the graphics to render onto
	 * @param x - position inside pen to draw at
	 * @param y - position inside pen to draw at
	 */
	public void draw(int x, int y, Graphics2D pen)
	{
		//Translate the pen to make it easier to draw
		pen.translate(x, y);
		
		//Wrap the left string
		wrapLeft();
		Rectangle2D.Double rect = new Rectangle2D.Double
		(
			0,
			0,
			width(),
			height()
		);
		iFill(pen, rect, clip);
		iDraw(pen, rect, clip);
		String rs = totalRightContents();
		int displacement = metrics.stringWidth(rs);
		System.out.println("disp:" + displacement);
		int dx = (int)(width() - px(rPad) - displacement);
		int dy = (int)(height() - py(bPad));
		System.out.println("Drew R Contents x:" + dx + " y:" + dy);
		pen.drawString(rs, dx, dy);
		String ls = totalLeftContents();
		int lHeight = leftContents.size() * metrics.getHeight();
		for (int l = 0; l < leftContents.size(); l++)
		{
			System.out.println("Left Contents Line: " + leftContents.get(l));
			pen.drawString
			(
				leftContents.get(l),
				(int)(px(lPad)),
				(int)(height() - py(bPad) - lHeight + (l * metrics.getHeight()))
			);
		}
		
		//Un-translate the pen
		pen.translate(-x, -y);
	}
	
	/**
	 * Each parameter is a fraction (from 0 to 1) of the width or height of the row to pad
	 * Input values will be contorted to fit that constraint
	 */
	public void setPads(double l, double t, double r, double b)
	{
		//Pads must be from 0-1
		lPad = Math.abs(l) - (int)Math.abs(l);
		tPad = Math.abs(t) - (int)Math.abs(t);
		rPad = Math.abs(r) - (int)Math.abs(r);
		bPad = Math.abs(b) - (int)Math.abs(b);
	}

	/**
	 * The parameter is a fraction (from 0 to 1) of the width or height of the row to pad
	 * The input value will be contorted to fit that constraint
	 */
	public void setGap(double gap)
	{
		this.potentialGap = Math.abs(gap) - (int)Math.abs(gap);
	}
	
	/** @param a - the area to clip from */
	public void setClip(Area a)
	{
		this.clip = a;
	}
	
	public void setColors(Color leftColor, Color rightColor, Color fillColor, Color strokeColor)
	{
		this.leftColor = leftColor;
		this.rightColor = rightColor;
		this.fillColor = fillColor;
		this.strokeColor = strokeColor;
	}
	
	/**
	 * Sets the left-side contents of this row
	 * @param leftContent - contents
	 */
	public void setLeftContents(String leftContent)
	{
		this.leftContents = new ArrayList<String>();
		this.leftContents.add(leftContent);
	}
	
	/**
	 * Sets the right-side contents of this row
	 * @param rightContent - contents
	 */
	public void setRightContents(String rightContent)
	{
		this.rightContents = new ArrayList<String>();
		this.rightContents.add(rightContent);
	}

//-----------------------------------------PRIVATE FUNCTIONS-------------------------------------//	
	private double px(double p) {return Math.abs(p * width());}
	private double py(double p) {return Math.abs(p * height());}
	
	/**
	 * @param pen - what to draw with
	 * @param shape - what to draw
	 * @param intersect - the bounding of the drawing
	 * */
	private void iFill(Graphics2D pen, Shape shape, Area intersect)
	{
		pen.setColor(fillColor);
		Area sArea = new Area(shape);
		if (intersect != null)
		{
			sArea.intersect(intersect);
		}
		pen.fill(sArea);
	}
	
	/**
	 * @param pen - what to draw with
	 * @param shape - what to draw
	 * @param intersect - the bounding of the drawing
	 * */
	private void iDraw(Graphics2D pen, Shape shape, Area intersect)
	{
		pen.setColor(strokeColor);
		Area sArea = new Area(shape);
		if (intersect != null)
		{
			sArea.intersect(intersect);
		}
		pen.draw(sArea);
	}
	
	/**
	 * Splits the leftContents into rows based on the available space
	 */
	private void wrapLeft()
	{
		leftContents = completelyWrapString(totalLeftContents(), openLeftSpace());
	}
	
	/** Wraps strings once to fit inside a width */
	private String[] wrapString(String in, double width)
	{
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
			cumulativeWidth += metrics.stringWidth("" + in.charAt(j));
		}
		return out;
	}

	/** Wraps strings to fit inside a width */
	private ArrayList<String> completelyWrapString(String in, double width)
	{
		ArrayList<String> lines = new ArrayList<String>();
		String[] oneWrap = wrapString(in, width);
		lines.add(oneWrap[0]);
		if (oneWrap.length > 1)
		{
			//Continue wrapping
			lines.addAll(completelyWrapString(oneWrap[1], width));
		}
		return lines;
	}
	
	private double width()
		{return maxW;}
	
	private double height()
	{
		double ret = maxH * (tPad + bPad);
		return maxH;
	}
	
	/**
	 * @return - A single string with all of the left contents in it
	 */
	private String totalLeftContents()
	{
		String ret = "";
		for (String l : leftContents)
		{
			ret = ret + l;
		}
		return ret;
	}

	private boolean hasLeftContents()
		{return totalLeftContents() != "";}
	
	private double openLeftSpace() 
		{return width() * (1 - gap() - lPad - rPad) - getRightContentsWidth();}
	
	/**
	 * @return - A single string with all of the right contents in it
	 */
	private String totalRightContents()
	{
		String ret = "";
		for (String r : rightContents)
		{
			ret = ret + r;
		}
		return ret;
	}
	
	private boolean hasRightContents()
		{return totalRightContents() != "";}
	
	/**
	 * @return - the width taken by the right side contents
	 */
	private double getRightContentsWidth()
		{return metrics.stringWidth(totalRightContents());}
	
	/** @return true if the gap is enabled */
	private boolean gapEnabled()
		{return (hasLeftContents() && hasRightContents());}
	
	/**
	 * @return - the gap. Will be 0 if the gap is not enabled
	 */
	private Double gap()
		{return gapEnabled() ? potentialGap : 0;}
}
