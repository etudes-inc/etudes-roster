/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/roster/trunk/roster-impl/impl/src/java/org/etudes/roster/impl/RosterServiceImpl.java $
 * $Id: RosterServiceImpl.java 10618 2015-04-25 23:07:36Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2009, 2010, 2011, 2012, 2014, 2015 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.roster.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.roster.api.RosterService;
import org.etudes.roster.api.SiteEnrollment;
import org.etudes.roster.api.SiteVisits;
import org.etudes.roster.api.TermEnrollment;
import org.etudes.util.Different;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.util.StringUtil;

/**
 * RosterServiceImpl implements RosterService
 */
public class RosterServiceImpl extends ScheduleServiceImpl implements RosterService
{
	public class SiteEnrollmentImpl implements SiteEnrollment
	{
		protected String client = null;
		protected Integer numGuests = null;
		protected Integer numOthers = null;
		protected Integer numSeats = null;
		protected String siteId = null;
		protected String siteTitle = null;
		protected String term = null;

		public SiteEnrollmentImpl(String client, String term, Integer numGuests, Integer numOthers, Integer numSeats, String siteId, String siteTitle)
		{
			this.client = client;
			this.term = term;
			this.numGuests = numGuests;
			this.numOthers = numOthers;
			this.numSeats = numSeats;
			this.siteId = siteId;
			this.siteTitle = siteTitle;
		}

		public String getClient()
		{
			return this.client;
		}

		public Integer getNumGuests()
		{
			return this.numGuests;
		}

		public Integer getNumOther()
		{
			return this.numOthers;
		}

		public Integer getNumSeats()
		{
			return this.numSeats;
		}

		public String getSiteId()
		{
			return this.siteId;
		}

		public String getSiteTitle()
		{
			return this.siteTitle;
		}

		public String getTerm()
		{
			return this.term;
		}
	}

	public class SiteVisitsImpl implements SiteVisits
	{
		protected String siteId = null;
		protected String siteTitle = null;
		protected Integer visitors = null;
		protected Integer visits = null;

		public SiteVisitsImpl(String siteId, String siteTitle, Integer visits, Integer visitors)
		{
			this.siteId = siteId;
			this.siteTitle = siteTitle;
			this.visits = visits;
			this.visitors = visitors;
		}

		public String getSiteId()
		{
			return this.siteId;
		}

		public String getSiteTitle()
		{
			return this.siteTitle;
		}

		public Integer getVisitors()
		{
			return this.visitors;
		}

		public Integer getVisits()
		{
			return this.visits;
		}
	}

	public class TermEnrollmentImpl implements TermEnrollment
	{
		protected String client = null;
		protected Integer numGuests = null;
		protected Integer numOthers = null;
		protected Integer numSeats = null;
		protected Integer numSections = null;
		protected Integer numSites = null;
		protected String term = null;

		public TermEnrollmentImpl(String client, String term, Integer numGuests, Integer numOthers, Integer numSeats, Integer numSections,
				Integer numSites)
		{
			this.client = client;
			this.term = term;
			this.numGuests = numGuests;
			this.numOthers = numOthers;
			this.numSeats = numSeats;
			this.numSections = numSections;
			this.numSites = numSites;
		}

		public String getClient()
		{
			return this.client;
		}

		public Integer getNumGuests()
		{
			return this.numGuests;
		}

		public Integer getNumOther()
		{
			return this.numOthers;
		}

		public Integer getNumSeats()
		{
			return this.numSeats;
		}

		public Integer getNumSections()
		{
			return this.numSections;
		}

		public Integer getNumSites()
		{
			return this.numSites;
		}

		public String getTerm()
		{
			return this.term;
		}
	}

	class Mbr
	{
		boolean active;
		String role;
		String userId;
	}

	class RosterLine
	{
		String eid;
		String email;
		String firstName;
		String iid;
		String lastName;
		String line;
		String pw;
		String role;
		String section;
		String siteTitle;
		String status;
		String userType;
	}

	// actual role names
	public final static String ROLE_BLOCKED = "Blocked";
	public final static String ROLE_EVALUATOR = "Evaluator";
	public final static String ROLE_GUEST = "Guest";
	public final static String ROLE_INSTRUCTOR = "Instructor";
	public final static String ROLE_STUDENT = "Student";

	public final static String ROLE_TA = "Teaching Assistant";
	// acceptable strings in roster lines for roles (case insensitive)
	public final static String TYPE_EVALUATOR = "evaluator";
	public final static String TYPE_INSTRUCTOR = "instructor";

	public final static String TYPE_STUDENT = "student";

	public final static String TYPE_TA = "teachingassistant";

	protected final static String PASTED = "pasted text";

	/** Our logger. */
	private static Log M_log = LogFactory.getLog(RosterServiceImpl.class);

	/** Configuration: directory path to roster files. */
	protected String dirPath = null;

	/** Configuration: map of file names to institution codes. */
	protected Map<String, String> instCodeFileMap = new HashMap<String, String>();

	/** Configuration: map of site title prefix to institution codes. */
	protected Map<String, String> prefixMap = new HashMap<String, String>();

