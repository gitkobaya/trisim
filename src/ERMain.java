/**
 * @file ERMain.java
 * @brief main関数を定義しています。
 *        Trisimの実行するためのソースコードを記載しています。<br>
 *
 *        Trisimは3パターンモードがあり、コンソールモード、guiモード、逆シミュレーションモードがあります。<br>
 *        コマンドラインオプションに指定しないで実行すると、guiモードで起動します。
 *        gui以外はオプションの-modeで0を指定すると通常シミュレーションモード
 *        2を指定すると逆シミュレーションモードとして動作します。
 *
 * @date  2017/08/18
 * @author kobayashi
 */

import inverse.InverseSimulationEngine;
import inverse.optimization.constraintcondition.ConstraintCondition;
import inverse.optimization.constraintcondition.ConstraintConditionInterface;
import inverse.optimization.ga.GenAlgException;
import inverse.optimization.objectivefunction.ObjectiveFunction;
import inverse.optimization.objectivefunction.ObjectiveFunctionInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.lang.InterruptedException;

import jp.ac.nihon_u.cit.su.furulab.fuse.Environment;
import jp.ac.nihon_u.cit.su.furulab.fuse.NodeManager;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.examples.AltitudeColor;
import jp.ac.nihon_u.cit.su.furulab.fuse.examples.FusePanelSimpleMesh;
import jp.ac.nihon_u.cit.su.furulab.fuse.examples.FusePanelSimpleMesh3D;
import jp.ac.nihon_u.cit.su.furulab.fuse.examples.KeyAndMouseListner2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.examples.KeyAndMouseListner3D;
import jp.ac.nihon_u.cit.su.furulab.fuse.examples.SimpleMeshGeometry;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FuseControler;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FuseWindow;
import triage.ERDepartment;
import triage.ERDepartmentArrivalPatient;
import triage.ERDepartmentDraw2D;
import triage.ERTriageDebugWindowKeyAndMouseListner;
import triage.agent.ERClinicalEngineerAgent;
import triage.agent.ERDoctorAgent;
import triage.agent.ERDoctorAgentException;
import triage.agent.ERNurseAgent;
import triage.agent.ERPatientAgent;
import triage.agent.draw.ERClinicalEngineerAgentDraw2D;
import triage.agent.draw.ERDoctorAgentDraw2D;
import triage.agent.draw.ERNurseAgentDraw2D;
import triage.agent.draw.ERPatientAgentDraw2D;
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
import triage.room.ERSevereInjuryObservationRoom;
import triage.room.ERStairs;
import triage.room.ERWaitingRoom;
import triage.room.draw.ERAngiographyRoomDraw2D;
import triage.room.draw.ERCTRoomDraw2D;
import triage.room.draw.ERConsultationRoomDraw2D;
import triage.room.draw.ERElevatorDraw2D;
import triage.room.draw.EREmergencyRoomDraw2D;
import triage.room.draw.ERFastRoomDraw2D;
import triage.room.draw.ERGeneralWardRoomDraw2D;
import triage.room.draw.ERHighCareUnitRoomDraw2D;
import triage.room.draw.ERIntensiveCareUnitRoomDraw2D;
import triage.room.draw.ERMRIRoomDraw2D;
import triage.room.draw.ERObservationRoomDraw2D;
import triage.room.draw.EROperationRoomDraw2D;
import triage.room.draw.ERSevereInjuryObservationRoomDraw2D;
import triage.room.draw.ERStairsDraw2D;
import triage.room.draw.ERWaitingRoomDraw2D;
import triage.room.draw.ERXRayRoomDraw2D;
import utility.cmd.CCmdCheck;
import utility.initparam.InitGUISimParam;
import utility.initparam.InitInverseSimParam;
import utility.initparam.InitSimParam;
import utility.logger.CustomLogFormatter;
import utility.sfmt.Rand;


/**
 * main関数を定義しています。
 * Trisimの実行するためのソースコードを記載しています。<br>
 *
 * Trisimは3パターンモードがあり、コンソールモード、guiモード、逆シミュレーションモードがあります。<br>
 * コマンドラインオプションに指定しないで実行すると、guiモードで起動します。
 * gui以外はオプションの-modeで0を指定すると通常シミュレーションモード
 * 2を指定すると逆シミュレーションモードとして動作します。
 *
 * @author kobayashi
 */
public class ERMain
{

	public static final Object csCriticalSection = new Object();	// 排他制御用のロック変数

	private static SimpleMeshGeometry geo;							// シミュレーションの表示方法
	private static Environment env;									// シミュレーション環境
	private static SimulationEngine engine;							// シミュレーションエンジン
	private static InverseSimulationEngine invSimEngine;			// 逆シミュレーションエンジン

	private static Rand random;										// グローバルで乱数オブジェクトを作成

