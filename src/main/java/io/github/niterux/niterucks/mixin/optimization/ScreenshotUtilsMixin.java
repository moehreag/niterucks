package io.github.niterux.niterucks.mixin.optimization;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.niterux.niterucks.Niterucks;
import net.minecraft.client.util.ScreenshotUtils;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@Mixin(ScreenshotUtils.class)
public class ScreenshotUtilsMixin {
	//fixes a bug that can sometimes happen when attempting to take a new screenshot after resizing the window
	//this forcing of the buffer resizes ofc results in a lot more memory I/O, but I think modern computers can handle it
	@ModifyExpressionValue(method = "saveScreenshot(Ljava/io/File;II)Ljava/lang/String;", at = @At(target = "Lnet/minecraft/client/util/ScreenshotUtils;byteBuffer:Ljava/nio/ByteBuffer;", value = "FIELD", opcode = Opcodes.GETSTATIC, ordinal = 0))
	private static ByteBuffer forceByteBufferResize(ByteBuffer original) {
		return null;
	}

	@ModifyExpressionValue(method = "saveScreenshot(Ljava/io/File;II)Ljava/lang/String;", at = @At(target = "Lnet/minecraft/client/util/ScreenshotUtils;f_7482814:[I", value = "FIELD", opcode = Opcodes.GETSTATIC, ordinal = 0))
	private static int[] forceByteArrayResize(int[] original) {
		return null;
	}

	//I can't capture the BufferedImage using @Local or @ModifyVariable for some reason, tried really hard I promise, unfortunately this is the only way.
	//New multithreading by Wagyourtail
	@WrapOperation(method = "saveScreenshot", at = @At(value = "INVOKE", target = "Ljava/awt/image/BufferedImage;setRGB(IIII[III)V"))
	private static void onSaveScreenshot(BufferedImage instance, int off, int y, int i, int startX, int[] startY, int w, int h, Operation<Void> original, @Share("future") LocalRef<CompletableFuture<Void>> future) {
		future.set(CompletableFuture.runAsync(() -> original.call(instance, off, y, i, startX, startY, w, h)));
	}

	@WrapOperation(method = "saveScreenshot", at = @At(value = "INVOKE", target = "Ljavax/imageio/ImageIO;write(Ljava/awt/image/RenderedImage;Ljava/lang/String;Ljava/io/File;)Z"))
	private static boolean onSave(RenderedImage im, String formatName, File output, Operation<Boolean> original, @Share("future") LocalRef<CompletableFuture<Void>> future) {
		future.get().thenRun(() -> {
			try {
				original.call(im, formatName, output);
			} catch (Exception e) {
				Niterucks.LOGGER.error("Failed to save screenshot", e);
			}
		});
		return true;
	}
}
