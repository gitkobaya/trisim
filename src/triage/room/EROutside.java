package triage.room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import triage.ERDepartment;
import triage.agent.ERDoctorAgent;
import triage.agent.ERNurseAgent;
import triage.agent.ERNurseAgentException;
import triage.agent.ERPatientAgent;
import utility.initparam.InitSimParam;
import utility.node.ERTriageNode;
import utility.node.ERTriageNodeManager;
import utility.sfmt.Rand;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

public class EROutside extends Agent
{
	private static final long serialVersionUID = 1L;
	private Rand rnd;											// 乱数クラス
	private ArrayList<ERPatientAgent> ArrayListPatientAgents;	// 病院外で待っているエージェント
	private double lfTotalTime;									// シミュレーション経過時間
	private int iDisChargeNum;									// 退院数
	private double lfArrivalPatientPepole;						// 患者到達人数
	private Logger cOutsideLog;									// 病院外のログ出力設定

	private ERTriageNodeManager erTriageNodeManager;
	private ERTriageNode erTriageNode;
	private int iInverseSimFlag;

	// 描画関係
	private int iDrawX;
	private int iDrawY;
	private int iDrawZ;
	private int iDrawWidth;
	private int iDrawHeight;
	private int iDrawF;

	private Object csOutsideCriticalSection;					// クリティカルセクション用

	private int iMonthCount = 0;								// 患者出現分布用１ヶ月換算
	private int iDayCount = 0;									// 患者出現分布用１日換算
	private int iYearCount = 0;									// 患者出現分布用1年換算

	private double lfOneWeekArrivalPatientPepole = 1.0;				// 患者の1日単位での分布
	private double lfOneMonthArrivalPatientPepole = 1.0;			// 患者の1ヶ月単位での分布
	private double lfOneYearArrivalPatientPepole = 1.0;				// 患者の1年単位での分布

	private int iCurrentLeavingPatientNum;						// これから出発する患者の配列番号

	public EROutside()
	{
		vInitialize();
	}

	public void vInitialize()
	{
		ArrayListPatientAgents		= new ArrayList<ERPatientAgent>();
		lfTotalTime					= 0.0;
		iDisChargeNum				= 0;

		iInverseSimFlag = 0;

		iMonthCount = 0;								// 患者出現分布用１ヶ月換算
		iDayCount = 0;									// 患者出現分布用１日換算
		iYearCount = 0;									// 患者出現分布用1年換算

		iCurrentLeavingPatientNum = 0;

	}

	/**
	 * <PRE>
	 *    ファイルの読み込みを行います。
	 * </PRE>
	 * @param strPatientAgentStart	開始時に読み込む患者エージェント用出力ファイル名
	 * @param strPatientAgentEnd	終了時に読み込む患者エージェント用出力ファイル名
	 * @param iFileWriteMode		ファイル出力モード
	 * @throws IOException			ファイル出力例外エラー
	 */
	public void vSetReadWriteFileForAgents( String strPatientAgentStart, String strPatientAgentEnd, int iFileWriteMode ) throws IOException
	{
		ArrayListPatientAgents.get(ArrayListPatientAgents.size()-1).vSetReadWriteFile( iFileWriteMode );
	}

	/**
	 * <PRE>
	 *    終了処理を実行します。
	 * </PRE>
	 * @throws IOException	終了処理エラー
	 */
	public synchronized void vTerminate() throws IOException
	{
		synchronized( csOutsideCriticalSection )
		{
			// 患者エージェントの終了処理を行います。
			if( ArrayListPatientAgents != null )
			{
				for( int i = ArrayListPatientAgents.size()-1; i >= 0; i-- )
				{
					if( ArrayListPatientAgents.get(i) != null )
					{
						ArrayListPatientAgents.get(i).vTerminate();
						this.getEngine().addExitAgent( ArrayListPatientAgents.get(i) );
						ArrayListPatientAgents.set( i, null );
						ArrayListPatientAgents.remove( i );
					}
				}
			}
			ArrayListPatientAgents = null;
			cOutsideLog = null;									// 病院外ログ出力設定
			// 乱数
			rnd = null;											// 乱数クラス
			// FUSEノード、リンク
			erTriageNodeManager = null;
			erTriageNode = null;
			lfTotalTime = 0.0;
			iDisChargeNum = 0;
		}
	}

	/**
	 * <PRE>
	 *   患者エージェントオブジェクトを生成します。
	 * </PRE>
	 */
	public void vCreatePatientAgents()
	{
		ArrayListPatientAgents = new ArrayList<ERPatientAgent>();
	}

