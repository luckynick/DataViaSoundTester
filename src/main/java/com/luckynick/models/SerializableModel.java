package com.luckynick.models;

import com.luckynick.Utils;
import com.luckynick.models.profiles.Configurable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

public abstract class SerializableModel implements Serializable {

    public String nameOfModelClass = this.getClass().getSimpleName();
    protected String filenamePrefix = nameOfModelClass;
    public String filename= filenamePrefix + Utils.JSON_EXTENSION;
    public String fileRoot =  Utils.formPathString("data", "models");
    public String wholePath = fileRoot + filename;

    public void appendSubfolderToFileRoot(String subfolder) {
        fileRoot = Utils.formPathString(fileRoot, subfolder);
        wholePath = fileRoot + filename;
    }

    public Object getValue(String fieldName) {
        return getValueFromField(getFieldForName(fieldName));
    }

    public void setValue(String fieldName, Object value) {
        setValueInField(getFieldForName(fieldName), value);
    }

    public void setValue(Component value) {
        setValueInField(getFieldForName(value.getName()), value);
    }

    public Component resolveSwingComponent(String field) {
        return resolveSwingComponent(getFieldForName(field));
    }

    public String[] getAllowedFieldNamesArray() {
        Field[] fields = this.getClass().getDeclaredFields();
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            if(fields[i].getAnnotation(Configurable.class) != null) result.add(fields[i].getName());
        }
        return result.toArray(new String[0]);
    }

    private String[] getFieldNamesArray() {
        Field[] fields = this.getClass().getDeclaredFields();
        String[] result = new String[fields.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = fields[i].getName();
        }
        return result;
    }

    public Field[] getFieldsArray() {
        return this.getClass().getDeclaredFields();
    }



    private Component resolveSwingComponent(Field field) {
        Component result = new JTextField("", 30);
        Object value = getValueFromField(field);
        //TODO
        //result = new JTextField((value != null ? value.toString() : "*empty*"), 30);
        JTextField textField = new JTextField((value != null ? getValueFromField(field).toString() : "*empty*"), 30);
        textField.setActionCommand(field.getName());
        textField.setName(field.getName());

        result = textField;
        return result;
    }

    private Field getFieldForName(String fieldName) {
        for (Field f: getFieldsArray()) {
            if(f.getName().equals(fieldName)) return f;
        }
        return null;
    }

    private Object getValueFromField(Field field) {
        field.setAccessible(true);
        Object result = null;
        try {
            result = field.get(this);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        field.setAccessible(false);
        return result;
    }

    private void setValueInField(Field field, Component value) {
        System.out.println("setValueInField component: " + value.getClass().getSimpleName());
        if(value instanceof JTextField) {
            setValueInField(field, ((JTextField) value).getText());
        }
        else throw new NotImplementedException(); //TODO
    }

    private void setValueInField(Field field, Object value) {
        System.out.println("Field name: " + field.getName() + ", type: " + field.getAnnotatedType().getType().getTypeName());
        field.setAccessible(true);
        try {
            field.set(this, value);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        field.setAccessible(false);
    }
}