	/**
	 * <PRE>
	 *    メイン関数です。
	 * </PRE>
	 * @param args コマンド引数
	 */
	public static void main(String[] args)
	{
		InitSimParam initSimParam;									//初期設定ファイル
		InitGUISimParam initGuiSimParam;							//初期設定ファイル(GUIモード用)
		InitInverseSimParam initInvSimParam;						//初期設定ファイル(逆シミュレーション用)
		final Logger cTRISimLogger = Logger.getLogger("TRISimLogging");
		long lRet = 0;
		ERDepartment erDepartment;
		CCmdCheck cmd;
		ObjectiveFunctionInterface objFuncInterface;
		ERDepartmentArrivalPatient erThreadArrivalPatient = null;
//		ERDepartmentAdditionPanel panel = new ERDepartmentAdditionPanel();

		FusePanelSimpleMesh[] pt2d = null;
		ERTriageDebugWindowKeyAndMouseListner[] ptKam2d = null;
		FuseWindow[] ptWindow2d = null;

		FusePanelSimpleMesh3D[] pt3d = null;
		KeyAndMouseListner3D[] ptKam3d = null;
		FuseWindow[] ptWindow3d = null;

		FuseControler[] ptCtrl = null;

		String strNodeLinkFileName = "./parameter/TriageNodeLinkTable.txt";

		int iTimeStep = 10000;

		long seed;
		seed = (long)(Math.random()*Long.MAX_VALUE);
		random = null;
		random = new utility.sfmt.Rand( (int)seed );

		// 初期設定ファイル読み込みクラスのインスタンスを生成します。
		initSimParam = new InitSimParam();
		initGuiSimParam = new InitGUISimParam();
		initInvSimParam = new InitInverseSimParam();

		try
		{
			// デフォルト設定を行います。
			initSimParam.vSetDefaultValue();
			// 初期設定ファイルを読み込みます。
			initSimParam.readInitSettingFile();

			// ログ出力設定をします。
			vInitLogger( cTRISimLogger, initSimParam );

			pt2d = new FusePanelSimpleMesh[1];
			ptKam2d = new ERTriageDebugWindowKeyAndMouseListner[1];
			ptWindow2d = new FuseWindow[1];

			pt3d = new FusePanelSimpleMesh3D[1];
			ptKam3d = new KeyAndMouseListner3D[1];
			ptWindow3d = new FuseWindow[1];

			ptCtrl = new FuseControler[1];
			// コマンドライン解析クラスのインスタンスを作成します。
			cmd = new CCmdCheck();

			// コマンドラインの解析を実行します。
			lRet = cmd.lCommandCheck(args);

			// CUIモードであれば、バッチ処理を実行します。
			if( cmd.iGetExecMode() == 0 )
			{
				if( lRet == 0 )
				{
/*------------------------------初期化部-------------------------------------------------*/
					// TRISim実行クラスのインスタンスを作成します。
					erDepartment = new ERDepartment();
					// ログ出力を登録します。
					erDepartment.vSetLog( cTRISimLogger );

					// 初期化を実行します。
					vInitialize( cmd, erDepartment, cTRISimLogger, strNodeLinkFileName, csCriticalSection, initSimParam );

					// 患者エージェントを別スレッドから登場させるようにします。
					vThreadInvoke( cmd, engine, erDepartment, erThreadArrivalPatient, cTRISimLogger, csCriticalSection, initSimParam );

					// シミュレーションを開始します。
					vStart( cmd.iGetSimulationTimeStep(), engine );

					System.out.println("終了したよ");

					// 終了処理を実行します。
					vTerminate( erDepartment );

					// 実行ファイルを完全終了させます。
					System.exit(0);
				}
				else
				{
					cmd.vHelp();
				}
			}
			// 逆シミュレーションを実行します。
			else if( cmd.iGetExecMode() == 2 )
			{
				if( lRet == 0 )
				{
					// デフォルト設定を行います。
					initInvSimParam.vSetDefaultValue();
					// 初期設定ファイルを読み込みます。
					initInvSimParam.readInitSettingFile();

//					// 初期化を実行します。
					vInitialize( cmd, cTRISimLogger, strNodeLinkFileName, csCriticalSection, initSimParam, initInvSimParam );

					// 最適化手法の初期化を行います。
					vInitializeInvSimEngine( cmd );

					// 評価指標を設定します。
					vInstallCallbackFunction( cmd, initInvSimParam );

					// シミュレーションの初期設定を行います。
					vInitialInvSimSet();

//					invSimEngine.vInstallCallbackFunction(interfaceObjFunc);

					// 患者エージェントを別スレッドから登場させるようにします。
//					vThreadInvoke( cmd, engine, invSimEngine.erGetERDepartments, erThreadArrivalPatient, initSimParam );

					// シミュレーションを開始します。
					vStartInvSim( cmd.iGetSimulationTimeStep(), cmd.iGetInverseSimulationIntervalNumber() );

					// 終了処理を実行します。
					vTerminate();

					// 実行ファイルを完全終了させます。
					System.exit(0);
				}
				else
				{
					cmd.vHelp();
				}
			}
			// GUIモードであれば、画面を表示させてシミュレーションを実行します。
			else if( cmd.iGetExecMode() == 1 )
			{
				// TRISim実行クラスのインスタンスを作成します。
				erDepartment = new ERDepartment();
				// ログ出力を登録します。
				erDepartment.vSetLog( cTRISimLogger );

				// デフォルト設定を行います。
				initGuiSimParam.vSetDefaultValue();
				// 初期設定ファイルを読み込みます。
				initGuiSimParam.readInitSettingFile();

				// 初期化を実行します。
				vInitialize( erDepartment, cTRISimLogger, strNodeLinkFileName, csCriticalSection, initGuiSimParam, initSimParam );

				// 描画処理の初期を実行します。
//				vReadDrawERDepartmentFile();

				// エージェントの描画方法の初期化をします。
				// ２次元表示
				vInitDraw2D( engine, pt2d, ptKam2d, ptWindow2d );
				ptKam2d[0].vSetERTriageNodeManager(erDepartment.getERTriageNodeManager());
				// ３次元表示
//				vInitDraw3D( engine, p3d, kam3d, window3d );

				// シミュレーション制御用パネルの作成及びパネルへ追加します。
				vCreateControlPanel( ptCtrl, engine, ptWindow2d );

				// 制御パネル作成完了後、パネルに設定されたパラメータに従って初期化を実行します。
//				vSetParameter();

				// シミュレーション実行画面（２次元）の表示をします。
				vSetVisible2D( ptWindow2d[0] );
				// シミュレーション実行画面（３次元）の表示をします。
//				vSetVisible3D( window3d );

				// 患者エージェントを別スレッドから登場させるようにします。
//				vThreadInvoke( engine, erDepartment, erThreadArrivalPatient, initSimParam );

				// シミュレーションを開始します。
				vStart( iTimeStep, engine );

				// 終了処理を実行します。
//				vTerminate( erDepartment );
			}
			// テストモード
			else if( cmd.iGetExecMode() == 3 )
			{
				if( lRet == 0 )
				{
					// 制約条件の動作確認を行います。
					vTestConstraintCondition( initInvSimParam, cmd, cTRISimLogger, strNodeLinkFileName );

					// 実行ファイルを完全終了させます。
					System.exit(0);

				}
				else
				{
					cmd.vHelp();
				}
			}
		}
//		catch( ERDoctorAgentException edae )
//		{
//			int i;
//			StackTraceElement ste[] = (new Throwable()).getStackTrace();
//			edae.SetErrorInfo( ERDoctorAgent.ERDA_FATAL_ERROR, "action", "ERDoctorAgent", "不明、および致命的エラー", ste[0].getLineNumber() );
//			// エラー詳細を出力
//			String strMethodName = edae.strGetMethodName();
//			String strClassName = edae.strGetClassName();
//			String strErrDetail = edae.strGetErrDetail();
//			int iErrCode = edae.iGetErrCode();
//			int iErrLine = edae.iGetErrorLine();
//			cTRISimLogger.warning( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
//			for( i = 0;i < edae.getStackTrace().length; i++ )
//			{
//				String str = "クラス名" + "," + edae.getStackTrace()[i].getClassName();
//				str += "メソッド名" + "," + edae.getStackTrace()[i].getMethodName();
//				str += "ファイル名" + "," + edae.getStackTrace()[i].getFileName();
//				str += "行数" + "," + edae.getStackTrace()[i].getLineNumber();
//				cTRISimLogger.warning( str );
//			}
//		}
//		catch( ERNurseAgentException enae )
//		{
//
//		}
//		catch( ERClinicalEngineerAgentException ecnae )
//		{
//
//		}
//		catch( ERPatientAgentException epae )
//		{
//
//		}
		catch( GenAlgException gae )
		{
			String strMethodName = gae.strGetMethodName();
			String strClassName = gae.strGetClassName();
			String strErrDetail = gae.strGetErrDetail();
			int iErrCode = gae.iGetErrCode();
			int iErrLine = gae.iGetErrorLine();
			System.out.println( strClassName + "," + strMethodName + "," + strErrDetail + "," + iErrCode + "," + iErrLine );
		}
		catch( IOException ioe )
		{
			int i;
			String str = "";
			// エラー詳細を出力
			str += "例外名" + "," + ioe.getClass() + ",";
			str += "理由" + "," + ioe.getMessage() + ",";
			str += "詳細理由" + "," + ioe.getLocalizedMessage() + ",";
			for( i = 0;i < ioe.getStackTrace().length; i++ )
			{
				str += "クラス名" + "," + ioe.getStackTrace()[i].getClassName() + ",";
				str += "メソッド名" + "," + ioe.getStackTrace()[i].getMethodName() + ",";
				str += "ファイル名" + "," + ioe.getStackTrace()[i].getFileName() + ",";
				str += "行数" + "," + ioe.getStackTrace()[i].getLineNumber() + ",";
				cTRISimLogger.warning( str );
				System.out.println( str );
			}
		}
		catch( NullPointerException npe )
		{
			int i;
			String str = "";
			// エラー詳細を出力
			str += "例外名" + "," + npe.getClass() + ",";
			str += "理由" + "," + npe.getMessage() +",";
			str += "詳細理由" + "," + npe.getLocalizedMessage() + ",";
			for( i = 0;i < npe.getStackTrace().length; i++ )
			{
				str += "クラス名" + "," + npe.getStackTrace()[i].getClassName() + ",";
				str += "メソッド名" + "," + npe.getStackTrace()[i].getMethodName() + ",";
				str += "ファイル名" + "," + npe.getStackTrace()[i].getFileName() + ",";
				str += "行数" + "," + npe.getStackTrace()[i].getLineNumber() + ",";
				cTRISimLogger.warning( str );
				System.out.println( str );
			}
		}
		catch( ArrayIndexOutOfBoundsException iobe )
		{
			int i;
			String str = "";
			// エラー詳細を出力
			str += "例外名" + "," + iobe.getClass() + ",";
			str += "理由" + "," + iobe.getMessage() + ",";
			str += "詳細理由" + "," + iobe.getLocalizedMessage() + ",";
			for( i = 0;i < iobe.getStackTrace().length; i++ )
			{
				str += "クラス名" + "," + iobe.getStackTrace()[i].getClassName() + ",";
				str += "メソッド名" + "," + iobe.getStackTrace()[i].getMethodName() + ",";
				str += "ファイル名" + "," + iobe.getStackTrace()[i].getFileName() + ",";
				str += "行数" + "," + iobe.getStackTrace()[i].getLineNumber() + ",";
				cTRISimLogger.warning( str );
				System.out.println( str );
			}
		}
		catch( IllegalArgumentException ilae )
		{
			int i;
			String str = "";
			// エラー詳細を出力
			str += "例外名" + "," + ilae.getClass() + ",";
			str += "理由" + "," + ilae.getMessage() + ",";
			str += "詳細理由" + "," + ilae.getLocalizedMessage() + ",";
			for( i = 0;i < ilae.getStackTrace().length; i++ )
			{
				str += "クラス名" + "," + ilae.getStackTrace()[i].getClassName() + ",";
				str += "メソッド名" + "," + ilae.getStackTrace()[i].getMethodName() + ",";
				str += "ファイル名" + "," + ilae.getStackTrace()[i].getFileName() + ",";
				str += "行数" + "," + ilae.getStackTrace()[i].getLineNumber() + ",";
				cTRISimLogger.warning( str );
				System.out.println( str );
			}
		}
		finally
		{

		}
	}

