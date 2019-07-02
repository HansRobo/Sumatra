/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.data;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * The current game state and useful helper methods to query the state.
 */
@Persistent(version = 1)
public class GameState
{
	private final EGameState state;
	private final EGameState nextState;
	private final ETeamColor forTeam;
	private final ETeamColor nextForTeam;
	private final ETeamColor ourTeam;
	private final boolean penaltyShootout;
	private final IVector2 ballPlacementPosition;

	/** Neutral RUNNING state */
	public static final GameState RUNNING = Builder.empty().withState(EGameState.RUNNING).build();

	/** Neutral STOP state */
	public static final GameState STOP = Builder.empty().withState(EGameState.STOP).build();

	/** Neutral HALT state */
	public static final GameState HALT = Builder.empty().withState(EGameState.HALT).build();


	@SuppressWarnings("unused") // Required for persistence
	private GameState()
	{
		state = EGameState.HALT;
		nextState = EGameState.HALT;
		forTeam = ETeamColor.NEUTRAL;
		nextForTeam = ETeamColor.NEUTRAL;
		ourTeam = ETeamColor.NEUTRAL;
		penaltyShootout = false;
		ballPlacementPosition = null;
	}


	private GameState(final Builder builder)
	{
		state = builder.state;
		nextState = builder.nextState;
		forTeam = builder.forTeam;
		nextForTeam = builder.nextForTeam;
		ourTeam = builder.ourTeam;
		penaltyShootout = builder.penaltyShootout;
		ballPlacementPosition = builder.ballPlacementPosition;
	}


	public EGameState getState()
	{
		return state;
	}


	public ETeamColor getForTeam()
	{
		return forTeam;
	}


	public EGameState getNextState()
	{
		return nextState;
	}


	public ETeamColor getNextForTeam()
	{
		return nextForTeam;
	}


	public ETeamColor getOurTeam()
	{
		return ourTeam;
	}


	public boolean isPenaltyShootout()
	{
		return penaltyShootout;
	}


	/**
	 * @return Ball placement coordinates in vision frame.
	 */
	public IVector2 getBallPlacementPositionNeutral()
	{
		return ballPlacementPosition;
	}


	/**
	 * @return Ball placement coordinates in local team frame.
	 */
	public IVector2 getBallPlacementPositionForUs()
	{
		if (ballPlacementPosition != null && ourTeam != Geometry.getNegativeHalfTeam())
		{
			return ballPlacementPosition.multiplyNew(-1.0d);
		}

		return ballPlacementPosition;
	}


	/**
	 * Get the name of the current state appended by _BLUE or _YELLOW.
	 *
	 * @return
	 */
	public String getStateNameWithColor()
	{
		return getStateName(state, forTeam);
	}


	/**
	 * Get the name of the next state appended by _BLUE or _YELLOW.
	 *
	 * @return
	 */
	public String getNextStateNameWithColor()
	{
		return getStateName(nextState, nextForTeam);
	}


	private String getStateName(EGameState gameState, ETeamColor teamColor)
	{
		if (gameState == null)
		{
			return "-";
		}
		StringBuilder sb = new StringBuilder(gameState.toString());
		if (teamColor.isNonNeutral())
		{
			sb.append("_").append(teamColor);
		}

		return sb.toString();
	}


	/**
	 * @return true if the ball must be at rest (PENALTY, KICKOFF, INDIRECT_FREE, DIRECT_FREE)
	 */
	public boolean isBallAtRest()
	{
		switch (state)
		{
			case PENALTY:
			case KICKOFF:
			case INDIRECT_FREE:
			case DIRECT_FREE:
				return true;
			default:
				return false;
		}
	}


	/**
	 * @return true on any stopped game state (BREAK, HALT, POST_GAME, STOP, TIMEOUT, BALL_PLACEMENT)
	 */
	public boolean isStoppedGame()
	{
		switch (state)
		{
			case BREAK:
			case HALT:
			case POST_GAME:
			case STOP:
			case TIMEOUT:
			case BALL_PLACEMENT:
				return true;
			default:
				return false;
		}
	}


	/**
	 * @return true on any "nothing-happens" state (BREAK, HALT, TIMEOUT, POST_GAME)
	 */
	public boolean isIdleGame()
	{
		return isPausedGame() || state == EGameState.POST_GAME;
	}