	/**
	 * <PRE>
	 *   患者を生成します。
	 * </PRE>
	 * @param lfEndLogicalTime	シミュレーション終了時刻[時間]
	 * @param lfStepTime		ステップ時間
	 * @param engine			シミュレーションエンジン
	 * @param iRandomMode		患者発生分布の方法
	 * @param iInverseSimMode	逆シミュレーションモード
	 * @param iFileWriteMode	ファイル書き込みモード
	 * @param iDisasterFlag		患者到達分布のモード
	 * @param initSimParam		初期設定パラメータクラスのインスタンス
	 * @throws IOException
	 */
	public void vGeneratePatientAgents(double lfEndLogicalTime, double lfStepTime, SimulationEngine engine, int iRandomMode, int iInverseSimMode, int iFileWriteMode, int iDisasterFlag, InitSimParam initSimParam ) throws IOException
	{
		double lfSecond = 0.0;

		iInverseSimFlag = iInverseSimMode;
		for( lfSecond = 0.0; lfSecond < lfEndLogicalTime; lfSecond += 1.0/3600.0 )
		{
			// 患者を到達分布にしたがって生成します。(午前8時30分を0秒とする。)
			vArrivalPatient( lfSecond, 1.0/3600.0, engine, iRandomMode, iInverseSimMode, iFileWriteMode, iDisasterFlag, initSimParam );
		}
	}

