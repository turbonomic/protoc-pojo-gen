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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import com.turbonomic.protoc.plugin.common.generator.EnumDescriptor;
import com.turbonomic.protoc.plugin.common.generator.FileDescriptorProcessingContext;
import com.turbonomic.protoc.plugin.common.generator.MessageDescriptor;
import com.turbonomic.protoc.plugin.common.generator.ProtocPluginCodeGenerator;
import com.turbonomic.protoc.plugin.common.generator.Registry;
import com.turbonomic.protoc.plugin.common.generator.ServiceDescriptor;

/**
 * An implementation of {@link ProtocPluginCodeGenerator} that generates POJOs
 * that can be converted to and from protos. The POJOs are useful when you need
 * to make many mutations to a proto over time because although you can do this
 * with proto builders, the builders use lots of memory and the POJOs use less.
 *
 * <p/>Functionality implemented:
 * 1. Getters and Setters
 * 2. Hazzers (ie for a field foo, hasFoo tells you whether or not foo was set)
 * 3. Support for field defaults
 * 4. Conversion to proto
 * 5. Conversion from proto
 * 6. HashCode && Equals
 * 7. Methods for clearing fields
 * 8. Copy method and copy constructor
 *
 * <p/>Note that we reuse enum definitions directly from the protos rather than
 * re-defining them.
 *
 * <p/>All POJOs implement the IntoProto and {@link java.io.Serializable} interfaces.
 * IntoProto is a generated interface generated by {@link IntoProtoInterfaceGenerator}.
 */
public class PojoCodeGenerator extends ProtocPluginCodeGenerator {

    public static final String PACKAGE_SUFFIX = "POJO";

    public static final String JAVA_LANG_PACKAGE = "java.lang";

    public static final String GOOGLE_PROTOBUF_PACKAGE = "com.google.protobuf";

    public static final String LIST_CLASS_NAME = "List";

    public static final String MAP_CLASS_NAME = "Map";

    public static final String GET_DEFAULT_INSTANCE = "getDefaultInstance";

    public static final String ON_PARENT_SETTING_METHOD = "onParentSettingInternalUse";

    public static final String JAVADOC_WARNING =
        "Do not use this POJO in a Map or Set except by identity (ie IdentityHashMap)"
        + "\nbecause updating a field on the POJO will change its hashCode, resulting in no longer"
        + "\nbeing able to find it in the map or set after the change even though it is there. Also"
        + "\nnote that {@link #hashCode} and {@link #equals} for POJOs with many fields can be"
        + "\nvery expensive.";

    /**
     * Name of the protobuf to be translated to a POJO in the {@code #fromProto} method.
     */
    public static final String FROM_PROTO_FIELD_NAME = "proto";

    /**
     * Default subpackage.
     */
    public static final String DEFAULT_SUBPACKAGE = "com.pojo.gen";

    private IntoProtoInterfaceGenerator intoProtoGenerator;

    /**
     * In practice, users of the generated pojos found it confusing that the POJOs that were
     * equivalent to builders had the same names as the built protobufs. So we add the suffix
     * here to make it less likely to use the mutable POJOs as replacements for the immutable,
     * built protobufs. We do not use the "Builder" suffix even though these are designed
     * as replacements for the builders because they do not actually build anything, they are
     * just POJOs.
     */
    public static final String IMPLEMENTATION_SUFFIX = "Impl";

    public static final Map<String, TypeName> PRIMITIVES_MAP = ImmutableMap.<String, TypeName>builder()
        .put("Boolean", TypeName.BOOLEAN)
        .put("Byte", TypeName.BYTE)
        .put("Short", TypeName.SHORT)
        .put("Integer", TypeName.INT)
        .put("Long", TypeName.LONG)
        .put("Character", TypeName.CHAR)
        .put("Float", TypeName.FLOAT)
        .put("Double", TypeName.DOUBLE)
        .build();

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String getPluginName() {
        return "protoc-pojo-gen";
    }

