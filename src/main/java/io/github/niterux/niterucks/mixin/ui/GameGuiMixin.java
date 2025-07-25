package io.github.niterux.niterucks.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.niterux.niterucks.Niterucks;
import io.github.niterux.niterucks.niterucksfeatures.ItemCoords;
import io.github.niterux.niterucks.niterucksfeatures.SimpleChatLoggingFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.HitResult;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import static io.github.niterux.niterucks.Niterucks.modVersion;

@Mixin(GameGui.class)
public class GameGuiMixin extends GuiElement {
	@Unique
	private static final String[] directions = {
		"South (Towards Positive Z)",
		"West (Towards Negative X)",
		"North (Towards Negative Z)",
		"East (Towards Positive X)"};
	@Unique
	private static ItemStack heldItem;
	@Unique
	private static Logger chatLogger = Logger.getLogger("io.github.niterux.niterucks.mixin.ui.GameGuiMixin");
	@Shadow
	private static ItemRenderer ITEM_RENDERER;

	static {
		chatLogger.setUseParentHandlers(false);
		ConsoleHandler simpleChatLoggingHandler = new ConsoleHandler();
		simpleChatLoggingHandler.setFormatter(new SimpleChatLoggingFormatter());
		chatLogger.addHandler(simpleChatLoggingHandler);
	}

	@Unique
	double hotbarHeight;
	@Shadow
	private Minecraft minecraft;

