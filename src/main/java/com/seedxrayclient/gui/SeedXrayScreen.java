package com.seedxrayclient.gui;

import com.seedxrayclient.scan.OreHighlighter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class SeedXrayScreen extends Screen {
    private TextFieldWidget seedField;

    public SeedXrayScreen() {
        super(Text.translatable("gui.seedxray.title"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int topY = this.height / 2 - 30;

        this.seedField = new TextFieldWidget(this.textRenderer, centerX - 100, topY, 200, 20, Text.translatable("gui.seedxray.seed"));
        this.seedField.setMaxLength(64);
        this.seedField.setText(OreHighlighter.getSeedText());
        this.seedField.setFocused(true);
        addDrawableChild(this.seedField);

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.seedxray.ok"), button -> {
            String input = seedField.getText().trim();
            OreHighlighter.setSeedFromText(input);
            OreHighlighter.scanAroundPlayerAsync(50);
            close();
        }).dimensions(centerX - 100, topY + 30, 95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.seedxray.cancel"), button -> close())
                .dimensions(centerX + 5, topY + 30, 95, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.getTitle(), this.width / 2, this.height / 2 - 50, 0xFFFFFF);
    }

    private void close() {
        MinecraftClient.getInstance().setScreen(null);
    }
}
