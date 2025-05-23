/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
package eu.openanalytics.rdepot.r.api.v2.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2Controller;
import eu.openanalytics.rdepot.base.api.v2.converters.SubmissionDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApplyPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.CreateException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DeleteException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.MalformedPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.RepositoryNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.SubmissionNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.resolvers.CommonPageableSortResolver;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.mediator.deletion.SubmissionDeleter;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.exceptions.NonFatalSubmissionStrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.base.time.DateParser;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.base.utils.specs.SubmissionSpecs;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.base.validation.SubmissionPatchValidator;
import eu.openanalytics.rdepot.base.validation.ValidationResult;
import eu.openanalytics.rdepot.base.validation.ValidationResultImpl;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageDuplicateWithReplaceOff;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageValidationException;
import eu.openanalytics.rdepot.base.validation.exceptions.PatchValidationException;
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageUploadRequest;
import eu.openanalytics.rdepot.r.api.v2.hateoas.RSubmissionModelAssembler;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mediator.deletion.RSubmissionDeleter;
import eu.openanalytics.rdepot.r.messaging.RMessageCodes;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.exceptions.GenerateManualException;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonPatch;
import jakarta.json.spi.JsonProvider;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller implementation for R submissions.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v2/manager/r/submissions")
public class RSubmissionController extends ApiV2Controller<Submission, SubmissionDto> {

    private final SubmissionService submissionService;
    private final UserService userService;
    private final RStrategyFactory strategyFactory;
    private final SecurityMediator securityMediator;
    private final RRepositoryService repositoryService;
    private final PackageValidator<RPackage> packageValidator;
    private final SubmissionDeleter submissionDeleter;
    private final SubmissionPatchValidator submissionPatchValidator;
    private final PageableValidator pageableValidator;
    private final CommonPageableSortResolver pageableSortResolver;
    private final StrategyExecutor strategyExecutor;

    @Value("${replacing.packages.enabled}")
    private boolean replacingPackagesEnabled;

    public RSubmissionController(
            MessageSource messageSource,
            RSubmissionModelAssembler modelAssembler,
            PagedResourcesAssembler<Submission> pagedModelAssembler,
            ObjectMapper objectMapper,
            SubmissionService submissionService,
            UserService userService,
            RStrategyFactory strategyFactory,
            SecurityMediator securityMediator,
            RRepositoryService repositoryService,
            PackageValidator<RPackage> packageValidator,
            RSubmissionDeleter submissionDeleter,
            SubmissionDtoConverter submissionDtoConverter,
            SubmissionPatchValidator submissionPatchValidator,
            PageableValidator pageableValidator,
            CommonPageableSortResolver pageableSortResolver,
            StrategyExecutor strategyExecutor) {
        super(
                messageSource,
                LocaleContextHolder.getLocale(),
                modelAssembler,
                pagedModelAssembler,
                objectMapper,
                SubmissionDto.class,
                Optional.empty(),
                submissionDtoConverter);
        this.repositoryService = repositoryService;
        this.submissionService = submissionService;
        this.userService = userService;
        this.strategyFactory = strategyFactory;
        this.securityMediator = securityMediator;
        this.packageValidator = packageValidator;
        this.submissionDeleter = submissionDeleter;
        this.submissionPatchValidator = submissionPatchValidator;
        this.pageableValidator = pageableValidator;
        this.pageableSortResolver = pageableSortResolver;
        this.strategyExecutor = strategyExecutor;
    }

