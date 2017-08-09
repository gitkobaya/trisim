package main;
import java.util.ArrayList;

import triage.room.ERConsultationRoom;
import triage.room.EREmergencyRoom;
import triage.room.ERExaminationAngiographyRoom;
import triage.room.ERExaminationCTRoom;
import triage.room.ERExaminationFastRoom;
import triage.room.ERExaminationMRIRoom;
import triage.room.ERExaminationXRayRoom;
import triage.room.ERGeneralWardRoom;
import triage.room.ERHighCareUnitRoom;
import triage.room.ERIntensiveCareUnitRoom;
import triage.room.ERObservationRoom;
import triage.room.EROperationRoom;
import triage.room.ERSevereInjuryObservationRoom;
import triage.room.ERWaitingRoom;
import jp.ac.nihon_u.cit.su.furulab.fuse.Finisher;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;

// FUSEにおける終了判定クラス
// FUSEではシミュレーションを終了させるのに明示的にクラスを定義してそこに記述する必要があります。

public class ERFinisher extends Finisher
{
	private double lfEndTime;
	private double lfCurrentTime;
	private SimulationEngine erEngine;

	private ArrayList<ERConsultationRoom> ArrayListConsultationRooms;
	private ArrayList<EREmergencyRoom> ArrayListEmergencyRooms;
	private ArrayList<ERExaminationXRayRoom> ArrayListXRayRooms;
	private ArrayList<ERExaminationCTRoom> ArrayListCTRooms;
	private ArrayList<ERExaminationMRIRoom> ArrayListMRIRooms;
	private ArrayList<ERExaminationAngiographyRoom> ArrayListAngiographyRooms;
	private ArrayList<ERExaminationFastRoom> ArrayListFastRooms;
	private ArrayList<ERObservationRoom> ArrayListObservationRooms;
	private ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms;
	private ArrayList<EROperationRoom> ArrayListOperationRooms;
	private ArrayList<ERHighCareUnitRoom> ArrayListHighCareUnitRooms;
	private ArrayList<ERIntensiveCareUnitRoom> ArrayListIntensiveCareUnitRooms;
	private ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms;
	private ERWaitingRoom erWaitingRoom;

	private int iSurvivalNumber;

	private Object csFinisherCriticalSection;

	public ERFinisher()
	{
		lfEndTime = 0.0;
		lfCurrentTime = 0.0;
	}

	/**
	 * <PRE>
	 *   シミュレーションエンジンを設定します。
	 * </PRE>
	 * @param eng	FUSEシミュレーションエンジン
	 */
	public void vSetSimulationEngine( SimulationEngine eng )
	{
		erEngine = eng;
		this.setEngine( eng );
	}

	/**
	 * <PRE>
	 *    現在設定しているシミュレーションエンジンを取得します。
	 * </PRE>
	 * @return	FUSEシミュレーションエンジン
	 */
	public SimulationEngine seGetSimulationEngine()
	{
		return this.getEngine();
	}

	/**
	 * <PRE>
	 *    終了条件の終了時間を設定します。
	 * </PRE>
	 * @param lfTime	シミュレーション終了時間
	 */
	public void vSetFinishTime( double lfTime )
	{
		lfEndTime = lfTime;
	}

	/**
	 * <PRE>
	 *    終了条件の終了時間の判定を実施するための現在時刻を設定します。
	 * </PRE>
	 * @param lfTime	シミュレーション時刻
	 */
	public void vSetCurrentTime( double lfTime )
	{
		lfCurrentTime = lfTime;
	}

	/**
	 * <PRE>
	 *    最終判定をじっするのに必要な部屋オブジェクトを設定します。
	 * </PRE>
	 * @param ArrayListConsultationRoomsData			全診察室
	 * @param ArrayListEmergencyRoomsData				全初療室
	 * @param ArrayListXRayRoomsData					全X線室
	 * @param ArrayListCTRoomsData						全CT室
	 * @param ArrayListMRIRoomsData						全MRI室
	 * @param ArrayListAngiographyRoomsData				全血管造影室
	 * @param ArrayListFastRoomsData					全FAST室
	 * @param ArrayListObservationRoomsData				全観察室
	 * @param ArrayListSevereInjuryObservationRoomsData	全重症観察室
	 * @param ArrayListOperationRoomsData				全手術室
	 * @param ArrayListHighCareUnitRoomsData			全高度治療室
	 * @param ArrayListIntensiveCareUnitRoomsData		全集中治療室
	 * @param ArrayListGeneralWardRoomsData				一般病棟
	 * @param erWaitingRoomData							待合室
	 */
	public void vSetRooms( ArrayList<ERConsultationRoom> ArrayListConsultationRoomsData,
							ArrayList<EREmergencyRoom> ArrayListEmergencyRoomsData,
							ArrayList<ERExaminationXRayRoom> ArrayListXRayRoomsData,
							ArrayList<ERExaminationCTRoom> ArrayListCTRoomsData,
							ArrayList<ERExaminationMRIRoom> ArrayListMRIRoomsData,
							ArrayList<ERExaminationAngiographyRoom> ArrayListAngiographyRoomsData,
							ArrayList<ERExaminationFastRoom> ArrayListFastRoomsData,
							ArrayList<ERObservationRoom> ArrayListObservationRoomsData,
							ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRoomsData,
							ArrayList<EROperationRoom> ArrayListOperationRoomsData,
							ArrayList<ERHighCareUnitRoom> ArrayListHighCareUnitRoomsData,
							ArrayList<ERIntensiveCareUnitRoom> ArrayListIntensiveCareUnitRoomsData,
							ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRoomsData,
							ERWaitingRoom erWaitingRoomData)
	{
		ArrayListConsultationRooms					= ArrayListConsultationRoomsData;
		ArrayListEmergencyRooms						= ArrayListEmergencyRoomsData;
		ArrayListXRayRooms							= ArrayListXRayRoomsData;
		ArrayListCTRooms							= ArrayListCTRoomsData;
		ArrayListMRIRooms							= ArrayListMRIRoomsData;
		ArrayListAngiographyRooms					= ArrayListAngiographyRoomsData;
		ArrayListFastRooms							= ArrayListFastRoomsData;
		ArrayListObservationRooms					= ArrayListObservationRoomsData;
		ArrayListSevereInjuryObservationRooms		= ArrayListSevereInjuryObservationRoomsData;
		ArrayListOperationRooms						= ArrayListOperationRoomsData;
		ArrayListHighCareUnitRooms					= ArrayListHighCareUnitRoomsData;
		ArrayListIntensiveCareUnitRooms				= ArrayListIntensiveCareUnitRoomsData;
		ArrayListGeneralWardRooms					= ArrayListGeneralWardRoomsData;
		erWaitingRoom								= erWaitingRoomData;
	}

