package triage.room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import triage.agent.ERClinicalEngineerAgent;
import triage.agent.ERDoctorAgent;
import triage.agent.ERDoctorAgentException;
import triage.agent.ERNurseAgent;
import triage.agent.ERPatientAgent;
import utility.node.ERTriageNode;
import utility.node.ERTriageNodeManager;
import utility.sfmt.Rand;

public class EREmergencyRoom extends Agent
{
	private static final long serialVersionUID = -7465112602538861582L;

	ERPatientAgent erCurrentPatientAgent;								// 現在対応している患者
	ERDoctorAgent erSurgeonDoctorAgent;									// 執刀医エージェント
	ArrayList<ERDoctorAgent> ArrayListDoctorAgents;						// 初療室で見ている医師エージェント
	ArrayList<ERNurseAgent> ArrayListNurseAgents;						// 初療室で見ている看護師エージェント
	ArrayList<ERClinicalEngineerAgent> ArrayListClinicalEngineerAgents;	// 初療室で見ている医療技士エージェント
	int iAttachedDoctorNum;												// 初療室に所属する医師エージェント数
	int iAttachedNurseNum;												// 初療室に所属する看護師エージェント数
	int iAttachedClinicalEngineerNum;									// 初療室に所属する医療技師エージェント数
	boolean bUnderOperationFlag;										// 初療室対応中か否かを表すフラグ

	double lfTimeCourse;

	private Logger cEmergencyRoomLog;									// 初療室ログ出力設定

	Rand rnd;														// 乱数クラス

	private int iDisChargeNum;

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

	private Object csEmergencyRoomCriticalSection;						// クリティカルセクション用

	private double lfLastBedTime;
	private double lfLongestTotalTime;

	/**
	 * <PRE>
	 *    初療室のコンストラクタ
	 * </PRE>
	 */
	public EREmergencyRoom()
	{
		vInitialize();
	}

