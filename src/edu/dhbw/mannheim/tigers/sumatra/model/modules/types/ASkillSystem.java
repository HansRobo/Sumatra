/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s):
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.AModule;
import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.StartModuleException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AresData;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderThread;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillSystemObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.GeneralPurposeTimer;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;


/**
 * The base class for every implementation of a skill system
 * 
 * @author Gero
 */
public abstract class ASkillSystem extends AModule implements IEmergencyStop
{
	private static final Logger									log						= Logger.getLogger(ASkillSystem.class
																												.getName());
	
	/** */
	public static final String										MODULE_TYPE				= "AMoveSystem";
	/** */
	public static final String										MODULE_ID				= "skillsystem";
	
	private final Set<ISkillSystemObserver>					observers				= Collections
																												.newSetFromMap(new ConcurrentHashMap<ISkillSystemObserver, Boolean>());
	
	private static final Map<ETeamColor, ExecutorService>	THREAD_POOL				= new HashMap<>();
	private static ExecutorService								defaultService			= null;
	
	private static final int										NUM_THREADS_PER_TEAM	= 10;
	
	
	/**
	 * @param botId
	 * @param skill
	 */
	public abstract void execute(BotID botId, ISkill skill);
	
	
	/**
	 * @param botId
	 */
	public abstract void reset(final BotID botId);
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void deinitModule()
	{
		observers.clear();
	}
	
	
	/**
	 * Reset skill system aka delete roles from given ai color.
	 * Used after crash in AI
	 * 
	 * @param color
	 */
	public void reset(final ETeamColor color)
	{
		List<ISkillSystemObserver> copy = new ArrayList<ISkillSystemObserver>(observers);
		for (ISkillSystemObserver o : copy)
		{
			if (o instanceof ARole)
			{
				ARole role = (ARole) o;
				if (role.getBotID().getTeamColor() == color)
				{
					observers.remove(o);
				}
			}
		}
	}
	
	
	/**
	 */
	protected void checkObserversCleared()
	{
		// delay the check, so that the skillexecuter has time to complete its skills
		GeneralPurposeTimer.getInstance().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				if (!observers.isEmpty())
				{
					log.warn("There are still observers left: " + observers);
				}
				observers.clear();
			}
		}, 1000);
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final ISkillSystemObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ISkillSystemObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	protected void notifySkillStarted(final ISkill skill, final BotID botId)
	{
		synchronized (observers)
		{
			for (final ISkillSystemObserver observer : observers)
			{
				observer.onSkillStarted(skill, botId);
			}
		}
	}
	
	
	protected void notifySkillCompleted(final ISkill skill, final BotID botId)
	{
		synchronized (observers)
		{
			for (final ISkillSystemObserver observer : observers)
			{
				observer.onSkillCompleted(skill, botId);
			}
		}
	}
	
	
	/**
	 * @return
	 */
	public abstract AresData getLatestAresData();
	
	
	/**
	 * @return
	 */
	public abstract PathFinderThread getPathFinderScheduler();
	
	
	@Override
	public void emergencyStop()
	{
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		THREAD_POOL.put(ETeamColor.YELLOW,
				Executors.newFixedThreadPool(NUM_THREADS_PER_TEAM, new NamedThreadFactory("SkillThreadPool_YELLOW")));
		THREAD_POOL.put(ETeamColor.BLUE,
				Executors.newFixedThreadPool(NUM_THREADS_PER_TEAM, new NamedThreadFactory("SkillThreadPool_BLUE")));
	}
	
	
	@Override
	public void stopModule()
	{
		for (ExecutorService es : THREAD_POOL.values())
		{
			es.shutdown();
		}
		THREAD_POOL.clear();
	}
	
	
	/**
	 * @param color
	 * @return the threadPool
	 */
	public static ExecutorService getThreadPool(final ETeamColor color)
	{
		ExecutorService service = THREAD_POOL.get(color);
		if (service != null)
		{
			return service;
		}
		if (defaultService == null)
		{
			defaultService = Executors.newSingleThreadExecutor(new NamedThreadFactory("Default skill thread pool"));
		}
		return defaultService;
	}
}
