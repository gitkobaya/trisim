package triage.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import jp.ac.nihon_u.cit.su.furulab.fuse.Message;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import utility.csv.CCsv;

public class ERDoctorAgent extends Agent{

	private static final long serialVersionUID = 3036002566514283755L;

	public static final int ERDA_SUCCESS					= 0;
	public static final int ERDA_FATAL_ERROR				= -101;
	public static final int ERDA_MEMORYALLOCATE_ERROR		= -102;
	public static final int ERDA_NULLPOINT_ERROR			= -103;
	public static final int ERDA_INVALID_ARGUMENT_ERROR		= -104;
	public static final int ERDA_INVALID_DATA_ERROR			= -105;
	public static final int ERDA_ARRAY_INDEX_ERROR			= -106;
	public static final int ERDA_ZERO_DIVIDE_ERROR			= -107;

	// 医師パラメータ(定数)
	int iDoctorId;								// 医師のID
	int iDoctorDepartment;						// 医師の所属部門
	int iSurgeon;								// 執刀医かどうか
	int iRoomNumber;							// 所属している部屋番号
	double lfYearExperience;					// 経験年数
	double lfConExperience;						// 経験年数重みパラメータ
	double lfConExperienceAIS;					// 経験年数重みパラメータ(重症度)
	double lfExperienceRate1;					// 経験年数パラメータその１
	double lfExperienceRate2;					// 経験年数パラメータその２
	double lfExperienceRateAIS1;				// 経験年数パラメータその１(重症度)
	double lfExperienceRateAIS2;				// 経験年数パラメータその２(重症度)
	double lfExperienceRateOp1;					// 経験年数パラメータその１
	double lfConExperienceOp;					// 経験年数パラメータその２
	double lfExperienceRateOp2;					// 経験年数パラメータその３
	double lfTiredRate;							// 疲労度
	double lfConTired1;							// 疲労度パラメータ1
	double lfConTired2;							// 疲労度パラメータ2
	double lfConTired3;							// 疲労度パラメータ3
	double lfConTired4;							// 疲労度パラメータ4
	double lfRevisedOperationRate;				// 手術による傷病状態の改善度
	double lfRevisedEmergencyRate;				// 処置による傷病状態の改善度
	double lfAssociationRate;					// 連携度
	double lfConsultationAssociateRate;			// 診察室における連携度
	double lfOperationAssociateRate;			// 手術室における連携度

	// 医師パラメータ(変数)
	double lfConsultationTime;					// 診察時間
	double lfCurrentConsultationTime;			// 今回の診察時間
	double lfTotalConsultationTime;				// 診察総時間
	double lfOperationTime;						// 手術時間
	double lfCurrentOperationTime;				// 今回の手術時間
	double lfTotalOperationTime;				// 手術総時間
	double lfCurrentEmergencyTime;				// 現在の初療室対応時間
	double lfEmergencyTime;						// 初療室対応時間
	double lfTotalEmergencyTime;				// 初療室総対応時間
	double lfCurrentPassOverTime;				// 現在の医師が作業を開始してからの時間
	int iConsultationAttending;					// 診察室対応中フラグ
	int iOperationAttending;					// 手術室対応中フラグ
	int iEmergencyAttending;					// 初療室対応中フラグ
	int iEmergencyLevel;						// 緊急度
	int iTotalConsultationNum;					// 医師が患者を観察した回数
	int iTotalOperationNum;						// 医師が患者を手術した回数
	int iTotalEmergencyNum;						// 医師が患者を初療室で対応した回数

	// メッセージ取得関連パラメータ
	int iTriageProtocol;						// トリアージプロトコル
	int iTriageProtocolLevel;					// トリアージプロトコルのレベル
	int iTriageProcessFlag;						// トリアージを実施するか否か

	int iNurseDepartment;						// 看護師の所属部門
	int iNurseId;								// 看護師ID
	int iPatientLocation;						// 患者が現在いる部屋
	int iPatientId;								// 担当患者ID
	int iClinicalEngineerDepartment;			// 検査した医療技師所属室
	int iClinicalEngineerId;					// 検査した医療技士のID
	int iFromDoctorId;							// 担当した医師のID
	int iFromDoctorDepartment;					// 担当した医師の所属部門

	double lfObservationTime;					// 観察時間
	double lfWaitTime;							// 患者の待ち時間
	double lfExaminationTime;					// 検査時間
	int iExaminationFinishFlag;					// 検査完了フラグ

	// 医療技師に関わるパラメータ
	int iRequestExamination;					// 依頼する検査を表す変数
	int aiRequestAnatomys[];					// 依頼する検査をする部位
	int iRequestExaminationNum;					// 依頼する検査部位数

	// 患者に関わるパラメータ
	double lfJudgedAISHead;						// 頭部のAIS
	double lfJudgedAISFace;						// 顔面のAIS
	double lfJudgedAISNeck;						// 頸部（首）のAIS
	double lfJudgedAISThorax;					// 胸部のAIS
	double lfJudgedAISAbdomen;					// 腹部のAIS
	double lfJudgedAISSpine;					// 脊椎のAIS
	double lfJudgedAISUpperExtremity;			// 上肢のAIS
	double lfJudgedAISLowerExtremity;			// 下肢のAIS
	double lfJudgedAISUnspecified;				// 特定部位でない。（体表・熱傷・その他外傷）
	String strInjuryHeadStatus;					// 患者が訴える頭部AIS値
	String strInjuryFaceStatus;					// 患者が訴える顔面AIS値
	String strInjuryNeckStatus;					// 患者が訴える頸部AIS値;
	String strInjuryThoraxStatus;				// 患者が訴える胸部AIS値
	String strInjuryAbdomenStatus;				// 患者が訴える腹部AIS値
	String strInjurySpineStatus;				// 患者が訴える脊椎AIS値
	String strInjuryUpperExtremityStatus;		// 患者が訴える上肢AIS値
	String strInjuryLowerExtremityStatus;		// 患者が訴える下肢AIS値
	String strInjuryUnspecifiedStatus;			// 患者が訴える表面、熱傷、その他外傷AIS値
	double lfExamAISHead;						// 医療技師からの検査結果頭部AIS値
	double lfExamAISFace;						// 医療技師からの検査結果顔面AIS値
	double lfExamAISNeck;						// 医療技師からの検査結果頸部AIS値
	double lfExamAISThorax;						// 医療技師からの検査結果胸部AIS値
	double lfExamAISAbdomen;					// 医療技師からの検査結果腹部AIS値
	double lfExamAISSpine;						// 医療技師からの検査結果脊椎AIS値
	double lfExamAISUpperExtremity;				// 医療技師からの検査結果上肢AIS値
	double lfExamAISLowerExtremity;				// 医療技師からの検査結果下肢AIS値
	double lfExamAISUnspecified;				// 医療技師からの検査結果表面、熱傷、その他外傷AIS値
	double lfUpperExtremityNRS;					// 上肢痛みの強さのスケール
	double lfLowerExtremityNRS;					// 下肢痛みの強さのスケール
	double lfUnspecifiedNRS;					// 体表、熱傷、その他外傷の痛みの強さのスケール
	double lfSpineNRS;							// 脊椎痛みの強さのスケール
	double lfAbdomenNRS;						// 腹部痛みの強さのスケール
	double lfThoraxNRS;							// 胸部痛みの強さのスケール
	double lfNeckNRS;							// 頸部痛みの強さのスケール
	double lfFaceNRS;							// 顔面痛みの強さのスケール
	double lfHeadNRS;							// 頭部痛みの強さのスケール
	double lfPatientGcsLevel;					// 患者の意識レベル
	double lfPatientRr;							// 患者の呼吸回数
	double lfPatientSpO2;						// 患者の動脈血山荘飽和度(SpO2)
	double lfFatigue;							// 医師の疲労度
	int iCalcTiredFlag;							// 疲労度計算時の初めて計算したか否か

	ArrayList<Long> ArrayListDoctorAgentIds;			// 全医師のID
	ArrayList<Long> ArrayListNurseAgentIds;				// 全看護師のID
	ArrayList<Long> ArrayListClinicalEngineerAgentIds;	// 医療技師のID
	ArrayList<Long> ArrayListPatientAgentIds;			// 全患者のID

	ERPatientAgent erPatientAgent;						// 現在診察、あるいは手術をしている患者エージェント
	ERPatientAgent erPrevPatientAgent;					// 現在診察、あるいは手術をしている患者エージェント

	Queue<Message> mesQueueData;						// メッセージキューを保持する変数

	double lfTimeCourse;								// 経過時間
	int iPatientMoveWaitFlag;							// 患者移動フラグ

	utility.sfmt.Rand rnd;											// 乱数クラス

	CCsv csvWriteAgentData;								// 終了データファイル出力
	CCsv csvWriteAgentStartData;						// 開始データファイル出力

	private Logger cDoctorAgentLog;						// 医師エージェントのログ出力
	private double lfTotalTime;							// 総時間
	private double lfSimulationEndTime;					// シミュレーション終了時間

	private int iInverseSimMode;						// 逆シミュレーションモード

	private Object erDoctorCriticalSection;				// クリティカルセクション用

	private int iFileWriteMode;							// 長時間シミュレーションファイル出力モード

	/**
	 * <PRE>
	 *    コンストラクタ
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public ERDoctorAgent()
	{
		vInitialize();
	}

	void vInitialize()
	{
		String strFileName = "";
//		long seed;
//		seed = (long)(Math.random()*Long.MAX_VALUE);
//		rnd = null;
//		rnd = new sfmt.Rand( (int)seed );
		// 医師パラメータ(定数)
		iDoctorId							= 0;		// 医師のID
		iDoctorDepartment					= 0;		// 医師の所属部門
		lfYearExperience					= 5.0;
		lfConExperience						= 0.61;
		lfExperienceRate1					= 2.1;
		lfExperienceRate2					= 0.9;
		lfConExperienceAIS					= 0.14;		// 経験年数重みパラメータ(重症度用)
		lfExperienceRateAIS1				= 0.2;		// 経験年数パラメータその１(重症度用)
		lfExperienceRateAIS2				= 1.1;		// 経験年数パラメータその２(重症度用)
		lfExperienceRateOp1					= 0.9;		// 経験年数パラメータその１（手術における傷病重症度改善用）
//		lfConExperienceOp					= 0.16;		// 経験年数パラメータその２（手術における傷病重症度改善用）(5年で半分)
		lfConExperienceOp					= 0.05;		// 経験年数パラメータその２（手術における傷病重症度改善用）(5年で8割り)
		lfExperienceRateOp2					= 0.1;		// 経験年数パラメータその３（手術における傷病重症度改善用）
		lfTiredRate							= 0;		// 疲労度
		lfConTired1							= 0;		// 疲労度重みパラメータ1
		lfConTired2							= 0;		// 疲労度重みパラメータ2
		lfConTired3							= 0;		// 疲労度重みパラメータ3
		lfConTired4							= 0;		// 疲労度重みパラメータ4
		iSurgeon							= 0;		// 執刀医かどうか
		lfRevisedOperationRate				= 0.6666666;// 手術による傷病状態の改善度
		lfRevisedEmergencyRate				= 0.8;		// 処置による傷病状態の改善度
		lfAssociationRate					= 1.0;		// 連携度
		lfConsultationAssociateRate			= 1.0;		// 診察室における連携度
		lfOperationAssociateRate			= 1.0;		// 手術室における連携度

		// 医師パラメータ(変数)
		lfConsultationTime					= 0;		// 診察時間
		lfTotalConsultationTime				= 0;		// 診察総時間
		lfOperationTime						= 0;		// 手術時間
		lfTotalOperationTime				= 0;		// 手術総時間
		lfEmergencyTime						= 0;		// 初療室対応時間
		lfTotalEmergencyTime				= 0;		// 初療室総対応時間
		lfCurrentPassOverTime				= 0;		// 現在の医師が作業を開始してからの時間
		iConsultationAttending				= 0;		// 診察室対応中フラグ
		iOperationAttending					= 0;		// 手術室対応中フラグ
		iEmergencyAttending					= 0;		// 初療室対応中フラグ
		iEmergencyLevel						= 6;		// 緊急度
		iTotalConsultationNum				= 0;		// 医師が患者を観察した回数
		iTotalOperationNum					= 0;		// 医師が患者を手術した回数
		iTotalEmergencyNum					= 0;		// 医師が患者を初療室で対応した回数

		// メッセージ取得関連パラメータ
		iTriageProtocol						= 0;		// トリアージプロトコル
		iTriageProtocolLevel				= 0;		// トリアージプロトコルのレベル
		iTriageProcessFlag					= 0;		// トリアージを実施するか否か

		iNurseDepartment					= 0;		// 看護師の所属部門
		iNurseId							= 0;		// 看護師ID
		iPatientLocation					= 0;		// 患者が現在いる部屋
		iPatientId							= 0;		// 担当患者ID
		iClinicalEngineerDepartment			= 0;		// 検査した医療技師所属室
		iClinicalEngineerId					= 0;		// 検査した医療技士のID
		iExaminationFinishFlag				= 0;		// 検査完了フラグ

		lfObservationTime					= 0.0;		// 観察時間
		lfWaitTime							= 0.0;		// 患者の待ち時間
		lfExaminationTime					= 0.0;		// 検査時間

		// 医療技師に関わるパラメータ
		iRequestExamination					= 0;			// 依頼する検査を表す変数
		aiRequestAnatomys					= new int[9];	// 依頼する検査をする部位
		iRequestExaminationNum				= 0;			// 依頼する検査部位数

		// 患者に関わるパラメータ
		lfJudgedAISHead						= 0;		// 頭部のAIS
		lfJudgedAISFace						= 0;		// 顔面のAIS
		lfJudgedAISNeck						= 0;		// 頸部（首）のAIS
		lfJudgedAISThorax					= 0;		// 胸部のAIS
		lfJudgedAISAbdomen					= 0;		// 腹部のAIS
		lfJudgedAISSpine					= 0;		// 脊椎のAIS
		lfJudgedAISUpperExtremity			= 0;		// 上肢のAIS
		lfJudgedAISLowerExtremity			= 0;		// 下肢のAIS
		lfJudgedAISUnspecified				= 0;		// 特定部位でない。（体表・熱傷・その他外傷）
		strInjuryHeadStatus					= "";		// 患者が訴える頭部AIS値
		strInjuryFaceStatus					= "";		// 患者が訴える顔面AIS値
		strInjuryNeckStatus					= "";		// 患者が訴える頸部AIS値;
		strInjuryThoraxStatus				= "";		// 患者が訴える胸部AIS値
		strInjuryAbdomenStatus				= "";		// 患者が訴える腹部AIS値
		strInjurySpineStatus				= "";		// 患者が訴える脊椎AIS値
		strInjuryUpperExtremityStatus		= "";		// 患者が訴える上肢AIS値
		strInjuryLowerExtremityStatus		= "";		// 患者が訴える下肢AIS値
		strInjuryUnspecifiedStatus			= "";		// 患者が訴える表面、熱傷、その他外傷AIS値
		lfExamAISHead						= 0;		// 医療技師からの検査結果頭部AIS値
		lfExamAISFace						= 0;		// 医療技師からの検査結果顔面AIS値
		lfExamAISNeck						= 0;		// 医療技師からの検査結果頸部AIS値
		lfExamAISThorax						= 0;		// 医療技師からの検査結果胸部AIS値
		lfExamAISAbdomen					= 0;		// 医療技師からの検査結果腹部AIS値
		lfExamAISSpine						= 0;		// 医療技師からの検査結果脊椎AIS値
		lfExamAISUpperExtremity				= 0;		// 医療技師からの検査結果上肢AIS値
		lfExamAISLowerExtremity				= 0;		// 医療技師からの検査結果下肢AIS値
		lfExamAISUnspecified				= 0;		// 医療技師からの検査結果表面、熱傷、その他外傷AIS値
		lfPatientSpO2						= 0.0;		// 患者の動脈血山荘飽和度(SpO2)
		lfUpperExtremityNRS					= 0.0;		// 上肢痛みの強さのスケール
		lfLowerExtremityNRS					= 0.0;		// 下肢痛みの強さのスケール
		lfUnspecifiedNRS					= 0.0;		// 体表、熱傷、その他外傷の痛みの強さのスケール
		lfSpineNRS							= 0.0;		// 脊椎痛みの強さのスケール
		lfAbdomenNRS						= 0.0;		// 腹部痛みの強さのスケール
		lfThoraxNRS							= 0.0;		// 胸部痛みの強さのスケール
		lfNeckNRS							= 0.0;		// 頸部痛みの強さのスケール
		lfFaceNRS							= 0.0;		// 顔面痛みの強さのスケール
		lfHeadNRS							= 0.0;		// 頭部痛みの強さのスケール
		lfFatigue							= 0.0;		// 医師の疲労度
		iCalcTiredFlag						= 0;		// 疲労度計算時の初めて計算したか否か

		ArrayListDoctorAgentIds				= new ArrayList<Long>();// 全医師のID
		ArrayListNurseAgentIds				= new ArrayList<Long>();// 全看護師のID
		ArrayListPatientAgentIds			= new ArrayList<Long>();// 全患者のID
		ArrayListClinicalEngineerAgentIds	= new ArrayList<Long>();// 医療技師のID

		erPatientAgent						= null;		// 現在診察、あるいは手術をしている患者エージェント

		mesQueueData						= new LinkedList<Message>();

		lfTimeCourse 						= 0.0;		// 経過時間
		iPatientMoveWaitFlag				= 0;		// 患者移動フラグ

		iFileWriteMode						= 0;

//		try
//		{
//			csvWriteAgentData					= new CCsv();
//			strFileName							= "./er/dc/erdc_start" + this.getId() + ".csv";
//			csvWriteAgentData.vOpen( strFileName, "write");
//			csvWriteAgentStartData				= new CCsv();
//			strFileName							= "./er/dc/erdc_end" + this.getId() + ".csv";
//			csvWriteAgentStartData.vOpen( strFileName, "write");
//		}
//		catch( IOException ioe )
//		{
//
//		}
	}

	/**
	 * <PRE>
	 *    ファイルの読み込みを行います。
	 * </PRE>
	 * @param iFileWriteMode	ファイル書き込みモード
	 * 							0 すべて、1 最初と最後
	 * @throws IOException		ファイル書き込みエラー
	 */
	public void vSetReadWriteFile( int iFileWriteMode ) throws IOException
	{
		String strFileName = "";
		this.iFileWriteMode = iFileWriteMode;
		if( iFileWriteMode == 1 )
		{
			csvWriteAgentData					= new CCsv();
			strFileName							= "./er/dc/erdc_start" + this.getId() + ".csv";
			csvWriteAgentData.vOpen( strFileName, "write");
			csvWriteAgentStartData				= new CCsv();
			strFileName							= "./er/dc/erdc_end" + this.getId() + ".csv";
			csvWriteAgentStartData.vOpen( strFileName, "write");
		}
		else
		{
			csvWriteAgentData					= new CCsv();
			strFileName							= "./er/dc/erdc_end" + this.getId() + ".csv";
			csvWriteAgentData.vOpen( strFileName, "write");
		}
	}

	/**
	 * <PRE>
	 *    終了処理を実行します。
	 * </PRE>
	 * @throws IOException java標準エラー
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
		rnd = null;
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
		if( ArrayListClinicalEngineerAgentIds != null )
		{
			ArrayListClinicalEngineerAgentIds.clear();				// 全看護師のID
			ArrayListClinicalEngineerAgentIds = null;
		}
		if( ArrayListPatientAgentIds != null )
		{
			ArrayListPatientAgentIds.clear();
			ArrayListPatientAgentIds = null;
		}
		if( mesQueueData != null )
		{
			mesQueueData.clear();						// メッセージキューを保持する変数
			mesQueueData = null;
		}
	}

	/**
	 * <PRE>
	 *    FUSEエンジンにエージェントを登録します。
	 * </PRE>
	 * @param engine シミュレーションエンジン
	 */
	public void vSetSimulationEngine( SimulationEngine engine )
	{
		engine.addAgent(this);
	}

