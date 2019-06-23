/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 9, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test.PositionSkill;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Move to a given destination and orientation with PositionController
 * and make an epic shoot :P
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class PenaltyShootSkill extends PositionSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private EState					state									= EState.Prepositioning_1;
	
	/**
	 * This value adjusts the angle to shoot but to low or to high number
	 * will cause the skill to fail, so be very careful here.
	 */
	@Configurable(comment = "This value adjusts the angle to shoot but to low or to high number will cause the skill to fail, so be very careful here.")
	private static long			timeToShoot							= 50;
	
	@Configurable(comment = "dribbleSpeed")
	private static int			dribbleSpeed						= 0;
	
	@Configurable(comment = "correction Distance: distance to ball")
	private static float			correctionDist						= 0;
	
	@Configurable(comment = "speed to move closer to the ball, big numbers mean slower movement")
	private static long			stepTimeForSlowMove				= 150;
	
	private long					timeout								= 500;
	private long					time									= 0;
	private ERotateDirection	rotateDirection					= ERotateDirection.LEFT;
	
	private long					stepTimeCounter					= 0;
	
	@Configurable
	private static float			slowMoveDist						= 200;
	
	private float					slowMoveDistCounter				= 0;
	
	private boolean				ready									= false;
	private boolean				normalStartBeforePosReached	= false;
	
	private float					earlyNormalStartFix				= 0;
	
	
	/**
	 * @author MarkG
	 */
	public enum ERotateDirection
	{
		/**  */
		LEFT,
		/**  */
		RIGHT;
	}
	
	private enum EState
	{
		Prepositioning_1,
		Prepositioning_2,
		Turn;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Do not use this constructor, if you extend from this class
	 * 
	 * @param rotate
	 */
	public PenaltyShootSkill(final ERotateDirection rotate)
	{
		super(ESkillName.PENALTY_SHOOT);
		rotateDirection = rotate;
		slowMoveDistCounter = slowMoveDist;
	}
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		IVector2 destination = null;
		IVector2 orientation = new Vector2(0, 0);
		
		if (ready && (slowMoveDistCounter > 0))
		{
			earlyNormalStartFix = 20;
			normalStartBeforePosReached = true;
		}
		if (ready && (slowMoveDistCounter <= 0))
		{
			if (normalStartBeforePosReached)
			{
				if (slowMoveDistCounter <= (0 - earlyNormalStartFix))
				{
					state = EState.Turn;
				}
			}
			else
			{
				state = EState.Turn;
			}
		}
		
		switch (state)
		{
			case Prepositioning_1:
				destination = getWorldFrame().getBall().getPos().addNew(new Vector2(-200, 0));
				if (GeoMath.distancePP(destination, getPos()) < 30)
				{
					state = EState.Prepositioning_2;
				}
				orientation = getWorldFrame().getBall().getPos().subtractNew(getPos());
				break;
			case Prepositioning_2:
				if ((slowMoveDistCounter > (0 - earlyNormalStartFix))
						&& ((SumatraClock.currentTimeMillis() - stepTimeCounter) > stepTimeForSlowMove))
				{
					stepTimeCounter = SumatraClock.currentTimeMillis();
					slowMoveDistCounter = slowMoveDistCounter - 3;
				} else if (slowMoveDistCounter < (0 - earlyNormalStartFix))
				{
					slowMoveDistCounter = 0 - earlyNormalStartFix;
				}
				
				destination = getWorldFrame()
						.getBall()
						.getPos()
						.addNew(
								new Vector2(
										(-getBot().getCenter2DribblerDist()
												- AIConfig.getGeometry().getBallRadius() - correctionDist - slowMoveDistCounter), 0));
				orientation = getWorldFrame().getBall().getPos().subtractNew(getPos());
				
				
				break;
			case Turn:
				switch (rotateDirection)
				{
					case LEFT:
						orientation = getWorldFrame().getBall().getPos().addNew(new Vector2(0, 100)).subtractNew(getPos())
								.multiplyNew(-1);
						break;
					case RIGHT:
						orientation = getWorldFrame().getBall().getPos().addNew(new Vector2(0, -100)).subtractNew(getPos())
								.multiplyNew(-1);
						break;
				}
				destination = getPos().addNew(new Vector2(50, 0));
				if (time == 0)
				{
					time = SumatraClock.currentTimeMillis();
				} else if ((SumatraClock.currentTimeMillis() - time) > timeout)
				{
					complete();
					return;
				}
				else if ((SumatraClock.currentTimeMillis() - time) > timeToShoot)
				{
					getDevices().kickGeneralSpeed(cmds, EKickerMode.FORCE, EKickerDevice.STRAIGHT,
							8, dribbleSpeed);
				}
				break;
		}
		
		setDestination(destination);
		setOrientation(orientation.getAngle());
	}
	
	
	@Override
	public void doCalcEntryActions(final List<ACommand> cmds)
	{
		super.doCalcEntryActions(cmds);
		stepTimeCounter = SumatraClock.currentTimeMillis();
	}
	
	
	/**
	 * Skill will go on and Shoot.
	 */
	public void normalStartCalled()
	{
		ready = true;
	}
	
	
	/**
	 * change ShootDirection
	 * 
	 * @param rotate
	 */
	public void setShootDirection(final ERotateDirection rotate)
	{
		rotateDirection = rotate;
	}
}
