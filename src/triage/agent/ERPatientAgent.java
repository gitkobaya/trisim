package triage.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Logger;

import jp.ac.nihon_u.cit.su.furulab.fuse.Message;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;
import triage.ERDepartment;
import utility.csv.CCsv;
import utility.initparam.InitSimParam;
import utility.node.ERTriageNode;
import utility.sfmt.Rand;


public class ERPatientAgent extends Agent
{
	private static final long serialVersionUID = -3410291308252929141L;

	public static final int ERPA_AGENT_SUCCESS					= 0;
	public static final int ERPA_AGENT_FATAL_ERROR				= -401;
	public static final int ERPA_AGENT_MEMORYALLOCATE_ERROR		= -402;
	public static final int ERPA_AGENT_NULLPOINT_ERROR			= -403;
	public static final int ERPA_AGENT_INVALID_ARGUMENT_ERROR	= -404;
	public static final int ERPA_AGENT_INVALID_DATA_ERROR		= -405;
	public static final int ERPA_AGENT_ARRAY_INDEX_ERROR		= -406;
	public static final int ERPA_AGENT_ZERO_DIVIDE_ERROR		= -407;

	private static int iDeathNum = 0;

	// TODO 自動生成されたメソッド・スタブ

	// AIS関係パラメータ
	double lfInternalAISHead;			// 内部頭部のAIS
	double lfInternalAISFace;			// 内部顔面のAIS
	double lfInternalAISNeck;			// 内部頸部（首）のAIS
	double lfInternalAISThorax;			// 内部胸部のAIS
	double lfInternalAISAbdomen;		// 内部腹部のAIS
	double lfInternalAISSpine;			// 内部脊椎のAIS
	double lfInternalAISUpperExtremity;	// 内部上肢のAIS
	double lfInternalAISLowerExtremity;	// 内部下肢のAIS
	double lfInternalAISUnspecified;	// 内部特定部位でない。（体表・熱傷・その他外傷）
	double lfInternalHeartSign;			// 心肺の状態
	double lfInternalSkinSign;			// 皮膚の血色状態
	double lfExaminationTime;			// 検査時間
	double lfGcs;						// GlasgowComaScale 意識レベル
	double lfTriss;						// Trauma Score - injury Severity Score
	double lfRts;						// Revised Trauma Score
	double lfIss;						// Injury Severity Score
	double lfProbabilitySurvival;		// 生存率
	double lfArrivalTime;				// 患者が病院に到達した時間

	double lfOneDayTimeDeathRate;
	double lfSevenDayTimeDeathRate;
	double lfTwentyDayTimeDeathRate;
	double lfInfinityDayTimeDeathRate;
	double lfPrevTimeDeathRate;

	// バイタルサイン関係パラメータ
	double lfSbp;						// Systolic Blood Pressure 最大血圧
	double lfDbp;						// Diastolic Blood Pressure 拡張期血圧
	double lfRr;						// Respiratory Rate 呼吸速度
	double lfPulse;						// 脈拍
	double lfBodyTemperature;			// 体温
	double lfAge;						// 患者の年齢
	double lfSpO2;						//
	int iSex;							// 患者の性別
	int iEyeOpening;
	int iBestVerbalResponse;
	int iBestMotorResponse;
	int iNumberOfTrauma;				// 外傷箇所数
	double lfLeucocyte;					// 白血球数
	int iSurvivalFlag;					// 生存か否かを表すフラグ
	double lfInternalFaceSign;			// 顔面の血色
	int iExaminationFinishFlag;			// 検査終了フラグ
	int iDisChargeFlag;					// 退院可能か否か

	int iSurvivalCount;					// 生存判定
	double lfSurvivalProbability;		// 生存確率
	double lfTimeCourse;				// 病院到着後経過時間
	double lfHospitalStayDay;			// 患者の入院日数
	int iKindOfAgent;					// 到達患者の種類
	double lfKindOfPatientProbability;	// 到達患者の種類別確率
	int iLocation;						// 患者が現在いる部屋
	double lfWaitTime;					// 総待ち時間
	double lfCurrentWaitTime;			// 今回の待ち時間
	double lfTotalTime;					// 総経過時間
	double lfStayHospitalTime;			// 入院時間
	double lfIntensiveCareUnitStayTime;	// 集中治療室に入院している時間
	double lfHighCareUnitStayTime;		// 高度治療室に入院している時間
	double lfGeneralWardStayTime;		// 一般病棟に入院している時間
	int iStayIntensiveCareUnitFlag;		// 集中治療室にいるか否か
	int iStayHighCareUnitFlag;			// 高度治療室にいるか否か
	int iStayGeneralWardFlag;			// 一般病棟にいるか否か

	int iStayHospitalFlag;				// 入院するか否かのフラグ
	int iStayHospitalStartFlag;			// 入院開始フラグ

	int iNurseAttended;					// 看護師に対応してもらっているかどうかを表すフラグ
	int iObservedFlag;					// 観察をしてもらったかどうかを表すフラグ
	int iObservationWait;				// 観察プロセスを受けた回数
	double lfCurrentObservationWaitTime;// 観察室の現在の時間
	double lfObservationWaitTime;		// 観察室総待機時間
	double lfCurrentConsultationTime;	// 診察室の現在の時間
	double lfConsultationTime;			// 診察室総時間
	double lfCurrentOperationTime;		// 手術室の現在の時間
	double lfOperationTime;				// 手術室総時間
	double lfCurrentEmergencyTime;		// 初療室の現在の時間
	double lfEmergencyTime;				// 初療室総時間
	double lfMoveTime;					// 部屋間を移動に要する時間

	double lfXRayRoomStayTime;			// X線室の滞在時間
	double lfCTRoomStayTime;			// CT室の滞在時間
	double lfMRIRoomStayTime;			// MRI室の滞在時間
	double lfAngiographyRoomStayTime;	// 血管造影室の滞在時間
	double lfFastRoomStayTime;			// Fast室の滞在時間

	Rand rnd;							// 乱数クラス

	ArrayList<Integer> ArrayListDoctorAgentIds;		// 全医師のID
	ArrayList<Integer> ArrayListNurseAgentIds;		// 全看護師のID

	double lfJudgedAISHead;				// 判定結果頭部のAIS
	double lfJudgedAISFace;				// 判定結果顔面のAIS
	double lfJudgedAISNeck;				// 判定結果頸部（首）のAIS
	double lfJudgedAISThorax;			// 判定結果胸部のAIS
	double lfJudgedAISAbdomen;			// 判定結果腹部のAIS
	double lfJudgedAISSpine;			// 判定結果脊椎のAIS
	double lfJudgedAISUpperExtremity;	// 判定結果上肢のAIS
	double lfJudgedAISLowerExtremity;	// 判定結果下肢のAIS
	double lfJudgedAISUnspecified;		// 判定結果特定部位でない。（体表・熱傷・その他外傷）
	String strInjuryHeadStatus;			// 文字による頭部AIS
	String strInjuryFaceStatus;			// 文字による顔面AIS
	String strInjuryNeckStatus;			// 文字による頸部AIS
	String strInjuryThoraxStatus;			// 文字による胸部AIS
	String strInjuryAbdomenStatus;			// 文字による腹部AIS
	String strInjurySpineStatus;			// 文字による脊椎AIS
	String strInjuryUpperExtremityStatus;	// 文字による上肢AIS
	String strInjuryLowerExtremityStatus;	// 文字による下肢AIS
	String strInjuryUnspecifiedStatus;		// 文字による体表・熱傷・その他外傷AIS
	String strRespirationSignStatus;		// 呼吸状態
	String strConsciousnessSignStatus;		// 意識状態
	String strFaceSignStatus;				// 顔面の血色状態
	String strSkinSignStatus;				// 皮膚の血色の状態
	String strHeartSignStatus;				// 心肺の状態
	String strPulseSignStatus;				// 脈拍の状態
	String strBloodPressureSignStatus;		// 血圧の状態
	String strBodyTemperatureSignStatus;	// 体温の状態
	String strSpO2SignStatus;				// SpO2の状態

	int iDoctorDepartment;						// 担当医師の所属部門
	int iNurseDepartment;						// 担当看護師の所属部門
	int iDoctorId;								// 担当医師のID
	int iNurseId;								// 担当看護師のID
	int iEmergencyLevel;						// 緊急度レベル
	int iStartEmergencyLevel;					// 初回に判定された緊急度レベル

	private int iConsultationRoomWaitFlag;				// 診察待機フラグ
	private int iEmergencyRoomWaitFlag;				// 初療室待機フラグ
	private int iExaminationXRayRoomWaitFlag;			// X線室待機フラグ
	private int iExaminationCTRoomWaitFlag;			// CT室待機フラグ
	private int iExaminationMRIRoomWaitFlag;			// MRI室待機フラグ
	private int iExaminationAngiographyRoomWaitFlag;	// 血管造影室待機フラグ
	private int iExaminationFastRoomWaitFlag;			// Fast室待機フラグ
	private int iObservationRoomWaitFlag;				// 観察室待機フラグ
	private int iOperationRoomWaitFlag;				// 手術室待機フラグ
	private int iSereveInjuryObservationRoomWaitFlag;	// 重症観察室待機フラグ
	private int iIntensiveCareUnitRoomWaitFlag;		// 集中治療室待機フラグ
	private int iHighCareUnitRoomWaitFlag;				// 高度治療室待機フラグ
	private int iGeneralWardRoomWaitFlag;				// 一般病棟待機フラグ

	private double lfMoveWaitingTime;					// 部屋移動時間
	private int iMoveRoomFlag;							// 部屋移動実施中フラグ

	private double lfHospitalStayInitAISHead;			// 入院初期の頭部AIS
	private double lfHospitalStayInitAISFace;			// 入院初期の顔面AIS
	private double lfHospitalStayInitAISNeck;			// 入院初期の頸部AIS
	private double lfHospitalStayInitAISThorax;			// 入院初期の胸部AIS
	private double lfHospitalStayInitAISAbdomen;		// 入院初期の腹部AIS
	private double lfHospitalStayInitAISSpine;			// 入院初期の脊椎AIS
	private double lfHospitalStayInitAISLowerExtremity;	// 入院初期の下肢AIS
	private double lfHospitalStayInitAISUpperExtremity;	// 入院初期の上肢AIS
	private double lfHospitalStayInitAISUnspecified;	// 入院初期の表面、熱傷、その他外傷AIS
	private double lfAISRevisedSeries;					// 入院時の患者のAIS改善割合


	private Queue<Message> mesQueueData;				// メッセージキューを保持する変数
	private int iMoveWaitFlag;
	private int iEnterHighCareUnitFlag;					// 高度治療室に入ったかどうかを表すフラグ
	private int iEnterIntensiveCareUnitFlag;			// 集中治療室に入ったかどうかを表すフラグ
	private int iEnterGeneralWardFlag;					// 一般病棟に入ったかどうかを表すフラグ

	private CCsv csvWriteAgentData;						// 終了時の出力ファイルデータ
	private CCsv csvWriteAgentStartData;				// 開始時の出力ファイルデータ

	private Logger cPatientAgentLog;					// 患者のログ出力設定

	private double lfSimulationEndTime;					// シミュレーション終了時間

	private ArrayList<ERTriageNode> ArrayListRoute;		// 次に患者が移動する先の経路を格納した配列
	private ERTriageNode erCurNodeRoute;				// 移動中の患者が現在通過したノード
	private ERTriageNode erNextNodeRoute;				// 移動中の患者が次に通過するノード
	private int iLocNode = 0;							// 移動中の患者エージェントの自ノードの配列参照位置

	private int iInjuryRandomMode = 0;					// 患者の傷病状態の乱数発生方法(0:一様乱数 1:正規乱数)

	private int iInverseSimMode;						// 逆シミュレーションモードを設定します。

	private ERDoctorAgent erConsultationDoctor;

	private Object csPatientCriticalSection;			// クリティカルセクション用

	private int iFileWriteMode;							// 長時間シミュレーション用ファイル出力モード

	private InitSimParam initSimParam;					// 職設定ファイル操作用変数

	private double lfWidth;								// 患者エージェント描画用幅
	private double lfHeight;							// 患者エージェント描画用高さ
	private ERDepartment erDepartment;					// 救急部門のインスタンス

	/**
	 * <PRE>
	 *   コンストラクタ
	 * </PRE>
	 */
	public ERPatientAgent()
	{
		vInitialize();
	}

	/**
	 * <PRE>
	 *   コンストラクタ
	 * </PRE>
	 * @param lfDataAge 年齢
	 * @param iSexFlag 性別
	 * @param lfDataSbp 血圧量
	 * @param lfDataRr 呼吸数
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public ERPatientAgent( double lfDataAge, int iSexFlag, double lfDataSbp, double lfDataRr )
	{
		// 聖隷浜松病院の独歩外来の割合を設定します。
		lfKindOfPatientProbability = 0.683626606;
		lfAge = lfDataAge;
		iSex = iSexFlag;
		lfSbp = lfDataSbp;
		lfRr = lfDataRr;
	}

	/**
	 * <PRE>
	 *   エージェントの初期化を実行します。
	 * </PRE>
	 */
	public void vInitialize()
	{
		String strFileName					= "";
		lfInternalAISHead					= 0.0;
		lfInternalAISFace					= 0.0;	// 顔面のAIS
		lfInternalAISNeck					= 0.0;	// 頸部（首）のAIS
		lfInternalAISThorax 				= 0.0;	// 胸部のAIS
		lfInternalAISAbdomen				= 0.0;	// 腹部のAIS
		lfInternalAISSpine					= 0.0;	// 脊椎のAIS
		lfInternalAISUpperExtremity 		= 0.0;	// 上肢のAIS
		lfInternalAISLowerExtremity 		= 0.0;	// 下肢のAIS
		lfInternalAISUnspecified			= 0.0;	// 特定部位でない。（体表・熱傷・その他外傷）
		lfGcs								= 0.0;	// GlasgowComaScale 意識レベル
		lfTriss								= 0.0;	// Trauma Score - injury Severity Score
		lfRts								= 0.0;	// Revised Trauma Score
		lfSbp								= 0.0;	// Systolic Blood Pressure 収縮期血圧
		lfDbp								= 0.0;	// Diastolic Blood Pressure 拡張期血圧
		lfRr								= 0.0;	// Respiratory Rate 呼吸速度
		lfPulse								= 0.0;	// 脈拍
		lfBodyTemperature					= 0.0;	// 体温
		lfIss								= 0.0;	// Injury Severity Score
		lfAge								= 0.0;	// 患者の年齢
		iSex								= 0;	// 患者の性別
		iSurvivalFlag						= 1;
		iEyeOpening							= 0;
		iBestVerbalResponse					= 0;
		iBestMotorResponse					= 0;
		iNumberOfTrauma						= 0;			// 外傷箇所数
		lfSurvivalProbability				= 0.0;			// 生存確率
		lfTimeCourse						= 0.0;			// 病院到着後経過時間
		iKindOfAgent						= 0;			// 到達患者の種類
		lfKindOfPatientProbability			= 0.0;			// 到達患者の種類別確率
		strInjuryHeadStatus					= "痛くない";	// 文字による頭部AIS
		strInjuryFaceStatus					= "痛くない";	// 文字による顔面AIS
		strInjuryNeckStatus					= "痛くない";	// 文字による頸部AIS
		strInjuryThoraxStatus				= "痛くない";	// 文字による胸部AIS
		strInjuryAbdomenStatus				= "痛くない";	// 文字による腹部AIS
		strInjurySpineStatus				= "痛くない";	// 文字による脊椎AIS
		strInjuryUpperExtremityStatus		= "痛くない";	// 文字による上肢AIS
		strInjuryLowerExtremityStatus		= "痛くない";	// 文字による下肢AIS
		strInjuryUnspecifiedStatus			= "痛くない";	// 文字による体表・熱傷・その他外傷AIS
		lfWaitTime							= 0.0;			// 患者の待ち時間
		iExaminationFinishFlag				= 0;			// 検査終了フラグ
		iDisChargeFlag						= 0;			// 退院可能フラグ
		iObservationWait					= 0;			// 観察プロセスを受けた回数
		iSurvivalFlag						= 1;			// 生存フラグ
		iSurvivalCount						= 0;			// 生存判定
		lfHospitalStayDay					= 0;			// 患者の入院日数
		lfHospitalStayInitAISHead 			= 0.0;
		lfHospitalStayInitAISFace 			= 0.0;
		lfHospitalStayInitAISNeck 			= 0.0;
		lfHospitalStayInitAISThorax 		= 0.0;
		lfHospitalStayInitAISAbdomen		= 0.0;
		lfHospitalStayInitAISSpine			= 0.0;
		lfHospitalStayInitAISLowerExtremity = 0.0;
		lfHospitalStayInitAISUpperExtremity = 0.0;
		lfHospitalStayInitAISUnspecified	= 0.0;
		lfAISRevisedSeries					= 0.0;
		lfArrivalTime						= 0.0;

//		long seed;
//		seed = (long)(Math.random()*Long.MAX_VALUE);
//		rnd = null;
//		rnd = new Sfmt( (int)seed );

		ArrayListDoctorAgentIds	 			= new ArrayList<Integer>();		// 全医師のID
		ArrayListNurseAgentIds				= new ArrayList<Integer>();		// 全看護師のID
		mesQueueData						= new LinkedList<Message>();

		lfMoveWaitingTime = 0.0;
		iMoveWaitFlag = 0;
		lfMoveTime							= 180.0;		// 部屋間を移動に要する時間

//		try
//		{
//			csvWriteAgentData					= new CCsv();
//			strFileName							= "./er/pa/erpa_end" + this.getId() + ".csv";
//			csvWriteAgentData.vOpen( strFileName, "write");
//			csvWriteAgentStartData				= new CCsv();
//			strFileName							= "./er/pa/erpa_start" + this.getId() + ".csv";
//			csvWriteAgentStartData.vOpen( strFileName, "write");
//		}
//		catch( IOException ioe )
//		{
//
//		}

		lfOneDayTimeDeathRate			= 0.0;
		lfSevenDayTimeDeathRate			= 0.0;
		lfTwentyDayTimeDeathRate		= 0.0;
		lfInfinityDayTimeDeathRate		= 0.0;
		lfPrevTimeDeathRate				= 0.0;
		lfCurrentObservationWaitTime	= 0.0;	// 観察室の現在の時間
		lfObservationWaitTime			= 0.0;	// 観察室総待機時間
		lfCurrentObservationWaitTime	= 0.0;	// 観察室の現在の時間
		lfObservationWaitTime			= 0.0;	// 観察室総待機時間
		lfCurrentConsultationTime		= 0.0;	// 診察室の現在の時間
		lfConsultationTime				= 0.0;	// 診察室総時間
		lfCurrentOperationTime			= 0.0;	// 手術室の現在の時間
		lfOperationTime					= 0.0;	// 手術室総時間
		lfCurrentEmergencyTime			= 0.0;	// 初療室の現在の時間
		lfEmergencyTime					= 0.0;	// 初療室総時間
		lfXRayRoomStayTime				= 0.0;	// X線室の滞在時間
		lfCTRoomStayTime				= 0.0;	// CT室の滞在時間
		lfMRIRoomStayTime				= 0.0;	// MRI室の滞在時間
		lfAngiographyRoomStayTime		= 0.0;	// 血管造影室の滞在時間
		lfFastRoomStayTime				= 0.0;	// Fast室の滞在時間

		iEnterHighCareUnitFlag			= 0;	// 高度治療室に入ったかどうかを表すフラグ
		iEnterIntensiveCareUnitFlag		= 0;	// 集中治療室に入ったかどうかを表すフラグ
		iEnterGeneralWardFlag			= 0;	// 一般病棟に入ったかどうかを表すフラグ

		iFileWriteMode					= 0;


		double vel[] = {2.0,2.0,2.0};
		this.setVelocity( vel );
	}

	/**
	 * <PRE>
	 *    ファイルの読み込みを行います。
	 * </PRE>
	 * @param iFileWriteModeData	ファイル書き込みモード（0すべて書き込み、1最初と最後のみ）
	 * @throws IOException	ファイル読み込み時エラー
	 */
	public void vSetReadWriteFile( int iFileWriteModeData ) throws IOException
	{
		String strFileName = "";
		iFileWriteMode = iFileWriteModeData;
		if( iFileWriteModeData == 1 )
		{
			csvWriteAgentData					= new CCsv();
			strFileName							= "./er/pa/erpa_end" + this.getId() + ".csv";
			csvWriteAgentData.vOpen( strFileName, "write");
			csvWriteAgentStartData				= new CCsv();
			strFileName							= "./er/pa/erpa_start" + this.getId() + ".csv";
			csvWriteAgentStartData.vOpen( strFileName, "write");
		}
		else
		{
			csvWriteAgentData					= new CCsv();
			strFileName							= "./er/pa/erpa_end" + this.getId() + ".csv";
			csvWriteAgentData.vOpen( strFileName, "write");
		}
	}

	/**
	 * <PRE>
	 *    終了処理を実行します。
	 * </PRE>
	 * @throws IOException java 標準エラー
	 */
	public void vTerminate() throws IOException
	{
		if( iInverseSimMode == 0 || iInverseSimMode == 1 )
		{
			if( csvWriteAgentData != null )
			{
				csvWriteAgentData.vClose();
				csvWriteAgentData = null;
			}
			if( csvWriteAgentStartData != null )
			{
				csvWriteAgentStartData.vClose();
				csvWriteAgentStartData = null;
			}
		}
		if( ArrayListDoctorAgentIds != null )
		{
			ArrayListDoctorAgentIds.clear();			// 全医師のID
			ArrayListDoctorAgentIds = null;
		}
		if( ArrayListNurseAgentIds != null )
		{
			ArrayListNurseAgentIds.clear();				// 全看護師のID
			ArrayListNurseAgentIds = null;
		}
		if( mesQueueData != null )
		{
			mesQueueData.clear();						// メッセージキューを保持する変数
			mesQueueData = null;
		}
		rnd = null;
		iDeathNum = 0;
	}

	/**
	 * <PRE>
	 *    FUSEエンジンにエージェントを登録します。
	 * </PRE>
	 * @param engine	FUSEシミュレーションエンジンインスタンス
	 */
	public void vSetSimulationEngine( SimulationEngine engine )
	{
		engine.pause();
		if( engine.isPaused() == true )
		{
			engine.addAgent(this);
		}
		engine.resume();
	}

	/**
	 * <PRE>
	 *   患者のパラメータをランダムに割り当てます。
	 * </PRE>
	 */
	public void vSetRandom()
	{
		int i;
		// 損傷個所を算出
		iNumberOfTrauma = iInjuryAISNumber();

		// 損傷部位の生成及びAIS算出
		for( i = 0;i < iNumberOfTrauma; i++ )
		{
			vAISAnatomySeverity( iInjuryAISPartAnatomy() );
		}
		// バイタルサイン値を生成します。
		vGenerateVitalSign();

		// 現在のAIS値から患者状態の文字列を設定します。
		vStrSetInjuryStatus();
	}

	/**
	 * <PRE>
	 *    生存確率を算出します。（初期起動時に使用。）
	 * </PRE>
	 * @return	シミュレーション開始時の生存確率
	 */
	public double lfCalcInitialSurvivalProbability()
	{
		// ISS算出
		double lfIssData = lfCalcIss();

		// GCSの算出
		double lfGcsData = lfCalcGcs( true );

		// RTS算出
		double lfRtsData = lfCalcRts( lfGcsData, lfSbp, lfRr );

		// TRISS算出
		// もっとも多いとされる鈍器タイプのけがを想定する。
		double lfTrissData = lfCalcTriss( 0, lfRtsData, lfIssData, lfAge );

		// 生存率の算出
		double lfSurvivalProbabilityData = lfCurrentProbSurvival( 0.0, iSex, lfAge, lfTrissData );

		return lfSurvivalProbabilityData;
	}

