package inverse;
import inverse.optimization.abc.Abc;
import inverse.optimization.constraintcondition.ConstraintConditionInterface;
import inverse.optimization.ga.BaseGenAlg;
import inverse.optimization.ga.GenAlgException;
import inverse.optimization.objectivefunction.ObjectiveFunctionInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import jp.ac.nihon_u.cit.su.furulab.fuse.Environment;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import main.ERFinisher;
import triage.ERDepartment;
import utility.csv.CCsv;


public class InverseSimulationEngine
{
	private ERDepartment erDepartments[];
	private int iGensNumber;					// 遺伝子数
	private int iGenerationNumber;				// 世代計算回数
	private int iGensVectorDimension;			// 遺伝子の次元数
	private SimulationEngine SimEngine;			// シミュレーションエンジンクラス
	private ERFinisher SimFinisher;				// 終了判定を行うクラス
	private Environment SimEnv;					// シミュレーション環境クラス
	private Logger SimLog;						// ログ出力クラス
	private utility.sfmt.Rand SimRandom;				// メルセンヌツイスター乱数
	private Object invEngineCriticalSection;	// クリティカルセクション
	private ObjectiveFunctionInterface pflfObjectiveFunction; // 評価指標のコールバック関数
	private double pplfErArgument[][];
	private double plfFitProb[];

	private int iSimulationEndTime;				// シミュレーション終了時間
	private int iInverseSimulationMethod;		// 逆シミュレーション手法
	private String strConsultationRoomPath;
	private String strOperationRoomPath;
	private String strEmergencyRoomPath;
	private String strObservationRoomPath;
	private String strSevereInjuryObservationRoomPath;
	private String strIntensiveCareUnitPath;
	private String strHighCareUnitPath;
	private String strGeneralWardPath;
	private String strWaitingRoomPath;
	private String strXRayRoomPath;
	private String strCTRoomPath;
	private String strMRIRoomPath;
	private String strAngiographyRoomPath;
	private String strFastRoomPath;
	private double lfPatientPepole;
	private int iRandomMode;
	private int iInverseSimFlag;
	private int iEvaluationIndexCompMode;			// 逆シミュレーション用パラメーターのモード設定 1:全体平均、2:1シミュレーションにおける最大値
	private int iEvaluationIndexMode;				// 逆シミュレーション用評価指標 101:NEDOCS, 102:ED Work Score, 103:EDWIN

	// 遺伝的アルゴリズム関連パラメータ
	private BaseGenAlg ga;						// 遺伝的アルゴリズム
	private double lfGaMutationProb;			// 遺伝的アルゴリズム突然変異
	private double lfGaCorssOverProb;			// 遺伝的アルゴリズム交叉確率
	private int iGaSelectNumber;				// 遺伝的アルゴリズムトーナメント方式のトーナメント数
	private int iGaCrossOverMethod;				// 遺伝的アルゴリズム交叉手法
	private int iGaCrossOverLoc;				// 遺伝的アルゴリズム交叉位置
	private int iGaMethod;						// 遺伝的アルゴリズム淘汰方式

	// 粒子群最適法関連パラメータ
	private int iPsoMethod;						// 粒子群最適法方式

	// ABC法関連パラメータ
	private Abc abc;							// ABC法を実行するクラス
	private int iAbcMethod;						// ABC法の方式
	private int iSearchNum;						// 探索拠点数
	private int iLimitCount;					// 連続して更新されない回数
	private int iAbcUpperSearchNum;				// 適合度上位の蜂の数
	private double lfFitAccuracy;				// 適合度収束精度
	private double lfFitBound;					// 適合度許容限界値
	private int iIntervalMinNum;				// 最低反復回数
	private double lfConvergenceParam;			// 解への収束パラメータ状況

	// 実数値GA関連パラメータ
	private int iRcGaMethod;					// 実数値ＧＡ方式
	private double lfAlpha;						// UNDX用パラメータ
	private double lfBeta;						// UNDX用パラメータ
	private int iCrossOverNum;					// UNDX用交叉回数
	private int iParentNumber;					// REX用選択する親の数
	private int iChildrenNumber;				// REX用選択する親の数
	private int iUpperEvalChildrenNumber;		// REX用選択する適合度上位の子供の数
	private double lfLearningRate;				// AREX用学習率

	private CCsv csvWriteERData;
	private CCsv csvWriteERConstraintFunctionValue;
	private String strFileName;
	private String strConstraintFunctionValueFileName;
	private int iSurvivalNumber;				// 生存割合

	private int iUpdateCount;

	private int iFileWriteMode;

	private int iPatientArrivalMode;

	// 時間計測用
	private long lStartTime;
	private long lEndTime;
	private ArrayList<Long> alElapesdTime;

	/**
	 * <PRE>
	 *    逆シミュレーションのコンストラクタです。
	 * </PRE>
	 */
	public InverseSimulationEngine()
	{
		erDepartments 			= null;
		iGensNumber 			= 0;
		iGenerationNumber 		= 0;
		iGensVectorDimension 	= 0;
		pflfObjectiveFunction	= null;
		pplfErArgument 			= null;
		iSurvivalNumber 		= 0;
		iUpdateCount			= 0;
		iFileWriteMode			= 0;
		iEvaluationIndexCompMode = 1;		// 逆シミュレーション用パラメーターのモード設定 1:全体平均、2:1シミュレーションにおける最大値
		iEvaluationIndexMode = 101;			// 逆シミュレーション用評価指標 101:NEDOCS, 102:ED Work Score, 103:EDWIN
		alElapesdTime = new ArrayList<Long>();
	}

	/**
	 * <PRE>
	 *    逆シミュレーションのコンストラクタです。
	 * </PRE>
	 * @param iGenerationNum							逆シミュレーションの回数
	 * @param iGensNum									逆シミュレーション上で登場する個体の個数
	 * @param iGensVectorDimNum							逆シミュレーション上で登場する一個体の特徴ベクトルの次元数
	 * @param iInvSimMethod								逆シミュレーションのモード
	 * @param iSimEndTime								シミュレーションの終了時間[秒]
	 * @param engine									FUSEのシミュレーションエンジンクラス
	 * @param env										FUSEの環境クラス
	 * @param strConsultationRoomPath					診察室の設定ファイルパス
	 * @param strOperationRoomPath						手術室の設定ファイルパス
	 * @param strEmergencyRoomPath						初療室の設定ファイルパス
	 * @param strObservationRoomPath					観察室の設定ファイルパス
	 * @param strSevereInjuryObservationRoomPath		重症観察室の設定ファイルパス
	 * @param strIntensiveCareUnitPath					集中治療室の設定ファイルパス
	 * @param strHighCareUnitPath						高度治療室の設定ファイルパス
	 * @param strGeneralWardPath						一般病棟の設定ファイルパス
	 * @param strWaitingRoomPath						待合室の設定ファイルパス
	 * @param strXRayRoomPath							X線室の設定ファイルパス
	 * @param strCTRoomPath								CT室の設定ファイルパス
	 * @param strMRIRoomPath							MRI室の設定ファイルパス
	 * @param strAngiographyRoomPath					血管造影室の設定ファイルパス
	 * @param strFastRoomPath							超音波室の設定ファイルパス
	 * @param log										ロガークラスのインスタンス
	 * @param cs										クリティカルセクションのインスタンス
	 * @param rnd										メルセンヌツイスターのインスタンス
	 * @param lfPatientPepoleData						到達する患者の人数
	 * @param iRandomMode								患者の発生分布
	 * @param iInverseSimFlag							逆シミュレーションか否か
	 * @param iFileWriteModeData						ファイル書き込みモード
	 * @param iPatientArrivalModeData					患者到達モード（0:通常,1:災害）
	 * @throws IOException								java標準のIO例外クラス
	 */
	public InverseSimulationEngine( int iGenerationNum,
			 int iGensNum,
			 int iGensVectorDimNum,
			 int iInvSimMethod,
			 int iSimEndTime,
			 SimulationEngine engine,
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
			 Logger log,
			 Object cs,
			 utility.sfmt.Rand rnd,
			 double lfPatientPepoleData,
			 int iRandomMode,
			 int iInverseSimFlag,
			 int iFileWriteModeData,
			 int iPatientArrivalModeData) throws IOException
	{
		vInitialize( iGenerationNum, iGensNum, iGensVectorDimNum, iInvSimMethod,
				iSimEndTime,
				 engine,
				 env,
				 strConsultationRoomPath,
				 strOperationRoomPath,
				 strEmergencyRoomPath,
				 strObservationRoomPath,
				 strSevereInjuryObservationRoomPath,
				 strIntensiveCareUnitPath,
				 strHighCareUnitPath,
				 strGeneralWardPath,
				 strWaitingRoomPath,
				 strXRayRoomPath,
				 strCTRoomPath,
				 strMRIRoomPath,
				 strAngiographyRoomPath,
				 strFastRoomPath,
				 log,
				 cs,
				 rnd,
				 lfPatientPepoleData,
				 iRandomMode,
				 iInverseSimFlag,
				 iFileWriteModeData,
				 iPatientArrivalModeData );
	}

