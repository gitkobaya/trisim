package triage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import triage.agent.ERClinicalEngineerAgent;
import triage.agent.ERDoctorAgent;
import triage.agent.ERNurseAgent;
import triage.agent.ERPatientAgent;
import utility.initparam.InitGUISimParam;
import utility.node.ERTriageLink;
import utility.node.ERTriageNode;
import utility.node.ERTriageNodeManager;
import jp.ac.nihon_u.cit.su.furulab.fuse.examples.KeyAndMouseListner2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

public class ERTriageDebugWindowKeyAndMouseListner  extends KeyAndMouseListner2D
{

	private FusePanel fusePanel;						// FusePanelのインスタンス。
	private ERTriageNodeManager erTriageNodeManager;	// ノードマネージャーのインスタンス
	private static InitGUISimParam initSimParam;		// 描画用初期設定ファイルクラス

	private JFrame jfDebugFrame;
	private JPanel jfDebugPanel;

	/**
	 * <PRE>
	 *   GUIモード用初期設定ファイルを設定します。
	 * </PRE>
	 * @param initparam  GUIモード用初期設定ファイルクラスインスタンス
	 */
	public static void vSetInitGuiSimParam( InitGUISimParam initparam )
	{
		initSimParam = initparam;
	}

	public ERTriageDebugWindowKeyAndMouseListner(FusePanel panel)
	{
		super(panel);
		// FusePanelを取得します。
		fusePanel = panel;
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		// デバックモードでない場合はクリックしても反応しないようにさせます。
		if( initSimParam.iGetDebugMode() == 0 ) return;

		// デバッグウィンドウを表示します。一度作成したら閉じるまでは作成しません。
		if( jfDebugFrame == null )
		{
			JFrame.setDefaultLookAndFeelDecorated( true );
			jfDebugFrame = new JFrame();
			jfDebugPanel = new JPanel();
			jfDebugFrame.setTitle("TRISimデバックウィンドウ");
			jfDebugFrame.setVisible( true );
			jfDebugFrame.setSize(400,400);
//			jfDebugFrame.setDefaultCloseOperation( 3 );
			// これで半透明化が可能。Colorの四番目の引数がアルファブレンドに相当。
			jfDebugFrame.setBackground( new Color(0,0,0,0.5f));
		}

		if( e.getButton() == MouseEvent.BUTTON1 )
		{
			JLabel jlabel = new JLabel();
			Position pos;
			pos = fusePanel.getWorldPos(e.getX(), e.getY());
//			jfDebugFrame.(pos.getX() + "," + pos.getY() +"," + pos.getZ() );
			String strDebug = pos.getX() + "," + pos.getY() +"," + pos.getZ();
			jlabel.setText( strDebug );
			jfDebugPanel.add( jlabel );
			Container contentPane = jfDebugFrame.getContentPane();
			contentPane.add( jfDebugPanel, BorderLayout.CENTER );
			System.out.println( strDebug );
		}
		else if( e.getButton() == MouseEvent.BUTTON3 )
		{
			double lfDist = 0.0;
			double lfMinDist = Double.MAX_VALUE;
			Agent curAgent = null;
			Position pos;

			pos = fusePanel.getWorldPos(e.getX(), e.getY());
			List<VirtualObject> listAgents = pickErAgents(e, 30);

			// 選択したエージェントの情報を出力します。
			vOutputInfoAgents( listAgents, pos );

			List<ERTriageNode> nodes;
			ERTriageNode curNode = null;

			// 選択された座標から最も近いノードを参照します。
			nodes = erTriageNodeManager.getAllReference();
			curNode = erTriageNodeManager.getNearestNode(pos);
			if( curNode != null )
			{
				// 本当に近い場合は出力します。
				if( Math.abs( pos.getX() - curNode.getPosition().getX() ) < 30 && Math.abs( pos.getY()-curNode.getPosition().getY()) < 30 )
				{
					// ある場合はコマンドライン上に出力します。
					System.out.println(curNode.getId() + "," + curNode.getPosition().getX() + "," + curNode.getPosition().getY() + "," + curNode.getPosition().getZ());
				}
			}

			// 選択された座標から最も近いリンクを取得します。
			lfMinDist = Double.MAX_VALUE;
			ERTriageLink curLink = null;
			for( ERTriageNode node: nodes )
			{
				LinkedList<ERTriageLink> links;

				// 接続している全リンクを取得します。
				links = node.getLinks();
				for( ERTriageLink link: links )
				{
					double lfStartX, lfStartY, lfEndX, lfEndY;
					double lfA, lfB, lfC;
					// 始点終点の座標を取得します。
					lfStartX = node.getPosition().getX();
					lfStartY = node.getPosition().getY();
					lfEndX = link.getDestination().getPosition().getX();
					lfEndY = link.getDestination().getPosition().getY();
					// 現在点とリンクとの距離を算出します。
					lfA = lfEndY-lfStartY;
					lfB = lfEndX-lfStartX;
					lfC = -lfA*lfStartX+lfB*lfStartY;
					lfDist = Math.abs(lfA*pos.getX()-lfB*pos.getY()+lfC)/Math.sqrt(lfA*lfA+lfB*lfB);
					if( lfDist < lfMinDist )
					{
						curLink = link;
						lfMinDist = lfDist;
					}
				}
			}
			if( curLink != null )
			{
//				if( lfDist < 30.0 )
				{
					// ある場合はコマンドライン上に出力します。
					System.out.println("開始ノード:" + curLink.getStart().getId() + "," + "終了ノード:" + curLink.getDestination().getId() );
				}
			}
		}
	}

//	@Override
//	public void mousePressed(MouseEvent e) {
//
//	}
//
//	@Override
//	public void mouseReleased(MouseEvent e) {
//
//	}
//
//	@Override
//	public void mouseEntered(MouseEvent e) {
//
//	}
//
//	@Override
//	public void mouseExited(MouseEvent e) {
//
//	}
//
	@Override
	public void keyPressed( KeyEvent e )
	{
		int mod = e.getModifiersEx();
		if( (mod & InputEvent.CTRL_DOWN_MASK)!=0 )
		{
			if( e.getKeyCode() == KeyEvent.VK_Z )
			{
			}
			else if( e.getKeyCode() == KeyEvent.VK_Y )
			{
			}
		}
	}

//	@Override
//	public void keyReleased( KeyEvent e )
//	{
//
//	}
//
//	@Override
//	public void keyTyped( KeyEvent e )
//	{
//
//	}

