package triage.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import utility.csv.CCsv;
import utility.initparam.InitSimParam;
import utility.node.ERTriageNode;
import utility.sfmt.Rand;
import jp.ac.nihon_u.cit.su.furulab.fuse.Message;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;


public class ERClinicalEngineerAgent extends Agent
{

	private static final long serialVersionUID = -9052883723627748868L;

	public static final int ERCEA_SUCCESS					= 0;
	public static final int ERCEA_FATAL_ERROR				= -301;
	public static final int ERCEA_MEMORYALLOCATE_ERROR		= -302;
	public static final int ERCEA_NULLPOINT_ERROR			= -303;
	public static final int ERCEA_INVALID_ARGUMENT_ERROR	= -304;
	public static final int ERCEA_INVALID_DATA_ERROR		= -305;
	public static final int ERCEA_ARRAY_INDEX_ERROR			= -306;
	public static final int ERCEA_ZERO_DIVIDE_ERROR			= -307;

	int iClinicalEngineerId;			// 医療技師のID
	int iClinicalEngineerDepartment;	// 医療技師の担当部署
	int iRoomNumber;					// 部屋番号
	double lfYearExperience;			// 経験年数
	double lfConExperience;				// 経験年数重みパラメータ
	double lfExperienceRate1;			// 経験年数パラメータその１
	double lfExperienceRate2;			// 経験年数パラメータその２
	double lfConExperienceAIS;			// 経験年数重みパラメータ(重症度用)
	double lfExperienceRateAIS1;		// 経験年数パラメータその１(重症度用)
	double lfExperienceRateAIS2;		// 経験年数パラメータその２(重症度用)
	double lfTiredRate;					// 疲労度
	double lfConTired1;					// 疲労度重みパラメータ1
	double lfConTired2;					// 疲労度重みパラメータ2
	double lfConTired3;					// 疲労度重みパラメータ3
	double lfConTired4;					// 疲労度重みパラメータ4
	double lfAssociationRate;			// 連携度
	double lfContributionAssociationRate;	// 連携度の寄与度

	int iKindExamination;				// 検査の種類
	double lfExamAISHead;				// 頭部のAIS
	double lfExamAISFace;				// 顔面のAIS
	double lfExamAISNeck;				// 頸部（首）のAIS
	double lfExamAISThorax;				// 胸部のAIS
	double lfExamAISAbdomen;			// 腹部のAIS
	double lfExamAISSpine;				// 脊椎のAIS
	double lfExamAISUpperExtremity;		// 上肢のAIS
	double lfExamAISLowerExtremity;		// 下肢のAIS
	double lfExamAISUnspecified;		// 特定部位でない。（体表・熱傷・その他外傷）

	double lfExaminationTime;			// 検査時間
	double lfTotalExaminationTime;		// 総検査時間
	double lfCurrentPassOverTime;		// 検査開始からの経過時間
	double lfTotalTime;					// 総稼働時間

	int iDoctorId;						// メッセージを送信した医師ID
	int iDoctorDepartment;				// メッセージを送信した医師の所属部門

	int iRequestExamination;			// 医師が依頼する検査内容
	int aiRequestAnatomys[];			// 医師が依頼する検査部位
	int iRequestExaminationNum;			// 医師が依頼する検査部位数

	int iEmergencyLevel;				// 緊急度

	ERPatientAgent erPAgent;			// 患者エージェント。

	int iAttending;						// 検査対応中か否か

	utility.sfmt.Rand rnd;

	ArrayList<Long> ArrayListDoctorAgentIds;			// 全医師のID
	ArrayList<Long> ArrayListNurseAgentIds;				// 全看護師のID
	ArrayList<Long> ArrayListClinicalEngineerAgentIds;	// 全看護師のID

	Queue<Message> mesQueueData;						// メッセージキューを保持する変数

	CCsv csvWriteAgentData;								// 出力用データ
	CCsv csvWriteAgentStartData;						// 出力用データ
	int iPatientMoveWaitFlag;							// 患者移動フラグ

	private Logger cClinicalEngineerLog;

	private double lfSimulationEndTime;					// シミュレーション終了時間

	private int iInverseSimMode;						// 逆シミュレーションモード

	private Object erClinicalEngineerCriticalSection;	// クリティカルセクション用

	private Rand sfmtClinicalEngineer;					// メルセンヌツイスターオブジェクト

	private int iFileWriteMode;							// 長時間シミュレーション用ファイル出力モード

	private InitSimParam initParamClinicalEngineerAgent;// 初期設定ファイル操作用変数
	
	private ERTriageNode erTriageNode;					// 医療技師がいるノード

	/**
	 * <PRE>
	 *   コンストラクタ
	 * </PRE>
	 */
	public ERClinicalEngineerAgent()
	{
		vInitialize();
	}

	/**
	 * <PRE>
	 *   コンストラクタ
	 * </PRE>
	 * @param lfExper   経験年数
	 * @param lfTired   疲労度
	 * @param iKindExam 検査の種類
	 * 　　　　　　　　 1 レントゲン室（胸部撮影）
	 * 　　　　　　　　 2 レントゲン室（腹部撮影）
	 * 　　　　　　　　 3 CT検査
	 * 　　　　　　　　 4 MRI検査
	 * 　　　　　　　　 5 血管造影検査
	 * @author kobayashi
	 * @since 2015/07/31
	 */
	public ERClinicalEngineerAgent( double lfExper, double lfTired, int iKindExam )
	{
		vInitlaize( lfExper, lfTired, iKindExam );
	}

