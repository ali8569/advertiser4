package ir.markazandroid.advertiser.network.JSONParser.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Coded by Ali on 30/06/2017.
 * version 1.3
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface JSON {

    String CLASS_TYPE_SHORT="short";
    String CLASS_TYPE_BYTE="byte";
    String CLASS_TYPE_TIMESTAMP="timestamp";
    String CLASS_TYPE_BOOLEAN="boolean";
    String CLASS_TYPE_ARRAY="array";
    String CLASS_TYPE_OBJECT="object";
    String CLASS_TYPE_JSON_ARRAY="jsonarray";
    String CLASS_TYPE_JSON_OBJECT="jsonobject";

    String value() default "";

    String name() default "";

    String classType() default "";

    Class clazz() default Object.class;
}
