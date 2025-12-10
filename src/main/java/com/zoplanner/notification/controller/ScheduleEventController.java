package com.zoplanner.notification.controller;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.queue.InMemoryEventQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/events/schedule-update")
public class ScheduleEventController {

    private static final Logger log = LoggerFactory.getLogger(ScheduleEventController.class);

    private final InMemoryEventQueueService queueService;

    public ScheduleEventController(InMemoryEventQueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * Lägg ett nytt schedule-update-event i in-memory-kön.
     */
    @PostMapping
    public ResponseEntity<Void> enqueueEvent(@RequestBody ScheduleUpdateEvent event) {
        log.info("Received schedule update event to enqueue");
        queueService.enqueue(event);
        return ResponseEntity.accepted().build();
    }

    /**
     * Plocka nästa event från kön (simulerar konsumtion).
     * Returnerar 204 No Content om kön är tom.
     */
    @PostMapping("/process-next")
    public ResponseEntity<ScheduleUpdateEvent> processNext() {
        ScheduleUpdateEvent next = queueService.pollNext();
        if (next == null) {
            return ResponseEntity.noContent().build();
        }
        // Här skulle ni i framtiden kunna koppla på NotificationService etc.
        log.info("Processed schedule update event for teacherId={}", next.getTeacherId());
        return ResponseEntity.ok(next);
    }

    /**
     * Kolla hur många events som finns i kön just nu.
     */
    @GetMapping("/queue-size")
    public ResponseEntity<Map<String, Integer>> getQueueSize() {
        int size = queueService.getQueueSize();
        Map<String, Integer> body = new HashMap<>();
        body.put("queueSize", size);
        return ResponseEntity.ok(body);
    }
}
