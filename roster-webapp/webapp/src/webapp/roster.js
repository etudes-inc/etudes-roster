tool_obj =
{
	title: "ROSTER",	
	showReset: true,
	modeBarElementId: "roster_mode_bar",
	currentMode: 0,
	instText: "#(tab separated) TITLE user-login-id password last-name first-name email role E/D section full-user-id\n",
	statusTimer: null,
	sites: [],
	siteFilter: null,
	sectionFilter: null,
	client: null,
	term: null,
	totalCount: 0,
	organization: 0,
	clients:
	[
		{code: "cos", name: "College of the Siskiyous"},
		{code: "elac", name: "East Los Angeles College"},
		{code: "ecc", name: "El Camino College"},
		{code: "etu", name: "Etudes"},
		{code: "fh", name: "Foothill College"},
		{code: "hc", name: "Hartnell College"},
		{code: "ind", name: "Individual"},
		{code: "jpds", name: "J Productions Dental Seminars, Inc."},
		{code: "ltcc", name: "Lake Tahoe Community College"},
		{code: "lacc", name: "Los Angeles City College"},
		{code: "lahc", name: "Los Angeles Harbor College"},
		{code: "lamcitv", name: "Los Angeles Mission College - Instructional Television"},
		{code: "lavc", name: "Los Angeles Valley College"},
		{code: "mend", name: "Mendocino College"},
		{code: "jatc", name: "NORCAL JATC"},
		{code: "sjdc", name: "San Joaquin Delta College"},
		{code: "tc", name: "Taft College"},
		{code: "wlac", name: "West Los Angeles College"}
	],
	terms:
	[
		{code: "F18", name: "Fall 2018"},
		{code: "SU18", name: "Summer 2018"},
		{code: "SP18", name: "Spring 2018"},
		{code: "W18", name: "Winter 2018"},
		{code: "F17", name: "Fall 2017"},
		{code: "SU17", name: "Summer 2017"},
		{code: "SP17", name: "Spring 2017"},
		{code: "W17", name: "Winter 2017"},
		{code: "F16", name: "Fall 2016"},
		{code: "SU16", name: "Summer 2016"},
		{code: "SP16", name: "Spring 2016"},
		{code: "W16", name: "Winter 2016"},
		{code: "F15", name: "Fall 2015"},
		{code: "SU15", name: "Summer 2015"},
		{code: "SP15", name: "Spring 2015"},
		{code: "W15", name: "Winter 2015"},
		{code: "DEV", name: "Development"}
	],
	createInstructorDirective: "#@CREATEINSTRUCTORS@\n",
	noSectionsDirective: "#@NOSECTIONS@\n",
	addToUsersGroupDirective: "#@ADDTOUSERSGROUP@\n",
	modes:
	[
		{
			title: "Adhoc",
			elementId: "roster_adhoc",
			icon: "script_edit.png",
			start: function(obj, mode)
			{
			},
			reset: function(obj, mode)
			{
				obj.resetAdhoc(obj);
			},
			stop: function(obj)
			{
			}
		},
		{
			title: "Files",
			elementId: "roster_schedule",
			icon: "disk_multiple.png",
			toolItemTableElementId: "roster_schedule_table",
			headers:
			[
				{title: "Scheduled", type: null, sort: false},
				{title: "Started", type: null, sort: false},
				{title: "Ended", type: null, sort: false},
				{title: "Reschedule", type: null, sort: false},
			],
			start: function(obj, mode)
			{
				obj.loadSchedule(obj);
			},
			reset: function(obj, mode)
			{
				obj.loadSchedule(obj);
			},
			stop: function(obj)
			{
			}
		},
		{
			title: "Sections",
			elementId: "roster_sections",
			icon: "chart_organisation.png",
			toolActionsElementId: "roster_sections_actions",
			actions:
			[
				{title: "Add", icon: "document_add.png", click: function(){tool_obj.confirmAddSectionSite(tool_obj);return false;}, selectRequired: null},
				{title: "Remove", icon: "remove.png", click: function(){tool_obj.confirmRemoveSections(tool_obj);return false;}, selectRequired: "selectSections"}
			],
			toolItemTableElementId: "roster_sections_table",
			headers:
			[
				{title: " ", type: "center", sort: false},
				{title: " ", type: "center", sort: false},
				{title: "Site", type: null, sort: true},
				{title: "Section", type: null, sort: true},
			],
			start: function(obj, mode)
			{
				obj.populateSections(obj);
			},
			reset: function(obj, mode)
			{
				obj.resetSections(obj);
				obj.populateSections(obj);
			},
			stop: function(obj)
			{
				obj.clearSectionsTable(obj);
			}
		},
		{
			title: "Site Roster",
			elementId: "roster_sites",
			icon: "user_female.png",
			toolActionsElementId: "roster_sites_actions",
			actions:
			[
				{title: "Add", icon: "document_add.png", click: function(){tool_obj.confirmAddToSite(tool_obj);return false;}, selectRequired: null},
				{title: "Remove", icon: "remove.png", click: function(){tool_obj.confirmRemoveFromSite(tool_obj);return false;}, selectRequired: "selectSites"}
			],
			toolItemTableElementId: "roster_sites_table",
			headers:
			[
				{title: " ", type: "center", sort: false},
				{title: " ", type: "center", sort: false},
				{title: "Role", type: null, sort: true},
				{title: "Name", type: null, sort: true},
				{title: "EID", type: null, sort: true},
				{title: "IID", type: null, sort: true},
				{title: "Roster", type: null, sort: true},
			],
			start: function(obj, mode)
			{
				obj.populateSite(obj);
			},
			reset: function(obj, mode)
			{
				obj.resetSites(obj);
				obj.populateSite(obj);
			},
			stop: function(obj)
			{
				obj.clearSitesTable(obj);
			}
		},
		{
			title: "Reports",
			elementId: "roster_reports",
			icon: "report.png",
			toolActionsElementId: "roster_reports_actions",
			actions:
			[
				{title: "Print", icon: "document_add.png", click: function(){tool_obj.printReports(tool_obj);return false;}, selectRequired: null},
				{title: "Export", icon: "import_export.png", click: function(){tool_obj.exportReports(tool_obj);return false;}, selectRequired: null}
			],
			toolItemTableElementId: "roster_reports_table",
			headers:
			[
				{title: " ", type: "center", sort: false},
				{title: "Site", type: null, sort: true},
				{title: "Visits", type: null, sort: true},
				{title: "Visitors", type: null, sort: true},
				{title: "", type: null, sort: false},
				{title: "", type: null, sort: false},
				{title: "", type: null, sort: false},
				{title: "", type: null, sort: false}
			],
			start: function(obj, mode)
			{
				obj.populateReports(obj);
			},
			reset: function(obj, mode)
			{
				obj.resetReports(obj);
				obj.populateReports(obj);
			},
			stop: function(obj)
			{
				obj.clearReportsTable(obj);
			}
		}
	],

	start: function(obj, data)
	{
		setTitle(obj.title);
		populateToolModes(obj);

		setupDialog("roster_addUser", "Add", function(){return obj.addUserToSite(obj);});

		$("#roster_process_files").unbind("click").click(function(){obj.confirmProcessFiles(obj);return false;});
		$("#roster_process_lines").unbind("click").click(function(){obj.confirmProcessLines(obj);return false;});
		$("#roster_sections_load").unbind("click").click(function(){obj.loadSections(obj);return false;});
		$("#roster_sections_filter").unbind('click').click(function(){obj.filterSections(obj); return false;});
		$("#roster_sites_loadSite").unbind("click").click(function(){obj.loadSite(obj);return false;});
		$("#roster_sections_organize_help").unbind("click").click(function(){openAlert("roster_sections_organize_help_alert");return false;});
		$("#roster_selectReport1").unbind('click').click(function(){obj.selectReport(obj);return true;});
		$("#roster_selectReport2").unbind('click').click(function(){obj.selectReport(obj);return true;});
		$("#roster_selectReport3").unbind('click').click(function(){obj.selectReport(obj);return true;});
		$('input:radio[name=roster_selectReport][value="1"]').prop('checked', true);
		$("#roster_reports_run").unbind('click').click(function(){obj.runReport(obj);return false;});
		obj.resetAdhoc(obj);
		obj.resetSections(obj);
		obj.resetSites(obj);
		obj.resetReports(obj);
		obj.loadStatus(obj);

		var sel = $("#roster_sections_term");
		$(sel).empty();
		$.each(obj.terms, function(index, term)
		{
			$(sel).append($("<option />", {value: term.code, text: term.name}));
		});

		sel = $("#roster_sections_client");
		$(sel).empty();
		$.each(obj.clients, function(index, term)
		{
			$(sel).append($("<option />", {value: term.code, text: term.name}));
		});

		var sel = $("#roster_reports_term");
		$(sel).empty();
		$.each(obj.terms, function(index, term)
		{
			$(sel).append($("<option />", {value: term.code, text: term.name}));
		});

		sel = $("#roster_reports_client");
		$(sel).empty();
		$.each(obj.clients, function(index, term)
		{
			$(sel).append($("<option />", {value: term.code, text: term.name}));
		});

		startHeartbeat();
	},

	stop: function(obj, save)
	{
		obj.clearStatusTimer(obj);
		stopHeartbeat();
	},
	
	reset: function(obj)
	{
		obj.currentMode.reset(obj, obj.currentMode);
		obj.loadStatus(obj);
	},

	clearStatusTimer: function(obj)
	{
		if (obj.statusTimer != null)
		{
			clearTimeout(obj.statusTimer);
			obj.statusTimer = null;
		}
	},

	loadStatus: function(obj)
	{
		obj.clearStatusTimer(obj);

		var params = new Object();
		if (obj.currentMode.title == "Files")
		{
			params.schedule = "1";
		}
		requestCdp("roster_status", params, function(data)
		{
			obj.populateStatus(obj, data.status);
			if (obj.currentMode.title == "Files")
			{
				obj.populateSchedule(obj, data.schedule);
				obj.populateFiles(obj, data.files);
			}

			obj.statusTimer = setTimeout(function(){obj.loadStatus(obj);}, 10000);
		});
	},

	populateStatus: function(obj, status)
	{
		$("#roster_status").empty();
		if (status != null)
		{
			$("#roster_status").text(status);
		}
	},

	loadSchedule: function(obj)
	{
		var params = new Object();
		requestCdp("roster_schedule", params, function(data)
		{
			obj.populateSchedule(obj, data.schedule);
			obj.populateFiles(obj, data.files);
		});
	},

	populateSchedule: function(obj, schedule)
	{
		$("#roster_schedule_table tbody").empty();
		if (schedule != null)
		{
			$.each(schedule, function(index, value)
			{
				var tr = $("<tr />");			
				$("#roster_schedule_table tbody").append(tr);
	
				createTextTd(tr, value.scheduled);
				createTextTd(tr, value.started);
				createTextTd(tr, value.ended);
				createTextTd(tr, value.reschedule);
			});
		}
		adjustForNewHeight();
	},

	populateFiles: function(obj, files)
	{
		$("#roster_files_list").empty();
		if (files != null)
		{
			$.each(files, function(index, value)
			{
				var li = $("<li />").text(value.name);
				$("#roster_files_list").append(li);
			});
		}
		adjustForNewHeight();
	},
	
	confirmProcessFiles: function(obj)
	{
		openConfirm("roster_confirmProcessFiles", "Process", function(){obj.processFiles(obj);});
	},

	processFiles: function(obj)
	{
		var params = new Object();
		requestCdp("roster_processFiles", params, function(data)
		{
			obj.loadStatus(obj);
		});
	},

	resetAdhoc: function(obj)
	{
		$("#roster_adhoc_instcode").val("");
		$("#roster_adhoc_lines").val(obj.instText);
		$("#roster_adhoc_createinstructor").attr('checked', false);
		$("#roster_adhoc_addtousersgroup").attr('checked', false);
		$("#roster_adhoc_noSections").attr('checked', false);
	},

	confirmProcessLines: function(obj)
	{
		openConfirm("roster_confirmProcessLines", "Process", function(){obj.processLines(obj);});
	},

	processLines: function(obj)
	{
		var params = new Object();
		params.instCode = $.trim($("#roster_adhoc_instcode").val());
		params.lines = $.trim($("#roster_adhoc_lines").val());
		
		if ($("#roster_adhoc_createinstructor").attr('checked'))
		{
			params.lines = obj.createInstructorDirective + params.lines;
		}
		if ($("#roster_adhoc_addtousersgroup").attr('checked'))
		{
			params.lines = obj.addToUsersGroupDirective + params.lines;
		}
		if ($("#roster_adhoc_noSections").attr('checked'))
		{
			params.lines = obj.noSectionsDirective + params.lines;
		}

		requestCdp("roster_processLines", params, function(data)
		{
			obj.resetAdhoc(obj);
			obj.loadStatus(obj);
		});
	},

	resetSections: function(obj)
	{
		$("#roster_sections_client").val("");
		$("#roster_sections_term").val("");
		obj.resetFilters(obj);
		$("#roster_addSectionSite_siteTitle").val("");
		$("#roster_addSectionSite_sectionTitle").val("");
		obj.sites = null;
		obj.client = null;
		obj.term = null;
		obj.totalCount = 0;
		clearSelectAll("selectSections");
		if ($("#roster_sections_table").hasClass("tablesorter")) $("#roster_sections_table").trigger("destroy");
		$("#roster_sections_table tbody").empty();
		$("#roster_sections_results").empty();
	},

	resetFilters: function(obj)
	{
		$("#roster_sections_site_filter").val("");
		obj.siteFilter = null;
		$("#roster_sections_section_filter").val("");
		obj.sectionFilter = null;
		$('input:radio[name=roster_sections_mode][value="0"]').prop('checked', true);
		obj.organization = 0;
		$("#roster_sections_filtered_results").empty();
	},

	loadSections: function(obj)
	{
		var params = new Object();
		params.client = $.trim($("#roster_sections_client").val());
		params.term = $.trim($("#roster_sections_term").val());
		$("#roster_sections_results").empty().text("Loading ...");
		requestCdp("roster_sections", params, function(data)
		{
			obj.sites = data.sites;
			obj.client = params.client;
			obj.term = params.term;
			obj.countSections(obj);
			obj.resetFilters(obj);
			obj.populateSections(obj);
		});
	},

	countSections: function(obj)
	{
		obj.totalCount = 0;
		if (obj.sites != null) $.each(obj.sites, function(index, siteValue)
		{
			$.each(siteValue.sections, function(index, sectionValue)
			{
				anySections = true;
				obj.totalCount++;
			});
		});
	},

	filterSections: function(obj)
	{
		obj.siteFilter = $.trim($("#roster_sections_site_filter").val().toLowerCase().replace(/\s+/g,' '));
		$("#roster_sections_site_filter").val(obj.siteFilter.toUpperCase());
		if (obj.siteFilter == "") obj.siteFilter = null;
		obj.sectionFilter = $.trim($("#roster_sections_section_filter").val().toLowerCase().replace(/\s+/g,' '));
		$("#roster_sections_section_filter").val(obj.sectionFilter.toUpperCase());
		if (obj.sectionFilter == "") obj.sectionFilter = null;
		obj.organization = $('input:radio[name=roster_sections_mode]:checked').val();
		$("#roster_sections_filtered_results").empty().text("Updating ...");
		obj.populateSections(obj);
	},

	fmtNull: function(value, ifNull)
	{
		if (value == null) return ifNull;
		return value.toString();
	},

	clearSectionsTable: function(obj)
	{
		clearSelectAll("selectSections");
		if ($("#roster_sections_table").hasClass("tablesorter")) $("#roster_sections_table").trigger("destroy");
		$("#roster_sections_table tbody").empty();
	},

	populateSections: function(obj)
	{
		clearSelectAll("selectSections");
		if ($("#roster_sections_table").hasClass("tablesorter")) $("#roster_sections_table").trigger("destroy");
		$("#roster_sections_table tbody").empty();
		$("#roster_sections_results").empty();
		$("#roster_sections_filtered_results").empty();
		$("#roster_sections_results").text(obj.totalCount.toString() + " sections found for " + obj.fmtNull(obj.client,"-") + " in " + obj.fmtNull(obj.term,"-"));

		if (obj.organization == 0)
		{
			obj.populateSectionsMapping(obj);
		}
		else if (obj.organization == 1)
		{
			obj.populateSectionsSites(obj);
		}
		else if (obj.organization == 2)
		{
			obj.populateSectionsSections(obj);
		}

		updateSelectStatus(obj, "selectSections");
		adjustForNewHeight();
	},

	populateSectionsMapping: function(obj)
	{
		$("#roster_sections_table > thead > tr > th:nth-child(3)").html("Site");
		$("#roster_sections_table > thead > tr > th:nth-child(4)").html("Section");

		var count = 0;
		if (obj.sites != null) $.each(obj.sites, function(index, site)
		{
			if ((obj.siteFilter != null) && (site.title.toLowerCase().indexOf(obj.siteFilter)) == -1) return;

			var anySections = false;
			$.each(site.sections, function(index, section)
			{
				anySections = true;

				if ((obj.sectionFilter != null) && (section.title.toLowerCase().indexOf(obj.sectionFilter)) == -1) return;

				var tr = $("<tr />");			
				$("#roster_sections_table tbody").append(tr);
				var td = createSelectCheckboxTd(obj, tr, "selectSections", site.siteId + "@" + section.title);
				$(td).find("input").attr("oid2", site.title);
				createIconTd(tr, "user_female.png", "View Roster", function(){obj.viewSiteRoster(obj, site.title);});
				createTextTd(tr, site.title, "width:100px;white-space:nowrap;");
				createTextTd(tr, section.title);
				count++;
			});

			if ((!anySections) && (obj.sectionFilter == null))
			{
				var tr = $("<tr />");			
				$("#roster_sections_table tbody").append(tr);
				createTextTd(tr, "");
				createIconTd(tr, "user_female.png", "View Roster", function(){obj.viewSiteRoster(obj, site.title);});
				createTextTd(tr, site.title, "width:100px;white-space:nowrap;");
				createTextTd(tr, "");
				count++;
			}
		});
		
		if (count == 0)
		{
			var tr = $("<tr />");			
			$("#roster_sections_table tbody").append(tr);
			createTextTd(tr, "");
			createTextTd(tr, "");
			createTextTd(tr, "no match", "font-style:italic;width:100px;white-space:nowrap;");
			createTextTd(tr, "");
		}

		$("#roster_sections_table").tablesorter(
		{
			headers:{0:{sorter:false},1:{sorter:false},2:{sorter:"text"},3:{sorter:"text"}},
			sortList:[[2,0]],
			emptyTo:"zero"
		});

		obj.populateFilteredResults(obj, count, "  by mapping");
	},

	populateSectionsSites: function(obj)
	{
		$("#roster_sections_table > thead > tr > th:nth-child(3)").html("Site");
		$("#roster_sections_table > thead > tr > th:nth-child(4)").html("Sections");

		var count = 0;
		if (obj.sites != null) $.each(obj.sites, function(index, site)
		{
			if ((obj.siteFilter != null) && (site.title.toLowerCase().indexOf(obj.siteFilter)) == -1) return;

			var sections = "";
			var sectionsFilteredOut = (obj.sectionFilter != null);
			$.each(site.sections, function(index, section)
			{
				if ((obj.sectionFilter != null) && (section.title.toLowerCase().indexOf(obj.sectionFilter)) != -1) sectionsFilteredOut = false;

				if (index != 0) sections = sections + "<span style='color:#9a661f;'> + </span>";
				sections = sections + section.title;
			});

			if (sectionsFilteredOut) return;

			var tr = $("<tr />");			
			$("#roster_sections_table tbody").append(tr);
			if (sections == "")
			{
				createTextTd(tr, "");
			}
			else
			{
				var td = createSelectCheckboxTd(obj, tr, "selectSections", site.siteId);
				$(td).find("input").attr("oid2", site.title);
			}
			createIconTd(tr, "user_female.png", "View Roster", function(){obj.viewSiteRoster(obj, site.title);});
			createTextTd(tr, site.title, "width:100px;white-space:nowrap;");
			createHtmlTd(tr, sections);
			count++;
		});

		if (count == 0)
		{
			var tr = $("<tr />");			
			$("#roster_sections_table tbody").append(tr);
			createTextTd(tr, "");
			createTextTd(tr, "");
			createTextTd(tr, "no match", "font-style:italic;width:100px;white-space:nowrap;");
			createTextTd(tr, "");
		}

		$("#roster_sections_table").tablesorter(
		{
			headers:{0:{sorter:false},1:{sorter:false},2:{sorter:"text"},3:{sorter:false}},
			sortList:[[2,0]],
			emptyTo:"zero"
		});

		obj.populateFilteredResults(obj, count, "  by site");
	},

	populateSectionsSections: function(obj)
	{
		$("#roster_sections_table > thead > tr > th:nth-child(3)").html("Section");
		$("#roster_sections_table > thead > tr > th:nth-child(4)").html("Sites");

		var count = 0;
		var sections = new Object();
		if (obj.sites != null) $.each(obj.sites, function(index, site)
		{
			$.each(site.sections, function(index, section)
			{
				if ((obj.sectionFilter != null) && (section.title.toLowerCase().indexOf(obj.sectionFilter)) == -1) return;

				if (section.title in sections)
				{
					sections[section.title] += "<span style='color:#9a661f;'> + </span>" + site.title;
				}
				else
				{
					sections[section.title] = site.title;
					anySections = true;
				}
			});
		});

		for (var key in sections)
		{
			if (sections.hasOwnProperty(key))
			{
				if ((obj.siteFilter != null) && (sections[key].toLowerCase().indexOf(obj.siteFilter)) == -1) continue;

				var tr = $("<tr />");			
				$("#roster_sections_table tbody").append(tr);
				var td = createSelectCheckboxTd(obj, tr, "selectSections", key);
				$(td).find("input").attr("oid2", key);
				createTextTd(tr, "");
				createTextTd(tr, key, "width:100px;white-space:nowrap;");
				createHtmlTd(tr, sections[key]);
				count++;
			}	
		}

		if (count == 0)
		{
			var tr = $("<tr />");			
			$("#roster_sections_table tbody").append(tr);
			createTextTd(tr, "");
			createTextTd(tr, "");
			createTextTd(tr, "no match", "font-style:italic;width:100px;white-space:nowrap;");
			createTextTd(tr, "");
		}

		$("#roster_sections_table").tablesorter(
		{
			headers:{0:{sorter:false},1:{sorter:false},2:{sorter:"text"},3:{sorter:false}},
			sortList:[[2,0]],
			emptyTo:"zero"
		});

		obj.populateFilteredResults(obj, count, "  by section");
	},

	populateFilteredResults: function(obj, count, tag)
	{
		var value =  count.toString() + " results displayed, organized by " + tag + ", filtered by ";
		if (obj.siteFilter == null)
		{
			value += "*";
		}
		else
		{
			value += obj.siteFilter;
		}
		value += " / ";
		if (obj.sectionFilter == null)
		{
			value += "*";
		}
		else
		{
			value += obj.sectionFilter;
		}
		$("#roster_sections_filtered_results").text(value);
	},

	confirmRemoveSections: function(obj)
	{
		if (obj.organization == 0)
		{
			openConfirm("roster_confirmRemoveSectionsMapping", "Remove", function(){obj.removeSectionsMapping(obj);});			
		}
		else if (obj.organization == 1)
		{
			openConfirm("roster_confirmRemoveSectionsSites", "Remove", function(){obj.removeSectionsSites(obj);});			
		}
		else if (obj.organization == 2)
		{
			openConfirm("roster_confirmRemoveSectionsSections", "Remove", function(){obj.removeSectionsSections(obj);});			
		}
	},

	removeSectionsMapping: function(obj)
	{
		var params = new Object();
		params.ids = collectSelectedOids("selectSections");
		params.client = obj.client;
		params.term = obj.term;
		$("#roster_sections_results").empty().text("Loading ...");
		requestCdp("roster_removeSectionsMapping", params, function(data)
		{
			obj.sites = data.sites;
			obj.countSections(obj);
			obj.populateSections(obj);
			obj.openResultsAlert(obj, data, "Selected section(s) were removed from the selected site(s)","No sections were removed.");
		});
	},

	removeSectionsSites: function(obj)
	{
		var params = new Object();
		params.ids = collectSelectedOids("selectSections");
		params.client = obj.client;
		params.term = obj.term;
		$("#roster_sections_results").empty().text("Loading ...");
		requestCdp("roster_removeSectionsSites", params, function(data)
		{
			obj.sites = data.sites;
			obj.countSections(obj);
			obj.populateSections(obj);
			obj.openResultsAlert(obj, data, "All sections were removed from the selected site(s).","No sections were removed.");
		});
	},

	removeSectionsSections: function(obj)
	{
		var params = new Object();
		params.ids = collectSelectedOids("selectSections");
		params.client = obj.client;
		params.term = obj.term;
		$("#roster_sections_results").empty().text("Loading ...");
		requestCdp("roster_removeSectionsSections", params, function(data)
		{
			obj.sites = data.sites;
			obj.countSections(obj);
			obj.populateSections(obj);
			obj.openResultsAlert(obj, data, "Selected sections(s) were removed from all sites.","No sections were removed.");
		});
	},

	confirmAddSectionSite: function(obj)
	{
		var sel = collectSelectedAttrArray("selectSections", "oid2");
		if (sel.length > 0)
		{
			if ((obj.organization == 0) || (obj.organization == 1))
			{
				// pick up one selected site
				$("#roster_addSectionSite_siteTitle").val(sel[0]);
			}
			else if (obj.organization == 2)
			{
				// pick up one selected section
				$("#roster_addSectionSite_sectionTitle").val(sel[0]);
			}
		}

		openConfirm("roster_confirmAddSectionSite", "Add", function(){obj.addSectionSite(obj);});
	},

	addSectionSite: function(obj)
	{
		var params = new Object();
		params.siteTitle = $.trim($("#roster_addSectionSite_siteTitle").val());
		params.sectionTitle = $.trim($("#roster_addSectionSite_sectionTitle").val());
		params.client = $.trim($("#roster_sections_client").val());
		params.term = $.trim($("#roster_sections_term").val());
		
		if (params.siteTitle == "" || params.sectionTitle == "")
		{
			openAlert("roster_alert_addSectionSite_params", function(){obj.confirmAddSectionSite(obj);});
			return false;
		}

		$("#roster_sections_results").empty().text("Loading ...");
		requestCdp("roster_addSectionSite", params, function(data)
		{
			obj.sites = data.sites;
			obj.countSections(obj);
			obj.populateSections(obj);
			obj.openResultsAlert(obj, data, "Section added to site.","Section not added: Site may not have been found.");
			$("#roster_addSectionSite_siteTitle").val("");
			$("#roster_addSectionSite_sectionTitle").val("");
		});
	},
	
	openResultsAlert: function(obj, data, good, bad)
	{
		if (data.success == "1")
		{
			$("#roster_alert_results_good_msg").empty().text(good);
			openAlert("roster_alert_results_good");
		}
		else
		{
			$("#roster_alert_results_bad_msg").empty().text(bad);
			openAlert("roster_alert_results_bad");
		}
	},

	viewSiteRoster: function(obj, siteTitle)
	{
		obj.resetSites(obj);
		$("#roster_sites_siteTitle").val(siteTitle);

		var params = new Object();
		params.siteTitle = siteTitle;
		requestCdp("roster_siteMembership", params, function(data)
		{
			obj.site = data.site;
			selectToolMode(3,obj);
		});
	},

	site: null,
	resetSites: function(obj)
	{
		obj.site = null;
		$("#roster_sites_siteTitle").val("");
		clearSelectAll("selectSites");
		if ($("#roster_sites_table").hasClass("tablesorter")) $("#roster_sites_table").trigger("destroy");
		$("#roster_sites_table tbody").empty();
	},

	loadSite: function(obj)
	{
		var params = new Object();
		params.siteTitle = $.trim($("#roster_sites_siteTitle").val());
		if (params.siteTitle == "")
		{
			openAlert("roster_sites_alertMissingTitle");
			return;
		}
		requestCdp("roster_siteMembership", params, function(data)
		{
			obj.site = data.site;
			obj.populateSite(obj);
		});
	},
	
	populateMembershipTable: function(obj, tableId, members, selectId, includeRoster)
	{
		$("#" + tableId + " tbody").empty();
		if (members != null) $.each(members, function(index, member)
		{
			var tr = $("<tr />");			
			$("#" + tableId + " tbody").append(tr);
			if ((selectId != null) && (member.roster == 1) && ("Student" != member.role) && ("Blocked" != member.role))
			{
				createSelectCheckboxTd(obj, tr, selectId, member.userId);
			}
			else
			{
				createTextTd(tr, "");	
			}
			if (member.active == 1)
			{
				createIconTd(tr, "add.png", "Active");
			}
			else
			{
				createIconTd(tr, "remove.png", "Inactive");
			}
			createTextTd(tr, (member.role == "Teaching Assistant" ? "TA" : member.role), "width:100px;white-space:nowrap;");
			createTextTd(tr, member.sortName, "width:300px;");
			createTextTd(tr, member.eid, "width:100px;white-space:nowrap;");
			createTextTd(tr, member.iid);
			if (includeRoster)
			{
				if (member.roster == 1)
				{
					createIconTd(tr, "tick.png", "From Roster");
				}
				else
				{
					createTextTd(tr, "");	
				}
			}
		});
		if (selectId != null) updateSelectStatus(obj, selectId);
	},

	clearSitesTable: function(obj)
	{
		clearSelectAll("selectSites");
		if ($("#roster_sites_table").hasClass("tablesorter")) $("#roster_sites_table").trigger("destroy");
		$("#roster_sites_table tbody").empty();
	},

	populateSite: function(obj)
	{
		clearSelectAll("selectSites");
		if ($("#roster_sites_table").hasClass("tablesorter")) $("#roster_sites_table").trigger("destroy");
		obj.populateMembershipTable(obj, "roster_sites_table", (obj.site != null) ? obj.site.members : null, "selectSites", true);
		$("#roster_sites_displayingTitle").empty();
		$("#roster_sites_displayingId").empty();
		$("#roster_sites_visit").unbind('click').addClass("e3_offstage");
		if (obj.site != null)
		{
			$("#roster_sites_displayingTitle").text(obj.site.title);
			$("#roster_sites_displayingId").text(obj.site.siteId);
			$("#roster_sites_siteTitle").val(obj.site.title);
			$("#roster_sites_visit").unbind('click').click(function(){openSite(obj.site.siteId);return false;});
			$("#roster_sites_visit").removeClass("e3_offstage");
		}

		$("#roster_sites_table").tablesorter(
		{
			headers:{0:{sorter:false},1:{sorter:false},2:{sorter:"text"},3:{sorter:"text"},4:{sorter:"text"},5:{sorter:"text"},6:{sorter:false}},
			sortList:[[3,0]],
			emptyTo:"zero"
		});
		
		var sectionsDiv = $("#roster_sites_sections");
		$(sectionsDiv).empty();

		if ((obj.site != null) && (obj.site.sections != null)) $.each(obj.site.sections, function(index, section)
		{
			var idBase = "roster_sites_section_" + section.sectionId;
			$(sectionsDiv).append(
				'<div class="e3_configurationFeature">' +
					'<div class="e3_configurationFeature_frame">' +
						'<div class="e3_configurationFeature_title">' +
							'Section Roster: ' + section.title +
						'</div>' +
					'</div>' +
					'<div class="e3_configurationFeature_body">' +
						'<div class="e3_tool_items">' +
							'<table id="' + idBase + '_table' + '" class="e3_tool_item_table e3_sortable_table">' +
								'<thead />' +
								'<tbody />' +
							'</table>' +
						'</div>' +
					'</div>' +
					'</div>' +
				'</div>');
			obj.populateMembershipTable(obj, idBase + "_table", section.members, null, false);
		});

		adjustForNewHeight();
	},

	confirmRemoveFromSite: function(obj)
	{
		openConfirm("roster_confirmRemoveUserFromSite", "Remove", function(){obj.removeFromSite(obj);});			
	},

	removeFromSite: function(obj)
	{
		var params = new Object();
		params.ids = collectSelectedOids("selectSites");
		params.siteId = obj.site.siteId;
		requestCdp("roster_removeUserFromSite", params, function(data)
		{
			obj.site = data.site;
			obj.populateSite(obj);
		});		
	},

	confirmAddToSite: function(obj)
	{
		$("#roster_addUser_identifiers").val("");
		// $('#roster_assignRoleStudent').prop("checked", true);
		$("#roster_addUser").dialog('open');
	},

	addUserToSite: function(obj)
	{
		// get the info
		var params = new Object();
		params.identifiers = $('#roster_addUser_identifiers').val();
		params.role = $('input:radio[name=assignRole]:checked').val();
		params.siteId = obj.site.siteId;
		requestCdp("roster_addMembers", params, function(data)
		{
			obj.site = data.site;
			obj.populateSite(obj);
		});
		return true;
	},

	report: null,
	resetReports: function(obj)
	{
		obj.report = null;
		$("#roster_reports_client").val("");
		$("#roster_reports_term").val("");
		$('input:radio[name=roster_selectReport][value="1"]').prop('checked', true);
		obj.clearReportsTable(obj);
		$("#roster_reports_results").empty();
	},

	clearReportsTable: function(obj)
	{
		if ($("#roster_reports_table").hasClass("tablesorter")) $("#roster_reports_table").trigger("destroy");
		$("#roster_reports_table tbody").empty();
	},

	selectReport: function(obj)
	{
		var source = $('input:radio[name=roster_selectReport]:checked').val();
		if (("1" == source) || ("3" == source))
		{
			$("#roster_reports_term_ui").removeClass("e3_offstage");
		}
		else
		{
			$("#roster_reports_term_ui").addClass("e3_offstage");
		}
	},

	runReport: function(obj)
	{
		var params = new Object();
		params.report =  $('input:radio[name=roster_selectReport]:checked').val();
		params.client = $.trim($("#roster_reports_client").val());
		params.term = $.trim($("#roster_reports_term").val());
		$("#roster_reports_results").empty().text("Loading ...");
		requestCdp("roster_report", params, function(data)
		{
			obj.report = data.report;
			obj.populateReports(obj);
		});
	},

	populateReports: function(obj)
	{
		if ($("#roster_reports_table").hasClass("tablesorter")) $("#roster_reports_table").trigger("destroy");
		$("#roster_reports_table tbody").empty();
		$("#roster_reports_results").empty();

		if (obj.report != null)
		{
			if (parseInt(obj.report.report) == 1)
			{
				obj.populateReports1(obj);
			}
			else if (parseInt(obj.report.report) == 2)
			{
				obj.populateReports2(obj);
			}
			else if (parseInt(obj.report.report) == 3)
			{
				obj.populateReports3(obj);
			}
		}

		adjustForNewHeight();
	},

	printReports: function(obj)
	{
		if (obj.report != null)
		{
			if (parseInt(obj.report.report) == 1)
			{
				obj.printReports1(obj);
			}
			else if (parseInt(obj.report.report) == 2)
			{
				obj.printReports2(obj);
			}
			else if (parseInt(obj.report.report) == 3)
			{
				obj.printReports3(obj);
			}
		}
	},

	exportReports: function(obj)
	{
		if (obj.report != null)
		{
			if (parseInt(obj.report.report) == 1)
			{
				obj.exportReports1(obj);
			}
			else if (parseInt(obj.report.report) == 2)
			{
				obj.exportReports2(obj);
			}
			else if (parseInt(obj.report.report) == 3)
			{
				obj.exportReports3(obj);
			}
		}
	},

	sortTerm: function(obj, term, code)
	{
		return '<span style="display:none;">' + code + '</span>' + term; 
	},

	unsortTerm: function(obj, text)
	{
		return text.substring(text.indexOf("/span>") + 6, text.length);
	},

	populateReports1: function(obj)
	{
		$("#roster_reports_table > thead > tr > th:nth-child(2)").html("Site");
		$("#roster_reports_table > thead > tr > th:nth-child(3)").html("Visits");
		$("#roster_reports_table > thead > tr > th:nth-child(4)").html("Visitors");
		$("#roster_reports_table > thead > tr > th:nth-child(5)").html("");
		$("#roster_reports_table > thead > tr > th:nth-child(6)").html("");
		$("#roster_reports_table > thead > tr > th:nth-child(7)").html("");

		$("#roster_reports_results").html(obj.report.title + " for " + obj.report.client + ", "
				+ obj.report.term + "<br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Number of Sites: " + obj.report.numberSites + "<br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Number Unused: " + obj.report.numberUnused);

		if (obj.report.results != null) $.each(obj.report.results, function(index, line)
		{
			var tr = $("<tr />");
			if (parseInt(line.visitors) == 0)
			{
				$(tr).addClass("siteVisitAlert");
			}
			else if (parseInt(line.visitors) == 1)
			{
				$(tr).addClass("siteVisitWarning");
			}
			$("#roster_reports_table tbody").append(tr);
			createIconTd(tr, "user_female.png", "View Roster", function(){obj.viewSiteRoster(obj, line.siteTitle);});
			createTextTd(tr, line.siteTitle, "width:1px;white-space:nowrap;");
			createTextTd(tr, line.visits, "width:1px;white-space:nowrap;text-align:center");
			createTextTd(tr, line.visitors, "width:1px;white-space:nowrap;text-align:center");
			createTextTd(tr,"");
			createTextTd(tr,"");
			createTextTd(tr,"");
			createTextTd(tr,"");
		});

		$("#roster_reports_table").tablesorter(
		{
			headers:{0:{sorter:false},1:{sorter:"text"},2:{sorter:"numeric"},3:{sorter:"numeric"},4:{sorter:false},5:{sorter:false},6:{sorter:false},7:{sorter:false}},
			sortList:[[1,0]],
			emptyTo:"zero"
		});
	},
	
	printReports1: function(obj)
	{
		var w = window.open();
		w.document.title = obj.report.title + " for " + obj.report.client + ", " + obj.report.term;

		var div = $("<div />");
		$(w.document.body).append(div);
		$(div).append(obj.report.title + " for " + obj.report.client + ", " + obj.report.term);
		$(div).css("margin-bottom","20px");

		div = $("<div />");
		$(w.document.body).append(div);
		$(div).append("<br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Number of Sites: " + obj.report.numberSites + "<br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Number Unused: " + obj.report.numberUnused);
		$(div).css("margin-bottom","20px");

		if (obj.report.results != null)
		{
			var table = $("<table />");
			$(w.document.body).append(table);
			$(table).append("<thead><tr><th>Site</th><th>Visits</th><th>Visitors</th></tr></thead>");
			var body = $("<tbody />");
			$(table).append(body);
			$.each($("#roster_reports_table > tbody > tr"), function(index, line)
			{
				var tr = $("<tr />");
				$(body).append(tr);
				createTextTd(tr, $(line).find("td:nth-child(2)").html(), "width:1px;white-space:nowrap;");
				createTextTd(tr, $(line).find("td:nth-child(3)").html(), "width:1px;white-space:nowrap;text-align:center");
				createTextTd(tr, $(line).find("td:nth-child(4)").html(), "width:1px;white-space:nowrap;text-align:center");
				createTextTd(tr,"");
			});
		}
		
		w.print();
		w.close();
	},

	exportReports1: function(obj)
	{
		var csv = "data:text/csv;charset=utf-8,";
		csv += "\"" + obj.report.title + " for " + obj.report.client + ", " + obj.report.term + "\"\n\n";
		csv +="Number of Sites," + obj.report.numberSites + "\nNumber Unused," + obj.report.numberUnused + "\n\n";
		csv += "Site,Visits,Visitors\n";
		if (obj.report.results != null)
		{
			$.each($("#roster_reports_table > tbody > tr"), function(index, line)
			{
				csv += $(line).find("td:nth-child(2)").html() + "," + $(line).find("td:nth-child(3)").html() + "," + $(line).find("td:nth-child(4)").html() + "\n";
			});
		}
		
		var encoded = encodeURI(csv);
		var title = obj.report.title + " for " + obj.report.client + " " + obj.report.term + ".csv";

		var link = $("#export_link");
		$(link).attr({"href": encoded, "download": title, "target": "_blank"});
		link[0].click();
	},

	populateReports2: function(obj)
	{
		$("#roster_reports_table > thead > tr > th:nth-child(2)").html("Term");
		$("#roster_reports_table > thead > tr > th:nth-child(3)").html("Seats");
		$("#roster_reports_table > thead > tr > th:nth-child(4)").html("Guests");
		$("#roster_reports_table > thead > tr > th:nth-child(5)").html("Non-Students");
		$("#roster_reports_table > thead > tr > th:nth-child(6)").html("Sites");
		$("#roster_reports_table > thead > tr > th:nth-child(7)").html("Sections");

		$("#roster_reports_results").html(obj.report.title + " for " + obj.report.client);

		if (obj.report.results != null) $.each(obj.report.results, function(index, line)
		{
			var tr = $("<tr />");
			$("#roster_reports_table tbody").append(tr);
			createTextTd(tr,"");
			createHtmlTd(tr, obj.sortTerm(obj, line.term, line.termCode), "width:1px;white-space:nowrap;");
			createTextTd(tr, line.seats, "width:1px;white-space:nowrap;text-align:center");
			createTextTd(tr, line.guests, "width:1px;white-space:nowrap;text-align:center");
			createTextTd(tr, line.others, "width:1px;white-space:nowrap;text-align:center");
			createTextTd(tr, line.sites, "width:1px;white-space:nowrap;text-align:center");
			createTextTd(tr, line.sections, "width:1px;white-space:nowrap;text-align:center");
			createTextTd(tr,"");
		});

		$("#roster_reports_table").tablesorter(
		{
			headers:{0:{sorter:false},1:{sorter:"text"},2:{sorter:"numeric"},3:{sorter:"numeric"},4:{sorter:"numeric"},5:{sorter:"numeric"},6:{sorter:"numeric"},7:{sorter:false}},
			sortList:[[2,1]],
			emptyTo:"zero"
		});
	},

	printReports2: function(obj)
	{
		var w = window.open();
		w.document.title = obj.report.title + " for " + obj.report.client;

		var div = $("<div />");
		$(w.document.body).append(div);
		$(div).append(obj.report.title + " for " + obj.report.client);
		$(div).css("margin-bottom","20px");

		if (obj.report.results != null)
		{
			var table = $("<table />");
			$(w.document.body).append(table);
			$(table).append("<thead><tr><th>Term</th><th>Seats</th><th>Guests</th><th>Non-Students</th><th>Sites</th><th>Sections</th></tr></thead>");
			var body = $("<tbody />");
			$(table).append(body);
			$.each($("#roster_reports_table > tbody > tr"), function(index, line)
			{
				var tr = $("<tr />");
				$(body).append(tr);
				createHtmlTd(tr, $(line).find("td:nth-child(2)").html(), "width:1px;white-space:nowrap;");
				createTextTd(tr, $(line).find("td:nth-child(3)").html(), "width:1px;white-space:nowrap;text-align:center");
				createTextTd(tr, $(line).find("td:nth-child(4)").html(), "width:1px;white-space:nowrap;text-align:center");
				createTextTd(tr, $(line).find("td:nth-child(5)").html(), "width:1px;white-space:nowrap;text-align:center");
				createTextTd(tr, $(line).find("td:nth-child(6)").html(), "width:1px;white-space:nowrap;text-align:center");
				createTextTd(tr, $(line).find("td:nth-child(7)").html(), "width:1px;white-space:nowrap;text-align:center");
				createTextTd(tr,"");
			});
		}
		
		w.print();
		w.close();
	},

	exportReports2: function(obj)
	{
		var csv = "data:text/csv;charset=utf-8," + obj.report.title + " for " + obj.report.client + "\n\nTerm,Seats,Guests,Non-Students,Sites,Sections\n";

		if (obj.report.results != null)
		{
			$.each($("#roster_reports_table > tbody > tr"), function(index, line)
			{
				csv += obj.unsortTerm(obj, $(line).find("td:nth-child(2)").html()) + "," + $(line).find("td:nth-child(3)").html() + "," + $(line).find("td:nth-child(4)").html()
					+ "," + $(line).find("td:nth-child(5)").html()+ "," + $(line).find("td:nth-child(6)").html() + "," + $(line).find("td:nth-child(7)").html() + "\n";
			});
		}
		
		var encoded = encodeURI(csv);
		var title = obj.report.title + " for " + obj.report.client + ".csv";

		var link = $("#export_link");
		$(link).attr({"href": encoded, "download": title, "target": "_blank"});
		link[0].click();
	},

	populateReports3: function(obj)
	{
		$("#roster_reports_table > thead > tr > th:nth-child(2)").html("Site");
		$("#roster_reports_table > thead > tr > th:nth-child(3)").html("Seats");
		$("#roster_reports_table > thead > tr > th:nth-child(4)").html("Guests");
		$("#roster_reports_table > thead > tr > th:nth-child(5)").html("Non-Students");
		$("#roster_reports_table > thead > tr > th:nth-child(6)").html("");
		$("#roster_reports_table > thead > tr > th:nth-child(7)").html("");

		$("#roster_reports_results").html(obj.report.title + " for " + obj.report.client + ", " + obj.report.term);

		if (obj.report.results != null) $.each(obj.report.results, function(index, line)
		{
			var tr = $("<tr />");
			$("#roster_reports_table tbody").append(tr);
			createIconTd(tr, "user_female.png", "View Roster", function(){obj.viewSiteRoster(obj, line.siteTitle);});
			createHtmlTd(tr, line.siteTitle, "width:1px;white-space:nowrap;");
			createTextTd(tr, line.seats, "width:1px;white-space:nowrap;text-align:center");
			createTextTd(tr, line.guests, "width:1px;white-space:nowrap;text-align:center");
			createTextTd(tr, line.others, "width:1px;white-space:nowrap;text-align:center");
			createTextTd(tr, "");
			createTextTd(tr, "");
			createTextTd(tr,"");
		});

		$("#roster_reports_table").tablesorter(
		{
			headers:{0:{sorter:false},1:{sorter:"text"},2:{sorter:"numeric"},3:{sorter:"numeric"},4:{sorter:"numeric"},5:{sorter:false},6:{sorter:false},7:{sorter:false}},
			sortList:[[1,0]],
			emptyTo:"zero"
		});
	},

	printReports3: function(obj)
	{
		var w = window.open();
		w.document.title = obj.report.title + " for " + obj.report.client + ", " + obj.report.term;

		var div = $("<div />");
		$(w.document.body).append(div);
		$(div).append(obj.report.title + " for " + obj.report.client + ", " + obj.report.term);
		$(div).css("margin-bottom","20px");

		if (obj.report.results != null)
		{
			var table = $("<table />");
			$(w.document.body).append(table);
			$(table).append("<thead><tr><th>Site</th><th>Seats</th><th>Guests</th><th>Non-Students</th></tr></thead>");
			var body = $("<tbody />");
			$(table).append(body);
			$.each($("#roster_reports_table > tbody > tr"), function(index, line)
			{
				var tr = $("<tr />");
				$(body).append(tr);
				createHtmlTd(tr, $(line).find("td:nth-child(2)").html(), "width:1px;white-space:nowrap;");
				createTextTd(tr, $(line).find("td:nth-child(3)").html(), "width:1px;white-space:nowrap;text-align:center");
				createTextTd(tr, $(line).find("td:nth-child(4)").html(), "width:1px;white-space:nowrap;text-align:center");
				createTextTd(tr, $(line).find("td:nth-child(5)").html(), "width:1px;white-space:nowrap;text-align:center");
				createTextTd(tr,"");
			});
		}
		
		w.print();
		w.close();
	},

	exportReports3: function(obj)
	{
		var header = "data:text/csv;charset=utf-8,\"" + obj.report.title + " for " + obj.report.client + ", " + obj.report.term + "\"\n\nSite,Seats,Guests,Non-Students\n";
		var body = "";
		var totals = "";

		if (obj.report.results != null)
		{
			var count = 0;
			$.each($("#roster_reports_table > tbody > tr"), function(index, line)
			{
				body += $(line).find("td:nth-child(2)").html() + "," + $(line).find("td:nth-child(3)").html() + "," + $(line).find("td:nth-child(4)").html()
					+ "," + $(line).find("td:nth-child(5)").html() + "\n";
				count++;
			});
			totals = "Total,=SUM(B5:B" + (count+4) + "),=SUM(C5:C" + (count+4) + "),=SUM(D5:D" + (count+4) + ")\n";
		}

		var csv = header + totals + body;
		var encoded = encodeURI(csv);
		var title = obj.report.title + " for " + obj.report.client + " " + obj.report.term + ".csv";

		var link = $("#export_link");
		$(link).attr({"href": encoded, "download": title, "target": "_blank"});
		link[0].click();
	}
};

completeToolLoad();
