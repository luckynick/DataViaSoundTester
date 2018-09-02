package com.luckynick.models;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IOFieldHandling {
    public boolean serialize() default true;
    public boolean updateOnLoad() default false;
}
