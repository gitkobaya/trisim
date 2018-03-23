package inverse.optimization.rankt;

/**
 *    遺伝的アルゴリズム及び人工蜂コロニーアルゴリズムで使用する
 *    データソート用クラス。
 *    これを使ってデータソートを行います。
 *
 * @author kobayashi
 *
 */
public class Rank_t
{
	public int iLoc;			// 配列番号
	public double lfFitProb;	// 適応度

	/**
	 * <PRE>
	 *   デフォルトコンストラクタ（何もしません。）
	 * </PRE>
	 */
	public Rank_t()
	{

	}

	/**
	 * <PRE>
	 *    コンストラクタ（配列番号、適応度で初期化）
	 * </PRE>
	 * @param iData	 配列番号
	 * @param lfData 適応度
	 */
	public Rank_t( int iData, double lfData )
	{
		iLoc = iData;
		lfFitProb = lfData;
	}
}
