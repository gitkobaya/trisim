/**
 * @file CCmdCheck.java
 * @brief コマンドライン解析クラスです。
 *        コマンドを入力するとコマンドを解析して、必要なパラメータを取得します。<br>
 *        lCCmdCheck関数を呼び出し、コマンドを解析して必要に応じてGet関数によりパラメータを取得します。
 *        コマンドが合わない場合や未知のコマンドが入力されると-1を返却します。
 * @date  2017/08/18
 * @author kobayashi
 */

package utility.cmd;

import inverse.optimization.objectivefunction.ObjectiveFunctionInterface;

/**
 * コマンドライン解析クラスです。
 * コマンドを入力するとコマンドを解析して、必要なパラメータを取得します。<br>
 * lCCmdCheck関数を呼び出し、コマンドを解析して必要に応じてGet関数によりパラメータを取得します。
 * コマンドが合わない場合や未知のコマンドが入力されると-1を返却します。
 *
 * 使用方法は以下の次の通りです。<br>
 * コマンド解析　lCCmdCheck　<br>
 * 解析後　　　　Get関数を使用してパラメータを取得。<br>
 *
 * @author kobayashi
 */

public class CCmdCheck
{
	private final int CCMD_SUCCESS				= 0;
	private final int CCMD_ERROR_INVALID_FORMAT	= -1;
	private final int CCMD_ERROR_INVALID_DATA		= -2;
	private final int CCMD_ERROR_MEMORY_ALLOCATE 	= -3;
	private final int CCMD_ERROR_MEMORY_RELEASE	= -4;

	private String strEmergencyDepartmentFileName = "";			// 救急部門用ファイルパス
	private String strConsultationRoomFileName = "";			// 診察室用ファイルパス
	private String strOperationRoomFileName = "";				// 手術用ファイルパス
	private String strEmergencyRoomFileName = "";				// 初療室用ファイルパス
	private String strObservationRoomFileName = "";				// 観察室用ファイルパス
	private String strSevereInjuryObservationRoomFileName = "";	// 重症観察室用ファイルパス
	private String strIntensiveCareUnitFileName = "";			// 集中治療室用ファイルパス
	private String strHighCareUnitFileName = "";				// 高度治療室用ファイルパス
	private String strGeneralWardFileName = "";					// 一般病棟用ファイルパス
	private String strWaitingRoomFileName = "";					// 待合室用ファイルパス
	private String strXRayRoomFileName = "";					// X線室用ファイルパス
	private String strCTRoomFileName = "";						// CT室用ファイルパス
	private String strMRIRoomFileName = "";						// MRI室用ファイルパス
	private String strAngiographyRoomFileName = "";				// 血管造影室用ファイルパス
	private String strFastRoomFileName = "";					// FAST室用ファイルパス
	private String strNodeLinkFileName = "";					// ノード、リンクが記述されたファイル名

	private int iEndSimulationTime;								// シミュレーション終了時間
	private int iExecMode;										// TRISimの実行モード（CUIかGUIか）
	private int iSimulationTimeStep;							// TRISimのシミュレーション実行間隔
	private double lfPatientPepole;								// 患者の到達人数
	private int iPatientRandomMode;								// 患者の傷病状態を生成する乱数器の変更（0:一様乱数、1:正規乱数）

	private int iInverseSimulationIntervalNumber;				// 逆シミュレーションの回数
	private int iInverseSimulationMethod;						// 逆シミュレーション手法
	private String strEvaluationIndicator = "";					// 逆シミュレーション用評価関数
	private String strEvaluationIndexCompMode;					// 評価指標の比較方法の設定（1:全体平均, 2:シミュレーション中の最大値）
	private ObjectiveFunctionInterface objFunctionInterface;	// 逆シミュレーション用評価関数インターフェース
	private int iGensNumber;									// 逆シミュレーション用遺伝子数
	private int iGensVectorDimension;							// 逆シミュレーション用特徴量次元数

	// 人工蜂コロニー法
	private int iAbcMethod;										// 逆シミュレーション人工蜂コロニー法
	private int iAbcSearchNumber;								// 人工蜂コロニー探索領域数
	private int iAbcLimitCount;									// 人工蜂コロニー探索休止限界数
	private int iAbcIntervalMinNum;								// 最低反復回数
	private int iAbcUpperSearchNum;								// 探索点上位数
	private double lfAbcConvergenceParam;						// 収束状況パラメータ
	private double lfAbcFitBound;								// 適合度許容限界値
	private double lfAbcFitAccuracy;							// 適合度評価精度

