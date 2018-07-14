package waffleoRai_cafebots;

import waffleoRai_cafebotCore.AbstractBot;
import waffleoRai_cafebotCore.BotBrain;
import waffleoRai_cafebotCore.BotConstructor;

public class CafemasterWhite extends AbstractBot{
	
	public static final String VERSION = "1.0.0 [18.06.19]";
	
	public CafemasterWhite(String Token, String InitKey, BotBrain core, int localIndex)
	{
		super.setToken(Token);
		super.setVersionString(VERSION);
		super.setXMLInitKey(InitKey);
		super.instantiateListenerList();
		super.brain = core;
		super.setLocalIndex(localIndex);
	}
	
	public int getConstructorType() {
		return BotConstructor.CAFEMASTER_WHITE;
	}

}
