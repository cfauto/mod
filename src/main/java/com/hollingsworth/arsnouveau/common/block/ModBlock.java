package com.hollingsworth.arsnouveau.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class ModBlock extends Block {

    public ModBlock(Properties properties, String registry) {
        super(properties);
        setRegistryName(registry);
    }

    public ModBlock(String registryName){
        this(defaultProperties(), registryName);
    }

    public static Block.Properties defaultProperties(){
        return Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(2.0f);
    }
}
