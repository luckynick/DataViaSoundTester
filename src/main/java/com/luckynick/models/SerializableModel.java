package com.luckynick.models;

import com.luckynick.Utils;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

@IOClassHandling(dataStorage = Utils.DataStorage.MODELS)
public abstract class SerializableModel {

    public String nameOfModelClass = this.getClass().getSimpleName();
    @IOFieldHandling(serialize = false)
    protected String filenamePrefix = nameOfModelClass;
    @ManageableField(editable = false)
    public String filename= filenamePrefix + Utils.JSON_EXTENSION;
    @IOFieldHandling(serialize = false)
    public String fileRoot = this.getClass().getDeclaredAnnotation(IOClassHandling.class).dataStorage().toString();
    public String wholePath = fileRoot + filename;

    public void setFilename(String filename) {
        this.filename = filename;
        wholePath = fileRoot + filename;
    }

    public void appendSubfolderToFileRoot(String ... subfolders) {
        for (String folder: subfolders) {
            appendSubfolderToFileRoot(folder);
        }
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

    public String[] getConfigurableFieldNames() {
        Field[] fields = getFieldsArray();
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            if(fields[i].getAnnotation(ManageableField.class) != null) result.add(fields[i].getName());
        }
        return result.toArray(new String[0]);
    }

    private String[] getFieldNamesArray() {
        Field[] fields = getFieldsArray();
        String[] result = new String[fields.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = fields[i].getName();
        }
        return result;
    }

    public Field[] getFieldsArray() {

        Field[] inheritedFields = getClass().getFields();
        Field[] ownFields = getClass().getDeclaredFields();
        Field[] allFields = new Field[inheritedFields.length+ownFields.length];
        int i_all = 0;
        for(int i = 0; i < inheritedFields.length; i++) {
            allFields[i_all++] = inheritedFields[i];
        }
        for(int i = 0; i < ownFields.length; i++) {
            allFields[i_all++] = ownFields[i];
        }
        return allFields;
    }

    public boolean allRequiredFieldsAreSet() {
        for(Field f : getFieldsArray()) {
            if(isFieldRequired(f) && !isFieldSet(f)) return false;
        }
        return true;
    }

    public boolean isFieldRequired(Field field) {
        ManageableField a = field.getAnnotation(ManageableField.class);
        return a != null ? a.required() : false;
    }

    private boolean isFieldSet(Field field) {
        Object value = getValueFromField(field);
        if(value == null) return false;
        if(Utils.isReflectedAsNumber(field.getType()) && (int) value == -1) return false;
        return true;
    }



    private Component resolveSwingComponent(Field field) {
        Component result = new JTextField("", 30);
        Object value = getValueFromField(field);
        boolean editable = field.getAnnotation(ManageableField.class).editable();

        if(field.getType().isEnum()) {
            String selectedEnum = null;
            int selectedIndex = -1;
            if(value != null) {
                selectedEnum = value.toString();
            }
            Enum[] e = (Enum[])field.getType().getEnumConstants();
            String[] enumNames = new String[e.length];
            for(int i = 0; i < e.length; i++) {
                enumNames[i] = e[i].toString();
                if(enumNames[i].equals(selectedEnum)) selectedIndex = i;
            }
            JComboBox<String> enumList = new JComboBox<>(enumNames);
            enumList.setSelectedIndex(selectedIndex);
            result = enumList;
        }
        else {
            JTextField textField = new JTextField((value != null ? getValueFromField(field).toString() : "*empty*"), 30);
            result = textField;
        }
        String componentName = field.getName();
        //if(isFieldRequired(field)) componentName = "* " + componentName; //TODO
        result.setName(componentName);
        result.setEnabled(editable);
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
        if(value instanceof JTextField) {
            String textInField = ((JTextField) value).getText();
            setValueInField(field, parseTextField(field, textInField));
        }
        else if(value instanceof JComboBox) {
            String selectedItem = (String) ((JComboBox) value).getSelectedItem();
            setValueInField(field, selectedItem != null ?
                    Enum.valueOf((Class<Enum>) field.getType(), selectedItem) : null);
        }
    }

    private Object parseTextField(Field field, String textInField) {
        if(textInField == null || "".equals(textInField)) return null;
        Object toSet = textInField;
        String fieldTypeName = field.getType().getSimpleName();
        try {
            if("int".equals(fieldTypeName)) toSet = Integer.parseInt(textInField);
            else if("double".equals(fieldTypeName)) toSet = (double) Integer.parseInt(textInField);
        } catch (NumberFormatException e) {
            System.out.println(textInField + " not integer");
            try {
                if("int".equals(fieldTypeName)) toSet = (int) Double.parseDouble(textInField);
                else if("double".equals(fieldTypeName)) toSet = Double.parseDouble(textInField);
            } catch (NumberFormatException ex) {
                System.out.println(textInField + " not double");
            }
        }
        return toSet;
    }

    private void setValueInField(Field field, Object value) {
        field.setAccessible(true);
        try {
            if(Utils.isReflectedAsNumber(field.getType()) && value == null)
                field.set(this, -1);
            else
                field.set(this, value);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        field.setAccessible(false);
    }
}