	/**
	 * <PRE>
	 *    医師の診察プロセスを実行します。
	 * </PRE>
	 * @param erPatientAgent 診察をうける患者エージェント
	 * @return 1 診察終了待合室待機
	 *         2 検査の必要あり検査室へ移動
	 *         3 入院処置必要、一般病棟へ
	 *         4 手術室必要、手術室へ
	 *         5 緊急処置必要、初療室へ
	 * @throws ERDoctorAgentException 医師エージェント例外クラス
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public int iImplementConsultationProcess( ERPatientAgent erPatientAgent ) throws ERDoctorAgentException
	{
		int iProcessResult = 0;
		int iEmergencyLevel1 = 6;
		int iEmergencyLevel2 = 6;

		// 問診の実施をします。
		iEmergencyLevel1 = iMedicalInterview( erPatientAgent );

		// 診察の実施をします。
		iEmergencyLevel2 = iImplementConsultation( erPatientAgent );

		iEmergencyLevel = iEmergencyLevel1 > iEmergencyLevel2 ? iEmergencyLevel2 : iEmergencyLevel1;
		// 診察時間に経験年数を反映させる。(経験年数が浅いと過小評価するようにする。)
		iEmergencyLevel = (int)(lfCalcExperienceAIS()*iEmergencyLevel);
		erPatientAgent.vSetEmergencyLevel( iEmergencyLevel );

		if( iEmergencyLevel <= 3 )
		{
			// 緊急性があるので、初療室へ患者を移動するように判定します。
			iProcessResult = 5;
			//
			if( iEmergencyLevel == 3 && erPatientAgent.iGetStartEmergencyLevel() == 5 )
			{
				if( rnd.NextUnif() < 0.78 )
					iProcessResult = 1;
			}
			else if( iEmergencyLevel == 3 && erPatientAgent.iGetStartEmergencyLevel() == 4 )
			{
//				if( rnd.NextUnif() < 0.5 )
//					iProcessResult = 1;
			}
		}
		else
		{
			// 検査が必要な場合は検査室へ患者を移動します。
			if( erPatientAgent.iGetExaminataionFinishFlag() == 0 && isJudgeExamination( erPatientAgent ) == true )
			{
				vSetRequestExamination();
				iProcessResult = 2;
			}
			else
			{
				// 手術が必要な場合は手術室へ患者を移動します。
				if( isJudgeOperation(erPatientAgent) == true )
				{
					iProcessResult = 3;
				}
				else
				{
					// 入院（一般病棟）の必要がある場合は患者を一般病棟へ移動します。
					if( isJudgeGeneralWard( erPatientAgent ) == true )
					{
						iProcessResult = 4;
					}
					else
					{
						// 緊急を要するものではないので、診察室を終了し、待合室へ移動します。
						iProcessResult = 1;
					}
				}
			}
			// 検査終了している場合はここでフラグを0にします。
			if( erPatientAgent.iGetExaminataionFinishFlag() == 1 )
			{
				erPatientAgent.vSetExaminationFinishFlag( 0 );
			}
			if( iProcessResult == 4 )
			{
				if( iEmergencyLevel == 4 && erPatientAgent.iGetStartEmergencyLevel() == 5 )
				{
					if( rnd.NextUnif() < 0.999 )
						iProcessResult = 1;
				}
				else if( iEmergencyLevel == 5 && erPatientAgent.iGetStartEmergencyLevel() == 5 )
				{
					iProcessResult = 1;
				}
				else if( iEmergencyLevel == 5 && erPatientAgent.iGetStartEmergencyLevel() == 4 )
				{
					iProcessResult = 1;
				}
				else if( iEmergencyLevel == 4 && erPatientAgent.iGetStartEmergencyLevel() == 4 )
				{
					if( rnd.NextUnif() < 0.8 )
						iProcessResult = 1;
				}
			}
		}
		iTotalConsultationNum++;
		return iProcessResult;
	}

	/**
	 * <PRE>
	 *   問診を行います。基本的にはバイタルサイン、傷病重症度より判定します。
	 *   参考になるものがないので、トリアージの判定アルゴリズムを基にしています。
	 * </PRE>
	 * @param erPatientAgent			患者エージェントインスタンス
	 * @return							緊急度(1～5)
	 * @throws ERDoctorAgentException	医師エージェント例外
	 */
	private int iMedicalInterview( ERPatientAgent erPatientAgent ) throws ERDoctorAgentException
	{
		int iEmergency = 5;
		// バイタルサインが正常かどうかの判定を行います。
		if( isJudgeVitalSign( erPatientAgent ) == true )
		{
			// 正常でない場合は詳細に見ます。

			// 脈が明らかに低い場合はCTASレベル1とします。
			if( erPatientAgent.lfGetPulse() <= 30.0 )
			{
				iEmergency = 1;
				return iEmergency;
			}
			// 脈が明らかに多い場合はCTASレベルを1とします。
			else if( erPatientAgent.lfGetPulse() >=  140.0 )
			{
				iEmergency = 1;
				return iEmergency;
			}

			// 呼吸回数が明らかに低い、あるいはない場合はCTASレベル1とします。
			if( erPatientAgent.lfGetRr() <= 5.0 )
			{
				iEmergency = 1;
				return iEmergency;
			}
			// 呼吸回数があまりにも多い場合はCTASレベルを1とします。
			else if( erPatientAgent.lfGetRr() >= 40 )
			{
				iEmergency = 1;
				return iEmergency;
			}

			// けいれん状態の場合はCTASレベル1とします。
			if( erPatientAgent.strGetInjuryUnspecifiedStatus() == "けいれん状態" )
			{
				iEmergency = 1;
				return iEmergency;
			}
		}

		// 酸素飽和度から緊急度を判定します。
		vJudgeSpO2Status( erPatientAgent.strGetSpO2Status() );
		iEmergency = iJudgeSpO2( erPatientAgent );

		return iEmergency;
	}

	/**
	 * <PRE>
	 *    診察を実行します。
	 * </PRE>
	 * @param erPatientAgent			患者エージェントインスタンス
	 * @return							緊急度(1～5)
	 * @throws ERDoctorAgentException	医師エージェント例外
	 */
	private int iImplementConsultation( ERPatientAgent erPatientAgent ) throws ERDoctorAgentException
	{
		double lfEmergency = 0.0;
		int iEmergencyFlag = 0;
		int i;
		int iEmergency = 6;
		int iEmergencyRespiration = 0;
		int aiEmergency[] = new int[12];

		// 医療技師からの検査結果を反映します。
		vExaminationResult();

	// CTASレベル判定

		// 患者エージェントから状態を取得します。
		for( i = 0;i < aiEmergency.length; i++ )
		{
			aiEmergency[i] = 5;
		}

		// 呼吸のCTAS判定を実施します。
		vJudgeSpO2Status( erPatientAgent.strGetSpO2Status() );
		aiEmergency[0] = iJudgeSpO2( erPatientAgent );
		if( aiEmergency[0] == 1 )
		{
			// CTASレベル1と判定できる状態ならば、蘇生レベルの1を返却します。
			return aiEmergency[0];
		}

		// 循環動態のCTAS判定を実施します。
		aiEmergency[1] = iJudgeCirculatoryDynamics( erPatientAgent );
		if( aiEmergency[1] == 1 )
		{
			// CTASレベル1と判定できるならば、返却します。
			return aiEmergency[1];
		}

		// 意識状態のCTAS判定を実施します。
		aiEmergency[2] = iJudgeConsciousness( erPatientAgent );
		if( aiEmergency[2] == 1 )
		{
			// CTASレベル1と判定できるならば、返却します。
			return aiEmergency[2];
		}

		// 体温のCTAS判定を実施します。
		aiEmergency[3] = iJudgeBodyTemperature( erPatientAgent );

		// 出血性疾患のCTAS判定を実施します。
		aiEmergency[4] = iJudgeBloodIssue( erPatientAgent );

		// 疼痛のCTAS判定を実施します。
		aiEmergency[5] = iJudgeAIS( erPatientAgent );
		if( aiEmergency[5] == 1 )
		{
			// CTASレベル1と判定できるならば、返却します。
			return aiEmergency[5];
		}

		if( erPatientAgent.strGetSkinSignStatus() == "冷たい皮膚" )
		{
			aiEmergency[6] = 1;
		}
		if( erPatientAgent.strGetFaceSignStatus() == "チアノーゼ" )
		{
			aiEmergency[7] = 1;
		}
		if( erPatientAgent.strGetHeartSignStatus() == "心停止" )
		{
			aiEmergency[8] = 1;
		}
		if( erPatientAgent.strGetBodyTemperatureSignStatus() == "低体温症" ||
			erPatientAgent.strGetBodyTemperatureSignStatus() == "過高温" ||
			erPatientAgent.strGetBodyTemperatureSignStatus() == "超過高温" )
		{
			aiEmergency[9] = 1;
		}

		// 最終的に判定レベルが最も高いものを判定します。
		iEmergency = 6;
		for( i = 0;i < aiEmergency.length; i++ )
		{
			if( iEmergency > aiEmergency[i] && aiEmergency[i] != 0 )
			{
				iEmergency = aiEmergency[i];
			}
		}

		return iEmergency;
	}

	/**
	 * <PRE>
	 *    体温のCTAS判定を実施します。
	 *    全身性炎症症候群（SIRS）を判定してCTAS判定を実施します。
	 * </PRE>
	 * @param erPAgent 患者エージェント
	 * @return 患者の緊急度
	 */
	private int iJudgeBodyTemperature( ERPatientAgent erPAgent )
	{
		int iFlagCount = 0;
		int iFlagTemp = 0;
		int iEmergency = 5;
		// TODO 自動生成されたメソッド・スタブ

		// SIRSの診断項目　4項目
		// 1 体温
		if( erPAgent.lfGetBodyTemperature() > 38.0 || erPAgent.lfGetBodyTemperature() < 36.0 )
		{
			iFlagCount++;
			iFlagTemp = 1;
		}
		// 2 心拍数
		if( erPAgent.lfGetPulse() > 90.0 )
		{
			iFlagCount++;
		}
		// 3 呼吸数
		if( erPAgent.lfGetRr() > 20.0 )
		{
			iFlagCount++;
		}
		// 4 白血球数
		if( 12000.0 <= erPAgent.lfGetLeucocyte() || erPAgent.lfGetLeucocyte() <= 4000.0 )
		{
			iFlagCount++;
		}
		// 3項目以上満たす場合
		if( iFlagCount >= 3 )
		{
			iEmergency = 2;
		}
		// 2項目以下の場合
		if( iFlagCount <= 2 )
		{
			iEmergency = 3;
		}
		// 体温だけ該当する場合
		if( iFlagCount == 1 && iFlagTemp == 1 )
		{
			iEmergency = 4;
		}
		// 判定に該当しない場合は正常とします。
		if( iFlagCount == 0 || iFlagTemp == 0 )
		{
			iEmergency = 5;
		}
		// 意識障害（軽度）の場合
		if( 10.0 <= erPAgent.lfGetGcs() && erPAgent.lfGetGcs() <= 13.0 )
		{
			iEmergency = 2;
		}
		// 中等症の呼吸障害の場合
		if( 0.9 < erPAgent.lfGetSpO2() && erPAgent.lfGetSpO2() <= 0.92 )
		{
			iEmergency = 2;
		}
		return iEmergency;
	}

	/**
	 * <PRE>
	 *   出血のCTAS判定を実施します。
	 * </PRE>
	 * @param erPAgent 受診対象患者エージェント
	 * @return 患者エージェントの緊急度
	 */
	private int iJudgeBloodIssue( ERPatientAgent erPAgent )
	{
		int iEmergency;
		// TODO 自動生成されたメソッド・スタブ
		// この判定は再現が難しいため、正常とみなして緊急度を非緊急とします。
		iEmergency = 5;
		return iEmergency;
	}

	/**
	 * <PRE>
	 *    循環動態のCTAS判定を実施します。
	 * </PRE>
	 * @param erPAgent 対象患者エージェント
	 * @return 患者エージェントの緊急度
	 */
	private int iJudgeCirculatoryDynamics( ERPatientAgent erPAgent )
	{
		int i;
		int iEmergency;
		int aiEmergency[] = new int[5];
		// TODO 自動生成されたメソッド・スタブ

		// 弱い脈、明らかに低い、徐脈、重度の頻脈はCTASレベル1とします。
		if( erPAgent.lfGetPulse() <= 30.0 || erPAgent.lfGetPulse() >= 140.0 )
		{
			aiEmergency[0] = 1;
		}
		// 低血圧
		if( erPAgent.lfGetSbp() <= 80 || erPAgent.lfGetDbp() <= 30 )
		{
			aiEmergency[1] = 1;
		}
		// 顔面の状態から判定します。
		if( erPAgent.strGetFaceSignStatus() == "著明に蒼白" )
		{
			aiEmergency[2] = 1;
		}
		else if( erPAgent.strGetFaceSignStatus() == "蒼白" )
		{
			aiEmergency[2] = 2;
		}
		// 皮膚の状態を見ます。
		if( erPAgent.strGetSkinSignStatus() == "冷たい皮膚"  || erPAgent.strGetSkinSignStatus() == "冷たい発汗" )
		{
			aiEmergency[3] = 1;
		}
		// バイタルサインのチェックを行います。
		if( isJudgeVitalSign( erPAgent ) == true )
		{
			// 正常値のため、決定できないので他の判定条件により判定します。
//			aiEmergency[4] = rnd.NextUnif() <= 0.5 ? 4 : 5;
			aiEmergency[4] = 5;
		}
		// 最終的に判定レベルが最も高いものを判定します。
		iEmergency = 6;
		for( i = 0;i < aiEmergency.length; i++ )
		{
			if( iEmergency > aiEmergency[i] && aiEmergency[i] != 0 )
			{
				iEmergency = aiEmergency[i];
			}
		}
		return iEmergency;
	}


	/**
	 * <PRE>
	 *    バイタルサインが正常かどうかを判定します。
	 *    5項目を判定し、3項目以上正常値ならば正常と判定します。
	 * </PRE>
	 * @param erPAgent 患者エージェント
	 * @return 患者エージェントの緊急度
	 */
	private boolean isJudgeVitalSign( ERPatientAgent erPAgent )
	{
		boolean bRet = false;
		int iOK = 0;

		// 呼吸回数
		if( 15 <= erPAgent.lfGetRr() && erPAgent.lfGetRr() <= 20 )
		{
			iOK += 1;
		}
		// 体温
		if( 35.0 <= erPAgent.lfGetBodyTemperature() && erPAgent.lfGetBodyTemperature() <= 37.2 )
		{
			iOK += 1;
		}
		// 脈拍
		if( 60.0 <= erPAgent.lfGetPulse() && erPAgent.lfGetPulse() <= 100.0 )
		{
			iOK += 1;
		}
		// 拡張期血圧
		if( 110.0 <= erPAgent.lfGetSbp() && erPAgent.lfGetSbp() <= 140.0 )
		{
			iOK += 1;
		}
		// 収縮期血圧
		if( 55.0 <= erPAgent.lfGetDbp() && erPAgent.lfGetDbp() <= 85.0 )
		{
			iOK += 1;
		}
		// 3項目以上OKならば正常とみなします。
		if( iOK >= 3 )
		{
			bRet = true;
		}
		return bRet;
	}

	/**
	 * <PRE>
	 *   患者の意識レベルを判定します。
	 * </PRE>
	 * @param erPAgent	対象患者エージェント
	 * @return			緊急度レベル
	 */
	public int iJudgeConsciousness( ERPatientAgent erPAgent )
	{
		int iEmergency = 5;

		if( 3.0 <= erPAgent.lfGetGcs() && erPAgent.lfGetGcs() < 9.0 )
		{
			iEmergency = 1;
		}
		else if( 9.0 <= erPAgent.lfGetGcs() && erPAgent.lfGetGcs() < 13.0 )
		{
			iEmergency = 2;
		}
		else if( 13.0 <= erPAgent.lfGetGcs() && erPAgent.lfGetGcs() <= 15.0 )
		{
//			iEmergency = (int)(3.0*rnd.NextUnif()+2.0);
			//この領域のスコアに入った場合は他の条件から緊急度を判定する。
			iEmergency = 5;
		}
		else
		{

		}
		return iEmergency;
	}

	/**
	 * <PRE>
	 *   検査室から患者の検査結果を取得します。
	 * </PRE>
	 */
	private void vExaminationResult()
	{
		int i;
		int iCount = 0;

		if( iExaminationFinishFlag == 0 )
		{
			return ;
		}

		if( aiRequestAnatomys != null )
		{
			for( i = 0;i < aiRequestAnatomys.length; i++ )
			{
				if( aiRequestAnatomys[i] == 1 )
				{
					if( i == 0 ) 		lfJudgedAISHead				= lfExamAISHead;
					else if( i == 1 )	lfJudgedAISFace				= lfExamAISFace;
					else if( i == 2 )	lfJudgedAISNeck				= lfExamAISNeck;
					else if( i == 3 )	lfJudgedAISThorax			= lfExamAISThorax;
					else if( i == 4 )	lfJudgedAISAbdomen			= lfExamAISAbdomen;
					else if( i == 5 )	lfJudgedAISSpine			= lfExamAISSpine;
					else if( i == 6 )	lfJudgedAISUpperExtremity	= lfExamAISUpperExtremity;
					else if( i == 7 )	lfJudgedAISLowerExtremity	= lfExamAISLowerExtremity;
					else if( i == 8 )	lfJudgedAISUnspecified		= lfExamAISUnspecified;
					iCount++;
				}
			}
			if( iCount == 0 )
			{
				lfJudgedAISHead				= lfExamAISHead;
				lfJudgedAISFace				= lfExamAISFace;
				lfJudgedAISNeck				= lfExamAISNeck;
				lfJudgedAISThorax			= lfExamAISThorax;
				lfJudgedAISAbdomen			= lfExamAISAbdomen;
				lfJudgedAISSpine			= lfExamAISSpine;
				lfJudgedAISUpperExtremity	= lfExamAISUpperExtremity;
				lfJudgedAISLowerExtremity	= lfExamAISLowerExtremity;
				lfJudgedAISUnspecified		= lfExamAISUnspecified;
			}
		}
	}

	void vSetRequestExamination()
	{
		// ここでは仮アルゴリズムとして、各部位のAIS値が2以上の場合は検査を実施する。
		iRequestExamination = 2;		// 仮にCT室とする。

		// AIS値が一つでも大きいものがある場合はレントゲンとする。
		if( lfJudgedAISHead >= 2 )
		{
			iRequestExaminationNum++;
			aiRequestAnatomys[0] = 1;
			iRequestExamination = 1;
		}
		if( lfJudgedAISFace >= 2 )
		{
			iRequestExaminationNum++;
			aiRequestAnatomys[1] = 1;
			iRequestExamination = 1;
		}
		if( lfJudgedAISNeck >= 2 )
		{
			iRequestExaminationNum++;
			aiRequestAnatomys[2] = 1;
			iRequestExamination = 1;
		}
		if( lfJudgedAISThorax >= 2 )
		{
			iRequestExaminationNum++;
			aiRequestAnatomys[3] = 1;
			iRequestExamination = 1;
		}
		if( lfJudgedAISAbdomen >= 2 )
		{
			iRequestExaminationNum++;
			aiRequestAnatomys[4] = 1;
			iRequestExamination = 1;
		}
		if( lfJudgedAISSpine >= 2 )
		{
			iRequestExaminationNum++;
			aiRequestAnatomys[5] = 1;
			iRequestExamination = 1;
		}
		if( lfJudgedAISUpperExtremity >= 2 )
		{
			iRequestExaminationNum++;
			aiRequestAnatomys[6] = 1;
			iRequestExamination = 1;
		}
		if( lfJudgedAISLowerExtremity >= 2 )
		{
			iRequestExaminationNum++;
			aiRequestAnatomys[7] = 1;
			iRequestExamination = 1;
		}
		if( lfJudgedAISUnspecified >= 2 )
		{
			iRequestExaminationNum++;
			aiRequestAnatomys[8] = 1;
			iRequestExamination = 1;
		}

		// 検査部位が一か所の場合はX線室へ移動します。
		if( iRequestExaminationNum == 1 )
		{
			// 胸部の場合はX線検査
			if( aiRequestAnatomys[3] == 1 )
			{
				iRequestExamination = 1;
			}
			// 腹部の場合は血管造影検査を実施します。
			else if( aiRequestAnatomys[4] == 1 )
			{
				iRequestExamination = 5;
			}
			// 脊柱の場合はMRI検査を実施します。
			else if( aiRequestAnatomys[5] == 1)
			{
				iRequestExamination = 4;
			}
			// 上肢、下肢の場合はX線検査を実施します。
			else if( aiRequestAnatomys[6] == 1 || aiRequestAnatomys[7] == 1 )
			{
				iRequestExamination = 2;
			}

		}
		// 検査部位が複数個所の場合はCT室、MRI室のいずれかへ移動します。
		else if( iRequestExaminationNum >= 2 )
		{
			// CT検査を実施します。
			iRequestExamination = 3;

			// CT検査でわからなかった場合はMRI検査を実施します。
//			if()
//			{
//				iRequestExamination = 4;
//			}
		}
	}