    /**
     * Submits package archive and creates submission.
     * @param multipartFile package file
     * @param repository name of the destination repository
     * @param generateManual specifies if manuals should be generated for the package
     * @param replaceRequestParam specified if previous version should be replaced
     * @param principal used for authorization
     * @return DTO with created submission
     * @throws UserNotAuthorized when user could not be authenticated or authorized
     * @throws CreateException if there was an error on the server side
     * @throws RepositoryNotFound when no repository was found
     */
    @PreAuthorize("hasAuthority('user')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "submitRPackage")
    public @ResponseBody ResponseEntity<?> submitPackage(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("repository") final String repository,
            @RequestParam(name = "generateManual", defaultValue = "${generate-manuals}") final Boolean generateManual,
            @RequestParam(name = "replace", defaultValue = "false") final Boolean replaceRequestParam,
            @RequestParam(name = "binary", defaultValue = "false") final Boolean binaryPackage,
            @RequestParam(name = "rVersion") Optional<String> rVersion,
            @RequestParam(name = "architecture") Optional<String> architecture,
            @RequestParam(name = "distribution") Optional<String> distribution,
            @RequestParam(name = "changes") Optional<String> changes,
            Principal principal)
            throws UserNotAuthorized, CreateException, RepositoryNotFound {

        boolean replace = replaceRequestParam;

        final User uploader = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
        final RRepository repositoryEntity = repositoryService
                .findByNameAndDeleted(repository, false)
                .orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        final ValidationResult validationResult = ValidationResultImpl.createResult();

        packageValidator.validate(multipartFile, validationResult);
        if (validationResult.hasErrors()) {
            return handleValidationError(validationResult);
        }

        if (!replacingPackagesEnabled) replace = false;

        if (binaryPackage && (rVersion.isEmpty() || architecture.isEmpty() || distribution.isEmpty()))
            return handleValidationError(RMessageCodes.ERROR_MISSING_DATA_FOR_BINARY_PACKAGE);

        if (!binaryPackage && (rVersion.isPresent() || architecture.isPresent() || distribution.isPresent()))
            return handleValidationError(RMessageCodes.ERROR_PARAMETERS_NOT_ALLOWED_FOR_NON_BINARY_PACKAGE);

        final RPackageUploadRequest request = new RPackageUploadRequest(
                multipartFile,
                repositoryEntity,
                generateManual,
                replace,
                binaryPackage,
                rVersion.orElse(""),
                architecture.orElse(""),
                distribution.orElse(""),
                changes.orElse(""));
        final Strategy<Submission> strategy = strategyFactory.uploadPackageStrategy(request, uploader);

        try {
            final Submission submission = strategyExecutor.execute(strategy);

            if (generateManual && binaryPackage) {
                return handleWarningForSingleEntity(
                        submission, RMessageCodes.GENERATE_MANUAL_NOT_SUPPORTED, uploader, true);
            } else {
                return handleCreatedForSingleEntity(submission, uploader);
            }

        } catch (NonFatalSubmissionStrategyFailure e) {
            log.debug(e.getMessage(), e);
            return handleWarningForSingleEntity(
                    e.getSubmission(),
                    e.getReason() instanceof SynchronizeRepositoryException
                            ? MessageCodes.WARNING_SYNCHRONIZATION_FAILURE
                            : MessageCodes.WARNING_UNKNOWN,
                    uploader,
                    true);
        } catch (StrategyFailure e) {
            if (e.getReason() instanceof PackageDuplicateWithReplaceOff warningException) {
                log.debug("{}: {}", e.getMessage(), multipartFile.getOriginalFilename(), e);
                if (!replacingPackagesEnabled && replaceRequestParam)
                    return handleWarningForSingleEntity(
                            warningException.getSubmission(),
                            MessageCodes.WARNING_REPLACING_PACKAGES_DISABLED,
                            uploader,
                            false);
                else
                    return handleWarningForSingleEntity(
                            warningException.getSubmission(), MessageCodes.WARNING_PACKAGE_DUPLICATE, uploader, false);
            } else if (e.getReason() instanceof PackageValidationException
                    || e.getReason() instanceof GenerateManualException) {
                log.debug(e.getMessage(), e);
                return handleValidationError(e.getReason());
            }

            log.error(e.getMessage(), e);
            throw new CreateException(messageSource, locale);
        }
    }

    /**
     * Fetches all submissions.
     */
    @PreAuthorize("hasAuthority('user')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PageableAsQueryParam
    @Operation(operationId = "getAllRSubmissions")
    public @ResponseBody ResponseDto<PagedModel<EntityModel<SubmissionDto>>> getAllSubmissions(
            Principal principal,
            @ParameterObject Pageable pageable,
            @RequestParam(name = "state", required = false) List<SubmissionState> states,
            @RequestParam(name = "repository", required = false) List<String> repositories,
            @RequestParam(name = "fromDate", required = false) Optional<String> fromDate,
            @RequestParam(name = "toDate", required = false) Optional<String> toDate,
            @RequestParam(name = "search", required = false) Optional<String> search)
            throws ApiException {
        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        final DtoResolvedPageable resolvedPageable = pageableSortResolver.resolve(pageable);
        pageableValidator.validate(SubmissionDto.class, resolvedPageable);

        final Optional<Instant> fromDateInstant = fromDate.flatMap(DateParser::parseTimestampStart);
        final Optional<Instant> toDateInstant = toDate.flatMap(DateParser::parseTimestampEnd);

        Specification<Submission> specification = Specification.where(SubmissionSpecs.ofTechnology(List.of("R")));

        if (Objects.nonNull(states)) {
            specification = SpecificationUtils.andComponent(specification, SubmissionSpecs.ofState(states));
        }

        if (Objects.nonNull(repositories)) {
            specification = SpecificationUtils.andComponent(specification, SubmissionSpecs.ofRepository(repositories));
        }

        if (fromDateInstant.isPresent()) {
            specification =
                    SpecificationUtils.andComponent(specification, SubmissionSpecs.fromDate(fromDateInstant.get()));
        }

        if (toDateInstant.isPresent()) {
            specification = SpecificationUtils.andComponent(specification, SubmissionSpecs.toDate(toDateInstant.get()));
        }

        if (search.isPresent()) {
            specification = SpecificationUtils.andComponent(specification, SubmissionSpecs.ofPackage(search.get()))
                    .or(SubmissionSpecs.ofSubmitter(search.get()))
                    .or(SubmissionSpecs.ofApprover(search.get()));
        }

        return handleSuccessForPagedCollection(
                submissionService.findAllBySpecification(specification, resolvedPageable), requester);
    }

    /**
     * Updates a submission.
     * @param principal used for authorization
     * @param id submission id
     * @param jsonPatch JsonPatch object
     * @throws ApplyPatchException when some internal server errors occurs
     * @throws MalformedPatchException when provided JSON Patch object is incorrect (e.g. alters non-existing fields)
     */
    @PreAuthorize("hasAuthority('user')")
    @PatchMapping(value = "/{id}", consumes = "application/json-patch+json")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "updateRSubmission")
    public @ResponseBody ResponseEntity<?> updateSubmission(
            Principal principal, @PathVariable("id") Integer id, @RequestBody JsonPatch jsonPatch)
            throws SubmissionNotFound, UserNotAuthorized, ApplyPatchException, MalformedPatchException {
        Submission submission =
                submissionService.findById(id).orElseThrow(() -> new SubmissionNotFound(messageSource, locale));

        jsonPatch = fixPatch(jsonPatch); // So that it doesn't complain when state is written with lower case

        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new SubmissionNotFound(messageSource, locale));
        RRepository repository = repositoryService
                .findById(submission.getPackage().getRepository().getId())
                .orElseThrow(() -> new SubmissionNotFound(
                        messageSource, locale)); // If repository is not found for the technology,
        // then certainly a user tries to fetch a submission for a wrong technology.

        try {
            SubmissionDto submissionDto = applyPatchToEntity(jsonPatch, submission);

            if (!securityMediator.isAuthorizedToEdit(submission, submissionDto, requester))
                throw new UserNotAuthorized(messageSource, locale);

            submissionPatchValidator.validatePatch(jsonPatch, submission, submissionDto);

            Strategy<Submission> strategy = strategyFactory.updateSubmissionStrategy(
                    submission, dtoConverter.resolveDtoToEntity(submissionDto), repository, requester);
            submission = strategyExecutor.execute(strategy);
        } catch (StrategyFailure | EntityResolutionException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new ApplyPatchException(messageSource, locale);
        } catch (JsonProcessingException | JsonException | PatchValidationException e) {
            throw new MalformedPatchException(messageSource, locale, e);
        }

        return handleSuccessForSingleEntity(submission, requester);
    }

