package ws.miaw.lbsw.module;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ws.miaw.lbsw.util.GameUtil;
import ws.miaw.lbsw.LBMain;
import ws.miaw.lbsw.gui.GUIElement;

import java.awt.*;

public class LBAotECounter extends LBModule {

    private static final String AOTE_ITEM_NAME = "§9Aspect of the End";
    private static final String AOTE_BREAK_MESSAGE = "UH OH! Your Aspect of the End broke!";

    private static final Color GUI_COLOUR = new Color(236, 193, 248);
    private static String GUI_MESSAGE(int count) {
        return "AotE: " + count;
    }


    @Override
    public String getModuleName() {
        return "Aspect of the End Counter";
    }

    @Override
    public String getModuleId() {
        return "aote-count";
    }

    private GUIElement counterElement;

    @Override
    public void init() {
        super.init();

        this.counterElement = new GUIElement(getModuleId(), GUI_MESSAGE(10), 0, 0, 1, false, GUI_COLOUR, true, false);
        LBMain.getGUIManager().registerElement(counterElement);
    }

    /// Hypixel's implementation of the AOE counter is such that the unique item does not matter; it is tied to the player.
    private int currentCounter = 10;

    @SubscribeEvent
    public void onPlayerUseItem(PlayerInteractEvent e) {
        if(!isEnabled()) return;

        if (e.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) return;

        if (e.entityPlayer.getHeldItem() == null) return;
        if (!(e.entityPlayer.getHeldItem().getItem() instanceof ItemSword)) return;

        ItemStack item = e.entityPlayer.getHeldItem();
        if (item == null) return;

        if (!item.getDisplayName().equals(AOTE_ITEM_NAME)) return;

        // Action is still RIGHT_CLICK_AIR when the player is clicking on grass or leaves, which hypixel ignores.
        // Perform an additional check for this by checking if the player is targeting any block.
        if (e.entityPlayer.rayTrace(5.0D, 1.0F).typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
            // The player is targeting a block, so ignore this interaction
            return;
        }

        // Set detected true when the player uses the AotE for the first time
        if(!this.counterElement.isVisible()) this.counterElement.setVisible(true);

        setCounter(currentCounter-1);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent e) {
        if(!isEnabled()) return;

        if(!GameUtil.isInLBSWGame() && !GameUtil.isInLBSWPregame()) {
            this.counterElement.setVisible(false);
        }

        // reset counter on game start
        if (e.message.getUnformattedText().equals(GameUtil.GAME_ABOUT_TO_START_MESSAGE) && GameUtil.isInLBSWPregame()) {
            setCounter(10);
            this.counterElement.setVisible(false);
        }

        // reset counter as hypixel does
        // Sometimes this message seems to have colour codes so un-format it
        if (GameUtil.unformatted(e.message.getUnformattedText()).equals(AOTE_BREAK_MESSAGE)) {
            setCounter(10);
            this.counterElement.setVisible(false);
        }

    }

    private void setCounter(int newCount) {
        this.currentCounter = newCount;
        this.counterElement.setText(GUI_MESSAGE(currentCounter));
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        this.counterElement.setVisible(false);
    }
}
