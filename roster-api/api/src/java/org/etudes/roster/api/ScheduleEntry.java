/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/roster/trunk/roster-api/api/src/java/org/etudes/roster/api/ScheduleEntry.java $
 * $Id: ScheduleEntry.java 351 2009-12-06 02:17:50Z ggolden $
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

import java.util.Date;

/**
 * ScheduleEntry is one entry on a daily task schedule.
 */
public interface ScheduleEntry
{
	/**
	 * @return The Date when this ended, or null if it has not yet ended.
	 */
	Date getEndedDate();

	/**
	 * @return The RunTime when this is set to reschedule, or null if it is currently scheduled.
	 */
	RunTime getRescheduleTime();

	/**
	 * @return The RunTime when this is set to run.
	 */
	RunTime getScheduledTime();

	/**
	 * @return The Date when this started, or null if it has not yet started.
	 */
	Date getStartedDate();
}
