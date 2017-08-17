package utility.node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import jp.ac.nihon_u.cit.su.furulab.fuse.Constants;
import jp.ac.nihon_u.cit.su.furulab.fuse.SortNodes;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

public class ERTriageNodeManager
{

	// メンバー変数
	//private List<ERTriageNode> nodes=new LinkedList<ERTriageNode>();
	private List<ERTriageNode> nodes=new ArrayList<ERTriageNode>();

	// 最後に計算した距離
	private double prevDistance = 0;

	/**
	 * <PRE>
	 * コンストラクタ
	 * </PRE>
	 */
	public ERTriageNodeManager()
	{
	}

	/**
	 * <PRE>
	 * ファイル名つきコンストラクタ
	 * </PRE>
	 * @param filename ノードリンク情報が記述されたファイル名
	 */
	public ERTriageNodeManager( String filename )
	{
		File file = new File(filename);
		this.nodes = this.loadFile(file);
	}

	/**
	 * <PRE>
	 *    生成と同時にノードを設定するコンストラクタ
	 * </PRE>
	 * @param nodes 登録ノードリスト
	 */
	public ERTriageNodeManager(Collection<ERTriageNode> nodes)
	{
		//this.nodes=new LinkedList<ERTriageNode>(nodes);
		this.nodes = new ArrayList<ERTriageNode>(nodes);
	}

	/**
	 * <PRE>
	 *    ファイルからノードデータを読み込むメソッドです
	 * </PRE>
	 * @param file ファイルクラスのインスタンス
	 * @return 読み込んだノードリスト
	 */
	public List<ERTriageNode> loadFile( File file )
	{

		LinkedList<ERTriageNode> tempNodes=new LinkedList<ERTriageNode>();

		try
		{
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);

			String line;

			// 一行目を切って捨てる(Node)
			br.readLine();

			int index = Integer.parseInt( br.readLine() );

			// Node切り出し
			for( int i=0; i<index; i++ )
			{
				if( (line = br.readLine()) != null )
				{
					// 先頭が#だったらコメント
					if( line.startsWith("#") )
					{
						line = br.readLine();
					}

					// 半角スペースをデリミタとして切り出し
					String cols[] = line.split(" ");

					// Double型にキャストしてNodeListを作成
					tempNodes.add( new ERTriageNode( i, Double.parseDouble(cols[0]), Double.parseDouble(cols[1]), Double.parseDouble(cols[2]), Integer.parseInt(cols[3]), Integer.parseInt(cols[4])  ) );
				}
			}

			// 一行目を切って捨てる(Link)
			br.readLine();
			index = Integer.parseInt( br.readLine() );

			// Link切り出し
			for( int i=0; i<index; i++ )
			{
				if( (line = br.readLine()) != null )
				{

					// 先頭が#だったらコメント
					if( line.startsWith("#") )
					{
						line = br.readLine();
					}

					// 半角スペースをデリミタとして切り出し
					String cols[] = line.split(" ");

					// Linkの追加
					tempNodes.get( Integer.parseInt(cols[0]) ).addLink( new ERTriageLink( tempNodes.get( Integer.parseInt(cols[0]) ), tempNodes.get( Integer.parseInt(cols[1]) ), Double.parseDouble(cols[2]) ) );
				}
			}

			// ストリームをクローズする
			br.close();
			isr.close();
			fis.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return tempNodes;
	}

	/**
	 * <PRE>
	 *    Node全体を返すメソッド
	 *    返り値のリスト自体はコピーなのでエレメントの削除や追加をしても問題ないですが、参照しているエレメントは原本なので
	 *    それを操作すると値が変わってしまいます。
	 * </PRE>
	 * @return ノードマネージャクラスに登録された全ノード
	 */
	public List<ERTriageNode> getAll()
	{
		return new ArrayList<ERTriageNode>(this.nodes);
	}

