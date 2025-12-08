package com.zoplanner.notification.queue;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class InMemoryEventQueueService {

    private static final Logger log = LoggerFactory.getLogger(InMemoryEventQueueService.class);

    private final Queue<ScheduleUpdateEvent> queue = new ConcurrentLinkedQueue<>();

    public void enqueue(ScheduleUpdateEvent event) {
        if (event == null) {
            log.warn("Tried to enqueue null event");
            return;
        }
        queue.add(event);
        log.info("Enqueued schedule update event for teacherId={}, currentQueueSize={}",
                event.getTeacherId(), queue.size());
    }

    public ScheduleUpdateEvent pollNext() {
        ScheduleUpdateEvent event = queue.poll();
        if (event == null) {
            log.info("No event available to process (queue is empty)");
        } else {
            log.info("Dequeued schedule update event for teacherId={}, remainingQueueSize={}",
                    event.getTeacherId(), queue.size());
        }
        return event;
    }

    public int getQueueSize() {
        return queue.size();
    }
}
