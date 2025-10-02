package com.seedxrayclient;

import com.seedxrayclient.gui.SeedXrayScreen;
import com.seedxrayclient.render.OreRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SeedXrayClient implements ClientModInitializer {
    public static final String MOD_ID = "seedxray";

    private static KeyBinding toggleGuiKey;

    @Override
    public void onInitializeClient() {
        toggleGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.seedxray.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_6,
                "category.seedxray"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleGuiKey.wasPressed()) {
                if (client.currentScreen instanceof SeedXrayScreen) {
                    client.setScreen(null);
                } else {
                    client.setScreen(new SeedXrayScreen());
                }
            }
        });

        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> OreRenderer.render(context));

        // Clear markers when world changes to avoid stale data
        ClientTickEvents.START_WORLD_TICK.register(world -> {
            // If the world ref changes (e.g., dimension swap), the renderer will check player/world anyway
        });
    }

    public static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }
}
