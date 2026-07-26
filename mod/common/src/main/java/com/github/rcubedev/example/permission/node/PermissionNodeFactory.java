package com.github.rcubedev.example.permission.node;

@FunctionalInterface
public interface PermissionNodeFactory<T> {

    PermissionNode<T> create(String modId, String nodeName, PermissionNode.DefaultResolver<T> defaultResolver);
}
