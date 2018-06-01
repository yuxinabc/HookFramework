package com.synertone.reflectdemo.utils;

import com.synertone.reflectdemo.model.Student;

public class ReflectUtils {
    public static  Class getModelClass(int type){
        Class clazz=null;
        switch (type){
            case 1:
                Student student=new Student();
                clazz=student.getClass();
                break;
            case 2:
                clazz=Student.class;
                break;
            case 3:
                try {
                    clazz = Class.forName("com.synertone.reflectdemo.model.Student");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }

        return clazz;
    }
}