	/**
	 * <PRE>
	 *   初期化を実行します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/07/31
	 */
	public void vInitialize()
	{
		String strFileName = "";
//		long seed;
//		seed = System.currentTimeMillis();
//		rnd = null;
//		rnd = new Sfmt( (int)seed );
		lfYearExperience					= 5.0;
		lfConExperience						= 0.61;
		lfExperienceRate1					= 2.1;
		lfExperienceRate2					= 0.9;
		lfConExperienceAIS					= 0.14;		// 経験年数重みパラメータ(重症度用)
		lfExperienceRateAIS1				= 0.2;		// 経験年数パラメータその１(重症度用)
		lfExperienceRateAIS2				= 1.1;		// 経験年数パラメータその２(重症度用)
		lfConTired1							= 0.0;
		lfConTired2							= 0.0;
		lfConTired3							= 0.0;
		lfConTired4							= 0.0;
		lfTiredRate							= 0.0;
		lfAssociationRate					= 1.0;
		lfContributionAssociationRate		= 1.0;
		iKindExamination					= 0;
		lfTotalExaminationTime				= 0.0;
		lfExamAISHead						= 0.0;
		lfExamAISFace						= 0.0;
		lfExamAISNeck						= 0.0;
		lfExamAISThorax						= 0.0;
		lfExamAISAbdomen					= 0.0;
		lfExamAISSpine						= 0.0;
		lfExamAISUpperExtremity 			= 0.0;
		lfExamAISLowerExtremity 			= 0.0;
		lfExamAISUnspecified				= 0.0;
		lfExaminationTime					= 0.0;
		ArrayListDoctorAgentIds 			= new ArrayList<Long>();			// 全医師のID
		ArrayListNurseAgentIds 				= new ArrayList<Long>();			// 全看護師のID
		ArrayListClinicalEngineerAgentIds	= new ArrayList<Long>();			// 全医療技師のID
		mesQueueData						= new LinkedList<Message>();
		iFileWriteMode						= 0;
//		try
//		{
//			csvWriteAgentData					= new CCsv();
//			strFileName							= "./er/ce/erce_start" + this.getId() + ".csv";
//			csvWriteAgentData.vOpen( strFileName, "write");
//			csvWriteAgentStartData					= new CCsv();
//			strFileName							= "./er/ce/erce_end" + this.getId() + ".csv";
//			csvWriteAgentStartData.vOpen( strFileName, "write");
//		}
//		catch( IOException ioe )
//		{
//			int i;
//			StackTraceElement ste[] = (new Throwable()).getStackTrace();
//			// エラー詳細を出力
//			for( i = 0;i < ioe.getStackTrace().length; i++ )
//			{
//				String str = "クラス名" + "," + ioe.getStackTrace()[i].getClassName();
//				str += "メソッド名" + "," + ioe.getStackTrace()[i].getMethodName();
//				str += "ファイル名" + "," + ioe.getStackTrace()[i].getFileName();
//				str += "行数" + "," + ioe.getStackTrace()[i].getLineNumber();
//				cClinicalEngineerLog.warning( str );
//			}
//		}
		iPatientMoveWaitFlag = 0;
	}

	/**
	 * <PRE>
	 *    ファイルの読み込みを行います。
	 * </PRE>
	 * @param iFileWriteMode	ファイル書き込みモード(0:全書き込み、1:最初と最後書き込み)
	 * @throws IOException ファイル読み込みエラー
	 */
	public void vSetReadWriteFile( int iFileWriteMode ) throws IOException
	{
		String strFileName = "";
		this.iFileWriteMode = iFileWriteMode;
		if( iFileWriteMode == 1 )
		{
			csvWriteAgentData					= new CCsv();
			strFileName							= "./er/ce/erce_start" + this.getId() + ".csv";
			csvWriteAgentData.vOpen( strFileName, "write");
			csvWriteAgentStartData					= new CCsv();
			strFileName							= "./er/ce/erce_end" + this.getId() + ".csv";
			csvWriteAgentStartData.vOpen( strFileName, "write");
		}
		else
		{
			csvWriteAgentData					= new CCsv();
			strFileName							= "./er/ce/erce_end" + this.getId() + ".csv";
			csvWriteAgentData.vOpen( strFileName, "write");
		}
	}

