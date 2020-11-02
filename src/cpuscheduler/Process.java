package cpuscheduler;

public class Process {
    private final int id;
    private final float arrivalTime;
    private final float burst;

    private float waitTime;
    private float remainingBurst;
    private float lastTimeOnCpu;
    private float completionTime;

    Process(int id, float arrivalTime, float burst) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burst = burst;

        // set private vars
        waitTime = 0;
        remainingBurst = burst;
        completionTime = -1;
        lastTimeOnCpu = -1;
    }

    int getId() {
        return id;
    }

    float getArrivalTime() {
        return arrivalTime;
    }

    float getBurst() { return burst; }

    float getRemainingBurst() {
        return remainingBurst;
    }

    float getLastTimeOnCpu() {
        return lastTimeOnCpu;
    }

    float getCompletionTime() {
        return completionTime;
    }

    void setWaitTime(float waitTime) {
        this.waitTime = waitTime;
    }

    void setRemainingBurst(float remainingBurst) {
        this.remainingBurst = remainingBurst;
    }

    void setLastTimeOnCpu(float lastTimeOnCpu) {this.lastTimeOnCpu = lastTimeOnCpu;}

    void setCompletionTime(float completionTime) {
        this.completionTime = completionTime;
    }

    // HRRN only
    float getResponseRatio() {
        return (waitTime + burst) / burst;
    }
};