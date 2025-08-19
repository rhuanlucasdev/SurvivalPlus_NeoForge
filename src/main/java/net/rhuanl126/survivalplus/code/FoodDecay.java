package net.rhuanl126.survivalplus.code;
// Define o pacote do mod, onde o arquivo está localizado

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;
// Importa todas as classes necessárias para manipular itens, jogadores, inventários, eventos e NBT.

public class FoodDecay {
    // Define a classe FoodDecay que vai gerenciar o decaimento dos alimentos

    // Map de alimentos com tempo de decaimento em ticks
    private static final Map<Item, Integer> FOOD_DECAY_TIMES = new HashMap<>();
    // Cria um mapa estático que associa cada item com seu tempo de decaimento

    static {
        // Inicializa o mapa com itens e tempos em ticks (1 tick = 0,05s)
        // Carne crua
        FOOD_DECAY_TIMES.put(Items.BEEF, 200);          // 10s
        FOOD_DECAY_TIMES.put(Items.CHICKEN, 180);       // 9s
        FOOD_DECAY_TIMES.put(Items.PORKCHOP, 200);
        FOOD_DECAY_TIMES.put(Items.MUTTON, 200);
        FOOD_DECAY_TIMES.put(Items.RABBIT, 180);
        FOOD_DECAY_TIMES.put(Items.COD, 160);
        FOOD_DECAY_TIMES.put(Items.SALMON, 160);

        // Carne cozida
        FOOD_DECAY_TIMES.put(Items.COOKED_BEEF, 400);  // 20s
        FOOD_DECAY_TIMES.put(Items.COOKED_CHICKEN, 360);
        FOOD_DECAY_TIMES.put(Items.COOKED_PORKCHOP, 400);
        FOOD_DECAY_TIMES.put(Items.COOKED_MUTTON, 400);
        FOOD_DECAY_TIMES.put(Items.COOKED_RABBIT, 360);
        FOOD_DECAY_TIMES.put(Items.COOKED_COD, 320);
        FOOD_DECAY_TIMES.put(Items.COOKED_SALMON, 320);

        // Outros alimentos
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
        // Aqui definimos o tempo que cada alimento demora para estragar
    }

    public FoodDecay() {
        NeoForge.EVENT_BUS.register(this);
        // Registra esta classe no evento do NeoForge, permitindo ouvir ticks do servidor
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Pre event) {
        // Função que é chamada em cada tick do servidor (antes da atualização do mundo)

        for (ServerLevel world : event.getServer().getAllLevels()) {
            // Percorre todos os mundos do servidor (overworld, nether, end)

            for (ServerPlayer player : world.getPlayers(p -> true)) {
                // Percorre todos os jogadores online no mundo

                CompoundTag data = player.getPersistentData();
                // Recupera os dados persistentes do jogador (onde vamos armazenar timers)

                // Percorre cada slot do inventário (0 a 35)
                for (int i = 0; i < 36; i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.isEmpty()) continue;
                    // Pula slots vazios

                    Item item = stack.getItem();
                    Integer decayTime = FOOD_DECAY_TIMES.get(item);
                    if (decayTime == null) continue;
                    // Pula itens que não estão na lista de decaimento

                    // Chave NBT por slot para controlar timers individuais
                    String slotTimerKey = "sp_foodDecay_slot_" + i;
                    int timer = data.getInt(slotTimerKey);
                    timer++;
                    // Incrementa o timer desse slot

                    if (timer >= decayTime) {
                        // Se o timer atingir o tempo de decaimento, transforma o item

                        stack.shrink(1);
                        // Remove 1 unidade do item original

                        ItemStack rottenFlesh = new ItemStack(Items.ROTTEN_FLESH, 1);
                        // Cria um item de carne podre

                        boolean added = player.getInventory().add(rottenFlesh);
                        // Tenta adicionar no inventário do jogador

                        if (!added) {
                            Vec3 pos = player.position();
                            ItemEntity drop = new ItemEntity(world, pos.x, pos.y + 0.5, pos.z, rottenFlesh);
                            world.addFreshEntity(drop);
                        }
                        // Se não houver espaço, dropa no chão na posição do jogador

                        timer = 0;
                        // Reseta o timer do slot para começar novamente

                        data.putInt(slotTimerKey, timer);
                        break;
                        // Só transforma 1 item por tick
                    }

                    data.putInt(slotTimerKey, timer);
                    // Salva o timer atualizado do slot
                }
            }
        }
    }
}