	/**
	 * <PRE>
	 *    逆シミュレーションエンジンとして使用する最適化手法を設定します。
	 * </PRE>
	 * @param cmd	コマンド解析インスタンス
	 */
	private static void vInitializeInvSimEngine(CCmdCheck cmd)
	{
		// TODO 自動生成されたメソッド・スタブ
		if( cmd.iGetInverseSimulationMethod() == 1 )
		{

		}
		else if( cmd.iGetInverseSimulationMethod() == 2 )
		{

		}
		else if( cmd.iGetInverseSimulationMethod() == 3 )
		{

		}
		else if( cmd.iGetInverseSimulationMethod() == 4 )
		{
			int iLimitCountData = 300;
			int iIntervalNum = cmd.iGetInverseSimulationIntervalNumber();
			int iGensNum = cmd.iGetGensNumber();
			int iGensVectorDimension = cmd.iGetGensVectorDimension();
			int iSearchNum = cmd.iGetGensNumber()/2;
			int iAbcMethod = cmd.iGetAbcMethod();
			invSimEngine.vInitInvSimEngineAbc( iAbcMethod, iSearchNum, iLimitCountData );
		}
	}

	/**
	 * <PRE>
	 *   ログ出力の初期化をします。
	 * </PRE>
	 * @param cLogger		設定するロガーのインスタンス
	 * @param initSimParam	初期設定ファイルのインスタンス
	 * @throws IOException	ファイル読み込みエラー
	 */
	private static void vInitLogger( Logger cLogger, InitSimParam initSimParam ) throws IOException
	{
		// ログファイル出力設定をします。
		FileHandler fileHandler = new FileHandler(
				"SampleLogging%u.%g.log",				// 出力ファイル名			pattern
				(int)initSimParam.lGetLogFileSize(),	// 最大ファイルサイズ[byte] limit
				(int)initSimParam.lGetLogFileCount(),	// 出力するログファイル数   count
				initSimParam.bGetLogFileAppend()		// 追加モード				append
		);

		// テキスト形式でCustomLogFormatterを設定
		fileHandler.setFormatter( new CustomLogFormatter() );

		// ログの出力先を設定
		cLogger.addHandler( fileHandler );

		// ログ出力レベルを設定(ここではすべて出力するように設定)
		cLogger.setLevel(initSimParam.levGetLogLevel());

		// 親ロガーへ出力するようにしない。
		cLogger.setUseParentHandlers( false );

	}

