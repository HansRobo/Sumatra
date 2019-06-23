/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.observer;

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamGeometryFrame;
import edu.dhbw.mannheim.tigers.sumatra.util.observer.IObservable;
import edu.dhbw.mannheim.tigers.sumatra.util.observer.Observable;

/**
 * @see Observable
 * 
 * @author Gero
 */
public interface ICamGeomObservable extends IObservable<ICamGeomObservable, ICamGeomObserver, CamGeometryFrame>
{
	
}