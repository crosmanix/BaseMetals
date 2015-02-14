package cyano.basemetals.items;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.oredict.OreDictionary;
import cyano.basemetals.init.Materials;
import cyano.basemetals.material.MetalMaterial;
import cyano.basemetals.registry.CrusherRecipeRegistry;
import cyano.basemetals.registry.recipe.ICrusherRecipe;

public class ItemMetalCrackHammer extends ItemTool{
	protected final MetalMaterial metal;
	protected final Set<String> toolTypes;
	protected final String repairOreDictName;
	protected final boolean regenerates;
	protected final long regenInterval = 200; 
	public ItemMetalCrackHammer(MetalMaterial metal) {
		super(1 + Materials.getToolMaterialFor(metal).getDamageVsEntity(), Materials.getToolMaterialFor(metal), new HashSet<Block>());
		this.metal = metal;
		this.setMaxDamage((int)(0.75 * metal.getToolDurability()));
		this.efficiencyOnProperMaterial = metal.getToolEfficiency();
		this.toolTypes = new HashSet<>();
		toolTypes.add("crackhammer");
		toolTypes.add("pickaxe");
		repairOreDictName = "ingot"+metal.getCapitalizedName();
		if(metal.equals(Materials.starsteel)){
			regenerates = true;
		} else {
			regenerates = false;
		}
	}

	
	@Override
    public float getStrVsBlock(final ItemStack tool, final Block target) {
		if(isCrushableBlock(target) && canHarvestBlock(target) ){
			return Math.max(1.0f, 0.5f * this.metal.getToolEfficiency());
		}
		return 1.0f;
    }
	
	@Override
    public boolean onBlockDestroyed(final ItemStack tool, final World world, 
    		final Block target, final BlockPos coord, final EntityLivingBase player) {
		if(!world.isRemote && this.canHarvestBlock(target)){
			IBlockState bs = world.getBlockState(coord);
			ICrusherRecipe recipe = getCrusherRecipe(bs);
			if(recipe != null){
				ItemStack output = recipe.getOutput().copy();
				world.setBlockToAir(coord);
				if(output != null){
					int num = output.stackSize;
					output.stackSize = 1;
					for(int i = 0; i < num; i++){
						world.spawnEntityInWorld(new EntityItem(world, coord.getX()+0.5, coord.getY()+0.5, coord.getZ()+0.5, output.copy()));
					}
				}
			}
		}
		return super.onBlockDestroyed(tool, world, target, coord, player);
		
	}
	
	@Override
	public boolean onItemUse(final ItemStack item, final EntityPlayer player, final World w, 
			final BlockPos coord, final EnumFacing facing, 
			final float partialX, final float partialY, final float partialZ) {		
		if(facing != EnumFacing.UP) return false;
		if(w.isRemote) return true;
		List<Entity> entities = w.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(
				coord.getX(),coord.getY()+1,coord.getZ(),
				coord.getX()+1,coord.getY()+2,coord.getZ()+1));
		boolean success = false;
		for(Entity target : entities){
			if(target instanceof net.minecraft.entity.item.EntityItem){
				ItemStack targetItem = ((net.minecraft.entity.item.EntityItem)target).getEntityItem();
				if(targetItem != null ){
					ICrusherRecipe recipe = CrusherRecipeRegistry.getInstance().getRecipeForInputItem(targetItem);
					if(recipe != null){
						// crush the item
						ItemStack output = recipe.getOutput().copy();
						int count = output.stackSize;
						output.stackSize = 1;
						double x = target.posX;
						double y = target.posY;
						double z = target.posZ;
						
						targetItem.stackSize--;
						if(targetItem.stackSize <= 0){
							w.removeEntity(target);
						}
						for(int i = 0; i < count; i++){
							w.spawnEntityInWorld(new EntityItem(w,x,y,z,output.copy()));
						}
						item.damageItem(1, player);
						success = true;
						break;
					}
				}
			}
		}
		if(success && !w.isRemote){
			w.playSoundAtEntity(player, "dig.gravel", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
		}
        return success;
    }
	
	protected boolean isCrushableBlock(IBlockState block){
		return getCrusherRecipe(block) != null;
	}
	protected boolean isCrushableBlock(Block block){
		return getCrusherRecipe(block) != null;
	}
	
	protected ICrusherRecipe getCrusherRecipe(Block block){
		return getCrusherRecipe(block.getDefaultState());
	}
	
	protected ICrusherRecipe getCrusherRecipe(IBlockState block){
		if(block == null || Item.getItemFromBlock(block.getBlock()) == null)return null;
		//return CrusherRecipeRegistry.getInstance().getRecipeForInputItem(block);
		return CrusherRecipeRegistry.getInstance().getRecipeForInputItem(new ItemStack(block.getBlock(),1,block.getBlock().getMetaFromState(block)));
	}
	


    public ToolMaterial getToolMaterial() {
        return this.toolMaterial;
    }
    
    @Override
    public int getItemEnchantability() {
        return this.toolMaterial.getEnchantability();
    }
    
    public String getToolMaterialName() {
        return this.toolMaterial.toString();
    }
    
    @Override
    public boolean getIsRepairable(final ItemStack intputItem, final ItemStack repairMaterial) {
    	List<ItemStack> acceptableItems = OreDictionary.getOres(repairOreDictName);
    	for(ItemStack i : acceptableItems ){
    		if(ItemStack.areItemsEqual(i, repairMaterial)) return true;
    	}
    	return false;
    }
    
    @Override
    public int getHarvestLevel(final ItemStack item, final String typeRequested) {
    	if (typeRequested != null && toolTypes.contains(typeRequested)) {
            return metal.getToolHarvestLevel();
        }
        return -1;
    }
    @Override
    public Set<String> getToolClasses(final ItemStack item) {
        return toolTypes;
    }
    
   
    
    @Override
    public boolean hitEntity(final ItemStack item, final EntityLivingBase target, final EntityLivingBase attacker) {
        super.hitEntity(item, target, attacker);
        MetalToolEffects.extraEffectsOnAttack(metal,item, target, attacker);
        return true;
    }
    
    @Override
    public void onCreated(final ItemStack item, final World world, final EntityPlayer crafter) {
    	super.onCreated(item, world, crafter);
    	MetalToolEffects.extraEffectsOnCrafting(metal,item, world, crafter);
    }
    
    
    @Override
    public void onUpdate(final ItemStack item, final World world, final Entity player, final int inventoryIndex, final boolean isHeld) {
    	if(regenerates && !world.isRemote && isHeld && item.getItemDamage() > 0 && world.getTotalWorldTime() % regenInterval == 0){
    		item.setItemDamage(item.getItemDamage() - 1);
    	}
    }
    
    @Override
    public boolean canHarvestBlock(final Block target) {
		if(this.toolTypes.contains(target.getHarvestTool(target.getDefaultState()))){
			return metal.getToolHarvestLevel() >= target.getHarvestLevel(target.getDefaultState());
		}
		return false;
    }
   
    public String getMaterialName() {
        return metal.getName();
    }
}