	public void vSetERTriageNodeManager( ERTriageNodeManager cErNodeManager )
	{
		erTriageNodeManager = cErNodeManager;
	}

	public List<VirtualObject> pickErAgents( MouseEvent e, double lfRange )
	{
		// クリックした周辺にいるエージェントを取得します。
		List<Agent> listAgents = this.pickAgents(e, 30);
		ArrayList<VirtualObject> listVoErAgents;
		VirtualObject voErAgent;
		listVoErAgents = new ArrayList<VirtualObject>();

		// エージェントの型が継承されているのでいったんVritualObject型のリストにして取得します。
		for( Agent agent:listAgents )
		{
			voErAgent = this.fusePanel.getSimulationEngine().getObjectById(agent.getId());
			listVoErAgents.add( voErAgent );
		}
		return listVoErAgents;
	}

	/**
	 * <PRE>
	 *    各エージェントの情報を出力します。
	 *    医師、看護師、医療技師、患者
	 * </PRE>
	 * @param listVoErAgents	各エージェント情報の入ったリスト
	 * @param pos				マウスクリックした位置のワールド座標
	 */
	public void vOutputInfoAgents( List<VirtualObject> listVoErAgents, Position pos )
	{
		double lfDist = 0.0;
		double lfMinDist = Double.MAX_VALUE;
		int iFlag = 0;
		VirtualObject curAgent = null;

		if( iFlag == 1 )
		{
			// 取得したエージェントが医師、看護師、医療技師、患者エージェントのうちどのエージェントかを取得します。
			for( VirtualObject voErAgent:listVoErAgents )
			{
				lfDist = Position.getDistance2D(pos.getX(), pos.getY(), voErAgent.getX(), voErAgent.getY());
				if( lfDist < lfMinDist )
				{
					curAgent = voErAgent;
					lfMinDist = lfDist;
				}
			}
			// 医師エージェントの場合
			if( curAgent instanceof ERDoctorAgent )
			{
				ERDoctorAgent erDoctorAgent = (ERDoctorAgent)curAgent;
				System.out.println("医師エージェント" + "," + erDoctorAgent.getId() + "," + erDoctorAgent.getX() + "," + erDoctorAgent.getY() + "," + erDoctorAgent.getZ() + "," + erDoctorAgent.iGetAttending() );
			}
			// 看護師エージェントの場合
			else if( curAgent instanceof ERNurseAgent )
			{
				ERNurseAgent erNurseAgent = (ERNurseAgent)curAgent;
				System.out.println("看護師エージェント" + "," + erNurseAgent.getId() + "," + erNurseAgent.getX() + "," + erNurseAgent.getY() + "," + erNurseAgent.getZ() +"," + erNurseAgent.iGetNurseCategory() + "," + erNurseAgent.iGetAttending() );
			}
			// 医療技師エージェントの場合
			else if( curAgent instanceof ERClinicalEngineerAgent )
			{
				ERClinicalEngineerAgent erClinicalEngineerAgent = (ERClinicalEngineerAgent)curAgent;
				System.out.println("医療技師エージェント" + "," + erClinicalEngineerAgent.getId() + "," + erClinicalEngineerAgent.getX() + "," + erClinicalEngineerAgent.getY() + "," + erClinicalEngineerAgent.getZ() +"," + erClinicalEngineerAgent.iGetClinicalEngineerDepartment() );
			}
			// 患者エージェントの場合
			else if( curAgent instanceof ERPatientAgent )
			{
				ERPatientAgent erPatientAgent = (ERPatientAgent)curAgent;
				ERTriageNode erStartNode, erEndNode;
				erStartNode = erPatientAgent.erGetCurrentNode();
				erEndNode = erPatientAgent.erGetNextNode();
				if( erStartNode != null && erEndNode != null )
					System.out.println("患者エージェント" + "," + erPatientAgent.getId() + "," + erPatientAgent.getX() + "," + erPatientAgent.getY() + "," + erPatientAgent.getZ() + "," + erPatientAgent.iGetLocation() + "," + erStartNode.getId() + "," + erEndNode.getId() );
				else
					System.out.println("患者エージェント" + "," + erPatientAgent.getId() + "," + erPatientAgent.getX() + "," + erPatientAgent.getY() + "," + erPatientAgent.getZ() + "," + erPatientAgent.iGetLocation() );
			}
		}
		else
		{
			for( VirtualObject voErAgent:listVoErAgents )
			{
				// 医師エージェントの場合
				if( voErAgent instanceof ERDoctorAgent )
				{
					ERDoctorAgent erDoctorAgent = (ERDoctorAgent)voErAgent;
					System.out.println("医師エージェント" + "," + erDoctorAgent.getId() + "," + erDoctorAgent.getX() + "," + erDoctorAgent.getY() + "," + erDoctorAgent.getZ() + "," + erDoctorAgent.iGetAttending() );
				}
				// 看護師エージェントの場合
				else if( voErAgent instanceof ERNurseAgent )
				{
					ERNurseAgent erNurseAgent = (ERNurseAgent)voErAgent;
					System.out.println("看護師エージェント" + "," + erNurseAgent.getId() + "," + erNurseAgent.getX() + "," + erNurseAgent.getY() + "," + erNurseAgent.getZ() + "," + erNurseAgent.iGetNurseCategory() + "," + erNurseAgent.iGetAttending() );
				}
				// 医療技師エージェントの場合
				else if( voErAgent instanceof ERClinicalEngineerAgent )
				{
					ERClinicalEngineerAgent erClinicalEngineerAgent = (ERClinicalEngineerAgent)voErAgent;
					System.out.println("医療技師エージェント" + "," + erClinicalEngineerAgent.getId() + "," + erClinicalEngineerAgent.getX() + "," + erClinicalEngineerAgent.getY() + "," + erClinicalEngineerAgent.getZ() + "," + erClinicalEngineerAgent.iGetClinicalEngineerDepartment() );
				}
				// 患者エージェントの場合
				else if( voErAgent instanceof ERPatientAgent )
				{
					ERPatientAgent erPatientAgent = (ERPatientAgent)voErAgent;
					ERTriageNode erStartNode, erEndNode;
					ArrayList<ERTriageNode> erRoute;
					erStartNode = erPatientAgent.erGetCurrentNode();
					erEndNode = erPatientAgent.erGetNextNode();
					if( erStartNode != null && erEndNode != null )
					{
						System.out.println("患者エージェント" + "," + erPatientAgent.getId() + "," + erPatientAgent.getX() + "," + erPatientAgent.getY() + "," + erPatientAgent.getZ() );
						erRoute = erPatientAgent.erGetArrayListRoute();
						for( ERTriageNode node: erRoute )
						{
							System.out.print( node.getId() + "→→" );
						}
						System.out.print( "\n");
						System.out.println( "今現在いるノード:" + erStartNode.getId());
						System.out.println( "次に向かうノード:" + erEndNode.getId());
						System.out.println( "出発点の部屋：" + erStartNode.iGetLocation());
						System.out.println( "向かう部屋:" + erEndNode.iGetLocation());
					}
					else
						System.out.println("患者エージェント" + "," + erPatientAgent.getId() + "," + erPatientAgent.getX() + "," + erPatientAgent.getY() + "," + erPatientAgent.getZ() + "," + erPatientAgent.iGetLocation() );
				}
				else
				{

				}
			}
		}
	}
}
