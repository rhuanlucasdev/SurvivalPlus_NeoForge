package net.rhuanl126.survivalplus.code;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Mod("survivalplus")
public class FoodDecay {

    private static final Map<Item, Integer> FOOD_DECAY_TIMES = new HashMap<>();
    private static final Map<Object, Long> GLOBAL_FOOD_TIMERS = new HashMap<>();

    private static final Map<BlockPos, ServerLevel> containerPositions = new HashMap<>();

    private static final int CHECK_INTERVAL = 60; // 3 segundos
    private int tickCounter = 0;

    static {
        FOOD_DECAY_TIMES.put(Items.BEEF, 200);
        FOOD_DECAY_TIMES.put(Items.CHICKEN, 180);
        FOOD_DECAY_TIMES.put(Items.PORKCHOP, 200);
        FOOD_DECAY_TIMES.put(Items.MUTTON, 200);
        FOOD_DECAY_TIMES.put(Items.RABBIT, 180);
        FOOD_DECAY_TIMES.put(Items.COOKED_BEEF, 400);
        FOOD_DECAY_TIMES.put(Items.COOKED_CHICKEN, 360);
        FOOD_DECAY_TIMES.put(Items.COOKED_PORKCHOP, 400);
        FOOD_DECAY_TIMES.put(Items.COOKED_MUTTON, 400);
        FOOD_DECAY_TIMES.put(Items.COOKED_RABBIT, 360);
        FOOD_DECAY_TIMES.put(Items.BREAD, 500);
        FOOD_DECAY_TIMES.put(Items.APPLE, 600);
        FOOD_DECAY_TIMES.put(Items.PUMPKIN_PIE, 800);
        FOOD_DECAY_TIMES.put(Items.CARROT, 700);
    }

    public FoodDecay() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel world && event.getChunk() instanceof LevelChunk chunk) {
            chunk.getBlockEntities().values().stream()
                    .filter(be -> be instanceof Container)
                    .forEach(be -> containerPositions.put(be.getBlockPos(), world));
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel && event.getChunk() instanceof LevelChunk chunk) {
            chunk.getBlockEntities().values().stream()
                    .filter(be -> be instanceof Container)
                    .forEach(be -> containerPositions.remove(be.getBlockPos()));
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Pre event) {
        tickCounter++;

        for (ServerLevel world : event.getServer().getAllLevels()) {
            processPlayerInventories(world);

            if (tickCounter % CHECK_INTERVAL == 0) {
                processLoadedItems(world);
                processContainers();
            }
        }
    }

    private void processPlayerInventories(ServerLevel world) {
        for (ServerPlayer player : world.getPlayers(p -> true)) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                String key = "player_" + player.getUUID().toString() + "_" + i;
                processItem(stack, key, player.position(), world, null);
            }
        }
    }

    private void processLoadedItems(ServerLevel world) {
        for (ItemEntity itemEntity : StreamSupport.stream(world.getAllEntities().spliterator(), false)
                .filter(entity -> entity instanceof ItemEntity)
                .map(entity -> (ItemEntity) entity)
                .toList()) {
            ItemStack stack = itemEntity.getItem();
            UUID key = itemEntity.getUUID();
            processItem(stack, key, itemEntity.position(), world, null);

            if (stack.isEmpty()) {
                itemEntity.discard();
                GLOBAL_FOOD_TIMERS.remove(key);
            }
        }
    }

    private void processContainers() {
        Set<BlockPos> toRemove = new HashSet<>();

        for (Map.Entry<BlockPos, ServerLevel> entry : containerPositions.entrySet()) {
            BlockPos pos = entry.getKey();
            ServerLevel world = entry.getValue();

            if (world == null || !world.isLoaded(pos)) {
                toRemove.add(pos);
                continue;
            }

            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof Container container) {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack stack = container.getItem(i);
                    String key = "container_" + pos.toShortString() + "_" + i;
                    processItem(stack, key, Vec3.atCenterOf(pos), world, container);
                }
            } else {
                toRemove.add(pos);
            }
        }
        containerPositions.keySet().removeAll(toRemove);
    }

    private void processItem(ItemStack stack, Object key, Vec3 dropPosition, ServerLevel world, Container container) {
        if (stack.isEmpty()) {
            GLOBAL_FOOD_TIMERS.remove(key);
            return;
        }

        Item item = stack.getItem();
        Integer decayTime = FOOD_DECAY_TIMES.get(item);

        if (decayTime == null) {
            GLOBAL_FOOD_TIMERS.remove(key);
            return;
        }

        if (!GLOBAL_FOOD_TIMERS.containsKey(key)) {
            GLOBAL_FOOD_TIMERS.put(key, world.getGameTime());
        }

        long startTime = GLOBAL_FOOD_TIMERS.get(key);
        long currentTime = world.getGameTime();

        long elapsedTime = currentTime - startTime;

        if (elapsedTime >= decayTime) {
            stack.shrink(1);

            ItemStack rottenFlesh = new ItemStack(Items.ROTTEN_FLESH, 1);

            if (container != null) {
                boolean added = false;
                // Tenta encontrar um slot com carne podre para empilhar
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack containerStack = container.getItem(i);
                    if (containerStack.getItem() == Items.ROTTEN_FLESH && containerStack.getCount() < containerStack.getMaxStackSize()) {
                        containerStack.grow(1);
                        container.setChanged();
                        added = true;
                        break;
                    }
                }

                // Se não pôde empilhar, tenta encontrar um slot vazio
                if (!added) {
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        if (container.getItem(i).isEmpty()) {
                            container.setItem(i, rottenFlesh);
                            container.setChanged();
                            added = true;
                            break;
                        }
                    }
                }

                // Se ainda não adicionou, o baú está cheio. O item é simplesmente deletado.
            } else {
                // Para inventários de jogador ou itens no chão, joga o item no mundo
                ItemEntity drop = new ItemEntity(world, dropPosition.x, dropPosition.y + 0.5, dropPosition.z, rottenFlesh);
                world.addFreshEntity(drop);
            }

            GLOBAL_FOOD_TIMERS.put(key, world.getGameTime());
        }
    }
}