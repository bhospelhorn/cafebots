package waffleoRai_cafebotCore;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import net.dv8tion.jda.core.entities.IMentionable;

public interface DateFormatter {
	
	public String formatTime(GregorianCalendar timestamp, boolean includeMillis, boolean includeTZ);
	public String formatSequentialNumber(int n);
	public String getTimeRelative(long eventtime, TimeZone tz);
	public String getTimeLeft(long eventtime, TimeZone tz);
	
	public String getEveryoneString();
	public String formatStringList(List<String> list);
	public void insertMentionList(List<IMentionable> ulist, BotMessage msg, ReplaceStringType replace);
}
