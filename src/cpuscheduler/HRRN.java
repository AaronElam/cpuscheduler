package cpuscheduler;

import java.util.LinkedList;

import static cpuscheduler.EventType.*;

public class HRRN extends Scheduler {

    HRRN(int numProcesses, int arrivalRate, float serviceTime, float queryInterval) {
        this.numProcesses = numProcesses;
        this.arrivalRate = arrivalRate;
        this.serviceTime = serviceTime;
        this.queryInterval = queryInterval;
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
                }
                // cpu not idle
                else {
                    // TODO: Sort by wait time
                    rdQueue.add(event.getProcess());
                }
                // create new process
                Process nextProcess = new Process(event.getProcess().getId() + 1, clock + genexp(arrivalRate),
                        genexp(1 / serviceTime));
                processes.add(nextProcess);

                // schedule new process
                eventScheduler.ScheduleEvent(nextProcess.getArrivalTime(), nextProcess, ARRIVAL);

            } else if (event.getType() == DEPARTURE) {
                // update statistics for process that just finished
                event.getProcess().setCompletionTime(clock);
                event.getProcess().setRemainingBurst(0);
                processesSimulated++;

                if (!rdQueue.isEmpty()) {
                    // TODO: Sort by wait time

                    Process processInQueue = rdQueue.peekFirst();
                    onCpu = processInQueue;

                    // update the last time this process was assigned (now)
                    onCpu.setLastTimeOnCpu(clock);
                    // update the process wait time
                    onCpu.setWaitTime(clock - processInQueue.getArrivalTime());

                    // schedule this process's departure
                    eventScheduler.ScheduleEvent(clock + processInQueue.getBurst(), processInQueue, DEPARTURE);
                } else {
                    lastTimeCpuBusy = clock;
                    cpuIdle = true;
                    onCpu = null;
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
