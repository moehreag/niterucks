package io.github.niterux.niterucks.config.screen;

import io.github.axolotlclient.AxolotlClientConfig.api.AxolotlClientConfig;
import io.github.axolotlclient.AxolotlClientConfig.api.manager.ConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.api.ui.screen.ConfigScreen;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import io.github.niterux.niterucks.config.widget.NiterucksCategoryListWidget;
import io.github.niterux.niterucks.config.widget.NiterucksOptionListWidget;
import io.github.niterux.niterucks.mixin.accessors.MinecraftInstanceAccessor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.resource.language.I18n;

public class NiterucksConfigScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen implements ConfigScreen {
	private static Screen parent;
	private final ConfigManager configManager;
	private final OptionCategory category;
	public NiterucksConfigScreen(Screen parentScreen, OptionCategory category) {
		super(I18n.translate(category.getName()));
		if (!(parentScreen instanceof NiterucksConfigScreen)) {
			parent = parentScreen;
		}
		this.configManager = AxolotlClientConfig.getInstance().getConfigManager(category);
		this.category = category;
	}

	@Override
	public void init() {

		this.addDrawableChild(new NiterucksOptionListWidget(this.configManager, this.category, this.width, this.height, 35, this.height - 45, 25));
		this.addDrawableChild(new NiterucksCategoryListWidget(this.configManager, this.category, this.width/4, this.height, 35, this.height-45, 25));
		this.addDrawableChild(new VanillaButtonWidget(this.width / 2 - 75, this.height - 35, 150, 20, I18n.translate("gui.back"), (w) -> {
			MinecraftInstanceAccessor.getMinecraft().openScreen(parent);
		}));
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);

		drawCenteredString(MinecraftInstanceAccessor.getMinecraft().textRenderer,
			getTitle(), width / 2, 15, -1);
	}

	public ConfigManager getConfigManager() {
		return this.configManager;
	}
}