	/**
	 * <PRE>
	 *    終了処理を実行します。
	 * </PRE>
	 * @throws IOException java標準例外
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
	 *   初期化を実行します。
	 * </PRE>
	 * @param lfExper   経験年数
	 * @param lfTired   疲労度
	 * @param iKindExam 検査の種類
	 * 　　　　　　　　 1 レントゲン室（胸部撮影）
	 * 　　　　　　　　 2 レントゲン室（腹部撮影）
	 * 　　　　　　　　 3 CT検査
	 * 　　　　　　　　 4 MRI検査
	 * 　　　　　　　　 5 血管造影検査
	 * @author kobayashi
	 * @since 2015/07/31
	 */
	public void vInitlaize( double lfExper, double lfTired, int iKindExam )
	{
		String strFileName = "";
//		long seed;
//		seed = (long)(Math.random()*Long.MAX_VALUE);
//		rnd = new Sfmt( (int)seed );
		lfYearExperience					= lfExper;
		lfConExperience						= 0.61;
		lfExperienceRate1					= 2.1;
		lfExperienceRate2					= 0.9;
		lfConExperienceAIS					= 0.14;		// 経験年数重みパラメータ(重症度用)
		lfExperienceRateAIS1				= 0.2;		// 経験年数パラメータその１(重症度用)
		lfExperienceRateAIS2				= 1.1;		// 経験年数パラメータその２(重症度用)
		lfConTired1							= 0.0;
		lfConTired2							= 0.0;
		lfConTired3							= 0.0;
		lfConTired4							= 0.0;
		lfTiredRate							= lfTired;
		lfAssociationRate					= 1.0;
		lfContributionAssociationRate		= 1.0;
		iKindExamination 					= iKindExam;
		lfExamAISHead 						= 0.0;
		lfExamAISHead 						= 0.0;
		lfExamAISFace 						= 0.0;
		lfExamAISNeck 						= 0.0;
		lfExamAISThorax 					= 0.0;
		lfExamAISAbdomen					= 0.0;
		lfExamAISSpine 						= 0.0;
		lfExamAISUpperExtremity 			= 0.0;
		lfExamAISLowerExtremity 			= 0.0;
		lfExamAISUnspecified 				= 0.0;
		lfExaminationTime 					= 0.0;
		ArrayListDoctorAgentIds 			= new ArrayList<Long>();		// 全医師のID
		ArrayListNurseAgentIds 				= new ArrayList<Long>();		// 全看護師のID
		ArrayListClinicalEngineerAgentIds	= new ArrayList<Long>();		// 全医療技師のID
		mesQueueData						= new LinkedList<Message>();

		try
		{
			csvWriteAgentData					= new CCsv();
			strFileName							= "erce" + this.getId() + ".csv";
			csvWriteAgentData.vOpen( strFileName, "write");
		}
		catch( IOException ioe )
		{
			int i;
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			// エラー詳細を出力
			for( i = 0;i < ioe.getStackTrace().length; i++ )
			{
				String str = "クラス名" + "," + ioe.getStackTrace()[i].getClassName();
				str += "メソッド名" + "," + ioe.getStackTrace()[i].getMethodName();
				str += "ファイル名" + "," + ioe.getStackTrace()[i].getFileName();
				str += "行数" + "," + ioe.getStackTrace()[i].getLineNumber();
				cClinicalEngineerLog.warning( str );
			}
		}
		iPatientMoveWaitFlag = 0;
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
	 *   検査を実行します。
	 * </PRE>
	 * @param erPAgent				受検する患者エージェントインスタンス
	 * @param iImplementExamination 検査する医療機器
	 * @throws ERClinicalEngineerAgentException 技士エージェント例外
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public void vImplementExamination( ERPatientAgent erPAgent, int iImplementExamination ) throws ERClinicalEngineerAgentException
	{
		ERClinicalEngineerAgentException cERCleae = new ERClinicalEngineerAgentException();

		if( erPAgent == null )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cERCleae.SetErrorInfo( ERCEA_NULLPOINT_ERROR, "ERClinicalEngineerAgent", "lfImplementExamination", "null pointアクセス", ste[0].getLineNumber() );
			throw( cERCleae );
		}
		// レントゲン室（胸部撮影）の場合
		if( iImplementExamination == 1 )
		{
			vXRayRoom( erPAgent, 1 );
		}
		// レントゲン室（骨、軟部撮影）の場合
		else if( iImplementExamination == 2 )
		{
			vXRayRoom( erPAgent, 2 );
		}
		// CT室の場合
		else if( iImplementExamination == 3 )
		{
			vComputerizedTomography( erPAgent );
		}
		// MRIの場合
		else if( iImplementExamination == 4 )
		{
			vMagneticResonaceImaging( erPAgent );
		}
		// 血管造影検査の場合
		else if( iImplementExamination == 5 )
		{
			vAngiography( erPAgent );
		}
		// 腹部超音波検査の場合
		else if( iImplementExamination == 6 )
		{
			vFast( erPAgent );
		}
		else
		{
			// 行数を取得する。
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cERCleae.SetErrorInfo( ERCEA_INVALID_DATA_ERROR, "ERClinicalEngineerAgent", "lfImplementExamination", "null pointアクセス", ste[0].getLineNumber() );
			throw( cERCleae );
		}
	}

