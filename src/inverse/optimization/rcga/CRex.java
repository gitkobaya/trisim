package inverse.optimization.rcga;

import inverse.optimization.objectivefunction.ObjectiveFunctionInterface;
import inverse.optimization.rankt.Rank_t;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import utility.sfmt.Sfmt;

/**
 *    実数値遺伝的アルゴリズムのREXを扱うクラスです。
 *    交叉手法であるREXとしてはREX,REX-Star, Adaptation REXを実装しています。
 *    遺伝的アルゴリズム自体はJGGというものを使用しています。
 *    それぞれの性能に関しましては各論文を参照してください。
 *    Adaptation REXに関しては距離関数に論文掲載のユークリッド距離及びマハラノビス距離を実装しています。
 *    初期化   vInitialize()
 *    実行     vRex() or vRexStar() or vARex()
 *    終了処理 vTerminate()
 *    という流れで使用します。
 *
 * @author kobayashi
 *
 */
public class CRex extends CRealCodedGa
{
	private int iParentOutputFlag;
	private int iChildrenOutputFlag;
	private int iParentNumber;
	private int iChildrenNumber;
	private int iUpperEvalChildrenNumber;
	private int iDistanceFlag;
	private double[][] pplfChildren;
	private double[] plfTempVector;
	private double[] plfCentroid;
	private double[] plfChildVector;
	private double[] plfChildCentroid;
	private double[] plfUpperEvalCentroid;
	private double[] plfNormalizeRand;
	private double[][] pplfNormalizeRand;
	private double[] plfChildrenCentroid;
	private double[] plfUpperEvalChildrenCentroid;
	private double[] plfCentroidSteep;
	private double lfLcdp;
	private double lfLavg;
	private double lfLearningRate;
	private double lfAlpha;
	private int[] piParentLoc;
	private int iBestLoc;
//	private ObjectiveFunctionInterface pflfObjectiveFunction;

	/**
	 * <PRE>
	 * 　コンストラクタです。初期化処理を行います。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/10/30
	 * @version 1.0
	 */
	public CRex()
	{
		iParentOutputFlag = 0;
		iChildrenOutputFlag = 0;
		iParentNumber = 0;
		iChildrenNumber = 0;
		pplfChildren = null;
		piParentLoc = null;
		iDistanceFlag = 2;
	}

	/**
	 * <PRE>
	 * 　初期化処理を行います。
	 * </PRE>
	 * @param iGenerationNum		世代数
	 * @param iGenNum				遺伝子数
	 * @param iGenVectorData		遺伝子次元数
	 * @param iParentNumberData		親数
	 * @param iChildrenNumberData	子孫数
	 * @author kobayashi
	 * @since 2015/10/30
	 * @version 1.0
	 */
	public void vInitialize( int iGenerationNum, int iGenNum, int iGenVectorData, int iParentNumberData, int iChildrenNumberData )
	{
		int i,j;
		// 親の生成数を設定します。
		iParentNumber = iParentNumberData > iGenNum ? iGenNum : iParentNumberData;

		// 子供の生成数を設定します。
		iChildrenNumber = iChildrenNumberData;

		// 実数値GAの初期化を実行します。
		vInitialize( iGenerationNum, iGenNum, iGenVectorData );

		// 親選択用配列です。
		piParentLoc = new int[iGenNum];
		// 子供のデータを作成します。
		pplfChildren = new double[iChildrenNumber][iGenVector];
		// REX計算用ベクトルデータ一時保存変数です。
		plfTempVector = new double[iGenVector];
		// 重心の計算結果を保持します。
		plfCentroid = new double[iGenVector];
		plfChildVector = new double[iGenVector];
		plfNormalizeRand = new double[iParentNumber];
		plfChildrenCentroid = new double[iGenVector];
		plfUpperEvalChildrenCentroid = new double[iGenVector];
		plfCentroidSteep = new double[iGenVector];

		for( i = 0;i < iChildrenNumber; i++ )
			for( j = 0;j < iGenVector; j++ )
				pplfChildren[i][j] = 0.0;
		for( i = 0;i < iGenVector; i++ )
		{
			plfCentroid[i] = 0.0;
			plfChildVector[i] = 0.0;
			plfTempVector[i] = 0.0;
			plfUpperEvalChildrenCentroid[i] = 0.0;
			plfChildrenCentroid[i] = 0.0;
			plfCentroidSteep[i] = 0.0;
		}
		for( i = 0;i < iParentNumber; i++ )
		{
			plfNormalizeRand[i] = 0.0;
			piParentLoc[i] = i;
		}
		for( i = 0;i < iGenNum; i++ )
			piParentLoc[i] = i;
		long seed;
		seed = System.currentTimeMillis();
		rnd = new Sfmt( (int)seed );
	}

