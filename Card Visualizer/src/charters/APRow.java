package charters;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import visual.Textangle;

/**
 * A class that manages the drawing of a row of a charters ap.
 */
public class APRow
{
	private int height;
	private double lPad;
	private double tPad;
	private double rPad;
	private double bPad;
	/** The relative gap between the left and right side. Only enforced if both sides are not empty */
	private double gap;
	/** The percentage between the left and side sides */
	private double divisor;
	/** The width is fixed in this version */
	private int width; 
	/** If set not to -1, the height cannot exceed this. */
	private int maxHeight = -1;
	/** If set to > 0, the height cannot be below this. */
	private int minHeight = -1;
	/** The drawn row will clip inside this shape */
	private Area clip;
//-----------------------------------------PUBLIC INTERFACE-------------------------------------//
	public Color leftColor, rightColor, fillColor, strokeColor;
	public AttributedString leftText, rightText;
	public APRow()
	{
		lPad = tPad = rPad = bPad = 0;
		gap = 0;
		divisor = 0.5;
		clip = null;
		this.leftColor = Color.BLACK;
		this.rightColor = Color.BLACK;
		this.fillColor = Color.WHITE;
		this.strokeColor = Color.BLACK;
		maxHeight = -1;
	}
	
	/**
	 * The main function of this class.
	 * Renders the row onto the provided graphics
	 * @param pen - the graphics to render onto
	 * @param x - position inside pen to draw at
	 * @param y - position inside pen to draw at
	 * @return - the height of the row
	 */
	public int draw(Graphics2D pen, int bx, int by)
	{
		int returnHeight = 0;
		print("Drawing APRow");
		
		boolean emptyLeft = isEmpty(leftText);
		boolean emptyRight = isEmpty(rightText);

		Textangle lTangle = new Textangle(pen, leftText);
		Textangle rTangle = new Textangle(pen, rightText);
		
		if (!emptyLeft)
		{
			lTangle.fillColor = fillColor;
			lTangle.strokeColor = strokeColor;
			lTangle.setClip(clip);
			lTangle.leftJustified = true;
			int lWidth = px(divisor);
			print("lWidth: " + lWidth);
			lTangle.setWidth(px(divisor));
			lTangle.setLeftPad(lPad);
			lTangle.setTopPad(tPad);
			lTangle.setRightPad(gap/2);
			lTangle.setBottomPad(bPad);
			lTangle.setMaxHeight(maxHeight);
			lTangle.setMinHeight(minHeight);
		}
		if (!emptyRight)
		{
			//Moves the clip over to the other side so that the rTangle gets clipped for the other corner
			//if (clip != null)
			//{
			//	rTangle.clip = clip.createTransformedArea(AffineTransform.getTranslateInstance(-px(divisor), 0));
			//}
			rTangle.setClip(clip);
			rTangle.setWidth(px(1 - divisor));
			rTangle.leftJustified = false;
			rTangle.setLeftPad(gap/2);
			rTangle.setTopPad(tPad);
			rTangle.setRightPad(rPad);
			rTangle.setBottomPad(bPad);
			print("maxHeight: ");
			print("    " + maxHeight);
			rTangle.setMaxHeight(maxHeight);
			rTangle.setMinHeight(minHeight);
		}
		
		if (!emptyLeft && emptyRight)
		{
			print("Left, Empty Right");
			//lTangle takes up whole width
			lTangle.setWidth(width);
			lTangle.setRightPad(rPad);
			lTangle.draw(bx, by);
			returnHeight = lTangle.height();
		}
		else if (!emptyRight && emptyLeft)
		{
			print("Right, Empty Left");
			//rTangle takes up whole width
			rTangle.setWidth(width);
			rTangle.setLeftPad(lPad);
			rTangle.draw(bx, by);
			returnHeight = rTangle.height();
		}
		else if (!emptyRight && !emptyLeft)
		{
			print("Left and right have contents");
			lTangle.setWidth(px(divisor));
			rTangle.setWidth(px(1 - divisor));
			int bigHeight = Math.max(lTangle.height(), rTangle.height());
			lTangle.setOverrideHeight(bigHeight);
			rTangle.setOverrideHeight(bigHeight);
			lTangle.draw(bx, by);
			rTangle.draw(bx + px(divisor), by);
			returnHeight = bigHeight;
		}
		
		return returnHeight;
	}
	public void setClip(Area a) {this.clip = (a != null) ? (Area)a.clone() : null;}
	public void setLeftPad(double percent) {lPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setRightPad(double percent) {rPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setTopPad(double percent) {tPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setBottomPad(double percent) {bPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setDivisor(double percent) {divisor = Math.abs(percent) - (int)Math.abs(percent);}
	public void setGap(double percent) {gap = Math.abs(percent) - (int)Math.abs(percent);}
	public void setWidth(int units) {width = Math.abs(units);};
	public void setMaxHeight(int units) {maxHeight = Math.abs(units);}
	public void setMinHeight(int units) {minHeight = Math.abs(units);}
	public void setOverrideHeight(int units) {setMaxHeight(units); setMinHeight(units);}
	/** fx = fraction of x (width) */
	//public double fx(double percentBig, int sizeBig) {return (sizeBig * percentBig)/width;}
//-----------------------------------------PRIVATE FUNCTIONS-------------------------------------//	
	private int px(double p) {return (int)Math.abs(p * width);}
	private int py(double p) {return (int)Math.abs(p * height);}
	private boolean isEmpty(AttributedString s) 
	{
		if (s == null)
		{
			return true;
		}
		else
		{
			AttributedCharacterIterator i = s.getIterator();
			String contents = "";
			while (i.getIndex() < i.getEndIndex())
			{
				contents += i.next();
			}
			return contents.equals("");
		}
	}
	/** Helper function for clean printing */
	private static void print(Object o) {System.out.println("[APRow] " + o);}
}
