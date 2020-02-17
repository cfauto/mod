package com.hollingsworth.craftedmagic.spell.effect;

import com.hollingsworth.craftedmagic.ModConfig;
import com.hollingsworth.craftedmagic.api.spell.AbstractEffect;
import com.hollingsworth.craftedmagic.spell.augment.AugmentDampen;
import com.hollingsworth.craftedmagic.spell.augment.AugmentAmplify;
import com.hollingsworth.craftedmagic.spell.augment.AugmentExtract;
import com.hollingsworth.craftedmagic.api.spell.AugmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.ArrayList;

public class EffectExplosion extends AbstractEffect {

    public EffectExplosion() {
        super(ModConfig.EffectExplosionID, "Explosion");
    }

    @Override
    public void onResolve(RayTraceResult rayTraceResult, World world, LivingEntity shooter, ArrayList<AugmentType> augments) {
        System.out.println(rayTraceResult);
        if(rayTraceResult == null)
            return;
        Vec3d vec = rayTraceResult.getHitVec();
        float intensity = 0.75f + getBuffCount(augments, AugmentAmplify.class);
        int dampen = getBuffCount(augments, AugmentDampen.class);
        intensity -= 0.5 * dampen;
        Explosion.Mode mode = hasBuff(augments, AugmentDampen.class) ? Explosion.Mode.NONE  : Explosion.Mode.DESTROY;
        mode = hasBuff(augments, AugmentExtract.class) ? Explosion.Mode.BREAK : mode;
        world.createExplosion(shooter,  vec.x, vec.y, vec.z, intensity,  mode);
    }

    @Override
    public int getManaCost() {
        return 35;
    }

    @Override
    public boolean dampenIsAllowed() {
        return true;
    }

    @Override
    public Tier getTier() {
        return Tier.TWO;
    }
}