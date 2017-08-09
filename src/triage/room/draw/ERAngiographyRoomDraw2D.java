package triage.room.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import triage.room.ERConsultationRoom;
import triage.room.ERExaminationAngiographyRoom;

public class ERAngiographyRoomDraw2D extends ObjectDrawer2D{
	ERExaminationAngiographyRoom erAngiographyRoom;

	@Override
	public void setVirtualObject( VirtualObject obj )
	{
		super.setVirtualObject(obj);
		erAngiographyRoom = (ERExaminationAngiographyRoom)obj;
	}

	@Override
	public void draw(Graphics g, FusePanel2D panel)
	{
		Graphics2D g2 = (Graphics2D)g;
		erAngiographyRoom = (ERExaminationAngiographyRoom)this.getVirtualObject();
		// TODO 自動生成されたメソッド・スタブ
		int iX = panel.getScreenX(erAngiographyRoom.iGetX());
		int iY = panel.getScreenY(erAngiographyRoom.iGetY());
		double lfDots = panel.getDotsByMeter();
		int iWidth = (int)(erAngiographyRoom.iGetWidth()*lfDots);
		int iHeight = (int)(erAngiographyRoom.iGetHeight()*lfDots);
		// 血管造影室の描画を実施します。(塗りつぶしはしない。)
//		g.drawRect( erAngiographyRoom.iGetX(), erAngiographyRoom.iGetY(), erAngiographyRoom.iGetWidth(), erAngiographyRoom.iGetHeight() );
		BasicStroke cStroke = new BasicStroke( 2.0f );
		g2.setStroke( cStroke );
		g2.setColor(Color.blue);
		g2.drawRect( iX, iY, iWidth, iHeight );
		g2.setColor(Color.black);
		g2.setStroke( new BasicStroke( 1.0f ) );
	}
}
