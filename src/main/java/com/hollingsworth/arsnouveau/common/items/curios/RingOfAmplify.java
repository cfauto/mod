package com.hollingsworth.arsnouveau.common.items.curios;

import com.hollingsworth.arsnouveau.ModConfig;
import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import com.hollingsworth.arsnouveau.api.item.AbstractAugmentItem;
import com.hollingsworth.arsnouveau.api.spell.AbstractAugment;

public class RingOfAmplify extends AbstractAugmentItem {

    public RingOfAmplify() {
        super("ring_of_amplify");
    }

    @Override
    public AbstractAugment getBonusAugment() {
        return (AbstractAugment) ArsNouveauAPI.getInstance().getSpell_map().get(ModConfig.AugmentAmplifyID);
    }

    @Override
    public int getBonusLevel() {
        return 1;
    }
}
