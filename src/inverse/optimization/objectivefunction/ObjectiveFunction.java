package inverse.optimization.objectivefunction;

import inverse.optimization.abc.Abc;
import utility.sfmt.Sfmt;

/**
 * <PRE>
 *    目的関数のコールバック関数実体クラスです。
 *    目的関数をここに定義して使用します。コールバックの意味合いが薄れてしまったので、
 *    より適切に実装する必要があるのでそこは今後の課題です。
 *    目的関数として、逆シミュレーション用評価指標のNEDOCS、EDWIN、EDWorkScoreを実装しています。
 *    他は単目的最適化に使用しているスペック関数をできるだけ実装しています。
 *    使用方法はvSetFunctionMode()で使用する目的関数を設定し、lfObjectiveFunction()で実際に目的関数を実行します。
 * </PRE>
 * @author kobayashi
 *
 */
public class ObjectiveFunction implements ObjectiveFunctionInterface
{

	private int iObjectiveFunctionMode;
	private Sfmt rnd;

	public ObjectiveFunction()
	{
		long seed;
		seed = System.currentTimeMillis();
		rnd = new Sfmt( (int)seed );
	}

	/**
	 * <PRE>
	 *   コールバック関数の定義を記述します。
	 * </PRE>
	 * @param plfArg 変数
	 */
	@Override
	public double lfObjectiveFunction( double[] plfArg )
	{
		// TODO 自動生成されたメソッド・スタブ
		double lfRes = 0.0;

//		lfRes = lfSphere( plfArg );
		// 逆シミュレーション用の評価指標を記述します。
		if( iObjectiveFunctionMode == 1 )
		{
			lfRes = lfSphere( plfArg );
		}
		else if( iObjectiveFunctionMode == 2 )
		{
			lfRes = lfHyperEllipsoid( plfArg );
		}
		else if( iObjectiveFunctionMode == 3 )
		{
			lfRes = lfAxisParallelHyperEllipsoid( plfArg );
		}
		else if( iObjectiveFunctionMode == 4 )
		{
			lfRes = lfRotatedHyperEllipsoid( plfArg );
		}
		else if( iObjectiveFunctionMode == 5 )
		{
			lfRes = lfMovedAxisParallelHyperEllipsoid( plfArg );
		}
		else if( iObjectiveFunctionMode == 6 )
		{
			lfRes = lfSumOfDifferentPower( plfArg );
		}
		else if( iObjectiveFunctionMode == 7 )
		{
			lfRes = lfRosenbrock( plfArg );
		}
		else if( iObjectiveFunctionMode == 8 )
		{
			lfRes = lfRosenbrockStar( plfArg );
		}
		else if( iObjectiveFunctionMode == 9 )
		{
			lfRes = lf3rdDeJongsFunc( plfArg );
		}
		else if( iObjectiveFunctionMode == 10 )
		{
			lfRes = lfModified3rdDeJongsFunc( plfArg );
		}
		else if( iObjectiveFunctionMode == 11 )
		{
			lfRes = lf4thDeJongsFunc( plfArg );
		}
		else if( iObjectiveFunctionMode == 12 )
		{
			lfRes = lfModified4thDeJongsFunc( plfArg );
		}
		else if( iObjectiveFunctionMode == 13 )
		{
			lfRes = lf5thDeJongsFunc( plfArg );
		}
		else if( iObjectiveFunctionMode == 14 )
		{
			lfRes = lfAckley( plfArg );
		}
		else if( iObjectiveFunctionMode == 15 )
		{
			lfRes = lfEasoms( plfArg );
		}
		else if( iObjectiveFunctionMode == 16 )
		{
			lfRes = lfExtendEasoms( plfArg );
		}
		else if( iObjectiveFunctionMode == 17 )
		{
			lfRes = lfExtendEasoms( plfArg );
		}
		else if( iObjectiveFunctionMode == 18 )
		{
			lfRes = lfEqualityConstrained( plfArg );
		}
		else if( iObjectiveFunctionMode == 19 )
		{
			lfRes = lfGriewank( plfArg );
		}
		else if( iObjectiveFunctionMode == 20 )
		{
			lfRes = lfMichaelwicz(plfArg);
		}
		else if( iObjectiveFunctionMode == 21 )
		{
			lfRes = lfKatsuura(plfArg);
		}
		else if( iObjectiveFunctionMode == 22 )
		{
			lfRes = lfRastrigin(plfArg);
		}
		else if( iObjectiveFunctionMode == 23 )
		{
			lfRes = lfRastriginShift(plfArg);
		}
		else if( iObjectiveFunctionMode == 24 )
		{
			lfRes = lfSchwefel(plfArg);
		}
		else if( iObjectiveFunctionMode == 25 )
		{
			lfRes = lfSixHumpCamelBack(plfArg);
		}
		else if( iObjectiveFunctionMode == 26 )
		{
			lfRes = lfLangermann(plfArg);
		}
		else if( iObjectiveFunctionMode == 27 )
		{
			lfRes = lfBraninsRCos(plfArg);
		}
		else if( iObjectiveFunctionMode == 28 )
		{
			lfRes = lfShubert(plfArg);
		}
		else if( iObjectiveFunctionMode == 29 )
		{
			lfRes = lfDropWave( plfArg );
		}
		else if( iObjectiveFunctionMode == 30 )
		{
			lfRes = lfGoldsteinPrice( plfArg );
		}
		else if( iObjectiveFunctionMode == 31 )
		{
			lfRes = lfShekelsFoxholes( plfArg );
		}
		else if( iObjectiveFunctionMode == 32 )
		{
			lfRes = lfPavianiFoxholes( plfArg );
		}
		else if( iObjectiveFunctionMode == 33 )
		{
			lfRes = lfSineEnvelopeSineWave( plfArg );
		}
		else if( iObjectiveFunctionMode == 34 )
		{
			lfRes = lfEggHolder( plfArg );
		}
		else if( iObjectiveFunctionMode == 35 )
		{
			lfRes = lfRana( plfArg );
		}
		else if( iObjectiveFunctionMode == 36 )
		{
			lfRes = lfPathologicalTest( plfArg );
		}
		else if( iObjectiveFunctionMode == 37 )
		{
			lfRes = lfMasterCosineWave( plfArg );
		}
		else if( iObjectiveFunctionMode == 38 )
		{
			lfRes = lfKeane( plfArg );
		}
		else if( iObjectiveFunctionMode == 39 )
		{
			lfRes = lfTrid( plfArg );
		}
		else if( iObjectiveFunctionMode == 40 )
		{
			lfRes = lfkTablet( plfArg );
		}
		else if( iObjectiveFunctionMode == 41 )
		{
			lfRes = lfSchaffer( plfArg );
		}
		else if( iObjectiveFunctionMode == 42 )
		{
			lfRes = lfBohachevsky( plfArg );
		}
		else if( iObjectiveFunctionMode == 43 )
		{
			lfRes = lfZakharov( plfArg );
		}
		else if( iObjectiveFunctionMode == 44 )
		{
			lfRes = lfSalomonProblem( plfArg );
		}
		else if( iObjectiveFunctionMode == 45 )
		{
			lfRes = lfAlpine( plfArg );
		}
		else if( iObjectiveFunctionMode == 46 )
		{
			lfRes = lfWeierstrass( plfArg );
		}
		else if( iObjectiveFunctionMode == 47 )
		{
			lfRes = lfLevy( plfArg );
		}
		else if( iObjectiveFunctionMode == 48 )
		{
			lfRes = lfBukin( plfArg );
		}
		else if( iObjectiveFunctionMode == 49 )
		{
			lfRes = lfMccormick( plfArg );
		}
		// 逆シミュレーション用評価指標(独自設定)
		else if( iObjectiveFunctionMode == 100 )
		{
		}
		// 逆シミュレーション用評価指標(NEDOCS)
		else if( iObjectiveFunctionMode == 101 )
		{
			lfRes = EvaluationIndicatorInvSimNedocs( plfArg );
		}
		// 逆シミュレーション用評価指標(ED Work Score)
		else if( iObjectiveFunctionMode == 102 )
		{
			lfRes = EvaluationIndicatorInvSimEdWorkScore( plfArg );
		}
		// 逆シミュレーション用評価指標(EDWIN)
		else if( iObjectiveFunctionMode == 103 )
		{
			lfRes = EvaluationIndicatorInvSimEdWin( plfArg );
			lfRes = 0.015*EvaluationIndicatorInvSimNedocs( plfArg )+0.22;
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 *    ベンチマークとして使用する関数を指定します。
	 *    100 は逆シミュレーション用の評価指標とします。
	 * </PRE>
	 * @param iMode 使用する目的関数の番号
	 */
	@Override
	public void vSetFunctionMode( int iMode )
	{
		iObjectiveFunctionMode = iMode;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////評価関数

	/**
	 * <PRE>
	 * 　目的関数のSphere(1st De Jong's function)関数の計算を実行します。
	 * 　-5.12 &lt;= x_i &lt;= 5.12 f_i(x_i)=0,  x_i=0, i = 1,2,･･･,n
	 * 　f(x) = sum(x_{i}^{2})
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 2016/9/27 関数計算の最適化
	 * </PRE>
	 * @param plfArgs			引数
	 * @return Sphere関数の値
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 0.2
	 */
	private double lfSphere( double[] plfArgs )
	{
		int i;
		double lfRes = 0.0;
		int iVector1 = plfArgs.length%5;
		for( i = 0;i < iVector1; i++ )
			lfRes += plfArgs[i]*plfArgs[i];
		for( i = iVector1;i < plfArgs.length; i+=5 )
			lfRes += ( plfArgs[i]*plfArgs[i] + plfArgs[i+1]*plfArgs[i+1] + plfArgs[i+2]*plfArgs[i+2] + plfArgs[i+3]*plfArgs[i+3] + plfArgs[i+4]*plfArgs[i+4] );
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のEllipsoid関数の計算を実行します。
	 * 　-5.12 &lt;= x_i &lt;= 5.12 f_i(x_i)=0,  x_i=0, i = 1,2,･･･,n
	 * 　f(x) = sum(1000^{i-1/n-1}x_{i})^{2}
	 *   ver 0.1 2016/08/24 初版
	 *   ver 0.2 2016/11/01 高速化及び修正
	 * </PRE>
	 * @param plfArgs			引数
	 * @return					Ellipsoid関数の値
	 * @author kobayashi
	 * @since 2016/8/24
	 * @version 0.2
	 */
	private double lfEllipsoid( double[] plfArgs  )
	{
		int i;
		double lfRes = 0.0;
		double lfX = 0.0;
		double lfPower = 0.0;
		double lfPowerRes = 1.0;

		lfPower = Math.pow(1000, 1.0 / (double)(plfArgs.length - 1));
		lfRes = plfArgs[0]*plfArgs[0];
		for( i = 1;i < plfArgs.length; i++ )
		{
			lfPowerRes *= lfPower;
			lfX = lfPowerRes*plfArgs[i];
			lfRes += lfX*lfX;
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のHyper-Ellipsoid関数の計算を実行します。
	 * 　-1 &lt;== x_i &lt;= 1 f_i(x_i)=0,  x_i=0, i = 1,2,･･･,n
	 * 　f(x) = sum(i^{2}*x_{i}^{2})
	 *   ver 0.1 2015/06/12 初期版
	 *   ver 0.2 2016/11/30 実装ミスを修正
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/12
	 * @version 0.2
	 */
	double lfHyperEllipsoid( double[] plfArgs )
	{
		int i;
		double lfRes = 0.0;
		for( i = 0;i < plfArgs.length; i++ )
		{
			lfRes += (double)(i+1)*(double)(i+1)*plfArgs[i]*plfArgs[i];
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のAxis Parallel hyper-ellipsoid関数の計算を実行します。
	 * 　-5.12 &lt;= x_i &lt;= 5.12,  f_{i}(x_{i})=0,  x_{i}=0, i = 1,2,･･･,n
	 * 　f(x) = sum(i*x_{i}^{2})
	 *   ver 0.1 2015/06/12 初期版
	 *   ver 0.2 2016/11/30 実装ミスを修正
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/12
	 * @version 0.2
	 */
	double lfAxisParallelHyperEllipsoid( double[] plfArgs )
	{
		int i;
		double lfRes = 0.0;
		for( i = 0;i < plfArgs.length; i++ )
		{
			lfRes += (double)(i+1)*plfArgs[i]*plfArgs[i];
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のRotated hyper-ellipsoid関数の計算を実行します。
	 *   大域的最適解 x_{i} = 0 のときf(x_{i}) = 0 (-65.536 &lt;= Xi &lt;= 65.536)
	 * 　\sum_{i=1}^{n}(\sum_{j=1}^{i}(x_{j}))^{2}
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/12
	 * @version 1.0
	 */
	double lfRotatedHyperEllipsoid( double[] plfArgs )
	{
		int i,j;
		double lfRes = 0.0;
		for( i = 0;i < plfArgs.length; i++ )
		{
			for( j = 0;j < i; j++ )
			{
				lfRes += plfArgs[i]*plfArgs[i];
			}
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のMoved axis parallel hyper-ellipsoid関数の計算を実行します。
	 *   大域的最適解 x_{i} = 5i のときf(x_{i})=0 (-5.12 &lt;= x_{i} &lt;= 5.12)
	 * 　\sum{i=1}^{n}(5*i*x_{i}^{2})
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/12
	 * @version 1.0
	 */
	double lfMovedAxisParallelHyperEllipsoid( double[] plfArgs )
	{
		int i;
		double lfRes = 0.0;
		for( i = 0;i < plfArgs.length; i++ )
		{
			lfRes += 5.0*(double)i*plfArgs[i]*plfArgs[i];
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のSum of different power関数の計算を実行します。
	 *   大域的最適解 x_{i}=0 のとき f(x_{i})=0
	 *   \sum_{i=1}^{n}|x_{i}|^{i+1}
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/12
	 * @version 1.0
	 */
	double lfSumOfDifferentPower( double[] plfArgs)
	{
		int i;
		double lfRes = 0.0;
		for( i = 0;i < plfArgs.length; i++ )
		{
			lfRes += Math.abs( Math.pow(plfArgs[i], i+1 ) );
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のRosenbrock(2nd De Jong's function)関数の計算を実行します。
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 0.1
	 */
	double lfRosenbrock( double[] plfArgs  )
	{
		int i;
		double lfRes = 0.0;
		double lfTempX1 = 0.0;
		double lfTempX2 = 0.0;
		double lfXX = 0.0;
		for( i = 0;i < plfArgs.length-1; i++ )
		{
			lfXX = plfArgs[i]*plfArgs[i];
			lfTempX1 = 1.0-plfArgs[i];
			lfTempX2 = plfArgs[i+1]-lfXX;
			lfRes += (100*lfTempX2*lfTempX2+lfTempX1*lfTempX1);
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のRosenbrockStar型関数の計算を実行します。
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2016/8/24
	 * @version 0.1
	 */
	double lfRosenbrockStar( double[] plfArgs  )
	{
		int i;
		double lfRes = 0.0;
		double lfTempX1 = 0.0;
		double lfTempX2 = 0.0;
		double lfXX = 0.0;
		for( i = 1;i < plfArgs.length; i++ )
		{
			lfXX = plfArgs[i]*plfArgs[i];
			lfTempX1 = 1.0-plfArgs[i];
			lfTempX2 = plfArgs[0]-lfXX;
			lfRes += (100*lfTempX2*lfTempX2+lfTempX1*lfTempX1);
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数の3rd De Jong's関数の計算を実行します。
	 *   大域的最適解 Xi = 1 のときf(Xi) = 0
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/12
	 * @version 1.0
	 */
	double lf3rdDeJongsFunc( double[] plfArgs  )
	{
		int i;
		double lfRes = 0.0;
		for( i = 0;i < plfArgs.length; i++ )
		{
			lfRes += Math.abs( plfArgs[i] );
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のModified 3rd De Jong's関数の計算を実行します。
	 *   大域的最適解 Xi = 1 のときf(Xi) = 0
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/12
	 * @version 1.0
	 */
	double lfModified3rdDeJongsFunc( double[] plfArgs  )
	{
		int i;
		double lfRes = 0.0;
		double lfConst = 30.0;
		for( i = 0;i < plfArgs.length; i++ )
		{
			lfRes += Math.floor( plfArgs[i] );
		}
		lfRes = lfRes + lfConst;
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数の4th DeJong's Function( Quartic Function )関数の計算を実行します。
	 *	 \sum^{n}_{i=1}(ix_{i}^4)
	 * 　大域的最適解 -1.28 \leq x_{i} \leq 1.28 x = (0,0,0,0...,0)
	 *  ver 0.1 初期版
	 *  ver 0.2 2016/12/05 実装ミスを修正
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/12
	 * @version 0.2
	 */
	double lf4thDeJongsFunc( double[] plfArgs  )
	{
		int i;
		double lfRes = 0.0;
		double lfXX;
		for( i = 0;i < plfArgs.length; i++ )
		{
			lfXX = plfArgs[i]*plfArgs[i];
			lfRes += (double)(i+1)*lfXX*lfXX;
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　的関数のModified 4th DeJong's Function(Quartic Gussian Function)関数の計算を実行します。
	 *  大域的最適解 Xi = 1 のときf(Xi) = 0
	 *	 \sum^{n}_{i=1}(ix_{i}^4)+random[0,1)
	 * 　大域的最適解 -1.28 \leq x_{i} \leq 1.28 x = (0,0,0,0...,0)
	 *  ver 0.1 初期版
	 *  ver 0.2 2016/12/05 実装ミスを修正
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/10/16
	 * @version 0.2
	 */
	double lfModified4thDeJongsFunc( double[] plfArgs  )
	{
		int i;
		double lfRes = 0.0;
		double lfXX;
		for( i = 0;i < plfArgs.length; i++ )
		{
	//		if (-1.28 <= plfArgs[i] && plfArgs[i] <= 1.28)
			{
				lfXX = plfArgs[i] * plfArgs[i];
				lfRes += (double)(i + 1)*lfXX*lfXX;
			}
	//		else
			{
	//			lfRes += (double)(i + 1)*1.28*1.28*1.28*1.28;
			}
		}
		return lfRes+rnd.NextUnif();
	}

	/**
	 * <PRE>
	 * 　目的関数のDe Jong's f5関数の計算を実行します。
	 *   2次元データに関してのベンチマーク関数です。
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 2016/9/27 完成していなかったのでコードを追加し完成。
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 0.1
	 */
	double lf5thDeJongsFunc( double[] plfArgs  )
	{
		double lfRes = 0.0;
		double lfTemp;
		double lfT,lfT2;
		double[][] alfA = {{-32,-16,  0, 16, 32,-32,-16,  0, 16, 32,-32,-16, 0, 16, 32, -32, -16,  0, 16, 32, -32, -16,  0, 16, 32},
						  {-32,-32,-32,-32,-32,-16,-16,-16,-16,-16,  0,  0, 0,  0,  0,  16,  16, 16, 16, 16,  32,  32, 32, 32, 32}};
		int i,j;

		for( i = 0;i < 25; i++ )
		{
			lfTemp = 0.0;
			for( j = 0;j < 2; j++ )
			{
				lfT = plfArgs[i]-alfA[i][j];
				lfT2 = lfT*lfT;
				lfTemp += lfT2;
			}
			lfRes += 1.0/lfTemp;
		}
		return  1.0/(0.002+lfRes);
	}

	/**
	 * <PRE>
	 * 　目的関数のAckley Function関数の計算を実行します。
	 *   大域的最適解 Xi = 0 のときf(Xi) = 0
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/10/16
	 * @version 1.0
	 */
	double lfAckley( double[] plfArgs  )
	{
		int i;
		int iVectorLen51, iVectorLen52;
		double lfRes = 0.0;
		double lfCos = 0.0;
		double lfX2 = 0.0;
		double lfE = Math.exp(1.0);
		double lf2pi = Math.PI+Math.PI;
		double[] alfCos;
		double[] alfX2;

		alfCos = new double[5];
		alfX2 = new double[5];

		iVectorLen51 = plfArgs.length % 5;
		iVectorLen52 = plfArgs.length / 5;
		for (i = 0; i < iVectorLen51; i++)
		{
			lfX2 += plfArgs[i] * plfArgs[i];
			lfCos += Math.cos(lf2pi*plfArgs[i]);
		}
		for (i = iVectorLen51; i < plfArgs.length; i+=5)
		{
			lfX2 += plfArgs[i]*plfArgs[i] + plfArgs[i+1]*plfArgs[i+1] + plfArgs[i+2]*plfArgs[i+2] + plfArgs[i+3]*plfArgs[i+3] + plfArgs[i+4]*plfArgs[i+4];
			lfCos += Math.cos(lf2pi*plfArgs[i]) + Math.cos(lf2pi*plfArgs[i+1]) + Math.cos(lf2pi*plfArgs[i+2]) + Math.cos(lf2pi*plfArgs[i+3]) + Math.cos(lf2pi*plfArgs[i+4]);
		}
		lfX2 = lfX2 / (double)plfArgs.length;
		lfCos = lfCos / (double)plfArgs.length;

		lfRes = 20.0 - 20.0*Math.exp(-0.2*Math.sqrt(lfX2)) + lfE - Math.exp(lfCos);
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のEasom's Function関数の計算を実行します。
	 *   f(x) = -\cos(x_{1})\cos(x_{2})\exp(-(x_{1}-\pi)^{2}-(x_{2}-\pi)^{2})
	 *   大域的最適解 Xi = pi のときf(Xi) = -1
	 *   2次元関数であることに注意。
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 2016/9/27 実装していなかったので実装
	 *   ver 0.3 2016/11/29 実装ミスを修正
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/10/16
	 * @version 0.3
	 */
	double lfEasoms( double[] plfArgs  )
	{
		double lfRes = 0.0;
		double lfDiff1 = 0.0;
		double lfDiff2 = 0.0;

		lfDiff1 = plfArgs[0]-Math.PI;
		lfDiff2 = plfArgs[1]-Math.PI;

		lfRes = Math.cos(plfArgs[0])*Math.cos(plfArgs[1])*Math.exp(-(lfDiff1*lfDiff1+lfDiff2*lfDiff2));
		return -lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のEasom's Function(Xin-She Yang extended in 2008 this function to n dimensions)の計算を実行します。
	 *   大域的最適解 x_{i}=pi のとき f(x_{i}) = -1, -2π&lt;=x_{i}&lt;=2π
	 *   Easom's functionのn次元バージョン
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/10/16
	 * @version 1.0
	 */
	double lfExtendEasoms( double[] plfArgs  )
	{
		int i;
		double lfRes = 0.0;
		double lfProductRes = 1.0;
		double lfAddRes = 0.0;
		double lfCos;
		double lfDiff;
		int iSgn;

		iSgn = plfArgs.length < 0 ? -1 : 1;
		for( i = 0;i < plfArgs.length; i++ )
		{
			lfCos = Math.cos( plfArgs[i] );
			lfDiff = plfArgs[i]-Math.PI;
			lfProductRes *= lfCos*lfCos;
			lfAddRes += lfDiff*lfDiff;
		}
		lfRes = -1.0*lfProductRes*Math.exp( -lfAddRes )*iSgn;
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のEquality-Constrained 関数の計算を実行します。
	 *   -\sqrt(n)^{n}\PRO^{n}_i=1 x_{i}
	 *   大域的最適解 Xi = 1.0/\sqrt(n) のときf(Xi) = -1 (0 &lt;= Xi &lt;= 1.0)
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 2016/9/27 関数に誤りがあり修正
	 *   ver 0.3 2016/12/5 実装ミスのため修正
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 0.3
	 */
	double lfEqualityConstrained( double[] plfArgs  )
	{
		int i;
		double lfRes  = 1.0;
		double lfSqrt;

		lfSqrt = Math.sqrt(1.0/(double)plfArgs.length);
		for( i = 0; i < plfArgs.length; i++ )
		{
			if (0.0 <= plfArgs[i] && plfArgs[i] <= 1.0)
				lfRes *= lfSqrt*plfArgs[i];
		}
		return -lfRes;
	}


	/**
	 * <PRE>
	 * 　目的関数のGriewank関数の計算を実行します。
	 *   大域的最適解 Xi = 0 のときf(Xi) = 0 (-600 &lt;= Xi &lt;= 600)
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 2016/9/27 関数の計算部分の最適化を実施
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 0.2
	 */
	double lfGriewank( double[] plfArgs  )
	{
		int i;
		double lfRes  = 0.0;
		double lfRes1 = 0.0;
		double lfRes2 = 1.0;

		for( i = 0; i < plfArgs.length; i++ )
		{
			lfRes1 += plfArgs[i]*plfArgs[i];
			lfRes2 *= Math.cos(plfArgs[i]/Math.sqrt((double)i+1));
		}
		lfRes = 1.0 + lfRes1*0.00025 - lfRes2;
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のMichaelwicz's関数の計算を実行します。
	 *   大域的最適解 Xi = (X1, X2) = (2.20319, 1.57049) のときf(Xi) = -1.8013 ( 0.0 &lt;= Xi &lt;= π)
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 2016/9/27 誤りがあり修正
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 0.2
	 */
	double lfMichaelwicz( double[] plfArgs  )
	{
		int i;
		int m;
		double lfRes = 0.0;

		m = 10;
		for( i = 0; i < plfArgs.length; i++ )
		{
			if (0.0 <= plfArgs[i] && plfArgs[i] <= Math.PI)
				lfRes += Math.sin(plfArgs[i])*Math.pow(Math.sin((double)(i + 1)*plfArgs[i] * plfArgs[i] / Math.PI), 2.0*m);
			else
				lfRes += 0.0;
		}
		return -lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のKatsuura's関数の計算を実行します。
	 *   大域的最適解 Xi = 0 のときf(Xi) = 1 (-1000 &lt;= Xi &lt;= 1000)
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 1.0
	 */
	double lfKatsuura( double[] plfArgs  )
	{
		int i,j;
		int M;
		double lfRes1 = 0.0;
		double lfRes2 = 0.0;
		double lfCurK = 0.0;
		double lfPrevK = 1.0;

		M = 32;
		for( i = 0; i < plfArgs.length; i++ )
		{
			for( j = 0;j < M; j++ )
			{
				lfCurK = 2.0*lfPrevK;
				lfRes1 += Math.abs(lfCurK*plfArgs[i]-Math.floor(lfCurK*plfArgs[i]))/lfCurK;
				lfPrevK = lfCurK;
			}
			lfRes2 += (1.0+(i+1.0)*lfRes1);
		}
		return lfRes2;
	}

	/**
	 * <PRE>
	 * 　目的関数のRastrigin関数の計算を実行します。
	 * 　大域的最適解 Xi = 0 f(Xi) = 0 (-5.12 &lt;= Xi &lt;= 5.12)
	 *   ver 0.2 2016/9/27 関数間違いの修正
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 0.2
	 */
	double lfRastrigin( double[] plfArgs  )
	{
		int i;
		double lfRes = 0.0;
		double lf2pi = 2.0*Math.PI;
		int iVectorLen5;

		iVectorLen5 = plfArgs.length % 5;

		for (i = 0; i < iVectorLen5; i++)
		{
			lfRes += (plfArgs[i] * plfArgs[i] - 10.0*Math.cos(lf2pi*plfArgs[i]));
		}
		for (i = iVectorLen5; i < plfArgs.length; i+=5)
		{
			lfRes += (plfArgs[i]*plfArgs[i] + plfArgs[i+1]*plfArgs[i+1] + plfArgs[i+2]*plfArgs[i+2] + plfArgs[i+3]*plfArgs[i+3] + plfArgs[i+4]*plfArgs[i+4] -
					  10.0*( Math.cos(lf2pi*plfArgs[i]) + Math.cos(lf2pi*plfArgs[i+1]) + Math.cos(lf2pi*plfArgs[i+2]) + Math.cos(lf2pi*plfArgs[i+3]) + Math.cos(lf2pi*plfArgs[i+4])));
		}
		return lfRes + 10.0*plfArgs.length;
	}

	/**
	 * <PRE>
	 * 　目的関数のRastriginShift関数の計算を実行します。
	 * 　大域的最適解 Xi = 1 f(Xi) = 0 (-5.12 &lt;= Xi &lt;= 5.12)
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 2016/9/27 関数の誤りの修正
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2016/8/24
	 * @version 0.1
	 */
	double lfRastriginShift( double[] plfArgs  )
	{
		int i;
		double lfXX;
		double lfRes = 0.0;
		double lf2pi = Math.PI+Math.PI;
		double[] alfXX;
		int iVectorLen5;

		alfXX = new double[5];
		iVectorLen5 = plfArgs.length % 5;

		for (i = 0; i < iVectorLen5; i++)
		{
			lfXX = 1.0 - plfArgs[i];
			lfRes += (lfXX*lfXX - 10.0*Math.cos(lf2pi*lfXX));
		}
		for (i = iVectorLen5; i < plfArgs.length; i += 5)
		{
			alfXX[0] = plfArgs[i]*plfArgs[i];
			alfXX[1] = plfArgs[i+1]*plfArgs[i+1];
			alfXX[2] = plfArgs[i+2]*plfArgs[i+2];
			alfXX[3] = plfArgs[i+3]*plfArgs[i+3];
			alfXX[4] = plfArgs[i+4]*plfArgs[i+4];
			lfRes += (alfXX[0]*alfXX[0] + alfXX[1]*alfXX[1] + alfXX[2]*alfXX[2] + alfXX[3]*alfXX[3] + alfXX[4]*alfXX[4] -
				10.0*(Math.cos(lf2pi*plfArgs[i]) + Math.cos(lf2pi*plfArgs[i + 1]) + Math.cos(lf2pi*plfArgs[i + 2]) + Math.cos(lf2pi*plfArgs[i + 3]) + Math.cos(lf2pi*plfArgs[i + 4])));
		}
		return lfRes + 10.0*plfArgs.length;
	}

	/**
	 * <PRE>
	 * 　目的関数のSchwefel's 関数の計算を実行します。
	 * 　大域的最適解 x_{i}=420.09687 f(x_{i})=-418.9829n (-512&lt;=x_{i}&lt;=512)
	 *   \sum^{n}_{i=1}(x_{i}\sin\sqr(|x_{i}|))
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 2016/9/27 関数の誤り修正
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 1.0
	 */
	double lfSchwefel( double[] plfArgs  )
	{
		int i;
		double lfRes = 0.0;
		for( i = 0;i < plfArgs.length; i++ )
		{
			if( -512 <= plfArgs[i] && plfArgs[i] <= 512 )
				lfRes += ( -plfArgs[i]*Math.sin(Math.sqrt(Math.abs(plfArgs[i]))) );
		}
		return 418.982887272433369*plfArgs.length-lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のSix-hump camel back 関数の計算を実行します。
	 * 　大域的最適解 (x_{1},x_{2})=420.09687 f(x_{1},x_{2})=-1.0316 (-3&lt;=x_{1}&lt;=3, -2&lt;=x_{2}&lt;=2)
	 *   \sum^{n}_{i=1}(x_{i}\sin\sqr(|x_{i}|))
	 *   2次元関数です。
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 2016/9/27 計算の最適化を実施
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 1.0
	 */
	double lfSixHumpCamelBack( double[] plfArgs  )
	{
		double lfRes = 0.0;
		double lfXX1;
		double lfXX2;
		lfXX1 = plfArgs[0]*plfArgs[0];
		lfXX2 = plfArgs[1]*plfArgs[1];
		lfRes = ( 4.0-2.1*lfXX1+lfXX1*lfXX1/3.0 )*lfXX1 + plfArgs[0]*plfArgs[1] + 4.0*(lfXX2-1.0)*lfXX2;
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のShubert's 関数の計算を実行します。(2次元関数)
	 * 　大域的最適解 (x_{1},x_{2})=-186.7309 f(x_{1},x_{2})=-1.0316 (-3&lt;=x_{1}&lt;=3, -2&lt;=x_{2}&lt;=2)
	 *   \sum^{n}_{i=1}(i*\cos(i+(i+1x))*\sum^{n}_{i=1}(i*\cos(i+(i+1x))
	 *   2次元関数。
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 2016/9/27 関数計算の最適化
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 0.2
	 */
	double lfShubert( double[] plfArgs  )
	{
		int i;
		int iData;
		int n;
		double lfResX = 0.0;
		double lfResY = 0.0;
		n = 5;

		for( i = 0;i < n; i++ )
		{
			iData = i+2;
			lfResX += (double)(i+1)*Math.cos(i+1+iData*plfArgs[0]);
			lfResY += (double)(i+1)*Math.cos(i+1+iData*plfArgs[1]);
		}
		return lfResX*lfResY;
	}

	/**
	 * <PRE>
	 * 　目的関数のGoldstein-Price's 関数の計算を実行します。(2次元関数)
	 * 　大域的最適解 (x_{1},x_{2})=3 f(x_{1},x_{2})=(0,1) (-2&lt;=x_{1}&lt;=2, -2&lt;=x_{2}&lt;=2)
	 *   2次元関数です。
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 条件を追加(2次元以外はすべて0)
	 *   ver 0.3 2016/11/29 関数の誤りを修正
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 0.3
	 */
	double lfGoldsteinPrice( double[] plfArgs  )
	{
		double lfRes = 0.0;
		double lfX2_1,lfX2_2,lfX2_3,lfX2_4,lfX2_5;

		lfX2_1 = (plfArgs[0]+plfArgs[1]+1);
		lfX2_1 *= lfX2_1;
		lfX2_2 = (2*plfArgs[0]-3*plfArgs[1]);
		lfX2_2 *= lfX2_2;
		lfX2_3 = plfArgs[0]*plfArgs[0];
		lfX2_4 = plfArgs[1]*plfArgs[1];
		lfX2_5 = plfArgs[0]*plfArgs[1];

		lfRes = ( 1.0+lfX2_1*( 19.0-14.0*plfArgs[0]+3*lfX2_3-14*plfArgs[1]+6*lfX2_5+3*lfX2_4 ) ) *
				( 30.0+lfX2_2*( 18.0-32.0*plfArgs[0]+12.0*lfX2_3+48.0*plfArgs[1]-36.0*lfX2_5+27.0*lfX2_4) );

		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のBranins's rcos 関数の計算を実行します。(2次元関数)
	 * 　大域的最適解 (x_{1},x_{2})=0.397887 f(x_{1},x_{2})=(-π,12.275), (π,2.275) (9.42478,2.475)
	 *   ver 0.1 2015/6/17 初期版
	 *   ver 0.2 2016/11/29 実装誤りの修正
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 0.2
	 */
	double lfBraninsRCos( double[] plfArgs  )
	{
		double lfRes = 0.0;
		double lfX2_1;
		double lfA,lfB,lfC,lfD,lfE,lfF;

		lfA = 1.0;
		lfB = 5.1/(4.0*Math.PI*Math.PI);
		lfC = 5.0/Math.PI;
		lfD = 6.0;
		lfE = 10.0;
		lfF = 1.0/(8.0*Math.PI);
		lfX2_1 = plfArgs[1]-lfB*plfArgs[0]*plfArgs[0]+lfC*plfArgs[0]-lfD;
		lfX2_1 *= lfX2_1;

		lfRes = lfA*lfX2_1 + lfE*(1.0-lfF)*Math.cos(plfArgs[0]) + lfE;

		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のLangermann's 関数の計算を実行します。
	 *	 \sum^{m}_{i=1}c_{i}*\exp( -\dfrac{1}{\pi}\sum^{n}_{j=1}(x_{j}-a_{ij})^{2} )cos(\pi\sum^{n}_{j=1}(x_{j}-a_{ij})^{2})
	 * 　大域的最適解 -5.12 \leq X_{1}, X_{2} \leq 5.12
	 *   M の値は推奨値が5とされている。
	 * </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 1.0
	 */
	double lfLangermann( double[] plfArgs  )
	{
		int i,j;
		int M = 5;
		double lfRes1 = 0.0;
		double lfRes2 = 0.0;
		double lfRes = 0.0;
		double[][] pplfA = {{3,5,2,1,7},{5,2,1,4,9}};
		double[] plfC = {1,2,5,2,3};

		for( i = 0;i < M; i++ )
		{
			lfRes1 = lfRes2 = 0.0;
			for( j = 0;j < plfArgs.length; j++ )
			{
				lfRes1 += (plfArgs[j]-pplfA[i][j])*(plfArgs[j]-pplfA[i][j]);
			}
			lfRes2 = lfRes1;
			lfRes1 *= -1.0/Math.PI;
			lfRes2 *= Math.PI;
			lfRes += plfC[i]*Math.exp(lfRes1)*Math.cos(lfRes2);
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のDrop wave 関数の計算を実行します。(2次元関数)
	 *	 ( 1.0+\cos(12*\sqrt{X_{1}*X_{1}+X_{2}*X_{2}) )/( 1/2*(X_{1}*X_{1}+X_{2}*X_{2} )+2 )
	 * 　大域的最適解 -5.12 \leq X_{1}, X_{2} \leq 5.12
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 2016/9/27 関数計算の最適化
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 0.2
	 */
	double lfDropWave( double[] plfArgs  )
	{
		double lfRes = 0.0;
		double lfDist;

		lfDist = plfArgs[0]*plfArgs[0]+plfArgs[1]*plfArgs[1];
		lfRes = ( 1.0+Math.cos(12.0*Math.sqrt(lfDist) ) )/( 0.5*lfDist+2.0 );
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のShekel's Foxholes 関数の計算を実行します。
	 *	 -\sum^{m}_{i=1}( \sum^{n}_{j=1}(x_{j}-a_{ij})^{2} + c_{i} ) - 1
	 * 　大域的最適解 -5.12 \leq X_{1}, X_{2} \leq 5.12
	 *   M の値は推奨値が30とされている。
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 1.0
	 */
	double lfShekelsFoxholes( double[] plfArgs  )
	{
		int i,j;
		int M = 30;
		double lfRes1 = 0.0;
		double lfRes2 = 0.0;
		double lfRes = 0.0;
		double[][] pplfA = {{3,5,2,1,7},{5,2,1,4,9}};
		double[] plfC = {1,2,5,2,3};

		for( i = 0;i < M; i++ )
		{
			for( j = 0;j < plfArgs.length; j++ )
			{
				lfRes1 += (plfArgs[j]-pplfA[i][j])*(plfArgs[j]-pplfA[i][j]) + plfC[i];
			}
		}
		return lfRes-1.0;
	}

	/**
	 * <PRE>
	 * 　目的関数のPaviani's Foxholes 関数の計算を実行します。
	 *	 -\sum^{m}_{i=1}( \sum^{n}_{j=1}(x_{j}-a_{ij})^{2} + c_{i} ) - 1
	 * 　大域的最適解 -5.12 \leq X_{1}, X_{2} \leq 5.12
	 *   M の値は推奨値が30とされている。
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 1.0
	 */
	double lfPavianiFoxholes( double[] plfArgs )
	{
		double lfRes1 = 0.0;
		double lfRes2 = 0.0;
		double lfRes11 = 0.0;
		double lfRes12 = 1.0;
		int i;

		for( i = 0;i < plfArgs.length; i++ )
		{
			lfRes1 = Math.log( plfArgs[i]-2 );
			lfRes1 *= lfRes1;
			lfRes2 = Math.log( 10.0-plfArgs[i] );
			lfRes2 *= lfRes2;
			lfRes11 += lfRes1 + lfRes2;
		}
		for( i = 0;i < plfArgs.length; i++ )
		{
			lfRes12 *= plfArgs[i];
		}
		lfRes12 = Math.pow( lfRes12, 0.2 );
		return lfRes11 - lfRes12;
	}

	/**
	 * <PRE>
	 * 　目的関数のSine envelope sine wave 関数の計算を実行します。
	 *	 -\sum^{n-1}_{i=1}( \dfrac{\sin^{2}(\sqrt(x^{2}_{i+1}+x^{2}_{i})-0.5)}{(0.001(x^{2}_{i+1}+x^{2}_{i} + 1))^{2}} + 0.5)
	 * 　大域的最適解 -100 \leq X_{i} \leq 100
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 2016/9/27 関数に誤りがあり修正
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 0.1
	 */
	double lfSineEnvelopeSineWave( double[] plfArgs )
	{
		double lfRes = 0.0;
		double lfDist = 0.0;
		double lfDist2 = 0.0;
		double lfSin = 0.0;
		int i;

		for( i = 0;i < plfArgs.length-1; i++ )
		{
			lfDist = plfArgs[i]*plfArgs[i]+plfArgs[i+1]*plfArgs[i+1];
			lfSin = Math.sin( Math.sqrt(lfDist) - 0.5 );
			lfDist = (0.001*lfDist+1.0);
			lfRes += (lfSin*lfSin)/(lfDist*lfDist) + 0.5;
		}
		return -lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のEgg Hloder 関数の計算を実行します。
	 *	 -\sum^{m}_{i=1}( \sum^{n}_{j=1}(x_{j}-a_{ij})^{2} + c_{i} ) - 1
	 * 　大域的最適解 -512 \leq X_{1}, X_{2} \leq 512
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 関数の算出に誤りがあったので修正
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 0.2
	 */
	double lfEggHolder( double[] plfArgs )
	{
		double lfRes = 0.0;
		double lfDist = 0.0;
		double lfDist2 = 0.0;
		double lfSin = 0.0;
		double lfXi = 0.0;
		int i;

		for( i = 0;i < plfArgs.length-1; i++ )
		{
			lfXi = plfArgs[i+1]+47.0;
			lfRes += -lfXi*Math.sin( Math.sqrt( Math.abs( lfXi+0.5*plfArgs[i] ) ) )-plfArgs[i]*Math.sin( Math.sqrt( Math.abs( plfArgs[i]-lfXi ) ) );
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のRana's 関数の計算を実行します。
	 *	 -(x_{2}-47)\sin{sqrt(x_{2}+dfrac{x_{1}}{2}+47))-x_{1}\sin{\sqrt{\abs{x_{1}-(x_{2}+47)}}}
	 * 　大域的最適解 -500 \leq X_{1}, X_{2} \leq 500
	 *   f(x)=-50, x= -200
	 *   ver 0.1 初期バージョン
	 *   ver 0.2 関数算出最適化
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 1.0
	 */
	double lfRana( double[] plfArgs )
	{
		double lfRes = 0.0;
		double lfX11 = 0.0;
		double lfX12 = 0.0;
		double lfXAbs1,lfXAbs2;
		int i;

		for( i = 1;i < plfArgs.length-1; i++ )
		{
			lfX11 = plfArgs[i]+1;
			lfX12 = -plfArgs[i]+1;
			lfXAbs1 = Math.abs( plfArgs[i+1]+lfX12 );
			lfXAbs2 = Math.abs( plfArgs[i+1]+lfX11 );
			lfRes = lfX11*Math.cos( Math.sqrt( lfXAbs1 ) )*Math.sin( Math.sqrt( lfXAbs2 ) )+plfArgs[i]*Math.cos( Math.sqrt( lfXAbs2 ) )*Math.sin( lfXAbs1 );
		}
		return lfRes;
	}


	/**
	 * <PRE>
	 * 　目的関数のPathological test 関数の計算を実行します。
	 *	 -\sum^{n-1}_{i=1}( \dfrac{\sin^{2}(\sqrt(x^{2}_{i+1}+100x^{2}_{i})-0.5)}{(0.001(x^{2}_{i+1}-2x_{i+1}x_{i}+x^{2}_{i} + 1))} + 0.5)
	 * 　大域的最適解 -100 \leq X_{i} \leq 100
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 1.0
	 */
	double lfPathologicalTest( double[] plfArgs )
	{
		double lfRes = 0.0;
		double lfDist = 0.0;
		double lfDist2 = 0.0;
		double lfSin = 0.0;
		int i;

		for( i = 0;i < plfArgs.length-1; i++ )
		{
			lfDist = 100*plfArgs[i]*plfArgs[i]+plfArgs[i+1]*plfArgs[i+1];
			lfSin = Math.sin( Math.sqrt(lfDist) - 0.5 );
			lfDist = (0.001*plfArgs[i+1]*plfArgs[i+1]-2.0*plfArgs[i+1]*plfArgs[i]+plfArgs[i]*plfArgs[i]+1.0);
			lfRes += (lfSin*lfSin)/(lfDist*lfDist) + 0.5;
		}
		return -lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のMaster's cosine wave 関数の計算を実行します。
	 *	 \sum_{i=1}^{n-1}\exp(\dfrac{1}{8}(x_{i+1}^{2}+0.5x_{i}x_{i+1}+x_{I}^{2})\cos(4\sqrt(x_{i+1}^{2}+0.5x_{i}x_{i+1}+x_{I}^{2}))
	 * 　大域的最適解 -5 \leq X_{1}, X_{2} \leq 5
	 *   f(x)=-50, x= -200
	 *   ver 0.1 初期バージョン
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2016/9/28
	 * @version 0.1
	 */
	double lfMasterCosineWave( double[] plfArgs )
	{
		double lfRes = 0.0;
		double lfDist = 0.0;
		double lfProduct = 0.0;
		int i;

		for( i = 1;i < plfArgs.length-1; i++ )
		{
			lfDist = plfArgs[i+1]*plfArgs[i+1]+plfArgs[i]*plfArgs[i];
			lfProduct = 0.5*plfArgs[i]*plfArgs[i+1];
			lfRes += Math.exp( 0.125*( lfDist+lfProduct ) )*Math.cos( 4.0*Math.sqrt( lfDist+lfProduct ) );
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のKeane's 関数の計算を実行します。
	 *	 -(x_{2}-47)\sin{sqrt(x_{2}+dfrac{x_{1}}{2}+47))-x_{1}\sin{\sqrt{\abs{x_{1}-(x_{2}+47)}}}
	 * 　大域的最適解 0 \leq X_{1}, X_{2} 10
	 *   f(x)=-50, x= -200
	 *   ver 0.1 初期バージョン
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2016/9/28
	 * @version 0.1
	 */
	double lfKeane( double[] plfArgs )
	{
		double lfRes1 = 0.0;
		double lfRes2 = 1.0;
		double lfRes3 = 0.0;
		double lfCos,lfCos2,lfCos4;
		int i;

		for( i = 1;i < plfArgs.length; i++ )
		{
			lfCos = Math.cos(plfArgs[i]);
			lfCos2 = lfCos*lfCos;
			lfCos4 = lfCos2*lfCos2;
			lfRes1 += lfCos4;
			lfRes2 *= lfCos2;
			lfRes3 += (double)i*plfArgs[i]*plfArgs[i];
		}
		return ( lfRes1-2.0*lfRes2 )/Math.sqrt( lfRes3 );
	}

	/**
	 * <PRE>
	 * 　目的関数のTrid 関数の計算を実行します。
	 *	 -(x_{2}-47)\sin{sqrt(x_{2}+dfrac{x_{1}}{2}+47))-x_{1}\sin{\sqrt{\abs{x_{1}-(x_{2}+47)}}}
	 * 　大域的最適解 -d^2 \leq X_{1}, X_{2} \leq d^2
	 *   f(x)=-50, x= -200
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2015/6/17
	 * @version 0.1
	 */
	double lfTrid( double[] plfArgs )
	{
		double lfRes = 0.0;
		double lfRes1 = 0.0;
		double lfRes2 = 0.0;
		double lfX12 = 0.0;
		int i;

		for( i = 1;i < plfArgs.length; i++ )
		{
			lfX12 = (plfArgs[i]-1);
			lfX12 *= lfX12;
			lfRes1 += lfX12;
			lfRes2 += plfArgs[i]*plfArgs[i-1];
		}
		lfRes1 += (plfArgs[0]-1)*(plfArgs[0]-1);
		lfRes = lfRes1-lfRes2;
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のk-tablet関数の計算を実行します。
	 *	 -\sum^{k}_{i=1}x_{i}^2+\sum^{n}_{i=k+1}(100x_{i})^{2}
	 * 　大域的最適解 -5.12 \leq X_{1}, X_{2} \leq 5.12 x = (0,0,0,0...,0)
	 *   k=n/4
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2016/8/24
	 * @version 0.1
	 */
	double lfkTablet( double[] plfArgs )
	{
		double lfRes = 0.0;
		double lfXX = 0.0;
		int i;
		int k;

		k = plfArgs.length/4;

		for( i = 0;i < k; i++ )
		{
			lfRes += plfArgs[i]*plfArgs[i];
		}
		for( i = k;i < plfArgs.length; i++ )
		{
			lfXX = 100.0*plfArgs[i];
			lfRes += lfXX*lfXX;
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のSchaffer関数の計算を実行します。
	 *	 -\sum^{n-1}_{i=1}(x_{i}^2+(x_{i+1})^{2})^0.25*(\sin^{2}(50(x_{i}^{2}+x_{i+1}^{2})^{0.1})+1.0)
	 * 　大域的最適解 -100 \leq X_{1}, X_{2} \leq 100 x = (0,0,0,0...,0)
	 *   ver 0.1 初版
	 *   ver 0.2 数式に誤りがあり修正 2016/10/09
	 *   ver 0.3 高速化処置 2016/10/11
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2016/9/14
	 * @version 0.3
	 */
	double lfSchaffer( double[] plfArgs )
	{
		double lfRes = 0.0;
		double lfXX = 0.0;
		double lfSquare = 0.0;
		double lfSin = 0.0;
		double lfPower = 0.0;
		double lfPower2, lfPower4, lfPower8, lfPower10;
		double[] alfXX;
		double[] alfSquare;
		double[] alfSin;
		double[] alfPower2;
		double[] alfPower4;
		double[] alfPower8;
		double[] alfPower10;
		int iGenVector_51;
		int iGenVector_52;
		int i;

		alfXX = new double[6];
		alfSquare = new double[5];
		alfSin = new double[5];
		alfPower2 = new double[5];
		alfPower4 = new double[5];
		alfPower8 = new double[5];
		alfPower10 = new double[5];

		iGenVector_51 = plfArgs.length % 5;
		iGenVector_52 = (plfArgs.length - 1) / 5;
		for (i = 0; i < iGenVector_51; i++)
		{
			lfSquare = plfArgs[i] * plfArgs[i] + plfArgs[i + 1] * plfArgs[i + 1];
			lfPower2 = Math.pow(lfSquare, 0.05);
			lfPower4 = lfPower2*lfPower2;
			lfPower8 = lfPower4*lfPower4;
			lfPower10 = lfPower2*lfPower8;
			lfSin = Math.sin(50.0*lfPower4);
			lfRes += lfPower10*(lfSin*lfSin + 1.0);
		}
		for (i = iGenVector_51; i < plfArgs.length; i+=5)
		{
			alfXX[0] = plfArgs[i] * plfArgs[i];
			alfXX[1] = plfArgs[i+1] * plfArgs[i+1];
			alfXX[2] = plfArgs[i+2] * plfArgs[i+2];
			alfXX[3] = plfArgs[i+3] * plfArgs[i+3];
			alfXX[4] = plfArgs[i+4] * plfArgs[i+4];
			alfXX[5] = plfArgs[i+5] * plfArgs[i+5];
			alfSquare[0] = alfXX[0] + alfXX[1];
			alfSquare[1] = alfXX[1] + alfXX[2];
			alfSquare[2] = alfXX[2] + alfXX[3];
			alfSquare[3] = alfXX[3] + alfXX[4];
			alfSquare[4] = alfXX[4] + alfXX[5];
			alfPower2[0] = Math.pow(alfSquare[0], 0.05);
			alfPower2[1] = Math.pow(alfSquare[1], 0.05);
			alfPower2[2] = Math.pow(alfSquare[2], 0.05);
			alfPower2[3] = Math.pow(alfSquare[3], 0.05);
			alfPower2[4] = Math.pow(alfSquare[4], 0.05);
			alfPower4[0] = alfPower2[0] * alfPower2[0];
			alfPower4[1] = alfPower2[1] * alfPower2[1];
			alfPower4[2] = alfPower2[2] * alfPower2[2];
			alfPower4[3] = alfPower2[3] * alfPower2[3];
			alfPower4[4] = alfPower2[4] * alfPower2[4];
			alfPower8[0] = alfPower4[0] * alfPower4[0];
			alfPower8[1] = alfPower4[1] * alfPower4[1];
			alfPower8[2] = alfPower4[2] * alfPower4[2];
			alfPower8[3] = alfPower4[3] * alfPower4[3];
			alfPower8[4] = alfPower4[4] * alfPower4[4];
			alfPower10[0] = alfPower8[0] * alfPower2[0];
			alfPower10[1] = alfPower8[1] * alfPower2[1];
			alfPower10[2] = alfPower8[2] * alfPower2[2];
			alfPower10[3] = alfPower8[3] * alfPower2[3];
			alfPower10[4] = alfPower8[4] * alfPower2[4];
			alfSin[0] = Math.sin(50.0*alfPower2[0]);
			alfSin[1] = Math.sin(50.0*alfPower2[1]);
			alfSin[2] = Math.sin(50.0*alfPower2[2]);
			alfSin[3] = Math.sin(50.0*alfPower2[3]);
			alfSin[4] = Math.sin(50.0*alfPower2[4]);
			lfRes += alfPower10[0]*(alfSin[0]*alfSin[0]+1.0) +
					 alfPower10[1]*(alfSin[1]*alfSin[1]+1.0) +
					 alfPower10[2]*(alfSin[2]*alfSin[2]+1.0) +
					 alfPower10[3]*(alfSin[3]*alfSin[3]+1.0) +
					 alfPower10[4]*(alfSin[4]*alfSin[4]+1.0);
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　目的関数のBohachevsky関数の計算を実行します。
	 *	 -\sum^{n-1}_{i=1}(x_{i}^2+2x_{i+1}^{2}-0.3\cos(3\pi x_{i})-0.4cos(4\pi x_{i+1} + 0.7)
	 * 　大域的最適解 -5.12 \leq x_{i} \leq 5.12 x = (0,0,0,0...,0)
	 *   ver 0.1 初版
	 *   ver 0.2 数式に誤りがあり修正 2016/10/09
	 * </PRE>
	 * @param plfArgs			引数
	 * @return 関数値
	 * @author kobayashi
	 * @since 2016/9/14
	 * @version 0.1
	 */
	double lfBohachevsky( double[] plfArgs )
	{
		double lfRes = 0.0;
		double lfXX1 = 0.0;
		double lfXX2 = 0.0;
		double lfCos1 = 0.0;
		double lfCos2 = 0.0;
		double lf3pi,lf4pi;
		int i;

		lf3pi = 3.0*Math.PI;
		lf4pi = 4.0*Math.PI;
		for( i = 0;i < plfArgs.length-1; i++ )
		{
			lfXX1 = plfArgs[i]*plfArgs[i];
			lfXX2 = 2.0*plfArgs[i+1]*plfArgs[i+1];
			lfCos1 = 0.3*Math.cos(lf3pi*plfArgs[i]);
			lfCos2 = 0.4*Math.cos(lf4pi*plfArgs[i+1]);
			lfRes += lfXX1+lfXX2-lfCos1-lfCos2+0.7;
		}
		return lfRes;
	}

	/**
	* <PRE>
	* 　目的関数のZakharov関数の計算を実行します。
	*	 -\sum^{n}_{i=1}(x_{i}^2+(sum^{n}_{i=1}(\dfrac{ix_{i}^2}{2})^{2}+(sum^{n}_{i=1}(\dfrac{ix_{i}^2}{2})^{4})
	* 　大域的最適解 -5.12 \leq x_{i} \leq 5.12 x = (0,0,0,0...,0)
	*   ver 0.1 初版
	* </PRE>
	 * @param plfArgs			引数
	* @return 関数値
	* @author kobayashi
	* @since 2016/11/09
	* @version 0.1
	*/
	double lfZakharov(double[] plfArgs )
	{
		double lfRes1 = 0.0;
		double lfRes2 = 0.0;
		double lfRes4 = 0.0;
		double lfXX1 = 0.0;
		double lfXX2 = 0.0;
		int i;

		for (i = 0; i < plfArgs.length; i++)
		{
			lfXX1 = plfArgs[i] * plfArgs[i];
			lfXX2 = plfArgs[i] * plfArgs[i]*(double)i;
			lfRes1 += lfXX1;
			lfRes2 += lfXX2;
		}
		lfRes4 = 0.25*lfRes2*lfRes2;
		lfRes4 = lfRes4*lfRes4;
		return lfRes1+lfRes2+lfRes4;
	}

	/**
	* <PRE>
	* 　目的関数のSalomon Problem関数の計算を実行します。
	*	 1-\cos(2\pi\sqrt(\sum^{n}_{i=1}(x_{i}^2)))+0.1*sqrt(\sum^{n}_{i=1}(x_{i}^2))
	* 　大域的最適解 -100 \leq x_{i} \leq 100 x = (0,0,0,0...,0)
	*   ver 0.1 初版
	* </PRE>
	* @param plfArgs			引数
	* @return 関数値
	* @author kobayashi
	* @since 2016/11/09
	* @version 0.1
	*/
	double lfSalomonProblem(double[] plfArgs )
	{
		double lfRes1 = 0.0;
		double lfRes2 = 0.0;
		double lfRes4 = 0.0;
		double lfXX1 = 0.0;
		double lfXX2 = 0.0;
		int i;

		for (i = 0; i < plfArgs.length; i++)
		{
			lfXX1 = plfArgs[i] * plfArgs[i];
			lfRes1 += lfXX1;
		}
		lfRes1 = Math.sqrt(lfRes1);
		return 1.0 - Math.cos(2.0*Math.PI*lfRes1)+0.1*lfRes1;
	}

	/**
	* <PRE>
	* 　目的関数のAlpine functionの計算を実行します。
	*	 \sum^{n}_{i=1}\abs(x_{i}*\sin(x_{i})+0.1*x_{i})
	* 　大域的最適解 -10 \leq x_{i} \leq 10 x = (0,0,0,0...,0)
	*   ver 0.1 初版
	* </PRE>
	* @param plfArgs			引数
	* @return 関数値
	* @author kobayashi
	* @since 2016/11/09
	* @version 0.1
	*/
	double lfAlpine(double[] plfArgs )
	{
		double lfRes = 0.0;
		int i;

		for (i = 0; i < plfArgs.length; i++)
		{
			lfRes += Math.abs(plfArgs[i] * Math.sin(plfArgs[i]) + 0.1*plfArgs[i]);
		}
		return lfRes;
	}

	/**
	* <PRE>
	* 　目的関数のWeierstrass functionの計算を実行します。
	*	 \sum^{n}_{i=1}\abs(x_{i}*\sin(x_{i})+0.1*x_{i})
	* 　大域的最適解 -10 \leq x_{i} \leq 10 x = (0,0,0,0...,0)
	*   ver 0.1 初版
	* </PRE>
	* @param plfArgs			引数
	* @return 関数値
	* @author kobayashi
	* @since 2016/11/09
	* @version 0.1
	*/
	double lfWeierstrass(double[] plfArgs )
	{
		double lfRes = 0.0;
		double ikMax = 20;
		double lfA = 0.5;
		double lfB = 3.0;
		double lfRes1 = 0.0;
		double lfRes2 = 0.0;
		double lf2pi = Math.PI + Math.PI;
		double lfPowA = 1.0;
		double lfPowB = 1.0;
		int i;
		int iKMax = 20;
		double[] alfA;	// 配列サイズ20
		double[] alfB;	// 配列サイズ20

		for (i = 0; i < ikMax; i++)
		{
			lfPowA *= lfA;
			lfPowB *= lfB;
			lfRes1 += lfPowA*Math.cos(lf2pi*lfPowB*(plfArgs[i] + 0.5));
			lfRes2 += lfPowA*Math.cos(lf2pi*lfPowB*0.5);
			lfRes += lfRes1 - plfArgs.length*lfRes2;
		}
		return lfRes;
	}

	/**
	* <PRE>
	* 　目的関数のLevy functionの計算を実行します。
	*	 \sin^{2}(\pi\omega_{1})+\sum^{d-1}_{i=1}(\omega-1)^{2}[1+10\sin^{2}(\pi\oemga_{i}+1)]+(\oemga_{d}-1)^{2}[1+\sin^{2}(2\pi\omega_{d})]
	*    \omega_{i} = 1 + \dfrac{x_{i}-1}{4}
	* 　大域的最適解 -10 \leq x_{i} \leq 10 x = (1,1,1,1...,1)
	*   ver 0.1 初版
	* </PRE>
	* @param plfX			引数
	* @return 関数値
	* @author kobayashi
	* @since 2017/02/28
	* @version 0.1
	*/
	double lfLevy(double[] plfX)
	{
		int i;
		double lfRes = 0.0;
		double lfOmega;
		double lfOmega2;
		double lfSin21;
		double lfSin22;
		double lfSin20;

		lfOmega = 1.0 + (plfX[0] - 1.0)*0.25;
		lfSin20 = Math.sin(Math.PI*lfOmega);
		lfSin20 *= lfSin20;
		for (i = 0; i < plfX.length-1; i++)
		{
			lfOmega = 1.0 + (plfX[i] - 1.0)*0.25;
			lfOmega2 = (lfOmega - 1.0);
			lfOmega2 *= lfOmega2;
			lfSin21 = Math.sin(Math.PI*lfOmega + 1.0);
			lfSin21 *= lfSin21;
			lfRes += lfOmega2*(1.0+10.0*lfSin21);
		}
		lfOmega = 1.0 + (plfX[plfX.length-1] - 1.0)*0.25;
		lfOmega2 = (lfOmega - 1.0);
		lfOmega2 *= lfOmega2;
		lfSin22 = Math.sin(2.0*Math.PI*lfOmega);
		lfSin22 *= lfSin22;

		return lfSin20 + lfRes + lfSin22;
	}

	/**
	* <PRE>
	* 　目的関数のBukin functionの計算を実行します。
	*	 f(x) = 100\sqrt{x_{2}-0.01x^{2}_{1}}+0.01|x_{1}+10|
	* 　大域的最適解  f_{x} = 0 x = (10,1)
	*   ver 0.1 初版
	* </PRE>
	* @param plfX			引数
	* @return 関数値
	* @author kobayashi
	* @since 2017/02/28
	* @version 0.1
	*/
	double lfBukin(double plfX[] )
	{
		return 100.0 * Math.sqrt(Math.abs(plfX[1] - 0.01*plfX[0]*plfX[0])) + 0.01*Math.abs(plfX[0] + 10.0);
	}

	/**
	* <PRE>
	* 　目的関数のGramacy &amp; Lee functionの計算を実行します。
	*	 f(x) = \dfrac{\sin(10\pi x)}{2x}+(x-1)^{4}
	* 　大域的最適解  f_{x} = 0 x = 0.5 or 2.5
	*   ver 0.1 初版
	* </PRE>
	* @param plfX			引数
	* @author kobayashi
	* @return 関数値
	* @since 2017/02/28
	* @version 0.1
	*/
	double lfGramacyLee(double[] plfX)
	{
		return Math.sin(10*Math.PI*plfX[0])/plfX[0]*0.5+(plfX[0]-1.0)*(plfX[0] - 1.0)*(plfX[0] - 1.0)*(plfX[0] - 1.0);
	}

	/**
	* <PRE>
	* 　目的関数のMcCormick functionの計算を実行します。
	*	 f(x) = \sin(x_{1}+x_{2})+(x_{1}-x_{2})^{2}-1.5x_{1}+2.5x_{2}+1
	* 　大域的最適解  -1.5 &lt;= x_{1} &lt;= 4, -3 &lt;= x_{2} &lt;= 4 f_{x} = -1.9133 x = (-0.54719, -1.54719)
	*   ver 0.1 初版
	* </PRE>
	* @param plfX			引数
	* @author kobayashi
	* @since 2017/03/09
	* @return 関数値
	* @version 0.1
	*/
	double lfMccormick(double[] plfX)
	{
		return Math.sin(plfX[0]+plfX[1])+(plfX[0]-plfX[1])*(plfX[0] - plfX[1])-1.5*plfX[0]+2.5*plfX[1]+1;
	}

	/**
	 * <PRE>
	 *   逆シミュレーション用の評価指標
	 *   救急部門の混雑状況を表したNEDOCS(National emergency department overcrowding study)指標を使用。
	 * </PRE>
	 * @param plfArgs	引数
	 * @return	NEDOCS評価値
	 */
	private double EvaluationIndicatorInvSimNedocs( double[] plfArgs )
	{
		int i;
		double lfEdBeds = 0;
		double lfHospitalBeds = 0;
		double lfTotalPatients = 0;
		double lfEdPatients = 0;
		double lfVentilators = 0;
		double lfLongestAdmit = 0.0;
		double lfLastBedTime = 0.0;
		double lfRes = 0.0;

		lfEdBeds		= plfArgs[0]+plfArgs[2]+plfArgs[5]*plfArgs[24]+plfArgs[6]*plfArgs[26];
		lfHospitalBeds	= plfArgs[5]*plfArgs[24]+plfArgs[6]*plfArgs[26]+plfArgs[7]*plfArgs[28];
		lfTotalPatients	= plfArgs[35];
		lfEdPatients	= plfArgs[36];
		lfVentilators	= plfArgs[37];
		lfLongestAdmit	= plfArgs[38];
		lfLastBedTime	= plfArgs[39];
		// �f�@���A���Î��A��p���ɐl�����Ȃ��󋵂��Z�o�����ꍇ�͋ɒ[�ȏꍇ���V�~�����[�V�������ʂƂ��ďo�Ă���̂ŁA
		// ���̏ꍇ��NEDOCS�l���ɒ[�ɍ������A�̗p����Ȃ��悤�ɂ��܂��B
		lfRes = -20.0 + 85.8*(lfTotalPatients / lfEdBeds) + 600.0*(lfEdPatients / lfHospitalBeds) + 13.4*lfVentilators + 0.93*lfLongestAdmit + 5.64*lfLastBedTime;
		lfRes = lfRes < 0.0 ? 10000.0 : lfRes;
		return lfRes;
	}

	/**
	 * <PRE>
	 *   逆シミュレーション用の評価指標
	 *   救急部門の仕事量を表したED work Score指標を使用。
	 * </PRE>
	 * @param plfArgs	引数
	 * @return			ED Work Score 評価値
	 */
	private double EvaluationIndicatorInvSimEdWorkScore( double[] plfArgs )
	{
		int i;
		int iTotalEdBeds = 0;
		int iHospitalBeds = 0;
		int iNurseNum = 0;
		double lfWaitingRoomPatients = 0;
		double lfEdPatients = 0;
		double lfTriageResult = 0;
		double lfRes = 0.0;

		lfWaitingRoomPatients	= plfArgs[40];
		iTotalEdBeds			= (int)(plfArgs[0]+plfArgs[2]+plfArgs[5]*plfArgs[24]+plfArgs[6]*plfArgs[26]);
		iHospitalBeds			= (int)(plfArgs[5]*plfArgs[24]+plfArgs[6]*plfArgs[26]+plfArgs[7]*plfArgs[28]);
		lfTriageResult 			= plfArgs[41]+plfArgs[42]*2+plfArgs[43]*3+plfArgs[44]*4+plfArgs[45]*5;
		lfEdPatients			= plfArgs[36];
		iNurseNum				= (int)(plfArgs[0]*plfArgs[15]+plfArgs[2]*plfArgs[19]);
//		if( lfEdPatients < 1.0 )
//		{
//			lfRes = 1000000;
//		}
//		else
		{
			lfRes = 3.23*( lfWaitingRoomPatients/iTotalEdBeds )+0.097*( lfTriageResult/iNurseNum )+10.92*lfEdPatients/iTotalEdBeds;
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 *   逆シミュレーション用の評価指標
	 *   救急部門の混雑状況を表したED work Index指標を使用。
	 * </PRE>
	 * @param plfArgs	引数
	 * @return			EDWIN評価値
	 */
	private double EvaluationIndicatorInvSimEdWin( double[] plfArgs )
	{
		int i;
		int iTotalEdBeds = 0;
		int iHospitalBeds = 0;
		int iDoctorNurseNum = 0;
		double lfWaitingRoomPatients = 0;
		double lfEdPatients = 0;
		double lfTriageResult = 0;
		double lfRes = 0.0;

		lfWaitingRoomPatients	= plfArgs[40];
		iTotalEdBeds			= (int)(plfArgs[2]+plfArgs[5]*plfArgs[24]+plfArgs[6]*plfArgs[26]);
		iHospitalBeds			= (int)(plfArgs[5]*plfArgs[24]+plfArgs[6]*plfArgs[26]+plfArgs[7]*plfArgs[28]);
		lfTriageResult 			= plfArgs[41]+plfArgs[42]*2+plfArgs[43]*3+plfArgs[44]*4+plfArgs[45]*5;
		lfEdPatients			= plfArgs[36];
		iDoctorNurseNum			= (int)(plfArgs[0]*plfArgs[14]+plfArgs[2]*plfArgs[18]);
//		System.out.println("lfWaitingRoomPatients = " + plfArgs[40]);
//		System.out.println("iTotalEdBeds = " + iTotalEdBeds);
//		System.out.println("iHospitalBeds = " + iHospitalBeds);
//		System.out.println("lfTriageResult = " + lfTriageResult);
//		System.out.println("lfEdPatient = " + plfArgs[36]);
//		System.out.println(lfTriageResult/(iDoctorNurseNum*(iTotalEdBeds-lfEdPatients)));
		// 診察室、初療室、手術室に人がいない状況が算出される場合は極端な場合がシミュレーション結果として出ているので、
		// この場合はNEDOCS値を極端に高くし、採用されないようにします。
//		if( lfEdPatients < 1.0 )
//		{
//			lfRes = 1000000;
//		}
//		else
		{
			lfRes = lfTriageResult/(iDoctorNurseNum*(iTotalEdBeds-lfEdPatients));
		}
		return lfRes;
	}
}

