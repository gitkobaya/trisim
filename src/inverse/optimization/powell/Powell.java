package inverse.optimization.powell;

import inverse.optimization.objectivefunction.ObjectiveFunctionInterface;

/**
 *   最適化計算アルゴリズムの一つであるPowellの方法を実行するアルゴリズム。
 *   ここで実装したものはニューメリカルレシピインシーに記載されている変形Powell法を実装。
 *   具体的にはPowell ABC法を実装するために作成したが、論文と結果が合わず実装のみ。
 *   これを単独で使用することも可。
 *
 * @author kobayashi
 *
 */
public class Powell
{
	private	double[] plfXt;
	private	double[] plfXCom;
	private	double[] plfXiCom;
	private	double[] plfPt;
	private	double[] plfXit;
	private	double[] plfPtt;

	private	int iVectorLen;
	private	int iNCom;
	private	final double lfGolden = 1.68034;	// ステップごとの区間の拡大率
	private	final double lfGLimit = 100.0;		// 放物線あてはめのステップの最大拡大率
	private	final double lfTiny = 1.0e-20;		// 許容誤差(囲い込み法で使用。)
	private	final double lfCGolden = 0.3819660;	// 黄金分割比
	private	final int iIntervalNumber = 100;	// 反復回数
	private	final double lfZepsilon = 1.0e-10;	// 許容誤差(Brentの方法で使用)。
	private	final double lfTol = 2.0e-4;		// 許容誤差(Powell法で使用
	private	final int iItmax = 200;
	private ObjectiveFunctionInterface pflfObjectiveFunction;

	public Powell()
	{
	}

	public Powell( int iVectorDimNum )
	{
		vInitialize(iVectorDimNum);
	}

	public void vInitialize( int iVectorDimNum )
	{
		int i;
		iVectorLen = iVectorDimNum;
		plfXt = new double[iVectorLen];
		plfXCom = new double[iVectorLen];
		plfXiCom = new double[iVectorLen];
		plfPt = new double[iVectorLen];
		plfXit = new double[iVectorLen];
		plfPtt = new double[iVectorLen];

		for (i = 0; i < iVectorLen; i++)
		{
			plfXt[i] = 0.0;
			plfXCom[i] = 0.0;
			plfXiCom[i] = 0.0;
			plfPt[i] = 0.0;
			plfXit[i] = 0.0;
			plfPtt[i] = 0.0;
		}
	}

	public void vTerminate()
	{
		plfXt = null;
		plfXCom = null;
		plfXiCom = null;
		plfPt = null;
		plfXit = null;
		plfPtt = null;
	}

	/**
	* <PRE>
	* 　目的関数をインストールします。
	* 　実際にはコールバック関数をインストールします。
	* </PRE>
	* @param pflfFunction 目的関数の関数ポインタ
	* @author kobayashi
	* @since 2015/7/28
	* @version 0.1
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
	* @since 2015/7/28
	* @version 0.1
	*/
	public void vReleaseCallConstraintFunction()
	{
		pflfObjectiveFunction = null;
	}

	double lfF1Dim( double lfShift )
	{
		int i;
		for (i = 0; i < iNCom; i++)
			plfXt[i] = plfXCom[i] + lfShift*plfXiCom[i];
		return pflfObjectiveFunction.lfObjectiveFunction(plfXt);
	}

