
package android.realtime.alarm;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.realtime.AsyncEvent;
import javax.realtime.AsyncEventHandler;
import javax.realtime.PriorityParameters;
import javax.realtime.RealtimeThread;

import com.fiji.fivm.Time;

public class AsyncScheduler extends AlarmScheduler {
	
	private AlarmPool aPool;
	
    public AsyncScheduler() {
        super();
    }

    public void schdulerAlarm(RealtimeAlarm alarm, AlarmPool aPool) {
    	this.aPool = aPool;
    	Runnable run = new AlarmRunnableWrapper(alarm);
        RealtimeThread rt = new RealtimeThread(run);
        rt.start();
    }

    class AlarmRunnableWrapper implements Runnable {
        RealtimeAlarm alarm;
        long when;

        public AlarmRunnableWrapper(RealtimeAlarm alarm) {
            this.alarm = alarm;
            this.when = alarm.when;
        }

        public void run() {
            RealtimeThread currThread = RealtimeThread.currentRealtimeThread();
            currThread.setSchedulingParameters(new PriorityParameters(
                    alarm.priority));

            AsyncEventHandler handler = new AsyncEventHandler(alarm.operation.getRunnable());
            handler.setSchedulingParameters(new PriorityParameters(alarm.priority));
            handler.setDaemon(false);
            AsyncEvent event = new AsyncEvent();
            alarm.count++;
            event.addHandler(handler);
            System.out.println("when ===>" + when * 1000000);
            //Time.sleepAbsolute(when * 1000000L);
            try {
				RealtimeThread.sleep(when);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            System.out.println("Here");
            alarm.wakeup = System.nanoTime();
            System.out.println(alarm.wakeup);
            /*
             * check currentTimestampe > nextTimeStamp.when &&
             * nextTimeStamp.firstAlarm.priority > currentAlarm.priority fix
             * me!! Since the treemap is not synchronized container, it possible
             * there are incoming alarm that just get registered at the current
             * or the nearest higher timestamp. Then, these cases should use
             * real time handler, rather than alarm!
             */
            if (DROP_ALARMS) {
                Map.Entry<Long, TreeMap<Integer, LinkedList<RealtimeAlarm>>> higherEntry = schedulingThread.scheduledAlarmContainer
                        .higherEntry(alarm.when);
                if (higherEntry != null
                        && System.currentTimeMillis() > higherEntry.getKey()
                        && alarm.priority < higherEntry.getValue().firstKey()) {
                    alarm.operation.status = PendingIntent.PENDIND_INTENT_CANCELED;
                    schedulingThread.dropAlarm();
                }
            }

            if (alarm.operation.status != PendingIntent.PENDIND_INTENT_CANCELED) {
                alarm.beforeFired = System.nanoTime();
                event.fire();
                aPool.recycleObject(alarm);
            }

            if (alarm.isRepeatable) {
                alarm.when = System.currentTimeMillis() + alarm.repeatInterval;
                schedulingThread.setAlarm(alarm, aPool);
            }
        }

    }

}
