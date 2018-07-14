package waffleoRai_cafebotCore;

import java.util.GregorianCalendar;

public interface DateFormatter {
	
	public String formatTime(GregorianCalendar timestamp, boolean includeMillis, boolean includeTZ);
	public String formatSequentialNumber(int n);
}
