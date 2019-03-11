package charters;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * TODO: Non-string rendering
 */
public class DescrimbolLayout 
{
	private Descrimbol[][] rows;
	private Graphics2D pen;
	private FontMetrics metrics;

	public DescrimbolLayout(Graphics2D pen, Descrimbol[] descrimbols, int width)
	{
		this.pen = pen;
		this.metrics = pen.getFontMetrics(pen.getFont());
		int cumulativeWidth = 0;
		for (int i = 0; i < descrimbols.length; i++)
		{
			cumulativeWidth += descrimbolWidth(descrimbols[i]);
		}
	}
	
	public int getMaxHeight()
	{
		return 0;
	}
	
	public void drawAt(int x, int y)
	{
		int rowSpacing = 10;
		for (int h = 0; h < rows.length; h++) //For each row
		{
			int symbolSpacing = 10;
			for (int k = 0; k < rows[h].length; k++) //For each descrimbol
			{
				drawDescrimbol(rows[h][k], x + k * symbolSpacing, y + h * rowSpacing);
			}
		}
	}
	
	private int descrimbolWidth(Descrimbol d)
	{
		return metrics.stringWidth(d.toString());
	}
	
	private void drawDescrimbol(Descrimbol d, int x, int y)
	{
		pen.drawString(d.toString(), x, y);
	}
}
