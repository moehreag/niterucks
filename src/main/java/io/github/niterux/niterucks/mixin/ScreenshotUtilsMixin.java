package io.github.niterux.niterucks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.util.ScreenshotUtils;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.ByteBuffer;

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
}
