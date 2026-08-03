package com.github.rcubedev.example.neoforge;

import com.github.rcubedev.example.neoforge.permissions.node.factory.NeoForgePermissionNodeFactory;
import com.github.rcubedev.example.platform.PermissionFactoryManager;

public class NeoForgePermissionFactoryManager extends PermissionFactoryManager {

    public NeoForgePermissionFactoryManager() {
        super(NeoForgePermissionNodeFactory::new);
    }
}
