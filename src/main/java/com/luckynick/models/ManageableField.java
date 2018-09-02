package com.luckynick.models;

import com.luckynick.Utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManageableField {
    public boolean editable() default true;
    public boolean required() default false;
}
