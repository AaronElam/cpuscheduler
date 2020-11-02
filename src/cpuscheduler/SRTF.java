package cpuscheduler;

import java.util.LinkedList;

import static cpuscheduler.EventType.*;

public class SRTF extends Scheduler {

    SRTF(int numProcesses, int arrivalRate, float serviceTime, float queryInterval) {
        this.numProcesses = numProcesses;
        this.arrivalRate = arrivalRate;
        this.serviceTime = serviceTime;
        this.queryInterval = queryInterval;
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

                    // schedule a departure for this event
                    eventScheduler.scheduleEvent((clock + event.getProcess().getRemainingBurst()), event.getProcess(),
                            DEPARTURE);
                }
                // cpu not idle
                if (!cpuIdle) {
                    onCpu.setRemainingBurst((onCpu.getLastTimeOnCpu() + onCpu.getRemainingBurst()) - clock);
                    rdQueue.add(event.getProcess());

                    if (!rdQueue.isEmpty() && rdQueue.peek().getRemainingBurst() < onCpu.getRemainingBurst()) {
                        // swap to process that has greater priority
                        // TODO: eventScheduler.Remove
                        Process processInQueue = rdQueue.getFirst();
                        rdQueue.add(onCpu);

                        onCpu = processInQueue;
                        onCpu.setLastTimeOnCpu(clock);

                        eventScheduler.scheduleEvent((clock + processInQueue.getRemainingBurst()),
                                processInQueue, DEPARTURE);
                    }
                }
                Process nextProcess = new Process(event.getProcess().getId() + 1, clock + genexp(arrivalRate),
                        genexp((1 / serviceTime)));
                processes.add(nextProcess);

                // schedule this process for arrival
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
                    eventScheduler.scheduleEvent(clock + processInQueue.getRemainingBurst(), processInQueue, DEPARTURE);

                }
                // ready queue is empty
                else {
                    lastTimeCpuBusy = clock;
                    cpuIdle = true;
                    onCpu = null;
                }
            }
        }

        /*
         * Query event update rq stats
         */
        float totalTurnaroundTime = 0;
        for (Process process : processes) {
            if (process.getCompletionTime() != -1) {
                totalTurnaroundTime += (process.getCompletionTime() - process.getArrivalTime());
            }
        }

        /*
         * Turnaround time stats
         */
        float avgTurnaroundTime = totalTurnaroundTime / numProcesses;
        float throughput = processesSimulated / clock;
        float avgCpuUtil = (1 - (cpuIdleTime / clock)) * 100;
        float avgReadyQueueSize = totalReadyQueueProcesses / (clock / queryInterval);

        return new Stats(avgTurnaroundTime, throughput, avgCpuUtil, avgReadyQueueSize);
    }
}
