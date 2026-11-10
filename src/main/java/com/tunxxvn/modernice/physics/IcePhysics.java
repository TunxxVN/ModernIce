package com.tunxxvn.modernice.physics;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public final class IcePhysics {

    public static final IcePhysics INSTANCE = new IcePhysics();

    private static final float ICE_SLIPPERINESS_THRESHOLD = 0.98F;

    private WeakReference<Entity> supportPlayer = new WeakReference<Entity>(null);
    private WeakReference<World> supportWorld = new WeakReference<World>(null);
    private AxisAlignedBB supportBox;
    private Support support = new Support();

    private IcePhysics() {}

    public float movementSlipperiness(Entity player, Block vanillaBlock, boolean custom, float configured) {
        return movementSlipperiness(player, player.worldObj, vanillaBlock, custom, configured);
    }

    public float movementSlipperiness(Entity player, World world, Block vanillaBlock, boolean custom, float configured) {
        if (player == null || world == null || !player.onGround) return vanillaBlock.slipperiness;
        float selected = slipperiness(player, world, player.getEntityBoundingBox(), supportFor(player, world),
                vanillaBlock.slipperiness, custom, configured);
        return Float.isNaN(selected) ? vanillaBlock.slipperiness : selected;
    }

    public static float slipperiness(Entity player, World world, AxisAlignedBB feet, Support support,
                                     float vanilla, boolean custom, float configured) {
        if (!support.initialized) support.update(player, world, feet, true, 0.0D, 0.0D, false);
        if (support.block != null && !world.isBlockLoaded(support.block)) return Float.NaN;
        BlockPos pos = player == null ? movementFrictionPos(world, feet, support.block)
                : movementFrictionPos(world, player.posX, player.posY, player.posZ, support.block);
        if (!world.isBlockLoaded(pos)) return Float.NaN;
        float modern = world.getBlockState(pos).getBlock().slipperiness;
        float selected = vanilla >= ICE_SLIPPERINESS_THRESHOLD || modern >= ICE_SLIPPERINESS_THRESHOLD
                ? modern : vanilla;
        return custom && selected >= ICE_SLIPPERINESS_THRESHOLD ? configured : selected;
    }

    private Support supportFor(Entity player, World world) {
        if (supportPlayer.get() != player || supportWorld.get() != world || supportBox != player.getEntityBoundingBox()) {
            supportPlayer = new WeakReference<Entity>(player);
            supportWorld = new WeakReference<World>(world);
            supportBox = player.getEntityBoundingBox();
            support = new Support();
            support.update(player, world, supportBox, player.onGround, 0.0D, 0.0D, false);
        }
        return support;
    }

    public void afterMovement(Entity player, double moveX, double moveZ) {
        if (supportPlayer.get() != player || supportWorld.get() != player.worldObj) {
            supportPlayer = new WeakReference<Entity>(player);
            supportWorld = new WeakReference<World>(player.worldObj);
            support = new Support();
        }
        supportBox = player.getEntityBoundingBox();
        support.update(player, player.worldObj, supportBox, player.onGround, moveX, moveZ, true);
    }

    public static final class Support {
        private BlockPos block;
        private boolean noBlocks;
        private boolean initialized;

        public void update(Entity player, World world, AxisAlignedBB feet, boolean onGround,
                           double moveX, double moveZ, boolean hasMovement) {
            initialized = true;
            if (!onGround) {
                block = null;
                noBlocks = false;
                return;
            }
            double refX = player == null ? (feet.minX + feet.maxX) * 0.5D : player.posX;
            double refY = player == null ? feet.minY : player.posY;
            double refZ = player == null ? (feet.minZ + feet.maxZ) * 0.5D : player.posZ;
            BlockPos found = findSupportingBlockInArea(player, world,
                    feet.minX, feet.minZ, feet.maxX, feet.maxZ, feet.minY,
                    refX, refY, refZ);
            if (found != null || noBlocks) {
                block = found;
            } else if (hasMovement) {
                found = findSupportingBlockInArea(player, world,
                        feet.minX - moveX, feet.minZ - moveZ, feet.maxX - moveX, feet.maxZ - moveZ, feet.minY,
                        refX, refY, refZ);
                block = found;
            }
            noBlocks = found == null;
        }
    }

    public static BlockPos movementFrictionPos(World world, AxisAlignedBB feet, BlockPos supporting) {
        return movementFrictionPos(world, (feet.minX + feet.maxX) * 0.5D, feet.minY,
                (feet.minZ + feet.maxZ) * 0.5D, supporting);
    }

    private static BlockPos movementFrictionPos(World world, double x, double y, double z, BlockPos supporting) {
        // Round as float before promoting to double; a double literal changes boundary results.
        int belowY = MathHelper.floor_double(y - (double) 0.500001F);
        if (supporting == null) {
            return new BlockPos(MathHelper.floor_double(x), belowY, MathHelper.floor_double(z));
        }
        if (supporting.getY() == belowY) return supporting;
        Block block = world.getBlockState(supporting).getBlock();
        if (block instanceof BlockWall || block instanceof BlockFenceGate) return supporting;
        return new BlockPos(supporting.getX(), belowY, supporting.getZ());
    }

    public static BlockPos findSupportingBlockInArea(Entity player, World world,
                                                     double minX, double minZ, double maxX, double maxZ,
                                                     double feetMinY,
                                                     double refX, double refY, double refZ) {
        AxisAlignedBB testArea = new AxisAlignedBB(
                minX, feetMinY - 1.0E-6D, minZ,
                maxX, feetMinY, maxZ);

        int bMinX = MathHelper.floor_double(testArea.minX - 1.0E-7D) - 1;
        int bMaxX = MathHelper.floor_double(testArea.maxX + 1.0E-7D) + 1;
        int bMinY = MathHelper.floor_double(testArea.minY - 1.0E-7D) - 1;
        int bMaxY = MathHelper.floor_double(testArea.maxY + 1.0E-7D) + 1;
        int bMinZ = MathHelper.floor_double(testArea.minZ - 1.0E-7D) - 1;
        int bMaxZ = MathHelper.floor_double(testArea.maxZ + 1.0E-7D) + 1;

        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>(1);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = bMinY; y <= bMaxY; y++) {
            for (int z = bMinZ; z <= bMaxZ; z++) {
                for (int x = bMinX; x <= bMaxX; x++) {
                    pos.set(x, y, z);
                    if (!world.isBlockLoaded(pos)) continue;
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    float oldMinX = (float) block.getBlockBoundsMinX();
                    float oldMinY = (float) block.getBlockBoundsMinY();
                    float oldMinZ = (float) block.getBlockBoundsMinZ();
                    float oldMaxX = (float) block.getBlockBoundsMaxX();
                    float oldMaxY = (float) block.getBlockBoundsMaxY();
                    float oldMaxZ = (float) block.getBlockBoundsMaxZ();
                    boxes.clear();
                    try {
                        block.addCollisionBoxesToList(world, pos, state, testArea, boxes, player);
                    } finally {
                        // Collision queries can change the shared block's bounds.
                        block.setBlockBounds(oldMinX, oldMinY, oldMinZ, oldMaxX, oldMaxY, oldMaxZ);
                    }
                    for (int i = 0; i < boxes.size(); i++) {
                        AxisAlignedBB collision = boxes.get(i);
                        // Full cubes accept any positive overlap; other shapes use the 1e-7 tolerance.
                        boolean fullCube = collision.minX == x && collision.maxX == x + 1.0D
                                && collision.minY == y && collision.maxY == y + 1.0D
                                && collision.minZ == z && collision.maxZ == z + 1.0D;
                        double epsilon = fullCube ? 0.0D : 1.0E-7D;
                        if (collision.maxX <= testArea.minX + epsilon || collision.minX >= testArea.maxX - epsilon
                                || collision.maxY <= testArea.minY + epsilon || collision.minY >= testArea.maxY - epsilon
                                || collision.maxZ <= testArea.minZ + epsilon || collision.minZ >= testArea.maxZ - epsilon) continue;

                        double distance = distanceSqToCentre(pos, refX, refY, refZ);
                        if (distance < bestDistance
                                || (distance == bestDistance && (best == null || best.compareTo(pos) < 0))) {
                            best = pos.getImmutable();
                            bestDistance = distance;
                        }
                        break;
                    }
                }
            }
        }

        return best;
    }

    private static double distanceSqToCentre(BlockPos pos, double x, double y, double z) {
        double dx = (double) pos.getX() + 0.5D - x;
        double dy = (double) pos.getY() + 0.5D - y;
        double dz = (double) pos.getZ() + 0.5D - z;
        return dx * dx + dy * dy + dz * dz;
    }
}
