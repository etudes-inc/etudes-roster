/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/roster/trunk/roster-api/api/src/java/org/etudes/roster/api/RosterService.java $
 * $Id: RosterService.java 9028 2014-10-25 01:28:50Z ggolden $
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

package org.etudes.roster.api;

import java.util.Collection;
import java.util.List;

import org.sakaiproject.site.api.Site;

/**
 * RosterService synchronizes external information with users, sites, and membership.
 */
public interface RosterService extends ScheduleService
{
	/**
	 * Add this section to this site, based on the section enrollment in other sites (in the site's same term and client). Adjust site membership to the resulting section enrollment.
	 * 
	 * @param authenticatedUserId
	 *        The user making the request.
	 * @param section
	 *        The section id.
	 * @param siteId
	 *        The site id.
	 */
	void adddSectionToSite(String authenticatedUserId, String section, String siteId);

	/**
	 * Add these users to the site as active / provided and with this role. If the user already exists in the site, skip it.
	 * 
	 * @param authenticatedUserId
	 *        The user making the request.
	 * @param userIds
	 *        The user ids.
	 * @param siteId
	 *        The site id.
	 * @param role
	 *        The role id.
	 */
	void addUsersToSite(String authenticatedUserId, Collection<String> userIds, String siteId, String role);

	/**
	 * Check security for access to roster services
	 * 
	 * @param authenticatedUserId
	 *        The user id to check.
	 * @return true if the user has access, false if not.
	 * 
	 */
	boolean allowRosterAccess(String authenticatedUserId);

	/**
	 * Get the client's full name from the site title prefix.
	 * 
	 * @param clientPrefix
	 *        The site title client prefix.
	 * @return The client full name. Return the prefix if not found.
	 */
	String clientName(String clientPrefix);

	/**
	 * Figure the institution code for this site based on the title prefix.
	 * 
	 * @param siteTitle
	 *        The site title.
	 * @return The institution code for this site.
	 */
	String findInstitutionCode(String siteTitle);

	/**
	 * Find the sites matching this prefix and suffix, that have this section.
	 * 
	 * @param clientPrefix
	 *        The site title client prefix.
	 * @param termSuffix
	 *        The site title term suffix.
	 * @param section
	 *        The section id.
	 * @return The list of sites found, or empty if none found.
	 */
	List<Site> findSectionSites(String clientPrefix, String termSuffix, String section);

	/**
	 * Find a site given its title.
	 * 
	 * @param title
	 *        The site title.
	 * @return The site, or null if not found.
	 */
	Site findSiteByTitle(String title);

	/**
	 * Find the sites matching this prefix and suffix.
	 * 
	 * @param clientPrefix
	 *        The site title client prefix.
	 * @param termSuffix
	 *        The site title term suffix.
	 * @return The list of sites found, or empty if none found.
	 */
	List<Site> findSitesByClientTerm(String clientPrefix, String termSuffix);

	/**
	 * @return A list of all the roster file names waiting for processing, or an empty list if there are none.
	 * @param authenticatedUserId
	 *        The user making the request.
	 */
	List<String> getRosterFileNames(String authenticatedUserId);

	/**
	 * @return a roster text comment that describes the roster line format
	 */
	String getSampleRosterComment();

	/**
	 * Remove this section from this site. Adjust site membership to the resulting section enrollment.
	 * 
	 * @param authenticatedUserId
	 *        The user making the request.
	 * @param section
	 *        The section title.
	 * @param siteId
	 *        The site id.
	 * @return true if the section was successfully removed from the site, false if not.
	 */
	boolean removeSectionFromSite(String authenticatedUserId, String section, String siteId);

	/**
	 * Remove this section from all sites in which it lives, for sites matching this prefix and suffix. Adjust site membership to the resulting section enrollment.
	 * 
	 * @param authenticatedUserId
	 *        The user making the request.
	 * @param section
	 *        The section title.
	 * @param clientPrefix
	 *        The site title client prefix.
	 * @param termSuffix
	 *        The site title term suffix.
	 * @return true if the section was successfully removed from at least one site, false if not.
	 */
	boolean removeSectionFromSites(String authenticatedUserId, String section, String clientPrefix, String termSuffix);

	/**
	 * Remove all sections from this site. Adjust site membership to the resulting section enrollment.
	 * 
	 * @param authenticatedUserId
	 *        The user making the request.
	 * @param siteId
	 *        The site id.
	 * @param siteId
	 * @return true if the site was found and is now clear of sections, false if not.
	 */
	boolean removeSectionsFromSite(String authenticatedUserId, String siteId);

	/**
	 * If the user exists in the site at the site level (not section), is provided, and has a role other than Student or Blocked, remove the user.
	 * 
	 * @param authenticatedUserId
	 *        The user making the request.
	 * @param userId
	 *        The user id.
	 * @param siteId
	 *        The site id.
	 */
	void removeUserFromSite(String authenticatedUserId, String userId, String siteId);

	/**
	 * Generate the "Seats Per Site" report data for the client, for the sites in the term.
	 * 
	 * @param clientPrefix
	 *        The client site title prefix.
	 * @param term
	 *        The site title suffix.
	 * @return The SiteEnrollment list for all sites in the term for the client.
	 */
	List<SiteEnrollment> reportSeatsPerSite(String clientPrefix, String term);

	/**
	 * Generate the "Seats Per Term" report data for the client and terms
	 * 
	 * @param clientPrefix
	 *        The client site title prefix.
	 * @param terms
	 *        An array of the term site title suffix.
	 * @return The TermEnrollment list for all terms requested.
	 */
	List<TermEnrollment> reportSeatsPerTerm(String clientPrefix, String[] terms);

	/**
	 * Generate the "Visits Per Site" report data for the client and term.
	 * 
	 * @param clientPrefix
	 *        The client site title prefix.
	 * @param termSuffix
	 *        The term site title suffix.
	 * @return The SiteVisits list for all sites found.
	 */
	List<SiteVisits> reportVisitsPerSite(String clientPrefix, String termSuffix);

	/**
	 * Read and process any files in the configured roster file directory, updating users, sites and membership information to match.
	 * 
	 * @param authenticatedUserId
	 *        The user making the request.
	 */
	void syncWithRosterFiles(String authenticatedUserId);

	/**
	 * Process given roster file lines, updating users, sites and membership information to match.
	 * 
	 * @param authenticatedUserId
	 *        The user making the request.
	 * @param text
	 *        The text.
	 * @param institutionCode
	 *        The institution code for the people and courses referenced in the text.
	 */
	void syncWithRosterText(String authenticatedUserId, String text, String institutionCode);

	/**
	 * Get the term numeric code, formatted as a 3 digit number, from a site title suffix.
	 * 
	 * @param termSuffix
	 *        The site title suffix.
	 * @return The term numeric code. Returns the suffix if not found.
	 */
	String termCode(String termSuffix);
	
	/**
	 * Get the term name from a site title suffix.
	 * 
	 * @param termSuffix
	 *        The site title suffix.
	 * @return The term name. Returns the suffix if not found.
	 */
	String termName(String termSuffix);
}
