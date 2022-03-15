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

import static com.turbonomic.protoc.pojo.gen.PojoCodeGenerator.IMPLEMENTATION_SUFFIX;

import javax.annotation.Nonnull;

import com.turbonomic.protoc.plugin.common.generator.TypeNameFormatter;

/**
 * Formatter for POJO type nammes.
 */
public class PojoTypeNameFormatter implements TypeNameFormatter {
    @Nonnull
    @Override
    public String formatTypeName(@Nonnull String unformattedTypeName) {
        if (unformattedTypeName.endsWith("DTO")) {
            return unformattedTypeName.substring(0, unformattedTypeName.length() - 3) + IMPLEMENTATION_SUFFIX;
        } else if (unformattedTypeName.endsWith(IMPLEMENTATION_SUFFIX)) {
            return unformattedTypeName;
        } else {
            return unformattedTypeName + IMPLEMENTATION_SUFFIX;
        }
    }
}
