package waffleoRai_cafebotCore;

import java.util.LinkedList;
import java.util.List;

public class BotSet {
	
	private List<Bot> bots;
	
	public BotSet()
	{
		bots = new LinkedList<Bot>();
	}
	
	public List<Bot> getBots()
	{
		List<Bot> copy = new LinkedList<Bot>();
		copy.addAll(bots);
		return bots;
	}
	
	public void addBot(Bot b)
	{
		bots.add(b);
	}

}
