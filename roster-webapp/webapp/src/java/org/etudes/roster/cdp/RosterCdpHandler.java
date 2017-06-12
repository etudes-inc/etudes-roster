/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/roster/trunk/roster-webapp/webapp/src/java/org/etudes/roster/cdp/RosterCdpHandler.java $
 * $Id: RosterCdpHandler.java 12757 2016-10-26 21:03:41Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2014, 2015 Etudes, Inc.
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

package org.etudes.roster.cdp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.cdp.api.CdpHandler;
import org.etudes.cdp.api.CdpStatus;
import org.etudes.cdp.util.CdpResponseHelper;
import org.etudes.roster.api.RosterService;
import org.etudes.roster.api.ScheduleEntry;
import org.etudes.roster.api.SiteEnrollment;
import org.etudes.roster.api.SiteVisits;
import org.etudes.roster.api.TermEnrollment;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.StringUtil;

/**
 */
public class RosterCdpHandler implements CdpHandler
{
	class SortMap
	{
		Map<String, String> map;
		String sortBy;
	}

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(RosterCdpHandler.class);

	/** Terms (site title suffix) of interest in reporting. */
	protected String[] termsOfInterest =
	{"w15", "sp15", "su15", "f15", "w16", "sp16", "su16", "f16", "w17", "sp17", "su17", "f17"};

	public String getPrefix()
	{
		return "roster";
	}

	public Map<String, Object> handle(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String requestPath,
			String path, String authenticatedUserId) throws ServletException, IOException
	{
		// if no authenticated user, we reject all requests
		if (authenticatedUserId == null)
		{
			Map<String, Object> rv = new HashMap<String, Object>();
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.notLoggedIn.getId());
			return rv;
		}

		// admin only, please!
		if (!"admin".equals(authenticatedUserId))
		{
			Map<String, Object> rv = new HashMap<String, Object>();
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		else if (requestPath.equals("schedule"))
		{
			return dispatchSchedule(req, res, parameters, path, authenticatedUserId);
		}

		else if (requestPath.equals("processFiles"))
		{
			return dispatchProcessFiles(req, res, parameters, path, authenticatedUserId);
		}

		else if (requestPath.equals("processLines"))
		{
			return dispatchProcessLines(req, res, parameters, path, authenticatedUserId);
		}

		else if (requestPath.equals("status"))
		{
			return dispatchStatus(req, res, parameters, path, authenticatedUserId);
		}

		else if (requestPath.equals("sections"))
		{
			return dispatchSections(req, res, parameters, path, authenticatedUserId);
		}

		else if (requestPath.equals("removeSectionsMapping"))
		{
			return dispatchRemoveSectionsMapping(req, res, parameters, path, authenticatedUserId);
		}

		else if (requestPath.equals("removeSectionsSites"))
		{
			return dispatchRemoveSectionsSites(req, res, parameters, path, authenticatedUserId);
		}

		else if (requestPath.equals("removeSectionsSections"))
		{
			return dispatchRemoveSectionsSections(req, res, parameters, path, authenticatedUserId);
		}

		else if (requestPath.equals("addSectionSite"))
		{
			return dispatchAddSectionSite(req, res, parameters, path, authenticatedUserId);
		}

		else if (requestPath.equals("siteMembership"))
		{
			return dispatchSiteMembership(req, res, parameters, path, authenticatedUserId);
		}

		else if (requestPath.equals("removeUserFromSite"))
		{
			return dispatchRemoveUserFromSite(req, res, parameters, path, authenticatedUserId);
		}

		else if (requestPath.equals("addMembers"))
		{
			return dispatchAddMembers(req, res, parameters, path, authenticatedUserId);
		}

		else if (requestPath.equals("report"))
		{
			return dispatchReport(req, res, parameters, path, authenticatedUserId);
		}

		return null;
	}