	/**
	 * <PRE>
	 * 　初期化処理を行います。
	 * </PRE>
	 * @param iGenerationNum			世代数
	 * @param iGenNum					遺伝子数
	 * @param iGenVectorData			遺伝子次元数
	 * @param iParentNumberData			親数
	 * @param iChildrenNumberData		子孫数
	 * @param lfLearningRateData		学習率
	 * @param iUpperEvalChildrenNumData	評価値上位の子孫数
	 * @author kobayashi
	 * @since 2015/10/30
	 * @version 1.0
	 */
	public void vInitialize( int iGenerationNum, int iGenNum, int iGenVectorData, int iParentNumberData, int iChildrenNumberData, double lfLearningRateData,int iUpperEvalChildrenNumData )
	{
		int i,j;

		// 親の生成数を設定します。
		iParentNumber = iParentNumberData > iGenNum ? iGenNum : iParentNumberData;

		// 子供の生成数を設定します。
		iChildrenNumber = iChildrenNumberData;

		// 実数値GAの初期化を実行します。
		vInitialize( iGenerationNum, iGenNum, iGenVectorData );

		// 学習率の設定をします。
		lfLearningRate = lfLearningRateData;

		// 交叉後の評価値上位の子供の数の閾値を設定します。
		iUpperEvalChildrenNumber = iUpperEvalChildrenNumData > iChildrenNumber ? iChildrenNumber : iUpperEvalChildrenNumData;

		lfAlpha = 1.0;

		// 親選択用配列です。
		piParentLoc = new int[iGenNum];
		// 子供のデータを作成します。
		pplfChildren = new double[iChildrenNumber][iGenVector];
		pplfNormalizeRand = new double[iChildrenNumber][iParentNumber];
		plfTempVector = new double[iGenVector];
		plfCentroid = new double[iGenVector];
		plfChildVector = new double[iGenVector];
		plfNormalizeRand = new double[iParentNumber];
		plfChildrenCentroid = new double[iGenVector];
		plfUpperEvalChildrenCentroid = new double[iGenVector];
		plfCentroidSteep = new double[iGenVector];

		for( i = 0;i < iChildrenNumber; i++ )
			for( j = 0;j < iGenVector; j++ )
				pplfChildren[i][j] = 0.0;

		for( i = 0;i < iGenVector; i++ )
		{
			plfCentroid[i] = 0.0;
			plfChildVector[i] = 0.0;
			plfTempVector[i] = 0.0;
			plfUpperEvalChildrenCentroid[i] = 0.0;
			plfChildrenCentroid[i] = 0.0;
			plfCentroidSteep[i] = 0.0;
		}

		for( i = 0;i < iParentNumber; i++ )
			plfNormalizeRand[i] = 0.0;

		for( i = 0;i < iGenNum; i++ )
			piParentLoc[i] = i;

		for( i = 0;i < iChildrenNumber; i++ )
			for( j = 0;j < iParentNumber; j++ )
				pplfNormalizeRand[i][j] = 0.0;

		long seed;
		seed = System.currentTimeMillis();
		rnd = new Sfmt( (int)seed );
	}

