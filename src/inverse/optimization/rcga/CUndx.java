package inverse.optimization.rcga;

import inverse.optimization.rankt.Rank_t;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import utility.sfmt.Sfmt;

/**
*    実数値遺伝的アルゴリズムのMGG+UNDXを扱うクラスです。
*    UNDXのアルゴリズムの詳細は論文を参照してください。
*    MGGアルゴリズムに関しましても同様に論文を参照してください。
*    初期化    vInitialize()
*    実行      vImplement()
*    終了処理  vTerminate()
*    という流れで使用します。
*
* @author kobayashi
*
*/
public class CUndx extends CRealCodedGa
{
	private	int iCrossOverNum;
	private	int iChildrenNumber;
	private	double lfAlpha;
	private	double lfBeta;
	private	int iParentOutputFlag;
	private	int iChildrenOutputFlag;
	private	double[][] pplfChildren;
	private	double[] plfChild1;
	private	double[] plfChild2;
	private	int[] piParentLoc;
	private	int iBestLoc;
	private int iParent1Loc;
	private int iParent2Loc;
	private int iParent3Loc;
	private int iSelectParentFlag;
	private int i1stLoc;
	private int i2ndLoc;
//	private ObjectiveFunctionInterface pflfObjectiveFunction;

	public CUndx()
	{
		iCrossOverNum = 0;
		lfAlpha = 0.0;
		lfBeta = 0.0;
		iParentOutputFlag = 0;
		iChildrenOutputFlag = 0;
		iChildrenNumber = 0;
		pplfChildren = null;
		plfChild1 = null;
		plfChild2 = null;
		piParentLoc = null;
		i1stLoc = 0;
		i2ndLoc = 0;
	}

	/**
	 * <PRE>
	 *   MGG+UNDXの初期化を行います。
	 * </PRE>
	 * @param iGenerationNum    世代数
	 * @param iGenNum           遺伝子数
	 * @param iGenVectorData    次元数
	 * @param iCrossOverNumData 交叉回数
	 */
	public void vInitialize( int iGenerationNum, int iGenNum, int iGenVectorData, int iCrossOverNumData )
	{
		int i,j;

		// 実数値GAの初期化を実行します。
		vInitialize( iGenerationNum, iGenNum, iGenVectorData );

		// 交叉回数を設定します。
		iCrossOverNum = iCrossOverNumData;

		// 最大で作成される子供の個数を設定します。
		iChildrenNumber = iCrossOverNum * 2 + 2;
		// 子供のデータを作成します。
		pplfChildren = new double[iChildrenNumber][iGenVector];
		for( i = 0;i < iChildrenNumber; i++ )
		{
			for( j = 0;j < iGenVector; j++ )
			{
				pplfChildren[i][j] = 0.0;
			}
		}

		plfChild1 = new double[iGenVector];
		plfChild2 = new double[iGenVector];
		for( i = 0;i < iGenVector; i++ )
		{
			plfChild1[i] = 0.0;
			plfChild2[i] = 0.0;
		}
		piParentLoc = new int[iGenNumber];
		for (i = 0; i < iGenNum; i++)
		{
			piParentLoc[i] = i;
		}
		long seed;
		seed = System.currentTimeMillis();
		rnd = new Sfmt( (int)seed );
	}

	/**
	 * <PRE>
	 *   終了処理を行います。
	 * </PRE>
	 */
	public void vTerminate()
	{
		int i;

		// 継承元クラスに属するメンバ変数の終了処理を実行します。
		vTerminate();

		// 終了処理を実行します。
		pplfChildren = null;
		plfChild1 = null;
		plfChild2 = null;
		piParentLoc = null;
	}