	/**
	 * <PRE>
	 *    手術プロセスを実行します。
	 * </PRE>
	 * @param erPatientAgent			対象患者エージェント
	 * @throws ERDoctorAgentException	医師エージェント例外
	 * @return フラグ
	 */
	public int iImplementOperationProcess( ERPatientAgent erPatientAgent ) throws ERDoctorAgentException
	{
		int iEmergencyFlag = 0;

//		if( isJudgeOperation( erPatientAgent ) == true )
		{
			double lfAISHead = erPatientAgent.lfGetInternalAISHead();
			double lfAISFace = erPatientAgent.lfGetInternalAISFace();
			double lfAISNeck = erPatientAgent.lfGetInternalAISNeck();
			double lfAISThorax = erPatientAgent.lfGetInternalAISThorax();
			double lfAISAbdomen = erPatientAgent.lfGetInternalAISAbdomen();
			double lfAISSpine = erPatientAgent.lfGetInternalAISSpine();
			double lfAISUpperExtremity = erPatientAgent.lfGetInternalAISUpperExtremity();
			double lfAISLowerExtremity = erPatientAgent.lfGetInternalAISLowerExtremity();
			double lfAISUnspecified = erPatientAgent.lfGetInternalAISUnspecified();
			double lfRespirationNumber = erPatientAgent.lfGetRr();
			double lfSpO2 = erPatientAgent.lfGetSpO2();
			double lfPulse = erPatientAgent.lfGetPulse();
			double lfSbp = erPatientAgent.lfGetSbp();
			double lfDbp = erPatientAgent.lfGetDbp();
			double lfBodyTemperature = erPatientAgent.lfGetBodyTemperature();

			// AIS値を改善させます。
			// ここではAIS値を半分にします。
			lfAISHead = lfAISHead*lfRevisedOperationRate*lfCalcExperienceOperation();
			lfAISFace = lfAISFace*lfRevisedOperationRate*lfCalcExperienceOperation();
			lfAISNeck = lfAISNeck*lfRevisedOperationRate*lfCalcExperienceOperation();
			lfAISThorax = lfAISThorax*lfRevisedOperationRate*lfCalcExperienceOperation();
			lfAISAbdomen = lfAISAbdomen*lfRevisedOperationRate*lfCalcExperienceOperation();
			lfAISSpine = lfAISSpine*lfRevisedOperationRate*lfCalcExperienceOperation();
			lfAISUpperExtremity = lfAISUpperExtremity*lfRevisedOperationRate*lfCalcExperienceOperation();
			lfAISLowerExtremity = lfAISLowerExtremity*lfRevisedOperationRate*lfCalcExperienceOperation();
			lfAISUnspecified = lfAISUnspecified*lfRevisedOperationRate*lfCalcExperienceOperation();

			// 呼吸関係は正常値にします。
			lfSpO2 = 1.0;
			lfRespirationNumber = 5*rnd.NextUnif()+15.0;
			lfPulse = 10*rnd.NextUnif() + 70;
			lfSbp = 10*rnd.NextUnif() + 90;
			lfDbp = 10*rnd.NextUnif() + 70;
			lfBodyTemperature = 1.8*rnd.NextUnif() + 35;

			//

			// 改善した内容を代入します。
			erPatientAgent.vSetInternalAISHead( lfAISHead );
			erPatientAgent.vSetInternalAISFace( lfAISFace );
			erPatientAgent.vSetInternalAISNeck( lfAISNeck );
			erPatientAgent.vSetInternalAISThorax( lfAISThorax );
			erPatientAgent.vSetInternalAISAbdomen( lfAISAbdomen );
			erPatientAgent.vSetInternalAISSpine( lfAISSpine );
			erPatientAgent.vSetInternalAISUpperExtremity( lfAISUpperExtremity );
			erPatientAgent.vSetInternalAISLowerExtremity( lfAISLowerExtremity );
			erPatientAgent.vSetInternalAISUnspecified( lfAISUnspecified );

			// バイタルサイン系パラメータの改善
			erPatientAgent.vSetSpO2( lfSpO2 );
			erPatientAgent.vSetRr( lfRespirationNumber );
			erPatientAgent.vSetPulse( lfPulse );
			erPatientAgent.vSetSbp( lfSbp );
			erPatientAgent.vSetDbp( lfDbp );
			erPatientAgent.vSetBodyTemperature( lfBodyTemperature );

			// 患者の文字列による状態を更新します。
			erPatientAgent.vStrSetInjuryStatus();

			// AIS値を基に緊急レベルを判断します。
			iEmergencyLevel = iEmergencyFlag = iJudgeAIS( erPatientAgent );
			erPatientAgent.vSetEmergencyLevel( iEmergencyLevel );
			iTotalOperationNum++;						// 医師が患者を手術した回数
		}
		return iEmergencyFlag;
	}


	/**
	 * <PRE>
	 *    初療室プロセスを実行します。
	 * </PRE>
	 * @param erPatientAgent			処置を受ける患者エージェント
	 * @throws ERDoctorAgentException	医師エージェント例外
	 * @return 緊急度レベル
	 */
	public int iImplementEmergencyProcess( ERPatientAgent erPatientAgent )throws ERDoctorAgentException
	{
		int iEmergencyFlag = 0;
		int iEmergencyLevel1 = 6;
		int iEmergencyLevel2 = 6;

		// 診察を実施します。

		// 問診の実施をします。
		iEmergencyLevel1 = iMedicalInterview( erPatientAgent );

		// 医療技師からの検査結果を反映します。
		vExaminationResult();

		// AIS値を基に緊急レベルを判断します。
		iEmergencyLevel2 = iJudgeAIS( erPatientAgent );

		iEmergencyLevel = iEmergencyLevel1 > iEmergencyLevel2 ? iEmergencyLevel2 : iEmergencyLevel1;
		// 診察時間に経験年数を反映させます。(経験年数が浅いと過小評価するようにします。)
		iEmergencyLevel = (int)(lfCalcExperienceAIS()*iEmergencyLevel);

		// 手術を実施します。
//		if( isJudgeOperation( erPatientAgent ) == true )
		{
			double lfAISHead = erPatientAgent.lfGetInternalAISHead();
			double lfAISFace = erPatientAgent.lfGetInternalAISFace();
			double lfAISNeck = erPatientAgent.lfGetInternalAISNeck();
			double lfAISThorax = erPatientAgent.lfGetInternalAISThorax();
			double lfAISAbdomen = erPatientAgent.lfGetInternalAISAbdomen();
			double lfAISSpine = erPatientAgent.lfGetInternalAISSpine();
			double lfAISUpperExtremity = erPatientAgent.lfGetInternalAISUpperExtremity();
			double lfAISLowerExtremity = erPatientAgent.lfGetInternalAISLowerExtremity();
			double lfAISUnspecified = erPatientAgent.lfGetInternalAISUnspecified();
			double lfRespirationNumber = erPatientAgent.lfGetRr();
			double lfSpO2 = erPatientAgent.lfGetSpO2();
			double lfPulse = erPatientAgent.lfGetPulse();
			double lfSbp = erPatientAgent.lfGetSbp();
			double lfDbp = erPatientAgent.lfGetDbp();
			double lfBodyTemperature = erPatientAgent.lfGetBodyTemperature();

			// AIS値を改善させます。
			// ここではAIS値を半分にします。
//			lfAISHead = lfAISHead*lfRevisedEmergencyRate*lfCalcExperienceOperation();
//			lfAISFace = lfAISFace*lfRevisedEmergencyRate*lfCalcExperienceOperation();
//			lfAISNeck = lfAISNeck*lfRevisedEmergencyRate*lfCalcExperienceOperation();
//			lfAISThorax = lfAISThorax*lfRevisedEmergencyRate*lfCalcExperienceOperation();
//			lfAISAbdomen = lfAISAbdomen*lfRevisedEmergencyRate*lfCalcExperienceOperation();
//			lfAISSpine = lfAISSpine*lfRevisedEmergencyRate*lfCalcExperienceOperation();
//			lfAISUpperExtremity = lfAISUpperExtremity*lfRevisedEmergencyRate*lfCalcExperienceOperation();
//			lfAISLowerExtremity = lfAISLowerExtremity*lfRevisedEmergencyRate*lfCalcExperienceOperation();
//			lfAISUnspecified = lfAISUnspecified*lfRevisedEmergencyRate*lfCalcExperienceOperation();
			lfAISHead = lfAISHead*lfRevisedEmergencyRate;
			lfAISFace = lfAISFace*lfRevisedEmergencyRate;
			lfAISNeck = lfAISNeck*lfRevisedEmergencyRate;
			lfAISThorax = lfAISThorax*lfRevisedEmergencyRate;
			lfAISAbdomen = lfAISAbdomen*lfRevisedEmergencyRate;
			lfAISSpine = lfAISSpine*lfRevisedEmergencyRate;
			lfAISUpperExtremity = lfAISUpperExtremity*lfRevisedEmergencyRate;
			lfAISLowerExtremity = lfAISLowerExtremity*lfRevisedEmergencyRate;
			lfAISUnspecified = lfAISUnspecified*lfRevisedEmergencyRate;
			// バイタルサイン関係は正常値にします。
			lfSpO2 = 1.0;
			lfRespirationNumber = 5*rnd.NextUnif()+15.0;
			lfPulse = 10*rnd.NextUnif() + 70;
			lfSbp = 10*rnd.NextUnif() + 90;
			lfDbp = 10*rnd.NextUnif() + 70;
			lfBodyTemperature = 1.8*rnd.NextUnif() + 35;

			// 改善した内容を代入します。
			erPatientAgent.vSetInternalAISHead( lfAISHead );
			erPatientAgent.vSetInternalAISFace( lfAISFace );
			erPatientAgent.vSetInternalAISNeck( lfAISNeck );
			erPatientAgent.vSetInternalAISThorax( lfAISThorax );
			erPatientAgent.vSetInternalAISAbdomen( lfAISAbdomen );
			erPatientAgent.vSetInternalAISSpine( lfAISSpine );
			erPatientAgent.vSetInternalAISUpperExtremity( lfAISUpperExtremity );
			erPatientAgent.vSetInternalAISLowerExtremity( lfAISLowerExtremity );
			erPatientAgent.vSetInternalAISUnspecified( lfAISUnspecified );

			// バイタルサイン系パラメータの改善
			erPatientAgent.vSetSpO2( lfSpO2 );
			erPatientAgent.vSetRr( lfRespirationNumber );
			erPatientAgent.vSetPulse( lfPulse );
			erPatientAgent.vSetSbp( lfSbp );
			erPatientAgent.vSetDbp( lfDbp );
			erPatientAgent.vSetBodyTemperature( lfBodyTemperature );

			// 患者の文字列による状態を更新します。
			erPatientAgent.vStrSetInjuryStatus();

			// AIS値を基に緊急レベルを判断します。
			iEmergencyLevel = iJudgeAIS( erPatientAgent );

			erPatientAgent.vSetEmergencyLevel( iEmergencyLevel );
			iTotalEmergencyNum++;						// 医師が患者を初療室で対応した回数
		}
		return 0;
	}
	/**
	 * <PRE>
	 *     医師エージェントが通常プロセスに基づいて
	 *     患者エージェントの状況を判断します。
	 * </PRE>
	 * @param erPAgent 患者エージェントインスタンス
	 * @return 患者エージェントの緊急度
	 * @throws ERDoctorAgentException	医師エージェント例外
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	public int iJudgeAIS( ERPatientAgent erPAgent) throws ERDoctorAgentException
	{
		ERDoctorAgentException cERDae;
		int iTraumaNum = 0;
		int iEmergency = 0;
		int iMinEmergency = 10;

		// 患者から状態をメッセージで受診します
		cERDae = new ERDoctorAgentException();

		iTraumaNum = erPAgent.iGetNumberOfTrauma();
		if( iTraumaNum < 0 )
		{
			cERDae.SetErrorInfo( ERDA_INVALID_DATA_ERROR, "ERDoctorAgent", "iJudgeAIS", "不正な数値です。" );
			throw( cERDae );
		}

		// 受信後各部位に関しての痛度の度合いからAISを判定します。

		if( erPAgent.lfGetInternalAISHead() > 0.0 )
		{
			vJudgeAISHeadStatus( erPAgent.strGetInjuryHeadStatus() );
		}
		if( erPAgent.lfGetInternalAISFace() > 0.0 )
		{
			vJudgeAISFaceStatus( erPAgent.strGetInjuryFaceStatus() );
		}
		if( erPAgent.lfGetInternalAISNeck() > 0.0 )
		{
			vJudgeAISNeckStatus( erPAgent.strGetInjuryNeckStatus() );
		}
		if( erPAgent.lfGetInternalAISThorax() > 0.0 )
		{
			vJudgeAISThoraxStatus( erPAgent.strGetInjuryThoraxStatus() );
		}
		if( erPAgent.lfGetInternalAISAbdomen() > 0.0  )
		{
			vJudgeAISAbdomenStatus( erPAgent.strGetInjuryAbdomenStatus() );
		}
		if( erPAgent.lfGetInternalAISSpine() > 0.0  )
		{
			vJudgeAISSpineStatus( erPAgent.strGetInjurySpineStatus() );
		}
		if( erPAgent.lfGetInternalAISUpperExtremity() > 0.0 )
		{
			vJudgeAISUpperExtrimityStatus( erPAgent.strGetInjuryUpperExtremityStatus() );
		}
		if(erPAgent.lfGetInternalAISLowerExtremity() > 0.0 )
		{
			vJudgeAISLowerExtrimityStatus( erPAgent.strGetInjuryLowerExtremityStatus() );
		}
		if( erPAgent.lfGetInternalAISUnspecified() > 0.0  )
		{
			vJudgeAISUnspecifiedStatus( erPAgent.strGetInjuryUnspecifiedStatus() );
		}

		if( 0 <= lfHeadNRS && lfHeadNRS <= 3 )				iEmergency = iJudgeMildTrauma();
		else if( 4 <= lfHeadNRS && lfHeadNRS <= 7 )			iEmergency = iJudgeModerateTrauma();
		else if( 8 <= lfHeadNRS && lfHeadNRS <= 10 )		iEmergency = iJudgeSevereTrauma();
		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
		if( 0 <= lfFaceNRS && lfFaceNRS <= 3 )				iEmergency = iJudgeMildTrauma();
		else if( 4 <= lfFaceNRS && lfFaceNRS <= 7 )			iEmergency = iJudgeModerateTrauma();
		else if( 8 <= lfFaceNRS && lfFaceNRS <= 10 )		iEmergency = iJudgeSevereTrauma();
		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
		if( 0 <= lfNeckNRS && lfNeckNRS <= 3 )				iEmergency = iJudgeMildTrauma();
		else if( 4 <= lfNeckNRS && lfNeckNRS <= 7 )			iEmergency = iJudgeModerateTrauma();
		else if( 8 <= lfNeckNRS && lfNeckNRS <= 10 )		iEmergency = iJudgeSevereTrauma();
		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
		if( 0 <= lfThoraxNRS && lfThoraxNRS <= 3 )			iEmergency = iJudgeMildTrauma();
		else if( 4 <= lfThoraxNRS && lfThoraxNRS <= 7 )		iEmergency = iJudgeModerateTrauma();
		else if( 8 <= lfThoraxNRS && lfThoraxNRS <= 10 )	iEmergency = iJudgeSevereTrauma();
		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
		if( 0 <= lfAbdomenNRS && lfAbdomenNRS <= 3 )		iEmergency = iJudgeMildTrauma();
		else if( 4 <= lfAbdomenNRS && lfAbdomenNRS <= 7 )	iEmergency = iJudgeModerateTrauma();
		else if( 8 <= lfAbdomenNRS && lfAbdomenNRS <= 10 )	iEmergency = iJudgeSevereTrauma();
		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
		if( 0 <= lfSpineNRS && lfSpineNRS <= 3 )			iEmergency = iJudgeMildTrauma();
		else if( 4 <= lfSpineNRS && lfSpineNRS <= 7 )		iEmergency = iJudgeModerateTrauma();
		else if( 8 <= lfSpineNRS && lfSpineNRS <= 10 )		iEmergency = iJudgeSevereTrauma();
		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
		if( 0 <= lfUpperExtremityNRS && lfUpperExtremityNRS <= 3 )			iEmergency = iJudgeMildTrauma();
		else if( 4 <= lfUpperExtremityNRS && lfUpperExtremityNRS <= 7 )		iEmergency = iJudgeModerateTrauma();
		else if( 8 <= lfUpperExtremityNRS && lfUpperExtremityNRS <= 10 )	iEmergency = iJudgeMildTrauma();
		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
		if( 0 <= lfLowerExtremityNRS && lfLowerExtremityNRS <= 3 )			iEmergency = iJudgeMildTrauma();
		else if( 4 <= lfLowerExtremityNRS && lfLowerExtremityNRS <= 7 )		iEmergency = iJudgeModerateTrauma();
		else if( 8 <= lfLowerExtremityNRS && lfLowerExtremityNRS <= 10 )	iEmergency = iJudgeSevereTrauma();
		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
		if( 0 <= lfUnspecifiedNRS && lfUnspecifiedNRS <= 3 )				iEmergency = iJudgeMildTrauma();
		else if( 4 <= lfUnspecifiedNRS && lfUnspecifiedNRS <= 7 )			iEmergency = iJudgeModerateTrauma();
		else if( 8 <= lfUnspecifiedNRS && lfUnspecifiedNRS <= 10 )			iEmergency = iJudgeSevereTrauma();
		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
//		if( 0 <= lfHeadNRS && lfHeadNRS <= 3 )				iEmergency = 5;
//		else if( 4 <= lfHeadNRS && lfHeadNRS <= 7 )			iEmergency = 3;
//		else if( 8 <= lfHeadNRS && lfHeadNRS <= 10 )		iEmergency = 1;
//		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
//		if( 0 <= lfFaceNRS && lfFaceNRS <= 3 )				iEmergency = 5;
//		else if( 4 <= lfFaceNRS && lfFaceNRS <= 7 )			iEmergency = 3;
//		else if( 8 <= lfFaceNRS && lfFaceNRS <= 10 )		iEmergency = 1;
//		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
//		if( 0 <= lfNeckNRS && lfNeckNRS <= 3 )				iEmergency = 5;
//		else if( 4 <= lfNeckNRS && lfNeckNRS <= 7 )			iEmergency = 3;
//		else if( 8 <= lfNeckNRS && lfNeckNRS <= 10 )		iEmergency = 1;
//		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
//		if( 0 <= lfThoraxNRS && lfThoraxNRS <= 3 )			iEmergency = 5;
//		else if( 4 <= lfThoraxNRS && lfThoraxNRS <= 7 )		iEmergency = 3;
//		else if( 8 <= lfThoraxNRS && lfThoraxNRS <= 10 )	iEmergency = 1;
//		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
//		if( 0 <= lfAbdomenNRS && lfAbdomenNRS <= 3 )		iEmergency = 5;
//		else if( 4 <= lfAbdomenNRS && lfAbdomenNRS <= 7 )	iEmergency = 3;
//		else if( 8 <= lfAbdomenNRS && lfAbdomenNRS <= 10 )	iEmergency = 1;
//		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
//		if( 0 <= lfSpineNRS && lfSpineNRS <= 3 )			iEmergency = 5;
//		else if( 4 <= lfSpineNRS && lfSpineNRS <= 7 )		iEmergency = 3;
//		else if( 8 <= lfSpineNRS && lfSpineNRS <= 10 )		iEmergency = 1;
//		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
//		if( 0 <= lfUpperExtremityNRS && lfUpperExtremityNRS <= 3 )			iEmergency = 5;
//		else if( 4 <= lfUpperExtremityNRS && lfUpperExtremityNRS <= 7 )		iEmergency = 3;
//		else if( 8 <= lfUpperExtremityNRS && lfUpperExtremityNRS <= 10 )	iEmergency = 1;
//		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
//		if( 0 <= lfLowerExtremityNRS && lfLowerExtremityNRS <= 3 )			iEmergency = 5;
//		else if( 4 <= lfLowerExtremityNRS && lfLowerExtremityNRS <= 7 )		iEmergency = 3;
//		else if( 8 <= lfLowerExtremityNRS && lfLowerExtremityNRS <= 10 )	iEmergency = 1;
//		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
//		if( 0 <= lfUnspecifiedNRS && lfUnspecifiedNRS <= 3 )				iEmergency = 5;
//		else if( 4 <= lfUnspecifiedNRS && lfUnspecifiedNRS <= 7 )			iEmergency = 3;
//		else if( 8 <= lfUnspecifiedNRS && lfUnspecifiedNRS <= 10 )			iEmergency = 1;
//		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
		return iMinEmergency;
	}

	/**
	 * <PRE>
	 *    外傷が軽症の状態の緊急度を判定します。
	 * </PRE>
	 * @return	緊急度
	 */
	private int iJudgeMildTrauma()
	{
		int iEmergency = 5;
		double lfCurProb = 0.5;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

//		lfRand = 0.5 * ( normalRand() + 1.0 );
//		if( lfRand <= lfPrevProb )
//		{
//			iEmergency = 4;
//		}
//		if( lfPrevProb <= lfRand && lfRand <= lfCurProb )
//		{
//			iEmergency = 4;
//		}
//		lfPrevProb = lfCurProb;
//		lfCurProb += 0.5;
//		if( lfPrevProb <= lfRand && lfRand <= lfCurProb )
		{
			iEmergency = 5;
		}
		return iEmergency;
	}

