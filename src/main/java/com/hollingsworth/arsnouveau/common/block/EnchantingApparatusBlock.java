package com.hollingsworth.arsnouveau.common.block;

import com.hollingsworth.arsnouveau.common.block.tile.ArcanePedestalTile;
import com.hollingsworth.arsnouveau.common.block.tile.EnchantingApparatusTile;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EnchantingApparatusBlock extends ModBlock{
    public static final IProperty stage = IntegerProperty.create("stage", 1, 47);

    public EnchantingApparatusBlock() {
        super("enchanting_apparatus");
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(!world.isRemote) {
            EnchantingApparatusTile tile = (EnchantingApparatusTile) world.getTileEntity(pos);
            if(player.isSneaking()){
                tile.attemptCraft();
                return true;
            }
            if (tile.catalystItem != null && player.getHeldItem(handIn).isEmpty()) {
                ItemEntity item = new ItemEntity(world, player.posX, player.posY, player.posZ, tile.catalystItem);
                world.addEntity(item);
                tile.catalystItem = null;
            } else if (!player.inventory.getCurrentItem().isEmpty()) {
                tile.catalystItem = player.inventory.decrStackSize(player.inventory.currentItem, 1);
            }
            world.notifyBlockUpdate(pos, state, state, 2);
        }
        return true;
    }


    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EnchantingApparatusTile();
    }
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<net.minecraft.block.Block, BlockState> builder) { builder.add(stage); }
}
