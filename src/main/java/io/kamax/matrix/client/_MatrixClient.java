/*
 * matrix-java-sdk - Matrix Client SDK for Java
 * Copyright (C) 2017 Kamax Sarl
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

import com.google.gson.JsonObject;

import io.kamax.matrix._MatrixContent;
import io.kamax.matrix._MatrixID;
import io.kamax.matrix._MatrixUser;
import io.kamax.matrix.hs._MatrixRoom;
import io.kamax.matrix.room.RoomAlias;
import io.kamax.matrix.room._RoomAliasLookup;
import io.kamax.matrix.room._RoomCreationOptions;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java8.util.Optional;

public interface _MatrixClient extends _MatrixClientRaw {

    _MatrixID getWhoAmI();

    void setDisplayName(String name);

    _RoomAliasLookup lookup(RoomAlias alias);

    /**
     * Create a room alias.
     * 
     * @param alias
     *            The room alias to create
     * @param roomId
     *            The room ID to map the alias to
     */
    void createAlias(RoomAlias alias, String roomId);

    /**
     * Delete a room alias.
     * 
     * @param alias
     *            The room alias to delete
     */
    void deleteAlias(RoomAlias alias);

    _MatrixRoom createRoom(_RoomCreationOptions options);

    _MatrixRoom getRoom(String roomId);

    List<_MatrixRoom> getJoinedRooms();

    _MatrixRoom joinRoom(String roomIdOrAlias);

    /**
     * Knock on a room (request to join).
     * 
     * @param roomIdOrAlias
     *            The room ID or alias to knock on
     * @return The room that was knocked on
     */
    _MatrixRoom knockRoom(String roomIdOrAlias);

    /**
     * Knock on a room with specific servers.
     * 
     * @param roomIdOrAlias
     *            The room ID or alias to knock on
     * @param servers
     *            The servers to attempt to knock through
     * @return The room that was knocked on
     */
    _MatrixRoom knockRoom(String roomIdOrAlias, List<String> servers);

    _MatrixUser getUser(_MatrixID mxId);

    Optional<String> getDeviceId();

    /**
     * Get the list of public rooms.
     * 
     * @return The public rooms as a JSON object
     */
    JsonObject getPublicRooms();

    /**
     * Search public rooms.
     * 
     * @param filter
     *            The search filter as a JSON object
     * @return The search results as a JSON object
     */
    JsonObject searchPublicRooms(JsonObject filter);

    /**
     * Search for events.
     * 
     * @param searchCriteria
     *            The search criteria as a JSON object
     * @return The search results as a JSON object
     */
    JsonObject search(JsonObject searchCriteria);

    /**
     * Search the user directory.
     * 
     * @param searchTerm
     *            The term to search for
     * @param limit
     *            Maximum number of results to return
     * @return The search results as a JSON object
     */
    JsonObject searchUserDirectory(String searchTerm, int limit);

    /* Custom endpoint! */
    // TODO refactor into custom synapse class?
    void register(MatrixPasswordCredentials credentials, String sharedSecret, boolean admin);

    /**
     * Check if a username is available for registration.
     * 
     * @param username
     *            The username to check
     * @return True if the username is available, false otherwise
     */
    boolean checkUsernameAvailability(String username);

    /***
     * Set the access token to use for any authenticated API calls.
     * 
     * @param accessToken
     *            The access token provided by the server which must be valid
     * @throws MatrixClientRequestException
     *             If an error occurred while checking for the identity behind the access token
     */
    void setAccessToken(String accessToken) throws MatrixClientRequestException;

    void login(MatrixPasswordCredentials credentials);

    void logout();

    /**
     * Deactivate the current user's account.
     */
    void deactivateAccount();

    /**
     * Change the user's password.
     * 
     * @param newPassword
     *            The new password for the account
     * @param logoutDevices
     *            Whether to log out all other devices
     */
    void changePassword(String newPassword, boolean logoutDevices);

    /**
     * Refresh an access token.
     * 
     * @param refreshToken
     *            The refresh token
     * @return New access and refresh tokens
     */
    JsonObject refreshToken(String refreshToken);

    /**
     * Request an OpenID token for a user.
     * 
     * @return OpenID token object
     */
    JsonObject requestOpenIdToken();

    /**
     * Log out all of the user's devices.
     */
    void logoutAll();

    /**
     * Get a previously created filter.
     * 
     * @param filterId
     *            The filter ID to retrieve
     * @return The filter definition as a JSON object
     */
    JsonObject getFilter(String filterId);

    _SyncData sync(_SyncOptions options);

    /**
     * Download content from the media repository
     * 
     * @param mxUri
     *            The MXC URI for the content to download
     * @return The content
     * @throws IllegalArgumentException
     *             if the parameter is not a valid MXC URI
     */
    _MatrixContent getMedia(String mxUri) throws IllegalArgumentException;

    /**
     * Download content from the media repository
     * 
     * @param mxUri
     *            The MXC URI for the content to download
     * @return The content
     * @throws IllegalArgumentException
     *             if the parameter is not a valid MXC URI
     */
    _MatrixContent getMedia(URI mxUri) throws IllegalArgumentException;

    /**
     * Upload content to the media repository
     * 
     * @param data
     *            The data to send
     * @param type
     *            The mime-type of the content upload
     * @return The MXC URI for the uploaded content
     */
    String putMedia(byte[] data, String type);

    /**
     * Upload content to the media repository
     * 
     * @param data
     *            The data to send
     * @param type
     *            The mime-type of the content upload
     * @param filename
     *            A suggested filename for the content
     * @return The MXC URI for the uploaded content
     */
    String putMedia(byte[] data, String type, String filename);

    /**
     * Upload content to the media repository
     *
     * @param data
     *            The file to read the data from
     * @param type
     *            The mime-type of the content upload
     * @return The MXC URI for the uploaded content
     */
    String putMedia(File data, String type);

    /**
     * Upload content to the media repository
     * 
     * @param data
     *            The data to send
     * @param type
     *            The mime-type of the content upload
     * @param filename
     *            A suggested filename for the content
     * @return The MXC URI for the uploaded content
     */
    String putMedia(File data, String type, String filename);

    /**
     * Get media repository configuration.
     * 
     * @return The media configuration as a JSON object
     */
    JsonObject getMediaConfig();

    /**
     * Get a thumbnail of media.
     * 
     * @param mxUri
     *            The MXC URI for the media
     * @param width
     *            The desired width of the thumbnail
     * @param height
     *            The desired height of the thumbnail
     * @param method
     *            The resizing method ("crop" or "scale")
     * @return The thumbnail content
     * @throws IllegalArgumentException
     *             if the parameter is not a valid MXC URI
     */
    _MatrixContent getThumbnail(String mxUri, int width, int height, String method) throws IllegalArgumentException;

    /**
     * Get a thumbnail of media.
     * 
     * @param mxUri
     *            The MXC URI for the media
     * @param width
     *            The desired width of the thumbnail
     * @param height
     *            The desired height of the thumbnail
     * @param method
     *            The resizing method ("crop" or "scale")
     * @return The thumbnail content
     * @throws IllegalArgumentException
     *             if the parameter is not a valid MXC URI
     */
    _MatrixContent getThumbnail(URI mxUri, int width, int height, String method) throws IllegalArgumentException;

    /**
     * Get a preview of a URL.
     * 
     * @param url
     *            The URL to get a preview for
     * @param ts
     *            Optional timestamp for the URL
     * @return The URL preview information as a JSON object
     */
    JsonObject getUrlPreview(String url, Optional<Long> ts);

    /**
     * Get TURN server configuration for VoIP calls.
     * 
     * @return The TURN server configuration as a JSON object
     */
    JsonObject getTurnServer();

    List<JsonObject> getPushers();

    void setPusher(JsonObject pusher);

    void deletePusher(String pushKey);

    _GlobalPushRulesSet getPushRules();

    _PushRule getPushRule(String scope, String kind, String id);

    /**
     * Create or update a push rule.
     * 
     * @param scope
     *            The scope of the push rule (e.g., "global")
     * @param kind
     *            The kind of push rule (e.g., "override", "underride", "sender", "room", "content")
     * @param ruleId
     *            The rule ID
     * @param rule
     *            The complete push rule definition as a JSON object
     */
    void setPushRule(String scope, String kind, String ruleId, JsonObject rule);

    /**
     * Set the actions for a push rule.
     * 
     * @param scope
     *            The scope of the push rule (e.g., "global")
     * @param kind
     *            The kind of push rule (e.g., "override", "underride", "sender", "room", "content")
     * @param ruleId
     *            The rule ID
     * @param actions
     *            The actions to set for the rule as a JSON array
     */
    void setPushRuleActions(String scope, String kind, String ruleId, JsonObject actions);

    /**
     * Delete a push rule.
     * 
     * @param scope
     *            The scope of the push rule
     * @param kind
     *            The kind of push rule
     * @param ruleId
     *            The rule ID
     */
    void deletePushRule(String scope, String kind, String ruleId);

    /**
     * Enable or disable a push rule.
     * 
     * @param scope
     *            The scope of the push rule
     * @param kind
     *            The kind of push rule
     * @param ruleId
     *            The rule ID
     * @param enabled
     *            Whether the rule should be enabled
     */
    void setPushRuleEnabled(String scope, String kind, String ruleId, boolean enabled);

    /**
     * Get notifications.
     * 
     * @param from
     *            Optional pagination token
     * @param limit
     *            Maximum number of notifications to return
     * @param only
     *            Whether to only return notifications that have triggered a push
     * @return Notifications as a JSON object
     */
    JsonObject getNotifications(Optional<String> from, Optional<Integer> limit, Optional<Boolean> only);

    /**
     * Create a new filter.
     * 
     * @param filter
     *            The filter definition as a JSON object
     * @return The filter ID assigned by the server
     */
    String createFilter(JsonObject filter);

    /**
     * Get information about the feature support of the homeserver.
     * 
     * @return The capabilities of the homeserver as a JSON object
     */
    JsonObject getCapabilities();

    /**
     * Get the room versions supported by the homeserver.
     * 
     * @return A map where keys are room version identifiers (e.g., "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
     *         and values are their stability ("stable" or "unstable")
     */
    Map<String, String> getRoomVersions();

    /**
     * Get all devices for the current user.
     * 
     * @return List of devices as JSON objects
     */
    List<JsonObject> getDevices();

    /**
     * Get a specific device.
     * 
     * @param deviceId
     *            The device ID to retrieve
     * @return The device as a JSON object
     */
    JsonObject getDevice(String deviceId);

    /**
     * Update a device's display name.
     * 
     * @param deviceId
     *            The device ID to update
     * @param displayName
     *            The new display name for the device
     */
    void updateDevice(String deviceId, String displayName);

    /**
     * Delete a device.
     * 
     * @param deviceId
     *            The device ID to delete
     */
    void deleteDevice(String deviceId);

    /**
     * Delete multiple devices.
     * 
     * @param deviceIds
     *            List of device IDs to delete
     */
    void deleteDevices(List<String> deviceIds);

    /**
     * Upload end-to-end encryption keys.
     * 
     * @param keys
     *            The keys to upload, as a JSON object containing "device_keys" and/or "one_time_keys"
     * @return The response from the server, containing counts of uploaded keys
     */
    JsonObject uploadKeys(JsonObject keys);

    /**
     * Query end-to-end encryption keys.
     * 
     * @param request
     *            The key query request as a JSON object
     * @return The key query response as a JSON object
     */
    JsonObject queryKeys(JsonObject request);

    /**
     * Claim end-to-end encryption one-time keys.
     * 
     * @param request
     *            The key claim request as a JSON object
     * @return The key claim response as a JSON object
     */
    JsonObject claimKeys(JsonObject request);

    /**
     * Upload cross-signing keys.
     * 
     * @param keys
     *            The cross-signing keys to upload
     * @return The response from the server
     */
    JsonObject uploadSigningKeys(JsonObject keys);

    /**
     * Upload key signatures.
     * 
     * @param signatures
     *            The key signatures to upload
     * @return The response from the server
     */
    JsonObject uploadKeySignatures(JsonObject signatures);

    /**
     * Send to-device messages.
     * 
     * @param eventType
     *            The type of event to send
     * @param txnId
     *            Transaction ID for this request
     * @param messages
     *            Mapping from user ID to device ID to message content
     */
    void sendToDevice(String eventType, String txnId, JsonObject messages);

    /**
     * Set global account data.
     * 
     * @param type
     *            The type of account data to set
     * @param data
     *            The account data content
     */
    void setAccountData(String type, JsonObject data);

    /**
     * Get global account data.
     * 
     * @param type
     *            The type of account data to retrieve
     * @return The account data as a JSON object
     */
    JsonObject getAccountData(String type);

    /**
     * Get the user's third-party identifiers (3PIDs).
     * 
     * @return The 3PIDs as a JSON object containing a "threepids" array
     */
    JsonObject get3pids();

    /**
     * Add a third-party identifier (3PID) to the user's account.
     * 
     * @param threePid
     *            The 3PID object containing medium, address, and other parameters
     * @return The response from the server
     */
    JsonObject add3pid(JsonObject threePid);

    /**
     * Delete a third-party identifier (3PID) from the user's account.
     * 
     * @param medium
     *            The medium of the 3PID (e.g., "email", "msisdn")
     * @param address
     *            The address of the 3PID (e.g., "user@example.com")
     */
    void delete3pid(String medium, String address);

    /**
     * Bind a third-party identifier (3PID) to the user's account.
     * 
     * @param threePid
     *            The 3PID object containing medium, address, and other parameters
     * @return The response from the server
     */
    JsonObject bind3pid(JsonObject threePid);

    /**
     * Unbind a third-party identifier (3PID) from the user's account.
     * 
     * @param medium
     *            The medium of the 3PID (e.g., "email", "msisdn")
     * @param address
     *            The address of the 3PID (e.g., "user@example.com")
     */
    void unbind3pid(String medium, String address);

    /**
     * Request a verification token for a 3PID.
     * 
     * @param medium
     *            The medium of the 3PID (e.g., "email", "msisdn")
     * @param address
     *            The address of the 3PID (e.g., "user@example.com")
     * @param clientSecret
     *            A unique string generated by the client
     * @param sendAttempt
     *            The number of times the request has been attempted
     * @param nextLink
     *            Optional link to redirect after validation
     * @return The response from the server
     */
    JsonObject requestToken3pid(String medium, String address, String clientSecret, int sendAttempt,
            Optional<String> nextLink);

    /**
     * Create a new key backup version.
     * 
     * @param versionInfo
     *            The backup version information
     * @return The response containing the version ID
     */
    JsonObject createKeyBackupVersion(JsonObject versionInfo);

    /**
     * Get the current key backup version.
     * 
     * @return The current backup version information
     */
    JsonObject getKeyBackupVersion();

    /**
     * Update a key backup version.
     * 
     * @param version
     *            The version ID
     * @param versionInfo
     *            The backup version information
     */
    void updateKeyBackupVersion(String version, JsonObject versionInfo);

    /**
     * Delete a key backup version.
     * 
     * @param version
     *            The version ID
     */
    void deleteKeyBackupVersion(String version);

    /**
     * Get room keys from backup.
     * 
     * @param version
     *            Optional version ID (uses current version if empty)
     * @return The room keys as a JSON object
     */
    JsonObject getRoomKeys(Optional<String> version);

    /**
     * Set room keys in backup.
     * 
     * @param version
     *            The version ID
     * @param keys
     *            The room keys to store as a JSON object
     */
    void setRoomKeys(String version, JsonObject keys);

    /**
     * Delete room keys from backup.
     * 
     * @param version
     *            The version ID
     */
    void deleteRoomKeys(String version);

    /**
     * Set the user's presence.
     * 
     * @param presence
     *            The presence status
     * @param statusMsg
     *            Optional status message
     */
    void setPresence(PresenceStatus presence, String statusMsg);

    /**
     * Get the presence status of a user.
     * 
     * @param userId
     *            The user ID to get presence for
     * @return The presence status as a JSON object
     */
    JsonObject getPresence(String userId);

    /**
     * Get a summary of a room.
     * 
     * @param roomIdOrAlias
     *            The room ID or alias to get a summary for
     * @return The room summary as a JSON object
     */
    JsonObject getRoomSummary(String roomIdOrAlias);

    /**
     * Get the list of users whose devices have changed.
     * 
     * @param from
     *            The start token (paginated)
     * @param to
     *            The end token (paginated)
     * @return The key changes as a JSON object
     */
    JsonObject getKeyChanges(String from, String to);

}
