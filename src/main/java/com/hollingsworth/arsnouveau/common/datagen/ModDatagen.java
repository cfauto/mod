package com.hollingsworth.arsnouveau.common.datagen;

import com.hollingsworth.arsnouveau.ArsNouveau;
import net.minecraft.data.RecipeProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDatagen {

    @SubscribeEvent
    public static void datagen(GatherDataEvent event){
        event.getGenerator().addProvider(new LootTables(event.getGenerator()));
        event.getGenerator().addProvider(new ItemModelGenerator(event.getGenerator(), ArsNouveau.MODID, event.getExistingFileHelper()));
        event.getGenerator().addProvider(new LangDatagen(event.getGenerator(), ArsNouveau.MODID, "en_us"));
        event.getGenerator().addProvider(new SpellDocProvider(event.getGenerator()));
        event.getGenerator().addProvider(new Recipes(event.getGenerator()));
    }
}
