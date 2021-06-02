package com.abhiram.javassist.util;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassV extends ClassVisitor {

    public ClassV() {
        super(Opcodes.ASM7);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
       return null;
    }
}
