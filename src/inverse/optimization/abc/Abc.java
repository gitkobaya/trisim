package inverse.optimization.abc;

import inverse.optimization.constraintcondition.ConstraintConditionInterface;
import inverse.optimization.objectivefunction.ObjectiveFunctionInterface;
import inverse.optimization.powell.Powell;
import inverse.optimization.rankt.Rank_t;
import inverse.optimization.rcga.CRex;
import inverse.optimization.rcga.CUndx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import utility.sfmt.Sfmt;

public class Abc
{
	private int iGenerationNumber;				// 計算回数
	private int iAbcDataNum;					// コロニーの数
	private int iAbcVectorDimNum;				// 蜂の特徴ベクトル
	private int iAbcSearchNum;					// 探索点数
	private int iAbcLimitCount;					// 更新しなかった回数
	private int iAbcIntervalMinNum;				// 最低反復回数
	private int iAbcUpperSearchNum;				// 探索点上位数
	private double lfConvergenceParam;			// 収束状況パラメータ
	private double lfFitBound;					// 適合度許容限界値
	private double lfFitAccuracy;				// 適合度評価精度
	private int[] piLocalMaxAbcLoc;				//
	private double[][] pplfAbcData;				// ABCデータ配列
	private double[][] pplfNAbcData;				// ABC更新用データ配列
	private double lfGlobalMaxAbcData;			// 大域的最適値
	private double lfGlobalMinAbcData;			// 大域的最小値
	private double[] plfGlobalMaxAbcData;		// 大域的最適解を表す粒子のデータ
	private double[] plfGlobalMinAbcData;		// 大域的最適解を表す粒子のデータ
	private double[] plfLocalMaxAbcData;			// 局所最適解を表す粒子のデータ
	private double[] plfLocalMinAbcData;			// 局所最適解を表す粒子のデータ
	private double lfLocalMaxAbcData;			// 局所最適値
	private double[][] pplfLocalMaxAbcData;		// 局所最適解を表す粒子ごとの最大値を格納するデータ
	private double[][] pplfLocalMinAbcData;		// 局所最適解を表す粒子ごとの最小値を格納するデータ
	private double[] plfLocalMaxObjectiveAbcData;// 局所最適解を表す粒子のデータ
	private double[][] pplfVelocityData;			// ABCデータ速度配列
	private double[][] pplfNVelocityData;			// ABC更新用データ速度配列
	private double[] plfFitProb;					// 適合度相対確率
	private double[] plfFit;						// 適合度
	private int[] piNonUpdateCount;				// 更新しない回数
	private int[] piTotalNonUpdateCount;			// 更新しない総回数
	private double[] plfVelocity;				// ルーレット選択により選択する速度ベクトル
	private double[] plfCrossOverData;			// 交叉一時格納用配列
	private double lfSolveRange;				// 出力範囲
	private double lfSolveRangeMin;				// 出力範囲最小値
	private double lfSolveRangeMax;				// 出力範囲最小値
	private double lfFitInit;					// 平均評価値
	private double lfFitCurrentBest;			// 現在の最良値の適応度
	private double[] plfXnew1;					// Memetic Algorithm用更新配列
	private double[] plfXnew2;					// Memetic Algorithm用更新配列
	private CUndx pcUndx;						// UNDX用のインスタンス
	private int iCrossOverNum;					// 交叉回数
	private double lfAlpha;						// UNDX用第一親決定用α
	private double lfBeta;						// UNDX用第二親決定用β
	private CRex pcRex;						// REX用のインスタンス
	private int iParentNumber;					// Rex用選択する親の数
	private int iChildrenNumber;				// Rex用生成する子供の数
	private int iUpperEvalChildrenNumber;		// ARex用生成した子供の上位を選択する数
	private double lfLearningRate;				// ARex用学習率
	private ArrayList<Rank_t> stlFitProb;
	private int iReCounter;						// HJABC用カウンター
	private int iHJInterval;					// HJABC用HookeJeeves法適用間隔
	private int iHJCounter;						// HJABC用HookeJeeves法繰り返し回数
	private double[] plfX0;						// HJABC用更新用一時格納配列
	private double[] plfX1;						// HJABC用更新用一時格納配列
	private double[] plfX2;						// HJABC用更新用一時格納配列
	private double[] plfStepSize;				// HJABC用ステップサイズ格納配列
	private double[] plfScoutBeeResult;			// best-so-far ABC法用ScoutBee格納配列
	private Powell pcPowell;					// Powell法を計算する。（ニューメリカルレシピインシー参照）
	private Sfmt rnd;
	private ObjectiveFunctionInterface pflfObjectiveFunction;	// 評価指標のコールバック関数
	private ConstraintConditionInterface pfvConstraintCondition;	// 評価指標の制約条件のコールバック関数

	/**
	 * <PRE>
	 * 　コンストラクタ
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 0.1
	 */
	public Abc()
	{
		iGenerationNumber = 0;				// 計算回数
		iAbcDataNum = 0;				// 粒子群の数
		iAbcVectorDimNum = 0;				// 各粒子群の特徴ベクトル
		iAbcSearchNum = 0;				// 探索点の総数
		pplfAbcData = null;				// ABCデータ配列
		pplfNAbcData = null;				// ABC更新用データ配列
		pplfVelocityData = null;			// ABCデータ速度配列
		pplfNVelocityData = null;			// ABC更新用データ速度配列
		piNonUpdateCount = null;			// 更新されなかった回数
		piTotalNonUpdateCount = null;			// 更新されなかった総回数
		plfVelocity = null;				// ルーレット選択により選択する速度ベクトル
		plfGlobalMaxAbcData = null;
		plfGlobalMinAbcData = null;			// 大域的最適解を表す粒子のデータ
		plfCrossOverData = null;
		plfLocalMaxAbcData = null;
		lfSolveRange = 0.0;
		lfGlobalMinAbcData = Double.MAX_VALUE;
		lfGlobalMaxAbcData = -Double.MAX_VALUE;
		iReCounter = 0;

		plfXnew1 = null;
		plfXnew2 = null;

		plfX0 = null;
		plfX1 = null;
		plfX2 = null;
		plfStepSize = null;
		plfScoutBeeResult = null;

		pcUndx = null;
		pcRex = null;
		pcPowell = null;
	}



	/**
	 * <PRE>
	 * 　人口蜂コロニーの初期化を実行します。(コンストラクタ)
	 * 　オリジナルバージョンはこれを使用。
	 * </PRE>
	 * @param iGenCount       計算回数
	 * @param iGenNum         コロニー数
	 * @param iGenVectorDim   探索点の次元数
	 * @param iSearchNum  　  探索点の数
	 * @param iLimitCountData 更新しなかった回数
	 * @author kobayashi
	 * @since 2015/8/4
	 * @version 0.1
	 */
	public Abc( int iGenCount, int iGenNum, int iGenVectorDim, int iSearchNum, int iLimitCountData )
	{
		iGenerationNumber = 0;				// 計算回数
		iAbcDataNum = 0;				// 粒子群の数
		iAbcVectorDimNum = 0;				// 各粒子群の特徴ベクトル
		iAbcSearchNum = 0;				// 探索点の総数
		pplfAbcData = null;				// ABCデータ配列
		pplfNAbcData = null;				// ABC更新用データ配列
		pplfVelocityData = null;			// ABCデータ速度配列
		pplfNVelocityData = null;			// ABC更新用データ速度配列
		piNonUpdateCount = null;			// 更新されなかった回数
		piTotalNonUpdateCount = null;			// 更新されなかった総回数
		plfVelocity = null;				// ルーレット選択により選択する速度ベクトル
		plfGlobalMaxAbcData = null;
		plfGlobalMinAbcData = null;			// 大域的最適解を表す粒子のデータ
		plfCrossOverData = null;
		plfLocalMaxAbcData = null;
		lfSolveRange = 0.0;
		lfGlobalMinAbcData = Double.MAX_VALUE;
		lfGlobalMaxAbcData = -Double.MAX_VALUE;
		iReCounter = 0;

		plfXnew1 = null;
		plfXnew2 = null;

		plfX0 = null;
		plfX1 = null;
		plfX2 = null;
		plfStepSize = null;
		plfScoutBeeResult = null;

		pcUndx = null;
		pcRex = null;
		pcPowell = null;

		vInitialize( iGenCount, iGenNum, iGenVectorDim, iSearchNum, iLimitCountData );
	}

	/**
	 * <PRE>
	 *  人口蜂コロニーの初期化を実行します。(コンストラクタ)
	 *  UX-ABC法はこれを使用、GbestABC-UNDXもこれを使用。
	 * </PRE>
	 * @param iGenCount       計算回数
	 * @param iGenNum         コロニー数
	 * @param iGenVectorDim   探索点の次元数
	 * @param iSearchNum  　  探索点の数
	 * @param iLimitCountData 更新しなかった回数
	 * @param iCrossOverNum   交叉回数
	 * @param lfAlpha         UNDX用パラメータ１
	 * @param lfBeta          UNDX用パラメータ２
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public Abc( int iGenCount, int iGenNum, int iGenVectorDim, int iSearchNum, int iLimitCountData, int iCrossOverNum, double lfAlpha, double lfBeta )
	{
		iGenerationNumber = 0;				// 計算回数
		iAbcDataNum = 0;				// 粒子群の数
		iAbcVectorDimNum = 0;				// 各粒子群の特徴ベクトル
		iAbcSearchNum = 0;				// 探索点の総数
		pplfAbcData = null;				// ABCデータ配列
		pplfNAbcData = null;				// ABC更新用データ配列
		pplfVelocityData = null;			// ABCデータ速度配列
		pplfNVelocityData = null;			// ABC更新用データ速度配列
		piNonUpdateCount = null;			// 更新されなかった回数
		piTotalNonUpdateCount = null;			// 更新されなかった総回数
		plfVelocity = null;				// ルーレット選択により選択する速度ベクトル
		plfGlobalMaxAbcData = null;
		plfGlobalMinAbcData = null;			// 大域的最適解を表す粒子のデータ
		plfCrossOverData = null;
		plfLocalMaxAbcData = null;
		lfSolveRange = 0.0;
		lfGlobalMinAbcData = Double.MAX_VALUE;
		lfGlobalMaxAbcData = -Double.MAX_VALUE;
		iReCounter = 0;

		plfXnew1 = null;
		plfXnew2 = null;

		plfX0 = null;
		plfX1 = null;
		plfX2 = null;
		plfStepSize = null;
		plfScoutBeeResult = null;

		pcUndx = null;
		pcRex = null;
		pcPowell = null;

		vInitialize( iGenCount, iGenNum, iGenVectorDim, iSearchNum, iLimitCountData, iCrossOverNum, lfAlpha, lfBeta );
	}

	/**
	 * <PRE>
	 * 　人口蜂コロニーの初期化を実行します。(コンストラクタ)
	 * 　高性能化ABC法はこれを使用。
	 * </PRE>
	 * @param iGenCount				計算回数
	 * @param iGenNum				コロニー数
	 * @param iGenVectorDim			探索点の次元数
	 * @param iSearchNum			探索点の数
	 * @param iLimitCountData		更新しなかった回数
	 * @param iIntervalMinNumData	最低反復回数
	 * @param iAlphaData			パラメータ
	 * @param lfDrData				収束状況判定
	 * @param lfBoundData			適合度許容限界値
	 * @param lfAccuracyData		適合度評価精度
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public Abc( int iGenCount, int iGenNum, int iGenVectorDim, int iSearchNum, int iLimitCountData, int iIntervalMinNumData, int iAlphaData, double lfDrData, double lfBoundData, double lfAccuracyData )
	{
		iGenerationNumber = 0;				// 計算回数
		iAbcDataNum = 0;				// 粒子群の数
		iAbcVectorDimNum = 0;				// 各粒子群の特徴ベクトル
		iAbcSearchNum = 0;				// 探索点の総数
		pplfAbcData = null;				// ABCデータ配列
		pplfNAbcData = null;				// ABC更新用データ配列
		pplfVelocityData = null;			// ABCデータ速度配列
		pplfNVelocityData = null;			// ABC更新用データ速度配列
		piNonUpdateCount = null;			// 更新されなかった回数
		piTotalNonUpdateCount = null;			// 更新されなかった総回数
		plfVelocity = null;				// ルーレット選択により選択する速度ベクトル
		plfGlobalMaxAbcData = null;
		plfGlobalMinAbcData = null;			// 大域的最適解を表す粒子のデータ
		plfCrossOverData = null;
		plfLocalMaxAbcData = null;
		lfSolveRange = 0.0;
		lfGlobalMinAbcData = Double.MAX_VALUE;
		lfGlobalMaxAbcData = -Double.MAX_VALUE;
		iReCounter = 0;

		plfXnew1 = null;
		plfXnew2 = null;

		plfX0 = null;
		plfX1 = null;
		plfX2 = null;
		plfStepSize = null;
		plfScoutBeeResult = null;

		pcUndx = null;
		pcRex = null;
		pcPowell = null;

		vInitialize( iGenCount, iGenNum, iGenVectorDim, iSearchNum, iLimitCountData, iIntervalMinNumData, iAlphaData, lfDrData, lfBoundData, lfAccuracyData );
	}

	/**
	 * <PRE>
	 * 　人口蜂コロニーの初期化を実行します。(コンストラクタ)
	 * </PRE>
	 * @param iGenCount     		 計算回数
	 * @param iGenNum       		 コロニー数
	 * @param iGenVectorDim 		 探索点の次元数
	 * @param iSearchNum 			 探索点の数
	 * @param iLimitCountData		 更新しなかった回数
	 * @param iIntervalMinNumData	 最低反復回数
	 * @param iAlphaData			 パラメータ
	 * @param lfDrData       		 収束状況判定
	 * @param lfBoundData    		 適合度許容限界値
	 * @param lfAccuracyData		 適合度評価精度
	 * @param iParentNum 		     選択する親の数
	 * @param iChildrenNum           生成する子供の数
	 * @param iEvalChildrenUpperNum  選択する適合度上位の子供の数
	 * @param lfLearningRate         学習率
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public Abc( int iGenCount, int iGenNum, int iGenVectorDim, int iSearchNum, int iLimitCountData, int iIntervalMinNumData, int iAlphaData, double lfDrData, double lfBoundData, double lfAccuracyData, int iParentNum, int  iChildrenNum, int iEvalChildrenUpperNum, double lfLearningRate )
	{
		iGenerationNumber = 0;				// 計算回数
		iAbcDataNum = 0;				// 粒子群の数
		iAbcVectorDimNum = 0;				// 各粒子群の特徴ベクトル
		iAbcSearchNum = 0;				// 探索点の総数
		pplfAbcData = null;				// ABCデータ配列
		pplfNAbcData = null;				// ABC更新用データ配列
		pplfVelocityData = null;			// ABCデータ速度配列
		pplfNVelocityData = null;			// ABC更新用データ速度配列
		piNonUpdateCount = null;			// 更新されなかった回数
		piTotalNonUpdateCount = null;			// 更新されなかった総回数
		plfVelocity = null;				// ルーレット選択により選択する速度ベクトル
		plfGlobalMaxAbcData = null;
		plfGlobalMinAbcData = null;			// 大域的最適解を表す粒子のデータ
		plfCrossOverData = null;
		plfLocalMaxAbcData = null;
		lfSolveRange = 0.0;
		lfGlobalMinAbcData = Double.MAX_VALUE;
		lfGlobalMaxAbcData = -Double.MAX_VALUE;
		iReCounter = 0;

		plfXnew1 = null;
		plfXnew2 = null;

		plfX0 = null;
		plfX1 = null;
		plfX2 = null;
		plfStepSize = null;
		plfScoutBeeResult = null;

		pcUndx = null;
		pcRex = null;
		pcPowell = null;

		vInitialize( iGenCount, iGenNum, iGenVectorDim, iSearchNum, iLimitCountData, iIntervalMinNumData, iAlphaData, lfDrData, lfBoundData, lfAccuracyData, iParentNum, iChildrenNum, iEvalChildrenUpperNum, lfLearningRate );
	}

	/**
	 * <PRE>
	 * 　人口蜂コロニーの初期化を実行します。
	 *   ver 0.1 初版
	 *   ver 0.2 現在の最新版のABC法ソースコードにバージョンアップ
	 * </PRE>
	 * @param iGenCount				計算回数
	 * @param iGenNum				コロニー数
	 * @param iGenVectorDim			探索点の次元数
	 * @param iSearchNum			探索点の数
	 * @param iLimitCountData		更新しなかった回数
	 * @param iIntervalMinNumData	最低反復回数
	 * @param iAlphaData			探索点上位数
	 * @param lfDrData				収束状況判定
	 * @param lfBoundData			適合度許容限界値
	 * @param lfAccuracyData		適合度評価精度
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.2
	 */
	public void vInitialize( int iGenCount, int iGenNum, int iGenVectorDim, int iSearchNum, int iLimitCountData, int iIntervalMinNumData, int iAlphaData, double lfDrData, double lfBoundData, double lfAccuracyData )
	{
//		AbcException cae;

		int i,j;

		iGenerationNumber	= iGenCount;
		iAbcIntervalMinNum	= iIntervalMinNumData;
		iAbcDataNum			= iGenNum;
		iAbcVectorDimNum	= iGenVectorDim;
		iAbcSearchNum		= iSearchNum;
		iAbcLimitCount		= iLimitCountData;
		iAbcUpperSearchNum	= iAlphaData;
		lfConvergenceParam	= lfDrData;
		lfFitBound			= lfBoundData;
		lfFitAccuracy		= lfAccuracyData;

		long seed;
		seed = System.currentTimeMillis();
		rnd = new Sfmt( (int)seed );

		pplfAbcData 		= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfNAbcData 		= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfVelocityData	= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfNVelocityData 	= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfLocalMaxAbcData	= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfLocalMinAbcData	= new double[iAbcDataNum][iAbcVectorDimNum];
		plfFit				= new double[iAbcSearchNum];
		plfFitProb 			= new double[iAbcSearchNum];
		piNonUpdateCount 	= new int[iAbcSearchNum];
		plfVelocity 		= new double[iAbcVectorDimNum];
		plfGlobalMaxAbcData = new double[iAbcVectorDimNum];
		plfGlobalMinAbcData = new double[iAbcVectorDimNum];
		plfLocalMaxAbcData 	= new double[iAbcVectorDimNum];
		plfLocalMinAbcData 	= new double[iAbcVectorDimNum];
		plfCrossOverData 	= new double[iAbcVectorDimNum];
		plfXnew1	 		= new double[iAbcVectorDimNum];
		plfXnew2	 		= new double[iAbcVectorDimNum];
		plfX0 				= new double[iAbcVectorDimNum];
		plfX1		 		= new double[iAbcVectorDimNum];
		plfX2 				= new double[iAbcVectorDimNum];
		plfStepSize	 		= new double[iAbcVectorDimNum];
		plfScoutBeeResult	= new double[iAbcVectorDimNum];

		for( i= 0;i < iAbcDataNum; i++ )
		{
			for(j = 0;j < iAbcVectorDimNum; j++ )
			{
				pplfAbcData[i][j] = 0.0;
				pplfNAbcData[i][j] = 0.0;
				pplfVelocityData[i][j] = 0.0;
				pplfNVelocityData[i][j] = 0.0;
				pplfLocalMaxAbcData[i][j] = 0.0;
				pplfLocalMinAbcData[i][j] = 0.0;
			}
		}
		for( i = 0;i < iAbcSearchNum ; i++ )
		{
			plfFit[i] = 0.0;
			plfFitProb[i] = 0.0;
			piNonUpdateCount[i] = 0;
		}
		for( i = 0;i < iAbcVectorDimNum; i++ )
		{
			plfGlobalMaxAbcData[i] = 0.0;
			plfCrossOverData[i] = 0.0;
			plfVelocity[i] = 0.0;
			plfGlobalMaxAbcData[i] = 0.0;
			plfGlobalMinAbcData[i] = 0.0;
			plfLocalMaxAbcData[i] = 0.0;
			plfLocalMinAbcData[i] = 0.0;
			plfCrossOverData[i] = 0.0;
			plfXnew1[i] = 0.0;
			plfXnew2[i] = 0.0;
			plfX0[i] = 0.0;
			plfX1[i] = 0.0;
			plfX2[i] = 0.0;
			plfStepSize[i] = 0.0;
			plfScoutBeeResult[i] = 0.0;
		}
		pcPowell = new Powell();
		// Powellの初期化を実行します。
		pcPowell.vInitialize( iAbcVectorDimNum );

		// ソート用適応度を格納するベクターです。
		stlFitProb = new ArrayList<Rank_t>();
		Rank_t tempRankt = new Rank_t();
		tempRankt.iLoc = 0;
		tempRankt.lfFitProb = 0.0;
		for( i = 0;i < iAbcSearchNum; i++ )
			stlFitProb.add( tempRankt );
	}

