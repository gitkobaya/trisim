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
import triage.room.EROperationRoom;
import triage.room.ERSevereInjuryObservationRoom;
import utility.csv.CCsv;

public class ERSevereInjuryObservationRoomDraw2D extends ObjectDrawer2D
{
	ERSevereInjuryObservationRoom erSevereInjuryObservationRoom;

	@Override
	public void setVirtualObject( VirtualObject obj )
	{
		super.setVirtualObject(obj);
		erSevereInjuryObservationRoom = (ERSevereInjuryObservationRoom)obj;
	}

	@Override
	public void draw(Graphics g, FusePanel2D panel)
	{
		Graphics2D g2 = (Graphics2D)g;
		erSevereInjuryObservationRoom = (ERSevereInjuryObservationRoom)this.getVirtualObject();
		// TODO 自動生成されたメソッド・スタブ

		// スクリーンの座標系に変換します。
		int iX = panel.getScreenX(erSevereInjuryObservationRoom.iGetX());
		int iY = panel.getScreenY(erSevereInjuryObservationRoom.iGetY());

		// スクリーンの拡大幅を取得し、それに合わせて幅高さを設定します。
		double lfDots = panel.getDotsByMeter();
		int iWidth = (int)(erSevereInjuryObservationRoom.iGetWidth()*lfDots);
		int iHeight = (int)(erSevereInjuryObservationRoom.iGetHeight()*lfDots);
		// TODO 自動生成されたメソッド・スタブ

		// 重症観察室の描画を実施します。(塗りつぶしはしない。)
		BasicStroke cStroke = new BasicStroke( 2.0f );
		g2.setStroke( cStroke );
		g2.setColor(Color.orange);
//		g2.drawRect( erSevereInjuryObservationRoom.iGetX(), erSevereInjuryObservationRoom.iGetY(), erSevereInjuryObservationRoom.iGetWidth(), erSevereInjuryObservationRoom.iGetHeight() );
		g2.drawRect( iX, iY, iWidth, iHeight );
		g2.setColor(Color.black);
		g2.setStroke( new BasicStroke( 1.0f ) );
	}
}
