package triage.room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import triage.agent.ERDoctorAgent;
import triage.agent.ERNurseAgent;
import triage.agent.ERNurseAgentException;
import triage.agent.ERPatientAgent;
import utility.node.ERTriageNode;
import utility.node.ERTriageNodeManager;
import utility.sfmt.Rand;

/**
 * 病院の観察室を表すクラスです。
 * このプログラムではこのクラスを含めすべての部屋をエージェントとして定義しています。<br>
 * そのようにすることにより、いろいろと都合がよいためそのようにしております。<br>
 * 観察室では診察室が満員で診察できていない患者を受け入れて定期的にトリアージを実施する病室です。<br>
 * 診察室が空き次第、優先度に応じて患者を診察室へ移動させます。<br>
 * 次に行く場所は以下の通りです。<br>
 * １診察室<br>
 *
 * 使用方法は次の通りです。<br>
 * 初期化　　　　　　vInitialize　<br>
 * エージェント作成　vCreateNurseAgents<br>
 * 設定　　　　　　　vSetNurseAgentParameter<br>
 * 　　　　　　　　　vSetReadWriteFileForAgents<br>
 * 定期観察　　　　　vImplementObservationRoom<br>
 * 実行　　　　　　　action　<br>
 * 終了処理　　　　　　vTerminate　<br>
 *
 * @author kobayashi
 *
 */
public class ERObservationRoom extends Agent
{

	private static final long serialVersionUID = 7101773763944190016L;

	private ERPatientAgent erCurrentPatientAgent;								// 現在対応している患者
	private ArrayList<ERNurseAgent> ArrayListNurseAgents;						// 初療室で見ている看護師エージェント
	private ArrayList<ERPatientAgent> ArrayListPatientAgents;					// 初療室で受診している患者エージェント
	private ArrayList<Integer> ArrayListNursePatientLoc;						// 看護師と患者の対応位置
	private int iAttachedDoctorNum;												// 初療室に所属する医師エージェント数
	private int iAttachedNurseNum;												// 初療室に所属する看護師エージェント数
	private int iAttachedClinicalEngineerNum;									// 初療室に所属する医療技師エージェント数
	private double lfTotalTime;													// シミュレーション開始からの経過時間
	private Rand rnd;															// 乱数クラス
	private Logger cObservationRoomLog;											// 観察室のログ出力設定

	private ERTriageNodeManager erTriageNodeManager;
	private ERTriageNode erTriageNode;

	private int iJudgeUrgencyFlagMode = 1;										// 緊急度判定基準の判定モード(0:AIS値, 1：JTAS緊急度基準)
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

	private Object csObservationRoomCriticalSection;							// クリティカルセクション用

	private double lfLastBedTime;
	private double lfLongestTotalTime;

	public ERObservationRoom()
	{
		vInitialize();
	}

	public ERObservationRoom( int iNurseAgentNum )
	{
		vInitialize( iNurseAgentNum );
	}

	public void vInitialize( int iNurseAgentNum )
	{
		int i;
		ArrayListNurseAgents = new ArrayList<ERNurseAgent>();
		ArrayListNursePatientLoc = new ArrayList<Integer>();
		for( i = 0;i < iNurseAgentNum; i++ )
		{
			ArrayListNurseAgents.add( new ERNurseAgent() );
			ArrayListNursePatientLoc.add( new Integer( -1 ) );
		}
		ArrayListPatientAgents = new ArrayList<ERPatientAgent>();
	}

	public void vInitialize()
	{
		ArrayListNurseAgents			= new ArrayList<ERNurseAgent>();
		erCurrentPatientAgent			= null;								// 現在対応している患者
		ArrayListPatientAgents			= new ArrayList<ERPatientAgent>();	// 初療室で受診している患者エージェント
		ArrayListNursePatientLoc		= new ArrayList<Integer>();			// 看護師と患者の対応位置
		iAttachedDoctorNum				= 0;								// 初療室に所属する医師エージェント数
		iAttachedNurseNum				= 0;								// 初療室に所属する看護師エージェント数
		iAttachedClinicalEngineerNum	= 0;								// 初療室に所属する医療技師エージェント数
		lfTotalTime						= 0.0;								// 観察開始からの経過時間
//		long seed;
//		seed = System.currentTimeMillis();
//		rnd = null;
//		rnd = new Sfmt( (int)seed );

		iInverseSimFlag = 0;
	}

