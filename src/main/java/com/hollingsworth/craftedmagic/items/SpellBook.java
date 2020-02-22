package com.hollingsworth.craftedmagic.items;

import com.hollingsworth.craftedmagic.ArsNouveau;
import com.hollingsworth.craftedmagic.api.CraftedMagicAPI;
import com.hollingsworth.craftedmagic.api.spell.AbstractSpellPart;
import com.hollingsworth.craftedmagic.api.spell.ISpellTier;
import com.hollingsworth.craftedmagic.network.Networking;
import com.hollingsworth.craftedmagic.network.PacketOpenGUI;
import com.hollingsworth.craftedmagic.spell.SpellResolver;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SpellBook extends Item implements ISpellTier {
    public static final String BOOK_MODE_TAG = "mode";

    public SpellBook(){
        super(new Item.Properties().maxStackSize(1).group(ArsNouveau.itemGroup));
        //setUnlocalizedName(ExampleMod.MODID + ".spell_book");     // Used for localization (en_US.lang)
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if(!worldIn.isRemote && worldIn.getGameTime() % 20 == 0 && !stack.hasTag()) {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt(SpellBook.BOOK_MODE_TAG, 0);
            stack.setTag(tag);
        }
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

        /**
     * Returns true if the item can be used on the given entity, e.g. shears on sheep.
     */
    public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
        if(!playerIn.getEntityWorld().isRemote) {
            SpellResolver resolver = new SpellResolver(getCurrentRecipe(stack));
            resolver.onCastOnEntity(stack, playerIn, target, hand);
                                SoundEvent event = new SoundEvent(new ResourceLocation(ArsNouveau.MODID, "cast_spell"));
                    playerIn.world.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, event, SoundCategory.BLOCKS,
                            4.0F, (1.0F + (playerIn.world.rand.nextFloat() - playerIn.world.rand.nextFloat()) * 0.2F) * 0.7F);
//            int totalCost = spell_r.stream().mapToInt(AbstractSpellPart::getManaCost).sum();
//            if(!spell_r.isEmpty()) {
//
//                ManaCapability.getMana(playerIn).ifPresent(mana -> {
//                    System.out.println(totalCost);
//                    if(totalCost <= mana.getCurrentMana() || playerIn.isCreative()) {
//                        SpellResolver resolver = new SpellResolver(spell_r);
//                        resolver.onCastOnEntity(stack, playerIn, target, hand);
//                        mana.removeMana(totalCost);
//                        System.out.println(mana.getCurrentMana());
//                    }else{
//                        System.out.println("Not enough mana");
//                    }
//                });
//            }
        }
        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if(worldIn.isRemote || !stack.hasTag()){
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        System.out.println("Right click");
        if(getMode(stack.getTag()) == 0 && !playerIn.isSneaking() && playerIn instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) playerIn;
            Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()->player), new PacketOpenGUI(stack.getTag(), getTier().ordinal()));
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        if (playerIn.isSneaking() && stack.hasTag()){
            changeMode(stack);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }


        SpellResolver resolver = new SpellResolver(getCurrentRecipe(stack));
        resolver.onCast(stack, playerIn, worldIn);
        SoundEvent event = new SoundEvent(new ResourceLocation(ArsNouveau.MODID, "cast_spell"));
        playerIn.world.playSound( playerIn.posX, playerIn.posY, playerIn.posZ, event, SoundCategory.BLOCKS,
                4.0F, 1.0f, false);
        return new ActionResult<>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
    }

    /*
    Called on block use. TOUCH ONLY
     */
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World worldIn = context.getWorld();
        PlayerEntity playerIn = context.getPlayer();
        Hand handIn = context.getHand();
        BlockPos blockpos = context.getPos();
        BlockPos blockpos1 = blockpos.offset(context.getFace());
        ItemStack stack = playerIn.getHeldItem(handIn);

        if(worldIn.isRemote || !stack.hasTag() || getMode(stack.getTag()) == 0 || playerIn.isSneaking()) return ActionResultType.FAIL;

        SpellResolver resolver = new SpellResolver(getCurrentRecipe(stack));
        resolver.onCastOnBlock(context);
        return ActionResultType.SUCCESS;
    }

    public ArrayList<AbstractSpellPart> getCurrentRecipe(ItemStack stack){
        return SpellBook.getRecipeFromTag(stack.getTag(), getMode(stack.getTag()));
    }


    private void changeMode(ItemStack stack) {
        setMode(stack, (getMode(stack.getTag()) + 1) % 4);
    }

    public static ArrayList<AbstractSpellPart> getRecipeFromTag(CompoundNBT tag, int r_slot){
        ArrayList<AbstractSpellPart> recipe = new ArrayList<>();
        String recipeStr = getRecipeString(tag, r_slot);
        if (recipeStr.length() <= 3) // Account for empty strings and '[,]'
            return recipe;
        String[] recipeList = recipeStr.substring(1, recipeStr.length() - 1).split(",");
        for(String id : recipeList){
            if (CraftedMagicAPI.getInstance().spell_map.containsKey(id.trim()))
                recipe.add(CraftedMagicAPI.getInstance().spell_map.get(id.trim()));
        }
        return recipe;
    }

    public static void setSpellName(CompoundNBT tag, String name, int slot){
        tag.putString(slot + "_name", name);
    }

    public static String getSpellName(CompoundNBT tag, int slot){
        return tag.getString( slot+ "_name");
    }

    public static String getSpellName(CompoundNBT tag){
        return getSpellName( tag, getMode(tag));
    }

    public static String getRecipeString(CompoundNBT tag, int spell_slot){
        return tag.getString(spell_slot + "recipe");
    }

    public static void setRecipe(CompoundNBT tag, String recipe, int spell_slot){
        tag.putString(spell_slot + "recipe", recipe);
    }

    public static int getMode(CompoundNBT tag){
        return tag.getInt(SpellBook.BOOK_MODE_TAG);
    }

    public void setMode(ItemStack stack, int mode){
        stack.getTag().putInt(SpellBook.BOOK_MODE_TAG, mode);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<ITextComponent> tooltip, final ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if(stack != null && stack.hasTag()) {
            CompoundNBT tag = stack.getTag();
            tooltip.add(new StringTextComponent(SpellBook.getSpellName(stack.getTag())));
        }
    }

    @Override
    public Tier getTier() {
        return Tier.ONE;
    }
}
