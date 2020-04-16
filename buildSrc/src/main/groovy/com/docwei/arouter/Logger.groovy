package com.docwei.arouter

import org.gradle.api.Project;

class Logger {
    static org.gradle.api.logging.Logger mLogger;
    //必须要初始化日志
    static void make(Project project){
        mLogger=project.getLogger();
    }
    static void e(String message){
        mLogger.error(message)
    }
    static void d(String message){
        mLogger.debug(message)
    }
    static void i(String message){
        mLogger.info(message)
    }
}
