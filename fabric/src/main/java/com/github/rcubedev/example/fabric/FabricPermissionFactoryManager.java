package com.github.rcubedev.example.fabric;

import com.github.rcubedev.example.fabric.permissions.node.factory.FabricPermissionNodeFactory;
import com.github.rcubedev.example.platform.AbstractPermissionFactoryManager;

public class FabricPermissionFactoryManager extends AbstractPermissionFactoryManager {

    public FabricPermissionFactoryManager() {
        super(FabricPermissionNodeFactory::new);
    }
}
