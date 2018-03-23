package triage.room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import triage.agent.ERClinicalEngineerAgent;
import triage.agent.ERDoctorAgent;
import triage.agent.ERNurseAgent;
import triage.agent.ERNurseAgentException;
import triage.agent.ERPatientAgent;
import utility.initparam.InitSimParam;
import utility.node.ERTriageNode;
import utility.node.ERTriageNodeManager;
import utility.sfmt.Rand;

public class ERWaitingRoom extends Agent
{
	private static final long serialVersionUID = -2326023198447134895L;

	private int aiPatientNurseLoc[];							// 患者と看護師の対応
	private ERPatientAgent erCurrentPatientAgent;				// 現在対応している患者
	private Rand rnd;											// 乱数クラス
	private ArrayList<ERNurseAgent> ArrayListNurseAgents;		// 待合室で見ている看護師エージェント
	private ArrayList<ERPatientAgent> ArrayListPatientAgents;	// 待合室で待っているエージェント
	private ArrayList<Integer> ArrayListNursePatientLoc;		// 看護師と患者の対応位置
	private double lfTotalTime;									// シミュレーション経過時間
	private int iNurseAgentNum;									// 所属している看護師の数
	private int iDisChargeNum;									// 退院数
	private double lfArrivalPatientPepole;						// 患者到達人数
	private Logger cWaitingRoomLog;								// 待合室ログ出力設定

	private ERTriageNodeManager erTriageNodeManager;
	private ERTriageNode erTriageNode;
	private int iJudgeUrgencyFlagMode = 1;						// 緊急度判定基準の判定モード(0:AIS値, 1：JTAS緊急度基準)
	private int iInverseSimFlag;

	// 描画関係
	private int iDrawX;
	private int iDrawY;
	private int iDrawZ;
	private int iDrawCenterX;
	private int iDrawCenterY;
	private int iDrawWidth;
	private int iDrawHeight;
	private int iDrawF;

	private Object csWaitingRoomCriticalSection;				// クリティカルセクション用

	private double lfLastBedTime;
	private double lfLongestTotalTime;

	private int iMonthCount = 0;								// 患者出現分布用１ヶ月換算
	private int iDayCount = 0;									// 患者出現分布用１日換算
	private int iYearCount = 0;									// 患者出現分布用1年換算

	private double lfOneWeekArrivalPatientPepole = 1.0;				// 患者の1日単位での分布
	private double lfOneMonthArrivalPatientPepole = 1.0;			// 患者の1ヶ月単位での分布
	private double lfOneYearArrivalPatientPepole = 1.0;				// 患者の1年単位での分布

	public ERWaitingRoom()
	{
		vInitialize();
	}

	public void vInitialize()
	{
		ArrayListNurseAgents		= new ArrayList<ERNurseAgent>();
		ArrayListPatientAgents		= new ArrayList<ERPatientAgent>();
		ArrayListNursePatientLoc	= new ArrayList<Integer>();
		erCurrentPatientAgent		= null;
		lfTotalTime					= 0.0;
		iNurseAgentNum				= 0;
		iDisChargeNum				= 0;
//		long seed;
//		seed = System.currentTimeMillis();
//		rnd = null;
//		rnd = new Sfmt( (int)seed );

		iInverseSimFlag = 0;

		iMonthCount = 0;								// 患者出現分布用１ヶ月換算
		iDayCount = 0;									// 患者出現分布用１日換算
		iYearCount = 0;									// 患者出現分布用1年換算

	}

	public void vInitialize( int iNurseAgentNumData )
	{
		int i;
		iNurseAgentNum = iNurseAgentNumData;
		ArrayListNurseAgents = new ArrayList<ERNurseAgent>();
		ArrayListNursePatientLoc = new ArrayList<Integer>();
		for( i = 0;i < iNurseAgentNum; i++ )
		{
			ArrayListNurseAgents.add( new ERNurseAgent() );
			ArrayListNursePatientLoc.add( new Integer(-1) );
		}
		ArrayListPatientAgents = new ArrayList<ERPatientAgent>();

		iInverseSimFlag = 0;
	}

