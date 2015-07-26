////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualsites.commands.callers.CommandCaller;
import com.plotsquared.bukkit.util.UUIDHandler;


@CommandDeclaration(
        command = "purge",
        permission = "plots.admin",
        description = "Purge all plots for a world",
        category = CommandCategory.ACTIONS,
        requiredType = PS.class
)
public class Purge extends SubCommand {
    
    public PlotId getId(final String id) {
        try {
            final String[] split = id.split(";");
            return new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public boolean onCommand(final CommandCaller caller, final String[] args) {
        if (args.length == 1) {
            final String arg = args[0].toLowerCase();
            final PlotId id = getId(arg);
            if (id != null) {
                caller.message("/plot purxe x;z &l<world>");
                return false;
            }
            final UUID uuid = UUIDHandler.getUUID(args[0]);
            if (uuid != null) {
                caller.message("/plot purge " + args[0] + " &l<world>");
                return false;
            }
            if (arg.equals("player")) {
                caller.message("/plot purge &l<player> <world>");
                return false;
            }
            if (arg.equals("unowned")) {
                caller.message("/plot purge unowned &l<world>");
                return false;
            }
            if (arg.equals("unknown")) {
                caller.message("/plot purge unknown &l<world>");
                return false;
            }
            if (arg.equals("all")) {
                caller.message("/plot purge all &l<world>");
                return false;
            }
            caller.message(C.PURGE_SYNTAX);
            return false;
        }
        if (args.length != 2) {
            caller.message(C.PURGE_SYNTAX);
            return false;
        }
        final String worldname = args[1];
        if (!PS.get().getAllPlotsRaw().containsKey(worldname)) {
            caller.message("INVALID WORLD");
            return false;
        }
        final String arg = args[0].toLowerCase();
        final PlotId id = getId(arg);
        if (id != null) {
            final HashSet<Integer> ids = new HashSet<Integer>();
            final int DBid = DBFunc.getId(worldname, id);
            if (DBid != Integer.MAX_VALUE) {
                ids.add(DBid);
            }
            DBFunc.purgeIds(worldname, ids);
            return finishPurge(DBid == Integer.MAX_VALUE ? 1 : 0);
        }
        if (arg.equals("all")) {
            final Set<PlotId> ids = PS.get().getPlots(worldname).keySet();
            int length = ids.size();
            if (length == 0) {
                return MainUtil.sendMessage(null, "&cNo plots found");
            }
            DBFunc.purge(worldname, ids);
            return finishPurge(length);
        }
        if (arg.equals("unknown")) {
            final Collection<Plot> plots = PS.get().getPlots(worldname).values();
            final Set<PlotId> ids = new HashSet<>();
            for (final Plot plot : plots) {
                if (plot.owner != null) {
                    final String name = UUIDHandler.getName(plot.owner);
                    if (name == null) {
                        ids.add(plot.id);
                    }
                }
            }
            int length = ids.size();
            if (length == 0) {
                return MainUtil.sendMessage(null, "&cNo plots found");
            }
            DBFunc.purge(worldname, ids);
            return finishPurge(length);
        }
        if (arg.equals("unowned")) {
            final Collection<Plot> plots = PS.get().getPlots(worldname).values();
            final Set<PlotId> ids = new HashSet<>();
            for (final Plot plot : plots) {
                if (plot.owner == null) {
                    ids.add(plot.id);
                }
            }
            int length = ids.size();
            if (length == 0) {
                return MainUtil.sendMessage(null, "&cNo plots found");
            }
            DBFunc.purge(worldname, ids);
            return finishPurge(length);
        }
        final UUID uuid = UUIDHandler.getUUID(args[0]);
        if (uuid != null) {
            final Set<Plot> plots = PS.get().getPlots(worldname, uuid);
            final Set<PlotId> ids = new HashSet<>();
            for (final Plot plot : plots) {
                ids.add(plot.id);
            }
            int length = ids.size();
            DBFunc.purge(worldname, ids);
            return finishPurge(length);
        }
        caller.message(C.PURGE_SYNTAX);
        return false;
    }

    private boolean finishPurge(final int amount) {
        MainUtil.sendMessage(null, C.PURGE_SUCCESS, amount + "");
        return false;
    }
}