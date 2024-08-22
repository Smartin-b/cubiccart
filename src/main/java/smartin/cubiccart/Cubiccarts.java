package smartin.cubiccart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartin.cubiccart.block.CopperRail;

import java.io.IOException;
import java.nio.file.*;

public class Cubiccarts implements ModInitializer {
    private static final String CONFIG_FOLDER_NAME = "config";
    private static final String CONFIG_FILE_NAME = "cubiccart.json";
    public static MinecraftServer server;

    public static Logger LOGGER = LoggerFactory.getLogger("cubiccart");

    public static final Block COPPER_RAIL = register(
            new CopperRail(AbstractBlock.Settings.create().noCollision().strength(0.7F).sounds(BlockSoundGroup.METAL)),
            "copper_rail",
            true
    );

    public static Block register(Block block, String name, boolean shouldRegisterItem) {
        // Register the block and its item.
        Identifier id = Identifier.of("cubiccart", name);

        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:air` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, id, blockItem);
        }

        return Registry.register(Registries.BLOCK, id, block);
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(serverInstance -> {
            server = serverInstance;
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Get or create the config directory
            Path configDir = Paths.get(server.getRunDirectory().toAbsolutePath().toString(), CONFIG_FOLDER_NAME);
            createDirectories(configDir);

            // Get the config file path
            Path configFilePath = configDir.resolve(CONFIG_FILE_NAME);

            // Load or create the config file
            Config.instance = loadOrCreateConfig(gson, configFilePath);

            // Set up file watcher to monitor changes to the config file
            setupFileWatcher(gson, configFilePath);
        });
    }

    private static void createDirectories(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.println("Failed to create config directory: " + e.getMessage());
        }
    }

    private static Config loadOrCreateConfig(Gson gson, Path configFilePath) {
        try {
            if (Files.exists(configFilePath)) {
                // Load existing config file
                return gson.fromJson(Files.readString(configFilePath), Config.class);
            } else {
                // Create default config and save it to file
                Config defaultConfig = new Config(); // Create default instance of your Config class
                Files.writeString(configFilePath, gson.toJson(defaultConfig));
                return defaultConfig;
            }
        } catch (JsonSyntaxException | IOException e) {
            System.err.println("Failed to load or create config file: " + e.getMessage());
            return new Config(); // Return default config in case of error
        }
    }


    private static void setupFileWatcher(Gson gson, Path configFilePath) {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            configFilePath.getParent().register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );

            new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key;
                        try {
                            key = watchService.take();
                        } catch (InterruptedException ex) {
                            return;
                        }

                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();

                            if (kind == StandardWatchEventKinds.OVERFLOW) {
                                continue;
                            }

                            // We only care about modifications to the specific config file
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path changed = ev.context();
                            if (changed.equals(configFilePath.getFileName())) {
                                // Re-load the config file
                                Config.instance = loadOrCreateConfig(gson, configFilePath);
                                updated();
                            }
                        }

                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }
                    }
                } catch (RuntimeException e) {
                    LOGGER.error("Error watching config file: ", e);
                }
            }).start();
        } catch (IOException e) {
            LOGGER.error("Failed to set up file watcher: ", e);
        }
    }

    private static void updated() {
        // Handle the updated configuration here
        LOGGER.info("Config file updated!");
        server.execute(() -> {
            server.sendMessage(Text.of("Updated config"));
        });
        // For example, you might reapply settings or notify other parts of your code
    }
}
