package ws.miaw.lbsw.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import ws.miaw.lbsw.util.GameUtil;


public class LBNausea extends LBModule {

    @Override
    public String getModuleName() {
        return "No Nausea";
    }

    @Override
    public String getModuleId() {
        return "nausea";
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if(!isEnabled()) return;

        if (event.phase != TickEvent.Phase.END) return; // once per tick

        final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player != null && player.isPotionActive(Potion.confusion)) {
            if(!GameUtil.isInLBSWGame()) return;
            // this implementation does mean that if the player is in a portal at the same time, they won't see the portal effect,
            // but other solutions are far too complicated since no portals exist in lbsw.
            player.timeInPortal = 0;
        }

    }

}
