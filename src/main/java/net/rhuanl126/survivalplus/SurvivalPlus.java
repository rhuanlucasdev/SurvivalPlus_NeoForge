package net.rhuanl126.survivalplus;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.rhuanl126.survivalplus.block.ModBlocks;
import net.rhuanl126.survivalplus.code.FoodDecay;
import net.rhuanl126.survivalplus.item.ModCreativeModeTabs;
import net.rhuanl126.survivalplus.item.ModItems;
import org.slf4j.Logger;

@Mod(SurvivalPlus.MOD_ID)
public class SurvivalPlus {

    public static final String MOD_ID = "survivalplus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SurvivalPlus(IEventBus modEventBus, ModContainer modContainer) {

        // Registra common setup
        modEventBus.addListener(this::commonSetup);

        // Registra eventos no NeoForge Event Bus
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(net.rhuanl126.survivalplus.client.TemperatureHudOverlay.class);

        // Registra abas criativas, itens e blocos
        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        // Cria instância do FoodDecay (roda tick de decomposição e barrinha)
        NeoForge.EVENT_BUS.register(new FoodDecay());

        // Registra o item/aba criativa
        modEventBus.addListener(this::addCreative);

        // Registra o config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("SurvivalPlus setup iniciado com sistema de decomposição de comida!");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.ROPE);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.STEEL_INGOT);
        }
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModBlocks.BISMUTH_BLOCK);
            event.accept(ModBlocks.BISMUTH_ORE);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Servidor iniciando com SurvivalPlus ativo!");
    }
}
