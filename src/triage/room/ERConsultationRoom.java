package triage.room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import triage.agent.ERClinicalEngineerAgent;
import triage.agent.ERDoctorAgent;
import triage.agent.ERDoctorAgentException;
import triage.agent.ERNurseAgent;
import triage.agent.ERPatientAgent;
import utility.node.ERTriageNode;
import utility.node.ERTriageNodeManager;
import utility.sfmt.Rand;

/**
 * 病院の診察室を表すクラスです。
 * このプログラムではこのクラスを含めすべての部屋をエージェントとして定義しています。<br>
 * そのようにすることにより、いろいろと都合がよいためそのようにしております。<br>
 * 診察室では登録された患者に対して、患者の重症度、バイタルサインなどのデータから<br>
 * 次にどこの部屋に行けばよいのか診断します。診断後行く部屋は次の通りです。<br>
 * １待合室<br>
 * ２観察室<br>
 * ３初療室<br>
 * ４一般病棟<br>
 * ５手術室<br>
 *
 * 使用方法は次の通りです。<br>
 * 初期化　　　　　　vInitialize　<br>
 * エージェント作成　vCreateDoctorAgents<br>
 * 　　　　　　　　　vCreateNurseAgents<br>
 * 設定　　　　　　　vSetDoctorAgentParameter<br>
 * 　　　　　　　　　vSetNurseAgentParameter<br>
 * 　　　　　　　　　vSetReadWriteFileForAgents<br>
 * 診断　　　　　　　vImplementConsultation<br>
 * 実行　　　　　　　action　<br>
 * 終了処理　　　　　　vTerminate　<br>
 *
 * @author kobayashi
 *
 */
public class ERConsultationRoom extends Agent
{
	private static final long serialVersionUID = -4740079265086427212L;

	ERPatientAgent erCurrentPatientAgent;				// 診察室で対応を受けている患者エージェント
	ERDoctorAgent erConsultationDoctorAgent;			// 診察を行っている医師エージェント
	ArrayList<ERNurseAgent> ArrayListNurseAgents;		// 診察室で見ている看護師エージェント
	int iAttachedDoctorAgentNum;						// 診察室に所属する医師の数
	int iAttachedNurseAgentNum;							// 診察室に所属する看護師の数
	Rand rnd;											// 乱数クラス

	private Logger cConsultationRoomLog;				// 診察室ログ出力設定

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

	private Object csConsultationRoomCriticalSection;	// クリティカルセクション用

	private double lfLastBedTime;
	private double lfLongestTotalTime;

	/**
	 * <PRE>
	 *   診察室のコンストラクタ
	 * </PRE>
	 */
	public ERConsultationRoom()
	{
		vInitialize();
	}

	/**
	 * <PRE>
	 *   診察室を初期化します。
	 * </PRE>
	 */
	public void vInitialize()
	{
		erConsultationDoctorAgent	= new ERDoctorAgent();
		ArrayListNurseAgents		= new ArrayList<ERNurseAgent>();
		erCurrentPatientAgent		= null;								// 診察室で対応を受けている患者エージェント
		iAttachedDoctorAgentNum		= 0;								// 診察室に所属する医師の数
		iAttachedNurseAgentNum		= 0;								// 診察室に所属する看護師の数

	}

	/**
	 * <PRE>
	 *    診察室のコンストラクタ
	 * </PRE>
	 * @param iDoctorAgentNum			所属医師数
	 * @param iNurseAgentNum			所属看護師数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vInitialize( int iDoctorAgentNum, int iNurseAgentNum )
	{
		int i;

		erConsultationDoctorAgent = new ERDoctorAgent();
		for( i = 0;i < iNurseAgentNum; i++ )
		{
			ArrayListNurseAgents.add( new ERNurseAgent() );
		}

		iInverseSimFlag = 0;
	}

	public void vDrawInitialize()
	{
	}

	/**
	 * <PRE>
	 *    ファイルの読み込みを実行します。
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

		erConsultationDoctorAgent.vSetReadWriteFile( iFileWriteMode );
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetReadWriteFile( iFileWriteMode );
		}
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetReadWriteFile( iFileWriteMode );
		}
	}

	/**
	 * <PRE>
	 *    終了処理を実行します。
	 * </PRE>
	 * @throws IOException	java標準IO例外クラス
	 */
	public synchronized void vTerminate() throws IOException
	{
		int i;

		synchronized( csConsultationRoomCriticalSection )
		{
			// 患者エージェントの終了処理を行います。
			if( erCurrentPatientAgent != null )
			{
				erCurrentPatientAgent.vTerminate();
				this.getEngine().addExitAgent( erCurrentPatientAgent );
				erCurrentPatientAgent = null;
			}
			// 医師エージェントの終了処理を行います。
			if( erConsultationDoctorAgent != null )
			{
				erConsultationDoctorAgent.vTerminate();
				this.getEngine().addExitAgent( erConsultationDoctorAgent );
				erConsultationDoctorAgent = null;
			}
			// 看護師エージェントの終了処理を行います。
			if( ArrayListNurseAgents != null )
			{
				for( i = ArrayListNurseAgents.size()-1; i >= 0; i-- )
				{
					if( ArrayListNurseAgents.get(i) != null )
					{
						ArrayListNurseAgents.get(i).vTerminate();
						this.getEngine().addExitAgent( ArrayListNurseAgents.get(i) );
						ArrayListNurseAgents.set( i, null );
						ArrayListNurseAgents.remove(i);
					}
				}
				ArrayListNurseAgents = null;

				rnd = null;

				cConsultationRoomLog = null;				// 診察室ログ出力設定

				erTriageNodeManager = null;
				erTriageNode = null;
			}
		}
	}

