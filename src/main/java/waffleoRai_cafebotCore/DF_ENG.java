package waffleoRai_cafebotCore;

import java.util.GregorianCalendar;

import waffleoRai_Utils.FileBuffer;

public class DF_ENG implements DateFormatter{
	
	public String formatTime(GregorianCalendar timestamp, boolean includeMillis, boolean includeTZ)
	{
		return FileBuffer.formatTimeAmerican(timestamp, includeMillis, includeTZ);
	}

	public String formatSequentialNumber(int n)
	{
		if (n % 10 == 1 && (n % 100 != 11)) return n + "st";
		if (n % 10 == 2 && (n % 100 != 12)) return n + "nd";
		if (n % 10 == 3 && (n % 100 != 13)) return n + "rd";
		return n + "th";
	}

}
