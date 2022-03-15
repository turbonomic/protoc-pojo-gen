/*
 * Copyright (C) 2009 - 2022 Turbonomic, Inc.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.turbonomic.protoc.pojo.gen;

import java.io.IOException;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;

/**
 * This is the main entrypoint for the plugin. This class
 * handles interaction with the protobuf compiler.
 *
 * <p>The protobuf compiler calls the plugin, passing a
 * {@link CodeGeneratorRequest} message via stdin, and
 * expects the {@link CodeGeneratorResponse} message via
 * stdout.
 */
public class Main {

    private Main() {
        // Utility classes should not have a public or default constructor
    }

    /**
     * Main method.
     *
     * @param args The arguments to the main method.
     * @throws IOException If code generation throws an exception.
     */
    public static void main(String[] args) throws IOException {
        new PojoCodeGenerator().generate();
    }

}