	/**
	 * <PRE>
	 *   MGG+UNDXを実行します。
	 * </PRE>
	 */
	public void vImplement()
	{
		int i,j;
		int[] i1,i2,i3;
		int[] i1stGenLoc;
		int[] i2ndGenLoc;
		double[][] plfParent1 = null;
		double[][] plfParent2 = null;
		double[][] plfParent3 = null;

		double lfPrevProb = 0.0;
		double lfProb = 0.0;

		plfParent1 = new double[1][iGenVector];
		plfParent2 = new double[1][iGenVector];
		plfParent3 = new double[1][iGenVector];
		i1 = new int[1];
		i2 = new int[1];
		i3 = new int[1];
		i1stGenLoc = new int[1];
		i2ndGenLoc = new int[1];

		for( i = 0;i < iCrossOverNum; i++ )
		{
			// UNDXの2つの親を選択します。
			vSelectParent( plfParent1, plfParent2, plfParent3, i1, i2, i3 );

			// ここで、フラグが有効になっている場合に限り親を出力します。
			vOutputCurrentParent( plfParent1[0], plfParent2[0] );

			// UNDXを実行します。
			vUndx( plfParent1[0], plfParent2[0], plfParent3[0], lfAlpha, lfBeta, plfChild1, plfChild2 );

			// 生成した子供を追加します。
			for( j = 0;j < iGenVector; j++ )
			{
				pplfChildren[2*i][j] = plfChild1[j];
				pplfChildren[2*i+1][j] = plfChild2[j];
			}
			// 最後に親もこの集団に追加します。
			for( j = 0;j < iGenVector; j++ )
			{
				pplfChildren[2*iCrossOverNum][j] = plfParent1[0][j];
				pplfChildren[2*iCrossOverNum+1][j] = plfParent2[0][j];
			}

			// ここで、フラグが有効になっている場合に限り子を出力します。
			vOutputCurrentChildren( pplfChildren );

		// 世代交代を実施します。2つのよい遺伝子を選択します

			// 最も評価値のよい遺伝子と、ルーレット選択により決定した遺伝子を選択します。
			vSelectGens( pplfChildren, i1stGenLoc, i2ndGenLoc );

			// 今回選択した親と子を交換します。
			for( j = 0;j < iGenVector; j++ )
			{
				pplfGens[i1[0]][j] = pplfChildren[i1stGenLoc[0]][j];
				pplfGens[i2[0]][j] = pplfChildren[i2ndGenLoc[0]][j];
			}
			// 現在の最良値の番号を取得します。
			i1stLoc = i1[0];
			i2ndLoc = i2[0];
			// 一時的に保持していた子の集合を削除します。
			for(i = 0;i < iChildrenNumber; i++ )
			{
				for( j= 0;j < iGenVector;j++ )
				{
					pplfChildren[i][j] = 0.0;
				}
			}
		}
	}

	/**
	 * <PRE>
	 *    UNDX用の親を選択します。3体選択します。
	 * </PRE>
	 * @param pplfParent1	第一親
	 * @param pplfParent2	第二親
	 * @param pplfParent3	第三親
	 * @param piLoc1		全遺伝子の中での第一親の番号
	 * @param piLoc2		全遺伝子の中での第二親の番号
	 * @param piLoc3		全遺伝子の中での第三親の番号
	 */
	private void vSelectParent( double[][] pplfParent1, double[][] pplfParent2, double[][] pplfParent3, int[] piLoc1, int[] piLoc2, int[] piLoc3 )
	{
		int i,j;
		double lfSumAbs1, lfSumAbs2, lfSumAbs3;
		int i1, i2, i3;
		int iLoc;
		int iTemp;
		int iFlag = 0;

		if( iSelectParentFlag == 0 )
		{
			// UNDXを行う親を2つ決定します。
			// 親をランダムにNp個選択します。
			for (i = iGenNumber - 1; i > 0; i--)
			{
				iLoc = (int)((i + 1)*rnd.NextUnif());
				iTemp = piParentLoc[i];
				piParentLoc[i] = piParentLoc[iLoc];
				piParentLoc[iLoc] = iTemp;
			}
			lfSumAbs1 = lfSumAbs2 = lfSumAbs3 = 0.0;
			i1 = piParentLoc[0];
			i2 = piParentLoc[1];
			i3 = piParentLoc[2];
			for (i = 1; i < iGenNumber-1; i++)
			{
				lfSumAbs1 = lfSumAbs2 = lfSumAbs3 = 0.0;
				if( iFlag == 1 )
				{
					i2 = piParentLoc[i - 1];
					i3 = piParentLoc[i];
				}
				else if( iFlag == 2 ) i2 = piParentLoc[i];
				else if (iFlag == 3 ) i3 = piParentLoc[i];
				else if (iFlag == 4 ) i3 = piParentLoc[i];
				else if( iFlag == 5 ) break;

				for (j = 0; j < iGenVector; j++)
				{
					lfSumAbs1 += Math.abs(pplfGens[i1][j] - pplfGens[i2][j]);
					lfSumAbs2 += Math.abs(pplfGens[i1][j] - pplfGens[i3][j]);
					lfSumAbs3 += Math.abs(pplfGens[i2][j] - pplfGens[i3][j]);
				}
				// i1 = i2 = i3の場合（親がすべて等しい場合）
				if (lfSumAbs1 <= 0.000000000001 && lfSumAbs2 <= 0.000000000001) iFlag = 1;
				// i1 = i2の場合（1つ目と2つ目の親が等しい場合）
				else if (lfSumAbs1 <= 0.000000000001) iFlag = 2;
				// i1 = i3の場合（1つ目と3つ目の親が等しい場合）
				else if (lfSumAbs2 <= 0.000000000001) iFlag = 3;
				// i2 = i3の場合（2つ目と3つ目の親が等しい場合）
				else if (lfSumAbs3 <= 0.000000000001) iFlag = 4;
				else iFlag = 5;
			}
			pplfParent1[0] = pplfGens[i1];
			pplfParent2[0] = pplfGens[i2];
			pplfParent3[0] = pplfGens[i3];
			piLoc1[0] = i1;
			piLoc2[0] = i2;
			piLoc3[0] = i3;
		}
		else
		{
			pplfParent1[0] = pplfGens[iParent1Loc];
			pplfParent2[0] = pplfGens[iParent2Loc];
			pplfParent3[0] = pplfGens[iParent3Loc];
			piLoc1[0] = iParent1Loc;
			piLoc2[0] = iParent2Loc;
			piLoc3[0] = iParent3Loc;
			iSelectParentFlag = 1;
		}
	}

