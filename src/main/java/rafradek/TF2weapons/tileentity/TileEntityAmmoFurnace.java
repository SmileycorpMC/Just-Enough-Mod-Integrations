package rafradek.TF2weapons.tileentity;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import rafradek.TF2weapons.block.BlockAmmoFurnace;
import rafradek.TF2weapons.inventory.ContainerAmmoFurnace;
import rafradek.TF2weapons.item.ItemAmmo;
import rafradek.TF2weapons.item.ItemFireAmmo;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;

public class TileEntityAmmoFurnace extends TileEntityLockable implements ITickable, ISidedInventory {
	private static final int[] SLOTS_TOP = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
	private static final int[] SLOTS_BOTTOM = new int[] { 10, 11, 12, 13, 14, 15, 16, 17, 18, 9 };
	private static final int[] SLOTS_SIDES = new int[] { 9 };
	/**
	 * The ItemStacks that hold the items currently being used in the furnace
	 */
	private NonNullList<ItemStack> furnaceItemStacks = NonNullList.withSize(19,ItemStack.EMPTY);
	/** The number of ticks that the furnace will keep burning */
	private int furnaceBurnTime;
	/**
	 * The number of ticks that a fresh copy of the currently-burning item would
	 * keep the furnace burning for
	 */
	private int currentItemBurnTime;
	private int cookTime;
	private int totalCookTime;
	private String furnaceCustomName;
	private int ammoSmeltType;

	/**
	 * Returns the number of slots in the inventory.
	 */
	@Override
	public int getSizeInventory() {
		return this.furnaceItemStacks.size();
	}

	/**
	 * Returns the stack in the given slot.
	 */
	@Override
	@Nullable
	public ItemStack getStackInSlot(int index) {
		return this.furnaceItemStacks.get(index);
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and
	 * returns them in a new stack.
	 */
	@Override
	@Nullable
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(this.furnaceItemStacks, index, count);
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 */
	@Override
	@Nullable
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(this.furnaceItemStacks, index);
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be
	 * crafting or armor sections).
	 */
	@Override
	public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
		boolean flag = !stack.isEmpty() && stack.isItemEqual(this.furnaceItemStacks.get(index))
				&& ItemStack.areItemStackTagsEqual(stack, this.furnaceItemStacks.get(index));
		this.furnaceItemStacks.set(index, stack);

