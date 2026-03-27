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

package io.kamax.matrix.hs;

import com.google.gson.JsonObject;

import io.kamax.matrix.MatrixErrorInfo;
import io.kamax.matrix._MatrixContent;
import io.kamax.matrix._MatrixID;
import io.kamax.matrix._MatrixUserProfile;
import io.kamax.matrix.room.*;

import java.util.List;
import java8.util.Optional;

public interface _MatrixRoom {

    _MatrixHomeserver getHomeserver();

    String getAddress();

    Optional<String> getName();

    Optional<String> getTopic();

    Optional<String> getAvatarUrl();

    Optional<_MatrixContent> getAvatar();

    String getId();

    /**
     * Get the room version.
     * 
     * @return The room version as a string (e.g., "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
     */
    String getVersion();

    /**
     * Get a state event
     * 
     * @param type
     *            The type of state to look for
     * @return An optional JsonObject representing the content key of the event
     */
    Optional<JsonObject> getState(String type);

    /**
     * Get a state event
     * 
     * @param type
     *            The type of state to look for
     * @param key
     *            The state key to match
     * @return An optional JsonObject representing the content key of the event
     */
    Optional<JsonObject> getState(String type, String key);

    /**
     * Set room-specific account data.
     * 
     * @param type
     *            The type of account data to set
     * @param data
     *            The account data content
     */
    void setRoomAccountData(String type, JsonObject data);

    /**
     * Get room-specific account data.
     * 
     * @param type
     *            The type of account data to retrieve
     * @return The account data as a JSON object
     */
    JsonObject getRoomAccountData(String type);

    /**
     * Send a state event to a room.
     * 
     * @param type
     *            The type of state event to send
     * @param key
     *            The state key (use empty string for no key)
     * @param content
     *            The content of the state event
     * @return The event ID of the sent state event
     */
    String sendState(String type, String key, JsonObject content);

    /**
     * Redact an event in a room.
     * 
     * @param eventId
     *            The event ID to redact
     * @param reason
     *            Optional reason for redaction
     * @return The event ID of the redaction event
     */
    String redactEvent(String eventId, String reason);

    void join();

    void join(List<String> servers);

    Optional<MatrixErrorInfo> tryJoin();

    Optional<MatrixErrorInfo> tryJoin(List<String> servers);

    void leave();

    Optional<MatrixErrorInfo> tryLeave();

    void kick(_MatrixID user);

    void kick(_MatrixID user, String reason);

    Optional<MatrixErrorInfo> tryKick(_MatrixID user);

    Optional<MatrixErrorInfo> tryKick(_MatrixID user, String reason);

    String sendEvent(String type, JsonObject content);

    String sendText(String message);

    String sendFormattedText(String formatted, String rawFallback);

    String sendNotice(String message);

    String sendNotice(String formatted, String plain);

    /**
     * Send a receipt for an event
     * 
     * @param type
     *            The receipt type to send
     * @param eventId
     *            The Event ID targeted by the receipt
     */
    void sendReceipt(String type, String eventId);

    /**
     * Send a receipt for an event
     * 
     * @param type
     *            The receipt type to send
     * @param eventId
     *            The Event ID targeted by the receipt
     */
    void sendReceipt(ReceiptType type, String eventId);

    /**
     * Send a Read receipt for an event
     * 
     * @param eventId
     *            The Event ID targeted by the read receipt
     */
    void sendReadReceipt(String eventId);

    /**
     * Set fully read marker and/or read receipt.
     * 
     * @param fullyRead
     *            The event ID to set as fully read
     * @param readReceipt
     *            The event ID to set as read receipt
     */
    void setReadMarkers(String fullyRead, String readReceipt);

    void invite(_MatrixID mxId);

    List<_MatrixUserProfile> getJoinedUsers();

    /**
     * Get the list of members for a room.
     * 
     * @return The room members as a JSON object
     */
    JsonObject getMembers();

    /**
     * Get the list of local aliases for a room.
     * 
     * @return The room aliases as a JSON object containing an "aliases" array
     */
    JsonObject getAliases();