	/***
	 * <PRE>
	 *    生存数を取得します。
	 * </PRE>
	 * @return	生存数
	 */
	public int iGetSurvivalNumber()
	{
		return iSurvivalNumber;
	}

	/**
	 * <PRE>
	 *    シミュレーション終了時点で生存している患者の人数を算出します。
	 * </PRE>
	 * @return	現時点での生存数
	 */
	private int iCalcSurvivalNumber()
	{
		int i;
		int iCount = 0;

// 現在部屋に残っている人数をカウントします。

		synchronized( csFinisherCriticalSection )
		{
			for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
			{
				iCount += ArrayListConsultationRooms.get(i).iGetPatientInARoom();
			}
			for( i = 0;i < ArrayListOperationRooms.size(); i++ )
			{
				iCount += ArrayListOperationRooms.get(i).iGetPatientInARoom();
			}
			for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
			{
				iCount += ArrayListEmergencyRooms.get(i).iGetPatientInARoom();

				// 退院した患者数もカウントします。
				iCount += ArrayListEmergencyRooms.get(i).iGetDisChargeNum();
			}
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				iCount += ArrayListObservationRooms.get(i).iGetPatientInARoom();
			}
			for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
			{
				iCount += ArrayListSevereInjuryObservationRooms.get(i).iGetPatientInARoom();
			}
			for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
			{
				iCount += ArrayListIntensiveCareUnitRooms.get(i).iGetPatientInARoom();
			}
			for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
			{
				iCount += ArrayListHighCareUnitRooms.get(i).iGetPatientInARoom();
			}
			for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
			{
				iCount += ArrayListGeneralWardRooms.get(i).iGetPatientInARoom();

				// 退院した患者数もカウントします。
				iCount += ArrayListGeneralWardRooms.get(i).iGetDisChargeNum();
			}
			iCount += erWaitingRoom.iGetPatientInARoom();

			// 退院した患者数もカウントします。
			iCount += erWaitingRoom.iGetDisChargeNum();

			for( i = 0;i < ArrayListXRayRooms.size(); i++ )
			{
				iCount += ArrayListXRayRooms.get(i).iGetPatientInARoom();
			}
			for( i = 0;i < ArrayListCTRooms.size(); i++ )
			{
				iCount += ArrayListCTRooms.get(i).iGetPatientInARoom();
			}
			for( i = 0;i < ArrayListMRIRooms.size(); i++ )
			{
				iCount += ArrayListMRIRooms.get(i).iGetPatientInARoom();
			}
			for( i = 0;i < ArrayListAngiographyRooms.size(); i++ )
			{
				iCount += ArrayListAngiographyRooms.get(i).iGetPatientInARoom();
			}
			for( i = 0;i < ArrayListFastRooms.size(); i++ )
			{
				iCount += ArrayListFastRooms.get(i).iGetPatientInARoom();
			}
		}
		return iCount;
	}

	/**
	 * <PRE>
	 *    終了判定
	 * </PRE>
	 */
	@Override
	public boolean isFinish()
	{
		if( lfEndTime < lfCurrentTime )
		{
			iSurvivalNumber = iCalcSurvivalNumber();
			return true;
		}
		return false;
	}

	/**
	 * <PRE>
	 *    クリティカルセクションインスタンスを設定します。
	 * </PRE>
	 * @param cs	クリティカルセクションインスタンス
	 */
	public void vSetCriticalSection(Object cs)
	{
		// TODO 自動生成されたメソッド・スタブ
		csFinisherCriticalSection = cs;
	}

}