		if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit())
			stack.setCount( this.getInventoryStackLimit());

		if (index < 9 && !flag) {
			this.totalCookTime = this.getCookTime(stack);
			this.cookTime = 0;
			this.markDirty();
		}
	}

	/**
	 * Get the name of this object. For players this returns their username
	 */
	@Override
	public String getName() {
		return this.hasCustomName() ? this.furnaceCustomName : "container.ammofurnace";
	}

	/**
	 * Returns true if this thing is named
	 */
	@Override
	public boolean hasCustomName() {
		return this.furnaceCustomName != null && !this.furnaceCustomName.isEmpty();
	}

	public void setCustomInventoryName(String p_145951_1_) {
		this.furnaceCustomName = p_145951_1_;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		//NBTTagList nbttaglist = compound.getTagList("Items", 10);
		ItemStackHelper.loadAllItems(compound, furnaceItemStacks);
		/*this.furnaceItemStacks = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot");

			if (j >= 0 && j < this.furnaceItemStacks.length)
				this.furnaceItemStacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
		}*/

		this.furnaceBurnTime = compound.getInteger("BurnTime");
		this.cookTime = compound.getInteger("CookTime");
		this.totalCookTime = compound.getInteger("CookTimeTotal");
		this.currentItemBurnTime = getItemBurnTime(this.furnaceItemStacks.get(1));

		if (compound.hasKey("CustomName", 8))
			this.furnaceCustomName = compound.getString("CustomName");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("BurnTime", this.furnaceBurnTime);
		compound.setInteger("CookTime", this.cookTime);
		compound.setInteger("CookTimeTotal", this.totalCookTime);
		ItemStackHelper.saveAllItems(compound, furnaceItemStacks);
		/*NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.furnaceItemStacks.length; ++i)
			if (this.furnaceItemStacks[i] != null) {
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte) i);
				this.furnaceItemStacks[i].writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}

		compound.setTag("Items", nbttaglist);*/

		if (this.hasCustomName())
			compound.setString("CustomName", this.furnaceCustomName);

		return compound;
	}

	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be
	 * 64, possibly will be extended.
	 */
	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	/**
	 * Furnace isBurning
	 */
	public boolean isBurning() {
		return this.furnaceBurnTime > 0;
	}

	@SideOnly(Side.CLIENT)
	public static boolean isBurning(IInventory inventory) {
		return inventory.getField(0) > 0;
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	@Override
	public void update() {
		boolean flag = this.isBurning();
		boolean flag1 = false;

		if (this.isBurning())
			--this.furnaceBurnTime;

		if (!this.world.isRemote) {
			if (this.isBurning() || this.furnaceItemStacks.get(9) != null) {
				if (!this.isBurning() && this.canSmelt()) {
					this.furnaceBurnTime = getItemBurnTime(this.furnaceItemStacks.get(9));
					this.currentItemBurnTime = this.furnaceBurnTime;

					if (this.isBurning()) {
						flag1 = true;

						if (this.furnaceItemStacks.get(9) != null) {
							this.furnaceItemStacks.get(9).shrink(1);;

							if (this.furnaceItemStacks.get(9).getCount() ==0)
								this.furnaceItemStacks.set(9, furnaceItemStacks.get(9).getItem()
										.getContainerItem(furnaceItemStacks.get(9)));
						}
					}
				}

				if (this.isBurning() && this.canSmelt()) {
					++this.cookTime;

					if (this.cookTime == this.totalCookTime) {
						this.cookTime = 0;
						this.totalCookTime = this.getCookTime(this.furnaceItemStacks.get(0));
						this.smeltItem();
						flag1 = true;
					}
				} else
					this.cookTime = 0;
			} else if (!this.isBurning() && this.cookTime > 0)
				this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.totalCookTime);

			if (flag != this.isBurning()) {
				flag1 = true;
				BlockAmmoFurnace.setState(this.isBurning(), this.world, this.pos);
			}
		}

		if (flag1)
			this.markDirty();
	}

	public int getCookTime(@Nullable ItemStack stack) {
		return 200;
	}

	/**
	 * Returns true if the furnace can smelt an item, i.e. has a source item,
	 * destination stack isn't full, etc.
	 */
	private boolean canSmelt() {
		int[] ammoTypesCount = new int[ItemAmmo.AMMO_TYPES.length];
		for (int i = 0; i < 9; i++) {
			ItemStack base = this.furnaceItemStacks.get(i);
			// System.out.println("Base: "+i+" "+base+"
			// "+ItemAmmo.AMMO_RECIPES[base.getItemDamage()]);
			if (base != null && base.getMetadata() < TF2CraftingManager.AMMO_RECIPES.length
					&& base.getItem() instanceof ItemAmmo && !(base.getItem() instanceof ItemFireAmmo)
					&& TF2CraftingManager.AMMO_RECIPES[base.getItemDamage()] != null) {
				/*
				 * &&
				 * base.getCount()>=ItemAmmo.AMMO_RECIPES[base.getItemDamage()].
				 * getRecipeOutput().getCount()){
				 */
				ammoTypesCount[base.getItemDamage()] += base.getCount();
				if (ammoTypesCount[base.getItemDamage()] >= MathHelper.ceil(
						TF2CraftingManager.AMMO_RECIPES[base.getItemDamage()].getRecipeOutput().getCount() * 1.2f)) {
					this.ammoSmeltType = base.getItemDamage();
					return true;
				}
			}
			/*
			 * ItemStack itemstack =
			 * FurnaceRecipes.instance().getSmeltingResult(this.
			 * furnaceItemStacks[0]); if (itemstack == null) return false; if
			 * (this.furnaceItemStacks[2] == null) return true; if
			 * (!this.furnaceItemStacks[2].isItemEqual(itemstack)) return false;
			 * int result = furnaceItemStacks[2].getCount() +
			 * itemstack.getCount(); return result <= getInventoryStackLimit() &&
			 * result <= this.furnaceItemStacks[2].getMaxStackSize();
			 */// Forge BugFix: Make it respect stack sizes properly.
		}
		return false;
	}

	/**
	 * Turn one item from the furnace source stack into the appropriate smelted
	 * item in the furnace result stack
	 */
	public void smeltItem() {
		int ammoToConsume = MathHelper.ceil(
				TF2CraftingManager.AMMO_RECIPES[this.ammoSmeltType].getRecipeOutput().getCount() * 1.2f);
		for (int i = 0; i < 9; i++) {
			ItemStack base = this.furnaceItemStacks.get(i);
			if (base != null && base.getItem() instanceof ItemAmmo && base.getItemDamage() == this.ammoSmeltType) {

				ShapelessOreRecipe recipe = TF2CraftingManager.AMMO_RECIPES[base.getItemDamage()];
				int ammoConsumed = Math.min(base.getCount(), ammoToConsume);
				base.shrink(ammoConsumed);
				ammoToConsume -= ammoConsumed;
				if (base.getCount() <= 0)
					this.setInventorySlotContents(i, ItemStack.EMPTY);

				if (ammoToConsume <= 0) {
					for (Ingredient obj : recipe.getIngredients()) {

						ItemStack out=obj.getMatchingStacks()[0];
						for (int j = 10; j < 19; j++) {
							boolean handled = false;
							ItemStack inSlot = this.getStackInSlot(j);
							if (inSlot.isEmpty()) {
								this.setInventorySlotContents(j, out.copy());
								handled = true;
							} else if (out.isItemEqual(inSlot) && ItemStack.areItemStackTagsEqual(out, inSlot)) {
								int size = out.getCount() + inSlot.getCount();

								if (size <= out.getMaxStackSize()) {
									inSlot.setCount( size);
									handled = true;
								}
							}
							if (handled)
								break;
						}
					}
					return;
				}
			}
		}
		/*
		 * ItemStack itemstack =
		 * FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks[0]
		 * );
		 *
		 * if (this.furnaceItemStacks[2] == null) { this.furnaceItemStacks[2] =
		 * itemstack.copy(); } else if (this.furnaceItemStacks[2].getItem() ==
		 * itemstack.getItem()) { this.furnaceItemStacks[2].getCount() +=
		 * itemstack.getCount(); // Forge BugFix: Results may have multiple items
		 * }
		 *
		 * if (this.furnaceItemStacks[0].getItem() ==
		 * Item.getItemFromBlock(Blocks.SPONGE) &&
		 * this.furnaceItemStacks[0].getMetadata() == 1 &&
		 * this.furnaceItemStacks[1] != null &&
		 * this.furnaceItemStacks[1].getItem() == Items.BUCKET) {
		 * this.furnaceItemStacks[1] = new ItemStack(Items.WATER_BUCKET); }
		 *
		 * --this.furnaceItemStacks[0].getCount();
		 *
		 * if (this.furnaceItemStacks[0].getCount() <= 0) {
		 * this.furnaceItemStacks[0] = null; }
		 */
	}

	/**
	 * Returns the number of ticks that the supplied fuel item will keep the
	 * furnace burning, or 0 if the item isn't fuel
	 */
	@SuppressWarnings("deprecation")
	public static int getItemBurnTime(ItemStack stack) {
		if (stack.isEmpty())
			return 0;
		else {
			Item item = stack.getItem();

			if (item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.AIR) {
				Block block = Block.getBlockFromItem(item);

				if (block == Blocks.WOODEN_SLAB)
					return 150;

				if (block.getDefaultState().getMaterial() == Material.WOOD)
					return 300;

				if (block == Blocks.COAL_BLOCK)
					return 16000;
			}

			if (item instanceof ItemTool && "WOOD".equals(((ItemTool) item).getToolMaterialName()))
				return 200;
			if (item instanceof ItemSword && "WOOD".equals(((ItemSword) item).getToolMaterialName()))
				return 200;
			if (item instanceof ItemHoe && "WOOD".equals(((ItemHoe) item).getMaterialName()))
				return 200;
			if (item == Items.STICK)
				return 100;
			if (item == Items.COAL)
				return 1600;
			if (item == Items.LAVA_BUCKET)
				return 20000;
			if (item == Item.getItemFromBlock(Blocks.SAPLING))
				return 100;
			if (item == Items.BLAZE_ROD)
				return 2400;
			return net.minecraftforge.fml.common.registry.GameRegistry.getFuelValue(stack);
		}
	}

	public static boolean isItemFuel(ItemStack stack) {
		/**
		 * Returns the number of ticks that the supplied fuel item will keep the
		 * furnace burning, or 0 if the item isn't fuel
		 */
		return getItemBurnTime(stack) > 0;
	}

	/**
	 * Do not make give this method the name canInteractWith because it clashes
	 * with Container
	 */
	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return this.world.getTileEntity(this.pos) != this ? false
				: player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring
	 * stack size) into the given slot.
	 */
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index > 9)
			return false;
		else if (index != 9)
			return true;
		else {
			ItemStack itemstack = this.furnaceItemStacks.get(9);
			return isItemFuel(stack)
					|| SlotFurnaceFuel.isBucket(stack) && (itemstack.isEmpty() || itemstack.getItem() != Items.BUCKET);
		}
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return side == EnumFacing.DOWN ? SLOTS_BOTTOM : (side == EnumFacing.UP ? SLOTS_TOP : SLOTS_SIDES);
	}

	/**
	 * Returns true if automation can insert the given item in the given slot
	 * from the given side.
	 */
	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return this.isItemValidForSlot(index, itemStackIn);
	}

	/**
	 * Returns true if automation can extract the given item in the given slot
	 * from the given side.
	 */
	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		if (direction == EnumFacing.DOWN && index == 9) {
			Item item = stack.getItem();

			if (item != Items.WATER_BUCKET && item != Items.BUCKET)
				return false;
		}

		return true;
	}

	@Override
	public String getGuiID() {
		return "minecraft:furnace";
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		return new ContainerAmmoFurnace(playerInventory, this);
	}

	@Override
	public int getField(int id) {
		switch (id) {
		case 0:
			return this.furnaceBurnTime;
		case 1:
			return this.currentItemBurnTime;
		case 2:
			return this.cookTime;
		case 3:
			return this.totalCookTime;
		default:
			return 0;
		}
	}

	@Override
	public void setField(int id, int value) {
		switch (id) {
		case 0:
			this.furnaceBurnTime = value;
			break;
		case 1:
			this.currentItemBurnTime = value;
			break;
		case 2:
			this.cookTime = value;
			break;
		case 3:
			this.totalCookTime = value;
		}
	}

	@Override
	public int getFieldCount() {
		return 4;
	}

	@Override
	public void clear() {
		for (int i = 0; i < this.furnaceItemStacks.size(); ++i)
			this.furnaceItemStacks.set(9 ,ItemStack.EMPTY);
	}

	net.minecraftforge.items.IItemHandler handlerTop = new net.minecraftforge.items.wrapper.SidedInvWrapper(this,
			net.minecraft.util.EnumFacing.UP);
	net.minecraftforge.items.IItemHandler handlerBottom = new net.minecraftforge.items.wrapper.SidedInvWrapper(this,
			net.minecraft.util.EnumFacing.DOWN);
	net.minecraftforge.items.IItemHandler handlerSide = new net.minecraftforge.items.wrapper.SidedInvWrapper(this,
			net.minecraft.util.EnumFacing.WEST);

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability,
			net.minecraft.util.EnumFacing facing) {
		if (facing != null && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			if (facing == EnumFacing.DOWN)
				return (T) handlerBottom;
			else if (facing == EnumFacing.UP)
				return (T) handlerTop;
			else
				return (T) handlerSide;
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.furnaceItemStacks)
		{
			if (!itemstack.isEmpty())
			{
				return false;
			}
		}

		return true;
	}
}