	protected Map<String, Object> dispatchAddMembers(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String path,
			String authenticatedUserId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// security
		if (!rosterService().allowRosterAccess(authenticatedUserId))
		{
			M_log.warn("dispatchAddMembers - access denied");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// get the site id parameter
		String siteId = (String) parameters.get("siteId");
		String roleId = (String) parameters.get("role");
		String identifiers = (String) parameters.get("identifiers");
		if ((siteId == null) || (roleId == null) || (identifiers == null))
		{
			M_log.warn("dispatchAddMembers - incomplete parameters");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		// parse the identifiers (iid@instcode)
		String[] iids = StringUtil.split(identifiers, "\r\n");
		List<String> userIds = new ArrayList<String>();
		for (String iid : iids)
		{
			try
			{
				// isolate the iid and instCode
				String[] parts = splitLast(iid, "@");
				if (parts != null)
				{
					User user = userDirectoryService().getUserByIid(parts[1], parts[0]);
					userIds.add(user.getId());
				}
			}
			catch (UserNotDefinedException e)
			{
			}
		}

		try
		{
			Site site = siteService().getSite(siteId);
			rosterService().addUsersToSite(authenticatedUserId, userIds, siteId, roleId);
			doSiteMembership(rv, site);
		}
		catch (IdUnusedException e)
		{
			M_log.warn("dispatchAddMembers - missing site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchAddSectionSite(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters,
			String path, String authenticatedUserId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// security
		if (!rosterService().allowRosterAccess(authenticatedUserId))
		{
			M_log.warn("dispatchAddSectionSite - access denied");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		String client = StringUtil.trimToNull((String) parameters.get("client"));
		String term = StringUtil.trimToNull((String) parameters.get("term"));
		String sectionTitle = StringUtil.trimToNull((String) parameters.get("sectionTitle"));
		String siteTitle = readSiteTitleParam(parameters);

		if ((client == null) || (term == null) || (sectionTitle == null) || (siteTitle == null))
		{
			M_log.warn("dispatchAddSectionSite: incomplete parameters");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		// find the site
		Site site = rosterService().findSiteByTitle(siteTitle);
		if (site != null)
		{
			rosterService().adddSectionToSite(authenticatedUserId, sectionTitle, site.getId());
			rv.put("success", CdpResponseHelper.formatBoolean(Boolean.TRUE));
		}
		else
		{
			rv.put("success", CdpResponseHelper.formatBoolean(Boolean.FALSE));
		}

		// return the section response
		doSections(rv, client, term);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchProcessFiles(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String path,
			String authenticatedUserId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// security
		if (!rosterService().allowRosterAccess(authenticatedUserId))
		{
			M_log.warn("dispatchProcessFiles - access denied");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		rosterService().syncWithRosterFiles(authenticatedUserId);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchProcessLines(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String path,
			String authenticatedUserId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// security
		if (!rosterService().allowRosterAccess(authenticatedUserId))
		{
			M_log.warn("dispatchProcessLines - access denied");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		String lines = (String) parameters.get("lines");
		String instCode = StringUtil.trimToNull((String) parameters.get("instCode"));

		rosterService().syncWithRosterText(authenticatedUserId, lines, instCode);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchRemoveSectionsMapping(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters,
			String path, String authenticatedUserId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// security
		if (!rosterService().allowRosterAccess(authenticatedUserId))
		{
			M_log.warn("dispatchRemoveSectionsMapping - access denied");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		String client = StringUtil.trimToNull((String) parameters.get("client"));
		String term = StringUtil.trimToNull((String) parameters.get("term"));
		String idsParam = StringUtil.trimToNull((String) parameters.get("ids"));

		if ((client == null) || (term == null) || (idsParam == null))
		{
			M_log.warn("dispatchRemoveSectionsMapping: incomplete parameters");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		String ids[] = StringUtil.split(idsParam, "\t");
		boolean result = false;
		for (String id : ids)
		{
			String[] params = StringUtil.splitFirst(id, "@");
			if (rosterService().removeSectionFromSite(authenticatedUserId, params[1], params[0])) result = true;
		}

		// return the section sites response
		doSections(rv, client, term);

		// add a success parameter
		rv.put("success", CdpResponseHelper.formatBoolean(result));

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchRemoveSectionsSections(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters,
			String path, String authenticatedUserId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// security
		if (!rosterService().allowRosterAccess(authenticatedUserId))
		{
			M_log.warn("dispatchRemoveSectionsSections - access denied");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		String client = StringUtil.trimToNull((String) parameters.get("client"));
		String term = StringUtil.trimToNull((String) parameters.get("term"));
		String idsParam = StringUtil.trimToNull((String) parameters.get("ids"));

		if ((client == null) || (term == null) || (idsParam == null))
		{
			M_log.warn("dispatchRemoveSectionsSections: incomplete parameters");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		String sections[] = StringUtil.split(idsParam, "\t");
		boolean result = false;
		for (String section : sections)
		{
			if (rosterService().removeSectionFromSites(authenticatedUserId, section, client, term)) result = true;
		}

		// return the section sites response
		doSections(rv, client, term);

		// add a success parameter
		rv.put("success", CdpResponseHelper.formatBoolean(result));

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchRemoveSectionsSites(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters,
			String path, String authenticatedUserId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// security
		if (!rosterService().allowRosterAccess(authenticatedUserId))
		{
			M_log.warn("dispatchRemoveSectionsSites - access denied");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		String client = StringUtil.trimToNull((String) parameters.get("client"));
		String term = StringUtil.trimToNull((String) parameters.get("term"));
		String idsParam = StringUtil.trimToNull((String) parameters.get("ids"));

		if ((client == null) || (term == null) || (idsParam == null))
		{
			M_log.warn("dispatchRemoveSectionsSites: incomplete parameters");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		String ids[] = StringUtil.split(idsParam, "\t");
		boolean result = false;
		for (String siteId : ids)
		{
			if (rosterService().removeSectionsFromSite(authenticatedUserId, siteId)) result = true;
		}

		// return the section sites response
		doSections(rv, client, term);

		// add a success parameter
		rv.put("success", CdpResponseHelper.formatBoolean(result));

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchRemoveUserFromSite(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters,
			String path, String authenticatedUserId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// security
		if (!rosterService().allowRosterAccess(authenticatedUserId))
		{
			M_log.warn("dispatchRemoveUserFromSite - access denied");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		String siteId = StringUtil.trimToNull((String) parameters.get("siteId"));
		String idsParam = StringUtil.trimToNull((String) parameters.get("ids"));

		if ((siteId == null) || (idsParam == null))
		{
			M_log.warn("dispatchRemoveUserFromSite: incomplete parameters");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		String ids[] = StringUtil.split(idsParam, "\t");

		// find the site
		Site site = null;
		try
		{
			site = siteService().getSite(siteId);

			// remove the users
			for (String id : ids)
			{
				rosterService().removeUserFromSite(authenticatedUserId, id, siteId);
			}

			site = siteService().getSite(siteId);
			doSiteMembership(rv, site);
		}
		catch (IdUnusedException e)
		{
			M_log.warn("dispatchRemoveUserFromSite: missing site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchReport(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String path,
			String authenticatedUserId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		String report = (String) parameters.get("report");
		String client = StringUtil.trimToNull((String) parameters.get("client"));
		String term = StringUtil.trimToNull((String) parameters.get("term"));
		if ((report == null) || (client == null) || (term == null))
		{
			M_log.warn("dispatchReport - incomplete parameters");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		Map<String, Object> reportMap = new HashMap<String, Object>();
		rv.put("report", reportMap);
		reportMap.put("report", report);

		if (report.equals("1"))
		{
			generateVisitsPerSiteReport(client, term, reportMap);
		}
		else if (report.equals("2"))
		{
			generateSeatsPerTermReport(client, reportMap);
		}
		else if (report.equals("3"))
		{
			generateSeatsPerSiteReport(client, term, reportMap);
		}

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchSchedule(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String path,
			String authenticatedUserId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// security
		if (!rosterService().allowRosterAccess(authenticatedUserId))
		{
			M_log.warn("dispatchSchedule - access denied");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		doSchedule(authenticatedUserId, rv);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchSections(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String path,
			String authenticatedUserId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// security
		if (!rosterService().allowRosterAccess(authenticatedUserId))
		{
			M_log.warn("dispatchSections - access denied");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		String client = StringUtil.trimToNull((String) parameters.get("client"));
		String term = StringUtil.trimToNull((String) parameters.get("term"));

		doSections(rv, client, term);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchSiteMembership(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters,
			String path, String authenticatedUserId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// security
		if (!rosterService().allowRosterAccess(authenticatedUserId))
		{
			M_log.warn("dispatchSiteMembership - access denied");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		String siteTitle = readSiteTitleParam(parameters);
		if (siteTitle == null)
		{
			M_log.warn("dispatchSiteMembership: incomplete parameters");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		// find the site
		Site site = rosterService().findSiteByTitle(siteTitle);
		doSiteMembership(rv, site);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchStatus(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String path,
			String authenticatedUserId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// security
		if (!rosterService().allowRosterAccess(authenticatedUserId))
		{
			M_log.warn("dispatchStatus - access denied");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// is there a task running?
		if (rosterService().isTaskRunning())
		{
			String status = rosterService().getTaskStatus();
			rv.put("status", status);
		}

		String schedule = (String) parameters.get("schedule");
		if ("1".equals(schedule))
		{
			doSchedule(authenticatedUserId, rv);
		}

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected void doSchedule(String authenticatedUserId, Map<String, Object> rv)
	{
		// the schedule items
		List<Map<String, String>> scheduleMap = new ArrayList<Map<String, String>>();
		rv.put("schedule", scheduleMap);

		Map<String, String> scheduleEntryMap = null;

		List<ScheduleEntry> schedule = rosterService().getSchedule();
		for (ScheduleEntry s : schedule)
		{
			scheduleEntryMap = new HashMap<String, String>();
			scheduleMap.add(scheduleEntryMap);
			scheduleEntryMap.put("scheduled", s.getScheduledTime() == null ? "" : s.getScheduledTime().toString());
			scheduleEntryMap.put("started", s.getStartedDate() == null ? "" : CdpResponseHelper.dateDisplayInUserZone(s.getStartedDate().getTime()));
			scheduleEntryMap.put("ended", s.getEndedDate() == null ? "" : CdpResponseHelper.dateDisplayInUserZone(s.getEndedDate().getTime()));
			scheduleEntryMap.put("reschedule", s.getRescheduleTime() == null ? "" : s.getRescheduleTime().toString());
		}

		// the files
		List<Map<String, String>> filesMap = new ArrayList<Map<String, String>>();
		rv.put("files", filesMap);

		Map<String, String> fileEntryMap = null;

		List<String> files = rosterService().getRosterFileNames(authenticatedUserId);
		for (String fname : files)
		{
			fileEntryMap = new HashMap<String, String>();
			filesMap.add(fileEntryMap);
			fileEntryMap.put("name", fname);
		}
	}

	@SuppressWarnings("unchecked")
	protected void doSections(Map<String, Object> rv, String client, String term)
	{
		// the sites
		Map<String, Object> siteMap = null;
		List<Map<String, Object>> sitesMap = new ArrayList<Map<String, Object>>();
		rv.put("sites", sitesMap);
		List<Site> sites = rosterService().findSitesByClientTerm(client, term);
		for (Site site : sites)
		{
			siteMap = new HashMap<String, Object>();
			sitesMap.add(siteMap);
			siteMap.put("title", site.getTitle());
			siteMap.put("siteId", site.getId());

			// the sections
			Map<String, String> sectionMap = null;
			List<Map<String, String>> sectionsMap = new ArrayList<Map<String, String>>();
			siteMap.put("sections", sectionsMap);
			for (Group g : (Collection<Group>) site.getGroups())
			{
				if (g.getProperties().getProperty("group_prop_wsetup_created") == null)
				{
					sectionMap = new HashMap<String, String>();
					sectionsMap.add(sectionMap);
					sectionMap.put("sectionId", g.getId());
					sectionMap.put("title", g.getTitle());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void doSiteMembership(Map<String, Object> rv, Site site)
	{
		if (site == null) return;

		Map<String, Object> siteMap = new HashMap<String, Object>();
		rv.put("site", siteMap);

		siteMap.put("siteId", site.getId());
		siteMap.put("title", site.getTitle());

		List<SortMap> maps = new ArrayList<SortMap>();
		for (Member m : (Set<Member>) site.getMembers())
		{
			try
			{
				User user = userDirectoryService().getUser(m.getUserId());

				Map<String, String> memberMap = new HashMap<String, String>();
				SortMap sm = new SortMap();
				sm.map = memberMap;
				sm.sortBy = user.getSortName().toLowerCase();
				maps.add(sm);

				memberMap.put("userId", user.getId());
				memberMap.put("eid", user.getEid());
				memberMap.put("iid", userDirectoryService().getUser(m.getUserId()).getIidDisplay());
				// memberMap.put("displayName", user.getDisplayName());
				memberMap.put("role", m.getRole().getId());
				memberMap.put("sortName", user.getSortName());
				memberMap.put("active", CdpResponseHelper.formatBoolean(m.isActive()));
				memberMap.put("roster", CdpResponseHelper.formatBoolean(m.isProvided()));
			}
			catch (UserNotDefinedException e)
			{
			}
		}
		Collections.sort(maps, new Comparator<SortMap>()
		{
			public int compare(SortMap arg0, SortMap arg1)
			{
				int rv = arg0.sortBy.compareTo(arg1.sortBy);
				return rv;
			}
		});
		List<Map<String, String>> members = new ArrayList<Map<String, String>>();
		siteMap.put("members", members);
		for (SortMap sm : maps)
		{
			members.add(sm.map);
		}

		List<Map<String, Object>> sections = new ArrayList<Map<String, Object>>();
		siteMap.put("sections", sections);

		List<Group> sortedGroups = new ArrayList<Group>(site.getGroups());
		Collections.sort(sortedGroups, new Comparator<Group>()
		{
			public int compare(Group arg0, Group arg1)
			{
				int rv = arg0.getTitle().toLowerCase().compareTo(arg1.getTitle().toLowerCase());
				return rv;
			}
		});

		for (Group g : sortedGroups)
		{
			if (g.getProperties().getProperty("group_prop_wsetup_created") == null)
			{
				Map<String, Object> sectionMap = new HashMap<String, Object>();
				sections.add(sectionMap);

				sectionMap.put("sectionId", g.getId());
				sectionMap.put("title", g.getTitle());

				maps = new ArrayList<SortMap>();
				for (Member m : (Set<Member>) g.getMembers())
				{
					try
					{
						User user = userDirectoryService().getUser(m.getUserId());

						Map<String, String> memberMap = new HashMap<String, String>();
						SortMap sm = new SortMap();
						sm.map = memberMap;
						sm.sortBy = user.getSortName().toLowerCase();
						maps.add(sm);

						memberMap.put("userId", user.getId());
						memberMap.put("eid", user.getEid());
						memberMap.put("iid", userDirectoryService().getUser(m.getUserId()).getIidDisplay());
						// memberMap.put("displayName", user.getDisplayName());
						memberMap.put("role", m.getRole().getId());
						memberMap.put("sortName", user.getSortName());
						memberMap.put("active", CdpResponseHelper.formatBoolean(m.isActive()));
						memberMap.put("roster", CdpResponseHelper.formatBoolean(m.isProvided()));
					}
					catch (UserNotDefinedException e)
					{
					}
				}

				Collections.sort(maps, new Comparator<SortMap>()
				{
					public int compare(SortMap arg0, SortMap arg1)
					{
						int rv = arg0.sortBy.compareTo(arg1.sortBy);
						return rv;
					}
				});

				members = new ArrayList<Map<String, String>>();
				sectionMap.put("members", members);
				for (SortMap sm : maps)
				{
					members.add(sm.map);
				}
			}
		}
	}

	/**
	 * Generate the Seats Per Site report for the client and term (all sites in term).
	 * 
	 * @param client
	 *        The client site title prefix
	 * @param term
	 *        The term site title suffix
	 * @param reportMap
	 *        The map to contain the results.
	 */
	protected void generateSeatsPerSiteReport(String client, String term, Map<String, Object> reportMap)
	{
		List<SiteEnrollment> sites = rosterService().reportSeatsPerSite(client, term);
		reportMap.put("title", "Seats Per Site");
		reportMap.put("client", rosterService().clientName(client));
		reportMap.put("term", rosterService().termName(term));
		reportMap.put("termCode", rosterService().termCode(term));

		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		reportMap.put("results", results);

		for (SiteEnrollment se : sites)
		{
			Map<String, String> resultMap = new HashMap<String, String>();
			resultMap.put("siteTitle", se.getSiteTitle());
			resultMap.put("siteId", rosterService().termName(se.getSiteId()));
			resultMap.put("term", rosterService().termName(se.getTerm()));
			resultMap.put("guests", CdpResponseHelper.formatInt(se.getNumGuests()));
			resultMap.put("others", CdpResponseHelper.formatInt(se.getNumOther()));
			resultMap.put("seats", CdpResponseHelper.formatInt(se.getNumSeats()));
			results.add(resultMap);
		}
	}

	/**
	 * Generate the Seats Per Term report for the client and terms
	 * 
	 * @param client
	 *        The client site title prefix
	 * @param reportMap
	 *        The map to contain the results.
	 */
	protected void generateSeatsPerTermReport(String client, Map<String, Object> reportMap)
	{
		List<TermEnrollment> terms = rosterService().reportSeatsPerTerm(client, this.termsOfInterest);
		reportMap.put("title", "Seats Per Term");
		reportMap.put("client", rosterService().clientName(client));

		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		reportMap.put("results", results);

		for (TermEnrollment te : terms)
		{
			Map<String, String> resultMap = new HashMap<String, String>();
			resultMap.put("term", rosterService().termName(te.getTerm()));
			resultMap.put("termCode", rosterService().termCode(te.getTerm()));
			resultMap.put("guests", CdpResponseHelper.formatInt(te.getNumGuests()));
			resultMap.put("others", CdpResponseHelper.formatInt(te.getNumOther()));
			resultMap.put("seats", CdpResponseHelper.formatInt(te.getNumSeats()));
			resultMap.put("sections", CdpResponseHelper.formatInt(te.getNumSections()));
			resultMap.put("sites", CdpResponseHelper.formatInt(te.getNumSites()));
			results.add(resultMap);
		}
	}

	/**
	 * Generate the Visits Per Site report for the client and term.
	 * 
	 * @param client
	 *        The client site title prefix
	 * @param term
	 *        The term site title suffix
	 * @param reportMap
	 *        The map to contain the results.
	 */
	protected void generateVisitsPerSiteReport(String client, String term, Map<String, Object> reportMap)
	{
		List<SiteVisits> visits = rosterService().reportVisitsPerSite(client, term);
		reportMap.put("title", "Visits Per Site");
		reportMap.put("client", rosterService().clientName(client));
		reportMap.put("term", rosterService().termName(term));
		reportMap.put("termCode", rosterService().termCode(term));

		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		reportMap.put("results", results);

		int sites = 0;
		int unused = 0;
		for (SiteVisits sv : visits)
		{
			Map<String, String> resultMap = new HashMap<String, String>();
			resultMap.put("siteTitle", sv.getSiteTitle());
			resultMap.put("siteId", sv.getSiteId());
			resultMap.put("visits", CdpResponseHelper.formatInt(sv.getVisits()));
			resultMap.put("visitors", CdpResponseHelper.formatInt(sv.getVisitors()));
			results.add(resultMap);

			sites++;
			if (sv.getVisits() == 0) unused++;
		}

		reportMap.put("numberSites", CdpResponseHelper.formatInt(sites));
		reportMap.put("numberUnused", CdpResponseHelper.formatInt(unused));
	}

	/**
	 * Read a site title from the UI, collapsing any multiple internal spaces.
	 * 
	 * @param parameters
	 *        The request parameters.
	 * @return The site title, or null if not found.
	 */
	protected String readSiteTitleParam(Map<String, Object> parameters)
	{
		String siteTitle = StringUtil.trimToNull((String) parameters.get("siteTitle"));
		if (siteTitle != null)
		{
			siteTitle = siteTitle.replaceAll("\\s+", " ").toUpperCase();
		}
		return siteTitle;
	}

	/**
	 * Split the source into two strings at the last occurrence of the splitter.<br />
	 * Previous occurrences are not treated specially, and may be part of the first string.
	 * 
	 * @param source
	 *        The string to split
	 * @param splitter
	 *        The string that forms the boundary between the two strings returned.
	 * @return An array of two strings split from source by splitter.
	 */
	protected String[] splitLast(String source, String splitter)
	{
		String start = null;
		String end = null;

		// find last splitter in source
		int pos = source.lastIndexOf(splitter);

		// if not found, return null
		if (pos == -1)
		{
			return null;
		}

		// take up to the splitter for the start
		start = source.substring(0, pos);

		// and the rest after the splitter
		end = source.substring(pos + splitter.length(), source.length());

		String[] rv = new String[2];
		rv[0] = start;
		rv[1] = end;

		return rv;
	}

	/**
	 * @return The RosterService, via the component manager.
	 */
	private RosterService rosterService()
	{
		return (RosterService) ComponentManager.get(RosterService.class);
	}

	/**
	 * @return The SiteService, via the component manager.
	 */
	private SiteService siteService()
	{
		return (SiteService) ComponentManager.get(SiteService.class);
	}

	/**
	 * @return The UserDirectoryService, via the component manager.
	 */
	private UserDirectoryService userDirectoryService()
	{
		return (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);
	}
}
