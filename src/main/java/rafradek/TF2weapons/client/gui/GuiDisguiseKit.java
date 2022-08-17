package rafradek.TF2weapons.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.mercenary.EntitySpy;
import rafradek.TF2weapons.message.TF2Message;

public class GuiDisguiseKit extends GuiScreen {

	public GuiButton playerDisguise;
	private GuiTextField playerNameField;
	public EntityPlayer player;
	public ArrayList<EntityLivingBase> mobList = new ArrayList<>();
	public boolean needUpdating;
	public int ticksUpdate;
	public int firstIndex;
	public float scroll;
	private boolean isScrolling;
	private boolean wasClicking;

	private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/container/cabinet.png");

	public GuiDisguiseKit() {

	}

	@Override
	public void initGui() {
		player = new EntityOtherPlayerMP(this.mc.world, new GameProfile(mc.player.getUniqueID(), "name"));
		for(ResourceLocation entry:ForgeRegistries.ENTITIES.getKeys()) {
			Entity entity=EntityList.createEntityByIDFromName(entry, this.mc.world);
			if(entity instanceof EntityLivingBase && ((entity.width + entity.height < 6 && entity.isNonBoss())||this.mc.player.capabilities.isCreativeMode)) {
				mobList.add((EntityLivingBase) entity);
				if(entity instanceof EntitySpy) {
					entity.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks=0;
				}
			}
		}
		Collections.sort(mobList, new Comparator<EntityLivingBase>() {

			@Override
			public int compare(EntityLivingBase o1, EntityLivingBase o2) {
				return EntityList.getKey(o2).toString().compareTo(EntityList.getKey(o1).toString());
			}

		});
		Keyboard.enableRepeatEvents(true);
		this.playerNameField = new GuiTextField(6, this.fontRenderer, this.width / 2 + 26, this.height / 2 + 60, 108,
				19);
		this.playerNameField.setMaxStringLength(64);
		this.playerNameField.setFocused(true);
		this.buttonList.clear();
		for(int i=0;i<16;i++) {
			this.buttonList.add(new GuiButton(i, this.width / 2 - 135 + (i%2) * 70, this.height / 2 - 60+i/2*20, 70, 20,
					I18n.format(EntityList.getTranslationName(EntityList.getKey(EntityZombie.class)), new Object[0])));
		}
		this.buttonList
		.add(playerDisguise = new GuiButton(30, this.width / 2 + 25, this.height / 2 + 80, 110, 20, "Player"));
		this.setButtons();

	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			if (button.id == 30)
				TF2weapons.network.sendToServer(new TF2Message.DisguiseMessage("P:" + playerNameField.getText()));
			if (button.id < 16) {
				TF2weapons.network.sendToServer(new TF2Message.DisguiseMessage("M:"+ EntityList.getKey(mobList.get(button.id+this.firstIndex)).toString()));
			}
			this.mc.displayGuiScreen(null);
		}
	}

	public void setButtons() {

		for (int i = 0; i < 16; i++) {
			if(i+firstIndex<mobList.size()) {
				this.buttonList.get(i).displayString=mobList.get(i+firstIndex).getName();
				this.buttonList.get(i).visible=true;
			}
			else
				this.buttonList.get(i).visible=false;
		}
	}
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.playerNameField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		this.playerNameField.textboxKeyTyped(typedChar, keyCode);
		if (playerNameField.isFocused())
			this.needUpdating=true;

	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, I18n.format("gui.disguise.info", new Object[0]),
				this.width / 2 - 5, 20, 16777215);
		/*for (int i = 0; i < this.mobs.length; i++)
			drawEntityOnScreen(this.width / 2 - 105 + (i % 4) * 70, this.height / 2 - 26 + 110 * (i / 4), 35, mobs[i]);

		drawEntityOnScreen(this.width / 2 + 105, this.height / 2 + 66, 35, player);*/
		int entdraw= -1;
		for(int i=0;i<16;i++) {
			if(i+firstIndex<this.mobList.size() && this.buttonList.get(i).isMouseOver()) {
				entdraw=i+this.firstIndex;
				break;
			}
		}
		if (entdraw == -1)
			drawEntityOnScreen(this.width / 2 + 75, this.height / 2 + 40, 35, player);
		else
			drawEntityOnScreen(this.width / 2 + 75, this.height / 2 + 40, 35, this.mobList.get(entdraw));
		boolean flag = Mouse.isButtonDown(0);
		int i = this.width / 2;
		int j = this.height / 2;
		int k = i + 5;
		int l = j - 60;
		int i1 = k + 14;
		int j1 = l + 160;

		if (!this.wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1)
			this.isScrolling = true;

		if (!flag)
			this.isScrolling = false;

		this.wasClicking = flag;

		if (this.isScrolling) {
			int size = this.mobList.size();
			this.scroll = (mouseY - l - 7.5F) / (j1 - l - 15.0F);
			this.scroll = MathHelper.clamp(this.scroll, 0.0F, 1.0F);
			int rows=-(-this.mobList.size()/2)-7;
			this.firstIndex = Math.round(this.scroll * rows) * 2;
			this.setButtons();
		}
		this.mc.getTextureManager().bindTexture(CRAFTING_TABLE_GUI_TEXTURES);

		k = this.width / 2 + 6;
		l = this.height / 2 - 59;
		i = l + 160;

		this.drawTexturedModalRect(k, l + (int) ((i - l - 17) * this.scroll), 232, 0, 12, 15);

		super.drawScreen(mouseX, mouseY, partialTicks);
		this.playerNameField.drawTextBox();



	}

	public static void drawEntityOnScreen(int posX, int posY, int scale, EntityLivingBase ent) {
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate(posX, posY, 50.0F);
		GlStateManager.scale((-scale), scale, scale);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		float f = ent.renderYawOffset;
		float f1 = ent.rotationYaw;
		float f2 = ent.rotationPitch;
		float f3 = ent.prevRotationYawHead;
		float f4 = ent.rotationYawHead;
		GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		ent.renderYawOffset = 0;
		ent.rotationYaw = 0;
		ent.rotationPitch = 0;
		ent.rotationYawHead = ent.rotationYaw;
		ent.prevRotationYawHead = ent.rotationYaw;
		GlStateManager.translate(0.0F, 0.0F, 0.0F);
		RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
		rendermanager.setPlayerViewY(180.0F);
		rendermanager.setRenderShadow(false);
		rendermanager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
		rendermanager.setRenderShadow(true);
		ent.renderYawOffset = f;
		ent.rotationYaw = f1;
		ent.rotationPitch = f2;
		ent.prevRotationYawHead = f3;
		ent.rotationYawHead = f4;
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void updateScreen() {
		this.playerNameField.updateCursorCounter();
		if(--this.ticksUpdate<0&&this.needUpdating&&!StringUtils.isBlank(playerNameField.getText())){
			this.needUpdating=false;
			this.ticksUpdate=12;
			WeaponsCapability.THREAD_POOL.submit(new Runnable() {

				@Override
				public void run() {
					GameProfile profile = TileEntitySkull
							.updateGameprofile(new GameProfile(mc.player.getUniqueID(), playerNameField.getText()));

					player = new EntityOtherPlayerMP(mc.world, profile);
					WeaponsCapability.get(player).setDisguised(true);
					WeaponsCapability.get(player).setDisguiseType("P:"+playerNameField.getText());
					final WeaponsCapability cap = player.getCapability(TF2weapons.WEAPONS_CAP, null);
					cap.skinType = DefaultPlayerSkin.getSkinType(player.getUniqueID());
					if (profile.getId() != null)
						cap.skinType = DefaultPlayerSkin.getSkinType(profile.getId());
					Minecraft.getMinecraft().getSkinManager().loadProfileTextures(profile,
							new SkinManager.SkinAvailableCallback() {
						@Override
						public void skinAvailable(Type typeIn, ResourceLocation location,
								MinecraftProfileTexture profileTexture) {
							if (typeIn == Type.SKIN) {
								if (typeIn == Type.SKIN)
									cap.skinDisguise = location;
								cap.skinType = profileTexture.getMetadata("model");

								if (cap.skinType == null)
									cap.skinType = "default";
							}
						}
					}, false);
				}

			});
		}
		// this.playerNameField.updateCursorCounter();
	}
}
