package com.hollingsworth.arsnouveau.api.util;

import net.minecraft.entity.item.ItemEntity;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class MappingUtil {
    private static String ITEM_ENTITY_AGE;

    public static void setup(){
        //Prevent 'jump' in the bobbing
        //Bobbing is calculated as the age plus the yaw
       try{
           ObfuscationReflectionHelper.findField(ItemEntity.class, "field_70292_b");
           ITEM_ENTITY_AGE = "field_70292_b";
       }catch (Error e){
           System.out.println("Production field for Item Entity Age not found. Attempting to set dev mapping.");
           ObfuscationReflectionHelper.findField(ItemEntity.class, "age");
           ITEM_ENTITY_AGE = "age";
       }
    }

    public static String getItemEntityAge() {
        return ITEM_ENTITY_AGE;
    }
}
