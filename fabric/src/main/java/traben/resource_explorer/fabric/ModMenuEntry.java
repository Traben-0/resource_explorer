package traben.resource_explorer.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import traben.resource_explorer.REConfig;

public class ModMenuEntry implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        try {
            return REConfig.REConfigScreen::new;
        }catch (Exception e){
            return screen -> null;
        }
    }



}
