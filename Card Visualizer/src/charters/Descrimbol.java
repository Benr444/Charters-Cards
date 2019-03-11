package charters;

public abstract class Descrimbol 
{	
	public static class CharSymbol extends Descrimbol
	{
		public char symbol;
		public String toString()
		{
			return this.symbol + "";
		}
	}
	
	public static class AspectSymbol extends Descrimbol
	{
		public enum Aspect 
		{
			FIRE, WATER, EARTH, AIR, KNOWLEDGE, CHAOS, SACRED, ARCANE, MARTIAL, EXCHANGE;
		}
		
		public Aspect aspectAspect;
		public String aspect;
		public String toString()
		{
			return "[" + aspect + "]";
		}
	}
	
	public static class ValueSymbol extends Descrimbol
	{
		public int value;
		public String toString()
		{
			return "<" + value + ">";
		}
	}
}
