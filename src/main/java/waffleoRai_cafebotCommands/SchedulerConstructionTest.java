package waffleoRai_cafebotCommands;

import java.util.List;
import java.util.Map;

import waffleoRai_cafebotCommands.BotScheduler.Position;
import waffleoRai_cafebotCore.LaunchCore;

public class SchedulerConstructionTest {

	public static void main(String[] args) {
		Map<String, Integer> ppos = ParseCore.getDefaultPermPositionMap();
		List<Position> spos = ParseCore.getDefaultShiftPositions();
		new BotScheduler(LaunchCore.SHIFTS_PER_DAY_DEFO, spos, 9, ppos);

	}

}
