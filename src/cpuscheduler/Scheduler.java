package cpuscheduler;

import java.util.Random;
import java.util.Vector;

public class Scheduler {

    protected int numProcesses;
    protected int arrivalRate;
    protected float serviceTime;
    protected float queryInterval;
    protected float quantum;

    // total # processes completed
    int processesSimulated = 0;
    // running clock
    float clock = 0;

    // bool to keep track of cpu idle state
    boolean cpuIdle = true;
    // total time that cpu has been idle during the sim
    float cpuIdleTime = 0;
    // last busy cpu time
    float lastCpuBusyTime = 0;
    // Pointer to process currently on the cpu
    Process onCpu = null;

    // total # processes in readyQueue throughout simulation
    int totalReadyQueueProcesses = 0;


    // list of all processes in sim - used for calculating turnaround time
    Vector<Process> processes = new Vector<>();

    //    // parent constructor (if defined) is called first
//    public void RunSimulation() = 0;
    ////////////////////////////////////////////////////////////////
    // Functions below used for calculating exp. distribution and provided by
    // professor Mina
    // returns a random number between 0 and 1
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
