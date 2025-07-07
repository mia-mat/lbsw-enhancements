package ws.miaw.lbsw.util;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ServerUtil {

    private static boolean onHypixel = false;

    public static void setOnHypixel(boolean newValue) {
        onHypixel = newValue;
    }

    public static boolean isOnHypixel() {
        return  onHypixel;
    }

    static {
        init();
    }

    private static ServerUtilEventHandler eventHandler;

    public static boolean init() {
        if(eventHandler != null) return false;
        eventHandler = new ServerUtilEventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
        return true;
    }

    private static class ServerUtilEventHandler {
        @SubscribeEvent
        public void onWorldLoad(WorldEvent.Load e) {
            if (Minecraft.getMinecraft().getCurrentServerData() == null) {
                ServerUtil.setOnHypixel(false);
                return;
            }

            if (Minecraft.getMinecraft().isSingleplayer()) {
                ServerUtil.setOnHypixel(false);
                return;
            }

            final String ip = Minecraft.getMinecraft().getCurrentServerData().serverIP;
            if (ip.split("\\.").length == 2) {
                ServerUtil.setOnHypixel(ip.equalsIgnoreCase("hypixel.net"));
            } else {
                ServerUtil.setOnHypixel(ip.toLowerCase().endsWith(".hypixel.net"));
            }
        }
    }

}