	/**
	 * <PRE>
	 * 　人口蜂コロニーの初期化を実行します。
	 * </PRE>
	 * @param iGenCount       計算回数
	 * @param iGenNum         コロニー数
	 * @param iGenVectorDim   探索点の次元数
	 * @param iSearchNum  　  探索点の数
	 * @param iLimitCountData 更新しなかった回数
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public void vInitialize( int iGenCount, int iGenNum, int iGenVectorDim, int iSearchNum, int iLimitCountData )
	{
//		CAbcException cae;

		int i,j;

		iGenerationNumber	= iGenCount;
		iAbcDataNum			= iGenNum;
		iAbcVectorDimNum	= iGenVectorDim;
		iAbcSearchNum		= iSearchNum;
		iAbcLimitCount		= iLimitCountData;
		long seed;
		seed = System.currentTimeMillis();
		rnd = new Sfmt( (int)seed );

		pplfAbcData 		= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfNAbcData 		= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfVelocityData	= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfNVelocityData 	= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfLocalMaxAbcData	= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfLocalMinAbcData	= new double[iAbcDataNum][iAbcVectorDimNum];
		plfFit			= new double[iAbcSearchNum];
		plfFitProb 		= new double[iAbcSearchNum];
		piNonUpdateCount 	= new int[iAbcSearchNum];
		plfVelocity 		= new double[iAbcVectorDimNum];
		plfGlobalMaxAbcData = new double[iAbcVectorDimNum];
		plfGlobalMinAbcData = new double[iAbcVectorDimNum];
		plfLocalMaxAbcData 	= new double[iAbcVectorDimNum];
		plfLocalMinAbcData 	= new double[iAbcVectorDimNum];
		plfCrossOverData 	= new double[iAbcVectorDimNum];

		for( i= 0;i < iAbcDataNum; i++ )
		{
			for(j = 0;j < iAbcVectorDimNum; j++ )
			{
				pplfAbcData[i][j] = 0.0;
				pplfNAbcData[i][j] = 0.0;
				pplfVelocityData[i][j] = 0.0;
				pplfNVelocityData[i][j] = 0.0;
				pplfLocalMaxAbcData[i][j] = 0.0;
				pplfLocalMinAbcData[i][j] = 0.0;
			}
		}
		for( i = 0;i < iAbcSearchNum ; i++ )
		{
			plfFit[i] = 0.0;
			plfFitProb[i] = 0.0;
			piNonUpdateCount[i] = 0;
		}
		for( i = 0;i < iAbcVectorDimNum; i++ )
		{
			plfVelocity[i] = 0.0;
			plfGlobalMaxAbcData[i] = 0.0;
			plfGlobalMinAbcData[i] = 0.0;
			plfLocalMaxAbcData[i] = 0.0;
			plfLocalMinAbcData[i] = 0.0;
			plfCrossOverData[i] = 0.0;
		}
	}

	/**
	 * <PRE>
	 * 　人口蜂コロニーの初期化を実行します。
	 * </PRE>
	 * @param iGenCount      	 計算回数
	 * @param iGenNum        	 コロニー数
	 * @param iGenVectorDim  	 探索点の次元数
	 * @param iSearchNum  　 	 探索点の数
	 * @param iLimitCountData	 更新しなかった回数
	 * @param iCrossOverNumData	 交叉回数
	 * @param lfAlphaData		 UNDXにおけるAlpha
	 * @param lfBetaData		 UNDXにおけるBeta
	 * @author kobayashi
	 * @since 2015/8/10
	 * @version 0.1
	 */
	public void vInitialize( int iGenCount, int iGenNum, int iGenVectorDim, int iSearchNum, int iLimitCountData, int iCrossOverNumData, double lfAlphaData, double lfBetaData )
	{
		int i,j;

		iGenerationNumber	= iGenCount;
		iAbcDataNum		= iGenNum;
		iAbcVectorDimNum	= iGenVectorDim;
		iAbcSearchNum		= iSearchNum;
		iAbcLimitCount		= iLimitCountData;

		long seed;
		seed = System.currentTimeMillis();
		rnd = new Sfmt( (int)seed );

		pplfAbcData 		= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfNAbcData 		= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfVelocityData	= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfNVelocityData 	= new double[iAbcVectorDimNum][iAbcVectorDimNum];
		pplfLocalMaxAbcData 	= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfLocalMinAbcData 	= new double[iAbcDataNum][iAbcVectorDimNum];
		plfFit			= new double[iAbcSearchNum];
		plfFitProb 		= new double[iAbcSearchNum];
		piNonUpdateCount 	= new int[iAbcSearchNum];
		piTotalNonUpdateCount 	= new int[iAbcSearchNum];
		plfVelocity 		= new double[iAbcVectorDimNum];
		plfGlobalMaxAbcData 	= new double[iAbcVectorDimNum];
		plfGlobalMinAbcData 	= new double[iAbcVectorDimNum];
		plfLocalMaxAbcData 	= new double[iAbcVectorDimNum];
		plfLocalMinAbcData 	= new double[iAbcVectorDimNum];
		plfCrossOverData 	= new double[iAbcVectorDimNum];
		plfXnew1	 	= new double[iAbcVectorDimNum];
		plfXnew2	 	= new double[iAbcVectorDimNum];
		plfX0 			= new double[iAbcVectorDimNum];
		plfX1		 	= new double[iAbcVectorDimNum];
		plfX2 			= new double[iAbcVectorDimNum];
		plfStepSize	 	= new double[iAbcVectorDimNum];
		plfScoutBeeResult	= new double[iAbcVectorDimNum];

		for( i= 0;i < iAbcDataNum; i++ )
		{
			for(j = 0;j < iAbcVectorDimNum; j++ )
			{
				pplfAbcData[i][j] = 0.0;
				pplfNAbcData[i][j] = 0.0;
				pplfLocalMaxAbcData[i][j] = 0.0;
				pplfLocalMinAbcData[i][j] = 0.0;
				pplfVelocityData[i][j] = 0.0;
			}
		}
		for( i = 0;i < iAbcSearchNum ; i++ )
		{
			plfFit[i] = 0.0;
			plfFitProb[i] = 0.0;
			piNonUpdateCount[i] = 0;
			piTotalNonUpdateCount[i] = 0;
		}
		for( i = 0;i < iAbcVectorDimNum; i++ )
		{
			plfVelocity[i] = 0.0;
			plfGlobalMaxAbcData[i] = 0.0;
			plfGlobalMinAbcData[i] = 0.0;
			plfLocalMaxAbcData[i] = 0.0;
			plfLocalMinAbcData[i] = 0.0;
			plfCrossOverData[i] = 0.0;
			plfXnew1[i] = 0.0;
			plfXnew2[i] = 0.0;
			plfX0[i] = 0.0;
			plfX1[i] = 0.0;
			plfX2[i] = 0.0;
			plfStepSize[i] = 0.0;
			plfScoutBeeResult[i] = 0.0;
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfNVelocityData[i][j] = 0.0;
			}
		}
		pcUndx = new CUndx();
		iCrossOverNum = iCrossOverNumData;
		lfAlpha = lfAlphaData;
		lfBeta = lfBetaData;
		// UNDXの初期化を実行します。
		pcUndx.vInitialize( iGenerationNumber, iAbcDataNum, iAbcVectorDimNum, iCrossOverNum );
		pcUndx.vSetAlpha( lfAlpha );
		pcUndx.vSetBeta( lfBeta );
		pcPowell = new Powell();
		// Powellの初期化を実行します。
		pcPowell.vInitialize(iAbcVectorDimNum);

		// ソート用適応度を格納するベクターです。
		stlFitProb = new ArrayList<Rank_t>();
		Rank_t tempRankt = new Rank_t();
		tempRankt.iLoc = 0;
		tempRankt.lfFitProb = 0.0;
		for( i = 0;i < iAbcSearchNum; i++ )
			stlFitProb.add( tempRankt );
	}

	/**
	 * <PRE>
	 * 　人口蜂コロニーの初期化を実行します。
	 * </PRE>
	 * @param iGenCount      		 計算回数
	 * @param iGenNum        		 コロニー数
	 * @param iGenVectorDim  		 探索点の次元数
	 * @param iSearchNum  	 		 探索点の数
	 * @param iLimitCountData		 更新しなかった回数
	 * @param iIntervalMinNumData	 最低反復回数
	 * @param iAlphaData    		 探索点上位数
	 * @param lfDrData      		 収束状況判定
	 * @param lfBoundData   		 適合度許容限界値
	 * @param lfAccuracyData		 適合度評価精度
	 * @param iCrossOverNumData		 交叉回数
	 * @param lfAlphaData			 UNDXにおけるAlpha
	 * @param lfBetaData			 UNDXにおけるBeta
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public void vInitialize( int iGenCount, int iGenNum, int iGenVectorDim, int iSearchNum, int iLimitCountData, int iIntervalMinNumData, int iAlphaData, double lfDrData, double lfBoundData, double lfAccuracyData, int iCrossOverNumData, double lfAlphaData, double lfBetaData  )
	{

		int i,j;

		iGenerationNumber	= iGenCount;
		iAbcIntervalMinNum	= iIntervalMinNumData;
		iAbcDataNum		= iGenNum;
		iAbcVectorDimNum	= iGenVectorDim;
		iAbcSearchNum		= iSearchNum;
		iAbcLimitCount		= iLimitCountData;
		iAbcUpperSearchNum	= iAlphaData;
		lfConvergenceParam	= lfDrData;
		lfFitBound		= lfBoundData;
		lfFitAccuracy		= lfAccuracyData;

		long seed;
		seed = System.currentTimeMillis();
		rnd = new Sfmt( (int)seed );

		pplfAbcData 		= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfNAbcData 		= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfVelocityData	= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfNVelocityData 	= new double[iAbcVectorDimNum][iAbcVectorDimNum];
		pplfLocalMaxAbcData 	= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfLocalMinAbcData 	= new double[iAbcDataNum][iAbcVectorDimNum];
		plfFit			= new double[iAbcSearchNum];
		plfFitProb 		= new double[iAbcSearchNum];
		piNonUpdateCount 	= new int[iAbcSearchNum];
		plfVelocity 		= new double[iAbcVectorDimNum];
		plfGlobalMaxAbcData 	= new double[iAbcVectorDimNum];
		plfGlobalMinAbcData 	= new double[iAbcVectorDimNum];
		plfLocalMaxAbcData 	= new double[iAbcVectorDimNum];
		plfLocalMinAbcData 	= new double[iAbcVectorDimNum];
		plfCrossOverData 	= new double[iAbcVectorDimNum];
		plfXnew1	 	= new double[iAbcVectorDimNum];
		plfXnew2	 	= new double[iAbcVectorDimNum];
		plfX0 			= new double[iAbcVectorDimNum];
		plfX1		 	= new double[iAbcVectorDimNum];
		plfX2 			= new double[iAbcVectorDimNum];
		plfStepSize	 	= new double[iAbcVectorDimNum];
		plfScoutBeeResult	= new double[iAbcVectorDimNum];

		for( i= 0;i < iAbcDataNum; i++ )
		{
			for(j = 0;j < iAbcVectorDimNum; j++ )
			{
				pplfAbcData[i][j] = 0.0;
				pplfNAbcData[i][j] = 0.0;
				pplfLocalMaxAbcData[i][j] = 0.0;
				pplfLocalMinAbcData[i][j] = 0.0;
				pplfVelocityData[i][j] = 0.0;
			}
		}
		for( i = 0;i < iAbcSearchNum ; i++ )
		{
			plfFit[i] = 0.0;
			plfFitProb[i] = 0.0;
			piNonUpdateCount[i] = 0;
			piTotalNonUpdateCount[i] = 0;
		}
		for( i = 0;i < iAbcVectorDimNum; i++ )
		{
			plfVelocity[i] = 0.0;
			plfGlobalMaxAbcData[i] = 0.0;
			plfGlobalMinAbcData[i] = 0.0;
			plfLocalMaxAbcData[i] = 0.0;
			plfLocalMinAbcData[i] = 0.0;
			plfCrossOverData[i] = 0.0;
			plfXnew1[i] = 0.0;
			plfXnew2[i] = 0.0;
			plfX0[i] = 0.0;
			plfX1[i] = 0.0;
			plfX2[i] = 0.0;
			plfStepSize[i] = 0.0;
			plfScoutBeeResult[i] = 0.0;
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfNVelocityData[i][j] = 0.0;
			}
		}
		pcUndx = new CUndx();
		iCrossOverNum = iCrossOverNumData;
		lfAlpha = lfAlphaData;
		lfBeta = lfBetaData;
		// UNDXの初期化を実行します。
		pcUndx.vInitialize( iGenerationNumber, iAbcDataNum, iAbcVectorDimNum, iCrossOverNum );
		pcUndx.vSetAlpha( lfAlpha );
		pcUndx.vSetBeta( lfBeta );

		pcPowell = new Powell();
		// Powellの初期化を実行します。
		pcPowell.vInitialize(iAbcVectorDimNum);

		// ソート用適応度を格納するベクターです。
		stlFitProb = new ArrayList<Rank_t>();
		Rank_t tempRankt = new Rank_t();
		tempRankt.iLoc = 0;
		tempRankt.lfFitProb = 0.0;
		for( i = 0;i < iAbcSearchNum; i++ )
			stlFitProb.add( tempRankt );

	}

	/**
	 * <PRE>
	 * 　人工蜂コロニーの初期化を実行します。
	 * </PRE>
	 * @param iGenCount       			計算回数
	 * @param iGenNum         			コロニー数
	 * @param iGenVectorDim   			探索点の次元数
	 * @param iSearchNum  　  			探索点の数
	 * @param iLimitCountData 			更新しなかった回数
	 * @param iIntervalMinNumData		最低反復回数
	 * @param iAlphaData      			探索点上位数
	 * @param lfDrData       			収束状況判定
	 * @param lfBoundData    			適合度許容限界値
	 * @param lfAccuracyData  			適合度評価精度
	 * @param iParentNum				親の数
	 * @param iChildrenNum				子供の数
	 * @param iUpperEvalChildrenNum		子供で残す上位の数
	 * @param lfLearningRateData	 	学習率
	 * @author kobayashi
	 * @since 2016/8/26
	 * @version 0.1
	 */
	public void vInitialize( int iGenCount, int iGenNum, int iGenVectorDim, int iSearchNum, int iLimitCountData, int iIntervalMinNumData, int iAlphaData, double lfDrData, double lfBoundData, double lfAccuracyData, int iParentNum, int iChildrenNum, int iUpperEvalChildrenNum, double lfLearningRateData  )
	{
		int i,j;

		iGenerationNumber		= iGenCount;
		iAbcIntervalMinNum		= iIntervalMinNumData;
		iAbcDataNum			= iGenNum;
		iAbcVectorDimNum		= iGenVectorDim;
		iAbcSearchNum			= iSearchNum;
		iAbcLimitCount			= iLimitCountData;
		iAbcUpperSearchNum		= iAlphaData;
		lfConvergenceParam		= lfDrData;
		lfFitBound			= lfBoundData;
		lfFitAccuracy			= lfAccuracyData;
		iParentNumber			= iParentNum;			// Rex用選択する親の数
		iChildrenNumber			= iChildrenNum;			// Rex用生成する子供の数
		iUpperEvalChildrenNumber	= iUpperEvalChildrenNum;	// ARex用生成した子供の上位を選択する数
		lfLearningRate			= lfLearningRateData;		// ARex用学習率

		long seed;
		seed = System.currentTimeMillis();
		rnd = new Sfmt( (int)seed );

		pplfAbcData 			= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfNAbcData 			= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfVelocityData		= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfNVelocityData 		= new double[iAbcVectorDimNum][iAbcVectorDimNum];
		pplfLocalMaxAbcData 	= new double[iAbcDataNum][iAbcVectorDimNum];
		pplfLocalMinAbcData 	= new double[iAbcDataNum][iAbcVectorDimNum];
		plfFit					= new double[iAbcSearchNum];
		plfFitProb 				= new double[iAbcSearchNum];
		piNonUpdateCount 		= new int[iAbcSearchNum];
		piTotalNonUpdateCount	= new int[iAbcSearchNum];
		plfVelocity 			= new double[iAbcVectorDimNum];
		plfGlobalMaxAbcData		= new double[iAbcVectorDimNum];
		plfGlobalMinAbcData		= new double[iAbcVectorDimNum];
		plfLocalMaxAbcData 		= new double[iAbcVectorDimNum];
		plfLocalMinAbcData 		= new double[iAbcVectorDimNum];
		plfCrossOverData 		= new double[iAbcVectorDimNum];
		plfXnew1	 			= new double[iAbcVectorDimNum];
		plfXnew2	 			= new double[iAbcVectorDimNum];
		plfX0 					= new double[iAbcVectorDimNum];
		plfX1		 			= new double[iAbcVectorDimNum];
		plfX2 					= new double[iAbcVectorDimNum];
		plfStepSize	 			= new double[iAbcVectorDimNum];
		plfScoutBeeResult		= new double[iAbcVectorDimNum];

		for( i= 0;i < iAbcDataNum; i++ )
		{
			for(j = 0;j < iAbcVectorDimNum; j++ )
			{
				pplfAbcData[i][j] = 0.0;
				pplfNAbcData[i][j] = 0.0;
				pplfLocalMaxAbcData[i][j] = 0.0;
				pplfLocalMinAbcData[i][j] = 0.0;
				pplfVelocityData[i][j] = 0.0;
			}
		}
		for( i = 0;i < iAbcSearchNum ; i++ )
		{
			plfFit[i] = 0.0;
			plfFitProb[i] = 0.0;
			piNonUpdateCount[i] = 0;
			piTotalNonUpdateCount[i] = 0;
		}
		for( i = 0;i < iAbcVectorDimNum; i++ )
		{
			plfVelocity[i] = 0.0;
			plfGlobalMaxAbcData[i] = 0.0;
			plfGlobalMinAbcData[i] = 0.0;
			plfLocalMaxAbcData[i] = 0.0;
			plfLocalMinAbcData[i] = 0.0;
			plfCrossOverData[i] = 0.0;
			plfXnew1[i] = 0.0;
			plfXnew2[i] = 0.0;
			plfX0[i] = 0.0;
			plfX1[i] = 0.0;
			plfX2[i] = 0.0;
			plfStepSize[i] = 0.0;
			plfScoutBeeResult[i] = 0.0;
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfNVelocityData[i][j] = 0.0;
			}
		}
		pcRex = new CRex();
		// UNDXの初期化を実行します。
		pcRex.vInitialize( iGenerationNumber, iAbcDataNum, iAbcVectorDimNum, iParentNumber, iChildrenNumber, lfLearningRate, iUpperEvalChildrenNumber );
		pcPowell = new Powell();
		// Powellの初期化を実行します。
		pcPowell.vInitialize(iAbcVectorDimNum);

		// ソート用適応度を格納するベクターです。
//		stlFitProb.assign( iAbcSearchNum, Rank_t() );
		stlFitProb = new ArrayList<Rank_t>();
		Rank_t tempRankt = new Rank_t();
		tempRankt.iLoc = 0;
		tempRankt.lfFitProb = 0.0;
		for( i = 0;i < iAbcSearchNum; i++ )
			stlFitProb.add( tempRankt );
	}

	/**
	* <PRE>
	* 　人工蜂コロニーの初期位置を1に設定します。
	* </PRE>
	* @author kobayashi
	* @since 2017/4/4
	* @version 0.1
	*/
	public void vSetData()
	{
		int i, j;
		double lfMin = Double.MAX_VALUE;
		int iMinLoc = 0;
		double lfObjFunc = 0.0;
		for (i = 0; i < iAbcDataNum; i++)
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfAbcData[i][j] = 1.0;
				pplfVelocityData[i][j] = 1.0;
				pplfLocalMaxAbcData[i][j] = pplfAbcData[i][j];
			}
		}
		// 初期の状態で最適値を取得します。
		// 更新します。
		for (i = 0; i < iAbcSearchNum; i++)
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[i]);
			if (lfGlobalMinAbcData >= lfObjFunc)
			{
				iMinLoc = i;
				lfGlobalMinAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
				{
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
				}
			}
			if (lfGlobalMaxAbcData <= lfObjFunc)
			{
				lfGlobalMaxAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
				{
					plfGlobalMaxAbcData[j] = pplfAbcData[i][j];
				}
			}
		}
	}

	/**
	 * <PRE>
	 * 　人工蜂コロニーの初期位置を中心0の半径range内で一様乱数により設定します。
	 * </PRE>
	 * @param lfRange 粒子の初期位置の出現範囲
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public void vSetRandom( double lfRange )
	{
		int i,j;
		double lfMin = Double.MAX_VALUE;
		int iMinLoc = 0;
		double lfObjFunc = 0.0;
		for( i= 0;i < iAbcDataNum; i++ )
		{
			for(j = 0;j < iAbcVectorDimNum; j++ )
			{
				pplfAbcData[i][j] = lfRange*(2.0*rnd.NextUnif()-1.0);
				pplfVelocityData[i][j] = lfRange*(2.0*rnd.NextUnif()-1.0);
				if( pplfAbcData[i][j] >= 0.0 && pplfVelocityData[i][j] >= 0.0 ) break;
			}
		}
		lfSolveRange = lfRange;

		// 初期の状態で最適値を取得します。
		lfGlobalMinAbcData = Double.MAX_VALUE;
		lfGlobalMaxAbcData = -Double.MAX_VALUE;
		// 更新します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );
			if( lfGlobalMinAbcData >= lfObjFunc )
			{
				lfGlobalMinAbcData = lfObjFunc;
				for( j = 0; j < iAbcVectorDimNum; j++ )
				{
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
				}
			}
			if( lfGlobalMaxAbcData <= lfObjFunc )
			{
				lfGlobalMaxAbcData = lfObjFunc;
				for( j = 0; j < iAbcVectorDimNum; j++ )
				{
					plfGlobalMaxAbcData[j] = pplfAbcData[i][j];
				}
			}
		}
	}

	/**
	 * <PRE>
	 * 　人工蜂コロニーの初期位置を中心0の半径range内で一様乱数により設定します。
	 * </PRE>
	 * @param lfRangeMin 粒子の初期位置の出現範囲（最小値）
	 * @param lfRangeMax 粒子の初期位置の出現範囲（最大値）
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public void vSetRandom( double lfRangeMin, double lfRangeMax )
	{
		int i,j;
		double lfMin = Double.MAX_VALUE;
		int iMinLoc = 0;
		double lfObjFunc = 0.0;
		for( i= 0;i < iAbcDataNum; i++ )
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfAbcData[i][j] = (lfRangeMax-lfRangeMin)*rnd.NextUnif() + lfRangeMin;
				pplfVelocityData[i][j] = (lfRangeMax-lfRangeMin)*rnd.NextUnif() + lfRangeMin;
				pplfLocalMaxAbcData[i][j] = pplfAbcData[i][j];
			}
		}
		lfSolveRange = lfRangeMax-lfRangeMin;
		lfSolveRangeMin = lfRangeMin;
		lfSolveRangeMax = lfRangeMax;

		// 初期の状態で最適値を取得します。
		lfMin = Double.MAX_VALUE;
		// 更新します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );
			if( lfGlobalMinAbcData >= lfObjFunc )
			{
				iMinLoc = Integer.MAX_VALUE;
				lfGlobalMinAbcData = lfObjFunc;
				for( j = 0; j < iAbcVectorDimNum; j++ )
				{
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
				}
			}
			if( lfGlobalMaxAbcData <= lfObjFunc )
			{
				lfGlobalMaxAbcData = lfObjFunc;
				for( j = 0; j < iAbcVectorDimNum; j++ )
				{
					plfGlobalMaxAbcData[j] = pplfAbcData[i][j];
				}
			}
		}
	}

	/**
	 * <PRE>
	 * 　人工蜂コロニーの初期位置を中心0の半径range内で一様乱数により設定します。
	 * </PRE>
	 * @param lfRange 粒子の初期位置の出現範囲
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public void vSetModifiedRandom( double lfRange )
	{
		int i,j;
		double lfObjFunc = 0.0;
		for( i= 0;i < iAbcDataNum; i++ )
		{
			for(j = 0;j < iAbcVectorDimNum; j++ )
			{
				pplfAbcData[i][j] = lfRange*(2.0*rnd.NextUnif()-1.0);
				pplfVelocityData[i][j] = lfRange*(2.0*rnd.NextUnif()-1.0);
			}
		}
		lfSolveRange = lfRange;

		// 初期の状態で最適値を取得します。
		lfGlobalMinAbcData = Double.MAX_VALUE;
		lfGlobalMaxAbcData = -Double.MAX_VALUE;
		// 更新します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );
			if( lfGlobalMinAbcData >= lfObjFunc )
			{
				lfGlobalMinAbcData = lfObjFunc;
				for( j = 0; j < iAbcVectorDimNum; j++ )
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
				lfFitCurrentBest = lfGlobalMinAbcData;
			}
			if( lfGlobalMaxAbcData <= lfObjFunc )
			{
				lfGlobalMaxAbcData = lfObjFunc;
				for( j = 0; j < iAbcVectorDimNum; j++ )
					plfGlobalMaxAbcData[j] = pplfAbcData[i][j];
//				lfFitCurrentBest = lfMin;
			}
		}

		lfObjFunc = 0.0;
		// 初期状態における前探索点の平均評価値を算出します。
		for( i = 0;i < iAbcSearchNum; i++ )
			lfObjFunc += pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );
		lfFitInit = lfObjFunc/(double)iAbcSearchNum;
	}

	/**
	 * <PRE>
	 * 　人工蜂コロニーの初期位置を中心0の半径range内で一様乱数により設定します。
	 * </PRE>
	 * @param lfRangeMin 粒子の初期位置の出現範囲（最小値）
	 * @param lfRangeMax 粒子の初期位置の出現範囲（最大値）
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public void vSetModifiedRandom( double lfRangeMin, double lfRangeMax )
	{
		int i,j;
		double lfMin = Double.MAX_VALUE;
		int iMinLoc = 0;
		double lfObjFunc = 0.0;
		for( i= 0;i < iAbcDataNum; i++ )
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfAbcData[i][j] = (lfRangeMax-lfRangeMin)*rnd.NextUnif() + lfRangeMin;
				pplfVelocityData[i][j] = (lfRangeMax-lfRangeMin)*rnd.NextUnif() + lfRangeMin;
				pplfLocalMaxAbcData[i][j] = pplfAbcData[i][j];
			}
		}
		lfSolveRange = lfRangeMax-lfRangeMin;
		lfSolveRangeMin = lfRangeMin;
		lfSolveRangeMax = lfRangeMax;

		// 初期の状態で最適値を取得します。
		lfMin = Double.MAX_VALUE;
		// 更新します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );
			if( lfGlobalMinAbcData >= lfObjFunc )
			{
				iMinLoc = Integer.MAX_VALUE;
				lfGlobalMinAbcData = lfObjFunc;
				for( j = 0; j < iAbcVectorDimNum; j++ )
				{
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
				}
				lfFitCurrentBest = lfGlobalMinAbcData;
			}
			if( lfGlobalMaxAbcData <= lfObjFunc )
			{
				lfGlobalMaxAbcData = lfObjFunc;
				for( j = 0; j < iAbcVectorDimNum; j++ )
				{
					plfGlobalMaxAbcData[j] = pplfAbcData[i][j];
				}
//				lfFitCurrentBest = lfMin;
			}
		}

		lfObjFunc = 0.0;
		// 初期状態における前探索点の平均評価値を算出します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfObjFunc += pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );
		}
		lfFitInit = lfObjFunc/(double)iAbcSearchNum;
	}

	/**
	 * <PRE>
	 *    ABC法の初期設定を行います。
	 *    具体的には初期状態での最大あるいは最小値を求めます。
	 * </PRE>
	 */
	public void vInitialSet()
	{
		int i,j;
		double lfObjFunc = 0.0;

		// 初期の状態で最適値を取得します。
		lfGlobalMinAbcData = Double.MAX_VALUE;
		lfGlobalMaxAbcData = -Double.MAX_VALUE;
		// 更新します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );
			if( lfGlobalMinAbcData >= lfObjFunc && lfObjFunc >= 0.0 )
			{
				lfGlobalMinAbcData = lfObjFunc;
				for( j = 0; j < iAbcVectorDimNum; j++ )
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
				lfFitCurrentBest = lfObjFunc;
			}
			if( lfGlobalMaxAbcData <= lfObjFunc && lfObjFunc >= 0.0 )
			{
				lfGlobalMaxAbcData = lfObjFunc;
				for( j = 0; j < iAbcVectorDimNum; j++ )
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
				lfFitCurrentBest = lfGlobalMaxAbcData;
			}
		}

		lfObjFunc = 0.0;
		// 初期状態における前探索点の平均評価値を算出します。
		for( i = 0;i < iAbcSearchNum; i++ )
			lfObjFunc += pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );

		pcPowell = new Powell();
		// Powellの初期化を実行します。
		pcPowell.vInitialize( iAbcVectorDimNum );

		// ソート用適応度を格納するベクターです。
