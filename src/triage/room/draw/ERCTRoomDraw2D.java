package triage.room.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.ArrayList;

import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import triage.room.ERExaminationAngiographyRoom;
import triage.room.ERExaminationCTRoom;
import utility.csv.CCsv;

public class ERCTRoomDraw2D extends ObjectDrawer2D
{
	ERExaminationCTRoom erCTRoom;

	@Override
	public void setVirtualObject( VirtualObject obj )
	{
		super.setVirtualObject(obj);
		erCTRoom = (ERExaminationCTRoom)obj;
	}

	@Override
	public void draw(Graphics g, FusePanel2D panel)
	{
		Graphics2D g2 = (Graphics2D)g;
		erCTRoom = (ERExaminationCTRoom)this.getVirtualObject();
		// TODO 自動生成されたメソッド・スタブ
		int iX = panel.getScreenX(erCTRoom.iGetX());
		int iY = panel.getScreenY(erCTRoom.iGetY());
		double lfDots = panel.getDotsByMeter();
		int iWidth = (int)(erCTRoom.iGetWidth()*lfDots);
		int iHeight = (int)(erCTRoom.iGetHeight()*lfDots);
		// 診察室の描画を実施します。(塗りつぶしはしない。)
		BasicStroke cStroke = new BasicStroke( 2.0f );
		g2.setStroke( cStroke );
		g2.setColor(Color.blue);
//		g2.drawRect( erCTRoom.iGetX(), erCTRoom.iGetY(), erCTRoom.iGetWidth(), erCTRoom.iGetHeight() );
		g2.drawRect( iX, iY, iWidth, iHeight );
		g2.setColor(Color.black);
		g2.setStroke( new BasicStroke( 1.0f ) );
	}
}
