package waffleoRai_cafebots;

import waffleoRai_cafebotCore.AbstractBot;
import waffleoRai_cafebotCore.BotBrain;
import waffleoRai_cafebotCore.BotConstructor;

public class CafemasterRed extends AbstractBot{
	
	public static final String VERSION = "1.0.0 [18.06.19]";

	public CafemasterRed(String Token, String InitKey, BotBrain core, int localIndex)
	{
		super.setToken(Token);
		super.setVersionString(VERSION);
		super.setXMLInitKey(InitKey);
		super.instantiateInternals();
		super.brain = core;
		super.setLocalIndex(localIndex);
		super.addDisconnectListener();
	}
	
	public int getConstructorType() {
		return BotConstructor.CAFEMASTER_RED;
	}
	
	//Disable:
		//Role notifications that aren't type -Major-
		//Event reminders (all)
		//Role management
		//Admin actions (role/actor)
		//Pending role/event display
	
	

}
