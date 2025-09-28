// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.units.measure.Current;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import com.revrobotics.ColorSensorV3;
import com.revrobotics.ColorMatchResult;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.PositionTorqueCurrentFOC;
import com.ctre.phoenix6.controls.TorqueCurrentFOC;
import com.revrobotics.ColorMatch;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import static edu.wpi.first.units.Units.Amps;
import com.ctre.phoenix6.controls.PositionTorqueCurrentFOC;
import java.util.Optional;
import edu.wpi.first.wpilibj.DriverStation;

public class Intake extends SubsystemBase {
  private final I2C.Port i2cPort = I2C.Port.kOnboard;
  private final ColorSensorV3 m_colorSensor = new ColorSensorV3(i2cPort);
  private final ColorMatch m_colorMatcher = new ColorMatch();

  private final Color kBlueTarget = new Color(0.143, 0.427, 0.429);
  private final Color kGreenTarget = new Color(0.197, 0.561, 0.240);
  private final Color kRedTarget = new Color(0.561, 0.232, 0.114);
  private final Color kYellowTarget = new Color(0.361, 0.524, 0.113);

  private final PositionTorqueCurrentFOC m_positionTorque = new PositionTorqueCurrentFOC(0).withSlot(1);

  // motors
  TalonFX m_pivotMotor = new TalonFX(31);
  TalonFX m_rollerMotor = new TalonFX(5);


  // Intake states
  private enum State {
    IDLE,
    ACTIVE,
    EJECTING,
    INTAKING
  }

  State m_state = State.IDLE;

  String m_colorString;

  /** Creates a new Intake subsystem. */
  public Intake() {
    m_colorMatcher.addColorMatch(kBlueTarget);
    m_colorMatcher.addColorMatch(kGreenTarget);
    m_colorMatcher.addColorMatch(kRedTarget);
    m_colorMatcher.addColorMatch(kYellowTarget);

    TalonFXConfiguration configs = new TalonFXConfiguration();
    configs.Slot1.kP = 60; // An error of 1 rotation results in 60 A output
    configs.Slot1.kI = 0; // No output for integrated error
    configs.Slot1.kD = 6; // A velocity of 1 rps results in 6 A output
    // Peak output of 60 A
    configs.TorqueCurrent.withPeakForwardTorqueCurrent(Amps.of(60))
      .withPeakReverseTorqueCurrent(Amps.of(-60));

    m_pivotMotor.getConfigurator().apply(configs);
    m_pivotMotor.setPosition(0);
  }

  public Command exampleMethodCommand() {
    // Inline construction of command goes here.
    // Subsystem::RunOnce implicitly requires `this` subsystem.
    return runOnce(
        () -> {
          /* one-time action goes here */
        });
  }

  public Command active() {
    return Commands.runOnce(() -> {
        m_state = State.ACTIVE;
        TorqueCurrentFOC req = new TorqueCurrentFOC(Amps.of(3.0));
        m_rollerMotor.setControl(req);
    });
  }

  public Command ejecting() {
    return Commands.runOnce(() -> {
        m_state = State.EJECTING;
        TorqueCurrentFOC req = new TorqueCurrentFOC(Amps.of(-3.0));
        m_rollerMotor.setControl(req);
    }).withTimeout(5.0).andThen(active());
  }

  public Command intaking() {
    return Commands.runOnce(() -> {
        m_state = State.INTAKING;
    }).withTimeout(5.0).andThen(idle());
  }

  public Command idle() {
    return Commands.runOnce(() -> {
        m_state = State.IDLE;
        TorqueCurrentFOC req = new TorqueCurrentFOC(Amps.of(0.0));
        m_rollerMotor.setControl(req);
        m_pivotMotor.setControl(m_positionTorque.withPosition(0.0));
    });
  }

  public Command toggleIntake() {
    return Commands.runOnce(() -> {
      Current current;
      double rotations;
      if (m_state == State.IDLE) {
        current = Amps.of(3.0);
        m_state = State.ACTIVE;
        rotations = 0.0;
      } else {
        current = Amps.of(0.0);
        rotations = 80.5;
        m_state = State.IDLE;
      }
      TorqueCurrentFOC req = new TorqueCurrentFOC(current);
      m_rollerMotor.setControl(req);

      // m_pivot (up/down)
      m_pivotMotor.setControl(m_positionTorque.withPosition(rotations));
    });
  }

  /*
   * Conditions
   */
  public boolean isWrongColorCondition() {
    if (m_state != State.ACTIVE) {
      return false;
    }
    return isWrong();
  }

  public boolean isCorrectColorCondition() {
    if (m_state != State.ACTIVE) {
      return false;
    }
    return isCorrect();
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    Color detectedColor = m_colorSensor.getColor();

    /**
     * Run the color match algorithm on our detected color
     */
    ColorMatchResult match = m_colorMatcher.matchClosestColor(detectedColor);

    if (match.color == kBlueTarget) {
      m_colorString = "Blue";
    } else if (match.color == kRedTarget) {
      m_colorString = "Red";
    } else if (match.color == kGreenTarget) {
      m_colorString = "Green";
    } else if (match.color == kYellowTarget) {
      m_colorString = "Yellow";
    } else {
      m_colorString = "Unknown";
    }

    SmartDashboard.putNumber("Red", detectedColor.red);
    SmartDashboard.putNumber("Green", detectedColor.green);
    SmartDashboard.putNumber("Blue", detectedColor.blue);
    SmartDashboard.putNumber("Confidence", match.confidence);
    SmartDashboard.putString("Detected Color", m_colorString);
    SmartDashboard.putBoolean("IsCorrect", isCorrect());
    SmartDashboard.putBoolean("IsWrong", isWrong());
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }

  boolean isCorrect() {

    Optional<DriverStation.Alliance> aColor = DriverStation.getAlliance();

    if (!aColor.isPresent()) {
      return false;
    }
    if ((m_colorString == "Red") && (aColor.get() == DriverStation.Alliance.Red)) {
      return true;
    }
    if ((m_colorString == "Blue") && (aColor.get() == DriverStation.Alliance.Blue)) {
      return true;
    }
    return false;
  }

  boolean isWrong() {

    Optional<DriverStation.Alliance> aColor = DriverStation.getAlliance();

    if (!aColor.isPresent()) {
      return false;
    }
    if ((m_colorString == "Red") && (aColor.get() == DriverStation.Alliance.Blue)) {
      return true;
    }
    if ((m_colorString == "Blue") && (aColor.get() == DriverStation.Alliance.Red)) {
      return true;
    }
    return false;
  }
}
