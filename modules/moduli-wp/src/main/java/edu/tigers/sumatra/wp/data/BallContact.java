/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import com.sleepycat.persist.model.Persistent;
import lombok.AllArgsConstructor;
import lombok.Value;


// このクラスは各ロボットが保持し，そのロボットのボールとの接触を管理するクラス
/**
 * Encode the contact (time/duration) to the ball.
 */
@Persistent
@Value
@AllArgsConstructor
public class BallContact
{
	// 現在時刻？
	long current;
	// 接触開始時刻
	long start;
	// 接触終了時刻
	long end;
	// Visionによる判定かどうか（Visonではない場合，ロボットがドリブラセンサで検知している）
	boolean ballContactFromVision;


	@SuppressWarnings("unused")
	public BallContact()
	{
		current = 0;
		start = 0;
		end = 0;
		ballContactFromVision = false;
	}

	public static BallContact def(long timestamp)
	{
		return new BallContact(timestamp, (long) -1e9, (long) -1e9, false);
	}


	// 現在接触しているか（現在時刻と接触終了時刻が同じ）
	public boolean hasContact()
	{
		return current == end;
	}


	public boolean hasNoContact()
	{
		return !hasContact();
	}


	public boolean hasContactFromVisionOrBarrier()
	{
		return hasContact() || isBallContactFromVision();
	}


	public double getContactDuration()
	{
		if (hasNoContact())
		{
			return 0;
		}
		return (end - start) * 1e-9;
	}


	// horizon秒以内に接触しているか
	public boolean hadContact(double horizon)
	{
		return (current - end) * 1e-9 < horizon;
	}
}
