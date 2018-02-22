package triage.room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import triage.agent.ERDoctorAgent;
import triage.agent.ERDoctorAgentException;
import triage.agent.ERNurseAgent;
import triage.agent.ERPatientAgent;
import utility.node.ERTriageNode;
import utility.node.ERTriageNodeManager;
import utility.sfmt.Rand;


public class ERGeneralWardRoom extends Agent
{
	private static final long serialVersionUID = 6843485607134630501L;

	private Rand rnd;											// 乱数クラス
	private ERDoctorAgent erDoctorAgent;						// 医師エージェント（本来はいないがいないとうまくいかないため）
	private ArrayList<ERNurseAgent> ArrayListNurseAgents;		// 一般病棟を担当している看護師エージェント
	private ArrayList<ERPatientAgent> ArrayListPatientAgents;	// 一般病棟で入院しているエージェント
	private ArrayList<Integer> ArrayListPatientNurseLoc;		// 看護師と患者の対応位置
	private double lfTotalTime;									// シミュレーション経過時間
	private int iHospitalBedNum;								// 病床数
	private int iDisChargeNum;									// 退院数
	private Logger cGeneralWardLog;						// ログ出力

	private ERTriageNodeManager erTriageNodeManager;
	private ERTriageNode erTriageNode;
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

	private Object csGeneralWardCriticalSection;		// クリティカルセクション用

	private double lfLastBedTime;
	private double lfLongestTotalTime;

	/**
	 * <PRE>
	 *   コンストラクタです。
	 *   一般病棟の初期化を行います。
	 * </PRE>
	 */
	public ERGeneralWardRoom()
	{
		vInitialize();
	}