	/**
	 * <PRE>
	 *   UNDXを実行します。
	 * </PRE>
	 * @param plfParent1	第一親
	 * @param plfParent2	第二親
	 * @param plfParent3	第三親
	 * @param lfAlpha		制御パラメータα
	 * @param lfBeta		制御パラメータβ
	 * @param plfChild1		生成した第一子供
	 * @param plfChild2		生成した第二子供
	 */
	private void vUndx( double[] plfParent1, double[] plfParent2, double[] plfParent3, double lfAlpha, double lfBeta, double[] plfChild1, double[] plfChild2 )
	{
		int i;
		ArrayList<Double> stlUnityVector1,stlMedian;
		ArrayList<Double> stlTempT1,stlTempT2;
		double lfDistTemp = 0.0;
		double lfProduct = 0.0;
		double lfDist1 = 0.0;
		double lfDist2 = 0.0;
		double lfDist3 = 0.0;
		double lfSub1 = 0.0;
		double lfSub2 = 0.0;
		double lfSigma1 = 0.0;
		double lfSigma2 = 0.0;
		double lfS = 0.0;
		double lfTemp1 = 0.0;
		double lfTemp2 = 0.0;
		double lfTemp3 = 0.0;

		stlUnityVector1 = new ArrayList<Double>();
		stlMedian = new ArrayList<Double>();
		stlTempT1 = new ArrayList<Double>();
		stlTempT2 = new ArrayList<Double>();
		lfDist1 = lfDist2 = lfDistTemp = 0.0;
		for( i = 0; i < iGenVector; i++ )
		{
			// 2つの親の中点を算出します。
			stlMedian.add( ( plfParent1[i]+plfParent2[i] )/2.0 );
			stlUnityVector1.add( plfParent1[i]-plfParent2[i] );
			// 2つの親の距離を求めます。
			lfSub1 = (plfParent2[i]-plfParent1[i]);
			lfSub2 = (plfParent3[i]-plfParent1[i]);
			lfDist1 += lfSub1*lfSub1;
			lfDist2 += lfSub2*lfSub2;
			// 第3の親と2つの親との距離を求めます。
			lfDistTemp += lfSub1*lfSub2;
			// ここで、z1,z2を生成します。z1=N(0,σ_{1}^2), z2=N(0,σ_{2}^2)なので、これに従って生成します。
//			stlTempT1.add(lfSgima1*rnd.NextNormal());
//			stlTempT2.add(lfSigma2*rnd.NextNormal());
		}
		lfDist1 = Math.sqrt( lfDist1 );
		lfDist2 = Math.sqrt( lfDist2 );
		lfDistTemp = lfDistTemp/(lfDist1*lfDist2);
		lfDist3 = lfDist2*Math.sqrt(1.0-lfDistTemp*lfDistTemp);
		lfSigma1 = lfDist1*lfAlpha;
		lfSigma2 = lfDist3*lfBeta/Math.sqrt((double)iGenVector);
		for( i = 0;i < iGenVector; i++ )
			stlTempT2.add(lfSigma2*rnd.NextNormal());

		lfProduct = 0.0;
		for( i = 0; i < iGenVector; i++ )
		{
			// 単位ベクトルを作成します。
			stlUnityVector1.set(i, stlUnityVector1.get(i) / lfDist1);
		//まずz2に直行するベクトルを算出します。
			// 内積を求めます。
			lfProduct += stlTempT2.get(i)*stlUnityVector1.get(i);
		}
		// z2に直行するベクトルを生成します。
		lfS = lfSigma1*rnd.NextNormal();
		// 2子供を生成します。
		for( i = 0;i < iGenVector; i++ )
		{
			lfTemp1 = lfProduct*stlUnityVector1.get(i);
			lfTemp2 = lfS*stlUnityVector1.get(i);
			lfTemp3 = stlTempT2.get(i) - lfTemp1 + lfTemp2;
			plfChild1[i] = stlMedian.get(i) + lfTemp3;
			plfChild2[i] = stlMedian.get(i) - lfTemp3;
		}
	}