	/** Configuration: map of site title prefix to institution name. */
	protected Map<String, String> prefixNameMap = new HashMap<String, String>();

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void adddSectionToSite(String authenticatedUserId, String section, String siteId)
	{
		// admin only
		if (!allowRosterAccess(authenticatedUserId)) return;

		try
		{
			// get the site
			Site site = siteService().getSite(siteId);

			// see if the site already has the section
			boolean found = false;
			for (Group g : (Collection<Group>) site.getGroups())
			{
				if (g.getTitle().equalsIgnoreCase(section) && (g.getProperties().getProperty("group_prop_wsetup_created") == null))
				{
					found = true;
					break;
				}
			}

			if (!found)
			{
				// get the membership for the section
				Map<String, Mbr> members = new HashMap<String, Mbr>();

				// get the sites that have this section, in the site's same client and term
				Map<String, Site> workingSites = new HashMap<String, Site>();
				List<Site> sites = findSites(site.getTitle(), section, workingSites);
				if (!sites.isEmpty())
				{
					// consolidate membership from the existing section in all sites
					for (Site s : sites)
					{
						// get the section
						for (Group g : (Collection<Group>) s.getGroups())
						{
							if (g.getTitle().equalsIgnoreCase(section) && (g.getProperties().getProperty("group_prop_wsetup_created") == null))
							{
								for (Member m : (Set<Member>) g.getMembers())
								{
									Mbr mbr = members.get(m.getUserId());
									if (mbr == null)
									{
										mbr = new Mbr();
										mbr.active = m.isActive();
										mbr.role = m.getRole().getId();
										mbr.userId = m.getUserId();
										members.put(mbr.userId, mbr);
									}
									else
									{
										mbr.active = consolidateActive(mbr.active, m.isActive());
										mbr.role = consolidateRole(mbr.role, m.getRole().getId());
									}
								}
							}
						}
					}
				}

				// create the section
				Group g = site.addGroup();
				g.setTitle(section);
				g.getPropertiesEdit().addProperty("sections_category", "Lecture");

				// set the membership
				for (Mbr m : members.values())
				{
					g.addMember(m.userId, m.role, m.active, true);
				}

				consolidate(site);

				// save
				try
				{
					siteService().save(site);
				}
				catch (IdUnusedException e)
				{
					M_log.warn("processRosterBuffer: " + e);
				}
				catch (PermissionException e)
				{
					M_log.warn("processRosterBuffer: " + e);
				}
			}
		}
		catch (IdUnusedException e)
		{
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addUsersToSite(String authenticatedUserId, Collection<String> userIds, String siteId, String role)
	{
		// admin only
		if (!allowRosterAccess(authenticatedUserId)) return;

		// as active / provided
		try
		{
			// find the site
			Site site = siteService().getSite(siteId);

			// for Observer, assure the site has the Observer role
			if ("Observer".equals(role) && (site.getRole(role) == null))
			{
				// copy it from the !site.template.course
				try
				{
					AuthzGroup template = authzGroupService().getAuthzGroup("!site.template.course");
					Role roleTemplate = template.getRole(role);
					if (roleTemplate != null)
					{
						site.addRole(role, roleTemplate);
					}
				}
				catch (GroupNotDefinedException e)
				{
					M_log.warn("addUsersToSite: " + e);
				}
				catch (RoleAlreadyDefinedException e)
				{
					M_log.warn("addUsersToSite: " + e);
				}
			}

			for (String id : userIds)
			{
				// only for new users to the site
				Member m = site.getMember(id);
				if (m == null)
				{
					site.addMember(id, role, true, true);
				}
			}
			// save
			try
			{
				siteService().save(site);
			}
			catch (IdUnusedException e)
			{
				M_log.warn("addUsersToSite: " + e);
			}
			catch (PermissionException e)
			{
				M_log.warn("addUsersToSite: " + e);
			}
		}
		catch (IdUnusedException e)
		{
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean allowRosterAccess(String userId)
	{
		// security check - admin only
		return securityService().isSuperUser(userId);
	}

	/**
	 * {@inheritDoc}
	 */
	public String clientName(String clientPrefix)
	{
		String rv = prefixNameMap.get(clientPrefix.toLowerCase());
		if (rv == null) rv = clientPrefix;
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String findInstitutionCode(String siteTitle)
	{
		String[] parts = StringUtil.splitFirst(siteTitle, " ");
		if (parts == null || parts.length != 2) return null;
		String prefix = parts[0];

		String code = this.prefixMap.get(prefix);
		return code;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Site> findSectionSites(String clientPrefix, String termSuffix, String section)
	{
		String title = clientPrefix + " " + termSuffix;
		Map<String, Site> workingSites = new HashMap<String, Site>();
		List<Site> rv = findSites(title, section, workingSites);
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Site findSiteByTitle(String title)
	{
		Site rv = getSiteFromDB(title);
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<Site> findSitesByClientTerm(String clientPrefix, String termSuffix)
	{
		String sql = "SELECT S.SITE_ID FROM SAKAI_SITE S WHERE S.TITLE LIKE ?";
		Object[] fields = new Object[1];
		fields[0] = clientPrefix + " % " + termSuffix;
		Set<String> results = new HashSet<String>();
		results.addAll(sqlService().dbRead(sql, fields, null));

		List<Site> rv = new ArrayList<Site>();
		for (String siteId : results)
		{
			try
			{
				Site s = siteService().getSite(siteId);
				rv.add(s);
			}
			catch (IdUnusedException e)
			{
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getRosterFileNames(String authenticatedUserId)
	{
		List<String> rv = new ArrayList<String>();
		if (this.dirPath == null) return rv;

		// admin only
		if (!allowRosterAccess(authenticatedUserId)) return rv;

		// get the directory
		File dir = new File(this.dirPath);
		if ((dir == null) || (!dir.isDirectory()))
		{
			M_log.warn("processFiles: configured directory missing or not directory: " + this.dirPath);
			return rv;
		}

		// get the file list
		File files[] = dir.listFiles();
		if (files == null)
		{
			// nothing to do
			return rv;
		}

		// process each file found
		for (File file : files)
		{
			// skip "." files
			if (file.getName().startsWith(".")) continue;

			rv.add(file.getName());
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSampleRosterComment()
	{
		return "#(tab separated) TITLE user-login-id password last-name first-name email role E/D section full-user-id";
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();

		ComponentManager.whenAvailable(ServerConfigurationService.class, new Runnable()
		{
			public void run()
			{
				dirPath = serverConfigurationService().getString("etudes.roster.dir");
				if (dirPath == null)
				{
					M_log.warn("init: no directory configured for roster: missing etudes.roster.dir in properties");
					return;
				}
				String[] codes = serverConfigurationService().getStrings("etudes.roster.institution.codes");
				String[] fnames = serverConfigurationService().getStrings("etudes.roster.institution.fnames");
				String[] prefix = serverConfigurationService().getStrings("etudes.roster.institution.prefix");
				String[] names = serverConfigurationService().getStrings("etudes.roster.institution.names");
				if (codes == null || fnames == null || prefix == null || names == null || codes.length != fnames.length
						|| codes.length != prefix.length || names.length != prefix.length)
				{
					M_log.warn("etudes.roster.institution.codes, etudes.roster.institution.fnames and etudes.roster.institution.prefix not properly set in properties");
				}
				else
				{
					for (int i = 0; i < codes.length; i++)
					{
						instCodeFileMap.put(fnames[i], codes[i]);
						prefixMap.put(prefix[i], codes[i]);
						if (!names[i].equals("DUP")) prefixNameMap.put(prefix[i].toLowerCase(), names[i]);
					}
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public boolean removeSectionFromSite(String authenticatedUserId, String section, String siteId)
	{
		// admin only
		if (!allowRosterAccess(authenticatedUserId)) return false;

		boolean rv = false;
		try
		{
			// find the site
			Site site = siteService().getSite(siteId);

			// find the section
			Group sectionGroup = null;
			Collection<Group> siteGroups = site.getGroups();
			for (Group g : siteGroups)
			{
				if (g.getTitle().equalsIgnoreCase(section) && (g.getProperties().getProperty("group_prop_wsetup_created") == null))
				{
					sectionGroup = g;
					break;
				}
			}

			// remove the section
			if (sectionGroup != null)
			{
				site.removeGroup(sectionGroup);

				// remove the site's provided student members
				Set<Member> members = new HashSet<Member>();
				members.addAll((Set<Member>) site.getMembers());
				for (Member m : members)
				{
					if (m.isProvided() && (m.getRole().getId().equals(ROLE_STUDENT)))
					{
						site.removeMember(m.getUserId());
					}
				}

				// consolidate the site's membership
				consolidate(site);

				// save
				try
				{
					siteService().save(site);
					rv = true;
				}
				catch (IdUnusedException e)
				{
					M_log.warn("processRosterBuffer: " + e);
				}
				catch (PermissionException e)
				{
					M_log.warn("processRosterBuffer: " + e);
				}
			}
		}
		catch (IdUnusedException e)
		{
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeSectionFromSites(String authenticatedUserId, String section, String clientPrefix, String termSuffix)
	{
		// admin only
		if (!allowRosterAccess(authenticatedUserId)) return false;

		boolean rv = false;

		// get the sites
		List<Site> sites = findSectionSites(clientPrefix, termSuffix, section);

		// remove the section from each of these sites
		for (Site site : sites)
		{
			boolean result = removeSectionFromSite(authenticatedUserId, section, site.getId());
			if (result) rv = true;
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public boolean removeSectionsFromSite(String authenticatedUserId, String siteId)
	{
		// admin only
		if (!allowRosterAccess(authenticatedUserId)) return false;

		boolean rv = false;
		try
		{
			// find the site
			Site site = siteService().getSite(siteId);

			// find all sections
			Collection<Group> siteGroups = new ArrayList<Group>(site.getGroups());
			for (Group g : siteGroups)
			{
				if (g.getProperties().getProperty("group_prop_wsetup_created") == null)
				{
					site.removeGroup(g);
				}
			}

			// remove the site's provided student members
			Set<Member> members = new HashSet<Member>();
			members.addAll((Set<Member>) site.getMembers());
			for (Member m : members)
			{
				if (m.isProvided() && (m.getRole().getId().equals(ROLE_STUDENT)))
				{
					site.removeMember(m.getUserId());
				}
			}

			// consolidate the site's membership
			consolidate(site);

			// save
			try
			{
				siteService().save(site);
				rv = true;
			}
			catch (IdUnusedException e)
			{
				M_log.warn("processRosterBuffer: " + e);
			}
			catch (PermissionException e)
			{
				M_log.warn("processRosterBuffer: " + e);
			}
		}
		catch (IdUnusedException e)
		{
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeUserFromSite(String authenticatedUserId, String userId, String siteId)
	{
		// admin only
		if (!allowRosterAccess(authenticatedUserId)) return;

		try
		{
			// find the site
			Site site = siteService().getSite(siteId);

			// does the user exist in the site? Must be non-student, provided
			Member m = site.getMember(userId);
			if ((m != null) && (!ROLE_STUDENT.equals(m.getRole().getId())) && (!ROLE_BLOCKED.equals(m.getRole().getId())) && (m.isProvided()))
			{
				// remove from the site membership
				site.removeMember(userId);

				// save
				try
				{
					siteService().save(site);
				}
				catch (IdUnusedException e)
				{
					M_log.warn("processRosterBuffer: " + e);
				}
				catch (PermissionException e)
				{
					M_log.warn("processRosterBuffer: " + e);
				}
			}
		}
		catch (IdUnusedException e)
		{
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<SiteEnrollment> reportSeatsPerSite(String clientPrefix, String term)
	{
		List<SiteEnrollment> rv = new ArrayList<SiteEnrollment>();

		// get the sites for the client in the term
		List<Site> sites = findSitesByClientTerm(clientPrefix, term);

		for (Site s : sites)
		{
			// count roster added students (seats), roster added non-students, non-roster added members (guests)
			int numSeats = 0;
			int numOthers = 0;
			int numGuests = 0;

			for (Member m : (Set<Member>) s.getMembers())
			{
				if (m.isProvided())
				{
					if (ROLE_STUDENT.equals(m.getRole().getId()) || ROLE_BLOCKED.equals(m.getRole().getId()))
					{
						numSeats++;
					}
					else
					{
						numOthers++;
					}
				}
				else
				{
					numGuests++;
				}
			}

			SiteEnrollmentImpl se = new SiteEnrollmentImpl(clientPrefix, term, numGuests, numOthers, numSeats, s.getId(), s.getTitle());
			rv.add(se);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<TermEnrollment> reportSeatsPerTerm(String clientPrefix, String[] terms)
	{
		List<TermEnrollment> rv = new ArrayList<TermEnrollment>();

		// for all the terms
		for (String termSuffix : terms)
		{
			// get the sites for the client in the term
			List<Site> sites = findSitesByClientTerm(clientPrefix, termSuffix);

			// count sites, sections, roster added students (seats), roster added non-students, non-roster added members (guests)
			int numSites = 0;
			int numSections = 0;
			int numSeats = 0;
			int numOthers = 0;
			int numGuests = 0;

			Set<String> uniqueSectionTitles = new HashSet<String>();
			for (Site s : sites)
			{
				// if a site is marked specially to not be counted in seats, skip it. TODO:
				numSites++;

				for (Group g : (Collection<Group>) s.getGroups())
				{
					if (g.getProperties().getProperty("group_prop_wsetup_created") == null)
					{
						uniqueSectionTitles.add(g.getTitle());
					}
				}

				for (Member m : (Set<Member>) s.getMembers())
				{
					if (m.isProvided())
					{
						if (ROLE_STUDENT.equals(m.getRole().getId()) || ROLE_BLOCKED.equals(m.getRole().getId()))
						{
							numSeats++;
						}
						else
						{
							numOthers++;
						}
					}
					else
					{
						numGuests++;
					}
				}
			}

			numSections = uniqueSectionTitles.size();

			TermEnrollmentImpl te = new TermEnrollmentImpl(clientPrefix, termSuffix, numGuests, numOthers, numSeats, numSections, numSites);
			rv.add(te);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SiteVisits> reportVisitsPerSite(String clientPrefix, String termSuffix)
	{
		final List<SiteVisits> rv = new ArrayList<SiteVisits>();

		String sql = "SELECT S.TITLE, SUM(VISITS), COUNT(1), S.SITE_ID FROM AM_SITE_VISIT V                                                        "
				+ "RIGHT OUTER JOIN SAKAI_SITE S ON V.CONTEXT=S.SITE_ID AND V.USER_ID != 'admin'                                                   "
				// join archives_site_term T on S.site_id=T.site_id
				// where T.term_id = 40
				+ "WHERE S.TITLE LIKE ?                                                                                                            "
				+ "GROUP BY S.SITE_ID ORDER BY S.TITLE ASC                                                                                         ";
		Object[] fields = new Object[1];
		fields[0] = clientPrefix + " % " + termSuffix;
		sqlService().dbRead(sql, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					String title = sqlService().readString(result, 1);
					Integer visits = sqlService().readInteger(result, 2);
					Integer visitors = sqlService().readInteger(result, 3);
					String id = sqlService().readString(result, 4);
					// for no visits, visitors will be 1, but should be reported as 0
					SiteVisitsImpl sv = new SiteVisitsImpl(id, title, visits == null ? 0 : visits, visits == null ? 0 : (visitors == null ? 0
							: visitors));
					rv.add(sv);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("reportVisitsPerSite: " + e);
					return null;
				}
			}
		});

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public void syncWithRosterFiles(String authenticatedUserId)
	{
		// admin only
		if (!allowRosterAccess(authenticatedUserId)) return;

		syncWithRosterFiles(authenticatedUserId, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void syncWithRosterText(final String authenticatedUserId, final String text, String institutionCodeParam)
	{
		// admin only
		if (!allowRosterAccess(authenticatedUserId)) return;

		// massage institutionCode
		final String institutionCode = StringUtil.trimToNull(institutionCodeParam);

		// run on the task thread
		synchronized (taskSync)
		{
			if (this.task != null)
			{
				M_log.warn("syncWithRosterText: blocked by running task");
				return;
			}
			this.task = new Thread(new Runnable()
			{
				public void run()
				{
					BufferedReader br = null;
					try
					{
						// set the user into the thread
						Session s = sessionManager().getCurrentSession();
						if (s != null)
						{
							s.setUserId(authenticatedUserId);
						}

						pushAdvisor();

						message("processing roster text");
						br = new BufferedReader(new StringReader(text));
						processRosterBuffer(br, institutionCode, PASTED);
						message("processing roster text complete");

						synchronized (taskSync)
						{
							task = null;
						}
					}
					finally
					{
						if (br != null)
						{
							try
							{
								br.close();
							}
							catch (IOException e)
							{
								M_log.warn("syncWithRosterText(): on close of reader " + e);
							}
						}

						popAdvisor();

						if (task != null)
						{
							task = null;
						}

						threadLocalManager().clear();
					}
				}
			}, getClass().getName());
		}

		this.task.start();
	}

	/**
	 * {@inheritDoc}
	 */
	public String termCode(String suffix)
	{
		String rv = suffix;
		suffix = suffix.toUpperCase();

		Integer termId = this.getTermIdFromDB(suffix);
		if (termId != null) rv = fmtThreeDigit(termId);

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String termName(String suffix)
	{
		String rv = suffix;
		suffix = suffix.toUpperCase();

		if ("DEV".equals(suffix))
		{
			rv = "Development";
		}
		else if (StringUtil.trimToNull(suffix) == null)
		{
			rv = "Project";
		}
		else if (suffix.startsWith("W"))
		{
			rv = "Winter 20" + suffix.substring(1, 3);
		}
		else if (suffix.startsWith("SP"))
		{
			rv = "Spring 20" + suffix.substring(2, 4);
		}
		else if (suffix.startsWith("SU"))
		{
			rv = "Summer 20" + suffix.substring(2, 4);
		}
		else if (suffix.startsWith("F"))
		{
			rv = "Fall 20" + suffix.substring(1, 3);
		}

		return rv;
	}

	/**
	 * Make sure a site with this title exists, and has a section with this id.
	 * 
	 * @param title
	 *        The exact site title.
	 * @param section
	 *        The section id.
	 * @param workingSites
	 *        cached and possibly modified sites we have processed so far.
	 * @param noSectionDirective
	 *        if true, we create a site with no section.
	 * @return The site.
	 */
	@SuppressWarnings("unchecked")
	protected Site assureSiteSection(String title, String section, Map<String, Site> workingSites, Map<String, Site> modifiedSites,
			boolean noSectionsDirective)
	{
		Site site = getSite(title, workingSites);
		if (site == null)
		{
			site = newSite(title, workingSites);
			modifiedSites.put(site.getId(), site);
		}

		// the rest of this method assures the section - skip it all if we are under the noSectionsDirective
		if (noSectionsDirective) return site;

		// see if the site has the section
		Collection<Group> groups = site.getGroups();
		boolean found = false;
		for (Group g : groups)
		{
			if (g.getTitle().equalsIgnoreCase(section) && (g.getProperties().getProperty("group_prop_wsetup_created") == null))
			{
				found = true;
				break;
			}
		}

		if (!found)
		{
			// add a group for the section
			Group g = site.addGroup();
			g.setTitle(section);
			g.getPropertiesEdit().addProperty("sections_category", "Lecture");

			modifiedSites.put(site.getId(), site);
		}

		return site;
	}

	/**
	 * Complete the site modifications based on roster processing.
	 * 
	 * @param site
	 *        The site.
	 */
	@SuppressWarnings("unchecked")
	protected void consolidate(Site site)
	{
		// get a list of all users mentioned in the site's section groups
		Set<String> sectionUserIds = new HashSet<String>();
		for (Group g : (Collection<Group>) site.getGroups())
		{
			if (g.getProperties().getProperty("group_prop_wsetup_created") == null)
			{
				for (Member gm : (Collection<Member>) g.getMembers())
				{
					sectionUserIds.add(gm.getUserId());
				}
			}
		}

		// for each of these, compute the role and status for the site level based on section membership
		for (String userId : sectionUserIds)
		{
			// compute the best active and role for the user from the section data
			boolean active = false;
			String role = null;
			for (Group g : (Collection<Group>) site.getGroups())
			{
				if (g.getProperties().getProperty("group_prop_wsetup_created") == null)
				{
					Member gm = g.getMember(userId);
					if (gm != null)
					{
						active = consolidateActive(active, gm.isActive());
						role = consolidateRole(role, gm.getRole().getId());
					}
				}
			}

			// update the site membership
			Member m = site.getMember(userId);
			if (m == null)
			{
				site.addMember(userId, role, active, true);
			}

			else
			{
				// instructor, ta or evaluator roles at the site level don't get changed
				if (ROLE_INSTRUCTOR.equals(m.getRole().getId()) || ROLE_TA.equals(m.getRole().getId()) || ROLE_EVALUATOR.equals(m.getRole().getId()))
				{
					continue;
				}

				// blocked roles at the site level hold, unless we need to change them to a proper drop
				if (ROLE_BLOCKED.equals(m.getRole().getId()) && ROLE_STUDENT.equals(role) && active)
				{
					continue;
				}

				// if we have different computed information from the sections than what is at the site level, update it at the site level
				if ((!role.equals(m.getRole().getId())) || (m.isActive() != active) || (!m.isProvided()))
				{
					site.removeMember(userId);
					site.addMember(userId, role, active, true);
				}
			}
		}

		// finally, remove from all sections any site level members that are instructors, TAs, evaluators or guests, leaving only the students (and blocked) in the sections
		Set<String> siteLevelUserIds = new HashSet<String>();
		for (Member sm : (Set<Member>) site.getMembers())
		{
			if (!(ROLE_STUDENT.equals(sm.getRole().getId()) || ROLE_BLOCKED.equals(sm.getRole().getId())))
			{
				siteLevelUserIds.add(sm.getUserId());
			}
		}
		for (Group g : (Collection<Group>) site.getGroups())
		{
			if (g.getProperties().getProperty("group_prop_wsetup_created") == null)
			{
				for (Member gm : new HashSet<Member>(g.getMembers()))
				{
					if (siteLevelUserIds.contains(gm.getUserId()))
					{
						g.removeMember(gm.getUserId());
					}
				}
			}
		}
	}

	/**
	 * Consolidate a series of active flags - if any are true, end up true.
	 * 
	 * @param curValue
	 *        The current value.
	 * @param newValue
	 *        The new value.
	 * @return The next current value.
	 */
	protected boolean consolidateActive(boolean curValue, boolean newValue)
	{
		// once we see an active, keep it
		if (curValue) return true;
		return newValue;
	}

	/**
	 * Consolidate a series of role names. Keep the highest.
	 * 
	 * @param curValue
	 *        The current value.
	 * @param newValue
	 *        The new value.
	 * @return The next current value.
	 */
	protected String consolidateRole(String curValue, String newValue)
	{
		if (ROLE_INSTRUCTOR.equals(curValue)) return curValue;
		if (ROLE_TA.equals(curValue))
		{
			if (ROLE_INSTRUCTOR.equals(newValue))
				return newValue;
			else
				return curValue;
		}
		if (ROLE_EVALUATOR.equals(curValue))
		{
			if (ROLE_INSTRUCTOR.equals(newValue) || ROLE_TA.equals(newValue))
				return newValue;
			else
				return curValue;
		}
		if (ROLE_STUDENT.equals(curValue))
		{
			if (ROLE_INSTRUCTOR.equals(newValue) || ROLE_TA.equals(newValue) || ROLE_EVALUATOR.equals(newValue))
				return newValue;
			else
				return curValue;
		}
		if (ROLE_GUEST.equals(curValue))
		{
			if (ROLE_INSTRUCTOR.equals(newValue) || ROLE_TA.equals(newValue) || ROLE_EVALUATOR.equals(newValue) || ROLE_STUDENT.equals(newValue))
				return newValue;
			else
				return curValue;
		}
		return newValue;
	}

	/**
	 * Creates course site with default tools.
	 */
	protected Site createCourseSiteNoTemplate()
	{
		Site siteEdit = null;
		try
		{
			String newSiteId = idManager().createUuid();
			siteEdit = siteService().addSite(newSiteId, "course");

			siteEdit.setDescription(null);
			siteEdit.setShortDescription(null);
			siteEdit.setIconUrl("");
			siteEdit.setInfoUrl(null);
			siteEdit.setJoinable(false);
			siteEdit.setPublished(true);
			siteEdit.setPubView(true);
			siteEdit.setSkin("");
			siteEdit.setType("course");

			SitePage page = null;
			ToolConfiguration tool = null;

			// Home
			page = siteEdit.addPage();
			page.setTitle("Home");
			page.setLayout(SitePage.LAYOUT_SINGLE_COL);

			tool = page.addTool();
			tool.setTool("e3.homepage", toolManager().getTool("e3.homepage"));
			tool.setTitle("Home");
			tool.setLayoutHints("0,0");

			// CourseMap
			page = siteEdit.addPage();
			page.setTitle("CourseMap");
			page.setLayout(SitePage.LAYOUT_SINGLE_COL);

			tool = page.addTool();
			tool.setTool("sakai.coursemap", toolManager().getTool("sakai.coursemap"));
			tool.setTitle("CourseMap");
			tool.setLayoutHints("0,0");

			// Syllabus
			page = siteEdit.addPage();
			page.setTitle("Syllabus");
			page.setLayout(SitePage.LAYOUT_SINGLE_COL);

			tool = page.addTool();
			tool.setTool("sakai.syllabus", toolManager().getTool("sakai.syllabus"));
			tool.setTitle("Syllabus");
			tool.setLayoutHints("0,0");

			// Announcements
			page = siteEdit.addPage();
			page.setTitle("Announcements");
			page.setLayout(SitePage.LAYOUT_SINGLE_COL);

			tool = page.addTool();
			tool.setTool("sakai.announcements", toolManager().getTool("sakai.announcements"));
			tool.setTitle("Announcements");
			tool.setLayoutHints("0,0");

			// Modules
			page = siteEdit.addPage();
			page.setTitle("Modules");
			page.setLayout(SitePage.LAYOUT_SINGLE_COL);

			tool = page.addTool();
			tool.setTool("sakai.melete", toolManager().getTool("sakai.melete"));
			tool.setTitle("Modules");
			tool.setLayoutHints("0,0");

			// Mneme ( Assignments, Tests and Surveys)
			Tool mnemeTool = toolManager().getTool("sakai.mneme");
			String mnemeToolTitle = null;
			if (mnemeTool != null) mnemeToolTitle = mnemeTool.getTitle();

			page = siteEdit.addPage();
			if (mnemeToolTitle != null)
				page.setTitle(mnemeToolTitle);
			else
				page.setTitle("Assignments, Tests and Surveys");
			page.setLayout(SitePage.LAYOUT_SINGLE_COL);

			tool = page.addTool();
			tool.setTool("sakai.mneme", toolManager().getTool("sakai.mneme"));
			if (mnemeToolTitle != null)
				page.setTitle(mnemeToolTitle);
			else
				page.setTitle("Assignments, Tests and Surveys");
			tool.setLayoutHints("0,0");

			// Discussion and Private Messages
			page = siteEdit.addPage();
			page.setTitle("Discussion and Private Messages");
			page.setLayout(SitePage.LAYOUT_SINGLE_COL);

			tool = page.addTool();
			tool.setTool("sakai.jforum.tool", toolManager().getTool("sakai.jforum.tool"));
			tool.setTitle("Discussion and Private Messages");
			tool.setLayoutHints("0,0");

			// Chat
			page = siteEdit.addPage();
			page.setTitle("Chat");
			page.setLayout(SitePage.LAYOUT_SINGLE_COL);

			tool = page.addTool();
			tool.setTool("sakai.chat", toolManager().getTool("sakai.chat"));
			tool.setTitle("Chat");
			tool.setLayoutHints("0,0");

			// Gradebook
			page = siteEdit.addPage();
			page.setTitle("Gradebook");
			page.setLayout(SitePage.LAYOUT_SINGLE_COL);

			tool = page.addTool();
			tool.setTool("sakai.gradebook.tool", toolManager().getTool("e3.gradebook"));
			tool.setTitle("Gradebook");
			tool.setLayoutHints("0,0");

			// ActivityMeter
			page = siteEdit.addPage();
			page.setTitle("Activity Meter");
			page.setLayout(SitePage.LAYOUT_SINGLE_COL);

			tool = page.addTool();
			tool.setTool("sakai.activitymeter", toolManager().getTool("sakai.activitymeter"));
			tool.setTitle("Activity Meter");
			tool.setLayoutHints("0,0");

			// Site Setup
			page = siteEdit.addPage();
			page.setTitle("Site Setup");
			page.setLayout(SitePage.LAYOUT_SINGLE_COL);

			tool = page.addTool();
			tool.setTool("e3.configure", toolManager().getTool("e3.configure"));
			tool.setTitle("Site Setup");
			tool.setLayoutHints("0,0");

			// Roster
			page = siteEdit.addPage();
			page.setTitle("Roster");
			page.setLayout(SitePage.LAYOUT_SINGLE_COL);

			tool = page.addTool();
			tool.setTool("e3.siteroster", toolManager().getTool("e3.siteroster"));
			tool.setTitle("Roster");
			tool.setLayoutHints("0,0");

		}
		catch (IdInvalidException e)
		{
			M_log.warn("createCourseSiteNoTemplate: " + e);
		}
		catch (IdUsedException e)
		{
			M_log.warn("createCourseSiteNoTemplate: " + e);
		}
		catch (PermissionException e)
		{
			M_log.warn("createCourseSiteNoTemplate: " + e);
		}

		return siteEdit;
	}

	/**
	 * Create a user with the data in the roster line.
	 * 
	 * @param rl
	 *        The roster line.
	 * @param institutionCode
	 *        The institution code.
	 * @return The user created, or null if we were unable to create a user.
	 */
	protected User createUser(RosterLine rl, String institutionCode)
	{
		User rv = null;
		try
		{
			String userId = idManager().createUuid();
			rv = userDirectoryService().addUser(userId, rl.eid, rl.firstName, rl.lastName, rl.email, rl.pw, rl.userType, null);
			userDirectoryService().setIid(userId, institutionCode, rl.iid);
		}
		catch (Exception e)
		{
		}

		return rv;
	}

	/**
	 * Email the user that we just changed their EID.
	 * 
	 * @param rl
	 *        The roster line.
	 * @param user
	 *        The user.
	 */
	protected void emailEidChange(RosterLine rl, String oldEid, User user)
	{
		String from = "\"" + serverConfigurationService().getString("ui.service", "Etudes") + "\"<no-reply@"
				+ serverConfigurationService().getServerName() + ">";
		String subject = "New Etudes User Id";

		StringBuilder content = new StringBuilder();

		content.append("Dear " + user.getDisplayName() + ":\n\n"
				+ "Your college has changed your Etudes login. This change is effective immediately.\n\n" + "Old user id: " + oldEid + "\n"
				+ "New user id: " + rl.eid + "\n\n" + "The password remains whatever you had before.\n\n"
				+ "If this was done in error, please contact Admissions & Records or your Distance Learning office.\n\n----------------------\n\n"
				+ "This is an automatic notification.  Do not reply to this email.");

		// send to the user's email from record, as well as from the roster line (if we have two different email addresses)
		String email = StringUtil.trimToNull(user.getEmail());
		if (email != null)
		{
			emailService().send(from, email, subject, content.toString(), email, null, null);
		}
		if ((rl.email != null) && Different.different(email, rl.email))
		{
			emailService().send(from, rl.email, subject, content.toString(), rl.email, null, null);
		}
	}

	/**
	 * Report eid changes from roster processing.
	 * 
	 * @param client
	 *        The client (site prefix).
	 * @param term
	 *        The term (site suffix).
	 * @param sites
	 *        The sites with no students loaded by roster.
	 */
	protected void emailNoRoster(String client, String term, List<Site> sites)
	{
		String to = "roster-admin@etudes.org";
		String from = "\"" + serverConfigurationService().getString("ui.service", "Sakai") + "\"<no-reply@"
				+ serverConfigurationService().getServerName() + ">";
		String subject = "Roster Warnings: Sites Without Students: " + client + " - " + term;

		StringBuilder content = new StringBuilder();
		content.append("Roster processing has noticed these sites which have no roster loaded students, for client: " + client + " in term: " + term
				+ ".\n\n");

		for (Site s : sites)
		{
			content.append(s.getTitle() + "\n");
		}

		emailService().send(from, to, subject, content.toString(), to, null, null);
	}

	/**
	 * Report bad lines, missing sections, and EID changes in roster processing.
	 * 
	 * @param badLines
	 *        The roster lines.
	 * @param missingSections
	 *        The missing sections.
	 * @param eidChagnes
	 *        The EID changes.
	 * @param source
	 *        describes the source of the text line (file name or pasted).
	 */
	protected void emailRosterReport(List<String> badLines, Set<String> missingSections, List<String> eidChanges, String source)
	{
		String to = "roster-admin@etudes.org";
		String from = "\"" + serverConfigurationService().getString("ui.service", "Sakai") + "\"<no-reply@"
				+ serverConfigurationService().getServerName() + ">";
		String subject = "Roster Issues: " + source;

		StringBuilder content = new StringBuilder();

		// missing sections
		if (!missingSections.isEmpty())
		{
			content.append("Etudes has discovered sections in your student file which do not exist in any of your sites (" + source + "):\n\n");

			for (String section : missingSections)
			{
				content.append(section + "\n");
			}

			content.append("\n\n");
		}

		// EID changes
		if (!eidChanges.isEmpty())
		{
			content.append("Roster processing has changed user EIDs due to new data found in " + source + ".  These users have been emailed.\n\n");

			for (String line : eidChanges)
			{
				content.append(line + "\n");
			}

			content.append("\n\n");
		}

		// bad lines
		if (!badLines.isEmpty())
		{
			content.append("Roster processing has rejected invalid roster lines from " + source + ":\n\n");

			for (String line : badLines)
			{
				content.append(line + "\n");
			}
		}

		emailService().send(from, to, subject, content.toString(), to, null, null);
	}

	/**
	 * Figure the institution code for this file.
	 * 
	 * @param file
	 *        The file with the roster text.
	 * @return The institution code that this file represents.
	 */
	protected String findInstitutionCode(File file)
	{
		String code = this.instCodeFileMap.get(file.getName());
		return code;
	}

	/**
	 * Find the sites that have this section as a group, limited to the title's prefix and suffix.
	 * 
	 * @param title
	 *        A site title
	 * @param section
	 *        The section title
	 * @param workingSites
	 * @return A list of sites found.
	 */
	@SuppressWarnings("unchecked")
	protected List<Site> findSites(String title, String section, Map<String, Site> workingSites)
	{
		// parse the title for prefix (institution site title prefix) and suffix (term id)
		String[] parts = StringUtil.split(title, " ");
		String prefix = parts[0].toLowerCase();
		String suffix = parts[parts.length - 1].toLowerCase();

		// find the sites that have groups with section as the title
		List<Site> sites = getSitesWithGroup(section, workingSites);

		// filter out any sites that don't match the prefix and suffix
		List<Site> candidates = new ArrayList<Site>();
		for (Site s : sites)
		{
			String[] siteParts = StringUtil.split(s.getTitle(), " ");
			String sitePrefix = siteParts[0].toLowerCase();
			String siteSuffix = siteParts[siteParts.length - 1].toLowerCase();
			if (sitePrefix.equals(prefix) && siteSuffix.equals(suffix))
			{
				// skip non-section groups
				boolean found = false;
				Collection<Group> siteGroups = s.getGroups();
				for (Group g : siteGroups)
				{
					if (g.getTitle().equalsIgnoreCase(section) && (g.getProperties().getProperty("group_prop_wsetup_created") == null))
					{
						found = true;
						break;
					}
				}
				if (found) candidates.add(s);
			}
		}

		return candidates;
	}

	/**
	 * Find an existing user by IID
	 * 
	 * @param institutionCode
	 *        The institution code.
	 * @param iid
	 *        The iid.
	 * @return The user found, or null if not found.
	 */
	protected User findUser(String institutionCode, String iid)
	{
		// find the user by the IID
		User rv = null;
		try
		{
			rv = userDirectoryService().getUserByIid(institutionCode, iid);
		}
		catch (UserNotDefinedException e)
		{
		}

		return rv;
	}

	/**
	 * Format the Integer to at least three digits.
	 * 
	 * @param value
	 *        The integer value.
	 * @return The integer value formatted as a string of at least three digits.
	 */
	protected String fmtThreeDigit(Integer value)
	{
		if (value.intValue() < 100)
		{
			if (value.intValue() < 10)
				return "00" + value.toString();
			else
				return "0" + value.toString();
		}
		return value.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getConfigKeyRoot()
	{
		return "etudes.roster";
	}

	protected Site getSite(String title, Map<String, Site> workingSites)
	{
		Site rv = workingSites.get(title);
		if (rv == null)
		{
			rv = getSiteFromDB(title);
			if (rv != null)
			{
				workingSites.put(rv.getTitle(), rv);
			}
		}

		return rv;
	}

	protected Site getSiteById(String siteId, Map<String, Site> workingSites)
	{
		// scan the working sites (the key is title, but we have id
		for (Site s : workingSites.values())
		{
			if (s.getId().equals(siteId)) return s;
		}

		try
		{
			Site s = siteService().getSite(siteId);
			workingSites.put(s.getTitle(), s);
			return s;
		}
		catch (IdUnusedException e)
		{
		}

		return null;
	}

	/**
	 * get site from database
	 */
	@SuppressWarnings("unchecked")
	protected Site getSiteFromDB(String title)
	{
		// Note: mysql string compares are not case sensitive - but the use of "upper" causes this query to scan all records instead of using a title based key -ggolden
		// String sql = "SELECT S.SITE_ID FROM SAKAI_SITE S WHERE UPPER(S.TITLE) = UPPER(?)";
		String sql = "SELECT S.SITE_ID FROM SAKAI_SITE S WHERE S.TITLE = ?";
		Object[] fields = new Object[1];
		fields[0] = title;
		List<String> results = sqlService().dbRead(sql, fields, null);
		if (results.size() > 0)
		{
			String siteId = results.get(0);
			try
			{
				Site site = siteService().getSite(siteId);
				return site;
			}
			catch (IdUnusedException e)
			{
			}
		}

		return null;
	}

	/**
	 * get sites from database based on group titles they contain
	 */
	@SuppressWarnings("unchecked")
	protected List<Site> getSitesWithGroup(String title, Map<String, Site> workingSites)
	{
		String sql = "SELECT DISTINCT SITE_ID FROM SAKAI_SITE_GROUP WHERE TITLE = ?";
		Object[] fields = new Object[1];
		fields[0] = title;
		Set<String> results = new HashSet<String>();
		results.addAll(sqlService().dbRead(sql, fields, null));

		// also consider workingSites
		for (Site s : workingSites.values())
		{
			boolean found = false;
			for (Group g : (Collection<Group>) s.getGroups())
			{
				if (g.getTitle().equalsIgnoreCase(title) && (g.getProperties().getProperty("group_prop_wsetup_created") == null))
				{
					found = true;
					break;
				}
			}

			if (found)
			{
				results.add(s.getId());
			}
		}

		List<Site> rv = new ArrayList<Site>();
		for (String siteId : results)
		{
			Site site = getSiteById(siteId, workingSites);
			if (site != null) rv.add(site);
		}

		return rv;
	}

	/**
	 * Get a term's numeric id from the term (site title suffix)
	 * 
	 * @param term
	 *        the term code
	 * @return The term's numeric id, or null if not found.
	 */
	protected Integer getTermIdFromDB(String term)
	{
		String sql = "SELECT T.ID FROM ARCHIVES_TERM T WHERE T.TERM = ?";
		Object[] fields = new Object[1];
		fields[0] = term;
		final List<Integer> rv = new ArrayList<Integer>();
		sqlService().dbRead(sql, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					Integer value = sqlService().readInteger(result, 1);
					rv.add(value);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("getTermIdFromDB: " + e);
					return null;
				}
			}
		});

		if (rv.isEmpty()) return null;
		return rv.get(0);
	}

	/**
	 * Create a new course site with client template or default tools.
	 * 
	 * @param title
	 *        The site title
	 * @param workingSites
	 */
	protected Site newSite(String title, Map<String, Site> workingSites)
	{
		Site rv = null;

		// for the site template title, use "<site prefix> TEMPLATE DEV"
		String templateTitle = "";
		String[] parts = StringUtil.splitFirst(title, " ");
		if (parts != null && parts.length > 0)
		{
			templateTitle += parts[0].toUpperCase();
		}
		templateTitle += " TEMPLATE DEV";

		// find a site with this title
		Site template = getSite(templateTitle, workingSites);

		// if we find the template, use it to create the new site
		if (template != null)
		{
			try
			{
				String newSiteId = idManager().createUuid();
				rv = siteService().addSite(newSiteId, template);
			}
			catch (IdInvalidException e)
			{
				M_log.warn("createCourseSite: " + e);
			}
			catch (IdUsedException e)
			{
				M_log.warn("createCourseSite: " + e);
			}
			catch (PermissionException e)
			{
				M_log.warn("createCourseSite: " + e);
			}
		}

		// if we don't find a template
		else
		{
			// no template, create from scratch
			rv = createCourseSiteNoTemplate();
		}

		rv.setTitle(title);

		// remove admin member
		rv.removeMember("admin");

		workingSites.put(rv.getTitle(), rv);
		return rv;
	}

	/**
	 * Parse and validate syntax for a roster lime.
	 * 
	 * @param line
	 *        The roster line.
	 * @return The parsed line, or null if there is an error.
	 */
	protected RosterLine parseLine(String line)
	{
		// tabs are the separator, parse the line.
		String[] parts = StringUtil.split(line, "\t");

		// we need 10 parts
		if (parts.length != 10) return null;

		RosterLine rv = new RosterLine();
		rv.line = line;

		// site title (collapsing multiple consecutive white space to a space)
		rv.siteTitle = parts[0].trim().replaceAll("\\s+", " ");
		if (rv.siteTitle.length() == 0) return null;

		// eid
		rv.eid = parts[1].trim();
		if (rv.eid.length() == 0) return null;

		// pw
		rv.pw = parts[2].trim();
		if (rv.pw.length() == 0) return null;

		// last name
		rv.lastName = parts[3].trim();
		if (rv.lastName.length() == 0) return null;

		// first name
		rv.firstName = parts[4].trim();
		if (rv.firstName.length() == 0) return null;

		// email
		rv.email = StringUtil.trimToNull(parts[5]);

		// role (and a user type, in case we need to add the user)
		rv.role = parts[6].trim();
		if (rv.role.equalsIgnoreCase(TYPE_STUDENT))
		{
			rv.role = ROLE_STUDENT;
			rv.userType = TYPE_STUDENT;
		}
		else if (rv.role.equalsIgnoreCase(TYPE_INSTRUCTOR))
		{
			rv.role = ROLE_INSTRUCTOR;
			rv.userType = TYPE_INSTRUCTOR;
		}
		else if (rv.role.equalsIgnoreCase(TYPE_TA))
		{
			rv.role = ROLE_TA;
			rv.userType = TYPE_INSTRUCTOR;
		}
		else if (rv.role.equalsIgnoreCase(TYPE_EVALUATOR))
		{
			rv.role = ROLE_EVALUATOR;
			rv.userType = TYPE_INSTRUCTOR;
		}
		else
			return null;

		// status
		rv.status = parts[7].trim();
		if (rv.status.equalsIgnoreCase("E"))
			rv.status = "E";
		else if (rv.status.equalsIgnoreCase("D"))
			rv.status = "D";
		else
			return null;

		// section
		rv.section = parts[8].trim();
		if (rv.section.length() == 0) return null;

		// iid
		rv.iid = parts[9].trim();
		if (rv.iid.length() == 0) return null;

		return rv;
	}

	/**
	 * Remove our security advisor.
	 */
	protected void popAdvisor()
	{
		securityService().popAdvisor();
	}

	/**
	 * Process each file with a ".new" extension in the configured directory
	 */
	protected void processFiles()
	{
		if (this.dirPath == null) return;

		// get the directory
		File dir = new File(this.dirPath);
		if ((dir == null) || (!dir.isDirectory()))
		{
			M_log.warn("processFiles: configured directory missing or not directory: " + this.dirPath);
			return;
		}

		// get the file list
		File files[] = dir.listFiles();
		if (files == null)
		{
			// nothing to do
			return;
		}

		// process each file found
		for (File file : files)
		{
			// skip "." files
			if (file.getName().startsWith(".")) continue;

			message("processing roster file: " + file.getName());
			processRosterFile(file);

			// delete the file
			file.delete();
		}
	}

	/**
	 * Process the roster lines from a pasted text or a roster file in the reader.
	 * 
	 * @param br
	 *        The reader containing the roster text.
	 * @param institutionCode
	 *        The given or computed (from the file name) institution code for these roster lines.
	 * @param source
	 *        either the file name or "pasted text" indicating the source of the roster lines.
	 */
	@SuppressWarnings("unchecked")
	protected void processRosterBuffer(BufferedReader br, String bufferInstitutionCode, String source)
	{
		// save up sites and their modifications to save when we are done
		Map<String, Site> workingSites = new HashMap<String, Site>();
		Map<String, Site> modifiedSites = new HashMap<String, Site>();

		// lines that were invalid
		List<String> badLines = new ArrayList<String>();
		Set<String> missingSections = new HashSet<String>();
		List<String> eidChanges = new ArrayList<String>();

		// some special directives
		boolean checkForDirectives = true;
		boolean createSitesDirective = false;
		boolean ignoreDropsDirective = false;
		boolean createInstructorsDirective = false;
		boolean noSectionsDirective = false;
		boolean addToUsersGroupDirective = false;

		// accept only student role lines from files that have a known institution code - which indicate the file is registered as a daily client roster file.
		boolean onlyStudentLines = ((!source.equals(PASTED)) && (bufferInstitutionCode != null));

		String oneSeenTitle = null;

		// get the UG site
		Site usersGroup = null;
		try
		{
			usersGroup = siteService().getSiteByTitle("Users Group");
		}
		catch (IdUnusedException e)
		{
			M_log.warn("processRosterBuffer: cannot find users group");
		}

		// process each line - don't let any line's errors interfere with subsequent or past lines
		while (true)
		{
			try
			{
				// read lines till done
				String line = br.readLine();
				if (line == null) break;

				line = line.trim();

				// special directive recognition
				if (checkForDirectives)
				{
					if (source.equals(PASTED))
					{
						// recognize special directive
						if (line.equals("#@CREATESITES@"))
						{
							createSitesDirective = true;
						}
						else if (line.equals("#@IGNOREDROPS@"))
						{
							ignoreDropsDirective = true;
						}
						else if (line.equals("#@CREATEINSTRUCTORS@"))
						{
							createInstructorsDirective = true;
						}
						else if (line.equals("#@NOSECTIONS@"))
						{
							noSectionsDirective = true;
						}
						else if (line.equals("#@ADDTOUSERSGROUP@"))
						{
							addToUsersGroupDirective = true;
						}
					}
				}

				// skip blank links
				if (line.length() == 0) continue;

				// skip comment lines
				if (line.startsWith("#")) continue;

				// all directives must be before any non-comment line
				checkForDirectives = false;

				// parse the line
				RosterLine rl = parseLine(line);
				if (rl == null)
				{
					badLines.add("# parse error\n" + line);
					continue;
				}

				if (oneSeenTitle == null) oneSeenTitle = rl.siteTitle;

				// reject and "D" lines for non-student roles
				if ((!ROLE_STUDENT.equals(rl.role)) && "D".equals(rl.status))
				{
					badLines.add("# non-student drop\n" + line);
					continue;
				}

				// reject an instructor role line if we are not allowing them
				if (onlyStudentLines && (!rl.role.equals(ROLE_STUDENT)))
				{
					badLines.add("# only student role allowed\n" + line);
					continue;
				}

				// for the special create site directive
				if (createSitesDirective)
				{
					rl.role = ROLE_INSTRUCTOR;
					rl.eid = "admin";
					rl.iid = "admin";
				}

				// for the special ignore drops directive
				if (ignoreDropsDirective)
				{
					if ("D".equals(rl.status)) continue;
				}

				// pick up institutionCode from the site title, if missing (only for this one line)
				String institutionCode = bufferInstitutionCode;
				if (institutionCode == null)
				{
					institutionCode = findInstitutionCode(rl.siteTitle);
				}

				// reject line if we don't have an institution code
				if (institutionCode == null)
				{
					badLines.add("# unknown institution code\n" + line);
					continue;
				}

				// find the user
				User user = findUser(institutionCode, rl.iid);

				// if this is an instructor role line, assure the site exists, and has the section
				if (ROLE_INSTRUCTOR.equals(rl.role))
				{
					// the user must already exist, AND be a member of the user's group
					boolean inUg = userInUsersGroup(usersGroup, user);
					if ((user == null) || (!inUg))
					{
						if (!createInstructorsDirective)
						{
							if (user == null)
							{
								badLines.add("# instructor not found\n" + line);
							}
							else
							{
								badLines.add("# instructor not in Users Group\n" + line);
							}
							continue;
						}
						else
						{
							// assure the user exists
							if (user == null)
							{
								user = createUser(rl, institutionCode);
								if (user == null)
								{
									badLines.add("# could not create user\n" + line);
									continue;
								}
							}

							// assure the user is in the UG
							if ((!inUg) && (usersGroup != null) && addToUsersGroupDirective)
							{
								usersGroup.addMember(user.getId(), ROLE_STUDENT, true, false);
							}
						}
					}

					// create the site and section if needed
					Site site = assureSiteSection(rl.siteTitle, rl.section, workingSites, modifiedSites, noSectionsDirective);

					// make sure the user is set at the site level to this role
					setUserInSite(user, rl.role, site, modifiedSites);
				}

				// TA and Evaluator roles are applied at the site level, not in a section
				else if (ROLE_EVALUATOR.equals(rl.role) || ROLE_TA.equals(rl.role))
				{
					// site must exist - identified by site, not section
					Site site = getSite(rl.siteTitle, workingSites);
					if (site == null)
					{
						badLines.add("# site not found\n" + line);
						continue;
					}

					// create the user if needed
					if (user == null)
					{
						user = createUser(rl, institutionCode);
						if (user == null)
						{
							badLines.add("# could not create user\n" + line);
							continue;
						}
					}

					// make sure the user is set at the site level to this role
					setUserInSite(user, rl.role, site, modifiedSites);
				}

				// otherwise, make sure the user is properly registered in the sections indicated by the line - for each site that has that section
				else
				{
					// find any sites that match the roster line
					List<Site> sites = findSites(rl.siteTitle, rl.section, workingSites);

					// if none, we have a bad line
					if (sites.isEmpty())
					{
						badLines.add("# missing section\n" + line);
						missingSections.add(rl.section);
						continue;
					}

					// create the user if needed
					if (user == null)
					{
						user = createUser(rl, institutionCode);

						if (user == null)
						{
							badLines.add("# could not create user\n" + line);
							continue;
						}
					}

					// if we found a user, update it if the roster data has updates (unless the user has multiple IIDs, in which case we leave the user data alone)
					else
					{
						// check for multiple IIDs for the user
						List<String> iids = userDirectoryService().getIid(user.getId());
						boolean multipleIids = (iids.size() > 1);
						if (!multipleIids)
						{
							if (!updateUser(user, rl, eidChanges))
							{
								badLines.add("# user updates could not be applied\n" + line);
								continue;
							}
						}
					}

					// register the user with these sections in these sites
					String sitesNotProcessed = registerUser(user, sites, rl.section, rl.role, rl.status, modifiedSites);
					if (sitesNotProcessed != null)
					{
						badLines.add(sitesNotProcessed + "\n" + line);
					}
				}
			}
			catch (IOException e)
			{
				M_log.warn("processRosterBuffer(): " + e);
			}
		}

		// consolidate and save the working sites
		for (Site s : modifiedSites.values())
		{
			consolidate(s);

			// save
			try
			{
				siteService().save(s);
			}
			catch (IdUnusedException e)
			{
				M_log.warn("processRosterBuffer: " + e);
			}
			catch (PermissionException e)
			{
				M_log.warn("processRosterBuffer: " + e);
			}
		}

		// save the UG
		if (usersGroup != null)
		{
			try
			{
				siteService().save(usersGroup);
			}
			catch (IdUnusedException e)
			{
				M_log.warn("processRosterBuffer: " + e);
			}
			catch (PermissionException e)
			{
				M_log.warn("processRosterBuffer: " + e);
			}
		}

		// report any issues
		if ((!badLines.isEmpty()) || (!missingSections.isEmpty()) || (!eidChanges.isEmpty()))
		{
			emailRosterReport(badLines, missingSections, eidChanges, source);
		}

		// check for sites with no roster-loaded students (only if we just processed an official student roster file)
		if (onlyStudentLines && (oneSeenTitle != null))
		{
			// which client, which term
			String[] parts = StringUtil.split(oneSeenTitle, " ");
			String prefix = parts[0].toLowerCase();
			String suffix = parts[parts.length - 1].toLowerCase();

			List<Site> clientTermSites = findSitesByClientTerm(prefix, suffix);
			List<Site> clientTermSitesNoRoster = new ArrayList<Site>();
			for (Site cts : clientTermSites)
			{
				// skip if the site has no sections
				boolean noSections = true;
				for (Group g : (Collection<Group>) cts.getGroups())
				{
					if (g.getProperties().getProperty("group_prop_wsetup_created") == null)
					{
						// we see a section
						noSections = false;
						break;
					}
				}
				if (noSections) continue;

				boolean hasRoster = false;
				for (Member sm : (Set<Member>) cts.getMembers())
				{
					if (sm.isProvided() && (ROLE_STUDENT.equals(sm.getRole().getId()) || ROLE_BLOCKED.equals(sm.getRole().getId())))
					{
						hasRoster = true;
						break;
					}
				}
				if (!hasRoster) clientTermSitesNoRoster.add(cts);
			}

			if (!clientTermSitesNoRoster.isEmpty())
			{
				emailNoRoster(prefix, suffix, clientTermSitesNoRoster);
			}
		}
	}

	/**
	 * Process the file of roster text.
	 * 
	 * @param file
	 *        The file of roster text.
	 */
	protected void processRosterFile(File file)
	{
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(file));

			// find the institution code from the file name. Will be null if we don't for file not registered.
			String institutionCode = findInstitutionCode(file);

			processRosterBuffer(br, institutionCode, file.getName());
		}
		catch (FileNotFoundException e)
		{
			M_log.warn("processRosterFile(): " + e);
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					M_log.warn("processRosterFile(): on close of reader " + e);
				}
			}
		}
	}

	/**
	 * Setup a security advisor.
	 */
	protected void pushAdvisor()
	{
		// setup a security advisor
		securityService().pushAdvisor(new SecurityAdvisor()
		{
			public SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				if ("site.visit".equals(function)) return SecurityAdvice.PASS;
				return SecurityAdvice.ALLOWED;
			}
		});
	}

	/**
	 * Update (or add) the user's membership in this section of these sites to match that in the roster line. Don't touch the site's membership yet (dont later in consolidation).
	 * 
	 * @param user
	 * @param sites
	 * @param section
	 * @param role
	 * @param status
	 * @param modifiedSites
	 * @return null if all went well, or a "bad lines" error string if the registration failed for at least one site.
	 */
	@SuppressWarnings("unchecked")
	protected String registerUser(User user, List<Site> sites, String section, String role, String status, Map<String, Site> modifiedSites)
	{
		String sitesNotProcessed = null;
		for (Site site : sites)
		{
			// if the user has a site level membership that is instructor, ta or evaluator, we won't override it
			Member sm = site.getMember(user.getId());
			if (sm != null)
			{
				if (ROLE_INSTRUCTOR.equals(sm.getRole().getId()) || ROLE_TA.equals(sm.getRole().getId())
						|| ROLE_EVALUATOR.equals(sm.getRole().getId()))
				{
					if (sitesNotProcessed == null)
					{
						sitesNotProcessed = "# PARTIALLY PROCESSED: not in these sites (user already non-student member): " + site.getTitle();
					}
					else
					{
						sitesNotProcessed += ", " + site.getTitle();
					}

					continue;
				}
			}

			// find the group (section)
			for (Group g : (Collection<Group>) site.getGroups())
			{
				if (g.getTitle().equalsIgnoreCase(section) && (g.getProperties().getProperty("group_prop_wsetup_created") == null))
				{
					Member gm = g.getMember(user.getId());
					if (gm == null)
					{
						g.addMember(user.getId(), role, "E".equals(status), true);
						modifiedSites.put(site.getId(), site);
					}
					else if ((!role.equals(gm.getRole().getId())) || (gm.isActive() != "E".equals(status)))
					{
						g.removeMember(user.getId());
						g.addMember(user.getId(), role, "E".equals(status), true);
						modifiedSites.put(site.getId(), site);
					}

					break;
				}
			}
		}

		return sitesNotProcessed;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void runTask(ScheduleEntryImpl entry)
	{
		// we run the file sync
		syncWithRosterFiles("admin", entry);
	}

	/**
	 * Set the user in the site's membership with this role as active and provided. Remove the user from any sections.
	 * 
	 * @param user
	 * @param role
	 * @param site
	 * @param modifiedSites
	 */
	@SuppressWarnings("unchecked")
	protected void setUserInSite(User user, String role, Site site, Map<String, Site> modifiedSites)
	{
		Member m = site.getMember(user.getId());

		if (m == null)
		{
			site.addMember(user.getId(), role, true, true);
			modifiedSites.put(site.getId(), site);
		}

		else
		{
			// remove the user from all sections
			for (Group g : (Collection<Group>) site.getGroups())
			{
				if (g.getProperties().getProperty("group_prop_wsetup_created") == null)
				{
					Member gm = g.getMember(user.getId());
					if (gm != null)
					{
						g.removeMember(user.getId());
						modifiedSites.put(site.getId(), site);
					}
				}
			}

			if ((!role.equals(m.getRole().getId())) || (m.isActive() != true) || (!m.isProvided()))
			{
				site.removeMember(user.getId());
				site.addMember(user.getId(), role, true, true);
				modifiedSites.put(site.getId(), site);
			}
		}
	}

	/**
	 * Run the roster file sync with this optional schedule object.
	 * 
	 * @param authenticatedUserId
	 *        The user making the request.
	 * @param schedule
	 *        The optional schedule object.
	 */
	protected void syncWithRosterFiles(final String authenticatedUserId, final ScheduleEntryImpl schedule)
	{
		// run on the task thread
		synchronized (taskSync)
		{
			if (this.task != null)
			{
				M_log.warn("syncWithRosterFiles: blocked by running task");
				return;
			}
			this.task = new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						// set the user into the thread
						Session s = sessionManager().getCurrentSession();
						if (s != null)
						{
							s.setUserId(authenticatedUserId);
						}

						pushAdvisor();

						// do the work
						message("processing roster files");
						processFiles();
						message("processing roster files complete");

						synchronized (taskSync)
						{
							task = null;
						}
					}
					finally
					{
						popAdvisor();

						if (task != null)
						{
							task = null;
						}

						threadLocalManager().clear();

						// record end time
						if (schedule != null)
						{
							schedule.ended = new Date();
						}
					}
				}
			}, getClass().getName());
		}

		this.task.start();
	}

	/**
	 * Check if the user is in the Users Group site.
	 * 
	 * @param usersGroup
	 *        The Users Group site.
	 * @param user
	 *        The user.
	 * @return true if there, false if not.
	 */
	protected boolean userInUsersGroup(Site usersGroup, User user)
	{
		if ((user == null) || (usersGroup == null)) return false;

		// Note: our advisor will get called! It will ignore a "site.visit" request
		return securityService().unlock(user, "site.visit", usersGroup.getReference());
	}

	/**
	 * If a user record's EID, lastname or firstname has changed in the roster line, update the user record, and report to the user via email if the EID has changed.
	 * 
	 * @param user
	 *        The user record.
	 * @param rl
	 *        The roster line.
	 * @return true if all went well, false if there was a problem.
	 */
	boolean updateUser(User user, RosterLine rl, List<String> eidChanges)
	{
		if (!user.getEid().equalsIgnoreCase(rl.eid))
		{
			try
			{
				String oldEid = user.getEid();
				String eidReport = "EID change for " + user.getSortName() + " EID: " + oldEid + " IID: " + user.getIidDisplay() + "    to: "
						+ rl.lastName + ", " + rl.firstName + " EID: " + rl.eid + "   email: " + user.getEmail() + ", " + rl.email;

				UserEdit edit = userDirectoryService().editUser(user.getId());
				edit.setEid(rl.eid);
				edit.setLastName(rl.lastName);
				edit.setFirstName(rl.firstName);

				userDirectoryService().commitEdit(edit);

				// email and report
				emailEidChange(rl, oldEid, edit);
				eidChanges.add(eidReport);

				return true;
			}
			catch (UserPermissionException e)
			{
			}
			catch (UserLockedException e)
			{
			}
			catch (UserAlreadyDefinedException e)
			{
			}
			catch (UserNotDefinedException e)
			{
			}

			return false;
		}

		if ((!user.getLastName().equalsIgnoreCase(rl.lastName)) || (!user.getFirstName().equalsIgnoreCase(rl.firstName)))
		{
			try
			{
				UserEdit edit = userDirectoryService().editUser(user.getId());
				edit.setLastName(rl.lastName);
				edit.setFirstName(rl.firstName);

				userDirectoryService().commitEdit(edit);

				return true;
			}
			catch (UserPermissionException e)
			{
			}
			catch (UserLockedException e)
			{
			}
			catch (UserAlreadyDefinedException e)
			{
			}
			catch (UserNotDefinedException e)
			{
			}

			return false;
		}

		return true;
	}

	/**
	 * @return The AuthzGroupService, via the component manager.
	 */
	private AuthzGroupService authzGroupService()
	{
		return (AuthzGroupService) ComponentManager.get(AuthzGroupService.class);
	}

	/**
	 * @return The EmailService, via the component manager.
	 */
	private EmailService emailService()
	{
		return (EmailService) ComponentManager.get(EmailService.class);
	}

	/**
	 * @return The IdManager, via the component manager.
	 */
	private IdManager idManager()
	{
		return (IdManager) ComponentManager.get(IdManager.class);
	}

	/**
	 * @return The SecurityService, via the component manager.
	 */
	private SecurityService securityService()
	{
		return (SecurityService) ComponentManager.get(SecurityService.class);
	}

	/**
	 * @return The ServerConfigurationService, via the component manager.
	 */
	private ServerConfigurationService serverConfigurationService()
	{
		return (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
	}

	/**
	 * @return The SessionManager, via the component manager.
	 */
	private SessionManager sessionManager()
	{
		return (SessionManager) ComponentManager.get(SessionManager.class);
	}

	/**
	 * @return The SiteService, via the component manager.
	 */
	private SiteService siteService()
	{
		return (SiteService) ComponentManager.get(SiteService.class);
	}

	/**
	 * @return The SqlService, via the component manager.
	 */
	private SqlService sqlService()
	{
		return (SqlService) ComponentManager.get(SqlService.class);
	}

	/**
	 * @return The ThreadLocalManager, via the component manager.
	 */
	private ThreadLocalManager threadLocalManager()
	{
		return (ThreadLocalManager) ComponentManager.get(ThreadLocalManager.class);
	}

	/**
	 * @return The ToolManager, via the component manager.
	 */
	private ToolManager toolManager()
	{
		return (ToolManager) ComponentManager.get(ToolManager.class);
	}

	/**
	 * @return The UserDirectoryService, via the component manager.
	 */
	private UserDirectoryService userDirectoryService()
	{
		return (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);
	}
}
