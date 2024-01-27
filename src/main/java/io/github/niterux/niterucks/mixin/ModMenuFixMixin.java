package io.github.niterux.niterucks.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = TitleScreen.class, priority = 1500)
public class ModMenuFixMixin extends Screen {
	@SuppressWarnings({"MixinAnnotationTarget", "InvalidMemberReference", "UnresolvedMixinReference"})
	@TargetHandler(
		mixin = "com.terraformersmc.modmenu.mixin.MixinTitleScreen",
		name = "onInit"
	)
	@ModifyArg(
		method = "@MixinSquared:Handler",
		at = @At(
			value = "INVOKE",
			target = "com/terraformersmc/modmenu/gui/widget/ModMenuButtonWidget",
			ordinal = 0,
			remap = false
		), index = 3
	)
	private int reduceModMenuButtonSize(int originalWidth) {
		return 98;
	}
}
