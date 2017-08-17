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

	public int iGetX()
	{
		return iDrawX;
	}
	public int iGetY()
	{
		return iDrawY;
	}
	public int iGetWidth()
	{
		return iDrawWidth;
	}
	public int iGetHeight()
	{
		return iDrawHeight;
	}
	public int iGetF()
	{
		return iDrawF;
	}
	public void vSetX( int iData )
	{
		iDrawX = iData;
	}
	public void vSetY( int iData )
	{
		iDrawY = iData;
	}
	public void vSetZ( int iData )
	{
		iDrawZ = iData;
	}
	public void vSetWidth( int iData )
	{
		iDrawWidth = iData;
	}
	public void vSetHeight( int iData )
	{
		iDrawHeight = iData;
	}
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
