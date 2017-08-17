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
import utility.node.ERTriageNode;


public class ERNurseAgent extends Agent
{
	private static final long serialVersionUID = -4729964086609110155L;

	public static final int ERNA_SUCCESS					= 0;
	public static final int ERNA_FATAL_ERROR				= -201;
	public static final int ERNA_MEMORYALLOCATE_ERROR		= -202;
	public static final int ERNA_NULLPOINT_ERROR			= -203;
	public static final int ERNA_INVALID_ARGUMENT_ERROR		= -204;
	public static final int ERNA_INVALID_DATA_ERROR			= -205;
	public static final int ERNA_ARRAY_INDEX_ERROR			= -206;
	public static final int ERNA_ZERO_DIVIDE_ERROR			= -207;

	// 看護師の設定パラメータ(定数)
	int iNurseCategory;							// 看護師のカテゴリ
	int iTriageProtocol;						// トリアージプロトコル
	int iTriageProtocolLevel;					// トリアージプロトコルのレベル
	double iTriageYearExperience;				// トリアージ経験年数
	double lfYearExperience;					// 経験年数
	double lfConExperience;						// 経験年数重みパラメータ
	double lfExperienceRate1;					// 経験年数パラメータその１
	double lfExperienceRate2;					// 経験年数パラメータその２
	double lfConExperienceAIS;					// 経験年数重みパラメータ(重症度用)
	double lfExperienceRateAIS1;				// 経験年数パラメータその１(重症度用)
	double lfExperienceRateAIS2;				// 経験年数パラメータその２(重症度用)
	double lfTiredRate;							// 疲労度
	double lfConTired1;							// 疲労度の重みパラメータ1
	double lfConTired2;							// 疲労度の重みパラメータ2
	double lfConTired3;							// 疲労度の重みパラメータ3
	double lfConTired4;							// 疲労度の重みパラメータ4
	double lfAssociationRate;					// 連携度
	double lfObservationTime;					// 観察定期実施時間
	double lfTriageProcessTime;					// トリアージ定期実施時間
	double lfObservationProcessTime;			// 看護師が観察に要する時間
	int iNurseDepartment;						// 看護師の所属部門
	int iNurseId;								// 看護師ID
	int iRoomNumber;							// 所属している部屋番号
	double lfFatigue;							// 看護師の疲労度
	int iCalcFatigueFlag;						// 疲労度に関するフラグ

	// 看護師の設定パラメータ(変数)
	double lfCurrentPassOverTime;				// 看護師さんが現在の患者さんを観察した時間
	double lfCurrentObservationTime;			// 今回の看護師さんが観察した時間
	double lfTotalObservationTime;				// 看護師さんが患者さんを観察した総時間
	double lfTotalTriageTime;					// 看護師が患者にトリアージした総時間
	double lfCurrentTriageTime;					// 看護師さんが現在行ったトリアージの時間
	double lfPrevPassOverTime;					// 看護師さんが観察した一個前の時間

	int iTotalObservationNum;					// 看護師が患者を観察した回数
	int iTotalTriageNum;						// 看護師が患者をトリアージした回数
	int iAttending;								// 看護師が対応中か否かを表すパラメータ

	// メッセージ受信によるパラメータ
	int iTriageProcessFlag;						// トリアージを実施するか否か
	int iEmergencyLevel;						// 緊急度

	int iDoctorDepartment;						// 医師の所属部門
	int iDoctorId;								// 医師のID
	double lfConsultationTime;					// 診察時間

	int iClinicalEngineerDepartment;			// 医療技師の所属部門
	int iClinicalEngineerId;					// 医療技師ID
	double lfExaminationTime;					// 医療技師の検査時間

	int iPatientLocation;						// 患者が現在いる部屋
	int iPatientId;								// 担当患者ID
	double lfWaitTime;							// 患者の待ち時間

	int iFromNurseDepartment;					// 担当した看護師の所属部門
	int iFromNurseId;							// 担当した看護師のID
	double lfFromNurseObservationTime;			// 担当した看護師の観察にかかった時間

	ERPatientAgent erPatientAgent;				// 看護師が対応している患者エージェント
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
	String strRespirationStatus;				// 患者の呼吸状態
	double lfPatientSpO2;						// 患者の動脈血山荘飽和度(SpO2)
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

	private utility.sfmt.Rand rnd;						// 乱数クラス

	private ArrayList<Long> ArrayListDoctorAgentIds;			// 全医師のID
	private ArrayList<Long> ArrayListNurseAgentIds;				// 全看護師のID
	private ArrayList<Long> ArrayListClinicalEngineerAgentIds;	// 全医療技師のID
	private ArrayList<Long> ArrayListPatientAgentIds;			// 全患者のID

	private Queue<Message> mesQueueData;						// メッセージキューを保持する変数
	private CCsv csvWriteAgentData;								// 終了データ出力ファイル
	private CCsv csvWriteAgentStartData;						// 開始データ出力ファイル

	private double lfTimeCourse;								// 経過時間
	private int iPatientMoveWaitFlag;							// 患者が移動しているときの待ち中であることを表すフラグ

	private Logger cNurseAgentLog;

	private double lfSimulationEndTime;							// シミュレーション終了時間
	private double lfTotalTime;									// シミュレーション総時間
	private double lfTimeStep;									// シミュレーション実行間隔
	private double lfJudgeTriageProcessCount;					// トリアージプロセス判定カウント
	private double lfJudgeObservationProcessCount;				// 観察プロセス判定カウント

	private int iInverseSimMode;								// 逆シミュレーションモード

	ERTriageNode erTriageNode;

	private Object erNurseCriticalSection;						// クリティカルセクション用

	private int iFileWriteMode;									// 長時間シミュレーションファイル出力モード

	/**
	 * <PRE>
	 *    コンストラクタ
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public ERNurseAgent()
	{
		vInitialize();
	}

	public void vInitialize()
	{
		String strFileName = "";
//		long seed;
//		seed = (long)(Math.random()*Long.MAX_VALUE);
//		rnd = null;
//		rnd = new sfmt.Sfmt( (int)seed );

		lfYearExperience					= 5.0;
		lfConExperience						= 0.61;
		lfExperienceRate1					= 2.1;
		lfExperienceRate2					= 0.9;
		lfConExperienceAIS					= 0.14;		// 経験年数重みパラメータ(重症度用)
		lfExperienceRateAIS1				= 0.2;		// 経験年数パラメータその１(重症度用)
		lfExperienceRateAIS2				= 1.1;		// 経験年数パラメータその２(重症度用)
		iTriageProtocol = 0;										// トリアージプロトコル
		iTriageProtocolLevel = 0;									// トリアージプロトコルのレベル
		iTriageProcessFlag = 0;										// トリアージを実施するか否か
		iCalcFatigueFlag = 0;										// 疲労度に関するフラグ
		iEmergencyLevel = 0;										// 緊急度
		iNurseDepartment = 0;										// 担当部署
		lfObservationTime = 0.0;									// 定期観察時間
		lfConsultationTime = 0.0;									// 診察時間
		lfTriageProcessTime = 0.0;									// トリアージ定期実施時間
		lfCurrentTriageTime = 0.0;									// 現在実施しているトリアージ時間
		lfWaitTime = 0.0;											// 患者の待ち時間
		lfCurrentPassOverTime = 0.0;								// 看護師さんが現在の患者さんを観察した時間
		lfTotalObservationTime = 0.0;								// 看護師さんが患者さんを観察した総合時間
		iAttending = 0;												// 看護師が対応中か否かを表すパラメータ
		lfJudgedAISHead = 0.0;										// 頭部のAIS
		lfJudgedAISFace = 0.0;										// 顔面のAIS
		lfJudgedAISNeck = 0.0;										// 頸部（首）のAIS
		lfJudgedAISThorax = 0.0;									// 胸部のAIS
		lfJudgedAISAbdomen = 0.0;									// 腹部のAIS
		lfJudgedAISSpine = 0.0;										// 脊椎のAIS
		lfJudgedAISUpperExtremity = 0.0;							// 上肢のAIS
		lfJudgedAISLowerExtremity = 0.0;							// 下肢のAIS
		lfJudgedAISUnspecified = 0.0;								// 特定部位でない。（体表・熱傷・その他外傷）
		ArrayListDoctorAgentIds = new ArrayList<Long>();			// 全医師のID
		ArrayListNurseAgentIds = new ArrayList<Long>();				// 全看護師のID
		ArrayListPatientAgentIds = new ArrayList<Long>();			// 全患者のID
		ArrayListClinicalEngineerAgentIds = new ArrayList<Long>();	// 全医療技師のID
		lfPatientSpO2 = 0.0;										// 患者の動脈血山荘飽和度(SpO2)
		lfUpperExtremityNRS = 0.0;									// 上肢痛みの強さのスケール
		lfLowerExtremityNRS = 0.0;									// 下肢痛みの強さのスケール
		lfUnspecifiedNRS = 0.0;										// 体表、熱傷、その他外傷の痛みの強さのスケール
		lfSpineNRS = 0.0;											// 脊椎痛みの強さのスケール
		lfAbdomenNRS = 0.0;											// 腹部痛みの強さのスケール
		lfThoraxNRS = 0.0;											// 胸部痛みの強さのスケール
		lfNeckNRS = 0.0;											// 頸部痛みの強さのスケール
		lfFaceNRS = 0.0;											// 顔面痛みの強さのスケール
		lfHeadNRS = 0.0;											// 頭部痛みの強さのスケール
		mesQueueData = new LinkedList<Message>();					// 看護師エージェントが扱うメッセージキュー

		lfTimeCourse = 0.0;
		iPatientMoveWaitFlag = 0;

//		try
//		{
//			csvWriteAgentData					= new CCsv();
//			strFileName							= "er/nr/ernr_end" + this.getId() + ".csv";
//			csvWriteAgentData.vOpen( strFileName, "write");
//			csvWriteAgentStartData				= new CCsv();
//			strFileName							= "er/nr/ernr_start" + this.getId() + ".csv";
//			csvWriteAgentStartData.vOpen( strFileName, "write");
//		}
//		catch( IOException ioe )
//		{
//
//		}
		iTotalObservationNum	= 0;								// 看護師が患者を観察した回数
		iTotalTriageNum			= 0;								// 看護師が患者をトリアージした回数
		iAttending				= 0;								// 看護師が対応中か否かを表すパラメータ
		lfTotalTime				= 0;								// シミュレーション総時間
		lfTimeStep				= 1.0;
		lfJudgeTriageProcessCount = 0.0;

		iFileWriteMode			= 0;
	}

	/**
	 * <PRE>
	 *    ファイルの読み込みを行います。
	 * </PRE>
	 * @param iFileWriteModeFlag	ファイル書き込みモード
	 * 								0 1ステップごとのデータを書き込み
	 * 								1 最初と最後各100ステップ程度データを書き込み
	 * @throws IOException ファイル読み込みエラー
	 */
	public void vSetReadWriteFile( int iFileWriteModeFlag ) throws IOException
	{
		String strFileName = "";
		iFileWriteMode = iFileWriteModeFlag;
		if( iFileWriteModeFlag == 1 )
		{
			csvWriteAgentData					= new CCsv();
			strFileName							= "er/nr/ernr_end" + this.getId() + ".csv";
			csvWriteAgentData.vOpen( strFileName, "write");
			csvWriteAgentStartData				= new CCsv();
			strFileName							= "er/nr/ernr_start" + this.getId() + ".csv";
			csvWriteAgentStartData.vOpen( strFileName, "write");
		}
		else
		{
			csvWriteAgentData					= new CCsv();
			strFileName							= "er/nr/ernr_end" + this.getId() + ".csv";
			csvWriteAgentData.vOpen( strFileName, "write");
		}
	}

