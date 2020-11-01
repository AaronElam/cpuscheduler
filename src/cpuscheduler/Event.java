package cpuscheduler;

enum EventType { ARRIVAL, DEPARTURE, TIMEOUT, QUERY };

public class Event {

    private final EventType type;
    private final float time;
    private final Process process;

    public Event(EventType type, float time, Process process) {
        this.process = process;
        this.time = time;
        this.type = type;
    }

    float GetTime() {
        return time;
    }
    EventType GetType() {
        return type;
    }
    Process GetProcess() {
        return process;
    }
//    // the default operator the set<EventScheduler> uses for comparison
//    bool operator<(const Event& other) const;
};