	/**
	 * <PRE>
	 *    FUSEエンジンにエージェントを登録します。
	 * </PRE>
	 * @param engine シミュレーションエンジン
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	public void vSetSimulationEngine( SimulationEngine engine )
	{
		engine.addAgent(this);
	}

	/**
	 * <PRE>
	 *    患者発生用の病院外プロセスを実行します。
	 *    指定時間になったら患者を生成して待合室へ移動します。
	 * </PRE>
	 * @param erWaitingRoom				待合室エージェント
	 * @return							常に0を返却
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	public int iImplementOutside( ERWaitingRoom erWaitingRoom )
	{
		int i;
		int iRes = 0;

		synchronized( csOutsideCriticalSection )
		{
		// 退院可能なエージェントのチェックを行います。
			if( ArrayListPatientAgents.isEmpty() == false )
			{
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					// かなりの患者が亡くなられている場合のエラー対策。
					if( ArrayListPatientAgents.size() <= i )	continue;
					if( ArrayListPatientAgents.get(i) == null )	continue;
					// 退院可能な患者エージェントがいる場合は患者エージェントを削除します
					if( ArrayListPatientAgents.get(i).iGetDisChargeFlag() == 1 )
					{
						// なくなられたエージェントが配列上いた位置を削除するため、それ以降のデータがすべて1繰り下がるので、
						// それに対応する。そうしないと配列サイズを超えて参照したエラーが発生します。
						cOutsideLog.info( ArrayListPatientAgents.get(i).getId() + ","  + "病院外：退院しました！。");
						ArrayListPatientAgents.get(i).getEngine().addExitAgent(ArrayListPatientAgents.get(i));
						// 退院数をカウントします。
						iDisChargeNum++;
						// 登録されているエージェントで離脱したエージェントがいるかどうかを判定します。
						if( ArrayListPatientAgents.get(i).iGetDisChargeFlag() == 1 )
						{
							try{
								//いる場合は、ファイルに書き出しを実行します。
								ArrayListPatientAgents.get(i).vFlushFile( 0 );
							}
							catch( IOException ioe ){
							}
						}
						ArrayListPatientAgents.set(i, null);
						ArrayListPatientAgents.remove(i);
					}
				}
			}
			// 患者が到達していない場合は何もしません。
			if( ArrayListPatientAgents.isEmpty() == true ) return iRes;
			if( ArrayListPatientAgents.get(iCurrentLeavingPatientNum).isArraivalTime() == false ) return iRes;

			// 患者が到達する時間になったら、待合室へ移動します。
			vJudgeMoveWaitingRoom( erWaitingRoom,  ArrayListPatientAgents.get(0) );
			// 現在の患者を削除し、次の患者へ移ります。
			ArrayListPatientAgents.remove(0);

		}
//		cWaitingRoomLog.info("ArrayListPatientAgent size:" + ArrayListPatientAgents.size() );
		return iRes;
	}

	/**
	 * <PRE>
	 *    歩いて患者が到達したときの発生分布を表します。
	 * </PRE>
	 * @param engine シミュレーションエンジン
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	public void vArrivalAlonePatient( SimulationEngine engine )
	{
		double lfX,lfY,lfZ;
//		erCurrentPatientAgent = new ERPatientAgent();
		ArrayListPatientAgents.add( new ERPatientAgent() );
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetRandom( rnd );
		// ランダムに患者の容体を割り当てます。
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetRandom();
//		engine.addAgent(ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1));
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetSimulationEngine(engine);
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetMoveRoomFlag( 1 );
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetMoveWaitingTime( 181 );

		// 患者エージェントの位置を設定します。
		lfX = this.getPosition().getX()+10*(2*rnd.NextUnif()-1);
		lfY = this.getPosition().getY()+10*(2*rnd.NextUnif()-1);
		lfZ = this.getPosition().getZ()+10*(2*rnd.NextUnif()-1);
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1).setPosition( lfX, lfY, lfZ );

		return ;
	}

	/**
	 * <PRE>
	 *    患者を生成します。
	 *    通常は独歩来院用発生分布、救急車来院用発生分布です。
	 *    災害時は災害時用の発生分布を使用します。
	 * </PRE>
	 * @param lfTime			経過時間[時間]
	 * @param lfStepTime		時間間隔[時間]
	 * @param engine			シミュレーションエンジン
	 * @param iRandomMode		乱数モード
	 * @param iInverseSimMode	逆シミュレーションモード
	 * @param iFileWriteMode	ファイル書き込みモード
	 * @param iDisasterFlag		災害モード
	 * @param initSimParam		初期設定パラメータ（合わせ込み用）
	 * @throws IOException		例外
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	public void vArrivalPatient( double lfTime, double lfStepTime, SimulationEngine engine, int iRandomMode, int iInverseSimMode, int iFileWriteMode, int iDisasterFlag, InitSimParam initSimParam ) throws IOException
	{
		double lfProb1 = 0.0;
		double lfProb2 = 0.0;
		double lfRand1,lfRand2;
		double lfX,lfY,lfZ;
		double lfOneDay = 24.0;
		double lfPrevOneDay = 24.0;
		double lfMonth = 24.0*31.0;
		double lfPrevOneMonth = 24.0*31.0;
		double lfYear = 365.0*24.0;
		double lfPrevOneYear = 24.0*365.0;
		if( iDisasterFlag == 0 )
		{
			// 1年単位での分布を計算します。
			if( (lfTime % lfYear) < lfStepTime )
			{
				vCalcArrivalDensityWalkOneYear( (double)iYearCount );
				vCalcArrivalDensityAmbulanceOneYear( (double)iYearCount );
				// 1ヶ月終了したら0に戻す。
				if( iYearCount == 365 ) iYearCount = 0;
				iYearCount++;
			}
			// 1ヶ月単位での分布を計算します。
			if( (lfTime % lfMonth) < lfStepTime )
			{
				vCalcArrivalDensityWalkOneMonth( (double)iMonthCount );
				vCalcArrivalDensityAmbulanceOneMonth( (double)iMonthCount );
				// 1ヶ月終了したら0に戻す。
				if( iMonthCount == 31 ) iMonthCount = 0;
				iMonthCount++;
			}
			// １日単位の分布を計算します。
			if( (lfTime % lfOneDay) < lfStepTime )
			{
				vCalcArrivalDensityWalkOneWeek( (double)iDayCount );
				vCalcArrivalDensityAmbulanceOneWeek( (double)iDayCount );
				// 1週間経過したら終了したら0に戻す。
				if( iDayCount == 7 ) iDayCount = 0;
				iDayCount++;
			}
			// 1秒単位の分布の計算をします。
			lfProb1 = lfCalcArrivalDensityWalk( lfTime );
			lfProb2 = lfCalcArrivalDensityAmbulance( lfTime );
			lfRand1 = rnd.NextUnif();
			lfRand2 = rnd.NextUnif();
		}
		else
		{
			// 1ヶ月単位での分布を計算します。
			if( (lfTime % lfMonth) < lfStepTime )
			{
				vCalcArrivalDensityDisasterOneMonth( (double)iMonthCount );
				// 1ヶ月終了したら0に戻す。
				if( iMonthCount == 31 ) iMonthCount = 0;
				iMonthCount++;
			}
			// １日単位の分布を計算します。
			if( (lfTime % lfOneDay) < lfStepTime )
			{
				vCalcArrivalDensityDisasterOneWeek( (double)iDayCount );
				// 1週間経過したら終了したら0に戻す。
				if( iDayCount == 7 ) iDayCount = 0;
				iDayCount++;
			}
			lfPrevOneDay = lfTime % lfOneDay;
			// 1秒単位の分布の計算をします。
			lfProb1 = lfCalcArrivalDensityDisaster( lfTime );
			lfRand1 = rnd.NextUnif();
			lfRand2 = rnd.NextUnif();
		}
		// 患者エージェント生成
		engine.pause();
		if( lfProb1 > lfRand1 )
		{
//			erCurrentPatientAgent = new ERPatientAgent();
			ArrayListPatientAgents.add( new ERPatientAgent() );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetInitParam( initSimParam );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetRandom( rnd );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetPatientRandomMode( iRandomMode );
			// ランダムに患者の容体を割り当てます。
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetRandom();
//			engine.addAgent(ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ));
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetSimulationEngine(engine);
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetMoveRoomFlag( 1 );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetMoveWaitingTime( 181 );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetLocation( 9 );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetArraivalTime( lfTime );
			// 逆シミュレーションモードでなければ以下を実行します。
			iInverseSimFlag = iInverseSimMode;
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetInverseSimMode( iInverseSimFlag );
			if( iInverseSimFlag == 0 || iInverseSimFlag == 1 )
			{
				ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetReadWriteFile( iFileWriteMode );
			}
			// 患者エージェントの位置を設定します。
			lfX = this.getPosition().getX()+10*(2*rnd.NextUnif()-1);
			lfY = this.getPosition().getY()+10*(2*rnd.NextUnif()-1);
			lfZ = this.getPosition().getZ()+10*(2*rnd.NextUnif()-1);
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).setPosition( lfX, lfY, lfZ );
			return;
		}
		// 患者エージェント生成
		if( lfProb2 > lfRand2 )
		{
//			erCurrentPatientAgent = new ERPatientAgent();
			ArrayListPatientAgents.add( new ERPatientAgent() );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetInitParam( initSimParam );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetRandom( rnd );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetPatientRandomMode( iRandomMode );
			// ランダムに患者の容体を割り当てます。
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1).vSetRandom();
//			engine.addAgent(ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ));
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1).vSetSimulationEngine(engine);
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetMoveRoomFlag( 1 );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetMoveWaitingTime( 181 );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetLocation( 9 );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetArraivalTime( lfTime );
			// 逆シミュレーションモードでなければ以下を実行します。
			iInverseSimFlag = iInverseSimMode;
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetInverseSimMode( iInverseSimFlag );
			if( iInverseSimFlag == 0 || iInverseSimFlag == 1 )
			{
				ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetReadWriteFile( iFileWriteMode );
			}
			// 患者エージェントの位置を設定します。
			lfX = this.getPosition().getX()+10*(2*rnd.NextUnif()-1);
			lfY = this.getPosition().getY()+10*(2*rnd.NextUnif()-1);
			lfZ = this.getPosition().getZ()+10*(2*rnd.NextUnif()-1);
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).setPosition( lfX, lfY, lfZ );
		}
		engine.resume();
		return;
	}

	/**
	 * <PRE>
	 *    待合室へ移動可能かどうかを判定して、移動処置を行います。
	 * </PRE>
	 * @param erWaitingRoom				待合室オブジェクト
	 * @param erConsultationDoctorAgent	医師エージェント
	 * @param erPAgent					患者エージェント
	 * @throws NullPointerException		nullアクセス例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveWaitingRoom( ERWaitingRoom erWaitingRoom, ERPatientAgent erPAgent ) throws NullPointerException
	{
		int j;
		ERNurseAgent erWatingRoomNurseAgent;

		// 看護師が全員対応中でもそのまま待合室へ移動します。

		cOutsideLog.info(erPAgent.getId() + "," + "待合室へ移動準備開始" + "," + "外から");
		// 患者のいる位置を待合室に変更します。
		erPAgent.vSetLocation( 9 );

		// 移動開始フラグを設定します。
		erPAgent.vSetMoveRoomFlag( 1 );
		erPAgent.vSetMoveWaitingTime( 0.0 );

		// その患者を対応している医師、看護師エージェントのIDを0に設定します。
		erPAgent.vSetNurseAgent( 0 );
		erPAgent.vSetDoctorAgent( 0 );

		// 患者エージェントを待合室に配置します。
		erWaitingRoom.vSetPatientAgent( erPAgent );

		// 看護師エージェントへ患者情報を送信します。
		for( j = 0;j < erWaitingRoom.iGetNurseAgentsNum(); j++ )
		{
			erWatingRoomNurseAgent = erWaitingRoom.erGetNurseAgent(j);
		}

		cOutsideLog.info(erPAgent.getId() + "," + "待合室へ移動準備終了" + "," + "外から");
		if( iInverseSimFlag == 1 )
		{
			// 移動先の経路を患者エージェントに設定します。
			erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), erWaitingRoom.erGetTriageNode() ) );
			cOutsideLog.info(erPAgent.getId() + "," + "待合室へ移動開始" );
		}
		erPAgent = null;
	}

	/**
	 * <PRE>
	 *　待合室に到達する患者の数を概算で設定します。
	 * </PRE>
	 * @param lfArrivalPatientPepoleData	到達した患者数
	 */
	public void vSetArrivalPatientPepole(double lfArrivalPatientPepoleData)
	{
		// TODO 自動生成されたメソッド・スタブ
		this.lfArrivalPatientPepole = lfArrivalPatientPepoleData;
	}

