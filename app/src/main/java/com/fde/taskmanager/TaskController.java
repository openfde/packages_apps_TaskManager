package com.fde.taskmanager;

import android.openfde.AppTaskControllerProxy;

public class TaskController {

    public TaskController(){
        AppTaskControllerProxy proxy = AppTaskControllerProxy.create();
    }
}