	//hotbar screen safety
	@Inject(method = "render", at = @At("HEAD"))
	private void getHotbarHeight(float screenOpen, boolean mouseX, int mouseY, int par4, CallbackInfo ci) {
		hotbarHeight = Niterucks.CONFIG.hotbarScreenSafety.get();
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/living/player/InputPlayerEntity;m_1513977()I", ordinal = 0))
	private void moveHealthMatrixUp(float screenOpen, boolean mouseX, int mouseY, int par4, CallbackInfo ci) {
		GL11.glTranslated(0.0, -hotbarHeight, 0.0);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V", remap = false, ordinal = 1))
	private void moveHealthMatrixBackDown(float screenOpen, boolean mouseX, int mouseY, int par4, CallbackInfo ci) {
		GL11.glTranslated(0.0, hotbarHeight, 0.0);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V", remap = false, ordinal = 0))
	private void moveHotbarMatrixUp(float screenOpen, boolean mouseX, int mouseY, int par4, CallbackInfo ci) {
		GL11.glTranslated(0.0, -hotbarHeight, 0.0);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V", remap = false, ordinal = 1))
	private void moveHotbarMatrixBackDown(float screenOpen, boolean mouseX, int mouseY, int par4, CallbackInfo ci) {
		GL11.glTranslated(0.0, hotbarHeight, 0.0);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V", remap = false, ordinal = 0, shift = At.Shift.AFTER))
	private void moveItemsMatrixUp(float screenOpen, boolean mouseX, int mouseY, int par4, CallbackInfo ci) {
		GL11.glTranslated(0.0, -hotbarHeight, -50.0); //50 to item rendering over chat
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Lighting;turnOff()V", ordinal = 0))
	private void moveItemsMatrixBackDown(float screenOpen, boolean mouseX, int mouseY, int par4, CallbackInfo ci) {
		GL11.glTranslated(0.0, hotbarHeight, 50.0);
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;drawTexture(IIIIII)V", ordinal = 1), index = 5)
	private int fixSelectedSlotRendering(int height) {
		return 24;
	}

	//print chat message to console
	@Inject(method = "addChatMessage(Ljava/lang/String;)V", at = @At("HEAD"))
	private void printChatMessage(String text, CallbackInfo ci) {
		chatLogger.info(text);
	}

	//===============
	//f3 menu stuff
	//===============
	@ModifyExpressionValue(method = "render",
		at = @At(value = "CONSTANT", args = "stringValue=Minecraft Beta 1.7.3 ("))
	private String replaceGameName(String original) {
		return String.format("Niterucks Client %s (", modVersion);
	}

	@ModifyArg(
		method = "render", at =
	@At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/render/TextRenderer;drawWithShadow(Ljava/lang/String;III)V",
		ordinal = 0
	),
		slice =
		@Slice(
			from =
			@At(
				value = "INVOKE",
				ordinal = 0,
				target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V",
				remap = false
			)
		),
		index = 3
	)
	private int colorChangeVersion(int color) {
		return 0x9e87fe;
	}

	@ModifyArg(
		method = "render",
		at =
		@At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GameGui;drawString(Lnet/minecraft/client/render/TextRenderer;Ljava/lang/String;III)V"
		),
		slice =
		@Slice(
			from =
			@At(
				value = "INVOKE",
				ordinal = 2,
				target = "Lnet/minecraft/client/gui/GameGui;drawString(Lnet/minecraft/client/render/TextRenderer;Ljava/lang/String;III)V"
			)
		),
		index = 4
	)
	private int colorChangeCoords(int color) {
		return 0x6D86F2;
	}

	@ModifyArg(
		method = "render",
		at =
		@At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/TextRenderer;drawWithShadow(Ljava/lang/String;III)V"
		),
		slice =
		@Slice(
			from =
			@At(
				value = "INVOKE",
				ordinal = 1,
				target = "Lnet/minecraft/client/render/TextRenderer;drawWithShadow(Ljava/lang/String;III)V"
			),
			to =
			@At(
				value = "INVOKE",
				ordinal = 4,
				target = "Lnet/minecraft/client/render/TextRenderer;drawWithShadow(Ljava/lang/String;III)V"
			)
		),
		index = 3
	)
	private int colorChangeDebugData(int color) {
		return 0xAAAAAA;
	}

	@ModifyArg(
		method = "render",
		at =
		@At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GameGui;drawString(Lnet/minecraft/client/render/TextRenderer;Ljava/lang/String;III)V"),
		slice =
		@Slice(
			from =
			@At(
				value = "INVOKE",
				ordinal = 0,
				target = "Lnet/minecraft/client/gui/GameGui;drawString(Lnet/minecraft/client/render/TextRenderer;Ljava/lang/String;III)V"
			),
			to =
			@At(
				value = "INVOKE",
				ordinal = 1,
				target = "Lnet/minecraft/client/gui/GameGui;drawString(Lnet/minecraft/client/render/TextRenderer;Ljava/lang/String;III)V"
			)
		),
		index = 4
	)
	private int colorChangeMemory(int color) {
		return 0xd5312f;
	}

	@SuppressWarnings("SuspiciousNameCombination")
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;drawString(Lnet/minecraft/client/render/TextRenderer;Ljava/lang/String;III)V", ordinal = 0))
	private void addNewInfoText(float screenOpen, boolean mouseX, int mouseY, int par4, CallbackInfo ci, @Local(ordinal = 0) TextRenderer textRenderer, @Local(ordinal = 2) int width) {
		this.drawString(textRenderer, "For help: press F3 + Q", 2, 96, 0xd96e02);
		if (Niterucks.CONFIG.showSeed.get()) {
			this.drawString(textRenderer, "Seed: " + minecraft.world.getSeed(), 2, 104, 0xd96e02);
		}
		String biomeString = "Biome: " + minecraft.world.getBiomeSource().getBiome(MathHelper.floor(minecraft.player.x), MathHelper.floor(minecraft.player.z)).name;
		this.drawString(textRenderer, biomeString, width - textRenderer.getWidth(biomeString) - 2, 22, 0x46a848);
		String lightString = "Light: " + minecraft.world.getRawBrightness(MathHelper.floor(minecraft.player.x), MathHelper.floor(minecraft.player.y), MathHelper.floor(minecraft.player.z));
		this.drawString(textRenderer, lightString, width - textRenderer.getWidth(lightString) - 2, 32, 0x46a848);

		if (minecraft.crosshairTarget != null && minecraft.crosshairTarget.type != HitResult.Type.ENTITY) {
			int targetBlockID = minecraft.world.getBlock(minecraft.crosshairTarget.x, minecraft.crosshairTarget.y, minecraft.crosshairTarget.z);
			int targetBlockMetadata = minecraft.world.getBlockMetadata(minecraft.crosshairTarget.x, minecraft.crosshairTarget.y, minecraft.crosshairTarget.z);
			String blockString = "Block: " + targetBlockID + ":" + targetBlockMetadata;
			this.drawString(textRenderer, blockString, width - textRenderer.getWidth(blockString) - 2, 42, 0x46a848);
		}

		if (heldItem != null) {
			String itemString = "Item: " + heldItem.itemId + ":" + heldItem.getDamage();
			this.drawString(textRenderer, itemString, width - textRenderer.getWidth(itemString) - 2, 52, 0x46a848);
		}
	}

	@ModifyArg(
		method = "render",
		at =
		@At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GameGui;drawString(Lnet/minecraft/client/render/TextRenderer;Ljava/lang/String;III)V",
			ordinal = 5
		),
		slice =
		@Slice(
			from =
			@At(
				value = "INVOKE",
				ordinal = 0,
				target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V",
				remap = false
			)
		),
		index = 1
	)
	private String goodFaceDirection(String par2) {
		//todo, make the code less cursed
		int facingDir = (int) this.minecraft.player.yaw;
		int i;
		if (facingDir + 45 < 0) {
			i = ((facingDir - 45) / 90) % 4;
			return "Facing: " + directions[(i + 4) % 4];
		} else {
			i = ((facingDir + 45) / 90) % 4;
			return "Facing: " + directions[i];
		}
	}

	@ModifyExpressionValue(
		method = "render",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/entity/living/player/InputPlayerEntity;x:D",
			opcode = Opcodes.GETFIELD
		)
	)
	private double roundCoordsX(double x) {
		return truncate(x);
	}

	@ModifyExpressionValue(
		method = "render",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/entity/living/player/InputPlayerEntity;y:D",
			opcode = Opcodes.GETFIELD
		)
	)
	private double roundCoordsY(double y) {
		return truncate(y);
	}

	@ModifyExpressionValue(
		method = "render",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/entity/living/player/InputPlayerEntity;z:D",
			opcode = Opcodes.GETFIELD
		)
	)
	private double roundCoordsZ(double z) {
		return truncate(z);
	}