	/**
	 * <PRE>
	 *    Node全体のフルコピーを返すメソッド
	 * </PRE>
	 * @return ノードのコピー
	 */
	public ArrayList<ERTriageNode> getAllClone()
	{
		ArrayList<ERTriageNode> copiedList=new ArrayList<ERTriageNode>(nodes.size());
		Map<Long,ERTriageNode> idMap=new HashMap<Long, ERTriageNode>(nodes.size());

		// ノードだけのコピーを生成する
		for (ERTriageNode node:this.nodes)
		{
			ERTriageNode copy=node.simpleClone();
			copiedList.add(copy);

			// マップを作っておく
			idMap.put(copy.getId(), copy);
		}

		System.out.println("DEBUG: num of Nodes:"+this.nodes.size());

		// リンクを追加していく
		int linksDebug=0;
		int nodeCounter=0;
		for (ERTriageNode node:this.nodes)
		{
			ERTriageNode copy=copiedList.get(nodeCounter);
			ERTriageNode origine=node; // コピー元

			for (ERTriageLink link:origine.getLinks())
			{
				ERTriageNode desti=idMap.get(link.getDestination().getId());
				ERTriageLink newLink=new ERTriageLink(copy, desti, link.getDistance());
				copy.addLink(newLink);
				linksDebug++;
			}
			nodeCounter++;
		}

		System.out.println("DEBUG: num of links:"+linksDebug);

		return copiedList;
	}

	/**
	 * <PRE>
	 *    Node全体への参照を返すメソッド
	 * </PRE>
	 * @return ノード全体の参照（アドレスのみ）
	 */
	public List<ERTriageNode> getAllReference()
	{
		return this.nodes;
	}

	/**
	 * <PRE>
	 *    指定されたIDのノードを返すメソッド
	 * </PRE>
	 * @param id 指定ノードID
	 * @return IDで指定されたノード
	 */
	public ERTriageNode getNodeById(long id)
	{
		ERTriageNode result=null;
		result=this.getNodeById(id,nodes);
		return result;
	}

	/**
	 * <PRE>
	 *   指定されたIDのノードを返すメソッド
	 * </PRE>
	 * @param id			ノードID
	 * @param targetNodes	指定ノード群
	 * @return				指定ノード群の該当IDのノード
	 */
	private ERTriageNode getNodeById(long id, List<ERTriageNode> targetNodes){
		ERTriageNode result=null;

		for (ERTriageNode node:targetNodes)
		{
			if (node.getId()==id)
			{
				result=node;
				break;
			}
		}
		return result;
	}

	/**
	 * <PRE>
	 *    指定されたXY線分を+-Z方向に無限に伸ばした面によって切断される
	 *    リンクへの参照を返すメソッドです
	 * </PRE>
	 * @param pos1	始点(x,y)
	 * @param pos2	終点(x,y)
	 * @return		始点終点間のリンクすべて
	 */
	public List<ERTriageLink> getLinksCutByPlane(Position pos1, Position pos2)
	{
		ArrayList<ERTriageLink> cutLinks=new ArrayList<ERTriageLink>();
		double[] vec=new double[2];  // 切断面ベクトル
		double[] vec0=new double[2];  // 汎用の自分ベクトル
		double[] vecA=new double[2];// 汎用の相手ベクトル1
		double[] vecB=new double[2];// 汎用の相手ベクトル2

		vec[0]=pos2.getX()-pos1.getX();
		vec[1]=pos2.getY()-pos1.getY();

		for (ERTriageNode node:nodes)
		{
			for (ERTriageLink link:node.getLinks())
			{
				// 切断面から見たリンクとの関係
				Position posA=link.getStart().getPosition();
				vecA[0]=posA.getX()-pos1.getX();
				vecA[1]=posA.getY()-pos1.getY();

				Position posB=link.getDestination().getPosition();
				vecB[0]=posB.getX()-pos1.getX();
				vecB[1]=posB.getY()-pos1.getY();

				double check1=vec[0]*vecA[1]-vec[1]*vecA[0]; //Z軸方向要素
				double check2=vec[0]*vecB[1]-vec[1]*vecB[0]; //Z軸方向要素

				if (check1*check2<=0)
				{
					// リンクから見た切断面との関係
					vec0[0]=posB.getX()-posA.getX();
					vec0[1]=posB.getY()-posA.getY();

					vecA[0]=-vecA[0];
					vecA[1]=-vecA[1];

					vecB[0]=pos2.getX()-posA.getX();
					vecB[1]=pos2.getY()-posA.getY();

					check1=vec0[0]*vecA[1]-vec0[1]*vecA[0]; //Z軸方向要素
					check2=vec0[0]*vecB[1]-vec0[1]*vecB[0]; //Z軸方向要素
					if (check1*check2<=0)
					{
						cutLinks.add(link);
					}
				}
			}
		}

		return cutLinks;
	}