    /**
     * Get the list of joined members for a room.
     * 
     * @return The joined members as a JSON object containing a "joined" map
     */
    JsonObject getJoinedMembers();

    _MatrixRoomMessageChunk getMessages(_MatrixRoomMessageChunkOptions options);

    List<Tag> getAllTags();

    List<Tag> getUserTags();

    void addUserTag(String tag);

    void addUserTag(String tag, double order);

    void deleteUserTag(String tag);

    void addFavouriteTag();

    void addFavouriteTag(double order);

    void deleteFavouriteTag();

    Optional<Tag> getFavouriteTag();

    void addLowpriorityTag();

    void addLowpriorityTag(double order);

    Optional<Tag> getLowpriorityTag();

    void deleteLowpriorityTag();

    /**
     * Get the hierarchy of rooms (spaces).
     * 
     * @return The room hierarchy as a JSON object
     */
    JsonObject getHierarchy();

    /**
     * Get the hierarchy of rooms (spaces) with pagination.
     * 
     * @param from
     *            The room ID to start pagination from
     * @param limit
     *            Maximum number of rooms to return
     * @return The room hierarchy as a JSON object
     */
    JsonObject getHierarchy(Optional<String> from, Optional<Integer> limit);

    /**
     * Get threads in a room.
     * 
     * @return The threads as a JSON object
     */
    JsonObject getThreads();

    /**
     * Get threads in a room with pagination.
     * 
     * @param from
     *            The thread ID to start pagination from
     * @param limit
     *            Maximum number of threads to return
     * @return The threads as a JSON object
     */
    JsonObject getThreads(Optional<String> from, Optional<Integer> limit);

    /**
     * Get context around an event.
     * 
     * @param eventId
     *            The event ID to get context for
     * @param limit
     *            The maximum number of events to return around the event
     * @return The event context as a JSON object
     */
    JsonObject getEventContext(String eventId, int limit);

    /**
     * Upgrade a room to a new version.
     * 
     * @param newVersion
     *            The new room version to upgrade to
     * @return The ID of the new room
     */
    String upgradeRoom(String newVersion);

    /**
     * Set the user's typing state in the room.
     * 
     * @param typing
     *            Whether the user is typing
     * @param timeoutMs
     *            How long the typing state should persist (in milliseconds)
     */
    void setTyping(boolean typing, int timeoutMs);

    /**
     * Report an event as inappropriate.
     * 
     * @param eventId
     *            The event ID to report
     * @param score
     *            The score to rate the content (-100 to 100)
     * @param reason
     *            The reason for reporting
     */
    void reportEvent(String eventId, int score, String reason);

    /**
     * Ban a user from the room.
     * 
     * @param user
     *            The user to ban
     * @param reason
     *            Optional reason for banning
     */
    void ban(_MatrixID user, String reason);

    /**
     * Unban a user from the room.
     * 
     * @param user
     *            The user to unban
     */
    void unban(_MatrixID user);

    /**
     * Forget a room after leaving it.
     * This removes the room from the user's room list.
     */
    void forget();

    /**
     * Get a single event from a room.
     * 
     * @param eventId
     *            The event ID to retrieve
     * @return The event as a JSON object
     */
    JsonObject getEvent(String eventId);

    /**
     * Get relationships for an event.
     * 
     * @param eventId
     *            The event ID to get relationships for
     * @param relType
     *            Optional relationship type to filter by
     * @param eventType
     *            Optional event type to filter by
     * @return The relationships as a JSON object
     */
    JsonObject getRelations(String eventId, Optional<String> relType, Optional<String> eventType);

    /**
     * Get the event ID closest to a given timestamp.
     * 
     * @param timestamp
     *            The timestamp to search for
     * @param direction
     *            Optional direction to search ("f" for forwards, "b" for backwards)
     * @return The event ID and timestamp as a JSON object
     */
    JsonObject timestampToEvent(long timestamp, Optional<String> direction);

}