	/**
	 * <PRE>
	 *    外傷が中症の状態の緊急度を判定します。
	 * </PRE>
	 * @return	緊急度
	 */
	private int iJudgeModerateTrauma()
	{
		int iEmergency = 5;
		double lfCurProb = 0.25;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

		lfRand = 0.5 * ( normalRand() + 1.5 );
//		lfRand = 0.7 * normalRand() + 0.3;
		lfRand = weibullRand(1.0, 0.05);
//		lfRand = weibullRand(1.0, 0.1);
//		lfRand = weibullRand(1.0, 0.3);
//		lfRand = 0.5 * normalRand() + 0.5;
//		lfRand = 0.35 * normalRand() + 0.65;
//		lfRand = 0.01 * normalRand() + 0.99;
		if( lfRand <= lfPrevProb )
		{
			iEmergency = 3;
		}
		if( lfPrevProb <= lfRand && lfRand <= lfCurProb )
		{
			iEmergency = 3;
		}
		lfPrevProb = lfCurProb;
		lfCurProb += 0.5;
		if( lfPrevProb <= lfRand && lfRand <= lfCurProb )
		{
			iEmergency = 4;
		}
		lfPrevProb = lfCurProb;
		lfCurProb += 0.25;
		if( lfPrevProb <= lfRand && lfRand <= lfCurProb )
		{
			iEmergency = 5;
		}
		return iEmergency;
	}

	/**
	 * <PRE>
	 *    外傷が重症の状態の緊急度を判定します。
	 * </PRE>
	 * @return	緊急度
	 */
	private int iJudgeSevereTrauma()
	{
		int iEmergency = 4;
		double lfCurProb = 0.25;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

//		lfRand = 0.5 * ( normalRand() + 1.0 );
		lfRand = weibullRand(1.0, 0.05);
//		lfRand = weibullRand(1.0, 0.1);
//		lfRand = weibullRand(1.0, 0.4);
//		lfRand = 0.5 * normalRand() + 0.5;
//		lfRand = 0.4 * normalRand() + 0.6;
//		lfRand = 0.56 * normalRand() + 0.44;
//		lfRand = weibullRand(2.0, 1.1);
		if( lfRand <= lfPrevProb )
		{
			iEmergency = 2;
		}
		if( lfPrevProb <= lfRand && lfRand <= lfCurProb )
		{
			iEmergency = 2;
		}
		lfPrevProb = lfCurProb;
		lfCurProb += 0.5;
		if( lfPrevProb <= lfRand && lfRand <= lfCurProb )
		{
			iEmergency = 3;
		}
		lfPrevProb = lfCurProb;
		lfCurProb += 0.25;
		if( lfPrevProb <= lfRand && lfRand <= lfCurProb )
		{
			iEmergency = 4;
		}
		return iEmergency;
	}

	/**
	 * <PRE>
	 *   患者から頭部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryHeadStatus 患者が訴える頭部のAIS値
	 * @throws ERDoctorAgentException 医師エージェント例外クラス
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeAISHeadStatus( String strCurrentInjuryHeadStatus ) throws ERDoctorAgentException
	{
		ERDoctorAgentException cERDae;
		cERDae = new ERDoctorAgentException();

		if( strCurrentInjuryHeadStatus == "痛くない")
		{
			lfJudgedAISHead = 0.0;
			lfHeadNRS = 0;
		}
		// 頭部外傷軽度
		else if( strCurrentInjuryHeadStatus == "ほんの少し痛い")
		{
			lfJudgedAISHead = 0.3*rnd.NextUnif()+1;
			lfHeadNRS = 1;
		}
		else if( strCurrentInjuryHeadStatus == "少し痛い")
		{
			lfJudgedAISHead = 0.3*rnd.NextUnif()+1.3;
			lfHeadNRS = 2;
		}
		else if( strCurrentInjuryHeadStatus == "少々痛い")
		{
			lfJudgedAISHead = 0.3*rnd.NextUnif()+1.6;
			lfHeadNRS = 3;
		}
		// 頭部外傷中等度
		else if( strCurrentInjuryHeadStatus == "痛い" )
		{
			lfJudgedAISHead = 0.3*rnd.NextUnif()+2;
			lfHeadNRS = 4;
		}
		else if( strCurrentInjuryHeadStatus == "けっこう痛い" )
		{
			lfJudgedAISHead = 0.3*rnd.NextUnif()+2.3;
			lfHeadNRS = 5;
		}
		else if( strCurrentInjuryHeadStatus == "相当痛い" )
		{
			lfJudgedAISHead = 0.3*rnd.NextUnif()+2.6;
			lfHeadNRS = 6;
		}
		// 重症、重篤(頭部の機能を失う)
		else if( strCurrentInjuryHeadStatus == "かなり痛い" )
		{
			lfJudgedAISHead = 0.3*rnd.NextUnif()+3;
			lfHeadNRS = 7;
		}
		else if( strCurrentInjuryHeadStatus == "とてつもなく痛い" )
		{
			lfJudgedAISHead = 0.3*rnd.NextUnif()+3.3;
			lfHeadNRS = 8;
		}
		else if( strCurrentInjuryHeadStatus == "とてつもなくかなり痛い" )
		{
			lfJudgedAISHead = 0.3*rnd.NextUnif()+3.6;
			lfHeadNRS = 9;
		}
		else if( strCurrentInjuryHeadStatus == "耐えられないほど痛い" )
		{
			lfJudgedAISHead = rnd.NextUnif()+4;
			lfHeadNRS = 10;
		}
		else
		{
			cERDae.SetErrorInfo( ERDA_INVALID_DATA_ERROR, "ERDoctorAgent", "vJudgeTriageProcess", "不正な数値です。" );
			throw( cERDae );
		}
	}
	/**
	 * <PRE>
	 *   患者から顔面部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryFaceStatus 患者が訴える顔面のAIS値
	 * @throws ERDoctorAgentException 医師エージェント例外クラス
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeAISFaceStatus( String strCurrentInjuryFaceStatus ) throws ERDoctorAgentException
	{
		ERDoctorAgentException cERDae;
		cERDae = new ERDoctorAgentException();

		if( strCurrentInjuryFaceStatus == "痛くない")
		{
			lfJudgedAISFace = 0.0;
			lfFaceNRS = 0;
		}
		// 顔面外傷軽度
		else if( strCurrentInjuryFaceStatus == "ほんの少し痛い")
		{
			lfJudgedAISFace = 0.3*rnd.NextUnif()+1;
			lfFaceNRS = 1;
		}
		else if( strCurrentInjuryFaceStatus == "少し痛い")
		{
			lfJudgedAISFace = 0.3*rnd.NextUnif()+1.3;
			lfFaceNRS = 2;
		}
		else if( strCurrentInjuryFaceStatus == "少々痛い")
		{
			lfJudgedAISFace = 0.3*rnd.NextUnif()+1.6;
			lfFaceNRS = 3;
		}
		// 顔面外傷中等度
		else if( strCurrentInjuryFaceStatus == "痛い" )
		{
			lfJudgedAISFace = 0.3*rnd.NextUnif()+2;
			lfFaceNRS = 4;
		}
		else if( strCurrentInjuryFaceStatus == "けっこう痛い" )
		{
			lfJudgedAISFace = 0.3*rnd.NextUnif()+2.3;
			lfFaceNRS = 5;
		}
		else if( strCurrentInjuryFaceStatus == "相当痛い" )
		{
			lfJudgedAISFace = 0.3*rnd.NextUnif()+2.6;
			lfFaceNRS = 6;
		}
		// 重症、重篤(顔面の機能を失う)
		else if( strCurrentInjuryFaceStatus == "かなり痛い" )
		{
			lfJudgedAISFace = 0.3*rnd.NextUnif()+3;
			lfFaceNRS = 7;
		}
		else if( strCurrentInjuryFaceStatus == "とてつもなく痛い" )
		{
			lfJudgedAISFace = 0.3*rnd.NextUnif()+3.3;
			lfFaceNRS = 8;
		}
		else if( strCurrentInjuryFaceStatus == "とてつもなくかなり痛い" )
		{
			lfJudgedAISFace = 0.3*rnd.NextUnif()+3.6;
			lfFaceNRS = 9;
		}
		else if( strCurrentInjuryFaceStatus == "耐えられないほど痛い" )
		{
			lfJudgedAISFace = rnd.NextUnif()+4;
			lfFaceNRS = 10;
		}
		else
		{
			cERDae.SetErrorInfo( ERDA_INVALID_DATA_ERROR, "ERDoctorAgent", "vJudgeTriageProcess", "不正な数値です。" );
			throw( cERDae );
		}
	}

	/**
	 * <PRE>
	 *   患者から頸部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryNeckStatus 患者が訴える頸部のAIS値
	 * @throws ERDoctorAgentException 医師エージェント例外クラス
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeAISNeckStatus( String strCurrentInjuryNeckStatus ) throws ERDoctorAgentException
	{
		ERDoctorAgentException cERDae;
		cERDae = new ERDoctorAgentException();

		if( strCurrentInjuryNeckStatus == "痛くない")
		{
			lfJudgedAISNeck = 0.0;
			lfNeckNRS = 0;
		}
		// 頸部外傷軽度
		else if( strCurrentInjuryNeckStatus == "ほんの少し痛い")
		{
			lfJudgedAISNeck = 0.3*rnd.NextUnif()+1;
			lfNeckNRS = 1;
		}
		else if( strCurrentInjuryNeckStatus == "少し痛い")
		{
			lfJudgedAISNeck = 0.3*rnd.NextUnif()+1.3;
			lfNeckNRS = 2;
		}
		else if( strCurrentInjuryNeckStatus == "少々痛い")
		{
			lfJudgedAISNeck = 0.3*rnd.NextUnif()+1.6;
			lfNeckNRS = 3;
		}
		// 頸部外傷中等度
		else if( strCurrentInjuryNeckStatus == "痛い" )
		{
			lfJudgedAISNeck = 0.3*rnd.NextUnif()+2;
			lfNeckNRS = 4;
		}
		else if( strCurrentInjuryNeckStatus == "けっこう痛い" )
		{
			lfJudgedAISNeck = 0.3*rnd.NextUnif()+2.3;
			lfNeckNRS = 5;
		}
		else if( strCurrentInjuryNeckStatus == "相当痛い" )
		{
			lfJudgedAISNeck = 0.3*rnd.NextUnif()+2.6;
			lfNeckNRS = 6;
		}
		// 重症、重篤(頸部の機能を失う)
		else if( strCurrentInjuryNeckStatus == "かなり痛い" )
		{
			lfJudgedAISNeck = 0.3*rnd.NextUnif()+3;
			lfNeckNRS = 7;
		}
		else if( strCurrentInjuryNeckStatus == "とてつもなく痛い" )
		{
			lfJudgedAISNeck = 0.3*rnd.NextUnif()+3.3;
			lfNeckNRS = 8;
		}
		else if( strCurrentInjuryNeckStatus == "とてつもなくかなり痛い" )
		{
			lfJudgedAISNeck = 0.3*rnd.NextUnif()+3.6;
			lfNeckNRS = 9;
		}
		else if( strCurrentInjuryNeckStatus == "耐えられないほど痛い" )
		{
			lfJudgedAISNeck = rnd.NextUnif()+4;
			lfNeckNRS = 10;
		}
		else
		{
			cERDae.SetErrorInfo( ERDA_INVALID_DATA_ERROR, "ERDoctorAgent", "vJudgeTriageProcess", "不正な数値です。" );
			throw( cERDae );
		}
	}

	/**
	 * <PRE>
	 *   患者から胸部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryThoraxStatus 患者が訴える腹部のAIS値
	 * @throws ERDoctorAgentException 医師エージェントの例外
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeAISThoraxStatus( String strCurrentInjuryThoraxStatus ) throws ERDoctorAgentException
	{
		ERDoctorAgentException cERDae;
		cERDae = new ERDoctorAgentException();

		if( strCurrentInjuryThoraxStatus == "痛くない")
		{
			lfJudgedAISThorax = 0.0;
			lfThoraxNRS = 0;
		}
		// 脊椎外傷軽度
		else if( strCurrentInjuryThoraxStatus == "ほんの少し痛い")
		{
			lfJudgedAISThorax = 0.3*rnd.NextUnif()+1;
			lfThoraxNRS = 1;
		}
		else if( strCurrentInjuryThoraxStatus == "少し痛い")
		{
			lfJudgedAISThorax = 0.3*rnd.NextUnif()+1.3;
			lfThoraxNRS = 2;
		}
		else if( strCurrentInjuryThoraxStatus == "少々痛い")
		{
			lfJudgedAISThorax = 0.3*rnd.NextUnif()+1.6;
			lfThoraxNRS = 3;
		}
		// 脊椎外傷中等度
		else if( strCurrentInjuryThoraxStatus == "痛い" )
		{
			lfJudgedAISThorax = 0.3*rnd.NextUnif()+2;
			lfThoraxNRS = 4;
		}
		else if( strCurrentInjuryThoraxStatus == "けっこう痛い" )
		{
			lfJudgedAISThorax = 0.3*rnd.NextUnif()+2.3;
			lfThoraxNRS = 5;
		}
		else if( strCurrentInjuryThoraxStatus == "相当痛い" )
		{
			lfJudgedAISThorax = 0.3*rnd.NextUnif()+2.6;
			lfThoraxNRS = 6;
		}
		// 重症、重篤(脊椎の機能を失う)
		else if( strCurrentInjuryThoraxStatus == "かなり痛い" )
		{
			lfJudgedAISThorax = 0.3*rnd.NextUnif()+3;
			lfThoraxNRS = 7;
		}
		else if( strCurrentInjuryThoraxStatus == "とてつもなく痛い" )
		{
			lfJudgedAISThorax = 0.3*rnd.NextUnif()+3.3;
			lfThoraxNRS = 8;
		}
		else if( strCurrentInjuryThoraxStatus == "とてつもなくかなり痛い" )
		{
			lfJudgedAISThorax = 0.3*rnd.NextUnif()+3.6;
			lfThoraxNRS = 9;
		}
		else if( strCurrentInjuryThoraxStatus == "耐えられないほど痛い" )
		{
			lfJudgedAISThorax = rnd.NextUnif()+4;
			lfThoraxNRS = 10;
		}
		else if( strCurrentInjuryThoraxStatus == "心停止" )
		{
			lfJudgedAISThorax = 5;
			lfThoraxNRS = 10;
		}
		else
		{
			cERDae.SetErrorInfo( ERDA_INVALID_DATA_ERROR, "ERDoctorAgent", "vJudgeTriageProcess", "不正な数値です。" );
			throw( cERDae );
		}
	}

	/**
	 * <PRE>
	 *   患者から腹部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryAbdomenStatus 患者が訴える腹部のAIS値
	 * @throws ERDoctorAgentException 医師エージェント例外
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeAISAbdomenStatus( String strCurrentInjuryAbdomenStatus ) throws ERDoctorAgentException
	{
		ERDoctorAgentException cERDae;
		cERDae = new ERDoctorAgentException();

		if( strCurrentInjuryAbdomenStatus == "痛くない")
		{
			lfJudgedAISAbdomen = 0.0;
			lfAbdomenNRS = 0;
		}
		// 腹部外傷軽度
		else if( strCurrentInjuryAbdomenStatus == "ほんの少し痛い")
		{
			lfJudgedAISAbdomen = 0.3*rnd.NextUnif()+1;
			lfAbdomenNRS = 1;
		}
		else if( strCurrentInjuryAbdomenStatus == "少し痛い")
		{
			lfJudgedAISAbdomen = 0.3*rnd.NextUnif()+1.3;
			lfAbdomenNRS = 2;
		}
		else if( strCurrentInjuryAbdomenStatus == "少々痛い")
		{
			lfJudgedAISAbdomen = 0.3*rnd.NextUnif()+1.6;
			lfAbdomenNRS = 3;
		}
		// 腹部外傷中等度
		else if( strCurrentInjuryAbdomenStatus == "痛い" )
		{
			lfJudgedAISAbdomen = 0.3*rnd.NextUnif()+2;
			lfAbdomenNRS = 4;
		}
		else if( strCurrentInjuryAbdomenStatus == "けっこう痛い" )
		{
			lfJudgedAISAbdomen = 0.3*rnd.NextUnif()+2.3;
			lfAbdomenNRS = 5;
		}
		else if( strCurrentInjuryAbdomenStatus == "相当痛い" )
		{
			lfJudgedAISAbdomen = 0.3*rnd.NextUnif()+2.6;
			lfAbdomenNRS = 6;
		}
		// 重症、重篤(腹部の機能を失う)
		else if( strCurrentInjuryAbdomenStatus == "かなり痛い" )
		{
			lfJudgedAISAbdomen = 0.3*rnd.NextUnif()+3;
			lfAbdomenNRS = 7;
		}
		else if( strCurrentInjuryAbdomenStatus == "とてつもなく痛い" )
		{
			lfJudgedAISAbdomen = 0.3*rnd.NextUnif()+3.3;
			lfAbdomenNRS = 8;
		}
		else if( strCurrentInjuryAbdomenStatus == "とてつもなくかなり痛い" )
		{
			lfJudgedAISAbdomen = 0.3*rnd.NextUnif()+3.6;
			lfAbdomenNRS = 9;
		}
		else if( strCurrentInjuryAbdomenStatus == "耐えられないほど痛い" )
		{
			lfJudgedAISAbdomen = rnd.NextUnif()+4;
			lfAbdomenNRS = 10;
		}
		else
		{
			cERDae.SetErrorInfo( ERDA_INVALID_DATA_ERROR, "ERDoctorAgent", "vJudgeTriageProcess", "不正な数値です。" );
			throw( cERDae );
		}
	}

	/**
	 * <PRE>
	 *   患者から腹部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjurySpineStatus 患者が訴える脊椎のAIS値
	 * @throws ERDoctorAgentException 医師エージェント例外クラス
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeAISSpineStatus( String strCurrentInjurySpineStatus ) throws ERDoctorAgentException
	{
		ERDoctorAgentException cERDae;
		cERDae = new ERDoctorAgentException();

		if( strCurrentInjurySpineStatus == "痛くない")
		{
			lfJudgedAISSpine = 0.0;
			lfSpineNRS = 0;
		}
		// 脊椎外傷軽度
		else if( strCurrentInjurySpineStatus == "ほんの少し痛い")
		{
			lfJudgedAISSpine = 0.3*rnd.NextUnif()+1;
			lfSpineNRS = 1;
		}
		else if( strCurrentInjurySpineStatus == "少し痛い")
		{
			lfJudgedAISSpine = 0.3*rnd.NextUnif()+1.3;
			lfSpineNRS = 2;
		}
		else if( strCurrentInjurySpineStatus == "少々痛い")
		{
			lfJudgedAISSpine = 0.3*rnd.NextUnif()+1.6;
			lfSpineNRS = 3;
		}
		// 脊椎外傷中等度
		else if( strCurrentInjurySpineStatus == "痛い" )
		{
			lfJudgedAISSpine = 0.3*rnd.NextUnif()+2;
			lfSpineNRS = 4;
		}
		else if( strCurrentInjurySpineStatus == "けっこう痛い" )
		{
			lfJudgedAISSpine = 0.3*rnd.NextUnif()+2.3;
			lfSpineNRS = 5;
		}
		else if( strCurrentInjurySpineStatus == "相当痛い" )
		{
			lfJudgedAISSpine = 0.3*rnd.NextUnif()+2.6;
			lfSpineNRS = 6;
		}
		// 重症、重篤(脊椎の機能を失う)
		else if( strCurrentInjurySpineStatus == "かなり痛い" )
		{
			lfJudgedAISSpine = 0.3*rnd.NextUnif()+3;
			lfSpineNRS = 7;
		}
		else if( strCurrentInjurySpineStatus == "とてつもなく痛い" )
		{
			lfJudgedAISSpine = 0.3*rnd.NextUnif()+3.3;
			lfSpineNRS = 8;
		}
		else if( strCurrentInjurySpineStatus == "とてつもなくかなり痛い" )
		{
			lfJudgedAISSpine = 0.3*rnd.NextUnif()+3.6;
			lfSpineNRS = 9;
		}
		else if( strCurrentInjurySpineStatus == "耐えられないほど痛い" )
		{
			lfJudgedAISSpine = rnd.NextUnif()+4;
			lfSpineNRS = 10;
		}
		else
		{
			cERDae.SetErrorInfo( ERDA_INVALID_DATA_ERROR, "ERDoctorAgent", "vJudgeTriageProcess", "不正な数値です。" );
			throw( cERDae );
		}
	}

	/**
	 * <PRE>
	 *   患者から腹部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryUpperExtremityStatus 患者が訴える上肢のAIS値
	 * @throws ERDoctorAgentException 医師エージェント例外クラス
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeAISUpperExtrimityStatus( String strCurrentInjuryUpperExtremityStatus ) throws ERDoctorAgentException
	{
		ERDoctorAgentException cERDae;
		cERDae = new ERDoctorAgentException();

		if( strCurrentInjuryUpperExtremityStatus == "痛くない")
		{
			lfJudgedAISUpperExtremity = 0.0;
			lfUpperExtremityNRS = 0;
		}
		// 上肢外傷軽度
		else if( strCurrentInjuryUpperExtremityStatus == "ほんの少し痛い")
		{
			lfJudgedAISUpperExtremity = 0.3*rnd.NextUnif()+1;
			lfUpperExtremityNRS = 1;
		}
		else if( strCurrentInjuryUpperExtremityStatus == "少し痛い")
		{
			lfJudgedAISUpperExtremity = 0.3*rnd.NextUnif()+1.3;
			lfUpperExtremityNRS = 2;
		}
		else if( strCurrentInjuryUpperExtremityStatus == "少々痛い")
		{
			lfJudgedAISUpperExtremity = 0.3*rnd.NextUnif()+1.6;
			lfUpperExtremityNRS = 3;
		}
		// 上肢外傷中等度
		else if( strCurrentInjuryUpperExtremityStatus == "痛い" )
		{
			lfJudgedAISUpperExtremity = 0.3*rnd.NextUnif()+2;
			lfUpperExtremityNRS = 4;
		}
		else if( strCurrentInjuryUpperExtremityStatus == "けっこう痛い" )
		{
			lfJudgedAISUpperExtremity = 0.3*rnd.NextUnif()+2.3;
			lfUpperExtremityNRS = 5;
		}
		else if( strCurrentInjuryUpperExtremityStatus == "相当痛い" )
		{
			lfJudgedAISUpperExtremity = 0.3*rnd.NextUnif()+2.6;
			lfUpperExtremityNRS = 6;
		}
		// 重症、重篤(上肢の機能を失う、上肢を失う)
		else if( strCurrentInjuryUpperExtremityStatus == "かなり痛い" )
		{
			lfJudgedAISUpperExtremity = 0.3*rnd.NextUnif()+3;
			lfUpperExtremityNRS = 7;
		}
		else if( strCurrentInjuryUpperExtremityStatus == "とてつもなく痛い" )
		{
			lfJudgedAISUpperExtremity = 0.3*rnd.NextUnif()+3.3;
			lfUpperExtremityNRS = 8;
		}
		else if( strCurrentInjuryUpperExtremityStatus == "とてつもなくかなり痛い" )
		{
			lfJudgedAISUpperExtremity = 0.3*rnd.NextUnif()+3.6;
			lfUpperExtremityNRS = 9;
		}
		else if( strCurrentInjuryUpperExtremityStatus == "耐えられないほど痛い" )
		{
			lfJudgedAISUpperExtremity = rnd.NextUnif()+4;
			lfUpperExtremityNRS = 10;
		}
		else if( strCurrentInjuryUpperExtremityStatus == "上肢が動かない" )
		{
			lfJudgedAISUpperExtremity = rnd.NextUnif()+3;
			lfUpperExtremityNRS = 8;
		}
		else if( strCurrentInjuryUpperExtremityStatus == "上肢が変形している" )
		{
			lfJudgedAISUpperExtremity = rnd.NextUnif()+2;
			lfUpperExtremityNRS = 6;
		}
		else if( strCurrentInjuryUpperExtremityStatus == "上肢が切断している" )
		{
			lfJudgedAISUpperExtremity = 5;
			lfUpperExtremityNRS = 10;
		}
		else
		{
			cERDae.SetErrorInfo( ERDA_INVALID_DATA_ERROR, "ERDoctorAgent", "vJudgeTriageProcess", "不正な数値です。" );
			throw( cERDae );
		}
	}

	/**
	 * <PRE>
	 *   患者から腹部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryLowerExtremityStatus 患者が訴える下肢のAIS値
	 * @throws ERDoctorAgentException 医師エージェント例外クラス
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeAISLowerExtrimityStatus( String strCurrentInjuryLowerExtremityStatus ) throws ERDoctorAgentException
	{
		ERDoctorAgentException cERDae;
		cERDae = new ERDoctorAgentException();

		if( strCurrentInjuryLowerExtremityStatus == "痛くない")
		{
			lfJudgedAISLowerExtremity = 0.0;
			lfLowerExtremityNRS = 0;
		}
		// 下肢外傷軽度
		else if( strCurrentInjuryLowerExtremityStatus == "ほんの少し痛い")
		{
			lfJudgedAISLowerExtremity = 0.3*rnd.NextUnif()+1;
			lfLowerExtremityNRS = 1;
		}
		else if( strCurrentInjuryLowerExtremityStatus == "少し痛い")
		{
			lfJudgedAISLowerExtremity = 0.3*rnd.NextUnif()+1.3;
			lfLowerExtremityNRS = 2;
		}
		else if( strCurrentInjuryLowerExtremityStatus == "少々痛い")
		{
			lfJudgedAISLowerExtremity = 0.3*rnd.NextUnif()+1.6;
			lfLowerExtremityNRS = 3;
		}
		// 下肢外傷中等度
		else if( strCurrentInjuryLowerExtremityStatus == "痛い" )
		{
			lfJudgedAISLowerExtremity = 0.3*rnd.NextUnif()+2;
			lfLowerExtremityNRS = 4;
		}
		else if( strCurrentInjuryLowerExtremityStatus == "けっこう痛い" )
		{
			lfJudgedAISLowerExtremity = 0.3*rnd.NextUnif()+2.3;
			lfLowerExtremityNRS = 5;
		}
		else if( strCurrentInjuryLowerExtremityStatus == "相当痛い" )
		{
			lfJudgedAISLowerExtremity = 0.3*rnd.NextUnif()+2.6;
			lfLowerExtremityNRS = 6;
		}
		// 重症、重篤(下肢の機能を失う、下肢を失う)
		else if( strCurrentInjuryLowerExtremityStatus == "かなり痛い" )
		{
			lfJudgedAISLowerExtremity = 0.3*rnd.NextUnif()+3;
			lfLowerExtremityNRS = 7;
		}
		else if( strCurrentInjuryLowerExtremityStatus == "とてつもなく痛い" )
		{
			lfJudgedAISLowerExtremity = 0.3*rnd.NextUnif()+3.3;
			lfLowerExtremityNRS = 8;
		}
		else if( strCurrentInjuryLowerExtremityStatus == "とてつもなくかなり痛い" )
		{
			lfJudgedAISLowerExtremity = 0.3*rnd.NextUnif()+3.6;
			lfLowerExtremityNRS = 9;
		}
		else if( strCurrentInjuryLowerExtremityStatus == "耐えられないほど痛い" )
		{
			lfJudgedAISLowerExtremity = rnd.NextUnif()+4;
			lfLowerExtremityNRS = 10;
		}
		else if( strCurrentInjuryLowerExtremityStatus == "下肢が動かない" )
		{
			lfJudgedAISLowerExtremity = rnd.NextUnif()+3;
			lfLowerExtremityNRS = 6;
		}
		else if( strCurrentInjuryLowerExtremityStatus == "下肢が変形している" )
		{
			lfJudgedAISLowerExtremity = rnd.NextUnif()+2;
			lfLowerExtremityNRS = 8;
		}
		else if( strCurrentInjuryLowerExtremityStatus == "下肢が切断している" )
		{
			lfJudgedAISLowerExtremity = 5;
			lfLowerExtremityNRS = 10;
		}
		else
		{
			cERDae.SetErrorInfo( ERDA_INVALID_DATA_ERROR, "ERDoctorAgent", "vJudgeTriageProcess", "不正な数値です。" );
			throw( cERDae );
		}
	}

	/**
	 * <PRE>
	 *   患者から腹部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryUnspecifiedStatus 患者が訴える表面、熱傷、その他外傷のAIS値
	 * @throws ERDoctorAgentException 医師エージェント例外クラス
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeAISUnspecifiedStatus( String strCurrentInjuryUnspecifiedStatus ) throws ERDoctorAgentException
	{
		ERDoctorAgentException cERDae;
		cERDae = new ERDoctorAgentException();

		if( strCurrentInjuryUnspecifiedStatus == "痛くない")
		{
			lfJudgedAISUnspecified = 0.0;
			lfUnspecifiedNRS = 0;
		}
		// 下肢外傷軽度
		else if( strCurrentInjuryUnspecifiedStatus == "ほんの少し痛い")
		{
			lfJudgedAISUnspecified = 0.3*rnd.NextUnif()+1;
			lfUnspecifiedNRS = 1;
		}
		else if( strCurrentInjuryUnspecifiedStatus == "少し痛い")
		{
			lfJudgedAISUnspecified = 0.3*rnd.NextUnif()+1.3;
			lfUnspecifiedNRS = 2;
		}
		else if( strCurrentInjuryUnspecifiedStatus == "少々痛い")
		{
			lfJudgedAISUnspecified = 0.3*rnd.NextUnif()+1.6;
			lfUnspecifiedNRS = 3;
		}
		// 下肢外傷中等度
		else if( strCurrentInjuryUnspecifiedStatus == "痛い" )
		{
			lfJudgedAISUnspecified = 0.3*rnd.NextUnif()+2;
			lfUnspecifiedNRS = 4;
		}
		else if( strCurrentInjuryUnspecifiedStatus == "けっこう痛い" )
		{
			lfJudgedAISUnspecified = 0.3*rnd.NextUnif()+2.3;
			lfUnspecifiedNRS = 5;
		}
		else if( strCurrentInjuryUnspecifiedStatus == "相当痛い" )
		{
			lfJudgedAISUnspecified = 0.3*rnd.NextUnif()+2.6;
			lfUnspecifiedNRS = 6;
		}
		// 重症、重篤(下肢の機能を失う、下肢を失う)
		else if( strCurrentInjuryUnspecifiedStatus == "かなり痛い" )
		{
			lfJudgedAISUnspecified = 0.3*rnd.NextUnif()+3;
			lfUnspecifiedNRS = 7;
		}
		else if( strCurrentInjuryUnspecifiedStatus == "とてつもなく痛い" )
		{
			lfJudgedAISUnspecified = 0.3*rnd.NextUnif()+3.3;
			lfUnspecifiedNRS = 8;
		}
		else if( strCurrentInjuryUnspecifiedStatus == "とてつもなくかなり痛い" )
		{
			lfJudgedAISUnspecified = 0.3*rnd.NextUnif()+3.6;
			lfUnspecifiedNRS = 9;
		}
		else if( strCurrentInjuryUnspecifiedStatus == "耐えられないほど痛い" )
		{
			lfJudgedAISUnspecified = rnd.NextUnif()+4;
			lfUnspecifiedNRS = 10;
		}
		else if( strCurrentInjuryUnspecifiedStatus == "けいれん状態" )
		{
			lfJudgedAISUnspecified = 5;
			lfUnspecifiedNRS = 10;
		}
		else if( strCurrentInjuryUnspecifiedStatus == "冷たい皮膚" )
		{
			lfJudgedAISUnspecified = 5;
			lfUnspecifiedNRS = 10;
		}
		else
		{
			cERDae.SetErrorInfo( ERDA_INVALID_DATA_ERROR, "ERDoctorAgent", "vJudgeTriageProcess", "不正な数値です。" );
			throw( cERDae );
		}
	}

	/**
	 * <PRE>
	 *   患者から呼吸状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentRespirationStatus 患者の呼吸状態
	 * @throws ERDoctorAgentException 医師エージェント例外クラス
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeSpO2Status( String strCurrentRespirationStatus ) throws ERDoctorAgentException
	{
		ERDoctorAgentException cERDae;
		cERDae = new ERDoctorAgentException();

		if( strCurrentRespirationStatus == "呼吸停止")
		{
			lfPatientSpO2 = 0.1*rnd.NextUnif();
		}
		else if( strCurrentRespirationStatus == "チアノーゼ" )
		{
			lfPatientSpO2 = 0.65*rnd.NextUnif()+0.1;
		}
		else if( strCurrentRespirationStatus == "単語単位でのみ会話可能")
		{
			lfPatientSpO2 = 0.75*rnd.NextUnif()+0.15;
		}
		else if( strCurrentRespirationStatus == "文節単位で会話かろうじて可能" )
		{
			lfPatientSpO2 = 0.02*rnd.NextUnif()+0.9;
		}
		else if( strCurrentRespirationStatus == "文節単位で会話可能" )
		{
			lfPatientSpO2 = 0.02*rnd.NextUnif()+0.92;
		}
		else if( strCurrentRespirationStatus == "通常に会話可能" )
		{
			lfPatientSpO2 = 0.06*rnd.NextUnif()+0.94;
		}
		else
		{
			cERDae.SetErrorInfo( ERDA_INVALID_DATA_ERROR, "ERDoctorAgent", "vJudgeSpO2Status", "不正な数値です。" );
			throw( cERDae );
		}
	}

	public void vJudgeFaceSign( String strCurrentFaceSignStatus ) throws ERDoctorAgentException
	{
		ERDoctorAgentException cERDae;
		cERDae = new ERDoctorAgentException();

		if( strCurrentFaceSignStatus == "蒼白" )
		{
			lfJudgedAISFace = rnd.NextUnif()+2;
			lfFaceNRS = 6;
		}
		else if( strCurrentFaceSignStatus == "著明に蒼白" )
		{
			lfJudgedAISFace = 2*rnd.NextUnif()+3;
			lfFaceNRS = 10;
		}
		else
		{
			cERDae.SetErrorInfo( ERDA_INVALID_DATA_ERROR, "ERDoctorAgent", "vJudgeFaceSign", "不正な数値です。" );
			throw( cERDae );
		}
	}

	/**
	 * <PRE>
	 *   酸素飽和度から患者の緊急度を判定します。
	 * </PRE>
	 * @param erPAgent 対象となる患者エージェント
	 * @return 患者エージェントの緊急度
	 */
	private int iJudgeSpO2( ERPatientAgent erPAgent )
	{
		int iEmergency = 0;
		if( lfPatientSpO2 <= 0.9 )
		{
			iEmergency = 1;
		}
		else if( 0.9 < lfPatientSpO2 && lfPatientSpO2 <= 0.92 )
		{
			iEmergency = 2;
		}
		else if( 0.92 < lfPatientSpO2 && lfPatientSpO2 <= 0.94 )
		{
			iEmergency = 3;
		}
		else if( 0.94 < lfPatientSpO2 )
		{
			iEmergency = rnd.NextUnif() <= 0.5 ? 4 : 5;
		}
		return iEmergency;
	}

