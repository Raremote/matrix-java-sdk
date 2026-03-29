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

package io.kamax.matrix.client;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Rule;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.*;

public class MatrixHttpRoomEmoticonTest extends MatrixHttpTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(port));

    private String roomId = "!testroom:localhost";
    private String eventId = "YUwRidLecu";

    @Test
    public void testSendEmoticon() throws URISyntaxException {
        // Setup WireMock to capture the request
        stubFor(post(urlPathMatching("/_matrix/client/v3/rooms/" + roomId + "/send/m.room.message/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"event_id\": \"" + eventId + "\"}")));

        // Create a mock client context
        MatrixClientContext context = new MatrixClientContext(new io.kamax.matrix.hs.MatrixHomeserver(domain, baseUrl));
        context.setToken(testToken);
        MatrixHttpRoom room = new MatrixHttpRoom(context, roomId);

        // Send emoticon
        String rawFallback = "Raremote";
        String html = "<img src=\"mxc://matrix.local/vaqAwlPGRjSoyCjUuGVAxhHc\" width=\"16\" height=\"16\" style=\"vertical-align:middle; margin-right: 4px; border-radius: 2px;\" alt=\"Raremote\" title=\"Raremote\" data-mx-emoticon=\"true\" />";
        String result = room.sendFormattedText(html, rawFallback);

        // Verify request was made
        verify(postRequestedFor(urlPathMatching("/_matrix/client/v3/rooms/" + roomId + "/send/m.room.message/[0-9]+")));

        // Get the actual request body
        List<ServeEvent> events = WireMock.getAllServeEvents();
        if (!events.isEmpty()) {
            String body = events.get(0).getRequest().getBodyAsString();
            System.out.println("Request body: " + body);
            
            // Parse JSON
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            assertTrue(json.has("formatted_body"));
            String formattedBody = json.get("formatted_body").getAsString();
            assertTrue("formatted_body should contain data-mx-emoticon", formattedBody.contains("data-mx-emoticon"));
            assertTrue("formatted_body should contain the exact src", formattedBody.contains("mxc://matrix.local/vaqAwlPGRjSoyCjUuGVAxhHc"));
            assertTrue("formatted_body should contain alt", formattedBody.contains("alt=\"Raremote\""));
            assertTrue("formatted_body should contain title", formattedBody.contains("title=\"Raremote\""));
            assertTrue("formatted_body should contain height", formattedBody.contains("height=\"16\""));
            
            // Check format field
            assertEquals("org.matrix.custom.html", json.get("format").getAsString());
            assertEquals("m.text", json.get("msgtype").getAsString());
            assertEquals(rawFallback, json.get("body").getAsString());
        }
        
        assertEquals(eventId, result);
    }
}