/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.utils.repositories;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UnrecognizedQueryParameterException;
import eu.openanalytics.rdepot.base.entities.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PackageMaintainerQueryRepository {

    private final MessageSource messageSource;
    private static final Locale locale = LocaleContextHolder.getLocale();

    @Autowired
    private final JPAQueryFactory queryFactory;

    public List<PackageMaintainerQueryDSLTuple> findMaintainers(
            Boolean deleted,
            List<String> technologies,
            List<String> filteredRepositories,
            List<String> allowedRepositories,
            String search,
            Pageable pageable)
            throws UnrecognizedQueryParameterException {

        QPackageMaintainer maintainer = QPackageMaintainer.packageMaintainer;
        QPackage packageBag = QPackage.package$;
        QUser packageUser = QUser.user;
        QUser maintainerUser = new QUser("maintainerUser");
        QRepository repository = QRepository.repository;

        BooleanBuilder builder = new BooleanBuilder();

        if (BooleanUtils.isTrue(deleted)) {
            builder.and(maintainer.deleted.eq(deleted));
        } else if (BooleanUtils.isFalse(deleted)) {
            builder.and(maintainer.deleted.eq(deleted).or(maintainer.deleted.isNull()));
        }

        if (!search.isBlank()) {
            builder.and(maintainer.user.name.containsIgnoreCase(search).or(packageBag.name.containsIgnoreCase(search)));
        }

        if (Objects.nonNull(technologies)) {
            builder.and(packageBag.repositoryGeneric.resourceTechnology.in(technologies));
        }

        if (Objects.nonNull(filteredRepositories)) {
            builder.and(packageBag.repositoryGeneric.name.in(filteredRepositories));
        }

        if (!allowedRepositories.isEmpty()) {
            builder.and(packageBag.repositoryGeneric.name.in(allowedRepositories));
        }

        Map<String, ComparableExpressionBase<?>> sortableFields = Map.ofEntries(
                Map.entry("id", maintainer.id),
                Map.entry("user.id", packageBag.user.id),
                Map.entry("user", packageBag.user.name),
                Map.entry("user.name", packageBag.user.name),
                Map.entry("user.login", packageBag.user.login),
                Map.entry("user.email", packageBag.user.email),
                Map.entry("packageName", packageBag.name),
                Map.entry("repository.id", packageBag.repositoryGeneric.id),
                Map.entry("repository", packageBag.repositoryGeneric.name),
                Map.entry("repository.name", packageBag.repositoryGeneric.name),
                Map.entry("repository.publicationUri", packageBag.repositoryGeneric.publicationUri),
                Map.entry("repository.published", packageBag.repositoryGeneric.published),
                Map.entry("repository.requiresAuthentication", packageBag.repositoryGeneric.requiresAuthentication),
                Map.entry("repository.resourceTechnology", packageBag.repositoryGeneric.resourceTechnology),
                Map.entry(
                        "repository.lastPublicationSuccessful", packageBag.repositoryGeneric.lastPublicationSuccessful),
                Map.entry("deleted", maintainer.deleted));

        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        for (Sort.Order sortOrder : pageable.getSort()) {
            ComparableExpressionBase<?> sortField = sortableFields.get(sortOrder.getProperty());

            if (sortField == null) {
                throw new UnrecognizedQueryParameterException(List.of(sortOrder.getProperty()), messageSource, locale);
            }
            orderSpecifiers.add(new OrderSpecifier<>(sortOrder.isAscending() ? Order.ASC : Order.DESC, sortField));
        }

        List<Tuple> result = queryFactory
                .select(
                        maintainer.id,
                        packageBag.user.id,
                        packageBag.user.name,
                        packageBag.user.login,
                        packageBag.user.email,
                        packageBag.name,
                        packageBag.repositoryGeneric.id,
                        packageBag.repositoryGeneric.name,
                        packageBag.repositoryGeneric.publicationUri,
                        packageBag.repositoryGeneric.published,
                        packageBag.repositoryGeneric.requiresAuthentication,
                        packageBag.repositoryGeneric.resourceTechnology,
                        packageBag.repositoryGeneric.lastPublicationSuccessful,
                        maintainer.deleted)
                .distinct()
                .from(packageBag)
                .leftJoin(packageBag.maintainers, maintainer)
                .leftJoin(packageBag.user, packageUser)
                .leftJoin(packageBag.repositoryGeneric, repository)
                .leftJoin(maintainer.user, maintainerUser)
                .where(builder)
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .fetch();
        return result.stream().map(PackageMaintainerQueryDSLTuple::new).toList();
    }
}
