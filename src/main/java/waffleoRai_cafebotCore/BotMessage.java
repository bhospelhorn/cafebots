package waffleoRai_cafebotCore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.IMentionable;
import net.dv8tion.jda.core.entities.Message;

public class BotMessage {
	
	//private MessageBuilder builder;
	private MessageComp rootcomp;
	
	private static class MessageComp
	{
		private boolean ismention;
		
		private String str;
		private IMentionable mentioned;
		
		public List<MessageComp> subcomps;
		
		public MessageComp(String rootstr)
		{
			ismention = false;
			str = rootstr;
		}
		
		public MessageComp(IMentionable rootobj)
		{
			ismention = true;
			mentioned = rootobj;
		}
		
		public void substituteString(ReplaceStringType t, String s)
		{
			if (ismention) return; //Can't split
			if (subcomps == null)
			{
				String[] sarr = str.split(t.getString().toString());
				if (sarr != null && sarr.length > 1)
				{
					subcomps = new ArrayList<MessageComp>(sarr.length + (sarr.length - 1));
					for (int i = 0; i < sarr.length; i++)
					{
						if (i > 0) subcomps.add(new MessageComp(s));
						subcomps.add(new MessageComp(sarr[i]));
					}
				}	
			}
			else
			{
				for (MessageComp c : subcomps) c.substituteString(t, s);
			}
		}
		
		public void substituteStringSeries(ReplaceStringType target, List<String> strings)
		{
			if (ismention) return; //Can't split
			int nstr = strings.size();
			for (int i = 0; i < nstr; i++)
			{
				if (subcomps == null)
				{				
					String splitter = target.getString().toString() + i;
					String[] sarr = str.split(splitter);
					if (sarr != null && sarr.length > 1)
					{
						String s = strings.get(i);
						subcomps = new ArrayList<MessageComp>(sarr.length + (sarr.length - 1));
						for (int j = 0; j < sarr.length; j++)
						{
							if (j > 0) subcomps.add(new MessageComp(s));
							subcomps.add(new MessageComp(sarr[j]));
						}
					}	
				}
				else
				{
					for (MessageComp c : subcomps) c.substituteStringSeries(target, strings);
				}
			}
			
		}
		
		public void substituteMention(ReplaceStringType t, IMentionable o)
		{
			if (ismention) return; //Can't split
			if (subcomps == null)
			{
				String[] sarr = str.split(t.getString().toString());
				if (sarr != null && sarr.length > 1)
				{
					subcomps = new ArrayList<MessageComp>(sarr.length + (sarr.length - 1));
					for (int i = 0; i < sarr.length; i++)
					{
						if (i > 0) subcomps.add(new MessageComp(o));
						subcomps.add(new MessageComp(sarr[i]));
					}
				}
			}
			else
			{
				for (MessageComp c : subcomps) c.substituteMention(t, o);
			}
			
		}
		
		public void substituteMentions(ReplaceStringType t, Collection<IMentionable> olist)
		{
			if (ismention) return; //Can't split
			if (subcomps == null)
			{
				int mentions = olist.size();
				String[] sarr = str.split(t.getString().toString());
				if (sarr != null && sarr.length > 1)
				{
					subcomps = new ArrayList<MessageComp>(sarr.length + ((sarr.length - 1)*mentions));
					for (int i = 0; i < sarr.length; i++)
					{
						if (i > 0){
							for (IMentionable o : olist) subcomps.add(new MessageComp(o));
						};
						subcomps.add(new MessageComp(sarr[i]));
					}
				}
			}
			else
			{
				for (MessageComp c : subcomps) c.substituteMentions(t, olist);
			}
		}
		
		public void substituteFormattedMentions(ReplaceStringType t, Collection<IMentionable> olist, String delim, String lastdelim, String twodelim)
		{
			if (ismention) return; //Can't split
			if (subcomps == null)
			{
				int mentions = olist.size();
				String[] sarr = str.split(t.getString().toString());
				if (sarr != null && sarr.length > 1)
				{
					subcomps = new ArrayList<MessageComp>(sarr.length + ((sarr.length - 1)*mentions*2));
					for (int i = 0; i < sarr.length; i++)
					{
						if (i > 0){
							int j = 0;
							for (IMentionable o : olist)
							{
								subcomps.add(new MessageComp(o));
								if (mentions > 2)
								{
									if (j < mentions-1) subcomps.add(new MessageComp(delim));
									if (j == mentions-2) subcomps.add(new MessageComp(lastdelim));
								}
								else
								{
									if (j < 1) subcomps.add(new MessageComp(twodelim));
								}
								j++;
							}
						};
						subcomps.add(new MessageComp(sarr[i]));
					}
				}
			}
			else
			{
				for (MessageComp c : subcomps) c.substituteMentions(t, olist);
			}
		}
		
		public void addToBuilder(MessageBuilder builder)
		{
			if (subcomps == null)
			{
				if (ismention) builder.append(mentioned);
				else builder.append(str);
			}
			else
			{
				for (MessageComp c : subcomps) c.addToBuilder(builder);
			}
		}
		
		public void tackOnToEnd(String str)
		{
			subcomps.add(new MessageComp(str));
		}
	}
	
	public BotMessage(String sourceString)
	{
		rootcomp = new MessageComp(sourceString);
	}
	
	public void substituteString(ReplaceStringType target, String str)
	{
		rootcomp.substituteString(target, str);
	}
	
	public void substituteStringSeries(ReplaceStringType target, List<String> strings)
	{
		if(strings == null || strings.isEmpty()) return;
		rootcomp.substituteStringSeries(target, strings);
	}
	
	public void substituteMention(ReplaceStringType target, IMentionable obj)
	{
		rootcomp.substituteMention(target, obj);
	}
	
	public void substituteMentions(ReplaceStringType target, Collection<IMentionable> olist)
	{
		rootcomp.substituteMentions(target, olist);
	}
	
	public void substituteFormattedMentions(ReplaceStringType target, Collection<IMentionable> olist, String delimiter, String finalDelim, String twodelim)
	{
		rootcomp.substituteFormattedMentions(target, olist, delimiter, finalDelim, twodelim);
	}
	
	public void addToEnd(String str)
	{
		rootcomp.tackOnToEnd(str);
	}
	
	public Message buildMessage()
	{
		MessageBuilder builder = new MessageBuilder();
		rootcomp.addToBuilder(builder);
		return builder.build();
	}
	
	public String toString()
	{
		return this.buildMessage().toString();
	}

}
