package waffleoRai_cafebotCore;

public class BotDeferException extends RuntimeException{

	private static final long serialVersionUID = -802217788520918553L;

	private int constructorType;
	
	public BotDeferException(int botType)
	{
		constructorType = botType;
	}
	
	public int getRequestedBotType()
	{
		return constructorType;
	}
	
}
