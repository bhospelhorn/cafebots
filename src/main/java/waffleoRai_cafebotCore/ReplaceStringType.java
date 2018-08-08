package waffleoRai_cafebotCore;

public enum ReplaceStringType {
	
	
	REQUSER("%r"),
	REQUSER_MENTION("%R"),
	TARGUSER("%u"),
	TARGUSER_MENTION("%U"),
	CHANNEL("%c"),
	CHANNEL_MENTION("%C"),
	ROLE("%o"),
	ROLE_MENTION("%O"),
	
	TIME("%T"),
	TIME_NOTZ("%t"),
	YEAR("%Y"),
	MONTH_NAME("%M"),
	MONTH("%m"),
	DAYOFWEEK("%D"),
	DAYOFMONTH("%d"),
	NTH("%I"),
	TIMEONLY("%i"),
	TIMEZONE("%z"),
	
	EVENTENTRY("%E"),
	EVENTNAME("%e"),
	EVENTTYPE("%y"),
	EVENTLEVEL("%l"),
	
	GENERALNUM("%n"),
	ALTNUM("%N"),
	GUILDNAME("%G"),
	BOTNAME("%s"),
	
	FORMATTED_TIME_RELATIVE("%F"),
	FORMATTED_TIME_LEFT("%f"),
	
	PLACEHOLDER_1("%P1"),
	PLACEHOLDER_2("%P2"),
	PLACEHOLDER_3("%P3"),;
	
	private CharSequence string;
	
	private ReplaceStringType(CharSequence s)
	{
		string = s;
	}
	
	public CharSequence getString()
	{
		return string;
	}

}
