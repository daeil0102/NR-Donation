package net.teujaem.nrDonation;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = NrDonation.MODID, name = NrDonation.NAME, version = NrDonation.VERSION)
public class NrDonation {
    public static final String MODID = "nr-donation";
    public static final String NAME = "NR-Donation";
    public static final String VERSION = "1.3.0";

    private static Logger logger;


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }
}