	/**
	 * <PRE>
	 *   レントゲン室の検査を実施します。
	 * </PRE>
	 * @param iXRayExamMode レントゲン検査の種類
	 * @param erPAgent 患者エージェント
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	private void vXRayRoom( ERPatientAgent erPAgent, int iXRayExamMode )
	{
		// レントゲン検査を実施します。（肺、心臓、胸部大動脈）
		if( iXRayExamMode == 1 )
		{
			// レントゲン検査を実施します。（肺、心臓、胸部大動脈）

			// 患者エージェントから胸部AISを取得します。
			lfExamAISThorax = erPAgent.lfGetInternalAISThorax()*lfCalcExperienceAIS();

			//患者エージェントから腹部AISを取得します。
			lfExamAISAbdomen = erPAgent.lfGetInternalAISAbdomen()*lfCalcExperienceAIS();
		}
		// レントゲン室（骨、軟部撮影）の場合
		else if( iXRayExamMode == 2 )
		{
			// レントゲン検査を実施します。（骨、関節の状態、筋肉、脂肪組織の状態の検査）

			// 患者エージェントから脊柱AISを取得します。
			lfExamAISSpine  = erPAgent.lfGetInternalAISSpine()*lfCalcExperienceAIS();

			// 患者エージェントから上肢AISを取得します。
			lfExamAISUpperExtremity = erPAgent.lfGetInternalAISUpperExtremity()*lfCalcExperienceAIS();

			// 患者エージェントから下肢AISを取得します。
			lfExamAISLowerExtremity = erPAgent.lfGetInternalAISLowerExtremity()*lfCalcExperienceAIS();

		}
	}

	/**
	 * <PRE>
	 *    CT室にてCT検査を実施します。
	 * </PRE>
	 * @param erPAgent 患者エージェント
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	private void vComputerizedTomography( ERPatientAgent erPAgent )
	{
		// CT検査を実施します。(脳、頸部、顔面、腹部、胸部含め全身)
//		if( DAgent.iGetExamRequest() == 1 )

		// 患者エージェントから頭部AISを取得します。
		lfExamAISHead = erPAgent.lfGetInternalAISHead()*lfCalcExperienceAIS();

		// 患者エージェントから顔面AISを取得します。
		lfExamAISFace = erPAgent.lfGetInternalAISFace()*lfCalcExperienceAIS();

		// 患者エージェントから頸部AISを取得します。
		lfExamAISNeck = erPAgent.lfGetInternalAISNeck()*lfCalcExperienceAIS();

		// 患者エージェントから胸部AISを取得します。
		lfExamAISThorax = erPAgent.lfGetInternalAISThorax()*lfCalcExperienceAIS();

		// 患者エージェントから腹部AISを取得します。
		lfExamAISAbdomen = erPAgent.lfGetInternalAISAbdomen()*lfCalcExperienceAIS();

		// 患者エージェントから脊柱AISを取得します。
		lfExamAISSpine  = erPAgent.lfGetInternalAISSpine()*lfCalcExperienceAIS();

		// 患者エージェントから上肢AISを取得します。
		lfExamAISUpperExtremity = erPAgent.lfGetInternalAISUpperExtremity()*lfCalcExperienceAIS();

		// 患者エージェントから下肢AISを取得します。
		lfExamAISLowerExtremity = erPAgent.lfGetInternalAISLowerExtremity()*lfCalcExperienceAIS();
	}

	/**
	 * <PRE>
	 *    MRI室にてMRI検査を実施します。
	 * </PRE>
	 * @param erPAgent 患者エージェント
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	private void vMagneticResonaceImaging( ERPatientAgent erPAgent )
	{
		// MRI検査を実施します。(脳、脊椎、四肢)

		// 患者エージェントから頭部AISを取得します。
		lfExamAISHead = erPAgent.lfGetInternalAISHead()*lfCalcExperienceAIS();

		// 患者エージェントから脊柱AISを取得します。
		lfExamAISSpine  = erPAgent.lfGetInternalAISSpine()*lfCalcExperienceAIS();

		// 患者エージェントから上肢AISを取得します。
		lfExamAISUpperExtremity = erPAgent.lfGetInternalAISUpperExtremity()*lfCalcExperienceAIS();

		// 患者エージェントから下肢AISを取得します。
		lfExamAISLowerExtremity = erPAgent.lfGetInternalAISLowerExtremity()*lfCalcExperienceAIS();
	}

	/**
	 * <PRE>
	 *    血管造影室にて血管造影検査を実施します。
	 * </PRE>
	 * @param erPAgent 患者エージェント
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	private void vAngiography( ERPatientAgent erPAgent )
	{
		// 血管造影検査を実施します。（特に脳、胸部、腹部）

		// 患者エージェントから頭部AISを取得します。
		lfExamAISHead = erPAgent.lfGetInternalAISHead()*lfCalcExperienceAIS();

		// 患者エージェントから頸部AISを取得します。
		lfExamAISNeck = erPAgent.lfGetInternalAISNeck()*lfCalcExperienceAIS();

		// 患者エージェントから胸部AISを取得します。
		lfExamAISThorax = erPAgent.lfGetInternalAISThorax()*lfCalcExperienceAIS();

		// 患者エージェントから腹部AISを取得します。
		lfExamAISAbdomen = erPAgent.lfGetInternalAISAbdomen()*lfCalcExperienceAIS();
	}

	/**
	 * <PRE>
	 *    FAST室にてFAST検査を実施します。
	 * </PRE>
	 * @param erPAgent 患者エージェント
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	private void vFast( ERPatientAgent erPAgent )
	{
		// 腹部超音波検査を実施します。（特に胸部、腹部）Focused Assessment with Sonography for Trauma

		// 患者エージェントから胸部AISを取得します。
		lfExamAISThorax = erPAgent.lfGetInternalAISThorax()*lfCalcExperienceAIS();

		// 患者エージェントから腹部AISを取得します。
		lfExamAISAbdomen = erPAgent.lfGetInternalAISAbdomen()*lfCalcExperienceAIS();
	}

	/**
	 * <PRE>
	 *    経験年数を加算算出する式です。
	 *    初期値は3倍程度になり、経験年数が経過するにつれて1倍に近づきます。
	 * </PRE>
	 * @return 積算する重み。
	 * @author kobayashi
	 * @since 2015/10/09
	 */
	private double lfCalcExperienceExaminationTime()
	{
		return lfExperienceRate1 * Math.exp(-lfYearExperience*lfConExperience) + lfExperienceRate2;
	}

	/**
	 * <PRE>
	 *    経験年数を加算算出する式です。
	 *    初期値は0.8倍程度になり、経験年数が経過するにつれて1に近づきます。
	 * </PRE>
	 * @return 積算する重み。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	private double lfCalcExperienceAIS()
	{
		return lfExperienceRateAIS1 * Math.exp(-lfYearExperience*lfConExperienceAIS) + lfExperienceRateAIS2;
	}

	/**
	 * <PRE>
	 *    連携度を設定します。
	 *    医療技師エージェントが複数人いる場合、徐々に検査時間の効率が増します。
	 * </PRE>
	 * @param ArrayListClinicalEngineerAgents	他の医療技師エージェントのインスタンス
	 * @return 積算する重み。
	 * @author kobayashi
	 * @since 2015/10/11
	 */
	public double lfCalcAssociationRate( ArrayList<ERClinicalEngineerAgent> ArrayListClinicalEngineerAgents )
	{
		int i;
		double lfRes = 0.0;

		for( i = 0;i < ArrayListClinicalEngineerAgents.size(); i++ )
		{
			lfRes += ArrayListClinicalEngineerAgents.get(i).lfGetAssociationRate();
		}
		lfContributionAssociationRate = 1.0/lfRes;
		return 1.0/lfRes;
	}

	/**
	 * <PRE>
	 *    疲労度を計算する式です。
	 *    時間経過に従い値がどんどん小さくなります。
	 * </PRE>
	 * @return 積算する重み。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	private double lfCalcTired()
	{
		return 1.0-Math.exp(-lfTiredRate*lfConTired1);
	}

	/**
	 * <PRE>
	 *    連携度を取得します。
	 * </PRE>
	 * @return 医療技師エージェントの連携度
	 */
	public double lfGetAssociationRate()
	{
		return lfAssociationRate;
	}

