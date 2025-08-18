package net.rhuanl126.survivalplus.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rhuanl126.survivalplus.SurvivalPlus;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SurvivalPlus.MOD_ID);

    public static final DeferredItem<Item> ROPE = ITEMS.register("rope",
            ()-> new Item(new Item.Properties()));

    public static final DeferredItem<Item> STEEL_INGOT = ITEMS.register("steel_ingot",
            ()-> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
