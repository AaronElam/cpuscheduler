package cpuscheduler;

import java.util.LinkedList;

import static cpuscheduler.EventType.*;

public class RR extends Scheduler {
    RR(int numProcesses, int arrivalRate, float serviceTime, float queryInterval, float quantum) {
        this.numProcesses = numProcesses;
        this.arrivalRate = arrivalRate;
        this.serviceTime = serviceTime;
        this.queryInterval = queryInterval;
        this.quantum = quantum;
    }

    public Stats RunSimulation() {
        EventScheduler eventScheduler = new EventScheduler();

        Process firstProcess = new Process(0, clock, genexp(1 / serviceTime));
        processes.add(firstProcess);

        eventScheduler.ScheduleEvent(clock, firstProcess, ARRIVAL);
        eventScheduler.ScheduleEvent(clock + queryInterval, null, QUERY);

        // main loop
        while (processesSimulated < numProcesses && !eventScheduler.empty()) {
            Event event = eventScheduler.getEvent();
            LinkedList<Process> rdQueue = new LinkedList<>();
            clock = event.getTime();

            if (event.getType() == ARRIVAL) {
                if (cpuIdle) {
                    // cpu idle
                    cpuIdle = false;
                    cpuIdleTime += clock - lastTimeCpuBusy;
                    event.getProcess().setLastTimeOnCpu(clock);
                    // assign current process to the cpu
                    onCpu = event.getProcess();

                    // schedule a departure for this event
                    eventScheduler.ScheduleEvent((clock + event.getProcess().getRemainingBurst()), event.getProcess(),
                            DEPARTURE);

                } else {
                    rdQueue.add(event.getProcess());
                }
                // create new process
                Process nextProcess = new Process(event.getProcess().getId() + 1, clock + genexp(arrivalRate),
                        genexp(1 / serviceTime));
                processes.add(nextProcess);

                // schedule this new process for arrival
                eventScheduler.ScheduleEvent(nextProcess.getArrivalTime(), nextProcess, ARRIVAL);
            }
            // Departure event
            else if (event.getType() == DEPARTURE) {
                event.getProcess().setCompletionTime(clock);
                event.getProcess().setRemainingBurst(0);
                processesSimulated++;

                if (!rdQueue.isEmpty()) {
                    Process processInQueue = rdQueue.getFirst();
                    rdQueue.pop();

                    onCpu = processInQueue;
                    onCpu.setLastTimeOnCpu(clock);
                    onCpu.setWaitTime(clock - onCpu.getArrivalTime());

                    eventScheduler.ScheduleEvent(clock + processInQueue.getBurst(), processInQueue, DEPARTURE);
                } else {
                    lastTimeCpuBusy = clock;
                    cpuIdle = true;
                    onCpu = null;
                }
                /*
                 * QUANTUMS
                 */
                // TODO: Remove event
                eventScheduler.ScheduleEvent(clock + quantum, null, TIMEOUT);
            } else if (event.getType() == TIMEOUT) {
                if (!cpuIdle) {
                    onCpu.setRemainingBurst(onCpu.getLastTimeOnCpu() + onCpu.getRemainingBurst() - clock);
                    // TODO: Remove event
                    rdQueue.push(onCpu);

                    onCpu = rdQueue.getFirst();
                    rdQueue.pop();

                    // schedule new quantum
                    onCpu.setLastTimeOnCpu(clock);
                    eventScheduler.ScheduleEvent(clock + quantum, null, TIMEOUT);

                    eventScheduler.ScheduleEvent(clock + onCpu.getRemainingBurst(), onCpu, DEPARTURE);
                }
            }
        }
        // calculate turnaround time
        float totalTurnaroundTime = 0;
        for (Process process : processes) {
            // include only completed processes
            if (process.getCompletionTime() != -1) {
                totalTurnaroundTime += (process.getCompletionTime() - process.getArrivalTime());
            }
        }
        float avgTurnaroundTime = totalTurnaroundTime / numProcesses;
        float throughput = processesSimulated / clock;
        float avgCpuUtil = (1 - (cpuIdleTime / clock)) * 100;
        float avgReadyQueueSize = totalReadyQueueProcesses / (clock / queryInterval);

        return new Stats(avgTurnaroundTime, throughput, avgCpuUtil, avgReadyQueueSize);
    }
}
