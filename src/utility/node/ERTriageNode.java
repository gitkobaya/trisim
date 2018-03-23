package utility.node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

public class ERTriageNode implements Savable
{
	private static final long serialVersionUID = 82380599088713200L;

    // メンバー変数
    private long id;
    private double[] elements;
    private Position position;
    private LinkedList<ERTriageLink> links=new LinkedList<ERTriageLink>();
	private int iLocation;
	private int iFloor;

    /** 汎用の属性情報です */
    private Object property=null;

	/**
	 * <PRE>
	 *  TRISim用ノードコンストラクタ
	 * </PRE>
	 * @param elm   x,y,z座標のdouble型配列
	 * @param iLoc  ノードの所属する部屋番号
	 */
	public ERTriageNode(double[] elm, int iLoc )
	{
        this.id=this.hashCode();
        this.elements=elm;
        this.position=new Position(this.elements);
		iLocation = iLoc;
	}

	/**
	 * <PRE>
	 *  TRISim用ノードコンストラクタ
	 * </PRE>
	 * @param lfX		 ノードのX座標
	 * @param lfY		 ノードのY座標
	 * @param lfZ		 ノードのZ座標
	 * @param iLoc		 ノードの所属する部屋番号
	 * @param iFloorData ノードが位置する階
	 */
	public ERTriageNode(double lfX, double lfY, double lfZ, int iLoc, int iFloorData )
	{
		this(lfX,lfY,lfZ);
		iLocation = iLoc;
		iFloor = iFloorData;
	}

	/**
	 * <PRE>
	 *  TRISim用ノードコンストラクタ
	 * </PRE>
	 * @param id		 ノードのID番号
	 * @param lfX		 ノードのX座標
	 * @param lfY		 ノードのY座標
	 * @param lfZ		 ノードのZ座標
	 * @param iLoc		 ノードの所属する部屋番号
	 * @param iFloorData ノードが位置する階
	 */
	public ERTriageNode( int id, double lfX, double lfY, double lfZ, int iLoc, int iFloorData )
	{
		this( id, lfX, lfY, lfZ );
		iLocation = iLoc;
		iFloor = iFloorData;
	}

	/**
	 * <PRE>
	 *  TRISim用ノードコンストラクタ
	 * </PRE>
	 * @param elm   x,y,z座標のdouble型配列
	 */
    public ERTriageNode(double[] elm)
    {
        this.elements=elm;
        this.position=new Position(this.elements);
    }

	/**
	 * <PRE>
	 *  TRISim用ノードコンストラクタ
	 *  x,yを指定。zは0。
	 * </PRE>
     * @param x x座標
     * @param y y座標
	 */
    public ERTriageNode(double x, double y)
    {
        this.id=this.hashCode();
        this.elements=new double[]{x,y,0};
        this.position=new Position(this.elements);
    }

	/**
	 * <PRE>
	 *  TRISim用ノードコンストラクタ
	 *  x,y,zを指定。
	 * </PRE>
     * @param x x座標
     * @param y y座標
     * @param z z座標
	 */
    public ERTriageNode(double x, double y, double z)
    {
        this.id=this.hashCode();
        this.elements=new double[]{x,y,z};
        this.position=new Position(this.elements);
    }

	/**
	 * <PRE>
	 *  TRISim用ノードコンストラクタ
	 *  ID番号を指定し、x,y,zが格納された配列で座標を指定。
	 * </PRE>
     * @param id ノードのID番号
	 * @param elm   x,y,z座標のdouble型配列
	 */
    public ERTriageNode( long  id, double[] elm )
    {
        this.id = id;
        this.elements = elm;
        this.position=new Position(elements);
    }

	/**
	 * <PRE>
	 *  TRISim用ノードコンストラクタ
	 *  ID番号を指定し、x,yを指定。
	 * </PRE>
     * @param id ノードのID番号
     * @param x x座標
     * @param y y座標
	 */
    public ERTriageNode(long id, double x, double y)
    {
        // double配列を作成して上のコンストラクタを呼んでるよん
        this( id, new  double[]{x, y, 0} );
    }

	/**
	 * <PRE>
	 *  TRISim用ノードコンストラクタ
	 *  ID番号を指定し、x,y,zを指定。
	 * </PRE>
     * @param id ノードのID番号
     * @param x x座標
     * @param y y座標
     * @param z z座標
	 */
    public ERTriageNode(long id, double x, double y, double z)
    {
        this(id, new double[]{x, y, z});
    }

    /**
     * <PRE>
     *     汎用属性情報を取得します
     *  </PRE>
     *  @return 汎用情報
     */
    public Object getProperty()
    {
        return this.property;
    }

    /**
     * <PRE>
     * 汎用属性情報を設定します
     * </PRE>
     * @param property 汎用情報
     */
    public void setProperty(Object property)
    {
        this.property=property;
    }

    /**
     * <PRE>
     *     IDの取得
     * </PRE>
     * @return ID
     */
    public long getId()
    {
        return this.id;
    }

    /**
     * <PRE>
     *    このノードから出発しているlinkの数を取得します
     * </PRE>
     * @return ノードからでていくリンク数。
     */
    public int getNumOfLinks()
    {
        return this.links.size();
    }

    /**
     * <PRE>
     *   linkの追加
     *    引数：linkのインスタンス
     * </PRE>
     * @param link リンク
     */
    public void addLink(ERTriageLink link)
    {
        this.links.add(link);
    }

    /**
     * <PRE>
     *    linkの追加
     *    引数：他のノードとそのノードまでの距離
     * </PRE>
     * @param node 		ノード
     * @param distance	距離
     */
    public void addLink(ERTriageNode node, double distance)
    {
        ERTriageLink link=new ERTriageLink(this, node, distance);
        this.addLink(link);
    }