	// 実数値ＧＡ
	private int iRcGaMethod;									// 逆シミュレーション実数値ＧＡ手法

	// 遺伝的アルゴリズム
	private int iGaMethod;										// 逆シミュレーション遺伝的アルゴリズム手法

	// 粒子群最適化法
	private int iPsoMethod;										// 逆シミュレーション粒子群最適化法

	private int iFileWriteMode;									// 長時間シミュレーション用ファイル出力

	private int iPatientArrivalMode;							// 患者の到達モード(0:通常, 1:災害)
	private int iGenerationPatientMode;							// 患者生成モード(0:シミュレーション開始前にあらかじめ生成　1:別スレッドから生成)

	private int iInitIntensiveCareUnitPatientNum;				// 初期からいるICUの患者数
	private int iInitHighCareUnitPatientNum;					// 初期からいるHCUの患者数
	private int iInitGeneralWardPatientNum;						// 初期からいる一般病棟の患者数


	public CCmdCheck()
	{
		iEndSimulationTime = 178000;							// シミュレーション終了時間
		iExecMode = 1;											// TRISimの実行モード（CUIかGUIか）
		iSimulationTimeStep = 1000;								// TRISimのシミュレーション実行間隔
		lfPatientPepole = 365.0;								// 患者の到達人数
		iPatientRandomMode = 0;									// 一様乱数
		iFileWriteMode = 0;
		iPatientArrivalMode = 0;
		iGenerationPatientMode = 0;
	}