//		stlFitProb.assign( iAbcSearchNum, Rank_t() );
		lfFitInit = lfObjFunc/(double)iAbcSearchNum;
	}

	/**
	* <PRE>
	* 　人工蜂コロニーの初期位置を粒子群最適化法に基づいた手法により
	*   算出して設定します。
	* </PRE>
	* @param lfRange 粒子の初期位置の出現範囲
	* @author kobayashi
	* @since 2016/11/07
	* @version 0.1
	*/
	public void vSetRandomPso(double lfRange)
	{
		int i, j;
		double lfObjFunc = 0.0;
		double lfRand;
		for (i = 0; i < iAbcDataNum; i++)
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfAbcData[i][j] = lfRange*(2.0*rnd.NextUnif() - 1.0);
				pplfVelocityData[i][j] = lfRange*(2.0*rnd.NextUnif() - 1.0);
				pplfLocalMaxAbcData[i][j] = pplfAbcData[i][j];
			}
		}
		lfSolveRange = lfRange;

		// 初期の状態で最適値を取得します。
		lfGlobalMinAbcData = Double.MAX_VALUE;
		lfGlobalMaxAbcData = -Double.MAX_VALUE;
		// 更新します。
		for (i = 0; i < iAbcSearchNum; i++)
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[i]);
			if (lfGlobalMinAbcData >= lfObjFunc)
			{
				lfGlobalMinAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
				{
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
				}
			}
			if (lfGlobalMaxAbcData <= lfObjFunc)
			{
				lfGlobalMaxAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
				{
					plfGlobalMaxAbcData[j] = pplfAbcData[i][j];
				}
			}
		}
		// 粒子群最適化法のような感じで初期化します。(ScoutBeeの更新方法を初期化に適用。)
		for (i = 0; i < iAbcSearchNum; i++)
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				lfRand = rnd.NextUnif();
				pplfAbcData[i][j] = plfGlobalMinAbcData[j] + lfRand*(plfGlobalMaxAbcData[j] - plfGlobalMinAbcData[j]);
			}
		}
	}

	/**
	* <PRE>
	* 　人工蜂コロニーの初期位置を粒子群最適化法に基づいた手法により
	*   算出して設定します。
	* </PRE>
	 * @param lfRangeMin 粒子の初期位置の出現範囲（最小値）
	 * @param lfRangeMax 粒子の初期位置の出現範囲（最大値）
	* @author kobayashi
	* @since 2016/11/07
	* @version 0.1
	*/
	public void vSetRandomPso(double lfRangeMin, double lfRangeMax)
	{
		int i, j;
		double lfMin = Double.MAX_VALUE;
		int iMinLoc = 0;
		double lfObjFunc = 0.0;
		double lfRand;
		for (i = 0; i < iAbcDataNum; i++)
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfAbcData[i][j] = (lfRangeMax-lfRangeMin)*rnd.NextUnif() + lfRangeMin;
				pplfVelocityData[i][j] = (lfRangeMax-lfRangeMin)*rnd.NextUnif() + lfRangeMin;
				pplfLocalMaxAbcData[i][j] = pplfAbcData[i][j];
			}
		}
		lfSolveRange = lfRangeMax-lfRangeMin;
		lfSolveRangeMin = lfRangeMin;
		lfSolveRangeMax = lfRangeMax;

		// 初期の状態で最適値を取得します。
		// 更新します。
		for (i = 0; i < iAbcSearchNum; i++)
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[i]);
			if (lfGlobalMinAbcData >= lfObjFunc)
			{
				iMinLoc = Integer.MAX_VALUE;
				lfGlobalMinAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
				{
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
				}
			}
			if (lfGlobalMaxAbcData <= lfObjFunc)
			{
				lfGlobalMaxAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
				{
					plfGlobalMaxAbcData[j] = pplfAbcData[i][j];
				}
			}
		}
		// 粒子群最適化法のような感じで初期化します。(ScoutBeeの更新方法を初期化に適用。)
		for (i = 0; i < iAbcSearchNum; i++)
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				lfRand = rnd.NextUnif();
				pplfAbcData[i][j] = plfGlobalMinAbcData[j] + lfRand*(plfGlobalMaxAbcData[j] - plfGlobalMinAbcData[j]);
			}
		}
	}

	/**
	* <PRE>
	* 　人工蜂コロニーの初期位置をUNDXを用いて算出して設定します。
	* </PRE>
	* @param lfRange 粒子の初期位置の出現範囲
	* @author kobayashi
	* @since 2016/11/07
	* @version 0.1
	*/
	public void vSetRandomUndx(double lfRange)
	{
		int i, j;
		double lfObjFunc = 0.0;
		for (i = 0; i < iAbcDataNum; i++)
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfAbcData[i][j] = lfRange*(2.0*rnd.NextUnif() - 1.0);
				pplfVelocityData[i][j] = lfRange*(2.0*rnd.NextUnif() - 1.0);
				pplfLocalMaxAbcData[i][j] = pplfAbcData[i][j];
			}
		}
		lfSolveRange = lfRange;

		// 初期の状態で最適値を取得します。
		lfGlobalMinAbcData = Double.MAX_VALUE;
		lfGlobalMaxAbcData = -Double.MAX_VALUE;
		// 更新します。
		for (i = 0; i < iAbcSearchNum; i++)
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[i]);
			if (lfGlobalMinAbcData >= lfObjFunc)
			{
				lfGlobalMinAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
				{
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
				}
			}
			if (lfGlobalMaxAbcData <= lfObjFunc)
			{
				lfGlobalMaxAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
				{
					plfGlobalMaxAbcData[j] = pplfAbcData[i][j];
				}
			}
		}
		// 初期化をUNDXを用いて実行します。（１回分のみ。）
		pcUndx.vSetGenData(pplfAbcData);
		pcUndx.vImplement();
//		pcUndx.vGetBestGenData(pplfAbcData[i] );
		pcUndx.vGetGenData(pplfAbcData);
	}

	/**
	* <PRE>
	* 　人工蜂コロニーの初期位置をUNDXを用いて算出して設定します。
	* </PRE>
	 * @param lfRangeMin 粒子の初期位置の出現範囲（最小値）
	 * @param lfRangeMax 粒子の初期位置の出現範囲（最大値）
	* @author kobayashi
	* @since 2016/11/07
	* @version 0.1
	*/
	public void vSetRandomUndx(double lfRangeMin, double lfRangeMax )
	{
		int i, j;
		double lfMin = Double.MAX_VALUE;
		int iMinLoc = 0;
		double lfObjFunc = 0.0;
		for (i = 0; i < iAbcDataNum; i++)
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfAbcData[i][j] = (lfRangeMax-lfRangeMin)*rnd.NextUnif() + lfRangeMin;
				pplfVelocityData[i][j] = (lfRangeMax-lfRangeMin)*rnd.NextUnif() + lfRangeMin;
				pplfLocalMaxAbcData[i][j] = pplfAbcData[i][j];
			}
		}
		lfSolveRange = lfRangeMax-lfRangeMin;
		lfSolveRangeMin = lfRangeMin;
		lfSolveRangeMax = lfRangeMax;

		// 初期の状態で最適値を取得します。
		// 更新します。
		for (i = 0; i < iAbcSearchNum; i++)
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[i]);
			if (lfGlobalMinAbcData >= lfObjFunc)
			{
				iMinLoc = Integer.MAX_VALUE;
				lfGlobalMinAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
			}
			if (lfGlobalMaxAbcData <= lfObjFunc)
			{
				lfGlobalMaxAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
				{
					plfGlobalMaxAbcData[j] = pplfAbcData[i][j];
				}
			}
		}
		// 初期化をUNDXを用いて実行します。（１回分のみ。）
		pcUndx.vSetGenData(pplfAbcData);
		pcUndx.vImplement();
//		pcUndx.vGetBestGenData(pplfAbcData[i] );
		pcUndx.vGetGenData(pplfAbcData);
	}

	/**
	* <PRE>
	* 　人工蜂コロニーの初期位置をREX法を適用して設定します。
	* </PRE>
	* @param lfRange 粒子の初期位置の出現範囲
	* @author kobayashi
	* @since 2016/11/07
	* @version 0.1
	*/
	public void vSetRandomRex(double lfRange)
	{
		int i, j;
		double lfObjFunc = 0.0;
		for (i = 0; i < iAbcDataNum; i++)
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfAbcData[i][j] = lfRange*(2.0*rnd.NextUnif() - 1.0);
				pplfVelocityData[i][j] = lfRange*(2.0*rnd.NextUnif() - 1.0);
				pplfLocalMaxAbcData[i][j] = pplfAbcData[i][j];
			}
		}
		lfSolveRange = lfRange;

		// 初期の状態で最適値を取得します。
		lfGlobalMinAbcData = Double.MAX_VALUE;
		lfGlobalMaxAbcData = -Double.MAX_VALUE;
		// 更新します。
		for (i = 0; i < iAbcSearchNum; i++)
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[i]);
			if (lfGlobalMinAbcData >= lfObjFunc)
			{
				lfGlobalMinAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
				{
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
				}
			}
			if (lfGlobalMaxAbcData <= lfObjFunc)
			{
				lfGlobalMaxAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
				{
					plfGlobalMaxAbcData[j] = pplfAbcData[i][j];
				}
			}
		}
		// 交叉を実行します。ここでREXを実行します。（１回分のみ。）
		pcRex.vSetGenData(pplfAbcData);
		pcRex.vRex();
//		pcRex.vGetBestGenData(pplfAbcData[i] );
//		pcRex.vGetBest2ndGenData(pplfAbcData );
		pcRex.vGetGenData(pplfAbcData);
	}

	/**
	* <PRE>
	* 　人工蜂コロニーの初期位置をREX法を適用して設定します。
	* </PRE>
	 * @param lfRangeMin 粒子の初期位置の出現範囲（最小値）
	 * @param lfRangeMax 粒子の初期位置の出現範囲（最大値）
	* @author kobayashi
	* @since 2016/11/07
	* @version 0.1
	*/
	public void vSetRandomRex(double lfRangeMin, double lfRangeMax )
	{
		int i, j;
		double lfMin = Double.MAX_VALUE;
		int iMinLoc = 0;
		double lfObjFunc = 0.0;
		for (i = 0; i < iAbcDataNum; i++)
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfAbcData[i][j] = (lfRangeMax-lfRangeMin)*rnd.NextUnif() + lfRangeMin;
				pplfVelocityData[i][j] = (lfRangeMax-lfRangeMin)*rnd.NextUnif() + lfRangeMin;
				pplfLocalMaxAbcData[i][j] = pplfAbcData[i][j];
			}
		}
		lfSolveRange = lfRangeMax-lfRangeMin;
		lfSolveRangeMin = lfRangeMin;
		lfSolveRangeMax = lfRangeMax;

		// 初期の状態で最適値を取得します。
		// 更新します。
		for (i = 0; i < iAbcSearchNum; i++)
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[i]);
			if (lfGlobalMinAbcData >= lfObjFunc)
			{
				iMinLoc = Integer.MAX_VALUE;
				lfGlobalMinAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
			}
			if (lfGlobalMaxAbcData <= lfObjFunc)
			{
				lfGlobalMaxAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
					plfGlobalMaxAbcData[j] = pplfAbcData[i][j];
			}
		}
		// 交叉を実行します。ここでREXを実行します。（１回分のみ。）
		pcRex.vSetGenData(pplfAbcData);
		pcRex.vRex();
//		pcRex.vGetBestGenData(pplfAbcData[i] );
//		pcRex.vGetBest2ndGenData(pplfAbcData );
		pcRex.vGetGenData(pplfAbcData);
	}

	/**
	* <PRE>
	* 　人工蜂コロニーの初期位置をAREX法を適用して算出して設定します。
	* </PRE>
	* @param lfRange 粒子の初期位置の出現範囲
	* @author kobayashi
	* @since 2016/11/07
	* @version 0.1
	*/
	public void vSetRandomARex(double lfRange)
	{
		int i, j;
		double lfObjFunc = 0.0;
		for (i = 0; i < iAbcDataNum; i++)
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfAbcData[i][j] = lfRange*(2.0*rnd.NextUnif() - 1.0);
				pplfVelocityData[i][j] = lfRange*(2.0*rnd.NextUnif() - 1.0);
				pplfLocalMaxAbcData[i][j] = pplfAbcData[i][j];
			}
		}
		lfSolveRange = lfRange;

		// 初期の状態で最適値を取得します。
		lfGlobalMinAbcData = Double.MAX_VALUE;
		lfGlobalMaxAbcData = -Double.MAX_VALUE;
		// 更新します。
		for (i = 0; i < iAbcSearchNum; i++)
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[i]);
			if (lfGlobalMinAbcData >= lfObjFunc)
			{
				lfGlobalMinAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
			}
			if (lfGlobalMaxAbcData <= lfObjFunc)
			{
				lfGlobalMaxAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
					plfGlobalMaxAbcData[j] = pplfAbcData[i][j];
			}
		}
		// 新たな探索点を求めて探索を実行します。AREXを実行してABCの初期データを作成します。
		// 交叉を実行します。ここでAREXを実行します。（１回分のみ。）
		pcRex.vSetGenData(pplfAbcData);
		pcRex.vARex();
//		pcRex.vGetBestGenData( pplfAbcData[i] );
//		pcRex.vGetBest2ndGenData(pplfAbcData);
		pcRex.vGetGenData(pplfAbcData);
	}

	/**
	* <PRE>
	* 　人工蜂コロニーの初期位置をAREX法を適用して算出して設定します。
	* </PRE>
	 * @param lfRangeMin 粒子の初期位置の出現範囲（最小値）
	 * @param lfRangeMax 粒子の初期位置の出現範囲（最大値）
	* @author kobayashi
	* @since 2016/11/07
	* @version 0.1
	*/
	public void vSetRandomARex(double lfRangeMin, double lfRangeMax )
	{
		int i, j;
		double lfMin = Double.MAX_VALUE;
		int iMinLoc = 0;
		double lfObjFunc = 0.0;
		for (i = 0; i < iAbcDataNum; i++)
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfAbcData[i][j] = (lfRangeMax-lfRangeMin)*rnd.NextUnif() + lfRangeMin;
				pplfVelocityData[i][j] = (lfRangeMax-lfRangeMin)*rnd.NextUnif() + lfRangeMin;
				pplfLocalMaxAbcData[i][j] = pplfAbcData[i][j];
			}
		}
		lfSolveRange = lfRangeMax-lfRangeMin;
		lfSolveRangeMin = lfRangeMin;
		lfSolveRangeMax = lfRangeMax;

		// 初期の状態で最適値を取得します。
		// 更新します。
		for (i = 0; i < iAbcSearchNum; i++)
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[i]);
			if (lfGlobalMinAbcData >= lfObjFunc)
			{
				iMinLoc = Integer.MAX_VALUE;
				lfGlobalMinAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
			}
			if (lfGlobalMaxAbcData <= lfObjFunc)
			{
				lfGlobalMaxAbcData = lfObjFunc;
				for (j = 0; j < iAbcVectorDimNum; j++)
					plfGlobalMaxAbcData[j] = pplfAbcData[i][j];
			}
		}
		// 新たな探索点を求めて探索を実行します。AREXを実行してABCの初期データを作成します。
		// 交叉を実行します。ここでAREXを実行します。（１回分のみ。）
		pcRex.vSetGenData(pplfAbcData);
		pcRex.vARex();
