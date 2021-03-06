package com.hollingsworth.arsnouveau.common.spell.effect;

import com.hollingsworth.arsnouveau.ModConfig;
import com.hollingsworth.arsnouveau.api.spell.AbstractAugment;
import com.hollingsworth.arsnouveau.api.spell.AbstractEffect;
import com.hollingsworth.arsnouveau.common.entity.EntityEvokerFangs;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAccelerate;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentExtendTime;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class EffectFangs extends AbstractEffect {

    public EffectFangs() {
        super(ModConfig.EffectFangsID, "Fangs");
    }

    @Override
    public void onResolve(RayTraceResult rayTraceResult, World world, LivingEntity shooter, ArrayList<AbstractAugment> augments) {
        Vec3d vec = rayTraceResult.getHitVec();
        float bonusDamage = 2.5f * getAmplificationBonus(augments);
        double targetX = vec.x;
        double targetY = vec.y;
        double targetZ = vec.z;

        double d0 = Math.min(targetY, shooter.posY);
        double d1 = Math.max(targetY, shooter.posY) + 1.0D;
        float f = (float)MathHelper.atan2(targetZ - shooter.posZ, targetX - shooter.posX);

        for(int l = 0; l < 16; ++l) {
            double d2 = 1.25D * (double)(l + 1);
            int j =  ( l + getBuffCount(augments, AugmentExtendTime.class)) / (1 + getBuffCount(augments, AugmentAccelerate.class));
            this.spawnFangs(world, shooter.posX + (double)MathHelper.cos(f) * d2, shooter.posZ + (double)MathHelper.sin(f) * d2, d0, d1, f, j, shooter, bonusDamage);
        }
    }

    @Override
    public boolean dampenIsAllowed() {
        return true;
    }

    private void spawnFangs(World world, double xAngle, double zAngle, double yStart, double yEnd, float rotationYaw, int tickDelay, LivingEntity caster, float bonusDamage) {
        BlockPos blockpos = new BlockPos(xAngle, yEnd, zAngle);
        boolean flag = false;
        double d0 = 0.0D;

        while(true) {
            BlockPos blockpos1 = blockpos.down();
            BlockState blockstate = world.getBlockState(blockpos1);
            if (blockstate.func_224755_d(world, blockpos1, Direction.UP)) {
                if (!world.isAirBlock(blockpos)) {
                    BlockState blockstate1 = world.getBlockState(blockpos);
                    VoxelShape voxelshape = blockstate1.getCollisionShape(world, blockpos);
                    if (!voxelshape.isEmpty()) {
                        d0 = voxelshape.getEnd(Direction.Axis.Y);
                    }
                }

                flag = true;
                break;
            }

            blockpos = blockpos.down();
            if (blockpos.getY() < MathHelper.floor(yStart) - 1) {
                break;
            }
        }

        if (flag) {
            world.addEntity(new EntityEvokerFangs(world, xAngle, (double)blockpos.getY() + d0, zAngle, rotationYaw, tickDelay, caster, bonusDamage));
        }

    }

    @Override
    public int getManaCost() {
        return 35;
    }

    @Nullable
    @Override
    public Item getCraftingReagent() {
        return Items.PRISMARINE_SHARD;
    }

    @Override
    public Tier getTier() {
        return Tier.THREE;
    }

    @Override
    protected String getBookDescription() {
        return "Summons Evoker Fangs in the direction where the spell was targeted.";
    }
}