	/**
	 * <PRE>
	 *    ファイルの読み込みを行います。
	 * </PRE>
	 * @param strDoctorAgentDirectory			医師エージェントの出力用ディレクトリパス
	 * @param strNurseAgentDirectory			看護師エージェントの出力用ディレクトリパス
	 * @param strClinicalEngineerAgentDirectory	医療技師エージェントの出力用ディレクトリパス
	 * @param iFileWriteMode					ファイル書き込みモード
	 * 											0 1ステップごとのデータを書き込み
	 * 											1 最初と最後各100ステップ程度のデータを書き込み
	 * @throws IOException						ファイル書き込みエラー
	 */
	public void vSetReadWriteFileForAgents( String strDoctorAgentDirectory, String strNurseAgentDirectory, String strClinicalEngineerAgentDirectory, int iFileWriteMode ) throws IOException
	{
		int i;

		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetReadWriteFile( iFileWriteMode );
		}
	}

	/**
	 * <PRE>
	 *    ファイルの読み込みを行います。
	 * </PRE>
	 * @param strPatientAgentStart	開始時に読み込む患者エージェント用出力ファイル名
	 * @param strPatientAgentEnd	終了時に読み込む患者エージェント用出力ファイル名
	 * @param iFileWriteMode		ファイル出力モード
	 * @throws IOException			ファイル出力例外エラー
	 */
	public void vSetReadWriteFileForAgents( String strPatientAgentStart, String strPatientAgentEnd, int iFileWriteMode ) throws IOException
	{
		ArrayListPatientAgents.get(ArrayListPatientAgents.size()-1).vSetReadWriteFile( iFileWriteMode );
	}

	/**
	 * <PRE>
	 *    終了処理を実行します。
	 * </PRE>
	 * @throws IOException	終了処理エラー
	 */
	public synchronized void vTerminate() throws IOException
	{
		int i;

		synchronized( csWaitingRoomCriticalSection )
		{
			// 患者エージェントの終了処理を行います。
			if( ArrayListPatientAgents != null )
			{
				for( i = ArrayListPatientAgents.size()-1; i >= 0; i-- )
				{
					if( ArrayListPatientAgents.get(i) != null )
					{
						ArrayListPatientAgents.get(i).vTerminate();
						this.getEngine().addExitAgent( ArrayListPatientAgents.get(i) );
						ArrayListPatientAgents.set( i, null );
						ArrayListPatientAgents.remove( i );
					}
				}
			}
			ArrayListPatientAgents = null;
			// 看護師エージェントの終了処理を行います。
			if( ArrayListNurseAgents != null )
			{
				for( i = ArrayListNurseAgents.size()-1; i >= 0; i-- )
				{
					if( ArrayListNurseAgents.get(i) != null )
					{
						ArrayListNurseAgents.get(i).vTerminate();
						this.getEngine().addExitAgent( ArrayListNurseAgents.get(i) );
						ArrayListNurseAgents.set( i, null );
						ArrayListNurseAgents.remove( i );
					}
				}
			}
			ArrayListNurseAgents = null;

			if( ArrayListNursePatientLoc != null )
			{
				// 看護師エージェントの終了処理を行います。
				for( i = ArrayListNursePatientLoc.size()-1; i >= 0; i-- )
				{
					if( ArrayListNursePatientLoc.get(i) != null )
					{
						ArrayListNursePatientLoc.set( i, null );
						ArrayListNursePatientLoc.remove( i );
					}
				}
				ArrayListNursePatientLoc = null;
			}
			cWaitingRoomLog = null;									// 待合室ログ出力設定

			// 乱数
			rnd = null;												// 乱数クラス

			// FUSEノード、リンク
			erTriageNodeManager = null;
			erTriageNode = null;
			lfTotalTime = 0.0;
			iDisChargeNum = 0;
		}
	}

	/**
	 * <PRE>
	 *   待合室の看護師エージェントを生成します。
	 * </PRE>
	 * @param iNurseAgentNumData 看護師エージェント数
	 * @author kobayashi
	 * @since 2015/08/06
	 */
	public void vCreateNurseAgents( int iNurseAgentNumData )
	{
		int i;
		if( ArrayListNurseAgents == null )
		{
			// 逆シミュレーションの場合に通ります。
			ArrayListNurseAgents		= new ArrayList<ERNurseAgent>();
			ArrayListNursePatientLoc	= new ArrayList<Integer>();		// 看護師と患者の対応位置
		}
		iNurseAgentNum = iNurseAgentNumData;
		for( i = 0;i < iNurseAgentNum; i++ )
		{
			ArrayListNurseAgents.add( new ERNurseAgent() );
			ArrayListNursePatientLoc.add( new Integer( -1 ) );
		}
	}

	/**
	 * <PRE>
	 *   患者エージェントオブジェクトを生成します。
	 * </PRE>
	 */
	public void vCreatePatientAgents()
	{
		ArrayListPatientAgents = new ArrayList<ERPatientAgent>();
	}

	/**
	 * <PRE>
	 *    待合室の看護師エージェントのパラメータを設定します。
	 * </PRE>
	 * @param aiNurseCategory				看護師のカテゴリー
	 * @param aiNurseTriageProtocol			トリアージプロトコル
	 * @param aiNurseTriageLevel			トリアージの緊急度レベル
	 * @param alfNurseTriageYearExperience	トリアージ経験年数
	 * @param alfNurseYearExperience		看護師経験年数
	 * @param alfNurseConExperience			看護師経験年数重み
	 * @param alfExperienceRate1			経験年数パラメータその１
	 * @param alfExperienceRate2			経験年数パラメータその２
	 * @param alfConExperienceAIS			経験年数パラメータ重み（重症度）
	 * @param alfExperienceRateAIS1			経験年数パラメータその１（重症度）
	 * @param alfExperienceRateAIS2			経験年数パラメータその２（重症度）
	 * @param alfNurseConTired1				疲労パラメータ１
	 * @param alfNurseConTired2				疲労パラメータ２
	 * @param alfNurseConTired3				疲労パラメータ３
	 * @param alfNurseConTired4				疲労パラメータ４
	 * @param alfNurseTiredRate				疲労度の割合
	 * @param alfNurseAssociationRate		連携度
	 * @param alfObservationTime			定期観察時間
	 * @param alfObservationProcessTime		観察プロセス時間
	 * @param alfTriageTime					トリアージ時間
	 * @param aiDepartment					所属部門
	 * @param aiRoomNumber					所属部屋番号
	 * @author kobayashi
	 * @since 2015/08/10
	 */
	public void vSetNurseAgentParameter( int[] aiNurseCategory,
			int[] aiNurseTriageProtocol,
			int[] aiNurseTriageLevel,
			double[] alfNurseTriageYearExperience,
			double[] alfNurseYearExperience,
			double[] alfNurseConExperience,
			double[] alfExperienceRate1,
			double[] alfExperienceRate2,
			double[] alfConExperienceAIS,
			double[] alfExperienceRateAIS1,
			double[] alfExperienceRateAIS2,
			double[] alfNurseConTired1,
			double[] alfNurseConTired2,
			double[] alfNurseConTired3,
			double[] alfNurseConTired4,
			double[] alfNurseTiredRate,
			double[] alfNurseAssociationRate,
			double[] alfObservationTime,
			double[] alfObservationProcessTime,
			double[] alfTriageTime,
		    int[] aiDepartment,
		    int[] aiRoomNumber )
	{
		int i;

		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetNurseCategory( aiNurseCategory[i] );
			ArrayListNurseAgents.get(i).vSetTriageProtocol( aiNurseTriageProtocol[i] );
			ArrayListNurseAgents.get(i).vSetTriageProtocolLevel( aiNurseTriageLevel[i] );
			ArrayListNurseAgents.get(i).vSetTriageYearExperience( alfNurseTriageYearExperience[i] );
			ArrayListNurseAgents.get(i).vSetYearExperience( alfNurseYearExperience[i] );
			ArrayListNurseAgents.get(i).vSetConExperience( alfNurseConExperience[i] );
			ArrayListNurseAgents.get(i).vSetConTired1( alfNurseConTired1[i] );
			ArrayListNurseAgents.get(i).vSetConTired2( alfNurseConTired2[i] );
			ArrayListNurseAgents.get(i).vSetConTired3( alfNurseConTired3[i] );
			ArrayListNurseAgents.get(i).vSetConTired4( alfNurseConTired4[i] );
			ArrayListNurseAgents.get(i).vSetTiredRate( alfNurseTiredRate[i] );
			ArrayListNurseAgents.get(i).vSetAssociationRate( alfNurseAssociationRate[i] );
			ArrayListNurseAgents.get(i).vSetObservationTime( alfObservationTime[i] );
			ArrayListNurseAgents.get(i).vSetObservationProcessTime( alfObservationProcessTime[i] );
			ArrayListNurseAgents.get(i).vSetTriageProtocolTime( alfTriageTime[i] );
			ArrayListNurseAgents.get(i).vSetNurseDepartment( aiDepartment[i] );
			ArrayListNurseAgents.get(i).vSetExperienceRate1( alfExperienceRate1[i] );
			ArrayListNurseAgents.get(i).vSetExperienceRate2( alfExperienceRate2[i] );
			ArrayListNurseAgents.get(i).vSetConExperienceAIS( alfConExperienceAIS[i] );
			ArrayListNurseAgents.get(i).vSetExperienceRateAIS1( alfExperienceRateAIS1[i] );
			ArrayListNurseAgents.get(i).vSetExperienceRateAIS2( alfExperienceRateAIS2[i] );
			ArrayListNurseAgents.get(i).vSetRoomNumber( aiRoomNumber[i] );
		}
	}

	/**
	 * <PRE>
	 *    FUSEエンジンにエージェントを登録します。
	 * </PRE>
	 * @param engine シミュレーションエンジン
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	public void vSetSimulationEngine( SimulationEngine engine )
	{
		engine.addAgent(this);
	}


	/**
	 * <PRE>
	 *    待合室のプロセスを実行します。
	 *    患者がいない場合は何もしないで終了します。
	 * </PRE>
	 * @param ArrayListConsultationRooms			診察室エージェント
	 * @param ArrayListObservationRooms				観察室エージェント
	 * @param ArrayListSevereInjuryObservationRooms	重症観察室エージェント
	 * @param ArrayListEmergencyRooms				初療室エージェント
	 * @param ArrayListExaminationXRayRooms			X線室エージェント
	 * @param ArrayListExaminationCTRooms			CT室エージェント
	 * @param ArrayListExaminationMRIRooms			MRI室エージェント
	 * @param ArrayListExaminationAngiographyRooms	血管造影室エージェント
	 * @param ArrayListExaminationFastRooms			Fast室エージェント
	 * @param erOutside								病院外エージェント
	 * @return										常に0を返却
	 * @throws ERNurseAgentException				看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	public int iImplementWaitingRoom( ArrayList<ERConsultationRoom> ArrayListConsultationRooms,
									   ArrayList<ERObservationRoom> ArrayListObservationRooms,
									   ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms,
									   ArrayList<EREmergencyRoom> ArrayListEmergencyRooms,
									   ArrayList<ERExaminationXRayRoom> ArrayListExaminationXRayRooms,
									   ArrayList<ERExaminationCTRoom> ArrayListExaminationCTRooms,
									   ArrayList<ERExaminationMRIRoom> ArrayListExaminationMRIRooms,
									   ArrayList<ERExaminationAngiographyRoom> ArrayListExaminationAngiographyRooms,
									   ArrayList<ERExaminationFastRoom> ArrayListExaminationFastRooms,
									   EROutside erOutside ) throws ERNurseAgentException
	{
		int i,j;
		int iEnableNurse;
		int iRes = 0;
		int iLoc = 0;
		ERNurseAgent erNurseAgent = null;

		iEnableNurse = -1;
		synchronized( csWaitingRoomCriticalSection )
		{
		// 退院可能なエージェントのチェックを行います。
			if( ArrayListPatientAgents.isEmpty() == false )
			{
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					// かなりの患者が亡くなられている場合のエラー対策。
					if( ArrayListPatientAgents.size() <= i )	continue;
					if( ArrayListPatientAgents.get(i) == null )	continue;
					// 退院可能な患者エージェントがいる場合は患者エージェントを削除します
					if( ArrayListPatientAgents.get(i).iGetDisChargeFlag() == 1 )
					{
						ERPatientAgent erPAgent = ArrayListPatientAgents.get(i);
						if( erPAgent.isMoveWaitingTime() == false )
						{
							// 移動時間がまだ終了していないので、移動を実施しません。
				 			cWaitingRoomLog.info(erPAgent.getId() + "," + "待合室移動時間：" + erPAgent.lfGetMoveWaitingTime() );
				 			return 0;
				 		}
						// 部屋移動が終了したのでフラグOFFに処置中とします。
				 		erPAgent.vSetMoveRoomFlag( 0 );

						for( j = 0;j < ArrayListNursePatientLoc.size(); j++ )
						{
							if( ArrayListNursePatientLoc.get(j)  == i )
							{
								cWaitingRoomLog.info(ArrayListPatientAgents.get(i).getId() + "," + "WaitRoom通ったよ～");
								ArrayListNursePatientLoc.set( j, -1 );
								iLoc = i;
							}
						}
						// なくなられたエージェントが配列上いた位置を削除するため、それ以降のデータがすべて1繰り下がるので、
						// それに対応する。そうしないと配列サイズを超えて参照したエラーが発生します。
						cWaitingRoomLog.info( ArrayListPatientAgents.get(i).getId() + ","  + "待合室：退院しました！。");
						for( j = 0;j < ArrayListNursePatientLoc.size(); j++ )
						{
							if( iLoc < ArrayListNursePatientLoc.get(j) )
							{
								ArrayListNursePatientLoc.set( j, ArrayListNursePatientLoc.get(j)-1 );
							}
						}
//						ArrayListPatientAgents.get(i).getEngine().addExitAgent(ArrayListPatientAgents.get(i));
//						// 退院数をカウントします。
//						iDisChargeNum++;
//						// 登録されているエージェントで離脱したエージェントがいるかどうかを判定します。
//						if( ArrayListPatientAgents.get(i).iGetDisChargeFlag() == 1 )
//						{
//							try{
//								//いる場合は、ファイルに書き出しを実行します。
//								ArrayListPatientAgents.get(i).vFlushFile( 0 );
//							}
//							catch( IOException ioe ){
//							}
//						}
						vJudgeMoveOutside( erOutside, erNurseAgent, ArrayListPatientAgents.get(i), iLoc );
						vRemovePatientAgent( erPAgent, iLoc );
//						ArrayListPatientAgents.set(i, null);
//						ArrayListPatientAgents.remove(i);
					}
				}
			}
		// 看護師に患者を割り当てます。
			for( i= 0 ;i < ArrayListNurseAgents.size(); i++ )
			{
				// 看護師が対応していないかどうかを取得します。
	//			if( ArrayListNurseAgents.get(i).iGetAttending() == 0 )
	//			{
					iEnableNurse = i;
					// 観察を受けていない患者がいるかどうかを判定します。
					for( j = 0;j < ArrayListPatientAgents.size(); j++ )
					{
						// かなりの患者が亡くなられている場合のエラー対策。
						if( ArrayListPatientAgents.size() <= j )						continue;
						if( ArrayListPatientAgents.get(j) == null )						continue;
						// 患者が到達していない場合は何もしません。
						if( ArrayListPatientAgents.get(j).isArraivalTime() == false )	continue;
						// 到達している場合に対応しているか否かを判定します。
						if( ArrayListPatientAgents.get(j).iGetNurseAttended() == 0 )
						{
							ArrayListNursePatientLoc.set( iEnableNurse, j );
							ArrayListPatientAgents.get(j).vSetNurseAttended( 1 );
							ArrayListPatientAgents.get(j).vSetNurseAgent(ArrayListNurseAgents.get(i).getId());
							break;
						}
					}
	//				break;
	//			}
			}
			// 登録されている患者がいる場合は各看護師の観察を実行します。
			// いない場合は何もせずに終了します。
			if( ArrayListPatientAgents.isEmpty() == false )
			{
	//			for( i= 0 ;i < ArrayListNurseAgents.size(); i++ )
				for( i = 0 ;i < ArrayListPatientAgents.size(); i++ )
				{
	//				if( ArrayListNursePatientLoc.get(i) >= 0 )
	//				{
	//					// 観察を開始します。
	//					vImplementObservation( ArrayListConsultationRooms, ArrayListObservationRooms, ArrayListSevereInjuryObservationRooms, ArrayListEmergencyRooms, ArrayListNurseAgents.get( i ), ArrayListPatientAgents.get( ArrayListNursePatientLoc.get( i ) ), i );
	//				}
					// かなりの患者が亡くなられている場合のエラー対策。
					if( ArrayListPatientAgents.size() <= i )		continue;
					if( ArrayListPatientAgents.get(i) == null )		continue;
					// 看護師に対応されている場合は看護師による観察を実行します。
					if( ArrayListPatientAgents.get(i).iGetNurseAttended() != 0 )
					{
						// 該当看護師を探索します。
						for( j = 0;j < ArrayListNurseAgents.size(); j++ )
						{
							if( ArrayListPatientAgents.get(i).iGetNurseAgent() == ArrayListNurseAgents.get(j).getId() )
							{
								erNurseAgent = ArrayListNurseAgents.get(j);
								break;
							}
						}
						// 本来はないがもしも該当者がいない場合は仮に再度割り当て作業を実施します。
						if( j == ArrayListNurseAgents.size() )
						{
							for( j = 0;j < ArrayListNurseAgents.size(); j++ )
							{
								// 患者が到達していない場合は何もしません。
								if( ArrayListPatientAgents.get(i).isArraivalTime() == false )
								{
									continue;
								}
								// 到達している場合に対応しているか否かを判定します。
								ArrayListNursePatientLoc.set( iEnableNurse, j );
								ArrayListPatientAgents.get(i).vSetNurseAttended( 1 );
								ArrayListPatientAgents.get(i).vSetNurseAgent(ArrayListNurseAgents.get(j).getId());
								erNurseAgent = ArrayListNurseAgents.get(j);
								break;
							}
						}
						// 観察を開始します。
						vImplementObservation( ArrayListConsultationRooms, ArrayListObservationRooms,
											   ArrayListSevereInjuryObservationRooms,
											   ArrayListEmergencyRooms,
											   ArrayListExaminationXRayRooms,
											   ArrayListExaminationCTRooms,
											   ArrayListExaminationMRIRooms,
											   ArrayListExaminationAngiographyRooms,
											   ArrayListExaminationFastRooms,
											   erNurseAgent, ArrayListPatientAgents.get( i ), j );
					}
					// 割り当てられていないが、待合室に待っている患者さんがいる場合この部分を処理を実行します。
					else
					{
						vCheckMoveRoom( ArrayListConsultationRooms, ArrayListObservationRooms,
								   		ArrayListSevereInjuryObservationRooms,
								   		ArrayListEmergencyRooms,
								   		ArrayListExaminationXRayRooms,
								   		ArrayListExaminationCTRooms,
								   		ArrayListExaminationMRIRooms,
								   		ArrayListExaminationAngiographyRooms,
								   		ArrayListExaminationFastRooms,
								   		ArrayListPatientAgents.get( i ) );
					}
				}
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					if( ArrayListPatientAgents.get(i) != null )
					{
						// 患者が到達したが、まだ看護師の緊急度判定を受けていない場合
						if( ArrayListPatientAgents.get(i).lfGetTimeCourse() > 0.0 && ArrayListPatientAgents.get(i).lfGetMoveWaitingTime() > ArrayListPatientAgents.get(i).lfGetMoveTime() )
						{
							// 移動はしていないので、待機中とします。
							ArrayListPatientAgents.get(i).vSetMoveRoomFlag(0);
							ArrayListPatientAgents.get(i).vSetMoveWaitFlag(0);
						}
					}
				}
			}
		}
//		cWaitingRoomLog.info("ArrayListPatientAgent size:" + ArrayListPatientAgents.size() );
		return iRes;
	}

	/**
	 * <PRE>
	 *    歩いて患者が到達したときの発生分布を表します。
	 * </PRE>
	 * @param engine シミュレーションエンジン
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	public void vArrivalAlonePatient( SimulationEngine engine )
	{
		double lfX,lfY,lfZ;
//		erCurrentPatientAgent = new ERPatientAgent();
		ArrayListPatientAgents.add( new ERPatientAgent() );
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetRandom( rnd );
		// ランダムに患者の容体を割り当てます。
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetRandom();
//		engine.addAgent(ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1));
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetSimulationEngine(engine);
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetMoveRoomFlag( 1 );
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetMoveWaitingTime( 181 );

		// 患者エージェントの位置を設定します。
		lfX = this.getPosition().getX()+10*(2*rnd.NextUnif()-1);
		lfY = this.getPosition().getY()+10*(2*rnd.NextUnif()-1);
		lfZ = this.getPosition().getZ()+10*(2*rnd.NextUnif()-1);
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1).setPosition( lfX, lfY, lfZ );

		return ;
	}

	/**
	 * <PRE>
	 *    患者を生成します。
	 *    通常は独歩来院用発生分布、救急車来院用発生分布です。
	 *    災害時は災害時用の発生分布を使用します。
	 * </PRE>
	 * @param lfTime			経過時間[時間]
	 * @param lfStepTime		時間間隔[時間]
	 * @param engine			シミュレーションエンジン
	 * @param iRandomMode		乱数モード
	 * @param iInverseSimMode	逆シミュレーションモード
	 * @param iFileWriteMode	ファイル書き込みモード
	 * @param iDisasterFlag		災害モード
	 * @param initSimParam		初期設定パラメータ（合わせ込み用）
	 * @throws IOException		例外
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	public void vArrivalPatient( double lfTime, double lfStepTime, SimulationEngine engine, int iRandomMode, int iInverseSimMode, int iFileWriteMode, int iDisasterFlag, InitSimParam initSimParam ) throws IOException
	{
		double lfProb1 = 0.0;
		double lfProb2 = 0.0;
		double lfRand1,lfRand2;
		double lfX,lfY,lfZ;
		double lfOneDay = 24.0;
		double lfPrevOneDay = 24.0;
		double lfMonth = 24.0*31.0;
		double lfPrevOneMonth = 24.0*31.0;
		double lfYear = 365.0*24.0;
		double lfPrevOneYear = 24.0*365.0;
		if( iDisasterFlag == 0 )
		{
			// 1年単位での分布を計算します。
			if( (lfTime % lfYear) < lfStepTime )
			{
				vCalcArrivalDensityWalkOneYear( (double)iYearCount );
				vCalcArrivalDensityAmbulanceOneYear( (double)iYearCount );
				// 1ヶ月終了したら0に戻す。
				if( iYearCount == 365 ) iYearCount = 0;
				iYearCount++;
			}
			// 1ヶ月単位での分布を計算します。
			if( (lfTime % lfMonth) < lfStepTime )
			{
				vCalcArrivalDensityWalkOneMonth( (double)iMonthCount );
				vCalcArrivalDensityAmbulanceOneMonth( (double)iMonthCount );
				// 1ヶ月終了したら0に戻す。
				if( iMonthCount == 31 ) iMonthCount = 0;
				iMonthCount++;
			}
			// １日単位の分布を計算します。
			if( (lfTime % lfOneDay) < lfStepTime )
			{
				vCalcArrivalDensityWalkOneWeek( (double)iDayCount );
				vCalcArrivalDensityAmbulanceOneWeek( (double)iDayCount );
				// 1週間経過したら終了したら0に戻す。
				if( iDayCount == 7 ) iDayCount = 0;
				iDayCount++;
			}
			// 1秒単位の分布の計算をします。
			lfProb1 = lfCalcArrivalDensityWalk( lfTime );
			lfProb2 = lfCalcArrivalDensityAmbulance( lfTime );
			lfRand1 = rnd.NextUnif();
			lfRand2 = rnd.NextUnif();
		}
		else
		{
			// 1ヶ月単位での分布を計算します。
			if( (lfTime % lfMonth) < lfStepTime )
			{
				vCalcArrivalDensityDisasterOneMonth( (double)iMonthCount );
				// 1ヶ月終了したら0に戻す。
				if( iMonthCount == 31 ) iMonthCount = 0;
				iMonthCount++;
			}
			// １日単位の分布を計算します。
			if( (lfTime % lfOneDay) < lfStepTime )
			{
				vCalcArrivalDensityDisasterOneWeek( (double)iDayCount );
				// 1週間経過したら終了したら0に戻す。
				if( iDayCount == 7 ) iDayCount = 0;
				iDayCount++;
			}
			lfPrevOneDay = lfTime % lfOneDay;
			// 1秒単位の分布の計算をします。
			lfProb1 = lfCalcArrivalDensityDisaster( lfTime );
			lfRand1 = rnd.NextUnif();
			lfRand2 = rnd.NextUnif();
		}
		// 患者エージェント生成
		engine.pause();
		if( lfProb1 > lfRand1 )
		{
//			erCurrentPatientAgent = new ERPatientAgent();
			ArrayListPatientAgents.add( new ERPatientAgent() );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetInitParam( initSimParam );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetRandom( rnd );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetPatientRandomMode( iRandomMode );
			// ランダムに患者の容体を割り当てます。
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1).vSetRandom();
//			engine.addAgent(ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ));
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1).vSetSimulationEngine(engine);
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetMoveRoomFlag( 1 );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetMoveWaitingTime( 181 );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetLocation( 9 );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetArraivalTime( lfTime );
			// 逆シミュレーションモードでなければ以下を実行します。
			iInverseSimFlag = iInverseSimMode;
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetInverseSimMode( iInverseSimFlag );
			if( iInverseSimFlag == 0 || iInverseSimFlag == 1 )
			{
				ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetReadWriteFile( iFileWriteMode );
			}
			// 患者エージェントの位置を設定します。
			lfX = this.getPosition().getX()+10*(2*rnd.NextUnif()-1);
			lfY = this.getPosition().getY()+10*(2*rnd.NextUnif()-1);
			lfZ = this.getPosition().getZ()+10*(2*rnd.NextUnif()-1);
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).setPosition( lfX, lfY, lfZ );
			return;
		}
		// 患者エージェント生成
		if( lfProb2 > lfRand2 )
		{
//			erCurrentPatientAgent = new ERPatientAgent();
			ArrayListPatientAgents.add( new ERPatientAgent() );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetInitParam( initSimParam );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetRandom( rnd );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetPatientRandomMode( iRandomMode );
			// ランダムに患者の容体を割り当てます。
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1).vSetRandom();
//			engine.addAgent(ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ));
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1).vSetSimulationEngine(engine);
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetMoveRoomFlag( 1 );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetMoveWaitingTime( 181 );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetLocation( 9 );
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetArraivalTime( lfTime );
			// 逆シミュレーションモードでなければ以下を実行します。
			iInverseSimFlag = iInverseSimMode;
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetInverseSimMode( iInverseSimFlag );
			if( iInverseSimFlag == 0 || iInverseSimFlag == 1 )
			{
				ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetReadWriteFile( iFileWriteMode );
			}
			// 患者エージェントの位置を設定します。
			lfX = this.getPosition().getX()+10*(2*rnd.NextUnif()-1);
			lfY = this.getPosition().getY()+10*(2*rnd.NextUnif()-1);
			lfZ = this.getPosition().getZ()+10*(2*rnd.NextUnif()-1);
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).setPosition( lfX, lfY, lfZ );
		}
		engine.resume();
		return;
	}

	/**
	 * <PRE>
	 *   観察プロセスを実行します。
	 * </PRE>
	 * @param ArrayListConsultationRooms			全診察室
	 * @param ArrayListObservationRooms				全観察室
	 * @param ArrayListSevereInjuryObservationRooms	全重症観察室
	 * @param ArrayListEmergencyRooms				全初療室
	 * @param ArrayListExaminationXRayRooms			全X線室
	 * @param ArrayListExaminationCTRooms			全CT室
	 * @param ArrayListExaminationMRIRooms			全MRI室
	 * @param ArrayListExaminationAngiographyRooms	全血管造影室
	 * @param ArrayListExaminationFastRooms			全Fast室
	 * @param erNurseAgent							対応している看護師エージェント
	 * @param erPAgent								観察を受けている患者エージェント
	 * @param iLoc									看護師がどの患者を対応していたのか
	 * @throws ERNurseAgentException				看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	public void vImplementObservation( ArrayList<ERConsultationRoom> ArrayListConsultationRooms,
										ArrayList<ERObservationRoom> ArrayListObservationRooms,
										ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms,
										ArrayList<EREmergencyRoom> ArrayListEmergencyRooms,
										ArrayList<ERExaminationXRayRoom> ArrayListExaminationXRayRooms,
										ArrayList<ERExaminationCTRoom> ArrayListExaminationCTRooms,
										ArrayList<ERExaminationMRIRoom> ArrayListExaminationMRIRooms,
										ArrayList<ERExaminationAngiographyRoom> ArrayListExaminationAngiographyRooms,
										ArrayList<ERExaminationFastRoom> ArrayListExaminationFastRooms,
										ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc ) throws ERNurseAgentException
	{
		int iFlag = 0;
		int iPrevLocation = 0;
		// 患者に診察を実施する場合
		synchronized( csWaitingRoomCriticalSection )
		{
			if( erPAgent.isMoveWaitingTime() == false )
			{
	//			erPAgent.vSetMoveRoomFlag( 1 );
				erNurseAgent.vSetPatientMoveWaitFlag( 1 );
				// 移動時間がまだ終了していないので、移動を実施しません。
	 			cWaitingRoomLog.info(erPAgent.getId() + "," + "待合室移動時間：" + erPAgent.lfGetMoveWaitingTime() );
	 			return;
	 		}
			// 部屋移動が終了したのでフラグOFFに処置中とします。
	 		erPAgent.vSetMoveRoomFlag( 0 );
			erNurseAgent.vSetPatientMoveWaitFlag( 0 );

			// その患者を対応している看護師エージェントのIDを設定します。
			erPAgent.vSetNurseAgent(erNurseAgent.getId());
			// 現在いる位置を取得します。
			iPrevLocation = erPAgent.iGetLocation();

			if( erPAgent.iGetConsultationRoomWaitFlag() == 1 )
			{
				// 診察室へ移動できる場合は移動します。
				if( erPAgent.erGetConsultationDoctorAgent() != null )
					cWaitingRoomLog.info(erPAgent.getId() + "," + erPAgent.erGetConsultationDoctorAgent().getId() + "," + erPAgent.erGetConsultationDoctorAgent().iGetAttending() + "," + "優先的に診察室へ移動開始判定, 待合室" );
				else
					cWaitingRoomLog.info(erPAgent.getId() + "," + "優先的に診察室へ移動開始判定, 待合室" );
				vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
				if( erPAgent.iGetConsultationRoomWaitFlag() == 0 ) return;
				else
				{
					// この場合、元のエージェントは別の部屋に移動しているので、移動します。
					if( erPAgent.iGetLocation() != iPrevLocation ) return ;
					// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
				}
				iFlag = 1;
			}
			// 患者に初療室で処置を実施する場合
			if( erPAgent.iGetEmergencyRoomWaitFlag() == 1 )
			{
				// 初療室へ移動できる場合は移動します。
	 			cWaitingRoomLog.info(erPAgent.getId() + "," + "優先的に初療室へ移動開始判定, 待合室" );
				vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
				// 移動可能なので移動します。
				if( erPAgent.iGetEmergencyRoomWaitFlag() == 0 ) return;
				// 移動していないと思われる場合。
				else
				{
					// この場合、元のエージェントは別の部屋に移動しているので、移動します。
					if( erPAgent.iGetLocation() != iPrevLocation ) return ;
					// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
				}
				iFlag = 1;
			}
			if( erPAgent.iGetExaminationXRayRoomWaitFlag() == 1 )
			{
				// X線室へ移動できる場合は移動します。
	 			cWaitingRoomLog.info(erPAgent.getId() + "," + erPAgent.erGetConsultationDoctorAgent().getId() + "," + erPAgent.erGetConsultationDoctorAgent().iGetAttending() + "," + "優先的にX線室へ移動開始判定, 待合室" );
				vJudgeMoveExaminationXRayRoom( ArrayListExaminationXRayRooms, this, erPAgent.erGetConsultationDoctorAgent(), erPAgent, iLoc );
				// 移動可能なので移動します。
				if( erPAgent.iGetExaminationXRayRoomWaitFlag() == 0 ) return;
				// 移動していないと思われる場合。
				else
				{
					// この場合、元のエージェントは別の部屋に移動しているので、移動します。
					if( erPAgent.iGetLocation() != iPrevLocation ) return ;
					// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
				}
				iFlag = 1;
			}
			if( erPAgent.iGetExaminationCTRoomWaitFlag() == 1 )
			{
				// CT室へ移動できる場合は移動します。
	 			cWaitingRoomLog.info(erPAgent.getId() + "," + erPAgent.erGetConsultationDoctorAgent().getId() + "," + erPAgent.erGetConsultationDoctorAgent().iGetAttending() + "," + "優先的にCT室へ移動開始判定, 待合室" );
				vJudgeMoveExaminationCTRoom( ArrayListExaminationCTRooms, this, erPAgent.erGetConsultationDoctorAgent(), erPAgent, iLoc );
				// 移動可能なので移動します。
				if( erPAgent.iGetExaminationCTRoomWaitFlag() == 0 ) return;
				// 移動していないと思われる場合。
				else
				{
					// この場合、元のエージェントは別の部屋に移動しているので、移動します。
					if( erPAgent.iGetLocation() != iPrevLocation ) return ;
					// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
				}
				iFlag = 1;
			}
			if( erPAgent.iGetExaminationMRIRoomWaitFlag() == 1 )
			{
				// MRI室へ移動できる場合は移動します。
	 			cWaitingRoomLog.info(erPAgent.getId() + "," + erPAgent.erGetConsultationDoctorAgent().getId() + "," + erPAgent.erGetConsultationDoctorAgent().iGetAttending() + "," + "優先的にMRI室へ移動開始判定, 待合室" );
				vJudgeMoveExaminationMRIRoom( ArrayListExaminationMRIRooms, this, erPAgent.erGetConsultationDoctorAgent(), erPAgent, iLoc );
				// 移動可能なので移動します。
				if( erPAgent.iGetExaminationMRIRoomWaitFlag() == 0 ) return;
				// 移動していないと思われる場合。
				else
				{
					// この場合、元のエージェントは別の部屋に移動しているので、移動します。
					if( erPAgent.iGetLocation() != iPrevLocation ) return ;
					// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
				}
				iFlag = 1;
			}
			if( erPAgent.iGetExaminationAngiographyRoomWaitFlag() == 1 )
			{
				// 血管造影室へ移動できる場合は移動します。
	 			cWaitingRoomLog.info(erPAgent.getId() + "," + erPAgent.erGetConsultationDoctorAgent().getId() + "," + erPAgent.erGetConsultationDoctorAgent().iGetAttending() + "," + "優先的に血管造影室へ移動開始判定, 待合室" );
				vJudgeMoveExaminationAngiographyRoom( ArrayListExaminationAngiographyRooms, this, erPAgent.erGetConsultationDoctorAgent(), erPAgent, iLoc );
				// 移動可能なので移動します。
				if( erPAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 ) return;
				// 移動していないと思われる場合。
				else
				{
					// この場合、元のエージェントは別の部屋に移動しているので、移動します。
					if( erPAgent.iGetLocation() != iPrevLocation ) return ;
					// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
				}
				iFlag = 1;
			}
			if( erPAgent.iGetExaminationFastRoomWaitFlag() == 1 )
			{
				// 超音波室へ移動できる場合は移動します。
	 			cWaitingRoomLog.info(erPAgent.getId() + "," + erPAgent.erGetConsultationDoctorAgent().getId() + "," + erPAgent.erGetConsultationDoctorAgent().iGetAttending() + "," + "優先的にFast室へ移動開始判定, 待合室" );
				vJudgeMoveExaminationFastRoom( ArrayListExaminationFastRooms, this, erPAgent.erGetConsultationDoctorAgent(), erPAgent, iLoc );
				// 移動可能なので移動します。
				if( erPAgent.iGetExaminationFastRoomWaitFlag() == 0 ) return;
				// 移動していないと思われる場合。
				else
				{
					// この場合、元のエージェントは別の部屋に移動しているので、移動します。
					if( erPAgent.iGetLocation() != iPrevLocation ) return ;
					// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
				}
				iFlag = 1;
			}
			if( iFlag == 0 )
			{
				// 患者に診察を実施しない場合
				// 看護師エージェントの対応を実施します。
				erNurseAgent.vSetAttending( 1 );

		 		// 観察プロセスを実行します。
				if( erPAgent.iGetObservedFlag() == 0 )
				{
					cWaitingRoomLog.info(erPAgent.getId() + "," + "看護師観察開始(待合室)：" + erNurseAgent.lfGetObservationTime() + "," + erNurseAgent.lfGetCurrentPassOverTime());
					// 入ってきた患者に対して観察プロセス（トリアージプロセス）を実行します。
					vObservationProcess( ArrayListConsultationRooms, ArrayListObservationRooms, ArrayListSevereInjuryObservationRooms, ArrayListEmergencyRooms, erNurseAgent, erPAgent, iLoc );
				}
				else
				{
					// 2回目以降は定期観察時間及び定期トリアージ時間に従って観察プロセスを実行します。
					cWaitingRoomLog.info(erPAgent.getId() + "," + "看護師再観察開始(待合室)：" + erNurseAgent.lfGetObservationTime() + "," + erNurseAgent.lfGetCurrentPassOverTime());
					vReObservationProcess( ArrayListConsultationRooms, ArrayListObservationRooms, ArrayListSevereInjuryObservationRooms, ArrayListEmergencyRooms, erNurseAgent, erPAgent, iLoc );
				}
			}
		}
	}


	/**
	 * <PRE>
	 *   病室移動のため一時的に来た患者でなおかつ、待合室で看護師にまだトリアージを受けていない場合に
	 *   移動チェックを受けていないためこの処理を動作させます。
	 * </PRE>
	 *
	 * @param ArrayListConsultationRooms				全診察室
	 * @param ArrayListObservationRooms					全観察室
	 * @param ArrayListSevereInjuryObservationRooms		全重観察室
	 * @param ArrayListEmergencyRooms					全初療室
	 * @param ArrayListExaminationXRayRooms				全X線室
	 * @param ArrayListExaminationCTRooms				全CT室
	 * @param ArrayListExaminationMRIRooms				全MRI室
	 * @param ArrayListExaminationAngiographyRooms		血管造影室
	 * @param ArrayListExaminationFastRooms				全Fast室
	 * @param erPAgent									対象とする患者エージェント
	 * @throws ERNurseAgentException					看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	public void vCheckMoveRoom( ArrayList<ERConsultationRoom> ArrayListConsultationRooms,
								ArrayList<ERObservationRoom> ArrayListObservationRooms,
								ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms,
								ArrayList<EREmergencyRoom> ArrayListEmergencyRooms,
								ArrayList<ERExaminationXRayRoom> ArrayListExaminationXRayRooms,
								ArrayList<ERExaminationCTRoom> ArrayListExaminationCTRooms,
								ArrayList<ERExaminationMRIRoom> ArrayListExaminationMRIRooms,
								ArrayList<ERExaminationAngiographyRoom> ArrayListExaminationAngiographyRooms,
								ArrayList<ERExaminationFastRoom> ArrayListExaminationFastRooms,
								ERPatientAgent erPAgent ) throws ERNurseAgentException
	{
		int iPrevLocation = 0;
		int iFlag = 0;

		// 患者に診察を実施する場合
		if( erPAgent.isMoveWaitingTime() == false )
		{
//			erPAgent.vSetMoveRoomFlag( 1 );
//			erNurseAgent.vSetPatientMoveWaitFlag( 1 );
			// 移動時間がまだ終了していないので、移動を実施しません。
			cWaitingRoomLog.info(erPAgent.getId() + "," + "待合室移動時間：" + erPAgent.lfGetMoveWaitingTime() );
			return;
		}
		// 部屋移動が終了したのでフラグOFFに処置中とします。
		erPAgent.vSetMoveRoomFlag( 0 );
//		erNurseAgent.vSetPatientMoveWaitFlag( 0 );

		// その患者を対応している看護師エージェントのIDを設定します。
//		erPAgent.vSetNurseAgent(erNurseAgent.getId());
		// 現在いる位置を取得します。
		iPrevLocation = erPAgent.iGetLocation();

		if( erPAgent.iGetConsultationRoomWaitFlag() == 1 )
		{
			// 診察室へ移動できる場合は移動します。
			vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, null, erPAgent, -2 );
			if( erPAgent.iGetConsultationRoomWaitFlag() == 0 ) return;
			else
			{
				// この場合、元のエージェントは別の部屋に移動しているので、移動します。
				if( erPAgent.iGetLocation() != iPrevLocation ) return ;
				// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
			}
		}
		// 患者に初療室で処置を実施する場合
		if( erPAgent.iGetEmergencyRoomWaitFlag() == 1 )
		{
			// 初療室へ移動できる場合は移動します。
			vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, null, erPAgent, -2 );
			// 移動可能なので移動します。
			if( erPAgent.iGetEmergencyRoomWaitFlag() == 0 ) return;
			// 移動していないと思われる場合。
			else
			{
				// この場合、元のエージェントは別の部屋に移動しているので、移動します。
				if( erPAgent.iGetLocation() != iPrevLocation ) return ;
				// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
			}
		}
		if( erPAgent.iGetExaminationXRayRoomWaitFlag() == 1 )
		{
			// X線室へ移動できる場合は移動します。
			vJudgeMoveExaminationXRayRoom( ArrayListExaminationXRayRooms, this, erPAgent.erGetConsultationDoctorAgent(), erPAgent, -2 );
			// 移動可能なので移動します。
			if( erPAgent.iGetExaminationXRayRoomWaitFlag() == 0 ) return;
			// 移動していないと思われる場合。
			else
			{
				// この場合、元のエージェントは別の部屋に移動しているので、移動します。
				if( erPAgent.iGetLocation() != iPrevLocation ) return ;
				// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
			}
		}
		if( erPAgent.iGetExaminationCTRoomWaitFlag() == 1 )
		{
			// CT室へ移動できる場合は移動します。
			vJudgeMoveExaminationCTRoom( ArrayListExaminationCTRooms, this, erPAgent.erGetConsultationDoctorAgent(), erPAgent, -2 );
			// 移動可能なので移動します。
			if( erPAgent.iGetExaminationCTRoomWaitFlag() == 0 ) return;
			// 移動していないと思われる場合。
			else
			{
				// この場合、元のエージェントは別の部屋に移動しているので、移動します。
				if( erPAgent.iGetLocation() != iPrevLocation ) return ;
				// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
			}
		}
		if( erPAgent.iGetExaminationMRIRoomWaitFlag() == 1 )
		{
			// MRI室へ移動できる場合は移動します。
			vJudgeMoveExaminationMRIRoom( ArrayListExaminationMRIRooms, this, erPAgent.erGetConsultationDoctorAgent(), erPAgent, -2 );
			// 移動可能なので移動します。
			if( erPAgent.iGetExaminationMRIRoomWaitFlag() == 0 ) return;
			// 移動していないと思われる場合。
			else
			{
				// この場合、元のエージェントは別の部屋に移動しているので、移動します。
				if( erPAgent.iGetLocation() != iPrevLocation ) return ;
				// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
			}
		}
		if( erPAgent.iGetExaminationAngiographyRoomWaitFlag() == 1 )
		{
			// 血管造影室へ移動できる場合は移動します。
			vJudgeMoveExaminationAngiographyRoom( ArrayListExaminationAngiographyRooms, this, erPAgent.erGetConsultationDoctorAgent(), erPAgent, -2 );
			// 移動可能なので移動します。
			if( erPAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 ) return;
			// 移動していないと思われる場合。
			else
			{
				// この場合、元のエージェントは別の部屋に移動しているので、移動します。
				if( erPAgent.iGetLocation() != iPrevLocation ) return ;
				// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
			}
		}
		if( erPAgent.iGetExaminationFastRoomWaitFlag() == 1 )
		{
			// 超音波室へ移動できる場合は移動します。
			vJudgeMoveExaminationFastRoom( ArrayListExaminationFastRooms, this, erPAgent.erGetConsultationDoctorAgent(), erPAgent, -2 );
			// 移動可能なので移動します。
			if( erPAgent.iGetExaminationFastRoomWaitFlag() == 0 ) return;
			// 移動していないと思われる場合。
			else
			{
				// この場合、元のエージェントは別の部屋に移動しているので、移動します。
				if( erPAgent.iGetLocation() != iPrevLocation ) return ;
				// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
			}
		}
	}

	/**
	 * <PRE>
	 *    通常の観察及びトリアージプロセスを実行します。
	 * </PRE>
	 * @param ArrayListConsultationRooms			全診察室
	 * @param ArrayListObservationRooms				全観察室
	 * @param ArrayListSevereInjuryObservationRooms	全重症観察室
	 * @param ArrayListEmergencyRooms				全初療室
	 * @param erNurseAgent							対応している看護師エージェント
	 * @param erPAgent								観察を受けている患者エージェント
	 * @param iLoc									看護師がどの患者を対応していたのか
	 * @throws ERNurseAgentException				看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	private void vObservationProcess( ArrayList<ERConsultationRoom> ArrayListConsultationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc ) throws ERNurseAgentException
	{
		int i,j;
		int iPriorityFlag = 0;
		double lfObservationProcessTime;

		lfObservationProcessTime = erNurseAgent.lfGetObservationProcessTime()+5*(2*rnd.NextUnif()-1);
		// 観察プロセスを実行します。
		if( iJudgeObservationProcessTime( lfObservationProcessTime, erNurseAgent) == 1 )
//		if( lfObservationProcessTime <= erNurseAgent.lfGetCurrentPassOverTime() )
//		if( lfObservationProcessTime <= erNurseAgent.lfGetCurrentPassOverTime()-erPAgent.lfGetMoveTime() )
		{
			cWaitingRoomLog.info(erPAgent.getId() + "," + "看護師観察中(待合室)：" + erNurseAgent.lfGetObservationTime() + "," + erNurseAgent.lfGetCurrentPassOverTime());
			if( erNurseAgent.iGetNurseCategory() == 1 )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "トリアージ開始(待合室)：" + erNurseAgent.lfGetCurrentPassOverTime());
				erNurseAgent.vImplementNurseProcess( 1, erPAgent );
				// 再トリアージの時間設定を行います。
				erNurseAgent.vSetTriageProtocolTime( 0 );
			}
			else
			{
				erNurseAgent.vImplementNurseProcess( 0, erPAgent );
				// 再トリアージの時間設定を行います。
//				erNurseAgent.vSetTriageProtocolTime( 0 );
			}

			// 観察してもらったことを表すフラグをONにします。
			erPAgent.vSetObservedFlag( 1 );
			erPAgent.vSetEmergencyLevel( erNurseAgent.iGetEmergencyLevel() );

			// 非緊急であった場合
			if( erNurseAgent.iGetEmergencyLevel() == 5 )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル5" + "," + "待合室");
//				for( i = 0;i < ArrayListObservationRooms.size(); i++ )
//				{
//					for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
//					{
//						if( ArrayListObservationRooms.get(i).erGetPatientAgent(j).iGetEmergencyLevel() == 3 || ArrayListObservationRooms.get(i).erGetPatientAgent(j).iGetEmergencyLevel() == 4 )
//						{
//							// 観察室に緊急度が3の患者がいる場合はその人を優先するため、待合室で待機します。
//							// 診察フラグをONにします。
//							erPAgent.vSetConsultationRoomWaitFlag( 1 );
//							iPriorityFlag = 1;
//							break;
//						}
//					}
//				}
//				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
//				{
//					// かなりの患者が亡くなられている場合のエラー対策。
//					if( ArrayListPatientAgents.size() <= i )
//					{
//						continue;
//					}
//					if( ArrayListPatientAgents.get(i) == null )
//					{
//						continue;
//					}
//					if( ArrayListPatientAgents.get(i).iGetEmergencyLevel() == 3 || ArrayListPatientAgents.get(i).iGetEmergencyLevel() == 4 )
//					{
//						// 観察室に緊急度が3の患者がいる場合はその人を優先するため、待合室で待機します。
//						// 診察フラグをONにします。
//						erPAgent.vSetConsultationRoomWaitFlag( 1 );
//						iPriorityFlag = 1;
//						break;
//					}
//				}
//				if( iPriorityFlag == 0 )
				{
					// 診察室へ移動します。
					vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc  );
				}
			}
			// 低緊急であった場合
			else if( erNurseAgent.iGetEmergencyLevel() == 4 )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル4" + "," + "待合室");
//				for( i = 0;i < ArrayListObservationRooms.size(); i++ )
//				{
//					for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
//					{
//						if( ArrayListObservationRooms.get(i).erGetPatientAgent(j).iGetEmergencyLevel() == 3 )
//						{
//							// 観察室に緊急度が3の患者がいる場合はその人を優先するため、待合室で待機します。
//							// 診察フラグをONにします。
//							erPAgent.vSetConsultationRoomWaitFlag( 1 );
//							iPriorityFlag = 1;
//							break;
//						}
//					}
//				}
//				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
//				{
//					// かなりの患者が亡くなられている場合のエラー対策。
//					if( ArrayListPatientAgents.size() <= i )
//					{
//						continue;
//					}
//					if( ArrayListPatientAgents.get(i) == null )
//					{
//						continue;
//					}
//					if( ArrayListPatientAgents.get(i).iGetEmergencyLevel() == 3 )
//					{
//						// 観察室に緊急度が3の患者がいる場合はその人を優先するため、待合室で待機します。
//						// 診察フラグをONにします。
//						erPAgent.vSetConsultationRoomWaitFlag( 1 );
//						iPriorityFlag = 1;
//						break;
//					}
//				}
//				if( iPriorityFlag == 0 )
				{
					// 診察室へ移動します。
					vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc  );
				}
//				else
				{
					// 観察室へ移動します。
//					vJudgeMoveObservationRoom( ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc  );
				}
			}
			// 準緊急であった場合
			else if( erNurseAgent.iGetEmergencyLevel() == 3 )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル3" + "," + "待合室");
				// 診察フラグをONにします。
				erPAgent.vSetConsultationRoomWaitFlag( 1 );
				vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
			}
			// 緊急であった場合
			else if( erNurseAgent.iGetEmergencyLevel() == 2  )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル2" + "," + "待合室");
				// 初療室へ移動します。
				vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
			}
			// 蘇生レベルあった場合
			else if(  erNurseAgent.iGetEmergencyLevel() == 1 )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル1" + "," + "待合室");
				// 初療室へ移動します。
				vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
			}
		}
	}

	/**
	 * <PRE>
	 *    通常の観察及びトリアージプロセスを実行します。
	 * </PRE>
	 * @param ArrayListConsultationRooms			全診察室
	 * @param ArrayListObservationRooms				全観察室
	 * @param ArrayListSevereInjuryObservationRooms	全重症観察室
	 * @param ArrayListEmergencyRooms				全初療室
	 * @param erNurseAgent							対応している看護師エージェント
	 * @param erPAgent								観察を受けている患者エージェント
	 * @param iLoc									看護師がどの患者を対応していたのか
	 * @throws ERNurseAgentException				看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	private void vReObservationProcess( ArrayList<ERConsultationRoom> ArrayListConsultationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc ) throws ERNurseAgentException
	{
		int i,j;
		int iPriorityFlag = 0;
		double lfObservationProcessTime;
		lfObservationProcessTime = erNurseAgent.lfGetObservationProcessTime()+5*(2*rnd.NextUnif()+1);

		// 定期観察時間が経過した場合
//		if( iJudgeObservationProcessTime( lfObservationProcessTime, erNurseAgent) == 1 )
		if( erNurseAgent.lfGetObservationTime() <= erNurseAgent.lfGetCurrentPassOverTime() )
		{
			// トリアージプロトコ時間が経過した場合
			if( iJudgeTriageProcessTime( erNurseAgent.lfGetTriageProcessTime(), erNurseAgent) == 1 )
//			if( erNurseAgent.lfGetTriageProcessTime() <= erNurseAgent.lfGetCurrentPassOverTime() )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "看護師再度観察中(待合室)：" + erNurseAgent.lfGetObservationTime() + "," + erNurseAgent.lfGetCurrentPassOverTime());
				// トリアージ実行時間経過した場合は、
				// トリアージプロセスを実行します。
				if( erNurseAgent.iGetNurseCategory() == 1 )
				{
					cWaitingRoomLog.info(erPAgent.getId() + "," + "トリアージ開始(待合室)：" + erNurseAgent.lfGetCurrentPassOverTime());
					erNurseAgent.vImplementNurseProcess( 1, erPAgent );
					// 再トリアージの時間設定を行います。
					erNurseAgent.vSetTriageProtocolTime( 0 );
				}
				else
				{
					// トリアージナースでない場合は通常の観察を実行します。
					erNurseAgent.vImplementNurseProcess( 0, erPAgent );
					// 再トリアージの時間設定を行います。
//					erNurseAgent.vSetTriageProtocolTime( 0 );
				}
			}
			else
			{
				// トリアージ実行時間が経過していない場合は
				// 通常の観察を実行します。
				erNurseAgent.vImplementNurseProcess( 0, erPAgent );
			}
			erPAgent.vSetEmergencyLevel( erNurseAgent.iGetEmergencyLevel() );
			// 非緊急であった場合
			if( erNurseAgent.iGetEmergencyLevel() == 5 )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル5" + "," + "待合室");
//				for( i = 0;i < ArrayListObservationRooms.size(); i++ )
//				{
//					for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
//					{
//						if( ArrayListObservationRooms.get(i).erGetPatientAgent(j).iGetEmergencyLevel() == 3 || ArrayListObservationRooms.get(i).erGetPatientAgent(j).iGetEmergencyLevel() == 4 )
//						{
//							// 観察室に緊急度が3の患者がいる場合はその人を優先するため、待合室で待機します。
//							// 診察フラグをONにします。
//							erPAgent.vSetConsultationRoomWaitFlag( 1 );
//							iPriorityFlag = 1;
//							break;
//						}
//					}
//				}
//				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
//				{
//					// かなりの患者が亡くなられている場合のエラー対策。
//					if( ArrayListPatientAgents.size() <= i )
//					{
//						continue;
//					}
//					if( ArrayListPatientAgents.get(i) == null )
//					{
//						continue;
//					}
//					if( ArrayListPatientAgents.get(i).iGetEmergencyLevel() == 3 || ArrayListPatientAgents.get(i).iGetEmergencyLevel() == 4 )
//					{
//						// 観察室に緊急度が3の患者がいる場合はその人を優先するため、待合室で待機します。
//						// 診察フラグをONにします。
//						erPAgent.vSetConsultationRoomWaitFlag( 1 );
//						iPriorityFlag = 1;
//						break;
//					}
//				}
//				if( iPriorityFlag == 0 )
				{
					// 診察室へ移動します。
					vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc  );
				}
			}
			// 低緊急であった場合
			else if( erNurseAgent.iGetEmergencyLevel() == 4 )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル4" + "," + "待合室");
//				for( i = 0;i < ArrayListObservationRooms.size(); i++ )
//				{
//					for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
//					{
//						if( ArrayListObservationRooms.get(i).erGetPatientAgent(j).iGetEmergencyLevel() == 3 )
//						{
//							// 観察室に緊急度が3の患者がいる場合はその人を優先するため、待合室で待機します。
//							// 診察フラグをONにします。
//							erPAgent.vSetConsultationRoomWaitFlag( 1 );
//							iPriorityFlag = 1;
//							break;
//						}
//					}
//				}
//				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
//				{
//					// かなりの患者が亡くなられている場合のエラー対策。
//					if( ArrayListPatientAgents.size() <= i )
//					{
//						continue;
//					}
//					if( ArrayListPatientAgents.get(i) == null )
//					{
//						continue;
//					}
//					if( ArrayListPatientAgents.get(i).iGetEmergencyLevel() == 3 )
//					{
//						// 観察室に緊急度が3の患者がいる場合はその人を優先するため、待合室で待機します。
//						// 診察フラグをONにします。
//						erPAgent.vSetConsultationRoomWaitFlag( 1 );
//						iPriorityFlag = 1;
//						break;
//					}
//				}
//				if( iPriorityFlag == 0 )
				{
					// 診察室へ移動します。
					vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc  );
				}
//				else
				{
					// 観察室へ移動します。
//					vJudgeMoveObservationRoom( ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc  );
				}
			}
			// 準緊急であった場合
			else if( erNurseAgent.iGetEmergencyLevel() == 3 )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル3" + "," + "待合室");
				// 診察フラグをONにします。
				erPAgent.vSetConsultationRoomWaitFlag( 1 );
				// 診察室へ移動します。
				vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc  );
			}
			// 緊急であった場合
			else if( erNurseAgent.iGetEmergencyLevel() == 2  )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル2" + "," + "待合室");
				// 初療室へ移動します。
				vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
			}
			// 蘇生レベルあった場合
			else if(  erNurseAgent.iGetEmergencyLevel() == 1 )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル1" + "," + "待合室");
				// 初療室へ移動します。
				vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
			}
		}
		else
		{
			// 定期観察時間が経過していない場合は現在のエージェントが次に移動すべき情報を調べる必要があります。

			// トリアージプロトコ時間が経過した場合
			if( erNurseAgent.lfGetTriageProcessTime() <= erNurseAgent.lfGetCurrentPassOverTime() )
			{
				// トリアージ実行時間経過した場合は、
				// トリアージプロセスを実行します。
				if( erNurseAgent.iGetNurseCategory() == 1 )
				{
					cWaitingRoomLog.info(erPAgent.getId() + "," + "トリアージ開始：" + erNurseAgent.lfGetCurrentPassOverTime());
					erNurseAgent.vImplementNurseProcess( 1, erPAgent );
				}
				else
				{
					erNurseAgent.vImplementNurseProcess( 0, erPAgent );
					// 再トリアージの時間設定を行います。
//					erNurseAgent.vSetTriageProtocolTime( 0 );
				}
				erPAgent.vSetEmergencyLevel( erNurseAgent.iGetEmergencyLevel() );

				// 非緊急の場合は診察室が空いているかを調べます。
				if( erNurseAgent.iGetEmergencyLevel() == 5 )
				{
					cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル5" + "," + "待合室");
					vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
				}
				// 準緊急の場合は観察室が空いているかどうかを調べる。
				else if( erNurseAgent.iGetEmergencyLevel() == 4 )
				{
					cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル4" + "," + "待合室");
					vJudgeMoveObservationRoom( ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
				}
				// 緊急の場合は重症観察室が空いているかどうかを調べる。
				else if( erNurseAgent.iGetEmergencyLevel() == 3 )
				{
					cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル3" + "," + "待合室");
					// 診察フラグをONにします。
					erPAgent.vSetConsultationRoomWaitFlag( 1 );
					vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
				}
				// 緊急であった場合
				else if( erNurseAgent.iGetEmergencyLevel() == 2  )
				{
					cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル2" + "," + "待合室");
					// 初療室へ移動します。
					vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
				}
				// 蘇生レベルあった場合
				else if(  erNurseAgent.iGetEmergencyLevel() == 1 )
				{
					cWaitingRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル1" + "," + "待合室");
					// 初療室へ移動します。
					vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
				}
			}
		}
	}

	/**
	 * <PRE>
	 *    診察室への移動判定を実施します。
	 *    移動できない場合は待合室で待機します。
	 * </PRE>
	 *
	 * @param ArrayListConsultationRooms	全診察室
	 * @param ArrayListObservationRooms		全観察室
	 * @param erNurseAgent					看護師エージェント
	 * @param erPAgent						患者エージェント
	 * @param iLoc							看護師がどの患者を対応していたのか
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	private void vJudgeMoveConsultationRoom( ArrayList<ERConsultationRoom> ArrayListConsultationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc )
	{
		int i,j;
		int iJudgeCount = 0;
		int iJudgeSevereFlag = -1;
		int iJudgeWaitTimeFlag = -1;
		int iJudgeFlag = -1;
		boolean bRet = false;
		ERDoctorAgent erConsultationDoctorAgent;
		ERNurseAgent erConsultationNurseAgent;
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			// 診察室に空きがあるか否か
			if( ArrayListConsultationRooms.get(i).isVacant() == true )
			{
				// 現在対応している患者よりも重症患者がいない場合は初療室へ移動します。
				// 他にいる場合は、その人を移動させて、もっとも重症度の低い患者を移動させます。
//				iJudgeFlag = iJudgeConsultationPatient(0, erPAgent, ArrayListObservationRooms, this );
//				iJudgeFlag = iJudgeConsultationPatient(1, erPAgent, ArrayListObservationRooms, this );
				iJudgeSevereFlag = iJudgeConsultationPatient(1, erPAgent, ArrayListObservationRooms, this );
				iJudgeWaitTimeFlag = iJudgeWaitTimeConsultationPatient( erPAgent, ArrayListObservationRooms, this );
				// 両方のフラグが有効の場合は長時間待たせ続けている患者さんを対応します。
				if( iJudgeSevereFlag == -1 )								iJudgeFlag = iJudgeWaitTimeFlag;
				if( iJudgeWaitTimeFlag == -1 )								iJudgeFlag = iJudgeSevereFlag;
				if( iJudgeSevereFlag != -1 && iJudgeWaitTimeFlag != -1 )	iJudgeFlag = iJudgeWaitTimeFlag;

				// 待合室に該当者がいる場合
				if( iJudgeFlag == 0 )
				{
					// 待合室の患者が該当する場合は待合室の患者を診察室へ移動し、ここでの患者は待機します。
					bRet = bChangeConsultationRoomWaitingRoomPatient( erPAgent, ArrayListConsultationRooms );
					if( bRet == true )
					{
						// 診察室へ行くことには変わりがないので診察室待機フラグをONにします。
						erPAgent.vSetConsultationRoomWaitFlag( 1 );
						cWaitingRoomLog.info(erPAgent.getId() + "," + "待機(診察室へは他の待合室の患者が移動)" + "," + "待合室");
					}
				}
				// 観察室に該当者がいる場合
				else if( iJudgeFlag == 1 )
				{
					// 観察室の患者が該当する場合は観察室の患者を初療室へ移動し、ここでの患者は観察室へ移動します。
					bRet = bChangeConsultationRoomObservationRoomPatient( erPAgent, ArrayListConsultationRooms, ArrayListObservationRooms );
					if( bRet == true )
					{
						// 観察室待機フラグをONにします。
						erPAgent.vSetConsultationRoomWaitFlag( 1 );
						// 診察室へ行くことには変わりがないので診察室待機フラグをONにします。
						vJudgeMoveObservationRoom( ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
						cWaitingRoomLog.info(erPAgent.getId() + "," + "観察室へ移動(診察室へは観察室の患者が移動)" + "," + "待合室");
					}
				}
				// 当人の場合
				if( bRet == false )
				{
					cWaitingRoomLog.info(erPAgent.getId() + "," + "診察室へ移動準備開始" + "," + "待合室");

					// 診察室待機フラグをOFFにします。
					erPAgent.vSetConsultationRoomWaitFlag( 0 );

					// 患者のいる位置を診察室に変更します。
					erPAgent.vSetLocation( 1 );

					// 観察フラグをOFFにします。
					erPAgent.vSetObservedFlag( 0 );

					// 待合室での看護師に見てもらったフラグはOFFにします。
					erPAgent.vSetNurseAttended( 0 );

					// 移動開始フラグを設定します。
					erPAgent.vSetMoveRoomFlag( 1 );
					erPAgent.vSetMoveWaitingTime( 0.0 );

					// 患者エージェントを診察室に配置します。
					ArrayListConsultationRooms.get(i).vSetPatientAgent( erPAgent );

					// その患者を対応している看護師エージェントがいなくなるので0に設定します。
					erPAgent.vSetNurseAgent( 0 );

					// 看護師、および医師エージェントにメッセージを送信します。
					// 診察室の医師エージェントに患者情報を送信します。
					erConsultationDoctorAgent = ArrayListConsultationRooms.get(i).cGetDoctorAgent();

					if( erNurseAgent != null )
					{
						erNurseAgent.vSendToDoctorAgentMessage( erPAgent, erConsultationDoctorAgent, (int)erNurseAgent.getId(), (int)erConsultationDoctorAgent.getId() );

						// 診察室の看護師エージェントに患者情報を送信します。
						for( j = 0;j < ArrayListConsultationRooms.get(i).iGetNurseAgentsNum(); j++ )
						{
							erConsultationNurseAgent = ArrayListConsultationRooms.get(i).cGetNurseAgent(j);
							erNurseAgent.vSendToNurseAgentMessage( erPAgent, (int)erNurseAgent.getId(), (int)erConsultationNurseAgent.getId() );
						}

						// 看護師エージェントの対応を終了します。
						erNurseAgent.vSetAttending( 0 );
					}

					// もしも-2を指定した場合、看護師がまだ担当していない患者が移動するため、ここでは患者看護師対応表は参照しません。
					if( iLoc > -2 )
					{
						// 対応を受けた患者エージェントを削除します。
						vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
						ArrayListNursePatientLoc.set( iLoc, -1 );
					}
					// 診察室で担当する医師エージェントを設定します。
					cWaitingRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + erConsultationDoctorAgent.iGetAttending() + "," + "移動先の診察室の医師の状態" + "," + "待合室");
//					ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetConsultation(1);
					erConsultationDoctorAgent.vSetAttending(1);

					// 医師の診察時間を設定します。
					ArrayListConsultationRooms.get(i).cGetDoctorAgent().isJudgeConsultationTime( erPAgent );

					cWaitingRoomLog.info(erPAgent.getId() + "," + "診察室へ移動準備終了" + "," + "待合室");
					if( iInverseSimFlag == 1 )
					{
						// 移動先の経路を患者エージェントに設定します。
						cWaitingRoomLog.info(erPAgent.getId() + "," + "診察室へ移動開始" + "," + "待合室");
						erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListConsultationRooms.get(i).erGetTriageNode() ) );
					}
					erPAgent = null;

					break;
				}
//				// 別の人の場合
//				else
//				{
//					break;
//				}
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListConsultationRooms.size() )
		{
			// 診察室待機フラグをONにします。
			erPAgent.vSetConsultationRoomWaitFlag( 1 );
			cWaitingRoomLog.info(erPAgent.getId() + "," + "診察室満員" + "," + "待合室");
			// 観察室へ移動します。
			vJudgeMoveObservationRoom( ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
		}
	}

	/**
	 * <PRE>
	 *    観察室への移動判定を実施します。
	 *    移動できない場合は待合室で待機します。
	 * </PRE>
	 * @param ArrayListObservationRooms		観察室エージェント
	 * @param erNurseAgent					看護師エージェント
	 * @param erPAgent						患者エージェント
	 * @param iLoc							看護師がどの患者を対応していたのか
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	private void vJudgeMoveObservationRoom( ArrayList<ERObservationRoom> ArrayListObservationRooms, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc )
	{
		int i,j;
		int iJudgeCount = 0;
		ERNurseAgent erObservationNurseAgent;
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			// 観察室に空きがある場合は観察室へエージェントを移動します。
			if( ArrayListObservationRooms.get(i).isVacant() == true )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "観察室へ移動準備開始" + "," + "待合室");

				// 観察室待機フラグをOFFにします。
				erPAgent.vSetObservationRoomWaitFlag( 0 );

				// 患者のいる位置を観察室に変更します。
				erPAgent.vSetLocation( 4 );

				// 観察フラグをOFFにします。
				erPAgent.vSetObservedFlag( 0 );

				// 待合室での看護師に見てもらったフラグはOFFにします。
				erPAgent.vSetNurseAttended( 0 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				ArrayListObservationRooms.get(i).vSetPatientAgent( erPAgent );

				// その患者を対応している看護師エージェントがいなくなるので0に設定します。
				erPAgent.vSetNurseAgent( 0 );

				if( erNurseAgent != null )
				{
					// 看護師エージェントへメッセージを送信します。
					erNurseAgent.vSendToNurseAgentMessage( erPAgent, (int)erNurseAgent.getId(), (int)erPAgent.getId() );

					// 観察室の看護師エージェントに患者情報を送信します。
					for( j = 0;j < ArrayListObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
					{
						erObservationNurseAgent = ArrayListObservationRooms.get(i).erGetNurseAgent(j);
						// 看護師エージェントへメッセージを送信します。
						erNurseAgent.vSendToNurseAgentMessage( erPAgent, (int)erNurseAgent.getId(), (int)erObservationNurseAgent.getId() );
					}

					// 看護師エージェントの対応を終了します。
					erNurseAgent.vSetAttending( 0 );
				}

				// もしも-2を指定した場合、看護師がまだ担当していない患者が移動するため、ここでは患者看護師対応表は参照しません。
				if( iLoc > -2 )
				{
					// 対応を受けた患者エージェントを削除します。
					vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
					ArrayListNursePatientLoc.set( iLoc, -1 );
				}

				cWaitingRoomLog.info(erPAgent.getId() + "," + "観察室へ移動準備終了" + "," + "待合室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					cWaitingRoomLog.info(erPAgent.getId() + "," + "観察室へ移動開始" + "," + "待合室");
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListObservationRooms.get(i).erGetTriageNode() ) );
				}
				erPAgent = null;

				// 空いている看護師に割り当てます。
				ArrayListObservationRooms.get(i).bAssignVacantNurse();

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListObservationRooms.size() )
		{
			// 空きがない場合は観察室待機フラグをONにしてそのまま待機します。
			erPAgent.vSetObservationRoomWaitFlag( 1 );
			cWaitingRoomLog.info(erPAgent.getId() + "," + "観察室満員" + "," + "待合室");
		}
	}

	/**
	 * <PRE>
	 *    重症観察室への移動判定を実施します。
	 *    移動できない場合は待合室で待機します。
	 * </PRE>
	 * @param ArrayListSereveInjuryObservationRooms	全重症観察室
	 * @param erNurseAgent							看護師エージェント
	 * @param erPAgent								患者エージェント
	 * @param iLoc									看護師がどの患者を対応していたのか
	 */
	private void vJudgeMoveSereveInjuryObservationRoom( ArrayList<ERSevereInjuryObservationRoom> ArrayListSereveInjuryObservationRooms, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc )
	{
		int i,j;
		int iJudgeCount = 0;
		ERNurseAgent erSevereInjuryObservationNurseAgent;
		for( i = 0;i < ArrayListSereveInjuryObservationRooms.size(); i++ )
		{
			// 重症観察室に空きがある場合は観察室へエージェントを移動します。
			if( ArrayListSereveInjuryObservationRooms.get(i).isVacant() == true )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "重症観察室へ移動準備開始" + "," + "待合室");

				// 重傷観察室待機フラグをOFFにします。
				erPAgent.vSetSereveInjuryObservationRoomWaitFlag( 0 );

				// 患者のいる位置を重症観察室に変更します。
				erPAgent.vSetLocation( 5 );

				// 観察フラグをOFFにします。
				erPAgent.vSetObservedFlag( 0 );

				// 待合室での看護師に見てもらったフラグはOFFにします。
				erPAgent.vSetNurseAttended( 0 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している看護師エージェントがいなくなるので0に設定します。
				erPAgent.vSetNurseAgent( 0 );

				// 重症観察室へ移動します。
				ArrayListSereveInjuryObservationRooms.get(i).vSetPatientAgent( erPAgent );

				if( erNurseAgent != null )
				{
					// 重症観察室の看護師エージェントに患者情報を送信します。
					for( j = 0;j < ArrayListSereveInjuryObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
					{
						erSevereInjuryObservationNurseAgent = ArrayListSereveInjuryObservationRooms.get(i).erGetNurseAgent(j);
						// 看護師エージェントへメッセージを送信します。
						erNurseAgent.vSendToNurseAgentMessage( erPAgent, (int)erNurseAgent.getId(), (int)erSevereInjuryObservationNurseAgent.getId() );
					}

					// 看護師エージェントの対応を終了します。
					erNurseAgent.vSetAttending( 0 );
				}

				// もしも-2を指定した場合、看護師がまだ担当していない患者が移動するため、ここでは患者看護師対応表は参照しません。
				if( iLoc > -2 )
				{
					// 対応を受けた患者エージェントを削除します。
					vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
					ArrayListNursePatientLoc.set( iLoc, -1 );
				}

				cWaitingRoomLog.info(erPAgent.getId() + "," + "重症観察室へ移動準備終了" + "," + "待合室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListSereveInjuryObservationRooms.get(i).erGetTriageNode() ) );
					cWaitingRoomLog.info(erPAgent.getId() + "," + "重症観察室へ移動開始" + "," + "待合室");
				}
				erPAgent = null;

				// 空いている看護師に割り当てます。
				ArrayListSereveInjuryObservationRooms.get(i).bAssignVacantNurse();

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListSereveInjuryObservationRooms.size() )
		{
			// 空きがない場合は重傷観察室待機フラグをONにしてそのまま待合室で待機します。
			erPAgent.vSetSereveInjuryObservationRoomWaitFlag( 1 );
			cWaitingRoomLog.info(erPAgent.getId() + "," + "重症観察室満員" + "," + "待合室");
		}
	}

	/**
	 * <PRE>
	 *    初療室へ移動します。
	 * </PRE>
	 * @param ArrayListEmergencyRooms					全初療室
	 * @param ArrayListSevereInjuryObservationRooms		全重症観察室
	 * @param ArrayListObservationRooms					全観察室
	 * @param erNurseAgent								担当した看護師エージェント
	 * @param erPAgent									移動する患者エージェント
	 * @param iLoc										担当した看護師エージェントと患者エージェントの番号
	 */
	public void vJudgeMoveEmergencyRoom( ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc )
	{
		int i,j;
		int iJudgeFlag = 0;
		int iJudgeCount = 0;
		int iJudgeSevereFlag = -1;
		int iJudgeWaitTimeFlag = -1;
		boolean bRet = false;
		ERDoctorAgent erEmergencyDoctorAgent;
		ERNurseAgent erEmergencyNurseAgent;
//		ERClinicalEngineerAgent erEmergencyclinicalEngineerAgent;
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			// 初療室に空きがある場合
			if( ArrayListEmergencyRooms.get(i).isVacant() == true )
			{
				// 現在対応している患者よりも重症患者がいない場合は初療室へ移動します。
				// 他にいる場合は、その人を移動させて、もっとも重症度の低い患者を移動させます。
//				iJudgeFlag = iJudgeEmergencyPatient( 0, erPAgent, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms );
//				iJudgeFlag = iJudgeEmergencyPatient( 1, erPAgent, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms );
				iJudgeSevereFlag = iJudgeEmergencyPatient( 1, erPAgent, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms );
				iJudgeWaitTimeFlag = iJudgeWaitTimeEmergencyPatient( erPAgent, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms );
				// 両方のフラグが有効の場合は長時間待たせ続けている患者さんを対応します。
				if( iJudgeSevereFlag == -1 )								iJudgeFlag = iJudgeWaitTimeFlag;
				if( iJudgeWaitTimeFlag == -1 )								iJudgeFlag = iJudgeSevereFlag;
				if( iJudgeSevereFlag != -1 && iJudgeWaitTimeFlag != -1 )	iJudgeFlag = iJudgeWaitTimeFlag;

				// 待合室に該当者がいる場合
				if( iJudgeFlag == 0 )
				{
					// 待合室の患者が該当する場合は待合室の患者を初療室へ移動し、ここでの患者は待合室に待機します。
					bRet = bChangeEmergencyRoomWaitingRoomPatient( erPAgent, ArrayListEmergencyRooms, ArrayListObservationRooms, ArrayListSevereInjuryObservationRooms );
					// 初療室待機者に変わりはないので初療室待機フラグをONにします。
					erPAgent.vSetEmergencyRoomWaitFlag( 1 );
					cWaitingRoomLog.info(erPAgent.getId() + "," + "そのまま待機" + "," + "待合室(初療室へは待合室の他の患者が移動)" +"," + "待合室" );
				}
				// 重症観察室に該当者がいる場合
				else if( iJudgeFlag == 1 )
				{
					// 重症観察室の患者が該当する場合は重症観察室の患者を初療室へ移動し、ここでの患者は重症観察室へ移動します。
					bRet = bChangeEmergencyRoomSevereInjuryObservationPatient(erPAgent, ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms );
					if( bRet == true )
					{
						// 初療室待機者に変わりはないので初療室待機フラグをONにします。
						erPAgent.vSetEmergencyRoomWaitFlag( 1 );
						// 重症観察室の移動判定をします。
						vJudgeMoveSereveInjuryObservationRoom( ArrayListSevereInjuryObservationRooms, erNurseAgent, erPAgent, iLoc );
						cWaitingRoomLog.info(erPAgent.getId() + "," + "重症観察室へ移動(初療室へは重症観察室の患者が移動)" + "," + "待合室");
					}
				}
				// 観察室に該当者がいる場合
				else if( iJudgeFlag == 2 )
				{
					// 観察室の患者が該当する場合は観察室の患者を初療室へ移動し、ここでの患者は観察室へ移動します。
					bRet = bChangeEmergencyRoomObservationPatient(erPAgent, ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, this );
					if( bRet == true )
					{
						// 観察室待機フラグをONにします。
						erPAgent.vSetEmergencyRoomWaitFlag( 1 );
						// 観察室の移動判定をします。
						vJudgeMoveObservationRoom( ArrayListObservationRooms, erNurseAgent, erPAgent, iLoc );
						cWaitingRoomLog.info(erPAgent.getId() + "," + "観察室へ移動(初療室へは重症観察室の患者が移動)" + "," + "待合室");
					}
				}
				// 当人の場合
				if( bRet == false )
				{
					cWaitingRoomLog.info(erPAgent.getId() + "," + "初療室へ移動準備開始" + "," + "待合室");

					// 初療室待機フラグをOFFにします。
//					erPAgent.vSetWaitingRoomWaitFlag( 0 );
					erPAgent.vSetEmergencyRoomWaitFlag( 0 );

					// 患者のいる位置を初療室に変更します。
					erPAgent.vSetLocation( 3 );

					// 観察フラグをOFFにします。
					erPAgent.vSetObservedFlag( 0 );

					// 待合室での看護師に見てもらったフラグはOFFにします。
					erPAgent.vSetNurseAttended( 0 );

					// 移動開始フラグを設定します。
					erPAgent.vSetMoveRoomFlag( 1 );
					erPAgent.vSetMoveWaitingTime( 0.0 );

					// その患者を対応している看護師エージェントがいなくなるので0に設定します。
					erPAgent.vSetNurseAgent( 0 );

					// 初療室へ患者エージェントを移動します。
					ArrayListEmergencyRooms.get(i).vSetPatientAgent( erPAgent );

				// 看護師、医師、技士エージェントへメッセージを送信します。
					if( erNurseAgent != null )
					{
						// 初療室の看護師エージェントに患者情報を送信します。
						for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetNurseAgentsNum(); j++ )
						{
							erEmergencyNurseAgent = ArrayListEmergencyRooms.get(i).cGetNurseAgent(j);
							erNurseAgent.vSendToNurseAgentMessage( erPAgent, (int)erNurseAgent.getId(), (int)erEmergencyNurseAgent.getId() );
						}
						// 初療室の医師エージェントに患者情報を送信します。
						for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetDoctorAgentsNum(); j++ )
						{
							erEmergencyDoctorAgent = ArrayListEmergencyRooms.get(i).cGetDoctorAgent( j );
							erNurseAgent.vSendToDoctorAgentMessage( erPAgent, erEmergencyDoctorAgent, (int)erNurseAgent.getId(), (int)erEmergencyDoctorAgent.getId() );
						}
//						for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
//						{
//							erEmergencyCinicalEngineerAgent = ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgents(j);
//							erNurseAgent.vSendToEngineerAgentMessage( erPAgent, (int)erNurseAgent.getId(), (int)erEmergencyClinicalEngineerAgent.getId() );
//						}

						// 看護師エージェントの対応を終了します。
						erNurseAgent.vSetAttending( 0 );
					}
					// もしも-2を指定した場合、看護師がまだ担当していない患者が移動するため、ここでは患者看護師対応表は参照しません。
					if( iLoc > -2 )
					{
						// 対応を受けた患者エージェントを削除します。
						vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
						ArrayListNursePatientLoc.set( iLoc, -1 );
					}

					// 初療室で担当する医師エージェントを設定します。
					ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetSurgeon(1);
					ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetAttending(1);

					cWaitingRoomLog.info(erPAgent.getId() + "," + "初療室へ移動準備終了" + "," + "待合室");
					if( iInverseSimFlag == 1 )
					{
						// 移動先の経路を患者エージェントに設定します。
						erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListEmergencyRooms.get(i).erGetTriageNode() ) );
						cWaitingRoomLog.info(erPAgent.getId() + "," + "初療室へ移動開始" + "," + "待合室");
					}
					erPAgent = null;
					break;
				}
				// 別の人の場合
				else
				{
					break;
				}
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListEmergencyRooms.size() )
		{
			// 空きがない場合は初療室待機フラグをONにします。
			erPAgent.vSetEmergencyRoomWaitFlag( 1 );

			// 重症観察室の移動判定をします。
			cWaitingRoomLog.info(erPAgent.getId() + "," + "初療室満員" + "," + "待合室");
			vJudgeMoveSereveInjuryObservationRoom( ArrayListSevereInjuryObservationRooms, erNurseAgent, erPAgent, iLoc );
		}
	}

	/**
	 * <PRE>
	 *    X線室へ移動可能かどうかを判定し、移動できる場合はX線室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListExaminationXRayRooms				全X線室
	 * @param erWaitingRoom								待合室
	 * @param erConsultationDoctorAgentData				担当医師エージェント
	 * @param erPAgent									移動する患者エージェント
	 * @param iLoc										担当した看護師エージェントと患者エージェントの番号
	 * @throws NullPointerException						アドレスNULLアクセス例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveExaminationXRayRoom( ArrayList<ERExaminationXRayRoom> ArrayListExaminationXRayRooms, ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgentData, ERPatientAgent erPAgent, int iLoc  ) throws NullPointerException
	{
		int i;
		int iJudgeCount = 0;
		ERClinicalEngineerAgent erExaminationClinicalEngineerAgent;
		for( i = 0;i < ArrayListExaminationXRayRooms.size(); i++ )
		{
			// 検査室に空きがある場合は検査室へエージェントを移動します。
			if( ArrayListExaminationXRayRooms.get(i).isVacant() == true )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "X線室へ移動準備開始" + "," + "待合室");
				// 検査室待ちフラグをOFFにします。
				erPAgent.vSetExaminationXRayRoomWaitFlag( 0 );

				// 患者のいる位置を検査室に変更します。
				erPAgent.vSetLocation( 10 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
//				erPAgent.vSetDoctorAgent( 0 );

				// 検査室へ患者エージェントを移動させます。
				ArrayListExaminationXRayRooms.get(i).vSetPatientAgent( erPAgent );

				// 医療技師エージェントへ患者情報を送信します。
				erExaminationClinicalEngineerAgent = ArrayListExaminationXRayRooms.get(i).cGetCurrentClinicalEngineerAgent();
				erConsultationDoctorAgentData.vSendToEngineerAgentMessage( erPAgent, erExaminationClinicalEngineerAgent, (int)erConsultationDoctorAgentData.getId(), (int)erExaminationClinicalEngineerAgent.getId() );

				// もしも-2を指定した場合、看護師がまだ担当していない患者が移動するため、ここでは患者看護師対応表は参照しません。
				if( iLoc > -2 )
				{
					// 対応を受けた患者エージェントを削除します。
					vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
					ArrayListNursePatientLoc.set( iLoc, -1 );
				}

				cWaitingRoomLog.info(erPAgent.getId() + "," + "X線室へ移動準備終了" + "," + "待合室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListExaminationXRayRooms.get(i).erGetTriageNode() ) );
					cWaitingRoomLog.info(erPAgent.getId() + "," + "X線室へ移動開始" + "," + "待合室");
				}
				erPAgent = null;

				// X線室の主担当医療技師が対応開始したと設定します。
				ArrayListExaminationXRayRooms.get(i).cGetCurrentClinicalEngineerAgent().vSetAttending(1);

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListExaminationXRayRooms.size() )
		{
			// 空きがない場合は患者の検査室待ちフラグをONにして、待合室に移動します。
			erPAgent.vSetExaminationXRayRoomWaitFlag( 1 );
		}
	}

	/**
	 * <PRE>
	 *    CT室へ移動可能かどうかを判定し、移動できる場合はCT室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListExaminationCTRooms	全CT室
	 * @param erWaitingRoom					待合室オブジェクト
	 * @param erConsultationDoctorAgentData	医師オブジェクト
	 * @param erPAgent						患者オブジェクト
	 * @param iLoc							担当看護師の番号
	 * @throws NullPointerException			NULLアドレスアクセス例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveExaminationCTRoom( ArrayList<ERExaminationCTRoom> ArrayListExaminationCTRooms, ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgentData, ERPatientAgent erPAgent, int iLoc ) throws NullPointerException
	{
		int i;
		int iJudgeCount = 0;
		ERClinicalEngineerAgent erExaminationClinicalEngineerAgent;
		for( i = 0;i < ArrayListExaminationCTRooms.size(); i++ )
		{
			// 検査室に空きがある場合は検査室へエージェントを移動します。
			if( ArrayListExaminationCTRooms.get(i).isVacant() == true )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "CT室へ移動準備開始" + "," + "待合室");
				// 検査室待ちフラグをOFFにします。
				erPAgent.vSetExaminationCTRoomWaitFlag( 0 );

				// 患者のいる位置を検査室に変更します。
				erPAgent.vSetLocation( 11 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
//				erPAgent.vSetDoctorAgent( 0 );

				// 検査室へ患者エージェントを移動させます。
				ArrayListExaminationCTRooms.get(i).vSetPatientAgent( erPAgent );

				// 医療技師エージェントへ患者情報を送信します。
				erExaminationClinicalEngineerAgent = ArrayListExaminationCTRooms.get(i).cGetCurrentClinicalEngineerAgent();
				erConsultationDoctorAgentData.vSendToEngineerAgentMessage( erPAgent, erExaminationClinicalEngineerAgent, (int)erConsultationDoctorAgentData.getId(), (int)erExaminationClinicalEngineerAgent.getId() );

				// もしも-2を指定した場合、看護師がまだ担当していない患者が移動するため、ここでは患者看護師対応表は参照しません。
				if( iLoc > -2 )
				{
					// 対応を受けた患者エージェントを削除します。
					vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
					ArrayListNursePatientLoc.set( iLoc, -1 );
				}

				cWaitingRoomLog.info(erPAgent.getId() + "," + "CT室へ移動準備完了" + "," + "待合室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					cWaitingRoomLog.info(erPAgent.getId() + "," + "CT室へ移動開始" + "," + "待合室");
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListExaminationCTRooms.get(i).erGetTriageNode() ) );
				}

				// CT室の主担当医療技師が対応開始したと設定します。
				ArrayListExaminationCTRooms.get(i).cGetCurrentClinicalEngineerAgent().vSetAttending(1);

				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListExaminationCTRooms.size() )
		{
			// 空きがない場合は患者の検査室待ちフラグをONにして、待合室に移動します。
			erPAgent.vSetExaminationCTRoomWaitFlag( 1 );
		}
	}

	/**
	 * <PRE>
	 *    MRI室へ移動可能かどうかを判定し、移動できる場合はMRI室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListExaminationMRIRooms	全MRI室
	 * @param erWaitingRoom					待合室
	 * @param erConsultationDoctorAgentData	医師エージェント
	 * @param erPAgent						患者エージェント
	 * @param iLoc							担当した看護師エージェントと患者エージェントの番号
	 * @throws NullPointerException			nullアドレスアクセス例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveExaminationMRIRoom( ArrayList<ERExaminationMRIRoom> ArrayListExaminationMRIRooms, ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgentData, ERPatientAgent erPAgent, int iLoc ) throws NullPointerException
	{
		int i;
		int iJudgeCount = 0;
		ERClinicalEngineerAgent erExaminationClinicalEngineerAgent;
		for( i = 0;i < ArrayListExaminationMRIRooms.size(); i++ )
		{
			// 検査室に空きがある場合は検査室へエージェントを移動します。
			if( ArrayListExaminationMRIRooms.get(i).isVacant() == true )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "MRI室へ移動準備開始" + "," + "待合室");
				// 検査室待ちフラグをOFFにします。
				erPAgent.vSetExaminationMRIRoomWaitFlag( 0 );

				// 患者のいる位置を検査室に変更します。
				erPAgent.vSetLocation( 12 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
//				erPAgent.vSetDoctorAgent( 0 );

				// 検査室へ患者エージェントを移動させます。
				ArrayListExaminationMRIRooms.get(i).vSetPatientAgent( erPAgent );

				// 医療技師エージェントへ患者情報を送信します。
				erExaminationClinicalEngineerAgent = ArrayListExaminationMRIRooms.get(i).cGetCurrentClinicalEngineerAgent();
				erConsultationDoctorAgentData.vSendToEngineerAgentMessage( erPAgent, erExaminationClinicalEngineerAgent, (int)erConsultationDoctorAgentData.getId(), (int)erExaminationClinicalEngineerAgent.getId() );

				// もしも-2を指定した場合、看護師がまだ担当していない患者が移動するため、ここでは患者看護師対応表は参照しません。
				if( iLoc > -2 )
				{
					// 対応を受けた患者エージェントを削除します。
					vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
					ArrayListNursePatientLoc.set( iLoc, -1 );
				}

				cWaitingRoomLog.info(erPAgent.getId() + "," + "MRI室へ移動準備終了" + "," + "待合室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					cWaitingRoomLog.info(erPAgent.getId() + "," + "MRI室へ移動開始" + "," + "待合室");
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListExaminationMRIRooms.get(i).erGetTriageNode() ) );
				}
				// MRI室の主担当医療技師が対応開始したと設定します。
				ArrayListExaminationMRIRooms.get(i).cGetCurrentClinicalEngineerAgent().vSetAttending(1);

				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListExaminationMRIRooms.size() )
		{
			// 空きがない場合は患者の検査室待ちフラグをONにして、待合室に移動します。
			erPAgent.vSetExaminationMRIRoomWaitFlag( 1 );
		}
	}

	/**
	 * <PRE>
	 *    血管造影室へ移動可能かどうかを判定し、移動できる場合は血管造影室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListExaminationAngiographyRooms	全血管造影室
	 * @param erWaitingRoom							待合室オブジェクト
	 * @param erConsultationDoctorAgentData			医師オブジェクト
	 * @param erPAgent								患者オブジェクト
	 * @param iLoc									担当した看護師エージェントと患者エージェントの番号
	 * @throws NullPointerException					NULLアドレスアクセス例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveExaminationAngiographyRoom( ArrayList<ERExaminationAngiographyRoom> ArrayListExaminationAngiographyRooms, ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgentData, ERPatientAgent erPAgent, int iLoc ) throws NullPointerException
	{
		int i;
		int iJudgeCount = 0;
		ERClinicalEngineerAgent erExaminationClinicalEngineerAgent;
		for( i = 0;i < ArrayListExaminationAngiographyRooms.size(); i++ )
		{
			// 検査室に空きがある場合は検査室へエージェントを移動します。
			if( ArrayListExaminationAngiographyRooms.get(i).isVacant() == true )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "血管造影室へ移動" + "," + "待合室");
				// 検査室待ちフラグをOFFにします。
				erPAgent.vSetExaminationAngiographyRoomWaitFlag( 0 );

				// 患者のいる位置を検査室に変更します。
				erPAgent.vSetLocation( 13 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
//				erPAgent.vSetDoctorAgent( 0 );

				// 検査室へ患者エージェントを移動させます。
				ArrayListExaminationAngiographyRooms.get(i).vSetPatientAgent( erPAgent );

				// 医療技師エージェントへ患者情報を送信します。
				erExaminationClinicalEngineerAgent = ArrayListExaminationAngiographyRooms.get(i).cGetCurrentClinicalEngineerAgent();
				erConsultationDoctorAgentData.vSendToEngineerAgentMessage( erPAgent, erExaminationClinicalEngineerAgent, (int)erConsultationDoctorAgentData.getId(), (int)erExaminationClinicalEngineerAgent.getId() );

				// 血管造影室の主担当医療技師が対応開始したと設定します。
				ArrayListExaminationAngiographyRooms.get(i).cGetCurrentClinicalEngineerAgent().vSetAttending(1);

				// もしも-2を指定した場合、看護師がまだ担当していない患者が移動するため、ここでは患者看護師対応表は参照しません。
				if( iLoc > -2 )
				{
					// 対応を受けた患者エージェントを削除します。
					vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
					ArrayListNursePatientLoc.set( iLoc, -1 );
				}

				cWaitingRoomLog.info(erPAgent.getId() + "," + "血管造影室へ移動準備終了" + "," + "待合室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					cWaitingRoomLog.info(erPAgent.getId() + "," + "血管造影室へ移動開始" + "," + "待合室");
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListExaminationAngiographyRooms.get(i).erGetTriageNode() ) );
				}
				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListExaminationAngiographyRooms.size() )
		{
			// 空きがない場合は患者の検査室待ちフラグをONにして、待合室に移動します。
			erPAgent.vSetExaminationAngiographyRoomWaitFlag( 1 );
		}
	}

	/**
	 * <PRE>
	 *    FAST室へ移動可能かどうかを判定し、移動できる場合はFAST室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListExaminationFastRooms	 全FAST室
	 * @param erWaitingRoom     			 待合室
	 * @param erConsultationDoctorAgentData  担当医師エージェント
	 * @param erPAgent           			 患者エージェント
	 * @param iLoc							 担当した看護師エージェントと患者エージェントの番号
	 * @throws NullPointerException			 NULLアクセス例外
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveExaminationFastRoom( ArrayList<ERExaminationFastRoom> ArrayListExaminationFastRooms, ERWaitingRoom erWaitingRoom, ERDoctorAgent erConsultationDoctorAgentData, ERPatientAgent erPAgent, int iLoc )  throws NullPointerException
	{
		int i;
		int iJudgeCount = 0;
		ERClinicalEngineerAgent erExaminationClinicalEngineerAgent;
		for( i = 0;i < ArrayListExaminationFastRooms.size(); i++ )
		{
			// 検査室に空きがある場合は検査室へエージェントを移動します。
			if( ArrayListExaminationFastRooms.get(i).isVacant() == true )
			{
				cWaitingRoomLog.info(erPAgent.getId() + "," + "血管造影室へ移動" + "," + "診察室");
				// 検査室待ちフラグをOFFにします。
				erPAgent.vSetExaminationFastRoomWaitFlag( 0 );

				// 患者のいる位置を検査室に変更します。
				erPAgent.vSetLocation( 14 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
//				erPAgent.vSetDoctorAgent( 0 );

				// 検査室へ患者エージェントを移動させます。
				ArrayListExaminationFastRooms.get(i).vSetPatientAgent( erPAgent );

				// 医療技師エージェントへ患者情報を送信します。
				erExaminationClinicalEngineerAgent = ArrayListExaminationFastRooms.get(i).cGetCurrentClinicalEngineerAgent();
				erConsultationDoctorAgentData.vSendToEngineerAgentMessage( erPAgent, erExaminationClinicalEngineerAgent, (int)erConsultationDoctorAgentData.getId(), (int)erExaminationClinicalEngineerAgent.getId() );

				// 血管造影室の主担当医療技師が対応開始したと設定します。
				ArrayListExaminationFastRooms.get(i).cGetCurrentClinicalEngineerAgent().vSetAttending(1);

				// もしも-2を指定した場合、看護師がまだ担当していない患者が移動するため、ここでは患者看護師対応表は参照しません。
				if( iLoc > -2 )
				{
					// 対応を受けた患者エージェントを削除します。
					vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
					ArrayListNursePatientLoc.set( iLoc, -1 );
				}

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListExaminationFastRooms.get(i).erGetTriageNode() ) );
				}
				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListExaminationFastRooms.size() )
		{
			// 空きがない場合は患者の検査室待ちフラグをONにして、待合室に移動します。
			erPAgent.vSetExaminationFastRoomWaitFlag( 1 );
		}
	}

	/**
	 * <PRE>
	 *    病院外への移動を実施します。
	 * </PRE>
	 * @param erOutside						病院外エージェント
	 * @param erNurseAgent					看護師エージェント
	 * @param erPAgent						患者エージェント
	 * @param iLoc							看護師がどの患者を対応していたのか
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	private void vJudgeMoveOutside( EROutside erOutside, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc )
	{
		int i,j;
		int iJudgeCount = 0;

		// 患者エージェントを病院外へ移動させます。
		cWaitingRoomLog.info(erPAgent.getId() + "," + "病院外へ移動準備開始" + "," + "待合室");

		// 患者のいる位置を病院外に変更します。
		erPAgent.vSetLocation( -1 );

		// 観察フラグをOFFにします。
		erPAgent.vSetObservedFlag( 0 );

		// 待合室での看護師に見てもらったフラグはOFFにします。
		erPAgent.vSetNurseAttended( 0 );

		// 移動開始フラグを設定します。
		erPAgent.vSetMoveRoomFlag( 1 );
		erPAgent.vSetMoveWaitingTime( 0.0 );

		erOutside.vSetPatientAgent( erPAgent );

//		// もしも-2を指定した場合、看護師がまだ担当していない患者が移動するため、ここでは患者看護師対応表は参照しません。
//		if( iLoc > -2 )
//		{
//			// 対応を受けた患者エージェントを削除します。
//
//			ArrayListNursePatientLoc.set( iLoc, -1 );
//		}

		// その患者を対応している看護師エージェントがいなくなるので0に設定します。
		erPAgent.vSetNurseAgent( 0 );

		if( erNurseAgent != null )
		{
			// 看護師エージェントの対応を終了します。
			erNurseAgent.vSetAttending( 0 );
		}

		cWaitingRoomLog.info(erPAgent.getId() + "," + "病院外へ移動準備終了" + "," + "待合室");
		if( iInverseSimFlag == 1 )
		{
			// 移動先の経路を患者エージェントに設定します。
			cWaitingRoomLog.info(erPAgent.getId() + "," + "病院外へ移動開始" + "," + "待合室");
			erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), erOutside.erGetTriageNode() ) );
		}
		erPAgent = null;
	}

	/**
	 * <PRE>
	 *    担当患者エージェントを設定します。
	 * </PRE>
	 * @param erPAgent 患者エージェント
	 */
	public void vSetPatientAgent( ERPatientAgent erPAgent )
	{
		ArrayListPatientAgents.add( erPAgent );
//		erCurrentPatientAgent = erPAgent;
	}

	/**
	 * <PRE>
	 *    対応が終了した患者を削除します。
	 * </PRE>
	 * @param erPAgent 	患者エージェント
	 * @param iLocation 対応する看護師番号
	 */
	public void vRemovePatientAgent( ERPatientAgent erPAgent, int iLocation )
	{
		int i;

		synchronized( csWaitingRoomCriticalSection )
		{
			ArrayListPatientAgents.remove( erPAgent );
			for( i = 0;i < ArrayListNursePatientLoc.size(); i++ )
			{
				if( iLocation < ArrayListNursePatientLoc.get(i) )
				{
					ArrayListNursePatientLoc.set(i, ArrayListNursePatientLoc.get(i)-1 );
				}
			}
		}
	}

	/**
	 * <PRE>
	 *    対応が終了した患者を削除します。
	 * </PRE>
	 * @param erPAgent 患者エージェント
	 */
	public void vRemovePatientAgent( ERPatientAgent erPAgent )
	{
		synchronized( csWaitingRoomCriticalSection )
		{
			ArrayListPatientAgents.remove( erPAgent );
		}
	}

	/**
	 * <PRE>
	 *   初療室の看護師エージェントを取得します。
	 * </PRE>
	 * @param iLoc 所属している看護師の番号
	 * @return	番号に該当する看護師エージェントのインスタンス
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public ERNurseAgent erGetNurseAgent( int iLoc )
	{
		return ArrayListNurseAgents.get(iLoc);
	}

	/**
	 * <PRE>
	 *   初療室の看護師エージェントの数を取得します。
	 * </PRE>
	 * @return	看護師エージェント数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public int iGetNurseAgentsNum()
	{
		return ArrayListNurseAgents.size();
	}

	/**
	 * <PRE>
	 *   待合室の患者エージェントを取得します。
	 * </PRE>
	 * @return	患者エージェントの全インスタンス
	 * @author kobayashi
	 * @since 2015/10/29
	 */
	public ArrayList<ERPatientAgent> erGetPatientAgents()
	{
		return ArrayListPatientAgents;
	}

	/**
	 * <PRE>
	 *   待合室の個々の患者エージェントを取得します。
	 * </PRE>
	 * @param iLoc 番号に該当する患者エージェント
	 * @return	該当する患者エージェントのインスタンス
	 * @author kobayashi
	 * @since 2015/10/29
	 */
	public ERPatientAgent erGetPatientAgent( int iLoc )
	{
		return ArrayListPatientAgents.get(iLoc);
	}

	/**
	 * <PRE>
	 *   初療室の看護師エージェント番号を患者番号から取得します。
	 * </PRE>
	 * @param iTargetLoc 患者エージェントの番号
	 * @return	看護師エージェント番号
	 * @author kobayashi
	 * @since 2015/11/09
	 */
	public int iGetNurseAgentPatientLoc( int iTargetLoc )
	{
		int i;
		int iLoc = -1;
		for( i = 0;i < ArrayListNursePatientLoc.size(); i++ )
		{
			if( ArrayListNursePatientLoc.get(i) == iTargetLoc )
			{
				iLoc = i;
			}
		}
		return iLoc;
	}

	/**
	 * <PRE>
	 *    待合室看護師が対応中かどうかを判定します。
	 * </PRE>
	 * @return false 全員対応している
	 *         true  空きの看護師がいる
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public boolean isVacant()
	{
		int i;
		int iCount = 0;
		boolean bRet = true;
		// 室としては存在しているが、看護師がいない場合は対応できないため、空いていないとします。
		// 通常はないはずだが・・・。
		if( ArrayListNurseAgents == null )
		{
			bRet = false;
		}
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			// 所属看護師が全員対応中の場合、空いていないとします。
			if( ArrayListNurseAgents.get(i).iGetAttending() == 1 )
			{
				iCount++;
			}
		}
		if( iCount == ArrayListNurseAgents.size() )
		{
			bRet = false;
		}
		bRet= true;
		return bRet;
	}

	/**
	 * <PRE>
	 *     他室へ移動する場合に最も重傷度あるいは緊急度が高い患者がどの部屋に在院しているのかを判定します。
	 *     対象は待合室、観察室、重傷観察室。
	 * </PRE>
	 *
	 * @param iJudgeFlag								判定方法
	 * 													0 AISの値
	 * 													1 緊急度レベル
	 * @param erPAgent									移動の対象となる患者エージェント
	 * @param ArrayListSevereInjuryObservationRooms		重症観察室
	 * @param ArrayListObservationRooms					前観察室
	 * @return	0	移動先待合室
	 * 			1	移動先重症観察室
	 * 			2	移動先観察室
	 * @author kobayashi
	 * @since 2015/11/05
	 */
	public int iJudgeEmergencyPatient( int iJudgeFlag, ERPatientAgent erPAgent, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms )
	{
		int i,j;
		int iTargetLoc = -1;
		int iMinEmergencyLevel = 6;
		double lfMaxAISLevel = 6.0;
		ERPatientAgent erTempAgent;

		if( erPAgent == null ) return -1;

		cWaitingRoomLog.info(erPAgent.getId() + "," + "患者部屋変更判定関数通ったよ～。");
		if( iJudgeFlag == 0 )
		{
			lfMaxAISLevel = erPAgent.lfGetMaxAIS();
			// 移動予定の患者よりも緊急度の高い患者がいないかどうかを確認します。
			for( i = 0;i < ArrayListPatientAgents.size(); i++ )
			{
				// 患者が大量になくなられたときの対処
				if( ArrayListPatientAgents.size() <= i ) break;
				if( ArrayListPatientAgents.get(i) == null ) continue;

				// 到達した患者のみを対象とします。
				erTempAgent = ArrayListPatientAgents.get(i);
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( erTempAgent.lfGetTimeCourse() > 0.0 )
				{
					if( lfMaxAISLevel <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
					{
						cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "iJudgeEmergencyPatient" + "," + "待合室にいるみたいだよ。");
						lfMaxAISLevel = erTempAgent.lfGetMaxAIS();
						iTargetLoc = 0;
					}
				}
			}
			for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					// 患者が大量になくなられたときの対処
					if( ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgents().size() <= i ) break;
					if( ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgent(j) == null ) continue;

					erTempAgent = ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgent(j);
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( lfMaxAISLevel <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
					{
						cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "iJudgeEmergencyPatient" + "," + "重症観察室にいるみたいだよ。");
						lfMaxAISLevel = erTempAgent.lfGetMaxAIS();
						iTargetLoc = 1;
					}
				}
			}
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					// 患者が大量になくなられたときの対処
					if( ArrayListObservationRooms.get(i).erGetPatientAgents().size() <= i ) break;
					if( ArrayListObservationRooms.get(i).erGetPatientAgent(j) == null ) continue;
					erTempAgent = ArrayListObservationRooms.get(i).erGetPatientAgent(j);
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( lfMaxAISLevel <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
					{
						cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "iJudgeEmergencyPatient" + "," + "観察室にいるみたいだよ。");
						lfMaxAISLevel = erTempAgent.lfGetMaxAIS();
						iTargetLoc = 2;
					}
				}
			}
		}
		else if( iJudgeFlag == 1 )
		{
			iMinEmergencyLevel = erPAgent.iGetEmergencyLevel();
			// 移動予定の患者よりも緊急度の高い患者がいないかどうかを確認します。
			for( i = 0;i < ArrayListPatientAgents.size(); i++ )
			{
				// 患者が大量になくなられたときの対処
				if( ArrayListPatientAgents.size() <= i ) break;
				if( ArrayListPatientAgents.get(i) == null ) continue;

				// 到達した患者のみを対象とします。
				erTempAgent = ArrayListPatientAgents.get(i);
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( erTempAgent.lfGetTimeCourse() > 0.0 )
				{
					if( iMinEmergencyLevel >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId())
					{
						cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "iJudgeEmergencyPatient" + "," + "待合室にいるみたいだよ。");
						iMinEmergencyLevel = erTempAgent.iGetEmergencyLevel();
						iTargetLoc = 0;
					}
				}
			}
			for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					// 患者が大量になくなられたときの対処
					if( ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgents().size() <= i ) break;
					if( ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgent(j) == null ) continue;

					erTempAgent = ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgent(j);
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( iMinEmergencyLevel >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId())
					{
						cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "iJudgeEmergencyPatient" + "," + "重症観察室にいるみたいだよ。");
						iMinEmergencyLevel = erTempAgent.iGetEmergencyLevel();
						iTargetLoc = 1;
					}
				}
			}
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					// 患者が大量になくなられたときの対処
					if( ArrayListObservationRooms.get(i).erGetPatientAgents().size() <= i ) break;
					if( ArrayListObservationRooms.get(i).erGetPatientAgent(j) == null ) continue;

					erTempAgent = ArrayListObservationRooms.get(i).erGetPatientAgent(j);
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( iMinEmergencyLevel >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId())
					{
						cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "iJudgeEmergencyPatient" + "," + "観察室にいるみたいだよ。");
						iMinEmergencyLevel = erTempAgent.iGetEmergencyLevel();
						iTargetLoc = 2;
					}
				}
			}
		}
		return iTargetLoc;
	}


	/**
	 * <PRE>
	 *     初療室へ移動する場合にあまりにも長い時間待たせている患者がいるかどうかを判定します。
	 *     対象は待合室、観察室、重傷観察室。
	 * </PRE>
	 *
	 * @param erPAgent									移動の対象となる患者エージェント
	 * @param ArrayListSevereInjuryObservationRooms		全重症観察室
	 * @param ArrayListObservationRooms					全観察室
	 * @return											移動する部屋
	 * 													0 待合室
	 * 													1 重症観察室
	 * 													2 観察室
	 * @author kobayashi
	 * @since 2015/11/05
	 */
	public int iJudgeWaitTimeEmergencyPatient( ERPatientAgent erPAgent, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms )
	{
		int i,j;
		int iTargetLoc = -1;
		double lfMaxWaitTime = 6.0;
		ERPatientAgent erTempAgent;

		if( erPAgent == null ) return -1;

		cWaitingRoomLog.info(erPAgent.getId() + "," + "患者部屋変更判定関数通ったよ～。");
		lfMaxWaitTime = erPAgent.lfGetWaitTime();
		// 移動予定の患者よりも緊急度の高い患者がいないかどうかを確認します。
		for( i = 0;i < ArrayListPatientAgents.size(); i++ )
		{
			// 患者が大量になくなられたときの対処
			if( ArrayListPatientAgents.size() <= i ) break;
			if( ArrayListPatientAgents.get(i) == null ) continue;

			// 到達した患者のみを対象とします。
			erTempAgent = ArrayListPatientAgents.get(i);
			if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
			if( erTempAgent.lfGetTimeCourse() > 0.0 )
			{
				if( lfMaxWaitTime <= erTempAgent.lfGetWaitTime() && erPAgent.getId() != erTempAgent.getId())
				{
					cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "iJudgeWaitTimeEmergencyPatient" + "," + "待合室にいるみたいだよ。");
					lfMaxWaitTime = erTempAgent.lfGetWaitTime();
					iTargetLoc = 0;
				}
			}
		}
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgents().size(); j++ )
			{
				// 患者が大量になくなられたときの対処
				if( ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgents().size() <= i ) break;
				if( ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgent(j) == null ) continue;

				erTempAgent = ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgent(j);
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( lfMaxWaitTime <= erTempAgent.lfGetObservationTime() && erPAgent.getId() != erTempAgent.getId())
				{
					cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "iJudgeWaitTimeEmergencyPatient" + "," + "重症観察室にいるみたいだよ。");
					lfMaxWaitTime = erTempAgent.lfGetObservationTime();
					iTargetLoc = 1;
				}
			}
		}
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
			{
				// 患者が大量になくなられたときの対処
				if( ArrayListObservationRooms.get(i).erGetPatientAgents().size() <= i ) break;
				if( ArrayListObservationRooms.get(i).erGetPatientAgent(j) == null ) continue;

				erTempAgent = ArrayListObservationRooms.get(i).erGetPatientAgent(j);
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( lfMaxWaitTime <= erTempAgent.lfGetObservationTime() && erPAgent.getId() != erTempAgent.getId())
				{
					cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "iJudgeWaitTimeEmergencyPatient" + "," + "観察室にいるみたいだよ。");
					lfMaxWaitTime = erTempAgent.lfGetObservationTime();
					iTargetLoc = 2;
				}
			}
		}
		return iTargetLoc;
	}

	/**
	 * <PRE>
	 *     診察室へ移動する場合に最も重傷度あるいは緊急度が高い患者がどの部屋に在院しているのかを判定します。
	 *     対象は待合室、観察室、重傷観察室。
	 * </PRE>
	 * @param iJudgeFlag					緊急度のモード0:AIS,1:トリアージ緊急度
	 * @param erPAgent						待合室の患者エージェント
	 * @param ArrayListObservationRooms		全観察室
	 * @param erWaitingRoom					待合室
	 * @return 0 待合室
	 *         1 観察室
	 * @author kobayashi
	 * @since 2015/11/05
	 */
	public int iJudgeConsultationPatient( int iJudgeFlag, ERPatientAgent erPAgent, ArrayList<ERObservationRoom> ArrayListObservationRooms, ERWaitingRoom erWaitingRoom )
	{
		int i,j;
		int iTargetLoc = -1;
		int iLoc = 0;
		int iMinEmergencyLevel = 6;
		double lfMaxAISLevel = 6.0;
		ERPatientAgent erTempAgent;

		if( erPAgent == null ) return -1;

		cWaitingRoomLog.info(erPAgent.getId() + "," + "患者部屋変更判定関数通ったよ～。");
		if( iJudgeFlag == 0 )
		{
			lfMaxAISLevel = erPAgent.lfGetMaxAIS();
			// 移動予定の患者よりも緊急度の高い患者がいないかどうかを確認します。
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					// 患者が大量になくなられたときの対処
					if( ArrayListObservationRooms.get(i).erGetPatientAgents().size() <= i ) break;
					if( ArrayListObservationRooms.get(i).erGetPatientAgent(j) == null ) continue;
					erTempAgent = ArrayListObservationRooms.get(i).erGetPatientAgent(j);
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( lfMaxAISLevel <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
					{
						iLoc = ArrayListObservationRooms.get(i).iGetNurseAgentPatientLoc( i );
						if( iLoc != -1 )
						{
							cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxAISLevel + "," + "iJudgeConsultationPatient" + "," + "観察室にいるみたいだよ。");
							lfMaxAISLevel = erTempAgent.lfGetMaxAIS();
							iTargetLoc = 1;
						}
					}
				}
			}
			for( i = 0;i < erWaitingRoom.erGetPatientAgents().size(); i++ )
			{
				// 患者が大量になくなられたときの対処
				if( erWaitingRoom.erGetPatientAgents().size() <= i ) break;
				if( erWaitingRoom.erGetPatientAgent(i) == null ) continue;
				erTempAgent = erWaitingRoom.erGetPatientAgent(i);
				if( erWaitingRoom.erGetPatientAgent(i).lfGetTimeCourse() > 0.0 )
				{
					if( lfMaxAISLevel <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
					{
						// 移動中の患者さんは対象としません。
						if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
						// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
						if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
							erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
							erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
							erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
						{
							cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxAISLevel + "," + "iJudgeConsultationPatient" + "," + "待合室にいるみたいだよ。");
							lfMaxAISLevel = erTempAgent.lfGetMaxAIS();
							iTargetLoc = 0;
						}
					}
				}
			}
		}
		else if( iJudgeFlag == 1 )
		{
			iMinEmergencyLevel = erPAgent.iGetEmergencyLevel();
			// 移動予定の患者よりも緊急度の高い患者がいないかどうかを確認します。
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					// 患者が大量になくなられたときの対処
					if( ArrayListObservationRooms.get(i).erGetPatientAgents().size() <= i ) break;
					if( ArrayListObservationRooms.get(i).erGetPatientAgent(j) == null ) continue;
					erTempAgent = ArrayListObservationRooms.get(i).erGetPatientAgent(j);
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( iMinEmergencyLevel >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId())
					{
						iLoc = ArrayListObservationRooms.get(i).iGetNurseAgentPatientLoc( i );
						if( iLoc != -1 )
						{
							cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + iMinEmergencyLevel + "," + "iJudgeConsultationPatient" + "," + "観察室にいるみたいだよ。");
							iMinEmergencyLevel = erTempAgent.iGetEmergencyLevel();
							iTargetLoc = 1;
						}
					}
				}
			}
			for( i = 0;i < erWaitingRoom.erGetPatientAgents().size(); i++ )
			{
				// 患者が大量になくなられたときの対処
				if( erWaitingRoom.erGetPatientAgents().size() <= i ) break;
				if( erWaitingRoom.erGetPatientAgent(i) == null ) continue;
				erTempAgent = erWaitingRoom.erGetPatientAgent(i);
				if( erWaitingRoom.erGetPatientAgent(i).lfGetTimeCourse() > 0.0 )
				{
					if( iMinEmergencyLevel >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId())
					{
						// 移動中の患者さんは対象としません。
						if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
						// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
						if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
							erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
							erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
							erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
						{
							cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + iMinEmergencyLevel + "," + "iJudgeConsultationPatient" + "," + "待合室にいるみたいだよ。");
							iMinEmergencyLevel = erTempAgent.iGetEmergencyLevel();
							iTargetLoc = 0;
							int iRes;
							iRes = erWaitingRoom.iGetNurseAgentPatientLoc(iTargetLoc);
							iRes = 10;
						}
					}
				}
			}
		}
		return iTargetLoc;
	}

	/**
	 * <PRE>
	 *     診察室へ移動する場合にあまりにも長い時間待たせている患者がいるかどうかを判定します。
	 *     対象は待合室、観察室。
	 * </PRE>
	 *
	 * @param erPAgent						移動する対象の患者エージェント
	 * @param ArrayListObservationRooms		全観察室
	 * @param erWaitingRoom					待合室
	 * @return 0 待合室
	 *         1 観察室
	 * @author kobayashi
	 * @since 2015/11/05
	 */
	public int iJudgeWaitTimeConsultationPatient( ERPatientAgent erPAgent, ArrayList<ERObservationRoom> ArrayListObservationRooms, ERWaitingRoom erWaitingRoom )
	{
		int i,j;
		int iTargetLoc = -1;
		int iLoc = 0;
		int iMinEmergencyLevel = 6;
		double lfMaxWaitTime = 3600*2;		// ここでは仮に長時間待たせられると不満が出てくると統計的に言われている2時間を設定します。
		ERPatientAgent erTempAgent;

		if( erPAgent == null ) return -1;

		cWaitingRoomLog.info(erPAgent.getId() + "," + "iJudgeWaitTimeConsultationPatient" + "," + "患者部屋変更判定関数通ったよ～。");

		// 移動予定の患者よりも長時間待っている患者ががいないかどうかを確認します。
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
			{
				// 患者が大量になくなられたときの対処
				if( ArrayListObservationRooms.get(i).erGetPatientAgents().size() <= i ) break;
				if( ArrayListObservationRooms.get(i).erGetPatientAgent(j) == null ) continue;

				erTempAgent = ArrayListObservationRooms.get(i).erGetPatientAgent(j);
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( lfMaxWaitTime <= erTempAgent.lfGetObservationTime() && erPAgent.getId() != erTempAgent.getId())
				{
					iLoc = ArrayListObservationRooms.get(i).iGetNurseAgentPatientLoc( i );
					if( iLoc != -1 )
					{
						cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxWaitTime + "," + "iJudgeWaitTimeConsultationPatient" + "," + "観察室にいるみたいだよ。");
						lfMaxWaitTime = erTempAgent.lfGetObservationTime();
						iTargetLoc = 1;
					}
				}
			}
		}
		for( i = 0;i < erWaitingRoom.erGetPatientAgents().size(); i++ )
		{
			// 患者が大量になくなられたときの対処
			if( erWaitingRoom.erGetPatientAgents().size() <= i ) break;
			if( erWaitingRoom.erGetPatientAgent(i) == null ) continue;

			erTempAgent = erWaitingRoom.erGetPatientAgent(i);
			if( erWaitingRoom.erGetPatientAgent(i).lfGetTimeCourse() > 0.0 )
			{
				if( lfMaxWaitTime <= erTempAgent.lfGetWaitTime() && erPAgent.getId() != erTempAgent.getId())
				{
					// 移動中の患者さんは対象としません。
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
					if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
						erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
					{
						cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxWaitTime + "," + "iJudgeWaitTimeConsultationPatient" + "," + "待合室にいるみたいだよ。");
						lfMaxWaitTime = erTempAgent.lfGetWaitTime();
						iTargetLoc = 0;
					}
				}
			}
		}
		return iTargetLoc;
	}

	/**
	 * <PRE>
	 *     重症観察室から、あるいは観察室から移動する患者エージェントが待合室に重症度の大きい患者がいないかどうかを判定し、
	 *     判定結果に基づいて部屋の変更を実施します。
	 * </PRE>
	 *
	 * @param erPAgent									移動対象の患者エージェント
	 * @param ArrayListEmergencyRooms					全初療室
	 * @param ArrayListSevereInjuryObservationRooms		全重症観察室
	 * @param ArrayListObservationRooms					全観察室
	 * @return											true 移動対象とする別の患者エージェントがいるため、そちらが移動する。
	 * 													false 移動しない。
	 * @author kobayashi
	 * @since 2015/11/05
	 */
	public boolean bChangeEmergencyRoomSevereInjuryObservationPatient( ERPatientAgent erPAgent, ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms )
	{
		int i,j;
		int iLoc = -1;
		int iTargetRoomLoc = -1;
		int iTargetPatientLoc = -1;
		int iTargetNursePatientLoc = -1;
		int iMaxEmergency = Integer.MAX_VALUE;
		double lfMaxAIS = -Double.MAX_VALUE;
		double lfMaxWaitTime = -Double.MAX_VALUE;
		int iJudgeCount = 0;
		int iJudgeCountSize = 0;

		ERPatientAgent erTempAgent		= null;
		ERPatientAgent erAgent			= null;
		ERNurseAgent erTempNurseAgent	= null;
		ERNurseAgent erEmergencyNurseAgent = null;
		ERDoctorAgent erEmergencyDoctorAgent = null;

		// 基本的にはないと思いますが、重傷観察室が全室空の場合は強制的に移動するはずなので、すぐに終了します。
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgents().size(); j++ )
			{
				if( ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgents().isEmpty() == true )
				{
					iJudgeCount++;
				}
				iJudgeCountSize++;
			}
		}
		if( iJudgeCount == iJudgeCountSize )
		{
			return false;
		}
		cWaitingRoomLog.info( erPAgent.getId() + "," + "患者部屋変更関数通ったよ～。");
		if( iJudgeUrgencyFlagMode == 1 )
		{
			// 重症観察室で最も重症度あるいは緊急度の高い患者を見つけます。
			for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					erTempAgent = ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgent( j );
					// 移動中は対象外とします。
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( iMaxEmergency >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId())
					{
						iLoc = ArrayListSevereInjuryObservationRooms.get( i ).iGetNurseAgentPatientLoc( j );
						if( iLoc != -1 )
						{
							cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxWaitTime + "," + "bChangeEmergencyRoomSevereInjuryObservationPatient" + "," + "重症観察室にいるみたいだよ。");
							iMaxEmergency = erTempAgent.iGetEmergencyLevel();
							lfMaxWaitTime = erTempAgent.lfGetObservationTime();
							iTargetRoomLoc = i;
							iTargetPatientLoc = j;
							iTargetNursePatientLoc = iLoc;
							erAgent = erTempAgent;
						}
					}
				}
			}
		}
		else
		{
			for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					erTempAgent = ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgent( j );
					// 移動中は対象外とします。
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( lfMaxAIS <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
					{
						iLoc = ArrayListSevereInjuryObservationRooms.get( i ).iGetNurseAgentPatientLoc( j );
						if( iLoc != -1 )
						{
							cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxWaitTime + "," + "bChangeEmergencyRoomSevereInjuryObservationPatient" + "," + "重症観察室にいるみたいだよ。");
							lfMaxAIS = erTempAgent.lfGetMaxAIS();
							lfMaxWaitTime = erTempAgent.lfGetObservationTime();
							iTargetRoomLoc = i;
							iTargetPatientLoc = j;
							iTargetNursePatientLoc = iLoc;
							erAgent = erTempAgent;
						}
					}
				}
			}
		}
		lfMaxWaitTime = -Double.MAX_VALUE;
		for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgents().size(); j++ )
			{
				erTempAgent = ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgent( j );
				// 2時間以上経過している場合は強制的に移動できるように設定します。
				if( erTempAgent.lfGetObservationTime() >= 3600*2 && lfMaxWaitTime <= erTempAgent.lfGetObservationTime() && erPAgent.getId() != erTempAgent.getId())
				{
					iLoc = ArrayListSevereInjuryObservationRooms.get( i ).iGetNurseAgentPatientLoc( j );
					if( iLoc != -1 )
					{
						cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxWaitTime + "," + "観察室にいるみたいだよ。");
						lfMaxWaitTime = erTempAgent.lfGetObservationTime();
						iTargetRoomLoc = i;
						iTargetPatientLoc = j;
						iTargetNursePatientLoc = iLoc;
						erAgent = erTempAgent;
					}
				}
			}
		}
		if( iTargetPatientLoc == -1 || iTargetNursePatientLoc == -1 )
		{
			// 基本的にはないとは思いますが、患者番号及び看護師番号に該当が存在しなかった場合は終了します。
			cWaitingRoomLog.info(erPAgent.getId() + "," + "うむむ該当者がいなかったぞよ～。bChangeEmergencyRoomSevereInjuryObservationPatient");
			return false;
		}
		erTempNurseAgent = ArrayListSevereInjuryObservationRooms.get( iTargetRoomLoc ).erGetNurseAgent( iTargetNursePatientLoc );
		erTempAgent = ArrayListSevereInjuryObservationRooms.get( iTargetRoomLoc ).erGetPatientAgent( iTargetPatientLoc );
		cWaitingRoomLog.info(erPAgent.getId() + "," + erAgent.getId() + "," + erTempAgent.getId() + ","  + "緊急度判定" + "," + "ターゲットの患者：" + iTargetPatientLoc + "," +  ArrayListSevereInjuryObservationRooms.get( iTargetRoomLoc ).erGetPatientAgents().size() );

	// 初療室へ移動します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			// 初療室に空きがある場合
			if( ArrayListEmergencyRooms.get(i).isVacant() == true )
			{
				// 初療室に空きがある場合
				cWaitingRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動準備開始" + "," + "重症観察室");

				// 初療室待機フラグをOFFにします。
				erTempAgent.vSetEmergencyRoomWaitFlag( 0 );

				// 患者のいる位置を初療室に変更します。
				erTempAgent.vSetLocation( 3 );

				// 観察フラグをOFFにします。
				erTempAgent.vSetObservedFlag( 0 );

				// 待合室での看護師に見てもらったフラグはOFFにします。
				erTempAgent.vSetNurseAttended( 0 );

				// 移動開始フラグを設定します。
				erTempAgent.vSetMoveRoomFlag( 1 );
				erTempAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している看護師エージェントがいなくなるので0に設定します。
				erTempAgent.vSetNurseAgent( 0 );

				// 初療室へ患者エージェントを移動します。
				ArrayListEmergencyRooms.get(i).vSetPatientAgent( erTempAgent );

			// 看護師、医師、技士エージェントへメッセージを送信します。
				// 初療室の看護師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erEmergencyNurseAgent = ArrayListEmergencyRooms.get(i).cGetNurseAgent(j);
					erTempNurseAgent.vSendToNurseAgentMessage( erTempAgent, (int)erTempNurseAgent.getId(), (int)erEmergencyNurseAgent.getId() );
				}
				// 初療室の医師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetDoctorAgentsNum(); j++ )
				{
					erEmergencyDoctorAgent = ArrayListEmergencyRooms.get(i).cGetDoctorAgent( j );
					erTempNurseAgent.vSendToDoctorAgentMessage( erTempAgent, erEmergencyDoctorAgent, (int)erTempNurseAgent.getId(), (int)erEmergencyDoctorAgent.getId() );
				}
