package waffleoRai_cafebotCommands;

public class MessageID {
	
	private long messageUID;
	private long channelUID;
	
	public MessageID(long msg, long chan)
	{
		messageUID = msg;
		channelUID = chan;
	}
	
	public long getMessageID()
	{
		return messageUID;
	}
	
	public long getChannelID()
	{
		return channelUID;
	}
	

}
