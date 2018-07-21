package waffleoRai_cafebotCore;

import java.util.GregorianCalendar;
import java.util.TimeZone;

public interface DateFormatter {
	
	public String formatTime(GregorianCalendar timestamp, boolean includeMillis, boolean includeTZ);
	public String formatSequentialNumber(int n);
	public String getTimeRelative(long eventtime, TimeZone tz);
	public String getTimeLeft(long eventtime, TimeZone tz);
}