	/**
	 * <PRE>
	 *    初療室のコンストラクタ
	 * </PRE>
	 * @param iAttachedDoctorNumData			所属医師数
	 * @param iAttachedNurseNumData				所属看護師数
	 * @param iAttachedClinicalEngineerNumData	所属医療技師数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public EREmergencyRoom(int iAttachedDoctorNumData, int iAttachedNurseNumData, int iAttachedClinicalEngineerNumData )
	{
		vInitialize( iAttachedDoctorNumData, iAttachedNurseNumData, iAttachedClinicalEngineerNumData );
	}

	/**
	 * <PRE>
	 *    初療室の初期設定を実行します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vInitialize()
	{
		ArrayListDoctorAgents			= new ArrayList<ERDoctorAgent>();
		ArrayListNurseAgents			= new ArrayList<ERNurseAgent>();
		ArrayListClinicalEngineerAgents = new ArrayList<ERClinicalEngineerAgent>();
		erCurrentPatientAgent			= null;										// 現在対応している患者
		erSurgeonDoctorAgent			= null;										// 執刀医エージェント
		iAttachedDoctorNum				= 0;										// 初療室に所属する医師エージェント数
		iAttachedNurseNum				= 0;										// 初療室に所属する看護師エージェント数
		iAttachedClinicalEngineerNum	= 0;										// 初療室に所属する医療技師エージェント数

		lfTimeCourse					= 0;
		iDisChargeNum 					= 0;

//		long seed;
//		rnd = null;
//		seed = System.currentTimeMillis();
//		rnd = new Sfmt( (int)seed );
	}

	/**
	 * <PRE>
	 *    初療室の初期設定を実行します。
	 * </PRE>
	 * @param iAttachedDoctorNumData			所属医師数
	 * @param iAttachedNurseNumData				所属看護師数
	 * @param iAttachedClinicalEngineerNumData	所属医療技師数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vInitialize( int iAttachedDoctorNumData, int iAttachedNurseNumData, int iAttachedClinicalEngineerNumData )
	{
		int i;
		iAttachedDoctorNum = iAttachedDoctorNumData;
		iAttachedNurseNum = iAttachedNurseNumData;
		iAttachedClinicalEngineerNum = iAttachedClinicalEngineerNumData;

		ArrayListDoctorAgents			= new ArrayList<ERDoctorAgent>();
		ArrayListNurseAgents			= new ArrayList<ERNurseAgent>();
		ArrayListClinicalEngineerAgents = new ArrayList<ERClinicalEngineerAgent>();
		for( i = 0;i < iAttachedDoctorNum; i++ )
		{
			ArrayListDoctorAgents.add( new ERDoctorAgent() );
		}
		for( i = 0;i < iAttachedNurseNum; i++ )
		{
			ArrayListNurseAgents.add( new ERNurseAgent() );
		}
		for( i = 0;i < iAttachedClinicalEngineerNum; i++ )
		{
			ArrayListClinicalEngineerAgents.add( new ERClinicalEngineerAgent() );
		}
	}

	/**
	 * <PRE>
	 *    ファイルの読み込みを実行します。
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

		for( i = 0;i < ArrayListDoctorAgents.size(); i++ )
		{
			ArrayListDoctorAgents.get(i).vSetReadWriteFile( iFileWriteMode );
		}
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetReadWriteFile( iFileWriteMode );
		}
		for( i = 0;i < ArrayListClinicalEngineerAgents.size(); i++ )
		{
			ArrayListClinicalEngineerAgents.get(i).vSetReadWriteFile( iFileWriteMode );
		}
	}

	/**
	 * <PRE>
	 *    終了処理を実行します。
	 * </PRE>
	 * @throws IOException	IOクラス例外
	 */
	public synchronized void vTerminate() throws IOException
	{
		int i;

		synchronized( csEmergencyRoomCriticalSection )
		{
			// 患者エージェントの終了処理を行います。
			if( erCurrentPatientAgent != null )
			{
				erCurrentPatientAgent.vTerminate();
				this.getEngine().addExitAgent( erCurrentPatientAgent );
				erCurrentPatientAgent = null;
			}
			// 医師エージェントの終了処理を行います。
			if( ArrayListDoctorAgents != null )
			{
				for( i = ArrayListDoctorAgents.size()-1; i >= 0; i-- )
				{
					if( ArrayListDoctorAgents.get(i) != null )
					{
						ArrayListDoctorAgents.get(i).vTerminate();
						this.getEngine().addExitAgent( ArrayListDoctorAgents.get(i) );
						ArrayListDoctorAgents.set( i, null );
						ArrayListDoctorAgents.remove(i);
					}
				}
				ArrayListDoctorAgents = null;
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

			// 医療技師エージェントの終了処理を実行します。
			if( ArrayListClinicalEngineerAgents != null )
			{
				for( i = ArrayListClinicalEngineerAgents.size()-1; i >= 0; i-- )
				{
					if( ArrayListClinicalEngineerAgents.get(i) != null )
					{
						ArrayListClinicalEngineerAgents.get(i).vTerminate();
						this.getEngine().addExitAgent( ArrayListClinicalEngineerAgents.get(i) );
						ArrayListClinicalEngineerAgents.set( i, null );
						ArrayListClinicalEngineerAgents.remove(i);
					}
				}
				ArrayListClinicalEngineerAgents = null;
			}
			// ログ出力
			cEmergencyRoomLog = null;									// 初療室ログ出力設定

			// 乱数
			rnd = null;													// 乱数クラス

			// FUSEノード、リンク
			erTriageNodeManager = null;
			erTriageNode = null;

			lfTimeCourse = 0.0;
		}
	}

	/**
	 * <PRE>
	 *   初療室の医師エージェントを生成します。
	 * </PRE>
	 * @param iDoctorAgentNum	 医師エージェント数
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vCreateDoctorAgents( int iDoctorAgentNum )
	{
		int i;
		if( ArrayListDoctorAgents == null )
		{
			// 逆シミュレーションの場合に通ります。
			ArrayListDoctorAgents = new ArrayList<ERDoctorAgent>();
		}
		for( i = 0;i < iDoctorAgentNum; i++ )
		{
			ArrayListDoctorAgents.add( new ERDoctorAgent() );
		}
	}

	/**
	 * <PRE>
	 *   初療室の執刀医エージェントを設定します。
	 * </PRE>
	 * @param iLoc			 医師エージェント位置
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vSetSurgeonDoctorAgent( int iLoc )
	{
		if( ArrayListDoctorAgents == null )
		{
			ArrayListDoctorAgents = new ArrayList<ERDoctorAgent>();
		}
		// サイズが0の場合、追加します。
		if( ArrayListDoctorAgents.size() < 1 )
		{
			ArrayListDoctorAgents.add( new ERDoctorAgent() );
		}
		erSurgeonDoctorAgent = ArrayListDoctorAgents.get(iLoc);
		erSurgeonDoctorAgent.vSetSurgeon( 1 );
	}

	/**
	 * <PRE>
	 *   初療室の看護師エージェントを生成します。
	 * </PRE>
	 * @param iNurseAgentNum	看護師エージェント数
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
	 *   初療室の医療技師エージェントを生成します。
	 * </PRE>
	 * @param iClinicalEngineerAgentNum	医療技師エージェント数
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vCreateClinicalEngineerAgents( int iClinicalEngineerAgentNum )
	{
		int i;
		if( ArrayListClinicalEngineerAgents == null )
		{
			// 逆シミュレーションの場合にパラメータ更新の後の再シミュレーション実施前に通ります。
			ArrayListClinicalEngineerAgents = new ArrayList<ERClinicalEngineerAgent>();
		}
		for( i = 0;i < iClinicalEngineerAgentNum; i++ )
		{
			ArrayListClinicalEngineerAgents.add( new ERClinicalEngineerAgent() );
		}
	}

	/**
	 * <PRE>
	 *    医師エージェントのパラメータを設定します。
	 * </PRE>
	 * @param alfYearExperience			経験年数
	 * @param alfConExperience			経験数の重み
	 * @param alfExperienceRate1		経験年数パラメータ1
	 * @param alfExperienceRate2		経験年数パラメータ2
	 * @param alfConExperienceAIS		経験年数重み（重症度）
	 * @param alfExperienceRateAIS1		経験年数パラメータその１（重症度）
	 * @param alfExperienceRateAIS2		経験年数パラメータその２（重症度）
	 * @param alfConTired1				疲労度パラメータ1
	 * @param alfConTired2				疲労度パラメータ2
	 * @param alfConTired3				疲労度パラメータ3
	 * @param alfConTired4				疲労度パラメータ4
	 * @param alfTiredRate				疲労度重み
	 * @param alfRevisedOperationRate	手術室改善度割合
	 * @param alfAssociationRate		関連性パラメータ
	 * @param alfConsultationTime		診察時間
	 * @param alfOperationTime			手術時間
	 * @param alfEmergencyTime			初療室処置時間
	 * @param aiDepartment				所属部署
	 * @param aiRoomNumber				所属部屋番号
	 * @author kobayashi
	 * @since 2015/08/10
	 * @version 0.2
	 */
	public void vSetDoctorAgentParameter( double[] alfYearExperience,
										  double[] alfConExperience,
										  double[] alfExperienceRate1,
										  double[] alfExperienceRate2,
										  double[] alfConExperienceAIS,
										  double[] alfExperienceRateAIS1,
										  double[] alfExperienceRateAIS2,
										  double[] alfConTired1,
										  double[] alfConTired2,
										  double[] alfConTired3,
										  double[] alfConTired4,
										  double[] alfTiredRate,
										  double[] alfRevisedOperationRate,
										  double[] alfAssociationRate,
										  double[] alfConsultationTime,
										  double[] alfOperationTime,
										  double[] alfEmergencyTime,
										  int[] aiDepartment,
										  int[] aiRoomNumber )
	{
		int i;

		for( i = 0;i < ArrayListDoctorAgents.size(); i++ )
		{
			ArrayListDoctorAgents.get(i).vSetYearExperience( alfYearExperience[i] );
			ArrayListDoctorAgents.get(i).vSetConExperience( alfConExperience[i] );
			ArrayListDoctorAgents.get(i).vSetConTired1( alfConTired1[i] );
			ArrayListDoctorAgents.get(i).vSetConTired2( alfConTired2[i] );
			ArrayListDoctorAgents.get(i).vSetConTired3( alfConTired3[i] );
			ArrayListDoctorAgents.get(i).vSetConTired4( alfConTired4[i] );
			ArrayListDoctorAgents.get(i).vSetTiredRate( alfTiredRate[i] );
			ArrayListDoctorAgents.get(i).vSetRevisedOperationRate( alfRevisedOperationRate[i] );
			ArrayListDoctorAgents.get(i).vSetAssociationRate( alfAssociationRate[i] );
			ArrayListDoctorAgents.get(i).vSetConsultationTime( alfConsultationTime[i] );
			ArrayListDoctorAgents.get(i).vSetOperationTime( alfOperationTime[i] );
			ArrayListDoctorAgents.get(i).vSetEmergencyTime( alfEmergencyTime[i] );
			ArrayListDoctorAgents.get(i).vSetDoctorDepartment( aiDepartment[i] );
			ArrayListDoctorAgents.get(i).vSetExperienceRate1( alfExperienceRate1[i] );
			ArrayListDoctorAgents.get(i).vSetExperienceRate2( alfExperienceRate2[i] );
			ArrayListDoctorAgents.get(i).vSetConExperienceAIS( alfConExperienceAIS[i] );
			ArrayListDoctorAgents.get(i).vSetExperienceRateAIS1( alfExperienceRateAIS1[i] );
			ArrayListDoctorAgents.get(i).vSetExperienceRateAIS2( alfExperienceRateAIS2[i] );
			ArrayListDoctorAgents.get(i).vSetRoomNumber( aiRoomNumber[i] );
		}
	}

	/**
	 * <PRE>
	 *    初療室の看護師エージェントのパラメータを設定します。
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
	 * @version 0.2
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
	 *    医療技師エージェントのパラメータを設定します。
	 * </PRE>
	 * @param alfClinicalEngineerYearExperience			経験年数
	 * @param alfClinicalEngineerConExperience			経験数の重み
	 * @param alfExperienceRate1						経験年数パラメータ1
	 * @param alfExperienceRate2						経験年数パラメータ2
	 * @param alfConExperienceAIS						重症度判定パラメータ1
	 * @param alfExperienceRateAIS1						重症度判定パラメータ2
	 * @param alfExperienceRateAIS2						重症度判定パラメータ2
	 * @param alfClinicalEngineerConTired1				疲労度パラメータ1
	 * @param alfClinicalEngineerConTired2				疲労度パラメータ2
	 * @param alfClinicalEngineerConTired3				疲労度パラメータ3
	 * @param alfClinicalEngineerConTired4				疲労度パラメータ4
	 * @param alfClinicalEngineerTiredRate				疲労度重み
	 * @param alfClinicalEngineerAssociationRate		関連性パラメータ
	 * @param alfClinicalEngineerExaminationTime		診察時間
	 * @param aiClinicalEngineerDepartment				所属部署
	 * @param aiClinicalEngineerRoomNumber				所属部屋番号
	 * @since 2015/08/10
	 * @version 0.1
	 */
	public void vSetClinicalEngineerAgentParameter(
			double[] alfClinicalEngineerYearExperience,
			double[] alfClinicalEngineerConExperience,
			double[] alfExperienceRate1,
			double[] alfExperienceRate2,
			double[] alfConExperienceAIS,
			double[] alfExperienceRateAIS1,
			double[] alfExperienceRateAIS2,
			double[] alfClinicalEngineerConTired1,
			double[] alfClinicalEngineerConTired2,
			double[] alfClinicalEngineerConTired3,
			double[] alfClinicalEngineerConTired4,
			double[] alfClinicalEngineerTiredRate,
			double[] alfClinicalEngineerAssociationRate,
			double[] alfClinicalEngineerExaminationTime,
			int[] aiClinicalEngineerDepartment,
			int[] aiClinicalEngineerRoomNumber )
	{
		int i;

//		for( i = 0;i < ArrayListClinicalEngineerAgents.size(); i++ )
//		{
//			ArrayListClinicalEngineerAgents.get(i).vSetExaminationTime();
//			ArrayListClinicalEngineerAgents.get(i).vSetYearExperience( alfClinicalEngineerYearExperience[i] );
//			ArrayListClinicalEngineerAgents.get(i).vSetConExperience( alfClinicalEngineerConExperience[i] );
//			ArrayListClinicalEngineerAgents.get(i).vSetConTired1( alfClinicalEngineerConTired1[i] );
//			ArrayListClinicalEngineerAgents.get(i).vSetConTired2( alfClinicalEngineerConTired2[i] );
//			ArrayListClinicalEngineerAgents.get(i).vSetConTired3( alfClinicalEngineerConTired3[i] );
//			ArrayListClinicalEngineerAgents.get(i).vSetConTired4( alfClinicalEngineerConTired4[i] );
//			ArrayListClinicalEngineerAgents.get(i).vSetTiredRate( alfClinicalEngineerTiredRate[i] );
//			ArrayListClinicalEngineerAgents.get(i).vSetAssociationRate( alfClinicalEngineerAssociationRate[i] );
//			ArrayListClinicalEngineerAgents.get(i).vSetExaminationTime();
//			ArrayListClinicalEngineerAgents.get(i).vSetClinicalEngineerDepartment( aiClinicalEngineerDepartment[i] );
//			ArrayListClinicalEngineerAgents.get(i).vSetExperienceRate1( alfExperienceRate1[i] );
//			ArrayListClinicalEngineerAgents.get(i).vSetExperienceRate2( alfExperienceRate2[i] );
//			ArrayListClinicalEngineerAgents.get(i).vSetConExperienceAIS( alfConExperienceAIS[i] );
//			ArrayListClinicalEngineerAgents.get(i).vSetExperienceRateAIS1( alfExperienceRateAIS1[i] );
//			ArrayListClinicalEngineerAgents.get(i).vSetExperienceRateAIS2( alfExperienceRateAIS2[i] );
//			ArrayListClinicalEngineerAgents.get(i).vSetRoomNumber( aiRoomNumber[i] );
//		}
	}

	/**
	 * <PRE>
	 *    FUSEエンジンにエージェントを登録します。
	 * </PRE>
	 * @param engine	FUSEシミュレーションエンジン
	 */
	public void vSetSimulationEngine( SimulationEngine engine )
	{
		engine.addAgent(this);
	}

	/**
	 * <PRE>
	 *   手術室のプロセスを実行します。
	 * </PRE>
	 * @param ArrayListOperationRooms			手術室エージェント
	 * @param ArrayListIntensiveCareUnitRooms	ICUエージェント
	 * @param ArrayListHighCareUnitRooms		HCUエージェント
	 * @param ArrayListERGeneralWardRooms		一般病棟エージェント
	 * @throws ERDoctorAgentException			医師エージェントの例外
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vImplementEmergencyRoom( ArrayList<EROperationRoom> ArrayListOperationRooms, ArrayList<ERIntensiveCareUnitRoom> ArrayListIntensiveCareUnitRooms, ArrayList<ERHighCareUnitRoom> ArrayListHighCareUnitRooms, ArrayList<ERGeneralWardRoom> ArrayListERGeneralWardRooms ) throws ERDoctorAgentException
	{
		int i;

		synchronized( csEmergencyRoomCriticalSection )
		{
			// 初療室に患者がいる場合に実行します。
	 		if( erCurrentPatientAgent != null )
			{
				cEmergencyRoomLog.info(erCurrentPatientAgent.getId() + "," + "初療室対応中" );
				// 緊急処置を実行します。
				vImplementEmergency( ArrayListOperationRooms, ArrayListIntensiveCareUnitRooms, ArrayListHighCareUnitRooms, ArrayListERGeneralWardRooms, erSurgeonDoctorAgent, ArrayListNurseAgents, erCurrentPatientAgent );

				// 大量になくなられている場合のエラー対策。
				if( erCurrentPatientAgent != null )
				{
//					cEmergencyRoomLog.info(erCurrentPatientAgent.getId() + "," + "処置完全終了" );
					// 退院した患者がいる場合は、患者エージェントを削除します。
					if( erCurrentPatientAgent.iGetDisChargeFlag() == 1 )
					{
						erCurrentPatientAgent = null;
					}
					// 医師の対応が終了した場合、処置が終了した患者エージェントを削除します。
					if( erSurgeonDoctorAgent.iGetAttending() == 0 )
					{
						erCurrentPatientAgent = null;
					}
				}
			}
		}
	}

	/**
	 * <PRE>
	 *   緊急処置を実行します。
	 * </PRE>
	 * @param arrayListOperationRooms				全手術室
	 * @param ArrayListIntensiveCareUnitRooms		全集中治療室
	 * @param ArrayListHighCareUnitRooms			全高度治療室
	 * @param ArrayListGeneralWardRooms				全一般病棟
	 * @param erDoctorAgent							担当する医師エージェント
	 * @param ArrayListERNurseAgents				担当する看護師エージェント
	 * @param erPAgent								処置を受ける患者エージェント
	 * @throws ERDoctorAgentException				医師エージェントの例外
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	private void vImplementEmergency( ArrayList<EROperationRoom> arrayListOperationRooms, ArrayList<ERIntensiveCareUnitRoom> ArrayListIntensiveCareUnitRooms, ArrayList<ERHighCareUnitRoom> ArrayListHighCareUnitRooms, ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms, ERDoctorAgent erDoctorAgent, ArrayList<ERNurseAgent> ArrayListERNurseAgents, ERPatientAgent erPAgent ) throws ERDoctorAgentException
	{
		int i;
		// 医師エージェントの対応を実施します。
		erDoctorAgent.vSetAttending( 1 );
		erPAgent.vSetDoctorAgent(erDoctorAgent.getId());

		// 看護師エージェントの対応を実施します。
		for( i = 0;i < ArrayListERNurseAgents.size(); i++ )
		{
			ArrayListERNurseAgents.get(i).vSetAttending( 1 );
		}
		if( erPAgent.isMoveWaitingTime() == false )
		{
			// 移動時間がまだ終了していないので、移動を実施しません。

		// 移動中であることを医師、看護師エージェントに知らせます。

			erDoctorAgent.vSetPatientMoveWaitFlag( 1 );
			for( i = 0;i < ArrayListERNurseAgents.size(); i++ )
			{
				ArrayListERNurseAgents.get(i).vSetPatientMoveWaitFlag( 1 );
			}
//			erPAgent.vSetMoveRoomFlag( 1 );
			cEmergencyRoomLog.info(erPAgent.getId() + "," + "初療室移動時間：" + erPAgent.lfGetMoveWaitingTime() );
			return;
		}
		if( erPAgent.lfGetMoveWaitingTime() >= 182.0 )
		{
			cEmergencyRoomLog.info(erPAgent.getId() + "," + "大丈夫なんか変だよ：" + erPAgent.lfGetMoveWaitingTime() );
		}
		erPAgent.vSetMoveRoomFlag( 0 );
		erDoctorAgent.vSetPatientMoveWaitFlag( 0 );
		String strData = erPAgent.getId() + "," + erPAgent.lfGetInternalAISHead() + "," + erPAgent.lfGetInternalAISFace() + "," + erPAgent.lfGetInternalAISNeck() + "," + erPAgent.lfGetInternalAISThorax() + "," + erPAgent.lfGetInternalAISAbdomen() + "," + erPAgent.lfGetInternalAISSpine() +"," + erPAgent.lfGetInternalAISUpperExtremity() +"," + erPAgent.lfGetInternalAISLowerExtremity() + "," + erPAgent.lfGetInternalAISUnspecified();
		cEmergencyRoomLog.info(strData);
		for( i = 0;i < ArrayListERNurseAgents.size(); i++ )
		{
			ArrayListERNurseAgents.get(i).vSetPatientMoveWaitFlag( 0 );
		}

		// 部屋移動が終了したのでフラグOFFに処置中とします。
		erPAgent.vSetMoveRoomFlag( 0 );
		// 一般病棟移動待機フラグがONの場合
		if( erPAgent.iGetGeneralWardRoomWaitFlag() == 1 )
		{
			// 一般病棟へ移動します。
			vJudgeMoveGeneralWardRoom( ArrayListGeneralWardRooms, erDoctorAgent, ArrayListERNurseAgents, erPAgent );
			// 移動できるので、移動します。
			if( erPAgent.iGetGeneralWardRoomWaitFlag() == 0 ) return ;
		}
		// 高度治療室移動待機フラグがONの場合
		if( erPAgent.iGetHighCareUnitRoomWaitFlag() == 1 )
		{
			// 高度治療室へ移動判定を行います。
			vJudgeMoveHighCareUnitRoom( ArrayListHighCareUnitRooms, ArrayListGeneralWardRooms, erDoctorAgent, ArrayListERNurseAgents, erPAgent );
			// 移動できるので、移動します。
			if( erPAgent.iGetHighCareUnitRoomWaitFlag() == 0 ) return ;
		}
		if( erPAgent.iGetIntensiveCareUnitRoomWaitFlag() == 1 )
		{
			// 高度治療室へ移動判定を行います。
			vJudgeMoveIntensiveCareUnitRoom( ArrayListIntensiveCareUnitRooms, ArrayListHighCareUnitRooms, ArrayListGeneralWardRooms, erDoctorAgent, ArrayListERNurseAgents, erPAgent );
			// 移動できるので、移動します。
			if( erPAgent.iGetIntensiveCareUnitRoomWaitFlag() == 0 ) return ;
		}
		// 救急処置プロセスを実行します。
		vEmergencyProcess( arrayListOperationRooms, ArrayListIntensiveCareUnitRooms, ArrayListHighCareUnitRooms, ArrayListGeneralWardRooms, erDoctorAgent, ArrayListERNurseAgents, erPAgent );
	}

	/**
	 * <PRE>
	 *   初療室プロセスを実行します。
	 *   医師の診察プロセスを実行し、検査が必要な場合は
	 *   医療技師により検査を実施、最終的に手術を実行します。
	 *   終了後、ICUへ移動します。
	 * </PRE>
	 * @param ArrayListOperationRooms				全手術室
	 * @param ArrayListIntensiveCareUnitRooms		全集中治療室
	 * @param ArrayListHighCareUnitRooms			全高度治療室
	 * @param ArrayListGeneralWardRooms				全一般病棟
	 * @param erDoctorAgent							担当する医師エージェント
	 * @param ArrayListERNurseAgents				担当する看護師エージェント
	 * @param erPAgent								処置を受ける患者エージェント
	 * @throws ERDoctorAgentException				医師エージェントの例外
	 */
	private void vEmergencyProcess( ArrayList<EROperationRoom> ArrayListOperationRooms, ArrayList<ERIntensiveCareUnitRoom> ArrayListIntensiveCareUnitRooms, ArrayList<ERHighCareUnitRoom> ArrayListHighCareUnitRooms, ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms, ERDoctorAgent erDoctorAgent, ArrayList<ERNurseAgent> ArrayListERNurseAgents, ERPatientAgent erPAgent ) throws ERDoctorAgentException
	{
		int i;
		int iProcessResult = 0;
		double lfRand = 0.0;
		double lfEmergencyTime;

		// 初療室にいる看護師エージェント、医師エージェントの数から連携度を算出します。
		erDoctorAgent.lfCalcAssociateRateOperation(ArrayListDoctorAgents, ArrayListNurseAgents);

		// 患者の状態から予測される手術時間を推定します。
		if( bUnderOperationFlag == false )
		{
			erDoctorAgent.vSetOperationTime(erDoctorAgent.isJudgeOeprationTime( erPAgent ) );
			lfEmergencyTime = erDoctorAgent.lfGetOperationTime();
		}
		// 手術時間が経過していない場合
		if( erDoctorAgent.lfGetOperationTime() > erDoctorAgent.lfGetCurrentPassOverTime()-erPAgent.lfGetMoveTime() )
//		if( erDoctorAgent.lfGetOperationTime() > erDoctorAgent.lfGetCurrentPassOverTime() )
		{
			cEmergencyRoomLog.info(erPAgent.getId() +"," + "手術時間：" + erDoctorAgent.lfGetOperationTime() );
			cEmergencyRoomLog.info(erPAgent.getId() +"," + "対応時間：" + erDoctorAgent.lfGetCurrentPassOverTime() );
			// 何もせずに終了します。
			bUnderOperationFlag = true;
			return ;
		}
		bUnderOperationFlag = false;

		erPAgent.vSetMoveWaitingTime( 0.0 );

		// 手術を実行します。
		erDoctorAgent.iImplementEmergencyProcess( erPAgent );

		cEmergencyRoomLog.info(erPAgent.getId() +"," + "初療室対応終了" );

		// 集中治療室からの退院判定を次の通りとします。(京都府立病院データを参照。)
		if( erPAgent.iGetEmergencyLevel() == 5 )
		{
			lfRand = rnd.NextUnif();
//			if( lfRand > 0.033573 )
//			if( lfRand > 0.063573 )
			{
				// 初療室で治療を受けた患者の中で、少し緊急度がある患者であっても退院するものとします。
				// 医師エージェントが患者エージェントが退院可能かどうかを判定します。
				// 退院を実施します。（エージェントを消滅させます。）
				erPAgent.vSetDisChargeFlag( 1 );
				erPAgent.getEngine().addExitAgent(erPAgent);
				erPAgent.exitSimulation();
				// 登録されているエージェントで離脱したエージェントがいるかどうかを判定します。
				if( erPAgent.iGetDisChargeFlag() == 1 )
				{
					try{
						//いる場合は、ファイルに書き出しを実行します。
						erPAgent.vFlushFile( 0 );
					}
					catch( IOException ioe ){
					}
				}

				cEmergencyRoomLog.info(erPAgent.getId() +"," + "初療室：退院しました！。" + "," + erPAgent.iGetDisChargeFlag() );
//				System.out.println( "初療室：退院しました！。" );
				// 退院数をカウントします。
				iDisChargeNum++;

				// 医師エージェントの対応を終了します。
				erDoctorAgent.vSetAttending( 0 );

				// 看護師エージェントの対応も終了します。
				for( i = 0;i < ArrayListERNurseAgents.size(); i++ )
				{
					ArrayListERNurseAgents.get(i).vSetAttending(0);
				}
				return;
			}
		}
		else if( erPAgent.iGetEmergencyLevel() == 4 )
		{
			lfRand = rnd.NextUnif();
//			if( lfRand > 0.161016949 )
			if( lfRand > 0.301016949 )
			{
				// 初療室で治療を受けた患者の中で、少し緊急度がある患者であっても退院するものとします。
				// 医師エージェントが患者エージェントが退院可能かどうかを判定します。
				// 退院を実施します。（エージェントを消滅させます。）
				erPAgent.vSetDisChargeFlag( 1 );
				erPAgent.getEngine().addExitAgent(erPAgent);
				erPAgent.exitSimulation();
				// 登録されているエージェントで離脱したエージェントがいるかどうかを判定します。
				if( erPAgent.iGetDisChargeFlag() == 1 )
				{
					try{
						//いる場合は、ファイルに書き出しを実行します。
						erPAgent.vFlushFile( 0 );
					}
					catch( IOException ioe ){
					}
				}


				cEmergencyRoomLog.info(erPAgent.getId() +"," + "初療室：退院しました！。" + "," + erPAgent.iGetDisChargeFlag() );
//				System.out.println( "初療室：退院しました！。" );
				// 退院数をカウントします。
				iDisChargeNum++;

				// 医師エージェントの対応を終了します。
				erDoctorAgent.vSetAttending( 0 );

				// 看護師エージェントの対応も終了します。
				for( i = 0;i < ArrayListERNurseAgents.size(); i++ )
				{
					ArrayListERNurseAgents.get(i).vSetAttending(0);
				}
				return;
			}
		}
		else if( erPAgent.iGetEmergencyLevel() == 3 )
		{
			lfRand = rnd.NextUnif();
//			if( lfRand > 0.310423826 )
//			{
//				// 初療室で治療を受けた患者の中で、少し緊急度がある患者であっても退院するものとします。
//				// 医師エージェントが患者エージェントが退院可能かどうかを判定します。
//				// 退院を実施します。（エージェントを消滅させます。）
//				erPAgent.vSetDisChargeFlag( 1 );
//				erPAgent.getEngine().addExitAgent(erPAgent);
//				erPAgent.exitSimulation();
//				// 登録されているエージェントで離脱したエージェントがいるかどうかを判定します。
//				if( erPAgent.iGetDisChargeFlag() == 1 )
//				{
//					try{
//						//いる場合は、ファイルに書き出しを実行します。
//						erPAgent.vFlushFile( 0 );
//					}
//					catch( IOException ioe ){
//					}
//				}
//
//
//				cEmergencyRoomLog.info(erPAgent.getId() +"," + "初療室：退院しました！。" + "," + erPAgent.iGetDisChargeFlag() );
////				System.out.println( "初療室：退院しました！。" );
//				// 退院数をカウントします。
//				iDisChargeNum++;
//
//				// 医師エージェントの対応を終了します。
//				erDoctorAgent.vSetAttending( 0 );
//
//				// 看護師エージェントの対応も終了します。
//				for( i = 0;i < ArrayListERNurseAgents.size(); i++ )
//				{
//					ArrayListERNurseAgents.get(i).vSetAttending(0);
//				}
//				return;
//			}
		}
		else if( erPAgent.iGetEmergencyLevel() == 2 )
		{
			lfRand = rnd.NextUnif();
//			if( lfRand > 0.8 )
//			{
//				// 初療室で治療を受けた患者の中で、少し緊急度がある患者であっても退院するものとします。
//				// 医師エージェントが患者エージェントが退院可能かどうかを判定します。
//				// 退院を実施します。（エージェントを消滅させます。）
//				erPAgent.vSetDisChargeFlag( 1 );
//				erPAgent.getEngine().addExitAgent(erPAgent);
//				erPAgent.exitSimulation();
//				// 登録されているエージェントで離脱したエージェントがいるかどうかを判定します。
//				if( erPAgent.iGetDisChargeFlag() == 1 )
//				{
//					try{
//						//いる場合は、ファイルに書き出しを実行します。
//						erPAgent.vFlushFile( 0 );
//					}
//					catch( IOException ioe ){
//					}
//				}
//
//
//				cEmergencyRoomLog.info(erPAgent.getId() +"," + "初療室：退院しました！。" + "," + erPAgent.iGetDisChargeFlag() );
////				System.out.println( "初療室：退院しました！。" );
//				// 退院数をカウントします。
//				iDisChargeNum++;
//
//				// 医師エージェントの対応を終了します。
//				erDoctorAgent.vSetAttending( 0 );
//
//				// 看護師エージェントの対応も終了します。
//				for( i = 0;i < ArrayListERNurseAgents.size(); i++ )
//				{
//					ArrayListERNurseAgents.get(i).vSetAttending(0);
//				}
//				return;
//			}
		}
		// 集中治療室へ移動判定を実施します。
		vJudgeMoveIntensiveCareUnitRoom( ArrayListIntensiveCareUnitRooms, ArrayListHighCareUnitRooms, ArrayListGeneralWardRooms, erDoctorAgent, ArrayListERNurseAgents, erPAgent );
	}

	/**
	 * <PRE>
	 *    集中治療室への移動判定を行います。
	 * </PRE>
	 * @param ArrayListIntensiveCareUnitRooms	集中治療室エージェント
	 * @param ArrayListHighCareUnitRooms		高度治療室エージェント
	 * @param ArrayListGeneralWardRooms			一般病棟エージェント
	 * @param erDoctorAgent						担当医師エージェント
	 * @param ArrayListERNurseAgents			担当全看護師エージェント
	 * @param erPAgent							手術を受けている患者エージェント
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	private void vJudgeMoveIntensiveCareUnitRoom( ArrayList<ERIntensiveCareUnitRoom> ArrayListIntensiveCareUnitRooms, ArrayList<ERHighCareUnitRoom> ArrayListHighCareUnitRooms, ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms, ERDoctorAgent erDoctorAgent, ArrayList<ERNurseAgent> ArrayListERNurseAgents, ERPatientAgent erPAgent )
	{
		int i,j;
		int iJudgeCount = 0;
		ERDoctorAgent erEmergencyDoctorAgent;
		ERNurseAgent erEmergencyNurseAgent;
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			// 集中治療室に空きがある場合は観察室へエージェントを移動します。
			if( ArrayListIntensiveCareUnitRooms.get(i).isVacant() == true )
			{
				cEmergencyRoomLog.info(erPAgent.getId() +"," + "集中治療室へ移動準備開始" + "," + "初療室" );
				// 集中治療室待機フラグをOFFにします。
				erPAgent.vSetIntensiveCareUnitRoomWaitFlag( 0 );

				// 患者のいる位置を集中治療室に変更します。
				erPAgent.vSetLocation( 6 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 空きがある場合は集中治療室へ移動します。
				ArrayListIntensiveCareUnitRooms.get(i).vSetPatientAgent( erPAgent );

				// 看護師エージェントへメッセージを送信します。
				for( j = 0;j < ArrayListIntensiveCareUnitRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erEmergencyNurseAgent = ArrayListIntensiveCareUnitRooms.get(i).cGetNurseAgent(j);
					erDoctorAgent.vSendToNurseAgentMessage( erPAgent, erEmergencyNurseAgent, (int)erDoctorAgent.getId(), (int)erEmergencyNurseAgent.getId() );
				}
				// 医師エージェントへメッセージを送信します。
				for( j = 0;j < ArrayListIntensiveCareUnitRooms.get(i).iGetDoctorAgentsNum(); j++ )
				{
					erEmergencyDoctorAgent = ArrayListIntensiveCareUnitRooms.get(i).cGetDoctorAgent(j);
					erDoctorAgent.vSendToDoctorAgentMessage( erPAgent, (int)erDoctorAgent.getId(), (int)erEmergencyDoctorAgent.getId() );
				}

				// 医師エージェントの対応を終了します。
				erDoctorAgent.vSetAttending( 0 );

				// 看護師エージェントの対応も終了します。
				for( j= 0;j < ArrayListERNurseAgents.size(); j++ )
				{
					ArrayListERNurseAgents.get(j).vSetAttending(0);
				}

				cEmergencyRoomLog.info(erPAgent.getId() +"," + "集中治療室へ移動準備終了" + "," + "初療室" );
				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListIntensiveCareUnitRooms.get(i).erGetTriageNode() ) );
					cEmergencyRoomLog.info(erPAgent.getId() +"," + "集中治療室へ移動開始" + "," + "初療室" );
				}
				erPAgent = null;

				// 空いている看護師に割り当てます。
				ArrayListIntensiveCareUnitRooms.get(i).bAssignVacantNurse();

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListIntensiveCareUnitRooms.size() )
		{
			cEmergencyRoomLog.info(erPAgent.getId() +"," + "集中治療室満室" + "," + "初療室");
			for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
			{
				// 現在対応している患者よりも重症患者がいない場合は高度治療室に集中治療室にいる患者のうち
				// もっとも重症度の低い患者を移動させます。
				if( ArrayListIntensiveCareUnitRooms.get(i).bChangePatient(erPAgent, ArrayListHighCareUnitRooms, ArrayListGeneralWardRooms, erDoctorAgent ) == false )
				{
					cEmergencyRoomLog.info("初療室、重症患者の優先順位をちゃんとつけたよ～。");
					// 空きがない場合は高度治療室待機フラグをONにします。
					erPAgent.vSetHighCareUnitRoomWaitFlag( 1 );

					// 高度治療室へ移動可能かどうかを判定します。
					vJudgeMoveHighCareUnitRoom( ArrayListHighCareUnitRooms, ArrayListGeneralWardRooms, erDoctorAgent, ArrayListERNurseAgents, erPAgent );

					break;
				}
				// 該当患者がいない場合は自分を高度治療室へ移動します。
				else
				{
					erPAgent.vSetHighCareUnitRoomWaitFlag( 1 );
					// 高度治療室へ移動可能かどうかを判定します。
					vJudgeMoveHighCareUnitRoom( ArrayListHighCareUnitRooms, ArrayListGeneralWardRooms, erDoctorAgent, ArrayListERNurseAgents, erPAgent );

					break;
				}
			}
		}
	}

	/**
	 * <PRE>
	 *   高度治療室への移動判定を行います。
	 * </PRE>
	 * @param ArrayListHighCareUnitRooms		全高度治療室
	 * @param ArrayListGeneralWardRooms			全一般病棟
	 * @param erDoctorAgent						担当医師エージェント
	 * @param ArrayListERNurseAgents			担当看護師エージェント
	 * @param erPAgent							移動する患者エージェント
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	private void vJudgeMoveHighCareUnitRoom( ArrayList<ERHighCareUnitRoom> ArrayListHighCareUnitRooms, ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms, ERDoctorAgent erDoctorAgent, ArrayList<ERNurseAgent> ArrayListERNurseAgents, ERPatientAgent erPAgent )
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
				cEmergencyRoomLog.info(erPAgent.getId() +"," + "初療室から高度治療室へ移動します。");
				// 集中治療室待機フラグをOFFにします。
				erPAgent.vSetHighCareUnitRoomWaitFlag( 0 );

				// 患者のいる位置を高度治療室に変更します。
				erPAgent.vSetLocation( 7 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 初療室へ患者エージェントを移動します。
				ArrayListHighCareUnitRooms.get(i).vSetPatientAgent( erPAgent );

			// 看護師、医師、技士エージェントへメッセージを送信します。
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
//					erEmergencyCinicalEngineerAgent = ArrayListEmergencyRooms.get(i).cGetClinicalEngineerAgents(j);
//					erNurseAgent.vSendToEngineerAgentMessage( erPAgent, (int)erNurseAgent.getId(), (int)erEmergencyClinicalEngineerAgent.getId() );
//				}

				// 医師エージェントの対応を終了します。
				erDoctorAgent.vSetAttending( 0 );

				// 看護師エージェントの対応も終了します。
				for( j= 0;j < ArrayListERNurseAgents.size(); j++ )
				{
					ArrayListERNurseAgents.get(j).vSetAttending(0);
				}

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
			cEmergencyRoomLog.info(erPAgent.getId() +"," + "高度治療室満室" + "," + "初療室");
			// 空きがない場合は一般病棟待機フラグをONにします。
			erPAgent.vSetGeneralWardRoomWaitFlag( 1 );

			// 一般病棟への移動判定を実施ます。
			vJudgeMoveGeneralWardRoom( ArrayListGeneralWardRooms, erDoctorAgent, ArrayListERNurseAgents, erPAgent );
		}
	}

	/**
	 * <PRE>
	 *   一般病棟への移動判定を行います。
	 * </PRE>
	 * @param ArrayListGeneralWardRooms			全一般病棟
	 * @param erDoctorAgent						担当医師エージェント
	 * @param ArrayListERNurseAgents			担当看護師エージェント
	 * @param erPAgent							移動する患者エージェント
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	private void vJudgeMoveGeneralWardRoom( ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms, ERDoctorAgent erDoctorAgent, ArrayList<ERNurseAgent> ArrayListERNurseAgents, ERPatientAgent erPAgent )
	{
		int i,j;
		int iJudgeCount = 0;
		ERDoctorAgent erGeneralWardDoctorAgent;
		ERNurseAgent erGeneralWardNurseAgent;
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			// 初療室に空きがある場合
			if( ArrayListGeneralWardRooms.get(i).isVacant() == true )
			{
				cEmergencyRoomLog.info(erPAgent.getId() +"," + "初療室から一般病棟へ移動します。");
				// 一般病棟待機フラグをOFFにします。
				erPAgent.vSetGeneralWardRoomWaitFlag( 0 );

				// 患者のいる位置を一般病棟に変更します。
				erPAgent.vSetLocation( 8 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 初療室へ患者エージェントを移動します。
				ArrayListGeneralWardRooms.get(i).vSetPatientAgent( erPAgent );

				for(j = 0;j < ArrayListGeneralWardRooms.get(i).iGetDoctorAgentsNum(); j++ )
				{
					// 看護師、医師、技士エージェントへメッセージを送信します。
					erGeneralWardDoctorAgent = ArrayListGeneralWardRooms.get(i).cGetDoctorAgent(0);
					erDoctorAgent.vSendToDoctorAgentMessage( erPAgent, (int)erDoctorAgent.getId(), (int)erGeneralWardDoctorAgent.getId() );
				}
				for(j = 0;j < ArrayListGeneralWardRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					// 看護師、医師、技士エージェントへメッセージを送信します。
					erGeneralWardNurseAgent = ArrayListGeneralWardRooms.get(i).cGetNurseAgent(j);
					erDoctorAgent.vSendToNurseAgentMessage( erPAgent, erGeneralWardNurseAgent, (int)erDoctorAgent.getId(), (int)erGeneralWardNurseAgent.getId() );
				}
				// 医師エージェントの対応を終了します。
				erDoctorAgent.vSetAttending( 0 );

				// 看護師エージェントの対応も終了します。
				for( j= 0;j < ArrayListERNurseAgents.size(); j++ )
				{
					ArrayListERNurseAgents.get(j).vSetAttending(0);
				}

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListGeneralWardRooms.get(i).erGetTriageNode() ) );
				}
				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListGeneralWardRooms.size() )
		{
			cEmergencyRoomLog.info(erPAgent.getId() +"," + "初療室：一般病棟が満室です。");
			// 空きがない場合は一般病棟待機フラグをONにしてそのまま一般病棟の空きを判定します。
			erPAgent.vSetGeneralWardRoomWaitFlag( 1 );

			// 空きがない場合は転院を促します。つまり退院処置を行います。
			if( rnd.NextUnif() > 1.0 )
			{
				// 医師エージェントが患者エージェントが退院可能かどうかを判定します。
				if( erDoctorAgent.isJudgeDischarge( erPAgent ) == true )
				{
					cEmergencyRoomLog.info(erPAgent.getId() + ","  + "一般病棟：退院しました！。");
					// 退院を実施します。（エージェントを消滅させます。）
					erPAgent.getEngine().addExitAgent( erPAgent );

					// 退院数をカウントします。
					iDisChargeNum++;

					erPAgent = null;
				}
			}
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
		erCurrentPatientAgent = erPAgent;
	}

	/**
	 * <PRE>
	 *    対応が終了した患者を削除します。
	 * </PRE>
	 */
	public void vRemovePatientAgent()
	{
		erCurrentPatientAgent = null;
	}

	/**
	 * <PRE>
	 *   初療室の担当医師エージェントを取得します。
	 * </PRE>
	 * @return	担当エージェントのインスタンス
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public ERDoctorAgent cGetSurgeonDoctorAgent()
	{
		return erSurgeonDoctorAgent;
	}

	/**
	 * <PRE>
	 *    初療室の担当医師エージェントを割り当てます。
	 * </PRE>
	 */
	public void vSetSurgeonDoctorAgent()
	{
		erSurgeonDoctorAgent.vSetSurgeon( 1 );
	}

	/**
	 * <PRE>
	 *   初療室の医師エージェントを取得します。
	 * </PRE>
	 * @param i 所属している医師の番号
	 * @return	該当番号の医師エージェントインスタンス
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public ERDoctorAgent cGetDoctorAgent( int i )
	{
		return ArrayListDoctorAgents.get(i);
	}

	/**
	 * <PRE>
	 *   初療室の看護師エージェントの数を取得します。
	 * </PRE>
	 * @return	所属している看護師エージェントの総数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public int iGetDoctorAgentsNum()
	{
		return ArrayListDoctorAgents.size();
	}

	/**
	 * <PRE>
	 *   初療室の看護師エージェントを取得します。
	 * </PRE>
	 * @param i 所属している看護師の番号
	 * @return	該当番号の看護師エージェントのインスタンス
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
	 * @return	看護師エージェントの数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public int iGetNurseAgentsNum()
	{
		return ArrayListNurseAgents.size();
	}

	/**
	 * <PRE>
	 *   初療室の医療技師エージェントを取得します。
	 * </PRE>
	 * @param i 所属している医療技師の番号
	 * @return	該当する医療技師エージェントのインスタンス
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public ERClinicalEngineerAgent cGetClinicalEngineerAgent( int i )
	{
		return ArrayListClinicalEngineerAgents.get(i);
	}

	/**
	 * <PRE>
	 *   初療室の医療技師エージェントの数を取得します。
	 * </PRE>
	 * @return	医療技師エージェントの数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public int iGetClinicalEngineerAgentsNum()
	{
		return ArrayListClinicalEngineerAgents.size();
	}

	/**
	 * <PRE>
	 *   初療室の患者エージェントの数を取得します。
	 *   nullでなければ1人いるので1と返却します。
	 * </PRE>
	 * @return	現在初療室にいる患者の数
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public int iGetPatientAgentsNum()
	{
		return erCurrentPatientAgent != null ? 1 : 0;
	}

	/**
	 * <PRE>
	 *    手術室医師が対応中かどうかを判定します。
	 * </PRE>
	 * @return false 全員対応している
	 *         true  空きの医師がいる
	 */
	public boolean isVacant()
	{
		boolean bRet = true;

		// 部屋は存在しているが所属医師がいない場合は対応できないので空いていないとします。
		// 通常はないが・・・。
		if( erSurgeonDoctorAgent == null )
		{
			bRet = false;
		}
		// 所属医師が全員対応中の場合、空いていないとします。
		if( erSurgeonDoctorAgent.iGetAttending() == 1 )
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
		lfSecond = timeStep / 1000.0;
		lfTimeCourse += lfSecond;

		try
		{
			synchronized( csEmergencyRoomCriticalSection )
			{
				// 死亡患者がいる場合は削除します。
				if( erCurrentPatientAgent != null )
				{
					if( erCurrentPatientAgent.iGetSurvivalFlag() == 0 || erCurrentPatientAgent.iGetDisChargeFlag() == 1 )
					{
						// 登録されているエージェントで離脱したエージェントがいるかどうかを判定します。
						if( erCurrentPatientAgent.isExitAgent() == true )
						{
							// いる場合は、ファイルに書き出しを実行します。
							erCurrentPatientAgent.vFlushFile( 0 );
						}
						erCurrentPatientAgent = null;
						// さらに執刀医エージェントの対応中フラグを対応していないに設定します。
						erSurgeonDoctorAgent.vSetAttending( 0 );
						// 他の医師も対応していない状態にします。
						for( i = 0;i < ArrayListDoctorAgents.size(); i++ )
						{
							ArrayListDoctorAgents.get(i).vSetAttending( 0 );
						}
						// 看護師も同様に対応していない状態にします。
						for( i = 0;i < ArrayListNurseAgents.size(); i++ )
						{
							ArrayListNurseAgents.get(i).vSetAttending( 0 );
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
	 *    初療室のログ出力設定をします。
	 * </PRE>
	 * @param log	ロガークラスインスタンス
	 */
	public void vSetLog(Logger log)
	{
		// TODO 自動生成されたメソッド・スタブ
		cEmergencyRoomLog = log;
	}

	/**
	 * <PRE>
	 *    初療室のX座標を取得します。
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
	 *    初療室のY座標を取得します。
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
	 *    初療室の横幅を取得します。
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
	 *    初療室の縦幅を取得します。
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
	 *    初療室の階数を取得します。
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
	 *    初療室のX座標を格納します。
	 * </PRE>
	 * @param iData	X座標
	 */
	public void vSetX( int iData )
	{
		iDrawX = iData;
	}

	/**
	 * <PRE>
	 *    初療室のY座標を格納します。
	 * </PRE>
	 * @param iData	Y座標
	 */
	public void vSetY( int iData )
	{
		iDrawY = iData;
	}

	/**
	 * <PRE>
	 *    初療室のZ座標を格納します。
	 * </PRE>
	 * @param iData	Z座標
	 */
	public void vSetZ( int iData )
	{
		iDrawZ = iData;
	}

	/**
	 * <PRE>
	 *    初療室の横幅を格納します。
	 * </PRE>
	 * @param iData	横幅
	 */
	public void vSetWidth( int iData )
	{
		iDrawWidth = iData;
	}

	/**
	 * <PRE>
	 *    初療室の縦幅を格納します。
	 * </PRE>
	 * @param iData	縦幅
	 */
	public void vSetHeight( int iData )
	{
		iDrawHeight = iData;
	}

	/**
	 * <PRE>
	 *    初療室の階数を格納します。
	 * </PRE>
	 * @param iData	階数
	 */
	public void vSetF( int iData )
	{
		iDrawF = iData;
	}

	/**
	 * <PRE>
	 *   初療室に所属しているエージェントの座標を設定します。
	 * </PRE>
	 */
	public void vSetAffiliationAgentPosition()
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;

		double lfX = 0.0;
		double lfY = 0.0;
		double lfZ = 0.0;

		for( i = 0;i < ArrayListDoctorAgents.size(); i++ )
		{
			// 医師エージェントの位置を設定します。
			lfX = this.getPosition().getX()+3*rnd.NextUnif();
			lfY = this.getPosition().getY()+3*rnd.NextUnif();
			lfZ = this.getPosition().getZ()+3*rnd.NextUnif();
			ArrayListDoctorAgents.get(i).setPosition( lfX, lfY, lfZ );
		}
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			// 看護師エージェントの位置を設定します。
			lfX = this.getPosition().getX()+15*(2*rnd.NextUnif()-1);
			lfY = this.getPosition().getY()+15*(2*rnd.NextUnif()-1);
			lfZ = this.getPosition().getZ()+15*(2*rnd.NextUnif()-1);
			ArrayListNurseAgents.get(i).setPosition( lfX, lfY, lfZ );
		}
		for( i = 0;i < ArrayListClinicalEngineerAgents.size(); i++ )
		{
			// 医療技師エージェントの位置を設定します。
			lfX = this.getPosition().getX()+15*(2*rnd.NextUnif()-1);
			lfY = this.getPosition().getY()+15*(2*rnd.NextUnif()-1);
			lfZ = this.getPosition().getZ()+15*(2*rnd.NextUnif()-1);
			ArrayListClinicalEngineerAgents.get(i).setPosition( lfX, lfY, lfZ );
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
	 *    現在選択されている診察室のノードを取得します。
	 * </PRE>
	 * @return	初療室のノード
	 */
	public ERTriageNode erGetTriageNode()
	{
		return erTriageNode;
	}

	/**
	 * <PRE>
	 *   初療室のノードを設定します。
	 * </PRE>
	 * @param erNode	設定するノードインスタンス（初療室）
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

		for( i = 0;i < ArrayListDoctorAgents.size(); i++ )
		{
			ArrayListDoctorAgents.get(i).vSetInverseSimMode( iMode );
		}

		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetInverseSimMode( iMode );
		}

		for( i = 0;i < ArrayListClinicalEngineerAgents.size(); i++ )
		{
			ArrayListClinicalEngineerAgents.get(i).vSetInverseSimMode( iMode );
		}
	}

	/**
	 * <PRE>
	 *    現時点で患者がいるかどうかを取得します。
	 *    1室1人を前提としているのでいる場合は1をへ返却します。
	 * </PRE>
	 * @return	処置を受けている患者数
	 */
	public synchronized int iGetPatientInARoom()
	{
		// TODO 自動生成されたメソッド・スタブ

		synchronized( csEmergencyRoomCriticalSection )
		{
			if( erCurrentPatientAgent != null )
			{
				if( erCurrentPatientAgent.iGetSurvivalFlag() == 1 )
				{
					return 1;
				}
			}
		}
		return 0;
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
	public void vSetCriticalSection(Object cs )
	{
		// TODO 自動生成されたメソッド・スタブ
		csEmergencyRoomCriticalSection = cs;
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
	 *   (室所属する医師エージェント)
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vSetDoctorsRandom()
	{
		int i;
		for( i = 0;i < ArrayListDoctorAgents.size(); i++ )
		{
			ArrayListDoctorAgents.get(i).vSetRandom( rnd );
		}
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

	public void vSetClinicalEngineersRandom()
	{
		int i;
		for( i = 0;i < ArrayListClinicalEngineerAgents.size(); i++ )
		{
			ArrayListClinicalEngineerAgents.get(i).vSetRandom( rnd );
		}
	}
	/**
	 * <PRE>
	 *   現時点でのトリアージ緊急度別受診数の患者の数を求めます。
	 * </PRE>
	 * @param iCategory	トリアージ緊急度
	 * @author kobayashi
	 * @since 2016/07/27
	 * @return	指定した緊急度基準における患者の人数
	 */
	public int iGetTriageCategoryPatientNum( int iCategory )
	{
		int i;
		int iCategoryPatientNum = 0;
		if( erCurrentPatientAgent != null )
		{
			if( iCategory == erCurrentPatientAgent.iGetEmergencyLevel()-1 )
			{
				iCategoryPatientNum++;
			}
		}
		return iCategoryPatientNum;
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
		double lfLongestStayTime = -100000000000.0;
		if( erCurrentPatientAgent != null )
		{
			if( erCurrentPatientAgent.lfGetTimeCourse() > 0.0 )
			{
				lfLongestStayTime = erCurrentPatientAgent.lfGetTimeCourse();
			}
		}
		return lfLongestStayTime;
	}

	/**
	 * <PRE>
	 *    現在、最後に病床に入った患者の到着から入院までの時間を算出します。
	 *    NEDOCS用に使用する関数です。
	 * </PRE>
	 */
	public void vLastBedTime()
	{
		if( erCurrentPatientAgent != null )
		{
			if( erCurrentPatientAgent.lfGetTimeCourse() > 0.0 )
			{
				if( erCurrentPatientAgent.lfGetHospitalStayTime() > 0.0 )
				{
					lfLongestTotalTime = erCurrentPatientAgent.lfGetTotalTime();
					lfLastBedTime = erCurrentPatientAgent.lfGetTimeCourse()-erCurrentPatientAgent.lfGetHospitalStayTime();
				}
				// まだ入院していない場合は0とします。
				if( erCurrentPatientAgent.lfGetHospitalStayTime() == 0.0 )
				{
					lfLongestTotalTime = 0.0;
					lfLastBedTime = 0.0;
				}
			}
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