    /**
     * <PRE>
     *   linkの削除
     *   引数：linkのインスタンス
     * </PRE>
     * @param link リンク
     */
    public void removeLink(ERTriageLink link)
    {
        this.links.remove(link);
    }

    /**
     * <PRE>
     *    linkの全削除です
     *    ただし、自分からのリンクは切断されるが自分を対象としたリンクは残ります
     * </PRE>
     */
    public void removeAllLinks()
    {
        this.links.clear();
    }

    /**
     * <PRE>
     *     linkの全削除です
     *     このノードに向かうリンクも切断します
     * </PRE>
     */
    public void removeAllLinksCompletely()
    {
        for(ERTriageLink link:this.getLinks())
        {
            ERTriageNode target=link.getDestination();
            target.removeLink(target.getLink(this));
        }
        this.removeAllLinks();
    }

    /**
     * <PRE>
     *    x, y, z座標の取得
     * 戻り値：double[] elements;
     * </PRE>
     * @return 倍精度配列型(x,y,z)
     */
    public double[] getPosByArray()
    {
        return this.elements;
    }

    /**
     * <PRE>
     *    x, y, z座標の取得
     *    戻り値：Position;
     * </PRE>
     * @return Positionクラス
     */
    public Position getPosition()
    {
        return this.position;
    }

    /**
     * <PRE>
     *    このノードに隣接したノードのリストを返します
     * </PRE>
     * @return 隣接ノードすべて
     */
    public List<ERTriageNode> getNextNodes()
    {
        List<ERTriageNode> nexts=new ArrayList<ERTriageNode>(this.getNumOfLinks());
        for (ERTriageLink link:this.getLinks())
        {
            nexts.add(link.getDestination());
        }
        return nexts;
    }

    /**
     * <PRE>
     *   linkの取得です
     *   相手ノードを指定することでそこに至るリンクを取得します．
     *   そのようなリンクが無ければnullが返ります
     * </PRE>
     * @param targetNode 相手のノード
     * @return 相手ノードまで至るリンク
     */
    public ERTriageLink getLink(ERTriageNode targetNode)
    {
        ERTriageLink result=null;
        for(ERTriageLink link:this.getLinks())
        {
            if (link.getDestination()==targetNode)
            {
                result=link;
                break;
            }
        }
        return result;
    }

    /**
     * <PRE>
     *    全Linkの取得
     *    戻り値：LinkedList &lt;Edge&gt; link;
     * </PRE>
     *
     * @return 全リンクリスト
     */
    public LinkedList<ERTriageLink> getLinks()
    {
        return this.links;
    }

    /**
     * <PRE>
     *    コピーメソッド
     *    座標は新しい配列を作ってコピー元から複製されていますが、
     *    リンクについてはコピー元と同じインスタンスを参照しているので注意すること
     * </PRE>
     *
     * @return 複製したノード
     */
    @Override
    public ERTriageNode clone()
    {
        double[] pos=this.getPosByArray();
        ERTriageNode newNode=new ERTriageNode(this.getId(),pos[0],pos[1],pos[2]);

        for(ERTriageLink link:this.getLinks())
        {
            newNode.addLink(link);
        }

        return newNode;
    }

    /**
     * <PRE>
     * 	  リンクを無視してノードのみを複製します
     * </PRE>
     *
     * @return ノードのみ複製インスタンス
     */
    public ERTriageNode simpleClone()
    {
        double[] pos = this.getPosByArray();
        ERTriageNode newNode = new ERTriageNode( this.getId(),pos[0],pos[1],pos[2] );
        return newNode;
    }

   @Override
    public SaveDataPackage saveStatus()
    {
        SaveDataPackage sdp=new SaveDataPackage(this);
        sdp.addData("nodex",elements[0]);
        sdp.addData("nodey",elements[1]);
        sdp.addData("nodez",elements[2]);
        sdp.addChildPackage(this.position);
        return sdp;
    }

    @Override
    public Savable restoreStatus(SaveDataPackage saveData)
    {
        this.elements=new double[3];
        this.elements[0]=(Double)saveData.getData("nodex:value");
        this.elements[1]=(Double)saveData.getData("nodey:value");
        this.elements[2]=(Double)saveData.getData("nodez:value");
        for(SaveDataPackage pack:saveData.getAllChildren())
        {
            if (pack.getOwnerClass().equals(Position.class))
            {
                this.position=(Position)pack.restore();
            }
        }
        return this;
    }

	/**
	 * <PRE>
	 *   ノードが所属する部屋番号を取得します。
	 * </PRE>
	 * @return 部屋番号
	 */
	public int iGetLocation()
	{
		return iLocation;
	}

	/**
	 * <PRE>
	 *   ノードが所属する部屋番号を設定します。
	 * </PRE>
	 * @param iLoc 部屋番号
	 */
	public void vSetLocation( int iLoc )
	{
		// TODO 自動生成されたメソッド・スタブ
		iLocation = iLoc;
	}

	/**
	 * <PRE>
	 *   ノードが所属する部屋の階数を取得します。
	 * </PRE>
	 * @return 部屋番号
	 */
	public int iGetFloor()
	{
		return iFloor;
	}

	/**
	 * <PRE>
	 *   ノードが所属する部屋の階数を設定します。
	 * </PRE>
	 * @param iLoc 部屋番号
	 */
	public void vSetFloor( int iFloorData )
	{
		// TODO 自動生成されたメソッド・スタブ
		iFloor = iFloorData;
	}
}
