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
import triage.room.ERObservationRoom;
import triage.room.EROperationRoom;
import utility.csv.CCsv;

public class EROperationRoomDraw2D extends ObjectDrawer2D
{
	EROperationRoom erOperationRoom;

	@Override
	public void setVirtualObject( VirtualObject obj )
	{
		super.setVirtualObject(obj);
		erOperationRoom = (EROperationRoom)obj;
	}

	@Override
	public void draw(Graphics g, FusePanel2D panel)
	{
		Graphics2D g2 = (Graphics2D)g;
		erOperationRoom = (EROperationRoom)this.getVirtualObject();
		// TODO 自動生成されたメソッド・スタブ

		// スクリーンの座標系に変換します。
		int iX = panel.getScreenX(erOperationRoom.iGetX());
		int iY = panel.getScreenY(erOperationRoom.iGetY());

		// スクリーンの拡大幅を取得し、それに合わせて幅高さを設定します。
		double lfDots = panel.getDotsByMeter();
		int iWidth = (int)(erOperationRoom.iGetWidth()*lfDots);
		int iHeight = (int)(erOperationRoom.iGetHeight()*lfDots);
		// TODO 自動生成されたメソッド・スタブ

		// 手術室の描画を実施します。(塗りつぶしはしない。)
		BasicStroke cStroke = new BasicStroke( 2.0f );
		g2.setStroke( cStroke );
		g2.setColor(Color.red);
//		g2.drawRect( erOperationRoom.iGetX(), erOperationRoom.iGetY(), erOperationRoom.iGetWidth(), erOperationRoom.iGetHeight() );
		g2.drawRect( iX, iY, iWidth, iHeight );
		g2.setColor(Color.black);
		g2.setStroke( new BasicStroke( 1.0f ) );
	}
}