	/**
	 *<PRE>
	 *  入力されたコマンドをチェック
	 *  ver 0.1 初版
	 *  ver 0.2 オプション等の追加、およびそのほかの修正。
	 *  ver 0.3 人工知能用グラフ生成ツール作成のため新たに修正。
	 *</PRE>
	 * @param args	入力したコマンドの詳細
	 * @return	CCMD_SUCCESS
	 * 			CCMD_ERROR_INVALID_FORMAT
	 * 			CCMD_ERROR_INVALID_DATA
	 * 			CCMD_ERROR_MEMORY_ALLOCATE
	 * 			CCMD_ERROR_MEMORY_RELEASE
	 * @author kobayashi
	 * @since 0.1 2014/05/02
	 * @version 0.1
	 */
	public long lCommandCheck( String args[] )
	{
		int i;
		long lRet = 0;

		/* コマンドのチェック */
		/* コマンドのチェック */
		if( args == null )
		{
			return CCMD_ERROR_INVALID_FORMAT;
		}
		if( args.length <= 0 )
		{
			return CCMD_ERROR_INVALID_FORMAT;
		}
		for( i=0; i<args.length ;i++ )
		{
			/* 救急部門 */
			if( args[i].equals( "-ed" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strEmergencyDepartmentFileName = args[i+1];
				i++;
			}
			/* 診察室 */
			else if( args[i].equals( "-cr" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strConsultationRoomFileName = args[i+1];
				i++;
			}
			/* 手術室 */
			else if( args[i].equals( "-opr" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strOperationRoomFileName = args[i+1];
				i++;
			}
			/* 初療室 */
			else if( args[i].equals( "-er" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strEmergencyRoomFileName = args[i+1];
				i++;
			}
			/* 観察室 */
			else if( args[i].equals( "-or" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strObservationRoomFileName = args[i+1];
				i++;
			}
			/* 重傷観察室 */
			else if( args[i].equals( "-sior" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strSevereInjuryObservationRoomFileName = args[i+1];
				i++;
			}
			/* 集中治療室 */
			else if( args[i].equals( "-icu" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strIntensiveCareUnitFileName = args[i+1];
				i++;
			}
			/* 高度治療室 */
			else if( args[i].equals( "-hcu" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strHighCareUnitFileName = args[i+1];
				i++;
			}
			/* 一般病棟 */
			else if( args[i].equals( "-gw" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strGeneralWardFileName = args[i+1];
				i++;
			}
			/* 待合室 */
			else if( args[i].equals( "-wr" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strWaitingRoomFileName = args[i+1];
				i++;
			}
			/* X線室 */
			else if( args[i].equals( "-xr" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strXRayRoomFileName = args[i+1];
				i++;
			}
			/* CT室 */
			else if( args[i].equals( "-ct" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strCTRoomFileName = args[i+1];
				i++;
			}
			/* MRI室 */
			else if( args[i].equals( "-mri" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strMRIRoomFileName = args[i+1];
				i++;
			}
			/* 血管造影室 */
			else if( args[i].equals( "-agr" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strAngiographyRoomFileName = args[i+1];
				i++;
			}
			/* FAST室 */
			else if( args[i].equals( "-fast" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strFastRoomFileName = args[i+1];
				i++;
			}
			/* 終了時間 */
			else if( args[i].equals( "-et" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iEndSimulationTime = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 実行モード */
			else if( args[i].equals( "-mode" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iExecMode = Integer.parseInt( args[i+1] );
				i++;
			}
			/* シミュレーションタイムステップ */
			else if( args[i].equals( "-ts" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iSimulationTimeStep = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 患者の到達人数（概算） */
			else if( args[i].equals( "-pp" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				lfPatientPepole = Double.parseDouble( args[i+1] );
				i++;
			}
			/* 生成する乱数のモード */
			else if( args[i].equals( "-prm" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iPatientRandomMode = Integer.parseInt( args[i+1] );
				i++;
			}
			/* ノードリンクファイルの読み込み */
			else if( args[i].equals( "-nlf" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strNodeLinkFileName = args[i+1];
				i++;
			}
			/* 逆シミュレーションの繰り返し回数 */
			else if( args[i].equals( "-nis" ) == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iInverseSimulationIntervalNumber = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用評価指標 */
			else if( args[i].equals("-ei") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strEvaluationIndicator = args[i+1];
				i++;
			}
			/* 逆シミュレーション用ABC法のモード */
			else if( args[i].equals("-abc") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iAbcMethod = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用ABC法の探索領域数 */
			else if( args[i].equals("-abcsn") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iAbcSearchNumber = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用ABC法の探索領域数 */
			else if( args[i].equals("-abclc") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iAbcLimitCount = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用ABC法の探索最低反復回数 */
			else if( args[i].equals("-abcimn") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iAbcIntervalMinNum = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用ABC法の収束状況パラメータ */
			else if( args[i].equals("-abccp") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				lfAbcConvergenceParam = Double.parseDouble( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用ABC法の探索点上位数 */
			else if( args[i].equals("-abcusn") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iAbcUpperSearchNum = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用ABC法の適合度許容限界値 */
			else if( args[i].equals("-abcfb") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				lfAbcFitBound = Double.parseDouble( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用ABC法の適合度評価精度 */
			else if( args[i].equals("-abcfa") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				lfAbcFitAccuracy = Double.parseDouble( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用PSO法のモード */
			else if( args[i].equals("-pso") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iPsoMethod = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用GAのモード */
			else if( args[i].equals("-ga") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iGaMethod = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用実数値GAのモード */
			else if( args[i].equals("-rcga") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iRcGaMethod = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用1遺伝子の次元数 */
			else if( args[i].equals("-invdn") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iGensVectorDimension = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用遺伝子数 */
			else if( args[i].equals("-invn") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iGensNumber = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用利用アルゴリズム */
			else if( args[i].equals("-invm") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iInverseSimulationMethod = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 長時間シミュレーション用ファイル書き込みフラグ */
			else if( args[i].equals("-ifw") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iFileWriteMode = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 患者到達モード */
			else if( args[i].equals("-pam") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iPatientArrivalMode = Integer.parseInt( args[i+1] );
				i++;
			}
			/* 逆シミュレーション用評価指標の比較方法 */
			else if( args[i].equals("-eic") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				strEvaluationIndexCompMode = args[i+1];
				i++;
			}
			/* 患者生成モード(0:シミュレーション開始時に生成 1:別スレッドからリアルタイムに生成) */
			else if( args[i].equals("-gap") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iGenerationPatientMode = Integer.parseInt(args[i+1]);
				i++;
			}
			/* 初期一般病棟の患者生成人数 */
			else if( args[i].equals("-gwpn") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iInitGeneralWardPatientNum = Integer.parseInt(args[i+1]);
				i++;
			}
			/* 初期集中治療室の患者生成人数 */
			else if( args[i].equals("-icupn") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iInitIntensiveCareUnitPatientNum = Integer.parseInt(args[i+1]);
				i++;
			}
			/* 初期高度治療室の患者生成人数 */
			else if( args[i].equals("-hcupn") == true )
			{
				lRet = lCommandErrorCheck( args[i] );
				if( lRet != 0 ) return lRet;
				iInitHighCareUnitPatientNum = Integer.parseInt(args[i+1]);
				i++;
			}
			else
			{
				lRet = CCMD_ERROR_INVALID_DATA;
				vHelp();
				break;
			}
		}
		return lRet;
	}

	/**
	 *<PRE>
	 * 入力オプションかどうかをチェックする
	 * ver 0.1 新規作成
	 * ver 0.2 人工知能用グラフ生成ツール作成用に修正。
	 *</PRE>
	 * @param arg コマンド
	 * @return 0
	 *		  -1
	 *        -2
	 * @author kobayashi
	 * @since 2013/1/1
	 * @version 0.2
	 */
	private long lCommandErrorCheck( String arg )
	{
		long lRet = 0L;
		if( arg.equals( "-ed" ) == true 	||
			arg.equals( "-cr" ) == true 	||
			arg.equals( "-opr" ) == true 	||
			arg.equals( "-er" ) == true 	||
			arg.equals( "-or" ) == true 	||
			arg.equals( "-sior" ) == true 	||
			arg.equals( "-icu" ) == true 	||
			arg.equals( "-hcu" ) == true 	||
			arg.equals( "-gw" ) == true 	||
			arg.equals( "-wr" ) == true 	||
			arg.equals( "-xr" ) == true 	||
			arg.equals( "-ct" ) == true 	||
			arg.equals( "-mri" ) == true 	||
			arg.equals( "-agr" ) == true 	||
			arg.equals( "-fast" ) == true 	||
			arg.equals( "-et" ) == true 	||
			arg.equals( "-mode" ) == true	||
			arg.equals( "-pp" ) == true		||
			arg.equals( "-prm" ) == true	||
			arg.equals( "-nlf" ) == true	||
			arg.equals( "-ts" ) == true 	||
			arg.equals( "-nis" ) == true	||
			arg.equals( "-invdn" ) == true	||
			arg.equals( "-invn" ) == true	||
			arg.equals( "-invm" ) == true	||
			arg.equals( "-ga" ) == true		||
			arg.equals( "-abc" ) == true	||
			arg.equals( "-abcsn" ) == true	||
			arg.equals( "-abclc" ) == true	||
			arg.equals( "-abcimn" ) == true	||
			arg.equals( "-abccp" ) == true	||
			arg.equals( "-abcusn" ) == true	||
			arg.equals( "-abcfb" ) == true	||
			arg.equals( "-abcfa" ) == true	||
			arg.equals( "-pso" ) == true	||
			arg.equals( "-rcga" ) == true	||
			arg.equals( "-ifw" ) == true	||
			arg.equals( "-pam" ) == true	||
			arg.equals( "-ei" ) == true		||
			arg.equals( "-eic" ) == true	||
			arg.equals( "-gap" ) == true	||
			arg.equals( "-gwpn" ) == true	||
			arg.equals( "-icupn" ) == true	||
			arg.equals( "-hcupn" ) == true )
		{
			lRet = 0;
		}
		else
		{
			lRet = -2;
		}
		return lRet;
	}

	/**
	 *<PRE>
	 * 使用方法を表示する。
	 *</PRE>
	 * @author kobayashi
	 * @since 2015/6/10
	 * @version 0.2
	 */
	public void vHelp()
	{
		System.out.println("トリアージシミュレーションTRISim");
		System.out.println("使用方法");
		System.out.println("triage [-ed][-cr][-opr][-er][-or][-sior][-icu][-hcu][-gw][-wr][-xr][-ct][-mri][-agr][-fast][-et][-mode][-ts][-pp][-prm][-nlf][-nis][-invm][-invn][-invdn][-ga][-rcga][-pso][-abc][-abcsn][-abclc][-abcusn][-abcimn][-abccp][-abcfa][-abcfb][-ei]");
		System.out.println("-ed 救急部門のパラメータ定義ファイル");
		System.out.println("-cr 診察室のパラメータ定義ファイル");
		System.out.println("-opr 手術室のパラメータ定義ファイル");
		System.out.println("-er 初療室のパラメータ定義ファイル");
		System.out.println("-or 観察室のパラメータ定義ファイル");
		System.out.println("-sior 重傷観察室のパラメータ定義ファイル");
		System.out.println("-icu 集中治療室のパラメータ定義ファイル");
		System.out.println("-hcu 高度治療室のパラメータ定義ファイル");
		System.out.println("-gw 一般病棟のパラメータ定義ファイル");
		System.out.println("-wr 待合室のパラメータ定義ファイル");
		System.out.println("-xr X線室のパラメータ定義ファイル");
		System.out.println("-ct CT室のパラメータ定義ファイル");
		System.out.println("-mri MRI室のパラメータ定義ファイル");
		System.out.println("-agr 血管造影室のパラメータ定義ファイル");
		System.out.println("-fast FAST室のパラメータ定義ファイル");
		System.out.println("-et シミュレーション終了時間");
		System.out.println("-mode シミュレーションの実行モード 0：GUI, 1:CUI, 2:逆シミュレーション");
		System.out.println("-ts シミュレーションのタイムステップ （ミリ秒単位で指定）");
		System.out.println("-pp 患者の到達人数（概算指定）");
		System.out.println("-prm 患者の傷病状態の乱数生成方法(0:一様乱数 1:正規乱数, 2:ワイブル分布)");
		System.out.println("-nlf ノードリンク定義ファイル");
		System.out.println("-nis 逆シミュレーション繰り返し回数");
		System.out.println("-invm 逆シミュレーションの利用アルゴリズム 1:遺伝的アルゴリズム, 2:実数値GA, 3:粒子群最適化法, 4:人工蜂コロニー法");
		System.out.println("-invn 逆シミュレーションの構成遺伝子数");
		System.out.println("-invdn 逆シミュレーションの構成遺伝子の次元数");
		System.out.println("-ga 遺伝的アルゴリズムのアルゴリズム 1:ルーレット法, 2:ランキング法, 3:トーナメント法");
		System.out.println("-rcga 実数値GAのアルゴリズム 1:UNDX, 2:REX, 3:REC-star, 4:AREX");
		System.out.println("-pso 粒子群最適化法のアルゴリズム 0:通常, 1:");
		System.out.println("-abc 人工蜂コロニー法のアルゴリズム 1:通常, 2:改善手法, 3:交叉を利用したABC法, 4:提案手法");
		System.out.println("-abcsn ABC法の探索点数");
		System.out.println("-abclc ABC法の未更新探索ステップ数");
		System.out.println("-abcusn ABC法の適合度上位数");
		System.out.println("-abcimn ABC法の最低反復回数");
		System.out.println("-abccp ABC法の解への収束状況を表す変数(0～1で設定)");
		System.out.println("-abcfa 適応度の精度値");
		System.out.println("-abcfb 適応度の許容限界値");
		System.out.println("-ei 逆シミュレーションの評価指標");
		System.out.println("-eic 逆シミュレーションの評価指標の比較方法");
	}

	/**
	 * <PRE>
	 *    救急部門のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 救急部門のファイル名
	 */
	public String strGetEmergencyDepartmentPath()
	{
		return strEmergencyDepartmentFileName;
	}

	/**
	 * <PRE>
	 *    診察室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 診察室のファイル名
	 */
	public String strGetConsultationRoomPath()
	{
		return strConsultationRoomFileName;
	}

	/**
	 * <PRE>
	 *    手術室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 手術室のファイル名
	 */
	public String strGetOperationRoomPath()
	{
		return strOperationRoomFileName;
	}

	/**
	 * <PRE>
	 *    初療室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 初療室のファイル名
	 */
	public String strGetEmergencyRoomPath()
	{
		return strEmergencyRoomFileName;
	}

	/**
	 * <PRE>
	 *    観察室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 観察室のファイル名
	 */
	public String strGetObservationRoomPath()
	{
		return strObservationRoomFileName;
	}

	/**
	 * <PRE>
	 *    重傷観察室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 重症観察室のファイル名
	 */
	public String strGetSevereInjuryObservationRoomPath()
	{
		return strSevereInjuryObservationRoomFileName;
	}

	/**
	 * <PRE>
	 *    集中治療室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 集中治療室のファイル名
	 */
	public String strGetIntensiveCareUnitPath()
	{
		return strIntensiveCareUnitFileName;
	}

	/**
	 * <PRE>
	 *    高度治療室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 高度治療室のファイル名
	 */
	public String strGetHighCareUnitPath()
	{
		return strHighCareUnitFileName;
	}

	/**
	 * <PRE>
	 *    一般病棟のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 一般病棟のファイル名
	 */
	public String strGetGeneralWardPath()
	{
		return strGeneralWardFileName;
	}

	/**
	 * <PRE>
	 *    待合室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 待合室のファイル名
	 */
	public String strGetWaitingRoomPath()
	{
		return strWaitingRoomFileName;
	}

	/**
	 * <PRE>
	 *    X線室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return X線室のファイル名
	 */
	public String strGetXRayRoomPath()
	{
		return strXRayRoomFileName;
	}

	/**
	 * <PRE>
	 *    CT室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return CT室のファイル名
	 */
	public String strGetCTRoomPath()
	{
		return strCTRoomFileName;
	}

	/**
	 * <PRE>
	 *    ＭＲＩ室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return MRI室のファイル名
	 */
	public String strGetMRIRoomPath()
	{
		return strMRIRoomFileName;
	}

	/**
	 * <PRE>
	 *    血管造影室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 血管造影室のファイル名
	 */
	public String strGetAngiographyRoomPath()
	{
		return strAngiographyRoomFileName;
	}

	/**
	 * <PRE>
	 *    FAST室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return FAST室のファイル名
	 */
	public String strGetFastRoomPath()
	{
		return strFastRoomFileName;
	}

	/**
	 * <PRE>
	 *    ノードリンクが記述されたファイルを取得します。
	 * </PRE>
	 * @return ノードリンク記述ファイル名
	 */
	public String strGetNodeLinkPath()
	{
		return strNodeLinkFileName;
	}

	/**
	 * <PRE>
	 *    シミュレーション終了時間を取得します。
	 * </PRE>
	 * @return シミュレーション終了時間
	 */
	public int iGetEndSimulationTime()
	{
		return iEndSimulationTime;
	}

	/**
	 * <PRE>
	 *    シミュレーション実行モードを取得します。
	 *    0 コンソールモード
	 *    1 GUIモード
	 *    2 逆シミュレーションモード
	 * </PRE>
	 * @return シミュレーション実行モード
	 */
	public int iGetExecMode()
	{
		return iExecMode;
	}

	/**
	 * <PRE>
	 *    シミュレーションの実行間隔を取得します。
	 *    秒で指定します。
	 * </PRE>
	 * @return シミュレーション実行間隔
	 */
	public int iGetSimulationTimeStep()
	{
		return iSimulationTimeStep;
	}

	/**
	 * <PRE>
	 *    患者の到達人数を取得します。
	 * </PRE>
	 * @return 到達人数
	 */
	public double lfGetPatientPepole()
	{
		return lfPatientPepole;
	}

	/**
	 * <PRE>
	 *    乱数の生成方法のモードを取得します。
	 * </PRE>
	 * @return 乱数生成モード
	 */
	public int iGetPatientRandomMode()
	{
		return iPatientRandomMode;
	}

	/**
	 * <PRE>
	 *    逆シミュレーションの繰り返し回数を取得します。
	 * </PRE>
	 * @return 逆シミュレーション繰り返し回数
	 */
	public int iGetInverseSimulationIntervalNumber()
	{
		return iInverseSimulationIntervalNumber;
	}


	/**
	 * <PRE>
	 *    逆シミュレーション手法を取得します。
	 * </PRE>
	 * @return 逆シミュレーションエンジン
	 */
	public int iGetInverseSimulationMethod()
	{
		return iInverseSimulationMethod;
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用の評価指標を取得します。
	 * </PRE>
	 * @return 評価指標
	 */
	public String strGetEvaluationIndicator()
	{
		return strEvaluationIndicator;
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用の評価指標を取得します。
	 * </PRE>
	 * @return 評価指標のインターフェース
	 */
	public ObjectiveFunctionInterface objGetObjectiveFunctionInterface()
	{
		return objFunctionInterface;
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用の遺伝子数を取得します。
	 * </PRE>
	 * @return 遺伝子あるいは粒子数
	 */
	public int iGetGensNumber()
	{
		return iGensNumber;
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用の遺伝子の次元数を取得します。
	 * </PRE>
	 * @return 遺伝子あるいは粒子の次元数
	 */
	public int iGetGensVectorDimension()
	{
		return iGensVectorDimension;
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用のABC法を取得します。
	 * </PRE>
	 * @return ABC法のモード
	 */
	public int iGetAbcMethod()
	{
		return iAbcMethod;
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用のABC法探索領域数を取得します。
	 * </PRE>
	 * @return ABC法探索領域数
	 */
	public int iGetAbcSearchNumber()
	{
		return iAbcSearchNumber;
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用のABC法非更新回数を取得します。
	 * </PRE>
	 * @return ABC探索非更新回数
	 */
	public int iGetAbcLimitCount()
	{
		return iAbcLimitCount;
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用の粒子群最適化法を取得します。
	 * </PRE>
	 * @return 粒子群最適化法モード
	 */
	public int iGetPsoMethod()
	{
		return iPsoMethod;
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用の遺伝的アルゴリズムを取得します。
	 * </PRE>
	 * @return 遺伝的アルゴリズムモード
	 */
	public int iGetGaMethod()
	{
		return iGaMethod;
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用の実数値ＧＡを取得します。
	 * </PRE>
	 * @return 実数値GAのモード
	 */
	public int iGetRcGaMethod()
	{
		return iRcGaMethod;
	}


	/**
	 * <PRE>
	 *    最小反復回数を取得します。
	 * </PRE>
	 * @return 最小反復回数パラメータ（高精度化ABC法）
	 */
	public int iGetAbcIntervalMinNum()
	{
		return iAbcIntervalMinNum;
	}

	/**
	 * <PRE>
	 *    探索点上位数
	 * </PRE>
	 * @return 探索点数上位数パラメータ（高精度化ABC法）
	 */
	public int iGetAbcUpperSearchNum()
	{
		// 探索点上位数
		return iAbcUpperSearchNum;
	}

	/**
	 * <PRE>
	 *    収束状況パラメータ
	 * </PRE>
	 * @return 収束状況パラメータ（高精度化ABC法）
	 */
	public double lfGetAbcConvergenceParam()
	{
		// 収束状況パラメータ
		return lfAbcConvergenceParam;
	}

	/**
	 * <PRE>
	 *    適合度許容限界値パラメータ
	 * </PRE>
	 * @return 適合度許容限界値パラメータ（高精度化ABC法）
	 */
	public double lfGetAbcFitBound()
	{
		// 適合度許容限界値
		return lfAbcFitBound;
	}

	/**
	 * <PRE>
	 *    適合度評価制度パラメータ
	 * </PRE>
	 * @return 適合度評価パラメータ（高精度化ABC法）
	 */
	public double lfGetAbcFitAccuracy()
	{
		// 適合度評価精度
		return lfAbcFitAccuracy;
	}

	/**
	 * <PRE>
	 *    長時間シミュレーションファイル書き込みフラグ
	 * </PRE>
	 * @return ファイル書き込みモード
	 */
	public int iGetFileWriteMode()
	{
		return iFileWriteMode;
	}

	/**
	 * <PRE>
	 *    患者到達モードフラグ
	 *    0:通常
	 *    1:災害
	 * </PRE>
	 * @return ファイル書き込みモード
	 */
	public int iGetPatientArrivalMode()
	{
		return iPatientArrivalMode;
	}

	/**
	 * <PRE>
	 *    逆シミュレーション用評価指標の比較方法を取得します。
	 * </PRE>
	 * @return 逆シミュレーション比較方法
	 */
	public String strGetEvaluationIndexCompMode()
	{
		return strEvaluationIndexCompMode;
	}

	/**
	 * <PRE>
	 *    患者生成モードの取得をします。
	 * </PRE>
	 * @return 患者生成モード
	 */
	public int iGetGenerationPatientMode()
	{
		return iGenerationPatientMode;
	}

	/**
	 * <PRE>
	 *    初期化時のICU患者数を取得をします。
	 * </PRE>
	 * @return 初期からいるICUの患者数
	 */
	public int iGetInitIntensiveCareUnitPatientNum()
	{
		// 初期からいるICUの患者数
		return iInitIntensiveCareUnitPatientNum;
	}

	/**
	 * <PRE>
	 *    初期化時のHCU患者数を取得をします。
	 * </PRE>
	 * @return 初期からいるHCUの患者数
	 */
	public int iGetInitHighCareUnitPatientNum()
	{
		// 初期からいるHCUの患者数
		return iInitHighCareUnitPatientNum;
	}

	/**
	 * <PRE>
	 *    初期化時の一般病棟患者数を取得をします。
	 * </PRE>
	 * @return 初期からいる一般病棟の患者数
	 */
	public int iGetInitGeneralWardPatientNum()
	{
		// 初期からいる一般病棟の患者数
		return iInitGeneralWardPatientNum;
	}
}

