/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.general.ADesiredBotCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;


/**
 * Find the desired bots for offense
 */
public class DesiredOffendersCalc extends ADesiredBotCalc
{
	private static Logger log = LogManager.getLogger(DesiredOffendersCalc.class);
	private boolean invalidNumberWarned = false;
	
	
	public DesiredOffendersCalc()
	{
		super(EPlay.OFFENSIVE);
	}
	
	
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		int numOffenders = tacticalField.getPlayNumbers().getOrDefault(EPlay.OFFENSIVE, 0);
		Set<BotID> desiredBots = tacticalField.getOffensiveStrategy().getDesiredBots().stream().limit(numOffenders)
				.collect(Collectors.toSet());
		if (desiredBots.size() != numOffenders && !invalidNumberWarned)
		{
			invalidNumberWarned = true;
			log.warn("Invalid number of offensive bots (allowed: " + numOffenders + ") actual: "
					+ Arrays.toString(desiredBots.toArray()));
		}
		addDesiredBots(desiredBots);
	}
}