    /**
     * This method is supposed to make state field case-insensitive.
     * @param jsonPatch to fix
     * @return fixed patch
     */
    private JsonPatch fixPatch(JsonPatch jsonPatch) {
        JsonArray jsonArray = jsonPatch.toJsonArray();
        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();

        final String value = "value";
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject obj = jsonArray.getJsonObject(i);

            if (obj.containsKey("op")
                    && obj.containsKey("path")
                    && obj.getString("op").equals("replace")
                    && obj.getString("path").equals("/state")
                    && obj.containsKey(value)) {

                JsonObjectBuilder builder = Json.createObjectBuilder()
                        .add(value, obj.getString(value).toUpperCase());

                obj.entrySet().stream()
                        .filter(e -> !e.getKey().equals(value))
                        .forEach(e -> builder.add(e.getKey(), e.getValue()));
                obj = builder.build();
            }
            arrBuilder.add(obj);
        }
        return JsonProvider.provider().createPatch(arrBuilder.build());
    }

    /**
     * Find a submission of given id
     */
    @PreAuthorize("hasAuthority('user')")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "getRSubmissionById")
    public @ResponseBody ResponseEntity<ResponseDto<EntityModel<SubmissionDto>>> getSubmissionById(
            Principal principal, @PathVariable("id") Integer id) throws SubmissionNotFound, UserNotAuthorized {
        final User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
        Submission submission =
                submissionService.findById(id).orElseThrow(() -> new SubmissionNotFound(messageSource, locale));
        return handleSuccessForSingleEntity(submission, requester);
    }

    /**
     * Erases submission from database and file system.
     * Requires admin privileges.
     */
    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteRSubmission")
    public void deleteSubmission(Principal principal, @PathVariable("id") Integer id)
            throws SubmissionNotFound, UserNotAuthorized, DeleteException {
        Optional<User> requester = userService.findActiveByLogin(principal.getName());

        if (requester.isEmpty() || requester.get().getRole().getValue() != Role.VALUE.ADMIN)
            throw new UserNotAuthorized(messageSource, locale);

        Submission submission =
                submissionService.findById(id).orElseThrow(() -> new SubmissionNotFound(messageSource, locale));

        try {
            submissionDeleter.delete(submission);
        } catch (DeleteEntityException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new DeleteException(messageSource, locale);
        }
    }
}
