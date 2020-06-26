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
package org.spongepowered.launch;

import com.google.inject.Guice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.launch.plugin.DummyPluginContainer;
import org.spongepowered.launch.plugin.PluginLoader;
import org.spongepowered.launch.plugin.PluginManager;
import org.spongepowered.plugin.Blackboard;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;
import org.spongepowered.plugin.jvm.JVMPluginContainer;
import org.spongepowered.plugin.metadata.PluginContributor;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.nio.file.Path;
import java.util.List;

public abstract class Launcher {

    public static Launcher INSTANCE;

    private final Logger logger = LogManager.getLogger("Launcher");
    private final PluginEnvironment pluginEnvironment = new PluginEnvironment();
    private final PluginManager pluginManager = new PluginManager();

    protected Launcher() {
        INSTANCE = this;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public PluginEnvironment getPluginEnvironment() {
        return this.pluginEnvironment;
    }

    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    public static void launch(final String pluginSpiVersion, final Path baseDirectory, final List<Path> pluginDirectories, final String[] args) {
        Launcher.INSTANCE.launch0(pluginSpiVersion, baseDirectory, pluginDirectories, args);
    }

    protected void launch0(final String pluginSpiVersion, final Path baseDirectory, final List<Path> pluginDirectories, final String[] args) {
        this.populateBlackboard(pluginSpiVersion, baseDirectory, pluginDirectories);
        this.createInternalPlugins();
        this.loadPlugins();
    }

    protected void populateBlackboard(final String pluginSpiVersion, final Path baseDirectory, final List<Path> pluginDirectories) {
        final Blackboard blackboard = this.getPluginEnvironment().getBlackboard();
        blackboard.getOrCreate(PluginKeys.VERSION, () -> pluginSpiVersion);
        blackboard.getOrCreate(PluginKeys.BASE_DIRECTORY, () -> baseDirectory);
        blackboard.getOrCreate(PluginKeys.PLUGIN_DIRECTORIES, () -> pluginDirectories);
        blackboard.getOrCreate(PluginKeys.PARENT_INJECTOR, () -> Guice.createInjector(new LauncherModule()));
    }

    protected void loadPlugins() {
        final PluginLoader pluginLoader = new PluginLoader(this.pluginEnvironment, this.pluginManager);
        pluginLoader.discoverServices();
        pluginLoader.initialize();
        pluginLoader.discoverResources();
        pluginLoader.determineCandidates();
        pluginLoader.createContainers();
    }

    private void createInternalPlugins() {
        final Path gameDirectory = this.pluginEnvironment.getBlackboard().get(PluginKeys.BASE_DIRECTORY).get();
        this.pluginManager.addPlugin(this.createMinecraftPlugin(gameDirectory));
        PluginMetadata metadata = PluginMetadata.builder()
            .setId("launcher")
            .setName("Launcher")
            .setVersion("0.1")
            .setDescription("Testing Launcher for starting a Minecraft Client/Server with Mixin and Plugins.")
            .setMainClass("org.spongepowered.launch.Launcher")
            .contributor(PluginContributor.builder()
                .setName("Zidane")
                .setDescription("Lead Developer")
                .build())
            .build();
        this.pluginManager.addPlugin(new DummyPluginContainer(metadata, gameDirectory, this.getLogger(), this));
    }

    protected abstract PluginContainer createMinecraftPlugin(final Path gameDirectory);
}
