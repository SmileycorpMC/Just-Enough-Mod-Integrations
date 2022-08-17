package rafradek.TF2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.common.WeaponsCapability.RageType;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;

public class ItemHorn extends Item {

	public ItemHorn() {
		this.setMaxStackSize(1);
		this.setCreativeTab(TF2weapons.tabutilitytf2);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
		ItemStack backpack = ItemBackpack.getBackpack(entityLiving);
		if (backpack.getItem() instanceof ItemSoldierBackpack && this.getMaxItemUseDuration(stack) - timeLeft >= ItemFromData.getData(backpack)
				.getInt(PropertyType.FIRE_SPEED) && WeaponsCapability.get(entityLiving).getRage(RageType.BANNER) >= 1f)
			((ItemSoldierBackpack) backpack.getItem()).setActive(entityLiving,backpack);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn,
			EnumHand hand) {
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		ItemStack backpack = ItemBackpack.getBackpack(playerIn);
		if (ItemToken.allowUse(playerIn, "soldier") && backpack.getItem() instanceof ItemSoldierBackpack
				&& (WeaponsCapability.get(playerIn).getRage(RageType.BANNER) >= 1f || playerIn.isCreative())) {
			playerIn.setActiveHand(hand);
			if (TF2Util.getTeamForDisplay(playerIn) == 1)
				playerIn.playSound(ItemFromData.getSound(backpack, PropertyType.HORN_BLU_SOUND), 0.8f, 1f);
			else
				playerIn.playSound(ItemFromData.getSound(backpack, PropertyType.HORN_RED_SOUND), 0.8f, 1f);
			return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
		}
		return new ActionResult<>(EnumActionResult.FAIL, itemStackIn);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean showDurabilityBar(ItemStack stack) {
		if (!(ItemBackpack.getBackpack(Minecraft.getMinecraft().player).getItem() instanceof ItemSoldierBackpack))
			return false;
		return WeaponsCapability.get(Minecraft.getMinecraft().player).getRage(RageType.BANNER) != 1f;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getDurabilityForDisplay(ItemStack stack) {
		if (!(ItemBackpack.getBackpack(Minecraft.getMinecraft().player).getItem() instanceof ItemSoldierBackpack))
			return 0;
		return 1-WeaponsCapability.get(Minecraft.getMinecraft().player).getRage(RageType.BANNER);
	}
}