//		pcRex.vGetBestGenData( pplfAbcData[i] );
//		pcRex.vGetBest2ndGenData(pplfAbcData);
		pcRex.vGetGenData(pplfAbcData);
	}

	/**
	* <PRE>
	* 　人工蜂コロニーの初期位置を中心0の半径range内で一様乱数により設定します。
	* </PRE>
	* @param lfRange 粒子の初期位置の出現範囲
	* @param iUpdateStartLoc 更新したいベクトルの次元開始位置
	* @param iUpdateEndLoc 更新したいベクトルの次元終了位置
	* @author kobayashi
	* @since 2017/4/3
	* @version 0.1
	*/
	public void vSetUpdateData(double lfRange, int iUpdateStartLoc, int iUpdateEndLoc )
	{
		int i, j;
		double lfMin = Double.MAX_VALUE;
		int iMinLoc = 0;
		double lfObjFunc = 0.0;
		for (i = 0; i < iAbcDataNum; i++)
		{
			for (j = iUpdateStartLoc; j < iAbcVectorDimNum; j++)
			{
				if (j < iUpdateEndLoc)
				{
					pplfAbcData[i][j] = lfRange*(2.0*rnd.NextUnif() - 1.0);
					pplfVelocityData[i][j] = lfRange*(2.0*rnd.NextUnif() - 1.0);
				}
			}
			pfvConstraintCondition.vConstraintCondition(pplfAbcData[i]);
			pfvConstraintCondition.vConstraintCondition(pplfVelocityData[i]);
		}
	}

	/**
	* <PRE>
	* 　人工蜂コロニーの初期位置を中心0の半径range内で一様乱数により設定します。
	* </PRE>
	* @param lfMinRange 粒子の最小位置の出現範囲
	* @param lfMaxRange 粒子の最大位置の出現範囲
	* @param iUpdateStartLoc 更新したいベクトルの次元開始位置
	* @param iUpdateEndLoc 更新したいベクトルの次元終了位置
	* @author kobayashi
	* @since 2017/4/3
	* @version 0.1
	*/
	public void vSetUpdateData(double lfMinRange, double lfMaxRange, int iUpdateStartLoc, int iUpdateEndLoc)
	{
		int i, j;
		double lfMin = Double.MAX_VALUE;
		int iMinLoc = 0;
		double lfObjFunc = 0.0;
		for (i = 0; i < iAbcDataNum; i++)
		{
//			for (j = iUpdateStartLoc; j < iAbcVectorDimNum; j++)
//			{
//				if (j < iUpdateEndLoc)
//				{
//					pplfAbcData[i][j] = (lfMaxRange-lfMinRange)*rnd() + lfMinRange;
//					pplfVelocityData[i][j] = (lfMaxRange-lfMinRange)*rnd() + lfMinRange;
//				}
//			}
			// NEDOCS用
			double lfPatientNum = 100;
			//全患者数
			lfPatientNum = lfPatientNum*rnd.NextUnif();
			pplfAbcData[i][35] = lfPatientNum;
			// 診察及び処置を受けている人数
			pplfAbcData[i][36] = lfPatientNum*0.4+lfPatientNum*0.1+lfPatientNum*0.04;
			// 人工呼吸器をつけている人数
			pplfAbcData[i][37] = lfPatientNum*0.05+lfPatientNum*0.05+lfPatientNum*0.1;
			// 診察後から最も長くいる人の時間数
			pplfAbcData[i][38] = 24.0*rnd.NextUnif();
			// 一番最後に入院した患者が入院してからの時間
			pplfAbcData[i][39] = 24.0*rnd.NextUnif();
			// 待合室にいる患者の人数
			pplfAbcData[i][40] = lfPatientNum*0.2;
			// 患者の緊急度別人数1
			pplfAbcData[i][41] = lfPatientNum*0.05;
			// 患者の緊急度別人数2
			pplfAbcData[i][42] = lfPatientNum*0.05;
			// 患者の緊急度別人数3
			pplfAbcData[i][43] = lfPatientNum*0.1;
			// 患者の緊急度別人数4
			pplfAbcData[i][44] = lfPatientNum*0.2;
			// 患者の緊急度別人数5
			pplfAbcData[i][45] = lfPatientNum*0.6;
			pfvConstraintCondition.vConstraintCondition(pplfAbcData[i]);
//			pfvConstraintCondition.vConstraintCondition(pplfVelocityData[i]);
		}
	}

	/**
	 * <PRE>
	 * 　終了処理を実行します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public void vTerminate()
	{
		piLocalMaxAbcLoc 			= null;		//
		pplfAbcData 				= null;		// ABCデータ配列
		pplfNAbcData 				= null;		// ABC更新用データ配列
		plfGlobalMaxAbcData 		= null;		// 大域的最適解を表す粒子のデータ
		plfLocalMaxAbcData 			= null;		// 局所最適解を表す粒子のデータ
		pplfLocalMaxAbcData 		= null;		// 局所最適解を表す粒子ごとの最大値を格納するデータ
		plfLocalMaxObjectiveAbcData = null;		// 局所最適解を表す粒子のデータ
		pplfVelocityData 			= null;		// ABCデータ速度配列
		pplfNVelocityData 			= null;		// ABC更新用データ速度配列
		plfFitProb 					= null;		// 適合度相対確率
		plfFit 						= null;		// 適合度
		piNonUpdateCount 			= null;		// 更新しない回数
		plfVelocity					= null;		// ルーレット選択により選択する速度ベクトル
		plfCrossOverData 			= null;		// 交叉一時格納用配列
	}

	/**
	 * <PRE>
	 *   目的関数をインストールします。
	 *   実際にはコールバック関数をインストールします。
	 * </PRE>
	 * @param pflfFunction 目的関数の関数ポインタ
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public void vSetConstraintFunction( ObjectiveFunctionInterface pflfFunction )
	{
		pflfObjectiveFunction = pflfFunction;
		if( pcUndx != null )
		{
			pcUndx.vSetConstraintFunction( pflfObjectiveFunction );
		}
		if( pcRex != null )
		{
			pcRex.vSetConstraintFunction( pflfObjectiveFunction );
		}
		if (pcPowell != null)
		{
			pcPowell.vSetConstraintFunction(pflfObjectiveFunction );
		}
	}

	/**
	 * <PRE>
	 * 　使用する目的関数を設定します。
	 * </PRE>
	 * @param iMode 使用する目的関数
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public void vSetConstarintFunctionMode( int iMode )
	{
		pflfObjectiveFunction.vSetFunctionMode( iMode );
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
	public void vReleaseCallbackConstraintFunction()
	{
		pflfObjectiveFunction = null;
		if( pcUndx != null )
		{
			pcUndx.vReleaseCallbackConstraintFunction();
		}
		if( pcRex != null )
		{
			pcRex.vReleaseCallbackConstraintFunction();
		}
		if (pcPowell != null)
		{
			pcPowell.vReleaseCallConstraintFunction();
		}
	}

	/**
	 * <PRE>
	 * 　制約条件をインストールします。
	 * 　実際にはコールバック関数をインストールします。
	 * </PRE>
	 * @param pfvCondition 制約条件の関数ポインタ
	 * @author kobayashi
	 * @since 2016/8/12
	 * @version 0.1
	 */
	public void vSetConstraintCondition( ConstraintConditionInterface pfvCondition )
	{
		pfvConstraintCondition = pfvCondition;
		if( pcUndx != null )
		{
			pcUndx.vSetConstraintFunction( pflfObjectiveFunction );
		}
		if( pcRex != null )
		{
			pcRex.vSetConstraintFunction( pflfObjectiveFunction );
		}
		if (pcPowell != null)
		{
			pcPowell.vSetConstraintFunction(pflfObjectiveFunction);
		}
	}

	/**
	 * <PRE>
	 * 　使用する制約条件を設定します。
	 * </PRE>
	 * @param iMode 使用する制約条件
	 * @author kobayashi
	 * @since 2016/8/12
	 * @version 0.1
	 */
	public void vSetConstarintConditionMode( int iMode )
	{
		pfvConstraintCondition.vSetConditionMode( iMode );
	}

	/**
	 * <PRE>
	 * 　制約条件をアンインストールします。
	 * 　実際にはコールバック関数をアンインストールします。
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/12
	 * @version 0.1
	 */
	public void vReleaseCallbackConstraintCondition()
	{
		pfvConstraintCondition = null;
		if( pcUndx != null )
		{
			pcUndx.vReleaseCallbackConstraintFunction();
		}
		if( pcRex != null )
		{
			pcRex.vReleaseCallbackConstraintFunction();
		}
		if (pcPowell != null)
		{
			pcPowell.vReleaseCallConstraintFunction();
		}
	}

	/**
	 * <PRE>
	 * 　人工蜂コロニー最適化法を実行します。
	 *   ver 0.1 初版
	 *   ver 0.2 機能わけを実施。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.2
	 */
	public void vAbc()
	{
		// employee bee の動作
		vEmployBeeOrigin();

		// onlookers beeの動作
		vOnlookerBeeOrigin();

		// scout bee の実行
		vScoutBeeOrigin();

		// 更新します。
		vGetGlobalMaxMin();
	}

	/**
	 * <PRE>
	 * 　人工蜂コロニー最適化法を実行します。
	 *	 GBestを利用したABC法
	 *   Gbest-guided artificial bee colony algorithm for numerical function optimization.
	 *   Applied Mathematics and Computation, 217(7):3166-3173,2010.
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public void vGAbc()
	{
		// employee bee の動作
		vEmployBeeGBest();

		// onlookers beeの動作
		vOnlookerBeeGBest();

		// scout bee の実行
		vScoutBeeNormal();

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	 * <PRE>
	 * 　人工蜂コロニー最適化法を実行します。
	 * </PRE>
	 * @param iUpdateCount 現世代数
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public void vModifiedAbc( int iUpdateCount )
	{
		double lfFitJudge = 0.0;
		// employed beesによる探索を実施します。
		lfFitJudge = lfEmployBeeEnhanced( iUpdateCount );

		// onlooker beesによる探索
		vOnlookerBeeEnhanced( iUpdateCount, lfFitJudge );

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	 * <PRE>
	 * 　人工蜂コロニー最適化法を実行します。
	 *   粒子群最適化法のIWCFAを適用
	 * </PRE>
	 * @param iUpdateCount 現世代数
	 * @author kobayashi
	 * @since 2015/7/28
	 * @version 0.1
	 */
	public void vIWCFAAbc( int iUpdateCount )
	{
		double lfFai = 0.0;
		double lfK = 0.0;
		double lfCoe1 = 2.0;
		double lfCoe2 = 2.0;

		lfFai = lfCoe1 + lfCoe2;
		if( lfFai > 4.0 )	lfK = 2.0/(Math.abs(2.0-lfFai - Math.sqrt( lfFai*lfFai-4.0*lfFai ) )  );
		else			lfK = 1.0;

//		lfWeight = lfMaxWeight - (lfMaxWeight-lfMinWeight)/(double)iGenerationNumber*(double)(iUpdateCount-piTotalNonUpdateCount[i];
		// employee bee の動作
		vEmployBeeIWCFA( lfK, lfCoe1, lfCoe2, iUpdateCount );

		// onlookers beeの動作
		vOnlookerBeeIWCFA( lfK, lfCoe1, lfCoe2, iUpdateCount );

		// scout bee の実行
		vScoutBeeNormal();

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	 * <PRE>
	 * 　2013 Memetic search in artificial bee colony algorthimより
	 *   ver 0.1 2016/07/28 初版
	 *   ver 0.2 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 *   ver 0.3 2016/11/07 論文読み落としによるアルゴリズムの実装ミスの修正。
	 * </PRE>
	 * @param iUpdateCount 現世代数
	 * @author kobayashi
	 * @since 2016/7/28
	 * @version 0.3
	 */
	public void vMeAbc( int iUpdateCount )
	{
		int j, k;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfFai = 0.618;
		double lfA    = -1.2;
		double lfB    = 1.2;
		double lfPr = 0.4;
		double lfF1 = 0.0;
		double lfF2 = 0.0;

		// employee bee の動作
		vEmployBeeGBest();

		// onlookers beeの動作
		vOnlookerBeeGBest();

		// scout bee の実行
		vScoutBeeNormal();
//		vScoutBeeUndx();

		// Memetic artificial bee colony Algorithm(Prに関しては0～1の間の適当な値を設定。)
		while( Math.abs(lfA-lfB) < 0.01 )
		{
			lfF1 = ( lfB-(lfB-lfA)*lfFai );
			lfF2 = ( lfA+(lfB-lfA)*lfFai );

			j = rnd.NextInt(iAbcSearchNum);
			for( k = 0;k < iAbcVectorDimNum; k++ )
			{
				if( rnd.NextUnif() > lfPr )
				{
					plfXnew1[k] = plfGlobalMinAbcData[k] + lfF1*(plfGlobalMinAbcData[k]-pplfAbcData[j][k]);
					plfXnew2[k] = plfGlobalMinAbcData[k] + lfF2*(plfGlobalMinAbcData[k]-pplfAbcData[j][k]);
				}
				else
				{
					plfXnew1[k] = plfGlobalMinAbcData[k];
					plfXnew2[k] = plfGlobalMinAbcData[k];
				}
			}
			lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( plfXnew1 );
			lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( plfXnew2 );
			if( lfFunc1 < lfFunc2 )
			{
				lfB = lfF2;
				if( lfFunc1 < lfGlobalMinAbcData )
				{
					for( k = 0;k < iAbcVectorDimNum; k++ )
					{
						plfGlobalMinAbcData[k] = plfXnew1[k];
					}
				}
			}
			else
			{
				lfA = lfF1;
				if( lfFunc2 < lfGlobalMinAbcData )
				{
					for( k = 0;k < iAbcVectorDimNum; k++ )
					{
						plfGlobalMinAbcData[k] = plfXnew2[k];
					}
				}
			}
		}

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	 * <PRE>
	 * 　Randomized Memetic Artificial Bee Colony Algorthimより
	 *   International Journal of Emerging Trends of Technology in Computer Science, vol.3(1), 2014
	 *   ver 0.1 2016/09/23 初版
	 *   ver 0.2 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 * </PRE>
	 * @param iUpdateCount 現世代数
	 * @author kobayashi
	 * @since 2016/9/23
	 * @version 0.2
	 */
	public void vRMAbc( int iUpdateCount )
	{
		int j, k;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfFai = 0.618;
		double lfA    = -1.2;
		double lfB    = 1.2;
		double lfK = 0.0;
		double lfPr = 0.4;
		double lfF1 = 0.0;
		double lfF2 = 0.0;

		// employee bee の動作
//		vEmployBeeOrigin();
		vEmployBeeGBest();

		// onlookers beeの動作
		vOnlookerBeeRM();

		// scout bee の実行
		vScoutBeeNormal();
//		vScoutBeeUndx();

		// Randomized Memetic artificial bee colony Algorithm(Prに関しては0～1の間の適当な値を設定。)前回論文では0.3が推奨値。
		while( Math.abs(lfA-lfB) < 0.01 )
		{
			lfF1 = rnd.NextUnif()*( lfB-(lfB-lfA)*lfFai );
			lfF2 = (rnd.NextUnif()-1.0)*( lfA+(lfB-lfA)*lfFai );

			j = rnd.NextInt(iAbcSearchNum);
			for( k = 0;k < iAbcVectorDimNum; k++ )
			{
				if( rnd.NextUnif() > lfPr )
				{
					plfXnew1[k] = plfGlobalMinAbcData[k] + lfF1*(plfGlobalMinAbcData[k]-pplfAbcData[j][k]);
					plfXnew2[k] = plfGlobalMinAbcData[k] + lfF2*(plfGlobalMinAbcData[k]-pplfAbcData[j][k]);
				}
				else
				{
					plfXnew1[k] = plfGlobalMinAbcData[k];
					plfXnew2[k] = plfGlobalMinAbcData[k];
				}
			}
			lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( plfXnew1 );
			lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( plfXnew2 );
			if( lfFunc1 < lfFunc2 )
			{
				lfB = lfF2;
				if( lfFunc1 < lfGlobalMinAbcData )
				{
					for( k = 0;k < iAbcVectorDimNum; k++ )
					{
						plfGlobalMinAbcData[k] = plfXnew1[k];
					}
				}
			}
			else
			{
				lfA = lfF1;
				if( lfFunc2 < lfGlobalMinAbcData )
				{
					for( k = 0;k < iAbcVectorDimNum; k++ )
					{
						plfGlobalMinAbcData[k] = plfXnew2[k];
					}
				}
			}
		}

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	 * <PRE>
	 * 　人工蜂コロニー最適化法（交叉を導入した手法）を実行します。
	 *   A Novel Hybrid Crossover based Artificial Bee Colony Algorithm for Optimization Problem International Journal of Computer Applications 2013より
	 *   ver 0.1 2016/04/11 初版
	 *   ver 0.2 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/4/11
	 * @version 0.1
	 */
	public void vCbAbc()
	{
		int i,j,k,m,h,c;
		int iRankCount = 0;
		int iLocalMaxAbcLoc = 0;
		int iGlobalMaxAbcLoc = 0;
		int iMinLoc = 0;
		double lfTempAbcData = -Double.MAX_VALUE;
		double lfTempWeight = 0.0;
		double lfMin = 0.0;
		double lfObjFunc = 0.0;
		double lfRand = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfFunc3 = 0.0;
		double lfRes = 0.0;
		double lfPrevProb = 0.0;
		double lfProb = 0.0;
		double lfFitProb = 0.0;
		int icNonUpdateCount = 0;
		ArrayList<Double> stlCrossOverData1,stlCrossOverData2,stlCrossOverData3;

		int iParentLoc1;
		int iParentLoc2;

		// 初期化
		stlCrossOverData1 = new ArrayList<Double>();
		stlCrossOverData2 = new ArrayList<Double>();
		stlCrossOverData3 = new ArrayList<Double>();

		// employee bee の動作
		vEmployBeeOrigin();

		// crossoverを実行します。(もっとも旧式の実数値ＧＡの交叉)
		iParentLoc1 = rnd.NextInt(iAbcSearchNum);
		iParentLoc2 = rnd.NextInt(iAbcSearchNum);

	// 実数値ＧＡの平均交叉を実行します。中点、内分点、外分点の３点を算出します。
		for( i = 0;i < iAbcVectorDimNum; i++ )
		{
			stlCrossOverData1.add( 0.5*( pplfAbcData[iParentLoc1][i] + pplfAbcData[iParentLoc2][i] ) );
			stlCrossOverData2.add( 1.5*( pplfAbcData[iParentLoc1][i] + 0.5*pplfAbcData[iParentLoc2][i] ) );
			stlCrossOverData3.add( -0.5*( pplfAbcData[iParentLoc1][i] + 1.5*pplfAbcData[iParentLoc2][i] ) );
		}

		// ランダムに選択した親の中で評価関数の値が悪いものと生成した子供を交換します。
		for( i = 0;i < iAbcVectorDimNum; i++ ) plfCrossOverData[i] = stlCrossOverData1.get(i);
		lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( plfCrossOverData);
		for( i = 0;i < iAbcVectorDimNum; i++ ) plfCrossOverData[i] = stlCrossOverData2.get(i);
		lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( plfCrossOverData );
		for( i = 0;i < iAbcVectorDimNum; i++ ) plfCrossOverData[i] = stlCrossOverData3.get(i);
		lfFunc3 = pflfObjectiveFunction.lfObjectiveFunction( plfCrossOverData );
		if( lfFunc1 < lfFunc2 )
		{
			if( lfFunc2 < lfFunc3 )
			{
				for( i = 0;i < iAbcVectorDimNum; i++ )
				{
					pplfAbcData[iParentLoc1][i] = stlCrossOverData1.get(i);
					pplfAbcData[iParentLoc2][i] = stlCrossOverData2.get(i);
				}
			}
			else if( lfFunc1 < lfFunc3 )
			{
				for( i = 0;i < iAbcVectorDimNum; i++ )
				{
					pplfAbcData[iParentLoc1][i] = stlCrossOverData1.get(i);
					pplfAbcData[iParentLoc2][i] = stlCrossOverData3.get(i);
				}
			}
			else
			{
				for( i = 0;i < iAbcVectorDimNum; i++ )
				{
					pplfAbcData[iParentLoc1][i] = stlCrossOverData3.get(i);
					pplfAbcData[iParentLoc2][i] = stlCrossOverData1.get(i);
				}
			}
		}
		else if( lfFunc2 < lfFunc1 )
		{
			if( lfFunc1 < lfFunc3 )
			{
				for( i = 0;i < iAbcVectorDimNum; i++ )
				{
					pplfAbcData[iParentLoc1][i] = stlCrossOverData2.get(i);
					pplfAbcData[iParentLoc2][i] = stlCrossOverData1.get(i);
				}
			}
			else if( lfFunc2 < lfFunc3 )
			{
				for( i = 0;i < iAbcVectorDimNum; i++ )
				{
					pplfAbcData[iParentLoc1][i] = stlCrossOverData2.get(i);
					pplfAbcData[iParentLoc2][i] = stlCrossOverData3.get(i);
				}
			}
			else
			{
				for( i = 0;i < iAbcVectorDimNum; i++ )
				{
					pplfAbcData[iParentLoc1][i] = stlCrossOverData3.get(i);
					pplfAbcData[iParentLoc2][i] = stlCrossOverData2.get(i);
				}
			}
		}


		// onlookers beeの動作
		vOnlookerBeeOrigin();

		// scout bee の実行
		vScoutBeeNormal();

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	* <PRE>
	* 　人工蜂コロニー最適化法（交叉を導入した手法）を実行します。
	*   完全自作アレンジ
	* </PRE>
	* @author kobayashi
	* @since 2016/8/10
	* @version 0.1
	*/
	public void vUndxAbc()
	{
		// employee bee の動作
//		vEmployBeeBF();
//		vEmployBeeBest();
		vEmployBeeGBest();

		// onlookers beeの動作
//		vOnlookerBeeBF();
//		vOnlookerBeeBest();
		vOnlookerBeeGBest();

		// scout bee の実行
//		vScoutBeeBF(iUpdateCount);
		vScoutBeeUndx();

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	* <PRE>
	* 　人工蜂コロニー最適化法（交叉を導入した手法）を実行します。
	*   完全自作アレンジ（ベースはBest-so-Far ABC法）
	* </PRE>
	* @author kobayashi
	* @since 2016/11/18
	* @version 0.1
	*/
	public void vUXAbc()
	{
		// employee bee の動作
		vEmployBeeBF();
//		vEmployBeeBest();
//		vEmployBeeGBest();

		// onlookers beeの動作
		vOnlookerBeeBF();
//		vOnlookerBeeBest();
//		vOnlookerBeeGBest();

		// scout bee の実行
//		vScoutBeeBF(iUpdateCount);
		vScoutBeeUndx();

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	 * <PRE>
	 * 　人工蜂コロニー最適化法（交叉を導入した手法）を実行します。
	 *   完全自作アレンジ
	 *   2011の高性能化を導入
	 * </PRE>
	 * @param iUpdateCount 現世代数
	 * @author kobayashi
	 * @since 2016/8/19
	 * @version 0.1
	 */
	public void vUndxEnhancedAbc( int iUpdateCount )
	{
		double lfFitJudge = 0.0;

		// employee bee の動作
//		lfFitJudge = lfEmployBeeBestEnhanced( iUpdateCount );
		lfFitJudge = lfEmployBeeEnhanced( iUpdateCount );

		// onlookers beeの動作
//		vOnlookerBeeBestEnhanced( iUpdateCount, lfFitJudge );
		vOnlookerBeeEnhanced( iUpdateCount, lfFitJudge );

		// scout bee の実行
		vScoutBeeUndx();

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	 * <PRE>
	 * 　人工蜂コロニー最適化法（交叉を導入した手法）を実行します。
	 *   完全自作アレンジ（Emsanble Real Coded CrossOverを用います。）
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/10
	 * @version 0.1
	 */
	public void vRexAbc()
	{
		// employee bee の動作
//		vEmployBeeBest();
		vEmployBeeGBest();

		// onlookers beeの動作
//		vOnlookerBeeBest();
		vOnlookerBeeGBest();

		// scout bee の実行
		vScoutBeeRex();

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	 * <PRE>
	 * 　人工蜂コロニー最適化法（交叉を導入した手法）を実行します。
	 *   完全自作アレンジ（Adaptation Emsanble Real Coded CrossOverを用います。）
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/27
	 * @version 0.1
	 */
	public void vARexAbc()
	{
		// employee bee の動作
//		vEmployBeeBest();
		vEmployBeeGBest();

		// onlookers beeの動作
//		vOnlookerBeeBest();
		vOnlookerBeeGBest();

		// scout bee の実行
		vScoutBeeARex();

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	 * <PRE>
	 * 　人工蜂コロニー最適化法を実行します。
	 *   HJABC法を適用します。
	 *   Artificial Bee Colony Algorithm ith Local Search for Numerical Optimization, Jornal of Software, Vol.6, no.3, march 2011より
	 *   ver 0.1 2016/10/03 初版
	 *   ver 0.2 2016/10/25 アルゴリズムが実現できていなかったので実現中。
	 *   ver 0.3 2016/10/27 アルゴリズム実装完了。
	 * </PRE>
	 * @param iUpdateCount 現世代数
	 * @author kobayashi
	 * @since 2016/10/3
	 * @version 0.1
	 */
	public void vHJAbc( int iUpdateCount )
	{
		int i, j;
		double lfRes = 0.0;
		double lfStepSize = 1.0;
		double lfObjFunc0 = 0.0;
		double lfObjFunc1 = 0.0;
		double lfObjFunc2 = 0.0;
		int iInterval;
		double rho = 0.5;
		boolean bRet;

		iHJCounter = 50 * iAbcSearchNum;
		iHJInterval = 3 * iAbcSearchNum;
		rho = 0.5;

		// employee bee の動作
		vEmployBeeOrigin();

		// onlookers beeの動作
		vOnlookerBeeHJ();

		// scout bee の実行
		vScoutBeeNormal();

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();

		// ステップサイズの計算を実行します。
		for( i = 0; i < plfX0.length; i++ ) plfX0[i] = plfGlobalMinAbcData[i];
//		memcpy(plfX0, plfGlobalMinAbcData, sizeof(double)*iAbcVectorDimNum);
		if (iUpdateCount % iHJInterval == 0)
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				lfRes = 0.0;
				for (i = 0; i < iAbcSearchNum; i++)
					lfRes += (pplfAbcData[i][j] - plfGlobalMinAbcData[j]);
				plfStepSize[j] = (0.1*lfRes / (double)iAbcSearchNum);
			}
			// Hooke-Jeeves法を適用します。
			vModifiedHookeJeevesMethod(plfStepSize, plfX1, plfX2, plfX0);

			lfObjFunc0 = pflfObjectiveFunction.lfObjectiveFunction(plfX0);
			lfObjFunc1 = pflfObjectiveFunction.lfObjectiveFunction(plfX1);
			lfObjFunc2 = pflfObjectiveFunction.lfObjectiveFunction(plfX2);

			// 現在の最適値を更新します。
			if (lfObjFunc2 <= lfObjFunc0)
			{
				for( i = 0; i < plfGlobalMinAbcData.length; i++ ) plfGlobalMinAbcData[i] = plfX2[i];
//				memcpy(plfGlobalMinAbcData, plfX2, sizeof(double)*iAbcVectorDimNum);
			}
			if (lfObjFunc0 <= lfObjFunc1)
			{
				for( i = 0; i< plfX1.length; i++ ) plfX1[i] = plfX0[i];
//				memcpy(plfX1, plfX0, sizeof(double)*iAbcVectorDimNum);
				iReCounter = 0;
			}
			else iReCounter++;
			if (iReCounter > iHJCounter)
			{
				// Hooke-Jeeves法を適用します。
				vModifiedHookeJeevesMethod(plfStepSize, plfX1, plfX2, plfX0);
			}
		}
	}


	/**
	* <PRE>
	* 　人工蜂コロニー最適化法（交叉を導入した手法）を実行します。
	*   算術交叉を用いた改良型Artificial Bee Colonyアルゴリズム, 第28回Fuzzy System Symposium, 9, 2012. より.
	*   ver 0.1 初版
	* </PRE>
	* @author kobayashi
	* @since 2016/10/17
	* @version 0.1
	*/
	public void vACAbc()
	{
		// employee bee の動作
		vEmployBeeOrigin();

		// onlookers beeの動作
		vOnlookerBeeAC();

		// scout bee の実行
		vScoutBeeNormal();

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	* <PRE>
	* 　交叉手法を導入した人工蜂コロニー最適化法を実行します。
	*   Ivona Brajevic, Crossover-based artificial bee colony algorithm for constrained optimization problems, Neural Computing and Application (2015) 26:1587-1601.
	*   ver 0.1 初版
	* </PRE>
	 * @param iUpdateCount 現世代数
	* @author kobayashi
	* @since 2016/10/19
	* @version 0.1
	*/
	public void vCBAbc( int iUpdateCount )
	{
		double lfMr = 0.1;
		double lfMrMax = 0.9;
		double lfMCN = iGenerationNumber;
		double lfP = 0.3;

		// employee bee の動作
		vEmployBeeCB( lfMr );

		// onlookers beeの動作
		vOnlookerBeeCB( lfMr );

		// scout bee の実行
		vScoutBeeCB( iUpdateCount );

		// 閾値判定を更新。
		lfMr = lfMr < lfMrMax ? lfMr + (lfMrMax - 0.1) / (lfP*lfMCN) : lfMrMax;

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	 * <PRE>
	 * 　Best-so-far Artificial Bee Colony Methodを実行します。
	 *   The best-so-far selection in Artificial Bee Colony algorithm Applied Soft Computing 11 (2011) 2888-2901
	 *   ver 0.1 2016.10.28 初版
	 * </PRE>
	 * @param iUpdateCount 現世代数
	 * @author kobayashi
	 * @since 2016/10/28
	 * @version 0.1
	 */
	public void vBFAbc( int iUpdateCount )
	{
		// employee bee の動作
		vEmployBeeBF();

		// onlookers beeの動作
		vOnlookerBeeBF();
		// scout bee の実行
		vScoutBeeBF( iUpdateCount );
	//	vScoutBeeUndx();
		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	* <PRE>
	* 　人工蜂コロニー最適化法を実行します。
	*   Powell法を導入したPABC法を実行します。
	*   ver 0.1 2016/1104 初版
	* </PRE>
	* @param iUpdateCount 現世代数
	* @author kobayashi
	* @since 2016/11/04
	* @version 0.1
	*/
	public void vPAbc( int iUpdateCount )
	{
		// employee bee の動作
		vEmployBeeOrigin();

		// onlookers beeの動作
		vOnlookerBeeBest();

		// Powell法の実行
		vPowell( iUpdateCount );

		// scout bee の実行
		vScoutBeeNormal();
	//	vScoutBeeUndx();

		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	* <PRE>
	* 　人工蜂コロニー最適化法（交叉を導入した手法）を実行します。
	*   完全自作アレンジ（ベースはBest-so-Far ABC法）
	* </PRE>
	* @author kobayashi
	* @since 2016/11/18
	* @version 0.1
	*/
	public void vBFRexAbc()
	{
		// employee bee の動作
		vEmployBeeBF();
		//	vEmployBeeBest();
		//	vEmployBeeGBest();

		// onlookers beeの動作
		vOnlookerBeeBF();
		//	vOnlookerBeeBest();
		//	vOnlookerBeeGBest();
		// scout bee の実行
		//	vScoutBeeBF(iUpdateCount);
		vScoutBeeRex();
		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}

	/**
	* <PRE>
	* 　人工蜂コロニー最適化法（交叉を導入した手法）を実行します。
	*   完全自作アレンジ（ベースはBest-so-Far ABC法）
	* </PRE>
	* @author kobayashi
	* @since 2016/11/18
	* @version 0.1
	*/
	public void vBFARexAbc()
	{
		// employee bee の動作
		vEmployBeeBF();
		//	vEmployBeeBest();
		//	vEmployBeeGBest();

		// onlookers beeの動作
		vOnlookerBeeBF();
		//	vOnlookerBeeBest();
		//	vOnlookerBeeGBest();
		// scout bee の実行
		//	vScoutBeeBF(iUpdateCount);
		vScoutBeeARex();
		// 局所最大値、最小値を取得します。
		vGetLocalMaxMin();

		// 大域的最大値、最小値を取得します。
		vGetGlobalMaxMin();
	}


	/**
	 * <PRE>
	 * 　Employ Beeを実行します。(大本のバージョンと同じ手法)
	 *   ver 0.1 2016/08/18 初版
	 *   ver 0.2 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 *   ver 0.3 2016/10/28 余計なループの削除を実施。
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/18
	 * @version 0.3
	 */
	private void vEmployBeeOrigin()
	{
		int m,h;
		int i,j;
		double lfRand = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
	// employee bee の動作

		// 更新点候補を算出します。
		m = rnd.NextInt(iAbcSearchNum);
//		h = rnd.NextInt(iAbcVectorDimNum);

//		m = rnd.NextInt( iAbcSearchNum-1 );
		h = rnd.NextInt( iAbcVectorDimNum - 12 );

		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfRand = 2*rnd.NextUnif()-1;
			for( j = 0; j < iAbcVectorDimNum; j++ )
				pplfVelocityData[i][j] = pplfAbcData[i][j];
			pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*( pplfAbcData[i][h] - pplfAbcData[m][h] );
			pfvConstraintCondition.vConstraintCondition( pplfVelocityData[i] );

			// 各探索点と更新しなかった回数を格納する変数を更新します。
			lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[i] );
			lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );

			if( lfFunc1 < lfFunc2 )
			{
				pplfAbcData[i][h] = pplfVelocityData[i][h];
				piNonUpdateCount[i] = 0;
			}
			else	piNonUpdateCount[i] = piNonUpdateCount[i] + 1;
		}
	}

	/**
	 * <PRE>
	 * 　Employ Beeを実行します。(高精度化バージョン)
	 *   2011の電子情報通信学会の論文より
	 *   ver 0.1
	 *   ver 0.2 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 * </PRE>
	 * @param iUpdateCount	現在の世代数
	 * @return				適応度算出用パラメータ
	 * @author kobayashi
	 * @since 2016/8/18
	 * @version 0.2
	 */
	private double lfEmployBeeEnhanced( int iUpdateCount )
	{
		int m = 0;
		int h;
		int i,j;
		double lfObjFunc = 0.0;
		double lfFitProb = 0.0;
		double lfFitJudge = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfRes = 0.0;
		double lfPrevProb = 0.0;
		double lfProb = 0.0;
		double lfRand = 0.0;
		double lfMin = 0.0;
		int iMinLoc = 0;


		// employee bee の動作
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			// 適応度の算出
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );
			if( lfObjFunc-lfFitBound >= lfFitAccuracy )	lfFitProb = 1.0/( lfObjFunc-lfFitBound );
			else										lfFitProb = 1.0/lfFitAccuracy;
			plfFit[i] = lfFitProb;

			// 評価値をNp個算出します。
			stlFitProb.get(i).iLoc = i;
			stlFitProb.get(i).lfFitProb = lfFitProb;
		}
	//	// 適応度のソートを実行します。
	//	qsort( 0, iAbcSearchNum, plfFit );
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

		//
		if( iUpdateCount >= iAbcIntervalMinNum )
		{
			if( lfFitJudge < lfConvergenceParam )
			{
				// Fjudgeの値を更新します。
				lfFitJudge = ( lfFitInit - lfFitCurrentBest ) / ( lfFitInit - lfFitBound );
				if( lfFitJudge >= lfConvergenceParam )
				{
					// 各探索点の相対評価確率を算出します。
					lfRes = 0.0;
					for( i = 0;i < iAbcSearchNum; i++ )	lfRes += stlFitProb.get(i).lfFitProb;
					for( i = 0;i < iAbcSearchNum; i++ )	plfFitProb[i] = stlFitProb.get(i).lfFitProb/lfRes;
				}
				// 更新点候補を算出します。
				if( iUpdateCount >= iAbcIntervalMinNum && lfFitJudge >= lfConvergenceParam )
				{
					// ルーレット戦略により、mの値を決定します。
					lfProb = lfPrevProb = 0.0;
					lfRand = rnd.NextUnif();
					for( j = 0;j < iAbcSearchNum; j++ )
					{
						lfProb += plfFitProb[j];
						if( lfPrevProb <= lfRand && lfRand <= lfProb ) m = j;
						lfPrevProb = lfProb;
					}
				}
				else
				{
					// 適応度上位αからランダムに決定します。
					m = rnd.NextInt( iAbcUpperSearchNum );
				}
				// ランダムに決定します。
//				h = rnd.NextInt(iAbcVectorDimNum);
				h = rnd.NextInt(iAbcVectorDimNum-12);

				for( i = 0;i < iAbcSearchNum; i++ )
				{
					lfRand = 2*rnd.NextUnif()-1;
					for( j = 0; j < iAbcVectorDimNum; j++ )
						pplfVelocityData[i][j] = pplfAbcData[i][j];
					pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*( pplfAbcData[i][h] - pplfAbcData[stlFitProb.get(m).iLoc][h] );
				}
			}
			else
			{
				// 各探索点の相対評価確率を算出します。
				lfRes = 0.0;
				for( i = 0;i < iAbcSearchNum; i++ )	lfRes += stlFitProb.get(i).lfFitProb;
				for( i = 0;i < iAbcSearchNum; i++ )	plfFitProb[i] = stlFitProb.get(i).lfFitProb/lfRes;

				// 更新点候補を算出します。
				if( iUpdateCount >= iAbcIntervalMinNum && lfFitJudge >= lfConvergenceParam )
				{
					// ルーレット戦略により、mの値を決定します。
					lfProb = lfPrevProb = 0.0;
					lfRand = rnd.NextUnif();
					for( j = 0;j < iAbcSearchNum; j++ )
					{
						lfProb += plfFitProb[j];
						if( lfPrevProb <= lfRand && lfRand <= lfProb )	m = j;
						lfPrevProb = lfProb;
					}
				}
				else
				{
					// その他の場合はランダムに決定します。
					m = rnd.NextInt(iAbcUpperSearchNum);
				}
				// ランダムに決定します。
				h = rnd.NextInt(iAbcVectorDimNum);

				for( i = 0;i < iAbcSearchNum; i++ )
				{
					lfRand = 2*rnd.NextUnif()-1;
					for( j = 0; j < iAbcVectorDimNum; j++ )
						pplfVelocityData[i][j] = pplfAbcData[i][j];
					pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*( pplfAbcData[i][h] - pplfAbcData[stlFitProb.get(m).iLoc][h] );
				}
			}
		}
		else
		{
			// 更新点候補を算出します。
			if( iUpdateCount >= iAbcIntervalMinNum && lfFitJudge >= lfConvergenceParam )
			{
				// ルーレット戦略により、mの値を決定します。
				lfProb = lfPrevProb = 0.0;
				lfRand = rnd.NextUnif();
				for( j = 0;j < iAbcSearchNum; j++ )
				{
					lfProb += plfFitProb[j];
					if( lfPrevProb <= lfRand && lfRand <= lfProb )	m = j;
					lfPrevProb = lfProb;
				}
			}
			else
			{
				// その他の場合はランダムに決定します。
				m = rnd.NextInt(iAbcUpperSearchNum);
			}
			// ランダムに決定します。
			h = rnd.NextInt(iAbcVectorDimNum);

			for( i = 0;i < iAbcSearchNum; i++ )
			{
				lfRand = 2.0*rnd.NextUnif()-1.0;
				for( j = 0; j < iAbcVectorDimNum; j++ )
					pplfVelocityData[i][j] = pplfAbcData[i][j];
				pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*( pplfAbcData[i][h] - pplfAbcData[stlFitProb.get(m).iLoc][h] );
			}
		}

		// 各探索点を更新します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[i]);
			lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );
			if( lfFunc1 < lfFunc2 )
			{
				for( j = 0;j < iAbcVectorDimNum; j++ )
					pplfAbcData[i][j] = pplfVelocityData[i][j];
			}
		}
		return lfFitJudge;
	}

	/**
	 * <PRE>
	 * 　Employ Beeを実行します。(高精度化バージョン)
	 *   2011の電子情報通信学会の論文より
	 *   ver 0.1
	 *   ver 0.2 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 * </PRE>
	 * @param iUpdateCount	現在の世代数
	 * @return				適応度算出用パラメータ
	 * @author kobayashi
	 * @since 2016/8/18
	 * @version 0.2
	 */
	private double lfEmployBeeBestEnhanced( int iUpdateCount )
	{
		int m = 0;
		int h;
		int i,j;
		double lfObjFunc = 0.0;
		double lfFitProb = 0.0;
		double lfFitJudge = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfRes = 0.0;
		double lfPrevProb = 0.0;
		double lfProb = 0.0;
		double lfRand = 0.0;
		double lfMin = 0.0;
		int iMinLoc = 0;

		// employee bee の動作
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			// 適応度の算出
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );
			if( lfObjFunc-lfFitBound >= lfFitAccuracy )	lfFitProb = 1.0/( lfObjFunc-lfFitBound );
			else										lfFitProb = 1.0/lfFitAccuracy;
			plfFit[i] = lfFitProb;
		}
		// 適応度のソートを実行します。
		qsort( 0, iAbcSearchNum, plfFit );

		//
		if( iUpdateCount >= iAbcIntervalMinNum )
		{
			if( lfFitJudge < lfConvergenceParam )
			{
				// Fjudgeの値を更新します。
				lfFitJudge = ( lfFitInit - lfFitCurrentBest ) / ( lfFitInit - lfFitBound );
				if( lfFitJudge >= lfConvergenceParam )
				{
					// 各探索点の相対評価確率を算出します。
					lfRes = 0.0;
					for( i = 0;i < iAbcSearchNum; i++ )	lfRes += plfFit[i];
					for( i = 0;i < iAbcSearchNum; i++ )	plfFitProb[i] = plfFit[i]/lfRes;
				}
				else
				{
					// 更新点候補を算出します。
					if( iUpdateCount >= iAbcIntervalMinNum && lfFitJudge >= lfConvergenceParam )
					{
						// ルーレット戦略により、mの値を決定します。
						lfProb = lfPrevProb = 0.0;
						lfRand = rnd.NextUnif();
						for( j = 0;j < iAbcSearchNum; j++ )
						{
							lfProb += plfFitProb[j];
							if( lfPrevProb <= lfRand && lfRand <= lfProb ) m = j;
							lfPrevProb = lfProb;
						}
					}
					else
					{
						// 適応度上位αからランダムに決定します。
						m = rnd.NextInt(iAbcUpperSearchNum);
					}
					// ランダムに決定します。
					h = rnd.NextInt(iAbcVectorDimNum);

					for( i = 0;i < iAbcSearchNum; i++ )
					{
						lfRand = 2*rnd.NextUnif()-1;
						for( j = 0; j < iAbcVectorDimNum; j++ )
							pplfVelocityData[i][j] = pplfAbcData[i][j];
	//					pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*( pplfAbcData[i][h] - pplfAbcData[m][h] );
						pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*( pplfAbcData[i][h] - pplfLocalMinAbcData[m][h] );
					}
				}
			}
			else
			{
				// 各探索点の相対評価確率を算出します。
				lfRes = 0.0;
				for( i = 0;i < iAbcSearchNum; i++ )	lfRes += plfFit[i];
				for( i = 0;i < iAbcSearchNum; i++ )	plfFitProb[i] = plfFit[i]/lfRes;

				// 更新点候補を算出します。
				if( iUpdateCount >= iAbcIntervalMinNum && lfFitJudge >= lfConvergenceParam )
				{
					// ルーレット戦略により、mの値を決定します。
					lfProb = lfPrevProb = 0.0;
					lfRand = rnd.NextUnif();
					for( j = 0;j < iAbcSearchNum; j++ )
					{
						lfProb += plfFitProb[j];
						if( lfPrevProb <= lfRand && lfRand <= lfProb )	m = j;
						lfPrevProb = lfProb;
					}
				}
				else
				{
					// その他の場合はランダムに決定します。
					m = rnd.NextInt(iAbcUpperSearchNum);
				}
				// ランダムに決定します。
				h = rnd.NextInt(iAbcVectorDimNum);

				for( i = 0;i < iAbcSearchNum; i++ )
				{
					lfRand = 2*rnd.NextUnif()-1;
					for( j = 0; j < iAbcVectorDimNum; j++ )
						pplfVelocityData[i][j] = pplfAbcData[i][j];
	//				pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*( pplfAbcData[i][h] - pplfAbcData[m][h] );
					pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*( pplfAbcData[i][h] - pplfLocalMinAbcData[m][h] );
				}
			}
		}
		else
		{
			// 更新点候補を算出します。
			if( iUpdateCount >= iAbcIntervalMinNum && lfFitJudge >= lfConvergenceParam )
			{
				// ルーレット戦略により、mの値を決定します。
				lfProb = lfPrevProb = 0.0;
				lfRand = rnd.NextUnif();
				for( j = 0;j < iAbcSearchNum; j++ )
				{
					lfProb += plfFitProb[j];
					if( lfPrevProb <= lfRand && lfRand <= lfProb )	m = j;
					lfPrevProb = lfProb;
				}
			}
			else
			{
				// その他の場合はランダムに決定します。
				m = rnd.NextInt(iAbcUpperSearchNum);
			}
			// ランダムに決定します。
//			h = rnd.NextInt(iAbcVectorDimNum);
			h = rnd.NextInt(iAbcVectorDimNum-12);

			for( i = 0;i < iAbcSearchNum; i++ )
			{
				lfRand = 2.0*rnd.NextUnif()-1.0;
				for( j = 0; j < iAbcVectorDimNum; j++ )
					pplfVelocityData[i][j] = pplfAbcData[i][j];
	//			pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*( pplfAbcData[i][h] - pplfAbcData[m][h] );
				pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*( pplfAbcData[i][h] - pplfLocalMinAbcData[m][h] );
			}
		}

		// 各探索点を更新します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[i] );
			lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );
			if( lfFunc1 < lfFunc2 )
			{
				for( j = 0;j < iAbcVectorDimNum; j++ )
					pplfAbcData[i][j] = pplfVelocityData[i][j];
				piNonUpdateCount[i] = 0;
			}
			else	piNonUpdateCount[i] = piNonUpdateCount[i] + 1;
		}
		return lfFitJudge;
	}

	/**
	 * <PRE>
	 * 　Employ Beeを実行します。
	 *   ver 0.1
	 *   ver 0.2 NBest版に修正
	 *   ver 0.3 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 *   ver 0.4 2016/10/28 余計なループの削除を実施。
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/10
	 * @version 0.3
	 */
	void vEmployBeeBest()
	{
		int m,h;
		int i,j;
		double lfRand = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
	// employee bee の動作

		// 更新点候補を算出します。
		m = rnd.NextInt(iAbcSearchNum);
//		h = rnd.NextInt(iAbcVectorDimNum);

//		m = rnd.NextInt( iAbcSearchNum-1 );
		h = rnd.NextInt( iAbcVectorDimNum - 12 );

		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfRand = 2*rnd.NextUnif()-1;
			for( j = 0; j < iAbcVectorDimNum; j++ )
				pplfVelocityData[i][j] = pplfAbcData[i][j];
			pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*( pplfAbcData[i][h] - pplfLocalMinAbcData[m][h] );
			pfvConstraintCondition.vConstraintCondition( pplfVelocityData[i] );

			// 各探索点と更新しなかった回数を格納する変数を更新します。
			lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[i] );
			lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );

			if( lfFunc1 < lfFunc2 )
			{
				pplfAbcData[i][h] = pplfVelocityData[i][h];
				piNonUpdateCount[i] = 0;
			}
			else	piNonUpdateCount[i] = piNonUpdateCount[i] + 1;
		}
	}

	/**
	 * <PRE>
	 * 　Employ Beeを実行します。(GBest版)
	 *   ver 0.1
	 *   ver 0.2 NBest版に修正
	 *   ver 0.3 2016/10/24 論文を基に修正
	 *   ver 0.4 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 *   ver 0.5 2016/10/28 余計なループの削除を実施。
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/10
	 * @version 0.4
	 */
	void vEmployBeeGBest()
	{
		int m,h;
		int i,j;
		double lfRand = 0.0;
		double lfRand2 = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
	// employee bee の動作

		// 更新点候補を算出します。
		m = rnd.NextInt(iAbcSearchNum);
//		h = rnd.NextInt(iAbcVectorDimNum);

//		m = rnd.NextInt( iAbcSearchNum-1 );
		h = rnd.NextInt( iAbcVectorDimNum - 12 );

		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfRand = 2.0*rnd.NextUnif()-1.0;
			lfRand2 = 1.5*rnd.NextUnif();
			for( j = 0; j < iAbcVectorDimNum; j++ )
				pplfVelocityData[i][j] = pplfAbcData[i][j];
	//			pplfVelocityData[i][j] = pplfAbcData[i][j] + lfRand*(pplfAbcData[i][j] - pplfAbcData[m][j]) + lfRand2*(plfGlobalMinAbcData[j] - pplfAbcData[i][j]);
			pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*( pplfAbcData[i][h] - pplfAbcData[m][h] ) + lfRand2*(plfGlobalMinAbcData[h] - pplfAbcData[i][h] );
			pfvConstraintCondition.vConstraintCondition( pplfVelocityData[i] );

			// 各探索点と更新しなかった回数を格納する変数を更新します。
			lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[i] );
			lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );

			if( lfFunc1 < lfFunc2 )
			{
				pplfAbcData[i][h] = pplfVelocityData[i][h];
				piNonUpdateCount[i] = 0;
			}
			else 	piNonUpdateCount[i] = piNonUpdateCount[i] + 1;
		}
	}

	/**
	 * <PRE>
	 * 　Employ Beeを実行します。
	 *   ver 0.1
	 *   ver 0.2 2016/08/18 IWCFA(粒子群最適化法の一手法を適用)
	 *   ver 0.3 2016/10/28 余計なループの削除を実施。
	 * </PRE>
	 * @param lfK			IWCFA制御パラメータ1
	 * @param lfCoe1		IWCFA制御パラメータ2
	 * @param lfCoe2		IWCFA制御パラメータ3
	 * @param iUpdateCount	現世代数
	 * @author kobayashi
	 * @since 2016/8/10
	 * @version 0.2
	 */
	private void vEmployBeeIWCFA( double lfK, double lfCoe1, double lfCoe2, int iUpdateCount )
	{
		int m,h;
		int i,j;
		double lfRand = 0.0;
		double lfRand2 = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfWeight = 0.0;
		double lfMaxWeight = 1.0;
		double lfMinWeight = 0.3;

		// 更新点候補を算出します。
		m = rnd.NextInt(iAbcSearchNum);
//		h = rnd.NextInt(iAbcVectorDimNum);

//		m = rnd.NextInt( iAbcSearchNum-1 );
		h = rnd.NextInt( iAbcVectorDimNum - 12 );

		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfRand = 2.0*rnd.NextUnif()-1.0;
	//		lfRand2 = 2.0*rnd.NextUnif()-1.0;
			lfRand2 = rnd.NextUnif();
			for( j = 0; j < iAbcVectorDimNum; j++ )
				pplfVelocityData[i][j] = pplfAbcData[i][j];
	//		pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*( pplfAbcData[i][h] - pplfAbcData[m][h] );
			lfWeight = lfMaxWeight - (lfMaxWeight-lfMinWeight)/(double)iGenerationNumber*(double)(iUpdateCount-piTotalNonUpdateCount[m]);
			pplfVelocityData[i][h] = pplfAbcData[i][h] + lfCoe1*lfRand*( pplfAbcData[i][h] - pplfAbcData[m][h] ) + lfCoe2*lfRand2*( pplfAbcData[i][h] - pplfLocalMinAbcData[m][h] );
			pfvConstraintCondition.vConstraintCondition( pplfVelocityData[i] );

			// 各探索点と更新しなかった回数を格納する変数を更新します。
			lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[i] );
			lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );

			if( lfFunc1 < lfFunc2 )
			{
				pplfAbcData[i][h] = pplfVelocityData[i][h];
				piNonUpdateCount[i] = 0;
			}
			else
			{
				piNonUpdateCount[i] = piNonUpdateCount[i] + 1;
				piTotalNonUpdateCount[i] = piTotalNonUpdateCount[i] + 1;
			}
		}
	}

	/**
	* <PRE>
	*   Ivona Brajevic, Crossover-based artificial bee colony algorithm for constrained optimization problems, Neural Computing and Application (2015) 26:1587-1601.
	*   ver 0.1 2016/10/19 初版
	*   ver 0.2 2016/10/28 余計なループの削除を実施。
	* </PRE>
	* @param lfMr 更新用パラメーター
	* @author kobayashi
	* @since 2016/10/19
	* @version 0.1
	*/
	private void vEmployBeeCB( double lfMr )
	{
		int m, h;
		int i, j;
		double lfRand = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfMrMax = 0.9;
		double lfP = 0.3;
		double lfMCN = iGenerationNumber;
		double lfLowerVelocity = -Double.MAX_VALUE;
		double lfUpperVelocity = Double.MAX_VALUE;

		// employee bee の動作
		// 更新点候補を算出します。
		m = rnd.NextInt(iAbcSearchNum);

//		m = rnd.NextInt( iAbcSearchNum-1 );
		h = rnd.NextInt( iAbcVectorDimNum - 12 );

		for (i = 0; i < iAbcSearchNum; i++)
		{
			lfRand = 2 * rnd.NextUnif() - 1;
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				pplfVelocityData[i][j] = rnd.NextUnif() < lfMr ? pplfAbcData[i][j] + lfRand*(pplfAbcData[i][j] - pplfAbcData[m][j]) : pplfAbcData[i][j];
				if (pplfVelocityData[i][j] < lfLowerVelocity) pplfVelocityData[i][j] = 2.0*lfLowerVelocity - pplfVelocityData[i][j];
				else if (pplfVelocityData[i][j] > lfUpperVelocity) pplfVelocityData[i][j] = 2.0*lfUpperVelocity - pplfVelocityData[i][j];
				pfvConstraintCondition.vConstraintCondition( pplfVelocityData[i] );
			}
			// 各探索点と更新しなかった回数を格納する変数を更新します。
			lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction(pplfVelocityData[i]);
			lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[i]);

			if (lfFunc1 < lfFunc2)
			{
				for( j = 0;j < iAbcVectorDimNum; j++ )
					pplfAbcData[i][j] = pplfVelocityData[i][j];
				piNonUpdateCount[i] = 0;
			}
			else	piNonUpdateCount[i] = piNonUpdateCount[i] + 1;
		}
	}

	/**
	 * <PRE>
	 * 　Employ Beeを実行します。(Best-so-Far版)
	 *   The best-so-far selection in Artificial Bee Colony algorithm Applied Soft Computing 11 (2011) 2888-2901
	 *   ver 0.1 2016.10.28 初版
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/10/28
	 * @version 0.1
	 */
	private void vEmployBeeBF()
	{
		int m,h;
		int i,j;
		double lfRand = 0.0;
		double lfRand2 = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfObjFunc = 0.0;
		double lfLocalMinAbcData = Double.MAX_VALUE;

		// employee bee の動作
		// 更新点候補を算出します。
		m = rnd.NextInt(iAbcSearchNum);
//		h = rnd.NextInt(iAbcVectorDimNum);

//		m = rnd.NextInt( iAbcSearchNum-1 );
		h = rnd.NextInt( iAbcVectorDimNum - 12 );

		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfRand = 2.0*rnd.NextUnif()-1.0;
			for( j = 0; j < iAbcVectorDimNum; j++ )
				pplfVelocityData[i][j] = pplfAbcData[i][j];
			pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*(pplfAbcData[i][h] - pplfAbcData[m][h]);
			pfvConstraintCondition.vConstraintCondition( pplfVelocityData[i] );

			// 各探索点と更新しなかった回数を格納する変数を更新します。
			lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[i] );
			lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );
			if( lfFunc1 < lfFunc2 )
			{
				pplfAbcData[i][h] = pplfVelocityData[i][h];
				piNonUpdateCount[i] = 0;
			}
			else 	piNonUpdateCount[i] = piNonUpdateCount[i] + 1;
		}

		for (j = 0; j < iAbcVectorDimNum; j++)
			plfLocalMinAbcData[j] = pplfAbcData[0][j];
		lfLocalMinAbcData = pflfObjectiveFunction.lfObjectiveFunction(plfLocalMinAbcData);
		for(i = 1;i < iAbcSearchNum; i++ )
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[i] );
			if( lfObjFunc < lfLocalMinAbcData )
			{
				for( j = 0; j < iAbcVectorDimNum; j++ )
					plfLocalMinAbcData[j] = pplfAbcData[i][j];
				lfLocalMinAbcData = lfObjFunc;
			}
		}
	}

	/**
	 * <PRE>
	 *   Onlooker Beeを実行します。(大本のバージョン)
	 *   ver 0.1 2016/08/10 初版
	 *   ver 0.2 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/10
	 * @version 0.2
	 */
	private void vOnlookerBeeOrigin()
	{
		int i,j;
		int c,m,h;
		double lfRes = 0.0;
		double lfRand = 0.0;
		double lfFitProb = 0.0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfObjFunc = 0.0;

		lfRes = 0.0;
		for(j = 0;j < iAbcSearchNum; j++ )
		{
			// 適応度の算出
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[j] );
			if( lfObjFunc >= 0.0 )	lfFitProb = 1.0/( 1.0+lfObjFunc );
			else			lfFitProb = 1.0+Math.abs( lfObjFunc );
			lfRes += lfFitProb;
			plfFit[j] = lfFitProb;
		}
		// 適応度の正規化
		for( j = 0;j < iAbcSearchNum; j++ )	plfFitProb[j] = plfFit[j]/lfRes;
		// ルーレット戦略を実行
		lfProb = lfPrevProb = 0.0;
		lfRand = rnd.NextUnif();
		c = 0;
		for( j = 0;j < iAbcSearchNum; j++ )
		{
			lfProb += plfFitProb[j];
			if( lfPrevProb <= lfRand && lfRand <= lfProb )	c = j;
			lfPrevProb = lfProb;
		}
		// ルーレット選択した探索点に対して更新候補を求めます。

		// 更新点候補を算出します。
		// 更新点候補を乱数により決定します。
		m = rnd.NextInt(iAbcSearchNum);
