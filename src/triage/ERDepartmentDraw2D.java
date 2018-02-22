package triage;


import java.awt.Color;
import java.awt.Graphics;
import java.io.IOException;

import triage.room.ERConsultationRoom;
import triage.room.EROperationRoom;
import utility.csv.CCsv;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;

public class ERDepartmentDraw2D extends ObjectDrawer2D
{
	private int[][] ppiX;	// 各階の描画用X座標
	private int[][] ppiY;	// 各階の描画用Y座標
	private int[][] ppiZ;	// 各階の描画用Z座標
	private int[] piF;		// 各階ごとの描画点数
	private int[][] ppiInnerOuter;	// 外枠か内枠か

	ERDepartment erEmergencyDepartment;

	@Override
	public void setVirtualObject( VirtualObject obj )
	{
		super.setVirtualObject(obj);
		erEmergencyDepartment = (ERDepartment)obj;

	}

	@Override
	public void draw(Graphics g, FusePanel2D panel)
	{
		int i,j;
		int[] piX = null;
		int[] piY = null;

		erEmergencyDepartment = (ERDepartment)this.getVirtualObject();

	// 救急部門の描画を実施します。(塗りつぶしはしない。)

		// 描画用座標ファイルから読み込んだデータを取得します。
		ppiX = erEmergencyDepartment.ppiGetX();
		ppiY = erEmergencyDepartment.ppiGetY();
		ppiZ = erEmergencyDepartment.ppiGetZ();
		piF = erEmergencyDepartment.piGetFloor();

		// 階数ごとに描画していきます。
		for( i = 0;i < piF.length; i++ )
		{
			piX = new int[piF[i]];
			piY = new int[piF[i]];
			// 各階ごとの描画点数分変換します。
			for( j = 0;j < piF[i]; j++ )
			{
	//			System.out.print( "(" + erEmergencyDepartment.ppiGetX()[i][j] + "," + erEmergencyDepartment.ppiGetY()[i][j] + ")" + "," );
				// スクリーンの座標系に変換します。
				piX[j] = panel.getScreenX( erEmergencyDepartment.ppiGetX()[i][j] );
				piY[j] = panel.getScreenY( erEmergencyDepartment.ppiGetY()[i][j] );
			}
//			System.out.println();
			// 描画を実行します。
			g.drawPolyline( piX, piY, piF[i] );
			piX = null;
			piY = null;
		}
	}
}