	/**
	 * <PRE>
	 * 　終了処理を行います。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/10/30
	 * @version 1.0
	 */
	public void vTerminate()
	{
		int i;

		// 継承元クラスに属するメンバ変数の終了処理を実行します。
		vTerminate();

		piParentLoc = null;
		pplfChildren = null;
		pplfNormalizeRand = null;
		plfTempVector = null;
		plfCentroid = null;
		plfChildVector = null;
		plfNormalizeRand = null;
		plfChildrenCentroid = null;
		plfUpperEvalChildrenCentroid = null;
		plfCentroidSteep = null;
	}

	/**
	 * <PRE>
	 * 　REXを実行します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/10/30
	 * @version 1.0
	 */
	public void vRex()
	{
		int i,j,k;
		int iLoc;
		int iTemp = 0;
		double lfSigma = 0.0;
		ArrayList<Rank_t> stlFitProb;
		Rank_t tTempRankData;

	/* JGGモデル */
		stlFitProb = new ArrayList<Rank_t>();
		tTempRankData = new Rank_t();

		// 親をランダムにNp個選択します。
		for( i = iGenNumber-1; i > 0 ; i-- )
		{
			iLoc = (int)((i+1)*rnd.NextUnif());
			iTemp = piParentLoc[i];
			piParentLoc[i] = piParentLoc[iLoc];
			piParentLoc[iLoc] = iTemp;
		}

		// 重心を算出します。
		for( j = 0;j < iGenVector; j++ )
		{
			plfCentroid[j] = 0.0;
			for( i = 0;i < iParentNumber; i++ )
				plfCentroid[j] += pplfGens[piParentLoc[i]][j];
			plfCentroid[j] /= (double)iParentNumber;
		}
		// REX(RealCoded Emsanble )を実行します。交叉回数Nc回実行し、Nc個の子供を生成します。
		// 統計量遺伝における普遍分散を算出します。
//		lfSigma = 1.0/(double)sqrt( (double)iParentNumber );
		lfSigma = Math.sqrt(3.0/(double)iParentNumber);

		for( i = 0;i < iChildrenNumber; i++ )
		{
			for( j = 0;j < iParentNumber; j++ )
			{
//				plfNormalizeRand[j] = lfSigma*rnd.NextNormal();
				plfNormalizeRand[j] = lfSigma*(2.0*rnd.NextUnif()-1.0);
			}
			for( k = 0;k < iGenVector; k++ )
			{
			// REXを実行して、子供を生成します。
				// 正規乱数により乱数を発生させます。
				plfTempVector[k] = 0.0;
				for( j = 0;j < iParentNumber; j++ )
					plfTempVector[k] += plfNormalizeRand[j] * ( pplfGens[piParentLoc[j]][k] - plfCentroid[k] );
				pplfChildren[i][k] = plfCentroid[k] + plfTempVector[k];
			}
			// 評価値をNp個算出します。
			tTempRankData.lfFitProb = pflfObjectiveFunction.lfObjectiveFunction( pplfChildren[i] );
			tTempRankData.iLoc = i;
			stlFitProb.add( tTempRankData );
		}

		// 目的関数値によるソートを実施します。
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

		// 親を入れ替えます。(JGGモデルの場合は親はすべて変更するものとします。)
		for( i = 0; i < iParentNumber; i++ )
			for( j = 0;j < iGenVector; j++ )
				pplfGens[piParentLoc[i]][j] = pplfChildren[stlFitProb.get(i).iLoc][j];
		// 現在の最良位置を取得します。
		iBestLoc = piParentLoc[0];
	}

