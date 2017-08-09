package triage.agent.draw;

import java.awt.Graphics;

import triage.agent.ERNurseAgent;
import triage.agent.ERPatientAgent;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;

public class ERPatientAgentDraw2D  extends ObjectDrawer2D
{
	double lfCurX;
	double lfCurY;
	double lfCurZ;
	double lfTimeStep;
	double[] alfCurVelocity = {0.0,0.0,0.0};
	ERPatientAgent erPatientAgent;

	int iPatientAgentWidth = 15;
	int iPatientAgentHeight = 15;

	@Override
	public void setVirtualObject( VirtualObject obj )
	{
		super.setVirtualObject(obj);
		erPatientAgent = (ERPatientAgent)obj;
	}


	@Override
	public void draw(Graphics g, FusePanel2D panel)
	{
		// TODO 自動生成されたメソッド・スタブ

		int iX,iY;
		int iWidth;
		int iHeight;
		double lfMoveTime = 0.0;
		double lfDots = 0.0;

		if( erPatientAgent.lfGetTimeCourse() > 0.0 )
		{
			// 患者の現在位置を取得します。
			lfCurX = erPatientAgent.getPosition().getX();
			lfCurY = erPatientAgent.getPosition().getY();
			lfCurZ = erPatientAgent.getPosition().getZ();

			// スクリーンの拡大幅を取得し、それに合わせて幅高さを設定します。
			lfDots = panel.getDotsByMeter();

			// ○表示をします。
			iX=(int)panel.getScreenX( lfCurX );
			iY=(int)panel.getScreenY( lfCurY );
			iWidth = (int)(iPatientAgentWidth*lfDots);
			iHeight = (int)(iPatientAgentHeight*lfDots);

			// 楕円を作成します。
//			g.drawOval(iX, iY, iPatientAgentWidth, iPatientAgentHeight );

			// 内部を塗りつぶします。
			g.fillOval(iX, iY, iWidth, iHeight );
		}
	}

}