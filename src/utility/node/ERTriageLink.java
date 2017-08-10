package utility.node;

public class ERTriageLink
{

    // メンバー変数
    private ERTriageNode start;
    private ERTriageNode end;
    private double distance;
    private double lfTime;


    /**
     * <PRE>
     *   コンストラクタです。
     * 引数はスタートノード、エンドノード、その距離です。
     * </PRE>
     * @param start     開始ノード
     * @param end		終了ノード
     * @param distance	開始ノードから終了ノードまでの距離
     */
    public ERTriageLink(ERTriageNode start, ERTriageNode end, double distance)
    {
       this.start = start;
       this.end = end;
       this.distance = distance;
    }

	/**
	 * <PRE>
	 *   Linkの距離を返すメソッド
	 * 経路探索に使う場合の距離を表すため，必ずしも物理的な距離とは一致しない場合があります．
	 * 戻り値：double distance
	 * </PRE>
	 * @return 距離
	 *
	 */
	public double getDistance()
	{
		return this.distance;
	}

	/**
	 * <PRE>
	 *    Linkの距離を設定するメソッド
	 * </PRE>
	 * @param dist 距離
	 */
	public void setDistance(double dist)
	{
		this.distance=dist;
	}

	/**
	 * <PRE>
	 *   接続元のERTriageNodeを返すメソッド
	 *   戻り値：ERTriageNode start
	 * </PRE>
	 * @return 開始ノード
	 */
	public ERTriageNode getStart()
	{
		return this.start;
	}


	/**
	 * <PRE>
	 *   接続先のERTriageNodeを返すメソッド
	 *   戻り値：ERTriageNode destination
	 * </PRE>
	 * @return 終了ノード
	 */
	public ERTriageNode getDestination()
	{
		return this.end;
	}
}