//		h = rnd.NextInt(iAbcVectorDimNum);

//		m = rnd.NextInt( iAbcSearchNum-1 );
		h = rnd.NextInt( iAbcVectorDimNum - 12 );

		lfRand = 2*rnd.NextUnif()-1;
		for( j = 0; j < iAbcVectorDimNum; j++ )
			pplfVelocityData[c][j] = pplfAbcData[c][j];
		pplfVelocityData[c][h] = pplfAbcData[c][h] + lfRand*( pplfAbcData[c][h] - pplfAbcData[m][h] );
		pfvConstraintCondition.vConstraintCondition( pplfVelocityData[c] );

		// 更新点候補を次のように更新します。
		lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[c] );
		lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[c] );
		if( lfFunc1 < lfFunc2 )
		{
			for( j = 0;j < iAbcVectorDimNum; j++ )
				pplfAbcData[c][j] = pplfVelocityData[c][j];
			piNonUpdateCount[c] = 0;
		}
		else	piNonUpdateCount[c] = piNonUpdateCount[c] + 1;
	}

	/**
	 * <PRE>
	 * 　Onlooker Beeを実行します。(高精度化バージョン)
	 *   2011の電子情報通信学会の論文より
	 *   ver 0.1 2016/08/18 初版
	 *   ver 0.2 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 * </PRE>
	 * @param iUpdateCount	現世代数
	 * @param lfFitJudge	適応度フィッティング判定パラメータ
	 * @author kobayashi
	 * @since 2016/8/18
	 * @version 0.1
	 */
	void vOnlookerBeeEnhanced( int iUpdateCount, double lfFitJudge )
	{
		int i,j,l;
		int c,m,h;
		double lfRes = 0.0;
		double lfRand = 0.0;
		double lfFitProb = 0.0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfObjFunc = 0.0;

		// 更新点候補を算出します。
		if( iUpdateCount >= iAbcIntervalMinNum && lfFitJudge >= lfConvergenceParam )
		{
			// ルーレット戦略により、mの値を決定します。
			lfProb = lfPrevProb = 0.0;
			lfRand = rnd.NextUnif();
			c = 0;
			for( j = 0;j < iAbcSearchNum; j++ )
			{
				lfProb += plfFitProb[j];
				if( lfPrevProb <= lfRand &&	lfRand <= lfProb )	c = j;
				lfPrevProb = lfProb;
			}
		}
		else
		{
			// その他の場合は適応度上位αからランダムに決定します。
			c = rnd.NextInt(iAbcUpperSearchNum);
//			c = rnd.NextInt( iAbcUpperSearchNum-1 );
		}
		// ランダムに決定します。
		h = rnd.NextInt(iAbcVectorDimNum);
//		h = rnd.NextInt( iAbcVectorDimNum - 12 );

		// 更新点候補を生成します。
		lfRand = 2*rnd.NextUnif()-1;
		for( j = 0; j < iAbcVectorDimNum; j++ )
			pplfVelocityData[stlFitProb.get(c).iLoc][j] = pplfAbcData[stlFitProb.get(c).iLoc][j];
		pplfVelocityData[stlFitProb.get(c).iLoc][h] = pplfAbcData[stlFitProb.get(c).iLoc][h] + lfRand*( pplfAbcData[stlFitProb.get(c).iLoc][h] - pplfAbcData[stlFitProb.get(c).iLoc][h] );
		pfvConstraintCondition.vConstraintCondition( pplfVelocityData[c] );

		// 各探索点を更新します。
		lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[stlFitProb.get(c).iLoc] );
		lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[stlFitProb.get(c).iLoc] );
		if( lfFunc1 < lfFunc2 )
		{
			for( j = 0;j < iAbcVectorDimNum; j++ )
				pplfAbcData[stlFitProb.get(c).iLoc][j] = pplfVelocityData[stlFitProb.get(c).iLoc][j];
		}
	}

	/**
	 * <PRE>
	 * 　Onlooker Beeを実行します。(高精度化バージョン)
	 *   2011の電子情報通信学会の論文より
	 *   ver 0.1 2016/08/18 初版
	 *   ver 0.2 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 * </PRE>
	 * @param iUpdateCount	現在の世代数
	 * @param lfFitJudge	適応度の閾値
	 * @author kobayashi
	 * @since 2016/8/18
	 * @version 0.1
	 */
	private void vOnlookerBeeBestEnhanced( int iUpdateCount, double lfFitJudge )
	{
		int i,j;
		int c,m,h;
		double lfRes = 0.0;
		double lfRand = 0.0;
		double lfFitProb = 0.0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfObjFunc = 0.0;

		// 更新点候補を算出します。
		if( iUpdateCount >= iAbcIntervalMinNum && lfFitJudge >= lfConvergenceParam )
		{
			// ルーレット戦略により、mの値を決定します。
			lfProb = lfPrevProb = 0.0;
			lfRand = rnd.NextUnif();
			c = 0;
			for( j = 0;j < iAbcSearchNum; j++ )
			{
				lfProb += plfFitProb[j];
				if( lfPrevProb <= lfRand && lfRand <= lfProb )	c = j;
				lfPrevProb = lfProb;
			}
		}
		else
		{
			// その他の場合はランダムに決定します。
			c = rnd.NextInt(iAbcUpperSearchNum);
//			c = rnd.NextInt(iAbcUpperSearchNum-1);
		}
		// ランダムに決定します。
//		h = rnd.NextInt(iAbcVectorDimNum);
		h = rnd.NextInt( iAbcVectorDimNum - 12 );

		// 更新点候補を生成します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfRand = 2*rnd.NextUnif()-1;
			for( j = 0; j < iAbcVectorDimNum; j++ )
				pplfVelocityData[i][j] = pplfAbcData[i][j];
			pplfVelocityData[i][h] = pplfAbcData[i][h] + lfRand*( pplfAbcData[i][h] - plfGlobalMinAbcData[h] );
			pfvConstraintCondition.vConstraintCondition( pplfVelocityData[i] );
		}
		// 各探索点を更新します。
		lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[stlFitProb.get(c).iLoc] );
		lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[stlFitProb.get(c).iLoc] );
		if( lfFunc1 < lfFunc2 )
		{
			for( j = 0;j < iAbcVectorDimNum; j++ )
				pplfAbcData[c][j] = pplfVelocityData[stlFitProb.get(c).iLoc][j];
			piNonUpdateCount[c] = 0;
		}
		else	piNonUpdateCount[stlFitProb.get(c).iLoc] = piNonUpdateCount[stlFitProb.get(c).iLoc] + 1;
	}

	/**
	 * <PRE>
	 *   Onlooker Beeを実行します。(NBest版)
	 *   ver 0.1 2016/08/10 初版
	 *   ver 0.2 2016/09/13 NBest版を追加
	 *   ver 0.3 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/10
	 * @version 0.3
	 */
	private void vOnlookerBeeBest()
	{
		int i,j;
		int c,m,h;
		double lfRes = 0.0;
		double lfRand = 0.0;
		double lfFitProb = 0.0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfObjFunc = 0.0;

		lfRes = 0.0;
		for(j = 0;j < iAbcSearchNum; j++ )
		{
			// 適応度の算出
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[j] );
//			if( lfObjFunc >= 0.0 )	lfFitProb = 1.0/( 1.0+lfObjFunc );
//			else			lfFitProb = 1.0+Math.abs( lfObjFunc );
			lfFitProb = lfObjFunc;
			lfRes += lfFitProb;
			plfFit[j] = lfFitProb;
		}
		// 適応度の正規化
		for( j = 0;j < iAbcSearchNum; j++ )	plfFitProb[j] = plfFit[j]/lfRes;
		// ルーレット戦略を実行
		lfProb = lfPrevProb = 0.0;
		lfRand = rnd.NextUnif();
		c = 0;
		for( j = 0;j < iAbcSearchNum; j++ )
		{
			lfProb += plfFitProb[j];
			if( lfPrevProb <= lfRand && lfRand <= lfProb )	c = j;
			lfPrevProb = lfProb;
		}

		// ルーレット選択した探索点に対して更新候補を求めます。

		// 更新点候補を算出します。
		// 更新点候補を乱数により決定します。
		m = rnd.NextInt(iAbcSearchNum);
		h = rnd.NextInt(iAbcVectorDimNum);