//				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
//				{
//					erEmergencyCinicalEngineerAgent = ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgents(j);
//					erNurseAgent.vSendToEngineerAgentMessage( erTempAgent, (int)erNurseAgent.getId(), (int)erEmergencyClinicalEngineerAgent.getId() );
//				}

				// 看護師エージェントの対応を終了します。
				erTempNurseAgent.vSetAttending( 0 );

				// 対応を受けた患者エージェントを削除します。
				ArrayListSevereInjuryObservationRooms.get( iTargetRoomLoc ).vRemovePatientAgent( erTempAgent, iTargetPatientLoc );
				ArrayListSevereInjuryObservationRooms.get( iTargetRoomLoc ).vSetArrayListNursePatientLoc( iTargetNursePatientLoc, -1 );

				cWaitingRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動準備終了" + "," + "重症観察室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erTempAgent.vSetMoveRoute( erTriageNodeManager.getRoute( ArrayListSevereInjuryObservationRooms.get( iTargetRoomLoc ).erGetTriageNode(), ArrayListEmergencyRooms.get(i).erGetTriageNode() ) );
					cWaitingRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動開始" + "," + "重症観察室");
				}
				erTempAgent = null;

				// 初療室で担当する医師エージェントを設定します。
				ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetSurgeon(1);
				ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetAttending(1);
				break;
			}
		}
		return true;
	}

	/**
	 * <PRE>
	 *     観察室から初療室へ移動する対象患者エージェントよりも
	 *     待合室に重症度の大きい患者がいないかどうかを判定し、
	 *     判定結果に基づいて部屋の変更を実施します。
	 * </PRE>
	 *
	 * @param erPAgent									移動対象の患者エージェント
	 * @param ArrayListEmergencyRooms					全初療室
	 * @param ArrayListObservationRooms					全観察室
	 * @param ArrayListSevereInjuryObservationRooms		全重症観察室
	 * @param erWaitingRoom								待合室
	 * @return											true 移動対象とする別の患者エージェントがいるため、そちらが移動する。
	 * 													false 移動しない。
	 * @author kobayashi
	 * @since 2015/11/05
	 */
	public boolean bChangeEmergencyRoomObservationPatient( ERPatientAgent erPAgent, ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms, ERWaitingRoom erWaitingRoom )
	{
		int i,j;
		int iLoc = -1;
		int iTargetRoomLoc = -1;
		int iTargetPatientLoc = -1;
		int iTargetNursePatientLoc = -1;
		int iMaxEmergency = Integer.MAX_VALUE;
		double lfMaxAIS = -Double.MAX_VALUE;
		double lfMaxWaitTime = -Double.MAX_VALUE;
		int iJudgeCount = 0;
		int iJudgeCountSize = 0;

		ERPatientAgent erTempAgent		= null;
		ERPatientAgent erAgent			= null;
		ERNurseAgent erTempNurseAgent	= null;
		ERNurseAgent erEmergencyNurseAgent = null;
		ERDoctorAgent erEmergencyDoctorAgent = null;

		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
			{
				if( ArrayListObservationRooms.get(i).erGetPatientAgents().isEmpty() == true )
				{
					iJudgeCount++;
				}
				iJudgeCountSize++;
			}
		}
		if( iJudgeCount == iJudgeCountSize )
		{
			return false;
		}
		cWaitingRoomLog.info(erPAgent.getId() + "," + "bChangeEmergencyRoomObservationPatient" + "," + "患者部屋変更関数通ったよ～。");

		// 重症観察室で最も重症度あるいは緊急度の高い患者を見つけます。
		if(iJudgeUrgencyFlagMode == 1 )
		{
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					erTempAgent = ArrayListObservationRooms.get(i).erGetPatientAgent( j );
					// 移動中は対象外とします。
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( iMaxEmergency >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId())
					{
						iLoc = ArrayListObservationRooms.get( i ).iGetNurseAgentPatientLoc( j );
						if( iLoc != -1 )
						{
							iMaxEmergency = erTempAgent.iGetEmergencyLevel();
							lfMaxWaitTime = erTempAgent.lfGetObservationTime();
							iTargetRoomLoc = i;
							iTargetPatientLoc = j;
							iTargetNursePatientLoc = iLoc;
							erAgent = erTempAgent;
						}
					}
				}
			}
		}
		else
		{
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					erTempAgent = ArrayListObservationRooms.get(i).erGetPatientAgent( j );
					// 移動中は対象外とします。
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( lfMaxAIS <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
					{
						iLoc = ArrayListObservationRooms.get( i ).iGetNurseAgentPatientLoc( j );
						if( iLoc != -1 )
						{
							cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxAIS + "," + "bChangeEmergencyRoomObservationPatient" + "," + "観察室にいるみたいだよ。");
							lfMaxAIS = erTempAgent.lfGetMaxAIS();
							lfMaxWaitTime = erTempAgent.lfGetObservationTime();
							iTargetRoomLoc = i;
							iTargetPatientLoc = j;
							iTargetNursePatientLoc = iLoc;
							erAgent = erTempAgent;
						}
					}
				}
			}
		}
		lfMaxWaitTime = -Double.MAX_VALUE;
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
			{
				erTempAgent = ArrayListObservationRooms.get(i).erGetPatientAgent( j );
				// 移動中は対象外とします。
				if( erTempAgent.iGetMoveRoomFlag() == 0 ) continue;
				// 5時間以上経過している場合は強制的に移動できるように設定します。
				if( erTempAgent.lfGetObservationTime() >= 3600*2 && lfMaxWaitTime < erTempAgent.lfGetObservationTime() && erPAgent.getId() != erTempAgent.getId())
				{
					iLoc = ArrayListObservationRooms.get( i ).iGetNurseAgentPatientLoc( j );
					if( iLoc != -1 )
					{
						cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxWaitTime + "," + "bChangeEmergencyRoomObservationPatient" + "," + "観察室にいるみたいだよ。");
						lfMaxWaitTime = erTempAgent.lfGetObservationTime();
						iTargetRoomLoc = i;
						iTargetPatientLoc = j;
						iTargetNursePatientLoc = iLoc;
						erAgent = erTempAgent;
					}
				}
			}
		}
		if( iTargetPatientLoc == -1 )
		{
			// 基本的にはないとは思いますが、患者番号及び看護師番号に該当が存在しなかった場合は終了します。
			cWaitingRoomLog.info(erPAgent.getId() + "," + "うむむ該当者がいなかったぞよ～。bChangeEmergencyRoomObservationPatient");
			return false;
		}
		erTempNurseAgent = ArrayListObservationRooms.get( iTargetRoomLoc ).erGetNurseAgent( iTargetNursePatientLoc );
		erTempAgent = ArrayListObservationRooms.get( iTargetRoomLoc ).erGetPatientAgent( iTargetPatientLoc );
		cWaitingRoomLog.info(erPAgent.getId() + "," + erAgent.getId() + "," + erTempAgent.getId() + "," +  "緊急度判定" + "," + "ターゲットの患者：" + iTargetPatientLoc + "," +  ArrayListObservationRooms.get( iTargetRoomLoc ).erGetPatientAgents().size() );

	// 初療室へ移動します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			// 初療室に空きがある場合
			if( ArrayListEmergencyRooms.get(i).isVacant() == true )
			{
				// 初療室に空きがある場合
				cWaitingRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動準備開始" + "," + "観察室");

				// 初療室待機フラグをOFFにします。
				erTempAgent.vSetEmergencyRoomWaitFlag( 0 );

				// 患者のいる位置を初療室に変更します。
				erTempAgent.vSetLocation( 3 );

				// 観察フラグをOFFにします。
				erTempAgent.vSetObservedFlag( 0 );

				// 待合室での看護師に見てもらったフラグはOFFにします。
				erTempAgent.vSetNurseAttended( 0 );

				// 移動開始フラグを設定します。
				erTempAgent.vSetMoveRoomFlag( 1 );
				erTempAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している看護師エージェントがいなくなるので0に設定します。
				erTempAgent.vSetNurseAgent( 0 );

				// 初療室へ患者エージェントを移動します。
				ArrayListEmergencyRooms.get(i).vSetPatientAgent( erTempAgent );

			// 看護師、医師、技士エージェントへメッセージを送信します。
				// 初療室の看護師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erEmergencyNurseAgent = ArrayListEmergencyRooms.get(i).cGetNurseAgent(j);
					erTempNurseAgent.vSendToNurseAgentMessage( erTempAgent, (int)erTempNurseAgent.getId(), (int)erEmergencyNurseAgent.getId() );
				}
				// 初療室の医師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetDoctorAgentsNum(); j++ )
				{
					erEmergencyDoctorAgent = ArrayListEmergencyRooms.get(i).cGetDoctorAgent( j );
					erTempNurseAgent.vSendToDoctorAgentMessage( erTempAgent, erEmergencyDoctorAgent, (int)erTempNurseAgent.getId(), (int)erEmergencyDoctorAgent.getId() );
				}