	/**
	 * <PRE>
	 *   患者エージェントの行動実行します。
	 * </PRE>
	 * @throws ERPatientAgentException	患者エージェント例外
	 * @throws IOException				ファイル処理例外
	 */
	public void vImplementPatientAgent() throws ERPatientAgentException, IOException
	{
		int i = 0;
		int iTimeStep = 0;
		int iInjuryFlag		= 0;
		double lfRand		= 0.0;
		double lfOneDay		= 86400;

/*---------------------------- 患者が到達したので以下の行を実行します。------------------------------*/
		if( rnd == null ) return;

		// ISS算出
		lfIss = lfCalcIss();

		// GCSの算出
		lfGcs = lfCalcGcs( true );

		// RTS算出
		lfRts = lfCalcRts( lfGcs, lfSbp, lfRr );

		// TRISS算出
		lfTriss = lfCalcTriss( iInjuryFlag, lfRts, lfIss, lfAge );

		iTimeStep = (int)this.getEngine().getLatestTimeStep()/1000;
		for( i = 0;i < iTimeStep; i++ )
		{
			// 生存率の算出
//			lfSurvivalProbability = lfCurrentProbSurvival( (lfTimeCourse+iTimeStep)/lfOneDay, iSex, lfAge, lfTriss );
			lfSurvivalProbability = lfCurrentProbSurvival( (lfTimeCourse+i)/lfOneDay, iSex, lfAge, lfTriss );

			// 生存可否判定
	//		lfRand = rnd.NextUnif()*0.93;
			if( rnd != null )
			{
				lfRand = rnd.NextUnif();
			}
			// もしも、メルセンヌツイスターオブジェクトがnullの場合はjava標準の擬似乱数を使用します。
			else
			{
				Random rand = new Random();
				lfRand = rand.nextDouble();
			}
			// 生存確率を超えたら本当に死んでしまったのかを判定する。
			// 5回判定してやはりなくなられてしまったとわかった場合は死亡とみなす。
			if( lfRand > lfSurvivalProbability )
			{
				iSurvivalCount++;
			}
			else
			{
				iSurvivalCount = 0;
			}

			// 重みをかけて死亡率の調整をします。
			if( iSurvivalCount*initSimParam.lfGetSurvivalProbabilityWeight() > initSimParam.lfGetSurvivalJudgeCount() )
			{
				// 死亡とみなし、医師エージェント及び看護師エージェントにその内容を送信します。
				// 生存とし、この場合における患者エージェントの状態を担当者に送信します。
				if( iLocation == 1 || iLocation == 2 || iLocation == 3 || iLocation == 6 || iLocation == 7 || iLocation == 8 )
				{
					if( iDoctorId != 0 )
					{
						// 診察室、初療室の場合にメッセージを送信します。
//						vSendToDoctorAgentMessage( (ERDoctorAgent)(this.getEngine().getAgentById( iDoctorId )), (int)this.getId(), iDoctorId );
					}
				}
				else if( iLocation == 9 || iLocation == 4 || iLocation == 5 )
				{
					if( iNurseId != 0 )
					{
						// 待合室、観察室、重症観察室の場合にメッセージを送信します。
//						vSendToNurseAgentMessage(  (ERNurseAgent)(this.getEngine().getAgentById( iNurseId )), (int)this.getId(), iNurseId );
					}
				}

				iSurvivalFlag = 0;
				cPatientAgentLog.info(this.getId() + "," + "なくなられました・・・。");
				iDeathNum++;
				cPatientAgentLog.info("死亡人数：" + iDeathNum);
//				System.out.println("死亡人数：" + iDeathNum);

				this.getEngine().addExitAgent(this);
				this.setPosition(0.0, 0.0, 0.0);

//				System.out.println("なくなられました・・・");

				if( iInverseSimMode == 0 || iInverseSimMode == 1 )
				{
					vFlushFile( 0 );
				}
				break;
			}
		}
	}

	/**
	 * <PRE>
	 *   患者の現在の状態の生存率を算出します。
	 * </PRE>
	 * @param lfTimeCourseData	病院搬送後の経過時間
	 * @param iSexFlag			性別
	 * @param lfDataAge			年齢
	 * @param lfTrissData		TRISS値
	 * @return 累積の生存率
	 */
	private double lfCurrentProbSurvival( double lfTimeCourseData, int iSexFlag, double lfDataAge, double lfTrissData )
	{
		double lfProb1 = 1.0;
		double lfProb2 = 1.0;
		double lfProb3 = 1.0;

		// 生存率を算出。
		// TRISSモデルによる生存確率の計算
		lfProb1 = lfCalcTrissProbabilitySurvival( lfTrissData );
		// 外傷データバンクからの近似計算
		// （外傷データバンク報告では死亡者は全体の1割なのでそれを加味してなおかつその中での死亡確率を算出する。）
		lfProb2 = 1.0-lfCalcDeathRateTimeCourse( lfTimeCourseData );
		lfProb3 = 1.0-lfCalcDeathRateMSexYear( iSexFlag, lfDataAge )*0.112920749;
//		lfProb2 = 1.0-lfCalcDeathRateTimeCourse( lfTimeCourseData );
//		lfProb3 = 1.0-lfCalcDeathRateMSexYear( iSexFlag, lfDataAge );

		return lfProb1*lfProb2*lfProb3;
	}

	/**
	 * <PRE>
	 *   現時点でのTRISSによる生存率を算出します。
	 * </PRE>
	 * @param lfData TRISS値
	 * @return 生存率
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	private double lfCalcTrissProbabilitySurvival( double lfData )
	{
		double lfProb = 0.0;
		lfProb = 1.0/( 1.0+Math.exp(-lfData) );
		return lfProb;
	}

	/**
	 * <PRE>
	 *   現時点でのTrauma and Injury Severity Scoreを算出します。
	 *   (藤木ら 2009)のを採用しています。
	 * </PRE>
	 * @param iFlag   0 鈍的外傷
	 *                1 鋭的外傷
	 * @param lfDataRts RTS値
	 * @param lfDataIss ISS値
	 * @param lfDataAge 年齢
	 * @return TRISS
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	private double lfCalcTriss( int iFlag, double lfDataRts, double lfDataIss, double lfDataAge )
	{
		double lfB0 = -2.1928;
		double lfB1 = 0.9325;
		double lfB2 = 0.0705;
		double lfB3 = 1.41778;
		double lfDataTriss = 0.0;

		// 鈍的外傷
		if( iFlag == 0 )
		{
			lfB0 = -2.1928;
			lfB1 = 0.9325;
			lfB2 = -0.0705;
			lfB3 = -1.41778;
		}
		// 鋭的外傷
		else
		{
			lfB0 = -0.8050;
			lfB1 = 0.7359;
			lfB2 = -0.0717;
			lfB3 = -0.8222;
		}
		lfDataTriss = lfB0 + lfB1*lfDataRts + lfB2*lfDataIss + lfB3*(lfDataAge >= 7 ? 1 : 0);
		return lfDataTriss;
	}

	/**
	 * <PRE>
	 *    現時点でのRevised Trauma Scoreを算出します。
	 *    （藤木ら 2009）のを採用します。
	 * </PRE>
	 * @param lfDataGcs GCS値
	 * @param lfDataSbp SBP値
	 * @param lfDataRr  RR値
	 * @return RTS値
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	private double lfCalcRts( double lfDataGcs, double lfDataSbp, double lfDataRr )
	{
		double lfDataRts = 0.0;
		double lfGcsCode = 0.0;
		double lfSbpCode = 0.0;
		double lfRrCode  = 0.0;

		if( 13 <= lfDataGcs && lfDataGcs <= 15 )		lfGcsCode = 4;
		else if( 9 <= lfDataGcs && lfDataGcs <= 12 )	lfGcsCode = 3;
		else if( 6 <= lfDataGcs && lfDataGcs < 9 )		lfGcsCode = 2;
		else if( 4 <= lfDataGcs && lfDataGcs < 6 )		lfGcsCode = 1;
		else if( 3 <= lfDataGcs && lfDataGcs < 4 )		lfGcsCode = 0;
		if( lfDataSbp >= 89 )							lfSbpCode = 4;
		else if( 76 <= lfDataSbp && lfDataSbp < 89 )	lfSbpCode = 3;
		else if( 50 <= lfDataGcs && lfDataGcs <= 76 )	lfSbpCode = 2;
		else if( 1 <= lfDataGcs && lfDataGcs <= 50 )	lfSbpCode = 1;
		else if( 0 <= lfDataGcs && lfDataGcs < 1 )		lfSbpCode = 0;
		if( 10 <= lfDataRr && lfDataRr <= 29 )			lfRrCode = 4;
		else if( lfDataRr >= 29 )						lfRrCode = 3;
		else if( 6 <= lfDataRr && lfDataRr < 10 )		lfRrCode = 2;
		else if( 1 <= lfDataRr && lfDataRr < 6 )		lfRrCode = 1;
		else if( 0 <= lfDataRr && lfDataRr < 1 )		lfRrCode = 0;

		lfDataRts = 0.9013*lfGcsCode + 0.7365*lfSbpCode + 0.4668*lfRrCode;
		return lfDataRts;
	}

	/**
	 * <PRE>
	 *   現時点でのInjury Severity Scoreを算出します。
	 * </PRE>
	 * @return ISS
	 */
	private double lfCalcIss()
	{
		int i;
		double alfInternalAISData[] = new double[9];
		double lfDataIss = 0.0;
		double lfTemp = -1;
		double lfInternalAISMax1 = 0.0;
		double lfInternalAISMax2 = 0.0;
		double lfInternalAISMax3 = 0.0;
		int iAISMaxLoc1 = 0;
		int iAISMaxLoc2 = 0;
//		int iAISMaxLoc3 = 0;

		alfInternalAISData[0] = lfInternalAISHead;
		alfInternalAISData[1] = lfInternalAISFace;
		alfInternalAISData[2] = lfInternalAISNeck;
		alfInternalAISData[3] = lfInternalAISThorax;
		alfInternalAISData[4] = lfInternalAISAbdomen;
		alfInternalAISData[5] = lfInternalAISSpine;
		alfInternalAISData[6] = lfInternalAISUpperExtremity;
		alfInternalAISData[7] = lfInternalAISLowerExtremity;
		alfInternalAISData[8] = lfInternalAISUnspecified;

		// AIS値の大きいものを上位3箇所を算出します。
		for( i = 0; i < 9; i++ )
		{
			if( lfTemp < alfInternalAISData[i] )
			{
				lfTemp = alfInternalAISData[i];
				iAISMaxLoc1 = i;
				lfInternalAISMax1 = lfTemp;
			}
		}
		lfTemp = -1;
		for( i = 0; i < 9; i++ )
		{
			if( lfTemp < alfInternalAISData[i] && i != iAISMaxLoc1 )
			{
				lfTemp = alfInternalAISData[i];
				iAISMaxLoc2 = i;
				lfInternalAISMax2 = lfTemp;
			}
		}
		for( i = 0; i < 9; i++ )
		{
			if( lfTemp < alfInternalAISData[i] && i != iAISMaxLoc1 && i != iAISMaxLoc2 )
			{
				lfTemp = alfInternalAISData[i];
//				iAISMaxLoc3 = i;
				lfInternalAISMax3 = lfTemp;
			}
		}

		// 算出したらISS値を求めます。
		lfDataIss = lfInternalAISMax1*lfInternalAISMax1 + lfInternalAISMax2*lfInternalAISMax2 + lfInternalAISMax3*lfInternalAISMax3;

		return lfDataIss;
	}

	/**
	 * <PRE>
	 *   Glasgow Coma Scale（意識レベル）を算出します。
	 *   3つの要素から総合して算出します。
	 *   Improving the Glasgow Coma Scale Score: Motor Score Alone Is a Better Predictor
	 *   The Journal of TRAUMA Injury, Infection, and Critical Care Volume 54 より。
	 * </PRE>
	 * @param bAgeFlag 成人か幼児か
	 * @return GCS値
	 */
	double lfCalcGcs( boolean bAgeFlag )
	{
		double lfProb;
		double lfPrevProb;
		double lfRand;
		// 頭部に外傷がある場合に限定します。
		if( lfInternalAISHead > 0.0 )
		{
			if( bAgeFlag == true )
			{
//				lfRand = rnd.NextUnif();
//				lfRand = weibullRand(1.0, 0.05);
				lfRand = weibullRand(1.0, 0.1);

				lfPrevProb = 0.0;
//				lfProb = 0.793972955;
				lfProb = initSimParam.lfGetGcsScore15();
				if( lfRand <= lfProb )
				{
					iBestMotorResponse = 6;
					iEyeOpening = 4;
					iBestVerbalResponse = 5;
				}
				lfPrevProb = lfProb;
//				lfProb += 0.060714533;
				lfProb += initSimParam.lfGetGcsScore14();
				if( lfPrevProb <= lfRand && lfRand <= lfProb )
				{
					iBestMotorResponse = 6;
					iEyeOpening = 3;
					iBestVerbalResponse = 5;
				}
				lfPrevProb = lfProb;
//				lfProb += 0.017235668;
				lfProb += initSimParam.lfGetGcsScore13();
				if( lfPrevProb <= lfRand && lfRand <= lfProb )
				{
					iBestMotorResponse = 5;
					iEyeOpening = 4;
					iBestVerbalResponse = 4;
				}
				lfPrevProb = lfProb;
//				lfProb += 0.009092482;
				lfProb += initSimParam.lfGetGcsScore12();
				if( lfPrevProb <= lfRand && lfRand <= lfProb )
				{
					iBestMotorResponse = 5;
					iEyeOpening = 3;
					iBestVerbalResponse = 4;
				}
				lfPrevProb = lfProb;
//				lfProb += 0.008771106;
				lfProb += initSimParam.lfGetGcsScore11();
				if( lfPrevProb <= lfRand && lfRand <= lfProb )
				{
					iBestMotorResponse = 6;
					iEyeOpening = 4;
					iBestVerbalResponse = 1;
				}
				lfPrevProb = lfProb;
//				lfProb += 0.0074856;
				lfProb += initSimParam.lfGetGcsScore10();
				if( lfPrevProb <= lfRand && lfRand <= lfProb )
				{
					iBestMotorResponse = 6;
					iEyeOpening = 3;
					iBestVerbalResponse = 1;
				}
				lfPrevProb = lfProb;
//				lfProb += 0.005211243;
				lfProb += initSimParam.lfGetGcsScore9();
				if( lfPrevProb <= lfRand && lfRand <= lfProb )
				{
					iBestMotorResponse = 5;
					iEyeOpening = 2;
					iBestVerbalResponse = 2;
				}
				lfPrevProb = lfProb;
//				lfProb += 0.005794665;
				lfProb += initSimParam.lfGetGcsScore8();
				if( lfPrevProb <= lfRand && lfRand <= lfProb )
				{
					iBestMotorResponse = 5;
					iEyeOpening = 2;
					iBestVerbalResponse = 1;
				}
				lfPrevProb = lfProb;
//				lfProb += 0.008840325;
				lfProb += initSimParam.lfGetGcsScore7();
				if( lfPrevProb <= lfRand && lfRand <= lfProb )
				{
					iBestMotorResponse = 5;
					iEyeOpening = 1;
					iBestVerbalResponse = 1;
				}
				lfPrevProb = lfProb;
//				lfProb += 0.008563447;
				lfProb += initSimParam.lfGetGcsScore6();
				if( lfPrevProb <= lfRand && lfRand <= lfProb )
				{
					iBestMotorResponse = 4;
					iEyeOpening = 1;
					iBestVerbalResponse = 1;
				}
				lfPrevProb = lfProb;
//				lfProb += 0.004598156;
				lfProb += initSimParam.lfGetGcsScore5();
				if( lfPrevProb <= lfRand && lfRand <= lfProb )
				{
					iBestMotorResponse = 3;
					iEyeOpening = 1;
					iBestVerbalResponse = 1;
				}
				lfPrevProb = lfProb;
//				lfProb += 0.004879978;
				lfProb += initSimParam.lfGetGcsScore4();
				if( lfPrevProb <= lfRand && lfRand <= lfProb )
				{
					iBestMotorResponse = 2;
					iEyeOpening = 1;
					iBestVerbalResponse = 1;
				}
				lfPrevProb = lfProb;
//				lfProb += 0.064838941;
				lfProb += initSimParam.lfGetGcsScore3();
//				if( lfPrevProb <= lfRand && lfRand <= lfProb )
				if( lfPrevProb <= lfRand )
				{
					iBestMotorResponse = 1;
					iEyeOpening = 1;
					iBestVerbalResponse = 1;
				}
			}
			else
			{
				// ひとまずは一様乱数で数値を算出。
				// ただし、AISとの関連性が間違えなくあるはずなのでここは要確認の必要あり。
				iEyeOpening = (int)(4*rnd.NextUnif());
				iBestVerbalResponse = (int)(5*rnd.NextUnif());
				iBestMotorResponse = (int)(6*rnd.NextUnif());
			}
		}
		else
		{
			iBestMotorResponse = 6;
			iEyeOpening = 4;
			iBestVerbalResponse = 5;
		}
		// GCSを算出します。
		return iEyeOpening + iBestVerbalResponse + iBestMotorResponse;
	}

	/**
	 * <PRE>
	 *   患者エージェントの外傷ヶ所を生成します。
	 * </PRE>
	 * @return 外傷ヶ所数
	 */
	public int iInjuryAISNumber()
	{
		int iNumTrauma = 0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;
		if( iInjuryRandomMode == 0 )
		{
			lfRand = rnd.NextUnif();
		}
		else if( iInjuryRandomMode == 1 )
		{
			lfRand = normalRand();
		}
		else if( iInjuryRandomMode == 2 )
		{
			lfRand = rnd.NextUnif();
//			lfRand	= weibullRand( initSimParam.lfGetInjuryAISNumberWeibullAlpha(), initSimParam.lfGetInjuryAISNumberWeibullBeta() );
		}

		lfPrevProb = 0.0;
//		lfProb = 0.534646217;
		lfProb = initSimParam.lfGetInjuryAISNumber1();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			iNumTrauma = 1;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.225528;
		lfProb += initSimParam.lfGetInjuryAISNumber2();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			iNumTrauma = 2;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.140907571;
		lfProb += initSimParam.lfGetInjuryAISNumber3();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			iNumTrauma = 3;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.065361013;
		lfProb += initSimParam.lfGetInjuryAISNumber4();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			iNumTrauma = 4;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.025350891;
		lfProb += initSimParam.lfGetInjuryAISNumber5();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			iNumTrauma = 5;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.007049446;
		lfProb += initSimParam.lfGetInjuryAISNumber6();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			iNumTrauma = 6;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.001120681;
		lfProb += initSimParam.lfGetInjuryAISNumber7();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			iNumTrauma = 7;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.0000271133;
		lfProb += initSimParam.lfGetInjuryAISNumber8();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			iNumTrauma = 8;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.00000903775;
		lfProb += initSimParam.lfGetInjuryAISNumber9();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
		if( lfPrevProb <= lfRand )
		{
			iNumTrauma = 9;
		}
		return iNumTrauma;

	}

	/**
	 * <PRE>
	 *   患者エージェントの部位別外傷割合を生成します。
	 * </PRE>
	 * @return 外傷ヶ所数
	 */
	int iInjuryAISPartAnatomy()
	{
		int iPartTrauma = 0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

		if( iInjuryRandomMode == 0 )
		{
			lfRand = rnd.NextUnif();
		}
		else if( iInjuryRandomMode == 1 )
		{
			lfRand = normalRand();
		}
		else if( iInjuryRandomMode == 2 )
		{
			lfRand = rnd.NextUnif();
//			lfRand	= weibullRand( initSimParam.lfGetInjuryAISPartAnatomyWeibullAlpha(), initSimParam.lfGetInjuryAISPartAnatomyWeibullBeta() );
		}

		lfPrevProb = 0.0;
//		lfProb = 0.211858767;
		lfProb = initSimParam.lfGetInjuryAISPartAnatomyHead();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// 頭部外傷の場合
			iPartTrauma = 1;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.08642084;
		lfProb += initSimParam.lfGetInjuryAISPartAnatomyFace();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// 顔面外傷の場合
			iPartTrauma = 2;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.007814182;
		lfProb += initSimParam.lfGetInjuryAISPartAnatomyNeck();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// 頸部外傷の場合
			iPartTrauma = 3;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.145592351;
		lfProb += initSimParam.lfGetInjuryAISPartAnatomyThorax();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// 胸部外傷の場合
			iPartTrauma = 4;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.06012076;
		lfProb += initSimParam.lfGetInjuryAISPartAnatomyAbdomen();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// 腹部及び骨盤内臓器
			iPartTrauma = 5;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.094156734;
		lfProb += initSimParam.lfGetInjuryAISPartAnatomySpine();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// 脊椎外傷の場合
			iPartTrauma = 6;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.125022019;
		lfProb += initSimParam.lfGetInjuryAISPartAnatomyUpperExtremity();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// 上肢外傷の場合
			iPartTrauma = 7;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.232047443;
		lfProb += initSimParam.lfGetInjuryAISPartAnatomyLowerExtremity();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// 下肢外傷の場合
			iPartTrauma = 8;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.036966903;
		lfProb += initSimParam.lfGetInjuryAISPartAnatomyUnspecified();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
		if( lfPrevProb <= lfRand )
		{
			// 体表・熱傷・その他外傷の場合
			iPartTrauma = 9;
		}
		return iPartTrauma;
	}

	/**
	 * <PRE>
	 *    患者の部位別AIS重症度の発生割合を算出します。
	 * </PRE>
	 * @param iPartAnatomy AIS損傷区分に基づく損傷部位
	 */
	void vAISAnatomySeverity( int iPartAnatomy )
	{
		// 頭部
		if( iPartAnatomy == 1 )
		{
			lfInternalAISHead = lfInternalAISHeadSeverity();
		}
		// 顔面
		else if( iPartAnatomy ==  2 )
		{
			lfInternalAISFace = lfInternalAISFaceSeverity();
		}
		// 頸部
		else if( iPartAnatomy == 3 )
		{
			lfInternalAISNeck = lfInternalAISNeckSeverity();
		}
		// 胸部
		else if( iPartAnatomy == 4 )
		{
			lfInternalAISThorax = lfInternalAISThoraxSeverity();
		}
		// 腹部
		else if( iPartAnatomy == 5 )
		{
			lfInternalAISAbdomen = lfInternalAISAbdomenSeverity();
		}
		// 脊椎
		else if( iPartAnatomy == 6 )
		{
			lfInternalAISSpine = lfInternalAISSpineSeverity();
		}
		// 上肢
		else if( iPartAnatomy == 7 )
		{
			lfInternalAISUpperExtremity = lfInternalAISUpperExtremitySeverity();
		}
		// 下肢
		else if( iPartAnatomy == 8 )
		{
			lfInternalAISLowerExtremity = lfInternalAISLowerExtremitySeverity();
		}
		// 表面・熱傷・その他外傷
		else if( iPartAnatomy == 9 )
		{
			lfInternalAISUnspecified = lfInternalAISUnspecifiedSeverity();
		}
		else
		{
		}
	}

