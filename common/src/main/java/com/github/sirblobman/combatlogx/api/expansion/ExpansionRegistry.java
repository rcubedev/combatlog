package com.github.sirblobman.combatlogx.api.expansion;

import com.github.sirblobman.combatlogx.expansion.ExpansionRegistryImpl;

public interface ExpansionRegistry {

    static ExpansionRegistry getInstance() {
        return ExpansionRegistryImpl.getInstance();
    }

    void registerExpansion(ExpansionFactory factory);
}
