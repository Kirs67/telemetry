/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirs.telemetry.modules;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Kirs
 */
public class TelemetryModule {
    public File moduleJar;
    public String ID;
    public String name;
    public String version;
    public String moduleclass;
    public String[] dependences;
    public String[] providers;
    public String[] requireBefore;
    public String[] requireAfter;
    
    public TelemetryModule(File moduleJar) throws IOException {
        FileInputStream fis = new FileInputStream(moduleJar);
        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;
        while((entry = zin.getNextEntry()) != null) {
           if(entry.getName().equals("module.info")){
               IdentifyModule(moduleJar, entry);
           }
        }
    }
    
    public TelemetryModule(File moduleJar, ZipEntry moduleInfo) throws IOException {
        IdentifyModule(moduleJar, moduleInfo);
    }
    
    private void IdentifyModule(File moduleJar, ZipEntry moduleInfo) throws IOException {
        this.moduleJar = moduleJar;
        ZipFile moduleZip = new ZipFile(moduleJar);
        InputStream inputStream = moduleZip.getInputStream(moduleInfo);
        //@TODO This requires java 8, rewrite
        String result;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            result = br.lines().collect(Collectors.joining("\n"));
        }
        String[] moduleInfoSrings = result.split("\n");
        for(String currentString:moduleInfoSrings) {
            String[] currentStringParts = currentString.split(":", 2);
            switch(currentStringParts[0].toLowerCase()){
                case "id":
                    this.ID = currentStringParts[1];
                    break;
                case "name":
                    this.name = currentStringParts[1];
                    break;
                case "version":
                    this.version = currentStringParts[1];
                    break;
                case "moduleclass":
                    this.moduleclass = currentStringParts[1];
                    break;
                case "dependences":
                    this.dependences = currentStringParts[1].split(";");
                    break;
                case "providers":
                    this.providers = currentStringParts[1].split(";");
                    break;
                case "before":
                    this.requireBefore = currentStringParts[1].split(";");
                    break;
                case "after":
                    this.requireAfter = currentStringParts[1].split(";");
                    break;
            }
        }

    }
    
    @Override
    public String toString(){
        String result = "";
        result+= (ID+":"+version);
        return result;
    }
}