	/**
	 * <PRE>
	 *   診察、あるいは手術した患者の緊急状態を取得します。
	 * </PRE>
	 * @return	判定した患者の緊急度レベル
	 */
	public int iGetEmergencyLevel()
	{
		return iEmergencyLevel;
	}

	/**
	 * <PRE>
	 *   医師が応対している患者エージェントを取得します。
	 * </PRE>
	 * @return	担当している患者エージェント
	 */
	public ERPatientAgent cGetERPatientAgent()
	{
		return erPatientAgent;
	}

	/**
	 * <PRE>
	 *    対応中か否かを取得します。
	 * </PRE>
	 * @return	0 未対応中 1 対応中
	 */
	public int iGetAttending()
	{
		int iAttendingData = 0;
		// 診察室の場合
		if( iDoctorDepartment == 1 )
		{
			// 診察中か否か
			iAttendingData = iConsultationAttending;
		}
		// 手術室の場合
		else if( iDoctorDepartment == 2 )
		{
			// 診察中か否か
			iAttendingData = iOperationAttending;
		}
		// 初療室の場合
		else if( iDoctorDepartment ==  3 )
		{
			// 診察中か否か
			iAttendingData = iEmergencyAttending;
		}
		// それ以外の場合
		else
		{

		}
		return iAttendingData;
	}

	/**
	 * <PRE>
	 *   医師の診察時間を取得します。
	 * </PRE>
	 * @return 診察時間
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public double lfGetConsultationTime()
	{
		return lfConsultationTime;
	}

	/**
	 * <PRE>
	 *   医師の手術時間を取得します。
	 * </PRE>
	 * @return 手術時間
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public double lfGetOperationTime()
	{
		return lfOperationTime;
	}

	/**
	 * <PRE>
	 *   医師が応対する患者エージェントを設定します。
	 * </PRE>
	 * @param erPAgent 担当する患者エージェント
	 */
	public void vSetERPatientAgent( ERPatientAgent erPAgent )
	{
		erPatientAgent = erPAgent;
	}

	/**
	 * <PRE>
	 *    対応しているか否かのフラグを設定します。
	 * </PRE>
	 * @param iAttendingData
	 *         1 診察室
	 *         2 手術室
	 *         3 初療室
	 */
	public void vSetAttending( int iAttendingData )
	{
		// 診察室の場合
		if( iDoctorDepartment == 1 )
		{
			// 診察中か否か
			iConsultationAttending = iAttendingData;
			if( erPatientAgent != null && iAttendingData == 0 )
			{

			}
		}
		// 手術室の場合
		else if( iDoctorDepartment == 2 )
		{
			// 診察中か否か
			iOperationAttending = iAttendingData;
		}
		// 初療室の場合
		else if( iDoctorDepartment ==  3 )
		{
			// 診察中か否か
			iEmergencyAttending = iAttendingData;
		}
		// それ以外の場合
		else
		{

		}
	}

	/**
	 * <PRE>
	 *    救急部門に登録されている全エージェントのIDを設定します。
	 * </PRE>
	 * @param ArrayListNurseAgentIdsData			全看護師エージェントのID
	 * @param ArrayListDoctorAgentIdsData			全医師エージェントのID
	 * @param ArrayListClinicalEngineerAgentIdsData	全医療技師エージェントのID
	 */
	public void vSetAgentIds(ArrayList<Long> ArrayListNurseAgentIdsData, ArrayList<Long> ArrayListDoctorAgentIdsData, ArrayList<Long> ArrayListClinicalEngineerAgentIdsData)
	{
		int i;
		// TODO 自動生成されたメソッド・スタブ
		for( i = 0;i < ArrayListNurseAgentIdsData.size(); i++ )
		{
			this.ArrayListNurseAgentIds.add( ArrayListNurseAgentIdsData.get(i) );
		}
		for( i = 0;i < ArrayListDoctorAgentIdsData.size(); i++ )
		{
			this.ArrayListDoctorAgentIds.add( ArrayListDoctorAgentIdsData.get(i) );
		}
		for( i = 0;i < ArrayListClinicalEngineerAgentIdsData.size(); i++ )
		{
			this.ArrayListClinicalEngineerAgentIds.add( ArrayListClinicalEngineerAgentIdsData.get(i) );
		}
	}

	/**
	 * <PRE>
	 *    診察対応中か否かを取得します。
	 * </PRE>
	 * @return	true 診察中, false 未診察
	 */
	public boolean isConsultation()
	{
		// 診察室の場合
		if( iConsultationAttending == 1 )
		{
			// 診察中か否か
			return true;
		}
		return false;
	}

	/**
	 * <PRE>
	 *    手術判断を行います。
	 * </PRE>
	 * @param erPatientAgent  患者エージェント
	 * @return	true 手術を実施 false 手術しない
	 * @throws ERDoctorAgentException 医師エージェント例外
	 */
	public boolean isJudgeOperation( ERPatientAgent erPatientAgent ) throws ERDoctorAgentException
	{
		boolean bRet = false;

//		iJudgeAIS( erPatientAgent );
		// ここでは仮アルゴリズムとして、
		// 各部位のAIS値が1つでも3以上のものがある場合は手術を実施する。

		if( lfJudgedAISHead >= 3 )
		{
			bRet = true;
		}
		if( lfJudgedAISFace >= 3 )
		{
			bRet = true;
		}
		if( lfJudgedAISNeck >= 3 )
		{
			bRet = true;
		}
		if( lfJudgedAISThorax >= 3 )
		{
			bRet = true;
		}
		if( lfJudgedAISAbdomen >= 3 )
		{
			bRet = true;
		}
		if( lfJudgedAISSpine >= 3 )
		{
			bRet = true;
		}
		if( lfJudgedAISUpperExtremity >= 3 )
		{
			bRet = true;
		}
		if( lfJudgedAISLowerExtremity >= 3 )
		{
			bRet = true;
		}
		if( lfJudgedAISUnspecified >= 3 )
		{
			bRet = true;
		}
		if( iEmergencyLevel <= 3 )
		{
			bRet = true;
		}
		return bRet;
	}

