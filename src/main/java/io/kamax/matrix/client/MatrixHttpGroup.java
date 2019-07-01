/*
 * matrix-java-sdk - Matrix Client SDK for Java
 * Copyright (C) 2017 Kamax Sarl
 *
 * https://www.kamax.io/
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
import io.kamax.matrix.*;
import io.kamax.matrix.hs._MatrixGroup;
import io.kamax.matrix.json.*;
import java8.util.Optional;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MatrixHttpGroup extends AMatrixHttpClient implements _MatrixGroup {

    private Logger log = LoggerFactory.getLogger(MatrixHttpGroup.class);

    private String groupId;

    public MatrixHttpGroup(MatrixClientContext context, String groupId) {
        super(context);
        this.groupId = groupId;
    }

    @Override
    public String getAddress() {
        return groupId;
    }

    @Override
    public Optional<String> getName() {
        return getState("m.group.name").flatMap(obj -> GsonUtil.findString(obj, "name"));
    }

    @Override
    public Optional<String> getTopic() {
        return getState("m.group.topic").flatMap(obj -> GsonUtil.findString(obj, "topic"));
    }

    @Override
    public Optional<String> getAvatarUrl() {
        return getState("m.group.avatar").flatMap(obj -> GsonUtil.findString(obj, "url"));
    }

    @Override
    public Optional<_MatrixContent> getAvatar() {
        return getAvatarUrl().flatMap(url -> {
            try {
                return Optional.of(new MatrixHttpContent(context, new URI(url)));
            } catch (URISyntaxException e) {
                log.debug("{} is not a valid URI for avatar, returning empty", url);
                return Optional.empty();
            }
        });
    }

    @Override
    public String getId() {
        return groupId;
    }

    @Override
    public Optional<JsonObject> getState(String type) {
        URL path = getClientPath("groups", getAddress(), "state", type);

        MatrixHttpRequest request = new MatrixHttpRequest(new Request.Builder().get().url(path));
        request.addIgnoredErrorCode(404);
        String body = executeAuthenticated(request);
        if (StringUtils.isBlank(body)) {
            return Optional.empty();
        }

        return Optional.of(GsonUtil.parseObj(body));
    }

    @Override
    public Optional<JsonObject> getState(String type, String key) {
        URL path = getClientPath("groups", groupId, "state", type, key);

        MatrixHttpRequest request = new MatrixHttpRequest(new Request.Builder().get().url(path));
        request.addIgnoredErrorCode(404);
        String body = executeAuthenticated(request);
        if (StringUtils.isBlank(body)) {
            return Optional.empty();
        }

        return Optional.of(GsonUtil.parseObj(body));
    }

    @Override
    public void join() {
        join(Collections.emptyList());
    }

    @Override
    public void join(List<String> servers) {
        HttpUrl.Builder b = getClientPathBuilder("groups", groupId, "join");
        servers.forEach(server -> b.addQueryParameter("server_name", server));
        executeAuthenticated(new Request.Builder().post(getJsonBody(new JsonObject())).url(b.build()));
    }

    @Override
    public Optional<MatrixErrorInfo> tryJoin() {
        return tryJoin(Collections.emptyList());
    }

    @Override
    public Optional<MatrixErrorInfo> tryJoin(List<String> servers) {
        try {
            join(servers);
            return Optional.empty();
        } catch (MatrixClientRequestException e) {
            return e.getError();
        }
    }

    @Override
    public void leave() {
        URL path = getClientPath("groups", groupId, "leave");
        MatrixHttpRequest request = new MatrixHttpRequest(
                new Request.Builder().post(getJsonBody(new JsonObject())).url(path));

        request.addIgnoredErrorCode(404);
        executeAuthenticated(request);
    }

    @Override
    public Optional<MatrixErrorInfo> tryLeave() {
        try {
            leave();
            return Optional.empty();
        } catch (MatrixClientRequestException e) {
            return e.getError();
        }
    }

    @Override
    public void kick(_MatrixID user) {
        kick(user, null);
    }

    @Override
    public void kick(_MatrixID user, String reason) {
        JsonObject body = new JsonObject();
        body.addProperty("user_id", user.getId());
        body.addProperty("reason", reason);
        URL path = getClientPath("groups", groupId, "kick");
        MatrixHttpRequest request = new MatrixHttpRequest(new Request.Builder().post(getJsonBody(body)).url(path));
        executeAuthenticated(request);
    }

    @Override
    public Optional<MatrixErrorInfo> tryKick(_MatrixID user) {
        return tryKick(user, null);
    }

    @Override
    public Optional<MatrixErrorInfo> tryKick(_MatrixID user, String reason) {
        try {
            kick(user, reason);
            return Optional.empty();
        } catch (MatrixClientRequestException e) {
            return e.getError();
        }
    }

    @Override
    public String sendEvent(String type, JsonObject content) {
        // FIXME URL encoding
        URL path = getClientPath("groups", groupId, "send", type, Long.toString(System.currentTimeMillis()));
        String body = executeAuthenticated(new Request.Builder().put(getJsonBody(content)).url(path));
        return GsonUtil.getStringOrThrow(GsonUtil.parseObj(body), "event_id");
    }

    @Override
    public void invite(_MatrixID mxId) {
        URL path = getClientPath("groups", groupId, "invite");
        executeAuthenticated(
                new Request.Builder().post(getJsonBody(GsonUtil.makeObj("user_id", mxId.getId()))).url(path));
    }

    @Override
    public List<_MatrixUserProfile> getJoinedUsers() {
        URL path = getClientPath("groups", groupId, "joined_members");
        String body = executeAuthenticated(new Request.Builder().get().url(path));

        List<_MatrixUserProfile> ids = new ArrayList<>();
        if (StringUtils.isNotEmpty(body)) {
            JsonObject joinedUsers = jsonParser.parse(body).getAsJsonObject().get("joined").getAsJsonObject();
            ids = StreamSupport.stream(joinedUsers.entrySet()).filter(e -> e.getValue().isJsonObject()).map(entry -> {
                JsonObject obj = entry.getValue().getAsJsonObject();
                return new MatrixHttpUser(getContext(), MatrixID.asAcceptable(entry.getKey())) {

                    @Override
                    public Optional<String> getName() {
                        return GsonUtil.findString(obj, "display_name");
                    }

                    @Override
                    public Optional<_MatrixContent> getAvatar() {
                        return GsonUtil.findString(obj, "avatar_url").flatMap(s -> {
                            try {
                                return Optional.of(new URI(s));
                            } catch (URISyntaxException e) {
                                return Optional.empty();
                            }
                        }).map(uri -> new MatrixHttpContent(getContext(), uri));
                    }

                };
            }).collect(Collectors.toList());
        }

        return ids;
    }

    private void addTag(String tag, Double order) {
        // TODO check name size

        if (order != null && (order < 0 || order > 1)) {
            throw new IllegalArgumentException("Order out of range!");
        }

        URL path = getClientPath("user", getUserId(), "groups", getAddress(), "tags", tag);
        Request.Builder request = new Request.Builder().url(path);
        if (order != null) {
            request.put(getJsonBody(new GroupTagSetBody(order)));
        } else {
            request.put(getJsonBody(new JsonObject()));
        }
        executeAuthenticated(request);
    }

    private void deleteTag(String tag) {
        URL path = getClientPath("user", getUserId(), "groups", getAddress(), "tags", tag);
        executeAuthenticated(new Request.Builder().url(path).delete());
    }
}
