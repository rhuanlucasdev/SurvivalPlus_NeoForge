package net.rhuanl126.survivalplus.code;

// ==== Imports ====

// Representa qualquer item no Minecraft (Ex: BEEF, APPLE, etc.)
import net.minecraft.world.item.Item;
// Representa uma pilha de itens, incluindo quantidade e NBT
import net.minecraft.world.item.ItemStack;
// Acesso aos itens padrão do Minecraft
import net.minecraft.world.item.Items;
// Representa um jogador no servidor
import net.minecraft.server.level.ServerPlayer;
// Representa o mundo/nível do servidor
import net.minecraft.server.level.ServerLevel;
// Entidade de item que pode ser solta no mundo
import net.minecraft.world.entity.item.ItemEntity;
// Vetor 3D (posição x, y, z)
import net.minecraft.world.phys.Vec3;
// Evento de tick do servidor (executa várias vezes por segundo)
import net.neoforged.neoforge.event.tick.ServerTickEvent;
// Anotação para se inscrever no Event Bus do NeoForge
import net.neoforged.bus.api.SubscribeEvent;
// Acesso ao Event Bus do NeoForge
import net.neoforged.neoforge.common.NeoForge;

import java.util.Map;
// Interface de mapa (chave -> valor)
import java.util.HashMap;
// Implementação concreta de Map

// ==== Código principal ====
public class FoodDecay {

    // Tempo de decaimento de cada alimento (em ticks)
    private static final Map<Item, Integer> FOOD_DECAY_TIMES = new HashMap<>();

    // Timer interno para cada jogador: jogador -> slot do inventário -> tempo decorrido
    private static final Map<ServerPlayer, Map<Integer, Integer>> PLAYER_FOOD_TIMERS = new HashMap<>();

    // Inicializa os tempos de decaimento
    static {
        FOOD_DECAY_TIMES.put(Items.BEEF, 200);
        FOOD_DECAY_TIMES.put(Items.CHICKEN, 180);
        FOOD_DECAY_TIMES.put(Items.PORKCHOP, 200);
        FOOD_DECAY_TIMES.put(Items.MUTTON, 200);
        FOOD_DECAY_TIMES.put(Items.RABBIT, 180);
        FOOD_DECAY_TIMES.put(Items.COD, 160);
        FOOD_DECAY_TIMES.put(Items.SALMON, 160);
        FOOD_DECAY_TIMES.put(Items.COOKED_BEEF, 400);
        FOOD_DECAY_TIMES.put(Items.COOKED_CHICKEN, 360);
        FOOD_DECAY_TIMES.put(Items.COOKED_PORKCHOP, 400);
        FOOD_DECAY_TIMES.put(Items.COOKED_MUTTON, 400);
        FOOD_DECAY_TIMES.put(Items.COOKED_RABBIT, 360);
        FOOD_DECAY_TIMES.put(Items.COOKED_COD, 320);
        FOOD_DECAY_TIMES.put(Items.COOKED_SALMON, 320);
        FOOD_DECAY_TIMES.put(Items.BREAD, 500);
        FOOD_DECAY_TIMES.put(Items.APPLE, 600);
        FOOD_DECAY_TIMES.put(Items.GOLDEN_APPLE, 1200);
        FOOD_DECAY_TIMES.put(Items.PUMPKIN_PIE, 800);
        FOOD_DECAY_TIMES.put(Items.MELON_SLICE, 300);
        FOOD_DECAY_TIMES.put(Items.CARROT, 700);
        FOOD_DECAY_TIMES.put(Items.POTATO, 700);
        FOOD_DECAY_TIMES.put(Items.BAKED_POTATO, 800);
        FOOD_DECAY_TIMES.put(Items.BEETROOT, 600);
        FOOD_DECAY_TIMES.put(Items.BEETROOT_SOUP, 900);
        FOOD_DECAY_TIMES.put(Items.SWEET_BERRIES, 400);
    }

    // Construtor: registra o listener no Event Bus do NeoForge
    public FoodDecay() {
        NeoForge.EVENT_BUS.register(this);
    }

    // Evento chamado a cada tick do servidor
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Pre event) {
        // Itera por todos os mundos do servidor
        for (ServerLevel world : event.getServer().getAllLevels()) {
            // Itera por todos os jogadores do mundo
            for (ServerPlayer player : world.getPlayers(p -> true)) {

                // Obtém ou cria o mapa de timers para o jogador
                Map<Integer, Integer> timers = PLAYER_FOOD_TIMERS.computeIfAbsent(player, p -> new HashMap<>());

                // Itera por cada slot do inventário (0 a 35)
                for (int i = 0; i < 36; i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.isEmpty()) continue; // pula slots vazios

                    Item item = stack.getItem();
                    Integer decayTime = FOOD_DECAY_TIMES.get(item);
                    if (decayTime == null) continue; // pula itens que não têm decaimento

                    // Atualiza o timer do slot
                    int timer = timers.getOrDefault(i, 0) + 1;
                    timers.put(i, timer);

                    // Calcula progresso de decaimento em %
                    int progress = (int)((timer / (float)decayTime) * 100);
                    if (progress > 100) progress = 100;

                    // Aqui poderia atualizar o lore/nome do item para mostrar barra de progresso

                    // Se o alimento decaiu completamente, substitui por carne podre
                    if (timer >= decayTime) {
                        stack.shrink(1); // remove 1 do item atual

                        ItemStack rottenFlesh = new ItemStack(Items.ROTTEN_FLESH, 1);
                        boolean added = player.getInventory().add(rottenFlesh); // tenta adicionar ao inventário
                        if (!added) { // se não couber, dropa no chão
                            Vec3 pos = player.position();
                            ItemEntity drop = new ItemEntity(world, pos.x, pos.y + 0.5, pos.z, rottenFlesh);
                            world.addFreshEntity(drop);
                        }

                        timers.put(i, 0); // reseta o timer
                    }
                }
            }
        }
    }
}
