/*
 * matrix-java-sdk - Matrix Client SDK for Java
 * Copyright (C) 2017 Kamax Sarl
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package io.kamax.matrix.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class RoomMessageFormattedTextPutBodyTest {

    public static void main(String[] args) {
        String rawFallback = "test";
        String html = "<img src=\"mxc://matrix.local/vaqAwlPGRjSoyCjUuGVAxhHc\" width=\"16\" height=\"16\" style=\"vertical-align:middle; margin-right: 4px; border-radius: 2px;\" alt=\"Raremote\" title=\"Raremote\" data-mx-emoticon=\"true\" />";
        RoomMessageFormattedTextPutBody body = new RoomMessageFormattedTextPutBody(rawFallback, html);
        
        JsonObject json = GsonUtil.makeObj(body);
        System.out.println("Generated JSON:");
        System.out.println(new Gson().toJson(json));
        
        System.out.println("\nformatted_body value:");
        System.out.println(json.get("formatted_body").getAsString());
    }

    @Test
    public void testDataMxEmoticonPreserved() {
        String rawFallback = "test";
        String html = "<img src=\"mxc://matrix.local/vaqAwlPGRjSoyCjUuGVAxhHc\" width=\"16\" height=\"16\" style=\"vertical-align:middle; margin-right: 4px; border-radius: 2px;\" alt=\"Raremote\" title=\"Raremote\" data-mx-emoticon=\"true\" />";
        RoomMessageFormattedTextPutBody body = new RoomMessageFormattedTextPutBody(rawFallback, html);
        
        JsonObject json = GsonUtil.makeObj(body);
        System.out.println("DEBUG JSON: " + json);
        assertTrue(json.has("formatted_body"));
        assertEquals(html, json.get("formatted_body").getAsString());
        assertTrue(json.has("format"));
        assertEquals("org.matrix.custom.html", json.get("format").getAsString());
        assertTrue(json.has("body"));
        assertEquals(rawFallback, json.get("body").getAsString());
        assertTrue(json.has("msgtype"));
        assertEquals("m.text", json.get("msgtype").getAsString());
    }

    @Test
    public void testHtmlEscaping() {
        String rawFallback = "test";
        String html = "<span data-test=\"value\">text</span>";
        RoomMessageFormattedTextPutBody body = new RoomMessageFormattedTextPutBody(rawFallback, html);
        
        JsonObject json = GsonUtil.makeObj(body);
        String formattedBody = json.get("formatted_body").getAsString();
        assertTrue(formattedBody.contains("<span"));
        assertTrue(formattedBody.contains("</span>"));
        assertTrue(formattedBody.contains("data-test=\"value\""));
    }
}