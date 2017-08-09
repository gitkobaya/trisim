package triage.agent.draw;

import java.awt.Graphics;

import triage.agent.ERClinicalEngineerAgent;
import triage.agent.ERDoctorAgent;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;

public class ERDoctorAgentDraw2D extends ObjectDrawer2D
{
	ERDoctorAgent erDoctorAgent;
	int iDoctorAgentWidth = 10;
	int iDoctorAgentHeight = 10;

	@Override
	public void setVirtualObject( VirtualObject obj )
	{
		super.setVirtualObject(obj);
		erDoctorAgent = (ERDoctorAgent)obj;
	}

	@Override
	public void draw(Graphics g, FusePanel2D panel)
	{
		// TODO 自動生成されたメソッド・スタブ

		int iX,iY;
		int iWidth;
		int iHeight;
		double lfDots = 0.0;

		erDoctorAgent = (ERDoctorAgent)this.getVirtualObject();
		// TODO 自動生成されたメソッド・スタブ

		// スクリーンの拡大幅を取得し、それに合わせて幅高さを設定します。
		lfDots = panel.getDotsByMeter();

		// □表示をします。
	// スクリーンの座標系に変換します。
		iX = panel.getScreenX(erDoctorAgent.getX()-10);
		iY = panel.getScreenY(erDoctorAgent.getY()-10);
		iWidth = (int)(iDoctorAgentWidth*lfDots);
		iHeight = (int)(iDoctorAgentHeight*lfDots);

//		g.drawRect( iX, iY, iDoctorAgentWidth, iDoctorAgentHeight );
		g.fillRect( iX, iY, iWidth, iHeight );
	}

}
