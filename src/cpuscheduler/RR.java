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
                if (cpuIdle) {
                    // nothing on cpu
                    cpuIdle = false;
                    cpuIdleTime += clock - lastTimeCpuBusy;
                    event.getProcess().setLastTimeOnCpu(clock);
                    // assign to cpu
                    onCpu = event.getProcess();

                    // schedule this process for departure
                    eventScheduler.scheduleEvent((clock + event.getProcess().getRemainingBurst()), event.getProcess(),
                            DEPARTURE);

                    // cpu not idle
                    if (!cpuIdle) {
                        rdQueue.add(event.getProcess());
                    }
                    Process nextProcess = new Process(event.getProcess().getId() + 1, clock + genexp(arrivalRate),
                            genexp(1 / serviceTime));
                    processes.add(nextProcess);

                    // send this process off to arrive
                    eventScheduler.scheduleEvent(nextProcess.getArrivalTime(), nextProcess, ARRIVAL);
                }
            }
                /*
                 *
                 * Departure event
                 *
                 */
                else if (event.getType() == DEPARTURE) {
                    // stat updates
                    event.getProcess().setCompletionTime(clock);
                    event.getProcess().setRemainingBurst(0);
                    processesSimulated++;

                    // place a waiting process in the ready queue then assign to cpu
                    if (!rdQueue.isEmpty()) {
                        Process processInQueue = rdQueue.getFirst();
                        rdQueue.pop();
                        onCpu = processInQueue;

                        // update times
                        onCpu.setLastTimeOnCpu(clock);
                        onCpu.setWaitTime(clock - onCpu.getArrivalTime());

                        // schedule this process for departure
                        eventScheduler.scheduleEvent(clock + processInQueue.getBurst(), processInQueue, DEPARTURE);
                    } else {
                        lastTimeCpuBusy = clock;
                        cpuIdle = true;
                        onCpu = null;
                    }
                    /*
                     * Quantums
                     */
                    // TODO: Remove event
                    eventScheduler.scheduleEvent(clock + quantum, null, TIMEOUT);
                } else if (event.getType() == TIMEOUT) {
                    if (!cpuIdle) {
                        onCpu.setRemainingBurst(onCpu.getLastTimeOnCpu() + onCpu.getRemainingBurst() - clock);
                        // TODO: Remove event
                        rdQueue.push(onCpu);

                        onCpu = rdQueue.getFirst();
                        rdQueue.pop();

                        // schedule new quantum
                        onCpu.setLastTimeOnCpu(clock);
                        eventScheduler.scheduleEvent(clock + quantum, null, TIMEOUT);

                        eventScheduler.scheduleEvent(clock + onCpu.getRemainingBurst(), onCpu, DEPARTURE);
                    }
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
