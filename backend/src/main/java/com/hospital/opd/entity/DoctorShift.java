package com.hospital.opd.entity;

public enum DoctorShift {
    MORNING("Morning Batch (09:00 - 13:00)", 9, 13),
    EVENING("Evening Batch (14:00 - 18:00)", 14, 18),
    ALL_DAY("Full Day / Both Batches (09:00 - 18:00)", 9, 18);

    private final String displayName;
    private final int startHour;
    private final int endHour;

    DoctorShift(String displayName, int startHour, int endHour) {
        this.displayName = displayName;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getEndHour() {
        return endHour;
    }
}
