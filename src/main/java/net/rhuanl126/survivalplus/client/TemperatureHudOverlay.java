// Classe responsável por desenhar a temperatura atual do jogador na tela (HUD).
// Utiliza o evento de renderização da HUD do Minecraft.
package net.rhuanl126.survivalplus.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.rhuanl126.survivalplus.temperature.BiomeTemperature;

public class TemperatureHudOverlay {

    // Valor suavizado da temperatura que mostramos na tela
    private static float displayTemp = 20.0F;

    // Texturas dos ícones (coloque seus PNGs em: resources/assets/survivalplus/textures/gui/)
    private static final ResourceLocation ICON_COLD = ResourceLocation.fromNamespaceAndPath("survivalplus", "textures/gui/temp_cold.png");
    private static final ResourceLocation ICON_NEUTRAL = ResourceLocation.fromNamespaceAndPath("survivalplus", "textures/gui/temp_neutral.png");
    private static final ResourceLocation ICON_HOT = ResourceLocation.fromNamespaceAndPath("survivalplus", "textures/gui/temp_hot.png");
    /**
     * Esse método é chamado toda vez que o Minecraft termina de renderizar a HUD.
     * Aqui nós desenhamos o valor da temperatura na tela do jogador.
     */
    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Temperatura real
        int realTemp = BiomeTemperature.getTemperature(mc.player);

        // Suaviza a transição: displayTemp "persegue" realTemp devagar
        displayTemp += (realTemp - displayTemp) * 0.05F; // 0.05 = velocidade (quanto menor, mais lento)

        // Objeto de desenho
        GuiGraphics gui = event.getGuiGraphics();

        // Texto com 1 casa decimal
        String text = String.format("%.1f°C", displayTemp);

        int x = 10;
        int y = 10;

        // Desenha o texto
        gui.drawString(mc.font, text, x, y, 0xFFFFFF, true);

        // Decide qual ícone usar
        ResourceLocation icon;
        if (displayTemp <= 10) {
            icon = ICON_COLD;
        } else if (displayTemp >= 30) {
            icon = ICON_HOT;
        } else {
            icon = ICON_NEUTRAL;
        }

        // Renderiza o ícone do lado do texto
        RenderSystem.enableBlend();
        gui.blit(icon, x + 50, y - 4, 0, 0, 16, 16, 16, 16);
        RenderSystem.disableBlend();
    }
}
