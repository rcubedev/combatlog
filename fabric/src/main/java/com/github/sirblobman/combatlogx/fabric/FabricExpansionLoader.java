package com.github.sirblobman.combatlogx.fabric;

import com.github.sirblobman.combatlogx.api.expansion.ExpansionFactory;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionFactoryImpl;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionInitializer;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionMetadata;
import com.github.sirblobman.combatlogx.platform.IExpansionLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class FabricExpansionLoader implements IExpansionLoader {

    @Override
    public @NotNull List<ExpansionFactory> load() {
        List<EntrypointContainer<ExpansionInitializer>> containers = FabricLoader.getInstance()
                .getEntrypointContainers(ExpansionInitializer.ENTRYPOINT_KEY, ExpansionInitializer.class);

        List<ExpansionFactory> factories = new ArrayList<>(containers.size());
        for (EntrypointContainer<ExpansionInitializer> container : containers) {
            factories.add(createFactory(container));
        }
        return factories;
    }

    private ExpansionFactory createFactory(EntrypointContainer<ExpansionInitializer> container) {
        ExpansionInitializer initializer = container.getEntrypoint();
        ExpansionMetadata metadata = createMetadata(container.getProvider().getMetadata());

        return new ExpansionFactoryImpl(initializer, metadata);
    }

    private ExpansionMetadata createMetadata(ModMetadata meta) {
        return ExpansionMetadata.builder(meta.getName(), meta.getId(), meta.getVersion().getFriendlyString())
                .withDescription(meta.getDescription())
                .withAuthors(meta.getAuthors().stream().map(Person::getName).toList())
                .build();
    }
}
