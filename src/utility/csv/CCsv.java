package utility.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CCsv
{
	int iRow;
	int iColumn;
	String strFileName;
	String strFileMode;
	BufferedReader brCsvReadFilePointer;
	PrintWriter pwCsvWriteFilePointer;

	/**
	 * <PRE>
	 *    コンストラクタ
	 * </PRE>
	 */
	public CCsv()
	{
		iRow = 0;
		iColumn = 0;
		strFileName = "";
		strFileMode = "";
		brCsvReadFilePointer = null;
		pwCsvWriteFilePointer = null;

	}

	/**
	 * <PRE>
	 *    csvファイルを開きます。
	 * </PRE>
	 * @param strFileNameData	ファイル名
	 * @param strFileModeData	ファイルオープンモード(read：読み込み,write：書き込み)
	 * @throws IOException		ファイル操作エラー例外
	 */
	public void vOpen( String strFileNameData, String strFileModeData ) throws IOException
	{
		if( strFileModeData.equals( "read" ) == true )
		{
			brCsvReadFilePointer = new BufferedReader( new FileReader( strFileNameData ) );
			strFileName = strFileNameData;
			strFileMode = strFileModeData;
		}
		else if( strFileModeData.equals( "write" ) == true )
		{
			pwCsvWriteFilePointer = new PrintWriter( new BufferedWriter( new FileWriter( strFileNameData ) ) );
			strFileName = strFileNameData;
			strFileMode = strFileModeData;
		}
		else
		{

		}
	}

	/**
	 * <PRE>
	 *    ファイルクローズ処理を実行します。
	 * </PRE>
	 * @throws IOException	ファイルクローズエラー
	 */
	public void vClose() throws IOException
	{
		if( strFileMode.equals( "read" ) == true )
		{
			brCsvReadFilePointer.close();
		}
		else if( strFileMode.equals( "write" ) == true )
		{
			pwCsvWriteFilePointer.close();
		}
		else
		{

		}
	}

	/**
	 * <PRE>
	 *   csvファイルの行数、列数を取得します。
	 * </PRE>
	 * @throws IOException	ファイル読み込みエラー
	 */
	public void vGetRowColumn() throws IOException
	{
		int i,j;
		int iRowMax = -Integer.MAX_VALUE;
		int iRowTemp = 0;
		String strFileData = "";
		i = 0;
		while( ( strFileData = brCsvReadFilePointer.readLine() ) != null )
		{
			String pStrData[] = strFileData.split(",");

			iRowTemp = 0;
			for( j = 0;j < pStrData.length; j++ )
			{
				iRowTemp++;
			}
			iRowMax = iRowTemp > iRowMax ? iRowTemp : iRowMax;
			iRow = iRowMax;
			iColumn++;
		}
		vClose();
		vOpen( strFileName, strFileMode );
	}

	/**
	 * <PRE>
	 *   ファイルの読み込みを行います。
	 * </PRE>
	 * @param objData		読み込みデータ格納配列
	 * @throws IOException	ファイル読み込みエラー
	 */
	public void vRead( Object objData ) throws IOException
	{
		int i,j;
		String strFileData = "";
		if( objData instanceof String[][] )
		{
			String ppStrData[][] = (String[][])objData;
			i = 0;
			while( ( strFileData = brCsvReadFilePointer.readLine() ) != null )
			{
				String pStrData[] = strFileData.split(",");

				for( j = 0;j < pStrData.length; j++ )
				{
					ppStrData[i][j] = pStrData[j];
				}
				j++;
			}
		}
		else if( objData instanceof double[][] )
		{
			double pplfData[][] = (double[][])objData;
			i = 0;
			while( ( strFileData = brCsvReadFilePointer.readLine() ) != null )
			{
				String pStrData[] = strFileData.split(",");

				for( j = 0;j < pStrData.length; j++ )
				{
					if( isDoubleDigit( pStrData[j] ) == true )
					{
						pplfData[i][j] = Double.parseDouble(pStrData[j]);
					}
				}
				i++;
			}
		}
		else if( objData instanceof float[][] )
		{
			float pplfData[][] = (float[][])objData;
			i = 0;
			while( ( strFileData = brCsvReadFilePointer.readLine() ) != null )
			{
				String pStrData[] = strFileData.split(",");

				for( j = 0;j < pStrData.length; j++ )
				{
					if( isDoubleDigit( pStrData[j] ) == true )
					{
						pplfData[i][j] = Float.parseFloat(pStrData[j]);
					}
				}
				i++;
			}
		}
		else if( objData instanceof int[][] )
		{
			int ppiData[][] = (int[][])objData;
			i = 0;
			while( ( strFileData = brCsvReadFilePointer.readLine() ) != null )
			{
				String pStrData[] = strFileData.split(",");

				for( j = 0;j < pStrData.length; j++ )
				{
					if( isIntDigit( pStrData[j] ) == true )
					{
						ppiData[i][j] = Integer.parseInt(pStrData[j]);
					}
				}
				i++;
			}
		}
		else if( objData instanceof Double[][] )
		{
			Double pplfData[][] = (Double[][])objData;
			i = 0;
			while( ( strFileData = brCsvReadFilePointer.readLine() ) != null )
			{
				String pStrData[] = strFileData.split(",");

				for( j = 0;j < pStrData.length; j++ )
				{
					if( isDoubleDigit( pStrData[j] ) == true )
					{
						pplfData[i][j] = Double.parseDouble(pStrData[j]);
					}
				}
				i++;
			}
		}
		else if( objData instanceof Float[][] )
		{
			Float pplfData[][] = (Float[][])objData;
			i = 0;
			while( ( strFileData = brCsvReadFilePointer.readLine() ) != null )
			{
				String pStrData[] = strFileData.split(",");

				for( j = 0;j < pStrData.length; j++ )
				{
					if( isDoubleDigit( pStrData[j] ) == true )
					{
						pplfData[i][j] = Float.parseFloat(pStrData[j]);
					}
				}
				i++;
			}
		}
		else if( objData instanceof Integer[][] )
		{
			Integer ppiData[][] = (Integer[][])objData;
			i = 0;
			while( ( strFileData = brCsvReadFilePointer.readLine() ) != null )
			{
				String pStrData[] = strFileData.split(",");

				for( j = 0;j < pStrData.length; j++ )
				{
					if( isIntDigit( pStrData[j] ) == true )
					{
						ppiData[i][j] = Integer.parseInt(pStrData[j]);
					}
				}
				i++;
			}
		}
	}

	/**
	 * <PRE>
	 *   ファイルの書き込みを行います。(String型)
	 * </PRE>
	 * @param strWriteData	書き込み用データ
	 * @throws IOException	ファイル書き込みエラー
	 */
	public void vWrite( String strWriteData ) throws IOException
	{
		pwCsvWriteFilePointer.println( strWriteData );
	}

	/**
	 * <PRE>
	 *   ファイルの書き込みを行います。(String[][]型)
	 * </PRE>
	 * @param ppstrWriteData	書き込み用データ
	 * @throws IOException		ファイル書き込みエラー
	 */
	public void vWrite( String[][] ppstrWriteData ) throws IOException
	{
		int i,j;
		String strWriteData = "";

		for( i = 0;i < ppstrWriteData.length; i++ )
		{
			for( j = 0;j < ppstrWriteData[i].length; j++ )
			{
				strWriteData += ppstrWriteData[i][j];
				strWriteData += ",";
			}
			pwCsvWriteFilePointer.println( strWriteData );
		}
	}

	/**
	 * <PRE>
	 *   ファイルの書き込みを行います。(double型)
	 * </PRE>
	 * @param pplfWriteData	書き込み用データ
	 * @throws IOException	ファイル書き込みエラー
	 */
	public void vWrite( double[][] pplfWriteData ) throws IOException
	{
		int i,j;
		String strWriteData = "";

		for( i = 0;i < pplfWriteData.length; i++ )
		{
			for( j = 0;j < pplfWriteData[i].length; j++ )
			{
				strWriteData += String.valueOf( pplfWriteData[i][j] );
				strWriteData += ",";
			}
			pwCsvWriteFilePointer.println( strWriteData );
		}
	}

	/**
	 * <PRE>
	 *   ファイルの書き込みを行います。(float型)
	 * </PRE>
	 * @param pplfWriteData	書き込み用データ
	 * @throws IOException	ファイル書き込みエラー
	 */
	public void vWrite( float[][] pplfWriteData ) throws IOException
	{
		int i,j;
		String strWriteData = "";

		for( i = 0;i < pplfWriteData.length; i++ )
		{
			for( j = 0;j < pplfWriteData[i].length; j++ )
			{
				strWriteData += String.valueOf( pplfWriteData[i][j] );
				strWriteData += ",";
			}
			pwCsvWriteFilePointer.println( strWriteData );
		}
	}

	/**
	 * <PRE>
	 *   ファイルの書き込みを行います。(int[][]型)
	 * </PRE>
	 * @param ppiWriteData	書き込み用データ
	 * @throws IOException	ファイル書き込みエラー
	 */
	public void vWrite( int[][] ppiWriteData ) throws IOException
	{
		int i,j;
		String strWriteData = "";

		for( i = 0;i < ppiWriteData.length; i++ )
		{
			for( j = 0;j < ppiWriteData[i].length; j++ )
			{
				strWriteData += String.valueOf( ppiWriteData[i][j] );
				strWriteData += ",";
			}
			pwCsvWriteFilePointer.println( strWriteData );
		}
	}

	/**
	 * <PRE>
	 *   ファイルの書き込みを行います。(Double[][]型)
	 * </PRE>
	 * @param pplfWriteData	書き込み用データ
	 * @throws IOException	ファイル書き込みエラー
	 */
	public void vWrite( Double[][] pplfWriteData ) throws IOException
	{
		int i,j;
		String strWriteData = "";

		for( i = 0;i < pplfWriteData.length; i++ )
		{
			for( j = 0;j < pplfWriteData[i].length; j++ )
			{
				strWriteData += String.valueOf( pplfWriteData[i][j] );
				strWriteData += ",";
			}
			pwCsvWriteFilePointer.println( strWriteData );
		}
	}

	/**
	 * <PRE>
	 *   ファイルの書き込みを行います。(Float[][]型)
	 * </PRE>
	 * @param pplfWriteData	書き込み用データ
	 * @throws IOException	ファイル書き込みエラー
	 */
	public void vWrite( Float[][] pplfWriteData ) throws IOException
	{
		int i,j;
		String strWriteData = "";

		for( i = 0;i < pplfWriteData.length; i++ )
		{
			for( j = 0;j < pplfWriteData[i].length; j++ )
			{
				strWriteData += String.valueOf( pplfWriteData[i][j] );
				strWriteData += ",";
			}
			pwCsvWriteFilePointer.println( strWriteData );
		}
	}

	/**
	 * <PRE>
	 *   数値かどうかを判定します。(小数点用)
	 * </PRE>
	 * @param strData		判定用文字列
	 * @return				true  数値
	 * 						false 数値以外の文字列
	 */
	public boolean isDoubleDigit( String strData )
	{
		try
		{
			Double.parseDouble( strData );
		}
		catch( NumberFormatException nfex )
		{
			return false;
		}
		return true;
	}

	/**
	 * <PRE>
	 *   数値かどうかを判定します。(整数用)
	 * </PRE>
	 * @param strData		判定用文字列
	 * @return				true  数値
	 * 						false 数値以外の文字列
	 */
	public boolean isIntDigit( String strData )
	{
		try
		{
			Integer.parseInt( strData );
		}
		catch( NumberFormatException nfex )
		{
			return false;
		}
		return true;
	}

	/**
	 * <PRE>
	 *   csvファイルの行数を取得します。
	 * </PRE>
	 * @return	行数
	 */
	public int iGetRow()
	{
		return iRow;
	}

	/**
	 * <PRE>
	 *   csvファイルの列数を取得します。
	 * </PRE>
	 * @return	列数
	 */
	public int iGetColumn()
	{
		return iColumn;
	}
}
