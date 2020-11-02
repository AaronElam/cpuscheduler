package cpuscheduler;

import java.util.Random;
import java.util.Vector;

public class Scheduler {

    protected int numProcesses;
    protected int arrivalRate;
    protected float serviceTime;
    protected float queryInterval;
    protected float quantum;

    int processesSimulated = 0;

    // overall clock
    float clock = 0;

    boolean cpuIdle = true;
    float cpuIdleTime = 0;
    float lastTimeCpuBusy = 0;
    Process onCpu = null;

    public int totalReadyQueueProcesses = 0;

    // vector of all processes
    Vector<Process> processes = new Vector<>();

    public static float genexp(float lambda) {
        Random rand = new Random();
        float u, x;
        x = 0;
        while (x == 0) {
            u = rand.nextFloat();
            x = (float) ((-1 / lambda) * Math.log(u));
        }
        return (x);
    }
}
