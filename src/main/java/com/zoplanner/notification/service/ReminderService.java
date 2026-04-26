package com.zoplanner.notification.service;

import com.zoplanner.notification.event.reminderevent.ReminderEvent;
import com.zoplanner.notification.model.Reminder;
import com.zoplanner.notification.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {
    private final ReminderRepository repository;

    public void saveReminder(ReminderEvent e) {

        Reminder r = new Reminder();

        r.setConsultantEmail(e.teacherEmail());
        r.setMessage(e.message());
        r.setSendAt(e.sendAt());
        r.setEventDate(e.eventDate());
        r.setStatus("PENDING");
        r.setCreatedAt(LocalDateTime.now());
        r.setAttempts(0);

        log.info("Saving reminder -> email={} sendAt={} message={}",
                r.getConsultantEmail(),
                r.getSendAt(),
                r.getMessage());


        repository.save(r);
        log.info("Reminder saved with id={}", r.getId());

    }



}
