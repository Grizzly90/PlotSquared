/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.listener;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.util.WEManager;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.lang.reflect.Field;
import java.util.Set;

public class ProcessedWEExtent extends AbstractDelegateExtent {

    private final Set<CuboidRegion> mask;
    private final String world;
    private final int max;
    int BScount = 0;
    int Ecount = 0;
    boolean BSblocked = false;
    boolean Eblocked = false;
    private int count;
    private Extent parent;

    public ProcessedWEExtent(String world, Set<CuboidRegion> mask, int max, Extent child,
        Extent parent) {
        super(child);
        this.mask = mask;
        this.world = world;
        if (max == -1) {
            max = Integer.MAX_VALUE;
        }
        this.max = max;
        this.count = 0;
        this.parent = parent;
    }

    @Override public BlockState getBlock(BlockVector3 position) {
        if (WEManager.maskContains(this.mask, position.getX(), position.getY(), position.getZ())) {
            return super.getBlock(position);
        }
        return WEExtent.AIRSTATE;
    }

    @Override public BaseBlock getFullBlock(BlockVector3 position) {
        if (WEManager.maskContains(this.mask, position.getX(), position.getY(), position.getZ())) {
            return super.getFullBlock(position);
        }
        return WEExtent.AIRBASE;
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block)
        throws WorldEditException {
        String id = block.getBlockType().getId();
        switch (id) {
            case "54":
            case "130":
            case "142":
            case "27":
            case "137":
            case "52":
            case "154":
            case "84":
            case "25":
            case "144":
            case "138":
            case "176":
            case "177":
            case "63":
            case "68":
            case "323":
            case "117":
            case "116":
            case "28":
            case "66":
            case "157":
            case "61":
            case "62":
            case "140":
            case "146":
            case "149":
            case "150":
            case "158":
            case "23":
            case "123":
            case "124":
            case "29":
            case "33":
            case "151":
            case "178":
                if (this.BSblocked) {
                    return false;
                }
                this.BScount++;
                if (this.BScount > Settings.Chunk_Processor.MAX_TILES) {
                    this.BSblocked = true;
                    PlotSquared.debug(
                        Captions.PREFIX + "&cDetected unsafe WorldEdit: " + location.getX() + ","
                            + location.getZ());
                }
                if (WEManager
                    .maskContains(this.mask, location.getX(), location.getY(), location.getZ())) {
                    if (this.count++ > this.max) {
                        if (this.parent != null) {
                            try {
                                Field field =
                                    AbstractDelegateExtent.class.getDeclaredField("extent");
                                field.setAccessible(true);
                                field.set(this.parent, new NullExtent());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            this.parent = null;
                        }
                        return false;
                    }
                    return super.setBlock(location, block);
                }
                break;
            default:
                if (WEManager
                    .maskContains(this.mask, location.getX(), location.getY(), location.getZ())) {
                    if (this.count++ > this.max) {
                        if (this.parent != null) {
                            try {
                                Field field =
                                    AbstractDelegateExtent.class.getDeclaredField("extent");
                                field.setAccessible(true);
                                field.set(this.parent, new NullExtent());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            this.parent = null;
                        }
                        return false;
                    }
                    super.setBlock(location, block);
                }
                return true;

        }
        return false;
    }

    @Override public Entity createEntity(Location location, BaseEntity entity) {
        if (this.Eblocked) {
            return null;
        }
        this.Ecount++;
        if (this.Ecount > Settings.Chunk_Processor.MAX_ENTITIES) {
            this.Eblocked = true;
            PlotSquared.debug(
                Captions.PREFIX + "&cDetected unsafe WorldEdit: " + location.getBlockX() + ","
                    + location.getBlockZ());
        }
        if (WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(),
            location.getBlockZ())) {
            return super.createEntity(location, entity);
        }
        return null;
    }

    @Override public boolean setBiome(BlockVector2 position, BiomeType biome) {
        return WEManager.maskContains(this.mask, position.getX(), position.getZ()) && super
            .setBiome(position, biome);
    }
}
