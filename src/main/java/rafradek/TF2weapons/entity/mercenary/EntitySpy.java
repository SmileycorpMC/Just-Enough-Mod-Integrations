package rafradek.TF2weapons.entity.mercenary;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.entity.ai.EntityAIAmbush;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.item.ItemCloak;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.TF2Util;

public class EntitySpy extends EntityTF2Character {

	public int weaponCounter;
	public int cloakCounter;

	public float prevHealth;
	public EntitySpy(World p_i1738_1_) {
		super(p_i1738_1_);
		this.experienceValue = 15;
		this.rotation = 20;
		this.tasks.addTask(2, new EntityAIAvoidEntity<>(this, EntityLivingBase.class, entity -> {
			return entity == this.getAttackTarget() && TF2Util.lookingAtFast(entity, 105, this.posX, this.posY, this.posZ);
		}, 4, 1.0f, 1.0f));
		this.tasks.addTask(3, new EntityAIAmbush(this));
		this.tasks.removeTask(avoidSentry);
		if (this.attack != null) {
			attack.setRange(30f);
			this.tasks.addTask(4, this.attack);
		}
		this.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks = 20;
		if(!this.world.isRemote) {
			this.getWepCapability().setInvisible(true);
			this.getWepCapability().setDisguised(true);
			this.getWepCapability().setDisguiseType("T:Engineer");
		}
	}

	@Override
	public float[] getDropChance() {
		return new float[] { 0.1f, 0.7f, 0.08f, 0.05f};
	}

	@Override
	protected ResourceLocation getLootTable() {
		return TF2weapons.lootSpy;
	}

	@Override
	public int getTalkInterval() {
		return 80;
	}

	public boolean hasWatch() {
		return this.loadout.getStackInSlot(3).getItem() instanceof ItemCloak;
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!this.world.isRemote) {
			if (this.ticksExisted == 0) {
				this.getWepCapability().setInvisible(this.hasWatch());
			}
			this.prevHealth=this.getHealth();

			this.cloakCounter--;
			EntityLivingBase target = this.getAttackTarget();
			if (this.hasWatch()) {
				if (target != null && this.loadout.getStackInSlot(3).getTagCompound().getBoolean("Active")) {
					boolean useKnife = false;
					if ((this.getRevengeTarget() != null && this.ticksExisted - this.getRevengeTimer() < 45)
							|| (useKnife = (this.getDistanceSq(target) < (TF2Util.lookingAtFast(target, 105, this.posX, this.posY, this.posZ) ? 6 : 40)))) {

						((ItemCloak) this.loadout.getStackInSlot(3).getItem()).setCloak(
								!this.getWepCapability().isInvisible(), this.loadout.getStackInSlot(3), this,
								this.world);
						if (useKnife) {
							this.weaponCounter = 19;
							this.setCombatTask(false);
							this.cloakCounter = 36;
						} else
							this.cloakCounter = 20 + (int) ((16 - this.getDistance(target)) * 10);
					}
					/*
					 * float x = -MathHelper.sin(target.rotationYaw / 180.0F *
					 * (float)Math.PI); float z = MathHelper.cos(target.rotationYaw
					 * / 180.0F * (float)Math.PI);
					 * this.setPosition(target.posX-x,target.posY,target.posZ-z);
					 */
				}

				//System.out.println(this.loadout.getStackInSlot(3));
				if (this.cloakCounter <= 0 && !this.loadout.getStackInSlot(3).getTagCompound().getBoolean("Active"))
					((ItemCloak) this.loadout.getStackInSlot(3).getItem()).setCloak(true, this.loadout.getStackInSlot(3), this, this.world);
			}
			this.weaponCounter--;
			if (this.weaponCounter <= 0 && this.getAttackTarget() != null
					&& this.getDistanceSq(this.getAttackTarget()) < (TF2Util.lookingAtFast(target, 105, this.posX, this.posY, this.posZ) ? 4 : 25)) {
				this.setCombatTask(false);
				this.weaponCounter = 8;
			} else if (this.weaponCounter <= 0 && this.getHeldItemMainhand() == this.loadout.getStackInSlot(2)) {
				this.setCombatTask(true);
				this.weaponCounter = 3;
			}
			if (this.getAttackTarget() != null && this.getAttackTarget() instanceof EntityBuilding
					&& ((EntityBuilding) this.getAttackTarget()).isSapped()
					&& ((EntityBuilding) this.getAttackTarget()).getOwner() != null)
				this.setAttackTarget(((EntityBuilding) this.getAttackTarget()).getOwner());
		}
	}

	@Override
	public int[] getValidSlots() {
		return new int[] { 0, 1, 2, 3 };
	}

	@Override
	protected void addWeapons() {
		super.addWeapons();
		this.loadout.setStackInSlot(1,ItemFromData.getRandomWeaponOfSlotMob("spy", 1, this.rand, true, true, this.noEquipment));
		this.loadout.getStackInSlot(1).setCount( 64);
	}

	@Override
	public String getName() {
		if(this.world.isRemote && ClientProxy.getLocalPlayer() != null && this.getWepCapability().isDisguised()) {
			String username=this.getWepCapability().getDisguiseType().substring(2);

			if(TF2Util.isOnSameTeam(ClientProxy.getLocalPlayer(), this)) {
				return super.getName()+" ["+username+"]";
			}
			else {
				if(this.getWepCapability().getDisguiseType().startsWith("M:")) {
					return TextFormatting.RESET+I18n.format("entity."+username+".name");
				}
				else
					return ScorePlayerTeam.formatPlayerName(Minecraft.getMinecraft().world.getScoreboard().getPlayersTeam(username), username);
			}
		}
		else
			return super.getName();
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TF2Sounds.MOB_SPY_SAY;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TF2Sounds.MOB_SPY_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_SPY_DEATH;
	}

	/**
	 * Plays step sound at given x, y, z for the entity
	 */
	public void setCombatTask(boolean ranged) {
		this.ranged = ranged;
		if (ranged) {

			this.switchSlot(0);
			this.attack.setRange(30);
		} else if (this.getAttackTarget() instanceof EntityBuilding) {
			this.switchSlot(1);
			this.attack.setRange(1.9f);
		} else {
			this.switchSlot(2);
			this.attack.setRange(2.2f);
		}
	}

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
		if (this.rand.nextFloat() < 0.085f + p_70628_2_ * 0.05f)
			this.entityDropItem(ItemFromData.getNewStack("revolver"), 0);
		if (this.rand.nextFloat() < 0.06f + p_70628_2_ * 0.025f)
			this.entityDropItem(ItemFromData.getNewStack("butterflyknife"), 0);
		if (this.rand.nextFloat() < 0.05f + p_70628_2_ * 0.025f)
			this.entityDropItem(ItemFromData.getNewStack("cloak"), 0);
		if (this.rand.nextFloat() < 0.05f + p_70628_2_ * 0.025f)
			this.entityDropItem(new ItemStack(TF2weapons.itemDisguiseKit), 0);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12.5D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.1D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.14111D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
	}

	@Override
	public int getClassIndex() {
		return 8;
	}

	@Override
	public boolean canBecomeGiant() {
		return false;
	}

	@Override
	public boolean isValidTarget(EntityLivingBase living) {
		return super.isValidTarget(living) && !(living instanceof EntityBuilding && ((EntityBuilding)living).isSapped());
	}
}
