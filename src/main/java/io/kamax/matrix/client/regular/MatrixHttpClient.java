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

package io.kamax.matrix.client.regular;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import io.kamax.matrix.MatrixID;
import io.kamax.matrix._MatrixContent;
import io.kamax.matrix._MatrixID;
import io.kamax.matrix._MatrixUser;
import io.kamax.matrix.client.*;
import io.kamax.matrix.hs._MatrixRoom;
import io.kamax.matrix.json.*;
import io.kamax.matrix.room.RoomAlias;
import io.kamax.matrix.room.RoomAliasLookup;
import io.kamax.matrix.room._RoomAliasLookup;
import io.kamax.matrix.room._RoomCreationOptions;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java8.util.Optional;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

import okhttp3.*;

public class MatrixHttpClient extends AMatrixHttpClient implements _MatrixClient {

    private Logger log = LoggerFactory.getLogger(MatrixHttpClient.class);

    public MatrixHttpClient(String domain) {
        super(domain);
    }

    public MatrixHttpClient(URL hsBaseUrl) {
        super(hsBaseUrl);
    }

    public MatrixHttpClient(MatrixClientContext context) {
        super(context);
    }

    public MatrixHttpClient(MatrixClientContext context, OkHttpClient.Builder client) {
        super(context, client);
    }

    public MatrixHttpClient(MatrixClientContext context, OkHttpClient.Builder client, MatrixClientDefaults defaults) {
        super(context, client, defaults);
    }

    public MatrixHttpClient(MatrixClientContext context, OkHttpClient client) {
        super(context, client);
    }

    protected _MatrixID getIdentity(String token) {
        URL path = getClientPath("account", "whoami");
        String body = executeAuthenticated(new Request.Builder().get().url(path), token);
        return MatrixID.from(GsonUtil.getStringOrThrow(GsonUtil.parseObj(body), "user_id")).acceptable();
    }

    @Override
    public _MatrixID getWhoAmI() {
        URL path = getClientPath("account", "whoami");
        String body = executeAuthenticated(new Request.Builder().get().url(path));
        return MatrixID.from(GsonUtil.getStringOrThrow(GsonUtil.parseObj(body), "user_id")).acceptable();
    }

    @Override
    public void setDisplayName(String name) {
        URL path = getClientPath("profile", getUserId(), "displayname");
        execute(new Request.Builder().put(getJsonBody(new UserDisplaynameSetBody(name))).url(path));
    }

    @Override
    public _RoomAliasLookup lookup(RoomAlias alias) {
        URL path = getClientPath("directory", "room", alias.getId());
        String resBody = execute(new Request.Builder().get().url(path));
        RoomAliasLookupJson lookup = GsonUtil.get().fromJson(resBody, RoomAliasLookupJson.class);
        return new RoomAliasLookup(lookup.getRoomId(), alias.getId(), lookup.getServers());
    }

    @Override
    public void createAlias(RoomAlias alias, String roomId) {
        URL path = getClientPath("directory", "room", alias.getId());
        JsonObject body = new JsonObject();
        body.addProperty("room_id", roomId);
        executeAuthenticated(new Request.Builder().put(getJsonBody(body)).url(path));
    }

    @Override
    public void deleteAlias(RoomAlias alias) {
        URL path = getClientPath("directory", "room", alias.getId());
        executeAuthenticated(new Request.Builder().delete().url(path));
    }

    @Override
    public _MatrixRoom createRoom(_RoomCreationOptions options) {
        URL path = getClientPath("createRoom");
        String resBody = executeAuthenticated(
                new Request.Builder().post(getJsonBody(new RoomCreationRequestJson(options))).url(path));
        String roomId = GsonUtil.get().fromJson(resBody, RoomCreationResponseJson.class).getRoomId();
        return getRoom(roomId);
    }

    @Override
    public _MatrixRoom getRoom(String roomId) {
        return new MatrixHttpRoom(getContext(), roomId);
    }

