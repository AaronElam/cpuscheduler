package cpuscheduler;

public class Process {
    // these vars do not have setters (not necessary)
    private final int id;
    private final float arrivalTime;
    private final float serviceTime;

    // time that process has been waiting in ready queue
    private float waitTime;
    // time remaining until process is terminated
    private float remainingServiceTime;
    // time at which process is expected to finish
    private float completionTime;
    // last time that process was on the cpu
    private float lastTimeOnCpu;

    /**
     * Constructs a process object using int id, double arrivaltime, double
     * serviceTime
     */
    Process(int id, float arrivalTime, float serviceTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;

        // set private vars
        waitTime = 0;
        remainingServiceTime = serviceTime;
        completionTime = -1;
        lastTimeOnCpu = -1;
    }

    int GetId() {
        return id;
    }

    float GetArrivalTime() {
        return arrivalTime;
    }

    float GetServiceTime() {
        return serviceTime;
    }

    float GetWaitTime() {
        return waitTime;
    }

    float GetLastTimeOnCpu() {
        return lastTimeOnCpu;
    }

    float GetRemainingServiceTime() {
        return remainingServiceTime;
    }

    float GetCompletionTime() {
        return completionTime;
    }

    void SetCompletionTime(float completionTime) {
        this.completionTime = completionTime;
    }

    void SetRemainingServiceTime(float remainingServiceTime) {
        this.remainingServiceTime = remainingServiceTime;
    }

    void SetWaitTime(float waitTime) {
        this.waitTime = waitTime;
    }

    void SetLastTimeAssigned(float lastTimeOnCpu) {
        this.lastTimeOnCpu = lastTimeOnCpu;
    }

    /**
     * Calculates the Response Ratio, used in the HRRN scheduling scheme
     */
    float GetResponseRatio() {
        return (waitTime + serviceTime) / serviceTime;
    }
};