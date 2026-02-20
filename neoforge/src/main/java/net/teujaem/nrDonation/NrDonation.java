package net.teujaem.nrDonation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.teujaem.nrDonation.client.NrDonationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(NrDonation.MODID)
public class NrDonation {

    public static final String MODID  = "nr_donation";
    public static final String NAME   = "NR-Donation";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public NrDonation(IEventBus modEventBus, ModContainer modContainer) {
        // ★ Mod 이벤트 버스: IModBusEvent 구현체만 허용
        //   - FMLClientSetupEvent        → Mod 버스 ✅
        //   - RegisterClientCommandsEvent → NeoForge.EVENT_BUS ❌ (여기 등록 불가)
        modEventBus.addListener(this::onClientSetup);
    }

    /**
     * FMLClientSetupEvent: Mod 버스 ✅
     * NrDonationClient 생성 → 생성자 내부에서 NeoForge.EVENT_BUS.register(this) 호출
     * NeoForge.EVENT_BUS 에 등록된 이후 모든 이벤트 (RegisterClientCommandsEvent, LoggingIn 등) 처리
     */
    private void onClientSetup(FMLClientSetupEvent event) {
        new NrDonationClient();
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