    @Override
    public List<_MatrixRoom> getJoinedRooms() {
        URL path = getClientPath("joined_rooms");
        JsonObject resBody = GsonUtil.parseObj(executeAuthenticated(new Request.Builder().get().url(path)));
        return StreamSupport.stream(GsonUtil.asList(resBody, "joined_rooms", String.class)).map(this::getRoom)
                .collect(Collectors.toList());
    }

    @Override
    public _MatrixRoom joinRoom(String roomIdOrAlias) {
        URL path = getClientPath("join", roomIdOrAlias);
        String resBody = executeAuthenticated(new Request.Builder().post(getJsonBody(new JsonObject())).url(path));
        String roomId = GsonUtil.get().fromJson(resBody, RoomCreationResponseJson.class).getRoomId();
        return getRoom(roomId);
    }

    @Override
    public _MatrixRoom knockRoom(String roomIdOrAlias) {
        return knockRoom(roomIdOrAlias, Collections.emptyList());
    }

    @Override
    public _MatrixRoom knockRoom(String roomIdOrAlias, List<String> servers) {
        HttpUrl.Builder b = getClientPathBuilder("knock", roomIdOrAlias);
        servers.forEach(server -> b.addQueryParameter("server_name", server));
        String resBody = executeAuthenticated(new Request.Builder().post(getJsonBody(new JsonObject())).url(b.build()));
        String roomId = GsonUtil.get().fromJson(resBody, RoomCreationResponseJson.class).getRoomId();
        return getRoom(roomId);
    }

    @Override
    public _MatrixUser getUser(_MatrixID mxId) {
        return new MatrixHttpUser(getContext(), mxId);
    }

    @Override
    public Optional<String> getDeviceId() {
        return Optional.ofNullable(context.getDeviceId());
    }