	/**
	 * <PRE>
	 *    逆シミュレーションの初期化を行います。
	 * </PRE>
	 * @param iGenerationNum							逆シミュレーションの回数
	 * @param iGensNum									逆シミュレーション上で登場する個体の個数
	 * @param iGensVectorDimNum							逆シミュレーション上で登場する一個体の特徴ベクトルの次元数
	 * @param iInvSimMethod								逆シミュレーションのモード
	 * @param iSimEndTime								シミュレーションの終了時間[秒]
	 * @param engine									FUSEのシミュレーションエンジンクラス
	 * @param env										FUSEの環境クラス
	 * @param strConsultationRoomPathData				診察室の設定ファイルパス
	 * @param strOperationRoomPathData					手術室の設定ファイルパス
	 * @param strEmergencyRoomPathData					初療室の設定ファイルパス
	 * @param strObservationRoomPathData				観察室の設定ファイルパス
	 * @param strSevereInjuryObservationRoomPathData	重症観察室の設定ファイルパス
	 * @param strIntensiveCareUnitPathData				集中治療室の設定ファイルパス
	 * @param strHighCareUnitPathData					高度治療室の設定ファイルパス
	 * @param strGeneralWardPathData					一般病棟の設定ファイルパス
	 * @param strWaitingRoomPathData					待合室の設定ファイルパス
	 * @param strXRayRoomPathData						X線室の設定ファイルパス
	 * @param strCTRoomPathData							CT室の設定ファイルパス
	 * @param strMRIRoomPathData						MRI室の設定ファイルパス
	 * @param strAngiographyRoomPathData				血管造影室の設定ファイルパス
	 * @param strFastRoomPathData						超音波室の設定ファイルパス
	 * @param log										ロガークラスのインスタンス
	 * @param cs										クリティカルセクションのインスタンス
	 * @param rnd										メルセンヌツイスターのインスタンス
	 * @param lfPatientPepoleData						到達する患者の人数
	 * @param iRandomModeData							患者の発生分布
	 * @param iInverseSimFlagData						逆シミュレーションか否か
	 * @param iFileWriteModeData						ファイル書き込みモード
	 * @param iPatientArrivalModeData					患者到達モード（0:通常,1:災害）
	 * @throws IOException								java標準のIO例外クラス
	 */
	public void vInitialize( int iGenerationNum,
							 int iGensNum,
							 int iGensVectorDimNum,
							 int iInvSimMethod,
							 int iSimEndTime,
							 SimulationEngine engine,
							 Environment env,
							 String strConsultationRoomPathData,
							 String strOperationRoomPathData,
							 String strEmergencyRoomPathData,
							 String strObservationRoomPathData,
							 String strSevereInjuryObservationRoomPathData,
							 String strIntensiveCareUnitPathData,
							 String strHighCareUnitPathData,
							 String strGeneralWardPathData,
							 String strWaitingRoomPathData,
							 String strXRayRoomPathData,
							 String strCTRoomPathData,
							 String strMRIRoomPathData,
							 String strAngiographyRoomPathData,
							 String strFastRoomPathData,
							 Logger log,
							 Object cs,
							 utility.sfmt.Rand rnd,
							 double lfPatientPepoleData,
							 int iRandomModeData,
							 int iInverseSimFlagData,
							 int iFileWriteModeData,
							 int iPatientArrivalModeData ) throws IOException
	{
		int i;

		// 初期化を実行します。
		SimEngine					= engine;
		SimEnv						= env;
		SimLog						= log;
		SimRandom					= rnd;
		iGensNumber					= iGensNum;
		iGenerationNumber			= iGenerationNum;
		iGensVectorDimension		= iGensVectorDimNum;
		iInverseSimulationMethod	= iInvSimMethod;
		iSimulationEndTime			= iSimEndTime;
		pplfErArgument				= new double[iGensNumber][iGensVectorDimNum];
		erDepartments				= new ERDepartment[iGensNumber];

		strConsultationRoomPath				= strConsultationRoomPathData;
		strOperationRoomPath				= strOperationRoomPathData;
		strEmergencyRoomPath				= strEmergencyRoomPathData;
		strObservationRoomPath				= strObservationRoomPathData;
		strSevereInjuryObservationRoomPath	= strSevereInjuryObservationRoomPathData;
		strIntensiveCareUnitPath			= strIntensiveCareUnitPathData;
		strHighCareUnitPath					= strHighCareUnitPathData;
		strGeneralWardPath					= strGeneralWardPathData;
		strWaitingRoomPath					= strWaitingRoomPathData;
		strXRayRoomPath						= strXRayRoomPathData;
		strCTRoomPath						= strCTRoomPathData;
		strMRIRoomPath						= strMRIRoomPathData;
		strAngiographyRoomPath				= strAngiographyRoomPathData;
		strFastRoomPath						= strFastRoomPathData;
		lfPatientPepole						= lfPatientPepoleData;
		iRandomMode							= iRandomModeData;
		iInverseSimFlag						= iInverseSimFlagData;
		invEngineCriticalSection			= cs;
		iFileWriteMode						= iFileWriteModeData;
		iPatientArrivalMode					= iPatientArrivalModeData;

		SimLog.warning( "クラス名" + "," + this.getClass() + "," + "メソッド名" + "," + "vInitialize" + "," + "クリティカルセクションのアドレス" + "," + cs + "," );
		// 各救急部門オブジェクトにパラメーターを割り振ります。
		try
		{
			for( i = 0;i < erDepartments.length; i++ )
			{
				erDepartments[i] = new ERDepartment();
				erDepartments[i].vSetLog(SimLog);
				erDepartments[i].vSetSimulationEndTime( iSimulationEndTime );
				erDepartments[i].vSetErDepartmentRandom( rnd );
				erDepartments[i].vSetRandomEmergencyDepartment( 1 );
				erDepartments[i].vSetRandomEmergencyDepartmentAgents( 1 );
				erDepartments[i].vInitialize( engine, env, strConsultationRoomPathData,
											  strOperationRoomPathData, strEmergencyRoomPathData, strObservationRoomPathData,
											  strSevereInjuryObservationRoomPathData, strIntensiveCareUnitPathData,
											  strHighCareUnitPathData, strGeneralWardPathData, strWaitingRoomPathData,
											  strXRayRoomPathData, strCTRoomPathData, strMRIRoomPathData, strAngiographyRoomPathData,
											  strFastRoomPathData, lfPatientPepoleData, iRandomModeData, iInverseSimFlagData, iFileWriteModeData,
											  iPatientArrivalMode, SimRandom );
				erDepartments[i].vSetAllLog( log );
				erDepartments[i].vSetCriticalSection( cs );
			}
			csvWriteERData					= new CCsv();
			strFileName						= "./result_inverse_simulation.csv";
			csvWriteERData.vOpen( strFileName, "write");
			csvWriteERConstraintFunctionValue = new CCsv();
			strConstraintFunctionValueFileName	= "./result_constraint_function_inverse_simulation.csv";
			csvWriteERConstraintFunctionValue.vOpen( strConstraintFunctionValueFileName, "write" );

			// 初期状態を出力します。
			for( i = 0;i < pplfErArgument.length; i++ )
			{
				vSetArgument( pplfErArgument[i], erDepartments[i] );
//				pplfErArgument[i][36] = 1.0;
//				pplfErArgument[i][37] = 0.0;
			}
			vInitialOutput();

			// 時間計測を開始します。
			vGetStartTime();
		}
		catch( IOException ioe )
		{
			throw(ioe);
		}
	}

	/**
	 * <PRE>
	 *    遺伝的アルゴリズムの初期化を実行します。
	 * </PRE>
	 */
	public void vInitInvSimEngineGa()
	{

	}

	/**
	 * <PRE>
	 *    実数値遺伝的アルゴリズムの初期化を実行します。
	 * </PRE>
	 */
	public void vInitInvSimEngineRcGa()
	{

	}

