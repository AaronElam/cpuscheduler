package cpuscheduler;

import java.util.LinkedList;
import static cpuscheduler.EventType.*;

public class FCFS extends Scheduler {

    FCFS(int numProcesses, int arrivalRate,
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
                // cpu is idle - no process is currently assigned
                if (cpuIdle) {
                    // cpu idle
                    cpuIdle = false;
                    cpuIdleTime += clock - lastCpuBusyTime;
                    event.GetProcess().SetLastTimeAssigned(clock);
                    // assign current process to the cpu
                    onCpu = event.GetProcess();

                    // schedule a departure for this event
                    eventScheduler.ScheduleEvent(
                            (float) (clock + event.GetProcess().GetRemainingServiceTime()),
                            event.GetProcess(), DEPARTURE);

                }
                // cpu is not idle - add this event's process to the ready queue
                else {
                    rdQueue.add(event.GetProcess());
                }
                Process nextProcess =
                        new Process(event.GetProcess().GetId() + 1,
                                clock + genexp(arrivalRate), genexp(1 / serviceTime));
                processes.add(nextProcess);

                // schedule this new process for arrival
                eventScheduler.ScheduleEvent(nextProcess.GetArrivalTime(), nextProcess, ARRIVAL);

                /*Departure Event
                 *
                 * This process is scheduled for departure from the system.
                 * It has completed its required service time
                 */
            } else if (event.GetType() == DEPARTURE) {
                /// update statistics for process that just finished
                event.GetProcess().SetCompletionTime(clock);
                event.GetProcess().SetRemainingServiceTime(0);
                processesSimulated++;

                // if there is another process waiting in the ready queue,
                // assign it to the cpu
                if (!rdQueue.isEmpty()) {
                    // Set current process on cpu to front of ready Queue
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
                // the rdQueue is  - there are no processes currently to process
                else {
                    // update the last time the cpu was busy
                    lastCpuBusyTime = clock;
                    cpuIdle = true;
                    onCpu = null;
                }
            }
            // Query Event - Here we query for statistics on the state of
            // the readyQueue
            else if (event.GetType() == QUERY) {
                // update the total number of processes in the readyQueue every
                // query interval
                totalReadyQueueProcesses += rdQueue.size();
                eventScheduler.ScheduleEvent(clock + queryInterval, null, QUERY);
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

//        Stats newStats{avgTurnaroundTime, throughput, avgCpuUtil, avgReadyQueueSize};
//        return newStats;
    }
}

