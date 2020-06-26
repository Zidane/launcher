/*
 * This file is part of launcher, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.modlauncher;

import cpw.mods.modlauncher.Launcher;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import org.spongepowered.modlauncher.util.ArgumentList;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public final class Main {

    private static PluginEnvironment pluginEnvironment;

    public static void main(final String[] args) throws IOException {
        final OptionParser parser = new OptionParser();
        final ArgumentAcceptingOptionSpec<Path> gameDir = parser.accepts("gameDir", "Alternative game directory").withRequiredArg().withValuesConvertedBy(new PathConverter(PathProperties.DIRECTORY_EXISTING)).defaultsTo(Paths.get("."));
        parser.allowsUnrecognizedOptions();
        final OptionSet optionSet = parser.parse(args);

        final Path gameDirectory = optionSet.valueOf(gameDir);
        Main.pluginEnvironment = new PluginEnvironment();
        Main.pluginEnvironment.getBlackboard().getOrCreate(PluginKeys.VERSION, () -> PluginEnvironment.class.getPackage().getImplementationVersion());
        Main.pluginEnvironment.getBlackboard().getOrCreate(PluginKeys.BASE_DIRECTORY, () -> gameDirectory);
        // TODO Read in plugin directories from CLI/Config
        final Path modsDirectory = gameDirectory.resolve("mods");
        if (Files.notExists(modsDirectory)) {
            Files.createDirectories(modsDirectory);
        }
        final Path pluginsDirectory = gameDirectory.resolve("plugins");
        if (Files.notExists(pluginsDirectory)) {
            Files.createDirectories(pluginsDirectory);
        }
        Main.pluginEnvironment.getBlackboard().getOrCreate(PluginKeys.PLUGIN_DIRECTORIES, () -> Arrays.asList(modsDirectory, pluginsDirectory));

        final ArgumentList lst = ArgumentList.from(args);
        Launcher.main(lst.getArguments());
    }

    public static PluginEnvironment getLaunchPluginEnvironment() {
        return Main.pluginEnvironment;
    }
}