	/**
	 * <PRE>
	 *   指定された範囲に含まれるノードへの参照を返すメソッドです
	 *   中心となる点から指定された範囲の園内のノードをピックアップ
	 *   なお，境界線上は範囲内とみなされません
	 * </PRE>
	 * @param pos		指定した位置
	 * @param radius	指定した位置を中心とした半径
	 * @return	範囲内にあるノードすべて
	 */
	public List<ERTriageNode> getNodesInRange(Position pos, double radius)
	{
		ArrayList<ERTriageNode> rangedNodes=new ArrayList<ERTriageNode>(100);

		// 範囲に含まれるかどうかの確認
		for ( ERTriageNode node:this.nodes )
		{
			if (node.getPosition().getDistance(pos)<radius)
			{
				rangedNodes.add(node);
			}
		}

		return rangedNodes;
	}

    /** 指定された範囲に含まれるノードへの参照を返すメソッドです<br>
     * 二つの座標を対角とする直方体に含まれるノードをピックアップ<br>
    なお，境界線上は範囲内とみなされません*/
    public List<ERTriageNode> getNodesInRange(Position pos1, Position pos2){
        ArrayList<ERTriageNode> rangedNodes=new ArrayList<ERTriageNode>(100);

        // バウンダリボックスに含まれるかどうかの確認
        for ( ERTriageNode node:this.nodes ){
            if (this.isInbound(node, pos1, pos2)){
                rangedNodes.add(node);
            }
        }

        return rangedNodes;
    }

    /** ノードがバウンダリボックスに含まれるかどうかの確認メソッドです <br>
     ** 二つの座標を対角とする直方体にノードが含まれるならtrueを返します */
    public boolean isInbound(ERTriageNode node,Position pos1, Position pos2){
        boolean result=false;
        double minX,minY,minZ;
        double maxX,maxY,maxZ;

        if (pos1.getX()<pos2.getX()){
            minX=pos1.getX();
            maxX=pos2.getX();
        }else{
            minX=pos2.getX();
            maxX=pos1.getX();
        }

        if (pos1.getY()<pos2.getY()){
            minY=pos1.getY();
            maxY=pos2.getY();
        }else{
            minY=pos2.getY();
            maxY=pos1.getY();
        }

        if (pos1.getZ()<pos2.getZ()){
            minZ=pos1.getZ();
            maxZ=pos2.getZ();
        }else{
            minZ=pos2.getZ();
            maxZ=pos1.getZ();
        }

        double[] nodePos=node.getPosByArray();

        // 指定したバウンダリボックスに含まれるかどうか
        if (minX<nodePos[0] && nodePos[0]<maxX &&
            minY<nodePos[1] && nodePos[1]<maxY &&
            minZ<nodePos[2] && nodePos[2]<maxZ){
            result=true;
        }
        return result;
    }

    /** ノードのソート */
    public void sortNodesById(ArrayList<ERTriageNode> targetNodes){
        Collections.sort(targetNodes,new ERTriageSortNodes());
    }

