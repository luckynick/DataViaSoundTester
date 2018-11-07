package com.luckynick.models;


import com.luckynick.CustomJFrame;
import com.luckynick.custom.Device;
import com.luckynick.models.profiles.SequentialTestProfile;
import com.luckynick.models.profiles.SingleTestProfile;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.shared.IOFieldHandling;
import com.luckynick.shared.SharedUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.luckynick.custom.Utils.Log;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.MODELS)
public abstract class SerializableModel {

    public static final String LOG_TAG = "SerializableModel";

    public static final String EMPTY_LABEL = "*empty*";

    public String nameOfModelClass = this.getClass().getSimpleName();
    @IOFieldHandling(serialize = false)
    protected String filenamePrefix = nameOfModelClass;
    @ManageableField(editable = false) //fals
    public String filename;
    @IOFieldHandling(serialize = false)
    public String fileRoot = this.getClass().getDeclaredAnnotation(IOClassHandling.class).dataStorage().toString();
    public String wholePath = fileRoot + filename;

    public SerializableModel() {
        setFilename();
    }

    public void setFilename() {
        setFilename(filenamePrefix + SharedUtils.JSON_EXTENSION);
    }

    public void setFilename(String filename) {
        this.filename = filename;
        wholePath = fileRoot + filename;
    }

    public void appendSubfolderToFileRoot(String ... subfolders) {
        for (String folder: subfolders) {
            appendSubfolderToFileRoot(folder);
        }
    }
    @Override
    public String toString() {
        return filename;
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

    public Field[] getFieldsArray() {

        Field[] inheritedFields = getClass().getFields();
        Field[] ownFields = getClass().getDeclaredFields();
        ArrayList<Field> allFieldsList = new ArrayList<>();
        for(Field f : inheritedFields) {
            allFieldsList.add(f);
        }
        for(Field f : ownFields) {
            if(!allFieldsList.contains(f)) allFieldsList.add(f);
        }
        return allFieldsList.toArray(new Field[0]);
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
        if(SharedUtils.isReflectedAsNumber(field.getType()) && (int) value == -1) return false;
        if(Collection.class.isAssignableFrom(field.getType()) && ((Collection) value).size() < 1) return false;
        return true;
    }

    private Component resolveSwingComponent(Field field) {
        SerializableModel thisObj = this;
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
        else if(Collection.class.isAssignableFrom(field.getType())) { //if list
            ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
            Class<?> listGenericType = (Class<?>) stringListType.getActualTypeArguments()[0];

            if(String.class.isAssignableFrom(listGenericType)) {
                result = createButtonWithActionForStringList(field);
            }
            else {
                result = createButtonWithActionForList(field, listGenericType);
            }
        }
        else if(String.class.isAssignableFrom(field.getType()) || SharedUtils.isReflectedAsNumber(field.getType())) {
            JTextField textField = new JTextField((value != null ?
                    getValueFromField(field).toString() : EMPTY_LABEL), 30);
            result = textField;
        }
        else {
            result = createButtonWithAction(field, field.getType());
        }
        String componentName = field.getName();
        //if(isFieldRequired(field)) componentName = "* " + componentName; //TODO
        result.setName(componentName);
        result.setEnabled(editable);
        return result;
    }

    private JButton createButtonWithActionForStringList(Field buttonCorrespondence) {
        SerializableModel thisObj = this;
        JButton button = new JButton();
        button.setAction(new AbstractAction("Select strings") {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    List<String> result = StringPopulator.requireStrings();
                    try {
                        buttonCorrespondence.set(thisObj, result);
                    }
                    catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                }).start();
            }
        });
        return button;
    }

    private JButton createButtonWithActionForList(Field buttonCorrespondence, Class<?> modelClass) {
        SerializableModel thisObj = this;
        JButton button = new JButton();
        button.setAction(new AbstractAction("Select item") {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    List<SerializableModel> result = ModelSelector.requireSelection(
                            new ModelIO<>((Class<SerializableModel>) modelClass), true);
                    try {
                        buttonCorrespondence.set(thisObj, result);
                    }
                    catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                }).start();
            }
        });
        return button;
    }

    private JButton createButtonWithAction(Field buttonCorrespondence, Class<?> modelClass) {
        SerializableModel thisObj = this;
        JButton button = new JButton();
        button.setAction(new AbstractAction("Select item") {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    List<SerializableModel> result = ModelSelector.requireSelection(
                            new ModelIO<>((Class<SerializableModel>) modelClass), false);
                    Log(LOG_TAG, "Size of selection: " + result.size());
                    try {
                        if(result.size() > 0) {
                            buttonCorrespondence.set(thisObj, result.get(0));
                        }
                    }
                    catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                }).start();
            }
        });
        return button;
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
            try {
                if("int".equals(fieldTypeName)) toSet = (int) Double.parseDouble(textInField);
                else if("double".equals(fieldTypeName)) toSet = Double.parseDouble(textInField);
            } catch (NumberFormatException ex) {
            }
        }
        return toSet;
    }

    private void setValueInField(Field field, Object value) {
        field.setAccessible(true);
        try {
            if(EMPTY_LABEL.equals(value)) return;
            else if(SharedUtils.isReflectedAsNumber(field.getType()) && value == null)
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