	/**
	 * <PRE>
	 *    逆シミュレーション用の初期パラメータ設定をします。
	 * </PRE>
	 */
	private static void vInitialInvSimSet()
	{
		invSimEngine.vInitialSet();
	}

	/**
	 * <PRE>
	 *   逆シミュレーション時に設定する評価指標を設定します。
	 *   逆シミュレーション時に使用します。
	 * </PRE>
	 * @param cmd		コマンド解析インスタンス
	 * @param invParam	逆シミュレーション用パラメータ調整用ファイル
	 */
	private static void vInstallCallbackFunction( CCmdCheck cmd, InitInverseSimParam invParam )
	{
		// TODO 自動生成されたメソッド・スタブ
		int iEvaluationIndexMode = 101;
		int iEvaluationIndexCompMode = 1;

		// 逆シミュレーションの評価指標を設定します。
		if( cmd.strGetEvaluationIndicator().equals("nedocs") == true ) iEvaluationIndexMode = 101;
		else if( cmd.strGetEvaluationIndicator().equals("edworkscore") == true  ) iEvaluationIndexMode = 102;
		else if( cmd.strGetEvaluationIndicator().equals("edwin") == true ) iEvaluationIndexMode = 103;

		// 逆シミュレーションの評価指標比較方法を設定します。
		if( cmd.strGetEvaluationIndexCompMode().equals("avg") == true ) iEvaluationIndexCompMode = 1;
		else if( cmd.strGetEvaluationIndexCompMode().equals("max") == true ) iEvaluationIndexCompMode = 2;

		// 目的関数を設定します。
		ObjectiveFunctionInterface pflfObjectiveFunction;
		pflfObjectiveFunction = new ObjectiveFunction();
		invSimEngine.vInstallCallbackFunction( pflfObjectiveFunction, iEvaluationIndexMode );
		invSimEngine.vSetEvaluationIndexCompMode( iEvaluationIndexCompMode );

		// 制約条件を設定します。
		ConstraintConditionInterface pflfConstraintCondition;
		pflfConstraintCondition = new ConstraintCondition( 4, invParam );
		invSimEngine.vInstallCallbackCondition( pflfConstraintCondition );
	}

	/**
	 * <PRE>
	 *    通常モードの初期化を行います。
	 *    CUI上で動作することを前提としているモードです。
	 *    コマンドを設定して実行します。
	 * </PRE>
	 * @param cmd						コマンド解析インスタンス
	 * @param erDepartment				救急部門インスタンス
	 * @param log						ログのインスタンス
	 * @param strNodeLinkFileName		FUSEノードリンクのファイル名
	 * @param cs						クリティカルセクションのインスタンス
	 * @param initParam					初期設定ファイルのインスタンス
	 */
	private static void vInitialize( CCmdCheck cmd, ERDepartment erDepartment, Logger log, String strNodeLinkFileName, Object cs, InitSimParam initParam )
	{
		// 地形の作成(縦横のメッシュの数を指定。)
		geo = new SimpleMeshGeometry(300, 300);

		// メッシュサイズの指定
		geo.setMeshCellSize(10,10);

		// 環境の作成
		env = new Environment(geo);

		// シミュレーションエンジンの作成
		engine = new SimulationEngine(env);

		// 通常シミュレーションモード
		// 救急部門を構成します。
		erDepartment.vSetSimulationEndTime( cmd.iGetEndSimulationTime() );
		erDepartment.vInitialize( engine, env, cmd.strGetEmergencyDepartmentPath(), cmd.strGetConsultationRoomPath(),
				cmd.strGetOperationRoomPath(), cmd.strGetEmergencyRoomPath(), cmd.strGetObservationRoomPath(),
				cmd.strGetSevereInjuryObservationRoomPath(), cmd.strGetIntensiveCareUnitPath(),
				cmd.strGetHighCareUnitPath(), cmd.strGetGeneralWardPath(), cmd.strGetWaitingRoomPath(), cmd.strGetXRayRoomPath(),
				cmd.strGetCTRoomPath(), cmd.strGetMRIRoomPath(), cmd.strGetAngiographyRoomPath(), cmd.strGetFastRoomPath(),
				cmd.lfGetPatientPepole(), cmd.iGetPatientRandomMode(), cmd.iGetExecMode(), cmd.iGetFileWriteMode(),
				cmd.iGetPatientArrivalMode(), random, initParam, cmd.iGetInitGeneralWardPatientNum(), cmd.iGetInitIntensiveCareUnitPatientNum(), cmd.iGetInitHighCareUnitPatientNum() );
		erDepartment.vSetAllLog( log );
		erDepartment.vSetCriticalSection( cs );
//		erDepartment.vReadNodeManager( strNodeLinkFileName );
	}

