/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s):
 * Gero
 * Oliver Steinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;


/**
 * This class is used to assigned the needed roles of each play to a robot.
 * (@see {@link Lachesis#assignRoles(AthenaAiFrame)})
 * 
 * @author Gero, Oliver Steinbrecher <OST1988@aol.com>
 */
public class Lachesis
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IRoleAssigner	roleAssigner;
	
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public Lachesis()
	{
		// roleAssigner = new NewRoleAssigner();
		roleAssigner = new SimplifiedRoleAssigner();
	}
	
	
	/**
	 * <p>
	 * Takes the roles and assigns them. It also catches a lot of special cases (less roles then bots, less bots then
	 * roles, etc.) for debugging purposes.
	 * </p>
	 * 
	 * @param frame
	 */
	public final void assignRoles(final AthenaAiFrame frame)
	{
		final BotIDMap<TrackedTigerBot> assignees = new BotIDMap<TrackedTigerBot>(
				frame.getWorldFrame().tigerBotsAvailable);
		roleAssigner.assignRoles(assignees, frame.getPlayStrategy().getActivePlays(), frame);
	}
}
