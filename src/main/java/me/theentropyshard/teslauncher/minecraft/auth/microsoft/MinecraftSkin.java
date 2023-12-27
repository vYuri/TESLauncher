/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023 TESLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.theentropyshard.teslauncher.minecraft.auth.microsoft;

public class MinecraftSkin {
    public String id;
    public String state;
    public String url;
    public String variant;
    public String alias;

    @Override
    public String toString() {
        return "MinecraftSkin{" +
                "id='" + this.id + '\'' +
                ", state='" + this.state + '\'' +
                ", url='" + this.url + '\'' +
                ", variant='" + this.variant + '\'' +
                ", alias='" + this.alias + '\'' +
                '}';
    }
}
