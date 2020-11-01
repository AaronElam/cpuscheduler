package cpuscheduler;

import java.util.LinkedList;

import static cpuscheduler.EventType.*;

public class RR extends Scheduler {
    RR(int numProcesses, int arrivalRate,
       float serviceTime, float queryInterval, float quantum) {
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
        while (processesSimulated < numProcesses && !eventScheduler.Empty()) {
            Event event = eventScheduler.GetEvent();
            LinkedList<Process> rdQueue = new LinkedList<>();
            clock = event.GetTime();

            if (event.GetType() == ARRIVAL) {
                if (cpuIdle) {
                    // cpu idle
                    cpuIdle = false;
                    cpuIdleTime += clock - lastCpuBusyTime;
                    event.GetProcess().SetLastTimeAssigned(clock);
                    // assign current process to the cpu
                    onCpu = event.GetProcess();

                    // schedule a departure for this event
                    eventScheduler.ScheduleEvent(
                            (clock + event.GetProcess().GetRemainingServiceTime()),
                            event.GetProcess(), DEPARTURE);

                } else {
                    rdQueue.add(event.GetProcess());
                }
                // create new process
                Process nextProcess =
                        new Process(event.GetProcess().GetId() + 1,
                                clock + genexp(arrivalRate), genexp(1 / serviceTime));
                processes.add(nextProcess);

                // schedule this new process for arrival
                eventScheduler.ScheduleEvent(nextProcess.GetArrivalTime(), nextProcess, ARRIVAL);
            }
            // Departure event
            else if (event.GetType() == DEPARTURE) {
                event.GetProcess().SetCompletionTime(clock);
                event.GetProcess().SetRemainingServiceTime(0);
                processesSimulated++;

                if (!rdQueue.isEmpty()) {
                    Process processInQueue = rdQueue.getFirst();
                    rdQueue.pop();

                    onCpu = processInQueue;
                    onCpu.SetLastTimeAssigned(clock);
                    onCpu.SetWaitTime(clock - onCpu.GetArrivalTime());

                    eventScheduler.ScheduleEvent(clock + processInQueue.GetServiceTime(),
                            processInQueue, DEPARTURE);
                } else {
                    lastCpuBusyTime = clock;
                    cpuIdle = true;
                    onCpu = null;
                }
                /*
                    QUANTUMS
                 */
                //TODO: Remove event
                eventScheduler.ScheduleEvent(clock + quantum, null, TIMEOUT);
            } else if (event.GetType() == TIMEOUT) {
                if (!cpuIdle) {
                    onCpu.SetRemainingServiceTime(
                            onCpu.GetLastTimeOnCpu() + onCpu.GetRemainingServiceTime() - clock);
                    //TODO: Remove event
                    rdQueue.push(onCpu);

                    onCpu = rdQueue.getFirst();
                    rdQueue.pop();

                    //schedule new quantum
                    onCpu.SetLastTimeAssigned(clock);
                    eventScheduler.ScheduleEvent(clock + quantum, null, TIMEOUT);

                    eventScheduler.ScheduleEvent(clock + onCpu.GetRemainingServiceTime(),
                            onCpu, DEPARTURE);
                }
            }
        }
        // calculate  turnaround time
        float totalTurnaroundTime = 0;
        for (Process process : processes) {
            // include only completed processes
            if (process.GetCompletionTime() != -1) {
                totalTurnaroundTime +=
                        (process.GetCompletionTime() - process.GetArrivalTime());
            }
        }
        float avgTurnaroundTime = totalTurnaroundTime / numProcesses;
        float throughput = processesSimulated / clock;
        float avgCpuUtil = (1 - (cpuIdleTime / clock)) * 100;
        float avgReadyQueueSize = totalReadyQueueProcesses / (clock / queryInterval);

        return new Stats(avgTurnaroundTime, throughput, avgCpuUtil, avgReadyQueueSize);
    }
}
