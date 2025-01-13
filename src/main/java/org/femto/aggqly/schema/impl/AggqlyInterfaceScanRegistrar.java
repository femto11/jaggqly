package org.femto.aggqly.schema.impl;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Map;

import org.femto.aggqly.AggqlyInterfaceScan;
import org.femto.aggqly.schema.AggqlyColumn;
import org.femto.aggqly.schema.AggqlyComputed;
import org.femto.aggqly.schema.AggqlyJoin;
import org.femto.aggqly.schema.AggqlyJunction;
import org.femto.aggqly.schema.AggqlyRoot;
import org.femto.aggqly.schema.AggqlyType;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

public final class AggqlyInterfaceScanRegistrar
        implements ImportBeanDefinitionRegistrar, EnvironmentAware, ResourceLoaderAware {
    private Environment environment;
    private ResourceLoader resourceLoader;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata metadata,
            @NonNull BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = metadata
                .getAnnotationAttributes(AggqlyInterfaceScan.class.getCanonicalName());

        if (annotationAttributes != null) {
            String[] basePackages = (String[]) annotationAttributes.get("value");

            if (basePackages.length == 0) {
                basePackages = new String[] {
                        ((StandardAnnotationMetadata) metadata).getIntrospectedClass().getPackage().getName() };
            }

            final var provider = new ClassPathScanningCandidateComponentProvider(
                    false, environment) {
                @Override
                protected boolean isCandidateComponent(@NonNull AnnotatedBeanDefinition beanDefinition) {
                    AnnotationMetadata metadata = beanDefinition.getMetadata();
                    return metadata.isIndependent() && metadata.isInterface();
                }
            };

            // provider.addIncludeFilter(new AnnotationTypeFilter(AggqlyRoot.class));
            provider.addIncludeFilter(new AnnotationTypeFilter(AggqlyType.class));

            var schemaBuilder = new AggqlyDataLoaders.Builder();

            for (String basePackage : basePackages) {
                for (final var beanDefinition : provider.findCandidateComponents(basePackage)) {
                    final var aggqlyType = loadAggqlyTypeInterface(beanDefinition.getBeanClassName());

                    final var typeConfig = aggqlyType.getAnnotation(AggqlyType.class);
                    if (typeConfig == null) {
                        continue;
                    }

                    final var builder = AggqlyTypeImpl.Builder.fromAnnotation(typeConfig);

                    for (var method : aggqlyType.getDeclaredMethods()) {
                        final var root = method.getAnnotation(AggqlyRoot.class);
                        if (root != null) {
                            builder.root(AggqlyRootImpl.fromAnnotation(method.getName(), root));
                        }
                    }

                    Arrays.stream(aggqlyType.getDeclaredMethods())
                            .map(method -> {
                                final var returnType = method.getReturnType();

                                if (returnType.getName() == "java.util.List") {
                                    var gi = returnType.getGenericInterfaces()[0];
                                    var it = ((ParameterizedType) gi).getActualTypeArguments()[0];
                                }

                                final var join = method.getAnnotation(AggqlyJoin.class);
                                if (join != null) {
                                    return AggqlyJoinImpl.fromAnnotation(method.getName(), join);
                                }

                                final var junction = method.getAnnotation(AggqlyJunction.class);
                                if (junction != null) {
                                    return AggqlyJunctionImpl.fromAnnotation(method.getName(), junction);
                                }

                                final var computed = method.getAnnotation(AggqlyComputed.class);
                                if (computed != null) {
                                    return AggqlyColumnImpl.fromName(method.getName());
                                }

                                final var column = method.getAnnotation(AggqlyColumn.class);
                                if (column != null) {
                                    return AggqlyColumnImpl.fromAnnotation(method.getName(), column);
                                }

                                return AggqlyColumnImpl.fromName(method.getName());
                            })
                            .forEach(field -> {
                                builder.field(field);
                            });

                    schemaBuilder.type(builder.build());
                }
            }

            var aggqlyDataLoaders = schemaBuilder.build();

            var beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(AggqlyDataLoaders.class,
                    () -> aggqlyDataLoaders);

            registry.registerBeanDefinition("aggqlyDataLoaders", beanDefinitionBuilder.getBeanDefinition());
        }
    }

    @Nullable
    private Class<?> loadAggqlyTypeInterface(String className) {
        try {
            return ClassUtils.forName(className, resourceLoader.getClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
        }

        return null;
    }
}
