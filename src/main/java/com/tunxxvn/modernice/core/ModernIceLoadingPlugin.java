package com.tunxxvn.modernice.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.Name("ModernIce")
@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions("com.tunxxvn.modernice.core.")
public final class ModernIceLoadingPlugin implements IFMLLoadingPlugin {
    @Override public String[] getASMTransformerClass() {
        return new String[]{"com.tunxxvn.modernice.core.MovementTransformer"};
    }
    @Override public String getModContainerClass() { return null; }
    @Override public String getSetupClass() { return null; }
    @Override public void injectData(Map<String, Object> data) { }
    @Override public String getAccessTransformerClass() { return null; }
}
