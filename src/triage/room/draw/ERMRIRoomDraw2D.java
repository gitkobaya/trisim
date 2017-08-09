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
import triage.room.ERExaminationMRIRoom;
import utility.csv.CCsv;

public class ERMRIRoomDraw2D extends ObjectDrawer2D
{
	ERExaminationMRIRoom erExaminationMRIRoom;

	@Override
	public void setVirtualObject( VirtualObject obj )
	{
		super.setVirtualObject(obj);
		erExaminationMRIRoom = (ERExaminationMRIRoom)obj;
	}

	@Override
	public void draw(Graphics g, FusePanel2D panel)
	{
		Graphics2D g2 = (Graphics2D)g;
		erExaminationMRIRoom = (ERExaminationMRIRoom)this.getVirtualObject();
		// TODO 自動生成されたメソッド・スタブ
		int iX = panel.getScreenX(erExaminationMRIRoom.iGetX());
		int iY = panel.getScreenY(erExaminationMRIRoom.iGetY());
		double lfDots = panel.getDotsByMeter();
		int iWidth = (int)(erExaminationMRIRoom.iGetWidth()*lfDots);
		int iHeight = (int)(erExaminationMRIRoom.iGetHeight()*lfDots);

		// 診察室の描画を実施します。(塗りつぶしはしない。)
		BasicStroke cStroke = new BasicStroke( 2.0f );
		g2.setStroke( cStroke );
		g2.setColor(Color.blue);
//		g2.drawRect( erExaminationMRIRoom.iGetX(), erExaminationMRIRoom.iGetY(), erExaminationMRIRoom.iGetWidth(), erExaminationMRIRoom.iGetHeight() );
		g2.drawRect( iX, iY, iWidth, iHeight );
		g2.setColor(Color.black);
		g2.setStroke( new BasicStroke( 1.0f ) );
	}
}
