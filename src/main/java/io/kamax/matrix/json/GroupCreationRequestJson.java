/*
 * matrix-java-sdk - Matrix Client SDK for Java
 * Copyright (C) 2018 Kamax Sarl
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

package io.kamax.matrix.json;

import io.kamax.matrix.group.GroupProfile;
import io.kamax.matrix.group._GroupCreationOptions;


public class GroupCreationRequestJson {

    private String localpart;
    private GroupProfile profile;

    public GroupCreationRequestJson(_GroupCreationOptions options) {
        this.localpart = options.getLocalpart().orElse(null);
        this.profile = new GroupProfile(options.getName().orElse(null));
    }

}
