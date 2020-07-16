/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
package eu.openanalytics.rdepot.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.comparator.UserComparator;
import eu.openanalytics.rdepot.exception.AdminNotFound;
import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.PackageMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.PackageMaintainerNotFound;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionEditException;
import eu.openanalytics.rdepot.exception.UserActivateException;
import eu.openanalytics.rdepot.exception.UserCreateException;
import eu.openanalytics.rdepot.exception.UserDeactivateException;
import eu.openanalytics.rdepot.exception.UserDeleteException;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.exception.UserNotFound;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageEvent;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.PackageMaintainerEvent;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.RepositoryMaintainerEvent;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.SubmissionEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.model.UserEvent;
import eu.openanalytics.rdepot.repository.UserRepository;
import eu.openanalytics.rdepot.time.DateProvider;
import eu.openanalytics.rdepot.warning.UserAlreadyActivatedWarning;
import eu.openanalytics.rdepot.warning.UserAlreadyDeactivatedWarning;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class UserService implements MessageSourceAware, LdapAuthoritiesPopulator
{
	
	//protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
	Locale locale = LocaleContextHolder.getLocale();
	Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Resource
	MessageSource messageSource;
	
	@Resource
	private UserRepository userRepository;
	
	@Resource
	private RoleService roleService;
	
	@Resource
	private UserEventService userEventService;
	
	@Resource
	private PackageEventService packageEventService;
	
	@Resource
	private RepositoryEventService repositoryEventService;
	
	@Resource
	private RepositoryMaintainerEventService repositoryMaintainerEventService;
	
	@Resource
	private PackageMaintainerEventService packageMaintainerEventService;
	
	@Resource
	private SubmissionEventService submissionEventService;
	
	@Resource
	private EventService eventService;
	
	@Resource
	private PackageService packageService;
	
	@Resource
	private SubmissionService submissionService;
	
	@Resource
	private PackageMaintainerService packageMaintainerService;
	
	@Resource
	private RepositoryMaintainerService repositoryMaintainerService;
	
	@Value("${ldap.loginfield}")
	private String ldapLoginfield;

	@Transactional(readOnly = false)
	public User create(User user) throws UserCreateException {
		User createdUser = user;
		User admin;
		Event createEvent = null;
		createdUser = userRepository.save(createdUser);
		
		try {
			createEvent = eventService.getCreateEvent();
			admin = findFirstAdmin();
		} catch(EventNotFound e) {
			logger.error(e.getClass().getName() + ": ", e.getMessage(), e);
			throw new UserCreateException(messageSource, locale, user);
		} catch(AdminNotFound e) {
			admin = user;
		}
		
		userEventService.create(createEvent, admin, createdUser);
		return createdUser;
	}
	
	public User findById(int id) {
		return userRepository.findByIdAndDeleted(id, false);
	}

	public User findByLogin(String login) {
		return userRepository.findByLoginIgnoreCaseAndDeleted(login, false);
	}
	
	public User findByLoginWithRepositoryMaintainers(String login) {
		User user = userRepository.findByLoginIgnoreCaseAndDeleted(login, false);
//		TODO Is it necessary? 
		Hibernate.initialize(user.getRepositoryMaintainers());
		return user;
	}
	
	public User findByLoginWithMaintainers(String login) {
		User user = userRepository.findByLoginIgnoreCaseAndDeleted(login, false);
		Hibernate.initialize(user.getRepositoryMaintainers());
		Hibernate.initialize(user.getPackageMaintainers());
		return user;
	}
	
	public User findByLoginEvenDeleted(String login) {
		return userRepository.findByLoginIgnoreCase(login);
	}

	public User findByEmail(String email) {
		return userRepository.findByEmailAndDeleted(email, false);
	}
	
	public User findByEmailEvenDeleted(String email) {
		return userRepository.findByEmail(email);
	}
	
	@Transactional(readOnly=false, rollbackFor={UserNotFound.class})
	public User shiftDelete(int id) throws UserNotFound {
		// Only make sure "deleted" users can be "shift deleted"
		User deletedUser = userRepository.findByIdAndDeleted(id, true);
		if (deletedUser == null)
			throw new UserNotFound(messageSource, locale, id);
		deleteEvents(deletedUser);
		userRepository.delete(deletedUser);
		return deletedUser;
	}
	
	@Transactional(readOnly = false)
	public void deleteEvents(User user) {
		for(UserEvent event : user.getUserEvents())
			userEventService.delete(event.getId());
		for(UserEvent event : user.getChangedUserEvents())
			userEventService.delete(event.getId());
		for(PackageEvent event : user.getChangedPackageEvents())
			packageEventService.delete(event.getId());
		for(RepositoryEvent event : user.getChangedRepositoryEvents())
			repositoryEventService.delete(event.getId());
		for(PackageMaintainerEvent event : user.getChangedPackageMaintainerEvents())
			packageMaintainerEventService.delete(event.getId());
		for(RepositoryMaintainerEvent event : user.getChangedRepositoryMaintainerEvents())
			repositoryMaintainerEventService.delete(event.getId());
		for(SubmissionEvent event : user.getChangedSubmissionEvents())
			submissionEventService.delete(event.getId());
	}
	
	@Transactional(readOnly = false, rollbackFor={UserDeleteException.class})
	public User delete(int id, User deleter) throws UserDeleteException, UserNotFound {
		try {
			User deletedUser = userRepository.findByIdAndDeleted(id, false);
			if (deletedUser == null)
				throw new UserNotFound(messageSource, locale, id);
			
			Event deleteEvent = eventService.getDeleteEvent();
			
			switch(deletedUser.getRole().getName()) {
				case "admin":
					if(isOnlyAdminLeft())
						throw new AdminNotFound();
					else
						deleteAdmin(deletedUser, deleter);
					break;
				case "packagemaintainer":
					deletePackageMaintainers(deletedUser, deleter);					
					break;
				case "repositorymaintainer":
					deleteRepositoryMaintainers(deletedUser, deleter);
					break;
			}
			submissionService.fixSubmissions(deletedUser.getSubmissions(), deleter);
			userEventService.create(deleteEvent, deleter, deletedUser);
			
			deletedUser.setDeleted(true);
			return deletedUser;
		}
		catch(AdminNotFound | RepositoryEditException | PackageEditException | PackageMaintainerDeleteException | RepositoryMaintainerDeleteException | EventNotFound | SubmissionEditException e) {
			logger.error(e.getClass().getName() + ": ", e.getMessage(), e);
			throw new UserDeleteException(messageSource, locale, id);
		}
	}
	
	public List<User> findAll() {
		return userRepository.findByDeleted(false, Sort.by(new Order(Direction.ASC, "name")));
	}
	
	public List<User> findByDeleted(boolean deleted) {
		return userRepository.findByDeleted(deleted, Sort.by(new Order(Direction.ASC, "name")));
	}
	
	@Transactional(readOnly = false, rollbackFor={UserEditException.class})
	public void activateUser(User user, User updater) throws UserActivateException, UserAlreadyActivatedWarning {
		if(updater == null)
			updater = chooseBestUpdater(user);
		
		if(user.isActive())
			throw new UserAlreadyActivatedWarning(messageSource, locale, user);
		
		try {
			Event updateEvent = eventService.getUpdateEvent();
			user.setActive(true);
			UserEvent activateUserEvent = new UserEvent(0, DateProvider.now(), updater, user, updateEvent, "active", "" + !user.isActive(), "" + user.isActive(), DateProvider.now());
			update(activateUserEvent);
		} catch (EventNotFound e) {
			logger.error(e.getClass().getName() + ": ", e.getMessage(), e);
			throw new UserActivateException(messageSource, locale, user);
		}
	}
	
	@Transactional(readOnly = false, rollbackFor={UserEditException.class})
	public void deactivateUser(User user, User updater) throws UserDeactivateException, UserAlreadyDeactivatedWarning {
		if(updater == null)
			updater = chooseBestUpdater(user);
		
		if(!user.isActive())
			throw new UserAlreadyDeactivatedWarning(messageSource, locale, user);
		
		try {
			Event updateEvent = eventService.getUpdateEvent();
			user.setActive(false);
			UserEvent activateUserEvent = new UserEvent(0, DateProvider.now(), updater, user, updateEvent, "active", "" + !user.isActive(), "" + user.isActive(), DateProvider.now());
			update(activateUserEvent);
		} catch (EventNotFound e) {
			logger.error(e.getClass().getName() + ": ", e.getMessage(), e);
			throw new UserDeactivateException(messageSource, locale, user);
		}
	}
	
	@Transactional(readOnly = false, rollbackFor={UserEditException.class})
	public void updateLastLoggedInOn(User user, User updater, Date newLastLoggedInOn) throws UserEditException {
		if(updater == null)
			updater = chooseBestUpdater(user);
			
		Date currentLastLoggedInOn = user.getLastLoggedInOn();
		try {
			Event updateEvent = eventService.getUpdateEvent();
			user.setLastLoggedInOn(newLastLoggedInOn);
			UserEvent updateLastLoggedInOnEvent = new UserEvent(0, DateProvider.now(), updater, user, updateEvent, "last logged in", "" + currentLastLoggedInOn, "" + user.getLastLoggedInOn(), DateProvider.now());
			update(updateLastLoggedInOnEvent);
		} 
		catch (EventNotFound e) {
			logger.error(e.getClass().getName() + ": ", e.getMessage(), e);
			throw new UserEditException(messageSource, locale, user);
		}
	}
	
	private User chooseBestUpdater(User user) {
		try {
			return findFirstAdmin();
		} catch (AdminNotFound e) {
			return user; //TODO: if there is no admin, should a standard user be allowed to activate itself?
		}
	}
	
	@Transactional(readOnly = false)
	public void updateRole(User user, User updater, Role newRole) throws UserEditException {
		if(updater == null)
			updater = chooseBestUpdater(user);
		
		Role currentRole = user.getRole();
				
		try {
			switch(user.getRole().getName()) {
				case "admin":
					if(isOnlyAdminLeft())
						throw new AdminNotFound();
					else {
						user.setRole(newRole);
						deleteAdmin(user, updater);
					}
					break;
				case "packagemaintainer":
					user.setRole(newRole);
					deletePackageMaintainers(user, updater);					
					break;
				case "repositorymaintainer":
					//123
					user.setRole(newRole);
					deleteRepositoryMaintainers(user, updater);
					break;
				default:
					user.setRole(newRole);
					break;
			}
			
			Event updateEvent = eventService.getUpdateEvent();
			
			UserEvent updateRoleEvent = new UserEvent(0, DateProvider.now(), updater, user, updateEvent, "role", "" + currentRole.getId(), "" + newRole.getId(), DateProvider.now());
			update(updateRoleEvent);
		} 
		catch(AdminNotFound | PackageEditException | RepositoryMaintainerDeleteException | RepositoryEditException | PackageMaintainerDeleteException | EventNotFound e) {
			logger.error(e.getClass().getName() + ": ", e.getMessage(), e);
			throw new UserEditException(messageSource, locale, user);
		}
	}
	
	@Transactional(readOnly = false)
	public void evaluateAndUpdate(User user, User updater) throws UserEditException, UserNotFound {
		User currentUser = userRepository.findByIdAndDeleted(user.getId(), false);
		
		if(currentUser == null)
			throw new UserNotFound(messageSource, locale, user.getId());
		
		if(currentUser.getRole().getId() != user.getRole().getId())
			updateRole(currentUser, updater, user.getRole());
		try {
			if(currentUser.isActive() != user.isActive())
			{
				if(user.isActive())
					activateUser(currentUser, updater);
				else
					deactivateUser(currentUser, updater);
			}
		} catch(UserAlreadyActivatedWarning | UserAlreadyDeactivatedWarning w) {
			logger.warn(w.getClass().getName() + ": ", w.getMessage(), w);
		} catch (UserActivateException | UserDeactivateException e) {
			logger.error(e.getClass().getName() + ": ", e.getMessage(), e);
			throw new UserEditException(messageSource, locale, user);
		}
		
		if(currentUser.getLastLoggedInOn() != user.getLastLoggedInOn())
			updateLastLoggedInOn(currentUser, updater, user.getLastLoggedInOn());
	}
	
	@Transactional(readOnly = false)
	private void update(UserEvent updateEvent) {		
		userEventService.create(updateEvent);
	}
	
	@Transactional(readOnly = false)
	public void deletePackageMaintainers(User user, User updater) 
			throws PackageMaintainerDeleteException, PackageEditException, RepositoryEditException {
		Set<PackageMaintainer> packageMaintainers = user.getPackageMaintainers();
		for(PackageMaintainer packageMaintainer : packageMaintainers) {
			if(!packageMaintainer.isDeleted()) {
				try {
					packageMaintainerService.delete(packageMaintainer.getId(), updater);
					
					List<Package> packages = packageService.findByNameAndRepository(packageMaintainer.getPackage(), packageMaintainer.getRepository());
					for(Package p : packages) {
						//packageService.update(packageService.chooseBestMaintainer(p), updater);
						packageService.refreshMaintainer(p, updater);
					}
				} catch (PackageMaintainerNotFound e) {
					logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
				}				
			}
		}
	}
	
	@Transactional(readOnly = false)
	public void deleteAdmin(User user, User deleter) throws PackageEditException, RepositoryEditException {
		List<Package> packages = packageService.findAll();
		for(Package p : packages) {
			packageService.refreshMaintainer(p, deleter);
		}
	}
	

	public boolean isOnlyAdminLeft() throws AdminNotFound {
		Role role = roleService.getAdminRole();
		if(role == null)
			throw new AdminNotFound(); 
		else if(role.getUsers().size() < 2)
			return true;
		else
			return false;
	}
	
	@Transactional(readOnly = false)
	public void deleteRepositoryMaintainers(User user, User updater) throws RepositoryMaintainerDeleteException, PackageEditException {
		Set<RepositoryMaintainer> repositoryMaintainers = user.getRepositoryMaintainers();
		for(RepositoryMaintainer repositoryMaintainer : repositoryMaintainers) {
			if(!repositoryMaintainer.isDeleted()) 				
				repositoryMaintainerService.delete(repositoryMaintainer.getId(), updater);
				
				//TODO: Is it necessary?
				
				List<Package> packages = packageService.findByRepositoryAndMaintainer(repositoryMaintainer.getRepository(), user);
				for(Package p : packages)
				{
					//packageService.update(packageService.chooseBestMaintainer(p), updater);
					packageService.refreshMaintainer(p, updater);
				}
		}
	}

	public void setMessageSource(MessageSource messageSource){
		//this.messages = new MessageSourceAccessor(messageSource); //TODO do we need to use accessor?
		this.messageSource = messageSource;
	}

	public List<User> findByRole(Role role) {
		return userRepository.findByRoleAndDeleted(role, false);
	}

	public User findFirstAdmin() throws AdminNotFound {
		Role role = roleService.getAdminRole();
		if(role == null)
			throw new AdminNotFound();
		List<User> admins = findByRole(role);
		if(admins.size() < 1)
			throw new AdminNotFound();
		else
			return admins.get(0);
	}
	
	public List<User> findEligiblePackageMaintainers() {
		ArrayList<User> users = new ArrayList<User>();
		users.addAll(findByRole(roleService.getUserRole()));
		users.addAll(findByRole(roleService.getPackageMaintainerRole()));
		Collections.sort(users, new UserComparator());
		return users;
	}
	
	public boolean isAuthorizedToCancel(Submission submission, User requester) {
		if(submission.getUser().getId() == requester.getId())
			return true;
		else
			return isAuthorizedToAccept(submission, requester);
	}

	public boolean isAuthorizedToAccept(Submission submission, User requester) {
		return isAuthorizedToEdit(submission.getPackage(), requester);
	}

	public boolean isAuthorizedToEdit(Package packageBag, User requester) {
		if(packageBag.getUser().getId() == requester.getId())
			return true;
		switch(requester.getRole().getName()) {
			case "admin":
				return true;
			case "repositorymaintainer":
				for(RepositoryMaintainer repositoryMaintainer : requester.getRepositoryMaintainers()) {
					if(repositoryMaintainer.getRepository().getId() == packageBag.getRepository().getId())
						return true;	
				}
				break;
			case "packagemaintainer":
				for(PackageMaintainer packageMaintainer : requester.getPackageMaintainers()) {
					if(packageMaintainer.getRepository().getId() == packageBag.getRepository().getId() && Objects.equals(packageMaintainer.getPackage(), packageBag.getName()))
						return true;	
				}
				break;
		}
		return false;
	}
	
	public boolean isAuthorizedToEdit(Repository repository, User requester) {
		switch(requester.getRole().getName()) {
			case "admin":
				return true;
			case "repositorymaintainer":
				for(RepositoryMaintainer repositoryMaintainer : requester.getRepositoryMaintainers()) {
					if(repositoryMaintainer.getRepository().getId() == repository.getId()
							&& !repositoryMaintainer.isDeleted())
						return true;	
				}
				break;
		}
		return false;
	}

	public boolean isAuthorizedToEdit(PackageMaintainer packageMaintainer, User requester) {
		switch(requester.getRole().getName()) {
			case "admin":
				return true;
			case "repositorymaintainer":
				for(RepositoryMaintainer repositoryMaintainer : requester.getRepositoryMaintainers()) {
					if(repositoryMaintainer.getRepository().getId() == packageMaintainer.getRepository().getId())
						return true;	
				}
				break;
		}
		return false;
	}

	public List<User> findEligibleRepositoryMaintainers() {
		ArrayList<User> users = new ArrayList<User>();
		users.addAll(findByRole(roleService.findByName("user"))); //TODO: should user be really able to become repository maintainer?
		users.addAll(findByRole(roleService.findByName("repositorymaintainer")));
		Collections.sort(users, new UserComparator());
		return users;
	}
	
	@Transactional
	@Override
	public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {
		String login = userData.getStringAttribute(ldapLoginfield);
		User user = userRepository.findByLoginIgnoreCase(login);	
		Collection<SimpleGrantedAuthority> authorities = new HashSet<SimpleGrantedAuthority>(0);
		
		for(int i = 0, v = user.getRole().getValue(); i <= v; i++) {
			authorities.add(new SimpleGrantedAuthority(roleService.findByValue(i).getName()));
		}		
		return authorities;
	}
}
