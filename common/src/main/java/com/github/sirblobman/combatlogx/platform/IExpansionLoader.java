package com.github.sirblobman.combatlogx.platform;

import com.github.rcubedev.example.util.IService;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IExpansionLoader extends IService {

    static IExpansionLoader getInstance() {
        return Holder.INSTANCE;
    }

    @NotNull List<ExpansionFactory> load();

    static class Holder {
        private static final IExpansionLoader INSTANCE = IService.createInstance(IExpansionLoader.class);
    }
}
