package triage;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import triage.room.ERConsultationRoom;
import triage.room.EROperationRoom;
import utility.csv.CCsv;
import utility.initparam.InitGUISimParam;
import utility.node.ERTriageLink;
import utility.node.ERTriageNode;
import utility.node.ERTriageNodeManager;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;

public class ERDepartmentDraw2D extends ObjectDrawer2D
{
	private int[][] ppiX;	// 各階の描画用X座標
	private int[][] ppiY;	// 各階の描画用Y座標
	private int[][] ppiZ;	// 各階の描画用Z座標
	private int[] piF;		// 各階ごとの描画点数
	private int[][] ppiInnerOuter;	// 外枠か内枠か

	private ERDepartment erEmergencyDepartment;			// 救急部門全体を表すクラス
	private static InitGUISimParam initSimParam;		// 描画用初期設定ファイルクラス

	/**
	 * <PRE>
	 *   GUIモード用初期設定ファイルを設定します。
	 * </PRE>
	 * @param initparam
	 */
	public static void vSetInitGuiSimParam( InitGUISimParam initparam )
	{
		initSimParam = initparam;
	}

	@Override
	public void setVirtualObject( VirtualObject obj )
	{
		super.setVirtualObject(obj);
		erEmergencyDepartment = (ERDepartment)obj;

	}

	@Override
	public void draw(Graphics g, FusePanel2D panel)
	{
		int i,j;
		int[] piX = null;
		int[] piY = null;
		int iScreenXStart, iScreenYStart, iScreenXEnd, iScreenYEnd;
		Graphics2D g2 = (Graphics2D)g;

		erEmergencyDepartment = (ERDepartment)this.getVirtualObject();

	// 救急部門の描画を実施します。(塗りつぶしはしない。)

		// 描画用座標ファイルから読み込んだデータを取得します。
		ppiX = erEmergencyDepartment.ppiGetX();
		ppiY = erEmergencyDepartment.ppiGetY();
		ppiZ = erEmergencyDepartment.ppiGetZ();
		piF = erEmergencyDepartment.piGetFloor();

		// 階数ごとに描画していきます。
		for( i = 0;i < piF.length; i++ )
		{
			piX = new int[piF[i]];
			piY = new int[piF[i]];
			// 各階ごとの描画点数分変換します。
			for( j = 0;j < piF[i]; j++ )
			{
	//			System.out.print( "(" + erEmergencyDepartment.ppiGetX()[i][j] + "," + erEmergencyDepartment.ppiGetY()[i][j] + ")" + "," );
				// スクリーンの座標系に変換します。
				piX[j] = panel.getScreenX( erEmergencyDepartment.ppiGetX()[i][j] );
				piY[j] = panel.getScreenY( erEmergencyDepartment.ppiGetY()[i][j] );
			}
//			System.out.println();
			// 描画を実行します。
			g.drawPolyline( piX, piY, piF[i] );
			piX = null;
			piY = null;
		}

// デバックモードノード間をすべて結びます。
		if( initSimParam.iGetDebugMode() == 1 )
		{
			if( erEmergencyDepartment.getNodeManager() != null )
			{
				ERTriageNodeManager erNodeManager = erEmergencyDepartment.getERTriageNodeManager();

				// すべてのノードを取得します。
				List<ERTriageNode> nodes = erNodeManager.getAllReference();

				// ノードを順番に検索して関連ノードを直線で結びます。
				for( ERTriageNode startNode:nodes)
				{
					LinkedList<ERTriageLink> links = startNode.getLinks();
					for( ERTriageLink link:links)
					{
						// 接続先のノードを取得します。
						ERTriageNode endNode = link.getDestination();

						// スクリーン座標に変換します。
						iScreenXStart = panel.getScreenX( startNode.getPosition().getX() );
						iScreenYStart = panel.getScreenY( startNode.getPosition().getY() );
						iScreenXEnd = panel.getScreenX( endNode.getPosition().getX() );
						iScreenYEnd = panel.getScreenY( endNode.getPosition().getY() );

						// もしも、一方向のみしかない場合は色違いで表示します。
						if( endNode.getLink(startNode) == null )
						{
							// 接続元と、接続先を直線で結びます。
							BasicStroke cStroke = new BasicStroke( 2.0f );
							g2.setStroke( cStroke );
							g2.setColor(Color.blue);
							g2.drawLine( iScreenXStart, iScreenYStart, iScreenXEnd, iScreenYEnd );
						}
						else
						{
							// 接続元と、接続先を直線で結びます。
							BasicStroke cStroke = new BasicStroke( 2.0f );
							g2.setStroke( cStroke );
							g2.setColor(Color.red);
							g2.drawLine( iScreenXStart, iScreenYStart, iScreenXEnd, iScreenYEnd );
						}
						// 接続元を描画します。
						g2.fillOval(iScreenXStart, iScreenYStart, 5, 5);
					}
				}
			}
		}
		g2.setColor(Color.black);
	}
}
