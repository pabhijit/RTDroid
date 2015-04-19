package android.realtime.alarm;

import edu.buffalo.rtdroid.util.ObjectPool;

public class AlarmPool extends ObjectPool<RealtimeAlarm>{

	public AlarmPool(final int capacity, AlarmComparator aComp) {
		super(capacity, aComp);
	}

	@Override
	protected RealtimeAlarm createObject() {
		RealtimeAlarm alarm = new RealtimeAlarm();
		return alarm;
	}

	@Override
	protected RealtimeAlarm dropMessage() {
		return null;
	}

	@Override
	protected void setObjectContext(RealtimeAlarm a) {
        a.when = 0;
        a.repeatInterval = 0;
        a.operation = null;
        a.priority = -1;
        a.isRepeatable = false;
	}
}