	@Unique
	private double truncate(double coord) {
		return (double) Math.round(coord * 1000) / 1000;
	}

	//===============
	//game hud stuff
	//===============
	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V",
			shift = At.Shift.AFTER,
			remap = false
		)
	)
	private void addDurability(float screenOpen, boolean mouseX, int mouseY, int par4, CallbackInfo ci, @Local(ordinal = 0) TextRenderer textRenderer, @Local(ordinal = 3) int height, @Local(ordinal = 2) int width) {
		heldItem = minecraft.player.inventory.getMainHandStack();
		if (heldItem != null) {
			if (heldItem.getMaxDamage() > 0) {
				String damageText = String.valueOf(heldItem.getMaxDamage() - heldItem.getDamage());
				this.drawCenteredString(textRenderer, damageText, width / 2 - ItemCoords.displayX + ItemCoords.textOffsetX, height - ItemCoords.displayY + ItemCoords.damageOffsetY, 0x55FF55);
			}
			if (heldItem.size > 1 || heldItem.getMaxSize() > 1) { //account for overstacked items
				this.drawCenteredString(textRenderer, String.valueOf(heldItem.size), width / 2 - ItemCoords.displayX + ItemCoords.textOffsetX, height - ItemCoords.displayY + ItemCoords.stackOffsetY, 0x55FF55);
			}
			ITEM_RENDERER.renderGuiItemWithEnchantmentGlint(this.minecraft.textRenderer, this.minecraft.textureManager, heldItem, width / 2 - ItemCoords.displayX + ItemCoords.itemIconOffsetX, height - ItemCoords.displayY + ItemCoords.itemIconOffsetY);
		}
	}
}
