package cpuscheduler;

import java.util.Comparator;
import java.util.LinkedList;

import static cpuscheduler.EventType.*;

public class FCFS extends Scheduler {

    FCFS(int numProcesses, int arrivalRate, float serviceTime, float queryInterval) {
        this.numProcesses = numProcesses;
        this.arrivalRate = arrivalRate;
        this.serviceTime = serviceTime;
        this.queryInterval = queryInterval;
    }

    // Comparator for use in Ready Queue.
    // Ensures ready queue is sorted s.t. highest priority has lowest arrival time.
    class FCFSComparator implements Comparator<Process> {
        @Override
        public int compare(Process o1, Process o2) {
            return Float.compare(o1.getArrivalTime(), o2.getArrivalTime());
        }
    }


    public Stats RunSimulation() {
        EventScheduler eventScheduler = new EventScheduler();

        Process firstProcess = new Process(0, clock, genexp(1 / serviceTime));
        processes.add(firstProcess);

        eventScheduler.scheduleEvent(clock, firstProcess, ARRIVAL);
        eventScheduler.scheduleEvent(clock + queryInterval, null, QUERY);

        // main loop
        while (processesSimulated < numProcesses && !eventScheduler.empty()) {
            Event event = eventScheduler.getEvent();
            LinkedList<Process> rdQueue = new LinkedList<>();
            clock = event.getTime();

            /*
             *
             * Arrival event
             *
             */
            if (event.getType() == ARRIVAL) {
                // nothing on cpu
                if (cpuIdle) {
                    cpuIdle = false;
                    cpuIdleTime += clock - lastTimeCpuBusy;
                    event.getProcess().setLastTimeOnCpu(clock);
                    // assign to cpu
                    onCpu = event.getProcess();

                    // schedule this process for departure
                    eventScheduler.scheduleEvent((clock + event.getProcess().getRemainingBurst()), event.getProcess(),
                            DEPARTURE);
                }
                // cpu not idle
                if(!cpuIdle){
                    //TODO: sort from lowest service time to highest. Low gets priority
                    rdQueue.add(event.getProcess());
                    totalReadyQueueProcesses++;
                    rdQueue.sort(event.getProcess());
                }
                Process nextProcess = new Process(event.getProcess().getId() + 1, clock + genexp(arrivalRate),
                        genexp(1 / serviceTime));
                processes.add(nextProcess);

                // send this process off to arrive
                eventScheduler.scheduleEvent(nextProcess.getArrivalTime(), nextProcess, ARRIVAL);

                /*
                 *
                 * Departure event
                 *
                 */
            } if (event.getType() == DEPARTURE) {
                // stat updates
                event.getProcess().setCompletionTime(clock);
                event.getProcess().setRemainingBurst(0);
                processesSimulated++;

                // place a waiting process in the ready queue then assign to cpu
                if (!rdQueue.isEmpty()) {
                    Process processInQueue = rdQueue.peekFirst();
                    onCpu = processInQueue;

                    // update times
                    onCpu.setLastTimeOnCpu(clock);
                    onCpu.setWaitTime(clock - processInQueue.getArrivalTime());

                    // schedule this process for departure
                    eventScheduler.scheduleEvent(clock + processInQueue.getBurst(), processInQueue, DEPARTURE);

                }
                // ready queue is empty
                else {
                    lastTimeCpuBusy = clock;
                    cpuIdle = true;
                    onCpu = null;
                }
            }

            /*
             * Query event update rq stats
             */
            else if (event.getType() == QUERY) {
                totalReadyQueueProcesses += rdQueue.size();
                eventScheduler.scheduleEvent(clock + queryInterval, null, QUERY);
            }
        }

        /*
         * Turnaround time stats
         */
        float totalTurnaroundTime = 0;
        for (Process process : processes) {
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