    /** 最も自分に近いNodeを返すメソッド<br>
        * 引数：double x, y, z<br>
        * 戻り値：一番近いNode  */
    public ERTriageNode getNearestNode( double[] elements ) {
        ERTriageNode result=null;
        double distance=Constants.HUGE; // とにかく巨大な数

        // ノードの中で指定された地点の最近傍を求める
        for(ERTriageNode node:nodes){
            double tempDist=this.getDistance(elements, node.getPosByArray());
            if (tempDist<distance){
                result=node;
                distance=tempDist;
            }
        }
        return result;
    }

    /** 最も自分に近いNodeを返すメソッド<br>
     * 引数：Position<br>
     * 戻り値：一番近いNode  */
    public ERTriageNode getNearestNode( Position pos) {
        return this.getNearestNode(pos.get());
    }

    /** 現在管理しているノード数を取得 */
    public int getNumOfNodes(){
        return this.nodes.size();
    }

    /** ノードリストをセットするメソッド */
    public void setNodeList(List<ERTriageNode> list){
        nodes=list;
    }

    /** Nodeの追加用メソッド <br>
     * 引数：ERTriageNode point */
    public void addNode( ERTriageNode point ) {
        this.nodes.add( point );
    }

    /** Nodeをまとめて追加するメソッド */
    public void addNodes(Collection<ERTriageNode> nodes){
        this.nodes.addAll(nodes);
    }

    /** Nodeを除去するメソッドです<br>
     * そのノードに対するリンクも切断されます */
    public void removeNode(ERTriageNode node){
        if (this.nodes.contains(node)){
            for(ERTriageLink link:node.getLinks()){
                ERTriageNode des=link.getDestination();
                ERTriageLink linkToMe=des.getLink(node);
                if (linkToMe!=null){
                    des.removeLink(linkToMe);
                }
            }
            node.removeAllLinks(); // リンク全削除
            this.nodes.remove(node);
        }
        return;
    }

    /** 既存ノードを削除し、ノードをまとめて設定するメソッド<br>
     * 2次元配列の状態で追加することができます */
    public void setNodes(ERTriageNode[][] nodes){
        this.nodes.clear();
        for (ERTriageNode[] nodeArray:nodes){
            for (ERTriageNode thisNode:nodeArray){
                this.nodes.add(thisNode);
            }
        }
    }

    /** 最後に検索したルートの距離を返すメソッド */
    public double getLastDistance(){
        return prevDistance;
    }

    /** ルートを探索するメソッドです <br>
     * 要素(0)にスタートノードが，要素(n-1)にゴールノードが入ります．<br>
     * 引数：ERTriageNode s, e <br>
     * 戻り値：Nodeのリスト
     */
    public ArrayList<ERTriageNode> getRoute( ERTriageNode s, ERTriageNode g ) {
        ArrayList<ERTriageNode> path = astar(s, g);
        return path;
    }

    /** ノードをダンプするデバッグ用メソッド */
    public void dumpNodes(){
        System.out.println("### DUMP NODES INFO ###");
        for (ERTriageNode node:this.nodes){
            System.out.println("NODE: ID:"+node.getId()+" "+node.getPosition());
            for (ERTriageLink link:node.getLinks()){
                System.out.println("        LINK: ID:"+link.getStart().getId()+" - ID:"+link.getDestination().getId()+" Distance:"+link.getDistance());
            }
        }
    }

    /***** A*アルゴリズム関連 *****/

