package com.synertone.hookframework.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import com.synertone.hookframework.LoginActivity;
import com.synertone.hookframework.ProxyActivity;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HookUtil {
    private Context context;
    private static HookUtil mHookUtil;
    private HookUtil(){

    }
    private HookUtil(Context context) {
        this.context=context;
    }

    public static  HookUtil getInstance(Context context){
        if(mHookUtil==null){
            mHookUtil=new HookUtil(context);
        }
        return mHookUtil;
    }
    public void hookStartActivity(){
        try {
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            Field mSingletonField = activityManagerNativeClass.getDeclaredField("gDefault");
            mSingletonField.setAccessible(true);
            //因为gDefault是静态的，所以可以传null
            Object mSingleton = mSingletonField.get(null);
            //获取Class尽量用Class.forName，不要用getClass()
            Class<?> mSingletonClass =Class.forName("android.util.Singleton"); //mSingleton.getClass();
            Field mIActivityManagerField = mSingletonClass.getDeclaredField("mInstance");
            mIActivityManagerField.setAccessible(true);
            //非静态Field属性，要想获得对象，必须传类（包含了该属性）的对象
            Object mIActivityManager = mIActivityManagerField.get(mSingleton);
            //获取Class尽量用Class.forName，不要用getClass()
            Class<?> IActivityManagerIntercept = Class.forName("android.app.IActivityManager");
            //动态代理
            MyInvocationHandler myInvocationHandler=new MyInvocationHandler(mIActivityManager);
            Object proxyIActivityManager = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{IActivityManagerIntercept}, myInvocationHandler);
            //将系统的IActivityManager替换成自己代理的proxyIActivityManager
            //非静态属性设置对象值，需要类（包含了该属性）的对象和该属性的对象
            mIActivityManagerField.set(mSingleton,proxyIActivityManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void hookActivityThreadmH(){
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Field sCurrentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            //还原系统ActivityThread
            Object mActivityThread = sCurrentActivityThreadField.get(null);
            Field mHField = activityThreadClass.getDeclaredField("mH");
            mHField.setAccessible(true);
            Handler mHandler = (Handler) mHField.get(mActivityThread);
            Class<?> handlerClass = Class.forName("android.os.Handler");
            Field mCallbackField = handlerClass.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            mCallbackField.set(mHandler,new MyCallback(mHandler));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    class MyInvocationHandler implements InvocationHandler{
        private Object mIActivityManager;
        public MyInvocationHandler(Object mIActivityManager) {
            this.mIActivityManager=mIActivityManager;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if("startActivity".equals(method.getName())) {
                System.out.println("--------------startActivity invoke--------------");
                Intent targetIntent;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        targetIntent = (Intent) args[i];
                        //Intent proxyIntent=new Intent(mContext, ProxyActivity.class);
                        Intent proxyIntent=new Intent();
                        ComponentName componentName=new ComponentName(context,ProxyActivity.class);
                        proxyIntent.setComponent(componentName);
                        proxyIntent.putExtra("targetIntent",targetIntent);
                        args[i]=proxyIntent;
                        break;
                    }
                }

            }
            return method.invoke(mIActivityManager,args);
        }
    }
    class MyCallback implements Handler.Callback{

        private static final int LAUNCH_ACTIVITY = 100;
        private Handler systemHandler;
        public MyCallback(Handler mHandler) {
            systemHandler=mHandler;
        }

        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;
            //即将要加载acitivity
            if(what==LAUNCH_ACTIVITY){
                System.out.println("------------handleMessage-------------");
                Object obj = msg.obj;
                Class<?> aClass = obj.getClass();
                try {
                    Field intentField = aClass.getDeclaredField("intent");
                    intentField.setAccessible(true);
                    Intent proxyIntent = (Intent) intentField.get(obj);
                    Intent targetIntent = proxyIntent.getParcelableExtra("targetIntent");
                    if(targetIntent!=null){
                        SharedPreferences share = context.getSharedPreferences("david",
                                Context.MODE_PRIVATE);
                        if (share.getBoolean("login",false)) {

//                      登录  还原  把原有的意图    放到proxyIntent
                            proxyIntent.setComponent(targetIntent.getComponent());
                        }else {
                            ComponentName componentName = new ComponentName(context,LoginActivity.class);
                            proxyIntent.putExtra("extraIntent", targetIntent.getComponent()
                                    .getClassName());
                            proxyIntent.setComponent(componentName);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            systemHandler.handleMessage(msg);
            return true;
        }
    }
}