	/**
	 * <PRE>
	 *    現在世代の中で1番目によい個体と2番目によい個体を取得します。
	 * </PRE>
	 * @param pplfChildren	現在世代の個体
	 * @param pi1stGenLoc	1番目によい個体
	 * @param pi2ndGenLoc	2番目によい個体
	 */
	private void vSelectGens( double[][] pplfChildren, int[] pi1stGenLoc, int[] pi2ndGenLoc )
	{
		int i;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfRes = 0.0;
		double lf1stGen = Double.MAX_VALUE;
		double lfRand = 0.0;
		int i1stGenLoc = Integer.MAX_VALUE;
		int i2ndGenLoc = Integer.MAX_VALUE;
		int iRank = 0;
		ArrayList<Rank_t> stlFitProb;
		Rank_t tTempRankData;
		// まず、適応度関数の値を計算します。
		tTempRankData = new Rank_t();
		stlFitProb = new ArrayList<Rank_t>();
		lfRes = 0.0;
		for( i = 0;i < iChildrenNumber; i++ )
		{
			tTempRankData.lfFitProb = pflfObjectiveFunction.lfObjectiveFunction( pplfChildren[i] );
			tTempRankData.iLoc = i;
			stlFitProb.add( tTempRankData );
			lfRes += stlFitProb.get(i).lfFitProb;
			if( stlFitProb.get(i).lfFitProb < lf1stGen )
			{
				lf1stGen = stlFitProb.get(i).lfFitProb;
				i1stGenLoc = i;
			}
		}
		// 目的関数値によるソートを実施します。(昇順、降順にしたい場合は1と-1を返却するのを逆にする。)
		Collections.sort( stlFitProb, new Comparator<Rank_t>(){
			@Override
			public int compare( Rank_t a, Rank_t b )
			{
				if( a.lfFitProb > b.lfFitProb )
					return 1;
				else if( a.lfFitProb < b.lfFitProb )
					return -1;
				return 0;
			}
		});
		// ランクに基づくルーレット選択を実行。
		iRank = rnd.NextInt(iChildrenNumber-1) + 1;
		i2ndGenLoc = stlFitProb.get(iRank).iLoc;
		// 最良個体の位置とそれ以外でルーレット選択により選ばれた位置を返却します。
		pi1stGenLoc[0] = i1stGenLoc;
		pi2ndGenLoc[0] = i2ndGenLoc;
	}

