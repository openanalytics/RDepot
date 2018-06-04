/**
 * R Depot
 *
 * Copyright (C) 2012-2018 Open Analytics NV
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
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
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
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionEditException;
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

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class UserService implements MessageSourceAware, LdapAuthoritiesPopulator
{
	
	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
	
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
	public User create(User user) 
	{
		User createdUser = user;
		createdUser = userRepository.save(createdUser);
		Event createEvent = eventService.findByValue("create");
		User admin;
		try
		{
			admin = findFirstAdmin();
		}
		catch(AdminNotFound e)
		{
			admin = user;
		}
		userEventService.create(createEvent, admin, createdUser);
		return createdUser;
	}
	
	public User findById(int id) 
	{
		return userRepository.findByIdAndDeleted(id, false);
	}

	public User findByLogin(String login) 
	{
		return userRepository.findByLoginIgnoreCaseAndDeleted(login, false);
	}
	
	public User findByLoginWithRepositoryMaintainers(String login) 
	{
		User user = userRepository.findByLoginIgnoreCaseAndDeleted(login, false);
		Hibernate.initialize(user.getRepositoryMaintainers());
		return user;
	}
	
	public User findByLoginWithMaintainers(String login) 
	{
		User user = userRepository.findByLoginIgnoreCaseAndDeleted(login, false);
		Hibernate.initialize(user.getRepositoryMaintainers());
		Hibernate.initialize(user.getPackageMaintainers());
		return user;
	}
	
	public User findByLoginEvenDeleted(String login) 
	{
		return userRepository.findByLoginIgnoreCase(login);
	}

	public User findByEmail(String email) 
	{
		return userRepository.findByEmailAndDeleted(email, false);
	}
	
	public User findByEmailEvenDeleted(String email) 
	{
		return userRepository.findByEmail(email);
	}
	
	@Transactional(readOnly=false, rollbackFor={UserNotFound.class})
	public User shiftDelete(int id) throws UserNotFound 
	{
		// Only make sure "deleted" users can be "shift deleted"
		User deletedUser = userRepository.findByIdAndDeleted(id, true);
		if (deletedUser == null)
			throw new UserNotFound();
		deleteEvents(deletedUser);
		userRepository.delete(deletedUser);
		return deletedUser;
	}
	
	@Transactional(readOnly = false)
	public void deleteEvents(User user)
	{
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
	public User delete(int id, User deleter) throws UserDeleteException 
	{
		User deletedUser = userRepository.findByIdAndDeleted(id, false);
		Event deleteEvent = eventService.findByValue("delete");
		try
		{
			if (deletedUser == null)
				throw new UserNotFound();
			if (deleteEvent == null)
				throw new EventNotFound();
			deletedUser.setDeleted(true);
			switch(deletedUser.getRole().getName())
			{
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
			fixSubmissions(deletedUser, deleter);	
			userEventService.create(deleteEvent, deleter, deletedUser);
			return deletedUser;
		}
		catch(UserNotFound | AdminNotFound | SubmissionEditException | RepositoryEditException | PackageEditException | PackageMaintainerDeleteException | RepositoryMaintainerDeleteException | EventNotFound e)
		{
			throw new UserDeleteException(e.getMessage());
		}
	}
	
	@Transactional(readOnly = false)
	public void fixSubmissions(User user, User deleter) throws SubmissionEditException
	{
		for(Submission submission : user.getSubmissions())
			submissionService.update(submissionService.chooseBestSubmitter(submission), deleter);
	}

	public List<User> findAll() 
	{
		return userRepository.findByDeleted(false, Sort.by(new Order(Direction.ASC, "name")));
	}
	
	public List<User> findByDeleted(boolean deleted) 
	{
		return userRepository.findByDeleted(deleted, Sort.by(new Order(Direction.ASC, "name")));
	}

	@Transactional(readOnly = false, rollbackFor={UserEditException.class})
	public User update(User user, User admin) throws UserEditException
	{
		User updatedUser = userRepository.findByIdAndDeleted(user.getId(), false);
		Event updateEvent = eventService.getUpdateEvent();
		
		if(admin == null)
		{
			try
			{
				admin = findFirstAdmin();
			}
			catch(AdminNotFound e)
			{
				admin = user;
			}
		}
		
		try
		{
			if (updatedUser == null)
				throw new UserNotFound();
			
			if(updateEvent == null)
				throw new EventNotFound();
			
			List<UserEvent> events = new ArrayList<UserEvent>();
			
			if(updatedUser.getRole().getId() != user.getRole().getId())
			{
				events.add(new UserEvent(0, new Date(), admin, user, updateEvent, "role", "" + updatedUser.getRole().getId(), "" + user.getRole().getId(), new Date()));
				switch(updatedUser.getRole().getName())
				{
					case "admin":
						if(isOnlyAdminLeft())
							throw new AdminNotFound();
						else
						{
							updatedUser.setRole(user.getRole());
							deleteAdmin(updatedUser, admin);
						}
						break;
					case "packagemaintainer":
						updatedUser.setRole(user.getRole());
						deletePackageMaintainers(updatedUser, admin);					
						break;
					case "repositorymaintainer":
						updatedUser.setRole(user.getRole());
						deleteRepositoryMaintainers(updatedUser, admin);
						break;
					default:
						updatedUser.setRole(user.getRole());
						break;
				}
			}
			
//			if(!updatedUser.getName().equals(user.getName()))
//			{
//				events.add(new UserEvent(0, new Date(), admin, user, updateEvent, "name", updatedUser.getName(), user.getName(), new Date()));
//				updatedUser.setName(user.getName());
//			}
//			
//			if(!updatedUser.getEmail().equals(user.getEmail()))
//			{
//				events.add(new UserEvent(0, new Date(), admin, user, updateEvent, "email", updatedUser.getEmail(), user.getEmail(), new Date()));
//				updatedUser.setEmail(user.getEmail());
//			}
//			if(!updatedUser.getLogin().equals(user.getLogin()))
//			{
//				events.add(new UserEvent(0, new Date(), admin, user, updateEvent, "login", updatedUser.getLogin(), user.getLogin(), new Date()));
//				updatedUser.setLogin(user.getLogin());
//			}
			if(updatedUser.isActive() != user.isActive())
			{
				events.add(new UserEvent(0, new Date(), admin, user, updateEvent, "active", "" + updatedUser.isActive(), "" + user.isActive(), new Date()));
				updatedUser.setActive(user.isActive());
			}
			if(updatedUser.getLastLoggedInOn() != user.getLastLoggedInOn())
			{
				events.add(new UserEvent(0, new Date(), admin, user, updateEvent, "last logged in", "" + updatedUser.getLastLoggedInOn(), "" + user.getLastLoggedInOn(), new Date()));
				updatedUser.setLastLoggedInOn(user.getLastLoggedInOn());
			}
			
			//updatedUser.setHashedPassword(user.getHashedPassword());
			// setLogin? -> Principal has to change as well, so maybe redirect to logout then?			
			
			for(UserEvent rEvent : events)
			{
				rEvent = userEventService.create(rEvent);
			}
			
			return updatedUser;
		}
		catch(UserNotFound | AdminNotFound | PackageEditException | RepositoryEditException | PackageMaintainerDeleteException | RepositoryMaintainerDeleteException | EventNotFound e)
		{
			throw new UserEditException(e.getMessage());
		}
	}
	
	@Transactional(readOnly = false)
	public void deletePackageMaintainers(User user, User updater) throws PackageMaintainerDeleteException, PackageEditException, RepositoryEditException
	{
		Set<PackageMaintainer> packageMaintainers = user.getPackageMaintainers();
		for(PackageMaintainer packageMaintainer : packageMaintainers)
		{
			if(!packageMaintainer.isDeleted())
			{
				String packageName = packageMaintainer.getPackage();
				Repository repository = packageMaintainer.getRepository();
				
				packageMaintainerService.delete(packageMaintainer.getId(), updater);
				List<Package> packages = packageService.findByNameAndRepository(packageName, repository);
				for(Package p : packages)
					packageService.update(packageService.chooseBestMaintainer(p), updater);
			}
		}
	}
	
	@Transactional(readOnly = false)
	public void deleteAdmin(User user, User deleter) throws PackageEditException, RepositoryEditException
	{
		List<Package> packages = packageService.findAll();
		for(Package p : packages)
		{
			packageService.update(packageService.chooseBestMaintainer(p), deleter);
		}
	}
	

	public boolean isOnlyAdminLeft() throws AdminNotFound
	{
		Role role = roleService.findByName("admin");
		if(role == null)
			throw new AdminNotFound(); 
		else if(role.getUsers().size() < 2)
			return true;
		else
			return false;
	}
	
	@Transactional(readOnly = false)
	public void deleteRepositoryMaintainers(User user, User updater) throws PackageEditException, RepositoryMaintainerDeleteException, RepositoryEditException
	{
		Set<RepositoryMaintainer> repositoryMaintainers = user.getRepositoryMaintainers();
		for(RepositoryMaintainer repositoryMaintainer : repositoryMaintainers)
		{
			if(!repositoryMaintainer.isDeleted())
			{
				Repository repository = repositoryMaintainer.getRepository();
				repositoryMaintainerService.delete(repositoryMaintainer.getId(), updater);
				List<Package> packages = packageService.findByRepositoryAndMaintainer(repository, user);
				for(Package p : packages)
					packageService.update(packageService.chooseBestMaintainer(p), updater);
			}
		}
	}
	
//	@Transactional
//	public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException 
//	{
//		User user = userRepository.findByLogin(login);
//		
//		if (user == null)
//			throw new UsernameNotFoundException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
//		
//		Collection<SimpleGrantedAuthority> authorities = new HashSet<SimpleGrantedAuthority>(0);
//		
//		for(int i = 0, v = user.getRole().getValue(); i <= v; i++)
//		{
//			authorities.add(new SimpleGrantedAuthority(roleService.findByValue(i).getName()));
//		}
//		
//		UserDetails springUser = new org.springframework.security.core.userdetails.User(login, user.getHashedPassword(), user.isActive(), true, true, true, authorities);
//		
//		user.setLastLoggedInOn(new Date());
//		
//		return springUser;
//	}

	public void setMessageSource(MessageSource messageSource)
	{
		this.messages = new MessageSourceAccessor(messageSource);
		
	}

	public List<User> findByRole(Role role) 
	{
		return userRepository.findByRoleAndDeleted(role, false);
	}

	public User findFirstAdmin() throws AdminNotFound 
	{
		Role role = roleService.findByName("admin");
		if(role == null)
			throw new AdminNotFound();
		List<User> admins = findByRole(role);
		if(admins.size() < 1)
			throw new AdminNotFound();
		else
			return admins.get(0);
	}
	
	public List<User> findEligiblePackageMaintainers()
	{
		ArrayList<User> users = new ArrayList<User>();
		users.addAll(findByRole(roleService.findByName("user")));
		users.addAll(findByRole(roleService.findByName("packagemaintainer")));
		Collections.sort(users, new UserComparator());
		return users;
	}
	
	public static boolean isAuthorizedToCancel(Submission submission, User requester)
	{
		if(submission.getUser().getId() == requester.getId())
			return true;
		else
			return isAuthorizedToAccept(submission, requester);
	}

	public static boolean isAuthorizedToAccept(Submission submission, User requester)
	{
		return isAuthorizedToEdit(submission.getPackage(), requester);
	}

	public static boolean isAuthorizedToEdit(Package packageBag, User requester)
	{
		if(packageBag.getUser().getId() == requester.getId())
			return true;
		switch(requester.getRole().getName())
		{
			case "admin":
				return true;
			case "repositorymaintainer":
				for(RepositoryMaintainer repositoryMaintainer : requester.getRepositoryMaintainers())
				{
					if(repositoryMaintainer.getRepository().getId() == packageBag.getRepository().getId())
						return true;	
				}
				break;
			case "packagemaintainer":
				for(PackageMaintainer packageMaintainer : requester.getPackageMaintainers())
				{
					if(packageMaintainer.getRepository().getId() == packageBag.getRepository().getId() && Objects.equals(packageMaintainer.getPackage(), packageBag.getName()))
						return true;	
				}
				break;
		}
		return false;
	}
	
	public static boolean isAuthorizedToEdit(Repository repository, User requester)
	{
		switch(requester.getRole().getName())
		{
			case "admin":
				return true;
			case "repositorymaintainer":
				for(RepositoryMaintainer repositoryMaintainer : requester.getRepositoryMaintainers())
				{
					if(repositoryMaintainer.getRepository().getId() == repository.getId())
						return true;	
				}
				break;
		}
		return false;
	}

	public boolean isAuthorizedToEdit(PackageMaintainer packageMaintainer, User requester)
	{
		switch(requester.getRole().getName())
		{
			case "admin":
				return true;
			case "repositorymaintainer":
				for(RepositoryMaintainer repositoryMaintainer : requester.getRepositoryMaintainers())
				{
					if(repositoryMaintainer.getRepository().getId() == packageMaintainer.getRepository().getId())
						return true;	
				}
				break;
		}
		return false;
	}

	public List<User> findEligibleRepositoryMaintainers() 
	{
		ArrayList<User> users = new ArrayList<User>();
		users.addAll(findByRole(roleService.findByName("user")));
		users.addAll(findByRole(roleService.findByName("repositorymaintainer")));
		Collections.sort(users, new UserComparator());
		return users;
	}
	
	@Transactional
	@Override
	public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) 
	{
		String login = userData.getStringAttribute(ldapLoginfield);
		User user = userRepository.findByLoginIgnoreCase(login);	
		Collection<SimpleGrantedAuthority> authorities = new HashSet<SimpleGrantedAuthority>(0);
		
		for(int i = 0, v = user.getRole().getValue(); i <= v; i++)
		{
			authorities.add(new SimpleGrantedAuthority(roleService.findByValue(i).getName()));
		}		
		return authorities;
	}
}
