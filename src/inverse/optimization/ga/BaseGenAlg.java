package inverse.optimization.ga;

import java.util.Arrays;

public class BaseGenAlg {

//////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////関数実装

	public static final int GENALG_SUCCESS					= 0;
	public static final int GENALG_FATAL_ERROR				= -1;
	public static final int GENALG_MEMORYALLOCATE_ERROR		= -2;
	public static final int GENALG_INVALID_ARGUMENT_ERROR	= -3;
	public static final int GENALG_INVALID_DATA_ERROR		= -4;
	public static final int GENALG_ARRAY_INDEX_ERROR		= -5;

	private double[] plfFit;
	private double[] plfCdf;
	private int[] paiRank;
	private long[][] plGens;
	private long[][] plBufGens;
	private int ulGenNumbers;
	private int ulGenBitNumbers;

	private Sfmt rnd;

	/**
	 * コンストラクタ
	 * @author kobayashi
	 * @since 2009/8/23
	 * @version 1.0
	 */
	public BaseGenAlg()
	{
		ulGenNumbers	= 0;
		ulGenBitNumbers	= 0;
		plGens			= null;
		plBufGens		= null;
		plfCdf			= null;
	}

	/**
	 * <PRE>
	 *   GAで使用する染色体の初期化を行います。遺伝子はすべて0で初期化されます。
	 * </PRE>
	 * @param ulGensNums	染色体数
	 * @param ulGenBits		ビット数
	 * @return 0 成功
	 * @throws GenAlgException 遺伝的アルゴリズム例外クラス
	 * @author kobayashi
	 * @since 2015/04/15
	 * @version 0.1
	 */
	public long lGensInit( long ulGensNums, long ulGenBits ) throws GenAlgException
	{
		int i,j;

		long seed;
		seed = System.currentTimeMillis();
		rnd = new Sfmt( (int)seed );
		GenAlgException cGae = new GenAlgException();

		if( ulGensNums <= 0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_ARGUMENT_ERROR, "lGensInit", "CBaseGenAlg", "染色体数不正データ", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( ulGenBits <= 0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_ARGUMENT_ERROR, "lGensInit", "CBaseGenAlg", "染色体ビット番号不正データ", ste[0].getLineNumber() );
			throw( cGae );
		}

		try
		{
			ulGenNumbers	= (int)ulGensNums;
			ulGenBitNumbers = (int)ulGenBits;
			plGens = new long[ulGenNumbers][ulGenBitNumbers];
			plBufGens = new long[ulGenNumbers][ulGenBitNumbers];
			for( i=0; i<ulGensNums; i++ )
			{
				//
				for( j=0; j<ulGenBits; j++ )
				{
					plBufGens[i][j] = 0;
				}
			}
		}
		catch( NullPointerException npe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensInit", "CBaseGenAlg", "null pointアクセス", ste[0].getLineNumber()  );
			throw( cGae );
		}
		catch( OutOfMemoryError ome )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensInit", "CBaseGenAlg", "メモリ確保失敗", ste[0].getLineNumber()  );
			throw( cGae );
		}
		catch( RuntimeException re )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_FATAL_ERROR, "lGensInit", "CBaseGenAlg", "不明、および致命的エラー", ste[0].getLineNumber()  );
			throw( cGae );
		}
		return 0;
	}

	/**
	 * <PRE>
	 *   GAで使用する染色体のビット割り当てを行います。
	 *   ランダムに割り当てを行います。
	 * </PRE>
	 * @return 0 成功
	 * @throws GenAlgException 遺伝的アルゴリズムの例外クラス
	 * @author kobayashi
	 * @since 2015/04/15
	 * @version 0.1
	 */
	public long lGensInitRandom() throws GenAlgException
	{
		int i,j;
		double lfRand = rnd.NextUnif();
		GenAlgException cGae= new GenAlgException();

		if( ulGenNumbers <= 0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_DATA_ERROR, "lGensFit", "CBaseGenAlg", "染色体数不正データ", ste[0].getLineNumber()  );
			throw( cGae );
		}
		if( ulGenBitNumbers <= 0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_DATA_ERROR, "lGensFit", "CBaseGenAlg", "染色体ビット番号不正データ", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( plGens == null )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensFit", "CBaseGenAlg", "染色体格納配列メモリ不正", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( plBufGens == null )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensFit", "CBaseGenAlg", "染色体一時格納配列メモリ不正", ste[0].getLineNumber() );
			throw( cGae );
		}

		try
		{
			// 乱数により初期化(遺伝子の初期化)
			for( i=0; i<ulGenNumbers; i++ )
			{
				for( j=0; j<ulGenBitNumbers; j++ )
				{
					lfRand = lfNormalRand();
					plGens[i][j] = (int)(lfRand);
//					plGens[i][j] = (lfRand <= 0.5) ? 1 : 0;
				}
			}
		}
		catch( NullPointerException npe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensFit", "BaseGenAlg", "null pointアクセス", ste[0].getLineNumber() );
			throw( cGae );
		}
		catch( ArrayIndexOutOfBoundsException aiobe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_ARRAY_INDEX_ERROR, "lGensFit", "BaseGenAlg", "配列サイズを越えてアクセス", ste[0].getLineNumber() );
			throw( cGae );
		}
		catch( RuntimeException re )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_FATAL_ERROR, "lGensFit", "CaseGenAlg", "不明、および致命的エラー", ste[0].getLineNumber() );
			throw( cGae );
		}
		return 0;
	}

	/**
	 * <PRE>
	 *   ルーレット選択を実行します。
	 *   lSetGensFitFunctionで適応度を設定してからこの関数を実行します。
	 * </PRE>
	 * @return 0 成功
	 * @throws GenAlgException 遺伝的アルゴリズムの例外を扱うクラス
	 * @author kobayashi
	 * @since 2015/04/15
	 * @version 0.1
	 */
	public long lGensSelectRolette() throws GenAlgException
	{
		int i,j;
		double lfSum			= 0.0;
		double lfInvSum			= 0.0;
		double lfProb_i			= 0.0;
		double lfProb_i1		= 0.0;
		double lfProb			= 0.0;
		double lfRand			= rnd.NextUnif()*9;
		long plSelectRoletteGens[] = null;
		GenAlgException cGae = new GenAlgException();

		if( ulGenNumbers <= 0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_DATA_ERROR, "lGensSelectRolette", "BaseGenAlg", "染色体数不正データ", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( ulGenBitNumbers <= 0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_DATA_ERROR, "lGensSelectRolette", "BaseGenAlg", "染色体ビット番号不正データ", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( plGens == null )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensSelectRolette", "BaseGenAlg", "染色体格納配列メモリ不正", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( plBufGens == null )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensSelectRolette", "BaseGenAlg", "染色体一時格納配列メモリ不正", ste[0].getLineNumber() );
			throw( cGae );
		}
		try
		{
			plSelectRoletteGens = new long[ulGenNumbers];

			for( i=0; i<ulGenNumbers; i++ )
			{
				lfSum += plfFit[i];
			}
			lfInvSum = 1.0/lfSum;
			for( i = 0;i < ulGenNumbers; i++ )
			{
				lfProb = rnd.NextUnif();
				// 初期位置のルーレット選択確率の位置を計算。
				lfProb_i = 0.0;
				for(j=0; j<ulGenNumbers; j++ )
				{
					// 2つめの選択確率の位置を算出。
					lfProb_i1 = lfProb_i + plfFit[j]*lfInvSum;
					if( lfProb_i <= lfProb && lfProb <= lfProb_i1 )
					{
						// 領域内に入ったら該当番号を残す因子として選択する。
						plSelectRoletteGens[i] = j;
					}
					lfProb_i = lfProb_i1;
				}
			}
			// 選択した染色体をいったんバッファへ代入。
			for( i=0; i<ulGenNumbers; i++ )
			{
				for( j=0; j<ulGenBitNumbers; j++ )
				{
					plBufGens[i][j] = plGens[(int)plSelectRoletteGens[i]][j];
				}
			}
			// 染色体の更新
			for( i=0; i<ulGenNumbers; i++ )
			{
				for( j=0; j<ulGenBitNumbers; j++ )
				{
					plGens[i][j] = plBufGens[i][j];
				}
			}
		}
		catch( NullPointerException npe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensSelectRolette", "BaseGenAlg", "null pointアクセス", ste[0].getLineNumber() );
			throw( cGae );
		}
		catch( OutOfMemoryError ome )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensSelectRolette", "BaseGenAlg", "交叉する染色体番号格の配列メモリ作成失敗", ste[0].getLineNumber() );
			throw( cGae );
		}
		catch( ArrayIndexOutOfBoundsException aiobe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_ARRAY_INDEX_ERROR, "lGensSelectRolette", "BaseGenAlg", "配列サイズを越えてアクセス", ste[0].getLineNumber() );
			throw( cGae );
		}
		catch( RuntimeException re )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_FATAL_ERROR, "lGensSelectRolette", "BaseGenAlg", "不明、および致命的エラー", ste[0].getLineNumber() );
			throw( cGae );
		}
		return 0;
	}

	/**
	 * <PRE>
	 *   トーナメント方式により染色体を選定します。
	 *   2のみアルゴリズムを変更しています。
	 *   3以降は手前から順番にトーナメントを実行します。
	 *   2のみ最大値と最小値、2番目に大きい値と2番目に小さい値というようにトーナメントを実行していきます。
	 *   lSetGensTournamentProbで適応度を設定してからこの関数を実行します。
	 * </PRE>
	 * @param iSelectNum トーナメントを行う数
	 * @return 0 成功
	 * @throws GenAlgException 遺伝的アルゴリズムの例外を扱うクラス
	 * @author kobayashi
	 * @since 2015/04/15
	 * @version 0.1
	 */
	public long lGensSelectTournament(int iSelectNum ) throws GenAlgException
	{
		int i,j,k;
		int plBufCdfSelect[] = null;
		double lfTemp = 0.0;
		GenAlgException cGae = new GenAlgException();

		if( iSelectNum < 2 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_DATA_ERROR, "lGensSelectTournament", "CBaseGenAlg", "トーナメント対象遺伝子数不正", ste[0].getLineNumber()  );
			throw( cGae );
		}
		if( ulGenNumbers <= 0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_DATA_ERROR, "lGensSelectTournament", "CBaseGenAlg", "染色体数不正データ", ste[0].getLineNumber()  );
			throw( cGae );
		}
		if( ulGenBitNumbers <= 0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_DATA_ERROR, "lGensSelectTournament", "CBaseGenAlg", "染色体ビット番号不正データ", ste[0].getLineNumber()  );
			throw( cGae );
		}
		if( plGens == null )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensSelectTournament", "CBaseGenAlg", "染色体格納配列メモリ不正", ste[0].getLineNumber()  );
			throw( cGae );
		}
		if( plBufGens == null )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensSelectTournament", "CBaseGenAlg", "染色体一時格納配列メモリ不正", ste[0].getLineNumber()  );
			throw( cGae );
		}

		try
		{
			if( iSelectNum > 2 )
			{
				// トーナメント方式で選択する番号を取得する。
				plBufCdfSelect = new int[(int)(ulGenNumbers/iSelectNum)+1];
				for( i = 0;i < (int)(ulGenNumbers/iSelectNum); i++ )
				{
					lfTemp = -10000000;
					for( j = i*iSelectNum; j < (i+1)*iSelectNum; j++ )
					{
						if( lfTemp < plfCdf[j])
						{
							lfTemp = plfCdf[j];
							// 該当番号を取得する。
							plBufCdfSelect[i] = j;
						}
					}
				}
				// 遺伝子数が奇数ならば、最終の遺伝子を比較する。
				if( (ulGenNumbers-i*iSelectNum) > 0 )
				{
					lfTemp = -10000000;
					for( j = i*iSelectNum; j < ulGenNumbers; j++ )
					{
						if( lfTemp < plfCdf[j])
						{
							lfTemp = plfCdf[j];
							// 該当番号を取得する。
							plBufCdfSelect[i] = j;
						}
					}
				}
				/* メイン */
				for( i = 0;i < (int)(ulGenNumbers/iSelectNum); i++ )
				{
					// 選択した染色体をいったんバッファへ代入。
					for( j = i*iSelectNum; j < (i+1)*iSelectNum; j++ )
					{
						for( k=0 ; k<ulGenBitNumbers ; k++ )
						{
							plBufGens[j][k] = plGens[plBufCdfSelect[i]][k];
						}
					}
				}
				// 遺伝子数が奇数ならば、最終の遺伝子を比較する。
				if( (ulGenNumbers-i*iSelectNum) > 0 )
				{
					for( j = i*iSelectNum; j < ulGenNumbers; j++ )
					{
						// 該当番号を取得する。
						for( k=0 ; k<ulGenBitNumbers ; k++ )
						{
							plBufGens[j][k] = plGens[plBufCdfSelect[i]][k];
						}
					}
				}
				plBufCdfSelect = null;
			}
			else
			{
				double plfTempData[] = new double[plfCdf.length];

				int piTempRank[] = new int[plfCdf.length];

				// 降順にソートする。
				for( i = 0;i < plfCdf.length; i++ )
				{
					plfTempData[i] = plfCdf[i];
				}
				Arrays.sort( plfTempData );
				for(i = 0;i < plfTempData.length/2; i++ )
				{
					lfTemp = plfTempData[i];
					plfTempData[i] = plfTempData[plfTempData.length-1-i];
					plfTempData[plfTempData.length-1-i] = lfTemp;
				}

				// タグ付けの実行
				for( i = 0; i < plfTempData.length; i++ )
				{
					for( j = 0;j < plfCdf.length; j++ )
					{
						if( Math.abs(plfTempData[i] - plfCdf[j]) <= 0.000000001 )
						{
							piTempRank[i] = j;
							break;
						}
					}
				}

				// トーナメント方式を実行する。(最大値と最小値という順番にトーナメントしていく)
				for( i = 0;i < ulGenNumbers/2; i++ )
				{
					for( j = 0;j < ulGenBitNumbers; j++ )
					{
						plBufGens[i][j] = plGens[piTempRank[i]][j];
						plBufGens[ulGenNumbers-1-i][j] = plGens[piTempRank[i]][j];
					}
				}
				if( ulGenNumbers % 2 == 1 )
				{
					for( j = 0;j < ulGenBitNumbers; j++ )
					{
						plBufGens[i][j] = plGens[piTempRank[i]][j];
					}
				}
				plfTempData = null;
				piTempRank = null;
			}
			// 染色体の更新
			for( i=0; i<ulGenNumbers; i++ )
			{
				for( j=0; j<ulGenBitNumbers; j++ )
				{
					plGens[i][j] = plBufGens[i][j];
				}
			}
		}
		catch( NullPointerException npe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensSelectTournament", "BaseGenAlg", "null pointアクセス", ste[0].getLineNumber()  );
			throw( cGae );
		}
		catch( OutOfMemoryError ome )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensSelectTournament", "BaseGenAlg", "交叉する染色体番号格の配列メモリ作成失敗", ste[0].getLineNumber()  );
			throw( cGae );
		}
		catch( ArrayIndexOutOfBoundsException aiobe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_ARRAY_INDEX_ERROR, "lGensSelectTournament", "BaseGenAlg", "配列サイズを越えてアクセス", ste[0].getLineNumber()  );
			throw( cGae );
		}
		catch( RuntimeException re )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_FATAL_ERROR, "lGensSelectTournament", "BaseGenAlg", "不明、および致命的エラー", ste[0].getLineNumber()  );
			throw( cGae );
		}
		return 0;
	}

	/**
	 * <PRE>
	 *   ランキング方式により染色体を選定します。
	 *   lSetGensRankingDecisionを呼び出して適応度を先に設定する必要があります。
	 * </PRE>
	 * @return 0 成功
	 * @throws GenAlgException 遺伝的アルゴリズムの例外を扱うクラス
	 * @author kobayashi
	 * @since 2015/04/15
	 * @version 0.1
	 */
	public long lGensSelectRanking() throws GenAlgException
	{
		int i,j;
		GenAlgException cGae = new GenAlgException();

		if( ulGenNumbers <= 0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_DATA_ERROR, "lGensSelectRanking", "CBaseGenAlg", "染色体数不正データ", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( ulGenBitNumbers <= 0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_DATA_ERROR, "lGensSelectRanking", "CBaseGenAlg", "染色体ビット番号不正データ", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( plGens == null )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensSelectRanking", "CBaseGenAlg", "染色体格納配列メモリ不正", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( plBufGens == null )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensSelectRanking", "CBaseGenAlg", "染色体一時格納配列メモリ不正", ste[0].getLineNumber() );
			throw( cGae );
		}

		try
		{
			// ランキングを求める。
			// 選択した染色体をいったんバッファへ代入。
			for( i=0; i<ulGenNumbers; i++ )
			{
				for( j=0; j<ulGenBitNumbers; j++ )
				{
					plBufGens[i][j] = plGens[paiRank[i]][j];
				}
			}
			// 染色体の更新
			for( i=0; i<ulGenNumbers; i++ )
			{
				for( j=0; j<ulGenBitNumbers; j++ )
				{
					plGens[i][j] = plBufGens[i][j];
				}
			}
		}
		catch( NullPointerException npe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensSelectRanking", "BaseGenAlg", "null pointアクセス", ste[0].getLineNumber() );
			throw( cGae );
		}
		catch( OutOfMemoryError ome )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensSelectRanking", "BaseGenAlg", "交叉する染色体番号格の配列メモリ作成失敗", ste[0].getLineNumber() );
			throw( cGae );
		}
		catch( ArrayIndexOutOfBoundsException aiobe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_ARRAY_INDEX_ERROR, "lGensSelectRanking", "BaseGenAlg", "配列サイズを越えてアクセス", ste[0].getLineNumber() );
			throw( cGae );
		}
		catch( RuntimeException re )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_FATAL_ERROR, "lGensSelectRanking", "BaseGenAlg", "不明、および致命的エラー", ste[0].getLineNumber() );
			throw( cGae );
		}
		return 0;
	}

	/**
	 * <PRE>
	 *   染色体の交叉を実行します。
	 *   Pcの確率で交叉が発生します。
	 *
	 * </PRE>
	 * @param lfCrossProb 交叉確率
	 * @param iMethodFlag 0 一点交叉
	 *                    1 二点交叉
	 *                    2 多点交叉(2点以上)
	 *                    3 一様交叉
	 * @return 0 成功
	 * @throws GenAlgException	遺伝的アルゴリズム計算エラー
	 * @author kobayashi
	 * @since 2015/04/15
	 * @version 0.1
	 */
	public long lGensCrossOver( double lfCrossProb, int iMethodFlag ) throws GenAlgException
	{
		int i,j,k;
		int i1,i2;
		int iCrossOverNum		= 0;
		int iCrossLoc			= 0;
		int iPrevCrossLoc		= 0;
		int iCrossOverCount		= 0;
		int iCrossCount			= 0;
		long lTemp				= 0;
		double lfProb			= 0.0;
		long plSelectCrossGens[] = null;
		long plSelectCrossOverLoc[] = null;
		GenAlgException cGae = new GenAlgException();

		if( lfCrossProb < 0.0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_ARGUMENT_ERROR, "lGensCrossOver", "CBaseGenAlg", "交叉確率不正データ", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( ulGenNumbers <= 0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_DATA_ERROR, "lGensCrossOver", "CBaseGenAlg", "染色体数不正データ", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( ulGenBitNumbers <= 0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_DATA_ERROR, "lGensCrossOver", "CBaseGenAlg", "染色体ビット番号不正データ", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( plGens == null )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensCrossOver", "CBaseGenAlg", "染色体格納配列メモリ不正", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( plBufGens == null )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensCrossOver", "CBaseGenAlg", "染色体一時格納配列メモリ不正", ste[0].getLineNumber() );
			throw( cGae );
		}

		try
		{
			plSelectCrossOverLoc = new long[ulGenBitNumbers];
			plSelectCrossGens = new long[ulGenNumbers];
			// 集団のうち確率Pcの割合で、交叉する染色体を選択する。
			for( i = 0;i < ulGenNumbers; i++ )
			{
				plSelectCrossGens[i] = -1;
			}
			for( i=0,j=0; i<ulGenNumbers; i++ )
			{
				lfProb = rnd.NextUnif();
				if( lfProb <= lfCrossProb )
				{
					plSelectCrossGens[j++] = i;
					if( j >= ulGenNumbers ) j = ulGenNumbers-1;
				}
			}
			// 奇数の場合、ランダムに選択する。
			if( (j & 1)==1 )
			{
				lfProb = rnd.NextUnif();
				i = (int)(lfProb*(double)j);
				plSelectCrossGens[j++] = i;
			}
			// 1点交叉を実行する。
			if( iMethodFlag == 0 )
			{
				// 交叉位置を乱数により決定する。
				lfProb = rnd.NextUnif();
				iCrossLoc = (int)(lfProb*(double)ulGenBitNumbers);

				iCrossCount = 0;
				for( i=0; i<ulGenNumbers/2; i++ )
				{
					i1 = 2*i;
					i2 = 2*i+1;
					if( plSelectCrossGens[i1] != -1 &&  plSelectCrossGens[i2] != -1 )
					{
						// 交叉を位置まで実行しない。
						for( j=0; j<iCrossLoc; j++ )
						{
							plBufGens[i1][j] = plGens[(int)plSelectCrossGens[i1]][j];
							plBufGens[i2][j] = plGens[(int)plSelectCrossGens[i2]][j];
						}
						// 交叉を実行する。
						for( j=iCrossLoc; j<ulGenBitNumbers; j++ )
						{
							plBufGens[i1][j] = plGens[(int)plSelectCrossGens[i2]][j];
							plBufGens[i2][j] = plGens[(int)plSelectCrossGens[i1]][j];
						}
						iCrossCount+=2;
					}
				}
				// 交叉後のデータに親データを保存する。
				for( i = 0;i < ulGenNumbers; i++ )
				{
					for( j = 0;j < ulGenBitNumbers; j++ )
					{
						if( plSelectCrossGens[i] != -1 )
						{
							plBufGens[i+iCrossCount][j] = plGens[i][j];
						}
					}
				}
			}
			// 二点交叉を実行する。
			else if( iMethodFlag == 1 )
			{
				// 交叉位置を乱数により決定する。
				iCrossOverCount = 0;
				iCrossOverNum = 2;
				while( true )
				{
					lfProb = rnd.NextUnif();
					iCrossLoc = (int)(lfProb*(double)ulGenBitNumbers);
					if( iPrevCrossLoc != iCrossLoc )
					{
						plSelectCrossOverLoc[iCrossOverCount] = iCrossLoc;
						iCrossOverCount++;
					}
					if( iCrossOverCount == iCrossOverNum ) break;
					iPrevCrossLoc = iCrossLoc;
				}
				if( plSelectCrossOverLoc[0] > plSelectCrossOverLoc[1] )
				{
					lTemp = plSelectCrossOverLoc[0];
					plSelectCrossOverLoc[0] = plSelectCrossOverLoc[1];
					plSelectCrossOverLoc[1] = lTemp;
				}
				iCrossCount = 0;
				for( i=0; i<ulGenNumbers/2; i++ )
				{
					i1 = 2*i;
					i2 = 2*i+1;
					if( plSelectCrossGens[i1] != -1 &&  plSelectCrossGens[i2] != -1 )
					{
						for( j = 0;j < ulGenBitNumbers; j++ )
						{
							// 交叉を実行しない。
							if( j < plSelectCrossOverLoc[0] || j >= plSelectCrossOverLoc[1] )
							{
								plBufGens[i1][j] = plGens[(int)plSelectCrossGens[i1]][j];
								plBufGens[i2][j] = plGens[(int)plSelectCrossGens[i2]][j];
							}
							// 交叉を実行する。
							else if( plSelectCrossOverLoc[0] <= j && j < plSelectCrossOverLoc[1] )
							{
								plBufGens[i1][j] = plGens[(int)plSelectCrossGens[i2]][j];
								plBufGens[i2][j] = plGens[(int)plSelectCrossGens[i1]][j];
							}
						}
						iCrossCount+=2;
					}
				}
				// 交叉後のデータに親データを保存する。
				for( i = 0;i < ulGenNumbers; i++ )
				{
					for( j = 0;j < ulGenBitNumbers; j++ )
					{
						if( plSelectCrossGens[i] != -1 )
						{
							plBufGens[i+iCrossCount][j] = plGens[i][j];
						}
					}
				}
			}
			// n点交叉を実行する。
			else if( iMethodFlag == 2 )
			{
				iCrossOverCount = 0;
				// 交叉数をランダムに決定する。
				iCrossOverNum = (int)(rnd.NextUnif()*ulGenBitNumbers);
				// 交叉位置を乱数により決定する。
				while( true )
				{
					lfProb = rnd.NextUnif();

					iCrossLoc = (int)(lfProb*(double)ulGenBitNumbers);
					if( iPrevCrossLoc != iCrossLoc )
					{
						plSelectCrossOverLoc[iCrossOverCount] = iCrossLoc;
						iCrossOverCount++;
					}
					if( iCrossOverCount == iCrossOverNum ) break;
					iPrevCrossLoc = iCrossLoc;
				}
				iCrossCount = 0;
				for( i=0; i<ulGenNumbers/2; i++ )
				{
					i1 = 2*i;
					i2 = 2*i+1;
//					if( plSelectCrossGens[i1] != -1 &&  plSelectCrossGens[i2] != -1 )
//					{
//						for( j = 0;j < ulGenBitNumbers; j++ )
//						{
//							for( k = 0;k < iCrossOverCount; k++ )
//							{
							// 交叉を位置まで実行しない。
//								if( j < plSelectCrossOverLoc[k] )
//								{
//									plBufGens[i1][j] = plGens[(int)plSelectCrossGens[i1]][j];
//									plBufGens[i2][j] = plGens[(int)plSelectCrossGens[i2]][j];
//								}
//								// 交叉を実行する。
//								else
//								{
//									plBufGens[i1][j] = plGens[(int)plSelectCrossGens[i2]][j];
//									plBufGens[i2][j] = plGens[(int)plSelectCrossGens[i1]][j];
//								}
//							}
//						}
//						iCrossCount+=2;
//					}
				}
			}
			// 一様交叉を実行する。
			else if( iMethodFlag == 3 )
			{
				// 交叉位置を乱数により決定する。
				for( i = 0;i < ulGenBitNumbers; i++ )
				{
					lfProb = rnd.NextUnif();
					plSelectCrossOverLoc[i] = lfProb <= 0.5 ? 1 : 0;
				}
				iCrossCount = 0;
				for( i=0; i<ulGenNumbers/2; i++ )
				{
					i1 = 2*i;
					i2 = 2*i+1;
					if( plSelectCrossGens[i1] != -1 &&  plSelectCrossGens[i2] != -1 )
					{
						for( j = 0;j < ulGenBitNumbers; j++ )
						{
							// 交叉を位置まで実行しない。
							if( plSelectCrossOverLoc[j] == 0 )
							{
								plBufGens[i1][j] = plGens[(int)plSelectCrossGens[i1]][j];
								plBufGens[i2][j] = plGens[(int)plSelectCrossGens[i2]][j];
							}
							// 交叉を実行する。
							else
							{
								plBufGens[i1][j] = plGens[(int)plSelectCrossGens[i2]][j];
								plBufGens[i2][j] = plGens[(int)plSelectCrossGens[i1]][j];
							}
						}
					}
					iCrossCount+=2;
				}
				// 交叉後のデータに交叉元の親データを保存する。
				for( i = 0;i < ulGenNumbers-iCrossCount; i++ )
				{
					for( j = 0;j < ulGenBitNumbers; j++ )
					{
						if( plSelectCrossGens[i] != -1 )
						{
							plBufGens[i+iCrossCount][j] = plGens[(int)plSelectCrossGens[i]][j];
						}
					}
				}
			}
			// 遺伝子データの更新をする。
			for( i=0; i<ulGenNumbers; i++ )
			{
				for( j=0; j<ulGenBitNumbers; j++ )
				{
					plGens[i][j] = plBufGens[i][j];
				}
			}
			plSelectCrossGens = null;
			plSelectCrossOverLoc = null;
		}
		catch( NullPointerException npe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensCrossOver", "BaseGenAlg", "null pointアクセス", ste[0].getLineNumber() );
			throw( cGae );
		}
		catch( OutOfMemoryError ome )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensCrossOver", "BaseGenAlg", "交叉する染色体番号格の配列メモリ作成失敗", ste[0].getLineNumber() );
			throw( cGae );
		}
		catch( ArrayIndexOutOfBoundsException aiobe )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_ARRAY_INDEX_ERROR, "lGensCrossOver", "BaseGenAlg", "配列サイズを越えてアクセス", ste[0].getLineNumber() );
			throw( cGae );
		}
		catch( RuntimeException re )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_FATAL_ERROR, "lGensCrossOver", "BaseGenAlg", "不明、および致命的エラー", ste[0].getLineNumber() );
			throw( cGae );
		}
		return 0;
	}

	/**
	 * <PRE>
	 *   染色体に突然変異をおこさせる。
	 *   N*B*P個で染色体が突然変異をおこす。
	 *   疑似乱数を発生させて P &lt;= N*B*Pならば
	 *   突然変異がおこったとみなしてその部分のビットを変更する。
	 * </PRE>
	 * @param lfMutProb		突然変異確率
	 * @return	0			成功
	 * 			それ以外	失敗
	 * @throws GenAlgException 遺伝的アルゴリズム処理例外
	 * @author kobayashi
	 * @since 2015/04/15
	 * @version 0.1
	 */
	public long lGensMutation( double lfMutProb ) throws GenAlgException
	{
		int i,j;
		double lfProb	= 0.0;
		GenAlgException cGae = new GenAlgException();

		if( lfMutProb < 0.0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_ARGUMENT_ERROR, "lGensMutation", "CBaseGenAlg", "突然変異確率不正データ", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( ulGenNumbers <= 0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_DATA_ERROR, "lGensMutation", "CBaseGenAlg", "染色体数不正データ", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( ulGenBitNumbers <= 0 )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_INVALID_DATA_ERROR, "lGensMutation", "CBaseGenAlg", "染色体ビット番号不正データ", ste[0].getLineNumber() );
			throw( cGae );
		}
		if( plGens == null )
		{
			StackTraceElement ste[] = (new Throwable()).getStackTrace();
			cGae.SetErrorInfo( GENALG_MEMORYALLOCATE_ERROR, "lGensMutation", "CBaseGenAlg", "染色体格納配列メモリ不正", ste[0].getLineNumber() );
			throw( cGae );
		}

		for( i=0; i<ulGenNumbers ; i++ )
		{
			for( j=0; j<ulGenBitNumbers ; j++ )
			{
				lfProb = rnd.NextUnif();
				if( lfProb <= lfMutProb )
				{
//					if( plGens[i][j] == 0 )		plGens[i][j] = 1;
//					else if( plGens[i][j] == 1 )plGens[i][j] = 0;
					plGens[i][j] = (int)(rnd.NextUnif()*9);
				}
			}
		}
		return 0;
	}

	/**
	 * <PRE>
	 *   作成した染色体を削除する。
	 * </PRE>
	 * @return	0		 成功
	 * 			それ以外 失敗
	 * @author kobayashi
	 * @since 2015/04/15
	 * @version 0.1
	 */
	public long lGensDelete()
	{
		plGens = null;
		plBufGens = null;
		return 0;
	}

	/**
	 * <PRE>
	 *   ルーレット選択用の適応度を設定します。
	 *   lGensSelectRoletteを実行する前にこの関数を呼び出して適応度を指定する必要があります。
	 * </PRE>
	 * @param plfData 適応度を指定した配列
	 * @return 0 成功
	 * @author kobayashi
	 * @since 2015/05/14
	 * @version 0.1
	 */
	public long lSetGensFitFunction( double[] plfData )
	{
		int i;
		plfFit = new double[plfData.length];
		for( i = 0; i < plfData.length; i++ )
		{
			plfFit[i] = plfData[i];
		}
		return 0;
	}

	/**
	 * <PRE>
	 *   トーナメント方式用の適応度を設定します。
	 *   lGensSelectTournamentを実行する前にこの関数を呼び出して適応度を設定する必要があります。
	 * </PRE>
	 * @param plfData 適応度を指定した配列
	 * @return	0 		 成功
	 * 			それ以外 失敗
	 * @author kobayashi
	 * @since 2015/05/14
	 * @version 0.1
	 */
	public long lSetGensTournamentProb( double[] plfData )
	{
		int i;
		plfCdf = new double[plfData.length];
		for( i = 0; i < plfData.length; i++ )
		{
			plfCdf[i] = plfData[i];
		}
		return 0;
	}

	/**
	 * <PRE>
	 *   ランキング方式のランキングを設定します。
	 *   lGensRankingを実行する前にこの関数を呼び出して適応度を設定する必要があります。
	 *
	 * </PRE>
	 * @param plfData ランキングを指定した配列
	 * @return	0 成功
	 * 			それ以外 失敗
	 * @author kobayashi
	 * @since 2015/05/14
	 * @version 0.1
	 */
	public long lSetGensRankingDecision( double[] plfData )
	{
		int i,j;
		double plfTempData[] = new double[plfData.length];
		double lfTemp = 0.0;

		paiRank = new int[plfData.length];

		for( i = 0;i < plfData.length; i++ )
		{
			plfTempData[i] = plfData[i];
		}
		Arrays.sort( plfTempData );
		for(i = 0;i < plfTempData.length/2; i++ )
		{
			lfTemp = plfTempData[i];
			plfTempData[i] = plfTempData[plfTempData.length-1-i];
			plfTempData[plfTempData.length-1-i] = lfTemp;
		}

		for( i = 0; i < plfTempData.length; i++ )
		{
			for( j = 0;j < plfData.length; j++ )
			{
				if( Math.abs(plfTempData[i] - plfData[j]) <= 0.000000001 )
				{
					paiRank[i] = j;
					break;
				}
			}
		}
		// 半分以降は淘汰する。
		for( i = paiRank.length/2;i < paiRank.length; i++ )
		{
			paiRank[i] = 0;
		}
		return 0;
	}

	/**
	 * <PRE>
	 *   指定した位置の遺伝子の値を取得します。
	 * </PRE>
	 * @param iX 遺伝子の番号
	 * @param iY 遺伝子列の指定位置
	 * @return 遺伝子のビット
	 */
	public long lGetGensData(int iX, int iY)
	{
		return plGens[iX][iY];
	}

	/**
	 * <PRE>
	 *   指定した遺伝子列を取得します。
	 * </PRE>
	 * @param iLoc 指定位置
	 * @return 遺伝子データ
	 */
	public long[] plGetGensData( int iLoc )
	{
		return plGens[iLoc];
	}

	/**
	 * <PRE>
	 *    遺伝子全体を取得します。
	 * </PRE>
	 * @return 遺伝子データ
	 */
	public long[][] pplGetGensData()
	{
		return plGens;
	}

	/**
	 * <PRE>
	 *   遺伝子にデータを代入します。
	 * </PRE>
	 * @param iGenNum   代入する遺伝子
	 * @param iGenBits  代入位置
	 * @param lData     代入するデータ
	 */
	public void vSetGenData( int iGenNum, int iGenBits, long lData )
	{
		plGens[iGenNum][iGenBits] = lData;
	}

	/**
	 * <PRE>
	 *   遺伝的アルゴリズムの遺伝子数を取得します。
	 * </PRE>
	 * @return 遺伝子数
	 * @author kobayashi
	 * @since 2015/04/15
	 * @version 0.1
	 */
	public int ulGetGenNumbers()
	{
		return ulGenNumbers;
	}

	/**
	 * <PRE>
	 *   遺伝的アルゴリズムの遺伝子のビット数を取得します。
	 * </PRE>
	 * @return 遺伝子のビット数
	 * @author kobayashi
	 * @since 2015/04/15
	 * @version 0.1
	 */
	public int ulGetGenBitNumbers()
	{
		return ulGenBitNumbers;
	}

	public double lfNormalRand( )
	{
		double lfRes;
		double lfNormal = Math.abs(rnd.NextNormal());
//		double lfNormal = rnd.NextNormal();
//		lfRes = 9*Math.exp(-lfNormal*lfNormal*0.5);
		lfRes = lfNormal*9;
		if( lfRes >= 9.0 ) lfRes = 9.0;
///		else if( lfRes <= 0.0 ) lfRes = 0.0;
		return lfRes;
	}

}
