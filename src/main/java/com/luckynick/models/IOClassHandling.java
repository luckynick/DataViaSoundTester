package com.luckynick.models;

import com.luckynick.Utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE})
public @interface IOClassHandling {
    boolean sendViaNetwork() default false;
    Utils.DataStorage dataStorage();
}
