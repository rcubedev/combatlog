package com.github.rcubedev.example.platform;

import com.github.rcubedev.example.permission.type.PermissionType;
import com.github.rcubedev.example.util.IService;

public interface IPermissionFactoryManager extends IService {

    static IPermissionFactoryManager getInstance() {
        return IService.createInstance(IPermissionFactoryManager.class);
    }

    <T> IPermissionNodeFactory<T> getFactory(PermissionType<T> type);
}
