package charters;

import java.util.ArrayList;

public class CardObject 
{
	//Fields
	public String name;
	public boolean isAbility;
	public String types;
	public String cost;
	public ArrayList<String> aspects;
	public ArrayList<String> modifiers;
	public Art art;
	public ArrayList<String> contentsText;
	public ArrayList<CardObject> contentsSubs;
	public Extras extras;
	
	//Functions
	public CardObject()
	{
		name = null;
		isAbility = false;
		types = null;
		cost = "";
		types = "";
		aspects = new ArrayList<String>();
		modifiers = new ArrayList<String>();
		art = null;
		contentsText = new ArrayList<String>();
		contentsSubs = new ArrayList<CardObject>();
		extras = null;
	}
	
	/**
	 * Call this function after reading the card from json
	 * Ensures that the card is ready to be visualized
	 */
	public void clean()
	{
		if (cost.equals("")) cost = null; //Blank cost -> No cost
		if (types.equals("")) types = null; //Blank types -> No types
	}
	
	public String toString()
	{
		String ret = "";
		for (int i = 0; i < 40; i++) {ret = ret + "-";}
		ret = ret + "\nName: " + name;
		ret = ret + "\nIsAbility: " + isAbility;
		ret = ret + "\nTypes: " + types;
		ret = ret + "\nCost: " + cost;
		ret = ret + "\nAspect(s): ";
		for (String as : aspects)
		{
			ret = ret + "[" + as + "]";
		}
		ret = ret + "\nModifier(s): ";
		for (String mod : modifiers)
		{
			ret = ret + mod;
		}
		ret = ret + "\nText: ";
		for (String text : contentsText)
		{
			ret = ret + text;
		}
		ret = ret + "\n";
		for (CardObject c : contentsSubs)
		{
			ret = ret + "\n>>>" + c + "\n";
		}
		for (int i = 0; i < 40; i++) {ret = ret + "-";}
		return ret;
	}
}
