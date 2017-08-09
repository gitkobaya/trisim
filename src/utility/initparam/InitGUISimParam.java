package utility.initparam;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.logging.Level;

import utility.initparam.initsettingfile.InitSettingFileRead;

public class InitGUISimParam extends InitSettingFileRead
{
	private int iEndSimulationTime;								// シミュレーション終了時間
	private int iExecMode;										// TRISimの実行モード（CUIかGUIか）
	private int iSimulationTimeStep;							// TRISimのシミュレーション実行間隔
	private double lfPatientPepole;								// 患者の到達人数
	private int iPatientRandomMode;								// 患者の傷病状態を生成する乱数器の変更（0:一様乱数、1:正規乱数）
	private int iFileWriteMode;									//

	// 地形の作成(縦横の1メッシュのサイズを指定。)
	private int iMeshWidth;
	private int iMeshHeight;
	// 縦横のメッシュ数を指定
	private int iMeshWidthNum;
	private int iMeshHeightNum;

	// 救急部門のパラメータ設定を行います。
	private String strEmergencyDepartmentPath 		 	= "./parameter/ER.csv";
	private String strConsultationRoomPath			 	= "./parameter/診察室.csv";
	private String strOperationRoomPath				 	= "./parameter/手術室.csv";
	private String strEmergencyRoomPath				 	= "./parameter/初療室.csv";
	private String strObservationRoomPath			 	= "./parameter/観察室.csv";
	private String strSevereInjuryObservationRoomPath	= "./parameter/重症観察室.csv";
	private String strIntensiveCareUnitPath				= "./parameter/ICU.csv";
	private String strHighCareUnitPath 					= "./parameter/HCU.csv";
	private String strGeneralWardPath					= "./parameter/一般病棟.csv";
	private String strWaitingRoomPath					= "./parameter/待合室.csv";
	private String strXRayRoomPath						= "./parameter/X線室.csv";
	private String strCTRoomPath						= "./parameter/CT室.csv";
	private String strMRIRoomPath						= "./parameter/MRI室.csv";
	private String strAngiographyRoomPath				= "./parameter/血管造影室.csv";
	private String strFastRoomPath						= "./parameter/FAST室.csv";
	private int iPatientPepole = 365;

	// 救急部門の合わせ込みパラメータの読み込みを行います。

	// 救急部門の施設配置を描画します。
	private String strEmergencyDepartmentAxisPath = "./parameter/ER配置.csv";
	private String strConsultationRoomAxisPath = "./parameter/診察室配置.csv";
	private String strOperationRoomAxisPath = "./parameter/手術室配置.csv";
	private String strEmergencyRoomAxisPath = "./parameter/初療室配置.csv";
	private String strObservationRoomAxisPath = "./parameter/観察室配置.csv";
	private String strSevereInjuryObservationRoomAxisPath = "./parameter/重症観察室配置.csv";
	private String strIntensiveCareUnitAxisPath = "./parameter/ICU配置.csv";
	private String strHighCareUnitAxisPath = "./parameter/HCU配置.csv";
	private String strGeneralWardAxisPath = "./parameter/一般病棟配置.csv";
	private String strWaitingRoomAxisPath = "./parameter/待合室配置.csv";
	private String strXRayRoomAxisPath = "./parameter/X線室配置.csv";
	private String strCTRoomAxisPath = "./parameter/CT室配置.csv";
	private String strMRIRoomAxisPath = "./parameter/MRI室配置.csv";
	private String strAngiographyRoomAxisPath = "./parameter/血管造影室配置.csv";
	private String strFastRoomAxisPath = "./parameter/FAST室配置.csv";
	private String strStairsAxisPath = "./parameter/階段配置.csv";
	private String strElevatorAxisPath = "./parameter/エレベーター配置.csv";
	private String strOtherRoomAxisPath = "./parameter/その他部屋配置.csv";

	// ノード及びリンクの設定をします。
	private String strNodeLinkFileName = "./parameter/TriageNodeLinkTable.txt";