	/**
	 * <PRE>
	 *    検査判定をします。
	 * </PRE>
	 * @param erPatientAgent	担当している患者エージェント
	 * @return	true 検査を実施 false 検査はしない
	 */
	public boolean isJudgeExamination( ERPatientAgent erPatientAgent )
	{
		boolean bRet = true;
		// ここでは仮アルゴリズムとして、
		// 各部位のAIS値が1つでも2以上のものがある場合は検査を実施する。

		if( lfJudgedAISHead >= 2 )
		{
			bRet = true;
		}
		if( lfJudgedAISFace >= 2 )
		{
			bRet = true;
		}
		if( lfJudgedAISNeck >= 2 )
		{
			bRet = true;
		}
		if( lfJudgedAISThorax >= 2 )
		{
			bRet = true;
		}
		if( lfJudgedAISAbdomen >= 2 )
		{
			bRet = true;
		}
		if( lfJudgedAISSpine >= 2 )
		{
			bRet = true;
		}
		if( lfJudgedAISUpperExtremity >= 2 )
		{
			bRet = true;
		}
		if( lfJudgedAISLowerExtremity >= 2 )
		{
			bRet = true;
		}
		if( lfJudgedAISUnspecified >= 2 )
		{
			bRet = true;
		}
		if( iEmergencyLevel <= 4 )
		{
			bRet = true;
		}
		return bRet;
	}

	/**
	 * <PRE>
	 *    患者エージェントの退院判定を行います。
	 * </PRE>
	 * @param erPatientAgent	担当している患者エージェント
	 * @return					true 入院する false 入院しない
	 */
	public boolean isJudgeDischarge( ERPatientAgent erPatientAgent )
	{
		boolean bRet = false;
		int iMoveFlag = 0;
		int iMoveJudgeFlag = 0;

		// ここでは仮アルゴリズムとして、
		// 各部位のAIS値が1以下のものがある場合は退院を許可する。

		iMoveJudgeFlag = erPatientAgent.iGetNumberOfTrauma();

		if( lfJudgedAISHead < 1.0 )
		{
			iMoveFlag += 1;
		}
		if( lfJudgedAISFace < 1.0 )
		{
			iMoveFlag += 1;
		}
		if( lfJudgedAISNeck < 1.0 )
		{
			iMoveFlag += 1;
		}
		if( lfJudgedAISThorax < 1.0 )
		{
			iMoveFlag += 1;
		}
		if( lfJudgedAISAbdomen < 1.0 )
		{
			iMoveFlag += 1;
		}
		if( lfJudgedAISSpine < 1.0 )
		{
			iMoveFlag += 1;
		}
		if( lfJudgedAISUpperExtremity < 1.0 )
		{
			iMoveFlag += 1;
		}
		if( lfJudgedAISLowerExtremity < 1.0 )
		{
			iMoveFlag += 1;
		}
		if( lfJudgedAISUnspecified < 1.0 )
		{
			iMoveFlag += 1;
		}
		if( iEmergencyLevel == 5 )
		{
			iMoveFlag += 1;
//			iMoveFlag = 9;
		}
		if( iMoveFlag >= 9 )
		{
			erPatientAgent.vSetStayHospital( 0 );
			erPatientAgent.vSetStayGeneralWardFlag( 0 );
			bRet = true;
		}
		return bRet;
	}

	/**
	 * <PRE>
	 *    患者エージェントの高度治療室判定を行います。
	 * </PRE>
	 * @param erPatientAgent	担当する患者エージェント
	 * @return					true 高度治療室へ入院 false 入院しない
	 */
	public boolean isJudgeHighCareUnit( ERPatientAgent erPatientAgent )
	{
		boolean bRet = false;
		int iMoveJudgeFlag = 0;
		int iMoveFlag = 0;
		int i;
		// ここでは仮アルゴリズムとして、
		// 各部位のAIS値が1つでも3以上のものがある場合は入院を実施する。

		if( erPatientAgent.iGetStayHighCareUnit() == 0 )
		{
			iMoveJudgeFlag = erPatientAgent.iGetNumberOfTrauma();

			if( 2.5 <= lfJudgedAISHead && lfJudgedAISHead < 3 );
			{
				iMoveFlag += 1;
			}
			if( 2.5 <= lfJudgedAISFace && lfJudgedAISFace < 3 )
			{
				iMoveFlag += 1;
			}
			if( 2.5 <= lfJudgedAISNeck &&lfJudgedAISNeck < 3 )
			{
				iMoveFlag += 1;
			}
			if( 2.5 <= lfJudgedAISThorax && lfJudgedAISThorax < 3 )
			{
				iMoveFlag += 1;
			}
			if( 2.5 <= lfJudgedAISAbdomen && lfJudgedAISAbdomen < 3 )
			{
				iMoveFlag += 1;
			}
			if( 2.5 <= lfJudgedAISSpine && lfJudgedAISSpine < 3 )
			{
				iMoveFlag += 1;
			}
			if( 2.5 <= lfJudgedAISUpperExtremity && lfJudgedAISUpperExtremity < 3 )
			{
				iMoveFlag += 1;
			}
			if( 2.5 <= lfJudgedAISLowerExtremity && lfJudgedAISLowerExtremity < 3 )
			{
				iMoveFlag += 1;
			}
			if( 2.5 <= lfJudgedAISUnspecified && lfJudgedAISUnspecified < 3 )
			{
				iMoveFlag += 1;
			}
			// 緊急度レベ3以上の場合は高度治療室へ入院するものとする。
			if( iEmergencyLevel == 3 )
			{
				iMoveFlag += 1;
			}

			// 集中治療室に入る重症度の場合はiMoveFlagを強制的に0にする。
			if( 3 <= lfJudgedAISHead )
			{
				iMoveFlag = 0;
			}
			if( 3 <= lfJudgedAISFace )
			{
				iMoveFlag = 0;
			}
			if( 3 <= lfJudgedAISNeck )
			{
				iMoveFlag = 0;
			}
			if( 3 <= lfJudgedAISThorax )
			{
				iMoveFlag = 0;
			}
			if( 3 <= lfJudgedAISAbdomen )
			{
				iMoveFlag = 0;
			}
			if( 3 <= lfJudgedAISSpine )
			{
				iMoveFlag = 0;
			}
			if( 3 <= lfJudgedAISUpperExtremity )
			{
				iMoveFlag = 0;
			}
			if( 3 <= lfJudgedAISLowerExtremity )
			{
				iMoveFlag = 0;
			}
			if( 3 <= lfJudgedAISUnspecified )
			{
				iMoveFlag = 0;
			}
			if( iEmergencyLevel < 3 )
			{
				iMoveFlag = 0;
			}
			if( iMoveFlag > 0 )
			{
				erPatientAgent.vSetStayHospital( 1 );
				erPatientAgent.vSetStayHighCareUnitFlag( 1 );
				erPatientAgent.vSetStayIntensiveCareUnitFlag( 0 );
				erPatientAgent.vSetStayGeneralWardFlag( 0 );
				bRet = true;
			}
		}
		return bRet;
	}

	/**
	 * <PRE>
	 *    患者エージェントの集中治療室判定を行います。
	 * </PRE>
	 * @param erPatientAgent 対象となる患者エージェント
	 * @return true  集中治療室へ移動
	 *         false 集中治療室へは行かない
	 */
	public boolean isJudgeIntensiveCareUnit( ERPatientAgent erPatientAgent )
	{
		boolean bRet = false;
		int iMoveFlag = 0;
		int iMoveJudgeFlag = 0;
		int i;
		// ここでは仮アルゴリズムとして、
		// 各部位のAIS値が1つでも3以上のものがある場合は集中治療室に入院する判定を行う。

		if( erPatientAgent.iGetStayIntensiveCareUnitFlag() == 0 )
		{
			iMoveJudgeFlag = erPatientAgent.iGetNumberOfTrauma();
//			System.out.println( lfJudgedAISHead + "," + lfJudgedAISFace + "," + lfJudgedAISNeck + "," + lfJudgedAISThorax + "," + lfJudgedAISAbdomen + "," + lfJudgedAISSpine + "," + lfJudgedAISUpperExtremity + "," + lfJudgedAISLowerExtremity + "," + lfJudgedAISUnspecified);
			if( 3 <= lfJudgedAISHead )
			{
				iMoveFlag += 1;
			}
			if( 3 <= lfJudgedAISFace )
			{
				iMoveFlag += 1;
			}
			if( 3 <= lfJudgedAISNeck )
			{
				iMoveFlag += 1;
			}
			if( 3 <= lfJudgedAISThorax )
			{
				iMoveFlag += 1;
			}
			if( 3 <= lfJudgedAISAbdomen )
			{
				iMoveFlag += 1;
			}
			if( 3 <= lfJudgedAISSpine )
			{
				iMoveFlag += 1;
			}
			if( 3 <= lfJudgedAISUpperExtremity )
			{
				iMoveFlag += 1;
			}
			if( 3 <= lfJudgedAISLowerExtremity )
			{
				iMoveFlag += 1;
			}
			if( 3 <= lfJudgedAISUnspecified )
			{
				iMoveFlag += 1;
			}
			if( iEmergencyLevel <= 2 )
			{
				iMoveFlag += 1;
			}
			if( iMoveFlag > 0 )
			{
				erPatientAgent.vSetStayHospitalFlag( 1 );
				erPatientAgent.vSetStayIntensiveCareUnitFlag( 1 );
				erPatientAgent.vSetStayHighCareUnitFlag( 0 );
				erPatientAgent.vSetStayGeneralWardFlag( 0 );
				bRet = true;
			}
		}
		return bRet;
	}

	/**
	 * <PRE>
	 *    患者エージェントの一般病棟判定を行います。
	 * </PRE>
	 * @param erPatientAgent 対象となる患者エージェント
	 * @return true 一般病棟へ移動可能
	 *         false 一般病棟へ移動不可
	 */
	public boolean isJudgeGeneralWard( ERPatientAgent erPatientAgent )
	{
		boolean bRet = false;
		int iMoveFlag = 0;
		int iMoveJudgeFlag = 0;
		int i;
		// ここでは仮アルゴリズムとして、
		// 各部位のAIS値が1つでも2以上のものがある場合は入院を実施する。

		if( erPatientAgent.iGetStayGeneralWardFlag() == 0 )
		{
			iMoveJudgeFlag = erPatientAgent.iGetNumberOfTrauma();

			if( 1 <= lfJudgedAISHead && lfJudgedAISHead < 2.5 )
			{
				iMoveFlag += 1;
			}
			if( 1 <= lfJudgedAISFace && lfJudgedAISFace < 2.5 )
			{
				iMoveFlag += 1;
			}
			if( 1 <= lfJudgedAISNeck &&lfJudgedAISNeck < 2.5 )
			{
				iMoveFlag += 1;
			}
			if( 1 <= lfJudgedAISThorax && lfJudgedAISThorax < 2.5 )
			{
				iMoveFlag += 1;
			}
			if( 1 <= lfJudgedAISAbdomen && lfJudgedAISAbdomen < 2.5 )
			{
				iMoveFlag += 1;
			}
			if( 1 <= lfJudgedAISSpine && lfJudgedAISSpine < 2.5 )
			{
				iMoveFlag += 1;
			}
			if( 1 <= lfJudgedAISUpperExtremity && lfJudgedAISUpperExtremity < 2.5 )
			{
				iMoveFlag += 1;
			}
			if( 1 <= lfJudgedAISLowerExtremity && lfJudgedAISLowerExtremity < 2.5 )
			{
				iMoveFlag += 1;
			}
			if( 1 <= lfJudgedAISUnspecified && lfJudgedAISUnspecified < 2.5 )
			{
				iMoveFlag += 1;
			}
			else if( iEmergencyLevel == 3 )
			{
				if( erPatientAgent.iGetStartEmergencyLevel() > iEmergencyLevel )
				{
//					if( rnd.NextUnif() < 0.8 )
						iMoveFlag = 0;
				}
			}
			else if( iEmergencyLevel == 4 )
			{
				iMoveFlag += 1;
				if( erPatientAgent.iGetStartEmergencyLevel() >= iEmergencyLevel )
				{
//					if( rnd.NextUnif() < 0.8 )
						iMoveFlag = 0;
				}
				if( rnd.NextUnif() < 0.8 )
					iMoveFlag = 0;
			}
			else if( iEmergencyLevel > 4 )
			{
				iMoveFlag = 0;
			}

			// 高度治療室、集中治療室に入る重症度の場合はiMoveFlagを強制的に0にする。
			if( 2 <= lfJudgedAISHead )
			{
				iMoveFlag = 0;
			}
			if( 2 <= lfJudgedAISFace )
			{
				iMoveFlag = 0;
			}
			if( 2 <= lfJudgedAISNeck )
			{
				iMoveFlag = 0;
			}
			if( 2 <= lfJudgedAISThorax )
			{
				iMoveFlag = 0;
			}
			if( 2 <= lfJudgedAISAbdomen )
			{
				iMoveFlag = 0;
			}
			if( 2 <= lfJudgedAISSpine )
			{
				iMoveFlag = 0;
			}
			if( 2 <= lfJudgedAISUpperExtremity )
			{
				iMoveFlag = 0;
			}
			if( 2 <= lfJudgedAISLowerExtremity )
			{
				iMoveFlag = 0;
			}
			if( 2 <= lfJudgedAISUnspecified )
			{
				iMoveFlag = 0;
			}
			if( iEmergencyLevel <= 3 )
			{
				iMoveFlag = 0;
			}
			if( iMoveFlag > 0 )
			{
				erPatientAgent.vSetStayHospital( 1 );
				erPatientAgent.vSetStayGeneralWardFlag( 1 );
				erPatientAgent.vSetStayIntensiveCareUnitFlag( 0 );
				erPatientAgent.vSetStayHighCareUnitFlag( 0 );
				bRet = true;
			}
		}
		return bRet;
	}

	public boolean isSurgeon()
	{
		boolean bRet = true;

		if( iSurgeon == 0 ) bRet = false;
		return bRet;
	}

	public void vSetSurgeon( int iSurgeonData )
	{
		iSurgeon = iSurgeonData;
	}

	/**
	 * <PRE>
	 *    患者の手術時間を算出します。
	 *
	 * </PRE>
	 * @param  erPatientAgent 患者エージェント
	 * @return 手術時間
	 * @throws ERDoctorAgentException 医師エージェント例外クラス
	 */
	public int isJudgeOeprationTime( ERPatientAgent erPatientAgent ) throws ERDoctorAgentException
	{
		int i,j;
		int iNumOfTrauma = 0;
		int iOperationTimeData = 0;
		int iMaxOperationTime = -1;
		int iSumOperationTime = 0;

		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfRes = 0.0;
		double lfRand = 0.0;

		// 患者の真のAIS値を推定します。
		iJudgeAIS( erPatientAgent );
		// 外傷数を取得します。
		iNumOfTrauma = erPatientAgent.iGetNumberOfTrauma();

		iMaxOperationTime = 0;
		// 該当する手術時間を算出します。(重症度に応じて割り当てる必要はどうもないようだ。)
		for( j = 0;j < iNumOfTrauma; j++ )
		{
			lfRand = rnd.NextUnif();
			lfProb = lfPrevProb = 0.0;
			for( i = 0;i < 649; i++ )
			{
				lfRes  = lfCalcOperationTime( i, 2, 110 );
				lfProb += lfRes/20.99971074;
				if( lfPrevProb <= lfRand && lfRand < lfProb )
				{
					iOperationTimeData = i;
					break;
				}
				lfPrevProb = lfProb;
			}

//			iSumOperationTime += iOperationTimeData;
			lfRes  = lfCalcOperationTime( lfRand, 2, 110 );
//			iOperationTimeData = (int)lfRes;
			iMaxOperationTime = iOperationTimeData > iMaxOperationTime ? iOperationTimeData : iMaxOperationTime;
		}

		// 経験年数及び連携度に応じて手術時間を変更します。
		iMaxOperationTime = (int)(iMaxOperationTime*lfCalcExperienceTime()*rnd.NextUnif()*lfOperationAssociateRate);

		return iMaxOperationTime*60;
//		return iSumOperationTime*60;
	}

	/**
	 * <PRE>
	 *	ワイブル分布に当て込んで手術時間を算出する。
	 * </PRE>
	 * @param lfData  ワイブル分布データ
	 * @param lfK     形状パラメータ
	 * @param lfM     尺度パラメータ
	 * @return 手術時間
	 */
	private double lfCalcOperationTime( double lfData, double lfK, double lfM )
	{
		double lfRes = 0.0;
		double lfA = 21.0;
		lfRes = lfA*lfK/lfM*Math.pow( lfData/lfM,lfK-1 )*Math.exp( -Math.pow( lfData/lfM,lfK ) );
//		lfRes = lfK*Math.log( Math.pow( 1.0/(1-lfData)/lfM, 1.0/lfK ) );
		return lfRes;
	}

