package io.github.niterux.niterucks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.InputPlayerEntity;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.niterux.niterucks.Niterucks.LOGGER;
import static io.github.niterux.niterucks.bevofeatures.BetaEVOFlyHelper.flySpeed;
import static io.github.niterux.niterucks.bevofeatures.BetaEVOFlyHelper.flying;

@Mixin(GameGui.class)
public class GameGuiMixin extends GuiElement {
	@Unique
	private static final String[] directions = {
		"South (Towards positive Z)",
		"West (Towards Negative X)",
		"North (Towards Negative Z)",
		"East (Towards Positive X)"};

	@Shadow
	private Minecraft minecraft;

	@Shadow
	private static ItemRenderer ITEM_RENDERER;

	@Inject(method = "addChatMessage(Ljava/lang/String;)V", at = @At("HEAD"))
	private void printChatMessage(String text, CallbackInfo ci) {
		LOGGER.info(text);
	}

	@ModifyConstant(method = "render",
		constant = @Constant(stringValue = "Minecraft Beta 1.7.3 ("))
	private String replaceGameName(String original) {
		return String.format("Niterucks Client %s (", FabricLoader.getInstance().getModContainer("niterucks").get().getMetadata().getVersion().getFriendlyString());
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

	//todo, oh no
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

	@Redirect(
		method = "render",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/entity/living/player/InputPlayerEntity;x:D",
			opcode = Opcodes.GETFIELD
		)
	)
	private double roundCoordsX(InputPlayerEntity instance) {
		return truncate(instance.x);
	}

	@Redirect(
		method = "render",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/entity/living/player/InputPlayerEntity;y:D",
			opcode = Opcodes.GETFIELD
		)
	)
	private double roundCoordsY(InputPlayerEntity instance) {
		return truncate(instance.y);
	}

	@Redirect(
		method = "render",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/entity/living/player/InputPlayerEntity;z:D",
			opcode = Opcodes.GETFIELD
		)
	)
	private double roundCoordsZ(InputPlayerEntity instance) {
		return truncate(instance.z);
	}

	@Unique
	private double truncate(double coord) {
		return (double) Math.round(coord * 1000) / 1000;
	}

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			ordinal = 3,
			target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V",
			remap = false
		)
	)
	private void addFlyText(float screenOpen, boolean mouseX, int mouseY, int par4, CallbackInfo ci, @Local(ordinal = 0) TextRenderer var8, @Local(ordinal = 3) int height) {
		if (flying) {
			this.drawString(var8, "Flying", 2, height - 25, 0x55FF55);
			this.drawString(var8, "Fly Speed: " + flySpeed, 2, height - 15, 0x55FF55);
		}
	}

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
	private void addDurability(float screenOpen, boolean mouseX, int mouseY, int par4, CallbackInfo ci, @Local(ordinal = 0) TextRenderer var8, @Local(ordinal = 3) int height, @Local(ordinal = 2) int width) {
		ItemStack heldItem = minecraft.player.inventory.getMainHandStack();
		String damageText;
		if (heldItem != null) {
			String itemIdText = String.valueOf(heldItem.itemId);
			if (heldItem.getMaxDamage() > 0) {
				damageText = String.valueOf(heldItem.getMaxDamage() - heldItem.getDamage());
			} else {
				damageText = String.valueOf(heldItem.getDamage());
			}
			ITEM_RENDERER.renderGuiItemWithEnchantmentGlint(this.minecraft.textRenderer, this.minecraft.textureManager, heldItem, width / 2 - 130, height - 31);
			if (heldItem.size > 1) {
				this.drawString(var8, String.valueOf(heldItem.size), width / 2 - var8.getWidth(String.valueOf(heldItem.size)) / 2 - 122, height - 40, 0x55FF55);
			}
			this.drawString(var8, damageText, width / 2 - var8.getWidth(damageText) / 2 - 122, height - 15, 0x55FF55);
			this.drawString(var8, itemIdText, width / 2 - var8.getWidth(itemIdText) - 130, height - 27, 0x55FF55);
		}
	}
}
