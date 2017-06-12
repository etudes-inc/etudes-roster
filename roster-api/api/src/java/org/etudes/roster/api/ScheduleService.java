/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/roster/trunk/roster-api/api/src/java/org/etudes/roster/api/ScheduleService.java $
 * $Id: ScheduleService.java 353 2009-12-06 20:18:27Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2009 Etudes, Inc.
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

package org.etudes.roster.api;

import java.util.List;

/**
 * ScheduleService handles the schedule and task parts of a schedule-based service.
 */
public interface ScheduleService
{
	/**
	 * @return the current schedule.
	 */
	List<ScheduleEntry> getSchedule();

	/**
	 * Get the status of the running task.
	 * 
	 * @return The running task status message, or null if there's no running task.
	 */
	String getTaskStatus();

	/**
	 * Check if a task is currently running.
	 * 
	 * @return true if a task is running, false if not.
	 */
	boolean isTaskRunning();
}
