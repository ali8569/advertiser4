package ir.markazandroid.advertiser.network.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import ir.markazandroid.advertiser.network.JSONParser.annotations.JSON;


/**
 * Coded by Ali on 30/06/2017.
 * version 1.4
 */

public class Parser {

    protected ConcurrentHashMap<String, ArrayList<Methods>> classes;

    public Parser() {
        classes = new ConcurrentHashMap<>();
    }

    public void addClass(Class c) throws NoSuchMethodException {
        Method[] methods = c.getDeclaredMethods();
        ArrayList<Methods> mMethods = new ArrayList<>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(JSON.class)) {
                if (method.getName().startsWith("get")) {
                    mMethods.add(new Methods(method,
                            c.getDeclaredMethod(
                                    method.getName().replaceFirst("get", "set")
                                    , method.getReturnType()), method.getAnnotation(JSON.class)));
                }
            }
        }
        classes.put(c.getName(), mMethods);
    }

    public void addSubClass(Class c) throws NoSuchMethodException {
        Method[] methods = c.getSuperclass().getDeclaredMethods();
        ArrayList<Methods> mMethods = new ArrayList<>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(JSON.class)) {
                if (method.getName().startsWith("get")) {
                    mMethods.add(new Methods(method,
                            c.getSuperclass().getDeclaredMethod(
                                    method.getName().replaceFirst("get", "set")
                                    , method.getReturnType()), method.getAnnotation(JSON.class)));
                }
            }
        }
        methods = c.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(JSON.class)) {
                if (method.getName().startsWith("get")) {
                    mMethods.add(new Methods(method,
                            c.getDeclaredMethod(
                                    method.getName().replaceFirst("get", "set")
                                    , method.getReturnType()), method.getAnnotation(JSON.class)));
                }
            }
        }
        classes.put(c.getName(), mMethods);
    }

    private <T> T getObject(Class<T> c, JSONObject json) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        ArrayList<Methods> methods = classes.get(c.getName());
        T object = c.newInstance();
        for (Methods method : methods) {
            Object o;
            if (!method.annotation.name().equals("")) {
                o = json.opt(method.annotation.name());
            } else {
                StringBuilder b = new StringBuilder(method.setter.getName());
                b.setCharAt(3, Character.toLowerCase(b.charAt(3)));
                o = json.opt(b.substring(3));
            }
            if (o != null) {
                try {
                    if (method.annotation.classType().equals(""))
                        method.setter.invoke(object, o);
                    else {
                        invokeSetter(object, o, method);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        return object;
    }

    private JSONObject getJSON(Object object) throws IllegalAccessException, JSONException, InvocationTargetException {
        JSONObject json = new JSONObject();
        if (object == null) return null;
        ArrayList<Methods> methods = classes.get(object.getClass().getName());
        for (Methods method : methods) {
            String name;
            if (!method.annotation.name().equals("")) {
                name = method.annotation.name();
            } else {
                StringBuilder b = new StringBuilder(method.getter.getName());
                b.setCharAt(3, Character.toLowerCase(b.charAt(3)));
                name = b.substring(3);
            }
            if (method.annotation.classType().equals(""))
                json.put(name, method.getter.invoke(object));
            else
                json.put(name, invokeGetter(object, method));
        }
        return json;
    }

    private Object invokeGetter(Object source, Methods methods) throws InvocationTargetException, IllegalAccessException {
        String valueType = methods.annotation.classType();
        Method method = methods.getter;
        if (valueType.equalsIgnoreCase(JSON.CLASS_TYPE_SHORT)) {
            try {
                return ((Short) method.invoke(source)).intValue();

            } catch (NullPointerException e) {
                return null;
            }
        } else if (valueType.equalsIgnoreCase(JSON.CLASS_TYPE_BYTE)) {
            try {
                return ((Byte) method.invoke(source)).intValue();
            } catch (NullPointerException e) {
                return null;
            }
        } else if (valueType.equalsIgnoreCase(JSON.CLASS_TYPE_TIMESTAMP)) {
            try {
                return ((Timestamp) method.invoke(source))
                        .getTime();
            } catch (NullPointerException e) {
                return null;
            }
        } else if (valueType.equalsIgnoreCase(JSON.CLASS_TYPE_BOOLEAN)) {
            try {
                return (Byte) method.invoke(source) > 0;
            } catch (NullPointerException e) {
                return false;
            }
        } else if (valueType.equalsIgnoreCase(JSON.CLASS_TYPE_ARRAY)) {
            try {
                if (methods.annotation.clazz().equals(Object.class))
                    return new JSONArray(((Collection) method.invoke(source)));
                return getArray(((Collection) method.invoke(source)));
            } catch (NullPointerException e) {
                return null;

            }
        } else if (valueType.equalsIgnoreCase(JSON.CLASS_TYPE_OBJECT)) {
            try {
                return get(method.invoke(source));
            } catch (NullPointerException e) {
                return null;
            }
        } else if (valueType.equalsIgnoreCase(JSON.CLASS_TYPE_JSON_ARRAY)) {
            try {
                return new JSONArray((String) method.invoke(source));
            } catch (NullPointerException e) {
                return null;

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else if (valueType.equalsIgnoreCase(JSON.CLASS_TYPE_JSON_OBJECT)) {
            try {
                return new JSONObject((String) method.invoke(source));
            } catch (NullPointerException e) {
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else
            return method.invoke(source);
    }

    private void invokeSetter(Object source, Object parameter, Methods methods) throws InvocationTargetException, IllegalAccessException {
        String valueType = methods.annotation.classType();
        Method method = methods.setter;
        if (valueType.equalsIgnoreCase(JSON.CLASS_TYPE_SHORT))
            method.invoke(source, ((Integer) parameter).shortValue());
        else if (valueType.equalsIgnoreCase(JSON.CLASS_TYPE_BYTE))
            method.invoke(source, ((Integer) parameter).byteValue());
        else if (valueType.equalsIgnoreCase(JSON.CLASS_TYPE_TIMESTAMP))
            method.invoke(source, new Timestamp((long) (parameter)));
        else if (valueType.equalsIgnoreCase(JSON.CLASS_TYPE_ARRAY))
            if (methods.annotation.clazz().equals(Object.class))
                method.invoke(source, JSONArrayToArrayList((JSONArray) parameter));
            else
                method.invoke(source, get(methods.annotation.clazz(), (JSONArray) parameter));
        else if (valueType.equalsIgnoreCase(JSON.CLASS_TYPE_OBJECT))
            method.invoke(source, get(methods.annotation.clazz(), (JSONObject) parameter));
        else
            method.invoke(source, parameter);
    }

    public <T> T get(Class<T> c, JSONObject json) {
        try {
            return getObject(c, json);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> ArrayList<T> get(Class<T> c, JSONArray jarray) {
        ArrayList<T> array = new ArrayList<>();
        for (int i = 0; i < jarray.length(); i++) {
            try {
                array.add(getObject(c, jarray.optJSONObject(i)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    public JSONObject get(Object object) {
        try {
            return getJSON(object);
        } catch (IllegalAccessException | JSONException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> JSONArray getArray(Collection<T> list) {
        JSONArray array = new JSONArray();
        for (T o : list) {
            try {
                array.put(getJSON(o));
            } catch (IllegalAccessException | JSONException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    private ArrayList JSONArrayToArrayList(JSONArray jsonArray) {
        ArrayList arrayList = new ArrayList(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                arrayList.add(jsonArray.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

    private class Methods {
        Method getter;
        Method setter;
        JSON annotation;

        public Methods(Method getter, Method setter, JSON annotation) {
            this.getter = getter;
            this.setter = setter;
            this.annotation = annotation;
        }
    }
}