	/**
	 * <PRE>
	 *   診察室の医師エージェントを生成します。
	 * </PRE>
	 * @param iDoctorAgentNum  医師エージェント数
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vCreateDoctorAgents( int iDoctorAgentNum )
	{
		erConsultationDoctorAgent = new ERDoctorAgent();
	}

	/**
	 * <PRE>
	 *   診察室の看護師エージェントを生成します。
	 * </PRE>
	 * @param iNurseAgentNum 看護師エージェント数
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vCreateNurseAgents( int iNurseAgentNum )
	{
		int i;

		if( ArrayListNurseAgents == null )
		{
			// 逆シミュレーションの場合に通ります。
			ArrayListNurseAgents = new ArrayList<ERNurseAgent>();
		}
		for( i = 0;i < iNurseAgentNum; i++ )
		{
			ArrayListNurseAgents.add( new ERNurseAgent() );
		}
	}

	/**
	 * <PRE>
	 *    診察室の医師エージェントのパラメータを設定します。
	 * </PRE>
	 * @param lfYearExperience			経験年数
	 * @param lfConExperience			経験数の重み
	 * @param lfExperienceRate1			経験年数パラメータ1
	 * @param lfExperienceRate2			経験年数パラメータ2
	 * @param lfConExperienceAIS		経験年数重み（重症度）
	 * @param lfExperienceRateAIS1		経験年数パラメータその１（重症度）
	 * @param lfExperienceRateAIS2		経験年数パラメータその２（重症度）
	 * @param lfConTired1				疲労度パラメータ1
	 * @param lfConTired2				疲労度パラメータ2
	 * @param lfConTired3				疲労度パラメータ3
	 * @param lfConTired4				疲労度パラメータ4
	 * @param lfTiredRate				疲労度重み
	 * @param lfRevisedOperationRate	手術室改善度割合
	 * @param lfAssociationRate			関連性パラメータ
	 * @param lfConsultationTime		診察時間
	 * @param lfOperationTime			手術時間
	 * @param lfEmergencyTime			初療室処置時間
	 * @param iDepartment				所属部署
	 * @param iRoomNumber				所属部屋番号
	 * @author kobayashi
	 * @since 2015/08/10
	 */
	public void vSetDoctorAgentParameter( double lfYearExperience,
										  double lfConExperience,
										  double lfExperienceRate1,
										  double lfExperienceRate2,
										  double lfConExperienceAIS,
										  double lfExperienceRateAIS1,
										  double lfExperienceRateAIS2,
										  double lfConTired1,
										  double lfConTired2,
										  double lfConTired3,
										  double lfConTired4,
										  double lfTiredRate,
										  double lfRevisedOperationRate,
										  double lfAssociationRate,
										  double lfConsultationTime,
										  double lfOperationTime,
										  double lfEmergencyTime,
										  int iDepartment,
										  int iRoomNumber)
	{
		erConsultationDoctorAgent.vSetYearExperience( lfYearExperience );
		erConsultationDoctorAgent.vSetConExperience( lfConExperience );
		erConsultationDoctorAgent.vSetConTired1( lfConTired1 );
		erConsultationDoctorAgent.vSetConTired2( lfConTired2 );
		erConsultationDoctorAgent.vSetConTired3( lfConTired3 );
		erConsultationDoctorAgent.vSetConTired4( lfConTired4 );
		erConsultationDoctorAgent.vSetTiredRate( lfTiredRate );
		erConsultationDoctorAgent.vSetRevisedOperationRate( lfRevisedOperationRate );
		erConsultationDoctorAgent.vSetAssociationRate( lfAssociationRate );
		erConsultationDoctorAgent.vSetConsultationTime( lfConsultationTime );
		erConsultationDoctorAgent.vSetOperationTime( lfOperationTime );
		erConsultationDoctorAgent.vSetEmergencyTime( lfEmergencyTime );
		erConsultationDoctorAgent.vSetDoctorDepartment( iDepartment );
		erConsultationDoctorAgent.vSetExperienceRate1( lfExperienceRate1 );
		erConsultationDoctorAgent.vSetExperienceRate2( lfExperienceRate2 );
		erConsultationDoctorAgent.vSetConExperienceAIS( lfConExperienceAIS );
		erConsultationDoctorAgent.vSetExperienceRateAIS1( lfExperienceRateAIS1 );
		erConsultationDoctorAgent.vSetExperienceRateAIS2( lfExperienceRateAIS2 );
		erConsultationDoctorAgent.vSetRoomNumber( iRoomNumber );
	}