//				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
//				{
//					erEmergencyCinicalEngineerAgent = ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgents(j);
//					erNurseAgent.vSendToEngineerAgentMessage( erTempAgent, (int)erNurseAgent.getId(), (int)erEmergencyClinicalEngineerAgent.getId() );
//				}

				// 看護師エージェントの対応を終了します。
				erTempNurseAgent.vSetAttending( 0 );

				// 対応を受けた患者エージェントを削除します。
				if( iLoc > -2 )
				{
					ArrayListObservationRooms.get( iTargetRoomLoc ).vRemovePatientAgent( erTempAgent, iTargetPatientLoc );
					ArrayListObservationRooms.get( iTargetRoomLoc ).vSetArrayListNursePatientLoc( iTargetNursePatientLoc, -1 );
				}
				cWaitingRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動準備終了" + "," + "観察室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erTempAgent.vSetMoveRoute( erTriageNodeManager.getRoute( ArrayListObservationRooms.get( iTargetRoomLoc ).erGetTriageNode(), ArrayListEmergencyRooms.get(i).erGetTriageNode() ) );
					cWaitingRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動開始" + "," + "観察室");
				}
				erTempAgent = null;

				// 初療室で担当する医師エージェントを設定します。
				ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetSurgeon(1);
				ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetAttending(1);
				break;
			}
		}
		return true;
	}

	/**
	 * <PRE>
	 *     待合室から初療室へ移動する対象患者エージェントに対して待合室にいる患者エージェントの中で
	 *     移動する患者エージェントを変更します。
	 * </PRE>
	 *
	 * @param erPAgent									移動対象の患者エージェント
	 * @param ArrayListEmergencyRooms					全初療室
	 * @param ArrayListObservationRooms					全観察室
	 * @param ArrayListSevereInjuryObservationRooms		全重症観察室
	 * @return											true 移動対象とする別の患者エージェントがいるため、そちらが移動する。
	 * 													false 移動しない。
	 * @author kobayashi
	 * @since 2015/11/05
	 */
	public boolean bChangeEmergencyRoomWaitingRoomPatient( ERPatientAgent erPAgent, ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms )
	{
		int i,j;
		int iLoc = -1;
		int iTargetRoomLoc = -1;
		int iTargetPatientLoc = -1;
		int iTargetNursePatientLoc = -1;
		int iMaxEmergency = Integer.MAX_VALUE;
		double lfMaxAIS = -Double.MAX_VALUE;
		double lfMaxWaitTime = -Double.MAX_VALUE;

		ERPatientAgent erTempAgent = null;
		ERPatientAgent erAgent = null;
		ERNurseAgent erTempNurseAgent = null;
		ERNurseAgent erEmergencyNurseAgent = null;
		ERDoctorAgent erEmergencyDoctorAgent = null;

		if( ArrayListPatientAgents.isEmpty() == true )
		{
			// 待合室が空室の場合は初療室へ移動するようにします。
			return false;
		}
		cWaitingRoomLog.info( erPAgent.getId() + "," + "患者部屋変更関数通ったよ～。");

		// 待合室で最も重症度あるいは緊急度の高い患者を見つけます。
		if( iJudgeUrgencyFlagMode == 1 )
		{
			for( i = 0;i < ArrayListPatientAgents.size(); i++ )
			{
				erTempAgent = ArrayListPatientAgents.get(i);
				// まだ、到着していない患者は対象外とします。
				if( erTempAgent.lfGetTimeCourse() <= 0.0 ) continue;
				// 移動中は対象外とします。
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( iMaxEmergency >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId())
				{
					// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
					if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
						erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
					{
						iMaxEmergency = erTempAgent.iGetEmergencyLevel();
						lfMaxWaitTime = erTempAgent.lfGetObservationTime();
						iTargetRoomLoc = 0;
						iTargetPatientLoc = i;
						iTargetNursePatientLoc = this.iGetNurseAgentPatientLoc(iTargetPatientLoc);
						erAgent = erTempAgent;
					}
				}
			}
		}
		else
		{
			for( i = 0;i < ArrayListPatientAgents.size(); i++ )
			{
				erTempAgent = ArrayListPatientAgents.get( i );
				// まだ、到着していない患者は対象外とします。
				if( erTempAgent.lfGetTimeCourse() <= 0.0 ) continue;
				// 移動中は対象外とします。
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( lfMaxAIS <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
				{
					// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
					if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
						erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
					{
						lfMaxAIS = erTempAgent.lfGetMaxAIS();
						lfMaxWaitTime = erTempAgent.lfGetObservationTime();
						iTargetRoomLoc = 0;
						iTargetPatientLoc = i;
						iTargetNursePatientLoc = this.iGetNurseAgentPatientLoc(iTargetPatientLoc);
						erAgent = erTempAgent;
					}
				}
			}
		}
		lfMaxWaitTime = -Double.MAX_VALUE;
		for( i = 0;i < ArrayListPatientAgents.size(); i++ )
		{
			erTempAgent = ArrayListPatientAgents.get( i );
			// まだ、到着していない患者は対象外とします。
			if( erTempAgent.lfGetTimeCourse() <= 0.0 ) continue;
			// 移動中は対象外とします。
			if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
			// 5時間以上経過している場合は強制的に移動できるように設定します。
			if( erTempAgent.lfGetWaitTime() >= 3600*2 && lfMaxWaitTime < erTempAgent.lfGetWaitTime() && erPAgent.getId() != erTempAgent.getId())
			{
				// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
				if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
					erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
					erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
					erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
				{
					lfMaxWaitTime = erTempAgent.lfGetWaitTime();
					iTargetRoomLoc = 0;
					iTargetPatientLoc = i;
					iTargetNursePatientLoc = this.iGetNurseAgentPatientLoc(iTargetPatientLoc);
					erAgent = erTempAgent;
				}
			}
		}
//		for( i = 0;i < ArrayListNursePatientLoc.size(); i++ )
//		{
//			if( ArrayListNursePatientLoc.get(i) == iTargetPatientLoc )
//			{
//				iTargetNursePatientLoc = i;
//			}
//		}
		if( iTargetPatientLoc == -1 )
		{
			// 基本的にはないとは思いますが、患者番号及び看護師番号に該当が存在しなかった場合は終了します。
			cWaitingRoomLog.info(erPAgent.getId() + "," + "うむむ該当者がいなかったぞよ～。bChangeEmergencyRoomWaitingRoomPatient");
			return false;
		}
		if( iTargetNursePatientLoc != -1 )
		{
			erTempNurseAgent = ArrayListNurseAgents.get( iTargetNursePatientLoc );
		}
		erTempAgent = ArrayListPatientAgents.get( iTargetPatientLoc );
		cWaitingRoomLog.info(erPAgent.getId() + "," + erAgent.getId() + "," + erTempAgent.getId() + "," + "緊急度判定" + "," + "ターゲットの患者：" + iTargetPatientLoc + "," +  ArrayListPatientAgents.size() );

	// 初療室へ移動します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			// 初療室に空きがある場合
			if( ArrayListEmergencyRooms.get(i).isVacant() == true )
			{
				// 初療室に空きがある場合
				cWaitingRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動準備開始" + "," + "待合室");

				// 初療室待機フラグをOFFにします。
				erTempAgent.vSetEmergencyRoomWaitFlag( 0 );

				// 患者のいる位置を初療室に変更します。
				erTempAgent.vSetLocation( 3 );

				// 観察フラグをOFFにします。
				erTempAgent.vSetObservedFlag( 0 );

				// 待合室での看護師に見てもらったフラグはOFFにします。
				erTempAgent.vSetNurseAttended( 0 );

				// 移動開始フラグを設定します。
				erTempAgent.vSetMoveRoomFlag( 1 );
				erTempAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している看護師エージェントがいなくなるので0に設定します。
				erTempAgent.vSetNurseAgent( 0 );

				// 初療室へ患者エージェントを移動します。
				ArrayListEmergencyRooms.get(i).vSetPatientAgent( erTempAgent );

				if( iTargetNursePatientLoc != -1 )
				{
					// 看護師、医師、技士エージェントへメッセージを送信します。
					// 初療室の看護師エージェントに患者情報を送信します。
					for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetNurseAgentsNum(); j++ )
					{
						erEmergencyNurseAgent = ArrayListEmergencyRooms.get(i).cGetNurseAgent(j);
						erTempNurseAgent.vSendToNurseAgentMessage( erTempAgent, (int)erTempNurseAgent.getId(), (int)erEmergencyNurseAgent.getId() );
					}
					// 初療室の医師エージェントに患者情報を送信します。
					for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetDoctorAgentsNum(); j++ )
					{
						erEmergencyDoctorAgent = ArrayListEmergencyRooms.get(i).cGetDoctorAgent( j );
						erTempNurseAgent.vSendToDoctorAgentMessage( erTempAgent, erEmergencyDoctorAgent, (int)erTempNurseAgent.getId(), (int)erEmergencyDoctorAgent.getId() );
					}
//					for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
//					{
//						erEmergencyCinicalEngineerAgent = ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgents(j);
//						erNurseAgent.vSendToEngineerAgentMessage( erTempAgent, (int)erNurseAgent.getId(), (int)erEmergencyClinicalEngineerAgent.getId() );
//					}

					// 看護師エージェントの対応を終了します。
					erTempNurseAgent.vSetAttending( 0 );

					// 対応を受けた患者エージェントを削除します。
					vRemovePatientAgent( erTempAgent, iTargetPatientLoc );
					ArrayListNursePatientLoc.set( iTargetNursePatientLoc, -1 );
				}
				else
				{
					vRemovePatientAgent( erTempAgent );
				}
				// 初療室で担当する医師エージェントを設定します。
				ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetSurgeon(1);
				ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetAttending(1);

				cWaitingRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動準備終了" + "," + "待合室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erTempAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListEmergencyRooms.get(i).erGetTriageNode() ) );
					cWaitingRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動開始" + "," + "待合室");
				}
				erTempAgent = null;
				break;
			}
		}
		return true;
	}

	/**
	 * <PRE>
	 *     待合室から移動する患者エージェントが重症観察室に重症度の大きい患者がいないかどうかを判定し、
	 *     判定結果に基づいて部屋の変更を実施します。
	 * </PRE>
	 * @param erPAgent						患者エージェント
	 * @param ArrayListConsultationRooms	全診察室
	 * @param ArrayListObservationRooms		全観察室
	 * @return								false 現在の患者エージェントが移動
	 * 										true 他の患者エージェントが移動
	 * @author kobayashi
	 * @since 2015/11/05
	 */
	public boolean bChangeConsultationRoomObservationRoomPatient( ERPatientAgent erPAgent, ArrayList<ERConsultationRoom> ArrayListConsultationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms )
	{
		int i,j;
		int iLoc = -1;
		int iTargetRoomLoc = -1;
		int iTargetPatientLoc = -1;
		int iTargetNursePatientLoc = -1;
		int iMaxEmergency = Integer.MAX_VALUE;
		double lfMaxAIS = -Double.MAX_VALUE;
		double lfMaxWaitTime = -Double.MAX_VALUE;
		int iJudgeCount = 0;
		int iJudgeCountSize = 0;

		ERPatientAgent erTempAgent = null;
		ERPatientAgent erAgent = null;
		ERNurseAgent erTempNurseAgent = null;
		ERNurseAgent erConsultationNurseAgent = null;
		ERDoctorAgent erConsultationDoctorAgent = null;

		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
			{
				if( ArrayListObservationRooms.get(i).erGetPatientAgents().isEmpty() == true )
				{
					iJudgeCount++;
				}
				iJudgeCountSize++;
			}
		}
		if( iJudgeCount == iJudgeCountSize )
		{
			return false;
		}
		cWaitingRoomLog.info(erPAgent.getId() + "," + "患者部屋変更関数通ったよ～。");

		// 待合室で最も重症度あるいは緊急度の高い患者を見つけます。
		if( iJudgeUrgencyFlagMode == 1 )
		{
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					erTempAgent = ArrayListObservationRooms.get( i ).erGetPatientAgent(j);
					// 移動中は対象外とします。
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( iMaxEmergency >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId())
					{
						iLoc = ArrayListObservationRooms.get(i).iGetNurseAgentPatientLoc( j );
						if( iLoc != -1 )
						{
							iMaxEmergency = erTempAgent.iGetEmergencyLevel();
							lfMaxWaitTime = erTempAgent.lfGetObservationTime();
							cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxWaitTime + "," + iMaxEmergency + "," + "bChangeConsultationRoomObservationRoomPatient" + "," + "観察室に移動する人選択。");
							iTargetRoomLoc = i;
							iTargetPatientLoc = j;
							iTargetNursePatientLoc = iLoc;
							erAgent = erTempAgent;
						}
					}
				}
			}
		}
		else
		{
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					erTempAgent = ArrayListObservationRooms.get( i ).erGetPatientAgent(j);
					// 移動中は対象外とします。
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( lfMaxAIS <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
					{
						iLoc = ArrayListObservationRooms.get(i).iGetNurseAgentPatientLoc( j );
						if( iLoc != -1 )
						{
							lfMaxAIS = erTempAgent.lfGetMaxAIS();
							lfMaxWaitTime = erTempAgent.lfGetObservationTime();
							cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxWaitTime + "," + lfMaxAIS + "," + "bChangeConsultationRoomObservationRoomPatient" + "," + "観察室に移動する人選択。");
							iTargetRoomLoc = i;
							iTargetPatientLoc = j;
							iTargetNursePatientLoc = iLoc;
							erAgent = erTempAgent;
						}
					}
				}
			}
		}
		lfMaxWaitTime = -Double.MAX_VALUE;
		for( i = 0;i < ArrayListObservationRooms.size(); i++ )
		{
			for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
			{
				erTempAgent = ArrayListObservationRooms.get( i ).erGetPatientAgent(j);
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				// 5時間以上経過している場合は強制的に移動できるように設定します。
				if( erTempAgent.lfGetObservationTime() >= 3600*2 && lfMaxWaitTime <= erTempAgent.lfGetObservationTime() && erPAgent.getId() != erTempAgent.getId())
				{
					iLoc = ArrayListObservationRooms.get(i).iGetNurseAgentPatientLoc( j );
					if( iLoc != -1 )
					{
						lfMaxWaitTime = erTempAgent.lfGetObservationTime();
						cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxWaitTime + "," + iMaxEmergency + "," + "bChangeConsultationRoomObservationRoomPatient" + "," + "観察室に移動する人選択。");
						iTargetRoomLoc = i;
						iTargetPatientLoc = j;
						iTargetNursePatientLoc = iLoc;
						erAgent = erTempAgent;
					}
				}
			}
		}
		if( iTargetPatientLoc == -1 || iTargetNursePatientLoc == -1 )
		{
			// 基本的にはないとは思いますが、患者番号及び看護師番号に該当が存在しなかった場合は終了します。
			cWaitingRoomLog.info(erPAgent.getId() + "," + "うむむ該当者がいなかったぞよ～。bChangeConsultationRoomObservationRoomPatient");
			return false;
		}
		erTempNurseAgent = ArrayListObservationRooms.get( iTargetRoomLoc ).erGetNurseAgent( iTargetNursePatientLoc );
		erTempAgent = ArrayListObservationRooms.get( iTargetRoomLoc ).erGetPatientAgent( iTargetPatientLoc );
		cWaitingRoomLog.info(erPAgent.getId() + "," + erAgent.getId() + "," + erTempAgent.getId() + "," + "緊急度判定" + "," + "ターゲットの患者：" + iTargetPatientLoc + "," +  ArrayListPatientAgents.size() );

	// 診察室へ移動します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			// 診察室に空きがある場合
			if( ArrayListConsultationRooms.get(i).isVacant() == true )
			{
				// 診察室に空きがある場合
				cWaitingRoomLog.info(erTempAgent.getId() + "," + "診察室へ移動準備開始" + "," + "観察室");

				// 診察室待機フラグをOFFにします。
				erTempAgent.vSetEmergencyRoomWaitFlag( 0 );

				// 患者のいる位置を初療室に変更します。
				erTempAgent.vSetLocation( 1 );

				// 観察フラグをOFFにします。
				erTempAgent.vSetObservedFlag( 0 );

				// 待合室での看護師に見てもらったフラグはOFFにします。
				erTempAgent.vSetNurseAttended( 0 );

				// 移動開始フラグを設定します。
				erTempAgent.vSetMoveRoomFlag( 1 );
				erTempAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している看護師エージェントがいなくなるので0に設定します。
				erTempAgent.vSetNurseAgent( 0 );

				// 初療室へ患者エージェントを移動します。
				ArrayListConsultationRooms.get(i).vSetPatientAgent( erTempAgent );

			// 看護師、医師、技士エージェントへメッセージを送信します。
				// 診察室の看護師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListConsultationRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erConsultationNurseAgent = ArrayListConsultationRooms.get(i).cGetNurseAgent(j);
					erTempNurseAgent.vSendToNurseAgentMessage( erTempAgent, (int)erTempNurseAgent.getId(), (int)erConsultationNurseAgent.getId() );
				}
				// 診察室の医師エージェントに患者情報を送信します。
				erConsultationDoctorAgent = ArrayListConsultationRooms.get(i).cGetDoctorAgent();
				erTempNurseAgent.vSendToDoctorAgentMessage( erTempAgent, erConsultationDoctorAgent, (int)erTempNurseAgent.getId(), (int)erConsultationDoctorAgent.getId() );
