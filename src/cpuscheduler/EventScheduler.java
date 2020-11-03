package cpuscheduler;

import java.util.LinkedList;
import java.util.ListIterator;

public class EventScheduler {
    private final LinkedList<Event> eventSet = new LinkedList<>();

    public void scheduleEvent(float time, Process p, EventType type) {
        Event newEvent = new Event(type, time, p);
        eventSet.add(newEvent);
    }

    public Event getEvent() {
        Event event = eventSet.getFirst();
        eventSet.removeFirst();
        return event;
    }

    public void removeEvent(int pid, EventType type) {
//        ListIterator<Event> li = eventSet.listIterator();
//        int i = 0;
//        while(li.hasNext()) {
//            System.out.println(pid);
//            System.out.println(eventSet.get(i).getProcess().getId());
//            if(pid == eventSet.get(i).getProcess().getId() && type == eventSet.get(i).getType()) {
//                eventSet.remove(i);
//            }
//            i++;
//        }
        //eventSet.remove(pid);
    }

    public boolean isEmpty() {
        return eventSet.isEmpty();
    }
}
