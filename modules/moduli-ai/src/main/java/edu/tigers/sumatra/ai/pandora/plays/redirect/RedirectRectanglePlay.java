/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Redirect based on a desired angle. Only works with 4 roles
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectRectanglePlay extends ARedirectPlay
{
	@Configurable(comment = "Desired redirect angle in degree", defValue = "45.0")
	private static double angleDeg = 45;
	
	@Configurable(comment = "dist to center (radius) [mm]", defValue = "3000.0")
	private static double distance = 3000;
	
	
	public RedirectRectanglePlay()
	{
		super(EPlay.REDIRECT_ANGLE);
	}
	
	
	@Override
	protected List<IVector2> getFormation()
	{
		if (getRoles().size() == 4)
		{
			double y = distance;
			double angle = AngleMath.deg2rad(angleDeg);
			double x = y / SumatraMath.tan(angle);
			
			List<IVector2> dests = new ArrayList<>(4);
			if (angleDeg > 45)
			{
				dests.add(Vector2.fromXY(-x, -y));
				dests.add(Vector2.fromXY(-x, y));
				dests.add(Vector2.fromXY(x, -y));
				dests.add(Vector2.fromXY(x, y));
			} else
			{
				dests.add(Vector2.fromXY(x, -y));
				dests.add(Vector2.fromXY(x, y));
				dests.add(Vector2.fromXY(-x, -y));
				dests.add(Vector2.fromXY(-x, y));
			}
			return dests;
		}
		return new ArrayList<>();
	}
	
	
	@Override
	protected void getReceiveModes(final Map<ARole, EReceiveMode> modes)
	{
		// empty
	}
	
	
	@Override
	protected int getReceiverTarget(final int roleIdx)
	{
		switch (roleIdx)
		{
			case 0:
				return 2;
			case 1:
				return 3;
			case 2:
				return 1;
			case 3:
				return 0;
			default:
				return 0;
		}
	}
}