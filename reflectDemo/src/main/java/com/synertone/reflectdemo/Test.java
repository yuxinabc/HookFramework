package com.synertone.reflectdemo;

import com.synertone.reflectdemo.model.Student;
import com.synertone.reflectdemo.utils.ReflectUtils;

public class Test {
    public static void main(String[] args){
        Student student=new Student();
        Class<? extends Student> aClass = student.getClass();
        Student studenta=new Student("dddd",18);
        Class<? extends Student> aClassa = studenta.getClass();
        Class aClass1 = ReflectUtils.getModelClass(1);
        Class aClass2 = ReflectUtils.getModelClass(2);
        Class aClass3 = ReflectUtils.getModelClass(3);
        System.out.println(aClass==aClassa);
        System.out.println(aClass==aClass1);
        System.out.println(aClass1==aClass2);
        System.out.println(aClass2==aClass3);


    }
}
