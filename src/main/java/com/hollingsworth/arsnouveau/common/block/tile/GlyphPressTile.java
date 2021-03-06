package com.hollingsworth.arsnouveau.common.block.tile;

import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import com.hollingsworth.arsnouveau.api.spell.ISpellTier;
import com.hollingsworth.arsnouveau.common.block.BlockRegistry;
import com.hollingsworth.arsnouveau.common.block.GlyphPressBlock;
import com.hollingsworth.arsnouveau.common.items.Glyph;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

public class GlyphPressTile extends AnimatedTile implements ITickableTileEntity, IInventory {
    public long frames;
    public boolean isCrafting;
    public ItemStack reagentItem;
    public ItemStack baseMaterial;
    public ItemEntity entity;
    public long timeStartedSpraying;

    public GlyphPressTile() {
        super(BlockRegistry.GLYPH_PRESS_TILE);
        frames = 0;
        //itemStack = new ItemStack(Items.AIR);
    }

    @Override
    public void read(CompoundNBT compound) {
        reagentItem = ItemStack.read((CompoundNBT)compound.get("itemStack"));
        baseMaterial = ItemStack.read((CompoundNBT)compound.get("baseMat"));
        isCrafting = compound.getBoolean("crafting");
        timeStartedSpraying = compound.getLong("spraying");
        super.read(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        if(reagentItem != null) {
            CompoundNBT reagentTag = new CompoundNBT();
            reagentItem.write(reagentTag);
            compound.put("itemStack", reagentTag);
        }
        if(baseMaterial != null){
            CompoundNBT baseMatTag = new CompoundNBT();
            baseMaterial.write(baseMatTag);
            compound.put("baseMat", baseMatTag);
        }
        compound.putBoolean("crafting", isCrafting);
        compound.putLong("spraying", timeStartedSpraying);
        return super.write(compound);
    }
    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {

        return new SUpdateTileEntityPacket(this.pos, 3, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getNbtCompound());
    }

// 5 - become template
// 20 - spraying
    @Override
    public void tick() {
        if(!isCrafting){
            return;
        }

        if(world.isRemote && counter != 20)
            return;

        if(counter < 9 ){
//            if(world.getGameTime() % 4 != 0)
//                return;
            counter += 1;


            updateBlock();
        }else if(counter <= 19){
            if(world.getGameTime() % 2 != 0)
                return;
            counter += 1;
            if(counter == 19) {
                this.timeStartedSpraying = world.getGameTime();
              //  counter = 20;
            }
            updateBlock();
        }else if(counter <= 20){
            if(world.isRemote && world.getGameTime() % 2 != 0) {
                for (int i = 0; i < 1; i++) {
                    double posX = pos.getX();
                    double posY = pos.getY();
                    double posZ = pos.getZ();

                    double randX = world.rand.nextFloat() > 0.5 ? world.rand.nextFloat() : -world.rand.nextFloat();
                    double randZ = world.rand.nextFloat() > 0.5 ? world.rand.nextFloat() : -world.rand.nextFloat();

                    double d0 = posX + 0.5 + randX * 0.2; //+ world.rand.nextFloat();
                    double d1 = posY + 0.4;//+ world.rand.nextFloat() ;
                    double d2 = posZ + 0.5 + randZ * 0.2; //+ world.rand.nextFloat();
                    double spdX = world.rand.nextFloat() > 0.5 ? world.rand.nextFloat() : -world.rand.nextFloat();
                    double spdZ = world.rand.nextFloat() > 0.5 ? world.rand.nextFloat() : -world.rand.nextFloat();

                    world.addParticle(ParticleTypes.ENCHANTED_HIT, d0, d1, d2,  spdX * 0.05, 0.0,  spdZ * 0.05);
                }
                return;
            }
            if(!world.isRemote && world.getGameTime() - this.timeStartedSpraying > 20 * 5){
                counter += 1;
                Glyph glyph = ArsNouveauAPI.getInstance().hasCraftingReagent(reagentItem.getItem());
                this.baseMaterial = new ItemStack(glyph);
                updateBlock();
            }
         //  counter += 1;
        }
        else if(counter < 31){
            if(world.getGameTime() % 2 != 0)
                return;
            counter += 1;
            if(counter ==31) {
                Glyph glyph = ArsNouveauAPI.getInstance().hasCraftingReagent(reagentItem.getItem());
                AtomicBoolean canContinue = new AtomicBoolean(false);
                int manaCost = glyph.spellPart.getTier() == ISpellTier.Tier.ONE ? 2000 : (glyph.spellPart.getTier() == ISpellTier.Tier.TWO ? 4000 : 6000);
                BlockPos.getAllInBox(this.getPos().add(5, -3, 5), this.getPos().add(-5, 3, -5)).forEach(blockPos -> {
                    if(world.getTileEntity(blockPos) instanceof ManaJarTile && ((ManaJarTile) world.getTileEntity(blockPos)).getCurrentMana() >= manaCost && !canContinue.get()) {
                        ((ManaJarTile) world.getTileEntity(blockPos)).removeMana(manaCost);
                        canContinue.set(true);
                        return;
                    }
                });
                counter = 1;

                world.addEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY()+0.5, pos.getZ()+0.5, new ItemStack(glyph)));
                reagentItem = new ItemStack(null);
                this.baseMaterial = new ItemStack(null);
                isCrafting = false;
            }
            updateBlock();
        }
    }

    public void updateBlock(){
        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(GlyphPressBlock.stage, counter), 3);
        world.notifyBlockUpdate(pos, state, state, 2);
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return reagentItem;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        reagentItem.shrink(1);
        return reagentItem;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = reagentItem;
        reagentItem.setCount(0);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        reagentItem = stack;
        System.out.println("Setting slot");
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        reagentItem = null;
    }

    public boolean craft(PlayerEntity playerEntity) {
        System.out.println("crafting");
        if(isCrafting)
            return false;
        Glyph glyph = ArsNouveauAPI.getInstance().hasCraftingReagent(reagentItem.getItem());
        int manaCost = glyph.spellPart.getTier() == ISpellTier.Tier.ONE ? 2000 : (glyph.spellPart.getTier() == ISpellTier.Tier.TWO ? 4000 : 6000);
        AtomicBoolean valid = new AtomicBoolean(false);
        if(glyph == null)
            return false;
        BlockPos.getAllInBox(this.getPos().add(5, -3, 5), this.getPos().add(-5, 3, -5)).forEach(blockPos -> {
            if(world.getTileEntity(blockPos) instanceof ManaJarTile && ((ManaJarTile) world.getTileEntity(blockPos)).getCurrentMana() >= manaCost) {
                valid.set(true);

            }
        });
        if(!valid.get())
            playerEntity.sendMessage(new StringTextComponent("There does not appear to be enough mana nearby. "));
        if(glyph != null && valid.get()){
            isCrafting = true;
            return true;
        }
        return false;
    }
}