	/**
	 * <PRE>
	 *    患者の診察時間を算出します。
	 *    厚生労働省統計データ　平成23年受療行動調査（確定数）の概況より参照
	 * </PRE>
	 * @param  erPatientAgent 患者エージェント
	 * @return 診察時間
	 */
	public double isJudgeConsultationTime( ERPatientAgent erPatientAgent )
	{
		int iOperationTimeData = 0;
		int iMaxOperationTime = -1;
		int iSumOperationTime = 0;

		double lfRand = 0.0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfAvg = 0.0;
		double lfStd = 0.0;

		// 診察に係る時間を算出します。
		lfRand = rnd.NextUnif();
		lfProb = 0.154846;
		lfPrevProb = 0.0;
		if( lfRand < lfProb )
		{
			// 3分未満
			lfAvg = 90;
			lfStd = 90;
			lfConsultationTime = lfAvg + lfStd*normalRand();
			lfConsultationTime = lfConsultationTime > 180 ? 180 : lfConsultationTime;
		}
		lfPrevProb = lfProb;
		lfProb += 0.481087;
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// 3分以上10分未満
			lfAvg = 390;
			lfStd = 210;
			lfConsultationTime = lfAvg + lfStd*normalRand();
			lfConsultationTime = lfConsultationTime < 180 ? 180 : lfConsultationTime;
			lfConsultationTime = lfConsultationTime > 600 ? 600 : lfConsultationTime;
		}
		lfPrevProb = lfProb;
		lfProb += 0.276596;
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// 10分以上20分未満
			lfAvg = 900;
			lfStd = 300;
			lfConsultationTime = lfAvg + lfStd*normalRand();
			lfConsultationTime = lfConsultationTime < 600 ? 600 : lfConsultationTime;
			lfConsultationTime = lfConsultationTime > 1200 ? 1200 : lfConsultationTime;
		}
		lfPrevProb = lfProb;
		lfProb += 0.043735;
		if( lfPrevProb <= lfRand && lfRand < lfProb )
		{
			// 20分以上30分未満
			lfAvg = 1500;
			lfStd = 300;
			lfConsultationTime = lfAvg + lfStd*normalRand();
			lfConsultationTime = lfConsultationTime < 1200 ? 1200 : lfConsultationTime;
			lfConsultationTime = lfConsultationTime > 1800 ? 1800 : lfConsultationTime;
		}
		lfPrevProb = lfProb;
		lfProb += 0.043735;
		if( lfPrevProb <= lfRand  )
		{
			// 30分以上
			lfAvg = 2700;
			lfStd = 900;
			lfConsultationTime = lfAvg + lfStd*normalRand();
			lfConsultationTime = lfConsultationTime < 1800 ? 1800 : lfConsultationTime;
		}

		// 経験年数及び連携度に応じて診察時間を変更します。
		lfConsultationTime = lfConsultationTime*lfCalcExperienceTime()*lfConsultationAssociateRate;
		return lfConsultationTime;
	}

	/**
	 * <PRE>
	 *    経験年数を加算算出する式です。
	 *    初期値は3倍程度になり、経験年数が経過するにつれて1倍に近づきます。
	 *    診察時間、手術時間算出に使用します。
	 * </PRE>
	 * @return 積算する重み。
	 * @author kobayashi
	 * @since 2015/10/09
	 */
	private double lfCalcExperienceTime()
	{
		double lfYearExperienceData = 0.0;
		lfYearExperienceData = lfYearExperience >= 5.0 ? 5.0 : lfYearExperience;
		return lfExperienceRate1 * Math.exp(-lfYearExperienceData*lfConExperience) + lfExperienceRate2;
	}

	/**
	 * <PRE>
	 *    経験年数を加算算出する式です。
	 *    初期値は0.8倍程度になり、経験年数が経過するにつれて1に近づきます。
	 *    5年以降は一定とします。
	 * </PRE>
	 * @return 積算する重み。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	private double lfCalcExperienceAIS()
	{
		double lfYearExperienceData = 0.0;
		lfYearExperienceData = lfYearExperience >= 5.0 ? 5.0 : lfYearExperience;
		return -lfExperienceRateAIS1 * Math.exp(-lfYearExperienceData*lfConExperienceAIS) + lfExperienceRateAIS2;
	}

	/**
	 * <PRE>
	 *    経験年数を加算算出する式です。
	 *    初期値は1倍程度になり、経験年数が経過するにつれて0.5に近づきます。
	 * </PRE>
	 * @return 積算する重み。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	private double lfCalcExperienceOperation()
	{
		double lfRes = 0.0;
		double lfYearExperienceData = 0.0;
		lfYearExperienceData = lfYearExperience >= 5.0 ? 5.0 : lfYearExperience;
		lfRes = lfExperienceRateOp1 * Math.exp(-lfYearExperienceData*lfConExperienceOp) + lfExperienceRateOp2;
		cDoctorAgentLog.info("経験年数パラメータ：" + lfRes);
		return lfRes;
	}

	/**
	 * <PRE>
	 *    疲労度を算出します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/10/29
	 * @param lfCurrentTime	現在のシミュレーション経過時刻
	 */
	private void vCalcFatigue( double lfCurrentTime )
	{
		double lfRes = 0.0;
		double lfU = 0.0;
		double lfDeltaR = 0.0;
		double lfDeltaU = 0.0;
		double lfDeltaWorkLoad = 10.0;
		double lfFrameWidth = 0.5;		// 500[ms]
		double lfMu = lfConTired1;		// リカバー用パラメータ
		double lfOmega = lfConTired2;	// リカバー用パラメータ
		double lfAlpha = lfConTired3;	// 疲労用パラメータ
		double lfBeta = lfConTired4;	// 疲労用パラメータ

		if( iCalcTiredFlag == 0 )
		{
			// 初回は初期の値を代入します。
			lfFatigue = lfMu*Math.exp( lfOmega*lfFrameWidth );
			iCalcTiredFlag = 1;
		}
		else
		{
			if( iDoctorDepartment == 1 )
			{
				// 診察室のワークロードを設定します。
				lfDeltaWorkLoad = 10;
			}
			else if( iDoctorDepartment == 2 )
			{
				// 手術室のワークロードを設定します。
				lfDeltaWorkLoad = 5;
			}
			else if( iDoctorDepartment == 3 )
			{
				// 初療室のワークロードを設定します。
				lfDeltaWorkLoad = 20;
			}
			lfDeltaU = lfAlpha*Math.exp( lfBeta*lfDeltaWorkLoad );
			lfFatigue = (lfFatigue + lfDeltaU)/(1.0+Math.exp( lfOmega*lfFrameWidth ));
		}
		cDoctorAgentLog.info(this.getId() + "," + "疲労度パラメータ：" + lfFatigue );
	}

	/**
	 * <PRE>
	 *    連携度を設定します。
	 *    看護師エージェントが複数人いる場合、徐々に診察の効率が増します。
	 * </PRE>
	 * @param ArrayListNurseAgents 医師が所属する室に所属している全看護師エージェント
	 * @return 積算する重み。
	 * @author kobayashi
	 * @since 2015/10/11
	 */
	public double lfCalcAssociateRateConsultation( ArrayList<ERNurseAgent> ArrayListNurseAgents )
	{
		int i;
		double lfRes = 0.0;

		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			lfRes += ArrayListNurseAgents.get(i).lfGetAssociationRate();
		}
		lfConsultationAssociateRate = 0.9*Math.exp( -0.09*1.0/lfRes )+0.1;
		lfConsultationAssociateRate = lfConsultationAssociateRate < 0.5 ? 0.5 : lfConsultationAssociateRate;
		cDoctorAgentLog.info(this.getId() + "," + "連携度：" + lfConsultationAssociateRate);
		return lfConsultationAssociateRate;
	}

	/**
	 * <PRE>
	 *    連携度を設定します。
	 *    医師エージェントが複数人いる場合及び看護師エージェントが複数人いる場合、徐々に手術の効率が増します。
	 * </PRE>
	 * @param ArrayListDoctorAgents	所属している医師エージェント
	 * @param ArrayListNurseAgents	所属している看護師エージェント
	 * @return 積算する重み。
	 * @author kobayashi
	 * @since 2015/10/11
	 */
	public double lfCalcAssociateRateOperation( ArrayList<ERDoctorAgent> ArrayListDoctorAgents, ArrayList<ERNurseAgent> ArrayListNurseAgents )
	{
		int i;
		double lfRes = 0.0;

		for( i = 0;i < ArrayListDoctorAgents.size(); i++ )
		{
			lfRes += ArrayListDoctorAgents.get(i).lfGetAssociationRate();
		}
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			lfRes += ArrayListNurseAgents.get(i).lfGetAssociationRate();
		}

		lfOperationAssociateRate = 0.9*Math.exp( -0.09*1.0/lfRes )+0.1;
		lfOperationAssociateRate = lfOperationAssociateRate < 0.5 ? 0.5 : lfOperationAssociateRate;
		cDoctorAgentLog.info("連携度：" + lfOperationAssociateRate);
		return lfOperationAssociateRate;
	}

	/**
	 * <PRE>
	 *   初療室での処置時間を取得します。
	 * </PRE>
	 * @return	初療室の処置時間
	 */
	public double lfGetEmergencyTime()
	{
		return lfEmergencyTime;
	}

	/**
	 * <PRE>
	 *   診察にかかる時間を設定します。
	 * </PRE>
	 * @param lfTime	診察時間
	 */
	public void vSetConsultationTime( double  lfTime )
	{
		lfConsultationTime = lfTime;					// 診察時間
	}

	/**
	 * <PRE>
	 *   手術にかかる時間を設定します。
	 * </PRE>
	 * @param lfTime	手術時間
	 */
	public void vSetOperationTime( double lfTime )
	{
		lfOperationTime = lfTime;						// 手術時間
	}

	/**
	 * <PRE>
	 *   初療室でかかる時間を設定します。
	 * </PRE>
	 * @param lfTime	初療室の処置時間
	 */
	public void vSetEmergencyTime( double lfTime )
	{
		lfEmergencyTime = lfTime;						// 初療室対応時間
	}

	/**
	 * <PRE>
	 *   医師の所属部門を設定します。
	 * </PRE>
	 * @param iDepartment	医師が所属する部屋
	 */
	public void vSetDoctorDepartment( int iDepartment )
	{
		iDoctorDepartment = iDepartment;
	}

	/**
	 * <PRE>
	 *    経験年数重みを設定します。
	 * </PRE>
	 * @param lfCon	経験年数重み
	 */
	public void vSetConExperience( double lfCon )
	{
		lfConExperience = lfCon;
	}

	/**
	 * <PRE>
	 *    経験年数を設定します。
	 * </PRE>
	 * @param lfCon	経験年数
	 */
	public void vSetYearExperience( double lfCon )
	{
		lfYearExperience = lfCon;
	}

	/**
	 * <PRE>
	 *    疲労度パラメータ1を設定します。
	 * </PRE>
	 * @param lfCon	疲労度1
	 */
	public void vSetConTired1( double lfCon )
	{
		lfConTired1 = lfCon;
	}

	/**
	 * <PRE>
	 *    疲労度パラメータ2を設定します。
	 * </PRE>
	 * @param lfCon	疲労度2
	 */
	public void vSetConTired2( double lfCon )
	{
		lfConTired2 = lfCon;
	}

	/**
	 * <PRE>
	 *    疲労度パラメータ3を設定します。
	 * </PRE>
	 * @param lfCon	疲労度3
	 */
	public void vSetConTired3( double lfCon )
	{
		lfConTired3 = lfCon;
	}

	/**
	 * <PRE>
	 *    疲労度パラメータ4を設定します。
	 * </PRE>
	 * @param lfCon	疲労度4
	 */
	public void vSetConTired4( double lfCon )
	{
		lfConTired4 = lfCon;
	}

	/**
	 * <PRE>
	 *    疲労度を設定します。
	 * </PRE>
	 * @param lfCon	疲労度
	 */
	public void vSetTiredRate( double lfCon )
	{
		lfTiredRate = lfCon;
	}

	/**
	 * <PRE>
	 *    手術による重症度改善値を設定します。
	 * </PRE>
	 * @param lfCon	手術改善度
	 */
	public void vSetRevisedOperationRate( double lfCon )
	{
		lfRevisedOperationRate = lfCon;
	}

	/**
	 * <PRE>
	 *    初療室による重症度改善値を設定します。
	 * </PRE>
	 * @param lfCon	初療室処置改善度
	 */
	public void vSetRevisedEmergencyRate( double lfCon )
	{
		lfRevisedEmergencyRate = lfCon;
	}

	/**
	 * <PRE>
	 *    他のエージェントとの連携度を設定します。
	 * </PRE>
	 * @param lfCon	連携度
	 */
	public void vSetAssociationRate( double lfCon )
	{
		lfAssociationRate = lfCon;
	}

	/**
	 * <PRE>
	 *    患者エージェントが移動中か否かを表すフラグです。
	 *    0 移動していない。
	 *    1 移動している。
	 * </PRE>
	 * @param iData	移動中か否かのフラグ0 or 1
	 */
	public void vSetPatientMoveWaitFlag( int iData )
	{
		iPatientMoveWaitFlag = iData;
	}


	/**
	 * <PRE>
	 *    経験値による重みづけ計算に使用するパラメータ(その1)です。
	 * </PRE>
	 * @param lfData 経験値による重みづけパラメータ(その1)
	 */
	public void vSetExperienceRate1( double lfData )
	{
		lfExperienceRate1 = lfData;
	}

	/**
	 * <PRE>
	 *    経験値による重みづけ計算に使用するパラメータ(その2)です。
	 * </PRE>
	 * @param lfData 経験値による重みづけパラメータ(その2)
	 */
	public void vSetExperienceRate2( double lfData )
	{
		lfExperienceRate2 = lfData;
	}

	/**
	 * <PRE>
	 *    経験値(AIS重症度)による重みづけ計算に使用するパラメータです。
	 * </PRE>
	 * @param lfData 経験値による重みづけパラメータ(AIS重症度)
	 */
	public void vSetConExperienceAIS( double lfData )
	{
		lfConExperienceAIS = lfData;
	}

	/**
	 * <PRE>
	 *    経験値(AIS重症度)による重みづけ計算に使用するパラメータ(その1)です。
	 * </PRE>
	 * @param lfData 経験値(AIS重症度)による重みづけパラメータ(その1)
	 */
	public void vSetExperienceRateAIS1( double lfData )
	{
		lfExperienceRateAIS1 = lfData;
	}

	/**
	 * <PRE>
	 *    経験値(AIS重症度)による重みづけ計算に使用するパラメータ(その2)です。
	 * </PRE>
	 * @param lfData 経験値(AIS重症度)による重みづけパラメータ(その2)
	 */
	public void vSetExperienceRateAIS2( double lfData )
	{
		lfExperienceRateAIS2 = lfData;
	}

	/**
	 * <PRE>
	 *    所属している部屋番号を設定します。
	 * </PRE>
	 * @param iNum 部屋番号
	 */
	public void vSetRoomNumber( int iNum )
	{
		iRoomNumber = iNum;
	}


	/**
	 * <PRE>
	 *    現在の医師が作業開始してからの時間を取得します。
	 *    なお、診察室の医師ならば、1患者開始からの時間で、
	 *    終了したら0に初期化します。
	 * </PRE>
	 * @return 医師の現在の作業時間
	 */
	public double lfGetCurrentPassOverTime()
	{
		return lfCurrentPassOverTime;
	}

	/**
	 * <PRE>
	 *     医師エージェントが検査依頼したかどうかを取得します。
	 * </PRE>
	 * @return	依頼部位
	 */
	public int iGetRequestExamination()
	{
		return iRequestExamination;
	}

	public double lfGetAssociationRate()
	{
		return lfAssociationRate;
	}

	/**
	 * <PRE>
	 *   医師エージェントへ診察結果送信の設定をします。
	 * </PRE>
	 * @param erPAgent				検査対象の患者エージェント
	 * @param iFromAgentId  		送信先のエージェント（ここでは医師エージェント）
	 * @param iToAgentId			送信元のエージェント（ここでは医師エージェント）
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public void vSendToDoctorAgentMessage( ERPatientAgent erPAgent, int iFromAgentId, int iToAgentId )
	{
		Message mesSend;
		mesSend = new Message();
		mesSend.setData( new MessageFromDocToDoc() );
		mesSend.setFromAgent( iFromAgentId );
		mesSend.setToAgent( iToAgentId );
		((MessageFromDocToDoc)mesSend.getData()).vSetERPatientAgent( erPAgent );
		((MessageFromDocToDoc)mesSend.getData()).vSetJudgedAISHead( lfJudgedAISHead );
		((MessageFromDocToDoc)mesSend.getData()).vSetJudgedAISFace( lfJudgedAISFace );
		((MessageFromDocToDoc)mesSend.getData()).vSetJudgedAISNeck( lfJudgedAISNeck );
		((MessageFromDocToDoc)mesSend.getData()).vSetJudgedAISThorax( lfJudgedAISThorax );
		((MessageFromDocToDoc)mesSend.getData()).vSetJudgedAISAbdomen( lfJudgedAISAbdomen );
		((MessageFromDocToDoc)mesSend.getData()).vSetJudgedAISSpine( lfJudgedAISSpine );
		((MessageFromDocToDoc)mesSend.getData()).vSetJudgedAISUpperExtremity( lfJudgedAISUpperExtremity );
		((MessageFromDocToDoc)mesSend.getData()).vSetJudgedAISLowerExtremity( lfJudgedAISLowerExtremity );
		((MessageFromDocToDoc)mesSend.getData()).vSetJudgedAISUnspecified( lfJudgedAISUnspecified );
		if( iDoctorDepartment == 1 )	((MessageFromDocToDoc)mesSend.getData()).vSetConsultationTime( lfCurrentPassOverTime );
		else							((MessageFromDocToDoc)mesSend.getData()).vSetConsultationTime( 0.0 );
		if( iDoctorDepartment == 2 || iDoctorDepartment == 3 )	((MessageFromDocToDoc)mesSend.getData()).vSetOperationTime( lfCurrentPassOverTime );
		else													((MessageFromDocToDoc)mesSend.getData()).vSetOperationTime( lfCurrentPassOverTime );
		((MessageFromDocToDoc)mesSend.getData()).vSetEmergencyLevel( iEmergencyLevel );
		((MessageFromDocToDoc)mesSend.getData()).vSetFromDoctorDepartment( iDoctorDepartment );
		this.vSendMessage( mesSend );
	}

	/**
	 * <PRE>
	 *   看護師エージェントへ診察結果送信の設定をします。
	 * </PRE>
	 * @param erPAgent					検査対象の患者エージェント
	 * @param erNurseAgent				担当看護師エージェント
	 * @param iFromAgentId  			送信先のエージェント（ここでは医師エージェント）
	 * @param iToAgentId				送信元のエージェント（ここでは看護師エージェント）
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public void vSendToNurseAgentMessage( ERPatientAgent erPAgent, ERNurseAgent erNurseAgent, int iFromAgentId, int iToAgentId )
	{
		Message mesSend;
		mesSend = new Message();
		mesSend.setData( new MessageFromDocToNurse() );
		mesSend.setFromAgent( iFromAgentId );
		mesSend.setToAgent( iToAgentId );
		((MessageFromDocToNurse)mesSend.getData()).vSetERPatientAgent( erPAgent );
		((MessageFromDocToNurse)mesSend.getData()).vSetExamAISHead( lfJudgedAISHead );
		((MessageFromDocToNurse)mesSend.getData()).vSetExamAISFace( lfJudgedAISFace );
		((MessageFromDocToNurse)mesSend.getData()).vSetExamAISNeck( lfJudgedAISNeck );
		((MessageFromDocToNurse)mesSend.getData()).vSetExamAISThorax( lfJudgedAISThorax );
		((MessageFromDocToNurse)mesSend.getData()).vSetExamAISAbdomen( lfJudgedAISAbdomen );
		((MessageFromDocToNurse)mesSend.getData()).vSetExamAISSpine( lfJudgedAISSpine );
		((MessageFromDocToNurse)mesSend.getData()).vSetExamAISUpperExtremity( lfJudgedAISUpperExtremity );
		((MessageFromDocToNurse)mesSend.getData()).vSetExamAISLowerExtremity( lfJudgedAISLowerExtremity );
		((MessageFromDocToNurse)mesSend.getData()).vSetExamAISUnspecified( lfJudgedAISUnspecified );
		if( iDoctorDepartment == 1 )	((MessageFromDocToNurse)mesSend.getData()).vSetConsultationTime( lfCurrentPassOverTime );
		else							((MessageFromDocToNurse)mesSend.getData()).vSetConsultationTime( 0.0 );
		if( iDoctorDepartment == 2 || iDoctorDepartment == 3 )	((MessageFromDocToNurse)mesSend.getData()).vSetOperationTime( lfCurrentPassOverTime );
		else													((MessageFromDocToNurse)mesSend.getData()).vSetOperationTime( lfCurrentPassOverTime );
		((MessageFromDocToNurse)mesSend.getData()).vSetEmergencyLevel( iEmergencyLevel );
		((MessageFromDocToNurse)mesSend.getData()).vSetDoctorDepartment( iDoctorDepartment );
//		this.sendMessage( mesSend );

		// 対象となる看護師エージェントが自分自身でメッセージ送信します。
		erNurseAgent.vSendMessage( mesSend );
	}

	/**
	 * <PRE>
	 *   医療技師エージェントへ検査依頼送信の設定をします。
	 * </PRE>
	 * @param erPAgent			検査対象の患者エージェント
	 * @param erClinicalEngineerAgent	担当医療技師エージェント
	 * @param iFromAgentId  	送信先のエージェント（ここでは医師エージェント）
	 * @param iToAgentId		送信元のエージェント（ここでは看護師エージェント）
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public void vSendToEngineerAgentMessage( ERPatientAgent erPAgent, ERClinicalEngineerAgent erClinicalEngineerAgent, int iFromAgentId, int iToAgentId )
	{
		Message mesSend;
		mesSend = new Message();
		mesSend.setData( new MessageFromDocToEng() );
		mesSend.setFromAgent( iFromAgentId );
		mesSend.setToAgent( iToAgentId );
		((MessageFromDocToEng)mesSend.getData()).vSetERPatientAgent( erPAgent );
		((MessageFromDocToEng)mesSend.getData()).vSetRequestExamination( iRequestExamination );
		((MessageFromDocToEng)mesSend.getData()).vSetRequestAnatomy( aiRequestAnatomys );
		((MessageFromDocToEng)mesSend.getData()).vSetRequestExaminationNum( iRequestExaminationNum );
		((MessageFromDocToEng)mesSend.getData()).vSetConsultationTime( lfCurrentPassOverTime );
		if( iDoctorDepartment == 1 )	((MessageFromDocToEng)mesSend.getData()).vSetConsultationTime( lfCurrentPassOverTime );
		else							((MessageFromDocToEng)mesSend.getData()).vSetConsultationTime( 0.0 );
//		if( iDoctorDepartment == 2 || iDoctorDepartment == 3 )	((MessageFromDocToEng)mesSend.getData()).vSetOperationTime( lfCurrentPassOverTime );
//		else													((MessageFromDocToEng)mesSend.getData()).vSetOperationTime( lfCurrentPassOverTime );
		((MessageFromDocToEng)mesSend.getData()).vSetEmergencyLevel( iEmergencyLevel );
		((MessageFromDocToEng)mesSend.getData()).vSetDoctorDepartment( iDoctorDepartment );
//		this.sendMessage( mesSend );

		// 対象となる医療技師エージェントが自分自身にメッセージ送信します。
		erClinicalEngineerAgent.vSendMessage( mesSend );
	}

	/**
	 * <PRE>
	 *   患者エージェントへ診察結果送信の設定をします。
	 * </PRE>
	 * @param erPAgent			患者エージェントのインスタンス
	 * @param iFromAgentId  	送信先のエージェント（ここでは患者エージェント）
	 * @param iToAgentId		送信元のエージェント（ここでは看護師エージェント）
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	void vSendToPatientAgentMessage( ERPatientAgent erPAgent, int iFromAgentId, int iToAgentId )
	{
		Message mesSend;
		mesSend = new Message();
		mesSend.setData( new MessageFromDocToPat() );
		mesSend.setFromAgent( iFromAgentId );
		mesSend.setToAgent( iToAgentId );
		((MessageFromDocToPat)mesSend.getData()).vSetExamAISHead( lfExamAISHead );
		((MessageFromDocToPat)mesSend.getData()).vSetExamAISFace( lfExamAISFace );
		((MessageFromDocToPat)mesSend.getData()).vSetExamAISNeck( lfExamAISNeck );
		((MessageFromDocToPat)mesSend.getData()).vSetExamAISThorax( lfExamAISThorax );
		((MessageFromDocToPat)mesSend.getData()).vSetExamAISAbdomen( lfExamAISAbdomen );
		((MessageFromDocToPat)mesSend.getData()).vSetExamAISSpine( lfExamAISSpine );
		((MessageFromDocToPat)mesSend.getData()).vSetExamAISUpperExtremity( lfExamAISUpperExtremity );
		((MessageFromDocToPat)mesSend.getData()).vSetExamAISLowerExtremity( lfExamAISLowerExtremity );
		((MessageFromDocToPat)mesSend.getData()).vSetExamAISUnspecified( lfExamAISUnspecified );
		((MessageFromDocToPat)mesSend.getData()).vSetEmergencyLevel( iEmergencyLevel );
		if( iDoctorDepartment == 1 )	((MessageFromDocToPat)mesSend.getData()).vSetConsultationTime( lfCurrentPassOverTime );
		else							((MessageFromDocToPat)mesSend.getData()).vSetConsultationTime( 0.0 );
		if( iDoctorDepartment == 2 || iDoctorDepartment == 3 )	((MessageFromDocToPat)mesSend.getData()).vSetOperationTime( lfCurrentPassOverTime );
		else													((MessageFromDocToPat)mesSend.getData()).vSetOperationTime( lfCurrentPassOverTime );
		((MessageFromDocToPat)mesSend.getData()).vSetEmergencyLevel( iEmergencyLevel );
		((MessageFromDocToPat)mesSend.getData()).vSetDoctorDepartment( iDoctorDepartment );
//		this.sendMessage( mesSend );

		// 対象となる患者エージェントが自分自身にメッセージ送信します。
		erPAgent.vSendMessage( mesSend );
	}

	public void vSendMessage( Message mess )
	{
		if( mesQueueData != null )
		{
			mesQueueData.add( mess );
		}
	}

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
		int i;
		double lfSecond = timeStep / 1000.0;
		int iDoctorMessFlag = 0;
		int iNurseMessFlag = 0;
		int iClinicalEngineerMessFlag = 0;
		int iSurvivalFlag = 1;
		// TODO 自動生成されたメソッド・スタブ

		ERDoctorAgentException edae;
		edae = new ERDoctorAgentException();
		try
		{
			Message mess = null;
			// 看護師、医師からのメッセージを1つずつ取得します。
			// 対応中でない場合のみメッセージを取得します。
			if( this.iGetAttending() == 0 )
			{
				mess = messGetMessage();
				if( mess != null )
				{
					// 医師からのメッセージかどうかを判定します。
					if( mess.getData() instanceof MessageFromDocToDoc )
					{
						// 診察内容を取得します。
						// 部屋の想定は診察室、初療室、手術室、
						// 外傷の状況を取得します。
//						lfJudgedAISHead				= ((MessageFromDocToDoc)mess.getData()).lfGetAISHead();
//						lfJudgedAISFace				= ((MessageFromDocToDoc)mess.getData()).lfGetAISFace();
//						lfJudgedAISNeck				= ((MessageFromDocToDoc)mess.getData()).lfGetAISNeck();
//						lfJudgedAISThorax			= ((MessageFromDocToDoc)mess.getData()).lfGetAISThorax();
//						lfJudgedAISAbdomen			= ((MessageFromDocToDoc)mess.getData()).lfGetAISAbdomen();
//						lfJudgedAISSpine			= ((MessageFromDocToDoc)mess.getData()).lfGetAISSpine();
//						lfJudgedAISUpperExtremity	= ((MessageFromDocToDoc)mess.getData()).lfGetAISUpperExtremity();
//						lfJudgedAISLowerExtremity	= ((MessageFromDocToDoc)mess.getData()).lfGetAISLowerExtremity();
//						lfJudgedAISUnspecified		= ((MessageFromDocToDoc)mess.getData()).lfGetAISUnspecified();
						// 担当した医師エージェントを取得します。
						iFromDoctorDepartment		= ((MessageFromDocToDoc)mess.getData()).iGetFromDoctorDepartment();
						iFromDoctorId				= (int)mess.getFromAgentId();
						iEmergencyLevel				= ((MessageFromDocToDoc)mess.getData()).iGetEmergencyLevel();
//						lfConsultationTime			= ((MessageFromDocToDoc)mess.getData()).lfGetConsultationTime();
						// これから対応する患者エージェントを取得します。
						erPatientAgent				= ((MessageFromDocToDoc)mess.getData()).cGetERPatientAgent();
						iDoctorMessFlag = 1;
					}
					// 看護師からのメッセージかどうかを判定します。
					if( mess.getData() instanceof MessageFromNurseToDoc )
					{
						// 観察内容を取得します。
						// 外傷の状況を取得します。
//						lfJudgedAISHead				= ((MessageFromNurseToDoc)mess.getData()).lfGetAISHead();
//						lfJudgedAISFace				= ((MessageFromNurseToDoc)mess.getData()).lfGetAISFace();
//						lfJudgedAISNeck				= ((MessageFromNurseToDoc)mess.getData()).lfGetAISNeck();
//						lfJudgedAISThorax			= ((MessageFromNurseToDoc)mess.getData()).lfGetAISThorax();
//						lfJudgedAISAbdomen			= ((MessageFromNurseToDoc)mess.getData()).lfGetAISAbdomen();
//						lfJudgedAISSpine			= ((MessageFromNurseToDoc)mess.getData()).lfGetAISSpine();
//						lfJudgedAISUpperExtremity	= ((MessageFromNurseToDoc)mess.getData()).lfGetAISUpperExtremity();
//						lfJudgedAISLowerExtremity	= ((MessageFromNurseToDoc)mess.getData()).lfGetAISLowerExtremity();
//						lfJudgedAISUnspecified		= ((MessageFromNurseToDoc)mess.getData()).lfGetAISUnspecified();
						// 担当した看護師エージェントを取得します。
						iNurseDepartment			= ((MessageFromNurseToDoc)mess.getData()).iGetNurseDepartment();
						iNurseId					= (int)mess.getFromAgentId();
						iEmergencyLevel				= ((MessageFromNurseToDoc)mess.getData()).iGetEmergencyLevel();
						lfObservationTime			= ((MessageFromNurseToDoc)mess.getData()).lfGetObservationTime();
						// これから対応する患者エージェントを取得します。
						erPatientAgent				= ((MessageFromNurseToDoc)mess.getData()).cGetERPatientAgent();
						iNurseMessFlag = 1;
					}
					// 医療技師からのメッセージかどうかを判定します。
					if( mess.getData() instanceof MessageFromEngToDoc )
					{
						// 観察内容を取得します。
						// 外傷の状況を取得します。
						lfJudgedAISHead				= ((MessageFromEngToDoc)mess.getData()).lfGetAISHead();
						lfJudgedAISFace				= ((MessageFromEngToDoc)mess.getData()).lfGetAISFace();
						lfJudgedAISNeck				= ((MessageFromEngToDoc)mess.getData()).lfGetAISNeck();
						lfJudgedAISThorax			= ((MessageFromEngToDoc)mess.getData()).lfGetAISThorax();
						lfJudgedAISAbdomen			= ((MessageFromEngToDoc)mess.getData()).lfGetAISAbdomen();
						lfJudgedAISSpine			= ((MessageFromEngToDoc)mess.getData()).lfGetAISSpine();
						lfJudgedAISUpperExtremity	= ((MessageFromEngToDoc)mess.getData()).lfGetAISUpperExtremity();
						lfJudgedAISLowerExtremity	= ((MessageFromEngToDoc)mess.getData()).lfGetAISLowerExtremity();
						lfJudgedAISUnspecified		= ((MessageFromEngToDoc)mess.getData()).lfGetAISUnspecified();
						// 担当した医療技師エージェントを取得します。
						iClinicalEngineerDepartment	= ((MessageFromEngToDoc)mess.getData()).iGetClinicalEngineerDepartment();
						iClinicalEngineerId			= (int)mess.getFromAgentId();
						lfExaminationTime			= ((MessageFromEngToDoc)mess.getData()).lfGetExaminationTime();
						// これから対応する患者エージェントを取得します。
						erPatientAgent				= ((MessageFromEngToDoc)mess.getData()).cGetERPatientAgent();
						iClinicalEngineerMessFlag = 1;
					}
					// 患者からのメッセージかどうかを判定します。
					// (医師、看護師及び医療技師以外で受信するメッセージは患者しかいないため。)
					if( iDoctorMessFlag != 1 && iNurseMessFlag != 1 && iClinicalEngineerMessFlag != 1 )
					{
						// 観察内容を取得します。
						// 外傷の状況を取得します。
						// 患者が訴える症状を取得します。
						if( mess.getData() instanceof MessageFromPatToNurse )
						{
							strInjuryHeadStatus				= ((MessageFromPatToDoc)mess.getData()).strGetInjuryHeadStatus();
							strInjuryFaceStatus				= ((MessageFromPatToDoc)mess.getData()).strGetInjuryFaceStatus();
							strInjuryNeckStatus				= ((MessageFromPatToDoc)mess.getData()).strGetInjuryNeckStatus();
							strInjuryThoraxStatus			= ((MessageFromPatToDoc)mess.getData()).strGetInjuryThoraxStatus();
							strInjuryAbdomenStatus			= ((MessageFromPatToDoc)mess.getData()).strGetInjuryAbdomenStatus();
							strInjurySpineStatus			= ((MessageFromPatToDoc)mess.getData()).strGetInjurySpineStatus();
							strInjuryUpperExtremityStatus	= ((MessageFromPatToDoc)mess.getData()).strGetInjuryUpperExtremityStatus();
							strInjuryLowerExtremityStatus	= ((MessageFromPatToDoc)mess.getData()).strGetInjuryLowerExtremityStatus();
							strInjuryUnspecifiedStatus		= ((MessageFromPatToDoc)mess.getData()).strGetInjuryUnspecifiedStatus();
							// 担当した患者エージェントを取得します。
							iPatientLocation 				= ((MessageFromPatToDoc)mess.getData()).iGetPatientLocation();
							iPatientId						= (int)mess.getFromAgentId();
							lfWaitTime						= ((MessageFromPatToDoc)mess.getData()).lfGetWaitTime();
							iSurvivalFlag					= ((MessageFromPatToDoc)mess.getData()).iGetSurvivalFlag();
							// 患者が生存していない場合は医師の各種フラグを初期化する。
							if( iSurvivalFlag == 0 )
							{
								iConsultationAttending = 0;
								iOperationAttending = 0;
								iEmergencyAttending = 0;
								iPatientMoveWaitFlag = 0;
								lfCurrentPassOverTime = 0.0;
								lfCurrentEmergencyTime = 0.0;
								lfCurrentOperationTime = 0.0;
								lfCurrentConsultationTime = 0.0;
							}
						}
					}
				}
			}
		// 診察中であれば、経過時間を計算します。
			if( iConsultationAttending == 1 )
			{
				lfCurrentPassOverTime += lfSecond;
				lfCurrentConsultationTime += lfSecond;
				lfTotalConsultationTime += lfSecond;
			}
			else
			{
				// 手術中であれば、経過時間を計算します。
				if( iOperationAttending == 1 )
				{
					if( iPatientMoveWaitFlag == 0 )
					{
						lfCurrentPassOverTime += lfSecond;
						lfCurrentOperationTime += lfSecond;
						lfTotalOperationTime += lfSecond;
					}
				}
				else
				{
					// 初療室対応中であれば、経過時間を計算します。
					if( iEmergencyAttending == 1 )
					{
						if( iPatientMoveWaitFlag == 0 )
						{
							lfCurrentPassOverTime += lfSecond;
							lfCurrentEmergencyTime += lfSecond;
							lfTotalEmergencyTime += lfSecond;
						}
					}
					else
					{
						if( iPatientMoveWaitFlag == 0 )
						{
							lfCurrentPassOverTime = 0.0;
							lfCurrentEmergencyTime = 0.0;
							lfCurrentOperationTime = 0.0;
							lfCurrentConsultationTime = 0.0;
						}
					}
				}
			}
			// 対応中の場合、疲労の計算をします。
			if( this.iGetAttending() == 1 )
			{
				vCalcFatigue( lfTimeCourse );
			}
			if( erPatientAgent != null )
			{
				// なくなったので患者エージェントを削除します。
				if( erPatientAgent.iGetSurvivalFlag() == 0 )
				{
					iConsultationAttending = 0;					// 診察室対応中フラグ
					iOperationAttending = 0;					// 手術室対応中フラグ
					iEmergencyAttending = 0;					// 初療室対応中フラグ
					iEmergencyLevel = 6;						// 緊急度
					iPatientMoveWaitFlag = 0;
					lfCurrentPassOverTime = 0.0;
					lfCurrentEmergencyTime = 0.0;
					lfCurrentOperationTime = 0.0;
					lfCurrentConsultationTime = 0.0;
					erPatientAgent = null;
				}
			}

			if( iInverseSimMode == 0 )
			{
				// 終了100秒前からファイルに書き始めます。（長時間処理のため）
				vWriteFile( iFileWriteMode, lfTotalTime );
			}
			lfTimeCourse += lfSecond;
			lfTotalTime += lfSecond;
		}
		catch( NullPointerException npe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			edae.SetErrorInfo( ERDA_NULLPOINT_ERROR, "action", "ERDoctorAgent", "NULLポイントアクセスエラー", ste[0].getLineNumber() );
			// エラー詳細を出力
			String strMethodName = edae.strGetMethodName();
			String strClassName = edae.strGetClassName();
			String strErrDetail = edae.strGetErrDetail();
			int iErrCode = edae.iGetErrCode();
			int iErrLine = edae.iGetErrorLine();
			cDoctorAgentLog.warning( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
			for( i = 0;i < npe.getStackTrace().length; i++ )
			{
				String str = "クラス名" + "," + npe.getStackTrace()[i].getClassName();
				str += "メソッド名" + "," + npe.getStackTrace()[i].getMethodName();
				str += "ファイル名" + "," + npe.getStackTrace()[i].getFileName();
				str += "行数" + "," + npe.getStackTrace()[i].getLineNumber();
				cDoctorAgentLog.warning( str );
			}
		}
		catch( RuntimeException re )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			edae.SetErrorInfo( ERDA_FATAL_ERROR, "action", "ERDoctorAgent", "不明、および致命的エラー", ste[0].getLineNumber() );
			// エラー詳細を出力
			String strMethodName = edae.strGetMethodName();
			String strClassName = edae.strGetClassName();
			String strErrDetail = edae.strGetErrDetail();
			int iErrCode = edae.iGetErrCode();
			int iErrLine = edae.iGetErrorLine();
			cDoctorAgentLog.warning( re.getLocalizedMessage() + "," + strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
			for( i = 0;i < re.getStackTrace().length; i++ )
			{
				String str = "クラス名" + "," + re.getStackTrace()[i].getClassName();
				str += "メソッド名" + "," + re.getStackTrace()[i].getMethodName();
				str += "ファイル名" + "," + re.getStackTrace()[i].getFileName();
				str += "行数" + "," + re.getStackTrace()[i].getLineNumber();
				cDoctorAgentLog.warning( str );
			}
		}
		catch( IOException ioe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			edae.SetErrorInfo( ERDA_FATAL_ERROR, "action", "ERDoctorAgent", "不明、および致命的エラー", ste[0].getLineNumber() );
			// エラー詳細を出力
			String strMethodName = edae.strGetMethodName();
			String strClassName = edae.strGetClassName();
			String strErrDetail = edae.strGetErrDetail();
			int iErrCode = edae.iGetErrCode();
			int iErrLine = edae.iGetErrorLine();
			cDoctorAgentLog.warning( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
			for( i = 0;i < ioe.getStackTrace().length; i++ )
			{
				String str = "クラス名" + "," + ioe.getStackTrace()[i].getClassName();
				str += "メソッド名" + "," + ioe.getStackTrace()[i].getMethodName();
				str += "ファイル名" + "," + ioe.getStackTrace()[i].getFileName();
				str += "行数" + "," + ioe.getStackTrace()[i].getLineNumber();
				cDoctorAgentLog.warning( str );
			}
		}
	}

	/**
	 * <PRE>
	 *    医師エージェントのログ出力設定をします。
	 * </PRE>
	 * @param log java標準のロガークラスのインスタンス
	 */
	public void vSetLog(Logger log)
	{
		// TODO 自動生成されたメソッド・スタブ
		cDoctorAgentLog = log;
	}

	/**
	 * <PRE>
	 *   シミュレーションの終了時間を設定します。
	 * </PRE>
	 * @param lfEndTime 終了時間(秒指定)
	 */
	public void vSetSimulationEndTime( double lfEndTime )
	{
		lfSimulationEndTime = lfEndTime;
	}

	/**
	 * <PRE>
	 *    逆シミュレーションモードを設定します。
	 * </PRE>
	 * @param iMode 逆シミュレーションのモード
	 */
	public void vSetInverseSimMode( int iMode )
	{
		iInverseSimMode = iMode;
	}


	/**
	 * <PRE>
	 *    クリティカルセクションを設定します。
	 * </PRE>
	 * @param cs クリティカルセクションのインスタンス
	 */
	public void vSetCriticalSection(Object cs)
	{
		// TODO 自動生成されたメソッド・スタブ
		erDoctorCriticalSection = cs;
	}

	/**
	 * <PRE>
	 *    正規乱数を発生させます。-1.0以下、1.0以上が乱数を発生させた結果出力された場合、
	 *    再度乱数を発生させます。乱数発生回数の繰り返し回数は100回とします。
	 * </PRE>
	 * @return	正規乱数の結果(-1.0 \leq rand \leq 1.0)
	 */
	public double normalRand()
	{
		double lfRes = 0.0;
		int i;

		for( i = 0;i < 100; i++ )
		{
			lfRes = rnd.NextNormal();
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
	 *    weibull分布関数の逆関数。
	 * </PRE>
	 * @param lfAlpha 調整パラメータ1
	 * @param lfBeta  調整パラメータ2
	 * @param lfRand  入力値
	 * @return 逆関数値
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
	 * @param lfAlpha 調整パラメータ1
	 * @param lfBeta  調整パラメータ2
	 * @return  ワイブル分布乱数
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
	 *    ファイルの書き込みを行います。
	 * </PRE>
	 * @param iFlag			ファイル書き込みモード
	 * @param lfTime		ファイル書き込み時間
	 * @throws IOException	ファイル処理エラー
	 */
	public void vWriteFile( int iFlag, double lfTime ) throws IOException
	{
		String strData = lfTimeCourse + "," + lfCurrentConsultationTime + "," + lfCurrentOperationTime + "," + lfCurrentEmergencyTime + "," + iTotalConsultationNum +"," + iTotalOperationNum + "," + iTotalEmergencyNum + "," + iDoctorDepartment + "," + iSurgeon +",";
		strData += lfJudgedAISHead + "," + lfJudgedAISFace + "," + lfJudgedAISNeck + "," + lfJudgedAISThorax + "," + lfJudgedAISAbdomen + "," + lfJudgedAISSpine  + "," + lfJudgedAISLowerExtremity + "," + lfJudgedAISUpperExtremity + "," + lfJudgedAISUnspecified;
		// 終了時の書き込みか、特に指定していない場合
		if( iFlag == 0 )
		{
			csvWriteAgentData.vWrite( strData );
		}
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
	 *    メルセンヌツイスターインスタンスを設定します。
	 * </PRE>
	 * @param sfmtRandom メルセンヌツイスターインスタンス
	 */
	public void vSetRandom(utility.sfmt.Rand sfmtRandom )
	{
		// TODO 自動生成されたメソッド・スタブ
		rnd = sfmtRandom;
	}
}
