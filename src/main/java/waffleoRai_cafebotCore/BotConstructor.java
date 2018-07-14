package waffleoRai_cafebotCore;

import waffleoRai_cafebots.CafebotBlue;
import waffleoRai_cafebots.CafebotGreen;
import waffleoRai_cafebots.CafebotPink;
import waffleoRai_cafebots.CafebotPurple;
import waffleoRai_cafebots.CafebotRed;
import waffleoRai_cafebots.CafebotWhite;
import waffleoRai_cafebots.CafebotYellow;
import waffleoRai_cafebots.CafemasterRed;
import waffleoRai_cafebots.CafemasterWhite;
import waffleoRai_cafebots.VanillaBot;

public class BotConstructor {
	
	public static final int VANILLA = 0;
	public static final int CAFEMASTER_RED = 1;
	public static final int CAFEMASTER_WHITE = 2;
	public static final int CAFEBOT_WHITE = 3;
	public static final int CAFEBOT_RED = 4;
	public static final int CAFEBOT_PURPLE = 5;
	public static final int CAFEBOT_YELLOW = 6;
	public static final int CAFEBOT_GREEN = 7;
	public static final int CAFEBOT_BLUE = 8;
	public static final int CAFEBOT_PINK = 9;
	
	public static AbstractBot makeBot(Bot initbot, BotBrain brain, int i)
	{
		int type = initbot.getConstructorType();
		AbstractBot mybot = null;
		switch(type)
		{
		case VANILLA:
			mybot = new VanillaBot(initbot.getToken(), initbot.getXMLKey(), brain, i);
			break;
		case CAFEMASTER_RED:
			mybot = new CafemasterRed(initbot.getToken(), initbot.getXMLKey(), brain, i);
			break;
		case CAFEMASTER_WHITE:
			mybot = new CafemasterWhite(initbot.getToken(), initbot.getXMLKey(), brain, i);
			break;
		case CAFEBOT_WHITE:
			mybot = new CafebotWhite(initbot.getToken(), initbot.getXMLKey(), brain, i);
			break;
		case CAFEBOT_RED:
			mybot = new CafebotRed(initbot.getToken(), initbot.getXMLKey(), brain, i);
			break;
		case CAFEBOT_PURPLE:
			mybot = new CafebotPurple(initbot.getToken(), initbot.getXMLKey(), brain, i);
			break;
		case CAFEBOT_YELLOW:
			mybot = new CafebotYellow(initbot.getToken(), initbot.getXMLKey(), brain, i);
			break;
		case CAFEBOT_GREEN:
			mybot = new CafebotGreen(initbot.getToken(), initbot.getXMLKey(), brain, i);
			break;
		case CAFEBOT_BLUE:
			mybot = new CafebotBlue(initbot.getToken(), initbot.getXMLKey(), brain, i);
			break;
		case CAFEBOT_PINK:
			mybot = new CafebotPink(initbot.getToken(), initbot.getXMLKey(), brain, i);
			break;
		}
		return mybot;
	}
	
	public static AbstractBot makeVanillaBot(Bot initbot, BotBrain brain, int i)
	{
		return new VanillaBot(initbot.getToken(), initbot.getXMLKey(), brain, i);
	}
	
	public static String getVersion(int constructorType)
	{
		switch(constructorType)
		{
		case VANILLA:
			return VanillaBot.VERSION;
		case CAFEMASTER_RED:
			return CafemasterRed.VERSION;
		case CAFEMASTER_WHITE:
			return CafemasterWhite.VERSION;
		case CAFEBOT_WHITE:
			return CafebotWhite.VERSION;
		case CAFEBOT_RED:
			return CafebotRed.VERSION;
		case CAFEBOT_PURPLE:
			return CafebotPurple.VERSION;
		case CAFEBOT_YELLOW:
			return CafebotYellow.VERSION;
		case CAFEBOT_GREEN:
			return CafebotGreen.VERSION;
		case CAFEBOT_BLUE:
			return CafebotBlue.VERSION;
		case CAFEBOT_PINK:
			return CafebotPink.VERSION;
		}
		
		return "";
	}

}