	/**
	 * <PRE>
	 *   技士エージェントが保持している頭部のAISを取得します。
	 * </PRE>
	 * @return 頭部AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public double lfGetAISHead()
	{
		return lfExamAISHead;
	}

	/**
	 * <PRE>
	 *   技士エージェントが保持している顔面のAISを取得します。
	 * </PRE>
	 * @return 顔面AISを返却。
	 */
	public double lfGetAISFace()
	{
		return lfExamAISFace;
	}

	/**
	 * <PRE>
	 *   技士エージェントが保持している首のAISを取得します。
	 * </PRE>
	 * @return 首AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public double lfGetAISNeck()
	{
		return lfExamAISNeck;
	}

	/**
	 * <PRE>
	 *   技士エージェントが保持している胸部のAISを取得します。
	 * </PRE>
	 * @return 胸部AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public double lfGetAISThorax()
	{
		return lfExamAISThorax;
	}

	/**
	 * <PRE>
	 *   技士エージェントが保持している腹部のAISを取得します。
	 * </PRE>
	 * @return 腹部AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public double lfGetAISAbdomen()
	{
		return lfExamAISAbdomen;
	}

	/**
	 * <PRE>
	 *   技士エージェントが保持している脊椎のAISを取得します。
	 * </PRE>
	 * @return 脊椎AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public double lfGetAISSpine()
	{
		return lfExamAISSpine;
	}

	/**
	 * <PRE>
	 *   技士エージェントが保持している上肢のAISを取得します。
	 * </PRE>
	 * @return 上肢AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public 	double lfGetAISUpperExtremity()
	{
		return lfExamAISUpperExtremity;
	}

	/**
	 * <PRE>
	 *   技士エージェントが保持している下肢のAISを取得します。
	 * </PRE>
	 * @return 下肢AISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public 	double lfGetAISLowerExtremity()
	{
		return lfExamAISLowerExtremity;
	}

	/**
	 * <PRE>
	 *   技士エージェントが保持している特定しない（表面・熱傷・その他外傷）場合のAISを取得します。
	 * </PRE>
	 * @return 特定しないAISを返却。
	 * @author kobayashi
	 * @since 2015/07/17
	 */
	public double lfGetAISUnspecified()
	{
		return lfExamAISUnspecified;
	}

	/**
	 * <PRE>
	 *    検査時間を取得します。
	 * </PRE>
	 * @return 医療技師エージェントの検査時間
	 */
	public double lfGetExaminationTime()
	{
		return lfExaminationTime;
	}

	/**
	 * <PRE>
	 *    現在の医療技師が作業開始してからの時間を取得します。
	 *    なお、1患者開始からの時間で終了したら0に初期化します。
	 * </PRE>
	 * @return 作業時間
	 */
	public double lfGetCurrentPassOverTime()
	{
		return lfCurrentPassOverTime;
	}

	/**
	 * <PRE>
	 *    現在対応中か否かのフラグを取得します。
	 * </PRE>
	 * @return 医療技師エージェントの対応フラグ
	 */
	public int iGetAttending()
	{
		return iAttending;
	}

	/**
	 * <PRE>
	 *    医療技師エージェントの所属部門を取得します。
	 * </PRE>
	 * @return 医療技師エージェントの所属部門
	 */
	public int iGetClinicalEngineerDepartment()
	{
		return iClinicalEngineerDepartment;
	}

	/**
	 * <PRE>
	 *    医療技師エージェントが依頼を受けた医師エージェントの所属部門を取得します。
	 * </PRE>
	 * @return 医療技師エージェントの所属部門
	 */
	public int iGetRequestDoctorDepartment()
	{
		// TODO 自動生成されたメソッド・スタブ
		return iDoctorDepartment;
	}

	/**
	 * <PRE>
	 *    現在対応中か否かを表すフラグを設定します。
	 * </PRE>
	 * @param iAttendingData 対応中フラグ(0:非対応, 1:対応中)
	 */
	public void vSetAttending( int iAttendingData )
	{
		iAttending = iAttendingData;
	}

