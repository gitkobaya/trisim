package triage.agent.draw;

import java.awt.Graphics;

import triage.agent.ERDoctorAgent;
import triage.agent.ERNurseAgent;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;

public class ERNurseAgentDraw2D  extends ObjectDrawer2D
{
	ERNurseAgent erNurseAgent;
	@Override
	public void setVirtualObject( VirtualObject obj )
	{
		super.setVirtualObject(obj);
		erNurseAgent = (ERNurseAgent)obj;
	}

	@Override
	public void draw(Graphics g, FusePanel2D panel)
	{
		// TODO 自動生成されたメソッド・スタブ

		int iX,iY;
		int[] alfX;
		int[] alfY;
		double lfDots = 1.0;

		alfX = new int[3];
		alfY = new int[3];

		// スクリーンの拡大幅を取得し、それに合わせて幅高さを設定します。
		lfDots = panel.getDotsByMeter();

		// △表示をします。
		alfX[0]=(int)panel.getScreenX(erNurseAgent.getX());
		alfY[0]=(int)panel.getScreenY(erNurseAgent.getY()+5);
		alfX[1]=(int)panel.getScreenX(erNurseAgent.getX()+5);
		alfY[1]=(int)panel.getScreenY(erNurseAgent.getY()-5);
		alfX[2]=(int)panel.getScreenX(erNurseAgent.getX()-5);
		alfY[2]=(int)panel.getScreenY(erNurseAgent.getY()-5);

		// △を作成します。
		g.drawPolyline(alfX, alfY, 3);

		// 三角形の中を塗りつぶします。
		g.fillPolygon(alfX, alfY, 3);
	}

}
