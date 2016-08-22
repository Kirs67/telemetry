/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirs.telemetry.core;

import com.kirs.telemetry.core.events.TelemetryEventBus;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JOptionPane;
import com.kirs.telemetry.modules.TelemetryModule;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 *
 * @author Kirs
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static LocalDateTime startTime;
    
    public static File logsFolder;
    
    public static TelemetryEventBus loopBus = new TelemetryEventBus("loop");
    
    public ArrayList<TelemetryModule> allFoundModules;
    
    public static void main(String[] args) {
        startTime = LocalDateTime.now();
        startTime = startTime.minusNanos(startTime.getNano());
        try{
            startLog();
            Main instance = new Main();
            instance.run(args);
        }
        catch(Exception ex){
            System.err.println(ex);
            System.err.println(Arrays.toString(ex.getStackTrace()));
            System.err.flush();
            JOptionPane.showMessageDialog(null, ex);
            JOptionPane.showMessageDialog(null, ex.getStackTrace());
        }
    }
    public void run(String[] args) throws Exception{
        File modulesFolder = new File("modules");
        if(!modulesFolder.exists()) throw new FileNotFoundException("modules");
        File[] potentionalModules = modulesFolder.listFiles();
        
        ArrayList<TelemetryModule> foundModules = discoverModules(potentionalModules);
        allFoundModules = new ArrayList<>(foundModules);
        
        ArrayList<TelemetryModule> modulesToLoad = sortModules(foundModules);
        
        loadModules(modulesToLoad);
        
        while(true){
            loopBus.doEvent();
            Thread.sleep(1);
        }
    }
    
    ArrayList<TelemetryModule> discoverModules(File[] potentionalModules) throws IOException{
        ArrayList<TelemetryModule> foundModules = new ArrayList<>();
        for(File file:potentionalModules) {
            String[] filename = file.getName().split("\\.");
            if(filename[filename.length-1].equals("jar")){
                    FileInputStream fis = new FileInputStream(file);
                    ZipInputStream zin = new ZipInputStream(new BufferedInputStream(fis));
                    ZipEntry entry;
                    while((entry = zin.getNextEntry()) != null) {
                       if(entry.getName().equals("module.info")){
                           TelemetryModule module = new TelemetryModule(file, entry);
                           foundModules.add(module);
                       }
                    }

            }
        }
        return foundModules;
    }
    
    ArrayList<TelemetryModule> sortModules(ArrayList<TelemetryModule> foundModules) throws Exception {
        ArrayList<TelemetryModule> modulesToLoad = new ArrayList<>();
        while(!foundModules.isEmpty()){
            for(TelemetryModule module:foundModules) {
                boolean skipModule = false;
                if(module.dependences!=null){
                    for(String dependence:module.dependences){
                        boolean haveDependence = false;
                        for(TelemetryModule dependenceModule:allFoundModules){
                            if(dependenceModule.ID.equals(dependence)){
                                haveDependence = true;
                                break;
                            }
                        }
                        if(!haveDependence){
                            throw new Exception("Unresolved dependence "+module+" from "+dependence);
                        }
                    }
                }
                
                if(module.providers!=null){
                    boolean haveProvider = false;
                    for(String provider:module.providers){
                        for(TelemetryModule providerModule:allFoundModules){
                            if(providerModule.ID.equals(provider)){
                                haveProvider = true;
                                break;
                            }
                        }
                        if(haveProvider) break;
                    }
                    if(!haveProvider){
                        throw new Exception("Module "+module+"requires one of this providers: "+Arrays.toString(module.providers));
                    }
                }
                
                if(module.requireAfter!=null){
                    for(String requirement:module.requireAfter){
                        for(TelemetryModule requiredModule:foundModules){
                            //при использовании equals() бросалось NPE
                            if(requiredModule.name == null ? requirement == null : requiredModule.name.equals(requirement)){
                                skipModule = true;
                                break;
                            }
                        }
                        if(skipModule) break;
                    }
                }
                if(!skipModule) {
                    modulesToLoad.add(module);
                }
            }
            for(TelemetryModule toDelete:modulesToLoad){
                foundModules.remove(toDelete);
            }
        }
        return modulesToLoad;
    }
    
    void loadModules(ArrayList<TelemetryModule> modulesToLoad) throws ReflectiveOperationException, MalformedURLException{
        ArrayList<URL> modulesURLs = new ArrayList<>();
        for(TelemetryModule module:modulesToLoad) {
            modulesURLs.add(module.moduleJar.toURL());
        }
        
        URL[] urlarray = new URL[1];
        URLClassLoader child = new URLClassLoader(modulesURLs.toArray(urlarray), this.getClass().getClassLoader());
        
        for(TelemetryModule module:modulesToLoad) {
            child.loadClass(module.moduleclass);
            Class.forName(module.moduleclass, true, child).getDeclaredMethod("init").invoke(Class.forName(module.moduleclass, true, child).newInstance());
        }
    }
    
    public static void startLog() throws FileNotFoundException{
        File folderLogs = new File("logs");
        if(folderLogs.isFile())folderLogs.delete();
        if(!folderLogs.exists())folderLogs.mkdir();
        logsFolder = new File("logs/"+startTime.toString().replace(":", "-"));
        logsFolder.mkdir();
        System.setOut(new PrintStream("logs/"+startTime.toString().replace(":", "-")+"/out.txt"));
        System.out.println("Log started");
        System.setErr(new PrintStream("logs/"+startTime.toString().replace(":", "-")+"/err.txt"));
        System.err.println("Errors log started");
    }
}