	/**
	 * <PRE>
	 *    ロガーを設定します。
	 * </PRE>
	 * @param log	ロガークラスインスタンス
	 */
	public void vSetLog(Logger log)
	{
		// TODO 自動生成されたメソッド・スタブ
		cOutsideLog = log;
	}

	/**
	 * <PRE>
	 *    病院外のX座標を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	X座標
	 */
	public int iGetX()
	{
		return iDrawX;
	}

	/**
	 * <PRE>
	 *    病院外のY座標を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	Y座標
	 */
	public int iGetY()
	{
		return iDrawY;
	}

	/**
	 * <PRE>
	 *    病院外の横幅を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	横幅
	 */
	public int iGetWidth()
	{
		return iDrawWidth;
	}

	/**
	 * <PRE>
	 *    病院外の縦幅を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	縦幅
	 */
	public int iGetHeight()
	{
		return iDrawHeight;
	}

	/**
	 * <PRE>
	 *    病院外の階数を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	階数
	 */
	public int iGetF()
	{
		return iDrawF;
	}

	/**
	 * <PRE>
	 *    病院外のX座標を格納します。
	 * </PRE>
	 * @param iData	X座標
	 */
	public void vSetX( int iData )
	{
		iDrawX = iData;
	}

	/**
	 * <PRE>
	 *    病院外のY座標を格納します。
	 * </PRE>
	 * @param iData	Y座標
	 */
	public void vSetY( int iData )
	{
		iDrawY = iData;
	}

