package com.hollingsworth.arsnouveau.api.util;

import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import com.hollingsworth.arsnouveau.api.ISpellBonus;
import com.hollingsworth.arsnouveau.api.spell.AbstractAugment;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.common.items.SpellBook;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SpellRecipeUtil {

    public static ArrayList<AbstractAugment> getAugments(ArrayList<AbstractSpellPart> spell_recipe, int startPosition, @Nullable LivingEntity caster){
        ArrayList<AbstractAugment> augments = new ArrayList<>();
        for(int j = startPosition + 1; j < spell_recipe.size(); j++){
            AbstractSpellPart next_spell = spell_recipe.get(j);
            if(next_spell instanceof AbstractAugment){
                augments.add((AbstractAugment) next_spell);
            }else{
                break;
            }
        }
        // Add augment bonuses from equipment
        if(caster != null)
            augments.addAll(getEquippedAugments(caster));
        return augments;
    }

    /**
     * Returns the list of augments that come from equipment
     */
    public static List<AbstractAugment> getEquippedAugments(@Nonnull LivingEntity caster){
        ArrayList<AbstractAugment> augments = new ArrayList<>();
        CuriosUtil.getAllWornItems(caster).ifPresent(e ->{
            for(int i = 0; i < e.getSlots(); i++){
                Item item = e.getStackInSlot(i).getItem();
                if(item instanceof ISpellBonus)
                    augments.addAll(((ISpellBonus) item).getList());
            }

        });
        caster.getArmorInventoryList().forEach(itemStack -> {
            if(itemStack.getItem() instanceof ISpellBonus)
                augments.addAll(((ISpellBonus) itemStack.getItem()).getList());
        });
        return augments;
    }

    public static ArrayList<AbstractSpellPart> getSpellsFromString(String spellString){
        List<String> spellStrings = Arrays.asList(spellString.split(","));
        ArrayList<AbstractSpellPart> spells = new ArrayList<>();

        spellStrings.forEach(s->{
            Optional<AbstractSpellPart> spell =  ArsNouveauAPI.getInstance().getSpell_map().values().stream().filter(sp -> sp.getTag().equals(s.trim())).findFirst();
            spell.ifPresent(spells::add);
        });
        return spells;
    }
}
