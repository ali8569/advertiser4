package ir.markazandroid.advertiser.util;

/**
 * Coded by Ali on 8/28/2018.
 */
public enum PersianDaysOfWeek {

    SUNDAY("یکشنبه"),
    MONDAY("دوشنبه"),
    TUESDAY("سه\u200Cشنبه"),
    WEDNESDAY("چهارشنبه"),
    THURSDAY("پنجشنبه"),
    FRIDAY("جمعه"),
    SATURDAY("شنبه");

    private String name;

    PersianDaysOfWeek(String name){
        this.name=name;
    }

    @Override
    public String toString() {
        return name;
    }
}
