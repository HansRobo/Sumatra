/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;

import java.awt.Color;


/**
 * Move straight to the ball and get the ball onto the dribbler.
 */
public class GetBallContactSkill extends AMoveToSkill
{
	@Configurable(defValue = "300", comment = "Max dist [mm] to push towards an invisible ball")
	private static double maxApproachBallExtraDist = 300;

	@Configurable(comment = "How fast to accelerate when getting ball contact", defValue = "1.0")
	private static double getContactAcc = 1.0;

	@Configurable(comment = "Min contact time [s] to reach before coming to a stop", defValue = "0.2")
	private static double minContactTime = 0.2;

	@Configurable(comment = "Min contact time [s] to reach before succeeding", defValue = "0.4")
	private static double minSuccessContactTime = 0.4;

	private double cachedOrientation;
	private double approachBallExtraDist;
	private IVector2 initBallPos;

	private final PositionValidator positionValidator = new PositionValidator();


	private double getTargetOrientation()
	{
		// ボールの方向
		var dir = getBall().getPos().subtractNew(getPos());
		boolean ballNotOnCamera = !getBall().isOnCam(0.1);
		boolean insideBot = dir.getLength2() < getTBot().getCenter2DribblerDist();
		if (ballNotOnCamera || insideBot)
		{
			// ボールが見えない（ロボットに隠れている？）
			// ボールが十分に近い
			// => 前回の方向を使う
			return cachedOrientation;
		}
		double currentDir = dir.getAngle(0);
		if (AngleMath.diffAbs(currentDir, cachedOrientation) > 0.3)
		{
			return currentDir;
		}
		return cachedOrientation;
	}


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		positionValidator.setMarginToFieldBorder(Geometry.getBoundaryWidth() - Geometry.getBotRadius());
		// 初期方向：ボールの方向
		cachedOrientation = getBall().getPos().subtractNew(getPos()).getAngle();
		approachBallExtraDist = 0;
		initBallPos = getBall().getPos();
		// キッカーはOFF、ドリブラーはON
		setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.DEFAULT));

		// ボールを避けないようにする
		getMoveCon().physicalObstaclesOnly();
		getMoveCon().setBallObstacle(false);
	}


	@Override
	public void doUpdate()
	{
	    // 接触時間が一定以上になったら成功
		if (getTBot().getBallContact().getContactDuration() > minSuccessContactTime)
		{
			setSkillState(ESkillState.SUCCESS);
		}
		// 0.1s以上ボールに接触していない　&& (ボールが初期位置から OR ボールが初期位置から10cm以上移動している)
		// => 失敗
		else if (!getTBot().getBallContact().hadContact(0.1)
				&& (approachBallExtraDist > maxApproachBallExtraDist
				|| initBallPos.distanceTo(getBall().getPos()) > 100))
		{
			approachBallExtraDist = maxApproachBallExtraDist;
			setSkillState(ESkillState.FAILURE);
		} else
		{
			setSkillState(ESkillState.IN_PROGRESS);
		}

		if (getVel().getLength2() <= getMoveConstraints().getVelMax())
		{
			getMoveConstraints().setAccMax(getContactAcc);
		}

		positionValidator.update(getWorldFrame(), getMoveCon());

        // 接触時間を満たしていない場合継続
		if (getTBot().getBallContact().getContactDuration() < minContactTime)
		{
			var dest = getDest();
			dest = positionValidator.movePosInsideFieldWrtBallPos(dest);
			dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);
			updateDestination(dest);

			cachedOrientation = getTargetOrientation();
			updateTargetAngle(cachedOrientation);

            // 15mmより近い場合，さらに10mm近づく
			if (getPos().distanceTo(dest) < 15)
			{
				approachBallExtraDist += 10;
			}
		}
		// 十分に近づいて接触時間を満たしている場合はボールとの適正な距離を維持
		else
		{
			approachBallExtraDist = 0;
		}

		getShapes().get(ESkillShapesLayer.GET_BALL_CONTACT).add(new DrawableLine(
				Lines.segmentFromOffset(getPos(), Vector2.fromAngle(cachedOrientation).scaleTo(500)),
				Color.red
		));

		super.doUpdate();
	}


	private IVector2 getDest()
	{
        // ロボットがcachedOrientationを向いた状態でアプローチできる位置にターゲットを生成
        // approachBallExtraDistの分だけボールから離れる
		return getBall().getPos().subtractNew(
				Vector2.fromAngle(cachedOrientation).scaleTo(
						getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() - approachBallExtraDist
				)
		);
	}
}
