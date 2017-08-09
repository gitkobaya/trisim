package utility.node;

public class ERTriageLink
{

    // メンバー変数
    private ERTriageNode start;
    private ERTriageNode end;
    private double distance;
    private double lfTime;


    /** コンストラクタです。<br>
    * 引数はスタートノード、エンドノード、その距離です。*/
    public ERTriageLink(ERTriageNode start, ERTriageNode end, double distance)
    {
       this.start = start;
       this.end = end;
       this.distance = distance;
    }

	/** Linkの距離を返すメソッド<br>
	 * 経路探索に使う場合の距離を表すため，必ずしも物理的な距離とは一致しない場合があります．
	 * 戻り値：double distance*/
	public double getDistance()
	{
		return this.distance;
	}

	/** Linkの距離を設定するメソッド */
	public void setDistance(double dist)
	{
		this.distance=dist;
	}

       /** 接続元のERTriageNodeを返すメソッド
         * 戻り値：ERTriageNode start */
        public ERTriageNode getStart() {
                return this.start;
        }


	/** 接続先のERTriageNodeを返すメソッド
	 * 戻り値：ERTriageNode destination*/
	public ERTriageNode getDestination() {
		return this.end;
	}
}
