package com.hollingsworth.arsnouveau.common.entity.goal;

import com.hollingsworth.arsnouveau.api.ISummon;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;

import java.util.EnumSet;

public class FollowSummonerGoal extends Goal {
    protected final ISummon summon;
    protected final IWorldReader world;
    private final double followSpeed;
    private final PathNavigator navigator;
    private int timeToRecalcPath;
    private final float maxDist;
    private final float minDist;
    private float oldWaterCost;

    public FollowSummonerGoal(ISummon mobEntity, LivingEntity owner, double followSpeedIn, float minDistIn, float maxDistIn) {
        this.summon = mobEntity;
        this.world = mobEntity.getWorld();
        this.followSpeed = followSpeedIn;
        this.navigator = mobEntity.getNavigator();
        this.minDist = minDistIn;
        this.maxDist = maxDistIn;
        System.out.println("Made follow goal2");
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(mobEntity.getNavigator() instanceof GroundPathNavigator) && !(mobEntity.getNavigator() instanceof FlyingPathNavigator)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        LivingEntity livingentity = summon.getSummoner();
        //System.out.println("Checking should exec");
        if (livingentity == null) {
            return false;
        } else if (livingentity instanceof PlayerEntity && ((PlayerEntity)livingentity).isSpectator()) {
            return false;
        } else if (this.summon instanceof TameableEntity && ((TameableEntity) this.summon).isSitting()) {
            return false;
        } else if (this.summon.getSelfEntity().getDistanceSq(livingentity) < (double)(this.minDist * this.minDist)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting() {
        System.out.println("Shoul continue");
        boolean flag = true;
        if(this.summon instanceof TameableEntity)
            flag = !((TameableEntity) this.summon).isSitting();

        if(this.summon.getSummoner() == null)
            return false;

        return !this.navigator.noPath() && this.summon.getSelfEntity().getDistanceSq(this.summon.getSummoner()) > (double)(this.maxDist * this.maxDist) && flag;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        System.out.println("Started executing");
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.summon.getSelfEntity().getPathPriority(PathNodeType.WATER);
        this.summon.getSelfEntity().setPathPriority(PathNodeType.WATER, 0.0F);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask() {
        System.out.println("Interupted");
        this.navigator.clearPath();
        this.summon.getSelfEntity().setPathPriority(PathNodeType.WATER, this.oldWaterCost);
    }


    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        System.out.println("Ticking");
        if(this.summon.getSummoner() == null) {
            System.out.println("Null owner");
            return;
        }
        this.summon.getSelfEntity().getLookController().setLookPositionWithEntity(this.summon.getSummoner(), 10.0F, (float)this.summon.getSelfEntity().getVerticalFaceSpeed());
        if(this.summon instanceof TameableEntity && ((TameableEntity) this.summon).isSitting())
            return;

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            System.out.println("Ticking");
            if (!this.navigator.tryMoveToEntityLiving(this.summon.getSummoner(), this.followSpeed)) {
                System.out.println("Ticking");
                if (!(this.summon.getSelfEntity().getDistanceSq(this.summon.getSummoner()) < 144.0D)) {
                    int i = MathHelper.floor(this.summon.getSummoner().posX) - 2;
                    int j = MathHelper.floor(this.summon.getSummoner().posZ) - 2;
                    int k = MathHelper.floor(this.summon.getSummoner().getBoundingBox().minY);

                    for(int l = 0; l <= 4; ++l) {
                        for(int i1 = 0; i1 <= 4; ++i1) {
                            if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.canTeleportToBlock(new BlockPos(i + l, k - 1, j + i1))) {
                                this.summon.getSelfEntity().setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), this.summon.getSelfEntity().rotationYaw, this.summon.getSelfEntity().rotationPitch);
                                this.navigator.clearPath();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean canTeleportToBlock(BlockPos pos) {
        BlockState blockstate = this.world.getBlockState(pos);
        return blockstate.canEntitySpawn(this.world, pos, this.summon.getSelfEntity().getType()) && this.world.isAirBlock(pos.up()) && this.world.isAirBlock(pos.up(2));
    }
}
