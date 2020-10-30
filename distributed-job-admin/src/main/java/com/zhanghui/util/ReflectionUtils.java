package com.zhanghui.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @description: 反射工具类
 * @author: LeoLee
 * @company: ***
 * @version:
 * @date: 2019/7/12 14:41
 */
public class ReflectionUtils {

    public static final String SETTER_PREFIX = "set";
    public static final String GETTER_PREFIX = "get";
    public static final String LIST_TAG = "java.util.List";

    /**
     * 设置实体属性
     * 不安全
     * @param obj
     * @param fieldName
     * @param fieldvalue
     */
    public static void setBeanFieldValue(Object obj,Class<?> aClass,String fieldName, Object fieldvalue) {
        try {
            Field declaredField = aClass.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            declaredField.set(obj,fieldvalue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 设置实体属性
     * @param obj
     * @param fieldName
     * @param fieldvalue
     */
    public static void setBeanAttr(Object obj, String fieldName, Object fieldvalue) {
        // 将属性名的首字母变为大写，为执行set/get方法做准备
        String methodName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method setMethod = null;
        Method getMethod = null;
        try {
            getMethod = obj.getClass().getMethod(GETTER_PREFIX + methodName);
            setMethod = obj.getClass().getMethod(SETTER_PREFIX + methodName,getMethod.getReturnType());
            // 如果该类中不存在set
            if (setMethod == null) {
                // 找父类
                setMethod = obj.getClass().getSuperclass().getMethod(SETTER_PREFIX + methodName, getMethod.getReturnType());
            }
            // set器不为空
            if (setMethod != null) {
                setMethod.invoke(obj, converseType(fieldvalue, getMethod.getReturnType()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 数据类型转换
     * @param param
     * @param paramClass
     * @return
     */
    public static Object converseType(Object param, Class<?> paramClass) {
        if(LIST_TAG.equals(paramClass.getName())){
            return Arrays.asList(param);
        }
        // 如果是自定义类型 通过返回值类型 实例化此类,给实例赋值 然后将实例set给引用的对象
        if(!isJavaClass(param.getClass())){
            // System.out.println(param.getClass().getSimpleName());
            return param;
        }
        // 基本数据类型
        if (param instanceof String && String.class.equals(paramClass)) {
            return (String)param;
        }

        if (param instanceof Byte && Byte.TYPE.getClass().equals(paramClass)) {
            return (Byte)param;
        }
        if (param instanceof Short && Short.TYPE.getClass().equals(paramClass)) {
            return (Short)param;
        }

        if (param instanceof Integer && Integer.TYPE.getClass().equals(paramClass)) {
            return (Integer)param;
        }
        if (param instanceof Long && Long.TYPE.getClass().equals(paramClass)) {
            return (Long)param;
        }
        if (param instanceof Float && Float.TYPE.getClass().equals(paramClass)) {
            return (Float)param;
        }
        if (param instanceof Double && Double.TYPE.getClass().equals(paramClass)) {
            return (Double)param;
        }
        if (param instanceof Boolean && Boolean.TYPE.getClass().equals(paramClass)) {
            return (Boolean)param;
        }
        return param;
    }

    public static boolean isJavaClass(Class<?> clz) {
        return clz != null && clz.getClassLoader() == null;
    }
}
