/*
 * Copyright (c) 2019 HRZN LTD
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hrznstudio.galacticraft.network.packet;

import com.hrznstudio.galacticraft.Constants;
import com.hrznstudio.galacticraft.Galacticraft;
import com.hrznstudio.galacticraft.api.block.entity.ConfigurableElectricMachineBlockEntity;
import com.hrznstudio.galacticraft.api.configurable.SideOption;
import com.hrznstudio.galacticraft.blocks.machines.bubbledistributor.BubbleDistributorBlockEntity;
import net.fabricmc.fabric.impl.networking.ClientSidePacketRegistryImpl;
import net.fabricmc.fabric.impl.networking.ServerSidePacketRegistryImpl;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

/**
 * @author <a href="https://github.com/StellarHorizons">StellarHorizons</a>
 */
public class GalacticraftPackets {
    public static void register() {
        ServerSidePacketRegistryImpl.INSTANCE.register(new Identifier(Constants.MOD_ID, "redstone_update"), ((context, buf) -> {
            PacketByteBuf buffer = new PacketByteBuf(buf.copy());
            if (context.getPlayer() instanceof ServerPlayerEntity) {
                context.getPlayer().getServer().execute(() -> {
                    BlockEntity blockEntity = context.getPlayer().world.getBlockEntity(buffer.readBlockPos());
                    if (blockEntity instanceof ConfigurableElectricMachineBlockEntity) {
                        ((ConfigurableElectricMachineBlockEntity) blockEntity).setRedstoneState(buffer.readEnumConstant(ConfigurableElectricMachineBlockEntity.RedstoneState.class));
                    }
                });
            }
        }));

        ServerSidePacketRegistryImpl.INSTANCE.register(new Identifier(Constants.MOD_ID, "security_update"), ((context, buf) -> {
            PacketByteBuf buffer = new PacketByteBuf(buf.copy());
            if (context.getPlayer() instanceof ServerPlayerEntity) {
                context.getPlayer().getServer().execute(() -> {
                    BlockEntity blockEntity = ((ServerPlayerEntity) context.getPlayer()).getServerWorld().getBlockEntity(buffer.readBlockPos());
                    if (blockEntity instanceof ConfigurableElectricMachineBlockEntity) {
                        if (!((ConfigurableElectricMachineBlockEntity) blockEntity).getSecurity().hasOwner() ||
                                ((ConfigurableElectricMachineBlockEntity) blockEntity).getSecurity().getOwner().equals(context.getPlayer().getUuid())) {
                            ConfigurableElectricMachineBlockEntity.SecurityInfo.Publicity publicity = buffer.readEnumConstant(ConfigurableElectricMachineBlockEntity.SecurityInfo.Publicity.class);

                            ((ConfigurableElectricMachineBlockEntity) blockEntity).getSecurity().setOwner(context.getPlayer());
                            ((ConfigurableElectricMachineBlockEntity) blockEntity).getSecurity().setPublicity(publicity);
                        } else {
                            Galacticraft.logger.error("Received invaild security packet from: " + context.getPlayer().getEntityName());
                        }
                    }
                });
            }
        }));

        ServerSidePacketRegistryImpl.INSTANCE.register(new Identifier(Constants.MOD_ID, "side_config_update"), ((context, buf) -> {
            PacketByteBuf buffer = new PacketByteBuf(buf.copy());
            if (context.getPlayer() instanceof ServerPlayerEntity) {
                context.getPlayer().getServer().execute(() -> {
                    BlockPos pos = buffer.readBlockPos();
                    BlockEntity blockEntity = ((ServerPlayerEntity) context.getPlayer()).getServerWorld().getBlockEntity(pos);
                    if (blockEntity instanceof ConfigurableElectricMachineBlockEntity) {
                        String data = buffer.readString();
                        context.getPlayer().world.setBlockState(pos, context.getPlayer().world.getBlockState(pos)
                                .with(EnumProperty.of(data.split(",")[0], SideOption.class, SideOption.getApplicableValuesForMachine(context.getPlayer().world.getBlockState(pos).getBlock())),
                                        SideOption.valueOf(data.split(",")[1])));
                    }

                });
            }
        }));

        ClientSidePacketRegistryImpl.INSTANCE.register(new Identifier(Constants.MOD_ID, "bubble_size"), (packetContext, packetByteBuf) -> {
            PacketByteBuf buf = new PacketByteBuf(packetByteBuf.copy());
            MinecraftClient.getInstance().execute(() -> {
                BlockPos pos = buf.readBlockPos();
                if (packetContext.getPlayer().world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
                    if (packetContext.getPlayer().world.getBlockEntity(pos) instanceof BubbleDistributorBlockEntity) {
                        ((BubbleDistributorBlockEntity) packetContext.getPlayer().world.getBlockEntity(pos)).setSize(buf.readByte());
                    }
                }
            });
        });

        ServerSidePacketRegistryImpl.INSTANCE.register(new Identifier(Constants.MOD_ID, "bubble_max"), (context, packetByteBuf) -> {
            PacketByteBuf buf = new PacketByteBuf(packetByteBuf.copy());
            if (context.getPlayer() instanceof ServerPlayerEntity) {
                context.getPlayer().getServer().execute(() -> {
                    byte max = buf.readByte();
                    BlockPos pos = buf.readBlockPos();
                    if (context.getPlayer().world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
                        if (context.getPlayer().world.getBlockEntity(pos) instanceof BubbleDistributorBlockEntity) {
                            if (max > 0) {
                                ((BubbleDistributorBlockEntity) context.getPlayer().world.getBlockEntity(pos)).setMaxSize(max);
                            }
                        }
                    }
                });
            }
        });

        ServerSidePacketRegistryImpl.INSTANCE.register(new Identifier(Constants.MOD_ID, "bubble_visible"), (context, packetByteBuf) -> {
            PacketByteBuf buf = new PacketByteBuf(packetByteBuf.copy());
            if (context.getPlayer() instanceof ServerPlayerEntity) {
                context.getPlayer().getServer().execute(() -> {
                    boolean visible = buf.readBoolean();
                    BlockPos pos = buf.readBlockPos();
                    if (context.getPlayer().world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
                        if (context.getPlayer().world.getBlockEntity(pos) instanceof BubbleDistributorBlockEntity) {
                            ((BubbleDistributorBlockEntity) context.getPlayer().world.getBlockEntity(pos)).bubbleVisible = visible;
                        }
                    }
                });
            }
        });
    }
}