	/**
	 * <PRE>
	 *   頭部AIS重症度の発生割合を算出します。
	 * </PRE>
	 * @return 頭部AIS重症度
	 */
	double lfInternalAISHeadSeverity()
	{
		int iPartTrauma = 0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

		if( iInjuryRandomMode == 0 )
		{
			lfRand = rnd.NextUnif();
		}
		else if( iInjuryRandomMode == 1 )
		{
			lfRand = normalRand();
		}
		else if( iInjuryRandomMode == 2 )
		{
//			lfRand	= weibullRand( 1.0, 0.05 );
//			lfRand	= weibullRand( 1.0, 0.12 );		// 現在設定値
//			lfRand	= weibullRand( 1.0, 0.375 );
//			lfRand	= weibullRand( 1.0, 0.250 );
//			lfRand = 0.5*normalRand()+0.5;
			lfRand	= weibullRand( initSimParam.lfGetInternalAISHeadSeverityWeibullAlpha(), initSimParam.lfGetInternalAISHeadSeverityWeibullBeta() );
		}

		lfPrevProb = 0.0;
//		lfProb = 0.12540995;
		lfProb = initSimParam.lfGetInternalAISHeadSeverity1();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 1の場合
			iPartTrauma = 1;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.101690609;
		lfProb += initSimParam.lfGetInternalAISHeadSeverity2();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 2の場合
			iPartTrauma = 2;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.290336736;
		lfProb += initSimParam.lfGetInternalAISHeadSeverity3();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 3の場合
			iPartTrauma = 3;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.29243845;
		lfProb += initSimParam.lfGetInternalAISHeadSeverity4();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 4の場合
			iPartTrauma = 4;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.176105132;
		lfProb += initSimParam.lfGetInternalAISHeadSeverity5();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 5の場合
			iPartTrauma = 5;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.010046653;
		lfProb += initSimParam.lfGetInternalAISHeadSeverity6();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
		if( lfPrevProb <= lfRand )
		{
			// AIS 6の場合
			iPartTrauma = 6;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.00397247;
//		lfProb += initSimParam.lfGetInternalAISHeadSeverity7();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 7の場合
//			iPartTrauma = 7;
//		}

		return iPartTrauma;
	}

	/**
	 * <PRE>
	 *   顔面AIS重症度の発生割合を算出します。
	 * </PRE>
	 * @return 顔面AIS重症度
	 */
	double lfInternalAISFaceSeverity()
	{
		int iPartTrauma = 0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

		if( iInjuryRandomMode == 0 )
		{
			lfRand = rnd.NextUnif();
		}
		else if( iInjuryRandomMode == 1 )
		{
			lfRand = normalRand();
		}
		else if( iInjuryRandomMode == 2 )
		{
//			lfRand	= weibullRand( 1.0, 0.05 );
//			lfRand	= weibullRand( 1.0, 0.12 );		//現在の設定値
			lfRand	= weibullRand( initSimParam.lfGetInternalAISFaceSeverityWeibullAlpha(), initSimParam.lfGetInternalAISFaceSeverityWeibullBeta() );
		}

		lfPrevProb = 0.0;
//		lfProb = 0.574000679;
		lfProb = initSimParam.lfGetInternalAISFaceSeverity1();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 1の場合
			iPartTrauma = 1;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.37255124;
		lfProb += initSimParam.lfGetInternalAISFaceSeverity2();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 2の場合
			iPartTrauma = 2;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.046031027;
		lfProb += initSimParam.lfGetInternalAISFaceSeverity3();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 3の場合
			iPartTrauma = 3;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.00577511;
		lfProb += initSimParam.lfGetInternalAISFaceSeverity4();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 4の場合
			iPartTrauma = 4;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.0;
		lfProb += initSimParam.lfGetInternalAISFaceSeverity5();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 5の場合
			iPartTrauma = 5;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.0;
		lfProb += initSimParam.lfGetInternalAISFaceSeverity6();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
		if( lfPrevProb <= lfRand )
		{
			// AIS 6の場合
			iPartTrauma = 6;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.001641943;
//		lfProb += initSimParam.lfGetInternalAISFaceSeverity7();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 7の場合
//			iPartTrauma = 7;
//		}
		return iPartTrauma;
	}

	/**
	 * <PRE>
	 *   頸部AIS重症度の発生割合を算出します。
	 * </PRE>
	 * @return 頸部AIS重症度
	 */
	double lfInternalAISNeckSeverity()
	{
		int iPartTrauma = 0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

		if( iInjuryRandomMode == 0 )
		{
			lfRand = rnd.NextUnif();
		}
		else if( iInjuryRandomMode == 1 )
		{
			lfRand = normalRand();
		}
		else if( iInjuryRandomMode == 2 )
		{
//			lfRand	= weibullRand( 1.0, 0.05 );
//			lfRand	= weibullRand( 1.0, 0.12 );		// 現在設定値
			lfRand	= weibullRand( initSimParam.lfGetInternalAISNeckSeverityWeibullAlpha(), initSimParam.lfGetInternalAISNeckSeverityWeibullBeta() );
		}

		lfPrevProb = 0.0;
//		lfProb = 0.444583594;
		lfProb = initSimParam.lfGetInternalAISNeckSeverity1();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 1の場合
			iPartTrauma = 1;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.170319349;
		lfProb += initSimParam.lfGetInternalAISNeckSeverity2();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 2の場合
			iPartTrauma = 2;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.221665623;
		lfProb += initSimParam.lfGetInternalAISNeckSeverity3();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 3の場合
			iPartTrauma = 3;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.041953663;
		lfProb += initSimParam.lfGetInternalAISNeckSeverity4();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 4の場合
			iPartTrauma = 4;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.023168441;
		lfProb += initSimParam.lfGetInternalAISNeckSeverity5();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 5の場合
			iPartTrauma = 5;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.000626174;
		lfProb += initSimParam.lfGetInternalAISNeckSeverity6();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
		if( lfPrevProb <= lfRand )
		{
			// AIS 6の場合
			iPartTrauma = 6;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.097683156;
//		lfProb += initSimParam.lfGetInternalAISNeckSeverity7();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 7の場合
//			iPartTrauma = 7;
//		}
		return iPartTrauma;
	}

	/**
	 * <PRE>
	 *   胸部AIS重症度の発生割合を算出します。
	 * </PRE>
	 * @return 胸部AIS重症度
	 */
	double lfInternalAISThoraxSeverity()
	{
		int iPartTrauma = 0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

		if( iInjuryRandomMode == 0 )
		{
			lfRand = rnd.NextUnif();
		}
		else if( iInjuryRandomMode == 1 )
		{
			lfRand = normalRand();
		}
		else if( iInjuryRandomMode == 2 )
		{
//			lfRand	= weibullRand( 1.0, 0.05 );
//			lfRand	= weibullRand( 1.0, 0.12 );			// 現在設定値
//			lfRand	= weibullRand( 1.0, 0.375 );
			lfRand	= weibullRand( initSimParam.lfGetInternalAISThoraxSeverityWeibullAlpha(), initSimParam.lfGetInternalAISThoraxSeverityWeibullBeta() );
		}

		lfPrevProb = 0.0;
//		lfProb = 0.08801882;
		lfProb = initSimParam.lfGetInternalAISThoraxSeverity1();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 1の場合
			iPartTrauma = 1;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.08435557;
		lfProb += initSimParam.lfGetInternalAISThoraxSeverity2();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 2の場合
			iPartTrauma = 2;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.402621408;
		lfProb += initSimParam.lfGetInternalAISThoraxSeverity3();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 3の場合
			iPartTrauma = 3;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.30159637;
		lfProb += initSimParam.lfGetInternalAISThoraxSeverity4();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 4の場合
			iPartTrauma = 4;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.100420097;
		lfProb += initSimParam.lfGetInternalAISThoraxSeverity5();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 5の場合
			iPartTrauma = 5;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.014518568;
		lfProb += initSimParam.lfGetInternalAISThoraxSeverity6();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
		if( lfPrevProb <= lfRand )
		{
			// AIS 6の場合
			iPartTrauma = 6;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.008469165;
//		lfProb += initSimParam.lfGetInternalAISThoraxSeverity7();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 7の場合
//			iPartTrauma = 7;
//		}

//	// 新アルゴリズム方式
//
//		lfPrevProb = 0.0;
//		lfProb = 0.66063043;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 1の場合
//			iPartTrauma = 1;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.275518273;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 2の場合
//			iPartTrauma = 2;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.028882121;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 3の場合
//			iPartTrauma = 3;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.021635071;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 4の場合
//			iPartTrauma = 4;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.007203654;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 5の場合
//			iPartTrauma = 5;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.00613045;
////		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		if( lfPrevProb <= lfRand )
//		{
//			// AIS 6の場合
//			iPartTrauma = 6;
//		}
//		lfPrevProb = lfProb;
////		lfProb += 0.008469165;
////		if( lfPrevProb <= lfRand && lfRand < lfProb )
////		{
////			// AIS 7の場合
////			iPartTrauma = 7;
////		}

		return iPartTrauma;
	}

	/**
	 * <PRE>
	 *   腹部及び骨盤内蔵器AIS重症度の発生割合を算出します。
	 * </PRE>
	 * @return 腹部及び骨盤内蔵器AIS重症度
	 */
	double lfInternalAISAbdomenSeverity()
	{
		int iPartTrauma = 0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

		if( iInjuryRandomMode == 0 )
		{
			lfRand = rnd.NextUnif();
		}
		else if( iInjuryRandomMode == 1 )
		{
			lfRand = normalRand();
		}
		else if( iInjuryRandomMode == 2 )
		{
//			lfRand	= weibullRand( 1.0, 0.05 );
//			lfRand	= weibullRand( 1.0, 0.12 );		// 現在設定値
			lfRand	= weibullRand( initSimParam.lfGetInternalAISAbdomenSeverityWeibullAlpha(), initSimParam.lfGetInternalAISAbdomenSeverityWeibullBeta() );
		}

		lfPrevProb = 0.0;
//		lfProb = 0.1235307;
		lfProb = initSimParam.lfGetInternalAISAbdomenSeverity1();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 1の場合
			iPartTrauma = 1;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.311747098;
		lfProb += initSimParam.lfGetInternalAISAbdomenSeverity2();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 2の場合
			iPartTrauma = 2;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.031075803;
		lfProb += initSimParam.lfGetInternalAISAbdomenSeverity3();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 3の場合
			iPartTrauma = 3;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.013322522;
		lfProb += initSimParam.lfGetInternalAISAbdomenSeverity4();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 4の場合
			iPartTrauma = 4;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.013322522;
		lfProb += initSimParam.lfGetInternalAISAbdomenSeverity5();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 5の場合
			iPartTrauma = 5;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.00613273;
		lfProb += initSimParam.lfGetInternalAISAbdomenSeverity6();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
		if( lfPrevProb <= lfRand )
		{
			// AIS 6の場合
			iPartTrauma = 6;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.0;
//		lfProb += initSimParam.lfGetInternalAISAbdomenSeverity7();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 7の場合
//			iPartTrauma = 7;
//		}


//	// 新アルゴリズム方式
//
//		lfPrevProb = 0.0;
//		lfProb = 0.66063043;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 1の場合
//			iPartTrauma = 1;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.275518273;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 2の場合
//			iPartTrauma = 2;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.028882121;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 3の場合
//			iPartTrauma = 3;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.021635071;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 4の場合
//			iPartTrauma = 4;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.007203654;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 5の場合
//			iPartTrauma = 5;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.00613045;
////		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		if( lfPrevProb <= lfRand )
//		{
//			// AIS 6の場合
//			iPartTrauma = 6;
//		}
//		lfPrevProb = lfProb;
////		lfProb += 0.008469165;
////		if( lfPrevProb <= lfRand && lfRand < lfProb )
////		{
////			// AIS 7の場合
////			iPartTrauma = 7;
////		}

		return iPartTrauma;
	}

	/**
	 * <PRE>
	 *   脊椎AIS重症度の発生割合を算出します。
	 * </PRE>
	 * @return 脊椎AIS重症度
	 */
	double lfInternalAISSpineSeverity()
	{
		int iPartTrauma = 0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

		if( iInjuryRandomMode == 0 )
		{
			lfRand = rnd.NextUnif();
		}
		else if( iInjuryRandomMode == 1 )
		{
			lfRand = normalRand();
		}
		else if( iInjuryRandomMode == 2 )
		{
//			lfRand	= weibullRand( 1.0, 0.05 );
//			lfRand	= weibullRand( 1.0, 0.12 );		// 現在設定値
			lfRand	= weibullRand( initSimParam.lfGetInternalAISSpineSeverityWeibullAlpha(), initSimParam.lfGetInternalAISSpineSeverityWeibullBeta() );
		}

		lfPrevProb = 0.0;
//		lfProb = 0.059034454;
		lfProb = initSimParam.lfGetInternalAISSpineSeverity1();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 1の場合
			iPartTrauma = 1;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.431013875;
		lfProb += initSimParam.lfGetInternalAISSpineSeverity2();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 2の場合
			iPartTrauma = 2;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.264667671;
		lfProb += initSimParam.lfGetInternalAISSpineSeverity3();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 3の場合
			iPartTrauma = 3;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.167957179;
		lfProb += initSimParam.lfGetInternalAISSpineSeverity4();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 4の場合
			iPartTrauma = 4;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.056799875;
		lfProb += initSimParam.lfGetInternalAISSpineSeverity5();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 5の場合
			iPartTrauma = 5;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.016213688;
		lfProb += initSimParam.lfGetInternalAISSpineSeverity6();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
		if( lfPrevProb <= lfRand )
		{
			// AIS 6の場合
			iPartTrauma = 6;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.004313257;
//		lfProb += initSimParam.lfGetInternalAISSpineSeverity7();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 7の場合
//			iPartTrauma = 7;
//		}


//	// 新アルゴリズム方式
//
//		lfPrevProb = 0.0;
//		lfProb = 0.66063043;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 1の場合
//			iPartTrauma = 1;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.275518273;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 2の場合
//			iPartTrauma = 2;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.031213875;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 3の場合
//			iPartTrauma = 3;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.019808216;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 4の場合
//			iPartTrauma = 4;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.006698756;
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 5の場合
//			iPartTrauma = 5;
//		}
//		lfPrevProb = lfProb;
//		lfProb += 0.00613045;
////		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		if( lfPrevProb <= lfRand )
//		{
//			// AIS 6の場合
//			iPartTrauma = 6;
//		}
//		lfPrevProb = lfProb;
////		lfProb += 0.008469165;
////		if( lfPrevProb <= lfRand && lfRand < lfProb )
////		{
////			// AIS 7の場合
////			iPartTrauma = 7;
////		}

		return iPartTrauma;
	}

	/**
	 * <PRE>
	 *   上肢AIS重症度の発生割合を算出します。
	 * </PRE>
	 * @return 上肢AIS重症度
	 */
	double lfInternalAISUpperExtremitySeverity()
	{
		int iPartTrauma = 0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

		if( iInjuryRandomMode == 0 )
		{
			lfRand = rnd.NextUnif();
		}
		else if( iInjuryRandomMode == 1 )
		{
			lfRand = normalRand();
		}
		else if( iInjuryRandomMode == 2 )
		{
//			lfRand	= weibullRand( 1.0, 0.05 );
//			lfRand	= weibullRand( 1.0, 0.12 );		// 現在設定値
			lfRand	= weibullRand( initSimParam.lfGetInternalAISUpperExtremitySeverityWeibullAlpha(), initSimParam.lfGetInternalAISUpperExtremitySeverityWeibullBeta() );
		}

		lfPrevProb = 0.0;
//		lfProb = 0.249148761;
		lfProb = initSimParam.lfGetInternalAISUpperExtremitySeverity1();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 1の場合
			iPartTrauma = 1;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.515087472;
		lfProb += initSimParam.lfGetInternalAISUpperExtremitySeverity2();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 2の場合
			iPartTrauma = 2;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.235489805;
		lfProb += initSimParam.lfGetInternalAISUpperExtremitySeverity3();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 3の場合
			iPartTrauma = 3;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.0;
		lfProb += initSimParam.lfGetInternalAISUpperExtremitySeverity4();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 4の場合
			iPartTrauma = 4;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.0;
		lfProb += initSimParam.lfGetInternalAISUpperExtremitySeverity5();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 5の場合
			iPartTrauma = 5;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.0;
		lfProb += initSimParam.lfGetInternalAISUpperExtremitySeverity6();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
		if( lfPrevProb <= lfRand )
		{
			// AIS 6の場合
			iPartTrauma = 6;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.000273962;
//		lfProb += initSimParam.lfGetInternalAISUpperExtremitySeverity7();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 7の場合
//			iPartTrauma = 7;
//		}


//		// 新アルゴリズム方式
//
//			lfPrevProb = 0.0;
//			lfProb = 0.66063043;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 1の場合
//				iPartTrauma = 1;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.275518273;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 2の場合
//				iPartTrauma = 2;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.057720847;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 3の場合
//				iPartTrauma = 3;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.0;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 4の場合
//				iPartTrauma = 4;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.0;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 5の場合
//				iPartTrauma = 5;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.0;
////			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			if( lfPrevProb <= lfRand )
//			{
//				// AIS 6の場合
//				iPartTrauma = 6;
//			}
//			lfPrevProb = lfProb;
////			lfProb += 0.008469165;
////			if( lfPrevProb <= lfRand && lfRand < lfProb )
////			{
////				// AIS 7の場合
////				iPartTrauma = 7;
////			}

			return iPartTrauma;
	}

	/**
	 * <PRE>
	 *   下肢AIS重症度の発生割合を算出します。
	 * </PRE>
	 * @return 下肢AIS重症度
	 */
	double lfInternalAISLowerExtremitySeverity()
	{
		int iPartTrauma = 0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

		if( iInjuryRandomMode == 0 )
		{
			lfRand = rnd.NextUnif();
		}
		else if( iInjuryRandomMode == 1 )
		{
			lfRand = normalRand();
		}
		else if( iInjuryRandomMode == 2 )
		{
//			lfRand	= weibullRand( 1.0, 0.05 );
//			lfRand	= weibullRand( 1.0, 0.12 );		// 現在設定値
			lfRand	= weibullRand( initSimParam.lfGetInternalAISLowerExtremitySeverityWeibullAlpha(), initSimParam.lfGetInternalAISLowerExtremitySeverityWeibullBeta() );
		}

		lfPrevProb = 0.0;
//		lfProb = 0.122111167;
		lfProb = initSimParam.lfGetInternalAISLowerExtremitySeverity1();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 1の場合
			iPartTrauma = 1;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.205613192;
		lfProb += initSimParam.lfGetInternalAISLowerExtremitySeverity2();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 2の場合
			iPartTrauma = 2;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.591915486;
		lfProb += initSimParam.lfGetInternalAISLowerExtremitySeverity3();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 3の場合
			iPartTrauma = 3;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.043269231;
		lfProb += initSimParam.lfGetInternalAISLowerExtremitySeverity4();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 4の場合
			iPartTrauma = 4;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.036795715;
		lfProb += initSimParam.lfGetInternalAISLowerExtremitySeverity5();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 5の場合
			iPartTrauma = 5;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.00613273;
		lfProb += initSimParam.lfGetInternalAISLowerExtremitySeverity6();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
		if( lfPrevProb <= lfRand )
		{
			// AIS 6の場合
			iPartTrauma = 6;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.000295209;
//		lfProb += initSimParam.lfGetInternalAISLowerExtremitySeverity7();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 7の場合
//			iPartTrauma = 7;
//		}



//		// 新アルゴリズム方式
//
//			lfPrevProb = 0.0;
//			lfProb = 0.66063043;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 1の場合
//				iPartTrauma = 1;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.275518273;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 2の場合
//				iPartTrauma = 2;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.050843539;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 3の場合
//				iPartTrauma = 3;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.003716681;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 4の場合
//				iPartTrauma = 4;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.003160628;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 5の場合
//				iPartTrauma = 5;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.00613045;
////			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			if( lfPrevProb <= lfRand )
//			{
//				// AIS 6の場合
//				iPartTrauma = 6;
//			}
//			lfPrevProb = lfProb;
////			lfProb += 0.008469165;
////			if( lfPrevProb <= lfRand && lfRand < lfProb )
////			{
////				// AIS 7の場合
////				iPartTrauma = 7;
////			}

			return iPartTrauma;
	}

	/**
	 * <PRE>
	 *   体表・熱傷・その他外傷AIS重症度の発生割合を算出します。
	 * </PRE>
	 * @return 体表・熱傷・その他外傷AIS重症度
	 */
	double lfInternalAISUnspecifiedSeverity()
	{
		int iPartTrauma = 0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

		if( iInjuryRandomMode == 0 )
		{
			lfRand = rnd.NextUnif();
		}
		else if( iInjuryRandomMode == 1 )
		{
			lfRand = normalRand();
		}
		else if( iInjuryRandomMode == 2 )
		{
//			lfRand	= weibullRand( 1.0, 0.05 );
//			lfRand	= weibullRand( 1.0, 0.12 );		// 現在設定値
			lfRand	= weibullRand( initSimParam.lfGetInternalAISUnspecifiedSeverityWeibullAlpha(), initSimParam.lfGetInternalAISUnspecifiedSeverityWeibullBeta() );
		}

		lfPrevProb = 0.0;
//		lfProb = 0.686300463;
		lfProb = initSimParam.lfGetInternalAISUnspecifiedSeverity1();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 1の場合
			iPartTrauma = 1;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.072931833;
		lfProb += initSimParam.lfGetInternalAISUnspecifiedSeverity2();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 2の場合
			iPartTrauma = 2;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.100198544;
		lfProb += initSimParam.lfGetInternalAISUnspecifiedSeverity3();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 3の場合
			iPartTrauma = 3;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.047782925;
		lfProb += initSimParam.lfGetInternalAISUnspecifiedSeverity4();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 4の場合
			iPartTrauma = 4;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.081535407;
		lfProb += initSimParam.lfGetInternalAISUnspecifiedSeverity5();
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// AIS 5の場合
			iPartTrauma = 5;
		}
		lfPrevProb = lfProb;
//		lfProb += 0.011250827;
		lfProb += initSimParam.lfGetInternalAISUnspecifiedSeverity6();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
		if( lfPrevProb <= lfRand )
		{
			// AIS 6の場合
			iPartTrauma = 6;
		}
		lfPrevProb = lfProb;
//		lfProb += 0;
//		lfProb += initSimParam.lfGetInternalAISUnspecifiedSeverity7();
//		if( lfPrevProb <= lfRand && lfRand < lfProb )
//		{
//			// AIS 7の場合
//			iPartTrauma = 7;
//		}


//		// 新アルゴリズム方式
//
//			lfPrevProb = 0.0;
//			lfProb = 0.66063043;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 1の場合
//				iPartTrauma = 1;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.275518273;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 2の場合
//				iPartTrauma = 2;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.025198778;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 3の場合
//				iPartTrauma = 3;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.012016855;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 4の場合
//				iPartTrauma = 4;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.020505214;
//			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			{
//				// AIS 5の場合
//				iPartTrauma = 5;
//			}
//			lfPrevProb = lfProb;
//			lfProb += 0.00613045;
////			if( lfPrevProb <= lfRand && lfRand < lfProb )
//			if( lfPrevProb <= lfRand )
//			{
//				// AIS 6の場合
//				iPartTrauma = 6;
//			}
//			lfPrevProb = lfProb;
////			lfProb += 0.008469165;
////			if( lfPrevProb <= lfRand && lfRand < lfProb )
////			{
////				// AIS 7の場合
////				iPartTrauma = 7;
////			}

			return iPartTrauma;

	}

	/**
	 * <PRE>
	 *    死亡率（時間経過ごと）を算出します。
	 * </PRE>
	 * @param lfTimeCourseData 経過時間［日］
	 * @return 死亡率
	 */
	double lfCalcDeathRateTimeCourse( double lfTimeCourseData )
	{
		double lfDeathRateTimeCourse = 0.0;
		// 統計データから近似曲線を算出
		lfDeathRateTimeCourse = 0.0004068/Math.pow(lfTimeCourseData+0.07, 1.25);
//		lfDeathRateTimeCourse = 0.009068/Math.pow(lfTimeCourseData+0.05, 1.1);
//		lfOneDayTimeDeathRate			= 0.999999;
		lfOneDayTimeDeathRate			= 0.999999999;
//		lfPrevTimeDeathRate				= 0.0;
//		if( lfTimeCourseData == 0.0 )
//		{
////			lfDeathRateTimeCourse = lfPrevTimeDeathRate = lfOneDayTimeDeathRate;
//			lfDeathRateTimeCourse = 0.112920749*0.547277;
////			lfDeathRateTimeCourse = 0.112920749*0.547277*1.0/86400000000.0;
//		}
//		else if( lfTimeCourseData <= 1.0 )
//		{
////			lfDeathRateTimeCourse = lfPrevTimeDeathRate*0.999999999925;
////			lfDeathRateTimeCourse = lfPrevTimeDeathRate*0.999999925;
////			lfDeathRateTimeCourse = 0.112920749*0.547277;
////			lfDeathRateTimeCourse = 0.112920749*0.547277*1.0/86400000000.0;
//			lfPrevTimeDeathRate = lfDeathRateTimeCourse;
//		}
//		else if( 1.0 < lfTimeCourseData && lfTimeCourseData <= 7.0 )
//		{
////			lfDeathRateTimeCourse = lfPrevTimeDeathRate*0.999999961;
//			lfDeathRateTimeCourse = 0.112920749*0.251938;
////			lfDeathRateTimeCourse = 0.112920749*0.251938*1.0/(7*86400.0);
//			lfPrevTimeDeathRate = lfDeathRateTimeCourse;
//		}
//		else if( 7.0 < lfTimeCourseData && lfTimeCourseData <= 28.0 )
//		{
////			lfDeathRateTimeCourse = lfPrevTimeDeathRate*0.999999994;
//			lfDeathRateTimeCourse = 0.112920749*0.131985;
////			lfDeathRateTimeCourse = 0.112920749*0.131985*1.0/(21*86400.0);
//			lfPrevTimeDeathRate = lfDeathRateTimeCourse;
//		}
//		else if( 28.0 < lfTimeCourseData )
//		{
////			lfDeathRateTimeCourse = lfPrevTimeDeathRate*0.9999999994;
//			lfDeathRateTimeCourse = 0.112920749*0.0688;
////			lfDeathRateTimeCourse = 0.112920749*0.0688*1.0/(30*86400.0);
//			lfPrevTimeDeathRate = lfDeathRateTimeCourse;
//		}

		return lfDeathRateTimeCourse;
	}

	/**
	 * <PRE>
	 *   死亡率（性別及び年齢）を算出します。
	 * </PRE>
	 * @param iSexFlag 性別
	 * @param lfDataAge 年齢
	 * @return 死亡率（性別年齢）
	 */
	private double lfCalcDeathRateMSexYear( int iSexFlag, double lfDataAge )
	{
		double lfManAge = 0.0;
		double lfFemaleAge = 0.0;
		double lfDeathRateSexYear = 0.0;
		if( iSexFlag == 0 )
		{
			lfManAge = lfDataAge;
			// 統計データから性別ごとに近似曲線（男性）を算出
			lfDeathRateSexYear = 0.025*Math.exp( 0.0152*lfManAge );
		}
		else
		{
			lfFemaleAge = lfDataAge;
			// 統計データから性別ごとに近似曲線（女性）を算出
			lfDeathRateSexYear = -0.00002*lfFemaleAge*lfFemaleAge+0.0024*lfFemaleAge+0.0096;
		}
		return lfDeathRateSexYear;
	}

	/**
	 * <PRE>
	 *   患者エージェントのバイタルサインを生成します。
	 *   2013年の論文より
	 * </PRE>
	 */
	public void vGenerateVitalSign()
	{
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;
		double lfCurProb = 0.0;
		double lfTemp1 = 0.0;
		double lfTemp2 = 0.0;

		// バイタルサインの設定をします。
		// メルセンヌツイスターの正規乱数は最大8.57σ
		lfSbp						 = 10*normalRand() + 120.0;				// 収縮期血圧
		lfDbp						 = 15*normalRand() + 75.0;				// 拡張期血圧
		lfRr						 = normalRand() + 17.5;					// 呼吸速度
		lfPulse						 = 5*normalRand() + 70.0;				// 脈拍
//		lfBodyTemperature			 = normalRand() + 36.0;					// 体温
		lfBodyTemperature			 = normalRand() + 36.5;					// 体温
		lfAge						 = normalRand() + 35.0;					// 患者の年齢
		iSex						 = rnd.NextUnif() <= 0.5 ? 0 : 1;		// 患者の性別(0:男性、1:女性)
//		lfSpO2						 = 0.04*weibullRand( 5.5, 2 )+0.96;		// 飽和酸素濃度
		lfSpO2						 = 0.02*normalRand()+0.98;				// 飽和酸素濃度
		lfLeucocyte					 = 3150*normalRand()+6650;				// 白血球数
//		lfLeucocyte					 = 1575*normalRand()+6650;				// 白血球数

		if( iInjuryRandomMode == 0 )
		{
			lfCurProb = rnd.NextUnif();
		}
		else if( iInjuryRandomMode == 1 )
		{
			lfCurProb = normalRand();
		}
		else if( iInjuryRandomMode == 2 )
		{
			lfCurProb	= weibullRand( 1.0, 0.1 );
		}
		//
		if( lfInternalAISHead >= 3 || lfInternalAISThorax >= 3 || lfInternalAISAbdomen >= 3 ||
			lfInternalAISSpine >= 3 || lfInternalAISLowerExtremity >= 3 || lfInternalAISUpperExtremity >= 3 )
		{
			lfPrevProb = 0.0;
			lfProb = 0.297;
			if( lfPrevProb <= lfCurProb && lfCurProb < lfProb )
			{
				if( iInjuryRandomMode == 0 )
				{
					lfTemp1	= 148.4 + ( 2*rnd.NextUnif() - 1 )*25.6;
					lfTemp2	= 148.4 + ( 2*rnd.NextUnif() - 1 )*25.6;
				}
				else if( iInjuryRandomMode == 1 )
				{
					lfRand = normalRand();
					lfTemp1	= 148.4 + lfRand*25.6;
					lfRand = normalRand();
					lfTemp2	= 148.4 + lfRand*25.6;
				}
				else if( iInjuryRandomMode == 2 )
				{
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand	= normalRand();
					lfTemp1	= 148.4 + lfRand*25.6;
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand	= normalRand();
					lfTemp2	= 148.4 + lfRand*25.6;
//					lfTemp1	= 148.4 + ( 2*rnd.NextUnif() - 1 )*25.6;
//					lfTemp2	= 148.4 + ( 2*rnd.NextUnif() - 1 )*25.6;
				}
				lfSbp = lfTemp1 > lfTemp2 ? lfTemp1 : lfTemp2;
				lfDbp = lfTemp1 < lfTemp2 ? lfTemp1 : lfTemp2;

				if( iInjuryRandomMode == 0 )
				{
					lfPulse =  73.7 + ( 2*rnd.NextUnif() - 1 )*13.6;
					lfRr	=  17.0 + ( 2*rnd.NextUnif() - 1 )*3.0;
				}
				else if( iInjuryRandomMode == 1 )
				{
					lfRand = normalRand();
					lfPulse =  73.7 + lfRand*13.6;
					lfRand = normalRand();
					lfRr	=  17.0 + lfRand*3.0;
				}
				else if( iInjuryRandomMode == 2 )
				{
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand = normalRand();
					lfPulse =  73.7 + lfRand*13.6;
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand = normalRand();
					lfRr	=  17.0 + lfRand*3.0;
//					lfPulse =  73.7 + ( 2*rnd.NextUnif() - 1 )*13.6;
//					lfRr	=  17.0 + ( 2*rnd.NextUnif() - 1 )*3.0;
				}
			}
			lfPrevProb = lfProb;
			lfProb += 0.554;
			if( lfPrevProb <= lfCurProb && lfCurProb < lfProb )
			{
				if( iInjuryRandomMode == 0 )
				{
					lfTemp1	= 124.0 + ( 2*rnd.NextUnif() - 1 )*20.2;
					lfTemp2	= 124.0 + ( 2*rnd.NextUnif() - 1 )*20.0;
				}
				else if( iInjuryRandomMode == 1 )
				{
					lfRand = normalRand();
					lfTemp1	= 124.0 + lfRand*20.2;
					lfRand = normalRand();
					lfTemp2	= 124.0 + lfRand*20.0;
				}
				else if( iInjuryRandomMode == 2 )
				{
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand	= normalRand();
					lfTemp1	= 124.0 + lfRand*20.2;
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand	= normalRand();
					lfTemp2	= 124.0 + lfRand*20.0;
//					lfTemp1	= 124.0 + ( 2*rnd.NextUnif() - 1 )*20.2;
//					lfTemp2	= 124.0 + ( 2*rnd.NextUnif() - 1 )*20.0;
				}
				lfSbp = lfTemp1 > lfTemp2 ? lfTemp1 : lfTemp2;
				lfDbp = lfTemp1 < lfTemp2 ? lfTemp1 : lfTemp2;
				if( iInjuryRandomMode == 0 )
				{
					lfPulse =  91.3 + ( 2*rnd.NextUnif() - 1 )*15.1;
					lfRr	=  17.0 + ( 2*rnd.NextUnif() - 1 )*3.0;
				}
				else if( iInjuryRandomMode == 1 )
				{
					lfRand = normalRand();
					lfPulse =  91.3 + lfRand*15.1;
					lfRand = normalRand();
					lfRr	=  17.0 + lfRand*3.0;
				}
				else if( iInjuryRandomMode == 2 )
				{
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand = normalRand();
					lfPulse =  91.3 + lfRand*15.1;
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand = normalRand();
					lfRr	=  25.0 + lfRand*5.0;
//					lfPulse =  91.3 + ( 2*rnd.NextUnif() - 1 )*15.1;
//					lfRr	=  17.0 + ( 2*rnd.NextUnif() - 1 )*3.0;
				}
			}
			lfPrevProb = lfProb;
			lfProb += 0.104;
			if( lfPrevProb <= lfCurProb && lfCurProb < lfProb )
			{
				if( iInjuryRandomMode == 0 )
				{
					lfTemp1	=  96.9 + ( 2*rnd.NextUnif() - 1 )*16.8;
					lfTemp2	=  96.9 + ( 2*rnd.NextUnif() - 1 )*16.8;
				}
				else if( iInjuryRandomMode == 1 )
				{
					lfRand = normalRand();
					lfTemp1	=  96.9 + lfRand*16.8;
					lfRand = normalRand();
					lfTemp2	=  96.9 + lfRand*16.8;
				}
				else if( iInjuryRandomMode == 2 )
				{
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand	= normalRand();
					lfTemp1	=  96.9 + lfRand*16.8;
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand	= normalRand();
					lfTemp2	=  96.9 + lfRand*16.8;
//					lfTemp1	=  96.9 + ( 2*rnd.NextUnif() - 1 )*16.8;
//					lfTemp2	=  96.9 + ( 2*rnd.NextUnif() - 1 )*16.8;
				}
				lfSbp = lfTemp1 > lfTemp2 ? lfTemp1 : lfTemp2;
				lfDbp = lfTemp1 < lfTemp2 ? lfTemp1 : lfTemp2;

				if( iInjuryRandomMode == 0 )
				{
					lfPulse = 109.1 + ( 2*rnd.NextUnif() - 1 )*17.9;
					lfRr	=  25.0 + ( 2*rnd.NextUnif() - 1 )*5.0;
				}
				else if( iInjuryRandomMode == 1 )
				{
					lfRand = normalRand();
					lfPulse = 109.1 + lfRand*17.9;
					lfRand = normalRand();
					lfRr	=  25.0 + lfRand*5.0;
				}
				else if( iInjuryRandomMode == 2 )
				{
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand = normalRand();
					lfPulse = 109.1 + lfRand*17.9;
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand = normalRand();
					lfRr	=  25.0 + lfRand*5.0;
//					lfPulse = 109.1 + ( 2*rnd.NextUnif() - 1 )*17.9;
//					lfRr	=  25.0 + ( 2*rnd.NextUnif() - 1 )*5.0;
				}
			}
			lfPrevProb = lfProb;
			lfProb += 0.046;
//			if( lfPrevProb <= lfCurProb && lfCurProb < lfProb )
			if( lfPrevProb <= lfCurProb )
			{
				if( iInjuryRandomMode == 0 )
				{
					lfTemp1	=  70.6 + ( 2*rnd.NextUnif() - 1 )*15.7;
					lfTemp2	=  70.6 + ( 2*rnd.NextUnif() - 1 )*15.7;
				}
				else if( iInjuryRandomMode == 1 )
				{
					lfRand = normalRand();
					lfTemp1	=  70.6 + lfRand*15.7;
					lfRand = normalRand();
					lfTemp2	=  70.6 + lfRand*15.7;
				}
				else if( iInjuryRandomMode == 2 )
				{
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand = normalRand();
					lfTemp1	=  70.6 + lfRand*15.7;
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand = normalRand();
					lfTemp2	=  70.6 + lfRand*15.7;
//					lfTemp1	=  70.6 + ( 2*rnd.NextUnif() - 1 )*15.7;
//					lfTemp2	=  70.6 + ( 2*rnd.NextUnif() - 1 )*15.7;
				}
				lfSbp = lfTemp1 > lfTemp2 ? lfTemp1 : lfTemp2;
				lfDbp = lfTemp1 < lfTemp2 ? lfTemp1 : lfTemp2;

				if( iInjuryRandomMode == 0 )
				{
					lfPulse = 122.7 + ( 2*rnd.NextUnif() - 1 )*19.5;
					lfRr	=  35.0 + ( 2*rnd.NextUnif() - 1 )*5.0;
				}
				else if( iInjuryRandomMode == 1 )
				{
					lfRand = normalRand();
					lfPulse = 122.7 + lfRand*19.5;
					lfRand = normalRand();
					lfRr	=  35.0 + lfRand*5.0;
				}
				else if( iInjuryRandomMode == 2 )
				{
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand = normalRand();
					lfPulse = 122.7 + lfRand*19.5;
//					lfRand	= weibullRand( 1.0, 0.5 );
					lfRand = normalRand();
					lfRr	=  35.0 + lfRand*5.0;
//					lfPulse = 122.7 + ( 2*rnd.NextUnif() - 1 )*19.5;
//					lfRr	=  35.0 + ( 2*rnd.NextUnif() - 1 )*5.0;
				}
			}
		}
		else
		{
			if( iInjuryRandomMode == 0 )
			{
				lfTemp1	=  110.0 + ( 2*rnd.NextUnif() - 1 )*14.5;
				lfTemp2	=  72.5 + ( 2*rnd.NextUnif() - 1 )*12.5;
			}
			else if( iInjuryRandomMode == 1 )
			{
				lfRand = normalRand();
				lfTemp1	=  110.0 + lfRand*14.5;
				lfRand = normalRand();
				lfTemp2	=  72.5 + lfRand*12.5;
			}
			else if( iInjuryRandomMode == 2 )
			{
//				lfRand	= weibullRand( 1.0, 0.5 );
				lfRand	= normalRand();
				lfTemp1	=  110.0 + lfRand*14.5;
//				lfRand	= weibullRand( 1.0, 0.5 );
				lfRand	= normalRand();
				lfTemp2	=  72.5 + lfRand*12.5;
//				lfTemp1	=  95.0 + ( 2*rnd.NextUnif() - 1 )*25.0;
//				lfTemp2	=  95.0 + ( 2*rnd.NextUnif() - 1 )*25.0;
			}
			lfSbp = lfTemp1;
			lfDbp = lfTemp2;

			if( iInjuryRandomMode == 0 )
			{
				lfPulse =  80.0 + ( 2*rnd.NextUnif() - 1 )*10.0;
				lfRr	=  17.5 + ( 2*rnd.NextUnif() - 1 )*2.5;
			}
			else if( iInjuryRandomMode == 1 )
			{
				lfRand = normalRand();
				lfPulse =  80.0 + lfRand*10.0;
				lfRand = normalRand();
				lfRr	=  17.5 + lfRand*2.5;
			}
			else if( iInjuryRandomMode == 2 )
			{
				lfRand	= normalRand();
				lfPulse =  80.0 + lfRand*10.0;
				lfRand	= normalRand();
				lfRr	=  17.5 + lfRand*2.5;
//				lfPulse =  80.0 + ( 2*rnd.NextUnif() - 1 )*10.0;
//				lfRr	=  17.5 + ( 2*rnd.NextUnif() - 1 )*2.5;
			}
		}
	}

	/**
	 * <PRE>
	 *    現在の外傷状態で外に反応する状態を設定します。
	 * </PRE>
	 */
	public void vStrSetInjuryStatus()
	{
		if( lfInternalAISHead < 1 )														strInjuryHeadStatus = "痛くない";
		else if( 1.0000000 <= lfInternalAISHead && lfInternalAISHead <= 1.333333 )		strInjuryHeadStatus = "ほんの少し痛い";
		else if( 1.3333333 < lfInternalAISHead && lfInternalAISHead <= 1.6666666 )		strInjuryHeadStatus = "少し痛い";
		else if( 1.6666666 < lfInternalAISHead && lfInternalAISHead <= 2.0000000 )		strInjuryHeadStatus = "少々痛い";
		else if( 2.0000000 < lfInternalAISHead && lfInternalAISHead <= 2.3333333 )		strInjuryHeadStatus = "痛い";
		else if( 2.3333333 < lfInternalAISHead && lfInternalAISHead <= 2.6666666 )		strInjuryHeadStatus = "けっこう痛い";
		else if( 2.6666666 < lfInternalAISHead && lfInternalAISHead <= 3.0000000 )		strInjuryHeadStatus = "相当痛い";
		else if( 3.0000000 < lfInternalAISHead && lfInternalAISHead <= 3.3333333 )		strInjuryHeadStatus = "かなり痛い";
		else if( 3.3333333 < lfInternalAISHead && lfInternalAISHead <= 3.6666666 )		strInjuryHeadStatus = "とてつもなく痛い";
		else if( 3.6666666 < lfInternalAISHead && lfInternalAISHead <= 4.0000000 )		strInjuryHeadStatus = "とてつもなくかなり痛い";
		else if( 4.0000000 < lfInternalAISHead && lfInternalAISHead <= 5.0000000 )		strInjuryHeadStatus = "耐えられないほど痛い";

		if( lfInternalAISFace < 1 )														strInjuryFaceStatus = "痛くない";
		else if( 1.0000000 <= lfInternalAISFace && lfInternalAISFace <= 1.333333 )		strInjuryFaceStatus = "ほんの少し痛い";
		else if( 1.3333333 < lfInternalAISFace && lfInternalAISFace <= 1.6666666 )		strInjuryFaceStatus = "少し痛い";
		else if( 1.6666666 < lfInternalAISFace && lfInternalAISFace <= 2.0000000 )		strInjuryFaceStatus = "少々痛い";
		else if( 2.0000000 < lfInternalAISFace && lfInternalAISFace <= 2.3333333 )		strInjuryFaceStatus = "痛い";
		else if( 2.3333333 < lfInternalAISFace && lfInternalAISFace <= 2.6666666 )		strInjuryFaceStatus = "けっこう痛い";
		else if( 2.6666666 < lfInternalAISFace && lfInternalAISFace <= 3.0000000 )		strInjuryFaceStatus = "相当痛い";
		else if( 3.0000000 < lfInternalAISFace && lfInternalAISFace <= 3.3333333 )		strInjuryFaceStatus = "かなり痛い";
		else if( 3.3333333 < lfInternalAISFace && lfInternalAISFace <= 3.6666666 )		strInjuryFaceStatus = "とてつもなく痛い";
		else if( 3.6666666 < lfInternalAISFace && lfInternalAISFace <= 4.0000000 )		strInjuryFaceStatus = "とてつもなくかなり痛い";
		else if( 4.0000000 < lfInternalAISFace && lfInternalAISFace <= 5.0000000 )		strInjuryFaceStatus = "耐えられないほど痛い";

		if( lfInternalAISNeck < 1 )														strInjuryNeckStatus = "痛くない";
		else if( 1.0000000 <= lfInternalAISNeck && lfInternalAISNeck <= 1.333333 )		strInjuryNeckStatus = "ほんの少し痛い";
		else if( 1.3333333 < lfInternalAISNeck && lfInternalAISNeck <= 1.6666666 )		strInjuryNeckStatus = "少し痛い";
		else if( 1.6666666 < lfInternalAISNeck && lfInternalAISNeck <= 2.0000000 )		strInjuryNeckStatus = "少々痛い";
		else if( 2.0000000 < lfInternalAISNeck && lfInternalAISNeck <= 2.3333333 )		strInjuryNeckStatus = "痛い";
		else if( 2.3333333 < lfInternalAISNeck && lfInternalAISNeck <= 2.6666666 )		strInjuryNeckStatus = "けっこう痛い";
		else if( 2.6666666 < lfInternalAISNeck && lfInternalAISNeck <= 3.0000000 )		strInjuryNeckStatus = "相当痛い";
		else if( 3.0000000 < lfInternalAISNeck && lfInternalAISNeck <= 3.3333333 )		strInjuryNeckStatus = "かなり痛い";
		else if( 3.3333333 < lfInternalAISNeck && lfInternalAISNeck <= 3.6666666 )		strInjuryNeckStatus = "とてつもなく痛い";
		else if( 3.6666666 < lfInternalAISNeck && lfInternalAISNeck <= 4.0000000 )		strInjuryNeckStatus = "とてつもなくかなり痛い";
		else if( 4.0000000 < lfInternalAISNeck && lfInternalAISNeck <= 5.0000000 )		strInjuryNeckStatus = "耐えられないほど痛い";

		if( lfInternalAISThorax < 1 )														strInjuryThoraxStatus = "痛くない";
		else if( 1.0000000 <= lfInternalAISThorax && lfInternalAISThorax <= 1.333333 )		strInjuryThoraxStatus = "ほんの少し痛い";
		else if( 1.3333333 < lfInternalAISThorax && lfInternalAISThorax <= 1.6666666 )		strInjuryThoraxStatus = "少し痛い";
		else if( 1.6666666 < lfInternalAISThorax && lfInternalAISThorax <= 2.0000000 )		strInjuryThoraxStatus = "少々痛い";
		else if( 2.0000000 < lfInternalAISThorax && lfInternalAISThorax <= 2.3333333 )		strInjuryThoraxStatus = "痛い";
		else if( 2.3333333 < lfInternalAISThorax && lfInternalAISThorax <= 2.6666666 )		strInjuryThoraxStatus = "けっこう痛い";
		else if( 2.6666666 < lfInternalAISThorax && lfInternalAISThorax <= 3.0000000 )		strInjuryThoraxStatus = "相当痛い";
		else if( 3.0000000 < lfInternalAISThorax && lfInternalAISThorax <= 3.3333333 )		strInjuryThoraxStatus = "かなり痛い";
		else if( 3.3333333 < lfInternalAISThorax && lfInternalAISThorax <= 3.6666666 )		strInjuryThoraxStatus = "とてつもなく痛い";
		else if( 3.6666666 < lfInternalAISThorax && lfInternalAISThorax <= 4.0000000 )		strInjuryThoraxStatus = "とてつもなくかなり痛い";
		else if( 4.0000000 < lfInternalAISThorax && lfInternalAISThorax <= 5.0000000 )		strInjuryThoraxStatus = "耐えられないほど痛い";

		if( lfInternalAISAbdomen < 1 )																		strInjuryAbdomenStatus = "痛くない";
		else if( 1.0000000 <= lfInternalAISAbdomen && lfInternalAISAbdomen <= 1.333333 )					strInjuryAbdomenStatus = "ほんの少し痛い";
		else if( 1.3333333 < lfInternalAISAbdomen && lfInternalAISAbdomen <= 1.6666666 )					strInjuryAbdomenStatus = "少し痛い";
		else if( 1.6666666 < lfInternalAISAbdomen && lfInternalAISAbdomen <= 2.0000000 )					strInjuryAbdomenStatus = "少々痛い";
		else if( 2.0000000 < lfInternalAISAbdomen && lfInternalAISAbdomen <= 2.3333333 )					strInjuryAbdomenStatus = "痛い";
		else if( 2.3333333 < lfInternalAISAbdomen && lfInternalAISAbdomen <= 2.6666666 )					strInjuryAbdomenStatus = "けっこう痛い";
		else if( 2.6666666 < lfInternalAISAbdomen && lfInternalAISAbdomen <= 3.0000000 )					strInjuryAbdomenStatus = "相当痛い";
		else if( 3.0000000 < lfInternalAISAbdomen && lfInternalAISAbdomen <= 3.3333333 )					strInjuryAbdomenStatus = "かなり痛い";
		else if( 3.3333333 < lfInternalAISAbdomen && lfInternalAISAbdomen <= 3.6666666 )					strInjuryAbdomenStatus = "とてつもなく痛い";
		else if( 3.6666666 < lfInternalAISAbdomen && lfInternalAISAbdomen <= 4.0000000 )					strInjuryAbdomenStatus = "とてつもなくかなり痛い";
		else if( 4.0000000 < lfInternalAISAbdomen && lfInternalAISAbdomen <= 5.0000000 )					strInjuryAbdomenStatus = "耐えられないほど痛い";

		if( lfInternalAISSpine < 1 )																		strInjurySpineStatus = "痛くない";
		else if( 1.0000000 <= lfInternalAISSpine && lfInternalAISSpine <= 1.333333 )						strInjurySpineStatus = "ほんの少し痛い";
		else if( 1.3333333 < lfInternalAISSpine && lfInternalAISSpine <= 1.6666666 )						strInjurySpineStatus = "少し痛い";
		else if( 1.6666666 < lfInternalAISSpine && lfInternalAISSpine <= 2.0000000 )						strInjurySpineStatus = "少々痛い";
		else if( 2.0000000 < lfInternalAISSpine && lfInternalAISSpine <= 2.3333333 )						strInjurySpineStatus = "痛い";
		else if( 2.3333333 < lfInternalAISSpine && lfInternalAISSpine <= 2.6666666 )						strInjurySpineStatus = "けっこう痛い";
		else if( 2.6666666 < lfInternalAISSpine && lfInternalAISSpine <= 3.0000000 )						strInjurySpineStatus = "相当痛い";
		else if( 3.0000000 < lfInternalAISSpine && lfInternalAISSpine <= 3.3333333 )						strInjurySpineStatus = "かなり痛い";
		else if( 3.3333333 < lfInternalAISSpine && lfInternalAISSpine <= 3.6666666 )						strInjurySpineStatus = "とてつもなく痛い";
		else if( 3.6666666 < lfInternalAISSpine && lfInternalAISSpine <= 4.0000000 )						strInjurySpineStatus = "とてつもなくかなり痛い";
		else if( 4.0000000 < lfInternalAISSpine && lfInternalAISSpine <= 5.0000000 )						strInjurySpineStatus = "耐えられないほど痛い";

		if( lfInternalAISUpperExtremity < 1 )																strInjuryUpperExtremityStatus = "痛くない";
		else if( 1.0000000 <= lfInternalAISUpperExtremity && lfInternalAISUpperExtremity <= 1.333333 )		strInjuryUpperExtremityStatus = "ほんの少し痛い";
		else if( 1.3333333 < lfInternalAISUpperExtremity && lfInternalAISUpperExtremity <= 1.6666666 )		strInjuryUpperExtremityStatus = "少し痛い";
		else if( 1.6666666 < lfInternalAISUpperExtremity && lfInternalAISUpperExtremity <= 2.0000000 )		strInjuryUpperExtremityStatus = "少々痛い";
		else if( 2.0000000 < lfInternalAISUpperExtremity && lfInternalAISUpperExtremity <= 2.3333333 )		strInjuryUpperExtremityStatus = "痛い";
		else if( 2.3333333 < lfInternalAISUpperExtremity && lfInternalAISUpperExtremity <= 2.6666666 )		strInjuryUpperExtremityStatus = "けっこう痛い";
		else if( 2.6666666 < lfInternalAISUpperExtremity && lfInternalAISUpperExtremity <= 3.0000000 )		strInjuryUpperExtremityStatus = "相当痛い";
		else if( 3.0000000 < lfInternalAISUpperExtremity && lfInternalAISUpperExtremity <= 3.3333333 )		strInjuryUpperExtremityStatus = "かなり痛い";
		else if( 3.3333333 < lfInternalAISUpperExtremity && lfInternalAISUpperExtremity <= 3.6666666 )		strInjuryUpperExtremityStatus = "とてつもなく痛い";
		else if( 3.6666666 < lfInternalAISUpperExtremity && lfInternalAISUpperExtremity <= 4.0000000 )		strInjuryUpperExtremityStatus = "とてつもなくかなり痛い";
		else if( 4.0000000 < lfInternalAISUpperExtremity && lfInternalAISUpperExtremity <= 5.0000000 )		strInjuryUpperExtremityStatus = "耐えられないほど痛い";

		if( lfInternalAISLowerExtremity < 1 )																strInjuryLowerExtremityStatus = "痛くない";
		else if( 1.0000000 <= lfInternalAISLowerExtremity && lfInternalAISLowerExtremity <= 1.333333 )		strInjuryLowerExtremityStatus = "ほんの少し痛い";
		else if( 1.3333333 < lfInternalAISLowerExtremity && lfInternalAISLowerExtremity <= 1.6666666 )		strInjuryLowerExtremityStatus = "少し痛い";
		else if( 1.6666666 < lfInternalAISLowerExtremity && lfInternalAISLowerExtremity <= 2.0000000 )		strInjuryLowerExtremityStatus = "少々痛い";
		else if( 2.0000000 < lfInternalAISLowerExtremity && lfInternalAISLowerExtremity <= 2.3333333 )		strInjuryLowerExtremityStatus = "痛い";
		else if( 2.3333333 < lfInternalAISLowerExtremity && lfInternalAISLowerExtremity <= 2.6666666 )		strInjuryLowerExtremityStatus = "けっこう痛い";
		else if( 2.6666666 < lfInternalAISLowerExtremity && lfInternalAISLowerExtremity <= 3.0000000 )		strInjuryLowerExtremityStatus = "相当痛い";
		else if( 3.0000000 < lfInternalAISLowerExtremity && lfInternalAISLowerExtremity <= 3.3333333 )		strInjuryLowerExtremityStatus = "かなり痛い";
		else if( 3.3333333 < lfInternalAISLowerExtremity && lfInternalAISLowerExtremity <= 3.6666666 )		strInjuryLowerExtremityStatus = "とてつもなく痛い";
		else if( 3.6666666 < lfInternalAISLowerExtremity && lfInternalAISLowerExtremity <= 4.0000000 )		strInjuryLowerExtremityStatus = "とてつもなくかなり痛い";
		else if( 4.0000000 < lfInternalAISLowerExtremity && lfInternalAISLowerExtremity <= 5.0000000 )		strInjuryLowerExtremityStatus = "耐えられないほど痛い";

		if( lfInternalAISUnspecified < 1 )																strInjuryUnspecifiedStatus = "痛くない";
		else if( 1.0000000 <= lfInternalAISUnspecified && lfInternalAISUnspecified <= 1.333333 )		strInjuryUnspecifiedStatus = "ほんの少し痛い";
		else if( 1.3333333 < lfInternalAISUnspecified && lfInternalAISUnspecified <= 1.6666666 )		strInjuryUnspecifiedStatus = "少し痛い";
		else if( 1.6666666 < lfInternalAISUnspecified && lfInternalAISUnspecified <= 2.0000000 )		strInjuryUnspecifiedStatus = "少々痛い";
		else if( 2.0000000 < lfInternalAISUnspecified && lfInternalAISUnspecified <= 2.3333333 )		strInjuryUnspecifiedStatus = "痛い";
		else if( 2.3333333 < lfInternalAISUnspecified && lfInternalAISUnspecified <= 2.6666666 )		strInjuryUnspecifiedStatus = "けっこう痛い";
		else if( 2.6666666 < lfInternalAISUnspecified && lfInternalAISUnspecified <= 3.0000000 )		strInjuryUnspecifiedStatus = "相当痛い";
		else if( 3.0000000 < lfInternalAISUnspecified && lfInternalAISUnspecified <= 3.3333333 )		strInjuryUnspecifiedStatus = "かなり痛い";
		else if( 3.3333333 < lfInternalAISUnspecified && lfInternalAISUnspecified <= 3.6666666 )		strInjuryUnspecifiedStatus = "とてつもなく痛い";
		else if( 3.6666666 < lfInternalAISUnspecified && lfInternalAISUnspecified <= 4.0000000 )		strInjuryUnspecifiedStatus = "とてつもなくかなり痛い";
		else if( 4.0000000 < lfInternalAISUnspecified && lfInternalAISUnspecified <= 5.0000000 )		strInjuryUnspecifiedStatus = "耐えられないほど痛い";

		if( lfSpO2 < 0.1 )							strSpO2SignStatus = "呼吸停止";
		if( 0.1 <= lfSpO2 && lfSpO2 < 0.75 )		strSpO2SignStatus = "チアノーゼ";
		else if( 0.75 <= lfSpO2 && lfSpO2 < 0.9 )	strSpO2SignStatus = "単語単位でのみ会話可能";
		else if( 0.9 <= lfSpO2 && lfSpO2 < 0.92 )	strSpO2SignStatus = "文節単位で会話かろうじて可能";
		else if( 0.92 <= lfSpO2 && lfSpO2 < 0.94 )	strSpO2SignStatus = "文節単位で会話可能";
		else										strSpO2SignStatus = "通常に会話可能";

		if( lfRr == 0.0 )
		{
			strRespirationSignStatus = "呼吸停止";
			lfInternalFaceSign = 1.0;
		}
		else if( 0.0 < lfRr && lfRr <= 5.0 )		strRespirationSignStatus = "あえぎ呼吸";
		else if( 5.0 < lfRr && lfRr <= 8.0 )		strRespirationSignStatus = "徐呼吸";
		else if( 8.0 < lfRr && lfRr <= 12.0 )		strRespirationSignStatus = "少々徐呼吸";
		else if( 12.0 < lfRr && lfRr <= 20.0 )
		{
			strRespirationSignStatus = "正常呼吸";
			lfInternalFaceSign =  4.0;
		}
		else if( 20.0 < lfRr && lfRr <= 24.0 )		strRespirationSignStatus = "少々頻呼吸";
		else if( 24.0 < lfRr && lfRr <= 30.0 )		strRespirationSignStatus = "頻呼吸";
		else if( 30.0 < lfRr && lfRr <= 40.0 )		strRespirationSignStatus = "過換気呼吸";
		else if( lfRr <= 40.0 )						strRespirationSignStatus = "重症頻呼吸";

		if( lfBodyTemperature < 32.0 )
		{
			strBodyTemperatureSignStatus = "低体温症";
		}
		else if( 32.0 <= lfBodyTemperature && lfBodyTemperature < 34.0 )
		{
			strBodyTemperatureSignStatus = "低体温";
		}
		else if( 34.0 <= lfBodyTemperature && lfBodyTemperature < 37.0 )
		{
			strBodyTemperatureSignStatus = "常温";
		}
		else if( 37.0 <= lfBodyTemperature && lfBodyTemperature < 38.0 )
		{
			strBodyTemperatureSignStatus = "微熱";
		}
		else if( 38.0 <= lfBodyTemperature && lfBodyTemperature < 39.0 )
		{
			strBodyTemperatureSignStatus = "中等度発熱";
		}
		else if( 39.0 <= lfBodyTemperature && lfBodyTemperature < 41.5 )
		{
			strBodyTemperatureSignStatus = "高熱";
		}
		else if( 41.5 <= lfBodyTemperature && lfBodyTemperature < 43.0 )
		{
			strBodyTemperatureSignStatus = "過高温";
		}
		else if( 43.0 <= lfBodyTemperature )
		{
			strBodyTemperatureSignStatus = "超過高温";
		}

		if( lfPulse < 40.0 )
		{
			strPulseSignStatus = "危険な徐脈";
			lfInternalHeartSign = 2.0;
		}
		else if( 40.0 <= lfPulse && lfPulse < 60.0 )
		{
			strPulseSignStatus = "徐脈";
			lfInternalHeartSign = 2.0;
		}
		else if( 60.0 <= lfPulse && lfPulse < 90.0 )
		{
			strPulseSignStatus = "通常脈拍";
			lfInternalHeartSign = 3.0;
			lfInternalSkinSign = 4.0;
		}
		else if( 90.0 <= lfPulse && lfPulse < 100.0 )
		{
			strPulseSignStatus = "通常脈拍高め";
			lfInternalHeartSign = 3.0;
			lfInternalSkinSign = 4.0;
		}
		else if( 100.0 <= lfPulse && lfPulse < 120.0 )	strPulseSignStatus = "頻脈";
		else if( 120.0 <= lfPulse && lfPulse < 140.0 )
		{
			strPulseSignStatus = "意識低下と心不全を伴う頻脈";
			lfInternalSkinSign = 2.5;
		}
		else if( 140.0 <= lfPulse )
		{
			strPulseSignStatus = "失神を伴う頻脈";
			lfInternalSkinSign = 2.5;
		}

		if( ( lfSbp < 80.0 ) || lfDbp <= 60.0 )
		{
			strBloodPressureSignStatus = "低血圧";
			lfInternalFaceSign = 3.0;
		}
		else if( ( 80.0 <= lfSbp && lfSbp < 120.0 )	 ||	lfDbp <= 80.0 )
		{
			strBloodPressureSignStatus = "至適血圧";
			lfInternalFaceSign = 4.0;
			lfInternalSkinSign = 4.0;
		}
		else if( ( 120.0 <= lfSbp && lfSbp < 130.0 ) ||	lfDbp <= 85.0 )
		{
			strBloodPressureSignStatus = "正常血圧";
			lfInternalFaceSign = 4.0;
			lfInternalSkinSign = 4.0;
		}
		else if( ( 130.0 <= lfSbp && lfSbp < 140.0 ) ||	lfDbp <= 90.0 )
		{
			strBloodPressureSignStatus = "正常高値";
			lfInternalFaceSign = 4.0;
		}
		else if( ( 140.0 <= lfSbp && lfSbp < 160.0 ) || lfDbp <= 100.0 )
		{
			strBloodPressureSignStatus = "軽症高血圧";
		}
		else if( ( 160.0 <= lfSbp && lfSbp < 180.0 ) || lfDbp <= 110.0 )
		{
			strBloodPressureSignStatus = "中等度高血圧";
		}
		else if( 180.0 <= lfSbp )
		{
			strBloodPressureSignStatus = "重症高血圧";
		}

		if( 1.0 <= lfInternalFaceSign && lfInternalFaceSign < 2.0 )			strFaceSignStatus = "チアノーゼ";
		else if( 2.0 <= lfInternalFaceSign && lfInternalFaceSign < 3.0 )	strFaceSignStatus = "著明に蒼白";
		else if( 3.0 <= lfInternalFaceSign && lfInternalFaceSign < 4.0 )	strFaceSignStatus = "蒼白";
		else if( 4.0 <= lfInternalFaceSign )								strFaceSignStatus = "正常";

		if( 1.0 <= lfInternalSkinSign && lfInternalSkinSign < 2.0 )			strSkinSignStatus = "冷たい皮膚";
		else if( 2.0 <= lfInternalSkinSign && lfInternalSkinSign <= 3.0 )	strSkinSignStatus = "冷たい発汗";
		else if( 3.0 <= lfInternalSkinSign && lfInternalSkinSign < 4.0 )	strSkinSignStatus = "温かい発汗";
		else if( 4.0 <= lfInternalSkinSign )								strSkinSignStatus = "正常";

		if( 1.0 <= lfInternalHeartSign && lfInternalHeartSign < 2.0 )		strHeartSignStatus = "心停止";
		else if( 2.0 <= lfInternalHeartSign && lfInternalHeartSign <= 3.0 )	strHeartSignStatus = "心臓の鼓動不安定";
		else if( 3.0 <= lfInternalHeartSign )								strHeartSignStatus = "正常";
	}

	public String strGetInjuryHeadStatus()
	{
		return strInjuryHeadStatus;
	}

	public String strGetInjuryFaceStatus()
	{
		return strInjuryFaceStatus;
	}

	public String strGetInjuryNeckStatus()
	{
		return strInjuryNeckStatus;
	}

	public String strGetInjuryThoraxStatus()
	{
		return strInjuryThoraxStatus;
	}

	public String strGetInjuryAbdomenStatus()
	{
		return strInjuryAbdomenStatus;
	}

	public String strGetInjurySpineStatus()
	{
		return strInjurySpineStatus;
	}

	public String strGetInjuryUpperExtremityStatus()
	{
		return strInjuryUpperExtremityStatus;
	}

	public String strGetInjuryLowerExtremityStatus()
	{
		return strInjuryLowerExtremityStatus;
	}

	public String strGetInjuryUnspecifiedStatus()
	{
		return strInjuryUnspecifiedStatus;
	}

	/**
	 * <PRE>
	 *   患者から皮膚の血色状態を文字列で取得します。
	 * </PRE>
	 * @return　皮膚の血色状態
	 * @author kobayashi
	 * @since 2015/08/12
	 */
	public String strGetSkinSignStatus()
	{
		return strSkinSignStatus;
	}

	/**
	 * <PRE>
	 *   患者から心肺の状態を文字列で取得します。
	 * </PRE>
	 * @return 心配の状態
	 * @author kobayashi
	 * @since 2015/08/12
	 */
	public String strGetHeartSignStatus()
	{
		return strHeartSignStatus;
	}

	/**
	 * <PRE>
	 *   患者から意識状態を文字列で取得します。
	 * </PRE>
	 * @return 意識状態
	 * @author kobayashi
	 * @since 2015/08/12
	 */
	public String strGetConsciousnessStatus()
	{
		// TODO 自動生成されたメソッド・スタブ
		return strConsciousnessSignStatus;
	}

	/**
	 * <PRE>
	 *   患者から呼吸状態を文字列で取得します。
	 * </PRE>
	 * @return 呼吸状態
	 * @author kobayashi
	 * @since 2015/08/12
	 */
	public String strGetRespirationStatus()
	{
		// TODO 自動生成されたメソッド・スタブ
		return strRespirationSignStatus;
	}

	/**
	 * <PRE>
	 *   患者からSpO2の状態を取得します。
	 * </PRE>
	 * @return SpO2の状態
	 * @author kobayashi
	 * @since 2015/08/12
	 */
	public String strGetSpO2Status()
	{
		return strSpO2SignStatus;
	}

	/**
	 * <PRE>
	 *    患者から顔面の血色を取得します。
	 * </PRE>
	 * @return　顔面の血色
	 * @author kobayashi
	 * @since 2015/08/12
	 */
	public String strGetFaceSignStatus()
	{
		return strFaceSignStatus;
	}

	public String strGetBodyTemperatureSignStatus()
	{
		return strBodyTemperatureSignStatus;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している頭部のAISを取得します。
	 * </PRE>
	 * @return 頭部AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public double lfGetInternalAISHead()
	{
		return lfInternalAISHead;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している顔面のAISを取得します。
	 * </PRE>
	 * @return 顔面AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public double lfGetInternalAISFace()
	{
		return lfInternalAISFace;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している首のAISを取得します。
	 * </PRE>
	 * @return 首AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public double lfGetInternalAISNeck()
	{
		return lfInternalAISNeck;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している胸部のAISを取得します。
	 * </PRE>
	 * @return 胸部AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public double lfGetInternalAISThorax()
	{
		return lfInternalAISThorax;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している腹部のAISを取得します。
	 * </PRE>
	 * @return 腹部AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public double lfGetInternalAISAbdomen()
	{
		return lfInternalAISAbdomen;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している脊椎のAISを取得します。
	 * </PRE>
	 * @return 脊椎AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public 	double lfGetInternalAISSpine()
	{
		return lfInternalAISSpine;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している上肢のAISを取得します。
	 * </PRE>
	 * @return 上肢AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public double lfGetInternalAISUpperExtremity()
	{
		return lfInternalAISUpperExtremity;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している下肢のAISを取得します。
	 * </PRE>
	 * @return 下肢AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public double lfGetInternalAISLowerExtremity()
	{
		return lfInternalAISLowerExtremity;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している特定しない（表面・熱傷・その他外傷）場合のAISを取得します。
	 * </PRE>
	 * @return 特定しないAISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public double lfGetInternalAISUnspecified()
	{
		return lfInternalAISUnspecified;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している頭部のAISを設定します。
	 * </PRE>
	 * @param lfInternalAISHeadData 患者の実際の頭部AIS値
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vSetInternalAISHead( double lfInternalAISHeadData )
	{
		lfInternalAISHead = lfInternalAISHeadData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している顔面のAISを設定します。
	 * </PRE>
	 * @param lfInternalAISFaceData 患者の実際の顔面AIS値
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vSetInternalAISFace( double lfInternalAISFaceData )
	{
		lfInternalAISFace = lfInternalAISFaceData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している首のAISを設定します。
	 * </PRE>
	 * @param lfInternalAISNeckData 患者の実際の頸部AIS値
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vSetInternalAISNeck( double lfInternalAISNeckData )
	{
		lfInternalAISNeck = lfInternalAISNeckData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している胸部のAISを設定します。
	 * </PRE>
	 * @param lfInternalAISThoraxData 患者の実際の胸部AIS値
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vSetInternalAISThorax( double lfInternalAISThoraxData )
	{
		lfInternalAISThorax = lfInternalAISThoraxData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している腹部のAISを設定します。
	 * </PRE>
	 * @param lfInternalAISAbdomenData 患者の実際の頸部AIS値
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vSetInternalAISAbdomen( double lfInternalAISAbdomenData )
	{
		lfInternalAISAbdomen = lfInternalAISAbdomenData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している脊椎のAISを設定します。
	 * </PRE>
	 * @param lfInternalAISSpineData 患者の実際の脊椎AIS値
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public 	void vSetInternalAISSpine( double lfInternalAISSpineData )
	{
		lfInternalAISSpine = lfInternalAISSpineData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している上肢のAISを設定します。
	 * </PRE>
	 * @param lfInternalAISUpperExtremityData 患者の実際の上肢AIS値
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vSetInternalAISUpperExtremity( double lfInternalAISUpperExtremityData )
	{
		lfInternalAISUpperExtremity = lfInternalAISUpperExtremityData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している下肢のAISを設定します。
	 * </PRE>
	 * @param lfInternalAISLowerExtremityData 患者の実際の下肢AIS値
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vSetInternalAISLowerExtremity( double lfInternalAISLowerExtremityData )
	{
		lfInternalAISLowerExtremity = lfInternalAISLowerExtremityData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが保持している特定しない（表面・熱傷・その他外傷）場合のAISを設定します。
	 * </PRE>
	 * @param lfInternalAISUnspecifiedData 患者の実際の体表、熱傷、その他外傷AIS値
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vSetInternalAISUnspecified( double lfInternalAISUnspecifiedData )
	{
		lfInternalAISUnspecified = lfInternalAISUnspecifiedData;
	}


	/**
	 * <PRE>
	 *   患者エージェントが診察待ちをしているかのフラグを取得します。
	 * </PRE>
	 * @return 診察待ちフラグ
	 *         0 診察待ちしていない
	 *         1 診察待ちしている
	 * @author kobayashi
	 * @since 2015/07/30
	 */
	public int iGetConsultationRoomWaitFlag()
	{
		return iConsultationRoomWaitFlag;
	}

	/**
	 * <PRE>
	 *   患者エージェントが検査室待ちをしているかのフラグを取得します。
	 * </PRE>
	 * @return X線室待ちフラグ
	 *         0 待っていない
	 *         1 待っている
	 * @author kobayashi
	 * @since 2015/07/30
	 */
	public int iGetExaminationXRayRoomWaitFlag()
	{
		return iExaminationXRayRoomWaitFlag;
	}

	/**
	 * <PRE>
	 *   患者エージェントが検査室待ちをしているかのフラグを取得します。
	 * </PRE>
	 * @return 0 CT検査待ちせず。
	 *         1 CT検査待ち中
	 * @author kobayashi
	 * @since 2015/07/30
	 */
	public int iGetExaminationCTRoomWaitFlag()
	{
		return iExaminationCTRoomWaitFlag;
	}

	/**
	 * <PRE>
	 *   患者エージェントが検査室待ちをしているかのフラグを取得します。
	 * </PRE>
	 * @return 0 MRI検査待ちせず。
	 *         1 MRI検査待ち中
	 * @author kobayashi
	 * @since 2015/07/30
	 */
	public int iGetExaminationMRIRoomWaitFlag()
	{
		return iExaminationMRIRoomWaitFlag;
	}

	/**
	 * <PRE>
	 *   患者エージェントが検査室待ちをしているかのフラグを取得します。
	 * </PRE>
	 * @return 0 血管造影検査待ちせず。
	 *         1 血管造影検査待ち中
	 * @author kobayashi
	 * @since 2015/07/30
	 */
	public int iGetExaminationAngiographyRoomWaitFlag()
	{
		return iExaminationAngiographyRoomWaitFlag;
	}

	/**
	 * <PRE>
	 *   患者エージェントが検査室待ちをしているかのフラグを取得します。
	 * </PRE>
	 * @return 0 超音波検査待ちせず。
	 *         1 超音波検査待ち中
	 * @author kobayashi
	 * @since 2015/07/30
	 */
	public int iGetExaminationFastRoomWaitFlag()
	{
		return iExaminationFastRoomWaitFlag;
	}

	/**
	 * <PRE>
	 *   患者エージェントが初療室待ちをしているかのフラグを取得します。
	 * </PRE>
	 * @return 0 初療室待ちせず。
	 *         1 初療室待ち中
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public int iGetEmergencyRoomWaitFlag()
	{
		return iEmergencyRoomWaitFlag;
	}

	/**
	 * <PRE>
	 *   患者エージェントが観察室待ちをしているかのフラグを取得します。
	 * </PRE>
	 * @return 0 観察室待ちせず。
	 *         1 観察室待ち中
	 * @author kobayashi
	 * @since 2015/07/30
	 */
	public int iGetObservationRoomWaitFlag()
	{
		return iObservationRoomWaitFlag;
	}

	/**
	 * <PRE>
	 *   患者エージェントが手術室待ちをしているかのフラグを取得します。
	 * </PRE>
	 * @return 0 手術室待ちせず。
	 *         1 手術室待ち中
	 * @author kobayashi
	 * @since 2015/07/30
	 */
	public int iGetOperationRoomWaitFlag()
	{
		return iOperationRoomWaitFlag;
	}

	/**
	 * <PRE>
	 *   患者エージェントが重症観察室待ちをしているかのフラグを取得します。
	 * </PRE>
	 * @return 0 重症観察室待ちせず。
	 *         1 重症観察室待ち中
	 * @author kobayashi
	 * @since 2015/07/30
	 */
	public int iGetSereveInjuryObservationRoomWaitFlag()
	{
		return iSereveInjuryObservationRoomWaitFlag;
	}

	/**
	 * <PRE>
	 *   患者エージェントが集中治療室待ちをしているかのフラグを取得します。
	 * </PRE>
	 * @return 0 集中治療室待ちせず。
	 *         1 集中治療室待ち中
	 * @author kobayashi
	 * @since 2015/07/30
	 */
	public int iGetIntensiveCareUnitRoomWaitFlag()
	{
		return iIntensiveCareUnitRoomWaitFlag;
	}

	/**
	 * <PRE>
	 *   患者エージェントが高度治療室待ちをしているかのフラグを取得します。
	 * </PRE>
	 * @return 0 高度治療室待ちせず。
	 *         1 高度治療室待ち中
	 * @author kobayashi
	 * @since 2015/07/30
	 */
	public int iGetHighCareUnitRoomWaitFlag()
	{
		return iHighCareUnitRoomWaitFlag;
	}

	/**
	 * <PRE>
	 *   患者エージェントが一般病棟待ちをしているかのフラグを取得します。
	 * </PRE>
	 * @return 0 一般病棟待ちせず。
	 *         1 一般病棟待ち中
	 * @author kobayashi
	 * @since 2015/07/30
	 */
	public int iGetGeneralWardRoomWaitFlag()
	{
		return iGeneralWardRoomWaitFlag;
	}

	/**
	 * <PRE>
	 *   患者エージェントが診察待ちをしているかのフラグを設定します。
	 * </PRE>
	 * @param iConsultationRoomWaitFlagData 診察室待ちフラグ
	 *                                           0 待っていない
	 *                                           1 待っている
	 * @author kobayashi
	 * @since 2015/07/30
	 */
	public void vSetConsultationRoomWaitFlag( int iConsultationRoomWaitFlagData )
	{
		iConsultationRoomWaitFlag = iConsultationRoomWaitFlagData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが検査待ちをしているかのフラグを設定します。
	 * </PRE>
	 * @param iExaminationRoomWaitFlagData X線室待ちフラグ
	 *                                           0 待っていない
	 *                                           1 待っている
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetExaminationXRayRoomWaitFlag( int iExaminationRoomWaitFlagData )
	{
		iExaminationXRayRoomWaitFlag = iExaminationRoomWaitFlagData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが検査待ちをしているかのフラグを設定します。
	 * </PRE>
	 * @param iExaminationRoomWaitFlagData CT室待ちフラグ
	 *                                           0 待っていない
	 *                                           1 待っている
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetExaminationCTRoomWaitFlag( int iExaminationRoomWaitFlagData )
	{
		iExaminationCTRoomWaitFlag = iExaminationRoomWaitFlagData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが検査待ちをしているかのフラグを設定します。
	 * </PRE>
	 * @param iExaminationRoomWaitFlagData MRI室待ちフラグ
	 *                                           0 待っていない
	 *                                           1 待っている
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetExaminationMRIRoomWaitFlag( int iExaminationRoomWaitFlagData )
	{
		iExaminationMRIRoomWaitFlag = iExaminationRoomWaitFlagData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが検査待ちをしているかのフラグを設定します。
	 * </PRE>
	 * @param iExaminationRoomWaitFlagData 血管造影室待ちフラグ
	 *                                           0 待っていない
	 *                                           1 待っている
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetExaminationAngiographyRoomWaitFlag( int iExaminationRoomWaitFlagData )
	{
		iExaminationAngiographyRoomWaitFlag = iExaminationRoomWaitFlagData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが検査待ちをしているかのフラグを設定します。
	 * </PRE>
	 * @param iExaminationRoomWaitFlagData 超音波室待ちフラグ
	 *                                           0 待っていない
	 *                                           1 待っている
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetExaminationFastRoomWaitFlag( int iExaminationRoomWaitFlagData )
	{
		iExaminationFastRoomWaitFlag = iExaminationRoomWaitFlagData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが初療室待ちをしているかのフラグを設定します。
	 * </PRE>
	 * @param iEmergencyRoomWaitFlagData 初療室待ちフラグ
	 *                                   0 待っていない
	 *                                   1 待っている
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetEmergencyRoomWaitFlag( int iEmergencyRoomWaitFlagData )
	{
		iEmergencyRoomWaitFlag = iEmergencyRoomWaitFlagData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが観察室待ちをしているかのフラグを設定します。
	 * </PRE>
	 * @param iObservationRoomWaitFlagData  観察室待ちフラグ
	 *                                      0 待っていない
	 *                                      1 待っている
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetObservationRoomWaitFlag( int iObservationRoomWaitFlagData )
	{
		iObservationRoomWaitFlag = iObservationRoomWaitFlagData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが手術室待ちをしているかのフラグを設定します。
	 * </PRE>
	 * @param iOperationRoomWaitFlagData 手術室待ちフラグ
	 *                                   0 待っていない
	 *                                   1 待っている
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetOperationRoomWaitFlag( int iOperationRoomWaitFlagData )
	{
		iOperationRoomWaitFlag = iOperationRoomWaitFlagData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが重症観察室待ちをしているかのフラグを設定します。
	 * </PRE>
	 * @param iSereveInjuryObservationRoomWaitFlagData   重症観察室待ちフラグ
	 *                                                   0 待っていない
	 *                                                   1 待っている
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetSereveInjuryObservationRoomWaitFlag( int iSereveInjuryObservationRoomWaitFlagData )
	{
		iSereveInjuryObservationRoomWaitFlag = iSereveInjuryObservationRoomWaitFlagData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが集中治療室待ちをしているかのフラグを設定します。
	 * </PRE>
	 * @param iIntensiveCareUnitRoomWaitFlagData   集中治療室待ちフラグ
	 *                                             0 待っていない
	 *                                             1 待っている
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetIntensiveCareUnitRoomWaitFlag( int iIntensiveCareUnitRoomWaitFlagData )
	{
		iIntensiveCareUnitRoomWaitFlag = iIntensiveCareUnitRoomWaitFlagData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが高度治療室待ちをしているかのフラグを設定します。
	 * </PRE>
	 * @param iHighCareUnitRoomWaitFlagData      高度治療室待ちフラグ
	 *                                           0 待っていない
	 *                                           1 待っている
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetHighCareUnitRoomWaitFlag( int iHighCareUnitRoomWaitFlagData )
	{
		iHighCareUnitRoomWaitFlag = iHighCareUnitRoomWaitFlagData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが一般病棟待ちをしているかのフラグを設定します。
	 * </PRE>
	 * @param iGeneralWardRoomWaitFlagData       一般病棟待ちフラグ
	 *                                           0 待っていない
	 *                                           1 待っている
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetGeneralWardRoomWaitFlag( int iGeneralWardRoomWaitFlagData )
	{
		iGeneralWardRoomWaitFlag = iGeneralWardRoomWaitFlagData;
	}

	/**
	 * <PRE>
	 *   患者エージェントが観察されたか否かのフラグを設定します。
	 * </PRE>
	 * @param iObservedFlagData       観察されたかどうかフラグ
	 *                                 0 されていない
	 *                                 1 された
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void  vSetObservedFlag(int iObservedFlagData )
	{
		// TODO 自動生成されたメソッド・スタブ
		iObservedFlag = iObservedFlagData;
	}

	/**
	 * <PRE>
	 *    酸素飽和度を設定します。[％]
	 * </PRE>
	 * @param lfSpO2Data	SpO2値
	 */
	public void vSetSpO2( double lfSpO2Data )
	{
		lfSpO2 = lfSpO2Data;
	}

	/**
	 * <PRE>
	 *    呼吸回数を設定します。[回／分]
	 * </PRE>
	 * @param lfRrData	呼吸回数
	 */
	public void vSetRr( double lfRrData )
	{
		lfRr = lfRrData;
	}

	/**
	 * <PRE>
	 *    病院入院経過時間を取得します。[秒]
	 * </PRE>
	 * @return	総入院経過時間[秒]
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public double lfGetHospitalStayTime()
	{
		return lfStayHospitalTime;
	}

	/**
	 * <PRE>
	 *    集中治療室入院経過時間を取得します。[秒]
	 * </PRE>
	 * @return	集中治療室入院経過時間[秒]
	 * @author kobayashi
	 * @since 2015/10/20
	 */
	public double lfGetIntensiveCareUnitStayTime()
	{
		return lfIntensiveCareUnitStayTime;
	}

	/**
	 * <PRE>
	 *    高度治療室入院経過時間を取得します。[秒]
	 * </PRE>
	 * @return	高度治療室入院経過時間[秒]
	 * @author kobayashi
	 * @since 2015/10/20
	 */
	public double lfGetHighCareUnitStayTime()
	{
		return lfHighCareUnitStayTime;
	}

	/**
	 * <PRE>
	 *    一般病棟入院経過時間を取得します。[秒]
	 * </PRE>
	 * @return	一般病棟入院経過時間[秒]
	 * @author kobayashi
	 * @since 2015/10/20
	 */
	public double lfGetGeneralWardStayTime()
	{
		return lfGeneralWardStayTime;
	}

	public int iGetStayGeneralWardFlag()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iStayGeneralWardFlag;
	}

	public int iGetStayIntensiveCareUnitFlag()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iStayIntensiveCareUnitFlag;
	}

	public int iGetStayHighCareUnit()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iStayHighCareUnitFlag;
	}

	public void vSetStayHospitalFlag(int iStayHospitalFlagData )
	{
		// TODO 自動生成されたメソッド・スタブ
		iStayHospitalFlag = iStayHospitalFlagData;
	}

	public void vSetStayIntensiveCareUnitFlag(int iStayIntensiveCareUnitFlagData )
	{
		// TODO 自動生成されたメソッド・スタブ
		iStayIntensiveCareUnitFlag = iStayIntensiveCareUnitFlagData;
	}
	public void vSetStayGeneralWardFlag(int iStayGeneralWardFlagData )
	{
		// TODO 自動生成されたメソッド・スタブ
		iStayGeneralWardFlag = iStayGeneralWardFlagData;
	}

	public void vSetStayHighCareUnitFlag(int iStayHighCareUnitFlagData )
	{
		// TODO 自動生成されたメソッド・スタブ
		iStayHighCareUnitFlag = iStayHighCareUnitFlagData;
	}

	public void vSetStayHospital(int iStayHospitalFlagData)
	{
		// TODO 自動生成されたメソッド・スタブ
		iStayHospitalFlag = iStayHospitalFlagData;

	}

	public int iGetStayHospitalTimeFlag()
	{
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void vSetStayHospitalTimeFlag(int i)
	{
		// TODO 自動生成されたメソッド・スタブ

	}
	public int iGetStayHospitalFlag()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iStayHospitalFlag;
	}

	public int iGetObservedFlag()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iObservedFlag;
	}

	/**
	 * <PRE>
	 *   呼吸回数を返します。
	 *   1分間の呼吸回数となります。
	 * </PRE>
	 * @return	呼吸回数[分／回]
	 */
	public double lfGetRr()
	{
		return lfRr;
	}

	/**
	 * <PRE>
	 *   酸素飽和度を取得します。
	 * </PRE>
	 * @return	酸素飽和度[%]
	 */
	public double lfGetSpO2()
	{
		return lfSpO2;
	}

	/**
	 * <PRE>
	 *    体温を返します。
	 *    単位は℃となります。
	 * </PRE>
	 * @return	体温[℃]
	 */
	public double lfGetBodyTemperature()
	{
		return lfBodyTemperature;
	}

	/**
	 * <PRE>
	 *   バイタルサインの1つである脈拍を返却します。
	 *   1分間の脈拍とします。
	 * </PRE>
	 * @return	脈拍[回／分]
	 */
	public double lfGetPulse()
	{
		return lfPulse;
	}

	/**
	 * <PRE>
	 *    収縮期血圧（最大血圧）を返却します。
	 *    単位はmmHgです。
	 * </PRE>
	 * @return 収縮期血圧
	 */
	public double lfGetSbp()
	{
		return lfSbp;
	}

	/**
	 * <PRE>
	 *    拡張期血圧（最小血圧）を返却します。
	 *    単位はmmHgです。
	 * </PRE>
	 * @return 拡張期血圧
	 */
	public double lfGetDbp()
	{
		return lfDbp;
	}

	/**
	 * <PRE>
	 *    年齢を返却します。
	 * </PRE>
	 * @return 年齢
	 */
	public double lfGetAge()
	{
		return lfAge;
	}

	/**
	 * <PRE>
	 *    患者の性別を返却します。
	 * </PRE>
	 * @return 性別
	 */
	public int iGetSex()
	{
		return iSex;
	}

	/**
	 * <PRE>
	 *    外傷ヶ所の数を取得します。
	 * </PRE>
	 * @return 外傷の個所数
	 */
	public int iGetNumberOfTrauma()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iNumberOfTrauma;
	}

	/**
	 * <PRE>
	 *    白血球数を取得します。
	 * </PRE>
	 * @return 白血球の数
	 */
	public double lfGetLeucocyte()
	{
		// TODO 自動生成されたメソッド・スタブ
		return lfLeucocyte;
	}

	/**
	 * <PRE>
	 *    患者エージェントのGCSを取得します。
	 * </PRE>
	 * @return GCS値
	 */
	public double lfGetGcs()
	{
		// TODO 自動生成されたメソッド・スタブ
		return lfGcs;
	}

	/**
	 * <PRE>
	 *    看護師に対応してもらっているかどうかを表すフラグを返却します。
	 * </PRE>
	 * @return 看護師に対応して
	 */
	public int iGetNurseAttended()
	{
		return iNurseAttended;
	}

	/**
	 * <PRE>
	 *    看護師に対応してもらっているかを表すフラグを設定します。
	 * </PRE>
	 * @param iAttended 対応中フラグ
	 */
	public void vSetNurseAttended( int iAttended )
	{
		iNurseAttended = iAttended;
	}

	/**
	 * <PRE>
	 *   患者エージェントが現在どの部屋にいるかを設定します。
	 * </PRE>
	 * @param iLoc 部屋の番号
	 *  			1 診察室
	 *				2 手術室
	 *				3 初療室
	 *				4 観察室
	 *				5 重症観察室
	 *				6 集中治療室
	 *				7 高度治療室
	 *				8 一般病棟
	 * 				9 待合室
	 * 				10 X線室
	 * 				11 CT室
	 * 				12 MRI室
	 * 				13 血管造影検査室
	 */
	public void vSetLocation( int iLoc )
	{
		iLocation = iLoc;
	}

	/**
	 * <PRE>
	 *    患者エージェントが現在どの部屋にいるのかを取得します。
	 * </PRE>
	 * @return 患者の位置(部屋番号)
	 */
	public int iGetLocation()
	{
		return iLocation;
	}

	/**
	 * <PRE>
	 *    患者エージェントがなくなったかどうかを表すフラグを取得します。
	 * </PRE>
	 * @return 1 生存
	 *         0 死亡
	 */
	public int iGetSurvivalFlag()
	{
		return iSurvivalFlag;
	}

	/**
	 * <PRE>
	 *   検査が終了したことを表すフラグを返却します。
	 * </PRE>
	 * @return 1 終了
	 *         0 検査を実施していない
	 */
	public int iGetExaminataionFinishFlag()
	{
		return iExaminationFinishFlag;
	}

	/**
	 * <PRE>
	 *   患者が移動中かどうかを表すフラグを取得します。
	 * </PRE>
	 * @return 患者移動中か否かのフラグ
	 */
	public double lfGetMoveWaitingTime()
	{
		return lfMoveWaitingTime;
	}

	/**
	 * <PRE>
	 *    検査が終了したことを表すフラグを設定します。
	 * </PRE>
	 * @param iFinishFlag 1 終了
	 *                    0 検査を実施しない
	 */
	public void vSetExaminationFinishFlag( int iFinishFlag )
	{
		iExaminationFinishFlag = iFinishFlag;
	}

	/**
	 * <PRE>
	 *    患者が退院可能かどうかのフラグを取得します。
	 * </PRE>
	 * @return 1 退院可能
	 *         0 退院不可
	 */
	public int iGetDisChargeFlag()
	{
		return iDisChargeFlag;
	}

	/**
	 * <PRE>
	 *    患者エージェントが退院可能かどうかのフラグを設定します。
	 * </PRE>
	 * @param iFlag 0 まだ退院できない
	 *              1 退院可能
	 */
	public void vSetDisChargeFlag( int iFlag )
	{
		iDisChargeFlag = iFlag;
	}

	/**
	 * <PRE>
	 *    患者エージェントが他室へ移動できるかどうかのフラグを設定します。
	 * </PRE>
	 * @param iData 0 まだ移動できない、あるいは処置中
	 *              1 移動可能
	 */
	public void vSetMoveWaitFlag(int iData)
	{
		iMoveWaitFlag = iData;
	}

	/**
	 * <PRE>
	 *    観察室で観察プロセスを何回受けたのかをカウントします。
	 * </PRE>
	 * @return 観察プロセスを受けた回数
	 */
	public int iObservationWaitCount()
	{
		iObservationWait++;
		return iObservationWait;
	}

	/**
	 * <PRE>
	 *   観察室で観察プロセスを受けた回数を初期化します。
	 * </PRE>
	 */
	public void vInitObservationWaitCount()
	{
		iObservationWait = 0;
	}

	/**
	 * <PRE>
	 *    患者の部屋を移動する際に待ちます。
	 * </PRE>
	 * @return 移動時の待機フラグ
	 *         rue 待っている
	 *         false 待っていない
	 */
	public boolean isMoveWaitingTime()
	{
		if( lfMoveWaitingTime >= lfMoveTime )
		{
			return true;
		}
		return false;
	}

	/**
	 * <PRE>
	 *    患者の部屋を移動する際に待つフラグを設定します。
	 * </PRE>
	 * @param iData  移動時の待機フラグ
	 * 				 true 待っている
	 *      		 false 待っていない
	 */
	public void vSetMoveRoomFlag( int iData )
	{
		iMoveRoomFlag = iData;
	}

	/**
	 * <PRE>
	 *    患者の部屋を移動する際に待つ時間を設定します。
	 * </PRE>
	 * @param lfData  移動時の待機時間
	 */
	public void vSetMoveWaitingTime(double lfData )
	{
		// TODO 自動生成されたメソッド・スタブ
		lfMoveWaitingTime = lfData;
	}

	/**
	 * <PRE>
	 *    患者の入院日数を設定します。
	 * </PRE>
	 * @param lfHospitalStayDayData	入院日数
	 * @author kobayashi
	 * @since 2015/09/11
	 */
	public void vSetHospitalStayDay( double lfHospitalStayDayData )
	{
		lfHospitalStayDay = lfHospitalStayDayData;
	}

	/**
	 * <PRE>
	 *    患者の入院日数を取得します。
	 * </PRE>
	 * @return	入院日数
	 * @author kobayashi
	 * @since 2015/09/11
	 */
	public double lfGetHospitalStayDay()
	{
		return lfHospitalStayDay;
	}

	/**
	 * <PRE>
	 *    初期入院時のAIS値を設定します。
	 *    その中でも最大のAIS値を返却します。
	 * </PRE>
	 * @return 最大のAIS値
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public double lfSetHospitalStayInitAIS()
	{
		double lfMaxAIS = -10;
		// 入院開始時のAIS値を保持します。
		lfHospitalStayInitAISHead = lfInternalAISHead;
		lfMaxAIS = lfHospitalStayInitAISHead > lfMaxAIS ? lfHospitalStayInitAISHead : lfMaxAIS;
		lfHospitalStayInitAISFace = lfInternalAISFace;
		lfMaxAIS = lfHospitalStayInitAISFace > lfMaxAIS ? lfHospitalStayInitAISFace : lfMaxAIS;
		lfHospitalStayInitAISNeck = lfInternalAISNeck;
		lfMaxAIS = lfHospitalStayInitAISNeck > lfMaxAIS ? lfHospitalStayInitAISNeck : lfMaxAIS;
		lfHospitalStayInitAISThorax = lfInternalAISThorax;
		lfMaxAIS = lfHospitalStayInitAISThorax > lfMaxAIS ? lfHospitalStayInitAISThorax : lfMaxAIS;
		lfHospitalStayInitAISAbdomen = lfInternalAISAbdomen;
		lfMaxAIS = lfHospitalStayInitAISAbdomen > lfMaxAIS ? lfHospitalStayInitAISAbdomen : lfMaxAIS;
		lfHospitalStayInitAISSpine = lfInternalAISSpine;
		lfMaxAIS = lfHospitalStayInitAISSpine > lfMaxAIS ? lfHospitalStayInitAISSpine : lfMaxAIS;
		lfHospitalStayInitAISLowerExtremity = lfInternalAISLowerExtremity;
		lfMaxAIS = lfHospitalStayInitAISLowerExtremity > lfMaxAIS ? lfHospitalStayInitAISLowerExtremity : lfMaxAIS;
		lfHospitalStayInitAISUpperExtremity = lfInternalAISUpperExtremity;
		lfMaxAIS = lfHospitalStayInitAISUpperExtremity > lfMaxAIS ? lfHospitalStayInitAISUpperExtremity : lfMaxAIS;
		lfHospitalStayInitAISUnspecified = lfInternalAISUnspecified;
		lfMaxAIS = lfHospitalStayInitAISUnspecified > lfMaxAIS ? lfHospitalStayInitAISUnspecified : lfMaxAIS;
		return lfMaxAIS;
	}

	/**
	 * <PRE>
	 *    患者の入院時の改善割合を設定します。
	 * </PRE>
	 * @param lfAISRevisedSeriesData	タイムステップごとの患者の改善割合
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public void vSetRevisedSeries(double lfAISRevisedSeriesData )
	{
		lfAISRevisedSeries = lfAISRevisedSeriesData;
	}

	/**
	 * <PRE>
	 *    患者の入院時の改善割合を取得します。
	 * </PRE>
	 * @return 入院の改善割合
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public double lfGetRevisedSeries()
	{
		return lfAISRevisedSeries;
	}


	/**
	 * <PRE>
	 *    脈拍を設定します。
	 * </PRE>
	 * @param lfPulseData	脈拍値
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public void vSetPulse(double lfPulseData)
	{
		lfPulse = lfPulseData;
	}

	/**
	 * <PRE>
	 *   拡張期の血圧を設定します。
	 * </PRE>
	 * @param lfSbpData	拡張期血圧値
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public void vSetSbp(double lfSbpData)
	{
		lfSbp = lfSbpData;
	}

	/**
	 * <PRE>
	 *   収縮期の血圧を設定します。
	 * </PRE>
	 * @param lfDbpData	収縮期血圧値
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public void vSetDbp(double lfDbpData)
	{
		lfDbp = lfDbpData;
	}

	/**
	 * <PRE>
	 *    体温を設定します。
	 * </PRE>
	 * @param lfBodyTemperatureData	体温
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public void vSetBodyTemperature(double lfBodyTemperatureData)
	{
		lfBodyTemperature = lfBodyTemperatureData;
	}

	/**
	 * <PRE>
	 *    看護師エージェントIDを設定します。
	 * </PRE>
	 * @param iNurseAgentId	患者が対応受けている看護師エージェントのID
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public void vSetNurseAgent(long iNurseAgentId)
	{
		// TODO 自動生成されたメソッド・スタブ
		iNurseId = (int)iNurseAgentId;
	}

	/**
	 * <PRE>
	 *   現在対応してもらっている看護師のエージェントIDを取得します。
	 * </PRE>
	 * @return	患者が対応受けている看護師エージェントのID
	 */
	public int iGetNurseAgent()
	{
		return iNurseId;
	}

	/**
	 * <PRE>
	 *    医師エージェントIDを設定します。
	 * </PRE>
	 * @param iDoctorAgentId 医師エージェントのID
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public void vSetDoctorAgent(long iDoctorAgentId)
	{
		// TODO 自動生成されたメソッド・スタブ
		iDoctorId = (int)iDoctorAgentId;
	}

	/**
	 * <PRE>
	 *    高度治療室に入室したか否かのフラグです。
	 * </PRE>
	 * @return 高度治療室入室フラグ
	 */
	public int iGetEnterHighCareUnitFlag()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iEnterHighCareUnitFlag;
	}

	/**
	 * <PRE>
	 *    高度治療室に入室したか否かのフラグを設定します。
	 * </PRE>
	 * @param iSetData フラグ値 0 入室していない
	 *                          1 入室している
	 */
	public void vSetEnterHighCareUnitFlag( int iSetData )
	{
		// TODO 自動生成されたメソッド・スタブ
		iEnterHighCareUnitFlag = iSetData;
	}

	/**
	 * <PRE>
	 *    集中治療室に入室したか否かのフラグです。
	 * </PRE>
	 * @return 集中治療室入室フラグ
	 */
	public int iGetEnterIntensiveCareUnitFlag()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iEnterIntensiveCareUnitFlag;
	}

	/**
	 * <PRE>
	 *    集中治療室に入室したか否かのフラグを設定します。
	 * </PRE>
	 * @param iSetData フラグ値 0 入室していない
	 *                          1 入室している
	 */
	public void vSetEnterIntensiveCareUnitFlag( int iSetData )
	{
		// TODO 自動生成されたメソッド・スタブ
		iEnterIntensiveCareUnitFlag = iSetData;
	}

	/**
	 * <PRE>
	 *    一般病棟に入室したか否かのフラグです。
	 * </PRE>
	 * @return 一般病棟入室フラグ
	 */
	public int iGetEnterGeneralWardFlag()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iEnterGeneralWardFlag;
	}

	/**
	 * <PRE>
	 *    一般病棟に入室したか否かのフラグを設定します。
	 * </PRE>
	 * @param iSetData フラグ値 0 入室していない
	 *                          1 入室している
	 */
	public void vSetEnterGeneralWardFlag( int iSetData )
	{
		// TODO 自動生成されたメソッド・スタブ
		iEnterGeneralWardFlag = iSetData;
	}

	/**
	 * <PRE>
	 *    患者の緊急度を設定します。
	 * </PRE>
	 * @param iEmergency 緊急度
	 */
	public void vSetEmergencyLevel( int iEmergency )
	{
		iEmergencyLevel = iEmergency;
	}

	/**
	 * <PRE>
	 *    患者の緊急度を取得します。
	 * </PRE>
	 * @return 緊急度
	 */
	public int iGetEmergencyLevel()
	{
		return iEmergencyLevel;
	}

	/**
	 * <PRE>
	 *    到着時間を設定します。
	 * </PRE>
	 * @param lfData		救急部門に到達した時間
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public void vSetArraivalTime( double lfData )
	{
		lfArrivalTime = lfData;
	}

	/**
	 * <PRE>
	 *    到着時間を取得します。
	 * </PRE>
	 * @return 到達時間
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public double lfGetArraivalTime()
	{
		return lfArrivalTime;
	}

	/**
	 * <PRE>
	 *    到着時間に達したかどうかを判定します。
	 * </PRE>
	 * @return true 到達時間に達した。
	 *         false 到達時間に達していない
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public boolean isArraivalTime()
	{
		return lfArrivalTime*3600.0 >= lfTotalTime ? false : true;
	}

	/**
	 * <PRE>
	 *    外傷部位の中で最も最大のものを取得します。
	 * </PRE>
	 * @return AIS重症度最大値
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public double lfGetMaxAIS()
	{
		int i;
		double lfMaxAIS = -Double.MAX_VALUE;
		double plfAISs[] = new double[9];
		plfAISs[0] = lfInternalAISHead;
		plfAISs[1] = lfInternalAISFace;
		plfAISs[2] = lfInternalAISNeck;
		plfAISs[3] = lfInternalAISThorax;
		plfAISs[4] = lfInternalAISAbdomen;
		plfAISs[5] = lfInternalAISSpine;
		plfAISs[6] = lfInternalAISLowerExtremity;
		plfAISs[7] = lfInternalAISUpperExtremity;
		plfAISs[8] = lfInternalAISUnspecified;

		for( i = 0;i < 9; i++ )
		{
			if( lfMaxAIS < plfAISs[i] )
			{
				lfMaxAIS = plfAISs[i];
			}
		}
		return lfMaxAIS;
	}

	/**
	 * <PRE>
	 *    外傷部位の中で最も最小のものを取得します。
	 * </PRE>
	 * @return AIS重症度最小値
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public double lfGetMinAIS()
	{
		int i;
		double lfMinAIS = Double.MAX_VALUE;
		double plfAISs[] = new double[9];
		plfAISs[0] = lfInternalAISHead;
		plfAISs[1] = lfInternalAISFace;
		plfAISs[2] = lfInternalAISNeck;
		plfAISs[3] = lfInternalAISThorax;
		plfAISs[4] = lfInternalAISAbdomen;
		plfAISs[5] = lfInternalAISSpine;
		plfAISs[6] = lfInternalAISLowerExtremity;
		plfAISs[7] = lfInternalAISUpperExtremity;
		plfAISs[8] = lfInternalAISUnspecified;

		for( i = 0;i < 9; i++ )
		{
			if( lfMinAIS < plfAISs[i] )
			{
				lfMinAIS = plfAISs[i];
			}
		}
		return lfMinAIS;
	}

	/**
	 * <PRE>
	 *   経過時間を取得します。
	 * </PRE>
	 * @return 病院到着後経過時間
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public double lfGetTimeCourse()
	{
		return lfTimeCourse;
	}

	/**
	 * <PRE>
	 *    待ち時間を取得します。
	 * </PRE>
	 * @return 総待ち時間
	 * @author kobayashi
	 * @since 2015/11/12
	 */
	public double lfGetWaitTime()
	{
		return lfWaitTime;
	}


	/**
	 * <PRE>
	 *    患者の観察を受けるまでの時間を返します。
	 * </PRE>
	 * @return 観察待ち時間
	 */
	public double lfGetObservationTime()
	{
		// TODO 自動生成されたメソッド・スタブ
		return lfObservationWaitTime;
	}

	/**
	 * <PRE>
	 *    患者エージェントのログ出力を設定します。
	 * </PRE>
	 * @param log java標準のログ―クラスのインスタンス
	 */
	public void vSetLog(Logger log)
	{
		// TODO 自動生成されたメソッド・スタブ
		cPatientAgentLog = log;
	}

	/**
	 * <PRE>
	 *    シミュレーション終了時間を設定します。
	 * </PRE>
	 * @param lfEndTime シミュレーション終了時間
	 */
	public void vSetSimulationEndTime( double lfEndTime )
	{
		lfSimulationEndTime = lfEndTime;
	}

	/**
	 * <PRE>
	 *    逆シミュレーションモードを設定します。
	 * </PRE>
	 * @param iMode 0 通常シミュレーション
	 *              1 GUIモード
	 *              2 逆シミュレーション
	 */
	public void vSetInverseSimMode( int iMode )
	{
		iInverseSimMode = iMode;
	}

	/**
	 * <PRE>
	 *   患者エージェントの幅を設定します。
	 * </PRE>
	 * @param lfWidthData
	 */
	public void vSetWidth(double lfWidthData)
	{
		lfWidth = lfWidthData;
	}

	/**
	 * <PRE>
	 *   患者エージェントの高さを設定します。
	 * </PRE>
	 * @param lfHeightData
	 */
	public void vSetHeight(double lfHeightData)
	{
		lfHeight = lfHeightData;
	}

	/**
	 * <PRE>
	 *   患者エージェントの幅を取得します。
	 * </PRE>
	 * @return 患者エージェントの幅（楕円の場合は横の半径）
	 */
	public double lfGetWidth()
	{
		return lfWidth;
	}

	/**
	 * <PRE>
	 *   患者エージェントの高さを取得します。
	 * </PRE>
	 * @return 患者エージェントの高さ(楕円の場合は縦の半径)
	 */
	public double lfGetHeight()
	{
		return lfHeight;
	}

	/**
	 *<PRE>
	 *	現在在籍している病室から移動する病室までの移動経路を設定します。
	 *</PRE>
	 * @param ListRoute	移動ルート（ノードリスト）
	 */
	public void vSetMoveRoute(ArrayList<ERTriageNode> ListRoute)
	{
		int i;
		double lfDist = 0.0;
		double lfTempDist = 0.0;
		double[] alfDist;
		double[] alfVel = {0.0,0.0,0.0};
		double lfVelocity = 0.0;
		// TODO 自動生成されたメソッド・スタブ
		ArrayListRoute = ListRoute;
		// 現在のノードと次のノードを設定します。
		erCurNodeRoute = ArrayListRoute.get(0);
		erNextNodeRoute = ArrayListRoute.get(1);
		iLocNode = 1;

		alfDist = new double[ArrayListRoute.size()];

		// 患者エージェントの移動速度を設定します。
		for( i = 1;i < ArrayListRoute.size(); i++ )
		{
			lfTempDist = ArrayListRoute.get(i-1).getPosition().getDistance( ArrayListRoute.get(i).getPosition() );
			lfDist += lfTempDist;
			alfDist[i-1] = lfTempDist;
		}
		alfDist[i-1] = lfTempDist;
		// こっちは患者の速度を決める場合、移動時間は固定
//		for( i = 0;i < alfVel.length; i++ )
//		{
//			alfVel[i] = lfDist/lfMoveTime;
//		}
//		this.setVelocity( alfVel );
		// こちらは患者の移動時間を設定、患者の移動速度は一定
//		lfVelocity = Math.sqrt( this.getVelocity()[0]*this.getVelocity()[0]+this.getVelocity()[1]*this.getVelocity()[1]+this.getVelocity()[2]*this.getVelocity()[2] );
		lfVelocity = Math.sqrt( this.getVelocity()[0]*this.getVelocity()[0]+this.getVelocity()[1]*this.getVelocity()[1] );
//		lfVelocity = this.getVelocity()[0];
		lfMoveTime = lfDist/this.getVelocity()[0];

	}

	/**
	 * <PRE>
	 *    目的地へ移動します。
	 * </PRE>
	 * @return 目的地への
	 */
	private int iMoveRoute()
	{
		int i;
		Position posCur, posNext;
		double alfDirection[] = {0.0,0.0,0.0};
		double alfMoveDirection[] = {0.0,0.0,0.0};
		double lfDirection = 0.0;
		double lfCurrentTimeStep;
		double lfMoveDistX, lfMoveDistY, lfMoveDistZ;
		double t = 0.0;

		double lfJudgeCurPosition, lfJudgePrevPosition;
		double lfPrevX, lfPrevY;
		double lfDistX1, lfDistX2, lfDistY1, lfDistY2;

		boolean bExceedFlag = false;

		// まだ探索をしていない場合は、移動処理をしません。
		if( ArrayListRoute == null ) return 0;
		if( erCurNodeRoute == null || erNextNodeRoute == null ) return 0;

		lfPrevX = erCurNodeRoute.getPosition().getX();
		lfPrevY = erCurNodeRoute.getPosition().getY();
		lfCurrentTimeStep = this.getEngine().getLatestTimeStep()/1000.0;
		for( t = 0;t < lfCurrentTimeStep; t += 1.0 )
		{
			// 目的地に到着したかどうかを判定します。
			if( erCurNodeRoute == ArrayListRoute.get(ArrayListRoute.size()-1) )
			{
				// 到着した場合は1を出力します。
				return 1;
			}

			// 目的地の方向を取得します。
			posCur = erCurNodeRoute.getPosition();
			posNext = erNextNodeRoute.getPosition();
			alfDirection[0] = posNext.getX() - posCur.getX();
			alfDirection[1] = posNext.getY() - posCur.getY();
			alfDirection[2] = posNext.getZ() - posCur.getZ();

		// 次のノードへ移動します。

			// 次ノードへの方向ベクトル（単位ベクトル）を算出します。
			lfDirection = Position.getDistance2D(posCur.getX(), posCur.getY(), posNext.getX(), posNext.getY() );
			alfDirection[0] = alfDirection[0] / lfDirection;
			alfDirection[1] = alfDirection[1] / lfDirection;
			alfDirection[2] = alfDirection[2] / lfDirection;

			// 1ステップで進む距離を算出します。(1秒ごとの計算を実施。)
			lfMoveDistX = this.getVelocity()[0] * alfDirection[0];
			lfMoveDistY = this.getVelocity()[1] * alfDirection[1];
			lfMoveDistZ = this.getVelocity()[2] * alfDirection[2];
			// 位置を速度分移動させます。
			alfMoveDirection[0] = this.getX() + lfMoveDistX;
			alfMoveDirection[1] = this.getY() + lfMoveDistY;
//			alfMoveDirection[2] = this.getZ() + lfMoveDistZ;
			alfMoveDirection[2] = 0.0;

			// 位置を更新します。
			this.setPosition( alfMoveDirection );

			// 次のノードに触れたかどうかを判定します。
			lfJudgeCurPosition = posNext.getDistance( this.getPosition() );

			// 次に進むノードを超えていないかいるかを判定します。
			lfDistX1 = lfPrevX-posNext.getX();
			lfDistX2 = alfMoveDirection[0]-posNext.getX();
			lfDistY1 = lfPrevY-posNext.getY();
			lfDistY2 = alfMoveDirection[1]-posNext.getY();
			if( lfDistX1 != 0 && lfDistX2 != 0 )
			{
				// 次に向かうノードを超えたと判定します。
				if( lfDistX1*lfDistX2 < 0 )	bExceedFlag = true;
				// 次に向かうノードを超えていないと判定します。
				else						bExceedFlag = false;
			}
			else
			{
				// 次に向かうノードを超えたと判定します。
				if( lfDistY1*lfDistY2 < 0 )	bExceedFlag = true;
				// 次に向かうノードを超えていないと判定します。
				else						bExceedFlag = false;
			}
			// エージェントが目的地付近に到達したかどうかを判定します。
			if( lfJudgeCurPosition <= 30 || bExceedFlag == true )
			{

				double lfDist = 0.0;
				double lfPrevDist = Double.MAX_VALUE;
				// ノード範囲内にいる場合はエージェントをノード座標まで移動させます。
				for( lfDist = lfJudgeCurPosition; lfDist >= 0; lfDist = posNext.getDistance( this.getPosition() ) )
				{
					if( lfPrevDist < lfDist ) break;
					// 位置を更新します。
					alfMoveDirection[0] = this.getX() + lfMoveDistX;
					alfMoveDirection[1] = this.getY() + lfMoveDistY;
//					alfMoveDirection[2] = this.getZ() + lfMoveDistZ;
					alfMoveDirection[2] = 0.0;
					lfPrevDist = lfDist;
					this.setPosition( alfMoveDirection );
				}
				// 経路として到着目前に次の判定を行い、部屋内のどこかに割り当てます。
				if( iLocNode+1 >= ArrayListRoute.size() )
				{
					vSetLocationInTheRoom( alfMoveDirection[0], alfMoveDirection[1] );
				}
				// 触れた場合は現在のノード及び次のノードを更新します。
				if( iLocNode < ArrayListRoute.size() )
				{
					iLocNode++;
					erCurNodeRoute = erNextNodeRoute;
					if( erCurNodeRoute != ArrayListRoute.get(ArrayListRoute.size()-1) )
					{
						erNextNodeRoute = ArrayListRoute.get(iLocNode);
					}
					else
					{
						erNextNodeRoute = null;
					}
				}
			}
			lfPrevX = alfMoveDirection[0];
			lfPrevY = alfMoveDirection[1];
		}
		return 0;
	}

	private void vSetLocationInTheRoom( double lfMoveX, double lfMoveY )
	{
		// エージェントが近傍にいるかどうかを調べます。
		int i;
		int iFinishFlag = 0;
		double lfMaxDist = 0.0;
		double lfCurDist = 0.0;
		double lfCurX = 0.0;
		double lfCurY = 0.0;
		double lfX,lfY;
		double lfCurTheta = 0.0;
		double lfRoomX,lfRoomY,lfRoomWidth,lfRoomHeight;
		List<Agent> agents;

		// 待合室の時にのみ実行します。
		if( this.iGetLocation() == 9 )
		{
			lfCurX = this.getX();
			lfCurY = this.getY();
			agents = this.getEngine().getNeighborAgents(this, 30);
			// 部屋のサイズｎうち大きいほうの寸法を取得します。
			lfMaxDist = erDepartment.erGetWaitingRoom().iGetWidth() > erDepartment.erGetWaitingRoom().iGetHeight() ? erDepartment.erGetWaitingRoom().iGetWidth() : erDepartment.erGetWaitingRoom().iGetHeight();
			// どの位置ならば患者エージェントを配置できるのかを調査し設定します。
			for( i = 0; i < 100; i++ )
			{
				double lfTheta = Math.PI*2.0*rnd.NextUnif();
				for( Agent agent:agents)
				{
					// 患者エージェントのみを取得します。
					Object target = this.getEngine().getObjectById( agent.getId() );
					if( target instanceof ERPatientAgent )
					{
						ERPatientAgent targetAgent = (ERPatientAgent)target;
						// 指定位置にエージェントがいるかどうかを判定します。
						lfX = lfMaxDist*rnd.NextUnif()*Math.cos(lfTheta)+this.getX();
						lfY = lfMaxDist*rnd.NextUnif()*Math.sin(lfTheta)+this.getY();
						if( Position.getDistance2D( lfX, lfY, targetAgent.getX(), targetAgent.getY() ) > this.lfGetWidth() )
						{
							// 部屋の中に納まるように配置します。
							lfRoomX = erDepartment.erGetWaitingRoom().getX();
							lfRoomY = erDepartment.erGetWaitingRoom().getY();
							lfRoomWidth = erDepartment.erGetWaitingRoom().iGetWidth();
							lfRoomHeight = erDepartment.erGetWaitingRoom().iGetHeight();
							if( (lfRoomX-lfRoomWidth/2 <= lfX && lfX <= lfRoomX+lfRoomWidth/2-this.lfGetWidth()/2) &&
								(lfRoomY-lfRoomHeight/2+this.lfGetHeight()/2 <= lfY && lfY <= lfRoomY+lfRoomHeight/2) )
							{
								lfCurX = lfX;
								lfCurY = lfY;
								lfCurTheta = lfTheta;
								iFinishFlag = 1;
								break;
							}
						}
					}
				}
				if( iFinishFlag == 1 ) break;
			}
			// 移動先にエージェントがいて衝突してしまう場合は手前で停止します。
			lfMoveX = lfCurX;
			lfMoveY = lfCurY;
	//		lfMoveZ = 0.0;
			this.setPosition( lfMoveX, lfMoveY, 0.0 );
			iFinishFlag = 1;
		}
		else if( this.iGetLocation() == 8 )
		{
//			lfCurX = this.getX();
//			lfCurY = this.getY();
//			agents = this.getEngine().getNeighborAgents(this, 30);
//			// 部屋のサイズｎうち大きいほうの寸法を取得します。
//			lfMaxDist = erDepartment.erGetWaitingRoom().iGetWidth() > erDepartment.erGetWaitingRoom().iGetHeight() ? erDepartment.erGetWaitingRoom().iGetWidth() : erDepartment.erGetWaitingRoom().iGetHeight();
//			// どの位置ならば患者エージェントを配置できるのかを調査し設定します。
//			for( i = 0; i < 100; i++ )
//			{
//				double lfTheta = Math.PI*2.0*rnd.NextUnif();
//				for( Agent agent:agents)
//				{
//					// 患者エージェントのみを取得します。
//					Object target = this.getEngine().getObjectById( agent.getId() );
//					if( target instanceof ERPatientAgent )
//					{
//						ERPatientAgent targetAgent = (ERPatientAgent)target;
//						// 指定位置にエージェントがいるかどうかを判定します。
//						lfX = lfMaxDist*rnd.NextUnif()*Math.cos(lfTheta)+this.getX();
//						lfY = lfMaxDist*rnd.NextUnif()*Math.sin(lfTheta)+this.getY();
//						if( Position.getDistance2D( lfX, lfY, targetAgent.getX(), targetAgent.getY() ) > this.lfGetWidth() )
//						{
//							// 部屋の中に納まるように配置します。
//							lfRoomX = erDepartment.GetWaitingRoom().getX();
//							lfRoomY = erDepartment.erGetWaitingRoom().getY();
//							lfRoomWidth = erDepartment.erGetWaitingRoom().iGetWidth();
//							lfRoomHeight = erDepartment.erGetWaitingRoom().iGetHeight();
//							if( (lfRoomX-lfRoomWidth/2 <= lfX && lfX <= lfRoomX+lfRoomWidth/2-this.lfGetWidth()/2) &&
//								(lfRoomY-lfRoomHeight/2+this.lfGetHeight()/2 <= lfY && lfY <= lfRoomY+lfRoomHeight/2) )
//							{
//								lfCurX = lfX;
//								lfCurY = lfY;
//								lfCurTheta = lfTheta;
//								iFinishFlag = 1;
//								break;
//							}
//						}
//					}
//				}
//				if( iFinishFlag == 1 ) break;
//			}
//			// 移動先にエージェントがいて衝突してしまう場合は手前で停止します。
//			lfMoveX = lfCurX;
//			lfMoveY = lfCurY;
//			lfMoveZ = 0.0;
//			this.setPosition( lfMoveX, lfMoveY, 0.0 );
//			iFinishFlag = 1;
		}
	}

	/***
	 * <PRE>
	 *    患者の傷病状態を発生させる頻度を取得します。
	 * </PRE>
	 * @param iData 傷病状態発生乱数モード
	 *              0 一様乱数
	 *              1 正規乱数
	 *              2 ワイブル分布乱数
	 */
	public void vSetPatientRandomMode( int iData )
	{
		iInjuryRandomMode = iData;
	}

	/**
	 * <PRE>
	 *    部屋間を移動中であることを表すフラグを取得します。
	 * </PRE>
	 * @return 部屋移動中フラグ
	 *         0 移動していない
	 *         1 移動している
	 */
	public int iGetMoveRoomFlag()
	{
		return iMoveRoomFlag;
	}

	/**
	 * <PRE>
	 *    部屋間を移動するのに要する時間を取得します。
	 * </PRE>
	 * @return 部屋間移動時間
	 */
	public double lfGetMoveTime()
	{
		return lfMoveTime;
	}

	/**
	 * <PRE>
	 *    シミュレーションが開始してからの総時間を取得します。
	 * </PRE>
	 * @return シミュレーションの総経過時間
	 */
	public double lfGetTotalTime()
	{
		// TODO 自動生成されたメソッド・スタブ
		return lfTotalTime;
	}

	/**
	 * <PRE>
	 *    weibull分布関数の逆関数。
	 * </PRE>
	 * @param lfBeta	ワイブル分布用パラメータ1
	 * @param lfAlpha	ワイブル分布用パラメータ2
	 * @param lfRand	乱数値
	 * @return ワイブル分布関数の逆関数値
	 */
	private double InvWeibull( double lfAlpha, double lfBeta, double lfRand )
	{
		double lfRes = 0.0;
		lfRes = lfBeta*Math.pow( Math.log( 1.0/(1.0-lfRand) ), 1.0/lfAlpha );
		return lfRes;
	}

	/**
	 * <PRE>
	 *    weibull分布乱数を発生させます。-1.0以下、1.0以上が乱数を発生させた結果出力された場合、
	 *    再度乱数を発生させます。乱数発生回数の繰り返し回数は100回とします。
	 * </PRE>
	 * @param lfBeta	ワイブル分布用パラメータ1
	 * @param lfAlpha	ワイブル分布用パラメータ2
	 * @return ワイブル乱数[-1.0～1.0]
	 */
	public double weibullRand( double lfAlpha, double lfBeta )
	{
		double lfRes = 0.0;
		double lfRand = 0.0;
		int i;

		for( i = 0;i < 100; i++ )
		{
			if( rnd == null )	lfRand = Math.random();
			else 				lfRand = rnd.NextUnif();
			lfRand = lfRand >= 1.0 ? 0.9999999999 : lfRand;
			lfRes = InvWeibull( lfAlpha, lfBeta, lfRand );
			if( -1.0 <= lfRes && lfRes <= 1.0 )
				break;
		}
		// この場合は一様乱数で発生させます。
		if( i == 100 )
		{
			lfRes = rnd.NextUnif();
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 *    正規乱数を発生させます。-1.0以下、1.0以上が乱数を発生させた結果出力された場合、
	 *    再度乱数を発生させます。乱数発生回数の繰り返し回数は100回とします。
	 * </PRE>
	 * @return	正規乱数値
	 */
	public double normalRand()
	{
		double lfRes = 0.0;
		int i;

		for( i = 0;i < 100; i++ )
		{
			if( rnd == null )	lfRes = Math.random();
			else				lfRes = rnd.NextNormal();
			if( -1.0 <= lfRes && lfRes <= 1.0 ) break;
		}
		// この場合は一様乱数ではっせさせます。
		if( i == 100 )
		{
			lfRes = rnd.NextUnif();
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 *    診察した医者を取得します。
	 * </PRE>
	 * @return	診察室医師エージェントのインスタンス
	 */
	public ERDoctorAgent erGetConsultationDoctorAgent()
	{
		// TODO Auto-generated method stub
		return erConsultationDoctor;
	}

	/**
	 * <PRE>
	 *    診察してくれる医者を設定します。
	 * </PRE>
	 * @param erDAgent 担当医エージェント
	 */
	public void vSetConsultationDoctorAgent( ERDoctorAgent erDAgent )
	{
		erConsultationDoctor = erDAgent;
	}

	/**
	 * <PRE>
	 *   現在のなくなられた数を出力します。
	 * </PRE>
	 * @return 亡くなられた患者の数
	 */
	public int iGetDeathNum()
	{
		return iDeathNum;
	}

	/**
	 * <PRE>
	 *    患者の生存確率を取得します。
	 * </PRE>
	 * @return	患者の現時点での生存確率
	 */
	public double lfGetSurvivalProbability()
	{
		// TODO 自動生成されたメソッド・スタブ
		return lfSurvivalProbability;
	}

	public void vSetCriticalSection(Object cs )
	{
		// TODO 自動生成されたメソッド・スタブ
		csPatientCriticalSection = cs;
	}

	public void vSetRandom( Rand sfmtRandom )
	{
		rnd = sfmtRandom;
	}

	/**
	 * <PRE>
	 *    シミュレーション開始初めてトリアージを受けたときの緊急度を取得します。
	 * </PRE>
	 * @return	シミュレーション開始はじめの緊急度
	 */
	public int iGetStartEmergencyLevel()
	{
		return iStartEmergencyLevel;
	}

	/**
	 * <PRE>
	 *   ファイルへ書き込みを行います。1行ずつ実行します。
	 *   0 の場合はすべてのデータを書き込んでいきます。
	 *   1 の場合は最初と最後100秒分のデータを書き込みます。
	 * </PRE>
	 * @param iFlag			ファイル書き込みモード選択フラグ
	 * @param lfTime		現在のシミュレーション時間
	 * @throws IOException	ファイル書き込みエラー
	 */
	public synchronized void vWriteFile( int iFlag, double lfTime ) throws IOException
	{
		String strData = lfTotalTime + "," + lfTimeCourse + "," + lfMoveWaitingTime + "," + lfCurrentWaitTime + "," + lfCurrentObservationWaitTime + "," + lfCurrentConsultationTime +"," + lfCurrentOperationTime + "," + lfCurrentEmergencyTime + "," + lfIntensiveCareUnitStayTime + "," + lfHighCareUnitStayTime + "," + lfGeneralWardStayTime + "," + lfStayHospitalTime + "," + iSurvivalFlag + "," + iLocation + ",";
		strData += lfInternalAISHead + "," + lfInternalAISFace + "," + lfInternalAISNeck + "," + lfInternalAISThorax + "," + lfInternalAISAbdomen + "," + lfInternalAISSpine +"," + lfInternalAISUpperExtremity +"," + lfInternalAISLowerExtremity + "," + lfInternalAISUnspecified + "," + iEmergencyLevel + "," + iStartEmergencyLevel + ",";
		strData += lfSpO2 + "," + lfRr + "," + lfPulse + "," + lfSbp + "," + lfDbp + "," + lfBodyTemperature + "," + lfSurvivalProbability + ",";
		strData += iDisChargeFlag + ",";
		strData += lfXRayRoomStayTime + "," + lfCTRoomStayTime + "," + lfMRIRoomStayTime + "," + lfAngiographyRoomStayTime + "," + lfFastRoomStayTime;
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
			if( lfTime >= lfSimulationEndTime-100.0 )
			{
				csvWriteAgentData.vWrite( strData );
			}
		}
	}

	/**
	 * <PRE>
	 *   書き出しできなかったデータをすべて書き込みます。
	 *   0 の場合はすべてのデータを書き込んでいきます。
	 *   1 の場合は最初と最後100秒分のデータを書き込みます。
	 * </PRE>
	 * @param iFlag			ファイル書き込みモード選択フラグ
	 * @param lfTime		現在のシミュレーション時間
	 * @throws IOException	ファイル書き込みエラー
	 */
	public synchronized void vFlushFile( int iFlag ) throws IOException
	{
		String strData = lfTotalTime + "," + lfTimeCourse + "," + lfMoveWaitingTime + "," + lfWaitTime + "," + lfObservationWaitTime + "," + lfConsultationTime +"," + lfOperationTime + "," + lfEmergencyTime + "," + lfIntensiveCareUnitStayTime + "," + lfHighCareUnitStayTime + "," + lfGeneralWardStayTime + "," + lfStayHospitalTime + "," + iSurvivalFlag + "," + iLocation + ",";
		strData += lfInternalAISHead + "," + lfInternalAISFace + "," + lfInternalAISNeck + "," + lfInternalAISThorax + "," + lfInternalAISAbdomen + "," + lfInternalAISSpine +"," + lfInternalAISUpperExtremity +"," + lfInternalAISLowerExtremity + "," + lfInternalAISUnspecified + "," + iEmergencyLevel + "," + iStartEmergencyLevel + ",";
		strData += lfSpO2 + "," + lfRr + "," + lfPulse + "," + lfSbp + "," + lfDbp + "," + lfBodyTemperature + "," + lfSurvivalProbability + ",";
		strData += iDisChargeFlag + ",";
		strData += lfXRayRoomStayTime + "," + lfCTRoomStayTime + "," + lfMRIRoomStayTime + "," + lfAngiographyRoomStayTime + "," + lfFastRoomStayTime;
		// 終了時の書き込みか、特に指定していない場合
		if( csvWriteAgentData != null )
		{
			csvWriteAgentData.vWrite( strData );
			csvWriteAgentData.vClose();
		}
		// 開始時の書き込み
		if( csvWriteAgentStartData != null )
		{
			csvWriteAgentStartData.vWrite( strData );
			csvWriteAgentStartData.vClose();
		}
	}

	/**
	 * <PRE>
	 *   初期設定ファイルクラスのインスタンスを設定します。
	 * </PRE>
	 * @param initparam
	 */
	public void vSetInitParam(InitSimParam initparam)
	{
		// TODO 自動生成されたメソッド・スタブ
		initSimParam = initparam;
	}

	/**
	 * <PRE>
	 *   現在訪れたノードを取得します。
	 * </PRE>
	 * @return 現在のFuseNode
	 */
	public ERTriageNode erGetCurrentNode()
	{
		return erCurNodeRoute;
	}

	/**
	 * <PRE>
	 *   次に訪れるノードを取得します。
	 * </PRE>
	 * @return 次のFuseNode
	 */
	public ERTriageNode erGetNextNode()
	{
		return erNextNodeRoute;
	}

	/**
	 * <PRE>
	 *   経路を取得します。
	 * </PRE>
	 * @return 経路のノードリスト
	 */
	public ArrayList<ERTriageNode> erGetArrayListRoute()
	{
		return ArrayListRoute;
	}

	public void vSetErDepartment( ERDepartment erData )
	{
		erDepartment = erData;
	}

	public ERDepartment erGetErDepartment()
	{
		return erDepartment;
	}


	/**
	 * <PRE>
	 *   医師エージェントへ外傷状態を送信します。
	 * </PRE>
	 * @param erDoctorAgent		担当予定医師エージェント
	 * @param iFromAgentId  	送信先のエージェント（ここでは医師エージェント）
	 * @param iToAgentId		送信元のエージェント（ここでは患者エージェント）
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public void vSendToDoctorAgentMessage( ERDoctorAgent erDoctorAgent, int iFromAgentId, int iToAgentId )
	{
		Message mesSend;
		mesSend = new Message();
		mesSend.setData( new MessageFromPatToDoc() );
		mesSend.setFromAgent( iFromAgentId );
		mesSend.setToAgent( iToAgentId );
		((MessageFromPatToDoc)mesSend.getData()).vSetERPatientAgent( this );
		((MessageFromPatToDoc)mesSend.getData()).vSetInjuryHeadStatus( strInjuryHeadStatus );
		((MessageFromPatToDoc)mesSend.getData()).vSetInjuryFaceStatus( strInjuryFaceStatus );
		((MessageFromPatToDoc)mesSend.getData()).vSetInjuryNeckStatus( strInjuryNeckStatus );
		((MessageFromPatToDoc)mesSend.getData()).vSetInjuryThoraxStatus( strInjuryThoraxStatus );
		((MessageFromPatToDoc)mesSend.getData()).vSetInjuryAbdomenStatus( strInjuryAbdomenStatus );
		((MessageFromPatToDoc)mesSend.getData()).vSetInjurySpineStatus( strInjurySpineStatus );
		((MessageFromPatToDoc)mesSend.getData()).vSetInjuryUpperExtremityStatus( strInjuryUpperExtremityStatus );
		((MessageFromPatToDoc)mesSend.getData()).vSetInjuryLowerExtremityStatus( strInjuryLowerExtremityStatus );
		((MessageFromPatToDoc)mesSend.getData()).vSetInjuryUnspecifiedStatus( strInjuryUnspecifiedStatus );
		((MessageFromPatToDoc)mesSend.getData()).vSetWaitTime( lfWaitTime );
		((MessageFromPatToDoc)mesSend.getData()).vSetSurvivalFlag( iSurvivalFlag );
//		this.sendMessage( mesSend );

		// 対象となる医師エージェントが自分自身にメッセージ送信します。
		erDoctorAgent.vSendMessage( mesSend );
	}

	/**
	 * <PRE>
	 *   看護師エージェントへ外傷状態を送信します。
	 * </PRE>
	 * @param erNurseAgent		担当する看護師エージェント
	 * @param iFromAgentId  	送信先のエージェント（ここでは看護師エージェント）
	 * @param iToAgentId		送信元のエージェント（ここでは患者エージェント）
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public void vSendToNurseAgentMessage( ERNurseAgent erNurseAgent, int iFromAgentId, int iToAgentId )
	{
		Message mesSend;
		mesSend = new Message();
		mesSend.setData( new MessageFromPatToNurse() );
		mesSend.setFromAgent( iFromAgentId );
		mesSend.setToAgent( iToAgentId );
		((MessageFromPatToNurse)mesSend.getData()).vSetERPatientAgent( this );
		((MessageFromPatToNurse)mesSend.getData()).vSetInjuryHeadStatus( strInjuryHeadStatus );
		((MessageFromPatToNurse)mesSend.getData()).vSetInjuryFaceStatus( strInjuryFaceStatus );
		((MessageFromPatToNurse)mesSend.getData()).vSetInjuryNeckStatus( strInjuryNeckStatus );
		((MessageFromPatToNurse)mesSend.getData()).vSetInjuryThoraxStatus( strInjuryThoraxStatus );
		((MessageFromPatToNurse)mesSend.getData()).vSetInjuryAbdomenStatus( strInjuryAbdomenStatus );
		((MessageFromPatToNurse)mesSend.getData()).vSetInjurySpineStatus( strInjurySpineStatus );
		((MessageFromPatToNurse)mesSend.getData()).vSetInjuryUpperExtremityStatus( strInjuryUpperExtremityStatus );
		((MessageFromPatToNurse)mesSend.getData()).vSetInjuryLowerExtremityStatus( strInjuryLowerExtremityStatus );
		((MessageFromPatToNurse)mesSend.getData()).vSetInjuryUnspecifiedStatus( strInjuryUnspecifiedStatus );
		((MessageFromPatToNurse)mesSend.getData()).vSetWaitTime( lfWaitTime );
		((MessageFromPatToNurse)mesSend.getData()).vSetSurvivalFlag( iSurvivalFlag );
//		this.sendMessage( mesSend );

		// 対象となる看護師エージェントが自分自身にメッセージ送信します。
		erNurseAgent.vSendMessage( mesSend );
	}

	/**
	 * <PRE>
	 *   メッセージを送信します。
	 * </PRE>
	 * @param mess メッセージデータ
	 */
	public void vSendMessage( Message mess )
	{
		if( mesQueueData != null )
		{
			mesQueueData.add( mess );
		}
	}

	/**
	 * <PRE>
	 *   メッセージを受診します。
	 * </PRE>
	 * @return メッセージデータ
	 */
	public Message messGetMessage()
	{
		Message mes = null;
		if( mesQueueData != null )
		{
			mes = mesQueueData.poll();
		}
		return mes;
	}

	@Override
	public void action(long timeStep)
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;
		double lfSecond = 0.0;
		ERPatientAgentException epae;
		epae = new ERPatientAgentException();
		try
		{
			lfSecond += timeStep/1000.0;
			if( lfArrivalTime*3600.0 >= lfTotalTime )
			{
//				lfTimeCourse += lfSecond;
				lfTotalTime += lfSecond;
				return ;
			}
			// 患者エージェントを実行します。
			Message mess;
			// 看護師、医師からのメッセージを取得します。
			mess = messGetMessage();
			if( mess != null )
			{
				// 医師からのメッセージかどうかを判定します。
				if( mess.getData() instanceof MessageFromDocToPat )
				{
					// 診察内容を取得します。
					// 部屋の想定は診察室、初療室、手術室
					// 外傷の状況を取得します。
//					lfJudgedAISHead				= ((MessageFromDocToPat)mess.getData()).lfGetAISHead();
//					lfJudgedAISFace				= ((MessageFromDocToPat)mess.getData()).lfGetAISFace();
//					lfJudgedAISNeck				= ((MessageFromDocToPat)mess.getData()).lfGetAISNeck();
//					lfJudgedAISThorax			= ((MessageFromDocToPat)mess.getData()).lfGetAISThorax();
//					lfJudgedAISAbdomen			= ((MessageFromDocToPat)mess.getData()).lfGetAISAbdomen();
//					lfJudgedAISSpine			= ((MessageFromDocToPat)mess.getData()).lfGetAISSpine();
//					lfJudgedAISUpperExtremity	= ((MessageFromDocToPat)mess.getData()).lfGetAISUpperExtremity();
//					lfJudgedAISLowerExtremity	= ((MessageFromDocToPat)mess.getData()).lfGetAISLowerExtremity();
//					lfJudgedAISUnspecified		= ((MessageFromDocToPat)mess.getData()).lfGetAISUnspecified();
					// 担当した医師エージェントを取得します。
					iDoctorDepartment			= ((MessageFromDocToPat)mess.getData()).iGetDoctorDepartment();
					iDoctorId					= (int)mess.getFromAgentId();
//					iEmergencyLevel				= ((MessageFromDocToPat)mess.getData()).iGetEmergencyLevel();
				}
				// 看護師からのメッセージかどうかを判定します。
				if( mess.getData() instanceof MessageFromNurseToPat )
				{
					// 観察内容を取得します。
					// 外傷の状況を取得します。
//					lfJudgedAISHead				= ((MessageFromNurseToPat)mess.getData()).lfGetAISHead();
//					lfJudgedAISFace				= ((MessageFromNurseToPat)mess.getData()).lfGetAISFace();
//					lfJudgedAISNeck				= ((MessageFromNurseToPat)mess.getData()).lfGetAISNeck();
//					lfJudgedAISThorax			= ((MessageFromNurseToPat)mess.getData()).lfGetAISThorax();
//					lfJudgedAISAbdomen			= ((MessageFromNurseToPat)mess.getData()).lfGetAISAbdomen();
//					lfJudgedAISSpine			= ((MessageFromNurseToPat)mess.getData()).lfGetAISSpine();
//					lfJudgedAISUpperExtremity	= ((MessageFromNurseToPat)mess.getData()).lfGetAISUpperExtremity();
//					lfJudgedAISLowerExtremity	= ((MessageFromNurseToPat)mess.getData()).lfGetAISLowerExtremity();
//					lfJudgedAISUnspecified		= ((MessageFromNurseToPat)mess.getData()).lfGetAISUnspecified();
					// 担当した看護師エージェントを取得します。
					iNurseDepartment			= ((MessageFromNurseToPat)mess.getData()).iGetNurseDepartment();
					iNurseId					= (int)mess.getFromAgentId();
//					iEmergencyLevel				= ((MessageFromNurseToPat)mess.getData()).iGetEmergencyLevel();
				}
			}

			// 初回に判定された緊急度レベルを保持します。
			if( iEmergencyLevel != 0 && iStartEmergencyLevel == 0 )
			{
				iStartEmergencyLevel = iEmergencyLevel;
			}

			// 患者エージェントの生存状況を算出します。(患者到達時間を超えたら計算を開始します。)
			vImplementPatientAgent();

			// 待合室及び観察室にいる時間をタイムステップごとに加算します。
			if( iLocation == 9 )
			{
				if( iMoveWaitFlag == 0 )
				{
					lfWaitTime += lfSecond;
					lfCurrentWaitTime = lfWaitTime;
				}
				else
				{
					lfWaitTime = 0.0;
				}
			}
			if( iLocation == 4 || iLocation == 5 )
			{
				if( iMoveWaitFlag == 0 )
				{
					lfObservationWaitTime += lfSecond;
					lfCurrentObservationWaitTime = lfObservationWaitTime;
				}
				else
				{
					lfObservationWaitTime = 0.0;
				}
			}
			if( iLocation == 1 )
			{
				if( iMoveWaitFlag == 0 )
				{
					lfConsultationTime += lfSecond;
					lfCurrentConsultationTime = lfConsultationTime;
				}
				else
				{
					lfConsultationTime = 0.0;
				}
			}
			if( iLocation == 2 )
			{
				if( iMoveWaitFlag == 0 )
				{
					lfOperationTime += lfSecond;
					lfCurrentOperationTime = lfOperationTime;
				}
				else
				{
					lfOperationTime = 0.0;
				}
			}
			if( iLocation == 3 )
			{
				if( iMoveWaitFlag == 0 )
				{
					lfEmergencyTime += lfSecond;
					lfCurrentEmergencyTime = lfEmergencyTime;
				}
				else
				{
					lfEmergencyTime = 0.0;
				}
			}
			// 一般病棟、高度治療室、集中治療室の場合にカウントを行います。
			if( iLocation == 6 || iLocation == 7 || iLocation == 8 )
			{
				cPatientAgentLog.info(this.getId() + "," + this.iLocation + "," + "移動中," + lfMoveWaitingTime );
				if( iMoveWaitFlag == 0 )
				{
					lfStayHospitalTime += lfSecond;			// 入院時間
					if( iLocation == 6 )	lfIntensiveCareUnitStayTime += lfSecond;
					if( iLocation == 7 )	lfHighCareUnitStayTime += lfSecond;
					if( iLocation == 8 )	lfGeneralWardStayTime += lfSecond;
				}
				else
				{
//					lfStayHospitalTime = 0.0;
					if( iLocation == 6 )	lfIntensiveCareUnitStayTime = 0.0;
					if( iLocation == 7 )	lfHighCareUnitStayTime = 0.0;
					if( iLocation == 8 )	lfGeneralWardStayTime = 0.0;
				}
			}
			// 各検査室の場合に
			if( iLocation == 10 || iLocation == 11 || iLocation == 12 || iLocation == 13 || iLocation == 14 )
			{
				if( iMoveWaitFlag == 0 )
				{
					if( iLocation == 10 )	lfXRayRoomStayTime += lfSecond;
					if( iLocation == 11 )	lfCTRoomStayTime += lfSecond;
					if( iLocation == 12 )	lfMRIRoomStayTime += lfSecond;
					if( iLocation == 13 )	lfAngiographyRoomStayTime += lfSecond;
					if( iLocation == 14 )	lfFastRoomStayTime += lfSecond;
				}
				else
				{
					if( iLocation == 10 )	lfXRayRoomStayTime = 0.0;
					if( iLocation == 11 )	lfCTRoomStayTime = 0.0;
					if( iLocation == 12 )	lfMRIRoomStayTime = 0.0;
					if( iLocation == 13 )	lfAngiographyRoomStayTime = 0.0;
					if( iLocation == 14 )	lfFastRoomStayTime = 0.0;
				}
			}
			if( iMoveRoomFlag == 1 )
			{
				// 目的の病室へ移動します。
				int iArrivalRoomFlag = iMoveRoute();
				if( iArrivalRoomFlag == 1 )
				{
					cPatientAgentLog.info(this.getId() + "," + this.iLocation + "," + "到着" + lfMoveWaitingTime );
				}

				lfMoveWaitingTime += lfSecond;
				// デバック用
				if( lfMoveWaitingTime >= lfMoveTime )
				{
					cPatientAgentLog.info(this.getId() + "," + this.iLocation + "," + "なぜかカウントされ続け中" + lfMoveWaitingTime );
				}
			}
			else
			{
				if( lfMoveWaitingTime >= lfMoveTime )
				{
					lfMoveWaitingTime = lfMoveTime;
				}
				else
				{
					lfMoveWaitingTime = 0.0;
				}
//				cPatientAgentLog.info(this.getId() + "," + this.iLocation + "," + "移動終了中" + lfMoveWaitingTime );
			}
			// 病院到達後からの経過時間を算出します。
			lfTimeCourse += lfSecond;
			// シミュレーション開始からの経過時間を算出します。
			lfTotalTime += lfSecond;

			if( iSurvivalFlag == 1 )
			{
				if( iInverseSimMode == 0 )
				{
					// 終了100秒前からファイルに書き始めます。（長時間処理のため）
//					if( lfTotalTime >= lfSimulationEndTime-100.0 )
//					{
//						vWriteFile( 0 );
//					}
//					else if( lfTimeCourse <= 100.0 )
//					{
//						vWriteFile( 1 );
//					}
					vWriteFile( iFileWriteMode, lfTotalTime );
				}
			}
			// 退院したか、なくなられた場合、エージェントをシミュレーションから退場させる。
			if( iDisChargeFlag == 1 || iSurvivalFlag == 0 )
			{
				if( this.isExitAgent() == false )
				{
					this.getEngine().addExitAgent( this );
				}
				return;
			}
		}
		catch( ERPatientAgentException cERPae )
		{
			String strMethodName = cERPae.strGetMethodName();
			String strClassName = cERPae.strGetClassName();
			String strErrDetail = cERPae.strGetErrDetail();
			int iErrCode = cERPae.iGetErrCode();
			int iErrLine = cERPae.iGetErrorLine();
			cPatientAgentLog.warning( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
			for( i = 0;i < cERPae.getStackTrace().length; i++ )
			{
				String str = "クラス名" + "," + cERPae.getStackTrace()[i].getClassName();
				str += "メソッド名" + "," + cERPae.getStackTrace()[i].getMethodName();
				str += "ファイル名" + "," + cERPae.getStackTrace()[i].getFileName();
				str += "行数" + "," + cERPae.getStackTrace()[i].getLineNumber();
				cPatientAgentLog.warning( str );
			}
		}
		catch( IOException ioe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			epae.SetErrorInfo( ERPA_AGENT_FATAL_ERROR, "action", "ERPatientAgent", "不明、および致命的エラー", ste[0].getLineNumber() );
			// エラー詳細を出力
			String strMethodName = epae.strGetMethodName();
			String strClassName = epae.strGetClassName();
			String strErrDetail = epae.strGetErrDetail();
			int iErrCode = epae.iGetErrCode();
			int iErrLine = epae.iGetErrorLine();
			cPatientAgentLog.warning( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
			for( i = 0;i < ioe.getStackTrace().length; i++ )
			{
				String str = "クラス名" + "," + ioe.getStackTrace()[i].getClassName();
				str += "メソッド名" + "," + ioe.getStackTrace()[i].getMethodName();
				str += "ファイル名" + "," + ioe.getStackTrace()[i].getFileName();
				str += "行数" + "," + ioe.getStackTrace()[i].getLineNumber();
				cPatientAgentLog.warning( str );
			}
		}
	}
}
