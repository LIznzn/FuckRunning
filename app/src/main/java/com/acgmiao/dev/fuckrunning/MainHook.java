package com.acgmiao.dev.fuckrunning;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.SparseArray;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    private static float count = 0;
    private static final int max = 100000;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //测试是否成功生效
        if (loadPackageParam.packageName.equals("com.acgmiao.dev.fuckrunning")) {
            XposedBridge.log("找到测试应用");
            Class clazz = loadPackageParam.classLoader.loadClass("com.acgmiao.dev.fuckrunning.activity.MainActivity");
            XposedHelpers.findAndHookMethod(clazz, "toastMessage", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    param.setResult("你已被劫持");
                }
            });
        }
        //开始拦截目标应用
        if (loadPackageParam.packageName.equals("com.zjwh.android_wh_physicalfitness")) {
            XposedBridge.log("找到目标应用");
            XposedHelpers.findAndHookMethod("com.stub.StubApp", loadPackageParam.classLoader, "getNewAppInstance", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    XposedBridge.log("找到防反编译壳");
                    Context context = (Context) param.args[0];
                    ClassLoader classLoader = context.getClassLoader();
                    XposedHelpers.findAndHookMethod("com.zjwh.android_wh_physicalfitness.emulator.c", classLoader, "a", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XposedBridge.log("成功劫持检测方法a()");
                            param.setResult(false);
                        }
                    });
                    XposedHelpers.findAndHookMethod("com.zjwh.android_wh_physicalfitness.emulator.c", classLoader, "b", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XposedBridge.log("成功劫持检测方法b()");
                            param.setResult(false);
                        }
                    });
                    XposedHelpers.findAndHookMethod("com.zjwh.android_wh_physicalfitness.emulator.c", classLoader, "c", Context.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XposedBridge.log("成功劫持检测方法c(Context)");
                            param.setResult(false);
                        }
                    });
                    //劫持传感器
                    final Class<?> sensorEL = XposedHelpers.findClass("android.hardware.SystemSensorManager$SensorEventQueue", classLoader);
                    XposedBridge.hookAllMethods(sensorEL, "dispatchSensorEvent", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            int handle = (Integer) param.args[0];
                            Field field = param.thisObject.getClass().getDeclaredField("mSensorsEvents");
                            field.setAccessible(true);
                            Sensor ss = ((SparseArray<SensorEvent>) field.get(param.thisObject)).get(handle).sensor;
                            if (ss == null) {
                                XposedBridge.log("未劫持到传感器");
                                return;
                            }
                            if (ss.getType() == Sensor.TYPE_ACCELEROMETER) {
                                count += 1;
                                //步频100算法
                                if (count % 3 == 0) {
                                    ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] * 100;
                                    ((float[]) param.args[1])[1] += (float) -10;
                                } else if (count % 2 == 0) {
                                    ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] * 1000;
                                    ((float[]) param.args[1])[2] += (float) -20;
                                    ((float[]) param.args[1])[1] += (float) -5;
                                } else {
                                    ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] * 10;
                                    ((float[]) param.args[1])[2] += (float) 20;
                                    ((float[]) param.args[1])[1] += (float) -15;
                                }
                                XposedBridge.log("传感器类型：" + ss.getType() + "加速度传感器，数据" + ((float[]) param.args[1])[0] + "," + ((float[]) param.args[1])[1] + "," + ((float[]) param.args[1])[2]);
                            }
                            if (ss.getType() == Sensor.TYPE_STEP_COUNTER || ss.getType() == Sensor.TYPE_STEP_DETECTOR) {
                                if (10000 * count <= max) {
                                    ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] + 10000 * count;
                                    count += 1;
                                } else {
                                    count = 0;
                                }
                                XposedBridge.log("传感器类型：" + ss.getType() + "计步器，数据" + ((float[]) param.args[1])[0]);
                            }
                        }
                    });
                }
            });
        }
    }
}