	/**
	 * @return true on any state within a game, where nothing happens (BREAK, HALT, TIMEOUT)
	 */
	public boolean isPausedGame()
	{
		switch (state)
		{
			case BREAK:
			case HALT:
			case TIMEOUT:
				return true;
			default:
				return false;
		}
	}


	/**
	 * @return true on any game state that requires a limited velocity.
	 */
	public boolean isVelocityLimited()
	{
		// note: explicitly not in HALT: Robots may not move here anyway, but during skill testing, its
		// annoying to change the gameState after each Sumatra restart.
		return state == EGameState.STOP;
	}


	/**
	 * @return true on any gamestate that requires that we keep a distance to the ball.
	 */
	public boolean isDistanceToBallRequired()
	{
		switch (state)
		{
			case STOP:
			case PREPARE_KICKOFF:
				return true;
			default:
				break;
		}

		if (ourTeam != forTeam)
		{
			// some gamestate for THEM (or NEUTRAL)
			switch (state)
			{
				case BALL_PLACEMENT:
				case INDIRECT_FREE:
				case DIRECT_FREE:
				case PREPARE_PENALTY:
					return true;
				default:
					break;
			}
		}

		return false;
	}


	/**
	 * Check if this state equals a given state in state and forTeam.
	 *
	 * @param compare
	 * @return
	 */
	public boolean isSameStateAndForTeam(final GameState compare)
	{
		return (state == compare.state) && (forTeam == compare.forTeam);
	}


	/**
	 * @return true if this is a <i>whatever-state</i> for us. Neutral situations will also return true.
	 */
	public boolean isGameStateForUs()
	{
		return (forTeam == ourTeam) || (forTeam == ETeamColor.NEUTRAL);
	}


	/**
	 * @return true if this is a <i>whatever-state</i> for the opponent.
	 */
	public boolean isGameStateForThem()
	{
		return !isGameStateForUs();
	}


	/**
	 * @return true if the next state is a <i>whatever-state</i> for us. Neutral situations will also return true.
	 */
	public boolean isNextGameStateForUs()
	{
		return (nextForTeam == ourTeam) || (nextForTeam == ETeamColor.NEUTRAL);
	}


	/**
	 * @return true if the next state is a <i>whatever-state</i> for the opponent.
	 */
	public boolean isNextGameStateForThem()
	{
		return !isNextGameStateForUs();
	}


	/**
	 * @return true if this is a DIRECT_FREE or INDIRECT_FREE for the opponent.
	 */
	public boolean isStandardSituationForThem()
	{
		if (isGameStateForUs())
		{
			return false;
		}

		return (state == EGameState.DIRECT_FREE) || (state == EGameState.INDIRECT_FREE);
	}


	/**
	 * @return true if this is a DIRECT_FREE or INDIRECT_FREE for us.
	 */
	public boolean isStandardSituationForUs()
	{
		if (isGameStateForThem())
		{
			return false;
		}

		return (state == EGameState.DIRECT_FREE) || (state == EGameState.INDIRECT_FREE);
	}


	/**
	 * @return true if the next state is a DIRECT_FREE or INDIRECT_FREE for us.
	 */
	public boolean isNextStandardSituationForUs()
	{
		if (isNextGameStateForThem())
		{
			return false;
		}

		return (nextState == EGameState.DIRECT_FREE) || (nextState == EGameState.INDIRECT_FREE);
	}


	/**
	 * @return true if this is a DIRECT_FREE or INDIRECT_FREE
	 */
	public boolean isStandardSituation()
	{
		return (state == EGameState.DIRECT_FREE) || (state == EGameState.INDIRECT_FREE);
	}


	/**
	 * @return true if this is a DIRECT_FREE, INDIRECT_FREE, or KICKOFF for the opponent.
	 */
	public boolean isStandardSituationIncludingKickoffForThem()
	{
		if (isGameStateForUs())
		{
			return false;
		}

		return (state == EGameState.DIRECT_FREE) || (state == EGameState.INDIRECT_FREE) || (state == EGameState.KICKOFF);
	}