//				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
//				{
//					erEmergencyCinicalEngineerAgent = ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgents(j);
//					erNurseAgent.vSendToEngineerAgentMessage( erTempAgent, (int)erNurseAgent.getId(), (int)erEmergencyClinicalEngineerAgent.getId() );
//				}

				// 看護師エージェントの対応を終了します。
				erTempNurseAgent.vSetAttending( 0 );

				// 対応を受けた患者エージェントを削除します。
				ArrayListObservationRooms.get( iTargetRoomLoc ).vRemovePatientAgent( erTempAgent, iTargetPatientLoc );
				ArrayListObservationRooms.get( iTargetRoomLoc ).vSetArrayListNursePatientLoc( iTargetNursePatientLoc, -1 );

				cWaitingRoomLog.info(erPAgent.getId() + "," + ArrayListConsultationRooms.get(i).cGetDoctorAgent().getId() + "," + ArrayListConsultationRooms.get(i).cGetDoctorAgent().iGetAttending() + "," + "移動先の診察室の医師の状態" + "," + "観察室");
				ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetAttending(1);

				// 医師の診察時間を設定します。
				ArrayListConsultationRooms.get(i).cGetDoctorAgent().isJudgeConsultationTime( erTempAgent );

				cWaitingRoomLog.info(erTempAgent.getId() + "," + "診察室へ移動準備終了" + "," + "観察室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erTempAgent.vSetMoveRoute( erTriageNodeManager.getRoute( ArrayListObservationRooms.get( iTargetRoomLoc ).erGetTriageNode(), ArrayListConsultationRooms.get(i).erGetTriageNode() ) );
					cWaitingRoomLog.info(erTempAgent.getId() + "," + "診察室へ移動開始" + "," + "観察室");
				}
				erTempAgent = null;
				break;
			}
		}
		return true;
	}

	/**
	 * <PRE>
	 *     待合室から移動する患者エージェントが重症観察室に重症度の大きい患者がいないかどうかを判定し、
	 *     判定結果に基づいて部屋の変更を実施します。
	 * </PRE>
	 *
	 * @param erPAgent						移動対象となる患者エージェント
	 * @param ArrayListConsultationRooms	全診察室
	 * @return								true 移動する。
	 * 										false 移動しない。
	 * @author kobayashi
	 * @since 2015/11/05
	 */
	public boolean bChangeConsultationRoomWaitingRoomPatient( ERPatientAgent erPAgent, ArrayList<ERConsultationRoom> ArrayListConsultationRooms )
	{
		int i,j;
		int iLoc = -1;
		int iTargetRoomLoc = -1;
		int iTargetPatientLoc = -1;
		int iTargetNursePatientLoc = -1;
		int iMaxEmergency = Integer.MAX_VALUE;
		double lfMaxAIS = -Double.MAX_VALUE;
		double lfMaxWaitTime = -Double.MAX_VALUE;

		ERPatientAgent erTempAgent = null;
		ERPatientAgent erAgent = null;
		ERNurseAgent erTempNurseAgent = null;
		ERNurseAgent erConsultationNurseAgent = null;
		ERDoctorAgent erConsultationDoctorAgent = null;

		if( ArrayListPatientAgents.isEmpty() == true )
		{
			// 待合室が空室の場合は初療室へ移動するようにします。
			return false;
		}
		cWaitingRoomLog.info(erPAgent.getId() + "," + "患者部屋変更関数通ったよ～。");

		// 待合室で最も重症度あるいは緊急度の高い患者を見つけます。
		if( iJudgeUrgencyFlagMode == 1 )
		{
			for( i = 0;i < ArrayListPatientAgents.size(); i++ )
			{
				erTempAgent = ArrayListPatientAgents.get( i );
				// まだ、到着していない患者は対象外とします。
				if( erTempAgent.lfGetTimeCourse() <= 0.0 ) continue;
				// 移動中は対象外とします。
				if( erTempAgent.iGetMoveRoomFlag() == 1 )	continue;
				if( iMaxEmergency >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId())
				{
					// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
					if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
						erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
					{
						iMaxEmergency = erTempAgent.iGetEmergencyLevel();
						lfMaxWaitTime = erTempAgent.lfGetWaitTime();
						cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxWaitTime + "," + iMaxEmergency + "," + "bChangeConsultationRoomWaitingRoomPatient" + "," + "待合室に移動する人がいるみたい。");
						iTargetRoomLoc = 0;
						iTargetPatientLoc = i;
						iTargetNursePatientLoc = this.iGetNurseAgentPatientLoc(iTargetPatientLoc);
						erAgent = erTempAgent;
					}
				}
			}
		}
		else
		{
			for( i = 0;i < ArrayListPatientAgents.size(); i++ )
			{
				erTempAgent = ArrayListPatientAgents.get( i );
				// まだ、到着していない患者は対象外とします。
				if( erTempAgent.lfGetTimeCourse() <= 0.0 ) continue;
				// 移動中は対象外とします。
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( lfMaxAIS <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
				{
					// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
					if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
						erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
					{
						lfMaxAIS = erTempAgent.lfGetMaxAIS();
						lfMaxWaitTime = erTempAgent.lfGetWaitTime();
						cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxWaitTime + "," + lfMaxAIS + "," + "bChangeConsultationRoomWaitingRoomPatient" + "," + "待合室にいるみたいだよ。");
						iTargetRoomLoc = 0;
						iTargetPatientLoc = i;
						iTargetNursePatientLoc = this.iGetNurseAgentPatientLoc(iTargetPatientLoc);
						erAgent = erTempAgent;
					}
				}
			}
		}
		lfMaxWaitTime = -Double.MAX_VALUE;
		for( i = 0;i < ArrayListPatientAgents.size(); i++ )
		{
			erTempAgent = ArrayListPatientAgents.get( i );
			// まだ、到着していない患者は対象外とします。
			if( erTempAgent.lfGetTimeCourse() <= 0.0 ) continue;
			// 移動中は対象外とします。
			if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
			// 5時間以上経過している場合は強制的に移動できるように設定します。
			if( erTempAgent.lfGetWaitTime() >= 3600*2 && lfMaxWaitTime <= erTempAgent.lfGetWaitTime() && erPAgent.getId() != erTempAgent.getId())
			{
				// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
				if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
					erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
					erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
					erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
				{
					lfMaxWaitTime = erTempAgent.lfGetWaitTime();
					cWaitingRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + lfMaxWaitTime + "," + "bChangeConsultationRoomWaitingRoomPatient" + "," + "待合室にいるみたいだよ。");
					iTargetRoomLoc = 0;
					iTargetPatientLoc = i;
					iTargetNursePatientLoc = this.iGetNurseAgentPatientLoc(iTargetPatientLoc);
					erAgent = erTempAgent;
				}
			}
		}
