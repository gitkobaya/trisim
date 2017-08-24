package triage;

import java.io.IOException;
import java.util.logging.Logger;

import triage.room.ERWaitingRoom;
import utility.initparam.InitSimParam;
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

	private InitSimParam initSimParam;

	private Object csCriticalSection;
	private Logger cLogERDepartmentArrivalPatient;

	public ERDepartmentArrivalPatient()
	{
		cEngine = null;
		erWaitingRoom = null;
		iPatientRandomMode = 0;
		iInverseSimFlag = 0;
		iFileWriteMode = 0;
		iPatientArrivalMode = 0;
	}

	@Override
	public void run()
	{
		int i = 0;
		long iCount = 0;
		double lfSecond = 0.0;
		double lfPrevSecond = 0.0;
		try
		{
			for(;;)
			{
				Thread.sleep(100);
				// シミュレーションエンジンインスタンスが生成されていないときは患者を発生させません。
				if( cEngine == null ) continue;
				// シミュレーションが停止しているときは患者を発生させません。
				if( cEngine.isPaused() == true && cEngine.getLogicalTime() == 0 ) continue;
				// シミュレーションが実行状態でないときは動作しません。
				if( cEngine.getLatestTimeStep() == 0 ) continue;
				// 患者到達分布確率に従って患者を発生させます。
				//シミュレーションの現在時刻を取得します。
				lfSecond = cEngine.getLogicalTime()/cEngine.getLatestTimeStep();
				iCount = cEngine.getLatestTimeStep()/1000L;
				lfSecond /= (3600.0/(double)iCount);
				if( lfSecond == lfPrevSecond )
				{
					iCount = 0;
					lfPrevSecond = lfSecond;
					continue;
				}
				// 患者を到達分布にしたがって生成します。(午前8時30分を0秒とする。)
				// 一端停止してから再実行します。
				cEngine.pause();
				for( i = 0;i < iCount; i++ )
				{
					erWaitingRoom.vArrivalPatient( lfSecond, cEngine, iPatientRandomMode, iInverseSimFlag, iFileWriteMode, iPatientArrivalMode, initSimParam );
					int iSize = erWaitingRoom.erGetPatientAgents().size();
					// 追加でログ出力及びクリティカルセクションの設定をします。
					// 設定の関係上でnull値になっているため。
					if( iSize > 0 )
					{
						erWaitingRoom.erGetPatientAgent(iSize-1).vSetLog(cLogERDepartmentArrivalPatient);
						erWaitingRoom.erGetPatientAgent(iSize-1).vSetCriticalSection(csCriticalSection);
					}
				}
				iCount = 0;
				cEngine.resume();
				lfPrevSecond = lfSecond;
			}
		}
		catch( IOException ioe )
		{
			ioe.printStackTrace();
		}
		catch( InterruptedException ite )
		{
			ite.printStackTrace();
		}
	}

	/**
	 * <PRE>
	 *   シミュレーションエンジンを登録します。
	 * </PRE>
	 * @param engine シミュレーションエンジン
	 */
	public void vSetSimulationEngine( SimulationEngine engine )
	{
		cEngine = engine;
	}

	/**
	 * <PRE>
	 *    待合室インスタンスを設定します。
	 * </PRE>
	 * @param waitingroom 待合室インスタンス
	 */
	public void vSetWaitingRoom( ERWaitingRoom waitingroom )
	{
		erWaitingRoom = waitingroom;
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


	/**
	 * <PRE>
	 *    初期設定パラメータクラスを設定します。
	 * </PRE>
	 * @param initparam 初期設定パラメータクラス
	 */
	public void vSetInitSimParam(InitSimParam initparam)
	{
		this.initSimParam = initparam;
	}

	/**
	 * <PRE>
	 *    ログ出力クラスのインスタンスを設定します。
	 * </PRE>
	 * @param cLogData ログ出力クラスインスタンス
	 */
	public void vSetLogger(Logger cLogData)
	{
		cLogERDepartmentArrivalPatient = cLogData;
	}

	/**
	 * <PRE>
	 *    クリティカルセクションのインスタンスを設定します。
	 * </PRE>
	 * @param cObject クリティカルセクションインスタンス
	 */
	public void vSetCriticalSection(Object cObject)
	{
		csCriticalSection = cObject;
	}
}
