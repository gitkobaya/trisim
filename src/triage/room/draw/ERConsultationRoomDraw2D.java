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
import triage.room.ERConsultationRoom;
import utility.csv.CCsv;

public class ERConsultationRoomDraw2D extends ObjectDrawer2D
{
	ERConsultationRoom erConsultationRoom;

	@Override
	public void setVirtualObject( VirtualObject obj )
	{
		super.setVirtualObject(obj);
//		erConsultationRoom = (ERConsultationRoom)obj;
	}

	@Override
	public void draw(Graphics g, FusePanel2D panel)
	{
		Graphics2D g2 = (Graphics2D)g;
		erConsultationRoom = (ERConsultationRoom)this.getVirtualObject();
		// TODO 自動生成されたメソッド・スタブ
		int iX = panel.getScreenX(erConsultationRoom.iGetX());
		int iY = panel.getScreenY(erConsultationRoom.iGetY());
		double lfDots = panel.getDotsByMeter();
		int iWidth = (int)(erConsultationRoom.iGetWidth()*lfDots);
		int iHeight = (int)(erConsultationRoom.iGetHeight()*lfDots);
		// 診察室の描画を実施します。(塗りつぶしはしない。)
		BasicStroke cStroke = new BasicStroke( 2.0f );
		g2.setStroke( cStroke );
		g2.setColor(Color.red);
//		g2.drawRect( erConsultationRoom.iGetX(), erConsultationRoom.iGetY(), erConsultationRoom.iGetWidth(), erConsultationRoom.iGetHeight() );
		g2.drawRect( iX, iY, iWidth, iHeight );
		g2.setColor(Color.black);
		g2.setStroke( new BasicStroke( 1.0f ) );
	}
}
