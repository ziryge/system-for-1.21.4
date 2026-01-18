package dlindustries.vigillant.system.module.modules.optimizer;

import dlindustries.vigillant.system.event.events.PacketSendListener;
import dlindustries.vigillant.system.event.events.TickListener;
import dlindustries.vigillant.system.module.Category;
import dlindustries.vigillant.system.module.Module;
import dlindustries.vigillant.system.module.setting.NumberSetting;
import dlindustries.vigillant.system.utils.EncryptedString;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public final class Blink
    extends Module
    implements PacketSendListener, TickListener
{

    private final NumberSetting delay = new NumberSetting(
        EncryptedString.of("Delay"),
        0.1,
        10.0,
        4.0,
        0.1
    ).setDescription(EncryptedString.of("Seconds between sending packets"));

    private final List<PlayerMoveC2SPacket> packets = new ArrayList<>();
    private long timer = 0;
    private boolean isReleasing = false;

    public Blink() {
        super(
            EncryptedString.of("Blink"),
            EncryptedString.of(
                "Cancels the sending of packets for a length of time"
            ),
            -1,
            Category.optimizer
        );
        addSettings(delay);
    }

    @Override
    public void onEnable() {
        eventManager.add(PacketSendListener.class, this);
        eventManager.add(TickListener.class, this);
        packets.clear();
        timer = System.currentTimeMillis();
        isReleasing = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        eventManager.remove(PacketSendListener.class, this);
        eventManager.remove(TickListener.class, this);
        sendPackets();
        super.onDisable();
    }

    @Override
    public void onTick() {
        long delayMs = (long) (delay.getValue() * 1000);
        if (System.currentTimeMillis() - timer >= delayMs) {
            timer = System.currentTimeMillis();
            sendPackets();
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (isReleasing) return;

        if (mc.player == null || mc.getNetworkHandler() == null) return;

        // Only cancel and store player movement packets
        if (!(event.packet instanceof PlayerMoveC2SPacket)) {
            return;
        }

        event.cancel();
        packets.add((PlayerMoveC2SPacket) event.packet);
    }

    private void sendPackets() {
        if (packets.isEmpty() || mc.getNetworkHandler() == null) return;

        isReleasing = true;
        try {
            for (PlayerMoveC2SPacket packet : packets) {
                mc.getNetworkHandler().sendPacket(packet);
            }
        } finally {
            isReleasing = false;
        }
        packets.clear();
    }

    public boolean isBlinking() {
        return isEnabled() && !packets.isEmpty();
    }

    public int getPacketCount() {
        return packets.size();
    }
}
