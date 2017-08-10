package utility.node;

public class ERTriageCameFrom
{
	// メンバー変数
	private ERTriageNode currnt, prev;
	private double distance;
	private double totalScore;

	/**
	 *
	 * <PRE>
	 * コンストラクタです。
	 * 引数は現在のノード、一つ前のノード、スタート位置からの距離、トータルスコアです（スタートからの距離＋ヒューリスティック）。
	 * </PRE>
	 *
	 * @param current		現在のノード
	 * @param prev			ひとつ前のノード
	 * @param distance		スタート位置からの距離
	 * @param totalScore	トータルスコアです（スタートからの距離＋ヒューリスティック）
	 */
	public ERTriageCameFrom(ERTriageNode current, ERTriageNode prev, double distance, double totalScore)
	{
		this.currnt = current;
		this.prev = prev;
		this.distance = distance;
		this.totalScore = totalScore;
	}

	/**
	 * <PRE>
	 * 	  現在のノードを返すメソッド
	 * </PRE>
	 *
	 * @return 現在のノード
	 */
	public ERTriageNode getCurrentNode()
	{
		return this.currnt;
	}

	/**
	 * <PRE>
	 * 		一つ前のノードを返すメソッド
	 * </PRE>
	 *
	 * @return ひとつ前のノード
	 */
	public ERTriageNode getPrevNode()
	{
		return prev;
	}

	/**
	 * <PRE>
	 * 	スタート位置からの距離を返すメソッド
	 * </PRE>
	 * @return スタート位置からの距離
	 *
	 */
	public double getDist()
	{
		return this.distance;
	}

	/**
	 * <PRE>
	 *     トータルスコア（スタートからの距離＋ヒューリスティック）を返すメソッド
	 * </PRE>
	 *
	 *  @return トータルスコア（スタートからの距離＋ヒューリスティック）
	 */
	public double getTotalCost()
	{
		return this.totalScore;
	}
}
