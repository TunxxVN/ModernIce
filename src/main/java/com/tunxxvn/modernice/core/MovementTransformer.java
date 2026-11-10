package com.tunxxvn.modernice.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class MovementTransformer implements IClassTransformer {
    private static final String LIVING = "net/minecraft/entity/EntityLivingBase";
    private static final String HOOKS = "com/tunxxvn/modernice/physics/MovementHooks";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !"net.minecraft.entity.EntityLivingBase".equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        int frictionReads = 0;
        int movements = 0;
        for (Object entry : node.methods) {
            MethodNode method = (MethodNode) entry;
            if (!method.desc.equals("(FF)V") || !(method.name.equals("moveEntityWithHeading")
                    || method.name.equals("func_70612_e"))) continue;
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
                AbstractInsnNode next = instruction.getNext();
                if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode field = (FieldInsnNode) instruction;
                    if (field.getOpcode() == Opcodes.GETFIELD && field.owner.equals("net/minecraft/block/Block")
                            && field.desc.equals("F") && (field.name.equals("slipperiness")
                            || field.name.equals("field_149765_K"))) {
                        InsnList replacement = new InsnList();
                        replacement.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "slipperiness",
                                "(Lnet/minecraft/block/Block;L" + LIVING + ";)F", false));
                        method.instructions.insertBefore(field, replacement);
                        method.instructions.remove(field);
                        frictionReads++;
                    }
                } else if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (call.getOpcode() == Opcodes.INVOKEVIRTUAL && call.owner.equals(LIVING)
                            && call.desc.equals("(DDD)V") && (call.name.equals("moveEntity")
                            || call.name.equals("func_70091_d"))) {
                        method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                                "moveEntity", "(L" + LIVING + ";DDD)V", false));
                        movements++;
                    }
                }
                instruction = next;
            }
        }
        if (frictionReads != 2 || movements != 3) {
            throw new IllegalStateException("Modern Ice: incompatible movement bytecode (friction="
                    + frictionReads + ", movement=" + movements + "; expected 2 and 3)");
        }
        // Preserve stack-map frames; the patch does not change control flow.
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
