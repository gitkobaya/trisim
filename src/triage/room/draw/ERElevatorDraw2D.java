package triage.room.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import triage.room.ERElevator;

public class ERElevatorDraw2D extends ObjectDrawer2D{
	private ERElevator erElevator;

	@Override
	public void setVirtualObject( VirtualObject obj )
	{
		super.setVirtualObject(obj);
		erElevator = (ERElevator)obj;
	}

	@Override
	public void draw(Graphics g, FusePanel2D panel)
	{
		Graphics2D g2 = (Graphics2D)g;
		erElevator = (ERElevator)this.getVirtualObject();
		// TODO 自動生成されたメソッド・スタブ
		int iX = panel.getScreenX(erElevator.iGetX());
		int iY = panel.getScreenY(erElevator.iGetY());
		double lfDots = panel.getDotsByMeter();
		int iWidth = (int)(erElevator.iGetWidth()*lfDots);
		int iHeight = (int)(erElevator.iGetHeight()*lfDots);
		// エレベーターの描画を実施します。(塗りつぶしはしない。)
		BasicStroke cStroke = new BasicStroke( 2.0f );
		g2.setStroke( cStroke );
		g2.setColor( new Color(0, 64, 0) );
		g2.drawRect( iX, iY, iWidth, iHeight );
		g2.setColor(Color.black);
		g2.setStroke( new BasicStroke( 1.0f ) );
	}
}
