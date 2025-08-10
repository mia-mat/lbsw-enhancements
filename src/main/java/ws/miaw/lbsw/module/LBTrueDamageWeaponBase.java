package ws.miaw.lbsw.module;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ws.miaw.lbsw.LBMain;
import ws.miaw.lbsw.gui.GUIElement;
import ws.miaw.lbsw.util.GameUtil;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

// base for perun, excalibur...
public abstract class LBTrueDamageWeaponBase extends LBModule {

    public abstract String getWeaponName();

    // allow overriding
    public double getCooldown() {
        return 4.5d;
    }

    public double getTimerTaskPeriodInSeconds() {
        return 0.5;
    }

    public Color getGuiColour() {
        return new Color(76, 162, 238);
    }

    public abstract String getGuiMessagePrefix();

    private static final DecimalFormat GUI_MESSAGE_FORMATTER = new DecimalFormat("#0"); // 0dp

    private String GUI_MESSAGE(double time) {
        return getGuiMessagePrefix() + ": " + GUI_MESSAGE_FORMATTER.format(time) + "s";
    }

    private GUIElement timerElement;
    private double currentTimer = getCooldown();

    private TimerTask countdownTask;

    private Timer timer;

    @Override
    public void init() {
        super.init();

        this.timerElement = new GUIElement(getModuleId(), GUI_MESSAGE(getCooldown()), 20, 20, 1, false, getGuiColour(), true, false);
        this.timerElement = LBMain.getGUIManager().registerElement(timerElement);

        this.countdownTask = new TimerTask() {
            @Override
            public void run() {
                currentTimer -= getTimerTaskPeriodInSeconds();

                if (currentTimer <= 0) {
                    timer.cancel();
                    timer = null;
                    timerElement.setVisible(false);
                }

                timerElement.setText(GUI_MESSAGE(currentTimer));
            }
        };
    }

    @SubscribeEvent
    public void onHit(AttackEntityEvent e) {
        if (!isEnabled() || !GameUtil.isInLBSWGame()) {
            this.timerElement.setVisible(false);

            if (timer != null) timer.cancel();
            return;
        }

        if (e.target instanceof EntityPlayer) {
            if(e.entityPlayer == null) return;
            if(e.entityPlayer.getHeldItem() == null) return;
            if(e.entityPlayer.getHeldItem().getDisplayName() == null) return;

            String attackedWith = e.entityPlayer.getHeldItem().getDisplayName();
            if (!attackedWith.equals(getWeaponName())) return;

            timerElement.setVisible(true);
            currentTimer = getCooldown();
            timer = new Timer();
            timer.scheduleAtFixedRate(countdownTask, (long) (getTimerTaskPeriodInSeconds() * 1000L), (long) (getTimerTaskPeriodInSeconds() * 1000L));
        }


    }

}
