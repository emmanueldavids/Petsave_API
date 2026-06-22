package com.petsave.petsave.Entity;

public enum AdoptionCheckInMilestone {
    ONE_WEEK(7),
    ONE_MONTH(30),
    THREE_MONTHS(90),
    SIX_MONTHS(180),
    ONE_YEAR(365);

    private final int daysAfterAdoption;

    AdoptionCheckInMilestone(int daysAfterAdoption) {
        this.daysAfterAdoption = daysAfterAdoption;
    }

    public int getDaysAfterAdoption() {
        return daysAfterAdoption;
    }
}
