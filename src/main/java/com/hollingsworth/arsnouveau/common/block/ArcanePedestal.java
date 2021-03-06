package com.hollingsworth.arsnouveau.common.block;

import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import com.hollingsworth.arsnouveau.common.block.tile.ArcanePedestalTile;
import com.hollingsworth.arsnouveau.common.block.tile.GlyphPressTile;
import com.hollingsworth.arsnouveau.common.items.ItemsRegistry;
import com.hollingsworth.arsnouveau.common.lib.LibBlockNames;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ArcanePedestal extends ModBlock{

    public ArcanePedestal() {
        super(LibBlockNames.ARCANE_PEDESTAL);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(!world.isRemote) {
            ArcanePedestalTile tile = (ArcanePedestalTile) world.getTileEntity(pos);
            if (tile.stack != null && player.getHeldItem(handIn).isEmpty()) {
                if(world.getBlockState(pos.up()).getMaterial() != Material.AIR)
                    return true;
                ItemEntity item = new ItemEntity(world, player.posX, player.posY, player.posZ, tile.stack);
                world.addEntity(item);
                tile.stack = null;
            } else if (!player.inventory.getCurrentItem().isEmpty()) {
                tile.stack = player.inventory.decrStackSize(player.inventory.currentItem, 1);
            }
            world.notifyBlockUpdate(pos, state, state, 2);
        }
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ArcanePedestalTile();
    }
}
