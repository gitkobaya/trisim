package utility.initparam.initsettingfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public abstract class InitSettingFileRead
{
	/**
	 * <PRE>
	 *	*.iniファイルに設定された値を読み込みます。
	 * </PRE>
	 * @throws IllegalArgumentException   設定パラメータにフォーマット誤りがある場合に例外
	 * @throws IOException                ファイル読み込み時にエラーが発生した場合に例外
	 * @author kobayashi
	 * @since	2017/3/8
	 * @version 0.1
	 */
	abstract public void readInitSettingFile() throws IllegalArgumentException, IOException;

	/**
	 *  <PRE>
	 *	*.iniファイルのパラメーター値(文字列)を読み込みます。
	 * </PRE>
	 *	@param strSectionName		属するセクションの名前
	 *	@param strParamName			パラメーターの名前
	 *	@param strDefaultParamValue	パラメーターのデフォルト値
	 *	@param strIniFilePath		ファイルのパス名
	 *	@return 					読み込んだパラメータ
	 *	@throws IOException			ファイル読み込みエラー
	 *	@author kobayashi
	 *	@since	0.1
	 *	@version 0.1 2017/03/01
	 */
	protected String GetInitDataString(String strSectionName, String strParamName, String strDefaultParamValue, String strIniFilePath ) throws IOException
	{
		String strRes = "";
		String strReadData;
		String strAbsolutePath;
		String[] strParamData;
		File file;
		BufferedReader brReadFile = null;

		file = new File(strIniFilePath);
		strAbsolutePath = file.getAbsolutePath();

		brReadFile = new BufferedReader( new FileReader( strAbsolutePath ) );

		//セクション名の読み込み
		while( (strReadData = brReadFile.readLine()) != null )
		{
			// コメント文なので読み飛ばします。
			if( strReadData.indexOf(";") != -1 ) continue;
			// 指定されたセクション名があったらそこの次の行から読み始めます。
			if( strReadData.indexOf(strSectionName) != -1 ) break;
		}
		// パラメータの読み込みを開始します。
		while( (strReadData = brReadFile.readLine()) != null )
		{
			// コメント文なので読み飛ばします。
			if( strReadData.indexOf(";") != -1 ) continue;
			// 指定されたパラメータ名があったらパラメータを読み込みます。
			if( strReadData.indexOf(strParamName) != -1 )
			{
				// "="," ","""で分割します。
				strParamData = strReadData.split("[=\"]");
				for( String str : strParamData )
				{
					if( str.equals("") == false && str.equals(" ") == false && str.indexOf(strParamName) == -1 )
					{
						// パラメータを取得します。
						strRes = str;
						break;
					}
				}
				if( strRes.equals("") == true )
				{
					strRes = strDefaultParamValue;
				}
				break;
			}
		}
		brReadFile.close();
		return strRes;
	}

	/**
	 * <PRE>
	 *   *.iniファイルのパラメーター値(浮動小数点型)を読み込みます。
	 * </PRE>
	 * @param strSectionName			属するセクションの名前
	 * @param strParamName				パラメーターの名前
	 * @param lfDefaultParamValue		パラメーターのデフォルト値
	 * @param strIniFilePath			初期ファイルパス名
	 * @return 							読み込んだパラメータ値
	 * @throws IOException				ファイル読み込みエラー
	 * @author kobayashi
	 * @since   0.1
	 * @version 0.1 2017/03/01
	 */
	protected double GetInitDataFloat(String strSectionName, String strParamName, double lfDefaultParamValue, String strIniFilePath ) throws IOException
	{
		String strReadData;
		String strAbsolutePath;
		String[] strParamData;
		File file;
		BufferedReader brReadFile = null;
		double lfRes = Double.MAX_VALUE;

		file = new File(strIniFilePath);
		strAbsolutePath = file.getAbsolutePath();

		brReadFile = new BufferedReader( new FileReader( strAbsolutePath ) );

		//セクション名の読み込み
		while( (strReadData = brReadFile.readLine()) != null )
		{
			// コメント文なので読み飛ばします。
			if( strReadData.indexOf(";") != -1 ) continue;
			// 指定されたセクション名があったらそこの次の行から読み始めます。
			if( strReadData.indexOf(strSectionName) != -1 ) break;
		}
		// パラメータの読み込みを開始します。
		while( (strReadData = brReadFile.readLine()) != null )
		{
			// コメント文なので読み飛ばします。
			if( strReadData.indexOf(";") != -1 ) continue;
			// 指定されたパラメータ名があったらパラメータを読み込みます。
			if( strReadData.indexOf(strParamName) != -1 )
			{
				// "="," ","""で分割します。
				strParamData = strReadData.split("[=\\s\"]");
				for( String str : strParamData )
				{
					if( str.equals("") == false && str.equals(" ") == false && str.indexOf(strParamName) == -1 )
					{
						// パラメータを取得します。
						try
						{
							// あった場合はその値を設定します。
							lfRes = Double.parseDouble(str);
						}
						catch( NumberFormatException nfe )
						{
							// 数値以外のものが指定されていた場合はデフォルト値を指定します。
							lfRes = lfDefaultParamValue;
						}
						break;
					}
				}
				// 指定されていなかった場合はデフォルト値を設定します。
				if( lfRes == Double.MAX_VALUE )
				{
					lfRes = lfDefaultParamValue;
				}
				break;
			}
		}
		brReadFile.close();
		return lfRes;
	}

	/**
	 * <PRE>
	 *   *.iniファイルのパラメーター値(整数型)を読み込みます。
	 * </PRE>
	 * @param strSectionName			属するセクションの名前
	 * @param strParamName				パラメーターの名前
	 * @param lDefaultParamValue		パラメーターのデフォルト値
	 * @param strIniFilePath			ファイルパス名
	 * @return 							読み込んだパラメータ値
	 * @throws IOException				ファイル読み込みエラー
	 * @author kobayashi
	 * @since   0.1
	 * @version 0.1 2017/03/01
	 */
	protected long GetInitDataInt(String strSectionName, String strParamName, long lDefaultParamValue, String strIniFilePath ) throws IOException
	{
		String strReadData;
		String strAbsolutePath;
		String[] strParamData;
		File file;
		BufferedReader brReadFile = null;
		long lRes = Long.MAX_VALUE;

		file = new File(strIniFilePath);
		strAbsolutePath = file.getAbsolutePath();

		brReadFile = new BufferedReader( new FileReader( strAbsolutePath ) );

		//セクション名の読み込み
		while( (strReadData = brReadFile.readLine()) != null )
		{
			// コメント文なので読み飛ばします。
			if( strReadData.indexOf(";") != -1 ) continue;
			// 指定されたセクション名があったらそこの次の行から読み始めます。
			if( strReadData.indexOf(strSectionName) != -1 ) break;
		}
		// パラメータの読み込みを開始します。
		while( (strReadData = brReadFile.readLine()) != null )
		{
			// コメント文なので読み飛ばします。
			if( strReadData.indexOf(";") != -1 ) continue;
			// 指定されたパラメータ名があったらパラメータを読み込みます。
			if( strReadData.indexOf(strParamName) != -1 )
			{
				// "="," ","""で分割します。
				strParamData = strReadData.split("[=\\s\"]");
				for( String str : strParamData )
				{
					if( str.equals("") == false && str.equals(" ") == false && str.indexOf(strParamName) == -1 )
					{
						// パラメータを取得します。
						try
						{
							// あった場合はその値を設定します。
							lRes = Long.parseLong(str);
						}
						catch( NumberFormatException nfe )
						{
							// 数値以外のものが指定されていた場合はデフォルト値を指定します。
							lRes = lDefaultParamValue;
						}
						break;
					}
				}
				// 指定されていなかった場合はデフォルト値を設定します。
				if( lRes == Long.MAX_VALUE )
				{
					lRes = lDefaultParamValue;
				}
				break;
			}
		}
		brReadFile.close();
		return lRes;
	}

	/**
	 * <PRE>
	 *   *.iniファイルのパラメーター値(整数型)を読み込みます。
	 * </PRE>
	 * @param strSectionName			属するセクションの名前
	 * @param strParamName				パラメーターの名前
	 * @param bDefaultParamValue		パラメーターのデフォルト値
	 * @param strIniFilePath			ファイルパス名
	 * @return 							読み込んだパラメータ値
	 * @throws IOException				ファイル読み込みエラー
	 * @author kobayashi
	 * @since   0.1
	 * @version 0.1 2017/03/01
	 */
	protected boolean GetInitDataBoolean(String strSectionName, String strParamName, boolean bDefaultParamValue, String strIniFilePath ) throws IOException
	{
		int iCount;
		String strReadData;
		String strAbsolutePath;
		String[] strParamData;
		File file;
		BufferedReader brReadFile = null;
		boolean bRes = false;

		file = new File(strIniFilePath);
		strAbsolutePath = file.getAbsolutePath();

		brReadFile = new BufferedReader( new FileReader( strAbsolutePath ) );

		//セクション名の読み込み
		while( (strReadData = brReadFile.readLine()) != null )
		{
			// コメント文なので読み飛ばします。
			if( strReadData.indexOf(";") != -1 ) continue;
			// 指定されたセクション名があったらそこの次の行から読み始めます。
			if( strReadData.indexOf(strSectionName) != -1 ) break;
		}
		iCount = 0;
		// パラメータの読み込みを開始します。
		while( (strReadData = brReadFile.readLine()) != null )
		{
			// コメント文なので読み飛ばします。
			if( strReadData.indexOf(";") != -1 ) continue;
			// 指定されたパラメータ名があったらパラメータを読み込みます。
			if( strReadData.indexOf(strParamName) != -1 )
			{
				// "="," ","""で分割します。
				strParamData = strReadData.split("[=\\s\"]");
				for( String str : strParamData )
				{
					if( str.equals("") == false && str.equals(" ") == false && str.indexOf(strParamName) == -1 )
					{
						// パラメータを取得します。
						try
						{
							// あった場合はその値を設定します。
							bRes = Boolean.parseBoolean(strParamData[1]);
						}
						catch( NumberFormatException nfe )
						{
							bRes = bDefaultParamValue;
						}
						break;
					}
					iCount++;
				}
				// 指定されていなかった場合はデフォルト値を設定します。
				if( iCount == strParamData.length )
				{
					bRes = bDefaultParamValue;
				}
				break;
			}
		}
		brReadFile.close();
		return bRes;
	}
}
