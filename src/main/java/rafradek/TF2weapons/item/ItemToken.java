package rafradek.TF2weapons.item;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;

public class ItemToken extends Item {

	public static final UUID SPEED_UUID = UUID.fromString("769e0d47-bb57-45ed-8fe4-ab84592d842b");
	public static final UUID HEALTH_UUID = UUID.fromString("ff0b26f3-cf6d-49e6-9989-3886ec3d3b83");
	public static final UUID HEALTH_MULT_UUID = UUID.fromString("ff0b26f3-cf6d-49e6-9989-3886ec3d3e83");
	
	public static final float[] SPEED_VALUES= {0.7638f, 0.0583f, 0.329f, 0.2347f, 0f, 0.329f, 0.4111f, 0.329f, 0.4111f};
	public static final float[] FOV_VALUES= {0.7638f, 0.0583f, 0.329f, 0.2347f, 0f, 0.329f, 0.4111f, 0.329f, 0.4111f};
	public static final float[] HEALTH_VALUES= {-7.5f, 0f, -2.5f, -2.5f, 10f, -7.5f, -5f, -7.5f, -7.5f};
	public static final double[] EXPLOSION_VALUES= {1D, 0.667D, 0.95D, 0.75D, 0.5D, 1D, 0.95D, 1D, 1D};
	public static final String[] CLASS_NAMES= {"scout", "soldier", "pyro", "demoman", "heavy", "engineer", "medic", "sniper", "spy"};
	public ItemToken() {
		this.setHasSubtypes(true);
		this.setCreativeTab(TF2weapons.tabsurvivaltf2);
		this.setUnlocalizedName("token");
	}

	@Override
	public void getSubItems(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if(!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < 9; i++)
			par3List.add(new ItemStack(this, 1, i));
	}
	
	public void updateAttributes(ItemStack stack, EntityLivingBase living) {
		living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(SPEED_UUID);
		living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(HEALTH_UUID);
		living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(HEALTH_MULT_UUID);
		if (!stack.isEmpty() && stack.getMetadata() >= 0 && stack.getMetadata() < CLASS_NAMES.length) {
			WeaponsCapability.get(living).setUsedToken(stack.getMetadata());
			living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(new AttributeModifier(SPEED_UUID, "tokenspeed", SPEED_VALUES[stack.getMetadata()], 2)); 
			living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier(HEALTH_UUID, "tokenhealth", HEALTH_VALUES[stack.getMetadata()], 0)); 
			living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier(HEALTH_MULT_UUID, "tokenhealthmult", TF2ConfigVars.damageMultiplier-1, 2)); 
		}
		else {
			WeaponsCapability.get(living).setUsedToken(-1);
		}
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick( World world, EntityPlayer living, EnumHand hand) {
		if (!world.isRemote)
			FMLNetworkHandler.openGui(living, TF2weapons.instance, 0, world, 0, 0, 0);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, living.getHeldItem(hand));
	}
	
	public static boolean allowUse(EntityLivingBase living, String clazz) {
		return allowUse(living, Sets.newHashSet(clazz));
	}
	
	public static boolean allowUse(EntityLivingBase living, Set<String> clazz) {
		return !(living instanceof EntityPlayer) || allowUse(WeaponsCapability.get(living).getUsedToken(), clazz);
	}
	
	public static boolean allowUse(int livingclass, Set<String> clazz) {
		return clazz.isEmpty() || !(livingclass >= 0 && livingclass < CLASS_NAMES.length
				&& !clazz.contains(CLASS_NAMES[livingclass]));
	}
	
	public String getUnlocalizedName(ItemStack stack)
    {
        return super.getUnlocalizedName(stack)+"."+CLASS_NAMES[stack.getMetadata()];
    }
	
	public static int getClassID(String name) {
		switch (name) {
		case "scout": return 0; 
		case "soldier": return 1; 
		case "pyro": return 2; 
		case "demoman": return 3; 
		case "heavy": return 4; 
		case "engineer": return 5; 
		case "medic": return 6; 
		case "sniper": return 7; 
		case "spy": return 8; 
		default: return 0;
		}
	}
}
