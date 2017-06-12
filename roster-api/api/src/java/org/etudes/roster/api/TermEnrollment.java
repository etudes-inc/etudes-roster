/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/roster/trunk/roster-api/api/src/java/org/etudes/roster/api/TermEnrollment.java $
 * $Id: TermEnrollment.java 7670 2014-03-23 00:46:12Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2014 Etudes, Inc.
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

/**
 * TermEnrollment models enrollment in a term.
 */
public interface TermEnrollment
{
	/**
	 * @return The client (site title prefix).
	 */
	String getClient();

	/**
	 * @return The number of non-roster, any role, enrolled across the sites in the term for this client.
	 */
	Integer getNumGuests();

	/**
	 * @return The number of non-students enrolled across the sites in the term for this client (official roster instructors, evaluators and TAs).
	 */
	Integer getNumOther();

	/**
	 * @return The number of students enrolled across the sites in the term for this client (official roster students).
	 */
	Integer getNumSeats();

	/**
	 * @return The # different sections in the term for this client.
	 */
	Integer getNumSections();

	/**
	 * @return The # sites in the term for this client.
	 */
	Integer getNumSites();

	/**
	 * @return The term (site title suffix).
	 */
	String getTerm();
}
