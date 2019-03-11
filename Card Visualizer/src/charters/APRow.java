package charters;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

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
//-----------------------------------------PUBLIC INTERFACE-------------------------------------//
	/** The drawn row will clip inside this shape */
	public Area clip;
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
	}
	
	/**
	 * The main function of this class.
	 * Renders the row onto the provided graphics
	 * @param pen - the graphics to render onto
	 * @param x - position inside pen to draw at
	 * @param y - position inside pen to draw at
	 * @return - the height of the row
	 */
	public int draw(Graphics2D pen, int x, int y)
	{
		int returnHeight = 0;
		System.out.println("Drawing APRow");
		//Translate the pen to make it easier to draw
		pen.translate(x, y);
		
		boolean emptyLeft = isEmpty(leftText);
		boolean emptyRight = isEmpty(rightText);

		Textangle lTangle = new Textangle(pen, leftText);
		Textangle rTangle = new Textangle(pen, rightText);
		
		if (!emptyLeft)
		{
			lTangle.fillColor = fillColor;
			lTangle.strokeColor = strokeColor;
			lTangle.clip = clip;
			lTangle.leftJustified = true;
			int lWidth = px(divisor);
			System.out.println("lWidth: " + lWidth);
			lTangle.setWidth(px(divisor));
			lTangle.setLeftPad(lPad);
			lTangle.setTopPad(tPad);
			lTangle.setRightPad(gap/2);
			lTangle.setBottomPad(bPad);
		}
		if (!emptyRight)
		{
			//Moves the clip over to the other side so that the rTangle gets clipped for the other corner
			if (clip != null)
			{
				rTangle.clip = clip.createTransformedArea(AffineTransform.getTranslateInstance(-px(divisor), 0));
			}
			rTangle.setWidth(px(1 - divisor));
			rTangle.leftJustified = false;
			rTangle.setLeftPad(gap/2);
			rTangle.setTopPad(tPad);
			rTangle.setRightPad(rPad);
			rTangle.setBottomPad(bPad);
		}
		
		if (!emptyLeft && emptyRight)
		{
			System.out.println("Left, Empty Right");
			//lTangle takes up whole width
			lTangle.setWidth(width);
			lTangle.setRightPad(rPad);
			lTangle.draw(0, 0);
			returnHeight = lTangle.height();
		}
		else if (!emptyRight && emptyLeft)
		{
			System.out.println("Right, Empty Left");
			//rTangle takes up whole width
			rTangle.setWidth(width);
			rTangle.setLeftPad(lPad);
			rTangle.draw(0, 0);
			returnHeight = rTangle.height();
		}
		else if (!emptyRight && !emptyLeft)
		{
			System.out.println("Left and right have contents");
			lTangle.setWidth(px(divisor));
			rTangle.setWidth(px(1 - divisor));
			int bigHeight = Math.max(lTangle.height(), rTangle.height());
			lTangle.setOverrideHeight(bigHeight);
			rTangle.setOverrideHeight(bigHeight);
			lTangle.draw(0, 0);
			rTangle.draw(px(divisor), 0);
			returnHeight = bigHeight;
		}
		
		//Un-translate the pen
		pen.translate(-x, -y);
		return returnHeight;
	}
	
	public void setLeftPad(double percent) {lPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setRightPad(double percent) {rPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setTopPad(double percent) {tPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setBottomPad(double percent) {bPad = Math.abs(percent) - (int)Math.abs(percent);}
	public void setDivisor(double percent) {divisor = Math.abs(percent) - (int)Math.abs(percent);}
	public void setGap(double percent) {gap = Math.abs(percent) - (int)Math.abs(percent);}
	public void setWidth(int units) {width = Math.abs(units);};
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
}
