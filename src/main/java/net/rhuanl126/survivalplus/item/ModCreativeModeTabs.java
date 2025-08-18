package net.rhuanl126.survivalplus.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rhuanl126.survivalplus.SurvivalPlus;
import net.rhuanl126.survivalplus.block.ModBlocks;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SurvivalPlus.MOD_ID);

    public static final Supplier<CreativeModeTab> BISMUTH_ITEMS_TABS = CREATIVE_MODE_TAB.register("bismuth_items_tab",
            ()-> CreativeModeTab.builder().icon(()->new ItemStack(ModItems.ROPE.get()))
                    .title(Component.translatable("creativetab.survivalplus.bismuth_items"))
                    .displayItems((itemDisplayParameters, output) ->{
                        output.accept(ModItems.ROPE);
                        output.accept(ModItems.STEEL_INGOT);
                    })
                    .build());

    public static final Supplier<CreativeModeTab> BISMUTH_BLOCK_TABS = CREATIVE_MODE_TAB.register("bismuth_blocks_tab",
            ()-> CreativeModeTab.builder().icon(()->new ItemStack(ModBlocks.BISMUTH_BLOCK.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(SurvivalPlus.MOD_ID, "bismuth_items_tab"))
                    .title(Component.translatable("creativetab.survivalplus.bismuth_blocks"))
                    .displayItems((itemDisplayParameters, output) ->{
                        output.accept(ModBlocks.BISMUTH_BLOCK);
                        output.accept(ModBlocks.BISMUTH_ORE);
                    })
                    .build());

    public static void register (IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }


}
