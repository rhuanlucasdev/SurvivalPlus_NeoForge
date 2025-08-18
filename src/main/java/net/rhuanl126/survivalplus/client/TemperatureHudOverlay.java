// Classe responsável por desenhar a temperatura atual do jogador na tela (HUD).
// Utiliza o evento de renderização da HUD do Minecraft.
package net.rhuanl126.survivalplus.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.rhuanl126.survivalplus.temperature.BiomeTemperature;

public class TemperatureHudOverlay {
    /**
     * Esse método é chamado toda vez que o Minecraft termina de renderizar a HUD.
     * Aqui nós desenhamos o valor da temperatura na tela do jogador.
     */
    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // Se não existe jogador (ex.: ainda carregando mundo), não fazemos nada
        if (mc.player == null) return;

        // Calculamos a temperatura atual com base no bioma + altura
        int temp = BiomeTemperature.getTemperature(mc.player);

        // Objeto responsável por desenhar coisas na tela (texto, imagens, etc.)
        GuiGraphics gui = event.getGuiGraphics();

        // Texto que será exibido (exemplo: "22°C")
        String text = temp + "°C";

        // Coordenadas da tela onde o texto será desenhado
        // (10,10) significa canto superior esquerdo
        int x = 10;
        int y = 10;

        // Desenha o texto na tela:
        // - mc.font → usa a fonte padrão do Minecraft
        // - text → o que será escrito
        // - x, y → posição
        // - 0xFFFFFF → cor branca em hexadecimal
        // - true → habilita "sombreamento" no texto (deixa mais legível)
        gui.drawString(mc.font, text, x, y, 0xFFFFFF, true);
    }
}