	/**
	 * <PRE>
	 * 　REX Starを実行します。未完
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/10/30
	 * @version 1.0
	 */
	public void vRexStar()
	{
		int i,j,k;
		int iLoc;
		int iMaxSize = 0;
		int iOverLapLoc = 0;
		double lfSigma = 0.0;
		ArrayList<Rank_t> stlFitProb;
		Rank_t tTempRankData;
		ArrayList<Integer> stlSelectParentLoc;

	/* JGGモデル */
		stlSelectParentLoc = new ArrayList<Integer>();
		stlFitProb = new ArrayList<Rank_t>();
		tTempRankData = new Rank_t();

		// 親をランダムにNp個選択します。
		for(;;)
		{
			iLoc = rnd.NextInt( iGenNumber );
			// 選択した親と重なっていないか調査します。
			iOverLapLoc = -1;
			for( i = 0;i < stlSelectParentLoc.size(); i++ )
			{
				if( stlSelectParentLoc.get(i) == iLoc )
				{
					iOverLapLoc = i;
					break;
				}
			}
			// 重なっていなければ、位置を追加します。
			if( iOverLapLoc == -1 )
			{
				stlSelectParentLoc.add( iLoc );
				iMaxSize++;
			}
			// 指定した親の数になったら終了します。
			if( iMaxSize == iParentNumber ) break;
		}
		// 重心を算出します。
		for( i = 0;i < iGenVector; i++ )
			plfCentroid[i] = 0.0;
		for( i = 0;i < iParentNumber; i++ )
			for( j = 0;j < iGenVector; j++ )
				plfCentroid[j] += ( pplfGens[stlSelectParentLoc.get(i)][j] );
		for( i = 0;i < iParentNumber; i++ )
			plfCentroid[i] /= (double)iParentNumber;
	// REX(RealCoded Emsanble )を実行します。交叉回数Nc回実行し、Nc個の子供を生成します。
		// 統計量遺伝における普遍分散を算出します。
		lfSigma = 1.0/(double)Math.sqrt( (double)iParentNumber );

		for( i = 0;i < iChildrenNumber; i++ )
		{
			// 正規乱数により乱数を発生させます。
			for( j = 0;j < iParentNumber; j++ )
				plfNormalizeRand[j] = lfSigma*rnd.NextNormal();
			for( j = 0;j < iGenVector; j++ )
			{
				plfTempVector[j] = 0.0;
				plfChildVector[j] = 0.0;
			}
			for( j = 0;j < iParentNumber; j++ )
			{
			// REXを実行して、子供を生成します。
				for( k = 0;k < iGenVector; k++ )
					plfTempVector[k] += plfNormalizeRand[j] * ( pplfGens[stlSelectParentLoc.get(j)][k] - plfCentroid[k] );
			}
			for( k = 0;k < iGenVector; k++ )
				plfChildVector[k] = plfCentroid[k] + plfTempVector[k];
			for( j = 0;j < iGenVector; j++ )
				pplfChildren[i][j] = plfChildVector[j];
		}

		// 評価値をNp個算出します。
		for( i = 0;i < iChildrenNumber; i++ )
		{
			tTempRankData.lfFitProb = pflfObjectiveFunction.lfObjectiveFunction( pplfChildren[i] );
			tTempRankData.iLoc = i;
			stlFitProb.add( tTempRankData );
		}
		// 目的関数値によるソートを実施します。
		Collections.sort( stlFitProb, new Comparator<Rank_t>(){
			@Override
			public int compare( Rank_t a, Rank_t b )
			{
				return a.lfFitProb > b.lfFitProb ? 1 : 0;
			}
		});
		// 親を入れ替えます。(JGGモデルの場合は親はすべて変更するものとします。)
		for( i = 0; i < iParentNumber; i++ )
			for( j = 0;j < iGenVector; j++ )
				pplfGens[stlSelectParentLoc.get(i)][j] = pplfChildren[stlFitProb.get(i).iLoc][j];
	}

