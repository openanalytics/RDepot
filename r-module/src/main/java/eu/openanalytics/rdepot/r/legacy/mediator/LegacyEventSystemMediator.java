/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.r.legacy.mediator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.technology.RLanguage;

@Component
public class LegacyEventSystemMediator {
	private final RRepositoryService repositoryService;
	private final RepositoryMaintainerService repositoryMaintainerService;
	private final NewsfeedEventService newsfeedEventService;
	
	private static final class DateComparator implements Comparator<LocalDate> {

		@Override
		public int compare(LocalDate lhs, LocalDate rhs) {
			 if (lhs.toEpochDay() > rhs.toEpochDay())
	             return -1;
	         else if (lhs.toEpochDay() == rhs.toEpochDay())
	             return 0;
	         else
	             return 1;
		}
		
	}
	
	public LegacyEventSystemMediator(RRepositoryService repositoryService,
			NewsfeedEventService newsfeedEventService,
			RepositoryMaintainerService repositoryMaintainerService) {
		this.repositoryService = repositoryService;
		this.repositoryMaintainerService = repositoryMaintainerService;
		this.newsfeedEventService = newsfeedEventService;
	}
	
	public Map<LocalDate, List<NewsfeedEvent>> findLatestRepositoryEventsByUser(
			User requester, LocalDate lastRefreshedDate, int lastEventId) {
		Map<LocalDate, List<NewsfeedEvent>> content = findRepositoryEventsByUser(requester);
		
		content.entrySet().removeIf(e -> e.getKey().compareTo(lastRefreshedDate) < 0);
		List<NewsfeedEvent> oldest = content.get(lastRefreshedDate);
		
		if(oldest != null) {
			oldest.removeIf(e -> e.getDate().compareTo(lastRefreshedDate) == 0 && e.getId() <= lastEventId);
		}
		
		return content;
	}
	
	public Map<LocalDate, List<NewsfeedEvent>> findRepositoryEventsByUser(User requester) {
//		Map<LocalDate, List<NewsfeedEvent>> content = new TreeMap<>(new DateComparator());
		List<RRepository> repositories = findRepositoriesMaintainedBy(requester, true);
		List<NewsfeedEvent> sortedEvents = new ArrayList<>();
		
		for(RRepository repository : repositories) {
			sortedEvents.addAll(newsfeedEventService.findByRepository(repository));
		}
		
		sortedEvents.sort(new Comparator<NewsfeedEvent>() {

			@Override
			public int compare(NewsfeedEvent lhs, NewsfeedEvent rhs) {
				return -1 * lhs.getTime().compareTo(rhs.getTime());					
			}
			
		});
		
		return sortedEvents.stream().collect(Collectors.groupingBy(e -> e.getDate()));
//		sortedEvents.stream().collect(Collectors.toMap(NewsfeedEvent::getDate, NewsfeedEvent))
//		for(LocalDate date : dates) {
//			List<NewsfeedEvent> sortedEvents = new ArrayList<>();
//			
//			for(RRepository repository : repositories) {
//				sortedEvents.addAll(newsfeedEventService.findByDateAndRepository(date, repository));
//			}
//			
//			sortedEvents.sort(new Comparator<NewsfeedEvent>() {
//
//				@Override
//				public int compare(NewsfeedEvent lhs, NewsfeedEvent rhs) {
//					return -1 * lhs.getTime().compareTo(rhs.getTime());					
//				}
//				
//			});
//			content.put(date, sortedEvents);
//		}
		
//		return content;
	}

	private List<LocalDate> getUniqueRepositoryEventDates() {
		return new ArrayList<>(
				newsfeedEventService.findAll().stream()
				.map(e -> e.getDate()).collect(Collectors.toSet())
		);
	}
	
	public Map<LocalDate, List<NewsfeedEvent>> 
		findPackageEventsByUserAndPackage(User requester, RPackage packageBag) {
		TreeMap<LocalDate, List<NewsfeedEvent>> events = new TreeMap<>(new DateComparator());
		List<LocalDate> dates = getUniquePackageEventDates(packageBag);
		
		for(LocalDate date : dates) {
			List<NewsfeedEvent> pEvents = 
					newsfeedEventService.findByDateAndResource(date, packageBag)
					.stream().map(e -> (NewsfeedEvent)e).collect(Collectors.toList());
			pEvents.sort(new Comparator<NewsfeedEvent>() {

				@Override
				public int compare(NewsfeedEvent lhs, NewsfeedEvent rhs) {
					if(lhs.getTime().toLocalTime().toSecondOfDay() > 
						rhs.getTime().toLocalTime().toSecondOfDay()) {
						return -1;
					} else if(lhs.getTime().toLocalTime().toSecondOfDay() ==
						rhs.getTime().toLocalTime().toSecondOfDay()) {
						return 0;
					} else {
						return 1;
					}
				}
			});
			events.put(date, pEvents);
		}
		
		return events;
	}
	
	private List<LocalDate> getUniquePackageEventDates(RPackage packageBag) {
		return new ArrayList<>(
				newsfeedEventService.findAllByRelatedResourceType(ResourceType.PACKAGE).stream()
				.filter(e -> e.getRelatedResource().getId() == packageBag.getId() &&
						e.getRelatedResource().getResourceType() == packageBag.getResourceType())
				.map(e -> e.getTime().toLocalDate()).collect(Collectors.toSet())
		);
	}

	private List<RRepository> findRepositoriesMaintainedBy(User requester, boolean includeDeleted) {
		List<RRepository> repositories = new ArrayList<>();
		
		if(requester.getRole().getValue() == Role.VALUE.ADMIN) {
			return repositoryService.findAll();
		}
		
		repositoryMaintainerService.findByUserWithoutDeleted(requester)
			.stream().filter(m -> m.getRepository().getTechnology() == RLanguage.instance)
			.forEach(
					m -> repositoryService.findById(m.getRepository().getId())
					.ifPresent(r -> repositories.add(r)));
		
		return repositories;
	}
}
