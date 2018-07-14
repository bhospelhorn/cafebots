package waffleoRai_schedulebot;

public class DeadlineEvent extends OneTimeEvent{
	
	public DeadlineEvent(long user, int numberUsers)
	{
		super(user, numberUsers);
	}
	
	@Override
	public synchronized EventType getEventType()
	{
		return EventType.DEADLINE;
	}
	
}
