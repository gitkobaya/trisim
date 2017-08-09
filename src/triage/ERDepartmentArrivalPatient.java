package triage;
import java.io.IOException;

import triage.room.ERWaitingRoom;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.extention.FuseLightPlugIn;


public class ERDepartmentArrivalPatient extends FuseLightPlugIn
{
	private SimulationEngine cEngine;
	private ERWaitingRoom erWaitingRoom;

	public void vSetSimulationEngine( SimulationEngine engine )
	{
		cEngine = engine;
	}

	public void vSetWaitingRoom( ERWaitingRoom waitingroom )
	{
		erWaitingRoom = waitingroom;
	}

	@Override
	public void update() 
	{
		// TODO 自動生成されたメソッド・スタブ
		double lfSecond = 0.0;
			
//			for(;;)
//			{
////				sleep(cEngine.getLatestTimeStep());
//				// シミュレーションが開始したら以下を実行。
//				if( cEngine.getLogicalTime() > 0 )
//				{
//					if( cEngine.isPaused() == false )
//					{
//						// 停止していない場合にエージェントを追加。
//						lfSecond = cEngine.getLogicalTime()/3600000.0;
////						System.out.println("エージェント追加したよ～");
//						erWaitingRoom.vArrivalPatient( lfSecond, cEngine, 0, 0, 0, 0 );
//					}
//				}
//			}
		
	}

}
