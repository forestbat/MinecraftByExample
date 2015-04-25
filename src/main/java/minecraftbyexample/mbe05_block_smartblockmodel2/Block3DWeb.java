package minecraftbyexample.mbe05_block_smartblockmodel2;

import minecraftbyexample.mbe04_block_smartblockmodel1.UnlistedPropertyCopiedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by TheGreyGhost on 19/04/2015.
 *
 * This block forms a 3D web.
 * If the block is adjacent to another Block3DWeb, or to a solid surface, it joins to it with a strand of web.
 * Its IBlockState has six unlisted properties, one for each direction (up, down, north, south, east, west)
 */
public class Block3DWeb extends Block {
  public Block3DWeb()
  {
    super(Material.web);                     // ensures the player can walk through the block
    this.setCreativeTab(CreativeTabs.tabBlock);   // the block will appear on the Blocks tab in creative
  }

  // the block will render in the SOLID layer.  See http://greyminecraftcoder.blogspot.co.at/2014/12/block-rendering-18.html for more information.
  @SideOnly(Side.CLIENT)
  public EnumWorldBlockLayer getBlockLayer()
  {
    return EnumWorldBlockLayer.SOLID;
  }

  // used by the renderer to control lighting and visibility of other blocks.
  // set to false because this block doesn't fully occupy the entire 1x1x1 space
  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  // make colliding players stick in the web like normal web
  public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
  {
    entityIn.setInWeb();
  }

  // used by the renderer to control lighting and visibility of other blocks, also by
  // (eg) wall or fence to control whether the fence joins itself to this block
  // set to false because this block does not occupy the entire 1x1x1 space
  @Override
  public boolean isFullCube() {
    return false;
  }

  // render using an IBakedModel
  // not strictly required because the default (super method) is 3.
  @Override
  public int getRenderType() {
    return 3;
  }

  // by returning a null collision bounding box, we stop the player from colliding with it
  @Override
  public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
  {
    return null;
  }

  // createBlockState is used to define which properties your blocks possess
  // Vanilla BlockState is composed of listed properties only.  A variant is created for each combination of listed
  //   properties; for example two properties ON(true/false) and READY(true/false) would give rise to four variants
  //   [on=true, ready=true]
  //   [on=false, ready=true]
  //   [on=true, ready=false]
  //   [on=false, ready=false]
  // Forge adds ExtendedBlockState, which has two types of property:
  // - listed properties (like vanilla), and
  // - unlisted properties, which can be used to convey information but do not cause extra variants to be created.
  @Override
  protected BlockState createBlockState() {
    IProperty [] listedProperties = new IProperty[0]; // no listed properties
    IUnlistedProperty [] unlistedProperties = new IUnlistedProperty[] {LINK_UP, LINK_DOWN, LINK_EAST, LINK_WEST, LINK_NORTH, LINK_SOUTH};
    return new ExtendedBlockState(this, listedProperties, unlistedProperties);
  }

  // this method uses the block state and BlockPos to update the unlisted LINK properties in IExtendedBlockState based
  // on non-metadata information.  This is then conveyed to the ISmartBlockModel during rendering.
  // In this case, we look around the block to see which faces are next to either a solid block or another web block:
  // The web node forms a strand of web to any adjacent solid blocks or web nodes
  @Override
  public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
    if (state instanceof IExtendedBlockState) {  // avoid crash in case of mismatch
      IExtendedBlockState retval = (IExtendedBlockState)state;
      boolean linkup = canConnectTo(world, pos.up());
      retval = retval.withProperty(LINK_UP, linkup);

      boolean linkdown = canConnectTo(world, pos.down());
      retval = retval.withProperty(LINK_DOWN, linkdown);

      boolean linkeast = canConnectTo(world, pos.east());
      retval = retval.withProperty(LINK_EAST, linkeast);

      boolean linkwest = canConnectTo(world, pos.west());
      retval = retval.withProperty(LINK_WEST, linkwest);

      boolean linknorth = canConnectTo(world, pos.north());
      retval = retval.withProperty(LINK_NORTH, linknorth);

      boolean linksouth = canConnectTo(world, pos.south());
      retval = retval.withProperty(LINK_SOUTH, linksouth);

      return retval;
    }
    return state;
  }

  /** returns true if the web should connect to this block
   * Copied from BlockFence...
   * @param worldIn
   * @param pos
   * @return
   */
  private boolean canConnectTo(IBlockAccess worldIn, BlockPos pos)
  {
    Block block = worldIn.getBlockState(pos).getBlock();
    if (block == Blocks.barrier) return false;
    if (block == StartupCommon.block3DWeb) return true;
    if (block.getMaterial().isOpaque() && block.isFullCube() && block.getMaterial() != Material.gourd) return true;
    return false;
  }

  // the LINK properties are used to communicate to the ISmartBlockModel which of the links should be drawn
  public static final IUnlistedProperty<Boolean> LINK_UP = new Properties.PropertyAdapter<Boolean>(PropertyBool.create("link_up"));
  public static final IUnlistedProperty<Boolean> LINK_DOWN = new Properties.PropertyAdapter<Boolean>(PropertyBool.create("link_down"));
  public static final IUnlistedProperty<Boolean> LINK_WEST = new Properties.PropertyAdapter<Boolean>(PropertyBool.create("link_west"));
  public static final IUnlistedProperty<Boolean> LINK_EAST = new Properties.PropertyAdapter<Boolean>(PropertyBool.create("link_east"));
  public static final IUnlistedProperty<Boolean> LINK_NORTH = new Properties.PropertyAdapter<Boolean>(PropertyBool.create("link_north"));
  public static final IUnlistedProperty<Boolean> LINK_SOUTH = new Properties.PropertyAdapter<Boolean>(PropertyBool.create("link_south"));
}