	/**
	 * <PRE>
	 *    粒子群最適化法アルゴリズムの初期化を実行します。
	 * </PRE>
	 */
	public void vInitInvSimEnginePso()
	{

	}

	/**
	 * <PRE>
	 *    ABC法の初期化を実行します。
	 * </PRE>
	 * @param iAbcMethodData	選択するABC法
	 * @param iSearchNumData	探索点数
	 * @param iLimitCountData	更新しない回数
	 */
	public void vInitInvSimEngineAbc( int iAbcMethodData, int iSearchNumData, int iLimitCountData )
	{
		iAbcMethod = iAbcMethodData;
		iSearchNum = iSearchNumData;
		iLimitCount = iLimitCountData;
		iIntervalMinNum = iGenerationNumber/2;				// 高性能化ABC法のパラメータ(世代数/2が標準設定)
		iAbcUpperSearchNum = 20;							// 高性能化ABC法のパラメータ(20が推奨値)
		lfConvergenceParam = 0.9;							// 高性能化ABC法のパラメータ(0.9が推奨値)
		lfFitBound = 0.0;									// 高性能化ABC法のパラメータ(0.0が推奨値)
		lfFitAccuracy = 0.000000000000001;					// 高性能化ABC法のパラメータ(10^-16が推奨値)
		lfAlpha	= 0.5;										// UNDX用パラメータ(0.5が推奨値)
		lfBeta	= 0.35;										// UNDX用パラメータ(0.35が推奨値)
		iCrossOverNum = 1600;								// UNDX用交叉回数(なるべく多めに設定。)
		iParentNumber = iGensVectorDimension+1;				// REX用選択する親の数(次元数+1が通常)
		iChildrenNumber = iGensVectorDimension*4;			// REX用選択する親の数(次元数×4当りが通常)
		iUpperEvalChildrenNumber = iGensVectorDimension+1;	// REX用選択する適合度上位の子供の数(次元数+1が通常)
		lfLearningRate = 1.0/iGensVectorDimension;			// AREX用学習率(次元数の逆数が通常)

		// オリジナルArtificial Bee Colony Method(2005)
		if( iAbcMethod == 1 )
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount );
		}
		// 変形Artificial Bee Colony Method (2011)
		else if( iAbcMethod == 2 )
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount, iIntervalMinNum, iAbcUpperSearchNum, lfConvergenceParam, lfFitBound, lfFitAccuracy );
		}
		// 交叉を用いたArtificial Bee Colony Method (2013)
		else if( iAbcMethod == 3 )
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount );
		}
		//　GbestABC法
		else if( iAbcMethod == 4 )
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount );
		}
		//　Memetic ABC法
		else if( iAbcMethod == 5 )
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount );
		}
		//　Memetic ABC Algorithm(2013)
		else if( iAbcMethod == 6 )
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount, iCrossOverNum, lfAlpha, lfBeta );
		}
		// UNDXを混ぜたハイブリッドABC法(提案手法)
		else if( iAbcMethod == 7 )
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount, iCrossOverNum, lfAlpha, lfBeta );
		}
		// REXを混ぜたハイブリッドABC法(提案手法3)
		else if( iAbcMethod == 8 )
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount, iIntervalMinNum, iAbcUpperSearchNum, lfConvergenceParam, lfFitBound, lfFitAccuracy, iParentNumber, iChildrenNumber, iUpperEvalChildrenNumber, lfLearningRate );
		}
		// AREXを混ぜたハイブリッドABC法(提案手法4)
		else if(iAbcMethod == 9 )
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount, iIntervalMinNum, iAbcUpperSearchNum, lfConvergenceParam, lfFitBound, lfFitAccuracy, iParentNumber, iChildrenNumber, iUpperEvalChildrenNumber, lfLearningRate );
		}
		// HJABC法
		else if (iAbcMethod == 10)
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount, iIntervalMinNum, iAbcUpperSearchNum, lfConvergenceParam, lfFitBound, lfFitAccuracy, iParentNumber, iChildrenNumber, iUpperEvalChildrenNumber, lfLearningRate );
		}
		// ACABC法
		else if (iAbcMethod == 11 )
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount, iIntervalMinNum, iAbcUpperSearchNum, lfConvergenceParam, lfFitBound, lfFitAccuracy, iParentNumber, iChildrenNumber, iUpperEvalChildrenNumber, lfLearningRate );
		}
		// BestSoFarABC法
		else if (iAbcMethod == 12)
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount, iCrossOverNum, lfAlpha, lfBeta );
		}
		// PABC法
		else if (iAbcMethod == 13)
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount, iIntervalMinNum, iAbcUpperSearchNum, lfConvergenceParam, lfFitBound, lfFitAccuracy, iParentNumber, iChildrenNumber, iUpperEvalChildrenNumber, lfLearningRate );
		}
		// UXABC法
		else if (iAbcMethod == 14)
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount, iCrossOverNum, lfAlpha, lfBeta );
		}
		// REXを混ぜたハイブリッドABC法(提案手法3)
		else if (iAbcMethod == 15)
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount, iIntervalMinNum, iAbcUpperSearchNum, lfConvergenceParam, lfFitBound, lfFitAccuracy, iParentNumber, iChildrenNumber, iUpperEvalChildrenNumber, lfLearningRate );
		}
		// AREXを混ぜたハイブリッドABC法(提案手法4)
		else if (iAbcMethod == 16)
		{
			abc = new Abc( iGenerationNumber, iGensNumber, iGensVectorDimension, iSearchNum, iLimitCount, iIntervalMinNum, iAbcUpperSearchNum, lfConvergenceParam, lfFitBound, lfFitAccuracy, iParentNumber, iChildrenNumber, iUpperEvalChildrenNumber, lfLearningRate );
		}
	}

	/**
	 * <PRE>
	 *    メタヒューリスティック手法の初期設定を行います。
	 * </PRE>
	 */
	public void vInitialSet()
	{
		int i;
		if( iInverseSimulationMethod == 0 )
		{

		}
		else if( iInverseSimulationMethod == 1 )
		{

		}
		else if( iInverseSimulationMethod == 2 )
		{

		}
		else if( iInverseSimulationMethod == 3 )
		{

		}
		else if( iInverseSimulationMethod == 4 )
		{
			for( i = 0;i < pplfErArgument.length; i++ )
			{
				vSetOptimumData(i, pplfErArgument[i] );
			}
//			abc.vInitialSet();
		}
	}

	/**
	 * <PRE>
	 *    1ステップの逆シミュレーションを実行します。
	 * </PRE>
	 * @param iTimeStep			FUSEのシミュレーションタイムステップ
	 * @throws GenAlgException	遺伝的アルゴリズム計算エラー
	 * @throws IOException		ファイル出力エラー
	 */
	public void vImplement( int iTimeStep ) throws GenAlgException, IOException
	{
		int i;
		int iCount = 0;

		// シミュレーションを実行します。
		SimEngine.start( iTimeStep );

		System.out.println("結果出力");
		// 途中結果を出力します。
		if( iUpdateCount == 0 )	vOutput(2);
		else 					vOutput(1);

		for(;;)
		{
			// 3秒間メインループを停止し、すべてのエージェントの処理が終了するまで待ちます。
			try{
				Thread.sleep( 500 );
			}
			catch( InterruptedException ite ){

			}
			iCount = 0;
			for( i = 0;i < erDepartments.length; i++ )
			{
				if( erDepartments[i].isFinishAgentFlag() == true )
				{
					iCount++;
				}
			}
			if( iCount >= erDepartments.length ) break;
		}
		// シミュレーション実行結果から各種パラメータを取得します。
		for( i = 0;i < iGensNumber; i++ )
		{
			if( iEvaluationIndexCompMode == 1 )
			{
				// 全体平均値を設定します。
				vSetArgument( pplfErArgument[i], erDepartments[i] );
			}
			else if( iEvaluationIndexCompMode == 2 )
			{
				// 1シミュレーションの中の最大値を設定します。
				vSetArgumentMaxParam( pplfErArgument[i], erDepartments[i] );
			}
			else
			{
				// 設定されていないものを参照しています。
			}
			// 最適化手法にデータを設定します。
			vSetOptimumData( i, pplfErArgument[i] );
		}

		System.out.println("最適化手法の実行");
		// 仮で出力
//		vOutput();
//		vOutput(2);
		// ここで最適化手法を実行します。
		vImplementOptimize();
//		vOutput(1);

		// 逆シミュレーションの現在回数を更新します。
		iUpdateCount++;

		System.out.println("結果の取得");
		// メタヒューリスティック結果を更新します。
		vGetOptimumData();

		System.out.println("救急部門のパラメータを更新");
		// 更新されたパラメータを設定します。
		vSetErDepartmentData();

		//
		System.out.println( "終了" );

		// 現在までの時間を計測
		vGetCurrentTime();

	}

	/**
	 * <PRE>
	 *    終了処理を実行します。
	 * </PRE>
	 * @throws IOException	ファイル書き込み終了エラー
	 */
	public void vTerminate() throws IOException
	{
		erDepartments 			= null;
		iGensNumber 			= 0;
		iGenerationNumber 		= 0;
		iGensVectorDimension 	= 0;
		pflfObjectiveFunction	= null;
		pplfErArgument 			= null;
		csvWriteERData.vClose();
		csvWriteERConstraintFunctionValue.vClose();
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用のパラメータを設定します。
	 * </PRE>
	 * @param plfArg	設定するパラメータ
	 * @param erDepart	救急部門エージェントのインスタンス
	 */
	public void vSetArgument( double[] plfArg, ERDepartment erDepart )
	{
		// 部屋数の設定をします。
		plfArg[0] = erDepart.iGetConsultationRoomNum();
		plfArg[1] = erDepart.iGetOperationRoomNum();
		plfArg[2] = erDepart.iGetEmergencyRoomNum();
		plfArg[3] = erDepart.iGetObservationRoomNum();
		plfArg[4] = erDepart.iGetInjurySevereObservationRoomNum();
		plfArg[5] = erDepart.iGetIntensiveCareUnitRoomNum();
		plfArg[6] = erDepart.iGetHighCareUnitRoomNum();
		plfArg[7] = erDepart.iGetGeneralWardRoomNum();
		plfArg[8] = erDepart.iGetWaitingRoomNum();
		plfArg[9] = erDepart.iGetExaminationXRayRoomNum();
		plfArg[10] = erDepart.iGetExaminationCTRoomNum();
		plfArg[11] = erDepart.iGetExaminationMRIRoomNum();
		plfArg[12] = erDepart.iGetExaminationAngiographyRoomNum();
		plfArg[13] = erDepart.iGetExaminationFastRoomNum();

		// 部屋を構成する人員の人数を設定します。
		plfArg[14] = erDepart.iGetConsultationRoomDoctorNum();
		plfArg[15] = erDepart.iGetConsultationRoomNurseNum();
		plfArg[16] = erDepart.iGetOperationRoomDoctorNum();
		plfArg[17] = erDepart.iGetOperationRoomNurseNum();
		plfArg[18] = erDepart.iGetEmergencyRoomDoctorNum();
		plfArg[19] = erDepart.iGetEmergencyRoomNurseNum();
		plfArg[20] = erDepart.iGetEmergencyRoomClinicalEngineerNum();
		plfArg[21] = erDepart.iGetObservationRoomNurseNum();
		plfArg[22] = erDepart.iGetInjurySevereObservationRoomNurseNum();
		plfArg[23] = erDepart.iGetIntensiveCareUnitRoomDoctorNum();
		plfArg[24] = erDepart.iGetIntensiveCareUnitRoomNurseNum();
		plfArg[25] = erDepart.iGetHighCareUnitRoomDoctorNum();
		plfArg[26] = erDepart.iGetHighCareUnitRoomNurseNum();
		plfArg[27] = erDepart.iGetGeneralWardRoomDoctorNum();
		plfArg[28] = erDepart.iGetGeneralWardRoomNurseNum();
		plfArg[29] = erDepart.iGetWaitingRoomNurseNum();
		plfArg[30] = erDepart.iGetExaminationXRayRoomClinicalEngineerNum();
		plfArg[31] = erDepart.iGetExaminationCTRoomClinicalEngineerNum();
		plfArg[32] = erDepart.iGetExaminationMRIRoomClinicalEngineerNum();
		plfArg[33] = erDepart.iGetExaminationAnmgiographyRoomClinicalEngineerNum();
		plfArg[34] = erDepart.iGetExaminationFastRoomClinicalEngineerNum();
//		plfArg[35] = erDepart.lfGetAvgSurvivalProbability();
//		plfArg[36] = erDepart.iGetSurvivalNum()/(double)erDepart.iGetTotalPatientNum();
//		plfArg[37] = (double)(erDepart.iGetTotalPatientNum()-erDepart.iGetSurvivalNum())/(double)erDepart.iGetTotalPatientNum();
		plfArg[35] = erDepart.iGetTotalPatientAgentNum()/(erDepart.lfGetEndTime()/10);
		plfArg[36] = erDepart.iGetEdAdmittedAgentNum()/(erDepart.lfGetEndTime()/10);
		plfArg[37] = erDepart.iGetEdVentilatorsNum()/(erDepart.lfGetEndTime()/10);
//		plfArg[35] = erDepart.iGetTotalPatientAgentNum();
//		plfArg[36] = erDepart.iGetEdAdmittedAgentNum();
//		plfArg[37] = erDepart.iGetEdVentilatorsNum();
		plfArg[38] = erDepart.lfGetLongestAdmittedTime()/3600.0;
		plfArg[39] = erDepart.lfGetLastBedTime()/3600.0;
		plfArg[40] = erDepart.iGetWaitingRoomPatientNum()/(erDepart.lfGetEndTime()/10);
		plfArg[41] = erDepart.iGetTriageCategoryPatientNum(1)/(erDepart.lfGetEndTime()/10);
		plfArg[42] = erDepart.iGetTriageCategoryPatientNum(2)/(erDepart.lfGetEndTime()/10);
		plfArg[43] = erDepart.iGetTriageCategoryPatientNum(3)/(erDepart.lfGetEndTime()/10);
		plfArg[44] = erDepart.iGetTriageCategoryPatientNum(4)/(erDepart.lfGetEndTime()/10);
		plfArg[45] = erDepart.iGetTriageCategoryPatientNum(5)/(erDepart.lfGetEndTime()/10);
//		plfArg[40] = erDepart.iGetWaitingRoomPatientNum();
//		plfArg[41] = erDepart.iGetTriageCategoryPatientNum(1);
//		plfArg[42] = erDepart.iGetTriageCategoryPatientNum(2);
//		plfArg[43] = erDepart.iGetTriageCategoryPatientNum(3);
//		plfArg[44] = erDepart.iGetTriageCategoryPatientNum(4);
//		plfArg[45] = erDepart.iGetTriageCategoryPatientNum(5);
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用のパラメータを設定します。
	 *    こちらは評価指標が最大となるパラメータを参照します。
	 * </PRE>
	 * @param plfArg	設定するパラメータ
	 * @param erDepart	救急部門エージェントのインスタンス
	 */
	public void vSetArgumentMaxParam( double[] plfArg, ERDepartment erDepart )
	{

		int i;
		for(i = 0;i < plfArg.length; i++ )
		{
			// 部屋数の設定をします。
			// 部屋を構成する人員の人数を設定します。
			// 評価指標算出に必要なパラメータを設定します。
			plfArg[i] = erDepart.iGetMaxInvSimParam(i);
		}
	}

	/**
	 * <PRE>
	 *    評価指標のコールバック関数をインストールします。
	 * </PRE>
	 * @param interfaceObjFunc				評価関数のコールバック関数
	 * @param iEvaluationIndexModeData		使用するアルゴリズム
	 * 										1 GA
	 * 										2 RCGA
	 * 										3 PSO
	 * 										4 ABC
	 */
	public void vInstallCallbackFunction( ObjectiveFunctionInterface interfaceObjFunc, int iEvaluationIndexModeData )
	{
		// ＧＡを適用します。
		if( iInverseSimulationMethod == 1 )
		{
		}
		// 実数値ＧＡを適用します。
		else if( iInverseSimulationMethod == 2 )
		{

		}
		// 粒子群最適化法を適用します。
		else if( iInverseSimulationMethod == 3 )
		{
		}
		// 人工蜂コロニー法を適用します。
		else if( iInverseSimulationMethod == 4 )
		{
			abc.vSetConstraintFunction( interfaceObjFunc );
			abc.vSetConstarintFunctionMode( iEvaluationIndexModeData );
//			abc.vSetConstarintFunctionMode( 102 );
//			abc.vSetConstarintFunctionMode( 103 );
		}
		// それ以外の手法を適用したので異常終了とします。
		else
		{

		}
	}

	/**
	 * <PRE>
	 *   評価指標のコールバック関数のアンインストールを実行します。
	 * </PRE>
	 */
	public void vUnInstallCallbackFunction()
	{
		// ＧＡの場合。
		if( iInverseSimulationMethod == 1 )
		{
		}
		// 実数値ＧＡの場合。
		else if( iInverseSimulationMethod == 2 )
		{

		}
		// 粒子群最適化法の場合。
		else if( iInverseSimulationMethod == 3 )
		{
		}
		// 人工蜂コロニー法の場合
		else if( iInverseSimulationMethod == 4 )
		{
			abc.vReleaseCallbackConstraintFunction();
		}
		// それ以外の手法を適用したので異常終了とします。
		else
		{

		}
	}

	/**
	 * <PRE>
	 *   評価指標の制約条件のコールバック関数のアンインストールを実行します。
	 * </PRE>
	 */
	public void vUnInstallCallbackCondition()
	{
		// ＧＡの場合。
		if( iInverseSimulationMethod == 1 )
		{
		}
		// 実数値ＧＡの場合。
		else if( iInverseSimulationMethod == 2 )
		{

		}
		// 粒子群最適化法の場合。
		else if( iInverseSimulationMethod == 3 )
		{
		}
		// 人工蜂コロニー法の場合
		else if( iInverseSimulationMethod == 4 )
		{
			abc.vReleaseCallbackConstraintCondition();
		}
		// それ以外の手法を適用したので異常終了とします。
		else
		{

		}
	}

	/**
	 * <PRE>
	 *    最適化手法を実行のためにデータを設定します。
	 * </PRE>
	 * @param iLoc				// 最適化用の遺伝子
	 * @param plfErArgument		// 最適化用の遺伝子の構成パラメータ
	 */
	public void vSetOptimumData( int iLoc, double plfErArgument[] )
	{
		int i;
		if( iInverseSimulationMethod == 1 )
		{
			for( i = 0;i < plfErArgument.length; i++ )
			{
				ga.vSetGenData( iLoc, i, (long)plfErArgument[i] );
			}
		}
		// 実数値ＧＡを適用します。
		else if( iInverseSimulationMethod == 2 )
		{
			for( i = 0;i < plfErArgument.length; i++ )
			{
//				rcga.vSetRcGaData( iLoc, i, plfErArgument[i] );
			}
		}
		// 粒子群最適化法を適用します。
		else if( iInverseSimulationMethod == 3 )
		{
			for( i = 0;i < plfErArgument.length; i++ )
			{
//				pso.vSetPsoData( iLoc, i, (long)plfErArgument[i] );
			}
		}
		// 人工蜂コロニー法を適用します。
		else if( iInverseSimulationMethod == 4 )
		{
			for( i = 0;i < plfErArgument.length; i++ )
			{
				abc.vSetAbcData( iLoc, i, plfErArgument[i] );
			}
		}
		else
		{

		}
	}

	/**
	 * <PRE>
	 *    最適化手法を実行のためにデータを設定します。
	 * </PRE>
	 */
	public void vGetOptimumData()
	{
		int i,j;
		if( iInverseSimulationMethod == 1 )
		{
			for( i = 0;i < pplfErArgument.length; i++ )
			{
				for( j = 0;j < pplfErArgument[i].length; j++ )
				{
					pplfErArgument[i][j] = ga.lGetGensData( i, j );
				}
			}
		}
		// 実数値ＧＡを適用します。
		else if( iInverseSimulationMethod == 2 )
		{
			for( i = 0;i < pplfErArgument.length; i++ )
			{
				for( j = 0;j < pplfErArgument[i].length; j++ )
				{
//					pplfErArgument[i][j] = rcga.lGetGensData( i, j );
				}
			}
		}
		// 粒子群最適化法を適用します。
		else if( iInverseSimulationMethod == 3 )
		{
			for( i = 0;i < pplfErArgument.length; i++ )
			{
				for( j = 0;j < pplfErArgument[i].length; j++ )
				{
//					pplfErArgument[i][j] = pso.lGetGensData( i, j );
				}
			}
		}
		// 人工蜂コロニー法を適用します。
		else if( iInverseSimulationMethod == 4 )
		{
			for( i = 0;i < pplfErArgument.length; i++ )
			{
				for( j = 0;j < pplfErArgument[i].length; j++ )
				{
					pplfErArgument[i][j] = abc.lfGetBeeData( i, j );
				}
			}
		}
		else
		{

		}
	}

	/**
	 * <PRE>
	 *   逆シミュレーションを実行します。
	 * </PRE>
	 * @throws GenAlgException 遺伝的アルゴリズム計算エラー
	 */
	public void vImplementOptimize() throws GenAlgException
	{
		// 遺伝的アルゴリズムを実行します。
		if( iInverseSimulationMethod == 1 )
		{
			vGeneticAlgorithm();
		}
		// 実数値ＧＡを適用します。
		else if( iInverseSimulationMethod == 2 )
		{
			vRealCodedGeneticAlgorithm();
		}
		// 粒子群最適化法を適用します。
		else if( iInverseSimulationMethod == 3 )
		{
			vParticleSwarmOptimization();
		}
		// 人工蜂コロニー法を適用します。
		else if( iInverseSimulationMethod == 4 )
		{
			vArtificialBeeColonyMethod();
		}
		// それ以外の手法を適用したので異常終了とします。
		else
		{

		}
	}

	/**
	 * <PRE>
	 *    遺伝的アルゴリズムを実行します。
	 * </PRE>
	 * @throws GenAlgException	遺伝的アルゴリズム計算エラー
	 */
	private void vGeneticAlgorithm() throws GenAlgException
	{
		// 淘汰を実行します。
		if( iGaMethod == 1 )
		{
			// ルーレット選択
			ga.lGensSelectRolette();
		}
		else if( iGaMethod == 2 )
		{
			// ランキング法
			ga.lGensSelectRanking();
		}
		else if( iGaMethod == 3 )
		{
			// トーナメント法
			ga.lGensSelectTournament( iGaSelectNumber );
		}
		// 突然変異を実行します。
		ga.lGensMutation( lfGaMutationProb );
	}

	/**
	 * <PRE>
	 *    粒子群最適化法を実行します。
	 * </PRE>
	 */
	public void vParticleSwarmOptimization()
	{
		// PSOを実行します。
		if( iPsoMethod == 1 )
		{
			// 通常法
		}
		else if( iPsoMethod == 2 )
		{
			// Inertia Weights Approach法
		}
		else if( iPsoMethod == 3 )
		{
			// Constriction Factor Approach法
		}
		else if( iPsoMethod == 4 )
		{
			// Constriction Factor Approach法
		}
	}

	/**
	 * <PRE>
	 *    人工蜂コロニー法を実行します。
	 * </PRE>
	 */
	private void vArtificialBeeColonyMethod()
	{
		// オリジナルArtificial Bee Colony Method(2005)
		if( iAbcMethod == 1 )
		{
			abc.vAbc();
		}
		// 変形Artificial Bee Colony Method (2011)
		else if( iAbcMethod == 2 )
		{
			abc.vModifiedAbc(iUpdateCount);
		}
		// 交叉を用いたArtificial Bee Colony Method (2013)
		else if( iAbcMethod == 3 )
		{
			abc.vCbAbc();
		}
		//　GbestABC法
		else if( iAbcMethod == 4 )
		{
			abc.vGAbc();
		}
		//　Memetic ABC法
		else if( iAbcMethod == 5 )
		{
			abc.vMeAbc(iUpdateCount);
		}
		//　Randamized Memetic ABC Algorithm(2013)
		else if( iAbcMethod == 6 )
		{
			abc.vRMAbc(iUpdateCount);
		}
		// UNDXを混ぜたハイブリッドABC法(提案手法)
		else if( iAbcMethod == 7 )
		{
			abc.vUndxAbc();
		}
		// REXを混ぜたハイブリッドABC法(提案手法3)
		else if( iAbcMethod == 8 )
		{
			abc.vRexAbc();
		}
		// AREXを混ぜたハイブリッドABC法(提案手法4)
		else if(iAbcMethod == 9 )
		{
			abc.vARexAbc();
		}
		// HJABC法
		else if (iAbcMethod == 10)
		{
			abc.vHJAbc(iUpdateCount);
		}
		// ACABC法
		else if (iAbcMethod == 11 )
		{
			abc.vACAbc();
		}
		// BestSoFarABC法
		else if (iAbcMethod == 12)
		{
			abc.vBFAbc(iUpdateCount);
		}
		// PABC法
		else if (iAbcMethod == 13)
		{
			abc.vPAbc(iUpdateCount);
		}
		// UXABC法
		else if (iAbcMethod == 14)
		{
			abc.vUXAbc();
		}
		// REXを混ぜたハイブリッドABC法(提案手法3)
		else if (iAbcMethod == 15)
		{
			abc.vBFRexAbc();
		}
		// AREXを混ぜたハイブリッドABC法(提案手法4)
		else if (iAbcMethod == 16)
		{
			abc.vBFARexAbc();
		}
	}

	/**
	 * <PRE>
	 *    実数値遺伝的アルゴリズムを実行します。
	 * </PRE>
	 */
	private void vRealCodedGeneticAlgorithm()
	{
	// 実数値GA法を実行します。
		if( iRcGaMethod == 1 )
		{
			// UNDX
		}
		else if( iRcGaMethod == 2 )
		{
			// REX
		}
		else if( iRcGaMethod == 3 )
		{
			// REC-star
		}
		else if( iRcGaMethod == 4 )
		{
			// AREX
		}
	}

	/**
	 * <PRE>
	 *    更新したパラメータを救急部門エージェントに設定します。
	 * </PRE>
	 * @throws IOException ファイル書き込みエラー
	 */
	public void vSetErDepartmentData() throws IOException
	{
		int i,j;
		Runtime rc = Runtime.getRuntime();

		// 終了処理を実行します。
		for( i = 0;i < erDepartments.length; i++ )
		{
			// 次のシミュレーション実行のためにいったん終了処理を実行します。
			erDepartments[i].vTerminate();
			// 逆シミュレーション用に初期化を実施。
			erDepartments[i].vSetLastBedTime(0.0);
			erDepartments[i].vSetLongestAdmittedTime(0.0);
			erDepartments[i].vSetWaitingRoomPatientNum(0);
			for( j = 0;j < 5; j++ )
				erDepartments[i].vSetEdAdmittedPatientTriageCategoryNum(j,0);
			erDepartments[i].vSetEdTotalPatientNum(0);
			erDepartments[i].vSetEdTotalAdmittedPatientNum(0);
			erDepartments[i].vSetEdAdmittedAgentNum(0);
			erDepartments[i].vSetEdVentilatorsNum(0);
			for( j =  0; j < pplfErArgument[i].length; j++ )
			{
				erDepartments[i].vSetMaxInvSimParam(j, 0.0 );
			}
			erDepartments[i].vSetMaxNedocs( -Double.MAX_VALUE );
			erDepartments[i].vSetMaxEdWorkScore( -Double.MAX_VALUE );
			erDepartments[i].vSetMaxEdwin( -Double.MAX_VALUE );
			erDepartments[i].vSetMaxInvSimParam(7, 1.0 );
		}
		// いったんシミュレーションエンジンに登録しているエージェントをすべて解放します。
		SimEngine.clearAgents();
		rc.gc();

		// 更新処理を実行します。
		for( i = 0;i < pplfErArgument.length; i++ )
		{
			// 更新したパラメータを設定します。
			erDepartments[i].vSetParameter( pplfErArgument[i] );

			// シミュレーション終了時間を設定します。
			erDepartments[i].vSetSimulationEndTime( iSimulationEndTime );

			// 救急部門を構成する部屋数を設定します。
			erDepartments[i].vSetEmergencyDepartment((int)pplfErArgument[i][0],
					(int)pplfErArgument[i][1], (int)pplfErArgument[i][2], (int)pplfErArgument[i][3], (int)pplfErArgument[i][4],
					(int)pplfErArgument[i][5], (int)pplfErArgument[i][6], (int)pplfErArgument[i][7], (int)pplfErArgument[i][8],
					(int)pplfErArgument[i][9], (int)pplfErArgument[i][10], (int)pplfErArgument[i][11], (int)pplfErArgument[i][12],
					(int)pplfErArgument[i][13]);

			// 救急部門の各部屋を構成するエージェント数を設定します。
			erDepartments[i].vSetEmergencyDepartmentAgents((int)pplfErArgument[i][14],
					(int)pplfErArgument[i][15], (int)pplfErArgument[i][16], (int)pplfErArgument[i][17], (int)pplfErArgument[i][18],
					(int)pplfErArgument[i][19], (int)pplfErArgument[i][20], (int)pplfErArgument[i][21], (int)pplfErArgument[i][22],
					(int)pplfErArgument[i][23], (int)pplfErArgument[i][24], (int)pplfErArgument[i][25], (int)pplfErArgument[i][26],
					(int)pplfErArgument[i][27], (int)pplfErArgument[i][28], (int)pplfErArgument[i][29], (int)pplfErArgument[i][30],
					(int)pplfErArgument[i][31], (int)pplfErArgument[i][32], (int)pplfErArgument[i][33], (int)pplfErArgument[i][34]);

			// 救急部門の初期化を実行します。
			erDepartments[i].vInitialize( SimEngine, SimEnv, strConsultationRoomPath,
										  strOperationRoomPath, strEmergencyRoomPath, strObservationRoomPath,
										  strSevereInjuryObservationRoomPath, strIntensiveCareUnitPath,
										  strHighCareUnitPath, strGeneralWardPath, strWaitingRoomPath,
										  strXRayRoomPath, strCTRoomPath, strMRIRoomPath, strAngiographyRoomPath,
										  strFastRoomPath, lfPatientPepole, iRandomMode, iInverseSimFlag, iFileWriteMode, iPatientArrivalMode, SimRandom );
			// ログ出力を設定します。
			erDepartments[i].vSetAllLog( SimLog );
			erDepartments[i].vSetCriticalSection( invEngineCriticalSection );
		}
	}

	/**
	 * <PRE>
	 *   結果出力をします。
	 *   ファイルへも出力します。
	 * </PRE>
	 * @param iOutFlag 結果出力用フラグ 0 設定パラメータ配列
	 * 									1 ABC法のデータ配列
	 * 									2 シミュレーション結果
	 * @throws IOException	ファイルの書き込みエラー
	 */
	public void vOutput( int iOutFlag ) throws IOException
	{
		int i,j;
		String strData = "";
		double lfRes = 0.0;
		double lfMax = -Double.MAX_VALUE;
		if( iOutFlag == 0 )
		{
			for( i = 0; i < pplfErArgument.length; i++ )
			{
				for( j = 0;j < pplfErArgument[i].length; j++ )
				{
					strData += pplfErArgument[i][j] + "," ;
					System.out.print( pplfErArgument[i][j] + "," );
				}
				strData += abc.strOutputSingleConstraintFunction(i);
				strData += "\n";
				System.out.println(abc.strOutputSingleConstraintFunction(i) + "");
			}
			csvWriteERData.vWrite( strData );
			lfRes = abc.lfOutputGlobalMinAbcDataConstFuncValue();
			strData = abc.strOutputGlobalMinAbcData();
			System.out.println( strData + Double.toString( lfRes ) );
			csvWriteERConstraintFunctionValue.vWrite( strData + Double.toString( lfRes ) );
		}
		else if( iOutFlag == 1 )
		{
			for( i = 0; i < pplfErArgument.length; i++ )
			{
				for( j = 0;j < pplfErArgument[i].length; j++ )
				{
					strData += abc.lfGetBeeData(i, j) + "," ;
					System.out.print( abc.lfGetBeeData(i, j) + "," );
				}
				strData += abc.strOutputSingleConstraintFunction(i);
				strData += "\n";
				System.out.println(abc.strOutputSingleConstraintFunction(i) + "");
			}
			csvWriteERData.vWrite( strData );
			lfRes = abc.lfOutputGlobalMinAbcDataConstFuncValue();
			strData = abc.strOutputGlobalMinAbcData();
			System.out.println( strData + Double.toString( lfRes ) );
			csvWriteERConstraintFunctionValue.vWrite( strData + Double.toString( lfRes ) );
		}
		else
		{
			// 部屋数の設定をします。
			for( i = 0;i < erDepartments.length; i++ )
			{
				System.out.print( erDepartments[i].iGetConsultationRoomNum() + "," );
				System.out.print( erDepartments[i].iGetOperationRoomNum() + "," );
				System.out.print( erDepartments[i].iGetEmergencyRoomNum() + ","  );
				System.out.print( erDepartments[i].iGetObservationRoomNum() + ","  );
				System.out.print( erDepartments[i].iGetInjurySevereObservationRoomNum() + "," );
				System.out.print( erDepartments[i].iGetIntensiveCareUnitRoomNum() + "," );
				System.out.print( erDepartments[i].iGetHighCareUnitRoomNum() + "," );
				System.out.print( erDepartments[i].iGetGeneralWardRoomNum() + "," );
				System.out.print( erDepartments[i].iGetWaitingRoomNum() + "," );
				System.out.print( erDepartments[i].iGetExaminationXRayRoomNum() + "," );
				System.out.print( erDepartments[i].iGetExaminationCTRoomNum() + "," );
				System.out.print( erDepartments[i].iGetExaminationMRIRoomNum() + "," );
				System.out.print( erDepartments[i].iGetExaminationAngiographyRoomNum() + "," );
				System.out.print( erDepartments[i].iGetExaminationFastRoomNum() + "," );

				strData += erDepartments[i].iGetConsultationRoomNum() + ",";
				strData += erDepartments[i].iGetOperationRoomNum() + "," ;
				strData += erDepartments[i].iGetEmergencyRoomNum() + ","  ;
				strData += erDepartments[i].iGetObservationRoomNum() + ","  ;
				strData += erDepartments[i].iGetInjurySevereObservationRoomNum() + "," ;
				strData += erDepartments[i].iGetIntensiveCareUnitRoomNum() + "," ;
				strData += erDepartments[i].iGetHighCareUnitRoomNum() + "," ;
				strData += erDepartments[i].iGetGeneralWardRoomNum() + "," ;
				strData += erDepartments[i].iGetWaitingRoomNum() + "," ;
				strData += erDepartments[i].iGetExaminationXRayRoomNum() + "," ;
				strData += erDepartments[i].iGetExaminationCTRoomNum() + "," ;
				strData += erDepartments[i].iGetExaminationMRIRoomNum() + "," ;
				strData += erDepartments[i].iGetExaminationAngiographyRoomNum() + "," ;
				strData += erDepartments[i].iGetExaminationFastRoomNum() + "," ;

			// 部屋を構成する人員の人数を設定します。
				System.out.print( erDepartments[i].iGetConsultationRoomDoctorNum() + "," );
				System.out.print( erDepartments[i].iGetConsultationRoomNurseNum() + "," );
				System.out.print( erDepartments[i].iGetOperationRoomDoctorNum() + "," );
				System.out.print( erDepartments[i].iGetOperationRoomNurseNum() + "," );
				System.out.print( erDepartments[i].iGetEmergencyRoomDoctorNum() + "," );
				System.out.print( erDepartments[i].iGetEmergencyRoomNurseNum() + "," );
				System.out.print( erDepartments[i].iGetEmergencyRoomClinicalEngineerNum() + "," );
				System.out.print( erDepartments[i].iGetObservationRoomNurseNum() + "," );
				System.out.print( erDepartments[i].iGetInjurySevereObservationRoomNurseNum() + "," );
				System.out.print( erDepartments[i].iGetIntensiveCareUnitRoomDoctorNum() + "," );
				System.out.print( erDepartments[i].iGetIntensiveCareUnitRoomNurseNum() + "," );
				System.out.print( erDepartments[i].iGetHighCareUnitRoomDoctorNum() + "," );
				System.out.print( erDepartments[i].iGetHighCareUnitRoomNurseNum() + "," );
				System.out.print( erDepartments[i].iGetGeneralWardRoomDoctorNum() + "," );
				System.out.print( erDepartments[i].iGetGeneralWardRoomNurseNum() + "," );
				System.out.print( erDepartments[i].iGetWaitingRoomNurseNum() + "," );
				System.out.print( erDepartments[i].iGetExaminationXRayRoomClinicalEngineerNum() + "," );
				System.out.print( erDepartments[i].iGetExaminationCTRoomClinicalEngineerNum() + "," );
				System.out.print( erDepartments[i].iGetExaminationMRIRoomClinicalEngineerNum() + "," );
				System.out.print( erDepartments[i].iGetExaminationAnmgiographyRoomClinicalEngineerNum() + "," );
				System.out.print( erDepartments[i].iGetExaminationFastRoomClinicalEngineerNum() + "," );
//				System.out.print( erDepartments[i].lfGetAvgSurvivalProbability() + "," );
//				System.out.print( erDepartments[i].iGetSurvivalNum()/(double)erDepart.iGetTotalPatientNum() + "," );
//				System.out.print( erDepartments[i].iGetTotalPatientNum()-erDepart.iGetSurvivalNum())/(double)erDepart.iGetTotalPatientNum() + "," );
				System.out.print( erDepartments[i].iGetTotalPatientAgentNum()/(erDepartments[i].lfGetEndTime()/10) + "," );
				System.out.print( erDepartments[i].iGetEdAdmittedAgentNum()/(erDepartments[i].lfGetEndTime()/10) + "," );
				System.out.print( erDepartments[i].iGetEdVentilatorsNum()/(erDepartments[i].lfGetEndTime()/10) + "," );
//				System.out.print( erDepartments[i].iGetTotalPatientAgentNum() + "," );
//				System.out.print( erDepartments[i].iGetEdAdmittedAgentNum() + "," );
//				System.out.print( erDepartments[i].iGetEdVentilatorsNum() + "," );
				System.out.print( erDepartments[i].lfGetLongestAdmittedTime()/3600.0 + "," );
				System.out.print( erDepartments[i].lfGetLastBedTime()/3600.0 + "," );
				System.out.print( erDepartments[i].iGetWaitingRoomPatientNum()/(erDepartments[i].lfGetEndTime()/10) + "," );
				System.out.print( erDepartments[i].iGetTriageCategoryPatientNum(1)/(erDepartments[i].lfGetEndTime()/10) + "," );
				System.out.print( erDepartments[i].iGetTriageCategoryPatientNum(2)/(erDepartments[i].lfGetEndTime()/10) + "," );
				System.out.print( erDepartments[i].iGetTriageCategoryPatientNum(3)/(erDepartments[i].lfGetEndTime()/10) + "," );
				System.out.print( erDepartments[i].iGetTriageCategoryPatientNum(4)/(erDepartments[i].lfGetEndTime()/10) + "," );
				System.out.print( erDepartments[i].iGetTriageCategoryPatientNum(5)/(erDepartments[i].lfGetEndTime()/10) + "," );
//				System.out.print( erDepartments[i].iGetWaitingRoomPatientNum() + "," );
//				System.out.print( erDepartments[i].iGetTriageCategoryPatientNum(1) + "," );
//				System.out.print( erDepartments[i].iGetTriageCategoryPatientNum(2) + "," );
//				System.out.print( erDepartments[i].iGetTriageCategoryPatientNum(3) + "," );
//				System.out.print( erDepartments[i].iGetTriageCategoryPatientNum(4) + "," );
//				System.out.print( erDepartments[i].iGetTriageCategoryPatientNum(5) + "," );
				if( iEvaluationIndexMode == 101 ) System.out.print( erDepartments[i].lfGetMaxNedocs()/erDepartments[i].lfGetTotalSimulationStep() + "," );
				else if( iEvaluationIndexMode == 102 ) System.out.print( erDepartments[i].lfGetMaxEdWorkScore()/erDepartments[i].lfGetTotalSimulationStep() + "," );
				else if( iEvaluationIndexMode == 103 ) System.out.print( erDepartments[i].lfGetMaxEdwin()/erDepartments[i].lfGetTotalSimulationStep() + "," );
				System.out.print( "\n" );

				strData += erDepartments[i].iGetConsultationRoomDoctorNum() + "," ;
				strData += erDepartments[i].iGetConsultationRoomNurseNum() + "," ;
				strData += erDepartments[i].iGetOperationRoomDoctorNum() + "," ;
				strData += erDepartments[i].iGetOperationRoomNurseNum() + "," ;
				strData += erDepartments[i].iGetEmergencyRoomDoctorNum() + "," ;
				strData += erDepartments[i].iGetEmergencyRoomNurseNum() + "," ;
				strData += erDepartments[i].iGetEmergencyRoomClinicalEngineerNum() + "," ;
				strData += erDepartments[i].iGetObservationRoomNurseNum() + "," ;
				strData += erDepartments[i].iGetInjurySevereObservationRoomNurseNum() + "," ;
				strData += erDepartments[i].iGetIntensiveCareUnitRoomDoctorNum() + "," ;
				strData += erDepartments[i].iGetIntensiveCareUnitRoomNurseNum() + "," ;
				strData += erDepartments[i].iGetHighCareUnitRoomDoctorNum() + "," ;
				strData += erDepartments[i].iGetHighCareUnitRoomNurseNum() + "," ;
				strData += erDepartments[i].iGetGeneralWardRoomDoctorNum() + "," ;
				strData += erDepartments[i].iGetGeneralWardRoomNurseNum() + "," ;
				strData += erDepartments[i].iGetWaitingRoomNurseNum() + "," ;
				strData += erDepartments[i].iGetExaminationXRayRoomClinicalEngineerNum() + "," ;
				strData += erDepartments[i].iGetExaminationCTRoomClinicalEngineerNum() + "," ;
				strData += erDepartments[i].iGetExaminationMRIRoomClinicalEngineerNum() + "," ;
				strData += erDepartments[i].iGetExaminationAnmgiographyRoomClinicalEngineerNum() + "," ;
				strData += erDepartments[i].iGetExaminationFastRoomClinicalEngineerNum() + "," ;
//				strData += erDepartments[i].lfGetAvgSurvivalProbability() + "," ;
//				strData += erDepartments[i].iGetSurvivalNum()/(double)erDepart.iGetTotalPatientNum() + "," ;
//				strData += erDepartments[i].iGetTotalPatientNum()-erDepart.iGetSurvivalNum())/(double)erDepart.iGetTotalPatientNum() + "," ;
				strData += erDepartments[i].iGetTotalPatientAgentNum()/(erDepartments[i].lfGetEndTime()/10) + "," ;
				strData += erDepartments[i].iGetEdAdmittedAgentNum()/(erDepartments[i].lfGetEndTime()/10) + "," ;
				strData += erDepartments[i].iGetEdVentilatorsNum()/(erDepartments[i].lfGetEndTime()/10) + "," ;
//				strData += erDepartments[i].iGetTotalPatientAgentNum() + "," ;
//				strData += erDepartments[i].iGetEdAdmittedAgentNum() + "," ;
//				strData += erDepartments[i].iGetEdVentilatorsNum() + "," ;
				strData += erDepartments[i].lfGetLongestAdmittedTime()/3600.0 + "," ;
				strData += erDepartments[i].lfGetLastBedTime()/3600.0 + "," ;
				strData += erDepartments[i].iGetWaitingRoomPatientNum()/(erDepartments[i].lfGetEndTime()/10) + "," ;
				strData += erDepartments[i].iGetTriageCategoryPatientNum(1)/(erDepartments[i].lfGetEndTime()/10) + "," ;
				strData += erDepartments[i].iGetTriageCategoryPatientNum(2)/(erDepartments[i].lfGetEndTime()/10) + "," ;
				strData += erDepartments[i].iGetTriageCategoryPatientNum(3)/(erDepartments[i].lfGetEndTime()/10) + "," ;
				strData += erDepartments[i].iGetTriageCategoryPatientNum(4)/(erDepartments[i].lfGetEndTime()/10) + "," ;
				strData += erDepartments[i].iGetTriageCategoryPatientNum(5)/(erDepartments[i].lfGetEndTime()/10) + "," ;
//				strData += erDepartments[i].iGetWaitingRoomPatientNum() + "," ;
//				strData += erDepartments[i].iGetTriageCategoryPatientNum(1) + "," ;
//				strData += erDepartments[i].iGetTriageCategoryPatientNum(2) + "," ;
//				strData += erDepartments[i].iGetTriageCategoryPatientNum(3) + "," ;
//				strData += erDepartments[i].iGetTriageCategoryPatientNum(4) + "," ;
//				strData += erDepartments[i].iGetTriageCategoryPatientNum(5) + "," ;
				if( iEvaluationIndexMode == 101 ) strData += erDepartments[i].lfGetMaxNedocs()/erDepartments[i].lfGetTotalSimulationStep() + "," ;
				else if( iEvaluationIndexMode == 102 ) strData += erDepartments[i].lfGetMaxEdWorkScore()/erDepartments[i].lfGetTotalSimulationStep() + "," ;
				else if( iEvaluationIndexMode == 103 ) strData += erDepartments[i].lfGetMaxEdwin()/erDepartments[i].lfGetTotalSimulationStep() + "," ;
				strData += "\n";

				if( iEvaluationIndexMode == 101 )
				{
					lfRes = erDepartments[i].lfGetMaxNedocs()/erDepartments[i].lfGetTotalSimulationStep();
					if( lfRes > lfMax ) lfMax = lfRes;
				}
				else if( iEvaluationIndexMode == 102 )
				{
					lfRes = erDepartments[i].lfGetMaxEdWorkScore()/erDepartments[i].lfGetTotalSimulationStep();
					if( lfRes > lfMax ) lfMax = lfRes;
				}
				else if( iEvaluationIndexMode == 103 )
				{
					lfRes = erDepartments[i].lfGetMaxEdwin()/erDepartments[i].lfGetTotalSimulationStep();
					if( lfRes > lfMax ) lfMax = lfRes;
				}

			}
			csvWriteERData.vWrite( strData );
			System.out.println( strData + Double.toString( lfRes ) );
			csvWriteERConstraintFunctionValue.vWrite( strData + Double.toString( lfRes ) );
		}
	}

	/**
	 * <PRE>
	 *   初期起動時の結果を出力します。
	 *   ファイルにも書き込みます。
	 * </PRE>
	 * @throws IOException ファイル書き込みエラー
	 */
	public void vInitialOutput() throws IOException
	{
		int i,j;
		String strData = "";
		double lfRes = 0.0;
		for( i = 0; i < pplfErArgument.length; i++ )
		{
			for( j = 0;j < pplfErArgument[i].length; j++ )
			{
				strData += pplfErArgument[i][j] + "," ;
				System.out.print( pplfErArgument[i][j] + "," );
			}
			strData += "\n";
			System.out.println("");
		}
		csvWriteERData.vWrite( strData );
	}

	/**
	 * <PRE>
	 *    制約条件を設定します。
	 *    コールバック関数です。
	 * </PRE>
	 * @param interfaceConstraintCondition	制約条件を記述したコールバック関数のインターフェース
	 */
	public void vInstallCallbackCondition( ConstraintConditionInterface interfaceConstraintCondition )
	{
		// ＧＡを適用します。
		if( iInverseSimulationMethod == 1 )
		{
		}
		// 実数値ＧＡを適用します。
		else if( iInverseSimulationMethod == 2 )
		{

		}
		// 粒子群最適化法を適用します。
		else if( iInverseSimulationMethod == 3 )
		{
		}
		// 人工蜂コロニー法を適用します。
		else if( iInverseSimulationMethod == 4 )
		{
			abc.vSetConstraintCondition( interfaceConstraintCondition );
//			abc.vSetConstarintConditionMode( 3 );
			abc.vSetConstarintConditionMode( 4 );
		}
		// それ以外の手法を適用したので異常終了とします。
		else
		{

		}
	}

	/**
	 * <PRE>
	 *    評価指標のモデルを設定します。
	 * </PRE>
	 * @param iMode	評価指標のモード
	 */
	public void vSetEvaluationIndexMode( int iMode )
	{
		iEvaluationIndexMode = iMode;
	}

	public void vSetEvaluationIndexCompMode(int iEvaluationIndexCompModeData )
	{
		// TODO 自動生成されたメソッド・スタブ
		iEvaluationIndexCompMode = iEvaluationIndexCompModeData;

	}

	/**
	 * <PRE>
	 *    逆シミュレーション計測用。開始時間を取得します。
	 *    ミリ秒単位で取得。
	 *    開始時間が初期化されていない場合は取得しません。
	 * </PRE>
	 */
	public void vGetStartTime()
	{
		if( lStartTime == 0.0 )
		{
			lStartTime = System.currentTimeMillis();
			alElapesdTime.add( lStartTime );
		}
	}

	/**
	 * <PRE>
	 *    逆シミュレーション計測用。終了時刻を取得します。
	 *    ミリ秒単位で取得。
	 *    開始時間が設定されている場合に取得します。
	 * </PRE>
	 */
	public void vGetEndTime()
	{
		if( lEndTime == 0.0 && lStartTime > 0.0 )
		{
			lEndTime = System.currentTimeMillis();
			alElapesdTime.add( lEndTime-lStartTime );
		}
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用。開始から現在までにかかった時間を取得します。
	 *    ミリ秒単位で取得。
	 * </PRE>
	 */
	public void vGetCurrentTime()
	{
		if( lEndTime > 0.0 )
			alElapesdTime.add(lEndTime-lStartTime);
		else
			alElapesdTime.add(System.currentTimeMillis()-lStartTime);
	}

	/**
	 * <PRE>
	 *   現在までに取得した時間を出力します。
	 * </PRE>
	 */
	public void vOutputElapsedTime()
	{
		int i;
		// 世代ごとに経過時間を出力します。
		for( i = 0;i < alElapesdTime.size(); i++ )
			System.out.println(alElapesdTime.get(i));
	}
}