	/**
	 * @return true if this is a DIRECT_FREE, INDIRECT_FREE, or KICKOFF for us.
	 */
	public boolean isStandardSituationIncludingKickoffForUs()
	{
		if (isGameStateForThem())
		{
			return false;
		}

		return (state == EGameState.DIRECT_FREE) || (state == EGameState.INDIRECT_FREE) || (state == EGameState.KICKOFF);
	}


	/**
	 * @return true if the current state is KICKOFF or PREPARE_KICKOFF
	 */
	public boolean isKickoffOrPrepareKickoff()
	{
		return (state == EGameState.KICKOFF) || (state == EGameState.PREPARE_KICKOFF);
	}


	/**
	 * @return true if this is a KICKOFF or PREPARE_KICKOFF for us.
	 */
	public boolean isKickoffOrPrepareKickoffForUs()
	{
		return isKickoffOrPrepareKickoff() && isGameStateForUs();
	}


	/**
	 * @return true if this is a KICKOFF or PREPARE_KICKOFF for them.
	 */
	public boolean isKickoffOrPrepareKickoffForThem()
	{
		return isKickoffOrPrepareKickoff() && isGameStateForThem();
	}


	/**
	 * @return true if this is an INDIRECT_FREE for us.
	 */
	public boolean isIndirectFreeForUs()
	{
		return (state == EGameState.INDIRECT_FREE) && isGameStateForUs();
	}


	public boolean isDirectFreeForUs()
	{
		return state == EGameState.DIRECT_FREE && isGameStateForUs();
	}


	public boolean isDirectFreeForThem()
	{
		return state == EGameState.DIRECT_FREE && isGameStateForThem();
	}


	/**
	 * @return true if this is an INDIRECT_FREE for them.
	 */
	public boolean isIndirectFreeForThem()
	{
		return (state == EGameState.INDIRECT_FREE) && isGameStateForThem();
	}


	/**
	 * @return true if this is a BALL_PLACEMENT for them.
	 */
	public boolean isBallPlacementForThem()
	{
		return isBallPlacement() && isGameStateForThem();
	}


	/**
	 * @return true if this is a BALL_PLACEMENT for us.
	 */
	public boolean isBallPlacementForUs()
	{
		return isBallPlacement() && isGameStateForUs();
	}


	/**
	 * @return true if state is BALL_PLACEMENT.
	 */
	public boolean isBallPlacement()
	{
		return state == EGameState.BALL_PLACEMENT;
	}


	/**
	 * @return true if state is RUNNING.
	 */
	public boolean isRunning()
	{
		return state == EGameState.RUNNING;
	}


	/**
	 * @return true if the game is in running state, in a standard situation or in kickoff (no prepare states)
	 */
	public boolean isGameRunning()
	{
		return isRunning()
				|| isStandardSituation()
				|| getState() == EGameState.KICKOFF;
	}


	/**
	 * @return true if state is PENALTY or PREPARE_PENALTY
	 */
	public boolean isPenaltyOrPreparePenalty()
	{
		return (state == EGameState.PENALTY) || (state == EGameState.PREPARE_PENALTY);
	}


	/**
	 * @return true if state is PENALTY or PREPARE_PENALTY for them
	 */
	public boolean isPenaltyOrPreparePenaltyForThem()
	{
		return isPenaltyOrPreparePenalty() && isGameStateForThem();
	}


	/**
	 * @return true if state is PENALTY or PREPARE_PENALTY for us
	 */
	public boolean isPenaltyOrPreparePenaltyForUs()
	{
		return isPenaltyOrPreparePenalty() && isGameStateForUs();
	}


	/**
	 * @return true if state is PREPARE_KICKOFF
	 */
	public boolean isPrepareKickoff()
	{
		return state == EGameState.PREPARE_KICKOFF;
	}


	/**
	 * @return true if state is PREPARE_KICKOFF for them
	 */
	public boolean isPrepareKickoffForThem()
	{
		return isPrepareKickoff() && isGameStateForThem();
	}


	/**
	 * @return true if state is PREPARE_KICKOFF for us
	 */
	public boolean isPrepareKickoffForUs()
	{
		return isPrepareKickoff() && isGameStateForUs();
	}


	/**
	 * @return true if state is STOP
	 */
	public boolean isStop()
	{
		return state == EGameState.STOP;
	}