	/**
	 * <PRE>
	 *    終了処理を実行します。
	 * </PRE>
	 * @throws IOException ファイルクローズ処理エラー
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
	 * @param engine FUSEシミュレーションエンジン
	 */
	public void vSetSimulationEngine( SimulationEngine engine )
	{
		engine.addAgent(this);
	}

	/**
	 * <PRE>
	 *    ナースの患者観察プロセスを実行します。
	 *    観察プロセスか、トリアージプロセスを実行します。
	 * </PRE>
	 * @param iTriageProcessFlagData トリアージプロセス番号
	 *                               0 非トリアージ
	 *                               1 トリアージ
	 *                               それ以外　エラー
	 * @param erPAgent				 対象となる患者エージェント
	 * @throws ERNurseAgentException 看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vImplementNurseProcess( int iTriageProcessFlagData, ERPatientAgent erPAgent ) throws ERNurseAgentException
	{
		ERNurseAgentException cERNae;
		int iEmergency = 0;
		cERNae = new ERNurseAgentException();

		if( iTriageProcessFlagData == 0 )
		{
			// 通常プロセス
			iEmergency = iJudgeAIS( erPAgent );
		}
		else if( iTriageProcessFlagData == 1 )
		{
			// トリアージプロセス
			iEmergency = iJudgeTriageProcess( iTriageProtocol, iTriageProtocolLevel, erPAgent );
			// トリアージを実施した回数をカウントします。
			iTotalTriageNum++;
		}
		else
		{
			// エラー
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERNurseAgent", "vImplementNurseProcess", "不正な数値です。" );
			throw( cERNae );
		}
		// 観察を実施した回数をカウントします。
		iTotalObservationNum++;
		iEmergencyLevel = iEmergency;
	}

	/**
	 * <PRE>
	 *     看護師エージェントが通常プロセスに基づいて
	 *     患者エージェントの状況を判断します。
	 * </PRE>
	 * @param erPAgent 対象となる患者エージェント
	 * @return 患者エージェントの緊急度
	 * @throws ERNurseAgentException 看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private int iJudgeAIS( ERPatientAgent erPAgent) throws ERNurseAgentException
	{
		ERNurseAgentException cERNae;
		int iTraumaNum = 0;
		int iEmergency = 0;
		int iMinEmergency = 10;

		// 患者から状態をメッセージで受診します
		cERNae = new ERNurseAgentException();

		iTraumaNum = erPAgent.iGetNumberOfTrauma();
		if( iTraumaNum < 0 )
		{
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERDoctorAgent", "iJudgePainStatus", "不正な数値です。" );
			throw( cERNae );
		}

		// いったん初期化します。
		lfUpperExtremityNRS = 0.0;									// 上肢痛みの強さのスケール
		lfLowerExtremityNRS = 0.0;									// 下肢痛みの強さのスケール
		lfUnspecifiedNRS = 0.0;										// 体表、熱傷、その他外傷の痛みの強さのスケール
		lfSpineNRS = 0.0;											// 脊椎痛みの強さのスケール
		lfAbdomenNRS = 0.0;											// 腹部痛みの強さのスケール
		lfThoraxNRS = 0.0;											// 胸部痛みの強さのスケール
		lfNeckNRS = 0.0;											// 頸部痛みの強さのスケール
		lfFaceNRS = 0.0;											// 顔面痛みの強さのスケール
		lfHeadNRS = 0.0;											// 頭部痛みの強さのスケール
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

//		// AISからの緊急度判定（NRSをもとにしたもの）
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
//
//		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;

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
		else if( 8 <= lfUpperExtremityNRS && lfUpperExtremityNRS <= 10 )	iEmergency = iJudgeSevereTrauma();
		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
		if( 0 <= lfLowerExtremityNRS && lfLowerExtremityNRS <= 3 )			iEmergency = iJudgeMildTrauma();
		else if( 4 <= lfLowerExtremityNRS && lfLowerExtremityNRS <= 7 )		iEmergency = iJudgeModerateTrauma();
		else if( 8 <= lfLowerExtremityNRS && lfLowerExtremityNRS <= 10 )	iEmergency = iJudgeSevereTrauma();
		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
		if( 0 <= lfUnspecifiedNRS && lfUnspecifiedNRS <= 3 )				iEmergency = iJudgeMildTrauma();
		else if( 4 <= lfUnspecifiedNRS && lfUnspecifiedNRS <= 7 )			iEmergency = iJudgeModerateTrauma();
		else if( 8 <= lfUnspecifiedNRS && lfUnspecifiedNRS <= 10 )			iEmergency = iJudgeSevereTrauma();
		iMinEmergency = iMinEmergency > iEmergency ? iEmergency : iMinEmergency;
		return iMinEmergency;
	}

	/**
	 * <PRE>
	 *   患者から頭部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryHeadStatus 患者が訴える頭部のAIS値
	 * @throws ERNurseAgentException 看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeAISHeadStatus( String strCurrentInjuryHeadStatus ) throws ERNurseAgentException
	{
		ERNurseAgentException cERNae;
		cERNae = new ERNurseAgentException();

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
			lfJudgedAISUpperExtremity = 0.3*rnd.NextUnif()+2;
			lfHeadNRS = 4;
		}
		else if( strCurrentInjuryHeadStatus == "けっこう痛い" )
		{
			lfJudgedAISHead = 0.3*rnd.NextUnif()+2.3;
			lfUpperExtremityNRS = 5;
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
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERNurseAgent", "vJudgeAISHeadStatus", "不正な数値です。" );
			throw( cERNae );
		}
	}
	/**
	 * <PRE>
	 *   患者から顔面部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryFaceStatus 患者が訴える顔面のAIS値
	 * @throws ERNurseAgentException 看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeAISFaceStatus( String strCurrentInjuryFaceStatus ) throws ERNurseAgentException
	{
		ERNurseAgentException cERNae;
		cERNae = new ERNurseAgentException();

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
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERNurseAgent", "vJudgeAISFaceStatus", "不正な数値です。" );
			throw( cERNae );
		}
	}

	/**
	 * <PRE>
	 *   患者から頸部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryNeckStatus 患者が訴える頸部のAIS値
	 * @throws ERNurseAgentException 看護師エージェント例外
	 * @since 2015/08/04
	 */
	private void vJudgeAISNeckStatus( String strCurrentInjuryNeckStatus ) throws ERNurseAgentException
	{
		ERNurseAgentException cERNae;
		cERNae = new ERNurseAgentException();

		if( strCurrentInjuryNeckStatus == "痛くない")
		{
			lfJudgedAISNeck = 0.0;
			lfNeckNRS = 0;
		}
		// 脊椎外傷軽度
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
		// 脊椎外傷中等度
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
		// 重症、重篤(脊椎の機能を失う)
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
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERNurseAgent", "vJudgeAISNeckStatus", "不正な数値です。" );
			throw( cERNae );
		}
	}

	/**
	 * <PRE>
	 *   患者から胸部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryThoraxStatus 患者が訴える胸部のAIS値
	 * @throws ERNurseAgentException 看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeAISThoraxStatus( String strCurrentInjuryThoraxStatus ) throws ERNurseAgentException
	{
		ERNurseAgentException cERNae;
		cERNae = new ERNurseAgentException();

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
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERNurseAgent", "vJudgeAISThoraxStatus", "不正な数値です。" );
			throw( cERNae );
		}
	}

	/**
	 * <PRE>
	 *   患者から腹部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryAbdomenStatus 患者エージェントからのメッセージ
	 * @throws ERNurseAgentException 看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeAISAbdomenStatus( String strCurrentInjuryAbdomenStatus ) throws ERNurseAgentException
	{
		ERNurseAgentException cERNae;
		cERNae = new ERNurseAgentException();

		if( strCurrentInjuryAbdomenStatus == "痛くない")
		{
			lfJudgedAISAbdomen = 0.0;
			lfAbdomenNRS = 0;
		}
		// 脊椎外傷軽度
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
		// 脊椎外傷中等度
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
		// 重症、重篤(脊椎の機能を失う)
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
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERNurseAgent", "vJudgeAISAbdomenStatus", "不正な数値です。" );
			throw( cERNae );
		}
	}

	/**
	 * <PRE>
	 *   患者から脊椎状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjurySpineStatus 患者が訴える脊椎のAIS値
	 * @throws ERNurseAgentException 看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeAISSpineStatus( String strCurrentInjurySpineStatus ) throws ERNurseAgentException
	{
		ERNurseAgentException cERNae;
		cERNae = new ERNurseAgentException();

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
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERNurseAgent", "vJudgeAISSpineStatus", "不正な数値です。" );
			throw( cERNae );
		}
	}

	/**
	 * <PRE>
	 *   患者から上肢状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryUpperExtremityStatus 患者が訴える上肢のAIS値
	 * @throws ERNurseAgentException 看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeAISUpperExtrimityStatus( String strCurrentInjuryUpperExtremityStatus ) throws ERNurseAgentException
	{
		ERNurseAgentException cERNae;
		cERNae = new ERNurseAgentException();

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
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERNurseAgent", "vJudgeAISUpperExtremityStatus", "不正な数値です。" );
			throw( cERNae );
		}
	}

	/**
	 * <PRE>
	 *   患者から下肢状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryLowerExtremityStatus 患者が訴える下肢のAIS値
	 * @throws ERNurseAgentException 看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeAISLowerExtrimityStatus( String strCurrentInjuryLowerExtremityStatus ) throws ERNurseAgentException
	{
		ERNurseAgentException cERNae;
		cERNae = new ERNurseAgentException();

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
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERNurseAgent", "vJudgeAISLowerExtremityStatus", "不正な数値です。" );
			throw( cERNae );
		}
	}

	/**
	 * <PRE>
	 *   患者から腹部状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentInjuryUnspecifiedStatus 患者が訴える表面、熱傷、その他外傷のAIS値
	 * @throws ERNurseAgentException 看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeAISUnspecifiedStatus( String strCurrentInjuryUnspecifiedStatus ) throws ERNurseAgentException
	{
		ERNurseAgentException cERNae;
		cERNae = new ERNurseAgentException();

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
		else
		{
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERNurseAgent", "vJudgeAISUnspecifiedStatus", "不正な数値です。" );
			throw( cERNae );
		}
	}

	/**
	 * <PRE>
	 *   患者から呼吸状態をメッセージで受信し、状態を把握します。
	 * </PRE>
	 * @param strCurrentRespirationStatus 患者の呼吸状態
	 * @throws ERNurseAgentException 看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/07/21
	 */
	private void vJudgeSpO2Status( String strCurrentRespirationStatus ) throws ERNurseAgentException
	{
		ERNurseAgentException cERNae;
		cERNae = new ERNurseAgentException();

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
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERNurseAgent", "vJudgeSpO2Status", "不正な数値です。" );
			throw( cERNae );
		}
	}

	/**
	 * <PRE>
	 *    トリアージプロトコルに基づいて患者の状況を判断します。
	 * </PRE>
	 * @param iTriageProtocolData 1 JTAS
	 *                  	      2 CTAS
	 *                      	  3 ProNQA
	 *                      	  4 ESI
	 *                     		  5 ・・・
	 * @param iProtocolLevelData トリアージのプロトコルレベル(緊急度基準は3段階、4段階、5段階が設定可能。)
	 * @param erPAgent			担当する患者エージェント
	 * @return 患者エージェントの緊急度
	 * @throws ERNurseAgentException 看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	private int iJudgeTriageProcess( int iTriageProtocolData, int iProtocolLevelData, ERPatientAgent erPAgent ) throws ERNurseAgentException
	{
		int iEmergency = 6;
		ERNurseAgentException cERNae = new ERNurseAgentException();

		// 現在の患者のAIS値を取得します。

		iJudgeAIS( erPAgent );

		if( iTriageProtocolData == 1 )
		{
			// JTASプロトコル
			iEmergency = iJtasProtocol( iProtocolLevelData, erPAgent );
		}
		else if( iTriageProtocolData == 2 )
		{
			// CTASプロトコル
			iEmergency = iCtasProtocol( iProtocolLevelData, erPAgent );
		}
		else if( iTriageProtocolData == 3 )
		{
			// ProNQAプロトコル
			iEmergency = iProNQAProtocol( iProtocolLevelData, erPAgent );
		}
		else if( iTriageProtocolData == 4 )
		{
			// ESIプロトコル
			iEmergency = iEsiProtocol( iProtocolLevelData, erPAgent );
		}
		// 院外トリアージプロセス
		else if( iTriageProtocolData == 5 )
		{
			// STARTプロトコル
			iEmergency = iStartProtocol( iProtocolLevelData, erPAgent );
		}
		else
		{
			// エラー
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERNurseAgent", "vJudgeTriageProcess", "不正な数値です。" );
			throw( cERNae );
		}
//		iEmergency = 5;		// テスト用(診察プロセス強制実行)
//		iEmergency = 4;		// テスト用(観察プロセス強制実行)
		return iEmergency;
	}

	/**
	 * <PRE>
	 *    JTASプロトコルの判定を実施します。
	 * </PRE>
	 * @param iProtocolLevelData	トリアージのレベル
	 * @param erPAgent				患者エージェント
	 * @return 患者エージェントの緊急度
	 * @throws ERNurseAgentException 看護師エージェント例外
	 */
	private int iJtasProtocol( int iProtocolLevelData, ERPatientAgent erPAgent ) throws ERNurseAgentException
	{
		int i;
		int iEmergency = 6;
		int iEmergencyRespiration = 0;
		int aiEmergency[] = new int[12];

	// CTASレベル判定

		// 患者エージェントから状態を取得します。
		for( i = 0;i < aiEmergency.length; i++ )
		{
			aiEmergency[i] = 5;
		}

		// バイタルサインの判定を行います。
		if( isJudgeVitalSign( erPAgent ) == false )
		{
			// 正常でない場合は詳細に見ます。

			// 脈が明らかに低い場合はCTASレベル1とします。
			if( erPAgent.lfGetPulse() <= 30.0 )
			{
				iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
				return iEmergency;
			}
			// 脈が明らかに多い場合はCTASレベルを1とします。
			else if( erPAgent.lfGetPulse() >=  140.0 )
			{
				iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
				return iEmergency;
			}

			// 呼吸回数が明らかに低い、あるいはない場合はCTASレベル1とします。
			if( erPAgent.lfGetRr() <= 5.0 )
			{
				iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
				return iEmergency;
			}
			// 呼吸回数があまりにも多い場合はCTASレベルを1とします。
			else if( erPAgent.lfGetRr() >= 40 )
			{
				iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
				return iEmergency;
			}

			// けいれん状態の場合はCTASレベル1とします。
			if( erPAgent.strGetInjuryUnspecifiedStatus() == "けいれん状態" )
			{
				iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
				return iEmergency;
			}
		}

		// 呼吸のCTAS判定を実施します。
		vJudgeSpO2Status( erPAgent.strGetSpO2Status() );
		aiEmergency[0] = iJudgeSpO2();
		if( aiEmergency[0] == 1 )
		{
			// CTASレベル1と判定できる状態ならば、蘇生レベルの1を返却します。
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}

		// 循環動態のCTAS判定を実施します。
		aiEmergency[1] = iJudgeCirculatoryDynamics( erPAgent );
		if( aiEmergency[1] == 1 )
		{
			// CTASレベル1と判定できるならば、返却します。
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}

		// 意識状態のCTAS判定を実施します。
		aiEmergency[2] = iJudgeConsciousness( erPAgent );
		if( aiEmergency[2] == 1 )
		{
			// CTASレベル1と判定できるならば、返却します。
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}

		// 体温のCTAS判定を実施します。
		aiEmergency[3] = iJudgeBodyTemperature( erPAgent );

		// 出血性疾患のCTAS判定を実施します。
		aiEmergency[4] = iJudgeBloodIssue( erPAgent );

		// 疼痛のCTAS判定を実施します。
		aiEmergency[5] = iJudgePainStatus( erPAgent );
		if( aiEmergency[5] == 1 )
		{
			// CTASレベル1と判定できるならば、返却します。
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}

		// 明らかにわかる緊急状態に関してはレベルを1とします。
		if( erPAgent.strGetSkinSignStatus() == "冷たい皮膚" )
		{
			aiEmergency[6] = 1;
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}
		if( erPAgent.strGetFaceSignStatus() == "チアノーゼ" )
		{
			aiEmergency[7] = 1;
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}
		if( erPAgent.strGetHeartSignStatus() == "心停止" )
		{
			aiEmergency[8] = 1;
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}
		if( erPAgent.strGetBodyTemperatureSignStatus() == "低体温症" ||
			erPAgent.strGetBodyTemperatureSignStatus() == "過高温"   ||
			erPAgent.strGetBodyTemperatureSignStatus() == "超過高温" )
		{
			aiEmergency[9] = 1;
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}

		// 受傷機転のCTAS判定を実施します。

		// 最終的に判定レベルが最も高いものを判定します。
		iEmergency = 6;
		for( i = 0;i < aiEmergency.length; i++ )
		{
			if( iEmergency > aiEmergency[i] && aiEmergency[i] != 0 )
			{
				iEmergency = aiEmergency[i];
			}
		}
		// 3段階の場合は以下の通りにします。
		if( iProtocolLevelData == 3 )
		{
			if( iEmergency == 1 || iEmergency == 2 )
			{
				iEmergency = 2;
			}
			else if( iEmergency == 3 || iEmergency == 4 )
			{
				iEmergency = 3;
			}
			else if( iEmergency == 5 )
			{
				iEmergency = 5;
			}
		}
		// 4段階の場合は次の通りにします。
		else if( iProtocolLevelData == 4 )
		{
			if( iEmergency == 1 )
			{
				iEmergency = 1;
			}
			else if( iEmergency == 2 )
			{
				iEmergency = 2;
			}
			else if( iEmergency == 3 )
			{
				iEmergency = 3;
			}
			else if( iEmergency == 4 || iEmergency == 5 )
			{
				iEmergency = 4;
			}
		}
		return iEmergency;
	}

	/**
	 * <PRE>
	 *    CTASプロトコルを実施します。
	 * </PRE>
	 * @param iProtocolLevelData 緊急度基準
	 * @param erPAgent			 対象となる患者エージェント
	 * @return トリアージ緊急度
	 * @throws ERNurseAgentException 看護師エージェント例外
	 */
	private int iCtasProtocol( int iProtocolLevelData, ERPatientAgent erPAgent ) throws ERNurseAgentException
	{
		int i;
		int iEmergency = 6;
		int iEmergencyRespiration = 0;
		int aiEmergency[] = new int[7];

	// CTASレベル判定

		// 患者エージェントから状態を取得します。
		for( i = 0;i < 7; i++ )
		{
			aiEmergency[i] = 5;
		}

		// バイタルサインの判定を行います。
		if( isJudgeVitalSign( erPAgent ) == false )
		{
			// 正常でない場合は詳細に見ます。

			// 脈が明らかに低い場合はCTASレベル1とします。
			if( erPAgent.lfGetPulse() <= 30.0 )
			{
				iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
				return iEmergency;
			}

			// 呼吸回数が明らかに低い、あるいはない場合はCTASレベル1とします。
			if( erPAgent.lfGetRr() <= 5.0 )
			{
				iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
				return iEmergency;
			}

			// けいれん状態の場合はCTASレベル1とします。
			if( erPAgent.strGetInjuryUnspecifiedStatus() == "けいれん状態" )
			{
				iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
				return iEmergency;
			}
		}

		// 呼吸のCTAS判定を実施します。
		vJudgeSpO2Status( erPAgent.strGetSpO2Status() );
		aiEmergency[0] = iJudgeSpO2();
		if( aiEmergency[0] == 1 )
		{
			// CTASレベル1と判定できる状態ならば、蘇生レベルの1を返却します。
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}

		// 循環動態のCTAS判定を実施します。
		aiEmergency[2] = iJudgeCirculatoryDynamics( erPAgent );
		if( aiEmergency[2] == 1 )
		{
			// CTASレベル1と判定できるならば、返却します。
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}

		// 意識状態のCTAS判定を実施します。
		aiEmergency[3] = iJudgeConsciousness( erPAgent );
		if( aiEmergency[3] == 1 )
		{
			// CTASレベル1と判定できるならば、返却します。
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}

		// 体温のCTAS判定を実施します。
		aiEmergency[4] = iJudgeBodyTemperature( erPAgent );

		// 出血性疾患のCTAS判定を実施します。
		aiEmergency[5] = iJudgeBloodIssue( erPAgent );

		// 疼痛のCTAS判定を実施します。
		aiEmergency[6] = iJudgePainStatus( erPAgent );
		if( aiEmergency[6] == 1 )
		{
			// CTASレベル1と判定できるならば、返却します。
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
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
		// 3段階の場合は以下の通りにします。
		if( iProtocolLevelData == 3 )
		{
			if( iEmergency == 1 || iEmergency == 2 )
			{
				iEmergency = 2;
			}
			else if( iEmergency == 3 || iEmergency == 4 )
			{
				iEmergency = 3;
			}
			else if( iEmergency == 5 )
			{
				iEmergency = 5;
			}
		}
		// 4段階の場合は次の通りにします。
		else if( iProtocolLevelData == 4 )
		{
			if( iEmergency == 1 )
			{
				iEmergency = 1;
			}
			else if( iEmergency == 2 )
			{
				iEmergency = 2;
			}
			else if( iEmergency == 3 )
			{
				iEmergency = 3;
			}
			else if( iEmergency == 4 || iEmergency == 5 )
			{
				iEmergency = 4;
			}
		}
		return iEmergency;
	}


	private int iProNQAProtocol( int iProtocolLevelData, ERPatientAgent erPAgent )
	{
		int iEmergency = 0;
		return iEmergency;
	}

	/**
	 * <PRE>
	 *    アメリカのトリアージプロトコルのESIを適用します。
	 * </PRE>
	 * @param iProtocolLevelData 緊急度基準
	 * @param erPAgent			  対象となる患者エージェント
	 * @return					  トリアージ緊急度
	 * @throws ERNurseAgentException 看護師エージェント例ギア
	 */
	private int iEsiProtocol( int iProtocolLevelData, ERPatientAgent erPAgent ) throws ERNurseAgentException
	{
		int i;
		int iExaminationCount = 0;
		int iEmergency = 6;
		int iEmergencyRespiration = 0;
		int aiEmergency[] = new int[7];

	// ESIレベル判定

		// 患者エージェントから状態を取得します。
		for( i = 0;i < 7; i++ )
		{
			aiEmergency[i] = 5;
		}

	// ESIレベル1の判定

		// 脈が明らかに低い場合はESIレベル1とします。
		if( erPAgent.lfGetPulse() <= 30.0 )
		{
			iEmergency = 1;
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}

		// 呼吸回数が明らかに低い、あるいはない場合はESIレベル1とします。
		if( erPAgent.lfGetRr() <= 5.0 )
		{
			iEmergency = 1;
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}

		// けいれん状態の場合はESIレベル1とします。
		if( erPAgent.strGetInjuryUnspecifiedStatus() == "けいれん状態" )
		{
			iEmergency = 1;
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}

		// 酸素飽和度が90%以下の場合はESIレベル1とします。
		if( erPAgent.lfGetSpO2() < 0.9 )
		{
			iEmergency = 1;
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}

		// 緊急度レベル1判定に該当しなかったので5とします。
		aiEmergency[0] = 5;

	// ESIレベル2の判定

		// 意識状態のESI判定を実施します。
		aiEmergency[1] = iJudgeConsciousness( erPAgent );
		if( aiEmergency[1] == 1 )
		{
			// ESIレベル2と判定できるならば、返却します。
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}
		// 疼痛のCTAS判定を実施します。
		aiEmergency[2] = iJudgePainStatus( erPAgent );
		if( aiEmergency[2] == 1 )
		{
			// ESIレベル2と判定できるならば、返却します。
			iEmergency =  iProtocolLevelData == 3 ? 2 : 1;
			return iEmergency;
		}

	// ESIレベル4,5の判定

		// 検査個所が複数個所かどうかからESIレベルを判定します。
		// 検査個所0　　　ESIレベル5の判定へ
		// 検査個所1　　　ESIレベル4の判定へ
		// 検査個所複数　 ESIレベル3の判定へ
		// 判定は推定
		// 傷病状態が軽症程度の場合は、緊急度をESI5とする。
		iExaminationCount = 0;
		if( lfJudgedAISHead >= 2.0 )			iExaminationCount++;
		if( lfJudgedAISFace >= 2.0 )			iExaminationCount++;
		if( lfJudgedAISNeck >= 2.0 )			iExaminationCount++;
		if( lfJudgedAISAbdomen >= 2.0 )			iExaminationCount++;
		if( lfJudgedAISThorax >= 2.0 )			iExaminationCount++;
		if( lfJudgedAISSpine >= 2.0 )			iExaminationCount++;
		if( lfJudgedAISLowerExtremity >= 2.0 )	iExaminationCount++;
		if( lfJudgedAISUpperExtremity >= 2.0 )	iExaminationCount++;
		if( lfJudgedAISUnspecified >= 2.0 )		iExaminationCount++;

		// バイタルサインにより判定。
		if( erPAgent.lfGetSbp() > 150 )
		{
			iExaminationCount++;
		}
		if( erPAgent.lfGetDbp() < 50 )
		{
			iExaminationCount++;
		}
		if( isJudgeVitalSign(erPAgent) == false )
		{
			// 悪い場合は複数個所を見る必要があるので、バイタルサイン判定へ移る。
			iExaminationCount++;
		}
		if( iExaminationCount == 0 )
		{
			aiEmergency[3] = 5;
			aiEmergency[3] = iProtocolLevelData == 4 ? 4 : 5;
			return aiEmergency[3];
		}
		else if( iExaminationCount == 1 )
		{
			// 検査部位が一か所である場合はESIレベルを4に設定する。
			if( erPAgent.iGetNumberOfTrauma() == 1 )
			{
				aiEmergency[3] = 4;
				aiEmergency[3] = iProtocolLevelData == 3 ? 5 : 4;
				return aiEmergency[3];
			}
		}
		// 複数個所だったので緊急度レベルを3とします。
		aiEmergency[3] = 3;

	// それ以外はバイタルサインの判定に移ります。

	// ESIレベル3の判定

		// バイタルサインから判定します。
		if( erPAgent.lfGetAge() <= 0.3 )
		{
			if( erPAgent.lfGetSpO2() < 0.92 )
			{
				aiEmergency[4] = 2;
				return aiEmergency[4];
			}
			if( erPAgent.lfGetPulse() > 180.0 )
			{
				aiEmergency[4] = 2;
				return aiEmergency[4];
			}
			if( erPAgent.lfGetRr() > 50.0 )
			{
				aiEmergency[4] = 2;
				return aiEmergency[4];
			}
		}
		else if( 0.3 < erPAgent.lfGetAge() && erPAgent.lfGetAge() <= 3.0 )
		{
			if( erPAgent.lfGetSpO2() < 0.92 )
			{
				aiEmergency[4] = 2;
				return aiEmergency[4];
			}
			if( erPAgent.lfGetPulse() > 160.0 )
			{
				aiEmergency[4] = 2;
				return aiEmergency[4];
			}
			if( erPAgent.lfGetRr() > 40.0 )
			{
				aiEmergency[4] = 2;
				return aiEmergency[4];
			}
		}
		else if( 3.0 < erPAgent.lfGetAge() && erPAgent.lfGetAge() <= 8.0 )
		{
			if( erPAgent.lfGetSpO2() < 0.92 )
			{
				aiEmergency[4] = 2;
				return aiEmergency[4];
			}
			if( erPAgent.lfGetPulse() > 140.0 )
			{
				aiEmergency[4] = 2;
				return aiEmergency[4];
			}
			if( erPAgent.lfGetRr() > 30.0 )
			{
				aiEmergency[4] = 2;
				return aiEmergency[4];
			}
		}
		else if( erPAgent.lfGetAge() > 8.0 )
		{
			if( erPAgent.lfGetSpO2() < 0.92 )
			{
				aiEmergency[4] = 2;
				return aiEmergency[4];
			}
			if( erPAgent.lfGetPulse() > 100.0 )
			{
				aiEmergency[4] = 2;
				return aiEmergency[4];
			}
			if( erPAgent.lfGetRr() > 20.0 )
			{
				aiEmergency[4] = 2;
				return aiEmergency[4];
			}
		}

		// 該当する部分がなかったので、緊急度レベルを3とします。
		aiEmergency[4] = 3;

		iEmergency = 6;
		for( i = 0;i < 7; i++ )
		{
			iEmergency = iEmergency > aiEmergency[i] ? aiEmergency[i] : iEmergency;
		}

		return iEmergency;
	}

	private int iStartProtocol( int iProtocolLevelData, ERPatientAgent erPAgent )
	{
		int iEmergency = 0;
		return iEmergency;
	}

	/**
	 * <PRE>
	 *    患者の意識レベルを取得します。
	 * </PRE>
	 * @param strConsciousnessStatus 患者の意識の状態
	 * @throws ERNurseAgentException 看護師エージェント例外
	 */
	private void vJudgeConsciousnessStatus(String strConsciousnessStatus) throws ERNurseAgentException
	{
		// TODO 自動生成されたメソッド・スタブ
		ERNurseAgentException cERNae;
		cERNae = new ERNurseAgentException();

		if( strConsciousnessStatus == "大きな音のみ反応")
		{
			lfPatientGcsLevel = 6.0*rnd.NextUnif()+3.0;
		}
		else if( strConsciousnessStatus == "話すと不適切な反応")
		{
			lfPatientGcsLevel = 10.0*rnd.NextUnif()+3.0;
		}
		else if( strConsciousnessStatus == "特に問題なし" )
		{
			lfPatientGcsLevel = 14.0*rnd.NextUnif()+1.0;
		}
		else
		{
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERNurseAgent", "vJudgeConsciousnessStatus", "不正な数値です。" );
			throw( cERNae );
		}
	}

	/**
	 * <PRE>
	 *    疼痛のCTASプロセスを判定します。
	 * </PRE>
	 * @param erPAgent 対応している患者エージェント
	 * @return		   トリアージ緊急度
	 * @throws ERNurseAgentException 看護師エージェント例外
	 */
	private int iJudgePainStatus(ERPatientAgent erPAgent) throws ERNurseAgentException
	{
		ERNurseAgentException cERNae;
		int iTraumaNum = 0;
		int iEmergency = 5;
		int iMinEmergency = 10;

		// 患者から状態をメッセージで受診します
		cERNae = new ERNurseAgentException();

		iTraumaNum = erPAgent.iGetNumberOfTrauma();
		if( iTraumaNum < 0 )
		{
			cERNae.SetErrorInfo( ERNA_INVALID_DATA_ERROR, "ERDoctorAgent", "iJudgePainStatus", "不正な数値です。" );
			throw( cERNae );
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
		// AISからの緊急度判定（NRSをもとにしたもの）
		// 深在性、表在性、急性、慢性から緊急度が変化する。

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
		else if( 8 <= lfUpperExtremityNRS && lfUpperExtremityNRS <= 10 )	iEmergency = iJudgeSevereTrauma();
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

	private int iJudgeModerateTrauma()
	{
		int iEmergency = 5;
		double lfCurProb = 0.25;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

//		lfRand = 0.5 * ( normalRand() + 1.5 );
//		lfRand = 0.5 * ( normalRand() + 1.3 );
		lfRand = 0.5 * ( normalRand() + 1.0 );
//		lfRand = 0.7 * normalRand() + 0.3;
//		lfRand = weibullRand(1.0, 0.1);
//		lfRand = weibullRand(1.0, 0.5);
		lfRand = weibullRand(1.8, 0.35);
//		lfRand = weibullRand(2.5, 0.4);	//O.Kデータ
//		lfRand = weibullRand(4.0, 0.4);
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

	private int iJudgeSevereTrauma()
	{
		int iEmergency = 4;
		double lfCurProb = 0.25;
		double lfPrevProb = 0.0;
		double lfRand = 0.0;

//		lfRand = 0.5 * ( normalRand() + 1.0 );
		lfRand = 0.56 * normalRand() + 0.44;
//		lfRand = 0.5 * normalRand() + 0.5;
//		lfRand = 0.15 * normalRand() + 0.85;
//		lfRand = weibullRand(1.0, 0.5);
//		lfRand = rnd.NextUnif();
//		lfRand = 0.6 * normalRand() + 0.4;
		lfRand = weibullRand(2.0, 1.0);
		lfRand = weibullRand(2.5, 0.4);	//O.Kデータ
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
	 *    体温のCTAS判定を実施します。
	 *    全身性炎症症候群（SIRS）を判定してCTAS判定を実施します。
	 * </PRE>
	 * @param erPAgent 患者エージェント
	 * @return トリアージ緊急度
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
	 * @param erPAgent 対応する患者エージェント
	 * @return トリアージ緊急度
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
	 * @param erPAgent トリアージを受ける患者エージェント
	 * @return トリアージ緊急度
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
	 * @param erPAgent バイタル判定を受ける患者エージェント
	 * @return 緊急度
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
	 *   患者の酸素飽和度を判定します。
	 * </PRE>
	 * @return トリアージ緊急度
	 */
	private int iJudgeSpO2()
	{
		int iEmergency = 5;
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
		// 正常値のため、決定できないので他の判定条件により判定します。
		else if( 0.94 < lfPatientSpO2 )
		{
//			iEmergency = rnd.NextUnif() <= 0.5 ? 4 : 5;
			iEmergency = 5;
		}
		return iEmergency;
	}

	/**
	 * <PRE>
	 *   患者の意識レベルを判定します。
	 * </PRE>
	 * @param erPAgent 意識レベルの判定を受ける患者エージェント
	 * @return トリアージ緊急度
	 */
	public int iJudgeConsciousness( ERPatientAgent erPAgent )
	{
		int iEmergency = 5;

		// 意識障害中等症以上
		if( 3.0 <= erPAgent.lfGetGcs() && erPAgent.lfGetGcs() < 9.0 )
		{
			iEmergency = 1;
		}
		// 意識障害軽度
		else if( 9.0 <= erPAgent.lfGetGcs() && erPAgent.lfGetGcs() < 13.0 )
		{
			iEmergency = 2;
		}
		// 正常
		else if( 13.0 <= erPAgent.lfGetGcs() )
		{
//			iEmergency = (int)(3.0*rnd.NextUnif()+2.0);
			// CTAS,JTASではこの領域のスコアに入った場合は他の条件から緊急度を判定します。
			iEmergency = 5;
		}
		else
		{

		}
		return iEmergency;
	}

	/**
	 * <PRE>
	 *    看護師が観察した結果の患者に対する緊急度の結果を取得します。
	 * </PRE>
	 * @return トリアージ緊急度
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public int iGetEmergencyLevel()
	{
		//> テスト用
//		iEmergencyLevel = 3;
		//< テスト用
		return iEmergencyLevel;
	}

	/**
	 * <PRE>
	 *    定期観察時間を取得します。
	 * </PRE>
	 * @return 定期観察時間
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public double lfGetObservationTime()
	{
		return lfObservationTime;
	}

	/**
	 * <PRE>
	 *    定期トリアージ時間を取得します。
	 * </PRE>
	 * @return 定期トリアージ実施時間
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public double lfGetTriageProcessTime()
	{
		return lfTriageProcessTime;
	}

	/**
	 * <PRE>
	 *   患者対応中か否かのフラグを取得します。
	 * </PRE>
	 * @return true 対応中。
	 * 		   false 未対応中。
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public int iGetAttending()
	{
		return iAttending;
	}

	/**
	 * <PRE>
	 *   看護師が医師から受け取った診察時間を取得します。
	 * </PRE>
	 * @return 医師の診察時間
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public double lfGetConsultationTime()
	{
		return lfConsultationTime;
	}

	/**
	 * <PRE>
	 *   対応した患者の待合室での待ち時間を取得します。
	 * </PRE>
	 * @return 対応した患者の待ち時間
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public double lfGetWaitTime()
	{
		return lfWaitTime;
	}

	/**
	 * <PRE>
	 *   看護師が現在対応している患者の総経過時間を取得します。
	 * </PRE>
	 * @return 現在の総対応時間
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public double lfGetCurrentPassOverTime()
	{
		return lfCurrentPassOverTime;
	}

	/**
	 * <PRE>
	 *    看護師が今まで稼働した総経過時間を取得します。
	 * </PRE>
	 * @return 総観察時間
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public double lfGetTotalObservationTime()
	{
		return lfTotalObservationTime;
	}

	/**
	 * <PRE>
	 *   看護師が観察に要する時間を取得します。
	 * </PRE>
	 * @return 観察時間
	 */
	public double lfGetObservationProcessTime()
	{
		return lfObservationProcessTime;
	}

	/**
	 * <PRE>
	 *   看護師が現在対応中の患者エージェントデータを取得します。
	 * </PRE>
	 * @return 看護師が対応している患者エージェント
	 */
	public ERPatientAgent cGetERPatientAgent()
	{
		return erPatientAgent;
	}

/*----------------------------------設定関数----------------------------------------*/
	/**
	 * <PRE>
	 *   看護師が応対する患者エージェントを設定します。
	 * </PRE>
	 * @param erPAgent 看護師が対応している患者エージェント
	 */
	public void vSetERPatientAgent( ERPatientAgent erPAgent )
	{
		erPatientAgent = erPAgent;
	}

	/**
	 * <PRE>
	 *   患者対応中か否かのフラグを設定します。
	 * </PRE>
	 * @param iAttendingData 対応中フラグ
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetAttending( int iAttendingData )
	{
		iAttending = iAttendingData;
	}

	/**
	 * <PRE>
	 *   看護師の所属部門を設定します。
	 * </PRE>
	 * @param iDepartment 所属部門
	 */
	public void vSetNurseDepartment( int iDepartment )
	{
		iNurseDepartment = iDepartment;
	}

	/**
	 * <PRE>
	 *   看護師のトリアージプロトコルを設定します。
	 * </PRE>
	 * @param iProtocol 看護師のトリアージプロトコル
	 */
	public void vSetTriageProtocol( int iProtocol )
	{
		iTriageProtocol = iProtocol;
	}

	/**
	 * <PRE>
	 *   看護師のトリアージプロトコルレベルを設定します。
	 * </PRE>
	 * @param iLevel トリアージ緊急度基準
	 */
	public void vSetTriageProtocolLevel( int iLevel )
	{
		iTriageProtocolLevel = iLevel;
	}

	/**
	 * <PRE>
	 *   看護師のトリアージ経験年数を設定します。
	 * </PRE>
	 * @param alfNurseTriageYearExperience トリアージ経験年数
	 */
	public void vSetTriageYearExperience( double alfNurseTriageYearExperience )
	{
		iTriageYearExperience = alfNurseTriageYearExperience;
	}

	public void vSetYearExperience( double lfYear )
	{
		lfYearExperience = lfYear;
	}

	public void vSetConExperience( double lfCon )
	{
		lfConExperience = lfCon;
	}

	public void vSetConTired1( double lfCon )
	{
		lfConTired1 = lfCon;
	}

	public void vSetConTired2( double lfCon )
	{
		lfConTired2 = lfCon;
	}

	public void vSetConTired3( double lfCon )
	{
		lfConTired3 = lfCon;
	}

	public void vSetConTired4( double lfCon )
	{
		lfConTired4 = lfCon;
	}

	/**
	 * <PRE>
	 *    緊急度に従った再トリアージ時間を設定します。
	 * </PRE>
	 * @param lfTriageTime 再トリアージまでの時間
	 */
	public void vSetTriageProtocolTime(double lfTriageTime )
	{
		if( lfTriageTime != 0.0 )
		{
			lfTriageProcessTime = lfTriageTime;
		}
		else
		{
			// JTAS 及び CTASモデルに関しては以下の再トリアージ時間を適用します。
			if( iTriageProtocol == 1 || iTriageProtocol == 2 )
			{
				if( iEmergencyLevel == 0 )
				{
					lfTriageProcessTime = 120.0*60;
					cNurseAgentLog.info( "通常は通りません。" );
				}
				else if( iEmergencyLevel == 1 )
				{
					lfTriageProcessTime = 0.0;
				}
				else if( iEmergencyLevel == 2 )
				{
					lfTriageProcessTime = 15.0*60;
				}
				else if( iEmergencyLevel == 3 )
				{
					lfTriageProcessTime = 30.0*60;
				}
				else if( iEmergencyLevel == 4 )
				{
					lfTriageProcessTime = 60.0*60.0;
				}
				else if( iEmergencyLevel == 5 )
				{
					lfTriageProcessTime = 120.0*60;
				}
			}
			// ESIモデルに関しては記述がないため、仮にJTASモデルを参照します。
			else if( iTriageProtocol == 4 )
			{
				if( iEmergencyLevel == 0 )
				{
					lfTriageProcessTime = 120.0*60;
					cNurseAgentLog.info( "通常は通りません。" );
				}
				else if( iEmergencyLevel == 1 )
				{
					lfTriageProcessTime = 0.0;
				}
				else if( iEmergencyLevel == 2 )
				{
					lfTriageProcessTime = 15.0*60;
				}
				else if( iEmergencyLevel == 3 )
				{
					lfTriageProcessTime = 30.0*60;
				}
				else if( iEmergencyLevel == 4 )
				{
					lfTriageProcessTime = 60.0*60.0;
				}
				else if( iEmergencyLevel == 5 )
				{
					lfTriageProcessTime = 120.0*60;
				}
			}
		}
	}

	public void vSetObservationTime( double lfTime )
	{
		lfObservationTime = lfTime;
	}

	/**
	 * <PRE>
	 *   看護師が観察に要する時間を設定します。
	 * </PRE>
	 * @param lfData 観察時間
	 */
	public void vSetObservationProcessTime( double lfData )
	{
		lfObservationProcessTime = lfData;
	}


	public void vSetTiredRate( double lfTiredRateData )
	{
		// TODO 自動生成されたメソッド・スタブ
		lfTiredRate = lfTiredRateData;
	}


	public void vSetAssociationRate( double lfAssociationRateData )
	{
		// TODO 自動生成されたメソッド・スタブ
		lfAssociationRate = lfAssociationRateData;
	}

	/**
	 * <PRE>
	 *    救急部門に登録されている全エージェントのIDを設定します。
	 * </PRE>
	 * @param ArrayListNurseAgentIdsData			全看護師エージェントID
	 * @param ArrayListDoctorAgentIdsData			全医師エージェントID
	 * @param ArrayListClinicalEngineerAgentIdsData	全医療技師エージェントID
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
	 *    患者が移動するまでの待ちを表すフラグです。
	 * </PRE>
	 * @param iData 移動待ちフラグ 1：移動待ち中。 0:移動待ちはしていない。
	 */
	public void vSetPatientMoveWaitFlag(int iData )
	{
		iPatientMoveWaitFlag = iData;
	}

	/**
	 * <PRE>
	 *    トリアージナースか否かを取得します。
	 * </PRE>
	 * @return トリアージナースフラグ
	 *         0 通常の看護師
	 *         1 トリアージナース
	 */
	public int iGetNurseCategory()
	{
		return iNurseCategory;
	}

	/**
	 * <PRE>
	 *   トリアージナースか否かの設定をします。
	 * </PRE>
	 * @param iNurseCategoryData トリアージナースの設定。 0：通常の看護師、1：トリアージナース
	 */
	public void vSetNurseCategory(int iNurseCategoryData )
	{
		iNurseCategory = iNurseCategoryData;
	}


	/**
	 * <PRE>
	 *    経験値による重みづけ計算に使用するパラメータです。
	 * </PRE>
	 * @param lfData 経験年数重みづけパラメータ1
	 */
	public void vSetExperienceRate1( double lfData )
	{
		lfExperienceRate1 = lfData;
	}

	/**
	 * <PRE>
	 *    経験値による重みづけ計算に使用するパラメータです。
	 * </PRE>
	 * @param lfData 経験年数重みづけパラメータ2
	 */
	public void vSetExperienceRate2( double lfData )
	{
		lfExperienceRate2 = lfData;
	}

	/**
	 * <PRE>
	 *    経験値による重みづけ計算に使用するパラメータです。
	 * </PRE>
	 * @param lfData	重症度の経験値重み
	 */
	public void vSetConExperienceAIS( double lfData )
	{
		lfConExperienceAIS = lfData;
	}

	/**
	 * <PRE>
	 *    経験値(AIS重症度)による重みづけ計算に使用するパラメータです。
	 * </PRE>
	 * @param lfData	重症度パラメータ1
	 */
	public void vSetExperienceRateAIS1( double lfData )
	{
		lfExperienceRateAIS1 = lfData;
	}

	/**
	 * <PRE>
	 *    経験値(AIS重症度)による重みづけ計算に使用するパラメータです。
	 * </PRE>
	 * @param lfData	重症度パラメータ2
	 */
	public void vSetExperienceRateAIS2( double lfData )
	{
		lfExperienceRateAIS2 = lfData;
	}

	/**
	 * <PRE>
	 *    所属している部屋番号を設定します。
	 * </PRE>
	 * @param iNum	所属している部屋番号
	 */
	public void vSetRoomNumber( int iNum )
	{
		iRoomNumber = iNum;
	}

	/**
	 * <PRE>
	 *    経験年数を加算算出する式です。
	 *    経験年数が経過するにつれて1に近づきます。
	 * </PRE>
	 * @return 積算する重み。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	private double lfCalcExperience()
	{
		return 1.0-Math.exp(-lfYearExperience*lfConExperience);
	}

	/**
	 * <PRE>
	 *    疲労度を算出します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/10/29
	 * @param lfCurrentTime	現シミュレーション時刻
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

		if( iCalcFatigueFlag == 0 )
		{
			// 初回は初期の値を代入します。
			lfFatigue = lfMu*Math.exp( lfOmega*lfFrameWidth );
			iCalcFatigueFlag = 1;
		}
		else
		{
			if( iNurseDepartment == 2 )
			{
				// 手術室のワークロードを設定します。
				lfDeltaWorkLoad = 5;
			}
			else if( iNurseDepartment == 3 )
			{
				// 初療室のワークロードを設定します。
				lfDeltaWorkLoad = 10;
			}
			else if( iDoctorDepartment == 4 )
			{
				// 観察室のワークロードを設定します。
				lfDeltaWorkLoad = 10;
			}
			else if( iDoctorDepartment == 5 )
			{
				// 重症観察室のワークロードを設定します。
				lfDeltaWorkLoad = 10;
			}
			else if( iNurseDepartment == 6 )
			{
				// 集中治療室のワークロードを設定します。
				lfDeltaWorkLoad = 20;
			}
			else if( iDoctorDepartment == 7 )
			{
				// 高度治療室のワークロードを設定します。
				lfDeltaWorkLoad = 20;
			}
			else if( iDoctorDepartment == 8 )
			{
				// 一般病棟のワークロードを設定します。
				lfDeltaWorkLoad = 20;
			}
			else if( iDoctorDepartment == 9 )
			{
				// 待合室のワークロードを設定します。
				lfDeltaWorkLoad = 10;
			}
			lfDeltaR = lfFatigue*Math.exp( lfOmega*lfFrameWidth );
			lfDeltaU = lfAlpha*Math.exp( lfBeta*lfDeltaWorkLoad );
			lfFatigue = lfFatigue + lfDeltaU - lfDeltaR;
		}
		cNurseAgentLog.info(this.getId() + "," + "疲労度パラメータ：" + lfFatigue );
	}

	/**
	 * <PRE>
	 *    看護師エージェントの連携度を取得します。
	 * </PRE>
	 * @return 看護師の連携度
	 */
	public double lfGetAssociationRate()
	{
		return lfAssociationRate;
	}

	/**
	 * <PRE>
	 *   医師エージェントへ観察結果を送信します。
	 * </PRE>
	 * @param erPAgent				検査対象の患者エージェント
	 * @param erDoctorAgent			対象医師エージェント
	 * @param iFromAgentId  		送信先のエージェント（ここでは医師エージェント）
	 * @param iToAgentId			送信元のエージェント（ここでは看護師エージェント）
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public void vSendToDoctorAgentMessage( ERPatientAgent erPAgent, ERDoctorAgent erDoctorAgent, int iFromAgentId, int iToAgentId )
	{
		Message mesSend;
		mesSend = new Message();
		mesSend.setData( new MessageFromNurseToDoc() );
		mesSend.setFromAgent( iFromAgentId );
		mesSend.setToAgent( iToAgentId );
		((MessageFromNurseToDoc)mesSend.getData()).vSetERPatientAgent( erPAgent );
		((MessageFromNurseToDoc)mesSend.getData()).vSetExamAISHead( lfJudgedAISHead );
		((MessageFromNurseToDoc)mesSend.getData()).vSetExamAISFace( lfJudgedAISFace );
		((MessageFromNurseToDoc)mesSend.getData()).vSetExamAISNeck( lfJudgedAISNeck );
		((MessageFromNurseToDoc)mesSend.getData()).vSetExamAISThorax( lfJudgedAISThorax );
		((MessageFromNurseToDoc)mesSend.getData()).vSetExamAISAbdomen( lfJudgedAISAbdomen );
		((MessageFromNurseToDoc)mesSend.getData()).vSetExamAISSpine( lfJudgedAISSpine );
		((MessageFromNurseToDoc)mesSend.getData()).vSetExamAISUpperExtremity( lfJudgedAISUpperExtremity );
		((MessageFromNurseToDoc)mesSend.getData()).vSetExamAISLowerExtremity( lfJudgedAISLowerExtremity );
		((MessageFromNurseToDoc)mesSend.getData()).vSetExamAISUnspecified( lfJudgedAISUnspecified );
		((MessageFromNurseToDoc)mesSend.getData()).vSetObservationTime( lfCurrentPassOverTime );
		((MessageFromNurseToDoc)mesSend.getData()).vSetEmergencyLevel( iEmergencyLevel );
		((MessageFromNurseToDoc)mesSend.getData()).vSetNurseDepartment( iNurseDepartment );
//		this.sendMessage( mesSend );

		// 対象の医師エージェントが自分自身にメッセージを送信する。
		erDoctorAgent.vSendMessage( mesSend );
	}

	/**
	 * <PRE>
	 *   患者エージェントへ観察結果を送信します。
	 * </PRE>
	 * @param erPAgent				看護師が担当している患者エージェント
	 * @param iFromAgentId  		送信先のエージェント（ここでは患者エージェント）
	 * @param iToAgentId			送信元のエージェント（ここでは看護師エージェント）
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public void vSendToPatientAgentMessage( ERPatientAgent erPAgent, int iFromAgentId, int iToAgentId )
	{
		Message mesSend;
		mesSend = new Message();
		mesSend.setData( new MessageFromNurseToPat() );
		mesSend.setFromAgent( iFromAgentId );
		mesSend.setToAgent( iToAgentId );
		((MessageFromNurseToPat)mesSend.getData()).vSetExamAISHead( lfJudgedAISHead );
		((MessageFromNurseToPat)mesSend.getData()).vSetExamAISFace( lfJudgedAISFace );
		((MessageFromNurseToPat)mesSend.getData()).vSetExamAISNeck( lfJudgedAISNeck );
		((MessageFromNurseToPat)mesSend.getData()).vSetExamAISThorax( lfJudgedAISThorax );
		((MessageFromNurseToPat)mesSend.getData()).vSetExamAISAbdomen( lfJudgedAISAbdomen );
		((MessageFromNurseToPat)mesSend.getData()).vSetExamAISSpine( lfJudgedAISSpine );
		((MessageFromNurseToPat)mesSend.getData()).vSetExamAISUpperExtremity( lfJudgedAISUpperExtremity );
		((MessageFromNurseToPat)mesSend.getData()).vSetExamAISLowerExtremity( lfJudgedAISLowerExtremity );
		((MessageFromNurseToPat)mesSend.getData()).vSetExamAISUnspecified( lfJudgedAISUnspecified );
		((MessageFromNurseToPat)mesSend.getData()).vSetObservationTime( lfCurrentPassOverTime );
		((MessageFromNurseToPat)mesSend.getData()).vSetEmergencyLevel( iEmergencyLevel );
		((MessageFromNurseToPat)mesSend.getData()).vSetNruseDepartment( iNurseDepartment );

		// 対象の患者エージェントが自分自身にメッセージ送信する。
		erPAgent.vSendMessage( mesSend );
	}

	/**
	 * <PRE>
	 *   看護師エージェントへ観察結果を送信します。
	 * </PRE>
	 * @param erPAgent 				看護師が対応している患者エージェント
	 * @param iFromAgentId  		送信先のエージェント（ここでは患者エージェント）
	 * @param iToAgentId			送信元のエージェント（ここでは看護師エージェント）
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public void vSendToNurseAgentMessage( ERPatientAgent erPAgent, int iFromAgentId, int iToAgentId )
	{
		Message mesSend;
		mesSend = new Message();
		mesSend.setData( new MessageFromNurseToNurse() );
		mesSend.setFromAgent(iFromAgentId );
		mesSend.setToAgent( iToAgentId );
		((MessageFromNurseToNurse)mesSend.getData()).vSetExamAISHead( lfJudgedAISHead );
		((MessageFromNurseToNurse)mesSend.getData()).vSetExamAISFace( lfJudgedAISFace );
		((MessageFromNurseToNurse)mesSend.getData()).vSetExamAISNeck( lfJudgedAISNeck );
		((MessageFromNurseToNurse)mesSend.getData()).vSetExamAISThorax( lfJudgedAISThorax );
		((MessageFromNurseToNurse)mesSend.getData()).vSetExamAISAbdomen( lfJudgedAISAbdomen );
		((MessageFromNurseToNurse)mesSend.getData()).vSetExamAISSpine( lfJudgedAISSpine );
		((MessageFromNurseToNurse)mesSend.getData()).vSetExamAISUpperExtremity( lfJudgedAISUpperExtremity );
		((MessageFromNurseToNurse)mesSend.getData()).vSetExamAISLowerExtremity( lfJudgedAISLowerExtremity );
		((MessageFromNurseToNurse)mesSend.getData()).vSetExamAISUnspecified( lfJudgedAISUnspecified );
		((MessageFromNurseToNurse)mesSend.getData()).vSetObservationTime( lfCurrentPassOverTime );
		((MessageFromNurseToNurse)mesSend.getData()).vSetEmergencyLevel( iEmergencyLevel );
		((MessageFromNurseToNurse)mesSend.getData()).vSetNurseDepartment( iNurseDepartment );
		((MessageFromNurseToNurse)mesSend.getData()).vSetERPatientAgent( erPAgent );
		this.vSendMessage( mesSend );
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
		// TODO 自動生成されたメソッド・スタブ
		double lfSecond = timeStep / 1000.0;
		ERNurseAgentException ernae = new ERNurseAgentException();
		int i;
		int iDoctorMessFlag = 0;
		int iNurseMessFlag = 0;
		int iClinicalEngineerMessFlag = 0;
		int iSurvivalFlag = 0;
		try
		{
			Message mess;

			lfTimeStep = lfSecond;
			// 看護師、医師、医療技師からのメッセージを取得します。
			// 対応中でない場合のみメッセージを取得します。
			if( this.iAttending == 0 )
			{
				mess = messGetMessage();

				if( mess != null )
				{
					// 医師からのメッセージかどうかを判定します。
					if( mess.getData() instanceof MessageFromDocToNurse )
					{
						// 診察内容を取得します。
						// 部屋の想定は診察室、初療室、手術室、
						// 外傷の状況を取得します。
//						lfJudgedAISHead				= ((MessageFromDocToNurse)mess.getData()).lfGetAISHead();
//						lfJudgedAISFace				= ((MessageFromDocToNurse)mess.getData()).lfGetAISFace();
//						lfJudgedAISNeck				= ((MessageFromDocToNurse)mess.getData()).lfGetAISNeck();
//						lfJudgedAISThorax			= ((MessageFromDocToNurse)mess.getData()).lfGetAISThorax();
//						lfJudgedAISAbdomen			= ((MessageFromDocToNurse)mess.getData()).lfGetAISAbdomen();
//						lfJudgedAISSpine			= ((MessageFromDocToNurse)mess.getData()).lfGetAISSpine();
//						lfJudgedAISUpperExtremity	= ((MessageFromDocToNurse)mess.getData()).lfGetAISUpperExtremity();
//						lfJudgedAISLowerExtremity	= ((MessageFromDocToNurse)mess.getData()).lfGetAISLowerExtremity();
//						lfJudgedAISUnspecified		= ((MessageFromDocToNurse)mess.getData()).lfGetAISUnspecified();
						// 担当した医師エージェントを取得します。
						iDoctorDepartment			= ((MessageFromDocToNurse)mess.getData()).iGetDoctorDepartment();
						iDoctorId					= (int)mess.getFromAgentId();
						iEmergencyLevel				= ((MessageFromDocToNurse)mess.getData()).iGetEmergencyLevel();
						lfConsultationTime			= ((MessageFromDocToNurse)mess.getData()).lfGetConsultationTime();
						// これから対応する患者エージェントを取得します。
						erPatientAgent				= ((MessageFromDocToNurse)mess.getData()).cGetERPatientAgent();
						iDoctorMessFlag = 1;
					}
					// 看護師からのメッセージかどうかを判定します。
					if( mess.getData() instanceof MessageFromNurseToNurse )
					{
						// 観察内容を取得します。
						// 外傷の状況を取得します。
//						lfJudgedAISHead				= ((MessageFromNurseToNurse)mess.getData()).lfGetAISHead();
//						lfJudgedAISFace				= ((MessageFromNurseToNurse)mess.getData()).lfGetAISFace();
//						lfJudgedAISNeck				= ((MessageFromNurseToNurse)mess.getData()).lfGetAISNeck();
//						lfJudgedAISThorax			= ((MessageFromNurseToNurse)mess.getData()).lfGetAISThorax();
//						lfJudgedAISAbdomen			= ((MessageFromNurseToNurse)mess.getData()).lfGetAISAbdomen();
//						lfJudgedAISSpine			= ((MessageFromNurseToNurse)mess.getData()).lfGetAISSpine();
//						lfJudgedAISUpperExtremity	= ((MessageFromNurseToNurse)mess.getData()).lfGetAISUpperExtremity();
//						lfJudgedAISLowerExtremity	= ((MessageFromNurseToNurse)mess.getData()).lfGetAISLowerExtremity();
//						lfJudgedAISUnspecified		= ((MessageFromNurseToNurse)mess.getData()).lfGetAISUnspecified();
						// 担当した看護師エージェントを取得します。
						iFromNurseDepartment		= ((MessageFromNurseToNurse)mess.getData()).iGetNurseDepartment();
						iFromNurseId				= (int)mess.getFromAgentId();
						iEmergencyLevel				= ((MessageFromNurseToNurse)mess.getData()).iGetEmergencyLevel();
						lfFromNurseObservationTime	= ((MessageFromNurseToNurse)mess.getData()).lfGetObservationTime();
						// これから対応する患者エージェントを取得します。
						erPatientAgent				= ((MessageFromNurseToNurse)mess.getData()).cGetERPatientAgent();
						iNurseMessFlag = 1;
					}
					// 医療技師からのメッセージかどうかを判定します
					if( mess.getData() instanceof MessageFromEngToNurse )
					{
						// 観察内容を取得します。
						// 外傷の状況を取得します。
//						lfJudgedAISHead				= ((MessageFromEngToNurse)mess.getData()).lfGetAISHead();
//						lfJudgedAISFace				= ((MessageFromEngToNurse)mess.getData()).lfGetAISFace();
//						lfJudgedAISNeck				= ((MessageFromEngToNurse)mess.getData()).lfGetAISNeck();
//						lfJudgedAISThorax			= ((MessageFromEngToNurse)mess.getData()).lfGetAISThorax();
//						lfJudgedAISAbdomen			= ((MessageFromEngToNurse)mess.getData()).lfGetAISAbdomen();
//						lfJudgedAISSpine			= ((MessageFromEngToNurse)mess.getData()).lfGetAISSpine();
//						lfJudgedAISUpperExtremity	= ((MessageFromEngToNurse)mess.getData()).lfGetAISUpperExtremity();
//						lfJudgedAISLowerExtremity	= ((MessageFromEngToNurse)mess.getData()).lfGetAISLowerExtremity();
//						lfJudgedAISUnspecified		= ((MessageFromEngToNurse)mess.getData()).lfGetAISUnspecified();
						// 担当した医療技師エージェントを取得します。
						iClinicalEngineerDepartment	= ((MessageFromEngToNurse)mess.getData()).iGetNurseDepartment();
						iClinicalEngineerId			= (int)mess.getFromAgentId();
//						iEmergencyLevel				= ((MessageFromEngToNurse)mess.getData()).iGetEmergencyLevel();
						lfExaminationTime			= ((MessageFromEngToNurse)mess.getData()).lfGetExaminationTime();
						// これから対応する患者エージェントを取得します。
						erPatientAgent				= ((MessageFromEngToNurse)mess.getData()).cGetERPatientAgent();
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
							strInjuryHeadStatus				= ((MessageFromPatToNurse)mess.getData()).strGetInjuryHeadStatus();
							strInjuryFaceStatus				= ((MessageFromPatToNurse)mess.getData()).strGetInjuryFaceStatus();
							strInjuryNeckStatus				= ((MessageFromPatToNurse)mess.getData()).strGetInjuryNeckStatus();
							strInjuryThoraxStatus			= ((MessageFromPatToNurse)mess.getData()).strGetInjuryThoraxStatus();
							strInjuryAbdomenStatus			= ((MessageFromPatToNurse)mess.getData()).strGetInjuryAbdomenStatus();
							strInjurySpineStatus			= ((MessageFromPatToNurse)mess.getData()).strGetInjurySpineStatus();
							strInjuryUpperExtremityStatus	= ((MessageFromPatToNurse)mess.getData()).strGetInjuryUpperExtremityStatus();
							strInjuryLowerExtremityStatus	= ((MessageFromPatToNurse)mess.getData()).strGetInjuryLowerExtremityStatus();
							strInjuryUnspecifiedStatus		= ((MessageFromPatToNurse)mess.getData()).strGetInjuryUnspecifiedStatus();
							// 担当した患者エージェントを取得します。
							iPatientLocation 			= ((MessageFromPatToNurse)mess.getData()).iGetPatientLocation();
							iPatientId					= (int)mess.getFromAgentId();
							lfWaitTime					= ((MessageFromPatToNurse)mess.getData()).lfGetWaitTime();
							iSurvivalFlag					= ((MessageFromPatToDoc)mess.getData()).iGetSurvivalFlag();
							// 患者が生存していない場合は医師の各種フラグを初期化する。
							if( iSurvivalFlag == 1 )
							{
								iAttending = 0;
								iPatientMoveWaitFlag = 0;
								lfCurrentPassOverTime = 0;
								lfCurrentObservationTime = 0;
								lfCurrentTriageTime = 0;
							}
						}
					}
				}
			}
			// 看護師が対応中の場合、観察経過時間を計算します。
			if( iAttending == 1 )
			{
				if( iNurseCategory == 0 )
				{
					if( iPatientMoveWaitFlag == 0 )
					{
						// 看護師のカテゴリ
						lfCurrentPassOverTime += lfSecond;
						lfTotalObservationTime += lfSecond;
						lfCurrentObservationTime = lfCurrentPassOverTime;
					}
				}
				else if( iNurseCategory == 1 )
				{
					if( iPatientMoveWaitFlag == 0 )
					{
						// 看護師のカテゴリ
						lfCurrentPassOverTime += lfSecond;
						lfTotalObservationTime += lfSecond;
						lfCurrentObservationTime = lfCurrentPassOverTime;
						lfCurrentTriageTime += lfSecond;
						lfTotalTriageTime += lfSecond;
					}
				}
			}
			else
			{
				lfCurrentPassOverTime = 0.0;
				lfCurrentObservationTime = 0.0;
				lfCurrentTriageTime = 0.0;
			}
			if( erPatientAgent != null )
			{
				// なくなったので患者エージェントを削除します。
				if( erPatientAgent.iGetSurvivalFlag() == 0 )
				{
					erPatientAgent = null;
					iAttending = 0;
					iPatientMoveWaitFlag = 0;
					lfCurrentPassOverTime = 0;
					lfCurrentObservationTime = 0;
					lfCurrentTriageTime = 0;
				}
			}
			if( iInverseSimMode == 0 )
			{
				// 終了100秒前からファイルに書き始めます。（長時間処理のため）
//				if( lfTotalTime >= lfSimulationEndTime-100.0 )
//				{
//					vWriteFile( 0 );
//				}
//				else if( lfTimeCourse <= 100.0 )
//				{
//					vWriteFile( 1 );
//				}
				vWriteFile( iFileWriteMode, lfTotalTime );
			}
			lfTimeCourse += lfSecond;
			lfTotalTime += lfSecond;
		}
		catch( ArrayIndexOutOfBoundsException aiobe )
		{
			cNurseAgentLog.warning(aiobe.getMessage());
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			ernae.SetErrorInfo( ERNA_FATAL_ERROR, "action", "ERNurseAgent", "配列のサイズを超えて参照しました", ste[0].getLineNumber() );
			// エラー詳細を出力
			String strMethodName = ernae.strGetMethodName();
			String strClassName = ernae.strGetClassName();
			String strErrDetail = ernae.strGetErrDetail();
			int iErrCode = ernae.iGetErrCode();
			int iErrLine = ernae.iGetErrorLine();
			cNurseAgentLog.warning( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
		}
		catch( NullPointerException npe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			ernae.SetErrorInfo( ERNA_NULLPOINT_ERROR, "action", "ERNurseAgent", "NULLポイントアクセスエラー", ste[0].getLineNumber() );
			// エラー詳細を出力
			String strMethodName = ernae.strGetMethodName();
			String strClassName = ernae.strGetClassName();
			String strErrDetail = ernae.strGetErrDetail();
			int iErrCode = ernae.iGetErrCode();
			int iErrLine = ernae.iGetErrorLine();
			cNurseAgentLog.warning( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
		}
		catch( RuntimeException re )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			ernae.SetErrorInfo( ERNA_FATAL_ERROR, "action", "ERNurseAgent", "不明および致命的エラー", ste[0].getLineNumber() );
			// エラー詳細を出力
			String strMethodName = ernae.strGetMethodName();
			String strClassName = ernae.strGetClassName();
			String strErrDetail = ernae.strGetErrDetail();
			int iErrCode = ernae.iGetErrCode();
			int iErrLine = ernae.iGetErrorLine();
			cNurseAgentLog.warning( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
			cNurseAgentLog.warning( re.getLocalizedMessage() + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
			for( i = 0;i < ste.length; i++ )
				cNurseAgentLog.warning( ste[i].getClassName()+"," );
		}
		catch( IOException ioe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			ernae.SetErrorInfo( ERNA_FATAL_ERROR, "action", "ERNurseAgent", "不明および致命的エラー", ste[0].getLineNumber() );
			// エラー詳細を出力
			String strMethodName = ernae.strGetMethodName();
			String strClassName = ernae.strGetClassName();
			String strErrDetail = ernae.strGetErrDetail();
			int iErrCode = ernae.iGetErrCode();
			int iErrLine = ernae.iGetErrorLine();
			cNurseAgentLog.warning( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
			cNurseAgentLog.warning( ioe.getLocalizedMessage() + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
		}
	}

	/**
	 * <PRE>
	 *    看護師エージェントのログ出力を設定します。
	 * </PRE>
	 * @param log ログインスタンス
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public void vSetLog(Logger log)
	{
		// TODO 自動生成されたメソッド・スタブ
		cNurseAgentLog = log;
	}

	/**
	 * <PRE>
	 *    シミュレーション終了時間の設定をします。
	 * </PRE>
	 * @param lfEndTime シミュレーション終了時間[秒]
	 */
	public void vSetSimulationEndTime( double lfEndTime )
	{
		lfSimulationEndTime = lfEndTime;
	}

	/**
	 * <PRE>
	 *    トリアージプロトコルの設定をします。
	 * </PRE>
	 * @param cCurNode トリアージプロトコル
	 */
	public void vSetTriageNode( ERTriageNode cCurNode )
	{
		erTriageNode = cCurNode;
	}

	/**
	 * <PRE>
	 *   トリアージプロトコルを取得します。
	 * </PRE>
	 * @return トリアージプロトコル
	 */
	public ERTriageNode erGetTriageNode()
	{
		return erTriageNode;
	}

	/**
	 * <PRE>
	 *    逆シミュレーションモードを設定します。
	 * </PRE>
	 * @param iMode 逆シミュレーションモード
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public void vSetInverseSimMode( int iMode )
	{
		iInverseSimMode = iMode;
	}

	/**
	 * <PRE>
	 *    観察プロセス起動時間を判定します。
	 * </PRE>
	 * @param lfObservationProcessTime	定期観察時間
	 * @return 0 起動しない。
	 *         1 起動する。
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public int isJudgeObservationProcessTime(double lfObservationProcessTime )
	{
		// TODO 自動生成されたメソッド・スタブ
		int iRet = 0;

		lfJudgeObservationProcessCount += lfTimeStep;
		if( lfJudgeObservationProcessCount >= lfObservationProcessTime )
		{
			lfJudgeObservationProcessCount = 0;
			iRet = 1;
		}
		return iRet;
	}

	/**
	 * <PRE>
	 *    トリアージプロセス起動時間を判定します。
	 * </PRE>
	 * @param lfTriageProcessTime		トリアージ判定時間
	 * @return 0 起動しない。
	 *         1 起動する。
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public int isJudgeTriageProcessTime(double lfTriageProcessTime )
	{
		// TODO 自動生成されたメソッド・スタブ
		int iRet = 0;

		lfJudgeTriageProcessCount += lfTimeStep;
		if( lfJudgeTriageProcessCount >= lfTriageProcessTime )
		{
			lfJudgeTriageProcessCount = 0;
			iRet = 1;
		}
		return iRet;
	}

	public void vSetCriticalSection(Object cs )
	{
		// TODO 自動生成されたメソッド・スタブ
		erNurseCriticalSection = cs;
	}


	/**
	 * <PRE>
	 *    正規乱数を発生させます。-1.0以下、1.0以上が乱数を発生させた結果出力された場合、
	 *    再度乱数を発生させます。乱数発生回数の繰り返し回数は100回とします。
	 * </PRE>
	 * @return 正規分布確率
	 * @author kobayashi
	 * @since 2015/07/23
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
	 * @param lfAlpha ワイブル分布用パラメータ2
	 * @param lfBeta  ワイブル分布用パラメータ1
	 * @return ワイブル分布確率
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
	 *   ファイルの書き込みを行います。
	 * </PRE>
	 * @param iFlag ファイル書き込みモード
	 * @param lfTime 書き込む時間
	 * @throws IOException 例外
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public void vWriteFile( int iFlag, double lfTime ) throws IOException
	{
		String strData = lfTotalTime + "," + lfTimeCourse + "," + lfCurrentPassOverTime + "," + lfCurrentObservationTime + "," + lfTotalObservationTime + "," + lfCurrentTriageTime + "," + lfTotalTriageTime + "," + iTotalObservationNum +"," + iTotalTriageNum + "," + iNurseCategory + "," + iNurseDepartment + ",";
		strData += lfJudgedAISHead + "," + lfJudgedAISFace + "," + lfJudgedAISNeck + "," + lfJudgedAISThorax + "," + lfJudgedAISAbdomen + "," + lfJudgedAISSpine  + "," + lfJudgedAISLowerExtremity + "," + lfJudgedAISUpperExtremity + "," + lfJudgedAISUnspecified;
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
	 *    メルセンヌツイスターオブジェクトを設定します。
	 * </PRE>
	 * @param sfmtRandom メルセンヌツイスタのインスタンス
	 * @author kobayashi
	 * @since 2015/07/23
	 */
	public void vSetRandom(utility.sfmt.Rand sfmtRandom )
	{
		// TODO 自動生成されたメソッド・スタブ
		rnd = sfmtRandom;
	}
}