    /** A-starアルゴリズムのメソッド */
    private ArrayList<ERTriageNode> astar( ERTriageNode s, ERTriageNode g ) {

        LinkedList<ERTriageCameFrom> closeList = new LinkedList<ERTriageCameFrom>(); // 探索済みリスト(ひょっとするとMapだけでいいかも)
        Map<Long,ERTriageCameFrom> closeMap = new HashMap<Long,ERTriageCameFrom>(); // 探索済みリストの検索用インデックス
        LinkedList<ERTriageCameFrom> openList = new LinkedList<ERTriageCameFrom>(); // 探索候補リスト
        Map<Long,ERTriageCameFrom> openMap = new HashMap<Long,ERTriageCameFrom>(); // 探索候補リストの検索用インデックス
        ArrayList<ERTriageNode> result = null; // 探索結果
        ERTriageCameFrom currentCameFrom = null; // 現在探索しているCameFrom
        LinkedList<ERTriageLink> nextLinks = null; // 次の探索候補へのリンク

        // 初期位置の追加
        double dist = 0.0;
        double totalCost = dist + getHeuristic(s, g);
        currentCameFrom = new ERTriageCameFrom(s, null, dist, totalCost);
        openList.add(currentCameFrom);

        // メインループ
        int counter=0;
        while( !openList.isEmpty() ) {

            // openList内の最小コストのNodeを現在のNodeにする。
            currentCameFrom = openList.getFirst(); // ソートしているので最初のノードが常に最小コスト
            ERTriageNode currentNode=currentCameFrom.getCurrentNode();

            //openList.remove(currentCameFrom);
            this.removeFromList(currentCameFrom, openList, openMap);
            //closeList.add(currentCameFrom);
            this.addToCloseList(currentCameFrom, closeList, closeMap);

            // ゴールに着いていた場合は終了
            if( currentNode.getId() == g.getId() ) {
                result = reconstructPath( closeList, closeMap, g );
                break;
            }

            // 現在のノードの移動先候補点を探索
            nextLinks = currentNode.getLinks();
            for( ERTriageLink candidateLink : nextLinks ) {

                ERTriageNode target=candidateLink.getDestination();

                dist = currentCameFrom.getDist() + candidateLink.getDistance();
                totalCost = dist + getHeuristic(target, g);
                ERTriageCameFrom candidateKeiro=new ERTriageCameFrom(target, currentNode, dist, totalCost);

               // 行き先が探索済みの場合、そのノードと現在調査中のノードの距離を比較
                ERTriageCameFrom already=getERTriageCameFrom(target, closeList,closeMap);
                if (already !=null){
                    // 探索済みのケースより短い経路発見
                    if (totalCost<already.getTotalCost()){
                        // クローズリストの要素を置き換えて探索候補に追加
                        //boolean check=closeList.remove(already);
                        boolean check=this.removeFromList(already, closeList, closeMap);
                        if (!check){
                            System.err.println("ERROR: Close list don't have the element !");
                        }
                        addToOpenList(candidateKeiro,openList,openMap);
                    }
                    continue; // この後は必要なし
                }

               // 移動先候補点が既に探索候補にあるか調査
               ERTriageCameFrom isExist=getERTriageCameFrom(target, openList,openMap);
               if( isExist==null ) { // まだ探索候補になっていない
                   // 探索候補リストに追加
                   addToOpenList( candidateKeiro, openList,openMap);
               }else{ // もう探索候補になっている
                   // 探索候補に入っている経路より短い経路発見
                   if (totalCost<isExist.getTotalCost()){
                       // 探索候補ノードの経路置き換え
                       //boolean check=openList.remove(isExist);
                       boolean check=this.removeFromList(isExist, openList, openMap);
                       if (!check){
                           System.err.println("ERROR: Open list don't have the element !");
                       }
                       addToOpenList( candidateKeiro, openList,openMap);
                   }
               }
               counter++;
               //System.out.println("DEBUG: dist:"+dist + "  cost:" + totalCost+" cycle:"+counter+" sizeOfOpenList:"+openList.size()+" sizeOfCloseList:"+closeList.size());
           }
       }
        return result;
    }

