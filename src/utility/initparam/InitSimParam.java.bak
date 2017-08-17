package utility.initparam;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.logging.Level;

import utility.initparam.initsettingfile.InitSettingFileRead;

public class InitSimParam extends InitSettingFileRead
{
	// ログ出力設定
	private boolean bLogFileAppend;			// ファイル追記の可否
	private long lLogFileCount;				// ログファイル出力数
	private long lLogFileSize;				// ログ出力ファイルサイズ
	private int iLogLevel;					// ログ出力レベル
	private String strLogDirectoryName;		// ログ出力ディレクトリ
	private String strLogPrefix;			// ログ出力ファイルの接頭辞

	// 救急部門の合わせ込みパラメータの読み込みを行います。

	//患者パラメータ
	private double lfSurvivalProbabilityWeight;				// 患者の生存確率の重みパラメータ
	private double lfSurvivalJudgeCount;					// 患者の生死判定回数
	private double alfGcsScore[];							// 患者のGCS調整パラメータ(weibull分布用α、β及び各度数分布13パラメータ)
	private double alfInternalAISHeadSeverity[];			// 患者の頭部AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
	private double alfInternalAISFaceSeverity[];			// 患者の顔面AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
	private double alfInternalAISNeckSeverity[];			// 患者の頸椎AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
	private double alfInternalAISThoraxSeverity[];			// 患者の胸部AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
	private double alfInternalAISAbdomenSeverity[];			// 患者の腹部AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
	private double alfInternalAISSpineSeverity[];			// 患者の脊椎AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
	private double alfInternalAISUpperExtremitySeverity[];	// 患者の上肢AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
	private double alfInternalAISLowerExtremitySeverity[];	// 患者の下肢AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
	private double alfInternalAISUnspecifiedSeverity[];		// 患者の熱傷及びその他AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
	private double alfInjuryAISPartAnatomy[];				// 患者の外傷部位調整パラメータ(weibull分布用α、β及び各度数分布9パラメータ)
	private double alfInjuryAISNumber[];					// 患者の外傷ヶ所調整パラメータ(各度数分布9パラメータ)

	// 医師パラメータ
	private double alfDoctorJudgeModerateTrauma[];				// 傷病状態（中症度）判定確率の発生確率weibull分布のα、β及び医師の度数(5パラメータ)
	private double alfDoctorJudgeMildTrauma[];					// 傷病状態（軽症）判定確率の発生確率weibull分布のα、β及び医師の度数(5パラメータ)
	private double alfDoctorJudgeSevereTrauma[];				// 傷病状態（重症）判定確率の発生確率weibull分布のα、β及び医師の度数(5パラメータ)
	private double alfDoctorImplementConsultationProcess[];		// 初療室へ行くようにするか退院か、入院するか退院かの判定用パラメータ(6パラメータ)

	// 看護師パラメータ
	private double alfNurseJudgeModerateTrauma[];				// 傷病状態（中症度）判定確率の発生確率weibull分布のα、β及び看護師の度数(5パラメータ)
	private double alfNurseJudgeMildTrauma[];					// 傷病状態（軽症）判定確率の発生確率weibull分布のα、β及び看護師の度数(5パラメータ)
	private double alfNurseJudgeSevereTrauma[];				// 傷病状態（重症）判定確率の発生確率weibull分布のα、β及び看護師の度数(5パラメータ)

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
		bLogFileAppend = true;				// ファイル追記の可否
		lLogFileCount = 50;					// ログファイル出力数
		lLogFileSize = 100000;				// ログ出力ファイルサイズ
		iLogLevel = 3;						// ログ出力レベル
		strLogDirectoryName = "./";			// ログ出力ディレクトリ
		strLogPrefix = "TRISimLogging";		// ログ出力ファイルの接頭辞

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
		String strIniFileName					= "erEV.ini";
		String strLogSectionName				= "LogData";
		String strPatientSectionName			= "PatientParameter";
		String strDoctorSectionName				= "DoctorParameter";
		String strNurseSectionName				= "NurseParameter";
		String strClinicalEngineerSectionName	= "ClinicalEngineerParameter";

		String strParam;
		long lRet = 0L;
		double lfRet = 0.0;
		File file;
		File fPathCheck;

		alfGcsScore								= new double[17];			// 患者のGCS調整パラメータ(weibull分布用α、β及び各度数分布13パラメータ)
		alfInternalAISHeadSeverity				= new double[9];			// 患者の頭部AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
		alfInternalAISFaceSeverity				= new double[9];			// 患者の顔面AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
		alfInternalAISNeckSeverity				= new double[9];			// 患者の頸椎AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
		alfInternalAISThoraxSeverity			= new double[9];			// 患者の胸部AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
		alfInternalAISAbdomenSeverity			= new double[9];			// 患者の腹部AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
		alfInternalAISSpineSeverity				= new double[9];			// 患者の脊椎AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
		alfInternalAISUpperExtremitySeverity	= new double[9];			// 患者の上肢AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
		alfInternalAISLowerExtremitySeverity	= new double[9];			// 患者の下肢AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
		alfInternalAISUnspecifiedSeverity		= new double[9];			// 患者の熱傷及びその他AIS調整パラメータ(weibull分布用α、β及び各度数分布7パラメータ)
		alfInjuryAISPartAnatomy					= new double[11];			// 患者の外傷部位調整パラメータ(weibull分布用α、β及び各度数分布9パラメータ)
		alfInjuryAISNumber						= new double[10];			// 患者の外傷ヶ所調整パラメータ(各度数分布9パラメータ)

		// 医師パラメータ
		alfDoctorJudgeModerateTrauma			= new double[5];			// 傷病状態（中症度）判定確率の発生確率weibull分布のα、β及び医師の度数(5パラメータ)
		alfDoctorJudgeMildTrauma				= new double[5];			// 傷病状態（軽症）判定確率の発生確率weibull分布のα、β及び医師の度数(5パラメータ)
		alfDoctorJudgeSevereTrauma				= new double[5];			// 傷病状態（重症）判定確率の発生確率weibull分布のα、β及び医師の度数(5パラメータ)
		alfDoctorImplementConsultationProcess	= new double[6];			// 初療室へ行くようにするか退院か、入院するか退院かの判定用パラメータ(6パラメータ)

		// 看護師パラメータ
		alfNurseJudgeModerateTrauma				= new double[5];			// 傷病状態（中症度）判定確率の発生確率weibull分布のα、β及び看護師の度数(5パラメータ)
		alfNurseJudgeMildTrauma					= new double[5];			// 傷病状態（軽症）判定確率の発生確率weibull分布のα、β及び看護師の度数(5パラメータ)
		alfNurseJudgeSevereTrauma				= new double[5];			// 傷病状態（重症）判定確率の発生確率weibull分布のα、β及び看護師の度数(5パラメータ)

		file = new File( strIniFileName );

	// ログ出力設定

		strIniFullPath = file.getAbsolutePath( );
//		System.out.println(strIniFullPath);
		//ログの接頭辞の設定
		strParam = GetInitDataString( strLogSectionName, "LogPrefix", "TRISimLogging", strIniFullPath );
		if( strParam.indexOf("\\") != -1 || strParam.indexOf(":") != -1 ||
			strParam.indexOf("?") != -1  || strParam.indexOf("/") != -1 ||
			strParam.indexOf("*") != -1  || strParam.indexOf("<") != -1 ||
			strParam.indexOf(">") != -1  ||	strParam.indexOf("|") != -1 )
		{
			/* パス名禁止文字が含まれていたときはエラーを返して終了 */
			throw(new IllegalArgumentException("contain the forbidden string"));
		}
		strLogPrefix = strParam;

		//ログの保存先設定
		strParam = GetInitDataString( strLogSectionName, "LogDirectoryName", ".\\", strIniFullPath );
		fPathCheck = new File( strParam );
		if( fPathCheck.isDirectory() == false )
		{
			/* ディレクトリが存在しない場合は例外を返却します。 */
			throw(new NoSuchFileException("the directory to output log file is none"));
		}
		fPathCheck = null;
		strLogDirectoryName = strParam;

