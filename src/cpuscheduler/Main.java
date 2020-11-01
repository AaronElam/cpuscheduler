package cpuscheduler;

public class Main {

    public static void main(String[] args) {
        int numProcesses = 12000;
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
        float serviceTime = Float.parseFloat(args[2]);
        float quantum = Float.parseFloat(args[3]);

        // Checking args
        if (arrivalRate <= 0 || serviceTime <= 0 || quantum <= 0) {
            System.err.println("ERROR: Arguments must be >= 0");
        }
        new Scheduler();
        Scheduler s = switch (scheduler) {
            case 1 -> new FCFS(numProcesses, arrivalRate, serviceTime, queryInterval);
            case 2 -> new SRTF(numProcesses, arrivalRate, serviceTime, queryInterval);
            case 3 -> new HRRN(numProcesses, arrivalRate, serviceTime, queryInterval);
            case 4 -> new RR(numProcesses, arrivalRate, serviceTime, queryInterval, quantum);
            default -> new Scheduler();
        };
    }
}