    @Nonnull
    public static Optional<String> multiLineComment(@Nonnull final String originalComment) {
        if (originalComment.length() > 2) {
            // Proto comments are always surrounded by double quotes. Strip those off.
            // ie "this is a comment" --> this is a comment
            // Multiline comments have a " character and a + character followed by another "
            // as in:
            // This is a bar!\n" + "And it has more than one line.
            return Optional.of(originalComment.substring(1, originalComment.length() - 1)
                .replaceAll("\\\\n\" \\+ \"", "\n")
                .replaceAll("\\$", "\\$\\$")
            );
        } else {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p/>TopologyEntityDTO --> TopologyEntityImpl
     * AnalysisSettings --> AnalysisSettingsImpl
     */
    @Override
    @Nonnull
    protected String generatePluginJavaClass(@Nonnull final String protoJavaClass) {
        return protoJavaClass.endsWith("DTO")
            ? protoJavaClass.substring(0, protoJavaClass.length() - 3) + PACKAGE_SUFFIX
            : protoJavaClass + PACKAGE_SUFFIX;
    }

    @Override
    @Nonnull
    protected FileDescriptorProcessingContext createFileDescriptorProcessingContext(
        @Nonnull final Registry registry, @Nonnull final FileDescriptorProto fileDescriptorProto,
        @Nullable String maximalSubpackage) {
        if (intoProtoGenerator == null) {
            intoProtoGenerator = new IntoProtoInterfaceGenerator(getMaximalSubpackage(maximalSubpackage));
        }
        return new PojoFileDescriptorProcessingContext(this, registry,
            fileDescriptorProto, intoProtoGenerator);
    }

    /**
     * The maximal subpackage shared by all files to be processed. If none is available we use a default.
     *
     * @return The maximal subpackage shared by all files to be processed.
     */
    @Nonnull
    public String getMaximalSubpackage(@Nullable final String maximalSubpackage) {
        return maximalSubpackage == null ? DEFAULT_SUBPACKAGE : maximalSubpackage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected String generateImports() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected Optional<String> generateEnumCode(@Nonnull final EnumDescriptor enumDescriptor) {
        // We reuse the actual proto enums.
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected Optional<String> generateServiceCode(@Nonnull final ServiceDescriptor serviceDescriptor) {
        // No need to generate code for services.
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected Optional<String> generateMessageCode(@Nonnull final MessageDescriptor messageDescriptor) {
        // We do generate code for messages but we go through the generateTypeForMessage route.
        return Optional.empty();
    }

    /**
     * Generate the concrete POJO implementation for the protobuf message along with a read-only interface
     * definition.
     *
     * @param msgDescriptor The descriptor for the proto message.
     * @return The builder for the POJO class and its read-only interface that represents the proto message.
     *         Return an empty list if you do not wish to generate a POJO class or interface for the message.
     */
    @Nonnull
    List<TypeSpec.Builder> generateTypesForMessage(@Nonnull final MessageDescriptor msgDescriptor) {
        final PojoImplGenerator implGen = new PojoImplGenerator(intoProtoGenerator);
        return implGen.generateImplForMessage(msgDescriptor);
    }

    @Nonnull
    @Override
    protected List<File> generateMiscellaneousFiles() {
        final String classPath = intoProtoGenerator.getClassPath();
        final File.Builder fileBuilder = File.newBuilder()
            .setName(classPath.replace('.', '/') + ".java");

        final AnnotationSpec generatedAnnotation = AnnotationSpec.builder(Generated.class)
            .addMember("value", "\"by $L compiler plugin\"", getPluginName())
            .build();

        final JavaFile file = JavaFile.builder(intoProtoGenerator.getJavaPackage(),
                intoProtoGenerator.generateInterface()
                    .addAnnotation(generatedAnnotation)
                    .build())
            .addFileComment("Generated by the $L compiler plugin. DO NOT EDIT!\n",
                getPluginName())
            .build();

        return Collections.singletonList(fileBuilder.setContent(file.toString()).build());
    }
}
