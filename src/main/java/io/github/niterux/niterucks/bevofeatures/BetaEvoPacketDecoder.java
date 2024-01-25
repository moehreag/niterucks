package io.github.niterux.niterucks.bevofeatures;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.github.niterux.niterucks.NiteLogger;
import io.github.niterux.niterucks.Niterucks;
import io.github.niterux.niterucks.mixin.accessors.MinecraftInstanceAccessor;
import org.apache.logging.log4j.Logger;

public class BetaEvoPacketDecoder {
	private static final NiteLogger logger = Niterucks.logger;
	private static final Gson gson = new Gson();

	public static void decode(String PacketData) {
		logger.debug(PacketData);
		try {
			BetaEvoPacketPOJO[] data = gson.fromJson(PacketData, BetaEvoPacketPOJO[].class);
			for (BetaEvoPacketPOJO datum : data) {
				decodeUpdate(datum);
			}
		} catch (JsonSyntaxException e) {
			logger.warn("Invalid BetaEvo packet data recieved! Outdated client?");
		}
	}

	private static void decodeUpdate(BetaEvoPacketPOJO data) {
		switch (data.updateType) {
			case "handshake":
				MinecraftInstanceAccessor.getMinecraft().getNetworkHandler().sendPacket(new BetaEvoPacket("[{\"protocol\":2,\"updateType\":\"handshake\"}]"));
				break;
			case "fly":
				BetaEVOFlyHelper.flyAllowed = data.enabled;
				break;
			case "none":
				logger.warn("No update type provided in betaevo packet! Outdated client?");
				break;
            default:
				logger.warn("Unexpected updateType: " + data.updateType);
        }
	}
}
