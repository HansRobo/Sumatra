/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.09.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ABotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.EBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.commons.InstanceablePanel;


/**
 * Control various skills from this panel.
 * 
 * @author AndreR, DanielW
 */
public class SkillsPanel extends JPanel implements IInstanceableObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long						serialVersionUID			= 5399293600256113771L;
	
	private static final String					DEF_SKILL_KEY				= SkillsPanel.class.getCanonicalName()
																									+ ".defskill";
	private static final String					DEF_BOT_SKILL_KEY			= SkillsPanel.class.getCanonicalName()
																									+ ".defbotskill";
	
	private JTextField								moveToX						= null;
	private JTextField								moveToY						= null;
	
	private JTextField								rotateAndMoveToX			= null;
	private JTextField								rotateAndMoveToY			= null;
	private JTextField								rotateAndMoveToXyAngle	= null;
	
	private JTextField								straightMoveDist			= null;
	private JTextField								straightMoveAngle			= null;
	
	private JTextField								lookX							= null;
	private JTextField								lookY							= null;
	
	private JTextField								dribbleRPM					= null;
	
	private final List<ISkillsPanelObserver>	observers					= new ArrayList<ISkillsPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public SkillsPanel()
	{
		setLayout(new MigLayout("fill"));
		
		InstanceablePanel skillCustomPanel = new InstanceablePanel(ESkillName.values());
		skillCustomPanel.addObserver(this);
		String strDefSkill = SumatraModel.getInstance().getUserProperty(DEF_SKILL_KEY, ESkillName.KICK.name());
		try
		{
			ESkillName defSkill = ESkillName.valueOf(strDefSkill);
			skillCustomPanel.setSelectedItem(defSkill);
		} catch (IllegalArgumentException err)
		{
			// ignore
		}
		
		InstanceablePanel botSkillCustomPanel = new InstanceablePanel(EBotSkill.values());
		botSkillCustomPanel.addObserver(this);
		String strDefBotSkill = SumatraModel.getInstance().getUserProperty(DEF_BOT_SKILL_KEY,
				EBotSkill.GLOBAL_POSITION.name());
		try
		{
			EBotSkill defSkill = EBotSkill.valueOf(strDefBotSkill);
			botSkillCustomPanel.setSelectedItem(defSkill);
		} catch (IllegalArgumentException err)
		{
			// ignore
		}
		
		
		// MOVING TO XY
		final JPanel moveToXYPanel = new JPanel(new MigLayout("fill", "[]10[50,fill]20[]10[50,fill]20[100,fill]"));
		
		moveToX = new JTextField("0");
		moveToY = new JTextField("0");
		
		final JButton moveToXY = new JButton("Move to XY");
		moveToXY.addActionListener(new MoveToXY());
		
		moveToXYPanel.add(new JLabel("X:"));
		moveToXYPanel.add(moveToX);
		moveToXYPanel.add(new JLabel("Y:"));
		moveToXYPanel.add(moveToY);
		moveToXYPanel.add(moveToXY);
		
		moveToXYPanel.setBorder(BorderFactory.createTitledBorder("MoveToXY"));
		
		// STRAIGHT MOVING
		final JPanel straightMovePanel = new JPanel(new MigLayout("fill", "[]10[60,fill]20[]20[50,fill]10[]10[]"));
		
		straightMoveDist = new JTextField("1000");
		straightMoveAngle = new JTextField("0");
		
		final JButton btnForewards = new JButton("Forewards");
		final JButton btnBackwards = new JButton("Backwards");
		btnForewards.addActionListener(new StraightMove(1));
		btnBackwards.addActionListener(new StraightMove(-1));
		
		straightMovePanel.add(btnForewards);
		straightMovePanel.add(btnBackwards);
		straightMovePanel.add(new JLabel("Dist [mm]:"));
		straightMovePanel.add(straightMoveDist);
		straightMovePanel.add(new JLabel("Angle [deg]:"));
		straightMovePanel.add(straightMoveAngle);
		
		straightMovePanel.setBorder(BorderFactory.createTitledBorder("StraightMove"));
		
		
		// LOOKING
		final JPanel aimPanel = new JPanel(new MigLayout("fill", "[]10[50,fill]20[]10[50,fill]20[100,fill]"));
		lookX = new JTextField("0");
		lookY = new JTextField("0");
		
		final JButton lookAt = new JButton("look xy");
		lookAt.addActionListener(new LookAt());
		
		
		aimPanel.add(new JLabel("x: "));
		aimPanel.add(lookX);
		aimPanel.add(new JLabel("y: "));
		aimPanel.add(lookY);
		aimPanel.add(lookAt);
		
		aimPanel.setBorder(BorderFactory.createTitledBorder("LookAt"));
		
		// DRIBBLE
		final JPanel dribblePanel = new JPanel(new MigLayout("fill", "[]10[50,fill]20[100,fill]"));
		
		dribbleRPM = new JTextField("10000");
		
		final JButton dribble = new JButton("Dribble");
		dribble.addActionListener(new Dribble());
		
		dribblePanel.add(new JLabel("rpm: "));
		dribblePanel.add(dribbleRPM);
		dribblePanel.add(dribble);
		
		dribblePanel.setBorder(BorderFactory.createTitledBorder("Dribble"));
		
		// RotateAndMoveToXY
		final JPanel rotateAndMoveToXYPanel = new JPanel(new MigLayout("fill",
				"[]10[50,fill]20[]10[50,fill]20[]10[50,fill]20[]"));
		
		rotateAndMoveToX = new JTextField("0");
		rotateAndMoveToY = new JTextField("0");
		rotateAndMoveToXyAngle = new JTextField("3");
		
		final JButton rotateAndMoveToXY = new JButton("Move to XY and rotate");
		rotateAndMoveToXY.addActionListener(new RotateAndMoveToXY());
		
		rotateAndMoveToXYPanel.add(new JLabel("X:"));
		rotateAndMoveToXYPanel.add(rotateAndMoveToX);
		rotateAndMoveToXYPanel.add(new JLabel("Y:"));
		rotateAndMoveToXYPanel.add(rotateAndMoveToY);
		rotateAndMoveToXYPanel.add(new JLabel("Angle:"));
		rotateAndMoveToXYPanel.add(rotateAndMoveToXyAngle);
		
		rotateAndMoveToXYPanel.add(rotateAndMoveToXY);
		
		add(skillCustomPanel, "wrap");
		add(botSkillCustomPanel, "wrap");
		add(moveToXYPanel, "wrap");
		add(straightMovePanel, "wrap");
		add(aimPanel, "wrap");
		add(dribblePanel, "wrap");
		add(Box.createGlue(), "push");
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(final ISkillsPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ISkillsPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyMoveToXY(final float x, final float y)
	{
		synchronized (observers)
		{
			for (final ISkillsPanelObserver observer : observers)
			{
				observer.onMoveToXY(x, y);
			}
		}
	}
	
	
	private void notifyRotateAndMoveToXY(final float x, final float y, final float angle)
	{
		synchronized (observers)
		{
			for (final ISkillsPanelObserver observer : observers)
			{
				observer.onRotateAndMoveToXY(x, y, angle);
			}
		}
	}
	
	
	private void notifyStraightMove(final int distance, final float angle)
	{
		synchronized (observers)
		{
			for (final ISkillsPanelObserver observer : observers)
			{
				observer.onStraightMove(distance, angle);
			}
		}
	}
	
	
	private void notifyLookAt(final Vector2 lookAtTarget)
	{
		synchronized (observers)
		{
			for (final ISkillsPanelObserver observer : observers)
			{
				observer.onLookAt(lookAtTarget);
			}
		}
	}
	
	
	private void notifyDribble(final int rpm)
	{
		synchronized (observers)
		{
			for (final ISkillsPanelObserver observer : observers)
			{
				observer.onDribble(rpm);
			}
		}
	}
	
	
	/**
	 * @param skill
	 */
	private void notifySkill(final ASkill skill)
	{
		synchronized (observers)
		{
			for (final ISkillsPanelObserver observer : observers)
			{
				observer.onSkill(skill);
			}
		}
	}
	
	
	/**
	 * @param skill
	 */
	private void notifyBotSkill(final ABotSkill skill)
	{
		synchronized (observers)
		{
			for (final ISkillsPanelObserver observer : observers)
			{
				observer.onBotSkill(skill);
			}
		}
	}
	
	
	@Override
	public void onNewInstance(final Object object)
	{
		if (object instanceof ASkill)
		{
			ASkill skill = (ASkill) object;
			SumatraModel.getInstance().setUserProperty(DEF_SKILL_KEY, skill.getSkillName().name());
			notifySkill(skill);
		}
		else if (object instanceof ABotSkill)
		{
			ABotSkill skill = (ABotSkill) object;
			SumatraModel.getInstance().setUserProperty(DEF_BOT_SKILL_KEY, skill.getType().name());
			notifyBotSkill(skill);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- actions --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private class MoveToXY implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			float x;
			float y;
			
			try
			{
				x = Float.parseFloat(moveToX.getText());
				y = Float.parseFloat(moveToY.getText());
			} catch (final NumberFormatException e)
			{
				return;
			}
			
			notifyMoveToXY(x, y);
		}
	}
	
	private class RotateAndMoveToXY implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			float x;
			float y;
			float angle;
			try
			{
				x = Float.parseFloat(rotateAndMoveToX.getText());
				y = Float.parseFloat(rotateAndMoveToY.getText());
				angle = Float.parseFloat(rotateAndMoveToXyAngle.getText());
			} catch (final NumberFormatException e)
			{
				return;
			}
			
			notifyRotateAndMoveToXY(x, y, angle);
		}
	}
	
	private class StraightMove implements ActionListener
	{
		private final int	factor;
		
		
		/**
		 * @param factor
		 */
		public StraightMove(final int factor)
		{
			this.factor = factor;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			int distance;
			float angle;
			
			try
			{
				distance = Integer.parseInt(straightMoveDist.getText());
			} catch (final NumberFormatException e)
			{
				return;
			}
			
			try
			{
				angle = AngleMath.deg2rad(Float.parseFloat(straightMoveAngle.getText()));
			} catch (final NumberFormatException e)
			{
				return;
			}
			
			notifyStraightMove(factor * distance, angle);
		}
	}
	
	
	private class LookAt implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			Float x;
			Float y;
			try
			{
				x = Float.valueOf(lookX.getText());
				y = Float.valueOf(lookY.getText());
			} catch (final NumberFormatException err)
			{
				return;
			}
			notifyLookAt(new Vector2(x, y));
		}
	}
	
	
	private class Dribble implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			int rpm;
			try
			{
				rpm = Integer.valueOf(dribbleRPM.getText());
				dribbleRPM.setBackground(Color.WHITE);
			} catch (final NumberFormatException err)
			{
				dribbleRPM.setBackground(Color.RED);
				return;
			}
			notifyDribble(rpm);
		}
	}
}