	/**
	 * <PRE>
	 *    適応的実数値交叉AREXを実行します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/6/10
	 * @version 1.0
	 */
	public void vARex()
	{
		int i,j,k;
		int iLoc;
		int iMaxSize = 0;
		int iOverLapLoc = 0;
		int iTemp = 0;
		double lfSigma = 0.0;
		double l;
		double lAvgDist = 0.0;
		double lfTemp = 0.0;
		ArrayList<Rank_t> stlFitProb;
		ArrayList<Rank_t> stlParentFitProb;
		Rank_t tTempRankData;
		ArrayList<Integer> stlSelectParentLoc;

		int i1stGenLoc = 0;
		int i2ndGenLoc = 0;

	/* JGGモデル */
		stlSelectParentLoc = new ArrayList<Integer>();
		tTempRankData = new Rank_t();
		stlFitProb = new ArrayList<Rank_t>();
		stlParentFitProb = new ArrayList<Rank_t>();

		// 親をランダムにNp個選択します。
		for( i = iGenNumber-1; i >= 0 ; i-- )
		{
			iLoc = (int)((i+1)*rnd.NextUnif());
			iTemp = piParentLoc[i];
			piParentLoc[i] = piParentLoc[iLoc];
			piParentLoc[iLoc] = iTemp;
			if( i <= iParentNumber )
			{
				// 親データを適応度でソートするため、データを代入します。
				tTempRankData.lfFitProb = pflfObjectiveFunction.lfObjectiveFunction( pplfGens[piParentLoc[i]] );
				tTempRankData.iLoc = piParentLoc[i];
				stlParentFitProb.add( tTempRankData );
			}
		}

//		std::sort( stlParentFitProb.begin(), stlParentFitProb.end(), CCompareToRank() );
		Collections.sort( stlParentFitProb, new Comparator<Rank_t>(){
			@Override
			public int compare( Rank_t a, Rank_t b )
			{
				return a.lfFitProb > b.lfFitProb ? 1 : 0;
			}
		});

		// 重心及び交叉中心降下を算出します。
		for( j = 0;j < iGenVector; j++ )
		{
			plfCentroid[j] = 0.0;
			plfCentroidSteep[j] = 0.0;
			for( i = 0;i < iParentNumber; i++ )
			{
				plfCentroid[j] += pplfGens[stlParentFitProb.get(i).iLoc][j];
				plfCentroidSteep[j] += 2.0*(double)(iParentNumber+1-(i+1))*pplfGens[stlParentFitProb.get(i).iLoc][j];
			}
			plfCentroid[j] /= (double)iParentNumber;
			plfCentroidSteep[j] /= (double)(iParentNumber*(iParentNumber+1));
		}
	// REX(RealCoded Emsanble )を実行します。交叉回数Nc回実行し、Nc個の子供を生成します。
		// 統計量遺伝における普遍分散を算出します。
		lfSigma = 1.0/(double)Math.sqrt( (double)iParentNumber-1 );
//		lfSigma = Math.sqrt(3.0/(double)(iParentNumber));

		for( i = 0;i < iChildrenNumber; i++ )
		{
			// 正規乱数により乱数を発生させます。
			for( j = 0;j < iParentNumber; j++ )
			{
				plfNormalizeRand[j] = lfSigma*rnd.NextNormal();
//				plfNormalizeRand[j] = lfSigma*(2.0*rnd.NextUnif()-1.0);
				if( iDistanceFlag == 2 ) pplfNormalizeRand[i][j] = plfNormalizeRand[j];
			}
			for( k = 0;k < iGenVector; k++ )
			{
			// REXを実行して、子供を生成します。
				plfTempVector[k] = 0.0;
				for( j = 0;j < iParentNumber; j++ )
					plfTempVector[k] += plfNormalizeRand[j] * ( pplfGens[stlParentFitProb.get(j).iLoc][k] - plfCentroid[k] );
				pplfChildren[i][k] = plfCentroidSteep[k] + lfAlpha*plfTempVector[k];
			}
			// 評価値をNp個算出します。
			tTempRankData.lfFitProb = pflfObjectiveFunction.lfObjectiveFunction( pplfChildren[i] );
			tTempRankData.iLoc = i;
			stlFitProb.add( tTempRankData );
		}

		// 目的関数値によるソートを実施します。
		Collections.sort( stlFitProb, new Comparator<Rank_t>(){
			@Override
			public int compare( Rank_t a, Rank_t b )
			{
				return a.lfFitProb > b.lfFitProb ? 1 : 0;
			}
		});

		// 拡張率適応度を計算します。
		if( iDistanceFlag == 1 )
		{
			// 交叉の中心を求めます。
			// 評価値上位μα個の子供の中心を求めます。
			for( j = 0;j < iGenVector; j++ )
			{
				plfChildrenCentroid[j] = 0.0;
				plfUpperEvalChildrenCentroid[j] = 0.0;
				for( i = 0;i < iChildrenNumber; i++ )
				{
					plfChildrenCentroid[j] += pplfChildren[stlFitProb.get(i).iLoc][j];
					plfUpperEvalChildrenCentroid[j] += pplfChildren[stlFitProb.get(i).iLoc][j];
				}
				plfChildrenCentroid[j] /= (double)(iChildrenNumber);
				plfUpperEvalChildrenCentroid[j] /= (double)iUpperEvalChildrenNumber;
			}
			vAerEuclide( stlParentFitProb );
		}
		else if( iDistanceFlag == 2 )
		{
			vAerMahalanobis( stlFitProb );
		}

			// 親を入れ替えます。(JGGモデルの場合は親はすべて変更するものとします。)
		for( i = 0; i < iParentNumber; i++ )
			for( j = 0;j < iGenVector; j++ )
				pplfGens[stlParentFitProb.get(i).iLoc][j] = pplfChildren[stlFitProb.get(i).iLoc][j];
		// 現在の最良位置を取得します。
		iBestLoc = stlParentFitProb.get(0).iLoc;
	}