	/**
	 * <PRE>
	 *    病院外のZ座標を格納します。
	 * </PRE>
	 * @param iData	Z座標
	 */
	public void vSetZ( int iData )
	{
		iDrawZ = iData;
	}

	/**
	 * <PRE>
	 *   病院外の横幅を格納します。
	 * </PRE>
	 * @param iData	横幅
	 */
	public void vSetWidth( int iData )
	{
		iDrawWidth = iData;
	}

	/**
	 * <PRE>
	 *    病院外の縦幅を格納します。
	 * </PRE>
	 * @param iData	縦幅
	 */
	public void vSetHeight( int iData )
	{
		iDrawHeight = iData;
	}

	/**
	 * <PRE>
	 *    病院外の階数を格納します。
	 * </PRE>
	 * @param iData	階数
	 */
	public void vSetF( int iData )
	{
		iDrawF = iData;
	}

	/**
	 * <PRE>
	 *   病院外に所属しているエージェントの座標を設定します。
	 * </PRE>
	 */
	public void vSetAffiliationAgentPosition()
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;

		double lfX = 0.0;
		double lfY = 0.0;
		double lfZ = 0.0;

		for( i = 0;i < ArrayListPatientAgents.size(); i++ )
		{
			// 患者エージェントの位置を設定します。
			lfX = this.getPosition().getX()+(2*rnd.NextUnif()-1);
			lfY = this.getPosition().getY()+(2*rnd.NextUnif()-1);
			lfZ = this.getPosition().getZ()+(2*rnd.NextUnif()-1);
			ArrayListPatientAgents.get(i).setPosition( lfX, lfY, lfZ );
			ArrayListPatientAgents.get(i).vSetWidth( 15 );
			ArrayListPatientAgents.get(i).vSetHeight( 15 );
		}
	}

	public void vSetSimulationEndTime(double lfEndTime)
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;

		for( i = 0;i < ArrayListPatientAgents.size(); i++ )
		{
			ArrayListPatientAgents.get( i ).vSetSimulationEndTime( lfEndTime );
		}
	}

	/**
	 * <PRE>
	 *    トリアージノードマネージャーを設定します。
	 * </PRE>
	 * @param erNodeManager	ノード、リンクが格納されたノードマネージャのインスタンス
	 */
	public void vSetERTriageNodeManager( ERTriageNodeManager erNodeManager )
	{
		erTriageNodeManager = erNodeManager;
	}

	/**
	 * <PRE>
	 *    現在選択されている病院外のノードを取得します。
	 * </PRE>
	 * @return	病院外のノード
	 */
	public ERTriageNode erGetTriageNode()
	{
		return erTriageNode;
	}

	/**
	 * <PRE>
	 *   病院外のノードを設定します。
	 * </PRE>
	 * @param erNode	設定するノードインスタンス(病院外)
	 */
	public void vSetTriageNode( ERTriageNode erNode )
	{
		erTriageNode = erNode;
	}

	/**
	 * <PRE>
	 *   患者の逆シミュレーションモードを設定します。
	 *    ver 0.1 初版
	 *    ver 0.2 患者配列参照部分のクリティカルセクション追加
	 * </PRE>
	 * @param iMode	0 通常シミュレーションモード
	 * 				1 GUIモード
	 * 				2 逆シミュレーションモード
	 */
	public synchronized void vSetPatientInverseSimMode( int iMode )
	{
		if( ArrayListPatientAgents.size() > 0 )
		{
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetInverseSimMode( iMode );
		}
	}

	/**
	 * <PRE>
	 *   乱数クラスのインスタンスを設定します。
	 * </PRE>
	 * @param sfmtRandom 乱数クラスのインスタンス
	 */
	public void vSetRandom(Rand sfmtRandom)
	{
		rnd = sfmtRandom;
	}

	/**
	 * <PRE>
	 *    クリティカルセクションを設定します。
	 * </PRE>
	 * @param csCriticalSection クリティカルセクションのハンドル
	 */
	public void vSetCriticalSection( Object csCriticalSection )
	{
		csOutsideCriticalSection = csCriticalSection;
	}

	/**
	 * <PRE>
	 *   1観察室に滞在している全患者エージェントを取得します。
	 * </PRE>
	 * @return 観察室に所属する全看護師エージェント
	 * @author kobayashi
	 * @since 2015/11/09
	 */
	public ArrayList<ERPatientAgent> erGetPatientAgents()
	{
		// TODO 自動生成されたメソッド・スタブ
		return ArrayListPatientAgents;
	}

	/**
	 * <PRE>
	 *   該当番号に当たる患者エージェントを取得します。
	 * </PRE>
	 * @param iLoc 患者の番号
	 * @return	該当する患者エージェントインスタンス
	 * @author kobayashi
	 * @since 2015/11/09
	 */
	public ERPatientAgent erGetPatientAgent(int iLoc)
	{
		// TODO 自動生成されたメソッド・スタブ
		return ArrayListPatientAgents.get(iLoc);
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////演算関係

	/**
	 * <PRE>
	 *    独歩で外来した患者の到達分布を算出します。
	 *    exp(-(t-μ)/s)/(s*(1+exp(-(t-μ)/s))*(1+exp(-(t-μ)/s)))
	 *    μ = 11.75, s = 4
	 *    出展元、聖隷浜松病院のデータを基に推測
	 * </PRE>
	 * @param lfTime 経過時間[単位は時間]
	 * @return	患者到達確率
	 */
	private double lfCalcArrivalDensityWalk( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 11.75;
		double lfS		= 4;
		double lfExp	= 0.0;

		if( lfTime >= 24.0 )	lfTime = 0.0;

		lfExp = Math.exp( -(lfTime-lfMu)/lfS );
		lfProb = lfExp/(lfS*(1.0+lfExp)*(1.0+lfExp));
		return lfProb/lfArrivalPatientPepole*lfOneWeekArrivalPatientPepole*lfOneMonthArrivalPatientPepole*lfOneYearArrivalPatientPepole;
	}

	/**
	 * <PRE>
	 *    独歩で外来した患者の到達分布を算出します。(1日単位)
	 *    出展元
	 * </PRE>
	 * @param lfTime 経過時間[単位は日]
	 */
	private void vCalcArrivalDensityWalkOneWeek( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 11.75;
		double lfS		= 4;
		double lfExp	= 0.0;
		double lfOneWeek = 7.0;

		if( lfTime >= 7.0 )	lfTime = lfTime % lfOneWeek;

		lfOneWeekArrivalPatientPepole = 1.0;
	}

	/**
	 * <PRE>
	 *    独歩で外来した患者の到達分布を算出します。(1ヶ月単位)
	 *    出展元
	 * </PRE>
	 * @param lfTime 経過時間[単位は日]
	 */
	private void vCalcArrivalDensityWalkOneMonth( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 11.75;
		double lfS		= 4;
		double lfExp	= 0.0;
		double lfOneMonth = 31.0;

		if( lfTime >= 31.0 )	lfTime = lfTime % lfOneMonth;

		lfOneMonthArrivalPatientPepole = 1;

	}

	/**
	 * <PRE>
	 *    独歩で外来した患者の到達分布を算出します。(年間単位)
	 *    exp(-(t-μ)/s)/(s*(1+exp(-(t-μ)/s))*(1+exp(-(t-μ)/s)))
	 *    μ = 11.75, s = 4
	 *    出展元、聖隷浜松病院のデータを基に推測
	 * </PRE>
	 * @param lfTime 経過時間[単位は日]
	 */
	private void vCalcArrivalDensityWalkOneYear( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 11.75;
		double lfS		= 4;
		double lfExp	= 0.0;
		double lfOneYear = 365.0;

		if( lfTime >= 365.0 )	lfTime = lfTime % lfOneYear;

		lfOneYearArrivalPatientPepole = 1.0;
	}

	/**
	 * <PRE>
	 *    救急車搬送されて到達した患者の到達分布を算出します。
	 *    exp(-(t-μ)/s)/(s*(1+exp(-(t-μ)/s))*(1+exp(-(t-μ)/s)))
	 *    μ = 9.5, s = 3.8
	 *    出展元、聖隷浜松病院のデータを基に推測
	 * </PRE>
	 * @param lfTime 経過時間[単位は時間]
	 * @return	患者到達確率
	 */
	private double lfCalcArrivalDensityAmbulance( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 9.5;
		double lfS		= 3.8;
		double lfExp	= 0.0;

		if( lfTime >= 24.0 ) lfTime = 0.0;

		lfExp = Math.exp( -(lfTime-lfMu)/lfS );
		lfProb = lfExp/(lfS*(1.0+lfExp)*(1.0+lfExp));
		return lfProb/lfArrivalPatientPepole*lfOneWeekArrivalPatientPepole*lfOneMonthArrivalPatientPepole*lfOneYearArrivalPatientPepole;
	}

	/**
	 * <PRE>
	 *    救急車搬送されて到達した患者の到達分布を算出します。(1週間単位)
	 *    出展元
	 * </PRE>
	 * @param lfTime 経過時間[単位は日]
	 */
	private void vCalcArrivalDensityAmbulanceOneWeek( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 9.5;
		double lfS		= 3.8;
		double lfExp	= 0.0;
		double lfOneWeek = 7.0;

		if( lfTime >= 7.0 ) lfTime = lfTime % lfOneWeek;

		lfOneWeekArrivalPatientPepole = 1.0;
	}

	/**
	 * <PRE>
	 *    救急車搬送されて到達した患者の到達分布を算出します。(1ヶ月単位)
	 *    出展元
	 * </PRE>
	 * @param lfTime 経過時間[単位は日]
	 */
	private void vCalcArrivalDensityAmbulanceOneMonth( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 9.5;
		double lfS		= 3.8;
		double lfExp	= 0.0;
		double lfOneMonth = 31.0;

		if( lfTime >= 31.0 ) lfTime = lfTime % lfOneMonth;

		lfOneMonthArrivalPatientPepole = 1.0;
	}

	/**
	 * <PRE>
	 *    救急車搬送されて到達した患者の到達分布を算出します。(1年単位)
	 *    出展元
	 * </PRE>
	 * @param lfTime 経過時間[単位は日]
	 */
	private void vCalcArrivalDensityAmbulanceOneYear( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 9.5;
		double lfS		= 3.8;
		double lfExp	= 0.0;
		double lfOneYear = 365.0;

		if( lfTime >= 365.0 ) lfTime = lfTime % lfOneYear;

		lfOneYearArrivalPatientPepole = 1.0;
	}

	/**
	 * <PRE>
	 *    災害発生時に外来した患者の到達分布を算出します。(1日単位)
	 *    Γ(t,α,β)から算出。tは時間。
	 *    λ = 2.259, α = 2.18
	 *   出展元
	 *   池内淳子,矢田雅子,権丈（武井）英理子,東原紘道
	 *   大規模地震災害時における病院間の傷病者搬送に関する考察-阪神・淡路大震災時における分析を通して-
	 *   地域安全学会論文集 No.19, 2012.3
	 *   これの西病院を参照。
	 * </PRE>
	 * @param lfTime 経過時間[単位は時間]
	 * @return	患者到達確率
	 */
	private double lfCalcArrivalDensityDisaster( double lfTime )
	{
		double lfProb	= 0.0;
		double lfAlpha = 2.18;
		double lfBeta = 2.2259;

		if( lfTime >= 24.0 )	lfTime = 0.0;

		lfProb = lfGammaDensity( lfTime, lfAlpha, lfBeta );
		return lfProb*lfOneWeekArrivalPatientPepole*lfOneMonthArrivalPatientPepole/lfArrivalPatientPepole;
	}

	/**
	 * <PRE>
	 *    災害発生時に外来した患者の到達分布を算出します。(1週間単位)
	 *    平成７年６月阪神淡路大震災災害医療実態アンケート調査結果をもとに算出。
	 *
	 * </PRE>
	 * @param lfTime 経過時間[単位は1日]
	 */
	private void vCalcArrivalDensityDisasterOneWeek( double lfTime )
	{
		double lfProb	= 0.0;
		double lfAlpha = 2.18;
		double lfBeta = 2.2259;
		double lfOneWeek = 7.0;

		if( lfTime >= 7.0 )	lfTime = lfTime % lfOneWeek;

		if( lfTime >= 6.0 ) lfOneWeekArrivalPatientPepole = 0.814751449;
		else if( lfTime >= 5.0 ) lfOneWeekArrivalPatientPepole = 0.262522574;
		else if( lfTime >= 4.0 ) lfOneWeekArrivalPatientPepole = 0.562589107;
		else if( lfTime >= 3.0 ) lfOneWeekArrivalPatientPepole = 0.723315274;
		else if( lfTime >= 2.0 ) lfOneWeekArrivalPatientPepole = 0.695276114;
		else if( lfTime >= 1.0 ) lfOneWeekArrivalPatientPepole = 0.756201882;
		else if( lfTime >= 0.0 ) lfOneWeekArrivalPatientPepole = 1;


//		System.out.println(lfProb);

	}

	/**
	 * <PRE>
	 *    災害発生時に外来した患者の到達分布を算出します。(1ヶ月単位)
	 *    データがないため1として実質動かないようにする。
	 *
	 * </PRE>
	 * @param lfTime 経過時間[単位は1ヶ月]
	 */
	private void vCalcArrivalDensityDisasterOneMonth( double lfTime )
	{
		double lfProb	= 0.0;
		double lfAlpha = 2.18;
		double lfBeta = 2.2259;
		double lfOneMonth = 31.0;

		if( lfTime >= 31.0 )	lfTime = lfTime % lfOneMonth;

		lfOneMonthArrivalPatientPepole = 1.0;
	}

	/**
	 * <PRE>
	 *    ガンマ分布の算出
	 *    奥村晴彦, C言語によるアルゴリズム辞典より
	 * </PRE>
	 * @param lfX		確率変数
	 * @param lfAlpha	α
	 * @param lfBeta	β
	 * @return			ガンマ分布値
	 */
	private double lfGammaDensity(double lfX, double lfAlpha, double lfBeta )
	{
		double lfRes = 0.0;
		lfRes = Math.pow( lfX, lfAlpha - 1.0 )*Math.exp( -lfX/lfBeta )/(Math.pow(lfBeta, lfAlpha)*lfGamma(lfX));
		return lfRes;
	}

	/**
	 * <PRE>
	 *    対数ガンマ関数を算出します。
	 *    奥村晴彦, C言語によるアルゴリズム辞典より
	 * </PRE>
	 * @param lfX	変数
	 * @return		対数ガンマ値
	 */
	private double lfLogGamma( double lfX )
	{
		double lfLog2pi = 1.83787706640934548;
		int iN = 8;
		double lfB0 = 1.0;
		double lfB1 = -0.5;
		double lfB2 = 0.16666666666666666666666666;
		double lfB4 = -0.0333333333333333333333333;
		double lfB6 = 0.023809524;
		double lfB8 = lfB4;
		double lfB10 = 5.0/66.0;
		double lfB12 = -891.0/2730.0;
		double lfB14 = 7.0/6.0;
		double lfB16 = -3617.0/520.0;

		double lfV, lfW;

		lfV = 1;
		while( lfX < iN ){ lfV *= lfX; lfX++;}
		lfW = 1.0/(lfX*lfX);

		return (((((lfB16/(16.0*15.0))*lfW + (lfB14/(14.0*13.0)) )*lfW
				    + (lfB12/(12.0*11.0))*lfW + (lfB10/(10.0* 9.0)) )*lfW
				    + ( lfB8/( 8.0* 7.0))*lfW + ( lfB6/( 6.0* 5.0)) )*lfW
				    + ( lfB4/( 4.0* 3.0))*lfW + ( lfB2/( 2.0* 1.0)) )/lfX
				    + 0.5*lfLog2pi - Math.log( lfV ) - lfX + ( lfX - 0.5 )*Math.log( lfX );
	}

	/**
	 * <PRE>
	 *    ガンマ関数を算出します。
	 *    奥村晴彦, C言語によるアルゴリズム辞典より
	 * </PRE>
	 * @param lfX	変数
	 * @return		対数ガンマ値
	 */
	private double lfGamma( double lfX )
	{
		if( lfX < 0.0 )
		{
			return Math.PI/( Math.sin(Math.PI*lfX)*Math.exp(lfLogGamma( 1.0-lfX)) );
		}
		return Math.exp(lfLogGamma(lfX));
	}


//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////エージェント実行関数メイン

	@Override
	public void action(long timeStep)
	{
		double lfSecond = 0.0;
		lfSecond = timeStep / 1000.0;
		// TODO 自動生成されたメソッド・スタブ

		if( ArrayListPatientAgents == null )	return ;
		if( ArrayListPatientAgents.size() <= 0 )return ;

		try
		{
			synchronized( csOutsideCriticalSection )
			{
				for( int i = ArrayListPatientAgents.size()-1; i >= 0; i-- )
				{
					// 死亡フラグが立っている場合は患者エージェントを削除します。
					if( ArrayListPatientAgents.get(i) != null )
					{
						if( ArrayListPatientAgents.get(i).iGetSurvivalFlag() == 0 )
						{
							ArrayListPatientAgents.set(i, null);
							ArrayListPatientAgents.remove(i);
							break;
						}
						else if( ArrayListPatientAgents.get(i).iGetDisChargeFlag() == 1 )
						{
							break;
						}
						// 登録されているエージェントで離脱したエージェントがいるかどうかを判定します。
						if( ArrayListPatientAgents.get(i).isExitAgent() == true )
						{
							// いる場合は、ファイルに書き出しを実行します。
							ArrayListPatientAgents.get(i).vFlushFile( 0 );
						}
					}
				}
			}
		}
		catch( IOException ioe )
		{

		}
		lfSecond = timeStep/1000.0;
		lfTotalTime += lfSecond;
	}

	/**
	 * <PRE>
	 *    救急部門インスタンスのアドレスを設定します。
	 * </PRE>
	 * @param erDepartment 救急部門のインスタンス
	 */
	public void vSetErDepartmentPatientAgents(ERDepartment erDepartment)
	{
		int i;
		for( i = 0;i < ArrayListPatientAgents.size(); i++ )
		{
			ArrayListPatientAgents.get(i).vSetErDepartment( erDepartment );
		}
	}
}