		//ログの出力レベル設定
		iLogLevel = (int)GetInitDataInt( strLogSectionName, "LogLevel", -1, strIniFullPath );
		if( 0 > iLogLevel || iLogLevel > 6 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("log level out of range"));
		}

		//ログファイルのサイズ設定
		lLogFileSize = GetInitDataInt( strLogSectionName, "LogFileSize", -1, strIniFullPath );
		if (lLogFileSize < 0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("log file size is less than 0"));
		}

		//ログファイル数の設定
		lLogFileCount = GetInitDataInt( strLogSectionName, "LogFileCount", -1, strIniFullPath );
		if (lLogFileCount < 0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("log file count is less than 0"));
		}

		// ログファイル追記可否の設定
		bLogFileAppend = GetInitDataBoolean( strLogSectionName, "LogFileAppend", false, strIniFullPath );

	/* 患者調整パラメータ */

		//生存確率重みの設定
		lfSurvivalProbabilityWeight = GetInitDataFloat( strPatientSectionName, "SurvivalProbabilityWeight", -1, strIniFullPath );
		if( 0 > lfSurvivalProbabilityWeight || lfSurvivalProbabilityWeight > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("survival probability weight out of range"));
		}

		//生存判定回数の設定
		lfSurvivalJudgeCount = GetInitDataFloat( strPatientSectionName, "SurvivalJudgeCount", -1, strIniFullPath );
		if( 0 > lfSurvivalJudgeCount )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("survival judge count out of range"));
		}

		// GCS weibull分布αの設定
		alfGcsScore[0] = GetInitDataFloat( strPatientSectionName, "GcsScoreWeibullAlpha", -1, strIniFullPath );

		// GCS weibull分布βの設定
		alfGcsScore[1] = GetInitDataFloat( strPatientSectionName, "GcsScoreWeibullBeta", -1, strIniFullPath );

		// GCS=15の発生確率の設定
		alfGcsScore[2] = GetInitDataFloat( strPatientSectionName, "GcsScore15", -1, strIniFullPath );
		if( 0 > alfGcsScore[2] || alfGcsScore[2] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("probability of gcs=15 out of range"));
		}
		// GCS=14の発生確率の設定
		alfGcsScore[3] = GetInitDataFloat( strPatientSectionName, "GcsScore14", -1, strIniFullPath );
		if( 0 > alfGcsScore[3] || alfGcsScore[3] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("probability of gcs=14 out of range"));
		}
		// GCS=13の発生確率の設定
		alfGcsScore[4] = GetInitDataFloat( strPatientSectionName, "GcsScore13", -1, strIniFullPath );
		if( 0 > alfGcsScore[4] || alfGcsScore[4] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("probability of gcs=13 out of range"));
		}
		// GCS=12の発生確率の設定
		alfGcsScore[5] = GetInitDataFloat( strPatientSectionName, "GcsScore12", -1, strIniFullPath );
		if( 0 > alfGcsScore[5] || alfGcsScore[5] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("probability of gcs=12 out of range"));
		}
		// GCS=11の発生確率の設定
		alfGcsScore[6] = GetInitDataFloat( strPatientSectionName, "GcsScore11", -1, strIniFullPath );
		if( 0 > alfGcsScore[6] || alfGcsScore[6] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("probability of gcs=11 out of range"));
		}
		// GCS=10の発生確率の設定
		alfGcsScore[7] = GetInitDataFloat( strPatientSectionName, "GcsScore10", -1, strIniFullPath );
		if( 0 > alfGcsScore[7] || alfGcsScore[7] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("probability of gcs=10 out of range"));
		}
		// GCS=9の発生確率の設定
		alfGcsScore[8] = GetInitDataFloat( strPatientSectionName, "GcsScore9", -1, strIniFullPath );
		if( 0 > alfGcsScore[8] || alfGcsScore[8] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("probability of gcs=9 out of range"));
		}
		// GCS=8の発生確率の設定
		alfGcsScore[9] = GetInitDataFloat( strPatientSectionName, "GcsScore8", -1, strIniFullPath );
		if( 0 > alfGcsScore[9] || alfGcsScore[9] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("probability of gcs=8 out of range"));
		}
		// GCS=7の発生確率の設定
		alfGcsScore[10] = GetInitDataFloat( strPatientSectionName, "GcsScore7", -1, strIniFullPath );
		if( 0 > alfGcsScore[10] || alfGcsScore[10] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("probability of gcs=7 out of range"));
		}
		// GCS=6の発生確率の設定
		alfGcsScore[11] = GetInitDataFloat( strPatientSectionName, "GcsScore6", -1, strIniFullPath );
		if( 0 > alfGcsScore[11] || alfGcsScore[11] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("probability of gcs=6 out of range"));
		}
		// GCS=5の発生確率の設定
		alfGcsScore[12] = GetInitDataFloat( strPatientSectionName, "GcsScore5", -1, strIniFullPath );
		if( 0 > alfGcsScore[12] || alfGcsScore[12] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("probability of gcs=5 out of range"));
		}
		// GCS=4の発生確率の設定
		alfGcsScore[13] = GetInitDataFloat( strPatientSectionName, "GcsScore4", -1, strIniFullPath );
		if( 0 > alfGcsScore[13] || alfGcsScore[13] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("probability of gcs=4 out of range"));
		}
		// GCS=3の発生確率の設定
		alfGcsScore[14] = GetInitDataFloat( strPatientSectionName, "GcsScore3", -1, strIniFullPath );
		if( 0 > alfGcsScore[14] || alfGcsScore[14] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("probability of gcs=3 out of range"));
		}

		// 患者の頭部重症度の発生weibull分布のα
		alfInternalAISHeadSeverity[0] = GetInitDataFloat( strPatientSectionName, "AISHeadSeverityWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfInternalAISHeadSeverity[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity weibull parameter alpha out of range"));
		}

		// 患者の頭部重症度の発生weibull分布のβ
		alfInternalAISHeadSeverity[1] = GetInitDataFloat( strPatientSectionName, "AISHeadSeverityWeibullBeta", -1, strIniFullPath );
		if( 0 > alfInternalAISHeadSeverity[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity weibull parameter beta out of range"));
		}

		// 患者のAIS頭部重症度1
		alfInternalAISHeadSeverity[2] = GetInitDataFloat( strPatientSectionName, "AISHeadSeverity1", -1, strIniFullPath );
		if( 0 > alfInternalAISHeadSeverity[2] || alfInternalAISHeadSeverity[2] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity AIS 1 parameter out of range"));
		}

		// 患者のAIS頭部重症度2
		alfInternalAISHeadSeverity[3] = GetInitDataFloat( strPatientSectionName, "AISHeadSeverity2", -1, strIniFullPath );
		if( 0 > alfInternalAISHeadSeverity[3] || alfInternalAISHeadSeverity[3] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity AIS 2 parameter out of range"));
		}

		// 患者のAIS頭部重症度3
		alfInternalAISHeadSeverity[4] = GetInitDataFloat( strPatientSectionName, "AISHeadSeverity3", -1, strIniFullPath );
		if( 0 > alfInternalAISHeadSeverity[4] || alfInternalAISHeadSeverity[4] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity AIS 3 parameter out of range"));
		}

		// 患者のAIS頭部重症度4
		alfInternalAISHeadSeverity[5] = GetInitDataFloat( strPatientSectionName, "AISHeadSeverity4", -1, strIniFullPath );
		if( 0 > alfInternalAISHeadSeverity[5] || alfInternalAISHeadSeverity[5] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity AIS 4 parameter out of range"));
		}

		// 患者のAIS頭部重症度5
		alfInternalAISHeadSeverity[6] = GetInitDataFloat( strPatientSectionName, "AISHeadSeverity5", -1, strIniFullPath );
		if( 0 > alfInternalAISHeadSeverity[6] || alfInternalAISHeadSeverity[6] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity AIS 5 parameter out of range"));
		}

		// 患者のAIS頭部重症度6
		alfInternalAISHeadSeverity[7] = GetInitDataFloat( strPatientSectionName, "AISHeadSeverity6", -1, strIniFullPath );
		if( 0 > alfInternalAISHeadSeverity[7] || alfInternalAISHeadSeverity[7] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity AIS 6 parameter out of range"));
		}

		// 患者の顔面重症度の発生weibull分布のα
		alfInternalAISFaceSeverity[0] = GetInitDataFloat( strPatientSectionName, "AISFaceSeverityWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfInternalAISFaceSeverity[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity weibull parameter alpha out of range"));
		}

		// 患者の顔面重症度の発生weibull分布のβ
		alfInternalAISFaceSeverity[1] = GetInitDataFloat( strPatientSectionName, "AISFaceSeverityWeibullBeta", -1, strIniFullPath );
		if( 0 > alfInternalAISFaceSeverity[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity weibull parameter beta out of range"));
		}

		// 患者のAIS顔面重症度1
		alfInternalAISFaceSeverity[2] = GetInitDataFloat( strPatientSectionName, "AISFaceSeverity1", -1, strIniFullPath );
		if( 0 > alfInternalAISFaceSeverity[2] || alfInternalAISFaceSeverity[2] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity AIS 1 parameter out of range"));
		}

		// 患者のAIS顔面重症度2
		alfInternalAISFaceSeverity[3] = GetInitDataFloat( strPatientSectionName, "AISFaceSeverity2", -1, strIniFullPath );
		if( 0 > alfInternalAISFaceSeverity[3] || alfInternalAISFaceSeverity[3] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity AIS 2 parameter out of range"));
		}

		// 患者のAIS顔面重症度3
		alfInternalAISFaceSeverity[4] = GetInitDataFloat( strPatientSectionName, "AISFaceSeverity3", -1, strIniFullPath );
		if( 0 > alfInternalAISFaceSeverity[4] || alfInternalAISFaceSeverity[4] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity AIS 3 parameter out of range"));
		}

		// 患者のAIS顔面重症度4
		alfInternalAISFaceSeverity[5] = GetInitDataFloat( strPatientSectionName, "AISFaceSeverity4", -1, strIniFullPath );
		if( 0 > alfInternalAISFaceSeverity[5] || alfInternalAISFaceSeverity[5] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity AIS 4 parameter out of range"));
		}

		// 患者のAIS顔面重症度5
		alfInternalAISFaceSeverity[6] = GetInitDataFloat( strPatientSectionName, "AISFaceSeverity5", -1, strIniFullPath );
		if( 0 > alfInternalAISFaceSeverity[6] || alfInternalAISFaceSeverity[6] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity AIS 5 parameter out of range"));
		}

		// 患者のAIS顔面重症度6
		alfInternalAISFaceSeverity[7] = GetInitDataFloat( strPatientSectionName, "AISFaceSeverity6", -1, strIniFullPath );
		if( 0 > alfInternalAISFaceSeverity[7] || alfInternalAISFaceSeverity[7] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("head severity AIS 6 parameter out of range"));
		}

		// 患者の頸椎重症度の発生weibull分布のα
		alfInternalAISNeckSeverity[0] = GetInitDataFloat( strPatientSectionName, "AISNeckSeverityWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfInternalAISNeckSeverity[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("neck severity weibull parameter alpha out of range"));
		}

		// 患者の頸椎重症度の発生weibull分布のβ
		alfInternalAISNeckSeverity[1] = GetInitDataFloat( strPatientSectionName, "AISNeckSeverityWeibullBeta", -1, strIniFullPath );
		if( 0 > alfInternalAISNeckSeverity[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("neck severity weibull parameter beta out of range"));
		}

		// 患者のAIS頸椎重症度1
		alfInternalAISNeckSeverity[2] = GetInitDataFloat( strPatientSectionName, "AISNeckSeverity1", -1, strIniFullPath );
		if( 0 > alfInternalAISNeckSeverity[2] || alfInternalAISNeckSeverity[2] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("neck severity AIS 1 parameter out of range"));
		}

		// 患者のAIS頸椎重症度2
		alfInternalAISNeckSeverity[3] = GetInitDataFloat( strPatientSectionName, "AISNeckSeverity2", -1, strIniFullPath );
		if( 0 > alfInternalAISNeckSeverity[3] || alfInternalAISNeckSeverity[3] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("neck severity AIS 2 parameter out of range"));
		}

		// 患者のAIS頸椎重症度3
		alfInternalAISNeckSeverity[4] = GetInitDataFloat( strPatientSectionName, "AISNeckSeverity3", -1, strIniFullPath );
		if( 0 > alfInternalAISNeckSeverity[4] || alfInternalAISNeckSeverity[4] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("neck severity AIS 3 parameter out of range"));
		}

		// 患者のAIS頸椎重症度4
		alfInternalAISNeckSeverity[5] = GetInitDataFloat( strPatientSectionName, "AISNeckSeverity4", -1, strIniFullPath );
		if( 0 > alfInternalAISNeckSeverity[5] || alfInternalAISNeckSeverity[5] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("neck severity AIS 4 parameter out of range"));
		}

		// 患者のAIS頸椎重症度5
		alfInternalAISNeckSeverity[6] = GetInitDataFloat( strPatientSectionName, "AISNeckSeverity5", -1, strIniFullPath );
		if( 0 > alfInternalAISNeckSeverity[6] || alfInternalAISNeckSeverity[6] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("neck severity AIS 5 parameter out of range"));
		}

		// 患者のAIS頸椎重症度6
		alfInternalAISNeckSeverity[7] = GetInitDataFloat( strPatientSectionName, "AISNeckSeverity6", -1, strIniFullPath );
		if( 0 > alfInternalAISNeckSeverity[7] || alfInternalAISNeckSeverity[7] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("neck severity AIS 6 parameter out of range"));
		}

		// 患者の胸部重症度の発生weibull分布のα
		alfInternalAISThoraxSeverity[0] = GetInitDataFloat( strPatientSectionName, "AISThoraxSeverityWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfInternalAISThoraxSeverity[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Thorax severity weibull parameter alpha out of range"));
		}

		// 患者の胸部重症度の発生weibull分布のβ
		alfInternalAISThoraxSeverity[1] = GetInitDataFloat( strPatientSectionName, "AISThoraxSeverityWeibullBeta", -1, strIniFullPath );
		if( 0 > alfInternalAISThoraxSeverity[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Thorax severity weibull parameter beta out of range"));
		}

		// 患者のAIS胸部重症度1
		alfInternalAISThoraxSeverity[2] = GetInitDataFloat( strPatientSectionName, "AISThoraxSeverity1", -1, strIniFullPath );
		if( 0 > alfInternalAISThoraxSeverity[2] || alfInternalAISThoraxSeverity[2] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Thorax severity AIS 1 parameter out of range"));
		}

		// 患者のAIS胸部重症度2
		alfInternalAISThoraxSeverity[3] = GetInitDataFloat( strPatientSectionName, "AISThoraxSeverity2", -1, strIniFullPath );
		if( 0 > alfInternalAISThoraxSeverity[3] || alfInternalAISThoraxSeverity[3] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Thorax severity AIS 2 parameter out of range"));
		}

		// 患者のAIS胸部重症度3
		alfInternalAISThoraxSeverity[4] = GetInitDataFloat( strPatientSectionName, "AISThoraxSeverity3", -1, strIniFullPath );
		if( 0 > alfInternalAISThoraxSeverity[4] || alfInternalAISThoraxSeverity[4] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Thorax severity AIS 3 parameter out of range"));
		}

		// 患者のAIS胸部重症度4
		alfInternalAISThoraxSeverity[5] = GetInitDataFloat( strPatientSectionName, "AISThoraxSeverity4", -1, strIniFullPath );
		if( 0 > alfInternalAISThoraxSeverity[5] || alfInternalAISThoraxSeverity[5] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Thorax severity AIS 4 parameter out of range"));
		}

		// 患者のAIS胸部重症度5
		alfInternalAISThoraxSeverity[6] = GetInitDataFloat( strPatientSectionName, "AISThoraxSeverity5", -1, strIniFullPath );
		if( 0 > alfInternalAISThoraxSeverity[6] || alfInternalAISThoraxSeverity[6] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Thorax severity AIS 5 parameter out of range"));
		}

		// 患者のAIS胸部重症度6
		alfInternalAISThoraxSeverity[7] = GetInitDataFloat( strPatientSectionName, "AISThoraxSeverity6", -1, strIniFullPath );
		if( 0 > alfInternalAISThoraxSeverity[7] || alfInternalAISThoraxSeverity[7] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Thorax severity AIS 6 parameter out of range"));
		}

		// 患者の腹部重症度の発生weibull分布のα
		alfInternalAISAbdomenSeverity[0] = GetInitDataFloat( strPatientSectionName, "AISAbdomenSeverityWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfInternalAISAbdomenSeverity[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Abdomen severity weibull parameter alpha out of range"));
		}

		// 患者の腹部重症度の発生weibull分布のβ
		alfInternalAISAbdomenSeverity[1] = GetInitDataFloat( strPatientSectionName, "AISAbdomenSeverityWeibullBeta", -1, strIniFullPath );
		if( 0 > alfInternalAISAbdomenSeverity[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Abdomen severity weibull parameter beta out of range"));
		}

		// 患者のAIS腹部重症度1
		alfInternalAISAbdomenSeverity[2] = GetInitDataFloat( strPatientSectionName, "AISAbdomenSeverity1", -1, strIniFullPath );
		if( 0 > alfInternalAISAbdomenSeverity[2] || alfInternalAISAbdomenSeverity[2] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Abdomen severity AIS 1 parameter out of range"));
		}

		// 患者のAIS腹部重症度2
		alfInternalAISAbdomenSeverity[3] = GetInitDataFloat( strPatientSectionName, "AISAbdomenSeverity2", -1, strIniFullPath );
		if( 0 > alfInternalAISAbdomenSeverity[3] || alfInternalAISAbdomenSeverity[3] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Abdomen severity AIS 2 parameter out of range"));
		}

		// 患者のAIS腹部重症度3
		alfInternalAISAbdomenSeverity[4] = GetInitDataFloat( strPatientSectionName, "AISAbdomenSeverity3", -1, strIniFullPath );
		if( 0 > alfInternalAISAbdomenSeverity[4] || alfInternalAISAbdomenSeverity[4] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Abdomen severity AIS 3 parameter out of range"));
		}

		// 患者のAIS腹部重症度4
		alfInternalAISAbdomenSeverity[5] = GetInitDataFloat( strPatientSectionName, "AISAbdomenSeverity4", -1, strIniFullPath );
		if( 0 > alfInternalAISAbdomenSeverity[5] || alfInternalAISAbdomenSeverity[5] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Abdomen severity AIS 4 parameter out of range"));
		}

		// 患者のAIS腹部重症度5
		alfInternalAISAbdomenSeverity[6] = GetInitDataFloat( strPatientSectionName, "AISAbdomenSeverity5", -1, strIniFullPath );
		if( 0 > alfInternalAISAbdomenSeverity[6] || alfInternalAISAbdomenSeverity[6] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Abdomen severity AIS 5 parameter out of range"));
		}

		// 患者のAIS腹部重症度6
		alfInternalAISAbdomenSeverity[7] = GetInitDataFloat( strPatientSectionName, "AISAbdomenSeverity6", -1, strIniFullPath );
		if( 0 > alfInternalAISAbdomenSeverity[7] || alfInternalAISAbdomenSeverity[7] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Abdomen severity AIS 6 parameter out of range"));
		}

		// 患者の脊椎重症度の発生weibull分布のα
		alfInternalAISSpineSeverity[0] = GetInitDataFloat( strPatientSectionName, "AISSpineSeverityWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfInternalAISSpineSeverity[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Spine severity weibull parameter alpha out of range"));
		}

		// 患者の脊椎重症度の発生weibull分布のβ
		alfInternalAISSpineSeverity[1] = GetInitDataFloat( strPatientSectionName, "AISSpineSeverityWeibullBeta", -1, strIniFullPath );
		if( 0 > alfInternalAISSpineSeverity[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Spine severity weibull parameter beta out of range"));
		}

		// 患者のAIS脊椎重症度1
		alfInternalAISSpineSeverity[2] = GetInitDataFloat( strPatientSectionName, "AISSpineSeverity1", -1, strIniFullPath );
		if( 0 > alfInternalAISSpineSeverity[2] || alfInternalAISSpineSeverity[2] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Spine severity AIS 1 parameter out of range"));
		}

		// 患者のAIS脊椎重症度2
		alfInternalAISSpineSeverity[3] = GetInitDataFloat( strPatientSectionName, "AISSpineSeverity2", -1, strIniFullPath );
		if( 0 > alfInternalAISSpineSeverity[3] || alfInternalAISSpineSeverity[3] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Spine severity AIS 2 parameter out of range"));
		}

		// 患者のAIS脊椎重症度3
		alfInternalAISSpineSeverity[4] = GetInitDataFloat( strPatientSectionName, "AISSpineSeverity3", -1, strIniFullPath );
		if( 0 > alfInternalAISSpineSeverity[4] || alfInternalAISSpineSeverity[4] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Spine severity AIS 3 parameter out of range"));
		}

		// 患者のAIS脊椎重症度4
		alfInternalAISSpineSeverity[5] = GetInitDataFloat( strPatientSectionName, "AISSpineSeverity4", -1, strIniFullPath );
		if( 0 > alfInternalAISSpineSeverity[5] || alfInternalAISSpineSeverity[5] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Spine severity AIS 4 parameter out of range"));
		}

		// 患者のAIS脊椎重症度5
		alfInternalAISSpineSeverity[6] = GetInitDataFloat( strPatientSectionName, "AISSpineSeverity5", -1, strIniFullPath );
		if( 0 > alfInternalAISSpineSeverity[6] || alfInternalAISSpineSeverity[6] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Spine severity AIS 5 parameter out of range"));
		}

		// 患者のAIS脊椎重症度6
		alfInternalAISSpineSeverity[7] = GetInitDataFloat( strPatientSectionName, "AISSpineSeverity6", -1, strIniFullPath );
		if( 0 > alfInternalAISSpineSeverity[7] || alfInternalAISSpineSeverity[7] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Spine severity AIS 6 parameter out of range"));
		}

		// 患者の上肢重症度の発生weibull分布のα
		alfInternalAISUpperExtremitySeverity[0] = GetInitDataFloat( strPatientSectionName, "AISUpperExtremitySeverityWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfInternalAISUpperExtremitySeverity[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("UpperExtremity severity weibull parameter alpha out of range"));
		}

		// 患者の上肢重症度の発生weibull分布のβ
		alfInternalAISUpperExtremitySeverity[1] = GetInitDataFloat( strPatientSectionName, "AISUpperExtremitySeverityWeibullBeta", -1, strIniFullPath );
		if( 0 > alfInternalAISUpperExtremitySeverity[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("UpperExtremity severity weibull parameter beta out of range"));
		}

		// 患者のAIS上肢重症度1
		alfInternalAISUpperExtremitySeverity[2] = GetInitDataFloat( strPatientSectionName, "AISUpperExtremitySeverity1", -1, strIniFullPath );
		if( 0 > alfInternalAISUpperExtremitySeverity[2] || alfInternalAISUpperExtremitySeverity[2] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("UpperExtremity severity AIS 1 parameter out of range"));
		}

		// 患者のAIS上肢重症度2
		alfInternalAISUpperExtremitySeverity[3] = GetInitDataFloat( strPatientSectionName, "AISUpperExtremitySeverity2", -1, strIniFullPath );
		if( 0 > alfInternalAISUpperExtremitySeverity[3] || alfInternalAISUpperExtremitySeverity[3] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("UpperExtremity severity AIS 2 parameter out of range"));
		}

		// 患者のAIS上肢重症度3
		alfInternalAISUpperExtremitySeverity[4] = GetInitDataFloat( strPatientSectionName, "AISUpperExtremitySeverity3", -1, strIniFullPath );
		if( 0 > alfInternalAISUpperExtremitySeverity[4] || alfInternalAISUpperExtremitySeverity[4] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("UpperExtremity severity AIS 3 parameter out of range"));
		}

		// 患者のAIS上肢重症度4
		alfInternalAISUpperExtremitySeverity[5] = GetInitDataFloat( strPatientSectionName, "AISUpperExtremitySeverity4", -1, strIniFullPath );
		if( 0 > alfInternalAISUpperExtremitySeverity[5] || alfInternalAISUpperExtremitySeverity[5] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("UpperExtremity severity AIS 4 parameter out of range"));
		}

		// 患者のAIS上肢重症度5
		alfInternalAISUpperExtremitySeverity[6] = GetInitDataFloat( strPatientSectionName, "AISUpperExtremitySeverity5", -1, strIniFullPath );
		if( 0 > alfInternalAISUpperExtremitySeverity[6] || alfInternalAISUpperExtremitySeverity[6] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("UpperExtremity severity AIS 5 parameter out of range"));
		}

		// 患者のAIS上肢重症度6
		alfInternalAISUpperExtremitySeverity[7] = GetInitDataFloat( strPatientSectionName, "AISUpperExtremitySeverity6", -1, strIniFullPath );
		if( 0 > alfInternalAISUpperExtremitySeverity[7] || alfInternalAISUpperExtremitySeverity[7] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("UpperExtremity severity AIS 6 parameter out of range"));
		}

		// 患者の下肢重症度の発生weibull分布のα
		alfInternalAISLowerExtremitySeverity[0] = GetInitDataFloat( strPatientSectionName, "AISLowerExtremitySeverityWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfInternalAISLowerExtremitySeverity[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("LowerExtremity severity weibull parameter alpha out of range"));
		}

		// 患者の下肢重症度の発生weibull分布のβ
		alfInternalAISLowerExtremitySeverity[1] = GetInitDataFloat( strPatientSectionName, "AISLowerExtremitySeverityWeibullBeta", -1, strIniFullPath );
		if( 0 > alfInternalAISLowerExtremitySeverity[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("LowerExtremity severity weibull parameter beta out of range"));
		}

		// 患者のAIS下肢重症度1
		alfInternalAISLowerExtremitySeverity[2] = GetInitDataFloat( strPatientSectionName, "AISLowerExtremitySeverity1", -1, strIniFullPath );
		if( 0 > alfInternalAISLowerExtremitySeverity[2] || alfInternalAISLowerExtremitySeverity[2] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("LowerExtremity severity AIS 1 parameter out of range"));
		}

		// 患者のAIS下肢重症度2
		alfInternalAISLowerExtremitySeverity[3] = GetInitDataFloat( strPatientSectionName, "AISLowerExtremitySeverity2", -1, strIniFullPath );
		if( 0 > alfInternalAISLowerExtremitySeverity[3] || alfInternalAISLowerExtremitySeverity[3] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("LowerExtremity severity AIS 2 parameter out of range"));
		}

		// 患者のAIS下肢重症度3
		alfInternalAISLowerExtremitySeverity[4] = GetInitDataFloat( strPatientSectionName, "AISLowerExtremitySeverity3", -1, strIniFullPath );
		if( 0 > alfInternalAISLowerExtremitySeverity[4] || alfInternalAISLowerExtremitySeverity[4] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("LowerExtremity severity AIS 3 parameter out of range"));
		}

		// 患者のAIS下肢重症度4
		alfInternalAISLowerExtremitySeverity[5] = GetInitDataFloat( strPatientSectionName, "AISLowerExtremitySeverity4", -1, strIniFullPath );
		if( 0 > alfInternalAISLowerExtremitySeverity[5] || alfInternalAISLowerExtremitySeverity[5] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("LowerExtremity severity AIS 4 parameter out of range"));
		}

		// 患者のAIS下肢重症度5
		alfInternalAISLowerExtremitySeverity[6] = GetInitDataFloat( strPatientSectionName, "AISLowerExtremitySeverity5", -1, strIniFullPath );
		if( 0 > alfInternalAISLowerExtremitySeverity[6] || alfInternalAISLowerExtremitySeverity[6] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("LowerExtremity severity AIS 5 parameter out of range"));
		}

		// 患者のAIS下肢重症度6
		alfInternalAISLowerExtremitySeverity[7] = GetInitDataFloat( strPatientSectionName, "AISLowerExtremitySeverity6", -1, strIniFullPath );
		if( 0 > alfInternalAISLowerExtremitySeverity[7] || alfInternalAISLowerExtremitySeverity[7] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("LowerExtremity severity AIS 6 parameter out of range"));
		}

		// 患者の熱傷及びその他重症度の発生weibull分布のα
		alfInternalAISUnspecifiedSeverity[0] = GetInitDataFloat( strPatientSectionName, "AISUnspecifiedSeverityWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfInternalAISUnspecifiedSeverity[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Unspecified severity weibull parameter alpha out of range"));
		}

		// 患者の熱傷及びその他重症度の発生weibull分布のβ
		alfInternalAISUnspecifiedSeverity[1] = GetInitDataFloat( strPatientSectionName, "AISUnspecifiedSeverityWeibullBeta", -1, strIniFullPath );
		if( 0 > alfInternalAISUnspecifiedSeverity[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Unspecified severity weibull parameter beta out of range"));
		}

		// 患者のAIS熱傷及びその他重症度1
		alfInternalAISUnspecifiedSeverity[2] = GetInitDataFloat( strPatientSectionName, "AISUnspecifiedSeverity1", -1, strIniFullPath );
		if( 0 > alfInternalAISUnspecifiedSeverity[2] || alfInternalAISUnspecifiedSeverity[2] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Unspecified severity AIS 1 parameter out of range"));
		}

		// 患者のAIS熱傷及びその他重症度2
		alfInternalAISUnspecifiedSeverity[3] = GetInitDataFloat( strPatientSectionName, "AISUnspecifiedSeverity2", -1, strIniFullPath );
		if( 0 > alfInternalAISUnspecifiedSeverity[3] || alfInternalAISUnspecifiedSeverity[3] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Unspecified severity AIS 2 parameter out of range"));
		}

		// 患者のAIS熱傷及びその他重症度3
		alfInternalAISUnspecifiedSeverity[4] = GetInitDataFloat( strPatientSectionName, "AISUnspecifiedSeverity3", -1, strIniFullPath );
		if( 0 > alfInternalAISUnspecifiedSeverity[4] || alfInternalAISUnspecifiedSeverity[4] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Unspecified severity AIS 3 parameter out of range"));
		}

		// 患者のAIS熱傷及びその他重症度4
		alfInternalAISUnspecifiedSeverity[5] = GetInitDataFloat( strPatientSectionName, "AISUnspecifiedSeverity4", -1, strIniFullPath );
		if( 0 > alfInternalAISUnspecifiedSeverity[5] || alfInternalAISUnspecifiedSeverity[5] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Unspecified severity AIS 4 parameter out of range"));
		}

		// 患者のAIS熱傷及びその他重症度5
		alfInternalAISUnspecifiedSeverity[6] = GetInitDataFloat( strPatientSectionName, "AISUnspecifiedSeverity5", -1, strIniFullPath );
		if( 0 > alfInternalAISUnspecifiedSeverity[6] || alfInternalAISUnspecifiedSeverity[6] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Unspecified severity AIS 5 parameter out of range"));
		}

		// 患者のAIS熱傷及びその他重症度6
		alfInternalAISUnspecifiedSeverity[7] = GetInitDataFloat( strPatientSectionName, "AISUnspecifiedSeverity6", -1, strIniFullPath );
		if( 0 > alfInternalAISUnspecifiedSeverity[7] || alfInternalAISUnspecifiedSeverity[7] > 6.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Unspecified severity AIS 6 parameter out of range"));
		}

		// 患者の傷病部位の発生weibull分布のα
		alfInjuryAISPartAnatomy[0] = GetInitDataFloat( strPatientSectionName, "InjuryAISPartAnatomyWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfInjuryAISPartAnatomy[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Part of Anatomy weibull parameter alpha out of range"));
		}

		// 患者の傷病部位の発生weibull分布のβ
		alfInjuryAISPartAnatomy[1] = GetInitDataFloat( strPatientSectionName, "InjuryAISPartAnatomyWeibullBeta", -1, strIniFullPath );
		if( 0 > alfInjuryAISPartAnatomy[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Part of Anatomy weibull parameter beta out of range"));
		}

		// 患者の傷病部位頭部
		alfInjuryAISPartAnatomy[2] = GetInitDataFloat( strPatientSectionName, "InjuryPartAnatomyHead", -1, strIniFullPath );
		if( 0 > alfInjuryAISPartAnatomy[2] || alfInjuryAISPartAnatomy[2] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Part of Anatomy Head parameter out of range"));
		}

		// 患者の傷病部位顔面
		alfInjuryAISPartAnatomy[3] = GetInitDataFloat( strPatientSectionName, "InjuryPartAnatomyFace", -1, strIniFullPath );
		if( 0 > alfInjuryAISPartAnatomy[3] || alfInjuryAISPartAnatomy[3] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Part of Anatomy Face parameter out of range"));
		}

		// 患者の傷病部位頸椎
		alfInjuryAISPartAnatomy[4] = GetInitDataFloat( strPatientSectionName, "InjuryPartAnatomyNeck", -1, strIniFullPath );
		if( 0 > alfInjuryAISPartAnatomy[4] || alfInjuryAISPartAnatomy[4] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Part of Anatomy Neck parameter out of range"));
		}

		// 患者の傷病部位胸部
		alfInjuryAISPartAnatomy[5] = GetInitDataFloat( strPatientSectionName, "InjuryPartAnatomyThorax", -1, strIniFullPath );
		if( 0 > alfInjuryAISPartAnatomy[5] || alfInjuryAISPartAnatomy[5] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Part of Anatomy Thorax parameter out of range"));
		}

		// 患者の傷病部位腹部
		alfInjuryAISPartAnatomy[6] = GetInitDataFloat( strPatientSectionName, "InjuryPartAnatomyAbdomen", -1, strIniFullPath );
		if( 0 > alfInjuryAISPartAnatomy[6] || alfInjuryAISPartAnatomy[6] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Part of Anatomy Lower Abdomen parameter out of range"));
		}

		// 患者の傷病部位脊椎
		alfInjuryAISPartAnatomy[7] = GetInitDataFloat( strPatientSectionName, "InjuryPartAnatomySpine", -1, strIniFullPath );
		if( 0 > alfInjuryAISPartAnatomy[7] || alfInjuryAISPartAnatomy[7] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Part of Anatomy Spine parameter out of range"));
		}

		// 患者の傷病部位上肢
		alfInjuryAISPartAnatomy[8] = GetInitDataFloat( strPatientSectionName, "InjuryPartAnatomyUpperExtremity", -1, strIniFullPath );
		if( 0 > alfInjuryAISPartAnatomy[8] || alfInjuryAISPartAnatomy[8] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Part of Anatomy Upper Extremity parameter out of range"));
		}

		// 患者の傷病部位下肢
		alfInjuryAISPartAnatomy[9] = GetInitDataFloat( strPatientSectionName, "InjuryPartAnatomyLowerExtremity", -1, strIniFullPath );
		if( 0 > alfInjuryAISPartAnatomy[9] || alfInjuryAISPartAnatomy[9] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Part of Anatomy Lower Extremity parameter out of range"));
		}

		// 患者の傷病部位熱傷及びその他
		alfInjuryAISPartAnatomy[10] = GetInitDataFloat( strPatientSectionName, "InjuryPartAnatomyUnspecified", -1, strIniFullPath );
		if( 0 > alfInjuryAISPartAnatomy[10] || alfInjuryAISPartAnatomy[10] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Part of Anatomy Unspecified parameter out of range"));
		}

		// 患者の傷病数の発生weibull分布のα
		alfInjuryAISNumber[0] = GetInitDataFloat( strPatientSectionName, "InjuryNumberAnatomyWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfInjuryAISNumber[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Number of Anatomy weibull parameter alpha out of range"));
		}

		// 患者の傷病数の発生weibull分布のβ
		alfInjuryAISNumber[1] = GetInitDataFloat( strPatientSectionName, "InjuryNumberPartAnatomyWeibullBeta", -1, strIniFullPath );
		if( 0 > alfInjuryAISNumber[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Number of Anatomy weibull parameter beta out of range"));
		}

		// 患者の傷病数1
		alfInjuryAISNumber[2] = GetInitDataFloat( strPatientSectionName, "InjuryNumberAnatomy1", -1, strIniFullPath );
		if( 0 > alfInjuryAISNumber[2] || alfInjuryAISNumber[2] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Number of Anatomy 1 parameter out of range"));
		}

		// 患者の傷病数2
		alfInjuryAISNumber[3] = GetInitDataFloat( strPatientSectionName, "InjuryNumberAnatomy2", -1, strIniFullPath );
		if( 0 > alfInjuryAISNumber[3] || alfInjuryAISNumber[3] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Number of Anatomy 2 parameter out of range"));
		}

		// 患者の傷病数3
		alfInjuryAISNumber[4] = GetInitDataFloat( strPatientSectionName, "InjuryNumberAnatomy3", -1, strIniFullPath );
		if( 0 > alfInjuryAISNumber[4] || alfInjuryAISNumber[4] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Number of Anatomy 3 parameter out of range"));
		}

		// 患者の傷病数4
		alfInjuryAISNumber[5] = GetInitDataFloat( strPatientSectionName, "InjuryNumberAnatomy4", -1, strIniFullPath );
		if( 0 > alfInjuryAISNumber[5] || alfInjuryAISNumber[5] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Number of 4 parameter out of range"));
		}

		// 患者の傷病数5
		alfInjuryAISNumber[6] = GetInitDataFloat( strPatientSectionName, "InjuryNumberAnatomy5", -1, strIniFullPath );
		if( 0 > alfInjuryAISNumber[6] || alfInjuryAISNumber[6] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Number of 5 parameter out of range"));
		}

		// 患者の傷病数6
		alfInjuryAISNumber[7] = GetInitDataFloat( strPatientSectionName, "InjuryNumberAnatomy6", -1, strIniFullPath );
		if( 0 > alfInjuryAISNumber[7] || alfInjuryAISNumber[7] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Number of Anatomy 6 parameter out of range"));
		}

		// 患者の傷病数7
		alfInjuryAISNumber[8] = GetInitDataFloat( strPatientSectionName, "InjuryNumberAnatomy7", -1, strIniFullPath );
		if( 0 > alfInjuryAISNumber[8] || alfInjuryAISNumber[8] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury number of Anatomy 7 parameter out of range"));
		}

		// 患者の傷病数8
		alfInjuryAISNumber[9] = GetInitDataFloat( strPatientSectionName, "InjuryNumberAnatomy8", -1, strIniFullPath );
		if( 0 > alfInjuryAISNumber[9] || alfInjuryAISNumber[9] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Injury Number of Anatomy 8 parameter out of range"));
		}

/*医師パラメータ*/
		// 医師の外傷（軽症）の緊急度判定の判定分布用weibull分布α
		alfDoctorJudgeMildTrauma[0] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeMildTraumaWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeMildTrauma[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Mild Trauma weibull parameter alpha out of range"));
		}

		// 医師の外傷（軽症）の緊急度判定の判定分布用weibull分布β
		alfDoctorJudgeMildTrauma[1] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeMildTraumaWeibullBeta", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeMildTrauma[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Mild Trauma weibull parameter beta out of range"));
		}

		// 医師の外傷（軽症）の緊急度3判定用パラメータ
		alfDoctorJudgeMildTrauma[2] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeMildTraumaEmergencyLevel3", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeMildTrauma[2] || alfDoctorJudgeMildTrauma[2] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Mild Trauma emergency level 3 parameter out of range"));
		}

		// 医師の外傷（軽症）の緊急度4判定用パラメータ
		alfDoctorJudgeMildTrauma[3] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeMildTraumaEmergencyLevel4", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeMildTrauma[3] || alfDoctorJudgeMildTrauma[3] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Mild Trauma emergency level 4 parameter out of range"));
		}

		// 医師の外傷（軽症）の緊急度5判定用パラメータ
		alfDoctorJudgeMildTrauma[4] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeMildTraumaEmergencyLevel5", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeMildTrauma[4] || alfDoctorJudgeMildTrauma[4] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Mild Trauma emergency level 5 parameter out of range"));
		}

		// 医師の外傷（中症）の緊急度判定の判定分布用weibull分布α
		alfDoctorJudgeModerateTrauma[0] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeModerateTraumaWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeModerateTrauma[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Moderate Trauma weibull parameter alpha out of range"));
		}

		// 医師の外傷（中症）の緊急度判定の判定分布用weibull分布β
		alfDoctorJudgeModerateTrauma[1] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeModerateTraumaWeibullBeta", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeModerateTrauma[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Moderate Trauma weibull parameter beta out of range"));
		}

		// 医師の外傷（中症）の緊急度3判定用パラメータ
		alfDoctorJudgeModerateTrauma[2] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeModerateTraumaEmergencyLevel3", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeModerateTrauma[2] || alfDoctorJudgeModerateTrauma[2] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Moderate Trauma emergency level 3 parameter out of range"));
		}

		// 医師の外傷（中症）の緊急度4判定用パラメータ
		alfDoctorJudgeModerateTrauma[3] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeModerateTraumaEmergencyLevel4", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeModerateTrauma[3] || alfDoctorJudgeModerateTrauma[3] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Moderate Trauma emergency level 4 parameter out of range"));
		}

		// 医師の外傷（中症）の緊急度5判定用パラメータ
		alfDoctorJudgeModerateTrauma[4] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeModerateTraumaEmergencyLevel5", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeModerateTrauma[4] || alfDoctorJudgeModerateTrauma[4] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Moderate Trauma emergency level 5 parameter out of range"));
		}

		// 医師の外傷（重症）の緊急度判定の判定分布用weibull分布α
		alfDoctorJudgeSevereTrauma[0] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeSevereTraumaWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeSevereTrauma[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Severe Trauma weibull parameter alpha out of range"));
		}

		// 医師の外傷（重症）の緊急度判定の判定分布用weibull分布β
		alfDoctorJudgeSevereTrauma[1] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeSevereTraumaWeibullBeta", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeSevereTrauma[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Severe Trauma weibull parameter beta out of range"));
		}

		// 医師の外傷（重症）の緊急度2判定用パラメータ
		alfDoctorJudgeSevereTrauma[2] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeSevereTraumaEmergencyLevel2", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeSevereTrauma[2] || alfDoctorJudgeSevereTrauma[2] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Severe Trauma emergency level 2 parameter out of range"));
		}

		// 医師の外傷（重症）の緊急度3判定用パラメータ
		alfDoctorJudgeSevereTrauma[3] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeSevereTraumaEmergencyLevel3", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeSevereTrauma[3] || alfDoctorJudgeSevereTrauma[3] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Severe Trauma emergency level 3 parameter out of range"));
		}

		// 医師の外傷（重症）の緊急度4判定用パラメータ
		alfDoctorJudgeSevereTrauma[4] = GetInitDataFloat( strDoctorSectionName, "DoctorJudgeSevereTraumaEmergencyLevel4", -1, strIniFullPath );
		if( 0 > alfDoctorJudgeSevereTrauma[4] || alfDoctorJudgeSevereTrauma[4] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Judge Severe Trauma emergency level 4 parameter out of range"));
		}

		// 医師の診察室の初療室及び退院判定(診断結果重症度3及び当初結果重症度5)
		alfDoctorImplementConsultationProcess[0] = GetInitDataFloat( strDoctorSectionName, "DoctorImplementConsultationJudgeEmergencyDischargeRate1", -1, strIniFullPath );
		if( 0 > alfDoctorImplementConsultationProcess[0] || alfDoctorImplementConsultationProcess[0] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Implement Consultation Judge Emergency Discharge Rate1 parameter out of range"));
		}

		// 医師の診察室の初療室及び退院判定(診断結果重症度3及び当初結果重症度4)
		alfDoctorImplementConsultationProcess[1] = GetInitDataFloat( strDoctorSectionName, "DoctorImplementConsultationJudgeEmergencyDischargeRate2", -1, strIniFullPath );
		if( 0 > alfDoctorImplementConsultationProcess[1] || alfDoctorImplementConsultationProcess[1] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Implement Consultation Judge Emergency Discharge Rate2 parameter out of range"));
		}

		// 医師の診察室の入院及び退院判定(診断結果重症度4及び当初結果重症度5)
		alfDoctorImplementConsultationProcess[2] = GetInitDataFloat( strDoctorSectionName, "DoctorImplementConsultationJudgeGeneralWardDischargeRate1", -1, strIniFullPath );
		if( 0 > alfDoctorImplementConsultationProcess[2] || alfDoctorImplementConsultationProcess[2] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Implement Consultation Judge General Ward Discharge Rate 1 parameter out of range"));
		}

		// 医師の診察室の入院室及び退院判定(診断結果重症度5及び当初結果重症度5)
		alfDoctorImplementConsultationProcess[3] = GetInitDataFloat( strDoctorSectionName, "DoctorImplementConsultationJudgeGeneralWardDischargeRate2", -1, strIniFullPath );
		if( 0 > alfDoctorImplementConsultationProcess[3] || alfDoctorImplementConsultationProcess[3] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Implement Consultation Judge General Ward Discharge Rate 2 parameter out of range"));
		}

		// 医師の診察室の入院室及び退院判定(診断結果重症度5及び当初結果重症度4)
		alfDoctorImplementConsultationProcess[4] = GetInitDataFloat( strDoctorSectionName, "DoctorImplementConsultationJudgeGeneralWardDischargeRate3", -1, strIniFullPath );
		if( 0 > alfDoctorImplementConsultationProcess[4] || alfDoctorImplementConsultationProcess[4] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Implement Consultation Judge General Ward Discharge Rate 3 parameter out of range"));
		}

		// 医師の診察室の入院室及び退院判定(診断結果重症度4及び当初結果重症度4)
		alfDoctorImplementConsultationProcess[5] = GetInitDataFloat( strDoctorSectionName, "DoctorImplementConsultationJudgeGeneralWardDischargeRate4", -1, strIniFullPath );
		if( 0 > alfDoctorImplementConsultationProcess[5] || alfDoctorImplementConsultationProcess[5] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Doctor Implement Consultation Judge General Ward Discharge Rate 4 parameter out of range"));
		}