    @Override
    public JsonObject getPublicRooms() {
        URL path = getClientPath("publicRooms");
        String body = execute(new Request.Builder().get().url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public JsonObject searchPublicRooms(JsonObject filter) {
        URL path = getClientPath("publicRooms");
        String body = execute(new Request.Builder().post(getJsonBody(filter)).url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public JsonObject getRoomSummary(String roomIdOrAlias) {
        URL path = getClientV1Path("room_summary", roomIdOrAlias);
        String body = executeAuthenticated(new Request.Builder().get().url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public JsonObject search(JsonObject searchCriteria) {
        URL path = getClientPath("search");
        String body = executeAuthenticated(new Request.Builder().post(getJsonBody(searchCriteria)).url(path));
        return GsonUtil.parseObj(body);
    }

    protected void updateContext(String resBody) {
        LoginResponse response = gson.fromJson(resBody, LoginResponse.class);
        context.setToken(response.getAccessToken());
        context.setDeviceId(response.getDeviceId());
        context.setUser(MatrixID.asAcceptable(response.getUserId()));
    }

    @Override
    public void register(MatrixPasswordCredentials credentials, String sharedSecret, boolean admin) {
        // As per synapse registration script:
        // https://github.com/matrix-org/synapse/blob/master/scripts/register_new_matrix_user#L28

        String value = credentials.getLocalPart() + "\0" + credentials.getPassword() + "\0"
                + (admin ? "admin" : "notadmin");
        String mac = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, sharedSecret).hmacHex(value);
        JsonObject body = new JsonObject();
        body.addProperty("user", credentials.getLocalPart());
        body.addProperty("password", credentials.getPassword());
        body.addProperty("mac", mac);
        body.addProperty("type", "org.matrix.login.shared_secret");
        body.addProperty("admin", false);
        URL url = getPath("client", "api", "v1", "register");
        updateContext(execute(new Request.Builder().post(getJsonBody(body)).url(url)));
    }

    @Override
    public void setAccessToken(String accessToken) {
        context.setUser(getIdentity(accessToken));
        context.setToken(accessToken);
    }

    @Override
    public void login(MatrixPasswordCredentials credentials) {
        URL url = getClientPath("login");

        LoginPostBody data = new LoginPostBody(credentials.getLocalPart(), credentials.getPassword());
        getDeviceId().ifPresent(data::setDeviceId);
        Optional.ofNullable(context.getInitialDeviceName()).ifPresent(data::setInitialDeviceDisplayName);

        updateContext(execute(new Request.Builder().post(getJsonBody(data)).url(url)));
    }

    @Override
    public void logout() {
        URL path = getClientPath("logout");
        executeAuthenticated(new Request.Builder().post(getJsonBody(new JsonObject())).url(path));
        context.setToken(null);
        context.setUser(null);
        context.setDeviceId(null);
    }

    @Override
    public void deactivateAccount() {
        URL path = getClientPath("account", "deactivate");
        executeAuthenticated(new Request.Builder().post(getJsonBody(new JsonObject())).url(path));
    }

    @Override
    public void setAccountData(String type, JsonObject data) {
        URL path = getClientPath("user", getUserId(), "account_data", type);
        executeAuthenticated(new Request.Builder().put(getJsonBody(data)).url(path));
    }

    @Override
    public JsonObject getAccountData(String type) {
        URL path = getClientPath("user", getUserId(), "account_data", type);
        String body = executeAuthenticated(new Request.Builder().get().url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public JsonObject get3pids() {
        URL path = getClientPath("account", "3pid");
        String body = executeAuthenticated(new Request.Builder().get().url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public JsonObject add3pid(JsonObject threePid) {
        URL path = getClientPath("account", "3pid");
        String body = executeAuthenticated(new Request.Builder().post(getJsonBody(threePid)).url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public void delete3pid(String medium, String address) {
        URL path = getClientPath("account", "3pid", "delete");
        JsonObject body = new JsonObject();
        body.addProperty("medium", medium);
        body.addProperty("address", address);
        executeAuthenticated(new Request.Builder().post(getJsonBody(body)).url(path));
    }

    @Override
    public JsonObject bind3pid(JsonObject threePid) {
        URL path = getClientPath("account", "3pid", "bind");
        String body = executeAuthenticated(new Request.Builder().post(getJsonBody(threePid)).url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public void unbind3pid(String medium, String address) {
        URL path = getClientPath("account", "3pid", "unbind");
        JsonObject body = new JsonObject();
        body.addProperty("medium", medium);
        body.addProperty("address", address);
        executeAuthenticated(new Request.Builder().post(getJsonBody(body)).url(path));
    }

    @Override
    public JsonObject requestToken3pid(String medium, String address, String clientSecret, int sendAttempt,
            Optional<String> nextLink) {
        URL path;
        String addressField;
        if ("email".equals(medium)) {
            path = getClientPath("account", "3pid", "email", "requestToken");
            addressField = "email";
        } else if ("msisdn".equals(medium)) {
            path = getClientPath("account", "3pid", "msisdn", "requestToken");
            addressField = "phone";
        } else {
            throw new IllegalArgumentException("Unsupported medium: " + medium);
        }
        JsonObject body = new JsonObject();
        body.addProperty("client_secret", clientSecret);
        body.addProperty(addressField, address);
        body.addProperty("send_attempt", sendAttempt);
        nextLink.ifPresent(link -> body.addProperty("next_link", link));
        String response = execute(new Request.Builder().post(getJsonBody(body)).url(path));
        return GsonUtil.parseObj(response);
    }

    @Override
    public JsonObject createKeyBackupVersion(JsonObject versionInfo) {
        URL path = getClientPath("room_keys", "version");
        String body = executeAuthenticated(new Request.Builder().post(getJsonBody(versionInfo)).url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public JsonObject getKeyBackupVersion() {
        URL path = getClientPath("room_keys", "version");
        String body = executeAuthenticated(new Request.Builder().get().url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public void updateKeyBackupVersion(String version, JsonObject versionInfo) {
        URL path = getClientPath("room_keys", "version", version);
        executeAuthenticated(new Request.Builder().put(getJsonBody(versionInfo)).url(path));
    }

    @Override
    public void deleteKeyBackupVersion(String version) {
        URL path = getClientPath("room_keys", "version", version);
        executeAuthenticated(new Request.Builder().delete().url(path));
    }

    @Override
    public JsonObject getRoomKeys(Optional<String> version) {
        HttpUrl.Builder builder = getClientPathBuilder("room_keys", "keys");
        version.ifPresent(v -> builder.addEncodedPathSegment(v));
        URL path = builder.build().url();
        String body = executeAuthenticated(new Request.Builder().get().url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public void setRoomKeys(String version, JsonObject keys) {
        URL path = getClientPath("room_keys", "keys", version);
        executeAuthenticated(new Request.Builder().put(getJsonBody(keys)).url(path));
    }

    @Override
    public void deleteRoomKeys(String version) {
        URL path = getClientPath("room_keys", "keys", version);
        executeAuthenticated(new Request.Builder().delete().url(path));
    }

    @Override
    public void setPresence(PresenceStatus presence, String statusMsg) {
        URL path = getClientPath("presence", getUserId(), "status");
        JsonObject body = new JsonObject();
        body.addProperty("presence", presence.getId());
        if (statusMsg != null) {
            body.addProperty("status_msg", statusMsg);
        }
        executeAuthenticated(new Request.Builder().put(getJsonBody(body)).url(path));
    }

    @Override
    public JsonObject getPresence(String userId) {
        URL path = getClientPath("presence", userId, "status");
        String body = executeAuthenticated(new Request.Builder().get().url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public _SyncData sync(_SyncOptions options) {
        long start = System.currentTimeMillis();
        HttpUrl.Builder path = getClientPathBuilder("sync");
        path.addQueryParameter("timeout", options.getTimeout().map(Long::intValue).orElse(30000).toString());
        options.getSince().ifPresent(since -> path.addQueryParameter("since", since));
        options.getFilter().ifPresent(filter -> path.addQueryParameter("filter", filter));
        options.withFullState().ifPresent(state -> path.addQueryParameter("full_state", state ? "true" : "false"));
        options.getSetPresence().ifPresent(presence -> path.addQueryParameter("presence", presence));

        String body = executeAuthenticated(new Request.Builder().get().url(path.build().url()));
        long request = System.currentTimeMillis();
        log.info("Sync: network request took {} ms", (request - start));
        SyncDataJson data = new SyncDataJson(GsonUtil.parseObj(body));
        long parsing = System.currentTimeMillis();
        log.info("Sync: parsing took {} ms", (parsing - request));
        return data;
    }

    @Override
    public _MatrixContent getMedia(String mxUri) throws IllegalArgumentException {
        return getMedia(URI.create(mxUri));
    }

    @Override
    public _MatrixContent getMedia(URI mxUri) throws IllegalArgumentException {
        return new MatrixHttpContent(context, mxUri);
    }

    private String putMedia(Request.Builder builder, String filename) {
        HttpUrl.Builder b = getMediaPathBuilder("upload");
        if (StringUtils.isNotEmpty(filename)) b.addQueryParameter("filename", filename);

        String body = executeAuthenticated(builder.url(b.build()));
        return GsonUtil.getStringOrThrow(GsonUtil.parseObj(body), "content_uri");
    }

    @Override
    public String putMedia(byte[] data, String type) {
        return putMedia(data, type, null);
    }

    @Override
    public String putMedia(byte[] data, String type, String filename) {
        return putMedia(new Request.Builder().post(RequestBody.create(MediaType.parse(type), data)), filename);
    }

    @Override
    public String putMedia(File data, String type) {
        return putMedia(data, type, null);
    }

    @Override
    public String putMedia(File data, String type, String filename) {
        return putMedia(new Request.Builder().post(RequestBody.create(MediaType.parse(type), data)), filename);
    }

    @Override
    public List<JsonObject> getPushers() {
        URL url = getClientPath("pushers");
        JsonObject response = GsonUtil.parseObj(executeAuthenticated(new Request.Builder().get().url(url)));
        return GsonUtil.findArray(response, "pushers").map(array -> GsonUtil.asList(array, JsonObject.class))
                .orElse(Collections.emptyList());
    }

    @Override
    public void setPusher(JsonObject pusher) {
        URL url = getClientPath("pushers", "set");
        executeAuthenticated(new Request.Builder().url(url).post(getJsonBody(pusher)));
    }

    @Override
    public void deletePusher(String pushKey) {
        JsonObject pusher = new JsonObject();
        pusher.add("kind", JsonNull.INSTANCE);
        pusher.addProperty("pushkey", pushKey);
        setPusher(pusher);
    }

    @Override
    public _GlobalPushRulesSet getPushRules() {
        URL url = getClientPath("pushrules", "global", "");
        JsonObject response = GsonUtil.parseObj(executeAuthenticated(new Request.Builder().url(url).get()));
        return new GlobalPushRulesSet(response);
    }

    @Override
    public _PushRule getPushRule(String scope, String kind, String id) {
        return new MatrixHttpPushRule(context, scope, kind, id);
    }

    @Override
    public void setPushRule(String scope, String kind, String ruleId, JsonObject rule) {
        URL path = getClientPath("pushrules", scope, kind, ruleId);
        executeAuthenticated(new Request.Builder().put(getJsonBody(rule)).url(path));
    }

    @Override
    public void setPushRuleActions(String scope, String kind, String ruleId, JsonObject actions) {
        URL path = getClientPath("pushrules", scope, kind, ruleId, "actions");
        executeAuthenticated(new Request.Builder().put(getJsonBody(actions)).url(path));
    }

    @Override
    public void deletePushRule(String scope, String kind, String ruleId) {
        URL path = getClientPath("pushrules", scope, kind, ruleId);
        executeAuthenticated(new Request.Builder().delete().url(path));
    }

    @Override
    public void setPushRuleEnabled(String scope, String kind, String ruleId, boolean enabled) {
        URL path = getClientPath("pushrules", scope, kind, ruleId, "enabled");
        JsonObject body = new JsonObject();
        body.addProperty("enabled", enabled);
        executeAuthenticated(new Request.Builder().put(getJsonBody(body)).url(path));
    }

    @Override
    public JsonObject getNotifications(Optional<String> from, Optional<Integer> limit, Optional<Boolean> only) {
        HttpUrl.Builder builder = getClientPathBuilder("notifications");
        from.ifPresent(f -> builder.addQueryParameter("from", f));
        limit.ifPresent(l -> builder.addQueryParameter("limit", l.toString()));
        only.ifPresent(o -> builder.addQueryParameter("only", o.toString()));
        String response = executeAuthenticated(new Request.Builder().get().url(builder.build().url()));
        return GsonUtil.parseObj(response);
    }

    @Override
    public boolean checkUsernameAvailability(String username) {
        HttpUrl.Builder builder = getClientPathBuilder("register", "available");
        builder.addQueryParameter("username", username);
        String response = execute(new Request.Builder().get().url(builder.build().url()));
        JsonObject json = GsonUtil.parseObj(response);
        return GsonUtil.findBoolean(json, "available").orElse(false);
    }

    @Override
    public String createFilter(JsonObject filter) {
        URL path = getClientPath("user", getUserId(), "filter");
        String body = executeAuthenticated(new Request.Builder().post(getJsonBody(filter)).url(path));
        return GsonUtil.getStringOrThrow(GsonUtil.parseObj(body), "filter_id");
    }

    @Override
    public JsonObject getCapabilities() {
        URL path = getClientPath("capabilities");
        String body = execute(new Request.Builder().get().url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public Map<String, String> getRoomVersions() {
        JsonObject caps = getCapabilities();
        JsonObject roomVersions = GsonUtil.findObj(caps, "room_versions").orElse(null);
        if (roomVersions == null) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : roomVersions.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getAsString());
        }
        return result;
    }

    @Override
    public List<JsonObject> getDevices() {
        URL path = getClientPath("devices");
        String body = executeAuthenticated(new Request.Builder().get().url(path));
        JsonObject response = GsonUtil.parseObj(body);
        return GsonUtil.findArray(response, "devices").map(array -> GsonUtil.asList(array, JsonObject.class))
                .orElse(Collections.emptyList());
    }

    @Override
    public JsonObject getDevice(String deviceId) {
        URL path = getClientPath("devices", deviceId);
        String body = executeAuthenticated(new Request.Builder().get().url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public void updateDevice(String deviceId, String displayName) {
        URL path = getClientPath("devices", deviceId);
        JsonObject body = new JsonObject();
        body.addProperty("display_name", displayName);
        executeAuthenticated(new Request.Builder().put(getJsonBody(body)).url(path));
    }

    @Override
    public void deleteDevice(String deviceId) {
        URL path = getClientPath("devices", deviceId);
        executeAuthenticated(new Request.Builder().delete().url(path));
    }

    @Override
    public void deleteDevices(List<String> deviceIds) {
        URL path = getClientPath("delete_devices");
        JsonObject body = new JsonObject();
        JsonArray devicesArray = new JsonArray();
        deviceIds.forEach(devicesArray::add);
        body.add("devices", devicesArray);
        executeAuthenticated(new Request.Builder().post(getJsonBody(body)).url(path));
    }

    @Override
    public JsonObject uploadKeys(JsonObject keys) {
        URL path = getClientPath("keys", "upload");
        String body = executeAuthenticated(new Request.Builder().post(getJsonBody(keys)).url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public JsonObject queryKeys(JsonObject request) {
        URL path = getClientPath("keys", "query");
        String body = executeAuthenticated(new Request.Builder().post(getJsonBody(request)).url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public JsonObject claimKeys(JsonObject request) {
        URL path = getClientPath("keys", "claim");
        String body = executeAuthenticated(new Request.Builder().post(getJsonBody(request)).url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public JsonObject uploadSigningKeys(JsonObject keys) {
        URL path = getClientPath("keys", "device_signing", "upload");
        String body = executeAuthenticated(new Request.Builder().post(getJsonBody(keys)).url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public JsonObject uploadKeySignatures(JsonObject signatures) {
        URL path = getClientPath("keys", "signatures", "upload");
        String body = executeAuthenticated(new Request.Builder().post(getJsonBody(signatures)).url(path));
        return GsonUtil.parseObj(body);
    }

    @Override
    public JsonObject getKeyChanges(String from, String to) {
        URL path = getClientPath("keys", "changes");
        HttpUrl url = HttpUrl.parse(path.toString()).newBuilder().addQueryParameter("from", from)
                .addQueryParameter("to", to).build();
        String body = executeAuthenticated(new Request.Builder().get().url(url));
        return GsonUtil.parseObj(body);
    }

    @Override
    public void sendToDevice(String eventType, String txnId, JsonObject messages) {
        URL path = getClientPath("sendToDevice", eventType, txnId);
        executeAuthenticated(new Request.Builder().put(getJsonBody(messages)).url(path));
    }

    @Override
    public JsonObject searchUserDirectory(String searchTerm, int limit) {
        URL path = getClientPath("user_directory", "search");
        JsonObject body = new JsonObject();
        body.addProperty("search_term", searchTerm);
        body.addProperty("limit", limit);
        String response = executeAuthenticated(new Request.Builder().post(getJsonBody(body)).url(path));
        return GsonUtil.parseObj(response);
    }

    @Override
    public void changePassword(String newPassword, boolean logoutDevices) {
        URL path = getClientPath("account", "password");
        JsonObject body = new JsonObject();
        body.addProperty("new_password", newPassword);
        body.addProperty("logout_devices", logoutDevices);
        executeAuthenticated(new Request.Builder().post(getJsonBody(body)).url(path));
    }

    @Override
    public JsonObject refreshToken(String refreshToken) {
        URL path = getClientPath("refresh");
        JsonObject body = new JsonObject();
        body.addProperty("refresh_token", refreshToken);
        String response = executeAuthenticated(new Request.Builder().post(getJsonBody(body)).url(path));
        return GsonUtil.parseObj(response);
    }

    @Override
    public JsonObject requestOpenIdToken() {
        URL path = getClientPath("user", getUserId(), "openid", "request_token");
        String response = executeAuthenticated(new Request.Builder().post(getJsonBody(new JsonObject())).url(path));
        return GsonUtil.parseObj(response);
    }

    @Override
    public void logoutAll() {
        URL path = getClientPath("logout", "all");
        executeAuthenticated(new Request.Builder().post(getJsonBody(new JsonObject())).url(path));
    }

    @Override
    public JsonObject getFilter(String filterId) {
        URL path = getClientPath("user", getUserId(), "filter", filterId);
        String response = executeAuthenticated(new Request.Builder().get().url(path));
        return GsonUtil.parseObj(response);
    }

    @Override
    public JsonObject getMediaConfig() {
        URL path = getClientMediaV1Path("config");
        String response = executeAuthenticated(new Request.Builder().get().url(path));
        return GsonUtil.parseObj(response);
    }

    @Override
    public JsonObject getTurnServer() {
        URL path = getClientPath("voip", "turnServer");
        String response = executeAuthenticated(new Request.Builder().get().url(path));
        return GsonUtil.parseObj(response);
    }

    @Override
    public _MatrixContent getThumbnail(String mxUri, int width, int height, String method)
            throws IllegalArgumentException {
        return getThumbnail(URI.create(mxUri), width, height, method);
    }

    @Override
    public _MatrixContent getThumbnail(URI mxUri, int width, int height, String method)
            throws IllegalArgumentException {
        if (!"mxc".equalsIgnoreCase(mxUri.getScheme())) {
            throw new IllegalArgumentException("Not a valid MXC URI: " + mxUri);
        }
        String server = mxUri.getHost();
        String mediaId = mxUri.getPath().substring(1); // Remove leading slash
        HttpUrl.Builder builder = getClientMediaV1PathBuilder("thumbnail", server).addEncodedPathSegments(mediaId);
        builder.addQueryParameter("width", Integer.toString(width));
        builder.addQueryParameter("height", Integer.toString(height));
        if (method != null) {
            builder.addQueryParameter("method", method);
        }
        URL thumbnailUrl = builder.build().url();
        // We need to return a _MatrixContent that fetches from the thumbnail URL
        // Since MatrixHttpContent expects an MXC URI and builds download URL, we need a different approach.
        // Let's create a custom implementation that uses the thumbnail URL.
        return new _MatrixContent() {
            private MatrixHttpContentResult result;
            private boolean loaded = false;

            private synchronized void load() {
                if (loaded) return;
                MatrixHttpRequest request = new MatrixHttpRequest(new Request.Builder().url(thumbnailUrl));
                result = executeContentRequest(request);
                loaded = true;
            }

            @Override
            public URL getPermaLink() {
                return thumbnailUrl;
            }

            @Override
            public URI getAddress() {
                return mxUri;
            }

            @Override
            public boolean isValid() {
                load();
                return result.isValid();
            }

            @Override
            public Optional<String> getType() {
                load();
                if (!isValid()) {
                    throw new IllegalStateException("This method should only be called if valid is true.");
                }
                return result.getContentType();
            }

            @Override
            public byte[] getData() {
                load();
                if (!isValid()) {
                    throw new IllegalStateException("This method should only be called if valid is true.");
                }
                return result.getData();
            }

            @Override
            public Optional<String> getFilename() {
                load();
                if (!isValid()) {
                    throw new IllegalStateException("This method should only be called if valid is true.");
                }
                return result.getHeader("Content-Disposition").filter(l -> !l.isEmpty()).flatMap(l -> {
                    Pattern filenamePattern = Pattern.compile("filename=\"?([^\";]+)");
                    for (String v : l) {
                        Matcher m = filenamePattern.matcher(v);
                        if (m.find()) {
                            return Optional.of(m.group(1));
                        }
                    }
                    return Optional.empty();
                });
            }
        };
    }

    @Override
    public JsonObject getUrlPreview(String url, Optional<Long> ts) {
        HttpUrl.Builder builder = getClientMediaV1PathBuilder("preview_url");
        builder.addQueryParameter("url", url);
        ts.ifPresent(t -> builder.addQueryParameter("ts", t.toString()));
        String response = executeAuthenticated(new Request.Builder().get().url(builder.build().url()));
        return GsonUtil.parseObj(response);
    }

}
