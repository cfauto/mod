package com.hollingsworth.arsnouveau.common.items;

import com.hollingsworth.arsnouveau.ArsNouveau;
import net.minecraft.item.Item;

public class ModItem extends Item {
    public ModItem(Properties properties) {
        super(properties);
    }

    public ModItem(Properties properties, String registryName){
        this(properties);
        setRegistryName(ArsNouveau.MODID, registryName);
    }

    public ModItem(String registryName){
        this(ItemsRegistry.defaultItemProperties(), registryName);
    }
}