//		m = rnd.NextInt( iAbcSearchNum-1 );
//		h = rnd.NextInt( iAbcVectorDimNum-12 );

		lfRand = rnd.NextUnif();
		for( j = 0; j < iAbcVectorDimNum; j++ )
			pplfVelocityData[c][j] = pplfAbcData[c][j];
		pplfVelocityData[c][h] = pplfAbcData[c][h] + lfRand*(plfGlobalMinAbcData[h] - pplfAbcData[c][h] );
		pfvConstraintCondition.vConstraintCondition( pplfVelocityData[c] );

		// 更新点候補を次のように更新します。
		lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[c] );
		lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[c] );

		if( lfFunc1 < lfFunc2 )
		{
			for( j = 0;j < iAbcVectorDimNum; j++ )
				pplfAbcData[c][j] = pplfVelocityData[c][j];
			piNonUpdateCount[c] = 0;
		}
		else	piNonUpdateCount[c] = piNonUpdateCount[c] + 1;
	}

	/**
	 * <PRE>
	 *   Onlooker Beeを実行します。(GBest版)
	 *   ver 0.1 2016/08/10 初版
	 *   ver 0.2 2016/09/13 NBest版を追加
	 *   ver 0.3 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/10
	 * @version 0.3
	 */
	void vOnlookerBeeGBest()
	{
		int i,j;
		int c,m,h;
		double lfRes = 0.0;
		double lfRand = 0.0;
		double lfRand2 = 0.0;
		double lfFitProb = 0.0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfObjFunc = 0.0;

		lfRes = 0.0;
		for(j = 0;j < iAbcSearchNum; j++ )
		{
			// 適応度の算出
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[j] );
	//		if( lfObjFunc >= 0.0 )	lfFitProb = 1.0/( 1.0+lfObjFunc );
	//		else					lfFitProb = 1.0+Math.abs( lfObjFunc );
			lfFitProb = lfObjFunc;
			lfRes += lfFitProb;
			plfFit[j] = lfFitProb;
		}
		// 適応度の正規化
		for( j = 0;j < iAbcSearchNum; j++ ) plfFitProb[j] = plfFit[j]/lfRes;
		// ルーレット戦略を実行
		lfProb = lfPrevProb = 0.0;
		lfRand = rnd.NextUnif();
		c = 0;
		for( j = 0;j < iAbcSearchNum; j++ )
		{
			lfProb += plfFitProb[j];
			if( lfPrevProb <= lfRand && lfRand <= lfProb )	c = j;
			lfPrevProb = lfProb;
		}

		// ルーレット選択した探索点に対して更新候補を求めます。
		// 更新点候補を算出する。
		// 更新点候補を乱数により決定する。
		m = rnd.NextInt(iAbcSearchNum);
//		h = rnd.NextInt(iAbcVectorDimNum);

//		m = rnd.NextInt( iAbcSearchNum-1 );
		h = rnd.NextInt( iAbcVectorDimNum - 12 );

		lfRand = 2.0*rnd.NextUnif()-1.0;
		lfRand2 = 1.5*rnd.NextUnif();
		for( j = 0; j < iAbcVectorDimNum; j++ )
//			pplfVelocityData[c][j] = pplfAbcData[c][j] + lfRand*(pplfAbcData[c][j] - pplfAbcData[m][j]) + lfRand2*(plfGlobalMinAbcData[j] - pplfAbcData[c][j]);
			pplfVelocityData[c][j] = pplfAbcData[c][j];
		pplfVelocityData[c][h] = pplfAbcData[c][h] + lfRand*( pplfAbcData[c][h] - pplfAbcData[m][h] ) + lfRand2*( plfGlobalMinAbcData[h] - pplfAbcData[c][h] );
		pfvConstraintCondition.vConstraintCondition( pplfVelocityData[c] );

		// 更新点候補を次のように更新します。
		lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[c] );
		lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[c] );

		if( lfFunc1 < lfFunc2 )
		{
			for( j = 0;j < iAbcVectorDimNum; j++ )
				pplfAbcData[c][j] = pplfVelocityData[c][j];
			piNonUpdateCount[c] = 0;
		}
		else
		{
			piNonUpdateCount[c] = piNonUpdateCount[c] + 1;
//			piTotalNonUpdateCount[c] = piTotalNonUpdateCount[c] + 1;
		}
	}

	/**
	 * <PRE>
	 *   Onlooker Beeを実行します。(IWCFA版)
	 *   ver 0.1 2016/08/10 初版
	 *   ver 0.2 2016/09/13 IWCFA版を追加
	 *   ver 0.3 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 * </PRE>
	 * @param lfK			全体の速度調整パラメータ
	 * @param lfCoe1		対象探索点からの離れ具合調整パラメータ
	 * @param lfCoe2		最適探索点からの離れ具合調整パラメータ
	 * @param iUpdateCount	現世代数
	 * @author kobayashi
	 * @since 2016/8/10
	 * @version 0.3
	 */
	private void vOnlookerBeeIWCFA( double lfK, double lfCoe1, double lfCoe2, int iUpdateCount )
	{
		int i,j;
		int c,m,h;
		double lfRes = 0.0;
		double lfRand = 0.0;
		double lfRand2 = 0.0;
		double lfFitProb = 0.0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfObjFunc = 0.0;
		double lfWeight = 0.0;
		double lfMaxWeight = 1.0;
		double lfMinWeight = 0.3;

		lfRes = 0.0;
		for(j = 0;j < iAbcSearchNum; j++ )
		{
			// 適応度の算出
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[j] );
			if( lfObjFunc >= 0.0 )	lfFitProb = 1.0/( 1.0+lfObjFunc );
			else					lfFitProb = 1.0+Math.abs( lfObjFunc );
			lfRes += lfFitProb;
			plfFit[j] = lfFitProb;
		}
		// 適応度の正規化
		for( j = 0;j < iAbcSearchNum; j++ ) plfFitProb[j] = plfFit[j]/lfRes;
		// ルーレット戦略を実行
		lfProb = lfPrevProb = 0.0;
		lfRand = rnd.NextUnif();
		c = 0;
		for( j = 0;j < iAbcSearchNum; j++ )
		{
			lfProb += plfFitProb[j];
			if( lfPrevProb <= lfRand && lfRand <= lfProb )	c = j;
			lfPrevProb = lfProb;
		}

		// ルーレット選択した探索点に対して更新候補を求めます。

		// 更新点候補を算出する。
		// 更新点候補を乱数により決定する。
		m = rnd.NextInt(iAbcSearchNum);
//		h = rnd.NextInt(iAbcVectorDimNum);

//		m = rnd.NextInt( iAbcSearchNum-1 );
		h = rnd.NextInt( iAbcVectorDimNum - 12 );

		lfRand = 2.0*rnd.NextUnif()-1.0;
		lfRand2 = 2.0*rnd.NextUnif()-1.0;
		for( j = 0; j < iAbcVectorDimNum; j++ )
			pplfVelocityData[c][j] = pplfAbcData[c][j];
		lfWeight = lfMaxWeight - (lfMaxWeight-lfMinWeight)/(double)iGenerationNumber*(double)(iUpdateCount-piTotalNonUpdateCount[c]);
		pplfVelocityData[c][h] = lfK*(pplfAbcData[c][h] + lfCoe1*lfRand*( pplfAbcData[c][h] - pplfAbcData[m][h] ) + lfCoe2*lfRand2*( pplfAbcData[c][h] - plfGlobalMinAbcData[h] ) );
		pfvConstraintCondition.vConstraintCondition( pplfVelocityData[c] );

		// 更新点候補を次のように更新します。
		lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[c] );
		lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[c] );

		if( lfFunc1 < lfFunc2 )
		{
			for( j = 0;j < iAbcVectorDimNum; j++ )
				pplfAbcData[c][j] = pplfVelocityData[c][j];
			piNonUpdateCount[c] = 0;
		}
		else
		{
			piNonUpdateCount[c] = piNonUpdateCount[c] + 1;
			piTotalNonUpdateCount[c] = piTotalNonUpdateCount[c] + 1;
		}
	}

	/**
	 * <PRE>
	 *   Onlooker Beeを実行します。
	 *   Randomized Memtic Bee Colony Method用
	 *   ver 0.1 2016/09/22 初版
	 *   ver 0.2 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 *   ver 0.3 2016/10/28 一部誤りがあり修正。
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/9/22
	 * @version 0.3
	 */
	private void vOnlookerBeeRM()
	{
		int i,j;
		int c,m,h;
		double lfRes = 0.0;
		double lfRand = 0.0;
		double lfRand2 = 0.0;
		double lfFitProb = 0.0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfObjFunc = 0.0;

		lfRes = 0.0;
		for(j = 0;j < iAbcSearchNum; j++ )
		{
			// 適応度の算出
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[j] );
			if( lfObjFunc >= 0.0 )	lfFitProb = 1.0/( 2.0*lfObjFunc+1.0 );
			else			lfFitProb = 1.0+Math.abs( 1.0/lfObjFunc );
			lfRes += lfFitProb;
			plfFit[j] = lfFitProb;
		}
		// 適応度の正規化
		for( j = 0;j < iAbcSearchNum; j++ )	plfFitProb[j] = plfFit[j]/lfRes;
		// ルーレット戦略を実行
		lfProb = lfPrevProb = 0.0;
		lfRand = rnd.NextUnif();
		c = 0;
		for( j = 0;j < iAbcSearchNum; j++ )
		{
			lfProb += plfFitProb[j];
			if( lfPrevProb <= lfRand && lfRand <= lfProb )	c = j;
			lfPrevProb = lfProb;
		}
		// ルーレット選択した探索点に対して更新候補を求めます。

		// 更新点候補を算出します。
		// 更新点候補を乱数により決定します。
		m = rnd.NextInt(iAbcSearchNum);
//		h = rnd.NextInt(iAbcVectorDimNum);