	/**
	 * <PRE>
	 *   医療技師の所属部門を設定します。
	 * </PRE>
	 * @param iDepartment	所属している部屋
	 */
	public void vSetClinicalEngineerDepartment( int iDepartment )
	{
		iClinicalEngineerDepartment = iDepartment;
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
	 *    経験年数を設定します。
	 * </PRE>
	 * @param lfCon	経験年数重み
	 */
	public void vSetConExperience( double lfCon )
	{
		lfConExperience = lfCon;
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
	 * @param lfCon	疲労度割合
	 */
	public void vSetTiredRate( double lfCon )
	{
		lfTiredRate = lfCon;
	}

	/**
	 * <PRE>
	 *    連携度を設定します。
	 * </PRE>
	 * @param lfCon	連携度
	 */
	public void vSetAssociationRate( double lfCon )
	{
		lfAssociationRate = lfCon;
	}

	/**
	 * <PRE>
	 *    医療技師エージェントの検査時間を設定します。
	 *    10    X線室(胸部)   	2～3分
	 *    11    X線室(骨部)		10～30分
	 *    12	CT室			20～30分
	 *    13	MRI室			20～40分
	 *    14	血管造影室		180～240分
	 * </PRE>
	 */
	public void vSetExaminationTime()
	{
		double lfExamTime;
		ERClinicalEngineerAgentException eee;
		eee = new ERClinicalEngineerAgentException();

		// X線室（肺、心臓、胸部大動脈）
		if( iClinicalEngineerDepartment == 10 )
		{
			// X線室の検査時間を設定します。（2～3分）乱数により検査時間の誤差を与えます。
//			lfExamTime = rnd.NextUnif()/lfCalcExperience()+2;
			lfExamTime = 2.0+rnd.NextUnif()*lfCalcExperienceExaminationTime()*lfContributionAssociationRate;
		}
		// CT室
		else if( iClinicalEngineerDepartment == 11 )
		{
			// CT室の検査時間を設定します。（5～20分）
//			lfExamTime = 5+15*rnd.NextUnif()/lfCalcExperience();
			lfExamTime = 5+15*rnd.NextUnif()*lfCalcExperienceExaminationTime()*lfContributionAssociationRate;
		}
		// MRI室
		else if( iClinicalEngineerDepartment == 12 )
		{
			// MRI室の検査時間を指定します。（20～40分）
//			lfExamTime = 20+20*rnd.NextUnif()/lfCalcExperience();
			lfExamTime = 20+20*rnd.NextUnif()*lfCalcExperienceExaminationTime()*lfContributionAssociationRate;
		}
		// 血管造影室
		else if( iClinicalEngineerDepartment == 13 )
		{
			// 血管造影室の検査時間を設定します。（60～180分）
//			lfExamTime = 60+120*rnd.NextUnif()/lfCalcExperience();
			lfExamTime = 60+120*rnd.NextUnif()*lfCalcExperienceExaminationTime()*lfContributionAssociationRate;
		}
		// X線室（骨、関節の状態、筋肉、脂肪組織の状態の検査）
		else if( iClinicalEngineerDepartment == 14 )
		{
			// 検査時間を設定します。（10～30分）乱数により検査時間の誤差を与えます。
//			lfExamTime = 10.0+20*rnd.NextUnif()/lfCalcExperience();
			lfExamTime = 10.0+20*rnd.NextUnif()*lfCalcExperienceExaminationTime()*lfContributionAssociationRate;
		}
		// FAST室（超音波測定）
		else if( iClinicalEngineerDepartment == 15 )
		{
			// 検査時間を設定します。（10～30分）乱数により検査時間の誤差を与えます。
//			lfExamTime = 10.0+20*rnd.NextUnif()/lfCalcExperience();
			lfExamTime = 5.0+5.0*rnd.NextUnif()*lfCalcExperienceExaminationTime()*lfContributionAssociationRate;
		}
		else
		{
			lfExamTime = 0.0;
//			eee.SetErrorInfo( ERCEA_INVALID_DATA_ERROR, "ERClinicalEngineerAgent", "vSetExaminationTime", "不正な数値です。" );
//			throw( eee );
		}
		lfExaminationTime = lfExamTime*60.0;
	}

	/**
	 * <PRE>
	 *    救急部門に登録されている全エージェントのIDを設定します。
	 * </PRE>
	 * @param ArrayListNurseAgentIdsData				全看護師エージェントのID
	 * @param ArrayListDoctorAgentIdsData				全医師エージェントのID
	 * @param ArrayListClinicalEngineerAgentIdsData		全医療技師エージェントのID
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
	 *    経験値による重みづけ計算に使用するパラメータその1です。
	 * </PRE>
	 * @param lfData	経験年数パラメータ1
	 */
	public void vSetExperienceRate1( double lfData )
	{
		lfExperienceRate1 = lfData;
	}

	/**
	 * <PRE>
	 *    経験値による重みづけ計算に使用するパラメータその2です。
	 * </PRE>
	 * @param lfData	経験年数パラメータ2
	 */
	public void vSetExperienceRate2( double lfData )
	{
		lfExperienceRate2 = lfData;
	}

	/**
	 * <PRE>
	 *    経験値による重みづけ計算に使用するパラメータです。
	 * </PRE>
	 * @param lfData	AIS重症度経験年数パラメータ
	 */
	public void vSetConExperienceAIS( double lfData )
	{
		lfConExperienceAIS = lfData;
	}

	/**
	 * <PRE>
	 *    経験値(AIS重症度)による重みづけ計算に使用するパラメータです。
	 * </PRE>
	 * @param lfData	AIS重症度パラメータ1
	 */
	public void vSetExperienceRateAIS1( double lfData )
	{
		lfExperienceRateAIS1 = lfData;
	}

	/**
	 * <PRE>
	 *    経験値(AIS重症度)による重みづけ計算に使用するパラメータです。
	 * </PRE>
	 * @param lfData	AIS重症度パラメータ2
	 */
	public void vSetExperienceRateAIS2( double lfData )
	{
		lfExperienceRateAIS2 = lfData;
	}

	public void vSetRoomNumber( int iNum )
	{
		iRoomNumber = iNum;
	}

	/**
	 * <PRE>
	 *   医師エージェントへ検査結果を送信します。
	 * </PRE>
	 * @param erPAgent				検査対象の患者エージェント
	 * @param erDoctorAgent			送信先医師エージェント
	 * @param iFromAgentId  		送信先のエージェント（ここでは医師エージェント）
	 * @param iToAgentId			送信元のエージェント（ここでは臨床技士エージェント）
	 * @author kobayashi
	 * @since 2015/07/31
	 */
	public void vSendToDoctorAgentMessage( ERPatientAgent erPAgent, ERDoctorAgent erDoctorAgent, int iFromAgentId, int iToAgentId )
	{
		Message mesSend;
		mesSend = new Message();
		mesSend.setData( new MessageFromEngToDoc() );
		mesSend.setFromAgent( iFromAgentId );
		mesSend.setToAgentId( iToAgentId );
		((MessageFromEngToDoc)mesSend.getData()).vSetExamAISHead( lfExamAISHead );
		((MessageFromEngToDoc)mesSend.getData()).vSetExamAISFace( lfExamAISFace );
		((MessageFromEngToDoc)mesSend.getData()).vSetExamAISNeck( lfExamAISNeck );
		((MessageFromEngToDoc)mesSend.getData()).vSetExamAISThorax( lfExamAISThorax );
		((MessageFromEngToDoc)mesSend.getData()).vSetExamAISAbdomen( lfExamAISAbdomen );
		((MessageFromEngToDoc)mesSend.getData()).vSetExamAISSpine( lfExamAISSpine );
		((MessageFromEngToDoc)mesSend.getData()).vSetExamAISUpperExtremity( lfExamAISUpperExtremity );
		((MessageFromEngToDoc)mesSend.getData()).vSetExamAISLowerExtremity( lfExamAISLowerExtremity );
		((MessageFromEngToDoc)mesSend.getData()).vSetExamAISUnspecified( lfExamAISUnspecified );
		((MessageFromEngToDoc)mesSend.getData()).vSetExaminationTime( lfCurrentPassOverTime );
		((MessageFromEngToDoc)mesSend.getData()).vSetClinicalEngineerDepartment( iClinicalEngineerDepartment );
		((MessageFromEngToDoc)mesSend.getData()).vSetRequestAnatomys( aiRequestAnatomys );
		((MessageFromEngToDoc)mesSend.getData()).vSetRequestExamination( iRequestExamination );
		((MessageFromEngToDoc)mesSend.getData()).vSetRequestExaminationNum( iRequestExaminationNum );
		((MessageFromEngToDoc)mesSend.getData()).vSetERPatientAgent( erPAgent );
//		this.sendMessage( mesSend );

		// メッセージ送信用にエージェントを一時的に生成
		erDoctorAgent.vSendMessage( mesSend );
	}

	/**
	 * <PRE>
	 *   看護師エージェントへ検査結果を送信します。
	 * </PRE>
	 * @param erPAgent				検査対象の患者エージェント
	 * @param erNurseAgent			送信先看護師エージェント
	 * @param iFromAgentId  		送信先のエージェント（ここでは医師エージェント）
	 * @param iToAgentId			送信元のエージェント（ここでは臨床技士エージェント）
	 * @author kobayashi
	 * @since 2015/07/31
	 */
	public void vSendToNurseAgentMessage( ERPatientAgent erPAgent, ERNurseAgent erNurseAgent, int iFromAgentId, int iToAgentId )
	{
		Message mesSend;
		mesSend = new Message();
		mesSend.setData( new MessageFromEngToNurse() );
		mesSend.setFromAgent( iFromAgentId );
		mesSend.setToAgentId( iToAgentId );
		((MessageFromEngToNurse)mesSend.getData()).vSetExamAISHead( lfExamAISHead );
		((MessageFromEngToNurse)mesSend.getData()).vSetExamAISFace( lfExamAISFace );
		((MessageFromEngToNurse)mesSend.getData()).vSetExamAISNeck( lfExamAISNeck );
		((MessageFromEngToNurse)mesSend.getData()).vSetExamAISThorax( lfExamAISThorax );
		((MessageFromEngToNurse)mesSend.getData()).vSetExamAISAbdomen( lfExamAISAbdomen );
		((MessageFromEngToNurse)mesSend.getData()).vSetExamAISSpine( lfExamAISSpine );
		((MessageFromEngToNurse)mesSend.getData()).vSetExamAISUpperExtremity( lfExamAISUpperExtremity );
		((MessageFromEngToNurse)mesSend.getData()).vSetExamAISLowerExtremity( lfExamAISLowerExtremity );
		((MessageFromEngToNurse)mesSend.getData()).vSetExamAISUnspecified( lfExamAISUnspecified );
		((MessageFromEngToNurse)mesSend.getData()).vSetExaminationTime( lfCurrentPassOverTime );
		((MessageFromEngToNurse)mesSend.getData()).vSetClinicalEngineerDepartment( iClinicalEngineerDepartment );
		((MessageFromEngToNurse)mesSend.getData()).vSetRequestAnatomys( aiRequestAnatomys );
		((MessageFromEngToNurse)mesSend.getData()).vSetRequestExamination( iRequestExamination );
		((MessageFromEngToNurse)mesSend.getData()).vSetRequestExaminationNum( iRequestExaminationNum );
		((MessageFromEngToNurse)mesSend.getData()).vSetERPatientAgent( erPAgent );
//		this.sendMessage( mesSend );

		// メッセージ送信用にエージェントを一時的に生成
		erNurseAgent.vSendMessage( mesSend );
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
		int i;
		double lfSecond;
		ERClinicalEngineerAgentException cERCleae = new ERClinicalEngineerAgentException();
		Message mess;	// 医師からの依頼メッセージ。

		// 医師エージェントから送信されたメッセージを取得します。
		lfSecond = timeStep / 1000.0;

		try
		{
			// 対応中でない場合にメッセージ取得。
			if( this.iAttending == 0 )
			{
				mess = messGetMessage();
				// 医師→医療技師メッセージを確認します。
				if( mess != null )
				{
					// 医師から検査依頼があるかいないかを確認します。
					// 医師からのメッセージかどうかを判定します。
					if( mess.getData() instanceof MessageFromDocToEng )
					{
						// 検査リクエストを取得します。
						erPAgent = ((MessageFromDocToEng)mess.getData()).cGetERPatientAgent();
						iRequestExamination = ((MessageFromDocToEng)mess.getData()).iGetRequestExamination();
						aiRequestAnatomys = ((MessageFromDocToEng)mess.getData()).aiGetRequestAnatomys();
						iRequestExaminationNum = ((MessageFromDocToEng)mess.getData()).iGetRequestExaminationNum();

						// 担当した医師エージェントを取得します。
						iDoctorId = (int)mess.getToAgentId();
						iDoctorDepartment = ((MessageFromDocToEng)mess.getData()).iGetDoctorDepartment();

						// 緊急度情報を取得します。
						iEmergencyLevel = ((MessageFromDocToEng)mess.getData()).iGetEmergencyLevel();
					}
				}
			}
			// なくなったので患者エージェントを削除します。
			if( erPAgent != null )
			{
				if( erPAgent.iGetSurvivalFlag() == 0 )
				{
					erPAgent = null;
					iAttending = 0;
				}
			}
			// 診察中であれば、経過時間を計算します。
			if( iAttending == 1 )
			{
				lfCurrentPassOverTime += lfSecond;
				lfTotalExaminationTime += lfSecond;
			}
			else
			{
				lfCurrentPassOverTime = 0.0;
			}
			lfTotalTime += lfSecond;

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
		}
		catch( NullPointerException npe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cERCleae.SetErrorInfo( ERCEA_FATAL_ERROR, "ERClinicalEngineerAgent", "action", "不明、および致命的エラー", ste[0].getLineNumber() );
			String strMethodName = cERCleae.strGetMethodName();
			String strClassName = cERCleae.strGetClassName();
			String strErrDetail = cERCleae.strGetErrDetail();
			int iErrCode = cERCleae.iGetErrCode();
			int iErrLine = cERCleae.iGetErrorLine();
			cClinicalEngineerLog.warning( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
		}
		catch( RuntimeException re )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cERCleae.SetErrorInfo( ERCEA_FATAL_ERROR, "ERClinicalEngineerAgent", "action", "不明、および致命的エラー", ste[0].getLineNumber() );
			// エラー詳細を出力
			String strMethodName = cERCleae.strGetMethodName();
			String strClassName = cERCleae.strGetClassName();
			String strErrDetail = cERCleae.strGetErrDetail();
			int iErrCode = cERCleae.iGetErrCode();
			int iErrLine = cERCleae.iGetErrorLine();
			cClinicalEngineerLog.warning( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
		}
		catch( IOException ioe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cERCleae.SetErrorInfo( ERCEA_FATAL_ERROR, "ERClinicalEngineerAgent", "action", "不明、および致命的エラー", ste[0].getLineNumber() );
			// エラー詳細を出力
			String strMethodName = cERCleae.strGetMethodName();
			String strClassName = cERCleae.strGetClassName();
			String strErrDetail = cERCleae.strGetErrDetail();
			int iErrCode = cERCleae.iGetErrCode();
			int iErrLine = cERCleae.iGetErrorLine();
			cClinicalEngineerLog.warning( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
		}
	}

	/**
	 * <PRE>
	 *    患者移動中フラグを設定します。
	 * </PRE>
	 * @param iData	移動中フラグ(0:移動していない、1：移動している。)
	 */
	public void vSetPatientMoveWaitFlag(int iData)
	{
		iPatientMoveWaitFlag = iData;
	}

	/**
	 * <PRE>
	 *    医療技師エージェントのログ出力を設定します。
	 * </PRE>
	 * @param log	ロガーインスタンス
	 */
	public void vSetLog(Logger log)
	{
		// TODO 自動生成されたメソッド・スタブ
		cClinicalEngineerLog = log;
	}


	/**
	 * <PRE>
	 *    シミュレーション終了時間を設定します。
	 * </PRE>
	 * @param lfEndTime	シミュレーションの終了時間
	 */
	public void vSetSimulationEndTime( double lfEndTime )
	{
		lfSimulationEndTime = lfEndTime;
	}

	/**
	 * <PRE>
	 *   逆シミュレーションモードの設定をします。
	 * </PRE>
	 * @param iMode	逆シミュレーションのモード
	 */
	public void vSetInverseSimMode( int iMode )
	{
		iInverseSimMode = iMode;
	}

	/**
	 * <PRE>
	 *    クリティカルセクションを設定します。
	 * </PRE>
	 * @param cs	クリティカルセクションのインスタンス
	 */
	public void vSetCriticalSection(Object cs)
	{
		// TODO 自動生成されたメソッド・スタブ
		erClinicalEngineerCriticalSection = cs;
	}

	/**
	 * <PRE>
	 *    ファイルの書き込みます。
	 * </PRE>
	 * @param iFlag			ファイル書き込みモード(0：全書き込み、1：最初と最後書き込み)
	 * @param lfTime		現在のシミュレーション時刻
	 * @throws IOException	ファイル書き込みエラー
	 */
	public void vWriteFile( int iFlag, double lfTime ) throws IOException
	{
		String strData = lfTotalTime + "," + this.getTotakProcessingTime() + "," + lfCurrentPassOverTime + "," + lfTotalTime + ",";
		strData += lfExamAISHead + "," + lfExamAISFace + "," + lfExamAISNeck + "," + lfExamAISThorax + "," + lfExamAISAbdomen + "," + lfExamAISSpine  + "," + lfExamAISLowerExtremity + "," + lfExamAISUpperExtremity + "," + lfExamAISUnspecified;
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
	 *    メルセンヌツイスターインスタンスを設定します。
	 * </PRE>
	 * @param sfmtRandom	メルセンヌツイスターインスタンス
	 */
	public void vSetRandom(utility.sfmt.Rand sfmtRandom)
	{
		// TODO 自動生成されたメソッド・スタブ
		rnd = sfmtRandom;
	}

	public void vSetInitParam(InitSimParam initparam)
	{
		// TODO 自動生成されたメソッド・スタブ
		initParamClinicalEngineerAgent = initparam;
	}

	/**
	 * <PRE>
	 *   医療技師エージェントが何階にいるか取得します。
	 * </PRE>
	 * @return 患者エージェントの現在いる階数
	 */
	public int iGetFloor()
	{
		return erTriageNode.iGetFloor();
	}

	/**
	 * <PRE>
	 *   医療技師エージェントが何階にいるか設定します。
	 * </PRE>
	 * @param 現在いる階数
	 */
	public void vSetFloor( int iFloorData )
	{
		erTriageNode.vSetFloor( iFloorData );
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

}