    /**
     * <PRE>
     * 	ヒューリスティック計算
     * 	引数：ERTriageNode point, goal
     * 	戻り値：Goalまでの直線距離
     * </PRE>
     *
     * @param current		現在のノード
     * @param goal			目標のノード
     * @return				現在位置から目標位置までの距離
     */
    private double getHeuristic( ERTriageNode current, ERTriageNode goal ) {
        double distance=0;
        double[] posC=current.getPosByArray();
        double[] posG=goal.getPosByArray();

        distance=this.getDistance(posC,posG);

        return distance;
    }

    /**
     * <PRE>
     * 	2点間の距離を計算します
     * </PRE>
     *
     * @param pos1		位置1
     * @param pos2		位置2
     * @return	2点間の距離
     */
    private double getDistance(double[] pos1, double[] pos2){
        double distance=0;
        for(int i=0;i<pos1.length;i++){
            distance=distance+(pos1[i]-pos2[i])*(pos1[i]-pos2[i]);
        }
        distance=Math.sqrt(distance);
        return distance;
    }

    /**
     * <PRE>
     * 	経路を作成するメソッド
     * </PRE>
     *
     * @param cameList	経路リストデータ
     * @param cameMap	経路マップデータ
     * @param g			ゴールノード
     * @return			目標までの経路データ
     */
    private ArrayList<ERTriageNode> reconstructPath( List<ERTriageCameFrom> cameList, Map<Long,ERTriageCameFrom> cameMap, ERTriageNode g ) {
        ArrayList<ERTriageNode> path = new ArrayList<ERTriageNode>();
        ERTriageCameFrom current = null;

        // ゴールを設定
        path.add( g );
        current = getERTriageCameFrom(g, cameList,cameMap);

        // スタートノードになるまでパスに追加
        while( current.getPrevNode() != null )
        {
            // 次のERTriageCameFromをリスト中から見つける
            current = getERTriageCameFrom(current.getPrevNode(), cameList,cameMap);
            cameList.remove(current);
            path.add( 0, current.getCurrentNode() );
        }

        return path;
    }

    /** リストへ昇順で追加 */
    private void addToOpenList(ERTriageCameFrom keiro, LinkedList<ERTriageCameFrom> openList, Map<Long,ERTriageCameFrom> openMap)
    {
        boolean inserted=false;
        for (ListIterator<ERTriageCameFrom> i = openList.listIterator(); i.hasNext();)
        {
            ERTriageCameFrom target=(ERTriageCameFrom)i.next();
            // ターゲットよりもコストが小さいならその前に挿入
            if (keiro.getTotalCost()<target.getTotalCost())
            {
                openList.add(i.nextIndex()-1,keiro);
                inserted=true;
                break;
            }
        }

        // 自分よりコストが大きい要素が無いなら末尾に追加
        if (!inserted){
            openList.addLast(keiro);
        }

        // 検索マップに追加
        openMap.put(keiro.getCurrentNode().getId(), keiro);
    }

    /** リストへ追加(特にソートは行いません) */
    private void addToCloseList(ERTriageCameFrom keiro, LinkedList<ERTriageCameFrom> closeList, Map<Long,ERTriageCameFrom> closeMap)
    {
        closeList.add(keiro);
        closeMap.put(keiro.getCurrentNode().getId(), keiro);
    }

    /** リストから削除<br>
     * 検索用マップからも確実に削除しなければ誤動作します */
    private boolean removeFromList(ERTriageCameFrom target, List<ERTriageCameFrom> cameList,Map<Long,ERTriageCameFrom> cameMap )
    {
        boolean result=cameList.remove(target);
        cameMap.remove(target.getCurrentNode().getId());
        return result;
    }

    /** ERTriageCameFromのリスト中から現在のNodeのERTriageCameFromを取得 */
    private ERTriageCameFrom getERTriageCameFrom( ERTriageNode current, List<ERTriageCameFrom> cameList,Map<Long,ERTriageCameFrom> cameMap )
    {
        return cameMap.get(current.getId());
    }
    /*****    A*アルゴリズム関連    *****/

}