	/**
	 * <PRE>
	 *    看護師エージェントのパラメータを設定します。
	 * </PRE>
	 * @param aiNurseCategory				看護師のカテゴリー
	 * @param aiNurseTriageProtocol			トリアージプロトコル
	 * @param aiNurseTriageLevel			トリアージの緊急度レベル
	 * @param alfNurseTriageYearExperience	トリアージ経験年数
	 * @param alfNurseYearExperience		看護師経験年数
	 * @param alfNurseConExperience			看護師経験年数重み
	 * @param alfExperienceRate1			経験年数パラメータその１
	 * @param alfExperienceRate2			経験年数パラメータその２
	 * @param alfConExperienceAIS			経験年数パラメータ重み（重症度）
	 * @param alfExperienceRateAIS1			経験年数パラメータその１（重症度）
	 * @param alfExperienceRateAIS2			経験年数パラメータその２（重症度）
	 * @param alfNurseConTired1				疲労パラメータ１
	 * @param alfNurseConTired2				疲労パラメータ２
	 * @param alfNurseConTired3				疲労パラメータ３
	 * @param alfNurseConTired4				疲労パラメータ４
	 * @param alfNurseTiredRate				疲労度の割合
	 * @param alfNurseAssociationRate		連携度
	 * @param alfObservationTime			定期観察時間
	 * @param alfObservationProcessTime		観察プロセス時間
	 * @param alfTriageTime					トリアージ時間
	 * @param aiDepartment					所属部門
	 * @param aiRoomNumber					所属部屋番号
	 * @author kobayashi
	 * @since 2015/08/10
	 * @version 0.2
	 */
	public void vSetNurseAgentParameter( int[] aiNurseCategory,
			int[] aiNurseTriageProtocol,
			int[] aiNurseTriageLevel,
			double[] alfNurseTriageYearExperience,
			double[] alfNurseYearExperience,
			double[] alfNurseConExperience,
			double[] alfExperienceRate1,
			double[] alfExperienceRate2,
			double[] alfConExperienceAIS,
			double[] alfExperienceRateAIS1,
			double[] alfExperienceRateAIS2,
			double[] alfNurseConTired1,
			double[] alfNurseConTired2,
			double[] alfNurseConTired3,
			double[] alfNurseConTired4,
			double[] alfNurseTiredRate,
			double[] alfNurseAssociationRate,
			double[] alfObservationTime,
			double[] alfObservationProcessTime,
			double[] alfTriageTime,
			int[] aiDepartment,
			int[] aiRoomNumber )
	{
		int i;
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetNurseCategory( aiNurseCategory[i] );
			ArrayListNurseAgents.get(i).vSetTriageProtocol( aiNurseTriageProtocol[i] );
			ArrayListNurseAgents.get(i).vSetTriageProtocolLevel( aiNurseTriageLevel[i] );
			ArrayListNurseAgents.get(i).vSetTriageYearExperience( alfNurseTriageYearExperience[i] );
			ArrayListNurseAgents.get(i).vSetYearExperience( alfNurseYearExperience[i] );
			ArrayListNurseAgents.get(i).vSetConExperience( alfNurseConExperience[i] );
			ArrayListNurseAgents.get(i).vSetConTired1( alfNurseConTired1[i] );
			ArrayListNurseAgents.get(i).vSetConTired2( alfNurseConTired2[i] );
			ArrayListNurseAgents.get(i).vSetConTired3( alfNurseConTired3[i] );
			ArrayListNurseAgents.get(i).vSetConTired4( alfNurseConTired4[i] );
			ArrayListNurseAgents.get(i).vSetTiredRate( alfNurseTiredRate[i] );
			ArrayListNurseAgents.get(i).vSetAssociationRate( alfNurseAssociationRate[i] );
			ArrayListNurseAgents.get(i).vSetObservationTime( alfObservationTime[i] );
			ArrayListNurseAgents.get(i).vSetObservationProcessTime( alfObservationProcessTime[i] );
			ArrayListNurseAgents.get(i).vSetTriageProtocolTime( alfTriageTime[i] );
			ArrayListNurseAgents.get(i).vSetNurseDepartment( aiDepartment[i] );
			ArrayListNurseAgents.get(i).vSetExperienceRate1( alfExperienceRate1[i] );
			ArrayListNurseAgents.get(i).vSetExperienceRate2( alfExperienceRate2[i] );
			ArrayListNurseAgents.get(i).vSetConExperienceAIS( alfConExperienceAIS[i] );
			ArrayListNurseAgents.get(i).vSetExperienceRateAIS1( alfExperienceRateAIS1[i] );
			ArrayListNurseAgents.get(i).vSetExperienceRateAIS2( alfExperienceRateAIS2[i] );
			ArrayListNurseAgents.get(i).vSetRoomNumber( aiRoomNumber[i] );
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
	 *    診察を実行します。
	 * </PRE>
	 * @param erWaitingRoom							待合室
	 * @param ArrayListExaminationXRayRooms			全X線室
	 * @param ArrayListExaminationCTRooms			全CT室
	 * @param ArrayListExaminationMRIRooms			全MRI室
	 * @param ArrayListExaminationAngiographyRooms	全血管造影室
	 * @param ArrayListOperationRooms				全手術室
	 * @param ArrayListEmergencyRooms				全初療室
	 * @param ArrayListSereveInjuryObservationRooms	全重症観察室
	 * @param ArrayListGeneralWardRooms				全一般病棟
	 * @throws ERDoctorAgentException				医師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vImplementConsultation( ERWaitingRoom erWaitingRoom,
			ArrayList<ERExaminationXRayRoom> ArrayListExaminationXRayRooms,
			ArrayList<ERExaminationCTRoom> ArrayListExaminationCTRooms,
			ArrayList<ERExaminationMRIRoom> ArrayListExaminationMRIRooms,
			ArrayList<ERExaminationAngiographyRoom> ArrayListExaminationAngiographyRooms,
			ArrayList<EROperationRoom> ArrayListOperationRooms,
			ArrayList<EREmergencyRoom> ArrayListEmergencyRooms,
			ArrayList<ERSevereInjuryObservationRoom> ArrayListSereveInjuryObservationRooms,
			ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms ) throws ERDoctorAgentException
	{
		int i;

		synchronized( csConsultationRoomCriticalSection )
		{
			// 診察室に患者がいれば診察を実行します。
			if( erCurrentPatientAgent != null )
			{
				// 診察を開始したことを表します。
				erConsultationDoctorAgent.vSetAttending( 1 );

	 			if( erCurrentPatientAgent.isMoveWaitingTime() == false )
	 			{
	 				// 移動中であることを医師、看護師エージェントに知らせます。
	 				erConsultationDoctorAgent.vSetPatientMoveWaitFlag( 1 );
	 				for( i = 0;i < ArrayListNurseAgents.size(); i++ )
	 				{
	 					ArrayListNurseAgents.get(i).vSetPatientMoveWaitFlag( 1 );
	 				}
					// 移動時間がまだ終了していないので、移動を実施しません。
	 				cConsultationRoomLog.info(erCurrentPatientAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + erConsultationDoctorAgent.iGetAttending() + "," + "診察室移動時間：" + erCurrentPatientAgent.lfGetMoveWaitingTime() );
					return;
	 			}
	 			// 部屋移動が終了したのでフラグOFFに処置中とします。
	 			erCurrentPatientAgent.vSetMoveRoomFlag( 0 );
				erConsultationDoctorAgent.vSetPatientMoveWaitFlag( 0 );
				for( i = 0;i < ArrayListNurseAgents.size(); i++ )
				{
					ArrayListNurseAgents.get(i).vSetPatientMoveWaitFlag( 0 );
				}
				cConsultationRoomLog.info(erCurrentPatientAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "診察室対応中" );
				vConsultaionProcess( erWaitingRoom, ArrayListExaminationXRayRooms, ArrayListExaminationCTRooms, ArrayListExaminationMRIRooms, ArrayListExaminationAngiographyRooms, ArrayListOperationRooms, ArrayListEmergencyRooms, ArrayListSereveInjuryObservationRooms, ArrayListGeneralWardRooms, erConsultationDoctorAgent, erCurrentPatientAgent );

				// 診察が終了した患者エージェントを削除します。
				if( erConsultationDoctorAgent.iGetAttending() == 0 )
				{
					cConsultationRoomLog.info(erCurrentPatientAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "診察完全終了" );
					erCurrentPatientAgent = null;
				}
			}
		}
	}

	/**
	 * <PRE>
	 *    診察プロセスを実行します。
	 * </PRE>
	 * @param erWaitingRoom							待合室
	 * @param ArrayListExaminationXRayRooms			全X線室
	 * @param ArrayListExaminationCTRooms			全CT室
	 * @param ArrayListExaminationMRIRooms			全MRI室
	 * @param ArrayListExaminationAngiographyRooms	全血管造影室
	 * @param ArrayListOperationRooms				全手術室
	 * @param ArrayListEmergencyRooms				全初療室
	 * @param ArrayListSereveInjuryObservationRooms	全重症観察室
	 * @param ArrayListGeneralWardRooms				全一般病棟
	 * @param erConsultationDoctorAgentData			担当医師エージェントインスタンス
	 * @param erPAgent								診察を受ける患者エージェントインスタンス
	 * @throws ERDoctorAgentException				医師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vConsultaionProcess( ERWaitingRoom erWaitingRoom,
			ArrayList<ERExaminationXRayRoom> ArrayListExaminationXRayRooms,
			ArrayList<ERExaminationCTRoom> ArrayListExaminationCTRooms,
			ArrayList<ERExaminationMRIRoom> ArrayListExaminationMRIRooms,
			ArrayList<ERExaminationAngiographyRoom> ArrayListExaminationAngiographyRooms,
			ArrayList<EROperationRoom> ArrayListOperationRooms,
			ArrayList<EREmergencyRoom> ArrayListEmergencyRooms,
			ArrayList<ERSevereInjuryObservationRoom> ArrayListSereveInjuryObservationRooms,
			ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms,
			ERDoctorAgent erConsultationDoctorAgentData,
			ERPatientAgent erPAgent ) throws ERDoctorAgentException
	{
		int iProcessResult;
		double lfConsultaionTime;

		// 診察室にいる看護師エージェントの数から連携度を算出します。
		erConsultationDoctorAgentData.lfCalcAssociateRateConsultation(ArrayListNurseAgents);
		lfConsultaionTime = erConsultationDoctorAgentData.lfGetConsultationTime();

		// 診察時間が終了したかどうかを見ます。
		if( lfConsultaionTime > erConsultationDoctorAgentData.lfGetCurrentPassOverTime()-erPAgent.lfGetMoveTime() )
//		if( erConsultationDoctorAgentData.lfGetConsultationTime() > erConsultationDoctorAgentData.lfGetCurrentPassOverTime() )
		{
			cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + erConsultationDoctorAgentData.iGetAttending() + "," + "診察時間：" + erConsultationDoctorAgentData.lfGetConsultationTime() );
			cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + erConsultationDoctorAgentData.iGetAttending() + "," + "対応時間：" + erConsultationDoctorAgentData.lfGetCurrentPassOverTime() );
			// 何もせずに終了します。
			return ;
		}
		cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "診察終了" );
		// 医師の診察プロセスを実行します。
		iProcessResult = erConsultationDoctorAgentData.iImplementConsultationProcess( erPAgent );

		// 診察結果をもとに次の部屋に移るかどうかを判定します。
		// 診察が終了したので、待合室へ移動します。
		if( iProcessResult == 1 )
		{
			// 医師エージェントが患者エージェントが退院可能かどうかを判定します。
			if( erConsultationDoctorAgent.isJudgeDischarge( erPAgent ) == true )
			{
				// 退院を実施します。
				erPAgent.vSetDisChargeFlag( 1 );

				cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "退院判定を実施" + "," + erPAgent.iGetDisChargeFlag() );
			}
			// 退院を実施します。
			erPAgent.vSetDisChargeFlag( 1 );
			// 待合室へ移動します。
			vJudgeMoveWaitingRoom( erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
		}
		else if( iProcessResult == 2 )
		{
			if( erConsultationDoctorAgentData.iGetRequestExamination() == 1 )
			{
				// X線室へ移動します。
				vJudgeMoveExaminationXRayRoom( ArrayListExaminationXRayRooms, erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
			}
			else if( erConsultationDoctorAgentData.iGetRequestExamination() == 2 )
			{
				// CT室へ移動します。
				vJudgeMoveExaminationCTRoom( ArrayListExaminationCTRooms, erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
			}
			else if( erConsultationDoctorAgentData.iGetRequestExamination() == 3 )
			{
				// MRI室へ移動します。
				vJudgeMoveExaminationMRIRoom( ArrayListExaminationMRIRooms, erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
			}
			else if( erConsultationDoctorAgentData.iGetRequestExamination() == 4 )
			{
				// 血管造影室へ移動します。
				vJudgeMoveExaminationAngiographyRoom( ArrayListExaminationAngiographyRooms, erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
			}
		}
		else if( iProcessResult == 3 )
		{
			// 手術が必要なので、手術室へ移動します。
			vJudgeMoveOperationRoom( ArrayListOperationRooms, erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
		}
		else if( iProcessResult == 4 )
		{
			// 入院処置が必要なので、一般病棟へ移動します。
			vJudgeMoveGeneralWardRoom( ArrayListGeneralWardRooms, erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
		}
		else if( iProcessResult == 5 )
		{
			if( ArrayListEmergencyRooms.isEmpty() == false )
			{
				// 緊急処置が必要なので初療室へ移動します。
				vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSereveInjuryObservationRooms, erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
				// 手術が必要なので、手術室へ移動します。
//				vJudgeMoveOperationRoom( ArrayListOperationRooms, erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
			}
			else
			{
				// 初療室がない場合のため、手術室へ移動します。
				vJudgeMoveOperationRoom( ArrayListOperationRooms, erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
			}
		}
		else
		{
			// 診察時間終了まで対応中であることを設定します。
			erConsultationDoctorAgent.vSetAttending( 1 );
		}
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
	private void vJudgeMoveWaitingRoom( ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgent, ERPatientAgent erPAgent ) throws NullPointerException
	{
		int j;
		ERNurseAgent erWatingRoomNurseAgent;

		// 看護師が全員対応中でもそのまま待合室へ移動します。

		cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "待合室へ移動準備開始" + "," + "診察室");
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
			erConsultationDoctorAgent.vSendToNurseAgentMessage( erPAgent, erWatingRoomNurseAgent, (int)erConsultationDoctorAgent.getId(), (int)erWatingRoomNurseAgent.getId() );
		}
		// 医師エージェントの対応を終了します。
		erConsultationDoctorAgent.vSetAttending( 0 );

		// 他の部屋へ移動するために一時的にではなく、待合室へ純粋に再移動する場合は退院しても問題ないので、退院フラグを有効にします。
		if( erPAgent.iGetEnterGeneralWardFlag() == 0 &&
			erPAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
			erPAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
			erPAgent.iGetExaminationMRIRoomWaitFlag() == 0 &&
			erPAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 &&
			erPAgent.iGetExaminationFastRoomWaitFlag() == 0 &&
			erPAgent.iGetObservationRoomWaitFlag() == 0 &&
			erPAgent.iGetSereveInjuryObservationRoomWaitFlag() == 0 &&
			erPAgent.iGetOperationRoomWaitFlag() == 0 )
		{
//			System.out.println(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "患者退院するよ?" );
			cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "患者退院するよ?" );
			erPAgent.vSetDisChargeFlag(1);
			cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + ","  + "待合室へ移動で退院判定OK" + "," + erPAgent.iGetDisChargeFlag() );
		}

		cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "待合室へ移動準備終了" + "," + "診察室");
		if( iInverseSimFlag == 1 )
		{
			// 移動先の経路を患者エージェントに設定します。
			erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), erWaitingRoom.erGetTriageNode() ) );
			cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "待合室へ移動開始" );
		}
		erPAgent = null;
	}

	/**
	 * <PRE>
	 *    X線室へ移動可能かどうかを判定し、移動できる場合はX線室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListExaminationXRayRooms		全X線室
	 * @param erWaitingRoom						待合室
	 * @param erConsultationDoctorAgentData		担当した医師エージェント
	 * @param erPAgent							移動する患者エージェント
	 * @throws NullPointerException				NULLアクセス例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveExaminationXRayRoom( ArrayList<ERExaminationXRayRoom> ArrayListExaminationXRayRooms, ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgentData, ERPatientAgent erPAgent  ) throws NullPointerException
	{
		int i;
		int iJudgeCount = 0;
		ERClinicalEngineerAgent erExaminationClinicalEngineerAgent;
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			// 検査室に空きがある場合は検査室へエージェントを移動します。
			if( ArrayListExaminationXRayRooms.get(i).isVacant() == true )
			{
				cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "X線室へ移動準備開始" + "," + "診察室");
				// 検査室待ちフラグをOFFにします。
				erPAgent.vSetExaminationXRayRoomWaitFlag( 0 );

				// 患者のいる位置を検査室に変更します。
				erPAgent.vSetLocation( 10 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 患者エージェントに対応した医師エージェントを登録します。
				erPAgent.vSetConsultationDoctorAgent( erConsultationDoctorAgentData );

				// 検査室へ患者エージェントを移動させます。
				ArrayListExaminationXRayRooms.get(i).vSetPatientAgent( erPAgent );

				// 医療技師エージェントへ患者情報を送信します。
				erExaminationClinicalEngineerAgent = ArrayListExaminationXRayRooms.get(i).cGetCurrentClinicalEngineerAgent();
				erConsultationDoctorAgentData.vSendToEngineerAgentMessage( erPAgent, erExaminationClinicalEngineerAgent, (int)erConsultationDoctorAgentData.getId(), (int)erExaminationClinicalEngineerAgent.getId() );

				// 医師エージェントの対応を終了します。
				erConsultationDoctorAgentData.vSetAttending( 0 );

				cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "X線室へ移動準備終了" + "," + "診察室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListExaminationXRayRooms.get(i).erGetTriageNode() ) );
					cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "X線室へ移動開始" );
				}
				erPAgent = null;

				// X線室の主担当医療技師が対応開始したと設定します。
				ArrayListExaminationXRayRooms.get(i).cGetCurrentClinicalEngineerAgent().vSetAttending(1);

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListExaminationXRayRooms.size() )
		{
			// 空きがない場合は患者の検査室待ちフラグをONにして、待合室に移動します。
			cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "X線室満室" + "," + "診察室");
			erPAgent.vSetExaminationXRayRoomWaitFlag( 1 );
			erPAgent.vSetConsultationDoctorAgent(erConsultationDoctorAgentData);
			vJudgeMoveWaitingRoom( erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
		}
	}

	/**
	 * <PRE>
	 *    CT室へ移動可能かどうかを判定し、移動できる場合はCT室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListExaminationCTRooms		全CT室
	 * @param erWaitingRoom						待合室
	 * @param erConsultationDoctorAgentData		担当している医師エージェント
	 * @param erPAgent							診察を受けている患者エージェント
	 * @throws NullPointerException				NULL例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveExaminationCTRoom( ArrayList<ERExaminationCTRoom> ArrayListExaminationCTRooms, ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgentData, ERPatientAgent erPAgent  ) throws NullPointerException
	{
		int i;
		int iJudgeCount = 0;
		ERClinicalEngineerAgent erExaminationClinicalEngineerAgent;
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			// 検査室に空きがある場合は検査室へエージェントを移動します。
			if( ArrayListExaminationCTRooms.get(i).isVacant() == true )
			{
				cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "CT室へ移動準備開始" + "," + "診察室");
				// 検査室待ちフラグをOFFにします。
				erPAgent.vSetExaminationCTRoomWaitFlag( 0 );

				// 患者のいる位置を検査室に変更します。
				erPAgent.vSetLocation( 11 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 患者エージェントに対応した医師エージェントを登録します。
				erPAgent.vSetConsultationDoctorAgent( erConsultationDoctorAgentData );

				// 検査室へ患者エージェントを移動させます。
				ArrayListExaminationCTRooms.get(i).vSetPatientAgent( erPAgent );

				// 医療技師エージェントへ患者情報を送信します。
				erExaminationClinicalEngineerAgent = ArrayListExaminationCTRooms.get(i).cGetCurrentClinicalEngineerAgent();
				erConsultationDoctorAgentData.vSendToEngineerAgentMessage( erPAgent, erExaminationClinicalEngineerAgent, (int)erConsultationDoctorAgentData.getId(), (int)erExaminationClinicalEngineerAgent.getId() );

				// 医師エージェントの対応を終了します。
				erConsultationDoctorAgentData.vSetAttending( 0 );

				// CT室の主担当医療技師が対応開始したと設定します。
				ArrayListExaminationCTRooms.get(i).cGetCurrentClinicalEngineerAgent().vSetAttending(1);

				cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "CT室へ移動準備終了" + "," + "診察室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListExaminationCTRooms.get(i).erGetTriageNode() ) );
					cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "CT室へ移動開始" );
				}
				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListExaminationCTRooms.size() )
		{
			// 空きがない場合は患者の検査室待ちフラグをONにして、待合室に移動します。
			cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "CT室満室" + "," + "診察室");
			erPAgent.vSetExaminationCTRoomWaitFlag( 1 );
			erPAgent.vSetConsultationDoctorAgent(erConsultationDoctorAgentData);
			vJudgeMoveWaitingRoom( erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
		}
	}

	/**
	 * <PRE>
	 *    MRI室へ移動可能かどうかを判定し、移動できる場合はMRI室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListExaminationMRIRooms		全MRI室
	 * @param erWaitingRoom						待合室
	 * @param erConsultationDoctorAgentData		担当している医師エージェント
	 * @param erPAgent							診察を受けた患者エージェント
	 * @throws NullPointerException				NULL例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveExaminationMRIRoom( ArrayList<ERExaminationMRIRoom> ArrayListExaminationMRIRooms, ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgentData, ERPatientAgent erPAgent  ) throws NullPointerException
	{
		int i;
		int iJudgeCount = 0;
		ERClinicalEngineerAgent erExaminationClinicalEngineerAgent;
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			// 検査室に空きがある場合は検査室へエージェントを移動します。
			if( ArrayListExaminationMRIRooms.get(i).isVacant() == true )
			{
				cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "MRI室へ移動準備開始" + "," + "診察室");
				// 検査室待ちフラグをOFFにします。
				erPAgent.vSetExaminationMRIRoomWaitFlag( 0 );

				// 患者のいる位置を検査室に変更します。
				erPAgent.vSetLocation( 12 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 患者エージェントに対応した医師エージェントを登録します。
				erPAgent.vSetConsultationDoctorAgent( erConsultationDoctorAgentData );

				// 検査室へ患者エージェントを移動させます。
				ArrayListExaminationMRIRooms.get(i).vSetPatientAgent( erPAgent );

				// 医療技師エージェントへ患者情報を送信します。
				erExaminationClinicalEngineerAgent = ArrayListExaminationMRIRooms.get(i).cGetCurrentClinicalEngineerAgent();
				erConsultationDoctorAgentData.vSendToEngineerAgentMessage( erPAgent, erExaminationClinicalEngineerAgent, (int)erConsultationDoctorAgentData.getId(), (int)erExaminationClinicalEngineerAgent.getId() );

				// 医師エージェントの対応を終了します。
				erConsultationDoctorAgentData.vSetAttending( 0 );

				// MRI室の主担当医療技師が対応開始したと設定します。
				ArrayListExaminationMRIRooms.get(i).cGetCurrentClinicalEngineerAgent().vSetAttending(1);

				cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "MRI室へ移動準備終了" + "," + "診察室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListExaminationMRIRooms.get(i).erGetTriageNode() ) );
					cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "MRI室へ移動開始" );
				}
				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListExaminationMRIRooms.size() )
		{
			// 空きがない場合は患者の検査室待ちフラグをONにして、待合室に移動します。
			cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "MRI室満室" + "," + "診察室");
			erPAgent.vSetExaminationMRIRoomWaitFlag( 1 );
			erPAgent.vSetConsultationDoctorAgent(erConsultationDoctorAgentData);
			vJudgeMoveWaitingRoom( erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
		}
	}

	/**
	 * <PRE>
	 *    血管造影室へ移動可能かどうかを判定し、移動できる場合は血管造影室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListExaminationAngiographyRooms		全血管造影室
	 * @param erWaitingRoom								待合室
	 * @param erConsultationDoctorAgentData				担当している医師エージェント
	 * @param erPAgent									診察を受けている患者エージェント
	 * @throws NullPointerException						NULL例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveExaminationAngiographyRoom( ArrayList<ERExaminationAngiographyRoom> ArrayListExaminationAngiographyRooms, ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgentData, ERPatientAgent erPAgent  ) throws NullPointerException
	{
		int i;
		int iJudgeCount = 0;
		ERClinicalEngineerAgent erExaminationClinicalEngineerAgent;
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			// 検査室に空きがある場合は検査室へエージェントを移動します。
			if( ArrayListExaminationAngiographyRooms.get(i).isVacant() == true )
			{
				cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "血管造影室へ移動準備開始" + "," + "診察室");
				// 検査室待ちフラグをOFFにします。
				erPAgent.vSetExaminationAngiographyRoomWaitFlag( 0 );

				// 患者のいる位置を検査室に変更します。
				erPAgent.vSetLocation( 13 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 患者エージェントに対応した医師エージェントを登録します。
				erPAgent.vSetConsultationDoctorAgent( erConsultationDoctorAgent );

				// 検査室へ患者エージェントを移動させます。
				ArrayListExaminationAngiographyRooms.get(i).vSetPatientAgent( erPAgent );

				// 医療技師エージェントへ患者情報を送信します。
				erExaminationClinicalEngineerAgent = ArrayListExaminationAngiographyRooms.get(i).cGetCurrentClinicalEngineerAgent();
				erConsultationDoctorAgentData.vSendToEngineerAgentMessage( erPAgent, erExaminationClinicalEngineerAgent, (int)erConsultationDoctorAgentData.getId(), (int)erExaminationClinicalEngineerAgent.getId() );

				// 医師エージェントの対応を終了します。
				erConsultationDoctorAgentData.vSetAttending( 0 );

				// 血管造影室の主担当医療技師が対応開始したと設定します。
				ArrayListExaminationAngiographyRooms.get(i).cGetCurrentClinicalEngineerAgent().vSetAttending(1);

				cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "血管造影室へ移動準備終了" + "," + "診察室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListExaminationAngiographyRooms.get(i).erGetTriageNode() ) );
					cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "血管造影室へ移動開始" + "," + "診察室");
				}
				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListExaminationAngiographyRooms.size() )
		{
			// 空きがない場合は患者の検査室待ちフラグをONにして、待合室に移動します。
			cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "血管造影室満室" + "," + "診察室");
			erPAgent.vSetExaminationAngiographyRoomWaitFlag( 1 );
			erPAgent.vSetConsultationDoctorAgent(erConsultationDoctorAgentData);
			vJudgeMoveWaitingRoom( erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
		}
	}

	/**
	 * <PRE>
	 *    FAST室へ移動可能かどうかを判定し、移動できる場合はFAST室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListExaminationFastRooms		全FAST室
	 * @param erWaitingRoom						待合室
	 * @param erConsultationDoctorAgentData		担当医師エージェント
	 * @param erPAgent							移動する患者エージェント
	 * @throws NullPointerException				NULLアクセス例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveExaminationFastRoom( ArrayList<ERExaminationFastRoom> ArrayListExaminationFastRooms, ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgentData, ERPatientAgent erPAgent  )  throws NullPointerException
	{
		int i;
		int iJudgeCount = 0;
		ERClinicalEngineerAgent erExaminationClinicalEngineerAgent;
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			// 検査室に空きがある場合は検査室へエージェントを移動します。
			if( ArrayListExaminationFastRooms.get(i).isVacant() == true )
			{
				cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "血管造影室へ移動" + "," + "診察室");
				// 検査室待ちフラグをOFFにします。
				erPAgent.vSetExaminationFastRoomWaitFlag( 0 );

				// 患者のいる位置を検査室に変更します。
				erPAgent.vSetLocation( 14 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 患者エージェントに対応した医師エージェントを登録します。
				erPAgent.vSetConsultationDoctorAgent( erConsultationDoctorAgent );
				// 検査室へ患者エージェントを移動させます。
				ArrayListExaminationFastRooms.get(i).vSetPatientAgent( erPAgent );

				// 医療技師エージェントへ患者情報を送信します。
				erExaminationClinicalEngineerAgent = ArrayListExaminationFastRooms.get(i).cGetCurrentClinicalEngineerAgent();
				erConsultationDoctorAgentData.vSendToEngineerAgentMessage( erPAgent, erExaminationClinicalEngineerAgent, (int)erConsultationDoctorAgentData.getId(), (int)erExaminationClinicalEngineerAgent.getId() );

				// 医師エージェントの対応を終了します。
				erConsultationDoctorAgentData.vSetAttending( 0 );

				// 血管造影室の主担当医療技師が対応開始したと設定します。
				ArrayListExaminationFastRooms.get(i).cGetCurrentClinicalEngineerAgent().vSetAttending(1);

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListExaminationFastRooms.get(i).erGetTriageNode() ) );
					cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "超音波室へ移動開始" + "," + "診察室");
				}
				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListExaminationFastRooms.size() )
		{
			// 空きがない場合は患者の検査室待ちフラグをONにして、待合室に移動します。
			cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "Fast室満室" + "," + "診察室");
			erPAgent.vSetExaminationFastRoomWaitFlag( 1 );
			erPAgent.vSetConsultationDoctorAgent(erConsultationDoctorAgentData);
			vJudgeMoveWaitingRoom( erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
		}
	}

	/**
	 * <PRE>
	 *    手術室へ移動可能かどうかを判定し、移動できる場合は待合室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListOperationRooms			全手術室
	 * @param erWaitingRoom						待合室
	 * @param erConsultationDoctorAgentData		担当医師エージェント
	 * @param erPAgent							患者エージェント
	 * @throws NullPointerException				NULL例外クラス
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveOperationRoom( ArrayList<EROperationRoom> ArrayListOperationRooms, ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgentData, ERPatientAgent erPAgent ) throws NullPointerException
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
				cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "手術室へ移動" + "," + "診察室");
				// 手術室待ちフラグをOFFにします。
				erPAgent.vSetOperationRoomWaitFlag( 0 );

				// 患者のいる位置を手術室に変更します。
				erPAgent.vSetLocation( 2 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 診察室へ患者エージェントを移動します。
				ArrayListOperationRooms.get(i).vSetPatientAgent( erPAgent );

			// 医師、看護師エージェントへメッセージを送信します。

				// 手術室の医師エージェントへメッセージを送信します。
				erOperationDoctorAgent = ArrayListOperationRooms.get(i).cGetSurgeonDoctorAgent();
				erConsultationDoctorAgentData.vSendToDoctorAgentMessage( erPAgent, (int)erConsultationDoctorAgentData.getId(), (int)erOperationDoctorAgent.getId() );

				// 手術室の看護師エージェントへメッセージを送信します。
				for( j = 0;j < ArrayListOperationRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erOperationNurseAgent = ArrayListOperationRooms.get(i).cGetNurseAgent(j);
					erConsultationDoctorAgentData.vSendToNurseAgentMessage( erPAgent, erOperationNurseAgent, (int)erConsultationDoctorAgentData.getId(), (int)erOperationNurseAgent.getId() );
				}
				// 医師エージェントの対応を終了します。
				erConsultationDoctorAgentData.vSetAttending( 0 );

				// 手術室の執刀医エージェントを対応中に設定します。
				ArrayListOperationRooms.get(i).cGetSurgeonDoctorAgent().vSetAttending(1);
				ArrayListOperationRooms.get(i).cGetSurgeonDoctorAgent().vSetSurgeon(1);

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListOperationRooms.get(i).erGetTriageNode() ) );
					cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "手術室へ移動開始" + "," + "診察室");
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
			cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgentData.getId() + "," + "手術室満室" + "," + "診察室");
			erPAgent.vSetOperationRoomWaitFlag( 1 );
			vJudgeMoveWaitingRoom( erWaitingRoom, erConsultationDoctorAgentData, erPAgent );
		}
	}

	/**
	 * <PRE>
	 *    初療室へ移動可能かどうかを判定し、移動できる場合は初療室へ移動します。
	 *    移動できない場合は重症観察室へいったん移動します。
	 * </PRE>
	 * @param ArrayListEmergencyRooms					全初療室
	 * @param ArrayListSereveInjuryObservationRooms		全重症観察室
	 * @param erWaitingRoom								待合室
	 * @param erConsultationDoctorAgent					担当医師エージェント
	 * @param erPAgent									移動する患者エージェント
	 * @throws NullPointerException						NULLアクセス例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveEmergencyRoom( ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSereveInjuryObservationRooms, ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgent, ERPatientAgent erPAgent ) throws NullPointerException
	{
		int i,j;
		int iJudgeCount = 0;
		ERDoctorAgent erEmergencyDoctorAgent;
		ERNurseAgent erEmergencyNurseAgent;
		ERClinicalEngineerAgent erEmergencyClinicalEngineerAgent;
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			// 初療室に空きがある場合
			if( ArrayListEmergencyRooms.get(i).isVacant() == true )
			{
				cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "初療室へ移動" + "," + "診察室");
				// 初療室待ちフラグをOFFにします。
				erPAgent.vSetEmergencyRoomWaitFlag( 0 );

				// 患者のいる位置を初療室に変更します。
				erPAgent.vSetLocation( 3 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 初療室へ患者エージェントを移動します。
				ArrayListEmergencyRooms.get(i).vSetPatientAgent( erPAgent );

			// 看護師、医師、技士エージェントへメッセージを送信します。
				// 初療室の看護師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erEmergencyNurseAgent = ArrayListEmergencyRooms.get(i).cGetNurseAgent(j);
					erConsultationDoctorAgent.vSendToNurseAgentMessage( erPAgent, erEmergencyNurseAgent, (int)erConsultationDoctorAgent.getId(), (int)erEmergencyNurseAgent.getId() );
				}
				// 初療室の医師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetDoctorAgentsNum(); j++ )
				{
					erEmergencyDoctorAgent = ArrayListEmergencyRooms.get(i).cGetDoctorAgent( j );
					erConsultationDoctorAgent.vSendToDoctorAgentMessage( erPAgent, (int)erConsultationDoctorAgent.getId(), (int)erEmergencyDoctorAgent.getId() );
				}
				// 初療室の医療技師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
				{
					erEmergencyClinicalEngineerAgent = ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgent(j);
					erConsultationDoctorAgent.vSendToEngineerAgentMessage( erPAgent, erEmergencyClinicalEngineerAgent, (int)erConsultationDoctorAgent.getId(), (int)erEmergencyClinicalEngineerAgent.getId() );
				}

				// 医師エージェントの対応を終了します。
				erConsultationDoctorAgent.vSetAttending( 0 );

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListEmergencyRooms.get(i).erGetTriageNode() ) );
					cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "初療室へ移動開始" + "," + "診察室");
				}
				erPAgent = null;

				// 初療室で担当する医師エージェントを設定します。
				ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetSurgeon(1);
				ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetAttending(1);

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
			cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "初療室満室" + "," + "診察室");
			erPAgent.vSetEmergencyRoomWaitFlag( 1 );
			vJudgeMoveSereveInjuryObservationRoom( ArrayListSereveInjuryObservationRooms, erWaitingRoom, erConsultationDoctorAgent, erPAgent );
		}
	}

	/**
	 * <PRE>
	 *    重症観察室へ移動可能かどうかを判定し、移動できる場合は重症観察室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListSereveInjuryObservationRooms		全重症観察室
	 * @param erWaitingRoom								待合室オブジェクト
	 * @param erConsultationDoctorAgent					医師エージェント
	 * @param erPAgent									患者エージェント
	 * @throws NullPointerException						NULLアクセス例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveSereveInjuryObservationRoom( ArrayList<ERSevereInjuryObservationRoom> ArrayListSereveInjuryObservationRooms, ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgent, ERPatientAgent erPAgent ) throws NullPointerException
	{
		int i,j;
		int iJudgeCount = 0;
		ERNurseAgent erSevereInjuryObservationNurseAgent;
		// 初療室に空きがある場合
		for( i = 0;i < ArrayListSereveInjuryObservationRooms.size(); i++ )
		{
			if( ArrayListSereveInjuryObservationRooms.get(i).isVacant() == true )
			{
				cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "重症観察室へ移動" + "," + "診察室");
				// 重症観察室待ちフラグをOFFにします。
				erPAgent.vSetSereveInjuryObservationRoomWaitFlag( 0 );

				// 患者のいる位置を重症観察室に変更します。
				erPAgent.vSetLocation( 5 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 初療室へ患者エージェントを移動します。
				ArrayListSereveInjuryObservationRooms.get(i).vSetPatientAgent( erPAgent );

				// 重症観察室の看護師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListSereveInjuryObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					// 看護師エージェントへメッセージを送信します。
					erSevereInjuryObservationNurseAgent = ArrayListSereveInjuryObservationRooms.get(i).erGetNurseAgent(j);
					erConsultationDoctorAgent.vSendToNurseAgentMessage( erPAgent, erSevereInjuryObservationNurseAgent, (int)erConsultationDoctorAgent.getId(), (int)erSevereInjuryObservationNurseAgent.getId() );
				}

				// 医師エージェントの対応を終了します。
				erConsultationDoctorAgent.vSetAttending( 0 );

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListSereveInjuryObservationRooms.get(i).erGetTriageNode() ) );
					cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "重症観察室へ移動開始" + "," + "診察室");
				}

				erPAgent = null;

				// 空いている看護師に割り当てます。
				ArrayListSereveInjuryObservationRooms.get(i).bAssignVacantNurse();

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListSereveInjuryObservationRooms.size() )
		{
			// 空きがない場合は患者の重症観察待ちフラグをONにして、待合室に移動します。
			cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "重症観察室満室" + "," + "診察室");
			erPAgent.vSetSereveInjuryObservationRoomWaitFlag( 1 );
			vJudgeMoveWaitingRoom( erWaitingRoom, erConsultationDoctorAgent, erPAgent  );
		}
	}

	/**
	 * <PRE>
	 *   一般病棟への移動判定を行います。
	 * </PRE>
	 * @param ArrayListGeneralWardRooms			全一般病棟
	 * @param erWaitingRoom						待合室
	 * @param erConsultationDoctorAgent			担当医師エージェント
	 * @param erPAgent							手術をうける患者エージェント
	 * @throws NullPointerException				Nullアクセス例外
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	private void vJudgeMoveGeneralWardRoom( ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms, ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgent, ERPatientAgent erPAgent ) throws NullPointerException
	{
		int i,j;
		int iJudgeCount = 0;
		ERDoctorAgent erGeneralWardDoctorAgent;
		ERNurseAgent erGeneralWardNurseAgent;
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			// 初療室に空きがある場合
			if( ArrayListGeneralWardRooms.get(i).isVacant() == true )
			{
				cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "一般病棟へ移動" + "," + "診察室");
				// 一般病棟待機フラグをOFFにします。
				erPAgent.vSetGeneralWardRoomWaitFlag( 0 );

				// 患者のいる位置を一般病棟に変更します。
				erPAgent.vSetLocation( 8 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 初療室へ患者エージェントを移動します。
				ArrayListGeneralWardRooms.get(i).vSetPatientAgent( erPAgent );

			// 看護師、医師エージェントへメッセージを送信します。
				// 医師へメッセージを送信します。
				for(j = 0;j < ArrayListGeneralWardRooms.get(i).iGetDoctorAgentsNum(); j++ )
				{
					// 看護師、医師、技士エージェントへメッセージを送信します。
					erGeneralWardDoctorAgent = ArrayListGeneralWardRooms.get(i).cGetDoctorAgent(j);
					erConsultationDoctorAgent.vSendToDoctorAgentMessage( erPAgent, (int)erConsultationDoctorAgent.getId(), (int)erGeneralWardDoctorAgent.getId() );
				}
				for(j = 0;j < ArrayListGeneralWardRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					// 看護師、医師、技士エージェントへメッセージを送信します。
					erGeneralWardNurseAgent = ArrayListGeneralWardRooms.get(i).cGetNurseAgent(j);
					erConsultationDoctorAgent.vSendToNurseAgentMessage( erPAgent, erGeneralWardNurseAgent, (int)erConsultationDoctorAgent.getId(), (int)erGeneralWardNurseAgent.getId() );
				}

				// 医師エージェントの対応を終了します。
				erConsultationDoctorAgent.vSetAttending( 0 );

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListGeneralWardRooms.get(i).erGetTriageNode() ) );
					cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "一般病棟へ移動開始" + "," + "診察室");
				}
				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListGeneralWardRooms.size() )
		{
			// 空きがない場合は一般病棟待機フラグをONにしてそのまま一般病棟の空きを判定します。
			cConsultationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + "一般病棟満室" + "," + "診察室");
			erPAgent.vSetGeneralWardRoomWaitFlag( 1 );

			// 空きがない場合は転院を促します。つまり退院処置を行います。
			if( rnd.NextUnif() > 1.0 )
			{
				erPAgent.vSetDisChargeFlag( 1 );
				vJudgeMoveWaitingRoom( erWaitingRoom, erConsultationDoctorAgent, erPAgent );
			}
		}
	}

	/**
	 * <PRE>
	 *    移動する患者エージェントを取得します。
	 * </PRE>
	 * @param erPAgent		移動する患者エージェントのインスタンス
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetPatientAgent( ERPatientAgent erPAgent )
	{
		erCurrentPatientAgent = erPAgent;
	}

	/**
	 * <PRE>
	 *    対応が終了した患者を削除します。
	 * </PRE>
	 */
	public void vRemovePatientAgent()
	{
		erCurrentPatientAgent = null;
	}

	/**
	 * <PRE>
	 *    診察室の対応中かどうかを取得します。
	 * </PRE>
	 * @return false 対応している。
	 *         true  対応していない。
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public boolean isVacant()
	{
		boolean bRet = true;

		// 部屋は存在しているが所属医師がいない場合は対応できないので空いていないとします。
		// 通常はないが・・・。
		if( erConsultationDoctorAgent == null )
		{
			bRet = false;
		}
		// 所属医師が全員対応中の場合、空いていないとします。
		if( erConsultationDoctorAgent.iGetAttending() == 1 )
		{
			bRet = false;
		}
		return bRet;
	}

	/**
	 * <PRE>
	 *   診察室の医師エージェントを取得します。
	 * </PRE>
	 * @return		担当している医師エージェントのインスタンス
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public ERDoctorAgent cGetDoctorAgent()
	{
		return erConsultationDoctorAgent;
	}

	/**
	 * <PRE>
	 *   診察室の看護師エージェントを取得します。
	 * </PRE>
	 * @param i	看護師番号
	 * @return	所属している看護師エージェント
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public ERNurseAgent cGetNurseAgent( int i )
	{
		return ArrayListNurseAgents.get(i);
	}

	/**
	 * <PRE>
	 *   診察室の看護師エージェントの数を取得します。
	 * </PRE>
	 * @return	所属している看護師エージェント数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public int iGetNurseAgentsNum()
	{
		return ArrayListNurseAgents.size();
	}

	/**
	 * <PRE>
	 *   初療室の患者エージェントの数を取得します。
	 *   nullでなければ1人いるので1と返却します。
	 * </PRE>
	 * @return	診察中ならば1を返却し、そうでないならば0を返却します。
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public int iGetPatientAgentsNum()
	{
		return erCurrentPatientAgent != null ? 1 : 0;
	}

	@Override
	public void action(long timeStep)
	{
		// TODO 自動生成されたメソッド・スタブ

		try
		{
			synchronized( csConsultationRoomCriticalSection )
			{
				// 死亡患者がいる場合は削除をする。
				if( erCurrentPatientAgent != null )
				{
					// 登録されているエージェントで離脱したエージェントがいるかどうかを判定します。
					if( erCurrentPatientAgent.isExitAgent() == true )
					{
						// いる場合は、ファイルに書き出しを実行します。
						erCurrentPatientAgent.vFlushFile( 0 );
					}
					if( erCurrentPatientAgent.iGetSurvivalFlag() == 0 || erCurrentPatientAgent.iGetDisChargeFlag() == 1 )
					{
						erCurrentPatientAgent = null;
					}
				}
			}
		}
		catch( IOException ioe )
		{

		}
	}

	/**
	 * <PRE>
	 *    診察室のログ出力を設定します。
	 * </PRE>
	 * @param log	ロガークラスインスタンス
	 */
	public void vSetLog(Logger log)
	{
		// TODO 自動生成されたメソッド・スタブ
		cConsultationRoomLog = log;
	}

	/**
	 * <PRE>
	 *    診察室のX座標を取得します。
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
	 *    診察室のY座標を取得します。
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
	 *    診察室の横幅を取得します。
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
	 *    診察室の縦幅を取得します。
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
	 *    診察室の階数を取得します。
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
	 *    診察室のX座標を格納します。
	 * </PRE>
	 * @param iData	X座標
	 */
	public void vSetX( int iData )
	{
		iDrawX = iData;
	}

	/**
	 * <PRE>
	 *    診察室のY座標を格納します。
	 * </PRE>
	 * @param iData	Y座標
	 */
	public void vSetY( int iData )
	{
		iDrawY = iData;
	}

	/**
	 * <PRE>
	 *    診察室のZ座標を格納します。
	 * </PRE>
	 * @param iData	Z座標
	 */
	public void vSetZ( int iData )
	{
		iDrawZ = iData;
	}

	/**
	 * <PRE>
	 *    診察室の横幅を格納します。
	 * </PRE>
	 * @param iData	横幅
	 */
	public void vSetWidth( int iData )
	{
		iDrawWidth = iData;
	}

	/**
	 * <PRE>
	 *    診察室の縦幅を格納します。
	 * </PRE>
	 * @param iData	縦幅
	 */
	public void vSetHeight( int iData )
	{
		iDrawHeight = iData;
	}

	/**
	 * <PRE>
	 *    診察室の階数を格納します。
	 * </PRE>
	 * @param iData	階数
	 */
	public void vSetF( int iData )
	{
		iDrawF = iData;
	}

	/**
	 * <PRE>
	 *   診察室に所属しているエージェントの座標を設定します。
	 * </PRE>
	 */
	public void vSetAffiliationAgentPosition()
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;

		double lfX = 0.0;
		double lfY = 0.0;
		double lfZ = 0.0;

		// 医師エージェントの位置を設定します。
		lfX = this.getPosition().getX();
		lfY = this.getPosition().getY();
		lfZ = this.getPosition().getZ();
		erConsultationDoctorAgent.setPosition( lfX, lfY, lfZ );
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			// 看護師エージェントの位置を設定します。
			lfX = this.getPosition().getX()+10*(rnd.NextUnif());
			lfY = this.getPosition().getY()+10*(rnd.NextUnif());
			lfZ = this.getPosition().getZ();
			ArrayListNurseAgents.get(i).setPosition( lfX, lfY, lfZ );
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
	 *    現在選択されている診察室のノードを取得します。
	 * </PRE>
	 * @return	診察室のノード
	 */
	public ERTriageNode erGetTriageNode()
	{
		return erTriageNode;
	}

	/**
	 * <PRE>
	 *   診察室のノードを設定します。
	 * </PRE>
	 * @param erNode	設定するノードインスタンス（診察室）
	 */
	public void vSetTriageNode( ERTriageNode erNode )
	{
		erTriageNode = erNode;
	}

	/**
	 * <PRE>
	 *   逆シミュレーションモードを設定します。
	 *   所属する医師及び看護師エージェントも合わせて設定します。
	 * </PRE>
	 * @param iMode	0 通常シミュレーションモード
	 * 				1 GUIモード
	 * 				2 逆シミュレーションモード
	 */
	public void vSetInverseSimMode( int iMode )
	{
		int i;
		iInverseSimFlag = iMode;

		erConsultationDoctorAgent.vSetInverseSimMode( iMode );

		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetInverseSimMode( iMode );
		}
	}