//		m = rnd.NextInt( iAbcSearchNum-1 );
		h = rnd.NextInt( iAbcVectorDimNum - 12 );

		lfRand = 2.0*rnd.NextUnif()-1.0;
		lfRand2 = 1.5*rnd.NextUnif();
		for( j = 0; j < iAbcVectorDimNum; j++ )
			pplfVelocityData[c][j] = pplfAbcData[c][j];
		pplfVelocityData[c][h] = pplfAbcData[c][h] + lfRand*( pplfAbcData[c][h] - pplfAbcData[m][h] ) + lfRand2*( plfGlobalMinAbcData[h] - pplfAbcData[c][h] );
		pfvConstraintCondition.vConstraintCondition( pplfVelocityData[c] );

		// 更新点候補を次のように更新します。
		lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[c] );
		lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[c] );

		if( lfFunc1 < lfFunc2 )
		{
			for( j = 0;j < iAbcVectorDimNum; j++ )
				pplfAbcData[c][j] = pplfVelocityData[c][j];
			piNonUpdateCount[c] = 0;
		}
		else	piNonUpdateCount[c] = piNonUpdateCount[c] + 1;
	}

	/**
	 * <PRE>
	 *   Onlooker Beeを実行します。(Hooke-Jeeves法用)
	 *   Artificial Bee Colony Algorithm ith Local Search for Numerical Optimization, Jornal of Software, Vol.6, no.3, march 2011より
	 *   ver 0.1 2016/10/03 初版
	 *   ver 0.2 2016/10/25 更新候補点の算出に誤りを発見し修正。
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/10/3
	 * @version 0.2
	 */
	private void vOnlookerBeeHJ()
	{
		int i,j;
		int c,m,h;
		double lfRes = 0.0;
		double lfRand = 0.0;
		double lfFitProb = 0.0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfObjFunc = 0.0;
		ArrayList<Rank_t> stlRank;
		Rank_t tempRank;
		double lfSP = 1.5;

		stlRank = new ArrayList<Rank_t>();
		tempRank = new Rank_t();

		// 適応度でソートする。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );
			tempRank.iLoc = i;
			tempRank.lfFitProb = lfObjFunc;
			stlRank.add( tempRank );
		}
//		std::sort( stlRank.begin(), stlRank.end(), CCompareToRank() );
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

		lfRes = 0.0;
		for(j = 0;j < iAbcSearchNum; j++ )
		{
			// 適応度の算出
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[stlRank.get(j).iLoc] );
			lfFitProb = 2.0-lfSP+2.0*(lfSP-1.0)*(j-1.0)/( iAbcSearchNum-1.0 );
			lfRes += lfFitProb;
			plfFit[stlRank.get(j).iLoc] = lfFitProb;
		}
		// 適応度の正規化
		for( j = 0;j < iAbcSearchNum; j++ )	plfFitProb[j] = plfFit[stlRank.get(j).iLoc]/lfRes;
		// ルーレット戦略を実行
		lfProb = lfPrevProb = 0.0;
		lfRand = rnd.NextUnif();
		c = 0;
		for( j = 0;j < iAbcSearchNum; j++ )
		{
			lfProb += plfFitProb[stlRank.get(j).iLoc];
			if( lfPrevProb <= lfRand && lfRand <= lfProb )	c = stlRank.get(j).iLoc;
			lfPrevProb = lfProb;
		}

		// ルーレット選択した探索点に対して更新候補を求めます。
		// 更新点候補を算出します。
		// 更新点候補を乱数により決定します。
		m = rnd.NextInt(iAbcSearchNum);
//		h = rnd.NextInt(iAbcVectorDimNum);
		h = rnd.NextInt(iAbcVectorDimNum-12);
		lfRand = 2*rnd.NextUnif()-1;
		for( j = 0; j < iAbcVectorDimNum; j++ )
			pplfVelocityData[c][j] = pplfAbcData[c][j];
		pplfVelocityData[c][h] = pplfAbcData[c][h] + lfRand*( pplfAbcData[c][h] - pplfAbcData[m][h] );
		// 更新点候補を次のように更新します。
		lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction( pplfVelocityData[c] );
		lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[c] );

		if( lfFunc1 < lfFunc2 )
		{
			for( j = 0;j < iAbcVectorDimNum; j++ )
				pplfAbcData[c][j] = pplfVelocityData[c][j];
			piNonUpdateCount[c] = 0;
		}
		else	piNonUpdateCount[c] = piNonUpdateCount[c] + 1;
	}

	/**
	* <PRE>
	*   Onlooker Beeを実行します。(AC-ABC用算術交叉を利用したABC法。)
	*   算術交叉を用いた改良型Artificial Bee Colony アルゴリズム 第28回ファジーシンポジウム, 2012.9.12～14
	*   ver 0.1 2016/10/13 初版
	*   ver 0.2 2016/10/25 更新候補点の算出に誤りを発見し修正。
	* </PRE>
	* @author kobayashi
	* @since 2016/10/13
	* @version 0.2
	*/
	private void vOnlookerBeeAC()
	{
		int i, j;
		int c1, c2, m, h;
		double lfRes = 0.0;
		double lfRand = 0.0;
		double lfFitProb = 0.0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfObjFunc = 0.0;
		double lfRand1 = 0.0;
		double lfRand2 = 0.0;

		// 論文記載の内容より。
		double lfLambda = 0.1;
		double lfCrossOverRate = 0.1;

		lfRes = 0.0;
		for (j = 0; j < iAbcSearchNum; j++)
		{
			// 適応度の算出
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[j]);
	//		if (lfObjFunc >= 0.0)	lfFitProb = 1.0 / (1.0 + lfObjFunc);
	//		else					lfFitProb = 1.0 + Math.abs(lfObjFunc);
			lfFitProb = lfObjFunc;
			lfRes += lfFitProb;
			plfFit[j] = lfFitProb;
		}
		// 適応度の正規化
		for (j = 0; j < iAbcSearchNum; j++)	plfFitProb[j] = plfFit[j] / lfRes;
		// ルーレット戦略を実行
		lfProb = lfPrevProb = 0.0;
		lfRand = rnd.NextUnif();
		c1 = c2 = 0;
		for (j = 0; j < iAbcSearchNum; j++)
		{
			lfProb += plfFitProb[j];
			if (lfPrevProb <= lfRand && lfRand <= lfProb)
			{
				c1 = j;
				// 簡単な乱択アルゴリズム
				if (j == 0) c2 = rnd.NextInt(iAbcSearchNum + 1);
				else	    c2 = rnd.NextInt(j);
			}
			lfPrevProb = lfProb;
		}
		// ルーレット選択した探索点に対して更新候補を求めます。

		// 更新点候補を算出します。
		// 更新点候補を乱数により決定します。
		m = rnd.NextInt(iAbcSearchNum);
//		h = rnd.NextInt(iAbcVectorDimNum);
		h = rnd.NextInt(iAbcVectorDimNum-12);

		lfRand = rnd.NextUnif();
		for (j = 0; j < iAbcVectorDimNum; j++)
		{
			if (lfRand < lfCrossOverRate )
			{
				pplfVelocityData[m][j] = lfLambda*pplfAbcData[c1][j] + (1.0 - lfLambda)*pplfAbcData[c2][j];
			}
			else
			{
				pplfVelocityData[m][j] = pplfAbcData[m][j];
			}
		}

		// 更新点候補を次のように更新します。
		lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction(pplfVelocityData[m]);
		lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[m]);
		if (lfFunc1 < lfFunc2)
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
				pplfAbcData[m][j] = pplfVelocityData[m][j];
			piNonUpdateCount[m] = 0;
		}
		else	piNonUpdateCount[m] = piNonUpdateCount[m] + 1;
	}

	/**
	 * <PRE>
	 *   Onlooker Beeを実行します。
	 *   Best-so-far Artificial Bee Colony Method用
	 *   The best-so-far seelction in ARtificial Bee Colony algorithm, Applied Soft Computing 11 (2011) 2888-2901.
	 *   ver 0.1 2016/10/28 初版
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/10/28
	 * @version 0.1
	 */
	private void vOnlookerBeeBF()
	{
		int i, j;
		int b, c, m, h;
		double lfRes = 0.0;
		double lfRand = 0.0;
		double lfFitProb = 0.0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfObjFunc = 0.0;
		double lfLocalBestRand = 0.0;

		ArrayList<Integer> stlLoc = new ArrayList<Integer>();

		lfLocalBestRand = pflfObjectiveFunction.lfObjectiveFunction(plfLocalMinAbcData);
		lfRes = 0.0;
		for (j = 0; j < iAbcSearchNum; j++)
		{
			// 適応度の算出
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[j]);
			if (lfObjFunc >= 0.0)	lfFitProb = 1.0 / (1.0 + lfObjFunc);
			else			lfFitProb = 1.0 + Math.abs(lfObjFunc);
	//		lfFitProb = lfObjFunc;
			lfRes += lfFitProb;
			plfFit[j] = lfFitProb;
		}
		// 適応度の正規化
		for (j = 0; j < iAbcSearchNum; j++)	plfFitProb[j] = plfFit[j] / lfRes;
		lfLocalBestRand /= lfRes;

		for (i = 0; i < iAbcSearchNum; i++)
		{
			// ルーレット戦略を実行
			lfProb = lfPrevProb = 0.0;
			lfRand = rnd.NextUnif();
			c = 0;
			b = 0;
			for (j = 0; j < iAbcSearchNum; j++)
			{
				lfProb += plfFitProb[j];
				if (lfPrevProb <= lfRand && lfRand <= lfProb)	c = j;
				lfPrevProb = lfProb;
			}
			stlLoc.add(c);
			// ルーレット選択した探索点に対して更新候補を求めます。

			// 更新点候補を算出します。
			// 更新点候補を乱数により決定します。
//			h = rnd.NextInt(iAbcVectorDimNum);
			h = rnd.NextInt( iAbcVectorDimNum - 12 );

			for (j = 0; j < iAbcVectorDimNum-12; j++)
			{
				lfRand = 2.0*rnd.NextUnif() - 1.0;
				pplfVelocityData[i][j] = pplfAbcData[c][h] + lfRand*lfLocalBestRand*(pplfAbcData[c][h] - plfLocalMinAbcData[h]);
//				pfvConstraintCondition.vConstraintCondition( pplfVelocityData[i] );
			}
			for (j = iAbcVectorDimNum - 12; j < iAbcVectorDimNum; j++)
				pplfVelocityData[i][j] = pplfAbcData[i][j];
			pfvConstraintCondition.vConstraintCondition( pplfVelocityData[i] );
			// 更新点候補を次のように更新します。
			lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction(pplfVelocityData[i]);
			lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[c]);
			if (lfFunc1 < lfFunc2)
			{
				for (j = 0; j < iAbcVectorDimNum; j++)
					pplfAbcData[c][j] = pplfVelocityData[i][j];
				piNonUpdateCount[c] = 0;
			}
			else	piNonUpdateCount[c] = piNonUpdateCount[c] + 1;
		}
	}

   /**
	* <PRE>
	*   Ivona Brajevic, Crossover-based artificial bee colony algorithm for constrained optimization problems, Neural Computing and Application (2015) 26:1587-1601.
	*   ver 0.1 2016/10/19 初版
	*   ver 0.2 2016/10/25 更新候補点の算出に誤りを発見し修正。
	* </PRE>
	* @param lfMr	値更新用パラメータ
	* @author kobayashi
	* @since 2016/10/19
	* @version 0.2
	*/
	private void vOnlookerBeeCB(double lfMr)
	{
		int i, j;
		int c, m, h;
		double lfRes = 0.0;
		double lfRand = 0.0;
		double lfFitProb = 0.0;
		double lfProb = 0.0;
		double lfPrevProb = 0.0;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;
		double lfObjFunc = 0.0;
		double lfMrMax = 0.9;
		double lfP = 0.3;
		double lfMCN = iGenerationNumber;
		double lfLowerVelocity = -Double.MAX_VALUE;
		double lfUpperVelocity = Double.MAX_VALUE;
		double lfMaxFit =-Double.MAX_VALUE;
		double lfDelta = 0.0;

		lfRes = 0.0;
		for (j = 0; j < iAbcSearchNum; j++)
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[j]);
			plfFit[j] = lfObjFunc;
			lfMaxFit = plfFit[j] < lfMaxFit ? lfMaxFit : plfFit[j];
		}
		lfRes = 0.0;
		for (j = 0; j < iAbcSearchNum; j++)
		{
			// 適応度の算出
			plfFit[j] = 0.9*(plfFit[j] / lfMaxFit) + 0.1;
			lfRes += plfFit[j];
		}
		// 適応度の正規化
		for (j = 0; j < iAbcSearchNum; j++)	plfFitProb[j] = plfFit[j] / lfRes;
		// ルーレット戦略を実行
		lfProb = lfPrevProb = 0.0;
		lfRand = rnd.NextUnif();
		c = 0;
		for (j = 0; j < iAbcSearchNum; j++)
		{
			lfProb += plfFitProb[j];
			if (lfPrevProb <= lfRand && lfRand <= lfProb)	c = j;
			lfPrevProb = lfProb;
		}
		// ルーレット選択した探索点に対して更新候補を求めます。

		// 更新点候補を算出します。
		// 更新点候補を乱数により決定します。
		m = rnd.NextInt(iAbcSearchNum);
		h = rnd.NextInt(iAbcSearchNum);

