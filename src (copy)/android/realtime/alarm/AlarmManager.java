package android.realtime.alarm;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.realtime.RealtimeThread;

import edu.buffalo.rtdroid.experimentUtil.RealTimeHelper;

/*
 * TODO
 * we didn't handle different alarm type
 */

public class AlarmManager {
    /**
     * Alarm time in {@link System#currentTimeMillis System.currentTimeMillis()}
     * (wall clock time in UTC), which will wake up the device when it goes off.
     */
    public static final int RTC_WAKEUP = 0;
    /**
     * Alarm time in {@link System#currentTimeMillis System.currentTimeMillis()}
     * (wall clock time in UTC). This alarm does not wake the device up; if it
     * goes off while the device is asleep, it will not be delivered until the
     * next time the device wakes up.
     */
    public static final int RTC = 1;
    /**
     * Alarm time in {@link android.os.SystemClock#elapsedRealtime
     * SystemClock.elapsedRealtime()} (time since boot, including sleep), which
     * will wake up the device when it goes off.
     */
    public static final int ELAPSED_REALTIME_WAKEUP = 2;
    /**
     * Alarm time in {@link android.os.SystemClock#elapsedRealtime
     * SystemClock.elapsedRealtime()} (time since boot, including sleep). This
     * alarm does not wake the device up; if it goes off while the device is
     * asleep, it will not be delivered until the next time the device wakes up.
     */
    public static final int ELAPSED_REALTIME = 3;
    private AlarmPool aPool;
    private static AlarmManager instance = null;
    private AlarmScheduleThread schedulingThread;
    private int MAX_ALARMS = 100;
    private BufferedReader kb;

    private AlarmManager() {
        schedulingThread = AlarmSchedulerFactory.getInstance().getSchedulingThread();
        System.out.println("Enter the no of alarm objects to be created.");
        kb = new BufferedReader(new InputStreamReader(System.in));
        try {
			MAX_ALARMS = Integer.parseInt(kb.readLine());
		} catch (Exception e) {}
        AlarmComparator aComp = new AlarmComparator();
        aPool = new AlarmPool(MAX_ALARMS, aComp);
    }

    public static AlarmManager getInstance() {
        if (instance == null) {
            instance = new AlarmManager();
        }
        return instance;
    }
    
    public AlarmScheduleThread getSchedulingThread(){
    	return schedulingThread;
    }

    public void set(int type, long triggerAtmMillis, PendingIntent operation) {
        RealtimeAlarm alarm = aPool.requestObject();
        if(alarm == null){
        	System.out.println("You have run out of objects. Waiting for objects to be recycled ...");
        	while((alarm = aPool.requestObject()) == null);
        }
	    alarm.operation = operation;
	    operation.alarm = alarm;
	    alarm.when = triggerAtmMillis;
	    alarm.type = type;
	    alarm.repeatInterval = 0L;
	    alarm.isRepeatable = false;
	    int pr = RealTimeHelper.getInstance().FijiFIFO2RTSJ(RealtimeThread.currentRealtimeThread().getPriority());
	    alarm.priority = pr;
	    operation.priority = pr;
	    System.out.println("Registered: " + alarm);
	    schedulingThread.setAlarm(alarm, aPool);
    }

    public void setRepeatIng(int type, long triggerAtmMillis,
            long intervalMills, PendingIntent operation) {
        RealtimeAlarm alarm = new RealtimeAlarm();
        alarm.operation = operation;
        operation.alarm = alarm;
        alarm.when = triggerAtmMillis;
        alarm.type = type;
        alarm.repeatInterval = intervalMills;
        alarm.isRepeatable = true;
        int pr = RealTimeHelper.getInstance().FijiFIFO2RTSJ(
                RealtimeThread.currentRealtimeThread().getPriority());
        alarm.priority = pr;
        operation.priority = pr;
        schedulingThread.setAlarm(alarm, aPool);

    }

    public void cancel(PendingIntent operation)
            throws AlarmCancellationException {
        schedulingThread.removeAlarm(operation);
    }

    public void setInexactRepeating(int type, long triggerAtTime,
            long interval, Runnable operation) {
        System.out.println("coming soon");
    }

    public void setTime(long millis) {
        System.out.println("coming soon");
    }

    public void setTimeZone(String tz) {
        System.out.println("coming soon");
    }

    /*
     * this function is created for experimental purpose
     */
    public int getNumDropAlarm() {
        return schedulingThread.numDropAlarm;
    }
}