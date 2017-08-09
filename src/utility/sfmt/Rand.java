package utility.sfmt;

// メルセンヌツイスタークラスのラッパー関数（排他制御対応済み）
public class Rand extends Sfmt
{
	/**
	 * <PRE>
	 *  整数の種 s による初期化
	 * </PRE>
	 */
	synchronized public void InitMt(int s)
	{
		super.InitMt(s);
	}

	/**
	 * <PRE>
	 *   コンストラクタ
	 * </PRE>
	 * @param s 整数
	 */
	public Rand(int s)
	{
		super(s);
	}

	/**
	 * <PRE>
	 *   配列 init_key による初期化
	 * </PRE>
	 */
	synchronized public void InitMtEx(int[]init_key)
	{
		super.InitMtEx(init_key);
    }

	/**
	 * <PRE>
	 *   コンストラクタ
	 * </PRE>
	 * @param init_key 整数キー
	 */
	Rand(int[] init_key)
	{
		super(init_key);
	}

	/* 32ビット符号あり整数の乱数 */
	synchronized public int NextMt()
	{
		return super.NextMt();
	}

	/* ０以上 n 未満の整数乱数 */
	synchronized public int NextInt(int n)
	{
		return super.NextInt(n);
	}

	/* ０以上１未満の乱数(53bit精度) */
	synchronized public double NextUnif()
	{
		return super.NextUnif();
	}

	/* ０か１を返す乱数 */
	synchronized public int NextBit()
	{
		return super.NextBit();
	}

	/* ０から２５５を返す乱数 */
	synchronized public int NextByte()
	{
		return super.NextByte();
	}

	/* 丸め誤差のない０以上 range_ 未満の整数乱数 */
	synchronized public int NextIntEx(int range_)
	{
		return super.NextIntEx( range );
	}

	/* 自由度νのカイ２乗分布 */
	synchronized public double NextChisq(double n)
	{
		return super.NextChisq( n );
	}

	/* パラメータａのガンマ分布 */
	synchronized public double NextGamma(double a)
	{
		return super.NextGamma(a);
	}

	/* 確率Ｐの幾何分布 */
	synchronized public int NextGeometric(double p)
	{
		return super.NextGeometric(p);
	}

	/* 三角分布 */
	synchronized public double NextTriangle()
	{
		return super.NextTriangle();
	}

	/* 平均１の指数分布 */
	synchronized public double NextExp()
	{
		return super.NextExp();
	}

	/* 標準正規分布(最大8.57σ) */
	synchronized public double NextNormal()
	{
		return super.NextNormal();
	}

	/* Ｎ次元のランダム単位ベクトル */
	synchronized public double[]NextUnitVect(int n)
	{
		return super.NextUnitVect( n );
	}

	/* パラメータＮ,Ｐの２項分布 */
	synchronized public int NextBinomial(int n,double p)
	{
		return super.NextBinomial( n, p );
	}

	/* 相関係数Ｒの２変量正規分布 */
	synchronized public double[]NextBinormal(double r)
	{
		return super.NextBinormal( r );
	}

	/* パラメータＡ,Ｂのベータ分布 */
	synchronized public double NextBeta(double a,double b)
	{
		return super.NextBeta( a, b );
	}

	/* パラメータＮの累乗分布 */
	synchronized public double NextPower(double n)
	{
		return super.NextPower( n );
	}

	/* ロジスティック分布 */
	synchronized public double NextLogistic()
	{
		return super.NextLogistic();
	}

	/* コーシー分布 */
	synchronized public double NextCauchy()
	{
		return super.NextCauchy();
	}

	/* 自由度 n1,n2 のＦ分布 */
	synchronized public double NextFDist(double n1,double n2)
	{
		return super.NextFDist( n1, n2 );
	}

	/* 平均λのポアソン分布 */
	synchronized public int NextPoisson(double lambda)
	{
		return super.NextPoisson( lambda );
	}

	/* 自由度Ｎのｔ分布 */
	synchronized public double NextTDist(double n)
	{
		return super.NextTDist( n );
	}

	/* パラメータαのワイブル分布 */
	synchronized public double NextWeibull(double alpha)
	{
		return super.NextWeibull( alpha );
	}
}
