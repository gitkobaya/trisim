package triage.agent.draw;

import java.awt.Graphics;

import triage.agent.ERClinicalEngineerAgent;
import triage.room.EROperationRoom;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;

public class ERClinicalEngineerAgentDraw2D  extends ObjectDrawer2D
{
	ERClinicalEngineerAgent erClinicalEngineerAgent;
	@Override
	public void setVirtualObject( VirtualObject obj )
	{
		super.setVirtualObject(obj);
		erClinicalEngineerAgent = (ERClinicalEngineerAgent)obj;
	}

	@Override
	public void draw(Graphics g, FusePanel2D panel)
	{
		int iX,iY;
		int iX11,iY11;
		int iX12,iY12;
		int iX21,iY21;
		int iX22,iY22;
		double lfDots = 0.0;

		erClinicalEngineerAgent = (ERClinicalEngineerAgent)this.getVirtualObject();
		// TODO 自動生成されたメソッド・スタブ

		// スクリーンの座標系に変換します。
		iX = panel.getScreenX(erClinicalEngineerAgent.getX());
		iY = panel.getScreenY(erClinicalEngineerAgent.getY());

		// スクリーンの拡大幅を取得し、それに合わせて幅高さを設定します。
		lfDots = panel.getDotsByMeter();

		// ×表示をします。
		iX11 = (int)(iX - 5*lfDots);
		iY11 = (int)(iY - 5*lfDots);
		iX12 = (int)(iX + 5*lfDots);
		iY12 = (int)(iY + 5*lfDots);
		iX21 = (int)(iX - 5*lfDots);
		iY21 = (int)(iY + 5*lfDots);
		iX22 = (int)(iX + 5*lfDots);
		iY22 = (int)(iY - 5*lfDots);
		g.drawLine( iX11, iY11, iX12, iY12 );
		g.drawLine( iX21, iY21, iX22, iY22 );
	}

}