	/**
	 * <PRE>
	 *    逆シミュレーションモードの初期化を行います。
	 *    CUI上で動作することを前提としているモードです。
	 *    コマンドを設定して実行します。
	 *    ノードとリンクは使用しません。
	 *    制約条件に関してはerInvSimEv.iniファイルで設定します。
	 *
	 * </PRE>
	 * @param cmd						コマンド解析インスタンス
	 * @param log						ログインスタンス
	 * @param strNodeLinkFileName		FUSEノードリンクのファイル名
	 * @param cs						クリティカルセクションのインスタンス
	 * @param initParam					初期設定ファイルのインスタンス
	 * @param initInvParam				逆シミュレーション用初期設定ファイルのインスタンス
	 * @throws IOException				java標準エラー
	 */
	private static void vInitialize( CCmdCheck cmd, Logger log, String strNodeLinkFileName, Object cs, InitSimParam initParam, InitInverseSimParam initInvParam  ) throws IOException
	{
		// 地形の作成(縦横のメッシュの数を指定。)
		geo = new SimpleMeshGeometry(300, 300);

		// メッシュサイズの指定
		geo.setMeshCellSize(10,10);

		// 環境の作成
		env = new Environment(geo);

		// シミュレーションエンジンの作成
		engine = new SimulationEngine(env);

		// 逆シミュレーションエンジンの作成
		invSimEngine = new InverseSimulationEngine();

		// 逆シミュレーションの初期化を行います。
		log.warning( "クリティカルセクションのアドレス" + "," + csCriticalSection + "," );
		invSimEngine.vInitialize( cmd.iGetInverseSimulationIntervalNumber(), cmd.iGetGensNumber(), cmd.iGetGensVectorDimension(), cmd.iGetInverseSimulationMethod(),
			cmd.iGetEndSimulationTime(), engine, env, cmd.strGetConsultationRoomPath(),
			cmd.strGetOperationRoomPath(), cmd.strGetEmergencyRoomPath(), cmd.strGetObservationRoomPath(),
			cmd.strGetSevereInjuryObservationRoomPath(), cmd.strGetIntensiveCareUnitPath(),
			cmd.strGetHighCareUnitPath(), cmd.strGetGeneralWardPath(), cmd.strGetWaitingRoomPath(), cmd.strGetXRayRoomPath(),
			cmd.strGetCTRoomPath(), cmd.strGetMRIRoomPath(), cmd.strGetAngiographyRoomPath(), cmd.strGetFastRoomPath(),
			log, cs, random, cmd.lfGetPatientPepole(), cmd.iGetPatientRandomMode(), cmd.iGetExecMode(), cmd.iGetFileWriteMode(),cmd.iGetPatientArrivalMode(), initParam, initInvParam,
			cmd.iGetInitGeneralWardPatientNum(), cmd.iGetInitIntensiveCareUnitPatientNum(), cmd.iGetInitHighCareUnitPatientNum() );

		if( cmd.iGetExecMode() == 1 )
		{
//			invSimEngine.vInitInvSimEngineGa(iGaMethodData, iSearchNumData, iLimitCountData);
		}
		else if( cmd.iGetExecMode() == 2 )
		{
//			invSimEngine.vInitInvSimEngineRcGa(iRcGaMethodData, iSearchNumData, iLimitCountData);
		}
		else if( cmd.iGetExecMode() == 3 )
		{
//			invSimEngine.vInitInvSimEnginePso(iPsoMethodData, iSearchNumData, iLimitCountData);
		}
		else if( cmd.iGetExecMode() == 4 )
		{
			invSimEngine.vInitInvSimEngineAbc( cmd.iGetAbcMethod(), cmd.iGetAbcSearchNumber(), cmd.iGetAbcLimitCount() );
		}
	}

	/**
	 * <PRE>
	 *   可視化モードの場合の初期化を行います。
	 *   erEnv.iniファイルに記載された内容を基に設定して初期化を行います。
	 *   設定がない場合はデフォルト値を設定して初期化を行います。。
	 * </PRE>
	 * @param erDepartment				救急部門インスタンス
	 * @param log						ログのインスタンス
	 * @param strNodeLinkFileName		FUSEノードリンクのファイル名
	 * @param cs						クリティカルセクションのインスタンス
	 * @param initGuiSimParam			可視化用初期設定ファイルのインスタンス
	 * @param initParam					初期設定ファイルのインスタンス
	 * @throws IOException				java標準IOExceptionクラス
	 */
	private static void vInitialize( ERDepartment erDepartment, Logger log, String strNodeLinkFileName, Object cs, InitGUISimParam initGuiSimParam, InitSimParam initParam ) throws IOException
	{
		int iPatientRandomMode = 0;
		int iFileWriteModeFlag = 0;
		int iPatientArrivalMode = 0;
//		double lfEndTime = 86400.0;
		double lfEndTime = 172800.0;

		// 地形の作成(縦横のメッシュの数を指定。)
		geo = new SimpleMeshGeometry(initGuiSimParam.iGetMeshHeightNum(), initGuiSimParam.iGetMeshWidthNum());

		// メッシュサイズの指定
		geo.setMeshCellSize(initGuiSimParam.iGetMeshHeight(),initGuiSimParam.iGetMeshWidth());

		// 環境の作成
		env = new Environment(geo);

		// シミュレーションエンジンの作成
		engine = new SimulationEngine(env);

		String strEmergencyDepartmentPath = initGuiSimParam.strGetEmergencyDepartmentPath();
		String strConsultationRoomPath = initGuiSimParam.strGetConsultationRoomPath();
		String strOperationRoomPath = initGuiSimParam.strGetOperationRoomPath();
		String strEmergencyRoomPath = initGuiSimParam.strGetEmergencyRoomPath();
		String strObservationRoomPath = initGuiSimParam.strGetObservationRoomPath();
		String strSevereInjuryObservationRoomPath = initGuiSimParam.strGetSevereInjuryObservationRoomPath();
		String strIntensiveCareUnitPath = initGuiSimParam.strGetIntensiveCareUnitPath();
		String strHighCareUnitPath = initGuiSimParam.strGetHighCareUnitPath();
		String strGeneralWardPath = initGuiSimParam.strGetGeneralWardPath();
		String strWaitingRoomPath = initGuiSimParam.strGetWaitingRoomPath();
		String strXRayRoomPath = initGuiSimParam.strGetXRayRoomPath();
		String strCTRoomPath = initGuiSimParam.strGetCTRoomPath();
		String strMRIRoomPath = initGuiSimParam.strGetMRIRoomPath();
		String strAngiographyRoomPath = initGuiSimParam.strGetAngiographyRoomPath();
		String strFastRoomPath = initGuiSimParam.strGetFastRoomPath();
		double lfPatientPepole = initGuiSimParam.lfGetPatientPepole();
		iPatientRandomMode = initGuiSimParam.iGetPatientRandomMode();
		iFileWriteModeFlag = initGuiSimParam.iGetFileWriteMode();
		lfEndTime = initGuiSimParam.iGetEndSimulationTime();

		// 救急部門を構成します。
		//< 一般病棟、ICU、HCUに関しての初期入院患者設定に関してはGUIモードでは動作しないように設定。
		//< 今後動作させるように修正予定。
		erDepartment.vSetSimulationEndTime( lfEndTime );
		erDepartment.vInitialize( engine, env, strEmergencyDepartmentPath, strConsultationRoomPath,
				strOperationRoomPath, strEmergencyRoomPath, strObservationRoomPath,
				strSevereInjuryObservationRoomPath, strIntensiveCareUnitPath,
				strHighCareUnitPath, strGeneralWardPath, strWaitingRoomPath, strXRayRoomPath,
				strCTRoomPath, strMRIRoomPath, strAngiographyRoomPath, strFastRoomPath,
				lfPatientPepole, iPatientRandomMode, 1, iFileWriteModeFlag,
				iPatientArrivalMode, random, initParam, 0, 0, 0 );

		// ログインスタンス及びクリティカルセクションインスタンスを設定します。
		erDepartment.vSetAllLog( log );
		erDepartment.vSetCriticalSection( cs );

		// 救急部門の施設配置を描画します。
		String strEmergencyDepartmentAxisPath = initGuiSimParam.strGetEmergencyDepartmentAxisPath();
		String strConsultationRoomAxisPath = initGuiSimParam.strGetConsultationRoomAxisPath();
		String strOperationRoomAxisPath = initGuiSimParam.strGetOperationRoomAxisPath();
		String strEmergencyRoomAxisPath = initGuiSimParam.strGetEmergencyRoomAxisPath();
		String strObservationRoomAxisPath = initGuiSimParam.strGetObservationRoomAxisPath();
		String strSevereInjuryObservationRoomAxisPath = initGuiSimParam.strGetSevereInjuryObservationRoomAxisPath();
		String strIntensiveCareUnitAxisPath = initGuiSimParam.strGetIntensiveCareUnitAxisPath();
		String strHighCareUnitAxisPath = initGuiSimParam.strGetHighCareUnitAxisPath();
		String strGeneralWardAxisPath = initGuiSimParam.strGetGeneralWardAxisPath();
		String strWaitingRoomAxisPath = initGuiSimParam.strGetWaitingRoomAxisPath();
		String strXRayRoomAxisPath = initGuiSimParam.strGetXRayRoomAxisPath();
		String strCTRoomAxisPath = initGuiSimParam.strGetCTRoomAxisPath();
		String strMRIRoomAxisPath = initGuiSimParam.strGetMRIRoomAxisPath();
		String strAngiographyRoomAxisPath = initGuiSimParam.strGetAngiographyRoomAxisPath();
		String strFastRoomAxisPath = initGuiSimParam.strGetFastRoomAxisPath();
		String strStairsAxisPath = initGuiSimParam.strGetStairsAxisPath();
		String strElevatorAxisPath = initGuiSimParam.strGetElevatorAxisPath();
		String strOtherRoomAxisPath = initGuiSimParam.strGetOtherRoomAxisPath();

		erDepartment.vReadDrawERDepartmentFile(strEmergencyDepartmentAxisPath,
				strConsultationRoomAxisPath, strOperationRoomAxisPath, strEmergencyRoomAxisPath,
				strObservationRoomAxisPath, strSevereInjuryObservationRoomAxisPath,
				strIntensiveCareUnitAxisPath, strHighCareUnitAxisPath, strGeneralWardAxisPath,
				strWaitingRoomAxisPath, strXRayRoomAxisPath, strCTRoomAxisPath,
				strMRIRoomAxisPath, strAngiographyRoomAxisPath, strFastRoomAxisPath,
				strStairsAxisPath, strElevatorAxisPath, strOtherRoomAxisPath );
		erDepartment.vReadNodeManager( strNodeLinkFileName );

		// 描画用初期設定ファイルクラスの設定を行います。
		ERDepartmentDraw2D.vSetInitGuiSimParam( initGuiSimParam );
		ERTriageDebugWindowKeyAndMouseListner.vSetInitGuiSimParam( initGuiSimParam );
}