	/**
	 * <PRE>
	 *    現時点で患者がいるかどうかを取得します。
	 * </PRE>
	 * @return	診察中の患者エージェントがいる場合は1を返却、そうでない場合は0を返却。
	 */
	public synchronized int iGetPatientInARoom()
	{
		// TODO 自動生成されたメソッド・スタブ

		synchronized( csConsultationRoomCriticalSection )
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
	 *    クリティカルセクションを設定します。
	 * </PRE>
	 * @param cs	クリティカルセクションのインスタンス
	 */
	public void vSetCriticalSection(Object cs )
	{
		// TODO 自動生成されたメソッド・スタブ
		csConsultationRoomCriticalSection = cs;
	}

	/**
	 * <PRE>
	 *   メルセンヌツイスターインスタンスを設定します。
	 * </PRE>
	 * @param sfmtRandom メルセンヌツイスターインスタンス(部屋自体)
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vSetConsultationRoomRandom( Rand sfmtRandom )
	{
		// TODO 自動生成されたメソッド・スタブ
		rnd = sfmtRandom;
	}


	/**
	 * <PRE>
	 *   メルセンヌツイスターインスタンスを設定します。
	 *   (室所属する医師エージェント)
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vSetDoctorsRandom()
	{
		erConsultationDoctorAgent.vSetRandom( rnd );
	}

	/**
	 * <PRE>
	 *   メルセンヌツイスターインスタンスを設定します。
	 *   (室所属する看護師エージェント)
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vSetNursesRandom()
	{
		int i;
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetRandom( rnd );
		}
	}
	/**
	 * <PRE>
	 *   現時点でのトリアージ緊急度別受診数の患者の数を求めます。
	 * </PRE>
	 * @param iCategory	トリアージ緊急度のカテゴリ(1,2,3,4,5)
	 * @author kobayashi
	 * @since 2016/07/27
	 * @return	緊急度別トリアージ受診人数
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

	public double lfGetLongestStayHospitalTotalTime()
	{
		return lfLongestTotalTime;
	}

	public double lfGetLastBedTime()
	{
		return lfLastBedTime;
	}
}
