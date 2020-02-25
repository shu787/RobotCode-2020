package frc.robot.subClass;

public class State {


    public enum ControlState{
        m_ShootingBall, m_PanelRotation, m_Climb, m_Drive

    }

    public enum DriveState{
        kManual,
        kLow,
        kdoNothing
    }
    public enum ShooterState{
        kshoot,
        kintake,
        kmanual,
        doNothing,
        kouttake
    }
    public enum  IntakeState{
        kIntake,
        kouttake,
        doNothing
    }

    public enum IntakeBeltState{
        kIntake,
        kouttake,
        doNothing
    }

    public enum ClimbState{
        doNothing,
        climbExtend,
        climbShrink,
        climbLock,
        climbRightSlide,
        climbLeftSlide
    }

    public enum ArmState{
        k_Basic,           //基本状態（最も下を向いている）
        k_Aaiming,         //砲台の照準を合わせている状態
        k_Maxup            //最も上を向いている状態
    }

    public enum PanelState{
        p_DoNothing,        //stop
        p_ManualRot,        //手動
        p_toBlue,           //色合わせ(大会のパネル側のセンサーの色なのでカラーセンサーが読み取るのは二つずれた値。青<->赤、黄<->緑）
        p_toYellow,
        p_toRed,
        p_toGreen

    }


    public double driveStraightSpeed, driveRotateSpeed;  //Driveの速度;
    public double shooterLeftSpeed,shooterRightSpeed,shooterPIDSpeed;
    public double hangingMotorSpeed;
    public double canonMotorSpeed;
    public double hangingServoAngle;
    public double climbSlideMotorSpeed;
    public double panelManualSpeed;

    public ShooterState shooterState;
    public IntakeState intakeState;
    public IntakeBeltState intakeBeltState;
    public DriveState driveState;
    public ClimbState climbState;
    public ArmState armState;
    public ControlState controlState;
    public PanelState panelState;

    public State(){
        stateInit();
    }

    public void stateInit(){

        //DriveのStateを初期化
        driveState = DriveState.kManual;
        driveRotateSpeed = 0;

        //Shooter
        shooterState = ShooterState.doNothing;
        shooterLeftSpeed = 0;
        shooterRightSpeed = 0;
        shooterPIDSpeed = 0;

        //Intake
        intakeState = IntakeState.doNothing;

        //IntakeBeltState
        intakeBeltState = IntakeBeltState.doNothing;

        //Climb
        climbState = ClimbState.doNothing;
        hangingMotorSpeed = 0;
        canonMotorSpeed = 0;
        hangingServoAngle = 0;
        climbSlideMotorSpeed = 0;

        //Arm
        armState = ArmState.k_Basic;

        panelState = PanelState.p_DoNothing;
        //ContloalMode

        controlState = ControlState.m_Drive;


    }

}