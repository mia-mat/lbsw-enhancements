package ws.miaw.lbsw.module;

import net.minecraftforge.common.MinecraftForge;
import ws.miaw.lbsw.LBMain;

import java.io.*;

public abstract class LBModule {

    // We save just the toggled states of each module to disk.
    private File TOGGLE_STATE_FILE = new File(LBMain.SAVE_FOLDER.getAbsolutePath() + "\\module\\toggle-states\\"+getModuleId());;

    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        if(this.enabled == value) return;

        this.enabled = value;

        if(value) {
            onEnable();
        } else {
            onDisable();
        }

        serializeState();
    }

    protected void onEnable() {
        // We let extending classes choose whether to implement this
    }

    protected void onDisable() {
        // We let extending classes choose whether to implement this
    }

    public abstract String getModuleName();
    public abstract String getModuleId();

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);

        setEnabled(getStateFromFile(true));
    };

    private void serializeState() {
        TOGGLE_STATE_FILE.getParentFile().mkdirs();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TOGGLE_STATE_FILE));
            oos.writeObject(isEnabled());
            oos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean getStateFromFile(boolean _default) {
        if(!TOGGLE_STATE_FILE.exists()) {
            this.enabled = _default;
            serializeState();
        }

        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(TOGGLE_STATE_FILE));
            return (boolean) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException();
        }
    }



}
