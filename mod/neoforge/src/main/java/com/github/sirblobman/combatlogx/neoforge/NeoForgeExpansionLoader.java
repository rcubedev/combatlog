package com.github.sirblobman.combatlogx.neoforge;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.expansion.*;
import com.github.sirblobman.combatlogx.neoforge.api.CLXExpansion;
import com.github.sirblobman.combatlogx.platform.IExpansionLoader;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.neoforged.neoforgespi.locating.IModFile;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

// fixme match FML more to prob fix issues; untested
public final class NeoForgeExpansionLoader implements IExpansionLoader {

    private record ModContext<T>(IModInfo info, T context) {}

    @Override
    public @NotNull List<ExpansionFactory> load() {
        List<ExpansionFactory> factories = new ArrayList<>();

        Set<ModContext<IModFile>> files = ModList.get().getMods().stream()
                .map(i -> new ModContext<>(i, i.getOwningFile()))
                .filter(c -> c.context() != null)
                .map(c -> new ModContext<>(c.info(), c.context().getFile()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (ModContext<IModFile> context : files) {
            ModFileScanData scanData = context.context().getScanResult();
            factories.addAll(findExpansionInitializers(context, scanData));
            factories.addAll(findExpansionAnnotations(context, scanData));
        }
        return factories;
//        ModList.get().getAllScanData().forEach(data -> {
//            data.get
//        });
    }

    private static List<ExpansionFactory> findExpansionInitializers(ModContext<IModFile> context, ModFileScanData scanData) {
        List<ExpansionFactory> factories = new ArrayList<>();

        IModInfo info = context.info();
        IModFile fileInfo = context.context();

        Type expansionInitializerType = Type.getType(ExpansionInitializer.class);

        for (ModFileScanData.ClassData classData : scanData.getClasses()) {
            if (!classData.interfaces().contains(expansionInitializerType)) continue;

            Class<?> clazz = loadClass(fileInfo, classData.clazz().getClassName());
            if (!ExpansionInitializer.class.isAssignableFrom(clazz))
                throw new IllegalStateException(clazz.getName()
                        + " was found as an " + expansionInitializerType.getClassName() + " but does not implement "
                        + ExpansionInitializer.class.getName());

            @SuppressWarnings("unchecked")
            Class<? extends ExpansionInitializer> initializerClass = (Class<? extends ExpansionInitializer>) clazz;

            ExpansionInitializer initializer = createInitializer(initializerClass);
            ExpansionFactory factory = new ExpansionFactoryImpl(initializer, createMetadata(info));
            factories.add(factory);
        }
        return factories;
    }

    private static List<ExpansionFactory> findExpansionAnnotations(ModContext<IModFile> context, ModFileScanData scanData) {
        List<ExpansionFactory> factories = new ArrayList<>();

        IModInfo info = context.info();
        IModFile fileInfo = context.context();

        scanData.getAnnotatedBy(CLXExpansion.class, ElementType.TYPE)
                .filter(data -> info.getModId().equals(data.annotationData().get("value")))
                .map(ad -> ad.clazz().getClassName())
                .forEach(className -> {
                    Class<?> clazz = loadClass(fileInfo, className);
                    if (!Expansion.class.isAssignableFrom(clazz))
                        throw new IllegalStateException(clazz + " annotated with " + CLXExpansion.class.getName()
                                + " but is not an " + Expansion.class.getName() + " subtype");

                    @SuppressWarnings("unchecked")
                    Class<? extends Expansion> expansionClass = (Class<? extends Expansion>) clazz;

                    ExpansionFactory factory = createFactory(expansionClass, createMetadata(info));
                    factories.add(factory);
        });

        return factories;
    }

    private static @NotNull ExpansionInitializer createInitializer(Class<? extends ExpansionInitializer> clazz) {

        Constructor<? extends ExpansionInitializer> constructor;
        try {
             constructor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Expansion initializer " + clazz + " must have a public no-arg constructor.");
        }

        ExpansionInitializer initializer;
        try {
            initializer = constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            Throwable failure = e;
            if (e instanceof InvocationTargetException) failure = e.getCause();
            throw new RuntimeException(failure);
        }
        return initializer;
    }

    private static @NotNull ExpansionFactory createFactory(Class<? extends Expansion> clazz, ExpansionMetadata metadata) {

        Constructor<? extends Expansion> constructor;
        Class<?>[] args = { ICombatLogX.class, ExpansionMetadata.class };
        try {
            constructor = clazz.getConstructor(args);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Expansion " + clazz + " must have a public constructor with args: " + Arrays.toString(args));
        }

        //fixme remove anon class
        return new ExpansionFactory() {

            Expansion expansion;

            @Override
            public @NotNull Expansion create(ICombatLogX api) {
                if (expansion != null) return expansion;

                try {
                    expansion = constructor.newInstance(api, metadata);
                } catch (ReflectiveOperationException e) {
                    Throwable failure = e;
                    if (e instanceof InvocationTargetException) failure = e.getCause();
                    throw new RuntimeException(failure);
                }
                return expansion;
            }
        };
    }

    private static Class<?> loadClass(IModFile file, String className) {
        String moduleName = file./*? if >=1.21.10 {*/ /*getId()*/ /*?} else {*/ getModFileInfo().moduleName() /*?}*/;
        Module module = FMLLoader
                //? if >=1.21.10
                /*.getCurrent()*/
                .getGameLayer()
                .findModule(moduleName)
                .orElseThrow();

        Class<?> clazz = Class.forName(module, className);
        if (clazz == null) {
            throw new IllegalStateException("Could not load class " + className);
        }

        return clazz;
    }

    private static ExpansionMetadata createMetadata(IModInfo info) {
        String authors = info.getConfig().getConfigElement("authors").map(Object::toString).orElse(null);
        return ExpansionMetadata.builder(info.getDisplayName(), info.getModId(), info.getVersion().toString())
                .withDescription(info.getDescription())
                .withAuthors(authors == null ? List.of() : List.of(authors))
                .build();
    }
}
