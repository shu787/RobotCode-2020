
package frc.robot.subClass;

//import edu.wpi.first.wpilibj.Encoder;
//import edu.wpi.first.wpilibj.DigitalInput;
//import edu.wpi.first.wpilibj.SpeedController;
//import edu.wpi.first.wpilibj.PIDController;
//import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.SensorCollection;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
public class Arm{

    //宣言
    TalonSRX Motor;            //モーター
    SensorCollection Encoder;

    ArmSensor armSensor;


    //コンストラクター
    public Arm(TalonSRX ArmMotor, SensorCollection Encoder, ArmSensor armSensor){
        this.Motor = ArmMotor;
        this.Encoder = Encoder;
        this.armSensor = armSensor;
    }

    //---------------------------------------------------------------------------------------------------
    //出力処理
    public void applyState(State state){

        state.armAngle = getArmNow(Encoder.getAnalogInRaw());


        switch(state.armState){
            //---------------------------------------------------------------
            //Armの角度維持 
            case k_Conserve:
                state.armPID_ON = false;
                ArmStop(state.armAngle);
                break;
            //---------------------------------------------------------------
            //砲台の角度をセル発射用に(PID)
            case k_Shoot:
                state.armPID_ON = true;
                ArmPIDMove(state.setArmAngle,getArmNow(Encoder.getAnalogInRaw()));
                state.setArmAngle = Const.armShootAngle;
                break;
            //---------------------------------------------------------------
            //砲台の角度をパネル回転用に(PID)
            case k_Panel:
                state.armPID_ON = true;
                state.setArmAngle = Const.armPanelAngle;
                ArmPIDMove(state.setArmAngle, getArmNow(Encoder.getAnalogInRaw()));
                break;
            //---------------------------------------------------------------
            //砲台の角度を微調整
            case k_LittleAaim:
                state.armPID_ON = false;
                ArmAiming(state);
                break;
            //---------------------------------------------------------------
            //砲台の角度を地面と平行に(PID)
            case k_Parallel:
                state.armPID_ON = true;
                state.setArmAngle = Const.armParallelAngle;
                ArmPIDMove(state.setArmAngle, getArmNow(Encoder.getAnalogInRaw()));
                break;
            //---------------------------------------------------------------
            //基本状態へ(Arm一番下)
            case k_Basic:
                state.armPID_ON = false;
                ArmChangeBasic(state.armAngle);
                break;
            //---------------------------------------------------------------
        }
        //PIDがONの時、設定した角度までPID制御
        if(state.armPID_ON == true){
            ArmPIDMove(state.setArmAngle, state.armAngle);
        }
    }

    //--------------------------------------------------------------------------------
    //砲台の角度を微調整する（PID無し）
    private void ArmAiming(State state){

        if(armSensor.getArmFrontSensor()){
            //------------------------------------------
            //角度が初期状態（-30度）
            if(state.armMotorSpeed > 0){
                ArmMove(Const.ArmBasicUpSpeed);
            }
        }else if(armSensor.getArmBackSensor()){
            //------------------------------------------
            //角度が最も上（80度）
            if(state.armMotorSpeed < 0){
               //Lowspeedで下げると下がらないので少し早め
               ArmMove(Const.ArmHighDownSpeed);
            }
        }else{
            //------------------------------------------
            //角度が-30度～80度
            if(state.armMotorSpeed > 0){
                ArmMove(Const.ArmBasicUpSpeed);     
            }else if(state.armMotorSpeed < 0){
                ArmMove(Const.ArmLowDownSpeed);  
            }else{
                //念のため、入力ナシならStop
                ArmStop(state.armAngle);
            }
        }

    }

    //--------------------------------------------------------------------------------
    //砲台のモーターを回す制御(速度をsetSpeedで決める)（）
    public void ArmMove(double setSpeed){
        Motor.set(ControlMode.PercentOutput, setSpeed);
        SmartDashboard.putNumber("setSpeedMove",setSpeed);
    }

    //重力オフセットを使う事でモーターを停止させる(SetFeedForward()から決める)
    public void ArmStop(double NowAngle){
        Motor.set(ControlMode.PercentOutput, 0, 
                  DemandType.ArbitraryFeedForward, SetFeedForward(NowAngle));
        SmartDashboard.putNumber("Stop",NowAngle);
    }

    //砲台のモーターを回すPID制御(位置をSetPoint()で決める・重力オフセットをSetFeedForward()で決める)
    public void ArmPIDMove(double targetAngle, double nowAngle){

        Motor.set(ControlMode.Position, SetPoint(targetAngle),
                DemandType.ArbitraryFeedForward, SetFeedForward(nowAngle));

    }

    //--------------------------------------------------------------------------------

    //砲台を初期状態にする
    private void ArmChangeBasic(double armAngle){
        if(!armSensor.getArmFrontSensor()){               
            //角度下限認識スイッチが反応したら何も起こらない
            //角度下限認識スイッチが反応してなかったら、回す
            if(armAngle > Const.ArmDownBorderAngle){
                //早く落とす
                SmartDashboard.putNumber("High", armAngle);
                ArmMove(Const.ArmHighDownSpeed);
            }else{
                //ゆっくり落とす
                SmartDashboard.putNumber("Low", armAngle);
                ArmMove(Const.ArmLowDownSpeed);
            }
        }
    }

    //---------------------------------------------------------------------
    //砲台角度制御に関する計算式

    //現在の砲台の角度を計算(この関数上手く動いてくれない)
    //(角度の最大最小差分) ÷（エンコーダー値の最大最小差分) × (エンコーダー現在値 - 最小値) + (角度の最小値)
    private double getArmNow(int armNowPoint){
        return Const.armAngleDifference / Const.armPointDifference *
                (armNowPoint - Const.armMinPoint) + Const.armMinAngle;
    }

    //目標角度に合わせたPIDの目標値を計算
    //(目標角度 - 最小角度) ×（エンコーダー値の最大最小差分) ÷ (角度の最大最小差分) + (0からの差分)
    private double SetPoint(double targetAngle){
        return (targetAngle - Const.armMinAngle) * Const.armPointDifference /
                Const.armAngleDifference + Const.armMinPoint;
    }

    //目標角度に合わせた重力オフセットを計算
    //(地面と水平な時の重力オフセット) × (cos目標角度)
    private double SetFeedForward(double targetAngle){
        return Const.armMaxOffset * Math.cos(Math.toRadians(targetAngle));
    }

    //--------------------------------------------------------------------------------------


}