package triage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jp.ac.nihon_u.cit.su.furulab.fuse.Environment;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.collision.TreeSpaceManagerBinary;
import triage.agent.ERClinicalEngineerAgentException;
import triage.agent.ERDoctorAgentException;
import triage.agent.ERNurseAgentException;
import triage.agent.ERPatientAgent;
import triage.room.ERConsultationRoom;
import triage.room.ERElevator;
import triage.room.EREmergencyRoom;
import triage.room.ERExaminationAngiographyRoom;
import triage.room.ERExaminationCTRoom;
import triage.room.ERExaminationFastRoom;
import triage.room.ERExaminationMRIRoom;
import triage.room.ERExaminationXRayRoom;
import triage.room.ERGeneralWardRoom;
import triage.room.ERHighCareUnitRoom;
import triage.room.ERIntensiveCareUnitRoom;
import triage.room.ERObservationRoom;
import triage.room.EROperationRoom;
import triage.room.EROutside;
import triage.room.ERSevereInjuryObservationRoom;
import triage.room.ERStairs;
import triage.room.ERWaitingRoom;
import utility.csv.CCsv;
import utility.initparam.InitInverseSimParam;
import utility.initparam.InitSimParam;
import utility.node.ERTriageNode;
import utility.node.ERTriageNodeManager;
import utility.sfmt.Rand;

/**
 * 病院の救急部門を表すクラスです。
 * このプログラムではこのクラスを含めすべての部屋をエージェントとして定義しています。
 * そのようにすることにより、いろいろと都合がよいためそのようにしております。
 * 各部屋、各構成エージェントすべてがこのクラスにまとまっています。
 * 逆シミュレーションではこれを複数インスタンスを生成して各１個体として生成します。
 * 使用方法は次の通りです。<br>
 * 初期化　　vInitialize　<br>
 * 実行　　　action　<br>
 * 終了処理　vTerminate　<br>
 * 初期化で読み込みファイル（各部屋数、部屋を構成するエージェントのパラメータ）、
 * シミュレーションエンジン、ログ、クリティカルセクション、各種パラメータを指定します。
 * 指定すると、それらをすべて読み込み、患者エージェントを生成します。
 *
 *
 * @author kobayashi
 *
 */
public class ERDepartment extends Agent
{
	private static final long serialVersionUID = -7012846720434796778L;

	private int iConsultationRoomNum = 3;
	private int iConsultationDoctorNum = 1;
	private int iConsultationNurseNum = 2;
	private int iXRayRoomNum = 2;
	private int iCTRoomNum = 1;
	private int iMRIRoomNum = 1;
	private int iAngiographyRoomNum = 1;
	private int iFastRoomNum = 1;
	private int iXRayClinicalEngineerNum = 3;
	private int iCTClinicalEngineerNum = 3;
	private int iMRIClinicalEngineerNum = 3;
	private int iAngiographyClinicalEngineerNum = 3;
	private int iFastClinicalEngineerNum = 3;
	private int iEmergencyRoomNum = 2;
	private int iEmergencyDoctorNum = 3;
	private int iEmergencyNurseNum = 6;
	private int iEmergencyClinicalEngineerNum = 2;
	private int iOperationRoomNum = 2;
	private int iOperationDoctorNum = 2;
	private int iOperationNurseNum = 4;
	private int iObservationRoomNum = 4;
	private int iObservationNurseNum = 8;
	private int iHighCareUnitRoomNum = 4;
	private int iHighCareUnitDoctorNum = 2;
	private int iHighCareUnitNurseNum = 4;
	private int iIntensiveCareUnitRoomNum = 6;
	private int iIntensiveCareUnitDoctorNum = 2;
	private int iIntensiveCareUnitNurseNum = 4;
	private int iGeneralWardRoomNum = 10;
	private int iGeneralWardDoctorNum = 1;
	private int iGeneralWardNurseNum = 4;
	private int iSevereInjuryObservationRoomNum = 4;
	private int iSevereInjuryObservationNurseNum = 8;
	private int iWaitingRoomNum = 1;
	private int iWaitingNurseNum = 3;
	private ArrayList<ERConsultationRoom> ArrayListConsultationRooms;
	private ArrayList<EREmergencyRoom> ArrayListEmergencyRooms;
	private ArrayList<ERExaminationXRayRoom> ArrayListExaminationXRayRooms;
	private ArrayList<ERExaminationCTRoom> ArrayListExaminationCTRooms;
	private ArrayList<ERExaminationMRIRoom> ArrayListExaminationMRIRooms;
	private ArrayList<ERExaminationAngiographyRoom> ArrayListExaminationAngiographyRooms;
	private ArrayList<ERExaminationFastRoom> ArrayListExaminationFastRooms;
	private ArrayList<ERObservationRoom> ArrayListObservationRooms;
	private ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms;
	private ArrayList<EROperationRoom> ArrayListOperationRooms;
	private ArrayList<ERHighCareUnitRoom> ArrayListHighCareUnitRooms;
	private ArrayList<ERIntensiveCareUnitRoom> ArrayListIntensiveCareUnitRooms;
	private ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms;
	private ERWaitingRoom erWaitingRoom;
	private ArrayList<ERStairs> ArrayListStairs;
	private ArrayList<ERElevator> ArrayListElevators;
//	private ArrayList<EROtherRoom> ArrayListOtherRooms;
	private EROutside erOutside;

	private ArrayList<Long> ArrayListDoctorAgentIds;
	private ArrayList<Long> ArrayListNurseAgentIds;
	private ArrayList<Long> ArrayListClinicalEngineerAgentIds;

	private double lfTotalTime;
	private double lfEndTime;

	private SimulationEngine erEngine;
	private Environment erEnvironment;

	private CCsv csvWriteERData;

	private ERFinisher erFinisher = null;					// 終了処理用クラス
	private Logger ERDepartmentLog;							// ログ出力用クラスインスタンス
	int iCurrentStatusFlag = 0;								// シミュレーション進捗状況を表すステータス
	private double lfSimulationStep;						// シミュレーションの実行タイムステップ

	private int[][] ppiAxisX;
	private int[][] ppiAxisY;
	private int[][] ppiAxisZ;
	private int[] piFloor;
	private ERTriageNodeManager cErNodeManager;

	private int[][] ppiInnerOuter;

	private int iInverseSimMode;
	private Rand rnd;

	private int iTotalPatientNum;
	private int lfSurvivalNum;
	private int iDeathNum;
	private double lfSurvivalProbability;

	private Object csErDepartmentCriticalSection;

	private int iFileWriteModeFlag;							// 長時間シミュレーション用フラグ

	private utility.sfmt.Rand sfmtErDepartmentRandom;

	// 逆シミュレーション用パラメータ
	private int iTotalEdAdmittedAgentNum;					// 初療室、診察室、ICU、HCUの患者の数
	private int iEdAdmittedAgentNum;						// 初療室、診察室の患者の数
	private int iEdVentilatorsNum;							// 人工呼吸器をつけた患者の数
	private int iWaitingRoomPatientNum;						// 待合室に待つ患者の数
	private int[] piTriageCategoryPatientNum;				// トリアージ別受診数
	private double lfLongestAdmittedTime;					// 患者が最も長く受診されていた時間
	private double lfLastBedTime;							// 最後に患者が病院に到達してから入院した時間
	private double lfLongestTotalTime;						// 最後に入院した場合の患者の到達から入院までの時間（秒単位）計算は時間単位
	private int iTotalPatientAgentNum;						// 現時点での患者の総数

	private int iCurrentTotalEdAdmittedAgentNum;			// 初療室、診察室、ICU、HCUの患者の数
	private int iCurrentEdAdmittedAgentNum;					// 初療室、診察室の患者の数
	private int iCurrentEdVentilatorsNum;					// 人工呼吸器をつけた患者の数
	private int iCurrentWaitingRoomPatientNum;				// 待合室に待つ患者の数
	private int[] piCurrentTriageCategoryPatientNum;		// トリアージ別受診数
	private double lfCurrentLongestAdmittedTime;			// 患者が最も長く受診されていた時間
	private double lfCurrentLastBedTime;					// 最後に患者が病院に到達してから入院した時間
	private double lfCurrentLongestTotalTime;				// 最後に入院した場合の患者の到達から入院までの時間（秒単位）計算は時間単位
	private int iCurrentTotalPatientAgentNum;				// 現時点での患者の総数

	private double lfNedocs;								// Naitional Emergency Department OverCrowding Study 救急部門の混雑状況を表した指標
	private double lfEdwin;									// Emergency Department Work Index 救急部門の混雑状況を表した指標
	private double lfEdWorkScore;							// 救急部門の混雑状況及び医師、看護師の負荷状況を表した指標

	private CCsv csvWriteAgentData;							// 終了データ出力ファイル
	private CCsv csvWriteAgentStartData;					// 開始データ出力ファイル

	private double plfMaxInvSimParam[];						// 評価指標が最大になる場合のパラメータ格納配列
	private double lfMaxNedocs;								// 1シミュレーション中の最大NEDOCS
	private double lfMaxEdwin;								// 1シミュレーション中の最大EDWIN
	private double lfMaxEdWorkScore;						// 1シミュレーション中の最大ED Work Score

	private boolean iFinishAgentFlag;						// 各エージェントの処理がすべて終了したことを表すフラグ

	private int iPatientArrivalMode;						// 災害モードフラグ

	private int iGenerationPatientMode;						// 別スレッドからの患者到達制御フラグ

	private InitSimParam initSimParam;						// 初期設定ファイル操作用変数

	private ArrayList<ERPatientAgent> ArrayListPatientAgents;// 来院する患者エージェントのインスタンス

	private int iMonthCount = 0;								// 患者出現分布用１ヶ月換算
	private int iDayCount = 0;									// 患者出現分布用１日換算
	private int iYearCount = 0;									// 患者出現分布用1年換算

	private double lfOneWeekArrivalPatientPepole = 1.0;				// 患者の1日単位での分布
	private double lfOneMonthArrivalPatientPepole = 1.0;			// 患者の1ヶ月単位での分布
	private double lfOneYearArrivalPatientPepole = 1.0;				// 患者の1年単位での分布
	private double lfArrivalPatientPepole;						// 患者到達人数


	public ERDepartment()
	{
		vInitialize();
	}

	/**
	 * <PRE>
	 *    初期化処理を実行します。
	 * </PRE>
	 */
	public void vInitialize()
	{
		ArrayListConsultationRooms = new ArrayList<ERConsultationRoom>();
		ArrayListEmergencyRooms = new ArrayList<EREmergencyRoom>();
		ArrayListExaminationXRayRooms = new ArrayList<ERExaminationXRayRoom>();
		ArrayListExaminationCTRooms = new ArrayList<ERExaminationCTRoom>();
		ArrayListExaminationMRIRooms = new ArrayList<ERExaminationMRIRoom>();
		ArrayListExaminationAngiographyRooms = new ArrayList<ERExaminationAngiographyRoom>();
		ArrayListExaminationFastRooms = new ArrayList<ERExaminationFastRoom>();
		ArrayListObservationRooms = new ArrayList<ERObservationRoom>();
		ArrayListSevereInjuryObservationRooms = new ArrayList<ERSevereInjuryObservationRoom>();
		ArrayListOperationRooms = new ArrayList<EROperationRoom>();
		ArrayListHighCareUnitRooms = new ArrayList<ERHighCareUnitRoom>();
		ArrayListIntensiveCareUnitRooms = new ArrayList<ERIntensiveCareUnitRoom>();
		ArrayListGeneralWardRooms = new ArrayList<ERGeneralWardRoom>();
		erWaitingRoom = new ERWaitingRoom();
		ArrayListStairs = new ArrayList<ERStairs>();
		ArrayListElevators = new ArrayList<ERElevator>();
//		ArrayListOtherRooms = new ArrayList<EROther>();
		ArrayListDoctorAgentIds = new ArrayList<Long>();
		ArrayListNurseAgentIds = new ArrayList<Long>();
		ArrayListClinicalEngineerAgentIds = new ArrayList<Long>();
		ArrayListPatientAgents = new ArrayList<ERPatientAgent>();
		lfTotalTime = 0.0;
		lfEndTime = 24.0*3600.0;
		iFileWriteModeFlag = 0;

		iTotalEdAdmittedAgentNum		= 0;					// 初療室、診察室、ICU、HCUの患者の数
		iEdAdmittedAgentNum				= 0;					// 初療室、診察室の患者の数
		iEdVentilatorsNum				= 0;					// 人工呼吸器をつけた患者の数
		iWaitingRoomPatientNum			= 0;					// 待合室に待つ患者の数
		piTriageCategoryPatientNum		=  new int[6];			// トリアージ別受診数
		lfLongestAdmittedTime			= 0;					// 患者が最も長く受診されていた時間
		lfLastBedTime					= 0;					// 最後に患者が病院に到達してから入院した時間
		iTotalPatientAgentNum			= 0;					// 現時点での患者の総数

		iCurrentTotalEdAdmittedAgentNum		= 0;					// 初療室、診察室、ICU、HCUの患者の数
		iCurrentEdAdmittedAgentNum			= 0;					// 初療室、診察室の患者の数
		iCurrentEdVentilatorsNum			= 0;					// 人工呼吸器をつけた患者の数
		iCurrentWaitingRoomPatientNum		= 0;					// 待合室に待つ患者の数
		piCurrentTriageCategoryPatientNum	=  new int[6];			// トリアージ別受診数
		lfCurrentLongestAdmittedTime		= 0;					// 患者が最も長く受診されていた時間
		lfCurrentLastBedTime				= 0;					// 最後に患者が病院に到達してから入院した時間
		iCurrentTotalPatientAgentNum		= 0;					// 現時点での患者の総数
		plfMaxInvSimParam = new double[46];						// 評価指標が最大になる場合のパラメータ格納配列(数は逆シミュレーションのパラメータ参照)
		lfMaxNedocs = -Double.MAX_VALUE;						// 1シミュレーション中の最大NEDOCS
		lfMaxEdwin = -Double.MAX_VALUE;							// 1シミュレーション中の最大EDWIN
		lfMaxEdWorkScore = -Double.MAX_VALUE;					// 1シミュレーション中の最大ED Work Score

		iFinishAgentFlag = false;
		iGenerationPatientMode = 0;
	}

	/**
	 * <PRE>
	 *    初期化を実行します。
	 *    部屋番号は次の通りです。（看護師、医師、患者のみ）
	 *    1 診察室
	 *    2 手術室
	 *    3 初療室
	 *    4 観察室
	 *    5 重症観察室
	 *    6 集中治療室
	 *    7 高度治療室
	 *    8 一般病棟
	 *    9 待合室
	 *    10 X線室
	 *    11 CT室
	 *    12 MRI室
	 *    13 血管造影検査室
	 *    14 FAST室
	 * </PRE>
	 * @param engine								FUSEのシミュレーションエンジンクラス
	 * @param env 									FUSEの環境クラス
	 * @param strConsultationRoomPath				診察室の設定ファイルパス
	 * @param strOperationRoomPath 					手術室の設定ファイルパス
	 * @param strEmergencyRoomPath 					初療室の設定ファイルパス
	 * @param strObservationRoomPath 				観察室の設定ファイルパス
	 * @param strSevereInjuryObservationRoomPath 	重症観察室の設定ファイルパス
	 * @param strIntensiveCareUnitPath 				集中治療室の設定ファイルパス
	 * @param strHighCareUnitPath 					高度治療室の設定ファイルパス
	 * @param strGeneralWardPath 					一般病棟の設定ファイルパス
	 * @param strWaitingRoomPath 					待合室の設定ファイルパス
	 * @param strXRayRoomPath 						X線室の設定ファイルパス
	 * @param strCTRoomPath 						CT室の設定ファイルパス
	 * @param strMRIRoomPath 						MRI室の設定ファイルパス
	 * @param strAngiographyRoomPath 				血管造影室の設定ファイルパス
	 * @param strFastRoomPath 						超音波室の設定ファイルパス
	 * @param lfPatientPepole 						到達する患者の人数
	 * @param iRandomMode 							患者の発生分布
	 * @param iInverseSimFlag 						逆シミュレーションか否か
	 * @param iFileWriteMode 						ファイル書き込みモード
	 * @param iPatientArrivalMode 					患者到達モード（0:通常,1:災害）
	 * @param sfmtRandom 							メルセンヌツイスターのインスタンス
	 * @param initparam								初期設定パラメータ（合わせ込み用）
	 * @param iInitGeneralWardPatientNum			一般病棟の初期患者数
	 * @param iInitIntensiveCareUnitPatientNum		集中治療室の初期患者数
	 * @param iInitHighCareUnitPatientNum			高度治療室の初期患者数
	 * @throws IOException 							java標準のIO例外クラス
	 */
	public void vInitialize( SimulationEngine engine,
			Environment env,
			String strConsultationRoomPath,
			String strOperationRoomPath,
			String strEmergencyRoomPath,
			String strObservationRoomPath,
			String strSevereInjuryObservationRoomPath,
			String strIntensiveCareUnitPath,
			String strHighCareUnitPath,
			String strGeneralWardPath,
			String strWaitingRoomPath,
			String strXRayRoomPath,
			String strCTRoomPath,
			String strMRIRoomPath,
			String strAngiographyRoomPath,
			String strFastRoomPath,
			double lfPatientPepole,
			int iRandomMode,
			int iInverseSimFlag,
			int iFileWriteMode,
			int iPatientArrivalMode,
			Rand sfmtRandom,
			InitSimParam initparam,
			int iInitGeneralWardPatientNum,
			int iInitIntensiveCareUnitPatientNum,
			int iInitHighCareUnitPatientNum) throws IOException
	{
		int i,j;
		double lfEndLogicalTime = 0.0;

		try
		{
			iInverseSimMode = iInverseSimFlag;
			iFileWriteModeFlag = iFileWriteMode;
			initSimParam = initparam;
			lfArrivalPatientPepole = lfPatientPepole;
			ERDepartmentLog.warning( "クラス名" + "," + this.getClass() + "," + "メソッド名" + "," + "vInitialize" + "," + "," + "ファイル出力開始" + "," );
			vSetConsultationRooms(iConsultationRoomNum, strConsultationRoomPath, engine, sfmtRandom );
			vSetEmergencyRooms(iEmergencyRoomNum, strEmergencyRoomPath, engine, sfmtRandom );
			vSetOperationRooms(iOperationRoomNum, strOperationRoomPath, engine, sfmtRandom );
			vSetObservationRooms(iObservationRoomNum, strObservationRoomPath, engine, sfmtRandom );
			vSetSereveInjuryObservationRooms(iSevereInjuryObservationRoomNum, strSevereInjuryObservationRoomPath, engine, sfmtRandom );
			vSetIntensiveCareUnitRooms(iIntensiveCareUnitRoomNum, strIntensiveCareUnitPath, engine, sfmtRandom );
			vSetHighCareUnitRooms(iHighCareUnitRoomNum, strHighCareUnitPath, engine, sfmtRandom );
			vSetGeneralWardRooms(iGeneralWardRoomNum, strGeneralWardPath, engine, sfmtRandom );
			vSetWaitingRooms( iWaitingRoomNum, lfPatientPepole, strWaitingRoomPath, engine, sfmtRandom );
			vSetExaminationXRayRooms(iXRayRoomNum, strXRayRoomPath, engine, sfmtRandom );
			vSetExaminationCTRooms(iCTRoomNum, strCTRoomPath, engine, sfmtRandom );
			vSetExaminationMRIRooms(iMRIRoomNum, strMRIRoomPath, engine, sfmtRandom );
			vSetExaminationAngiographyRooms(iAngiographyRoomNum, strAngiographyRoomPath, engine, sfmtRandom );
			vSetExaminationFastRooms(iFastRoomNum, strFastRoomPath, engine, sfmtRandom );
			vSetOutside(1, lfPatientPepole, engine, sfmtRandom );

			// 逆シミュレーションモードを設定します。
			vSetInverseSimMode( iInverseSimFlag );

			// 逆シミュレーションモードでなければ、ファイルへ出力します。
			if( iInverseSimFlag == 0 || iInverseSimFlag == 1 )
			{
				// 出力用ファイルを設定します。
				vSetReadWriteFile( "./er/er/", "./er/dc/", "./er/nr/", "./er/ce/", iFileWriteMode );
			}

			// 初療室
			for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
			{
				// 仮に最初に登録されている医師を執刀医とします。
				ArrayListEmergencyRooms.get(i).vSetSurgeonDoctorAgent(0);
			}
			// 手術室
			for( i = 0;i < ArrayListOperationRooms.size(); i++ )
			{
				// 仮に最初に登録されている医師を執刀医とします。
				ArrayListOperationRooms.get(i).vSetSurgeonDoctorAgent(0);
			}
			// 検査室(X線室)
			for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
			{
				ArrayListExaminationXRayRooms.get(i).vSetCurrentClinicalEngineerAgent(0);
			}
			// 検査室(CT室)
			for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
			{
				ArrayListExaminationCTRooms.get(i).vSetCurrentClinicalEngineerAgent(0);
			}
			// 検査室(MRI室)
			for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
			{
				ArrayListExaminationMRIRooms.get(i).vSetCurrentClinicalEngineerAgent(0);
			}
			// 検査室(血管造影室)
			for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
			{
				ArrayListExaminationAngiographyRooms.get(i).vSetCurrentClinicalEngineerAgent(0);
			}

			vSetSimulationEngine( engine );

			erEngine = engine;
			erEnvironment = env;
			lfEndLogicalTime = lfEndTime/3600.0;
			vSetRandom( rnd );

	/*-------------------------------------患者を生成するタイミング(開始)----------------------------------------*/

			// 別スレッドからの患者到達モードでない場合はこちらであらかじめ患者がどのタイミングで到達するのか設定します。
			if( iGenerationPatientMode == 0 )
			{
				// 平時の場合の患者発生分布により患者を生成します。
				// 一日の分布です。
//				double lfSecond = 0.0;
//				for( lfSecond = 0.0; lfSecond < lfEndLogicalTime; lfSecond += 1.0/3600.0 )
//				{
//					// 患者を到達分布にしたがって生成します。(午前8時30分を0秒とする。)
////					erWaitingRoom.vArrivalPatient( lfSecond, 1.0/3600.0, erEngine, iRandomMode, iInverseSimFlag, iFileWriteMode, iPatientArrivalMode, initparam );
//					vArrivalPatient( lfSecond, 1.0/3600.0, erEngine, iRandomMode, iInverseSimFlag, iFileWriteMode, iPatientArrivalMode, initparam );
//				}
				erOutside.vGeneratePatientAgents( lfEndLogicalTime, 1.0/3600.0, erEngine, iRandomMode, iInverseSimFlag, iFileWriteMode, iPatientArrivalMode, initparam );
				erOutside.vSetErDepartmentPatientAgents( this );
			}
			erWaitingRoom.vSetSimulationEndTime( lfEndTime );
			// テスト用
//			erWaitingRoom.vArrivalAlonePatient( engine );

			// 一般病棟、ICU、HCUにあらかじめ患者が入院しているという前提の場合に患者を追加します。
			// 一般病棟に指定人数患者を生成します。
			if( iInitGeneralWardPatientNum > 0 )
			{
				int iPatientNumPerRoom = iInitGeneralWardPatientNum / ArrayListGeneralWardRooms.size();
				if( iPatientNumPerRoom > 0 )
				{
					for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
						for( j = 0;j < iPatientNumPerRoom; j++ )
							ArrayListGeneralWardRooms.get(i).vGeneratePatient( erEngine, iRandomMode, iInverseSimFlag, iFileWriteMode, initparam, ERDepartmentLog, csErDepartmentCriticalSection );
				}
				// 初期にいる患者の人数よりも部屋数のほうが多い場合
				else
				{
					// 1部屋目にすべての患者を入れるということにする。
					for( j = 0;j < iInitGeneralWardPatientNum; j++ )
						ArrayListGeneralWardRooms.get(0).vGeneratePatient( erEngine, iRandomMode, iInverseSimFlag, iFileWriteMode, initparam, ERDepartmentLog, csErDepartmentCriticalSection );
				}
			}
			if( iInitIntensiveCareUnitPatientNum > 0 )
			{
				// ICUに指定人数生成します。
				int iPatientNumPerRoom = iInitIntensiveCareUnitPatientNum / ArrayListIntensiveCareUnitRooms.size();
				if( iPatientNumPerRoom > 0 )
				{
					for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
						for( j = 0;j < iPatientNumPerRoom; j++ )
							ArrayListIntensiveCareUnitRooms.get(i).vGeneratePatient( erEngine, iRandomMode, iInverseSimFlag, iFileWriteMode, initparam, ERDepartmentLog, csErDepartmentCriticalSection );
				}
				// 初期にいる患者の人数よりも部屋数のほうが多い場合
				else
				{
					// 1部屋目にすべての患者を入れるということにする。
					for( j = 0;j < iInitIntensiveCareUnitPatientNum; j++ )
						ArrayListIntensiveCareUnitRooms.get(0).vGeneratePatient( erEngine, iRandomMode, iInverseSimFlag, iFileWriteMode, initparam, ERDepartmentLog, csErDepartmentCriticalSection );
				}
			}
			if( iInitHighCareUnitPatientNum > 0 )
			{
				// HCUに指定人数生成します。
				int iPatientNumPerRoom = iInitHighCareUnitPatientNum / ArrayListHighCareUnitRooms.size();
				if( iPatientNumPerRoom > 0 )
				{
					for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
						for( j = 0;j < iPatientNumPerRoom; j++ )
							ArrayListHighCareUnitRooms.get(i).vGeneratePatient( erEngine, iRandomMode, iInverseSimFlag, iFileWriteMode, initparam, ERDepartmentLog, csErDepartmentCriticalSection );
				}
				// 初期にいる患者の人数よりも部屋数のほうが多い場合
				else
				{
					// 1部屋目にすべての患者を入れるということにする。
					for( j = 0;j < iInitHighCareUnitPatientNum; j++ )
						ArrayListHighCareUnitRooms.get(0).vGeneratePatient( erEngine, iRandomMode, iInverseSimFlag, iFileWriteMode, initparam, ERDepartmentLog, csErDepartmentCriticalSection );
				}
			}
			// 救急部門に到達する人数を取得します。
			iTotalPatientNum = erWaitingRoom.iGetTotalPatientNum()+iInitGeneralWardPatientNum+iInitIntensiveCareUnitPatientNum+iInitHighCareUnitPatientNum;
			lfSurvivalProbability = erWaitingRoom.lfCalcInitialAvgSurvivalProbability();
//			iTotalPatientNum = erOutside.iGetTotalPatientNum()+iInitGeneralWardPatientNum+iInitIntensiveCareUnitPatientNum+iInitHighCareUnitPatientNum;
//			lfSurvivalProbability = erOutside.lfCalcInitialAvgSurvivalProbability();

	/*-------------------------------------患者を生成するタイミング(終了)---------------------------------------*/

			// 初期設定ファイル操作用クラスの設定をします。
			vSetAllInitParam( initparam );

			// 終了条件用の値を設定し、終了条件に達したら自動的に終了します。
			ERDepartmentLog.warning( "クラス名" + "," + this.getClass() + "," + "メソッド名" + "," + "vInitialize" + "," + "終了条件クラス生成開始" + "," + erFinisher + "," );
			erFinisher = new ERFinisher();
			erFinisher.vSetFinishTime( lfEndTime );
			erFinisher.vSetSimulationEngine( erEngine );
			erFinisher.vSetRooms(ArrayListConsultationRooms, ArrayListEmergencyRooms, ArrayListExaminationXRayRooms, ArrayListExaminationCTRooms, ArrayListExaminationMRIRooms, ArrayListExaminationAngiographyRooms, ArrayListExaminationFastRooms, ArrayListObservationRooms, ArrayListSevereInjuryObservationRooms, ArrayListOperationRooms, ArrayListHighCareUnitRooms, ArrayListIntensiveCareUnitRooms, ArrayListGeneralWardRooms, erWaitingRoom);
			ERDepartmentLog.warning( "クラス名" + "," + this.getClass() + "," + "メソッド名" + "," + "vInitialize" + "," + "終了条件クラス生成終了" + "," + erFinisher + "," );
			// エンジンに登録します。
			erEngine.setFinisher( erFinisher );
			ERDepartmentLog.warning( "クラス名" + "," + this.getClass() + "," + "メソッド名" + "," + "vInitialize" + "," + "終了条件クラスエンジンに登録完了" + "," + erFinisher + "," );

		}
		catch( IOException ioe )
		{
			throw(ioe);
		}
		catch (ERClinicalEngineerAgentException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	/**
	 * <PRE>
	 *    初期化を実行します。
	 *    部屋番号は次の通りです。（看護師、医師、患者のみ）
	 *    1 診察室
	 *    2 手術室
	 *    3 初療室
	 *    4 観察室
	 *    5 重症観察室
	 *    6 集中治療室
	 *    7 高度治療室
	 *    8 一般病棟
	 *    9 待合室
	 *    10 X線室
	 *    11 CT室
	 *    12 MRI室
	 *    13 血管造影検査室
	 *    14 FAST室
	 * </PRE>
	 * @param engine								FUSEのシミュレーションエンジンクラス
	 * @param env 									FUSEの環境クラス
	 * @param strEmergencyDepartmentPath 			救急部門の設定ファイルパス
	 * @param strConsultationRoomPath 				診察室の設定ファイルパス
	 * @param strOperationRoomPath 					手術室の設定ファイルパス
	 * @param strEmergencyRoomPath 					初療室の設定ファイルパス
	 * @param strObservationRoomPath 				観察室の設定ファイルパス
	 * @param strSevereInjuryObservationRoomPath 	重症観察室の設定ファイルパス
	 * @param strIntensiveCareUnitPath 				集中治療室の設定ファイルパス
	 * @param strHighCareUnitPath 					高度治療室の設定ファイルパス
	 * @param strGeneralWardPath 					一般病棟の設定ファイルパス
	 * @param strWaitingRoomPath 					待合室の設定ファイルパス
	 * @param strXRayRoomPath 						X線室の設定ファイルパス
	 * @param strCTRoomPath 						CT室の設定ファイルパス
	 * @param strMRIRoomPath 						MRI室の設定ファイルパス
	 * @param strAngiographyRoomPath 				血管造影室の設定ファイルパス
	 * @param strFastRoomPath 						超音波室の設定ファイルパス
	 * @param lfPatientPepole 						到達する患者の人数
	 * @param iRandomMode 							患者の発生分布
	 * @param iInverseSimFlag 						逆シミュレーションか否か
	 * @param iFileWriteMode 						ファイル書き込みモード
	 * @param iPatientArrivalMode 					患者到達モード(0:通常, 1:災害)
	 * @param sfmtRandom 							メルセンヌツイスターのインスタンス
	 * @param initparam								初期設定パrメータ（合わせ込み用）
	 * @param iInitGeneralWardPatientNum			一般病棟の初期患者数
	 * @param iInitIntensiveCareUnitPatientNum		集中治療室の初期患者数
	 * @param iInitHighCareUnitPatientNum			高度治療室の初期患者数
	 */
	public void vInitialize( SimulationEngine engine,
			Environment env,
			String strEmergencyDepartmentPath,
			String strConsultationRoomPath,
			String strOperationRoomPath,
			String strEmergencyRoomPath,
			String strObservationRoomPath,
			String strSevereInjuryObservationRoomPath,
			String strIntensiveCareUnitPath,
			String strHighCareUnitPath,
			String strGeneralWardPath,
			String strWaitingRoomPath,
			String strXRayRoomPath,
			String strCTRoomPath,
			String strMRIRoomPath,
			String strAngiographyRoomPath,
			String strFastRoomPath,
			double lfPatientPepole,
			int iRandomMode,
			int iInverseSimFlag,
			int iFileWriteMode,
			int iPatientArrivalMode,
			Rand sfmtRandom,
			InitSimParam initparam,
			int iInitGeneralWardPatientNum,
			int iInitIntensiveCareUnitPatientNum,
			int iInitHighCareUnitPatientNum)
	{
		try
		{

		// 単体テスト用(その２)
//			iConsultationRoomNum = 1;
//			iConsultationDoctorNum = 1;
//			iConsultationNurseNum = 4;
//			iExaminationRoomNum = 1;
//			iExaminationClinicalEngineerNum = 4;
//			iEmergencyRoomNum = 1;
//			iEmergencyDoctorNum = 2;
//			iEmergencyNurseNum = 6;
//			iEmergencyClinicalEngineerNum = 2;
//			iOperationRoomNum = 1;
//			iOperationDoctorNum = 2;
//			iOperationNurseNum = 4;
//			iObservationRoomNum = 1;
//			iObservationNurseNum = 4;
//			iHighCareUnitRoomNum = 1;
//			iHighCareUnitDoctorNum = 1;
//			iHighCareUnitNurseNum = 4;
//			iIntensiveCareUnitRoomNum = 1;
//			iIntensiveCareUnitDoctorNum = 2;
//			iIntensiveCareUnitNurseNum = 8;
//			iGeneralWardRoomNum = 1;
//			iGeneralWardDoctorNum = 1;
//			iGeneralWardNurseNum = 4;
//			iSevereInjuryObservationRoomNum = 1;
//			iSevereInjuryObservationNurseNum = 4;
//			iWaitingNurseNum = 4;

		// 単体テスト用(その１)
//			iConsultationRoomNum = 1;
//			iConsultationDoctorNum = 1;
//			iConsultationNurseNum = 1;
//			iExaminationRoomNum = 1;
//			iExaminationClinicalEngineerNum = 1;
//			iEmergencyRoomNum = 1;
//			iEmergencyDoctorNum = 1;
//			iEmergencyNurseNum = 1;
//			iEmergencyClinicalEngineerNum = 1;
//			iOperationRoomNum = 1;
//			iOperationDoctorNum = 1;
//			iOperationNurseNum = 1;
//			iObservationRoomNum = 1;
//			iObservationNurseNum = 1;
//			iHighCareUnitRoomNum = 1;
//			iHighCareUnitDoctorNum = 1;
//			iHighCareUnitNurseNum = 1;
//			iIntensiveCareUnitRoomNum = 1;
//			iIntensiveCareUnitDoctorNum = 1;
//			iIntensiveCareUnitNurseNum = 1;
//			iGeneralWardRoomNum = 1;
//			iGeneralWardDoctorNum = 1;
//			iGeneralWardNurseNum = 1;
//			iSevereInjuryObservationRoomNum = 1;
//			iSevereInjuryObservationNurseNum = 1;
//			iWaitingNurseNum = 1;

			// 各部屋に対してパラメータを設定します。
			vSetEmergencyDepartment( strEmergencyDepartmentPath );

			vInitialize( engine, env, strConsultationRoomPath, strOperationRoomPath, strEmergencyRoomPath,
					strObservationRoomPath, strSevereInjuryObservationRoomPath, strIntensiveCareUnitPath,
					strHighCareUnitPath, strGeneralWardPath, strWaitingRoomPath, strXRayRoomPath,
					strCTRoomPath, strMRIRoomPath, strAngiographyRoomPath, strFastRoomPath,
					lfPatientPepole, iRandomMode, iInverseSimFlag, iFileWriteMode, iPatientArrivalMode,
					sfmtRandom, initparam, iInitGeneralWardPatientNum, iInitIntensiveCareUnitPatientNum,
					iInitHighCareUnitPatientNum );
		}
		catch( IOException ioe )
		{
			System.out.println( ioe.getMessage() );
		}
	}

	/**
	 * <PRE>
	 *    描画用各部屋の座標値が記載されたファイルを読み込みます。
	 * </PRE>
	 * @param strEmergencyDepartmentAxisPath				救急部門の描画用座標ファイル名
	 * @param strConsultationRoomAxisPath					診察室の描画用座標ファイル名
	 * @param strOperationRoomAxisPath						手術室の描画用座標ファイル名
	 * @param strEmergencyRoomAxisPath						初療室の描画用座標ファイル名
	 * @param strObservationRoomAxisPath					観察室用の描画用座標ファイル名
	 * @param strSevereInjuryObservationRoomAxisPath		重症観察室用描画座標ファイル名
	 * @param strIntensiveCareUnitAxisPath					集中治療室描画用座標ファイル名
	 * @param strHighCareUnitAxisPath						高度治療室描画用座標ファイル名
	 * @param strGeneralWardAxisPath						一般病棟描画用座標ファイル名
	 * @param strWaitingRoomAxisPath						待合室描画用座標ファイル名
	 * @param strXRayRoomAxisPath							X線室描画用座標ファイル名
	 * @param strCTRoomAxisPath								CT室描画用座標ファイル名
	 * @param strMRIRoomAxisPath							MRI室描画用座標ファイル名
	 * @param strAngiographyRoomAxisPath					血管造影室描画用座標ファイル名
	 * @param strFastRoomAxisPath							FAST室用座標描画用ファイル名
	 * @param strStairsAxisPath								階段描画用ファイル名
	 * @param strElevatorAxisPath							エレベーター描画用ファイル名
	 * @param strOtherRoomAxisPath							その他部屋描画用ファイル名
	 * @throws IOException									java標準のIO例外クラス
	 */
	public void vReadDrawERDepartmentFile(
			String strEmergencyDepartmentAxisPath,
			String strConsultationRoomAxisPath,
			String strOperationRoomAxisPath,
			String strEmergencyRoomAxisPath,
			String strObservationRoomAxisPath,
			String strSevereInjuryObservationRoomAxisPath,
			String strIntensiveCareUnitAxisPath,
			String strHighCareUnitAxisPath,
			String strGeneralWardAxisPath,
			String strWaitingRoomAxisPath,
			String strXRayRoomAxisPath,
			String strCTRoomAxisPath,
			String strMRIRoomAxisPath,
			String strAngiographyRoomAxisPath,
			String strFastRoomAxisPath,
			String strStairsAxisPath,
			String strElevatorAxisPath,
			String strOtherRoomAxisPath ) throws IOException
	{
		// 描画ファイルの読み込みを行います。
		vReadERDepartment( strEmergencyDepartmentAxisPath );
		vReadERDepartmentEachRoom( strConsultationRoomAxisPath, 1 );
		vReadERDepartmentEachRoom( strOperationRoomAxisPath, 2 );
		vReadERDepartmentEachRoom( strEmergencyRoomAxisPath, 3 );
		vReadERDepartmentEachRoom( strObservationRoomAxisPath, 4 );
		vReadERDepartmentEachRoom( strSevereInjuryObservationRoomAxisPath, 5 );
		vReadERDepartmentEachRoom( strIntensiveCareUnitAxisPath, 6 );
		vReadERDepartmentEachRoom( strHighCareUnitAxisPath, 7 );
		vReadERDepartmentEachRoom( strGeneralWardAxisPath, 8 );
		vReadERDepartmentEachRoom( strWaitingRoomAxisPath, 9 );
		vReadERDepartmentEachRoom( strXRayRoomAxisPath, 10 );
		vReadERDepartmentEachRoom( strCTRoomAxisPath, 11 );
		vReadERDepartmentEachRoom( strMRIRoomAxisPath, 12 );
		vReadERDepartmentEachRoom( strAngiographyRoomAxisPath, 13 );
		vReadERDepartmentEachRoom( strFastRoomAxisPath, 14 );
		vReadERDepartmentEachRoom( strStairsAxisPath, 15 );
		vReadERDepartmentEachRoom( strElevatorAxisPath, 16 );
//		vReadERDepartmentEachRoom( strOtherRoomAxisPath, 0 );
	}


	/**
	 * <PRE>
	 *    終了処理を実行します。
	 * </PRE>
	 * @throws IOException	java標準例外クラス
	 */
	public void vTerminate() throws IOException
	{
		int i;

		synchronized( csErDepartmentCriticalSection )
		{
			// 各部屋エージェントの終了処理を実行します。
			if( ArrayListConsultationRooms != null )
			{
				for( i = ArrayListConsultationRooms.size()-1; i >= 0; i-- )
				{
					ArrayListConsultationRooms.get(i).vTerminate();
					ArrayListConsultationRooms.set(i, null);
					ArrayListConsultationRooms.remove(i);
				}
				ArrayListConsultationRooms = null;
			}
			if( ArrayListOperationRooms != null )
			{
				for( i = ArrayListOperationRooms.size()-1; i >= 0; i-- )
				{
					ArrayListOperationRooms.get(i).vTerminate();
					ArrayListOperationRooms.set(i, null);
					ArrayListOperationRooms.remove(i);
				}
				ArrayListOperationRooms = null;
			}
			if( ArrayListEmergencyRooms != null )
			{
				for( i = ArrayListEmergencyRooms.size()-1; i >= 0; i-- )
				{
					ArrayListEmergencyRooms.get(i).vTerminate();
					ArrayListEmergencyRooms.set(i, null);
					ArrayListEmergencyRooms.remove(i);
				}
				ArrayListEmergencyRooms = null;
			}
			if( ArrayListObservationRooms != null )
			{
				for( i = ArrayListObservationRooms.size()-1; i >= 0; i-- )
				{
					ArrayListObservationRooms.get(i).vTerminate();
					ArrayListObservationRooms.set(i, null);
					ArrayListObservationRooms.remove(i);
				}
				ArrayListObservationRooms = null;
			}
			if( ArrayListSevereInjuryObservationRooms != null )
			{
				for( i = ArrayListSevereInjuryObservationRooms.size()-1; i >= 0; i-- )
				{
					ArrayListSevereInjuryObservationRooms.get(i).vTerminate();
					ArrayListSevereInjuryObservationRooms.set(i, null);
					ArrayListSevereInjuryObservationRooms.remove(i);
				}
				ArrayListSevereInjuryObservationRooms = null;
			}
			if( ArrayListIntensiveCareUnitRooms != null )
			{
				for( i = ArrayListIntensiveCareUnitRooms.size()-1; i >= 0; i-- )
				{
					ArrayListIntensiveCareUnitRooms.get(i).vTerminate();
					ArrayListIntensiveCareUnitRooms.set(i, null);
					ArrayListIntensiveCareUnitRooms.remove(i);
				}
				ArrayListIntensiveCareUnitRooms = null;
			}
			if( ArrayListHighCareUnitRooms != null )
			{
				for( i = ArrayListHighCareUnitRooms.size()-1; i >= 0; i-- )
				{
					ArrayListHighCareUnitRooms.get(i).vTerminate();
					ArrayListHighCareUnitRooms.set(i, null);
					ArrayListHighCareUnitRooms.remove(i);
				}
				ArrayListHighCareUnitRooms = null;
			}
			if( ArrayListGeneralWardRooms != null )
			{
				for( i = ArrayListGeneralWardRooms.size()-1; i >= 0; i-- )
				{
					ArrayListGeneralWardRooms.get(i).vTerminate();
					ArrayListGeneralWardRooms.set(i, null);
					ArrayListGeneralWardRooms.remove(i);
				}
				ArrayListGeneralWardRooms = null;
			}
			if( erWaitingRoom != null )
			{
				erWaitingRoom.vTerminate();
				erWaitingRoom = null;
			}
			if( ArrayListExaminationXRayRooms != null )
			{
				for( i = ArrayListExaminationXRayRooms.size()-1; i >= 0; i-- )
				{
					ArrayListExaminationXRayRooms.get(i).vTerminate();
					ArrayListExaminationXRayRooms.set(i, null);
					ArrayListExaminationXRayRooms.remove(i);
				}
				ArrayListExaminationXRayRooms = null;
			}
			if( ArrayListExaminationCTRooms != null )
			{
				for( i = ArrayListExaminationCTRooms.size()-1; i >= 0; i-- )
				{
					ArrayListExaminationCTRooms.get(i).vTerminate();
					ArrayListExaminationCTRooms.set(i, null);
					ArrayListExaminationCTRooms.remove(i);
				}
				ArrayListExaminationCTRooms = null;
			}
			if( ArrayListExaminationMRIRooms != null )
			{
				for( i = ArrayListExaminationMRIRooms.size()-1; i >= 0; i-- )
				{
					ArrayListExaminationMRIRooms.get(i).vTerminate();
					ArrayListExaminationMRIRooms.set(i, null);
					ArrayListExaminationMRIRooms.remove(i);
				}
				ArrayListExaminationMRIRooms = null;
			}
			if( ArrayListExaminationAngiographyRooms != null )
			{
				for( i = ArrayListExaminationAngiographyRooms.size()-1; i >= 0; i-- )
				{
					ArrayListExaminationAngiographyRooms.get(i).vTerminate();
					ArrayListExaminationAngiographyRooms.set(i, null);
					ArrayListExaminationAngiographyRooms.remove(i);
				}
				ArrayListExaminationAngiographyRooms = null;
			}
			if( ArrayListExaminationFastRooms != null )
			{
				for( i = ArrayListExaminationFastRooms.size()-1; i >= 0; i-- )
				{
					ArrayListExaminationFastRooms.get(i).vTerminate();
					ArrayListExaminationFastRooms.set(i, null);
					ArrayListExaminationFastRooms.remove(i);
				}
				ArrayListExaminationFastRooms = null;
			}
			// 各部屋エージェントの終了処理を実行します。
			if( ArrayListDoctorAgentIds != null )
			{
				for( i = ArrayListDoctorAgentIds.size()-1; i >= 0; i-- )
				{
					ArrayListDoctorAgentIds.set(i, null);
					ArrayListDoctorAgentIds.remove(i);
				}
				ArrayListDoctorAgentIds = null;
			}
			if( ArrayListNurseAgentIds != null )
			{
				for( i = ArrayListNurseAgentIds.size()-1; i >= 0; i-- )
				{
					ArrayListNurseAgentIds.set(i, null);
					ArrayListNurseAgentIds.remove(i);
				}
				ArrayListNurseAgentIds = null;
			}
			if( ArrayListClinicalEngineerAgentIds != null )
			{
				for( i = ArrayListClinicalEngineerAgentIds.size()-1; i >= 0; i-- )
				{
					ArrayListClinicalEngineerAgentIds.set(i, null);
					ArrayListClinicalEngineerAgentIds.remove(i);
				}
				ArrayListClinicalEngineerAgentIds = null;
			}

			erEngine = null;
			erEnvironment = null;

			lfTotalTime = 0.0;
			iCurrentStatusFlag = 0;
		}
	}

	/**
	 * <PRE>
	 *    初期設定ファイル用インスタンスを設定します。
	 * </PRE>
	 * @param initparam 初期設定パラメータインスタンス
	 */
	public void vSetAllInitParam( InitSimParam initparam )
	{
		int i,j;
		// 診察室
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListConsultationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListConsultationRooms.get(i).cGetNurseAgent(j).vSetInitParam( initparam );
			}
			ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetInitParam( initparam );
		}
		// 初療室
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetNurseAgent(j).vSetInitParam( initparam );
			}
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetDoctorAgent(j).vSetInitParam( initparam );
			}
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgent(j).vSetInitParam( initparam );
			}
		}
		// 観察室
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListObservationRooms.get(i).erGetNurseAgent(j).vSetInitParam( initparam );
			}
		}
		// 重症観察室
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListSevereInjuryObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListSevereInjuryObservationRooms.get(i).erGetNurseAgent(j).vSetInitParam( initparam );
			}
		}
		// 手術室
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListOperationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListOperationRooms.get(i).cGetNurseAgent(j).vSetInitParam( initparam );
			}
			for( j = 0; j < ArrayListOperationRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListOperationRooms.get(i).cGetDoctorAgent(j).vSetInitParam( initparam );
			}
		}
		// 高度治療室
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListHighCareUnitRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListHighCareUnitRooms.get(i).cGetNurseAgent(j).vSetInitParam( initparam );
			}
			for( j = 0; j < ArrayListHighCareUnitRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListHighCareUnitRooms.get(i).cGetDoctorAgent(j).vSetInitParam( initparam );
			}
		}
		// 集中治療室
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListIntensiveCareUnitRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListIntensiveCareUnitRooms.get(i).cGetNurseAgent(j).vSetInitParam( initparam );
			}
			for( j = 0; j < ArrayListIntensiveCareUnitRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListIntensiveCareUnitRooms.get(i).cGetDoctorAgent(j).vSetInitParam( initparam );
			}
		}
		// 一般病棟
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListGeneralWardRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListGeneralWardRooms.get(i).cGetNurseAgent(j).vSetInitParam( initparam );
			}
			for( j = 0;j < ArrayListGeneralWardRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListGeneralWardRooms.get(i).cGetDoctorAgent(j).vSetInitParam( initparam );
			}
		}
		// 待合室
		for( i = 0;i < erWaitingRoom.iGetNurseAgentsNum(); i++ )
		{
			erWaitingRoom.erGetNurseAgent(i).vSetInitParam( initparam );
		}
		for( i = 0;i < erWaitingRoom.erGetPatientAgents().size(); i++ )
		{
			erWaitingRoom.erGetPatientAgent(i).vSetInitParam( initparam );
		}
		// 検査室(X線室)
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationXRayRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationXRayRooms.get(i).cGetClinicalEngineerAgent(j).vSetInitParam( initparam );
			}
		}
		// 検査室(CT室)
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationCTRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationCTRooms.get(i).cGetClinicalEngineerAgent(j).vSetInitParam( initparam );
			}
		}
		// 検査室(MRI室)
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationMRIRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationMRIRooms.get(i).cGetClinicalEngineerAgent(j).vSetInitParam( initparam );
			}
		}
		// 検査室(血管造影室)
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationAngiographyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationAngiographyRooms.get(i).cGetClinicalEngineerAgent(j).vSetInitParam( initparam );
			}
		}
		// 検査室(FAST室)
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationFastRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationFastRooms.get(i).cGetClinicalEngineerAgent(j).vSetInitParam( initparam );
			}
		}
//		// 病室外
//		for( i =0;i < erOutside.erGetPatientAgents().size(); i++ )
//		{
//			erOutside.erGetPatientAgent(i).vSetInitParam(initparam);
//		}
	}

	/**
	 * <PRE>
	 *    救急部門のログインスタンスを登録します。
	 * </PRE>
	 * @param log	ロガークラスインスタンス
	 */
	public void vSetLog( Logger log )
	{
		// TODO 自動生成されたメソッド・スタブ
		ERDepartmentLog = log;
	}

	/**
	 * <PRE>
	 *    ログ出力を設定します。
	 * </PRE>
	 * @param log ログ出力用インスタンス
	 */
	public void vSetAllLog( Logger log )
	{
		int i,j;
		ERDepartmentLog = log;
		// 診察室
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListConsultationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListConsultationRooms.get(i).cGetNurseAgent(j).vSetLog( log );
			}
			ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetLog( log );
			ArrayListConsultationRooms.get(i).vSetLog( log );
		}
		// 初療室
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetNurseAgent(j).vSetLog( log );
			}
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetDoctorAgent(j).vSetLog( log );
			}
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgent(j).vSetLog( log );
			}
			ArrayListEmergencyRooms.get(i).vSetLog( log );
		}
		// 観察室
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListObservationRooms.get(i).erGetNurseAgent(j).vSetLog( log );
			}
			ArrayListObservationRooms.get(i).vSetLog( log );
		}
		// 重症観察室
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListSevereInjuryObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListSevereInjuryObservationRooms.get(i).erGetNurseAgent(j).vSetLog( log );
			}
			ArrayListSevereInjuryObservationRooms.get(i).vSetLog( log );
		}
		// 手術室
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListOperationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListOperationRooms.get(i).cGetNurseAgent(j).vSetLog( log );
			}
			for( j = 0; j < ArrayListOperationRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListOperationRooms.get(i).cGetDoctorAgent(j).vSetLog( log );
			}
			ArrayListOperationRooms.get(i).vSetLog( log );
		}
		// 高度治療室
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListHighCareUnitRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListHighCareUnitRooms.get(i).cGetNurseAgent(j).vSetLog( log );
			}
			for( j = 0; j < ArrayListHighCareUnitRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListHighCareUnitRooms.get(i).cGetDoctorAgent(j).vSetLog( log );
			}
			ArrayListHighCareUnitRooms.get(i).vSetLog( log );
		}
		// 集中治療室
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListIntensiveCareUnitRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListIntensiveCareUnitRooms.get(i).cGetNurseAgent(j).vSetLog( log );
			}
			for( j = 0; j < ArrayListIntensiveCareUnitRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListIntensiveCareUnitRooms.get(i).cGetDoctorAgent(j).vSetLog( log );
			}
			ArrayListIntensiveCareUnitRooms.get(i).vSetLog( log );
		}
		// 一般病棟
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListGeneralWardRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListGeneralWardRooms.get(i).cGetNurseAgent(j).vSetLog( log );
			}
			for( j = 0;j < ArrayListGeneralWardRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListGeneralWardRooms.get(i).cGetDoctorAgent(j).vSetLog( log );
			}
			ArrayListGeneralWardRooms.get(i).vSetLog( log );
		}
		// 待合室
		for( i = 0;i < erWaitingRoom.iGetNurseAgentsNum(); i++ )
		{
			erWaitingRoom.erGetNurseAgent(i).vSetLog( log );
		}
		for( i = 0;i < erWaitingRoom.erGetPatientAgents().size(); i++ )
		{
			erWaitingRoom.erGetPatientAgent(i).vSetLog( log );
		}
		erWaitingRoom.vSetLog( log );
		// 検査室(X線室)
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationXRayRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationXRayRooms.get(i).cGetClinicalEngineerAgent(j).vSetLog( log );
			}
			ArrayListExaminationXRayRooms.get(i).vSetLog( log );
		}
		// 検査室(CT室)
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationCTRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationCTRooms.get(i).cGetClinicalEngineerAgent(j).vSetLog( log );
			}
			ArrayListExaminationCTRooms.get(i).vSetLog( log );
		}
		// 検査室(MRI室)
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationMRIRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationMRIRooms.get(i).cGetClinicalEngineerAgent(j).vSetLog( log );
			}
			ArrayListExaminationMRIRooms.get(i).vSetLog( log );
		}
		// 検査室(血管造影室)
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationAngiographyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationAngiographyRooms.get(i).cGetClinicalEngineerAgent(j).vSetLog( log );
			}
			ArrayListExaminationAngiographyRooms.get(i).vSetLog( log );
		}
		// 検査室(FAST室)
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationFastRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationFastRooms.get(i).cGetClinicalEngineerAgent(j).vSetLog( log );
			}
			ArrayListExaminationFastRooms.get(i).vSetLog( log );
		}
		// 病院外
		erOutside.vSetLog( log );
		for( i = 0;i < erOutside.erGetPatientAgents().size(); i++ )
		{
			erOutside.erGetPatientAgent(i).vSetLog( log );
		}
	}

	/**
	 * <PRE>
	 *    クリティカルセクションオブジェクトを設定します。
	 * </PRE>
	 * @param cs クリティカルセクション
	 */
	public void vSetCriticalSection( Object cs )
	{
		int i,j;

		ERDepartmentLog.warning( "クラス名" + "," + this.getClass() + "," + "メソッド名" + "," + "vSetCriticalSection" + "," + "クリティカルセクションのアドレス" + "," + cs + "," );
		ERDepartmentLog.warning( "クラス名" + "," + this.getClass() + "," + "メソッド名" + "," + "vSetCriticalSection" + "," + "erFinisher" + "," + erFinisher + "," );
		csErDepartmentCriticalSection = cs;
		erFinisher.vSetCriticalSection( csErDepartmentCriticalSection );
		// 診察室
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListConsultationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListConsultationRooms.get(i).cGetNurseAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetCriticalSection( csErDepartmentCriticalSection );
			ArrayListConsultationRooms.get(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		// 初療室
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetNurseAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetDoctorAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			ArrayListEmergencyRooms.get(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		// 観察室
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListObservationRooms.get(i).erGetNurseAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			ArrayListObservationRooms.get(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		// 重症観察室
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListSevereInjuryObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListSevereInjuryObservationRooms.get(i).erGetNurseAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			ArrayListSevereInjuryObservationRooms.get(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		// 手術室
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListOperationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListOperationRooms.get(i).cGetNurseAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			for( j = 0; j < ArrayListOperationRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListOperationRooms.get(i).cGetDoctorAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			ArrayListOperationRooms.get(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		// 高度治療室
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListHighCareUnitRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListHighCareUnitRooms.get(i).cGetNurseAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			for( j = 0; j < ArrayListHighCareUnitRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListHighCareUnitRooms.get(i).cGetDoctorAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			ArrayListHighCareUnitRooms.get(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		// 集中治療室
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListIntensiveCareUnitRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListIntensiveCareUnitRooms.get(i).cGetNurseAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			for( j = 0; j < ArrayListIntensiveCareUnitRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListIntensiveCareUnitRooms.get(i).cGetDoctorAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			ArrayListIntensiveCareUnitRooms.get(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		// 一般病棟
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListGeneralWardRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListGeneralWardRooms.get(i).cGetNurseAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			for( j = 0;j < ArrayListGeneralWardRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListGeneralWardRooms.get(i).cGetDoctorAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			ArrayListGeneralWardRooms.get(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		// 待合室
		erWaitingRoom.vSetCriticalSection( csErDepartmentCriticalSection );
		for( i = 0;i < erWaitingRoom.iGetNurseAgentsNum(); i++ )
		{
			erWaitingRoom.erGetNurseAgent(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		for( j = 0;j < erWaitingRoom.erGetPatientAgents().size(); j++ )
		{
			erWaitingRoom.erGetPatientAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		// 検査室(X線室)
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationXRayRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationXRayRooms.get(i).cGetClinicalEngineerAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			ArrayListExaminationXRayRooms.get(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		// 検査室(CT室)
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationCTRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationCTRooms.get(i).cGetClinicalEngineerAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			ArrayListExaminationCTRooms.get(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		// 検査室(MRI室)
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationMRIRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationMRIRooms.get(i).cGetClinicalEngineerAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			ArrayListExaminationMRIRooms.get(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		// 検査室(血管造影室)
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationAngiographyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationAngiographyRooms.get(i).cGetClinicalEngineerAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			ArrayListExaminationAngiographyRooms.get(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		// 検査室(FAST室)
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationFastRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationFastRooms.get(i).cGetClinicalEngineerAgent(j).vSetCriticalSection( csErDepartmentCriticalSection );
			}
			ArrayListExaminationFastRooms.get(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
		// 病院外
		erOutside.vSetCriticalSection( csErDepartmentCriticalSection );
		for( i = 0;i < erOutside.erGetPatientAgents().size(); i++ )
		{
			erOutside.erGetPatientAgent(i).vSetCriticalSection( csErDepartmentCriticalSection );
		}
	}

	/**
	 * <PRE>
	 *    メルセンヌツイスターオブジェクトを設定します。
	 * </PRE>
	 * @param rnd メルセンヌツイスターのインスタンス
	 */
	public void vSetRandom( Rand rnd )
	{
		int i,j;
		long seed;
		seed = System.currentTimeMillis();

		sfmtErDepartmentRandom = new Rand((int)seed);

//		erFinisher.vSetRandom( csErDepartmentCriticalSection );
		// 診察室
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListConsultationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListConsultationRooms.get(i).cGetNurseAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetRandom( sfmtErDepartmentRandom );
			ArrayListConsultationRooms.get(i).vSetConsultationRoomRandom( sfmtErDepartmentRandom );
		}
		// 初療室
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetNurseAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetDoctorAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgent(j).vSetRandom( sfmtErDepartmentRandom  );
			}
			ArrayListEmergencyRooms.get(i).vSetRandom( sfmtErDepartmentRandom );
		}
		// 観察室
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListObservationRooms.get(i).erGetNurseAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			ArrayListObservationRooms.get(i).vSetRandom( sfmtErDepartmentRandom );
		}
		// 重症観察室
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListSevereInjuryObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListSevereInjuryObservationRooms.get(i).erGetNurseAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			ArrayListSevereInjuryObservationRooms.get(i).vSetRandom( sfmtErDepartmentRandom );
		}
		// 手術室
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListOperationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListOperationRooms.get(i).cGetNurseAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			for( j = 0; j < ArrayListOperationRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListOperationRooms.get(i).cGetDoctorAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			ArrayListOperationRooms.get(i).vSetRandom( sfmtErDepartmentRandom );
		}
		// 高度治療室
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListHighCareUnitRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListHighCareUnitRooms.get(i).cGetNurseAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			for( j = 0; j < ArrayListHighCareUnitRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListHighCareUnitRooms.get(i).cGetDoctorAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			ArrayListHighCareUnitRooms.get(i).vSetRandom( sfmtErDepartmentRandom );
		}
		// 集中治療室
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListIntensiveCareUnitRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListIntensiveCareUnitRooms.get(i).cGetNurseAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			for( j = 0; j < ArrayListIntensiveCareUnitRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListIntensiveCareUnitRooms.get(i).cGetDoctorAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			ArrayListIntensiveCareUnitRooms.get(i).vSetRandom( sfmtErDepartmentRandom );
		}
		// 一般病棟
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			for( j = 0; j < ArrayListGeneralWardRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListGeneralWardRooms.get(i).cGetNurseAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			for( j = 0;j < ArrayListGeneralWardRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListGeneralWardRooms.get(i).cGetDoctorAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			ArrayListGeneralWardRooms.get(i).vSetRandom( sfmtErDepartmentRandom );
		}
		// 待合室
		for( i = 0;i < erWaitingRoom.iGetNurseAgentsNum(); i++ )
		{
			erWaitingRoom.erGetNurseAgent(i).vSetRandom( sfmtErDepartmentRandom );
		}
		for( j = 0;j < erWaitingRoom.erGetPatientAgents().size(); j++ )
		{
			erWaitingRoom.erGetPatientAgent(j).vSetRandom( sfmtErDepartmentRandom );
		}
		erWaitingRoom.vSetRandom( sfmtErDepartmentRandom );
		// 検査室(X線室)
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationXRayRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationXRayRooms.get(i).cGetClinicalEngineerAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			ArrayListExaminationXRayRooms.get(i).vSetRandom( sfmtErDepartmentRandom );
		}
		// 検査室(CT室)
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationCTRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationCTRooms.get(i).cGetClinicalEngineerAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			ArrayListExaminationCTRooms.get(i).vSetRandom( sfmtErDepartmentRandom );
		}
		// 検査室(MRI室)
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationMRIRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationMRIRooms.get(i).cGetClinicalEngineerAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			ArrayListExaminationMRIRooms.get(i).vSetRandom( sfmtErDepartmentRandom );
		}
		// 検査室(血管造影室)
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationAngiographyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationAngiographyRooms.get(i).cGetClinicalEngineerAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			ArrayListExaminationAngiographyRooms.get(i).vSetRandom( sfmtErDepartmentRandom );
		}
		// 検査室(FAST室)
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListExaminationFastRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationFastRooms.get(i).cGetClinicalEngineerAgent(j).vSetRandom( sfmtErDepartmentRandom );
			}
			ArrayListExaminationFastRooms.get(i).vSetRandom( sfmtErDepartmentRandom );
		}
		// 病院外
		erOutside.vSetRandom( sfmtErDepartmentRandom );
	}

	/**
	 * <PRE>
	 *    逆シミュレーションモードの設定をします。
	 * </PRE>
	 * @param iInverseSimMode 逆シミュレーションモード
	 */
	public void vSetInverseSimMode( int iInverseSimMode )
	{
		int i,j;
		// 診察室
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			ArrayListConsultationRooms.get(i).vSetInverseSimMode( iInverseSimMode );
		}
		// 初療室
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			ArrayListEmergencyRooms.get(i).vSetInverseSimMode( iInverseSimMode );
		}
		// 観察室
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			ArrayListObservationRooms.get(i).vSetInverseSimMode( iInverseSimMode );
		}
		// 重症観察室
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			ArrayListSevereInjuryObservationRooms.get(i).vSetInverseSimMode( iInverseSimMode );
		}
		// 手術室
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			ArrayListOperationRooms.get(i).vSetInverseSimMode( iInverseSimMode );
		}
		// 高度治療室
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			ArrayListHighCareUnitRooms.get(i).vSetInverseSimMode( iInverseSimMode );
		}
		// 集中治療室
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			ArrayListIntensiveCareUnitRooms.get(i).vSetInverseSimMode( iInverseSimMode );
		}
		// 一般病棟
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			ArrayListGeneralWardRooms.get(i).vSetInverseSimMode( iInverseSimMode );
		}
		// 待合室
		for( i = 0;i < erWaitingRoom.iGetNurseAgentsNum(); i++ )
		{
			erWaitingRoom.vSetInverseSimMode( iInverseSimMode );
		}
		// 検査室(X線室)
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			ArrayListExaminationXRayRooms.get(i).vSetInverseSimMode( iInverseSimMode );
		}
		// 検査室(CT室)
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			ArrayListExaminationCTRooms.get(i).vSetInverseSimMode( iInverseSimMode );
		}
		// 検査室(MRI室)
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			ArrayListExaminationMRIRooms.get(i).vSetInverseSimMode( iInverseSimMode );
		}
		// 検査室(血管造影室)
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			ArrayListExaminationAngiographyRooms.get(i).vSetInverseSimMode( iInverseSimMode );
		}
		// 検査室(FAST室)
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			ArrayListExaminationFastRooms.get(i).vSetInverseSimMode( iInverseSimMode );
		}
		// 病院外(患者生成時に設定します。)
		erOutside.vSetPatientInverseSimMode( iInverseSimMode );
	}

	/**
	 * <PRE>
	 *    逆シミュレーションモードの設定をします。
	 * </PRE>
	 * @param strERDirectory					救急部門設定ファイルパス
	 * @param strDoctorAgentDirectory			医師設定ファイルパス
	 * @param strNurseAgentDirectory			看護師設定ファイルパス
	 * @param strClinicalEngineerAgentDirectory	医療技師設定ファイルパス
	 * @param iFileWriteMode					ファイル書き込みモード
	 * @throws IOException						ファイル処理中の例外
	 */
	public void vSetReadWriteFile( String strERDirectory, String strDoctorAgentDirectory, String strNurseAgentDirectory, String strClinicalEngineerAgentDirectory, int iFileWriteMode ) throws IOException
	{
		int i;

		// 救急部門
		vSetReadWriteFile( strERDirectory, iFileWriteMode );

		// 診察室
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			ArrayListConsultationRooms.get(i).vSetReadWriteFileForAgents( strDoctorAgentDirectory, strNurseAgentDirectory, strClinicalEngineerAgentDirectory, iFileWriteMode );
		}
		// 初療室
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			ArrayListEmergencyRooms.get(i).vSetReadWriteFileForAgents( strDoctorAgentDirectory, strNurseAgentDirectory, strClinicalEngineerAgentDirectory, iFileWriteMode );
		}
		// 観察室
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			ArrayListObservationRooms.get(i).vSetReadWriteFileForAgents( strDoctorAgentDirectory, strNurseAgentDirectory, strClinicalEngineerAgentDirectory, iFileWriteMode );
		}
		// 重症観察室
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			ArrayListSevereInjuryObservationRooms.get(i).vSetReadWriteFileForAgents( strDoctorAgentDirectory, strNurseAgentDirectory, strClinicalEngineerAgentDirectory, iFileWriteMode );
		}
		// 手術室
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			ArrayListOperationRooms.get(i).vSetReadWriteFileForAgents( strDoctorAgentDirectory, strNurseAgentDirectory, strClinicalEngineerAgentDirectory, iFileWriteMode );
		}
		// 高度治療室
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			ArrayListHighCareUnitRooms.get(i).vSetReadWriteFileForAgents( strDoctorAgentDirectory, strNurseAgentDirectory, strClinicalEngineerAgentDirectory, iFileWriteMode );
		}
		// 集中治療室
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			ArrayListIntensiveCareUnitRooms.get(i).vSetReadWriteFileForAgents( strDoctorAgentDirectory, strNurseAgentDirectory, strClinicalEngineerAgentDirectory, iFileWriteMode );
		}
		// 一般病棟
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			ArrayListGeneralWardRooms.get(i).vSetReadWriteFileForAgents( strDoctorAgentDirectory, strNurseAgentDirectory, strClinicalEngineerAgentDirectory, iFileWriteMode );
		}
		// 待合室
		for( i = 0;i < erWaitingRoom.iGetNurseAgentsNum(); i++ )
		{
			erWaitingRoom.vSetReadWriteFileForAgents( strDoctorAgentDirectory, strNurseAgentDirectory, strClinicalEngineerAgentDirectory, iFileWriteMode );
		}
		// 検査室(X線室)
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			ArrayListExaminationXRayRooms.get(i).vSetReadWriteFileForAgents( strDoctorAgentDirectory, strNurseAgentDirectory, strClinicalEngineerAgentDirectory, iFileWriteMode );
		}
		// 検査室(CT室)
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			ArrayListExaminationCTRooms.get(i).vSetReadWriteFileForAgents( strDoctorAgentDirectory, strNurseAgentDirectory, strClinicalEngineerAgentDirectory, iFileWriteMode );
		}
		// 検査室(MRI室)
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			ArrayListExaminationMRIRooms.get(i).vSetReadWriteFileForAgents( strDoctorAgentDirectory, strNurseAgentDirectory, strClinicalEngineerAgentDirectory, iFileWriteMode );
		}
		// 検査室(血管造影室)
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			ArrayListExaminationAngiographyRooms.get(i).vSetReadWriteFileForAgents( strDoctorAgentDirectory, strNurseAgentDirectory, strClinicalEngineerAgentDirectory, iFileWriteMode );
		}
		// 検査室(FAST室)
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			ArrayListExaminationFastRooms.get(i).vSetReadWriteFileForAgents( strDoctorAgentDirectory, strNurseAgentDirectory, strClinicalEngineerAgentDirectory, iFileWriteMode );
		}
	}

	/**
	 * <PRE>
	 *    FUSEのエンジンにエージェントを登録します。
	 * </PRE>
	 * @param engine FUSEエンジン
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	public void vSetSimulationEngine( SimulationEngine engine )
	{
		engine.addAgent( this );
	}

	/**
	 * <PRE>
	 *    部屋数を設定します。
	 * </PRE>
	 * @param strPath			部屋の数を記述したファイル名
	 * @throws IOException		java 標準のIOクラス例外
	 */
	public void vSetEmergencyDepartment( String strPath ) throws IOException
	{
		int iRow = 0;
		int iColumn = 0;
		int ppiParameter[][];

		CCsv csv = new CCsv();
		if( strPath == "" )
		{
			csv.vOpen( "./parameter/ER.csv", "read" );
			csv.vGetRowColumn();
		}
		else
		{
			csv.vOpen( strPath, "read" );
			csv.vGetRowColumn();
		}
		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		ppiParameter = new int[iColumn][iRow];
		csv.vRead( ppiParameter );

		iConsultationRoomNum 			= ppiParameter[0][0];
		iOperationRoomNum 				= ppiParameter[1][0];
		iEmergencyRoomNum 				= ppiParameter[2][0];
		iObservationRoomNum 			= ppiParameter[3][0];
		iSevereInjuryObservationRoomNum	= ppiParameter[4][0];
		iIntensiveCareUnitRoomNum 		= ppiParameter[5][0];
		iHighCareUnitRoomNum 			= ppiParameter[6][0];
		iGeneralWardRoomNum				= ppiParameter[7][0];
		iWaitingRoomNum 				= ppiParameter[8][0];
		iXRayRoomNum		 			= ppiParameter[9][0];
		iCTRoomNum 						= ppiParameter[10][0];
		iMRIRoomNum 					= ppiParameter[11][0];
		iAngiographyRoomNum 			= ppiParameter[12][0];
		iFastRoomNum		 			= ppiParameter[13][0];
	}

	/**
	 * <PRE>
	 *    部屋数を設定します。
	 * </PRE>
	 * @param iRandomMode				ランダムモード（未使用）
	 */
	public void vSetRandomEmergencyDepartment( InitInverseSimParam initparam )
	{
		// 固定値にします。
		if( initparam.iGetInitializeGenerateMode() == 0 )
		{
			iConsultationRoomNum			= initparam.iGetConsultationRoomNum();
			iOperationRoomNum				= initparam.iGetOperationRoomNum();
			iEmergencyRoomNum				= initparam.iGetEmergencyRoomNum();
			iObservationRoomNum				= initparam.iGetObservationRoomNum();
			iSevereInjuryObservationRoomNum = initparam.iGetSevereInjuryObservationRoomNum();
			iIntensiveCareUnitRoomNum		= initparam.iGetIntensiveCareUnitNum();
			iHighCareUnitRoomNum			= initparam.iGetHighCareUnitNum();
			iGeneralWardRoomNum				= initparam.iGetGeneralWardNum();
			iXRayRoomNum					= initparam.iGetXRayRoomNum();
			iCTRoomNum						= initparam.iGetCTRoomNum();
			iMRIRoomNum						= initparam.iGetMRIRoomNum();
			iAngiographyRoomNum				= initparam.iGetAngiographyRoomNum();
			iFastRoomNum					= initparam.iGetFastRoomNum();

			// 聖隷浜松病院用
//			iConsultationRoomNum 			= 12;
//			iOperationRoomNum 				= 15;
//			iEmergencyRoomNum 				= 8;
//			iObservationRoomNum 			= 0;
//			iSevereInjuryObservationRoomNum = 0;
//			iHighCareUnitRoomNum			= 1;
//			iIntensiveCareUnitRoomNum 		= 1;
//			iGeneralWardRoomNum 			= 1;
//			iWaitingRoomNum 				= 1;
//			iXRayRoomNum		 			= 5;
//			iCTRoomNum 						= 4;
//			iMRIRoomNum 					= 5;
//			iAngiographyRoomNum 			= 2;
//			iFastRoomNum		 			= 1;
		}
		// 一様乱数により発生させます。
		else if( initparam.iGetInitializeGenerateMode() == 1 )
		{
			// パラメータの設定をします。
			iConsultationRoomNum			= rnd.NextInt( initparam.iGetConsultationRoomNum()+1 );
			iOperationRoomNum				= rnd.NextInt( initparam.iGetOperationRoomNum()+1 );
			iEmergencyRoomNum				= rnd.NextInt( initparam.iGetEmergencyRoomNum()+1 );
			iObservationRoomNum				= rnd.NextInt( initparam.iGetObservationRoomNum()+1 );
			iSevereInjuryObservationRoomNum = rnd.NextInt( initparam.iGetSevereInjuryObservationRoomNum()+1 );
			iIntensiveCareUnitRoomNum		= rnd.NextInt( initparam.iGetIntensiveCareUnitNum()+1 );
			iHighCareUnitRoomNum			= rnd.NextInt( initparam.iGetHighCareUnitNum()+1 );
			iGeneralWardRoomNum				= rnd.NextInt( initparam.iGetGeneralWardNum()+1 );
			iXRayRoomNum					= rnd.NextInt( initparam.iGetXRayRoomNum()+1 );
			iCTRoomNum						= rnd.NextInt( initparam.iGetCTRoomNum()+1 );
			iMRIRoomNum						= rnd.NextInt( initparam.iGetMRIRoomNum()+1 );
			iAngiographyRoomNum				= rnd.NextInt( initparam.iGetAngiographyRoomNum()+1 );
			iFastRoomNum					= rnd.NextInt( initparam.iGetFastRoomNum()+1 );
		}
		// 各部屋数を中心としてそこの近傍の範囲内で一様乱数により発生させます。
		else if( initparam.iGetInitializeGenerateMode() == 2 )
		{
			double lfRes = 0.0;
			// パラメータの設定をします。
			lfRes = initparam.lfGetConsultationRoomDoctorNumMaxWeight()-initparam.lfGetConsultationRoomDoctorNumMinWeight();
			iConsultationRoomNum			= (int)(initparam.iGetConsultationRoomNum()*lfRes*rnd.NextUnif()+initparam.iGetConsultationRoomNum()*initparam.lfGetConsultationRoomDoctorNumMinWeight() );
			lfRes = initparam.lfGetOperationRoomDoctorNumMaxWeight()-initparam.lfGetOperationRoomDoctorNumMinWeight();
			iOperationRoomNum				= (int)(initparam.iGetOperationRoomNum()*lfRes*rnd.NextUnif()+initparam.iGetOperationRoomNum()*initparam.lfGetOperationRoomDoctorNumMinWeight());
			lfRes = initparam.lfGetOperationRoomDoctorNumMaxWeight()-initparam.lfGetOperationRoomDoctorNumMinWeight();
			iEmergencyRoomNum				= (int)(initparam.iGetEmergencyRoomNum()*lfRes*rnd.NextUnif()+initparam.iGetEmergencyRoomNum()*initparam.lfGetEmergencyRoomDoctorNumMinWeight());
			lfRes = initparam.lfGetObservationRoomNurseNumMaxWeight()-initparam.lfGetObservationRoomNurseNumMinWeight();
			iObservationRoomNum				= (int)(initparam.iGetObservationRoomNum()*lfRes*rnd.NextUnif()+initparam.iGetObservationRoomNum()*initparam.lfGetOperationRoomNurseNumMinWeight());
			lfRes = initparam.lfGetSevereInjuryObservationRoomNurseNumMaxWeight()-initparam.lfGetSevereInjuryObservationRoomNurseNumMinWeight();
			iSevereInjuryObservationRoomNum = (int)(initparam.iGetSevereInjuryObservationRoomNum()*lfRes*rnd.NextUnif()+initparam.iGetSevereInjuryObservationRoomNum()*initparam.lfGetSevereInjuryObservationRoomNurseNumMinWeight());
			lfRes = initparam.lfGetIntensiveCareUnitDoctorNumMaxWeight()-initparam.lfGetIntensiveCareUnitDoctorNumMinWeight();
			iIntensiveCareUnitRoomNum		= (int)(initparam.iGetIntensiveCareUnitNum()*lfRes*rnd.NextUnif()+initparam.iGetIntensiveCareUnitNum()*initparam.lfGetIntensiveCareUnitDoctorNumMaxWeight());
			lfRes = initparam.lfGetHighCareUnitDoctorNumMaxWeight()-initparam.lfGetHighCareUnitDoctorNumMinWeight();
			iHighCareUnitRoomNum			= (int)(initparam.iGetHighCareUnitNum()*lfRes*rnd.NextUnif()+initparam.iGetHighCareUnitNum()*initparam.lfGetHighCareUnitDoctorNumMinWeight());
			lfRes = initparam.lfGetGeneralWardNurseNumMaxWeight()-initparam.lfGetGeneralWardNurseNumMinWeight();
			iGeneralWardRoomNum				= (int)(initparam.iGetGeneralWardNum()*lfRes*rnd.NextUnif()+initparam.iGetGeneralWardNum()*initparam.lfGetGeneralWardNurseNumMinWeight());
			iGeneralWardRoomNum				= iGeneralWardRoomNum < 1 ? 1 : iGeneralWardRoomNum;
			lfRes = initparam.lfGetXRayRoomClinicalEngineerNumMaxWeight()-initparam.lfGetXRayRoomClinicalEngineerNumMinWeight();
			iXRayRoomNum					= (int)(initparam.iGetXRayRoomNum()*lfRes*rnd.NextUnif()+initparam.iGetXRayRoomNum()*initparam.lfGetXRayRoomClinicalEngineerNumMinWeight());
			lfRes = initparam.lfGetCTRoomClinicalEngineerNumMaxWeight()-initparam.lfGetCTRoomClinicalEngineerNumMinWeight();
			iCTRoomNum						= (int)(initparam.iGetCTRoomNum()*lfRes*rnd.NextUnif()+initparam.iGetCTRoomNum()*initparam.lfGetCTRoomClinicalEngineerNumMinWeight());
			lfRes = initparam.lfGetMRIRoomClinicalEngineerNumMaxWeight()-initparam.lfGetMRIRoomClinicalEngineerNumMinWeight();
			iMRIRoomNum						= (int)(initparam.iGetMRIRoomNum()*lfRes*rnd.NextUnif()+initparam.iGetMRIRoomNum()*initparam.lfGetMRIRoomClinicalEngineerNumMinWeight());
			lfRes = initparam.lfGetAngiographyRoomClinicalEngineerNumMaxWeight()-initparam.lfGetAngiographyRoomClinicalEngineerNumMinWeight();
			iAngiographyRoomNum				= (int)(initparam.iGetAngiographyRoomNum()*lfRes*rnd.NextUnif()+initparam.iGetAngiographyRoomNum()*initparam.lfGetAngiographyRoomClinicalEngineerNumMinWeight());
			lfRes = initparam.lfGetFastRoomClinicalEngineerNumMaxWeight()-initparam.lfGetFastRoomClinicalEngineerNumMinWeight();
			iFastRoomNum					= (int)(initparam.iGetFastRoomNum()*lfRes*rnd.NextUnif()+initparam.iGetFastRoomNum()*initparam.lfGetFastRoomClinicalEngineerNumMinWeight());
			iWaitingRoomNum					= 1;
		}
	}

	/**
	 * <PRE>
	 *    部屋数を設定します。
	 * </PRE>
	 * @param iConsultationRoomNumData					診察室数
	 * @param iOperationRoomNumData						手術室数
	 * @param iEmergencyRoomNumData						初療室数
	 * @param iObservationRoomNumData					観察室数
	 * @param iSevereInjuryObservationRoomNumData		重症観察室数
	 * @param iIntensiveCareUnitRoomNumData				集中治療室数
	 * @param iHighCareUnitRoomNumData					高度治療室数
	 * @param iGeneralWardRoomNumData					一般病棟室数
	 * @param iWaitingRoomNumData						待合室数（１）
	 * @param iXRayRoomNumData							X線室数
	 * @param iCTRoomNumData							CT室数
	 * @param iMRIRoomNumData							MRI室数
	 * @param iAngiographyRoomNumData					血管造影室数
	 * @param iFastRoomNumData							FAST室数
	 */
	public void vSetEmergencyDepartment( int iConsultationRoomNumData,
										 int iOperationRoomNumData,
										 int iEmergencyRoomNumData,
										 int iObservationRoomNumData,
										 int iSevereInjuryObservationRoomNumData,
										 int iIntensiveCareUnitRoomNumData,
										 int iHighCareUnitRoomNumData,
										 int iGeneralWardRoomNumData,
										 int iWaitingRoomNumData,
										 int iXRayRoomNumData,
										 int iCTRoomNumData,
										 int iMRIRoomNumData,
										 int iAngiographyRoomNumData,
										 int iFastRoomNumData )
	{
		// パラメータの設定をします。
		iConsultationRoomNum 			= iConsultationRoomNumData;
		iOperationRoomNum 				= iOperationRoomNumData;
		iEmergencyRoomNum 				= iEmergencyRoomNumData;
		iObservationRoomNum 			= iObservationRoomNumData;
		iSevereInjuryObservationRoomNum = iSevereInjuryObservationRoomNumData;
		iIntensiveCareUnitRoomNum 		= iIntensiveCareUnitRoomNumData;
		iHighCareUnitRoomNum			= iHighCareUnitRoomNumData;
		iGeneralWardRoomNum 			= iGeneralWardRoomNumData;
		iWaitingRoomNum 				= 1;
		iXRayRoomNum		 			= iXRayRoomNumData;
		iCTRoomNum 						= iCTRoomNumData;
		iMRIRoomNum 					= iMRIRoomNumData;
		iAngiographyRoomNum 			= iAngiographyRoomNumData;
		iFastRoomNum		 			= iFastRoomNumData;
	}

	/**
	 * <PRE>
	 *    部屋を構成するエージェントの数を設定します。
	 * </PRE>
	 * @param iConsultationRoomDoctorNumData						診察室の医師数
	 * @param iConsultationRoomNurseNumData							診察室の看護師数
	 * @param iOperationRoomDoctorNumData							手術室の医師数
	 * @param iOperationRoomNurseNumData							手術室の看護師数
	 * @param iEmergencyRoomDoctorNumData							初療室の医師数
	 * @param iEmergencyRoomNurseNumData							初療室の看護師数
	 * @param iEmergencyRoomClinicalEngineerNumData					初療室の医療技師数
	 * @param iObservationRoomNurseNumData							観察室の看護師数
	 * @param iInjurySevereObservationRoomNurseNumData				重症観察室の看護師数
	 * @param iIntensiveCareUnitRoomDoctorNumData					集中治療室の医師数
	 * @param iIntensiveCareUnitRoomNurseNumData					集中治療室の看護師数
	 * @param iHighCareUnitRoomDoctorNumData						高度治療室の医師数
	 * @param iHighCareUnitRoomNurseNumData							高度治療室の看護師数
	 * @param iGeneralWardRoomDoctorNumData							一般病棟の医師数
	 * @param iGeneralWardRoomNurseNumData							一般病棟の看護師数
	 * @param iWaitingRoomNurseNumData								待合室の看護師数
	 * @param iExaminationXRayRoomClinicalEngineerNumData			X線室の医療技師数
	 * @param iExaminationCTRoomClinicalEngineerNumData				CT室の医療技師数
	 * @param iExaminationMRIRoomClinicalEngineerNumData			MRI室の医療技師数
	 * @param iExaminationAngiographyRoomClinicalEngineerNumData	血管造影室の医療技師数
	 * @param iExaminationFastRoomClinicalEngineerNumData			FAST室の医療技師数
	 */
	public void vSetEmergencyDepartmentAgents( 	int iConsultationRoomDoctorNumData,
												int iConsultationRoomNurseNumData,
												int iOperationRoomDoctorNumData,
												int iOperationRoomNurseNumData,
												int iEmergencyRoomDoctorNumData,
												int iEmergencyRoomNurseNumData,
												int iEmergencyRoomClinicalEngineerNumData,
												int iObservationRoomNurseNumData,
												int iInjurySevereObservationRoomNurseNumData,
												int iIntensiveCareUnitRoomDoctorNumData,
												int iIntensiveCareUnitRoomNurseNumData,
												int iHighCareUnitRoomDoctorNumData,
												int iHighCareUnitRoomNurseNumData,
												int iGeneralWardRoomDoctorNumData,
												int iGeneralWardRoomNurseNumData,
												int iWaitingRoomNurseNumData,
												int iExaminationXRayRoomClinicalEngineerNumData,
												int iExaminationCTRoomClinicalEngineerNumData,
												int iExaminationMRIRoomClinicalEngineerNumData,
												int iExaminationAngiographyRoomClinicalEngineerNumData,
												int iExaminationFastRoomClinicalEngineerNumData )
	{
		// パラメータの設定をします。
		iConsultationDoctorNum = iConsultationRoomDoctorNumData;
		iConsultationNurseNum = iConsultationRoomNurseNumData;
		iOperationDoctorNum = iOperationRoomDoctorNumData;
		iOperationNurseNum = iOperationRoomNurseNumData;
		iEmergencyDoctorNum = iEmergencyRoomDoctorNumData;
		iEmergencyNurseNum = iEmergencyRoomNurseNumData;
		iEmergencyClinicalEngineerNum = iEmergencyRoomClinicalEngineerNumData;
		iObservationNurseNum = iObservationRoomNurseNumData;
		iSevereInjuryObservationNurseNum = iInjurySevereObservationRoomNurseNumData;
		iIntensiveCareUnitDoctorNum = iIntensiveCareUnitRoomDoctorNumData;
		iIntensiveCareUnitNurseNum = iIntensiveCareUnitRoomNurseNumData;
		iHighCareUnitDoctorNum = iHighCareUnitRoomDoctorNumData;
		iHighCareUnitNurseNum = iHighCareUnitRoomNurseNumData;
		iGeneralWardDoctorNum = iGeneralWardRoomDoctorNumData;
		iGeneralWardNurseNum = iGeneralWardRoomNurseNumData;
		iWaitingNurseNum = iWaitingRoomNurseNumData;
		iXRayClinicalEngineerNum = iExaminationXRayRoomClinicalEngineerNumData;
		iCTClinicalEngineerNum = iExaminationCTRoomClinicalEngineerNumData;
		iMRIClinicalEngineerNum = iExaminationMRIRoomClinicalEngineerNumData;
		iAngiographyClinicalEngineerNum = iExaminationAngiographyRoomClinicalEngineerNumData;
		iFastClinicalEngineerNum = iExaminationFastRoomClinicalEngineerNumData;
	}


	/**
	 * <PRE>
	 *    部屋を構成するエージェントの数を設定します。
	 * </PRE>
	 * @param iRandomMode				ランダム発生モード（未使用）
	 */
	public void vSetRandomEmergencyDepartmentAgents( InitInverseSimParam initparam )
	{
		// 固定値とします。
		if( initparam.iGetInitializeGenerateMode() == 0 )
		{
			iConsultationDoctorNum = initparam.iGetConsultationRoomDoctorNum();
			iConsultationNurseNum = initparam.iGetConsultationRoomNurseNum();
			iOperationDoctorNum = initparam.iGetOperationRoomDoctorNum();
			iOperationNurseNum = initparam.iGetOperationRoomNurseNum();
			iEmergencyDoctorNum = initparam.iGetEmergencyRoomDoctorNum();
			iEmergencyNurseNum = initparam.iGetEmergencyRoomNurseNum();
			iEmergencyClinicalEngineerNum = initparam.iGetEmergencyRoomClinicalEngineerNum();
			iObservationNurseNum = initparam.iGetObservationRoomNurseNum();
			iSevereInjuryObservationNurseNum = initparam.iGetSevereInjuryObservationRoomNurseNum();
			iIntensiveCareUnitDoctorNum = initparam.iGetIntensiveCareUnitDoctorNum();
			iIntensiveCareUnitNurseNum = initparam.iGetIntensiveCareUnitNurseNum();
			iHighCareUnitDoctorNum = initparam.iGetHighCareUnitDoctorNum();
			iHighCareUnitNurseNum = initparam.iGetHighCareUnitNurseNum();
			iGeneralWardDoctorNum = initparam.iGetGeneralWardDoctorNum();;
			iGeneralWardNurseNum = initparam.iGetGeneralWardNurseNum();
			iWaitingNurseNum = initparam.iGetWaitingRoomNurseNum();
			iXRayClinicalEngineerNum = initparam.iGetXRayRoomClinicalEngineerNum();
			iCTClinicalEngineerNum = initparam.iGetCTRoomClinicalEngineerNum();
			iMRIClinicalEngineerNum = initparam.iGetMRIRoomClinicalEngineerNum();
			iAngiographyClinicalEngineerNum = initparam.iGetAngiographyRoomClinicalEngineerNum();
			iFastClinicalEngineerNum = initparam.iGetFastRoomClinicalEngineerNum();

			// 聖隷浜松病院用
//			iConsultationDoctorNum = 1;
//			iConsultationNurseNum = 2;
//			iOperationDoctorNum = 2;
//			iOperationNurseNum = 4;
//			iEmergencyDoctorNum = 2;
//			iEmergencyNurseNum = 6;
//			iEmergencyClinicalEngineerNum = 0;
//			iObservationNurseNum = 0;
//			iSevereInjuryObservationNurseNum = 0;
//			iIntensiveCareUnitDoctorNum = 2;
//			iIntensiveCareUnitNurseNum = 22;
//			iHighCareUnitDoctorNum = 2;
//			iHighCareUnitNurseNum = 8;
//			iGeneralWardDoctorNum = 1;
//			iGeneralWardNurseNum = 744;
//			iWaitingNurseNum = 8;
//			iXRayClinicalEngineerNum = 1;
//			iCTClinicalEngineerNum = 1;
//			iMRIClinicalEngineerNum = 1;
//			iAngiographyClinicalEngineerNum = 1;
//			iFastClinicalEngineerNum = 1;
		}
		// 一様乱数により発生させます。
		else if( initparam.iGetInitializeGenerateMode() == 1 )
		{
			// パラメータの設定をします。
			iConsultationDoctorNum = rnd.NextInt( initparam.iGetConsultationRoomDoctorNum()+1 );
			iConsultationNurseNum = rnd.NextInt( initparam.iGetConsultationRoomNurseNum()+1 );
			iOperationDoctorNum = rnd.NextInt( initparam.iGetOperationRoomDoctorNum()+1 );
			iOperationNurseNum = rnd.NextInt( initparam.iGetOperationRoomNurseNum()+1 );
			iEmergencyDoctorNum = rnd.NextInt( initparam.iGetEmergencyRoomDoctorNum()+1 );
			iEmergencyNurseNum = rnd.NextInt( initparam.iGetEmergencyRoomNurseNum()+1 );
			iEmergencyClinicalEngineerNum = rnd.NextInt( initparam.iGetEmergencyRoomClinicalEngineerNum()+1 );
			iObservationNurseNum = rnd.NextInt( initparam.iGetObservationRoomNurseNum()+1 );
			iSevereInjuryObservationNurseNum = rnd.NextInt( initparam.iGetSevereInjuryObservationRoomNurseNum()+1 );
			iIntensiveCareUnitDoctorNum = rnd.NextInt( initparam.iGetIntensiveCareUnitDoctorNum()+1 );
			iIntensiveCareUnitNurseNum = rnd.NextInt( initparam.iGetIntensiveCareUnitNurseNum()+1 );
			iHighCareUnitDoctorNum = rnd.NextInt( initparam.iGetHighCareUnitDoctorNum()+1 );
			iHighCareUnitNurseNum = rnd.NextInt( initparam.iGetHighCareUnitNurseNum()+1 );
			iGeneralWardDoctorNum = rnd.NextInt( initparam.iGetGeneralWardDoctorNum()+1 );
			iGeneralWardNurseNum = rnd.NextInt( initparam.iGetGeneralWardNurseNum()+1 );
			iWaitingNurseNum = rnd.NextInt( initparam.iGetWaitingRoomNurseNum()+1 );
			iXRayClinicalEngineerNum = rnd.NextInt( initparam.iGetXRayRoomClinicalEngineerNum()+1 );
			iCTClinicalEngineerNum = rnd.NextInt( initparam.iGetCTRoomClinicalEngineerNum()+1 );
			iMRIClinicalEngineerNum = rnd.NextInt( initparam.iGetMRIRoomClinicalEngineerNum()+1 );
			iAngiographyClinicalEngineerNum = rnd.NextInt( initparam.iGetAngiographyRoomClinicalEngineerNum()+1 );
			iFastClinicalEngineerNum = rnd.NextInt( initparam.iGetFastRoomClinicalEngineerNum()+1 );
		}
		// 基準となる値からの近傍値で発生させます。
		else if( initparam.iGetInitializeGenerateMode() == 2 )
		{
			double lfRes = 0.0;
			// パラメータの設定をします。
			lfRes = initparam.lfGetConsultationRoomDoctorNumMaxWeight()-initparam.lfGetConsultationRoomDoctorNumMinWeight();
			iConsultationDoctorNum = (int)(initparam.iGetConsultationRoomDoctorNum()*lfRes*rnd.NextUnif()+initparam.iGetConsultationRoomDoctorNum()*initparam.lfGetConsultationRoomDoctorNumMinWeight());
			iConsultationDoctorNum = iConsultationRoomNum > 0 && iConsultationDoctorNum == 0  ? 1 : iConsultationDoctorNum;
			lfRes = initparam.lfGetConsultationRoomNurseNumMaxWeight()-initparam.lfGetConsultationRoomNurseNumMinWeight();
			iConsultationNurseNum = (int)(initparam.iGetConsultationRoomNurseNum()*lfRes*rnd.NextUnif()+initparam.iGetConsultationRoomNurseNum()*initparam.lfGetConsultationRoomNurseNumMinWeight());
			iConsultationNurseNum = iConsultationRoomNum > 0 && iConsultationNurseNum == 0  ? 1 : iConsultationNurseNum;
			lfRes = initparam.lfGetOperationRoomDoctorNumMaxWeight()-initparam.lfGetOperationRoomDoctorNumMinWeight();
			iOperationDoctorNum = (int)(initparam.iGetOperationRoomDoctorNum()*lfRes*rnd.NextUnif()+ initparam.iGetOperationRoomDoctorNum()*initparam.lfGetOperationRoomDoctorNumMinWeight());
			iOperationDoctorNum = iOperationRoomNum > 0 && iOperationDoctorNum == 0  ? 1 : iOperationDoctorNum;
			lfRes = initparam.lfGetOperationRoomNurseNumMaxWeight()-initparam.lfGetOperationRoomNurseNumMinWeight();
			iOperationNurseNum = (int)( initparam.iGetOperationRoomNurseNum()*lfRes*rnd.NextUnif()+ initparam.iGetOperationRoomNurseNum()*initparam.lfGetOperationRoomNurseNumMinWeight());
			iOperationNurseNum = iOperationRoomNum > 0 && iOperationNurseNum == 0  ? 1 : iOperationNurseNum;
			lfRes = initparam.lfGetEmergencyRoomDoctorNumMaxWeight()-initparam.lfGetEmergencyRoomDoctorNumMinWeight();
			iEmergencyDoctorNum = (int)( initparam.iGetEmergencyRoomDoctorNum()*lfRes*rnd.NextUnif()+ initparam.iGetEmergencyRoomDoctorNum()*initparam.lfGetEmergencyRoomDoctorNumMinWeight());
			iEmergencyDoctorNum = iEmergencyRoomNum > 0 && iEmergencyDoctorNum == 0  ? 1 : iEmergencyDoctorNum;
			lfRes = initparam.lfGetEmergencyRoomNurseNumMaxWeight()-initparam.lfGetEmergencyRoomNurseNumMinWeight();
			iEmergencyNurseNum = (int)(initparam.iGetEmergencyRoomNurseNum()*lfRes*rnd.NextUnif()+ initparam.iGetEmergencyRoomNurseNum()*initparam.lfGetEmergencyRoomNurseNumMinWeight());
			iEmergencyNurseNum = iEmergencyRoomNum > 0 && iEmergencyNurseNum == 0  ? 1 : iEmergencyNurseNum;
			lfRes = initparam.lfGetEmergencyRoomDoctorNumMaxWeight()-initparam.lfGetEmergencyRoomDoctorNumMinWeight();
			iEmergencyClinicalEngineerNum = (int)(initparam.iGetEmergencyRoomClinicalEngineerNum()*lfRes*rnd.NextUnif()+ initparam.iGetEmergencyRoomClinicalEngineerNum()*initparam.lfGetEmergencyRoomDoctorNumMinWeight());
			iEmergencyClinicalEngineerNum = iEmergencyRoomNum > 0 && iEmergencyClinicalEngineerNum == 0  ? 1 : iEmergencyClinicalEngineerNum;
			lfRes = initparam.lfGetObservationRoomNurseNumMaxWeight()-initparam.lfGetObservationRoomNurseNumMinWeight();
			iObservationNurseNum = (int)(initparam.iGetObservationRoomNurseNum()*lfRes*rnd.NextUnif()+ initparam.iGetObservationRoomNurseNum()*initparam.lfGetObservationRoomNurseNumMinWeight());
			iObservationNurseNum = iObservationRoomNum > 0 && iObservationNurseNum == 0  ? 1 : iObservationNurseNum;
			lfRes = initparam.lfGetSevereInjuryObservationRoomNurseNumMaxWeight()-initparam.lfGetSevereInjuryObservationRoomNurseNumMinWeight();
			iSevereInjuryObservationNurseNum = (int)(initparam.iGetSevereInjuryObservationRoomNurseNum()*lfRes*rnd.NextUnif()+ initparam.iGetSevereInjuryObservationRoomNurseNum()*initparam.lfGetSevereInjuryObservationRoomNurseNumMinWeight());
			iSevereInjuryObservationNurseNum = iSevereInjuryObservationRoomNum > 0 && iSevereInjuryObservationNurseNum == 0  ? 1 : iSevereInjuryObservationNurseNum;
			lfRes = initparam.lfGetIntensiveCareUnitDoctorNumMaxWeight()-initparam.lfGetIntensiveCareUnitDoctorNumMinWeight();
			iIntensiveCareUnitDoctorNum = (int)( initparam.iGetIntensiveCareUnitDoctorNum()*lfRes*rnd.NextUnif()+ initparam.iGetIntensiveCareUnitDoctorNum()*initparam.lfGetIntensiveCareUnitDoctorNumMinWeight());
			iIntensiveCareUnitDoctorNum = iIntensiveCareUnitRoomNum > 0 && iIntensiveCareUnitDoctorNum == 0  ? 1 : iIntensiveCareUnitDoctorNum;
			lfRes = initparam.lfGetIntensiveCareUnitNurseNumMaxWeight()-initparam.lfGetIntensiveCareUnitNurseNumMinWeight();
			iIntensiveCareUnitNurseNum = (int)( initparam.iGetIntensiveCareUnitNurseNum()*lfRes*rnd.NextUnif()+ initparam.iGetIntensiveCareUnitNurseNum()*initparam.lfGetIntensiveCareUnitNurseNumMinWeight());
			iIntensiveCareUnitNurseNum = iIntensiveCareUnitRoomNum > 0 && iIntensiveCareUnitNurseNum == 0  ? 1 : iIntensiveCareUnitNurseNum;
			lfRes = initparam.lfGetHighCareUnitDoctorNumMaxWeight()-initparam.lfGetHighCareUnitDoctorNumMinWeight();
			iHighCareUnitDoctorNum = (int)( initparam.iGetHighCareUnitDoctorNum()*lfRes*rnd.NextUnif()+ initparam.iGetHighCareUnitDoctorNum()*initparam.lfGetHighCareUnitDoctorNumMinWeight());
			iHighCareUnitDoctorNum = iHighCareUnitRoomNum > 0 && iHighCareUnitDoctorNum == 0  ? 1 : iHighCareUnitDoctorNum;
			lfRes = initparam.lfGetHighCareUnitNurseNumMaxWeight()-initparam.lfGetHighCareUnitNurseNumMinWeight();
			iHighCareUnitNurseNum = (int)( initparam.iGetHighCareUnitNurseNum()*lfRes*rnd.NextUnif()+ initparam.iGetHighCareUnitNurseNum()*initparam.lfGetHighCareUnitNurseNumMinWeight());
			iHighCareUnitNurseNum = iHighCareUnitRoomNum > 0 && iHighCareUnitNurseNum == 0  ? 1 : iHighCareUnitNurseNum;
			lfRes = initparam.lfGetGeneralWardDoctorNumMaxWeight()-initparam.lfGetGeneralWardDoctorNumMinWeight();
			iGeneralWardDoctorNum = (int)( initparam.iGetGeneralWardDoctorNum()*lfRes*rnd.NextUnif()+ initparam.iGetGeneralWardDoctorNum()*initparam.lfGetGeneralWardDoctorNumMinWeight());
			iGeneralWardDoctorNum = iGeneralWardRoomNum > 0 && iGeneralWardDoctorNum == 0  ? 1 : iGeneralWardDoctorNum;
			lfRes = initparam.lfGetGeneralWardNurseNumMaxWeight()-initparam.lfGetGeneralWardNurseNumMinWeight();
			iGeneralWardNurseNum = (int)( initparam.iGetGeneralWardNurseNum()*lfRes*rnd.NextUnif()+ initparam.iGetGeneralWardNurseNum()*initparam.lfGetGeneralWardNurseNumMinWeight());
			iGeneralWardNurseNum = iGeneralWardRoomNum > 0 && iGeneralWardNurseNum == 0  ? 1 : iGeneralWardNurseNum;
			lfRes = initparam.lfGetWaitingRoomNurseNumMaxWeight()-initparam.lfGetWaitingRoomNurseNumMinWeight();
			iWaitingNurseNum = (int)( initparam.iGetWaitingRoomNurseNum()*lfRes*rnd.NextUnif()+ initparam.iGetWaitingRoomNurseNum()*initparam.lfGetWaitingRoomNurseNumMinWeight());
			iWaitingNurseNum = iWaitingRoomNum > 0 && iWaitingNurseNum == 0  ? 1 : iWaitingNurseNum;
			lfRes = initparam.lfGetXRayRoomClinicalEngineerNumMaxWeight()-initparam.lfGetXRayRoomClinicalEngineerNumMinWeight();
			iXRayClinicalEngineerNum = (int)( initparam.iGetXRayRoomClinicalEngineerNum()*lfRes*rnd.NextUnif()+ initparam.iGetXRayRoomClinicalEngineerNum()*initparam.lfGetXRayRoomClinicalEngineerNumMinWeight());
			iXRayClinicalEngineerNum = iXRayRoomNum > 0 && iXRayClinicalEngineerNum == 0  ? 1 : iXRayClinicalEngineerNum;
			lfRes = initparam.lfGetCTRoomClinicalEngineerNumMaxWeight()-initparam.lfGetCTRoomClinicalEngineerNumMinWeight();
			iCTClinicalEngineerNum = (int)( initparam.iGetCTRoomClinicalEngineerNum()*lfRes*rnd.NextUnif()+initparam.iGetCTRoomClinicalEngineerNum()*initparam.lfGetCTRoomClinicalEngineerNumMinWeight());
			iCTClinicalEngineerNum = iCTRoomNum > 0 && iCTClinicalEngineerNum == 0  ? 1 : iCTClinicalEngineerNum;
			lfRes = initparam.lfGetMRIRoomClinicalEngineerNumMaxWeight()-initparam.lfGetMRIRoomClinicalEngineerNumMinWeight();
			iMRIClinicalEngineerNum = (int)( initparam.iGetMRIRoomClinicalEngineerNum()*lfRes*rnd.NextUnif()+ initparam.iGetMRIRoomClinicalEngineerNum()*initparam.lfGetMRIRoomClinicalEngineerNumMinWeight());
			iMRIClinicalEngineerNum = iMRIRoomNum > 0 && iMRIClinicalEngineerNum == 0  ? 1 : iMRIClinicalEngineerNum;
			lfRes = initparam.lfGetAngiographyRoomClinicalEngineerNumMaxWeight()-initparam.lfGetAngiographyRoomClinicalEngineerNumMinWeight();
			iAngiographyClinicalEngineerNum = (int)( initparam.iGetAngiographyRoomClinicalEngineerNum()*lfRes*rnd.NextUnif()+ initparam.iGetAngiographyRoomClinicalEngineerNum()*initparam.lfGetAngiographyRoomClinicalEngineerNumMinWeight());
			iAngiographyClinicalEngineerNum = iAngiographyRoomNum > 0 && iAngiographyClinicalEngineerNum == 0  ? 1 : iAngiographyClinicalEngineerNum;
			lfRes = initparam.lfGetFastRoomClinicalEngineerNumMaxWeight()-initparam.lfGetFastRoomClinicalEngineerNumMinWeight();
			iFastClinicalEngineerNum = (int)( initparam.iGetFastRoomClinicalEngineerNum()*lfRes*rnd.NextUnif()+ initparam.iGetFastRoomClinicalEngineerNum()*initparam.lfGetFastRoomClinicalEngineerNumMinWeight());
			iFastClinicalEngineerNum = iFastRoomNum > 0 && iFastClinicalEngineerNum == 0 ? 1 : iFastClinicalEngineerNum;
		}
		else
		{

		}
	}

	/**
	 * <PRE>
	 *   診察室数及び構成する医師の数、看護師の数を設定します。
	 * </PRE>
	 * @param iConsultationRoomNum	診察室エージェント数
	 * @param strPath				診察室に所属する医師、看護師、医療技師のパラメータが記述されたファイルパス
	 * @param engine				FUSEエンジン
	 * @param sfmtRandom			メルセンヌツイスターのインスタンス
	 * @throws IOException			java標準のIO例外クラス
	 * @since 2015/08/05
	 */
	private void vSetConsultationRooms(int iConsultationRoomNum, String strPath, SimulationEngine engine, utility.sfmt.Rand sfmtRandom ) throws IOException
	{
		int i,j;
		int iCurrentNurseNum = 0;
		int iStartNurseLoc = 0;
		int iStartDoctorLoc = 0;
		int iLocation = 0;

		int[] aiNurseCategory;
		int[] aiNurseTriageProtocol;
		int[] aiNurseTriageLevel;
		double[] alfNurseTriageYearExperience;
		double[] alfNurseYearExperience;
		double[] alfNurseConExperience;
		double[] alfNurseExperienceRate1;
		double[] alfNurseExperienceRate2;;
		double[] alfNurseConExperienceAIS;
		double[] alfNurseExperienceRateAIS1;
		double[] alfNurseExperienceRateAIS2;
		double[] alfNurseConTired1;
		double[] alfNurseConTired2;
		double[] alfNurseConTired3;
		double[] alfNurseConTired4;
		double[] alfNurseTiredRate;
		double[] alfNurseAssociationRate;
		double[] alfNurseObservationTime;
		double[] alfNurseObservationProcessTime;
		double[] alfNurseTriageTime;
		int[] aiNurseDepartment;
		int[] aiNurseRoomNumber;

		double lfDoctorYearExperience;
		double lfDoctorConExperience;
		double lfDoctorExperienceRate1;
		double lfDoctorExperienceRate2;;
		double lfDoctorConExperienceAIS;
		double lfDoctorExperienceRateAIS1;
		double lfDoctorExperienceRateAIS2;
		double lfDoctorConTired1;
		double lfDoctorConTired2;
		double lfDoctorConTired3;
		double lfDoctorConTired4;
		double lfDoctorTiredRate;
		double lfDoctorRevisedOperationRate;
		double lfDoctorAssociationRate;
		double lfDoctorConsultationTime;
		double lfDoctorOperationTime;
		double lfDoctorEmergencyTime;
		int iDoctorDepartment;
		int iDoctorRoomNumber;

		int iRow = 0;
		int iColumn = 0;
		double[][] pplfParameter;

		CCsv csv = new CCsv();
		if( strPath == "" )
		{
			csv.vOpen( "./parameter/診察室.csv", "read" );
		}
		else
		{
			csv.vOpen( strPath, "read" );
		}
		csv.vGetRowColumn();

		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		pplfParameter = new double[iColumn][iRow];
		csv.vRead( pplfParameter );
		csv.vClose();

		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			// 医師数、看護師数、医療技師数を設定します。
			if( pplfParameter[0].length > 1 )
			{
				iConsultationDoctorNum = 0;
				iConsultationNurseNum = 0;
			}
			for( i = 0;i < pplfParameter[0].length; i++ )
			{
				if( pplfParameter[0][i] == 1.0 )
				{
					iConsultationDoctorNum++;
				}
				else if( pplfParameter[0][i] == 2.0 )
				{
					iConsultationNurseNum++;
				}
			}
		}
		// 逆シミュレーションモードの場合
		else
		{
			// 部屋数が0以下の場合はすべて0にして終了する。
			if( this.iConsultationRoomNum <= 0 )
			{
				this.iConsultationRoomNum = 0;
				iConsultationNurseNum = 0;
				iConsultationDoctorNum = 0;
				ArrayListConsultationRooms = new ArrayList<ERConsultationRoom>();
				return ;
			}
		}


		if( ArrayListConsultationRooms == null )
		{
			ArrayListConsultationRooms = new ArrayList<ERConsultationRoom>();
		}

		alfNurseYearExperience			= new double[iConsultationNurseNum];
		alfNurseConExperience			= new double[iConsultationNurseNum];
		alfNurseExperienceRate1			= new double[iConsultationNurseNum];
		alfNurseExperienceRate2			= new double[iConsultationNurseNum];
		alfNurseConExperienceAIS		= new double[iConsultationNurseNum];
		alfNurseExperienceRateAIS1		= new double[iConsultationNurseNum];
		alfNurseExperienceRateAIS2		= new double[iConsultationNurseNum];
		alfNurseConTired1				= new double[iConsultationNurseNum];
		alfNurseConTired2				= new double[iConsultationNurseNum];
		alfNurseConTired3				= new double[iConsultationNurseNum];
		alfNurseConTired4				= new double[iConsultationNurseNum];
		alfNurseTiredRate				= new double[iConsultationNurseNum];
		alfNurseAssociationRate			= new double[iConsultationNurseNum];
		alfNurseObservationTime			= new double[iConsultationNurseNum];
		alfNurseObservationProcessTime	= new double[iConsultationNurseNum];
		alfNurseTriageTime				= new double[iConsultationNurseNum];
		alfNurseAssociationRate			= new double[iConsultationNurseNum];
		alfNurseTriageYearExperience	= new double[iConsultationNurseNum];

		aiNurseTriageProtocol			= new int[iConsultationNurseNum];
		aiNurseTriageLevel				= new int[iConsultationNurseNum];
		aiNurseDepartment				= new int[iConsultationNurseNum];
		aiNurseCategory					= new int[iConsultationNurseNum];
		aiNurseRoomNumber				= new int[iConsultationNurseNum];

		lfDoctorYearExperience	 		= 10;
		lfDoctorConExperience 			= 3;
		lfDoctorConTired1 				= 3;
		lfDoctorConTired2 				= 3;
		lfDoctorConTired3 				= 3;
		lfDoctorConTired4 				= 3;
		lfDoctorTiredRate	 			= 3.0;
		lfDoctorRevisedOperationRate	= 0.666666666666;
		lfDoctorAssociationRate	 		= 1.0;
		lfDoctorConsultationTime 		= 30*60;
		lfDoctorOperationTime 			= 30;
		lfDoctorEmergencyTime 			= 30*60;
		iDoctorDepartment 				= 2;
		lfDoctorConExperience			= 3;
		lfDoctorExperienceRate1			= 2.1;
		lfDoctorExperienceRate2			= 0.9;
		lfDoctorConExperienceAIS		= 0.14;
		lfDoctorExperienceRateAIS1		= 0.2;
		lfDoctorExperienceRateAIS2		= 1.1;
		iDoctorRoomNumber 				= 1;

		iStartDoctorLoc = 0;
		iStartNurseLoc = 0;
		for( i = 0;i < pplfParameter[0].length; i++ )
		{
			if( pplfParameter[0][i] == 1.0 )
			{
				if( iStartDoctorLoc == 0 ) iStartDoctorLoc = i;
				lfDoctorYearExperience 		= pplfParameter[1][i];
				lfDoctorConExperience 		= pplfParameter[2][i];
				lfDoctorConTired1 			= pplfParameter[3][i];
				lfDoctorConTired2 			= pplfParameter[4][i];
				lfDoctorConTired3 			= pplfParameter[5][i];
				lfDoctorConTired4 			= pplfParameter[6][i];
				lfDoctorTiredRate 			= pplfParameter[7][i];
				lfDoctorRevisedOperationRate= pplfParameter[8][i];
				lfDoctorAssociationRate	 	= pplfParameter[9][i];
				iDoctorDepartment			= (int)pplfParameter[10][i];
				lfDoctorExperienceRate1		= pplfParameter[11][i];
				lfDoctorExperienceRate2		= pplfParameter[12][i];
				lfDoctorConExperienceAIS	= pplfParameter[13][i];
				lfDoctorExperienceRateAIS1	= pplfParameter[14][i];
				lfDoctorExperienceRateAIS2	= pplfParameter[15][i];
				iDoctorRoomNumber			= (int)pplfParameter[16][i];
				lfDoctorConsultationTime	= 30*60;
				lfDoctorOperationTime		= 30;
				lfDoctorEmergencyTime		= 30*60;
			}
			else if( pplfParameter[0][i] == 2.0 )
			{
				if( iConsultationNurseNum <= iCurrentNurseNum ) continue;
				if( iStartNurseLoc == 0 ) iStartNurseLoc = i;
				aiNurseCategory[iCurrentNurseNum] 					= (int)pplfParameter[1][i];
				aiNurseTriageProtocol[iCurrentNurseNum] 			= (int)pplfParameter[2][i];
				aiNurseTriageLevel[iCurrentNurseNum] 				= (int)pplfParameter[3][i];
				alfNurseTriageYearExperience[iCurrentNurseNum]	 	= pplfParameter[4][i];
				alfNurseYearExperience[iCurrentNurseNum] 			= pplfParameter[5][i];
				alfNurseConExperience[iCurrentNurseNum] 			= pplfParameter[6][i];
				alfNurseConTired1[iCurrentNurseNum]			 		= pplfParameter[7][i];
				alfNurseConTired2[iCurrentNurseNum]				 	= pplfParameter[8][i];
				alfNurseConTired3[iCurrentNurseNum]					= pplfParameter[9][i];
				alfNurseConTired4[iCurrentNurseNum]				 	= pplfParameter[10][i];
				alfNurseTiredRate[iCurrentNurseNum]					= pplfParameter[11][i];
				alfNurseAssociationRate[iCurrentNurseNum]		 	= pplfParameter[12][i];
				aiNurseDepartment[iCurrentNurseNum]				 	= (int)pplfParameter[13][i];
				alfNurseObservationTime[iCurrentNurseNum]		 	= 30*60;
				alfNurseObservationProcessTime[iCurrentNurseNum] 	= 30;
				alfNurseTriageTime[iCurrentNurseNum] 				= 30*60;
				alfNurseExperienceRate1[iCurrentNurseNum]			= pplfParameter[14][i];
				alfNurseExperienceRate2[iCurrentNurseNum]			= pplfParameter[15][i];
				alfNurseConExperienceAIS[iCurrentNurseNum]			= pplfParameter[16][i];
				alfNurseExperienceRateAIS1[iCurrentNurseNum]		= pplfParameter[17][i];
				alfNurseExperienceRateAIS2[iCurrentNurseNum]		= pplfParameter[18][i];
				aiNurseRoomNumber[iCurrentNurseNum]					= (int)pplfParameter[19][i];
				iCurrentNurseNum++;
			}
			else if( pplfParameter[0][i] == 3.0 )
			{

			}
			else
			{

			}
		}

		if( iConsultationNurseNum > iCurrentNurseNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iConsultationNurseNum-iCurrentNurseNum; i < iConsultationNurseNum; i++ )
			{
				iLocation = i % iCurrentNurseNum + iStartNurseLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				aiNurseCategory[i] 					= (int)pplfParameter[1][iLocation];
				aiNurseTriageProtocol[i] 			= (int)pplfParameter[2][iLocation];
				aiNurseTriageLevel[i] 				= (int)pplfParameter[3][iLocation];
				alfNurseTriageYearExperience[i]	 	= pplfParameter[4][iLocation];
				alfNurseYearExperience[i] 			= pplfParameter[5][iLocation];
				alfNurseConExperience[i] 			= pplfParameter[6][iLocation];
				alfNurseConTired1[i]			 	= pplfParameter[7][iLocation];
				alfNurseConTired2[i]			 	= pplfParameter[8][iLocation];
				alfNurseConTired3[i]				= pplfParameter[9][iLocation];
				alfNurseConTired4[i]			 	= pplfParameter[10][iLocation];
				alfNurseTiredRate[i]				= pplfParameter[11][iLocation];
				alfNurseAssociationRate[i]		 	= pplfParameter[12][iLocation];
				aiNurseDepartment[i]			 	= (int)pplfParameter[13][iLocation];
				alfNurseObservationTime[i]		 	= 30*60;
				alfNurseObservationProcessTime[i] 	= 30;
				alfNurseTriageTime[i] 				= 30*60;
				alfNurseExperienceRate1[i]			= pplfParameter[14][iLocation];
				alfNurseExperienceRate2[i]			= pplfParameter[15][iLocation];
				alfNurseConExperienceAIS[i]			= pplfParameter[16][iLocation];
				alfNurseExperienceRateAIS1[i]		= pplfParameter[17][iLocation];
				alfNurseExperienceRateAIS2[i]		= pplfParameter[18][iLocation];
				aiNurseRoomNumber[i]				= (int)pplfParameter[19][iLocation];
			}
		}

		for( i = 0;i < iConsultationRoomNum; i++ )
		{
			ArrayListConsultationRooms.add( new ERConsultationRoom() );
			ArrayListConsultationRooms.get(i).vSetConsultationRoomRandom( sfmtRandom );
			ArrayListConsultationRooms.get(i).vSetSimulationEngine( engine );

			ArrayListConsultationRooms.get(i).vCreateDoctorAgents( iConsultationDoctorNum );
			ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetSimulationEngine( engine );
			ArrayListConsultationRooms.get(i).vSetDoctorsRandom();
			ArrayListConsultationRooms.get(i).vSetDoctorAgentParameter( lfDoctorYearExperience,
					lfDoctorConExperience,
					lfDoctorExperienceRate1,
					lfDoctorExperienceRate2,
					lfDoctorConExperienceAIS,
					lfDoctorExperienceRateAIS1,
					lfDoctorExperienceRateAIS2,
					lfDoctorConTired1,
					lfDoctorConTired2,
					lfDoctorConTired3,
					lfDoctorConTired4,
					lfDoctorTiredRate,
					lfDoctorRevisedOperationRate,
					lfDoctorAssociationRate,
					lfDoctorConsultationTime,
					lfDoctorOperationTime,
					lfDoctorEmergencyTime,
					iDoctorDepartment,
					iDoctorRoomNumber );
			ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetSimulationEndTime( lfEndTime );

			ArrayListConsultationRooms.get(i).vCreateNurseAgents( iConsultationNurseNum );
			ArrayListConsultationRooms.get(i).vSetNursesRandom();
			ArrayListConsultationRooms.get(i).vSetNurseAgentParameter(aiNurseCategory,
					  aiNurseTriageProtocol,
					  aiNurseTriageLevel,
					  alfNurseTriageYearExperience,
					  alfNurseYearExperience,
					  alfNurseConExperience,
					  alfNurseExperienceRate1,
					  alfNurseExperienceRate2,
					  alfNurseConExperienceAIS,
					  alfNurseExperienceRateAIS1,
					  alfNurseExperienceRateAIS2,
					  alfNurseConTired1,
					  alfNurseConTired2,
					  alfNurseConTired3,
					  alfNurseConTired4,
					  alfNurseTiredRate,
					  alfNurseAssociationRate,
					  alfNurseObservationTime,
					  alfNurseObservationProcessTime,
					  alfNurseTriageTime,
					  aiNurseDepartment,
					  aiNurseRoomNumber );
			for( j = 0; j < ArrayListConsultationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListConsultationRooms.get(i).cGetNurseAgent(j).vSetSimulationEngine( engine );
				ArrayListConsultationRooms.get(i).cGetNurseAgent(j).vSetRandom( sfmtRandom );
				ArrayListConsultationRooms.get(i).cGetNurseAgent(j).vSetSimulationEndTime( lfEndTime );
			}
		}
	}

	/**
	 * <PRE>
	 *   手術室数及び構成する医師の数、看護師の数を設定します。
	 * </PRE>
	 * @param iOperationRoomNum		手術室数
	 * @param strPath				手術室に所属する医師、看護師、医療技師のパラメータが記述されたファイルパス
	 * @param engine				FUSEエンジン
	 * @param sfmtRandom			メルセンヌツイスターインスタンス
	 * @throws IOException			ファイル処理中の例外
	 * @since 2015/08/05
	 */
	private void vSetOperationRooms(int iOperationRoomNum, String strPath, SimulationEngine engine, utility.sfmt.Rand sfmtRandom ) throws IOException
	{
		int i,j;
		int iCurrentDoctorNum = 0;
		int iCurrentNurseNum = 0;
		int iStartDoctorLoc = 0;
		int iStartNurseLoc = 0;
		int iLocation = 0;

		int[] aiNurseCategory;
		int[] aiNurseTriageProtocol;
		int[] aiNurseTriageLevel;
		double[] alfNurseTriageYearExperience;
		double[] alfNurseYearExperience;
		double[] alfNurseConExperience;
		double[] alfNurseExperienceRate1;
		double[] alfNurseExperienceRate2;;
		double[] alfNurseConExperienceAIS;
		double[] alfNurseExperienceRateAIS1;
		double[] alfNurseExperienceRateAIS2;
		double[] alfNurseConTired1;
		double[] alfNurseConTired2;
		double[] alfNurseConTired3;
		double[] alfNurseConTired4;
		double[] alfNurseTiredRate;
		double[] alfNurseAssociationRate;
		double[] alfNurseObservationTime;
		double[] alfNurseObservationProcessTime;
		double[] alfNurseTriageTime;
		int[] aiNurseDepartment;
		int[] aiNurseRoomNumber;

		double[] alfDoctorYearExperience;
		double[] alfDoctorConExperience;
		double[] alfDoctorExperienceRate1;
		double[] alfDoctorExperienceRate2;;
		double[] alfDoctorConExperienceAIS;
		double[] alfDoctorExperienceRateAIS1;
		double[] alfDoctorExperienceRateAIS2;
		double[] alfDoctorConTired1;
		double[] alfDoctorConTired2;
		double[] alfDoctorConTired3;
		double[] alfDoctorConTired4;
		double[] alfDoctorTiredRate;
		double[] alfDoctorRevisedOperationRate;
		double[] alfDoctorAssociationRate;
		double[] alfDoctorConsultationTime;
		double[] alfDoctorOperationTime;
		double[] alfDoctorEmergencyTime;
		int[] aiDoctorDepartment;
		int[] aiDoctorRoomNumber;

		int iRow = 0;
		int iColumn = 0;
		double[][] pplfParameter;

		CCsv csv = new CCsv();
		if( strPath == "" )
		{
			csv.vOpen( "./parameter/手術室.csv", "read" );
		}
		else
		{
			csv.vOpen( strPath, "read" );
		}
		csv.vGetRowColumn();

		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		pplfParameter = new double[iColumn][iRow];
		csv.vRead( pplfParameter );
		csv.vClose();
		// 医師数、看護師数、医療技師数を設定します。
		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			if( pplfParameter[0].length > 1 )
			{
				iOperationDoctorNum = 0;
				iOperationNurseNum = 0;
			}
			for( i = 0;i < pplfParameter[0].length; i++ )
			{
				if( pplfParameter[0][i] == 1.0 )
				{
					iOperationDoctorNum++;
				}
				else if( pplfParameter[0][i] == 2.0 )
				{
					iOperationNurseNum++;
				}
				else if( pplfParameter[0][i] == 3.0 )
				{

				}
			}
		}
		// 逆シミュレーションモードの場合
		else
		{
			// 部屋数が0以下の場合はすべて0にして終了する。
			if( this.iOperationRoomNum <= 0 )
			{
				this.iOperationRoomNum = 0;
				iOperationNurseNum = 0;
				iOperationDoctorNum = 0;
				ArrayListOperationRooms = new ArrayList<EROperationRoom>();
				return ;
			}
		}

		alfNurseYearExperience			= new double[iOperationNurseNum];
		alfNurseConExperience			= new double[iOperationNurseNum];
		alfNurseExperienceRate1			= new double[iOperationNurseNum];
		alfNurseExperienceRate2			= new double[iOperationNurseNum];
		alfNurseConExperienceAIS		= new double[iOperationNurseNum];
		alfNurseExperienceRateAIS1		= new double[iOperationNurseNum];
		alfNurseExperienceRateAIS2		= new double[iOperationNurseNum];
		alfNurseConTired1				= new double[iOperationNurseNum];
		alfNurseConTired2				= new double[iOperationNurseNum];
		alfNurseConTired3				= new double[iOperationNurseNum];
		alfNurseConTired4				= new double[iOperationNurseNum];
		alfNurseTiredRate				= new double[iOperationNurseNum];
		alfNurseAssociationRate			= new double[iOperationNurseNum];
		alfNurseObservationTime			= new double[iOperationNurseNum];
		alfNurseObservationProcessTime	= new double[iOperationNurseNum];
		alfNurseTriageTime				= new double[iOperationNurseNum];
		alfNurseAssociationRate			= new double[iOperationNurseNum];
		alfNurseTriageYearExperience	= new double[iOperationNurseNum];

		aiNurseTriageProtocol			= new int[iOperationNurseNum];
		aiNurseTriageLevel				= new int[iOperationNurseNum];
		aiNurseDepartment				= new int[iOperationNurseNum];
		aiNurseCategory					= new int[iOperationNurseNum];
		aiNurseRoomNumber				= new int[iOperationNurseNum];

		if( ArrayListOperationRooms == null )
		{
			ArrayListOperationRooms = new ArrayList<EROperationRoom>();
		}


		for( i = 0;i < iOperationNurseNum; i++ )
		{
			alfNurseConExperience[i]			= 3;
			alfNurseExperienceRate1[i]			= 2.1;
			alfNurseExperienceRate2[i]			= 0.9;
			alfNurseConExperienceAIS[i]			= 0.14;
			alfNurseExperienceRateAIS1[i]		= 0.2;
			alfNurseExperienceRateAIS2[i]		= 1.1;
			aiNurseRoomNumber[i] 				= 1;
			alfNurseConTired1[i]				= 3;
			alfNurseConTired2[i]				= 3;
			alfNurseConTired3[i]				= 3;
			alfNurseConTired4[i]				= 3;
			alfNurseTiredRate[i]				= 3;
			alfNurseObservationTime[i]			= 30*60;
			alfNurseObservationProcessTime[i]	= 30;
			alfNurseTriageTime[i] 				= 30*60;
			aiNurseTriageProtocol[i] 			= 1;
			aiNurseTriageLevel[i] 				= 5;
			alfNurseAssociationRate[i]		 	= 1.0;
			aiNurseDepartment[i] 				= 2;
		}

		alfDoctorYearExperience			= new double[iOperationDoctorNum];
		alfDoctorConExperience			= new double[iOperationDoctorNum];
		alfDoctorExperienceRate1		= new double[iOperationDoctorNum];
		alfDoctorExperienceRate2		= new double[iOperationDoctorNum];
		alfDoctorConExperienceAIS		= new double[iOperationDoctorNum];
		alfDoctorExperienceRateAIS1		= new double[iOperationDoctorNum];
		alfDoctorExperienceRateAIS2		= new double[iOperationDoctorNum];
		alfDoctorConTired1 				= new double[iOperationDoctorNum];
		alfDoctorConTired2 				= new double[iOperationDoctorNum];
		alfDoctorConTired3 				= new double[iOperationDoctorNum];
		alfDoctorConTired4 				= new double[iOperationDoctorNum];
		alfDoctorTiredRate				= new double[iOperationDoctorNum];
		alfDoctorRevisedOperationRate	= new double[iOperationDoctorNum];
		alfDoctorAssociationRate		= new double[iOperationDoctorNum];
		alfDoctorConsultationTime		= new double[iOperationDoctorNum];
		alfDoctorOperationTime			= new double[iOperationDoctorNum];
		alfDoctorEmergencyTime			= new double[iOperationDoctorNum];
		aiDoctorDepartment				= new int[iOperationDoctorNum];
		aiDoctorRoomNumber				= new int[iOperationDoctorNum];
		for( i = 0;i < iOperationDoctorNum; i++ )
		{
			alfDoctorYearExperience[i]		= 10;
			alfDoctorConExperience[i] 		= 3;
			alfDoctorExperienceRate1[i]		= 2.1;
			alfDoctorExperienceRate2[i]		= 0.9;
			alfDoctorConExperienceAIS[i]	= 0.14;
			alfDoctorExperienceRateAIS1[i]	= 0.2;
			alfDoctorExperienceRateAIS2[i]	= 1.1;
			aiDoctorRoomNumber[i] 			= 1;
			alfDoctorConTired1[i] 			= 3;
			alfDoctorConTired2[i] 			= 3;
			alfDoctorConTired3[i] 			= 3;
			alfDoctorConTired4[i] 			= 3;
			alfDoctorTiredRate[i] 			= 3.0;
			alfDoctorRevisedOperationRate[i]= 0.666666666666;
			alfDoctorAssociationRate[i]	 	= 1.0;
			alfDoctorConsultationTime[i]	= 30*60;
			alfDoctorOperationTime[i] 		= 30;
			alfDoctorEmergencyTime[i]		= 30*60;
			aiDoctorDepartment[i] 			= 2;
		}

		for( i = 0;i < pplfParameter[0].length; i++ )
		{
			if( pplfParameter[0][i] == 1.0 )
			{
				if( iOperationDoctorNum <= iCurrentDoctorNum ) continue;
				if( iStartDoctorLoc == 0 ) iStartDoctorLoc = i;
				alfDoctorYearExperience[iCurrentDoctorNum] 		= pplfParameter[1][i];
				alfDoctorConExperience[iCurrentDoctorNum] 		= pplfParameter[2][i];
				alfDoctorConTired1[iCurrentDoctorNum] 			= pplfParameter[3][i];
				alfDoctorConTired2[iCurrentDoctorNum] 			= pplfParameter[4][i];
				alfDoctorConTired3[iCurrentDoctorNum] 			= pplfParameter[5][i];
				alfDoctorConTired4[iCurrentDoctorNum] 			= pplfParameter[6][i];
				alfDoctorTiredRate[iCurrentDoctorNum] 			= pplfParameter[7][i];
				alfDoctorRevisedOperationRate[iCurrentDoctorNum]= pplfParameter[8][i];
				alfDoctorAssociationRate[iCurrentDoctorNum]	 	= pplfParameter[9][i];
				aiDoctorDepartment[iCurrentDoctorNum]			= (int)pplfParameter[10][i];
				alfDoctorConsultationTime[iCurrentDoctorNum]	= 30*60;
				alfDoctorOperationTime[iCurrentDoctorNum]		= 30;
				alfDoctorEmergencyTime[iCurrentDoctorNum]		= 30*60;
				alfDoctorExperienceRate1[iCurrentDoctorNum]		= pplfParameter[11][i];
				alfDoctorExperienceRate2[iCurrentDoctorNum]		= pplfParameter[12][i];
				alfDoctorConExperienceAIS[iCurrentDoctorNum]	= pplfParameter[13][i];
				alfDoctorExperienceRateAIS1[iCurrentDoctorNum]	= pplfParameter[14][i];
				alfDoctorExperienceRateAIS2[iCurrentDoctorNum]	= pplfParameter[15][i];
				aiDoctorRoomNumber[iCurrentDoctorNum]			= (int)pplfParameter[16][i];
				iCurrentDoctorNum++;
			}
			else if( pplfParameter[0][i] == 2.0 )
			{
				if( iOperationNurseNum <= iCurrentNurseNum ) continue;
				if( iStartNurseLoc == 0 ) iStartNurseLoc = i;
				aiNurseCategory[iCurrentNurseNum] 					= (int)pplfParameter[1][i];
				aiNurseTriageProtocol[iCurrentNurseNum] 			= (int)pplfParameter[2][i];
				aiNurseTriageLevel[iCurrentNurseNum] 				= (int)pplfParameter[3][i];
				alfNurseTriageYearExperience[iCurrentNurseNum] 		= pplfParameter[4][i];
				alfNurseYearExperience[iCurrentNurseNum] 			= pplfParameter[5][i];
				alfNurseConExperience[iCurrentNurseNum] 			= pplfParameter[6][i];
				alfNurseConTired1[iCurrentNurseNum]			 		= pplfParameter[7][i];
				alfNurseConTired2[iCurrentNurseNum]			 		= pplfParameter[8][i];
				alfNurseConTired3[iCurrentNurseNum]			 		= pplfParameter[9][i];
				alfNurseConTired4[iCurrentNurseNum]			 		= pplfParameter[10][i];
				alfNurseTiredRate[iCurrentNurseNum]					= pplfParameter[11][i];
				alfNurseAssociationRate[iCurrentNurseNum]		 	= pplfParameter[12][i];
				aiNurseDepartment[iCurrentNurseNum]			 		= (int)pplfParameter[13][i];
				alfNurseObservationTime[iCurrentNurseNum]		 	= 30*60;
				alfNurseObservationProcessTime[iCurrentNurseNum] 	= 30;
				alfNurseTriageTime[iCurrentNurseNum] 				= 30*60;
				alfNurseExperienceRate1[iCurrentNurseNum]			= pplfParameter[14][i];
				alfNurseExperienceRate2[iCurrentNurseNum]			= pplfParameter[15][i];
				alfNurseConExperienceAIS[iCurrentNurseNum]			= pplfParameter[16][i];
				alfNurseExperienceRateAIS1[iCurrentNurseNum]		= pplfParameter[17][i];
				alfNurseExperienceRateAIS2[iCurrentNurseNum]		= pplfParameter[18][i];
				aiNurseRoomNumber[iCurrentNurseNum]			 		= (int)pplfParameter[19][i];
				iCurrentNurseNum++;
			}
			else if( pplfParameter[0][i] == 3.0 )
			{

			}
			else
			{

			}
		}

		if( iOperationDoctorNum > iCurrentDoctorNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iOperationDoctorNum-iCurrentDoctorNum; i < iOperationDoctorNum; i++ )
			{
				iLocation = i % iCurrentDoctorNum + iStartDoctorLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				alfDoctorYearExperience[i] 		= pplfParameter[1][iLocation];
				alfDoctorConExperience[i] 		= pplfParameter[2][iLocation];
				alfDoctorConTired1[i] 			= pplfParameter[3][iLocation];
				alfDoctorConTired2[i] 			= pplfParameter[4][iLocation];
				alfDoctorConTired3[i] 			= pplfParameter[5][iLocation];
				alfDoctorConTired4[i] 			= pplfParameter[6][iLocation];
				alfDoctorTiredRate[i] 			= pplfParameter[7][iLocation];
				alfDoctorRevisedOperationRate[i]= pplfParameter[8][iLocation];
				alfDoctorAssociationRate[i]	 	= pplfParameter[9][iLocation];
				aiDoctorDepartment[i]			= (int)pplfParameter[10][iLocation];
				alfDoctorConsultationTime[i]	= 30*60;
				alfDoctorOperationTime[i]		= 30;
				alfDoctorEmergencyTime[i]		= 30*60;
				alfDoctorExperienceRate1[i]		= pplfParameter[11][iLocation];
				alfDoctorExperienceRate2[i]		= pplfParameter[12][iLocation];
				alfDoctorConExperienceAIS[i]	= pplfParameter[13][iLocation];
				alfDoctorExperienceRateAIS1[i]	= pplfParameter[14][iLocation];
				alfDoctorExperienceRateAIS2[i]	= pplfParameter[15][iLocation];
				aiDoctorRoomNumber[i]			= (int)pplfParameter[16][iLocation];
			}
		}
		if( iOperationNurseNum > iCurrentNurseNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iOperationNurseNum-iCurrentNurseNum; i < iOperationNurseNum; i++ )
			{
				iLocation = i % iCurrentNurseNum + iStartNurseLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				aiNurseCategory[i] 					= (int)pplfParameter[1][iLocation];
				aiNurseTriageProtocol[i] 			= (int)pplfParameter[2][iLocation];
				aiNurseTriageLevel[i] 				= (int)pplfParameter[3][iLocation];
				alfNurseTriageYearExperience[i]	 	= pplfParameter[4][iLocation];
				alfNurseYearExperience[i] 			= pplfParameter[5][iLocation];
				alfNurseConExperience[i] 			= pplfParameter[6][iLocation];
				alfNurseConTired1[i]			 	= pplfParameter[7][iLocation];
				alfNurseConTired2[i]			 	= pplfParameter[8][iLocation];
				alfNurseConTired3[i]				= pplfParameter[9][iLocation];
				alfNurseConTired4[i]			 	= pplfParameter[10][iLocation];
				alfNurseTiredRate[i]				= pplfParameter[11][iLocation];
				alfNurseAssociationRate[i]		 	= pplfParameter[12][iLocation];
				aiNurseDepartment[i]			 	= (int)pplfParameter[13][iLocation];
				alfNurseObservationTime[i]		 	= 30*60;
				alfNurseObservationProcessTime[i] 	= 30;
				alfNurseTriageTime[i] 				= 30*60;
				alfNurseExperienceRate1[i]			= pplfParameter[14][iLocation];
				alfNurseExperienceRate2[i]			= pplfParameter[15][iLocation];
				alfNurseConExperienceAIS[i]			= pplfParameter[16][iLocation];
				alfNurseExperienceRateAIS1[i]		= pplfParameter[17][iLocation];
				alfNurseExperienceRateAIS2[i]		= pplfParameter[18][iLocation];
				aiNurseRoomNumber[i]				= (int)pplfParameter[19][iLocation];
			}
		}

		for( i = 0;i < iOperationRoomNum; i++ )
		{
			ArrayListOperationRooms.add( new EROperationRoom() );
			ArrayListOperationRooms.get(i).vSetSimulationEngine( engine );
			ArrayListOperationRooms.get(i).vSetRandom( sfmtRandom );

			ArrayListOperationRooms.get(i).vCreateDoctorAgents( iOperationDoctorNum );
			ArrayListOperationRooms.get(i).vSetDoctorsRandom();
			ArrayListOperationRooms.get(i).vSetDoctorAgentParameter( alfDoctorYearExperience,
					alfDoctorConExperience,
					alfDoctorExperienceRate1,
					alfDoctorExperienceRate2,
					alfDoctorConExperienceAIS,
					alfDoctorExperienceRateAIS1,
					alfDoctorExperienceRateAIS2,
					alfDoctorConTired1,
					alfDoctorConTired2,
					alfDoctorConTired3,
					alfDoctorConTired4,
					alfDoctorTiredRate,
					alfDoctorRevisedOperationRate,
					alfDoctorAssociationRate,
					alfDoctorConsultationTime,
					alfDoctorOperationTime,
					alfDoctorEmergencyTime,
					aiDoctorDepartment,
					aiDoctorRoomNumber );

			for( j = 0; j < ArrayListOperationRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListOperationRooms.get(i).cGetDoctorAgent(j).vSetSimulationEngine( engine );
				ArrayListOperationRooms.get(i).cGetDoctorAgent(j).vSetSimulationEndTime( lfEndTime );
			}
			ArrayListOperationRooms.get(i).vCreateNurseAgents( iOperationNurseNum );
			ArrayListOperationRooms.get(i).vSetNursesRandom();
			ArrayListOperationRooms.get(i).vSetNurseAgentParameter(aiNurseCategory,
					  aiNurseTriageProtocol,
					  aiNurseTriageLevel,
					  alfNurseTriageYearExperience,
					  alfNurseYearExperience,
					  alfNurseConExperience,
					  alfNurseExperienceRate1,
					  alfNurseExperienceRate2,
					  alfNurseConExperienceAIS,
					  alfNurseExperienceRateAIS1,
					  alfNurseExperienceRateAIS2,
					  alfNurseConTired1,
					  alfNurseConTired2,
					  alfNurseConTired3,
					  alfNurseConTired4,
					  alfNurseTiredRate,
					  alfNurseAssociationRate,
					  alfNurseObservationTime,
					  alfNurseObservationProcessTime,
					  alfNurseTriageTime,
					  aiNurseDepartment,
					  aiNurseRoomNumber );
			for( j = 0; j < ArrayListOperationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListOperationRooms.get(i).cGetNurseAgent(j).vSetSimulationEngine( engine );
				ArrayListOperationRooms.get(i).cGetNurseAgent(j).vSetSimulationEndTime( lfEndTime );
			}
		}
	}

	/**
	 * <PRE>
	 *   初療室数及び構成する医師の数、看護師の数、医療技師の数を設定します。
	 * </PRE>
	 *
	 * @param iEmergencyRoomNum					初療室エージェント数
	 * @param strPath							初療室に所属する医師、看護師、医療技師のパラメータが記述されたファイルパス
	 * @param engine							FUSEエンジン
	 * @param sfmtRandom 						メルセンヌツイスターのインスタンス
	 * @throws IOException						ファイル処理中にエラーが発生した場合の例外
	 * @throws ERClinicalEngineerAgentException 医療技師の処理中でエラーが発生した場合の例外
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	private void vSetEmergencyRooms(int iEmergencyRoomNum, String strPath, SimulationEngine engine, utility.sfmt.Rand sfmtRandom ) throws IOException, ERClinicalEngineerAgentException
	{
		int i,j;
		int iCurrentDoctorNum = 0;
		int iCurrentNurseNum = 0;
		int iCurrentClinicalEngineerNum = 0;
		int iStartDoctorLoc = 0;
		int iStartNurseLoc = 0;
		int iStartClinicalEngineerLoc = 0;
		int iLocation = 0;

		int[] aiNurseCategory;
		int[] aiNurseTriageProtocol;
		int[] aiNurseTriageLevel;
		double[] alfNurseTriageYearExperience;
		double[] alfNurseYearExperience;
		double[] alfNurseConExperience;
		double[] alfNurseExperienceRate1;
		double[] alfNurseExperienceRate2;;
		double[] alfNurseConExperienceAIS;
		double[] alfNurseExperienceRateAIS1;
		double[] alfNurseExperienceRateAIS2;
		double[] alfNurseConTired1;
		double[] alfNurseConTired2;
		double[] alfNurseConTired3;
		double[] alfNurseConTired4;
		double[] alfNurseTiredRate;
		double[] alfNurseAssociationRate;
		double[] alfNurseObservationTime;
		double[] alfNurseObservationProcessTime;
		double[] alfNurseTriageTime;
		int[] aiNurseDepartment;
		int[] aiNurseRoomNumber;

		double[] alfDoctorYearExperience;
		double[] alfDoctorConExperience;
		double[] alfDoctorExperienceRate1;
		double[] alfDoctorExperienceRate2;;
		double[] alfDoctorConExperienceAIS;
		double[] alfDoctorExperienceRateAIS1;
		double[] alfDoctorExperienceRateAIS2;
		double[] alfDoctorConTired1;
		double[] alfDoctorConTired2;
		double[] alfDoctorConTired3;
		double[] alfDoctorConTired4;
		double[] alfDoctorTiredRate;
		double[] alfDoctorRevisedOperationRate;
		double[] alfDoctorAssociationRate;
		double[] alfDoctorConsultationTime;
		double[] alfDoctorOperationTime;
		double[] alfDoctorEmergencyTime;
		int[] aiDoctorDepartment;
		int[] aiDoctorRoomNumber;

		double[] alfClinicalEngineerYearExperience;
		double[] alfClinicalEngineerConExperience;
		double[] alfClinicalEngineerExperienceRate1;
		double[] alfClinicalEngineerExperienceRate2;;
		double[] alfClinicalEngineerConExperienceAIS;
		double[] alfClinicalEngineerExperienceRateAIS1;
		double[] alfClinicalEngineerExperienceRateAIS2;
		double[] alfClinicalEngineerConTired1;
		double[] alfClinicalEngineerConTired2;
		double[] alfClinicalEngineerConTired3;
		double[] alfClinicalEngineerConTired4;
		double[] alfClinicalEngineerTiredRate;
		double[] alfClinicalEngineerAssociationRate;
		double[] alfClinicalEngineerExaminationTime;
		int[] aiClinicalEngineerDepartment;
		int[] aiClinicalEngineerRoomNumber;

		int iRow = 0;
		int iColumn = 0;
		double[][] pplfParameter;

		CCsv csv = new CCsv();
		if( strPath == "" )
		{
			csv.vOpen( "./parameter/初療室.csv", "read" );
		}
		else
		{
			csv.vOpen( strPath, "read" );
		}
		csv.vGetRowColumn();

		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		pplfParameter = new double[iColumn][iRow];
		csv.vRead( pplfParameter );
		csv.vClose();
		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			// 医師数、看護師数、医療技師数を設定します。
			if( pplfParameter[0].length > 2 )
			{
				iEmergencyDoctorNum = 0;
				iEmergencyNurseNum = 0;
				iEmergencyClinicalEngineerNum = 0;
				for( i = 0;i < pplfParameter[0].length; i++ )
				{
					if( pplfParameter[0][i] == 1.0 )
					{
						iEmergencyDoctorNum++;
					}
					else if( pplfParameter[0][i] == 2.0 )
					{
						iEmergencyNurseNum++;
					}
					else if( pplfParameter[0][i] == 3.0 )
					{
						iEmergencyClinicalEngineerNum++;
					}
				}
			}
		}
		// 逆シミュレーションモードの場合
		else
		{
			// 部屋数が0以下の場合はすべて0にして終了する。
			if( this.iEmergencyRoomNum <= 0 )
			{
				this.iEmergencyRoomNum = 0;
				iEmergencyNurseNum = 0;
				iEmergencyDoctorNum = 0;
				iEmergencyClinicalEngineerNum = 0;
				ArrayListEmergencyRooms = new ArrayList<EREmergencyRoom>();
				return ;
			}
		}

		alfNurseYearExperience			= new double[iEmergencyNurseNum];
		alfNurseConExperience			= new double[iEmergencyNurseNum];
		alfNurseExperienceRate1			= new double[iEmergencyNurseNum];
		alfNurseExperienceRate2			= new double[iEmergencyNurseNum];
		alfNurseConExperienceAIS		= new double[iEmergencyNurseNum];
		alfNurseExperienceRateAIS1		= new double[iEmergencyNurseNum];
		alfNurseExperienceRateAIS2		= new double[iEmergencyNurseNum];
		alfNurseConTired1				= new double[iEmergencyNurseNum];
		alfNurseConTired2				= new double[iEmergencyNurseNum];
		alfNurseConTired3				= new double[iEmergencyNurseNum];
		alfNurseConTired4				= new double[iEmergencyNurseNum];
		alfNurseTiredRate				= new double[iEmergencyNurseNum];
		alfNurseAssociationRate			= new double[iEmergencyNurseNum];
		alfNurseObservationTime			= new double[iEmergencyNurseNum];
		alfNurseObservationProcessTime	= new double[iEmergencyNurseNum];
		alfNurseTriageTime				= new double[iEmergencyNurseNum];
		alfNurseAssociationRate			= new double[iEmergencyNurseNum];
		alfNurseTriageYearExperience	= new double[iEmergencyNurseNum];

		aiNurseTriageProtocol			= new int[iEmergencyNurseNum];
		aiNurseTriageLevel				= new int[iEmergencyNurseNum];
		aiNurseDepartment				= new int[iEmergencyNurseNum];
		aiNurseCategory					= new int[iEmergencyNurseNum];
		aiNurseRoomNumber				= new int[iEmergencyNurseNum];


		if( ArrayListEmergencyRooms == null )
		{
			ArrayListEmergencyRooms = new ArrayList<EREmergencyRoom>();
		}

//		for( i = 0;i < iNurseNum; i++ )
//		{
//			alfNurseConExperience[i] = 3;
//			alfNurseConTired[i] = 3;
//			alfNurseObservationTime[i] = 30*60;
//			alfNurseObservationProcessTime[i] = 30;
//			alfNurseTriageTime[i] = 30*60;
//			aiNurseProtocol[i] = 1;
//			aiNurseLevel[i] = 5;
//			aiNurseDepartment[i] = 1;
//		}

		for( i = 0;i < iEmergencyNurseNum; i++ )
		{
			alfNurseConExperience[i]			= 3;
			alfNurseExperienceRate1[i]			= 2.1;
			alfNurseExperienceRate2[i]			= 0.9;
			alfNurseConExperienceAIS[i]			= 0.14;
			alfNurseExperienceRateAIS1[i]		= 0.2;
			alfNurseExperienceRateAIS2[i]		= 1.1;
			aiNurseRoomNumber[i] 				= 1;
			alfNurseConTired1[i]				= 3;
			alfNurseConTired2[i]				= 3;
			alfNurseConTired3[i]				= 3;
			alfNurseConTired4[i]				= 3;
			alfNurseTiredRate[i]				= 3;
			alfNurseObservationTime[i]			= 30*60;
			alfNurseObservationProcessTime[i]	= 30;
			alfNurseTriageTime[i] 				= 30*60;
			aiNurseTriageProtocol[i] 			= 1;
			aiNurseTriageLevel[i] 				= 5;
			alfNurseAssociationRate[i]		 	= 1.0;
			aiNurseDepartment[i] 				= 3;
			alfNurseExperienceRate1[i]			= 2.1;
			alfNurseExperienceRate2[i]			= 0.9;
			alfNurseConExperienceAIS[i]			= 0.14;
			alfNurseExperienceRateAIS1[i]		= 0.2;
			alfNurseExperienceRateAIS2[i]		= 1.1;
			aiNurseRoomNumber[i] 				= 1;
		}

		alfDoctorYearExperience					= new double[iEmergencyDoctorNum];
		alfDoctorConExperience					= new double[iEmergencyDoctorNum];
		alfDoctorExperienceRate1				= new double[iEmergencyDoctorNum];
		alfDoctorExperienceRate2				= new double[iEmergencyDoctorNum];
		alfDoctorConExperienceAIS				= new double[iEmergencyDoctorNum];
		alfDoctorExperienceRateAIS1				= new double[iEmergencyDoctorNum];
		alfDoctorExperienceRateAIS2				= new double[iEmergencyDoctorNum];
		alfDoctorConTired1 						= new double[iEmergencyDoctorNum];
		alfDoctorConTired2 						= new double[iEmergencyDoctorNum];
		alfDoctorConTired3 						= new double[iEmergencyDoctorNum];
		alfDoctorConTired4 						= new double[iEmergencyDoctorNum];
		alfDoctorTiredRate						= new double[iEmergencyDoctorNum];
		alfDoctorRevisedOperationRate			= new double[iEmergencyDoctorNum];
		alfDoctorAssociationRate				= new double[iEmergencyDoctorNum];
		alfDoctorConsultationTime				= new double[iEmergencyDoctorNum];
		alfDoctorOperationTime					= new double[iEmergencyDoctorNum];
		alfDoctorEmergencyTime					= new double[iEmergencyDoctorNum];
		aiDoctorDepartment						= new int[iEmergencyDoctorNum];
		aiDoctorRoomNumber						= new int[iEmergencyDoctorNum];
		for( i = 0;i < iEmergencyDoctorNum; i++ )
		{
			alfDoctorYearExperience[i] 			= 10;
			alfDoctorConExperience[i] 			= 3;
			alfDoctorConTired1[i] 				= 3;
			alfDoctorConTired2[i] 				= 3;
			alfDoctorConTired3[i] 				= 3;
			alfDoctorConTired4[i] 				= 3;
			alfDoctorTiredRate[i] 				= 3.0;
			alfDoctorRevisedOperationRate[i]	= 0.666666666666;
			alfDoctorAssociationRate[i]	 		= 1.0;
			alfDoctorConsultationTime[i] 		= 30*60;
			alfDoctorOperationTime[i] 			= 30;
			alfDoctorEmergencyTime[i] 			= 30*60;
			aiDoctorDepartment[i] 				= 3;
			alfDoctorExperienceRate1[i]			= 2.1;
			alfDoctorExperienceRate2[i]			= 0.9;
			alfDoctorConExperienceAIS[i]		= 0.14;
			alfDoctorExperienceRateAIS1[i]		= 0.2;
			alfDoctorExperienceRateAIS2[i]		= 1.1;
			aiDoctorRoomNumber[i] 				= 1;
		}

		alfClinicalEngineerYearExperience			= new double[iEmergencyClinicalEngineerNum];
		alfClinicalEngineerConExperience			= new double[iEmergencyClinicalEngineerNum];
		alfClinicalEngineerExperienceRate1			= new double[iEmergencyClinicalEngineerNum];
		alfClinicalEngineerExperienceRate2			= new double[iEmergencyClinicalEngineerNum];
		alfClinicalEngineerConExperienceAIS			= new double[iEmergencyClinicalEngineerNum];
		alfClinicalEngineerExperienceRateAIS1		= new double[iEmergencyClinicalEngineerNum];
		alfClinicalEngineerExperienceRateAIS2		= new double[iEmergencyClinicalEngineerNum];
		alfClinicalEngineerConTired1				= new double[iEmergencyClinicalEngineerNum];
		alfClinicalEngineerConTired2				= new double[iEmergencyClinicalEngineerNum];
		alfClinicalEngineerConTired3				= new double[iEmergencyClinicalEngineerNum];
		alfClinicalEngineerConTired4				= new double[iEmergencyClinicalEngineerNum];
		alfClinicalEngineerTiredRate				= new double[iEmergencyClinicalEngineerNum];
		alfClinicalEngineerAssociationRate			= new double[iEmergencyClinicalEngineerNum];
		alfClinicalEngineerAssociationRate			= new double[iEmergencyClinicalEngineerNum];
		aiClinicalEngineerDepartment				= new int[iEmergencyClinicalEngineerNum];
		aiClinicalEngineerRoomNumber				= new int[iEmergencyClinicalEngineerNum];
		alfClinicalEngineerExaminationTime			= new double[iEmergencyClinicalEngineerNum];
		for( i = 0;i < iEmergencyClinicalEngineerNum; i++ )
		{
			alfClinicalEngineerYearExperience[i] 			= 10;
			alfClinicalEngineerConExperience[i] 			= 3;
			alfClinicalEngineerConTired1[i] 				= 3;
			alfClinicalEngineerConTired2[i] 				= 3;
			alfClinicalEngineerConTired3[i] 				= 3;
			alfClinicalEngineerConTired4[i] 				= 3;
			alfClinicalEngineerTiredRate[i] 				= 3.0;
			alfClinicalEngineerAssociationRate[i]	 		= 1.0;
			alfClinicalEngineerExaminationTime[i]			= 30.0;
			alfClinicalEngineerExperienceRate1[i]			= 2.1;
			alfClinicalEngineerExperienceRate2[i]			= 0.9;
			alfClinicalEngineerConExperienceAIS[i]			= 0.14;
			alfClinicalEngineerExperienceRateAIS1[i]		= 0.2;
			alfClinicalEngineerExperienceRateAIS2[i]		= 1.1;
			aiClinicalEngineerRoomNumber[i] 				= 1;
		}

		for( i = 0;i < pplfParameter[0].length; i++ )
		{
			if( pplfParameter[0][i] == 1.0 )
			{
				if( iEmergencyDoctorNum <= iCurrentDoctorNum ) continue;
				if( iStartDoctorLoc == 0 ) iStartDoctorLoc = i;
				alfDoctorYearExperience[iCurrentDoctorNum] 		= pplfParameter[1][i];
				alfDoctorConExperience[iCurrentDoctorNum] 		= pplfParameter[2][i];
				alfDoctorConTired1[iCurrentDoctorNum] 			= pplfParameter[3][i];
				alfDoctorConTired2[iCurrentDoctorNum] 			= pplfParameter[4][i];
				alfDoctorConTired3[iCurrentDoctorNum] 			= pplfParameter[5][i];
				alfDoctorConTired4[iCurrentDoctorNum] 			= pplfParameter[6][i];
				alfDoctorTiredRate[iCurrentDoctorNum] 			= pplfParameter[7][i];
				alfDoctorRevisedOperationRate[iCurrentDoctorNum]= pplfParameter[8][i];
				alfDoctorAssociationRate[iCurrentDoctorNum]	 	= pplfParameter[9][i];
				aiDoctorDepartment[iCurrentDoctorNum]			= (int)pplfParameter[10][i];
				alfDoctorConsultationTime[iCurrentDoctorNum]	= 30*60;
				alfDoctorOperationTime[iCurrentDoctorNum]		= 30;
				alfDoctorEmergencyTime[iCurrentDoctorNum]		= 30*60;
				alfDoctorExperienceRate1[iCurrentDoctorNum]		= pplfParameter[11][i];
				alfDoctorExperienceRate2[iCurrentDoctorNum]		= pplfParameter[12][i];
				alfDoctorConExperienceAIS[iCurrentDoctorNum]	= pplfParameter[13][i];
				alfDoctorExperienceRateAIS1[iCurrentDoctorNum]	= pplfParameter[14][i];
				alfDoctorExperienceRateAIS2[iCurrentDoctorNum]	= pplfParameter[15][i];
				aiDoctorRoomNumber[iCurrentDoctorNum]			= (int)pplfParameter[16][i];
				iCurrentDoctorNum++;
			}
			else if( pplfParameter[0][i] == 2.0 )
			{
				if( iEmergencyNurseNum <= iCurrentNurseNum ) continue;
				if( iStartNurseLoc == 0 ) iStartNurseLoc = i;
				aiNurseCategory[iCurrentNurseNum] 					= (int)pplfParameter[1][i];
				aiNurseTriageProtocol[iCurrentNurseNum] 			= (int)pplfParameter[2][i];
				aiNurseTriageLevel[iCurrentNurseNum] 				= (int)pplfParameter[3][i];
				alfNurseTriageYearExperience[iCurrentNurseNum] 		= pplfParameter[4][i];
				alfNurseYearExperience[iCurrentNurseNum] 			= pplfParameter[5][i];
				alfNurseConExperience[iCurrentNurseNum] 			= pplfParameter[6][i];
				alfNurseConTired1[iCurrentNurseNum]			 		= pplfParameter[7][i];
				alfNurseConTired2[iCurrentNurseNum]			 		= pplfParameter[8][i];
				alfNurseConTired3[iCurrentNurseNum]			 		= pplfParameter[9][i];
				alfNurseConTired4[iCurrentNurseNum]			 		= pplfParameter[10][i];
				alfNurseTiredRate[iCurrentNurseNum]					= pplfParameter[11][i];
				alfNurseAssociationRate[iCurrentNurseNum]		 	= pplfParameter[12][i];
				aiNurseDepartment[iCurrentNurseNum]			 		= (int)pplfParameter[13][i];
				alfNurseExperienceRate1[iCurrentNurseNum]			= pplfParameter[14][i];
				alfNurseExperienceRate2[iCurrentNurseNum]			= pplfParameter[15][i];
				alfNurseConExperienceAIS[iCurrentNurseNum]			= pplfParameter[16][i];
				alfNurseExperienceRateAIS1[iCurrentNurseNum]		= pplfParameter[17][i];
				alfNurseExperienceRateAIS2[iCurrentNurseNum]		= pplfParameter[18][i];
				aiNurseRoomNumber[iCurrentNurseNum]			 		= (int)pplfParameter[19][i];
				alfNurseObservationTime[iCurrentNurseNum]		 	= 30*60;
				alfNurseObservationProcessTime[iCurrentNurseNum] 	= 30;
				alfNurseTriageTime[iCurrentNurseNum] 				= 30*60;
				iCurrentNurseNum++;
			}
			else if( pplfParameter[0][i] == 3.0 )
			{
				if( iEmergencyClinicalEngineerNum <= iCurrentClinicalEngineerNum ) continue;
				if( iStartClinicalEngineerLoc == 0 ) iStartClinicalEngineerLoc = i;
				alfClinicalEngineerYearExperience[iCurrentClinicalEngineerNum] 				= pplfParameter[1][i];
				alfClinicalEngineerConExperience[iCurrentClinicalEngineerNum] 				= pplfParameter[2][i];
				alfClinicalEngineerConTired1[iCurrentClinicalEngineerNum]			 		= pplfParameter[3][i];
				alfClinicalEngineerConTired2[iCurrentClinicalEngineerNum]			 		= pplfParameter[4][i];
				alfClinicalEngineerConTired3[iCurrentClinicalEngineerNum]			 		= pplfParameter[5][i];
				alfClinicalEngineerConTired4[iCurrentClinicalEngineerNum]			 		= pplfParameter[6][i];
				alfClinicalEngineerTiredRate[iCurrentClinicalEngineerNum]					= pplfParameter[7][i];
				alfClinicalEngineerAssociationRate[iCurrentClinicalEngineerNum]			 	= pplfParameter[8][i];
				aiClinicalEngineerDepartment[iCurrentClinicalEngineerNum]			 		= (int)pplfParameter[9][i];
				alfClinicalEngineerExperienceRate1[iCurrentClinicalEngineerNum]				= pplfParameter[10][i];
				alfClinicalEngineerExperienceRate2[iCurrentClinicalEngineerNum]				= pplfParameter[11][i];
				alfClinicalEngineerConExperienceAIS[iCurrentClinicalEngineerNum]			= pplfParameter[12][i];
				alfClinicalEngineerExperienceRateAIS1[iCurrentClinicalEngineerNum]			= pplfParameter[13][i];
				alfClinicalEngineerExperienceRateAIS2[iCurrentClinicalEngineerNum]			= pplfParameter[14][i];
				aiClinicalEngineerRoomNumber[iCurrentClinicalEngineerNum]			 		= (int)pplfParameter[15][i];
//				alfNurseObservationTime[iCurrentClinicalEngineerNum]		 				= 30*60;
//				alfNurseObservationProcessTime[iCurrentClinicalEngineerNum] 				= 30;
//				alfNurseTriageTime[iCurrentClinicalEngineerNum] 							= 30*60;
				iCurrentClinicalEngineerNum++;
			}
			else
			{

			}
		}

		if( iEmergencyDoctorNum > iCurrentDoctorNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iEmergencyDoctorNum-iCurrentDoctorNum; i < iEmergencyDoctorNum; i++ )
			{
				iLocation = i % iCurrentDoctorNum + iStartDoctorLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				alfDoctorYearExperience[i] 		= pplfParameter[1][iLocation];
				alfDoctorConExperience[i] 		= pplfParameter[2][iLocation];
				alfDoctorConTired1[i] 			= pplfParameter[3][iLocation];
				alfDoctorConTired2[i] 			= pplfParameter[4][iLocation];
				alfDoctorConTired3[i] 			= pplfParameter[5][iLocation];
				alfDoctorConTired4[i] 			= pplfParameter[6][iLocation];
				alfDoctorTiredRate[i] 			= pplfParameter[7][iLocation];
				alfDoctorRevisedOperationRate[i]= pplfParameter[8][iLocation];
				alfDoctorAssociationRate[i]	 	= pplfParameter[9][iLocation];
				aiDoctorDepartment[i]			= (int)pplfParameter[10][iLocation];
				alfDoctorConsultationTime[i]	= 30*60;
				alfDoctorOperationTime[i]		= 30;
				alfDoctorEmergencyTime[i]		= 30*60;
				alfDoctorExperienceRate1[i]		= pplfParameter[11][iLocation];
				alfDoctorExperienceRate2[i]		= pplfParameter[12][iLocation];
				alfDoctorConExperienceAIS[i]	= pplfParameter[13][iLocation];
				alfDoctorExperienceRateAIS1[i]	= pplfParameter[14][iLocation];
				alfDoctorExperienceRateAIS2[i]	= pplfParameter[15][iLocation];
				aiDoctorRoomNumber[i]			= (int)pplfParameter[16][iLocation];
			}
		}
		if( iEmergencyNurseNum > iCurrentNurseNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iEmergencyNurseNum-iCurrentNurseNum; i < iEmergencyNurseNum; i++ )
			{
				iLocation = i % iCurrentNurseNum + iStartNurseLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				aiNurseCategory[i] 					= (int)pplfParameter[1][iLocation];
				aiNurseTriageProtocol[i] 			= (int)pplfParameter[2][iLocation];
				aiNurseTriageLevel[i] 				= (int)pplfParameter[3][iLocation];
				alfNurseTriageYearExperience[i]	 	= pplfParameter[4][iLocation];
				alfNurseYearExperience[i] 			= pplfParameter[5][iLocation];
				alfNurseConExperience[i] 			= pplfParameter[6][iLocation];
				alfNurseConTired1[i]			 	= pplfParameter[7][iLocation];
				alfNurseConTired2[i]			 	= pplfParameter[8][iLocation];
				alfNurseConTired3[i]				= pplfParameter[9][iLocation];
				alfNurseConTired4[i]			 	= pplfParameter[10][iLocation];
				alfNurseTiredRate[i]				= pplfParameter[11][iLocation];
				alfNurseAssociationRate[i]		 	= pplfParameter[12][iLocation];
				aiNurseDepartment[i]			 	= (int)pplfParameter[13][iLocation];
				alfNurseObservationTime[i]		 	= 30*60;
				alfNurseObservationProcessTime[i] 	= 30;
				alfNurseTriageTime[i] 				= 30*60;
				alfNurseExperienceRate1[i]			= pplfParameter[14][iLocation];
				alfNurseExperienceRate2[i]			= pplfParameter[15][iLocation];
				alfNurseConExperienceAIS[i]			= pplfParameter[16][iLocation];
				alfNurseExperienceRateAIS1[i]		= pplfParameter[17][iLocation];
				alfNurseExperienceRateAIS2[i]		= pplfParameter[18][iLocation];
				aiNurseRoomNumber[i]				= (int)pplfParameter[19][iLocation];
			}
		}
		if( iEmergencyClinicalEngineerNum > iCurrentClinicalEngineerNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iEmergencyClinicalEngineerNum-iCurrentClinicalEngineerNum; i < iEmergencyClinicalEngineerNum; i++ )
			{
				iLocation = i % iCurrentClinicalEngineerNum + iStartClinicalEngineerLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				alfClinicalEngineerYearExperience[i] 			= pplfParameter[1][iLocation];
				alfClinicalEngineerConExperience[i] 			= pplfParameter[2][iLocation];
				alfClinicalEngineerConTired1[i]			 		= pplfParameter[3][iLocation];
				alfClinicalEngineerConTired2[i]			 		= pplfParameter[4][iLocation];
				alfClinicalEngineerConTired3[i]			 		= pplfParameter[5][iLocation];
				alfClinicalEngineerConTired4[i]			 		= pplfParameter[6][iLocation];
				alfClinicalEngineerTiredRate[i]					= pplfParameter[7][iLocation];
				alfClinicalEngineerAssociationRate[i]		 	= pplfParameter[8][iLocation];
				aiClinicalEngineerDepartment[i]			 		= (int)pplfParameter[9][iLocation];
				alfClinicalEngineerExperienceRate1[i]			= pplfParameter[10][iLocation];
				alfClinicalEngineerExperienceRate2[i]			= pplfParameter[11][iLocation];
				alfClinicalEngineerConExperienceAIS[i]			= pplfParameter[12][iLocation];
				alfClinicalEngineerExperienceRateAIS1[i]		= pplfParameter[13][iLocation];
				alfClinicalEngineerExperienceRateAIS2[i]		= pplfParameter[14][iLocation];
				aiClinicalEngineerRoomNumber[i]			 		= (int)pplfParameter[15][iLocation];
			}
		}

		for( i = 0;i < iEmergencyRoomNum; i++ )
		{
			ArrayListEmergencyRooms.add( new EREmergencyRoom() );
			ArrayListEmergencyRooms.get(i).vSetSimulationEngine( engine );
			ArrayListEmergencyRooms.get(i).vSetRandom( sfmtRandom );

			// 医師エージェントの作成
			ArrayListEmergencyRooms.get(i).vCreateDoctorAgents( iEmergencyDoctorNum );
			ArrayListEmergencyRooms.get(i).vSetDoctorsRandom();
			ArrayListEmergencyRooms.get(i).vSetDoctorAgentParameter( alfDoctorYearExperience,
					alfDoctorConExperience,
					alfDoctorExperienceRate1,
					alfDoctorExperienceRate2,
					alfDoctorConExperienceAIS,
					alfDoctorExperienceRateAIS1,
					alfDoctorExperienceRateAIS2,
					alfDoctorConTired1,
					alfDoctorConTired2,
					alfDoctorConTired3,
					alfDoctorConTired4,
					alfDoctorTiredRate,
					alfDoctorRevisedOperationRate,
					alfDoctorAssociationRate,
					alfDoctorConsultationTime,
					alfDoctorOperationTime,
					alfDoctorEmergencyTime,
					aiDoctorDepartment,
					aiDoctorRoomNumber );
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetDoctorAgent(j).vSetSimulationEngine( engine );
				ArrayListEmergencyRooms.get(i).cGetDoctorAgent(j).vSetSimulationEndTime( lfEndTime );
			}

			// 看護師エージェントの作成
			ArrayListEmergencyRooms.get(i).vCreateNurseAgents( iEmergencyNurseNum );
			ArrayListEmergencyRooms.get(i).vSetNursesRandom();
			ArrayListEmergencyRooms.get(i).vSetNurseAgentParameter(aiNurseCategory,
					  aiNurseTriageProtocol,
					  aiNurseTriageLevel,
					  alfNurseTriageYearExperience,
					  alfNurseYearExperience,
					  alfNurseConExperience,
					  alfNurseExperienceRate1,
					  alfNurseExperienceRate2,
					  alfNurseConExperienceAIS,
					  alfNurseExperienceRateAIS1,
					  alfNurseExperienceRateAIS2,
					  alfNurseConTired1,
					  alfNurseConTired2,
					  alfNurseConTired3,
					  alfNurseConTired4,
					  alfNurseTiredRate,
					  alfNurseAssociationRate,
					  alfNurseObservationTime,
					  alfNurseObservationProcessTime,
					  alfNurseTriageTime,
					  aiNurseDepartment,
					  aiNurseRoomNumber );
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetNurseAgent(j).vSetSimulationEngine( engine );
				ArrayListEmergencyRooms.get(i).cGetNurseAgent(j).vSetSimulationEndTime( lfEndTime );
			}

			// 医療技師エージェントの作成
			ArrayListEmergencyRooms.get(i).vCreateClinicalEngineerAgents( iEmergencyClinicalEngineerNum );
			ArrayListEmergencyRooms.get(i).vSetClinicalEngineersRandom();
			ArrayListEmergencyRooms.get(i).vSetClinicalEngineerAgentParameter( alfClinicalEngineerYearExperience,
					alfClinicalEngineerConExperience,
					alfClinicalEngineerExperienceRate1,
					alfClinicalEngineerExperienceRate2,
					alfClinicalEngineerConExperienceAIS,
					alfClinicalEngineerExperienceRateAIS1,
					alfClinicalEngineerExperienceRateAIS2,
					alfClinicalEngineerConTired1,
					alfClinicalEngineerConTired2,
					alfClinicalEngineerConTired3,
					alfClinicalEngineerConTired4,
					alfClinicalEngineerTiredRate,
					alfClinicalEngineerAssociationRate,
					alfClinicalEngineerExaminationTime,
					aiClinicalEngineerDepartment,
					aiClinicalEngineerRoomNumber );
			for( j = 0; j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgent(j).vSetSimulationEngine( engine );
				ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgent(j).vSetSimulationEndTime( lfEndTime );
			}
		}
	}

	/**
	 * <PRE>
	 *   観察室数及び構成する看護師の数を設定します。
	 * </PRE>
	 *
	 * @param iObservationRoomNum		観察室エージェント数
	 * @param strPath					観察室に所属する看護師のパラメータが記述されたファイルパス
	 * @param engine					FUSEエンジン
	 * @param sfmtRandom				メルセンヌツイスターのインスタンス
	 * @throws IOException				ファイル処理中の例外
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	private void vSetObservationRooms(int iObservationRoomNum, String strPath, SimulationEngine engine, utility.sfmt.Rand sfmtRandom ) throws IOException
	{
		int i,j;
		int iCurrentNurseNum = 0;
		int iStartNurseLoc = 0;
		int iLocation = 0;

		int[] aiNurseCategory;
		int[] aiNurseTriageProtocol;
		int[] aiNurseTriageLevel;
		double[] alfNurseTriageYearExperience;
		double[] alfNurseYearExperience;
		double[] alfNurseConExperience;
		double[] alfNurseConTired1;
		double[] alfNurseConTired2;
		double[] alfNurseConTired3;
		double[] alfNurseConTired4;
		double[] alfNurseTiredRate;
		double[] alfNurseAssociationRate;
		double[] alfNurseObservationTime;
		double[] alfNurseObservationProcessTime;
		double[] alfNurseTriageTime;
		int[] aiNurseDepartment;
		double[] alfNurseExperienceRate1;
		double[] alfNurseExperienceRate2;
		double[] alfNurseConExperienceAIS;
		double[] alfNurseExperienceRateAIS1;
		double[] alfNurseExperienceRateAIS2;
		int[] aiNurseRoomNumber;

		int iRow = 0;
		int iColumn = 0;
		double[][] pplfParameter;

		CCsv csv = new CCsv();
		if( strPath == "")
		{
			csv.vOpen( "./parameter/観察室.csv", "read" );
		}
		else
		{
			csv.vOpen( strPath, "read" );
		}
		csv.vGetRowColumn();

		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		pplfParameter = new double[iColumn][iRow];
		csv.vRead( pplfParameter );
		csv.vClose();
		// 医師数、看護師数、医療技師数を設定します。
		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			if( pplfParameter[0].length > 0 )
			{
				iObservationNurseNum = 0;
				for( i = 0;i < pplfParameter[0].length; i++ )
				{
					if( pplfParameter[0][i] == 1.0 )
					{
					}
					else if( pplfParameter[0][i] == 2.0 )
					{
						iObservationNurseNum++;
					}
					else if( pplfParameter[0][i] == 3.0 )
					{

					}
				}
			}
		}
		// 逆シミュレーションモードの場合
		else
		{
			// 部屋数が0以下の場合はすべて0にして終了する。
			if( this.iObservationRoomNum <= 0 )
			{
				this.iObservationRoomNum = 0;
				iObservationNurseNum = 0;
				ArrayListObservationRooms = new ArrayList<ERObservationRoom>();
				return ;
			}
		}
		alfNurseYearExperience			= new double[iObservationNurseNum];
		alfNurseConExperience			= new double[iObservationNurseNum];
		alfNurseExperienceRate1			= new double[iObservationNurseNum];
		alfNurseExperienceRate2			= new double[iObservationNurseNum];
		alfNurseConExperienceAIS		= new double[iObservationNurseNum];
		alfNurseExperienceRateAIS1		= new double[iObservationNurseNum];
		alfNurseExperienceRateAIS2		= new double[iObservationNurseNum];
		alfNurseConTired1				= new double[iObservationNurseNum];
		alfNurseConTired2				= new double[iObservationNurseNum];
		alfNurseConTired3				= new double[iObservationNurseNum];
		alfNurseConTired4				= new double[iObservationNurseNum];
		alfNurseTiredRate				= new double[iObservationNurseNum];
		alfNurseAssociationRate			= new double[iObservationNurseNum];
		alfNurseObservationTime			= new double[iObservationNurseNum];
		alfNurseObservationProcessTime	= new double[iObservationNurseNum];
		alfNurseTriageTime				= new double[iObservationNurseNum];
		alfNurseAssociationRate			= new double[iObservationNurseNum];
		alfNurseTriageYearExperience	= new double[iObservationNurseNum];

		aiNurseTriageProtocol			= new int[iObservationNurseNum];
		aiNurseTriageLevel				= new int[iObservationNurseNum];
		aiNurseDepartment				= new int[iObservationNurseNum];
		aiNurseCategory					= new int[iObservationNurseNum];
		aiNurseRoomNumber				= new int[iObservationNurseNum];

		if( ArrayListObservationRooms == null )
		{
			ArrayListObservationRooms = new ArrayList<ERObservationRoom>();
		}

//		for( i = 0;i < iNurseNum; i++ )
//		{
//		alfConExperience[i] = 3;
//		alfConTired[i] = 3;
//		alfObservationTime[i] = 30*60;
//		alfObservationProcessTime[i] = 30;
//		alfTriageTime[i] = 30*60;
//		aiProtocol[i] = 1;
//		aiLevel[i] = 5;
//		aiDepartment[i] = 4;
//		}

		for( i = 0;i < iObservationNurseNum; i++ )
		{
			alfNurseConExperience[i]			= 5;
			alfNurseConTired1[i]				= 3;
			alfNurseConTired2[i]				= 3;
			alfNurseConTired3[i]				= 3;
			alfNurseConTired4[i]				= 3;
			alfNurseTiredRate[i]				= 3;
			alfNurseObservationTime[i]			= 30*60;
			alfNurseObservationProcessTime[i]	= 30;
			alfNurseTriageTime[i] 				= 30*60;
			aiNurseTriageProtocol[i] 			= 1;
			aiNurseTriageLevel[i] 				= 5;
			alfNurseAssociationRate[i]		 	= 1.0;
			aiNurseDepartment[i] 				= 4;
			alfNurseExperienceRate1[i]			= 2.1;
			alfNurseExperienceRate2[i]			= 0.9;
			alfNurseConExperienceAIS[i]			= 0.14;
			alfNurseExperienceRateAIS1[i]		= 0.2;
			alfNurseExperienceRateAIS2[i]		= 1.1;
			aiNurseRoomNumber[i] 				= 1;
		}
		for( i = 0;i < pplfParameter[0].length; i++ )
		{
			if( pplfParameter[0][i] == 1.0 )
			{
			}
			else if( pplfParameter[0][i] == 2.0 )
			{
				if( iObservationNurseNum <= iCurrentNurseNum ) continue;
				if( iStartNurseLoc == 0 ) iStartNurseLoc = i;
				aiNurseCategory[iCurrentNurseNum] 					= (int)pplfParameter[1][i];
				aiNurseTriageProtocol[iCurrentNurseNum] 			= (int)pplfParameter[2][i];
				aiNurseTriageLevel[iCurrentNurseNum] 				= (int)pplfParameter[3][i];
				alfNurseTriageYearExperience[iCurrentNurseNum] 		= pplfParameter[4][i];
				alfNurseYearExperience[iCurrentNurseNum] 			= pplfParameter[5][i];
				alfNurseConExperience[iCurrentNurseNum] 			= pplfParameter[6][i];
				alfNurseConTired1[iCurrentNurseNum]			 		= pplfParameter[7][i];
				alfNurseConTired2[iCurrentNurseNum]			 		= pplfParameter[8][i];
				alfNurseConTired3[iCurrentNurseNum]			 		= pplfParameter[9][i];
				alfNurseConTired4[iCurrentNurseNum]			 		= pplfParameter[10][i];
				alfNurseTiredRate[iCurrentNurseNum]					= pplfParameter[11][i];
				alfNurseAssociationRate[iCurrentNurseNum]		 	= pplfParameter[12][i];
				aiNurseDepartment[iCurrentNurseNum]			 		= (int)pplfParameter[13][i];
				alfNurseExperienceRate1[iCurrentNurseNum]			= pplfParameter[14][i];
				alfNurseExperienceRate2[iCurrentNurseNum]			= pplfParameter[15][i];
				alfNurseConExperienceAIS[iCurrentNurseNum]			= pplfParameter[16][i];
				alfNurseExperienceRateAIS1[iCurrentNurseNum]		= pplfParameter[17][i];
				alfNurseExperienceRateAIS2[iCurrentNurseNum]		= pplfParameter[18][i];
				aiNurseRoomNumber[iCurrentNurseNum]			 		= (int)pplfParameter[19][i];
				alfNurseObservationTime[iCurrentNurseNum]		 	= 30*60;
				alfNurseObservationProcessTime[iCurrentNurseNum] 	= 30;
				alfNurseTriageTime[iCurrentNurseNum] 				= 30*60;
				iCurrentNurseNum++;
			}
			else if( pplfParameter[0][i] == 3.0 )
			{

			}
			else
			{

			}
		}

		if( iObservationNurseNum > iCurrentNurseNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iObservationNurseNum-iCurrentNurseNum; i < iObservationNurseNum; i++ )
			{
				iLocation = i % iCurrentNurseNum + iStartNurseLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				aiNurseCategory[i] 					= (int)pplfParameter[1][iLocation];
				aiNurseTriageProtocol[i] 			= (int)pplfParameter[2][iLocation];
				aiNurseTriageLevel[i] 				= (int)pplfParameter[3][iLocation];
				alfNurseTriageYearExperience[i]	 	= pplfParameter[4][iLocation];
				alfNurseYearExperience[i] 			= pplfParameter[5][iLocation];
				alfNurseConExperience[i] 			= pplfParameter[6][iLocation];
				alfNurseConTired1[i]			 	= pplfParameter[7][iLocation];
				alfNurseConTired2[i]			 	= pplfParameter[8][iLocation];
				alfNurseConTired3[i]				= pplfParameter[9][iLocation];
				alfNurseConTired4[i]			 	= pplfParameter[10][iLocation];
				alfNurseTiredRate[i]				= pplfParameter[11][iLocation];
				alfNurseAssociationRate[i]		 	= pplfParameter[12][iLocation];
				aiNurseDepartment[i]			 	= (int)pplfParameter[13][iLocation];
				alfNurseObservationTime[i]		 	= 30*60;
				alfNurseObservationProcessTime[i] 	= 30;
				alfNurseTriageTime[i] 				= 30*60;
				alfNurseExperienceRate1[i]			= pplfParameter[14][iLocation];
				alfNurseExperienceRate2[i]			= pplfParameter[15][iLocation];
				alfNurseConExperienceAIS[i]			= pplfParameter[16][iLocation];
				alfNurseExperienceRateAIS1[i]		= pplfParameter[17][iLocation];
				alfNurseExperienceRateAIS2[i]		= pplfParameter[18][iLocation];
				aiNurseRoomNumber[i]				= (int)pplfParameter[19][iLocation];
			}
		}

		for( i = 0;i < iObservationRoomNum; i++ )
		{
			ArrayListObservationRooms.add( new ERObservationRoom() );
			ArrayListObservationRooms.get(i).vSetRandom( sfmtRandom );
			ArrayListObservationRooms.get(i).vSetSimulationEngine( engine );

			// 看護師エージェントの作成
			ArrayListObservationRooms.get(i).vCreateNurseAgents( iObservationNurseNum );
			ArrayListObservationRooms.get(i).vSetNursesRandom();
			ArrayListObservationRooms.get(i).vSetNurseAgentParameter(aiNurseCategory,
					  aiNurseTriageProtocol,
					  aiNurseTriageLevel,
					  alfNurseTriageYearExperience,
					  alfNurseYearExperience,
					  alfNurseConExperience,
					  alfNurseExperienceRate1,
					  alfNurseExperienceRate2,
					  alfNurseConExperienceAIS,
					  alfNurseExperienceRateAIS1,
					  alfNurseExperienceRateAIS2,
					  alfNurseConTired1,
					  alfNurseConTired2,
					  alfNurseConTired3,
					  alfNurseConTired4,
					  alfNurseTiredRate,
					  alfNurseAssociationRate,
					  alfNurseObservationTime,
					  alfNurseObservationProcessTime,
					  alfNurseTriageTime,
					  aiNurseDepartment,
					  aiNurseRoomNumber );
			for( j = 0; j < ArrayListObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListObservationRooms.get(i).erGetNurseAgent(j).vSetSimulationEngine( engine );
				ArrayListObservationRooms.get(i).erGetNurseAgent(j).vSetSimulationEndTime( lfEndTime );
			}
			ArrayListObservationRooms.get(i).vCreatePatientAgents();
		}
	}

	/**
	 * <PRE>
	 *   重症観察室数及び構成する看護師の数を設定します。
	 * </PRE>
	 *
	 * @param iSereveInjuryObservationRoomNum	重症観察室エージェント数
	 * @param strPath							観察室に所属する看護師のパラメータが記述されたファイルパス
	 * @param engine							FUSEエンジン
	 * @param sfmtRandom						メルセンヌツイスターのインスタンス
	 * @throws IOException						ファイル処理中の例外
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	private void vSetSereveInjuryObservationRooms(int iSereveInjuryObservationRoomNum, String strPath, SimulationEngine engine, utility.sfmt.Rand sfmtRandom ) throws IOException
	{
		int i,j;
		int iCurrentNurseNum = 0;
		int iStartNurseLoc = 0;
		int iLocation = 0;

		int[] aiNurseCategory;
		int[] aiNurseTriageProtocol;
		int[] aiNurseTriageLevel;
		double[] alfNurseTriageYearExperience;
		double[] alfNurseYearExperience;
		double[] alfNurseConExperience;
		double[] alfNurseConTired1;
		double[] alfNurseConTired2;
		double[] alfNurseConTired3;
		double[] alfNurseConTired4;
		double[] alfNurseTiredRate;
		double[] alfNurseAssociationRate;
		double[] alfNurseObservationTime;
		double[] alfNurseObservationProcessTime;
		double[] alfNurseTriageTime;
		int[] aiNurseDepartment;
		double[] alfNurseExperienceRate1;
		double[] alfNurseExperienceRate2;
		double[] alfNurseConExperienceAIS;
		double[] alfNurseExperienceRateAIS1;
		double[] alfNurseExperienceRateAIS2;
		int[] aiNurseRoomNumber;


		int iRow = 0;
		int iColumn = 0;
		double[][] pplfParameter;

		CCsv csv = new CCsv();
		if( strPath == "")
		{
			csv.vOpen( "./parameter/重症観察室.csv", "read" );
		}
		else
		{
			csv.vOpen( strPath, "read" );
		}
		csv.vGetRowColumn();

		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		pplfParameter = new double[iColumn][iRow];
		csv.vRead( pplfParameter );
		csv.vClose();
		// 医師数、看護師数、医療技師数を設定します。
		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			if( pplfParameter[0].length > 0 )
			{
				iSevereInjuryObservationNurseNum = 0;
				for( i = 0;i < pplfParameter[0].length; i++ )
				{
					if( pplfParameter[0][i] == 1.0 )
					{
					}
					else if( pplfParameter[0][i] == 2.0 )
					{
						iSevereInjuryObservationNurseNum++;
					}
					else if( pplfParameter[0][i] == 3.0 )
					{

					}
				}
			}
		}
		// 逆シミュレーションモードの場合
		else
		{
			// 部屋数が0以下の場合はすべて0にして終了する。
			if( this.iSevereInjuryObservationNurseNum <= 0 )
			{
				iSevereInjuryObservationNurseNum = 0;
				ArrayListSevereInjuryObservationRooms = new ArrayList<ERSevereInjuryObservationRoom>();
				return ;
			}
		}
		alfNurseYearExperience			= new double[iSevereInjuryObservationNurseNum];
		alfNurseConExperience			= new double[iSevereInjuryObservationNurseNum];
		alfNurseExperienceRate1			= new double[iSevereInjuryObservationNurseNum];
		alfNurseExperienceRate2			= new double[iSevereInjuryObservationNurseNum];
		alfNurseConExperienceAIS		= new double[iSevereInjuryObservationNurseNum];
		alfNurseExperienceRateAIS1		= new double[iSevereInjuryObservationNurseNum];
		alfNurseExperienceRateAIS2		= new double[iSevereInjuryObservationNurseNum];
		alfNurseConTired1				= new double[iSevereInjuryObservationNurseNum];
		alfNurseConTired2				= new double[iSevereInjuryObservationNurseNum];
		alfNurseConTired3				= new double[iSevereInjuryObservationNurseNum];
		alfNurseConTired4				= new double[iSevereInjuryObservationNurseNum];
		alfNurseTiredRate				= new double[iSevereInjuryObservationNurseNum];
		alfNurseAssociationRate			= new double[iSevereInjuryObservationNurseNum];
		alfNurseObservationTime			= new double[iSevereInjuryObservationNurseNum];
		alfNurseObservationProcessTime	= new double[iSevereInjuryObservationNurseNum];
		alfNurseTriageTime				= new double[iSevereInjuryObservationNurseNum];
		alfNurseAssociationRate			= new double[iSevereInjuryObservationNurseNum];
		alfNurseTriageYearExperience	= new double[iSevereInjuryObservationNurseNum];

		aiNurseTriageProtocol			= new int[iSevereInjuryObservationNurseNum];
		aiNurseTriageLevel				= new int[iSevereInjuryObservationNurseNum];
		aiNurseDepartment				= new int[iSevereInjuryObservationNurseNum];
		aiNurseCategory					= new int[iSevereInjuryObservationNurseNum];
		aiNurseRoomNumber				= new int[iSevereInjuryObservationNurseNum];

		if( ArrayListSevereInjuryObservationRooms == null )
		{
			ArrayListSevereInjuryObservationRooms = new ArrayList<ERSevereInjuryObservationRoom>();
		}

//		for( i = 0;i < iNurseNum; i++ )
//		{
//		alfConExperience[i] = 3;
//		alfConTired[i] = 3;
//		alfObservationTime[i] = 30*60;
//		alfObservationProcessTime[i] = 30;
//		alfTriageTime[i] = 30*60;
//		aiProtocol[i] = 1;
//		aiLevel[i] = 5;
//		aiDepartment[i] = 4;
//		}

		for( i = 0;i < iSevereInjuryObservationNurseNum; i++ )
		{
			alfNurseConExperience[i]			= 5;
			alfNurseConTired1[i]				= 3;
			alfNurseConTired2[i]				= 3;
			alfNurseConTired3[i]				= 3;
			alfNurseConTired4[i]				= 3;
			alfNurseTiredRate[i]				= 3;
			alfNurseObservationTime[i]			= 30*60;
			alfNurseObservationProcessTime[i]	= 30;
			alfNurseTriageTime[i] 				= 30*60;
			aiNurseTriageProtocol[i] 			= 1;
			aiNurseTriageLevel[i] 				= 5;
			alfNurseAssociationRate[i]		 	= 1.0;
			aiNurseDepartment[i] 				= 5;
			alfNurseExperienceRate1[i]			= 2.1;
			alfNurseExperienceRate2[i]			= 0.9;
			alfNurseConExperienceAIS[i]			= 0.14;
			alfNurseExperienceRateAIS1[i]		= 0.2;
			alfNurseExperienceRateAIS2[i]		= 1.1;
			aiNurseRoomNumber[i] 				= 1;
		}
		for( i = 0;i < pplfParameter[0].length; i++ )
		{
			if( pplfParameter[0][i] == 1.0 )
			{
			}
			else if( pplfParameter[0][i] == 2.0 )
			{
				if( iSevereInjuryObservationNurseNum <= iCurrentNurseNum ) continue;
				if( iStartNurseLoc == 0 ) iStartNurseLoc = i;
				aiNurseCategory[iCurrentNurseNum] 					= (int)pplfParameter[1][i];
				aiNurseTriageProtocol[iCurrentNurseNum] 			= (int)pplfParameter[2][i];
				aiNurseTriageLevel[iCurrentNurseNum] 				= (int)pplfParameter[3][i];
				alfNurseTriageYearExperience[iCurrentNurseNum] 		= pplfParameter[4][i];
				alfNurseYearExperience[iCurrentNurseNum] 			= pplfParameter[5][i];
				alfNurseConExperience[iCurrentNurseNum] 			= pplfParameter[6][i];
				alfNurseConTired1[iCurrentNurseNum]			 		= pplfParameter[7][i];
				alfNurseConTired2[iCurrentNurseNum]			 		= pplfParameter[8][i];
				alfNurseConTired3[iCurrentNurseNum]			 		= pplfParameter[9][i];
				alfNurseConTired4[iCurrentNurseNum]			 		= pplfParameter[10][i];
				alfNurseTiredRate[iCurrentNurseNum]					= pplfParameter[11][i];
				alfNurseAssociationRate[iCurrentNurseNum]		 	= pplfParameter[12][i];
				aiNurseDepartment[iCurrentNurseNum]			 		= (int)pplfParameter[13][i];
				alfNurseExperienceRate1[iCurrentNurseNum]			= pplfParameter[14][i];
				alfNurseExperienceRate2[iCurrentNurseNum]			= pplfParameter[15][i];
				alfNurseConExperienceAIS[iCurrentNurseNum]			= pplfParameter[16][i];
				alfNurseExperienceRateAIS1[iCurrentNurseNum]		= pplfParameter[17][i];
				alfNurseExperienceRateAIS2[iCurrentNurseNum]		= pplfParameter[18][i];
				aiNurseRoomNumber[iCurrentNurseNum]				 	= (int)pplfParameter[19][i];
				alfNurseObservationTime[iCurrentNurseNum]		 	= 30*60;
				alfNurseObservationProcessTime[iCurrentNurseNum] 	= 30;
				alfNurseTriageTime[iCurrentNurseNum] 				= 30*60;
				iCurrentNurseNum++;
			}
			else if( pplfParameter[0][i] == 3.0 )
			{

			}
			else
			{

			}
		}


		if( iSevereInjuryObservationNurseNum > iCurrentNurseNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iSevereInjuryObservationNurseNum-iCurrentNurseNum; i < iSevereInjuryObservationNurseNum; i++ )
			{
				iLocation = i % iCurrentNurseNum + iStartNurseLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				aiNurseCategory[i] 					= (int)pplfParameter[1][iLocation];
				aiNurseTriageProtocol[i] 			= (int)pplfParameter[2][iLocation];
				aiNurseTriageLevel[i] 				= (int)pplfParameter[3][iLocation];
				alfNurseTriageYearExperience[i]	 	= pplfParameter[4][iLocation];
				alfNurseYearExperience[i] 			= pplfParameter[5][iLocation];
				alfNurseConExperience[i] 			= pplfParameter[6][iLocation];
				alfNurseConTired1[i]			 	= pplfParameter[7][iLocation];
				alfNurseConTired2[i]			 	= pplfParameter[8][iLocation];
				alfNurseConTired3[i]				= pplfParameter[9][iLocation];
				alfNurseConTired4[i]			 	= pplfParameter[10][iLocation];
				alfNurseTiredRate[i]				= pplfParameter[11][iLocation];
				alfNurseAssociationRate[i]		 	= pplfParameter[12][iLocation];
				aiNurseDepartment[i]			 	= (int)pplfParameter[13][iLocation];
				alfNurseObservationTime[i]		 	= 30*60;
				alfNurseObservationProcessTime[i] 	= 30;
				alfNurseTriageTime[i] 				= 30*60;
				alfNurseExperienceRate1[i]			= pplfParameter[14][iLocation];
				alfNurseExperienceRate2[i]			= pplfParameter[15][iLocation];
				alfNurseConExperienceAIS[i]			= pplfParameter[16][iLocation];
				alfNurseExperienceRateAIS1[i]		= pplfParameter[17][iLocation];
				alfNurseExperienceRateAIS2[i]		= pplfParameter[18][iLocation];
				aiNurseRoomNumber[i]				= (int)pplfParameter[19][iLocation];
			}
		}

		for( i = 0;i < iSereveInjuryObservationRoomNum; i++ )
		{
			ArrayListSevereInjuryObservationRooms.add( new ERSevereInjuryObservationRoom() );
			ArrayListSevereInjuryObservationRooms.get(i).vSetRandom( sfmtRandom );
			ArrayListSevereInjuryObservationRooms.get(i).vSetSimulationEngine( engine );

			// 看護師エージェントの作成
			ArrayListSevereInjuryObservationRooms.get(i).vCreateNurseAgents( iSevereInjuryObservationNurseNum );
			ArrayListSevereInjuryObservationRooms.get(i).vSetNursesRandom();
			ArrayListSevereInjuryObservationRooms.get(i).vSetNurseAgentParameter(aiNurseCategory,
					  aiNurseTriageProtocol,
					  aiNurseTriageLevel,
					  alfNurseTriageYearExperience,
					  alfNurseYearExperience,
					  alfNurseConExperience,
					  alfNurseExperienceRate1,
					  alfNurseExperienceRate2,
					  alfNurseConExperienceAIS,
					  alfNurseExperienceRateAIS1,
					  alfNurseExperienceRateAIS2,
					  alfNurseConTired1,
					  alfNurseConTired2,
					  alfNurseConTired3,
					  alfNurseConTired4,
					  alfNurseTiredRate,
					  alfNurseAssociationRate,
					  alfNurseObservationTime,
					  alfNurseObservationProcessTime,
					  alfNurseTriageTime,
					  aiNurseDepartment,
					  aiNurseRoomNumber );
			for( j = 0; j < ArrayListSevereInjuryObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListSevereInjuryObservationRooms.get(i).erGetNurseAgent(j).vSetSimulationEngine( engine );
				ArrayListSevereInjuryObservationRooms.get(i).erGetNurseAgent(j).vSetSimulationEndTime( lfEndTime );
			}
			ArrayListSevereInjuryObservationRooms.get(i).vCreatePatientAgents();
		}
	}

	/**
	 * <PRE>
	 *   集中治療室数及び構成する医師の数、看護師の数を設定します。
	 * </PRE>
	 *
	 * @param iIntensiveCareUnitRoomNum		集中治療室エージェント数
	 * @param strPath						集中治療室に所属する医師、看護師、医療技師のパラメータが記述されたファイルパス
	 * @param engine						FUSEエンジン
	 * @param sfmtRandom					メルセンヌツイスターのインスタンス
	 * @throws IOException					ファイル処理中の例外
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	private void vSetIntensiveCareUnitRooms(int iIntensiveCareUnitRoomNum, String strPath, SimulationEngine engine, utility.sfmt.Rand sfmtRandom ) throws IOException
	{
		int i,j;
		int iCurrentDoctorNum = 0;
		int iCurrentNurseNum = 0;
		int iStartDoctorLoc = 0;
		int iStartNurseLoc = 0;
		int iLocation = 0;

		int[] aiNurseCategory;
		int[] aiNurseTriageProtocol;
		int[] aiNurseTriageLevel;
		double[] alfNurseTriageYearExperience;
		double[] alfNurseYearExperience;
		double[] alfNurseConExperience;
		double[] alfNurseConTired1;
		double[] alfNurseConTired2;
		double[] alfNurseConTired3;
		double[] alfNurseConTired4;
		double[] alfNurseTiredRate;
		double[] alfNurseAssociationRate;
		double[] alfNurseObservationTime;
		double[] alfNurseObservationProcessTime;
		double[] alfNurseTriageTime;
		int[] aiNurseDepartment;
		double[] alfNurseExperienceRate1;
		double[] alfNurseExperienceRate2;
		double[] alfNurseConExperienceAIS;
		double[] alfNurseExperienceRateAIS1;
		double[] alfNurseExperienceRateAIS2;
		int[] aiNurseRoomNumber;

		double[] alfDoctorYearExperience;
		double[] alfDoctorConExperience;
		double[] alfDoctorConTired1;
		double[] alfDoctorConTired2;
		double[] alfDoctorConTired3;
		double[] alfDoctorConTired4;
		double[] alfDoctorTiredRate;
		double[] alfDoctorRevisedOperationRate;
		double[] alfDoctorAssociationRate;
		double[] alfDoctorConsultationTime;
		double[] alfDoctorOperationTime;
		double[] alfDoctorEmergencyTime;
		int[] aiDoctorDepartment;
		double[] alfDoctorExperienceRate1;
		double[] alfDoctorExperienceRate2;
		double[] alfDoctorConExperienceAIS;
		double[] alfDoctorExperienceRateAIS1;
		double[] alfDoctorExperienceRateAIS2;
		int[] aiDoctorRoomNumber;

		int iRow = 0;
		int iColumn = 0;
		double[][] pplfParameter;

		CCsv csv = new CCsv();
		if( strPath == "" )
		{
			csv.vOpen( "./parameter/ICU.csv", "read" );
		}
		else
		{
			csv.vOpen( strPath, "read" );
		}
		csv.vGetRowColumn();

		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		pplfParameter = new double[iColumn][iRow];
		csv.vRead( pplfParameter );
		csv.vClose();
		// 医師数、看護師数、医療技師数を設定します。
		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			if( pplfParameter[0].length > 2 )
			{
				iIntensiveCareUnitDoctorNum = 0;
				iIntensiveCareUnitNurseNum = 0;
//				iIntensiveCareUnitClinicalEngineerNum = 0;
				for( i = 0;i < pplfParameter[0].length; i++ )
				{
					if( pplfParameter[0][i] == 1.0 )
					{
						iIntensiveCareUnitDoctorNum++;
					}
					else if( pplfParameter[0][i] == 2.0 )
					{
						iIntensiveCareUnitNurseNum++;
					}
					else if( pplfParameter[0][i] == 3.0 )
					{
//						iIntensiveCareUnitClinicalEngineerNum++;
					}
				}
			}
		}
		// 逆シミュレーションモードの場合
		else
		{
			// 部屋数が0以下の場合はすべて0にして終了する。
			if( this.iIntensiveCareUnitRoomNum <= 0 )
			{
				this.iIntensiveCareUnitRoomNum = 0;
				this.iIntensiveCareUnitDoctorNum = 0;
				this.iIntensiveCareUnitNurseNum = 0;
				ArrayListIntensiveCareUnitRooms = new ArrayList<ERIntensiveCareUnitRoom>();
				return ;
			}
		}
		alfNurseYearExperience			= new double[iIntensiveCareUnitNurseNum];
		alfNurseConExperience			= new double[iIntensiveCareUnitNurseNum];
		alfNurseExperienceRate1			= new double[iIntensiveCareUnitNurseNum];
		alfNurseExperienceRate2			= new double[iIntensiveCareUnitNurseNum];
		alfNurseConExperienceAIS		= new double[iIntensiveCareUnitNurseNum];
		alfNurseExperienceRateAIS1		= new double[iIntensiveCareUnitNurseNum];
		alfNurseExperienceRateAIS2		= new double[iIntensiveCareUnitNurseNum];
		alfNurseConTired1				= new double[iIntensiveCareUnitNurseNum];
		alfNurseConTired2				= new double[iIntensiveCareUnitNurseNum];
		alfNurseConTired3				= new double[iIntensiveCareUnitNurseNum];
		alfNurseConTired4				= new double[iIntensiveCareUnitNurseNum];
		alfNurseTiredRate				= new double[iIntensiveCareUnitNurseNum];
		alfNurseAssociationRate			= new double[iIntensiveCareUnitNurseNum];
		alfNurseObservationTime			= new double[iIntensiveCareUnitNurseNum];
		alfNurseObservationProcessTime	= new double[iIntensiveCareUnitNurseNum];
		alfNurseTriageTime				= new double[iIntensiveCareUnitNurseNum];
		alfNurseAssociationRate			= new double[iIntensiveCareUnitNurseNum];
		alfNurseTriageYearExperience	= new double[iIntensiveCareUnitNurseNum];

		aiNurseTriageProtocol			= new int[iIntensiveCareUnitNurseNum];
		aiNurseTriageLevel				= new int[iIntensiveCareUnitNurseNum];
		aiNurseDepartment				= new int[iIntensiveCareUnitNurseNum];
		aiNurseCategory					= new int[iIntensiveCareUnitNurseNum];
		aiNurseRoomNumber				= new int[iIntensiveCareUnitNurseNum];

		if( ArrayListIntensiveCareUnitRooms == null )
		{
			ArrayListIntensiveCareUnitRooms = new ArrayList<ERIntensiveCareUnitRoom>();
		}

		for( i = 0;i < iIntensiveCareUnitNurseNum; i++ )
		{
			alfNurseConExperience[i]			= 3;
			alfNurseConTired1[i]				= 3;
			alfNurseConTired2[i]				= 3;
			alfNurseConTired3[i]				= 3;
			alfNurseConTired4[i]				= 3;
			alfNurseTiredRate[i]				= 3;
			alfNurseObservationTime[i]			= 30*60;
			alfNurseObservationProcessTime[i]	= 30;
			alfNurseTriageTime[i] 				= 30*60;
			aiNurseTriageProtocol[i] 			= 1;
			aiNurseTriageLevel[i] 				= 5;
			alfNurseAssociationRate[i]		 	= 1.0;
			aiNurseDepartment[i] 				= 6;
			alfNurseExperienceRate1[i]			= 2.1;
			alfNurseExperienceRate2[i]			= 0.9;
			alfNurseConExperienceAIS[i]			= 0.14;
			alfNurseExperienceRateAIS1[i]		= 0.2;
			alfNurseExperienceRateAIS2[i]		= 1.1;
			aiNurseRoomNumber[i] 				= 1;
		}

		alfDoctorYearExperience					= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorConExperience					= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorConTired1 						= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorConTired2 						= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorConTired3 						= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorConTired4 						= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorTiredRate						= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorRevisedOperationRate			= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorAssociationRate				= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorConsultationTime				= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorOperationTime					= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorEmergencyTime					= new double[iIntensiveCareUnitDoctorNum];
		aiDoctorDepartment						= new int[iIntensiveCareUnitDoctorNum];
		aiDoctorRoomNumber						= new int[iIntensiveCareUnitDoctorNum];
		alfDoctorExperienceRate1				= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorExperienceRate2				= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorConExperienceAIS				= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorExperienceRateAIS1				= new double[iIntensiveCareUnitDoctorNum];
		alfDoctorExperienceRateAIS2				= new double[iIntensiveCareUnitDoctorNum];
		for( i = 0;i < iIntensiveCareUnitDoctorNum; i++ )
		{
			alfDoctorYearExperience[i] 			= 10;
			alfDoctorConExperience[i] 			= 3;
			alfDoctorConTired1[i] 				= 3;
			alfDoctorConTired2[i] 				= 3;
			alfDoctorConTired3[i] 				= 3;
			alfDoctorConTired4[i] 				= 3;
			alfDoctorTiredRate[i] 				= 3.0;
			alfDoctorRevisedOperationRate[i]	= 0.666666666666;
			alfDoctorAssociationRate[i]			= 1.0;
			alfDoctorConsultationTime[i]		= 30*60;
			alfDoctorOperationTime[i] 			= 30;
			alfDoctorEmergencyTime[i] 			= 30*60;
			aiDoctorDepartment[i] 				= 6;
			alfDoctorExperienceRate1[i]			= 2.1;
			alfDoctorExperienceRate2[i]			= 0.9;
			alfDoctorConExperienceAIS[i]		= 0.14;
			alfDoctorExperienceRateAIS1[i]		= 0.2;
			alfDoctorExperienceRateAIS2[i]		= 1.1;
			aiDoctorRoomNumber[i] 				= 1;
		}

		for( i = 0;i < pplfParameter[0].length; i++ )
		{
			if( pplfParameter[0][i] == 1.0 )
			{
				if( iIntensiveCareUnitDoctorNum <= iCurrentDoctorNum ) continue;
				if( iStartDoctorLoc == 0 ) iStartDoctorLoc = i;
				alfDoctorYearExperience[iCurrentDoctorNum] 			= pplfParameter[1][i];
				alfDoctorConExperience[iCurrentDoctorNum] 			= pplfParameter[2][i];
				alfDoctorConTired1[iCurrentDoctorNum] 				= pplfParameter[3][i];
				alfDoctorConTired2[iCurrentDoctorNum] 				= pplfParameter[4][i];
				alfDoctorConTired3[iCurrentDoctorNum] 				= pplfParameter[5][i];
				alfDoctorConTired4[iCurrentDoctorNum] 				= pplfParameter[6][i];
				alfDoctorTiredRate[iCurrentDoctorNum] 				= pplfParameter[7][i];
				alfDoctorRevisedOperationRate[iCurrentDoctorNum]	= pplfParameter[8][i];
				alfDoctorAssociationRate[iCurrentDoctorNum]	 		= pplfParameter[9][i];
				aiDoctorDepartment[iCurrentDoctorNum]				= (int)pplfParameter[10][i];
				alfDoctorExperienceRate1[iCurrentDoctorNum]			= pplfParameter[11][i];
				alfDoctorExperienceRate2[iCurrentDoctorNum]			= pplfParameter[12][i];
				alfDoctorConExperienceAIS[iCurrentDoctorNum]		= pplfParameter[13][i];
				alfDoctorExperienceRateAIS1[iCurrentDoctorNum]		= pplfParameter[14][i];
				alfDoctorExperienceRateAIS2[iCurrentDoctorNum]		= pplfParameter[15][i];
				aiDoctorRoomNumber[iCurrentDoctorNum]				= (int)pplfParameter[16][i];
				alfDoctorConsultationTime[iCurrentDoctorNum]		= 30*60;
				alfDoctorOperationTime[iCurrentDoctorNum]			= 30;
				alfDoctorEmergencyTime[iCurrentDoctorNum]			= 30*60;
				iCurrentDoctorNum++;
			}
			else if( pplfParameter[0][i] == 2.0 )
			{
				if( iIntensiveCareUnitNurseNum <= iCurrentNurseNum ) continue;
				if( iStartNurseLoc == 0 ) iStartNurseLoc = i;
				aiNurseCategory[iCurrentNurseNum] 					= (int)pplfParameter[1][i];
				aiNurseTriageProtocol[iCurrentNurseNum] 			= (int)pplfParameter[2][i];
				aiNurseTriageLevel[iCurrentNurseNum] 				= (int)pplfParameter[3][i];
				alfNurseTriageYearExperience[iCurrentNurseNum] 		= pplfParameter[4][i];
				alfNurseYearExperience[iCurrentNurseNum] 			= pplfParameter[5][i];
				alfNurseConExperience[iCurrentNurseNum] 			= pplfParameter[6][i];
				alfNurseConTired1[iCurrentNurseNum]			 		= pplfParameter[7][i];
				alfNurseConTired2[iCurrentNurseNum]			 		= pplfParameter[8][i];
				alfNurseConTired3[iCurrentNurseNum]			 		= pplfParameter[9][i];
				alfNurseConTired4[iCurrentNurseNum]			 		= pplfParameter[10][i];
				alfNurseTiredRate[iCurrentNurseNum]					= pplfParameter[11][i];
				alfNurseAssociationRate[iCurrentNurseNum]		 	= pplfParameter[12][i];
				aiNurseDepartment[iCurrentNurseNum]			 		= (int)pplfParameter[13][i];
				alfNurseExperienceRate1[iCurrentNurseNum]			= pplfParameter[14][i];
				alfNurseExperienceRate2[iCurrentNurseNum]			= pplfParameter[15][i];
				alfNurseConExperienceAIS[iCurrentNurseNum]			= pplfParameter[16][i];
				alfNurseExperienceRateAIS1[iCurrentNurseNum]		= pplfParameter[17][i];
				alfNurseExperienceRateAIS2[iCurrentNurseNum]		= pplfParameter[18][i];
				aiNurseRoomNumber[iCurrentNurseNum]				 	= (int)pplfParameter[19][i];
				alfNurseObservationTime[iCurrentNurseNum]		 	= 30*60;
				alfNurseObservationProcessTime[iCurrentNurseNum] 	= 30;
				alfNurseTriageTime[iCurrentNurseNum] 				= 30*60;
				iCurrentNurseNum++;
			}
			else if( pplfParameter[0][i] == 3.0 )
			{

			}
			else
			{

			}
		}

		if( iIntensiveCareUnitDoctorNum > iCurrentDoctorNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iIntensiveCareUnitDoctorNum-iCurrentDoctorNum; i < iIntensiveCareUnitDoctorNum; i++ )
			{
				iLocation = i % iCurrentDoctorNum + iStartDoctorLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				alfDoctorYearExperience[i] 		= pplfParameter[1][iLocation];
				alfDoctorConExperience[i] 		= pplfParameter[2][iLocation];
				alfDoctorConTired1[i] 			= pplfParameter[3][iLocation];
				alfDoctorConTired2[i] 			= pplfParameter[4][iLocation];
				alfDoctorConTired3[i] 			= pplfParameter[5][iLocation];
				alfDoctorConTired4[i] 			= pplfParameter[6][iLocation];
				alfDoctorTiredRate[i] 			= pplfParameter[7][iLocation];
				alfDoctorRevisedOperationRate[i]= pplfParameter[8][iLocation];
				alfDoctorAssociationRate[i]	 	= pplfParameter[9][iLocation];
				aiDoctorDepartment[i]			= (int)pplfParameter[10][iLocation];
				alfDoctorConsultationTime[i]	= 30*60;
				alfDoctorOperationTime[i]		= 30;
				alfDoctorEmergencyTime[i]		= 30*60;
				alfDoctorExperienceRate1[i]		= pplfParameter[11][iLocation];
				alfDoctorExperienceRate2[i]		= pplfParameter[12][iLocation];
				alfDoctorConExperienceAIS[i]	= pplfParameter[13][iLocation];
				alfDoctorExperienceRateAIS1[i]	= pplfParameter[14][iLocation];
				alfDoctorExperienceRateAIS2[i]	= pplfParameter[15][iLocation];
				aiDoctorRoomNumber[i]			= (int)pplfParameter[16][iLocation];
			}
		}
		if( iIntensiveCareUnitNurseNum > iCurrentNurseNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iIntensiveCareUnitNurseNum-iCurrentNurseNum; i < iIntensiveCareUnitNurseNum; i++ )
			{
				iLocation = i % iCurrentNurseNum + iStartNurseLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				aiNurseCategory[i] 					= (int)pplfParameter[1][iLocation];
				aiNurseTriageProtocol[i] 			= (int)pplfParameter[2][iLocation];
				aiNurseTriageLevel[i] 				= (int)pplfParameter[3][iLocation];
				alfNurseTriageYearExperience[i]	 	= pplfParameter[4][iLocation];
				alfNurseYearExperience[i] 			= pplfParameter[5][iLocation];
				alfNurseConExperience[i] 			= pplfParameter[6][iLocation];
				alfNurseConTired1[i]			 	= pplfParameter[7][iLocation];
				alfNurseConTired2[i]			 	= pplfParameter[8][iLocation];
				alfNurseConTired3[i]				= pplfParameter[9][iLocation];
				alfNurseConTired4[i]			 	= pplfParameter[10][iLocation];
				alfNurseTiredRate[i]				= pplfParameter[11][iLocation];
				alfNurseAssociationRate[i]		 	= pplfParameter[12][iLocation];
				aiNurseDepartment[i]			 	= (int)pplfParameter[13][iLocation];
				alfNurseObservationTime[i]		 	= 30*60;
				alfNurseObservationProcessTime[i] 	= 30;
				alfNurseTriageTime[i] 				= 30*60;
				alfNurseExperienceRate1[i]			= pplfParameter[14][iLocation];
				alfNurseExperienceRate2[i]			= pplfParameter[15][iLocation];
				alfNurseConExperienceAIS[i]			= pplfParameter[16][iLocation];
				alfNurseExperienceRateAIS1[i]		= pplfParameter[17][iLocation];
				alfNurseExperienceRateAIS2[i]		= pplfParameter[18][iLocation];
				aiNurseRoomNumber[i]				= (int)pplfParameter[19][iLocation];
			}
		}

		for( i = 0;i < iIntensiveCareUnitRoomNum; i++ )
		{
			ArrayListIntensiveCareUnitRooms.add( new ERIntensiveCareUnitRoom() );
			ArrayListIntensiveCareUnitRooms.get(i).vSetRandom( sfmtRandom );
			ArrayListIntensiveCareUnitRooms.get(i).vSetSimulationEngine( engine );

			// 医師エージェントの作成
			ArrayListIntensiveCareUnitRooms.get(i).vCreateDoctorAgents( iIntensiveCareUnitDoctorNum );
			ArrayListIntensiveCareUnitRooms.get(i).vSetDoctorsRandom();
			ArrayListIntensiveCareUnitRooms.get(i).vSetDoctorAgentParameter( alfDoctorYearExperience,
					alfDoctorConExperience,
					alfDoctorExperienceRate1,
					alfDoctorExperienceRate2,
					alfDoctorConExperienceAIS,
					alfDoctorExperienceRateAIS1,
					alfDoctorExperienceRateAIS2,
					alfDoctorConTired1,
					alfDoctorConTired2,
					alfDoctorConTired3,
					alfDoctorConTired4,
					alfDoctorTiredRate,
					alfDoctorRevisedOperationRate,
					alfDoctorAssociationRate,
					alfDoctorConsultationTime,
					alfDoctorOperationTime,
					alfDoctorEmergencyTime,
					aiDoctorDepartment,
					aiDoctorRoomNumber );

			for( j = 0; j < ArrayListIntensiveCareUnitRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListIntensiveCareUnitRooms.get(i).cGetDoctorAgent(j).vSetSimulationEngine( engine );
				ArrayListIntensiveCareUnitRooms.get(i).cGetDoctorAgent(j).vSetSimulationEndTime( lfEndTime );
			}

			// 看護師エージェントの作成
			ArrayListIntensiveCareUnitRooms.get(i).vCreateNurseAgents( iIntensiveCareUnitNurseNum );
			ArrayListIntensiveCareUnitRooms.get(i).vSetNursesRandom();
			ArrayListIntensiveCareUnitRooms.get(i).vSetNurseAgentParameter(aiNurseCategory,
					  aiNurseTriageProtocol,
					  aiNurseTriageLevel,
					  alfNurseTriageYearExperience,
					  alfNurseYearExperience,
					  alfNurseConExperience,
					  alfNurseExperienceRate1,
					  alfNurseExperienceRate2,
					  alfNurseConExperienceAIS,
					  alfNurseExperienceRateAIS1,
					  alfNurseExperienceRateAIS2,
					  alfNurseConTired1,
					  alfNurseConTired2,
					  alfNurseConTired3,
					  alfNurseConTired4,
					  alfNurseTiredRate,
					  alfNurseAssociationRate,
					  alfNurseObservationTime,
					  alfNurseObservationProcessTime,
					  alfNurseTriageTime,
					  aiNurseDepartment,
					  aiNurseRoomNumber );
			for( j = 0; j < ArrayListIntensiveCareUnitRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListIntensiveCareUnitRooms.get(i).cGetNurseAgent(j).vSetSimulationEngine( engine );
				ArrayListIntensiveCareUnitRooms.get(i).cGetNurseAgent(j).vSetSimulationEndTime( lfEndTime );
			}
			ArrayListIntensiveCareUnitRooms.get(i).vCreatePatientAgents();
		}
	}

	/**
	 * <PRE>
	 *   高度治療室数及び構成する医師の数、看護師の数を設定します。
	 * </PRE>
	 *
	 * @param iHighCareUnitRoomNum		高度治療室エージェント数
	 * @param strPath					高度治療室に所属する医師、看護師、医療技師のパラメータが記述されたファイルパス
	 * @param engine					FUSEエンジン
	 * @param sfmtRandom				メルセンヌツイスターのインスタンス
	 * @throws IOException				ファイル処理中の例外
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	private void vSetHighCareUnitRooms(int iHighCareUnitRoomNum, String strPath, SimulationEngine engine, utility.sfmt.Rand sfmtRandom ) throws IOException
	{
		int i,j;
		int iCurrentDoctorNum = 0;
		int iCurrentNurseNum = 0;
		int iStartDoctorLoc = 0;
		int iStartNurseLoc = 0;
		int iLocation = 0;

		int[] aiNurseCategory;
		int[] aiNurseTriageProtocol;
		int[] aiNurseTriageLevel;
		double[] alfNurseTriageYearExperience;
		double[] alfNurseYearExperience;
		double[] alfNurseConExperience;
		double[] alfNurseExperienceRate1;
		double[] alfNurseExperienceRate2;
		double[] alfNurseConExperienceAIS;
		double[] alfNurseExperienceRateAIS1;
		double[] alfNurseExperienceRateAIS2;
		double[] alfNurseConTired1;
		double[] alfNurseConTired2;
		double[] alfNurseConTired3;
		double[] alfNurseConTired4;
		double[] alfNurseTiredRate;
		double[] alfNurseAssociationRate;
		double[] alfNurseObservationTime;
		double[] alfNurseObservationProcessTime;
		double[] alfNurseTriageTime;
		int[] aiNurseDepartment;
		int[] aiNurseRoomNumber;

		double[] alfDoctorYearExperience;
		double[] alfDoctorConExperience;
		double[] alfDoctorConTired1;
		double[] alfDoctorConTired2;
		double[] alfDoctorConTired3;
		double[] alfDoctorConTired4;
		double[] alfDoctorTiredRate;
		double[] alfDoctorRevisedOperationRate;
		double[] alfDoctorAssociationRate;
		double[] alfDoctorConsultationTime;
		double[] alfDoctorOperationTime;
		double[] alfDoctorEmergencyTime;
		int[] aiDoctorDepartment;
		double[] alfDoctorExperienceRate1;
		double[] alfDoctorExperienceRate2;
		double[] alfDoctorConExperienceAIS;
		double[] alfDoctorExperienceRateAIS1;
		double[] alfDoctorExperienceRateAIS2;
		int[] aiDoctorRoomNumber;

		int iRow = 0;
		int iColumn = 0;
		double[][] pplfParameter;

		CCsv csv = new CCsv();
		if( strPath == "")
		{
			csv.vOpen( "./parameter/HCU.csv", "read" );
		}
		else
		{
			csv.vOpen( strPath, "read" );
		}
		csv.vGetRowColumn();

		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		pplfParameter = new double[iColumn][iRow];
		csv.vRead( pplfParameter );
		csv.vClose();
		// 医師数、看護師数、医療技師数を設定します。
		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			if( pplfParameter[0].length > 2 )
			{
				iHighCareUnitDoctorNum = 0;
				iHighCareUnitNurseNum = 0;
//				iHighCareUnitClinicalEngineerNum = 0;
				for( i = 0;i < pplfParameter[0].length; i++ )
				{
					if( pplfParameter[0][i] == 1.0 )
					{
						iHighCareUnitDoctorNum++;
					}
					else if( pplfParameter[0][i] == 2.0 )
					{
						iHighCareUnitNurseNum++;
					}
					else if( pplfParameter[0][i] == 3.0 )
					{
//						iHighCareUnitClinicalEngineerNum++;
					}
				}
			}
		}
		// 逆シミュレーションモードの場合
		else
		{
			// 部屋数が0以下の場合はすべて0にして終了する。
			if( this.iHighCareUnitRoomNum <= 0 )
			{
				this.iHighCareUnitRoomNum = 0;
				this.iHighCareUnitDoctorNum = 0;
				this.iHighCareUnitNurseNum = 0;
				ArrayListHighCareUnitRooms = new ArrayList<ERHighCareUnitRoom>();
				return ;
			}
		}
		alfNurseYearExperience			= new double[iHighCareUnitNurseNum];
		alfNurseConExperience			= new double[iHighCareUnitNurseNum];
		alfNurseExperienceRate1			= new double[iHighCareUnitNurseNum];
		alfNurseExperienceRate2			= new double[iHighCareUnitNurseNum];
		alfNurseConExperienceAIS		= new double[iHighCareUnitNurseNum];
		alfNurseExperienceRateAIS1		= new double[iHighCareUnitNurseNum];
		alfNurseExperienceRateAIS2		= new double[iHighCareUnitNurseNum];
		alfNurseConTired1				= new double[iHighCareUnitNurseNum];
		alfNurseConTired2				= new double[iHighCareUnitNurseNum];
		alfNurseConTired3				= new double[iHighCareUnitNurseNum];
		alfNurseConTired4				= new double[iHighCareUnitNurseNum];
		alfNurseTiredRate				= new double[iHighCareUnitNurseNum];
		alfNurseAssociationRate			= new double[iHighCareUnitNurseNum];
		alfNurseObservationTime			= new double[iHighCareUnitNurseNum];
		alfNurseObservationProcessTime	= new double[iHighCareUnitNurseNum];
		alfNurseTriageTime				= new double[iHighCareUnitNurseNum];
		alfNurseAssociationRate			= new double[iHighCareUnitNurseNum];
		alfNurseTriageYearExperience	= new double[iHighCareUnitNurseNum];

		aiNurseTriageProtocol			= new int[iHighCareUnitNurseNum];
		aiNurseTriageLevel				= new int[iHighCareUnitNurseNum];
		aiNurseDepartment				= new int[iHighCareUnitNurseNum];
		aiNurseRoomNumber				= new int[iHighCareUnitNurseNum];
		aiNurseCategory					= new int[iHighCareUnitNurseNum];

		if( ArrayListHighCareUnitRooms == null )
		{
			ArrayListHighCareUnitRooms = new ArrayList<ERHighCareUnitRoom>();
		}

//		for( i = 0;i < iNurseNum; i++ )
//		{
//			alfNurseConExperience[i] = 3;
//			alfNurseConTired[i] = 3;
//			alfNurseObservationTime[i] = 30*60;
//			alfNurseObservationProcessTime[i] = 30;
//			alfNurseTriageTime[i] = 30*60;
//			aiNurseProtocol[i] = 1;
//			aiNurseLevel[i] = 5;
//			aiNurseDepartment[i] = 7;
//		}

		for( i = 0;i < iHighCareUnitNurseNum; i++ )
		{
			alfNurseConExperience[i]			= 3;
			alfNurseConTired1[i]				= 3;
			alfNurseConTired2[i]				= 3;
			alfNurseConTired3[i]				= 3;
			alfNurseConTired4[i]				= 3;
			alfNurseTiredRate[i]				= 3;
			alfNurseObservationTime[i]			= 30*60;
			alfNurseObservationProcessTime[i]	= 30;
			alfNurseTriageTime[i] 				= 30*60;
			aiNurseTriageProtocol[i] 			= 1;
			aiNurseTriageLevel[i] 				= 5;
			alfNurseAssociationRate[i]		 	= 1.0;
			aiNurseDepartment[i] 				= 7;
			alfNurseExperienceRate1[i]			= 2.1;
			alfNurseExperienceRate2[i]			= 0.9;
			alfNurseConExperienceAIS[i]			= 0.14;
			alfNurseExperienceRateAIS1[i]		= 0.2;
			alfNurseExperienceRateAIS2[i]		= 1.1;
			aiNurseRoomNumber[i] 				= 1;
		}

		alfDoctorYearExperience					= new double[iHighCareUnitDoctorNum];
		alfDoctorConExperience					= new double[iHighCareUnitDoctorNum];
		alfDoctorExperienceRate1				= new double[iHighCareUnitDoctorNum];
		alfDoctorExperienceRate2				= new double[iHighCareUnitDoctorNum];
		alfDoctorConExperienceAIS				= new double[iHighCareUnitDoctorNum];
		alfDoctorExperienceRateAIS1				= new double[iHighCareUnitDoctorNum];
		alfDoctorExperienceRateAIS2				= new double[iHighCareUnitDoctorNum];
		alfDoctorConTired1 						= new double[iHighCareUnitDoctorNum];
		alfDoctorConTired2 						= new double[iHighCareUnitDoctorNum];
		alfDoctorConTired3 						= new double[iHighCareUnitDoctorNum];
		alfDoctorConTired4 						= new double[iHighCareUnitDoctorNum];
		alfDoctorTiredRate						= new double[iHighCareUnitDoctorNum];
		alfDoctorRevisedOperationRate			= new double[iHighCareUnitDoctorNum];
		alfDoctorAssociationRate				= new double[iHighCareUnitDoctorNum];
		alfDoctorConsultationTime				= new double[iHighCareUnitDoctorNum];
		alfDoctorOperationTime					= new double[iHighCareUnitDoctorNum];
		alfDoctorEmergencyTime					= new double[iHighCareUnitDoctorNum];
		aiDoctorDepartment						= new int[iHighCareUnitDoctorNum];
		aiDoctorRoomNumber						= new int[iHighCareUnitDoctorNum];

		for( i = 0;i < iHighCareUnitDoctorNum; i++ )
		{
			alfDoctorYearExperience[i] 			= 10;
			alfDoctorConExperience[i] 			= 3;
			alfDoctorConTired1[i] 				= 3;
			alfDoctorConTired2[i] 				= 3;
			alfDoctorConTired3[i] 				= 3;
			alfDoctorConTired4[i] 				= 3;
			alfDoctorTiredRate[i] 				= 3.0;
			alfDoctorRevisedOperationRate[i]	= 0.666666666666;
			alfDoctorAssociationRate[i]	 		= 1.0;
			alfDoctorConsultationTime[i] 		= 30*60;
			alfDoctorOperationTime[i] 			= 30;
			alfDoctorEmergencyTime[i] 			= 30*60;
			aiDoctorDepartment[i] 				= 7;
			alfDoctorExperienceRate1[i]			= 2.1;
			alfDoctorExperienceRate2[i]			= 0.9;
			alfDoctorConExperienceAIS[i]		= 0.14;
			alfDoctorExperienceRateAIS1[i]		= 0.2;
			alfDoctorExperienceRateAIS2[i]		= 1.1;
			aiDoctorRoomNumber[i] 				= 1;
		}

		for( i = 0;i < pplfParameter[0].length; i++ )
		{
			if( pplfParameter[0][i] == 1.0 )
			{
				if( iHighCareUnitDoctorNum <= iCurrentDoctorNum ) continue;
				if( iStartDoctorLoc == 0 ) iStartDoctorLoc = i;
				alfDoctorYearExperience[iCurrentDoctorNum] 		= pplfParameter[1][i];
				alfDoctorConExperience[iCurrentDoctorNum] 		= pplfParameter[2][i];
				alfDoctorConTired1[iCurrentDoctorNum] 			= pplfParameter[3][i];
				alfDoctorConTired2[iCurrentDoctorNum] 			= pplfParameter[4][i];
				alfDoctorConTired3[iCurrentDoctorNum] 			= pplfParameter[5][i];
				alfDoctorConTired4[iCurrentDoctorNum] 			= pplfParameter[6][i];
				alfDoctorTiredRate[iCurrentDoctorNum] 			= pplfParameter[7][i];
				alfDoctorRevisedOperationRate[iCurrentDoctorNum]= pplfParameter[8][i];
				alfDoctorAssociationRate[iCurrentDoctorNum]	 	= pplfParameter[9][i];
				aiDoctorDepartment[iCurrentDoctorNum]			= (int)pplfParameter[10][i];
				alfDoctorExperienceRate1[iCurrentDoctorNum]		= pplfParameter[11][i];
				alfDoctorExperienceRate2[iCurrentDoctorNum]		= pplfParameter[12][i];
				alfDoctorConExperienceAIS[iCurrentDoctorNum]	= pplfParameter[13][i];
				alfDoctorExperienceRateAIS1[iCurrentDoctorNum]	= pplfParameter[14][i];
				alfDoctorExperienceRateAIS2[iCurrentDoctorNum]	= pplfParameter[15][i];
				aiDoctorRoomNumber[iCurrentDoctorNum]			= (int)pplfParameter[16][i];
				alfDoctorConsultationTime[iCurrentDoctorNum]	= 30*60;
				alfDoctorOperationTime[iCurrentDoctorNum]		= 30;
				alfDoctorEmergencyTime[iCurrentDoctorNum]		= 30*60;
				iCurrentDoctorNum++;
			}
			else if( pplfParameter[0][i] == 2.0 )
			{
				if( iHighCareUnitNurseNum <= iCurrentNurseNum ) continue;
				if( iStartNurseLoc == 0 ) iStartNurseLoc = i;
				aiNurseCategory[iCurrentNurseNum] 					= (int)pplfParameter[1][i];
				aiNurseTriageProtocol[iCurrentNurseNum] 			= (int)pplfParameter[2][i];
				aiNurseTriageLevel[iCurrentNurseNum] 				= (int)pplfParameter[3][i];
				alfNurseTriageYearExperience[iCurrentNurseNum] 		= pplfParameter[4][i];
				alfNurseYearExperience[iCurrentNurseNum] 			= pplfParameter[5][i];
				alfNurseConExperience[iCurrentNurseNum] 			= pplfParameter[6][i];
				alfNurseConTired1[iCurrentNurseNum]			 		= pplfParameter[7][i];
				alfNurseConTired2[iCurrentNurseNum]			 		= pplfParameter[8][i];
				alfNurseConTired3[iCurrentNurseNum]			 		= pplfParameter[9][i];
				alfNurseConTired4[iCurrentNurseNum]			 		= pplfParameter[10][i];
				alfNurseTiredRate[iCurrentNurseNum]					= pplfParameter[11][i];
				alfNurseAssociationRate[iCurrentNurseNum]		 	= pplfParameter[12][i];
				aiNurseDepartment[iCurrentNurseNum]			 		= (int)pplfParameter[13][i];
				alfNurseExperienceRate1[iCurrentNurseNum]			= pplfParameter[14][i];
				alfNurseExperienceRate2[iCurrentNurseNum]			= pplfParameter[15][i];
				alfNurseConExperienceAIS[iCurrentNurseNum]			= pplfParameter[16][i];
				alfNurseExperienceRateAIS1[iCurrentNurseNum]		= pplfParameter[17][i];
				alfNurseExperienceRateAIS2[iCurrentNurseNum]		= pplfParameter[18][i];
				aiNurseRoomNumber[iCurrentNurseNum]				 	= (int)pplfParameter[19][i];
				alfNurseObservationTime[iCurrentNurseNum]		 	= 30*60;
				alfNurseObservationProcessTime[iCurrentNurseNum] 	= 30;
				alfNurseTriageTime[iCurrentNurseNum] 				= 30*60;
				iCurrentNurseNum++;
			}
			else if( pplfParameter[0][i] == 3.0 )
			{

			}
			else
			{

			}
		}


		if( iHighCareUnitDoctorNum > iCurrentDoctorNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iHighCareUnitDoctorNum-iCurrentDoctorNum; i < iHighCareUnitDoctorNum; i++ )
			{
				iLocation = i % iCurrentDoctorNum + iStartDoctorLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				alfDoctorYearExperience[i] 		= pplfParameter[1][iLocation];
				alfDoctorConExperience[i] 		= pplfParameter[2][iLocation];
				alfDoctorConTired1[i] 			= pplfParameter[3][iLocation];
				alfDoctorConTired2[i] 			= pplfParameter[4][iLocation];
				alfDoctorConTired3[i] 			= pplfParameter[5][iLocation];
				alfDoctorConTired4[i] 			= pplfParameter[6][iLocation];
				alfDoctorTiredRate[i] 			= pplfParameter[7][iLocation];
				alfDoctorRevisedOperationRate[i]= pplfParameter[8][iLocation];
				alfDoctorAssociationRate[i]	 	= pplfParameter[9][iLocation];
				aiDoctorDepartment[i]			= (int)pplfParameter[10][iLocation];
				alfDoctorConsultationTime[i]	= 30*60;
				alfDoctorOperationTime[i]		= 30;
				alfDoctorEmergencyTime[i]		= 30*60;
				alfDoctorExperienceRate1[i]		= pplfParameter[11][iLocation];
				alfDoctorExperienceRate2[i]		= pplfParameter[12][iLocation];
				alfDoctorConExperienceAIS[i]	= pplfParameter[13][iLocation];
				alfDoctorExperienceRateAIS1[i]	= pplfParameter[14][iLocation];
				alfDoctorExperienceRateAIS2[i]	= pplfParameter[15][iLocation];
				aiDoctorRoomNumber[i]			= (int)pplfParameter[16][iLocation];
			}
		}
		if( iHighCareUnitNurseNum > iCurrentNurseNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iHighCareUnitNurseNum-iCurrentNurseNum; i < iHighCareUnitNurseNum; i++ )
			{
				iLocation = i % iCurrentNurseNum + iStartNurseLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				aiNurseCategory[i] 					= (int)pplfParameter[1][iLocation];
				aiNurseTriageProtocol[i] 			= (int)pplfParameter[2][iLocation];
				aiNurseTriageLevel[i] 				= (int)pplfParameter[3][iLocation];
				alfNurseTriageYearExperience[i]	 	= pplfParameter[4][iLocation];
				alfNurseYearExperience[i] 			= pplfParameter[5][iLocation];
				alfNurseConExperience[i] 			= pplfParameter[6][iLocation];
				alfNurseConTired1[i]			 	= pplfParameter[7][iLocation];
				alfNurseConTired2[i]			 	= pplfParameter[8][iLocation];
				alfNurseConTired3[i]				= pplfParameter[9][iLocation];
				alfNurseConTired4[i]			 	= pplfParameter[10][iLocation];
				alfNurseTiredRate[i]				= pplfParameter[11][iLocation];
				alfNurseAssociationRate[i]		 	= pplfParameter[12][iLocation];
				aiNurseDepartment[i]			 	= (int)pplfParameter[13][iLocation];
				alfNurseObservationTime[i]		 	= 30*60;
				alfNurseObservationProcessTime[i] 	= 30;
				alfNurseTriageTime[i] 				= 30*60;
				alfNurseExperienceRate1[i]			= pplfParameter[14][iLocation];
				alfNurseExperienceRate2[i]			= pplfParameter[15][iLocation];
				alfNurseConExperienceAIS[i]			= pplfParameter[16][iLocation];
				alfNurseExperienceRateAIS1[i]		= pplfParameter[17][iLocation];
				alfNurseExperienceRateAIS2[i]		= pplfParameter[18][iLocation];
				aiNurseRoomNumber[i]				= (int)pplfParameter[19][iLocation];
			}
		}

		for( i = 0;i < iHighCareUnitRoomNum; i++ )
		{
			ArrayListHighCareUnitRooms.add( new ERHighCareUnitRoom() );
			ArrayListHighCareUnitRooms.get(i).vSetRandom( sfmtRandom );
			ArrayListHighCareUnitRooms.get(i).vSetSimulationEngine( engine );

			// 医師エージェントの作成
			ArrayListHighCareUnitRooms.get(i).vCreateDoctorAgents( iHighCareUnitDoctorNum );
			ArrayListHighCareUnitRooms.get(i).vSetDoctorsRandom();
			ArrayListHighCareUnitRooms.get(i).vSetDoctorAgentParameter( alfDoctorYearExperience,
					alfDoctorConExperience,
					alfDoctorExperienceRate1,
					alfDoctorExperienceRate2,
					alfDoctorConExperienceAIS,
					alfDoctorExperienceRateAIS1,
					alfDoctorExperienceRateAIS2,
					alfDoctorConTired1,
					alfDoctorConTired2,
					alfDoctorConTired3,
					alfDoctorConTired4,
					alfDoctorTiredRate,
					alfDoctorRevisedOperationRate,
					alfDoctorAssociationRate,
					alfDoctorConsultationTime,
					alfDoctorOperationTime,
					alfDoctorEmergencyTime,
					aiDoctorDepartment,
					aiDoctorRoomNumber );
			for( j = 0; j < ArrayListHighCareUnitRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListHighCareUnitRooms.get(i).cGetDoctorAgent(j).vSetSimulationEngine( engine );
				ArrayListHighCareUnitRooms.get(i).cGetDoctorAgent(j).vSetSimulationEndTime( lfEndTime );
			}
			// 看護師エージェントの作成
			ArrayListHighCareUnitRooms.get(i).vCreateNurseAgents( iHighCareUnitNurseNum );
			ArrayListHighCareUnitRooms.get(i).vSetNursesRandom();
			ArrayListHighCareUnitRooms.get(i).vSetNurseAgentParameter(aiNurseCategory,
					  aiNurseTriageProtocol,
					  aiNurseTriageLevel,
					  alfNurseTriageYearExperience,
					  alfNurseYearExperience,
					  alfNurseConExperience,
					  alfNurseExperienceRate1,
					  alfNurseExperienceRate2,
					  alfNurseConExperienceAIS,
					  alfNurseExperienceRateAIS1,
					  alfNurseExperienceRateAIS2,
					  alfNurseConTired1,
					  alfNurseConTired2,
					  alfNurseConTired3,
					  alfNurseConTired4,
					  alfNurseTiredRate,
					  alfNurseAssociationRate,
					  alfNurseObservationTime,
					  alfNurseObservationProcessTime,
					  alfNurseTriageTime,
					  aiNurseDepartment,
					  aiNurseRoomNumber );
			for( j = 0; j < ArrayListHighCareUnitRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListHighCareUnitRooms.get(i).cGetNurseAgent(j).vSetSimulationEngine( engine );
				ArrayListHighCareUnitRooms.get(i).cGetNurseAgent(j).vSetSimulationEndTime( lfEndTime );
			}
			ArrayListHighCareUnitRooms.get(i).vCreatePatientAgents();
		}
	}

	/**
	 * <PRE>
	 *   一般病棟数及び構成する医師の数、看護師の数を設定します。
	 * </PRE>
	 *
	 * @param iGeneralWardRoomNum		一般病棟エージェント数
	 * @param strPath					一般病棟に所属する医師、看護師のパラメータが記述されたファイルパス
	 * @param engine					FUSEエンジン
	 * @param sfmtRandom				メルセンヌツイスターのインスタンス
	 * @throws IOException				ファイル処理中の例外
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	private void vSetGeneralWardRooms(int iGeneralWardRoomNum, String strPath, SimulationEngine engine, utility.sfmt.Rand sfmtRandom ) throws IOException
	{
		int i,j;
		int iCurrentNurseNum = 0;
		int iStartNurseLoc = 0;
		int iLocation = 0;

		int[] aiNurseCategory;
		int[] aiNurseTriageProtocol;
		int[] aiNurseTriageLevel;
		double[] alfNurseTriageYearExperience;
		double[] alfNurseYearExperience;
		double[] alfNurseConExperience;
		double[] alfNurseConTired1;
		double[] alfNurseConTired2;
		double[] alfNurseConTired3;
		double[] alfNurseConTired4;
		double[] alfNurseTiredRate;
		double[] alfNurseAssociationRate;
		double[] alfNurseObservationTime;
		double[] alfNurseObservationProcessTime;
		double[] alfNurseTriageTime;
		int[] aiNurseDepartment;
		double[] alfNurseExperienceRate1;
		double[] alfNurseExperienceRate2;
		double[] alfNurseConExperienceAIS;
		double[] alfNurseExperienceRateAIS1;
		double[] alfNurseExperienceRateAIS2;
		int[] aiNurseRoomNumber;

		double lfDoctorYearExperience;
		double lfDoctorConExperience;
		double lfDoctorConTired1;
		double lfDoctorConTired2;
		double lfDoctorConTired3;
		double lfDoctorConTired4;
		double lfDoctorTiredRate;
		double lfDoctorRevisedOperationRate;
		double lfDoctorAssociationRate;
		double lfDoctorConsultationTime;
		double lfDoctorOperationTime;
		double lfDoctorEmergencyTime;
		int iDoctorDepartment;
		double lfDoctorExperienceRate1;
		double lfDoctorExperienceRate2;
		double lfDoctorConExperienceAIS;
		double lfDoctorExperienceRateAIS1;
		double lfDoctorExperienceRateAIS2;
		int iDoctorRoomNumber;

		int iRow = 0;
		int iColumn = 0;
		double[][] pplfParameter;

		CCsv csv = new CCsv();
		if( strPath == "" )
		{
			csv.vOpen( "./parameter/一般病棟.csv", "read" );
		}
		else
		{
			csv.vOpen( strPath, "read" );
		}
		csv.vGetRowColumn();

		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		pplfParameter = new double[iColumn][iRow];
		csv.vRead( pplfParameter );
		csv.vClose();
		// 医師数、看護師数、医療技師数を設定します。
		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			if( pplfParameter[0].length > 0 )
			{
				iGeneralWardNurseNum = 0;
				for( i = 0;i < pplfParameter[0].length; i++ )
				{
					if( pplfParameter[0][i] == 1.0 )
					{
					}
					else if( pplfParameter[0][i] == 2.0 )
					{
						iGeneralWardNurseNum++;
					}
					else if( pplfParameter[0][i] == 3.0 )
					{

					}
				}
			}
		}
		// 逆シミュレーションモードの場合
		else
		{
			// 部屋数が0以下の場合はすべて0にして終了する。
			if( this.iGeneralWardRoomNum <= 0 )
			{
				this.iGeneralWardRoomNum = 0;
				this.iGeneralWardDoctorNum = 0;
				this.iGeneralWardNurseNum = 0;
				ArrayListGeneralWardRooms = new ArrayList<ERGeneralWardRoom>();
				return ;
			}
		}

		alfNurseYearExperience			= new double[iGeneralWardNurseNum];
		alfNurseConExperience			= new double[iGeneralWardNurseNum];
		alfNurseExperienceRate1			= new double[iGeneralWardNurseNum];
		alfNurseExperienceRate2			= new double[iGeneralWardNurseNum];
		alfNurseConExperienceAIS		= new double[iGeneralWardNurseNum];
		alfNurseExperienceRateAIS1		= new double[iGeneralWardNurseNum];
		alfNurseExperienceRateAIS2		= new double[iGeneralWardNurseNum];
		alfNurseConTired1				= new double[iGeneralWardNurseNum];
		alfNurseConTired2				= new double[iGeneralWardNurseNum];
		alfNurseConTired3				= new double[iGeneralWardNurseNum];
		alfNurseConTired4				= new double[iGeneralWardNurseNum];
		alfNurseTiredRate				= new double[iGeneralWardNurseNum];
		alfNurseAssociationRate			= new double[iGeneralWardNurseNum];
		alfNurseObservationTime			= new double[iGeneralWardNurseNum];
		alfNurseObservationProcessTime	= new double[iGeneralWardNurseNum];
		alfNurseTriageTime				= new double[iGeneralWardNurseNum];
		alfNurseAssociationRate			= new double[iGeneralWardNurseNum];
		alfNurseTriageYearExperience	= new double[iGeneralWardNurseNum];

		aiNurseTriageProtocol			= new int[iGeneralWardNurseNum];
		aiNurseTriageLevel				= new int[iGeneralWardNurseNum];
		aiNurseDepartment				= new int[iGeneralWardNurseNum];
		aiNurseRoomNumber				= new int[iGeneralWardNurseNum];
		aiNurseCategory					= new int[iGeneralWardNurseNum];

		if( ArrayListGeneralWardRooms == null )
		{
			ArrayListGeneralWardRooms = new ArrayList<ERGeneralWardRoom>();
		}

		for( i = 0;i < iGeneralWardNurseNum; i++ )
		{
			alfNurseConExperience[i]			= 3;
			alfNurseConTired1[i]				= 3;
			alfNurseConTired2[i]				= 3;
			alfNurseConTired3[i]				= 3;
			alfNurseConTired4[i]				= 3;
			alfNurseTiredRate[i]				= 3;
			alfNurseObservationTime[i]			= 30*60;
			alfNurseObservationProcessTime[i]	= 30;
			alfNurseTriageTime[i] 				= 30*60;
			aiNurseTriageProtocol[i] 			= 1;
			aiNurseTriageLevel[i] 				= 5;
			alfNurseAssociationRate[i]		 	= 1.0;
			aiNurseDepartment[i] 				= 6;
			alfNurseExperienceRate1[i]			= 2.1;
			alfNurseExperienceRate2[i]			= 0.9;
			alfNurseConExperienceAIS[i]			= 0.14;
			alfNurseExperienceRateAIS1[i]		= 0.2;
			alfNurseExperienceRateAIS2[i]		= 1.1;
			aiNurseRoomNumber[i] 				= 1;
		}

		lfDoctorYearExperience 			= 10;
		lfDoctorConExperience 			= 3;
		lfDoctorConTired1 				= 3;
		lfDoctorConTired2 				= 3;
		lfDoctorConTired3 				= 3;
		lfDoctorConTired4 				= 3;
		lfDoctorTiredRate 				= 3.0;
		lfDoctorRevisedOperationRate	= 0.666666666666;
		lfDoctorAssociationRate	 		= 1.0;
		lfDoctorConsultationTime 		= 30*60;
		lfDoctorOperationTime 			= 30;
		lfDoctorEmergencyTime 			= 30*60;
		iDoctorDepartment 				= 8;
		lfDoctorExperienceRate1			= 2.1;
		lfDoctorExperienceRate2			= 0.9;
		lfDoctorConExperienceAIS		= 0.14;
		lfDoctorExperienceRateAIS1		= 0.2;
		lfDoctorExperienceRateAIS2		= 1.1;
		iDoctorRoomNumber				= 1;

		for( i = 0;i < pplfParameter[0].length; i++ )
		{
			if( pplfParameter[0][i] == 1.0 )
			{
				lfDoctorYearExperience 			= pplfParameter[1][i];
				lfDoctorConExperience 			= pplfParameter[2][i];
				lfDoctorConTired1 				= pplfParameter[3][i];
				lfDoctorConTired2 				= pplfParameter[4][i];
				lfDoctorConTired3 				= pplfParameter[5][i];
				lfDoctorConTired4 				= pplfParameter[6][i];
				lfDoctorTiredRate 				= pplfParameter[7][i];
				lfDoctorRevisedOperationRate	= pplfParameter[8][i];
				lfDoctorAssociationRate	 		= pplfParameter[9][i];
				iDoctorDepartment				= (int)pplfParameter[10][i];
				lfDoctorExperienceRate1			= pplfParameter[11][i];
				lfDoctorExperienceRate2			= pplfParameter[12][i];
				lfDoctorConExperienceAIS		= pplfParameter[13][i];
				lfDoctorExperienceRateAIS1		= pplfParameter[14][i];
				lfDoctorExperienceRateAIS2		= pplfParameter[15][i];
				iDoctorRoomNumber				= (int)pplfParameter[16][i];
				lfDoctorConsultationTime		= 30*60;
				lfDoctorOperationTime			= 30;
				lfDoctorEmergencyTime			= 30*60;
			}
			else if( pplfParameter[0][i] == 2.0 )
			{
				if( iGeneralWardNurseNum <= iCurrentNurseNum ) continue;
				if( iStartNurseLoc == 0 ) iStartNurseLoc = i;
				aiNurseCategory[iCurrentNurseNum] 					= (int)pplfParameter[1][i];
				aiNurseTriageProtocol[iCurrentNurseNum] 			= (int)pplfParameter[2][i];
				aiNurseTriageLevel[iCurrentNurseNum] 				= (int)pplfParameter[3][i];
				alfNurseTriageYearExperience[iCurrentNurseNum] 		= pplfParameter[4][i];
				alfNurseYearExperience[iCurrentNurseNum] 			= pplfParameter[5][i];
				alfNurseConExperience[iCurrentNurseNum] 			= pplfParameter[6][i];
				alfNurseConTired1[iCurrentNurseNum]			 		= pplfParameter[7][i];
				alfNurseConTired2[iCurrentNurseNum]			 		= pplfParameter[8][i];
				alfNurseConTired3[iCurrentNurseNum]			 		= pplfParameter[9][i];
				alfNurseConTired4[iCurrentNurseNum]			 		= pplfParameter[10][i];
				alfNurseTiredRate[iCurrentNurseNum]					= pplfParameter[11][i];
				alfNurseAssociationRate[iCurrentNurseNum]		 	= pplfParameter[12][i];
				aiNurseDepartment[iCurrentNurseNum]			 		= (int)pplfParameter[13][i];
				alfNurseExperienceRate1[iCurrentNurseNum]			= pplfParameter[14][i];
				alfNurseExperienceRate2[iCurrentNurseNum]			= pplfParameter[15][i];
				alfNurseConExperienceAIS[iCurrentNurseNum]			= pplfParameter[16][i];
				alfNurseExperienceRateAIS1[iCurrentNurseNum]		= pplfParameter[17][i];
				alfNurseExperienceRateAIS2[iCurrentNurseNum]		= pplfParameter[18][i];
				aiNurseRoomNumber[iCurrentNurseNum]				 	= (int)pplfParameter[19][i];
				alfNurseObservationTime[iCurrentNurseNum]		 	= 30*60;
				alfNurseObservationProcessTime[iCurrentNurseNum] 	= 30;
				alfNurseTriageTime[iCurrentNurseNum] 				= 30*60;
			}
			else if( pplfParameter[0][i] == 3.0 )
			{

			}
			else
			{

			}
		}


		if( iGeneralWardNurseNum > iCurrentNurseNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iGeneralWardNurseNum-iCurrentNurseNum; i < iGeneralWardNurseNum; i++ )
			{
				iLocation = i % iCurrentNurseNum + iStartNurseLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				aiNurseCategory[i] 					= (int)pplfParameter[1][iLocation];
				aiNurseTriageProtocol[i] 			= (int)pplfParameter[2][iLocation];
				aiNurseTriageLevel[i] 				= (int)pplfParameter[3][iLocation];
				alfNurseTriageYearExperience[i]	 	= pplfParameter[4][iLocation];
				alfNurseYearExperience[i] 			= pplfParameter[5][iLocation];
				alfNurseConExperience[i] 			= pplfParameter[6][iLocation];
				alfNurseConTired1[i]			 	= pplfParameter[7][iLocation];
				alfNurseConTired2[i]			 	= pplfParameter[8][iLocation];
				alfNurseConTired3[i]				= pplfParameter[9][iLocation];
				alfNurseConTired4[i]			 	= pplfParameter[10][iLocation];
				alfNurseTiredRate[i]				= pplfParameter[11][iLocation];
				alfNurseAssociationRate[i]		 	= pplfParameter[12][iLocation];
				aiNurseDepartment[i]			 	= (int)pplfParameter[13][iLocation];
				alfNurseObservationTime[i]		 	= 30*60;
				alfNurseObservationProcessTime[i] 	= 30;
				alfNurseTriageTime[i] 				= 30*60;
				alfNurseExperienceRate1[i]			= pplfParameter[14][iLocation];
				alfNurseExperienceRate2[i]			= pplfParameter[15][iLocation];
				alfNurseConExperienceAIS[i]			= pplfParameter[16][iLocation];
				alfNurseExperienceRateAIS1[i]		= pplfParameter[17][iLocation];
				alfNurseExperienceRateAIS2[i]		= pplfParameter[18][iLocation];
				aiNurseRoomNumber[i]				= (int)pplfParameter[19][iLocation];
			}
		}

		for( i = 0;i < iGeneralWardRoomNum; i++ )
		{
			ArrayListGeneralWardRooms.add( new ERGeneralWardRoom() );
			ArrayListGeneralWardRooms.get(i).vSetRandom( sfmtRandom );
			ArrayListGeneralWardRooms.get(i).vSetSimulationEngine( engine );

			// 医師エージェントの作成
			ArrayListGeneralWardRooms.get(i).vCreateDoctorAgents( 1 );
			ArrayListGeneralWardRooms.get(i).vSetDoctorsRandom();
			ArrayListGeneralWardRooms.get(i).vSetDoctorAgentParameter( lfDoctorYearExperience,
					lfDoctorConExperience,
					lfDoctorExperienceRate1,
					lfDoctorExperienceRate2,
					lfDoctorConExperienceAIS,
					lfDoctorExperienceRateAIS1,
					lfDoctorExperienceRateAIS2,
					lfDoctorConTired1,
					lfDoctorConTired2,
					lfDoctorConTired3,
					lfDoctorConTired4,
					lfDoctorTiredRate,
					lfDoctorRevisedOperationRate,
					lfDoctorAssociationRate,
					lfDoctorConsultationTime,
					lfDoctorOperationTime,
					lfDoctorEmergencyTime,
					iDoctorDepartment,
					iDoctorRoomNumber );
			for( j = 0; j < ArrayListGeneralWardRooms.get(i).iGetDoctorAgentsNum(); j++ )
			{
				ArrayListGeneralWardRooms.get(i).cGetDoctorAgent(j).vSetSimulationEngine( engine );
				ArrayListGeneralWardRooms.get(i).cGetDoctorAgent(j).vSetSimulationEndTime( lfEndTime );
			}

			// 看護師エージェントの作成
			ArrayListGeneralWardRooms.get(i).vCreateNurseAgents( iGeneralWardNurseNum );
			ArrayListGeneralWardRooms.get(i).vSetNursesRandom();
			ArrayListGeneralWardRooms.get(i).vSetNurseAgentParameter(aiNurseCategory,
					  aiNurseTriageProtocol,
					  aiNurseTriageLevel,
					  alfNurseTriageYearExperience,
					  alfNurseYearExperience,
					  alfNurseConExperience,
					  alfNurseExperienceRate1,
					  alfNurseExperienceRate2,
					  alfNurseConExperienceAIS,
					  alfNurseExperienceRateAIS1,
					  alfNurseExperienceRateAIS2,
					  alfNurseConTired1,
					  alfNurseConTired2,
					  alfNurseConTired3,
					  alfNurseConTired4,
					  alfNurseTiredRate,
					  alfNurseAssociationRate,
					  alfNurseObservationTime,
					  alfNurseObservationProcessTime,
					  alfNurseTriageTime,
					  aiNurseDepartment);
			for( j = 0; j < ArrayListGeneralWardRooms.get(i).iGetNurseAgentsNum(); j++ )
			{
				ArrayListGeneralWardRooms.get(i).cGetNurseAgent(j).vSetSimulationEngine( engine );
				ArrayListGeneralWardRooms.get(i).cGetNurseAgent(j).vSetSimulationEndTime( lfEndTime );
			}
			ArrayListGeneralWardRooms.get(i).vCreatePatientAgents();
		}
	}

	/**
	 * <PRE>
	 *   X線室数及び構成する医療技師の数を設定します。
	 * </PRE>
	 *
	 * @param iExaminationRoomNum				検査室エージェント数
	 * @param strPath							X線室に所属する医師、看護師のパラメータが記述されたファイルパス
	 * @param engine							FUSEエンジン
	 * @param sfmtRandom						メルセンヌツイスターインスタンス
	 * @throws ERClinicalEngineerAgentException	医療技師処理中の例外
	 * @throws IOException						ファイル処理中の例外
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	private void vSetExaminationXRayRooms(int iExaminationRoomNum, String strPath, SimulationEngine engine, utility.sfmt.Rand sfmtRandom ) throws ERClinicalEngineerAgentException, IOException
	{
		int i,j;
		int iCurrentClinicalEngineerNum = 0;
		int iStartClinicalEngineerLoc = 0;
		int iLocation = 0;

		double[] alfClinicalEngineerYearExperience;
		double[] alfClinicalEngineerConExperience;
		double[] alfClinicalEngineerConTired1;
		double[] alfClinicalEngineerConTired2;
		double[] alfClinicalEngineerConTired3;
		double[] alfClinicalEngineerConTired4;
		double[] alfClinicalEngineerTiredRate;
		double[] alfClinicalEngineerAssociationRate;
		double[] alfClinicalEngineerExaminationTime;
		int[] aiClinicalEngineerDepartment;
		int[] aiClinicalEngineerRoomNumber;
		double[] alfClinicalEngineerExperienceRate1;
		double[] alfClinicalEngineerExperienceRate2;
		double[] alfClinicalEngineerConExperienceAIS;
		double[] alfClinicalEngineerExperienceRateAIS1;
		double[] alfClinicalEngineerExperienceRateAIS2;

		int iRow = 0;
		int iColumn = 0;
		double[][] pplfParameter;

		CCsv csv = new CCsv();
		if( strPath == "" )
		{
			csv.vOpen( "./parameter/X線室.csv", "read" );
		}
		else
		{
			ERDepartmentLog.warning("X線室所属医療技師特性パラメータ" + "," + strPath);
			csv.vOpen( strPath, "read" );
		}
		csv.vGetRowColumn();

		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		pplfParameter = new double[iColumn][iRow];
		csv.vRead( pplfParameter );
		csv.vClose();
		// 医師数、看護師数、医療技師数を設定します。
		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			if( pplfParameter[0].length > 0 )
			{
				iXRayClinicalEngineerNum = 0;
				for( i = 0;i < pplfParameter[0].length; i++ )
				{
					if( pplfParameter[0][i] == 1.0 )
					{
					}
					else if( pplfParameter[0][i] == 2.0 )
					{
					}
					else if( pplfParameter[0][i] == 3.0 )
					{
						iXRayClinicalEngineerNum++;
					}
				}
			}
		}
		// 逆シミュレーションモードの場合
		else
		{
			// 部屋数が0以下の場合はすべて0にして終了する。
			if( this.iXRayRoomNum <= 0 )
			{
				this.iXRayRoomNum = 0;
				this.iXRayClinicalEngineerNum = 0;
				ArrayListExaminationXRayRooms = new ArrayList<ERExaminationXRayRoom>();
				return ;
			}
		}

		alfClinicalEngineerYearExperience			= new double[iXRayClinicalEngineerNum];
		alfClinicalEngineerConExperience			= new double[iXRayClinicalEngineerNum];
		alfClinicalEngineerExperienceRate1			= new double[iXRayClinicalEngineerNum];
		alfClinicalEngineerExperienceRate2			= new double[iXRayClinicalEngineerNum];
		alfClinicalEngineerConExperienceAIS			= new double[iXRayClinicalEngineerNum];
		alfClinicalEngineerExperienceRateAIS1		= new double[iXRayClinicalEngineerNum];
		alfClinicalEngineerExperienceRateAIS2		= new double[iXRayClinicalEngineerNum];
		alfClinicalEngineerConTired1				= new double[iXRayClinicalEngineerNum];
		alfClinicalEngineerConTired2				= new double[iXRayClinicalEngineerNum];
		alfClinicalEngineerConTired3				= new double[iXRayClinicalEngineerNum];
		alfClinicalEngineerConTired4				= new double[iXRayClinicalEngineerNum];
		alfClinicalEngineerTiredRate				= new double[iXRayClinicalEngineerNum];
		alfClinicalEngineerAssociationRate			= new double[iXRayClinicalEngineerNum];
		alfClinicalEngineerAssociationRate			= new double[iXRayClinicalEngineerNum];
		aiClinicalEngineerDepartment				= new int[iXRayClinicalEngineerNum];
		aiClinicalEngineerRoomNumber				= new int[iXRayClinicalEngineerNum];
		alfClinicalEngineerExaminationTime			= new double[iXRayClinicalEngineerNum];

		if( ArrayListExaminationXRayRooms == null )
		{
			ArrayListExaminationXRayRooms = new ArrayList<ERExaminationXRayRoom>();
		}

//		for( i = 0;i < iClinicalEngineerNum; i++ )
//		{
//			alfClinicalEngineerConExperience[i] = 0.5;
//			alfClinicalEngineerConTired[i] = 3;
//			alfClinicalEngineerExaminationTime[i] = 30*60;
//			aiClinicalEngineerDepartment[i] = 12;
//		}
		for( i = 0;i < iXRayClinicalEngineerNum; i++ )
		{
			alfClinicalEngineerYearExperience[i] 			= 10;
			alfClinicalEngineerConExperience[i] 			= 3;
			alfClinicalEngineerConTired1[i] 				= 3;
			alfClinicalEngineerConTired2[i] 				= 3;
			alfClinicalEngineerConTired3[i] 				= 3;
			alfClinicalEngineerConTired4[i] 				= 3;
			alfClinicalEngineerTiredRate[i] 				= 3.0;
			alfClinicalEngineerAssociationRate[i]	 		= 1.0;
			alfClinicalEngineerExaminationTime[i]			= 30*60;
			alfClinicalEngineerExperienceRate1[i]			= 2.1;
			alfClinicalEngineerExperienceRate2[i]			= 0.9;
			alfClinicalEngineerConExperienceAIS[i]			= 0.14;
			alfClinicalEngineerExperienceRateAIS1[i]		= 0.2;
			alfClinicalEngineerExperienceRateAIS2[i]		= 1.1;
			aiClinicalEngineerDepartment[i]					= 10;
			aiClinicalEngineerRoomNumber[i]					= 1;
		}

		for( i = 0;i < pplfParameter[0].length; i++ )
		{
			if( pplfParameter[0][i] == 1.0 )
			{
			}
			else if( pplfParameter[0][i] == 2.0 )
			{
			}
			else if( pplfParameter[0][i] == 3.0 )
			{
				if( iXRayClinicalEngineerNum <= iCurrentClinicalEngineerNum ) continue;
				if( iStartClinicalEngineerLoc == 0 ) iStartClinicalEngineerLoc = i;
				alfClinicalEngineerYearExperience[iCurrentClinicalEngineerNum] 				= pplfParameter[1][i];
				alfClinicalEngineerConExperience[iCurrentClinicalEngineerNum] 				= pplfParameter[2][i];
				alfClinicalEngineerConTired1[iCurrentClinicalEngineerNum]			 		= pplfParameter[3][i];
				alfClinicalEngineerConTired2[iCurrentClinicalEngineerNum]			 		= pplfParameter[4][i];
				alfClinicalEngineerConTired3[iCurrentClinicalEngineerNum]			 		= pplfParameter[5][i];
				alfClinicalEngineerConTired4[iCurrentClinicalEngineerNum]			 		= pplfParameter[6][i];
				alfClinicalEngineerTiredRate[iCurrentClinicalEngineerNum]					= pplfParameter[7][i];
				alfClinicalEngineerAssociationRate[iCurrentClinicalEngineerNum]			 	= pplfParameter[8][i];
				aiClinicalEngineerDepartment[iCurrentClinicalEngineerNum]			 		= (int)pplfParameter[9][i];
				alfClinicalEngineerExaminationTime[iCurrentClinicalEngineerNum] 			= 30*60;
				alfClinicalEngineerExperienceRate1[iCurrentClinicalEngineerNum]				= pplfParameter[10][i];
				alfClinicalEngineerExperienceRate2[iCurrentClinicalEngineerNum]				= pplfParameter[11][i];
				alfClinicalEngineerConExperienceAIS[iCurrentClinicalEngineerNum]			= pplfParameter[12][i];
				alfClinicalEngineerExperienceRateAIS1[iCurrentClinicalEngineerNum]			= pplfParameter[13][i];
				alfClinicalEngineerExperienceRateAIS2[iCurrentClinicalEngineerNum]			= pplfParameter[14][i];
				aiClinicalEngineerRoomNumber[iCurrentClinicalEngineerNum]			 		= (int)pplfParameter[15][i];
				iCurrentClinicalEngineerNum++;
			}
			else
			{

			}
		}

		if( iXRayClinicalEngineerNum > iCurrentClinicalEngineerNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iXRayClinicalEngineerNum-iCurrentClinicalEngineerNum; i < iXRayClinicalEngineerNum; i++ )
			{
				iLocation = i % iCurrentClinicalEngineerNum + iStartClinicalEngineerLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				alfClinicalEngineerYearExperience[i] 			= pplfParameter[1][iLocation];
				alfClinicalEngineerConExperience[i] 			= pplfParameter[2][iLocation];
				alfClinicalEngineerConTired1[i]			 		= pplfParameter[3][iLocation];
				alfClinicalEngineerConTired2[i]			 		= pplfParameter[4][iLocation];
				alfClinicalEngineerConTired3[i]			 		= pplfParameter[5][iLocation];
				alfClinicalEngineerConTired4[i]			 		= pplfParameter[6][iLocation];
				alfClinicalEngineerTiredRate[i]					= pplfParameter[7][iLocation];
				alfClinicalEngineerAssociationRate[i]		 	= pplfParameter[8][iLocation];
				aiClinicalEngineerDepartment[i]			 		= (int)pplfParameter[9][iLocation];
				alfClinicalEngineerExperienceRate1[i]			= pplfParameter[10][iLocation];
				alfClinicalEngineerExperienceRate2[i]			= pplfParameter[11][iLocation];
				alfClinicalEngineerConExperienceAIS[i]			= pplfParameter[12][iLocation];
				alfClinicalEngineerExperienceRateAIS1[i]		= pplfParameter[13][iLocation];
				alfClinicalEngineerExperienceRateAIS2[i]		= pplfParameter[14][iLocation];
				aiClinicalEngineerRoomNumber[i]			 		= (int)pplfParameter[15][iLocation];
			}
		}

		for( i = 0;i < iExaminationRoomNum; i++ )
		{
			ArrayListExaminationXRayRooms.add( new ERExaminationXRayRoom() );
			ArrayListExaminationXRayRooms.get(i).vSetRandom( sfmtRandom );
			ArrayListExaminationXRayRooms.get(i).vSetSimulationEngine( engine );

			// 医療技師エージェントの作成
			ArrayListExaminationXRayRooms.get(i).vCreateClinicalEngineerAgents( iXRayClinicalEngineerNum );
			ArrayListExaminationXRayRooms.get(i).vSetClinicalEngineersRandom();
			ArrayListExaminationXRayRooms.get(i).vSetClinicalEngineerAgentParameter( alfClinicalEngineerYearExperience,
					alfClinicalEngineerConExperience,
					alfClinicalEngineerExperienceRate1,
					alfClinicalEngineerExperienceRate2,
					alfClinicalEngineerConExperienceAIS,
					alfClinicalEngineerExperienceRateAIS1,
					alfClinicalEngineerExperienceRateAIS2,
					alfClinicalEngineerConTired1,
					alfClinicalEngineerConTired2,
					alfClinicalEngineerConTired3,
					alfClinicalEngineerConTired4,
					alfClinicalEngineerTiredRate,
					alfClinicalEngineerAssociationRate,
					alfClinicalEngineerExaminationTime,
					aiClinicalEngineerDepartment,
					aiClinicalEngineerRoomNumber );
			for( j = 0; j < ArrayListExaminationXRayRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationXRayRooms.get(i).cGetClinicalEngineerAgent(j).vSetSimulationEngine( engine );
				ArrayListExaminationXRayRooms.get(i).cGetClinicalEngineerAgent(j).vSetSimulationEndTime( lfEndTime );
			}
		}
	}

	/**
	 * <PRE>
	 *   CT線室数及び構成する医療技師の数を設定します。
	 * </PRE>
	 *
	 * @param iExaminationRoomNum				検査室エージェント数
	 * @param strPath							CT室に所属する医師、看護師のパラメータが記述されたファイルパス
	 * @param engine							FUSEエンジン
	 * @param sfmtRandom						メルセンヌツイスターインスタンス
	 * @throws ERClinicalEngineerAgentException	医療技師処理中の例外
	 * @throws IOException						ファイル処理中の例外
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	private void vSetExaminationCTRooms(int iExaminationRoomNum, String strPath, SimulationEngine engine, utility.sfmt.Rand sfmtRandom ) throws ERClinicalEngineerAgentException, IOException
	{
		int i,j;
		int iCurrentClinicalEngineerNum = 0;
		int iStartClinicalEngineerLoc = 0;
		int iLocation = 0;

		double[] alfClinicalEngineerYearExperience;
		double[] alfClinicalEngineerConExperience;
		double[] alfClinicalEngineerConTired1;
		double[] alfClinicalEngineerConTired2;
		double[] alfClinicalEngineerConTired3;
		double[] alfClinicalEngineerConTired4;
		double[] alfClinicalEngineerTiredRate;
		double[] alfClinicalEngineerAssociationRate;
		double[] alfClinicalEngineerExaminationTime;
		double[] alfClinicalEngineerExperienceRate1;
		double[] alfClinicalEngineerExperienceRate2;
		double[] alfClinicalEngineerConExperienceAIS;
		double[] alfClinicalEngineerExperienceRateAIS1;
		double[] alfClinicalEngineerExperienceRateAIS2;
		int[] aiClinicalEngineerDepartment;
		int[] aiClinicalEngineerRoomNumber;

		int iRow = 0;
		int iColumn = 0;
		double[][] pplfParameter;

		CCsv csv = new CCsv();
		if( strPath == "" )
		{
			csv.vOpen( "./parameter/CT室.csv", "read" );
		}
		else
		{
			csv.vOpen( strPath, "read" );
		}
		csv.vGetRowColumn();

		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		pplfParameter = new double[iColumn][iRow];
		csv.vRead( pplfParameter );
		csv.vClose();
		// 医師数、看護師数、医療技師数を設定します。
		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			if( pplfParameter[0].length > 0 )
			{
				iCTClinicalEngineerNum = 0;
				for( i = 0;i < pplfParameter[0].length; i++ )
				{
					if( pplfParameter[0][i] == 1.0 )
					{
					}
					else if( pplfParameter[0][i] == 2.0 )
					{
					}
					else if( pplfParameter[0][i] == 3.0 )
					{
						iCTClinicalEngineerNum++;
					}
				}
			}
		}
		// 逆シミュレーションモードの場合
		else
		{
			// 部屋数が0以下の場合はすべて0にして終了する。
			if( this.iCTRoomNum <= 0 )
			{
				this.iCTRoomNum = 0;
				this.iCTClinicalEngineerNum = 0;
				ArrayListExaminationCTRooms = new ArrayList<ERExaminationCTRoom>();
				return ;
			}
		}

		alfClinicalEngineerYearExperience			= new double[iCTClinicalEngineerNum];
		alfClinicalEngineerConExperience			= new double[iCTClinicalEngineerNum];
		alfClinicalEngineerExperienceRate1			= new double[iCTClinicalEngineerNum];
		alfClinicalEngineerExperienceRate2			= new double[iCTClinicalEngineerNum];
		alfClinicalEngineerConExperienceAIS			= new double[iCTClinicalEngineerNum];
		alfClinicalEngineerExperienceRateAIS1		= new double[iCTClinicalEngineerNum];
		alfClinicalEngineerExperienceRateAIS2		= new double[iCTClinicalEngineerNum];
		alfClinicalEngineerConTired1				= new double[iCTClinicalEngineerNum];
		alfClinicalEngineerConTired2				= new double[iCTClinicalEngineerNum];
		alfClinicalEngineerConTired3				= new double[iCTClinicalEngineerNum];
		alfClinicalEngineerConTired4				= new double[iCTClinicalEngineerNum];
		alfClinicalEngineerTiredRate				= new double[iCTClinicalEngineerNum];
		alfClinicalEngineerAssociationRate			= new double[iCTClinicalEngineerNum];
		alfClinicalEngineerAssociationRate			= new double[iCTClinicalEngineerNum];
		aiClinicalEngineerDepartment				= new int[iCTClinicalEngineerNum];
		aiClinicalEngineerRoomNumber				= new int[iCTClinicalEngineerNum];
		alfClinicalEngineerExaminationTime			= new double[iCTClinicalEngineerNum];

		if( ArrayListExaminationCTRooms == null )
		{
			ArrayListExaminationCTRooms = new ArrayList<ERExaminationCTRoom>();
		}

//		for( i = 0;i < iClinicalEngineerNum; i++ )
//		{
//			alfClinicalEngineerConExperience[i] = 0.5;
//			alfClinicalEngineerConTired[i] = 3;
//			alfClinicalEngineerExaminationTime[i] = 30*60;
//			aiClinicalEngineerDepartment[i] = 12;
//		}
		for( i = 0;i < iCTClinicalEngineerNum; i++ )
		{
			alfClinicalEngineerYearExperience[i] 			= 10;
			alfClinicalEngineerConExperience[i] 			= 3;
			alfClinicalEngineerConTired1[i] 				= 3;
			alfClinicalEngineerConTired2[i] 				= 3;
			alfClinicalEngineerConTired3[i] 				= 3;
			alfClinicalEngineerConTired4[i] 				= 3;
			alfClinicalEngineerTiredRate[i] 				= 3.0;
			alfClinicalEngineerAssociationRate[i]	 		= 1.0;
			alfClinicalEngineerExaminationTime[i]			= 30*60;
			alfClinicalEngineerExperienceRate1[i]			= 2.1;
			alfClinicalEngineerExperienceRate2[i]			= 0.9;
			alfClinicalEngineerConExperienceAIS[i]			= 0.14;
			alfClinicalEngineerExperienceRateAIS1[i]		= 0.2;
			alfClinicalEngineerExperienceRateAIS2[i]		= 1.1;
			aiClinicalEngineerDepartment[i]					= 12;
			aiClinicalEngineerRoomNumber[i]					= 1;
		}

		for( i = 0;i < pplfParameter[0].length; i++ )
		{
			if( pplfParameter[0][i] == 1.0 )
			{
			}
			else if( pplfParameter[0][i] == 2.0 )
			{
			}
			else if( pplfParameter[0][i] == 3.0 )
			{
				if( iCTClinicalEngineerNum <= iCurrentClinicalEngineerNum ) continue;
				if( iStartClinicalEngineerLoc == 0 ) iStartClinicalEngineerLoc = i;
				alfClinicalEngineerYearExperience[iCurrentClinicalEngineerNum] 				= pplfParameter[1][i];
				alfClinicalEngineerConExperience[iCurrentClinicalEngineerNum] 				= pplfParameter[2][i];
				alfClinicalEngineerConTired1[iCurrentClinicalEngineerNum]			 		= pplfParameter[3][i];
				alfClinicalEngineerConTired2[iCurrentClinicalEngineerNum]			 		= pplfParameter[4][i];
				alfClinicalEngineerConTired3[iCurrentClinicalEngineerNum]			 		= pplfParameter[5][i];
				alfClinicalEngineerConTired4[iCurrentClinicalEngineerNum]			 		= pplfParameter[6][i];
				alfClinicalEngineerTiredRate[iCurrentClinicalEngineerNum]					= pplfParameter[7][i];
				alfClinicalEngineerAssociationRate[iCurrentClinicalEngineerNum]			 	= pplfParameter[8][i];
				aiClinicalEngineerDepartment[iCurrentClinicalEngineerNum]			 		= (int)pplfParameter[9][i];
				alfClinicalEngineerExaminationTime[iCurrentClinicalEngineerNum] 			= 30*60;
				alfClinicalEngineerExperienceRate1[iCurrentClinicalEngineerNum]				= pplfParameter[10][i];
				alfClinicalEngineerExperienceRate2[iCurrentClinicalEngineerNum]				= pplfParameter[11][i];
				alfClinicalEngineerConExperienceAIS[iCurrentClinicalEngineerNum]			= pplfParameter[12][i];
				alfClinicalEngineerExperienceRateAIS1[iCurrentClinicalEngineerNum]			= pplfParameter[13][i];
				alfClinicalEngineerExperienceRateAIS2[iCurrentClinicalEngineerNum]			= pplfParameter[14][i];
				aiClinicalEngineerRoomNumber[iCurrentClinicalEngineerNum]			 		= (int)pplfParameter[15][i];
				iCurrentClinicalEngineerNum++;
			}
			else
			{

			}
		}

		if( iCTClinicalEngineerNum > iCurrentClinicalEngineerNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iCTClinicalEngineerNum-iCurrentClinicalEngineerNum; i < iCTClinicalEngineerNum; i++ )
			{
				iLocation = i % iCurrentClinicalEngineerNum + iStartClinicalEngineerLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				alfClinicalEngineerYearExperience[i] 			= pplfParameter[1][iLocation];
				alfClinicalEngineerConExperience[i] 			= pplfParameter[2][iLocation];
				alfClinicalEngineerConTired1[i]			 		= pplfParameter[3][iLocation];
				alfClinicalEngineerConTired2[i]			 		= pplfParameter[4][iLocation];
				alfClinicalEngineerConTired3[i]			 		= pplfParameter[5][iLocation];
				alfClinicalEngineerConTired4[i]			 		= pplfParameter[6][iLocation];
				alfClinicalEngineerTiredRate[i]					= pplfParameter[7][iLocation];
				alfClinicalEngineerAssociationRate[i]		 	= pplfParameter[8][iLocation];
				aiClinicalEngineerDepartment[i]			 		= (int)pplfParameter[9][iLocation];
				alfClinicalEngineerExperienceRate1[i]			= pplfParameter[10][iLocation];
				alfClinicalEngineerExperienceRate2[i]			= pplfParameter[11][iLocation];
				alfClinicalEngineerConExperienceAIS[i]			= pplfParameter[12][iLocation];
				alfClinicalEngineerExperienceRateAIS1[i]		= pplfParameter[13][iLocation];
				alfClinicalEngineerExperienceRateAIS2[i]		= pplfParameter[14][iLocation];
				aiClinicalEngineerRoomNumber[i]			 		= (int)pplfParameter[15][iLocation];
			}
		}

		for( i = 0;i < iExaminationRoomNum; i++ )
		{
			ArrayListExaminationCTRooms.add( new ERExaminationCTRoom() );
			ArrayListExaminationCTRooms.get(i).vSetSimulationEngine( engine );
			ArrayListExaminationCTRooms.get(i).vSetRandom( sfmtRandom );

			// 医療技師エージェントの作成
			ArrayListExaminationCTRooms.get(i).vCreateClinicalEngineerAgents( iCTClinicalEngineerNum );
			ArrayListExaminationCTRooms.get(i).vSetClinicalEngineersRandom();
			ArrayListExaminationCTRooms.get(i).vSetClinicalEngineerAgentParameter( alfClinicalEngineerYearExperience,
					alfClinicalEngineerConExperience,
					alfClinicalEngineerExperienceRate1,
					alfClinicalEngineerExperienceRate2,
					alfClinicalEngineerConExperienceAIS,
					alfClinicalEngineerExperienceRateAIS1,
					alfClinicalEngineerExperienceRateAIS2,
					alfClinicalEngineerConTired1,
					alfClinicalEngineerConTired2,
					alfClinicalEngineerConTired3,
					alfClinicalEngineerConTired4,
					alfClinicalEngineerTiredRate,
					alfClinicalEngineerAssociationRate,
					alfClinicalEngineerExaminationTime,
					aiClinicalEngineerDepartment,
					aiClinicalEngineerRoomNumber );
			for( j = 0; j < ArrayListExaminationCTRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationCTRooms.get(i).cGetClinicalEngineerAgent(j).vSetSimulationEngine( engine );
				ArrayListExaminationCTRooms.get(i).cGetClinicalEngineerAgent(j).vSetSimulationEndTime( lfEndTime );
			}
		}
	}

	/**
	 * <PRE>
	 *   MRI室数及び構成する医療技師の数を設定します。
	 * </PRE>
	 *
	 * @param iExaminationRoomNum				検査室エージェント数
	 * @param strPath							MRI室に所属する医師、看護師のパラメータが記述されたファイルパス
	 * @param engine							FUSEエンジン
	 * @param sfmtRandom						メルセンヌツイスターインスタンス
	 * @throws ERClinicalEngineerAgentException	医療技師処理中の例外
	 * @throws IOException						ファイル処理中の例外
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	private void vSetExaminationMRIRooms(int iExaminationRoomNum, String strPath, SimulationEngine engine, utility.sfmt.Rand sfmtRandom ) throws ERClinicalEngineerAgentException, IOException
	{
		int i,j;
		int iCurrentClinicalEngineerNum = 0;
		int iStartClinicalEngineerLoc = 0;
		int iLocation = 0;

		double[] alfClinicalEngineerYearExperience;
		double[] alfClinicalEngineerConExperience;
		double[] alfClinicalEngineerConTired1;
		double[] alfClinicalEngineerConTired2;
		double[] alfClinicalEngineerConTired3;
		double[] alfClinicalEngineerConTired4;
		double[] alfClinicalEngineerTiredRate;
		double[] alfClinicalEngineerAssociationRate;
		double[] alfClinicalEngineerExaminationTime;
		double[] alfClinicalEngineerExperienceRate1;
		double[] alfClinicalEngineerExperienceRate2;
		double[] alfClinicalEngineerConExperienceAIS;
		double[] alfClinicalEngineerExperienceRateAIS1;
		double[] alfClinicalEngineerExperienceRateAIS2;
		int[] aiClinicalEngineerDepartment;
		int[] aiClinicalEngineerRoomNumber;

		int iRow = 0;
		int iColumn = 0;
		double[][] pplfParameter;

		CCsv csv = new CCsv();
		if( strPath == "" )
		{
			csv.vOpen( "./parameter/MRI室.csv", "read" );
		}
		else
		{
			csv.vOpen( strPath, "read" );
		}
		csv.vGetRowColumn();

		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		pplfParameter = new double[iColumn][iRow];
		csv.vRead( pplfParameter );
		csv.vClose();
		// 医師数、看護師数、医療技師数を設定します。
		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			if( pplfParameter[0].length > 0 )
			{
				iMRIClinicalEngineerNum = 0;
				for( i = 0;i < pplfParameter[0].length; i++ )
				{
					if( pplfParameter[0][i] == 1.0 )
					{
					}
					else if( pplfParameter[0][i] == 2.0 )
					{
					}
					else if( pplfParameter[0][i] == 3.0 )
					{
						iMRIClinicalEngineerNum++;
					}
				}
			}
		}
		// 逆シミュレーションモードの場合
		else
		{
			// 部屋数が0以下の場合はすべて0にして終了する。
			if( this.iMRIRoomNum <= 0 )
			{
				this.iMRIRoomNum = 0;
				this.iMRIClinicalEngineerNum = 0;
				ArrayListExaminationMRIRooms = new ArrayList<ERExaminationMRIRoom>();
				return ;
			}
		}


		alfClinicalEngineerYearExperience			= new double[iMRIClinicalEngineerNum];
		alfClinicalEngineerConExperience			= new double[iMRIClinicalEngineerNum];
		alfClinicalEngineerExperienceRate1			= new double[iMRIClinicalEngineerNum];
		alfClinicalEngineerExperienceRate2			= new double[iMRIClinicalEngineerNum];
		alfClinicalEngineerConExperienceAIS			= new double[iMRIClinicalEngineerNum];
		alfClinicalEngineerExperienceRateAIS1		= new double[iMRIClinicalEngineerNum];
		alfClinicalEngineerExperienceRateAIS2		= new double[iMRIClinicalEngineerNum];
		alfClinicalEngineerConTired1				= new double[iMRIClinicalEngineerNum];
		alfClinicalEngineerConTired2				= new double[iMRIClinicalEngineerNum];
		alfClinicalEngineerConTired3				= new double[iMRIClinicalEngineerNum];
		alfClinicalEngineerConTired4				= new double[iMRIClinicalEngineerNum];
		alfClinicalEngineerTiredRate				= new double[iMRIClinicalEngineerNum];
		alfClinicalEngineerAssociationRate			= new double[iMRIClinicalEngineerNum];
		alfClinicalEngineerAssociationRate			= new double[iMRIClinicalEngineerNum];
		aiClinicalEngineerDepartment				= new int[iMRIClinicalEngineerNum];
		aiClinicalEngineerRoomNumber				= new int[iMRIClinicalEngineerNum];
		alfClinicalEngineerExaminationTime			= new double[iMRIClinicalEngineerNum];

		if( ArrayListExaminationMRIRooms == null )
		{
			ArrayListExaminationMRIRooms = new ArrayList<ERExaminationMRIRoom>();
		}

//		for( i = 0;i < iClinicalEngineerNum; i++ )
//		{
//			alfClinicalEngineerConExperience[i] = 0.5;
//			alfClinicalEngineerConTired[i] = 3;
//			alfClinicalEngineerExaminationTime[i] = 30*60;
//			aiClinicalEngineerDepartment[i] = 12;
//		}
		for( i = 0;i < iMRIClinicalEngineerNum; i++ )
		{
			alfClinicalEngineerYearExperience[i] 			= 10;
			alfClinicalEngineerConExperience[i] 			= 3;
			alfClinicalEngineerConTired1[i] 				= 3;
			alfClinicalEngineerConTired2[i] 				= 3;
			alfClinicalEngineerConTired3[i] 				= 3;
			alfClinicalEngineerConTired4[i] 				= 3;
			alfClinicalEngineerTiredRate[i] 				= 3.0;
			alfClinicalEngineerAssociationRate[i]	 		= 1.0;
			alfClinicalEngineerExaminationTime[i]			= 30*60;
			aiClinicalEngineerDepartment[i]					= 13;
			alfClinicalEngineerExperienceRate1[i]			= 2.1;
			alfClinicalEngineerExperienceRate2[i]			= 0.9;
			alfClinicalEngineerConExperienceAIS[i]			= 0.14;
			alfClinicalEngineerExperienceRateAIS1[i]		= 0.2;
			alfClinicalEngineerExperienceRateAIS2[i]		= 1.1;
			aiClinicalEngineerRoomNumber[i]					= 1;
		}

		for( i = 0;i < pplfParameter[0].length; i++ )
		{
			if( pplfParameter[0][i] == 1.0 )
			{
			}
			else if( pplfParameter[0][i] == 2.0 )
			{
			}
			else if( pplfParameter[0][i] == 3.0 )
			{
				if( iMRIClinicalEngineerNum <= iCurrentClinicalEngineerNum ) continue;
				if( iStartClinicalEngineerLoc == 0 ) iStartClinicalEngineerLoc = i;
				alfClinicalEngineerYearExperience[iCurrentClinicalEngineerNum] 				= pplfParameter[1][i];
				alfClinicalEngineerConExperience[iCurrentClinicalEngineerNum] 				= pplfParameter[2][i];
				alfClinicalEngineerConTired1[iCurrentClinicalEngineerNum]			 		= pplfParameter[3][i];
				alfClinicalEngineerConTired2[iCurrentClinicalEngineerNum]			 		= pplfParameter[4][i];
				alfClinicalEngineerConTired3[iCurrentClinicalEngineerNum]			 		= pplfParameter[5][i];
				alfClinicalEngineerConTired4[iCurrentClinicalEngineerNum]			 		= pplfParameter[6][i];
				alfClinicalEngineerTiredRate[iCurrentClinicalEngineerNum]					= pplfParameter[7][i];
				alfClinicalEngineerAssociationRate[iCurrentClinicalEngineerNum]			 	= pplfParameter[8][i];
				aiClinicalEngineerDepartment[iCurrentClinicalEngineerNum]			 		= (int)pplfParameter[9][i];
				alfClinicalEngineerExaminationTime[i] 										= 30*60;
				alfClinicalEngineerExperienceRate1[i]										= pplfParameter[10][i];
				alfClinicalEngineerExperienceRate2[i]										= pplfParameter[11][i];
				alfClinicalEngineerConExperienceAIS[i]										= pplfParameter[12][i];
				alfClinicalEngineerExperienceRateAIS1[i]									= pplfParameter[13][i];
				alfClinicalEngineerExperienceRateAIS2[i]									= pplfParameter[14][i];
				aiClinicalEngineerRoomNumber[i]												= (int)pplfParameter[15][i];
				iCurrentClinicalEngineerNum++;
			}
			else
			{

			}
		}

		if( iMRIClinicalEngineerNum > iCurrentClinicalEngineerNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iMRIClinicalEngineerNum-iCurrentClinicalEngineerNum; i < iMRIClinicalEngineerNum; i++ )
			{
				iLocation = i % iCurrentClinicalEngineerNum + iStartClinicalEngineerLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				alfClinicalEngineerYearExperience[i] 			= pplfParameter[1][iLocation];
				alfClinicalEngineerConExperience[i] 			= pplfParameter[2][iLocation];
				alfClinicalEngineerConTired1[i]			 		= pplfParameter[3][iLocation];
				alfClinicalEngineerConTired2[i]			 		= pplfParameter[4][iLocation];
				alfClinicalEngineerConTired3[i]			 		= pplfParameter[5][iLocation];
				alfClinicalEngineerConTired4[i]			 		= pplfParameter[6][iLocation];
				alfClinicalEngineerTiredRate[i]					= pplfParameter[7][iLocation];
				alfClinicalEngineerAssociationRate[i]		 	= pplfParameter[8][iLocation];
				aiClinicalEngineerDepartment[i]			 		= (int)pplfParameter[9][iLocation];
				alfClinicalEngineerExperienceRate1[i]			= pplfParameter[10][iLocation];
				alfClinicalEngineerExperienceRate2[i]			= pplfParameter[11][iLocation];
				alfClinicalEngineerConExperienceAIS[i]			= pplfParameter[12][iLocation];
				alfClinicalEngineerExperienceRateAIS1[i]		= pplfParameter[13][iLocation];
				alfClinicalEngineerExperienceRateAIS2[i]		= pplfParameter[14][iLocation];
				aiClinicalEngineerRoomNumber[i]			 		= (int)pplfParameter[15][iLocation];
			}
		}

		for( i = 0;i < iExaminationRoomNum; i++ )
		{
			ArrayListExaminationMRIRooms.add( new ERExaminationMRIRoom() );
			ArrayListExaminationMRIRooms.get(i).vSetSimulationEngine( engine );
			ArrayListExaminationMRIRooms.get(i).vSetRandom( sfmtRandom );

			// 医療技師エージェントの作成
			ArrayListExaminationMRIRooms.get(i).vCreateClinicalEngineerAgents( iMRIClinicalEngineerNum );
			ArrayListExaminationMRIRooms.get(i).vSetClinicalEngineersRandom();
			ArrayListExaminationMRIRooms.get(i).vSetClinicalEngineerAgentParameter( alfClinicalEngineerYearExperience,
					alfClinicalEngineerConExperience,
					alfClinicalEngineerExperienceRate1,
					alfClinicalEngineerExperienceRate2,
					alfClinicalEngineerConExperienceAIS,
					alfClinicalEngineerExperienceRateAIS1,
					alfClinicalEngineerExperienceRateAIS2,
					alfClinicalEngineerConTired1,
					alfClinicalEngineerConTired2,
					alfClinicalEngineerConTired3,
					alfClinicalEngineerConTired4,
					alfClinicalEngineerTiredRate,
					alfClinicalEngineerAssociationRate,
					alfClinicalEngineerExaminationTime,
					aiClinicalEngineerDepartment,
					aiClinicalEngineerRoomNumber );
			for( j = 0; j < ArrayListExaminationMRIRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationMRIRooms.get(i).cGetClinicalEngineerAgent(j).vSetSimulationEngine( engine );
				ArrayListExaminationMRIRooms.get(i).cGetClinicalEngineerAgent(j).vSetSimulationEndTime( lfEndTime );
			}
		}
	}

	/**
	 * <PRE>
	 *   血管造影室数及び構成する医療技師の数を設定します。
	 * </PRE>
	 *
	 * @param iExaminationRoomNum		検査室エージェント数
	 * @param strPath					血管造影室に所属する医師、看護師のパラメータが記述されたファイルパス
	 * @param engine					FUSEエンジン
	 * @param sfmtRandom							メルセンヌツイスターインスタンス
	 * @throws ERClinicalEngineerAgentException		医療技師処理中の例外
	 * @throws IOException							ファイル処理中の例外
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	private void vSetExaminationAngiographyRooms(int iExaminationRoomNum, String strPath, SimulationEngine engine, utility.sfmt.Rand sfmtRandom ) throws ERClinicalEngineerAgentException, IOException
	{
		int i,j;
		int iCurrentClinicalEngineerNum = 0;
		int iStartClinicalEngineerLoc = 0;
		int iLocation = 0;

		double[] alfClinicalEngineerYearExperience;
		double[] alfClinicalEngineerConExperience;
		double[] alfClinicalEngineerConTired1;
		double[] alfClinicalEngineerConTired2;
		double[] alfClinicalEngineerConTired3;
		double[] alfClinicalEngineerConTired4;
		double[] alfClinicalEngineerTiredRate;
		double[] alfClinicalEngineerAssociationRate;
		double[] alfClinicalEngineerExaminationTime;
		double[] alfClinicalEngineerExperienceRate1;
		double[] alfClinicalEngineerExperienceRate2;
		double[] alfClinicalEngineerConExperienceAIS;
		double[] alfClinicalEngineerExperienceRateAIS1;
		double[] alfClinicalEngineerExperienceRateAIS2;
		int[] aiClinicalEngineerDepartment;
		int[] aiClinicalEngineerRoomNumber;

		int iRow = 0;
		int iColumn = 0;
		double[][] pplfParameter;

		CCsv csv = new CCsv();
		if( strPath == "" )
		{
			csv.vOpen( "./parameter/血管造影室.csv", "read" );
		}
		else
		{
			csv.vOpen( strPath, "read" );
		}
		csv.vGetRowColumn();

		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		pplfParameter = new double[iColumn][iRow];
		csv.vRead( pplfParameter );
		csv.vClose();
		// 医師数、看護師数、医療技師数を設定します。
		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			if( pplfParameter[0].length > 0 )
			{
				iAngiographyClinicalEngineerNum = 0;
				for( i = 0;i < pplfParameter[0].length; i++ )
				{
					if( pplfParameter[0][i] == 1.0 )
					{
					}
					else if( pplfParameter[0][i] == 2.0 )
					{
					}
					else if( pplfParameter[0][i] == 3.0 )
					{
						iAngiographyClinicalEngineerNum++;
					}
				}
			}
		}
		// 逆シミュレーションモードの場合
		else
		{
			// 部屋数が0以下の場合はすべて0にして終了する。
			if( this.iAngiographyRoomNum <= 0 )
			{
				this.iAngiographyRoomNum = 0;
				this.iAngiographyClinicalEngineerNum = 0;
				ArrayListExaminationAngiographyRooms = new ArrayList<ERExaminationAngiographyRoom>();
				return ;
			}
		}

		alfClinicalEngineerYearExperience			= new double[iAngiographyClinicalEngineerNum];
		alfClinicalEngineerConExperience			= new double[iAngiographyClinicalEngineerNum];
		alfClinicalEngineerExperienceRate1			= new double[iAngiographyClinicalEngineerNum];
		alfClinicalEngineerExperienceRate2			= new double[iAngiographyClinicalEngineerNum];
		alfClinicalEngineerConExperienceAIS			= new double[iAngiographyClinicalEngineerNum];
		alfClinicalEngineerExperienceRateAIS1		= new double[iAngiographyClinicalEngineerNum];
		alfClinicalEngineerExperienceRateAIS2		= new double[iAngiographyClinicalEngineerNum];
		alfClinicalEngineerConTired1				= new double[iAngiographyClinicalEngineerNum];
		alfClinicalEngineerConTired2				= new double[iAngiographyClinicalEngineerNum];
		alfClinicalEngineerConTired3				= new double[iAngiographyClinicalEngineerNum];
		alfClinicalEngineerConTired4				= new double[iAngiographyClinicalEngineerNum];
		alfClinicalEngineerTiredRate				= new double[iAngiographyClinicalEngineerNum];
		alfClinicalEngineerAssociationRate			= new double[iAngiographyClinicalEngineerNum];
		alfClinicalEngineerAssociationRate			= new double[iAngiographyClinicalEngineerNum];
		aiClinicalEngineerDepartment				= new int[iAngiographyClinicalEngineerNum];
		aiClinicalEngineerRoomNumber				= new int[iAngiographyClinicalEngineerNum];
		alfClinicalEngineerExaminationTime			= new double[iAngiographyClinicalEngineerNum];

		if( ArrayListExaminationAngiographyRooms == null )
		{
			ArrayListExaminationAngiographyRooms = new ArrayList<ERExaminationAngiographyRoom>();
		}

//		for( i = 0;i < iClinicalEngineerNum; i++ )
//		{
//			alfClinicalEngineerConExperience[i] = 0.5;
//			alfClinicalEngineerConTired[i] = 3;
//			alfClinicalEngineerExaminationTime[i] = 30*60;
//			aiClinicalEngineerDepartment[i] = 12;
//		}
		for( i = 0;i < iAngiographyClinicalEngineerNum; i++ )
		{
			alfClinicalEngineerYearExperience[i] 			= 10;
			alfClinicalEngineerConExperience[i] 			= 3;
			alfClinicalEngineerConTired1[i] 				= 3;
			alfClinicalEngineerConTired2[i] 				= 3;
			alfClinicalEngineerConTired3[i] 				= 3;
			alfClinicalEngineerConTired4[i] 				= 3;
			alfClinicalEngineerTiredRate[i] 				= 3.0;
			alfClinicalEngineerAssociationRate[i]	 		= 1.0;
			alfClinicalEngineerExaminationTime[i]			= 30*60;
			aiClinicalEngineerDepartment[i]					= 14;
			alfClinicalEngineerExperienceRate1[i]			= 2.1;
			alfClinicalEngineerExperienceRate2[i]			= 0.9;
			alfClinicalEngineerConExperienceAIS[i]			= 0.14;
			alfClinicalEngineerExperienceRateAIS1[i]		= 0.2;
			alfClinicalEngineerExperienceRateAIS2[i]		= 1.1;
			aiClinicalEngineerRoomNumber[i]					= 1;
		}

		for( i = 0;i < pplfParameter[0].length; i++ )
		{
			if( pplfParameter[0][i] == 1.0 )
			{
			}
			else if( pplfParameter[0][i] == 2.0 )
			{
			}
			else if( pplfParameter[0][i] == 3.0 )
			{
				if( iAngiographyClinicalEngineerNum <= iCurrentClinicalEngineerNum ) continue;
				if( iStartClinicalEngineerLoc == 0 ) iStartClinicalEngineerLoc = i;
				alfClinicalEngineerYearExperience[iCurrentClinicalEngineerNum] 				= pplfParameter[1][i];
				alfClinicalEngineerConExperience[iCurrentClinicalEngineerNum] 				= pplfParameter[2][i];
				alfClinicalEngineerConTired1[iCurrentClinicalEngineerNum]			 		= pplfParameter[3][i];
				alfClinicalEngineerConTired2[iCurrentClinicalEngineerNum]			 		= pplfParameter[4][i];
				alfClinicalEngineerConTired3[iCurrentClinicalEngineerNum]			 		= pplfParameter[5][i];
				alfClinicalEngineerConTired4[iCurrentClinicalEngineerNum]			 		= pplfParameter[6][i];
				alfClinicalEngineerTiredRate[iCurrentClinicalEngineerNum]					= pplfParameter[7][i];
				alfClinicalEngineerAssociationRate[iCurrentClinicalEngineerNum]			 	= pplfParameter[8][i];
				aiClinicalEngineerDepartment[iCurrentClinicalEngineerNum]			 		= (int)pplfParameter[9][i];
				alfClinicalEngineerExaminationTime[i] 										= 30*60;
				alfClinicalEngineerExperienceRate1[i]										= pplfParameter[10][i];
				alfClinicalEngineerExperienceRate2[i]										= pplfParameter[11][i];
				alfClinicalEngineerConExperienceAIS[i]										= pplfParameter[12][i];
				alfClinicalEngineerExperienceRateAIS1[i]									= pplfParameter[13][i];
				alfClinicalEngineerExperienceRateAIS2[i]									= pplfParameter[14][i];
				aiClinicalEngineerRoomNumber[i]												= (int)pplfParameter[15][i];
				iCurrentClinicalEngineerNum++;
			}
			else
			{

			}
		}

		if( iAngiographyClinicalEngineerNum > iCurrentClinicalEngineerNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iAngiographyClinicalEngineerNum-iCurrentClinicalEngineerNum; i < iAngiographyClinicalEngineerNum; i++ )
			{
				iLocation = i % iCurrentClinicalEngineerNum + iStartClinicalEngineerLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				alfClinicalEngineerYearExperience[i] 			= pplfParameter[1][iLocation];
				alfClinicalEngineerConExperience[i] 			= pplfParameter[2][iLocation];
				alfClinicalEngineerConTired1[i]			 		= pplfParameter[3][iLocation];
				alfClinicalEngineerConTired2[i]			 		= pplfParameter[4][iLocation];
				alfClinicalEngineerConTired3[i]			 		= pplfParameter[5][iLocation];
				alfClinicalEngineerConTired4[i]			 		= pplfParameter[6][iLocation];
				alfClinicalEngineerTiredRate[i]					= pplfParameter[7][iLocation];
				alfClinicalEngineerAssociationRate[i]		 	= pplfParameter[8][iLocation];
				aiClinicalEngineerDepartment[i]			 		= (int)pplfParameter[9][iLocation];
				alfClinicalEngineerExperienceRate1[i]			= pplfParameter[10][iLocation];
				alfClinicalEngineerExperienceRate2[i]			= pplfParameter[11][iLocation];
				alfClinicalEngineerConExperienceAIS[i]			= pplfParameter[12][iLocation];
				alfClinicalEngineerExperienceRateAIS1[i]		= pplfParameter[13][iLocation];
				alfClinicalEngineerExperienceRateAIS2[i]		= pplfParameter[14][iLocation];
				aiClinicalEngineerRoomNumber[i]			 		= (int)pplfParameter[15][iLocation];
			}
		}

		for( i = 0;i < iExaminationRoomNum; i++ )
		{
			ArrayListExaminationAngiographyRooms.add( new ERExaminationAngiographyRoom() );
			ArrayListExaminationAngiographyRooms.get(i).vSetSimulationEngine( engine );
			ArrayListExaminationAngiographyRooms.get(i).vSetRandom( sfmtRandom );

			// 医療技師エージェントの作成
			ArrayListExaminationAngiographyRooms.get(i).vCreateClinicalEngineerAgents( iAngiographyClinicalEngineerNum );
			ArrayListExaminationAngiographyRooms.get(i).vSetClinicalEngineersRandom();
			ArrayListExaminationAngiographyRooms.get(i).vSetClinicalEngineerAgentParameter( alfClinicalEngineerYearExperience,
					alfClinicalEngineerConExperience,
					alfClinicalEngineerExperienceRate1,
					alfClinicalEngineerExperienceRate2,
					alfClinicalEngineerConExperienceAIS,
					alfClinicalEngineerExperienceRateAIS1,
					alfClinicalEngineerExperienceRateAIS2,
					alfClinicalEngineerConTired1,
					alfClinicalEngineerConTired2,
					alfClinicalEngineerConTired3,
					alfClinicalEngineerConTired4,
					alfClinicalEngineerTiredRate,
					alfClinicalEngineerAssociationRate,
					alfClinicalEngineerExaminationTime,
					aiClinicalEngineerDepartment,
					aiClinicalEngineerRoomNumber );
			for( j = 0; j < ArrayListExaminationAngiographyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationAngiographyRooms.get(i).cGetClinicalEngineerAgent(j).vSetSimulationEngine( engine );
				ArrayListExaminationAngiographyRooms.get(i).cGetClinicalEngineerAgent(j).vSetSimulationEndTime( lfEndTime );
			}
		}
	}

	/**
	 * <PRE>
	 *   Fast(超音波診断)室数及び構成する医療技師の数を設定します。
	 * </PRE>
	 *
	 * @param iExaminationRoomNum					検査室エージェント数
	 * @param strPath								FAST室に所属する医師、看護師のパラメータが記述されたファイルパス
	 * @param engine								FUSEエンジン
	 * @param sfmtRandom							メルセンヌツイスターインスタンス
	 * @throws ERClinicalEngineerAgentException		医療技師処理中の例外
	 * @throws IOException							ファイル処理中の例外
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	private void vSetExaminationFastRooms(int iExaminationRoomNum, String strPath, SimulationEngine engine, Rand sfmtRandom ) throws ERClinicalEngineerAgentException, IOException
	{
		int i,j;
		int iCurrentClinicalEngineerNum = 0;
		int iStartClinicalEngineerLoc = 0;
		int iLocation = 0;

		double[] alfClinicalEngineerYearExperience;
		double[] alfClinicalEngineerConExperience;
		double[] alfClinicalEngineerConTired1;
		double[] alfClinicalEngineerConTired2;
		double[] alfClinicalEngineerConTired3;
		double[] alfClinicalEngineerConTired4;
		double[] alfClinicalEngineerTiredRate;
		double[] alfClinicalEngineerAssociationRate;
		double[] alfClinicalEngineerExaminationTime;
		int[] aiClinicalEngineerDepartment;
		double[] alfClinicalEngineerExperienceRate1;
		double[] alfClinicalEngineerExperienceRate2;
		double[] alfClinicalEngineerConExperienceAIS;
		double[] alfClinicalEngineerExperienceRateAIS1;
		double[] alfClinicalEngineerExperienceRateAIS2;
		int[] aiClinicalEngineerRoomNumber;

		int iRow = 0;
		int iColumn = 0;
		double[][] pplfParameter;

		CCsv csv = new CCsv();
		if( strPath == "" )
		{
			csv.vOpen( "./parameter/FAST室.csv", "read" );
		}
		else
		{
			csv.vOpen( strPath, "read" );
		}
		csv.vGetRowColumn();

		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		pplfParameter = new double[iColumn][iRow];
		csv.vRead( pplfParameter );
		csv.vClose();
		// 医師数、看護師数、医療技師数を設定します。
		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			if( pplfParameter[0].length > 0 )
			{
				iFastClinicalEngineerNum = 0;
				for( i = 0;i < pplfParameter[0].length; i++ )
				{
					if( pplfParameter[0][i] == 1.0 )
					{
					}
					else if( pplfParameter[0][i] == 2.0 )
					{
					}
					else if( pplfParameter[0][i] == 3.0 )
					{
						iFastClinicalEngineerNum++;
					}
				}
			}
		}
		// 逆シミュレーションモードの場合
		else
		{
			// 部屋数が0以下の場合はすべて0にして終了する。
			if( this.iFastRoomNum <= 0 )
			{
				this.iFastRoomNum = 0;
				this.iFastClinicalEngineerNum = 0;
				ArrayListExaminationFastRooms = new ArrayList<ERExaminationFastRoom>();
				return ;
			}
		}

		alfClinicalEngineerYearExperience			= new double[iFastClinicalEngineerNum];
		alfClinicalEngineerConExperience			= new double[iFastClinicalEngineerNum];
		alfClinicalEngineerExperienceRate1			= new double[iFastClinicalEngineerNum];
		alfClinicalEngineerExperienceRate2			= new double[iFastClinicalEngineerNum];
		alfClinicalEngineerConExperienceAIS			= new double[iFastClinicalEngineerNum];
		alfClinicalEngineerExperienceRateAIS1		= new double[iFastClinicalEngineerNum];
		alfClinicalEngineerExperienceRateAIS2		= new double[iFastClinicalEngineerNum];
		alfClinicalEngineerConTired1				= new double[iFastClinicalEngineerNum];
		alfClinicalEngineerConTired2				= new double[iFastClinicalEngineerNum];
		alfClinicalEngineerConTired3				= new double[iFastClinicalEngineerNum];
		alfClinicalEngineerConTired4				= new double[iFastClinicalEngineerNum];
		alfClinicalEngineerTiredRate				= new double[iFastClinicalEngineerNum];
		alfClinicalEngineerAssociationRate			= new double[iFastClinicalEngineerNum];
		alfClinicalEngineerAssociationRate			= new double[iFastClinicalEngineerNum];
		aiClinicalEngineerDepartment				= new int[iFastClinicalEngineerNum];
		aiClinicalEngineerRoomNumber				= new int[iFastClinicalEngineerNum];
		alfClinicalEngineerExaminationTime			= new double[iFastClinicalEngineerNum];

		if( ArrayListExaminationFastRooms == null )
		{
			ArrayListExaminationFastRooms = new ArrayList<ERExaminationFastRoom>();
		}

//		for( i = 0;i < iClinicalEngineerNum; i++ )
//		{
//			alfClinicalEngineerConExperience[i] = 0.5;
//			alfClinicalEngineerConTired[i] = 3;
//			alfClinicalEngineerExaminationTime[i] = 30*60;
//			aiClinicalEngineerDepartment[i] = 12;
//		}
		for( i = 0;i < iFastClinicalEngineerNum; i++ )
		{
			alfClinicalEngineerYearExperience[i] 			= 10;
			alfClinicalEngineerConExperience[i] 			= 3;
			alfClinicalEngineerConTired1[i] 				= 3;
			alfClinicalEngineerConTired2[i] 				= 3;
			alfClinicalEngineerConTired3[i] 				= 3;
			alfClinicalEngineerConTired4[i] 				= 3;
			alfClinicalEngineerTiredRate[i] 				= 3.0;
			alfClinicalEngineerAssociationRate[i]	 		= 1.0;
			alfClinicalEngineerExaminationTime[i]			= 30*60;
			aiClinicalEngineerDepartment[i]					= 14;
			aiClinicalEngineerRoomNumber[i]					= 1;
		}

		for( i = 0;i < pplfParameter[0].length; i++ )
		{
			if( pplfParameter[0][i] == 1.0 )
			{
			}
			else if( pplfParameter[0][i] == 2.0 )
			{
			}
			else if( pplfParameter[0][i] == 3.0 )
			{
				if( iFastClinicalEngineerNum <= iCurrentClinicalEngineerNum ) continue;
				if( iStartClinicalEngineerLoc == 0 ) iStartClinicalEngineerLoc = i;
				alfClinicalEngineerYearExperience[iCurrentClinicalEngineerNum] 				= pplfParameter[1][i];
				alfClinicalEngineerConExperience[iCurrentClinicalEngineerNum] 				= pplfParameter[2][i];
				alfClinicalEngineerConTired1[iCurrentClinicalEngineerNum]			 		= pplfParameter[3][i];
				alfClinicalEngineerConTired2[iCurrentClinicalEngineerNum]			 		= pplfParameter[4][i];
				alfClinicalEngineerConTired3[iCurrentClinicalEngineerNum]			 		= pplfParameter[5][i];
				alfClinicalEngineerConTired4[iCurrentClinicalEngineerNum]			 		= pplfParameter[6][i];
				alfClinicalEngineerTiredRate[iCurrentClinicalEngineerNum]					= pplfParameter[7][i];
				alfClinicalEngineerAssociationRate[iCurrentClinicalEngineerNum]			 	= pplfParameter[8][i];
				aiClinicalEngineerDepartment[iCurrentClinicalEngineerNum]			 		= (int)pplfParameter[9][i];
				alfClinicalEngineerExaminationTime[i] 										= 30*60;
				alfClinicalEngineerExperienceRate1[i]										= pplfParameter[10][i];
				alfClinicalEngineerExperienceRate2[i]										= pplfParameter[11][i];
				alfClinicalEngineerConExperienceAIS[i]										= pplfParameter[12][i];
				alfClinicalEngineerExperienceRateAIS1[i]									= pplfParameter[13][i];
				alfClinicalEngineerExperienceRateAIS2[i]									= pplfParameter[14][i];
				aiClinicalEngineerRoomNumber[i]												= (int)pplfParameter[15][i];
				iCurrentClinicalEngineerNum++;
			}
			else
			{

			}
		}

		if( iFastClinicalEngineerNum > iCurrentClinicalEngineerNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iFastClinicalEngineerNum-iCurrentClinicalEngineerNum; i < iFastClinicalEngineerNum; i++ )
			{
				iLocation = i % iCurrentClinicalEngineerNum + iStartClinicalEngineerLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				alfClinicalEngineerYearExperience[i] 			= pplfParameter[1][iLocation];
				alfClinicalEngineerConExperience[i] 			= pplfParameter[2][iLocation];
				alfClinicalEngineerConTired1[i]			 		= pplfParameter[3][iLocation];
				alfClinicalEngineerConTired2[i]			 		= pplfParameter[4][iLocation];
				alfClinicalEngineerConTired3[i]			 		= pplfParameter[5][iLocation];
				alfClinicalEngineerConTired4[i]			 		= pplfParameter[6][iLocation];
				alfClinicalEngineerTiredRate[i]					= pplfParameter[7][iLocation];
				alfClinicalEngineerAssociationRate[i]		 	= pplfParameter[8][iLocation];
				aiClinicalEngineerDepartment[i]			 		= (int)pplfParameter[9][iLocation];
				alfClinicalEngineerExperienceRate1[i]			= pplfParameter[10][iLocation];
				alfClinicalEngineerExperienceRate2[i]			= pplfParameter[11][iLocation];
				alfClinicalEngineerConExperienceAIS[i]			= pplfParameter[12][iLocation];
				alfClinicalEngineerExperienceRateAIS1[i]		= pplfParameter[13][iLocation];
				alfClinicalEngineerExperienceRateAIS2[i]		= pplfParameter[14][iLocation];
				aiClinicalEngineerRoomNumber[i]			 		= (int)pplfParameter[15][iLocation];
			}
		}

		for( i = 0;i < iExaminationRoomNum; i++ )
		{
			ArrayListExaminationFastRooms.add( new ERExaminationFastRoom() );
			ArrayListExaminationFastRooms.get(i).vSetRandom( sfmtRandom );
			ArrayListExaminationFastRooms.get(i).vSetSimulationEngine( engine );

			// 医療技師エージェントの作成
			ArrayListExaminationFastRooms.get(i).vCreateClinicalEngineerAgents( iFastClinicalEngineerNum );
			ArrayListExaminationFastRooms.get(i).vSetClinicalEngineersRandom();
//			ArrayListExaminationFastRooms.get(i).vSetClinicalEngineerAgentParameter( alfClinicalEngineerYearExperience,
//					alfClinicalEngineerConExperience,
//					alfClinicalEngineerConTired1,
//					alfClinicalEngineerConTired2,
//					alfClinicalEngineerConTired3,
//					alfClinicalEngineerConTired4,
//					alfClinicalEngineerTiredRate,
//					alfClinicalEngineerAssociationRate,
//					alfClinicalEngineerExaminationTime,
//					aiClinicalEngineerDepartment );
			for( j = 0; j < ArrayListExaminationFastRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
			{
				ArrayListExaminationFastRooms.get(i).cGetClinicalEngineerAgent(j).vSetSimulationEngine( engine );
				ArrayListExaminationFastRooms.get(i).cGetClinicalEngineerAgent(j).vSetSimulationEndTime( lfEndTime );
			}
		}
	}

	/**
	 * <PRE>
	 *   待合室及び構成する看護師の数を設定します。
	 * </PRE>
	 * @param iWaitingRoomNum				待合室の部屋数
	 * @param lfArrivalPatientPepoleData	到達患者の人数
	 * @param strPath						待合室設定ファイル
	 * @param engine						FUSEシミュレーションエンジン
	 * @param sfmtRandom					メルセンヌツイスターのインスタンス
	 * @throws IOException					ファイル処理中に発生した例外
	 * @author kobayashi
	 * @since 2015/08/05
	 * @version 0.1
	 */
	private void vSetWaitingRooms(int iWaitingRoomNum, double lfArrivalPatientPepoleData, String strPath, SimulationEngine engine, Rand sfmtRandom ) throws IOException
	{
		int i,j;
		int iCurrentNurseNum = 0;
		int iStartNurseLoc = 0;
		int iLocation = 0;

		int[] aiNurseCategory;
		int[] aiNurseTriageProtocol;
		int[] aiNurseTriageLevel;
		double[] alfNurseTriageYearExperience;
		double[] alfNurseYearExperience;
		double[] alfNurseConExperience;
		double[] alfNurseExperienceRate1;
		double[] alfNurseExperienceRate2;
		double[] alfNurseConExperienceAIS;
		double[] alfNurseExperienceRateAIS1;
		double[] alfNurseExperienceRateAIS2;
		double[] alfNurseConTired1;
		double[] alfNurseConTired2;
		double[] alfNurseConTired3;
		double[] alfNurseConTired4;
		double[] alfNurseTiredRate;
		double[] alfNurseAssociationRate;
		double[] alfNurseObservationTime;
		double[] alfNurseObservationProcessTime;
		double[] alfNurseTriageTime;
		int[] aiNurseDepartment;
		int[] aiNurseRoomNumber;

		int iRow = 0;
		int iColumn = 0;
		double[][] pplfParameter;

		CCsv csv = new CCsv();
		if( strPath == "" )
		{
			csv.vOpen( "./parameter/待合室.csv", "read" );
		}
		else
		{
			csv.vOpen( strPath, "read" );
		}
		csv.vGetRowColumn();

		iRow = csv.iGetRow();
		iColumn = csv.iGetColumn();

		pplfParameter = new double[iColumn][iRow];
		csv.vRead( pplfParameter );
		csv.vClose();
		// 医師数、看護師数、医療技師数を設定します。
		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			if( pplfParameter[0].length > 0 )
			{
				iWaitingNurseNum = 0;
				for( i = 0;i < pplfParameter[0].length; i++ )
				{
					if( pplfParameter[0][i] == 1.0 )
					{
					}
					else if( pplfParameter[0][i] == 2.0 )
					{
						iWaitingNurseNum++;
					}
					else if( pplfParameter[0][i] == 3.0 )
					{
					}
				}
			}
		}
		// 逆シミュレーションモードの場合
		else
		{
			// 部屋数が0以下の場合はすべて0にして終了する。
			if( this.iWaitingRoomNum <= 0 )
			{
				this.iWaitingRoomNum = 0;
				this.iWaitingNurseNum = 0;
				erWaitingRoom = new ERWaitingRoom();
				return ;
			}
		}

		alfNurseYearExperience			= new double[iWaitingNurseNum];
		alfNurseConExperience			= new double[iWaitingNurseNum];
		alfNurseExperienceRate1			= new double[iWaitingNurseNum];
		alfNurseExperienceRate2			= new double[iWaitingNurseNum];
		alfNurseConExperienceAIS		= new double[iWaitingNurseNum];
		alfNurseExperienceRateAIS1		= new double[iWaitingNurseNum];
		alfNurseExperienceRateAIS2		= new double[iWaitingNurseNum];
		alfNurseConTired1				= new double[iWaitingNurseNum];
		alfNurseConTired2				= new double[iWaitingNurseNum];
		alfNurseConTired3				= new double[iWaitingNurseNum];
		alfNurseConTired4				= new double[iWaitingNurseNum];
		alfNurseTiredRate				= new double[iWaitingNurseNum];
		alfNurseAssociationRate			= new double[iWaitingNurseNum];
		alfNurseObservationTime			= new double[iWaitingNurseNum];
		alfNurseObservationProcessTime	= new double[iWaitingNurseNum];
		alfNurseTriageTime				= new double[iWaitingNurseNum];
		alfNurseAssociationRate			= new double[iWaitingNurseNum];
		alfNurseTriageYearExperience	= new double[iWaitingNurseNum];

		aiNurseTriageProtocol			= new int[iWaitingNurseNum];
		aiNurseTriageLevel				= new int[iWaitingNurseNum];
		aiNurseDepartment				= new int[iWaitingNurseNum];
		aiNurseCategory					= new int[iWaitingNurseNum];
		aiNurseRoomNumber				= new int[iWaitingNurseNum];

		if( erWaitingRoom == null )
		{
			erWaitingRoom = new ERWaitingRoom();
		}

//		for( i = 0;i < iNurseNum; i++ )
//		{
//		alfConExperience[i] = 3;
//		alfConTired[i] = 3;
//		alfObservationTime[i] = 30*60;
//		alfObservationProcessTime[i] = 30;
//		alfTriageTime[i] = 30*60;
//		aiProtocol[i] = 1;
//		aiLevel[i] = 5;
//		aiDepartment[i] = 9;
//		}

		for( i = 0;i < iWaitingNurseNum; i++ )
		{
			alfNurseConExperience[i]			= 5;
			alfNurseConTired1[i]				= 3;
			alfNurseConTired2[i]				= 3;
			alfNurseConTired3[i]				= 3;
			alfNurseConTired4[i]				= 3;
			alfNurseTiredRate[i]				= 3;
			alfNurseObservationTime[i]			= 30*60;
			alfNurseObservationProcessTime[i]	= 30;
			alfNurseTriageTime[i] 				= 30*60;
			aiNurseTriageProtocol[i] 			= 1;
			aiNurseTriageLevel[i] 				= 5;
			alfNurseAssociationRate[i]		 	= 1.0;
			aiNurseDepartment[i] 				= 9;
			alfNurseExperienceRate1[i]			= 2.1;
			alfNurseExperienceRate2[i]			= 0.9;
			alfNurseConExperienceAIS[i]			= 0.14;
			alfNurseExperienceRateAIS1[i]		= 0.2;
			alfNurseExperienceRateAIS2[i]		= 1.1;
			aiNurseRoomNumber[i] 				= 1;
		}
		for( i = 0;i < pplfParameter[0].length; i++ )
		{
			if( pplfParameter[0][i] == 1.0 )
			{
			}
			else if( pplfParameter[0][i] == 2.0 )
			{
				if( iWaitingNurseNum <= iCurrentNurseNum ) continue;
				if( iStartNurseLoc == 0 ) iStartNurseLoc = i;
				aiNurseCategory[iCurrentNurseNum] 					= (int)pplfParameter[1][i];
				aiNurseTriageProtocol[iCurrentNurseNum] 			= (int)pplfParameter[2][i];
				aiNurseTriageLevel[iCurrentNurseNum] 				= (int)pplfParameter[3][i];
				alfNurseTriageYearExperience[iCurrentNurseNum] 		= pplfParameter[4][i];
				alfNurseYearExperience[iCurrentNurseNum] 			= pplfParameter[5][i];
				alfNurseConExperience[iCurrentNurseNum] 			= pplfParameter[6][i];
				alfNurseConTired1[iCurrentNurseNum]			 		= pplfParameter[7][i];
				alfNurseConTired2[iCurrentNurseNum]			 		= pplfParameter[8][i];
				alfNurseConTired3[iCurrentNurseNum]			 		= pplfParameter[9][i];
				alfNurseConTired4[iCurrentNurseNum]			 		= pplfParameter[10][i];
				alfNurseTiredRate[iCurrentNurseNum]					= pplfParameter[11][i];
				alfNurseAssociationRate[iCurrentNurseNum]		 	= pplfParameter[12][i];
				aiNurseDepartment[iCurrentNurseNum]			 		= (int)pplfParameter[13][i];
				alfNurseObservationTime[iCurrentNurseNum]		 	= 30*60;
				alfNurseObservationProcessTime[iCurrentNurseNum] 	= 30;
				alfNurseTriageTime[iCurrentNurseNum] 				= 30*60;
				alfNurseExperienceRate1[iCurrentNurseNum]			= pplfParameter[14][i];
				alfNurseExperienceRate2[iCurrentNurseNum]			= pplfParameter[15][i];
				alfNurseConExperienceAIS[iCurrentNurseNum]			= pplfParameter[16][i];
				alfNurseExperienceRateAIS1[iCurrentNurseNum]		= pplfParameter[17][i];
				alfNurseExperienceRateAIS2[iCurrentNurseNum]		= pplfParameter[18][i];
				aiNurseRoomNumber[iCurrentNurseNum]				 	= (int)pplfParameter[19][i];
				iCurrentNurseNum++;
			}
			else if( pplfParameter[0][i] == 3.0 )
			{

			}
			else
			{

			}
		}

		if( iWaitingNurseNum > iCurrentNurseNum )
		{
			// 逆シミュレーションの場合の更新
			// 数が足りない場合、設定ファイルに記載されているパラメータを巡回して入れるようにします。
			for( i = iWaitingNurseNum-iCurrentNurseNum; i < iWaitingNurseNum; i++ )
			{
				iLocation = i % iCurrentNurseNum + iStartNurseLoc;
				if( iLocation >= pplfParameter[1].length ) continue;
				aiNurseCategory[i] 					= (int)pplfParameter[1][iLocation];
				aiNurseTriageProtocol[i] 			= (int)pplfParameter[2][iLocation];
				aiNurseTriageLevel[i] 				= (int)pplfParameter[3][iLocation];
				alfNurseTriageYearExperience[i]	 	= pplfParameter[4][iLocation];
				alfNurseYearExperience[i] 			= pplfParameter[5][iLocation];
				alfNurseConExperience[i] 			= pplfParameter[6][iLocation];
				alfNurseConTired1[i]			 	= pplfParameter[7][iLocation];
				alfNurseConTired2[i]			 	= pplfParameter[8][iLocation];
				alfNurseConTired3[i]				= pplfParameter[9][iLocation];
				alfNurseConTired4[i]			 	= pplfParameter[10][iLocation];
				alfNurseTiredRate[i]				= pplfParameter[11][iLocation];
				alfNurseAssociationRate[i]		 	= pplfParameter[12][iLocation];
				aiNurseDepartment[i]			 	= (int)pplfParameter[13][iLocation];
				alfNurseObservationTime[i]		 	= 30*60;
				alfNurseObservationProcessTime[i] 	= 30;
				alfNurseTriageTime[i] 				= 30*60;
				alfNurseExperienceRate1[i]			= pplfParameter[14][iLocation];
				alfNurseExperienceRate2[i]			= pplfParameter[15][iLocation];
				alfNurseConExperienceAIS[i]			= pplfParameter[16][iLocation];
				alfNurseExperienceRateAIS1[i]		= pplfParameter[17][iLocation];
				alfNurseExperienceRateAIS2[i]		= pplfParameter[18][iLocation];
				aiNurseRoomNumber[i]				= (int)pplfParameter[19][iLocation];
			}
		}

		// 看護師エージェントの作成
		erWaitingRoom.vSetSimulationEngine( engine );
		erWaitingRoom.vSetRandom( sfmtRandom );
		erWaitingRoom.vCreateNurseAgents( iWaitingNurseNum );
		erWaitingRoom.vSetNursesRandom();
		erWaitingRoom.vSetNurseAgentParameter(aiNurseCategory,
				  aiNurseTriageProtocol,
				  aiNurseTriageLevel,
				  alfNurseTriageYearExperience,
				  alfNurseYearExperience,
				  alfNurseConExperience,
				  alfNurseExperienceRate1,
				  alfNurseExperienceRate2,
				  alfNurseConExperienceAIS,
				  alfNurseExperienceRateAIS1,
				  alfNurseExperienceRateAIS2,
				  alfNurseConTired1,
				  alfNurseConTired2,
				  alfNurseConTired3,
				  alfNurseConTired4,
				  alfNurseTiredRate,
				  alfNurseAssociationRate,
				  alfNurseObservationTime,
				  alfNurseObservationProcessTime,
				  alfNurseTriageTime,
				  aiNurseDepartment,
				  aiNurseRoomNumber );
		erWaitingRoom.vSetArrivalPatientPepole( lfArrivalPatientPepoleData );
		for( j = 0; j < erWaitingRoom.iGetNurseAgentsNum(); j++ )
		{
			erWaitingRoom.erGetNurseAgent(j).vSetSimulationEngine( engine );
			erWaitingRoom.erGetNurseAgent(j).vSetSimulationEndTime( lfEndTime );
		}
		erWaitingRoom.vCreatePatientAgents();
	}


	/**
	 * <PRE>
	 *   病院外の状態を設定します。
	 * </PRE>
	 * @param iOutsideNum					病院外エージェント数
	 * @param lfArrivalPatientPepoleData	来院患者数の設定
	 * @param engine						FUSEエンジン
	 * @param sfmtRandom					メルセンヌツイスターのインスタンス
	 * @throws IOException					java標準のIO例外クラス
	 * @since 2015/08/05
	 */
	private void vSetOutside(int iOutsideNum, double lfArrivalPatientPepoleData, SimulationEngine engine, utility.sfmt.Rand sfmtRandom ) throws IOException
	{
		if( erOutside == null )
		{
			erOutside = new EROutside();
		}
		erOutside.vSetArrivalPatientPepole( lfArrivalPatientPepoleData );
		erOutside.vSetSimulationEngine( engine );
		erOutside.vSetRandom( sfmtRandom );
	}

	/**
	 * <PRE>
	 *   階段アセットを設定します。
	 * </PRE>
	 *
	 * @param env					FUSE環境
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vSetStairs( Environment env )
	{
	}

	/**
	 * <PRE>
	 *   エレベーターアセットを設定します。
	 * </PRE>
	 *
	 * @param env					FUSE環境
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vSetElevators( Environment env )
	{
	}

	public ERWaitingRoom erGetWaitingRoom()
	{
		return erWaitingRoom;
	}

	public void vOutput() throws IOException
	{
		String strData = "";
//		csvWriteERData.vWrite( strData );
	}

	/**
	 * <PRE>
	 *    シミュレーション終了時間を設定します。
	 * </PRE>
	 * @param lfTime シミュレーション終了時間
	 * @author kobayashi
	 * @since 2015/08/05
	 * @version 0.1
	 */
	public void vSetSimulationEndTime(double lfTime )
	{
		// TODO 自動生成されたメソッド・スタブ
		lfEndTime = lfTime;
	}

	/**
	 * <PRE>
	 *    終了時間までの処理状況を簡易プログレスバーのようなもので出力します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/08/05
	 * @version 0.1
	 */
	private void vCurrentProcessStatus()
	{
		double lfCurrentTime;
		double lfCompleteRate;
		double lfTimeStep = erEngine.getLatestTimeStep()/1000;
		lfCurrentTime = lfTotalTime;
		lfCompleteRate = lfCurrentTime / lfEndTime;

		if( lfCompleteRate < 0.1 && iCurrentStatusFlag == 0 )
		{
			iCurrentStatusFlag++;
			System.out.println("  0%終了：*");
		}
		else if( lfCompleteRate > 0.1 && iCurrentStatusFlag == 1  )
		{
			iCurrentStatusFlag++;
			System.out.println(" 10%終了：**");
		}
		else if( lfCompleteRate > 0.2 && iCurrentStatusFlag == 2  )
		{
			iCurrentStatusFlag++;
			System.out.println(" 20%終了：***");
		}
		else if( lfCompleteRate > 0.3 && iCurrentStatusFlag == 3  )
		{
			iCurrentStatusFlag++;
			System.out.println(" 30%終了：****");
		}
		else if( lfCompleteRate > 0.4 && iCurrentStatusFlag == 4  )
		{
			iCurrentStatusFlag++;
			System.out.println(" 40%終了：*****");
		}
		else if( lfCompleteRate > 0.5 && iCurrentStatusFlag == 5  )
		{
			iCurrentStatusFlag++;
			System.out.println(" 50%終了：******");
		}
		else if( lfCompleteRate > 0.6 && iCurrentStatusFlag == 6  )
		{
			iCurrentStatusFlag++;
			System.out.println(" 60%終了：*******");
		}
		else if( lfCompleteRate > 0.7 && iCurrentStatusFlag == 7 )
		{
			iCurrentStatusFlag++;
			System.out.println(" 70%終了：********");
		}
		else if( lfCompleteRate > 0.8 && iCurrentStatusFlag == 8 )
		{
			iCurrentStatusFlag++;
			System.out.println(" 80%終了：*********");
		}
		else if( lfCompleteRate > 0.9 && iCurrentStatusFlag == 9 )
		{
			iCurrentStatusFlag++;
			System.out.println(" 90%終了：**********");
		}
		else if( lfCompleteRate >= 1.0 && iCurrentStatusFlag == 10 )
		{
			iCurrentStatusFlag++;
			System.out.println("100%終了：***********");
		}
	}

	/**
	 * <PRE>
	 *   各部屋の施設図の座標が記載されたファイルを読み込みます。
	 * </PRE>
	 * @param strFileName	描画用の各部屋の施設図の座標が記載されたファイルパス
	 * @param iLoc			指定した各部屋
	 * @throws IOException	ファイル処理中の例外
	 * @author kobayashi
	 * @since 2015/08/05
	 * @version 0.1
	 */
	private void vReadERDepartmentEachRoom( String strFileName, int iLoc ) throws IOException
	{
		int i;
		CCsv csv;
		int[][] ppiXYData;
		int[] piX;
		int[] piY;
		int[] piZ;
		int[] piWidth;
		int[] piHeight;
		int[] piF;

		int iRow;
		int iColumn;

		csv = new CCsv();

		csv.vOpen(strFileName, "read");

		csv.vGetRowColumn();

		iColumn = csv.iGetRow();
		iRow = csv.iGetColumn();

		ppiXYData = new int[iRow][iColumn];
		piX = new int[iRow];
		piY = new int[iRow];
		piZ = new int[iRow];
		piWidth = new int[iRow];
		piHeight = new int[iRow];
		piF = new int[iRow];

		csv.vRead( ppiXYData );

		for( i = 0;i < iRow; i++ )
		{
			piX[i]		= ppiXYData[i][0];
			piY[i]		= ppiXYData[i][1];
			piZ[i]		= ppiXYData[i][2];
			piWidth[i]	= ppiXYData[i][3];
			piHeight[i] = ppiXYData[i][4];
			piF[i] 		= ppiXYData[i][5];
		}
		csv.vClose();

		if( iLoc == 1 )
		{
			// 各診察室の中心座標を設定します。
			for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
			{
				ArrayListConsultationRooms.get(i).vSetX( piX[i] );
				ArrayListConsultationRooms.get(i).vSetY( piY[i] );
				ArrayListConsultationRooms.get(i).vSetZ( piZ[i] );
				ArrayListConsultationRooms.get(i).vSetWidth( piWidth[i] );
				ArrayListConsultationRooms.get(i).vSetHeight( piHeight[i] );
				ArrayListConsultationRooms.get(i).vSetF( piF[i] );
				ArrayListConsultationRooms.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
			}
		}
		else if( iLoc == 2 )
		{
			// 各手術室の中心座標を設定します。
			for( i = 0;i < ArrayListOperationRooms.size(); i++ )
			{
				ArrayListOperationRooms.get(i).vSetX( piX[i] );
				ArrayListOperationRooms.get(i).vSetY( piY[i] );
				ArrayListOperationRooms.get(i).vSetZ( piZ[i] );
				ArrayListOperationRooms.get(i).vSetWidth( piWidth[i] );
				ArrayListOperationRooms.get(i).vSetHeight( piHeight[i] );
				ArrayListOperationRooms.get(i).vSetF( piF[i] );
				ArrayListOperationRooms.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
			}
		}
		else if( iLoc == 3 )
		{
			// 各初療室の中心座標を設定します。
			for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
			{
				ArrayListEmergencyRooms.get(i).vSetX( piX[i] );
				ArrayListEmergencyRooms.get(i).vSetY( piY[i] );
				ArrayListEmergencyRooms.get(i).vSetZ( piZ[i] );
				ArrayListEmergencyRooms.get(i).vSetWidth( piWidth[i] );
				ArrayListEmergencyRooms.get(i).vSetHeight( piHeight[i] );
				ArrayListEmergencyRooms.get(i).vSetF( piF[i] );
				ArrayListEmergencyRooms.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
			}
		}
		else if( iLoc == 4 )
		{
			// 各観察屋の中心座標を設定します。
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				ArrayListObservationRooms.get(i).vSetX( piX[i] );
				ArrayListObservationRooms.get(i).vSetY( piY[i] );
				ArrayListObservationRooms.get(i).vSetZ( piZ[i] );
				ArrayListObservationRooms.get(i).vSetWidth( piWidth[i] );
				ArrayListObservationRooms.get(i).vSetHeight( piHeight[i] );
				ArrayListObservationRooms.get(i).vSetF( piF[i] );
				ArrayListObservationRooms.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
			}
		}
		else if( iLoc == 5 )
		{
			// 各重症観察室の中心座標を設定します。
			for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
			{
				ArrayListSevereInjuryObservationRooms.get(i).vSetX( piX[i] );
				ArrayListSevereInjuryObservationRooms.get(i).vSetY( piY[i] );
				ArrayListSevereInjuryObservationRooms.get(i).vSetZ( piZ[i] );
				ArrayListSevereInjuryObservationRooms.get(i).vSetWidth( piWidth[i] );
				ArrayListSevereInjuryObservationRooms.get(i).vSetHeight( piHeight[i] );
				ArrayListSevereInjuryObservationRooms.get(i).vSetF( piF[i] );
				ArrayListSevereInjuryObservationRooms.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
			}
		}
		else if( iLoc == 6 )
		{
			// 各集中治療室の中心座標を設定します。
			for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
			{
				ArrayListIntensiveCareUnitRooms.get(i).vSetX( piX[i] );
				ArrayListIntensiveCareUnitRooms.get(i).vSetY( piY[i] );
				ArrayListIntensiveCareUnitRooms.get(i).vSetZ( piZ[i] );
				ArrayListIntensiveCareUnitRooms.get(i).vSetWidth( piWidth[i] );
				ArrayListIntensiveCareUnitRooms.get(i).vSetHeight( piHeight[i] );
				ArrayListIntensiveCareUnitRooms.get(i).vSetF( piF[i] );
				ArrayListIntensiveCareUnitRooms.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
			}
		}
		else if( iLoc == 7 )
		{
			// 各高度治療室の中心座標を設定します。
			for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
			{
				ArrayListHighCareUnitRooms.get(i).vSetX( piX[i] );
				ArrayListHighCareUnitRooms.get(i).vSetY( piY[i] );
				ArrayListHighCareUnitRooms.get(i).vSetZ( piZ[i] );
				ArrayListHighCareUnitRooms.get(i).vSetWidth( piWidth[i] );
				ArrayListHighCareUnitRooms.get(i).vSetHeight( piHeight[i] );
				ArrayListHighCareUnitRooms.get(i).vSetF( piF[i] );
				ArrayListHighCareUnitRooms.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
			}
		}
		else if( iLoc == 8 )
		{
			// 各一般病棟の中心座標を設定します。
			for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
			{
				ArrayListGeneralWardRooms.get(i).vSetX( piX[i] );
				ArrayListGeneralWardRooms.get(i).vSetY( piY[i] );
				ArrayListGeneralWardRooms.get(i).vSetZ( piZ[i] );
				ArrayListGeneralWardRooms.get(i).vSetWidth( piWidth[i] );
				ArrayListGeneralWardRooms.get(i).vSetHeight( piHeight[i] );
				ArrayListGeneralWardRooms.get(i).vSetF( piF[i] );
				ArrayListGeneralWardRooms.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
			}
		}
		else if( iLoc == 9 )
		{
			i = 0;
			// 各待合室の中心座標を設定します。
			erWaitingRoom.vSetX( piX[i] );
			erWaitingRoom.vSetY( piY[i] );
			erWaitingRoom.vSetZ( piZ[i] );
			erWaitingRoom.vSetWidth( piWidth[i] );
			erWaitingRoom.vSetHeight( piHeight[i] );
			erWaitingRoom.vSetF( piF[i] );
			erWaitingRoom.setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
		}
		else if( iLoc == 10 )
		{
			// 各X線室の中心座標を設定します。
			for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
			{
				ArrayListExaminationXRayRooms.get(i).vSetX( piX[i] );
				ArrayListExaminationXRayRooms.get(i).vSetY( piY[i] );
				ArrayListExaminationXRayRooms.get(i).vSetZ( piZ[i] );
				ArrayListExaminationXRayRooms.get(i).vSetWidth( piWidth[i] );
				ArrayListExaminationXRayRooms.get(i).vSetHeight( piHeight[i] );
				ArrayListExaminationXRayRooms.get(i).vSetF( piF[i] );
				ArrayListExaminationXRayRooms.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
			}
		}
		else if( iLoc == 11 )
		{
			// 各CT室の中心座標を設定します。
			for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
			{
				ArrayListExaminationCTRooms.get(i).vSetX( piX[i] );
				ArrayListExaminationCTRooms.get(i).vSetY( piY[i] );
				ArrayListExaminationCTRooms.get(i).vSetZ( piZ[i] );
				ArrayListExaminationCTRooms.get(i).vSetWidth( piWidth[i] );
				ArrayListExaminationCTRooms.get(i).vSetHeight( piHeight[i] );
				ArrayListExaminationCTRooms.get(i).vSetF( piF[i] );
				ArrayListExaminationCTRooms.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
			}
		}
		else if( iLoc == 12 )
		{
			// 各MRI室の中心座標を設定します。
			for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
			{
				ArrayListExaminationMRIRooms.get(i).vSetX( piX[i] );
				ArrayListExaminationMRIRooms.get(i).vSetY( piY[i] );
				ArrayListExaminationMRIRooms.get(i).vSetZ( piZ[i] );
				ArrayListExaminationMRIRooms.get(i).vSetWidth( piWidth[i] );
				ArrayListExaminationMRIRooms.get(i).vSetHeight( piHeight[i] );
				ArrayListExaminationMRIRooms.get(i).vSetF( piF[i] );
				ArrayListExaminationMRIRooms.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
			}
		}
		else if( iLoc == 13 )
		{
			// 各血管造影室の中心座標を設定します。
			for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
			{
				ArrayListExaminationAngiographyRooms.get(i).vSetX( piX[i] );
				ArrayListExaminationAngiographyRooms.get(i).vSetY( piY[i] );
				ArrayListExaminationAngiographyRooms.get(i).vSetZ( piZ[i] );
				ArrayListExaminationAngiographyRooms.get(i).vSetWidth( piWidth[i] );
				ArrayListExaminationAngiographyRooms.get(i).vSetHeight( piHeight[i] );
				ArrayListExaminationAngiographyRooms.get(i).vSetF( piF[i] );
				ArrayListExaminationAngiographyRooms.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
			}
		}
		else if( iLoc == 14 )
		{
			// 各超音波室の中心座標を設定します。
			for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
			{
				ArrayListExaminationFastRooms.get(i).vSetX( piX[i] );
				ArrayListExaminationFastRooms.get(i).vSetY( piY[i] );
				ArrayListExaminationFastRooms.get(i).vSetZ( piZ[i] );
				ArrayListExaminationFastRooms.get(i).vSetWidth( piWidth[i] );
				ArrayListExaminationFastRooms.get(i).vSetHeight( piHeight[i] );
				ArrayListExaminationFastRooms.get(i).vSetF( piF[i] );
				ArrayListExaminationFastRooms.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
			}
		}
		else if( iLoc == 15 )
		{
			for( i = 0;i < iRow; i++ )
			{
				ArrayListStairs.add( new ERStairs() );
			}
			// 階段の中心座標を設定します。
			for( i = 0;i < ArrayListStairs.size(); i++ )
			{
				ArrayListStairs.get(i).vSetX( piX[i] );
				ArrayListStairs.get(i).vSetY( piY[i] );
				ArrayListStairs.get(i).vSetZ( piZ[i] );
				ArrayListStairs.get(i).vSetWidth( piWidth[i] );
				ArrayListStairs.get(i).vSetHeight( piHeight[i] );
				ArrayListStairs.get(i).vSetF( piF[i] );
				ArrayListStairs.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
				ArrayListStairs.get(i).vSetEnvironment( erEnvironment );
			}
		}
		else if( iLoc == 16 )
		{
			for( i = 0;i < iRow; i++ )
			{
				ArrayListElevators.add( new ERElevator() );
			}
			// エレベータの中心座標を設定します。
			for( i = 0;i < ArrayListElevators.size(); i++ )
			{
				ArrayListElevators.get(i).vSetX( piX[i] );
				ArrayListElevators.get(i).vSetY( piY[i] );
				ArrayListElevators.get(i).vSetZ( piZ[i] );
				ArrayListElevators.get(i).vSetWidth( piWidth[i] );
				ArrayListElevators.get(i).vSetHeight( piHeight[i] );
				ArrayListElevators.get(i).vSetF( piF[i] );
				ArrayListElevators.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
				ArrayListElevators.get(i).vSetEnvironment( erEnvironment );
			}
		}
		else if( iLoc == 0 )
		{
//			// その他部屋の中心座標を設定します。
//			for( i = 0;i < ArrayListOtherRooms.size(); i++ )
//			{
//				ArrayListOtherRooms.get(i).vSetX( piX[i] );
//				ArrayListOtherRooms.get(i).vSetY( piY[i] );
//				ArrayListOtherRooms.get(i).vSetZ( piZ[i] );
//				ArrayListOtherRooms.get(i).vSetWidth( piWidth[i] );
//				ArrayListOtherRooms.get(i).vSetHeight( piHeight[i] );
//				ArrayListOtherRooms.get(i).vSetF( piF[i] );
//				ArrayListOtherRooms.get(i).setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, 0);
//			}
		}
		else if( iLoc == -1 )
		{
			i = 0;
			// 病院外の中心座標を設定します。
			erOutside.vSetX( piX[i] );
			erOutside.vSetY( piY[i] );
			erOutside.vSetZ( piZ[i] );
			erOutside.vSetWidth( piWidth[i] );
			erOutside.vSetHeight( piHeight[i] );
			erOutside.vSetF( piF[i] );
			erOutside.setPosition( piX[i] + piWidth[i]/2, piY[i]+piHeight[i]/2, piZ[i]);
		}
		else
		{

		}
	}

	/**
	 * <PRE>
	 *   救急部門の施設図の座標が記載されたファイルを読み込みます。
	 * </PRE>
	 * @param strFileName 救急部門描画設定ファイルパス
	 * @throws IOException ファイル処理中の例外
	 * @author kobayashi
	 * @since 2015/08/05
	 * @version 0.1
	 */
	private void vReadERDepartment( String strFileName ) throws IOException
	{
		int i,j;
		CCsv csv;
		int[][] ppiXYData;

		int iRow;
		int iColumn;
		int iCurrentFloor;
		int iMaxFloor = 0;
		int iLoc = 0;

		csv = new CCsv();

		csv.vOpen(strFileName, "read");

		csv.vGetRowColumn();

		iColumn = csv.iGetRow();
		iRow = csv.iGetColumn();

		ppiXYData = new int[iRow][iColumn];

		csv.vRead( ppiXYData );

		// 病院のフロアー数を取得します。
		for( i = 0;i < iRow; i++ )
		{
			iMaxFloor = ppiXYData[i][3];
		}
		piFloor = new int[iMaxFloor];
		// 1フロアーにどれだけの座標点指定が必要かを取得します。
		iCurrentFloor = 1;
		for( i = 0;i < iRow; i++ )
		{
			if( ppiXYData[i][3] == iCurrentFloor )
			{
				piFloor[iCurrentFloor-1]++;
			}
			else
			{
				iCurrentFloor = ppiXYData[i][3];
				piFloor[iCurrentFloor-1] = 1;
			}
		}

		// 各フロアーごとのデータを取得後、座標を割り当てます。
		ppiAxisX = new int[piFloor.length][];
		ppiAxisY = new int[piFloor.length][];
		ppiAxisZ = new int[piFloor.length][];
		ppiInnerOuter = new int[piFloor.length][];
		for( i = 0;i < piFloor.length; i++ )
		{
			ppiAxisX[i] = new int[piFloor[i]];
			ppiAxisY[i] = new int[piFloor[i]];
			ppiAxisZ[i] = new int[piFloor[i]];
			ppiInnerOuter[i] = new int[piFloor[i]];
		}
		for( i = 0;i < piFloor.length; i++ )
		{
			for( j = 0;j < ppiAxisX[i].length; j++ )
			{
				ppiAxisX[i][j] 		= ppiXYData[iLoc + j][0];
				ppiAxisY[i][j] 		= ppiXYData[iLoc + j][1];
				ppiAxisZ[i][j] 		= ppiXYData[iLoc + j][2];
				ppiInnerOuter[i][j] = ppiXYData[iLoc + j][4];
			}
			iLoc += piFloor[i];
		}
		csv.vClose();
	}

	/**
	 * <PRE>
	 *    各階のX座標データを取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	各階のX座標を格納した2次元配列
	 */
	public int[][] ppiGetX()
	{
		// TODO 自動生成されたメソッド・スタブ
		return ppiAxisX;
	}

	/**
	 * <PRE>
	 *    各階のY座標データを取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	各階のY座標を格納した2次元配列
	 */
	public int[][] ppiGetY()
	{
		// TODO 自動生成されたメソッド・スタブ
		return ppiAxisY;
	}

	/**
	 * <PRE>
	 *    各階のZ座標データを取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	各階のZ座標を格納した2次元配列
	 */
	public int[][] ppiGetZ()
	{
		// TODO 自動生成されたメソッド・スタブ
		return ppiAxisZ;
	}

	/**
	 * <PRE>
	 *    各階の座標点データ数を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	各階の座標点データ数を格納した1次元配列
	 */
	public int[] piGetFloor()
	{
		// TODO 自動生成されたメソッド・スタブ
		return piFloor;
	}

	/**
	 * <PRE>
	 *    各階の病院の外枠の座標点データ数を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	各階の病院の外枠の座標点データ数を格納した1次元配列
	 */
	public int[][] ppiGetInnerOuter()
	{
		return ppiInnerOuter;
	}

	/**
	 * <PRE>
	 *    ノード、リンクのインスタンスを設定します。
	 * </PRE>
	 * @param cErNodeManagerData	ノード及びリンクデータを格納したNodeManagerのインスタンス
	 */
	public void vSetNodeManager( ERTriageNodeManager cErNodeManagerData )
	{
		// TODO 自動生成されたメソッド・スタブ
		cErNodeManager = cErNodeManagerData;
	}

	/**
	 * <PRE>
	 *    ノード、リンクが書かれたファイルを読み込み、各部屋へ割り付けをします。
	 *    なお、医師、看護師、医療技師エージェントも同様に割り当てをします。
	 * </PRE>
	 * @param strFileName	ノード、リンクが記述されたファイルパス名
	 */
	public void vReadNodeManager( String strFileName )
	{
		int i;
		int aiLoc[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		List<ERTriageNode> ArrayListERNode;
		// TODO 自動生成されたメソッド・スタブ
		cErNodeManager = new ERTriageNodeManager( strFileName );
		ArrayListERNode = cErNodeManager.getAllReference();

		// ノードに付随している施設番号を元に各部屋エージェントへ座標を割り当てていきます。
		for( ERTriageNode cCurNode:ArrayListERNode )
		{
			// 診察室の場合
			if( cCurNode.iGetLocation() == 1 )
			{
				int iLoc = aiLoc[0];
				if( iLoc < ArrayListConsultationRooms.size() )
				{
					// 中心座標を設定して、ノードと紐付けをします。
					// 診察室の座標を設定します。
					ArrayListConsultationRooms.get(iLoc).setPosition( cCurNode.getPosition() );
					// ノードを各部屋エージェントに持たせます。
					ArrayListConsultationRooms.get(iLoc).vSetTriageNode( cCurNode );
					// 全体のノード情報を持たせます。
					ArrayListConsultationRooms.get(iLoc).vSetERTriageNodeManager( cErNodeManager );
					// 診察室に所属しているエージェントの座標を指定します。
					ArrayListConsultationRooms.get(iLoc).vSetAffiliationAgentPosition();
				}
				aiLoc[0]++;
			}
			else if( cCurNode.iGetLocation() == 2 )
			{
				int iLoc = aiLoc[1];
				if( iLoc < ArrayListOperationRooms.size() )
				{
					// 中心座標を設定して、ノードと紐付けをします。
					ArrayListOperationRooms.get(iLoc).setPosition( cCurNode.getPosition() );
					// ノードを手術室エージェントに持たせます。
					ArrayListOperationRooms.get(iLoc).vSetTriageNode( cCurNode );
					// 全体のノード情報を持たせます。
					ArrayListOperationRooms.get(iLoc).vSetERTriageNodeManager( cErNodeManager );
					// 手術室に所属しているエージェントの座標を指定します。
					ArrayListOperationRooms.get(iLoc).vSetAffiliationAgentPosition();
				}
				aiLoc[1]++;
			}
			else if( cCurNode.iGetLocation() == 3 )
			{
				int iLoc = aiLoc[2];
				if( iLoc < ArrayListEmergencyRooms.size() )
				{
					// 中心座標を設定して、ノードと紐付けをします。
					ArrayListEmergencyRooms.get(iLoc).setPosition( cCurNode.getPosition() );
					// ノードを初療室エージェントに持たせます。
					ArrayListEmergencyRooms.get(iLoc).vSetTriageNode( cCurNode );
					// 全体のノード情報を持たせます。
					ArrayListEmergencyRooms.get(iLoc).vSetERTriageNodeManager( cErNodeManager );
					// 初療室に所属しているエージェントの座標を指定します。
					ArrayListEmergencyRooms.get(iLoc).vSetAffiliationAgentPosition();
				}
				aiLoc[2]++;
			}
			else if( cCurNode.iGetLocation() == 4 )
			{
				int iLoc = aiLoc[3];
//				if( iLoc < ArrayListObservationRooms.size() )
//				{
					// 中心座標を設定して、ノードと紐付けをします。
					ArrayListObservationRooms.get(0).setPosition( cCurNode.getPosition() );
					// 観察室に所属しているエージェントの座標を指定します。
					ArrayListObservationRooms.get(0).vSetAffiliationAgentPosition( cCurNode, iLoc );
					// ノードを観察室エージェントに持たせます。
					ArrayListObservationRooms.get(0).vSetTriageNode( cCurNode );
					ArrayListObservationRooms.get(0).vSetERTriageNodeManager( cErNodeManager );
//				}
				aiLoc[3]++;
			}
			else if( cCurNode.iGetLocation() == 5 )
			{
				int iLoc = aiLoc[4];
//				if( iLoc < ArrayListSevereInjuryObservationRooms.size() )
//				{
					// 中心座標を設定して、ノードと紐付けをします。
					ArrayListSevereInjuryObservationRooms.get(0).setPosition( cCurNode.getPosition() );
					// 重症観察室に所属しているエージェントの座標を指定します。
					ArrayListSevereInjuryObservationRooms.get(0).vSetAffiliationAgentPosition( cCurNode, iLoc );
					// ノードを重症観察室エージェントに持たせます。
					ArrayListSevereInjuryObservationRooms.get(0).vSetTriageNode( cCurNode );
					ArrayListSevereInjuryObservationRooms.get(0).vSetERTriageNodeManager( cErNodeManager );
//				}
				aiLoc[4]++;
			}
			else if( cCurNode.iGetLocation() == 6 )
			{
				int iLoc = aiLoc[5];
//				if( iLoc < ArrayListIntensiveCareUnitRooms.size() )
//				{
					// 中心座標を設定して、ノードと紐付けをします。
					ArrayListIntensiveCareUnitRooms.get(0).setPosition( cCurNode.getPosition() );
					// 集中治療室に所属しているエージェントの座標を指定します。
					ArrayListIntensiveCareUnitRooms.get(0).vSetAffiliationAgentPosition( cCurNode, iLoc );
					// ノードを集中治療室エージェントに持たせます。
					ArrayListIntensiveCareUnitRooms.get(0).vSetTriageNode( cCurNode );
					ArrayListIntensiveCareUnitRooms.get(0).vSetERTriageNodeManager( cErNodeManager );
//				}
				aiLoc[5]++;
			}
			else if( cCurNode.iGetLocation() == 7 )
			{
				int iLoc = aiLoc[6];
//				if( iLoc < ArrayListHighCareUnitRooms.size() )
//				{
					// 中心座標を設定して、ノードと紐付けをします。
					ArrayListHighCareUnitRooms.get(0).setPosition( cCurNode.getPosition() );
					// 高度治療室に所属しているエージェントの座標を指定します。
					ArrayListHighCareUnitRooms.get(0).vSetAffiliationAgentPosition( cCurNode, iLoc );
					// ノードを高度治療室エージェントに持たせます。
					ArrayListHighCareUnitRooms.get(0).vSetTriageNode( cCurNode );
					ArrayListHighCareUnitRooms.get(0).vSetERTriageNodeManager( cErNodeManager );
//				}
				aiLoc[6]++;
			}
			else if( cCurNode.iGetLocation() == 8 )
			{
				int iLoc = aiLoc[7];
				if( iLoc < ArrayListGeneralWardRooms.size() )
				{
					// 中心座標を設定して、ノードと紐付けをします。
					ArrayListGeneralWardRooms.get(iLoc).setPosition( cCurNode.getPosition() );
					// 一般病棟に所属しているエージェントの座標を指定します。
					ArrayListGeneralWardRooms.get(iLoc).vSetAffiliationAgentPosition();
					// ノードを一般病棟エージェントに持たせます。
					ArrayListGeneralWardRooms.get(iLoc).vSetTriageNode( cCurNode );
					ArrayListGeneralWardRooms.get(iLoc).vSetERTriageNodeManager( cErNodeManager );
				}
				aiLoc[7]++;
			}
			else if( cCurNode.iGetLocation() == 9 )
			{
				int iLoc = aiLoc[8];
				// 中心座標を設定して、ノードと紐付けをします。
				erWaitingRoom.setPosition( cCurNode.getPosition() );
				// 待合室に所属しているエージェントの座標を指定します。
				erWaitingRoom.vSetAffiliationAgentPosition();
				// ノードを待合室エージェントに持たせます。
				erWaitingRoom.vSetTriageNode( cCurNode );
				erWaitingRoom.vSetERTriageNodeManager( cErNodeManager );
				aiLoc[8]++;
			}
			else if( cCurNode.iGetLocation() == 10 )
			{
				int iLoc = aiLoc[9];
				if( iLoc < ArrayListExaminationXRayRooms.size() )
				{
					// 中心座標を設定して、ノードと紐付けをします。
					ArrayListExaminationXRayRooms.get(iLoc).setPosition( cCurNode.getPosition() );
					// ノードをX線室エージェントに持たせます。
					ArrayListExaminationXRayRooms.get(iLoc).vSetTriageNode( cCurNode );
					// 全体のノード情報を持たせます。
					ArrayListExaminationXRayRooms.get(iLoc).vSetERTriageNodeManager( cErNodeManager );
					// X線室に所属しているエージェントの座標を指定します。
					ArrayListExaminationXRayRooms.get(iLoc).vSetAffiliationAgentPosition();
				}
				aiLoc[9]++;
			}
			else if( cCurNode.iGetLocation() == 11 )
			{
				int iLoc = aiLoc[10];
				if( iLoc < ArrayListExaminationCTRooms.size() )
				{
					// 中心座標を設定して、ノードと紐付けをします。
					ArrayListExaminationCTRooms.get(iLoc).setPosition( cCurNode.getPosition() );
					// ノードをCT室エージェントに持たせます。
					ArrayListExaminationCTRooms.get(iLoc).vSetTriageNode( cCurNode );
					// 全体のノード情報を持たせます。
					ArrayListExaminationCTRooms.get(iLoc).vSetERTriageNodeManager( cErNodeManager );
					// CT室に所属しているエージェントの座標を指定します。
					ArrayListExaminationCTRooms.get(iLoc).vSetAffiliationAgentPosition();
				}
				aiLoc[10]++;
			}
			else if( cCurNode.iGetLocation() == 12 )
			{
				int iLoc = aiLoc[11];
				if( iLoc < ArrayListExaminationMRIRooms.size() )
				{
					// 中心座標を設定して、ノードと紐付けをします。
					ArrayListExaminationMRIRooms.get(iLoc).setPosition( cCurNode.getPosition() );
					// ノードをMRI線室エージェントに持たせます。
					ArrayListExaminationMRIRooms.get(iLoc).vSetTriageNode( cCurNode );
					// 全体のノード情報を持たせます。
					ArrayListExaminationMRIRooms.get(iLoc).vSetERTriageNodeManager( cErNodeManager );
					// MRI室に所属しているエージェントの座標を指定します。
					ArrayListExaminationMRIRooms.get(iLoc).vSetAffiliationAgentPosition();
				}
				aiLoc[11]++;
			}
			else if( cCurNode.iGetLocation() == 13 )
			{
				int iLoc = aiLoc[12];
				if( iLoc < ArrayListExaminationAngiographyRooms.size() )
				{
					// 中心座標を設定して、ノードと紐付けをします。
					ArrayListExaminationAngiographyRooms.get(iLoc).setPosition( cCurNode.getPosition() );
					// ノードを血管造影室室エージェントに持たせます。
					ArrayListExaminationAngiographyRooms.get(iLoc).vSetTriageNode( cCurNode );
					// ノード全体を持たせます。
					ArrayListExaminationAngiographyRooms.get(iLoc).vSetERTriageNodeManager( cErNodeManager );
					// 血管造影室に所属しているエージェントの座標を指定します。
					ArrayListExaminationAngiographyRooms.get(iLoc).vSetAffiliationAgentPosition();
				}
				aiLoc[12]++;
			}
			else if( cCurNode.iGetLocation() == 14 )
			{
				int iLoc = aiLoc[13];
				if( iLoc < ArrayListExaminationFastRooms.size() )
				{
					// 中心座標を設定して、ノードと紐付けをします。
					ArrayListExaminationFastRooms.get(iLoc).setPosition( cCurNode.getPosition() );
					// ノードをFast室エージェントに持たせます。
					ArrayListExaminationFastRooms.get(iLoc).vSetTriageNode( cCurNode );
					// ノード全体を持たせます。
					ArrayListExaminationFastRooms.get(iLoc).vSetERTriageNodeManager( cErNodeManager );
					// Fast室に所属しているエージェントの座標を指定します。
					ArrayListExaminationFastRooms.get(iLoc).vSetAffiliationAgentPosition();
				}
				aiLoc[13]++;
			}
			else if( cCurNode.iGetLocation() == 15 )
			{
				int iLoc = aiLoc[14];
				if( iLoc < ArrayListStairs.size() )
				{
					// 中心座標を設定して、ノードと紐付けをします。
					ArrayListStairs.get(iLoc).setPosition( cCurNode.getPosition() );
					// ノードを階段アセットに持たせます。
					ArrayListStairs.get(iLoc).vSetTriageNode( cCurNode );
					ArrayListStairs.get(iLoc).vSetERTriageNodeManager( cErNodeManager );
				}
				aiLoc[14]++;
			}
			else if( cCurNode.iGetLocation() == 16 )
			{
				int iLoc = aiLoc[15];
				if( iLoc < ArrayListElevators.size() )
				{
					// 中心座標を設定して、ノードと紐付けをします。
					ArrayListElevators.get(iLoc).setPosition( cCurNode.getPosition() );
					// ノードをエレベーターアセットに持たせます。
					ArrayListElevators.get(iLoc).vSetTriageNode( cCurNode );
					ArrayListElevators.get(iLoc).vSetERTriageNodeManager( cErNodeManager );
				}
				aiLoc[15]++;
			}
			// 外の場合、患者をそこに設定します。
			else if( cCurNode.iGetLocation() == -1 )
			{
				// 外から患者が来院するものとして、患者を所定のノードに設定します。
				erOutside.setPosition( cCurNode.getPosition() );
				erOutside.vSetTriageNode( cCurNode );
				erOutside.vSetERTriageNodeManager( cErNodeManager );
				erOutside.vSetAffiliationAgentPosition();
			}
		}
	}

	/**
	 * <PRE>
	 *    ノード、リンクが書かれたファイルを読み込み、各部屋へ割り付けをします。
	 *    なお、医師、看護師、医療技師エージェントも同様に割り当てをします。
	 * </PRE>
	 */
	public void vSetNodeLink()
	{
		int i;
		int iLoc = 0;
		double lfX,lfY,lfZ;
		double lfDist = 0.0;
		double lfMinDist = Double.MAX_VALUE;
		double lfX1, lfX2;
		List<ERTriageNode> ArrayListERNode;
		ERTriageNode cMinNode = new ERTriageNode(0.0,0.0,0.0);
		// TODO 自動生成されたメソッド・スタブ
		cErNodeManager = new ERTriageNodeManager();
		ArrayListERNode = cErNodeManager.getAllReference();

	// 各部屋の中心座標をノードに割り当てます。

		// 各診察室の中心座標を設定します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			lfX = ArrayListConsultationRooms.get(i).getX();
			lfY = ArrayListConsultationRooms.get(i).getY();
			lfZ = ArrayListConsultationRooms.get(i).getZ();
			ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, 1, 1 ) );
		}
		// 各手術室の中心座標を設定します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			lfX = ArrayListOperationRooms.get(i).getX();
			lfY = ArrayListOperationRooms.get(i).getY();
			lfZ = ArrayListOperationRooms.get(i).getZ();
			ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, 2, 2 ) );
		}
		// 各初療室の中心座標を設定します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			lfX = ArrayListEmergencyRooms.get(i).getX();
			lfY = ArrayListEmergencyRooms.get(i).getY();
			lfZ = ArrayListEmergencyRooms.get(i).getZ();
			ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, 3, 1 ) );
		}
		// 各観察屋の中心座標を設定します。
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			lfX = ArrayListObservationRooms.get(i).getX();
			lfY = ArrayListObservationRooms.get(i).getY();
			lfZ = ArrayListObservationRooms.get(i).getZ();
			ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, 4, 1 ) );
		}
		// 各重症観察室の中心座標を設定します。
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			lfX = ArrayListSevereInjuryObservationRooms.get(i).getX();
			lfY = ArrayListSevereInjuryObservationRooms.get(i).getY();
			lfZ = ArrayListSevereInjuryObservationRooms.get(i).getZ();
			ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, 5, 1 ) );
		}
		// 各集中治療室の中心座標を設定します。
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			lfX = ArrayListIntensiveCareUnitRooms.get(i).getX();
			lfY = ArrayListIntensiveCareUnitRooms.get(i).getY();
			lfZ = ArrayListIntensiveCareUnitRooms.get(i).getZ();
			ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, 6, 3 ) );
		}
		// 各高度治療室の中心座標を設定します。
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			lfX = ArrayListHighCareUnitRooms.get(i).getX();
			lfY = ArrayListHighCareUnitRooms.get(i).getY();
			lfZ = ArrayListHighCareUnitRooms.get(i).getZ();
			ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, 7, 3 ) );
		}
		// 各一般病棟の中心座標を設定します。
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			lfX = ArrayListGeneralWardRooms.get(i).getX();
			lfY = ArrayListGeneralWardRooms.get(i).getY();
			lfZ = ArrayListGeneralWardRooms.get(i).getZ();
			ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, 8, 3 ) );
		}
		// 各待合室の中心座標を設定します。
		lfX = erWaitingRoom.getX();
		lfY = erWaitingRoom.getY();
		lfZ = erWaitingRoom.getZ();
		ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, 9, 1 ) );

		// 各X線室の中心座標を設定します。
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			lfX = ArrayListExaminationXRayRooms.get(i).getX();
			lfY = ArrayListExaminationXRayRooms.get(i).getY();
			lfZ = ArrayListExaminationXRayRooms.get(i).getZ();
			ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, 10, 1 ) );
		}
		// 各CT室の中心座標を設定します。
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			lfX = ArrayListExaminationCTRooms.get(i).getX();
			lfY = ArrayListExaminationCTRooms.get(i).getY();
			lfZ = ArrayListExaminationCTRooms.get(i).getZ();
			ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, 11, 1 ) );
		}
		// 各MRI室の中心座標を設定します。
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			lfX = ArrayListExaminationMRIRooms.get(i).getX();
			lfY = ArrayListExaminationMRIRooms.get(i).getY();
			lfZ = ArrayListExaminationMRIRooms.get(i).getZ();
			ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, 12, 1 ) );
		}
		// 各血管造影室の中心座標を設定します。
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			lfX = ArrayListExaminationAngiographyRooms.get(i).getX();
			lfY = ArrayListExaminationAngiographyRooms.get(i).getY();
			lfZ = ArrayListExaminationAngiographyRooms.get(i).getZ();
			ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, 13, 2 ) );
		}
		// 各超音波室の中心座標を設定します。
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			lfX = ArrayListExaminationFastRooms.get(i).getX();
			lfY = ArrayListExaminationFastRooms.get(i).getY();
			lfZ = ArrayListExaminationFastRooms.get(i).getZ();
			ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, 14, 2 ) );
		}
		// 病院外の中心座標を設定します。
		lfX = erOutside.getX();
		lfY = erOutside.getY();
		lfZ = erOutside.getZ();
		ArrayListERNode.add( new ERTriageNode( lfX, lfY, lfZ, -1, 1 ) );

	// 各部屋間を移動する際に必要なノードを作成します。
		for( ERTriageNode cCurNode:ArrayListERNode )
		{
			iLoc = cCurNode.iGetLocation();
			for( ERTriageNode cSelectNode:ArrayListERNode )
			{
				if( cSelectNode.iGetLocation() != iLoc )
				{
					lfDist = cCurNode.getPosition().getDistance( cSelectNode.getPosition() );
					if( lfMinDist > lfDist )
					{
						lfMinDist = lfDist;
						cMinNode = cSelectNode;
					}
				}
			}
			lfX1 = cMinNode.getPosition().getX();
			lfX2 = cCurNode.getPosition().getX();
			lfY = cCurNode.getPosition().getY();
			lfZ = cCurNode.getPosition().getZ();
			cMinNode.getPosition().setPosition( (lfX1+lfX2)/2, lfY, lfZ );
			cMinNode.vSetLocation( 0 );
		}
	}

	/**
	 * <PRE>
	 *   患者エージェントの座標を設定します。
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
			lfZ = this.getPosition().getZ();
			ArrayListPatientAgents.get(i).setPosition( lfX, lfY, lfZ );
		}
	}

	/**
	 * <PRE>
	 *   診察室の部屋数を設定します。
	 * </PRE>
	 * @param iRoomNum 診察室の部屋数
	 */
	public void vSetConsultationRoomNum( int iRoomNum )
	{
		// 部屋数を設定します。
		iConsultationRoomNum = iRoomNum;
	}

	/**
	 * <PRE>
	 *   診察室の部屋数を取得します。
	 * </PRE>
	 * @return  診察室の部屋数
	 */
	public double iGetConsultationRoomNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iConsultationRoomNum;
	}

	/**
	 * <PRE>
	 *   手術室の部屋数を取得します。
	 * </PRE>
	 * @return 手術室の部屋数
	 */
	public double iGetOperationRoomNum()
	{
		return iOperationRoomNum;
	}

	/**
	 * <PRE>
	 *   初療室の部屋数を取得します。
	 * </PRE>
	 * @return 初療室の部屋数
	 */
	public double iGetEmergencyRoomNum()
	{
		return iEmergencyRoomNum;
	}

	/**
	 * <PRE>
	 *   観察室の部屋数を取得します。
	 * </PRE>
	 * @return  観察室の部屋数
	 */
	public double iGetObservationRoomNum()
	{
		return iObservationRoomNum;
	}

	/**
	 * <PRE>
	 *   重症観察室の部屋数を取得します。
	 * </PRE>
	 * @return 重症観察室の部屋数
	 */
	public double iGetInjurySevereObservationRoomNum()
	{
		return iSevereInjuryObservationRoomNum;
	}

	/**
	 * <PRE>
	 *   集中治療室の部屋数を取得します。
	 * </PRE>
	 * @return 集中治療室の部屋数
	 */
	public double iGetIntensiveCareUnitRoomNum()
	{
		return iIntensiveCareUnitRoomNum;
	}

	/**
	 * <PRE>
	 *   高度治療室の部屋数を取得します。
	 * </PRE>
	 * @return 高度治療室の部屋数
	 */
	public double iGetHighCareUnitRoomNum()
	{
		return iHighCareUnitRoomNum;
	}

	/**
	 * <PRE>
	 *   待合室の部屋数を取得します。
	 * </PRE>
	 * @return 待合室の部屋数
	 */
	public double iGetWaitingRoomNum()
	{
		return iWaitingRoomNum;
	}

	/**
	 * <PRE>
	 *   一般病棟室の部屋数を取得します。
	 * </PRE>
	 * @return 一般病棟室の部屋数
	 */
	public double iGetGeneralWardRoomNum()
	{
		return iGeneralWardRoomNum;
	}

	/**
	 * <PRE>
	 *   Ｘ線室の部屋数を取得します。
	 * </PRE>
	 * @return X線室の部屋数
	 */
	public double iGetExaminationXRayRoomNum()
	{
		return iXRayRoomNum;
	}

	/**
	 * <PRE>
	 *   CT線室の部屋数を取得します。
	 * </PRE>
	 * @return CT線室の部屋数
	 */
	public double iGetExaminationCTRoomNum()
	{
		return iCTRoomNum;
	}

	/**
	 * <PRE>
	 *   血管造影室の部屋数を取得します。
	 * </PRE>
	 * @return 血管造影室の部屋数
	 */
	public double iGetExaminationAngiographyRoomNum()
	{
		return iAngiographyRoomNum;
	}

	/**
	 * <PRE>
	 *   MRI室の部屋数を取得します。
	 * </PRE>
	 * @return MRI室の部屋数
	 */
	public double iGetExaminationMRIRoomNum()
	{
		return iMRIRoomNum;
	}

	/**
	 * <PRE>
	 *   超音波室線室の部屋数を取得します。
	 * </PRE>
	 * @return 超音波室の部屋数
	 */
	public double iGetExaminationFastRoomNum()
	{
		return iFastRoomNum;
	}

	/**
	 * <PRE>
	 *   診察室の医師数を取得します。
	 * </PRE>
	 * @return 診察室の医師数
	 */
	public double iGetConsultationRoomDoctorNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iConsultationDoctorNum;
	}

	/**
	 * <PRE>
	 *   診察室の看護師数を取得します。
	 * </PRE>
	 * @return 診察室の看護師数
	 */
	public double iGetConsultationRoomNurseNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iConsultationNurseNum;
	}

	/**
	 * <PRE>
	 *   手術室の医師数を取得します。
	 * </PRE>
	 * @return 手術室の医師数
	 */
	public double iGetOperationRoomDoctorNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iOperationDoctorNum;
	}

	/**
	 * <PRE>
	 *   手術室の看護師数を取得します。
	 * </PRE>
	 * @return 手術室の看護師数
	 */
	public double iGetOperationRoomNurseNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iOperationNurseNum;
	}

	/**
	 * <PRE>
	 *   初療室の医師数を取得します。
	 * </PRE>
	 * @return 初療室の医師数
	 */
	public double iGetEmergencyRoomDoctorNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iEmergencyDoctorNum;
	}

	/**
	 * <PRE>
	 *   初療室の看護師数を取得します。
	 * </PRE>
	 * @return 初療室の看護師数
	 */
	public double iGetEmergencyRoomNurseNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iEmergencyNurseNum;
	}

	/**
	 * <PRE>
	 *   初療室の医療技師数を取得します。
	 * </PRE>
	 * @return 初療室の医療技師数
	 */
	public double iGetEmergencyRoomClinicalEngineerNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iEmergencyClinicalEngineerNum;
	}

	/**
	 * <PRE>
	 *   観察室の看護師数を取得します。
	 * </PRE>
	 * @return 観察室の看護師数
	 */
	public double iGetObservationRoomNurseNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iObservationNurseNum;
	}

	/**
	 * <PRE>
	 *   重症観察室の看護師数を取得します。
	 * </PRE>
	 * @return 重症観察室の看護師数
	 */
	public double iGetInjurySevereObservationRoomNurseNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iSevereInjuryObservationNurseNum;
	}

	/**
	 * <PRE>
	 *   集中治療室の医師数を取得します。
	 * </PRE>
	 * @return 集中治療室の医師数
	 */
	public double iGetIntensiveCareUnitRoomDoctorNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iIntensiveCareUnitDoctorNum;
	}

	/**
	 * <PRE>
	 *   集中治療室の看護師数を取得します。
	 * </PRE>
	 * @return 集中治療室の看護師数
	 */
	public double iGetIntensiveCareUnitRoomNurseNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iIntensiveCareUnitNurseNum;
	}

	/**
	 * <PRE>
	 *   高度治療室の医師数を取得します。
	 * </PRE>
	 * @return 高度治療室の医師数
	 */
	public double iGetHighCareUnitRoomDoctorNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iHighCareUnitDoctorNum;
	}

	/**
	 * <PRE>
	 *   高度治療室の看護師数を取得します。
	 * </PRE>
	 * @return 高度治療室の看護師数
	 */
	public double iGetHighCareUnitRoomNurseNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iHighCareUnitNurseNum;
	}

	/**
	 * <PRE>
	 *   一般病棟の医師数を取得します。
	 * </PRE>
	 * @return 一般病棟の医師数
	 */
	public double iGetGeneralWardRoomDoctorNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iGeneralWardDoctorNum;
	}

	/**
	 * <PRE>
	 *   一般病棟の看護師数を取得します。
	 * </PRE>
	 * @return 一般病棟の看護師数
	 */
	public double iGetGeneralWardRoomNurseNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iGeneralWardNurseNum;
	}

	/**
	 * <PRE>
	 *   待合室の看護師数を取得します。
	 * </PRE>
	 * @return 待合室の看護師数
	 */
	public double iGetWaitingRoomNurseNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iWaitingNurseNum;
	}

	/**
	 * <PRE>
	 *   X線室の医療技師数を取得します。
	 * </PRE>
	 * @return X線室の医療技師数
	 */
	public double iGetExaminationXRayRoomClinicalEngineerNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iXRayClinicalEngineerNum;
	}

	/**
	 * <PRE>
	 *   CT室の医療技師数を取得します。
	 * </PRE>
	 * @return CT室の医療技師数
	 */
	public double iGetExaminationCTRoomClinicalEngineerNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iCTClinicalEngineerNum;
	}

	/**
	 * <PRE>
	 *   MRI室の医療技師数を取得します。
	 * </PRE>
	 * @return MRI室の医療技師数
	 */
	public double iGetExaminationMRIRoomClinicalEngineerNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iMRIClinicalEngineerNum;
	}

	/**
	 * <PRE>
	 *   血管造影室の医療技師数を取得します。
	 * </PRE>
	 * @return 血管造影室の医療技師数
	 */
	public double iGetExaminationAnmgiographyRoomClinicalEngineerNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iAngiographyClinicalEngineerNum;
	}

	/**
	 * <PRE>
	 *   超音波室の医療技師数を取得します。
	 * </PRE>
	 * @return 超音波室の医療技師数
	 */
	public double iGetExaminationFastRoomClinicalEngineerNum()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iFastClinicalEngineerNum;
	}

	/**
	 * <PRE>
	 *   シミュレーションに登場する総患者数を取得します。
	 * </PRE>
	 * @return 総患者数
	 */
	public int iGetTotalPatientNum()
	{
		return iTotalPatientNum;
	}

	/**
	 * <PRE>
	 *   患者のなくなられた数を取得します。
	 * </PRE>
	 *
	 * @return 亡くなった患者の人数
	 */
	public int iGetDeathNum()
	{
		return iTotalPatientNum - erFinisher.iGetSurvivalNumber();
	}

	/**
	 * <PRE>
	 *    現在生存している患者の人数を取得します。
	 * </PRE>
	 * @return 生存数
	 */
	public int iGetSurvivalNum()
	{
		return erFinisher.iGetSurvivalNumber();
	}

	/**
	 * <PRE>
	 *   現在の平均生存確率を取得します。
	 * </PRE>
	 * @return 平均生存確率
	 */
	public double lfGetAvgSurvivalProbability()
	{
		return lfSurvivalProbability;
	}

	/**
	 * <PRE>
	 *   総シミュレーションステップ数を取得します。
	 * </PRE>
	 * @return 総シミュレーションステップ数
	 */
	public double lfGetTotalSimulationStep()
	{
		return lfEndTime/(erEngine.getLatestTimeStep()*0.001);
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用に外部引数にパラメータを設定します。
	 * </PRE>
	 * @param piArgument 逆シミュレーション用パラメータ
	 */
	public void vSetParameter( double piArgument[] )
	{
		// 部屋数を設定します。
		iConsultationRoomNum				= (int)piArgument[0];
		iOperationRoomNum					= (int)piArgument[1];
		iEmergencyRoomNum					= (int)piArgument[2];
		iObservationRoomNum					= (int)piArgument[3];
		iSevereInjuryObservationRoomNum		= (int)piArgument[4];
		iIntensiveCareUnitRoomNum			= (int)piArgument[5];
		iHighCareUnitRoomNum 				= (int)piArgument[6];
		iGeneralWardRoomNum					= (int)piArgument[7];
		iWaitingRoomNum 					= (int)piArgument[8];
		iXRayRoomNum						= (int)piArgument[9];
		iCTRoomNum							= (int)piArgument[10];
		iMRIRoomNum							= (int)piArgument[11];
		iAngiographyRoomNum					= (int)piArgument[12];
		iFastRoomNum						= (int)piArgument[13];

		// 部屋に所属するエージェント数を設定します。
		iConsultationDoctorNum				= (int)piArgument[14];
		iConsultationNurseNum				= (int)piArgument[15];
		iOperationDoctorNum					= (int)piArgument[16];
		iOperationNurseNum					= (int)piArgument[17];
		iEmergencyDoctorNum					= (int)piArgument[18];
		iEmergencyNurseNum					= (int)piArgument[19];
		iEmergencyClinicalEngineerNum		= (int)piArgument[20];
		iObservationNurseNum				= (int)piArgument[21];
		iSevereInjuryObservationNurseNum	= (int)piArgument[22];
		iIntensiveCareUnitDoctorNum			= (int)piArgument[23];
		iIntensiveCareUnitNurseNum			= (int)piArgument[24];
		iHighCareUnitDoctorNum				= (int)piArgument[25];
		iHighCareUnitNurseNum				= (int)piArgument[26];
		iGeneralWardDoctorNum				= (int)piArgument[27];
		iGeneralWardNurseNum				= (int)piArgument[28];
		iWaitingNurseNum					= (int)piArgument[29];
		iXRayClinicalEngineerNum			= (int)piArgument[30];
		iCTClinicalEngineerNum				= (int)piArgument[31];
		iMRIClinicalEngineerNum				= (int)piArgument[32];
		iAngiographyClinicalEngineerNum		= (int)piArgument[33];
		iFastClinicalEngineerNum			= (int)piArgument[34];
	}

	/**
	 * <PRE>
	 *    メルセンヌツイスターのインスタンスを設定します。
	 * </PRE>
	 * @param sfmtRandom メルセンヌツイスターのインスタンス
	 * @since 2016/07/27
	 * @author kobayashi
	 * @version 0.1
	 */
	public void vSetErDepartmentRandom(Rand sfmtRandom )
	{
		// TODO 自動生成されたメソッド・スタブ
		rnd = sfmtRandom;
	}

	/**
	 * <PRE>
	 *    診察室、初療室に在籍している患者の数を計算します。
	 *    NEDOCSにおいて使用します。
	 * </PRE>
	 * @throws ERNurseAgentException				看護師処理中の例外
	 * @throws ERDoctorAgentException				医師処理中の例外
	 * @throws ERClinicalEngineerAgentException		医療技師処理中の例外
	 * @since 2016/07/27
	 * @author kobayashi
	 * @version 0.1
	 */
	private void vEdAdmittedPatientNum() throws ERNurseAgentException, ERDoctorAgentException, ERClinicalEngineerAgentException
	{
		int i;

		iCurrentEdAdmittedAgentNum = 0;
		// 初療室プロセスを実行します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			iCurrentEdAdmittedAgentNum += ArrayListEmergencyRooms.get(i).iGetPatientAgentsNum();
		}

		// 診察室プロセスを実行します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			iCurrentEdAdmittedAgentNum += ArrayListConsultationRooms.get(i).iGetPatientAgentsNum();
		}

		// 手術室プロセスを実行します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			iCurrentEdAdmittedAgentNum += ArrayListOperationRooms.get(i).iGetPatientAgentsNum();
		}
		iEdAdmittedAgentNum += iCurrentEdAdmittedAgentNum;
	}

	/**
	 * <PRE>
	 *    診察室、初療室に在籍している人工呼吸器を装着している患者の数を計算します。
	 *    NEDOCSにおいて使用します。
	 * </PRE>
	 * @throws ERNurseAgentException				看護師処理中の例外
	 * @throws ERDoctorAgentException				医師処理中の例外
	 * @throws ERClinicalEngineerAgentException		医療技師処理中の例外
	 * @since 2016/07/27
	 * @author kobayashi
	 * @version 0.1
	 */
	private void vEdVentilatorsNum() throws ERNurseAgentException, ERDoctorAgentException, ERClinicalEngineerAgentException
	{
		int i;

		iCurrentEdVentilatorsNum = 0;

		// 初療室プロセスを実行します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			iCurrentEdVentilatorsNum += ArrayListEmergencyRooms.get(i).iGetPatientAgentsNum();
		}
		// 手術室プロセスを実行します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			iCurrentEdVentilatorsNum += ArrayListOperationRooms.get(i).iGetPatientAgentsNum();
		}
		// 集中治療室プロセスを実行します。
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			iCurrentEdVentilatorsNum += ArrayListIntensiveCareUnitRooms.get(i).iGetPatientAgentsNum();
		}
		iEdVentilatorsNum += iCurrentEdVentilatorsNum;
	}

	/**
	 * <PRE>
	 *    診察室、初療室、ICU、HCUに在籍している患者の数を計算します。
	 *    ED Work Scoreで使用します。
	 * </PRE>
	 * @throws ERNurseAgentException				看護師処理中の例外
	 * @throws ERDoctorAgentException				医師処理中の例外
	 * @throws ERClinicalEngineerAgentException		医療技師処理中の例外
	 * @since 2016/07/27
	 * @author kobayashi
	 * @version 0.1
	 */
	private void vEdTotalAdmittedPatientNum() throws ERNurseAgentException, ERDoctorAgentException, ERClinicalEngineerAgentException
	{
		int i;

		iTotalEdAdmittedAgentNum = 0;

		// 初療室プロセスを実行します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			iTotalEdAdmittedAgentNum += ArrayListEmergencyRooms.get(i).iGetPatientAgentsNum();
		}

		// 診察室プロセスを実行します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			iTotalEdAdmittedAgentNum += ArrayListConsultationRooms.get(i).iGetPatientAgentsNum();
		}

		// 手術室プロセスを実行します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			iTotalEdAdmittedAgentNum += ArrayListOperationRooms.get(i).iGetPatientAgentsNum();
		}

		// 集中治療室プロセスを実行します。
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			iTotalEdAdmittedAgentNum += ArrayListIntensiveCareUnitRooms.get(i).iGetPatientAgentsNum();
		}

		// 高度治療室プロセスを実行します。
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			iTotalEdAdmittedAgentNum += ArrayListHighCareUnitRooms.get(i).iGetPatientAgentsNum();
		}
	}

	/**
	 * <PRE>
	 *    診察室、手術室、初療室、ICU、HCU、観察室、重症観察室、一般病棟、待合室、X線室、CT室、MRI室、血管造影室、Fast室に在籍している患者の数を計算します。
	 *    ED Work Scoreで使用します。
	 * </PRE>
	 * @throws ERNurseAgentException				看護師処理中の例外
	 * @throws ERDoctorAgentException				医師処理中の例外
	 * @throws ERClinicalEngineerAgentException		医療技師処理中の例外
	 * @since 2016/07/27
	 * @author kobayashi
	 * @version 0.1
	 */
	private void vEdTotalPatientNum() throws ERNurseAgentException, ERDoctorAgentException, ERClinicalEngineerAgentException
	{
		int i;

		iCurrentTotalPatientAgentNum = 0;

		// 初療室プロセスを実行します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			iCurrentTotalPatientAgentNum += ArrayListEmergencyRooms.get(i).iGetPatientAgentsNum();
		}

		// 診察室プロセスを実行します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			iCurrentTotalPatientAgentNum += ArrayListConsultationRooms.get(i).iGetPatientAgentsNum();
		}

		// 手術室プロセスを実行します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			iCurrentTotalPatientAgentNum += ArrayListOperationRooms.get(i).iGetPatientAgentsNum();
		}

		// 観察室プロセスを実行します。
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			iCurrentTotalPatientAgentNum += ArrayListObservationRooms.get(i).iGetPatientAgentsNum();
		}

		// 重症観察室プロセスを実行します。
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			iCurrentTotalPatientAgentNum += ArrayListSevereInjuryObservationRooms.get(i).iGetPatientAgentsNum();
		}

		// 集中治療室プロセスを実行します。
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			iCurrentTotalPatientAgentNum += ArrayListIntensiveCareUnitRooms.get(i).iGetPatientAgentsNum();
		}

		// 高度治療室プロセスを実行します。
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			iCurrentTotalPatientAgentNum += ArrayListHighCareUnitRooms.get(i).iGetPatientAgentsNum();
		}

		// 一般病棟プロセスを実行します。
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			iCurrentTotalPatientAgentNum += ArrayListGeneralWardRooms.get(i).iGetPatientAgentsNum();
		}

		// X線室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			iCurrentTotalPatientAgentNum += ArrayListExaminationXRayRooms.get(i).iGetPatientAgentsNum();
		}

		// CT室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			iCurrentTotalPatientAgentNum += ArrayListExaminationCTRooms.get(i).iGetPatientAgentsNum();
		}

		// MRI室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			iCurrentTotalPatientAgentNum += ArrayListExaminationMRIRooms.get(i).iGetPatientAgentsNum();
		}

		// 血管造影室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			iCurrentTotalPatientAgentNum += ArrayListExaminationAngiographyRooms.get(i).iGetPatientAgentsNum();
		}

		// Fast室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			iCurrentTotalPatientAgentNum += ArrayListExaminationFastRooms.get(i).iGetPatientAgentsNum();
		}

		// 待合室プロセスを実行します。
		iCurrentTotalPatientAgentNum += erWaitingRoom.iGetTotalPatientNum();

		iTotalPatientAgentNum += iCurrentTotalPatientAgentNum;
	}

	/**
	 * <PRE>
	 *    トリアージ別受診者数を計算します。
	 * </PRE>
	 * @throws ERNurseAgentException				看護師処理中の例外
	 * @throws ERDoctorAgentException				医師処理中の例外
	 * @throws ERClinicalEngineerAgentException		医療技師処理中の例外
	 * @since 2016/07/27
	 * @author kobayashi
	 * @version 0.1
	 */
	private void vEdAdmittedPatientTriageCategoryNum() throws ERNurseAgentException, ERDoctorAgentException, ERClinicalEngineerAgentException
	{
		int i,j;

		for( j = 0;j < piCurrentTriageCategoryPatientNum.length; j++ )
			piCurrentTriageCategoryPatientNum[j] = 0;

		for( j = 0;j < 5; j++ )
		{
			piCurrentTriageCategoryPatientNum[j] += erWaitingRoom.iGetTriageCategoryPatientNum(j);
		}

		// 観察室プロセスを実行します。
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piCurrentTriageCategoryPatientNum[j] += ArrayListObservationRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 重症観察室プロセスを実行します。
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piCurrentTriageCategoryPatientNum[j] += ArrayListSevereInjuryObservationRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 初療室プロセスを実行します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piCurrentTriageCategoryPatientNum[j] += ArrayListEmergencyRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// X線室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piCurrentTriageCategoryPatientNum[j] += ArrayListExaminationXRayRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// CT室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piCurrentTriageCategoryPatientNum[j] += ArrayListExaminationCTRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// MRI室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piCurrentTriageCategoryPatientNum[j] += ArrayListExaminationMRIRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 血管造影室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piCurrentTriageCategoryPatientNum[j] += ArrayListExaminationAngiographyRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 診察室プロセスを実行します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piCurrentTriageCategoryPatientNum[j] += ArrayListConsultationRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 手術室プロセスを実行します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piCurrentTriageCategoryPatientNum[j] += ArrayListOperationRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 集中治療室プロセスを実行します。
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piCurrentTriageCategoryPatientNum[j] += ArrayListIntensiveCareUnitRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 高度治療室プロセスを実行します。
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piCurrentTriageCategoryPatientNum[j] += ArrayListHighCareUnitRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 一般病棟プロセスを実行します。
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piCurrentTriageCategoryPatientNum[j] += ArrayListGeneralWardRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}
		for( j = 0;j < piCurrentTriageCategoryPatientNum.length; j++ )
			piTriageCategoryPatientNum[j] += piCurrentTriageCategoryPatientNum[j];
	}
	/**
	 * <PRE>
	 *    待合室の患者の人数を計算します。
	 *    ED Work Scoreで使用します。
	 * </PRE>
	 * @throws ERNurseAgentException				看護師処理中の例外
	 * @throws ERDoctorAgentException				医師処理中の例外
	 * @throws ERClinicalEngineerAgentException		医療技師処理中の例外
	 * @since 2016/07/27
	 * @author kobayashi
	 * @version 0.1
	 */
	private void vWaitingRoomPatientNum() throws ERNurseAgentException, ERDoctorAgentException, ERClinicalEngineerAgentException
	{
		iCurrentWaitingRoomPatientNum = erWaitingRoom.iGetTotalPatientNum();
		iWaitingRoomPatientNum += iCurrentWaitingRoomPatientNum;
	}

	/**
	 * <PRE>
	 *    現在、待合室に最も長く病院にいる患者の在院時間を算出します。
	 * </PRE>
	 * @since 2016/07/27
	 * @author kobayashi
	 * @version 0.1
	 */
	private void vLongestAdmittedTime()
	{
		int i;
		double lfLongestTime = 0.0;

		// 初療室プロセスを実行します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			lfLongestTime = ArrayListEmergencyRooms.get(i).lfGetLongestStayPatient();
			if( lfLongestTime > lfLongestAdmittedTime )
			{
				lfLongestAdmittedTime = lfLongestTime;
			}
		}

		// 診察室プロセスを実行します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			lfLongestTime = ArrayListConsultationRooms.get(i).lfGetLongestStayPatient();
			if( lfLongestTime > lfLongestAdmittedTime )
			{
				lfLongestAdmittedTime = lfLongestTime;
			}
		}

		// 手術室プロセスを実行します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			lfLongestTime = ArrayListOperationRooms.get(i).lfGetLongestStayPatient();
			if( lfLongestTime > lfLongestAdmittedTime )
			{
				lfLongestAdmittedTime = lfLongestTime;
			}
		}

		// 観察室プロセスを実行します。
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			lfLongestTime = ArrayListObservationRooms.get(i).lfGetLongestStayPatient();
			if( lfLongestTime > lfLongestAdmittedTime )
			{
				lfLongestAdmittedTime = lfLongestTime;
			}
		}

		// 重症観察室プロセスを実行します。
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			lfLongestTime = ArrayListSevereInjuryObservationRooms.get(i).lfGetLongestStayPatient();
			if( lfLongestTime > lfLongestAdmittedTime )
			{
				lfLongestAdmittedTime = lfLongestTime;
			}
		}

		// 集中治療室プロセスを実行します。
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			lfLongestTime = ArrayListIntensiveCareUnitRooms.get(i).lfGetLongestStayPatient();
			if( lfLongestTime > lfLongestAdmittedTime )
			{
				lfLongestAdmittedTime = lfLongestTime;
			}
		}

		// 高度治療室プロセスを実行します。
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			lfLongestTime = ArrayListHighCareUnitRooms.get(i).lfGetLongestStayPatient();
			if( lfLongestTime > lfLongestAdmittedTime )
			{
				lfLongestAdmittedTime = lfLongestTime;
			}
		}

		// 一般病棟プロセスを実行します。
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			lfLongestTime = ArrayListGeneralWardRooms.get(i).lfGetLongestStayPatient();
			if( lfLongestTime > lfLongestAdmittedTime )
			{
				lfLongestAdmittedTime = lfLongestTime;
			}
		}

		// X線室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			lfLongestTime = ArrayListExaminationXRayRooms.get(i).lfGetLongestStayPatient();
			if( lfLongestTime > lfLongestAdmittedTime )
			{
				lfLongestAdmittedTime = lfLongestTime;
			}
		}

		// CT室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			lfLongestTime = ArrayListExaminationCTRooms.get(i).lfGetLongestStayPatient();
			if( lfLongestTime > lfLongestAdmittedTime )
			{
				lfLongestAdmittedTime = lfLongestTime;
			}
		}

		// MRI室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			lfLongestTime = ArrayListExaminationMRIRooms.get(i).lfGetLongestStayPatient();
			if( lfLongestTime > lfLongestAdmittedTime )
			{
				lfLongestAdmittedTime = lfLongestTime;
			}
		}

		// 血管造影室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			lfLongestTime = ArrayListExaminationAngiographyRooms.get(i).lfGetLongestStayPatient();
			if( lfLongestTime > lfLongestAdmittedTime )
			{
				lfLongestAdmittedTime = lfLongestTime;
			}
		}

		// Fast室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			lfLongestTime = ArrayListExaminationFastRooms.get(i).lfGetLongestStayPatient();
			if( lfLongestTime > lfLongestAdmittedTime )
			{
				lfLongestAdmittedTime = lfLongestTime;
			}
		}

		// 待合室プロセスを実行します。
		lfLongestTime = erWaitingRoom.lfGetLongestStayPatient();
		if( lfLongestTime > lfLongestAdmittedTime )
		{
			lfLongestAdmittedTime = lfLongestTime;
		}
	}

	/**
	 * <PRE>
	 *    現在、最後に病床に入った患者の到着から入院までの時間を算出します。
	 * </PRE>
	 * @since 2016/07/27
	 * @author kobayashi
	 * @version 0.1
	 */
	private void vLastBedTime()
	{
		int i;
		double lfLastTime = 0.0;
		double lfLongestTime = -Double.MAX_VALUE;

		// 初療室プロセスを実行します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			ArrayListEmergencyRooms.get(i).vLastBedTime();
			lfLongestTime = ArrayListEmergencyRooms.get(i).lfGetLongestStayHospitalTotalTime();
			if(lfLongestTime > lfLongestTotalTime )
			{
				lfLastBedTime = ArrayListEmergencyRooms.get(i).lfGetLastBedTime();
				lfLongestTotalTime = lfLongestTime;
			}
		}

		// 診察室プロセスを実行します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			ArrayListConsultationRooms.get(i).vLastBedTime();
			lfLongestTime = ArrayListConsultationRooms.get(i).lfGetLongestStayHospitalTotalTime();
			if(lfLongestTime > lfLongestTotalTime )
			{
				lfLastBedTime = ArrayListConsultationRooms.get(i).lfGetLastBedTime();
				lfLongestTotalTime = lfLongestTime;
			}
		}

		// 手術室プロセスを実行します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			ArrayListOperationRooms.get(i).vLastBedTime();
			lfLongestTime = ArrayListOperationRooms.get(i).lfGetLongestStayHospitalTotalTime();
			if(lfLongestTime > lfLongestTotalTime )
			{
				lfLastBedTime = ArrayListOperationRooms.get(i).lfGetLastBedTime();
				lfLongestTotalTime = lfLongestTime;
			}
		}

		// 観察室プロセスを実行します。
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			ArrayListObservationRooms.get(i).vLastBedTime();
			lfLongestTime = ArrayListObservationRooms.get(i).lfGetLongestStayHospitalTotalTime();
			if(lfLongestTime > lfLongestTotalTime )
			{
				lfLastBedTime = ArrayListObservationRooms.get(i).lfGetLastBedTime();
				lfLongestTotalTime = lfLongestTime;
			}
		}

		// 重症観察室プロセスを実行します。
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			ArrayListSevereInjuryObservationRooms.get(i).vLastBedTime();
			lfLongestTime = ArrayListSevereInjuryObservationRooms.get(i).lfGetLongestStayHospitalTotalTime();
			if(lfLongestTime > lfLongestTotalTime )
			{
				lfLastBedTime = ArrayListSevereInjuryObservationRooms.get(i).lfGetLastBedTime();
				lfLongestTotalTime = lfLongestTime;
			}
		}

		// 集中治療室プロセスを実行します。
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			ArrayListIntensiveCareUnitRooms.get(i).vLastBedTime();
			lfLongestTime = ArrayListIntensiveCareUnitRooms.get(i).lfGetLongestStayHospitalTotalTime();
			if(lfLongestTime > lfLongestTotalTime )
			{
				lfLastBedTime = ArrayListIntensiveCareUnitRooms.get(i).lfGetLastBedTime();
				lfLongestTotalTime = lfLongestTime;
			}
		}

		// 高度治療室プロセスを実行します。
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			ArrayListHighCareUnitRooms.get(i).vLastBedTime();
			lfLongestTime = ArrayListHighCareUnitRooms.get(i).lfGetLongestStayHospitalTotalTime();
			if(lfLongestTime > lfLongestTotalTime )
			{
				lfLastBedTime = ArrayListHighCareUnitRooms.get(i).lfGetLastBedTime();
				lfLongestTotalTime = lfLongestTime;
			}
		}

		// 一般病棟プロセスを実行します。
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			ArrayListGeneralWardRooms.get(i).vLastBedTime();
			lfLongestTime = ArrayListGeneralWardRooms.get(i).lfGetLongestStayHospitalTotalTime();
			if(lfLongestTime > lfLongestTotalTime )
			{
				lfLastBedTime = ArrayListGeneralWardRooms.get(i).lfGetLastBedTime();
				lfLongestTotalTime = lfLongestTime;
			}
		}

		// X線室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			ArrayListExaminationXRayRooms.get(i).vLastBedTime();
			lfLongestTime = ArrayListExaminationXRayRooms.get(i).lfGetLongestStayHospitalTotalTime();
			if(lfLongestTime > lfLongestTotalTime )
			{
				lfLastBedTime = ArrayListExaminationXRayRooms.get(i).lfGetLastBedTime();
				lfLongestTotalTime = lfLongestTime;
			}
		}

		// CT室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			ArrayListExaminationCTRooms.get(i).vLastBedTime();
			lfLongestTime = ArrayListExaminationCTRooms.get(i).lfGetLongestStayHospitalTotalTime();
			if(lfLongestTime > lfLongestTotalTime )
			{
				lfLastBedTime = ArrayListExaminationCTRooms.get(i).lfGetLastBedTime();
				lfLongestTotalTime = lfLongestTime;
			}
		}

		// MRI室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			ArrayListExaminationMRIRooms.get(i).vLastBedTime();
			lfLongestTime = ArrayListExaminationMRIRooms.get(i).lfGetLongestStayHospitalTotalTime();
			if(lfLongestTime > lfLongestTotalTime )
			{
				lfLastBedTime = ArrayListExaminationMRIRooms.get(i).lfGetLastBedTime();
				lfLongestTotalTime = lfLongestTime;
			}
		}

		// 血管造影室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			ArrayListExaminationAngiographyRooms.get(i).vLastBedTime();
			lfLongestTime = ArrayListExaminationAngiographyRooms.get(i).lfGetLongestStayHospitalTotalTime();
			if(lfLongestTime > lfLongestTotalTime )
			{
				lfLastBedTime = ArrayListExaminationAngiographyRooms.get(i).lfGetLastBedTime();
				lfLongestTotalTime = lfLongestTime;
			}
		}

		// Fast室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			ArrayListExaminationFastRooms.get(i).vLastBedTime();
			lfLongestTime = ArrayListExaminationFastRooms.get(i).lfGetLongestStayHospitalTotalTime();
			if(lfLongestTime > lfLongestTotalTime )
			{
				lfLastBedTime = ArrayListExaminationFastRooms.get(i).lfGetLastBedTime();
				lfLongestTotalTime = lfLongestTime;
			}
		}

		// 待合室プロセスを実行します。
		erWaitingRoom.vLastBedTime();
		lfLongestTime = erWaitingRoom.lfGetLongestStayHospitalTotalTime();
		if(lfLongestTime > lfLongestTotalTime )
		{
			lfLastBedTime = erWaitingRoom.lfGetLastBedTime();
			lfLongestTotalTime = lfLongestTime;
		}
		if( lfLastBedTime < 0.0 )
		{
			lfLastBedTime = 0.0;
		}
	}

	/**
	 * <PRE>
	 *    受診者数の総数を計算します。
	 * </PRE>
	 * @return 受診者数の総数
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public int iGetTotalEdAdmittedAgentNum()
	{
		return iTotalEdAdmittedAgentNum;
	}

	/**
	 * <PRE>
	 *    受診者数を計算します。
	 * </PRE>
	 * @return  受診者数
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public int iGetCurrentTotalEdAdmittedAgentNum()
	{
		return iCurrentTotalEdAdmittedAgentNum;
	}

	/**
	 * <PRE>
	 *    救急部門に来院した受診者数の総数を計算します。
	 * </PRE>
	 * @return 救急部門に来院した受診者数の総数
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public int iGetEdAdmittedAgentNum()
	{
		return iEdAdmittedAgentNum;
	}

	/**
	 * <PRE>
	 *    救急部門に来院した受診者数の総数を計算します。
	 * </PRE>
	 * @return 救急部門に来院した受診者数
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public int iGetCurrentEdAdmittedAgentNum()
	{
		return iCurrentEdAdmittedAgentNum;
	}

	/**
	 * <PRE>
	 *    人工呼吸器をつけている患者の総人数を計算します。
	 * </PRE>
	 * @return 人工呼吸器をつけている患者の総人数
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public int iGetEdVentilatorsNum()
	{
		return iEdVentilatorsNum;
	}

	/**
	 * <PRE>
	 *    人工呼吸器をつけている患者の人数を計算します。
	 * </PRE>
	 * @return 人工呼吸器をつけている患者の人数
	 * @author kobayashi
	 * @since 2016/07/27
	 * @version 0.1
	 */
	public int iGetCurrentEdVentilatorsNum()
	{
		return iCurrentEdVentilatorsNum;
	}

	/**
	 * <PRE>
	 *    シミュレーションにおける待合室の患者の総数を取得します。
	 *    ED Work Scoreで使用します。
	 * </PRE>
	 * @return 待合室の患者の総数
	 * @author kobayashi
	 * @since 2016/07/27
	 * @version 0.1
	 */
	public int iGetWaitingRoomPatientNum()
	{
		return iWaitingRoomPatientNum;
	}

	/**
	 * <PRE>
	 *    シミュレーションにおける待合室の患者の総数を取得します。
	 *    ED Work Scoreで使用します。
	 * </PRE>
	 * @return	現在の待合室でいる患者数
	 * @author kobayashi
	 * @since 2017/03/01
	 * @version 0.1
	 */
	public int iGetCurrentWaitingRoomPatientNum()
	{
		return iCurrentWaitingRoomPatientNum;
	}

	/**
	 * <PRE>
	 *    トリアージ別受診者総数を取得します。
	 * </PRE>
	 * @param iEmergencyLevel トリアージ緊急度レベル
	 * @return 該当する緊急度の患者の人数
	 * @author kobayashi
	 * @since 2016/07/27
	 * @version 0.1
	 */
	public int iGetTriageCategoryPatientNum( int iEmergencyLevel )
	{
		return piTriageCategoryPatientNum[iEmergencyLevel-1];
	}

	/**
	 * <PRE>
	 *    トリアージ別受診者数数を取得します。
	 * </PRE>
	 * @param iEmergencyLevel トリアージ緊急度レベル
	 * @return 該当する緊急度の患者の人数
	 * @author kobayashi
	 * @since 2017/03/01
	 * @version 0.1
	 */
	public int iGetCurrentTriageCategoryPatientNum( int iEmergencyLevel )
	{
		return piCurrentTriageCategoryPatientNum[iEmergencyLevel-1];
	}

	/**
	 * <PRE>
	 *    最も受診してからの経過時間の長い患者の滞在時間を取得します。
	 * </PRE>
	 * @return 最も受診してからの経過時間の長い患者の滞在時間
	 * @author kobayashi
	 * @since 2016/07/27
	 * @version 0.1
	 */
	public double lfGetLongestAdmittedTime()
	{
		return lfLongestAdmittedTime;
	}

	/**
	 * <PRE>
	 *    現在の最も受診してからの経過時間の長い患者の滞在時間を取得します。
	 * </PRE>
	 * @return 現在の最も受診してからの経過時間の長い患者の滞在時間
	 * @author kobayashi
	 * @since 2016/07/27
	 * @version 0.1
	 */
	public double lfGetCurrentLongestAdmittedTime()
	{
		return lfCurrentLongestAdmittedTime;
	}

	/**
	 * <PRE>
	 *   来院してから入院するまでの最も長い時間を取得します。
	 * </PRE>
	 * @return 来院してから入院するまでの最も長い時間
	 * @author kobayashi
	 * @since 2016/07/27
	 * @version 0.1
	 */
	public double lfGetLastBedTime()
	{
		return lfLastBedTime;
	}

	/**
	 * <PRE>
	 *   来院してから入院するまでの最も長い時間を取得します。
	 * </PRE>
	 * @return 来院してから入院するまでの最も長い時間
	 * @author kobayashi
	 * @since 2016/07/27
	 * @version 0.1
	 */
	public double lfGetCurrentLastBedTime()
	{
		return lfCurrentLastBedTime;
	}

	/**
	 * <PRE>
	 *    診察室、手術室、初療室、ICU、HCU、観察室、重症観察室、一般病棟、待合室、X線室、CT室、MRI室、血管造影室、Fast室に在籍している患者の総数を取得します。
	 *    NEDOCS, EDWIN, ED Work Scoreで使用します。
	 * </PRE>
	 * @return 患者の総数
	 * @author kobayashi
	 * @since 2016/08/09
	 * @version 0.1
	 */
	public int iGetTotalPatientAgentNum()
	{
		return iTotalPatientAgentNum;
	}

	/**
	 * <PRE>
	 *    診察室、手術室、初療室、ICU、HCU、観察室、重症観察室、一般病棟、待合室、X線室、CT室、MRI室、血管造影室、Fast室に在籍している患者の現在の数を取得します。
	 *    NEDOCS, EDWIN, ED Work Scoreで使用します。
	 * </PRE>
	 * @return 現時点での患者の総数
	 * @author kobayashi
	 * @since 2016/08/09
	 * @version 0.1
	 */
	public int iGetCurrentTotalPatientAgentNum()
	{
		return iCurrentTotalPatientAgentNum;
	}

	/**
	 * <PRE>
	 *   患者が退院したあるいは死亡するまでに病院に滞在した時間を取得します。
	 * </PRE>
	 * @return 病院に滞在した時間
	 * @author kobayashi
	 * @since 2016/08/09
	 * @version 0.1
	 */
	public double lfGetEndTime()
	{
		return lfEndTime;
	}

	/**
	 * <PRE>
	 *    診察室、初療室に在籍している患者の数を設定します。
	 *    NEDOCSにおいて使用します。
	 *
	 * </PRE>
	 * @param iData		診察室や初療室で受診している患者の数
	 * @since 2016/08/09
	 */
	public void vSetEdAdmittedAgentNum( int iData )
	{
		iEdAdmittedAgentNum = iData;
	}

	/**
	 * <PRE>
	 *    診察室、初療室、ICU、HCUに在籍している患者の数を設定します。
	 *    ED Work Scoreで使用します。
	 * </PRE>
	 * @param iData		診察室、初療室、ICU、HCUに在籍している患者数
	 * @since 2016/08/09
	 */
	public void vSetEdTotalAdmittedPatientNum( int iData )
	{
		iTotalEdAdmittedAgentNum = iData;
	}

	/**
	 * <PRE>
	 *    診察室、手術室、初療室、ICU、HCU、観察室、重症観察室、一般病棟、待合室、X線室、CT室、MRI室、血管造影室、Fast室に在籍している患者の数を設定します。
	 *    ED Work Scoreで使用します。
	 * </PRE>
	 * @param iData		診察室、手術室、初療室、ICU、HCU、観察室、重症観察室、一般病棟、待合室、X線室、CT室、MRI室、血管造影室、Fast室の患者数
	 * @since 2016/08/09
	 */
	public void vSetEdTotalPatientNum( int iData )
	{
		iTotalPatientAgentNum = iData;
	}

	/**
	 * <PRE>
	 *    トリアージ別受診者数を設定します。
	 * </PRE>
	 * @param iLoc 緊急度レベル
	 * @param iData 緊急度レベルに該当する患者数
	 * @since 2016/08/09
	 * @version 0.1
	 */
	public void vSetEdAdmittedPatientTriageCategoryNum( int iLoc, int iData )
	{
		piTriageCategoryPatientNum[iLoc] = iData;
	}

	/**
	 * <PRE>
	 *    人工呼吸器をつけている人数を設定します。
	 * </PRE>
	 * @param iData 人工呼吸器をつけている人数
	 * @since 2016/08/09
	 * @version 0.1
	 */
	public void vSetEdVentilatorsNum( int iData )
	{
		iEdVentilatorsNum = iData;
	}


	/**
	 * <PRE>
	 *    待合室の患者の人数を設定します。
	 *    ED Work Scoreで使用します。
	 * </PRE>
	 * @param iData 待合室の患者の人数
	 * @since 2016/08/09
	 * @version 0.1
	 */
	public void vSetWaitingRoomPatientNum( int iData )
	{
		iWaitingRoomPatientNum = iData;
	}

	/**
	 * <PRE>
	 *    現在、待合室に最も長く病院にいる患者の在院時間を設定します。
	 *    NEDOCSで使用します。
	 * </PRE>
	 * @param lfData 待合室に最も長く病院にいる患者の在院時間
	 * @since 2016/08/09
	 * @version 0.1
	 */
	public void vSetLongestAdmittedTime( double lfData )
	{
		lfLongestAdmittedTime = lfData;
	}

	/**
	 * <PRE>
	 *    現在、最後に病床に入った患者の到着から入院までの時間を設定します。
	 *    NEDOCSで使用します。
	 * </PRE>
	 * @param lfData 最後に病床に入った患者の到着から入院までの時間
	 * @since 2016/08/09
	 * @version 0.1
	 */
	public void vSetLastBedTime( double lfData )
	{
		lfLastBedTime = lfData;
		lfLongestTotalTime = 0.0;
	}


	/**
	 * <PRE>
	 *   救急部門の混雑状況を表したNEDOCS(National emergency department overcrowding study)指標を使用。
	 * </PRE>
	 * @return NEDOCS値
	 * @since 2016/08/12
	 * @version 0.1
	 */
	private double lfCalcNedocs()
	{
		int i;
		int iEdBeds = 0;
		int iHospitalBeds = 0;
		double lfTotalPatients = 0;
		double lfEdPatients = 0;
		double lfVentilators = 0;
		double lfLongestAdmit = 0.0;
		double lfLBedTime = 0.0;
		double lfRes = 0.0;

//		int iConsultationRoomNum = 0;
//		int iEmergencyRoomNum = 0;
//		int iDoctorNum = 0;
//		int iIcuRoomNum = 0;
//		int iHcuRoomNum = 0;
//		int iGeneralWardRoomNum = 0;
//		int iIcuNurseNum = 0;
//		int iHcuNurseNum = 0;
//		int iGeneralWardNurseNum = 0;
//
//		iConsultationRoomNum = ArrayListConsultationRooms.size();
//		iEmergencyRoomNum = ArrayListEmergencyRooms.size();
//		iIcuRoomNum = ArrayListIntensiveCareUnitRooms.size();
//		iHcuRoomNum = ArrayListHighCareUnitRooms.size();
//		iGeneralWardRoomNum = ArrayListGeneralWardRooms.size();
//		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
//			iIcuNurseNum += ArrayListIntensiveCareUnitRooms.get(i).iGetNurseAgentsNum();
//		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
//			iHcuNurseNum += ArrayListHighCareUnitRooms.get(i).iGetNurseAgentsNum();
//		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
//			iGeneralWardNurseNum += ArrayListGeneralWardRooms.get(i).iGetNurseAgentsNum();

		iEdBeds			= (int)(iConsultationRoomNum+iEmergencyRoomNum+iIntensiveCareUnitRoomNum*iIntensiveCareUnitNurseNum+iHighCareUnitRoomNum*iHighCareUnitNurseNum);
		iHospitalBeds	= (int)(iIntensiveCareUnitRoomNum*iIntensiveCareUnitNurseNum+iHighCareUnitRoomNum*iHighCareUnitNurseNum+iGeneralWardRoomNum*iGeneralWardNurseNum);
		lfTotalPatients	= iCurrentTotalPatientAgentNum;
		lfEdPatients	= iCurrentEdAdmittedAgentNum;
		lfVentilators	= iCurrentEdVentilatorsNum;
		lfLongestAdmit	= lfLongestAdmittedTime;
		lfLBedTime		= lfLastBedTime;
		lfRes = -20.0+85.8*( lfTotalPatients/iEdBeds )+600.0*( lfEdPatients/iHospitalBeds )+13.4*lfVentilators+0.93*lfLongestAdmit+5.64*lfLBedTime;
		lfRes = lfRes < 0.0 ? 10000.0 : lfRes;
		return lfRes;
	}

	/**
	 * <PRE>
	 *   現時刻での救急部門の仕事量を表したED work Score指標を算出します。
	 * </PRE>
	 * @return	ED Work Score値を返します。
	 * @since 2016/08/12
	 * @version 0.1
	 */
	private double lfCalcEdWorkScore()
	{
		int i;
		int iTotalEdBeds = 0;
		int iHospitalBeds = 0;
		int iNurseNum = 0;
		double lfWaitingRoomPatients = 0;
		double lfEdPatients = 0;
		double lfTriageResult = 0;
//		int iConsultationRoomNum = 0;
//		int iEmergencyRoomNum = 0;
//		int iDoctorNum = 0;
//		int iIcuRoomNum = 0;
//		int iHcuRoomNum = 0;
//		int iGeneralWardRoomNum = 0;
//		int iIcuNurseNum = 0;
//		int iHcuNurseNum = 0;
//		int iGeneralWardNurseNum = 0;

//		iConsultationRoomNum = ArrayListConsultationRooms.size();
//		iEmergencyRoomNum = ArrayListEmergencyRooms.size();
//		iIcuRoomNum = ArrayListIntensiveCareUnitRooms.size();
//		iHcuRoomNum = ArrayListHighCareUnitRooms.size();
//		iGeneralWardRoomNum = ArrayListGeneralWardRooms.size();
//		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
//			iIcuNurseNum += ArrayListIntensiveCareUnitRooms.get(i).iGetNurseAgentsNum();
//		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
//			iHcuNurseNum += ArrayListHighCareUnitRooms.get(i).iGetNurseAgentsNum();
//		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
//			iGeneralWardNurseNum += ArrayListGeneralWardRooms.get(i).iGetNurseAgentsNum();


		lfWaitingRoomPatients	= iCurrentWaitingRoomPatientNum;
//		iTotalEdBeds			= (int)(iConsultationRoomNum+iEmergencyRoomNum+iIntensiveCareUnitRoomNum*iIntensiveCareUnitNurseNum+iHighCareUnitRoomNum*iHighCareUnitNurseNum);
		iTotalEdBeds			= (int)(iConsultationRoomNum+iEmergencyRoomNum);
		iHospitalBeds			= (int)(iIntensiveCareUnitRoomNum*iIntensiveCareUnitNurseNum+iHighCareUnitRoomNum*iHighCareUnitNurseNum+iGeneralWardRoomNum*iGeneralWardNurseNum);
//		lfTriageResult 			= piTriageCategoryPatientNum[0]+piTriageCategoryPatientNum[1]*2+piTriageCategoryPatientNum[2]*3+piTriageCategoryPatientNum[3]*4+piTriageCategoryPatientNum[4]*5;
		// EDWIN日本用に合わせる。（目視でおそらくこのくらいであろうという値が出ているので、問題ないと判断。）
		lfTriageResult 			= piCurrentTriageCategoryPatientNum[0]*5+piCurrentTriageCategoryPatientNum[1]*4+piCurrentTriageCategoryPatientNum[2]*3+piCurrentTriageCategoryPatientNum[3]*3+piCurrentTriageCategoryPatientNum[4]*2;
		lfEdPatients			= iCurrentEdAdmittedAgentNum;
		iNurseNum				= (int)(iConsultationRoomNum*iConsultationNurseNum+iEmergencyRoomNum*iEmergencyNurseNum);
		return 3.23*( lfWaitingRoomPatients/iTotalEdBeds )+0.097*( lfTriageResult/iNurseNum )+10.92*lfEdPatients/iTotalEdBeds;
	}

	/**
	 * <PRE>
	 *   現時刻での救急部門の混雑状況を表したED work Index指標を算出します。
	 * </PRE>
	 * @return	EDWIN値を返します。
	 * @since 2016/08/12
	 * @version 0.1
	 */
	private double lfCalcEdWin()
	{
		int i;
		int iTotalEdBeds = 0;
		int iHospitalBeds = 0;
		int iDoctorNurseNum = 0;
		int iNurseNum = 0;
		double lfWaitingRoomPatients = 0;
		double lfEdPatients = 0;
		double lfTriageResult = 0;

		lfWaitingRoomPatients	= iCurrentWaitingRoomPatientNum;
//		iTotalEdBeds			= (int)(iConsultationRoomNum+iEmergencyRoomNum+iIntensiveCareUnitRoomNum*iIntensiveCareUnitNurseNum+iHighCareUnitRoomNum*iHighCareUnitNurseNum);
		iTotalEdBeds			= (int)(iConsultationRoomNum+iEmergencyRoomNum);
		iHospitalBeds			= (int)(iIntensiveCareUnitRoomNum*iIntensiveCareUnitNurseNum+iHighCareUnitRoomNum*iHighCareUnitNurseNum+iGeneralWardRoomNum*iGeneralWardNurseNum);
		// EDWIN日本用に合わせる。（目視でおそらくこのくらいであろうという値が出ているので、問題ないと判断。）
//		lfTriageResult 			= piTriageCategoryPatientNum[0]+piTriageCategoryPatientNum[1]*2+piTriageCategoryPatientNum[2]*3+piTriageCategoryPatientNum[3]*4+piTriageCategoryPatientNum[4]*5;
		lfTriageResult 			= piCurrentTriageCategoryPatientNum[0]*5+piCurrentTriageCategoryPatientNum[1]*4+piCurrentTriageCategoryPatientNum[2]*3+piCurrentTriageCategoryPatientNum[3]*3+piCurrentTriageCategoryPatientNum[4]*2;
		lfEdPatients			= iCurrentEdAdmittedAgentNum;
		iDoctorNurseNum			= (int)(iConsultationRoomNum*iConsultationDoctorNum+iEmergencyRoomNum*iEmergencyDoctorNum);
		iNurseNum				= (int)(iConsultationRoomNum*iConsultationNurseNum+iEmergencyRoomNum*iEmergencyNurseNum);
//		System.out.println("lfWaitingRoomPatients = " + plfX[40]);
//		System.out.println("iTotalEdBeds = " + iTotalEdBeds);
//		System.out.println("iHospitalBeds = " + iHospitalBeds);
//		System.out.println("lfTriageResult = " + lfTriageResult);
//		System.out.println("lfEdPatient = " + plfX[36]);
//		System.out.println(lfTriageResult/(iDoctorNurseNum*(iTotalEdBeds-lfEdPatients)));
		return lfTriageResult/(iDoctorNurseNum*(iTotalEdBeds-lfEdPatients));
		// 論文から設定(NEDOCS及びEDWINの値の関連性を論じた研究から)
//		return 0.015*Math.abs(lfNedocs)+0.22;
	}

	/**
	 * <PRE>
	 *    現時点での評価指標の最大値を取得します。
	 *    ver 0.1
	 *    2016/08/17 ver 0.2 EDWIN, ED Work ScoreにもNEDOCSと同様の処理を追加。
	 * </PRE>
	 * @param lfTime		経過時間
	 * @author kobayashi
	 * @version 0.2
	 * @since 2016/08/12
	 */
	private void vMaxGetEvaluationIndex( double lfTime )
	{
		int i;
		double lfCount;

		lfCount = lfTotalTime / lfTime;
		if( lfMaxNedocs < lfNedocs )
		{
			lfMaxNedocs = lfNedocs;
			// 部屋数の設定をします。
			plfMaxInvSimParam[0] = iGetConsultationRoomNum();
			plfMaxInvSimParam[1] = iGetOperationRoomNum();
			plfMaxInvSimParam[2] = iGetEmergencyRoomNum();
			plfMaxInvSimParam[3] = iGetObservationRoomNum();
			plfMaxInvSimParam[4] = iGetInjurySevereObservationRoomNum();
			plfMaxInvSimParam[5] = iGetIntensiveCareUnitRoomNum();
			plfMaxInvSimParam[6] = iGetHighCareUnitRoomNum();
			plfMaxInvSimParam[7] = iGetGeneralWardRoomNum();
			plfMaxInvSimParam[8] = iGetWaitingRoomNum();
			plfMaxInvSimParam[9] = iGetExaminationXRayRoomNum();
			plfMaxInvSimParam[10] = iGetExaminationCTRoomNum();
			plfMaxInvSimParam[11] = iGetExaminationMRIRoomNum();
			plfMaxInvSimParam[12] = iGetExaminationAngiographyRoomNum();
			plfMaxInvSimParam[13] = iGetExaminationFastRoomNum();

			// 部屋を構成する人員の人数を設定します。
			plfMaxInvSimParam[14] = iGetConsultationRoomDoctorNum();
			plfMaxInvSimParam[15] = iGetConsultationRoomNurseNum();
			plfMaxInvSimParam[16] = iGetOperationRoomDoctorNum();
			plfMaxInvSimParam[17] = iGetOperationRoomNurseNum();
			plfMaxInvSimParam[18] = iGetEmergencyRoomDoctorNum();
			plfMaxInvSimParam[19] = iGetEmergencyRoomNurseNum();
			plfMaxInvSimParam[20] = iGetEmergencyRoomClinicalEngineerNum();
			plfMaxInvSimParam[21] = iGetObservationRoomNurseNum();
			plfMaxInvSimParam[22] = iGetInjurySevereObservationRoomNurseNum();
			plfMaxInvSimParam[23] = iGetIntensiveCareUnitRoomDoctorNum();
			plfMaxInvSimParam[24] = iGetIntensiveCareUnitRoomNurseNum();
			plfMaxInvSimParam[25] = iGetHighCareUnitRoomDoctorNum();
			plfMaxInvSimParam[26] = iGetHighCareUnitRoomNurseNum();
			plfMaxInvSimParam[27] = iGetGeneralWardRoomDoctorNum();
			plfMaxInvSimParam[28] = iGetGeneralWardRoomNurseNum();
			plfMaxInvSimParam[29] = iGetWaitingRoomNurseNum();
			plfMaxInvSimParam[30] = iGetExaminationXRayRoomClinicalEngineerNum();
			plfMaxInvSimParam[31] = iGetExaminationCTRoomClinicalEngineerNum();
			plfMaxInvSimParam[32] = iGetExaminationMRIRoomClinicalEngineerNum();
			plfMaxInvSimParam[33] = iGetExaminationAnmgiographyRoomClinicalEngineerNum();
			plfMaxInvSimParam[34] = iGetExaminationFastRoomClinicalEngineerNum();
//			plfMaxInvSimParam[35] = lfGetAvgSurvivalProbability();
//			plfMaxInvSimParam[36] = iGetSurvivalNum()/(double)erDepart.iGetTotalPatientNum();
//			plfMaxInvSimParam[37] = (double)(iGetTotalPatientNum()-iGetSurvivalNum())/(double)iGetTotalPatientNum();
//			plfMaxInvSimParam[35] = iGetTotalPatientAgentNum()/lfCount;
//			plfMaxInvSimParam[36] = iGetEdAdmittedAgentNum()/lfCount;
//			plfMaxInvSimParam[37] = iGetEdVentilatorsNum()/lfCount;
			plfMaxInvSimParam[35] = iGetCurrentTotalPatientAgentNum();
			plfMaxInvSimParam[36] = iGetCurrentEdAdmittedAgentNum();
			plfMaxInvSimParam[37] = iGetCurrentEdVentilatorsNum();
			plfMaxInvSimParam[38] = lfGetLongestAdmittedTime()/3600.0;
			plfMaxInvSimParam[39] = lfGetLastBedTime()/3600.0;
//			plfMaxInvSimParam[40] = iGetWaitingRoomPatientNum()/lfCount;
//			plfMaxInvSimParam[41] = iGetTriageCategoryPatientNum(1)/lfCount;
//			plfMaxInvSimParam[42] = iGetTriageCategoryPatientNum(2)/lfCount;
//			plfMaxInvSimParam[43] = iGetTriageCategoryPatientNum(3)/lfCount;
//			plfMaxInvSimParam[44] = iGetTriageCategoryPatientNum(4)/lfCount;
//			plfMaxInvSimParam[45] = iGetTriageCategoryPatientNum(5)/lfCount;
			plfMaxInvSimParam[40] = iGetCurrentWaitingRoomPatientNum();
			plfMaxInvSimParam[41] = iGetCurrentTriageCategoryPatientNum(1);
			plfMaxInvSimParam[42] = iGetCurrentTriageCategoryPatientNum(2);
			plfMaxInvSimParam[43] = iGetCurrentTriageCategoryPatientNum(3);
			plfMaxInvSimParam[44] = iGetCurrentTriageCategoryPatientNum(4);
			plfMaxInvSimParam[45] = iGetCurrentTriageCategoryPatientNum(5);
		}
		if( lfMaxEdwin < lfEdwin )
		{
			lfMaxEdwin = lfEdwin;
			// 部屋数の設定をします。
			plfMaxInvSimParam[0] = iGetConsultationRoomNum();
			plfMaxInvSimParam[1] = iGetOperationRoomNum();
			plfMaxInvSimParam[2] = iGetEmergencyRoomNum();
			plfMaxInvSimParam[3] = iGetObservationRoomNum();
			plfMaxInvSimParam[4] = iGetInjurySevereObservationRoomNum();
			plfMaxInvSimParam[5] = iGetIntensiveCareUnitRoomNum();
			plfMaxInvSimParam[6] = iGetHighCareUnitRoomNum();
			plfMaxInvSimParam[7] = iGetGeneralWardRoomNum();
			plfMaxInvSimParam[8] = iGetWaitingRoomNum();
			plfMaxInvSimParam[9] = iGetExaminationXRayRoomNum();
			plfMaxInvSimParam[10] = iGetExaminationCTRoomNum();
			plfMaxInvSimParam[11] = iGetExaminationMRIRoomNum();
			plfMaxInvSimParam[12] = iGetExaminationAngiographyRoomNum();
			plfMaxInvSimParam[13] = iGetExaminationFastRoomNum();

			// 部屋を構成する人員の人数を設定します。
			plfMaxInvSimParam[14] = iGetConsultationRoomDoctorNum();
			plfMaxInvSimParam[15] = iGetConsultationRoomNurseNum();
			plfMaxInvSimParam[16] = iGetOperationRoomDoctorNum();
			plfMaxInvSimParam[17] = iGetOperationRoomNurseNum();
			plfMaxInvSimParam[18] = iGetEmergencyRoomDoctorNum();
			plfMaxInvSimParam[19] = iGetEmergencyRoomNurseNum();
			plfMaxInvSimParam[20] = iGetEmergencyRoomClinicalEngineerNum();
			plfMaxInvSimParam[21] = iGetObservationRoomNurseNum();
			plfMaxInvSimParam[22] = iGetInjurySevereObservationRoomNurseNum();
			plfMaxInvSimParam[23] = iGetIntensiveCareUnitRoomDoctorNum();
			plfMaxInvSimParam[24] = iGetIntensiveCareUnitRoomNurseNum();
			plfMaxInvSimParam[25] = iGetHighCareUnitRoomDoctorNum();
			plfMaxInvSimParam[26] = iGetHighCareUnitRoomNurseNum();
			plfMaxInvSimParam[27] = iGetGeneralWardRoomDoctorNum();
			plfMaxInvSimParam[28] = iGetGeneralWardRoomNurseNum();
			plfMaxInvSimParam[29] = iGetWaitingRoomNurseNum();
			plfMaxInvSimParam[30] = iGetExaminationXRayRoomClinicalEngineerNum();
			plfMaxInvSimParam[31] = iGetExaminationCTRoomClinicalEngineerNum();
			plfMaxInvSimParam[32] = iGetExaminationMRIRoomClinicalEngineerNum();
			plfMaxInvSimParam[33] = iGetExaminationAnmgiographyRoomClinicalEngineerNum();
			plfMaxInvSimParam[34] = iGetExaminationFastRoomClinicalEngineerNum();
//			plfMaxInvSimParam[35] = lfGetAvgSurvivalProbability();
//			plfMaxInvSimParam[36] = iGetSurvivalNum()/(double)erDepart.iGetTotalPatientNum();
//			plfMaxInvSimParam[37] = (double)(iGetTotalPatientNum()-iGetSurvivalNum())/(double)iGetTotalPatientNum();
//			plfMaxInvSimParam[35] = iGetTotalPatientAgentNum()/lfCount;
//			plfMaxInvSimParam[36] = iGetEdAdmittedAgentNum()/lfCount;
//			plfMaxInvSimParam[37] = iGetEdVentilatorsNum()/lfCount;
			plfMaxInvSimParam[35] = iGetCurrentTotalPatientAgentNum();
			plfMaxInvSimParam[36] = iGetCurrentEdAdmittedAgentNum();
			plfMaxInvSimParam[37] = iGetCurrentEdVentilatorsNum();
			plfMaxInvSimParam[38] = lfGetLongestAdmittedTime()/3600.0;
			plfMaxInvSimParam[39] = lfGetLastBedTime()/3600.0;
//			plfMaxInvSimParam[40] = iGetWaitingRoomPatientNum()/lfCount;
//			plfMaxInvSimParam[41] = iGetTriageCategoryPatientNum(1)/lfCount;
//			plfMaxInvSimParam[42] = iGetTriageCategoryPatientNum(2)/lfCount;
//			plfMaxInvSimParam[43] = iGetTriageCategoryPatientNum(3)/lfCount;
//			plfMaxInvSimParam[44] = iGetTriageCategoryPatientNum(4)/lfCount;
//			plfMaxInvSimParam[45] = iGetTriageCategoryPatientNum(5)/lfCount;
			plfMaxInvSimParam[40] = iGetCurrentWaitingRoomPatientNum();
			plfMaxInvSimParam[41] = iGetCurrentTriageCategoryPatientNum(1);
			plfMaxInvSimParam[42] = iGetCurrentTriageCategoryPatientNum(2);
			plfMaxInvSimParam[43] = iGetCurrentTriageCategoryPatientNum(3);
			plfMaxInvSimParam[44] = iGetCurrentTriageCategoryPatientNum(4);
			plfMaxInvSimParam[45] = iGetCurrentTriageCategoryPatientNum(5);
		}
		if( lfMaxEdWorkScore < lfEdWorkScore )
		{
			lfMaxEdWorkScore = lfEdWorkScore;
			// 部屋数の設定をします。
			plfMaxInvSimParam[0] = iGetConsultationRoomNum();
			plfMaxInvSimParam[1] = iGetOperationRoomNum();
			plfMaxInvSimParam[2] = iGetEmergencyRoomNum();
			plfMaxInvSimParam[3] = iGetObservationRoomNum();
			plfMaxInvSimParam[4] = iGetInjurySevereObservationRoomNum();
			plfMaxInvSimParam[5] = iGetIntensiveCareUnitRoomNum();
			plfMaxInvSimParam[6] = iGetHighCareUnitRoomNum();
			plfMaxInvSimParam[7] = iGetGeneralWardRoomNum();
			plfMaxInvSimParam[8] = iGetWaitingRoomNum();
			plfMaxInvSimParam[9] = iGetExaminationXRayRoomNum();
			plfMaxInvSimParam[10] = iGetExaminationCTRoomNum();
			plfMaxInvSimParam[11] = iGetExaminationMRIRoomNum();
			plfMaxInvSimParam[12] = iGetExaminationAngiographyRoomNum();
			plfMaxInvSimParam[13] = iGetExaminationFastRoomNum();

			// 部屋を構成する人員の人数を設定します。
			plfMaxInvSimParam[14] = iGetConsultationRoomDoctorNum();
			plfMaxInvSimParam[15] = iGetConsultationRoomNurseNum();
			plfMaxInvSimParam[16] = iGetOperationRoomDoctorNum();
			plfMaxInvSimParam[17] = iGetOperationRoomNurseNum();
			plfMaxInvSimParam[18] = iGetEmergencyRoomDoctorNum();
			plfMaxInvSimParam[19] = iGetEmergencyRoomNurseNum();
			plfMaxInvSimParam[20] = iGetEmergencyRoomClinicalEngineerNum();
			plfMaxInvSimParam[21] = iGetObservationRoomNurseNum();
			plfMaxInvSimParam[22] = iGetInjurySevereObservationRoomNurseNum();
			plfMaxInvSimParam[23] = iGetIntensiveCareUnitRoomDoctorNum();
			plfMaxInvSimParam[24] = iGetIntensiveCareUnitRoomNurseNum();
			plfMaxInvSimParam[25] = iGetHighCareUnitRoomDoctorNum();
			plfMaxInvSimParam[26] = iGetHighCareUnitRoomNurseNum();
			plfMaxInvSimParam[27] = iGetGeneralWardRoomDoctorNum();
			plfMaxInvSimParam[28] = iGetGeneralWardRoomNurseNum();
			plfMaxInvSimParam[29] = iGetWaitingRoomNurseNum();
			plfMaxInvSimParam[30] = iGetExaminationXRayRoomClinicalEngineerNum();
			plfMaxInvSimParam[31] = iGetExaminationCTRoomClinicalEngineerNum();
			plfMaxInvSimParam[32] = iGetExaminationMRIRoomClinicalEngineerNum();
			plfMaxInvSimParam[33] = iGetExaminationAnmgiographyRoomClinicalEngineerNum();
			plfMaxInvSimParam[34] = iGetExaminationFastRoomClinicalEngineerNum();
//			plfMaxInvSimParam[35] = lfGetAvgSurvivalProbability();
//			plfMaxInvSimParam[36] = iGetSurvivalNum()/(double)erDepart.iGetTotalPatientNum();
//			plfMaxInvSimParam[37] = (double)(iGetTotalPatientNum()-iGetSurvivalNum())/(double)iGetTotalPatientNum();
//			plfMaxInvSimParam[35] = iGetTotalPatientAgentNum()/lfCount;
//			plfMaxInvSimParam[36] = iGetEdAdmittedAgentNum()/lfCount;
//			plfMaxInvSimParam[37] = iGetEdVentilatorsNum()/lfCount;
			plfMaxInvSimParam[35] = iGetCurrentTotalPatientAgentNum();
			plfMaxInvSimParam[36] = iGetCurrentEdAdmittedAgentNum();
			plfMaxInvSimParam[37] = iGetCurrentEdVentilatorsNum();
			plfMaxInvSimParam[38] = lfGetLongestAdmittedTime()/3600.0;
			plfMaxInvSimParam[39] = lfGetLastBedTime()/3600.0;
//			plfMaxInvSimParam[40] = iGetWaitingRoomPatientNum()/lfCount;
//			plfMaxInvSimParam[41] = iGetTriageCategoryPatientNum(1)/lfCount;
//			plfMaxInvSimParam[42] = iGetTriageCategoryPatientNum(2)/lfCount;
//			plfMaxInvSimParam[43] = iGetTriageCategoryPatientNum(3)/lfCount;
//			plfMaxInvSimParam[44] = iGetTriageCategoryPatientNum(4)/lfCount;
//			plfMaxInvSimParam[45] = iGetTriageCategoryPatientNum(5)/lfCount;
			plfMaxInvSimParam[40] = iGetCurrentWaitingRoomPatientNum();
			plfMaxInvSimParam[41] = iGetCurrentTriageCategoryPatientNum(1);
			plfMaxInvSimParam[42] = iGetCurrentTriageCategoryPatientNum(2);
			plfMaxInvSimParam[43] = iGetCurrentTriageCategoryPatientNum(3);
			plfMaxInvSimParam[44] = iGetCurrentTriageCategoryPatientNum(4);
			plfMaxInvSimParam[45] = iGetCurrentTriageCategoryPatientNum(5);
		}
	}

	/**
	 * <PRE>
	 *    現時点でのトリアージ別受診者数を計算し、出力します。
	 * </PRE>
	 * @throws ERNurseAgentException				看護師エージェント例外
	 * @throws ERDoctorAgentException				医師エージェント例外
	 * @throws ERClinicalEngineerAgentException		医療技師エージェント例外
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vOutputEdAdmittedPatientTriageCategoryNum() throws ERNurseAgentException, ERDoctorAgentException, ERClinicalEngineerAgentException
	{
		int i,j;
		int piTriageCategoryNum[] = new int[5];

		for( j = 0;j < 5; j++ )
		{
			piTriageCategoryNum[j] += erWaitingRoom.iGetTriageCategoryPatientNum(j);
		}

		// 観察室プロセスを実行します。
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piTriageCategoryNum[j] += ArrayListObservationRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 重症観察室プロセスを実行します。
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piTriageCategoryNum[j] += ArrayListSevereInjuryObservationRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 初療室プロセスを実行します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piTriageCategoryNum[j] += ArrayListEmergencyRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// X線室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piTriageCategoryNum[j] += ArrayListExaminationXRayRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// CT室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piTriageCategoryNum[j] += ArrayListExaminationCTRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// MRI室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piTriageCategoryNum[j] += ArrayListExaminationMRIRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 血管造影室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piTriageCategoryNum[j] += ArrayListExaminationAngiographyRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 診察室プロセスを実行します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piTriageCategoryNum[j] += ArrayListConsultationRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 手術室プロセスを実行します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piTriageCategoryNum[j] += ArrayListOperationRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 集中治療室プロセスを実行します。
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piTriageCategoryNum[j] += ArrayListIntensiveCareUnitRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 高度治療室プロセスを実行します。
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piTriageCategoryNum[j] += ArrayListHighCareUnitRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}

		// 一般病棟プロセスを実行します。
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			for( j = 0;j < 5; j++ )
			{
				piTriageCategoryNum[j] += ArrayListGeneralWardRooms.get(i).iGetTriageCategoryPatientNum(j);
			}
		}
		System.out.println("現時刻のトリアージ別受診数," + piTriageCategoryNum[0] + "," +  piTriageCategoryNum[1] + "," + piTriageCategoryNum[2] + "," + piTriageCategoryNum[3] + "," + piTriageCategoryNum[4] + "," );
	}

	/**
	 * <PRE>
	 *    診察室、手術室、初療室、ICU、HCU、観察室、重症観察室、一般病棟、待合室、X線室、CT室、MRI室、血管造影室、Fast室に在籍している患者の数を計算します。
	 *    ED Work Scoreで使用します。
	 * </PRE>
	 * @throws ERNurseAgentException				看護師エージェント例外
	 * @throws ERDoctorAgentException				医師エージェント例外
	 * @throws ERClinicalEngineerAgentException		医療技師エージェント例外
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vOutputEdTotalPatientNum() throws ERNurseAgentException, ERDoctorAgentException, ERClinicalEngineerAgentException
	{
		int i;
		int iTotalPatientNum = 0;

		// 初療室プロセスを実行します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			iTotalPatientNum += ArrayListEmergencyRooms.get(i).iGetPatientAgentsNum();
		}

		// 診察室プロセスを実行します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			iTotalPatientNum += ArrayListConsultationRooms.get(i).iGetPatientAgentsNum();
		}

		// 手術室プロセスを実行します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			iTotalPatientNum += ArrayListOperationRooms.get(i).iGetPatientAgentsNum();
		}

		// 観察室プロセスを実行します。
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			iTotalPatientNum += ArrayListObservationRooms.get(i).iGetPatientAgentsNum();
		}

		// 重症観察室プロセスを実行します。
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			iTotalPatientNum += ArrayListSevereInjuryObservationRooms.get(i).iGetPatientAgentsNum();
		}

		// 集中治療室プロセスを実行します。
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			iTotalPatientNum += ArrayListIntensiveCareUnitRooms.get(i).iGetPatientAgentsNum();
		}

		// 高度治療室プロセスを実行します。
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			iTotalPatientNum += ArrayListHighCareUnitRooms.get(i).iGetPatientAgentsNum();
		}

		// 一般病棟プロセスを実行します。
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			iTotalPatientNum += ArrayListGeneralWardRooms.get(i).iGetPatientAgentsNum();
		}

		// X線室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			iTotalPatientNum += ArrayListExaminationXRayRooms.get(i).iGetPatientAgentsNum();
		}

		// CT室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			iTotalPatientNum += ArrayListExaminationCTRooms.get(i).iGetPatientAgentsNum();
		}

		// MRI室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			iTotalPatientNum += ArrayListExaminationMRIRooms.get(i).iGetPatientAgentsNum();
		}

		// 血管造影室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			iTotalPatientNum += ArrayListExaminationAngiographyRooms.get(i).iGetPatientAgentsNum();
		}

		// Fast室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			iTotalPatientNum += ArrayListExaminationFastRooms.get(i).iGetPatientAgentsNum();
		}

		// 待合室プロセスを実行します。
		iTotalPatientNum += erWaitingRoom.iGetTotalPatientNum();
		System.out.println("現時刻での総患者数," + iTotalPatientNum );
	}

	/**
	 * <PRE>
	 *    診察室、初療室に在籍している患者の数を出力します。
	 *    NEDOCSにおいて使用します。
	 * </PRE>
	 * @throws ERNurseAgentException				看護師エージェント例外
	 * @throws ERDoctorAgentException				医師エージェント例外
	 * @throws ERClinicalEngineerAgentException		医療技師エージェント例外
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vOutputEdAdmittedPatientNum() throws ERNurseAgentException, ERDoctorAgentException, ERClinicalEngineerAgentException
	{
		int i;
		int iEdAdmittedNum = 0;

		// 初療室プロセスを実行します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			iEdAdmittedNum += ArrayListEmergencyRooms.get(i).iGetPatientAgentsNum();
		}

		// 診察室プロセスを実行します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			iEdAdmittedNum += ArrayListConsultationRooms.get(i).iGetPatientAgentsNum();
		}

		// 手術室プロセスを実行します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			iEdAdmittedNum += ArrayListOperationRooms.get(i).iGetPatientAgentsNum();
		}
		System.out.println("現時刻での診察室、初療室の患者数," + iEdAdmittedNum );
	}

	/**
	 * <PRE>
	 *    診察室、初療室に在籍している患者の数を出力します。
	 *    NEDOCSにおいて使用します。
	 * </PRE>
	 * @throws ERNurseAgentException				看護師エージェント例外
	 * @throws ERDoctorAgentException				医師エージェント例外
	 * @throws ERClinicalEngineerAgentException		医療技師エージェント例外
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vOutputEdVentilatorsNum() throws ERNurseAgentException, ERDoctorAgentException, ERClinicalEngineerAgentException
	{
		int i;
		int iEdVentilatorNum = 0;

		// 初療室プロセスを実行します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			iEdVentilatorNum += ArrayListEmergencyRooms.get(i).iGetPatientAgentsNum();
		}
		// 手術室プロセスを実行します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			iEdVentilatorNum += ArrayListOperationRooms.get(i).iGetPatientAgentsNum();
		}
		// 集中治療室プロセスを実行します。
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			iEdVentilatorNum += ArrayListIntensiveCareUnitRooms.get(i).iGetPatientAgentsNum();
		}
		System.out.println("現時刻での診察室、初療室、ICUの患者数," + iEdVentilatorNum );
	}

	/**
	 * <PRE>
	 *    診察室、初療室、ICU、HCUに在籍している患者の数を出力します。
	 *    ED Work Scoreで使用します。
	 * </PRE>
	 * @throws ERNurseAgentException				看護師エージェント例外
	 * @throws ERDoctorAgentException				医師エージェント例外
	 * @throws ERClinicalEngineerAgentException		医療技師エージェント例外
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vOutputEdTotalAdmittedPatientNum() throws ERNurseAgentException, ERDoctorAgentException, ERClinicalEngineerAgentException
	{
		int i;
		int iTotalEdAdmittedNum = 0;

		// 初療室プロセスを実行します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			iTotalEdAdmittedNum += ArrayListEmergencyRooms.get(i).iGetPatientAgentsNum();
		}

		// 診察室プロセスを実行します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			iTotalEdAdmittedNum += ArrayListConsultationRooms.get(i).iGetPatientAgentsNum();
		}

		// 手術室プロセスを実行します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			iTotalEdAdmittedNum += ArrayListOperationRooms.get(i).iGetPatientAgentsNum();
		}

		// 集中治療室プロセスを実行します。
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			iTotalEdAdmittedNum += ArrayListIntensiveCareUnitRooms.get(i).iGetPatientAgentsNum();
		}

		// 高度治療室プロセスを実行します。
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			iTotalEdAdmittedNum += ArrayListHighCareUnitRooms.get(i).iGetPatientAgentsNum();
		}
		System.out.println("現時刻での診察室、初療室、ICU、HCUの患者数," + iTotalEdAdmittedNum );
	}

	/**
	 * <PRE>
	 *    待合室の患者の人数を出力します。
	 *    ED Work Scoreで使用します。
	 * </PRE>
	 * @throws ERNurseAgentException				看護師エージェント例外
	 * @throws ERDoctorAgentException				医師エージェント例外
	 * @throws ERClinicalEngineerAgentException		医療技師エージェント例外
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vOutputWaitingRoomPatientNum() throws ERNurseAgentException, ERDoctorAgentException, ERClinicalEngineerAgentException
	{
		System.out.println("現時刻での待合室の患者数," + erWaitingRoom.iGetTotalPatientNum() );
	}

	/**
	 * <PRE>
	 *    現在、待合室に最も長く病院にいる患者の在院時間を出力します。
	 *    （時間単位ですが、秒単位で出力しています。）
	 * </PRE>
	 */
	public void vOutputLongestAdmittedTime()
	{
		int i;
		double lfLongestTime = 0.0;

		// 初療室プロセスを実行します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			lfLongestTime = ArrayListEmergencyRooms.get(i).lfGetLongestStayPatient();
		}

		// 診察室プロセスを実行します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			lfLongestTime = ArrayListConsultationRooms.get(i).lfGetLongestStayPatient();
		}

		// 手術室プロセスを実行します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			lfLongestTime = ArrayListOperationRooms.get(i).lfGetLongestStayPatient();
		}

		// 観察室プロセスを実行します。
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			lfLongestTime = ArrayListObservationRooms.get(i).lfGetLongestStayPatient();
		}

		// 重症観察室プロセスを実行します。
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			lfLongestTime = ArrayListSevereInjuryObservationRooms.get(i).lfGetLongestStayPatient();
		}

		// 集中治療室プロセスを実行します。
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			lfLongestTime = ArrayListIntensiveCareUnitRooms.get(i).lfGetLongestStayPatient();
		}

		// 高度治療室プロセスを実行します。
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			lfLongestTime = ArrayListHighCareUnitRooms.get(i).lfGetLongestStayPatient();
		}

		// 一般病棟プロセスを実行します。
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			lfLongestTime = ArrayListGeneralWardRooms.get(i).lfGetLongestStayPatient();
		}

		// X線室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			lfLongestTime = ArrayListExaminationXRayRooms.get(i).lfGetLongestStayPatient();
		}

		// CT室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			lfLongestTime = ArrayListExaminationCTRooms.get(i).lfGetLongestStayPatient();
		}

		// MRI室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			lfLongestTime = ArrayListExaminationMRIRooms.get(i).lfGetLongestStayPatient();
		}

		// 血管造影室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			lfLongestTime = ArrayListExaminationAngiographyRooms.get(i).lfGetLongestStayPatient();
		}

		// Fast室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			lfLongestTime = ArrayListExaminationFastRooms.get(i).lfGetLongestStayPatient();
		}

		// 待合室プロセスを実行します。
		lfLongestTime = erWaitingRoom.lfGetLongestStayPatient();

		System.out.println("現時刻でのもっとも長く在院している患者の時間," + lfLongestTime );
	}

	/**
	 * <PRE>
	 *    現在、最後に病床に入った患者の到着から入院までの時間を出力します。
	 *    （時間単位ですが、秒単位で出力しています。）
	 * </PRE>
	 */
	public void vOutputLastBedTime()
	{
		int i;
		double lfLastTime = 0.0;

		// 初療室プロセスを実行します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			lfLastTime = ArrayListEmergencyRooms.get(i).lfGetLastBedTime();
		}

		// 診察室プロセスを実行します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			lfLastTime = ArrayListConsultationRooms.get(i).lfGetLastBedTime();
		}

		// 手術室プロセスを実行します。
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			lfLastTime = ArrayListOperationRooms.get(i).lfGetLastBedTime();
		}

		// 観察室プロセスを実行します。
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			lfLastTime = ArrayListObservationRooms.get(i).lfGetLastBedTime();
		}

		// 重症観察室プロセスを実行します。
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			lfLastTime = ArrayListSevereInjuryObservationRooms.get(i).lfGetLastBedTime();
		}

		// 集中治療室プロセスを実行します。
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			lfLastTime = ArrayListIntensiveCareUnitRooms.get(i).lfGetLastBedTime();
		}

		// 高度治療室プロセスを実行します。
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			lfLastTime = ArrayListHighCareUnitRooms.get(i).lfGetLastBedTime();
		}

		// 一般病棟プロセスを実行します。
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			lfLastTime = ArrayListGeneralWardRooms.get(i).lfGetLastBedTime();
		}

		// X線室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			lfLastTime = ArrayListExaminationXRayRooms.get(i).lfGetLastBedTime();
		}

		// CT室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			lfLastTime = ArrayListExaminationCTRooms.get(i).lfGetLastBedTime();
		}

		// MRI室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			lfLastTime = ArrayListExaminationMRIRooms.get(i).lfGetLastBedTime();
		}

		// 血管造影室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			lfLastTime = ArrayListExaminationAngiographyRooms.get(i).lfGetLastBedTime();
		}

		// Fast室プロセスを実行します。
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			lfLastTime = ArrayListExaminationFastRooms.get(i).lfGetLastBedTime();
		}

		// 待合室プロセスを実行します。
		lfLastTime = erWaitingRoom.lfGetLastBedTime();
		if( lfLastTime < 0.0 )
		{
			lfLastTime = 0.0;
		}
		System.out.println("現時刻での一番最後に入院した患者の到着から入院までの時間," + lfLastBedTime );
	}

	/**
	 * <PRE>
	 *    ファイルの読み込みを行います。
	 * </PRE>
	 * @param strERDirectory		ERファイル格納ディレクトリ
	 * @param iFileWriteModeFlag	ファイル書き込みモード
	 * 								0 全部書き込む
	 * 								1 最初と最後のみを書き込む
	 * @throws IOException			ファイル読み込みエラー
	 * @author kobayashi
	 * @version 0.1
	 * @since 2016/08/12
	 */
	public void vSetReadWriteFile( String strERDirectory, int iFileWriteModeFlag ) throws IOException
	{
		String strFileName = "";
//		iFileWriteModeFlag = iFileWriteModeFlag;
		if( iFileWriteModeFlag == 1 )
		{
			csvWriteAgentData					= new CCsv();
			strFileName							= strERDirectory;
			strFileName							+= "/er_end" + this.getId() + ".csv";
			csvWriteAgentData.vOpen( strFileName, "write");
			csvWriteAgentStartData				= new CCsv();
			strFileName							= strERDirectory;
			strFileName							+= "/er_start" + this.getId() + ".csv";
			csvWriteAgentStartData.vOpen( strFileName, "write");
		}
		else
		{
			csvWriteAgentData					= new CCsv();
			strFileName							= strERDirectory;
			strFileName							+= "/er_end" + this.getId() + ".csv";
			csvWriteAgentData.vOpen( strFileName, "write");
		}
	}

	/**
	 * <PRE>
	 *   ファイルの書き込みを行います。
	 * </PRE>
	 * @param iFlag			ファイル書き込みフラグ
	 *                      0 シミュレーションステップ毎に書き込み
	 *                      1 最初と最後の100秒分のデータを書き込み
	 * @param lfTime		タイムステップ
	 * @throws IOException	ファイル書き込みエラー
	 * @author kobayashi
	 * @version 0.1
	 * @since 2016/08/12
	 */
	public void vWriteFile( int iFlag, double lfTime ) throws IOException
	{
		double lfNum = 0.0;

		lfNum = lfTotalTime / lfTime;
		String strData = lfTotalTime + "," + lfNedocs/lfNum + "," + lfEdwin/lfNum + "," + lfEdWorkScore/lfNum + ",";
		// 終了時の書き込みか、特に指定していない場合
		if( iFlag == 0 )
		{
			csvWriteAgentData.vWrite( strData );
		}
		// 開始時の書き込み
		else
		{
			// 開始時の書き込み
			if( lfTime <= 100.0 )
			{
				csvWriteAgentStartData.vWrite( strData );
			}
			// 終了時の書き込み
			if( lfTime >= lfEndTime-100.0 )
			{
				csvWriteAgentData.vWrite( strData );
			}
		}
	}

	/**
	 * <PRE>
	 *   評価指標用のパラメータの最大値を格納した内容を取得します。
	 * </PRE>
	 * @param i	評価指標の取り出したい番号
	 * @return	該当する評価指標値
	 * @author kobayashi
	 * @since 2016/08/12
	 */
	public double iGetMaxInvSimParam(int i)
	{
		// TODO 自動生成されたメソッド・スタブ
		return plfMaxInvSimParam[i];
	}

	/**
	 * <PRE>
	 *   評価指標用のパラメータの最大値を格納した配列に設定します。
	 * </PRE>
	 * @param iLoc				配列番号
	 * @param lfInvSimParam		パラメータ設定
	 * @author kobayashi
	 * @version 0.1
	 * @since 2016/08/17
	 */
	public void vSetMaxInvSimParam( int iLoc, double lfInvSimParam )
	{
		// TODO 自動生成されたメソッド・スタブ
		if( plfMaxInvSimParam != null )
			plfMaxInvSimParam[iLoc] = lfInvSimParam;
	}

	/**
	 * <PRE>
	 *   Nedocsの最大値を設定します。
	 * </PRE>
	 * @param lfData		NEDOCSパラメータ値
	 * @author kobayashi
	 * @version 0.1
	 * @since 2016/08/17
	 */
	public void vSetMaxNedocs( double lfData )
	{
		lfMaxNedocs = lfData;
	}

	/**
	 * <PRE>
	 *   ED Work Scoreの最大値を設定します。
	 * </PRE>
	 * @param lfData		ED Work Score値
	 * @author kobayashi
	 * @version 0.1
	 * @since 2016/08/17
	 */
	public void vSetMaxEdWorkScore( double lfData )
	{
		lfMaxEdWorkScore = lfData;
	}

	/**
	 * <PRE>
	 *   EDWINの最大値を設定します。
	 * </PRE>
	 * @param lfData		EDWIN値
	 * @author kobayashi
	 * @version 0.1
	 * @since 2016/08/17
	 */
	public void vSetMaxEdwin( double lfData )
	{
		lfMaxEdwin = lfData;
	}

	/**
	 * <PRE>
	 *   Nedocsの最大値を取得します。
	 * </PRE>
	 * @return NEDOCSの最大値
	 * @author kobayashi
	 * @version 0.1
	 * @since 2017/07/19
	 */
	public double lfGetMaxNedocs()
	{
		return lfMaxNedocs;
	}

	/**
	 * <PRE>
	 *   ED Work Scoreの最大値を取得します。
	 * </PRE>
	 * @return ED Work Scoreの最大値
	 * @author kobayashi
	 * @version 0.1
	 * @since 2017/07/19
	 */
	public double lfGetMaxEdWorkScore()
	{
		return lfMaxEdWorkScore;
	}

	/**
	 * <PRE>
	 *   EDWINの最大値を取得します。
	 * </PRE>
	 * @return EDWINの最大値
	 * @author kobayashi
	 * @version 0.1
	 * @since 2017/07/19
	 */
	public double lfGetMaxEdwin()
	{
		return lfMaxEdwin;
	}

	/**
	 * <PRE>
	 *    救急部門のシミュレーションが終了したかを返却します。
	 * </PRE>
	 * @return		シミュレーション終了フラグ
	 * @author kobayashi
	 * @version 0.1
	 * @since 2016/10/05
	 */
	public boolean isFinishAgentFlag()
	{
		return iFinishAgentFlag;
	}

	/**
	 * <PRE>
	 *    患者を生成する方式を変更します。
	 *    0：初期化時にシーケンシャルに生成します。
	 *    1：別スレッドからリアルタイムに生成します。
	 * </PRE>
	 * @param iGenPatientMode 患者生成モード
	 */
	public void vSetGenerationPatientMode(int iGenPatientMode )
	{
		// TODO 自動生成されたメソッド・スタブ
		iGenerationPatientMode = iGenPatientMode;
	}

	/**
	 * <PRE>
	 *    TRISim用にカスタマイズしたFuseNodeLink用を返す関数
	 * </PRE>
	 * @returnm ノードマネージャを返す
	 */
	public ERTriageNodeManager getERTriageNodeManager()
	{
		return cErNodeManager;
	}

	/**
	 * <PRE>
	 *    初期設定ファイル用クラスのインスタンスを取得します。
	 * </PRE>
	 * @returnm 初期設定ファイルクラスインスタンス
	 */
	public InitSimParam cGetInitParam()
	{
		return initSimParam;
	}


	@Override
	public void action(long timeStep)
	{
		int i;
		int iStatus = 0;
		double lfSecond = 0.0;
		// TODO 自動生成されたメソッド・スタブ

/* 待合室プロセスを実行します。 */

		try
		{
			lfSecond = timeStep / 1000.0;

			// 生成した患者が出てきた場合、患者を待合室へ移動します。
			erOutside.iImplementOutside(erWaitingRoom);

			// 患者を到達分布にしたがって生成します。(午前8時30分を0秒とする。)
//			erWaitingRoom.vArrivalPatient( lfSecond, erEngine );

			// 待合室プロセスを実行します。
//			System.out.println("待合室プロセス");
			iStatus = erWaitingRoom.iImplementWaitingRoom( ArrayListConsultationRooms, ArrayListObservationRooms, ArrayListSevereInjuryObservationRooms, ArrayListEmergencyRooms, ArrayListExaminationXRayRooms, ArrayListExaminationCTRooms, ArrayListExaminationMRIRooms, ArrayListExaminationAngiographyRooms, ArrayListExaminationFastRooms, erOutside );
			// なくなられた患者の数を取得します。
			iDeathNum = erWaitingRoom.iGetDeathNum();

			// 観察室プロセスを実行します。
//			System.out.println("観察室プロセス");
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				ArrayListObservationRooms.get(i).vImplementObservationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, ArrayListSevereInjuryObservationRooms, ArrayListEmergencyRooms, erWaitingRoom );
			}

			// 重症観察室プロセスを実行します。
//			System.out.println("重症観察室プロセス");
			for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
			{
				ArrayListSevereInjuryObservationRooms.get(i).vImplementSevereInjuryObservationRoom( ArrayListSevereInjuryObservationRooms, ArrayListEmergencyRooms, ArrayListObservationRooms, erWaitingRoom );
			}

			// 初療室プロセスを実行します。
//			System.out.println("初療室プロセス");
			for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
			{
				ArrayListEmergencyRooms.get(i).vImplementEmergencyRoom( ArrayListOperationRooms, ArrayListIntensiveCareUnitRooms, ArrayListHighCareUnitRooms, ArrayListGeneralWardRooms );
			}

			// X線室プロセスを実行します。
//			System.out.println("X線室プロセス");
			for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
			{
				ArrayListExaminationXRayRooms.get(i).vImplementExaminationRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListConsultationRooms, ArrayListOperationRooms, erWaitingRoom );
			}

			// CT室プロセスを実行します。
//			System.out.println("ＣＴ室プロセス");
			for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
			{
				ArrayListExaminationCTRooms.get(i).vImplementExaminationRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListConsultationRooms, ArrayListOperationRooms, erWaitingRoom );
			}

			// MRI室プロセスを実行します。
//			System.out.println("ＭＲＩ室プロセス");
			for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
			{
				ArrayListExaminationMRIRooms.get(i).vImplementExaminationRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListConsultationRooms, ArrayListOperationRooms, erWaitingRoom );
			}

			// 血管造影室プロセスを実行します。
//			System.out.println("血管造影室プロセス");
			for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
			{
				ArrayListExaminationAngiographyRooms.get(i).vImplementExaminationRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListConsultationRooms, ArrayListOperationRooms, erWaitingRoom );
			}

			// 診察室プロセスを実行します。
//			System.out.println("診察室プロセス");
			for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
			{
				ArrayListConsultationRooms.get(i).vImplementConsultation( erWaitingRoom, ArrayListExaminationXRayRooms, ArrayListExaminationCTRooms, ArrayListExaminationMRIRooms, ArrayListExaminationAngiographyRooms, ArrayListOperationRooms, ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListGeneralWardRooms );
			}

			// 手術室プロセスを実行します。
//			System.out.println("手術室プロセス");
			for( i = 0;i < ArrayListOperationRooms.size(); i++ )
			{
				ArrayListOperationRooms.get(i).vImplementOperationRoom( ArrayListOperationRooms, ArrayListIntensiveCareUnitRooms, ArrayListHighCareUnitRooms, ArrayListGeneralWardRooms );
			}

			// 集中治療室プロセスを実行します。
//			System.out.println("ICUプロセス");
			for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
			{
				ArrayListIntensiveCareUnitRooms.get(i).vImplementIntensiveCareUnitRoom( ArrayListOperationRooms, ArrayListHighCareUnitRooms, ArrayListGeneralWardRooms );
			}

			// 高度治療室プロセスを実行します。
//			System.out.println("HCUプロセス");
			for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
			{
				ArrayListHighCareUnitRooms.get(i).vImplementHighCareUnitRoom( ArrayListOperationRooms, ArrayListIntensiveCareUnitRooms, ArrayListGeneralWardRooms );
			}

			// 一般病棟プロセスを実行します。
//			System.out.println("一般病棟プロセス");
			for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
			{
				ArrayListGeneralWardRooms.get(i).vImplementGeneralWardRoom( ArrayListOperationRooms, ArrayListHighCareUnitRooms );
			}

			// 結果を出力します。
			vOutput();

			// 逆シミュレーション用
			// 初療室、診察室にいる患者の現在シミュレーションステップの場合の数を求めます。
			vEdAdmittedPatientNum();
//			vOutputEdAdmittedPatientNum();

			// 現在のトリアージ判定結果別受診数を求めます。
			vEdAdmittedPatientTriageCategoryNum();
//			vOutputEdAdmittedPatientTriageCategoryNum();

			// 人工呼吸器をつけている患者の総数
			vEdVentilatorsNum();
//			vOutputEdVentilatorsNum();

			// 初療室、診察室、ICU、HCUにいる患者の現在シミュレーションステップにおける総数を求めます。
			vEdTotalAdmittedPatientNum();
//			vOutputEdTotalAdmittedPatientNum();

			// 救急部門にいる患者の現在シミュレーションステップにおける総数を求めます。
			vEdTotalPatientNum();
//			vOutputEdTotalPatientNum();

			// 待合室にいる患者の現在シミュレーションステップにおける数を求めます。
			vWaitingRoomPatientNum();
//			vOutputWaitingRoomPatientNum();

			// もっとも長くいる患者の在院時間を求めます。
			vLongestAdmittedTime();
//			vOutputLongestAdmittedTime();

			// 最後に病床へ割り当てられた患者の到着から入院までの時間を求めます。
			vLastBedTime();
//			vOutputLastBedTime();

			// NEDOCSを算出します。
			lfNedocs = lfCalcNedocs();

			// EDWINを算出します。
			lfEdwin = lfCalcEdWin();

			// ED Work Scoreを算出します。
			lfEdWorkScore = lfCalcEdWorkScore();

			// 現在時点でのNEDOCS, EDWIN, ED Work Scoreの最大値を取得します。
			vMaxGetEvaluationIndex( lfSecond );

			if( iInverseSimMode == 0 )
			{
				// 逆シミュレーションの場合は書き込まないようにします。
				vWriteFile( iFileWriteModeFlag, lfSecond );
			}
		}
		catch( ERDoctorAgentException edae )
		{
			String strMethodName = edae.strGetMethodName();
			String strClassName = edae.strGetClassName();
			String strErrDetail = edae.strGetErrDetail();
			int iErrCode = edae.iGetErrCode();
			int iErrLine = edae.iGetErrorLine();
			System.out.println( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
		}
		catch( ERNurseAgentException enae )
		{
			String strMethodName = enae.strGetMethodName();
			String strClassName = enae.strGetClassName();
			String strErrDetail = enae.strGetErrDetail();
			int iErrCode = enae.iGetErrCode();
			int iErrLine = enae.iGetErrorLine();
			System.out.println( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );

		}
		catch( ERClinicalEngineerAgentException ereae )
		{
			String strMethodName = ereae.strGetMethodName();
			String strClassName = ereae.strGetClassName();
			String strErrDetail = ereae.strGetErrDetail();
			int iErrCode = ereae.iGetErrCode();
			int iErrLine = ereae.iGetErrorLine();
			System.out.println( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
		}
		catch( IOException ioe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			String str;
			System.out.println("IOException");
			ERDepartmentLog.warning( "IOException" );
			// エラー詳細を出力
			for( i = 0;i < ioe.getStackTrace().length; i++ )
			{
				str = "クラス名" + "," + ioe.getStackTrace()[i].getClassName();
				str += "メソッド名" + "," + ioe.getStackTrace()[i].getMethodName();
				str += "ファイル名" + "," + ioe.getStackTrace()[i].getFileName();
				str += "行数" + "," + ioe.getStackTrace()[i].getLineNumber();
				ERDepartmentLog.warning( str );
				System.out.println( str );
			}
		}
		catch( NullPointerException npe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			// エラー詳細を出力
			System.out.println("NullPointAccess");
			ERDepartmentLog.warning( "NullPointAccess" );
			for( i = 0;i < npe.getStackTrace().length; i++ )
			{
				String str = "クラス名" + "," + npe.getStackTrace()[i].getClassName();
				str += "メソッド名" + "," + npe.getStackTrace()[i].getMethodName();
				str += "ファイル名" + "," + npe.getStackTrace()[i].getFileName();
				str += "行数" + "," + npe.getStackTrace()[i].getLineNumber();
				ERDepartmentLog.warning( str );
				System.out.println( str );
			}
//			try
//			{
//				this.wait( 100000 );
//			}
//			catch( InterruptedException ie )
//			{
//
//			}
		}
		catch( ArrayIndexOutOfBoundsException iobe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			// エラー詳細を出力
			for( i = 0;i < iobe.getStackTrace().length; i++ )
			{
				String str = "クラス名" + "," + iobe.getStackTrace()[i].getClassName();
				str += "メソッド名" + "," + iobe.getStackTrace()[i].getMethodName();
				str += "ファイル名" + "," + iobe.getStackTrace()[i].getFileName();
				str += "行数" + "," + iobe.getStackTrace()[i].getLineNumber();
				ERDepartmentLog.warning( str );
				System.out.println( str );
			}
//			try
//			{
//				this.wait( 100000 );
//			}
//			catch( InterruptedException ie )
//			{
//
//			}
		}

		// 無限ループにはまりこむ場合があったので、終了条件は例外処理後に移動。
//		try
		{
			lfTotalTime += lfSecond;
			// 終了条件用の値を設定し、終了条件に達したら自動的に終了します。
			erFinisher.vSetCurrentTime( lfTotalTime );

			// プログレスバーの表示
			vCurrentProcessStatus();
			if( erFinisher.isFinish() == true )
			{
				System.out.println("はーいっす。");
				// ここにファイル出力のクローズ処理を入れます。
				iFinishAgentFlag = true;
//				vTerminate( );
			}
		}
//		catch( IOException ioe )
//		{
//			StackTraceElement ste[] = (new Throwable()).getStackTrace();
//			String str;
//			System.out.println("IOException");
//			ERDepartmentLog.warning( "IOException" );
//			// エラー詳細を出力
//			for( i = 0;i < ioe.getStackTrace().length; i++ )
//			{
//				str = "クラス名" + "," + ioe.getStackTrace()[i].getClassName();
//				str += "メソッド名" + "," + ioe.getStackTrace()[i].getMethodName();
//				str += "ファイル名" + "," + ioe.getStackTrace()[i].getFileName();
//				str += "行数" + "," + ioe.getStackTrace()[i].getLineNumber();
//				ERDepartmentLog.warning( str );
//				System.out.println( str );
//			}
//		}
	}
}
