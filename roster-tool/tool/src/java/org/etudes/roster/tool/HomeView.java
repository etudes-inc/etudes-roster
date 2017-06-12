/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/roster/trunk/roster-tool/tool/src/java/org/etudes/roster/tool/HomeView.java $
 * $Id: HomeView.java 7408 2014-02-16 20:22:01Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2009, 2014 Etudes, Inc.
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

package org.etudes.roster.tool;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Value;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.roster.api.RosterService;
import org.etudes.roster.api.ScheduleEntry;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.Web;

/**
 * The /home view for the roster tool.
 */
public class HomeView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(HomeView.class);

	/** Dependency: RosterService. */
	protected RosterService rosterService = null;

	/** Dependency: SecurityService. */
	protected SecurityService securityService = null;

	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params)
	{
		// if not logged in as the super user, we won't do anything
		if (!securityService.isSuperUser())
		{
			throw new IllegalArgumentException();
		}

		// no parameters expected
		if (params.length != 2)
		{
			throw new IllegalArgumentException();
		}

		// get the schedule
		List<ScheduleEntry> schedule = this.rosterService.getSchedule();
		context.put("schedule", schedule);

		// get roster files pending processing
		List<String> files = this.rosterService.getRosterFileNames(sessionManager().getCurrentSessionUserId());
		context.put("files", files);

		// is there a task running?
		if (this.rosterService.isTaskRunning())
		{
			String status = this.rosterService.getTaskStatus();
			context.put("status", status);
		}

		Value text = this.uiService.newValue();
		text.setValue(this.rosterService.getSampleRosterComment());
		context.put("text", text);

		// render
		uiService.render(ui, context);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();

		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		if (!context.getPostExpected())
		{
			throw new IllegalArgumentException();
		}

		// no parameters expected
		if (params.length != 2)
		{
			throw new IllegalArgumentException();
		}

		// read form
		Value text = this.uiService.newValue();
		context.put("text", text);

		Value institution = this.uiService.newValue();
		context.put("institution", institution);

		String destination = uiService.decode(req, context);

		if ("PROCESS_FILES".equals(destination))
		{
			this.rosterService.syncWithRosterFiles(sessionManager().getCurrentSessionUserId());
		}

		else if ("PROCESS_TEXT".equals(destination))
		{
			if (text.getValue() != null)
			{
				this.rosterService.syncWithRosterText(sessionManager().getCurrentSessionUserId(), text.getValue(), institution.getValue());
			}
		}

		destination = "/home";
		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * Set the RosterService.
	 * 
	 * @param service
	 *        The RosterService.
	 */
	public void setRosterService(RosterService service)
	{
		this.rosterService = service;
	}

	/**
	 * Set the security service.
	 * 
	 * @param service
	 *        The security service.
	 */
	public void setSecurityService(SecurityService service)
	{
		this.securityService = service;
	}

	/**
	 * @return The SessionManager, via the component manager.
	 */
	private SessionManager sessionManager()
	{
		return (SessionManager) ComponentManager.get(SessionManager.class);
	}

}
