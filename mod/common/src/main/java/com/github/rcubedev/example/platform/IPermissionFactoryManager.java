package com.github.rcubedev.example.platform;

import com.github.rcubedev.example.permission.type.PermissionType;
import com.github.rcubedev.example.util.IService;

public interface IPermissionFactoryManager extends IService {

    /**
     * Permission factory manager instance getter.
     *
     * @return The permission factory manager instance.
     */
    static IPermissionFactoryManager getInstance() {
        return Holder.INSTANCE;
    }

    <T> IPermissionNodeFactory<T> getFactory(PermissionType<T> type);

    static class Holder {
        private static final IPermissionFactoryManager INSTANCE = IService.createInstance(IPermissionFactoryManager.class);
    }
}
