package cyano.basemetals.items;

import astro.lib.api.item.IHorseArmor;
import cyano.basemetals.material.IMetalObject;
import cyano.basemetals.material.MetalMaterial;
import cyano.basemetals.utils.HorseUtilities;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.HorseArmorType;
import net.minecraft.entity.passive.HorseType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 *
 * @author Jasmine Iwanek
 *
 */
public class ItemMetalHorseArmor extends Item implements IMetalObject, IHorseArmor {

	private final MetalMaterial metal;

	public ItemMetalHorseArmor(MetalMaterial m) {
		this.metal = m;
		this.setCreativeTab(CreativeTabs.COMBAT);
		this.setMaxStackSize(1);
	}

	@Override
	public HorseArmorType getArmorType() {
		return HorseUtilities.getType("test", 1024, "tes");
	}

	@Override
	public String getArmorTexture(EntityHorse entity, ItemStack stack) {
		return stack.getItem().getRegistryName().getResourceDomain() + ":textures/entity/horse/armor/horse_armor_" + this.metal.getName() + ".png";
	}

	@Override
	public MetalMaterial getMaterial() {
		return this.metal;
	}

	@Override
	@Deprecated
	public MetalMaterial getMetalMaterial() {
		return this.metal;
	}
}