	/**
	 * <PRE>
	 *    囲い込み法（Numerical Recipe in Cを参照。）
	 *    ver 0.1 2016/11/02 初版
	 * </PRE>
	 * @param plfAx		パラメータ1
	 * @param plfBx		パラメータ2
	 * @param plfCx		パラメータ3
	 * @param plfFa		パラメータ4
	 * @param plfFb		パラメータ5
	 * @param plfFc		パラメータ6
	 * @since 2016/11/02
	 * @version 0.1
	 */
	public void vMnbrak( double[] plfAx, double[] plfBx, double[] plfCx, double[] plfFa, double[] plfFb, double[] plfFc )
	{
		double lfUlim, lfU, lfR, lfQ, lfFu, lfDum;

		plfFa[0] = lfF1Dim( plfAx[0] );
		plfFb[0] = lfF1Dim( plfBx[0] );

		if ( plfFb[0] > plfFa[0] )
		{
			lfDum = plfAx[0];	plfAx[0] = plfBx[0]; plfBx[0] = lfDum;
			lfDum = plfFb[0];	plfFb[0] = plfFa[0]; plfFa[0] = lfDum;
		}
		// Cの初期設定
		plfCx[0] = plfBx[0] + lfGolden*(plfBx[0]-plfAx[0]);
		plfFc[0] = lfF1Dim( plfCx[0] );

	// 囲い込みに成功するまで繰り返す。
	while( plfFb[0] > plfFc[0] )
	{
		// a, b, cから放物線補外でuを求める。
		lfR = (plfBx[0] - plfAx[0])*(plfFb[0]-plfFc[0]);
		lfQ = (plfBx[0] - plfCx[0])*(plfFb[0]-plfFa[0]);
		lfU = plfBx[0] - ((plfBx[0]-plfCx[0])*lfQ-(plfBx[0]-plfAx[0])*lfR)/(2.0*lfSgn( lfMax(Math.abs(lfQ-lfR), lfTiny), lfQ-lfR ) );
		lfUlim = plfBx[0] + lfGLimit*( plfCx[0]-plfBx[0] );

		// これ以上進まずに様々な可能性を調べる。
			if( (plfBx[0]-lfU)*(lfU-plfCx[0]) > 0.0 )
			{
				lfFu = lfF1Dim( lfU );
				if( lfFu < plfFc[0] )
				{
					plfAx[0] = plfBx[0];
					plfBx[0] = lfU;
					plfFa[0] = plfFb[0];
					plfFb[0] = lfFu;
					return;
				}
				else if( lfFu > plfFb[0] )
				{
					plfCx[0] = lfU;
					plfFc[0] = lfFu;
					return;
				}
				lfU = plfCx[0] + lfGolden*(plfCx[0]-plfBx[0]);
				lfFu = lfF1Dim( lfU );
			}
			else if( (plfCx[0]-lfU)*(lfU-lfUlim) > 0.0 )
			{
				lfFu = lfF1Dim( lfU );
				if( lfFu < plfFc[0] )
				{
					plfBx[0] = plfCx[0]; plfCx[0] = lfU; lfU = plfCx[0] + lfGolden*( plfCx[0]-plfBx[0] );
					plfFb[0] = plfFc[0]; plfFc[0] = lfFu; lfFu = lfF1Dim( lfU );
				}
			}
			else if( (lfU-lfUlim)*(lfUlim-plfCx[0]) >= 0.0 )
			{
				lfU = lfUlim;
				lfFu = lfF1Dim( lfU );
			}
			else
			{
				lfU = plfCx[0] + lfGolden*(plfCx[0]-plfBx[0]);
				lfFu = lfF1Dim( lfU );
			}
			plfAx[0] = plfBx[0]; plfBx[0] = plfCx[0]; plfCx[0] = lfU;
			plfFa[0] = plfFb[0]; plfFb[0] = plfFc[0]; plfFc[0] = lfFu;
		}
	}

	/**
	 * <PRE>
	 *    Brentの方法
	 * </PRE>
	 * @param lfAx		パラメータ1
	 * @param lfBx		パラメータ2
	 * @param lfCx		パラメータ3
	 * @param lfTol		パラメータ4
	 * @param plfXMin	パラメータ5
	 * @return 関数値
	 */
	double lfBrent( double lfAx, double lfBx, double lfCx, double lfTol, double[] plfXMin )
	{
		int iInter;
		double lfA, lfB, lfD, lfETemp, lfFu, lfFv, lfFw, lfFx, lfP, lfQ, lfR, lfTol1, lfTol2, lfU, lfV, lfW, lfX, lfXm;
		double lfE = 0.0;

		// a < bにする。
		lfA = ( lfAx < lfCx ? lfAx : lfCx);
		lfB = ( lfAx > lfCx ? lfAx : lfCx);
		lfD = 0.0;

	// 初期化
		lfX = lfW = lfV = lfBx;

		lfFw = lfFv = lfFx = lfF1Dim(lfX);

		for(iInter = 1; iInter <= iIntervalNumber; iInter++)
		{
			lfXm = 0.5*(lfA+lfB);
			lfTol2 = 2.0*(lfTol1 = lfTol*Math.abs(lfX)+lfZepsilon);
			if( Math.abs(lfX-lfXm) <= ( lfTol2-0.5*(lfB-lfA)))
			{
				plfXMin[0] = lfX;
				return lfFx;
			}
			if( Math.abs( lfE ) > lfTol1 )
			{
				lfR = ( lfX-lfW)*(lfFx-lfFv);
				lfQ = ( lfX-lfV)*(lfFx-lfFw);
				lfP = ( lfX-lfV )*lfQ-(lfX-lfW)*lfR;
				lfQ = 2.0*(lfQ-lfR);
				if( lfQ > 0.0 ) lfP = -lfP;
				lfQ = Math.abs( lfQ );
				lfETemp = lfE;
				lfE = lfD;
				if( Math.abs( lfP ) >= Math.abs(0.5*lfQ*lfETemp) || lfP <= lfQ*(lfA-lfX) || lfP >= lfQ*(lfB-lfX))
				{
					lfD = lfCGolden*(lfE =(lfX >= lfXm ? lfA-lfX : lfB-lfX));
				}
				else
				{
					lfD = lfP/lfQ;
					lfU = lfX+lfD;
					if( lfU-lfA < lfTol2 || lfB-lfU < lfTol2 )
						lfD = lfSgn( lfTol1, lfXm-lfX );
				}
			}
			else
			{
				lfD = lfCGolden*(lfE=(lfX >= lfXm ? lfA-lfX : lfB-lfX));
			}
			lfU = (Math.abs(lfD) >= lfTol1 ? lfX+lfD : lfX+lfSgn(lfTol1,lfD));
			lfFu = lfF1Dim(lfU);
			if( lfFu <= lfFx )
			{
				if( lfU >= lfX ) lfA = lfX;
				else		 lfB = lfX;
				lfV = lfW; lfW = lfX; lfX = lfU;
				lfFv = lfFw; lfFw = lfFx; lfFx = lfFu;
			}
			else
			{
				if( lfU < lfX ) lfA = lfU;
				else		lfB = lfU;
				if( lfFu <= lfFw || lfW == lfX )
				{
					lfV = lfW; lfW = lfU;
					lfFv = lfFw; lfFw = lfFu;
				}
				else if( lfFu <= lfFv || lfV == lfX || lfV == lfW )
				{
					lfV = lfU; lfFv = lfFu;
				}
			}
		}
		plfXMin[0] = lfX;
		return lfFx;
	}

