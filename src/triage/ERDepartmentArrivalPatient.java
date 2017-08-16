package triage;

import java.io.IOException;

import triage.room.ERWaitingRoom;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.extention.FuseLightPlugIn;


public class ERDepartmentArrivalPatient extends Thread
{
	private SimulationEngine cEngine;
	private ERWaitingRoom erWaitingRoom;
	private int iPatientRandomMode;
	private int iInverseSimFlag;
	private int iFileWriteMode;
	int iPatientArrivalMode;

	public ERDepartmentArrivalPatient()
	{
		cEngine = null;
		erWaitingRoom = null;
		iPatientRandomMode = 0;
		iInverseSimFlag = 0;
		iFileWriteMode = 0;
		iPatientArrivalMode = 0;
	}

	public void vSetSimulationEngine( SimulationEngine engine )
	{
		cEngine = engine;
	}

	public void vSetWaitingRoom( ERWaitingRoom waitingroom )
	{
		erWaitingRoom = waitingroom;
	}

	@Override
	public void run()
	{
		int i = 0;
		double lfSecond = 0.0;
		for(;;)
		{
			if( cEngine != null )
			{
				// シミュレーションが停止しているときは患者を発生させません。
				if( cEngine.isPaused() == true && cEngine.getLogicalTime() == 0 )
					continue;
				// シミュレーションが実行状態のとき
				if( cEngine.getLatestTimeStep() != 0 )
				{
					// 患者到達分布確率に従って患者を発生させます。
					if( cEngine.isPaused() == false )
					{
						//シミュレーションの現在時刻を取得します。
						lfSecond = cEngine.getLogicalTime()/cEngine.getLatestTimeStep()*0.001;

						// 患者を到達分布にしたがって生成します。(午前8時30分を0秒とする。)
						// 一端停止してから再実行します。
						cEngine.pause();
						try{
						erWaitingRoom.vArrivalPatient( lfSecond, cEngine, iPatientRandomMode, iInverseSimFlag, iFileWriteMode, iPatientArrivalMode );
						}
						catch( IOException ioe ){
							ioe.printStackTrace();
						}
						cEngine.resume();
					}
				}
			}
		}
	}

	/**
	 * <PRE>
	 *    患者の分布モードを設定します。
	 * </PRE>
	 * @param iPatientRandomMode 患者の分布モード
	 */
	public void vSetRandomMode(int iPatientRandomMode)
	{
		// TODO 自動生成されたメソッド・スタブ
		this.iPatientRandomMode = iPatientRandomMode;
	}

	/**
	 * <PRE>
	 *    逆シミュレーションモードを設定します。
	 * </PRE>
	 * @param iExecMode 逆シミュレーションモード
	 */
	public void vSetInverseSimFlag(int iExecMode)
	{
		this.iInverseSimFlag = iExecMode;
	}

	/**
	 * <PRE>
	 *    ファイル書き込みモードを設定します。
	 * </PRE>
	 * @param iFileWriteMode ファイル書き込みモード
	 */
	public void vSetFileWriteMode(int iFileWriteMode)
	{
		this.iFileWriteMode = iFileWriteMode;
	}

	/**
	 * <PRE>
	 *    患者到達分布のモードを設定します。
	 * </PRE>
	 * @param iPatientArrivalMode 患者到達分布モード
	 */
	public void vSetPatientArrivalMode(int iPatientArrivalMode)
	{
		this.iPatientArrivalMode = iPatientArrivalMode;
	}
}
