package com.tunxxvn.modernice.physics;

import com.tunxxvn.modernice.ModernIce;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;

public final class MovementHooks {
    private MovementHooks() { }

    private static boolean isLocalGroundMovement(EntityLivingBase entity) {
        Minecraft mc = Minecraft.getMinecraft();
        return entity == mc.thePlayer && entity.worldObj != null && entity.worldObj.isRemote
                && !entity.noClip && !entity.isRiding() && !mc.thePlayer.capabilities.isFlying;
    }

    public static float slipperiness(Block block, EntityLivingBase entity) {
        return ModernIce.config.enabled() && isLocalGroundMovement(entity)
                ? IcePhysics.INSTANCE.movementSlipperiness(entity, block, ModernIce.config.customSlipperiness(),
                        ModernIce.config.slipperiness())
                : block.slipperiness;
    }

    public static void moveEntity(EntityLivingBase entity, double x, double y, double z) {
        boolean sample = ModernIce.config.enabled() && isLocalGroundMovement(entity);
        double beforeX = entity.posX;
        double beforeZ = entity.posZ;
        entity.moveEntity(x, y, z);
        if (sample) IcePhysics.INSTANCE.afterMovement(entity, entity.posX - beforeX, entity.posZ - beforeZ);
    }
}
