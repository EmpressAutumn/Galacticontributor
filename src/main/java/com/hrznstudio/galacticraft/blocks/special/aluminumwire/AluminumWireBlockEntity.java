package com.hrznstudio.galacticraft.blocks.special.aluminumwire;

import com.hrznstudio.galacticraft.Galacticraft;
import com.hrznstudio.galacticraft.api.entity.WireBlockEntity;
import com.hrznstudio.galacticraft.blocks.machines.MachineBlockEntity;
import com.hrznstudio.galacticraft.energy.GalacticraftEnergy;
import com.hrznstudio.galacticraft.entity.GalacticraftBlockEntities;
import io.github.prospector.silk.util.ActionType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class AluminumWireBlockEntity extends BlockEntity implements Tickable, WireBlockEntity {
    private boolean tickedOnce = false;
    protected long networkId;

    public AluminumWireBlockEntity() {
        super(GalacticraftBlockEntities.ALUMINUM_WIRE_TYPE);
    }

    @Override
    public void tick() {
        if (!tickedOnce) {
            networkId = new WireNetwork(this).getId();
            tickedOnce = true;
            WireNetwork.blockPlaced();
        }
        Galacticraft.logger.info(networkId);
    }

    public void init() {
        networkId = new WireNetwork(this).getId(); //use id for easy replacement
        tickedOnce = true;
    }

    public long getNetworkId() {
        return networkId;
    }

    /*public boolean addToNetwork(BlockPos newWirePos) {
        for (BlockEntity wire : WireUtils.getAdjacentWires(pos, world)) {
            if (wire != null && wire.getPos() == newWirePos) {
                return WireUtils.getNetworkFromId(networkId).addWire(world, newWirePos);
            }
        }
        return false;
    }*/
}