/*看護師パラメータ*/
		// 看護師の外傷（軽症）の緊急度判定の判定分布用weibull分布α
		alfNurseJudgeMildTrauma[0] = GetInitDataFloat( strNurseSectionName, "NurseJudgeMildTraumaWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfNurseJudgeMildTrauma[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Mild Trauma weibull parameter alpha out of range"));
		}

		// 看護師の外傷（軽症）の緊急度判定の判定分布用weibull分布β
		alfNurseJudgeMildTrauma[1] = GetInitDataFloat( strNurseSectionName, "NurseJudgeMildTraumaWeibullBeta", -1, strIniFullPath );
		if( 0 > alfNurseJudgeMildTrauma[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Mild Trauma weibull parameter beta out of range"));
		}

		// 看護師の外傷（軽症）の緊急度3判定用パラメータ
		alfNurseJudgeMildTrauma[2] = GetInitDataFloat( strNurseSectionName, "NurseJudgeMildTraumaEmergencyLevel3", -1, strIniFullPath );
		if( 0 > alfNurseJudgeMildTrauma[2] || alfNurseJudgeMildTrauma[2] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Mild Trauma emergency level 3 parameter out of range"));
		}

		// 看護師の外傷（軽症）の緊急度4判定用パラメータ
		alfNurseJudgeMildTrauma[3] = GetInitDataFloat( strNurseSectionName, "NurseJudgeMildTraumaEmergencyLevel4", -1, strIniFullPath );
		if( 0 > alfNurseJudgeMildTrauma[3] || alfNurseJudgeMildTrauma[3] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Mild Trauma emergency level 4 parameter out of range"));
		}

		// 看護師の外傷（軽症）の緊急度5判定用パラメータ
		alfNurseJudgeMildTrauma[4] = GetInitDataFloat( strNurseSectionName, "NurseJudgeMildTraumaEmergencyLevel5", -1, strIniFullPath );
		if( 0 > alfNurseJudgeMildTrauma[4] || alfNurseJudgeMildTrauma[4] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Mild Trauma emergency level 5 parameter out of range"));
		}

		// 看護師の外傷（中症）の緊急度判定の判定分布用weibull分布α
		alfNurseJudgeModerateTrauma[0] = GetInitDataFloat( strNurseSectionName, "NurseJudgeModerateTraumaWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfNurseJudgeModerateTrauma[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Moderate Trauma weibull parameter alpha out of range"));
		}

		// 看護師の外傷（中症）の緊急度判定の判定分布用weibull分布β
		alfNurseJudgeModerateTrauma[1] = GetInitDataFloat( strNurseSectionName, "NurseJudgeModerateTraumaWeibullBeta", -1, strIniFullPath );
		if( 0 > alfNurseJudgeModerateTrauma[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Moderate Trauma weibull parameter beta out of range"));
		}

		// 看護師の外傷（中症）の緊急度5判定用パラメータ
		alfNurseJudgeModerateTrauma[2] = GetInitDataFloat( strNurseSectionName, "NurseJudgeModerateTraumaEmergencyLevel5", -1, strIniFullPath );
		if( 0 > alfNurseJudgeModerateTrauma[2] || alfNurseJudgeModerateTrauma[2] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Moderate Trauma emergency level 5 parameter out of range"));
		}

		// 看護師の外傷（中症）の緊急度4判定用パラメータ
		alfNurseJudgeModerateTrauma[3] = GetInitDataFloat( strNurseSectionName, "NurseJudgeModerateTraumaEmergencyLevel4", -1, strIniFullPath );
		if( 0 > alfNurseJudgeModerateTrauma[3] || alfNurseJudgeModerateTrauma[3] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Moderate Trauma emergency level 4 parameter out of range"));
		}

		// 看護師の外傷（中症）の緊急度3判定用パラメータ
		alfNurseJudgeModerateTrauma[4] = GetInitDataFloat( strNurseSectionName, "NurseJudgeModerateTraumaEmergencyLevel3", -1, strIniFullPath );
		if( 0 > alfNurseJudgeModerateTrauma[4] || alfNurseJudgeModerateTrauma[4] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Moderate Trauma emergency level 3 parameter out of range"));
		}

		// 看護師の外傷（重症）の緊急度判定の判定分布用weibull分布α
		alfNurseJudgeSevereTrauma[0] = GetInitDataFloat( strNurseSectionName, "NurseJudgeSevereTraumaWeibullAlpha", -1, strIniFullPath );
		if( 0 > alfNurseJudgeSevereTrauma[0] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Severe Trauma weibull parameter alpha out of range"));
		}

		// 看護師の外傷（重症）の緊急度判定の判定分布用weibull分布β
		alfNurseJudgeSevereTrauma[1] = GetInitDataFloat( strNurseSectionName, "NurseJudgeSevereTraumaWeibullBeta", -1, strIniFullPath );
		if( 0 > alfNurseJudgeSevereTrauma[1] )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Severe Trauma weibull parameter beta out of range"));
		}

		// 看護師の外傷（重症）の緊急度4判定用パラメータ
		alfNurseJudgeSevereTrauma[2] = GetInitDataFloat( strNurseSectionName, "NurseJudgeSevereTraumaEmergencyLevel4", -1, strIniFullPath );
		if( 0 > alfNurseJudgeSevereTrauma[2] || alfNurseJudgeSevereTrauma[2] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Severe Trauma emergency level 4 parameter out of range"));
		}

		// 看護師の外傷（重症）の緊急度3判定用パラメータ
		alfNurseJudgeSevereTrauma[3] = GetInitDataFloat( strNurseSectionName, "NurseJudgeSevereTraumaEmergencyLevel3", -1, strIniFullPath );
		if( 0 > alfNurseJudgeSevereTrauma[3] || alfNurseJudgeSevereTrauma[3] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Severe Trauma emergency level 3 parameter out of range"));
		}

		// 看護師の外傷（重症）の緊急度2判定用パラメータ
		alfNurseJudgeSevereTrauma[4] = GetInitDataFloat( strNurseSectionName, "NurseJudgeSevereTraumaEmergencyLevel2", -1, strIniFullPath );
		if( 0 > alfNurseJudgeSevereTrauma[4] || alfNurseJudgeSevereTrauma[4] > 1.0 )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("Nurse Judge Severe Trauma emergency level 2 parameter out of range"));
		}
	}

	/**
	 * <PRE>
	 *    ログ出力ファイル名の接頭辞を取得します。
	 * </PRE>
	 * @return ファイル名接頭辞
	 */
	public String strGetLogPrefix()
	{
		return strLogPrefix;
	}

	/**
	 * <PRE>
	 *    ログ出力ファイルパスディレクトリ名を取得します。
	 * </PRE>
	 * @return ディレクトリパス
	 */
	public String strGetLogDirectoryName()
	{
		return strLogDirectoryName;
	}

	/**
	 * <PRE>
	 *    ログ出力レベルを取得します。
	 * </PRE>
	 * @return ログ出力レベル
	 */
	public Level levGetLogLevel()
	{
		Level logLevel = Level.OFF;
		if( iLogLevel == 6 ) logLevel = Level.FINEST;
		else if( iLogLevel == 5 ) logLevel = Level.FINER;
		else if( iLogLevel == 4 ) logLevel = Level.FINE;
		else if( iLogLevel == 3 ) logLevel = Level.INFO;
		else if( iLogLevel == 2 ) logLevel = Level.WARNING;
		else if( iLogLevel == 1 ) logLevel = Level.SEVERE;
		else logLevel = Level.OFF;
		return logLevel;
	}

	/**
	 * <PRE>
	 *    ログ出力ファイルサイズを取得します。
	 * </PRE>
	 * @return ファイルサイズ
	 */
	public long lGetLogFileSize()
	{
		return lLogFileSize;
	}

	/**
	 * <PRE>
	 *    ログ出力ファイル数を取得します。
	 * </PRE>
	 * @return ログ出力ファイル数
	 */
	public long lGetLogFileCount()
	{
		return lLogFileCount;
	}

	/**
	 * <PRE>
	 *    ログ出力ファイル追記モードの可否を取得します。
	 * </PRE>
	 * @return ログファイル追記モード
	 */
	public boolean bGetLogFileAppend()
	{
		return bLogFileAppend;
	}

	/**
	 * <PRE>
	 *    患者の生存確率の重みパラメータを取得します。
	 * </PRE>
	 * @return 患者の生存確率の重みパラメータ
	 */
	public double lfGetSurvivalProbabilityWeight()
	{
		// 患者の生存確率の重みパラメータ
		return lfSurvivalProbabilityWeight;
	}

	/**
	 * <PRE>
	 *   患者の生死判定回数を取得します。
	 * </PRE>
	 * @return 患者の生死判定回数
	 */
	public double lfGetSurvivalJudgeCount()
	{
		return lfSurvivalJudgeCount;
	}

	/**
	 * <PRE>
	 *   患者のGCS値のweibull分布のαを取得します。
	 * </PRE>
	 * @return	weibull分布のα
	 */
	public double lfGetGcsScoreWeibullAlpha()
	{
		return alfGcsScore[0];
	}

	/**
	 * <PRE>
	 *   患者のGCS値のweibull分布のβを取得します。
	 * </PRE>
	 * @return	weibull分布のβ
	 */
	public double lfGetGcsScoreWeibullBeta()
	{
		return alfGcsScore[1];
	}

	/**
	 * <PRE>
	 *   患者のGCS値の15の発生確率を取得します。
	 * </PRE>
	 * @return	GCS=15の発生確率
	 */
	public double lfGetGcsScore15()
	{
		return alfGcsScore[2];
	}

	/**
	 * <PRE>
	 *   患者のGCS値の14の発生確率を取得します。
	 * </PRE>
	 * @return	GCS=14の発生確率
	 */
	public double lfGetGcsScore14()
	{
		return alfGcsScore[3];
	}

	/**
	 * <PRE>
	 *   患者のGCS値の13の発生確率を取得します。
	 * </PRE>
	 * @return	GCS=13の発生確率
	 */
	public double lfGetGcsScore13()
	{
		return alfGcsScore[4];
	}

	/**
	 * <PRE>
	 *   患者のGCS値の12の発生確率を取得します。
	 * </PRE>
	 * @return	GCS=12の発生確率
	 */
	public double lfGetGcsScore12()
	{
		return alfGcsScore[5];
	}

	/**
	 * <PRE>
	 *   患者のGCS値の11の発生確率を取得します。
	 * </PRE>
	 * @return	GCS=11の発生確率
	 */
	public double lfGetGcsScore11()
	{
		return alfGcsScore[6];
	}

	/**
	 * <PRE>
	 *   患者のGCS値の10の発生確率を取得します。
	 * </PRE>
	 * @return	GCS=10の発生確率
	 */
	public double lfGetGcsScore10()
	{
		return alfGcsScore[7];
	}

	/**
	 * <PRE>
	 *   患者のGCS値の9の発生確率を取得します。
	 * </PRE>
	 * @return	GCS=9の発生確率
	 */
	public double lfGetGcsScore9()
	{
		return alfGcsScore[8];
	}

	/**
	 * <PRE>
	 *   患者のGCS値の8の発生確率を取得します。
	 * </PRE>
	 * @return	GCS=8の発生確率
	 */
	public double lfGetGcsScore8()
	{
		return alfGcsScore[9];
	}

	/**
	 * <PRE>
	 *   患者のGCS値の7の発生確率を取得します。
	 * </PRE>
	 * @return	GCS=7の発生確率
	 */
	public double lfGetGcsScore7()
	{
		return alfGcsScore[10];
	}

	/**
	 * <PRE>
	 *   患者のGCS値の6の発生確率を取得します。
	 * </PRE>
	 * @return	GCS=6の発生確率
	 */
	public double lfGetGcsScore6()
	{
		return alfGcsScore[11];
	}

	/**
	 * <PRE>
	 *   患者のGCS値の5の発生確率を取得します。
	 * </PRE>
	 * @return	GCS=5の発生確率
	 */
	public double lfGetGcsScore5()
	{
		return alfGcsScore[12];
	}

	/**
	 * <PRE>
	 *   患者のGCS値の4の発生確率を取得します。
	 * </PRE>
	 * @return	GCS=4の発生確率
	 */
	public double lfGetGcsScore4()
	{
		return alfGcsScore[13];
	}

	/**
	 * <PRE>
	 *   患者のGCS値の3の発生確率を取得します。
	 * </PRE>
	 * @return	GCS=3の発生確率
	 */
	public double lfGetGcsScore3()
	{
		return alfGcsScore[14];
	}

	/**
	 * <PRE>
	 * 患者の頭部AIS調整パラメータweibull分布用α
	 * </PRE>
	 * @return 患者の頭部AIS調整パラメータweibull分布用α
	 */
	public double lfGetInternalAISHeadSeverityWeibullAlpha()
	{
		return alfInternalAISHeadSeverity[0];
	}

	/**
	 * <PRE>
	 * 患者の頭部AIS調整パラメータweibull分布用β
	 * </PRE>
	 * @return 患者の頭部AIS調整パラメータweibull分布用β
	 */
	public double lfGetInternalAISHeadSeverityWeibullBeta()
	{
		return alfInternalAISHeadSeverity[1];
	}

	/**
	 * <PRE>
	 * 患者の頭部AIS調整パラメータAIS=1
	 * </PRE>
	 * @return 患者の頭部AIS調整パラメータAIS=1
	 */
	public double lfGetInternalAISHeadSeverity1()
	{
		return alfInternalAISHeadSeverity[2];
	}

	/**
	 * <PRE>
	 * 患者の頭部AIS調整パラメータAIS=2
	 * </PRE>
	 * @return 患者の頭部AIS調整パラメータAIS=2
	 */
	public double lfGetInternalAISHeadSeverity2()
	{
		return alfInternalAISHeadSeverity[3];
	}

	/**
	 * <PRE>
	 * 患者の頭部AIS調整パラメータAIS=3
	 * </PRE>
	 * @return 患者の頭部AIS調整パラメータAIS=3
	 */
	public double lfGetInternalAISHeadSeverity3()
	{
		return alfInternalAISHeadSeverity[4];
	}

	/**
	 * <PRE>
	 * 患者の頭部AIS調整パラメータAIS=4
	 * </PRE>
	 * @return 患者の頭部AIS調整パラメータAIS=4
	 */
	public double lfGetInternalAISHeadSeverity4()
	{
		return alfInternalAISHeadSeverity[5];
	}

	/**
	 * <PRE>
	 * 患者の頭部AIS調整パラメータAIS=5
	 * </PRE>
	 * @return 患者の頭部AIS調整パラメータAIS=5
	 */
	public double lfGetInternalAISHeadSeverity5()
	{
		return alfInternalAISHeadSeverity[6];
	}

	/**
	 * <PRE>
	 * 患者の頭部AIS調整パラメータAIS=6
	 * </PRE>
	 * @return 患者の頭部AIS調整パラメータAIS=6
	 */
	public double lfGetInternalAISHeadSeverity6()
	{
		return alfInternalAISHeadSeverity[7];
	}

	/**
	 * <PRE>
	 * 患者の顔面AIS調整パラメータweibull分布用α
	 * </PRE>
	 * @return 患者の顔面AIS調整パラメータweibull分布用α
	 */
	public double lfGetInternalAISFaceSeverityWeibullAlpha()
	{
		return alfInternalAISFaceSeverity[0];
	}

	/**
	 * <PRE>
	 * 患者の顔面AIS調整パラメータweibull分布用β
	 * </PRE>
	 * @return 患者の顔面AIS調整パラメータweibull分布用β
	 */
	public double lfGetInternalAISFaceSeverityWeibullBeta()
	{
		return alfInternalAISFaceSeverity[1];
	}

	/**
	 * <PRE>
	 * 患者の顔面AIS調整パラメータAIS=1
	 * </PRE>
	 * @return 患者の顔面AIS調整パラメータAIS=1
	 */
	public double lfGetInternalAISFaceSeverity1()
	{
		return alfInternalAISFaceSeverity[2];
	}

	/**
	 * <PRE>
	 * 患者の顔面AIS調整パラメータAIS=2
	 * </PRE>
	 * @return 患者の顔面AIS調整パラメータAIS=2
	 */
	public double lfGetInternalAISFaceSeverity2()
	{
		return alfInternalAISFaceSeverity[3];
	}

	/**
	 * <PRE>
	 * 患者の顔面AIS調整パラメータAIS=3
	 * </PRE>
	 * @return 患者の顔面AIS調整パラメータAIS=3
	 */
	public double lfGetInternalAISFaceSeverity3()
	{
		return alfInternalAISFaceSeverity[4];
	}

	/**
	 * <PRE>
	 * 患者の顔面AIS調整パラメータAIS=4
	 * </PRE>
	 * @return 患者の顔面AIS調整パラメータAIS=4
	 */
	public double lfGetInternalAISFaceSeverity4()
	{
		return alfInternalAISFaceSeverity[5];
	}

	/**
	 * <PRE>
	 * 患者の顔面AIS調整パラメータAIS=5
	 * </PRE>
	 * @return 患者の顔面AIS調整パラメータAIS=5
	 */
	public double lfGetInternalAISFaceSeverity5()
	{
		return alfInternalAISFaceSeverity[6];
	}

	/**
	 * <PRE>
	 * 患者の顔面AIS調整パラメータAIS=6
	 * </PRE>
	 * @return 患者の顔面AIS調整パラメータAIS=6
	 */
	public double lfGetInternalAISFaceSeverity6()
	{
		return alfInternalAISFaceSeverity[7];
	}

	/**
	 * <PRE>
	 * 患者の頸椎AIS調整パラメータweibull分布用α
	 * </PRE>
	 * @return 患者の頸椎AIS調整パラメータweibull分布用α
	 */
	public double lfGetInternalAISNeckSeverityWeibullAlpha()
	{
		return alfInternalAISNeckSeverity[0];
	}

	/**
	 * <PRE>
	 * 患者の頸椎AIS調整パラメータweibull分布用β
	 * </PRE>
	 * @return 患者の頸椎AIS調整パラメータweibull分布用β
	 */
	public double lfGetInternalAISNeckSeverityWeibullBeta()
	{
		return alfInternalAISNeckSeverity[1];
	}

	/**
	 * <PRE>
	 * 患者の頸椎AIS調整パラメータAIS=1
	 * </PRE>
	 * @return 患者の頸椎AIS調整パラメータAIS=1
	 */
	public double lfGetInternalAISNeckSeverity1()
	{
		return alfInternalAISNeckSeverity[2];
	}

	/**
	 * <PRE>
	 * 患者の頸椎AIS調整パラメータAIS=2
	 * </PRE>
	 * @return 患者の頸椎AIS調整パラメータAIS=2
	 */
	public double lfGetInternalAISNeckSeverity2()
	{
		return alfInternalAISNeckSeverity[3];
	}

	/**
	 * <PRE>
	 * 患者の頸椎AIS調整パラメータAIS=3
	 * </PRE>
	 * @return 患者の頸椎AIS調整パラメータAIS=3
	 */
	public double lfGetInternalAISNeckSeverity3()
	{
		return alfInternalAISNeckSeverity[4];
	}

	/**
	 * <PRE>
	 * 患者の頸椎AIS調整パラメータAIS=4
	 * </PRE>
	 * @return 患者の頸椎AIS調整パラメータAIS=4
	 */
	public double lfGetInternalAISNeckSeverity4()
	{
		return alfInternalAISNeckSeverity[5];
	}

	/**
	 * <PRE>
	 * 患者の頸椎AIS調整パラメータAIS=5
	 * </PRE>
	 * @return 患者の頸椎AIS調整パラメータAIS=5
	 */
	public double lfGetInternalAISNeckSeverity5()
	{
		return alfInternalAISNeckSeverity[6];
	}

	/**
	 * <PRE>
	 * 患者の頸椎AIS調整パラメータAIS=6
	 * </PRE>
	 * @return 患者の頸椎AIS調整パラメータAIS=6
	 */
	public double lfGetInternalAISNeckSeverity6()
	{
		return alfInternalAISNeckSeverity[7];
	}

	/**
	 * <PRE>
	 * 患者の胸部AIS調整パラメータweibull分布用α
	 * </PRE>
	 * @return 患者の胸部AIS調整パラメータweibull分布用α
	 */
	public double lfGetInternalAISThoraxSeverityWeibullAlpha()
	{
		return alfInternalAISThoraxSeverity[0];
	}

	/**
	 * <PRE>
	 * 患者の胸部AIS調整パラメータweibull分布用β
	 * </PRE>
	 * @return 患者の胸部AIS調整パラメータweibull分布用β
	 */
	public double lfGetInternalAISThoraxSeverityWeibullBeta()
	{
		return alfInternalAISThoraxSeverity[1];
	}

	/**
	 * <PRE>
	 * 患者の胸部AIS調整パラメータAIS=1
	 * </PRE>
	 * @return 患者の胸部AIS調整パラメータAIS=1
	 */
	public double lfGetInternalAISThoraxSeverity1()
	{
		return alfInternalAISThoraxSeverity[2];
	}

	/**
	 * <PRE>
	 * 患者の胸部AIS調整パラメータAIS=2
	 * </PRE>
	 * @return 患者の胸部AIS調整パラメータAIS=2
	 */
	public double lfGetInternalAISThoraxSeverity2()
	{
		return alfInternalAISThoraxSeverity[3];
	}

	/**
	 * <PRE>
	 * 患者の胸部AIS調整パラメータAIS=3
	 * </PRE>
	 * @return 患者の胸部AIS調整パラメータAIS=3
	 */
	public double lfGetInternalAISThoraxSeverity3()
	{
		return alfInternalAISThoraxSeverity[4];
	}

	/**
	 * <PRE>
	 * 患者の胸部AIS調整パラメータAIS=4
	 * </PRE>
	 * @return 患者の胸部AIS調整パラメータAIS=4
	 */
	public double lfGetInternalAISThoraxSeverity4()
	{
		return alfInternalAISThoraxSeverity[5];
	}

	/**
	 * <PRE>
	 * 患者の胸部AIS調整パラメータAIS=5
	 * </PRE>
	 * @return 患者の胸部AIS調整パラメータAIS=5
	 */
	public double lfGetInternalAISThoraxSeverity5()
	{
		return alfInternalAISThoraxSeverity[6];
	}

	/**
	 * <PRE>
	 * 患者の胸部AIS調整パラメータAIS=6
	 * </PRE>
	 * @return 患者の胸部AIS調整パラメータAIS=6
	 */
	public double lfGetInternalAISThoraxSeverity6()
	{
		return alfInternalAISThoraxSeverity[7];
	}

	/**
	 * <PRE>
	 * 患者の腹部AIS調整パラメータweibull分布用α
	 * </PRE>
	 * @return 患者の腹部AIS調整パラメータweibull分布用α
	 */
	public double lfGetInternalAISAbdomenSeverityWeibullAlpha()
	{
		return alfInternalAISAbdomenSeverity[0];
	}

	/**
	 * <PRE>
	 * 患者の腹部AIS調整パラメータweibull分布用β
	 * </PRE>
	 * @return 患者の腹部AIS調整パラメータweibull分布用β
	 */
	public double lfGetInternalAISAbdomenSeverityWeibullBeta()
	{
		return alfInternalAISAbdomenSeverity[1];
	}

	/**
	 * <PRE>
	 * 患者の腹部AIS調整パラメータAIS=1
	 * </PRE>
	 * @return 患者の腹部AIS調整パラメータAIS=1
	 */
	public double lfGetInternalAISAbdomenSeverity1()
	{
		return alfInternalAISAbdomenSeverity[2];
	}

	/**
	 * <PRE>
	 * 患者の腹部AIS調整パラメータAIS=2
	 * </PRE>
	 * @return 患者の腹部AIS調整パラメータAIS=2
	 */
	public double lfGetInternalAISAbdomenSeverity2()
	{
		return alfInternalAISAbdomenSeverity[3];
	}

	/**
	 * <PRE>
	 * 患者の腹部AIS調整パラメータAIS=3
	 * </PRE>
	 * @return 患者の腹部AIS調整パラメータAIS=3
	 */
	public double lfGetInternalAISAbdomenSeverity3()
	{
		return alfInternalAISAbdomenSeverity[4];
	}

	/**
	 * <PRE>
	 * 患者の腹部AIS調整パラメータAIS=4
	 * </PRE>
	 * @return 患者の腹部AIS調整パラメータAIS=4
	 */
	public double lfGetInternalAISAbdomenSeverity4()
	{
		return alfInternalAISAbdomenSeverity[5];
	}

	/**
	 * <PRE>
	 * 患者の腹部AIS調整パラメータAIS=5
	 * </PRE>
	 * @return 患者の腹部AIS調整パラメータAIS=5
	 */
	public double lfGetInternalAISAbdomenSeverity5()
	{
		return alfInternalAISAbdomenSeverity[6];
	}

	/**
	 * <PRE>
	 * 患者の腹部AIS調整パラメータAIS=6
	 * </PRE>
	 * @return 患者の腹部AIS調整パラメータAIS=6
	 */
	public double lfGetInternalAISAbdomenSeverity6()
	{
		return alfInternalAISAbdomenSeverity[7];
	}

	/**
	 * <PRE>
	 * 患者の脊椎AIS調整パラメータweibull分布用α
	 * </PRE>
	 * @return 患者の脊椎AIS調整パラメータweibull分布用α
	 */
	public double lfGetInternalAISSpineSeverityWeibullAlpha()
	{
		return alfInternalAISSpineSeverity[0];
	}

	/**
	 * <PRE>
	 * 患者の脊椎AIS調整パラメータweibull分布用β
	 * </PRE>
	 * @return 患者の脊椎AIS調整パラメータweibull分布用β
	 */
	public double lfGetInternalAISSpineSeverityWeibullBeta()
	{
		return alfInternalAISSpineSeverity[1];
	}

	/**
	 * <PRE>
	 * 患者の脊椎AIS調整パラメータAIS=1
	 * </PRE>
	 * @return 患者の脊椎AIS調整パラメータAIS=1
	 */
	public double lfGetInternalAISSpineSeverity1()
	{
		return alfInternalAISSpineSeverity[2];
	}

	/**
	 * <PRE>
	 * 患者の脊椎AIS調整パラメータAIS=2
	 * </PRE>
	 * @return 患者の脊椎AIS調整パラメータAIS=2
	 */
	public double lfGetInternalAISSpineSeverity2()
	{
		return alfInternalAISSpineSeverity[3];
	}

	/**
	 * <PRE>
	 * 患者の脊椎AIS調整パラメータAIS=3
	 * </PRE>
	 * @return 患者の脊椎AIS調整パラメータAIS=3
	 */
	public double lfGetInternalAISSpineSeverity3()
	{
		return alfInternalAISSpineSeverity[4];
	}

	/**
	 * <PRE>
	 * 患者の脊椎AIS調整パラメータAIS=4
	 * </PRE>
	 * @return 患者の脊椎AIS調整パラメータAIS=4
	 */
	public double lfGetInternalAISSpineSeverity4()
	{
		return alfInternalAISSpineSeverity[5];
	}

	/**
	 * <PRE>
	 * 患者の脊椎AIS調整パラメータAIS=5
	 * </PRE>
	 * @return 患者の脊椎AIS調整パラメータAIS=5
	 */
	public double lfGetInternalAISSpineSeverity5()
	{
		return alfInternalAISSpineSeverity[6];
	}

	/**
	 * <PRE>
	 * 患者の脊椎AIS調整パラメータAIS=6
	 * </PRE>
	 * @return 患者の脊椎AIS調整パラメータAIS=6
	 */
	public double lfGetInternalAISSpineSeverity6()
	{
		return alfInternalAISSpineSeverity[7];
	}

	/**
	 * <PRE>
	 * 患者の上肢AIS調整パラメータweibull分布用α
	 * </PRE>
	 * @return 患者の上肢AIS調整パラメータweibull分布用α
	 */
	public double lfGetInternalAISUpperExtremitySeverityWeibullAlpha()
	{
		return alfInternalAISUpperExtremitySeverity[0];
	}

	/**
	 * <PRE>
	 * 患者の上肢AIS調整パラメータweibull分布用β
	 * </PRE>
	 * @return 患者の上肢AIS調整パラメータweibull分布用β
	 */
	public double lfGetInternalAISUpperExtremitySeverityWeibullBeta()
	{
		return alfInternalAISUpperExtremitySeverity[1];
	}

	/**
	 * <PRE>
	 * 患者の上肢AIS調整パラメータAIS=1
	 * </PRE>
	 * @return 患者の上肢AIS調整パラメータAIS=1
	 */
	public double lfGetInternalAISUpperExtremitySeverity1()
	{
		return alfInternalAISUpperExtremitySeverity[2];
	}

	/**
	 * <PRE>
	 * 患者の上肢AIS調整パラメータAIS=2
	 * </PRE>
	 * @return 患者の上肢AIS調整パラメータAIS=2
	 */
	public double lfGetInternalAISUpperExtremitySeverity2()
	{
		return alfInternalAISUpperExtremitySeverity[3];
	}

	/**
	 * <PRE>
	 * 患者の上肢AIS調整パラメータAIS=3
	 * </PRE>
	 * @return 患者の上肢AIS調整パラメータAIS=3
	 */
	public double lfGetInternalAISUpperExtremitySeverity3()
	{
		return alfInternalAISUpperExtremitySeverity[4];
	}

	/**
	 * <PRE>
	 * 患者の上肢AIS調整パラメータAIS=4
	 * </PRE>
	 * @return 患者の上肢AIS調整パラメータAIS=4
	 */
	public double lfGetInternalAISUpperExtremitySeverity4()
	{
		return alfInternalAISUpperExtremitySeverity[5];
	}

	/**
	 * <PRE>
	 * 患者の上肢AIS調整パラメータAIS=5
	 * </PRE>
	 * @return 患者の上肢AIS調整パラメータAIS=5
	 */
	public double lfGetInternalAISUpperExtremitySeverity5()
	{
		return alfInternalAISUpperExtremitySeverity[6];
	}

	/**
	 * <PRE>
	 * 患者の上肢AIS調整パラメータAIS=6
	 * </PRE>
	 * @return 患者の上肢AIS調整パラメータAIS=6
	 */
	public double lfGetInternalAISUpperExtremitySeverity6()
	{
		return alfInternalAISUpperExtremitySeverity[7];
	}

	/**
	 * <PRE>
	 * 患者の下肢AIS調整パラメータweibull分布用α
	 * </PRE>
	 * @return 患者の下肢AIS調整パラメータweibull分布用α
	 */
	public double lfGetInternalAISLowerExtremitySeverityWeibullAlpha()
	{
		return alfInternalAISLowerExtremitySeverity[0];
	}

	/**
	 * <PRE>
	 * 患者の下肢AIS調整パラメータweibull分布用β
	 * </PRE>
	 * @return 患者の下肢AIS調整パラメータweibull分布用β
	 */
	public double lfGetInternalAISLowerExtremitySeverityWeibullBeta()
	{
		return alfInternalAISLowerExtremitySeverity[1];
	}

	/**
	 * <PRE>
	 * 患者の下肢AIS調整パラメータAIS=1
	 * </PRE>
	 * @return 患者の下肢AIS調整パラメータAIS=1
	 */
	public double lfGetInternalAISLowerExtremitySeverity1()
	{
		return alfInternalAISLowerExtremitySeverity[2];
	}

	/**
	 * <PRE>
	 * 患者の下肢AIS調整パラメータAIS=2
	 * </PRE>
	 * @return 患者の下肢AIS調整パラメータAIS=2
	 */
	public double lfGetInternalAISLowerExtremitySeverity2()
	{
		return alfInternalAISLowerExtremitySeverity[3];
	}

	/**
	 * <PRE>
	 * 患者の下肢AIS調整パラメータAIS=3
	 * </PRE>
	 * @return 患者の下肢AIS調整パラメータAIS=3
	 */
	public double lfGetInternalAISLowerExtremitySeverity3()
	{
		return alfInternalAISLowerExtremitySeverity[4];
	}

	/**
	 * <PRE>
	 * 患者の下肢AIS調整パラメータAIS=4
	 * </PRE>
	 * @return 患者の下肢AIS調整パラメータAIS=4
	 */
	public double lfGetInternalAISLowerExtremitySeverity4()
	{
		return alfInternalAISLowerExtremitySeverity[5];
	}

	/**
	 * <PRE>
	 * 患者の下肢AIS調整パラメータAIS=5
	 * </PRE>
	 * @return 患者の下肢AIS調整パラメータAIS=5
	 */
	public double lfGetInternalAISLowerExtremitySeverity5()
	{
		return alfInternalAISLowerExtremitySeverity[6];
	}

	/**
	 * <PRE>
	 * 患者の下肢AIS調整パラメータAIS=6
	 * </PRE>
	 * @return 患者の下肢AIS調整パラメータAIS=6
	 */
	public double lfGetInternalAISLowerExtremitySeverity6()
	{
		return alfInternalAISLowerExtremitySeverity[7];
	}

	/**
	 * <PRE>
	 * 患者の熱傷及びそれ以外AIS調整パラメータweibull分布用α
	 * </PRE>
	 * @return 患者の熱傷及びそれ以外AIS調整パラメータweibull分布用α
	 */
	public double lfGetInternalAISUnspecifiedSeverityWeibullAlpha()
	{
		return alfInternalAISUnspecifiedSeverity[0];
	}

	/**
	 * <PRE>
	 * 患者の熱傷及びそれ以外AIS調整パラメータweibull分布用β
	 * </PRE>
	 * @return 患者の熱傷及びそれ以外AIS調整パラメータweibull分布用β
	 */
	public double lfGetInternalAISUnspecifiedSeverityWeibullBeta()
	{
		return alfInternalAISUnspecifiedSeverity[1];
	}

	/**
	 * <PRE>
	 * 患者の熱傷及びそれ以外AIS調整パラメータAIS=1
	 * </PRE>
	 * @return 患者の熱傷及びそれ以外AIS調整パラメータAIS=1
	 */
	public double lfGetInternalAISUnspecifiedSeverity1()
	{
		return alfInternalAISUnspecifiedSeverity[2];
	}

	/**
	 * <PRE>
	 * 患者の熱傷及びそれ以外AIS調整パラメータAIS=2
	 * </PRE>
	 * @return 患者の熱傷及びそれ以外AIS調整パラメータAIS=2
	 */
	public double lfGetInternalAISUnspecifiedSeverity2()
	{
		return alfInternalAISUnspecifiedSeverity[3];
	}

	/**
	 * <PRE>
	 * 患者の熱傷及びそれ以外AIS調整パラメータAIS=3
	 * </PRE>
	 * @return 患者の熱傷及びそれ以外AIS調整パラメータAIS=3
	 */
	public double lfGetInternalAISUnspecifiedSeverity3()
	{
		return alfInternalAISUnspecifiedSeverity[4];
	}

	/**
	 * <PRE>
	 * 患者の熱傷及びそれ以外AIS調整パラメータAIS=4
	 * </PRE>
	 * @return 患者の熱傷及びそれ以外AIS調整パラメータAIS=4
	 */
	public double lfGetInternalAISUnspecifiedSeverity4()
	{
		return alfInternalAISUnspecifiedSeverity[5];
	}

	/**
	 * <PRE>
	 * 患者の熱傷及びそれ以外AIS調整パラメータAIS=5
	 * </PRE>
	 * @return 患者の熱傷及びそれ以外AIS調整パラメータAIS=5
	 */
	public double lfGetInternalAISUnspecifiedSeverity5()
	{
		return alfInternalAISUnspecifiedSeverity[6];
	}

	/**
	 * <PRE>
	 * 患者の熱傷及びそれ以外AIS調整パラメータAIS=6
	 * </PRE>
	 * @return 患者の熱傷及びそれ以外AIS調整パラメータAIS=6
	 */
	public double lfGetInternalAISUnspecifiedSeverity6()
	{
		return alfInternalAISUnspecifiedSeverity[7];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生部位調整パラメータweibull分布用α
	 * </PRE>
	 * @return 患者の外傷発生部位調整パラメータweibull分布用α
	 */
	public double lfGetInjuryAISPartAnatomyWeibullAlpha()
	{
		return alfInjuryAISPartAnatomy[0];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生部位調整パラメータweibull分布用β
	 * </PRE>
	 * @return 患者の外傷発生部位調整パラメータweibull分布用β
	 */
	public double lfGetInjuryAISPartAnatomyWeibullBeta()
	{
		return alfInjuryAISPartAnatomy[1];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生部位調整パラメータ頭部
	 * </PRE>
	 * @return 患者の外傷発生部位調整パラメータ頭部
	 */
	public double lfGetInjuryAISPartAnatomyHead()
	{
		return alfInjuryAISPartAnatomy[2];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生部位調整パラメータ顔面
	 * </PRE>
	 * @return 患者の外傷発生部位調整パラメータ顔面
	 */
	public double lfGetInjuryAISPartAnatomyFace()
	{
		return alfInjuryAISPartAnatomy[3];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生部位調整パラメータ頸椎
	 * </PRE>
	 * @return 患者の外傷発生部位調整パラメータ頸椎
	 */
	public double lfGetInjuryAISPartAnatomyNeck()
	{
		return alfInjuryAISPartAnatomy[4];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生部位調整パラメータ胸部
	 * </PRE>
	 * @return 患者の外傷発生部位調整パラメータ胸部
	 */
	public double lfGetInjuryAISPartAnatomyThorax()
	{
		return alfInjuryAISPartAnatomy[5];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生部位調整パラメータ腹部
	 * </PRE>
	 * @return 患者の外傷発生部位調整パラメータ腹部
	 */
	public double lfGetInjuryAISPartAnatomyAbdomen()
	{
		return alfInjuryAISPartAnatomy[6];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生部位調整パラメータ脊椎
	 * </PRE>
	 * @return 患者の外傷発生部位調整パラメータ脊椎
	 */
	public double lfGetInjuryAISPartAnatomySpine()
	{
		return alfInjuryAISPartAnatomy[7];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生部位調整パラメータ上肢
	 * </PRE>
	 * @return 患者の外傷発生部位調整パラメータ上肢
	 */
	public double lfGetInjuryAISPartAnatomyUpperExtremity()
	{
		return alfInjuryAISPartAnatomy[8];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生部位調整パラメータ下肢
	 * </PRE>
	 * @return 患者の外傷発生部位調整パラメータ下肢
	 */
	public double lfGetInjuryAISPartAnatomyLowerExtremity()
	{
		return alfInjuryAISPartAnatomy[9];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生部位調整パラメータ熱傷及びその他
	 * </PRE>
	 * @return 患者の外傷発生部位調整パラメータ熱傷及びその他
	 */
	public double lfGetInjuryAISPartAnatomyUnspecified()
	{
		return alfInjuryAISPartAnatomy[10];
	}

	/**
	 * <PRE>
	 * 患者の外傷Numbereibull分布用α
	 * </PRE>
	 * @return 患者の外傷発生数調整パラメータweibull分布用α
	 */
	public double lfGetInjuryAISNumberWeibullAlpha()
	{
		return alfInjuryAISNumber[0];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生数調整パラメータweibull分布用β
	 * </PRE>
	 * @return 患者の外傷発生数調整パラメータweibull分布用β
	 */
	public double lfGetInjuryAISNumberWeibullBeta()
	{
		return alfInjuryAISNumber[1];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生数1
	 * </PRE>
	 * @return 患者の外傷発生数1
	 */
	public double lfGetInjuryAISNumber1()
	{
		return alfInjuryAISNumber[2];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生数2
	 * </PRE>
	 * @return 患者の外傷発生数2
	 */
	public double lfGetInjuryAISNumber2()
	{
		return alfInjuryAISNumber[3];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生数3
	 * </PRE>
	 * @return 患者の外傷発生数3
	 */
	public double lfGetInjuryAISNumber3()
	{
		return alfInjuryAISNumber[4];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生数4
	 * </PRE>
	 * @return 患者の外傷発生数4
	 */
	public double lfGetInjuryAISNumber4()
	{
		return alfInjuryAISNumber[5];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生数5
	 * </PRE>
	 * @return 患者の外傷発生数5
	 */
	public double lfGetInjuryAISNumber5()
	{
		return alfInjuryAISNumber[6];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生数6
	 * </PRE>
	 * @return 患者の外傷発生数6
	 */
	public double lfGetInjuryAISNumber6()
	{
		return alfInjuryAISNumber[7];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生数7
	 * </PRE>
	 * @return 患者の外傷発生数7
	 */
	public double lfGetInjuryAISNumber7()
	{
		return alfInjuryAISNumber[8];
	}

	/**
	 * <PRE>
	 * 患者の外傷発生数8
	 * </PRE>
	 * @return 患者の外傷発生数8
	 */
	public double lfGetInjuryAISNumber8()
	{
		return alfInjuryAISNumber[9];
	}

	/**
	 * <PRE>
	 * 傷病状態（軽症）医師の判定確率の発生確率weibull分布のα
	 * </PRE>
	 * @return 傷病状態（軽症）医師の判定確率の発生確率weibull分布のα
	 */
	public double lfGetDoctorJudgeMildTraumaWeibullAlpha()
	{
		return alfDoctorJudgeMildTrauma[0];
	}

	/**
	 * <PRE>
	 * 傷病状態（軽症）医師の判定確率の発生確率weibull分布のβ
	 * </PRE>
	 * @return 傷病状態（軽症）医師の判定確率の発生確率weibull分布のβ
	 */
	public double lfGetDoctorJudgeMildTraumaWeibullBeta()
	{
		return alfDoctorJudgeMildTrauma[1];
	}

	/**
	 * <PRE>
	 * 傷病状態（軽症）医師の判定確率の発生確率度数3
	 * </PRE>
	 * @return 傷病状態（軽症）医師の判定確率の発生確率度数3
	 */
	public double lfGetDoctorJudgeMildTrauma3()
	{
		return alfDoctorJudgeMildTrauma[2];
	}

	/**
	 * <PRE>
	 * 傷病状態（軽症）医師の判定確率の発生確率度数4
	 * </PRE>
	 * @return 傷病状態（軽症）医師の判定確率の発生確率度数4
	 */
	public double lfGetDoctorJudgeMildTrauma4()
	{
		return alfDoctorJudgeMildTrauma[3];
	}

	/**
	 * <PRE>
	 * 傷病状態（軽症）医師の判定確率の発生確率度数5
	 * </PRE>
	 * @return 傷病状態（軽症）医師の判定確率の発生確率度数5
	 */
	public double lfGetDoctorJudgeMildTrauma5()
	{
		return alfDoctorJudgeMildTrauma[4];
	}

	/**
	 * <PRE>
	 * 傷病状態（中症）医師の判定確率の発生確率weibull分布のα
	 * </PRE>
	 * @return 傷病状態（中症）医師の判定確率の発生確率weibull分布のα
	 */
	public double lfGetDoctorJudgeModerateTraumaWeibullAlpha()
	{
		return alfDoctorJudgeModerateTrauma[0];
	}

	/**
	 * <PRE>
	 * 傷病状態（中症）医師の判定確率の発生確率weibull分布のβ
	 * </PRE>
	 * @return 傷病状態（中症）医師の判定確率の発生確率weibull分布のβ
	 */
	public double lfGetDoctorJudgeModerateTraumaWeibullBeta()
	{
		return alfDoctorJudgeModerateTrauma[1];
	}

	/**
	 * <PRE>
	 * 傷病状態（中症）医師の判定確率の発生確率度数3
	 * </PRE>
	 * @return 傷病状態（中症）医師の判定確率の発生確率度数3
	 */
	public double lfGetDoctorJudgeModerateTrauma3()
	{
		return alfDoctorJudgeModerateTrauma[2];
	}

	/**
	 * <PRE>
	 * 傷病状態（中症）医師の判定確率の発生確率度数4
	 * </PRE>
	 * @return 傷病状態（中症）医師の判定確率の発生確率度数4
	 */
	public double lfGetDoctorJudgeModerateTrauma4()
	{
		return alfDoctorJudgeModerateTrauma[3];
	}

	/**
	 * <PRE>
	 * 傷病状態（中症）医師の判定確率の発生確率度数5
	 * </PRE>
	 * @return 傷病状態（中症）医師の判定確率の発生確率度数5
	 */
	public double lfGetDoctorJudgeModerateTrauma5()
	{
		return alfDoctorJudgeModerateTrauma[4];
	}

	/**
	 * <PRE>
	 * 傷病状態（重症）医師の判定確率の発生確率weibull分布のα
	 * </PRE>
	 * @return 傷病状態（重症）医師の判定確率の発生確率weibull分布のα
	 */
	public double lfGetDoctorJudgeSevereTraumaWeibullAlpha()
	{
		return alfDoctorJudgeSevereTrauma[0];
	}

	/**
	 * <PRE>
	 * 傷病状態（重症）医師の判定確率の発生確率weibull分布のβ
	 * </PRE>
	 * @return 傷病状態（重症）医師の判定確率の発生確率weibull分布のβ
	 */
	public double lfGetDoctorJudgeSevereTraumaWeibullBeta()
	{
		return alfDoctorJudgeSevereTrauma[1];
	}

	/**
	 * <PRE>
	 * 傷病状態（重症）医師の判定確率の発生確率度数2
	 * </PRE>
	 * @return 傷病状態（重症）医師の判定確率の発生確率度数2
	 */
	public double lfGetDoctorJudgeSevereTrauma2()
	{
		return alfDoctorJudgeSevereTrauma[2];
	}

	/**
	 * <PRE>
	 * 傷病状態（重症）医師の判定確率の発生確率度数3
	 * </PRE>
	 * @return 傷病状態（重症）医師の判定確率の発生確率度数3
	 */
	public double lfGetDoctorJudgeSevereTrauma3()
	{
		return alfDoctorJudgeSevereTrauma[3];
	}

	/**
	 * <PRE>
	 * 傷病状態（重症）医師の判定確率の発生確率度数4
	 * </PRE>
	 * @return 傷病状態（重症）医師の判定確率の発生確率度数4
	 */
	public double lfGetDoctorJudgeSevereTrauma4()
	{
		return alfDoctorJudgeSevereTrauma[4];
	}

	/**
	 * <PRE>
	 * 医師の診察室の初療室及び退院判定(診断結果重症度3及び当初結果重症度5)
	 * </PRE>
	 * @return 医師の診察室の初療室及び退院判定確率
	 */
	public double lfGetDoctorImplementConsultationJudgeEmergencyDischargeRate1()
	{
		return alfDoctorImplementConsultationProcess[0];
	}

	/**
	 * <PRE>
	 * 医師の診察室の初療室及び退院判定(診断結果重症度3及び当初結果重症度4)
	 * </PRE>
	 * @return 医師の診察室の初療室及び退院判定確率
	 */
	public double lfGetDoctorImplementConsultationJudgeEmergencyDischargeRate2()
	{
		return alfDoctorImplementConsultationProcess[1];
	}

	/**
	 * <PRE>
	 * 医師の診察室の入院及び退院判定(診断結果重症度4及び当初結果重症度5)
	 * </PRE>
	 * @return 医師の診察室の入院及び退院判定確率
	 */
	public double lfGetDoctorImplementConsultationJudgeGeneralWardDischargeRate1()
	{
		return alfDoctorImplementConsultationProcess[2];
	}

	/**
	 * <PRE>
	 * 医師の診察室の入院及び退院判定(診断結果重症度5及び当初結果重症度5)
	 * </PRE>
	 * @return 医師の診察室の入院及び退院判定確率
	 */
	public double lfGetDoctorImplementConsultationJudgeGeneralWardDischargeRate2()
	{
		return alfDoctorImplementConsultationProcess[3];
	}

	/**
	 * <PRE>
	 * 医師の診察室の入院及び退院判定(診断結果重症度5及び当初結果重症度4)
	 * </PRE>
	 * @return 医師の診察室の入院及び退院判定確率
	 */
	public double lfGetDoctorImplementConsultationJudgeGeneralWardDischargeRate3()
	{
		return alfDoctorImplementConsultationProcess[4];
	}

	/**
	 * <PRE>
	 * 医師の診察室の入院及び退院判定(診断結果重症度4及び当初結果重症度4)
	 * </PRE>
	 * @return 医師の診察室の入院及び退院判定確率
	 */
	public double lfGetDoctorImplementConsultationJudgeGeneralWardDischargeRate4()
	{
		return alfDoctorImplementConsultationProcess[5];
	}

	/**
	 * <PRE>
	 * 傷病状態（軽症）看護師の判定確率の発生確率weibull分布のα
	 * </PRE>
	 * @return 傷病状態（軽症）看護師の判定確率の発生確率weibull分布のα
	 */
	public double lfGetNurseJudgeMildTraumaWeibullAlpha()
	{
		return alfNurseJudgeMildTrauma[0];
	}

	/**
	 * <PRE>
	 * 傷病状態（軽症）看護師の判定確率の発生確率weibull分布のβ
	 * </PRE>
	 * @return 傷病状態（軽症）看護師の判定確率の発生確率weibull分布のβ
	 */
	public double lfGetNurseJudgeMildTraumaWeibullBeta()
	{
		return alfNurseJudgeMildTrauma[1];
	}

	/**
	 * <PRE>
	 * 傷病状態（軽症）看護師の判定確率の発生確率度数3
	 * </PRE>
	 * @return 傷病状態（軽症）看護師の判定確率の発生確率度数3
	 */
	public double lfGetNurseJudgeMildTrauma3()
	{
		return alfNurseJudgeMildTrauma[2];
	}

	/**
	 * <PRE>
	 * 傷病状態（軽症）看護師の判定確率の発生確率度数4
	 * </PRE>
	 * @return 傷病状態（軽症）看護師の判定確率の発生確率度数4
	 */
	public double lfGetNurseJudgeMildTrauma4()
	{
		return alfNurseJudgeMildTrauma[3];
	}

	/**
	 * <PRE>
	 * 傷病状態（軽症）看護師の判定確率の発生確率度数5
	 * </PRE>
	 * @return 傷病状態（軽症）看護師の判定確率の発生確率度数5
	 */
	public double lfGetNurseJudgeMildTrauma5()
	{
		return alfNurseJudgeMildTrauma[4];
	}

	/**
	 * <PRE>
	 * 傷病状態（中症）看護師の判定確率の発生確率weibull分布のα
	 * </PRE>
	 * @return 傷病状態（中症）看護師の判定確率の発生確率weibull分布のα
	 */
	public double lfGetNurseJudgeModerateTraumaWeibullAlpha()
	{
		return alfNurseJudgeModerateTrauma[0];
	}

	/**
	 * <PRE>
	 * 傷病状態（中症）看護師の判定確率の発生確率weibull分布のβ
	 * </PRE>
	 * @return 傷病状態（中症）看護師の判定確率の発生確率weibull分布のβ
	 */
	public double lfGetNurseJudgeModerateTraumaWeibullBeta()
	{
		return alfNurseJudgeModerateTrauma[1];
	}

	/**
	 * <PRE>
	 * 傷病状態（中症）看護師の判定確率の発生確率度数3
	 * </PRE>
	 * @return 傷病状態（中症）看護師の判定確率の発生確率度数3
	 */
	public double lfGetNurseJudgeModerateTrauma3()
	{
		return alfNurseJudgeModerateTrauma[2];
	}

	/**
	 * <PRE>
	 * 傷病状態（中症）看護師の判定確率の発生確率度数4
	 * </PRE>
	 * @return 傷病状態（中症）看護師の判定確率の発生確率度数4
	 */
	public double lfGetNurseJudgeModerateTrauma4()
	{
		return alfNurseJudgeModerateTrauma[3];
	}

	/**
	 * <PRE>
	 * 傷病状態（中症）看護師の判定確率の発生確率度数5
	 * </PRE>
	 * @return 傷病状態（中症）看護師の判定確率の発生確率度数5
	 */
	public double lfGetNurseJudgeModerateTrauma5()
	{
		return alfNurseJudgeModerateTrauma[4];
	}

	/**
	 * <PRE>
	 * 傷病状態（重症）看護師の判定確率の発生確率weibull分布のα
	 * </PRE>
	 * @return 傷病状態（重症）看護師の判定確率の発生確率weibull分布のα
	 */
	public double lfGetNurseJudgeSevereTraumaWeibullAlpha()
	{
		return alfNurseJudgeSevereTrauma[0];
	}

	/**
	 * <PRE>
	 * 傷病状態（重症）看護師の判定確率の発生確率weibull分布のβ
	 * </PRE>
	 * @return 傷病状態（重症）看護師の判定確率の発生確率weibull分布のβ
	 */
	public double lfGetNurseJudgeSevereTraumaWeibullBeta()
	{
		return alfNurseJudgeSevereTrauma[1];
	}

	/**
	 * <PRE>
	 * 傷病状態（重症）看護師の判定確率の発生確率度数2
	 * </PRE>
	 * @return 傷病状態（重症）看護師の判定確率の発生確率度数2
	 */
	public double lfGetNurseJudgeSevereTrauma2()
	{
		return alfNurseJudgeSevereTrauma[2];
	}

	/**
	 * <PRE>
	 * 傷病状態（重症）看護師の判定確率の発生確率度数3
	 * </PRE>
	 * @return 傷病状態（重症）看護師の判定確率の発生確率度数3
	 */
	public double lfGetNurseJudgeSevereTrauma3()
	{
		return alfNurseJudgeSevereTrauma[3];
	}

	/**
	 * <PRE>
	 * 傷病状態（重症）看護師の判定確率の発生確率度数4
	 * </PRE>
	 * @return 傷病状態（重症）看護師の判定確率の発生確率度数4
	 */
	public double lfGetNurseJudgeSevereTrauma4()
	{
		return alfNurseJudgeSevereTrauma[4];
	}
}