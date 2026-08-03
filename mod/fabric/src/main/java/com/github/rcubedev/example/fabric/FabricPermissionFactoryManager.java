package com.github.rcubedev.example.fabric;

import com.github.rcubedev.example.fabric.permissions.node.factory.FabricPermissionNodeFactory;
import com.github.rcubedev.example.platform.PermissionFactoryManager;

public class FabricPermissionFactoryManager extends PermissionFactoryManager {

    public FabricPermissionFactoryManager() {
        super(FabricPermissionNodeFactory::new);
    }
}
