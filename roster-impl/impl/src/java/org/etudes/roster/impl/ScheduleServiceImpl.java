/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/roster/trunk/roster-impl/impl/src/java/org/etudes/roster/impl/ScheduleServiceImpl.java $
 * $Id: ScheduleServiceImpl.java 7408 2014-02-16 20:22:01Z ggolden $
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

package org.etudes.roster.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.roster.api.RunTime;
import org.etudes.roster.api.ScheduleEntry;
import org.etudes.roster.api.ScheduleService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.util.StringUtil;

/**
 * ScheduleServiceImpl implements ScheduleService
 */
public abstract class ScheduleServiceImpl implements ScheduleService
{
	protected class RunTimeImpl implements RunTime
	{
		/** The 0..23 hour of day. */
		protected int hour = 0;

		/** The 0..59 minute of hour. */
		protected int minute = 0;

		/**
		 * Construct
		 * 
		 * @param hour
		 *        The hour.
		 * @param minute
		 *        The minute.
		 */
		public RunTimeImpl(int hour, int minute)
		{
			this.hour = hour;
			this.minute = minute;
		}

		/**
		 * Construct from an HH:MM time string (24 hour format)
		 * 
		 * @param time
		 *        The time string.
		 */
		public RunTimeImpl(String time)
		{
			try
			{
				String[] parts = StringUtil.split(time, ":");
				this.hour = Integer.parseInt(parts[0]);
				this.minute = Integer.parseInt(parts[1]);
			}
			catch (NumberFormatException e)
			{
				M_log.warn("invalid time spec:" + time);
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				M_log.warn("invalid time spec:" + time);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public int getHour()
		{
			return this.hour;
		}

		/**
		 * {@inheritDoc}
		 */
		public int getMinute()
		{
			return this.minute;
		}

		/**
		 * Check if the other time matches this one, or is just after within a threshold.
		 * 
		 * @param other
		 *        The other time.
		 * @return true if satisfied, false if not.
		 */
		public boolean isSatisfiedBy(RunTime other)
		{
			// same hour
			if (other.getHour() == this.hour)
			{
				// if other is up to threshold minutes past this time
				int delta = other.getMinute() - this.minute;
				if ((delta >= 0) && (delta <= MINUTE_THRESHOLD))
				{
					return true;
				}
			}

			// if it is the next hour, it might still be within the threshold
			else if (other.getHour() == ((this.hour + 1) % 24))
			{
				int delta = (other.getMinute() + 60) - this.minute;
				if ((delta >= 0) && (delta <= MINUTE_THRESHOLD))
				{
					return true;
				}
			}

			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public String toString()
		{
			return (this.hour < 10 ? "0" : "") + Integer.toString(this.hour) + ":" + (this.minute < 10 ? "0" : "") + Integer.toString(this.minute);
		}
	}

	protected class ScheduleEntryImpl implements ScheduleEntry
	{
		protected Date ended = null;

		protected RunTime reschedule = null;

		protected RunTime runTime = null;

		protected Date started = null;

		/**
		 * Construct
		 * 
		 * @param time
		 *        The run time.
		 */
		public ScheduleEntryImpl(RunTime time)
		{
			this.runTime = time;
		}

		/**
		 * {@inheritDoc}
		 */
		public Date getEndedDate()
		{
			return this.ended;
		}

		/**
		 * {@inheritDoc}
		 */
		public RunTime getRescheduleTime()
		{
			return this.reschedule;
		}

		/**
		 * {@inheritDoc}
		 */
		public RunTime getScheduledTime()
		{
			return this.runTime;
		}

		/**
		 * {@inheritDoc}
		 */
		public Date getStartedDate()
		{
			return this.started;
		}

		/**
		 * {@inheritDoc}
		 */
		public String toString()
		{
			return "scheduled: " + this.runTime.toString() + (this.started != null ? (" started: " + this.started.toString()) : " : not started")
					+ (this.ended != null ? (" ended: " + this.ended.toString()) : "")
					+ (this.reschedule != null ? (" reschedule: " + this.reschedule.toString()) : "");
		}
	}

	/**
	 * Runnable class to check the time.
	 */
	protected class Scheduler implements Runnable
	{
		/**
		 * Run the scheduler thread, checking to see if it is time to process files.
		 */
		public void run()
		{
			// since we might be running while the component manager is still being created and populated,
			// such as at server startup, wait here for a complete component manager
			ComponentManager.waitTillConfigured();

			// loop till told to stop
			while ((!threadStop) && (!Thread.currentThread().isInterrupted()))
			{
				try
				{
					// what time of day is it?
					Calendar rightNow = Calendar.getInstance();
					RunTimeImpl now = new RunTimeImpl(rightNow.get(Calendar.HOUR_OF_DAY), rightNow.get(Calendar.MINUTE));
					if (M_log.isDebugEnabled()) M_log.debug("run: checking: now: " + now.toString());

					// check if any scheduled times need rescheduling
					for (ScheduleEntryImpl time : schedule)
					{
						if (time.getRescheduleTime() != null)
						{
							if (time.getRescheduleTime().isSatisfiedBy(now))
							{
								time.reschedule = null;
							}
						}
					}

					// if we are just past a scheduled run time, and are not running, run
					ScheduleEntryImpl runNow = null;
					for (ScheduleEntryImpl time : schedule)
					{
						// if we are set to reschedule, don't consider
						if (time.getRescheduleTime() == null)
						{
							// if it is not yet time, don't do it
							if (time.getScheduledTime().isSatisfiedBy(now))
							{
								runNow = time;

								if (M_log.isDebugEnabled()) M_log.debug("will run for scheduled time: " + time.toString());
								break;
							}
						}
					}

					if (runNow != null)
					{
						if (M_log.isDebugEnabled()) M_log.debug("run: time!");

						// record when we started
						runNow.started = new Date();

						// not yet ended
						runNow.ended = null;

						// set to reschedule soon
						rightNow.add(Calendar.MINUTE, 2 * MINUTE_THRESHOLD);
						runNow.reschedule = new RunTimeImpl(rightNow.get(Calendar.HOUR_OF_DAY), rightNow.get(Calendar.MINUTE));

						// start the task
						runTask(runNow);
					}
				}
				catch (Throwable e)
				{
					M_log.warn("run: will continue: ", e);
				}
				finally
				{
					// clear out any current current bindings
					threadLocalManager().clear();
				}

				// take a small nap
				try
				{
					Thread.sleep(schedulerCheckMs);
				}
				catch (Exception ignore)
				{
				}
			}
		}
	}

	protected static int MINUTE_THRESHOLD = 5;

	/** Our logger. */
	private static Log M_log = LogFactory.getLog(ScheduleServiceImpl.class);

	/** Times to run the roster files. */
	protected List<ScheduleEntryImpl> schedule = new ArrayList<ScheduleEntryImpl>();

	/** How long to wait (ms) between checks for when to process the roster files in the scheduler thread (1 minute). */
	protected long schedulerCheckMs = 1000L * 60L;

	/** The scheduler thread. */
	protected Thread schedulerThread = null;

	/** The task thread. */
	protected Thread task = null;

	/** The task status. */
	protected StringBuilder taskStatusBuf = new StringBuilder(1024);

	/** Semaphore for task data. */
	protected Object taskSync = new Object();

	/** The thread quit flag. */
	protected boolean threadStop = false;

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		// stop the checking thread
		stopScheduler();

		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ScheduleEntry> getSchedule()
	{
		List<ScheduleEntry> rv = new ArrayList<ScheduleEntry>();
		rv.addAll(this.schedule);
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTaskStatus()
	{
		synchronized (this.taskSync)
		{
			return this.taskStatusBuf.toString();
		}
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// read the server's id
			String id = serverConfigurationService().getServerId();
			if (id != null)
			{
				// read configuration for this server
				String key = getConfigKeyRoot() + "." + id;
				String[] runTimes = serverConfigurationService().getStrings(key);
				if (runTimes != null)
				{
					for (String time : runTimes)
					{
						this.schedule.add(new ScheduleEntryImpl(new RunTimeImpl(time)));
					}
				}
			}

			// start the SCHEDULER thread
			if (this.schedulerCheckMs > 0)
			{
				startScheduler();
			}

			M_log.info("init(): scheduler check seconds: " + this.schedulerCheckMs / 1000);
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isTaskRunning()
	{
		synchronized (this.taskSync)
		{
			return this.task != null;
		}
	}

	/**
	 * Set the # seconds to wait between db checks for timed-out submissions.
	 * 
	 * @param time
	 *        The # seconds to wait between db checks for timed-out submissions.
	 */
	public void setSchedulerCheckSeconds(String time)
	{
		this.schedulerCheckMs = Integer.parseInt(time) * 1000L;
	}

	/**
	 * @return The root of the key for finding the scheduled times from the config service. A "." and the server id will be appended.
	 */
	protected abstract String getConfigKeyRoot();

	/**
	 * Put out a message made up of some number of components, to the task status and log.
	 * 
	 * @param components
	 *        The message components
	 */
	protected void message(Object... components)
	{
		messageNoLog(components);

		// and log
		M_log.info(this.taskStatusBuf.toString());
	}

	/**
	 * Put out a message made up of some number of components, to the task status; add to what is there.
	 * 
	 * @param components
	 *        The message components
	 */
	protected void messageAppendNoLog(Object... components)
	{
		synchronized (this.taskSync)
		{
			// append each component
			for (Object o : components)
			{
				this.taskStatusBuf.append(o.toString());
			}
		}
	}

	/**
	 * Put out a message made up of some number of components, to the task status.
	 * 
	 * @param components
	 *        The message components
	 */
	protected void messageNoLog(Object... components)
	{
		synchronized (this.taskSync)
		{
			// start clean
			this.taskStatusBuf.setLength(0);

			// append each component
			for (Object o : components)
			{
				this.taskStatusBuf.append(o.toString());
			}
		}
	}

	/**
	 * Run the scheduled task now.
	 * 
	 * @param entry
	 *        The schedule entry for this run.
	 */
	protected abstract void runTask(ScheduleEntryImpl entry);

	/**
	 * Start the scheduler thread.
	 */
	protected void startScheduler()
	{
		threadStop = false;

		schedulerThread = new Thread(new Scheduler(), getClass().getName() + ".scheduler");
		schedulerThread.start();
	}

	/**
	 * Stop the scheduler thread.
	 */
	protected void stopScheduler()
	{
		if (schedulerThread == null) return;

		// signal the thread to stop
		threadStop = true;

		// wake up the thread
		schedulerThread.interrupt();

		schedulerThread = null;
	}

	/**
	 * @return The ServerConfigurationService, via the component manager.
	 */
	private ServerConfigurationService serverConfigurationService()
	{
		return (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
	}

	/**
	 * @return The ThreadLocalManager, via the component manager.
	 */
	private ThreadLocalManager threadLocalManager()
	{
		return (ThreadLocalManager) ComponentManager.get(ThreadLocalManager.class);
	}
}
