package inverse.optimization.rcga;

import inverse.optimization.objectivefunction.ObjectiveFunctionInterface;
import inverse.optimization.rankt.Rank_t;
import utility.sfmt.Sfmt;


public class CRealCodedGa
{
	protected int iGenerationNumber;
	protected int iGenNumber;
	protected int iGenVector;
	protected double[][] pplfGens;
	protected Sfmt rnd;
	protected ObjectiveFunctionInterface pflfObjectiveFunction;

	public CRealCodedGa()
	{
		iGenerationNumber = 0;
		iGenNumber = 0;
		iGenVector = 0;
		pplfGens =  null;
	}

	public CRealCodedGa( int iGenerationNum, int iGenNum, int iGenVectorData )
	{
		vInitialize( iGenerationNum, iGenNum, iGenVectorData );
	}

	public void vInitialize( int iGenerationNum, int iGenNum, int iGenVectorData )
	{
		int i,j;

		iGenerationNumber = iGenerationNum;
		iGenNumber = iGenNum;
		iGenVector = iGenVectorData;

		pplfGens = new double[iGenNumber][iGenVector];

		for( i = 0;i < iGenNumber; i++ )
			for( j = 0;j < iGenVector; j++ )
				pplfGens[i][j] = 0.0;

		long seed;
		seed = System.currentTimeMillis();
		rnd = new Sfmt( (int)seed );
	}

	/**
	 * <PRE>
	 * 　乱数によりGAを設定します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/10/30
	 * @version 1.0
	 */
	public void vSetRandom()
	{
		int i,j;

		for( i = 0;i < iGenNumber; i++ )
			for( j = 0;j < iGenVector; j++ )
					pplfGens[i][j] = rnd.NextUnif();
	}

	/**
	 * <PRE>
	 * 　目的関数をインストールします。
	 * </PRE>
	 * @param lfDomainMin 乱数発生最小値
	 * @param lfDomainMax 乱数発生最大値
	 * @author kobayashi
	 * @since 2015/10/30
	 * @version 1.0
	 */
	public void vSetRandom( double lfDomainMin, double lfDomainMax )
	{
		int i,j;
		for( i = 0;i < iGenNumber; i++ )
			for( j = 0;j < iGenVector; j++ )
				pplfGens[i][j] = rnd.NextUnif()*(lfDomainMax-lfDomainMin) + lfDomainMin;
	}

	/**
	 * <PRE>
	 * 　目的関数をインストールします。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/10/30
	 * @version 1.0
	 */
	public void vTerminate()
	{
		pplfGens = null;
	}

	/**
	 * <PRE>
	 * 　目的関数をインストールします。
	 * 　実際にはコールバック関数をインストールします。
	 * </PRE>
	 * @param pflfFunction 目的関数の関数ポインタ
	 * @author kobayashi
	 * @since 2015/10/30
	 * @version 1.0
	 */
	public void vSetConstraintFunction( ObjectiveFunctionInterface pflfFunction )
	{
		pflfObjectiveFunction = pflfFunction;
	}

	/**
	 * <PRE>
	 * 　目的関数をアンインストールします。
	 * 　実際にはコールバック関数をアンインストールします。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/10/30
	 * @version 1.0
	 */
	public void vReleaseCallbackConstraintFunction()
	{
		pflfObjectiveFunction = null;
	}

	/**
	 * <PRE>
	 * 　実数値ＧＡの結果を出力します。(各遺伝子のベクトル)
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/12/16
	 * @version 1.0
	 */
	public void vOutputGenData()
	{
		int i,j;
		// 現時点での粒子の位置を出力します。
		for( i = 0; i < iGenNumber; i++ )
		{
			for( j = 0;j < iGenVector; j++ )
				System.out.print(pplfGens[i][j] + "," );
			System.out.print("\n");
		}
	}

	/**
	 * <PRE>
	 * 　現在の実数値GAに目的関数を適用した結果を出力します。(各遺伝子の目的関数値)
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/12/16
	 * @version 1.0
	 */
	public void vOutputConstraintFunction()
	{
		int i;
		// 現時点での各粒子の目的関数の値を出力します。
		for( i = 0; i < iGenNumber; i++ )
			System.out.print(pflfObjectiveFunction.lfObjectiveFunction(pplfGens[i]) + "," );
		System.out.print("\n");
	}

	/**
	 * <PRE>
	 * 　現時点でのもっともよい遺伝子の位置を出力します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/6/19
	 * @version 1.0
	 */
	public void vOutputGlobalMaxGenData()
	{
		int i;
		int iLoc = 0;
		double lfTemp = 0.0;
		double lfRes = 0.0;
		lfTemp = Double.MAX_VALUE;
		for( i = 0;i < iGenNumber; i++ )
		{
			// 現時点での各遺伝子の目的関数の値がもっとも最適値になっているものを出力します。
			lfRes = pflfObjectiveFunction.lfObjectiveFunction(pplfGens[i]);
			if( lfRes <= lfTemp )
			{
				lfTemp = lfRes;
				iLoc = i;
			}
		}
		// 最適値になっている遺伝子を出力します。
		for( i = 0;i < iGenVector; i++ )
			System.out.print(pplfGens[iLoc][i]+",");
		System.out.print("\n");
	}

	/**
	 * <PRE>
	 * 　現時点でのもっともよい遺伝子の目的関数値を出力します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/12/16
	 * @version 1.0
	 */
	public void vOutputGlobalMaxConstFuncValue()
	{
		int i;
		double lfTemp = 0.0;
		double lfRes = 0.0;
		lfTemp = Double.MAX_VALUE;
		for( i = 0;i < iGenNumber; i++ )
		{
			// 現時点での各遺伝子の目的関数の値を出力します。
			lfRes = pflfObjectiveFunction.lfObjectiveFunction(pplfGens[i]);
			if( lfRes <= lfTemp )
			{
				lfTemp = lfRes;
			}
		}
		// 最適値になっている目的関数の値を出力します。
		System.out.println( lfTemp );
	}

	/**
	 * <PRE>
	 * 　現時点でのもっともよい遺伝子及びその目的関数値を出力します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/12/16
	 * @version 1.0
	 */
	public void vOutputGlobalMaxGenDataConstFuncValue()
	{
		int i;
		int iLoc = 0;
		double lfTemp = 0.0;
		double lfRes = 0.0;
		lfTemp = Double.MAX_VALUE;
		for( i = 0;i < iGenNumber; i++ )
		{
			// 現時点での各遺伝子の目的関数の値を出力します。
			lfRes = pflfObjectiveFunction.lfObjectiveFunction(pplfGens[i]);
			if( lfRes <= lfTemp )
			{
				lfTemp = lfRes;
				iLoc = i;
			}
		}
		// 最適値になっている遺伝子を出力します。
		for( i = 0;i < iGenVector; i++ )
			System.out.print( pplfGens[iLoc][i] );
		// 最適値になっている目的関数の値を出力します。
		System.out.print( lfTemp );
	}
}
