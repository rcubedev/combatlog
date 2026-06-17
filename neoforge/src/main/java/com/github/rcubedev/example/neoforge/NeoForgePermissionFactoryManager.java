package com.github.rcubedev.example.neoforge;

import com.github.rcubedev.example.neoforge.permissions.node.factory.NeoForgePermissionNodeFactory;
import com.github.rcubedev.example.platform.AbstractPermissionFactoryManager;

public class NeoForgePermissionFactoryManager extends AbstractPermissionFactoryManager {

    public NeoForgePermissionFactoryManager() {
        super(NeoForgePermissionNodeFactory::new);
    }
}
