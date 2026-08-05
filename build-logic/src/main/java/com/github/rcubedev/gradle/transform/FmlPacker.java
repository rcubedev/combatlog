package com.github.rcubedev.gradle.transform;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.ProjectLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

//fixme this runs at cfg time, bad practice
public class FmlPacker {

    public enum InjectionType {
        MANIFEST_LIBRARY,
        NEO_MOD_TOML
    }

    public static File patchManifestLibrary(Configuration detached, ProjectLayout layout, String moduleName) {
        File originalJar = detached.getSingleFile();
        File modifiedJar = getOutputFile(layout, originalJar, "manifest-");

        if (!modifiedJar.exists()) {
            //System.out.println("PATCHING MANIFEST FOR NATIVE LOADING: " + originalJar.getName());

            runPackerEngine(originalJar, modifiedJar, manifest -> {
                Attributes attr = manifest.getMainAttributes();
                attr.put(new Attributes.Name("FMLModType"), "LIBRARY");
                if (moduleName != null && !moduleName.isEmpty()) attr.put(new Attributes.Name("Automatic-Module-Name"), moduleName);
            }, (srcJar, entry, jos) -> {
                if (JarFile.MANIFEST_NAME.equals(entry.getName())) {
                    return;
                }
                try (InputStream is = srcJar.getInputStream(entry)) {
                    is.transferTo(jos);
                }
            }, jos -> {});
        }
        return modifiedJar;
    }

    public static File patchNeoModToml(Configuration detached, ProjectLayout layout, String overrideModId, String overrideDisplayName) {
        File originalJar = detached.getSingleFile();
        File modifiedJar = getOutputFile(layout, originalJar, "toml-");

        if (!modifiedJar.exists()) {
            Dependency dependency = detached.getDependencies().iterator().next();
            String version = dependency.getVersion() != null ? dependency.getVersion() : "0.0.1-SNAPSHOT";
            String artifactName = dependency.getName();

            String modId = (overrideModId == null || overrideModId.isEmpty())
                    ? artifactName.toLowerCase().replace("-", "_")
                    : overrideModId;

            String displayName = (overrideDisplayName == null || overrideDisplayName.isEmpty())
                    ? capitalizeName(artifactName)
                    : overrideDisplayName;

            //System.out.println("INJECTING neoforge.mods.toml INTO: " + originalJar.getName() + " (v" + version + ")");

            String tomlTemplate =
                    "modLoader=\"javafml\"\n" +
                            "loaderVersion=\"[1,)\"\n" +
                            "license=\"Unknown\"\n\n" +
                            "[[mods]]\n" +
                            "modId=\"${modId}\"\n" +
                            "version=\"${version}\"\n" +
                            "displayName=\"${displayName}\"\n" +
                            "description=\"\"\n";

            String finalTomlContent = tomlTemplate
                    .replace("${modId}", modId)
                    .replace("${version}", version)
                    .replace("${displayName}", displayName);

            runPackerEngine(originalJar, modifiedJar, manifest -> {}, (srcJar, entry, jos) -> {
                String name = entry.getName();
                if (JarFile.MANIFEST_NAME.equals(name) ||
                        "META-INF/neoforge.mods.toml".equals(name) ||
                        "META-INF/mods.toml".equals(name)) {
                    return;
                }
                try (InputStream is = srcJar.getInputStream(entry)) {
                    is.transferTo(jos);
                }
            }, jos -> {
                jos.putNextEntry(new JarEntry("META-INF/neoforge.mods.toml"));
                jos.write(finalTomlContent.getBytes(StandardCharsets.UTF_8));
                jos.closeEntry();
            });
        }
        return modifiedJar;
    }

    private static String capitalizeName(String input) {
        String[] words = input.split("[-_]");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }

    private static File getOutputFile(ProjectLayout layout, File originalJar, String prefix) {
        File outputDir = layout.getBuildDirectory().dir("fml-patched").get().getAsFile();
        if (!outputDir.exists()) outputDir.mkdirs();
        return new File(outputDir, prefix + originalJar.getName());
    }

    private static void runPackerEngine(File source, File dest, ManifestModifier manifestModifier, JarEntryProcessor processor, JarFinishProcessor finisher) {
        try (JarFile sourceJar = new JarFile(source)) {
            Manifest manifest = sourceJar.getManifest();
            if (manifest == null) {
                manifest = new Manifest();
            }
            manifestModifier.modify(manifest);

            try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(dest), manifest)) {
                Enumeration<JarEntry> entries = sourceJar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (JarFile.MANIFEST_NAME.equals(entry.getName())) continue;

                    JarEntry newEntry = new JarEntry(entry.getName());
                    newEntry.setTime(entry.getTime());
                    newEntry.setExtra(entry.getExtra());
                    newEntry.setComment(entry.getComment());

                    jos.putNextEntry(newEntry);
                    processor.process(sourceJar, entry, jos);
                    jos.closeEntry();
                }
                finisher.finish(jos);
            }
        } catch (IOException e) {
            if (!source.exists()) {
                throw new IllegalStateException("FmlPacker input jar does not exist: " + source.getAbsolutePath(), e);
            }

            if (!source.isFile()) {
                throw new IllegalStateException("FmlPacker input is not a file: " + source.getAbsolutePath(), e);
            }

            if (!source.canRead()) {
                throw new IllegalStateException("FmlPacker cannot read input jar: " + source.getAbsolutePath(), e);
            }

            throw new RuntimeException("FmlPacker execution failed for: " + source.getName(), e);
        }
    }

    private interface ManifestModifier { void modify(Manifest manifest); }
    private interface JarEntryProcessor { void process(JarFile jar, JarEntry entry, JarOutputStream jos) throws IOException; }
    private interface JarFinishProcessor { void finish(JarOutputStream jos) throws IOException; }
}