// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.units.measure.Current;
import static edu.wpi.first.units.Units.Amps;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {
  public static class OperatorConstants {
    public static final int kDriverControllerPort = 0;
  }
  public static class Intake {
    public static final int kRollerID = 5;
    public static final int kPivotID = 31;

    public static final Current kIdleRollerCurrent    = Amps.of(0.0);
    public static final Current kForwardRollerCurrent = Amps.of(3.0);
    public static final Current kReverseRollerCurrent = Amps.of(-3.0);
    public static final double kStowedPostion         = 0.0;
    public static final double kExtendedPostion       = 80.5;
    public static final double kStateDelay            = 5.0;
  }
}
