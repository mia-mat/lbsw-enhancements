package ws.miaw.lbsw.module;

import ws.miaw.lbsw.LBMain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LBModuleManager {

    private Map<Class<? extends LBModule>, LBModule> registeredModules;

    public LBModuleManager() {
        this.registeredModules = new HashMap<>();
    }

    public <M extends LBModule> boolean registerModule(M module) {
        module.init();

        if (registeredModules.containsKey(module.getClass())) {
            return false;
        }

        registeredModules.put(module.getClass(), module);
        return true;
    }

    public <M extends LBModule> LBModule getModule(Class<M> moduleClass) {
        return registeredModules.get(moduleClass); // returns null if not found through map spec.
    }

    @Nullable
    public <M extends LBModule> LBModule getModule(String moduleId) {
        return registeredModules.values().stream().filter(module -> module.getModuleId().equals(moduleId)).findFirst().orElse(null);
    }

    public Collection<LBModule> getModules() {
        return Collections.unmodifiableCollection(registeredModules.values());
    }


}
