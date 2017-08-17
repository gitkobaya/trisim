package triage.room;

import utility.node.ERTriageNode;
import utility.node.ERTriageNodeManager;
import jp.ac.nihon_u.cit.su.furulab.fuse.Environment;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Asset;

public class ERElevator extends Asset
{
	// 描画関係
	private int iDrawX;
	private int iDrawY;
	private int iDrawZ;
	private int iDrawCenterX;
	private int iDrawCenterY;
	private int iDrawWidth;
	private int iDrawHeight;
	private int iDrawF;

	private ERTriageNodeManager erTriageNodeManager;
	private ERTriageNode erTriageNode;
	private Environment erSimEnv;

	/**
	 * <PRE>
	 *    エレベータアセットクラスのコンストラクタです。
	 *    初期化処理を行います。
	 * </PRE>
	 */
	public ERElevator()
	{
		iDrawX			= 0;
		iDrawY			= 0;
		iDrawZ			= 0;
		iDrawCenterX	= 0;
		iDrawCenterY	= 0;
		iDrawWidth		= 0;
		iDrawHeight		= 0;
		iDrawF			= 0;
	}

	/**
	 * <PRE>
	 *    Fuseの環境を取得します。
	 * </PRE>
	 * @param cEnv	環境インスタンス
	 */
	public void vSetEnvironment( Environment cEnv )
	{
		erSimEnv = cEnv;
		erSimEnv.addAsset( this );
	}

	/**
	 * <PRE>
	 *    エレベーターのX座標を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	X座標
	 */
	public int iGetX()
	{
		return iDrawX;
	}

	/**
	 * <PRE>
	 *    エレベーターのY座標を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	Y座標
	 */
	public int iGetY()
	{
		return iDrawY;
	}

	/**
	 * <PRE>
	 *    エレベーターの横幅を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	横幅
	 */
	public int iGetWidth()
	{
		return iDrawWidth;
	}

	/**
	 * <PRE>
	 *    エレベーターの縦幅を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	縦幅
	 */
	public int iGetHeight()
	{
		return iDrawHeight;
	}

	/**
	 * <PRE>
	 *    エレベーターの階数を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	階数
	 */
	public int iGetF()
	{
		return iDrawF;
	}

	/**
	 * <PRE>
	 *    エレベーターのX座標を格納します。
	 * </PRE>
	 * @param iData	X座標
	 */
	public void vSetX( int iData )
	{
		iDrawX = iData;
	}

	/**
	 * <PRE>
	 *    エレベーターのY座標を格納します。
	 * </PRE>
	 * @param iData	Y座標
	 */
	public void vSetY( int iData )
	{
		iDrawY = iData;
	}

	/**
	 * <PRE>
	 *    エレベーターのZ座標を格納します。
	 * </PRE>
	 * @param iData	Z座標
	 */
	public void vSetZ( int iData )
	{
		iDrawZ = iData;
	}

	/**
	 * <PRE>
	 *   エレベーターの横幅を格納します。
	 * </PRE>
	 * @param iData	横幅
	 */
	public void vSetWidth( int iData )
	{
		iDrawWidth = iData;
	}

	/**
	 * <PRE>
	 *    エレベーターの縦幅を格納します。
	 * </PRE>
	 * @param iData	縦幅
	 */
	public void vSetHeight( int iData )
	{
		iDrawHeight = iData;
	}

	/**
	 * <PRE>
	 *    エレベーターの階数を格納します。
	 * </PRE>
	 * @param iData	階数
	 */
	public void vSetF( int iData )
	{
		iDrawF = iData;
	}


	/**
	 * <PRE>
	 *    トリアージノードマネージャーを設定します。
	 * </PRE>
	 * @param erNodeManager	ノード、リンクが格納されたノードマネージャのインスタンス
	 */
	public void vSetERTriageNodeManager( ERTriageNodeManager erNodeManager )
	{
		erTriageNodeManager = erNodeManager;
	}

	/**
	 * <PRE>
	 *    現在選択されている診察室のノードを取得します。
	 * </PRE>
	 * @return	診察室のノード
	 */
	public ERTriageNode erGetTriageNode()
	{
		return erTriageNode;
	}

	/**
	 * <PRE>
	 *   エレベータのノードを設定します。
	 * </PRE>
	 * @param erNode	設定するノードインスタンス（エレベータ）
	 */
	public void vSetTriageNode( ERTriageNode erNode )
	{
		erTriageNode = erNode;
	}
}
