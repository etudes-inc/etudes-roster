/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/roster/trunk/roster-api/api/src/java/org/etudes/roster/api/SiteVisits.java $
 * $Id: SiteVisits.java 7505 2014-02-26 04:29:41Z ggolden $
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
 * SiteVisits models visits and visitors to a site.
 */
public interface SiteVisits
{
	/**
	 * @return The site id.
	 */
	String getSiteId();

	/**
	 * @return The site title.
	 */
	String getSiteTitle();

	/**
	 * @return The # unique visitors.
	 */
	Integer getVisitors();

	/**
	 * @return The # of visits (by all visitors).
	 */
	Integer getVisits();
}
