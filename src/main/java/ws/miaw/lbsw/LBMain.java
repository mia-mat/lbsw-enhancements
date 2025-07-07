package ws.miaw.lbsw;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import ws.miaw.lbsw.command.CommandLBSW;
import ws.miaw.lbsw.gui.GUIManager;
import ws.miaw.lbsw.module.*;
import ws.miaw.lbsw.util.ServerUtil;

import java.io.File;

@Mod(modid = LBMain.MODID, version = LBMain.VERSION)
public class LBMain {
    public static final String MODID = "lbsw-enhancements";
    public static final String VERSION = "1.1";

    private static GUIManager guiManager;
    private static LBModuleManager moduleManager;

    public static File SAVE_FOLDER;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        SAVE_FOLDER = new File(event.getModConfigurationDirectory().getAbsolutePath() + "\\miaw\\lbsw-enhancements\\");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        SAVE_FOLDER.mkdir();

        guiManager = new GUIManager();
        moduleManager = new LBModuleManager();
        ServerUtil.init();

        getModuleManager().registerModule(new LBNausea());
        getModuleManager().registerModule(new LBAotECounter());
        getModuleManager().registerModule(new LBDevilContractTimer());
        getModuleManager().registerModule(new LBIceSkatesTimer());
        getModuleManager().registerModule(new LBPerunTimer());
        getModuleManager().registerModule(new LBExcaliburTimer());
        getModuleManager().registerModule(new LBOPPotionColouredText());
        getModuleManager().registerModule(new LBGoldenZombieSwordCounter());

        CommandLBSW command = new CommandLBSW();
        ClientCommandHandler.instance.registerCommand(command);
        MinecraftForge.EVENT_BUS.register(command);
    }

    public static GUIManager getGUIManager() {
        return guiManager;
    }

    public static LBModuleManager getModuleManager() {
        return moduleManager;
    }

    public static File getMinecraftFolder() {
        return Minecraft.getMinecraft().mcDataDir;
    }

}
