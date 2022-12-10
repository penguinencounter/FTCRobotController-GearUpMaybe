package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.SharedHardware.frontLeft;
import static org.firstinspires.ftc.teamcode.SharedHardware.frontRight;
import static org.firstinspires.ftc.teamcode.SharedHardware.prepareHardware;
import static org.firstinspires.ftc.teamcode.SharedHardware.rearLeft;
import static org.firstinspires.ftc.teamcode.SharedHardware.rearRight;
import static org.firstinspires.ftc.teamcode.SharedHardware.turret;

import static java.util.Collections.swap;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name="TeleOp")
public class Teleop extends LinearOpMode {
    public Lift l;

    public final int TURRET_THRESHOLD = 800;
    public final int TURRET_DELTA = 1500; // STILL HAVE TO TEST
    public int turret_center;
    public boolean forward = true;

    @Override
    public void runOpMode() {
        prepareHardware(hardwareMap);
        l = new Lift(hardwareMap);
        turret_center = turret.getCurrentPosition();

        waitForStart();
        while (opModeIsActive()) {
            drive();
            lift();
            turret();
            telemetry.update();
        }
    }

    //////////////////////////////////////////////////////////////////

    public void drive() {
        double speed = (0.25 + gamepad1.left_trigger * 0.75);
        double vX = 0; // forward/back
        double vY = 0; // left/right
        boolean useDPad = true;
        if (gamepad1.dpad_up) {
            vX += 1;
        } else if (gamepad1.dpad_down) {
            vX -= 1;
        } else if (gamepad1.dpad_left) {
            vY -= 1;
        } else if (gamepad1.dpad_right) {
            vY += 1;
        } else {
            useDPad = false;
        }
        if (useDPad) {
            double m1 = vX + vY;
            double m2 = vX - vY;
            double m3 = vX - vY;
            double m4 = vX + vY;
            driveMotor(speed * m1, speed * m2, speed * m3, speed * m4);
        } else {
            double left = -gamepad1.left_stick_y,
                    right = -gamepad1.right_stick_y;
            updateDirection();
            driveMotor(left, left, right, right);
        }
    }

    public void updateDirection() {
        if(gamepad1.right_trigger > 0.5) {
            if(gamepad1.x)
                forward = true;
            else if(gamepad1.y)
                forward = false;
        }
    }

    public void driveMotor(double lf, double lb, double rf, double rb) {
        if(!l.isExtended())
            lf = lb = rf = rb = 0;
        if(!forward) {
            lf = -lf;
            lb = -lb;
            rf = -rf;
            rb = -rb;
        }

        frontLeft.setPower(lf);
        frontRight.setPower(rf);
        rearLeft.setPower(lb);
        rearRight.setPower(rb);
    }

    /////////////////////////////////////////////////////

    private boolean lastLeftBumper1 = false;
    private boolean lastLeftBumper2 = false;
    private boolean lastRightBumper2 = false;

    public void lift() {
        // TODO make dpad not go BRRRRRRRRRRRRRRRRRRRRRR
        if (gamepad2.y)
            l.setVerticalTarget(3);
        else if (gamepad2.b) {
            l.retract();
            l.setVerticalTarget(0);
        }
        else if (gamepad2.a)
            l.setVerticalTarget(1);
        else if (gamepad2.x)
            l.setVerticalTarget(2);
        else if (gamepad2.dpad_up)
            l.moveVertical(10);
        else if (gamepad2.dpad_down)
            l.moveVertical(-10);

        if (gamepad2.right_bumper && !lastRightBumper2) l.retract();
        else if (gamepad2.left_bumper && !lastLeftBumper2) l.extend();
        else if (gamepad2.dpad_right) l.moveHorizontal(-5);
        else if (gamepad2.dpad_left) l.moveHorizontal(5);


        // CLAW
        if (gamepad1.left_bumper && !lastLeftBumper1) {
            l.closeClaw();
            sleep(600);
            l.moveVertical(300);
        }
        else if (gamepad1.right_bumper) l.openClaw();

        lastLeftBumper1 = gamepad1.left_bumper;
        lastLeftBumper2 = gamepad2.left_bumper;
        lastRightBumper2 = gamepad2.right_bumper;
        l.update();
    }

    ////////////////////////////////////////////////////////////////////

    public void turret() {
        int b = 0;
        if (l.liftVertical1.getCurrentPosition() < TURRET_THRESHOLD)
            return;

        double speed = gamepad2.left_stick_x * 0.5; //Math.pow(gamepad2.left_stick_x, 2);
        int now = turret.getCurrentPosition() - turret_center;
        //if ((speed < 0 && now > -TURRET_DELTA) || (speed > 0 && now < TURRET_DELTA))
        turret.setPower(speed);
        //else
        //turret.setPower(0);
        // if(turret.getCurrentPosition() == 0)
        //   turret.setPower(0);
        /*if(gamepad2.b){
            //runtime.reset();
            b =1;
            if(turret.getCurrentPosition() > 0){
                turret.setTargetPosition(turret_center);
                turret.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                turret.setPower(-0.5);
            }
            else if(turret.getCurrentPosition() < 0){
                turret.setTargetPosition(0);
                turret.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                turret.setPower(0.5);
            }
        }*/
        telemetry.addData("turret center:", turret_center);
        telemetry.addData("turret position:", now);
        telemetry.addData("turret speed:", speed);
    }
}
