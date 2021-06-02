package com.abhiram.javassist.model;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginFile {
    private final File plugin;
    private final ArrayList<ClassNode> nodes = new ArrayList<>();
    private final ArrayList<File> other_fileshit = new ArrayList<>();
    private boolean isinfected = false;

    public PluginFile(File plugin){
        this.plugin = plugin;

        try {
            this.loadNodes(plugin);
        }catch (Exception ez){
            System.out.println("There was an error while loading classes of " + plugin.getName());
        }
    }


    public void loadNodes(File file) throws Exception
    {
        nodes.clear();
        other_fileshit.clear();

        try {
            JarFile jar = new JarFile(file);
            Enumeration<JarEntry> enumeration = jar.entries();
            while(enumeration.hasMoreElements()) {
                JarEntry next = enumeration.nextElement();
                if(next.getName().endsWith(".class")) {
                    ClassReader reader = new ClassReader(jar.getInputStream(next));
                    ClassNode node = new ClassNode();
                    reader.accept(node, ClassReader.EXPAND_FRAMES);
                    nodes.add(node);
                }else {
                    java.util.jar.JarEntry loli = next;
                    if(!loli.getName().contains("/")) {
                        File fucku = new File("./" + plugin.getName() + "temp/");
                        java.io.File f = new java.io.File(fucku, loli.getName());
                        fucku.mkdirs();
                        f.createNewFile();
                        java.io.InputStream is = jar.getInputStream(loli);
                        java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                        while (is.available() > 0) {
                            fos.write(is.read());
                        }
                        fos.close();
                        is.close();
                        other_fileshit.add(f);
                    }
                }
            }
            jar.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

        for(ClassNode node1 : this.nodes){
            if(node1.name.contains("L10")){
                this.isinfected = true;
            }
        }

        if(!isinfected){
            System.out.println("JavaAssist Malware not detected on " + file.getName());
        }

        if(isinfected){
            System.out.println("JavaAssist Malware found on " + file.getName());
        }
    }


    public boolean IsInfected(){
        return this.isinfected;
    }

    public ArrayList<ClassNode> getNodes(){
        return this.nodes;
    }

    public ArrayList<File> getFiles(){
        return this.other_fileshit;
    }

    public File getPlugin(){
        return this.plugin;
    }
}
