package cpuscheduler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class EventScheduler {
    private final LinkedList<Event> eventSet = new LinkedList<>();

    /**
     *Schedule and Event
     *@param time The time at which the event is scheduled
     *@param p A pointer to the process the event refereneces
     *@param type The type of event
     */
    public void ScheduleEvent(float time, Process p, EventType type){
        Event newEvent = new Event(type, time, p);
        // insert even in sorted order based on earliest time scheduled to last
        eventSet.add(newEvent);
    }

    Event GetEvent(){
        Event event = eventSet.getFirst();
        eventSet.removeFirst();
        return event;
    }
    boolean Empty() { return eventSet.isEmpty(); }
}
