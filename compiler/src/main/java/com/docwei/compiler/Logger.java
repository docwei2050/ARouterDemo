package com.docwei.compiler;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

public class Logger {
    public Messager mMessager;

    public Logger(Messager messager) {
        mMessager = messager;
    }

    public void  d(String message){
        mMessager.printMessage(Diagnostic.Kind.NOTE,message);
    }
    public void  e(String message){
        mMessager.printMessage(Diagnostic.Kind.ERROR,message);
    }
    public void  w(String message){
        mMessager.printMessage(Diagnostic.Kind.WARNING,message);
    }

}
