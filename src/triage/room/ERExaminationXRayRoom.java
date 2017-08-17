package triage.room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import triage.agent.ERClinicalEngineerAgent;
import triage.agent.ERClinicalEngineerAgentException;
import triage.agent.ERDoctorAgent;
import triage.agent.ERNurseAgent;
import triage.agent.ERPatientAgent;
import utility.node.ERTriageNode;
import utility.node.ERTriageNodeManager;
import utility.sfmt.Rand;

public class ERExaminationXRayRoom extends Agent
{
	private static final long serialVersionUID = -2280826609228507294L;

	private ERPatientAgent erCurrentPatientAgent;								// 現在対応している患者
	private ArrayList<ERClinicalEngineerAgent> ArrayListClinicalEngineerAgents;	// 現在所属している医療技師エージェント
	private ERClinicalEngineerAgent erCurrentClinicalEngineerAgent;				// 担当している医療技師エージェント
	private int iAttachedClinicalEngineerNum;
	private double lfTotalTime;
	private Logger cXRayRoomLog;										// ログクラス
	private Rand rnd;													// 乱数クラス

	private ERTriageNodeManager erTriageNodeManager;
	private ERTriageNode erTriageNode;
	private int iInverseSimFlag;

	// 描画関係
	private int iDrawX;
	private int iDrawY;
	private int iDrawZ;
	private int iDrawCenterX;
	private int iDrawCenterY;
	private int iDrawWidth;
	private int iDrawHeight;
	private int iDrawF;

	private Object csXRayRoomCriticalSection;							// クリティカルセクション用

	private double lfLastBedTime;
	private double lfLongestTotalTime;

	public ERExaminationXRayRoom()
	{
		vInitialize();
	}

	/**
	 * <PRE>
	 *   検査室の医療技師エージェントを生成します。
	 * </PRE>
	 * @param iAttachedClinicalEngineerNumData 医療技師エージェント数
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	public ERExaminationXRayRoom( int iAttachedClinicalEngineerNumData )
	{
		vInitialize( iAttachedClinicalEngineerNumData );
	}

	/**
	 * <PRE>
	 *   検査室の医療技師エージェントを生成します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	public void vInitialize()
	{
		erCurrentPatientAgent			= null;
		ArrayListClinicalEngineerAgents = new ArrayList<ERClinicalEngineerAgent>();
		erCurrentClinicalEngineerAgent	= null;										// 担当している医療技師エージェント
		iAttachedClinicalEngineerNum	= 0;
		lfTotalTime						= 0;
//		long seed;
//		seed = System.currentTimeMillis();
//		rnd = null;
//		rnd = new Sfmt( (int)seed );

		iInverseSimFlag = 0;
	}

	/**
	 * <PRE>
	 *   検査室の医療技師エージェントを生成します。
	 * </PRE>
	 * @param iAttachedClinicalEngineerNumData 医療技師エージェント数
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	public void vInitialize( int iAttachedClinicalEngineerNumData )
	{
		int i;
		iAttachedClinicalEngineerNum = iAttachedClinicalEngineerNumData;

		ArrayListClinicalEngineerAgents = new ArrayList<ERClinicalEngineerAgent>();
		for( i = 0;i < iAttachedClinicalEngineerNum; i++ )
		{
			ArrayListClinicalEngineerAgents.add( new ERClinicalEngineerAgent() );
		}
		// 先頭の担当医療技師エージェントを指定します。
		erCurrentClinicalEngineerAgent = ArrayListClinicalEngineerAgents.get(0);

		iInverseSimFlag = 0;
	}

	/**
	 * <PRE>
	 *    ファイルの読み込みを行います。
	 * </PRE>
	 * @param strDoctorAgentDirectory			医師エージェントの出力用ディレクトリパス
	 * @param strNurseAgentDirectory			看護師エージェントの出力用ディレクトリパス
	 * @param strClinicalEngineerAgentDirectory	医療技師エージェントの出力用ディレクトリパス
	 * @param iFileWriteMode					ファイル書き込みモード
	 * 											0 1ステップごとのデータを書き込み
	 * 											1 最初と最後各100ステップ程度のデータを書き込み
	 * @throws IOException						ファイル書き込みエラー
	 */
	public void vSetReadWriteFileForAgents( String strDoctorAgentDirectory, String strNurseAgentDirectory, String strClinicalEngineerAgentDirectory, int iFileWriteMode ) throws IOException
	{
		int i;

		for( i = 0;i < ArrayListClinicalEngineerAgents.size(); i++ )
		{
			ArrayListClinicalEngineerAgents.get(i).vSetReadWriteFile( iFileWriteMode );
		}
	}

	/**
	 * <PRE>
	 *    終了処理を実行します。
	 * </PRE>
	 * @throws IOException	java標準IOクラス例外
	 */
	public synchronized void vTerminate() throws IOException
	{
		int i;

		synchronized( csXRayRoomCriticalSection )
		{
			// 患者エージェントの終了処理を行います。
			if( erCurrentPatientAgent != null )
			{
				erCurrentPatientAgent.vTerminate();
				this.getEngine().addExitAgent( erCurrentPatientAgent );
				erCurrentPatientAgent = null;
			}
			// 医療技師エージェントの終了処理を行います。
			if( ArrayListClinicalEngineerAgents != null )
			{
				for( i = ArrayListClinicalEngineerAgents.size()-1; i >= 0; i-- )
				{
					if( ArrayListClinicalEngineerAgents.get(i) != null )
					{
						ArrayListClinicalEngineerAgents.get(i).vTerminate();
						this.getEngine().addExitAgent( ArrayListClinicalEngineerAgents.get(i) );
						ArrayListClinicalEngineerAgents.set( i, null );
						ArrayListClinicalEngineerAgents.remove(i);
					}
				}
				ArrayListClinicalEngineerAgents = null;
			}
			// ログ出力
			cXRayRoomLog = null;									// X線室ログ出力設定

			// 乱数
			rnd = null;												// 乱数クラス

			// FUSEノード、リンク
			erTriageNodeManager = null;
			erTriageNode = null;
			lfTotalTime = 0.0;
		}
	}