	/**
	 * <PRE>
	 *   n次元の点p[1,...n]とn次元の方向xi[1,...n]を与えると, このルーチンはpを方向xiに沿って動かして,
	 *   関数func(p)が最小になるようにする. xiはpが実際に動いた変位量で上書きされる. plfRetには最小店pでの関数値が入る.
	 *   実際の仕事はルーチンmnbrak, brentを呼び出して行う.
	 * </PRE>
	 *
	 * @param plfP		n次元の点
	 * @param plfXi		n次元の方向ベクトル
	 * @param iN		n次元の点の個数を指定
	 * @param plfRet	n次元の結果
	 */
	public void vLineMin(double[] plfP, double[] plfXi, int iN, double[] plfRet )
	{
		int i;
		double[] lfXX, lfXMin, lfFx, lfFb, lfFa, lfBx, lfAx;

		lfXX = new double[1];
		lfXMin = new double[1];
		lfFx = new double[1];
		lfFb = new double[1];
		lfFa = new double[1];
		lfBx = new double[1];
		lfAx = new double[1];

		iNCom = iN;
		for(i = 0; i < iN; i++ )
		{
			plfXCom[i] = plfP[i];
			plfXiCom[i] = plfXi[i];
		}
		lfAx[0] = 0.0;
		lfXX[0] = 1.0;
		vMnbrak( lfAx, lfXX, lfBx, lfFa, lfFx, lfFb );
		plfRet[0] = lfBrent( lfAx[0], lfXX[0], lfBx[0], lfTol, lfXMin );
		for( i = 0;i < iN; i++ )
		{
			plfXi[i] = plfXi[i]*lfXMin[0];
			plfP[i] += plfXi[i];
		}
	}

	public void vPowell( double[] plfP, double[][] pplfXi, int iN, double lfFtol, int[] piInter, double[] plfFRet )
	{
		int i, j, iBig;
		double lfDel, lfFp, lfFptt, lfT;

		plfFRet[0] = pflfObjectiveFunction.lfObjectiveFunction( plfP );
		for (i = 0; i < iN; i++)
		{
			plfPt[i] = plfP[i];
		}
		for ( piInter[0] = 1;; ++piInter[0])
		{
			lfFp = plfFRet[0];
			iBig = 0;
			// 関数値の最大減少量を求める変数
		lfDel = 0.0;
		// 各反復で、方向集合の全要素についてループ
		for (i = 0; i < iN; i++)
		{
			// 方向をコピー
			for (j = 0; j < iN; j++)
			{
				plfXit[j] = pplfXi[j][i];
			}
			lfFptt = plfFRet[0];
			// それに沿って最小化
			vLineMin(plfP, plfXit, iN, plfFRet);
			// 最大の現象であれば記録する。
			if (Math.abs(lfFptt - plfFRet[0]) > lfDel)
			{
				lfDel = Math.abs(lfFptt - plfFRet[0]);
				iBig = i;
			}
		}
		// 終了判定
			if (2.0*Math.abs(lfFp - plfFRet[0]) <= lfFtol*(Math.abs(lfFp) + Math.abs(plfFRet[0])))
			{
				return;
			}
			if ( piInter[0] == iItmax) break;
			for (j = 0; j < iN; j++)
			{
				plfPtt[j] = 2.0*plfP[j] - plfPt[j];
				plfXit[j] = plfP[j] - plfPt[j];
				plfPt[j] = plfP[j];
			}
			lfFptt = pflfObjectiveFunction.lfObjectiveFunction( plfPtt );
			if (lfFptt < lfFp)
			{
				lfT = 2.0*( lfFp-2.0*plfFRet[0]+lfFptt)*( lfFp-plfFRet[0]-lfDel )*(lfFp - plfFRet[0] - lfDel) -lfDel*(lfFp-lfFptt)*(lfFp - lfFptt);
				if (lfT < 0.0)
				{
					vLineMin( plfP, plfXit, iN, plfFRet );
					for (j = 0; j < iN; j++)
					{
						pplfXi[j][iBig] = pplfXi[j][iN-1];
						pplfXi[j][iN-1] = plfXit[j];
					}
				}
			}
		}
	}

	double lfSgn(double lfA, double lfB)
	{
		return ( lfB >= 0.0 ? Math.abs(lfA) : -Math.abs(lfA) );
	}

	double lfMax(double lfA, double lfB)
	{
		return (lfA >= lfB ? lfA : lfB);
	}
}
