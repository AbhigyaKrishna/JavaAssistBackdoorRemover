package com.abhiram.javassist;

import com.abhiram.javassist.model.PluginFile;
import com.abhiram.javassist.util.ClassV;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.apache.commons.lang3.RandomStringUtils;

import static org.objectweb.asm.Opcodes.*;


public class AntiBackdoor {
    private static AntiBackdoor instance;
    private final ArrayList<PluginFile> plugins = new ArrayList<>();

    public void init()  {
        File folder = new File("plugins");
        if(!folder.exists()){
            System.out.println("Plugins folder not found exiting....");
            System.exit(-1);
        }

        if(folder.listFiles().length == 0){
            System.out.println("No Plugins found on plugins folder");
            return;
        }

        for(File pl : folder.listFiles()){
            if(!pl.getName().endsWith(".jar")){
                System.out.println(pl.getName() + " is not an jar file...");
                return;
            }

            plugins.add(new PluginFile(pl));
        }

        this.check();
    }

    public void check(){
        System.out.println("Starting Backdoor removal Process...");
        for(PluginFile plugin : this.plugins){
            if(plugin.IsInfected()){
                System.out.println("Removing Backdoor on " + plugin.getPlugin().getName());
                for(ClassNode node : plugin.getNodes()){
                    if(node.name.contains("L10")) {
                        System.out.println("Removed backdoor from " + node.name);
                        node.fields.clear();
                        node.methods.clear();

                        MethodVisitor lol = node.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
                        lol.visitCode();
                        lol.visitVarInsn(ALOAD, 0);
                        lol.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                        lol.visitInsn(RETURN);
                        lol.visitMaxs(1,1);
                        lol.visitEnd();

                        MethodVisitor mv = node.visitMethod(ACC_PUBLIC,"a","(Ljava/lang/String;)V",null,null);
                        mv.visitCode();
                        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                        mv.visitLdcInsn("Hahahah nice try to backdoor");
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                        mv.visitInsn(Opcodes.RETURN);
                        mv.visitMaxs(0, 0);
                        mv.visitEnd();
                    }
                }
                this.save(new File("result/" + plugin.getPlugin().getName() + "-removed.jar"),plugin.getNodes(),plugin);
            }
        }
    }

    public void save(File jar, ArrayList<ClassNode> nodes,PluginFile plugin) {
        try {
            if(!jar.exists()){
                jar.createNewFile();
            }
            JarOutputStream output = new JarOutputStream(new FileOutputStream(jar));

            for(ClassNode element : nodes) {
                if(!element.name.contains("javassist")) {
                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    element.accept(writer);
                    output.putNextEntry(new JarEntry(element.name.replaceAll("\\.", "/") + ".class"));
                    output.write(writer.toByteArray());
                }
            }

            for(File entry : plugin.getFiles()){
                if(!entry.getName().contains(plugin.getPlugin().getName())) {
                    output.putNextEntry(new JarEntry(entry.getName()));
                    FileInputStream fis = new FileInputStream(entry);
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                }
            }
            output.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static AntiBackdoor getInstance(){
        if(instance == null){
            instance = new AntiBackdoor();
        }

        return instance;
    }
}
