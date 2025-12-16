package com.zoplanner.notification.event;

import com.zoplanner.notification.model.NotificationPreference;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleUpdateEvent {

    private String teacherId;
    private String teacherEmail;
    private String source;
    private Instant eventTime;
    private LocalDateTime createdAt;
    private List<ScheduleChange> changes;
    private NotificationPreference preference;

    public ScheduleUpdateEvent() {
    }

    public ScheduleUpdateEvent(String teacherId, String teacherEmail, String source,
                               LocalDateTime createdAt, List<ScheduleChange> changes, NotificationPreference preference,
                               Instant eventTime) {
        this.teacherId = teacherId;
        this.teacherEmail = teacherEmail;
        this.source = source;
        this.createdAt = createdAt;
        this.changes = changes;
        this.preference = preference;
        this.eventTime = eventTime;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<ScheduleChange> getChanges() {
        return changes;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public void setTeacherEmail(String teacherEmail) {
        this.teacherEmail = teacherEmail;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setChanges(List<ScheduleChange> changes) {
        this.changes = changes;
    }

    public Instant getEventTime() { return eventTime;}

    public void setEventTime(Instant eventTime) { this.eventTime = eventTime;}

    public NotificationPreference getPreference() {
        return preference;
    }

    public void setPreference(NotificationPreference preference) {
        this.preference = preference;
    }

    @Override
    public String toString() {
        return "ScheduleUpdateEvent{" +
                "teacherId='" + teacherId + '\'' +
                ", teacherEmail='" + teacherEmail + '\'' +
                ", source='" + source + '\'' +
                ", preference=" + preference +
                ", eventTime=" + eventTime +
                ", createdAt=" + createdAt +
                ", changes=" + changes +
                '}';
    }

    // Nested klass för förändringar i schemat
    public static class ScheduleChange {

        private String lessonId;
        private String changeType; // t.ex. TIME_UPDATED, CANCELED
        private String oldValue;
        private String newValue;

        public ScheduleChange() {
        }

        public ScheduleChange(String lessonId, String changeType, String oldValue, String newValue) {
            this.lessonId = lessonId;
            this.changeType = changeType;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public String getLessonId() {
            return lessonId;
        }

        public String getChangeType() {
            return changeType;
        }

        public String getOldValue() {
            return oldValue;
        }

        public String getNewValue() {
            return newValue;
        }

        public void setLessonId(String lessonId) {
            this.lessonId = lessonId;
        }

        public void setChangeType(String changeType) {
            this.changeType = changeType;
        }

        public void setOldValue(String oldValue) {
            this.oldValue = oldValue;
        }

        public void setNewValue(String newValue) {
            this.newValue = newValue;
        }

        @Override
        public String toString() {
            return "ScheduleChange{" +
                    "lessonId='" + lessonId + '\'' +
                    ", changeType='" + changeType + '\'' +
                    ", oldValue='" + oldValue + '\'' +
                    ", newValue='" + newValue + '\'' +
                    '}';
        }
    }
}