//		for( i = 0;i < ArrayListNursePatientLoc.size(); i++ )
//		{
//			if( ArrayListNursePatientLoc.get(i) == iTargetPatientLoc )
//			{
//				iTargetNursePatientLoc = i;
//			}
//		}
		if( iTargetPatientLoc == -1 )
		{
			// 基本的にはないとは思いますが、患者番号及び看護師番号に該当が存在しなかった場合は終了します。
			cWaitingRoomLog.info(erPAgent.getId() + "," + "うむむ該当者がいなかったぞよ～。bChangeConsultationRoomWaitingRoomPatient");
			return false;
		}
		if( iTargetNursePatientLoc != -1 )
		{
			erTempNurseAgent = ArrayListNurseAgents.get( iTargetNursePatientLoc );
		}
		erTempAgent = ArrayListPatientAgents.get( iTargetPatientLoc );
		cWaitingRoomLog.info(erPAgent.getId() + "," + erAgent.getId() + "," + erTempAgent.getId() + "," + "緊急度判定" + "," + "ターゲットの患者：" + iTargetPatientLoc + "," +  ArrayListPatientAgents.size() );

	// 診察室へ移動します。
		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			// 診察室に空きがある場合
			if( ArrayListConsultationRooms.get(i).isVacant() == true )
			{
				// 診察室に空きがある場合
				cWaitingRoomLog.info(erTempAgent.getId() + "," + "診察室へ移動準備開始" + "," + "待合室");

				// 診察室待機フラグをOFFにします。
				erTempAgent.vSetEmergencyRoomWaitFlag( 0 );

				// 患者のいる位置を診察室に変更します。
				erTempAgent.vSetLocation( 1 );

				// 観察フラグをOFFにします。
				erTempAgent.vSetObservedFlag( 0 );

				// 待合室での看護師に見てもらったフラグはOFFにします。
				erTempAgent.vSetNurseAttended( 0 );

				// 移動開始フラグを設定します。
				erTempAgent.vSetMoveRoomFlag( 1 );
				erTempAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している看護師エージェントがいなくなるので0に設定します。
				erTempAgent.vSetNurseAgent( 0 );

				// 初療室へ患者エージェントを移動します。
				ArrayListConsultationRooms.get(i).vSetPatientAgent( erTempAgent );

				// 看護師、医師、技士エージェントへメッセージを送信します。
				if( iTargetNursePatientLoc != -1 )
				{
					// 診察室の看護師エージェントに患者情報を送信します。
					for( j = 0;j < ArrayListConsultationRooms.get(i).iGetNurseAgentsNum(); j++ )
					{
						erConsultationNurseAgent = ArrayListConsultationRooms.get(i).cGetNurseAgent(j);
						erTempNurseAgent.vSendToNurseAgentMessage( erTempAgent, (int)erTempNurseAgent.getId(), (int)erConsultationNurseAgent.getId() );
					}
					// 診察室の医師エージェントに患者情報を送信します。
					erConsultationDoctorAgent = ArrayListConsultationRooms.get(i).cGetDoctorAgent();
					erTempNurseAgent.vSendToDoctorAgentMessage( erTempAgent, erConsultationDoctorAgent, (int)erTempNurseAgent.getId(), (int)erConsultationDoctorAgent.getId() );
//					for( j = 0;j < ArrayListConsultationRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
//					{
//						erConsultationCinicalEngineerAgent = ArrayListConsultationRooms.get(i).cGetClinicalEngineerAgents(j);
//						erNurseAgent.vSendToEngineerAgentMessage( erTempAgent, (int)erNurseAgent.getId(), (int)erConsultationClinicalEngineerAgent.getId() );
//					}

					// 看護師エージェントの対応を終了します。
					erTempNurseAgent.vSetAttending( 0 );

					// 対応を受けた患者エージェントを削除します。
					vRemovePatientAgent( erTempAgent, iTargetPatientLoc );
					ArrayListNursePatientLoc.set( iTargetNursePatientLoc, -1 );
				}
				else
				{
					vRemovePatientAgent( erTempAgent );
				}
				cWaitingRoomLog.info(erPAgent.getId() + "," + ArrayListConsultationRooms.get(i).cGetDoctorAgent().getId() + "," + ArrayListConsultationRooms.get(i).cGetDoctorAgent().iGetAttending() + "," + "移動先の診察室の医師の状態" + "," + "待合室");
				ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetAttending(1);

				// 医師の診察時間を設定します。
				ArrayListConsultationRooms.get(i).cGetDoctorAgent().isJudgeConsultationTime( erTempAgent );

				cWaitingRoomLog.info(erTempAgent.getId() + "," + "診察室へ移動準備終了" + "," + "待合室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erTempAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListConsultationRooms.get(i).erGetTriageNode() ) );
					cWaitingRoomLog.info(erTempAgent.getId() + "," + "診察室へ移動開始" + "," + "待合室");
				}
				erTempAgent = null;
				break;
			}
		}
		return true;
	}

	/**
	 * <PRE>
	 *    独歩で外来した患者の到達分布を算出します。
	 *    exp(-(t-μ)/s)/(s*(1+exp(-(t-μ)/s))*(1+exp(-(t-μ)/s)))
	 *    μ = 11.75, s = 4
	 *    出展元、聖隷浜松病院のデータを基に推測
	 * </PRE>
	 * @param lfTime 経過時間[単位は時間]
	 * @return	患者到達確率
	 */
	private double lfCalcArrivalDensityWalk( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 11.75;
		double lfS		= 4;
		double lfExp	= 0.0;

		if( lfTime >= 24.0 )	lfTime = 0.0;

		lfExp = Math.exp( -(lfTime-lfMu)/lfS );
		lfProb = lfExp/(lfS*(1.0+lfExp)*(1.0+lfExp));
		return lfProb/lfArrivalPatientPepole*lfOneWeekArrivalPatientPepole*lfOneMonthArrivalPatientPepole*lfOneYearArrivalPatientPepole;
	}

	/**
	 * <PRE>
	 *    独歩で外来した患者の到達分布を算出します。(1日単位)
	 *    出展元
	 * </PRE>
	 * @param lfTime 経過時間[単位は日]
	 */
	private void vCalcArrivalDensityWalkOneWeek( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 11.75;
		double lfS		= 4;
		double lfExp	= 0.0;
		double lfOneWeek = 7.0;

		if( lfTime >= 7.0 )	lfTime = lfTime % lfOneWeek;

		lfOneWeekArrivalPatientPepole = 1.0;
	}

	/**
	 * <PRE>
	 *    独歩で外来した患者の到達分布を算出します。(1ヶ月単位)
	 *    出展元
	 * </PRE>
	 * @param lfTime 経過時間[単位は日]
	 */
	private void vCalcArrivalDensityWalkOneMonth( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 11.75;
		double lfS		= 4;
		double lfExp	= 0.0;
		double lfOneMonth = 31.0;

		if( lfTime >= 31.0 )	lfTime = lfTime % lfOneMonth;

		lfOneMonthArrivalPatientPepole = 1;

	}

	/**
	 * <PRE>
	 *    独歩で外来した患者の到達分布を算出します。(年間単位)
	 *    exp(-(t-μ)/s)/(s*(1+exp(-(t-μ)/s))*(1+exp(-(t-μ)/s)))
	 *    μ = 11.75, s = 4
	 *    出展元、聖隷浜松病院のデータを基に推測
	 * </PRE>
	 * @param lfTime 経過時間[単位は日]
	 */
	private void vCalcArrivalDensityWalkOneYear( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 11.75;
		double lfS		= 4;
		double lfExp	= 0.0;
		double lfOneYear = 365.0;

		if( lfTime >= 365.0 )	lfTime = lfTime % lfOneYear;

		lfOneYearArrivalPatientPepole = 1.0;
	}

	/**
	 * <PRE>
	 *    救急車搬送されて到達した患者の到達分布を算出します。
	 *    exp(-(t-μ)/s)/(s*(1+exp(-(t-μ)/s))*(1+exp(-(t-μ)/s)))
	 *    μ = 9.5, s = 3.8
	 *    出展元、聖隷浜松病院のデータを基に推測
	 * </PRE>
	 * @param lfTime 経過時間[単位は時間]
	 * @return	患者到達確率
	 */
	private double lfCalcArrivalDensityAmbulance( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 9.5;
		double lfS		= 3.8;
		double lfExp	= 0.0;

		if( lfTime >= 24.0 ) lfTime = 0.0;

		lfExp = Math.exp( -(lfTime-lfMu)/lfS );
		lfProb = lfExp/(lfS*(1.0+lfExp)*(1.0+lfExp));
		return lfProb/lfArrivalPatientPepole*lfOneWeekArrivalPatientPepole*lfOneMonthArrivalPatientPepole*lfOneYearArrivalPatientPepole;
	}

	/**
	 * <PRE>
	 *    救急車搬送されて到達した患者の到達分布を算出します。(1週間単位)
	 *    出展元
	 * </PRE>
	 * @param lfTime 経過時間[単位は日]
	 */
	private void vCalcArrivalDensityAmbulanceOneWeek( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 9.5;
		double lfS		= 3.8;
		double lfExp	= 0.0;
		double lfOneWeek = 7.0;

		if( lfTime >= 7.0 ) lfTime = lfTime % lfOneWeek;

		lfOneWeekArrivalPatientPepole = 1.0;
	}

	/**
	 * <PRE>
	 *    救急車搬送されて到達した患者の到達分布を算出します。(1ヶ月単位)
	 *    出展元
	 * </PRE>
	 * @param lfTime 経過時間[単位は日]
	 */
	private void vCalcArrivalDensityAmbulanceOneMonth( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 9.5;
		double lfS		= 3.8;
		double lfExp	= 0.0;
		double lfOneMonth = 31.0;

		if( lfTime >= 31.0 ) lfTime = lfTime % lfOneMonth;

		lfOneMonthArrivalPatientPepole = 1.0;
	}

	/**
	 * <PRE>
	 *    救急車搬送されて到達した患者の到達分布を算出します。(1年単位)
	 *    出展元
	 * </PRE>
	 * @param lfTime 経過時間[単位は日]
	 */
	private void vCalcArrivalDensityAmbulanceOneYear( double lfTime )
	{
		double lfProb	= 0.0;
		double lfMu		= 9.5;
		double lfS		= 3.8;
		double lfExp	= 0.0;
		double lfOneYear = 365.0;

		if( lfTime >= 365.0 ) lfTime = lfTime % lfOneYear;

		lfOneYearArrivalPatientPepole = 1.0;
	}

	/**
	 * <PRE>
	 *    災害発生時に外来した患者の到達分布を算出します。(1日単位)
	 *    Γ(t,α,β)から算出。tは時間。
	 *    λ = 2.259, α = 2.18
	 *   出展元
	 *   池内淳子,矢田雅子,権丈（武井）英理子,東原紘道
	 *   大規模地震災害時における病院間の傷病者搬送に関する考察-阪神・淡路大震災時における分析を通して-
	 *   地域安全学会論文集 No.19, 2012.3
	 *   これの西病院を参照。
	 * </PRE>
	 * @param lfTime 経過時間[単位は時間]
	 * @return	患者到達確率
	 */
	private double lfCalcArrivalDensityDisaster( double lfTime )
	{
		double lfProb	= 0.0;
		double lfAlpha = 2.18;
		double lfBeta = 2.2259;

		if( lfTime >= 24.0 )	lfTime = 0.0;

		lfProb = lfGammaDensity( lfTime, lfAlpha, lfBeta );
		return lfProb*lfOneWeekArrivalPatientPepole*lfOneMonthArrivalPatientPepole/lfArrivalPatientPepole;
	}

	/**
	 * <PRE>
	 *    災害発生時に外来した患者の到達分布を算出します。(1週間単位)
	 *    平成７年６月阪神淡路大震災災害医療実態アンケート調査結果をもとに算出。
	 *
	 * </PRE>
	 * @param lfTime 経過時間[単位は1日]
	 */
	private void vCalcArrivalDensityDisasterOneWeek( double lfTime )
	{
		double lfProb	= 0.0;
		double lfAlpha = 2.18;
		double lfBeta = 2.2259;
		double lfOneWeek = 7.0;

		if( lfTime >= 7.0 )	lfTime = lfTime % lfOneWeek;

		if( lfTime >= 6.0 ) lfOneWeekArrivalPatientPepole = 0.814751449;
		else if( lfTime >= 5.0 ) lfOneWeekArrivalPatientPepole = 0.262522574;
		else if( lfTime >= 4.0 ) lfOneWeekArrivalPatientPepole = 0.562589107;
		else if( lfTime >= 3.0 ) lfOneWeekArrivalPatientPepole = 0.723315274;
		else if( lfTime >= 2.0 ) lfOneWeekArrivalPatientPepole = 0.695276114;
		else if( lfTime >= 1.0 ) lfOneWeekArrivalPatientPepole = 0.756201882;
		else if( lfTime >= 0.0 ) lfOneWeekArrivalPatientPepole = 1;


//		System.out.println(lfProb);

	}

	/**
	 * <PRE>
	 *    災害発生時に外来した患者の到達分布を算出します。(1ヶ月単位)
	 *    データがないため1として実質動かないようにする。
	 *
	 * </PRE>
	 * @param lfTime 経過時間[単位は1ヶ月]
	 */
	private void vCalcArrivalDensityDisasterOneMonth( double lfTime )
	{
		double lfProb	= 0.0;
		double lfAlpha = 2.18;
		double lfBeta = 2.2259;
		double lfOneMonth = 31.0;

		if( lfTime >= 31.0 )	lfTime = lfTime % lfOneMonth;

		lfOneMonthArrivalPatientPepole = 1.0;
	}

	/**
	 * <PRE>
	 *    ガンマ分布の算出
	 *    奥村晴彦, C言語によるアルゴリズム辞典より
	 * </PRE>
	 * @param lfX		確率変数
	 * @param lfAlpha	α
	 * @param lfBeta	β
	 * @return			ガンマ分布値
	 */
	private double lfGammaDensity(double lfX, double lfAlpha, double lfBeta )
	{
		double lfRes = 0.0;
		lfRes = Math.pow( lfX, lfAlpha - 1.0 )*Math.exp( -lfX/lfBeta )/(Math.pow(lfBeta, lfAlpha)*lfGamma(lfX));
		return lfRes;
	}

	/**
	 * <PRE>
	 *    対数ガンマ関数を算出します。
	 *    奥村晴彦, C言語によるアルゴリズム辞典より
	 * </PRE>
	 * @param lfX	変数
	 * @return		対数ガンマ値
	 */
	private double lfLogGamma( double lfX )
	{
		double lfLog2pi = 1.83787706640934548;
		int iN = 8;
		double lfB0 = 1.0;
		double lfB1 = -0.5;
		double lfB2 = 0.16666666666666666666666666;
		double lfB4 = -0.0333333333333333333333333;
		double lfB6 = 0.023809524;
		double lfB8 = lfB4;
		double lfB10 = 5.0/66.0;
		double lfB12 = -891.0/2730.0;
		double lfB14 = 7.0/6.0;
		double lfB16 = -3617.0/520.0;

		double lfV, lfW;

		lfV = 1;
		while( lfX < iN ){ lfV *= lfX; lfX++;}
		lfW = 1.0/(lfX*lfX);

		return (((((lfB16/(16.0*15.0))*lfW + (lfB14/(14.0*13.0)) )*lfW
				    + (lfB12/(12.0*11.0))*lfW + (lfB10/(10.0* 9.0)) )*lfW
				    + ( lfB8/( 8.0* 7.0))*lfW + ( lfB6/( 6.0* 5.0)) )*lfW
				    + ( lfB4/( 4.0* 3.0))*lfW + ( lfB2/( 2.0* 1.0)) )/lfX
				    + 0.5*lfLog2pi - Math.log( lfV ) - lfX + ( lfX - 0.5 )*Math.log( lfX );
	}

	/**
	 * <PRE>
	 *    ガンマ関数を算出します。
	 *    奥村晴彦, C言語によるアルゴリズム辞典より
	 * </PRE>
	 * @param lfX	変数
	 * @return		対数ガンマ値
	 */
	private double lfGamma( double lfX )
	{
		if( lfX < 0.0 )
		{
			return Math.PI/( Math.sin(Math.PI*lfX)*Math.exp(lfLogGamma( 1.0-lfX)) );
		}
		return Math.exp(lfLogGamma(lfX));
	}

	@Override
	public void action(long timeStep)
	{
		int i,j;
		int iLoc = 0;
		double lfSecond = 0.0;
		lfSecond = timeStep / 1000.0;
		// TODO 自動生成されたメソッド・スタブ

		if( ArrayListPatientAgents == null )	return ;
		if( ArrayListPatientAgents.size() <= 0 )return ;

		try
		{
			synchronized( csWaitingRoomCriticalSection )
			{
				for( i = ArrayListPatientAgents.size()-1; i >= 0; i-- )
				{
					// 死亡フラグが立っている場合は患者エージェントを削除します。
					if( ArrayListPatientAgents.get(i) != null )
					{
						if( ArrayListPatientAgents.get(i).iGetSurvivalFlag() == 0 )
						{
							ArrayListPatientAgents.set(i, null);
							ArrayListPatientAgents.remove(i);
							break;
						}
						else if( ArrayListPatientAgents.get(i).iGetDisChargeFlag() == 1 )
						{
							break;
						}
						// 登録されているエージェントで離脱したエージェントがいるかどうかを判定します。
						if( ArrayListPatientAgents.get(i).isExitAgent() == true )
						{
							// いる場合は、ファイルに書き出しを実行します。
							ArrayListPatientAgents.get(i).vFlushFile( 0 );
						}
					}
				}
			}
		}
		catch( IOException ioe )
		{

		}
		lfSecond = timeStep/1000.0;
		lfTotalTime += lfSecond;
	}

	/**
	 * <PRE>
	 * 　看護師、患者対応フラグ配列へ設定します。
	 *    ver 0.1 初版
	 *    ver 0.2 看護師、患者対応配列参照部分のクリティカルセクション追加
	 * </PRE>
	 * @param iNurseLoc		看護師の番号
	 * @param iPatientLoc	患者の番号
	 */
	public void vSetArrayListNursePatientLoc( int iNurseLoc, int iPatientLoc )
	{
		synchronized( csWaitingRoomCriticalSection )
		{
			ArrayListNursePatientLoc.set( iNurseLoc, iPatientLoc );
		}
	}

	/**
	 * <PRE>
	 *　待合室に到達する患者の数を概算で設定します。
	 * </PRE>
	 * @param lfArrivalPatientPepoleData	到達した患者数
	 */
	public void vSetArrivalPatientPepole(double lfArrivalPatientPepoleData)
	{
		// TODO 自動生成されたメソッド・スタブ
		this.lfArrivalPatientPepole = lfArrivalPatientPepoleData;
	}

	/**
	 * <PRE>
	 *    ロガーを設定します。
	 * </PRE>
	 * @param log	ロガークラスインスタンス
	 */
	public void vSetLog(Logger log)
	{
		// TODO 自動生成されたメソッド・スタブ
		cWaitingRoomLog = log;
	}

	/**
	 * <PRE>
	 *    待合室のX座標を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	X座標
	 */
	public int iGetX()
	{
		return iDrawX;
	}

	/**
	 * <PRE>
	 *    待合室のY座標を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	Y座標
	 */
	public int iGetY()
	{
		return iDrawY;
	}

	/**
	 * <PRE>
	 *    待合室の横幅を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	横幅
	 */
	public int iGetWidth()
	{
		return iDrawWidth;
	}

	/**
	 * <PRE>
	 *    待合室の縦幅を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	縦幅
	 */
	public int iGetHeight()
	{
		return iDrawHeight;
	}

	/**
	 * <PRE>
	 *    待合室の階数を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	階数
	 */
	public int iGetF()
	{
		return iDrawF;
	}

	/**
	 * <PRE>
	 *    待合室のX座標を格納します。
	 * </PRE>
	 * @param iData	X座標
	 */
	public void vSetX( int iData )
	{
		iDrawX = iData;
	}

	/**
	 * <PRE>
	 *    待合室のY座標を格納します。
	 * </PRE>
	 * @param iData	Y座標
	 */
	public void vSetY( int iData )
	{
		iDrawY = iData;
	}

	/**
	 * <PRE>
	 *    待合室のZ座標を格納します。
	 * </PRE>
	 * @param iData	Z座標
	 */
	public void vSetZ( int iData )
	{
		iDrawZ = iData;
	}

	/**
	 * <PRE>
	 *   待合室の横幅を格納します。
	 * </PRE>
	 * @param iData	横幅
	 */
	public void vSetWidth( int iData )
	{
		iDrawWidth = iData;
	}

	/**
	 * <PRE>
	 *    待合室の縦幅を格納します。
	 * </PRE>
	 * @param iData	縦幅
	 */
	public void vSetHeight( int iData )
	{
		iDrawHeight = iData;
	}

	/**
	 * <PRE>
	 *    待合室の階数を格納します。
	 * </PRE>
	 * @param iData	階数
	 */
	public void vSetF( int iData )
	{
		iDrawF = iData;
	}

	/**
	 * <PRE>
	 *   待合室に所属しているエージェントの座標を設定します。
	 * </PRE>
	 */
	public void vSetAffiliationAgentPosition()
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;

		double lfX = 0.0;
		double lfY = 0.0;
		double lfZ = 0.0;

