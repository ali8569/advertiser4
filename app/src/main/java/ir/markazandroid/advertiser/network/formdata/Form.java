package ir.markazandroid.advertiser.network.formdata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Coded by Ali on 29/11/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Form {
    String FILE = "file";
    String MAP = "map";
    String BYTE = "byte";
    String COLLECTION = "collection";

    String name() default "";

    String type() default "";
}
