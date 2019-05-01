package ir.markazandroid.advertiser.util;

/**
 * Coded by Ali on 8/28/2018.
 */
public enum GregorianDaysOfWeek {

    SUNDAY("Sunday"),
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday");

    private String name;

    GregorianDaysOfWeek(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