	/**
	 * <PRE>
	 *    患者発生を別スレッドに立ち上げて実行します。
	 *    通常シミュレーション用
	 * </PRE>
	 * @param cmd						コマンドライン解析クラス
	 * @param engine					FUSEシミュレーションエンジン
	 * @param erDepartment				救急部門インスタンス
	 * @param erThreadArrivalPatient	患者到達分布生成用スレッド
	 * @param cLogData					ログ出力インスタンス
	 * @param cObject					クリティカルセクション用のハンドル
	 * @param initparam					初期設定パラメータ
	 */
	private static void vThreadInvoke( CCmdCheck cmd, SimulationEngine engine, ERDepartment erDepartment, ERDepartmentArrivalPatient erThreadArrivalPatient, Logger cLogData, Object cObject, InitSimParam initparam )
	{
		erDepartment.vSetGenerationPatientMode( cmd.iGetGenerationPatientMode() );
		if( cmd.iGetGenerationPatientMode() == 1 )
		{
			erThreadArrivalPatient = new ERDepartmentArrivalPatient();
			erThreadArrivalPatient.vSetSimulationEngine( engine );
			erThreadArrivalPatient.vSetWaitingRoom( erDepartment.erGetWaitingRoom() );
			erThreadArrivalPatient.vSetRandomMode(cmd.iGetPatientRandomMode() );
			erThreadArrivalPatient.vSetInverseSimFlag( cmd.iGetExecMode() );
			erThreadArrivalPatient.vSetFileWriteMode( cmd.iGetFileWriteMode() );
			erThreadArrivalPatient.vSetPatientArrivalMode( cmd.iGetPatientArrivalMode() );
			erThreadArrivalPatient.vSetInitSimParam( initparam );
			erThreadArrivalPatient.vSetLogger( cLogData );
			erThreadArrivalPatient.vSetCriticalSection( cObject );
			erThreadArrivalPatient.start();
		}
	}

	/**
	 * <PRE>
	 *    患者発生を別スレッドに立ち上げて実行します。
	 *    逆シミュレーション用
	 * </PRE>
	 * @param cmd						コマンドライン解析クラス
	 * @param engine					FUSEシミュレーションエンジン
	 * @param erThreadArrivalPatient	患者到達分布生成用スレッド
	 * @param cLogData					ログ出力インスタンス
	 * @param cObject					クリティカルセクション用のハンドル
	 * @param initparam					初期設定パラメータ
	 */
	private static void vThreadInvoke( CCmdCheck cmd, SimulationEngine engine, ERDepartmentArrivalPatient erThreadArrivalPatient, Logger cLogData, Object cObject, InitSimParam initparam )
	{
		for( int i = 0; i < invSimEngine.erGetERDepartments().length; i++ )
		{
			invSimEngine.erGetERDepartments()[i].vSetGenerationPatientMode( cmd.iGetGenerationPatientMode() );
		}
		if( cmd.iGetGenerationPatientMode() == 1 )
		{
			for( int i = 0; i < invSimEngine.erGetERDepartments().length; i++ )
			{
				erThreadArrivalPatient = new ERDepartmentArrivalPatient();
				erThreadArrivalPatient.vSetSimulationEngine( engine );
				erThreadArrivalPatient.vSetWaitingRoom( invSimEngine.erGetERDepartments()[i].erGetWaitingRoom() );
				erThreadArrivalPatient.vSetRandomMode(cmd.iGetPatientRandomMode() );
				erThreadArrivalPatient.vSetInverseSimFlag( cmd.iGetExecMode() );
				erThreadArrivalPatient.vSetFileWriteMode( cmd.iGetFileWriteMode() );
				erThreadArrivalPatient.vSetPatientArrivalMode( cmd.iGetPatientArrivalMode() );
				erThreadArrivalPatient.vSetLogger( cLogData );
				erThreadArrivalPatient.vSetCriticalSection( cObject );
				erThreadArrivalPatient.vSetInitSimParam( initparam );
				erThreadArrivalPatient.start();
			}
		}
	}

