package com.grinderwolf.swm.nms.v1_16_R1;

import com.grinderwolf.swm.clsm.CLSMBridge;
import com.grinderwolf.swm.clsm.ClassModifier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_16_R1.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CraftCLSMBridge implements CLSMBridge {

    private static final Logger LOGGER = LogManager.getLogger("SWM Chunk Loader");

    private final v1_16_R1SlimeNMS nmsInstance;

    @Override
    public Object getChunk(Object worldObject, int x, int z) {
        if (!(worldObject instanceof CustomWorldServer)) {
            System.out.println("world is of type " + worldObject.getClass().getName());

            return null; // Returning null will just run the original getChunk method
        }
        System.out.println("Get chunk");

        CustomWorldServer world = (CustomWorldServer) worldObject;

        return world.getChunk(x, z);
    }

    @Override
    public boolean saveChunk(Object world, Object chunkAccess) {
        if (!(world instanceof CustomWorldServer)) {
            return false; // Returning false will just run the original saveChunk method
        }

        if (!(chunkAccess instanceof ProtoChunkExtension || chunkAccess instanceof Chunk) || !((IChunkAccess) chunkAccess).isNeedsSaving()) {
            // We're only storing fully-loaded chunks that need to be saved
            return true;
        }

        Chunk chunk;

        if (chunkAccess instanceof ProtoChunkExtension) {
            chunk = ((ProtoChunkExtension) chunkAccess).u();
        } else {
            chunk = (Chunk) chunkAccess;
        }


        ((CustomWorldServer) world).saveChunk(chunk);
        chunk.setNeedsSaving(false);

        return true;
    }

    @Override
    public boolean isCustomWorld(Object world) {
        return world instanceof CustomWorldServer;
    }

    @Override
    public boolean skipWorldAdd(Object world) {
        if (!isCustomWorld(world)) {
            return false;
        }

        CustomWorldServer worldServer = (CustomWorldServer) world;
        return !worldServer.isReady();
    }

    static void initialize(v1_16_R1SlimeNMS instance) {
        Bukkit.broadcastMessage("registering CLSM bridge");

        ClassModifier.setLoader(new CraftCLSMBridge(instance));
    }
}
