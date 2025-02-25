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

package io.kamax.matrix.hs;

import com.google.gson.JsonObject;
import io.kamax.matrix.MatrixErrorInfo;
import io.kamax.matrix._MatrixContent;
import io.kamax.matrix._MatrixID;
import io.kamax.matrix._MatrixUserProfile;
import java8.util.Optional;

import java.util.List;

public interface _MatrixGroup {

    _MatrixHomeserver getHomeserver();

    String getAddress();

    Optional<String> getName();

    Optional<String> getTopic();

    Optional<String> getAvatarUrl();

    Optional<_MatrixContent> getAvatar();

    String getId();

    Optional<JsonObject> getState(String type);

    Optional<JsonObject> getState(String type, String key);

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

    void invite(_MatrixID mxId);

    List<_MatrixUserProfile> getJoinedUsers();


}