//		m = rnd.NextInt( iAbcSearchNum-1 );
//		h = rnd.NextInt( iAbcVectorDimNum - 12 );

		lfMr = lfMr < lfMrMax ? lfMr + (lfMrMax - 0.1) / (lfP*lfMCN) : lfMrMax;
		for (i = 0; i < iAbcSearchNum; i++)
		{
			if (lfDelta < plfFitProb[i])
			{
				lfRand = 2 * rnd.NextUnif() - 1;
				for (j = 0; j < iAbcVectorDimNum; j++)
				{
					pplfVelocityData[i][j] = rnd.NextUnif() < lfMr ? pplfAbcData[i][j] + lfRand*(pplfAbcData[m][j] - pplfAbcData[h][j]) : pplfAbcData[i][j];
					if (pplfVelocityData[i][j] < lfLowerVelocity) pplfVelocityData[i][j] = 2.0*lfLowerVelocity - pplfVelocityData[i][j];
					else if (pplfVelocityData[i][j] > lfUpperVelocity) pplfVelocityData[i][j] = 2.0*lfUpperVelocity - pplfVelocityData[i][j];
					pfvConstraintCondition.vConstraintCondition( pplfVelocityData[i] );
				}
			}
		}
		// 各探索点と更新しなかった回数を格納する変数を更新します。
		for (i = 0; i < iAbcSearchNum; i++)
		{
			lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction(pplfVelocityData[i]);
			lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[i]);

			if (lfFunc1 < lfFunc2)
			{
				for (j = 0; j < iAbcVectorDimNum; j++)
					pplfAbcData[i][j] = pplfVelocityData[i][j];
				piNonUpdateCount[i] = 0;
			}
			else	piNonUpdateCount[i] = piNonUpdateCount[i] + 1;
		}
	}

	/**
	 * <PRE>
	 * Scout Beeを実行します。(大本の手法)
	 * ver 0.1
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/10
	 * @version 0.2
	 */
	private void vScoutBeeOrigin()
	{
		int i,k;
		double lfRand = 0.0;
		// 新たな探索点を求めて探索を実行します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			if( piNonUpdateCount[i] > iAbcLimitCount )
			{
				for( k = 0;k < iAbcVectorDimNum; k++ )
				{
					lfRand = rnd.NextUnif();
					pplfAbcData[i][k] = lfSolveRange*(2.0*lfRand-1.0);
				}
				// 制約条件を満たしているかどうかチェックを行う。
				pfvConstraintCondition.vConstraintCondition( pplfAbcData[i] );
			}
		}
	}

	/**
	 * <PRE>
	 * Scout Beeを実行します。
	 * ver 0.1 2016.8.10 初版
	 * ver 0.2 2016.8.11 手法の変更。（粒子群最適化法のような更新手法。論文より）
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/10
	 * @version 0.2
	 */
	private void vScoutBeeNormal()
	{
		int i,j,k;
		double lfRand = 0.0;
		// 新たな探索点を求めて探索を実行します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			if( piNonUpdateCount[i] > iAbcLimitCount )
			{
				for( k = 0;k < iAbcVectorDimNum; k++ )
				{
					lfRand = rnd.NextUnif();
					pplfAbcData[i][k] = plfGlobalMinAbcData[k] + lfRand*(plfGlobalMaxAbcData[k]-plfGlobalMinAbcData[k]);
				}
				// 制約条件を満たしているかどうかチェックを行う。
				pfvConstraintCondition.vConstraintCondition( pplfAbcData[i] );
			}
		}
	}

	/**
	 * <PRE>
	 * Scout Beeを実行します。（完全アレンジ版UNDXを実行する。）
	 * ver 0.1 2016.8.18 初版
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/18
	 * @version 0.1
	 */
	private void vScoutBeeUndx()
	{
		int i,j,k;
		int i1;
		int i2 = -1;
		int iMaxCount = -Integer.MAX_VALUE;
		int iCurCount = 0;
		double lfRand = 0.0;
		// 新たな探索点を求めて探索を実行します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			if( piNonUpdateCount[i] > iAbcLimitCount )
			{
				i1 = i;
				// 2番目にカウントの大きなものを探します。
				for (j = 0; j < iAbcSearchNum; j++)
				{
					if ( i1 != j && iMaxCount < piNonUpdateCount[j])
					{
						i2 = j;
						iMaxCount = piNonUpdateCount[j];
					}
				}
				// 交叉を実行します。ここでUNDXを実行します。（１回分のみ。）
				pcUndx.vSetGenData(pplfAbcData );
				pcUndx.vImplement();
//				pcUndx.vGetBestGenData(pplfAbcData[i] );
				pcUndx.vGetGenData(pplfAbcData );
//				for (j = 0; j < iAbcSearchNum; j++)
//					piNonUpdateCount[j] = 0;
				if( i2 == -1 )
					i2 = pcUndx.iGet2ndLoc();
				piNonUpdateCount[i1] = 0;
				piNonUpdateCount[i2] = 0;
				pfvConstraintCondition.vConstraintCondition(pplfAbcData[i1]);
				pfvConstraintCondition.vConstraintCondition(pplfAbcData[i2]);
				break;
			}
		}
	}

	/**
	 * <PRE>
	 * Scout Beeを実行します。（完全アレンジ版REXを実行する。）
	 * ver 0.1 2016.8.26 初版
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/26
	 * @version 0.1
	 */
	private void vScoutBeeRex()
	{
		int i,j,k;
		double lfRand = 0.0;
		// 新たな探索点を求めて探索を実行します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			if( piNonUpdateCount[i] > iAbcLimitCount )
			{
				// 交叉を実行します。ここでREXを実行します。（１回分のみ。）
				pcRex.vSetGenData(pplfAbcData );
				pcRex.vRex();
//				pcRex.vGetBestGenData(pplfAbcData[i] );
//				pcRex.vGetBest2ndGenData(pplfAbcData );
				pcRex.vGetGenData(pplfAbcData );
				for (j = 0; j < iAbcSearchNum; j++)
				{
					// 更新していないカウントを0にします。
					piNonUpdateCount[j] = 0;

				}
				// 制約条件を満たしているかどうかチェックを行います。
				pfvConstraintCondition.vConstraintCondition( pplfAbcData[j] );
				break;
			}
		}
	}

	/**
	 * <PRE>
	 * Scout Beeを実行します。（完全アレンジ版AREXを実行する。）
	 * ver 0.1 2016.8.27 初版
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/27
	 * @version 0.1
	 */
	private void vScoutBeeARex()
	{
		int i,j,k;
		double lfRand = 0.0;
		// 新たな探索点を求めて探索を実行します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			if( piNonUpdateCount[i] > iAbcLimitCount )
			{
				// 交叉を実行します。ここでAREXを実行します。（１回分のみ。）
				pcRex.vSetGenData(pplfAbcData );
				pcRex.vARex();
//				pcRex.vGetBestGenData( pplfAbcData[i] );
//				pcRex.vGetBest2ndGenData(pplfAbcData);
				pcRex.vGetGenData(pplfAbcData );
				for (j = 0; j < iAbcSearchNum; j++)
				{
					// 更新していないカウントを0にします。
					piNonUpdateCount[j] = 0;

				}
				// 制約条件を満たしているかどうかチェックを行います。
				pfvConstraintCondition.vConstraintCondition( pplfAbcData[j] );
				break;
			}
		}
	}

	/**
	* <PRE>
	*   Ivona Brajevic, Crossover-based artificial bee colony algorithm for constrained optimization problems, Neural Computing and Application (2015) 26:1587-1601.
	*   ver 0.1 2016.10.19 初版
	* </PRE>
	* @param iCount 未更新回数
	* @author kobayashi
	* @since 2016/10/19
	* @version 0.1
	*/
	private void vScoutBeeCB( int iCount )
	{
		int i, j, k;
		double lfRand = 0.0;
		// 新たな探索点を求めて探索を実行します。
		if (iCount % iAbcLimitCount == 0)
		{
			for (i = 0; i < iAbcSearchNum; i++)
			{
				for (k = 0; k < iAbcVectorDimNum; k++)
				{
					pplfVelocityData[i][k] = rnd.NextUnif() < 0.5 ? plfGlobalMaxAbcData[k] : pplfAbcData[i][k];
				}
			}
		}
	}

	/**
	 * <PRE>
	 * Scout Beeを実行します。(Best-so-far Artificial Bee Colony Method用)
	 * The best-so-far selection in Artificial Bee colony algorithm, Applied Soft Computing 11 (2011) 2888-2901.
	 * ver 0.1 初版 2016.10.28
	 * </PRE>
	 * @param iUpdateCount	現在の世代数
	 * @author kobayashi
	 * @since 2016/10/28
	 * @version 0.1
	 */
	private void vScoutBeeBF( int iUpdateCount )
	{
		int i,j,k;
		double lfRand = 0.0;
		double lfMaxWeight = 1.0;
		double lfMinWeight = 0.2;
		double lfFunc1 = 0.0;
		double lfFunc2 = 0.0;

		// 新たな探索点を求めて探索を実行します。
		for( i = 0;i < iAbcSearchNum; i++ )
		{
			if( piNonUpdateCount[i] > iAbcLimitCount )
			{
				for( k = 0;k < iAbcVectorDimNum; k++ )
				{
					lfRand = 2.0*rnd.NextUnif()-1.0;
					plfScoutBeeResult[k] = pplfAbcData[i][k] + lfRand*(lfMaxWeight-(double)iUpdateCount/(double)iGenerationNumber*(lfMaxWeight-lfMinWeight))*pplfAbcData[i][k];
				}
				lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction(plfScoutBeeResult);
				lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[i]);

				if (lfFunc1 < lfFunc2)
				{
					for (k = 0; k < iAbcVectorDimNum; k++)
						pplfAbcData[i][k] = plfScoutBeeResult[k];
					piNonUpdateCount[i] = 0;
				}
				else	piNonUpdateCount[i] = piNonUpdateCount[i] + 1;

				// 制約条件を満たしているかどうかチェックを行います。
				pfvConstraintCondition.vConstraintCondition( pplfAbcData[i] );
			}
		}
	}

	/**
	 * <PRE>
	 * Powell法を実行します。
	 * </PRE>
	 * @param iUpdateCount	現在の世代数
	 * @author kobayashi
	 * @since 2016/10/28
	 * @version 0.1
	 */
	private void vPowell(int iUpdateCount)
	{
		int i, k;
		int[] iUpdateFlag;
		int[] iInterval;
		double[] lfRet;
		double lfEpsilon = 0.000000001;
		double lfFunc1, lfFunc2;

		iUpdateFlag = new int[1];
		iInterval = new int[1];
		lfRet = new double[1];

		iUpdateFlag[0] = iAbcVectorDimNum + iAbcVectorDimNum;
		if ((iUpdateCount % iUpdateFlag[0]) == 0 )
		{
			k = rnd.NextInt(iAbcSearchNum);
			for (i = 0; i < iAbcVectorDimNum; i++)
			{
				plfVelocity[i] = pplfAbcData[k][i] + rnd.NextUnif()*( plfGlobalMinAbcData[i]-pplfAbcData[k][i] );
			}
			// d次元のベクトルplfVelocityData及び方向ベクトルplfDirection及び次元数を与えると最小点及び最小点での値が返却される。
			for (i = 0; i < iAbcSearchNum; i++)
			{
				for(k = 0; k < pplfNVelocityData.length; k++) pplfNVelocityData[i][k] = 0.0;
				if (i < iAbcVectorDimNum )
					pplfNVelocityData[i][i] = 1.0;
			}
			pcPowell.vPowell(plfVelocity, pplfNVelocityData, iAbcVectorDimNum, lfEpsilon, iInterval, lfRet );

			lfFunc1 = pflfObjectiveFunction.lfObjectiveFunction(plfVelocity);
			lfFunc2 = pflfObjectiveFunction.lfObjectiveFunction(pplfAbcData[k]);

			if (lfFunc1 < lfFunc2)
			{
				for (i = 0; i < iAbcVectorDimNum; i++)
					pplfAbcData[k][i] = plfVelocity[i];
				piNonUpdateCount[k] = 0;
			}
			else	piNonUpdateCount[k] = piNonUpdateCount[k] + 1;

		}
	}

	/**
	 * <PRE>
	 * 現時点での目的関数の最大、最小を求めます。
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/10
	 * @version 0.1
	 */
	private void vGetLocalMaxMin()
	{
		int i,j;
		double lfFunc = 0.0;
		double lfMin = 0.0;
		double lfMax = 0.0;

		lfMin = lfMax = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[0] );
		// ローカルの最大値を更新します。
		for( i = 1;i < iAbcSearchNum; i++ )
		{
			lfFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );

			if( lfMin > lfFunc )
			{
				for( j = 0;j < iAbcVectorDimNum; j++ )
					pplfLocalMinAbcData[i][j] = pplfAbcData[i][j];
				lfMin = lfFunc;
			}
			if( lfMax < lfFunc )
			{
				for( j = 0;j < iAbcVectorDimNum; j++ )
					pplfLocalMaxAbcData[i][j] = pplfAbcData[i][j];
				lfMax = lfFunc;
			}
		}
	}


	/**
	 * <PRE>
	 * 現時点での目的関数の全体を通しての最大、最小値を求めます。
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/8/10
	 * @version 0.1
	 */
	private void vGetGlobalMaxMin()
	{
		int i,j;
		int iMinLoc = 0;
		double lfObjFunc = 0.0;

		for( i = 0;i < iAbcSearchNum; i++ )
		{
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] );
			if( lfGlobalMinAbcData >= lfObjFunc )
			{
				iMinLoc = i;
				lfGlobalMinAbcData = lfObjFunc;
				for( j = 0; j < iAbcVectorDimNum; j++ )
					plfGlobalMinAbcData[j] = pplfAbcData[i][j];
				lfFitCurrentBest = lfGlobalMinAbcData;
			}
			if( lfGlobalMaxAbcData <= lfObjFunc )
			{
				iMinLoc = i;
				lfGlobalMaxAbcData = lfObjFunc;
				for( j = 0; j < iAbcVectorDimNum; j++ )
					plfGlobalMaxAbcData[j] = pplfAbcData[i][j];
			}
		}
	}

	/**
	 * <PRE>
	 * 　現時点での最小値の粒子の目的関数値を出力します。
	 * </PRE>
	 * @return 現時点での各粒子の目的関数の値
	 * @author kobayashi
	 * @since 2016/9/14
	 * @version 1.0
	 */
	public double lfGetGlobalMinAbcDataConstFuncValue()
	{
		// 現時点での各粒子の目的関数の値を出力します。
		return lfGlobalMinAbcData;
	}

	/**
	 * <PRE>
	 * 　ABC法を適用した結果を出力します。(各蜂の位置)
	 * </PRE>
	 * @return 現時点での各蜂の位置
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 1.0
	 */
	public String strOutputAbcData()
	{
		int i,j;
		String str = new String();
		// 現時点での蜂の位置を出力します。
		for( i = 0; i < iAbcDataNum; i++ )
		{
			for( j = 0;j < iAbcVectorDimNum; j++ )
			{
				str += Double.toString( pplfAbcData[i][j] ) + ",";
			}
		}
		return str;
	}

	/**
	 * <PRE>
	 * 　ABC法を適用した結果を出力します。(各ABCの速度)
	 * </PRE>
	 * @return 現時点での各蜂の速度
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 1.0
	 */
	public String strOutputVelocityData()
	{
		int i,j;
		String str = new String();
		// 現時点での蜂の速度を出力します。
		for( i = 0; i < iAbcDataNum; i++ )
		{
			for( j = 0;j < iAbcVectorDimNum; j++ )
			{
				str += Double.toString( pplfVelocityData[i][j] ) + ",";
			}
		}
		return str;
	}

	/**
	 * <PRE>
	 * 　ABC法を適用した結果を出力します。(各蜂の目的関数値)
	 * </PRE>
	 * @return 現時点での各蜂の目的関数値
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 1.0
	 */
	public String strOutputConstraintFunction()
	{
		int i;
		String str = new String();
		// 現時点での各蜂の目的関数の値を出力します。
		for( i = 0; i < iAbcDataNum; i++ )
		{
			str += Double.toString( pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] ) ) + ",";
		}
		return str;
	}

	/**
	 * <PRE>
	 * 　ABC法を適用した結果を出力します。(各蜂の目的関数値)
	 * </PRE>
	 * @param iLoc	各蜂の番号
	 * @return 現時点での各蜂の目的関数値
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 1.0
	 */
	public String strOutputSingleConstraintFunction( int iLoc )
	{
		String str = new String();
		// 現時点での各蜂の目的関数の値を出力します。
		str += Double.toString( pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[iLoc] ) ) + ",";
		return str;
	}

	/**
	 * <PRE>
	 * 　現時点でのもっともよい蜂の位置を出力します。(最大値)
	 * </PRE>
	 * @return 現時点での最も良い蜂の位置（最大値）
	 * @author kobayashi
	 * @since 2015/6/19
	 * @version 1.0
	 */
	public String strOutputGlobalMaxAbcData()
	{
		int i;
		String str = new String();
		// 現時点での各粒子の目的関数の値を出力します。
		for( i = 0; i < iAbcVectorDimNum; i++ )
		{
			str += Double.toString( plfGlobalMaxAbcData[i] ) + ",";
		}
		return str;
	}

	/**
	 * <PRE>
	 * 　現時点でのもっともよい粒子の目的関数値を出力します。(最大値)
	 * </PRE>
	 * @return 現時点での蜂の目的関数値（最大値）
	 * @author kobayashi
	 * @since 2015/6/19
	 * @version 1.0
	 */
	public double lfOutputGlobalMaxAbcDataConstFuncValue()
	{
		// 現時点での各蜂の目的関数の値を出力します。
		return pflfObjectiveFunction.lfObjectiveFunction( plfGlobalMaxAbcData );
	}

	/**
	 * <PRE>
	 * 　現時点でのもっともよい蜂の位置を出力します。(最小値)
	 * </PRE>
	 * @return 現時点での最も良い蜂の位置（最小値）
	 * @author kobayashi
	 * @since 2015/6/19
	 * @version 1.0
	 */
	public String strOutputGlobalMinAbcData()
	{
		int i;
		String str = new String();
		// 現時点での各蜂の目的関数の値を出力します。
		for( i = 0; i < iAbcVectorDimNum; i++ )
		{
			str += Double.toString( plfGlobalMinAbcData[i] ) + ",";
		}
		return str;
	}

	/**
	 * <PRE>
	 * 　現時点でのもっともよい蜂の目的関数値を出力します。(最小値)
	 * </PRE>
	 * @return もっともよい蜂の目的関数値(最小値)
	 * @author kobayashi
	 * @since 2015/6/19
	 * @version 1.0
	 */
	public double lfOutputGlobalMinAbcDataConstFuncValue()
	{
		// 現時点での各粒子の目的関数の値を出力します。
		return pflfObjectiveFunction.lfObjectiveFunction( plfGlobalMinAbcData );
	}

	/**
	 * <PRE>
	 *    現時点でのもっともよい蜂の位置とその他の粒子との距離を出力します。
	 * </PRE>
	 * @param iOutFlag	0 各蜂と他の蜂の距離のみ出力。
	 * 					1 平均距離を出力。
	 * @return 最適な探索点と現在の探索点との距離(Euclide)
	 * @author kobayashi
	 * @since 2015/6/19
	 * @version 0.1
	 */
	public double lfOutputAbcDataLocDist( int iOutFlag )
	{
		int i,j;
		double lfRes = 0.0;
		double lfAvgDist = 0.0;
		double lfDist = 0.0;
		double lfDist2 = 0.0;
		// 現時点での各粒子と他の粒子との距離を出力します。
		for( i= 0;i < iAbcDataNum; i++ )
		{
			lfRes = 0.0;
			for( j = 0;j < iAbcVectorDimNum; j++ )
			{
				lfDist = plfGlobalMaxAbcData[j]-pplfAbcData[i][j];
				lfDist2 = lfDist*lfDist;
				lfRes += lfDist2;
			}
			lfAvgDist += lfRes;
		}
		lfAvgDist /= (double)iAbcDataNum;
		return lfAvgDist;
	}

	/**
	 * <PRE>
	 * 　ABC法を適用した結果を出力します。(各蜂の位置)
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 1.0
	 */
	public void vOutputAbcData()
	{
		int i,j;
		// 現時点での蜂の位置を出力します。
		for( i = 0; i < iAbcDataNum; i++ )
		{
			for( j = 0;j < iAbcVectorDimNum; j++ )
			{
				System.out.print( pplfAbcData[i][j] + "," );
			}
			System.out.println("");
		}
	}

	/**
	 * <PRE>
	 * 　ABC法を適用した結果を出力します。(各ABCの速度)
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 1.0
	 */
	public void vOutputVelocityData()
	{
		int i,j;
		// 現時点での蜂の速度を出力します。
		for( i = 0; i < iAbcDataNum; i++ )
		{
			for( j = 0;j < iAbcVectorDimNum; j++ )
			{
				System.out.print( pplfVelocityData[i][j] + "," );
			}
			System.out.println("");
		}
	}

	/**
	 * <PRE>
	 * 　ABC法を適用した結果を出力します。(各蜂の目的関数値)
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/6/6
	 * @version 1.0
	 */
	public void vOutputConstraintFunction()
	{
		int i;
		// 現時点での各蜂の目的関数の値を出力します。
		for( i = 0; i < iAbcDataNum; i++ )
		{
			System.out.print( pflfObjectiveFunction.lfObjectiveFunction( pplfAbcData[i] ) + "," );
		}
		System.out.print("\n");
	}

	/**
	 * <PRE>
	 * 　現時点でのもっともよい粒子の位置を出力します。
	 * </PRE>
	 * @param iFlag 出力モード
	 * @author kobayashi
	 * @since 2015/6/19
	 * @version 1.0
	 */
	public void vOutputGlobalMaxAbcData( int iFlag )
	{
		int i;
		// 現時点での各粒子の目的関数の値を出力します。
		if (iFlag == 0)
		{
			for (i = 0; i < iAbcVectorDimNum; i++)
			{
				System.out.print( plfGlobalMaxAbcData[i] + "," );
			}
			System.out.println("");
		}
		else
		{
			for (i = 0; i < iAbcVectorDimNum; i++)
			{
				System.out.print( plfGlobalMaxAbcData[i] + "," );
			}
			System.out.print( lfGlobalMaxAbcData );
		}
	}

	/**
	 * <PRE>
	 * 　現時点でのもっともよい粒子の位置を出力します。
	 * </PRE>
	 * @param iFlag 出力モード
	 * @author kobayashi
	 * @since 2015/6/19
	 * @version 1.0
	 */
	public void vOutputGlobalMinAbcData( int iFlag )
	{
		int i;
		// 現時点での各粒子の値を出力します。
		if (iFlag == 0)
		{
			for (i = 0; i < iAbcVectorDimNum; i++)
			{
				System.out.print( plfGlobalMinAbcData[i] + "," );
			}
			System.out.println("");
		}
		else
		{
			for (i = 0; i < iAbcVectorDimNum; i++)
			{
				System.out.print( plfGlobalMinAbcData[i] + "," );
			}
			System.out.println(lfGlobalMinAbcData);
		}
	}

	/**
	 * <PRE>
	 * 　現時点でのもっともよい粒子の目的関数値を出力します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/6/19
	 * @version 1.0
	 */
	public void vOutputGlobalMaxAbcDataConstFuncValue()
	{
		// 現時点での各粒子の目的関数の値を出力します。
		System.out.println( pflfObjectiveFunction.lfObjectiveFunction( plfGlobalMaxAbcData ) + "," );
	}

	/**
	 * <PRE>
	 * 　現時点でのもっともよい粒子の目的関数値を出力します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/6/19
	 * @version 1.0
	 */
	public void vOutputGlobalMinAbcDataConstFuncValue()
	{
		// TODO 自動生成されたメソッド・スタブ
		System.out.println( pflfObjectiveFunction.lfObjectiveFunction( plfGlobalMinAbcData ) + "," );
	}
	/**
	 * <PRE>
	 * 　現時点でのもっともよい粒子位置とその他の粒子との距離を出力します。
	 * </PRE>
	 * @param iOutFlag 0 各粒子と他の粒子の距離のみ出力。
	 * 　　　　　　　　1 平均距離を出力。
	 * @author kobayashi
	 * @since 2015/6/19
	 * @version 0.1
	 */
	public void vOutputAbcDataLocDist( int iOutFlag )
	{
		int i,j;
		double lfRes = 0.0;
		double lfAvgDist = 0.0;
		double lfDist = 0.0;
		double lfDist2 = 0.0;
		// 現時点での各粒子と他の粒子との距離を出力します。
		for( i= 0;i < iAbcDataNum; i++ )
		{
			lfRes = 0.0;
			for( j = 0;j < iAbcVectorDimNum; j++ )
			{
				lfDist = plfGlobalMaxAbcData[j]-pplfAbcData[i][j];
				lfDist2 = lfDist*lfDist;
				lfRes += lfDist2;
			}
			lfAvgDist += lfRes;
			System.out.print( lfRes + "," );
		}
		lfAvgDist /= (double)iAbcDataNum;
		if( iOutFlag == 1 )
		{
			// 現時点粒子間の平均距離を出力します。
			System.out.print( lfAvgDist + "," );
		}
	}

	/**
	* <PRE>
	* 　現時点での各粒子ごとの最良位置を出力します。
	* </PRE>
	* @param iFlag 0 目的関数値を出力しない。
	* 　　　　　　 1 目的関数値を出力する。
	* @author kobayashi
	* @since 2015/7/6
	* @version 0.2
	*/
	public void vOutputLocalMinAbcData(int iFlag)
	{
		// TODO 自動生成されたメソッド・スタブ
		int i, j;
		double lfRes = 0.0;
		double lfAvgDist = 0.0;
		double lfDist = 0.0;
		double lfDist2 = 0.0;
		// 現時点での各粒子ごとの最良位置を出力します。
		for (i = 0; i < iAbcDataNum; i++)
		{
			for (j = 0; j < iAbcVectorDimNum; j++)
			{
				System.out.println(pplfLocalMinAbcData[i][j] + ",");
			}
			if (iFlag == 1)
			{
				// 現時点での各粒子の目的関数の値を出力します。
//				printf("%12.12f,", plfLocalMinObjectiveAbcData[i]);
			}
		}
		System.out.println("");

	}

	/**
	 * <PRE>
	 * 　現時点での各粒子ごとの最良位置を出力します。
	 * </PRE>
	 * @param iOutFlag	0 目的関数値を出力しない。
	 * 					1 目的関数値を出力する。
	 * @author kobayashi
	 * @since 2015/7/6
	 * @version 0.2
	 */
	public void vOutputLocalMaxAbcData( int iOutFlag )
	{
		int i,j;
		double lfRes = 0.0;
		double lfAvgDist = 0.0;
		double lfDist = 0.0;
		double lfDist2 = 0.0;
		// 現時点での各粒子ごとの最良位置を出力します。
		for( i= 0;i < iAbcDataNum; i++ )
		{
			for( j = 0; j < iAbcVectorDimNum; j++ )
			{
				System.out.print( pplfLocalMaxAbcData[i][j] + "," );
			}
			if( iOutFlag == 1 )
			{
				// 現時点での各粒子の目的関数の値を出力します。
				System.out.print( plfLocalMaxObjectiveAbcData[i] + "," );
			}
		}
		System.out.println("");
	}

	public void vSetRange( double lfRange )
	{
		lfSolveRange = lfRange;
	}

	/**
	 * <PRE>
	 *   探索点格納データに指定した探索点、位置に値を設定します。
	 * </PRE>
	 * @param iAbcDataLoc		指定した探索点番号
	 * @param iAbcVectorLoc		指定したベクトル番号
	 * @param lfData			設定する値
	 */
	public void vSetAbcData( int iAbcDataLoc, int iAbcVectorLoc, double lfData )
	{
		pplfAbcData[iAbcDataLoc][iAbcVectorLoc] = lfData;
	}

	/**
	 * <PRE>
	 *   探索点格納データから指定した探索点、位置の値を取得します。
	 * </PRE>
	 * @param iAbcDataLoc		指定した探索点番号
	 * @param iAbcVectorLoc		指定したベクトル番号
	 * @return	指定した番号、位置の値
	 */
	public double lfGetBeeData( int iAbcDataLoc, int iAbcVectorLoc )
	{
		return pplfAbcData[iAbcDataLoc][iAbcVectorLoc];
	}

	/**
	 * <PRE>
	 *    HookerJeeves法を適用します。
	 * </PRE>
	 * @param plfStepSize	各ステップサイズ
	 * @param plfX1			パラメータ１
	 * @param plfX2			パラメータ２
	 * @param plfX0			パラメータ３
	 */
	private void vModifiedHookeJeevesMethod( double[] plfStepSize, double[] plfX1, double[] plfX2, double[] plfX0 )
	{
		int i, j, k;
		int iCounter = 1000;
		double lfRes = 0.0;
		double lfStepSize = 1.0;
		double lfObjFunc = 0.0;
		double lfObjFunc1 = 0.0;
		double lfObjFunc2 = 0.0;
		double lfFuncMin = Double.MAX_VALUE;
		double rho = 0.5;
		boolean bRet;

		// Hooke-Jeeves法を適用します。
		for (k = 0; k < iHJCounter; k++)
		{
			// 各ベクトルのステップサイズの計算をします。
			bRet = bHJEmStep(plfX1, plfX0, plfStepSize);
			if (bRet == false)
			{
				lfObjFunc1 = pflfObjectiveFunction.lfObjectiveFunction(plfX0);
				lfObjFunc2 = pflfObjectiveFunction.lfObjectiveFunction(plfX1);
				if (lfObjFunc2 < lfObjFunc1)
				{
					for (;;)
					{
						// Pattern Move(PM step)
						for (i = 0; i < iAbcVectorDimNum; i++)
						{
							if (plfX1[i] < plfX0[i]) plfStepSize[i] = -Math.abs(plfStepSize[i]);
							else                     plfStepSize[i] = Math.abs(plfStepSize[i]);
						}
						for (i = 0; i < iAbcVectorDimNum; i++)
						{
							plfX2[i] = plfX1[i] + (plfX1[i] - plfX0[i]);
							plfX0[i] = plfX1[i];
						}
						// EM(Expolration Move) Phase
						bRet = bHJEmStep(plfX1, plfX2, plfStepSize);
						lfObjFunc1 = pflfObjectiveFunction.lfObjectiveFunction(plfX0);
						lfObjFunc2 = pflfObjectiveFunction.lfObjectiveFunction(plfX1);
						if (lfObjFunc2 >= lfObjFunc1) break;
					}
				}
			}
			else
			{
				for (;;)
				{
					// Pattern Move(PM step)
					for (i = 0; i < iAbcVectorDimNum; i++)
					{
						if (plfX1[i] < plfX0[i]) plfStepSize[i] = -Math.abs(plfStepSize[i]);
						else                     plfStepSize[i] = Math.abs(plfStepSize[i]);
					}
					for (i = 0; i < iAbcVectorDimNum; i++)
					{
						plfX2[i] = plfX1[i] + (plfX1[i] - plfX0[i]);
						plfX0[i] = plfX1[i];
					}
					// EM(Expolration Move) Phase
					bRet = bHJEmStep(plfX1, plfX2, plfStepSize);
					lfObjFunc1 = pflfObjectiveFunction.lfObjectiveFunction(plfX0);
					lfObjFunc2 = pflfObjectiveFunction.lfObjectiveFunction(plfX1);
					if (lfObjFunc2 >= lfObjFunc1) break;
				}
			}

			if (lfStepSize < 0.001) break;
			else
			{
				lfStepSize = rho*lfStepSize;
				for (j = 0; j < iAbcVectorDimNum; j++)
					plfStepSize[j] = plfStepSize[j] * rho;
			}
		}
	}

	/**
	 * <PRE>
	 *    HookerJeeves法のEmStepを算出します。
	 * </PRE>
	 * @param plfX1			パラメータ１
	 * @param plfX0			パラメータ３
	 * @param plfStepSize	各ステップサイズ
	 * @return				true ステップサイズ更新できた。
	 * 						false ステップサイズ更新できなかった。
	 */
	private boolean bHJEmStep( double[] plfX1, double[] plfX0, double[] plfStepSize )
	{
		double lfObjFunc = 0.0;
		double lfFuncMin = Double.MAX_VALUE;
		double lfXi;
		int i;

		for( i = 0; i < plfX1.length; i++ ) plfX1[i] = plfX0[i];
		for (i = 0; i < iAbcVectorDimNum; i++)
		{
			lfXi = plfX0[i]+plfStepSize[i];
			plfX1[i] = lfXi;
			lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(plfX1);
			if (lfObjFunc < lfFuncMin)	lfFuncMin = lfObjFunc;
			else
			{
				lfXi = plfX0[i] - plfStepSize[i];
				plfX1[i] = lfXi;
				lfObjFunc = pflfObjectiveFunction.lfObjectiveFunction(plfX1);
				if (lfObjFunc < lfFuncMin)	lfFuncMin = lfObjFunc;
				else			        lfXi = plfX0[i];
			}
			plfX1[i] = lfXi;
		}

		if (lfObjFunc >= lfFuncMin) return false;
		return true;
	}

	/**
	 * <PRE>
	 *    適応度ソート用のクイックソート
	 * </PRE>
	 * @param start		開始位置
	 * @param end		終了位置
	 * @param sort		ソートデータ
	 */
	private void qsort( int start, int end, double[] sort )
	{
		int i,j;
		int pibo = 0;
		double pibovalue = 0;
		double temp;
		i = start;
		j = end;
		pibo = (i + j) / 2;
		pibovalue = sort[pibo];

		while(true)
		{
			while( sort[i] > pibovalue ) i++;
			while( pibovalue > sort[j] ) j--;
			if( i >= j ) break;
			temp = sort[i];
			sort[i] = sort[j];
			sort[j] = temp;
			i++;
			j--;
		}
		if(start < i - 1) qsort(start,i - 1,sort);
		if(j + 1 < end)	qsort(j + 1,end,sort);
	}
}