	public void vInitialize()
	{
		ArrayListNurseAgents		= new ArrayList<ERNurseAgent>();
		erDoctorAgent				= new ERDoctorAgent();
		ArrayListPatientAgents		= new ArrayList<ERPatientAgent>();	// 一般病棟で入院しているエージェント
		ArrayListPatientNurseLoc	= new ArrayList<Integer>();			// 看護師と患者の対応位置
		iHospitalBedNum				= 0;								// 病床数
		lfTotalTime					= 0;								// シミュレーション経過時間
		iDisChargeNum				= 0;								// 退院数

//		long seed;
//		seed = System.currentTimeMillis();
//		rnd = null;
//		rnd = new Sfmt( (int)seed );

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

		erDoctorAgent.vSetReadWriteFile( iFileWriteMode );
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetReadWriteFile( iFileWriteMode );
		}
	}

	/**
	 * <PRE>
	 *    終了処理を実行します。
	 * </PRE>
	 * @throws IOException		終了処理エラー
	 */
	public synchronized void vTerminate() throws IOException
	{
		int i;

		synchronized( csGeneralWardCriticalSection )
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
						ArrayListPatientAgents.remove(i);
					}
				}
				ArrayListPatientAgents = null;
			}

			// 医師エージェントの終了処理を行います。
			if( erDoctorAgent != null )
			{
				erDoctorAgent.vTerminate();
				this.getEngine().addExitAgent( erDoctorAgent );
				erDoctorAgent = null;
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
						ArrayListNurseAgents.remove(i);
					}
				}
				ArrayListNurseAgents = null;
			}
			// ログ出力
			cGeneralWardLog = null;									// 一般病棟ログ出力設定

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
	 *   一般病棟の看護師エージェントを生成します。
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
			ArrayListNurseAgents = new ArrayList<ERNurseAgent>();
		}
		for( i = 0;i < iNurseAgentNum; i++ )
		{
			ArrayListNurseAgents.add( new ERNurseAgent() );
		}
	}

	/**
	 * <PRE>
	 *   一般病棟の医師エージェントを生成します。
	 *   引数は拡張性のために設定。（現状は使用していない。）
	 * </PRE>
	 * @param iDoctorAgentNum 医師エージェント数
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vCreateDoctorAgents( int iDoctorAgentNum )
	{
		erDoctorAgent = new ERDoctorAgent();
	}

	/**
	 * <PRE>
	 *    一般病棟の医師エージェントのパラメータを設定します。
	 * </PRE>
	 * @param lfYearExperience			経験年数
	 * @param lfConExperience			経験数の重み
	 * @param lfExperienceRate1			経験年数パラメータ1
	 * @param lfExperienceRate2			経験年数パラメータ2
	 * @param lfConExperienceAIS		経験年数重み（重症度）
	 * @param lfExperienceRateAIS1		経験年数パラメータその１（重症度）
	 * @param lfExperienceRateAIS2		経験年数パラメータその２（重症度）
	 * @param lfConTired1				疲労度パラメータ1
	 * @param lfConTired2				疲労度パラメータ2
	 * @param lfConTired3				疲労度パラメータ3
	 * @param lfConTired4				疲労度パラメータ4
	 * @param lfTiredRate				疲労度重み
	 * @param lfRevisedOperationRate	手術室改善度割合
	 * @param lfAssociationRate			関連性パラメータ
	 * @param lfConsultationTime		診察時間
	 * @param lfOperationTime			手術時間
	 * @param lfEmergencyTime			初療室処置時間
	 * @param iDepartment				所属部署
	 * @param iRoomNumber				所属部屋番号
	 * @author kobayashi
	 * @since 2016/01/08
	 */
	public void vSetDoctorAgentParameter( double lfYearExperience,
										  double lfConExperience,
										  double lfExperienceRate1,
										  double lfExperienceRate2,
										  double lfConExperienceAIS,
										  double lfExperienceRateAIS1,
										  double lfExperienceRateAIS2,
										  double lfConTired1,
										  double lfConTired2,
										  double lfConTired3,
										  double lfConTired4,
										  double lfTiredRate,
										  double lfRevisedOperationRate,
										  double lfAssociationRate,
										  double lfConsultationTime,
										  double lfOperationTime,
										  double lfEmergencyTime,
										  int iDepartment,
										  int iRoomNumber )
	{
		erDoctorAgent.vSetYearExperience( lfYearExperience );
		erDoctorAgent.vSetConExperience( lfConExperience );
		erDoctorAgent.vSetConTired1( lfConTired1 );
		erDoctorAgent.vSetConTired2( lfConTired2 );
		erDoctorAgent.vSetConTired3( lfConTired3 );
		erDoctorAgent.vSetConTired4( lfConTired4 );
		erDoctorAgent.vSetTiredRate( lfTiredRate );
		erDoctorAgent.vSetRevisedOperationRate( lfRevisedOperationRate );
		erDoctorAgent.vSetAssociationRate( lfAssociationRate );
		erDoctorAgent.vSetConsultationTime( lfConsultationTime );
		erDoctorAgent.vSetOperationTime( lfOperationTime );
		erDoctorAgent.vSetEmergencyTime( lfEmergencyTime );
		erDoctorAgent.vSetDoctorDepartment( iDepartment );
		erDoctorAgent.vSetExperienceRate1( lfExperienceRate1 );
		erDoctorAgent.vSetExperienceRate2( lfExperienceRate2 );
		erDoctorAgent.vSetConExperienceAIS( lfConExperienceAIS );
		erDoctorAgent.vSetExperienceRateAIS1( lfExperienceRateAIS1 );
		erDoctorAgent.vSetExperienceRateAIS2( lfExperienceRateAIS2 );
		erDoctorAgent.vSetRoomNumber( iRoomNumber );
	}


	/**
	 * <PRE>
	 *    一般病棟の看護師エージェントのパラメータを設定します。
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
	 * @author kobayashi
	 * @since 2016/01/12
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
			int[] aiDepartment )
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
		}
	}

	/**
	 * <PRE>
	 *    FUSEエンジンにエージェントを登録します。
	 * </PRE>
	 * @param engine	FUSEのシミュレーションエンジン
	 */
	public void vSetSimulationEngine( SimulationEngine engine )
	{
		engine.addAgent(this);
	}

	/**
	 * <PRE>
	 *    一般病棟のプロセスを実行します。
	 * </PRE>
	 * @param ArrayListOperationRooms		全手術室
	 * @param ArrayListHighCareUnitRooms	全高度治療室
	 * @throws ERDoctorAgentException		医師エージェントクラスの例外
	 */
	public void vImplementGeneralWardRoom( ArrayList<EROperationRoom> ArrayListOperationRooms, ArrayList<ERHighCareUnitRoom> ArrayListHighCareUnitRooms ) throws ERDoctorAgentException
	{
		int i,j;

		synchronized( csGeneralWardCriticalSection )
		{
			for( i = 0;i < ArrayListPatientAgents.size(); i++ )
			{
				cGeneralWardLog.info(ArrayListPatientAgents.get(i).getId() + "," + "一般病棟処理開始" );
				if( ArrayListPatientAgents.get(i).isMoveWaitingTime() == false )
				{
	 				erDoctorAgent.vSetPatientMoveWaitFlag( 1 );
	 				for( j = 0;j < ArrayListNurseAgents.size(); j++ )
	 				{
	 					ArrayListNurseAgents.get(j).vSetPatientMoveWaitFlag( 1 );
	 				}
	 				// 移動時間がまだ終了していないので、移動を実施しません。
	 				cGeneralWardLog.info(ArrayListPatientAgents.get(i).getId() + "," + "一般病棟移動時間：" + ArrayListPatientAgents.get(i).lfGetMoveWaitingTime() );
					continue;
				}
				// 部屋移動が終了したのでフラグOFFに処置中とします。
				ArrayListPatientAgents.get(i).vSetMoveRoomFlag( 0 );
				erDoctorAgent.vSetPatientMoveWaitFlag( 0 );
				for( j = 0;j < ArrayListNurseAgents.size(); j++ )
				{
					ArrayListNurseAgents.get(j).vSetPatientMoveWaitFlag( 0 );
				}

				// その患者を対応している医師、看護師エージェントのIDを設定します。
				ArrayListPatientAgents.get(i).vSetDoctorAgent(erDoctorAgent.getId());

				cGeneralWardLog.info(ArrayListPatientAgents.get(i).getId() + "," + "一般病棟入院中" );
				// その患者を対応している医師、看護師エージェントのIDを設定します。
	//			erPAgent.vSetNurseAgent(erNurseAgent.getId());
	//			erPAgent.vSetDoctorAgent(erDoctorAgent.getId());

				double lfAISRevisedSeries = 0.0;
				double lfHospitalStayDay = 0.0;
				double lfInitAIS = 0.0;
//				if( Math.abs(ArrayListPatientAgents.get(i).lfGetHospitalStayTime()-ArrayListPatientAgents.get(i).lfGetMoveWaitingTime()) <= 0.0 )
				if( ArrayListPatientAgents.get(i).lfGetHospitalStayTime()-ArrayListPatientAgents.get(i).lfGetMoveWaitingTime() <= 0.0 && ArrayListPatientAgents.get(i).lfGetRevisedSeries() == 0.0 )
				{
					cGeneralWardLog.info(ArrayListPatientAgents.get(i).getId() + ","  + "傷病状態改善度合いの算出。" + "," + "一般病棟" );
					// 入院時のAIS値を設定します。
					lfInitAIS = ArrayListPatientAgents.get(i).lfSetHospitalStayInitAIS();

					// 入院日数を算出します。
					lfHospitalStayDay = lfCalcHospitalStay( ArrayListPatientAgents.get(i) );

					// 患者の入院日数を設定します。
					ArrayListPatientAgents.get(i).vSetHospitalStayDay( lfHospitalStayDay );

					double lfStepTime = this.getEngine().getLatestTimeStep()/1000.0;
					double lfStayTime = lfStepTime / (24.0*3600.0);
					// AIS値改善割合を算出します。
					lfAISRevisedSeries = lfCurePatientAISSeries( lfStayTime, lfHospitalStayDay, 0.5, lfInitAIS );

					// AIS値改善割合を設定します。
					ArrayListPatientAgents.get(i).vSetRevisedSeries( lfAISRevisedSeries );

				}
				// 患者のAIS値を改善させます。（時間経過とともに）
				cGeneralWardLog.info(ArrayListPatientAgents.get(i).getId() + ","  + "改善処置実行。" + "," + "一般病棟" );
				vCurePatientAIS( ArrayListPatientAgents.get(i) );

				// 医師の判定を更新します。
				erDoctorAgent.iJudgeAIS( ArrayListPatientAgents.get(i) );

				// 他室へ移動するか否かを判定するのに１日程度時間を置きます。
				if( ArrayListPatientAgents.get(i).lfGetGeneralWardStayTime() < 86400 )
				{
	 				// 移動時間がまだ終了していないので、移動を実施しません。
	 				cGeneralWardLog.info(ArrayListPatientAgents.get(i).getId() + "," + "一般病棟入院時間：" + ArrayListPatientAgents.get(i).lfGetGeneralWardStayTime() );
					continue;
				}

				// 手術が必要かどうかの判定を実施します。
				if( erDoctorAgent.isJudgeOperation( ArrayListPatientAgents.get(i) ) == true )
				{
					cGeneralWardLog.info(ArrayListPatientAgents.get(i).getId() + ","  + "医師の手術実行判定。" + "," + "一般病棟" );
					// 手術室移動待機判定フラグをONにします。
					ArrayListPatientAgents.get(i).vSetOperationRoomWaitFlag( 1 );
					// ここで集中治療室及び高度治療室のフラグがONの場合はこれをOFFにして、ここで治療するようにする。
					// 一般病棟、手術室ループ現象をなくすため。
					ArrayListPatientAgents.get(i).vSetIntensiveCareUnitRoomWaitFlag( 0 );
					ArrayListPatientAgents.get(i).vSetHighCareUnitRoomWaitFlag( 0 );

					// 手術室移動判定を実施します。
					vJudgeMoveOperationRoom( ArrayListOperationRooms, erDoctorAgent, ArrayListNurseAgents, ArrayListPatientAgents.get(i) );

				}
				else
				{
					// 医師エージェントが、患者エージェントが高度治療室へ移動可能かどうかを判定します。
					if( erDoctorAgent.isJudgeHighCareUnit( ArrayListPatientAgents.get(i) ) == true )
					{
						cGeneralWardLog.info(ArrayListPatientAgents.get(i).getId() + ","  + "医師の高度治療室移動判定。" + "," + "一般病棟" );
						// 高度治療室への移動判定を実行します。
						vJudgeMoveHighCareUnitRoom( ArrayListHighCareUnitRooms, erDoctorAgent, ArrayListNurseAgents, ArrayListPatientAgents.get(i) );

						// 高度治療室移動待機判定フラグをONにします。
						ArrayListPatientAgents.get(i).vSetHighCareUnitRoomWaitFlag( 1 );
					}
					else
					{
						// 医師エージェントが患者エージェントが退院可能かどうかを判定します。
						if( erDoctorAgent.isJudgeDischarge( ArrayListPatientAgents.get(i) ) == true )
						{
							cGeneralWardLog.info(ArrayListPatientAgents.get(i).getId() + ","  + "一般病棟：退院しました！。");
							// 退院を実施します。（エージェントを消滅させます。）
							ArrayListPatientAgents.get(i).getEngine().addExitAgent(ArrayListPatientAgents.get(i));
							ArrayListPatientAgents.remove(i);

							// 退院数をカウントします。
							iDisChargeNum++;
						}
					}
				}
			}
		}
	}

	/**
	 * <PRE>
	 *    AIS値の改善を行います。
	 *    簡易的にここでは、指数関数を掛け合わせることで、AIS値を時間とともに低下させます。
	 * </PRE>
	 * @param erPatientAgent	対象となる患者エージェント
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vCurePatientAIS( ERPatientAgent erPatientAgent )
	{
		double lfStepTime = 0.0;
		double lfStayTime = 0.0;
		double lfCurrentAISHead = erPatientAgent.lfGetInternalAISHead();
		double lfCurrentAISFace = erPatientAgent.lfGetInternalAISFace();
		double lfCurrentAISNeck = erPatientAgent.lfGetInternalAISNeck();
		double lfCurrentAISThorax = erPatientAgent.lfGetInternalAISThorax();
		double lfCurrentAISAbdomen = erPatientAgent.lfGetInternalAISAbdomen();
		double lfCurrentAISSpine = erPatientAgent.lfGetInternalAISSpine();
		double lfCurrentAISUpperExtremity = erPatientAgent.lfGetInternalAISUpperExtremity();
		double lfCurrentAISLowerExtremity = erPatientAgent.lfGetInternalAISLowerExtremity();
		double lfCurrentAISUnspecified = erPatientAgent.lfGetInternalAISUnspecified();

		double lfHospitalStayDay = 0.0;
		double lfRevisedSeries = 0.0;

		lfHospitalStayDay = erPatientAgent.lfGetHospitalStayDay();
		lfRevisedSeries = erPatientAgent.lfGetRevisedSeries();

		// AIS値を改善させます。
		lfStepTime = this.getEngine().getLatestTimeStep()/1000.0;
		lfStayTime = lfStepTime / (24.0*3600.0);
		lfCurrentAISHead = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISHead );
		lfCurrentAISFace = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISFace );
		lfCurrentAISNeck = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISNeck );
		lfCurrentAISThorax = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISThorax );
		lfCurrentAISAbdomen = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISAbdomen );
		lfCurrentAISSpine = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISSpine );
		lfCurrentAISUpperExtremity = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISUpperExtremity );
		lfCurrentAISLowerExtremity = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISLowerExtremity );
		lfCurrentAISUnspecified = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISUnspecified );

		// 改善した内容を代入します。
		erPatientAgent.vSetInternalAISHead( lfCurrentAISHead );
		erPatientAgent.vSetInternalAISFace( lfCurrentAISFace );
		erPatientAgent.vSetInternalAISNeck( lfCurrentAISNeck );
		erPatientAgent.vSetInternalAISThorax( lfCurrentAISThorax );
		erPatientAgent.vSetInternalAISAbdomen( lfCurrentAISAbdomen );
		erPatientAgent.vSetInternalAISSpine( lfCurrentAISSpine );
		erPatientAgent.vSetInternalAISUpperExtremity( lfCurrentAISUpperExtremity );
		erPatientAgent.vSetInternalAISLowerExtremity( lfCurrentAISLowerExtremity );
		erPatientAgent.vSetInternalAISUnspecified( lfCurrentAISUnspecified );

		cGeneralWardLog.info(erPatientAgent.getId() + "," + erPatientAgent.lfGetHospitalStayTime() + "," + erPatientAgent.lfGetInternalAISHead() + "," + erPatientAgent.lfGetInternalAISFace() + "," + erPatientAgent.lfGetInternalAISNeck() + "," +
		erPatientAgent.lfGetInternalAISThorax() + "," + erPatientAgent.lfGetInternalAISAbdomen() + "," + erPatientAgent.lfGetInternalAISSpine() + "," +
		erPatientAgent.lfGetInternalAISUpperExtremity() + "," + erPatientAgent.lfGetInternalAISLowerExtremity() + "," + erPatientAgent.lfGetInternalAISUnspecified());

		erPatientAgent.vStrSetInjuryStatus();
	}

	/**
	 * <PRE>
	 *    患者の改善をします。
	 *    厚生労働省の統計データから退院日数を基に等比級数の級数を設定するようにします。
	 *    AIS値は0.5以下になった場合を退院とするので、そこまで値が低下することを前提とします。
	 * </PRE>
	 * @param lfTime				現在の経過時間[秒]を設定します。
	 * @param lfHospitalStayTime	病院に入院する日数を設定します。[日]
	 * @param lfFinishAIS			最終的なAIS値を設定します。
	 * @param lfInitAIS				初期のAIS値を設定します。
	 * @return						改善後のAIS値
	 */
	private double lfCurePatientAISSeries( double lfTime, double lfHospitalStayTime, double lfFinishAIS, double lfInitAIS )
	{
		double lfSeries = 0.0;
		double lfX = 0.0;
		double lfN = 0.0;
		double lfDeltaTime = 0.0;

		double lfStepTime = this.getEngine().getLatestTimeStep()/1000.0;

		// シミュレーション時間間隔を考慮して級数を算出します。
		lfDeltaTime = lfStepTime/86400.0;
		lfN = lfHospitalStayTime*86400;
		lfSeries = Math.pow( lfFinishAIS/lfInitAIS, 1.0/((lfN-1)*lfDeltaTime) );
		return lfSeries;
	}

	/**
	 * <PRE>
	 *    患者の改善をします。
	 *    厚生労働省の統計データから退院日数を基に等比級数の級数を設定するようにします。
	 *    AIS値は0.5以下になった場合を退院とするので、そこまで値が低下することを前提とします。
	 * </PRE>
	 * @param lfTime				現在の経過時間[秒]を設定します。
	 * @param lfHospitalStayTime	病院に入院する日数を設定します。[日]
	 * @param lfRevisedSeriesAIS	改善するためのベースとなるAIS値を設定します。
	 * @param lfCurrentAIS			現在のAIS値を設定します。
	 * @return	改善後のAIS値
	 */
	private double lfCurePatientAISFunction( double lfTime, double lfHospitalStayTime, double lfRevisedSeriesAIS, double lfCurrentAIS )
	{
		double lfRes = 0.0;

		lfRes = lfCurrentAIS*Math.pow( lfRevisedSeriesAIS, lfTime );
		return lfRes;
	}

	/**
	 * <PRE>
	 *    入院日数を算出します。単位[日]
	 *    厚生労働省の統計データより
	 * </PRE>
	 * @param erPatientAgent	対象とする患者エージェントのインスタンス
	 * @return	入院日数
	 */
	private double lfCalcHospitalStay( ERPatientAgent erPatientAgent )
	{
		double lfHospitalStayTime = 0.0;
		double lfMaxHospitalStayTime = -Double.MAX_VALUE;

		if( 0 < erPatientAgent.lfGetInternalAISHead() && erPatientAgent.lfGetInternalAISHead() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 16.64048 + 2*(rnd.NextUnif()-1)*11.08299;
			else								lfHospitalStayTime = 17.40952 + 2*(rnd.NextUnif()-1)*15.05288;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 16.64048 + 2*(rnd.NextUnif()-1)*11.08299;
			else								lfHospitalStayTime = 17.40952 + 2*(rnd.NextUnif()-1)*15.05288;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISFace() && erPatientAgent.lfGetInternalAISFace() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 9.695238 + 2*(rnd.NextUnif()-1)*6.306905;
			else								lfHospitalStayTime = 8.173684 + 2*(rnd.NextUnif()-1)*4.925591;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 9.695238 + 2*(rnd.NextUnif()-1)*6.306905;
			else								lfHospitalStayTime = 8.173684 + 2*(rnd.NextUnif()-1)*4.925591;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISNeck() && erPatientAgent.lfGetInternalAISNeck() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 29.41905 + 2*(rnd.NextUnif()-1)*16.41072;
			else								lfHospitalStayTime = 39.86000 + 2*(rnd.NextUnif()-1)*33.62594;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 29.41905 + 2*(rnd.NextUnif()-1)*16.41072;
			else								lfHospitalStayTime = 39.86000 + 2*(rnd.NextUnif()-1)*33.62594;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISThorax() && erPatientAgent.lfGetInternalAISThorax() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 22.65714 + 2*(rnd.NextUnif()-1)*14.36938;
			else								lfHospitalStayTime = 26.66410 + 2*(rnd.NextUnif()-1)*28.15095;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 22.65714 + 2*(rnd.NextUnif()-1)*14.36938;
			else								lfHospitalStayTime = 26.66410 + 2*(rnd.NextUnif()-1)*28.15095;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISAbdomen() && erPatientAgent.lfGetInternalAISAbdomen() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 16.45122 + 2*(rnd.NextUnif()-1)*13.50604;
			else								lfHospitalStayTime = 17.89524 + 2*(rnd.NextUnif()-1)*20.43413;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 16.45122 + 2*(rnd.NextUnif()-1)*13.50604;
			else								lfHospitalStayTime = 17.89524 + 2*(rnd.NextUnif()-1)*20.43413;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISSpine() && erPatientAgent.lfGetInternalAISSpine() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 29.41905 + 2*(rnd.NextUnif()-1)*16.41072;
			else								lfHospitalStayTime = 39.86000 + 2*(rnd.NextUnif()-1)*33.62594;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 29.41905 + 2*(rnd.NextUnif()-1)*16.41072;
			else								lfHospitalStayTime = 39.86000 + 2*(rnd.NextUnif()-1)*33.62594;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISLowerExtremity() && erPatientAgent.lfGetInternalAISLowerExtremity() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 18.26308 + 2*(rnd.NextUnif()-1)*10.95915;
			else								lfHospitalStayTime = 17.73750 + 2*(rnd.NextUnif()-1)*12.94469;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 18.26308 + 2*(rnd.NextUnif()-1)*10.95915;
			else								lfHospitalStayTime = 17.73750 + 2*(rnd.NextUnif()-1)*12.94469;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISUpperExtremity() && erPatientAgent.lfGetInternalAISUpperExtremity() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 27.73182 + 2*(rnd.NextUnif()-1)*17.39286;
			else								lfHospitalStayTime = 17.73750 + 2*(rnd.NextUnif()-1)*12.94469;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 27.73182 + 2*(rnd.NextUnif()-1)*17.39286;
			else								lfHospitalStayTime = 17.73750 + 2*(rnd.NextUnif()-1)*12.94469;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISUnspecified() && erPatientAgent.lfGetInternalAISUnspecified() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 32.96429 + 2*(rnd.NextUnif()-1)*30.50811;
			else								lfHospitalStayTime = 31.02381 + 2*(rnd.NextUnif()-1)*20.56167;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 32.96429 + 2*(rnd.NextUnif()-1)*30.50811;
			else								lfHospitalStayTime = 31.02381 + 2*(rnd.NextUnif()-1)*20.56167;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		return lfMaxHospitalStayTime;
	}

	/**
	 * <PRE>
	 *    手術室へ移動可能かどうかを判定し、移動できる場合は待合室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListOperationRooms	全手術室
	 * @param erDoctorAgent				担当医師エージェント
	 * @param ArrayListNurseAgents		担当する全看護師エージェント
	 * @param erPAgent					移動する患者
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveOperationRoom( ArrayList<EROperationRoom> ArrayListOperationRooms, ERDoctorAgent erDoctorAgent, ArrayList<ERNurseAgent> ArrayListNurseAgents, ERPatientAgent erPAgent )
	{
		int i,j;
		int iJudgeCount = 0;
		ERDoctorAgent erOperationDoctorAgent;
		ERNurseAgent erOperationNurseAgent;
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			// 手術室に空きがある場合
			if( ArrayListOperationRooms.get(i).isVacant() == true )
			{
				cGeneralWardLog.info(erPAgent.getId() + ","  + "一般病棟：手術室へ移動を開始しました。");
				// 手術室待ちフラグをOFFにします。
				erPAgent.vSetOperationRoomWaitFlag( 0 );

				// 患者のいる位置を手術室に変更します。
				erPAgent.vSetLocation( 2 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// 診察室へ患者エージェントを移動します。
				ArrayListOperationRooms.get(i).vSetPatientAgent( erPAgent );

			// 医師、看護師エージェントへメッセージを送信します。
				// 手術室の医師エージェントへメッセージを送信します。
				erOperationDoctorAgent = ArrayListOperationRooms.get(i).cGetSurgeonDoctorAgent();
				erDoctorAgent.vSendToDoctorAgentMessage( erPAgent, (int)erDoctorAgent.getId(), (int)erOperationDoctorAgent.getId() );

				// 手術室の看護師エージェントへメッセージを送信します。
				for( j = 0;j < ArrayListOperationRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erOperationNurseAgent = ArrayListOperationRooms.get(i).cGetNurseAgent(j);
					erDoctorAgent.vSendToNurseAgentMessage( erPAgent, erOperationNurseAgent, (int)erDoctorAgent.getId(), (int)erOperationNurseAgent.getId() );
				}

				// 医師エージェントの対応を終了します。
				erDoctorAgent.vSetAttending( 0 );

				// 対応を受けた患者エージェントを削除します。
				vRemovePatientAgent( erPAgent );

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListOperationRooms.get(i).erGetTriageNode() ) );
				}
				// 手術室で担当する医師エージェントを設定します。
				ArrayListOperationRooms.get(i).cGetSurgeonDoctorAgent().vSetSurgeon(1);
				ArrayListOperationRooms.get(i).cGetSurgeonDoctorAgent().vSetAttending(1);

				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListOperationRooms.size() )
		{
			cGeneralWardLog.info(erPAgent.getId() + ","  + "一般病棟：手術室満室。");
			// 空きがない場合は患者の手術室待ちフラグをONにします。
			erPAgent.vSetOperationRoomWaitFlag( 1 );

			// そのまま待機します。
		}
	}

	/**
	 * <PRE>
	 *   高度治療室への移動判定を行います。
	 * </PRE>
	 * @param ArrayListHighCareUnitRooms		全高度治療室
	 * @param erDoctorAgent						担当した医師エージェント
	 * @param ArrayListERNurseAgents			担当した全看護師エージェント
	 * @param erPAgent							移動する患者エージェント
	 * @author kobayashi
	 * @since 2015/08/17
	 */
	private void vJudgeMoveHighCareUnitRoom( ArrayList<ERHighCareUnitRoom> ArrayListHighCareUnitRooms, ERDoctorAgent erDoctorAgent, ArrayList<ERNurseAgent> ArrayListERNurseAgents, ERPatientAgent erPAgent )
	{
		int i,j;
		int iJudgeCount = 0;
		ERDoctorAgent erHighCareUnitDoctorAgent;
		ERNurseAgent erHighCareUnitNurseAgent;
//		ERClinicalEngineerAgent erEmergencyclinicalEngineerAgent;
		for( i = 0;i < ArrayListHighCareUnitRooms.size(); i++ )
		{
			// 初療室に空きがある場合
			if( ArrayListHighCareUnitRooms.get(i).isVacant() == true )
			{
				cGeneralWardLog.info(erPAgent.getId() + ","  + "高度治療室へ移動を開始しました。" + "," + "一般病棟" );
				// 高度治療室待機フラグをOFFにします。
				erPAgent.vSetHighCareUnitRoomWaitFlag( 0 );

				// 患者のいる位置を高度治療室に変更します。
				erPAgent.vSetLocation( 7 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// 高度治療室へ患者エージェントを移動します。
				ArrayListHighCareUnitRooms.get(i).vSetPatientAgent( erPAgent );

			// 看護師、医師エージェントへメッセージを送信します。
				// 高度治療室の看護師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListHighCareUnitRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erHighCareUnitNurseAgent = ArrayListHighCareUnitRooms.get(i).cGetNurseAgent(j);
					erDoctorAgent.vSendToNurseAgentMessage( erPAgent, erHighCareUnitNurseAgent, (int)erDoctorAgent.getId(), (int)erHighCareUnitNurseAgent.getId() );
				}
				// 高度治療室の医師エージェントに患者情報を送信します。
				for( j = 0;j < ArrayListHighCareUnitRooms.get(i).iGetDoctorAgentsNum(); j++ )
				{
					erHighCareUnitDoctorAgent = ArrayListHighCareUnitRooms.get(i).cGetDoctorAgent( j );
					erDoctorAgent.vSendToDoctorAgentMessage( erPAgent, (int)erDoctorAgent.getId(), (int)erHighCareUnitDoctorAgent.getId() );
				}
//				for( j = 0;j < ArrayListEmergencyRooms.get(i).iGetClinicalEngineerAgentsNum(); j++ )
//				{
//					erEmergencyCinicalEngineerAgent = ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgents(i);
//					erNurseAgent.vSendToEngineerAgentMessage( erPAgent, (int)erNurseAgent.getId(), (int)erEmergencyClinicalEngineerAgent.getId() );
//				}

				// 医師エージェントの対応を終了します。
				erDoctorAgent.vSetAttending( 0 );

				// 看護師エージェントの対応も終了します。
				for( j= 0;j < ArrayListERNurseAgents.size(); j++ )
				{
					ArrayListERNurseAgents.get(j).vSetAttending(0);
				}

				// 対応を受けた患者エージェントを削除します。
				vRemovePatientAgent( erPAgent );

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListHighCareUnitRooms.get(i).erGetTriageNode() ) );
				}
				erPAgent = null;

				// 空いている看護師に割り当てます。
				ArrayListHighCareUnitRooms.get(i).bAssignVacantNurse();

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListHighCareUnitRooms.size() )
		{
			// そのまま待機します。
			cGeneralWardLog.info(erPAgent.getId() + ","  + "高度治療室満室。" + "," + "一般病棟" );
		}
	}

	/**
	 * <PRE>
	 *    患者を一般病棟へ登録します。
	 * </PRE>
	 * @param erPAgent 患者エージェント
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetPatientAgent( ERPatientAgent erPAgent )
	{
		if( erPAgent.iGetStayHospitalFlag() == 0 )
		{
			// 患者が入院するフラグを有効にします。
			erPAgent.vSetStayHospitalFlag( 1 );
		}
		if( erPAgent.iGetStayHospitalTimeFlag() == 0 )
		{
			// 患者の入院経過時間の加算を開始します。
			erPAgent.vSetStayHospitalTimeFlag( 1 );
		}
		// 一般病棟フラグをONにし、他の入院病棟のフラグをOFFにします。
		erPAgent.vSetStayGeneralWardFlag( 1 );
		erPAgent.vSetStayHighCareUnitFlag( 0 );
		erPAgent.vSetStayIntensiveCareUnitFlag( 0 );

		// 一般病棟に患者を登録します。
		ArrayListPatientAgents.add( erPAgent );
	}

	/**
	 * <PRE>
	 *    対応が終了した患者を削除します。
	 * </PRE>
	 * @param erPatientAgent 患者エージェント
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vRemovePatientAgent( ERPatientAgent erPatientAgent )
	{
		ArrayListPatientAgents.remove( erPatientAgent );
	}

	/**
	 * <PRE>
	 *   一般病棟の医師エージェントを取得します。
	 * </PRE>
	 * @param i 所属している医師の番号
	 * @return	担当する医師エージェントのインスタンス
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public ERDoctorAgent cGetDoctorAgent( int i )
	{
		return erDoctorAgent;
	}

	/**
	 * <PRE>
	 *   一般病棟の医師エージェントの数を取得します。(現状は常に1を返します。)
	 * </PRE>
	 * @return	所属する医師エージェントの人数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public int iGetDoctorAgentsNum()
	{
		return 1;
	}

	/**
	 * <PRE>
	 *   初療室の看護師エージェントを取得します。
	 * </PRE>
	 * @param i 所属している看護師の番号
	 * @return	該当する看護師エージェントのインスタンス
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public ERNurseAgent cGetNurseAgent( int i )
	{
		return ArrayListNurseAgents.get(i);
	}

	/**
	 * <PRE>
	 *   初療室の看護師エージェントの数を取得します。
	 * </PRE>
	 * @return	所属する看護師エージェントの人数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public int iGetNurseAgentsNum()
	{
		return ArrayListNurseAgents.size();
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
			if( ArrayListNurseAgents.get(i).iGetAttending() == 1 )
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

	@Override
	public void action(long timeStep)
	{
		int i;
		double lfSecond = 0.0;
		// TODO 自動生成されたメソッド・スタブ
		lfSecond = timeStep / 1000.0;
		lfTotalTime += lfSecond;
		// 死亡患者がいる場合は削除をする。
		synchronized( csGeneralWardCriticalSection )
		{
			if( ArrayListPatientAgents != null )
			{
				for( i = ArrayListPatientAgents.size()-1; i >= 0 ; i-- )
				{
					if( ArrayListPatientAgents.get(i) != null )
					{
						if( ArrayListPatientAgents.get(i).iGetSurvivalFlag() == 0 || ArrayListPatientAgents.get(i).iGetDisChargeFlag() == 1 )
						{
							ArrayListPatientAgents.set(i, null);
							ArrayListPatientAgents.remove(i);
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * <PRE>
	 *    一般病棟のログ設定をします。
	 * </PRE>
	 * @param log	ロガークラスインスタンス
	 */
	public void vSetLog(Logger log)
	{
		// TODO 自動生成されたメソッド・スタブ
		cGeneralWardLog = log;
	}

	/**
	 * <PRE>
	 *    一般病棟のX座標を取得します。
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
	 *    一般病棟のY座標を取得します。
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
	 *    一般病棟の横幅を取得します。
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
	 *    一般病棟の縦幅を取得します。
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
	 *    一般病棟の階数を取得します。
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
	 *    一般病棟のX座標を格納します。
	 * </PRE>
	 * @param iData	X座標
	 */
	public void vSetX( int iData )
	{
		iDrawX = iData;
	}

	/**
	 * <PRE>
	 *    一般病棟のY座標を格納します。
	 * </PRE>
	 * @param iData	Y座標
	 */
	public void vSetY( int iData )
	{
		iDrawY = iData;
	}

	/**
	 * <PRE>
	 *    一般病棟のZ座標を格納します。
	 * </PRE>
	 * @param iData	Z座標
	 */
	public void vSetZ( int iData )
	{
		iDrawZ = iData;
	}

	/**
	 * <PRE>
	 *   一般病棟の横幅を格納します。
	 * </PRE>
	 * @param iData	横幅
	 */
	public void vSetWidth( int iData )
	{
		iDrawWidth = iData;
	}

	/**
	 * <PRE>
	 *    一般病棟の縦幅を格納します。
	 * </PRE>
	 * @param iData	縦幅
	 */
	public void vSetHeight( int iData )
	{
		iDrawHeight = iData;
	}

	/**
	 * <PRE>
	 *    一般病棟の階数を格納します。
	 * </PRE>
	 * @param iData	階数
	 */
	public void vSetF( int iData )
	{
		iDrawF = iData;
	}

	/**
	 * <PRE>
	 *   一般病棟に所属しているエージェントの座標を設定します。
	 * </PRE>
	 */
	public void vSetAffiliationAgentPosition()
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;

		double lfX = 0.0;
		double lfY = 0.0;
		double lfZ = 0.0;

		// 医師エージェントの位置を設定します。
		lfX = this.getPosition().getX()+3*rnd.NextUnif();
		lfY = this.getPosition().getY()+3*rnd.NextUnif();
		lfZ = this.getPosition().getZ()+3*rnd.NextUnif();
		erDoctorAgent.setPosition( lfX, lfY, lfZ );

		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			// 看護師エージェントの位置を設定します。
			lfX = this.getPosition().getX()+40*(2*rnd.NextUnif()-1);
			lfY = this.getPosition().getY()+40*(2*rnd.NextUnif()-1);
			lfZ = this.getPosition().getZ()+40*(2*rnd.NextUnif()-1);
			ArrayListNurseAgents.get(i).setPosition( lfX, lfY, lfZ );
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
	 *    現在選択されている一般病棟のノードを取得します。
	 * </PRE>
	 * @return	選択中の一般病棟のノード
	 */
	public ERTriageNode erGetTriageNode()
	{
		return erTriageNode;
	}

	/**
	 * <PRE>
	 *   一般病棟のノードを設定します。
	 * </PRE>
	 * @param erNode	設定するノードインスタンス(一般病棟)
	 */
	public void vSetTriageNode( ERTriageNode erNode )
	{
		erTriageNode = erNode;
	}

	/**
	 * <PRE>
	 *   逆シミュレーションモードを設定します。
	 * </PRE>
	 * @param iMode 0 通常シミュレーションモード
	 * 				1 GUIモード
	 * 				2 逆シミュレーションモード
	 */
	public void vSetInverseSimMode( int iMode )
	{
		int i;
		iInverseSimFlag = iMode;

		erDoctorAgent.vSetInverseSimMode( iMode );

		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetInverseSimMode( iMode );
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
	 *    現時点で患者がいるかどうかを取得します。
	 * </PRE>
	 * @return	現在一般病棟に在院している患者数
	 */
	public synchronized int iGetPatientInARoom()
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;
		int iCount = 0;

		try
		{
			synchronized( csGeneralWardCriticalSection )
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
		return iCount;
	}

	/**
	 * <PRE>
	 *    一般病棟で退院した人数を取得します。
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
		csGeneralWardCriticalSection = cs;
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
	 *   (室所属する医師エージェント)
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vSetDoctorsRandom()
	{
		erDoctorAgent.vSetRandom( rnd );
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
	public int iGetTriageCategoryPatientNum( int iCategory )
	{
		int i;
		int iCategoryPatientNum = 0;

		synchronized( csGeneralWardCriticalSection )
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
	 *   初療室の患者エージェントの数を取得します。
	 *   nullでなければ1人いるので1と返却します。
	 * </PRE>
	 * @return	現在所属している患者エージェントの人数
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
	 *    現在、待合室に最も長く病院にいる患者の在院時間を取得します。
	 * </PRE>
	 * @return		最も長く病院に在院する患者の在院時間
	 */
	public double lfGetLongestStayPatient()
	{
		int i;
		double lfLongestStayTime = -Double.MAX_VALUE;
		synchronized( csGeneralWardCriticalSection )
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
	public void vLastBedTime()
	{
		int i;
		double lfLongestTime = -Double.MAX_VALUE;
		double lfLastTime = -Double.MAX_VALUE;
		synchronized( csGeneralWardCriticalSection )
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