	/**
	 * <PRE>
	 *   診察室の看護師エージェントを生成します。
	 * </PRE>
	 * @param iNurseAgentNum 看護師エージェント数
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vCreateNurseAgents( int iNurseAgentNum )
	{
		int i;
		if( ArrayListNurseAgents == null )
		{
			// 逆シミュレーションの場合に通ります。
			ArrayListNurseAgents 		= new ArrayList<ERNurseAgent>();
			ArrayListNursePatientLoc	= new ArrayList<Integer>();		// 看護師と患者の対応位置
		}
		for( i = 0;i < iNurseAgentNum; i++ )
		{
			ArrayListNurseAgents.add( new ERNurseAgent() );
			ArrayListNursePatientLoc.add( new Integer( -1 ) );
		}
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
	 *    終了処理を実行します。
	 * </PRE>
	 * @throws IOException	終了処理エラー
	 */
	public synchronized void vTerminate() throws IOException
	{
		int i;

		synchronized( csObservationRoomCriticalSection )
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
				ArrayListPatientAgents = null;
			}

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
				ArrayListNurseAgents = null;
			}

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
			cObservationRoomLog = null;								// 観察室ログ出力設定

			// 乱数
			rnd = null;												// 乱数クラス

			// FUSEノード、リンク
			erTriageNodeManager = null;
			erTriageNode = null;
			lfTotalTime = 0.0;
		}
	}

	/**
	 * <PRE>
	 *    FUSEエンジンにエージェントを登録します。
	 * </PRE>
	 * @param engine シミュレーションエンジン
	 */
	public void vSetSimulationEngine( SimulationEngine engine )
	{
		engine.addAgent(this);
	}

	/**
	 * <PRE>
	 *    観察室の看護師エージェントのパラメータを設定します。
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
	 * version 0.2
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
	 *    観察室の観察プロセスを実行します。
	 * </PRE>
	 * @param ArrayListConsultationRooms				全診察
	 * @param ArrayListObservationRooms					全観察室
	 * @param ArrayListSevereInjuryObservationRooms		全重症観察室
	 * @param ArrayListEmergencyRooms					全初療室
	 * @param erWaitingRoom								待合室
	 * @throws ERNurseAgentException					看護師エージェント例外
	 */
	public void vImplementObservationRoom( ArrayList<ERConsultationRoom> ArrayListConsultationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ERWaitingRoom erWaitingRoom ) throws ERNurseAgentException
	{
		int i,j;
		int iEnableNurse = -1;
//		cObservationRoomLog.info("ObservationRoom" );
//		for( i= 0 ;i < ArrayListNurseAgents.size(); i++ )
//		{
//			// 看護師が対応していないかどうかを取得します。
//			if( ArrayListNurseAgents.get(i).iGetAttending() == 0 )
//			{
//				// 空きの看護師がいたら新たに入ってきた患者と関連付けます。
//				// 観察を受けていない患者がいるかどうかを判定します。
//				iEnableNurse = i;
//				for( j = 0;j < ArrayListPatientAgents.size(); j++ )
//				{
//					if( ArrayListPatientAgents.get(j).iGetNurseAttended() == 0 )
//					{
//						ArrayListNursePatientLoc.set( iEnableNurse, j );
//						ArrayListPatientAgents.get(j).vSetNurseAttended( 1 );
//						ArrayListPatientAgents.get(j).vSetNurseAgent(ArrayListNurseAgents.get(i).getId());
//						break;
//					}
//				}
//			}
/////			cObservationRoomLog.info("ArrayListNursePatientLoc Nurse:" + i + ",Patient:" + ArrayListNursePatientLoc.get(i) );
////			cObservationRoomLog.info("ArrayListNursePatientLoc size:" + ArrayListNursePatientLoc.size());
////			cObservationRoomLog.info("ArrayListPatientAgents size:" + ArrayListPatientAgents.size());
//		}
		synchronized( csObservationRoomCriticalSection )
		{
			// 登録されている患者がいる場合は各看護師の観察を実行します。
			// いない場合は何もせずに終了します。
			if( ArrayListPatientAgents.isEmpty() == false )
			{
				// 各看護師の観察を実行します。
				for( i = 0 ;i < ArrayListNurseAgents.size(); i++ )
				{
					// マルチスレッドによる齟齬への対処
					if( ArrayListNursePatientLoc.get(i) >= 0 )
					{
						if( ArrayListPatientAgents.size() <= 0 ) 										continue;
						if( ArrayListPatientAgents.size() <= ArrayListNursePatientLoc.get(i) ) 			continue;
						if( ArrayListPatientAgents.get( ArrayListNursePatientLoc.get( i ) ) == null )	continue;
						vImplementObservation( ArrayListConsultationRooms, ArrayListObservationRooms, ArrayListSevereInjuryObservationRooms, ArrayListEmergencyRooms, erWaitingRoom, ArrayListNurseAgents.get( i ), ArrayListPatientAgents.get( ArrayListNursePatientLoc.get( i ) ), i );
					}
				}
			}
		}
	}

	/**
	 * <PRE>
	 *   観察プロセスを実行します。
	 *   終了したら、診察室、重症観察室、初療室へ移動します。
	 *   あるいは、観察室で待機します。
	 * </PRE>
	 * @param ArrayListConsultationRooms				全診察室
	 * @param ArrayListObservationRooms					全観察室
	 * @param ArrayListSevereInjuryObservationRooms		全重症観察室
	 * @param ArrayListEmergencyRooms					全初療室
	 * @param erWaitingRoom								全待合室
	 * @param erNurseAgent								看護師エージェント
	 * @param erPAgent									患者エージェント
	 * @param iLoc										看護師がどの患者を対応していたのか
	 * @throws ERNurseAgentException					看護師エージェント例外
	 */
	public void vImplementObservation( ArrayList<ERConsultationRoom> ArrayListConsultationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ERWaitingRoom erWaitingRoom, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc ) throws ERNurseAgentException
	{
		int i;
		int iPrevLocation = 0;

		// 患者に診察を実施しない場合
		// 看護師エージェントの対応を実施します。
		erNurseAgent.vSetAttending( 1 );

		if( erPAgent.isMoveWaitingTime() == false )
		{
			// 移動時間がまだ終了していないので、移動を実施しません。
			erNurseAgent.vSetPatientMoveWaitFlag( 1 );
			cObservationRoomLog.info(erPAgent.getId() + "," + "観察室移動時間：" + erPAgent.lfGetMoveWaitingTime() );
			return;
		}

		// 部屋移動が終了したのでフラグOFFに処置中とします。
 		erPAgent.vSetMoveRoomFlag( 0 );
		erNurseAgent.vSetPatientMoveWaitFlag( 0 );

		// その患者を対応している看護師エージェントのIDを設定します。
		erPAgent.vSetNurseAgent(erNurseAgent.getId());

		// 患者の現在のいる部屋を取得します。
		iPrevLocation = erPAgent.iGetLocation();

		// 初療室待機フラグがONの場合
		if( erPAgent.iGetEmergencyRoomWaitFlag() == 1 )
		{
			// 初療室へ移動します。
			if( erPAgent.erGetConsultationDoctorAgent() != null )
				cObservationRoomLog.info(erPAgent.getId() + "," + erPAgent.erGetConsultationDoctorAgent().getId() + "," + erPAgent.erGetConsultationDoctorAgent().iGetAttending() + "," + "優先的に初療室へ移動開始判定, 観察室" );
			else
				cObservationRoomLog.info(erPAgent.getId() + "優先的に初療室へ移動開始判定, 観察室" );
			vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc );
			// 移動が完了したので、ここで処理を終了します。
			if( erPAgent.iGetEmergencyRoomWaitFlag() == 0 ) return ;
			// 移動していないと思われる場合。
			else
			{
				// この場合、元のエージェントは別の部屋に移動しているので、移動します。
				if( erPAgent.iGetLocation() != iPrevLocation ) return ;
				// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
			}
		}
		// 重症観察室フラグがONの場合
		if( erPAgent.iGetSereveInjuryObservationRoomWaitFlag() == 1 )
		{
			// 重症観察室へ移動します。
			cObservationRoomLog.info(erPAgent.getId() + "," + "優先的に重症観察室へ移動開始判定, 観察室" );
			vJudgeMoveSereveInjuryObservationRoom( ArrayListSevereInjuryObservationRooms, erNurseAgent, erPAgent, iLoc );
			// 移動が完了したので、ここで処理を終了します。
			if( erPAgent.iGetSereveInjuryObservationRoomWaitFlag() == 0 ) return ;
			// 移動していないと思われる場合。
			else
			{
				// この場合、元のエージェントは別の部屋に移動しているので、移動します。
				if( erPAgent.iGetLocation() != iPrevLocation ) return ;
				// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
			}
		}

		// 患者に診察を実施する場合
		if( erPAgent.iGetConsultationRoomWaitFlag() == 1 )
		{
			// 診察室へ移動できる場合は移動します。
			if(  erPAgent.erGetConsultationDoctorAgent() != null  )
				cObservationRoomLog.info(erPAgent.getId() + "," + erPAgent.erGetConsultationDoctorAgent().getId() + "," + erPAgent.erGetConsultationDoctorAgent().iGetAttending() + "," + "優先的に診察室へ移動開始判定, 観察室" );
			else
				cObservationRoomLog.info(erPAgent.getId() + "," + "優先的に診察室へ移動開始判定, 観察室" );
			vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc );
			// 移動が完了したので、ここで処理を終了します。
			if( erPAgent.iGetConsultationRoomWaitFlag() == 0 ) return ;
			// 移動していないと思われる場合。
			else
			{
				// この場合、元のエージェントは別の部屋に移動しているので、移動します。
				if( erPAgent.iGetLocation() != iPrevLocation ) return ;
				// その他の場合は待合室に待機し続けている状態になっているので、観察プロセスを実行します。
			}
		}

		// 観察プロセスを実行します。
		if( erPAgent.iGetObservedFlag() == 0 )
		{
			// 入ってきた患者に対して観察プロセス（トリアージプロセス）を実行します。
			vObservationProcess( ArrayListConsultationRooms, ArrayListObservationRooms, ArrayListSevereInjuryObservationRooms, ArrayListEmergencyRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc );
		}
		else
		{
			// 2回目以降は定期観察時間及び定期トリアージ時間に従って観察プロセスを実行します。
			vReObservationProcess( ArrayListConsultationRooms, ArrayListObservationRooms, ArrayListSevereInjuryObservationRooms, ArrayListEmergencyRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc );
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
	 * @param erWaitingRoom							待合室
	 * @param erNurseAgent							対応している看護師エージェント
	 * @param erPAgent								観察を受けている患者エージェント
	 * @param iLoc									看護師がどの患者を対応していたのか
	 * @throws ERNurseAgentException				看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	private void vObservationProcess( ArrayList<ERConsultationRoom> ArrayListConsultationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ERWaitingRoom erWaitingRoom, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc ) throws ERNurseAgentException
	{
		int i,j;
		int iPriorityFlag = 0;
		double lfObservationProcessTime;

		lfObservationProcessTime = erNurseAgent.lfGetObservationProcessTime()+5*(2*rnd.NextUnif()+1);
		// 観察プロセスを実行します。
		if( lfObservationProcessTime <= erNurseAgent.lfGetCurrentPassOverTime()-erPAgent.lfGetMoveTime() )
//		if( lfObservationProcessTime <= erNurseAgent.lfGetCurrentPassOverTime() )
		{
			if( erNurseAgent.iGetNurseCategory() == 1 )
			{
				erNurseAgent.vImplementNurseProcess( 1, erPAgent );
				// 再トリアージの時間設定を行います。
				erNurseAgent.vSetTriageProtocolTime( 0 );
			}
			else
			{
				erNurseAgent.vImplementNurseProcess( 0, erPAgent );
				// 再トリアージの時間設定を行います。
///				erNurseAgent.vSetTriageProtocolTime( 0 );
			}

			// 観察してもらったことを表すフラグをONにします。
			erPAgent.vSetObservedFlag( 1 );

			// 非緊急であった場合
			if( erNurseAgent.iGetEmergencyLevel() == 5 )
			{
				cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル5" + "," + "観察室");
//				for( j = 0;j < erWaitingRoom.erGetPatientAgents().size(); j++ )
//				{
//					if( erWaitingRoom.erGetPatientAgent(j).iGetEmergencyLevel() == 3 || erWaitingRoom.erGetPatientAgent(j).iGetEmergencyLevel() == 4 )
//					{
//						// 観察室に緊急度が3の患者がいる場合はその人を優先するため、待合室で待機します。
//						// 診察フラグをONにします。
//						erPAgent.vSetConsultationRoomWaitFlag( 1 );
//						iPriorityFlag = 1;
//						break;
//					}
//				}
//				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
//				{
//					if( ArrayListPatientAgents.get(i).iGetEmergencyLevel() == 3 || ArrayListPatientAgents.get(i).iGetEmergencyLevel() == 4 )
//					{
//						// 観察室に緊急度が3の患者がいる場合はその人を優先するため、観察室で待機します。
//						// 診察フラグをONにします。
//						erPAgent.vSetConsultationRoomWaitFlag( 1 );
//						iPriorityFlag = 1;
//						break;
//					}
//				}
//				if( iPriorityFlag == 0 )
				{
					// 診察室へ移動します。
					vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc  );
				}
			}
			// 低緊急であった場合
			else if( erNurseAgent.iGetEmergencyLevel() == 4 )
			{
				cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル4" + "," + "観察室");
				// 観察室でそのまま待機します。
				// 数回同じ状態であれば、診察室へ移動します。
				if( erPAgent.iObservationWaitCount() == 4 )
				{
//					for( j = 0;j < erWaitingRoom.erGetPatientAgents().size(); j++ )
//					{
//						if( erWaitingRoom.erGetPatientAgent(j).iGetEmergencyLevel() == 3 )
//						{
//							// 観察室に緊急度が3の患者がいる場合はその人を優先するため、待合室で待機します。
//							// 診察フラグをONにします。
//							erPAgent.vSetConsultationRoomWaitFlag( 1 );
//							iPriorityFlag = 1;
//							break;
//						}
//					}
//					for( i = 0;i < ArrayListPatientAgents.size(); i++ )
//					{
//						if( ArrayListPatientAgents.get(i).iGetEmergencyLevel() == 3 )
//						{
//							// 観察室に緊急度が3の患者がいる場合はその人を優先するため、観察室で待機します。
//							// 診察フラグをONにします。
//							erPAgent.vSetConsultationRoomWaitFlag( 1 );
//							iPriorityFlag = 1;
//							break;
//						}
//					}
//					if( iPriorityFlag == 0 )
					{
						// 診察フラグをONにします。
						erPAgent.vSetConsultationRoomWaitFlag( 1 );
						// 診察室へ移動します。
						vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc  );
					}
					erPAgent.vInitObservationWaitCount();
				}
			}
			// 準緊急であった場合
			else if( erNurseAgent.iGetEmergencyLevel() == 3 )
			{
				cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル3" + "," + "観察室");
				// 観察室でそのまま待機します。
				// 数回同じ状態であれば、診察室へ移動します。
				if( erPAgent.iObservationWaitCount() == 4 )
				{
					// 診察フラグをONにします。
					erPAgent.vSetConsultationRoomWaitFlag( 1 );
					vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc  );
					erPAgent.vInitObservationWaitCount();
				}
			}
			// 緊急であった場合
			else if( erNurseAgent.iGetEmergencyLevel() == 2  )
			{
				cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル2" + "," + "観察室");
				// 初療室へ移動します。
				vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc );
			}
			// 蘇生レベルあった場合
			else if(  erNurseAgent.iGetEmergencyLevel() == 1 )
			{
				cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル1" + "," + "観察室");
				// 初療室へ移動します。
				vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc );
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
	 * @param erWaitingRoom							待合室
	 * @param erNurseAgent							対応している看護師エージェント
	 * @param erPAgent								観察を受けている患者エージェント
	 * @param iLoc									看護師がどの患者を対応していたのか
	 * @throws ERNurseAgentException				看護師エージェント例外
	 * @author kobayashi
	 * @since 2015/08/07
	 */
	private void vReObservationProcess( ArrayList<ERConsultationRoom> ArrayListConsultationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ERWaitingRoom erWaitingRoom, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc ) throws ERNurseAgentException
	{
		int i,j;
		int iPriorityFlag = 0;
		double lfObservationTime = 0.0;
		//
		lfObservationTime = erNurseAgent.lfGetObservationTime()*erNurseAgent.lfGetAssociationRate();
		// 定期観察時間が経過した場合
//		if( iJudgeObservationProcessTime( lfObservationTime, erNurseAgent) == 1 )
		if( lfObservationTime <= erNurseAgent.lfGetCurrentPassOverTime() )
//		if( erNurseAgent.lfGetObservationTime() <= erNurseAgent.lfGetCurrentPassOverTime() )
		{
			// トリアージプロトコ時間が経過した場合
			if( erNurseAgent.iGetNurseCategory() == 1 )
			{
//				if( erNurseAgent.lfGetTriageProcessTime() <= erNurseAgent.lfGetCurrentPassOverTime() )
				if( iJudgeTriageProcessTime( erNurseAgent.lfGetTriageProcessTime(), erNurseAgent) == 1 )
				{
					// トリアージ実行時間経過した場合は、
					// トリアージプロセスを実行します。
					erNurseAgent.vImplementNurseProcess( 1, erPAgent );
				}
			}
			else
			{
				// トリアージ実行時間が経過していない場合は
				// 通常の観察を実行します。
				erNurseAgent.vImplementNurseProcess(0, erPAgent);
			}
			// 非緊急であった場合
			if( erNurseAgent.iGetEmergencyLevel() == 5 )
			{
				cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル5" + "," + "観察室");
//				for( j = 0;j < erWaitingRoom.erGetPatientAgents().size(); j++ )
//				{
//					if( erWaitingRoom.erGetPatientAgent(j).iGetEmergencyLevel() == 3 || erWaitingRoom.erGetPatientAgent(j).iGetEmergencyLevel() == 4 )
//					{
//						// 観察室に緊急度が3の患者がいる場合はその人を優先するため、待合室で待機します。
//						// 診察フラグをONにします。
//						erPAgent.vSetConsultationRoomWaitFlag( 1 );
//						iPriorityFlag = 1;
//						break;
//					}
//				}
//				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
//				{
//					if( ArrayListPatientAgents.get(i).iGetEmergencyLevel() == 3 || ArrayListPatientAgents.get(i).iGetEmergencyLevel() == 4 )
//					{
//						// 観察室に緊急度が3の患者がいる場合はその人を優先するため、観察室で待機します。
//						// 診察フラグをONにします。
//						erPAgent.vSetConsultationRoomWaitFlag( 1 );
//						iPriorityFlag = 1;
//						break;
//					}
//				}
//				if( iPriorityFlag == 0 )
				{
					// 診察室へ移動します。
					vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc  );
				}
			}
			// 低緊急であった場合
			else if( erNurseAgent.iGetEmergencyLevel() == 4 )
			{
				cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル4" + "," + "観察室");
				// 観察室でそのまま待機します。
				// 数回同じ状態であれば、診察室へ移動します。
				if( erPAgent.iObservationWaitCount() == 4 )
				{
//					for( j = 0;j < erWaitingRoom.erGetPatientAgents().size(); j++ )
//					{
//						if( erWaitingRoom.erGetPatientAgent(j).iGetEmergencyLevel() == 3 )
//						{
//							// 観察室に緊急度が3の患者がいる場合はその人を優先するため、待合室で待機します。
//							// 診察フラグをONにします。
//							erPAgent.vSetConsultationRoomWaitFlag( 1 );
//							iPriorityFlag = 1;
//							break;
//						}
//					}
//					for( i = 0;i < ArrayListPatientAgents.size(); i++ )
//					{
//						if( ArrayListPatientAgents.get(i).iGetEmergencyLevel() == 3 )
//						{
//							// 観察室に緊急度が3の患者がいる場合はその人を優先するため、観察室で待機します。
//							// 診察フラグをONにします。
//							erPAgent.vSetConsultationRoomWaitFlag( 1 );
//							iPriorityFlag = 1;
//							break;
//						}
//					}
//					if( iPriorityFlag == 0 )
					{
						// 診察フラグをONにします。
						erPAgent.vSetConsultationRoomWaitFlag( 1 );
						// 診察室へ移動します。
						vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc  );
					}
					erPAgent.vInitObservationWaitCount();
				}
			}
			// 準緊急であった場合
			else if( erNurseAgent.iGetEmergencyLevel() == 3 )
			{
				cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル3" + "," + "観察室");
				// 観察室でそのまま待機します。
				// 数回同じ状態であれば、診察室へ移動します。
				if( erPAgent.iObservationWaitCount() == 4 )
				{
					// 診察フラグをONにします。
					erPAgent.vSetConsultationRoomWaitFlag( 1 );
					vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc  );
					erPAgent.vInitObservationWaitCount();
				}
			}
			// 緊急であった場合
			else if( erNurseAgent.iGetEmergencyLevel() == 2  )
			{
				cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル2" + "," + "観察室");
				// 初療室へ移動します。
				vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc );
			}
			// 蘇生レベルあった場合
			else if(  erNurseAgent.iGetEmergencyLevel() == 1 )
			{
				cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル1" + "," + "観察室");
				// 初療室へ移動します。
				vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc );
			}
		}
		else
		{
			// トリアージナースの場合は再トリアージ時間を経過しているかどうかを判定します。
			// トリアージナースでない場合は観察時間が経過していないので、何もしません。
			if( erNurseAgent.iGetNurseCategory() == 1 )
			{
				// トリアージプロトコ時間判定
				if( erNurseAgent.lfGetTriageProcessTime() <= erNurseAgent.lfGetCurrentPassOverTime() )
				{
					// トリアージ実行時間経過した場合は、トリアージプロセスを実行します。
					erNurseAgent.vImplementNurseProcess( 1, erPAgent );
				}
				// 非緊急の場合は診察室が空いているかを調べます。
				if( erNurseAgent.iGetEmergencyLevel() == 5 )
				{
					cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル5" + "," + "観察室");
					vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc );
				}
				// 準緊急の場合は観察室が空いているかどうかを調べる。
				else if( erNurseAgent.iGetEmergencyLevel() == 4 )
				{
					cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル4" + "," + "観察室");
					// 観察室でそのまま待機します。
					// 数回同じ状態であれば、診察室へ移動します。
					if( erPAgent.iObservationWaitCount() == 4 )
					{
						// 診察フラグをONにします。
						erPAgent.vSetConsultationRoomWaitFlag( 1 );
						vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc  );
						erPAgent.vInitObservationWaitCount();
					}
				}
				// 緊急の場合は重症観察室が空いているかどうかを調べる。
				else if( erNurseAgent.iGetEmergencyLevel() == 3 )
				{
					cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル3" + "," + "観察室");
					// 観察室でそのまま待機します。
					// 数回同じ状態であれば、診察室へ移動します。
					if( erPAgent.iObservationWaitCount() == 4 )
					{
						// 診察フラグをONにします。
						erPAgent.vSetConsultationRoomWaitFlag( 1 );
						vJudgeMoveConsultationRoom( ArrayListConsultationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc  );
						erPAgent.vInitObservationWaitCount();
					}
				}
				// 緊急であった場合
				else if( erNurseAgent.iGetEmergencyLevel() == 2  )
				{
					cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル2" + "," + "観察室");
					// 初療室へ移動します。
					vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc );
				}
				// 蘇生レベルあった場合
				else if(  erNurseAgent.iGetEmergencyLevel() == 1 )
				{
					cObservationRoomLog.info(erPAgent.getId() + "," + "緊急度判定レベル1" + "," + "観察室");
					// 初療室へ移動します。
					vJudgeMoveEmergencyRoom( ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erWaitingRoom, erNurseAgent, erPAgent, iLoc );
				}
			}
		}
	}

	/**
	 * <PRE>
	 *    診察室への移動判定を実施し、移動します。
	 * </PRE>
	 * @param ArrayListConsultationRooms	全診察室
	 * @param ArrayListObservationRooms		全観察室
	 * @param erWaitingRoom					待合室
	 * @param erNurseAgent					看護師エージェント
	 * @param erPAgent						患者エージェント
	 * @param iLoc							看護師がどの患者を対応していたのか
	 * @author kobayashi
	 * @since 2015/08/11
	 */
	private void vJudgeMoveConsultationRoom( ArrayList<ERConsultationRoom> ArrayListConsultationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms, ERWaitingRoom erWaitingRoom, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc )
	{
		int i,j;
		int iJudgeCount = 0;
		int iJudgeFlag = -1;
		int iJudgeSevereFlag = -1;
		int iJudgeWaitTimeFlag = -1;
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
//				iJudgeFlag = iJudgeConsultationPatient( 0, erPAgent, ArrayListObservationRooms, erWaitingRoom );
//				iJudgeFlag = iJudgeConsultationPatient( 1, erPAgent, ArrayListObservationRooms, erWaitingRoom );
				iJudgeSevereFlag = iJudgeConsultationPatient(1, erPAgent, ArrayListObservationRooms, erWaitingRoom );
				iJudgeWaitTimeFlag = iJudgeWaitTimeConsultationPatient( erPAgent, ArrayListObservationRooms, erWaitingRoom );
				// 両方のフラグが有効の場合は長時間待たせ続けている患者さんを対応します。
				if( iJudgeSevereFlag == -1 )								iJudgeFlag = iJudgeWaitTimeFlag;
				if( iJudgeWaitTimeFlag == -1 )								iJudgeFlag = iJudgeSevereFlag;
				if( iJudgeSevereFlag != -1 && iJudgeWaitTimeFlag != -1 )	iJudgeFlag = iJudgeWaitTimeFlag;

				// 待合室に該当者がいる場合
				if( iJudgeFlag == 0 )
				{
					// 待合室で他の患者が該当する場合は待合室の患者を初療室へ移動し、ここでの患者はそのまま待機します。
					bRet = bChangeConsultationRoomWaitingRoomPatient( erPAgent, ArrayListConsultationRooms, ArrayListObservationRooms, erWaitingRoom );
					if( bRet == true )
					{
						cObservationRoomLog.info(erPAgent.getId() + "," + "待機(診察室へは他の待合室の患者が移動)" + "," + "観察室");
					}
					// 診察室待機者に変わりはないので診察室待機フラグをONにします。
					erPAgent.vSetConsultationRoomWaitFlag( 1 );
				}
				// 観察室に該当者がいる場合
				else if( iJudgeFlag == 1 )
				{
					// 待合室で他の患者が該当する場合は待合室の患者を初療室へ移動し、ここでの患者はそのまま待機します。
					bRet = bChangeConsultationRoomObservationRoomPatient( erPAgent, ArrayListConsultationRooms, ArrayListObservationRooms );
					if( bRet == true )
					{
						cObservationRoomLog.info(erPAgent.getId() + "," + "待機(診察室へは他の観察室の患者が移動)" + "," + "観察室");
					}
					// 診察室待機者に変わりはないので診察室待機フラグをONにします。
					erPAgent.vSetConsultationRoomWaitFlag( 1 );
				}
				// 当人の場合
				if( bRet == false )
				{
					cObservationRoomLog.info(erPAgent.getId() + "," + "診察室へ移動準備開始" + "," + "観察室");
					// 診察室待機フラグをOFFにします。
					erPAgent.vSetConsultationRoomWaitFlag( 0 );

					// 患者のいる位置を診察室に変更します。
					erPAgent.vSetLocation( 1 );

					// 観察フラグをOFFにします。
					erPAgent.vSetObservedFlag( 0 );

					// 観察室での看護師に見てもらったフラグはOFFにします。
					erPAgent.vSetNurseAttended( 0 );

					// 移動開始フラグを設定します。
					erPAgent.vSetMoveRoomFlag( 1 );
					erPAgent.vSetMoveWaitingTime( 0.0 );

					// 患者エージェントを診察室に配置します。
					ArrayListConsultationRooms.get(i).vSetPatientAgent( erPAgent );

					// 診察室の医師エージェントに患者情報を送信します。
					erConsultationDoctorAgent = ArrayListConsultationRooms.get(i).cGetDoctorAgent();
					erNurseAgent.vSendToDoctorAgentMessage( erPAgent, erConsultationDoctorAgent, (int)erNurseAgent.getId(), (int)erConsultationDoctorAgent.getId() );

					// 診察室の看護師エージェントに患者情報を送信します。
					for( j = 0;j < ArrayListConsultationRooms.get(i).iGetNurseAgentsNum(); j++ )
					{
						erConsultationNurseAgent = ArrayListConsultationRooms.get(i).cGetNurseAgent(j);
						erNurseAgent.vSendToNurseAgentMessage( erPAgent, (int)erNurseAgent.getId(), (int)erConsultationNurseAgent.getId() );
					}

					// 看護師エージェントの対応を終了します。
					erNurseAgent.vSetAttending( 0 );

					// 対応を受けた患者エージェントを削除します。
					vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
					ArrayListNursePatientLoc.set( iLoc, -1 );

					// 診察室で担当する医師エージェントを設定します。
					cObservationRoomLog.info(erPAgent.getId() + "," + erConsultationDoctorAgent.getId() + "," + erConsultationDoctorAgent.iGetAttending() + "," + "移動先の診察室の医師の状態" + "," + "待合室");
//					ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetConsultation(1);
					ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetAttending(1);

					// 医師の診察時間を設定します。
					ArrayListConsultationRooms.get(i).cGetDoctorAgent().isJudgeConsultationTime( erPAgent );

					cObservationRoomLog.info(erPAgent.getId() + "," + "診察室へ移動準備終了" + "," + "観察室");
					if( iInverseSimFlag == 1 )
					{
						// 移動先の経路を患者エージェントに設定します。
						erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListConsultationRooms.get(i).erGetTriageNode() ) );
						cObservationRoomLog.info(erPAgent.getId() + "," + "診察室へ移動開始" + "," + "観察室");
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
		if( iJudgeCount == ArrayListConsultationRooms.size() )
		{
			cObservationRoomLog.info(erPAgent.getId() + "," + "診察室満室" + "," + "観察室");
			// 診察室待機フラグをONにします。
			erPAgent.vSetConsultationRoomWaitFlag( 1 );
		}
	}

	/**
	 * <PRE>
	 *    重症観察室への移動判定を行います。
	 * </PRE>
	 * @param ArrayListSereveInjuryObservationRooms	全重症観察室
	 * @param erNurseAgent							観察室で担当した看護師エージェント
	 * @param erPAgent								観察を受けた患者エージェント
	 * @param iLoc									看護師がどの患者を対応していたのか
	 * @author kobayashi
	 * @since 2015/08/11
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
				cObservationRoomLog.info(erPAgent.getId() + "," + "重症観察室へ移動準備開始" + "," + "観察室");
				// 重傷観察室待機フラグをOFFにします。
				erPAgent.vSetSereveInjuryObservationRoomWaitFlag( 0 );

				// 患者のいる位置を重症観察室に変更します。
				erPAgent.vSetLocation( 5 );

				// 観察フラグをOFFにします。
				erPAgent.vSetObservedFlag( 0 );

				// 観察室での看護師に見てもらったフラグはOFFにします。
				erPAgent.vSetNurseAttended( 0 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// 空きがない場合は重症観察室へ移動します。
				ArrayListSereveInjuryObservationRooms.get(i).vSetPatientAgent( erPAgent );

				// 重症観察室の看護師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListSereveInjuryObservationRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erSevereInjuryObservationNurseAgent = ArrayListSereveInjuryObservationRooms.get(i).erGetNurseAgent(j);
					// 看護師エージェントへメッセージを送信します。
					erNurseAgent.vSendToNurseAgentMessage( erPAgent, (int)erNurseAgent.getId(), (int)erSevereInjuryObservationNurseAgent.getId() );
				}

				// 看護師エージェントの対応を終了します。
				erNurseAgent.vSetAttending( 0 );

				// 対応を受けた患者エージェントを削除します。
				vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
				ArrayListNursePatientLoc.set( iLoc, -1 );

				cObservationRoomLog.info(erPAgent.getId() + "," + "重症観察室へ移動準備終了" + "," + "観察室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListSereveInjuryObservationRooms.get(i).erGetTriageNode() ) );
					cObservationRoomLog.info(erPAgent.getId() + "," + "重症観察室へ移動開始" + "," + "観察室");
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
			cObservationRoomLog.info(erPAgent.getId() + "," + "重症観察室満室" + "," + "観察室");
			// 空きがない場合は観察室待機フラグをONにしてそのまま観察室で待機します。
			erPAgent.vSetObservationRoomWaitFlag( 1 );
		}
	}

	/**
	 * <PRE>
	 *   初療室への移動判定を実施します。
	 * </PRE>
	 * @param ArrayListEmergencyRooms					全初療室
	 * @param ArrayListSevereInjuryObservationRooms		全重症観察室
	 * @param ArrayListObservationRooms					全観察室
	 * @param erWaitingRoom								待合室
	 * @param erNurseAgent								観察を担当した看護師エージェント
	 * @param erPAgent									観察を受けた患者エージェント
	 * @param iLoc										看護師がどの患者を対応していたのか
	 * @author kobayashi
	 * @since 2015/08/11
	 */
	public void vJudgeMoveEmergencyRoom( ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ArrayList< ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ArrayList< ERObservationRoom> ArrayListObservationRooms, ERWaitingRoom erWaitingRoom, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc )
	{
		int i,j;
		int iJudgeCount = 0;
		int iJudgeFlag = -1;
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
//				iJudgeFlag = iJudgeEmergencyPatient( 1, erPAgent, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erWaitingRoom );
//				iJudgeSevereFlag = iJudgeEmergencyPatient( 0, erPAgent, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erWaitingRoom );
				iJudgeSevereFlag = iJudgeEmergencyPatient( 1, erPAgent, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erWaitingRoom );
				iJudgeWaitTimeFlag = iJudgeWaitTimeEmergencyPatient( erPAgent, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms );
				// 両方のフラグが有効の場合は長時間待たせ続けている患者さんを対応します。
				if( iJudgeSevereFlag == -1 )								iJudgeFlag = iJudgeWaitTimeFlag;
				if( iJudgeWaitTimeFlag == -1 )								iJudgeFlag = iJudgeSevereFlag;
				if( iJudgeSevereFlag != -1 && iJudgeWaitTimeFlag != -1 )	iJudgeFlag = iJudgeWaitTimeFlag;

				// 待合室の場合
				if( iJudgeFlag == 0 )
				{
					// 待合室で他の患者が該当する場合は待合室の患者を初療室へ移動し、ここでの患者はそのまま待機します。
					bRet = bChangeEmergencyRoomWaitingRoomPatient(erPAgent, ArrayListEmergencyRooms, erWaitingRoom, ArrayListObservationRooms, ArrayListSevereInjuryObservationRooms );
					if( bRet == true )
					{
						cObservationRoomLog.info(erPAgent.getId() + "," + "待機(初療室へは他の待合室の患者が移動)" + "," + "観察室");
					}
					// 初療室待機者に変わりはないので初療室待機フラグをONにします。
					erPAgent.vSetEmergencyRoomWaitFlag( 1 );
				}
				// 重症観察室の場合
				else if( iJudgeFlag == 1 )
				{
					// 重症観察室の患者が該当する場合は重症観察室の患者を初療室へ移動し、ここでの患者は観察室で待機します。
					bRet = bChangeEmergencyRoomSevereInjuryObservationPatient(erPAgent, ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erWaitingRoom );
					if( bRet == true )
					{
						cObservationRoomLog.info(erPAgent.getId() + "," + "待機(診察室へは重症観察室の患者が移動)" + "," + "観察室");
					}
					// 初療室待機者に変わりはないので初療室待機フラグをONにします。
					erPAgent.vSetEmergencyRoomWaitFlag( 1 );
				}
				// 観察室の場合
				else if( iJudgeFlag == 2 )
				{
					// 他の観察室の患者が該当する場合は観察室の患者を初療室へ移動し、ここでの患者は観察室で待機します。
					bRet = bChangeEmergencyRoomObservationPatient(erPAgent, ArrayListEmergencyRooms, ArrayListSevereInjuryObservationRooms, ArrayListObservationRooms, erWaitingRoom );
					if( bRet == true )
					{
						cObservationRoomLog.info(erPAgent.getId() + "," + "待機(初療室へは他の観察室の患者が移動)" + "," + "観察室");
					}
					// 初療室待機者に変わりはないので初療室待機フラグをONにします。
					erPAgent.vSetEmergencyRoomWaitFlag( 1 );
				}
				// 当人の場合
				if( bRet == false )
				{
					cObservationRoomLog.info(erPAgent.getId() + "," + "初療室へ移動準備開始" + "," + "観察室");

					// 初療室待機フラグをOFFにします。
					erPAgent.vSetObservationRoomWaitFlag( 0 );
					erPAgent.vSetEmergencyRoomWaitFlag( 0 );

					// 患者のいる位置を初療室に変更します。
					erPAgent.vSetLocation( 3 );

					// 観察フラグをOFFにします。
					erPAgent.vSetObservedFlag( 0 );

					// 観察室での看護師に見てもらったフラグはOFFにします。
					erPAgent.vSetNurseAttended( 0 );

					// 移動開始フラグを設定します。
					erPAgent.vSetMoveRoomFlag( 1 );
					erPAgent.vSetMoveWaitingTime( 0.0 );

					// 初療室へ患者エージェントを移動します。
					ArrayListEmergencyRooms.get(i).vSetPatientAgent( erPAgent );

				/* 看護師、医師、技士エージェントへメッセージを送信します。 */
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
	//				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
	//				{
	//					erEmergencyCinicalEngineerAgent = ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgents(j);
	//					erNurseAgent.vSendToEngineerAgentMessage( erPAgent, (int)erNurseAgent.getId(), (int)erEmergencyClinicalEngineerAgent.getId() );
	//				}
					// 看護師エージェントの対応を終了します。
					erNurseAgent.vSetAttending( 0 );

					// 対応を受けた患者エージェントを削除します。
					vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
					ArrayListNursePatientLoc.set( iLoc, -1 );

					// 初療室で担当する医師エージェントを設定します。(初療室対応患者が抜け落ちる現象の修正)
//					cObservationRoomLog.info(erPAgent.getId() + "," + erPAgent.erGetConsultationDoctorAgent().getId() + "," + erPAgent.erGetConsultationDoctorAgent().iGetAttending() + "," + "移動先の診察室の医師の状態" + "," + "待合室");
					ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetSurgeon(1);
					ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetAttending(1);

					cObservationRoomLog.info(erPAgent.getId() + "," + "初療室へ移動準備終了" + "," + "観察室");
					if( iInverseSimFlag == 1 )
					{
						// 移動先の経路を患者エージェントに設定します。
						erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListEmergencyRooms.get(i).erGetTriageNode() ) );
						cObservationRoomLog.info(erPAgent.getId() + "," + "初療室へ移動開始" + "," + "観察室");
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
			cObservationRoomLog.info(erPAgent.getId() + "," + "初療室満室" + "," + "観察室");
			// 空きがない場合は初療室待機フラグをONにしてそのまま重症観察室の空きを判定します。
			erPAgent.vSetEmergencyRoomWaitFlag( 1 );

			vJudgeMoveSereveInjuryObservationRoom( ArrayListSevereInjuryObservationRooms, erNurseAgent, erPAgent, iLoc );
		}
	}


	/**
	 * <PRE>
	 *    患者を登録します。
	 * </PRE>
	 * @param erPAgent 患者エージェント
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetPatientAgent( ERPatientAgent erPAgent )
	{
		ArrayListPatientAgents.add( erPAgent );
	}

	/**
	 * <PRE>
	 *    対応が終了した患者を削除します。
	 * </PRE>
	 * @param erPAgent 患者エージェント
	 * @param iLocation 担当が終了した看護師エージェントの番号の最小値
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vRemovePatientAgent( ERPatientAgent erPAgent, int iLocation )
	{
		int i;
		ArrayListPatientAgents.remove( erPAgent );
		for( i = 0;i < ArrayListNursePatientLoc.size(); i++ )
		{
			if( iLocation < ArrayListNursePatientLoc.get(i) )
			{
				ArrayListNursePatientLoc.set(i, ArrayListNursePatientLoc.get(i)-1 );
			}
		}
		erPAgent = null;
	}

	/**
	 * <PRE>
	 *   観察室の看護師エージェントの数を取得します。
	 * </PRE>
	 * @return	所属する看護師エージェントの数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public int iGetNurseAgentsNum()
	{
		return ArrayListNurseAgents.size();
	}

	/**
	 * <PRE>
	 *   観察室の看護師エージェント番号を患者番号から取得します。
	 * </PRE>
	 * @param iTargetLoc 患者の番号
	 * @return	該当する看護師の番号
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
	 *   1観察室に滞在している全患者エージェントを取得します。
	 * </PRE>
	 * @return 観察室に所属する全看護師エージェント
	 * @author kobayashi
	 * @since 2015/11/09
	 */
	public ArrayList<ERPatientAgent> erGetPatientAgents()
	{
		// TODO 自動生成されたメソッド・スタブ
		return ArrayListPatientAgents;
	}

	/**
	 * <PRE>
	 *   該当番号に当たる患者エージェントを取得します。
	 * </PRE>
	 * @param iLoc 患者の番号
	 * @return	該当する患者エージェントインスタンス
	 * @author kobayashi
	 * @since 2015/11/09
	 */
	public ERPatientAgent erGetPatientAgent(int iLoc)
	{
		// TODO 自動生成されたメソッド・スタブ
		return ArrayListPatientAgents.get(iLoc);
	}

	/**
	 * <PRE>
	 *   観察室の看護師エージェント番号を患者番号から取得します。
	 * </PRE>
	 * @return	観察室の全看護師
	 * @author kobayashi
	 * @since 2015/11/09
	 */
	public ArrayList<ERNurseAgent> erGetNurseAgents()
	{
		return ArrayListNurseAgents;
	}

	/**
	 * <PRE>
	 *   観察室の看護師エージェント番号を患者番号から取得します。
	 * </PRE>
	 * @param iLoc 看護師の番号
	 * @return	該当する看護師エージェントのインスタンス
	 * @author kobayashi
	 * @since 2015/11/09
	 */
	public ERNurseAgent erGetNurseAgent(int iLoc)
	{
		return ArrayListNurseAgents.get(iLoc);
	}

	/**
	 * <PRE>
	 *     他室へ移動する場合に最も重傷度あるいは緊急度が高い患者がどの部屋に在院しているのかを判定します。
	 *     対象は待合室、観察室、重傷観察室。
	 * </PRE>
	 * @param iJudgeFlag								0 トリアージ緊急度, 1 AIS重症度
	 * @param erPAgent									担当患者エージェント
	 * @param ArrayListSevereInjuryObservationRooms		全重症観察室
	 * @param ArrayListObservationRooms					全観察室
	 * @param erWaitingRoom								待合室
	 * @return 0 待合室
	 * 		   1 重傷観察室
	 * 		   2 観察室
	 * @author kobayashi
	 * @since 2015/11/05
	 */
	public int iJudgeEmergencyPatient( int iJudgeFlag, ERPatientAgent erPAgent, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms, ERWaitingRoom erWaitingRoom )
	{
		int i,j;
		int iTargetLoc = -1;
		int iMinEmergencyLevel = 6;
		double lfMaxAISLevel = 6.0;
		ERPatientAgent erTempAgent;

		cObservationRoomLog.info(erPAgent.getId() + "," + "観察室：患者部屋変更関数通ったよ～。");
		if( iJudgeFlag == 0 )
		{
			lfMaxAISLevel = erPAgent.lfGetMaxAIS();
			// 移動予定の患者よりも緊急度の高い患者がいないかどうかを確認します。
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					erTempAgent = ArrayListObservationRooms.get(i).erGetPatientAgent(j);
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( lfMaxAISLevel <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
					{
						cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "観察室にいるみたいだよ。");
						lfMaxAISLevel = erTempAgent.lfGetMaxAIS();
						iTargetLoc = 2;
					}
				}
			}
			for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					erTempAgent = ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgent(j);
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( lfMaxAISLevel <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
					{
						cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "重症観察室にいるみたいだよ。");
						lfMaxAISLevel = erTempAgent.lfGetMaxAIS();
						iTargetLoc = 1;
					}
				}
			}
			for( i = 0;i < erWaitingRoom.erGetPatientAgents().size(); i++ )
			{
				erTempAgent = erWaitingRoom.erGetPatientAgent(i);
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( erTempAgent.lfGetTimeCourse() > 0.0 )
				{
					if( lfMaxAISLevel <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
					{
						// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
						if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
							erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
							erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
							erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
						{
							cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "待合室にいるみたいだよ。");
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
			// 移動予定の患者よりも重傷度の高い患者がいないかどうかを確認します。
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					erTempAgent = ArrayListObservationRooms.get(i).erGetPatientAgent(j);
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( iMinEmergencyLevel >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId())
					{
						cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "観察室にいるみたいだよ。");
						iMinEmergencyLevel = erTempAgent.iGetEmergencyLevel();
						iTargetLoc = 2;
					}
				}
			}
			for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					erTempAgent = ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgent(j);
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( iMinEmergencyLevel >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId())
					{
						cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "重症観察室にいるみたいだよ。");
						iMinEmergencyLevel = erTempAgent.iGetEmergencyLevel();
						iTargetLoc = 1;
					}
				}
			}
			for( i = 0;i < erWaitingRoom.erGetPatientAgents().size(); i++ )
			{
				erTempAgent = erWaitingRoom.erGetPatientAgent(i);
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( erTempAgent.lfGetTimeCourse() > 0.0 )
				{
					if( iMinEmergencyLevel >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId())
					{
						// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
						if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
							erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
							erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
							erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
						{
							cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "待合室にいるみたいだよ。");
							iMinEmergencyLevel = erTempAgent.iGetEmergencyLevel();
							iTargetLoc = 0;
						}
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
	 * @param erPAgent									担当患者エージェント
	 * @param ArrayListSevereInjuryObservationRooms		全重症観察室
	 * @param ArrayListObservationRooms					全観察室
	 * @return 0 待合室
	 * 		   1 重傷観察室
	 * 		   2 観察室
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

		cObservationRoomLog.info(erPAgent.getId() + "," + "患者部屋変更判定関数通ったよ～。");
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
					cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "待合室にいるみたいだよ。");
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
					cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "重症観察室にいるみたいだよ。");
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
					cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "観察室にいるみたいだよ。");
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
	 *
	 * @param iJudgeFlag								0 トリアージ緊急度, 1 AIS重症度
	 * @param erPAgent									担当患者エージェント
	 * @param ArrayListObservationRooms					全観察室
	 * @param erWaitingRoom								待合室
	 * @return 0 待合室
	 * 		   1 重傷観察室
	 * 		   2 観察室
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

		cObservationRoomLog.info(erPAgent.getId() + "," + "患者部屋変更関数通ったよ～。, 観察室");
		if( iJudgeFlag == 0 )
		{
			lfMaxAISLevel = erPAgent.lfGetMaxAIS();
			// 移動予定の患者よりも緊急度の高い患者がいないかどうかを確認します。
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					erTempAgent = ArrayListObservationRooms.get(i).erGetPatientAgent(j);
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( lfMaxAISLevel <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
					{
						// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
						iLoc = ArrayListObservationRooms.get(i).iGetNurseAgentPatientLoc( i );
						if( iLoc != -1 )
						{
							cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "観察室にいるみたいだよ。");
							lfMaxAISLevel = erTempAgent.lfGetMaxAIS();
							iTargetLoc = 1;
						}
					}
				}
			}
			for( i = 0;i < erWaitingRoom.erGetPatientAgents().size(); i++ )
			{
				erTempAgent = erWaitingRoom.erGetPatientAgent(i);
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( erWaitingRoom.erGetPatientAgent(i).lfGetTimeCourse() > 0.0 )
				{
					if( lfMaxAISLevel <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId())
					{
						// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
						if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
							erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
							erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
							erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
						{
							cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "待合室にいるみたいだよ。");
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
			// 移動予定の患者よりも重傷度の高い患者がいないかどうかを確認します。
			for( i = 0;i < ArrayListObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					erTempAgent = ArrayListObservationRooms.get(i).erGetPatientAgent(j);
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( iMinEmergencyLevel >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId())
					{
						// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
						iLoc = ArrayListObservationRooms.get(i).iGetNurseAgentPatientLoc( i );
						if( iLoc != -1 )
						{
							cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "観察室にいるみたいだよ。");
							iMinEmergencyLevel = erTempAgent.iGetEmergencyLevel();
							iTargetLoc = 1;
						}
					}
				}
			}
			for( i = 0;i < erWaitingRoom.erGetPatientAgents().size(); i++ )
			{
				erTempAgent = erWaitingRoom.erGetPatientAgent(i);
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( erWaitingRoom.erGetPatientAgent(i).lfGetTimeCourse() > 0.0 )
				{
					if( iMinEmergencyLevel >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId() )
					{
						// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
						if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
							erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
							erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
							erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
						{
							cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "待合室にいるみたいだよ。");
							iMinEmergencyLevel = erTempAgent.iGetEmergencyLevel();
							iTargetLoc = 0;
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
	 * @param erPAgent									担当患者エージェント
	 * @param ArrayListObservationRooms					全観察室
	 * @param erWaitingRoom								待合室
	 * @return 0 待合室
	 * 		   1 重傷観察室
	 * 		   2 観察室
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

		cObservationRoomLog.info(erPAgent.getId() + "," + "患者部屋変更判定関数通ったよ～。");

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
						cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "観察室にいるみたいだよ。");
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
						cObservationRoomLog.info(erPAgent.getId() + "," + erTempAgent.getId() + "," + "待合室にいるみたいだよ。");
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
	 *     観察室から初療室へ移動する対象患者エージェントよりも
	 *     観察室に重症度の大きい患者がいないかどうかを判定し、
	 *     判定結果に基づいて部屋の変更を実施します。
	 * </PRE>
	 *
	 * @param erPAgent								移動対象の患者エージェント
	 * @param ArrayListEmergencyRooms				全初療室
	 * @param ArrayListSevereInjuryObservationRooms	全重症観察室
	 * @param ArrayListObservationRooms				全観察室
	 * @param erWaitingRoom							待合室
	 * @return							true		他に移動する患者あり。
	 * 									false 		移動する患者なし。
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
		cObservationRoomLog.info(erPAgent.getId() + "," + "患者部屋変更関数通ったよ～。");

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
			cObservationRoomLog.info(erPAgent.getId() + "," + "うむむ該当者がいなかったぞよ～。bChangeEmergencyRoomObservationPatient");
			return false;
		}
		erTempNurseAgent = ArrayListObservationRooms.get( iTargetRoomLoc ).erGetNurseAgent( iTargetNursePatientLoc );
		cObservationRoomLog.info(erPAgent.getId() + "," + erAgent.getId() + "," + "緊急度判定" + "," + "ターゲットの患者：" + iTargetPatientLoc + "," +  ArrayListObservationRooms.get( iTargetRoomLoc ).erGetPatientAgents().size() );
		erTempAgent = ArrayListObservationRooms.get( iTargetRoomLoc ).erGetPatientAgent( iTargetPatientLoc );

	// 初療室へ移動します。
		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			// 初療室に空きがある場合
			if( ArrayListEmergencyRooms.get(i).isVacant() == true )
			{
				// 初療室に空きがある場合
				cObservationRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動準備開始" + "," + "観察室");

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
				ArrayListObservationRooms.get( iTargetRoomLoc ).vRemovePatientAgent( erTempAgent, iTargetPatientLoc );
				ArrayListObservationRooms.get( iTargetRoomLoc ).vSetArrayListNursePatientLoc( iTargetNursePatientLoc, -1 );

				cObservationRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動準備終了" + "," + "観察室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erTempAgent.vSetMoveRoute( erTriageNodeManager.getRoute( ArrayListObservationRooms.get( iTargetRoomLoc ).erGetTriageNode(), ArrayListEmergencyRooms.get(i).erGetTriageNode() ) );
					cObservationRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動開始" + "," + "観察室");
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
	 *     観察室から移動する対象患者エージェントよりも
	 *     重症観察室に重症度の大きい患者がいないかどうかを判定し、
	 *     判定結果に基づいて部屋の変更を実施します。
	 * </PRE>
	 *
	 * @param erPAgent								移動対象の患者エージェント
	 * @param ArrayListEmergencyRooms				全初療室
	 * @param ArrayListSevereInjuryObservationRooms	全重症観察室
	 * @param ArrayListObservationRooms				全観察室
	 * @param erWaitingRoom							待合室
	 * @return							true		他に移動する患者あり。
	 * 									false 		移動する患者なし。
	 * @author kobayashi
	 * @since 2015/11/05
	 */
	public boolean bChangeEmergencyRoomSevereInjuryObservationPatient( ERPatientAgent erPAgent, ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms, ERWaitingRoom erWaitingRoom )
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
		ERDoctorAgent erEmergencyDoctorAgent = null;
		ERNurseAgent erEmergencyNurseAgent = null;


		cObservationRoomLog.info(erPAgent.getId() + "," + "患者部屋変更関数通ったよ～。");

		// 重症観察室で最も重症度あるいは緊急度の高い患者を見つけます。
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
		if( iJudgeUrgencyFlagMode == 1 )
		{
			for( i = 0;i < ArrayListSevereInjuryObservationRooms.size(); i++ )
			{
				for( j = 0;j < ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgents().size(); j++ )
				{
					erTempAgent = ArrayListSevereInjuryObservationRooms.get(i).erGetPatientAgent( j );
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( iMaxEmergency >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId() )
					{
						if( ArrayListSevereInjuryObservationRooms.get( i ).iGetNurseAgentPatientLoc( j ) != -1 )
						{
							iMaxEmergency = erTempAgent.iGetEmergencyLevel();
							lfMaxWaitTime = erTempAgent.lfGetObservationTime();
							iTargetRoomLoc = i;
							iTargetPatientLoc = j;
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
					if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
					if( lfMaxAIS <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId() )
					{
						if( ArrayListSevereInjuryObservationRooms.get( i ).iGetNurseAgentPatientLoc( j ) != -1 )
						{
							lfMaxAIS = erTempAgent.lfGetMaxAIS();
							lfMaxWaitTime = erTempAgent.lfGetObservationTime();
							iTargetRoomLoc = i;
							iTargetPatientLoc = j;
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
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				// 5時間以上経過している場合は強制的に移動できるように設定します。
				if( erTempAgent.lfGetObservationTime() >= 3600*2 && lfMaxWaitTime < erTempAgent.lfGetObservationTime() && erPAgent.getId() != erTempAgent.getId() )
				{
					if( ArrayListSevereInjuryObservationRooms.get( i ).iGetNurseAgentPatientLoc( j ) != -1 )
					{
						lfMaxWaitTime = erTempAgent.lfGetObservationTime();
						iTargetRoomLoc = i;
						iTargetPatientLoc = j;
						erAgent = erTempAgent;
					}
				}
			}
		}
		if( iTargetPatientLoc == -1 || iTargetPatientLoc == -1 )
		{
			// 基本的にはないとは思いますが、患者番号及び看護師番号に該当が存在しなかった場合は終了します。
			cObservationRoomLog.info(erPAgent.getId() + "," + "うむむ該当者がいなかったぞよ～。bChangeEmergencyRoomSevereInjuryObservationPatient");
			return false;
		}
		iTargetNursePatientLoc = ArrayListSevereInjuryObservationRooms.get( iTargetRoomLoc ).iGetNurseAgentPatientLoc( iTargetPatientLoc );
		erTempNurseAgent = ArrayListSevereInjuryObservationRooms.get( iTargetRoomLoc ).erGetNurseAgent( iTargetNursePatientLoc );
		// 初療室へ移動します。
		erTempAgent = ArrayListSevereInjuryObservationRooms.get( iTargetRoomLoc ).erGetPatientAgent( iTargetPatientLoc );
		cObservationRoomLog.info(erPAgent.getId() + "," + erAgent.getId() + "," + erTempAgent.getId() + "," + "緊急度判定" + "," + "ターゲットの患者：" + iTargetPatientLoc + "," +  ArrayListSevereInjuryObservationRooms.get( iTargetRoomLoc ).erGetPatientAgents().size() );

		// 移動処理を実施します。

		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			// 初療室に空きがある場合
			if( ArrayListEmergencyRooms.get(i).isVacant() == true )
			{
				// 初療室に空きがある場合
				cObservationRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動準備開始" + "," + "重症観察室");
				// 初療室待機フラグをOFFにします。
				erTempAgent.vSetSereveInjuryObservationRoomWaitFlag( 0 );
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

				// 初療室へ患者エージェントを移動します。
				ArrayListEmergencyRooms.get(i).vSetPatientAgent( erTempAgent );

				// その患者を対応している看護師エージェントがいなくなるので0に設定します。
				erTempAgent.vSetNurseAgent( 0 );

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

				// 初療室で担当する医師エージェントを設定します。(初療室対応患者が抜け落ちる現象の修正)
				ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetSurgeon(1);
				ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetAttending(1);

				cObservationRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動準備終了" + "," + "重症観察室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erTempAgent.vSetMoveRoute( erTriageNodeManager.getRoute( ArrayListSevereInjuryObservationRooms.get( iTargetRoomLoc ).erGetTriageNode(), ArrayListEmergencyRooms.get(i).erGetTriageNode() ) );
					cObservationRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動開始" + "," + "重症観察室");
				}
				erTempAgent = null;
				break;
			}
		}
		return true;
	}

	/**
	 * <PRE>
	 *     観察室から初療室へ移動する患者エージェントよりも待合室に
	 *     重症度の大きい患者がいないかどうかを判定し、
	 *     判定結果に基づいて部屋の変更を実施します。
	 * </PRE>
	 *
	 * @param erPAgent								移動対象の患者エージェント
	 * @param ArrayListEmergencyRooms				全初療室
	 * @param erWaitingRoom							待合室
	 * @param ArrayListObservationRooms				全観察室
	 * @param ArrayListSevereInjuryObservationRooms	全重症観察室
	 * @return							true		他に移動する患者あり。
	 * 									false 		移動する患者なし。
	 * @author kobayashi
	 * @since 2015/11/05
	 */
	public boolean bChangeEmergencyRoomWaitingRoomPatient( ERPatientAgent erPAgent, ArrayList<EREmergencyRoom> ArrayListEmergencyRooms, ERWaitingRoom erWaitingRoom, ArrayList<ERObservationRoom> ArrayListObservationRooms, ArrayList<ERSevereInjuryObservationRoom> ArrayListSevereInjuryObservationRooms )
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
		ERDoctorAgent erEmergencyDoctorAgent = null;
		ERNurseAgent erEmergencyNurseAgent = null;

		if( erWaitingRoom.erGetPatientAgents().isEmpty() == true )
		{
			// 待合室が空室の場合は待合室へ移動するようにします。
			return false;
		}
		cObservationRoomLog.info(erPAgent.getId() + "," + "患者部屋変更関数通ったよ～。");

		if( iJudgeUrgencyFlagMode == 1 )
		{
			// 重症観察室で最も重症度あるいは緊急度の高い患者を見つけます。
			for( j = 0;j < erWaitingRoom.erGetPatientAgents().size(); j++ )
			{
				erTempAgent = erWaitingRoom.erGetPatientAgent( j );
				// まだ、到着していない患者は対象外とします。
				if( erTempAgent.lfGetTimeCourse() <= 0.0 ) continue;
				// 移動中は対象外とします。
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( iMaxEmergency >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId() )
				{
					// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
					if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
						erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
					{
						iMaxEmergency = erTempAgent.iGetEmergencyLevel();
						lfMaxWaitTime = erTempAgent.lfGetWaitTime();
						iTargetRoomLoc = 0;
						iTargetPatientLoc = j;
						iTargetNursePatientLoc = erWaitingRoom.iGetNurseAgentPatientLoc(iTargetPatientLoc);
						erAgent = erTempAgent;
					}
				}
			}
		}
		else
		{
			for( j = 0;j < erWaitingRoom.erGetPatientAgents().size(); j++ )
			{
				erTempAgent = erWaitingRoom.erGetPatientAgent( j );
				// まだ、到着していない患者は対象外とします。
				if( erTempAgent.lfGetTimeCourse() <= 0.0 ) continue;
				// 移動中は対象外とします。
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( lfMaxAIS <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId() )
				{
					// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
					if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
						erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
					{
						lfMaxAIS = erTempAgent.lfGetMaxAIS();
						lfMaxWaitTime = erTempAgent.lfGetWaitTime();
						iTargetRoomLoc = 0;
						iTargetPatientLoc = j;
						iTargetNursePatientLoc = erWaitingRoom.iGetNurseAgentPatientLoc(iTargetPatientLoc);
						erAgent = erTempAgent;
					}
				}
			}
		}
		lfMaxWaitTime = -Double.MAX_VALUE;
		for( j = 0;j < erWaitingRoom.erGetPatientAgents().size(); j++ )
		{
			erTempAgent = erWaitingRoom.erGetPatientAgent( j );
			// まだ、到着していない患者は対象外とします。
			if( erTempAgent.lfGetTimeCourse() <= 0.0 ) continue;
			// 移動中は対象外とします。
			if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
			// 5時間以上経過している場合は強制的に移動できるように設定します。
			if( erTempAgent.lfGetWaitTime() >= 3600*2 && lfMaxWaitTime <= erTempAgent.lfGetWaitTime() && erPAgent.getId() != erTempAgent.getId() )
			{
				// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
				if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
					erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
					erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
					erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
				{
					lfMaxWaitTime = erTempAgent.lfGetWaitTime();
					iTargetRoomLoc = 0;
					iTargetPatientLoc = j;
					iTargetNursePatientLoc = erWaitingRoom.iGetNurseAgentPatientLoc(iTargetPatientLoc);
					erAgent = erTempAgent;
				}
			}
		}
		if( iTargetPatientLoc == -1 )
		{
			// 基本的にはないとは思いますが、患者番号及び看護師番号に該当が存在しなかった場合は終了します。
			cObservationRoomLog.info(erPAgent.getId() + "," + "うむむ該当者がいなかったぞよ～。bChangeEmergencyRoomWaitingRoomPatient");
			return false;
		}
		if( iTargetNursePatientLoc != -1 )
		{
			erTempNurseAgent = erWaitingRoom.erGetNurseAgent( iTargetNursePatientLoc );
		}
		erTempAgent = erWaitingRoom.erGetPatientAgent( iTargetPatientLoc );
		cObservationRoomLog.info(erPAgent.getId() + "," + erAgent.getId() + "," + "," + erTempAgent.getId() + "," + "緊急度判定" + "," + "ターゲットの患者：" + iTargetPatientLoc + "," +  erWaitingRoom.erGetPatientAgents().size() );

	// 初療室へ移動処理を実施します。

		for( i = 0;i < ArrayListEmergencyRooms.size(); i++ )
		{
			// 初療室に空きがある場合
			if( ArrayListEmergencyRooms.get(i).isVacant() == true )
			{
				// 初療室に空きがある場合
				cObservationRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動準備開始" + "," + "待合室");
				// 初療室待機フラグをOFFにします。
				erTempAgent.vSetSereveInjuryObservationRoomWaitFlag( 0 );
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

				// 初療室へ患者エージェントを移動します。
				ArrayListEmergencyRooms.get(i).vSetPatientAgent( erTempAgent );

				// その患者を対応している看護師エージェントがいなくなるので0に設定します。
				erTempAgent.vSetNurseAgent( 0 );

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
					erWaitingRoom.vRemovePatientAgent( erTempAgent, iTargetPatientLoc );
					erWaitingRoom.vSetArrayListNursePatientLoc( iTargetNursePatientLoc, -1 );
				}
				else
				{
					erWaitingRoom.vRemovePatientAgent( erTempAgent );
				}
				// 初療室で担当する医師エージェントを設定します。(初療室対応患者が抜け落ちる現象の修正)
				ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetSurgeon(1);
				ArrayListEmergencyRooms.get(i).cGetSurgeonDoctorAgent().vSetAttending(1);

				cObservationRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動準備終了" + "," + "待合室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erTempAgent.vSetMoveRoute( erTriageNodeManager.getRoute( erWaitingRoom.erGetTriageNode(), ArrayListEmergencyRooms.get(i).erGetTriageNode() ) );
					cObservationRoomLog.info(erTempAgent.getId() + "," + "初療室へ移動開始" + "," + "待合室");
				}
				erTempAgent = null;
				break;
			}
		}

		return true;
	}

	/**
	 * <PRE>
	 *     観察室から診察室へ移動する対象患者エージェントよりも待合室に重症度の大きい患者がいないかどうかを判定し、
	 *     判定結果に基づいて部屋の変更を実施します。
	 * </PRE>
	 * @param erPAgent								移動対象の患者エージェント
	 * @param ArrayListConsultationRooms			全診察室
	 * @param ArrayListObservationRooms				全観察室
	 * @param erWaitingRoom							待合室
	 * @return							true		他に移動する患者あり。
	 * 									false 		移動する患者なし。
	 * @author kobayashi
	 * @since 2015/11/05
	 */
	public boolean bChangeConsultationRoomWaitingRoomPatient( ERPatientAgent erPAgent, ArrayList<ERConsultationRoom> ArrayListConsultationRooms, ArrayList<ERObservationRoom> ArrayListObservationRooms, ERWaitingRoom erWaitingRoom )
	{
		int i,j;
		int iLoc = -1;
		int iTargetRoomLoc = -1;
		int iTargetPatientLoc = -1;
		int iTargetNursePatientLoc = -1;
		int iTargetCount = -1;
		int iMaxEmergency = Integer.MAX_VALUE;
		double lfMaxAIS = -Double.MAX_VALUE;
		double lfMaxWaitTime = -Double.MAX_VALUE;
		boolean bRet = false;

		ERPatientAgent erTempAgent = null;
		ERPatientAgent erAgent = null;
		ERNurseAgent erTempNurseAgent = null;
		ERNurseAgent erConsultationNurseAgent = null;
		ERDoctorAgent erConsultationDoctorAgent = null;

		if( erWaitingRoom.erGetPatientAgents().isEmpty() == true )
		{
			// 待合室が空室の場合は元々の対象車が移動するようにします。
			return false;
		}
		cObservationRoomLog.info(erPAgent.getId() + "," + "患者部屋変更関数通ったよ～。");

		if( iJudgeUrgencyFlagMode == 1 )
		{
			// 待合室で最も重症度あるいは緊急度の高い患者を見つけます。
			for( i = 0;i < erWaitingRoom.erGetPatientAgents().size(); i++ )
			{
				erTempAgent = erWaitingRoom.erGetPatientAgent( i );
				// まだ、到着していない患者は対象外とします。
				if( erTempAgent.lfGetTimeCourse() <= 0.0 ) continue;
				// 移動中は対象外とします。
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( iMaxEmergency >= erTempAgent.iGetEmergencyLevel() && erPAgent.getId() != erTempAgent.getId() )
				{
					// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
					if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
						erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
					{
						iMaxEmergency = erTempAgent.iGetEmergencyLevel();
						lfMaxWaitTime = erTempAgent.lfGetWaitTime();
						iTargetRoomLoc = 0;
						iTargetPatientLoc = i;
						iTargetNursePatientLoc = erWaitingRoom.iGetNurseAgentPatientLoc(iTargetPatientLoc);
						erAgent = erTempAgent;
					}
				}
			}
		}
		else
		{
			for( i = 0;i < erWaitingRoom.erGetPatientAgents().size(); i++ )
			{
				erTempAgent = erWaitingRoom.erGetPatientAgent( i );
				// まだ、到着していない患者は対象外とします。
				if( erTempAgent.lfGetTimeCourse() <= 0.0 ) continue;
				// 移動中は対象外とします。
				if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
				if( lfMaxAIS <= erTempAgent.lfGetMaxAIS() && erPAgent.getId() != erTempAgent.getId() )
				{
					// 診察室で受診し、他の室へ移動する患者さん以外を対象とします。
					if( erTempAgent.iGetExaminationAngiographyRoomWaitFlag() == 0 && erTempAgent.iGetExaminationCTRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationMRIRoomWaitFlag() == 0 && erTempAgent.iGetExaminationXRayRoomWaitFlag() == 0 &&
						erTempAgent.iGetExaminationFastRoomWaitFlag() == 0 && erTempAgent.iGetEmergencyRoomWaitFlag() == 0 &&
						erTempAgent.iGetGeneralWardRoomWaitFlag() == 0 && erTempAgent.iGetOperationRoomWaitFlag() == 0 )
					{
						lfMaxAIS = erTempAgent.lfGetMaxAIS();
						lfMaxWaitTime = erTempAgent.lfGetWaitTime();
						iTargetRoomLoc = 0;
						iTargetPatientLoc = i;
						iTargetNursePatientLoc = erWaitingRoom.iGetNurseAgentPatientLoc(iTargetPatientLoc);
						erAgent = erTempAgent;
					}
				}
			}
		}
		lfMaxWaitTime = -Double.MAX_VALUE;
		for( i = 0;i < erWaitingRoom.erGetPatientAgents().size(); i++ )
		{
			erTempAgent = erWaitingRoom.erGetPatientAgents().get( i );
			// まだ、到着していない患者は対象外とします。
			if( erTempAgent.lfGetTimeCourse() <= 0.0 ) continue;
			// 移動中は対象外とします。
			if( erTempAgent.iGetMoveRoomFlag() == 1 ) continue;
			// 5時間以上経過している場合は強制的に移動できるように設定します。
			if( erTempAgent.lfGetWaitTime() >= 3600*2 && lfMaxWaitTime <= erTempAgent.lfGetWaitTime() && erPAgent.getId() != erTempAgent.getId() )
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
					iTargetNursePatientLoc = erWaitingRoom.iGetNurseAgentPatientLoc(iTargetPatientLoc);
					erAgent = erTempAgent;
				}
			}
		}
		if( iTargetPatientLoc == -1 )
		{
			// 基本的にはないとは思いますが、患者番号及び看護師番号に該当が存在しなかった場合は終了します。
			cObservationRoomLog.info(erPAgent.getId() + "," + "うむむ該当者がいなかったぞよ～。bChangeConsultationRoomWaitingRoomPatient");
			return false;
		}
		if( iTargetNursePatientLoc != -1 )
		{
			erTempNurseAgent = erWaitingRoom.erGetNurseAgent( iTargetNursePatientLoc );
		}
		// 初療室へ移動します。
		erTempAgent = erWaitingRoom.erGetPatientAgent( iTargetPatientLoc );
		cObservationRoomLog.info(erPAgent.getId() + "," + erAgent.getId() + "," + erTempAgent.getId() + "," + "," + "緊急度判定" + "," + "ターゲットの患者：" + iTargetPatientLoc + "," +  ArrayListPatientAgents.size() );

		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			// 診察室に空きがある場合
			if( ArrayListConsultationRooms.get(i).isVacant() == true )
			{
				// 診察室に空きがある場合
				cObservationRoomLog.info(erTempAgent.getId() + "," + "診察室へ移動準備開始" + "," + "待合室");

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
//					for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
//					{
//						erEmergencyCinicalEngineerAgent = ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgents(j);
//						erNurseAgent.vSendToEngineerAgentMessage( erTempAgent, (int)erNurseAgent.getId(), (int)erEmergencyClinicalEngineerAgent.getId() );
//					}
					// 看護師エージェントの対応を終了します。
					erTempNurseAgent.vSetAttending( 0 );

					// 対応を受けた患者エージェントを削除します。
					erWaitingRoom.vRemovePatientAgent( erTempAgent, iTargetPatientLoc );
					erWaitingRoom.vSetArrayListNursePatientLoc( iTargetNursePatientLoc, -1 );
				}
				else
				{
					erWaitingRoom.vRemovePatientAgent( erTempAgent );
				}
				cObservationRoomLog.info(erPAgent.getId() + "," + ArrayListConsultationRooms.get(i).cGetDoctorAgent().getId() + "," + ArrayListConsultationRooms.get(i).cGetDoctorAgent().iGetAttending() + "," + "移動先の診察室の医師の状態" + "," + "待合室");
				ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetAttending(1);

				// 医師の診察時間を設定します。
				ArrayListConsultationRooms.get(i).cGetDoctorAgent().isJudgeConsultationTime( erTempAgent );

				cObservationRoomLog.info(erTempAgent.getId() + "," + "診察室へ移動準備終了" + "," + "待合室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erTempAgent.vSetMoveRoute( erTriageNodeManager.getRoute( erWaitingRoom.erGetTriageNode(), ArrayListConsultationRooms.get(i).erGetTriageNode() ) );
					cObservationRoomLog.info(erTempAgent.getId() + "," + "診察室へ移動開始" + "," + "待合室");
				}
				erTempAgent = null;
				break;
			}
		}
		return true;
	}


	/**
	 * <PRE>
	 *     観察室から診察室へ移動する対象患者エージェントよりも
	 *     観察室に重症度の大きい患者がいないかどうかを判定し、
	 *     判定結果に基づいて部屋の変更を実施します。
	 * </PRE>
	 *
	 * @param erPAgent								移動対象の患者エージェント
	 * @param ArrayListConsultationRooms			全診察室
	 * @param ArrayListObservationRooms				全観察室
	 * @return							true		他に移動する患者あり。
	 * 									false 		移動する患者なし。
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
		cObservationRoomLog.info(erPAgent.getId() + "," + "患者部屋変更関数通ったよ～。");

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
			cObservationRoomLog.info(erPAgent.getId() + "," + "うむむ該当者がいなかったぞよ～。bChangeConsultationRoomObservationRoomPatient");
			return false;
		}
		erTempNurseAgent = ArrayListObservationRooms.get( iTargetRoomLoc ).erGetNurseAgent( iTargetNursePatientLoc );
		// 診察室へ移動します。
		erTempAgent = ArrayListObservationRooms.get( iTargetRoomLoc ).erGetPatientAgent( iTargetPatientLoc );
		cObservationRoomLog.info(erPAgent.getId() + "," + erAgent.getId() + "," + erTempAgent.getId() + "," + "緊急度判定" + "," + "ターゲットの患者：" + iTargetPatientLoc + "," +  ArrayListPatientAgents.size() );

		for( i = 0;i < ArrayListConsultationRooms.size(); i++ )
		{
			// 診察室に空きがある場合
			if( ArrayListConsultationRooms.get(i).isVacant() == true )
			{
				// 診察室に空きがある場合
				cObservationRoomLog.info(erTempAgent.getId() + "," + "診察室へ移動準備開始" + "," + "観察室");

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

				cObservationRoomLog.info(erPAgent.getId() + "," + ArrayListConsultationRooms.get(i).cGetDoctorAgent().getId() + "," + ArrayListConsultationRooms.get(i).cGetDoctorAgent().iGetAttending() + "," + "移動先の診察室の医師の状態" + "," + "観察室");
				ArrayListConsultationRooms.get(i).cGetDoctorAgent().vSetAttending(1);

				// 医師の診察時間を設定します。
				ArrayListConsultationRooms.get(i).cGetDoctorAgent().isJudgeConsultationTime( erTempAgent );

				cObservationRoomLog.info(erTempAgent.getId() + "," + "診察室へ移動準備終了" + "," + "観察室");
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erTempAgent.vSetMoveRoute( erTriageNodeManager.getRoute( ArrayListObservationRooms.get( iTargetRoomLoc ).erGetTriageNode(), ArrayListConsultationRooms.get(i).erGetTriageNode() ) );
					cObservationRoomLog.info(erTempAgent.getId() + "," + "診察室へ移動開始" + "," + "観察室");
				}
				erTempAgent = null;
				break;
			}
		}
		return true;
	}

	/**
	 * <PRE>
	 *    待合室看護師が対応中かどうかを判定します。
	 * </PRE>
	 * @return false 全員対応している
	 *         true  空きの看護師がいる
	 * @author kobayashi
	 * @since 2015/08/03
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
			if( ArrayListNurseAgents.get(i).iGetAttending() == 1 || ArrayListNursePatientLoc.get(i) > -1 )
			{
				iCount++;
			}
		}
		if( iCount == ArrayListNurseAgents.size() )
		{
			bRet = false;
		}
		return bRet;
	}

	/**
	 * <PRE>
	 *    空いている看護師を割り当てます。
	 * </PRE>
	 * @return false 割り当てることができませんでした。
	 *         true  割り当てることができました。
	 */
	public boolean bAssignVacantNurse()
	{
		int i,j;
		int iEnableNurse = 0;
		boolean bRet = false;

		synchronized( csObservationRoomCriticalSection )
		{
			for( i = 0 ;i < ArrayListNurseAgents.size(); i++ )
			{
//				cObservationRoomLog.info("ArrayListNursePatientLoc Nurse:" + i + ",Patient:" + ArrayListNursePatientLoc.get(i) );
//				cObservationRoomLog.info("ArrayListNursePatientLoc size:" + ArrayListNursePatientLoc.size());
//				cObservationRoomLog.info("ArrayListPatientAgents size:" + ArrayListPatientAgents.size());
				// どの看護師が空いているかどうかを調べます。
				if( ArrayListNurseAgents.get(i).iGetAttending() == 0 )
				{
					// 空いている看護師に対して患者を割り振ります。
					iEnableNurse = i;
					for( j = 0;j < ArrayListPatientAgents.size(); j++ )
					{
						if( ArrayListPatientAgents.get(j).iGetNurseAttended() == 0 )
						{
							ArrayListNurseAgents.get(iEnableNurse).vSetAttending(1);
							ArrayListNursePatientLoc.set( iEnableNurse, j );
							ArrayListPatientAgents.get(j).vSetNurseAttended( 1 );
							ArrayListPatientAgents.get(j).vSetNurseAgent(ArrayListNurseAgents.get(i).getId());
							bRet = true;
							break;
						}
					}
				}
			}
		}
		return bRet;
	}

	@Override
	public void action(long timeStep)
	{
		// TODO 自動生成されたメソッド・スタブ
		int i,j;
		int iLoc = 0;
		double lfSecond = 0.0;
		lfSecond = timeStep / 1000.0;
		lfTotalTime += lfSecond;

		try
		{
			synchronized( csObservationRoomCriticalSection )
			{
				// 死亡患者がいる場合は削除をする。
				if( ArrayListPatientAgents != null && ArrayListPatientAgents.size() > 0 )
				{
					for( i = ArrayListPatientAgents.size()-1; i >= 0; i-- )
					{
						if( ArrayListPatientAgents.get(i) != null )
						{
							if( ArrayListPatientAgents.get(i).iGetSurvivalFlag() == 0 || ArrayListPatientAgents.get(i).iGetDisChargeFlag() == 1 )
							{
								for( j = 0;j < ArrayListNursePatientLoc.size(); j++ )
								{
									if( ArrayListNursePatientLoc.get(j)  == i )
									{
										cObservationRoomLog.info(ArrayListPatientAgents.get(i).getId() + "," + "死亡患者登録削除中" + "," + "観察室");
										ArrayListNursePatientLoc.set( j, -1 );
										iLoc = i;
									}
								}
								// なくなられたエージェントが配列上いた位置を削除するため、それ以降のデータがすべて1繰り下がるので、
								// それに対応する。そうしないと配列サイズを超えて参照したエラーが発生します。
								for( j = 0;j < ArrayListNursePatientLoc.size(); j++ )
								{
									if( iLoc < ArrayListNursePatientLoc.get(j) )
									{
										ArrayListNursePatientLoc.set( j, ArrayListNursePatientLoc.get(j)-1 );
									}
								}
								ArrayListPatientAgents.set(i, null);
								ArrayListPatientAgents.remove(i);

								break;
							}
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
	}


	/**
	 * <PRE>
	 * 　看護師、患者対応フラグ配列へ設定します。
	 * </PRE>
	 * @param iNurseLoc		担当看護師の番号
	 * @param iPatientLoc	該当する患者の番号
	 */
	public void vSetArrayListNursePatientLoc( int iNurseLoc, int iPatientLoc )
	{
		ArrayListNursePatientLoc.set( iNurseLoc, iPatientLoc );
	}

	/**
	 * <PRE>
	 *    観察室のログ出力を設定します。
	 * </PRE>
	 * @param log	ロガークラスインスタンス
	 */
	public void vSetLog(Logger log)
	{
		// TODO 自動生成されたメソッド・スタブ
		cObservationRoomLog = log;
	}

	/**
	 * <PRE>
	 *    観察室のX座標を取得します。
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
	 *    観察室のY座標を取得します。
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
	 *    観察室の横幅を取得します。
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
	 *    観察室の縦幅を取得します。
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
	 *    観察室の階数を取得します。
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
	 *    観察室のX座標を格納します。
	 * </PRE>
	 * @param iData	X座標
	 */
	public void vSetX( int iData )
	{
		iDrawX = iData;
	}

	/**
	 * <PRE>
	 *    観察室のY座標を格納します。
	 * </PRE>
	 * @param iData	Y座標
	 */
	public void vSetY( int iData )
	{
		iDrawY = iData;
	}

	/**
	 * <PRE>
	 *    観察室のZ座標を格納します。
	 * </PRE>
	 * @param iData	Z座標
	 */
	public void vSetZ( int iData )
	{
		iDrawZ = iData;
	}

	/**
	 * <PRE>
	 *   観察室の横幅を格納します。
	 * </PRE>
	 * @param iData	横幅
	 */
	public void vSetWidth( int iData )
	{
		iDrawWidth = iData;
	}

	/**
	 * <PRE>
	 *    観察室の縦幅を格納します。
	 * </PRE>
	 * @param iData	縦幅
	 */
	public void vSetHeight( int iData )
	{
		iDrawHeight = iData;
	}

	/**
	 * <PRE>
	 *    観察室の階数を格納します。
	 * </PRE>
	 * @param iData	階数
	 */
	public void vSetF( int iData )
	{
		iDrawF = iData;
	}

	/**
	 * <PRE>
	 *   観察室に所属しているエージェントの座標を設定します。
	 * </PRE>
	 * @param iLoc			対象としている看護師の番号
	 * @param cCurNode		対象としている看護師のノード
	 */
	public void vSetAffiliationAgentPosition( ERTriageNode cCurNode, int iLoc )
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;

		double lfX = 0.0;
		double lfY = 0.0;
		double lfZ = 0.0;

//		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
//		{
//			// 医師エージェントの位置を設定します。
//			lfX = this.getPosition().getX()+3*rnd.NextUnif();
//			lfY = this.getPosition().getY()+3*rnd.NextUnif();
//			lfZ = this.getPosition().getZ()+3*rnd.NextUnif();
//			ArrayListDoctorAgents.get(i).setPosition( lfX, lfY, lfZ );
//		}
//		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
//		{
//			// 看護師エージェントの位置を設定します。
//			lfX = this.getPosition().getX()+30*(2*rnd.NextUnif()-1);
//			lfY = this.getPosition().getY()+30*(2*rnd.NextUnif()-1);
//			lfZ = this.getPosition().getZ()+30*(2*rnd.NextUnif()-1);
//			ArrayListNurseAgents.get(i).setPosition( lfX, lfY, lfZ );
//		}
//		for( i = 0;i < ArrayListClinicalEngineerAgents.size(); i++ )
//		{
//			// 医療技師エージェントの位置を設定します。
//			lfX = this.getPosition().getX()+10*(2*rnd.NextUnif()-1);
//			lfY = this.getPosition().getY()+10*(2*rnd.NextUnif()-1);
//			lfZ = this.getPosition().getZ()+10*(2*rnd.NextUnif()-1);
//			ArrayListClinicalEngineerAgents.get(i).setPosition( lfX, lfY, lfZ );
//		}
		// 看護師エージェントをそれぞれのノード位置に設定します。
		if( iLoc < ArrayListNurseAgents.size() )
		{
			lfX = cCurNode.getPosition().getX();
			lfY = cCurNode.getPosition().getY();
			lfZ = cCurNode.getPosition().getZ();
			ArrayListNurseAgents.get( iLoc ).setPosition( lfX, lfY, lfZ );
			ArrayListNurseAgents.get( iLoc ).vSetTriageNode( cCurNode );
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
	 *    現在選択されている観察室のノードを取得します。
	 * </PRE>
	 * @return	選択中の観察室のノード
	 */
	public ERTriageNode erGetTriageNode()
	{
		int i;
		ERTriageNode erNode = null;
		// 割り当てられていない看護師エージェントを調べて、そこに移動するように設定します。
		for( i = 0;i < ArrayListNursePatientLoc.size(); i++ )
		{
			if( ArrayListNursePatientLoc.get(i) == -1 )
			{
				erNode = ArrayListNurseAgents.get(i).erGetTriageNode();
				break;
			}
		}
		return erNode;
//		return erTriageNode;
	}

	/**
	 * <PRE>
	 *   観察室のノードを設定します。
	 * </PRE>
	 * @param erNode 観察室のノード
	 */
	public void vSetTriageNode( ERTriageNode erNode )
	{
		erTriageNode = erNode;
	}

	/**
	 * <PRE>
	 *   逆シミュレーションモードを設定します。
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

	public void vCreatePatientAgents()
	{
		// TODO 自動生成されたメソッド・スタブ
		ArrayListPatientAgents = new ArrayList<ERPatientAgent>();
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
	 * @param lfTriageProcessTime		観察プロセス起動時間
	 * @param erNurseAgent				看護師エージェント
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
	 *    現時点で観察室に患者がいるかどうかを取得します。
	 * </PRE>
	 * @return	現在滞在している患者の数
	 */
	public synchronized int iGetPatientInARoom()
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;
		int iCount = 0;

		synchronized( csObservationRoomCriticalSection )
		{
			if( ArrayListPatientAgents != null )
			{
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					if( ArrayListPatientAgents.get(i) != null )
					{
						if( ArrayListPatientAgents.get(i).iGetSurvivalFlag() == 1 )
						{
							iCount++;
						}
					}
				}
			}
		}
		return iCount;
	}

	/**
	 * <PRE>
	 *    クリティカルセクションを設定します。
	 * </PRE>
	 * @param cs	クリティカルセクションのインスタンス
	 */
	public void vSetCriticalSection(Object cs )
	{
		// TODO 自動生成されたメソッド・スタブ
		csObservationRoomCriticalSection = cs;
	}

	/**
	 * <PRE>
	 *   メルセンヌツイスターインスタンスを設定します。
	 * </PRE>
	 * @param sfmtRandom メルセンヌツイスターインスタンス(部屋自体)
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vSetRandom(utility.sfmt.Rand sfmtRandom)
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
	 * @return	緊急度別トリアージ受診人数
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public synchronized int iGetTriageCategoryPatientNum( int iCategory )
	{
		int i;
		int iCategoryPatientNum = 0;

		synchronized( csObservationRoomCriticalSection )
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
	 *   観察室の患者エージェントの数を取得します。
	 *   nullでなければ1人いるので1と返却します。
	 * </PRE>
	 * @return		患者エージェントの総数
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public int iGetPatientAgentsNum()
	{
		int i;

		return ArrayListPatientAgents != null ? ArrayListPatientAgents.size() : 0;
	}


	/**
	 * <PRE>
	 *    現在、観察室に最も長く病院にいる患者の在院時間を取得します。
	 * </PRE>
	 * @return		最も長く病院に在院する患者の在院時間
	 */
	public synchronized double lfGetLongestStayPatient()
	{
		int i;
		double lfLongestStayTime = -Double.MAX_VALUE;

		synchronized( csObservationRoomCriticalSection )
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
	 */
	public synchronized void vLastBedTime()
	{
		int i;
		double lfLongestTime = -Double.MAX_VALUE;
		double lfLastTime = -Double.MAX_VALUE;

		synchronized( csObservationRoomCriticalSection )
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
		}
		lfLongestTotalTime = lfLongestTime;
		lfLastBedTime = lfLastTime;
	}

	/**
	 * <PRE>
	 *    現在最も長く病院にいる時間の総時間を返します。
	 * </PRE>
	 * @return	最も長く病院にいる時間の総時間
	 */
	public double lfGetLongestStayHospitalTotalTime()
	{
		return lfLongestTotalTime;
	}

	/**
	 * <PRE>
	 *    最後に入院と診断された患者の病院到達から入院までの時間を返します。
	 * </PRE>
	 * @return	最後に入院と診断された患者の病院到達から入院までの時間
	 */
	public double lfGetLastBedTime()
	{
		return lfLastBedTime;
	}
}
