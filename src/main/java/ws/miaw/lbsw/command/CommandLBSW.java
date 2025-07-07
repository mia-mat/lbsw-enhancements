package ws.miaw.lbsw.command;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import ws.miaw.lbsw.LBMain;
import ws.miaw.lbsw.gui.GUIElement;
import ws.miaw.lbsw.gui.PositionEditorGUI;
import ws.miaw.lbsw.module.LBModule;
import ws.miaw.lbsw.util.ServerUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CommandLBSW extends CommandBase {

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "lbsw";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "";
    }

    // fails if done immediately, presumably as the chat menu is still open, so process on next tick through a switch.
    private boolean openGUI = false;

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {

        if (args.length == 0) {
            if (ServerUtil.isOnHypixel()) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/play solo_insane_lucky");
            }
            return;
        }

        if (args[0].equalsIgnoreCase("gui")) {
            if (args.length >= 2) {
                if (args[1].equalsIgnoreCase("reset")) {
                    for (GUIElement element : LBMain.getGUIManager().getElements()) {
                        element.setPosition(element.getStartPosX(), element.getStartPosY());
                    }

                    sendMessage("Reset LBSW GUI element positions.");

                    try {
                        LBMain.getGUIManager().serialize();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }


                if (args[1].equalsIgnoreCase("panes")) {
                    boolean hasPanes = !LBMain.getGUIManager().hasPaddingPanes();
                    LBMain.getGUIManager().setPaddingPanes(hasPanes);

                    sendMessage("toggled panes " + ChatFormatting.LIGHT_PURPLE + (hasPanes ? "on" : "off") + ChatFormatting.WHITE + ".");
                    return;
                }
            }

            openGUI = true;
            return;
        }


        if (args[0].equalsIgnoreCase("modules")) {
            // show modules and if they're turned on or off
            for (LBModule mod : LBMain.getModuleManager().getModules()) {

                sendMessage(mod.getModuleName() + " (" + ChatFormatting.GRAY + (mod.isEnabled() ? "enabled" : "disabled") + ChatFormatting.WHITE + ")");
            }
            return;
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            if (args.length == 1) {
                sendErrorMessage("Invalid usage. specify a module id");
                return;
            }

            LBModule module = LBMain.getModuleManager().getModule(args[1]);
            if (module == null) {
                sendErrorMessage("Could not find a module with the id " + ChatFormatting.LIGHT_PURPLE + args[1] + ChatFormatting.WHITE + "!");
                sendErrorMessage("Valid options:");
                for (LBModule mod : LBMain.getModuleManager().getModules()) {
                    sendErrorMessage(ChatFormatting.WHITE + mod.getModuleName() + ChatFormatting.WHITE + ": " +
                            mod.getModuleId() + ChatFormatting.GRAY + " (" + (mod.isEnabled() ? "enabled" : "disabled") + ")");
                }
                return;
            }

            module.setEnabled(!module.isEnabled());

            sendMessage("Toggled " + module.getModuleName() + ChatFormatting.LIGHT_PURPLE + (module.isEnabled() ? " on" : " off") + ChatFormatting.WHITE + "!");
            return;
        }


        if (!args[0].equalsIgnoreCase("help")) {
            sendErrorMessage("Invalid usage.");
        }

        sendMessage("/lbsw " + ChatFormatting.GRAY + "- queue into a solo lbsw game");
        sendMessage("/lbsw gui " + ChatFormatting.GRAY + "- configure positions of UI elements");
        sendMessage("/lbsw gui reset " + ChatFormatting.GRAY + "- reset positions of UI elements");
        sendMessage("/lbsw gui panes " + ChatFormatting.GRAY + "- toggle the presence of transparent borders on UI elements");
        sendMessage("/lbsw modules " + ChatFormatting.GRAY + "- show current modules");
        sendMessage("/lbsw toggle <module id> " + ChatFormatting.GRAY + "- toggle a module's functionality");
        sendMessage("/lbsw help " + ChatFormatting.GRAY + "- shows this help message");

    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            return getListOfStringsMatchingLastWord(
                    args,
                    LBMain.getModuleManager().getModules().stream()
                            .map(LBModule::getModuleId)
                            .collect(Collectors.toList())
            );
        }
        return null;
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if (openGUI) {
            Minecraft.getMinecraft().displayGuiScreen(new PositionEditorGUI(LBMain.getGUIManager()));
            openGUI = false;
        }
    }


    private void sendMessage(final String msg) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(ChatFormatting.DARK_PURPLE + "[LBSW] " + ChatFormatting.WHITE + msg));
    }

    private void sendErrorMessage(final String msg) {
        sendMessage(ChatFormatting.RED + msg);
    }

}