//		for( i = 0;i < ArrayListDoctorAgents.size(); i++ )
//		{
//			// 医師エージェントの位置を設定します。
//			lfX = this.getPosition().getX()+3*rnd.NextUnif();
//			lfY = this.getPosition().getY()+3*rnd.NextUnif();
//			lfZ = this.getPosition().getZ()+3*rnd.NextUnif();
//			ArrayListDoctorAgents.get(i).setPosition( lfX, lfY, lfZ );
//		}
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			// 看護師エージェントの位置を設定します。
			lfX = this.getPosition().getX()+30*(2*rnd.NextUnif()-1);
			lfY = this.getPosition().getY()+30*(2*rnd.NextUnif()-1);
			lfZ = this.getPosition().getZ();
			ArrayListNurseAgents.get(i).setPosition( lfX, lfY, lfZ );
			ArrayListNurseAgents.get(i).vSetTriageNode( erTriageNode );
		}
//		for( i = 0;i < ArrayListClinicalEngineerAgents.size(); i++ )
//		{
//			// 医療技師エージェントの位置を設定します。
//			lfX = this.getPosition().getX()+10*(2*rnd.NextUnif()-1);
//			lfY = this.getPosition().getY()+10*(2*rnd.NextUnif()-1);
//			lfZ = this.getPosition().getZ()+10*(2*rnd.NextUnif()-1);
//			ArrayListClinicalEngineerAgents.get(i).setPosition( lfX, lfY, lfZ );
//		}
		for( i = 0;i < ArrayListPatientAgents.size(); i++ )
		{
			// 患者エージェントの位置を設定します。
			lfX = this.getPosition().getX()+(2*rnd.NextUnif()-1);
			lfY = this.getPosition().getY()+(2*rnd.NextUnif()-1);
			lfZ = this.getPosition().getZ();
			ArrayListPatientAgents.get(i).setPosition( lfX, lfY, lfZ );
		}
	}

	public void vSetSimulationEndTime(double lfEndTime)
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;

		for( i = 0;i < ArrayListPatientAgents.size(); i++ )
		{
			ArrayListPatientAgents.get( i ).vSetSimulationEndTime( lfEndTime );
		}
	}

	/**
	 * <PRE>
	 *    トリアージノードマネージャーを設定します。
	 * </PRE>
	 * @param erNodeManager	ノード、リンクが格納されたノードマネージャのインスタンス
	 */
	public void vSetERTriageNodeManager( ERTriageNodeManager erNodeManager )
	{
		erTriageNodeManager = erNodeManager;
	}

	/**
	 * <PRE>
	 *    現在選択されている待合室のノードを取得します。
	 * </PRE>
	 * @return	待合室のノード
	 */
	public ERTriageNode erGetTriageNode()
	{
		return erTriageNode;
	}

	/**
	 * <PRE>
	 *   待合室のノードを設定します。
	 * </PRE>
	 * @param erNode	設定するノードインスタンス(待合室)
	 */
	public void vSetTriageNode( ERTriageNode erNode )
	{
		erTriageNode = erNode;
	}

	/**
	 * <PRE>
	 *   逆シミュレーションモードを設定します。
	 *    ver 0.1 初版
	 *    ver 0.2 患者配列参照部分のクリティカルセクション追加
	 * </PRE>
	 * @param iMode	0 通常シミュレーションモード
	 * 				1 GUIモード
	 * 				2 逆シミュレーションモード
	 */
	public void vSetInverseSimMode( int iMode )
	{
		int i;
		iInverseSimFlag = iMode;

		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetInverseSimMode( iMode );
		}
	}

	/**
	 * <PRE>
	 *   患者の逆シミュレーションモードを設定します。
	 *    ver 0.1 初版
	 *    ver 0.2 患者配列参照部分のクリティカルセクション追加
	 * </PRE>
	 * @param iMode	0 通常シミュレーションモード
	 * 				1 GUIモード
	 * 				2 逆シミュレーションモード
	 */
	public synchronized void vSetPatientInverseSimMode( int iMode )
	{
		if( ArrayListPatientAgents.size() > 0 )
		{
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetInverseSimMode( iMode );
		}
	}

	/**
	 * <PRE>
	 *   観察プロセス時間を超えたか否かを判定します。
	 * </PRE>
	 * @param lfObservationProcessTime  観察プロセス起動時間
	 * @param erNurseAgent              看護師エージェント
	 * @return 0 起動しない。
	 *         1 起動する。
	 */
	public int iJudgeObservationProcessTime( double lfObservationProcessTime, ERNurseAgent erNurseAgent )
	{
		int iFlag;
		iFlag = erNurseAgent.isJudgeObservationProcessTime( lfObservationProcessTime );

		return iFlag;
	}

	/**
	 * <PRE>
	 *   トリアージプロセス時間を超えたか否かを判定します。
	 * </PRE>
	 * @param lfTriageProcessTime  観察プロセス起動時間
	 * @param erNurseAgent         看護師エージェント
	 * @return 0 起動しない。
	 *         1 起動する。
	 */
	public int iJudgeTriageProcessTime( double lfTriageProcessTime, ERNurseAgent erNurseAgent )
	{
		int iFlag;
		iFlag = erNurseAgent.isJudgeTriageProcessTime( lfTriageProcessTime );

		return iFlag;
	}

	/**
	 * <PRE>
	 *    現在のなくなられた人数を取得します。
	 * </PRE>
	 * @return 亡くなった患者数
	 */
	public synchronized int iGetDeathNum()
	{
		int iDeathNumData = 0;
		synchronized( csWaitingRoomCriticalSection )
		{
			if( ArrayListPatientAgents == null ) return 0;
			if( ArrayListPatientAgents.size() > 0 )
			{
				iDeathNumData = ArrayListPatientAgents.get(0).iGetDeathNum();
			}
		}
		return iDeathNumData;
	}

	/**
	 * <PRE>
	 *    現在の生存している人数を取得します。
	 *    ver 0.1 初版
	 *    ver 0.2 患者配列参照部分のクリティカルセクション追加
	 * </PRE>
	 * @return 現在の生存数
	 */
	public synchronized int iGetSurvivalNum()
	{
		synchronized( csWaitingRoomCriticalSection )
		{
			return ArrayListPatientAgents.size()-ArrayListPatientAgents.get(0).iGetDeathNum();
		}
	}

	/**
	 * <PRE>
	 *    現在の患者の生存確率を取得します。
	 *    ver 0.1 初版
	 *    ver 0.2 患者配列参照部分のクリティカルセクション追加
	 * </PRE>
	 * @param iLoc	該当する患者エージェントの番号
	 * @return	現在の指定した患者の生存確率
	 */
	public synchronized double lfGetSurvivalProbability( int iLoc )
	{
		synchronized( csWaitingRoomCriticalSection )
		{
			return ArrayListPatientAgents.get(iLoc).lfGetSurvivalProbability();
		}
	}

	/**
	 * <PRE>
	 *    現在のシミュレーションに登場する患者の平均生存確率を取得します。
	 *    ver 0.1 初版
	 *    ver 0.2 患者配列参照部分のクリティカルセクション追加
	 * </PRE>
	 * @return 現在の患者全体の平均生存確率
	 */
	public synchronized double lfGetAvgSurvivalProbability()
	{
		int i;
		double lfRes = 0.0;
		synchronized( csWaitingRoomCriticalSection )
		{
			for( i = 0;i < ArrayListPatientAgents.size(); i++ )
			{
				lfRes += ArrayListPatientAgents.get(i).lfGetSurvivalProbability();
			}
			lfRes /= (double)ArrayListPatientAgents.size();
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 *    現在のシミュレーションに登場する患者の平均生存確率を取得します。
	 * </PRE>
	 * @return シミュレーション開始時の患者の平均生存確率
	 */
	public synchronized double lfCalcInitialAvgSurvivalProbability()
	{
		int i;
		double lfRes = 0.0;
		if( csWaitingRoomCriticalSection != null )
		{
			synchronized( csWaitingRoomCriticalSection )
			{
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					lfRes += ArrayListPatientAgents.get(i).lfCalcInitialSurvivalProbability();
				}
				lfRes /= (double)ArrayListPatientAgents.size();
			}
		}
		else
		{
			for( i = 0;i < ArrayListPatientAgents.size(); i++ )
			{
				lfRes += ArrayListPatientAgents.get(i).lfCalcInitialSurvivalProbability();
			}
			lfRes /= (double)ArrayListPatientAgents.size();
		}
		return lfRes;
	}

	/**
	 * <PRE>
	 *    現在の患者の総数を取得します。
	 * </PRE>
	 * @return 現在の患者の総数
	 */
	public synchronized int iGetTotalPatientNum()
	{
		int i;
		int iTotalPatientNum = 0;
		// TODO 自動生成されたメソッド・スタブ
		if( csWaitingRoomCriticalSection != null )
		{
			synchronized( csWaitingRoomCriticalSection )
			{
				if( ArrayListPatientAgents != null )
				{
					for( i = 0;i < ArrayListPatientAgents.size(); i++ )
					{
						if( ArrayListPatientAgents.get(i).lfGetTimeCourse() > 0.0 )
							iTotalPatientNum++;
					}
				}
			}
		}
		else
		{
			if( ArrayListPatientAgents != null )
			{
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					if( ArrayListPatientAgents.get(i).lfGetTimeCourse() > 0.0 )
						iTotalPatientNum++;
				}
			}
		}
		return iTotalPatientNum;
	}

	/**
	 * <PRE>
	 *    現時点で患者がいるかどうかを取得します。
	 * </PRE>
	 * @return 待合室に在院している患者数
	 */
	public synchronized int iGetPatientInARoom()
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;
		int iCount = 0;

		synchronized( csWaitingRoomCriticalSection )
		{
			if( ArrayListPatientAgents != null )
			{
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					if( ArrayListPatientAgents.get(i).iGetSurvivalFlag() == 1 )
					{
						iCount++;
					}
				}
			}
		}
		return iCount;
	}

	/**
	 * <PRE>
	 *    救急救命室で退院した人数を取得します。
	 * </PRE>
	 * @return	退院した患者数
	 */
	public int iGetDisChargeNum()
	{
		return iDisChargeNum;
	}

	/**
	 * <PRE>
	 *    クリティカルセクションを設定します。
	 * </PRE>
	 * @param cs	クリティカルセクションのインスタンス
	 */
	public void vSetCriticalSection(Object cs)
	{
		// TODO 自動生成されたメソッド・スタブ
		csWaitingRoomCriticalSection = cs;
	}

	/**
	 * <PRE>
	 *   メルセンヌツイスターインスタンスを設定します。
	 * </PRE>
	 * @param sfmtRandom メルセンヌツイスターインスタンス(部屋自体)
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vSetRandom(Rand sfmtRandom)
	{
		// TODO 自動生成されたメソッド・スタブ
		rnd = sfmtRandom;
	}

	/**
	 * <PRE>
	 *   メルセンヌツイスターインスタンスを設定します。
	 *   (室所属する看護師エージェント)
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vSetNursesRandom()
	{
		int i;
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetRandom( rnd );
		}
	}

	/**
	 * <PRE>
	 *   現時点でのトリアージ緊急度別受診数の患者の数を求めます。
	 * </PRE>
	 * @param iCategory		トリアージ緊急度
	 * @author kobayashi
	 * @since 2016/07/27
	 * @return	緊急度別トリアージ受診人数
	 */
	public synchronized int iGetTriageCategoryPatientNum( int iCategory )
	{
		int i;
		int iCategoryPatientNum = 0;

		if( csWaitingRoomCriticalSection != null )
		{
			synchronized( csWaitingRoomCriticalSection )
			{
				if( ArrayListPatientAgents != null )
				{
					for( i = 0;i < ArrayListPatientAgents.size(); i++ )
					{
						if( iCategory == (ArrayListPatientAgents.get(i).iGetEmergencyLevel()-1) )
						{
							iCategoryPatientNum++;
						}
					}
				}
			}
		}
		else
		{
			if( ArrayListPatientAgents != null )
			{
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					if( iCategory == (ArrayListPatientAgents.get(i).iGetEmergencyLevel()-1) )
					{
						iCategoryPatientNum++;
					}
				}
			}
		}

		return iCategoryPatientNum;
	}

	/**
	 * <PRE>
	 *    現在、待合室に最も長く病院にいる患者の在院時間を取得します。
	 * </PRE>
	 * @return		最も長く病院に在院する患者の在院時間
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public synchronized double lfGetLongestStayPatient()
	{
		int i;
		double lfLongestStayTime = -Double.MAX_VALUE;

		if( csWaitingRoomCriticalSection != null )
		{
			synchronized( csWaitingRoomCriticalSection )
			{
				if( ArrayListPatientAgents != null )
				{
					for( i = 0;i < ArrayListPatientAgents.size(); i++ )
					{
						if( ArrayListPatientAgents.get(i).lfGetTimeCourse() > 0.0 )
						{
							if( lfLongestStayTime < ArrayListPatientAgents.get(i).lfGetTimeCourse() )
							{
								lfLongestStayTime = ArrayListPatientAgents.get(i).lfGetTimeCourse();
							}
						}
					}
				}
			}
		}
		else
		{
			if( ArrayListPatientAgents != null )
			{
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					if( ArrayListPatientAgents.get(i).lfGetTimeCourse() > 0.0 )
					{
						if( lfLongestStayTime < ArrayListPatientAgents.get(i).lfGetTimeCourse() )
						{
							lfLongestStayTime = ArrayListPatientAgents.get(i).lfGetTimeCourse();
						}
					}
				}
			}
		}
		return lfLongestStayTime;
	}

	/**
	 * <PRE>
	 *    現在、最後に病床に入った患者の到着から入院までの時間を算出します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public synchronized void vLastBedTime()
	{
		int i;
		double lfLongestTime = -Double.MAX_VALUE;
		double lfLastTime = -Double.MAX_VALUE;
		if( csWaitingRoomCriticalSection != null )
		{
			synchronized( csWaitingRoomCriticalSection )
			{
				if( ArrayListPatientAgents != null )
				{
					for( i = 0;i < ArrayListPatientAgents.size(); i++ )
					{
						if( ArrayListPatientAgents.get(i).lfGetTimeCourse() > 0.0 )
						{
							if( lfLongestTime < ArrayListPatientAgents.get(i).lfGetTotalTime() && ArrayListPatientAgents.get(i).lfGetHospitalStayTime() > 0.0 )
							{
								lfLongestTime = ArrayListPatientAgents.get(i).lfGetTotalTime();
								lfLastTime = ArrayListPatientAgents.get(i).lfGetTimeCourse()-ArrayListPatientAgents.get(i).lfGetHospitalStayTime();
							}
							// 入院していない場合は0とします。
							if( ArrayListPatientAgents.get(i).lfGetHospitalStayTime() == 0.0 )
							{
								lfLastTime = 0.0;
								lfLongestTime = 0.0;
							}
						}
					}
				}
				lfLongestTotalTime = lfLongestTime;
				lfLastBedTime = lfLastTime;
			}
		}
		else
		{
			if( ArrayListPatientAgents != null )
			{
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					if( ArrayListPatientAgents.get(i).lfGetTimeCourse() > 0.0 )
					{
						if( lfLongestTime < ArrayListPatientAgents.get(i).lfGetTotalTime() && ArrayListPatientAgents.get(i).lfGetHospitalStayTime() > 0.0 )
						{
							lfLongestTime = ArrayListPatientAgents.get(i).lfGetTotalTime();
							lfLastTime = ArrayListPatientAgents.get(i).lfGetTimeCourse()-ArrayListPatientAgents.get(i).lfGetHospitalStayTime();
						}
						// 入院していない場合は0とします。
						if( ArrayListPatientAgents.get(i).lfGetHospitalStayTime() == 0.0 )
						{
							lfLastTime = 0.0;
							lfLongestTime = 0.0;
						}
					}
				}
			}
			lfLongestTotalTime = lfLongestTime;
			lfLastBedTime = lfLastTime;
		}
	}

	/**
	 * <PRE>
	 *    入院したから病院に最も長く滞在した時間を取得します。
	 *    NEDOCSにおいて使用します。
	 * </PRE>
	 * @return 最も長く滞在した時間[秒]
	 */
	public double lfGetLongestStayHospitalTotalTime()
	{
		return lfLongestTotalTime;
	}

	/**
	 * <PRE>
	 *    最も最後に入院した患者の現在までに経過した時間を取得します。
	 * </PRE>
	 * @return 最も最後に入院した患者の現在までの経過時間[秒]]
	 */
	public double lfGetLastBedTime()
	{
		return lfLastBedTime;
	}
}
