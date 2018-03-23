package triage.agent.draw;

import java.awt.Color;
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
			// 患者エージェントが亡くなられている場合で、ループ終了前に描画に繰るタイミングがあり、
			// その場合は描画しません。
			if( erPatientAgent.iGetSurvivalFlag() == 0 ) return;

			// 患者の現在位置を取得します。
			lfCurX = erPatientAgent.getPosition().getX();
			lfCurY = erPatientAgent.getPosition().getY();
			lfCurZ = erPatientAgent.getPosition().getZ();
			// スクリーンの拡大幅を取得し、それに合わせて幅高さを設定します。
			lfDots = panel.getDotsByMeter();

			// ○表示をします。
			iX=(int)panel.getScreenX( lfCurX );
			iY=(int)panel.getScreenY( lfCurY );
			iWidth = (int)(erPatientAgent.lfGetWidth()*lfDots);
			iHeight = (int)(erPatientAgent.lfGetHeight()*lfDots);

			// 楕円を作成します。
			g.drawOval(iX, iY, iWidth, iHeight );

			// 患者の緊急度に応じて色を変更します。(JTASの色を指定)
			if( erPatientAgent.iGetEmergencyLevel() == 1 )
			{
				g.setColor(Color.BLUE);
			}
			else if( erPatientAgent.iGetEmergencyLevel() == 2 )
			{
				g.setColor(Color.RED);
			}
			else if( erPatientAgent.iGetEmergencyLevel() == 3 )
			{
				g.setColor(Color.YELLOW);
			}
			else if( erPatientAgent.iGetEmergencyLevel() == 4 )
			{
				g.setColor(Color.GREEN);
			}
			else if( erPatientAgent.iGetEmergencyLevel() == 5 )
			{
				g.setColor(Color.WHITE);
			}

			// 内部を塗りつぶします。
			g.fillOval(iX, iY, iWidth, iHeight );

			g.setColor(Color.BLACK);
		}
	}

}
