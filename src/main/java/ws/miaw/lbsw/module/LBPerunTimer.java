package ws.miaw.lbsw.module;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemTool;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ws.miaw.lbsw.LBMain;
import ws.miaw.lbsw.gui.GUIElement;
import ws.miaw.lbsw.util.GameUtil;

import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class LBPerunTimer extends LBTrueDamageWeaponBase {


    @Override
    public String getModuleName() {
        return "Perun Timer";
    }

    @Override
    public String getModuleId() {
        return "perun-timer";
    }

    @Override
    public String getWeaponName() {
        return "§6Axe of Perun";
    }

    @Override
    public String getGuiMessagePrefix() {
        return "Perun";
    }
}
