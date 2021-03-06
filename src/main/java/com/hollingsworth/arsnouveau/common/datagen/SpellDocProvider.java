package com.hollingsworth.arsnouveau.common.datagen;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import com.hollingsworth.arsnouveau.api.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.setup.APIRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;


public class SpellDocProvider implements IDataProvider {
    private final DataGenerator generator;
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private static final Logger LOGGER = LogManager.getLogger();
    public SpellDocProvider(DataGenerator generatorIn) {
        this.generator = generatorIn;
    }

    @Override
    public void act(DirectoryCache cache) throws IOException {
        APIRegistry.registerApparatusRecipes();
        Path path = this.generator.getOutputFolder();
        System.out.println("ACTING IN DOC PROVIDER");
        ArrayList<AbstractSpellPart> spells = new ArrayList<>(ArsNouveauAPI.getInstance().getSpell_map().values());

        for(AbstractSpellPart spellPatchouliObj : spells){
            Path path1 = getSpellPath(path, spellPatchouliObj);
            try {
                IDataProvider.save(GSON, cache, spellPatchouliObj.serialize(), path1);
            } catch (IOException ioexception) {
                LOGGER.error("Couldn't save spell {}", path1, ioexception);
            }
        }

        ArrayList<EnchantingApparatusRecipe> apparatusRecipes = ArsNouveauAPI.getInstance().getEnchantingApparatusRecipes();
        System.out.println(apparatusRecipes);
        for(EnchantingApparatusRecipe r : apparatusRecipes ){
            Path path1 = getApparatusPath(path, r);
            try {
                IDataProvider.save(GSON, cache, r.serialize(), path1);
                System.out.println(r);
            } catch (IOException ioexception) {
                LOGGER.error("Couldn't save apparatus {}", path1, ioexception);
            }
        }
    }
    private static Path getSpellPath(Path pathIn, AbstractSpellPart spell) {
        return pathIn.resolve("data/ars_nouveau/spells/" + spell.getTag() + ".json");
    }
    private static Path getApparatusPath(Path pathIn, EnchantingApparatusRecipe e) {
        return pathIn.resolve("data/ars_nouveau/apparatus/" + e.result.getItem().getRegistryName().toString().replace(ArsNouveau.MODID + ":", "") + ".json");
    }

    @Override
    public String getName() {
        return "Spell Documentation Patchouli Provider";
    }

}
