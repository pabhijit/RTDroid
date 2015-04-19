package android.realtime.alarm;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.realtime.PriorityParameters;
import javax.realtime.RealtimeThread;

import edu.buffalo.rtdroid.SystemConfig;

public class AlarmScheduleThread extends RealtimeThread {
    public TreeMap<Long, TreeMap<Integer, LinkedList<RealtimeAlarm>>> submitAlarmContainer;
    public TreeMap<Long, TreeMap<Integer, LinkedList<RealtimeAlarm>>> scheduledAlarmContainer;
    /* package */int numDropAlarm;// experimental purpose
    /* package */Object lock;
    /* package */boolean mQuit;
    /* package */AlarmScheduler schedler;
    /* package */int numSubmitAlarm;
    private AlarmPool aPool;

    public AlarmScheduleThread(AlarmScheduler scheduler) {
        numSubmitAlarm = 0;
        submitAlarmContainer = new TreeMap<Long, TreeMap<Integer, LinkedList<RealtimeAlarm>>>();
        scheduledAlarmContainer = new TreeMap<Long, TreeMap<Integer, LinkedList<RealtimeAlarm>>>();
        lock = new Object();
        mQuit = false;
        this.schedler = scheduler;
    }

    public void run() {
        RealtimeThread currThread = RealtimeThread.currentRealtimeThread();
        currThread.setSchedulingParameters(new PriorityParameters(SystemConfig
                .getMaxPriority()));

        while (!mQuit) {
            synchronized (lock) {
                try {
                    TreeMap<Integer, LinkedList<RealtimeAlarm>> node = pullNextTimeStampe();
                    if (node == null) {
                        lock.wait();
                    } else {
                        Iterator<LinkedList<RealtimeAlarm>> listIt = node
                                .values().iterator();
                        while (listIt.hasNext()) {
                            LinkedList<RealtimeAlarm> alarmList = listIt.next();
                            Iterator<RealtimeAlarm> it = alarmList.iterator();
                            while (it.hasNext()) {
                                RealtimeAlarm alarm = it.next();
                                /*
                                 * schedule the alarm with alarm scheduler
                                 * enqueue the alarm in scheduler alarm
                                 * container
                                 */
                                schedler.schdulerAlarm(alarm, aPool);
                                if (scheduledAlarmContainer
                                        .containsKey(alarm.when)) {
                                    if (scheduledAlarmContainer.get(alarm.when)
                                            .containsKey(alarm.priority)) {
                                        scheduledAlarmContainer.get(alarm.when)
                                                .get(alarm.priority).add(alarm);
                                    } else {
                                        LinkedList<RealtimeAlarm> list = new LinkedList<RealtimeAlarm>();
                                        list.add(alarm);
                                        scheduledAlarmContainer.get(alarm.when)
                                                .put(alarm.priority, list);
                                    }
                                } else {
                                    TreeMap<Integer, LinkedList<RealtimeAlarm>> newNode = new TreeMap<Integer, LinkedList<RealtimeAlarm>>();
                                    LinkedList<RealtimeAlarm> newList = new LinkedList<RealtimeAlarm>();
                                    newList.add(alarm);
                                    newNode.put(alarm.priority, newList);
                                    scheduledAlarmContainer.put(alarm.when,
                                            newNode);
                                }
                            }
                        }// end of inner while
                    }// end of if-else
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }// end of lock
        }// end of outer while
    }

    public TreeMap<Integer, LinkedList<RealtimeAlarm>> pullNextTimeStampe() {
        if (numSubmitAlarm > 0) {
            TreeMap<Integer, LinkedList<RealtimeAlarm>> returnValue = null;
            Iterator<Map.Entry<Long, TreeMap<Integer, LinkedList<RealtimeAlarm>>>> it = submitAlarmContainer
                    .entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, TreeMap<Integer, LinkedList<RealtimeAlarm>>> item = it
                        .next();
                if (item.getValue().size() > 0) {
                    returnValue = item.getValue();
                    numSubmitAlarm = numSubmitAlarm - returnValue.size();
                    it.remove();
                    break;
                }
            }

            return returnValue;
        } else {
            return null;
        }

    }

    public void setAlarm(RealtimeAlarm alarm, AlarmPool aPool) {
        this.aPool = aPool;
        synchronized (lock) {
            numSubmitAlarm++;
            if (submitAlarmContainer.containsKey(alarm.when)) {
                if (submitAlarmContainer.get(alarm.when).containsKey(
                        alarm.priority)) {
                    submitAlarmContainer.get(alarm.when).get(alarm.priority)
                            .add(alarm);
                } else {
                    LinkedList<RealtimeAlarm> list = new LinkedList<RealtimeAlarm>();
                    list.add(alarm);
                    submitAlarmContainer.get(alarm.when).put(alarm.priority,
                            list);
                }
            } else {
                TreeMap<Integer, LinkedList<RealtimeAlarm>> newNode = new TreeMap<Integer, LinkedList<RealtimeAlarm>>();
                LinkedList<RealtimeAlarm> newList = new LinkedList<RealtimeAlarm>();
                newList.add(alarm);
                newNode.put(alarm.priority, newList);
                submitAlarmContainer.put(alarm.when, newNode);
            }

            alarm.operation.status = PendingIntent.PENDIND_INTENT_REGISTERED;
            lock.notifyAll();
        }
    }

    public void removeAlarm(PendingIntent operation)
            throws AlarmCancellationException {
        synchronized (lock) {
            if (operation.status == PendingIntent.PENDIND_INTENT_REGISTERED) {
                operation.status = PendingIntent.PENDIND_INTENT_CANCELED;
                if (submitAlarmContainer.containsKey(operation.alarm.when)) {
                    submitAlarmContainer.get(operation.alarm.when)
                            .get(operation.priority).remove(operation.alarm);
                }
                if (scheduledAlarmContainer.containsKey(operation.alarm.when)) {
                    operation.status = PendingIntent.PENDIND_INTENT_CANCELED;
                }
            }
            lock.notifyAll();
        }
    }

    /*
     * experimental function
     */
    public synchronized void dropAlarm() {
        numDropAlarm++;
    }
}