	/**
	 * <PRE>
	 *   検査室の医療技師エージェントを生成します。
	 * </PRE>
	 * @param iClinicalEngineerAgentNum 医療技師エージェント数
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vCreateClinicalEngineerAgents( int iClinicalEngineerAgentNum )
	{
		int i;
		if( ArrayListClinicalEngineerAgents == null )
		{
			// 逆シミュレーション時のパラメータ更新を実施し、再シミュレーション実施時に通ります。
			ArrayListClinicalEngineerAgents = new ArrayList<ERClinicalEngineerAgent>();
		}
		for( i = 0;i < iClinicalEngineerAgentNum; i++ )
		{
			ArrayListClinicalEngineerAgents.add( new ERClinicalEngineerAgent() );
		}
	}

	/**
	 * <PRE>
	 *    作業する医療技師エージェントを指定します。
	 * </PRE>
	 * @param iLoc	該当する医療技師エージェントの番号
	 */
	public void vSetCurrentClinicalEngineerAgent( int iLoc )
	{
		if( iLoc < ArrayListClinicalEngineerAgents.size() )
		{
			erCurrentClinicalEngineerAgent = ArrayListClinicalEngineerAgents.get(iLoc);
		}
	}

	/**
	 * <PRE>
	 *    医師エージェントのパラメータを設定します。
	 * </PRE>
	 * @param alfYearExperience			経験年数
	 * @param alfConExperience			経験数の重み
	 * @param alfExperienceRate1		経験年数パラメータ1
	 * @param alfExperienceRate2		経験年数パラメータ2
	 * @param alfConExperienceAIS		重症度判定パラメータ1
	 * @param alfExperienceRateAIS1		重症度判定パラメータ2
	 * @param alfExperienceRateAIS2		重症度判定パラメータ3
	 * @param alfConTired1				疲労度パラメータ1
	 * @param alfConTired2				疲労度パラメータ2
	 * @param alfConTired3				疲労度パラメータ3
	 * @param alfConTired4				疲労度パラメータ4
	 * @param alfTiredRate				疲労度重み
	 * @param alfAssociationRate		関連性パラメータ
	 * @param alfExaminationTime		診察時間
	 * @param aiDepartment				所属部署
	 * @param aiRoomNumber				所属部屋番号
	 * @throws ERClinicalEngineerAgentException	医療技師エージェント例外クラス
	 * @author kobayashi
	 * @since 2015/08/10
	 * @version 0.1
	 */
	public void vSetClinicalEngineerAgentParameter( double[] alfYearExperience,
			double[] alfConExperience,
			double[] alfExperienceRate1,
			double[] alfExperienceRate2,
			double[] alfConExperienceAIS,
			double[] alfExperienceRateAIS1,
			double[] alfExperienceRateAIS2,
			double[] alfConTired1,
			double[] alfConTired2,
			double[] alfConTired3,
			double[] alfConTired4,
			double[] alfTiredRate,
			double[] alfAssociationRate,
			double[] alfExaminationTime,
			int[] aiDepartment,
			int[] aiRoomNumber ) throws ERClinicalEngineerAgentException
	{
		int i;

		for( i = 0;i < ArrayListClinicalEngineerAgents.size(); i++ )
		{
			ArrayListClinicalEngineerAgents.get(i).vSetConExperience( alfConExperience[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetYearExperience( alfYearExperience[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetConTired1( alfConTired1[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetConTired2( alfConTired2[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetConTired3( alfConTired3[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetConTired4( alfConTired4[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetTiredRate( alfTiredRate[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetAssociationRate( alfAssociationRate[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetClinicalEngineerDepartment( aiDepartment[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetExperienceRate1( alfExperienceRate1[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetExperienceRate2( alfExperienceRate2[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetConExperienceAIS( alfConExperienceAIS[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetExperienceRateAIS1( alfExperienceRateAIS1[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetExperienceRateAIS2( alfExperienceRateAIS2[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetRoomNumber( aiRoomNumber[i] );
			ArrayListClinicalEngineerAgents.get(i).vSetExaminationTime();
//			ArrayListClinicalEngineerAgents.get(i).vSetExaminationTime( alfExaminationTime[i] );
		}
	}

	/**
	 * <PRE>
	 *    FUSEエンジンにエージェントを登録します。
	 * </PRE>
	 * @param engine	FUSEシミュレーションエンジン
	 */
	public void vSetSimulationEngine( SimulationEngine engine )
	{
		engine.addAgent(this);
	}

	/**
	 * <PRE>
	 *    検査を実行します。
	 * </PRE>
	 * @param ArrayListEmergencyRooms					全初療室
	 * @param ArrayListSereveInjuryObservationRooms		全重症観察室
	 * @param ArrayListConsultationRooms				全診察室
	 * @param ArrayListOperationRooms					全手術室
	 * @param erWaitingRoom								待合室
	 * @throws ERClinicalEngineerAgentException			医療技師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vImplementExaminationRoom( ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSereveInjuryObservationRooms, ArrayList<ERConsultationRoom> ArrayListConsultationRooms, ArrayList<EROperationRoom> ArrayListOperationRooms, ERWaitingRoom erWaitingRoom ) throws ERClinicalEngineerAgentException
	{
		synchronized( csXRayRoomCriticalSection )
		{
			// 患者が検査室にいれば検査を実行します。
			if( erCurrentPatientAgent != null )
			{
				cXRayRoomLog.info(erCurrentPatientAgent.getId() + "," + "検査室対応中" );
				vImplementExamination( ArrayListEmergencyRooms, ArrayListSereveInjuryObservationRooms, ArrayListConsultationRooms, ArrayListOperationRooms, erWaitingRoom, erCurrentClinicalEngineerAgent, erCurrentPatientAgent );
				// 医師の対応が終了した場合、処置が終了した患者エージェントを削除します。
				if( erCurrentClinicalEngineerAgent.iGetAttending() == 0 )
				{
					erCurrentPatientAgent = null;
				}
			}
		}
	}

	/**
	 * <PRE>
	 *    医療技師エージェントの検査を実行します。
	 * </PRE>
	 * @param ArrayListEmergencyRooms					全初療室
	 * @param ArrayListSereveInjuryObservationRooms		全重症観察室
	 * @param ArrayListConsultationRooms				全診察室
	 * @param ArrayListOperationRooms					全手術室
	 * @param erWaitingRoom								待合室
	 * @param erClinicalEngineerAgent					担当した医療技師エージェント
	 * @param erPatientAgent							受検する患者エージェント
	 * @throws ERClinicalEngineerAgentException			医療技師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vImplementExamination( ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSereveInjuryObservationRooms, ArrayList<ERConsultationRoom> ArrayListConsultationRooms, ArrayList<EROperationRoom> ArrayListOperationRooms, ERWaitingRoom erWaitingRoom, ERClinicalEngineerAgent erClinicalEngineerAgent, ERPatientAgent erPatientAgent ) throws ERClinicalEngineerAgentException
	{
		// 医療技師エージェントの対応を実施します。
		erClinicalEngineerAgent.vSetAttending( 1 );

		if( erCurrentPatientAgent.isMoveWaitingTime() == false )
		{
			// 移動時間がまだ終了していないので、移動を実施しません。
			erClinicalEngineerAgent.vSetPatientMoveWaitFlag( 1 );
			cXRayRoomLog.info(erCurrentPatientAgent.getId() + "," + "X線室移動時間：" + erCurrentPatientAgent.lfGetMoveWaitingTime() );
			return;
		}
		// 部屋移動が終了したのでフラグOFFに処置中とします。
		erPatientAgent.vSetMoveRoomFlag( 0 );
		erClinicalEngineerAgent.vSetPatientMoveWaitFlag( 0 );

		vExaminationProcess( ArrayListEmergencyRooms, ArrayListSereveInjuryObservationRooms, ArrayListConsultationRooms, ArrayListOperationRooms, erWaitingRoom, erClinicalEngineerAgent, erPatientAgent );
	}

	/**
	 *
	 * @param ArrayListEmergencyRooms					全初療室
	 * @param ArrayListSereveInjuryObservationRooms		全重症観察室
	 * @param ArrayListConsultationRooms				全診察室
	 * @param ArrayListOperationRooms					全手術室
	 * @param erWaitingRoom								待合室
	 * @param erClinicalEngineerAgent					担当医療技師エージェント
	 * @param erPatientAgent							受検する患者エージェント
	 * @throws ERClinicalEngineerAgentException			医療技師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vExaminationProcess( ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSereveInjuryObservationRooms, ArrayList<ERConsultationRoom> ArrayListConsultationRooms, ArrayList<EROperationRoom> ArrayListOperationRooms, ERWaitingRoom erWaitingRoom, ERClinicalEngineerAgent erClinicalEngineerAgent, ERPatientAgent erPatientAgent ) throws ERClinicalEngineerAgentException
	{
		double lfExaminationTime;
		// X線室にいる医療技師エージェントの数及び連携度を算出します。
		erClinicalEngineerAgent.lfCalcAssociationRate(ArrayListClinicalEngineerAgents);
		lfExaminationTime = erClinicalEngineerAgent.lfGetExaminationTime()*erClinicalEngineerAgent.lfGetAssociationRate();

		// 手術時間が経過していない場合
		if( lfExaminationTime > erClinicalEngineerAgent.lfGetCurrentPassOverTime()-erPatientAgent.lfGetMoveTime() )
//		if( erClinicalEngineerAgent.lfGetExaminationTime() > erClinicalEngineerAgent.lfGetCurrentPassOverTime() )
		{
			// 何もせずに終了します。
			cXRayRoomLog.info(erPatientAgent.getId() + "," + this.getId() +"," + "検査時間：" + erClinicalEngineerAgent.lfGetExaminationTime() );
			cXRayRoomLog.info(erPatientAgent.getId() + "," + this.getId() +"," + "対応時間：" + erClinicalEngineerAgent.lfGetCurrentPassOverTime() );
			return ;
		}
		// X線室の検査を実行します。
		if( erClinicalEngineerAgent.iGetClinicalEngineerDepartment() == 10 )
		{
			erClinicalEngineerAgent.vImplementExamination( erPatientAgent, 1 );
			cXRayRoomLog.info(erPatientAgent.getId() + "," + this.getId() +"," + "X線検査終了" );
		}
		erPatientAgent.vSetMoveWaitingTime( 0.0 );

		// 診察室からの依頼の場合、空きがあるならば診察室へ移動します。
		if( erClinicalEngineerAgent.iGetRequestDoctorDepartment() == 1 )
		{
			cXRayRoomLog.info(erPatientAgent.getId() + "," + this.getId() +"," + "診察室からの依頼です。診察室へ移動します。" );
			vJudgeMoveConsultationRoom( ArrayListConsultationRooms, erWaitingRoom, erClinicalEngineerAgent, erPatientAgent );
		}
		// 手術室からの依頼の場合、空きがあるならば手術室へ移動します。
		else if( erClinicalEngineerAgent.iGetRequestDoctorDepartment() == 2 )
		{
			cXRayRoomLog.info(erPatientAgent.getId() + "," + this.getId() +"," + "手術室からの依頼です。手術室へ移動します。" );
			vJudgeMoveOperationRoom( ArrayListOperationRooms, erWaitingRoom, erClinicalEngineerAgent, erPatientAgent );
		}
		// 初療室からの依頼の場合、空きがあるならば初療室へ移動します。
		else if( erClinicalEngineerAgent.iGetRequestDoctorDepartment() == 3 )
		{
			cXRayRoomLog.info(erPatientAgent.getId() + "," + this.getId() +"," + "初療室からの依頼です。初療室へ移動します。" );
			vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSereveInjuryObservationRooms, erWaitingRoom, erCurrentClinicalEngineerAgent, erPatientAgent );
		}
		else
		{
			vJudgeMoveWaitingRoom( erWaitingRoom, erClinicalEngineerAgent, erPatientAgent );
		}
	}

	/**
	 * <PRE>
	 *    診察室への移動判定を実施します。
	 * </PRE>
	 * @param ArrayListConsultationRooms	全診察室
	 * @param erWaitingRoom					待合室
	 * @param erClinicalEngineerAgent		担当医療技師エージェント
	 * @param erPAgent						受検した患者エージェント
	 * @throws ERClinicalEngineerAgentException	医療技師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveConsultationRoom( ArrayList<ERConsultationRoom> ArrayListConsultationRooms, ERWaitingRoom erWaitingRoom, ERClinicalEngineerAgent erClinicalEngineerAgent, ERPatientAgent erPAgent  ) throws ERClinicalEngineerAgentException
	{
		int i,j;
		int iJudgeCount = 0;
		ERDoctorAgent erConsultaitonDoctorAgent;
		ERNurseAgent erConsultationNurseAgent;
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			// 診察室に空きがあるか否か
			if( ArrayListConsultationRooms.get(i).isVacant() == true )
			{
				// 診察室待機フラグをOFFにします。
				erPAgent.vSetConsultationRoomWaitFlag( 0 );

				// 患者のいる位置を診察室に変更します。
				erPAgent.vSetLocation( 1 );

				// 検査が終了したというフラグをONにします。
				erPAgent.vSetExaminationFinishFlag( 1 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// 患者エージェントを診察室に配置します。
				ArrayListConsultationRooms.get(i).vSetPatientAgent( erPAgent );

			// 看護師、および医師エージェントにメッセージを送信します。

				// 診察室の医師エージェントに結果を送信します。
				erConsultaitonDoctorAgent = ArrayListConsultationRooms.get(i).cGetDoctorAgent();
				erClinicalEngineerAgent.vSendToDoctorAgentMessage( erPAgent, erConsultaitonDoctorAgent, (int)erClinicalEngineerAgent.getId(), (int)erConsultaitonDoctorAgent.getId() );

				// 診察室の看護師エージェントに結果を送信します。
				for( j = 0;j < ArrayListConsultationRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erConsultationNurseAgent = ArrayListConsultationRooms.get(i).cGetNurseAgent(j);
					erClinicalEngineerAgent.vSendToNurseAgentMessage( erPAgent, erConsultationNurseAgent, (int)erClinicalEngineerAgent.getId(), (int)erConsultationNurseAgent.getId() );
				}

				// 技士エージェントの対応を終了します。
				erClinicalEngineerAgent.vSetAttending( 0 );

				// 対応を受けた患者エージェントを削除します。
				vRemovePatientAgent( erPAgent );

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListConsultationRooms.get(i).erGetTriageNode() ) );
				}
				erPAgent = null;

				// 診察室で担当する医師エージェントを設定します。
//				ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetConsultation(1);
				ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetAttending(1);

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListConsultationRooms.size() )
		{
			// 診察室待機フラグをONにします。
			erPAgent.vSetConsultationRoomWaitFlag( 1 );
			vJudgeMoveWaitingRoom( erWaitingRoom, erClinicalEngineerAgent, erPAgent );
		}
	}

	/**
	 * <PRE>
	 *    手術室へ移動可能かどうかを判定し、移動できる場合は待合室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListOperationRooms	手術室エージェント
	 * @param erWaitingRoom				待合室エージェント
	 * @param erClinicalEngineerAgent	医療技師エージェント
	 * @param erPAgent					患者エージェント
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveOperationRoom( ArrayList<EROperationRoom> ArrayListOperationRooms, ERWaitingRoom erWaitingRoom, ERClinicalEngineerAgent erClinicalEngineerAgent, ERPatientAgent erPAgent )
	{
		int i,j;
		int iJudgeCount = 0;
		ERDoctorAgent erOperationDoctorAgent;
		ERNurseAgent erOperationNurseAgent;
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			// 手術室に空きがある場合
			if( ArrayListOperationRooms.get(i).isVacant() == true )
			{
				// 手術室待ちフラグをOFFにします。
				erPAgent.vSetOperationRoomWaitFlag( 0 );

				// 患者のいる位置を手術室に変更します。
				erPAgent.vSetLocation( 2 );

				// 検査が終了したというフラグをONにします。
				erPAgent.vSetExaminationFinishFlag( 1 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// 手術室へ患者エージェントを移動します。
				ArrayListOperationRooms.get(i).vSetPatientAgent( erPAgent );

				// 手術室の医師エージェントへメッセージを送信します。
				erOperationDoctorAgent = ArrayListOperationRooms.get(i).cGetSurgeonDoctorAgent();
				erClinicalEngineerAgent.vSendToDoctorAgentMessage( erPAgent, erOperationDoctorAgent, (int)erClinicalEngineerAgent.getId(), (int)erOperationDoctorAgent.getId() );

				// 手術室の看護師エージェントへメッセージを送信します。
				for( j = 0;j < ArrayListOperationRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erOperationNurseAgent = ArrayListOperationRooms.get(i).cGetNurseAgent(j);
					erClinicalEngineerAgent.vSendToNurseAgentMessage( erPAgent, erOperationNurseAgent, (int)erClinicalEngineerAgent.getId(), (int)erOperationNurseAgent.getId() );
				}

				// 医師エージェントの対応を終了します。
				erClinicalEngineerAgent.vSetAttending( 0 );

				// 手術室で担当する医師エージェントを設定します。
				ArrayListOperationRooms.get(i).cGetSurgeonDoctorAgent().vSetSurgeon(1);
				ArrayListOperationRooms.get(i).cGetSurgeonDoctorAgent().vSetAttending(1);

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListOperationRooms.get(i).erGetTriageNode() ) );
				}
				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListOperationRooms.size() )
		{
			// 空きがない場合は患者の手術室待ちフラグをONにして、待合室に移動します。
			erPAgent.vSetOperationRoomWaitFlag( 1 );
			vJudgeMoveWaitingRoom( erWaitingRoom, erClinicalEngineerAgent, erPAgent );
		}
	}

	/**
	 * <PRE>
	 *    初療室へ移動可能かどうかを判定し、移動できる場合は初療室へ移動します。
	 *    移動できない場合は重症観察室へいったん移動します。
	 * </PRE>
	 * @param ArrayListEmergencyRooms					全初療室
	 * @param ArrayListSevereInjuryObservationRooms		全重症観察室
	 * @param erWaitingRoom								待合室
	 * @param erClinicalEngineerAgent					担当した医療技師エージェント
	 * @param erPAgent									受検した患者エージェント
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveEmergencyRoom( ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ERWaitingRoom erWaitingRoom, ERClinicalEngineerAgent erClinicalEngineerAgent, ERPatientAgent erPAgent )
	{
		int i,j;
		int iJudgeCount = 0;
		ERDoctorAgent erEmergencyDoctorAgent;
		ERNurseAgent erEmergencyNurseAgent;
//		ERClinicalEngineerAgent erEmergencyclinicalEngineerAgent;
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			// 初療室に空きがある場合
			if( ArrayListEmergencyRooms.get(i).isVacant() == true )
			{
				// 初療室待ちフラグをOFFにします。
				erPAgent.vSetEmergencyRoomWaitFlag( 0 );

				// 患者のいる位置を初療室に変更します。
				erPAgent.vSetLocation( 3 );

				// 検査が終了したというフラグをONにします。
				erPAgent.vSetExaminationFinishFlag( 1 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// 初療室へ患者エージェントを移動します。
				ArrayListEmergencyRooms.get(i).vSetPatientAgent( erPAgent );

				// 初療室の看護師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erEmergencyNurseAgent = ArrayListEmergencyRooms.get(i).cGetNurseAgent(j);
					erClinicalEngineerAgent.vSendToNurseAgentMessage( erPAgent, erEmergencyNurseAgent, (int)erClinicalEngineerAgent.getId(), (int)erEmergencyNurseAgent.getId() );
				}
				// 初療室の医師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetDoctorAgentsNum(); j++ )
				{
					erEmergencyDoctorAgent = ArrayListEmergencyRooms.get(i).cGetDoctorAgent( j );
					erClinicalEngineerAgent.vSendToDoctorAgentMessage( erPAgent, erEmergencyDoctorAgent, (int)erClinicalEngineerAgent.getId(), (int)erEmergencyDoctorAgent.getId() );
				}
//				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
//				{
//					erEmergencyCinicalEngineerAgent = ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgents(j);
//					erNurseAgent.vSendToEngineerAgentMessage( erPAgent, (int)erNurseAgent.getId(), (int)erEmergencyClinicalEngineerAgent.getId() );
//				}

				// 医師エージェントの対応を終了します。
				erClinicalEngineerAgent.vSetAttending( 0 );

				// 初療室で担当する医師エージェントを設定します。
				ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetSurgeon(1);
				ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetAttending(1);

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListEmergencyRooms.get(i).erGetTriageNode() ) );
				}
				erPAgent = null;
				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListEmergencyRooms.size() )
		{
			// 空きがない場合は患者の初療室待ちフラグをONにして、重症観察室に移動します。
			erPAgent.vSetEmergencyRoomWaitFlag( 1 );
			vJudgeMoveSereveInjuryObservationRoom( ArrayListSevereInjuryObservationRooms, erWaitingRoom, erClinicalEngineerAgent, erPAgent );
		}
	}

	/**
	 * <PRE>
	 *    重症観察室への移動判定を行います。
	 * </PRE>
	 * @param ArrayListSereveInjuryObservationRooms	全重症観察室
	 * @param erWaitingRoom							待合室
	 * @param erClinicalEngineerAgent				担当した医療技師エージェント
	 * @param erPAgent								受検した患者エージェント
	 */
	private void vJudgeMoveSereveInjuryObservationRoom( ArrayList<ERSevereInjuryObservationRoom> ArrayListSereveInjuryObservationRooms, ERWaitingRoom erWaitingRoom, ERClinicalEngineerAgent erClinicalEngineerAgent, ERPatientAgent erPAgent  )
	{
		int i,j;
		int iJudgeCount = 0;
		ERNurseAgent erSevereInjuryObservationNurseAgent;
		for( i = 0;i < ArrayListSereveInjuryObservationRooms.size(); i++ )
		{
			// 重症観察室に空きがある場合は観察室へエージェントを移動します。
			if( ArrayListSereveInjuryObservationRooms.get(i).isVacant() == true )
			{
				// 重傷観察室待機フラグをOFFにします。
				erPAgent.vSetSereveInjuryObservationRoomWaitFlag( 0 );

				// 患者のいる位置を重症観察室に変更します。
				erPAgent.vSetLocation( 5 );

				// 検査が終了したというフラグをONにします。
				erPAgent.vSetExaminationFinishFlag( 1 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// 空きがない場合は重症観察室へ移動します。
				ArrayListSereveInjuryObservationRooms.get(i).vSetPatientAgent( erPAgent );

				// 重症観察室の看護師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListSereveInjuryObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					// 看護師エージェントへメッセージを送信します。
					erSevereInjuryObservationNurseAgent = ArrayListSereveInjuryObservationRooms.get(i).erGetNurseAgent(j);
					erClinicalEngineerAgent.vSendToNurseAgentMessage( erPAgent, erSevereInjuryObservationNurseAgent, (int)erClinicalEngineerAgent.getId(), (int)erSevereInjuryObservationNurseAgent.getId() );
				}

				// 看護師エージェントの対応を終了します。
				erClinicalEngineerAgent.vSetAttending( 0 );

				// 対応を受けた患者エージェントを削除します。
				vRemovePatientAgent( erPAgent );

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListSereveInjuryObservationRooms.get(i).erGetTriageNode() ) );
				}
				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListSereveInjuryObservationRooms.size() )
		{
			// 空きがない場合は重症観察室待機フラグをONにしてそのまま待機室で待機します。
			erPAgent.vSetSereveInjuryObservationRoomWaitFlag( 1 );
			vJudgeMoveWaitingRoom( erWaitingRoom, erClinicalEngineerAgent, erPAgent );
		}
	}

	/**
	 * <PRE>
	 *    待合室へ移動可能かどうかを判定して、移動処置を行います。
	 * </PRE>
	 * @param erWaitingRoom				全待合室
	 * @param erClinicalEngineerAgent	担当した医療技師エージェント
	 * @param erPAgent					受検した患者エージェント
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	private void vJudgeMoveWaitingRoom( ERWaitingRoom erWaitingRoom, ERClinicalEngineerAgent erClinicalEngineerAgent, ERPatientAgent erPAgent )
	{
		int j;
		ERNurseAgent erWatingRoomNurseAgent;

		// 待合室に空きがあるか否か
		if( erWaitingRoom.isVacant() == true )
		{
			// 患者エージェントを待合室に配置します。
			erWaitingRoom.vSetPatientAgent( erPAgent );

			// 検査が終了したというフラグをONにします。
			erPAgent.vSetExaminationFinishFlag( 1 );

			// 患者のいる位置を待合室に変更します。
			erPAgent.vSetLocation( 9 );

			// 移動開始フラグを設定します。
			erPAgent.vSetMoveRoomFlag( 1 );
			erPAgent.vSetMoveWaitingTime( 0.0 );

			// 看護師エージェントへ患者情報を送信します。
			for( j = 0;j < erWaitingRoom.iGetNurseAgentsNum(); j++ )
			{
				erWatingRoomNurseAgent = erWaitingRoom.erGetNurseAgent(j);
				erClinicalEngineerAgent.vSendToNurseAgentMessage( erPAgent, erWatingRoomNurseAgent, (int)erClinicalEngineerAgent.getId(), (int)erWatingRoomNurseAgent.getId() );
			}
			// 医療技師エージェントの対応を終了します。
			erClinicalEngineerAgent.vSetAttending( 0 );

			if( iInverseSimFlag == 1 )
			{
				// 移動先の経路を患者エージェントに設定します。
				erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), erWaitingRoom.erGetTriageNode() ) );
			}
			erPAgent = null;
		}
		else
		{
			// 空きができるまでエージェントを待ち状態にします。
		}
	}

	/**
	 * <PRE>
	 *    移動する患者エージェントを取得します。
	 * </PRE>
	 * @param erPAgent		受検する患者エージェント
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vSetPatientAgent( ERPatientAgent erPAgent )
	{
		erCurrentPatientAgent = erPAgent;
	}

	/**
	 * <PRE>
	 *    検査が終了した患者エージェントを削除します。
	 * </PRE>
	 * @param erPAgent		受検した患者エージェント
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vRemovePatientAgent(ERPatientAgent erPAgent)
	{
		erCurrentPatientAgent = null;
	}

	/**
	 * <PRE>
	 *   初療室の医療技師エージェントを取得します。
	 * </PRE>
	 * @param i 所属している医療技師の番号
	 * @return	担当している医療技師エージェントのインスタンス
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public ERClinicalEngineerAgent cGetClinicalEngineerAgent( int i )
	{
		return ArrayListClinicalEngineerAgents.get(i);
	}

	/**
	 * <PRE>
	 *   検査室の医療技師エージェントの数を取得します。
	 * </PRE>
	 * @return				所属する医療技師エージェントの数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public int iGetClinicalEngineerAgentsNum()
	{
		return ArrayListClinicalEngineerAgents.size();
	}

	/**
	 * <PRE>
	 *    検査室の対応中かどうかを取得します。
	 * </PRE>
	 * @return				false 全員対応中
	 * 						true  空いている人がいる
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public boolean isVacant()
	{
		// 室は存在しているが、所属医療技師がいない場合。通常はないはずだが・・・。
		// 対応できないので空いていないとします。
		if( erCurrentClinicalEngineerAgent == null )
		{
			return false;
		}
		// 所属医師が全員対応中の場合、空いていないとします。
		if( erCurrentClinicalEngineerAgent.iGetAttending() == 1 )
		{
			return false;
		}
		return true;
	}

	public ERClinicalEngineerAgent cGetCurrentClinicalEngineerAgent()
	{
		return erCurrentClinicalEngineerAgent;
	}

	@Override
	public void action(long timeStep)
	{
		double lfSecond = 0.0;
		// TODO 自動生成されたメソッド・スタブ
		lfSecond = timeStep / 1000.0;
		lfTotalTime += lfSecond;

		synchronized( csXRayRoomCriticalSection )
		{
			// 死亡患者がいる場合は削除をする。
			if( erCurrentPatientAgent != null )
			{
				if( erCurrentPatientAgent.iGetSurvivalFlag() == 0 || erCurrentPatientAgent.iGetDisChargeFlag() == 1 )
				{
					erCurrentPatientAgent = null;
				}
			}
		}
	}

	/**
	 * <PRE>
	 *    MRIのログ出力設定をします。
	 * </PRE>
	 * @param log	ロガークラスインスタンス
	 */
	public void vSetLog(Logger log)
	{
		// TODO 自動生成されたメソッド・スタブ
		cXRayRoomLog = log;
	}

	/**
	 * <PRE>
	 *    X線室のX座標を取得します。
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
	 *    X線室のY座標を取得します。
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
	 *    X線室の横幅を取得します。
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
	 *    X線室の縦幅を取得します。
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
	 *    X線室の階数を取得します。
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
	 *    X線室のX座標を格納します。
	 * </PRE>
	 * @param iData	X座標
	 */
	public void vSetX( int iData )
	{
		iDrawX = iData;
	}

	/**
	 * <PRE>
	 *    X線室のY座標を格納します。
	 * </PRE>
	 * @param iData	Y座標
	 */
	public void vSetY( int iData )
	{
		iDrawY = iData;
	}

	/**
	 * <PRE>
	 *    X線室のZ座標を格納します。
	 * </PRE>
	 * @param iData	Z座標
	 */
	public void vSetZ( int iData )
	{
		iDrawZ = iData;
	}

	/**
	 * <PRE>
	 *   X線室の横幅を格納します。
	 * </PRE>
	 * @param iData	横幅
	 */
	public void vSetWidth( int iData )
	{
		iDrawWidth = iData;
	}

	/**
	 * <PRE>
	 *    X線室の縦幅を格納します。
	 * </PRE>
	 * @param iData	縦幅
	 */
	public void vSetHeight( int iData )
	{
		iDrawHeight = iData;
	}

	/**
	 * <PRE>
	 *    X線室の階数を格納します。
	 * </PRE>
	 * @param iData	階数
	 */
	public void vSetF( int iData )
	{
		iDrawF = iData;
	}

	/**
	 * <PRE>
	 *   X線室に所属しているエージェントの座標を設定します。
	 * </PRE>
	 */
	public void vSetAffiliationAgentPosition()
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;

		double lfX = 0.0;
		double lfY = 0.0;
		double lfZ = 0.0;

		for( i = 0;i < ArrayListClinicalEngineerAgents.size(); i++ )
		{
			// 医療技師エージェントの位置を設定します。
			lfX = this.getPosition().getX()+10*(2*rnd.NextUnif()-1);
			lfY = this.getPosition().getY()+10*(2*rnd.NextUnif()-1);
			lfZ = this.getPosition().getZ()+10*(2*rnd.NextUnif()-1);
			ArrayListClinicalEngineerAgents.get(i).setPosition( lfX, lfY, lfZ );
		}
	}

	/**
	 * <PRE>
	 *    トリアージノードマネージャーを設定します。
	 * </PRE>
	 * @param erNodeManager	ノード、リンクが格納されたノードマネージャー
	 */
	public void vSetERTriageNodeManager( ERTriageNodeManager erNodeManager )
	{
		erTriageNodeManager = erNodeManager;
	}

	/**
	 * <PRE>
	 *    現在選択されている待合室のノードを取得します。
	 * </PRE>
	 * @return	救急部門のノード
	 */
	public ERTriageNode erGetTriageNode()
	{
		return erTriageNode;
	}

	/**
	 * <PRE>
	 *    現在選択されているX線室のノードを取得します。
	 * </PRE>
	 * @param erNode 選択中のX線室のノード
	 */
	public void vSetTriageNode( ERTriageNode erNode )
	{
		erTriageNode = erNode;
	}

	/**
	 * <PRE>
	 *   逆シミュレーションモードを設定します。
	 * </PRE>
	 * @param iMode	0 通常シミュレーションモード
	 * 				1 GUIモード
	 * 				2 逆シミュレーションモード
	 */
	public void vSetInverseSimMode( int iMode )
	{
		int i;
		iInverseSimFlag = iMode;

		for( i = 0;i < ArrayListClinicalEngineerAgents.size(); i++ )
		{
			ArrayListClinicalEngineerAgents.get(i).vSetInverseSimMode( iMode );
		}
	}

	/**
	 * <PRE>
	 *    現時点で患者がいるかどうかを取得します。
	 * </PRE>
	 * @return	患者がいる場合は1,いない場合は0
	 */
	public synchronized int iGetPatientInARoom()
	{
		// TODO 自動生成されたメソッド・スタブ
		synchronized( csXRayRoomCriticalSection )
		{
			if( erCurrentPatientAgent != null )
			{
				if( erCurrentPatientAgent.iGetSurvivalFlag() == 1 )
				{
					return 1;
				}
			}
		}
		return 0;
	}


	/**
	 * <PRE>
	 *   クリティカルセクションを設定します。
	 * </PRE>
	 * @param cs	クリティカルセクションのインスタンス
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vSetCriticalSection(Object cs )
	{
		// TODO 自動生成されたメソッド・スタブ
		csXRayRoomCriticalSection = cs;
	}

	/**
	 * <PRE>
	 *   メルセンヌツイスターインスタンスを設定します。
	 * </PRE>
	 * @param sfmtRandom メルセンヌツイスターインスタンス(部屋自体)
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vSetRandom(Rand sfmtRandom)
	{
		// TODO 自動生成されたメソッド・スタブ
		rnd = sfmtRandom;
	}

	/**
	 * <PRE>
	 *   メルセンヌツイスターインスタンスを設定します。
	 *   (室所属する医療技師エージェント)
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vSetClinicalEngineersRandom()
	{
		int i;
		for( i = 0;i < ArrayListClinicalEngineerAgents.size(); i++ )
		{
			ArrayListClinicalEngineerAgents.get(i).vSetRandom( rnd );
		}
	}
	/**
	 * <PRE>
	 *   現時点でのトリアージ緊急度別受診数の患者の数を求めます。
	 * </PRE>
	 * @param iCategory	トリアージ緊急度
	 * @author kobayashi
	 * @since 2016/07/27
	 * @return			緊急度に該当する患者人数
	 */
	public int iGetTriageCategoryPatientNum( int iCategory )
	{
		int i;
		int iCategoryPatientNum = 0;
		if( erCurrentPatientAgent != null )
		{
			if( iCategory == erCurrentPatientAgent.iGetEmergencyLevel()-1 )
			{
				iCategoryPatientNum++;
			}
		}
		return iCategoryPatientNum;
	}

	/**
	 * <PRE>
	 *   初療室の患者エージェントの数を取得します。
	 *   nullでなければ1人いるので1と返却します。
	 * </PRE>
	 * @return				患者の人数
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public int iGetPatientAgentsNum()
	{
		return erCurrentPatientAgent != null ? 1 : 0;
	}


	/**
	 * <PRE>
	 *    現在、待合室に最も長く病院にいる患者の在院時間を取得します。
	 * </PRE>
	 * @return		最も長く病院に在院する患者の在院時間
	 */
	public double lfGetLongestStayPatient()
	{
		int i;
		double lfLongestStayTime = -Double.MAX_VALUE;
		if( erCurrentPatientAgent != null )
		{
			if( erCurrentPatientAgent.lfGetTimeCourse() > 0.0 )
			{
				lfLongestStayTime = erCurrentPatientAgent.lfGetTimeCourse();
			}
		}
		return lfLongestStayTime;
	}

	/**
	 * <PRE>
	 *    現在、最後に病床に入った患者の到着から入院までの時間を算出します。
	 * </PRE>
	 */
	public void vLastBedTime()
	{
		if( erCurrentPatientAgent != null )
		{
			if( erCurrentPatientAgent.lfGetTimeCourse() > 0.0 )
			{
				if( erCurrentPatientAgent.lfGetHospitalStayTime() > 0.0 )
				{
					lfLongestTotalTime = erCurrentPatientAgent.lfGetTotalTime();
					lfLastBedTime = erCurrentPatientAgent.lfGetTimeCourse()-erCurrentPatientAgent.lfGetHospitalStayTime();
				}
				// まだ入院していない場合は0とします。
				if( erCurrentPatientAgent.lfGetHospitalStayTime() == 0.0 )
				{
					lfLongestTotalTime = 0.0;
					lfLastBedTime = 0.0;
				}
			}
		}
	}

	/**
	 * <PRE>
	 *    現在最も長く病院にいる時間の総時間を返します。
	 * </PRE>
	 * @return	最も長く病院にいる時間の総時間
	 */
	public double lfGetLongestStayHospitalTotalTime()
	{
		return lfLongestTotalTime;
	}

	/**
	 * <PRE>
	 *    最後に入院と診断された患者の病院到達から入院までの時間を返します。
	 * </PRE>
	 * @return	最後に入院と診断された患者の病院到達から入院までの時間
	 */
	public double lfGetLastBedTime()
	{
		return lfLastBedTime;
	}
}