	/**
	 * @return true if state is PREPARE_PENALTY
	 */
	public boolean isPreparePenalty()
	{
		return state == EGameState.PREPARE_PENALTY;
	}


	/**
	 * @return true if state is PREPARE_PENALTY for them.
	 */
	public boolean isPreparePenaltyForThem()
	{
		return (state == EGameState.PREPARE_PENALTY) && isGameStateForThem();
	}


	/**
	 * @return true if state is PREPARE_PENALTY for us.
	 */
	public boolean isPreparePenaltyForUs()
	{
		return (state == EGameState.PREPARE_PENALTY) && isGameStateForUs();
	}


	@Override
	public String toString()
	{
		String result = state.toString();
		if (forTeam != ETeamColor.NEUTRAL)
		{
			result += " for " + forTeam;
		}
		return result;
	}

	/**
	 * Builder
	 */
	public static class Builder
	{
		private EGameState state;
		private EGameState nextState = EGameState.UNKNOWN;
		private ETeamColor forTeam;
		private ETeamColor nextForTeam = ETeamColor.NEUTRAL;
		private ETeamColor ourTeam;
		private boolean penaltyShootout;
		private IVector2 ballPlacementPosition = null;


		private Builder()
		{
		}


		/**
		 * @return new builder
		 */
		public static Builder create()
		{
			return new Builder();
		}


		/**
		 * @return this builder
		 */
		public static Builder empty()
		{
			Builder builder = new Builder();
			builder.state = EGameState.HALT;
			builder.forTeam = ETeamColor.NEUTRAL;
			builder.ourTeam = ETeamColor.NEUTRAL;
			return builder;
		}


		/**
		 * @param state
		 * @return this builder
		 */
		public Builder withState(final EGameState state)
		{
			this.state = state;
			return this;
		}


		/**
		 * @param nextState
		 * @return this builder
		 */
		public Builder withNextState(final EGameState nextState)
		{
			this.nextState = nextState;
			return this;
		}


		/**
		 * @param color
		 * @return this builder
		 */
		public Builder nextForTeam(final ETeamColor color)
		{
			nextForTeam = color;
			return this;
		}


		/**
		 * @param color
		 * @return this builder
		 */
		public Builder forTeam(final ETeamColor color)
		{
			forTeam = color;
			return this;
		}


		/**
		 * @param color
		 * @return this builder
		 */
		public Builder withOurTeam(final ETeamColor color)
		{
			ourTeam = color;
			return this;
		}


		/**
		 * @param penaltyShootout
		 * @return this builder
		 */
		public Builder withPenalyShootout(final boolean penaltyShootout)
		{
			this.penaltyShootout = penaltyShootout;
			return this;
		}


		/**
		 * @param ballPlacementPosition
		 * @return this builder
		 */
		public Builder withBallPlacementPosition(final IVector2 ballPlacementPosition)
		{
			this.ballPlacementPosition = ballPlacementPosition;
			return this;
		}


		/**
		 * @param gameState
		 * @return this builder
		 */
		public Builder withGameState(final GameState gameState)
		{
			state = gameState.state;
			nextState = gameState.nextState;
			forTeam = gameState.forTeam;
			nextForTeam = gameState.nextForTeam;
			ourTeam = gameState.ourTeam;
			penaltyShootout = gameState.penaltyShootout;
			ballPlacementPosition = gameState.ballPlacementPosition;
			return this;
		}


		/**
		 * @return new instance
		 */
		public GameState build()
		{
			Validate.notNull(state);
			Validate.notNull(forTeam);
			Validate.notNull(ourTeam);
			return new GameState(this);
		}
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final GameState gameState = (GameState) o;

		return new EqualsBuilder()
				.append(penaltyShootout, gameState.penaltyShootout)
				.append(state, gameState.state)
				.append(forTeam, gameState.forTeam)
				.append(ourTeam, gameState.ourTeam)
				.append(ballPlacementPosition, gameState.ballPlacementPosition)
				.isEquals();
	}


	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(state)
				.append(forTeam)
				.append(ourTeam)
				.append(penaltyShootout)
				.append(ballPlacementPosition)
				.toHashCode();
	}
}