	/**
	 * <PRE>
	 *    2次元表示の初期化を実行します。
	 * </PRE>
	 * @param erEngine		シミュレーションエンジン
	 * @param pt2d			描画領域のインスタンス
	 * @param ptKam			キーリスナーインスタンス
	 * @param ptWindow		描画ウィンドウのインスタンス
	 */
	private static void vInitDraw2D( SimulationEngine erEngine, FusePanelSimpleMesh[] pt2d, ERTriageDebugWindowKeyAndMouseListner[] ptKam, FuseWindow[] ptWindow )
	{
		AltitudeColor colorManager;
		// シミュレーション画面の設定を行います。
		// 2D表示
		pt2d[0] = new FusePanelSimpleMesh( erEngine );
		ptKam[0] = new ERTriageDebugWindowKeyAndMouseListner(pt2d[0]);
		pt2d[0].setKAMListner(ptKam[0]);
		ptWindow[0] = new FuseWindow(pt2d[0]);

		// 各エージェント描画クラスを対応するエージェントクラスのひも付けします。
		pt2d[0].addObjectDrawerCreateRule( ERDepartment.class, ERDepartmentDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERConsultationRoom.class, ERConsultationRoomDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( EROperationRoom.class, EROperationRoomDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( EREmergencyRoom.class, EREmergencyRoomDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERObservationRoom.class, ERObservationRoomDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERSevereInjuryObservationRoom.class, ERSevereInjuryObservationRoomDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERIntensiveCareUnitRoom.class, ERIntensiveCareUnitRoomDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERHighCareUnitRoom.class, ERHighCareUnitRoomDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERGeneralWardRoom.class, ERGeneralWardRoomDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERWaitingRoom.class, ERWaitingRoomDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERExaminationXRayRoom.class, ERXRayRoomDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERExaminationCTRoom.class, ERCTRoomDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERExaminationMRIRoom.class, ERMRIRoomDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERExaminationAngiographyRoom.class, ERAngiographyRoomDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERExaminationFastRoom.class, ERFastRoomDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERPatientAgent.class, ERPatientAgentDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERNurseAgent.class, ERNurseAgentDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERDoctorAgent.class, ERDoctorAgentDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERClinicalEngineerAgent.class, ERClinicalEngineerAgentDraw2D.class);
		pt2d[0].addObjectDrawerCreateRule( ERStairs.class, ERStairsDraw2D.class );
		pt2d[0].addObjectDrawerCreateRule( ERElevator.class, ERElevatorDraw2D.class );

		colorManager = new AltitudeColor();
		colorManager.addAltitudeColor(0.0, new Color( 0xFF, 0xFF, 0xFF ) );
		pt2d[0].setColorManager( colorManager );
	}

	/**
	 * <PRE>
	 *    3次元表示の初期化を実行します。
	 * </PRE>
	 * @param erEngine		シミュレーションエンジン
	 * @param p3d			描画領域のインスタンス
	 * @param kam3d			キーリスナーインスタンス
	 * @param window3d		描画ウィンドウのインスタンス
	 */
	private static void vInitDraw3D( SimulationEngine erEngine, FusePanelSimpleMesh3D p3d, KeyAndMouseListner3D kam3d, FuseWindow window3d )
	{
		// シミュレーション画面の設定
		p3d = new FusePanelSimpleMesh3D( erEngine );
		kam3d = new KeyAndMouseListner3D( p3d );
		p3d.setKAMListner(kam3d);
		window3d = new FuseWindow( p3d );

		// 各エージェント描画クラスを対応するエージェントクラスのひも付けします。
//		p3d.addObjectDrawerCreateRule( ERConsultationRoom.class, ERConsultationRoomDraw3D.class);
//		p3d.addObjectDrawerCreateRule( EROperationRoom.class, EROperationRoomDraw3D.class);
//		p3d.addObjectDrawerCreateRule( EREmergencyRoom.class, EREmergencyRoomDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERObservationRoom.class, ERObservationRoomDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERSevereInjuryObservationRoom.class, ERSevereInjuryObservationRoomDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERIntensiveCareUnit.class, ERIntensiveCareUnitDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERHighCareUnit.class, ERHighCareUnitDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERGeneralWard.class, ERGeneralWardDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERWaitingRoom.class, ERWaitingRoomDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERXRayRoom.class, ERXRayRoomDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERCTRoom.class, ERCTRoomDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERMRIRoom.class, ERMRIRoomDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERAngiographyRoom.class, ERAngiographyRoomDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERFastRoom.class, ERFastRoomDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERPatientAgent.class, ERPatientAgentDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERNurseAgent.class, ERNurseAgentDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERDoctorAgent.class, ERDoctorAgentDraw3D.class);
//		p3d.addObjectDrawerCreateRule( ERClinicalEngineerAgent.class, ERClinicalEngineerAgentDraw3D.class);
	}

	/**
	 * <PRE>
	 *    終了処理を実行します。
	 * </PRE>
	 * @param erDepartment 救急部門のインスタンス
	 * @throws IOException ファイル処理中の例外
	 */
	private static void vTerminate( ERDepartment erDepartment ) throws IOException
	{
		// スレッドを停止させます。
//		erThreadArrivalPatient.stop();

		// エンジンに登録されているエージェントをすべて解放します。
		engine.clearAgents();

		// 救急部門のシミュレーションの終了処理を行います。
		erDepartment.vTerminate();
		erDepartment = null;

		engine = null;
		env = null;
		geo = null;
	}

	/**
	 * <PRE>
	 *    終了処理を実行します。（逆シミュレーション用）
	 * </PRE>
	 * @throws IOException	ファイル処理中の例外
	 */
	private static void vTerminate() throws IOException
	{
		// スレッドを停止させます。
//		erThreadArrivalPatient.stop();

		// エンジンに登録されているエージェントをすべて解放します。
		engine.clearAgents();

		// 救急部門のシミュレーションの終了処理を行います。
		invSimEngine.vTerminate();

		engine = null;
		env = null;
		geo = null;
		invSimEngine = null;
	}

	/**
	 * <PRE>
	 *     シミュレーションを実行します。
	 * </PRE>
	 * @param iTimeStep シミュレーション実行間隔
	 * @param engine シミュレーションエンジンインスタンス
	 */
	private static void vStart( int iTimeStep, SimulationEngine engine )
	{
		engine.start( iTimeStep );
	}

	/**
	 * <PRE>
	 *     逆シミュレーションを実行します。
	 * </PRE>
	 * @param iTimeStep			シミュレーション実行間隔秒数
	 * @param iIntervalNum		逆シミュレーション実行回数
	 * @throws GenAlgException	GA計算中の例外
	 * @throws IOException		ファイル処理中の例外
	 */
	private static void vStartInvSim( int iTimeStep, int iIntervalNum ) throws GenAlgException, IOException
	{
		double lfRes = 0.0;
		// 開始時間を取得します。
		invSimEngine.vGetStartTime();
		for(int i = 0;i < iIntervalNum; i++ )
		{
			// 逆シミュレーションを実行します。
			lfRes = invSimEngine.lfImplement( iTimeStep );

			// 世代毎に時間を計測していきます。
			invSimEngine.vGetCurrentTime();

			// 10^-8以下になったら終了とします。
			if( lfRes <= 0.00000001 ) break;
		}
		// 最終出力結果を出力します。
		invSimEngine.vOutput(1);
		// 終了時間を計測します。
		invSimEngine.vGetEndTime();
		// 結果を出力します。
		invSimEngine.vOutputElapsedTime();
	}

	/**
	 * <PRE>
	 *    シミュレーション制御用パネルを作成し、実行パネルに追加します。
	 * </PRE>
	 * @param ptCtrl	FuseControler標準の制御用パネルオブジェクト
	 * @param engine	シミュレーション実行エンジンオブジェクト
	 * @param ptWindow	シミュレーション実行画面
	 */
	private static void vCreateControlPanel( FuseControler[] ptCtrl, SimulationEngine engine, FuseWindow[] ptWindow )
	{
		// 操作パネルを作成します。
		ptCtrl[0] = new FuseControler( engine, ptWindow[0] );

		// 追加の操作パネルの設定
//		panel.vInitialize( window, trurl, engine );

		// 追加したパネルの配置を設定します。
		ptWindow[0].add( ptCtrl[0], BorderLayout.WEST );

		// 成型します。
		ptWindow[0].pack();
	}

	/**
	 * <PRE>
	 *    画面表示設定をします。（２次元表示）
	 * </PRE>
	 * @param window シミュレーション実行画面のオブジェクト（２次元）
	 */
	private static void vSetVisible2D( FuseWindow window )
	{
		// 描画周期を設定します。
		window.setRefleshInterval(30);

		// 画面を表示します。
		window.setVisible(true);
	}

	/**
	 * <PRE>
	 *    画面表示設定をします。（３次元表示）
	 * </PRE>
	 * @param window シミュレーション実行画面のオブジェクト（３次元）
	 */
	private static void vSetVisible3D( FuseWindow window )
	{
		// 描画周期を設定します。
		window.setRefleshInterval(30);

		// 画面を表示します。
		window.setVisible(true);
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////テスト用関数（基本的に非公開関数）

	/**
	 * <PRE>
	 *    制約条件の動作確認を行います。(JUNITを使用していないため、手打ち・・・申し訳ない。)
	 * </PRE>
	 * @param initInvSimParam		初期設定ファイル操作クラス
	 * @param cmd					コマンドオプション操作クラス
	 * @param cTRISimLogger			ロガークラスインスタンス
	 * @param strNodeLinkFileName	ノードリンク情報のファイル名
	 */
	private static void vTestConstraintCondition( InitInverseSimParam initInvSimParam, CCmdCheck cmd, Logger cTRISimLogger, String strNodeLinkFileName )
	{
		int iEvaluationIndexMode = 101;
		int iEvaluationIndexCompMode = 1;
		int iConstraintConditionMode = 4;
		int i;

		double plfArg[] = new double[46];

		try
		{
			// デフォルト設定を行います。
			initInvSimParam.vSetDefaultValue();
			// 初期設定ファイルを読み込みます。
			initInvSimParam.readInitSettingFile();

			//> 制約条件の動作確認を行います。

			// 逆シミュレーションの評価指標を設定します。
			if( cmd.strGetEvaluationIndicator().equals("nedocs") == true ) iEvaluationIndexMode = 101;
			else if( cmd.strGetEvaluationIndicator().equals("edworkscore") == true  ) iEvaluationIndexMode = 102;
			else if( cmd.strGetEvaluationIndicator().equals("edwin") == true ) iEvaluationIndexMode = 103;

			// 逆シミュレーションの評価指標比較方法を設定します。
			if( cmd.strGetEvaluationIndexCompMode().equals("avg") == true ) iEvaluationIndexCompMode = 1;
			else if( cmd.strGetEvaluationIndexCompMode().equals("max") == true ) iEvaluationIndexCompMode = 2;

			// 制約条件を設定します。

//			for( i = 0; i < plfArg.length; i++ )
//			{
//			}

			// テスト用
			plfArg[0] = 12;
			plfArg[1] = 15;
			plfArg[2] = 8;
			plfArg[3] = 0;
			plfArg[4] = 0;
			plfArg[5] = 1;
			plfArg[6] = 1;
			plfArg[7] = 1;
			plfArg[8] = 1;
			plfArg[9] = 5;
			plfArg[10] = 4;
			plfArg[11] = 5;
			plfArg[12] = 2;
			plfArg[13] = 1;
			plfArg[14] = 1;
			plfArg[15] = 2;
			plfArg[16] = 2;
			plfArg[17] = 4;
			plfArg[18] = 2;
			plfArg[19] = 6;
			plfArg[20] = 0;
			plfArg[21] = 0;
			plfArg[22] = 0;
			plfArg[23] = 2;
			plfArg[24] = 22;
			plfArg[25] = 2;
			plfArg[26] = 8;
			plfArg[27] = 1;
			plfArg[28] = 744;
			plfArg[29] = 8;
			plfArg[30] = 1;
			plfArg[31] = 1;
			plfArg[32] = 1;
			plfArg[33] = 1;
			plfArg[34] = 1;

			for( i = 0;i < 10; i++ )
			{
				ConstraintConditionInterface pflfConstraintCondition;
				pflfConstraintCondition = new ConstraintCondition( iConstraintConditionMode, initInvSimParam );
				pflfConstraintCondition.vSetConditionMode( iConstraintConditionMode );
				pflfConstraintCondition.vConstraintCondition(plfArg);


			}
			//< 制約条件の動作確認を行います。

			// 終了処理を実行します。
			vTerminate();
		}
		catch( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}

}
