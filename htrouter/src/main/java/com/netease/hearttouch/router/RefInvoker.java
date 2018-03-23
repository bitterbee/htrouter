package com.netease.hearttouch.router;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zyl06 on 22/03/2018.
 */

class RefInvoker {
    private static final String TAG = "RefInvoker";

    @SuppressWarnings("rawtypes")
    public static Object invokeStaticMethod(String className, String methodName, Class[] paramTypes,
                                            Object[] paramValues) {

        return invokeMethod(null, className, methodName, paramTypes, paramValues);
    }

    @SuppressWarnings("rawtypes")
    public static Object invokeMethod(Object target, String className, String methodName, Class[] paramTypes,
                                      Object[] paramValues) {

        try {
            Class clazz = Class.forName(className);
            return invokeMethod(target, clazz, methodName, paramTypes, paramValues);
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invoke(Object target, Method m, Object ... args) {
        try {
            return m.invoke(target, args);
        } catch (IllegalAccessException e) {
            HTLogUtil.d(e.toString());
        } catch (IllegalArgumentException e) {
            HTLogUtil.d(e.toString());
        } catch (InvocationTargetException e) {
            HTLogUtil.d(e.toString());
        } catch (Exception e) {
            HTLogUtil.d(e.toString());
        }
        return null;
    }

    public static Object invokeMethod(Object target, Class clazz, String methodName, Class[] paramTypes,
                                      Object[] paramValues) {
        try {
            //LogUtil.e("Method", methodName);
            Method method = clazz.getDeclaredMethod(methodName, paramTypes);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method.invoke(target, paramValues);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            HTLogUtil.d(e.toString());
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Object getFieldObject(Object target, String fieldName) {
        return getFieldObject(target, target.getClass(), fieldName);
    }

    @SuppressWarnings("rawtypes")
    public static Object getFieldObject(Object target, Class clazz, String fieldName) {
        return getFieldObject(target, clazz, fieldName, true);
    }

    @SuppressWarnings("rawtypes")
    public static Object getFieldObject(Object target, Class clazz, String fieldName, boolean forceAccess) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (forceAccess && !field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(target);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // try supper for Miui, Miui has a class named MiuiPhoneWindow
            try {
                Field field = clazz.getSuperclass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (Exception superE) {
                e.printStackTrace();
                superE.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Object getFieldObject(Object target, String className, String fieldName) {
        Class clazz = null;
        try {
            clazz = Class.forName(className);
            return getFieldObject(target, clazz, fieldName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getStaticFieldObject(String className, String fieldName) {
        return getFieldObject(null, className, fieldName);
    }

    @SuppressWarnings("rawtypes")
    public static void setFieldObject(Object target, String className, String fieldName, Object fieldValue) {
        Class clazz = null;
        try {
            clazz = Class.forName(className);
            setFieldObject(target, clazz, fieldName, fieldValue);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setFieldObject(Object target, Class clazz, String fieldName, Object fieldValue) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(target, fieldValue);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // try supper for Miui, Miui has a class named MiuiPhoneWindow
            try {
                Field field = clazz.getSuperclass().getDeclaredField(fieldName);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                field.set(target, fieldValue);
            } catch (Exception superE) {
                e.printStackTrace();
                superE.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setStaticOjbect(String className, String fieldName, Object fieldValue) {
        setFieldObject(null, className, fieldName, fieldValue);
    }

    public static Method findMethod(Object object, String methodName, Class[] clazzes) {
        try {
            return object.getClass().getDeclaredMethod(methodName, clazzes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method findMethod(Object object, String methodName, Object[] args) {
        if (args == null) {
            try {
                return object.getClass().getDeclaredMethod(methodName, (Class[])null);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            Method[] methods = object.getClass().getDeclaredMethods();
            boolean isFound = false;
            Method method = null;
            for(Method m: methods) {
                if (m.getName().equals(methodName)) {
                    Class<?>[] types = m.getParameterTypes();
                    if (types.length == args.length) {
                        isFound = true;
                        for(int i = 0; i < args.length; i++) {
                            if (!(types[i] == args[i].getClass() || (types[i].isPrimitive() && primitiveToWrapper(types[i]) == args[i].getClass()))) {
                                isFound = false;
                                break;
                            }
                        }
                        if (isFound) {
                            method = m;
                            break;
                        }
                    }
                }
            }
            return  method;
        }
    }

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP = new HashMap<>();

    static {
        PRIMITIVE_WRAPPER_MAP.put(Boolean.TYPE, Boolean.class);
        PRIMITIVE_WRAPPER_MAP.put(Byte.TYPE, Byte.class);
        PRIMITIVE_WRAPPER_MAP.put(Character.TYPE, Character.class);
        PRIMITIVE_WRAPPER_MAP.put(Short.TYPE, Short.class);
        PRIMITIVE_WRAPPER_MAP.put(Integer.TYPE, Integer.class);
        PRIMITIVE_WRAPPER_MAP.put(Long.TYPE, Long.class);
        PRIMITIVE_WRAPPER_MAP.put(Double.TYPE, Double.class);
        PRIMITIVE_WRAPPER_MAP.put(Float.TYPE, Float.class);
        PRIMITIVE_WRAPPER_MAP.put(Void.TYPE, Void.TYPE);
    }

    static Class<?> primitiveToWrapper(final Class<?> cls) {
        Class<?> convertedClass = cls;
        if (cls != null && cls.isPrimitive()) {
            convertedClass = PRIMITIVE_WRAPPER_MAP.get(cls);
        }
        return convertedClass;
    }
}
