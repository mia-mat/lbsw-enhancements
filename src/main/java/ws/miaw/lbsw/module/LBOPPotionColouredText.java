package ws.miaw.lbsw.module;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ws.miaw.lbsw.util.GameUtil;

import java.util.ArrayList;
import java.util.List;

public class LBOPPotionColouredText extends LBModule {


    private static final String OP_POTION_NAME = "OP Potion"; // unformatted


    @Override
    public String getModuleName() {
        return "Highlighted OP Strength";
    }

    @Override
    public String getModuleId() {
        return "op-potion-colours";
    }

    @SubscribeEvent
    public void onRenderPotionEffects(ItemTooltipEvent event) {
        if (!isEnabled()) return;
        if (event.toolTip.size() == 0) return;
        if(!GameUtil.isInLBSWGame()) return;

        String itemName = event.toolTip.get(0);
        if(!GameUtil.unformatted(itemName).trim().equals(OP_POTION_NAME)) return;

        // move strength to top and highlight: We just swap since order doesn't matter anyway.
        for(int i = 1; i < event.toolTip.size(); i++) {
            String line = GameUtil.unformatted(event.toolTip.get(i));
            if(!line.startsWith("Strength ")) continue;
            event.toolTip.set(i, event.toolTip.get(1)); // if java was a sensible language this would also update the 'line' pointer but nah
            event.toolTip.set(1, ChatFormatting.RED + "" +ChatFormatting.BOLD + line); // if java was a sensible language this would also update the 'line' pointer but nah
            return;
        }

    }
}
