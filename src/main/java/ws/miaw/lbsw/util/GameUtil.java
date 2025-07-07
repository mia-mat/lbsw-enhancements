package ws.miaw.lbsw.util;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.IChatComponent;

import java.util.*;

public class GameUtil {

    public static final String GAME_ABOUT_TO_START_MESSAGE = "The game starts in 1 second!";

    public static List<String> getHypixelSidebarLines() {
        List<String> lines = new ArrayList<String>();

        // 1. Get the client scoreboard
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            return lines;
        }
        Scoreboard scoreboard = mc.theWorld.getScoreboard();

        // 2. Get the sidebar objective (display slot 1 is 'sidebar')
        ScoreObjective sidebar = scoreboard.getObjectiveInDisplaySlot(1);
        if (sidebar == null) {
            return lines; // No sidebar objective active
        }

        // 3. Retrieve all scores for this objective
        Collection<Score> scores = scoreboard.getSortedScores(sidebar);

        // 4. Build each line, including team prefix/suffix
        for (Score score : scores) {
            String entry = score.getPlayerName();
            ScorePlayerTeam team = scoreboard.getPlayersTeam(entry);
            if (team != null) {
                entry = team.getColorPrefix() + entry + team.getColorSuffix();
            }
            lines.add(entry);
        }

        // lines are in reverse order
        Collections.reverse(lines);

        return lines;
    }

    public static String getSidebarName(){
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return null;

        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        ScoreObjective sidebar = scoreboard.getObjectiveInDisplaySlot(1);
        if (sidebar == null) return null;

        // 1) Get the title
        return sidebar.getDisplayName();
    }

    public static boolean isInLBSWGame() {
        if(!ServerUtil.isOnHypixel()) return false;
        if(getSidebarName() == null) return false;
        if(!unformatted(getSidebarName()).equals("SKYWARS")) return false;

        List<String> sidebar = getHypixelSidebarLines();

        if(sidebar.size() != 13) return false;
        //weird hypixel formatting
        if(!unformatted(sidebar.get(10)).replaceAll("[^\\w:\\s]", "").equals("Lab: Lucky Blocks")) return false;

        return true;
    }

    public static boolean isInLBSWPregame() {
        if(!ServerUtil.isOnHypixel()) return false;
        if(getSidebarName() == null) return false;
        if(!unformatted(getSidebarName()).equals("SKYWARS")) return false;

        List<String> sidebar = getHypixelSidebarLines();

        if(sidebar.size() != 10) return false;
        if(!unformatted(sidebar.get(7)).replaceAll("[^\\w:\\s]", "").equals("Lab: Lucky Blocks")) return false;
        if(!unformatted(sidebar.get(4)).startsWith("Starting in ")) return false;

        return true;
    }

    public static String unformatted(String coloured) {
        return ChatFormatting.stripFormatting(coloured);
    }
}