	/**
	 * <PRE>
	 * 　分散の範囲内に確実に正規乱数を発生させるようにします。
	 * </PRE>
	 * @param sigma 分散
	 * @param mean 平均
	 * @return 正規乱数値(-1～1)
	 * @author kobayashi
	 * @since 2016/06/16
	 * @version 0.1
	 */
	private double normalRand( double sigma, double mean )
	{
		double lfRes = 0.0;
		double lfMin,lfMax;
		lfMin = -sigma + mean;
		lfMax = sigma + mean;
		for(;;)
		{
			lfRes = sigma*rnd.NextNormal()+mean;
			if( -lfMin <= lfRes && lfRes <= lfMax ) break;
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 * 　現時点での選択した親を出力します。
	 * </PRE>
	 * @param plfParent1		第一親
	 * @param plfParent2		第二親
	 * @author kobayashi
	 * @since 2015/12/16
	 * @version 0.1
	 */
	public void vOutputCurrentParent( double[] plfParent1, double[] plfParent2 )
	{
		int j;
		double lfRes = 0.0;
		double lfAvgDist = 0.0;
		double lfDist = 0.0;
		double lfDist2 = 0.0;
		if( iParentOutputFlag == 1 )
		{
			// 現時点で選択した第一親を出力します。
			for( j = 0;j < iGenVector; j++ )
			{
				System.out.print(plfParent1[j] + "," );
			}
			System.out.print("\n");
			// 現時点で選択した第二親を出力します。
			for( j = 0;j < iGenVector; j++ )
			{
				System.out.print(plfParent2[j] + "," );
			}
			System.out.print("\n");
		}
	}

	/**
	 * <PRE>
	 * 　現時点での生成した子を出力します。
	 * </PRE>
	 * @param pplfChildrenData	子孫データ
	 * @author kobayashi
	 * @since 2015/12/16
	 * @version 0.1
	 */
	public void vOutputCurrentChildren( double[][] pplfChildrenData )
	{
		int i,j;

		if( iChildrenOutputFlag == 1 )
		{
			for( i= 0; i < iChildrenNumber; i++ )
			{
				for( j = 0;j < iGenVector; j++ )
				{
					System.out.print(pplfChildrenData[i][j] + "," );
				}
				System.out.print("\n");
			}
		}
	}

	/**
	 * <PRE>
	 * 　遺伝子に値を設定します。
	 *   ver 0.1 初版
	 * </PRE>
	 * @param pplfGenData	遺伝子データ
	 * @author kobayashi
	 * @since 2016/08/10
	 * @version 0.1
	 */
	public void vSetGenData( double[][] pplfGenData )
	{
		int i,j;
		for( i= 0; i < iGenNumber; i++ )
		{
			for( j = 0;j < iGenVector; j++ )
			{
				pplfGens[i][j] = pplfGenData[i][j];
			}
		}
	}

	/**
	 * <PRE>
	 * 　現在の遺伝子データを取得します。
	 *   ver 0.1 初版
	 * </PRE>
	 * @param pplfGenData	遺伝子データ
	 * @author kobayashi
	 * @since 2016/08/10
	 * @version 0.1
	 */
	public void vGetGenData( double[][] pplfGenData )
	{
		int i,j;
		for( i= 0; i < iGenNumber; i++ )
		{
			for( j = 0;j < iGenVector; j++ )
			{
				pplfGenData[i][j] = pplfGens[i][j];
			}
		}
	}

	/**
	 * <PRE>
	 * 　現在の遺伝子データの最良値を取得します。
	 *   ver 0.1 初版
	 * </PRE>
	 * @param plfGenData 現時点での遺伝子データ（ベスト値）
	 * @author kobayashi
	 * @since 2016/09/14
	 * @version 0.1
	 */
	public void vGetBestGenData( double[] plfGenData )
	{
		int j;
		for( j = 0;j < iGenVector; j++ )
		{
			plfGenData[j] = pplfGens[iBestLoc][j];
		}
	}

	/**
	 * <PRE>
	 *   最良の個体の番号を取得します。
	 * </PRE>
	 * @return 最良個体の番号
	 */
	public int iGet1stLoc()
	{
		return i1stLoc;
	}

	/**
	 * <PRE>
	 *   2番目によい個体の番号を取得します。
	 * </PRE>
	 * @return 2番目によい個体の番号
	 */
	public int iGet2ndLoc()
	{
		return i2ndLoc;
	}

	/**
	 * <PRE>
	 *   制御用パラメータβを設定します。
	 * </PRE>
	 * @param lfBetaData 制御用パラメータβ
	 */
	public void vSetBeta( double lfBetaData )
	{
		lfBeta = lfBetaData;
	}

	/**
	 * <PRE>
	 *   制御用パラメータαを設定します。
	 * </PRE>
	 * @param lfAlphaData 制御用パラメータα
	 */
	public void vSetAlpha( double lfAlphaData )
	{
		lfAlpha = lfAlphaData;
	}
}
