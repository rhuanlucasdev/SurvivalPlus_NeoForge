// Classe responsável por calcular a temperatura do jogador
// levando em consideração o bioma base + ajuste pela altitude (Y).
package net.rhuanl126.survivalplus.temperature;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class BiomeTemperature {
    // Mapa que armazena a "temperatura base" de cada bioma em Celsius.
    // O valor é fixo e definido manualmente aqui.
    private static final Map<String, Integer> BIOME_TEMPERATURES = new HashMap<>();

    static {
        // Biomas frios
        BIOME_TEMPERATURES.put("minecraft:snowy_plains", 2);
        BIOME_TEMPERATURES.put("minecraft:frozen_peaks", -5);

        // Biomas quentes
        BIOME_TEMPERATURES.put("minecraft:desert", 40);
        BIOME_TEMPERATURES.put("minecraft:savanna", 30);
        BIOME_TEMPERATURES.put("minecraft:jungle", 28);

        // Bioma neutro
        BIOME_TEMPERATURES.put("minecraft:plains", 20);

    }

    /**
     * Retorna a temperatura atual do jogador em graus Celsius.
     * 1. Pega a temperatura base do bioma.
     * 2. Ajusta com base na altitude (Y).
     *
     * @param player O jogador que queremos calcular a temperatura
     * @return Temperatura final arredondada em graus Celsius
     */
    public static int getTemperature(Player player) {
        Level level = player.level();

        // Pegamos a "chave" do bioma onde o jogador está
        ResourceLocation biomeKey = level.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getKey(level.getBiome(player.blockPosition()).value());

        // Valor padrão caso o bioma não esteja na lista
        int baseTemp = 15; // padrão

        // Se o bioma estiver registrado no mapa, usamos a temperatura dele
        if (biomeKey != null && BIOME_TEMPERATURES.containsKey(biomeKey.toString())) {
            baseTemp = BIOME_TEMPERATURES.get(biomeKey.toString());
        }

        // --- Ajuste pela altura (Y) ---
        int y = player.blockPosition().getY();
        int diff = y - 64; // Consideramos Y=64 como a altitude neutra (sem ajuste)

        double adjustment = 0;
        if (diff > 0) {
            // Quanto mais alto acima de Y=64 → mais frio
            // Perde 0.5°C por bloco acima
            adjustment = diff * -0.5;
        } else if (diff < 0) {
            // Quanto mais baixo abaixo de Y=64 → mais quente
            // Ganha 0.5°C por bloco abaixo
            adjustment = Math.abs(diff) * 0.5;
        }

        // Retorna a soma da temperatura base + ajuste, arredondado para inteiro
        return (int)Math.round(baseTemp + adjustment);
    }
}
