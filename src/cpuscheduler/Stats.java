package cpuscheduler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Stats {
    float avgTurnaroundTime;
    float totalThroughput;
    float avgCpuUtil;
    float avgReadyQueueSize;
    File file = new File("./Data/sim.data");

    Stats(float avgTurnaroundTime, float totalThroughput, float avgCpuUtil, float avgReadyQueueSize) {
        this.avgTurnaroundTime = avgTurnaroundTime;
        this.totalThroughput = totalThroughput;
        this.avgCpuUtil = avgCpuUtil;
        this.avgReadyQueueSize = avgReadyQueueSize;
    }

    void display() {
        System.out.printf("Avg Turnaround Time:  %.3f\n", avgTurnaroundTime);
        System.out.printf("Total throughput:     %.3f\n", totalThroughput);
        System.out.printf("Avg CPU Util:         %.3f\n", avgCpuUtil);
        System.out.printf("Avg ReadyQueue size:  %.3f\n", avgReadyQueueSize);
    }

    void dump() {
        try {
            FileWriter w = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(w);
            bw.write(avgTurnaroundTime + "," + totalThroughput + "," + avgCpuUtil + "," + avgReadyQueueSize + "\n");
            bw.close();
            w.close();
            System.out.println("\nSuccessfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

}
