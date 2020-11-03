package cpuscheduler;

public class Main {

    public static void main(String[] args) {
        int numProcesses = 10000;
        float queryInterval = (float) 0.1;
        // Check if correct number of arguments are provided.
        if (args.length != 4) {
            System.out.println();
            System.err.println("USAGE: <scheduler_type> <arrival_rate> <burst_time> <quantum_length>");
            return;
        }

        // Parse args
        int scheduler = Integer.parseInt(args[0]);
        int arrivalRate = Integer.parseInt(args[1]);
        float burst = Float.parseFloat(args[2]);
        float quantum = Float.parseFloat(args[3]);

        // Checking args
        if (arrivalRate <= 0 || burst <= 0 || quantum <= 0) {
            System.err.println("ERROR: Arguments must be >= 0");
        }

        Stats stats;

        switch (scheduler) {
            case 1 -> {
                FCFS fcfs = new FCFS(numProcesses, arrivalRate, burst, queryInterval);
                stats = fcfs.RunSimulation();
                stats.display();
                stats.dump();
            }
            case 2 -> {
                SRTF srtf = new SRTF(numProcesses, arrivalRate, burst, queryInterval);
                stats = srtf.RunSimulation();
                stats.display();
                stats.dump();
            }
            case 3 -> {
                HRRN hrrn = new HRRN(numProcesses, arrivalRate, burst, queryInterval);
                stats = hrrn.RunSimulation();
                stats.display();
                stats.dump();
            }
            case 4 -> {
                RR rr = new RR(numProcesses, arrivalRate, burst, queryInterval, quantum);
                stats = rr.RunSimulation();
                stats.display();
                stats.dump();
            }
            default -> System.err.println("Please provide a valid scheduler from 1-4.");
        }
    }
}
