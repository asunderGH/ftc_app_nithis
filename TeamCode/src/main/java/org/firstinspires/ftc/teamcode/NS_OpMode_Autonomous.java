package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

/**
 * Created by Nithilan on 11/26/2017.
 */

@Autonomous(name = "NS: OpMode Autonomous", group = "Competition")

public class NS_OpMode_Autonomous extends LinearOpMode {
    NS_Robot_GoldenGears GGRobot = null;

    // VuMark
    public static final String TAG = "Vuforia VuMark Sample";
    OpenGLMatrix lastLocation = null;
    VuforiaLocalizer vuforia;

    // These constants define the desired driving/control characteristics
    // The can/should be tweaked to suite the specific robot drive train.
    static final double     DRIVE_SPEED             = 1.0;     // Nominal speed for better accuracy.
    static final double     TURN_SPEED              = 0.5;     // Nominal half speed for better accuracy.
    static final double     ARM_SPEED               = 0.2;

    // Gyro Drive
    static final double     HEADING_THRESHOLD       = 1 ;      // As tight as we can make it with an integer gyro
    static final double     P_TURN_COEFF            = 0.1;     // Larger is more responsive, but also less stable
    static final double     P_DRIVE_COEFF           = 0.15;     // Larger is more responsive, but also less stable

    private enum Platform {
        R1, R2, B1, B2
    }

    @Override
    public void runOpMode() throws InterruptedException {
        /**
         * The following steps for Autonomous mode are:
         * 1. Decode VuMark image
         * 2. Knock off opponent's jewel
         * 3. Place glyph in crypto box based on VuMark image
         * 4. Park in safe zone
         */

        GGRobot = new NS_Robot_GoldenGears(hardwareMap);
        telemetry.addData("Status: ", "Robot initialized");

        // VuMark
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        parameters.vuforiaLicenseKey = "AXFmWqD/////AAAAGcQnWrSZvE1AhYt9fSvjbLhCIaNZZUuskVeDd4leXiCP1m03vRltyNPflJVYp8aCNJWor3BBw1OSVr+ykNY6LBTrakg4pDgBBl+GP08GRwlaGdYHGRwMayINjNZfXEflnGzt4tERZA7Dab+c5pNsyn3EvyMmFabBg7LHefKWWkdP489G2/g0Pj7BDITZfiUCFlTMl0Zzv3SLQIA2ri8u77/FbfgKF5UrqLH0Am7rN3Q8l6BNBktQ69itm867v06ENiwzHRBNkGymjT2arEWwp43rL3JjxKMg8XP+75YIyfEAJ/87lBCHfAODClGmTITQu42UgFBAQUmnIVb/SVMZnKALPrdMPuUG+LMZwOgGe4f1";
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);
        VuforiaTrackables relicTrackables = this.vuforia.loadTrackablesFromAsset("RelicVuMark");
        VuforiaTrackable relicTemplate = relicTrackables.get(0);
        relicTemplate.setName("relicVuMarkTemplate"); // can help in debugging; otherwise not necessary
        telemetry.addData("VuMark: ", "Vuforia initialized");
        telemetry.update();

        // Gyro Drive

        // Verify the robot is ready
        while (!isStopRequested() && !GGRobot.IsReady())  {
            sleep(50);
            idle();
        }
        GGRobot.ResetGyro();

        waitForStart();
        GGRobot.Start();
        telemetry.addData("Status: ", "Robot started");

        GGRobot.PositionClaw(0.4);
        while (!isStopRequested() && GGRobot.IsClawActuating()) {
            sleep(50);
            idle();
        }
        telemetry.addData("Status: ", "Glyph grabbed");
        GGRobot.RotateArm(ARM_SPEED);
        sleep(500);
        GGRobot.RotateArm(0.0);

        // VuMark
        telemetry.addData("VuMark: ", "Decoding target");
        relicTrackables.activate();
        RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(relicTemplate);
        while (opModeIsActive() && vuMark == RelicRecoveryVuMark.UNKNOWN) {
            vuMark = RelicRecoveryVuMark.from(relicTemplate);
        }
        relicTrackables.deactivate();
        telemetry.addData("VuMark: ", "Identified target = %s", vuMark);

        // Gyro Drive
        // Step through each leg of the path,
        // Note: Reverse movement is obtained by setting a negative distance (not speed)
        // Put a hold after each turn
        /*
        gyroDrive(DRIVE_SPEED, 48.0, 0.0);    // Drive FWD 48 inches
        gyroTurn( TURN_SPEED, -45.0);         // Turn  CCW to -45 Degrees
        gyroHold( TURN_SPEED, -45.0, 0.5);    // Hold -45 Deg heading for a 1/2 second
        gyroDrive(DRIVE_SPEED, 12.0, -45.0);  // Drive FWD 12 inches at 45 degrees
        gyroTurn( TURN_SPEED,  45.0);         // Turn  CW  to  45 Degrees
        gyroHold( TURN_SPEED,  45.0, 0.5);    // Hold  45 Deg heading for a 1/2 second
        gyroTurn( TURN_SPEED,   0.0);         // Turn  CW  to   0 Degrees
        gyroHold( TURN_SPEED,   0.0, 1.0);    // Hold  0 Deg heading for a 1 second
        gyroDrive(DRIVE_SPEED,-48.0, 0.0);    // Drive REV 48 inches
        */
        // gyroDrive(DRIVE_SPEED, 24.0, 0.0);
        // gyroTurn(TURN_SPEED, 90.0);
        // gyroDrive(DRIVE_SPEED, 24.0, -45.0);*/

