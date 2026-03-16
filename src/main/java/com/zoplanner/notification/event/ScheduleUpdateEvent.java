package com.zoplanner.notification.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zoplanner.notification.model.NotificationPreference;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)

public class ScheduleUpdateEvent {

    @JsonAlias({"teacherId","TeacherId"})
    private String teacherId;
    @JsonAlias({"teacherEmail","TeacherEmail"})
    private String teacherEmail;
    @JsonAlias({"source","Source"})
    private String source;
    @JsonAlias({"eventTime","EventTime"})
    private Instant eventTime;
    @JsonAlias({"createdAt","CreatedAt"})
    private Instant createdAt;
    @JsonAlias({"changes","Changes"})
    private List<ScheduleChange> changes;
    @JsonAlias({"preference","Preference"})
    private NotificationPreference preference;
    @JsonAlias({"message","Message"})
    private String message;

    public ScheduleUpdateEvent() {
    }

    public ScheduleUpdateEvent(String teacherId, String teacherEmail, String source,
                               Instant createdAt, List<ScheduleChange> changes, NotificationPreference preference,
                               Instant eventTime, String message) {
        this.teacherId = teacherId;
        this.teacherEmail = teacherEmail;
        this.source = source;
        this.createdAt = createdAt;
        this.changes = changes;
        this.preference = preference;
        this.eventTime = eventTime;
        this.message = message;
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

    public Instant getCreatedAt() {
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

    public void setCreatedAt(Instant createdAt) {
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

    public void setMessage(String message) { this.message = message;}

    public String getMessage() {
        return message;
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

    // Nested klass för förändringar i schemat.
    public static class ScheduleChange {

        @JsonAlias({"lessonId","LessonId"})
        private String lessonId;
        @JsonAlias({"changeType","ChangeType"})
        private String changeType; // t.ex. TIME_UPDATED, CANCELED
        @JsonAlias({"oldValue","OldValue"})
        private String oldValue;
        @JsonAlias({"newValue","NewValue"})
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
