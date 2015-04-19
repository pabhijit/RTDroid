package android.realtime.alarm;

import java.util.Comparator;

public class AlarmComparator implements Comparator<RealtimeAlarm> {
	@Override
	public int compare(RealtimeAlarm o1, RealtimeAlarm o2) {
		return o1.priority-o2.priority;
	}
}