        Platform plat = Platform.B1;
        if (plat == Platform.R1) {
            gyroDrive(DRIVE_SPEED, -24, 0.0);
            gyroTurn(TURN_SPEED, 45.0);
            gyroDrive(DRIVE_SPEED, -13.42, 0.0);
            gyroTurn(TURN_SPEED, 45.0);
            gyroDrive(DRIVE_SPEED, 12.0, 0.0);
        }
        else if (plat == Platform.R2) {
            gyroDrive(DRIVE_SPEED, -24, 0.0);
            gyroDrive(DRIVE_SPEED, 6, 0.0);
            gyroTurn(TURN_SPEED, 90.0);
            gyroDrive(DRIVE_SPEED, -24, 0.0);
            gyroTurn(TURN_SPEED, 45.0);
            gyroDrive(DRIVE_SPEED, 3, 0.0);
        }
        else if (plat == Platform.B1) {
            gyroDrive(DRIVE_SPEED, -48, 0.0);
            gyroTurn(TURN_SPEED, 45.0);
            gyroDrive(DRIVE_SPEED, -13.42, 0.0);
            gyroTurn(TURN_SPEED, 45.0);
            gyroDrive(DRIVE_SPEED, 12, 0.0);
        }
        else if (plat == Platform.B2) {
            gyroDrive(DRIVE_SPEED, 24, 0.0);
            gyroTurn(TURN_SPEED, -20.0);
            gyroDrive(DRIVE_SPEED, 4, 0.0);
        }

        GGRobot.Stop();
    }

    /**
     *  Method to drive on a fixed compass bearing (angle), based on encoder counts.
     *  Move will stop if either of these conditions occur:
     *  1) Move gets to the desired position
     *  2) Driver stops the opmode running.
     *
     * @param speed      Target speed for forward motion.  Should allow for _/- variance for adjusting heading
     * @param distance   Distance (in inches) to move from current position.  Negative distance means move backwards.
     * @param angle      Absolute Angle (in Degrees) relative to last gyro reset.
     *                   0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
     *                   If a relative angle is required, add/subtract from current heading.
     */
    public void gyroDrive ( double speed,
                            double distance,
                            double angle) {

        int     newLeftTarget;
        int     newRightTarget;
        int     moveCounts;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {
            GGRobot.OnDriveDistance(distance, speed);

            // keep looping while we are still active, and BOTH motors are running.
            while (opModeIsActive() && GGRobot.IsDriving()) {
                GGRobot.OnDriveAngle(angle, P_DRIVE_COEFF);
            }

            // Stop all motion;
            GGRobot.ResetDrive();

            // Turn off RUN_TO_POSITION
            GGRobot.ResetEncoders();
        }
    }

    /**
     *  Method to spin on central axis to point in a new direction.
     *  Move will stop if either of these conditions occur:
     *  1) Move gets to the heading (angle)
     *  2) Driver stops the opmode running.
     *
     * @param speed Desired speed of turn.
     * @param angle      Absolute Angle (in Degrees) relative to last gyro reset.
     *                   0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
     *                   If a relative angle is required, add/subtract from current heading.
     */
    public void gyroTurn (  double speed, double angle) {

        // keep looping while we are still active, and not on heading.
        while (opModeIsActive() && !GGRobot.OnDriveTurn(speed, angle, P_TURN_COEFF, HEADING_THRESHOLD)) {
            // Update telemetry & Allow time for other processes to run.
            telemetry.update();
        }
    }

    /**
     *  Method to obtain & hold a heading for a finite amount of time
     *  Move will stop once the requested time has elapsed
     *
     * @param speed      Desired speed of turn.
     * @param angle      Absolute Angle (in Degrees) relative to last gyro reset.
     *                   0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
     *                   If a relative angle is required, add/subtract from current heading.
     * @param holdTime   Length of time (in seconds) to hold the specified heading.
     */
    public void gyroHold( double speed, double angle, double holdTime) {

        ElapsedTime holdTimer = new ElapsedTime();

        // keep looping while we have time remaining.
        holdTimer.reset();
        while (opModeIsActive() && (holdTimer.time() < holdTime)) {
            // Update telemetry & Allow time for other processes to run.
            GGRobot.OnDriveTurn(speed, angle, P_TURN_COEFF, HEADING_THRESHOLD);
            telemetry.update();
        }

        // Stop all motion;
        GGRobot.ResetDrive();
    }

}
