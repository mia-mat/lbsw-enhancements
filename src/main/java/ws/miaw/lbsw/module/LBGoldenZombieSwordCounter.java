package ws.miaw.lbsw.module;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ws.miaw.lbsw.LBMain;
import ws.miaw.lbsw.gui.GUIElement;
import ws.miaw.lbsw.util.GameUtil;

import java.awt.*;

public class LBGoldenZombieSwordCounter extends LBModule {

    private static final String SWORD_ITEM_NAME = "§9Golden Zombie Sword";
    private static final String SWORD_BREAK_MESSAGE = "UH OH! Your Golden Pigman Sword broke!";

    private static final Color GUI_COLOUR = new Color(245, 205, 95);
    private static String GUI_MESSAGE(int count) {
        return "Zombie: " + count;
    }


    @Override
    public String getModuleName() {
        return "Golden Zombie Sword Counter";
    }

    @Override
    public String getModuleId() {
        return "zombie-sword-count";
    }

    private GUIElement counterElement;

    @Override
    public void init() {
        super.init();

        this.counterElement = new GUIElement(getModuleId(), GUI_MESSAGE(10), 0, 0, 1, false, GUI_COLOUR, true, false);
        LBMain.getGUIManager().registerElement(counterElement);
    }

    /// Hypixel's implementation of the counter is such that the unique item does not matter; it is tied to the player.
    private int currentCounter = 10;

    @SubscribeEvent
    public void onHit(AttackEntityEvent e) {
        if (!isEnabled()) return;
        if(!GameUtil.isInLBSWGame()) return;

        if (e.target instanceof EntityPlayer) {
            String attackedWith = e.entityPlayer.getHeldItem().getDisplayName();
            if (!attackedWith.equals(SWORD_ITEM_NAME)) return;

            // so gets shown after first use
            if(!this.counterElement.isVisible()) this.counterElement.setVisible(true);
            setCounter(currentCounter-1);
        }


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
        if (GameUtil.unformatted(e.message.getUnformattedText()).equals(SWORD_BREAK_MESSAGE)) {
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
