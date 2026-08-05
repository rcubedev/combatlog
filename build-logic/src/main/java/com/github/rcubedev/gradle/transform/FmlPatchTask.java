package com.github.rcubedev.gradle.transform;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

@CacheableTask
public abstract class FmlPatchTask extends DefaultTask {

    @Classpath
    public abstract ConfigurableFileCollection getInputJar();

    @OutputFile
    public abstract RegularFileProperty getOutputJar();

    @Input
    public abstract Property<FmlPacker.InjectionType> getType();

    @Input
    public abstract Property<String> getModuleName();

    @Input
    public abstract Property<String> getOverrideModId();

    @Input
    public abstract Property<String> getOverrideDisplayName();

    @Input
    public abstract Property<String> getVersion();

    @Input
    public abstract Property<String> getArtifactName();

    @TaskAction
    public void run() {
        var input = getInputJar().getSingleFile();

        switch (getType().get()) {
            case MANIFEST_LIBRARY -> FmlPacker.patchManifestLibrary(
                    input,
                    getOutputJar().get().getAsFile(),
                    getModuleName().getOrNull()
            );

            case NEO_MOD_TOML -> FmlPacker.patchNeoModToml(
                    input,
                    getOutputJar().get().getAsFile(),
                    getOverrideModId().getOrNull(),
                    getOverrideDisplayName().getOrNull(),
                    getVersion().get(),
                    getArtifactName().get()
            );
        }
    }
}