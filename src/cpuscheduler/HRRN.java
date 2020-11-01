package cpuscheduler;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import static cpuscheduler.EventType.*;

public class HRRN extends Scheduler {

    HRRN(int numProcesses, int arrivalRate,
                  float serviceTime, float queryInterval) {
        this.numProcesses = numProcesses;
        this.arrivalRate = arrivalRate;
        this.serviceTime = serviceTime;
        this.queryInterval = queryInterval;
    }

    public void RunSimulation() {
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
                }
                // cpu not idle
                else {
                    //TODO: Sort by wait time
                    rdQueue.add(event.GetProcess());
                }
                // create new process
                Process nextProcess =
                        new Process(event.GetProcess().GetId() + 1,
                                clock + genexp(arrivalRate), genexp(1 / serviceTime));
                processes.add(nextProcess);

                // schedule new process
                eventScheduler.ScheduleEvent(nextProcess.GetArrivalTime(), nextProcess, ARRIVAL);

            }
            else if(event.GetType() == DEPARTURE) {
                // update statistics for process that just finished
                event.GetProcess().SetCompletionTime(clock);
                event.GetProcess().SetRemainingServiceTime(0);
                processesSimulated++;

                if(!rdQueue.isEmpty()) {
                    //TODO: Sort by wait time

                    Process processInQueue = rdQueue.peekFirst();
                    onCpu = processInQueue;

                    // update the last time this process was assigned (now)
                    onCpu.SetLastTimeAssigned(clock);
                    // update the process wait time
                    onCpu.SetWaitTime(clock - processInQueue.GetArrivalTime());

                    // schedule this process's departure
                    eventScheduler.ScheduleEvent(clock + processInQueue.GetServiceTime(),
                            processInQueue, DEPARTURE);
                }
                else {
                    lastCpuBusyTime = clock;
                    cpuIdle = true;
                    onCpu = null;
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
        }
    }

}