	/**
	 * <PRE>
	 *	*.iniファイルに設定するパラメーターのデフォルト値を設定します。
	 * </PRE>
	 *
	 *	@author kobayashi
	 *	@since	0.1 2017/3/2
	 *	@version 0.1
	 */
	public void vSetDefaultValue()
	{
	// デフォルト値
		iEndSimulationTime = 178000;		// シミュレーション終了時間(秒で指定:86400で1日)
		iExecMode = 1;						// TRISimの実行モード（CUIモード、GUIモード、逆シミュレーションモードか）
		iSimulationTimeStep = 10000;		// TRISimのシミュレーション実行間隔(ミリ秒で指定)
		lfPatientPepole = 365.0;			// 患者の到達人数
		iPatientRandomMode = 0;				// 一様乱数
		iFileWriteMode = 0;					// 各患者の行動データファイルの出力方法(0:出力せず、1:1ステップごとに出力、2:最初及び最後の数ステップを出力)

		// 地形の作成(縦横の1メッシュのサイズを指定。)
		iMeshWidth = 10;
		iMeshHeight = 10;

		// 縦横のメッシュ数を指定
		iMeshWidthNum = 280;
		iMeshHeightNum = 250;
	}

	/**
	 * <PRE>
	 *	*.iniファイルに設定された値を読み込みます。
	 * </PRE>
	 * @throws IllegalArgumentException   設定パラメータにフォーマット誤りがある場合に例外
	 * @throws NoSuchFileException        ファイルパス指定のものに関して例外
	 * @throws IOException                ファイル読み込み時にエラーが発生した場合に例外
	 * @author kobayashi
	 * @since	2017/3/2
	 * @version 0.1
	 */
	public void readInitSettingFile() throws IllegalArgumentException, IOException
	{
		String func_name				= "GetEnvParameter";
		int i = 0;
		String strIniFullPath;
		String strIniFileName			= "erGUIEV.ini";
		String strLogSectionName		= "LogData";
		String strInitParamSectionName	= "InitParameter";
		String strGeometrySectionName	= "Geometry";
		String strErSectionName			= "EmergencyDepartment";
		String strErDrawSectionName		= "EmergencyDepartmentDraw";
		String strParam;
		long lRet = 0L;
		double lfRet = 0.0;
		File file;
		File fPathCheck;

		file = new File( strIniFileName );
		strIniFullPath = file.getAbsolutePath( );

	//描画用ファイルの指定

		//救急部門の保存先設定
		strParam = GetInitDataString( strErSectionName, "EmergencyDepartmentPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.exists() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("emergency department setting file is none"));
		}
		fPathCheck = null;
		strEmergencyDepartmentPath = strParam;

		//診察室の保存先設定
		strParam = GetInitDataString( strErSectionName, "ConsultationRoomPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("consultation room setting file is none"));
		}
		fPathCheck = null;
		strConsultationRoomPath = strParam;

		//手術室の保存先設定
		strParam = GetInitDataString( strErSectionName, "OperationRoomPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("operation room setting file is none"));
		}
		fPathCheck = null;
		strOperationRoomPath = strParam;

		//初療室の保存先設定
		strParam = GetInitDataString( strErSectionName, "EmergencyRoomPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("emergency room setting file is none"));
		}
		fPathCheck = null;
		strEmergencyRoomPath = strParam;

		//観察室の保存先設定
		strParam = GetInitDataString( strErSectionName, "ObservationRoomPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("observation room setting file is none"));
		}
		fPathCheck = null;
		strObservationRoomPath = strParam;

		//重症観察室の保存先設定
		strParam = GetInitDataString( strErSectionName, "SevereInjuryObservationRoomPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("injury severe observation room setting file is none"));
		}
		fPathCheck = null;
		strSevereInjuryObservationRoomPath = strParam;

		//集中治療室の保存先設定
		strParam = GetInitDataString( strErSectionName, "IntensiveCareUnitPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("intensive care unit setting file is none"));
		}
		fPathCheck = null;
		strIntensiveCareUnitPath = strParam;

		//高度治療室の保存先設定
		strParam = GetInitDataString( strErSectionName, "HighCareUnitPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("high casre unit setting file is none"));
		}
		fPathCheck = null;
		strHighCareUnitPath = strParam;

		//一般病棟の保存先設定
		strParam = GetInitDataString( strErSectionName, "GeneralWardPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("general ward setting file is none"));
		}
		fPathCheck = null;
		strGeneralWardPath = strParam;

		//待合室の保存先設定
		strParam = GetInitDataString( strErSectionName, "WaitingRoomPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("waiting room setting file is none"));
		}
		fPathCheck = null;
		strWaitingRoomPath = strParam;

		//X線室の保存先設定
		strParam = GetInitDataString( strErSectionName, "XRayRoomPath", ".\\", strIniFullPath );
//		System.out.println(strParam);
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("x-ray room setting file is none"));
		}
		fPathCheck = null;
		strXRayRoomPath = strParam;

		//CT室の保存先設定
		strParam = GetInitDataString( strErSectionName, "CTRoomPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("ct room setting file is none"));
		}
		fPathCheck = null;
		strCTRoomPath = strParam;

		//MRI室の保存先設定
		strParam = GetInitDataString( strErSectionName, "MRIRoomPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("mri room setting file is none"));
		}
		fPathCheck = null;
		strMRIRoomPath = strParam;

		//血管造影室の保存先設定
		strParam = GetInitDataString( strErSectionName, "AngiographyRoomPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("angiography room setting file is none"));
		}
		fPathCheck = null;
		strAngiographyRoomPath = strParam;

		//FAST室の保存先設定
		strParam = GetInitDataString( strErSectionName, "FastRoomPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("fast room setting file is none"));
		}
		fPathCheck = null;
		strFastRoomPath = strParam;


		//救急部門描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "EmergencyDepartmentAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("emergency department draw setting file is none"));
		}
		fPathCheck = null;
		strEmergencyDepartmentAxisPath = strParam;

		//診察室描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "ConsultationRoomAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("consultation room draw setting file is none"));
		}
		fPathCheck = null;
		strConsultationRoomAxisPath = strParam;

		//手術室描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "OperationRoomAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("operation room draw setting file is none"));
		}
		fPathCheck = null;
		strOperationRoomAxisPath = strParam;

		//初療室描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "EmergencyRoomAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("emergency room draw setting file is none"));
		}
		fPathCheck = null;
		strEmergencyRoomAxisPath = strParam;

		//観察室描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "ObservationRoomAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("observation room draw setting file is none"));
		}
		fPathCheck = null;
		strObservationRoomAxisPath = strParam;

		//重症観察室描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "SevereInjuryObservationRoomAxisPath", ".\\", strIniFullPath );
		strSevereInjuryObservationRoomAxisPath = strParam;

		//集中治療室描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "IntensiveCareUnitAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("intensive care unit draw setting file is none"));
		}
		fPathCheck = null;
		strIntensiveCareUnitAxisPath = strParam;

		//高度治療室描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "HighCareUnitAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("high care unit draw setting file is none"));
		}
		fPathCheck = null;
		strHighCareUnitAxisPath = strParam;

		//一般病棟描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "GeneralWardAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("general ward draw setting file is none"));
		}
		fPathCheck = null;
		strGeneralWardAxisPath = strParam;

		//待合室描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "WaitingRoomAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("waiting room draw setting file is none"));
		}
		fPathCheck = null;
		strWaitingRoomAxisPath = strParam;

		//X線室描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "XRayRoomAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("x-ray room draw setting file is none"));
		}
		fPathCheck = null;
		strXRayRoomAxisPath = strParam;

		//CT室描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "CTRoomAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("ct room draw setting file is none"));
		}
		fPathCheck = null;
		strCTRoomAxisPath = strParam;

		//MRI室描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "MRIRoomAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("mri room draw setting file is none"));
		}
		fPathCheck = null;
		strMRIRoomAxisPath = strParam;

		//血管造影室描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "AngiographyRoomAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("angiography room draw setting file is none"));
		}
		fPathCheck = null;
		strAngiographyRoomAxisPath = strParam;

		//FAST室描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "FastRoomAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("fast room draw setting file is none"));
		}
		fPathCheck = null;
		strFastRoomAxisPath = strParam;

		//エレベーター描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "ElevatorAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("elevator room draw setting file is none"));
		}
		fPathCheck = null;
		strElevatorAxisPath = strParam;

		//階段描画部分の保存先設定
		strParam = GetInitDataString( strErSectionName, "StairsAxisPath", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isFile() == false )
		{
			/* ファイルが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("stairs draw setting file is none"));
		}
		fPathCheck = null;
		strStairsAxisPath = strParam;

		//その他部屋描画部分の保存先設定
//		strParam = GetInitDataString( strErSectionName, "OtherRoomAxisPath", ".\\", strIniFullPath );
//		fPathCheck = new File( strParam );
//		if( fPathCheck.isFile() == false )
//		{
//			/* ファイルが存在しない場合は例外を返却します。 */
//			throw(new NoSuchFileException("other room draw setting file is none"));
//		}
//		fPathCheck = null;
//		strOtherRoomAxisPath = strParam;

	// 描画範囲及びメッシュ設定

		//描画範囲の横のメッシュ数
		iMeshWidthNum = (int)GetInitDataInt( strGeometrySectionName, "MeshWidthNum", iMeshWidthNum, strIniFullPath );
		// メッシュ数を0以下にしていた場合はメッシュ数を280とします。
		if( iMeshWidthNum <= 0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("mesh width count is less than 0"));
		}

		//描画範囲の縦のメッシュ数
		iMeshHeightNum = (int)GetInitDataInt( strGeometrySectionName, "MeshHeightNum", iMeshHeightNum, strIniFullPath );
		// メッシュ数を0以下にしていた場合はメッシュ数を250とします。
		if( iMeshHeightNum <= 0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("mesh height count is less than 0"));
		}

		//描画範囲の横のメッシュサイズ
		iMeshWidth = (int)GetInitDataInt( strGeometrySectionName, "MeshWidth", iMeshWidth, strIniFullPath );
		// メッシュサイズを0以下にしていた場合はメッシュサイズを10とします。
		if( iMeshWidth <= 0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("mesh width size is less than 0"));
		}

		//描画範囲の縦のメッシュサイズ
		iMeshHeight = (int)GetInitDataInt( strGeometrySectionName, "MeshHeight", iMeshHeight, strIniFullPath );
		// メッシュサイズを0以下にしていた場合はメッシュサイズを10とします。
		if( iMeshHeight <= 0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("mesh height size is less than 0"));
		}

		iEndSimulationTime = (int)GetInitDataInt( strInitParamSectionName, "EndSimulationTime", iEndSimulationTime, strIniFullPath );
		// 0以下を指定した場合は1日に設定します。
		if( iEndSimulationTime <= 0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("simulation end time is less than 0"));
		}

		iExecMode = (int)GetInitDataInt( strInitParamSectionName, "ExecMode", iExecMode, strIniFullPath );
		// 範囲外を指定した場合はコンソールモードで起動します。
		if( iExecMode < 0 || iExecMode > 2 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("simulation executive mode is none number"));
		}

		iSimulationTimeStep = (int)GetInitDataInt( strInitParamSectionName, "SimulationTimeStep", iSimulationTimeStep, strIniFullPath );
		// シミュレーションのタイムステップを0以下に設定した場合は1000(1秒)とします。
		if( iSimulationTimeStep < 0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("simulation time step is less than 0"));
		}

		lfPatientPepole = GetInitDataFloat( strInitParamSectionName, "PatientPepole", -1.0, strIniFullPath);
		if( lfPatientPepole < 0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException(" patient pepole is less than 0"));
		}

		iPatientRandomMode = (int)GetInitDataInt( strInitParamSectionName, "PatientRandomMode", 0, strIniFullPath );
		if( iPatientRandomMode < 0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("density of random patient is less than 0"));
		}
	}

	/**
	 * <PRE>
	 *    救急部門のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 救急部門のファイル名
	 */
	public String strGetEmergencyDepartmentPath()
	{
		return strEmergencyDepartmentPath;
	}

	/**
	 * <PRE>
	 *    診察室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 診察室のファイル名
	 */
	public String strGetConsultationRoomPath()
	{
		return strConsultationRoomPath;
	}

	/**
	 * <PRE>
	 *    手術室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 手術室のファイル名
	 */
	public String strGetOperationRoomPath()
	{
		return strOperationRoomPath;
	}

	/**
	 * <PRE>
	 *    初療室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 初療室のファイル名
	 */
	public String strGetEmergencyRoomPath()
	{
		return strEmergencyRoomPath;
	}

	/**
	 * <PRE>
	 *    観察室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 観察室のファイル名
	 */
	public String strGetObservationRoomPath()
	{
		return strObservationRoomPath;
	}

	/**
	 * <PRE>
	 *    重傷観察室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 重症観察室のファイル名
	 */
	public String strGetSevereInjuryObservationRoomPath()
	{
		return strSevereInjuryObservationRoomPath;
	}

	/**
	 * <PRE>
	 *    集中治療室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 集中治療室のファイル名
	 */
	public String strGetIntensiveCareUnitPath()
	{
		return strIntensiveCareUnitPath;
	}

	/**
	 * <PRE>
	 *    高度治療室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 高度治療室のファイル名
	 */
	public String strGetHighCareUnitPath()
	{
		return strHighCareUnitPath;
	}

	/**
	 * <PRE>
	 *    一般病棟のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 一般病棟のファイル名
	 */
	public String strGetGeneralWardPath()
	{
		return strGeneralWardPath;
	}

	/**
	 * <PRE>
	 *    待合室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 待合室のファイル名
	 */
	public String strGetWaitingRoomPath()
	{
		return strWaitingRoomPath;
	}

	/**
	 * <PRE>
	 *    X線室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return X線室のファイル名
	 */
	public String strGetXRayRoomPath()
	{
		return strXRayRoomPath;
	}

	/**
	 * <PRE>
	 *    CT室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return CT室のファイル名
	 */
	public String strGetCTRoomPath()
	{
		return strCTRoomPath;
	}

	/**
	 * <PRE>
	 *    ＭＲＩ室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return MRI室のファイル名
	 */
	public String strGetMRIRoomPath()
	{
		return strMRIRoomPath;
	}

	/**
	 * <PRE>
	 *    血管造影室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 血管造影室のファイル名
	 */
	public String strGetAngiographyRoomPath()
	{
		return strAngiographyRoomPath;
	}

	/**
	 * <PRE>
	 *    FAST室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return FAST室のファイル名
	 */
	public String strGetFastRoomPath()
	{
		return strFastRoomPath;
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
	 *    描画用救急部門のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 救急部門の描画用ファイル名
	 */
	public String strGetEmergencyDepartmentAxisPath()
	{
		return strEmergencyDepartmentAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用診察室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 診察室の描画用ファイル名
	 */
	public String strGetConsultationRoomAxisPath()
	{
		return strConsultationRoomAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用手術室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 手術室の描画用ファイル名
	 */
	public String strGetOperationRoomAxisPath()
	{
		return strOperationRoomAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用初療室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 初療室の描画用ファイル名
	 */
	public String strGetEmergencyRoomAxisPath()
	{
		return strEmergencyRoomAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用観察室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 観察室の描画用ファイル名
	 */
	public String strGetObservationRoomAxisPath()
	{
		return strObservationRoomAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用重傷観察室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 重症観察室の描画用ファイル名
	 */
	public String strGetSevereInjuryObservationRoomAxisPath()
	{
		return strSevereInjuryObservationRoomAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用集中治療室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 集中治療室の描画用ファイル名
	 */
	public String strGetIntensiveCareUnitAxisPath()
	{
		return strIntensiveCareUnitAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用高度治療室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 高度治療室の描画用ファイル名
	 */
	public String strGetHighCareUnitAxisPath()
	{
		return strHighCareUnitAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用一般病棟のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 一般病棟の描画用ファイル名
	 */
	public String strGetGeneralWardAxisPath()
	{
		return strGeneralWardAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用待合室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 待合室の描画用ファイル名
	 */
	public String strGetWaitingRoomAxisPath()
	{
		return strWaitingRoomAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用X線室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return X線室の描画用ファイル名
	 */
	public String strGetXRayRoomAxisPath()
	{
		return strXRayRoomAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用CT室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return CT室の描画用ファイル名
	 */
	public String strGetCTRoomAxisPath()
	{
		return strCTRoomAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用ＭＲＩ室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return MRI室の描画用ファイル名
	 */
	public String strGetMRIRoomAxisPath()
	{
		return strMRIRoomAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用血管造影室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return 血管造影室の描画用ファイル名
	 */
	public String strGetAngiographyRoomAxisPath()
	{
		return strAngiographyRoomAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用FAST室のパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return FAST室のファイル名
	 */
	public String strGetFastRoomAxisPath()
	{
		return strFastRoomAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用エレベータのパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return エレベータの描画用ファイル名
	 */
	public String strGetElevatorAxisPath()
	{
		return strElevatorAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用エレベータのパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return エレベータの描画用ファイル名
	 */
	public String strGetStairsAxisPath()
	{
		return strStairsAxisPath;
	}

	/**
	 * <PRE>
	 *    描画用エレベータのパラメータ定義ファイルを取得します。
	 * </PRE>
	 * @return エレベータの描画用ファイル名
	 */
	public String strGetOtherRoomAxisPath()
	{
		return strOtherRoomAxisPath;
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
	 *    描画領域の横幅のメッシュ数を取得します。
	 * </PRE>
	 * @return 横幅のメッシュ数
	 */
	public int iGetMeshWidthNum()
	{
		return iMeshWidthNum;
	}

	/**
	 * <PRE>
	 *    描画領域の縦のメッシュ数を取得します。
	 * </PRE>
	 * @return 縦のメッシュ数
	 */
	public int iGetMeshHeightNum()
	{
		return iMeshHeightNum;
	}

	/**
	 * <PRE>
	 *    描画領域の横の1メッシュのサイズを取得します。
	 * </PRE>
	 * @return 1メッシュの横のサイズ
	 */
	public int iGetMeshWidth()
	{
		return iMeshWidth;
	}

	/**
	 * <PRE>
	 *    描画領域の縦の1メッシュサイズを取得します。
	 * </PRE>
	 * @return 1メッシュの縦のサイズ
	 */
	public int iGetMeshHeight()
	{
		return iMeshHeight;
	}
}
