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
		String strIniFileName			= "erEV.ini";
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

}
