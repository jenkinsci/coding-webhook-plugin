/**
 * Jenkins plugin for Coding https://coding.net
 *
 * Copyright (c) 2016-2018 Shuanglei Tao <tsl0922@gmail.com>
 * Copyright (c) 2016-present, Coding, Inc.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.coding.jenkins.plugin.common.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.Map;

public class JSON {

    public static Gson getGson() {
        return getGsonBuilder().create();
    }

    public static GsonBuilder getGsonBuilder() {
        return new GsonBuilder();
    }

    public static String toJson(Object obj) {
        return getGson().toJson(obj);
    }

    public static Map fromJson(String json) {
        return getGson().fromJson(json, Map.class);
    }

    // Note: GSON fromJson is written the same way.
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public static <T> T fromJson(String json, Type typeOfT) {
        return getGson().fromJson(json, typeOfT);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return getGson().fromJson(json, classOfT);
    }

    public static <T> T fromJson(JsonElement json, Class<T> classOfT) {
        return getGson().fromJson(json, classOfT);
    }
}
