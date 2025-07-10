package ws.miaw.lbsw.module;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ws.miaw.lbsw.util.GameUtil;
import ws.miaw.lbsw.LBMain;
import ws.miaw.lbsw.gui.GUIElement;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class LBDevilContractTimer extends LBModule{

    private static final String TRIGGER_STRING = "You have accepted the contract! Win the game in 60 or else you will be instantly killed!";

    private static final Color GUI_COLOUR = new Color(222, 77, 78);
    private static String GUI_MESSAGE(int time) {
        return "Contract: " + time + "s";
    }

    @Override
    public String getModuleName() {
        return "Devil's Contract Timer";
    }

    @Override
    public String getModuleId() {
        return "contract-timer";
    }

    private GUIElement timerElement;

    private TimerTask countdownTask;

    @Override
    public void init() {
        super.init();

        this.timerElement = new GUIElement(getModuleId(), GUI_MESSAGE(60), 20, 20, 1, false, GUI_COLOUR, true, false);
        this.timerElement = LBMain.getGUIManager().registerElement(timerElement);

        this.countdownTask = new TimerTask() {
            @Override
            public void run() {
                currentTimer--;

                if(currentTimer <= 0) {
                    timer.cancel();
                    timer = null;
                    timerElement.setVisible(false);
                }

                timerElement.setText(GUI_MESSAGE(currentTimer));
            }
        };

    }

    private int currentTimer = 60;
    private Timer timer;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent e) {
        if(!isEnabled()) return;

        if(!GameUtil.isInLBSWGame()) {
            this.timerElement.setVisible(false);

            if(timer != null) timer.cancel();
        }

        if(GameUtil.unformatted(e.message.getUnformattedText()).equals(TRIGGER_STRING)) {
            timerElement.setVisible(true);
            currentTimer = 60;
            timer = new Timer();
            timer.scheduleAtFixedRate(countdownTask, 1000, 1000);
        }

    }

    @Override
    protected void onDisable() {
        super.onDisable();
        this.timerElement.setVisible(false);
    }

}
