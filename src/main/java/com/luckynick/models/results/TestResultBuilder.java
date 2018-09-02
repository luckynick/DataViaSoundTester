package com.luckynick.models.results;

import java.lang.reflect.InvocationTargetException;

@Deprecated
abstract class TestResultBuilder<T extends TestResult> {

    private Class<T> resultClass;
    private String requireField;

    private Object[] constructorArgs;

    public TestResultBuilder(Class<T> resultClass, Object[] constructorArgs) {
        this.resultClass = resultClass;
        this.constructorArgs = constructorArgs;
    }

    public T build() {
        return callConstructor(constructorArgs);
    }

    private T callConstructor(Object ... args) {
        if(!readyForBuild()) throw new IllegalStateException("Field " + requireField + " must be set.");
        try {
            return resultClass.getConstructor(resultClass).newInstance(args);
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean readyForBuild() {
        for(int i = 0; i < constructorArgs.length; i++) {
            if(constructorArgs[i] == null)
            {
                requireField = "" + i;
                return false;
            }
        }
        return true;
    }
}