	/**
	 * <PRE>
	 * 　拡張率適応度をユークリッド距離により計算します。
	 * </PRE>
	 * @param stlParentFitProb ユークリッド距離計算用親の適応度を格納したArrayList
	 * @author kobayashi
	 * @since 2016/8/25
	 * @version 0.1
	 */
	public void vAerEuclide( ArrayList<Rank_t> stlParentFitProb )
	{
		int i,j;
		double lfTemp;
		double lfSigma;
		double lfNorm = 0.0;

		// Ldcpを算出します。
		lfSigma = 1.0/(double)Math.sqrt( (double)iParentNumber-1 );
		lfLcdp = 0.0;
		for( i = 0;i < iGenVector; i++ )
		{
			lfTemp = plfUpperEvalChildrenCentroid[i] - plfChildrenCentroid[i];
			lfLcdp += lfTemp*lfTemp;
		}

		// Lavgを算出します。
		lfLavg = 0.0;
		for( i = 0;i < iParentNumber; i++ )
		{
			lfNorm = 0.0;
			for( j = 0;j < iGenVector; j++ )
			{
				lfTemp = pplfGens[stlParentFitProb.get(i).iLoc][j] - plfCentroid[j];
				lfNorm += lfTemp*lfTemp;
			}
			lfLavg += lfNorm;
		}
		lfLavg = lfAlpha*lfAlpha*lfSigma*lfSigma/(double)iUpperEvalChildrenNumber;

		// αの更新を行います。
		lfTemp = lfAlpha * Math.sqrt( (1.0-lfLearningRate)+lfLearningRate*lfLcdp/lfLavg );
		lfAlpha = lfTemp < 1.0 ? 1.0 : lfTemp;
	}

