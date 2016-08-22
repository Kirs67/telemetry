/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirs.telemetry.core.events;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JOptionPane;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

/**
 *
 * @author Kirs
 */
public class TelemetryEventBus {
    public ArrayList<Method> methodsToInvoke = new ArrayList<>();
    public String name;

    public TelemetryEventBus(String name) {
        this.name = name;
    }
    
    public void addMethodToInvoke(Method method) {
        JOptionPane.showMessageDialog(null, "Method "+method.toString());
        methodsToInvoke.add(method);
    }
    
    public void doEvent() {
        try{
            for(Method methodToInvoke:methodsToInvoke){
                Object[] args = new Object[0];
                methodToInvoke.invoke(null,args);
            }
        }
        catch(Exception ex){
            System.err.println(ex);
            System.err.println(Arrays.toString(ex.getStackTrace()));
            System.err.println(ex.getCause());
            JOptionPane.showMessageDialog(null, ex);
            JOptionPane.showMessageDialog(null, ex.getStackTrace());
            JOptionPane.showMessageDialog(null, ex.getCause());
        }
    }
}
