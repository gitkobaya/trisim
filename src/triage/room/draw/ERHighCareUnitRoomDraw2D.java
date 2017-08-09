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
import triage.room.ERHighCareUnitRoom;
import utility.csv.CCsv;

public class ERHighCareUnitRoomDraw2D extends ObjectDrawer2D
{
	ERHighCareUnitRoom erHighCareUnitRoom;

	@Override
	public void setVirtualObject( VirtualObject obj )
	{
		super.setVirtualObject(obj);
		erHighCareUnitRoom = (ERHighCareUnitRoom)obj;
	}

	@Override
	public void draw(Graphics g, FusePanel2D panel)
	{
		Graphics2D g2 = (Graphics2D)g;
		erHighCareUnitRoom = (ERHighCareUnitRoom)this.getVirtualObject();
		// TODO 自動生成されたメソッド・スタブ
		int iX = panel.getScreenX(erHighCareUnitRoom.iGetX());
		int iY = panel.getScreenY(erHighCareUnitRoom.iGetY());
		double lfDots = panel.getDotsByMeter();
		int iWidth = (int)(erHighCareUnitRoom.iGetWidth()*lfDots);
		int iHeight = (int)(erHighCareUnitRoom.iGetHeight()*lfDots);
		// 診察室の描画を実施します。(塗りつぶしはしない。)
		BasicStroke cStroke = new BasicStroke( 2.0f );
		g2.setStroke( cStroke );
		g2.setColor( new Color( 208, 240, 208) );
//		g2.drawRect( erHighCareUnitRoom.iGetX(), erHighCareUnitRoom.iGetY(), erHighCareUnitRoom.iGetWidth(), erHighCareUnitRoom.iGetHeight() );
		g2.drawRect( iX, iY, iWidth, iHeight );
		g2.setColor(Color.black);
		g2.setStroke( new BasicStroke( 1.0f ) );
	}
}
