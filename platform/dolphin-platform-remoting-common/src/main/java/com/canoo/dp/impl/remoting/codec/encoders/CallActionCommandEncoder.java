/*
 * Copyright 2015-2017 Canoo Engineering AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.canoo.dp.impl.remoting.codec.encoders;

import com.canoo.dp.impl.remoting.commands.CallActionCommand;
import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.remoting.codec.CommandConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.Map;

public class CallActionCommandEncoder extends AbstractCommandEncoder<CallActionCommand> {

    @Override
    public JsonObject encode(final CallActionCommand command) {
        Assert.requireNonNull(command, "command");
        final JsonObject jsonCommand = new JsonObject();
        jsonCommand.addProperty(CommandConstants.CONTROLLER_ID, command.getControllerId());
        jsonCommand.addProperty(CommandConstants.ACTION_NAME, command.getActionName());

        final JsonArray paramArray = new JsonArray();
        for(Map.Entry<String, Object> paramEntry : command.getParams().entrySet()) {
            final JsonObject paramObject = new JsonObject();
            paramObject.addProperty(CommandConstants.PARAM_NAME, paramEntry.getKey());
            paramObject.add(CommandConstants.PARAM_VALUE, ValueEncoder.encodeValue(paramEntry.getValue()));
            paramArray.add(paramObject);
        }
        jsonCommand.add(CommandConstants.PARAMS, paramArray);

        jsonCommand.addProperty(CommandConstants.ID, CommandConstants.CALL_ACTION_COMMAND_ID);
        return jsonCommand;
    }

    @Override
    public CallActionCommand decode(JsonObject jsonObject) {
        Assert.requireNonNull(jsonObject, "jsonObject");
        try {
            final CallActionCommand command = new CallActionCommand();
            command.setControllerId(getStringElement(jsonObject, CommandConstants.CONTROLLER_ID));
            command.setActionName(getStringElement(jsonObject, CommandConstants.ACTION_NAME));

            final JsonArray jsonArray = jsonObject.getAsJsonArray(CommandConstants.PARAMS);
            if(jsonArray != null) {
                for (final JsonElement jsonElement : jsonArray) {
                    final JsonObject paramObject = jsonElement.getAsJsonObject();
                    command.addParam(getStringElement(paramObject, CommandConstants.PARAM_NAME), ValueEncoder.decodeValue(paramObject.get(CommandConstants.PARAM_VALUE)));
                }
            }
            return command;
        } catch (Exception ex) {
            throw new JsonParseException("Illegal JSON detected", ex);
        }
    }
}