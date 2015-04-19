
package android.realtime.alarm;

import java.io.PrintStream;

public class RealtimeAlarm {
    public int type;
    /** count for the execution times for repeating alarm */
    public int count;
    /** The triggered time in milliseconds */
    public long when;// the triggered time in milliseconds
    public long repeatInterval;
    public PendingIntent operation;
    /* package */boolean isRepeatable;
    /* package */int priority;
    /** This is populated with a system timer read before the intent is fired */
    public long beforeFired;// experimental purpose
    public long wakeup;// experimental purpose

    public RealtimeAlarm() {
        when = 0;
        repeatInterval = 0;
        operation = null;
        priority = -1;
        isRepeatable = false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Alarm{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" type= ");
        sb.append(type);
        sb.append(" ");
        sb.append(operation.getRunnable().getClass().getName());
        sb.append('}');
        return sb.toString();
    }

    public void dump(PrintStream ps, String prefix, long now) {
        ps.println(prefix);
        ps.println("type=");
        ps.println(type);
        ps.println(" when=");
        ps.print(" repeatInterval=");
        ps.print(repeatInterval);
        ps.print(" count=");
        ps.println(count);
        ps.print(prefix);
        ps.print("operation=");
        ps.println(operation.getRunnable().getClass().getName());
    }
}
