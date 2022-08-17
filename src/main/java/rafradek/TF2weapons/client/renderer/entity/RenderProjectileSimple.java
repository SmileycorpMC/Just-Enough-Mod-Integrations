package rafradek.TF2weapons.client.renderer.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.model.ModelSyringe;
import rafradek.TF2weapons.entity.projectile.EntityProjectileSimple;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.TF2Util;

public class RenderProjectileSimple extends Render<EntityProjectileSimple> {

	private ModelBase model;
	private static final ResourceLocation texturered = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/projectile/syringered.png");
	private static final ResourceLocation textureblu = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/projectile/syringeblu.png");
	private static final ResourceLocation arrow = new ResourceLocation(
			"textures/entity/projectiles/arrow.png");
	
	public RenderProjectileSimple(RenderManager manager) {
		super(manager);
		// we could have initialized it above, but here is fine as well:
		model = new ModelSyringe();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityProjectileSimple entity) {

		if (entity.getType() == 3)
			return arrow;
		return TF2Util.getTeamForDisplay(entity.shootingEntity) == 0 ? texturered : textureblu;
	}

	@Override
	public void doRender(EntityProjectileSimple entity, double x, double y, double z, float yaw, float partialTick) {
		if (entity.getType() == 4)
			return;
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);
		GlStateManager.color(0.7F, 0.7F, 0.7F, 1F);
		if(entity.getType() != 2 && entity.getType() != 3)
			GL11.glScalef(0.25f, 0.25f, 0.25f);
		if (entity.getType() != 3) {
		GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTick, 0.0F, 1.0F,
				0.0F);
		GL11.glRotatef(
				(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick) * -1, 1.0F,
				0.0F, 0.0F);
		}

		bindEntityTexture(entity);

		
		// GL11.glTranslatef((float)entity.posX, (float)entity.posY,
		// entity.posZ);
		if (entity.getCritical() == 2)
		GlStateManager.disableLighting();
	
		// GlStateManager.color(0.0F, 0.0F, 1.0F, 1F);
		if(entity.getType() == 2)
			Minecraft.getMinecraft().getRenderItem().renderItem(ItemFromData.getNewStack("cleaver"), ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
		else if (entity.getType() == 3) {
	        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	        GlStateManager.pushMatrix();
	        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTick - 90.0F, 0.0F, 1.0F, 0.0F);
	        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick, 0.0F, 0.0F, 1.0F);
	        Tessellator tessellator = Tessellator.getInstance();
	        BufferBuilder bufferbuilder = tessellator.getBuffer();
	        GlStateManager.enableRescaleNormal();
	        float f9 = 0;

	        if (f9 > 0.0F)
	        {
	            float f10 = -MathHelper.sin(f9 * 3.0F) * f9;
	            GlStateManager.rotate(f10, 0.0F, 0.0F, 1.0F);
	        }

	        GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
	        GlStateManager.scale(0.05625F, 0.05625F, 0.05625F);
	        GlStateManager.translate(-4.0F, 0.0F, 0.0F);

	        if (this.renderOutlines)
	        {
	            GlStateManager.enableColorMaterial();
	            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
	        }

	        GlStateManager.glNormal3f(0.05625F, 0.0F, 0.0F);
	        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
	        bufferbuilder.pos(-7.0D, -2.0D, -2.0D).tex(0.0D, 0.15625D).endVertex();
	        bufferbuilder.pos(-7.0D, -2.0D, 2.0D).tex(0.15625D, 0.15625D).endVertex();
	        bufferbuilder.pos(-7.0D, 2.0D, 2.0D).tex(0.15625D, 0.3125D).endVertex();
	        bufferbuilder.pos(-7.0D, 2.0D, -2.0D).tex(0.0D, 0.3125D).endVertex();
	        tessellator.draw();
	        GlStateManager.glNormal3f(-0.05625F, 0.0F, 0.0F);
	        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
	        bufferbuilder.pos(-7.0D, 2.0D, -2.0D).tex(0.0D, 0.15625D).endVertex();
	        bufferbuilder.pos(-7.0D, 2.0D, 2.0D).tex(0.15625D, 0.15625D).endVertex();
	        bufferbuilder.pos(-7.0D, -2.0D, 2.0D).tex(0.15625D, 0.3125D).endVertex();
	        bufferbuilder.pos(-7.0D, -2.0D, -2.0D).tex(0.0D, 0.3125D).endVertex();
	        tessellator.draw();

	        for (int j = 0; j < 4; ++j)
	        {
	            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
	            GlStateManager.glNormal3f(0.0F, 0.0F, 0.05625F);
	            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
	            bufferbuilder.pos(-8.0D, -2.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
	            bufferbuilder.pos(8.0D, -2.0D, 0.0D).tex(0.5D, 0.0D).endVertex();
	            bufferbuilder.pos(8.0D, 2.0D, 0.0D).tex(0.5D, 0.15625D).endVertex();
	            bufferbuilder.pos(-8.0D, 2.0D, 0.0D).tex(0.0D, 0.15625D).endVertex();
	            tessellator.draw();
	        }

	        if (this.renderOutlines)
	        {
	            GlStateManager.disableOutlineMode();
	            GlStateManager.disableColorMaterial();
	        }

	        GlStateManager.disableRescaleNormal();
	        GlStateManager.popMatrix();
		}
		else if(entity.getType() == 8) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(2, 2, 2);
			Minecraft.getMinecraft().getRenderItem().renderItem(ItemFromData.getNewStack("hhhaxe"), ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
			GlStateManager.popMatrix();
		}
		else	
			model.render(entity, 0F, 0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		// GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
		// GlStateManager.enableTexture2D();
		if (entity.getCritical() == 2)
			GlStateManager.disableLighting();
		GlStateManager.enableLighting();
		
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
		/*
		 * GL11.glScalef(1.5f, 1.5f, 1.5f); GL11.glEnable(GL11.GL_BLEND);
		 * //GL11.glDisable(GL11.GL_ALPHA_TEST); OpenGlHelper.glBlendFunc(770,
		 * 771, 1, 0);
		 * 
		 * char c0 = 61680; int j = c0 % 65536; int k = c0 / 65536;
		 * OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
		 * (float)j / 1.0F, (float)k / 1.0F);
		 * 
		 * model.render(entity, 0F, 0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		 * GL11.glDisable(GL11.GL_BLEND); // GL11.glEnable(GL11.GL_ALPHA_TEST);
		 */
		GL11.glPopMatrix();
		/*
		 * IIcon iicon = TF2EventBusListener.pelletIcon;
		 * 
		 * if (iicon != null) { GL11.glPushMatrix();
		 * GL11.glTranslatef((float)x,(float) y,(float) z);
		 * GL11.glEnable(GL12.GL_RESCALE_NORMAL); GL11.glScalef(0.5F, 0.5F,
		 * 0.5F); this.bindTexture(TextureMap.locationItemsTexture); Tessellator
		 * tessellator = Tessellator.instance;
		 * 
		 * float f = iicon.getMinU(); float f1 = iicon.getMaxU(); float f2 =
		 * iicon.getMinV(); float f3 = iicon.getMaxV(); float f4 = 1.0F; float
		 * f5 = 0.5F; float f6 = 0.25F; GL11.glRotatef(180.0F -
		 * this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		 * GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		 * tessellator.startDrawingQuads(); tessellator.setNormal(0.0F, 1.0F,
		 * 0.0F); tessellator.addVertexWithUV((double)(0.0F - f5), (double)(0.0F
		 * - f6), 0.0D, (double)f, (double)f3);
		 * tessellator.addVertexWithUV((double)(f4 - f5), (double)(0.0F - f6),
		 * 0.0D, (double)f1, (double)f3);
		 * tessellator.addVertexWithUV((double)(f4 - f5), (double)(f4 - f6),
		 * 0.0D, (double)f1, (double)f2);
		 * tessellator.addVertexWithUV((double)(0.0F - f5), (double)(f4 - f6),
		 * 0.0D, (double)f, (double)f2); tessellator.draw();
		 * GL11.glDisable(GL12.GL_RESCALE_NORMAL); GL11.glPopMatrix();
		 * 
		 * }
		 */

	}

}