	/**
	 * <PRE>
	 * 　拡張率適応度をマハラノビス距離により計算します。
	 * </PRE>
	 * @param stlFitProb マハラノビス距離計算用親の適応度を格納したArrayList
	 * @author kobayashi
	 * @since 2016/8/25
	 * @version 0.1
	 */
	public void vAerMahalanobis( ArrayList<Rank_t> stlFitProb )
	{
		int i,j;
		double lfTemp;
		double lfSigma;
		double lfRandAvg = 0.0;
		double lfRandAvgSumSquare = 0.0;
		double lfRandAvgSumSquareAvg = 0.0;

		// Ldcpを算出します。
		lfSigma = 1.0/(double)Math.sqrt( (double)iParentNumber );
//		lfSigma = Math.sqrt( 3.0/(double)iParentNumber );
		lfLcdp = 0.0;
		for( i = 0;i < iParentNumber; i++ )
		{
			lfRandAvg = 0.0;
			for( j = 0;j < iUpperEvalChildrenNumber; j++ )
			{
				lfRandAvg += pplfNormalizeRand[stlFitProb.get(j).iLoc][i];
			}
			lfRandAvg /= (double)iUpperEvalChildrenNumber;
			lfRandAvgSumSquare += lfRandAvg*lfRandAvg;
			lfRandAvgSumSquareAvg += lfRandAvg;
		}
		lfRandAvgSumSquareAvg = lfRandAvgSumSquareAvg*lfRandAvgSumSquareAvg/(double)iParentNumber;
		lfLcdp = lfAlpha*lfAlpha*(iParentNumber-1)*(lfRandAvgSumSquare-lfRandAvgSumSquareAvg);

		// Lavgを算出します。
		lfLavg = lfAlpha*lfAlpha*lfSigma*lfSigma*(iParentNumber-1)*(iParentNumber-1)/(double)iUpperEvalChildrenNumber;

		// αの更新を行います。
		lfTemp = lfAlpha * Math.sqrt( (1.0-lfLearningRate)+lfLearningRate*lfLcdp/lfLavg );
		lfAlpha = lfTemp < 1.0 ? 1.0 : lfTemp;
	}

	public void vSelectGens( double[][] pplfChildren, int[] pi1stGenLoc, int[] pi2ndGenLoc )
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
		lfRes = 0.0;
		tTempRankData = new Rank_t();
		stlFitProb = new ArrayList<Rank_t>();

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
		// 目的関数値によるソートを実施します。
		Collections.sort( stlFitProb, new Comparator<Rank_t>(){
			@Override
			public int compare( Rank_t a, Rank_t b )
			{
				return a.lfFitProb > b.lfFitProb ? 1: 0;
			}
		});
		// ランクに基づくルーレット選択を実行。
		iRank = rnd.NextInt( iChildrenNumber-1 ) + 1;
		i2ndGenLoc = stlFitProb.get(iRank).iLoc;
		// 最良個体の位置とそれ以外でルーレット選択により選ばれた位置を返却します。
		pi1stGenLoc[0] = i1stGenLoc;
		pi2ndGenLoc[0] = i2ndGenLoc;
	}

	/**
	 * <PRE>
	 * 　現時点での選択した親を出力します。
	 * </PRE>
	 * @param stlSelectParentLocData	選択した親データ
	 * @author kobayashi
	 * @since 2015/12/16
	 * @version 0.1
	 */
	public void vOutputCurrentParent( ArrayList<Integer> stlSelectParentLocData )
	{
		int i,j;
		double lfRes = 0.0;
		double lfAvgDist = 0.0;
		double lfDist = 0.0;
		double lfDist2 = 0.0;
		if( iParentOutputFlag == 1 )
		{
			for(i = 0;i < stlSelectParentLocData.size(); i++ )
			{
				// 現時点で選択した第一親を出力します。
				for( j = 0;j < iGenVector; j++ )
				{
					System.out.print( pplfGens[stlSelectParentLocData.get(i)][j] + "," );
				}
				System.out.print("\n");
			}
		}
	}

	/**
	 * <PRE>
	 * 　現時点での生成した子を出力します。
	 * </PRE>
	 * @param pplfChildrenData	子孫の遺伝子データ
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
	 * @since 2016/08/26
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
	 * @since 2016/08/26
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
	 * 　現在の遺伝子データを取得します。
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
}
